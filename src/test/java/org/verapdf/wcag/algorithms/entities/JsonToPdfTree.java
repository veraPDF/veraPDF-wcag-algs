package org.verapdf.wcag.algorithms.entities;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonToPdfTree {

	private JsonToPdfTree() {
	}

	public static INode getPdfTreeRoot(String jsonFileName) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		InputStream jsonFileInputStream = JsonToPdfTree.class.getResourceAsStream(jsonFileName);
		JsonNode jsonRoot = objectMapper.readValue(new InputStreamReader(jsonFileInputStream), JsonNode.class);
		return getPdfNode(jsonRoot);
	}

	private static INode getPdfNode(JsonNode jsonNode) {
		if (jsonNode == null) {
			return null;
		}

		INode node;
		String pdfType = jsonNode.getType();

		if ("PDTextChunk".equals(pdfType) || "TextChunk".equals(pdfType)) {
			node = new SemanticTextChunk(jsonNode.getPageNumber(), jsonNode.getBoundingBox(), jsonNode.getValue(),
			                             jsonNode.getFontName(), jsonNode.getFontSize(), jsonNode.getFontWeight(),
			                             jsonNode.getItalicAngle(), jsonNode.getBaseLine(), jsonNode.getColor());
		} else {
			node = new UnexpectedSemanticNode();
		}

		if (jsonNode.getChildren() != null) {
			for (JsonNode child : jsonNode.getChildren()) {
				node.getChildren().add(getPdfNode(child));
			}
		}

		return node;
	}
}
