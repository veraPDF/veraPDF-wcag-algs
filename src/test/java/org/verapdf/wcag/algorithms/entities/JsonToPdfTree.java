package org.verapdf.wcag.algorithms.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.verapdf.wcag.algorithms.entities.content.IChunk;
import org.verapdf.wcag.algorithms.entities.content.ImageChunk;
import org.verapdf.wcag.algorithms.entities.content.LineChunk;
import org.verapdf.wcag.algorithms.entities.content.LineArtChunk;
import org.verapdf.wcag.algorithms.entities.content.TextChunk;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.maps.SemanticTypeMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class JsonToPdfTree {

	private JsonToPdfTree() {
	}

	public static IDocument getDocument(String jsonFileName) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		InputStream jsonFileInputStream = JsonToPdfTree.class.getResourceAsStream(jsonFileName);
		JsonNode jsonRoot = objectMapper.readValue(new InputStreamReader(jsonFileInputStream, StandardCharsets.UTF_8), JsonNode.class);
		IDocument document = new Document(new SemanticTree(getPdfNode(jsonRoot)));
		if (jsonRoot.getPages() != null) {
			for (JsonNode page : jsonRoot.getPages()) {
				document.getPages().add(getPage(page));
			}
		}
		return document;
	}

	private static TextChunk getTextChunk(JsonNode jsonNode) {
		return new TextChunk(new BoundingBox(jsonNode.getPageNumber(), jsonNode.getBoundingBox()),
				jsonNode.getValue(), jsonNode.getFontName(), jsonNode.getFontSize(),
				jsonNode.getFontWeight(), jsonNode.getItalicAngle(), jsonNode.getBaseLine(),
				jsonNode.getColor(), jsonNode.getFontColorSpace(), jsonNode.getSymbolEnds());
	}

	private static ImageChunk getImageChunk(JsonNode jsonNode) {
		return new ImageChunk(new BoundingBox(jsonNode.getPageNumber(), jsonNode.getBoundingBox()));
	}

	private static LineArtChunk getLineArtChunk(JsonNode jsonNode) {
		return new LineArtChunk(new BoundingBox(jsonNode.getPageNumber(), jsonNode.getBoundingBox()));
	}

	private static LineChunk getLineChunk(JsonNode jsonNode) {
		return new LineChunk(jsonNode.getPageNumber(), jsonNode.getStartX(),
				jsonNode.getStartY(), jsonNode.getEndX(), jsonNode.getEndY(), jsonNode.getWidth());
	}

	private static IPage getPage(JsonNode jsonNode) {
		IPage page = new Page(jsonNode.getPageNumber());
		if (jsonNode.getArtifacts() != null) {
			for (JsonNode artifact : jsonNode.getArtifacts()) {
				page.getArtifacts().add(getArtifact(artifact));
			}
		}
		return page;
	}

	private static IChunk getArtifact(JsonNode jsonNode) {
		if ("TextChunk".equals(jsonNode.getType())) {
			return getTextChunk(jsonNode);
		}
		if ("ImageChunk".equals(jsonNode.getType())) {
			return getImageChunk(jsonNode);
		}
		if ("LineArtChunk".equals(jsonNode.getType())) {
			return getLineArtChunk(jsonNode);
		}
		if ("LineChunk".equals(jsonNode.getType())) {
			return getLineChunk(jsonNode);
		}
		return null;
	}

	private static INode getPdfNode(JsonNode jsonNode) {
		if (jsonNode == null) {
			return null;
		}

		INode node;
		String pdfType = jsonNode.getType();

		if ("TextChunk".equals(pdfType)) {
			node = new SemanticSpan(getTextChunk(jsonNode));
		} else if ("ImageChunk".equals(pdfType)) {
			node = new SemanticImageNode(getImageChunk(jsonNode));
		} else if ("LineArtChunk".equals(pdfType)) {
			node = new SemanticFigure(getLineArtChunk(jsonNode));
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
