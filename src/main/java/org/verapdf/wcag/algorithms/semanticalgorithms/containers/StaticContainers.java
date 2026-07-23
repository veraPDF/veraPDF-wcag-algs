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
package org.verapdf.wcag.algorithms.semanticalgorithms.containers;

import org.verapdf.wcag.algorithms.entities.IDocument;
import org.verapdf.wcag.algorithms.entities.RepeatedCharacters;
import org.verapdf.wcag.algorithms.entities.content.LinesCollection;
import org.verapdf.wcag.algorithms.entities.lists.PDFList;
import org.verapdf.wcag.algorithms.entities.maps.AccumulatedNodeMapper;
import org.verapdf.wcag.algorithms.entities.maps.ObjectKeyMapper;
import org.verapdf.wcag.algorithms.entities.tables.TableBordersCollection;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.IdMapper;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.ImagesUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.TextChunkUtils;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.WCAGValidationInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StaticContainers {

	private static final Logger LOGGER = Logger.getLogger(StaticContainers.class.getCanonicalName());

	private static final ThreadLocal<IDocument> document = new ThreadLocal<>();

	private static final ThreadLocal<String> fileName = new ThreadLocal<>();

	private static final ThreadLocal<String> password = new ThreadLocal<>();

	private static final ThreadLocal<WCAGValidationInfo> wcagValidationInfo = new ThreadLocal<>();
	
	private static final ThreadLocal<Boolean> isHuman = new ThreadLocal<>();

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

	private static final ThreadLocal<Boolean> keepLineBreaks = new ThreadLocal<>();

	private static final ThreadLocal<Boolean> isDataLoader = new ThreadLocal<>();
	private static final ThreadLocal<ImagesUtils> imagesUtils = new ThreadLocal<>();
	private static final ThreadLocal<Boolean> isImagesUtilsFailedToCreate = new ThreadLocal<>();

	private static final ThreadLocal<Boolean> isIgnoreCharactersWithoutUnicode = new ThreadLocal<>();

    private static final ThreadLocal<Double> textLineSpaceRatio = new ThreadLocal<>();

	static {
		StaticContainers.wcagValidationInfo.set(new WCAGValidationInfo());
        StaticContainers.setIsDataLoader(false);
        StaticContainers.textLineSpaceRatio.set(TextChunkUtils.TEXT_LINE_SPACE_RATIO);
	}

	public static void updateContainers(IDocument document) {
		updateContainers(document, null);
	}

	public static void updateContainers(IDocument document, String fileName) {
		StaticContainers.document.set(document);
		StaticContainers.fileName.set(fileName);
		StaticContainers.password.set("");
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
		StaticContainers.isIgnoreCharactersWithoutUnicode.set(true);
		StaticContainers.keepLineBreaks.set(true);
		if (StaticContainers.imagesUtils.get() != null) {
			try {
				StaticContainers.imagesUtils.get().close();
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, "Exception during image utils closing");
			}
			StaticContainers.imagesUtils.remove();
		}
		StaticContainers.isImagesUtilsFailedToCreate.set(false);
		StaticContainers.isDataLoader.set(false);
        StaticContainers.textLineSpaceRatio.set(TextChunkUtils.TEXT_LINE_SPACE_RATIO);
		if (StaticContainers.isHuman() == null) {
			StaticContainers.setIsHuman(true);
		}
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

	public static String getFileName() {
		return fileName.get();
	}

	public static void setFileName(String fileName) {
		StaticContainers.fileName.set(fileName);
	}

	public static String getPassword() {
		return password.get();
	}

	public static void setPassword(String password) {
		StaticContainers.password.set(password);
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

	public static Boolean isHuman() {
		return isHuman.get();
	}

	public static void setIsHuman(Boolean isHuman) {
		StaticContainers.isHuman.set(isHuman);
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

	public static void setKeepLineBreaks(boolean keepLineBreaks) {
		StaticContainers.keepLineBreaks.set(keepLineBreaks);
	}

	public static boolean isKeepLineBreaks() {
		return keepLineBreaks.get();
	}

	public static void setIsDataLoader(boolean isDataLoader) {
		StaticContainers.isDataLoader.set(isDataLoader);
	}

	public static boolean isDataLoader() {
		return isDataLoader.get();
	}

	public static ImagesUtils getImagesUtils() {
		try {
			if (imagesUtils.get() == null && !Boolean.TRUE.equals(isImagesUtilsFailedToCreate.get())) {
				imagesUtils.set(new ImagesUtils(true));
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Failed to initialize ImagesUtils for PDF '" + StaticContainers.getFileName() + "'", e);
			isImagesUtilsFailedToCreate.set(true);
		}
		return imagesUtils.get();
	}

	public static void setImagesUtils(ImagesUtils imagesUtils) {
		StaticContainers.imagesUtils.set(imagesUtils);
	}

	public static void closeImagesUtils() {
		try {
			if (imagesUtils.get() != null) {
				imagesUtils.get().close();
				imagesUtils.remove();
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error closing images utils: " + e.getMessage());
		}
	}

	public static Boolean getIsIgnoreCharactersWithoutUnicode() {
		return isIgnoreCharactersWithoutUnicode.get();
	}

	public static void setIsIgnoreCharactersWithoutUnicode(Boolean isIgnoreCharactersWithoutUnicode) {
		StaticContainers.isIgnoreCharactersWithoutUnicode.set(isIgnoreCharactersWithoutUnicode);
	}

    public static Double getTextLineSpaceRatio() {
        return textLineSpaceRatio.get();
    }

    public static void setTextLineSpaceRatio(Double textLineSpaceRatio) {
        StaticContainers.textLineSpaceRatio.set(textLineSpaceRatio);
    }
}
