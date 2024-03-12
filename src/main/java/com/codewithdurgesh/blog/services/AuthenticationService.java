package com.codewithdurgesh.blog.services;

import com.codewithdurgesh.blog.config.AppConstants;
import com.codewithdurgesh.blog.entities.Role;
import com.codewithdurgesh.blog.entities.User;
import com.codewithdurgesh.blog.exceptions.ApiException;
import com.codewithdurgesh.blog.exceptions.ResourceNotFoundException;
import com.codewithdurgesh.blog.payloads.AuthenticationRequest;
import com.codewithdurgesh.blog.payloads.AuthenticationResponse;
import com.codewithdurgesh.blog.payloads.RegisterRequest;
import com.codewithdurgesh.blog.payloads.UserDto;
import com.codewithdurgesh.blog.repositories.RoleRepo;
import com.codewithdurgesh.blog.repositories.UserRepo;
import com.codewithdurgesh.blog.security.JwtService;
import com.codewithdurgesh.blog.token.Token;
import com.codewithdurgesh.blog.token.TokenRepository;
import com.codewithdurgesh.blog.token.TokenType;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    @Autowired
    private final UserRepo userRepo;

    @Autowired
    private final TokenRepository tokenRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final JwtService jwtService;

    @Autowired
    private final AuthenticationManager authenticationManager;

    @Autowired
    private final RoleRepo roleRepo;

    @Autowired
    private ModelMapper modelMapper;


    public UserDto register(RegisterRequest request) {

        Role role = this.roleRepo.findById(AppConstants.NORMAL_USER).get();
        HashSet<Role> set = new HashSet<>();
        set.add(role);

        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .about(request.getAbout())
                .roles(set)
                .build();
        userRepo.save(user);
        UserDto userDto = this.modelMapper.map(user, UserDto.class);
//        var jwtToken = jwtService.generateToken(user);
//        return AuthenticationResponse.builder()
//                .token(jwtToken)
//                .build();
        return userDto;
    }

    private void revokeAllUserTokens(User user){
        var validUserTokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if (validUserTokens.isEmpty()){
            return;
        }
        validUserTokens.forEach(t->{
            t.setExpired(true);
            t.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) throws Exception {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

        }
        catch (BadCredentialsException e){
            System.out.println("Inavalid Details");
            throw new ApiException("Invalid username or password !!");
        }

        var user = userRepo.findByEmail(request.getEmail()).orElseThrow(()->new ResourceNotFoundException("User", "email", 0));
        var jwtToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .revoked(false)
                .expired(false)
                .build();
        tokenRepository.save(token);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
