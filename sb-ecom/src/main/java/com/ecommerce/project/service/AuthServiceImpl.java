package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AuthenticationResult;
import com.ecommerce.project.payload.UserDTO;
import com.ecommerce.project.payload.UserResponse;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.request.LoginRequest;
import com.ecommerce.project.security.request.SignupRequest;
import com.ecommerce.project.security.response.MessageResponse;
import com.ecommerce.project.security.response.UserInfoResponse;
import com.ecommerce.project.security.services.UserDetailsImpl;
import com.ecommerce.project.repositories.RoleRepository;
import com.ecommerce.project.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {
@Autowired
ModelMapper modelMapper;
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public AuthenticationResult login(LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
        } catch (AuthenticationException exception) {
            throw new BadCredentialsException("Invalid username or password");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtUtils.generateTokenFromUsername(userDetails);
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(jwt);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toList());

        UserInfoResponse response = new UserInfoResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                roles,
                jwt,
                jwtCookie.toString(),
                userDetails.getEmail()
        );

        return new AuthenticationResult(response, jwtCookie);
    }

    @Override
    public MessageResponse register(SignupRequest signUpRequest) {
        if (userRepository.existsByUserName(signUpRequest.getUsername())) {
            throw new APIException("Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new APIException("Email is already in use!");
        }

        User user = new User(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                passwordEncoder.encode(signUpRequest.getPassword())
        );

        Set<Role> roles = resolveRoles(signUpRequest.getRole());
        user.setRoles(roles);
        userRepository.save(user);

        return new MessageResponse("User registered successfully!");
    }

    @Override
    public ResponseCookie logout() {
        return jwtUtils.getCleanJwtCookie();
    }

    private Set<Role> resolveRoles(Set<String> requestedRoles) {
        Set<Role> roles = new HashSet<>();

        if (requestedRoles == null || requestedRoles.isEmpty()) {
            roles.add(getRole(AppRole.ROLE_USER));
            return roles;
        }

        for (String requestedRole : requestedRoles) {
            if (requestedRole == null) {
                continue;
            }

            switch (requestedRole.toLowerCase(Locale.ROOT)) {
                case "admin" -> roles.add(getRole(AppRole.ROLE_ADMIN));
                case "seller" -> roles.add(getRole(AppRole.ROLE_SELLER));
                default -> roles.add(getRole(AppRole.ROLE_USER));
            }
        }

        if (roles.isEmpty()) {
            roles.add(getRole(AppRole.ROLE_USER));
        }

        return roles;
    }

    private Role getRole(AppRole roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new APIException("Error: Role is not found."));
    }
    @Override
    public UserResponse getAllSellers(Pageable pageable){
        Page<User> allUser = userRepository.findByRoleName(AppRole.ROLE_SELLER,pageable);
            List<UserDTO> userDTOS = allUser.getContent().stream().map(p->modelMapper.map(p,UserDTO.class)).collect(Collectors.toList());
UserResponse response = new UserResponse() ;
response .setUsers(userDTOS); ;
response.setPageNumber(allUser.getNumber());
response.setPageSize(allUser.getSize());
response.setTotalElements(allUser.getTotalElements());
response.setTotalPages((long) allUser.getTotalPages());
response.setLastPage(allUser.isLast());
return response;
    }
}
