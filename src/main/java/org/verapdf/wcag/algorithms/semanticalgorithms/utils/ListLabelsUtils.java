package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.verapdf.wcag.algorithms.entities.content.InfoChunk;
import org.verapdf.wcag.algorithms.entities.lists.ListInterval;
import org.verapdf.wcag.algorithms.entities.lists.ListIntervalsCollection;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection.ArabicNumbersListLabelsDetectionAlgorithm;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection.AlfaLettersListLabelsDetectionAlgorithm1;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection.AlfaLettersListLabelsDetectionAlgorithm2;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection.RomanNumbersListLabelsDetectionAlgorithm;

public class ListLabelsUtils {

	private static final Set<Character> labels = new HashSet<>(Arrays.asList('\u002D', '\u006F', '\u2022', '\u25CF', '\u2714',
			'\u2717', '\u2794', '\u27A2', '\uE00A', '\uE00C', '\uF076', '\u2588', '\u25A0', '\u2013', '\uF0B7'));//office labels examples (-, •, ✔, ✗, ●, ➔, ➢, o), pdf files labels examples (█, ■)

	public static boolean isListLabel(Character label) {
		return labels.contains(label) || Character.isDigit(label);
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

	private static int getCommonStartLength(String s1, String s2) {
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

	public static Set<ListInterval> getImageListItemsIntervals(List<? extends InfoChunk> listItems) {
		Set<ListInterval> listIntervals = new HashSet<>();
		int index = 0;
		InfoChunk image = listItems.get(index);
		for (int i = 1; i < listItems.size(); i++) {
			InfoChunk currentImage = listItems.get(i);
			if (!NodeUtils.areCloseNumbers(image.getBoundingBox().getWidth(), currentImage.getBoundingBox().getWidth()) ||
					!NodeUtils.areCloseNumbers(image.getBoundingBox().getHeight(), currentImage.getBoundingBox().getHeight())) {
				if (index < i - 1) {
					listIntervals.add(new ListInterval(index, i - 1));
				}
				index = i;
				image = listItems.get(i);
			}
		}
		if (index < listItems.size() - 1) {
			listIntervals.add(new ListInterval(index, listItems.size() - 1));
		}
		return listIntervals;
	}

	public static Set<ListInterval> getListItemsIntervals(List<String> listItems) {
		ListIntervalsCollection listIntervals = new ListIntervalsCollection(getItemsWithEqualsLabels(listItems));
		listIntervals.putAll(new AlfaLettersListLabelsDetectionAlgorithm1().getItemsIntervals(listItems));
		listIntervals.putAll(new AlfaLettersListLabelsDetectionAlgorithm2().getItemsIntervals(listItems));
		listIntervals.putAll(new RomanNumbersListLabelsDetectionAlgorithm().getItemsIntervals(listItems));
		listIntervals.putAll(new ArabicNumbersListLabelsDetectionAlgorithm().getItemsIntervals(listItems));
		return listIntervals.getSet();
	}

	public static Set<ListInterval> getItemsWithEqualsLabels(List<String> items) {
		Set<ListInterval> listIntervals = new HashSet<>();
		char firstChar = items.get(0).charAt(0);
		int index = 0;
		for (int i = 1; i < items.size(); i++) {
			if (items.get(i).charAt(0) != firstChar) {
				if (index < i - 1 && labels.contains(firstChar)) {
					listIntervals.add(new ListInterval(index, i - 1));
				}
				firstChar = items.get(i).charAt(0);
				index = i;
			}
		}
		if (index < items.size() - 1 && labels.contains(firstChar)) {
			listIntervals.add(new ListInterval(index, items.size() - 1));
		}
		return listIntervals;
	}

}
