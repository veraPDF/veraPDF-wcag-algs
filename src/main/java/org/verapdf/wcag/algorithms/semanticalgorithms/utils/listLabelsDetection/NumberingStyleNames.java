package org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection;

import java.util.HashMap;
import java.util.Map;

public class NumberingStyleNames {
    public static final String ENGLISH_LETTERS_UPPER_CASE = "english letters in upper case";
    public static final String ENGLISH_LETTERS_LOWER_CASE = "english letters in lower case";
    public static final String ROMAN_NUMBERS_LOWER_CASE = "roman numbers in lower case";
    public static final String ROMAN_NUMBERS = "roman numbers";
    public static final String ROMAN_NUMBERS_UPPER_CASE = "roman numbers in upper case";
    public static final String KOREAN_LETTERS = "korean letters";
    public static final String ARABIC_NUMBERS = "arabic numbers";
    public static final String CIRCLED_ARABIC_NUMBERS = "circled arabic numbers";
    public static final String UNORDERED = "unordered";
    public static final String UNKNOWN = "unknown style";


    private static final Map<String, ListLabelsDetectionAlgorithm> map = new HashMap<>();

    static {
        map.put(ARABIC_NUMBERS, new ArabicNumbersListLabelsDetectionAlgorithm());
        map.put(CIRCLED_ARABIC_NUMBERS, new CircledArabicNumbersListLabelsDetectionAlgorithm());
        map.put(ENGLISH_LETTERS_UPPER_CASE, new AlfaLettersListLabelsDetectionAlgorithm1());//upperCase and lower case
        map.put(KOREAN_LETTERS, new KoreanLettersListLabelsDetectionAlgorithm());
        map.put(ROMAN_NUMBERS_UPPER_CASE, new RomanNumbersUpperCaseListLabelsDetectionAlgorithm());
        map.put(ROMAN_NUMBERS_LOWER_CASE, new RomanNumbersLowerCaseListLabelsDetectionAlgorithm());
        map.put(ROMAN_NUMBERS, new RomanNumbersListLabelsDetectionAlgorithm());
    }

    public static ListLabelsDetectionAlgorithm getAlgo(String numberingStyle) {
        return map.get(numberingStyle);
    }

    public static Map<String, ListLabelsDetectionAlgorithm> getMap() {
        return map;
    }
}
