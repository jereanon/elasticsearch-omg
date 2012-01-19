package org.elasticsearch.omg.support;

import java.lang.annotation.*;

/**
 * This annotation will help define how a mapping will be defined, as well as how items will be queried and filtered.
 *
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ElasticSearchProperty {

    /**
     * If this field is marked as true, the field associated with this annotation will be marked
     * as analyzed when indexed, but if this field is true as well as {@see freeTextSearch}, the field will be
     * marked as a multi-field type. {@see http://www.elasticsearch.org/guide/reference/mapping/multi-field-type.html}.
     * @return true if freeTextSearching should be enabled
     */
    boolean freeTextSearchable() default false;

    /**
     * If this field is marked as true, then the field will be indexed as not_analyzed,
     * but if this field is true as well as {@see freeTextSearch}, the field will be
     * marked as a multi-field type. {@see http://www.elasticsearch.org/guide/reference/mapping/multi-field-type.html}.
     * @return if the field is filterable
     */
    boolean filterable() default true;

    /**
     * Define the type of property that is being stored.
     * @return the type of elastic search property
     */
    ElasticSearchPropertyType type();
}

