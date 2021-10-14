package org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection;

import org.verapdf.wcag.algorithms.entities.lists.ListInterval;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class LettersListLabelsDetectionAlgorithm extends ListLabelsDetectionAlgorithm {
    public boolean isListLabels(List<String> labels, int commonStartLength, int commonEndLength) {
        if (!labels.get(0).substring(commonStartLength, labels.get(0).length() - commonEndLength).matches(getRegex())) {
            return false;
        }
        boolean isUpperCase;
        int startLength = getNotRegexStartLength(labels.get(0), commonStartLength);
        int endLength = getNotRegexEndLength(labels.get(0), commonEndLength);
        String substring = labels.get(0).substring(startLength, labels.get(0).length() - endLength);
        if (substring.matches(getLowerCaseRegex())) {
            isUpperCase = false;
        } else if (substring.matches(getUpperCaseRegex())) {
            isUpperCase = true;
        } else {
            return false;
        }
        Integer number = getNumberFromString(substring.toUpperCase());
        if (number == null) {
            return false;
        }
        if (!substring.toUpperCase().equals(getStringFromNumber(number))) {
            return false;
        }
        for (int i = 1; i < labels.size(); i++) {
            substring = labels.get(i).substring(startLength, labels.get(i).length() - endLength);
            if ((!substring.matches(getLowerCaseRegex()) || isUpperCase) &&
                    (!substring.matches(getUpperCaseRegex()) || !isUpperCase)) {
                return false;
            }
            Integer nextNumber = getNumberFromString(substring.toUpperCase());
            if (nextNumber == null) {
                return false;
            }
            if (!substring.toUpperCase().equals(getStringFromNumber(nextNumber)) || nextNumber != ++number) {
                return false;
            }
        }
        return true;
    }

    public Set<ListInterval> getItemsIntervals(List<String> items) {
        Set<ListInterval> listIntervals = new HashSet<>();
        int index = 0;
        Integer number = null;
        int start = 0;
        String prefix = null;
        boolean isUpperCase = false;
        for (int i = 0; i < items.size(); i++) {
            if (number != null) {
                number++;
                String s = getStringFromNumber(number);
                if (!items.get(i).toUpperCase().startsWith(s, start) || !items.get(i).startsWith(prefix) ||
                        isCharMatchRegex(items.get(i), start + s.length()) ||
                        ((!items.get(i).substring(start, start + s.length()).matches(getLowerCaseRegex()) || isUpperCase) &&
                                (!items.get(i).substring(start, start + s.length()).matches(getUpperCaseRegex()) || !isUpperCase))) {
                    if (i > index + 1) {
                        listIntervals.add(new ListInterval(index, --i));
                    }
                    i--;
                    number = null;
                }
            } else if (i != items.size() - 1) {
                int commonLength = getCommonStartLength(items.get(i), items.get(i + 1));
                start = getNotRegexStartLength(items.get(i), commonLength);
                prefix = items.get(i).substring(0, start);
                String substring = items.get(i).substring(start);
                substring = substring.substring(0, getRegexStartLength(substring));
                if (substring.matches(getLowerCaseRegex())) {
                    isUpperCase = false;
                } else if (substring.matches(getUpperCaseRegex())) {
                    isUpperCase = true;
                } else {
                    continue;
                }
                number = getNumberFromString(substring.toUpperCase());
                if (number == null) {
                    continue;
                }
                //only Roman???
                if (!substring.toUpperCase().startsWith(getStringFromNumber(number))) {
                    number = null;
                    continue;
                }
                index = i;
            }
        }
        if (number != null && items.size() > index + 1) {
            listIntervals.add(new ListInterval(index, items.size() - 1));
        }
        return listIntervals;
    }

    protected abstract String getLowerCaseRegex();

    protected abstract String getUpperCaseRegex();

    protected boolean isCharMatchRegex(String s, int index) {
        return isCharMatchRegex(s, index, getRegex());
    }

    private static boolean isCharMatchRegex(String s, int index, String regex) {
        if (s.length() <= index) {
            return false;
        }
        return s.substring(index, index + 1).matches(regex);
    }
}
