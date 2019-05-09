package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.UserDeleteResponse;
import com.upgrad.quora.service.business.AdminService;
import com.upgrad.quora.service.business.AuthorizationService;
import com.upgrad.quora.service.business.UserService;
import com.upgrad.quora.service.common.EndPointIdentifier;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;



@Controller
public class AdminController implements EndPointIdentifier {

    @Autowired
    UserService userService;

    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    AdminService adminService;

    /**
     * Method that implements the userDelete endpoint.
     *
     * @param accessToken String containing access token of user with admin role
     * @param userId String containing UUID of user to be deleted
     * @return ResponseEntity with UserDeleteResponse and HTTP Status
     * @throws AuthenticationFailedException in cases where the user has not signed in, has signed out or if the user is not an admin
     * @throws UserNotFoundException in cases where no user is found corresponding to the given UUID for deletion
     */

    @DeleteMapping(path = "/admin/user/{userId}")
    public ResponseEntity<UserDeleteResponse> userDelete(@RequestHeader("authorization") String accessToken,
                                                         @PathVariable String userId) throws
            AuthorizationFailedException, UserNotFoundException {

        UserAuthTokenEntity userAuthTokenEntity = authorizationService.getUserAuthTokenEntity(accessToken,EndPointIdentifier.ADMIN_ENDPOINT);
        UserEntity userEntity = userAuthTokenEntity.getUser();

        if (userEntity.getRole().equals("nonadmin")) {

            throw new AuthorizationFailedException("ATHR-003",
                    "Unauthorized Access, Entered user is not an admin");
        } else {

           String id = adminService.deleteUserByUUID(userId);
           UserDeleteResponse userDeleteResponse = new UserDeleteResponse().id(id)
                   .status("USER SUCCESSFULLY DELETED");

           return new ResponseEntity<UserDeleteResponse> (userDeleteResponse, HttpStatus.OK);
        }
    }

}
