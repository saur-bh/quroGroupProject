package com.upgrad.quora.service.business;

import com.upgrad.quora.service.common.EndPointIdentifier;
import com.upgrad.quora.service.dao.AnswerDao;
import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidAnswerException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
public class AnswerService implements EndPointIdentifier {

    @Autowired
    UserDao userDao;

    @Autowired
    QuestionDao questionDao;

    @Autowired
    AnswerDao answerDao;

    @Autowired
    QuestionValidityCheckService questionValidityCheckService;

    @Autowired
    UserAuthTokenValidifierService userAuthTokenValidifierService;

    /**
     * Method to create a new user.
     *
     * @param answerEntity the AnswerEntity to be created
     * @return created UserEntity
     */

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity createAnswer(final AnswerEntity answerEntity, String questionId) throws InvalidQuestionException {

        if (questionValidityCheckService.checkQuestionIsValid(questionId) != null)
            return answerDao.createAnswer(answerEntity);
        else
            throw new InvalidQuestionException("QUES-001", "The question entered is invalid");

    }


    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity checkAnswer(String answerId, String accessToken) throws AuthorizationFailedException, InvalidAnswerException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.findUserAuthTokenEntityByAccessToken(accessToken);

        AnswerEntity existingAnswerEntity = null;

        if (userAuthTokenValidifierService.userAuthTokenValidityCheck(accessToken, CHECK_ANSWER)) {

            String user_id = userAuthTokenEntity.getUser().getUuid();


            AnswerEntity answerEntity = answerDao.getAnswerById(answerId);

            if (existingAnswerEntity == null) {
                throw new InvalidAnswerException("ANS-001", "Entered answer uuid does not exist");
            } else if (!user_id.equals(existingAnswerEntity.getUuid())) {
                throw new AuthorizationFailedException("ATHR-003", "Only the answer owner can edit the answer");
            } else {
                existingAnswerEntity = answerEntity;
            }
        }
        return existingAnswerEntity;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity updateAnswer(AnswerEntity answerEntity) {
        return answerDao.updateAnswer(answerEntity);
    }

    /**
     * @param answerId
     * @param accessToken
     * @return
     * @throws AuthorizationFailedException
     * @throws InvalidAnswerException
     */

    @Transactional(propagation = Propagation.REQUIRED)
    public String deleteAnswer(String answerId, String accessToken) throws AuthorizationFailedException, InvalidAnswerException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.findUserAuthTokenEntityByAccessToken(accessToken);
        String deletedAnswerId = null;

        if (userAuthTokenValidifierService.userAuthTokenValidityCheck(accessToken, DELETE_ANSWER)) {

            String user_id = userAuthTokenEntity.getUser().getUuid();


            AnswerEntity existingAnswerEntity = answerDao.getAnswerById(answerId);

            if (existingAnswerEntity == null) {
                throw new InvalidAnswerException("ANS-001", "Entered answer uuid does not exist");
            } else if ((!user_id.equals(existingAnswerEntity.getUuid())) || (!userAuthTokenEntity.getUser().getRole().equals("admin"))) {
                throw new AuthorizationFailedException("ATHR-003", "Only the answer owner or admin can delete the answer");
            } else {
                answerDao.deleteAnswerByUUID(answerId);
                deletedAnswerId = answerId;
            }
        }

        return deletedAnswerId;

    }

    /**
     * Method to get all answers to the question
     *
     * @param accessToken access token assigned to user upon signup
     * @param questionId  the uuid of the question
     * @return
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     */

    public List<AnswerEntity> getAllAnswersToQuestion(String accessToken, String questionId) throws AuthorizationFailedException, InvalidQuestionException {

        List<AnswerEntity> answerEntityList = new ArrayList<>();

        if (userAuthTokenValidifierService.userAuthTokenValidityCheck(accessToken, GET_ALL_ANSWERS)) {

            QuestionEntity questionEntity = questionValidityCheckService.checkQuestionIsValid(questionId);

            if (questionEntity == null) {
                throw new InvalidQuestionException("QUES-001", "The question with entered uuid whose details are to be seen does not exist");
            } else {
                answerEntityList = answerDao.getAllAnswersToQuestion(questionEntity);
            }
        }
        return answerEntityList;

    }
}
