package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;


@Service
public class AuthenticationService {

    @Autowired
    private UserDao userDao;

    /**
     * Method to authenticate user credentials.
     *
     * @param userName username to be used in authentication
     * @param password password to be used in authentication
     * @return userAuthTokenEntity with the created auth token assigned to the user
     * @throws AuthenticationFailedException in cases where the username doesn't exist, or the password is incorrect
     */

    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthTokenEntity authenticate(final String userName, final String password) throws AuthenticationFailedException {

        UserEntity userEntity = userDao.findUserByUserName(userName);

        if (userEntity == null) {
            throw new AuthenticationFailedException("ATH-001", "This username does not exist");
        }

        final String encryptedPassword = PasswordCryptographyProvider
                .encrypt(password, userEntity.getSalt());

        if (encryptedPassword.equals(userEntity.getPassword())) {

            JwtTokenProvider tokenProvider = new JwtTokenProvider(encryptedPassword);

            UserAuthTokenEntity userAuthTokenEntity = new UserAuthTokenEntity();
            userAuthTokenEntity.setUser(userEntity);
            userAuthTokenEntity.setUuid(userEntity.getUuid());

            final ZonedDateTime currentTime = ZonedDateTime.now();
            final ZonedDateTime expiryTime = currentTime.plusHours(8);

            userAuthTokenEntity.setAccessToken(tokenProvider.generateToken(userEntity.getUuid(), currentTime, expiryTime));
            userAuthTokenEntity.setLoginAt(currentTime);
            userAuthTokenEntity.setExpiresAt(expiryTime);

            userDao.createAuthToken(userAuthTokenEntity);
            return userAuthTokenEntity;
        } else {

            throw new AuthenticationFailedException("ATH-002", "Password failed");
        }
    }
}
