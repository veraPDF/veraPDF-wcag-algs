package logiusAlgorithms.PDTree;

public class PDTextChunk extends PDNode implements Comparable<PDTextChunk> {
    private final static double EPS = 1e-7; // TODO: place in settings.json
    public final static String PD_TYPE = "PDTextChunk";

    private String text;
    private String fontName;
    private double fontSize;
    private double[] fontColor;
    private int fontWeight;
    private double italicAngle;
    private double[] boundingBox;
    private double baseLine;
    private int pageNumber;


    public PDTextChunk(String text, String fontName, double fontSize, int fontWeight, int italicAngle,
                       double[] fontColor, double[] boundingBox, double baseLine, int pageNumber) {
        super(PD_TYPE);
        this.text = text;
        this.fontName = fontName;
        this.fontSize = fontSize;
        this.fontColor = fontColor.clone();
        this.fontWeight = fontWeight;
        this.italicAngle = italicAngle;
        this.boundingBox = boundingBox.clone();
        this.baseLine = baseLine;
        this.pageNumber = pageNumber;
    }

    public String getText() { return text; }
    public String getFontName() { return fontName; }
    public double getFontSize() { return fontSize; }
    public double[] getFontColor() { return fontColor; }
    public int getFontWeight() { return fontWeight; }
    public double getItalicAngle() { return italicAngle; }
    public double[] getBoundingBox() { return boundingBox; }
    public double getBaseLine() { return baseLine; }
    public int getPageNumber() { return pageNumber; }

    public double getLeftX() { return boundingBox[0]; }
    public double getRightX() { return boundingBox[2]; }
    public double getBottomY() { return boundingBox[1]; }
    public double getTopY() { return boundingBox[3]; }

    @Override
    public int compareTo(PDTextChunk o) {
        double baseLinesDifference = Math.abs(baseLine - o.getBaseLine());
        if (baseLinesDifference > EPS)
            return Double.compare(baseLine, o.getBaseLine());

        return Double.compare(boundingBox[0], o.getBoundingBox()[0]);
    }
}
