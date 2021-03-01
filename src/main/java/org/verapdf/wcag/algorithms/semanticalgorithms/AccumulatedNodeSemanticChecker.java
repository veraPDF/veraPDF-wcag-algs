package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.AccumulatedNodeConsumer;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.SemanticTreePreprocessingConsumer;

import java.util.function.Consumer;

public class AccumulatedNodeSemanticChecker implements ISemanticsChecker {
	public void checkSemanticTree(ITree tree) {
		Consumer<INode> semanticTreeValidator = new SemanticTreePreprocessingConsumer();
		Consumer<INode> paragraphValidator = new AccumulatedNodeConsumer();
		tree.forEach(semanticTreeValidator);
		tree.forEach(paragraphValidator);
	}
}
