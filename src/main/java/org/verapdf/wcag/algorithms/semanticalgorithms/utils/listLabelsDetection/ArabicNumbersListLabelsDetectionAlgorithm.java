package org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.lists.ListInterval;
import org.verapdf.wcag.algorithms.entities.lists.info.ListItemTextInfo;

import java.util.*;

public class ArabicNumbersListLabelsDetectionAlgorithm extends ListLabelsDetectionAlgorithm {

    public static final String ARABIC_NUMBER_REGEX = "\\d+";
    public static final String DOUBLE_REGEX = ARABIC_NUMBER_REGEX + "\\." + ARABIC_NUMBER_REGEX;

    @Override
    public boolean isListLabels(List<String> labels, int commonStartLength, int commonEndLength) {
        if (!labels.get(0).substring(commonStartLength, labels.get(0).length() - commonEndLength).matches(getRegex())) {
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
            if (nextNumber == null || !nextNumber.equals(++number)) {
                return false;
            }
            numberOfStartZeros = nextNumberOfStartZeros;
            substring = nextSubstring;
        }
        return true;
    }

    @Override
    public Set<ListInterval> getItemsIntervals(List<ListItemTextInfo> itemsInfo) {
        Set<ListInterval> listIntervals = new HashSet<>();
        ListInterval interval = new ListInterval();
        ArabicNumberInformation arabicNumberInformation = new ArabicNumberInformation();
        for (int i = 0; i < itemsInfo.size(); i++) {
            if (arabicNumberInformation.number != null) {
                arabicNumberInformation.number++;
                ListItemTextInfo itemInfo = itemsInfo.get(i);
                if (!arabicNumberInformation.checkItem(itemInfo) || arabicNumberInformation.isBadItem(itemInfo)) {
                    if (SemanticType.LIST == itemInfo.getSemanticType()) {
                        arabicNumberInformation.number--;
                        interval.getListsIndexes().add(itemInfo.getIndex());
                        continue;
                    }
                    if (interval.getNumberOfListItems() > 1) {
                        --i;
                        listIntervals.add(interval);
                    }
                    arabicNumberInformation.number = null;
                } else {
                    interval.getListItemsInfos().add(itemInfo);
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
                    interval.getListItemsInfos().add(itemInfo);
                }
            }
        }
        if (arabicNumberInformation.number != null && interval.getNumberOfListItems() > 1) {
            listIntervals.add(interval);
        }
        return listIntervals;
    }

    @Override
    protected String getRegex() {
        return ARABIC_NUMBER_REGEX;
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
            int nextNumberOfStartZeros = getNumberOfStartZeros(nextSubstring);
            int nextArabicNumberStartLength = getRegexStartLength(nextSubstring);
            if (nextSubstring.length() == nextNumberOfStartZeros + nextArabicNumberStartLength && listItem.hasOneLine()) {
                return true;
            }
            if (item.matches(DOUBLE_REGEX) && listItem.hasOneLine()) {
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
}
