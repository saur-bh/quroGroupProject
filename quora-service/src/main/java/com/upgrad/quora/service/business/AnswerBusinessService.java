package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.AnswerDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnswerBusinessService {

    @Autowired
    private AnswerDao answerDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity createAnswer(AnswerEntity answerEntity , UserAuthEntity userAuthEntity) throws AuthorizationFailedException {

        if (userAuthEntity.getLogoutAt()!=null){
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to post an answer");
        }
        return answerDao.createAnswer(answerEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity getAnswerFromId(String uuid) throws AnswerNotFoundException {
        AnswerEntity answerEntity = answerDao.getAnswerById(uuid);
        if (answerEntity == null)
        {
            throw new AnswerNotFoundException("ANS-001","Entered answer uuid does not exist");
        }
        return answerEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity checkAnswerBelongToUser(UserAuthEntity userAuthEntity, AnswerEntity answerEntity) throws AuthorizationFailedException {

        UserEntity userEntity = userAuthEntity.getUser();

        if(userAuthEntity.getLogoutAt()!=null){
            throw new AuthorizationFailedException("ATHR-002" ,"User is signed out.Sign in first to edit an answer");
        }
        String auuid = answerEntity.getUuid();
        String uuuid = userEntity.getUuid();
        AnswerEntity checkedAnswer = answerDao.checkAnswerBelongToUser(auuid,uuuid);
        if(checkedAnswer==null)
        {
            throw new AuthorizationFailedException("ATHR-003","Only the answer owner can edit or delete the answer");
        }
        return checkedAnswer;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity updateAnswer(AnswerEntity answerEntity)
    {
        AnswerEntity updateAnswer = answerDao.updateAnswer(answerEntity);
        return updateAnswer;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity deleteAnswer(AnswerEntity answerEntity)
    {
        AnswerEntity deletedAnswer = answerDao.deleteAnswer(answerEntity);
        return deletedAnswer;
    }

    public List<AnswerEntity> getAllAnswers(String questionId , UserAuthEntity userAuthEntity) throws AuthorizationFailedException
    {
        if (userAuthEntity.getLogoutAt()!= null){
            throw new AuthorizationFailedException("ATHR-002" ,"User is signed out.Sign in first to edit an answer");

        }
        return answerDao.getAllAnswers(questionId);
    }
}
