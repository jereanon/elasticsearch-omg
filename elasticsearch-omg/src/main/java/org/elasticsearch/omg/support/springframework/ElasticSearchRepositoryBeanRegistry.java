package org.elasticsearch.omg.support.springframework;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.elasticsearch.client.Client;
import org.elasticsearch.omg.support.repository.ElasticSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * A post processor which will created named ElasticSearchRepository instances.
 *
 * @author pchapman
 */
public class ElasticSearchRepositoryBeanRegistry implements BeanFactoryPostProcessor {
    
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    
    private Integer bulkSize = 50;
    public Integer getBulkSize() {
        return bulkSize;
    }
    public void setBulkSize(Integer bulkSize) {
        this.bulkSize = bulkSize;
    }

    private Client client;
    public Client getClient() {
        return client;
    }
    // This is here in case user wants to set client in Spring directly XML config
    public void setClient(Client client) {
        this.client = client;
    }
    
    private String scanPath;
    public String getScanPath() {
        return scanPath;
    }
    @Required
    public void setScanPath(String path) {
        this.scanPath = path;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory registry) throws BeansException {
        if (client == null) {
            // Try to find client
            client = registry.getBean(Client.class);
            if (client == null) {
                throw new FatalBeanException("Unable to located ElasticSearch client bean");
            }
        }
        Map<String, Class<?>> managed = getRespositoryManagedObjects();
        ElasticSearchRepository repo;
        for (String beanName : managed.keySet()) {
            LOGGER.debug("Creating ElasticSearchRepository singleton with spring bean name {}", beanName);
            repo = new ElasticSearchRepository(managed.get(beanName), client, bulkSize);
            registry.registerSingleton(beanName, repo);
        }
    }
    
    private Map<String, Class<?>> getRespositoryManagedObjects() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(ManagedByRepository.class));
        Set <BeanDefinition> defs = new HashSet<BeanDefinition>();
        for (String path : scanPath.split(";")) {
            defs.addAll(scanner.findCandidateComponents(path));
        }
        Class<?> clazz;
        ManagedByRepository annot;
        Map<String, Class<?>> managed = new HashMap<String, Class<?>>(defs.size());
        for (BeanDefinition def : defs) {
            try {
                clazz = Class.forName(def.getBeanClassName());
                annot = clazz.getAnnotation(ManagedByRepository.class);
                managed.put(annot.beanName(), clazz);
            } catch (ClassNotFoundException ex) {
                LOGGER.error("Unable to get class", ex);
            }
        }
        return managed;
    }
}
