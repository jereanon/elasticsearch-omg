package com.elasticsearch.omg.support.springframework;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;

/**
 * Factory Bean for creating a singleton {@link org.elasticsearch.client.transport.TransportClient} used with elastic search queries.
 */
public class ElasticSearchTransportClientFactory implements FactoryBean<Client>, InitializingBean, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchTransportClientFactory.class);

    private TransportClient client;
    private Map<String, Integer> addresses;
    private boolean              sniffCluster;

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Destroying Elastic Search TransportClient");
        }
        client.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransportClient getObject() throws Exception {
        return client;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getObjectType() {
        return TransportClient.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        final TransportClient client = sniffCluster
                ? new TransportClient(ImmutableSettings.settingsBuilder().put("client.transport.sniff", true))
                : new TransportClient();

        if (addresses != null && addresses.size() > 0) {
            for (Map.Entry<String, Integer> address : addresses.entrySet()) {
                if (logger.isInfoEnabled()) {
                    logger.info("Adding InetSocketTransportAddress. address: " + address.getKey() + " port: " + address.getValue());
                }
                client.addTransportAddress(new InetSocketTransportAddress(address.getKey(), address.getValue()));
            }
        }

        this.client = client;
    }

    public void setAddresses(final Map<String, Integer> addresses) {
        this.addresses = addresses;
    }

    public void setSniffCluster(boolean sniffCluster) {
        this.sniffCluster = sniffCluster;
    }
}
