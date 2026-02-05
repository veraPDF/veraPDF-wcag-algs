/*
 * This file is part of veraPDF wcag algorithms, a module of the veraPDF project.
 * Copyright (c) 2015-2026, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF wcag algorithms is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF wcag algorithms as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF wcag algorithms as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.wcag.algorithms.entities;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

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
	public JavaType typeFromId(DatabindContext context, String id) {
		Class<?> subType;
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
