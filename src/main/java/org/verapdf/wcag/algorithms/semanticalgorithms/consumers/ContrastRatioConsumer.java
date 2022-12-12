package org.verapdf.wcag.algorithms.semanticalgorithms.consumers;

import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import org.apache.pdfbox.jbig2.JBIG2ImageReaderSpi;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.SemanticTextNode;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextColumn;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.NodeUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TextChunkUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.WCAGProgressStatus;

import javax.imageio.spi.IIORegistry;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ContrastRatioConsumer extends WCAGConsumer implements Consumer<INode> {

	private final Map<Integer, BufferedImage> renderedPages = new HashMap<>();
	private final String sourcePdfPath;
	private static final Logger logger = Logger.getLogger(ContrastRatioConsumer.class.getCanonicalName());
	private static final int RENDER_DPI = 144;
	private static final int PDF_DPI = 72;
	private static final double LUMINOSITY_DIFFERENCE = 0.001;
	private int processedTextChunks;
	private final Integer textChunksNumber;

	static {
		wcagProgressStatus = WCAGProgressStatus.CONTRAST_DETECTION;
	}

	public ContrastRatioConsumer(String sourcePdfPath, int ... textChunksNumber) {
		this.sourcePdfPath = sourcePdfPath;
		IIORegistry registry = IIORegistry.getDefaultInstance();
		registry.registerServiceProvider(new J2KImageReaderSpi());
		registry.registerServiceProvider(new JBIG2ImageReaderSpi());
		this.processedTextChunks = 0;
		this.textChunksNumber = textChunksNumber.length > 0 ? textChunksNumber[0] : null;
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

	public BufferedImage getRenderPage(int pageNumber) {
		BufferedImage renderedPage = renderedPages.get(pageNumber);
		if (renderedPage == null) {
			try (PDDocument document = PDDocument.load(new FileInputStream(sourcePdfPath))) {
				renderedPage = renderPage(document, pageNumber);
				renderedPages.clear();
				renderedPages.put(pageNumber, renderedPage);
			}
			catch (IOException | IllegalArgumentException e) {
				e.printStackTrace();
				logger.warning(e.getMessage());
			}
		}
		return renderedPage;
	}

	public void calculateContrastRatio(TextChunk textChunk) {
		BufferedImage renderedPage = getRenderPage(textChunk.getPageNumber());
		calculateContrastRation(textChunk, renderedPage);
	}

	private void calculateContrastRatio(SemanticTextNode node) {
		BufferedImage renderedPage = getRenderPage(node.getPageNumber());
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
			textChunk.setContrastRatio(Integer.MAX_VALUE);
			return;
		}

		BoundingBox bBox = textChunk.getBoundingBox();
		double dpiScaling = ((double) RENDER_DPI) / ((double) PDF_DPI);
		int renderedPageWidth = renderedPage.getRaster().getWidth();
		int renderedPageHeight = renderedPage.getRaster().getHeight();
		BoundingBox pageBBox = new BoundingBox(textChunk.getPageNumber(),0, 0, renderedPageWidth, renderedPageHeight);

		BoundingBox scaledBBox = new BoundingBox(textChunk.getPageNumber(), bBox.getLeftX() * dpiScaling,
				bBox.getBottomY() * dpiScaling,
				bBox.getRightX() * dpiScaling,
				bBox.getTopY() * dpiScaling);
		boolean isOverlappingBox = scaledBBox.overlaps(pageBBox);
		if (isOverlappingBox) {
			scaledBBox = scaledBBox.cross(pageBBox);
		} else if (!pageBBox.contains(scaledBBox)) {
			textChunk.setContrastRatio(Integer.MAX_VALUE);
			return;
		}
		int x = (int) (Math.round(scaledBBox.getLeftX()));
		int y = (int) (Math.round(scaledBBox.getTopY()));
		int width = getIntegerBBoxValueForProcessing(scaledBBox.getWidth(), 1);
		int height = getIntegerBBoxValueForProcessing(scaledBBox.getHeight(), 1);
		if (width <= 1 || height <= 1) {
			textChunk.setContrastRatio(Integer.MAX_VALUE);
			return;
		}
		try {
			BufferedImage targetBim = renderedPage.getSubimage(x, renderedPage.getHeight() - y, width,  height);
			double contrastRatio = getContrastRatio(targetBim, textChunk);
			textChunk.setContrastRatio(contrastRatio);
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage());
		}
	}

	private double [] convertCmykToRgb(double [] cmykColorComponentArray) {
		double [] result = new double[3];
		if (cmykColorComponentArray.length == 4) {
			double black = 1 - cmykColorComponentArray[3];
			for (int i = 0; i < 3; i++) {
				result[i] = (1 - cmykColorComponentArray[i]) * black;
			}
		}
		return result;
	}

	private Color getTextColorFromComponentArray(double [] colorComponentArray) {
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

	private Color makeRgbColorFromDoubleValues(double [] colorComponentArray) {
		assert colorComponentArray.length == 3;
		return new Color(convertDoubleColorValueToRgbInteger(colorComponentArray[0]),
		                 convertDoubleColorValueToRgbInteger(colorComponentArray[1]),
		                 convertDoubleColorValueToRgbInteger(colorComponentArray[2]));
	}

	private int convertDoubleColorValueToRgbInteger(double value) {
		int result = (int) Math.floor(value * 256);
		if (result > 255) {
			result = 255;
		} else if (result < 0) {
			result = 0;
		}
		return result;
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

	private double getContrastRatio(BufferedImage image, TextChunk textChunk) {
		double [] textChunkOriginalColor = textChunk.getFontColor();
		Color textColor = getTextColorFromComponentArray(textChunkOriginalColor);
		double textLuminosity = 0;
		double approximatedTextLuminosity = 0;
		if (textColor != null) {
			textLuminosity = relativeLuminosity(textColor);
			approximatedTextLuminosity = textLuminosity;
			double diff = 1.0;
			Map<Color, DataPoint> imageColorMap = getImageColorMap(image);
			textChunk.setBackgroundColor(checkForBackgroundColor(imageColorMap, textColor));
			List<DataPoint> dpFullArray = new ArrayList<>(new TreeSet<>(imageColorMap.values()));
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

	private double[] checkForBackgroundColor(Map<Color, DataPoint> imageColorMap, Color textColor) {
		Color backgroundColor = getBackgroundColor(imageColorMap, textColor);
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

	private Map<Color, DataPoint> getImageColorMap(BufferedImage bim) {
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

		return colorMap;
	}

	private List<DataPoint> getLuminosityPresenceList(BufferedImage bim) {
		return new ArrayList<>(new TreeSet<>(getImageColorMap(bim).values()));
	}

	private Color getBackgroundColor(Map<Color, DataPoint> colorMap, Color textColor) {
		if (colorMap.size() == 1) {
			Map.Entry<Color, DataPoint> entry = colorMap.entrySet().iterator().next();
			if (!textColor.equals(entry.getKey())) {
				return entry.getKey();
			}
			return null;
		}
		List<Integer> sortedOccurrences = colorMap.values()
		                                         .stream().map(DataPoint::getTotalOccurrence)
		                                         .sorted().collect(Collectors.toList());
		int firstFrequency = sortedOccurrences.get(sortedOccurrences.size() - 1);
		int secondFrequency = sortedOccurrences.get(sortedOccurrences.size() - 2);
		Color firstColor = null;
		Color secondColor = null;
		for (Map.Entry<Color, DataPoint> entry : colorMap.entrySet()) {
			if (firstColor == null && entry.getValue().getTotalOccurrence() == firstFrequency) {
				firstColor = entry.getKey();
			}
			if (secondColor == null && entry.getValue().getTotalOccurrence() == secondFrequency) {
				secondColor = entry.getKey();
			}
		}
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

	public int getProcessedTextChunks() {
		return processedTextChunks;
	}

	@Override
	public Double getPercent() {
		if (textChunksNumber == null) {
			return null;
		}
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
}
