package org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection;

import java.util.Arrays;
import java.util.List;

public class CircledArabicNumbersListLabelsDetectionAlgorithm extends LettersListLabelsDetectionAlgorithm {

    protected static final List<Character> letters = Arrays.asList(
            '\u24EA','\u2460','\u2461','\u2462','\u2463','\u2464','\u2465','\u2466','\u2467','\u2468',
            '\u2469','\u246A','\u246B','\u246C','\u246D','\u246E','\u246F','\u2470','\u2471','\u2472',
            '\u2473','\u3251','\u3252','\u3253','\u3254','\u3255','\u3256','\u3257','\u3258','\u3259',
            '\u325A','\u325B','\u325C','\u325D','\u325E','\u325F','\u32B1','\u32B2','\u32B3','\u32B4',
            '\u32B5','\u32B6','\u32B7','\u32B8','\u32B9','\u32BA','\u32BB','\u32BC','\u32BD','\u32BE',
            '\u32BF');

    private static final String KOREAN_LETTER_REGEX = "[\u24EA\u2460-\u2473\u3251-\u325F\u32B1-\u32BF]+";
    private static final String UPPER_CASE_KOREAN_LETTER_REGEX = KOREAN_LETTER_REGEX;
    private static final String LOWER_CASE_KOREAN_LETTER_REGEX = KOREAN_LETTER_REGEX;

    @Override
    protected String getRegex() {
        return KOREAN_LETTER_REGEX;
    }

    @Override
    protected String getLowerCaseRegex() {
        return LOWER_CASE_KOREAN_LETTER_REGEX;
    }

    @Override
    protected List<Character> getLetters() {
        return letters;
    }

    @Override
    protected String getUpperCaseRegex() {
        return UPPER_CASE_KOREAN_LETTER_REGEX;
    }
}
