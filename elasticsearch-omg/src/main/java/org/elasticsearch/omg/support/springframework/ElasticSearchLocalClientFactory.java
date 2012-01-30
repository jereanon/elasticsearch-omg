package org.elasticsearch.omg.support.springframework;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Factory bean to create an in-memory same-JVM instance of elastic search. Currently only used for testing, but
 * could conceivably be used for smaller implementations of elastic search. Currently just implements a very
 * bare-bones basic node without clustering or reading of any properties files and exposes its client as a
 * singleton.
 */
public class ElasticSearchLocalClientFactory implements FactoryBean<Client>, InitializingBean, DisposableBean {
    private Node node;
    private Client client;

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() throws Exception {
        client.close();
        node.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Client getObject() throws Exception {
        return client;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Client> getObjectType() {
        return Client.class;
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
        this.node = NodeBuilder.nodeBuilder()
                .local(true)
                .loadConfigSettings(false)
                .settings(ImmutableSettings.settingsBuilder().put("client.transport.sniff", false))
                .node().start();

        this.client = node.client();
    }
}
