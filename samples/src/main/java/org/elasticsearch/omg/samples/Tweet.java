package org.elasticsearch.omg.samples;

import java.util.Date;

/**
 * an individual tweet!
 */
public class Tweet {

    private String user;
    private Date postDate;
    private String message;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(Date postDate) {
        this.postDate = postDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
