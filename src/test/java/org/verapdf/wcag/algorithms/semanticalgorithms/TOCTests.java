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
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.JsonToPdfTree;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.*;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ErrorCodes;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.verapdf.wcag.algorithms.semanticalgorithms.TableCheckingTests.getErrorMessage;

public class TOCTests {

    private static final Set<Integer> checkedErrorCodes = new HashSet<>();

    static {
        checkedErrorCodes.add(ErrorCodes.ERROR_CODE_1000);
        checkedErrorCodes.add(ErrorCodes.ERROR_CODE_1001);
        checkedErrorCodes.add(ErrorCodes.ERROR_CODE_1002);
        checkedErrorCodes.add(ErrorCodes.ERROR_CODE_1003);
        checkedErrorCodes.add(ErrorCodes.ERROR_CODE_1004);
        checkedErrorCodes.add(ErrorCodes.ERROR_CODE_1005);
        checkedErrorCodes.add(ErrorCodes.ERROR_CODE_1006);
        checkedErrorCodes.add(ErrorCodes.ERROR_CODE_1007);
        checkedErrorCodes.add(ErrorCodes.ERROR_CODE_1008);
        checkedErrorCodes.add(ErrorCodes.ERROR_CODE_1009);
        checkedErrorCodes.add(ErrorCodes.ERROR_CODE_1010);
    }

    static Stream<Arguments> TOCDetectionTestParams() {
        return Stream.of(
                Arguments.of("libra_table_of_content.json", null),
                Arguments.of("Word_Table_of_Contents.json", new Integer[][]{{ErrorCodes.ERROR_CODE_1007}, {ErrorCodes.ERROR_CODE_1000}})
                );
    }

    @ParameterizedTest(name = "{index}: ({0}, {1}) => {0}")
    @MethodSource("TOCDetectionTestParams")
    void testTOCDetection(String filename, Integer[][] errorCodes) throws IOException {
        IDocument document = JsonToPdfTree.getDocument("/files/TOC/" + filename);
        ITree tree = document.getTree();
        StaticContainers.updateContainers(document);

        Consumer<INode> semanticDocumentValidator = new SemanticDocumentPreprocessingConsumer();
        tree.forEach(semanticDocumentValidator);

        AccumulatedNodeConsumer semanticDetectionValidator = new AccumulatedNodeConsumer();
        tree.forEach(semanticDetectionValidator);

        HeadingCaptionConsumer headingCaptionValidator = new HeadingCaptionConsumer();
        tree.forEach(headingCaptionValidator);

        TOCDetectionConsumer tocDetectionConsumer = new TOCDetectionConsumer();
        tree.forEach(tocDetectionConsumer);

        if (errorCodes == null) {
            testTOCInitialTreeStructure(tree);
        } else {
            int index = 0;
            for (INode node : tree) {
                if (node.getInitialSemanticType() == SemanticType.TABLE_OF_CONTENT_ITEM) {
                    Set<Integer> foundErrorCodes = getFoundErrorCodes(node);
                    if (!foundErrorCodes.isEmpty()) {
                        Assertions.assertNotEquals(errorCodes.length, index, getErrorMessage(foundErrorCodes, null));
                        Assertions.assertEquals(errorCodes[index].length, foundErrorCodes.size(),
                                getErrorMessage(foundErrorCodes, errorCodes[index]));
                        for (Integer errorCode : errorCodes[index]) {
                            Assertions.assertTrue(foundErrorCodes.contains(errorCode),
                                    getErrorMessage(foundErrorCodes, errorCodes[index]));
                        }
                        index++;
                    }
                }
            }
            Assertions.assertEquals(errorCodes.length, index,
                    getErrorMessage(null, index < errorCodes.length ? errorCodes[index] : null));
        }
    }

    private void testTOCInitialTreeStructure(ITree tree) {
        for (INode node : tree) {
            if (node.getInitialSemanticType() == SemanticType.TABLE_OF_CONTENT_ITEM) {
                Assertions.assertEquals(node.getInitialSemanticType(), node.getSemanticType());
            }
        }
    }

    private static Set<Integer> getFoundErrorCodes(INode node) {
        return TableCheckingTests.getFoundErrorCodes(node, checkedErrorCodes);
    }
}
