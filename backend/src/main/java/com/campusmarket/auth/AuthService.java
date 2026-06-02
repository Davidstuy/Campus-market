package com.campusmarket.auth;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campusmarket.auth.dto.LoginRequest;
import com.campusmarket.auth.dto.LoginResponse;
import com.campusmarket.auth.dto.RegisterRequest;
import com.campusmarket.common.exception.BusinessException;
import com.campusmarket.user.entity.User;
import com.campusmarket.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务 — 处理注册和登录的核心业务逻辑
 *
 * 关键知识点：
 * BCryptPasswordEncoder.encode(rawPassword) — 把明文密码加密为 bcrypt 密文
 * BCryptPasswordEncoder.matches(rawPassword, encodedPassword) — 比对明文和密文是否匹配
 *
 * @RequiredArgsConstructor — Lombok：为所有 final 字段生成构造器，Spring 自动注入
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtProvider jwtProvider;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 注册流程：
     * 1. 检查用户名是否已存在 → 抛 BusinessException
     * 2. 加密密码
     * 3. 保存用户
     */
    public void register(RegisterRequest request) {
        // 检查用户名唯一性
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", request.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 检查手机号唯一性
        QueryWrapper<User> phoneWrapper = new QueryWrapper<>();
        phoneWrapper.eq("phone", request.getPhone());
        if (userMapper.selectCount(phoneWrapper) > 0) {
            throw new BusinessException("该手机号已注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));  // bcrypt 加密！
        user.setNickname(request.getUsername());  // 默认昵称 = 用户名
        user.setPhone(request.getPhone());

        userMapper.insert(user);
    }

    /**
     * 登录流程：
     * 1. 根据用户名查找用户 → 不存在则抛异常
     * 2. 校验密码 → 不匹配则抛异常
     * 3. 生成 JWT token
     * 4. 返回 token + 用户信息
     *
     * 注意：登录失败不告诉用户具体是"用户名不存在"还是"密码错误"
     * 统一说"用户名或密码错误"，防止攻击者枚举用户名
     */
    public LoginResponse login(LoginRequest request) {
        // 查找用户
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", request.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查账号是否被封禁
        if ("BANNED".equals(user.getStatus())) {
            throw new BusinessException("账号已被封禁，请联系管理员");
        }

        // 生成 token
        String token = jwtProvider.generateToken(user.getId(), user.getRole());

        // 清除密码再返回（安全考虑：绝不把密码密文泄露给前端）
        user.setPassword(null);

        return new LoginResponse(token, user);
    }
}
