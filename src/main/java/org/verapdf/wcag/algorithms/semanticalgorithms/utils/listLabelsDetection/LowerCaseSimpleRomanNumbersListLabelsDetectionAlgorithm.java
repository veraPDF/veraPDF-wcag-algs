package org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection;

import java.util.Arrays;
import java.util.List;

public class LowerCaseSimpleRomanNumbersListLabelsDetectionAlgorithm extends LettersListLabelsDetectionAlgorithm {

    protected static final List<Character> LETTERS = Arrays.asList(
            '\u2170','\u2171','\u2172','\u2173','\u2174','\u2175','\u2176','\u2177','\u2178','\u2179','\u217A',
            '\u217B');

    private static final String SIMPLE_ROMAN_NUMBER_REGEX = "[\u2170-\u217B]+";
    private static final String UPPER_CASE_SIMPLE_ROMAN_NUMBER_REGEX = SIMPLE_ROMAN_NUMBER_REGEX;
    private static final String LOWER_CASE_SIMPLE_ROMAN_NUMBER_LETTER_REGEX = SIMPLE_ROMAN_NUMBER_REGEX;

    public LowerCaseSimpleRomanNumbersListLabelsDetectionAlgorithm() {
        super();
    }

    public LowerCaseSimpleRomanNumbersListLabelsDetectionAlgorithm(int increment) {
        super(increment);
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
