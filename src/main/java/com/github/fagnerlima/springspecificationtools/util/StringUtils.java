package com.github.fagnerlima.springspecificationtools.util;

public class StringUtils {

    /**
     * Remove os acentos de uma string.
     *
     * @param str string a ser manipulada
     * @return string sem acentos
     */
    public static String unaccent(String str) {
        return org.apache.commons.lang3.StringUtils.stripAccents(str);
    }

    /**
     * Verifica se a string é vazia ou nula.
     *
     * @param str string a ser verificada
     * @return {@code true} se a string for vazia ou nula
     */
    public static Boolean isEmpty(String str) {
        return org.apache.commons.lang3.StringUtils.isEmpty(str);
    }

    /**
     * Verifica se a string é vazia, nula ou se contém apenas espaços em branco.
     *
     * @param str string a ser verificada
     * @return {@code true} se a string for vazia, nula ou se contiver apenas espaços em branco
     */
    public static Boolean isBlank(String str) {
        return org.apache.commons.lang3.StringUtils.isBlank(str);
    }

}
