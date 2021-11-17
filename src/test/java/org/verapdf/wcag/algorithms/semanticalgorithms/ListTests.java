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
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.AccumulatedNodeConsumer;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.ClusterTableConsumer;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.LinesPreprocessingConsumer;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.SemanticDocumentPreprocessingConsumer;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ListUtils;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ListTests {

    static Stream<Arguments> listDetectionTestParams() {
        return Stream.of(
                Arguments.of("list_start_with_Figure_pass.json", new int[] {}, true),
                Arguments.of("test-document-13.json", new int[] {}, true),
                Arguments.of("fake_list_fail.json", new int[] {}, true),
                Arguments.of("list_start_from_005_pass.json", new int[] {}, true),
                Arguments.of("fake_list_with_i_item_diff_formatting_fail.json", new int[] {}, true),
                Arguments.of("list-with-image-label.json", new int[] {3}, false),
                Arguments.of("NEG-fake-list.json", new int[] {4}, false),
                Arguments.of("ordered-list1.json", new int[] {5, 5}, true),
                Arguments.of("PDFUA-Ref-2-06_Brochure.json", new int[] {7, 4, 4}, true)
                );
    }

    @ParameterizedTest(name = "{index}: ({0}, {1}, {2} ) => {0}")
    @MethodSource("listDetectionTestParams")
    void testListDetection(String filename, int[] checkSizes, boolean initialSemanticIsValid) throws IOException {
        IDocument document = JsonToPdfTree.getDocument("/files/lists/" + filename);
        ITree tree = document.getTree();

        LinesPreprocessingConsumer linesPreprocessingConsumer = new LinesPreprocessingConsumer(document);
        linesPreprocessingConsumer.findTableBorders();

        Consumer<INode> semanticDocumentValidator = new SemanticDocumentPreprocessingConsumer(document,
                linesPreprocessingConsumer.getLinesCollection());
        tree.forEach(semanticDocumentValidator);

        Table.updateTableCounter();

        Consumer<INode> paragraphValidator = new AccumulatedNodeConsumer();
        tree.forEach(paragraphValidator);

        ClusterTableConsumer tableFinder = new ClusterTableConsumer();
        tree.forEach(tableFinder);

        List<PDFList> resultLists = tableFinder.getLists();

        Assertions.assertEquals(checkSizes.length, resultLists.size());

        for (int i = 0; i < checkSizes.length; ++i) {
            Assertions.assertEquals(checkSizes[i], resultLists.get(i).getNumberOfListItems());
        }

        if (initialSemanticIsValid) {
            testListTreeStructure(tree);
        }
    }

    private void testListTreeStructure(ITree tree) {
        for (INode node : tree) {
            if (ListUtils.isListNode(node) && !node.isLeaf()) {
                Assertions.assertEquals(node.getInitialSemanticType(), node.getSemanticType());
            }
        }
    }
}
