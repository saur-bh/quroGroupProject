package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AdminService {

    @Autowired
    UserDao userDao;

    @Autowired
    UserService userService;

    /**
     * Method to deletes a user by UUID
     *
     * @param uuid UUID of the user to be deleted
     * @return String containing uuid of the deleted pulled from users table in the DB.
     * @throws UserNotFoundException in cases where no user is found corresponding to the given UUID.
     */

    @Transactional(propagation = Propagation.REQUIRED)
    public String deleteUserByUUID(String uuid) throws UserNotFoundException {

        UserEntity userEntity = userDao.findUserByUUID(uuid);

        if (userEntity == null) {

            throw new UserNotFoundException("USR-001",
                    "User with entered uuid to be deleted does not exist");
        } else {

            String id = userEntity.getUuid();
            userDao.deleteUserByUUID(uuid);
            return id;
        }
    }
}
