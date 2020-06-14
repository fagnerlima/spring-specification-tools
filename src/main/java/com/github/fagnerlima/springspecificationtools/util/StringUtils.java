package com.github.fagnerlima.springspecificationtools.util;

/**
 * Utils for String
 * @author Fagner Lima
 * @since 0.1.0
 */
public class StringUtils {

    /**
     * Remove accents from {@code str}.
     *
     * <pre>
     * StringUtils.stripAccents(null)      = null
     * StringUtils.stripAccents("")        = ""
     * StringUtils.stripAccents("control") = "control"
     * StringUtils.stripAccents("éclair")  = "eclair"
     * </pre>
     *
     * @param str string to be stripped
     * @return string with accents removed
     */
    public static String unaccent(String str) {
        return org.apache.commons.lang3.StringUtils.stripAccents(str);
    }

    /**
     * Check if the {@code str} is null or empty.
     *
     * @param str string to be checked
     * @return {@code true} if {@code str} is null or empty
     */
    public static Boolean isEmpty(String str) {
        return org.apache.commons.lang3.StringUtils.isEmpty(str);
    }

    /**
     * Check if the {@code str} is null, emtpty or whitespace only.
     *
     * @param str string to be checked
     * @return {@code true} if {@code str} is null, empty or whitespace only
     */
    public static Boolean isBlank(String str) {
        return org.apache.commons.lang3.StringUtils.isBlank(str);
    }

}
