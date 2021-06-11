package org.verapdf.wcag.algorithms.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.verapdf.wcag.algorithms.entities.content.ImageChunk;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.maps.SemanticTypeMapper;

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
			node = new SemanticSpan(new TextChunk(new BoundingBox(jsonNode.getPageNumber(), jsonNode.getBoundingBox()),
			                                      jsonNode.getValue(), jsonNode.getFontName(), jsonNode.getFontSize(),
			                                      jsonNode.getFontWeight(), jsonNode.getItalicAngle(), jsonNode.getBaseLine(),
			                                      jsonNode.getColor()));
		} else if ("ImageChunk".equals(pdfType)) {
			node = new SemanticImageNode(new ImageChunk(new BoundingBox(jsonNode.getPageNumber(), jsonNode.getBoundingBox())));
		} else {
			node = new UnexpectedSemanticNode(SemanticTypeMapper.getSemanticType(pdfType));
		}

		if (jsonNode.getChildren() != null) {
			for (JsonNode child : jsonNode.getChildren()) {
				node.getChildren().add(getPdfNode(child));
			}
		}

		return node;
	}
}
