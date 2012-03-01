package org.elasticsearch.omg.samples;

import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.omg.support.model.query.ComplexQuery;
import org.elasticsearch.omg.support.model.result.ElasticSearchResult;
import org.elasticsearch.omg.support.model.result.ElasticSearchResults;
import org.elasticsearch.omg.support.repository.ElasticSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Class for running examples.
 */
@Component
public class ExampleBusinessRunner {

    @Autowired
    @Qualifier("tweetRepo")
    private ElasticSearchRepository<Tweet> esRepo;

    public void doBusiness() {
        // do stuffs
        List<Tweet> tweets = createTweets();

        // create an in memory index and index some things
        Map<String, String> settings = new HashMap<String, String>();
        settings.put("es.index.storage.type", "memory");
        esRepo.deleteIndex(); // In case a previous run created it
        esRepo.createIndex(settings);
        esRepo.createMapping();
        esRepo.indexObjects(tweets);

        // query for dog tweets
        ComplexQuery dogQuery = new ComplexQuery(){{
            setBoolFilterBuilder(new BoolFilterBuilder().must(FilterBuilders.termFilter("user", "dog")));
        }};
        ElasticSearchResults<Tweet> retDogTweets = esRepo.executeSearch(dogQuery);
        for (ElasticSearchResult<Tweet> tweet : retDogTweets.getResults()) {
            System.out.println("dog tweet: "+tweet.getResult().getMessage());
        }

        esRepo.deleteIndex();
    }

    /**
     * Create some {@link Tweet}s to fool with.
     *
     * @return a list of tweets
     */
    public List<Tweet> createTweets() {
        Calendar cal = Calendar.getInstance();
        final Date today = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        final Date yesterday = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, -2);
        final Date dayBeforeYesterday = cal.getTime();

        return new ArrayList<Tweet>(){{
            // today tweets
            add(new Tweet(){{
                setUser("dog");
                setPostDate(today);
                setMessage("herro, this is dog?");
            }});
            add(new Tweet(){{
                setUser("cat");
                setPostDate(today);
                setMessage("herro, this is cat?");
            }});
            add(new Tweet(){{
                setUser("derpina");
                setPostDate(today);
                setMessage("herro, this is derpina?");
            }});

            // yesterday
            add(new Tweet(){{
                setUser("dog");
                setPostDate(yesterday);
                setMessage("herro, this is dog?");
            }});
            add(new Tweet(){{
                setUser("cat");
                setPostDate(yesterday);
                setMessage("herro, this is cat?");
            }});
            add(new Tweet(){{
                setUser("derpina");
                setPostDate(yesterday);
                setMessage("herro, this is derpina?");
            }});

            // day before yesterday
            add(new Tweet(){{
                setUser("dog");
                setPostDate(dayBeforeYesterday);
                setMessage("herro, this is dog?");
            }});
            add(new Tweet(){{
                setUser("cat");
                setPostDate(dayBeforeYesterday);
                setMessage("herro, this is cat?");
            }});
            add(new Tweet(){{
                setUser("derpina");
                setPostDate(dayBeforeYesterday);
                setMessage("herro, this is derpina?");
            }});
        }};
    }
}
