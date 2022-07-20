package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.verapdf.wcag.algorithms.entities.lists.ListInterval;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ListLabelsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class ListLabelsTests {

    static Stream<Arguments> labelsListTestParams() {
        return Stream.of(
                Arguments.of(new String[]{"1A", "1B", "2C"}, false),
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
                Arguments.of(new String[]{"III)", "IV)", "V)", "VI)", "VII)", "VIII)", "IX)", "X)"}, true),
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
                Arguments.of(new String[]{"AAB", "BBB"}, false),
                Arguments.of(new String[]{"iv", "v"}, true),
                Arguments.of(new String[]{"-XXI-", "-XXII-"}, true),
                Arguments.of(new String[]{"XIV", "XV"}, true),
                Arguments.of(new String[]{"10", "20", "30"}, false),
                Arguments.of(new String[]{"19", "110"}, false),
                Arguments.of(new String[]{"CA", "CB", "CC"}, true)
                );
    }

    @ParameterizedTest(name = "{index}: ({0},{1}) => {0}")
    @MethodSource("labelsListTestParams")
    void testLabelsList(String[] labelsList, boolean result) {
        testLabelsList(Arrays.asList(labelsList), result);
    }

    void testLabelsList(List<String> labelsList, boolean result) {
        Assertions.assertEquals(result, ListLabelsUtils.isListLabels(labelsList));
    }

    @Test
    void testLabelsList() {
        List<String> labelsList = new ArrayList<>();
        for (int i = 9; i < 100; i++) {
            labelsList.add("0" + i);
        }
        labelsList.add("100");
        testLabelsList(labelsList, false);

        labelsList.set(0, "009");
        testLabelsList(labelsList, true);
    }

    static Stream<Arguments> itemsListTestParams() {
        return Stream.of(
                Arguments.of(new String[]{"-1-", "-2-", "-2-"}, new ListInterval[]{new ListInterval(0, 2)}),
                Arguments.of(new String[]{"AZ", "BAA"}, new ListInterval[]{}),
                Arguments.of(new String[]{"1", "23"}, new ListInterval[]{}),
                Arguments.of(new String[]{"AZ", "BA"}, new ListInterval[]{new ListInterval(0, 1)}),
                Arguments.of(new String[]{"ZZ", "AAA"}, new ListInterval[]{new ListInterval(0, 1)}),
                Arguments.of(new String[]{"AAZ", "ABA"}, new ListInterval[]{new ListInterval(0, 1)}),
                Arguments.of(new String[]{" 1", "2", "3"}, new ListInterval[]{new ListInterval(1, 2)}),
                Arguments.of(new String[]{" ", " ", " ", " "}, new ListInterval[]{}),
                Arguments.of(new String[]{"i", "ii", "iii", "iv"}, new ListInterval[]{new ListInterval(0, 3)}),
                Arguments.of(new String[]{"ii", "iii"}, new ListInterval[]{new ListInterval(0, 1)}),
                Arguments.of(new String[]{"i", "ii", "iii", "iv", "v"}, new ListInterval[]{new ListInterval(0, 4)}),
                Arguments.of(new String[]{"i", "ii", "iii", "iiii"}, new ListInterval[]{new ListInterval(0, 2)}),
                Arguments.of(new String[]{"iiii", "v"}, new ListInterval[]{}),
                Arguments.of(new String[]{"-i-", "-ii-", "-iii-"}, new ListInterval[]{new ListInterval(0, 2)}),
                Arguments.of(new String[]{"--", "-i-", "-ii-"}, new ListInterval[]{new ListInterval(0, 2)}),
                Arguments.of(new String[]{"1", "2"}, new ListInterval[]{new ListInterval(0, 1)}),
                Arguments.of(new String[]{"0", "1", "2"}, new ListInterval[]{new ListInterval(0, 2)}),
                Arguments.of(new String[]{"00", "01", "02"}, new ListInterval[]{new ListInterval(0, 2)}),
                Arguments.of(new String[]{"1", "1"}, new ListInterval[]{}),
                Arguments.of(new String[]{"09", "10"}, new ListInterval[]{new ListInterval(0, 1)}),
                Arguments.of(new String[]{"Aa", "Bb"}, new ListInterval[]{}),
                Arguments.of(new String[]{"i", "II"}, new ListInterval[]{}),
                Arguments.of(new String[]{"i", "Ii"}, new ListInterval[]{}),
                Arguments.of(new String[]{"xlix", "l"}, new ListInterval[]{new ListInterval(0, 1)}),
                Arguments.of(new String[]{"08", "009", "10"}, new ListInterval[]{}),
                Arguments.of(new String[]{"005", "006", "07"}, new ListInterval[]{new ListInterval(0, 1)}),
                Arguments.of(new String[]{"005", "006", "7"}, new ListInterval[]{new ListInterval(0, 1)}),
                Arguments.of(new String[]{"008", "009", "10"}, new ListInterval[]{new ListInterval(0, 1)}),
                Arguments.of(new String[]{"08", "09", "010"}, new ListInterval[]{new ListInterval(0, 2)}),
                Arguments.of(new String[]{"8", "9", "010"}, new ListInterval[]{new ListInterval(0, 1)}),
                Arguments.of(new String[]{"008", "009", "010"}, new ListInterval[]{new ListInterval(0, 2)}),
                Arguments.of(new String[]{"8", "9", "10"}, new ListInterval[]{new ListInterval(0, 2)}),
                Arguments.of(new String[]{"08", "09", "10"}, new ListInterval[]{new ListInterval(0, 2)}),
                Arguments.of(new String[]{"1.1", "2.1", "3.1"}, new ListInterval[]{new ListInterval(0, 2)}),
                Arguments.of(new String[]{"1.1", "1.2", "1.3"}, new ListInterval[]{new ListInterval(0, 2)}),
                Arguments.of(new String[]{"-1", "0", "1"}, new ListInterval[]{new ListInterval(1, 2)}),
                Arguments.of(new String[]{"-1-", "-2-", "-3-"}, new ListInterval[]{new ListInterval(0, 2)}),
                Arguments.of(new String[]{"-2-", "-3-", "-4-"}, new ListInterval[]{new ListInterval(0, 2)}),
                Arguments.of(new String[]{"III)", "IV)", "V)", "VI)", "VII)", "VIII)", "IX)", "X)"}, new ListInterval[]{new ListInterval(0, 7)}),
                Arguments.of(new String[]{"-9-", "-10-", "-11-"}, new ListInterval[]{new ListInterval(0, 2)}),
                Arguments.of(new String[]{"-a-", "-a-", "-a-"}, new ListInterval[]{new ListInterval(0, 2)}),
                Arguments.of(new String[]{"--", "--", "--"}, new ListInterval[]{new ListInterval(0, 2)}),
                Arguments.of(new String[]{"CHAPTER 1", "CHAPTER 2", "CHAPTER 3"}, new ListInterval[]{new ListInterval(0, 2)}),
                Arguments.of(new String[]{"CHAPTER I", "CHAPTER II", "CHAPTER III"}, new ListInterval[]{new ListInterval(0, 2)}),
                Arguments.of(new String[]{"(A).", "(B).", "(C)."}, new ListInterval[]{new ListInterval(0, 2)}),
                Arguments.of(new String[]{"a", "B"}, new ListInterval[]{}),
                Arguments.of(new String[]{"a.", "b.", "c."}, new ListInterval[]{new ListInterval(0, 2)}),
                Arguments.of(new String[]{"Z", "AA", "BB"}, new ListInterval[]{new ListInterval(0, 2)}),
                Arguments.of(new String[]{"AAB", "BBB"}, new ListInterval[]{}),
                Arguments.of(new String[]{"iv", "v"}, new ListInterval[]{new ListInterval(0, 1)}),
                Arguments.of(new String[]{"-XXI-", "-XXII-"}, new ListInterval[]{new ListInterval(0, 1)}),
                Arguments.of(new String[]{"XIV", "XV"}, new ListInterval[]{new ListInterval(0, 1)}),
                Arguments.of(new String[]{"alo", "ali"}, new ListInterval[]{}),
                Arguments.of(new String[]{"vo", "vi"}, new ListInterval[]{}),
                Arguments.of(new String[]{"10", "20", "30"}, new ListInterval[]{}),
                Arguments.of(new String[]{"19", "110"}, new ListInterval[]{}),
                Arguments.of(new String[]{"CA", "CB", "CC"}, new ListInterval[]{new ListInterval(0, 2)})
                );
    }

    @ParameterizedTest(name = "{index}: ({0},{1}) => {0}")
    @MethodSource("itemsListTestParams")
    void testItemsList(String[] itemsList, ListInterval[] listIntervalsList) {
        testItemsList(Arrays.asList(itemsList), listIntervalsList);
    }

    void testItemsList(List<String> itemsList, ListInterval[] listIntervalsList) {
        Set<ListInterval> listIntervals = ListLabelsUtils.getListItemsIntervals(itemsList);
        Assertions.assertEquals(listIntervalsList.length, listIntervals.size());
        for (ListInterval listInterval : listIntervalsList) {
            Assertions.assertTrue(listIntervals.contains(listInterval));
        }
    }

    @Test
    void testItemsList() {
        List<String> itemsList = new ArrayList<>();
        for (int i = 9; i < 100; i++) {
            itemsList.add("0" + i);
        }
        itemsList.add("100");
        testItemsList(itemsList, new ListInterval[]{new ListInterval(0, 90), new ListInterval(90, 91)});

        itemsList.set(0, "009");
        testItemsList(itemsList, new ListInterval[]{new ListInterval(0, 91)});
    }

}
