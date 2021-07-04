package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ListLabelsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ListLabelsTests {

    static Stream<Arguments> listTestParams() {
        return Stream.of(
                Arguments.of(new String[]{" 1", "2", "3"}, true),
                Arguments.of(new String[]{" ", " ", " ", " "}, false),
                Arguments.of(new String[]{"", "", "", ""}, false),
                Arguments.of(new String[]{"i", "ii", "iii", "iv"}, true),
                Arguments.of(new String[]{"ii", "iii"}, true),
                Arguments.of(new String[]{"i", "ii", "iii", "iv", "v"}, true),
                Arguments.of(new String[]{"i", "ii", "iii", "iiii"}, false),
                Arguments.of(new String[]{"", "1", "2"}, false),
                Arguments.of(new String[]{"1", "", "2"}, true),
                Arguments.of(new String[]{"-1-", "-2-", "-2-"}, false),
                Arguments.of(new String[]{"-i-", "-ii-", "-iii-"}, true),
                Arguments.of(new String[]{"--", "-i-", "-ii-"}, false),
                Arguments.of(new String[]{"1", "2"}, true),
                Arguments.of(new String[]{"0", "1", "2"}, true),
                Arguments.of(new String[]{"00", "01", "02"}, true),
                Arguments.of(new String[]{"1", "1"}, true),
                Arguments.of(new String[]{"09", "10"}, true),
                Arguments.of(new String[]{"a", "B"}, false),
                Arguments.of(new String[]{"Aa", "Bb"}, false),
                Arguments.of(new String[]{"i", "II"}, false),
                Arguments.of(new String[]{"i", "Ii"}, false),
                Arguments.of(new String[]{"xlix", "l"}, true),
                Arguments.of(new String[]{"08", "009", "10"}, false),
                Arguments.of(new String[]{"005", "006", "07"}, false),
                Arguments.of(new String[]{"005", "006", "7"}, false),
                Arguments.of(new String[]{"008", "009", "10"}, false),
                Arguments.of(new String[]{"08", "09", "010"}, true),
                Arguments.of(new String[]{"8", "9", "010"}, false),
                Arguments.of(new String[]{"008", "009", "010"}, true),
                Arguments.of(new String[]{"8", "9", "10"}, true),
                Arguments.of(new String[]{"08", "09", "10"}, true),
                Arguments.of(new String[]{"1.1", "2.1", "3.1"}, true),
                Arguments.of(new String[]{"1.1", "1.2", "1.3"}, true),
                Arguments.of(new String[]{"-1", "0", "1"}, false),
                Arguments.of(new String[]{"-1-", "-2-", "-3-"}, true),
                Arguments.of(new String[]{"-2-", "-3-", "-4-"}, true),
                Arguments.of(new String[]{"-9-", "-10-", "-11-"}, true),
                Arguments.of(new String[]{"-a-", "-a-", "-a-"}, false),
                Arguments.of(new String[]{"--", "--", "--"}, false),
                Arguments.of(new String[]{"CHAPTER 1", "CHAPTER 2", "CHAPTER 3"}, true),
                Arguments.of(new String[]{"CHAPTER I", "CHAPTER II", "CHAPTER III"}, true),
                Arguments.of(new String[]{"(A).", "(B).", "(C)."}, true),
                Arguments.of(new String[]{"a.", "b.", "c."}, true),
                Arguments.of(new String[]{"Z", "AA", "BB"}, true),
                Arguments.of(new String[]{"AAB", "BBB"}, true),//fix?
                Arguments.of(new String[]{"iv", "v"}, true),
                Arguments.of(new String[]{"-XXI-", "-XXII-"}, true),
                Arguments.of(new String[]{"XIV", "XV"}, true),
                Arguments.of(new String[]{"10", "20", "30"}, false),
                Arguments.of(new String[]{"19", "110"}, false),
                Arguments.of(new String[]{"CA", "CB", "CC"}, true)
                );
    }

    @ParameterizedTest(name = "{index}: ({0},{1}) => {0}")
    @MethodSource("listTestParams")
    void testList(String[] list, boolean result) {
        Assertions.assertEquals(result, ListLabelsUtils.isListLabels(Arrays.asList(list)));
    }

    @Test
    void testList() {
        List<String> list = new ArrayList<>();
        for (int i = 9; i < 100; i++) {
            list.add("0" + i);
        }
        list.add("100");
        Assertions.assertFalse(ListLabelsUtils.isListLabels(list));

        List<String> list2 = new ArrayList<>();
        list2.add("009");
        for (int i = 10; i < 100; i++) {
            list2.add("0" + i);
        }
        list2.add("100");
        Assertions.assertTrue(ListLabelsUtils.isListLabels(list2));

    }

}
