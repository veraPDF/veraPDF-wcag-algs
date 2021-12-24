package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.verapdf.wcag.algorithms.entities.IDocument;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.JsonToPdfTree;
import org.verapdf.wcag.algorithms.entities.tables.Table;
import org.verapdf.wcag.algorithms.entities.tables.tableBorders.TableBorder;
import org.verapdf.wcag.algorithms.entities.tables.TableBorderBuilder;
import org.verapdf.wcag.algorithms.entities.tables.TableBordersCollection;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.AccumulatedNodeConsumer;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.LinesPreprocessingConsumer;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.SemanticDocumentPreprocessingConsumer;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.TableBorderConsumer;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TableUtils;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class TableBordersTests {

    static Stream<Arguments> tableBorderDetectionTestParams() {
        return Stream.of(
                Arguments.of("table-word", new int[][] {{65}}, new int[][] {{4}}, new int[][] {{4}}, true, true),
                Arguments.of("emptyCells", new int[][] {{40}}, new int[][] {{3}}, new int[][] {{3}}, true, true),
                Arguments.of("emptyCells2", new int[][] {{40}}, new int[][] {{3}}, new int[][] {{3}}, true, true),
                Arguments.of("emptyCells3", new int[][] {{40}}, new int[][] {{3}}, new int[][] {{3}}, true, true),
                Arguments.of("emptyCells4", new int[][] {{40}}, new int[][] {{3}}, new int[][] {{3}}, true, true),
                Arguments.of("NEG-fake-table", new int[][] {{10}}, new int[][] {{4}}, new int[][] {{4}}, false, true),
                Arguments.of("NEG-floating-text-box", new int[][] {{5}}, new int[][] {{}}, new int[][] {{}}, true, true),
                Arguments.of("PDFUA-Ref-2-02_Invoice", new int[][] {{}}, new int[][] {{}}, new int[][] {{}}, true, false),
                Arguments.of("PDFUA-Reference-01_(Matterhorn-Protocol_1-02)",
                        new int[][] {{},{62},{53},{173},{113,93,53,93,113},{73,73,193,53},{173,53,173,173},
                                {133,93,93,73,113},{93,53,53,53,53,53,73,53},{393},{53,73,233},{333},{133}},
                        new int[][] {{},{5},{6},{8},{5,4,2,4,5},{3,3,9,2},{8,2,8,8},{6,4,4,3,5},{4,2,2,2,2,2,3,2},
                                {19},{2,3,11},{16},{6}},
                        new int[][] {{},{3},{2},{6},{6,6,6,6,6},{6,6,6,6},{6,6,6,6},{6,6,6,6,6},{6,6,6,6,6,6,6,6},
                                {6},{6,6,6},{6},{6}}, true, true),
                Arguments.of("PDFUA-Ref-2-05_BookChapter-german",
                        new int[][] {{},{},{},{},{5},{},{62},{},{},{},{},{},{},{},{},{},{},{},{},{},{}},
                        new int[][] {{},{},{},{},{},{},{6},{},{},{},{},{},{},{},{},{},{},{},{},{},{}},
                        new int[][] {{},{},{},{},{},{},{5},{},{},{},{},{},{},{},{},{},{},{},{},{},{}}, true, true),
                Arguments.of("PDFUA-Ref-2-06_Brochure", new int[][] {{10,9},{}}, new int[][] {{},{}}, new int[][] {{},{}}, true, true),
                Arguments.of("tableBorders/table_word", new int[][] {{38}}, new int[][] {{4}}, new int[][] {{3}}, true, true),
                Arguments.of("tableBorders/table_libre", new int[][] {{8}}, new int[][] {{3}}, new int[][] {{3}}, true, true)
        );
    }

    @ParameterizedTest(name = "{index}: ({0}, {1}, {2}, {3}, {4}, {5}) => {0}")
    @MethodSource("tableBorderDetectionTestParams")
    void testTableBorderDetection(String filename, int[][] list, int[][] listN, int[][] listM, boolean semanticIsValid,
                                  boolean initialSemanticIsValid) throws IOException {
        IDocument document = JsonToPdfTree.getDocument("/files/tables/" + filename + ".json");
        ITree tree = document.getTree();
        LinesPreprocessingConsumer linesPreprocessingConsumer = new LinesPreprocessingConsumer(document);
        List<List<TableBorderBuilder>> tableBorderBuilders = linesPreprocessingConsumer.getTableBorders();
        Assertions.assertEquals(list.length, tableBorderBuilders.size());
        for (int pageNumber = 0; pageNumber < tableBorderBuilders.size(); pageNumber++) {
            List<TableBorderBuilder> borders = tableBorderBuilders.get(pageNumber);
            borders.sort(new TableBorderBuilder.TableBorderBuildersComparator());
            Assertions.assertEquals(list[pageNumber].length, borders.size());
            for (int i = 0; i < borders.size(); i++) {
                TableBorderBuilder border = borders.get(i);
                Assertions.assertEquals(list[pageNumber][i], border.getHorizontalLinesNumber() +
                        border.getVerticalLinesNumber());
            }
        }
        Consumer<INode> semanticDocumentValidator = new SemanticDocumentPreprocessingConsumer(document,
                linesPreprocessingConsumer.getLinesCollection());
        tree.forEach(semanticDocumentValidator);
        Table.updateTableCounter();
        TableBordersCollection tableBordersCollection = new TableBordersCollection(tableBorderBuilders);
        AccumulatedNodeConsumer paragraphValidator = new AccumulatedNodeConsumer(tableBordersCollection);
        tree.forEach(paragraphValidator);
        TableBorderConsumer tableBorderConsumer = new TableBorderConsumer(tableBordersCollection, null);
        tableBorderConsumer.recognizeTables(tree);
        List<SortedSet<TableBorder>> tableBorders = tableBordersCollection.getTableBorders();
        Assertions.assertEquals(listN.length, tableBorders.size());
        for (int pageNumber = 0; pageNumber < tableBorders.size(); pageNumber++) {
            SortedSet<TableBorder> borders = tableBorders.get(pageNumber);
            Assertions.assertEquals(listN[pageNumber].length, borders.size());
            int i = 0;
            for (TableBorder border : borders) {
                Assertions.assertEquals(listN[pageNumber][i], border.getNumberOfRows());
                Assertions.assertEquals(listM[pageNumber][i], border.getNumberOfColumns());
                i++;
            }
        }
        if (semanticIsValid) {
            testTableTreeStructure(tree);
        }

        if (initialSemanticIsValid) {
            testTableInitialTreeStructure(tree);
        }
    }

    private void testTableTreeStructure(ITree tree) {
        for (INode node : tree) {
            if (TableUtils.isTableNode(node)) {
                Assertions.assertEquals(node.getInitialSemanticType(), node.getSemanticType());
            }
        }
    }

    private void testTableInitialTreeStructure(ITree tree) {
        for (INode node : tree) {
            if (TableUtils.isInitialTableNode(node)) {
                Assertions.assertEquals(node.getInitialSemanticType(), node.getSemanticType());
            }
        }
    }
}
