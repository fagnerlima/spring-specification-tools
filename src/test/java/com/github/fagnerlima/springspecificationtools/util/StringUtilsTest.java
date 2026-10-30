package com.github.fagnerlima.springspecificationtools.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

class StringUtilsTest {

    @ParameterizedTest
    @CsvSource({
            "control, control",
            "éclair, eclair",
            "Revisão de código, Revisao de codigo",
            "AÇÃO à ÊXITO, ACAO a EXITO",
            "Łódź, Lodz",
            "naïve façade, naive facade"
    })
    void unaccentRemovesDiacritics(String input, String expected) {
        assertThat(StringUtils.unaccent(input)).isEqualTo(expected);
    }

    @Test
    void unaccentKeepsNullAndEmpty() {
        assertThat(StringUtils.unaccent(null)).isNull();
        assertThat(StringUtils.unaccent("")).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void isEmptyForNullOrEmpty(String value) {
        assertThat(StringUtils.isEmpty(value)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = { " ", "a" })
    void isNotEmptyForAnyCharacter(String value) {
        assertThat(StringUtils.isEmpty(value)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "\t\n" })
    void isBlankForNullEmptyOrWhitespace(String value) {
        assertThat(StringUtils.isBlank(value)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = { "a", " a " })
    void isNotBlankWithNonWhitespace(String value) {
        assertThat(StringUtils.isBlank(value)).isFalse();
    }

}
