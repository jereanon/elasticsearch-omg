package com.elasticsearch.omg.util;

import com.elasticsearch.omg.support.ElasticSearchProperty;
import com.elasticsearch.omg.support.ElasticSearchPropertyType;
import com.elasticsearch.omg.support.model.mapping.Mapping;
import com.elasticsearch.omg.support.model.mapping.MappingProperty;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities related to creating an elastic search mapping.
 *
 * {@see http://www.elasticsearch.org/guide/reference/mapping/index.html}
 */
public class ElasticSearchMappingUtil {
    
    /**
     * Create a JSON string representation of a {@link Mapping} for a specific type.
     * @param clazz the class
     * @return the json string
     * @throws Exception on error
     */
    public static String createJSONStringForType(Class clazz)
            throws Exception {
        Mapping mapping = createMappingForType(clazz);
        return getJSONStringFromMapping(mapping);
    }

    /**
     * Create a {@link Mapping} from the given class.
     * @param clazz the class to create a mapping for
     * @return the mapping
     */
    public static Mapping createMappingForType(Class clazz) {
        Mapping mapping = new Mapping();
        mapping.setMappingName(clazz.getName());

        // loop over each property and create the proper mapping
        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(clazz);
        for (PropertyDescriptor des : descriptors) {
            MappingProperty prop = createMappingPropertyFromPropertyDescriptor(des);
            if (prop!=null) {
                mapping.getProperties().add(prop);
            }
        }

        return mapping;
    }

    /**
     * Convenience method for building a list of {@link MappingProperty}s from an array of {@link PropertyDescriptor}s.
     * @param descriptors the property descriptors to create mapping properties from
     * @return a list of mapping properties
     */
    public static List<MappingProperty> createMappingPropertiesFromPropertyDescriptors(PropertyDescriptor... descriptors) {
        List<MappingProperty> retList = new ArrayList<MappingProperty>();
        for (PropertyDescriptor des : descriptors) {
            MappingProperty prop = createMappingPropertyFromPropertyDescriptor(des);
            if (prop!=null) {
                retList.add(prop);
            }
        }

        return retList;
    }

    /**
     * Create a {@link MappingProperty} from a given {@link PropertyDescriptor}, only if the {@link ElasticSearchProperty} annotation
     * is present, or this is a subproperty situation where mapping information may be important.
     * @param descriptor the property descriptor to use
     * @return a mapping property
     */
    public static MappingProperty createMappingPropertyFromPropertyDescriptor(PropertyDescriptor descriptor) {
        // create the mapping property
        MappingProperty property = new MappingProperty();
        property.setName(descriptor.getName());

        /* To process the PropertyDescriptor we need to do two things:
         * 1. process the PropertyDescriptors present on the type defined by this PropertyDescriptor
         * 2. ElasticSearchProperty annotation is explicitly defined on this PropertyDescriptor
         */
        ElasticSearchProperty esProp = JavaBeanUtil.getAnnotationFromPropertyDescriptor(ElasticSearchProperty.class, descriptor);
        if (esProp==null) {
            // exit if there is no read method to use for property inspection
            if (descriptor.getReadMethod()==null) {
                return null;
            }
            final Type type = descriptor.getReadMethod().getGenericReturnType();
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;
                boolean hasSubProperties = false;
                for (Type atp : pt.getActualTypeArguments()) {
                    if (atp instanceof Class) {
                        List<PropertyDescriptor> des = JavaBeanUtil.getPropertyDescriptorsForAnnotatedMethods((Class)atp, ElasticSearchProperty.class, false);
                        List<MappingProperty> props = createMappingPropertiesFromPropertyDescriptors(des.toArray(new PropertyDescriptor[0]));
                        if (!props.isEmpty()) {
                            property.setFields(props);
                            hasSubProperties = true;
                        }
                    }
                }
                // exit properly
                if (!hasSubProperties) {
                    return null;
                } else {
                    return property;
                }
            } else {
                return null;
            }
        }

        // handle an explicit ElasticSearchProperty being set
        property.setType(esProp.type().getElasticSearchMappingPropertyType());
        // handle a multi_field situation
        if (esProp.filterable() && esProp.freeTextSearchable()) {
            property.setType(ElasticSearchPropertyType.MULTI_FIELD.getElasticSearchMappingPropertyType());
            MappingProperty notAnalyzedProperty = new MappingProperty();
            notAnalyzedProperty.setType(esProp.type().getElasticSearchMappingPropertyType());
            notAnalyzedProperty.setIndex("not_analyzed"); // TODO: make better
            notAnalyzedProperty.setName(descriptor.getName());
            MappingProperty queryableProperty = new MappingProperty();
            queryableProperty.setType(esProp.type().getElasticSearchMappingPropertyType());
            queryableProperty.setIndex("analyzed"); // TODO: make better
            queryableProperty.setName("queryable");
            List<MappingProperty> properties = new ArrayList<MappingProperty>(2);
            properties.add(notAnalyzedProperty);
            properties.add(queryableProperty);
            property.setFields(properties);
            return property;
        } else if (esProp.filterable()) {
            property.setType(esProp.type().getElasticSearchMappingPropertyType());
            property.setIndex("not_analyzed"); // TODO: make better
            return property;
        } else if (esProp.freeTextSearchable()) {
            property.setType(esProp.type().getElasticSearchMappingPropertyType());
            property.setIndex("analyzed"); // TODO: make better
            return property;
        }

        // no ElasticSearchProperty present
        property.setIndex("not_analyzed"); // TODO: make better
        return property;
    }

    /**
     * Return the JSON String representation of an elastic search mapping.
     * @param m the mapping to convert
     * @return the JSON string
     */
    public static String getJSONStringFromMapping(Mapping m)
            throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode retMapping = mapper.createObjectNode();
        ObjectNode mapping = retMapping.putObject(m.getMappingName());

        // exit early if there are no properties to work with
        if (m.getProperties()==null || m.getProperties().isEmpty()) {
            return retMapping.toString();
        }

        // build the properties
        ObjectNode properties = mapping.putObject("properties");
        for (MappingProperty prop : m.getProperties()) {
            ObjectNode property = properties.putObject(prop.getName());
            mergeMappingPropertyWithObjectNode(property, prop);
        }

        return mapper.writeValueAsString(retMapping);
    }

    /**
     * Merges the contents of a {@link MappingProperty} with an {@link ObjectNode}.
     * @param node the node with existing content
     * @param prop the mapping property that contains the content we want
     */
    public static void mergeMappingPropertyWithObjectNode(ObjectNode node, MappingProperty prop) {
        // handle index property
        if (StringUtils.isNotEmpty(prop.getIndex())) {
            node.put("index", prop.getIndex());
        }

        // handle type property
        if (StringUtils.isNotEmpty(prop.getType())) {
            node.put("type", prop.getType());
        }

        // handle multi-field properties properly
        if (StringUtils.isNotEmpty(prop.getType()) && prop.getType().equalsIgnoreCase("multi_field")) {
            if (prop.getFields()!=null && !prop.getFields().isEmpty()) {
                ObjectNode multifieldNode = node.putObject("fields");
                for (MappingProperty subProp : prop.getFields()) {
                    ObjectNode subPropNode = multifieldNode.putObject(subProp.getName());
                    mergeMappingPropertyWithObjectNode(subPropNode, subProp);
                }
            }
        } else {
            // handle sub fields properly
            if (prop.getFields()!=null && !prop.getFields().isEmpty()) {
                ObjectNode multifieldNode = node.putObject("properties");
                for (MappingProperty subProp : prop.getFields()) {
                    ObjectNode subPropNode = multifieldNode.putObject(subProp.getName());
                    mergeMappingPropertyWithObjectNode(subPropNode, subProp);
                }
            }
        }
    }
}
