package com.upgrad.quora.service.business;

import com.upgrad.quora.service.common.EndPointIdentifier;
import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionService implements EndPointIdentifier {

    @Autowired
    UserDao userDao;

    @Autowired
    QuestionDao questionDao;

    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    QuestionValidityCheckService questionValidityCheckService;

    @Autowired
    UserAuthTokenValidifierService userAuthTokenValidifierService;

    /**
     * Method to create a new user.
     *
     * @param questionEntity the QuestionEntity to be created
     * @return created UserEntity
     */

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity createQuestion(final QuestionEntity questionEntity) {

        return questionDao.createQuestion(questionEntity);

    }

    /**
     * Method to get all the questions
     *
     * @param accessToken accessToken assigned to the user
     * @return List<QuestionEntity> list of all the questions associated with the user
     * @throws AuthorizationFailedException
     */


    @Transactional(propagation = Propagation.REQUIRED)
    public List<QuestionEntity> getAllQuestions(String accessToken) throws AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.findUserAuthTokenEntityByAccessToken(accessToken);

        List<QuestionEntity> questionEntityList = new ArrayList<>();

        if (userAuthTokenValidifierService.userAuthTokenValidityCheck(accessToken, GET_ALL_QUESTIONS)) {
            questionEntityList = questionDao.getAllQuestions();
        }
        return questionEntityList;


    }

    /**
     * @param accessToken accessToken assigned to the user
     * @param questionId  the uuid of the question
     * @return QuestionEntity
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     */

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity checkQuestion(String accessToken, String questionId) throws AuthorizationFailedException, InvalidQuestionException {

        UserAuthTokenEntity userAuthTokenEntity = userDao.findUserAuthTokenEntityByAccessToken(accessToken);
        QuestionEntity existingQuestionEntity = null;
        if (userAuthTokenValidifierService.userAuthTokenValidityCheck(accessToken, CHECK_QUESTION)) {
            UserEntity user = userAuthTokenEntity.getUser();

            existingQuestionEntity = questionValidityCheckService.checkQuestionIsValid(questionId);

            if (!user.equals(existingQuestionEntity.getUser())) {
                throw new AuthorizationFailedException("ATHR-003", "Only the question owner can edit the question");
            } else {
                return existingQuestionEntity;
            }

        }
        return existingQuestionEntity;
    }


    /**
     * Method to update a question
     *
     * @param questionEntity to be updated
     * @return updated questionEntity
     */

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity updateQuestion(QuestionEntity questionEntity) {
        return questionDao.updateQuestion(questionEntity);
    }

    /**
     * Method to delete a given question
     *
     * @param questionId  uuid of the question to be deleted
     * @param accessToken accessId assigned to the user
     * @return uuid of the deleted question
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     */

    @Transactional(propagation = Propagation.REQUIRED)
    public String deleteQuestion(String questionId, String accessToken) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.findUserAuthTokenEntityByAccessToken(accessToken);

        String deletedQuestionid = null;
        if (userAuthTokenValidifierService.userAuthTokenValidityCheck(accessToken, DELETE_QUESTION)) {
            UserEntity user = userAuthTokenEntity.getUser();

            QuestionEntity existingQuestionEntity = questionValidityCheckService.checkQuestionIsValid(questionId);

            if ((!user.equals(existingQuestionEntity.getUser()) || (!user.getRole().equals("admin")))) {
                throw new AuthorizationFailedException("ATHR-003", "Only the question owner or admin can delete the question");
            } else {
                questionDao.deleteUserByUUID(questionId);
                deletedQuestionid = questionId;
                return (deletedQuestionid);
            }
        }
        return deletedQuestionid;
    }

    /**
     * Method to get all the questions by a given user
     *
     * @param accessToken accessToken assigned to the user
     * @param userId      uuid of the user
     * @return List<QuestionEntity> list of all the questions by the corresponding user
     * @throws AuthorizationFailedException
     * @throws UserNotFoundException
     */

    @Transactional(propagation = Propagation.REQUIRED)
    public List<QuestionEntity> getAllQuestionsByUser(String accessToken, String userId) throws AuthorizationFailedException, UserNotFoundException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.findUserAuthTokenEntityByAccessToken(accessToken);


        List<QuestionEntity> questionEntityList = new ArrayList<>();
        if (userDao.findUserByUUID(userId) == null) {

            throw new UserNotFoundException("USR-001", "User with entered uuid whose question details are to be seen does not exist");

        } else if (userAuthTokenValidifierService.userAuthTokenValidityCheck(accessToken, GET_QUESTION_BY_USER)) {

            UserEntity userEntity = userAuthTokenEntity.getUser();

            questionEntityList = questionDao.getQuestionByUser(userEntity);

            return questionEntityList;
        }
        return questionEntityList;
    }
}
