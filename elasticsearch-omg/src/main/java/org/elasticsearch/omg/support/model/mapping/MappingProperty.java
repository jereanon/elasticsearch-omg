package org.elasticsearch.omg.support.model.mapping;

import java.util.List;

/**
 * Defines a property associated with an elastic search {@link Mapping}.
 */
public class MappingProperty {

    private String name;
    private String type;
    private String index;
    private List<MappingProperty> fields;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public List<MappingProperty> getFields() {
        return fields;
    }

    public void setFields(List<MappingProperty> fields) {
        this.fields = fields;
    }
}
