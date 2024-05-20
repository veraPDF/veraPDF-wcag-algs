package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.verapdf.wcag.algorithms.entities.IDocument;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.tables.TableBordersCollection;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.*;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;

import java.io.IOException;
import java.util.logging.Logger;

public class AccumulatedNodeSemanticChecker implements ISemanticsChecker {

	private static final Logger LOGGER = Logger.getLogger(AccumulatedNodeSemanticChecker.class.getCanonicalName());

	@Override
	public void checkSemanticDocument(IDocument document, String fileName) {
		StaticContainers.updateContainers(document);

		ITree tree = document.getTree();
		if (tree == null) {
			return;
		}

		LinesPreprocessingConsumer linesPreprocessingConsumer = new LinesPreprocessingConsumer();
		if (!startNextStep(linesPreprocessingConsumer)) {
			return;
		}
		linesPreprocessingConsumer.findTableBorders();
		StaticContainers.setTableBordersCollection(new TableBordersCollection(linesPreprocessingConsumer.getTableBorders()));

		SemanticDocumentPreprocessingConsumer semanticDocumentValidator = new SemanticDocumentPreprocessingConsumer();
		if (!startNextStep(semanticDocumentValidator)) {
			return;
		}
		tree.forEach(semanticDocumentValidator);
		StaticContainers.setStructElementsNumber(semanticDocumentValidator.getStructElementsNumber());
		StaticContainers.setTextChunksNumber(semanticDocumentValidator.getTextChunksNumber());

		if (fileName != null) {
			try (ContrastRatioConsumer contrastRatioConsumer = new ContrastRatioConsumer(fileName)) {
				if (!startNextStep(contrastRatioConsumer)) {
					return;
				}
				contrastRatioConsumer.calculateContrast(document.getTree());
			} catch (IOException e) {
				e.printStackTrace();
				LOGGER.warning(e.getMessage());
			}
		}

		AccumulatedNodeConsumer semanticDetectionValidator = new AccumulatedNodeConsumer();
		if (!startNextStep(semanticDetectionValidator)) {
			return;
		}
		tree.forEach(semanticDetectionValidator);

		HeadingCaptionConsumer headingCaptionConsumer = new HeadingCaptionConsumer();
		if (!startNextStep(headingCaptionConsumer)) {
			return;
		}
		tree.forEach(headingCaptionConsumer);

		TOCDetectionConsumer tocDetectionConsumer = new TOCDetectionConsumer();
		if (!startNextStep(tocDetectionConsumer)) {
			return;
		}
		tree.forEach(tocDetectionConsumer);

		ListDetectionConsumer listDetectionConsumer = new ListDetectionConsumer();
		if (!startNextStep(listDetectionConsumer)) {
			return;
		}
		tree.forEach(listDetectionConsumer);

		TableBorderConsumer tableBorderConsumer = new TableBorderConsumer();
		if (!startNextStep(tableBorderConsumer)) {
			return;
		}
		tableBorderConsumer.recognizeTables(tree);

		TableChecker tableChecker = new TableChecker();
		if (!startNextStep(tableChecker)) {
			return;
		}
		tree.forEach(tableChecker);

		ClusterTableConsumer tableFinder = new ClusterTableConsumer();
		if (!startNextStep(tableFinder)) {
			return;
		}
		tableFinder.findTables(tree.getRoot());

		SemanticDocumentPostprocessingConsumer documentPostprocessingConsumer = new SemanticDocumentPostprocessingConsumer();
		if (!startNextStep(documentPostprocessingConsumer)) {
			return;
		}
		documentPostprocessingConsumer.runPostprocessingChecks(tree);

		StaticContainers.getWCAGValidationInfo().setCurrentConsumer(null);
	}

	public static boolean startNextStep(WCAGConsumer consumer) {
		if (StaticContainers.getWCAGValidationInfo().getAbortProcessing()) {
			StaticContainers.getWCAGValidationInfo().setAbortProcessing(false);
			StaticContainers.getWCAGValidationInfo().setCurrentConsumer(null);
			return false;
		}
		StaticContainers.getWCAGValidationInfo().setCurrentConsumer(consumer);
		return true;
	}
}
