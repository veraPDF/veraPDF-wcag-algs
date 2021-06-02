package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticHeading;
import org.verapdf.wcag.algorithms.entities.SemanticTextNode;

public class NodeUtils {

	private static final double FLOATING_POINT_OPERATIONS_EPS = 1e-7;
	private static final double[] HEADING_PROBABILITY_PARAMS = {0.85, 0.85};

	public static double headingProbability(INode node, INode previousNode, INode nextNode) {
		if (previousNode == null || previousNode instanceof SemanticHeading) {
			if (nextNode == null) {
				return 0.0;
			}
			return headingProbability(node, nextNode);
		}
		if (nextNode == null) {
			return headingProbability(node, previousNode);
		}
		return Math.min(headingProbability(node, previousNode), headingProbability(node, nextNode));
	}

	public static double headingProbability(INode node, INode neighborNode) {
		if (node == null) {
			return 0.0;
		}
		if (neighborNode == null) {
			return 1.0;
		}
		if (!(node instanceof SemanticTextNode)) {
			return 0.0;
		}
		if (!(neighborNode instanceof SemanticTextNode)) {
			return 0.0;
		}
		double probability = 0.0;
		if (getFontWeight(node) > getFontWeight(neighborNode) + FLOATING_POINT_OPERATIONS_EPS) {
			probability += HEADING_PROBABILITY_PARAMS[0];
		}
		if (getFontSize(node) > getFontSize(neighborNode) + FLOATING_POINT_OPERATIONS_EPS) {
			probability += HEADING_PROBABILITY_PARAMS[1];
		}
		return Math.min(probability, 1.0);
	}

	public static Double getFontWeight(INode node) {
		if (node == null) {
			return null;
		}
		if (node instanceof SemanticTextNode) {
			return (((SemanticTextNode)node).getFontWeight());
		}
		return null;
	}

	public static Double getFontSize(INode node) {
		if (node == null) {
			return null;
		}
		if (node instanceof SemanticTextNode) {
			return (((SemanticTextNode)node).getFontSize());
		}
		return null;
	}

}
