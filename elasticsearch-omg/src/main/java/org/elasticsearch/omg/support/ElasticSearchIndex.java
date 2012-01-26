package org.elasticsearch.omg.support;

import java.lang.annotation.*;

/**
 * Annotates a the name of the index into which a class to be stored as a
 * document in elasticsearch.
 * 
 * @author pchapman
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ElasticSearchIndex {
    /**
     * The name of the index if using the AnnotatedIndexNameStrategy.  The
     * default is an empty string.  Note, however, that if the default strategy
     * is used, name <strong>must have</strong> a non-empty value.
     */
    String name() default "";
    
    /**
     * The fully qualified classname of the strategy to use for getting the
     * index name for storing instances of this class.  The default is to use
     * {@link org.elasticsearch.omg.support.index.AnnotatedIndexNameStrategy}.
     */
    String strategy() default "org.elasticsearch.omg.support.index.AnnotatedIndexNameStrategy";
}
