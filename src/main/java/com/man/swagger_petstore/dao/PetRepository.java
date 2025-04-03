package com.man.swagger_petstore.dao;

import com.man.swagger_petstore.api.spec.model.Category;
import com.man.swagger_petstore.api.spec.model.Pet;
import com.man.swagger_petstore.api.spec.model.Tag;
import com.man.swagger_petstore.controller.PetController;
import com.man.swagger_petstore.exceptions.BusinessException;
import com.man.swagger_petstore.utils.Constants;
import com.sun.net.httpserver.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Objects;

@Repository
public class PetRepository extends JdbcDaoSupport {

    private static final Logger LOG = LoggerFactory.getLogger(PetRepository.class);

    @Autowired
    public PetRepository(JdbcTemplate jdbcTemplate) { setJdbcTemplate(jdbcTemplate); }

    public void insertPet(
            Long pet_id,
            String pet_name,
            Category category,
            List<String> photoUrls,
            List<Tag> tags,
            String petStatus
    ) {
        LOG.info("Entering insertPet() class PetRepository");
        try (Connection conn = Objects.requireNonNull(getJdbcTemplate().getDataSource()).getConnection()) {
            try (CallableStatement stmt = conn.prepareCall(Constants.SQL_Query.INSERT_PET)) {
                stmt.setLong(1, pet_id);
                stmt.setString(2, pet_name);
                stmt.setLong(3, category.getId());
                stmt.setString(4, category.getName());

                // Converts photoUrls to SQL Arrays
                Array photoUrlArray = conn.createArrayOf(Constants.SQL_Types.TEXT, photoUrls.toArray());
                stmt.setArray(5, photoUrlArray);

                // Converts tags to PostgrSQL array
                PetTagMapper petTagMapper = new PetTagMapper();
                Array tagNameArray = petTagMapper.mapPetTag(conn, tags);
                stmt.setArray(6, tagNameArray);

                stmt.setString(7, petStatus);

                boolean isResultSet = stmt.execute();

                if (!isResultSet) {
                    LOG.info("SQL inquiry unsuccessful");
                } else {
                    LOG.info("SQL inquiry successful");
                }
            }
        } catch (Exception e) {
            LOG.debug("Something went wrong in the sql query{}", e.getMessage());
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), Constants.Error.BAD_QUERY, e.getMessage());
        }
        LOG.info("Exiting insertPet() class PetRepository");
    }

    public static class PetTagMapper {
        public java.sql.Array mapPetTag(Connection conn, List<Tag> tags) throws SQLException {
            String[] pgFormattedTags = tags.stream()
                    .map(PetTagMapper::toCompositeFormat)
                    .toArray(String[]::new);

            return conn.createArrayOf(Constants.SQL_Types.TAG_NAME, pgFormattedTags);
        }

        public static String toCompositeFormat(Tag tag) {
            return "(" + (tag.getId() == null ? "" : tag.getId()) + "," + tag.getName() + ")";
        }
    }
}
