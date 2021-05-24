package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.JsonToPdfTree;
import org.verapdf.wcag.algorithms.entities.SemanticTree;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;

import java.io.IOException;
import java.util.stream.Stream;

class AccumulatedNodeSemanticsCheckerTests {

	private static final double SEMANTIC_SCORE_TOLERANCE = 0.0001d;

	private final ISemanticsChecker semanticsChecker;

	public AccumulatedNodeSemanticsCheckerTests() {
		this.semanticsChecker = new AccumulatedNodeSemanticChecker();
	}

	static Stream<Arguments> semanticCorrectnessTestParams() {
		return Stream.of(
				Arguments.of("0.json", 0.042d, SemanticType.PARAGRAPH),
				Arguments.of("1.json", 1d, SemanticType.PARAGRAPH),
				Arguments.of("2.json", 1d, SemanticType.SPAN),
				Arguments.of("3.json", 1d, SemanticType.PARAGRAPH),
				Arguments.of("4.json", 1d, SemanticType.PARAGRAPH),
				Arguments.of("5.json", 1d, SemanticType.PARAGRAPH),
				Arguments.of("6.json", 1d, SemanticType.PARAGRAPH),
				Arguments.of("7.json", 1d, SemanticType.PARAGRAPH),
				Arguments.of("8.json", 1d, SemanticType.PARAGRAPH),
				Arguments.of("9.json", 1d, SemanticType.PARAGRAPH),
				Arguments.of("10.json", 1d, SemanticType.PARAGRAPH),
				Arguments.of("11.json", 1d, SemanticType.PARAGRAPH),
				Arguments.of("12.json", 0.9885d, SemanticType.PARAGRAPH),
				Arguments.of("subscript1.json", 0.8843d, SemanticType.PARAGRAPH),
				Arguments.of("superscript1.json", 0.8228d, SemanticType.PARAGRAPH),
				Arguments.of("superscript2.json", 0.8228d, SemanticType.PARAGRAPH),
				Arguments.of("superscript3.json", 0.8228d, SemanticType.PARAGRAPH),
				Arguments.of("superscript4.json", 0.8228d, SemanticType.PARAGRAPH),
				Arguments.of("superscript5.json", 0.8228d, SemanticType.PARAGRAPH),
				Arguments.of("superscript6.json", 0.8228d, SemanticType.PARAGRAPH));
	}

	@ParameterizedTest(name = "{index}: ({0}, {1}, {2}) => {0}")
	@MethodSource("semanticCorrectnessTestParams")
	void testSemanticCorrectness(String jsonPdfPath, double probability, SemanticType semanticType) throws IOException {
		INode root = JsonToPdfTree.getPdfTreeRoot("/files/" + jsonPdfPath);
		ITree tree = new SemanticTree(root);
		semanticsChecker.checkSemanticTree(tree);
		Assertions.assertEquals(probability, tree.getRoot().getCorrectSemanticScore(), SEMANTIC_SCORE_TOLERANCE);
		Assertions.assertEquals(semanticType, tree.getRoot().getSemanticType());
	}
}
