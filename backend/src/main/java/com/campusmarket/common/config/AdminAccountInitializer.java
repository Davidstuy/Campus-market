package com.campusmarket.common.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campusmarket.user.entity.User;
import com.campusmarket.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 应用启动时自动初始化管理员账号
 *
 * 如果 admin 账号不存在，自动创建 admin/admin123
 * CommandLineRunner：Spring Boot 启动完成后自动执行
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, "admin");

        if (userMapper.selectCount(wrapper) == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("管理员");
            admin.setRole("ADMIN");
            admin.setStatus("ACTIVE");
            userMapper.insert(admin);
            log.info("============================================");
            log.info("  管理员账号已创建：admin / admin123");
            log.info("  请登录后立即修改密码！");
            log.info("============================================");
        }
    }
}
