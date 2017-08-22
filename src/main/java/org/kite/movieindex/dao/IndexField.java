package org.kite.movieindex.dao;

import org.apache.lucene.search.SortField;

public enum IndexField
{
    NAME("name", SortField.Type.STRING),
    DIRECTOR("director", SortField.Type.STRING),
    CAST("cast", SortField.Type.STRING),
    YEAR("year", SortField.Type.STRING),
    RELEASE_DATE("release_date", SortField.Type.LONG),
    RATING("rating", SortField.Type.DOUBLE),
    GENRE("genre", SortField.Type.STRING);

    private String fieldName;
    private SortField.Type type;

    IndexField(String fieldName, SortField.Type type) {
        this.fieldName = fieldName;
        this.type = type;
    }

    public String getFacetField() {
        return "F_" + fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public SortField.Type getType() {
        return type;
    }
}
