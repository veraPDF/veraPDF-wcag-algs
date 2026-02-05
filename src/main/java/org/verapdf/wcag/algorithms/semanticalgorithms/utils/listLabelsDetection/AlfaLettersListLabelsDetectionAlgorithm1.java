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

public class AlfaLettersListLabelsDetectionAlgorithm1 extends AlfaLettersListLabelsDetectionAlgorithm {

    public AlfaLettersListLabelsDetectionAlgorithm1() {
        super();
    }
    
    public AlfaLettersListLabelsDetectionAlgorithm1(int increment) {
        super(increment);
    }

    @Override
    protected String getStringFromNumber(Integer number) {
        return getLetters1FromNumber(number);
    }

    @Override
    protected Integer getNumberFromString(String string) {
        return getNumberFromLetters1(string);
    }

    private String getLetters1FromNumber(int integer) {
        integer--;
        int n = integer / getLetters().size();
        char c = getLetters().get(integer % getLetters().size());
        StringBuilder str = new StringBuilder();
        for (int i = 0; i <= n; i++) {
            str.append(c);
        }
        return str.toString();
    }

    private Integer getNumberFromLetters1(String s) {
        if (s.isEmpty()) {
            return null;
        }
        int num = getLetters().indexOf(Character.toUpperCase(s.charAt(0)));
        if (num < 0) {
            return null;
        }
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) != s.charAt(0)) {
                return null;
            }
        }
        return getLetters().size() * (s.length() - 1) + num + 1;
    }
}
