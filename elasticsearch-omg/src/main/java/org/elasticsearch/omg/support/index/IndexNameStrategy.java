package org.elasticsearch.omg.support.index;

/**
 * A strategy that provides the index into which a created managed object is
 * to be written.
 *
 * @author pchapman
 */
public interface IndexNameStrategy {
    String indexFor(Class<?> clazz);
}
