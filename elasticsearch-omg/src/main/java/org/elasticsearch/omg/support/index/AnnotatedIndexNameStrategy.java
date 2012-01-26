package org.elasticsearch.omg.support.index;

import org.elasticsearch.omg.support.ElasticSearchIndex;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * A name is provided through the name argument to the DocumentIndex annotation.
 *
 * @author pchapman
 */
public class AnnotatedIndexNameStrategy implements IndexNameStrategy
{
    @Override
    public String indexFor(Class<?> clazz) {
        ElasticSearchIndex index = AnnotationUtils.findAnnotation(clazz, ElasticSearchIndex.class);
        String indexName = index.name();
        if (indexName == null || indexName.length() == 0) {
            throw new IllegalArgumentException("Annotated index name is invalid for class " + clazz.getName());
        }
        return indexName;
    }
}
