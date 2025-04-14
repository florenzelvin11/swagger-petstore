package com.man.swagger_petstore.dao;

import com.man.swagger_petstore.api.spec.model.Category;
import com.man.swagger_petstore.api.spec.model.Pet;
import com.man.swagger_petstore.api.spec.model.Tag;
import com.man.swagger_petstore.exceptions.BusinessException;
import com.man.swagger_petstore.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;

@Repository
public class PetRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PetRepository.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void insertPet(
            Long pet_id,
            String pet_name,
            Category category,
            List<String> photoUrls,
            List<Tag> tags,
            String petStatus
    ) {
        LOG.info("Entering insertPet() class PetRepository");
        try (Connection conn = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            try (CallableStatement stmt = conn.prepareCall(Constants.SQL_Query.INSERT_PET)) {
                stmt.setLong(Constants.SQL_Query.FIRST_PARAM, pet_id);
                stmt.setString(Constants.SQL_Query.SECOND_PARAM, pet_name);
                stmt.setLong(Constants.SQL_Query.THIRD_PARAM, category.getId());
                stmt.setString(Constants.SQL_Query.FOURTH_PARAM, category.getName());

                // Converts photoUrls to SQL Arrays
                Array photoUrlArray = conn.createArrayOf(Constants.SQL_Types.TEXT, photoUrls.toArray());
                stmt.setArray(Constants.SQL_Query.FIFTH_PARAM, photoUrlArray);

                // Converts tags to PostgreSQL array
                PetTagMapper petTagMapper = new PetTagMapper();
                Array tagNameArray = petTagMapper.mapPetTagToSql(conn, tags);
                stmt.setArray(Constants.SQL_Query.SIXTH_PARAM, tagNameArray);

                stmt.setString(Constants.SQL_Query.SEVENTH_PARAM, petStatus);

                boolean isSuccessfulQuery = stmt.execute();

                if (isSuccessfulQuery) {
                    LOG.info("SQL inquiry no return successful");
                }
            }
        } catch (SQLException e) {
            if (Constants.SQL_Error.UNIQUE_VIOLATION.equals(e.getSQLState())) {
                LOG.warn("Duplicate key violation: {}", e.getMessage());
                throw new BusinessException(HttpStatus.CONFLICT.value(),
                        Constants.Error.DUPLICATE_ID,
                        e.getMessage()
                );
            }
        } catch (Exception e) {
            LOG.warn("Something went wrong in the sql query{}", e.getMessage());
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), Constants.Error.BAD_QUERY, e.getMessage());
        }
        LOG.info("Exiting insertPet() class PetRepository");
    }

    public void updatePet(
            Long petId,
            String pet_name,
            Category category,
            List<String> photoUrls,
            List<Tag> tags,
            String petStatus
    ) {
        LOG.info("Entering updatePet() class PetRepository");

        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            try (CallableStatement stmt = conn.prepareCall(Constants.SQL_Query.UPDATE_PET)) {
                // In
                stmt.setLong(Constants.SQL_Query.FIRST_PARAM, petId);
                stmt.setString(Constants.SQL_Query.SECOND_PARAM, pet_name);
                stmt.setLong(Constants.SQL_Query.THIRD_PARAM, category.getId());
                stmt.setString(Constants.SQL_Query.FOURTH_PARAM, category.getName());

                // Converts photoUrls to SQL Arrays
                Array urlArray = conn.createArrayOf(Constants.SQL_Types.TEXT, photoUrls.toArray());
                stmt.setArray(Constants.SQL_Query.FIFTH_PARAM, urlArray);

                // Converts tags to sql array
                PetTagMapper petTagMapper = new PetTagMapper();
                Array tagNameArray = petTagMapper.mapPetTagToSql(conn, tags);
                stmt.setArray(Constants.SQL_Query.SIXTH_PARAM, tagNameArray);

                stmt.setString(Constants.SQL_Query.SEVENTH_PARAM, petStatus);

                stmt.execute();
            }
        } catch (SQLException e) {
            if (Constants.SQL_Error.NO_ID_FOUND.equals(e.getSQLState())) {
                LOG.warn("No pet found with Id: {}", petId);
                throw new BusinessException(HttpStatus.NOT_FOUND.value(),
                        Constants.Error.PET_NOT_FOUND,
                        e.getMessage()
                );
            } else {
                LOG.warn("Something went wrong in the SQL query {}", e.getMessage());
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST.value(),
                        Constants.Error.BAD_QUERY,
                        e.getMessage()
                );
            }
        }

        LOG.info("Exiting updatePet() class PetRepository");
    }

    public Pet getPetById(Long petId) {
        LOG.info("Entering getPetById() class PetRepository");

        Pet pet = null;
        try (Connection conn = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            conn.setAutoCommit(false);
            try (CallableStatement stmt = conn.prepareCall(Constants.SQL_Query.GET_PET_BY_ID)) {
                // IN
                stmt.setLong(Constants.SQL_Query.FIRST_PARAM, petId);

                // OUT
                stmt.registerOutParameter(Constants.SQL_Query.SECOND_PARAM, Types.OTHER);

                // Execute the query
                stmt.execute();

                // Map the results to a Pet Object
                try (ResultSet rs = (ResultSet) stmt.getObject(Constants.SQL_Query.SECOND_PARAM)) {
                    if (!rs.next()) {
                       LOG.warn("No pet exists with Id: {}", petId);
                        throw new BusinessException(
                                HttpStatus.NOT_FOUND.value(),
                                Constants.Error.PET_NOT_FOUND,
                                "No pet exists with Id: " + petId
                        );
                    }

                    PetRowMapper petMapper = new PetRowMapper();
                    pet = petMapper.mapRow(rs, 0);
                }
            }
            conn.commit();
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            LOG.warn("Something went wrong in the SQL query {}",e.getMessage());
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), Constants.Error.BAD_QUERY, e.getMessage());
        }

        LOG.info("Exiting getPetById() class PetRepository");
        return pet;
    }

    public List<Pet> getPetByTagName(List<String> tagNames) {
        LOG.info("Entering getPetByTagName() class PetRepository");
        List<Pet> pets = new ArrayList<>();
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try (CallableStatement stmt = conn.prepareCall(Constants.SQL_Query.GET_PET_BY_TAG_NAME)) {
                // IN
                Array tNames = conn.createArrayOf(Constants.SQL_Types.TEXT, tagNames.toArray());
                stmt.setArray(Constants.SQL_Query.FIRST_PARAM, tNames);

                // OUT
                stmt.registerOutParameter(Constants.SQL_Query.SECOND_PARAM, Types.OTHER);

                // Execute the query
                stmt.execute();

                // Map the results to a Pet Object
                try (ResultSet rs = (ResultSet) stmt.getObject(Constants.SQL_Query.SECOND_PARAM)) {
                    int i = 0;
                    while (rs.next()) {
                        PetRowMapper petRowMapper = new PetRowMapper();
                        pets.add(petRowMapper.mapRow(rs, i));
                    }
                }
            }
            conn.commit();
        } catch (SQLException e) {
            LOG.warn("Something went wrong in the SQL query {}",e.getMessage());
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                    e.getMessage());
        }
        LOG.info("Exiting getPetByTagName() class PetRepository");
        return pets;
    }

    public void deletePetById(Long petId) {
        LOG.info("Entering deletePetById() class PetRepository");
        try (Connection conn = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            try (CallableStatement stmt = conn.prepareCall(Constants.SQL_Query.DELETE_PET_BY_ID)) {
                // In
                stmt.setLong(Constants.SQL_Query.FIRST_PARAM, petId);

                stmt.execute();
            }
        } catch (SQLException e) {
            if (Constants.SQL_Error.NO_ID_FOUND.equals(e.getSQLState())) {
                LOG.warn("No pet found with Id: {}", petId);
                throw new BusinessException(HttpStatus.NOT_FOUND.value(),
                        Constants.Error.PET_NOT_FOUND,
                        e.getMessage()
                );
            }
        } catch (NullPointerException e) {
            LOG.warn("The jdbcTemplate.getDateSource() is null");
            throw new BusinessException(HttpStatus.BAD_GATEWAY.value(),
                    Constants.Error.PET_NOT_FOUND,
                    e.getMessage()
            );
        }
        LOG.info("Exiting deletePetById() class PetRepository");
    }

    public List<Pet> getPetByStatusName(List<String> status) {
        LOG.info("Entering getPetByStatusName() class PetRepository");

        List<Pet> pets = new ArrayList<>();

        try (Connection conn = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            conn.setAutoCommit(false);
            try (CallableStatement stmt = conn.prepareCall(Constants.SQL_Query.GET_PET_BY_STATUS)) {
                // IN
                Array s_names = conn.createArrayOf(Constants.SQL_Types.PET_STATUS, status.toArray());
                stmt.setArray(Constants.SQL_Query.FIRST_PARAM, s_names);

                // OUT
                stmt.registerOutParameter(Constants.SQL_Query.SECOND_PARAM, Types.REF_CURSOR);

                stmt.execute();

                // Map results to Pet Object add to list
                try (ResultSet rs = (ResultSet) stmt.getObject(Constants.SQL_Query.SECOND_PARAM)) {
                    int i = 0;
                    while (rs.next()) {
                        PetRowMapper petRowMapper = new PetRowMapper();
                        pets.add(petRowMapper.mapRow(rs, i));
                    }
                }
            }
            conn.commit();
        } catch (SQLException e) {
            if (Constants.SQL_Error.INVALID_ENUM_INPUT.equals(e.getSQLState())) {
                LOG.warn("Invalid order status enum type");
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                        Constants.Error.INVALID_INPUT,
                        e.getMessage());
            } else {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            LOG.warn("Something went wrong in the SQL query {}",e.getMessage());
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), Constants.Error.BAD_QUERY, e.getMessage());
        }
        LOG.info("Exiting getPetByStatusName() class PetRepository");
        return pets;
    }

    public void updatePetWithForm(Long petId, String name, String status) {
        LOG.info("Entering updatePetWithForm() class PetRepository");

        try (Connection conn = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            try (CallableStatement stmt = conn.prepareCall(Constants.SQL_Query.UPDATE_PET_WITH_FORM)) {
                // IN
                stmt.setLong(Constants.SQL_Query.FIRST_PARAM, petId);
                stmt.setString(Constants.SQL_Query.SECOND_PARAM, name);
                stmt.setString(Constants.SQL_Query.THIRD_PARAM, status);

                stmt.execute();
            }
        } catch (SQLException e) {
            if (Constants.SQL_Error.INVALID_ENUM_INPUT.equals(e.getSQLState())) {
                LOG.warn("Invalid Enum Type inputted");
                throw new BusinessException(
                        HttpStatus.METHOD_NOT_ALLOWED.value(),
                        Constants.Error.INVALID_INPUT,
                        "Pet Id input is invalid, please enter a valid Pet Id"
                );
            } else if (Constants.SQL_Error.NO_ID_FOUND.equals(e.getSQLState())) {
                LOG.warn("Invalid pet Id {}", petId);
                throw new BusinessException(
                        HttpStatus.METHOD_NOT_ALLOWED.value(),
                        Constants.Error.INVALID_INPUT_ID,
                        "Not Pet with pet id was found"
                );
            }
        } catch (Exception e) {
            LOG.warn("Something went wrong {}", e.getMessage());
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST.value(),
                    Constants.Error.BAD_QUERY,
                    e.getMessage()
            );
        }
        LOG.info("Exiting updatePetWithForm() class PetRepository");
    }

    public static class PetRowMapper implements RowMapper<Pet> {

        @Override
        public Pet mapRow(ResultSet rs, int rowNum) throws SQLException {
            // Initialising non array pet attributes
            Pet pet = new Pet();
            pet.setId(rs.getLong("pet_id"));
            pet.setName(rs.getString("pet_name"));

            Category category = new Category();
            category.setId(rs.getLong("category_id"));
            category.setName(rs.getString("category_name"));
            pet.setCategory(category);

            // Map this from order_status to ENUM string i.e. ("available")
            pet.setStatus(Pet.StatusEnum.fromValue(rs.getString("pet_status")));

            // Array Lists
            Array petUrlArray = rs.getArray("pet_urls");
            Array petTagArray = rs.getArray("pet_tag");

            // pet_urls -> List<String>
            if (petUrlArray != null) {
                String[] urlArray = (String[]) petUrlArray.getArray();
                pet.setPhotoUrls(Arrays.asList(urlArray));
            }

            // pet_tag -> List<String>
            if (petTagArray != null) {
                PetTagMapper petTagMapper = new PetTagMapper();
                List<Tag> tags = petTagMapper.mapSqlToPetTags(petTagArray);
                pet.setTags(tags);
            }

            System.out.println(pet.toString());

            return pet;
        }

    }

    public static class PetTagMapper {
        public java.sql.Array mapPetTagToSql(Connection conn, List<Tag> tags) throws SQLException {
            String[] pgFormattedTags = tags.stream()
                    .map(PetTagMapper::toCompositeFormat)
                    .toArray(String[]::new);

            return conn.createArrayOf(Constants.SQL_Types.TAG_NAME, pgFormattedTags);
        }

       public List<Tag> mapSqlToPetTags(Array petTagArray) throws SQLException {
            List<Tag> tags = new ArrayList<>();
            for (Object tObjs : (Object[]) petTagArray.getArray()) {
                String str = tObjs.toString();
                str = str.replaceAll("[()]","");
                String[] tStr = str.split(",", 2);
                Tag tag = new Tag();
                tag.setId(Long.valueOf(tStr[0]));
                tag.setName(tStr[1]);
                tags.add(tag);
            }

            return tags;
        }

        public static String toCompositeFormat(Tag tag) {
            return "(" + (tag.getId() == null ? "" : tag.getId()) + "," + tag.getName() + ")";
        }
    }
}
