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

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.lists.ListInterval;
import org.verapdf.wcag.algorithms.entities.lists.info.ListItemTextInfo;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;

import java.util.*;
import java.util.regex.Pattern;

public class ArabicNumbersListLabelsDetectionAlgorithm extends ListLabelsDetectionAlgorithm {

    public static final String ARABIC_NUMBER_REGEX = "\\d+";
    public static final Pattern ARABIC_NUMBER_REGEX_PATTERN = Pattern.compile(ARABIC_NUMBER_REGEX);
    public static final Pattern ARABIC_NUMBER_END_REGEX_PATTERN = Pattern.compile(ARABIC_NUMBER_REGEX + "$");
    public static final String DOUBLE_REGEX = ARABIC_NUMBER_REGEX + "\\." + ARABIC_NUMBER_REGEX;
    public static final Pattern DOUBLE_REGEX_PATTERN = Pattern.compile(DOUBLE_REGEX);

    private boolean isHeaderOrFooterDetection = false;

    public ArabicNumbersListLabelsDetectionAlgorithm() {
        super();
    }

    public ArabicNumbersListLabelsDetectionAlgorithm(int increment) {
        super(increment);
    }

    @Override
    public boolean isListLabels(List<String> labels, int commonStartLength, int commonEndLength) {
        if (!getRegexPattern().matcher(labels.get(0).substring(commonStartLength, labels.get(0).length() - commonEndLength)).matches()) {
            return false;
        }
        int startLength = getNotRegexStartLength(labels.get(0), commonStartLength);
        int endLength = getNotRegexEndLength(labels.get(0), commonEndLength);
        String substring = labels.get(0).substring(startLength, labels.get(0).length() - endLength);
        Integer number = getNumberFromString(substring);
        if (number == null) {
            return false;
        }
        int numberOfStartZeros = getNumberOfStartZeros(substring);
        boolean haveSameStartZeros = false;
        for (int i = 1; i < labels.size(); i++) {
            String nextSubstring = labels.get(i).substring(startLength, labels.get(i).length() - endLength);
            int nextNumberOfStartZeros = getNumberOfStartZeros(nextSubstring);
            if (numberOfStartZeros != nextNumberOfStartZeros &&
                    ((numberOfStartZeros - 1) != nextNumberOfStartZeros ||
                            (substring.length() - 1) == nextSubstring.length() || haveSameStartZeros)) {
                return false;
            }
            if (substring.length() + 1 == nextSubstring.length()) {
                haveSameStartZeros = true;
            }
            Integer nextNumber = getNumberFromString(nextSubstring);
            if (nextNumber == null || !nextNumber.equals(number + getIncrement())) {
                return false;
            }
            number += getIncrement();
            numberOfStartZeros = nextNumberOfStartZeros;
            substring = nextSubstring;
        }
        return true;
    }

    @Override
    protected String getNumberingStyle(boolean isUpperCase) {
        return NumberingStyleNames.ARABIC_NUMBERS;
    }

    @Override
    public Set<ListInterval> getItemsIntervals(List<ListItemTextInfo> itemsInfo) {
        Set<ListInterval> listIntervals = new HashSet<>();
        ListInterval interval = new ListInterval();
        ArabicNumberInformation arabicNumberInformation = new ArabicNumberInformation();
        for (int i = 0; i < itemsInfo.size(); i++) {
            if (arabicNumberInformation.number != null) {
                arabicNumberInformation.number += getIncrement();
                ListItemTextInfo itemInfo = itemsInfo.get(i);
                if (!arabicNumberInformation.checkItem(itemInfo) || arabicNumberInformation.isBadItem(itemInfo)) {
                    if (SemanticType.LIST == itemInfo.getSemanticType()) {
                        arabicNumberInformation.number -= getIncrement();
                        interval.getListsIndexes().add(itemInfo.getIndex());
                        continue;
                    }
                    if (interval.getNumberOfListItems() > 1) {
                        --i;
                        interval.setNumberingStyle(getNumberingStyle(false));
                        listIntervals.add(interval);
                    }
                    arabicNumberInformation.number = null;
                } else {
                    ListItemTextInfo newItemInfo = new ListItemTextInfo(itemInfo);
                    newItemInfo.setPrefix(arabicNumberInformation.prefix);
                    newItemInfo.setNumberedPart(itemInfo.getListItem().substring(arabicNumberInformation.start, 
                            arabicNumberInformation.start + arabicNumberInformation.arabicNumberStartLength));
                    interval.getListItemsInfos().add(newItemInfo);
                }
            }
            if (arabicNumberInformation.number == null && i != itemsInfo.size() - 1) {
                ListItemTextInfo itemInfo = itemsInfo.get(i);
                arabicNumberInformation = new ArabicNumberInformation(itemInfo.getListItem(),
                                                                      itemsInfo.get(i + 1).getListItem(), i);
                if (arabicNumberInformation.number != null && arabicNumberInformation.isBadItem(itemInfo)) {
                    arabicNumberInformation.number = null;
                }
                if (arabicNumberInformation.number != null) {
                    interval = new ListInterval();
                    ListItemTextInfo newItemInfo = new ListItemTextInfo(itemInfo);
                    newItemInfo.setPrefix(arabicNumberInformation.prefix);
                    newItemInfo.setNumberedPart(itemInfo.getListItem().substring(arabicNumberInformation.start,
                            arabicNumberInformation.start + arabicNumberInformation.arabicNumberStartLength));
                    interval.getListItemsInfos().add(newItemInfo);
                }
            }
        }
        if (arabicNumberInformation.number != null && interval.getNumberOfListItems() > 1) {
            interval.setNumberingStyle(getNumberingStyle(false));
            listIntervals.add(interval);
        }
        return listIntervals;
    }

    @Override
    protected Pattern getRegexPattern() {
        return ARABIC_NUMBER_REGEX_PATTERN;
    }

    @Override
    protected String getStringFromNumber(Integer number) {
        return number.toString();
    }

    @Override
    protected Integer getNumberFromString(String string) {
        return getIntegerFromString(string);
    }

    public static Integer getIntegerFromString(String string) {
        try {
            return Integer.parseUnsignedInt(string);
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    @Override
    public int recalculatePrefixLength(String string, int prefixLength) {
        return getNotRegexStartLength(string, prefixLength);
    }

    private static int getNumberOfStartZeros(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != '0') {
                return i;
            }
        }
        return s.length();
    }

    class ArabicNumberInformation {
        private Integer number;
//        private final int index;
        private final String prefix;
        private final int start;
        private int numberOfStartZeros;
        private int arabicNumberStartLength;
        private boolean haveSameStartZeros;

        ArabicNumberInformation() {
            this.number = null;
//            this.index = 0;
            this.prefix = null;
            this.start = 0;
            this.numberOfStartZeros = 0;
            this.arabicNumberStartLength = 0;
            this.haveSameStartZeros = true;
        }

        ArabicNumberInformation(String item, String nextItem, int i) {
            int commonLength = getCommonStartLength(item, nextItem);
            start = getNotRegexStartLength(item, commonLength);
            String substring = item.substring(start);
            arabicNumberStartLength = getRegexStartLength(substring);
            substring = substring.substring(0, arabicNumberStartLength);
            number = getNumberFromString(substring);
            numberOfStartZeros = getNumberOfStartZeros(substring);
            haveSameStartZeros = false;
//            index = i;
            prefix = item.substring(0, this.start);
        }

        private boolean isBadItem(ListItemTextInfo listItem) {
            String item = listItem.getListItem();
            String nextSubstring = item.substring(start);
            if (!StaticContainers.isDataLoader()) {
                int nextNumberOfStartZeros = getNumberOfStartZeros(nextSubstring);
                int nextArabicNumberStartLength = getRegexStartLength(nextSubstring);
                if (!isHeaderOrFooterDetection && nextSubstring.length() == nextNumberOfStartZeros + nextArabicNumberStartLength && listItem.hasOneLine()) {
                    return true;
                }   
            }
            if (DOUBLE_REGEX_PATTERN.matcher(item).matches() && listItem.hasOneLine()) {
                return true;
            }
            return false;
        }

        private boolean checkItem(ListItemTextInfo listItem) {
            String item = listItem.getListItem();
            if (!item.startsWith(prefix)) {
                return false;
            }
            String nextSubstring = item.substring(start);
            int nextNumberOfStartZeros = getNumberOfStartZeros(nextSubstring);
            int nextArabicNumberStartLength = getRegexStartLength(nextSubstring);
            if (numberOfStartZeros != nextNumberOfStartZeros &&
                    ((numberOfStartZeros - 1) != nextNumberOfStartZeros ||
                            (arabicNumberStartLength - 1) == nextArabicNumberStartLength || haveSameStartZeros)) {
                return false;
            }
            String s = getStringFromNumber(number);
            if (((numberOfStartZeros == nextNumberOfStartZeros || numberOfStartZeros - 1 == nextNumberOfStartZeros) &&
                    !nextSubstring.startsWith(s, nextNumberOfStartZeros)) ||
                    (s.length() + nextNumberOfStartZeros != nextArabicNumberStartLength)) {
                return false;
            }
            if (arabicNumberStartLength + 1 == nextArabicNumberStartLength) {
                haveSameStartZeros = true;
                arabicNumberStartLength = nextArabicNumberStartLength;
            }
            numberOfStartZeros = nextNumberOfStartZeros;
            return true;
        }
    }

    public boolean isHeaderOrFooterDetection() {
        return isHeaderOrFooterDetection;
    }

    public void setHeaderOrFooterDetection(boolean headerOrFooterDetection) {
        isHeaderOrFooterDetection = headerOrFooterDetection;
    }
}
