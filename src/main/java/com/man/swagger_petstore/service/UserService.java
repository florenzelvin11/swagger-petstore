package com.man.swagger_petstore.service;

import com.man.swagger_petstore.api.spec.model.User;
import com.man.swagger_petstore.dao.UserRepository;
import com.man.swagger_petstore.exceptions.BusinessException;
import com.man.swagger_petstore.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    public void createUser(User user) {
        LOG.info("Entering createUser() class UserService");

        try {
            userRepository.insertUser(
                    user.getId(),
                    user.getUsername(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getPhone(),
                    user.getUserStatus()
            );
        } catch (SQLException e) {
            if (Constants.SQL_Error.UNIQUE_VIOLATION.equals(e.getSQLState())) {
                LOG.warn("Duplicate key violation: {}", e.getMessage());
                throw new BusinessException(HttpStatus.CONFLICT.value(),
                        Constants.Error.DUPLICATE_ID,
                        e.getMessage()
                );
            } else {
                LOG.warn("Something went wrong with insertUser call");
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                        Constants.Error.BAD_QUERY,
                        e.getMessage());
            }
        }

        LOG.info("Exiting createUser() class UserService");
    }

    public void createMultipleUsers(List<User> users) {
        LOG.info("Entering createMultipleUsers() class UserService");

        users.forEach(this::createUser);

        LOG.info("Exiting createMultipleUsers() class UserService");
    }

    public User getUserByUsername(String username) {
        LOG.info("Entering getUserByUsername() class UserService");

        User user = null;
        try {
            user = userRepository.getUserByUsername(username);
            if (user == null) {
                throw new BusinessException(HttpStatus.NO_CONTENT.value(),
                        "Invalid username",
                        "No user found with username");
            }
        } catch (Exception e) {
            LOG.warn("Something went wrong with getUserByUsername call");
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    Constants.Error.BAD_QUERY,
                    e.getMessage());
        }

        LOG.info("Exiting getUserByUsername() class UserService");
        return user;
    }

    public String userLogin(String username, String password) {
        LOG.info("Entering userLogin() class UserService");

        User user = getUserByUsername(username);
        if (user == null
            || !user.getPassword().equals(password)) {
            LOG.warn("Invalid Username");
            throw new BusinessException(HttpStatus.METHOD_NOT_ALLOWED.value(),
                    Constants.Error.INVALID_USER,
                    "Please enter the correct Username or Password");
        }

        LOG.info("Exiting userLogin() class UserService");
        return user.getUsername();
    }

    public void updateUser(String username, User body) {
        LOG.info("Entering updateUser() class UserService");

        User user = getUserByUsername(username);
        if (user == null) {
            LOG.warn("Invalid Username");
            throw new BusinessException(HttpStatus.METHOD_NOT_ALLOWED.value(),
                    Constants.Error.INVALID_INPUT,
                    "Please enter the valid Username");
        }

        try {
            userRepository.updateUser(
                    body.getId(),
                    body.getUsername(),
                    body.getFirstName(),
                    body.getLastName(),
                    body.getEmail(),
                    body.getPassword(),
                    body.getPhone(),
                    body.getUserStatus()
            );
        } catch (SQLException e) {
            LOG.warn("Something went wrong with updateUser call");
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    Constants.Error.BAD_QUERY,
                    e.getMessage());
        }

        LOG.info("Exiting updateUser() class UserService");
    }

    public void deleteUser(String username) {
        LOG.info("Entering deleteUser() class UserService");
        User user = getUserByUsername(username);
        if (user == null) {
            LOG.warn("Invalid Username");
            throw new BusinessException(HttpStatus.METHOD_NOT_ALLOWED.value(),
                    Constants.Error.INVALID_INPUT,
                    "Please enter the valid Username");
        }

        try {
            userRepository.deleteUser(username);
        } catch (SQLException e) {
            LOG.warn("Something went wrong with updateUser call");
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    Constants.Error.BAD_QUERY,
                    e.getMessage());
        }

        LOG.info("Exiting deleteUser() class UserService");
    }
}
