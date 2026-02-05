/*
 * This file is part of veraPDF wcag algorithms, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF wcag algorithms is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF wcag algorithms as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF wcag algorithms as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticFigure;
import org.verapdf.wcag.algorithms.entities.SemanticTextNode;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection.ArabicNumbersListLabelsDetectionAlgorithm;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.listLabelsDetection.ListLabelsDetectionAlgorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaptionUtils {

	private static final double FLOATING_POINT_OPERATIONS_EPS = 1e-7;
	private static final double[] DEFAULT_INTERVAL_BEFORE_IMAGE = {-0.2, 1.8};
	private static final double[] DEFAULT_INTERVAL_AFTER_IMAGE = {0, 1.8};
	private static final double[] DEFAULT_INTERVAL_BEFORE_LEFT_IMAGE_SIDE = {0, 1.8};
	private static final double[] DEFAULT_INTERVAL_AFTER_RIGHT_IMAGE_SIDE = {0, 1.8};
	private static final double IMAGE_INTERVAL_STANDARD = 1;
	private static final double IMAGE_INTERVAL_SIDE_STANDARD = 1;
	private static final double[] CAPTION_PROBABILITY_PARAMS = {1.0, 0.95, 0.9, 0.85, 0.2, 0.1, 0.03, 0.003};
	private static final double WITH_TOLERANCE_FACTOR = 0.33;

	private static final String CAPTION_REGEX = "^\\s*(\\S+)\\s*(\\d*)\\s*([.:]?)\\s*(\\S*)";

	private static final Pattern PATTERN = Pattern.compile(CAPTION_REGEX);

	private static final Map<SemanticType, List<String>> OBJECT_NAMES = new HashMap<>();

	static {
		List<String> tableNames = new ArrayList<>();
		tableNames.add("table");
		OBJECT_NAMES.put(SemanticType.TABLE, tableNames);

		List<String> figureNames = new ArrayList<>();
		figureNames.add("figure");
		figureNames.add("image");
		OBJECT_NAMES.put(SemanticType.FIGURE, figureNames);
	}

	public static double imageCaptionProbability(INode node, SemanticFigure imageNode) {
		if (node == null) {
			return 0;
		}
		if (HeadingUtils.isDetectedHeadingNode(node) || (StaticContainers.isDataLoader() && node.getSemanticType() == SemanticType.LIST)) {
			return 0;
		}
		INode accumulatedNode = StaticContainers.getAccumulatedNodeMapper().get(node);
		if (!(accumulatedNode instanceof SemanticTextNode)) {
			return 0.0;
		}
		SemanticTextNode textNode = (SemanticTextNode) accumulatedNode;
		double captionContentProbability = captionContentProbability(textNode, imageNode.getSemanticType());
		double linesNumberCaptionProbability = getLinesNumberCaptionProbability(textNode);

		double captionProbability = captionVerticalProbability(textNode, imageNode.getBoundingBox());
		captionProbability *= captionHorizontalProbability(textNode, imageNode.getBoundingBox());
		captionProbability *= linesNumberCaptionProbability;
		captionProbability += captionContentProbability;

		double sideCaptionProbability = 0.0;
		if (captionContentProbability > NodeUtils.EPSILON) {
			sideCaptionProbability = sideCaptionHorizontalProbability(textNode, imageNode.getBoundingBox());
			sideCaptionProbability *= sideCaptionVerticalProbability(textNode, imageNode.getBoundingBox());
			sideCaptionProbability *= sideCaptionSpacingProbability(textNode, imageNode.getBoundingBox());
			sideCaptionProbability *= linesNumberCaptionProbability;
			sideCaptionProbability += captionContentProbability;
		}
		return Math.min(Math.max(captionProbability, sideCaptionProbability), 1.0);
	}

	public static double tableCaptionProbability(INode node, BoundingBox tableBoundingBox) {
		if (node == null) {
			return 0.0;
		}
		if (HeadingUtils.isDetectedHeadingNode(node) || node.getSemanticType() == SemanticType.LIST) {
			return 0.0;
		}
		INode accumulatedNode = StaticContainers.getAccumulatedNodeMapper().get(node);
		if (accumulatedNode == null) {
			return 0.0;
		}
		if (!(accumulatedNode instanceof SemanticTextNode)) {
			return 0.0;
		}
		SemanticTextNode textNode = (SemanticTextNode) accumulatedNode;
		double captionProbability = captionVerticalProbability(textNode, tableBoundingBox);
		captionProbability *= captionHorizontalProbability(textNode, tableBoundingBox);
		captionProbability *= getLinesNumberCaptionProbability(textNode);
		captionProbability += captionContentProbability(textNode, SemanticType.TABLE);
		return Math.min(captionProbability, 1.0);
	}

	private static double getLinesNumberCaptionProbability(SemanticTextNode textNode) {
		return Math.max(0, 1 - (StaticContainers.isDataLoader() ? CAPTION_PROBABILITY_PARAMS[7] : CAPTION_PROBABILITY_PARAMS[6]) *
				(textNode.getLinesNumber() - 1) * (textNode.getLinesNumber() - 1));
	}

	public static boolean isContaining(SemanticTextNode textNode, BoundingBox imageBoundingBox) {
		double tol = WITH_TOLERANCE_FACTOR * textNode.getFontSize();
		return (imageBoundingBox.getLeftX() + tol > textNode.getLeftX() &&
				imageBoundingBox.getRightX() < textNode.getRightX() + tol);
	}

	private static boolean isContaining(BoundingBox imageBoundingBox, SemanticTextNode textNode) {
		double tol = WITH_TOLERANCE_FACTOR * textNode.getFontSize();
		return (textNode.getLeftX() + tol > imageBoundingBox.getLeftX() &&
				textNode.getRightX() < imageBoundingBox.getRightX() + tol);
	}

	public static boolean isContaining(TextLine firstLine, TextLine secondLine) {
		double tol = WITH_TOLERANCE_FACTOR * firstLine.getFontSize();
		return (secondLine.getLeftX() + tol > firstLine.getLeftX() &&
				secondLine.getRightX() < firstLine.getRightX() + tol);
	}

	private static boolean areStrongCenterOverlapping(SemanticTextNode textNode, BoundingBox imageBoundingBox) {
		double tol = WITH_TOLERANCE_FACTOR * textNode.getFontSize();
		double textCenter = textNode.getBoundingBox().getCenterX();
		double imageCenter = imageBoundingBox.getCenterX();

		if (textCenter + tol > imageBoundingBox.getRightX() || textCenter < imageBoundingBox.getLeftX() + tol) {
			return false;
		}
		if (imageCenter + tol > textNode.getRightX() || imageCenter < textNode.getLeftX() + tol) {
			return false;
		}
		return true;
	}

	private static boolean areCenterOverlapping(SemanticTextNode textNode, BoundingBox imageBoundingBox) {
		double tol = WITH_TOLERANCE_FACTOR * textNode.getFontSize();
		double textCenter = textNode.getBoundingBox().getCenterX();
		double imageCenter = imageBoundingBox.getCenterX();

		if (textCenter + tol < imageBoundingBox.getRightX() && textCenter > imageBoundingBox.getLeftX() + tol) {
			return true;
		}
		if (imageCenter + tol < textNode.getRightX() && imageCenter > textNode.getLeftX() + tol) {
			return true;
		}
		return false;
	}

	public static boolean areOverlapping(TextLine textLine, BoundingBox boundingBox) {
		double tol = WITH_TOLERANCE_FACTOR * textLine.getFontSize();
		return (textLine.getLeftX() + tol < boundingBox.getRightX() && boundingBox.getLeftX() + tol < textLine.getRightX());
	}

	private static boolean areOverlapping(SemanticTextNode textNode, SemanticFigure imageNode) {
		double tol = WITH_TOLERANCE_FACTOR * textNode.getFontSize();
		return (textNode.getLeftX() + tol < imageNode.getRightX() && imageNode.getLeftX() + tol < textNode.getRightX());
	}

	private static double captionVerticalProbability(SemanticTextNode textNode, BoundingBox imageBoundingBox) {
		if (textNode.getLastPageNumber() == null || imageBoundingBox.getPageNumber() == null ||
				textNode.getPageNumber() == null || imageBoundingBox.getLastPageNumber() == null) {
			return 0.0;
		}
		if (!textNode.getPageNumber().equals(textNode.getLastPageNumber()) ||
				!textNode.getPageNumber().equals(imageBoundingBox.getPageNumber())) {
			return 0.0;
		}
		double firstBaseline = textNode.getFirstBaseline();
		double lastBaseline = textNode.getLastBaseline();
		if (lastBaseline > imageBoundingBox.getTopY() + FLOATING_POINT_OPERATIONS_EPS) {
			return ChunksMergeUtils.getUniformProbability(DEFAULT_INTERVAL_BEFORE_IMAGE,
					(textNode.getBottomY() - imageBoundingBox.getTopY()) / textNode.getFontSize(), IMAGE_INTERVAL_STANDARD);
		}
		if (firstBaseline < imageBoundingBox.getBottomY() - FLOATING_POINT_OPERATIONS_EPS) {
			return ChunksMergeUtils.getUniformProbability(DEFAULT_INTERVAL_AFTER_IMAGE,
					(imageBoundingBox.getBottomY() - textNode.getTopY()) / textNode.getFontSize(), IMAGE_INTERVAL_STANDARD);
		}
		return 0.0;
	}

	public static double captionContentProbability(SemanticTextNode textNode, SemanticType type) {
		if (!StaticContainers.isDataLoader()) {
			String value = textNode.getFirstLine().getValue().trim();
			if (value.startsWith(type.getValue())) {
				value = value.substring(type.getValue().length()).trim();
				if (!value.isEmpty() && ListLabelsDetectionAlgorithm.getRegexStartLength(value,
						ArabicNumbersListLabelsDetectionAlgorithm.ARABIC_NUMBER_REGEX) > 0) {
					return CAPTION_PROBABILITY_PARAMS[4];
				}
				return CAPTION_PROBABILITY_PARAMS[5];
			}
			return 0.0;
		}
		Matcher matcher = PATTERN.matcher(textNode.getFirstLine().getValue());
		if (!matcher.find()) {
			return 0.0;
		}

		List<String> objectNames = OBJECT_NAMES.get(type);
		if (!objectNames.contains(matcher.group(1).toLowerCase())){
			return 0.0;
		}
		
		double captionProbability = CAPTION_PROBABILITY_PARAMS[5];
		if (!matcher.group(2).isEmpty()) {
			captionProbability += CAPTION_PROBABILITY_PARAMS[5];
		}

		if (matcher.group(4).isEmpty() == matcher.group(3).isEmpty()) {
			captionProbability += CAPTION_PROBABILITY_PARAMS[5];
		}

		return captionProbability;
	}

	public static double captionHorizontalProbability(SemanticTextNode textNode, BoundingBox imageBoundingBox) {
		if (isContaining(imageBoundingBox, textNode) && areStrongCenterOverlapping(textNode, imageBoundingBox)) {
			return CAPTION_PROBABILITY_PARAMS[0];
		}
		if (isContaining(imageBoundingBox, textNode) && areCenterOverlapping(textNode, imageBoundingBox)) {
			return CAPTION_PROBABILITY_PARAMS[1];
		}
		if (isContaining(textNode, imageBoundingBox) && areStrongCenterOverlapping(textNode, imageBoundingBox)) {
			return CAPTION_PROBABILITY_PARAMS[2];
		}
		if (isContaining(textNode, imageBoundingBox) && areCenterOverlapping(textNode, imageBoundingBox)) {
			return CAPTION_PROBABILITY_PARAMS[3];
		}
		return 0.0;
	}

	private static double sideCaptionHorizontalProbability(SemanticTextNode textNode, BoundingBox imageBoundingBox) {
		if (textNode.getLeftX() > imageBoundingBox.getRightX() ||
				textNode.getRightX() < imageBoundingBox.getLeftX()) {
			return 1.0;
		}
		return 0.0;
	}

	private static double sideCaptionVerticalProbability(SemanticTextNode textNode, BoundingBox imageBoundingBox) {
		if (textNode.getLastPageNumber() == null || imageBoundingBox.getPageNumber() == null ||
				textNode.getPageNumber() == null || imageBoundingBox.getLastPageNumber() == null) {
			return 0.0;
		}
		if (!textNode.getPageNumber().equals(textNode.getLastPageNumber()) ||
				!textNode.getPageNumber().equals(imageBoundingBox.getPageNumber())) {
			return 0.0;
		}
		if (textNode.getTopY() < imageBoundingBox.getTopY() - FLOATING_POINT_OPERATIONS_EPS &&
				textNode.getBottomY() > imageBoundingBox.getBottomY() + FLOATING_POINT_OPERATIONS_EPS) {
			//check previous and next node, that they are above and below image?
			return 1.0;
		}
		return 0.0;
	}

	private static double sideCaptionSpacingProbability(SemanticTextNode textNode, BoundingBox imageBoundingBox) {
		if (textNode.getLeftX() > imageBoundingBox.getRightX()) {
			return ChunksMergeUtils.getUniformProbability(DEFAULT_INTERVAL_AFTER_RIGHT_IMAGE_SIDE,
			                                              (textNode.getLeftX() - imageBoundingBox.getRightX()) /
			                                              textNode.getFontSize(), IMAGE_INTERVAL_SIDE_STANDARD);
		} else if (textNode.getRightX() < imageBoundingBox.getLeftX()) {
			return ChunksMergeUtils.getUniformProbability(DEFAULT_INTERVAL_BEFORE_LEFT_IMAGE_SIDE,
			                                              (imageBoundingBox.getLeftX() - textNode.getRightX()) /
			                                              textNode.getFontSize(), IMAGE_INTERVAL_SIDE_STANDARD);
		}
		return 0.0;
	}

}
