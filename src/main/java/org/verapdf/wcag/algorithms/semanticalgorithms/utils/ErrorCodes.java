package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import org.verapdf.wcag.algorithms.entities.INode;

import java.util.Arrays;

public class ErrorCodes {

	//TOC/TOCI error codes
	public static final int ERROR_CODE_1000 = 1000;//TOCI doesn't have a text
	public static final int ERROR_CODE_1001 = 1001;//TOCI doesn't have a destination
	public static final int ERROR_CODE_1002 = 1002;//TOCI has a wrong page label
	public static final int ERROR_CODE_1003 = 1003;//TOCI has a bad right alignment
	public static final int ERROR_CODE_1004 = 1004;//TOCI has a bad left alignment
	public static final int ERROR_CODE_1005 = 1005;//TOCI text not found on a destination page
	public static final int ERROR_CODE_1006 = 1006;//This TOC and neighbor TOC(s) should be tagged as one TOC
	public static final int ERROR_CODE_1007 = 1007;//TOCI text not found on the document
	public static final int ERROR_CODE_1008 = 1008;//TOCI text not found on a destination page.
	public static final int ERROR_CODE_1009 = 1009;//TOCI doesn't have a destination.
	public static final int ERROR_CODE_1010 = 1010;//TOCI has a wrong page label.
	public static final int ERROR_CODE_1011 = 1011;//TOCI has a wrong numbering.

	//Table error codes
	public static final int ERROR_CODE_1100 = 1100;//This cell is below than some cells in the next row
	public static final int ERROR_CODE_1101 = 1101;//This cell is above than some cells in the previous row
	public static final int ERROR_CODE_1102 = 1102;//This cell is to the right of some cells in the next column
	public static final int ERROR_CODE_1103 = 1103;//This cell is to the left of some cells in the previous column
	public static final int ERROR_CODE_1104 = 1104;//The number of rows of this table does not match the number of rows of the visual representation of this table
	public static final int ERROR_CODE_1105 = 1105;//The number of columns of this table does not match the number of columns of the visual representation of this table
	public static final int ERROR_CODE_1106 = 1106;//The row span of this table cell does not match the row span of the visual representation of this table cell
	public static final int ERROR_CODE_1107 = 1107;//The column span of this table cell does not match the column span of the visual representation of this table cell

	//List error codes
	public static final int ERROR_CODE_1200 = 1200;//This list and neighbor list(s) should be tagged as one list
	public static final int ERROR_CODE_1201 = 1201;//Only one List inside list

	public static void addErrorCodeWithArguments(INode node, int errorCode, Object ... arguments) {
		if (!node.getErrorCodes().contains(errorCode)) {
			node.getErrorCodes().add(errorCode);
			node.getErrorArguments().add(Arrays.asList(arguments.clone()));
		}
	}

	public static void removeErrorCodeWithArgumentsAfterIndex(INode node, int index) {
		for (int i = node.getErrorCodes().size() - 1; i >= index; i--) {
			node.getErrorCodes().remove(i);
			node.getErrorArguments().remove(i);
		}
	}

}
