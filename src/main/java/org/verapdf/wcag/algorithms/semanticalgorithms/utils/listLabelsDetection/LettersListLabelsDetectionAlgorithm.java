package org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection;

import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.lists.ListInterval;
import org.verapdf.wcag.algorithms.entities.lists.info.ListItemTextInfo;

import java.util.*;

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
        if (!substring.equalsIgnoreCase(getStringFromNumber(number))) {
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
            if (!substring.equalsIgnoreCase(getStringFromNumber(nextNumber)) || nextNumber != ++number) {
                return false;
            }
        }
        return true;
    }

    public Set<ListInterval> getItemsIntervals(List<ListItemTextInfo> itemsInfo) {
        Set<ListInterval> listIntervals = new HashSet<>();
        Integer number = null;
        int start = 0;
        String prefix = null;
        boolean isUpperCase = false;
        ListInterval interval = new ListInterval();
        for (int i = 0; i < itemsInfo.size(); i++) {
            ListItemTextInfo itemInfo = itemsInfo.get(i);
            String item = itemInfo.getListItem();
            if (number != null) {
                number++;
                String s = getStringFromNumber(number);
                if (!item.toUpperCase().startsWith(s, start) || !item.startsWith(prefix) ||
                    isCharMatchRegex(item, start + s.length()) || isBadItem(itemInfo, item, s, start) ||
                    ((!item.substring(start, start + s.length()).matches(getLowerCaseRegex()) || isUpperCase) &&
                     (!item.substring(start, start + s.length()).matches(getUpperCaseRegex()) || !isUpperCase))) {
                    if (SemanticType.LIST.equals(itemInfo.getSemanticType())) {
                        interval.getListsIndexes().add(itemInfo.getIndex());
                        number--;
                        continue;
                    }
                    if (interval.getNumberOfListItems() > 1) {
                        listIntervals.add(interval);
                    }
                    number = null;
                } else {
                    interval.getListItemsInfos().add(itemInfo);
                }
            }
            if (number == null && i != itemsInfo.size() - 1) {
                int commonLength = getCommonStartLength(item, itemsInfo.get(i + 1).getListItem());
                start = getNotRegexStartLength(item, commonLength);
                prefix = item.substring(0, start);
                String substring = item.substring(start);
                int regexStartLength = getRegexStartLength(substring);
                String suffix = substring.substring(regexStartLength);
                if (!checkPrefixAndSuffix(prefix, suffix)) {
                    continue;
                }
                substring = substring.substring(0, regexStartLength);
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
                if (isBadItem(itemInfo, item, substring, start)) {
                    continue;
                }
                //only Roman???
                if (!substring.toUpperCase().startsWith(getStringFromNumber(number))) {
                    number = null;
                    continue;
                }
                interval = new ListInterval();
                interval.getListItemsInfos().add(itemInfo);
            }
        }
        if (number != null && interval.getNumberOfListItems() > 1) {
            listIntervals.add(interval);
        }
        return listIntervals;
    }

    private boolean isBadItem(ListItemTextInfo listItem, String item, String s, int start) {
        return item.length() == start + s.length() && listItem.hasOneLine();
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
