package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

public class ErrorCodes {

	//TOC/TOCI error codes
	public static final int ERROR_CODE_1000 = 1000;//TOCI doesn't have a text
	public static final int ERROR_CODE_1001 = 1001;//TOCI doesn't have a destination
	public static final int ERROR_CODE_1002 = 1002;//TOCI has a wrong page label
	public static final int ERROR_CODE_1003 = 1003;//TOCI has a bad right alignment
	public static final int ERROR_CODE_1004 = 1004;//TOCI has a bad left alignment
	public static final int ERROR_CODE_1005 = 1005;//TOCI text not found on a destination page
	public static final int ERROR_CODE_1006 = 1006;//This TOC and neighbor TOC(s) should be tagged as one TOC

	//Table error codes
	public static final int ERROR_CODE_1100 = 1100;//This cell is below than some cells in the next row
	public static final int ERROR_CODE_1101 = 1101;//This cell is above than some cells in the previous row
	public static final int ERROR_CODE_1102 = 1102;//This cell is to the right of some cells in the next column
	public static final int ERROR_CODE_1103 = 1103;//This cell is to the left of some cells in the previous column

	//List error codes
	public static final int ERROR_CODE_1200 = 1200;//This list and neighbor list(s) should be tagged as one list

}
