package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.verapdf.wcag.algorithms.entities.enums.TextType;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class ContrastRatioCheckerTests {

	static Stream<Arguments> contrastTestPassParams() {
		return Stream.of(
				Arguments.of("/files/colorcontrast/1.4.3-t01-pass-a.pdf", new int[]{235, 285, 380, 87}, TextType.LARGE),
				Arguments.of("/files/colorcontrast/1.4.3-t02-pass-a.pdf", new int[]{235, 235, 245, 57}, TextType.REGULAR),
				Arguments.of("/files/colorcontrast/1.4.3-t03-pass-a.pdf", new int[]{235, 525, 390, 62}, TextType.LOGO)
		                );
	}

	static Stream<Arguments> contrastTestFailParams() {
		return Stream.of(
				Arguments.of("/files/colorcontrast/1.4.3-t01-fail-a.pdf", new int[]{235, 285, 380, 87}, TextType.LARGE),
				Arguments.of("/files/colorcontrast/1.4.3-t02-fail-a.pdf", new int[]{235, 235, 245, 57}, TextType.REGULAR)
		                );
	}

	@ParameterizedTest(name = "{index}: ({0} ({1}) => {0}))")
	@MethodSource("contrastTestPassParams")
	public void createImagesFromPdfPdfBoxPassTest(String sourcePath, int[] bBox, TextType type) throws IOException {
		PDDocument document = PDDocument.load(getClass().getResourceAsStream(
				sourcePath));
		PDFRenderer pdfRenderer = new PDFRenderer(document);
		for (int page = 0; page < document.getNumberOfPages(); ++page) {
			BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
			BufferedImage targetBim = bim.getSubimage(bBox[0], bBox[1], bBox[2], bBox[3]);

			ContrastRatioChecker checker = new ContrastRatioChecker();

			Assertions.assertTrue(checker.isTextContrastRatioCompliant(targetBim, type, false));
		}
		document.close();
	}

	@ParameterizedTest(name = "{index}: ({0} ({1} , {2}) => {0}))")
	@MethodSource("contrastTestFailParams")
	public void createImagesFromPdfPdfBoxFailTest(String sourcePath, int[] bBox, TextType type) throws IOException {
		PDDocument document = PDDocument.load(getClass().getResourceAsStream(
				sourcePath));
		PDFRenderer pdfRenderer = new PDFRenderer(document);
		for (int page = 0; page < document.getNumberOfPages(); ++page) {
			BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
			BufferedImage targetBim = bim.getSubimage(bBox[0], bBox[1], bBox[2], bBox[3]);

			ContrastRatioChecker checker = new ContrastRatioChecker();

			Assertions.assertFalse(checker.isTextContrastRatioCompliant(targetBim, type, false));
		}
		document.close();
	}

	private List<String> presentListAsHistogram(List<ContrastRatioChecker.DataPoint> source) {
		List<String> result = new ArrayList<>();
		int absoluteMax = 0;
		for (ContrastRatioChecker.DataPoint entry : source) {
			if (entry.getTotalOccurrence() > absoluteMax) {
				absoluteMax = entry.getTotalOccurrence();
			}
		}
		for (ContrastRatioChecker.DataPoint entry : source) {
			StringBuilder stb = new StringBuilder();
			stb.append(entry.getValue()).append("  - ").append(entry.getTotalOccurrence()).append(" :   ");
			int relativePointCount = entry.getTotalOccurrence() * 1500 / absoluteMax / 10;
			for (int i = 0; i < relativePointCount; i++) {
				stb.append('*');
			}
			result.add(stb.toString());
		}
		return result;
	}

	private List<String> presentMapAsPercentHistogram(List<ContrastRatioChecker.DataPoint> source) {
		List<String> result = new ArrayList<>();
		int absoluteMax = 0;
		for (ContrastRatioChecker.DataPoint entry : source) {
			if (entry.getTotalOccurrence() > absoluteMax) {
				absoluteMax = entry.getTotalOccurrence();
			}
		}
		int[] percentArray = new int[101];
		for (ContrastRatioChecker.DataPoint entry : source) {
			percentArray[(int) Math.round(entry.getValue() * 100)] += entry.getTotalOccurrence();
		}

		for (int j = 0; j < 101; j++) {
			percentArray[j] = percentArray[j] * 1000 / absoluteMax / 10;
		}

		System.out.println("Percent : value 'histogram'");
		for (int j = 0; j < 101; j++) {
			StringBuilder stb = new StringBuilder();
			stb.append(j).append('-').append(j + 1).append(" : ");
			for (int k = 0; k <= percentArray[j]; k++) {
				stb.append('*');
			}
			result.add(stb.toString());
		}
		return result;
	}

	private int[] presentListAsHistogramData(List<ContrastRatioChecker.DataPoint> source) {
		int absoluteMax = 0;
		for (ContrastRatioChecker.DataPoint entry : source) {
			if (entry.getTotalOccurrence() > absoluteMax) {
				absoluteMax = entry.getTotalOccurrence();
			}
		}
		int[] percentArray = new int[101];
		for (ContrastRatioChecker.DataPoint entry : source) {
			percentArray[(int) Math.floor(entry.getValue() * 100)] += entry.getTotalOccurrence();
		}
		return percentArray;
	}

	private void printHistogramToFile(int[] dataArray, String filePath) throws IOException{
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		for (int i = 0; i < dataArray.length; i++) {
			dataset.addValue(dataArray[i], "", String.valueOf(i));
		}
		String plotTitle = "Relative luminance";
		String xaxis = "luminance percent";
		String yaxis = "pixelsPresent";
		PlotOrientation orientation = PlotOrientation.VERTICAL;
		boolean showLegend = false;
		boolean toolTips = false;
		boolean urls = false;
		JFreeChart chart = ChartFactory.createBarChart(plotTitle, xaxis, yaxis,
		                                               dataset, orientation, showLegend, toolTips, urls);
		int width = 2500;
		int height = 1500;
		ChartUtilities.saveChartAsPNG(new File(filePath), chart, width, height);
	}
}
