package org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection;

import java.util.Arrays;
import java.util.List;

public abstract class AlfaLettersListLabelsDetectionAlgorithm extends LettersListLabelsDetectionAlgorithm {

    protected static final List<Character> letters = Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');

    private static final String UPPER_CASE_ENGLISH_LETTER_REGEX = "[A-Z]+";
    private static final String LOWER_CASE_ENGLISH_LETTER_REGEX = "[a-z]+";
    private static final String ENGLISH_LETTER_REGEX = "[A-Za-z]+";

    @Override
    protected String getRegex() {
        return ENGLISH_LETTER_REGEX;
    }

    @Override
    protected String getLowerCaseRegex() {
        return LOWER_CASE_ENGLISH_LETTER_REGEX;
    }

    @Override
    protected String getUpperCaseRegex() {
        return UPPER_CASE_ENGLISH_LETTER_REGEX;
    }

    @Override
    protected List<Character> getLetters() {
        return letters;
    }
}
