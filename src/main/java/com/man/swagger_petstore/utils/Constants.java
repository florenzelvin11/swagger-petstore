package com.man.swagger_petstore.utils;

public class Constants {

    public class SQL_Query {
        // Queries
        public static String INSERT_PET = "CALL insert_pet(?,?,?,?,?,?,?::order_status";

        // Parameter Counts
        public static int FIRST_PARAM = 1;
        public static int SECOND_PARAM = 2;
        public static int THIRD_PARAM = 3;
        public static int FOURTH_PARAM = 4;
        public static int FIFTH_PARAM = 5;
        public static int SIXTH_PARAM = 6;
        public static int SEVENTH_PARAM = 7;
        public static int EIGHTH_PARAM = 8;
        public static int NINTH_PARAM = 9;
        public static int TENTH_PARAM = 10;
    }

    public class SQL_Types {
        public static String TEXT = "text";
        public static String TAG_NAME = "tag_name";
    }

    public class Error {
        public static String BAD_QUERY = "SQl query went bad";
        public static String INVALID_INPUT = "Invalid Input";
    }
}
