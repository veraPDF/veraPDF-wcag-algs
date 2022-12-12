package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.verapdf.wcag.algorithms.entities.IDocument;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.tables.TableBordersCollection;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.*;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;

import java.util.function.Consumer;

public class AccumulatedNodeSemanticChecker implements ISemanticsChecker {

	@Override
	public void checkSemanticDocument(IDocument document, String fileName) {
		StaticContainers.updateContainers(document);

		ITree tree = document.getTree();
		if (tree == null) {
			return;
		}

		SemanticDocumentPreprocessingConsumer semanticDocumentValidator = new SemanticDocumentPreprocessingConsumer();
		if (!startNextStep(semanticDocumentValidator)) {
			return;
		}
		tree.forEach(semanticDocumentValidator);

		LinesPreprocessingConsumer linesPreprocessingConsumer = new LinesPreprocessingConsumer();
		if (!startNextStep(linesPreprocessingConsumer)) {
			return;
		}
		linesPreprocessingConsumer.findTableBorders();
		StaticContainers.setTableBordersCollection(new TableBordersCollection(linesPreprocessingConsumer.getTableBorders()));

		if (fileName != null) {
			ContrastRatioConsumer contrastRatioConsumer = new ContrastRatioConsumer(fileName, semanticDocumentValidator.getTextChunksNumber());
			if (!startNextStep(contrastRatioConsumer)) {
				return;
			}
			document.getTree().forEach(contrastRatioConsumer);
		}

		AccumulatedNodeConsumer semanticDetectionValidator = new AccumulatedNodeConsumer();
		if (!startNextStep(semanticDetectionValidator)) {
			return;
		}
		tree.forEach(semanticDetectionValidator);

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
