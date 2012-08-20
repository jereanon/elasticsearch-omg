package org.elasticsearch.omg.support.springframework;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which supplies a name for the ElasticSearchRepository to be
 * automatically generated for persisting the bean.
 *
 * @author pchapman
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ManagedByRepository {
    String beanName();
}
