package org.verapdf.wcag.algorithms.semanticalgorithms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.verapdf.wcag.algorithms.entities.*;
import org.verapdf.wcag.algorithms.semanticalgorithms.consumers.*;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

class RepeatedCharactersTests {

	static Stream<Arguments> repeatedCharactersDetectionTestParams() {
		return Stream.of(
				Arguments.of("beginning_end.json", new RepeatedCharacters[]{new RepeatedCharacters(false, 5),
				                                                            new RepeatedCharacters(false, 8)}),
				Arguments.of("ctrl shift space.json", new RepeatedCharacters[]{new RepeatedCharacters(false, 5)}),
				Arguments.of("repeated characters in table.json", new RepeatedCharacters[]{new RepeatedCharacters(false, 7),
				                                                                           new RepeatedCharacters(false, 4)}),
				Arguments.of("repeated_characters_dots.json", new RepeatedCharacters[]{new RepeatedCharacters(true, 36),
				                                                                       new RepeatedCharacters(true, 36),
				                                                                       new RepeatedCharacters(true, 36)}),
				Arguments.of("repeated_characters_in_subscript_superscript.json", new RepeatedCharacters[]{new RepeatedCharacters(true, 11),
				                                                                                           new RepeatedCharacters(true, 14)}),
				Arguments.of("repeated_characters_letters.json", new RepeatedCharacters[]{new RepeatedCharacters(true, 3),
				                                                                          new RepeatedCharacters(true, 4),
				                                                                          new RepeatedCharacters(true, 5),
				                                                                          new RepeatedCharacters(true, 6)}),
				Arguments.of("repeated_characters_numbers.json", new RepeatedCharacters[]{new RepeatedCharacters(true, 10)}),
				Arguments.of("repeated_characters_spaces.json", new RepeatedCharacters[]{new RepeatedCharacters(false, 50)}),
				Arguments.of("repeated_characters_underscore.json", new RepeatedCharacters[]{new RepeatedCharacters(true, 72),
				                                                                             new RepeatedCharacters(true, 72),
				                                                                             new RepeatedCharacters(true, 72)}),
				Arguments.of("repeated_characters_with_big_character_spacing.json", new RepeatedCharacters[]{new RepeatedCharacters(true, 19),
				                                                                                             new RepeatedCharacters(true, 14),
				                                                                                             new RepeatedCharacters(true, 21)}),
				Arguments.of("space_tab.json", new RepeatedCharacters[]{new RepeatedCharacters(false, 5),
				                                                        new RepeatedCharacters(false, 5)}),
				Arguments.of("test-document-3.json", new RepeatedCharacters[]{new RepeatedCharacters(false, 3),
				                                                              new RepeatedCharacters(false, 5),
				                                                              new RepeatedCharacters(false, 3),
				                                                              new RepeatedCharacters(true, 3),
				                                                              new RepeatedCharacters(true, 3),
				                                                              new RepeatedCharacters(false, 3)}),
				Arguments.of("title.json", new RepeatedCharacters[]{new RepeatedCharacters(true, 7),
				                                                    new RepeatedCharacters(false, 5)}),
				Arguments.of("whitespace_nonbreaking.json", new RepeatedCharacters[]{new RepeatedCharacters(false, 5),
				                                                                     new RepeatedCharacters(false, 5)})
		                );
	}

	@ParameterizedTest(name = "{index}: ({0}, {1}) => {0}")
	@MethodSource("repeatedCharactersDetectionTestParams")
	void testRepeatedElementsDetection(String filename, RepeatedCharacters[] checks) throws IOException {
		IDocument document = JsonToPdfTree.getDocument("/files/repeatedCharacters/" + filename);
		ITree tree = document.getTree();

		StaticContainers.updateContainers(null);

		SemanticDocumentPostprocessingConsumer documentPostprocessingConsumer =
				new SemanticDocumentPostprocessingConsumer();
		documentPostprocessingConsumer.checkForRepeatedCharacters(tree);

		testRepeated(checks, StaticContainers.getRepeatedCharacters());
	}

	private void testRepeated(RepeatedCharacters[] checks, List<RepeatedCharacters> repeatedCharacters) {
		Assertions.assertEquals(checks.length, repeatedCharacters.size());
		for (int i = 0; i < checks.length; i++) {
			Assertions.assertEquals(checks[i].isNonSpace(), repeatedCharacters.get(i).isNonSpace());
			Assertions.assertEquals(checks[i].getNumberOfElements(), repeatedCharacters.get(i).getNumberOfElements());
		}
	}
}
