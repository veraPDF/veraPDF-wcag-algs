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

    static Stream<Arguments> tableBorderDetectionTestParams() {
        return Stream.of(
                Arguments.of("libra_table_of_content.json", true)
        );
    }

    @ParameterizedTest(name = "{index}: ({0}, {1}) => {0}")
    @MethodSource("tableBorderDetectionTestParams")
    void testTableBorderDetection(String filename,
                                  boolean initialSemanticIsValid) throws IOException {
        IDocument document = JsonToPdfTree.getDocument("/files/TOC/" + filename);
        ITree tree = document.getTree();
        StaticContainers.clearAllContainers(document);

        Consumer<INode> semanticDocumentValidator = new SemanticDocumentPreprocessingConsumer(document);
        tree.forEach(semanticDocumentValidator);

        AccumulatedNodeConsumer semanticDetectionValidator = new AccumulatedNodeConsumer();
        tree.forEach(semanticDetectionValidator);

        TOCDetectionConsumer tocDetectionConsumer = new TOCDetectionConsumer(document);
        tree.forEach(tocDetectionConsumer);

        if (initialSemanticIsValid) {
            testTableInitialTreeStructure(tree);
        }
    }

    private void testTableInitialTreeStructure(ITree tree) {
        for (INode node : tree) {
            if (node.getInitialSemanticType() == SemanticType.TABLE_OF_CONTENT_ITEM) {
                Assertions.assertEquals(node.getInitialSemanticType(), node.getSemanticType());
            }
        }
    }
}
