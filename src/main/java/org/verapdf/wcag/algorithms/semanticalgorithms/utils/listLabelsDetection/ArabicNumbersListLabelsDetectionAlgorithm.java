package org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection;

import org.verapdf.wcag.algorithms.entities.lists.ListInterval;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArabicNumbersListLabelsDetectionAlgorithm extends ListLabelsDetectionAlgorithm {

    private static final String ARABIC_NUMBER_REGEX = "\\d+";

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
            int nextNumberOfStartsZeros = getNumberOfStartZeros(nextSubstring);
            if (numberOfStartZeros != nextNumberOfStartsZeros &&
                    ((numberOfStartZeros - 1) != nextNumberOfStartsZeros ||
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
            numberOfStartZeros = nextNumberOfStartsZeros;
            substring = nextSubstring;
        }
        return true;
    }

    @Override
    public Set<ListInterval> getItemsIntervals(List<String> items) {
        Set<ListInterval> listIntervals = new HashSet<>();
        ArabicNumberInformation arabicNumberInformation = new ArabicNumberInformation();
        for (int i = 0; i < items.size(); i++) {
            if (arabicNumberInformation.number != null) {
                arabicNumberInformation.number++;
                if (!arabicNumberInformation.checkItem(items.get(i))) {
                    if (i > arabicNumberInformation.index + 1) {
                        listIntervals.add(new ListInterval(arabicNumberInformation.index, --i));
                    }
                    i--;
                    arabicNumberInformation.number = null;
                }
            } else if (i != items.size() - 1) {
                arabicNumberInformation = new ArabicNumberInformation(items.get(i), items.get(i + 1), i);
            }
        }
        if (arabicNumberInformation.number != null && items.size() > arabicNumberInformation.index + 1) {
            listIntervals.add(new ListInterval(arabicNumberInformation.index, items.size() - 1));
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
        private final int index;
        private final String prefix;
        private final int start;
        private int numberOfStartZeros;
        private int arabicNumberStartLength;
        private boolean haveSameStartZeros;

        ArabicNumberInformation() {
            this.number = null;
            this.index = 0;
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
            index = i;
            prefix = item.substring(0, this.start);
        }

        private boolean checkItem(String item) {
            if (!item.startsWith(prefix)) {
                return false;
            }
            String nextSubstring = item.substring(start);
            int nextNumberOfStartsZeros = getNumberOfStartZeros(nextSubstring);
            int nextArabicNumberStartLength = getRegexStartLength(nextSubstring);
            if (numberOfStartZeros != nextNumberOfStartsZeros &&
                    ((numberOfStartZeros - 1) != nextNumberOfStartsZeros ||
                            (arabicNumberStartLength - 1) == nextArabicNumberStartLength || haveSameStartZeros)) {
                return false;
            }
            String s = getStringFromNumber(number);
            if (((numberOfStartZeros == nextNumberOfStartsZeros || numberOfStartZeros - 1 == nextNumberOfStartsZeros) &&
                    !nextSubstring.startsWith(s, nextNumberOfStartsZeros)) ||
                    (s.length() + nextNumberOfStartsZeros != nextArabicNumberStartLength)) {
                return false;
            }
            if (arabicNumberStartLength + 1 == nextArabicNumberStartLength) {
                haveSameStartZeros = true;
                arabicNumberStartLength = nextArabicNumberStartLength;
            }
            numberOfStartZeros = nextNumberOfStartsZeros;
            return true;
        }
    }
}
