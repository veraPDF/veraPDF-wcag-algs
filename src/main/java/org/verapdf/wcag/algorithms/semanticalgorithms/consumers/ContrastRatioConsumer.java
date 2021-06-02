package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticSpan;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TextChunkUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ContrastRatioConsumer implements Consumer<INode> {

	private Map<Integer, BufferedImage> renderedPages = new HashMap<>();
	private final String sourcePdfPath;
	private static final Logger logger = Logger.getLogger(ContrastRatioConsumer.class.getCanonicalName());
	private static final int RENDER_DPI = 144;
	private static final int PDF_DPI = 72;

	public ContrastRatioConsumer(String sourcePdfPath) {
		this.sourcePdfPath = sourcePdfPath;
	}

	@Override
	public void accept(INode node) {
		if (node.getChildren().isEmpty() && (node instanceof SemanticSpan)) {
			calculateContrastRatio((SemanticSpan) node);
		}
	}

	public double getContrastRatio(double first, double second) {
		double l1 = Math.max(first, second);
		double l2 = Math.min(first, second);
		return (l1 + 0.05) / (l2 + 0.05);
	}

	private void calculateContrastRatio(SemanticSpan node) {
		BufferedImage renderedPage = renderedPages.get(node.getPageNumber());
		if (renderedPage == null) {
			try (PDDocument document = PDDocument.load(new FileInputStream(sourcePdfPath))) {
				renderedPage = renderPage(document, node.getPageNumber());
				renderedPages.put(node.getPageNumber(), renderedPage);
			}
			catch (IOException e) {
				e.printStackTrace();
				logger.warning(e.getMessage());
			}
		}

		if (renderedPage != null) {
			for (TextLine textLine : node.getLines()) {
				for (TextChunk textChunk : textLine.getTextChunks()) {
					if ((textChunk.getValue() != null && TextChunkUtils.isSpaceChunk(textChunk))) {
						textChunk.setContrastRatio(Integer.MAX_VALUE);
						continue;
					}
					BoundingBox bBox = textChunk.getBoundingBox();
					double dpiScaling = ((double) RENDER_DPI) / ((double) PDF_DPI);
					int x = (int) (Math.round(bBox.getLeftX()) * dpiScaling);
					int y = (int) (Math.round(bBox.getTopY()) * dpiScaling);
					int width = getIntegerBBoxValueForProcessing(bBox.getWidth(), dpiScaling);
					int height = getIntegerBBoxValueForProcessing(bBox.getHeight(), dpiScaling);
					try {
						BufferedImage targetBim = renderedPage.getSubimage(x, renderedPage.getHeight() - y, width,  height);
						double contrastRatio = getContrastRatio(targetBim);
						textChunk.setContrastRatio(contrastRatio);
					} catch (Exception e) {
						logger.log(Level.WARNING, e.getMessage());
					}
				}

			}
		}
	}

	private int getIntegerBBoxValueForProcessing(double initialValue, double dpiScaling) {
		int result = (int) (Math.round(initialValue * dpiScaling));
		if (result <= 0) {
			result = 1;
			logger.warning("The resulting target buffered image width is <= 0. Fall back to " + result);
		}
		return result;
	}

	private BufferedImage renderPage(PDDocument document, Integer pageNumber) throws IOException {
		RenderingHints renderingHints = new RenderingHints(null);
		renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		PDFRenderer pdfRenderer = new PDFRenderer(document);
		pdfRenderer.setRenderingHints(renderingHints);
		return pdfRenderer.renderImageWithDPI(pageNumber, RENDER_DPI, ImageType.RGB);
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

	private List<DataPoint> getLuminosityPresenceList(BufferedImage bim) {
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
