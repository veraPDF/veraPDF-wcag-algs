package org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection;

import java.util.Arrays;
import java.util.List;

public class KoreanLettersListLabelsDetectionAlgorithm extends LettersListLabelsDetectionAlgorithm {

    protected static final List<Character> letters = Arrays.asList('가', '나', '다', '라', '마', '바', '사', '아', '자', '차', 
            '카', '타', '파', '하', '거', '너', '더', '러', '머', '버', '서', '어', '저', '처', '커', '터', '퍼', '허');

    private static final String KOREAN_LETTER_REGEX = "[가나다라마바사아자차카타파하거너더러머버서어저처커터퍼허]+";
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
