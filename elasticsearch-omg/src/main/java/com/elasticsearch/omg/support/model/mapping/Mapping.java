package com.elasticsearch.omg.support.model.mapping;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a mapping for an Elastic Search document.
 */
public class Mapping {

    private String mappingName;
    private List<MappingProperty> properties = new ArrayList<MappingProperty>();

    public String getMappingName() {
        return mappingName;
    }

    public void setMappingName(String mappingName) {
        this.mappingName = mappingName;
    }

    public List<MappingProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<MappingProperty> properties) {
        this.properties = properties;
    }
}
