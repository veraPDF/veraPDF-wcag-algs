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
package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.verapdf.wcag.algorithms.entities.SemanticTextNode;
import org.verapdf.wcag.algorithms.entities.content.LineChunk;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

public class NodeUtilsTest {

    private static final double HEADING_PROBABILITY = 0.75;
    private static final double[] GRAY = new double[]{0.1, 0.1, 0.1};

    @BeforeEach
    public void setUp() {
        StaticContainers.setIsDataLoader(true);
        StaticContainers.setIsIgnoreCharactersWithoutUnicode(true);
    }

    @Test
    public void headingProbabilitySameFontRewardsEmphasis() {
        double[] curBBox = {0, 100, 100, 115};
        double[] nextBBox = {0, 85, 100, 95};
        SemanticTextNode cur = createTextNode("Heading", 15.0, 700.0, curBBox);
        SemanticTextNode next = createTextNode("Paragraph", 10.0, 400.0, nextBBox);

        double probability = NodeUtils.headingProbability(cur, next, true);

        assertThat(probability).isGreaterThanOrEqualTo(HEADING_PROBABILITY);
    }

    @Test
    public void headingProbabilityIsolatedWithBold() {
        double[] prevBBox = {0, 120, 100, 130};
        double[] curBBox = {0, 100, 80, 110};
        double[] nextBBox = {0, 80, 100, 90};
        SemanticTextNode prev = createTextNode("Paragraph", 10.0, 400.0, prevBBox);
        SemanticTextNode cur = createTextNode("Heading", 10.0, 700.0, curBBox);
        SemanticTextNode next = createTextNode("Paragraph", 10.0, 400.0, nextBBox);

        double probability = NodeUtils.headingProbability(cur, prev, next, cur);

        assertThat(probability).isGreaterThanOrEqualTo(HEADING_PROBABILITY);
    }

    @Test
    public void headingProbabilityPenalizesSmallerText() {
        double[] curBBox = {0, 100, 100, 110};
        double[] nextBBox = {0, 80, 100, 94};
        SemanticTextNode cur = createTextNode("Paragraph", 10.0, 400.0, curBBox);
        SemanticTextNode next = createTextNode("Heading", 14.0, 400.0, nextBBox);

        double probability = NodeUtils.headingProbability(cur, next, true);

        assertThat(probability).isLessThanOrEqualTo(HEADING_PROBABILITY);
    }

    @Test
    public void headingProbabilityDifferentFontWithBold() {
        double[] curBBox = {0, 100, 80, 112};
        double[] nextBBox = {0, 85, 100, 95};
        SemanticTextNode cur = createTextNode("Heading", 12.0, 700.0, curBBox, GRAY, "HeadingFont", null);
        SemanticTextNode next = createTextNode("Paragraph", 10.0, 400.0, nextBBox, GRAY, "BodyFont", null);

        double probability = NodeUtils.headingProbability(cur, next, true);

        assertThat(probability).isGreaterThanOrEqualTo(HEADING_PROBABILITY);
    }

    @Test
    public void headingProbabilityReturnsZeroWhenPreviousHeadingWithoutNext() {
        double[] prevBBox = {0, 120, 100, 130};
        double[] curBBox = {0, 100, 100, 110};
        SemanticTextNode prev = createTextNode("Heading", 14.0, 700.0, prevBBox);
        SemanticTextNode cur = createTextNode("Paragraph", 10.0, 400.0, curBBox);

        double probability = NodeUtils.headingProbability(cur, prev, null, cur);

        assertThat(probability).isCloseTo(0.0, offset(1e-9));
    }

    @Test
    public void headingProbabilityClampsAggregatedBonuses() {
        double[] prevBBox = {0, 120, 100, 130};
        double[] curBBox = {0, 100, 100, 115};
        double[] nextBBox = {0, 85, 100, 95};
        double[] lighterGray = {0.8, 0.8, 0.8};
        SemanticTextNode prev = createTextNode("Paragraph", 10.0, 400.0, prevBBox, lighterGray, "TestFont", null);
        SemanticTextNode cur = createTextNode("Heading", 15.0, 700.0, curBBox);
        SemanticTextNode next = createTextNode("Paragraph", 10.0, 400.0, nextBBox, lighterGray, "TestFont", null);

        double probability = NodeUtils.headingProbability(cur, prev, next, cur);

        assertThat(probability).isGreaterThanOrEqualTo(HEADING_PROBABILITY);
    }

    @Test
    public void hasSameStyleRespectsEpsilons() {
        double[] bbox = {0, 100, 80, 112};
        SemanticTextNode node = createTextNode("Text", 12.0, 600.0, bbox);
        SemanticTextNode neighbor = createTextNode("Text", 12.05, 600.08, bbox);

        boolean hasSameStyle = NodeUtils.hasSameStyle(node, neighbor, 0.1, 0.1);

        assertThat(hasSameStyle).isTrue();
    }

    @Test
    public void hasSameStyleDetectsUppercaseDifference() {
        double[] bbox = {0, 120, 80, 132};
        SemanticTextNode upper = createTextNode("TITLE", 12.0, 600.0, bbox);
        SemanticTextNode mixed = createTextNode("Title", 12.0, 600.0, bbox);

        boolean hasSameStyle = NodeUtils.hasSameStyle(upper, mixed, 0.1, 0.1);

        assertThat(hasSameStyle).isFalse();
    }

    @Test
    public void areOverlappingReturnsTrueForLargeOverlap() {
        double[] chunkBBox = {0, 0, 100, 10};
        TextChunk textChunk = createTextChunk("text", 12.0, 400.0, chunkBBox, GRAY, "TestFont");
        LineChunk lineChunk = new LineChunk(1, 0, 2, 100, 2, 1.0);

        boolean overlapping = NodeUtils.areOverlapping(textChunk, lineChunk);

        assertThat(overlapping).isTrue();
    }

    @Test
    public void areOverlappingReturnsFalseWhenLineIsFar() {
        double[] chunkBBox = {0, 0, 100, 10};
        TextChunk textChunk = createTextChunk("text", 12.0, 400.0, chunkBBox, GRAY, "TestFont");
        LineChunk lineChunk = new LineChunk(1, 150, 2, 200, 2, 1.0);

        boolean overlapping = NodeUtils.areOverlapping(textChunk, lineChunk);

        assertThat(overlapping).isFalse();
    }

    @Test
    public void hasSimilarBackgroundColorWithinTolerance() {
        double[] first = {0.3, 0.3, 0.31};
        double[] second = {0.32, 0.31, 0.33};

        boolean similar = NodeUtils.hasSimilarBackgroundColor(first, second);

        assertThat(similar).isTrue();
    }

    @Test
    public void hasSimilarBackgroundColorOutsideTolerance() {
        double[] first = {0.3, 0.3, 0.3};
        double[] second = {0.4, 0.35, 0.3};

        boolean similar = NodeUtils.hasSimilarBackgroundColor(first, second);

        assertThat(similar).isFalse();
    }

    private SemanticTextNode createTextNode(String value,
                                            double fontSize,
                                            double fontWeight,
                                            double[] boundingBox) {
        return createTextNode(value, fontSize, fontWeight, boundingBox, GRAY, "TestFont", null);
    }

    private SemanticTextNode createTextNode(String value,
                                            double fontSize,
                                            double fontWeight,
                                            double[] boundingBox,
                                            double[] color,
                                            String fontName,
                                            SemanticType initialType) {
        SemanticTextNode node = initialType == null ? new SemanticTextNode() : new SemanticTextNode(initialType);
        TextLine line = createLine(value, fontSize, fontWeight, boundingBox, color, fontName);
        node.add(line);
        return node;
    }

    private TextChunk createTextChunk(String value,
                                      double fontSize,
                                      double fontWeight,
                                      double[] boundingBox,
                                      double[] color,
                                      String fontName) {
        BoundingBox chunkBBox = new BoundingBox(1, 1, boundingBox);
        return new TextChunk(chunkBBox, value, fontName, fontSize, fontWeight, 0.0, boundingBox[1], color, 0.0);
    }

    private TextLine createLine(String value,
                                double fontSize,
                                double fontWeight,
                                double[] boundingBox,
                                double[] color,
                                String fontName) {
        TextChunk chunk = createTextChunk(value, fontSize, fontWeight, boundingBox, color, fontName);
        return new TextLine(chunk);
    }
}
