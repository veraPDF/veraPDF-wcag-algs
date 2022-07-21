package org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection;

public class RomanNumbersListLabelsDetectionAlgorithm extends LettersListLabelsDetectionAlgorithm {

    protected static final String ROMAN_NUMBER_REGEX = "[MDCLXVImdclxvi]*";
    private static final String UPPER_CASE_ROMAN_NUMBER_REGEX = "[MDCLXVI]+";
    private static final String LOWER_CASE_ROMAN_NUMBER_REGEX = "[mdclxvi]+";

    @Override
    protected String getRegex() {
        return ROMAN_NUMBER_REGEX;
    }

    @Override
    protected String getLowerCaseRegex() {
        return LOWER_CASE_ROMAN_NUMBER_REGEX;
    }

    @Override
    protected String getUpperCaseRegex() {
        return UPPER_CASE_ROMAN_NUMBER_REGEX;
    }

    @Override
    protected String getStringFromNumber(Integer number) {
        return new RomanNumber(number).toString();
    }

    @Override
    protected Integer getNumberFromString(String string) {
        try {
            return new RomanNumber(string.toUpperCase()).getArabicNumber();
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    @Override
    protected Boolean checkPrefixAndSuffix(String prefix, String suffix) {
        return (prefix.isEmpty() || !Character.isLetter(prefix.charAt(prefix.length() - 1))) &&
               (suffix.isEmpty() || !Character.isLetter(suffix.charAt(0)));
    }
}
