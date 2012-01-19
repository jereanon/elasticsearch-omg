package com.elasticsearch.omg.support.model;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Anything that is going to be indexed into the Elastic Search index needs to implement this interface.
 * It makes it so indexable objects provide an ID for the index (this will avoid having elastic search
 * auto-generate an ID).
 */
public interface Indexable {

    /**
     * @return the ID to use when indexing an object
     */
    @JsonIgnore
    String getIndexableId();

    /**
     * @return the elastic search object type to put this object into
     */
    @JsonIgnore
    String getIndexObjectType();
}
