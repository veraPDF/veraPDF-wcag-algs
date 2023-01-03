package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.verapdf.wcag.algorithms.entities.IDocument;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.JsonToPdfTree;
import org.verapdf.wcag.algorithms.entities.tables.TableBordersCollection;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.*;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ErrorCodes;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TableUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class TableCheckingTests {

    private static final Set<Integer> checkedErrorCodes = new HashSet<>();

    static {
        checkedErrorCodes.add(ErrorCodes.ERROR_CODE_1100);
        checkedErrorCodes.add(ErrorCodes.ERROR_CODE_1101);
        checkedErrorCodes.add(ErrorCodes.ERROR_CODE_1102);
        checkedErrorCodes.add(ErrorCodes.ERROR_CODE_1103);
        checkedErrorCodes.add(ErrorCodes.ERROR_CODE_1104);
        checkedErrorCodes.add(ErrorCodes.ERROR_CODE_1105);
        checkedErrorCodes.add(ErrorCodes.ERROR_CODE_1106);
        checkedErrorCodes.add(ErrorCodes.ERROR_CODE_1107);
    }

    static Stream<Arguments> tableCheckingTestParams() {
        return Stream.of(
                Arguments.of("word-pdf-accessibility-guide-page-4.json",new Integer[][]{}),
                Arguments.of("PDF-UA-in-a-Nutshell.json",new Integer[][]{{ErrorCodes.ERROR_CODE_1100,
                        ErrorCodes.ERROR_CODE_1102}, {ErrorCodes.ERROR_CODE_1102}}),
                Arguments.of("7.2-t15-pass-a.json",new Integer[][]{}),
                Arguments.of("7.2-t15-fail-a.json",new Integer[][]{})
        );
    }

    @ParameterizedTest(name = "{index}: ({0}) => {0}")
    @MethodSource("tableCheckingTestParams")
    void testTableChecking(String filename, Integer[][] errorCodes) throws IOException {
        IDocument document = JsonToPdfTree.getDocument("/files/tables/" + filename);
        ITree tree = document.getTree();
        StaticContainers.updateContainers(document);

        LinesPreprocessingConsumer linesPreprocessingConsumer = new LinesPreprocessingConsumer();

        SemanticDocumentPreprocessingConsumer semanticDocumentValidator = new SemanticDocumentPreprocessingConsumer();
        tree.forEach(semanticDocumentValidator);

        StaticContainers.setTableBordersCollection(new TableBordersCollection(linesPreprocessingConsumer.getTableBorders()));

        AccumulatedNodeConsumer semanticDetectionValidator = new AccumulatedNodeConsumer();
        tree.forEach(semanticDetectionValidator);

        TableBorderConsumer tableBorderConsumer = new TableBorderConsumer();
        tableBorderConsumer.recognizeTables(tree);

        TableChecker tableChecker = new TableChecker();
        tree.forEach(tableChecker);

        int index = 0;
        for (INode node : tree) {
            if (TableUtils.isInitialTableNode(node)) {
                Set<Integer> foundErrorCodes = getFoundErrorCodes(node);
                if (!foundErrorCodes.isEmpty()) {
                    Assertions.assertNotEquals(errorCodes.length, index, getErrorMessage(foundErrorCodes, null));
                    Assertions.assertEquals(errorCodes[index].length, foundErrorCodes.size(),
                            getErrorMessage(foundErrorCodes, errorCodes[index]));
                    for (Integer errorCode : errorCodes[index]) {
                        Assertions.assertTrue(foundErrorCodes.contains(errorCode),
                                getErrorMessage(foundErrorCodes, errorCodes[index]));
                    }
                    index++;
                }
            }
        }
        Assertions.assertEquals(errorCodes.length, index,
                getErrorMessage(null, index < errorCodes.length ? errorCodes[index] : null));
    }

    public static String getErrorMessage(Set<Integer> foundErrorCodes, Integer[] expectedErrorCodes) {
        return "Found error codes: " + foundErrorCodes + ". Expected error codes: " + Arrays.toString(expectedErrorCodes);
    }

    private Set<Integer> getFoundErrorCodes(INode node) {
        return getFoundErrorCodes(node, checkedErrorCodes);
    }

    public static Set<Integer> getFoundErrorCodes(INode node, Set<Integer> checkedErrorCodes) {
        Set<Integer> errorCodes = new HashSet<>();
        for (Integer errorCode : checkedErrorCodes) {
            if (node.getErrorCodes().contains(errorCode)) {
                errorCodes.add(errorCode);
            }
        }
        return errorCodes;
    }
}
