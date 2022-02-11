package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.JsonToPdfTree;
import org.verapdf.wcag.algorithms.entities.IDocument;
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
                Arguments.of("7NonTable.json", new int[] {6}, true, true),
                Arguments.of("list-with-line-arts-labels.json", new int[] {}, true, true),
                Arguments.of("list_start_with_Figure_pass.json", new int[] {}, true, true),
                Arguments.of("test-document-13.json", new int[] {}, true, true),
                Arguments.of("fake_list_fail.json", new int[] {}, true, true),
                Arguments.of("list_start_from_005_pass.json", new int[] {}, true, true),
                Arguments.of("fake_list_with_i_item_diff_formatting_fail.json", new int[] {}, true, true),
                Arguments.of("list-with-image-label.json", new int[] {3}, false, true),
                Arguments.of("NEG-fake-list.json", new int[] {4}, false, true),
                Arguments.of("ordered-list1.json", new int[] {5, 5}, true, true),
                Arguments.of("PDFUA-Ref-2-06_Brochure.json", new int[] {7, 4, 4}, true, true),
                Arguments.of("one_element_list.json", new int[]{}, true, true),
                Arguments.of("one_element_start_with_number.json", new int[]{}, true, true)
                );
    }

    @ParameterizedTest(name = "{index}: ({0}, {1}, {2}, {3}) => {0}")
    @MethodSource("listDetectionTestParams")
    void testListDetection(String filename, int[] checkSizes, boolean semanticIsValid,
                           boolean initialSemanticIsValid) throws IOException {
        IDocument document = JsonToPdfTree.getDocument("/files/lists/" + filename);
        ITree tree = document.getTree();

        StaticContainers.clearAllContainers(document);

        LinesPreprocessingConsumer linesPreprocessingConsumer = new LinesPreprocessingConsumer(document);
        linesPreprocessingConsumer.findTableBorders();

        Consumer<INode> semanticDocumentValidator = new SemanticDocumentPreprocessingConsumer(document);
        tree.forEach(semanticDocumentValidator);

        Table.updateTableCounter();

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
