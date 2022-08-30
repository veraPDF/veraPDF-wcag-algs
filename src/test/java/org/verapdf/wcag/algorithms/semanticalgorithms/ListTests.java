package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.verapdf.wcag.algorithms.entities.*;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.lists.PDFList;
import org.verapdf.wcag.algorithms.entities.tables.Table;
import org.verapdf.wcag.algorithms.entities.tables.TableBordersCollection;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.AccumulatedNodeConsumer;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.ClusterTableConsumer;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.LinesPreprocessingConsumer;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.SemanticDocumentPreprocessingConsumer;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ListUtils;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ListTests {

    static Stream<Arguments> listDetectionTestParams() {
        return Stream.of(
                Arguments.of("list-with-wrong-order-text-chunks.json", new int[] {}, true, true, new int[][] {{9, 0, 1}}),
                Arguments.of("7NonTable.json", new int[] {}, true, true, new int[][] {{6, 0, 1}}),
                Arguments.of("list-with-line-arts-labels.json", new int[] {}, true, true, new int[][] {{7, 0, 1}}),
                Arguments.of("list_start_with_Figure_pass.json", new int[] {}, true, true, new int[][] {{5, 0, 1}}),
                Arguments.of("test-document-13.json", new int[] {}, true, true, new int[][] {}),
                Arguments.of("fake_list_fail.json", new int[] {}, true, true, new int[][] {}),
                Arguments.of("list_start_from_005_pass.json", new int[] {}, true, true, new int[][] {{8, 0, 1}}),
                Arguments.of("fake_list_with_i_item_diff_formatting_fail.json", new int[] {}, true, true, new int[][] {{12, 0, 1}}),
                Arguments.of("list-with-image-label.json", new int[] {}, false, true, new int[][] {{3, 0, 1}}),
                Arguments.of("NEG-fake-list.json", new int[] {4}, false, true, new int[][] {{4, 0, 1}}),
                Arguments.of("ordered-list1.json", new int[] {}, true, true, new int[][] {{5, 0, 1}, {5, 0, 1}}),
                Arguments.of("PDFUA-Ref-2-03_AcademicAbstract.json", new int[] {}, false, false, new int[][] {{1, 0, 1}, {9, 0, 1}}),
                Arguments.of("PDFUA-Ref-2-06_Brochure.json", new int[] {}, true, true, new int[][] {{3, 0, 1}, {7, 0, 1}, {4, 0, 1}, {4, 0, 1}}),
                Arguments.of("PDFUA-Ref-2-08_BookChapter_fix.json", new int[] {}, false, true, new int[][] {{6, 0, 2}, {4, 0, 1},
                        {4, 0, 1}, {4, 0, 1}, {4, 0, 1}, {4, 0, 1}, {5, 0, 2}, {4, 0, 1}, {1, 0, 1}, {5, 0, 1}, {4, 0, 1}, {4, 0, 1}, {4, 0, 1}, {4, 0, 1},
                        {4, 0, 2}, {1, 0, 1}, {1, 0, 1}, {4, 0, 1}, {4, 0, 1}, {3, 0, 1}, {3, 0, 1}, {2, 0, 2}, {5, 0, 2}, {2, 0, 1}, {3, 0, 1}, {3, 0, 1},
                        {4, 0, 1}, {1, 0, 1}, {2, 0, 1}, {3, 0, 2}, {1, 0, 1}, {8, 0, 2}, {2, 0, 1}, {2, 0, 1}, {4, 0, 1}, {5, 0, 1}, {2, 0, 1}, {6, 0, 1},
                        {6, 0, 2}, {2, 0, 1}, {1, 0, 1}, {3, 0, 3}, {3, 0, 1}, {3, 0, 1}, {4, 0, 2}, {4, 0, 1}, {4, 0, 1}, {4, 0, 1}, {4, 0, 1}, {7, 0, 2}}),
                Arguments.of("one_element_list.json", new int[]{}, true, true, new int[][] {{1, 0, 1}}),
                Arguments.of("one_element_start_with_number.json", new int[]{}, true, true, new int[][] {{1, 0, 1}}),
                Arguments.of("list_not_caption.json", new int[]{}, true, true, new int[][] {{2, 0, 1}}),
                Arguments.of("one_elem_list_letter_label.json", new int[]{}, true, true, new int[][] {{1, 0, 1}, {3, 0, 1}}),
                Arguments.of("pdf_tagging_list_with_o_labels.json", new int[]{}, true, true, new int[][] {{3, 0, 1}, {4, 0, 1}, {5, 0, 1}}),
                Arguments.of("pdf_tagging_one_elem_list_with_o_label.json", new int[]{}, true, true, new int[][] {{1, 0, 1}, {4, 0, 1}}),
                Arguments.of("test-document-16.json", new int[]{}, true, true, new int[][] {{9, 0, 1}, {2, 0, 1}}),
                Arguments.of("accessible_pdf_webinar_session_ADA.json", new int[]{}, true, true, new int[][] {{3, 0, 1}, {1, 0, 1}}),
                Arguments.of("accessible_pdf_webinar_session_ADA1.json", new int[]{}, true, true, new int[][] {{1, 0, 1}, {4, 0, 1}, {3, 0, 1}, {1, 0, 1}, {2, 0, 1}}),
                Arguments.of("list-inside-list-creating-accessible.json", new int[]{}, true, true, new int[][] {{1, 0, 1}, {8, 1, 1}}),
                Arguments.of("list-inside-list-char-label-creating-accessible.json", new int[]{}, true, true, new int[][] {{1, 0, 1}, {8, 1, 1}}),
                Arguments.of("list-inside-list-roman-creating-accessible.json", new int[]{}, true, true, new int[][] {{1, 0, 1}, {8, 1, 1}}),
                Arguments.of("list-inside-list-creating-accessible2.json", new int[]{}, true, true, new int[][] {{1, 0, 1}, {1, 0, 1}, {6, 2, 1}}),
                Arguments.of("span-roman-letters-creating-accessible.json", new int[]{}, true, true, new int[][] {}),
                Arguments.of("three-lists-last-creating-accessible.json", new int[]{}, true, true,  new int[][] {{1, 0, 1}, {2, 1, 1}})
                );
    }

    @ParameterizedTest(name = "{index}: ({0}, {1}, {2}, {3}) => {0}")
    @MethodSource("listDetectionTestParams")
    void testListDetection(String filename, int[] checkSizes, boolean semanticIsValid,
                           boolean initialSemanticIsValid, int[][] itemsAndColumnsOfLists) throws IOException {
        IDocument document = JsonToPdfTree.getDocument("/files/lists/" + filename);
        ITree tree = document.getTree();

        StaticContainers.clearAllContainers(document);

        LinesPreprocessingConsumer linesPreprocessingConsumer = new LinesPreprocessingConsumer(document);
        linesPreprocessingConsumer.findTableBorders();

        Consumer<INode> semanticDocumentValidator = new SemanticDocumentPreprocessingConsumer(document);
        tree.forEach(semanticDocumentValidator);

        StaticContainers.setTableBordersCollection(new TableBordersCollection(linesPreprocessingConsumer.getTableBorders()));

        AccumulatedNodeConsumer paragraphValidator = new AccumulatedNodeConsumer();
        tree.forEach(paragraphValidator);

        ClusterTableConsumer tableFinder = new ClusterTableConsumer();
        tableFinder.findTables(tree.getRoot());

        List<PDFList> resultLists = tableFinder.getLists();

        Assertions.assertEquals(checkSizes.length, resultLists.size());

        for (int i = 0; i < checkSizes.length; ++i) {
            Assertions.assertEquals(checkSizes[i], resultLists.get(i).getNumberOfListItems());
        }

        if (semanticIsValid) {
            testListTreeStructure(tree);
        }

        if (initialSemanticIsValid) {
            testListInitialTreeStructure(tree);
        }

        int index = 0;
        for (INode node : tree) {
            if (node.getSemanticType() == SemanticType.LIST) {
                INode accumulatedNode = StaticContainers.getAccumulatedNodeMapper().get(node);
                if (accumulatedNode instanceof SemanticList) {
                    Assertions.assertTrue(index < itemsAndColumnsOfLists.length);
                    Assertions.assertEquals(itemsAndColumnsOfLists[index][0], ((SemanticList)accumulatedNode).getNumberOfListItems());
                    Assertions.assertEquals(itemsAndColumnsOfLists[index][1], ((SemanticList)accumulatedNode).getNumberOfLists());
                    Assertions.assertEquals(itemsAndColumnsOfLists[index][2], ((SemanticList)accumulatedNode).getNumberOfListColumns());
                    index++;
                }
            }
        }
        Assertions.assertEquals(itemsAndColumnsOfLists.length, index);
    }

    private void testListTreeStructure(ITree tree) {
        for (INode node : tree) {
            if (ListUtils.isListNode(node) && !node.isLeaf()) {
                Assertions.assertEquals(node.getInitialSemanticType(), node.getSemanticType());
            }
        }
    }

    private void testListInitialTreeStructure(ITree tree) {
        for (INode node : tree) {
            if (ListUtils.isInitialListNode(node)) {
                Assertions.assertEquals(node.getInitialSemanticType(), node.getSemanticType());
            }
        }
    }
}
