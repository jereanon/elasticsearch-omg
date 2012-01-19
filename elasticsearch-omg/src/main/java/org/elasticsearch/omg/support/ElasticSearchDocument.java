package org.elasticsearch.omg.support;

/**
 * Annotates a class to be stored as a document in elasticsearch.
 * 
 * @author jereanon
 * @author pchapman
 */
public @interface ElasticSearchDocument {
    /**
     * The name of documents of this type.  Not to be confused with field
     * types.  If no type name is provided, the simple name of the annotated
     * class will be used.
     * 
     * In the following example, the document type name is &quot;tweet&quot;:
     * 
     * <code>
     * $ curl -XPUT 'http://localhost:9200/twitter/tweet/1' -d '{
     *     &quot;user&quot; : &quot;kimchy&quot;,
     *     &quot;post_date&quot; : &quot;2009-11-15T14:12:12&quot;,
     *     &quot;message&quot; : &quot;trying out Elastic Search&quot;
     * }'
     * </code>
     * 
     * @return 
     */
    String typeName() default "";
}
