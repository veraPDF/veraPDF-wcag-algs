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

import org.verapdf.wcag.algorithms.entities.lists.ListInterval;
import org.verapdf.wcag.algorithms.entities.lists.info.ListItemTextInfo;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class ListLabelsDetectionAlgorithm {

    private int increment = 1;

    public ListLabelsDetectionAlgorithm() {
    }

    public ListLabelsDetectionAlgorithm(int increment) {
        setIncrement(increment);
    }

    protected abstract String getNumberingStyle(boolean isUpperCase);

    public abstract boolean isListLabels(List<String> labels, int commonStartLength, int commonEndLength);

    public abstract Set<ListInterval> getItemsIntervals(List<ListItemTextInfo> itemsInfo);

    public static int getCommonStartLength(String s1, String s2) {
        return getCommonStartLength(s1, s2, Math.min(s1.length(), s2.length()));
    }

    private static int getCommonStartLength(String s1, String s2, int length) {
        for (int i = 0; i < length; i++) {
            if (s1.charAt(i) != s2.charAt(i)) {
                return i;
            }
        }
        return length;
    }

    protected abstract Pattern getRegexPattern();

    protected abstract String getStringFromNumber(Integer number);

    protected abstract Integer getNumberFromString(String string);

    protected int getRegexStartLength(String string) {
        return getRegexStartLength(string, getRegexPattern());
    }

    public static int getRegexStartLength(String string, Pattern regex) {
        for (int i = 0; i < string.length(); i++) {
            if (!regex.matcher(string.substring(i, i + 1)).matches()) {
                return i;
            }
        }
        return string.length();
    }

    protected int getNotRegexEndLength(String string, int commonEndLength) {
        return getNotRegexEndLength(string, commonEndLength, getRegexPattern());
    }

    private static int getNotRegexEndLength(String string, int commonEndLength, Pattern regex) {
        if (commonEndLength == 0) {
            return 0;
        }
        for (int i = commonEndLength; i > 0; i--) {
            if (!regex.matcher(string.substring(string.length() - i, string.length() - i + 1)).matches()) {
                return i;
            }
        }
        return 0;
    }

    protected int getNotRegexStartLength(String string, int commonStartLength) {
        return getNotRegexStartLength(string, commonStartLength, getRegexPattern());
    }

    private static int getNotRegexStartLength(String string, int commonStartLength, Pattern regex) {
        if (commonStartLength == 0) {
            return 0;
        }
        for (int i = commonStartLength; i > 0; i--) {
            if (!regex.matcher(string.substring(i - 1, i)).matches()) {
                return i;
            }
        }
        return 0;
    }

    public ListItemTextInfo getListItemInfo(ListItemTextInfo listItemTextInfo, int prefixLength) {
        ListItemTextInfo newListItemTextInfo = new ListItemTextInfo(listItemTextInfo);
        int newPrefixLength = recalculatePrefixLength(listItemTextInfo.getListItem(), prefixLength);
        String substring = listItemTextInfo.getListItem().substring(newPrefixLength);
        int regexStartLength = getRegexStartLength(substring);
        if (regexStartLength == 0) {
            return null;
        }
        newListItemTextInfo.setNumberedPart(substring.substring(0, regexStartLength));
        Integer number = getNumberFromString(newListItemTextInfo.getNumberedPart());
        if (number == null) {
            return null;
        }
        newListItemTextInfo.setNumber(number);
        newListItemTextInfo.setPrefix(listItemTextInfo.getListItem().substring(0, newPrefixLength));
        newListItemTextInfo.setSuffix(substring.substring(regexStartLength));
        return newListItemTextInfo;
    }

    public int recalculatePrefixLength(String string, int prefixLength) {
        return prefixLength;
    }

    public Boolean checkPrefixAndSuffix(String prefix, String suffix) {
        return true;
    }

    public int getIncrement() {
        return increment;
    }

    public void setIncrement(int increment) {
        this.increment = increment;
    }
}
