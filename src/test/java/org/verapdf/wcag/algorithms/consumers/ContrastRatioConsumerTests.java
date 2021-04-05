package org.verapdf.wcag.algorithms.consumers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.verapdf.wcag.algorithms.entities.*;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.ContrastRatioConsumer;

import java.io.IOException;
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
				Arguments.of("1.4.3-t03-pass-a.pdf", "1.4.3-t03-pass-a.json", 4.5));
	}

	static Stream<Arguments> contrastTestFailParams() {
		return Stream.of(
				Arguments.of("hyphen-large-fail.pdf", "hyphen-large-fail.json", 3.0),
				Arguments.of("hyphen-regular-fail.pdf", "hyphen-regular-fail.json", 4.5),
				Arguments.of("1.4.3-t01-fail-a.pdf", "1.4.3-t01-fail-a.json", 3.0),
				Arguments.of("1.4.3-t02-fail-a.pdf", "1.4.3-t02-fail-a.json", 4.5));
	}

	@ParameterizedTest(name = "{index}: ({0}, {1}, {2}) => {0}")
	@MethodSource("contrastTestPassParams")
	void testColorContrastPass(String srcPdfPath, String jsonPdfPath, double ratioThreshold) throws IOException {
		INode root = JsonToPdfTree.getPdfTreeRoot(SRC_DIR + jsonPdfPath);
		ITree tree = new SemanticTree(root);
		ContrastRatioConsumer contrastRatioConsumer = new ContrastRatioConsumer(ROOT_DIR + srcPdfPath);

		tree.forEach(contrastRatioConsumer);
		tree.forEach(node -> {
			if (node.getChildren().size() == 0 && SemanticType.SPAN.equals(node.getSemanticType())) {
				List<TextChunk> textChunks = ((SemanticSpan)(node)).getLines();
				for	(TextChunk chunk : textChunks)	{
					Assertions.assertTrue(chunk.getContrastRatio() >= ratioThreshold);
				}
			}
		});
	}

	@ParameterizedTest(name = "{index}: ({0}, {1}, {2}) => {0}")
	@MethodSource("contrastTestFailParams")
	void testColorContrastFail(String srcPdfPath, String jsonPdfPath, double ratioThreshold) throws IOException {
		INode root = JsonToPdfTree.getPdfTreeRoot(SRC_DIR + jsonPdfPath);
		ITree tree = new SemanticTree(root);
		ContrastRatioConsumer contrastRatioConsumer = new ContrastRatioConsumer(ROOT_DIR + srcPdfPath);

		tree.forEach(contrastRatioConsumer);
		tree.forEach(node -> {
			if (node.getChildren().size() == 0 && SemanticType.SPAN.equals(node.getSemanticType())) {
				List<TextChunk> textChunks = ((SemanticSpan)(node)).getLines();
				for	(TextChunk chunk : textChunks)	{
					Assertions.assertTrue(chunk.getContrastRatio() < ratioThreshold);
				}
			}
		});
	}
}
