package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticTextNode;
import org.verapdf.wcag.algorithms.entities.content.LineChunk;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;

import java.util.function.Consumer;

public class NodeUtilsTest {

    private static final double[] GRAY = new double[]{0.1, 0.1, 0.1};

    @BeforeEach
    public void setUp() {
        StaticContainers.setIsDataLoader(true);
        StaticContainers.setIsIgnoreCharactersWithoutUnicode(true);
    }

    @Test
    public void headingProbabilitySameFontRewardsEmphasis() {
        TestSemanticTextNode heading = createTextNode("TITLE", "TestFont", 14.0, 700.0, GRAY);
        TestSemanticTextNode body = createTextNode("body text", "TestFont", 10.0, 400.0, GRAY);

        double probability = NodeUtils.headingProbability(heading, body, true);

        Assertions.assertEquals(1.35, probability, 0.0001);
    }

    @Test
    public void headingProbabilityPenalizesSmallerText() {
        TestSemanticTextNode candidate = createTextNode("body text", "TestFont", 10.0, 300.0, GRAY);
        TestSemanticTextNode dominantNeighbor = createTextNode("body text neighbor", "TestFont", 14.0, 700.0, GRAY);

        double probability = NodeUtils.headingProbability(candidate, dominantNeighbor, true);

        Assertions.assertEquals(0, probability, 0.0001);
    }

    @Test
    public void headingProbabilityDifferentFontUsesAlternativeWeights() {
        TestSemanticTextNode candidate = createTextNode("TITLE", "HeadingFont", 14.0, 700.0, GRAY);
        TestSemanticTextNode neighbor = createTextNode("body text", "BodyFont", 10.0, 400.0, GRAY);

        double probability = NodeUtils.headingProbability(candidate, neighbor, true);

        Assertions.assertEquals(1.09, probability, 0.0001);
    }

    @Test
    public void headingProbabilityReturnsZeroWhenPreviousHeadingWithoutNext() {
        TestSemanticTextNode candidate = createTextNode("Heading", "TestFont", 12.0, 500.0, GRAY);
        TestSemanticTextNode previousHeading = createTextNode("Previous", "TestFont", 11.0, 400.0, GRAY);
        previousHeading.setSemanticType(SemanticType.HEADING);

        double probability = NodeUtils.headingProbability(candidate, previousHeading, null, candidate);

        Assertions.assertEquals(0.0, probability, 0.0001);
    }

    @Test
    public void headingProbabilityClampsAggregatedBonuses() {
        TestSemanticTextNode candidate = createTextNode("1 Heading", "TestFont", 14.0, 700.0, GRAY,
                SemanticType.HEADING, null);
        TestSemanticTextNode previous = createTextNode("body text", "TestFont", 10.0, 400.0, GRAY);
        TestSemanticTextNode next = createTextNode("body text", "TestFont", 10.0, 400.0, GRAY);

        double probability = NodeUtils.headingProbability(candidate, previous, next, candidate);

        Assertions.assertEquals(1.0, probability, 0.0001);
    }

    @Test
    public void headingProbabilityAddsStartLineBonusWhenLastLineOpen() {
        TestSemanticTextNode candidate = createTextNode("Paragraph", "TestFont", 12.0, 400.0, GRAY,
                null, TextLine::setNotLineEnd);
        TestSemanticTextNode previous = createTextNode("Paragraph", "TestFont", 12.0, 400.0, GRAY);
        TestSemanticTextNode next = createTextNode("Paragraph", "TestFont", 12.0, 400.0, GRAY);

        double probability = NodeUtils.headingProbability(candidate, previous, next, candidate);

        Assertions.assertEquals(0.0, probability, 0.0001);
    }

    @Test
    public void headingProbabilityPenalizesBrokenBoundaries() {
        TestSemanticTextNode candidate = new TestSemanticTextNode();
        candidate.add(createLine("first", "TestFont", 12.0, 400.0, GRAY, TextLine::setNotLineStart));
        candidate.add(createLine("second", "TestFont", 12.0, 400.0, GRAY, TextLine::setNotLineEnd));
        TestSemanticTextNode previous = createTextNode("first", "TestFont", 12.0, 400.0, GRAY);
        TestSemanticTextNode next = createTextNode("second", "TestFont", 12.0, 400.0, GRAY);

        double probability = NodeUtils.headingProbability(candidate, previous, next, candidate);

        Assertions.assertEquals(0.0, probability, 0.0001);
    }

    @Test
    public void headingProbabilitySubtractsForCrossPageNeighbor() {
        double[] blue = new double[]{0.2, 0.2, 0.2};
        TestSemanticTextNode candidate = createTextNode("1 Heading", "TestFont", 12.0, 400.0, GRAY);
        TestSemanticTextNode previous = createTextNode("1 Heading", "TestFont", 12.0, 400.0, blue);
        TestSemanticTextNode next = createTextNode("1 Heading", "TestFont", 12.0, 400.0, blue);
        candidate.setNextNode(next);
        candidate.setPageNumber(1);
        previous.setPageNumber(1);
        next.setPageNumber(2);

        double probability = NodeUtils.headingProbability(candidate, previous, next, candidate);

        Assertions.assertEquals(0.1, probability, 0.0001);
    }

    @Test
    public void hasSameStyleRespectsEpsilons() {
        TestSemanticTextNode base = createTextNode("Body", "TestFont", 12.0, 400.0, GRAY);
        TestSemanticTextNode similar = createTextNode("Body", "TestFont", 12.04, 400.04, GRAY);

        boolean result = NodeUtils.hasSameStyle(base, similar, 0.05, 0.05);

        Assertions.assertTrue(result);
    }

    @Test
    public void hasSameStyleDetectsUppercaseDifference() {
        TestSemanticTextNode uppercase = createTextNode("TITLE", "TestFont", 12.0, 400.0, GRAY);
        TestSemanticTextNode capitalized = createTextNode("Title", "TestFont", 12.0, 400.0, GRAY);

        boolean result = NodeUtils.hasSameStyle(uppercase, capitalized, 0.05, 0.05);

        Assertions.assertFalse(result);
    }

    @Test
    public void areOverlappingReturnsTrueForLargeOverlap() {
        TextChunk textChunk = createTextChunk("text", 0.0, 0.0, 120.0, 12.0);
        LineChunk lineChunk = new LineChunk(1, 5.0, 0.0, 115.0, 0.0, 1.0);

        boolean result = NodeUtils.areOverlapping(textChunk, lineChunk);

        Assertions.assertTrue(result);
    }

    @Test
    public void areOverlappingReturnsFalseWhenLineIsFar() {
        TextChunk textChunk = createTextChunk("text", 0.0, 0.0, 120.0, 12.0);
        LineChunk lineChunk = new LineChunk(1, 200.0, 0.0, 210.0, 0.0, 1.0);

        boolean result = NodeUtils.areOverlapping(textChunk, lineChunk);

        Assertions.assertFalse(result);
    }

    @Test
    public void hasSimilarBackgroundColorWithinTolerance() {
        double[] first = new double[]{0.5, 0.5, 0.5};
        double[] second = new double[]{0.52, 0.48, 0.5};

        boolean result = NodeUtils.hasSimilarBackgroundColor(first, second);

        Assertions.assertTrue(result);
    }

    @Test
    public void hasSimilarBackgroundColorOutsideTolerance() {
        double[] first = new double[]{0.5, 0.5, 0.5};
        double[] second = new double[]{0.55, 0.5, 0.5};

        boolean result = NodeUtils.hasSimilarBackgroundColor(first, second);

        Assertions.assertFalse(result);
    }

    private TestSemanticTextNode createTextNode(String value, String fontName, double fontSize, double fontWeight,
                                                double[] color) {
        return createTextNode(value, fontName, fontSize, fontWeight, color, null, null);
    }

    private TestSemanticTextNode createTextNode(String value, String fontName, double fontSize, double fontWeight,
                                                double[] color, SemanticType initialType, Consumer<TextLine> lineCustomizer) {
        TestSemanticTextNode node = initialType == null ? new TestSemanticTextNode() : new TestSemanticTextNode(initialType);
        TextLine line = createLine(value, fontName, fontSize, fontWeight, color, lineCustomizer);
        node.add(line);
        return node;
    }

    private TextLine createLine(String value, String fontName, double fontSize, double fontWeight,
                                double[] color, Consumer<TextLine> lineCustomizer) {
        TextChunk chunk = createTextChunk(value, 0.0, 0.0,
                Math.max(fontSize * Math.max(1, value.length()), fontSize), fontSize);
        chunk.setFontName(fontName);
        chunk.setFontWeight(fontWeight);
        chunk.setFontSize(fontSize);
        if (color != null) {
            chunk.setFontColor(color.clone());
        }
        TextLine line = new TextLine(chunk);
        if (lineCustomizer != null) {
            lineCustomizer.accept(line);
        }
        return line;
    }

    private TextChunk createTextChunk(String value, double left, double bottom, double right, double top) {
        BoundingBox boundingBox = new BoundingBox(1, 1, new double[]{left, bottom, right, top});
        return new TextChunk(boundingBox, value, "TestFont", top - bottom, 400.0, 0.0, bottom, GRAY, 0.0);
    }

    private static class TestSemanticTextNode extends SemanticTextNode {

        private INode nextNode;
        private INode previousNode;

        TestSemanticTextNode() {
            super();
        }

        TestSemanticTextNode(SemanticType initialSemanticType) {
            super(initialSemanticType);
        }

        void setNextNode(INode nextNode) {
            this.nextNode = nextNode;
        }

        void setPreviousNode(INode previousNode) {
            this.previousNode = previousNode;
        }

        @Override
        public INode getNextNode() {
            return nextNode;
        }

        @Override
        public INode getPreviousNode() {
            return previousNode;
        }
    }
}
