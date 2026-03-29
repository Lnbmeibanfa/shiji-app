package com.shiji.api.config.security;

/**
 * 已认证用户标识，供 {@link org.springframework.security.core.Authentication#getPrincipal()} 使用。
 */
public record AuthPrincipal(Long userId) {}
