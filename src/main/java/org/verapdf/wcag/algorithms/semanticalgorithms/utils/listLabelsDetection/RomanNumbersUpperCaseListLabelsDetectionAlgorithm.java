package org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection;

import java.util.Arrays;
import java.util.List;

public class RomanNumbersUpperCaseListLabelsDetectionAlgorithm extends LettersListLabelsDetectionAlgorithm {

    protected static final List<Character> LETTERS = Arrays.asList(
            '\u2160','\u2161','\u2162','\u2163','\u2164','\u2165','\u2166','\u2167','\u2168','\u2169','\u216A',
            '\u216B');

    private static final String SIMPLE_ROMAN_NUMBER_REGEX = "[\u2160-\u216B]+";
    private static final String UPPER_CASE_SIMPLE_ROMAN_NUMBER_REGEX = SIMPLE_ROMAN_NUMBER_REGEX;
    private static final String LOWER_CASE_SIMPLE_ROMAN_NUMBER_LETTER_REGEX = SIMPLE_ROMAN_NUMBER_REGEX;

    public RomanNumbersUpperCaseListLabelsDetectionAlgorithm() {
        super();
    }
    
    public RomanNumbersUpperCaseListLabelsDetectionAlgorithm(int increment) {
        super(increment);
    }

    @Override
    protected String getNumberingStyle(boolean isUpperCase) {
        return NumberingStyleNames.ROMAN_NUMBERS_UPPER_CASE;
    }

    @Override
    protected String getRegex() {
        return SIMPLE_ROMAN_NUMBER_REGEX;
    }

    @Override
    protected String getLowerCaseRegex() {
        return LOWER_CASE_SIMPLE_ROMAN_NUMBER_LETTER_REGEX;
    }

    @Override
    protected List<Character> getLetters() {
        return LETTERS;
    }

    @Override
    protected String getUpperCaseRegex() {
        return UPPER_CASE_SIMPLE_ROMAN_NUMBER_REGEX;
    }
}
