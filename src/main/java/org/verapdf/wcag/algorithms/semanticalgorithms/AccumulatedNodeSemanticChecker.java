package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.verapdf.wcag.algorithms.entities.IDocument;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.tables.Table;
import org.verapdf.wcag.algorithms.entities.tables.TableBordersCollection;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.*;

import java.util.function.Consumer;

public class AccumulatedNodeSemanticChecker implements ISemanticsChecker {

	@Override
	public void checkSemanticDocument(IDocument document) {
		ITree tree = document.getTree();

		LinesPreprocessingConsumer linesPreprocessingConsumer = new LinesPreprocessingConsumer(document);
		linesPreprocessingConsumer.findTableBorders();

		Consumer<INode> semanticDocumentValidator = new SemanticDocumentPreprocessingConsumer(document,
				linesPreprocessingConsumer.getLinesCollection());
		tree.forEach(semanticDocumentValidator);

		Table.updateTableCounter();

		TableBordersCollection tableBordersCollection = new TableBordersCollection(linesPreprocessingConsumer.getTableBorders());

		AccumulatedNodeConsumer semanticDetectionValidator = new AccumulatedNodeConsumer(tableBordersCollection);
		tree.forEach(semanticDetectionValidator);

		TableBorderConsumer tableBorderConsumer = new TableBorderConsumer(tableBordersCollection,
				semanticDetectionValidator.getAccumulatedNodeMapper());
		tableBorderConsumer.recognizeTables(tree);

		ClusterTableConsumer tableFinder = new ClusterTableConsumer(tableBordersCollection,
				semanticDetectionValidator.getAccumulatedNodeMapper());
		tableFinder.findTables(tree.getRoot());

		SemanticDocumentPostprocessingConsumer documentPostprocessingConsumer =
				new SemanticDocumentPostprocessingConsumer(semanticDetectionValidator.getAccumulatedNodeMapper());
		documentPostprocessingConsumer.checkForTitle(tree);
	}
}
