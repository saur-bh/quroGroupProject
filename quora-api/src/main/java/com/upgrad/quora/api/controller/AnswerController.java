package com.upgrad.quora.api.controller;


import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.AnswerService;
import com.upgrad.quora.service.business.AuthorizationService;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.common.EndPointIdentifier;
import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidAnswerException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
public class AnswerController implements EndPointIdentifier {

    // Implemented Endpoint Identifier interface for generic AuthorizationFailedException Handling
    @Autowired
    QuestionService questionService;

    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    AnswerService answerService;

    @Autowired
    QuestionDao questionDao;


    @PostMapping(path = "/question/{questionId}/answer/create", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse> createAnswer(@RequestHeader("authorization") String accessToken,@PathVariable String questionId,
                                                       final AnswerRequest answerRequest) throws
            AuthorizationFailedException, InvalidQuestionException {

        final AnswerEntity answerEntity = new AnswerEntity();
        UserAuthTokenEntity userAuthTokenEntity = authorizationService.getUserAuthTokenEntity(accessToken,ANSWER_ENDPOINT);
        answerEntity.setUser(userAuthTokenEntity.getUser());
        answerEntity.setAns(answerRequest.getAnswer());
        answerEntity.setDate(ZonedDateTime.now());


        final AnswerEntity createdAnswerEntity = answerService.createAnswer(answerEntity, questionId);
        AnswerResponse answerResponse = new AnswerResponse().id(createdAnswerEntity.getUuid())
                .status("ANSWER CREATED");

        return new ResponseEntity<AnswerResponse>(answerResponse, HttpStatus.OK);
    }

    @PutMapping(path = "/answer/edit/{answerId}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerEditResponse> editAnswerContent(AnswerEditRequest answerEditRequest, @RequestHeader("authorization") String accessToken, @PathVariable String answerId)
            throws AuthorizationFailedException, InvalidAnswerException {


        AnswerEntity answerEntity = answerService.checkAnswer(answerId, accessToken);
        answerEntity.setAns(answerEditRequest.getContent());
        AnswerEntity updatedAnswerEntity = answerService.updateAnswer(answerEntity);

        AnswerEditResponse answerEditResponse = new AnswerEditResponse().id(updatedAnswerEntity.getUuid()).status("ANSWER EDITED");

        return new ResponseEntity<AnswerEditResponse>(answerEditResponse, HttpStatus.OK);
    }

        @DeleteMapping(path = "/answer/delete/{answerId}")
        public ResponseEntity<AnswerDeleteResponse> answerDelete(@RequestHeader("authorization") String accessToken,
                @PathVariable String answerId) throws
                AuthorizationFailedException, InvalidAnswerException {

            String id = answerService.deleteAnswer(answerId,accessToken);

            AnswerDeleteResponse answerDeleteResponse = new AnswerDeleteResponse().id(id)
                    .status("ANSWER DELETED");

            return new ResponseEntity<AnswerDeleteResponse> (answerDeleteResponse, HttpStatus.OK);


        }

    @GetMapping(path = "/all/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<AnswerDetailsResponse>> getAllAnswersToQuestion(@RequestHeader("authorization") String accessToken, @PathVariable String questionId) throws AuthorizationFailedException, InvalidAnswerException, InvalidQuestionException {


        List<AnswerEntity> answerEntityList = answerService.getAllAnswersToQuestion(accessToken, questionId);


        QuestionEntity questionEntity = questionDao.getQuestionById(questionId);
        String questionContent = questionEntity.getContent();
        List<AnswerDetailsResponse> answerDetailsResponseList = new ArrayList<>();
        if (!answerEntityList.isEmpty()) {

            for (AnswerEntity n : answerEntityList) {
                AnswerDetailsResponse answerDetailsResponse = new AnswerDetailsResponse();
                answerDetailsResponse.setId(n.getUuid());
                answerDetailsResponse.setAnswerContent(n.getAns());
                answerDetailsResponse.setQuestionContent(questionContent);

                answerDetailsResponseList.add(answerDetailsResponse);
            }

        }

        return new ResponseEntity<>(answerDetailsResponseList, HttpStatus.OK);


    }
}