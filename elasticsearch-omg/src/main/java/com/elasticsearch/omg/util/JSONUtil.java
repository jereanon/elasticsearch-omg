package com.elasticsearch.omg.util;

import com.elasticsearch.omg.ElasticSearchOMGException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Various utilities for JSON stuffs.
 */
public final class JSONUtil {

    private static ObjectMapper mapper = new ObjectMapper();

    /**
     * Serialize an object to string.
     * 
     * @param object the object
     * @return the String representation
     * @throws ElasticSearchOMGException on error
     */
    public static String serializeToString(Object object) throws ElasticSearchOMGException {
        try {
            return mapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new ElasticSearchOMGException("Error serializing object: " + object, e);
        }
    }

    /**
     * Deserializes a JSON string to the specified object type
     *
     * @param json the string to deserialize
     * @param klass the object type to use
     * @return the object
     * @throws ElasticSearchOMGException on error
     */
    public static <T> T deserializeToObject(String json, Class<T> klass) throws ElasticSearchOMGException {
        try {
            return mapper.readValue(json, klass);
        } catch (IOException e) {
            throw new ElasticSearchOMGException("Error deserializing JSON string to object of type: " + klass, e);
        }
    }
}
