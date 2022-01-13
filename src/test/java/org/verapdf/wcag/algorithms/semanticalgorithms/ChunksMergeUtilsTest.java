package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.verapdf.wcag.algorithms.entities.SemanticTextNode;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ChunksMergeUtils;

public class ChunksMergeUtilsTest {

    @Test
    public void toChunkMergeProbabilityDifferentFontsTest() {
        TextChunk first = createTextChunk("the goa", "Calibri", 9.96,
                                                  400, 0, 85.79, new double[] {0},
                                                  new double[] {70.8, 82.7, 101.4, 96.0}, 0, 0);

        TextChunk second = createTextChunk("l of enhancing the visual experience. In such a case, the background is not required for understanding the ",
                                                           "Times New Roman", 9.96,
                                                           400, 0, 85.79, new double[] {0},
                                                           new double[] {101.4, 82.7, 531.4, 96.0}, 0, 0);

        double resultProbability = ChunksMergeUtils.toChunkMergeProbability(first, second);
        Assertions.assertEquals(0.0, resultProbability, 0.0001);
    }

    @Test
    public void toChunkMergeProbabilityDifferentFontSizeTest() {
        TextChunk first = createTextChunk("the goa", "Calibri", 9.96,
                                                          400, 0, 85.79, new double[] {0},
                                                          new double[] {70.8, 82.7, 101.4, 96.0}, 0, 0);

        TextChunk second = createTextChunk("l of enhancing the visual experience. In such a case, the background is not required for understanding the ",
                                                           "Times New Roman", 10.96,
                                                           400, 0, 85.79, new double[] {0},
                                                           new double[] {101.4, 82.7, 531.4, 96.0}, 0, 0);

        double resultProbability = ChunksMergeUtils.toChunkMergeProbability(first, second);
        Assertions.assertEquals(0.0, resultProbability, 0.0001);
    }

    @Test
    public void toChunkMergeProbabilityPassTest() {
        TextChunk first = createTextChunk("the goa", "Calibri", 9.96,
                400, 0, 85.79, new double[] {0},
                new double[] {70.8, 82.7, 101.4, 96.0}, 0, 0);

        TextChunk second = createTextChunk("l of enhancing the visual experience. In such a case, the background is not required for understanding the ",
                                                           "Calibri", 9.96,
                                                           400, 0, 85.79, new double[] {0},
                                                           new double[] {101.4, 82.7, 531.4, 96.0}, 0, 0);

        double resultProbability = ChunksMergeUtils.toChunkMergeProbability(first, second);
        Assertions.assertEquals(1.0, resultProbability, 0.0001);
    }

    @Test
    public void toChunkMergeProbabilityDifferentColorTest() {
        TextChunk first =  createTextChunk("the goa", "Calibri", 9.96,
                                                           400, 0, 85.79, new double[] {0},
                                                           new double[] {70.8, 82.7, 101.4, 96.0}, 0, 0);

        TextChunk second = createTextChunk("l of enhancing the visual experience. In such a case, the background is not required for understanding the ",
                                                           "Calibri", 9.96,
                                                           400, 0, 85.79, new double[] {1},
                                                           new double[] {101.4, 82.7, 531.4, 96.0}, 0, 0);

        double resultProbability = ChunksMergeUtils.toChunkMergeProbability(first, second);
        Assertions.assertEquals(0.0, resultProbability, 0.0001);
    }

    @Test
    public void toLineMergeProbabilityTest() {
        TextChunk first =  createTextChunk("the goa", "Calibri", 9.96,
                                                           400, 0, 85.79, new double[] {0},
                                                           new double[] {70.8, 82.7, 101.4, 96.0}, 0, 0);

        TextChunk second = createTextChunk("l of enhancing the visual experience. In such a case, the background is not required for understanding the ",
                                                           "Calibri", 9.96,
                                                           400, 0, 85.79, new double[] {0},
                                                           new double[] {101.4, 82.7, 531.4, 96.0}, 0, 0);
        double resultProbability = ChunksMergeUtils.countOneLineProbability(new SemanticTextNode(), new TextLine(first),
                                                                            new TextLine(second));
        double secondResultProbability = ChunksMergeUtils.toLineMergeProbability(first, second);
        Assertions.assertEquals(1.0, resultProbability, 0.0001);
        Assertions.assertEquals(1.0, secondResultProbability, 0.0001);
    }

    @Test
    public void toParagraphMergeProbabilityTest() {
        TextLine first = new TextLine(createTextChunk("the goa", "Calibri", 9.96,
                                                           400, 0, 85.79, new double[] {0},
                                                           new double[] {70.8, 82.7, 101.4, 96.0}, 0, 0));

        TextLine second = new TextLine(createTextChunk("l of enhancing the visual experience. In such a case, the background is not required for understanding the ",
                                                      "Calibri", 9.96,
                                                      400, 0, 85.79, new double[] {0},
                                                      new double[] {70.8, 102.7, 500.4, 116.0}, 0, 0));

        double resultProbability = ChunksMergeUtils.toParagraphMergeProbability(first, second);
        Assertions.assertEquals(0.8, resultProbability, 0.0001);
    }

    @Test
    public void mergeLeadingProbabilityDifferentFontSizeTest() {
        TextLine first = new TextLine(createTextChunk("the goa", "Calibri", 8.96,
                                                          400, 0, 85.79, new double[] {0},
                                                          new double[] {70.8, 82.7, 101.4, 96.0}, 0, 0));

        TextLine second = new TextLine(createTextChunk("l of enhancing the visual experience. In such a case, the background is not required for understanding the ",
                                                           "Calibri", 11.96,
                                                           400, 0, 85.79, new double[] {0},
                                                           new double[] {70.8, 102.7, 500.4, 116.0}, 0, 0));

        double resultProbability = ChunksMergeUtils.toParagraphMergeProbability(first, second);
        Assertions.assertEquals(0.0, resultProbability, 0.0001);
    }

    @Test
    public void mergeByCharSpacingProbabilityLastCharIsWhitespaceTest() {
        TextChunk first = createTextChunk("the goal ", "Calibri", 9.96,
                                                          400, 0, 85.79, new double[] {0},
                                                          new double[] {70.8, 82.7, 109.4, 96.0}, 0, 0);

        TextChunk second = createTextChunk("of enhancing the visual experience. In such a case, the background is not required for understanding the ",
                                                           "Calibri", 9.96,
                                                           400, 0, 85.79, new double[] {0},
                                                           new double[] {109.4, 82.7, 531.4, 96.0}, 0, 0);

        double resultProbability = ChunksMergeUtils.countOneLineProbability(new SemanticTextNode(), new TextLine(first),
                                                                            new TextLine(second));
        double secondResultProbability = ChunksMergeUtils.toLineMergeProbability(first, second);
        Assertions.assertEquals(1.0, resultProbability, 0.0001);
        Assertions.assertEquals(1.0, secondResultProbability, 0.0001);
    }

    @Test
    public void mergeByCharSpacingProbabilityLastCharIsWhitespaceMediumSpacingBetweenChunksTest() {
        TextChunk first = createTextChunk("the goal ", "Calibri", 9.96,
                                                          400, 0, 85.79, new double[] {0},
                                                          new double[] {70.8, 82.7, 109.4, 96.0}, 0, 0);

        TextChunk second = createTextChunk("of enhancing the visual experience. In such a case, the background is not required for understanding the ",
                                                           "Calibri", 9.96,
                                                           400, 0, 85.79, new double[] {0},
                                                           new double[] {110.4, 82.7, 531.4, 96.0}, 0, 0);

        double resultProbability = ChunksMergeUtils.countOneLineProbability(new SemanticTextNode(), new TextLine(first),
                                                                            new TextLine(second));
        double secondResultProbability = ChunksMergeUtils.toLineMergeProbability(first, second);
        Assertions.assertEquals(1.0, resultProbability, 0.0001);
        Assertions.assertEquals(1.0, secondResultProbability, 0.0001);
    }

    @Test
    public void mergeByCharSpacingProbabilityFirstCharIsWhitespaceTest() {
        TextChunk first = createTextChunk("the goal", "Calibri", 9.96,
                                                          400, 0, 85.79, new double[] {0},
                                                          new double[] {70.8, 82.7, 105.4, 96.0}, 0, 0);

        TextChunk second = createTextChunk(" of enhancing the visual experience. In such a case, the background is not required for understanding the ",
                                                           "Calibri", 9.96,
                                                           400, 0, 85.79, new double[] {0},
                                                           new double[] {105.4, 82.7, 531.4, 96.0}, 0, 0);

        double resultProbability = ChunksMergeUtils.countOneLineProbability(new SemanticTextNode(), new TextLine(first),
                                                                            new TextLine(second));
        double secondResultProbability = ChunksMergeUtils.toLineMergeProbability(first, second);
        Assertions.assertEquals(1.0, resultProbability, 0.0001);
        Assertions.assertEquals(1.0, secondResultProbability, 0.0001);
    }

    public TextChunk createTextChunk(String text, String fontName, double fontSize, double fontWeight,
                                                     double italicAngle, double baseLine, double[] fontColor, double[] boundingBox, int pageNumber, int lastPageNumber) {
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
        result.setBoundingBox(new BoundingBox(pageNumber, lastPageNumber, boundingBox));
        return result;
    }
}
