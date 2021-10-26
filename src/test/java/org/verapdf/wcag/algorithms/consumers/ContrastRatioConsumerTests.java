package org.verapdf.wcag.algorithms.consumers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.verapdf.wcag.algorithms.entities.*;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.content.TextLine;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.ContrastRatioConsumer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ContrastRatioConsumerTests {

	private final ContrastRatioConsumer contrastRatioConsumer = null;

	private static final String SRC_DIR = "/files/colorcontrast/";
	private static final String ROOT_DIR = "./src/test/resources/files/colorcontrast/";

	public ContrastRatioConsumerTests() {
	}

	static Stream<Arguments> contrastTestPassParams() {
		return Stream.of(
				Arguments.of("hyphen-large-pass.pdf", "hyphen-large-pass.json", 3.0),
				Arguments.of("hyphen-regular-pass.pdf", "hyphen-regular-pass.json", 4.5),
				Arguments.of("1.4.3-t01-pass-a.pdf", "1.4.3-t01-pass-a.json", 3.0),
				Arguments.of("1.4.3-t02-pass-a.pdf", "1.4.3-t02-pass-a.json", 4.5),
				Arguments.of("1.4.3-t03-pass-a.pdf", "1.4.3-t03-pass-a.json", 4.5),
				//TODO fix problem with incorrect contrast detection in case on real contrast ratio 1:1 and
				// extra color pixels on the edges of the bbox
				Arguments.of("contrast-fail-black-on-black.pdf", "contrast-fail-black-on-black.json", 4.5));
	}

	static Stream<Arguments> contrastTestFailParams() {
		return Stream.of(
				Arguments.of("hyphen-large-fail.pdf", "hyphen-large-fail.json", 3.0),
				Arguments.of("hyphen-regular-fail.pdf", "hyphen-regular-fail.json", 4.5),
				Arguments.of("1.4.3-t01-fail-a.pdf", "1.4.3-t01-fail-a.json", 3.0),
				Arguments.of("1.4.3-t02-fail-a.pdf", "1.4.3-t02-fail-a.json", 4.5),
				Arguments.of("contrast-fail-white-on-white.pdf", "contrast-fail-white-on-white.json", 4.5),
				Arguments.of("contrast-fail-gray-on-gray.pdf", "contrast-fail-gray-on-gray.json", 4.5),
				Arguments.of("contrast-fail-white-on-gray.pdf", "contrast-fail-white-on-gray.json", 4.5));
	}

	@ParameterizedTest(name = "{index}: ({0}, {1}, {2}) => {0}")
	@MethodSource("contrastTestPassParams")
	void testColorContrastPass(String srcPdfPath, String jsonPdfPath, double ratioThreshold) throws IOException {
		IDocument document = JsonToPdfTree.getDocument(SRC_DIR + jsonPdfPath);
		ITree tree = document.getTree();
		ContrastRatioConsumer contrastRatioConsumer = new ContrastRatioConsumer(ROOT_DIR + srcPdfPath);

		tree.forEach(contrastRatioConsumer);
		tree.forEach(node -> {
			if (node.getChildren().size() == 0 && SemanticType.SPAN.equals(node.getSemanticType())) {
				List<TextLine> textLines = ((SemanticSpan)(node)).getLines();
				for (TextLine line : textLines) {
					for	(TextChunk chunk : line.getTextChunks())	{
						Assertions.assertTrue(chunk.getContrastRatio() >= ratioThreshold);
					}
				}
			}
		});
	}

	static Stream<Arguments> contrastCompletedWithoutExceptionsParams() {
		return Stream.of(
				Arguments.of("bbox-outside-page.pdf", "bbox-outside-page.json", 4.5));
	}

	@ParameterizedTest(name = "{index}: ({0}, {1}, {2}) => {0}")
	@MethodSource("contrastCompletedWithoutExceptionsParams")
	void testColorContrastCompletedWithoutExceptions(String srcPdfPath, String jsonPdfPath, double ratioThreshold) throws IOException {
		IDocument document = JsonToPdfTree.getDocument(SRC_DIR + jsonPdfPath);
		ITree tree = document.getTree();
		ContrastRatioConsumer contrastRatioConsumer = new ContrastRatioConsumer(ROOT_DIR + srcPdfPath);

		tree.forEach(contrastRatioConsumer);
		Assertions.assertTrue(true);
	}

	@ParameterizedTest(name = "{index}: ({0}, {1}, {2}) => {0}")
	@MethodSource("contrastTestFailParams")
	void testColorContrastFail(String srcPdfPath, String jsonPdfPath, double ratioThreshold) throws IOException {
		IDocument document = JsonToPdfTree.getDocument(SRC_DIR + jsonPdfPath);
		ITree tree = document.getTree();
		ContrastRatioConsumer contrastRatioConsumer = new ContrastRatioConsumer(ROOT_DIR + srcPdfPath);

		tree.forEach(contrastRatioConsumer);
		tree.forEach(node -> {
			if (node.getChildren().size() == 0 && SemanticType.SPAN.equals(node.getSemanticType())) {
				List<TextLine> textLines = ((SemanticSpan)(node)).getLines();
				for (TextLine line : textLines) {
					for	(TextChunk chunk : line.getTextChunks())	{
						Assertions.assertTrue(chunk.getContrastRatio() < ratioThreshold);
					}
				}
			}
		});
	}

	@Test
	void bBoxWidthZeroValueTest() {
		SemanticSpan nodeToCheck = new SemanticSpan();
		nodeToCheck.add(new TextLine(new TextChunk(new BoundingBox(0, new double [] {100, 100, 100.1, 120}), ".", 14, 118)));
		nodeToCheck.setPageNumber(0);
		Assertions.assertEquals(0.1, nodeToCheck.getLines().get(0).getTextChunks().get(0).getBoundingBox().getWidth(), 0.0001);
		ContrastRatioConsumer contrastRatioConsumer = new ContrastRatioConsumer(ROOT_DIR + "1.4.3-t02-fail-a.pdf");
		contrastRatioConsumer.accept(nodeToCheck);
		Assertions.assertEquals(0.1, nodeToCheck.getLines().get(0).getTextChunks().get(0).getBoundingBox().getWidth(), 0.0001);
	}

	@Test
	void bBoxHeightZeroValueTest() {
		SemanticSpan nodeToCheck = new SemanticSpan();
		nodeToCheck.add(new TextLine(new TextChunk(new BoundingBox(0, new double [] {100, 100, 110, 100.1}), ".", 14, 118)));
		nodeToCheck.setPageNumber(0);
		Assertions.assertEquals(0.1, nodeToCheck.getLines().get(0).getTextChunks().get(0).getBoundingBox().getHeight(), 0.0001);
		ContrastRatioConsumer contrastRatioConsumer = new ContrastRatioConsumer(ROOT_DIR + "1.4.3-t02-fail-a.pdf");
		contrastRatioConsumer.accept(nodeToCheck);
		Assertions.assertEquals(0.1, nodeToCheck.getLines().get(0).getTextChunks().get(0).getBoundingBox().getHeight(), 0.0001);
	}
}
