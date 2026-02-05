/*
 * This file is part of veraPDF wcag algorithms, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF wcag algorithms is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF wcag algorithms as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF wcag algorithms as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
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
        Assertions.assertEquals(0.3, resultProbability, 0.0001);
    }

    @Test
    public void mergeLeadingProbabilityDifferentFontSizeTest() {
        TextLine first = new TextLine(createTextChunk("the goa", "Calibri", 8.96,
                                                          400, 0, 85.79, new double[] {0},
                                                          new double[] {70.8, 82.7, 101.4, 96.0}, 0, 0));

        TextLine second = new TextLine(createTextChunk("l of enhancing the visual experience. In such a case, the background is not required for understanding the ",
                                                           "Calibri", 12.96,
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
