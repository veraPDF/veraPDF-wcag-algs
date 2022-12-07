package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.verapdf.wcag.algorithms.entities.IDocument;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.tables.TableBordersCollection;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.*;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.WCAGProgressStatus;

import java.util.function.Consumer;

public class AccumulatedNodeSemanticChecker implements ISemanticsChecker {

	@Override
	public void checkSemanticDocument(IDocument document, String fileName) {
		StaticContainers.updateContainers(document);

		if (!startNextStep(WCAGProgressStatus.CONTRAST_DETECTION)) {
			return;
		}
		if (fileName != null) {
			ContrastRatioChecker contrastRatioChecker = new ContrastRatioChecker();
			contrastRatioChecker.checkDocument(document, fileName);
		}

		ITree tree = document.getTree();
		if (tree == null) {
			return;
		}

		if (!startNextStep(WCAGProgressStatus.LINES_PREPROCESSING)) {
			return;
		}
		LinesPreprocessingConsumer linesPreprocessingConsumer = new LinesPreprocessingConsumer();
		linesPreprocessingConsumer.findTableBorders();

		if (!startNextStep(WCAGProgressStatus.DOCUMENT_PREPROCESSING)) {
			return;
		}
		Consumer<INode> semanticDocumentValidator = new SemanticDocumentPreprocessingConsumer();
		tree.forEach(semanticDocumentValidator);

		StaticContainers.setTableBordersCollection(new TableBordersCollection(linesPreprocessingConsumer.getTableBorders()));

		if (!startNextStep(WCAGProgressStatus.PARAGRAPH_DETECTION)) {
			return;
		}
		AccumulatedNodeConsumer semanticDetectionValidator = new AccumulatedNodeConsumer();
		tree.forEach(semanticDetectionValidator);

		if (!startNextStep(WCAGProgressStatus.TOC_DETECTION)) {
			return;
		}
		TOCDetectionConsumer tocDetectionConsumer = new TOCDetectionConsumer();
		tree.forEach(tocDetectionConsumer);

		if (!startNextStep(WCAGProgressStatus.LIST_DETECTION)) {
			return;
		}
		ListDetectionConsumer listDetectionConsumer = new ListDetectionConsumer();
		tree.forEach(listDetectionConsumer);

		if (!startNextStep(WCAGProgressStatus.TABLE_BORDER_DETECTION)) {
			return;
		}
		TableBorderConsumer tableBorderConsumer = new TableBorderConsumer();
		tableBorderConsumer.recognizeTables(tree);

		if (!startNextStep(WCAGProgressStatus.TABLE_VALIDATION)) {
			return;
		}
		TableChecker tableChecker = new TableChecker();
		tree.forEach(tableChecker);

		if (!startNextStep(WCAGProgressStatus.TABLE_DETECTION)) {
			return;
		}
		ClusterTableConsumer tableFinder = new ClusterTableConsumer();
		tableFinder.findTables(tree.getRoot());

		if (!startNextStep(WCAGProgressStatus.DOCUMENT_POSTPROCESSING)) {
			return;
		}
		SemanticDocumentPostprocessingConsumer documentPostprocessingConsumer = new SemanticDocumentPostprocessingConsumer();
		documentPostprocessingConsumer.runPostprocessingChecks(tree);
		StaticContainers.setWCAGProgressStatus(null);
	}

	public static boolean startNextStep(WCAGProgressStatus status) {
		if (StaticContainers.getAbortProcessing()) {
			StaticContainers.setAbortProcessing(false);
			StaticContainers.setWCAGProgressStatus(null);
			return false;
		}
		StaticContainers.setWCAGProgressStatus(status);
		return true;
	}
}
