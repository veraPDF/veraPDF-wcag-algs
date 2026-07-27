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

import java.util.regex.Pattern;

public class RomanNumbersListLabelsDetectionAlgorithm extends LettersListLabelsDetectionAlgorithm {

    private static final String ROMAN_NUMBER_REGEX = "[MDCLXVImdclxvi]*";
    protected static final Pattern ROMAN_NUMBER_REGEX_PATTERN = Pattern.compile(ROMAN_NUMBER_REGEX);
    private static final String UPPER_CASE_ROMAN_NUMBER_REGEX = "[MDCLXVI]+";
    private static final Pattern UPPER_CASE_ROMAN_NUMBER_REGEX_PATTERN = Pattern.compile(UPPER_CASE_ROMAN_NUMBER_REGEX);
    private static final String LOWER_CASE_ROMAN_NUMBER_REGEX = "[mdclxvi]+";
    private static final Pattern LOWER_CASE_ROMAN_NUMBER_REGEX_PATTERN = Pattern.compile(LOWER_CASE_ROMAN_NUMBER_REGEX);

    public RomanNumbersListLabelsDetectionAlgorithm() {
    }

    public RomanNumbersListLabelsDetectionAlgorithm(int increment) {
        setIncrement(increment);
    }

    @Override
    protected String getNumberingStyle(boolean isUpperCase) {
        return isUpperCase ? NumberingStyleNames.ROMAN_NUMBERS_UPPER_CASE : NumberingStyleNames.ROMAN_NUMBERS_LOWER_CASE;
    }

    @Override
    protected Pattern getRegexPattern() {
        return ROMAN_NUMBER_REGEX_PATTERN;
    }

    @Override
    protected Pattern getLowerCaseRegexPattern() {
        return LOWER_CASE_ROMAN_NUMBER_REGEX_PATTERN;
    }

    @Override
    protected Pattern getUpperCaseRegexPattern() {
        return UPPER_CASE_ROMAN_NUMBER_REGEX_PATTERN;
    }

    @Override
    protected String getStringFromNumber(Integer number) {
        return new RomanNumber(number).toString();
    }

    @Override
    protected Integer getNumberFromString(String string) {
        try {
            return new RomanNumber(string.toUpperCase()).getArabicNumber();
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    @Override
    public Boolean checkPrefixAndSuffix(String prefix, String suffix) {
        return (prefix.isEmpty() || !Character.isLetter(prefix.charAt(prefix.length() - 1))) &&
               (suffix.isEmpty() || !Character.isLetter(suffix.charAt(0)));
    }

    @Override
    public int recalculatePrefixLength(String string, int prefixLength) {
        return getNotRegexStartLength(string, prefixLength);
    }
}
