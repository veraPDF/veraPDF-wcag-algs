package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.verapdf.wcag.algorithms.entities.IDocument;
import org.verapdf.wcag.algorithms.entities.INode;
import org.verapdf.wcag.algorithms.entities.ITree;
import org.verapdf.wcag.algorithms.entities.JsonToPdfTree;
import org.verapdf.wcag.algorithms.entities.enums.SemanticType;
import org.verapdf.wcag.algorithms.entities.maps.SemanticTypeMapper;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.AccumulatedNodeConsumer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class AccumulatedNodeSemanticsCheckerTests {

	private static final double SEMANTIC_SCORE_TOLERANCE = 0.0001d;

	private final ISemanticsChecker semanticsChecker;
	private static final List<SemanticType> ignoredSemanticTypes = new ArrayList<>();

	static {
		ignoredSemanticTypes.add(SemanticType.LINK);
		ignoredSemanticTypes.add(SemanticType.TABLE);
		ignoredSemanticTypes.add(SemanticType.TABLE_BODY);
		ignoredSemanticTypes.add(SemanticType.TABLE_CELL);
		ignoredSemanticTypes.add(SemanticType.TABLE_ROW);
		ignoredSemanticTypes.add(SemanticType.TABLE_HEADER);
		ignoredSemanticTypes.add(SemanticType.BLOCK_QUOTE);
	}

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
				Arguments.of("13.json", 1d, SemanticType.SPAN),
				Arguments.of("subscript/subscript1.json", 0.8843d, SemanticType.PARAGRAPH),
				Arguments.of("superscript/superscript1.json", 0.8228d, SemanticType.PARAGRAPH),
				Arguments.of("superscript/superscript2.json", 0.8228d, SemanticType.PARAGRAPH),
				Arguments.of("superscript/superscript3.json", 0.8228d, SemanticType.PARAGRAPH),
				Arguments.of("superscript/superscript4.json", 0.8228d, SemanticType.PARAGRAPH),
				Arguments.of("superscript/superscript5.json", 0.8228d, SemanticType.PARAGRAPH),
				Arguments.of("superscript/superscript6.json", 0.8228d, SemanticType.PARAGRAPH));
	}

	@ParameterizedTest(name = "{index}: ({0}, {1}, {2}) => {0}")
	@MethodSource("semanticCorrectnessTestParams")
	void testSemanticCorrectness(String jsonPdfPath, double probability, SemanticType semanticType) throws IOException {
		IDocument document = JsonToPdfTree.getDocument("/files/" + jsonPdfPath);
		ITree tree = document.getTree();
		semanticsChecker.checkSemanticDocument(document);
		Assertions.assertEquals(probability, tree.getRoot().getCorrectSemanticScore(), SEMANTIC_SCORE_TOLERANCE);
		Assertions.assertEquals(semanticType, tree.getRoot().getSemanticType());
	}

	static Stream<Arguments> treeSemanticCorrectnessTestParams() {
		return Stream.of(
				Arguments.of("headings/Heading1.json"),
				Arguments.of("headings/Heading2.json"),
				Arguments.of("headings/Heading3.json"),
				Arguments.of("headings/Heading4.json"),
				Arguments.of("headings/heading_before_list.json"),
				Arguments.of("headings/heading_with_footnote.json"),
				Arguments.of("headings/headings_academic_abstract.json"),
				Arguments.of("headings/headings_book_chapter1.json"),
				Arguments.of("headings/headings_book_chapter2.json"),
				Arguments.of("headings/headings_danish_learn.json"),
				Arguments.of("headings/headings_danish_learn1.json"),
				Arguments.of("headings/headings_danish_learn_last_link.json"),
				Arguments.of("headings/headings_danish_learn_link.json"),
        		Arguments.of("headings/headings_magazine_danish_block_quote.json"),
				Arguments.of("headings/headings_magazine_danish2.json"),
				Arguments.of("headings/nutshell_heading.json"),
				Arguments.of("headings/span_as_heading.json"),
				Arguments.of("headings/nutshell_before_table.json"),
//				Arguments.of("headings/nutshell_contents.json"),
//				Arguments.of("headings/heading_Nota.De.Tweede.json"),
				Arguments.of("captions/caption1.json"),
				Arguments.of("captions/caption2.json"),
				Arguments.of("captions/caption3.json"),
				Arguments.of("captions/caption4.json"),
				Arguments.of("captions/caption5.json"),
				Arguments.of("titles/Title1.json"),
				Arguments.of("spans/Span1.json"),
				Arguments.of("paragraphs/one_line_vertical_paragraph.json"),
				Arguments.of("paragraphs/paragraph1.json")
		                );
	}

	@ParameterizedTest(name = "{index}: ({0}) => {0}")
	@MethodSource("treeSemanticCorrectnessTestParams")
	void testTreeSemanticCorrectness(String jsonPdfPath) throws IOException {
		IDocument document = JsonToPdfTree.getDocument("/files/" + jsonPdfPath);
		ITree tree = document.getTree();
		semanticsChecker.checkSemanticDocument(document);
		testTreeStructure(tree);
	}

	private void testTreeStructure(ITree tree) {
		for (INode node : tree) {
			if (node.getInitialSemanticType() != null && SemanticTypeMapper.containsType(node.getInitialSemanticType())
			    && !ignoredSemanticTypes.contains(node.getInitialSemanticType())) {
				Assertions.assertEquals(node.getInitialSemanticType(), node.getSemanticType());
				Assertions.assertTrue(node.getCorrectSemanticScore() >= AccumulatedNodeConsumer.MERGE_PROBABILITY_THRESHOLD);
			}
		}
	}
}
