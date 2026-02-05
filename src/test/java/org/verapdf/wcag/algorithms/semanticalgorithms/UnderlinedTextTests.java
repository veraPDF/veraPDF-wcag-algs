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
