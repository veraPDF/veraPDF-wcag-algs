package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.verapdf.wcag.algorithms.entities.IDocument;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.tables.TableBordersCollection;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.*;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;

import java.util.function.Consumer;

public class AccumulatedNodeSemanticChecker implements ISemanticsChecker {

	@Override
	public void checkSemanticDocument(IDocument document) {
		StaticContainers.updateContainers(document);

		ITree tree = document.getTree();

		LinesPreprocessingConsumer linesPreprocessingConsumer = new LinesPreprocessingConsumer();
		linesPreprocessingConsumer.findTableBorders();

		Consumer<INode> semanticDocumentValidator = new SemanticDocumentPreprocessingConsumer();
		tree.forEach(semanticDocumentValidator);

		StaticContainers.setTableBordersCollection(new TableBordersCollection(linesPreprocessingConsumer.getTableBorders()));

		AccumulatedNodeConsumer semanticDetectionValidator = new AccumulatedNodeConsumer();
		tree.forEach(semanticDetectionValidator);

		TOCDetectionConsumer tocDetectionConsumer = new TOCDetectionConsumer();
		tree.forEach(tocDetectionConsumer);

		ListDetectionConsumer listDetectionConsumer = new ListDetectionConsumer();
		tree.forEach(listDetectionConsumer);

		TableBorderConsumer tableBorderConsumer = new TableBorderConsumer();
		tableBorderConsumer.recognizeTables(tree);

		ClusterTableConsumer tableFinder = new ClusterTableConsumer();
		tableFinder.findTables(tree.getRoot());

		SemanticDocumentPostprocessingConsumer documentPostprocessingConsumer = new SemanticDocumentPostprocessingConsumer();
		documentPostprocessingConsumer.runPostprocessingChecks(tree);
	}
}
