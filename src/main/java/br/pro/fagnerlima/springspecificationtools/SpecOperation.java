package br.pro.fagnerlima.springspecificationtools;

public enum SpecOperation {

    EQUAL, EQUAL_IGNORE_CASE,
    LIKE, LIKE_IGNORE_CASE,
    GREATER_THAN, GREATER_THAN_OR_EQUAL,
    LESS_THAN, LESS_THAN_OR_EQUAL,
    DATETIME_TO_DATE,

    /** For PostgreSQL */
    EQUALS_IGNORE_CASE_UNACCENT,

    /** For PostgreSQL */
    LIKE_IGNORE_CASE_UNACCENT,

}
