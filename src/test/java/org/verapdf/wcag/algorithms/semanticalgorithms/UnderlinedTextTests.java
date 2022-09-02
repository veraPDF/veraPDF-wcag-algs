package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.verapdf.wcag.algorithms.entities.IDocument;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticSpan;
import org.verapdf.wcag.algorithms.entities.JsonToPdfTree;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextColumn;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.LinesPreprocessingConsumer;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.SemanticDocumentPreprocessingConsumer;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class UnderlinedTextTests {

    static Stream<Arguments> underlinedTextDetectionTestParams() {
        return Stream.of(
                Arguments.of("underlinedText1.json", new boolean[]{ true }),
                Arguments.of("underlinedText2.json", new boolean[]{ true }),
                Arguments.of("underlinedText3.json", new boolean[]{ true }),
                Arguments.of("link-with-space.json", new boolean[]{ true, true, false, false })
                );
    }

    @ParameterizedTest(name = "{index}: ({0}, {1}) => {0}")
    @MethodSource("underlinedTextDetectionTestParams")
    void testUnderlinedTextDetection(String filename, boolean[] isUnderlined) throws IOException {
        IDocument document = JsonToPdfTree.getDocument("/files/underlinedText/" + filename);
        ITree tree = document.getTree();
        StaticContainers.updateContainers(document);
        LinesPreprocessingConsumer linesPreprocessingConsumer = new LinesPreprocessingConsumer();
        linesPreprocessingConsumer.findTableBorders();

        Consumer<INode> semanticDocumentValidator = new SemanticDocumentPreprocessingConsumer();
        tree.forEach(semanticDocumentValidator);
        testUnderlined(tree, isUnderlined);
    }

    private void testUnderlined(ITree tree, boolean[] isUnderlined) {
        int index = 0;
        for (INode node : tree) {
            if (node instanceof SemanticSpan) {
                for (TextColumn column : ((SemanticSpan)node).getColumns()) {
                    for (TextLine textLine : column.getLines()) {
                        for (TextChunk textChunk : textLine.getTextChunks()) {
                            Assertions.assertEquals(isUnderlined[index], textChunk.getIsUnderlinedText());
                            index++;
                        }
                    }
                }
            }
        }
    }
}
