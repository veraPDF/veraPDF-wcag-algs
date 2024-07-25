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

    private String getLetters1FromNumber(int integer) {
        integer--;
        int n = integer / getLetters().size();
        char c = getLetters().get(integer % getLetters().size());
        StringBuilder str = new StringBuilder();
        for (int i = 0; i <= n; i++) {
            str.append(c);
        }
        return str.toString();
    }

    private Integer getNumberFromLetters1(String s) {
        if (s.isEmpty()) {
            return null;
        }
        int num = getLetters().indexOf(s.charAt(0));
        if (num < 0) {
            return null;
        }
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) != s.charAt(0)) {
                return null;
            }
        }
        return getLetters().size() * (s.length() - 1) + num + 1;
    }
}
