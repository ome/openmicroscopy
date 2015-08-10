/*
 * #%L
 * OME database I/O package for communicating with OME and OMERO servers.
 * %%
 * Copyright (C) 2005 - 2013 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

package loci.ome.io;

import static ome.formats.model.UnitsFactory.convertLength;
import static ome.formats.model.UnitsFactory.convertTime;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import loci.common.Constants;
import loci.common.DateTools;
import loci.common.RandomAccessInputStream;
import loci.formats.CoreMetadata;
import loci.formats.FormatException;
import loci.formats.FormatReader;
import loci.formats.FormatTools;
import loci.formats.MetadataTools;
import loci.formats.meta.MetadataStore;
import ome.units.UNITS;
import ome.xml.model.primitives.NonNegativeInteger;
import ome.xml.model.primitives.Timestamp;
import omero.RInt;
import omero.RString;
import omero.RTime;
import omero.ServerError;
import omero.api.IAdminPrx;
import omero.api.RawPixelsStorePrx;
import omero.api.RoiOptions;
import omero.api.RoiResult;
import omero.api.ServiceFactoryPrx;
import omero.model.Channel;
import omero.model.EllipseI;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.Image;
import omero.model.Label;
import omero.model.Length;
import omero.model.LineI;
import omero.model.LogicalChannel;
import omero.model.Pixels;
import omero.model.PointI;
import omero.model.PolygonI;
import omero.model.PolylineI;
import omero.model.RectI;
import omero.model.Roi;
import omero.model.Shape;
import omero.model.Time;
import omero.sys.EventContext;
import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

/**
 * Implementation of {@link loci.formats.IFormatReader}
 * for use in export from an OMERO Beta 4.2.x database.
 *
 */
public class OmeroReader extends FormatReader {

    // -- Constants --

    public static final int DEFAULT_PORT = 4064;

    // -- Fields --

    private String server;
    private String username;
    private String password;
    private int thePort = DEFAULT_PORT;
    private String sessionID;
    private String group;
    private Long groupID = null;
    private boolean encrypted = true;

    private omero.client client;
    private RawPixelsStorePrx store;
    private Image img;
    private Pixels pix;

    // -- Constructors --

    public OmeroReader() {
        super("OMERO", "*");
    }

    // -- OmeroReader methods --

    public void setServer(String server) {
        this.server = server;
    }

    public void setPort(int port) {
        thePort = port;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public void setGroupName(String group) {
        this.group = group;
    }

    public void setGroupID(Long groupID) {
        this.groupID = groupID;
    }

    // -- IFormatReader methods --

    @Override
    public boolean isThisType(String name, boolean open) {
        return name.startsWith("omero:");
    }

    @Override
    public byte[] openBytes(int no, byte[] buf, int x, int y, int w, int h)
            throws FormatException, IOException
    {
        FormatTools.assertId(currentId, true, 1);
        FormatTools.checkPlaneNumber(this, no);
        FormatTools.checkBufferSize(this, buf.length, w, h);

        final int[] zct = FormatTools.getZCTCoords(this, no);

        final byte[] plane;
        try {
            plane = store.getPlane(zct[0], zct[1], zct[2]);
        }
        catch (ServerError e) {
            throw new FormatException(e);
        }

        RandomAccessInputStream s = new RandomAccessInputStream(plane);
        readPlane(s, x, y, w, h, buf);
        s.close();

        return buf;
    }

    @Override
    public void close(boolean fileOnly) throws IOException {
        super.close(fileOnly);
        if (!fileOnly && client != null) {
            client.closeSession();
        }
    }

    @Override
    protected void initFile(String id) throws FormatException, IOException {
        LOGGER.debug("OmeroReader.initFile({})", id);

        super.initFile(id);

        if (!id.startsWith("omero:")) {
            throw new IllegalArgumentException("Not an OMERO id: " + id);
        }

        // parse credentials from id string

        LOGGER.info("Parsing credentials");

        String address = server, user = username, pass = password;
        int port = thePort;
        long iid = -1;

        final String[] tokens = id.substring(6).split("\n");
        for (String token : tokens) {
            final int equals = token.indexOf("=");
            if (equals < 0) continue;
            final String key = token.substring(0, equals);
            final String val = token.substring(equals + 1);
            if (key.equals("server")) address = val;
            else if (key.equals("user")) user = val;
            else if (key.equals("pass")) pass = val;
            else if (key.equals("port")) {
                try {
                    port = Integer.parseInt(val);
                }
                catch (NumberFormatException exc) { }
            }
            else if (key.equals("session")) {
                sessionID = val;
            }
            else if (key.equals("groupName")) {
                group = val;
            }
            else if (key.equals("groupID")) {
                groupID = new Long(val);
            }
            else if (key.equals("iid")) {
                try {
                    iid = Long.parseLong(val);
                }
                catch (NumberFormatException exc) { }
            }
        }

        if (address == null) {
            throw new FormatException("Invalid server address");
        }
        if (user == null && sessionID == null) {
            throw new FormatException("Invalid username");
        }
        if (pass == null && sessionID == null) {
            throw new FormatException("Invalid password");
        }
        if (iid < 0) {
            throw new FormatException("Invalid image ID");
        }

        try {
            // authenticate with OMERO server

            LOGGER.info("Logging in");

            client = new omero.client(address, port);
            ServiceFactoryPrx serviceFactory = null;
            if (user != null && pass != null) {
                serviceFactory = client.createSession(user, pass);
            }
            else {
                serviceFactory = client.createSession(sessionID, sessionID);
            }

            if (!encrypted) {
                client = client.createClient(false);
                serviceFactory = client.getSession();
            }

            IAdminPrx iAdmin = serviceFactory.getAdminService();
            EventContext eventContext = iAdmin.getEventContext();

            if (group != null || groupID != null) {

                ExperimenterGroup defaultGroup =
                        iAdmin.getDefaultGroup(eventContext.userId);
                if (!defaultGroup.getName().getValue().equals(group) &&
                        !new Long(defaultGroup.getId().getValue()).equals(groupID))
                {
                    Experimenter exp = iAdmin.getExperimenter(eventContext.userId);
                    List<Long> groupList = iAdmin.getMemberOfGroupIds(exp);

                    Iterator<Long> i = groupList.iterator();

                    Long g = null;

                    boolean in = false;
                    while (i.hasNext()) {
                        g = i.next();
                        if (g.equals(groupID))
                        {
                            in = true;
                            groupID = g;
                            break;
                        }
                    }
                    if (in) {
                        iAdmin.setDefaultGroup(exp, iAdmin.getGroup(groupID));
                        serviceFactory.setSecurityContext(
                                new ExperimenterGroupI(groupID, false));
                    }
                }
            }

            // get raw pixels store and pixels

            store = serviceFactory.createRawPixelsStore();

            img = (Image) serviceFactory.getContainerService()
                    .getImages("Image", Arrays.asList(iid), null).get(0);

            if (img == null) {
                throw new FormatException("Could not find Image with ID=" + iid +
                        " in group '" + group + "'.");
            }

            long pixelsId = img.getPixels(0).getId().getValue();

            pix = serviceFactory.getPixelsService().retrievePixDescription(pixelsId);
            store.setPixelsId(pixelsId, false);

            final int sizeX = pix.getSizeX().getValue();
            final int sizeY = pix.getSizeY().getValue();
            final int sizeZ = pix.getSizeZ().getValue();
            final int sizeC = pix.getSizeC().getValue();
            final int sizeT = pix.getSizeT().getValue();
            final String pixelType = pix.getPixelsType().getValue().getValue();

            // populate metadata

            LOGGER.info("Populating metadata");

            CoreMetadata m = core.get(0);
            m.sizeX = sizeX;
            m.sizeY = sizeY;
            m.sizeZ = sizeZ;
            m.sizeC = sizeC;
            m.sizeT = sizeT;
            m.rgb = false;
            m.littleEndian = false;
            m.dimensionOrder = "XYZCT";
            m.imageCount = sizeZ * sizeC * sizeT;
            m.pixelType = FormatTools.pixelTypeFromString(pixelType);

            Length x = pix.getPhysicalSizeX();
            Length y = pix.getPhysicalSizeY();
            Length z = pix.getPhysicalSizeZ();
            Time t = pix.getTimeIncrement();

            ome.units.quantity.Time t2 = convertTime(t);
            ome.units.quantity.Length px = convertLength(x);
            ome.units.quantity.Length py = convertLength(y);
            ome.units.quantity.Length pz = convertLength(z);

            RString imageName = img.getName();
            String name = imageName == null ? null : imageName.getValue();

            if (name != null) {
                currentId = name;
            }
            else {
                currentId = "Image ID " + iid;
            }

            RString imgDescription = img.getDescription();
            String description =
                    imgDescription == null ? null : imgDescription.getValue();
            RTime date = img.getAcquisitionDate();

            MetadataStore store = getMetadataStore();

            MetadataTools.populatePixels(store, this);
            store.setImageName(name, 0);
            store.setImageDescription(description, 0);
            if (date != null) {
                store.setImageAcquisitionDate(new Timestamp(
                        DateTools.convertDate(date.getValue(), (int) DateTools.UNIX_EPOCH)),
                        0);
            }

            if (px != null && px.value().doubleValue() > 0) {
                store.setPixelsPhysicalSizeX(px, 0);
            }
            if (py != null && py.value().doubleValue() > 0) {
                store.setPixelsPhysicalSizeY(py, 0);
            }
            if (pz != null && pz.value().doubleValue() > 0) {
                store.setPixelsPhysicalSizeZ(pz, 0);
            }
            if (t2 != null) {
                store.setPixelsTimeIncrement(t2, 0);
            }

            List<Channel> channels = pix.copyChannels();
            for (int c=0; c<channels.size(); c++) {
                LogicalChannel channel = channels.get(c).getLogicalChannel();

                Length emWave = channel.getEmissionWave();
                Length exWave = channel.getExcitationWave();
                Length pinholeSize = channel.getPinHoleSize();
                RString cname = channel.getName();

                ome.units.quantity.Length emission = convertLength(emWave);
                ome.units.quantity.Length excitation = convertLength(exWave);
                String channelName = cname == null ? null : cname.getValue();
                ome.units.quantity.Length pinhole = convertLength(pinholeSize);

                if (channelName != null) {
                    store.setChannelName(channelName, 0, c);
                }
                if (pinholeSize != null) {
                    store.setChannelPinholeSize(pinhole, 0, c);
                }
                if (emission != null && emission.value().doubleValue() > 0) {
                    store.setChannelEmissionWavelength( emission, 0, c);
                }
                if (excitation != null && excitation.value().doubleValue() > 0) {
                    store.setChannelExcitationWavelength(excitation, 0, c);
                }
            }

            //            store.setImageID("omero:iid=", (int) img.getId().getValue());
            //Load ROIs to the img -->
            RoiOptions options = new RoiOptions();
            options.userId = omero.rtypes.rlong(iAdmin.getEventContext().userId);
            RoiResult r = serviceFactory.getRoiService().findByImage(img.getId().getValue(), new RoiOptions());
            if (r != null){
                List<Roi> rois = r.rois;

                int n = rois.size();
                if (n != 0){
                    saveOmeroRoiToMetadataStore(rois,store);
                }
            }
        }
        catch (CannotCreateSessionException e) {
            throw new FormatException(e);
        }
        catch (PermissionDeniedException e) {
            throw new FormatException(e);
        }
        catch (ServerError e) {
            throw new FormatException(e);
        }
    }

    /** A simple command line tool for downloading images from OMERO. */
    public static void main(String[] args) throws Exception {
        // parse OMERO credentials
        BufferedReader con = new BufferedReader(
                new InputStreamReader(System.in, Constants.ENCODING));

        System.out.print("Server? ");
        final String server = con.readLine();

        System.out.printf("Port [%d]? ", DEFAULT_PORT);
        final String portString = con.readLine();
        final int port = portString.equals("") ? DEFAULT_PORT :
            Integer.parseInt(portString);

        System.out.print("Username? ");
        final String user = con.readLine();

        System.out.print("Password? ");
        final String pass = new String(con.readLine());

        System.out.print("Group? ");
        final String group = con.readLine();

        System.out.print("Image ID? ");
        final int imageId = Integer.parseInt(con.readLine());
        System.out.print("\n\n");

        // construct the OMERO reader
        final OmeroReader omeroReader = new OmeroReader();
        omeroReader.setUsername(user);
        omeroReader.setPassword(pass);
        omeroReader.setServer(server);
        omeroReader.setPort(port);
        omeroReader.setGroupName(group);
        final String id = "omero:iid=" + imageId;
        try {
            omeroReader.setId(id);
        }
        catch (Exception e) {
            omeroReader.close();
            throw e;
        }
        omeroReader.close();
    }
    /** Converts omero.model.Roi to ome.xml.model.* and updates the MetadataStore */
    public static void saveOmeroRoiToMetadataStore(List<omero.model.Roi> rois,
            MetadataStore store) {

        int n = rois.size();

        for (int thisROI=0  ; thisROI<n ; thisROI++){
            omero.model.Roi roi = rois.get(thisROI);
            int numShapes = roi.sizeOfShapes();
            int roiNum = thisROI;
            String roiID;
            roiID = MetadataTools.createLSID("ROI", roiNum, 0);
            for(int ns=0 ; ns<numShapes ; ns++){
                omero.model.Shape shape = roi.getShape(ns);
                int shapeNum= ns;

                if(shape instanceof LineI){
                    storeOmeroLine(shape,store, roiNum, shapeNum);
                }
                else if(shape instanceof PointI){
                    storeOmeroPoint(shape,store, roiNum, shapeNum);
                }
                else if(shape instanceof EllipseI){
                    storeOmeroEllipse(shape,store, roiNum, shapeNum);
                }
                else if(shape instanceof RectI){
                    storeOmeroRect(shape,store, roiNum, shapeNum);
                }
                else if(shape instanceof PolygonI || shape instanceof PolylineI) {
                    storeOmeroPolygon(shape,store, roiNum, shapeNum);
                }
                else if (shape instanceof Label){
                    //add support for TextROI's
                    storeOmeroLabel(shape,store, roiNum, shapeNum);
                }

            }
            if (roiID!=null){
                store.setROIID(roiID, roiNum);
                store. setImageROIRef(roiID, 0, roiNum);
            }
        }

    }

    /** Converts omero.model.Shape (omero.model.Label in this case) to ome.xml.model.* and updates the MetadataStore */
    private static void storeOmeroLabel(Shape shape, MetadataStore store,
            int roiNum, int shapeNum) {

        Label shape1 = (Label) shape;

        String polylineID = MetadataTools.createLSID("Shape", roiNum, shapeNum);
        store.setLabelID(polylineID, roiNum, shapeNum);
        if (shape1.getTextValue() != null){
            store.setLabelText(shape1.getTextValue().getValue(), roiNum, shapeNum);
        }
        store.setLabelX(shape1.getX().getValue(), roiNum, shapeNum);
        store.setLabelY(shape1.getY().getValue(), roiNum, shapeNum);
        
        store.setLabelTheC(unwrap(shape1.getTheC()), roiNum, shapeNum);
        store.setLabelTheZ(unwrap(shape1.getTheZ()), roiNum, shapeNum);
        store.setLabelTheT(unwrap(shape1.getTheT()), roiNum, shapeNum);

        if (shape1.getStrokeWidth() != null) {
            store.setLabelStrokeWidth(new ome.units.quantity.Length(shape1.getStrokeWidth().getValue(), UNITS.PIXEL), roiNum, shapeNum);
        }
        if (shape1.getStrokeColor() != null){
            store.setLabelStrokeColor(new ome.xml.model.primitives.Color(shape1.getStrokeColor().getValue()), roiNum, shapeNum);
        }
        if (shape1.getFillColor() != null){
            store.setLabelFillColor(new ome.xml.model.primitives.Color(shape1.getFillColor().getValue()), roiNum, shapeNum);
        }

        if (shape1.getFontSize() != null){
            ome.units.quantity.Length labelSize = new ome.units.quantity.Length(shape1.getFontSize().getValue(), UNITS.PIXEL);
            store.setLabelFontSize(labelSize , roiNum, shapeNum);
        }

    }

    private static NonNegativeInteger unwrap(RInt r) {

        if (r == null) {
            return null;
        }
        return new NonNegativeInteger(r.getValue()+1);
    }

    /** Converts omero.model.Shape (omero.model.RectI in this case) to ome.xml.model.* and updates the MetadataStore */
    private static void storeOmeroRect(omero.model.Shape shape,
            MetadataStore store, int roiNum, int shapeNum) {

        RectI shape1 = (RectI) shape;

        double x1 = shape1.getX().getValue();
        double y1 = shape1.getY().getValue();
        double width = shape1.getWidth().getValue();
        double height = shape1.getHeight().getValue();

        String polylineID = MetadataTools.createLSID("Shape", roiNum, shapeNum);
        store.setRectangleID(polylineID, roiNum, shapeNum);
        store.setRectangleX(x1, roiNum, shapeNum);
        store.setRectangleY(y1, roiNum, shapeNum);
        store.setRectangleWidth(width, roiNum, shapeNum);
        store.setRectangleHeight(height, roiNum, shapeNum);
        store.setRectangleTheC(unwrap(shape1.getTheC()), roiNum, shapeNum);
        store.setRectangleTheZ(unwrap(shape1.getTheZ()), roiNum, shapeNum);
        store.setRectangleTheT(unwrap(shape1.getTheT()), roiNum, shapeNum);

        if (shape1.getTextValue() != null){
            store.setRectangleText(shape1.getTextValue().getValue(), roiNum, shapeNum);
        }
        if (shape1.getStrokeWidth() != null) {
            store.setRectangleStrokeWidth(new ome.units.quantity.Length(shape1.getStrokeWidth().getValue(), UNITS.PIXEL), roiNum, shapeNum);
        }
        if (shape1.getStrokeColor() != null){
            store.setRectangleStrokeColor(new ome.xml.model.primitives.Color(shape1.getStrokeColor().getValue()), roiNum, shapeNum);
        }
        if (shape1.getFillColor() != null){
            store.setRectangleFillColor(new ome.xml.model.primitives.Color(shape1.getFillColor().getValue()), roiNum, shapeNum);
        }

    }
    /** Converts omero.model.Shape (omero.model.EllipseI in this case) to ome.xml.model.* and updates the MetadataStore */
    private static void storeOmeroEllipse(omero.model.Shape shape,
            MetadataStore store, int roiNum, int shapeNum) {

        EllipseI shape1 = (EllipseI) shape;

        double x1 = shape1.getCx().getValue();
        double y1 = shape1.getCy().getValue();
        double width = shape1.getRx().getValue();
        double height = shape1.getRy().getValue();

        String polylineID = MetadataTools.createLSID("Shape", roiNum, shapeNum);
        store.setEllipseID(polylineID, roiNum, shapeNum);
        store.setEllipseX(x1, roiNum, shapeNum);
        store.setEllipseY(y1, roiNum, shapeNum);
        store.setEllipseRadiusX(width, roiNum, shapeNum);
        store.setEllipseRadiusY(height, roiNum, shapeNum);
        store.setEllipseTheC(unwrap(shape1.getTheC()), roiNum, shapeNum);
        store.setEllipseTheZ(unwrap(shape1.getTheZ()), roiNum, shapeNum);
        store.setEllipseTheT(unwrap(shape1.getTheT()), roiNum, shapeNum);

        if (shape1.getTextValue() != null){
            store.setEllipseText(shape1.getTextValue().getValue(), roiNum, shapeNum);
        }
        if (shape1.getStrokeWidth() != null) {
            store.setEllipseStrokeWidth(new ome.units.quantity.Length(shape1.getStrokeWidth().getValue(), UNITS.PIXEL), roiNum, shapeNum);
        }
        if (shape1.getStrokeColor() != null){
            store.setEllipseStrokeColor(new ome.xml.model.primitives.Color(shape1.getStrokeColor().getValue()), roiNum, shapeNum);
        }
        if (shape1.getFillColor() != null){
            store.setEllipseFillColor(new ome.xml.model.primitives.Color(shape1.getFillColor().getValue()), roiNum, shapeNum);
        }

    }
    /** Converts omero.model.Shape (omero.model.PointI in this case) to ome.xml.model.* and updates the MetadataStore */
    private static void storeOmeroPoint(omero.model.Shape shape,
            MetadataStore store, int roiNum, int shapeNum) {

        PointI shape1 = (PointI) shape;
        double ox1 = shape1.getCx().getValue();
        double oy1 = shape1.getCy().getValue();

        String polylineID = MetadataTools.createLSID("Shape", roiNum, shapeNum);
        store.setPointID(polylineID, roiNum, shapeNum);
        store.setPointX(ox1, roiNum, shapeNum);
        store.setPointY(oy1, roiNum, shapeNum);
        store.setPointTheC(unwrap(shape1.getTheC()), roiNum, shapeNum);
        store.setPointTheZ(unwrap(shape1.getTheZ()), roiNum, shapeNum);
        store.setPointTheT(unwrap(shape1.getTheT()), roiNum, shapeNum);

        if (shape1.getTextValue() != null){
            store.setPointText(shape1.getTextValue().getValue(), roiNum, shapeNum);
        }
        if (shape1.getStrokeWidth() != null) {
            store.setPointStrokeWidth(new ome.units.quantity.Length(shape1.getStrokeWidth().getValue(), UNITS.PIXEL), roiNum, shapeNum);
        }
        if (shape1.getStrokeColor() != null){
            store.setPointStrokeColor(new ome.xml.model.primitives.Color(shape1.getStrokeColor().getValue()), roiNum, shapeNum);
        }
        if (shape1.getFillColor() != null){
            store.setPointFillColor(new ome.xml.model.primitives.Color(shape1.getFillColor().getValue()), roiNum, shapeNum);
        }

    }
    /** Converts omero.model.Shape (omero.model.LineI in this case) to ome.xml.model.* and updates the MetadataStore */
    private static void storeOmeroLine(omero.model.Shape shape,
            MetadataStore store, int roiNum, int shapeNum) {

        LineI shape1 = (LineI) shape;
        double x1 = shape1.getX1().getValue();
        double y1 = shape1.getY1().getValue();
        double x2 = shape1.getX2().getValue();
        double y2 = shape1.getY2().getValue();

        String polylineID = MetadataTools.createLSID("Shape", roiNum, shapeNum);
        store.setLineID(polylineID, roiNum, shapeNum);

        store.setLineX1(new Double(x1), roiNum, shapeNum);
        store.setLineX2(new Double(x2), roiNum, shapeNum);
        store.setLineY1(new Double(y1), roiNum, shapeNum);
        store.setLineY2(new Double(y2), roiNum, shapeNum);
        store.setLineTheC(unwrap(shape1.getTheC()), roiNum, shapeNum);
        store.setLineTheZ(unwrap(shape1.getTheZ()), roiNum, shapeNum);
        store.setLineTheT(unwrap(shape1.getTheT()), roiNum, shapeNum);

        if (shape1.getTextValue() != null){
            store.setLineText(shape1.getTextValue().getValue(), roiNum, shapeNum);
        }
        if (shape1.getStrokeWidth() != null) {
            store.setLineStrokeWidth(new ome.units.quantity.Length(shape1.getStrokeWidth().getValue(), UNITS.PIXEL), roiNum, shapeNum);
        }
        if (shape1.getStrokeColor() != null){
            store.setLineStrokeColor(new ome.xml.model.primitives.Color(shape1.getStrokeColor().getValue()), roiNum, shapeNum);
        }
        if (shape1.getFillColor() != null){
            store.setLineFillColor(new ome.xml.model.primitives.Color(shape1.getFillColor().getValue()), roiNum, shapeNum);
        }


    }
    /** Converts omero.model.Shape (omero.model.PolygonI/omero.model.PolylineI in this case) to ome.xml.model.* and updates the MetadataStore */
    private static void storeOmeroPolygon(omero.model.Shape shape, MetadataStore store,
            int roiNum, int shapeNum){

        String points=null;
        String polylineID = MetadataTools.createLSID("Shape", roiNum, shapeNum);

        if(shape instanceof PolygonI){
            PolygonI shape1 = (PolygonI) shape;
            points = shape1.getPoints().getValue();
            String points2d = parsePoints(convertPoints(points, "points"));

            store.setPolygonID(polylineID, roiNum, shapeNum);
            store.setPolygonPoints(points2d, roiNum, shapeNum);

            if (shape1.getTextValue() != null){
                store.setPolygonText(shape1.getTextValue().getValue(), roiNum, shapeNum);
            }
            if (shape1.getStrokeWidth() != null) {
                store.setPolygonStrokeWidth(new ome.units.quantity.Length(shape1.getStrokeWidth().getValue(), UNITS.PIXEL), roiNum, shapeNum);
            }
            if (shape1.getStrokeColor() != null){
                store.setPolygonStrokeColor(new ome.xml.model.primitives.Color(shape1.getStrokeColor().getValue()), roiNum, shapeNum);
            }
            if (shape1.getFillColor() != null){
                store.setPolygonFillColor(new ome.xml.model.primitives.Color(shape1.getFillColor().getValue()), roiNum, shapeNum);
            }

            store.setPolygonTheC(unwrap(shape1.getTheC()), roiNum, shapeNum);
            store.setPolygonTheZ(unwrap(shape1.getTheZ()), roiNum, shapeNum);
            store.setPolygonTheT(unwrap(shape1.getTheT()), roiNum, shapeNum);
        }else{
            PolylineI shape1 = (PolylineI) shape;
            points = shape1.getPoints().getValue();
            String points2d = parsePoints(convertPoints(points, "points"));

            store.setPolylineID(polylineID, roiNum, shapeNum);
            store.setPolylinePoints(points2d, roiNum, shapeNum);

            if (shape1.getTextValue() != null){
                store.setPolylineText(shape1.getTextValue().getValue(), roiNum, shapeNum);
            }
            if (shape1.getStrokeWidth() != null) {
                store.setPolylineStrokeWidth(new ome.units.quantity.Length(shape1.getStrokeWidth().getValue(), UNITS.PIXEL), roiNum, shapeNum);
            }
            if (shape1.getStrokeColor() != null){
                store.setPolylineStrokeColor(new ome.xml.model.primitives.Color(shape1.getStrokeColor().getValue()), roiNum, shapeNum);
            }
            if (shape1.getFillColor() != null){
                store.setPolylineFillColor(new ome.xml.model.primitives.Color(shape1.getFillColor().getValue()), roiNum, shapeNum);
            }
            store.setPolylineTheC(unwrap(shape1.getTheC()), roiNum, shapeNum);
            store.setPolylineTheZ(unwrap(shape1.getTheZ()), roiNum, shapeNum);
            store.setPolylineTheT(unwrap(shape1.getTheT()), roiNum, shapeNum);
        }

    }
    /** Parse Points String obtained from convertPoints and extract Coordinates as String*/
    protected static String parsePoints(String str)
    {
        String points = null;

        if (str == null) return points;

        List<Integer> x = new ArrayList<Integer>();
        List<Integer> y = new ArrayList<Integer>();
        StringTokenizer tt = new StringTokenizer(str, " ");
        int numTokens = tt.countTokens();
        StringTokenizer t;
        int total;
        for (int i = 0; i < numTokens; i++) {
            t = new StringTokenizer(tt.nextToken(), ",");
            total = t.countTokens()/2;
            for (int j = 0; j < total; j++) {
                x.add(new Integer(t.nextToken()));
                y.add(new Integer(t.nextToken()));
            }
        }

        for (int i=0 ; i<x.size() ; i++){

            if(i==0){
                points = (x.get(i) + "," + y.get(i));
            }else{
                points= (points + " " + x.get(i) + "," + y.get(i));
            }
        }

        return points.toString();
    }
    /** Split Points coordinates and obtain information for the first plane alone */
    private static String convertPoints(String pts, String type)
    {
        if (pts.length() == 0) return "";
        if (!pts.contains(type)) {//data inserted following the schema
            return pts;
        }
        String exp = type+'[';
        int typeStr = pts.indexOf(exp, 0);
        int start = pts.indexOf('[', typeStr);
        int end = pts.indexOf(']', start);
        return pts.substring(start+1,end);
    }



}
