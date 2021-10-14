package org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection;

public class AlfaLettersListLabelsDetectionAlgorithm1 extends AlfaLettersListLabelsDetectionAlgorithm {

    @Override
    protected String getStringFromNumber(Integer number) {
        return getLetters1FromNumber(number);
    }

    @Override
    protected Integer getNumberFromString(String string) {
        return getNumberFromLetters1(string);
    }

    private static String getLetters1FromNumber(int integer) {
        integer--;
        int n = integer / letters.size();
        char c = letters.get(integer % letters.size());
        StringBuilder str = new StringBuilder();
        for (int i = 0; i <= n; i++) {
            str.append(c);
        }
        return str.toString();
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
}
