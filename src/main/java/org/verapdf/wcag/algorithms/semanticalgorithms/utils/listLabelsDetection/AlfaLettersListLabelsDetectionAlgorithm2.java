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

public class AlfaLettersListLabelsDetectionAlgorithm2 extends AlfaLettersListLabelsDetectionAlgorithm {

    public AlfaLettersListLabelsDetectionAlgorithm2() {
        super();
    }

    public AlfaLettersListLabelsDetectionAlgorithm2(int increment) {
        super(increment);
    }
    
    @Override
    protected String getStringFromNumber(Integer number) {
        return getLetters2FromNumber(number);
    }

    @Override
    protected Integer getNumberFromString(String string) {
        return getNumberFromLetters2(string);
    }

    private String getLetters2FromNumber(int integer) {
        StringBuilder str = new StringBuilder();
        while (integer > 0) {
            integer--;
            int k = integer % getLetters().size();
            str.insert(0, getLetters().get(k));
            integer /= getLetters().size();
        }
        return str.toString();
    }

    private Integer getNumberFromLetters2(String s) {
        if (s.isEmpty()) {
            return null;
        }
        int result = 0;
        for (char c : s.toCharArray()) {
            int num = getLetters().indexOf(Character.toUpperCase(c));
            if (num < 0) {
                return null;
            }
            result = result * getLetters().size() + num + 1;
        }
        return result;
    }
}
