package org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection;

public class AlfaLettersListLabelsDetectionAlgorithm2 extends AlfaLettersListLabelsDetectionAlgorithm {

    @Override
    protected String getStringFromNumber(Integer number) {
        return getLetters2FromNumber(number);
    }

    @Override
    protected Integer getNumberFromString(String string) {
        return getNumberFromLetters2(string);
    }

    private String getLetters2FromNumber(int integer) {
        StringBuilder str = new StringBuilder();
        while (integer > 0) {
            integer--;
            int k = integer % getLetters().size();
            str.insert(0, getLetters().get(k));
            integer /= getLetters().size();
        }
        return str.toString();
    }

    private Integer getNumberFromLetters2(String s) {
        if (s.isEmpty()) {
            return null;
        }
        int result = 0;
        for (char c : s.toCharArray()) {
            int num = getLetters().indexOf(c);
            if (num < 0) {
                return null;
            }
            result = result * getLetters().size() + num + 1;
        }
        return result;
    }
}
