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
package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;

public class TextChunkUtilsTests {

    @BeforeEach
    public void setUp() {
        StaticContainers.setKeepLineBreaks(false);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            // hyphenation hyphens are removed
            "hyphen-    | ation      | hyphenation",
            "e-         | mail       | email",
            "Sep-       | tember     | September",
            "INTER-     | NATIONAL   | INTERNATIONAL",
            "przyczynowo- | -skutkowych | przyczynowo-skutkowych",
            "insuffi -  | cienza     | insuffi cienza",
            // hyphens that are part of the text are kept
            "the IT-    | department | the IT-department",
            "GDPR-      | fines      | GDPR-fines",
            "Anglo-     | Saxon      | Anglo-Saxon",
            "1990-      | 2000       | 1990-2000",
            "COVID-     | 19         | COVID-19",
            "pre-       | (draft)    | pre-(draft)",
            "Subsidiary risk - | Packing | Subsidiary risk - Packing",
            "risk -     | -          | risk - -",
            // a soft hyphen is always removed, an em dash is always kept
            "hyphen\u00AD | ation    | hyphenation",
            "IT\u00AD   | department | ITdepartment",
            "Front\u00AD\u00AD | zahnbereich | Frontzahnbereich",
            "word\u2014 | next       | word\u2014next",
            // other line ends get a space
            "word       | next       | word next",
            "word,      | next       | 'word, next'",
    })
    public void formatLineEndTest(String line, String nextLine, String expected) {
        StringBuilder stringBuilder = new StringBuilder(line);
        TextChunkUtils.formatLineEnd(stringBuilder, nextLine);
        stringBuilder.append(nextLine);
        Assertions.assertEquals(expected, stringBuilder.toString());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "the IT-    | the ITdepartment",
            "word\u2014 | word\u2014department",
            "word       | word department",
    })
    public void formatLineEndWithoutNextLineTest(String line, String expected) {
        StringBuilder stringBuilder = new StringBuilder(line);
        TextChunkUtils.formatLineEnd(stringBuilder);
        stringBuilder.append("department");
        Assertions.assertEquals(expected, stringBuilder.toString());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "''         | ' next'",
            "-          | - next",
    })
    public void formatLineEndShortLineTest(String line, String expected) {
        StringBuilder stringBuilder = new StringBuilder(line);
        TextChunkUtils.formatLineEnd(stringBuilder, "next");
        stringBuilder.append("next");
        Assertions.assertEquals(expected, stringBuilder.toString());
    }

    @Test
    public void formatLineEndKeepLineBreaksTest() {
        StaticContainers.setKeepLineBreaks(true);
        StringBuilder stringBuilder = new StringBuilder("the IT-");
        TextChunkUtils.formatLineEnd(stringBuilder, "department");
        Assertions.assertEquals("the IT-\n", stringBuilder.toString());
    }
}
