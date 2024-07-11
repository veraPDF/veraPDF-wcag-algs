package org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection;

import java.util.Arrays;
import java.util.List;

public class KoreanLettersListLabelsDetectionAlgorithm extends LettersListLabelsDetectionAlgorithm {

    protected static final List<Character> letters = Arrays.asList('가', '나', '다', '라', '마', '바', '사', '아', '자', '차', '카', '타', '파', '하');

    private static final String UPPER_CASE_KOREAN_LETTER_REGEX = "[가나다라마바사아자차카타파하]+";
    private static final String LOWER_CASE_KOREAN_LETTER_REGEX = "[가나다라마바사아자차카타파하]+";
    private static final String KOREAN_LETTER_REGEX = "[가나다라마바사아자차카타파하]+";

    @Override
    protected String getRegex() {
        return KOREAN_LETTER_REGEX;
    }

    @Override
    protected String getLowerCaseRegex() {
        return LOWER_CASE_KOREAN_LETTER_REGEX;
    }

    @Override
    protected String getUpperCaseRegex() {
        return UPPER_CASE_KOREAN_LETTER_REGEX;
    }

    @Override
    protected String getStringFromNumber(Integer number) {
        return getLettersFromNumber(number);
    }

    @Override
    protected Integer getNumberFromString(String string) {
        return getNumberFromLetters(string);
    }

    private static String getLettersFromNumber(int integer) {
        integer--;
        if (integer < letters.size()) {
            return letters.get(integer).toString();
        }
        return null;
    }

    private static Integer getNumberFromLetters(String s) {
        if (s.length() != 1) {
            return null;
        }
        int num = letters.indexOf(s.charAt(0));
        if (num < 0) {
            return null;
        }
        return num + 1;
    }
}
