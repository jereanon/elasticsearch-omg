package org.elasticsearch.omg.samples;

import org.elasticsearch.common.jackson.annotate.JsonIgnore;
import org.elasticsearch.omg.support.ElasticSearchDocument;
import org.elasticsearch.omg.support.ElasticSearchDocumentId;
import org.elasticsearch.omg.support.ElasticSearchProperty;
import org.elasticsearch.omg.support.ElasticSearchPropertyType;

import java.util.Date;
import org.elasticsearch.omg.support.ElasticSearchIndex;
import org.elasticsearch.omg.support.springframework.ManagedByRepository;

/**
 * an individual tweet!
 */
@ElasticSearchIndex(name = "tweets")
@ElasticSearchDocument(typeName = "tweet")
@ManagedByRepository(beanName="tweetRepo")
public class Tweet {

    private Long id;
    private String user;
    private Date postDate;
    private String message;

    @ElasticSearchDocumentId(autoGenerate = true)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @ElasticSearchProperty(filterable = true, freeTextSearchable = false, type = ElasticSearchPropertyType.DATE)
    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(Date postDate) {
        this.postDate = postDate;
    }

    @ElasticSearchProperty(freeTextSearchable = true, type = ElasticSearchPropertyType.STRING)
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * {@inheritDoc}
     */
    @JsonIgnore
    @Override
    public String toString() {
        return "["
                + " id: "+id
                + " user: "+user
                + " date: "+postDate
                + " message: "+message
                + "]";
    }
}
