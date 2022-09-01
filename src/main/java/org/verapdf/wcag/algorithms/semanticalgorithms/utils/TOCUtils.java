package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

import java.util.*;

public class TOCUtils {

	private static final Set<SemanticType> tocSemanticTypes = new HashSet<>(Arrays.asList(
			SemanticType.TABLE_OF_CONTENT, SemanticType.TABLE_OF_CONTENT_ITEM));

	public static boolean isTOCNode(INode node) {
		return tocSemanticTypes.contains(node.getSemanticType());
	}

	public static boolean isInitialTOCNode(INode node) {
		return tocSemanticTypes.contains(node.getInitialSemanticType());
	}

}
