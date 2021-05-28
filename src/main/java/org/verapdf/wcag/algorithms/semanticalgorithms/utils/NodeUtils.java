package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticHeading;
import org.verapdf.wcag.algorithms.entities.SemanticParagraph;
import org.verapdf.wcag.algorithms.entities.SemanticSpan;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextLine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		if (!(node instanceof SemanticSpan || node instanceof SemanticParagraph)) {
			return 0.0;
		}
		if (!(neighborNode instanceof SemanticSpan || neighborNode instanceof SemanticParagraph)) {
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

	public static double getFontWeight(List<TextLine> lines) {
		Map<Double, Double> weightMap = new HashMap<>();
		for (TextLine line : lines) {
			for (TextChunk chunk : line.getTextChunks()) {
				Double weight = chunk.getFontWeight();
				Double weightLength = weightMap.get(weight);
				if (weightLength == null) {
					weightMap.put(weight, chunk.getBoundingBox().getWidth());
				} else {
					weightMap.put(weight, weightLength + chunk.getBoundingBox().getWidth());
				}
			}
		}
		double commonWeight = 0.0;
		double commonWeightLength = 0.0;
		for (Map.Entry<Double, Double> weight : weightMap.entrySet()) {
			if (commonWeightLength < weight.getValue()) {
				commonWeightLength = weight.getValue();
				commonWeight = weight.getKey();
			}
		}
		return commonWeight;
	}

	public static Double getFontWeight(INode node) {
		if (node == null) {
			return null;
		}
		if (node instanceof SemanticSpan) {
			return getFontWeight(((SemanticSpan)node).getLines());
		} else if (node instanceof SemanticParagraph) {
			return getFontWeight(((SemanticParagraph)node).getLines());
		}
		return null;
	}

	public static double getFontSize(List<TextLine> lines) {
		Map<Double, Double> sizeMap = new HashMap<>();
		for (TextLine line : lines) {
			for (TextChunk chunk : line.getTextChunks()) {
				Double size = chunk.getFontSize();
				Double sizeLength = sizeMap.get(size);
				if (sizeLength == null) {
					sizeMap.put(size, chunk.getBoundingBox().getWidth());
				} else {
					sizeMap.put(size, sizeLength + chunk.getBoundingBox().getWidth());
				}
			}
		}
		double commonSize = 0.0;
		double commonSizeLength = 0.0;
		for (Map.Entry<Double, Double> weight : sizeMap.entrySet()) {
			if (commonSizeLength < weight.getValue()) {
				commonSizeLength = weight.getValue();
				commonSize = weight.getKey();
			}
		}
		return commonSize;
	}

	public static Double getFontSize(INode node) {
		if (node == null) {
			return null;
		}
		if (node instanceof SemanticSpan) {
			return getFontSize(((SemanticSpan)node).getLines());
		} else if (node instanceof SemanticParagraph) {
			return getFontSize(((SemanticParagraph)node).getLines());
		}
		return null;
	}

	public static TextLine getFirstLine(INode node) {
		if (node == null) {
			return null;
		}
		if (node instanceof SemanticSpan) {
			return ((SemanticSpan)node).getFirstLine();
		} else if (node instanceof SemanticParagraph) {
			return ((SemanticParagraph)node).getFirstLine();
		}
		return null;
	}

	public static TextLine getLastLine(INode node) {
		if (node == null) {
			return null;
		}
		if (node instanceof SemanticSpan) {
			return ((SemanticSpan)node).getLastLine();
		} else if (node instanceof SemanticParagraph) {
			return ((SemanticParagraph)node).getLastLine();
		}
		return null;
	}

}
