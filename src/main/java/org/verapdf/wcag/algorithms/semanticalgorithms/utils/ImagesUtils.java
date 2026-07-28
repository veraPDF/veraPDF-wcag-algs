package org.verapdf.wcag.algorithms.semanticalgorithms.utils;

import com.github.jaiimageio.jpeg2000.impl.J2KImageReaderSpi;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.jbig2.JBIG2ImageReaderSpi;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.verapdf.wcag.algorithms.entities.ObjectKey;
import org.verapdf.wcag.algorithms.entities.geometry.BoundingBox;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;

import javax.imageio.spi.IIORegistry;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImagesUtils implements Closeable {
    private static final Logger LOGGER = Logger.getLogger(ImagesUtils.class.getCanonicalName());

    private PDDocument document;
    private final Map<Integer, Float> renderDpiForPages = new HashMap<>();
    private final Map<Integer, BufferedImage> renderedPages = new HashMap<>();
    public static final int RENDER_DPI = 288;
    public static final int PDF_DPI = 72;

    private boolean isLoad = false;

    public ImagesUtils() throws IOException {
        this(true);
    }

    public ImagesUtils(boolean isLoad) throws IOException {
        IIORegistry registry = IIORegistry.getDefaultInstance();
        registry.registerServiceProvider(new J2KImageReaderSpi());
        registry.registerServiceProvider(new JBIG2ImageReaderSpi());
        if (isLoad) {
            loadDocument();
        }
    }
    
    public void loadDocument() throws IOException {
        if (!isLoad && StaticContainers.getFileName() != null) {
            this.document = Loader.loadPDF(new RandomAccessReadBuffer(new FileInputStream(StaticContainers.getFileName())), StaticContainers.getPassword());
            isLoad = true;
        }
    }

    public BufferedImage getRenderPage(int pageNumber, Double dpi, boolean enableAntialias) {
        BufferedImage renderedPage = renderedPages.get(pageNumber);
        if (renderedPage == null) {
            try {
                clearRenderedPages();
                renderedPage = renderPage(document, pageNumber, dpi, enableAntialias);
                renderedPages.put(pageNumber, renderedPage);
            } catch (IOException | IllegalArgumentException e) {
                e.printStackTrace();
                LOGGER.warning(e.getMessage());
            }
        }
        return renderedPage;
    }
    
    public void clearRenderedPages() {
        renderedPages.clear();
        renderDpiForPages.clear();
    }

    public BufferedImage getRenderPageForContrast(int pageNumber) {
        return getRenderPage(pageNumber, null, false);
    }

    public BufferedImage getRenderPage(int pageNumber) {
        return getRenderPage(pageNumber, null, true);
    }

    public BufferedImage getPageSubImage(BufferedImage renderedPage, BoundingBox bBox) {
        if (renderedPage == null) {
            return null;
        }
        int pageNumber = bBox.getPageNumber();
        double dpiScaling = getDpiScalingForPage(bBox.getPageNumber());
        int renderedPageWidth = renderedPage.getRaster().getWidth();
        int renderedPageHeight = renderedPage.getRaster().getHeight();
        BoundingBox pageBBox = new BoundingBox(pageNumber,0, 0, renderedPageWidth, renderedPageHeight);
        BoundingBox scaledBBox = new BoundingBox(pageNumber, bBox.getLeftX() * dpiScaling,
                bBox.getBottomY() * dpiScaling,
                bBox.getRightX() * dpiScaling,
                bBox.getTopY() * dpiScaling);
        boolean isOverlappingBox = scaledBBox.overlaps(pageBBox);
        if (isOverlappingBox) {
            scaledBBox = scaledBBox.cross(pageBBox);
        } else {
            return null;
        }

        int x = (int) (Math.floor(scaledBBox.getLeftX()));
        int y = (int) (Math.ceil(scaledBBox.getTopY()));
        int width = getIntegerBBoxValueForProcessing(scaledBBox.getWidth(), 1);
        int height = getIntegerBBoxValueForProcessing(scaledBBox.getHeight(), 1);
        return renderedPage.getSubimage(x, renderedPage.getHeight() - y, width,  height);
    }

    public BufferedImage getPageSubImage(BoundingBox bBox) {
        return getPageSubImage(bBox, null);
    }

    public BufferedImage getPageSubImage(BoundingBox bBox, Double dpi) {
        BufferedImage renderedPage = getRenderPage(bBox.getPageNumber(), dpi, true);
        return getPageSubImage(renderedPage, bBox);
    }

    private BufferedImage renderPage(PDDocument document, Integer pageNumber, Double dpi, boolean enableAntialias) throws IOException {
        RenderingHints renderingHints = new RenderingHints(null);
        renderingHints.put(RenderingHints.KEY_ANTIALIASING, enableAntialias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        pdfRenderer.setRenderingHints(renderingHints);
        float usedDPI = dpi != null ? dpi.floatValue() : StaticContainers.getImageResolution();
        renderDpiForPages.put(pageNumber, usedDPI / PDF_DPI);
        return pdfRenderer.renderImageWithDPI(pageNumber, usedDPI, ImageType.RGB);
    }
    
    private int getIntegerBBoxValueForProcessing(double initialValue, double dpiScaling) {
        int result = (int) (Math.round(initialValue * dpiScaling));
        if (result <= 0) {
            result = 1;
            LOGGER.warning("The resulting target buffered image width is <= 0. Fall back to " + result);
        }
        return result;
    }

    public double getDpiScalingForPage(int pageNumber) {
        return renderDpiForPages.get(pageNumber);
    }
    
    public BufferedImage getXObjectImage(int pageNumber, ObjectKey objectKey) {
        if (document == null) {
            return null;
        }
        PDPage page = document.getPages().get(pageNumber);
        try {
            PDImageXObject xImage = (PDImageXObject)findXImageByObjectKey(page, objectKey);
            if (xImage != null) {
                return xImage.getImage();
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Exception during xImage processing");
        }
        return null;
    }

    private static PDXObject findXImageByObjectKey(PDPage page, ObjectKey objectKey) throws IOException {
        Stack<COSBase> stack = new Stack<>();
        stack.add(page.getCOSObject());
        boolean isPageLevel = true;
        while (!stack.isEmpty()) {
            COSBase currentObject = stack.pop();
            if (!(currentObject instanceof COSDictionary)) {
                continue;
            }
            COSBase resources = ((COSDictionary)currentObject).getDictionaryObject(COSName.RESOURCES);
            if (!(resources instanceof COSDictionary)) {
                continue;
            }
            COSBase xObjectDictionary = ((COSDictionary)resources).getDictionaryObject(COSName.XOBJECT);
            if (!(xObjectDictionary instanceof COSDictionary)) {
                continue;

            }
            for (COSBase xObjectBase : ((COSDictionary) xObjectDictionary).getValues()) {
                if (xObjectBase instanceof COSObject) {
                    COSObject xObject = (COSObject)xObjectBase;
                    if (xObject.getObjectNumber() == objectKey.getNumber() && 
                            xObject.getGenerationNumber() == objectKey.getGeneration()) {
                        PDResources xObjectResources = isPageLevel ? page.getResources() : getResources(page.getResources(),
                                new PDResources((COSDictionary) resources));
                        return PDXObject.createXObject(xObject.getObject(), xObjectResources);
                    }
                    stack.add(xObject.getObject());
                } else if (xObjectBase instanceof COSDictionary) {
                    stack.add(xObjectBase);
                }
            }
            isPageLevel = false;
        }
        return null;
    }

    private static PDResources getResources(PDResources pageResources, PDResources xObjectResources) throws IOException {
        PDResources resources = new PDResources(pageResources.getCOSObject());
//        for (COSName name : xObjectResources.getXObjectNames()) {
//             resources.put(name, xObjectResources.getXObject(name));
//        }
        for (COSName name : xObjectResources.getFontNames()) {
            resources.put(name, xObjectResources.getFont(name));
        }
        for (COSName name : xObjectResources.getPatternNames()) {
            resources.put(name, xObjectResources.getPattern(name));
        }
        for (COSName name : xObjectResources.getPropertiesNames()) {
            resources.put(name, xObjectResources.getProperties(name));
        }
        for (COSName name : xObjectResources.getShadingNames()) {
            resources.put(name, xObjectResources.getShading(name));
        }
        for (COSName name : xObjectResources.getExtGStateNames()) {
            resources.put(name, xObjectResources.getExtGState(name));
        }
        for (COSName name : xObjectResources.getColorSpaceNames()) {
            resources.put(name, xObjectResources.getColorSpace(name));
        }
        return resources;
    }
    
    @Override
    public void close() throws IOException {
        if (document != null) {
            document.close();
        }
    }

    public boolean isLoad() {
        return isLoad;
    }
}
