package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticNode;
import org.verapdf.wcag.algorithms.entities.SemanticParagraph;
import org.verapdf.wcag.algorithms.entities.SemanticSpan;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

import java.util.List;
import java.util.Map;

public class AccumulatedNodeConsumerTests {

    private static AccumulatedNodeConsumer consumer;

    @BeforeEach
    public void initConsumer() {
        consumer = new AccumulatedNodeConsumer();
    }

    @Test
    public void acceptSemanticSpanWithOneTextChunkTest() {
        SemanticSpan node = new SemanticSpan();
        TextChunk first = new TextChunk(new BoundingBox(new double[] {70.80296, 82.69164, 101.41999999999999, 96.01812}),
                                        "the goa", 9.96, 85.8);
        node.add(first);
        consumer.accept(node);
        INode accumulatedNode = consumer.getAccumulatedNode(node);
        Assertions.assertEquals(0, node.getChildren().size());
        Assertions.assertEquals(1.0f, accumulatedNode.getCorrectSemanticScore(), 0.0001f);
    }

    @Test
    public void acceptSemanticSpanWithTwoTextChunksTest() {
        SemanticSpan node = new SemanticSpan();
        TextChunk first = new TextChunk(new BoundingBox(new double[] {70.80296, 82.69164, 101.41999999999999, 96.01812}),
                                        "the goa", 9.96, 85.8);
        node.add(first);
        TextChunk second = new TextChunk(new BoundingBox(new double[] {101.40012000000002, 82.69164, 531.3394560000002, 96.01812}),
                                         "l of enhancing the visual experience. In such a case, the background is not required for understanding the ",
                                         9.96, 85.8);
        node.add(second);
        consumer.accept(node);
        INode accumulatedNode = consumer.getAccumulatedNode(node);
        Assertions.assertEquals(0, node.getChildren().size());
        Assertions.assertEquals(1.0f, accumulatedNode.getCorrectSemanticScore(), 0.0001f);
    }

    @Test
    public void acceptNodeWithNoChildrenTest() {
        SemanticParagraph node = new SemanticParagraph();
        TextChunk firstLine = new TextChunk();
        firstLine.setBoundingBox(new BoundingBox(new double[] {100, 100, 500, 50}));
        TextChunk lastLine = new TextChunk();
        lastLine.setBoundingBox(new BoundingBox(new double[] {100, 150, 500, 50}));
        node.setFirstLine(firstLine);
        node.setLastLine(lastLine);
        consumer.accept(node);
        INode accumulatedNode = consumer.getAccumulatedNode(node);
        Assertions.assertEquals(0, node.getChildren().size());
        Assertions.assertEquals(1.0f, accumulatedNode.getCorrectSemanticScore(), 0.0001f);
    }

    @Test
    public void toParagraphMergeProbabilityNoCgildrenTest() {
        SemanticParagraph paragraph = new SemanticParagraph();
        TextChunk first = createTextChunk("the goal ", "Calibri", 9.96,
                                          400, 0, 85.79, new double[] {0},
                                          new double[] {70.8, 82.7, 109.4, 96.0});

        TextChunk second = createTextChunk("of enhancing the visual experience. In such a case, the background is not required for understanding the ",
                                           "Calibri", 9.96,
                                           400, 0, 85.79, new double[] {0},
                                           new double[] {110.4, 82.7, 531.4, 96.0});
        paragraph.setFirstLine(first);
        paragraph.setLastLine(second);

        consumer.accept(paragraph);
        INode accumulatedNode = consumer.getAccumulatedNode(paragraph);
        Assertions.assertEquals(0, paragraph.getChildren().size());
        Assertions.assertEquals(1.0f, accumulatedNode.getCorrectSemanticScore(), 0.0001f);

    }

    @Test
    public void isOneLineParagraphFailTest() {
        AccumulatedNodeConsumer consumer =  new AccumulatedNodeConsumer();
        SemanticParagraph node = new SemanticParagraph();
        TextChunk firstLine = new TextChunk();
        firstLine.setBoundingBox(new BoundingBox(new double[] {100, 100, 500, 50}));
        TextChunk lastLine = new TextChunk();
        lastLine.setBoundingBox(new BoundingBox(new double[] {100, 150, 500, 50}));
        node.setFirstLine(firstLine);
        node.setLastLine(lastLine);
        Assertions.assertFalse(consumer.isOneLineParagraph(node));
    }
    @Test
    public void isOneLineParagraphPassTest() {
        AccumulatedNodeConsumer consumer =  new AccumulatedNodeConsumer();
        SemanticParagraph node = new SemanticParagraph();
        TextChunk firstLine = new TextChunk();
        firstLine.setBoundingBox(new BoundingBox(new double[] {100, 100, 500, 50}));
        node.setFirstLine(firstLine);
        node.setLastLine(firstLine);
        Assertions.assertTrue(consumer.isOneLineParagraph(node));
    }

    @Test
    public void isOneLineParagraphNoLastLineFailTest() {
        AccumulatedNodeConsumer consumer =  new AccumulatedNodeConsumer();
        SemanticParagraph node = new SemanticParagraph();
        TextChunk firstLine = new TextChunk();
        firstLine.setBoundingBox(new BoundingBox(new double[] {100, 100, 500, 50}));
        node.setFirstLine(firstLine);
        Assertions.assertFalse(consumer.isOneLineParagraph(node));
    }

    public TextChunk createTextChunk(String text, String fontName, double fontSize, double fontWeight,
                                     double italicAngle, double baseLine, double[] fontColor, double[] boundingBox) {
        TextChunk result = new TextChunk();
        if (text != null) {
            result.setValue(text);
        }
        if (fontName != null) {
            result.setFontName(fontName);
        }
        if (fontSize >= 0.0001f) {
            result.setFontSize(fontSize);
        }
        if (fontWeight >= 0.0001f) {
            result.setFontWeight(fontWeight);
        }
        result.setItalicAngle(italicAngle);
        result.setBaseLine(baseLine);
        result.setFontColor(fontColor);
        result.setBoundingBox(new BoundingBox(boundingBox));
        return result;
    }
}
