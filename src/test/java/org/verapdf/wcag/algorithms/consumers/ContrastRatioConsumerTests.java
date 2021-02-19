package org.verapdf.wcag.algorithms.consumers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.JsonToPdfTree;
import org.verapdf.wcag.algorithms.entities.SemanticTree;
import org.verapdf.wcag.algorithms.semanticalgorithms.ContrastRatioChecker;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.ContrastRatioConsumer;
import sun.java2d.pipe.AAShapePipe;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

public class ContrastRatioConsumerTests {

	private final ContrastRatioConsumer contrastRatioConsumer = null;

	private static final String SRC_DIR = "/files/colorcontrast/";

	public ContrastRatioConsumerTests() {
	}

	static Stream<Arguments> contrastTestPassParams() {
		return Stream.of(
				Arguments.of("1.4.3-t01-pass-a.pdf", "1.4.3-t01-pass-a.json", 3.0),
				Arguments.of("1.4.3-t02-pass-a.pdf", "1.4.3-t02-pass-a.json", 4.5),
				Arguments.of("1.4.3-t03-pass-a.pdf", "1.4.3-t03-pass-a.json", 4.5));
	}

	static Stream<Arguments> contrastTestFailParams() {
		return Stream.of(
				Arguments.of("1.4.3-t01-fail-a.pdf", "1.4.3-t01-fail-a.json", 3.0),
				Arguments.of("1.4.3-t02-fail-a.pdf", "1.4.3-t02-fail-a.json", 4.5));
	}

	@ParameterizedTest(name = "{index}: ({0}, {1}, {2}) => {0}")
	@MethodSource("contrastTestPassParams")
	void testColorContrastPass(String srcPdfPath, String jsonPdfPath, double ratioThreshold) throws IOException {
		INode root = JsonToPdfTree.getPdfTreeRoot(SRC_DIR + jsonPdfPath);
		ITree tree = new SemanticTree(root);
		ContrastRatioConsumer contrastRatioConsumer = new ContrastRatioConsumer(SRC_DIR + srcPdfPath);

		tree.forEach(contrastRatioConsumer);
		//Map<INode, Double> contrastMap = contrastRatioConsumer.getColorContrastMap();
		//Assertions.assertEquals(1, contrastMap.size());
		tree.forEach(node -> {
			if (node.getChildren().size() == 0) {
				Assertions.assertTrue(node.getContrastRatio() >= ratioThreshold);
			}
		});
		//Assertions.assertEquals(1d, tree.getRoot().getCorrectSemanticScore(), 0d);
	}

	@ParameterizedTest(name = "{index}: ({0}, {1}, {2}) => {0}")
	@MethodSource("contrastTestFailParams")
	void testColorContrastFail(String srcPdfPath, String jsonPdfPath, double ratioThreshold) throws IOException {
		INode root = JsonToPdfTree.getPdfTreeRoot(SRC_DIR + jsonPdfPath);
		ITree tree = new SemanticTree(root);
		ContrastRatioConsumer contrastRatioConsumer = new ContrastRatioConsumer(SRC_DIR + srcPdfPath);

		tree.forEach(contrastRatioConsumer);
		tree.forEach(node -> Assertions.assertTrue(node.getContrastRatio() < ratioThreshold));
		//Assertions.assertEquals(1d, tree.getRoot().getCorrectSemanticScore(), 0d);
	}
}
