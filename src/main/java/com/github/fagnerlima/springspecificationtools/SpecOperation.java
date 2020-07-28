package com.github.fagnerlima.springspecificationtools;

/**
 * @author Fagner Lima
 * @since 0.1.0
 */
public enum SpecOperation {

    EQUAL, EQUAL_IGNORE_CASE,
    LIKE, LIKE_IGNORE_CASE,
    GREATER_THAN, GREATER_THAN_OR_EQUAL,
    LESS_THAN, LESS_THAN_OR_EQUAL,
    DATETIME_TO_DATE,

    /** For PostgreSQL (require unaccent extension) */
    EQUAL_IGNORE_CASE_UNACCENT,

    /** For PostgreSQL (require unaccent extension) */
    LIKE_IGNORE_CASE_UNACCENT,

}
