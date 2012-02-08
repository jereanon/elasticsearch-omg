package org.elasticsearch.omg.support.springframework;

import org.elasticsearch.client.Client;
import org.elasticsearch.omg.support.repository.ElasticSearchRepository;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

/**
 *
 * @author pchapman
 */
public class ElasticSearchRepositoryFactory implements FactoryBean<ElasticSearchRepository>, InitializingBean {

    private Integer bulkSize;
    public Integer getBulkSize() {
        return bulkSize;
    }
    @Required
    public void setBulkSize(Integer bulkSize) {
        this.bulkSize = bulkSize;
    }

    @Autowired
    private Client client;
    public Client getClient() {
        return client;
    }
    public void setClient(Client client) {
        this.client = client;
    }
    
    private String storedClassName;
    public String getStoredClassName() {
        return storedClassName;
    }
    @Required
    public void setStoredClassName(String storedClassName) {
        this.storedClassName = storedClassName;
    }
    
    private ElasticSearchRepository object;
    @Override
    public ElasticSearchRepository getObject() {
        return object;
    }

    @Override
    public Class<ElasticSearchRepository> getObjectType() {
        return ElasticSearchRepository.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Class clazz = Class.forName(storedClassName);
        object = new ElasticSearchRepository(clazz, client, bulkSize);
    }
}
