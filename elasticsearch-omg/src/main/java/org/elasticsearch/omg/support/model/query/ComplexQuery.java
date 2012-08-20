package org.elasticsearch.omg.support.model.query;

import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.sort.SortBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * This class helps manage a more complex query.
 */
public class ComplexQuery {

    private List<String> classNames = new ArrayList<String>();
    private BoolFilterBuilder boolFilterBuilder;
    private BoolQueryBuilder boolQueryBuilder;
    private SortBuilder sortBuilder;

    public BoolQueryBuilder getBoolQueryBuilder() {
        return boolQueryBuilder;
    }

    public void setBoolQueryBuilder(BoolQueryBuilder boolQueryBuilder) {
        this.boolQueryBuilder = boolQueryBuilder;
    }

    public List<String> getClassNames() {
        return classNames;
    }

    public void setClassNames(List<String> classNames) {
        this.classNames = classNames;
    }

    public BoolFilterBuilder getBoolFilterBuilder() {
        return boolFilterBuilder;
    }

    public void setBoolFilterBuilder(BoolFilterBuilder boolFilterBuilder) {
        this.boolFilterBuilder = boolFilterBuilder;
    }

    public SortBuilder getSortBuilder() {
        return sortBuilder;
    }

    public void setSortBuilder(SortBuilder sortBuilder) {
        this.sortBuilder = sortBuilder;
    }

}

