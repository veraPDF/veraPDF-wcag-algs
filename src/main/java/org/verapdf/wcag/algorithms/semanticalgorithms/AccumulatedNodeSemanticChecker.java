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

import org.verapdf.wcag.algorithms.entities.IDocument;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.*;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;

import java.io.IOException;
import java.util.logging.Logger;

public class AccumulatedNodeSemanticChecker implements ISemanticsChecker {

	private static final Logger LOGGER = Logger.getLogger(AccumulatedNodeSemanticChecker.class.getCanonicalName());

	@Override
	public void checkSemanticDocument(IDocument document, String fileName) {
		StaticContainers.updateContainers(document, fileName);

		ITree tree = document.getTree();
		if (tree == null) {
			return;
		}

		if (new LinesPreprocessingConsumer().run()) {
			return;
		}
		if (new SemanticDocumentPreprocessingConsumer().run()) {
			return;
		}
		try (ContrastRatioConsumer contrastRatioConsumer = new ContrastRatioConsumer()) {
			if (contrastRatioConsumer.run()) {
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.warning(e.getMessage());
		}
		if (new AccumulatedNodeConsumer().run()) {
			return;
		}
		if (new HeadingCaptionConsumer().run()) {
			return;
		}
		if (new TOCDetectionConsumer().run()) {
			return;
		}
		if (new ListDetectionConsumer().run()) {
			return;
		}
		if (new TableBorderConsumer().run()) {
			return;
		}
		if (new TableChecker().run()) {
			return;
		}
		if (new ClusterTableConsumer().run()) {
			return;
		}
		if (new SemanticDocumentPostprocessingConsumer().run()) {
			return;
		}
		StaticContainers.getWCAGValidationInfo().setCurrentConsumer(null);
	}
}
