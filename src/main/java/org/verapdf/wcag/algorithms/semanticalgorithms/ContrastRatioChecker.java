package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.verapdf.wcag.algorithms.entities.IDocument;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.IPage;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.enums.TextType;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.ContrastRatioConsumer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class ContrastRatioChecker {

	/**
	 * Traverses the document semantic tree and updates contrast ratio parameter of it's nodes. Uses pdf document
	 * associated with the tree to determine contrast ratio by rendering it's pages.
	 *
	 * @param tree {@link ITree tree} with nodes to update with calculated contrast ratio
	 * @param pdfName {@link String} path to the pdf document associated with given tree
	 */
	public void checkSemanticTree(ITree tree, String pdfName) {
		Consumer<INode> v = new ContrastRatioConsumer(pdfName);
		tree.forEach(v);
	}

	public void checkDocument(IDocument document, String pdfName) {
		ContrastRatioConsumer v = new ContrastRatioConsumer(pdfName);
		if (document.getTree() != null) {
			document.getTree().forEach(v);
		}
		for (IPage page : document.getPages()) {
			BufferedImage renderedPage = v.getRenderPage(page.getPageNumber());
			page.getArtifacts().stream()
				.filter(chunk -> chunk instanceof TextChunk)
				.forEach(chunk -> v.calculateContrastRation((TextChunk)chunk, renderedPage));
		}
	}

	/**
	 * Determines contrast ratio of two color based on their relative luminance.
	 * The resulting contrast ratio ranges from 1.0 to 21.0
	 *
	 * @param first relative luminance of the first color
	 * @param second relative luminance of the first color
	 * @return contrast ratio of the given colors
	 */
	public double getContrastRatio(double first, double second) {
		double l1 = Math.max(first, second);
		double l2 = Math.min(first, second);
		return (l1 + 0.05) / (l2 + 0.05);
	}

	boolean isTextContrastRatioCompliant(BufferedImage sourceTextImage, TextType type, boolean isHighVisibility) {
		List<DataPoint> localMaximums = findLocalMaximums(getLuminosityPresenceList(sourceTextImage));
		double[] contrastColors = get2MostPresentElements(localMaximums);
		double colorContrast = getContrastRatio(contrastColors[0], contrastColors[1]);
		return isContrastRatioCompliant(colorContrast, type, isHighVisibility);
	}

	private boolean isContrastRatioCompliant(double colorContrast, TextType type, boolean isHighVisibility) {
		switch (type) {
			case REGULAR:
				return isHighVisibility ? colorContrast >= 7.0 : colorContrast >= 4.5;
			case LARGE:
				return isHighVisibility ? colorContrast >= 4.5 : colorContrast >= 3.0;
			case LOGO:
				return true;
			default:
				break;
		}
		return false;
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

	private List<DataPoint> getLuminosityPresenceList(BufferedImage bim) {
		int width = bim.getWidth();
		int height = bim.getHeight();
		Map<Color, DataPoint> colorMap = new HashMap<>();

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rgb = bim.getRGB(i, j); //always returns TYPE_INT_ARGB
				int alpha = (rgb >> 24) & 0xFF;// not used for now, should remove?
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

		return new ArrayList<>(new TreeSet<>(colorMap.values()));
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
		double absoluteMaxPresent = -1;
		double secondMaxPresent = -1;
		int max = 0;
		int secondMax = 0;

		for (DataPoint dataPoint: source) {
			if (dataPoint.totalOccurrence >= max) {
				secondMaxPresent = absoluteMaxPresent;
				secondMax = max;
				absoluteMaxPresent = dataPoint.value;
				max = dataPoint.totalOccurrence;
			} else if (dataPoint.totalOccurrence >= secondMax) {
				secondMax = dataPoint.totalOccurrence;
				secondMaxPresent = dataPoint.value;
			}
		}
		return new double[]{absoluteMaxPresent, secondMaxPresent};
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

}
