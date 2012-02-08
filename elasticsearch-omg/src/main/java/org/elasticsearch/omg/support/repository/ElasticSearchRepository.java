package org.elasticsearch.omg.support.repository;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.action.deletebyquery.DeleteByQueryRequestBuilder;
import org.elasticsearch.client.action.get.MultiGetRequestBuilder;
import org.elasticsearch.client.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.omg.ElasticSearchOMGException;
import org.elasticsearch.omg.support.model.query.ComplexQuery;
import org.elasticsearch.omg.support.model.result.ElasticSearchResult;
import org.elasticsearch.omg.support.model.result.ElasticSearchResults;
import org.elasticsearch.omg.util.ElasticSearchMappingUtil;
import org.elasticsearch.omg.util.JSONUtil;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Abstract class for any class implementing the repository design pattern to connect to elastic search
 * 
 * @author jereanon
 * @author pchapman
 */
@Component
public final class ElasticSearchRepository<T> {
    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchRepository.class);

    @Autowired
    private Client client;
    
    private Integer bulkSize;
    
    private String indexName;
    
    private String objectType;

    private Class<T> storedClass;
    
    public ElasticSearchRepository(Class<T> storedClass, Client client, Integer bulkSize) {
        this.bulkSize = bulkSize;
        this.client = client;
        this.storedClass = storedClass;
        indexName = ElasticSearchMappingUtil.getIndexName(storedClass);
        objectType = ElasticSearchMappingUtil.getObjectType(storedClass);
    }
    
    public ElasticSearchRepository(String storedClassName, Client client, Integer bulkSize) throws ClassNotFoundException {
        this(((Class<T>)Class.forName(storedClassName)), client, bulkSize);
    }

    /**
     * Indexes a single object into elastic search
     *
     * @param object the object to index
     * @throws ElasticSearchOMGException on error
     */
    public void indexObject(T object) throws ElasticSearchOMGException {
        if (object == null) {
            return;
        }

        String json = JSONUtil.serializeToString(object);
        String id = ElasticSearchMappingUtil.getId(object);
        getClient().prepareIndex(indexName, objectType, id)
                .setSource(json)
                .setRefresh(true)
                .execute()
                .actionGet();
    }

    /**
     * Indexes a list of objects into elastic search
     *
     * @param objects the objects to index
     * @throws ElasticSearchOMGException on error
     */
    public void indexObjects(List<T> objects) throws ElasticSearchOMGException {
        if (objects == null || objects.isEmpty()) {
            return;
        }

        // TODO: this loop can run with 0 items in the subobjects list occasionally, fix it.
        for (int i = 0; i <= objects.size(); i += bulkSize) {
            BulkRequestBuilder bulk = client.prepareBulk().setRefresh(true);

            Integer toIndex = i + bulkSize;
            if (toIndex > objects.size()) {
                toIndex = objects.size();
            }
            List<T> subObjects = objects.subList(i, toIndex);
            for (T object : subObjects) {
                String id = ElasticSearchMappingUtil.getId(object);
                try {
                    String json = JSONUtil.serializeToString(object);
                    bulk.add(client.prepareIndex(indexName, objectType).setSource(json).setId(id));
                } catch (ElasticSearchOMGException e) {
                    logger.error("Error unmarshalling object and adding to bulk with ID: " + id, e);
                    throw new ElasticSearchOMGException("Error unmarshalling object and adding to bulk with ID: " + id, e);
                }
            }

            if (!subObjects.isEmpty()) {
                logger.debug("executing bulk index request: "+bulk);
                bulk.execute().actionGet();
            }
        }
    }

    /**
     * Retrieves an object from the elastic search index by its ID.
     * TODO: this makes an assumption currently that all of the objects that are indexed across this repository each
     * TODO (cont): have a unique ID. It returns the first result.
     *
     * @param clazz the class of the object to search for.
     * @param id the ID
     * @return the object, if found
     * @throws ElasticSearchOMGException on error
     */
    public <T> T getObject(String id) throws ElasticSearchOMGException {
        //TODO Get document type from class definition?  What about ID collisions among document types?
        GetResponse response = getClient().prepareGet(indexName, objectType, id)
                .execute()
                .actionGet();
        return (T)getObjectFromResponse(response);
    }

    /**
     * Retrieves a list of objects from the elastic search index by their IDs
     *
     * @param clazz the class of objects to retrieve
     * @param ids the IDs to retrieve on
     * @return the objects, if found
     * @throws ElasticSearchOMGException on error
     */
    public List<T> getObjects(String... ids) throws ElasticSearchOMGException {
        MultiGetRequestBuilder request = getClient().prepareMultiGet();
        for (String id : ids) {
            //TODO Get document type from class definition?  What about ID collisions among document types?
            request.add(indexName, objectType, id);
        }
        try {
            MultiGetResponse response = request.execute().actionGet();
            return (List<T>) getObjectsFromResponse(response);
        } catch (Exception e) {
            // TODO: this seems to have changed in newer versions of ES, previously I believe ES returned the equivalent of a map of ID -> (exists: false, source: null)
            throw new ElasticSearchOMGException("Error executing multiget for IDs: " + ids, e);
        }
    }

    /**
     * Marshalls a {@link org.elasticsearch.action.get.MultiGetResponse} of objects into their java objects
     *
     * @param response the get response
     * @return the collection
     * @throws ElasticSearchOMGException on error
     */
    protected List<T> getObjectsFromResponse(MultiGetResponse response) throws ElasticSearchOMGException {
        List<T> results = new ArrayList<T>(response.responses().length);

        for (MultiGetItemResponse item : response.responses()) {
            if (item.getResponse().exists()) {
                T object = (T) JSONUtil.deserializeToObject(item.getResponse().sourceAsString(), storedClass);
                results.add(object);
            }
        }
        return results;
    }

    /**
     * Marshalls a single {@link GetResponse}s JSON source (if it exists) into an object
     *
     * @param response the response from the request
     * @return the marshalled java object
     * @throws ElasticSearchOMGException on error
     */
    protected T getObjectFromResponse(GetResponse response) throws ElasticSearchOMGException {
        if (!response.exists() || response.isSourceEmpty()) {
            return null;
        }

        return (T) JSONUtil.deserializeToObject(response.sourceAsString(), storedClass);
    }

    /**
     * Deletes a single object from an elastic search index
     *
     * @param object the object to delete
     * @throws ElasticSearchOMGException on error
     */
    public void deleteObject(T object) throws ElasticSearchOMGException {
        if (object == null) {
            return;
        }

        String id = ElasticSearchMappingUtil.getId(object);
        getClient().prepareDelete(indexName, objectType, id)
                .execute()
                .actionGet();
    }

    /**
     * Delete items in the index given a specific query.
     * @param indexName the name of the index
     * @param queryBuilder the query
     */
    //TODO is requesting indexName the right thing to do?
    public void deleteByQuery(QueryBuilder queryBuilder) {
        if (queryBuilder == null) {
            return;
        }

        // execute the delete
        getClient().deleteByQuery(
                new DeleteByQueryRequestBuilder(getClient())
                        .setQuery(queryBuilder)
                        .request()
                        .indices(indexName)).actionGet();
    }

    /**
     * Deletes a list of objects from an elastic search index
     *
     * @param objects the objects to delete
     * @throws ElasticSearchOMGException on error
     */
    public void deleteObjects(List<T> objects) throws ElasticSearchOMGException {
        if (objects == null || objects.isEmpty()) {
            return;
        }

        for (int i = 0; i <= objects.size(); i += bulkSize) {
            BulkRequestBuilder bulk = client.prepareBulk();

            Integer toIndex = i + bulkSize;
            if (toIndex > objects.size()) {
                toIndex = objects.size();
            }
            List<T> subObjects = objects.subList(i, toIndex);
            for (T object : subObjects) {
                String id = ElasticSearchMappingUtil.getId(object);
                bulk.add(client.prepareDelete(indexName, objectType, id));
            }

            bulk.execute().actionGet();
        }
    }

    /**
     * Prepares a {@link org.elasticsearch.client.action.search.SearchRequestBuilder} for querying against the elastic search index. The classes provided will
     * converted into elastic search object types based on the configured mapping
     *
     * @param query   the query to apply to the search
     * @param filter  a filter to apply to the query
     * @param sort    sorting criteria to apply to the search results
     * @return the search
     */
    protected SearchRequestBuilder prepareSearch(QueryBuilder query, FilterBuilder filter, SortBuilder sort) {

        SearchRequestBuilder search = getClient().prepareSearch(indexName)
                .setTypes(objectType)
                .setSearchType(SearchType.QUERY_THEN_FETCH);

        if (query != null) {
            search.setQuery(query);
        }
        if (filter != null) {
            search.setFilter(filter);
        }
        if (sort != null) {
            search.addSort(sort);
        }

        return search;
    }

    /**
     * Convenience method
     *
     * @param query   the query to execute
     * @param classes the classes to allow in the query
     * @return the search request
     */
    protected SearchRequestBuilder prepareSearch(QueryBuilder query) {
        return prepareSearch(query, null, null);
    }

    /**
     * Executes a search for objects based on the provided criteria
     *
     * @param search the query to execute
     * @return the search result
     * @throws ElasticSearchOMGException on error
     */
    protected ElasticSearchResults<T> executeSearch(SearchRequestBuilder search) throws ElasticSearchOMGException {
        SearchResponse response = search.execute().actionGet();
        ElasticSearchResults<T> result = new ElasticSearchResults<T>();
        Collection<ElasticSearchResult<T>> results = getObjectsFromResponse(response);
        result.setResults(results);
        result.setTotalResults(response.getHits().getTotalHits());
        return result;
    }

    /**
     * Execute a search from a given {@link org.elasticsearch.omg.support.model.query.ComplexQuery}.
     *
     * @param query the query
     * @return the results
     * @throws ElasticSearchOMGException on error
     */
    public ElasticSearchResults<T> executeSearch(ComplexQuery query) throws ElasticSearchOMGException {
        SearchRequestBuilder search = prepareSearch(query.getBoolQueryBuilder(), query.getBoolFilterBuilder(), query.getSortBuilder());
        SearchResponse response = search.execute().actionGet();
        ElasticSearchResults<T> result = new ElasticSearchResults<T>();
        Collection<ElasticSearchResult<T>> results = getObjectsFromResponse(response);
        result.setResults(results);
        result.setTotalResults(response.getHits().getTotalHits());
        return result;
    }

    /**
     * Retrieve the JSON object results from elastic search and marshall them into java objects
     *
     * @param response the {@link SearchResponse} from elastic search
     * @param classes  the types to allow (marshall to)
     * @return the list of unmarshalled objects
     * @throws ElasticSearchOMGException on error
     */
    protected Collection<ElasticSearchResult<T>> getObjectsFromResponse(SearchResponse response) throws ElasticSearchOMGException {
        List<ElasticSearchResult<T>> results = new ArrayList<ElasticSearchResult<T>>();

        if (response.hits() == null || response.hits().totalHits() == 0) {
            return results;
        }

        for (SearchHit hit : response.hits().getHits()) {
            try {
                T object = (T) JSONUtil.deserializeToObject(hit.sourceAsString(), storedClass);
                results.add(new ElasticSearchResult<T>(object, storedClass, hit.getScore()));
            } catch (ElasticSearchOMGException ex) {
                logger.error("Error unmarshalling result", ex);
                throw new ElasticSearchOMGException("Error unmarshalling result. ID: " + hit.getId() + " , type: " + hit.getType(), ex);
            }
        }
        return results;
    }

    /**
     * Create an index.
     */
    public void createIndex() {
        if (indexExists()) {
            logger.warn("Attemping to create index: "+indexName+" that already exists.");
            return;
        }
        logger.debug("Creating index: "+indexName);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
        client.admin().indices().create(createIndexRequest).actionGet();
    }

    /**
     * Create an index.
     *
     * @param settings extra settings for this index creation
     */
    public void createIndex(Map<String, String> settings) {
        if (indexExists()) {
            logger.warn("Attemping to create index: "+indexName+" that already exists.");
            return;
        }
        logger.debug("Creating index: "+indexName);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
        createIndexRequest.settings(settings);
        client.admin().indices().create(createIndexRequest).actionGet();
    }

    /**
     * Create a mapping with the specified json.
     *
     * @param json the json for the mapping
     */
    protected void createMapping(String type, String json) {
        logger.debug("Elastic Search creating mapping with contents: "+json);
        PutMappingRequest putMapping = new PutMappingRequest(indexName);
        putMapping.source(json);
        putMapping.type(type);
        client.admin().indices().putMapping(putMapping).actionGet();
    }

    /**
     * Create the mapping for a given class.
     *
     * @param clazz the class
     */
    public void createMapping() {
        try {
            String json = ElasticSearchMappingUtil.createJSONStringForType(storedClass);
            createMapping(objectType, json);
        } catch (Exception ex) {
            throw new ElasticSearchOMGException("Exception creating mapping for type: "
                    +storedClass.getSimpleName());
        }
    }

    /**
     * Delete the index.
     */
    public void deleteIndex() {
        if (indexExists()) {
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
            client.admin().indices().delete(deleteIndexRequest).actionGet();
        } else {
            logger.warn("Asked to delete an index that doesn't exist: {}", indexName);
        }
    }

    /**
     * Check to see if the index exists,
     * 
     * @return true if the index exists, false otherwise
     */
    public boolean indexExists() {
        IndicesExistsRequest indicesExistsRequest = new IndicesExistsRequest(indexName);
        return client.admin().indices().exists(indicesExistsRequest).actionGet().exists();
    }

    /**
     * The {@link Client} used to interact with an elastic search cluster
     *
     * @param client the client
     */
    public Client getClient() {
        return client;
    }
    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * The number of requests to use when executing bulks
     *
     * @param bulkSize the size
     */
    public void setBulkSize(Integer bulkSize) {
        this.bulkSize = bulkSize;
    }

    /**
     * Turns the elastic search type into a Class, if it exists
     *
     * @param type the elastic saerch type
     * @return the class
     * @throws ElasticSearchOMGException on error
     */
    public Class<T> getClassForType(String type) throws ElasticSearchOMGException {
        try {
            return (Class<T>) Class.forName(type);
        } catch (Exception e) {
            throw new ElasticSearchOMGException("No class found for Elastic Search type: " + type, e);
        }
    }

    /**
     * Gets a list of elastic search object types from the classes provided.
     *
     * @param classes the classes to get object types from
     * @return the list of object types
     */
    public String[] getObjectTypes(Class<T>... classes) {
        String[] types = new String[classes.length];
        for (int i = 0; i < classes.length; i++) {
            types[i] = classes[i].getName();
        }
        return types;
    }
    
    /**
     * Gets a list of all the indexes that the class types are stored into.
     * 
     * @param classes
     * @return 
     */
    public String[] getIndexes(Class<T>... classes) {
        Set<String> indexes = new HashSet<String>();
        for (Class<T> c : classes) {
            indexes.add(ElasticSearchMappingUtil.getIndexName(c));
        }
        return indexes.toArray(new String[0]);
    }
}
