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

import java.util.HashMap;
import java.util.Map;

public class NumberingStyleNames {
    public static final String ENGLISH_LETTERS = "english letters";
    public static final String ENGLISH_LETTERS_UPPER_CASE = "english letters in upper case";
    public static final String ENGLISH_LETTERS_LOWER_CASE = "english letters in lower case";
    public static final String ROMAN_NUMBERS_LOWER_CASE = "roman numbers in lower case";
    public static final String ROMAN_NUMBERS = "roman numbers";
    public static final String ROMAN_NUMBERS_UPPER_CASE = "roman numbers in upper case";
    public static final String KOREAN_LETTERS = "korean letters";
    public static final String ARABIC_NUMBERS = "arabic numbers";
    public static final String CIRCLED_ARABIC_NUMBERS = "circled arabic numbers";
    public static final String UNORDERED = "unordered";
    public static final String ORDERED = "ordered";
    public static final String DESCRIPTION = "description";
    public static final String UNKNOWN = "unknown style";


    private static final Map<String, ListLabelsDetectionAlgorithm> map = new HashMap<>();

    static {
        map.put(ARABIC_NUMBERS, new ArabicNumbersListLabelsDetectionAlgorithm());
        map.put(CIRCLED_ARABIC_NUMBERS, new CircledArabicNumbersListLabelsDetectionAlgorithm());
        map.put(ENGLISH_LETTERS, new AlfaLettersListLabelsDetectionAlgorithm1());//upperCase and lower case
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
