package com.elasticsearch.omg;

/**
 * Base exception class thrown when stuff goes wrong.
 */
public class ElasticSearchOMGException extends RuntimeException {

    public ElasticSearchOMGException() {
    }

    public ElasticSearchOMGException(String s) {
        super(s);
    }

    public ElasticSearchOMGException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
