package com.example.flea_market_app.auth.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.flea_market_app.auth.domain.AuthUser;
import com.example.flea_market_app.auth.repository.AuthUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

	private final AuthUserProvider authUserProvider;
	private final AuthUserRepository authUserRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		AuthUser authUser;
		try {
			authUser = authUserProvider.loadByIdentifier(username);
		} catch (RuntimeException e) {
			throw new UsernameNotFoundException("User not found: " + username, e);
		}

		List<String> roles = new ArrayList<>(List.of("ROLE_USER"));
		authUserRepository.findByEmail(username)
				.filter(e -> e.isAdmin())
				.ifPresent(e -> roles.add("ROLE_ADMIN"));

		List<GrantedAuthority> authorities = roles.stream()
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());

		java.util.UUID userId = java.util.UUID.fromString(authUser.getUserId());
		return new UserIdUserDetails(userId, authUser.getPasswordHash(), authorities);
	}
}
