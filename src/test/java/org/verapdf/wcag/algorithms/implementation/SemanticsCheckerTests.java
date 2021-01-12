package org.verapdf.wcag.algorithms.implementation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.verapdf.wcag.algorithms.entity.INode;
import org.verapdf.wcag.algorithms.entity.ITree;
import org.verapdf.wcag.algorithms.entity.SemanticTree;
import org.verapdf.wcag.algorithms.util.JsonToPdfTree;

class SemanticsCheckerTests {

	private final ISemanticsChecker semanticsChecker;

	public SemanticsCheckerTests() {
		this.semanticsChecker = new SemanticsChecker();
	}

	@Test
	void testFullSemanticCorrectness() {
		INode root = JsonToPdfTree.getPdfTreeRoot("/files/3.json");
		ITree<INode> tree = new SemanticTree(root);
		semanticsChecker.checkSemanticTree(tree);
		Assertions.assertEquals(1d, tree.getRoot().getCorrectSemanticScore());
	}

	@Test
	void testPartialSemanticCorrectness() {
		INode root = JsonToPdfTree.getPdfTreeRoot("/files/0.json");
		ITree<INode> tree = new SemanticTree(root);
		semanticsChecker.checkSemanticTree(tree);
		Assertions.assertEquals(0.04199999999999893d, tree.getRoot().getCorrectSemanticScore());
	}
}
