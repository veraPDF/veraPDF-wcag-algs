package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.JsonToPdfTree;
import org.verapdf.wcag.algorithms.entities.SemanticTree;

import java.io.IOException;

class AccumulatedNodeSemanticsCheckerTests {

	private final ISemanticsChecker semanticsChecker;

	public AccumulatedNodeSemanticsCheckerTests() {
		this.semanticsChecker = new AccumulatedNodeSemanticChecker();
	}

	@Test
	void testFullSemanticCorrectness() throws IOException {
		INode root = JsonToPdfTree.getPdfTreeRoot("/files/3.json");
		ITree tree = new SemanticTree(root);
		semanticsChecker.checkSemanticTree(tree);
		Assertions.assertEquals(1d, tree.getRoot().getCorrectSemanticScore(), 0d);
	}

	//@Test
	void testPartialSemanticCorrectness() throws IOException {
		INode root = JsonToPdfTree.getPdfTreeRoot("/files/0.json");
		ITree tree = new SemanticTree(root);
		semanticsChecker.checkSemanticTree(tree);
		Assertions.assertEquals(0.042d, tree.getRoot().getCorrectSemanticScore(), 0.00001d);
	}
}
