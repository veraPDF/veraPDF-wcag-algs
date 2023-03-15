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
import java.util.LinkedList;
import java.util.List;

public class JsonToPdfTree {

	private JsonToPdfTree() {
	}

	public static IDocument getDocument(String jsonFileName) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		InputStream jsonFileInputStream = JsonToPdfTree.class.getResourceAsStream(jsonFileName);
		JsonNode jsonRoot = objectMapper.readValue(new InputStreamReader(jsonFileInputStream, StandardCharsets.UTF_8), JsonNode.class);
		IDocument document = new Document(new SemanticTree(getPdfNode(jsonRoot)));
		if (jsonRoot.getPages() != null) {
			for (JsonPage page : jsonRoot.getPages()) {
				document.getPages().add(getPage(page));
			}
		}
		return document;
	}

	private static TextChunk getTextChunk(JsonTextChunk jsonNode) {
		return new TextChunk(new BoundingBox(jsonNode.getPageNumber(), jsonNode.getBoundingBox()), jsonNode.getValue(),
		                     jsonNode.getFontName(), jsonNode.getFontSize(), jsonNode.getFontWeight(), jsonNode.getItalicAngle(),
		                     jsonNode.getBaseLine(), jsonNode.getColor(), jsonNode.getSymbolEnds(),
		                     jsonNode.getSlantDegree());
	}

	private static ImageChunk getImageChunk(JsonImageChunk jsonNode) {
		return new ImageChunk(new BoundingBox(jsonNode.getPageNumber(), jsonNode.getBoundingBox()));
	}

	private static LineArtChunk getLineArtChunk(JsonLineArtChunk jsonNode) {
		List<LineChunk> lineChunks = new LinkedList<>();
		if (jsonNode.getLines() != null) {
			for (JsonLineChunk lineChunk : jsonNode.getLines()) {
				lineChunks.add(getLineChunk(lineChunk));
			}
		}
		return new LineArtChunk(new BoundingBox(jsonNode.getPageNumber(), jsonNode.getBoundingBox()), lineChunks);
	}

	private static AnnotationNode getAnnotationNode(JsonAnnotationNode jsonNode) {
		return new AnnotationNode(jsonNode.getAnnotationType(), new BoundingBox(jsonNode.getBoundingBox()),
		                          jsonNode.getDestinationPageNumber(), jsonNode.getDestinationObjectKeyNumber());
	}

	private static LineChunk getLineChunk(JsonLineChunk jsonNode) {
		return new LineChunk(jsonNode.getPageNumber(), jsonNode.getStartX(), jsonNode.getStartY(),
		                     jsonNode.getEndX(), jsonNode.getEndY(), jsonNode.getWidth());
	}

	private static IPage getPage(JsonPage jsonPage) {
		IPage page = new Page(jsonPage.getPageNumber(), jsonPage.getPageLabel());
		if (jsonPage.getArtifacts() != null) {
			for (JsonNode artifact : jsonPage.getArtifacts()) {
				page.getArtifacts().add(getArtifact(artifact));
			}
		}
		return page;
	}

	private static IChunk getArtifact(JsonNode jsonNode) {
		if (jsonNode instanceof JsonTextChunk) {
			return getTextChunk((JsonTextChunk) jsonNode);
		}
		if (jsonNode instanceof JsonImageChunk) {
			return getImageChunk((JsonImageChunk) jsonNode);
		}
		if (jsonNode instanceof JsonLineArtChunk) {
			return getLineArtChunk((JsonLineArtChunk) jsonNode);
		}
		if (jsonNode instanceof JsonLineChunk) {
			return getLineChunk((JsonLineChunk) jsonNode);
		}
		return null;
	}

	private static INode getPdfNode(JsonNode jsonNode) {
		if (jsonNode == null) {
			return null;
		}

		INode node;
		if (jsonNode instanceof JsonTextChunk) {
			node = new SemanticSpan(getTextChunk((JsonTextChunk) jsonNode));
		} else if (jsonNode instanceof JsonImageChunk) {
			node = new SemanticImageNode(getImageChunk((JsonImageChunk) jsonNode));
		} else if (jsonNode instanceof JsonLineArtChunk) {
			node = new SemanticFigure(getLineArtChunk((JsonLineArtChunk) jsonNode));
		} else if (jsonNode instanceof JsonAnnotationNode) {
			node = getAnnotationNode((JsonAnnotationNode) jsonNode);
		} else {
			node = new SemanticNode(SemanticTypeMapper.getSemanticType(jsonNode.getType()), jsonNode.getObjectKeyNumber());
		}

		JsonAttributes attributes = jsonNode.getAttributes();
		if (attributes != null) {
			node.setAttributesDictionary(new AttributesDictionary(attributes.getRowSpan(), attributes.getColSpan()));
		}

		if (jsonNode.getChildren() != null) {
			for (JsonNode child : jsonNode.getChildren()) {
				node.getChildren().add(getPdfNode(child));
			}
		}

		return node;
	}
}
