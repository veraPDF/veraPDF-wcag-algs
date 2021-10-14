package org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection;

import java.util.ArrayList;
import java.util.List;

public class RomanNumber {

    private static final int[] numbers = {1000, 900, 500, 400, 100, 90,
            50, 40, 10, 9, 5, 4, 1};

    private static final String[] letters = {"M", "CM", "D", "CD", "C", "XC",
            "L", "XL", "X", "IX", "V", "IV", "I"};

    private final int arabicNumber;

    public RomanNumber(String s) throws NumberFormatException {
        if (!s.matches(RomanNumbersListLabelsDetectionAlgorithm.ROMAN_NUMBER_REGEX) || s.isEmpty()) {
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
