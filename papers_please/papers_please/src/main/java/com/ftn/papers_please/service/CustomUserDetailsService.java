package com.ftn.papers_please.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ftn.papers_please.exceptions.UsernameTakenException;
import com.ftn.papers_please.model.user.TRole;
import com.ftn.papers_please.model.user.TUser;
import com.ftn.papers_please.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
    private PasswordEncoder passwordEncoder;

	@Override
	public UserDetails loadUserByUsername(String username) {
		
		TUser user = userRepository.findByUsername(username);
		if (user == null)
			throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
		else {
			List<GrantedAuthority> authorities = new ArrayList<>();

			if (user.getRoles() != null)
				for (String role : user.getRoles().getRole().stream().map(TRole::getRole).distinct().collect(Collectors.toList()))
					authorities.add(new SimpleGrantedAuthority(role));

			return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
		}
	}

	public TUser findById(String userId) {
		return userRepository.findById(userId);
	}

	public TUser findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	public void registerUser(TUser user) {
		if (userRepository.findByUsername(user.getUsername()) != null)
			throw new UsernameTakenException();
		
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		userRepository.save(user);
	}

	public void update(TUser user) {
		userRepository.update(user);
	}

    public List<TUser> findAll() {
		return userRepository.findAll();
    }
    
}
