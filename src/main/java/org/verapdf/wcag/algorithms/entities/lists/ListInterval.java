package org.verapdf.wcag.algorithms.entities.lists;

public class ListInterval {
	public final int start;
	public final int end;

	public ListInterval(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return start;
	}

	@Override
	public int hashCode() {
		return 31 * end + start;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ListInterval)) {
			return false;
		}
		ListInterval that = (ListInterval) o;
		return this.start == that.start && this.end == that.end;
	}

	public boolean contains(ListInterval second) {
		return this.start <= second.start && this.end >= second.end;
	}
}
