package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListLabelsUtils {

	private	static final List<Character> letters = Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X','Y', 'Z');

	private static final String ROMAN_NUMBER_REGEX = "[MDCLXVImdclxvi]*";
	private static final String UPPER_CASE_ROMAN_NUMBER_REGEX = "[MDCLXVI]+";
	private static final String LOWER_CASE_ROMAN_NUMBER_REGEX = "[mdclxvi]+";

	private static final String ENGLISH_LETTER_REGEX = "[A-Za-z]+";
	private static final String UPPER_CASE_ENGLISH_LETTER_REGEX = "[A-Z]+";
	private static final String LOWER_CASE_ENGLISH_LETTER_REGEX = "[a-z]+";
	private static final String ARABIC_NUMBER_REGEX = "\\d+";

	public static boolean isListLabels(List<String> labels) {
		if (labels.size() < 2) {
			return false;
		}
		for (int i = 0; i < labels.size(); i++) {
			labels.set(i, labels.get(i).trim());//isSpaceCharacter
		}
		if (isEqualsLabels(labels)) {
			return true;
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
		String[] substrings = new String[labels.size()];
		for (int i = 0; i < substrings.length; i++) {
			substrings[i] = labels.get(i).substring(commonStartLength, labels.get(i).length() - commonEndLength);
		}
		return isArabicNumberLabels(labels, commonStartLength, commonEndLength) ||
		       isRomanNumberLabels(labels, commonStartLength, commonEndLength) ||
		       isEnglishLetterLabels1(substrings) || isEnglishLetterLabels2(substrings);
	}

	private static boolean isEqualsLabels(List<String> labels) {
		for (int i = 1; i < labels.size(); i++) {
			if (!labels.get(0).equals(labels.get(i))) {
				return false;
			}
		}
		return true;
	}

	private static boolean isArabicNumberLabels(List<String> labels, int commonStartLength, int commonEndLength) {
		try {
			if (!labels.get(0).substring(commonStartLength, labels.get(0).length() - commonEndLength).matches(ARABIC_NUMBER_REGEX)) {
				return false;
			}
			int startLength = getNotArabicNumberStartLength(labels.get(0), commonStartLength);
			int endLength = getNotArabicNumberEndLength(labels.get(0), commonEndLength);
			String substring = labels.get(0).substring(startLength, labels.get(0).length() - endLength);
			int number = Integer.parseUnsignedInt(substring);
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
				if (Integer.parseUnsignedInt(nextSubstring) != ++number) {
					return false;
				}
				numberOfStartZeros = nextNumberOfStartsZeros;
				substring = nextSubstring;
			}
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	private static boolean isRomanNumberLabels(List<String> labels, int commonStartLength, int commonEndLength) {
		try {
			if (!labels.get(0).substring(commonStartLength, labels.get(0).length() - commonEndLength).matches(ROMAN_NUMBER_REGEX)) {
				return false;
			}
			boolean isUpperCase;
			int startLength = getNotRomanNumberStartLength(labels.get(0), commonStartLength);
			int endLength = getNotRomanNumberEndLength(labels.get(0), commonEndLength);
			String substring = labels.get(0).substring(startLength, labels.get(0).length() - endLength);
			if (substring.matches(LOWER_CASE_ROMAN_NUMBER_REGEX)) {
				isUpperCase = false;
			} else if (substring.matches(UPPER_CASE_ROMAN_NUMBER_REGEX)) {
				isUpperCase = true;
			} else {
				return false;
			}
			RomanNumber romanNumber = new RomanNumber(substring.toUpperCase());
			if (!romanNumber.toString().equals(substring.toUpperCase())) {
				return false;
			}
			int number = romanNumber.getArabicNumber();
			for (int i = 1; i < labels.size(); i++) {
				substring = labels.get(i).substring(startLength, labels.get(i).length() - endLength);
				if ((!substring.matches(LOWER_CASE_ROMAN_NUMBER_REGEX) || isUpperCase) &&
				    (!substring.matches(UPPER_CASE_ROMAN_NUMBER_REGEX) || !isUpperCase)) {
					return false;
				}
				RomanNumber nextRomanNumber = new RomanNumber(substring.toUpperCase());
				if (!nextRomanNumber.toString().equals(substring.toUpperCase()) || nextRomanNumber.getArabicNumber() != ++number) {
					return false;
				}
			}
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	private static boolean isEnglishLetterLabels1(String[] labels) {
		boolean isUpperCase;
		if (labels[0].matches(LOWER_CASE_ENGLISH_LETTER_REGEX)) {
			isUpperCase = false;
		} else if (labels[0].matches(UPPER_CASE_ENGLISH_LETTER_REGEX)) {
			isUpperCase = true;
		} else {
			return false;
		}
		Integer number = getNumberFromLetters1(labels[0].toUpperCase());
		if (number == null) {
			return false;
		}
		for (int i = 1; i < labels.length; i++) {
			if ((!labels[i].matches(LOWER_CASE_ENGLISH_LETTER_REGEX) || isUpperCase) &&
			    (!labels[i].matches(UPPER_CASE_ENGLISH_LETTER_REGEX) || !isUpperCase)) {
				return false;
			}
			Integer nextNumber = getNumberFromLetters1(labels[i].toUpperCase());
			if (nextNumber == null || !nextNumber.equals(++number)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isEnglishLetterLabels2(String[] labels) {
		boolean isUpperCase;
		if (labels[0].matches(LOWER_CASE_ENGLISH_LETTER_REGEX)) {
			isUpperCase = false;
		} else if (labels[0].matches(UPPER_CASE_ENGLISH_LETTER_REGEX)) {
			isUpperCase = true;
		} else {
			return false;
		}
		Integer number = getNumberFromLetters2(labels[0].toUpperCase());
		if (number == null) {
			return false;
		}
		for (int i = 1; i < labels.length; i++) {
			if ((!labels[i].matches(LOWER_CASE_ENGLISH_LETTER_REGEX) || isUpperCase) &&
			    (!labels[i].matches(UPPER_CASE_ENGLISH_LETTER_REGEX) || !isUpperCase)) {
				return false;
			}
			Integer nextNumber = getNumberFromLetters2(labels[i].toUpperCase());
			if (nextNumber == null || !nextNumber.equals(++number)) {
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

	private static int getNumberOfStartZeros(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) != '0') {
				return i;
			}
		}
		return s.length();
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

	private static Integer getNumberFromLetters1(String s) {
		if (s.isEmpty()) {
			return null;
		}
		int num = letters.indexOf(s.charAt(0));
		if (num < 0) {
			return null;
		}
		for (int i = 1; i < s.length(); i++) {
			if (s.charAt(i) != s.charAt(0)) {
				return null;
			}
		}
		return letters.size() * (s.length() - 1) + num + 1;
	}

	private static Integer getNumberFromLetters2(String s) {
		if (s.isEmpty()) {
			return null;
		}
		int result = 0;
		for (char c : s.toCharArray()) {
			int num = letters.indexOf(c);
			if (num < 0) {
				return null;
			}
			result = result * letters.size() + num + 1;
		}
		return result;
	}

	private static int getNotEnglishLetterStartLength(String string, int commonStartLength) {
		return getNotRegexStartLength(string, commonStartLength, ENGLISH_LETTER_REGEX);
	}

	private static int getNotRomanNumberStartLength(String string, int commonStartLength) {
		return getNotRegexStartLength(string, commonStartLength, ROMAN_NUMBER_REGEX);
	}

	private static int getNotArabicNumberStartLength(String string, int commonStartLength) {
		return getNotRegexStartLength(string, commonStartLength, ARABIC_NUMBER_REGEX);
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

	private static int getNotEnglishLetterEndLength(String string, int commonEndLength) {
		return getNotRegexEndLength(string, commonEndLength, ENGLISH_LETTER_REGEX);
	}

	private static int getNotRomanNumberEndLength(String string, int commonEndLength) {
		return getNotRegexEndLength(string, commonEndLength, ROMAN_NUMBER_REGEX);
	}

	private static int getNotArabicNumberEndLength(String string, int commonEndLength) {
		return getNotRegexEndLength(string, commonEndLength, ARABIC_NUMBER_REGEX);
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

	public static class RomanNumber {

		private static final int[] numbers = {1000, 900, 500, 400, 100, 90,
		                                      50, 40, 10, 9, 5, 4, 1};

		private static final String[] letters = {"M", "CM", "D", "CD", "C", "XC",
		                                         "L", "XL", "X", "IX", "V", "IV", "I"};

		private final int arabicNumber;

		public RomanNumber(String s) {
			if (!s.matches(ROMAN_NUMBER_REGEX) || s.isEmpty()) {
				throw new NumberFormatException();
			}
			List<Roman> romans = new ArrayList<>();
			for (int i = 0; i < s.length(); i++) {
				romans.add(Roman.valueOf(s.substring(i, i + 1)));
			}
			int value = 0;
			while (!romans.isEmpty()) {
				Roman current = romans.remove(0);
				if (!romans.isEmpty() && current.isLess(romans.get(0))) {
					value += current.toInt(romans.remove(0));
				} else {
					value += current.toInt();
				}
			}
			arabicNumber = value;
		}

		public RomanNumber(int arabicNumber) {
			this.arabicNumber = arabicNumber;
		}

		public int getArabicNumber() {
			return arabicNumber;
		}

		public String toString() {
			StringBuilder roman = new StringBuilder();
			int N = arabicNumber;
			for (int i = 0; i < numbers.length; i++) {
				while (N >= numbers[i]) {
					roman.append(letters[i]);
					N -= numbers[i];
				}
			}
			return roman.toString();
		}

	}

	public enum Roman {
		I(1),
		V(5),
		X(10),
		L(50),
		C(100),
		D(500),
		M(1000);

		private final int value;

		Roman(int value) {
			this.value = value;
		}

		public int toInt() {
			return value;
		}

		public boolean isLess(Roman next) {
			return this.value < next.value;
		}

		public int toInt(Roman next) {
			return next.value - this.value;
		}

	}

}
