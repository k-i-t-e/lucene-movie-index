package org.kite.movieindex.dao;

public enum IndexField
{
    NAME("name"),
    DIRECTOR("director"),
    CAST("cast"),
    YEAR("year"),
    RELEASE_DATE("release_date"),
    RATING("rating"),
    GENRE("genre");

    private String fieldName;

    IndexField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFacetField() {
        return "F_" + fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
