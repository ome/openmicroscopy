import static omero.rtypes.*;
import omero.RInt;
import omero.api.*;
import omero.model.*;

public class Main {

    public static void main(String args[]) throws Exception{

        omero.client client = new omero.client();
        try {
            ServiceFactoryPrx sf = client.createSession();
            Roi roi = createRoi();
            roi = (Roi) sf.getUpdateService().saveAndReturnObject( roi );
            System.out.println("Roi:" + roi.getId().getValue());
            Image image = roi.getImage();

            IRoiPrx roiService = sf.getRoiService();
            RoiResult rr1 = roiService.findByImage( image.getId().getValue(), null );
            RoiResult rr2 = roiService.findByPlane( image.getId().getValue(), 0, 1, null );
            RoiResult rr3 = roiService.findByRoi( roi.getId().getValue(), null );

            //
            // The other methods -- getPoints and get*Stats()
            // all require actual data and so are omitted here.
            //

        } finally {
            client.closeSession();
        }

    }

    public static Roi createRoi() {

		RInt GREY = rint(11184810);
        CommentAnnotation comment = new CommentAnnotationI();
        comment.setTextValue(rstring("Wow. Take a look at this!"));

        Image image = new ImageI();
        image.setName(rstring("roi-example"));

        OriginalFile ofile = new OriginalFileI();
        ofile.setName(rstring("roi-source.xml"));
        ofile.setPath(rstring("roi-source.xml"));
        ofile.setSha1(rstring("0"));
        ofile.setSize(rlong(0));
        ofile.setMimetype(rstring("text/xml"));

        Roi roi = new RoiI();
        roi.setDescription(rstring("An example ROI with lots of shapes"));
        roi.setImage(image);              // Image container
        roi.linkAnnotation(comment);      // Annotated like Image
        roi.setSource(ofile);             // Source file during import ??

        // A real ellipse
        Ellipse ellipse = new EllipseI();
        ellipse.setCx(rdouble(1));
        ellipse.setCy(rdouble(1));
        ellipse.setRx(rdouble(1));
        ellipse.setRy(rdouble(2));
        roi.addShape(ellipse);

        // A circle as an ellipse
        Ellipse circle = new EllipseI();
        circle.setCx(rdouble(5));
        circle.setCy(rdouble(8));
        circle.setRx(rdouble(1));
        circle.setRy(rdouble(1)); // Same radius
        roi.addShape(circle);

        // Making a grouping of lines
        // for something like a scale bar
        Line line1 = new LineI();
        line1.setX1(rdouble(1));
        line1.setX2(rdouble(1));
        line1.setY1(rdouble(1));
        line1.setY2(rdouble(4)); // Fairly long
        line1.setG(rstring("scale-bar"));
        roi.addShape(line1);

        Line line2 = new LineI();
        line2.setX1(rdouble(2));
        line2.setX2(rdouble(2)); // Shifted to the right
        line2.setY1(rdouble(1));
        line2.setY2(rdouble(2)); // Half as long
        line2.setG(rstring("scale-bar"));
        roi.addShape(line2);

        Line line3 = new LineI();
        line3.setX1(rdouble(3));
        line3.setX2(rdouble(3)); // Shifted to the right again
        line3.setY1(rdouble(1));
        line3.setY2(rdouble(4)); // Full length
        line3.setG(rstring("scale-bar"));
        roi.addShape(line3);

        Line other = new LineI();
        other.setX1(rdouble(0));
        other.setX2(rdouble(5));
        other.setY1(rdouble(4));
        other.setY2(rdouble(9));
        other.setG(rstring("other"));
        roi.addShape(other);

        // Some examples of what bioformats
        // can already parse
        // Circle and Ellipse as above
        Rect rect = new RectI();
        rect.setX(rdouble(2.4));
        rect.setY(rdouble(1.5));
        rect.setWidth(rdouble(10));
        rect.setHeight(rdouble(10));
        rect.setTransform(null);
        roi.addShape(rect);

        Line line = new LineI();
        line.setX1(rdouble(100));
        line.setX2(rdouble(200));
        line.setY1(rdouble(300));
        line.setY2(rdouble(400));
        line.setTransform(rstring("100 0 0 200 0 0"));
        roi.addShape(line);

        Mask mask = new MaskI();
        mask.setX(rdouble(10));
        mask.setY(rdouble(10));
        mask.setWidth(rdouble(100.0));
        mask.setHeight(rdouble(100.0));
        // Remember: Pixels can't be created without an Image!
        mask.setPixels(new PixelsI(0, false));

        Point point = new PointI();
        point.setCx(rdouble(75.0));
        point.setCy(rdouble(75.0));
        // Point.r should be removed
        point.setTransform(null);
        roi.addShape(point);

        // For the following three, the format for the string value is unclear:
        //
        //   Path.d
        //   Polygon.points
        //   Polyline.points
        //
        Path path = new PathI();
        path.setD(rstring("M 100 100 L 300 100 L 200 300 z"));
        path.setTransform(null);
        roi.addShape(path);

        Polygon polygon = new PolygonI();
        polygon.setPoints(rstring("100.0,200.0 553.9,593.5 92.3,59.9"));
        polygon.setTransform(null);
        roi.addShape(polygon);

        Polyline polyline = new PolylineI();
        polyline.setPoints(rstring("100.0,200.0 553.9,593.5 92.3,59.9"));
        polyline.setTransform(null);
        roi.addShape(polyline);

        // Display fields which could quickly
        // be parsed from known formats
        Label text = new LabelI();
        text.setTextValue(rstring("This is a polyline"));
        text.setFontFamily(rstring("Verdana"));
        text.setFontSize(rint(40));
        text.setFontWeight(rstring("bold"));
        text.setFillColor(GREY);
        text.setStrokeColor(GREY);
        text.setStrokeWidth(rint(25));
        text.setVisibility(rbool(true));
        text.setLocked(rbool(true));
        roi.addShape(text);

        // Other options which may come with time
        text.setVectorEffect(rstring("non-scaling-stroke"));
        text.setFontStretch(rstring("wider"));
        text.setFontStyle(rstring("italic"));
        text.setFontVariant(rstring("small-caps"));
        text.setFillColor(GREY);
        text.setFillRule(rstring("even-odd"));
        text.setStrokeColor(GREY);
        text.setStrokeDashArray(rstring("10 20 30 10"));
        text.setStrokeLineCap(rstring("butt"));
        text.setStrokeWidth(rint(10));
        text.setAnchor(rstring("middle"));
        text.setDecoration(rstring("underline"));
        text.setBaselineShift(rstring("70%"));
        text.setGlyphOrientationVertical(rint(90));
        text.setDirection(rstring("rtl"));
        text.setWritingMode(rstring("tb-rl"));

        Rect singlePlane = new RectI();
        singlePlane.setX(rdouble(2.4));
        singlePlane.setY(rdouble(1.5));
        singlePlane.setWidth(rdouble(10));
        singlePlane.setHeight(rdouble(10));
        singlePlane.setTheZ(rint(0));
        singlePlane.setTheT(rint(1));
        roi.addShape(singlePlane);

        return roi;

   }
}
