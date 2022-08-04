package org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection;

import org.verapdf.wcag.algorithms.entities.lists.ListInterval;
import org.verapdf.wcag.algorithms.entities.lists.info.ListItemTextInfo;

import java.util.List;
import java.util.Set;

public abstract class ListLabelsDetectionAlgorithm {

    public abstract boolean isListLabels(List<String> labels, int commonStartLength, int commonEndLength);

    public abstract Set<ListInterval> getItemsIntervals(List<ListItemTextInfo> itemsInfo);

    protected static int getCommonStartLength(String s1, String s2) {
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

    protected abstract String getRegex();

    protected abstract String getStringFromNumber(Integer number);

    protected abstract Integer getNumberFromString(String string);

    protected int getRegexStartLength(String string) {
        return getRegexStartLength(string, getRegex());
    }

    public static int getRegexStartLength(String string, String regex) {
        for (int i = 0; i < string.length(); i++) {
            if (!string.substring(i, i + 1).matches(regex)) {
                return i;
            }
        }
        return string.length();
    }

    protected int getNotRegexEndLength(String string, int commonEndLength) {
        return getNotRegexEndLength(string, commonEndLength, getRegex());
    }

    private static int getNotRegexEndLength(String string, int commonEndLength, String regex) {
        if (commonEndLength == 0) {
            return 0;
        }
        for (int i = commonEndLength; i > 0; i--) {
            if (!string.substring(string.length() - i, string.length() - i + 1).matches(regex)) {
                return i;
            }
        }
        return 0;
    }

    protected int getNotRegexStartLength(String string, int commonStartLength) {
        return getNotRegexStartLength(string, commonStartLength, getRegex());
    }

    private static int getNotRegexStartLength(String string, int commonStartLength, String regex) {
        if (commonStartLength == 0) {
            return 0;
        }
        for (int i = commonStartLength; i > 0; i--) {
            if (!string.substring(i - 1, i).matches(regex)) {
                return i;
            }
        }
        return 0;
    }

    protected Boolean checkPrefixAndSuffix(String prefix, String suffix) {
        return true;
    }
}
