package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.content.InfoChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.lists.ListInterval;
import org.verapdf.wcag.algorithms.entities.lists.ListIntervalsCollection;
import org.verapdf.wcag.algorithms.entities.lists.info.ListItemInfo;
import org.verapdf.wcag.algorithms.entities.lists.info.ListItemTextInfo;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection.AlfaLettersListLabelsDetectionAlgorithm1;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection.AlfaLettersListLabelsDetectionAlgorithm2;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection.ArabicNumbersListLabelsDetectionAlgorithm;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection.RomanNumbersListLabelsDetectionAlgorithm;

import java.util.*;

public class ListLabelsUtils {

	private static final Set<Character> labels = new HashSet<>(
			Arrays.asList('\u002D', '\u2022', '\u25CF', '\u2714', '\u2717', '\u2794', '\u27A2', '\uE00A', '\uE00C',
			              '\uF076', '\u2588', '\u25A0', '\u2013', '\uF0B7', '\uF0A7', '\u25A1', '\uF0A1', '\u25AA', '\u25FC')); //office labels examples (-, •, ✔, ✗, ●, ➔, ➢), pdf files labels examples (█, ■, , □, , ▪, ◼)
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

	public static boolean isListLabels(List<String> listLabels) {
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
		return new RomanNumbersListLabelsDetectionAlgorithm().isListLabels(labels, commonStartLength, commonEndLength) ||
				new ArabicNumbersListLabelsDetectionAlgorithm().isListLabels(labels, commonStartLength, commonEndLength) ||
				new AlfaLettersListLabelsDetectionAlgorithm1().isListLabels(labels, commonStartLength, commonEndLength) ||
				new AlfaLettersListLabelsDetectionAlgorithm2().isListLabels(labels, commonStartLength, commonEndLength);
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
		listIntervals.putAll(new RomanNumbersListLabelsDetectionAlgorithm().getItemsIntervals(itemsInfo));
		listIntervals.putAll(new ArabicNumbersListLabelsDetectionAlgorithm().getItemsIntervals(itemsInfo));
		return listIntervals.getSet();
	}

	public static Set<ListInterval> getItemsWithEqualsLabels(List<ListItemTextInfo> itemsInfo) {
		Set<ListInterval> listIntervals = new HashSet<>();
		Character firstChar = null;
		Character secondChar = null;
		ListInterval interval = new ListInterval();
		for (ListItemTextInfo info : itemsInfo) {
			boolean badListItem = info.getListItem().length() == 1 && info.hasOneLine();
			if (!Objects.equals(info.getListItem().charAt(0), firstChar) || badListItem) {
				if (SemanticType.LIST == info.getSemanticType()) {
					interval.getListsIndexes().add(info.getIndex());
					continue;
				}
				if (interval.getNumberOfListItems() > 1 && checkForSuitableLabel(firstChar, secondChar)) {
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
			interval.getListItemsInfos().add(info);
		}
		if (interval.getNumberOfListItems() > 1 && checkForSuitableLabel(firstChar, secondChar)) {
			listIntervals.add(interval);
		}
		return listIntervals;
	}

	private static boolean checkForSuitableLabel(Character firstChar, Character secondChar) {
		return labels.contains(firstChar) || o.equals(firstChar) && !Character.isLetter(secondChar);
	}

}
