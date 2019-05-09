package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.*;
import com.upgrad.quora.api.model.SignoutResponse;
import com.upgrad.quora.api.model.SignupUserRequest;
import com.upgrad.quora.api.model.SignupUserResponse;
import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.UserService;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;


import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.UUID;


@Controller
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    AuthenticationService authenticationService;


    /**
     * Method that implements the user signup endpoint.
     *
     * @param signupUserRequest to get user credentials
     * @return ResponseEntity to indicate whether sign up is successful or not
     * @throws SignUpRestrictedException in cases where username already exists, or email is already registered
     */

    @PostMapping(path = "/user/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupUserResponse> signUp(final SignupUserRequest signupUserRequest) throws SignUpRestrictedException {

        final UserEntity userEntity = new UserEntity();

        userEntity.setUuid(UUID.randomUUID().toString());
        userEntity.setRole("nonadmin");
        userEntity.setFirstName(signupUserRequest.getFirstName());
        userEntity.setLastName(signupUserRequest.getLastName());
        userEntity.setUserName(signupUserRequest.getUserName());
        userEntity.setEmailAddress(signupUserRequest.getEmailAddress());
        userEntity.setPassword(signupUserRequest.getPassword());
        userEntity.setCountry(signupUserRequest.getCountry());
        userEntity.setAboutMe(signupUserRequest.getAboutMe());
        userEntity.setDob(signupUserRequest.getDob());
        userEntity.setContactNumber(signupUserRequest.getContactNumber());


        String userNameExists = String.valueOf(userService.getUserByUserName(signupUserRequest.getUserName()));
        String emailExists = String.valueOf(userService.getUserByEmail(signupUserRequest.getEmailAddress()));


        // If username exists or user with given email exists, throw SignUpRestrictedException
        // Else, create user and send response
        if (!userNameExists.equals("null")) {

            throw new SignUpRestrictedException("SGR-001", "Try any other Username, this Username has already been taken");
        } else if (!emailExists.equals("null")) {

            throw new SignUpRestrictedException("SGR-002", "This user has already been registered, try with any other emailId");
        } else {

            final UserEntity createdUserEntity = userService.createUser(userEntity);
            SignupUserResponse userResponse = new SignupUserResponse()
                    .id(createdUserEntity.getUuid()).status("USER SUCCESSFULLY REGISTERED");

            return new ResponseEntity<SignupUserResponse>(userResponse, HttpStatus.CREATED);
        }
    }

    /**
     * Method that implements user signin endpoint.
     *
     * @param authorization String containing "Basic username:password" where "username:password" is Base64 encoded
     * @return ResponseEntity with SignInResponse, HTTPHeader, and HTTPStatus
     * @throws AuthenticationFailedException in cases where the password is wrong or user does not exist
     */

    @PostMapping(path = "/user/signin", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SigninResponse> signIn(@RequestHeader("authorization") final String authorization) throws
            AuthenticationFailedException{

        byte[] decodeAuth = Base64.getDecoder().decode(authorization.split("Basic ")[1]);
        String decodedAuth = new String(decodeAuth);
        String[] decodedAuthArray = decodedAuth.split(":");

        UserAuthTokenEntity userAuthToken = authenticationService.authenticate(decodedAuthArray[0],
                decodedAuthArray[1]);

        UserEntity userEntity = userAuthToken.getUser();

        SigninResponse signinResponse = new SigninResponse().id(userEntity.getUuid())
                .message("SIGNED IN SUCCESSFULLY");

        HttpHeaders headers = new HttpHeaders();
        headers.add("access_token", userAuthToken.getAccessToken());

        return new ResponseEntity<SigninResponse>(signinResponse, headers, HttpStatus.OK);
    }

    /**
     * Method that implements signout endpoint.
     *
     * @param accessToken String containing access token of the signed in  user
     * @return SignOutResponse with SignOutResponse, and HTTPStatus
     * @throws SignOutRestrictedException in cases where the access token cannot be found in the db and the user is not currently signed in
     */

    @PostMapping(path = "/user/signout", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignoutResponse> signOut(@RequestHeader("authorization") String accessToken)
            throws SignOutRestrictedException {

        UserAuthTokenEntity userAuthTokenEntity = userService.getUserAuthTokenEntityByAccessToken(accessToken);

        SignoutResponse signoutResponse = new SignoutResponse().id(userAuthTokenEntity.getUuid())
                .message("SIGNED OUT SUCCESSFULLY");

        return new ResponseEntity<SignoutResponse>(signoutResponse, HttpStatus.OK);
    }

}
