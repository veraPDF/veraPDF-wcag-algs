package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.JsonToPdfTree;
import org.verapdf.wcag.algorithms.entities.IDocument;
import org.verapdf.wcag.algorithms.entities.tables.Table;
import org.verapdf.wcag.algorithms.entities.tables.TableBordersCollection;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.*;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TableUtils;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ClusterTableTests {

    static Stream<Arguments> clusterTableDetectionTestParams() {
        return Stream.of(
                Arguments.of("making-PDF-accessible-Acrobat-page-95.json", new int[][] {{3,25}}, true, true),
                Arguments.of("Prevent-Document-Frustration-JAWS-and-PDFs.json", new int[][] {}, false, false),
                Arguments.of("testdocument10.json", new int[][] {{2,15},{2,12},{2,8},{2,9}}, true, true),
                Arguments.of("table-word.json", new int[][] {/*{4, 4}*/}, true, true),
                Arguments.of("NEG-fake-table.json", new int[][] {{4, 4}}, false, true),
                Arguments.of("PDFUA-Ref-2-07_FormalDocument.json", new int[][] {}, true, true),
                Arguments.of("NEG-bad-table.json", new int[][] {{4, 5}}, false, false),
                Arguments.of("NEG-bad-table2.json", new int[][] {{4, 5}}, false, false),
                Arguments.of("no-table.json", new int[][] {}, true, true),
                Arguments.of("NEG-floating-text-box.json", new int[][] {}, true, true),
                Arguments.of("2columns2.json", new int[][] {}, true, true),
                Arguments.of("2columns3.json", new int[][] {}, true, true),
                Arguments.of("NEG-bad-table3.json", new int[][] {}, true, false),//table with merged cells
                Arguments.of("NEG-bad-table3-full.json", new int[][] {{2, 7}}, false, false), // should be 6 separated tables
                Arguments.of("fake-table1.json", new int[][] {{2, 3}, {2, 4}, {2, 4}, {2, 2}, {2, 2}, {2, 2}, {2, 4},
                        {2, 3}, {2, 5}}, false, true), // contents page is recognized as a set of tables
                Arguments.of("real-table.json", new int[][] {{4, 5}, {4, 9}}, false, false),
                Arguments.of("fake-table2.json", new int[][] {{4, 5}, {4, 9}}, false, false),
                Arguments.of("fake-table2-fix.json", new int[][] {{4, 5}, {4, 9}}, true, true),
                Arguments.of("fake-table3.json", new int[][] {}, true, true),
                Arguments.of("tableBorder.json", new int[][] {/*{3, 4}*/}, true, true),
                Arguments.of("Make-your-Acrobat-PDF-learning-resources-accessible-page-13.json", new int[][] {}, true, true),
                Arguments.of("Make-your-Acrobat-PDF-learning-resources-accessible-page-23.json", new int[][] {}, true, true),
                Arguments.of("PDFUA-Reference-08_(English-2013-EU-Conference).json", new int[][] {{2,16},{3,10}}, false, false), // to fix
                Arguments.of("PDFUA-Ref-2-02_Invoice.json", new int[][] {{4, 9}}, false, false),
                Arguments.of("../lists/PDFUA-Ref-2-03_AcademicAbstract.json", new int[][] {{2,4}}, false, true),//to fix
                Arguments.of("PDFUA-Ref-2-05_BookChapter-german.json", new int[][] {}, false, true), // contents page is recognized as table, table on 6th page is not recognized
                Arguments.of("PDFUA-Ref-2-06_Brochure.json", new int[][] {}, true, true),
                Arguments.of("PDFUA-Ref-2-08_BookChapter-page-6.json", new int[][] {{2,2}}, false, true),// to fix
                Arguments.of("PDFUA-Ref-2-08_BookChapter-page-18.json", new int[][] {}, true, true),
                Arguments.of("PDFUA-Ref-2-08_BookChapter-page-24.json", new int[][] {{2,4}}, false, true),//to fix
                Arguments.of("three-tables.json", new int[][] {{5, 6}, {4, 7}, {5, 4}}, false, false) // third table contains images
                );
    }

    @ParameterizedTest(name = "{index}: ({0}, {1}, {2}, {3}) => {0}")
    @MethodSource("clusterTableDetectionTestParams")
    void testClusterTableDetection(String filename, int[][] checkSizes, boolean semanticIsValid,
                                   boolean initialSemanticIsValid) throws IOException {
        IDocument document = JsonToPdfTree.getDocument("/files/tables/" + filename);
        StaticContainers.clearAllContainers(document);

        ITree tree = document.getTree();

        LinesPreprocessingConsumer linesPreprocessingConsumer = new LinesPreprocessingConsumer(document);
        linesPreprocessingConsumer.findTableBorders();

        Consumer<INode> semanticDocumentValidator = new SemanticDocumentPreprocessingConsumer(document);
        tree.forEach(semanticDocumentValidator);

        StaticContainers.setTableBordersCollection(new TableBordersCollection(linesPreprocessingConsumer.getTableBorders()));

        AccumulatedNodeConsumer paragraphValidator = new AccumulatedNodeConsumer();
        tree.forEach(paragraphValidator);

        TableBorderConsumer tableBorderConsumer = new TableBorderConsumer();
        tableBorderConsumer.recognizeTables(tree);

        ClusterTableConsumer tableFinder = new ClusterTableConsumer();
        tableFinder.findTables(tree.getRoot());

        SemanticDocumentPostprocessingConsumer documentPostprocessingConsumer = new SemanticDocumentPostprocessingConsumer();
        documentPostprocessingConsumer.runPostprocessingChecks(tree);

        List<Table> resultTables = tableFinder.getTables();

        Assertions.assertEquals(checkSizes.length, resultTables.size());

        for (int i = 0; i < checkSizes.length; ++i) {
            Assertions.assertEquals(checkSizes[i][0], resultTables.get(i).numberOfColumns());
            Assertions.assertEquals(checkSizes[i][1], resultTables.get(i).numberOfRows());
        }

        if (semanticIsValid) {
            testTableTreeStructure(tree);
        }

        if (initialSemanticIsValid) {//should be false for documents without table tags
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
