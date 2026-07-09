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
package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.SemanticTextNode;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextColumn;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ImagesUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.NodeUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TextChunkUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.WCAGProgressStatus;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class ContrastRatioConsumer extends WCAGConsumer implements Consumer<INode> {

	private static final Logger logger = Logger.getLogger(ContrastRatioConsumer.class.getCanonicalName());
	private static final double LUMINOSITY_DIFFERENCE = 0.001;
    private static final double BACKGROUND_COLOR_MIN_PERCENTAGE_THRESHOLD = 0.01;
	private long processedTextChunks;
	private final Long textChunksNumber;

	public ContrastRatioConsumer() {
		this.textChunksNumber = StaticContainers.getTextChunksNumber();
		this.processedTextChunks = 0;
	}

	@Override
	public boolean run() {
		if (!startStep()) {
			return true;
		}
		if (StaticContainers.getFileName() == null) {
			logger.warning("The file name is missing for ContrastRatioConsumer");
			return false;
		}
		try {
			ImagesUtils imagesUtils = StaticContainers.getImagesUtils();
			imagesUtils.loadDocument();
			if (imagesUtils.isLoad()) {
				calculateContrast(StaticContainers.getDocument().getTree());
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.warning(e.getMessage());
		}
		return false;
	}

	public void calculateContrast(ITree tree) {
		for (INode node : tree) {
			accept(node);
			if (StaticContainers.getWCAGValidationInfo().getAbortProcessing()) {
				break;
			}
		}
	}

	@Override
	public void accept(INode node) {
		if (node.getChildren().isEmpty() && (node instanceof SemanticTextNode)) {
			calculateContrastRatio((SemanticTextNode) node);
			processedTextChunks++;
		}
	}

	public double getContrastRatio(double first, double second) {
		double l1 = Math.max(first, second);
		double l2 = Math.min(first, second);
		return (l1 + 0.05) / (l2 + 0.05);
	}

	public void calculateContrastRatio(TextChunk textChunk) {
		BufferedImage renderedPage = StaticContainers.getImagesUtils().getRenderPageForContrast(textChunk.getPageNumber());
		if (renderedPage != null) {
			calculateContrastRation(textChunk, renderedPage);
		}
	}

	private void calculateContrastRatio(SemanticTextNode node) {
		BufferedImage renderedPage = StaticContainers.getImagesUtils().getRenderPageForContrast(node.getPageNumber());
		if (renderedPage != null) {
			for (TextColumn column : node.getColumns()) {
				for (TextLine textLine : column.getLines()) {
					for (TextChunk textChunk : textLine.getTextChunks()) {
						calculateContrastRation(textChunk, renderedPage);
					}
				}
			}
		}
	}

	public void calculateContrastRation(TextChunk textChunk, BufferedImage renderedPage) {
		if ((textChunk.getValue() != null && (TextChunkUtils.isWhiteSpaceChunk(textChunk)))) {
			return;
		}
		try {
			BufferedImage targetBim = StaticContainers.getImagesUtils().getPageSubImage(renderedPage, textChunk.getBoundingBox());
			if (targetBim != null) {
				if (targetBim.getWidth() <= 1 || targetBim.getHeight() <= 1) {
					return;
				}
				double contrastRatio = getContrastRatio(targetBim, textChunk);
				textChunk.setContrastRatio(contrastRatio);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage());
		}
	}

	private static double [] convertCmykToRgb(double [] cmykColorComponentArray) {
		double [] result = new double[3];
		if (cmykColorComponentArray.length == 4) {
			double black = 1 - cmykColorComponentArray[3];
			for (int i = 0; i < 3; i++) {
				result[i] = (1 - cmykColorComponentArray[i]) * black;
			}
		}
		return result;
	}

	public static Color getTextColorFromComponentArray(double [] colorComponentArray) {
		Color res = null;
		if (colorComponentArray != null) {
			if (colorComponentArray.length == 1) {
				int grayscaleValue =  convertDoubleColorValueToRgbInteger(colorComponentArray[0]);
				res = new Color(grayscaleValue,
				                grayscaleValue,
				                grayscaleValue);
			} else if (colorComponentArray.length == 3) {
				res = makeRgbColorFromDoubleValues(colorComponentArray);
			} else if (colorComponentArray.length == 4) {
				double [] convertedRgbColor = convertCmykToRgb(colorComponentArray);
				res = makeRgbColorFromDoubleValues(convertedRgbColor);
			}
		}
		return res;
	}

	private static Color makeRgbColorFromDoubleValues(double [] colorComponentArray) {
		assert colorComponentArray.length == 3;
		return new Color(convertDoubleColorValueToRgbInteger(colorComponentArray[0]),
		                 convertDoubleColorValueToRgbInteger(colorComponentArray[1]),
		                 convertDoubleColorValueToRgbInteger(colorComponentArray[2]));
	}

	private static int convertDoubleColorValueToRgbInteger(double value) {
		int result = (int) Math.floor(value * 256);
		if (result > 255) {
			result = 255;
		} else if (result < 0) {
			result = 0;
		}
		return result;
	}

	private double getContrastRatio(BufferedImage image, TextChunk textChunk) {
		double [] textChunkOriginalColor = textChunk.getFontColor();
		Color textColor = getTextColorFromComponentArray(textChunkOriginalColor);
		double textLuminosity = 0;
		double approximatedTextLuminosity = 0;
		if (textColor != null) {
			textLuminosity = relativeLuminosity(textColor);
			approximatedTextLuminosity = textLuminosity;
			double diff = 1.0;
			SortedMap<Color, DataPoint> imageColorMap = getImageColorMap(image);
            if (StaticContainers.isDataLoader() && (imageColorMap.size() == 1)) {
                return 1.0;
            }
            Color backgroundColor = getBackgroundColor(imageColorMap, textColor);
			textChunk.setBackgroundColor(extractColorComponents(backgroundColor));
            if (StaticContainers.isDataLoader() && getColorPercent(imageColorMap, backgroundColor) < BACKGROUND_COLOR_MIN_PERCENTAGE_THRESHOLD) {
                return 1.0;
            }
			List<DataPoint> dpFullArray = new ArrayList<>(imageColorMap.values());
			for (DataPoint dp : dpFullArray) {
				double luminosity = dp.getValue();
				double currentDifference = Math.abs(luminosity - textLuminosity);
				if (currentDifference <= diff) {
					approximatedTextLuminosity = luminosity;
					diff = currentDifference;
				}
			}
		}

		double[] contrastColors = get2MostPresentElements(getLuminosityPresenceList(image));
		if (Math.abs(approximatedTextLuminosity - contrastColors[0]) <= LUMINOSITY_DIFFERENCE) {
			if (contrastColors[1] == -1) {
				if (textColor != null && Math.abs(contrastColors[0] - textLuminosity) > LUMINOSITY_DIFFERENCE){
					contrastColors[1] = textLuminosity;
				} else {
					return 1;
				}
			}
			return getContrastRatio(approximatedTextLuminosity, contrastColors[1]);
		} else if ((Math.abs(approximatedTextLuminosity - contrastColors[1]) <= LUMINOSITY_DIFFERENCE) || textColor != null) {
			return getContrastRatio(approximatedTextLuminosity, contrastColors[0]);
		} else {
			return getContrastRatio(contrastColors[0], contrastColors[1]);
		}
	}

    private double getColorPercent(SortedMap<Color, DataPoint> colorMap, Color color) {
        if (color != null && colorMap.containsKey(color)) {
            DataPoint dp = colorMap.get(color);
            if (dp != null) {
                long totalPixels = colorMap.values().stream()
                        .mapToInt(DataPoint::getTotalOccurrence)
                        .sum();
                return (double) dp.totalOccurrence / totalPixels;
            }
        }
        return 0.0;
    }

	private double[] extractColorComponents(Color backgroundColor) {
		if (backgroundColor != null) {
			float[] components = backgroundColor.getColorComponents(null);
			return IntStream.range(0, components.length).mapToDouble(i -> components[i]).toArray();
		}
		return null;
	}

	private double getContrastRatio(BufferedImage image) {
		double[] contrastColors = get2MostPresentElements(getLuminosityPresenceList(image));
		return getContrastRatio(contrastColors[0], contrastColors[1]);
	}

	private double relativeLuminosity(Color color) {
		double normalizedRed = normalizeColorComponent(color.getRed());
		double normalizeGreen = normalizeColorComponent(color.getGreen());
		double normalizeBlue = normalizeColorComponent(color.getBlue());
		return 0.2126 * normalizedRed + 0.7152 * normalizeGreen + 0.0722 * normalizeBlue;
	}

	private double normalizeColorComponent(int colorComponent) {
		double doubleColorComponent = colorComponent / 255.;
		return doubleColorComponent < 0.03928 ? doubleColorComponent / 12.92 :
		       Math.pow(((doubleColorComponent + 0.055) / 1.055), 2.4);
	}

	private SortedMap<Color, DataPoint> getImageColorMap(BufferedImage bim) {
		int width = bim.getWidth();
		int height = bim.getHeight();
		Map<Color, DataPoint> colorMap = new HashMap<>();

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rgb = bim.getRGB(i, j); //always returns TYPE_INT_ARGB
				int alpha = (rgb >> 24) & 0xFF;
				int red = (rgb >> 16) & 0xFF;
				int green = (rgb >> 8) & 0xFF;
				int blue = (rgb) & 0xFF;
				Color color = new Color(red, green, blue);

				if (colorMap.containsKey(color)) {
					colorMap.get(color).totalOccurrence++;
				} else {
					double relativeLuminosity = relativeLuminosity(color);
					colorMap.put(color, new DataPoint(relativeLuminosity));
				}
			}
		}

        TreeMap<Color, DataPoint> sortedMap = new TreeMap<>((c1, c2) -> {
            DataPoint dp1 = colorMap.get(c1);
            DataPoint dp2 = colorMap.get(c2);
            int occurrenceCompare = Integer.compare(dp2.getTotalOccurrence(), dp1.getTotalOccurrence());
            if (occurrenceCompare != 0) {
                return occurrenceCompare;
            }
            return Integer.compare(c1.getRGB(), c2.getRGB());
        });
        sortedMap.putAll(colorMap);

		return sortedMap;
	}

	private List<DataPoint> getLuminosityPresenceList(BufferedImage bim) {
		return new ArrayList<>(getImageColorMap(bim).values());
	}

	private Color getBackgroundColor(SortedMap<Color, DataPoint> colorMap, Color textColor) {
		if (colorMap.size() == 1) {
			Map.Entry<Color, DataPoint> entry = colorMap.entrySet().iterator().next();
			if (!textColor.equals(entry.getKey())) {
				return entry.getKey();
			}
			return null;
		}
        Color firstColor = colorMap.keySet().stream()
                .findFirst()
                .orElse(null);
        Color secondColor = colorMap.keySet().stream()
                .skip(1)
                .findFirst()
                .orElse(null);
		if (firstColor!= null && !NodeUtils.hasSimilarBackgroundColor(textColor, firstColor)) {
			return firstColor;
		} else if (secondColor!= null && !NodeUtils.hasSimilarBackgroundColor(textColor, secondColor)) {
			return secondColor;
		}
		return null;
	}
	private List<DataPoint> findLocalMaximums(List<DataPoint> source) {
		List<DataPoint> localMaximums = new ArrayList<>();
		boolean isPreviousLessThanCurrent = true;

		// Iterating over all points to check
		// local maximum
		for (int i = 0; i < source.size() - 1; i++) {
			// Condition for local maximum
			boolean isNextLessThanCurrent = source.get(i).totalOccurrence > source.get(i + 1).totalOccurrence;
			if (isNextLessThanCurrent && isPreviousLessThanCurrent) {
				localMaximums.add(source.get(i));
			}
			isPreviousLessThanCurrent = !isNextLessThanCurrent;
		}

		// Checking whether the last point is
		// local maximum or none
		if (isPreviousLessThanCurrent) {
			localMaximums.add(source.get(source.size() - 1));
		}

		return localMaximums;
	}

	private double[] get2MostPresentElements(List<DataPoint> source) {
		return source.size() == 1 ? new double[] {source.get(0).value, -1} : new double[] {source.get(0).value, source.get(1).value};
	}

	@Override
	public Double getPercent() {
		return 100.0d * processedTextChunks / textChunksNumber;
	}

	static class DataPoint implements Comparable<DataPoint> {

		private double value;
		private int totalOccurrence;

		public DataPoint() {
		}

		public DataPoint(double value) {
			this.value = value;
			this.totalOccurrence = 1;
		}

		public double getValue() {
			return value;
		}

		public int getTotalOccurrence() {
			return totalOccurrence;
		}

		public void setTotalOccurrence(int totalOccurrence) {
			this.totalOccurrence = totalOccurrence;
		}

		@Override
		public int compareTo(DataPoint o) {
			return Double.compare(this.value, o.value);
		}
	}

	@Override
	public WCAGProgressStatus getWCAGProgressStatus() {
		return WCAGProgressStatus.CONTRAST_DETECTION;
	}
}
