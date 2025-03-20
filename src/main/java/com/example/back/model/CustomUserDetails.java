package com.example.back.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
	private final String userId;  // ✅ `userNo` → `userId`로 명확하게 변경
	private final String username;
	private final String password;

	public CustomUserDetails(User user) {
		this.userId = String.valueOf(user.getUserNo()); // ✅ `int` → `String` 변환
		this.username = user.getUsername();
		this.password = user.getPassword();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_USER"));
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	// ✅ `userNo` → `userId`로 네이밍 통일
	public String getUserId() {
		return this.userId;
	}
}
