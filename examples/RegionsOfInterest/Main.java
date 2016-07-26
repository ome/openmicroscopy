
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

		RInt GREY = omero.rtypes.rint(11184810);
        CommentAnnotation comment = new CommentAnnotationI();
        comment.setTextValue(omero.rtypes.rstring("Wow. Take a look at this!"));

        Image image = new ImageI();
        image.setName(omero.rtypes.rstring("roi-example"));

        OriginalFile ofile = new OriginalFileI();
        ofile.setName(omero.rtypes.rstring("roi-source.xml"));
        ofile.setPath(omero.rtypes.rstring("roi-source.xml"));
        ofile.setSha1(omero.rtypes.rstring("0"));
        ofile.setSize(omero.rtypes.rlong(0));
        ofile.setMimetype(omero.rtypes.rstring("text/xml"));

        Roi roi = new RoiI();
        roi.setDescription(omero.rtypes.rstring("An example ROI with lots of shapes"));
        roi.setImage(image);              // Image container
        roi.linkAnnotation(comment);      // Annotated like Image
        roi.setSource(ofile);             // Source file during import ??

        // A real ellipse
        Ellipse ellipse = new EllipseI();
        ellipse.setX(omero.rtypes.rdouble(1));
        ellipse.setY(omero.rtypes.rdouble(1));
        ellipse.setRadiusX(omero.rtypes.rdouble(1));
        ellipse.setRadiusY(omero.rtypes.rdouble(2));
        roi.addShape(ellipse);

        // A circle as an ellipse
        Ellipse circle = new EllipseI();
        circle.setX(omero.rtypes.double(5));
        circle.setY(omero.rtypes.rdouble(8));
        circle.setRadiusX(omero.rtypes.rdouble(1));
        circle.setRadiusY(omero.rtypes.rdouble(1)); // Same radius
        roi.addShape(circle);

        // Making a grouping of lines
        // for something like a scale bar
        Line line1 = new LineI();
        line1.setX1(omero.rtypes.rdouble(1));
        line1.setX2(omero.rtypes.rdouble(1));
        line1.setY1(omero.rtypes.rdouble(1));
        line1.setY2(omero.rtypes.rdouble(4)); // Fairly long
        line1.setG(omero.rtypes.rstring("scale-bar"));
        roi.addShape(line1);

        Line line2 = new LineI();
        line2.setX1(omero.rtypes.rdouble(2));
        line2.setX2(omero.rtypes.rdouble(2)); // Shifted to the right
        line2.setY1(omero.rtypes.rdouble(1));
        line2.setY2(omero.rtypes.rdouble(2)); // Half as long
        line2.setG(omero.rtypes.rstring("scale-bar"));
        roi.addShape(line2);

        Line line3 = new LineI();
        line3.setX1(omero.rtypes.rdouble(3));
        line3.setX2(omero.rtypes.rdouble(3)); // Shifted to the right again
        line3.setY1(omero.rtypes.rdouble(1));
        line3.setY2(omero.rtypes.rdouble(4)); // Full length
        line3.setG(omero.rtypes.rstring("scale-bar"));
        roi.addShape(line3);

        Line other = new LineI();
        other.setX1(omero.rtypes.rdouble(0));
        other.setX2(omero.rtypes.rdouble(5));
        other.setY1(omero.rtypes.rdouble(4));
        other.setY2(omero.rtypes.rdouble(9));
        other.setG(omero.rtypes.rstring("other"));
        roi.addShape(other);

        // Some examples of what bioformats
        // can already parse
        // Circle and Ellipse as above
        Rectangle rect = new RectangleI();
        rect.setX(omero.rtypes.rdouble(2.4));
        rect.setY(omero.rtypes.rdouble(1.5));
        rect.setWidth(omero.rtypes.rdouble(10));
        rect.setHeight(omero.rtypes.rdouble(10));
        roi.addShape(rect);

        Line line = new LineI();
        line.setX1(omero.rtypes.rdouble(100));
        line.setX2(omero.rtypes.rdouble(200));
        line.setY1(omero.rtypes.rdouble(300));
        line.setY2(omero.rtypes.rdouble(400));
        roi.addShape(line);

        Mask mask = new MaskI();
        mask.setX(omero.rtypes.rdouble(10));
        mask.setY(omero.rtypes.rdouble(10));
        mask.setWidth(omero.rtypes.rdouble(100.0));
        mask.setHeight(omero.rtypes.rdouble(100.0));
        // Remember: Pixels can't be created without an Image!
        mask.setPixels(new PixelsI(0, false));

        Point point = new PointI();
        point.setX(omero.rtypes.rdouble(75.0));
        point.setY(omero.rtypes.rdouble(75.0));
        // Point.r should be removed
        roi.addShape(point);

        // For the following three, the format for the string value is unclear:
        //
        //   Path.d
        //   Polygon.points
        //   Polyline.points
        //
        Path path = new PathI();
        path.setD(omero.rtypes.rstring("M 100 100 L 300 100 L 200 300 z"));
        roi.addShape(path);

        Polygon polygon = new PolygonI();
        polygon.setPoints(omero.rtypes.rstring("100.0,200.0 553.9,593.5 92.3,59.9"));
        roi.addShape(polygon);

        Polyline polyline = new PolylineI();
        polyline.setPoints(omero.rtypes.rstring("100.0,200.0 553.9,593.5 92.3,59.9"));
        roi.addShape(polyline);

        // Display fields which could quickly
        // be parsed from known formats
        Label text = new LabelI();
        text.setTextValue(omero.rtypes.rstring("This is a polyline"));
        text.setFontFamily(omero.rtypes.rstring("sans-serif"));
        text.setFontSize(omero.rtypes.rint(40));
        text.setFillColor(GREY);
        text.setStrokeColor(GREY);
        text.setStrokeWidth(omero.rtypes.rint(25));
        text.setVisibility(omero.rtypes.rbool(true));
        text.setLocked(omero.rtypes.rbool(true));
        roi.addShape(text);

        // Other options which may come with time
        text.setFontStyle(omero.rtypes.rstring("italic"));
        text.setFillColor(GREY);
        text.setFillRule(omero.rtypes.rstring("even-odd"));
        text.setStrokeColor(GREY);
        text.setStrokeDashArray(omero.rtypes.rstring("10 20 30 10"));
        text.setStrokeWidth(omero.rtypes.rint(10));

        Rectangle singlePlane = new RectangleI();
        singlePlane.setX(omero.rtypes.rdouble(2.4));
        singlePlane.setY(omero.rtypes.rdouble(1.5));
        singlePlane.setWidth(omero.rtypes.rdouble(10));
        singlePlane.setHeight(omero.rtypes.rdouble(10));
        singlePlane.setTheZ(omero.rtypes.rint(0));
        singlePlane.setTheT(omero.rtypes.rint(1));
        roi.addShape(singlePlane);

        return roi;

   }
}
