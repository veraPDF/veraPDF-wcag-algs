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
import org.verapdf.wcag.algorithms.entities.*;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextColumn;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.TextFormat;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.AccumulatedNodeConsumer;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.SemanticDocumentPreprocessingConsumer;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.stream.Stream;

class SubscriptSuperscriptTest {

	static Stream<Arguments> subscriptSuperscriptTestParams() {
		return Stream.of(
				Arguments.of("subscriptSuperscript/1.json",
						new TextFormat[]{TextFormat.NORMAL, TextFormat.SUBSCRIPT, TextFormat.SUPERSCRIPT,
								TextFormat.NORMAL, TextFormat.NORMAL, TextFormat.SUPERSCRIPT, TextFormat.SUBSCRIPT,
								TextFormat.NORMAL, TextFormat.NORMAL},
						new TextFormat[]{TextFormat.NORMAL, TextFormat.SUBSCRIPT, TextFormat.SUPERSCRIPT,
								TextFormat.NORMAL, TextFormat.SUPERSCRIPT, TextFormat.SUBSCRIPT}),
				Arguments.of("subscriptSuperscript/2.json",
						new TextFormat[]{TextFormat.NORMAL, TextFormat.SUPERSCRIPT, TextFormat.NORMAL, TextFormat.NORMAL,
								TextFormat.SUBSCRIPT, TextFormat.NORMAL, TextFormat.NORMAL, TextFormat.NORMAL},
						new TextFormat[]{TextFormat.NORMAL, TextFormat.SUPERSCRIPT, TextFormat.NORMAL, TextFormat.SUBSCRIPT}),
				Arguments.of("subscriptSuperscript/3.json",
						new TextFormat[]{TextFormat.NORMAL, TextFormat.SUPERSCRIPT, TextFormat.SUBSCRIPT,
								TextFormat.NORMAL, TextFormat.NORMAL, TextFormat.SUBSCRIPT, TextFormat.SUPERSCRIPT,
								TextFormat.NORMAL, TextFormat.NORMAL},
						new TextFormat[]{TextFormat.NORMAL, TextFormat.SUPERSCRIPT, TextFormat.SUBSCRIPT,
								TextFormat.NORMAL, TextFormat.SUBSCRIPT, TextFormat.SUPERSCRIPT}),
				Arguments.of("superscript/superscript1.json",
						new TextFormat[]{TextFormat.SUPERSCRIPT, TextFormat.NORMAL},
						new TextFormat[]{TextFormat.NORMAL, TextFormat.SUPERSCRIPT, TextFormat.NORMAL}),
				Arguments.of("superscript/superscript2.json",
						new TextFormat[]{TextFormat.SUPERSCRIPT, TextFormat.NORMAL},
						new TextFormat[]{TextFormat.NORMAL, TextFormat.SUPERSCRIPT, TextFormat.SUPERSCRIPT,
								TextFormat.NORMAL}),
				Arguments.of("superscript/superscript3.json",
						new TextFormat[]{TextFormat.SUPERSCRIPT, TextFormat.SUPERSCRIPT, TextFormat.NORMAL},
						new TextFormat[]{TextFormat.NORMAL, TextFormat.SUPERSCRIPT, TextFormat.SUPERSCRIPT,
								TextFormat.NORMAL}),
				Arguments.of("superscript/superscript4.json",
						new TextFormat[]{TextFormat.SUPERSCRIPT, TextFormat.NORMAL},
						new TextFormat[]{TextFormat.NORMAL, TextFormat.SUPERSCRIPT, TextFormat.NORMAL}),
				Arguments.of("superscript/superscript5.json",
						new TextFormat[]{TextFormat.NORMAL, TextFormat.NORMAL},
						new TextFormat[]{TextFormat.SUPERSCRIPT, TextFormat.NORMAL}),
				Arguments.of("superscript/superscript6.json",
						new TextFormat[]{TextFormat.NORMAL},
						new TextFormat[]{TextFormat.NORMAL, TextFormat.SUPERSCRIPT, TextFormat.NORMAL})
		);
	}

	@ParameterizedTest(name = "{index}: ({0}, {1}) => {0}")
	@MethodSource("subscriptSuperscriptTestParams")
	void testSubscriptSuperscriptDetection(String filename, TextFormat[] nodeChecks, TextFormat[] chunkChecks) throws IOException {
		IDocument document = JsonToPdfTree.getDocument("/files/" + filename);
		ITree tree = document.getTree();

		StaticContainers.updateContainers(document);

		Consumer<INode> semanticDocumentValidator = new SemanticDocumentPreprocessingConsumer();
		tree.forEach(semanticDocumentValidator);

		AccumulatedNodeConsumer paragraphValidator = new AccumulatedNodeConsumer();
		tree.forEach(paragraphValidator);

		checkSubscriptSuperscript(tree, nodeChecks, chunkChecks);
	}

	private void checkSubscriptSuperscript(ITree tree, TextFormat[] nodeChecks, TextFormat[] chunkChecks) {
		int count = 0;
		int countChunks = 0;
		for (INode node : tree) {
			INode accumulatedNode = StaticContainers.getAccumulatedNodeMapper().get(node);
			if (node instanceof SemanticTextNode) {
				for (TextColumn column : ((SemanticTextNode)node).getColumns()) {
					for (TextLine line : column.getLines()) {
						for (TextChunk chunk : line.getTextChunks()) {
							Assertions.assertEquals(chunkChecks[countChunks], chunk.getTextFormat(),
									"Chunk " + countChunks + " has wrong format");
							countChunks++;
						}
					}
				}
			} else if (accumulatedNode instanceof SemanticTextNode) {
				SemanticTextNode textNode = ((SemanticTextNode) accumulatedNode);
				Assertions.assertEquals(nodeChecks[count], textNode.getTextFormat(),
						 "Node " + count + " has wrong format");
				count++;
			}
		}
		Assertions.assertEquals(chunkChecks.length, countChunks);
		Assertions.assertEquals(nodeChecks.length, count);
	}
}
