package com.ftn.papers_please.controller;

import javax.validation.Valid;

import com.ftn.papers_please.model.user.TRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ftn.papers_please.dto.LoginDTO;
import com.ftn.papers_please.dto.RegistrationDTO;
import com.ftn.papers_please.exceptions.PasswordsDoNotMatchException;
import com.ftn.papers_please.model.user.TUser;
import com.ftn.papers_please.model.user.TUser.Roles;
import com.ftn.papers_please.security.TokenUtils;
import com.ftn.papers_please.service.CustomUserDetailsService;
import com.ftn.papers_please.service.EmailService;


@RestController
@RequestMapping(value = "/api/users")
public class UserController {
	
	@Autowired
	private CustomUserDetailsService userDetailsService;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private TokenUtils tokenUtils;
	
	@Autowired
	private EmailService emailService;
	
	@PostMapping(value = "/register")
	public ResponseEntity<String> register(@Valid @RequestBody RegistrationDTO dto) {
		
		if (!dto.getPassword().equals(dto.getPasswordConfirm()))
			throw new PasswordsDoNotMatchException();
		
		TUser user = new TUser();
		user.setUsername(dto.getUsername());
		user.setPassword(dto.getPassword());
		user.setName(dto.getName());
		user.setSurname(dto.getSurname());
		user.setEmail(dto.getEmail());

		Roles roles = new Roles();
		if (dto.getIsEditor()) {
			roles.getRole().add(new TRole("ROLE_REVIEWER"));
			roles.getRole().add(new TRole("ROLE_EDITOR"));
		}
		roles.getRole().add(new TRole("ROLE_AUTHOR"));
		user.setRoles(roles);
		
		userDetailsService.registerUser(user);
		try {
			emailService.registrationEmail(user.getEmail());
		} catch (MailException | InterruptedException e) {
			e.printStackTrace();
		}
		
		return new ResponseEntity<>("Registration was successful!", HttpStatus.CREATED);
	}
	
	@PostMapping(value = "/login")
	public ResponseEntity<String> login(@Valid @RequestBody LoginDTO dto) {
		
		try {
			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword());
			UserDetails userDetails = userDetailsService.loadUserByUsername(dto.getUsername());
			String generatedToken = tokenUtils.generateToken(userDetails);
			
			authenticationManager.authenticate(token);
			
			return new ResponseEntity<String>(generatedToken, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<String>("Login failed!", HttpStatus.BAD_REQUEST);
		}
	}

}
