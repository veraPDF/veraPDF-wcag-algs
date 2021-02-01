package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ChunksMergeUtils;

public class ChunksMergeUtilsTest {

    @Test
    public void toChunkMergeProbabilityDifferentFontsTest() {
        TextChunk first = createTextChunk("the goa", "Calibri", 9.96,
                                                  400, 0, 85.79, new double[] {0},
                                                  new double[] {70.8, 82.7, 101.4, 96.0});

        TextChunk second = createTextChunk("l of enhancing the visual experience. In such a case, the background is not required for understanding the ",
                                                           "Times New Roman", 9.96,
                                                           400, 0, 85.79, new double[] {0},
                                                           new double[] {101.4, 82.7, 531.4, 96.0});

        double resultProbability = ChunksMergeUtils.toChunkMergeProbability(first, second);
        Assertions.assertEquals(0.0, resultProbability, 0.0001);
    }

    @Test
    public void toChunkMergeProbabilityDifferentFontSizeTest() {
        TextChunk first = createTextChunk("the goa", "Calibri", 9.96,
                                                          400, 0, 85.79, new double[] {0},
                                                          new double[] {70.8, 82.7, 101.4, 96.0});

        TextChunk second = createTextChunk("l of enhancing the visual experience. In such a case, the background is not required for understanding the ",
                                                           "Times New Roman", 10.96,
                                                           400, 0, 85.79, new double[] {0},
                                                           new double[] {101.4, 82.7, 531.4, 96.0});

        double resultProbability = ChunksMergeUtils.toChunkMergeProbability(first, second);
        Assertions.assertEquals(0.0, resultProbability, 0.0001);
    }

    @Test
    public void toChunkMergeProbabilityPassTest() {
        TextChunk first = createTextChunk("the goa", "Calibri", 9.96,
                400, 0, 85.79, new double[] {0},
                new double[] {70.8, 82.7, 101.4, 96.0});

        TextChunk second = createTextChunk("l of enhancing the visual experience. In such a case, the background is not required for understanding the ",
                                                           "Calibri", 9.96,
                                                           400, 0, 85.79, new double[] {0},
                                                           new double[] {101.4, 82.7, 531.4, 96.0});

        double resultProbability = ChunksMergeUtils.toChunkMergeProbability(first, second);
        Assertions.assertEquals(1.0, resultProbability, 0.0001);
    }

    @Test
    public void toChunkMergeProbabilityDifferentColorTest() {
        TextChunk first =  createTextChunk("the goa", "Calibri", 9.96,
                                                           400, 0, 85.79, new double[] {0},
                                                           new double[] {70.8, 82.7, 101.4, 96.0});

        TextChunk second = createTextChunk("l of enhancing the visual experience. In such a case, the background is not required for understanding the ",
                                                           "Calibri", 9.96,
                                                           400, 0, 85.79, new double[] {1},
                                                           new double[] {101.4, 82.7, 531.4, 96.0});

        double resultProbability = ChunksMergeUtils.toChunkMergeProbability(first, second);
        Assertions.assertEquals(0.0, resultProbability, 0.0001);
    }

    @Test
    public void toLineMergeProbabilityTest() {
        TextChunk first =  createTextChunk("the goa", "Calibri", 9.96,
                                                           400, 0, 85.79, new double[] {0},
                                                           new double[] {70.8, 82.7, 101.4, 96.0});

        TextChunk second = createTextChunk("l of enhancing the visual experience. In such a case, the background is not required for understanding the ",
                                                           "Calibri", 9.96,
                                                           400, 0, 85.79, new double[] {0},
                                                           new double[] {101.4, 82.7, 531.4, 96.0});
        double resultProbability = ChunksMergeUtils.toLineMergeProbability(first, second);
        Assertions.assertEquals(1.0, resultProbability, 0.0001);
    }

    @Test
    public void toParagraphMergeProbabilityTest() {
        TextChunk first =  createTextChunk("the goa", "Calibri", 9.96,
                                                           400, 0, 85.79, new double[] {0},
                                                           new double[] {70.8, 82.7, 101.4, 96.0});

        TextChunk second = createTextChunk("l of enhancing the visual experience. In such a case, the background is not required for understanding the ",
                                                           "Calibri", 9.96,
                                                           400, 0, 85.79, new double[] {0},
                                                           new double[] {70.8, 102.7, 500.4, 116.0});

        double resultProbability = ChunksMergeUtils.toParagraphMergeProbability(first, second);
        Assertions.assertEquals(1.0, resultProbability, 0.0001);
    }

    @Test
    public void mergeLeadingProbabilityDifferentFontSizeTest() {
        TextChunk first = createTextChunk("the goa", "Calibri", 9.96,
                                                          400, 0, 85.79, new double[] {0},
                                                          new double[] {70.8, 82.7, 101.4, 96.0});

        TextChunk second = createTextChunk("l of enhancing the visual experience. In such a case, the background is not required for understanding the ",
                                                           "Calibri", 10.96,
                                                           400, 0, 85.79, new double[] {0},
                                                           new double[] {70.8, 102.7, 500.4, 116.0});

        double resultProbability = ChunksMergeUtils.toParagraphMergeProbability(first, second);
        Assertions.assertEquals(0.0, resultProbability, 0.0001);
    }

    @Test
    public void mergeByCharSpacingProbabilityLastCharIsWhitespaceTest() {
        TextChunk first = createTextChunk("the goal ", "Calibri", 9.96,
                                                          400, 0, 85.79, new double[] {0},
                                                          new double[] {70.8, 82.7, 109.4, 96.0});

        TextChunk second = createTextChunk("of enhancing the visual experience. In such a case, the background is not required for understanding the ",
                                                           "Calibri", 9.96,
                                                           400, 0, 85.79, new double[] {0},
                                                           new double[] {109.4, 82.7, 531.4, 96.0});

        double resultProbability = ChunksMergeUtils.toLineMergeProbability(first, second);
        Assertions.assertEquals(1.0, resultProbability, 0.0001);
    }

    @Test
    public void mergeByCharSpacingProbabilityLastCharIsWhitespaceMediumSpacingBetweenChunksTest() {
        TextChunk first = createTextChunk("the goal ", "Calibri", 9.96,
                                                          400, 0, 85.79, new double[] {0},
                                                          new double[] {70.8, 82.7, 109.4, 96.0});

        TextChunk second = createTextChunk("of enhancing the visual experience. In such a case, the background is not required for understanding the ",
                                                           "Calibri", 9.96,
                                                           400, 0, 85.79, new double[] {0},
                                                           new double[] {110.4, 82.7, 531.4, 96.0});

        double resultProbability = ChunksMergeUtils.toLineMergeProbability(first, second);
        Assertions.assertEquals(0.796, resultProbability, 0.0001);
    }

    @Test
    public void mergeByCharSpacingProbabilityFirstCharIsWhitespaceTest() {
        TextChunk first = createTextChunk("the goal", "Calibri", 9.96,
                                                          400, 0, 85.79, new double[] {0},
                                                          new double[] {70.8, 82.7, 105.4, 96.0});

        TextChunk second = createTextChunk(" of enhancing the visual experience. In such a case, the background is not required for understanding the ",
                                                           "Calibri", 9.96,
                                                           400, 0, 85.79, new double[] {0},
                                                           new double[] {105.4, 82.7, 531.4, 96.0});

        double resultProbability = ChunksMergeUtils.toLineMergeProbability(first, second);
        Assertions.assertEquals(1.0, resultProbability, 0.0001);
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
