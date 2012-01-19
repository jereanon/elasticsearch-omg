package org.elasticsearch.omg.util;

import org.elasticsearch.omg.ElasticSearchOMGException;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility methods used for Java Beans.
 */
public final class JavaBeanUtil {

    /**
     * Get a set of classes from a list of classNames.
     * @return the classes
     */
    public static Set<Class<? extends Class>> getClasses(String... classNames) {
        Set<Class<? extends Class>> retClasses = new HashSet<Class<? extends Class>>();

        // exit early if possible
        if (classNames==null || classNames.length<1) {
            return retClasses;
        }

        // create the classes for each classname
        for (String className : classNames) {
            try {
                retClasses.add((Class<? extends Class>) Class.forName(className));
            } catch (ClassNotFoundException cmfe) {
                throw new ElasticSearchOMGException("Class for: " + className + " not found.");
            }
        }

        return retClasses;
    }

    /**
     * Get an {@link java.lang.annotation.Annotation} from a {@link java.beans.PropertyDescriptor} on a specific class.
     *
     * @param annotation the annotation to look for
     * @param descriptor the property descriptor
     * @return the annotation
     */
    public static <T extends Annotation> T getAnnotationFromPropertyDescriptor(Class<? extends Annotation> annotation, PropertyDescriptor descriptor) {
        if (descriptor.getReadMethod() != null && descriptor.getReadMethod().getAnnotation(annotation) != null) {
            return (T) descriptor.getReadMethod().getAnnotation(annotation);
        }
        if (descriptor.getWriteMethod() != null && descriptor.getWriteMethod().getAnnotation(annotation) != null) {
            return (T) descriptor.getWriteMethod().getAnnotation(annotation);
        }
        return null;
    }

    /**
     * Returns a list of {@link PropertyDescriptor}s for {@link java.lang.reflect.Method}s with a specific {@link Annotation}. If you want both a read and
     * write method to be present for the property, set mustHaveReadWrite to true.
     *
     * @param clazz             the class
     * @param annotation        the annotation
     * @param mustHaveReadWrite true if a get/set method must exist for property
     * @return a list of PropertyDescriptors
     */
    public static List<PropertyDescriptor> getPropertyDescriptorsForAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotation, boolean mustHaveReadWrite) {
        List<PropertyDescriptor> retList = new ArrayList<PropertyDescriptor>();
        // get all property descriptors and loop through adding the correct ones
        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(clazz);
        for (PropertyDescriptor des : descriptors) {
            boolean hasAnnotation = false;
            boolean hasRead = false;
            boolean hasWrite = false;

            // look at read
            if (des.getReadMethod() != null) {
                hasRead = true;
                if (des.getReadMethod().getAnnotation(annotation) != null) {
                    hasAnnotation = true;
                }
            }

            // look at write
            if (des.getWriteMethod() != null) {
                hasWrite = true;
                if (des.getWriteMethod().getAnnotation(annotation) != null) {
                    hasAnnotation = true;
                }
            }

            // add to results if applicable
            if (mustHaveReadWrite) {
                if (hasRead && hasWrite && hasAnnotation) {
                    retList.add(des);
                }
            } else {
                if ((hasRead || hasWrite) && hasAnnotation) {
                    retList.add(des);
                }
            }
        }

        return retList;
    }
}
