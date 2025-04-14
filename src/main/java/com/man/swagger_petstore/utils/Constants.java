package com.man.swagger_petstore.utils;

public class Constants {

    public static class SQL_Query {
        // Queries
        public static final String INSERT_PET = "CALL insert_pet(?,?,?,?,?,?,?::pet_status)";
        public static final String GET_PET_BY_ID = "CALL get_pet_by_id(?, ?)";
        public static final String UPDATE_PET = "CALL update_pet(?,?,?,?,?,?,?::pet_status)";
        public static final String DELETE_PET_BY_ID = "CALL delete_pet_by_id(?)";
        public static final String GET_PET_BY_TAG_NAME = "CALL get_pet_by_tag_name(?, ?)";
        public static final String GET_PET_BY_STATUS = "CALL get_pet_by_status(?, ?)";
        public static final String UPDATE_PET_WITH_FORM = "CALL update_pet_with_form(?,?,?::pet_status)";
        public static final String INSERT_USER = "CALL insert_user(?,?,?,?,?,?,?,?)";
        public static final String GET_USER_BY_USERNAME = "CALL get_user_by_username(?,?)";
        public static final String UPDATE_USER = "CALL update_user(?,?,?,?,?,?,?,?)";
        public static final String DELETE_USER = "CALL delete_user(?)";
        public static final String GET_INVENTORY = "CALL get_inventory(?)";
        public static final String ADD_ORDER = "CALL add_order(?,?,?,?,?::order_status,?)";
        public static final String GET_ORDER = "CALL get_order(?,?)";

        // Parameter Counts
        public static final int FIRST_PARAM = 1;
        public static final int SECOND_PARAM = 2;
        public static final int THIRD_PARAM = 3;
        public static final int FOURTH_PARAM = 4;
        public static final int FIFTH_PARAM = 5;
        public static final int SIXTH_PARAM = 6;
        public static final int SEVENTH_PARAM = 7;
        public static final int EIGHTH_PARAM = 8;
        public static final int NINTH_PARAM = 9;
        public static final int TENTH_PARAM = 10;
    }

    public static class SQL_Types {
        public static final String TEXT = "text";
        public static final String TAG_NAME = "tag_name";
        public static final String ORDER_STATUS = "order_status";
        public static final String PET_STATUS = "pet_status";
    }

    public static class Error {
        public static final String BAD_QUERY = "SQL query went bad";
        public static final String INVALID_INPUT = "Invalid Input";
        public static final String INVALID_INPUT_ID = "Invalid ID supplied";
        public static final String DUPLICATE_ID = "Duplicate Id";
        public static final String PET_NOT_FOUND = "Pet not found";
        public static final String TAG_NOT_FOUND = "Tag not found";
        public static final String INVALID_USER = "Invalid username/password supplied";
    }

    public static class SQL_Error {
        public static final String UNIQUE_VIOLATION = "23505";
        public static final String NO_ID_FOUND = "P0001";
        public static final String INVALID_ENUM_INPUT = "22P02";
    }
}
