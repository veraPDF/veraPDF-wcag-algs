/**
 * This file is part of veraPDF Validation, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Validation is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Validation as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Validation as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.wcag.algorithms.semanticalgorithms.containers;

import org.verapdf.wcag.algorithms.entities.IDocument;
import org.verapdf.wcag.algorithms.entities.RepeatedCharacters;
import org.verapdf.wcag.algorithms.entities.content.LinesCollection;
import org.verapdf.wcag.algorithms.entities.lists.PDFList;
import org.verapdf.wcag.algorithms.entities.maps.AccumulatedNodeMapper;
import org.verapdf.wcag.algorithms.entities.maps.ObjectKeyMapper;
import org.verapdf.wcag.algorithms.entities.tables.TableBordersCollection;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.IdMapper;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.WCAGValidationInfo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class StaticContainers {

	private static final ThreadLocal<IDocument> document = new ThreadLocal<>();

	private static final ThreadLocal<WCAGValidationInfo> wcagValidationInfo = new ThreadLocal<>();

	private static final ThreadLocal<Long> structElementsNumber = new ThreadLocal<>();

	private static final ThreadLocal<Long> textChunksNumber = new ThreadLocal<>();

	private static final ThreadLocal<AccumulatedNodeMapper> accumulatedNodeMapper = new ThreadLocal<>();

	private static final ThreadLocal<ObjectKeyMapper> objectKeyMapper = new ThreadLocal<>();

	private static final ThreadLocal<TableBordersCollection> tableBordersCollection = new ThreadLocal<>();

	private static final ThreadLocal<List<PDFList>> listsCollection = new ThreadLocal<>();

	private static final ThreadLocal<LinesCollection> linesCollection = new ThreadLocal<>();

	private static final ThreadLocal<List<RepeatedCharacters>> repeatedCharacters = new ThreadLocal<>();

	private static final ThreadLocal<IdMapper> idMapper = new ThreadLocal<>();

	private static final ThreadLocal<Long> groupCounter = new ThreadLocal<>();

	static {
		StaticContainers.wcagValidationInfo.set(new WCAGValidationInfo());
	}

	public static void updateContainers(IDocument document) {
		StaticContainers.document.set(document);
		StaticContainers.accumulatedNodeMapper.set(new AccumulatedNodeMapper());
		StaticContainers.objectKeyMapper.set(new ObjectKeyMapper());
		StaticContainers.tableBordersCollection.set(new TableBordersCollection());
		StaticContainers.linesCollection.set(new LinesCollection());
		StaticContainers.repeatedCharacters.set(new ArrayList<>());
		StaticContainers.listsCollection.set(new LinkedList<>());
		StaticContainers.idMapper.set(new IdMapper());
		StaticContainers.groupCounter.set(0L);
		StaticContainers.structElementsNumber.set(0L);
		StaticContainers.textChunksNumber.set(0L);
		if (StaticContainers.getWCAGValidationInfo() == null) {
			StaticContainers.setWCAGValidationInfo(new WCAGValidationInfo());
		}
	}

	public static IDocument getDocument() {
		return document.get();
	}

	public static void setDocument(IDocument document) {
		StaticContainers.document.set(document);
	}

	public static WCAGValidationInfo getWCAGValidationInfo() {
		return wcagValidationInfo.get();
	}

	public static void setWCAGValidationInfo(WCAGValidationInfo wcagValidationInfo) {
		StaticContainers.wcagValidationInfo.set(wcagValidationInfo);
	}

	public static AccumulatedNodeMapper getAccumulatedNodeMapper() {
		return accumulatedNodeMapper.get();
	}

	public static void setAccumulatedNodeMapper(AccumulatedNodeMapper accumulatedNodeMapper) {
		StaticContainers.accumulatedNodeMapper.set(accumulatedNodeMapper);
	}

	public static ObjectKeyMapper getObjectKeyMapper() {
		return objectKeyMapper.get();
	}

	public static void setObjectKeyMapper(ObjectKeyMapper objectKeyMapper) {
		StaticContainers.objectKeyMapper.set(objectKeyMapper);
	}

	public static TableBordersCollection getTableBordersCollection() {
		return tableBordersCollection.get();
	}

	public static void setTableBordersCollection(TableBordersCollection tableBordersCollection) {
		StaticContainers.tableBordersCollection.set(tableBordersCollection);
	}

	public static LinesCollection getLinesCollection() {
		return linesCollection.get();
	}

	public static void setLinesCollection(LinesCollection linesCollection) {
		StaticContainers.linesCollection.set(linesCollection);
	}

	public static List<RepeatedCharacters> getRepeatedCharacters() {
		return repeatedCharacters.get();
	}

	public static void setRepeatedCharacters(List<RepeatedCharacters> repeatedCharacters) {
		StaticContainers.repeatedCharacters.set(repeatedCharacters);
	}

	public static List<PDFList> getListsCollection() {
		return listsCollection.get();
	}

	public static void setListsCollection(List<PDFList> listsCollection) {
		StaticContainers.listsCollection.set(listsCollection);
	}

	public static IdMapper getIdMapper() {
		return idMapper.get();
	}

	public static void setIdMapper(IdMapper idMapper) {
		StaticContainers.idMapper.set(idMapper);
	}

	public static Long getGroupCounter() {
		return groupCounter.get();
	}

	public static Long getNextID() {
		Long id = groupCounter.get();
		groupCounter.set(id + 1);
		return id;
	}

	public static Long getStructElementsNumber() {
		return structElementsNumber.get();
	}

	public static void setStructElementsNumber(Long structElementsNumber) {
		StaticContainers.structElementsNumber.set(structElementsNumber);
	}

	public static Long getTextChunksNumber() {
		return textChunksNumber.get();
	}

	public static void setTextChunksNumber(Long textChunksNumber) {
		StaticContainers.textChunksNumber.set(textChunksNumber);
	}
}
