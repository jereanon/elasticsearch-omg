package com.elasticsearch.omg.support.model.result;

/**
 * Models an elastic search query result.
 */
public class ElasticSearchResult<T> {
    private T        result;
    private Class<T> type;
    private float    score;

    public ElasticSearchResult() {
    }

    public ElasticSearchResult(T result, Class<T> type) {
        this.result = result;
        this.type = type;
    }

    public ElasticSearchResult(T result, Class<T> type, float score) {
        this.result = result;
        this.type = type;
        this.score = score;
    }

    /**
     * The marshalled JSON object from elastic search
     *
     * @return the result
     */
    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    /**
     * The type used to marshall the JSON result to the result object.
     *
     * @return the type
     */
    public Class<T> getType() {
        return type;
    }

    public void setType(Class<T> type) {
        this.type = type;
    }

    /**
     * The elastic search generated score of the object. This can be <p>null</p> if the result is actually from a GET
     * request.
     *
     * @return the score
     */
    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }
}
