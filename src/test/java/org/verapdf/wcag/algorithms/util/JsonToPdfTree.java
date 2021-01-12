package org.verapdf.wcag.algorithms.util;

import com.google.gson.Gson;
import org.verapdf.wcag.algorithms.entity.SemanticNode;
import org.verapdf.wcag.algorithms.entity.SemanticTextChunk;

import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonToPdfTree {

	private JsonToPdfTree() {
	}

	public static SemanticNode getPdfTreeRoot(String jsonFileName) {
		Gson gson = new Gson();
		InputStream jsonFileInputStream = JsonToPdfTree.class.getResourceAsStream(jsonFileName);
		JsonNode jsonRoot = gson.fromJson(new InputStreamReader(jsonFileInputStream), JsonNode.class);
		return getPdfNode(jsonRoot);
	}

	private static SemanticNode getPdfNode(JsonNode jsonNode) {
		if (jsonNode == null) {
			return null;
		}

		SemanticNode node;
		String pdfType = jsonNode.getType();

		if ("PDTextChunk".equals(pdfType)) {
			node = new SemanticTextChunk(jsonNode.getBoundingBox(), jsonNode.getPageNumber(), jsonNode.getValue(),
			                             jsonNode.getFontName(), jsonNode.getFontSize(), jsonNode.getFontWeight(),
			                             jsonNode.getItalicAngle(), jsonNode.getBaseLine(), jsonNode.getColor());
		} else {
			node = new SemanticNode(null);
		}

		if (jsonNode.getChildren() != null) {
			for (JsonNode child : jsonNode.getChildren()) {
				node.getChildren().add(getPdfNode(child));
			}
		}

		return node;
	}
}
