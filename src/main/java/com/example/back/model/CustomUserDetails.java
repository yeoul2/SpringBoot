package com.example.back.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
	private final int userNo;
	private final String username;
	private final String password;

	public CustomUserDetails(User user) {
		this.userNo = user.getUserNo();
		this.username = user.getUsername();
		this.password = user.getPassword();
	}

	// 모든 UserDetails 메서드를 구현해야 함.
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// 예를 들어, 권한을 설정하고 리턴하는 부분
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

	// `userNo`는 `UserDetails`에 포함된 메서드가 아니므로 따로 설정
	public int getUserNo() {
		return this.userNo;
	}
}
