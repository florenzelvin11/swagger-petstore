package com.man.swagger_petstore.controller;

import com.man.swagger_petstore.api.spec.handler.UserApi;
import com.man.swagger_petstore.api.spec.model.User;
import com.man.swagger_petstore.exceptions.BusinessException;
import com.man.swagger_petstore.service.UserService;
import com.man.swagger_petstore.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Validated
@RestController
public class UserController implements UserApi {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Override
    public ResponseEntity<Void> createUser(User body) {
        LOG.info("Entering createUser() class UserController");

        validateUserBodyRequest(body);

        userService.createUser(body);

        LOG.info("Exiting createUser() class UserController");
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> createUsersWithArrayInput(List<User> body) {
        LOG.info("Entering createUsersWithArrayInput() class UserController");
        if (body == null) {
            LOG.warn("Invalid Input");
            throw new BusinessException(HttpStatus.METHOD_NOT_ALLOWED.value(),
                    "Invalid request",
                    "Please enter a valid Request body");
        }

        userService.createMultipleUsers(body);

        LOG.info("Exiting createUsersWithArrayInput() class UserController");
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteUser(String username) {
        LOG.info("Entering deleteUser() class UserController");
        userService.deleteUser(username);
        LOG.info("Exiting deleteUser() class UserController");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<User> getUserByName(String username) {
        LOG.info("Entering getUserByName() class UserController");

        if (username == null) {
            LOG.warn("Invalid username");
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    "Invalid username",
                    "Please enter a valid username");
        }

        User user = userService.getUserByUsername(username);

        LOG.info("Exiting getUserByName() class UserController");
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> loginUser(String username, String password) {
        LOG.info("Entering loginUser() class UserController");
        if (username == null
            || password == null) {
            LOG.warn("Invalid inputs");
            throw new BusinessException(HttpStatus.METHOD_NOT_ALLOWED.value(),
                    "Invalid username/password supplied",
                    "Please enter correct username/password");
        }

        String res = userService.userLogin(username, password);

        LOG.info("Exiting loginUser() class UserController");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> logoutUser() {
        return null;
    }

    @Override
    public ResponseEntity<Void> updateUser(String username, User body) {
        LOG.info("Entering updateUser() class UserController");
        if (username == null) {
            LOG.warn("Invalid inputs");
            throw new BusinessException(HttpStatus.METHOD_NOT_ALLOWED.value(), Constants.Error.INVALID_INPUT, "Request body should not be null");
        }
        validateUserBodyRequest(body);

        userService.updateUser(username, body);

        LOG.info("Exiting updateUser() class UserController");
        return null;
    }

    private void validateUserBodyRequest(User body) {
        if (body == null
                || body.getId() == null
                || body.getUsername() == null
                || body.getFirstName() == null
                || body.getLastName() == null
                || body.getEmail() == null
                || body.getPassword() == null
                || body.getPhone() == null
                || body.getUserStatus() == null
        ) {
            LOG.warn("Invalid inputs");
            throw new BusinessException(HttpStatus.METHOD_NOT_ALLOWED.value(), Constants.Error.INVALID_INPUT, "Request body should not be null");
        }
    }
}
