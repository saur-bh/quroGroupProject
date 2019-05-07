package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.AnswerBusinessService;
import com.upgrad.quora.service.business.QuestionBusinessService;
import com.upgrad.quora.service.business.UserAuthBusinessService;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class AnswerController {

    @Autowired
    private AnswerBusinessService answerBusinessService;

    @Autowired
    private UserAuthBusinessService userAuthBusinessService;

    @Autowired
    private QuestionBusinessService questionBusinessService;

    @RequestMapping(method = RequestMethod.POST, path = "/question/{questionId}/answer/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse> createAnswer(@RequestHeader("authorization") final String authorization, @PathVariable("questionId") final String questionId, final AnswerRequest answerRequest)
            throws AuthorizationFailedException, InvalidQuestionException {

        final UserAuthEntity userAuthEntity = userAuthBusinessService.getUser(authorization);
        QuestionEntity questionEntity = questionBusinessService.validateQuestion(questionId);

        UserEntity userEntity = userAuthEntity.getUser();

        AnswerEntity answerEntity = new AnswerEntity();
        answerEntity.setUuid(UUID.randomUUID().toString());
        answerEntity.setDate(ZonedDateTime.now());
        answerEntity.setQuestion(questionEntity);
        answerEntity.setAnswer(answerRequest.getAnswer());
        answerEntity.setUser(userEntity);

        AnswerEntity createdAnswer = answerBusinessService.createAnswer(answerEntity, userAuthEntity);
        AnswerResponse answerResponse = new AnswerResponse().id(createdAnswer.getUuid()).
                    status("ANSWER CREATED");

        return new ResponseEntity<AnswerResponse>(answerResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/answer/edit/{answerId}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerEditResponse> editAnswerContent(final AnswerEditRequest answerEditRequest, @PathVariable("answerId") final String answerId,
                                                                @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, AnswerNotFoundException {

        UserAuthEntity userAuthEntity = userAuthBusinessService.getUser(authorization);

        AnswerEntity answerEntity = answerBusinessService.getAnswerFromId(answerId);
        AnswerEntity checkedAnswer = answerBusinessService.checkAnswerBelongToUser(userAuthEntity,answerEntity);

        checkedAnswer.setAnswer(answerEditRequest.getContent());
        AnswerEntity updatedAnswer = answerBusinessService.updateAnswer(checkedAnswer);

        AnswerEditResponse answerEditResponse = new AnswerEditResponse().id(updatedAnswer.getUuid()).status("ANSWER EDITED");


        return new ResponseEntity<AnswerEditResponse>(answerEditResponse,HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/answer/delete/{answerId}",  produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerDeleteResponse> deleteAnswer(@PathVariable("answerId") final String answerId, @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, AnswerNotFoundException {

        UserAuthEntity userAuthEntity = userAuthBusinessService.getUser(authorization);
        UserEntity userEntity= userAuthEntity.getUser();
        AnswerEntity answerEntity = answerBusinessService.getAnswerFromId(answerId);
        AnswerEntity checkedAnswer;

        if(userEntity.getRole().equalsIgnoreCase("admin")) {
            checkedAnswer = answerEntity;
        }
        else {
            checkedAnswer = answerBusinessService.checkAnswerBelongToUser(userAuthEntity, answerEntity);
        }
        AnswerEntity deletedAnswer = answerBusinessService.deleteAnswer(checkedAnswer);

        AnswerDeleteResponse answerDeleteResponse = new AnswerDeleteResponse().id(deletedAnswer.getUuid())
                .status("ANSWER DELETED");

        return new ResponseEntity<AnswerDeleteResponse>(answerDeleteResponse,HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "answer/all/{questionId}",  produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<AnswerDetailsResponse>> getAllAnswersToQuestion(@PathVariable("questionId") final String questionId, @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, InvalidQuestionException {

        UserAuthEntity userAuthEntity = userAuthBusinessService.getUser(authorization);
        QuestionEntity questionEntity = questionBusinessService.validateQuestion(questionId);


        ArrayList<AnswerDetailsResponse> list = null;
        ArrayList<AnswerEntity> rawList = (ArrayList) answerBusinessService.getAllAnswers(questionId , userAuthEntity);

        for(AnswerEntity answer : rawList)
        {
            AnswerDetailsResponse detailsResponse = new AnswerDetailsResponse();
            detailsResponse.setId(answer.getUuid());
            detailsResponse.setAnswerContent(answer.getAnswer());
            detailsResponse.setQuestionContent(questionEntity.getContent());
            list.add(detailsResponse);
        }

        return new ResponseEntity<List<AnswerDetailsResponse>>(list, HttpStatus.OK);

    }

}
