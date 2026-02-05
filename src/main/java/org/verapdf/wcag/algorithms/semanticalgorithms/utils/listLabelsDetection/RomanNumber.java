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

import java.util.ArrayList;
import java.util.List;

public class RomanNumber {

    private static final int[] NUMBERS = {1000, 900, 500, 400, 100, 90,
            50, 40, 10, 9, 5, 4, 1};

    private static final String[] LETTERS = {"M", "CM", "D", "CD", "C", "XC",
            "L", "XL", "X", "IX", "V", "IV", "I"};

    private final int arabicNumber;

    public RomanNumber(String s) throws NumberFormatException {
        if (!s.matches(RomanNumbersListLabelsDetectionAlgorithm.ROMAN_NUMBER_REGEX) || s.isEmpty()) {
            throw new NumberFormatException();
        }
        List<Roman> romans = new ArrayList<>();
        for (int i = 0; i < s.length(); i++) {
            romans.add(Roman.valueOf(s.substring(i, i + 1)));
        }
        int value = 0;
        while (!romans.isEmpty()) {
            Roman current = romans.remove(0);
            if (!romans.isEmpty() && current.isLess(romans.get(0))) {
                value += current.toInt(romans.remove(0));
            } else {
                value += current.toInt();
            }
        }
        arabicNumber = value;
    }

    public RomanNumber(int arabicNumber) {
        this.arabicNumber = arabicNumber;
    }

    public int getArabicNumber() {
        return arabicNumber;
    }

    public String toString() {
        StringBuilder roman = new StringBuilder();
        int N = arabicNumber;
        for (int i = 0; i < NUMBERS.length; i++) {
            while (N >= NUMBERS[i]) {
                roman.append(LETTERS[i]);
                N -= NUMBERS[i];
            }
        }
        return roman.toString();
    }
}
