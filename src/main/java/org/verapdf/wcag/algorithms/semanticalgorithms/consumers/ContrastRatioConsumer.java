package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class ContrastRatioConsumer implements Consumer<INode> {

	private Map<INode, Double> colorContrastMap = new HashMap<>();
	private Map<Integer, BufferedImage> renderedPages = new HashMap<>();
	private final String sourcePdfPath;
	//private static final Logger logger = LoggerFactory.getLogger(ContrastRatioConsumer.class);
	private static final int RENDER_DPI = 144;
	private static final int PDF_DPI = 72;

	public ContrastRatioConsumer(String sourcePdfPath) {
		this.sourcePdfPath = sourcePdfPath;
	}

	public Map<INode, Double> getColorContrastMap() {
		return colorContrastMap;
	}

	private void calculateContrastRatio(INode node, INode parentNode) {
		BufferedImage renderedPage = renderedPages.get(node.getPageNumber());
		if (renderedPage == null) {
			try {
				renderedPage = renderPage(sourcePdfPath, node.getPageNumber());
				renderedPages.put(node.getPageNumber(), renderedPage);
				BoundingBox bBox = node.getBoundingBox();
				double dpiScaling = ((double) RENDER_DPI) / ((double) PDF_DPI);
				int x = (int) (Math.round(bBox.getLeftX()) * dpiScaling);
				int y = (int) (Math.round(bBox.getTopY()) * dpiScaling);
				int width = (int) (Math.round(bBox.getWidth()) * dpiScaling);
				int height = (int) (Math.round(bBox.getHeight()) * dpiScaling);
				BufferedImage targetBim = renderedPage.getSubimage(x, renderedPage.getHeight() - y, width,  height);
				double contrastRatio = getContrastRatio(targetBim);
				node.setContrastRatio(contrastRatio);
				parentNode.setContrastRatio(contrastRatio);
				colorContrastMap.put(node, contrastRatio);
			}
			catch (IOException e) {
				e.printStackTrace();
				//logger.error(e, e.getMessage());
			}
		}
	}

	@Override
	public void accept(INode node) {
		List<INode> children = node.getChildren();
		//check if node is leaf
		if (!children.isEmpty()) {
			for (INode childNode : children) {
				if (childNode.getChildren().isEmpty()) {
					calculateContrastRatio(childNode, node);
				}
			}
		}
	}

	private BufferedImage renderPage(String sourcePdfPath, Integer pageNumber) throws IOException {
		PDDocument document = PDDocument.load(getClass().getResourceAsStream(
				sourcePdfPath));
		PDFRenderer pdfRenderer = new PDFRenderer(document);
		return pdfRenderer.renderImageWithDPI(pageNumber, RENDER_DPI, ImageType.RGB);
	}

	private double getContrastRatio(BufferedImage image) {
		List<DataPoint> localMaximums = findLocalMaximums(getLuminosityPresenceList(image));
		double[] contrastColors = get2MostPresentElements(localMaximums);
		return getContrastRatio(contrastColors[0], contrastColors[1]);
	}

	public double getContrastRatio(double first, double second) {
		double l1 = Math.max(first, second);
		double l2 = Math.min(first, second);
		return (l1 + 0.05) / (l2 + 0.05);
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
