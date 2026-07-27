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
import java.util.regex.Pattern;

public class RomanNumbersUpperCaseListLabelsDetectionAlgorithm extends LettersListLabelsDetectionAlgorithm {

    protected static final List<Character> LETTERS = Arrays.asList(
            '\u2160','\u2161','\u2162','\u2163','\u2164','\u2165','\u2166','\u2167','\u2168','\u2169','\u216A',
            '\u216B');

    private static final String SIMPLE_ROMAN_NUMBER_REGEX = "[\u2160-\u216B]+";
    private static final Pattern SIMPLE_ROMAN_NUMBER_REGEX_PATTERN = Pattern.compile(SIMPLE_ROMAN_NUMBER_REGEX);

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
    protected Pattern getRegexPattern() {
        return SIMPLE_ROMAN_NUMBER_REGEX_PATTERN;
    }

    @Override
    protected Pattern getLowerCaseRegexPattern() {
        return SIMPLE_ROMAN_NUMBER_REGEX_PATTERN;
    }

    @Override
    protected List<Character> getLetters() {
        return LETTERS;
    }

    @Override
    protected Pattern getUpperCaseRegexPattern() {
        return SIMPLE_ROMAN_NUMBER_REGEX_PATTERN;
    }
}
