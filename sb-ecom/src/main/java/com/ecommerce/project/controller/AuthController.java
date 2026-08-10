package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.AuthenticationResult;
import com.ecommerce.project.security.request.LoginRequest;
import com.ecommerce.project.security.request.SignupRequest;
import com.ecommerce.project.security.response.MessageResponse;
import com.ecommerce.project.security.response.UserInfoResponse;
import com.ecommerce.project.security.services.UserDetailsImpl;
import com.ecommerce.project.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<UserInfoResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        AuthenticationResult result = authService.login(loginRequest);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, result.getResponseCookie().toString())
                .body(result.getUserInfoResponse());
    }

    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        MessageResponse response = authService.register(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/username")
    public String currentUserName(Authentication authentication) {
        if (authentication != null) {
            return authentication.getName();
        }
        return "";
    }

    @GetMapping("/user")
    public ResponseEntity<UserInfoResponse> getUserDetails(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        UserInfoResponse response = new UserInfoResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                roles,
                userDetails.getEmail()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/signout")
    public ResponseEntity<MessageResponse> signoutUser() {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authService.logout().toString())
                .body(new MessageResponse("You've been signed out!"));
    }

    @GetMapping("/sellers")
    public ResponseEntity<?> getAllSellers(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber) {

        Sort sortByAndOrder = Sort.by(AppConstants.SORT_USERS_BY).descending();
        Pageable pageDetails = PageRequest.of(pageNumber ,
                Integer.parseInt(AppConstants.PAGE_SIZE), sortByAndOrder);

        return ResponseEntity.ok(authService.getAllSellers(pageDetails));
    }

}
