package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.verapdf.wcag.algorithms.entities.IDocument;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.tables.Table;
import org.verapdf.wcag.algorithms.entities.tables.TableBordersCollection;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.*;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;

import java.util.function.Consumer;

public class AccumulatedNodeSemanticChecker implements ISemanticsChecker {

	@Override
	public void checkSemanticDocument(IDocument document) {
		StaticContainers.clearAllContainers(document);

		ITree tree = document.getTree();

		LinesPreprocessingConsumer linesPreprocessingConsumer = new LinesPreprocessingConsumer(document);
		linesPreprocessingConsumer.findTableBorders();

		Consumer<INode> semanticDocumentValidator = new SemanticDocumentPreprocessingConsumer(document);
		tree.forEach(semanticDocumentValidator);

		Table.updateTableCounter();

		StaticContainers.setTableBordersCollection(new TableBordersCollection(linesPreprocessingConsumer.getTableBorders()));

		AccumulatedNodeConsumer semanticDetectionValidator = new AccumulatedNodeConsumer();
		tree.forEach(semanticDetectionValidator);

		TableBorderConsumer tableBorderConsumer = new TableBorderConsumer();
		tableBorderConsumer.recognizeTables(tree);

		ClusterTableConsumer tableFinder = new ClusterTableConsumer();
		tableFinder.findTables(tree.getRoot());

		SemanticDocumentPostprocessingConsumer documentPostprocessingConsumer = new SemanticDocumentPostprocessingConsumer();
		documentPostprocessingConsumer.runPostprocessingChecks(tree);

		TOCDetectionConsumer tocDetectionConsumer = new TOCDetectionConsumer(document);
		tree.forEach(tocDetectionConsumer);
	}
}
