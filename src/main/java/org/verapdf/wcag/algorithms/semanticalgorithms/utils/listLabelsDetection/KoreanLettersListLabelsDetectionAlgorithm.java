/*
 * This file is part of veraPDF wcag algorithms, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF wcag algorithms is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF wcag algorithms as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF wcag algorithms as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection;

import java.util.Arrays;
import java.util.List;

public class KoreanLettersListLabelsDetectionAlgorithm extends LettersListLabelsDetectionAlgorithm {

    protected static final List<Character> LETTERS = Arrays.asList('가', '나', '다', '라', '마', '바', '사', '아', '자', '차',
            '카', '타', '파', '하', '거', '너', '더', '러', '머', '버', '서', '어', '저', '처', '커', '터', '퍼', '허');

    private static final String KOREAN_LETTER_REGEX = "[가나다라마바사아자차카타파하거너더러머버서어저처커터퍼허]+";
    private static final String UPPER_CASE_KOREAN_LETTER_REGEX = KOREAN_LETTER_REGEX;
    private static final String LOWER_CASE_KOREAN_LETTER_REGEX = KOREAN_LETTER_REGEX;

    public KoreanLettersListLabelsDetectionAlgorithm() {
        super();
    }
    
    public KoreanLettersListLabelsDetectionAlgorithm(int increment) {
        super(increment);
    }

    @Override
    protected String getNumberingStyle(boolean isUpperCase) {
        return NumberingStyleNames.KOREAN_LETTERS;
    }

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
        return LETTERS;
    }

    @Override
    protected String getUpperCaseRegex() {
        return UPPER_CASE_KOREAN_LETTER_REGEX;
    }
}
