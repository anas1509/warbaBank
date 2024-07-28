package com.warba.account.account_service.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONUtil {

	/**
	 * A mapper class used to convert Objects from and to JSON Strings
	 * */
	private static final ObjectMapper objectMapper = new ObjectMapper();

    // Convert object to JSON string
    public static String convertObjectToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Convert JSON string to object
    public static <T> T convertJsonToObject(String jsonString, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}

