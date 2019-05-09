package com.upgrad.quora.api.controller;


import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.AuthorizationService;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.common.EndPointIdentifier;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
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
public class QuestionController implements EndPointIdentifier {

    // Implemented Endpoint Identifier interface for generic AuthorizationFailedException Handling

    @Autowired
    QuestionService questionService;

    @Autowired
    AuthorizationService authorizationService;



    /**
     * Method implements the question creation endpoint
     *
     * @param accessToken     accesstoken assigned to the user upon signin
     * @param questionRequest has all the details for creating a new question
     * @return ResponseEntity to indicate the question creation was successful or not and also returns uuid of question created
     * @throws AuthorizationFailedException
     */
    @PostMapping(path = "/question/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(@RequestHeader("authorization") String accessToken,
                                                           final QuestionRequest questionRequest) throws
            AuthorizationFailedException {

        final QuestionEntity questionEntity = new QuestionEntity();
        UserAuthTokenEntity userAuthTokenEntity = authorizationService.getUserAuthTokenEntity(accessToken,QUESTION_ENDPOINT);

        questionEntity.setUuid(UUID.randomUUID().toString());
        questionEntity.setUserId(userAuthTokenEntity.getUser());
        questionEntity.setContent(questionRequest.getContent());
        questionEntity.setDate(ZonedDateTime.now());

        final QuestionEntity createdQuestionEntity = questionService.createQuestion(questionEntity);
        QuestionResponse questionResponse = new QuestionResponse().id(createdQuestionEntity.getUuid())
                .status("QUESTION CREATED");

        return new ResponseEntity<>(questionResponse, HttpStatus.OK);
    }


    /**
     * Method implements the get all questions endpoint
     *
     * @param accessToken assigned to the user upon signin
     * @return ResponseEntity to indicate the status of the query as well as the list of questions
     * @throws AuthorizationFailedException
     */

    @GetMapping(path = "/question/all", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestions(@RequestHeader("authorization") String accessToken) throws AuthorizationFailedException {

        List<QuestionEntity> questionEntityList = questionService.getAllQuestions(accessToken);

        List<QuestionDetailsResponse> questionDetailsResponseList = new ArrayList<QuestionDetailsResponse>();
        if (!questionEntityList.isEmpty()) {

            for (QuestionEntity n : questionEntityList) {
                QuestionDetailsResponse questionDetailsResponse = new QuestionDetailsResponse();
                questionDetailsResponse.setId(n.getUuid());
                questionDetailsResponse.setContent(n.getContent());

                questionDetailsResponseList.add(questionDetailsResponse);
            }

        }

        return new ResponseEntity<>(questionDetailsResponseList, HttpStatus.OK);

    }

    /**
     * Method implements the edit question content endoint
     *
     * @param accessToken         assigned to the user upon signin
     * @param questionId          the uuid of the question to be edited
     * @param questionEditRequest provides the content to edit in the question
     * @return ResponseEntity  indicating the edit was a success or not along with the updated question uuid
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     */
    @PutMapping(path = "/question/edit/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionEditResponse> editQuestionContent(@RequestHeader("authorization") String accessToken, @PathVariable String questionId, QuestionEditRequest questionEditRequest)
            throws AuthorizationFailedException, InvalidQuestionException {


        QuestionEntity questionEntity = questionService.checkQuestion(accessToken, questionId);
        questionEntity.setContent(questionEditRequest.getContent());
        QuestionEntity updatedQuestionEntity = questionService.updateQuestion(questionEntity);

        QuestionEditResponse questionEditResponse = new QuestionEditResponse().id(updatedQuestionEntity.getUuid()).status("QUESTION EDITED");

        return new ResponseEntity<>(questionEditResponse, HttpStatus.OK);


    }

    /**
     * Method that implements question deletion endpoint
     *
     * @param accessToken accessToken assigned to the user upon signin
     * @param questionId  the uuid of the question to be deleted
     * @return ResponseEntity to indicated the deletion was successful or not along with the deleted question id
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     */

    @DeleteMapping(path = "/question/delete/{questionId}")
    public ResponseEntity<QuestionDeleteResponse> questionDelete(@RequestHeader("authorization") String accessToken,
                                                                 @PathVariable String questionId) throws

            AuthorizationFailedException, InvalidQuestionException {

        String id = questionService.deleteQuestion(questionId, accessToken);

        QuestionDeleteResponse questionDeleteResponse = new QuestionDeleteResponse().id(id)
                .status("QUESTION DELETED");

        return new ResponseEntity<>(questionDeleteResponse, HttpStatus.OK);
    }

    /**
     * Method implements the get all questions by user endpoint
     *
     * @param accessToken access token assigned to the user upon sigin
     * @param userId      uuid of the user
     * @return ResponseEntity has the status and the list of the questions
     * @throws AuthorizationFailedException
     * @throws UserNotFoundException
     */
    @GetMapping(path = "/question/all/{userId}")
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestionsByUser(@RequestHeader("authorization") String accessToken,
                                                                               @PathVariable String userId) throws AuthorizationFailedException, UserNotFoundException {

        List<QuestionEntity> questionEntityList = questionService.getAllQuestionsByUser(accessToken, userId);

        List<QuestionDetailsResponse> questionDetailsResponseList = new ArrayList<QuestionDetailsResponse>();
        for (QuestionEntity n : questionEntityList) {
            QuestionDetailsResponse questionDetailsResponse = new QuestionDetailsResponse();
            questionDetailsResponse.setId(n.getUuid());
            questionDetailsResponse.setContent(n.getContent());

            questionDetailsResponseList.add(questionDetailsResponse);
        }
        return new ResponseEntity<>(questionDetailsResponseList, HttpStatus.OK);


    }
}

