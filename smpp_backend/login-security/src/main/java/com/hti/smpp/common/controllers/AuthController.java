package com.hti.smpp.common.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hti.smpp.common.email.EmailSender;
import com.hti.smpp.common.exception.InvalidOtpException;
import com.hti.smpp.common.exception.InvalidPasswordException;
import com.hti.smpp.common.exception.NullValueException;
import com.hti.smpp.common.login.dto.ERole;
import com.hti.smpp.common.login.dto.Role;
import com.hti.smpp.common.login.dto.User;
import com.hti.smpp.common.login.repository.RoleRepository;
import com.hti.smpp.common.login.repository.UserRepository;
import com.hti.smpp.common.payload.request.LoginRequest;
import com.hti.smpp.common.payload.request.PasswordForgotRequest;
import com.hti.smpp.common.payload.request.PasswordUpdateRequest;
import com.hti.smpp.common.payload.request.SignupRequest;
import com.hti.smpp.common.payload.response.JwtResponse;
import com.hti.smpp.common.payload.response.MessageResponse;
import com.hti.smpp.common.security.jwt.JwtUtils;
import com.hti.smpp.common.security.services.UserDetailsImpl;
import com.hti.smpp.common.util.Constant;
import com.hti.smpp.common.util.EmailValidator;
import com.hti.smpp.common.util.OTPGenerator;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder encoder;

	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private EmailSender emailSender;

	@PostMapping("/login")
	public JwtResponse authenticateUser(@RequestBody LoginRequest loginRequest) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
				.collect(Collectors.toList());

		return new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles);
	}

	@GetMapping("/validate")
	public String validateJwtToken(@RequestParam("token") String token) {
		String jwt = null;
		if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
			jwt = token.substring(7);
		}
		if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
			String username = jwtUtils.getUserNameFromJwtToken(jwt);
			return "token is valid";
		}

		return "token is invalid";

	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
		}
		if (!EmailValidator.isEmailValid(signUpRequest.getEmail())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is not valid."));
		}
		// Create new user's account
		User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(),
				encoder.encode(signUpRequest.getPassword()));

		Set<String> strRoles = signUpRequest.getRole();
		Set<Role> roles = new HashSet<>();

		if (strRoles == null) {
			Role userRole = roleRepository.findByName(ERole.ROLE_USER)
					.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
			roles.add(userRole);
		} else {
			strRoles.forEach(role -> {
				switch (role) {
				case "admin":
					Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(adminRole);

					break;

				case "superadmin":
					Role superAdminRole = roleRepository.findByName(ERole.ROLE_SUPERADMIN)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(superAdminRole);

					break;
				case "system":
					Role systemRole = roleRepository.findByName(ERole.ROLE_SYSTEM)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(systemRole);

				case "user":
					Role userRole = roleRepository.findByName(ERole.ROLE_USER)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(userRole);
				}
			});
		}

		user.setRoles(roles);
		userRepository.save(user);

		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}

	@GetMapping("/password/forgot")
	public ResponseEntity<?> forgotPassword(@Valid @RequestBody PasswordForgotRequest passwordForgotRequest,
			HttpSession session) {
		// Validate OTP
		String sessionOtp = (String) session.getAttribute("otp");
		if (sessionOtp == null || !sessionOtp.equals(passwordForgotRequest.getOtp())) {
			throw new InvalidOtpException("Error: Please Enter a Valid OTP!");
		}

		// Find User by Email
		Optional<User> userOptional = userRepository.findByEmail(passwordForgotRequest.getEmail());
		if (userOptional.isEmpty()) {
			throw new NullValueException("Error: User Not Found!");
		}

		// Update User Password
		User user = userOptional.get();
		user.setPassword(encoder.encode(passwordForgotRequest.getNewPassword()));
		userRepository.save(user);
		if (EmailValidator.isEmailValid(user.getEmail()))
			emailSender.sendEmail(user.getEmail(), Constant.PASSWORD_FORGOT_SUBJECT, Constant.TEMPLATE_PATH,
					emailSender.createSourceMap(Constant.MESSAGE_FOR_FORGOT_PASSWORD, "username:- " + user.getUsername()
							+ "  password:- " + passwordForgotRequest.getNewPassword()));
		// Remove OTP from Session
		session.removeAttribute("otp");

		return ResponseEntity.ok(new MessageResponse("Password Reset Successfully!"));
	}

	@PostMapping("/send/otp")
	public ResponseEntity<?> sendOTP(@RequestParam String email, HttpSession session) {
		Optional<User> userOptional = userRepository.findByEmail(email);
		if (userOptional.isPresent()) {
			String generateOTP = OTPGenerator.generateOTP(6);
			session.setAttribute("otp", generateOTP);
			session.setMaxInactiveInterval(120);
			if (EmailValidator.isEmailValid(email))
				emailSender.sendEmail(email, Constant.OTP_SUBJECT, Constant.TEMPLATE_PATH,
						emailSender.createSourceMap(Constant.MESSAGE_FOR_OTP, generateOTP));

			return ResponseEntity.ok(new MessageResponse("OTP Sent Successfully!"));
		} else {
			throw new NullValueException("Error: User Not Found!");
		}
	}

	@GetMapping("/password/update")
	public ResponseEntity<?> updatePassword(@Valid @RequestBody PasswordUpdateRequest passwordUpdateRequest) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentUsername = authentication.getName();
		Optional<User> optionalUser = userRepository.findByUsername(currentUsername);

		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			String currentPassword = user.getPassword();

			if (encoder.matches(passwordUpdateRequest.getOldPassword(), currentPassword)) {
				// Valid old password, update the password
				user.setPassword(encoder.encode(passwordUpdateRequest.getNewPassword()));
				userRepository.save(user);
				if (EmailValidator.isEmailValid(user.getEmail()))
					emailSender.sendEmail(user.getEmail(), Constant.PASSWORD_UPDATE_SUBJECT, Constant.TEMPLATE_PATH,
							emailSender.createSourceMap(Constant.MESSAGE_FOR_PASSWORD_UPDATE, "username:- "
									+ user.getUsername() + "  password:- " + passwordUpdateRequest.getNewPassword()));
				return ResponseEntity.ok(new MessageResponse("Password Updated Successfully!"));
			} else {
				throw new InvalidPasswordException("Error: Invalid old password");
			}
		} else {
			throw new NullValueException("Error: User Not Found!");
		}
	}

}
