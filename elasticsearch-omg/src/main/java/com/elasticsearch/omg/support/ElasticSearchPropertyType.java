package com.elasticsearch.omg.support;

/**
 * Defines the type in an elastic search property mapping.
 * {@link http://www.elasticsearch.org/guide/reference/mapping/}.
 */
public enum ElasticSearchPropertyType {

    /* core types */
    STRING("string"),

    INTEGER("integer"),

    FLOAT("float"),

    LONG("long"),

    DOUBLE("double"),

    BOOLEAN("boolean"),

    NULL("null"),

    /* other types */
    MULTI_FIELD("multi_field");

    private String elasticSearchMappingPropertyType;

    /**
     * Construct the enum with the proper name
     * @param elasticSearchMappingPropertyType the mapping name that ES uses.
     */
    ElasticSearchPropertyType(String elasticSearchMappingPropertyType) {
        this.elasticSearchMappingPropertyType = elasticSearchMappingPropertyType;
    }

    /**
     * Get the mapping name for a given property type.
     * @return the mapping name
     */
    public String getElasticSearchMappingPropertyType() {
        return elasticSearchMappingPropertyType;
    }

    /**
     * Get the {@link ElasticSearchPropertyType} by the given mapping name.
     * @param mappingName the mapping name
     * @return the elastic search property type enum
     */
    public static ElasticSearchPropertyType getElasticSearchPropertyTypeByMappingName(String mappingName) {
        for (ElasticSearchPropertyType ty : ElasticSearchPropertyType.values()) {
            if (ty.getElasticSearchMappingPropertyType().equals(mappingName)) {
                return ty;
            }
        }
         throw new IllegalArgumentException("ElasticSearchPropertyType could not be found with a value of: " + mappingName);
    }
}

