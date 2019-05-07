package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.AnswerEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class AnswerDao {

    @PersistenceContext
    private EntityManager entityManager;

    public AnswerEntity createAnswer(AnswerEntity answerEntity)
    {
        entityManager.persist(answerEntity);
        return answerEntity;
    }

    public AnswerEntity getAnswerById(String uuid)
    {
        try {
             return entityManager.createNamedQuery("getAnswerFromId", AnswerEntity.class).setParameter("uuid", uuid)
                    .getSingleResult();
        }catch (NoResultException nre)
        {
            return null;
        }
    }

    public AnswerEntity checkAnswerBelongToUser(String auuid, String uuuid)
    {

        try {
            return entityManager.createNamedQuery("checkAnswerBelongToUser", AnswerEntity.class).setParameter("auuid", auuid).setParameter("uuuid",uuuid).getSingleResult();
        }catch (NoResultException nre)
        {
            return null;
        }
    }

    public AnswerEntity updateAnswer(AnswerEntity answerEntity)
    {
        return entityManager.merge(answerEntity);
    }

    public AnswerEntity deleteAnswer(AnswerEntity answerEntity)
    {
        entityManager.remove(answerEntity);
        return answerEntity;
    }

    public List<AnswerEntity> getAllAnswers(String questionId)
    {
        return entityManager.createNamedQuery("getAllAnswers",AnswerEntity.class)
                .setParameter("uuid",questionId).getResultList();
    }
}
