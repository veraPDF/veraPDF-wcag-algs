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

public abstract class AlfaLettersListLabelsDetectionAlgorithm extends LettersListLabelsDetectionAlgorithm {

    protected static final List<Character> LETTERS = Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');

    private static final String UPPER_CASE_ENGLISH_LETTER_REGEX = "[A-Z]+";
    private static final String LOWER_CASE_ENGLISH_LETTER_REGEX = "[a-z]+";
    private static final String ENGLISH_LETTER_REGEX = "[A-Za-z]+";

    AlfaLettersListLabelsDetectionAlgorithm() {
        super();
    }
    
    AlfaLettersListLabelsDetectionAlgorithm(int increment) {
        super(increment);
    }

    @Override
    protected String getRegex() {
        return ENGLISH_LETTER_REGEX;
    }

    @Override
    protected String getNumberingStyle(boolean isUpperCase) {
        return isUpperCase ? NumberingStyleNames.ENGLISH_LETTERS_UPPER_CASE : NumberingStyleNames.ENGLISH_LETTERS_LOWER_CASE;
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
        return LETTERS;
    }
}
