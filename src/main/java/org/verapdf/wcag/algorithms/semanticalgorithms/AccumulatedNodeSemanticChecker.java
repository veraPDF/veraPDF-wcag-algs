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
		try (ContrastRatioConsumer contrastRatioConsumer = new ContrastRatioConsumer("")) {
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
