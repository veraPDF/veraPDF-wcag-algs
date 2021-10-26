package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.verapdf.wcag.algorithms.entities.IDocument;
import org.verapdf.wcag.algorithms.entities.ITree;

public interface ISemanticsChecker {
	void checkSemanticTree(ITree tree);

	void checkSemanticTree(IDocument document);
}
