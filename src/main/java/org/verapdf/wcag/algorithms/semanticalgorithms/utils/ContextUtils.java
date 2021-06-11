package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.entities.geometry.MultiBoundingBox;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class ContextUtils {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final DecimalFormat FORMATTER;

	static {
		FORMATTER = new DecimalFormat("#0.000000");
		DecimalFormatSymbols decFormSymbols = FORMATTER.getDecimalFormatSymbols();
		decFormSymbols.setDecimalSeparator('.');
		FORMATTER.setDecimalFormatSymbols(decFormSymbols);
		SimpleModule module = new SimpleModule("BoundingBoxSerializer", new Version(2, 1, 3, null, null, null));
		BoundingBoxSerializer bboxSerializer = new BoundingBoxSerializer(BoundingBox.class);
		module.addSerializer(BoundingBox.class, bboxSerializer);

		MultiBoundingBoxSerializer multiBBoxSerializer = new MultiBoundingBoxSerializer(MultiBoundingBox.class);
		module.addSerializer(MultiBoundingBox.class, multiBBoxSerializer);
		objectMapper.registerModule(module);
    }

	public static String getContext(BoundingBox bbox) {
		try {
			return objectMapper.writeValueAsString(bbox);
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	static class BoundingBoxSerializer extends StdSerializer<BoundingBox> {

		protected BoundingBoxSerializer(Class<BoundingBox> t) {
			    super(t);
		    }

		public void serialize(BoundingBox bbox, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
			    throws IOException {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeFieldName("bbox");
			jsonGenerator.writeStartArray();
			jsonGenerator.writeStartObject();
			jsonGenerator.writeNumberField("p", bbox.getPageNumber());
			jsonGenerator.writeFieldName("rect");
			jsonGenerator.writeStartArray();
			jsonGenerator.writeNumber(FORMATTER.format(bbox.getLeftX()));
			jsonGenerator.writeNumber(FORMATTER.format(bbox.getBottomY()));
			jsonGenerator.writeNumber(FORMATTER.format(bbox.getRightX()));
			jsonGenerator.writeNumber(FORMATTER.format(bbox.getTopY()));
			jsonGenerator.writeEndArray();
			jsonGenerator.writeEndObject();
			jsonGenerator.writeEndArray();
			jsonGenerator.writeEndObject();
		}
	}

	static class MultiBoundingBoxSerializer extends StdSerializer<MultiBoundingBox> {

		protected MultiBoundingBoxSerializer(Class<MultiBoundingBox> t) {
			super(t);
		}

		public void serialize(MultiBoundingBox multiBBox, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
				throws IOException {

			jsonGenerator.writeStartObject();
			jsonGenerator.writeFieldName("bbox");
			jsonGenerator.writeStartArray();
			for (BoundingBox bbox : multiBBox.getBoundingBoxes()) {
				jsonGenerator.writeStartObject();
				jsonGenerator.writeNumberField("p", bbox.getPageNumber());
				jsonGenerator.writeFieldName("rect");
				jsonGenerator.writeStartArray();
				jsonGenerator.writeNumber(FORMATTER.format(bbox.getLeftX()));
				jsonGenerator.writeNumber(FORMATTER.format(bbox.getBottomY()));
				jsonGenerator.writeNumber(FORMATTER.format(bbox.getRightX()));
				jsonGenerator.writeNumber(FORMATTER.format(bbox.getTopY()));
				jsonGenerator.writeEndArray();
				jsonGenerator.writeEndObject();
			}
			jsonGenerator.writeEndArray();
			jsonGenerator.writeEndObject();
		}
	}

}
