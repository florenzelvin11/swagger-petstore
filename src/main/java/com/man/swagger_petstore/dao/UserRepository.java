package com.man.swagger_petstore.dao;

import com.man.swagger_petstore.api.spec.model.User;
import com.man.swagger_petstore.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.Objects;

@Repository
public class UserRepository {

    private final Logger LOG = LoggerFactory.getLogger(UserRepository.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void insertUser(
            Long userId,
            String username,
            String firstname,
            String lastname,
            String email,
            String password,
            String phone,
            Integer userStatus
    ) throws SQLException {
        LOG.info("Entering insertUser() class UserRepository");
        try (Connection conn = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection();
             CallableStatement stmt = conn.prepareCall(Constants.SQL_Query.INSERT_USER)) {
            // IN
            stmt.setLong(Constants.SQL_Query.FIRST_PARAM, userId);
            stmt.setString(Constants.SQL_Query.SECOND_PARAM, username);
            stmt.setString(Constants.SQL_Query.THIRD_PARAM, firstname);
            stmt.setString(Constants.SQL_Query.FOURTH_PARAM, lastname);
            stmt.setString(Constants.SQL_Query.FIFTH_PARAM, email);
            stmt.setString(Constants.SQL_Query.SIXTH_PARAM, password);
            stmt.setString(Constants.SQL_Query.SEVENTH_PARAM, phone);
            stmt.setInt(Constants.SQL_Query.EIGHTH_PARAM, userStatus);

            stmt.execute();
        }
        LOG.info("Exiting insertUser() class UserRepository");
    }

    public User getUserByUsername(String username) throws SQLException {
        LOG.info("Entering getUserByUsername() class UserRepository");
        try (Connection conn = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            conn.setAutoCommit(false);
            try (CallableStatement stmt = conn.prepareCall(Constants.SQL_Query.GET_USER_BY_USERNAME)) {
                // IN
                stmt.setString(Constants.SQL_Query.FIRST_PARAM, username);

                // OUT
                stmt.registerOutParameter(Constants.SQL_Query.SECOND_PARAM, Types.REF_CURSOR);

                stmt.execute();

                try (ResultSet rs = (ResultSet) stmt.getObject(Constants.SQL_Query.SECOND_PARAM)) {
                    if (rs.next()) {
                        UserMapper userMapper = new UserMapper();
                        return userMapper.mapRow(rs,1);
                    }
                }
            }
            conn.commit();
        }
        LOG.info("Exiting getUserByUsername() class UserRepository");
        return null;
    }

    public static class UserMapper implements RowMapper<User> {

        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("user_id"));
            user.setUsername(rs.getString("user_username"));
            user.setFirstName(rs.getString("user_firstname"));
            user.setLastName(rs.getString("user_lastname"));
            user.setEmail(rs.getString("user_email"));
            user.setPassword(rs.getString("user_password"));
            user.setPhone(rs.getString("user_phone"));
            user.setUserStatus(rs.getInt("user_status"));
            return user;
        }
    }
}
