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

import java.util.HashMap;
import java.util.Map;

public class WCAGProgress {
	private final WCAGProgressStatus status;
	private final Double percent;

	public WCAGProgress(WCAGProgressStatus status, Double percent) {
		this.status = status;
		this.percent = percent;
	}

	public WCAGProgress(WCAGProgressStatus status) {
		this(status, null);
	}

	public WCAGProgressStatus getStatus() { return status; }

	public Double getPercent() { return percent; }

	public Map<String, Object> getStats() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("status", status.name());
		if (percent != null) {
			properties.put("percent", percent.intValue());
		}
		return properties;
	}

	public String getMessage() {
		if (percent == null) {
			return status.getValue();
		}
		return status.getValue() + " " + percent.intValue() + "%"; 
	}
}
