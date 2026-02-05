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

public class CircledArabicNumbersListLabelsDetectionAlgorithm extends LettersListLabelsDetectionAlgorithm {

    protected static final List<Character> LETTERS = Arrays.asList(
            '\u24EA','\u2460','\u2461','\u2462','\u2463','\u2464','\u2465','\u2466','\u2467','\u2468',
            '\u2469','\u246A','\u246B','\u246C','\u246D','\u246E','\u246F','\u2470','\u2471','\u2472',
            '\u2473','\u3251','\u3252','\u3253','\u3254','\u3255','\u3256','\u3257','\u3258','\u3259',
            '\u325A','\u325B','\u325C','\u325D','\u325E','\u325F','\u32B1','\u32B2','\u32B3','\u32B4',
            '\u32B5','\u32B6','\u32B7','\u32B8','\u32B9','\u32BA','\u32BB','\u32BC','\u32BD','\u32BE',
            '\u32BF');

    private static final String CIRCLED_ARABIC_NUMBERS_REGEX = "[\u24EA\u2460-\u2473\u3251-\u325F\u32B1-\u32BF]+";

    public CircledArabicNumbersListLabelsDetectionAlgorithm() {
    }

    public CircledArabicNumbersListLabelsDetectionAlgorithm(int increment) {
        setIncrement(increment);
    }

    @Override
    protected String getNumberingStyle(boolean isUpperCase) {
        return NumberingStyleNames.CIRCLED_ARABIC_NUMBERS;
    }

    @Override
    protected String getRegex() {
        return CIRCLED_ARABIC_NUMBERS_REGEX;
    }

    @Override
    protected String getLowerCaseRegex() {
        return CIRCLED_ARABIC_NUMBERS_REGEX;
    }

    @Override
    protected List<Character> getLetters() {
        return LETTERS;
    }

    @Override
    protected String getUpperCaseRegex() {
        return CIRCLED_ARABIC_NUMBERS_REGEX;
    }
}
