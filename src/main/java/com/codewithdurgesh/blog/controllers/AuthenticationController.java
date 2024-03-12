package com.codewithdurgesh.blog.controllers;

import com.codewithdurgesh.blog.payloads.UserDto;
import com.codewithdurgesh.blog.services.AuthenticationService;
import com.codewithdurgesh.blog.payloads.AuthenticationRequest;
import com.codewithdurgesh.blog.payloads.AuthenticationResponse;
import com.codewithdurgesh.blog.payloads.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(
            @RequestBody RegisterRequest request
    ){

        return new ResponseEntity<UserDto>(authenticationService.register(request), HttpStatus.CREATED);
//        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate (
            @RequestBody AuthenticationRequest request
    ) throws Exception {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }


}
