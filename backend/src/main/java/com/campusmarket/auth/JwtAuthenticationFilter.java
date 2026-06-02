package com.campusmarket.auth;

import com.campusmarket.user.entity.User;
import com.campusmarket.user.mapper.UserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器 — 每次请求都会经过这里
 *
 * OncePerRequestFilter：Spring 提供的基类，保证同一个请求只过滤一次
 *
 * 流程：
 * 1. 检查是否为公开接口 → 是则直接放行
 * 2. 从请求头取 Authorization: Bearer <token>
 * 3. 验证 token 有效性
 * 4. 从 token 解析 userId，查数据库获取用户
 * 5. 把用户存入 request attribute，后续 Controller 可以取出使用
 * 6. 放行到 Controller
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // CORS 预检请求 (OPTIONS) — 直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // 公开接口：不需要 token，但如果带了 token 也解析出用户（供 myVote 等场景使用）
        if (isPublicPath(path, request.getMethod())) {
            trySetCurrentUser(request);
            chain.doFilter(request, response);
            return;
        }

        // 提取 Authorization 头
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(response, 401, "未登录，请先登录");
            return;
        }

        // 解析 token（去掉 "Bearer " 前缀）
        String token = authHeader.substring(7);

        // 验证 token
        if (!jwtProvider.validateToken(token)) {
            sendError(response, 401, "登录已过期，请重新登录");
            return;
        }

        // 从 token 中获取 userId，查数据库获取用户
        Long userId = jwtProvider.getUserIdFromToken(token);
        User user = userMapper.selectById(userId);

        if (user == null) {
            sendError(response, 401, "用户不存在");
            return;
        }

        // 检查账号是否被封禁
        if ("BANNED".equals(user.getStatus())) {
            sendError(response, 403, "账号已被封禁，请联系管理员");
            return;
        }

        // 检查管理员路径：需要 ADMIN 角色
        if (path.contains("/v1/admin/")) {
            String role = jwtProvider.getRoleFromToken(token);
            if (!"ADMIN".equals(role)) {
                sendError(response, 403, "权限不足，需要管理员权限");
                return;
            }
        }

        // 清除密码，保证安全
        user.setPassword(null);

        // 把用户存入 request，后续 Controller 通过 request.getAttribute("currentUser") 获取
        request.setAttribute("currentUser", user);

        // 放行到 Controller
        chain.doFilter(request, response);
    }

    /**
     * 判断哪些路径不需要认证
     *
     * 公开接口：
     * - 认证相关：/v1/auth/**（登录、注册）
     * - 商品浏览：GET /v1/products/**（列表、详情）
     * - 分类：GET /v1/categories/**
     * - 文件访问：/v1/files/**
     * - 健康检查：/v1/health
     * - Swagger 文档
     */
    private boolean isPublicPath(String path, String method) {
        if (path.contains("/v1/auth/")) return true;
        if (path.contains("/v1/health")) return true;
        // 文件查看公开（GET），文件上传需登录（POST 不在此放行）
        if ("GET".equalsIgnoreCase(method) && path.contains("/v1/files/")) return true;
        if (path.contains("/swagger-ui")) return true;
        if (path.contains("/v3/api-docs")) return true;

        // 商品列表和详情允许匿名访问，但 /mine 需要登录
        if ("GET".equalsIgnoreCase(method) && path.contains("/v1/products")
                && !path.contains("/v1/products/mine")) return true;

        // 分类允许匿名访问
        if ("GET".equalsIgnoreCase(method) && path.contains("/v1/categories")) return true;

        // 社区帖子列表、详情、评论（GET 公开）
        if ("GET".equalsIgnoreCase(method) && path.contains("/v1/posts")) return true;

        // 社区主题（GET 公开）
        if ("GET".equalsIgnoreCase(method) && path.contains("/v1/topics")) return true;

        // 常见问题（GET 公开）
        if ("GET".equalsIgnoreCase(method) && path.contains("/v1/faqs")) return true;

        // WebSocket 端点允许匿名访问（认证由 StompAuthInterceptor 处理）
        if (path.contains("/ws")) return true;

        return false;
    }

    /**
     * 发送 JSON 格式的错误响应（不是抛出异常，因为 Filter 层在 Spring 异常处理器之外）
     */
    /**
     * 尝试从请求中解析 token 并设置 currentUser（不强制要求登录）。
     * 用于公开接口也能获取当前用户身份（如评论列表的 myVote）。
     */
    private void trySetCurrentUser(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        String token = authHeader.substring(7);
        if (!jwtProvider.validateToken(token)) {
            return;
        }
        Long userId = jwtProvider.getUserIdFromToken(token);
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null);
            request.setAttribute("currentUser", user);
        }
    }

    private void sendError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + code + ",\"message\":\"" + message + "\",\"data\":null}");
    }
}
