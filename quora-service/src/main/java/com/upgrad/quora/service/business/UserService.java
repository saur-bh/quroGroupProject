package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;


@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;

    /**
     * Method to create a new user.
     *
     * @param userEntity the UserEntity to be created
     * @return created UserEntity
     */

    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity createUser(final UserEntity userEntity) {

        // If password is null, we will give the user a default password
        String password = userEntity.getPassword();
        if (password == null) {

            userEntity.setPassword("quora@123");
        }

        // Generate salt and encrypt the password before creating the user
        String[] encryptPassword = passwordCryptographyProvider.encrypt(userEntity.getPassword());
        String salt = encryptPassword[0];
        userEntity.setSalt(salt);
        userEntity.setPassword(encryptPassword[1]);
        return userDao.createUser(userEntity);
    }


    /**
     * Method to get user by username.
     *
     * @param userName the username of the user we are trying to find
     * @return UserEntity of given user
     */

    public UserEntity getUserByUserName(String userName) {

        return userDao.findUserByUserName(userName);
    }

    /**
     * Method to get user by email.
     *
     * @param email email of the user we are trying to search for
     * @return UserEntity of user with given email
     */

    public UserEntity getUserByEmail(String email) {

        return userDao.findUserByEmail(email);
    }

    /**
     * Method to get user by UUID.
     *
     * @param uuid String containing UUID of the user we are looking for
     * @return UserEntity of the user with the given UUID
     * @throws UserNotFoundException in cases where there is no user in the DB with the given UUID
     */

    public UserEntity getUserByUUID(String uuid) throws UserNotFoundException {

        if (userDao.findUserByUUID(uuid) == null) {

            throw new UserNotFoundException("USR-001", "User with entered uuid does not exist");
        } else {

            return userDao.findUserByUUID(uuid);
        }
    }

    /**
     * Method to get userAuthTokenEntity by access token and set logout timestamp for signout endpoint.
     *
     * @param accessToken access token assigned to the user
     * @return UserAuthTokenEntity of user corresponding to the access token
     */

    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthTokenEntity getUserAuthTokenEntityByAccessToken(String accessToken)
            throws SignOutRestrictedException {

        UserAuthTokenEntity userAuthTokenEntity =
                userDao.findUserAuthTokenEntityByAccessToken(accessToken);

        if (userAuthTokenEntity == null) {

            throw new SignOutRestrictedException("SGR-001", "User is not Signed in");
        } else {

            final ZonedDateTime currentTime = ZonedDateTime.now();
            userAuthTokenEntity.setLogoutAt(currentTime);
            return userAuthTokenEntity;
        }
    }

}
