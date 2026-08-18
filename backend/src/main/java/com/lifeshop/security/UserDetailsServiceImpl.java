package com.lifeshop.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 第一阶段使用占位实现，后续阶段从sys_admin/biz_user/biz_merchant按角色加载。
        throw new UsernameNotFoundException("用户不存在: " + username);
    }

    public UserDetails buildTempDeveloper(String username, String encodedPassword) {
        return new User(username, encodedPassword, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
