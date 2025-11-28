package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.lists.TextListInterval;
import org.verapdf.wcag.algorithms.entities.content.InfoChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.lists.ListInterval;
import org.verapdf.wcag.algorithms.entities.lists.ListIntervalsCollection;
import org.verapdf.wcag.algorithms.entities.lists.info.ListItemInfo;
import org.verapdf.wcag.algorithms.entities.lists.info.ListItemTextInfo;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection.*;

import java.util.*;

public class ListLabelsUtils {

	private static final Set<Character> labels = new HashSet<>(
			Arrays.asList('\u002D', '\u2022', '\u25CF', '\u2714', '\u2717', '\u2794', '\u27A2', '\uE00A', '\uE00C',
					'\uF076', '\u2588', '\u25A0', '\u2013', '\uF0B7', '\uF0A7', '\u25A1', '\uF0A1', '\u25AA',
					'\u25FC', '\u25CB', '\u203B', '\u274D', '\u25E6', '\u25B8', '\u3147', '\uFF4F', '\u002A', '\u25C7', '\u25ef'));
	//office labels examples (-, •, ✔, ✗, ●, ➔, ➢), pdf files labels examples (█, ■, , □, , ▪, ◼, ○, ※, ❍, ㅇ, ▸, ◦, ｏ, *, '◇', '◯')
	private static final Character o =  '\u006F';

	public static boolean isListLabel(String value) {
		char label = value.charAt(0);
		return labels.contains(label) || Character.isDigit(label) || isLetterLabel(value);
	}

	public static boolean isLetterLabel(String value) {
		boolean isFirstCharLetter = Character.isLetter(value.charAt(0));
		if (value.length() > 2) {
			return isFirstCharLetter && !Character.isLetter(value.charAt(1));
		}
		return isFirstCharLetter;
	}

	public static boolean isListLabels(List<String> listLabels, int increment) {
		if (listLabels.size() < 2) {
			return false;
		}
		if (listLabels.get(0).trim().isEmpty()) {
			return false;
		}
		List<String> labels = new ArrayList<>(listLabels.size());
		for (String listLabel : listLabels) {
			String label = listLabel.trim();//isWhiteSpaceCharacter
			if (!label.isEmpty()) {
				labels.add(label);
			}
		}
		if (labels.size() < 2) {
			return false;
		}
		if (isEqualsLabels(labels)) {
			return labels.get(0).length() == 1;
		}
		int commonStartLength = getCommonStartLength(labels.get(0), labels.get(1));
		int commonEndLength = getCommonEndLength(labels.get(0), labels.get(1));
		int minLength = Math.min(labels.get(0).length(), labels.get(1).length());
		for (int i = 2; i < labels.size(); i++) {
			commonStartLength = getCommonStartLength(labels.get(0), labels.get(i), commonStartLength);
			commonEndLength = getCommonEndLength(labels.get(0), labels.get(i), commonEndLength);
			if (minLength > labels.get(i).length()) {
				minLength = labels.get(i).length();
			}
		}
		if (commonStartLength + commonEndLength > minLength) {
			int temp = commonEndLength;
			commonEndLength = minLength - commonStartLength;
			commonStartLength = minLength - temp;
		}
		return new RomanNumbersListLabelsDetectionAlgorithm(increment).isListLabels(labels, commonStartLength, commonEndLength) ||
				new ArabicNumbersListLabelsDetectionAlgorithm(increment).isListLabels(labels, commonStartLength, commonEndLength) ||
				new KoreanLettersListLabelsDetectionAlgorithm(increment).isListLabels(labels, commonStartLength, commonEndLength) ||
				new CircledArabicNumbersListLabelsDetectionAlgorithm(increment).isListLabels(labels, commonStartLength, commonEndLength) ||
				new AlfaLettersListLabelsDetectionAlgorithm1(increment).isListLabels(labels, commonStartLength, commonEndLength) ||
				new AlfaLettersListLabelsDetectionAlgorithm2(increment).isListLabels(labels, commonStartLength, commonEndLength) ||
				new RomanNumbersLowerCaseListLabelsDetectionAlgorithm(increment).isListLabels(labels, commonStartLength, commonEndLength) ||
				new RomanNumbersUpperCaseListLabelsDetectionAlgorithm(increment).isListLabels(labels, commonStartLength, commonEndLength);
	}

	public static boolean isTwoListItemsOfOneList(TextListInterval interval, ListItemTextInfo listItem, Boolean isSequential, boolean isUnordered) {
		ListItemTextInfo previousListItem = interval.getLastListItemInfo();
		String style = interval.getNumberingStyle();
		if ((NumberingStyleNames.UNKNOWN.equals(style) && isUnordered)) {
			if (isUnorderedListItems(interval, listItem, previousListItem)) {
				return true;
			}
		} else if (NumberingStyleNames.UNORDERED.equals(style)) {
			return isUnordered && isUnorderedListItems(interval, listItem, previousListItem);
		}
		String string1 = previousListItem.getListItem();
		String string2 = listItem.getListItem();
		int commonStartLength = getCommonStartLength(string1, string2);
		ListItemTextInfo previousItem = null;
		ListItemTextInfo currentItem = null;
		ListLabelsDetectionAlgorithm algo = null;
		if (NumberingStyleNames.UNKNOWN.equals(style)) {
			for (Map.Entry<String, ListLabelsDetectionAlgorithm> entry : NumberingStyleNames.getMap().entrySet()) {
				ListLabelsDetectionAlgorithm currentAlgo = entry.getValue();
				previousItem = currentAlgo.getListItemInfo(previousListItem, commonStartLength);
				if (previousItem == null) {
					continue;
				}
				currentItem = currentAlgo.getListItemInfo(listItem, commonStartLength);
				if (currentItem != null) {
					style = entry.getKey();
					algo = currentAlgo;
					break;
				}
			}
		} else {
			previousItem = previousListItem;
			algo = NumberingStyleNames.getAlgo(interval.getNumberingStyle());
			currentItem = algo.getListItemInfo(listItem, commonStartLength);
		}
		if (!NumberingStyleNames.KOREAN_LETTERS.equals(style)) {
			isSequential = true;
		}
		if (previousItem == null || currentItem == null) {
			return false;
		}
		if (previousListItem.getPrefix() != null && !Objects.equals(previousListItem.getPrefix(), previousItem.getPrefix())) {
			return false;
		}
        if (!previousItem.getNumberedPart().equals(previousItem.getNumberedPart().toUpperCase()) == currentItem.getNumberedPart().equals(currentItem.getNumberedPart().toUpperCase())) {
            return false;
        }
		if (currentItem.getNumber() <= previousItem.getNumber()) {
			return false;
		}
		if (previousItem.getNumber() + 1 != currentItem.getNumber() && isSequential) {
			return false;
		}
		if (!previousItem.getSuffix().isEmpty() && !currentItem.getSuffix().isEmpty() &&
				previousItem.getSuffix().charAt(0) != currentItem.getSuffix().charAt(0)) {
			return false;
		}
		if (!algo.checkPrefixAndSuffix(previousItem.getPrefix(), previousItem.getSuffix()) ||
				!algo.checkPrefixAndSuffix(currentItem.getPrefix(), currentItem.getSuffix())) {
			return false;
		}
		interval.setLastListItemInfo(previousItem);
		interval.setNumberingStyle(style);
		interval.getListItemsInfos().add(currentItem);
		return true;
	}

	private static boolean isUnorderedListItems(TextListInterval interval, ListItemTextInfo listItem, ListItemTextInfo previousListItem) {
		List<ListItemTextInfo> items = new ArrayList<>(2);
		items.add(previousListItem);
		items.add(listItem);
		if (!getItemsWithEqualsLabels(items).isEmpty()) {
			interval.setNumberingStyle(NumberingStyleNames.UNORDERED);
			interval.getListItemsInfos().add(listItem);
			return true;
		}
		return false;
	}

	private static boolean isEqualsLabels(List<String> labels) {
		for (int i = 1; i < labels.size(); i++) {
			if (!labels.get(0).equals(labels.get(i))) {
				return false;
			}
		}
		return true;
	}

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

	private static int getCommonEndLength(String s1, String s2) {
		return getCommonEndLength(s1, s2, Math.min(s1.length(), s2.length()));
	}

	private static int getCommonEndLength(String s1, String s2, int length) {
		for (int i = 1; i <= length; i++) {
			if (s1.charAt(s1.length() - i) != s2.charAt(s2.length() - i)) {
				return i - 1;
			}
		}
		return length;
	}

	public static Set<ListInterval> getImageListItemsIntervals(List<? extends ListItemInfo> itemsInfo) {
		Set<ListInterval> listIntervals = new HashSet<>();
		ListInterval interval = new ListInterval();
		interval.getListItemsInfos().add(itemsInfo.get(0));
		InfoChunk image = itemsInfo.get(0).getListItemValue();
		for (int i = 1; i < itemsInfo.size(); i++) {
			InfoChunk currentImage = itemsInfo.get(i).getListItemValue();
			if (!NodeUtils.areCloseNumbers(image.getBoundingBox().getWidth(), currentImage.getBoundingBox().getWidth()) ||
			    !NodeUtils.areCloseNumbers(image.getBoundingBox().getHeight(), currentImage.getBoundingBox().getHeight())) {
				if (SemanticType.LIST == itemsInfo.get(i).getSemanticType()) {
					interval.getListsIndexes().add(itemsInfo.get(i).getIndex());
					continue;
				}
				if (interval.getNumberOfListItems() > 1) {
					listIntervals.add(interval);
				}
				image = itemsInfo.get(i).getListItemValue();
				interval = new ListInterval();
				interval.getListItemsInfos().add(itemsInfo.get(i));
			} else {
				interval.getListItemsInfos().add(itemsInfo.get(i));
			}
		}
		if (interval.getNumberOfListItems() > 1) {
			listIntervals.add(interval);
		}
		return listIntervals;
	}

	public static Set<ListInterval> getListItemsIntervals(List<ListItemTextInfo> itemsInfo) {
		ListIntervalsCollection listIntervals = new ListIntervalsCollection(getItemsWithEqualsLabels(itemsInfo));
		listIntervals.putAll(new AlfaLettersListLabelsDetectionAlgorithm1().getItemsIntervals(itemsInfo));
		listIntervals.putAll(new AlfaLettersListLabelsDetectionAlgorithm2().getItemsIntervals(itemsInfo));
		listIntervals.putAll(new KoreanLettersListLabelsDetectionAlgorithm().getItemsIntervals(itemsInfo));
		listIntervals.putAll(new CircledArabicNumbersListLabelsDetectionAlgorithm().getItemsIntervals(itemsInfo));
		listIntervals.putAll(new RomanNumbersListLabelsDetectionAlgorithm().getItemsIntervals(itemsInfo));
		listIntervals.putAll(new ArabicNumbersListLabelsDetectionAlgorithm().getItemsIntervals(itemsInfo));
		listIntervals.putAll(new RomanNumbersLowerCaseListLabelsDetectionAlgorithm().getItemsIntervals(itemsInfo));
		listIntervals.putAll(new RomanNumbersUpperCaseListLabelsDetectionAlgorithm().getItemsIntervals(itemsInfo));
		return listIntervals.getSet();
	}

	public static Set<ListInterval> getItemsWithEqualsLabels(List<ListItemTextInfo> itemsInfo) {
		Set<ListInterval> listIntervals = new HashSet<>();
		Character firstChar = null;
		Character secondChar = null;
		ListInterval interval = new ListInterval();
		for (ListItemTextInfo info : itemsInfo) {
			boolean badListItem = info.getListItem().length() == 1 && info.hasOneLine() || 
					(StaticContainers.isDataLoader() && firstChar != null && Objects.equals(firstChar, secondChar));
			if (!Objects.equals(info.getListItem().charAt(0), firstChar) || badListItem) {
				if (SemanticType.LIST == info.getSemanticType()) {
					interval.getListsIndexes().add(info.getIndex());
					continue;
				}
				if (interval.getNumberOfListItems() > 1 && checkForSuitableLabel(firstChar, secondChar)) {
					interval.setNumberingStyle(NumberingStyleNames.UNORDERED);
					listIntervals.add(interval);
				}
				if (badListItem) {
					firstChar = null;
					secondChar = null;
				} else {
					firstChar = info.getListItem().charAt(0);
					secondChar = info.getListItem().length() > 1 ? info.getListItem().charAt(1) : ' ';
				}
				interval = new ListInterval();
			}
			ListItemTextInfo newItemInfo = new ListItemTextInfo(info);
			newItemInfo.setPrefix("");
			newItemInfo.setNumberedPart(info.getListItem().substring(0, 1));
			interval.getListItemsInfos().add(newItemInfo);
		}
		if (interval.getNumberOfListItems() > 1 && checkForSuitableLabel(firstChar, secondChar)) {
			interval.setNumberingStyle(NumberingStyleNames.UNORDERED);
			listIntervals.add(interval);
		}
		return listIntervals;
	}

	private static boolean checkForSuitableLabel(Character firstChar, Character secondChar) {
		return labels.contains(firstChar) || o.equals(firstChar) && !Character.isLetter(secondChar);
	}

}
