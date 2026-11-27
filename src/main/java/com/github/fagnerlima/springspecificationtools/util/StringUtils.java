package com.github.fagnerlima.springspecificationtools.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Utils for String
 * @author Fagner Lima
 * @since 0.1.0
 */
public class StringUtils {

    private static final Pattern COMBINING_DIACRITICAL_MARKS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    /**
     * Remove accents from {@code str}.
     *
     * <pre>
     * StringUtils.unaccent(null)      = null
     * StringUtils.unaccent("")        = ""
     * StringUtils.unaccent("control") = "control"
     * StringUtils.unaccent("éclair")  = "eclair"
     * </pre>
     *
     * @param str string to be stripped
     * @return string with accents removed
     */
    public static String unaccent(String str) {
        if (str == null) {
            return null;
        }

        // Ł and ł have no Unicode decomposition, so NFD alone would keep them (same handling as commons-lang3).
        String decomposed = Normalizer.normalize(str, Normalizer.Form.NFD)
                .replace('Ł', 'L')
                .replace('ł', 'l');

        return COMBINING_DIACRITICAL_MARKS.matcher(decomposed).replaceAll("");
    }

    /**
     * Check if the {@code str} is null or empty.
     *
     * @param str string to be checked
     * @return {@code true} if {@code str} is null or empty
     */
    public static Boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Check if the {@code str} is null, empty or whitespace only.
     *
     * @param str string to be checked
     * @return {@code true} if {@code str} is null, empty or whitespace only
     */
    public static Boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

}
