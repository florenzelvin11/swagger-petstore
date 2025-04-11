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

    public User getUserByUsername(String username) {
        LOG.info("Entering getUserByUsername() class UserService");

        User user = null;
        try {
            user = userRepository.getUserByUsername(username);
        } catch (Exception e) {
            LOG.warn("Something went wrong with getUserByUsername call");
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    Constants.Error.BAD_QUERY,
                    e.getMessage());
        }

        LOG.info("Exiting getUserByUsername() class UserService");
        return user;
    }
}
