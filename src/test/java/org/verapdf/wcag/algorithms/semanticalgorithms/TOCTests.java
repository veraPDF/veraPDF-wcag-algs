package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.verapdf.wcag.algorithms.entities.IDocument;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.JsonToPdfTree;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.*;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class TOCTests {

    static Stream<Arguments> TOCDetectionTestParams() {
        return Stream.of(
                Arguments.of("libra_table_of_content.json", null),
                Arguments.of("Word_Table_of_Contents.json", new Integer[]{1001,null,null,null,null,null,1000})
        );
    }

    @ParameterizedTest(name = "{index}: ({0}, {1}) => {0}")
    @MethodSource("TOCDetectionTestParams")
    void testTOCDetection(String filename, Integer[] errorCodes) throws IOException {
        IDocument document = JsonToPdfTree.getDocument("/files/TOC/" + filename);
        ITree tree = document.getTree();
        StaticContainers.updateContainers(document);

        Consumer<INode> semanticDocumentValidator = new SemanticDocumentPreprocessingConsumer();
        tree.forEach(semanticDocumentValidator);

        AccumulatedNodeConsumer semanticDetectionValidator = new AccumulatedNodeConsumer();
        tree.forEach(semanticDetectionValidator);

        TOCDetectionConsumer tocDetectionConsumer = new TOCDetectionConsumer();
        tree.forEach(tocDetectionConsumer);

        if (errorCodes == null) {
            testTOCInitialTreeStructure(tree);
        } else {
            int index = 0;
            for (INode node : tree) {
                if (node.getInitialSemanticType() == SemanticType.TABLE_OF_CONTENT_ITEM) {
                    Assertions.assertNotEquals(errorCodes.length, index);
                    if (errorCodes[index] == null) {
                        Assertions.assertEquals(node.getInitialSemanticType(), node.getSemanticType());
                    } else {
                        Assertions.assertTrue(node.getErrorCodes().contains(errorCodes[index]));
                    }
                    index++;
                }
            }
            Assertions.assertEquals(errorCodes.length, index);
        }
    }

    private void testTOCInitialTreeStructure(ITree tree) {
        for (INode node : tree) {
            if (node.getInitialSemanticType() == SemanticType.TABLE_OF_CONTENT_ITEM) {
                Assertions.assertEquals(node.getInitialSemanticType(), node.getSemanticType());
            }
        }
    }
}
