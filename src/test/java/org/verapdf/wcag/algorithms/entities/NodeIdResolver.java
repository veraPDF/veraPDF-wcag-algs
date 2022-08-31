package org.verapdf.wcag.algorithms.entities;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

import java.io.IOException;

public class NodeIdResolver extends TypeIdResolverBase {

	private JavaType superType;

	@Override
	public void init(JavaType baseType) {
		superType = baseType;
	}

	@Override
	public JsonTypeInfo.Id getMechanism() {
		return JsonTypeInfo.Id.NAME;
	}

	@Override
	public String idFromValue(Object o) {
		return idFromValueAndType(o, o.getClass());
	}

	@Override
	public String idFromValueAndType(Object o, Class<?> aClass) {
		return null;
	}

	@Override
	public JavaType typeFromId(DatabindContext context, String id) throws IOException {
		Class<?> subType = null;
		switch (id) {
			case "TextChunk":
				subType = JsonTextChunk.class;
				break;
			case "LineChunk":
				subType = JsonLineChunk.class;
				break;
			case "ImageChunk":
				subType = JsonImageChunk.class;
				break;
			case "LineArtChunk":
				subType = JsonLineArtChunk.class;
				break;
			case "AnnotationNode":
				subType = JsonAnnotationNode.class;
				break;
			default:
				subType = JsonNode.class;
				break;
		}

		return context.constructSpecializedType(superType, subType);
	}
}
