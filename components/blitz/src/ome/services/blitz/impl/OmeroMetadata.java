/*
 *    OmeroMetadata.java
 *
 *-----------------------------------------------------------------------------
 *
 *    Copyright (C) 2009-2011 Glencoe Software, Inc. All rights reserved.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *-----------------------------------------------------------------------------
 */

package ome.services.blitz.impl;

import static omero.rtypes.rstring;

import static ome.formats.model.UnitsFactory.convertLength;
import static ome.formats.model.UnitsFactory.convertTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import loci.formats.meta.DummyMetadata;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.meta.MetadataStore;
import ome.conditions.ApiUsageException;
import ome.services.db.DatabaseIdentity;
import ome.tools.hibernate.ProxyCleanupFilter;
import ome.tools.hibernate.QueryBuilder;
import ome.units.quantity.Length;
import ome.units.quantity.Time;
import ome.xml.meta.MetadataRoot;
import ome.xml.model.AffineTransform;
import ome.xml.model.enums.AcquisitionMode;
import ome.xml.model.enums.ContrastMethod;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.EnumerationException;
import ome.xml.model.enums.FillRule;
import ome.xml.model.enums.FontFamily;
import ome.xml.model.enums.FontStyle;
import ome.xml.model.enums.IlluminationType;
import ome.xml.model.enums.LineCap;
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.Color;
import ome.xml.model.primitives.NonNegativeInteger;
import ome.xml.model.primitives.PositiveInteger;
import ome.xml.model.primitives.PositiveFloat;
import ome.xml.model.primitives.Timestamp;
import omero.RBool;
import omero.RDouble;
import omero.RInt;
import omero.RLong;
import omero.RString;
import omero.RTime;
import omero.model.Annotation;
import omero.model.Ellipse;
import omero.model.Label;
import omero.model.Line;
import omero.model.Point;
import omero.model.Polygon;
import omero.model.Polyline;
import omero.model.Rect;
import omero.model.XmlAnnotation;
import omero.model.LongAnnotation;
import omero.model.BooleanAnnotation;
import omero.model.DoubleAnnotation;
import omero.model.CommentAnnotation;
import omero.model.TimestampAnnotation;
import omero.model.TagAnnotation;
import omero.model.TermAnnotation;
import omero.model.Channel;
import omero.model.Details;
import omero.model.Event;
import omero.model.ExternalInfo;
import omero.model.ExternalInfoI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.PlaneInfo;
import omero.model.Roi;
import omero.model.Shape;
import omero.util.IceMapper;

import org.apache.commons.lang.ArrayUtils;
import org.joda.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation for {@link MetadataStore} and {@link MetadataRetrieve} that
 * knows how to read and write the OMERO object model.
 * 
 * @author Josh Moore josh at glencoesoftware.com
 * @author Chris Allan callan at blackcat.ca
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public class OmeroMetadata extends DummyMetadata {

    private final static Logger log = LoggerFactory.getLogger(OmeroMetadata.class);

    private static final Pattern AFFINE_TRANSFORM =
            Pattern.compile("\\[\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+\\]");

    // -- State --

    // Objects already in lists below, to avoid duplication
    private final Set<String> seenLSIDs = new HashSet<String>();

    // Images
    private final List<Image> imageList = new ArrayList<Image>();

    // Annotations
    private final List<XmlAnnotation> xmlAnnotationList = 
        new ArrayList<XmlAnnotation>();
    private final List<LongAnnotation> longAnnotationList = 
        new ArrayList<LongAnnotation>();
    private final List<BooleanAnnotation> booleanAnnotationList = 
        new ArrayList<BooleanAnnotation>();
    private final List<DoubleAnnotation> doubleAnnotationList = 
        new ArrayList<DoubleAnnotation>();
    private final List<CommentAnnotation> commentAnnotationList = 
        new ArrayList<CommentAnnotation>();
    private final List<TimestampAnnotation> timestampAnnotationList = 
        new ArrayList<TimestampAnnotation>();
    private final List<TagAnnotation> tagAnnotationList = 
        new ArrayList<TagAnnotation>();
    private final List<TermAnnotation> termAnnotationList = 
        new ArrayList<TermAnnotation>();

    // ROIs
    private final List<Roi> roiList = new ArrayList<Roi>();

    private final DatabaseIdentity db;

    public OmeroMetadata(DatabaseIdentity db) {
        this.db = db;
    }

    @Override
    public MetadataRoot getRoot()
    {
        return new OmeroMetadataRoot();
    }

    public void addImage(Image image) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Adding image for retrieval:", image));
        }
        imageList.add(image);
    }

    public Image getImage(int i) {
        return imageList.get(i);
    }

    public int sizeImages() {
        return imageList.size();
    }

    public String handleLsid(IObject obj) {

        if (obj == null) {
            return null;
        }

        Details d = obj.getDetails();
        ExternalInfo ei = d.getExternalInfo();
        Event ue = d.getUpdateEvent();

        // If an LSID has previously been set, always use that.
        if (ei != null && ei.getLsid() != null) {
            return ei.getLsid().getValue();
        }

        // Otherwise if we have an ID use that as the value.
        if (obj.getId() != null) {

            Class<? extends IObject> k = obj.getClass();
            if (Annotation.class.isAssignableFrom(k)) {
                k = Annotation.class;
            }
            long id = obj.getId().getValue();
            Long v = (ue == null) ? null : ue.getId().getValue();
            if (v == null) {
                return db.lsid(k, id);
            } else {
                return db.lsid(k, id, v);
            }
        }

        // Finally, we need to create an LSID since this object
        // doesn't have one. This should not be done in the general
        // case, since all exported objects should be coming from
        // the database (i.e. have an id), and only on re-import will they
        // be given their
        // LSIDs. However, to simplify any possible

        if (ei == null) {
            ei = new ExternalInfoI();
            d.setExternalInfo(ei);
        }

        String uuid = UUID.randomUUID().toString();
        String prefix = obj.getClass().getSimpleName();
        if (Annotation.class.isAssignableFrom(obj.getClass())) {
            prefix = Annotation.class.getSimpleName();
        }
        String lsid = prefix + ":" + uuid;
        ei.setLsid(rstring(lsid));

        log.warn("Assigned temporary LSID: " + lsid);

        if (log.isDebugEnabled()) {
            log.debug(String.format("Returning LSID for object %s: %s", obj,
                    lsid));
        }
        return ei.getLsid().getValue();

    }

    public void initialize(org.hibernate.Session session) {

        Map<Image, ome.model.core.Image> lookups = new HashMap<Image, ome.model.core.Image>();
        Map<Image, Image> replacements = new HashMap<Image, Image>();
        for (Image image : imageList) {
            if (!image.isLoaded()) {

                final long id = image.getId().getValue(); 
                QueryBuilder qb = new QueryBuilder();
                qb.select("i");
                qb.from("Image", "i");
                qb.join("i.details.owner",    "i_o",      false, true);
                qb.join("i.details.group",    "i_g",      false, true);
                qb.join("i.pixels",           "p",        false, true);
                qb.join("i.annotationLinks",  "i_a_link", true, true);
                qb.join("i_a_link.child",     "i_a",      true, true);
                qb.join("p.details.owner",    "p_o",      false, true);
                qb.join("p.details.group",    "p_g",      false, true);
                qb.join("p.pixelsType",       "pt",       false, true);
                qb.join("p.dimensionOrder",   "do",       false, true);
                qb.join("p.channels",         "c",        false, true);
                qb.join("p.planeInfo",        "pinfo",    true, true);
                qb.join("pinfo.deltaT",       "deltaT",   true, true);
                qb.join("pinfo.exposureTime", "expTime",  true, true);
                qb.join("c.logicalChannel",   "l",        false, true);
                qb.join("l.mode",             "a_mode",   true, true);
                qb.join("l.illumination",     "i_type",   true, true);
                qb.join("l.contrastMethod",   "c_method", true, true);
                qb.join("i.rois",             "r",        true, true);
                qb.join("r.details.owner",    "r_o",      true, true);
                qb.join("r.details.group",    "r_g",      true, true);
                qb.join("r.annotationLinks",  "r_a_link", true, true);
                qb.join("r_a_link.child",     "r_a",      true, true);
                qb.join("r.shapes",           "s",        true, true);
                qb.join("s.details.owner",    "s_o",      true, true);
                qb.join("s.details.group",    "s_g",      true, true);
                qb.join("s.annotationLinks",  "s_a_link", true, true);
                qb.join("s_a_link.child",     "s_a",      true, true);
                qb.where();
                qb.and("i.id = " + id);
                ome.model.core.Image _i =(ome.model.core.Image) qb.query(session).uniqueResult();
                if (_i == null) {
                    throw new ApiUsageException("Cannot load image: " + id);
                }
                lookups.put(image, _i);
                // Now load instrument if available
                if (_i.getInstrument() != null) {
                    qb = new QueryBuilder();
                    qb.select("i");
                    qb.from("Image","i");
                    qb.join("i.instrument",    "n",     true,  true);
                    qb.join("n.objective",     "o",     true,  true);
                    qb.join("o.correction",    "o_cor", true,  true);
                    qb.join("o.immersion",     "o_imm", true,  true);
                    qb.join("n.details.owner", "n_o",   true,  true);
                    qb.join("n.details.group", "n_g",   true,  true);
                    qb.where();
                    qb.and("i.id = "+ id);
                    qb.query(session);
                }
            }
        }
        session.clear();
        
        IceMapper mapper = new IceMapper();
        for (Image image : lookups.keySet()) {
            Image replacement = (Image) mapper.map(new ProxyCleanupFilter().filter("", lookups.get(image)));
            replacements.put(image, replacement);
        }
        
        List<Image> newImages = new ArrayList<Image>();
        for (int i = 0; i < imageList.size(); i++) {
            Image image = imageList.get(i);
            Image replacement = replacements.get(image);
            if (seenLSIDs.add(handleLsid(replacement))) {
                if (replacement != null) {
                    newImages.add(replacement);
                    initializeAnnotations(replacement.linkedAnnotationList());
                    initializeRois(replacement);
                } else {
                    newImages.add(image);
                    initializeAnnotations(image.linkedAnnotationList());
                    initializeRois(image);
                }
            }
        }
        this.imageList.clear();
        this.imageList.addAll(newImages);

    }

    private void initializeAnnotations(Iterable<Annotation> annotations)
    {
        for (Annotation annotation : annotations)
        {
            if (!seenLSIDs.add(handleLsid(annotation))) {
                /* already included this annotation in a list */
                continue;
            }
            if (annotation instanceof XmlAnnotation)
            {
                xmlAnnotationList.add((XmlAnnotation) annotation);
            }
            else if (annotation instanceof LongAnnotation)
            {
                longAnnotationList.add((LongAnnotation) annotation);
            }
            else if (annotation instanceof BooleanAnnotation)
            {
                booleanAnnotationList.add((BooleanAnnotation) annotation);
            }
            else if (annotation instanceof DoubleAnnotation)
            {
                doubleAnnotationList.add((DoubleAnnotation) annotation);
            }
            else if (annotation instanceof CommentAnnotation)
            {
                commentAnnotationList.add((CommentAnnotation) annotation);
            }
            else if (annotation instanceof TimestampAnnotation)
            {
                timestampAnnotationList.add((TimestampAnnotation) annotation);
            }
            else if (annotation instanceof TagAnnotation)
            {
                tagAnnotationList.add((TagAnnotation) annotation);
            }
            else if (annotation instanceof TermAnnotation)
            {
                termAnnotationList.add((TermAnnotation) annotation);
            }
            else
            {
                log.warn(String.format(
                        "Unhandled annotation of type '%s' ID:%d",
                        annotation.getClass(), annotation.getId().getValue()));
            }
        }
    }

    private void initializeRois(Image image) {
        for (final Roi roi : image.copyRois()) {
            if (seenLSIDs.add(handleLsid(roi))) {
                roiList.add(roi);
                initializeAnnotations(roi.linkedAnnotationList());
                for (final Shape shape : roi.copyShapes()) {
                    initializeAnnotations(shape.linkedAnnotationList());
                }
            }
        }
    }

    private Image _getImage(int imageIndex)
    {
        try
        {
            return imageList.get(imageIndex);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            return null;
        }
    }

    private <X extends Shape> X getShape(int ROIIndex, int shapeIndex, Class<X> expectedSubclass) {
        if (ROIIndex < 0 || shapeIndex < 0 || ROIIndex >= roiList.size()) {
            return null;
        }
        final Roi roi = roiList.get(ROIIndex);
        final List<Shape> shapes = roi.copyShapes();
        if (shapeIndex >= shapes.size()) {
            return null;
        }
        final Shape shape = shapes.get(shapeIndex);
        if (!expectedSubclass.isAssignableFrom(shape.getClass())) {
            return null;
        }
        return expectedSubclass.cast(shape);
    }

    private static Boolean fromRType(RBool v)
    {
        return v == null? null : v.getValue();
    }

    private static String fromRType(RString v)
    {
        return v == null? null : v.getValue();
    }

    private static Double fromRType(RDouble v)
    {
        return v == null? null : v.getValue();
    }

    private static Time fromRType(omero.model.Time v)
    {
        if (v == null) return null;
        return convertTime(v);
    }

    private static Integer fromRType(RInt v)
    {
        return v == null? null : v.getValue();
    }

    private static Long fromRType(RLong v)
    {
        return v == null? null : v.getValue();
    }

    private static PositiveInteger toPositiveInteger(RInt v)
    {
        try
        {
            Integer asInt = fromRType(v);
            return asInt != null? new PositiveInteger(asInt) : null;
        }
        catch (IllegalArgumentException e)
        {
            log.warn("Using new PositiveInteger(1)!", e);
            return new PositiveInteger(1);
        }
    }

    private static NonNegativeInteger toNonNegativeInteger(RInt v)
    {
        try
        {
            Integer asInt = fromRType(v);
            return asInt != null? new NonNegativeInteger(asInt) : null;
        }
        catch (IllegalArgumentException e)
        {
            log.warn("Using new NonNegativeInteger(0)!", e);
            return new NonNegativeInteger(0);
        }
    }

    private static AffineTransform toTransform(RString v) {
        final String transformString = fromRType(v);
        if (transformString == null) {
            return null;
        }
        final Matcher transformMatcher = AFFINE_TRANSFORM.matcher(transformString);
        if (!transformMatcher.matches()) {
            return null;
        }
        final AffineTransform transform = new AffineTransform();
        try {
            transform.setA00(Double.valueOf(transformMatcher.group(1)));
            transform.setA01(Double.valueOf(transformMatcher.group(2)));
            transform.setA02(Double.valueOf(transformMatcher.group(3)));
            transform.setA10(Double.valueOf(transformMatcher.group(4)));
            transform.setA11(Double.valueOf(transformMatcher.group(5)));
            transform.setA12(Double.valueOf(transformMatcher.group(6)));
            return transform;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /* IMAGE */

    @Override
    public Timestamp getImageAcquisitionDate(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        if (o != null) {
            final RTime acquisitionDate = o.getAcquisitionDate();
            if (acquisitionDate != null) {
                return new Timestamp(new Instant(acquisitionDate.getValue()));
            }
        }
        return null;
    }

    @Override
    public String getImageAnnotationRef(int imageIndex, int annotationRefIndex)
    {
        Image o = _getImage(imageIndex);
        if (o == null)
        {
            return null;
        }
        try
        {
            Annotation annotation = 
                o.linkedAnnotationList().get(annotationRefIndex);
            return handleLsid(annotation);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            return null;
        }
    }

    @Override
    public int getImageAnnotationRefCount(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        if (o == null)
        {
            return -1;
        }
        return o.sizeOfAnnotationLinks();
    }

    @Override
    public int getImageCount()
    {
        return imageList.size();
    }

    @Override
    public String getImageDescription(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? fromRType(o.getDescription()) : null;
    }

    @Override
    public String getImageID(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? handleLsid(o) : null;
    }

    @Override
    public String getImageName(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? fromRType(o.getName()) : null;
    }

    @Override
    public String getImageROIRef(int imageIndex, int ROIRefIndex) {
        if (imageIndex < 0 || ROIRefIndex < 0 || imageIndex >= imageList.size()) {
            return null;
        }
        final Image image = imageList.get(imageIndex);
        final List<Roi> rois = image.copyRois();
        if (ROIRefIndex >= rois.size()) {
            return null;
        }
        final Roi roi = rois.get(ROIRefIndex);
        return handleLsid(roi);
    }

    @Override
    public int getImageROIRefCount(int imageIndex) {
        if (imageIndex < 0 || imageIndex >= imageList.size()) {
            return -1;
        }
        final Image image = imageList.get(imageIndex);
        return image.sizeOfRois();
    }

    @Override
    public Boolean getPixelsBinDataBigEndian(int imageIndex, int binDataIndex)
    {
        return true;
    }

    @Override
    public DimensionOrder getPixelsDimensionOrder(int imageIndex)
    {
        return DimensionOrder.XYZCT;
    }

    @Override
    public String getPixelsID(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? handleLsid(o.getPrimaryPixels()) : null;
    }

    @Override
    public ome.units.quantity.Length getPixelsPhysicalSizeX(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? convertLength(
                o.getPrimaryPixels().getPhysicalSizeX()) : null;
    }

    @Override
    public ome.units.quantity.Length getPixelsPhysicalSizeY(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? convertLength(
                o.getPrimaryPixels().getPhysicalSizeY()) : null;
    }

    @Override
    public ome.units.quantity.Length getPixelsPhysicalSizeZ(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? convertLength(
                o.getPrimaryPixels().getPhysicalSizeZ()) : null;
    }

    @Override
    public PositiveInteger getPixelsSizeC(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? toPositiveInteger(
                o.getPrimaryPixels().getSizeC()) : null;
    }

    @Override
    public PositiveInteger getPixelsSizeT(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? toPositiveInteger(
                o.getPrimaryPixels().getSizeT()) : null;
    }

    @Override
    public PositiveInteger getPixelsSizeX(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? toPositiveInteger(
                o.getPrimaryPixels().getSizeX()) : null;
    }

    @Override
    public PositiveInteger getPixelsSizeY(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? toPositiveInteger(
                o.getPrimaryPixels().getSizeY()) : null;
    }

    @Override
    public PositiveInteger getPixelsSizeZ(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? toPositiveInteger(
                o.getPrimaryPixels().getSizeZ()) : null;
    }

    @Override
    public Time getPixelsTimeIncrement(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? fromRType(
                o.getPrimaryPixels().getTimeIncrement()) : null;
    }

    @Override
    public PixelType getPixelsType(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        if (o == null)
        {
            return null;
        }
        omero.model.PixelsType e = o.getPrimaryPixels().getPixelsType();
        try
        {
            return e != null? 
                    PixelType.fromString(fromRType(e.getValue()))
                    : null;
        }
        catch (EnumerationException ex)
        {
            log.error("Unable to map enumeration.", ex);
            return null;
        }
    }

    private Channel getChannel(int imageIndex, int channelIndex)
    {
        Image i = _getImage(imageIndex);
        if (i == null)
        {
            return null;
        }
        Pixels p = i.getPrimaryPixels();
        if (p == null)
        {
            return null;
        }
        try
        {
            return p.getChannel(channelIndex);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            return null;
        }
    }
    
    @Override
    public AcquisitionMode getChannelAcquisitionMode(int imageIndex,
            int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        if (o == null)
        {
            return null;
        }
        omero.model.AcquisitionMode e = o.getLogicalChannel().getMode();
        try
        {
            return e != null? 
                    AcquisitionMode.fromString(fromRType(e.getValue()))
                    : null;
        }
        catch (EnumerationException ex)
        {
            log.error("Unable to map enumeration.", ex);
            return null;
        }
    }

    @Override
    public Color getChannelColor(int imageIndex, int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        if (o == null)
        {
            return null;
        }
        try
        {
            return new Color(
                    fromRType(o.getRed()), fromRType(o.getGreen()),
                    fromRType(o.getBlue()), fromRType(o.getAlpha()));
        }
        catch (NullPointerException e)
        {
            return null;
        }
    }

    @Override
    public ContrastMethod getChannelContrastMethod(int imageIndex,
            int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        if (o == null)
        {
            return null;
        }
        omero.model.ContrastMethod e = o.getLogicalChannel().getContrastMethod();
        try
        {
            return e != null? 
                    ContrastMethod.fromString(fromRType(e.getValue()))
                    : null;
        }
        catch (EnumerationException ex)
        {
            log.error("Unable to map enumeration.", ex);
            return null;
        }
    }

    @Override
    public int getChannelCount(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        if (o == null)
        {
            return -1;
        }
        return o.getPrimaryPixels().sizeOfChannels();
    }

    @Override
    public ome.units.quantity.Length getChannelEmissionWavelength(int imageIndex,
            int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        return convertLength(o.getLogicalChannel().getEmissionWave());
    }

    @Override
    public ome.units.quantity.Length getChannelExcitationWavelength(int imageIndex,
            int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        return convertLength(o.getLogicalChannel().getExcitationWave());
    }

    @Override
    public String getChannelFluor(int imageIndex, int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        return o != null? fromRType(o.getLogicalChannel().getFluor()) : null;
    }

    @Override
    public String getChannelID(int imageIndex, int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        return o != null? handleLsid(o) : null;
    }

    @Override
    public IlluminationType getChannelIlluminationType(int imageIndex,
            int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        if (o == null)
        {
            return null;
        }
        omero.model.Illumination e = o.getLogicalChannel().getIllumination();
        try
        {
            return e != null? 
                    IlluminationType.fromString(fromRType(e.getValue()))
                    : null;
        }
        catch (EnumerationException ex)
        {
            log.error("Unable to map enumeration.", ex);
            return null;
        }
    }

    @Override
    public String getChannelName(int imageIndex, int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        return o != null? fromRType(o.getLogicalChannel().getName()) : null;
    }

    @Override
    public Double getChannelNDFilter(int imageIndex, int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        return o != null? fromRType(o.getLogicalChannel().getNdFilter()) : null;
    }

    @Override
    public ome.units.quantity.Length getChannelPinholeSize(int imageIndex, int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        return o != null? convertLength(
                o.getLogicalChannel().getPinHoleSize()) : null;
    }

    @Override
    public Integer getChannelPockelCellSetting(int imageIndex, int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        return o != null? fromRType(
                o.getLogicalChannel().getPockelCellSetting()) : null;
    }

    @Override
    public PositiveInteger getChannelSamplesPerPixel(int imageIndex,
            int channelIndex)
    {
        return new PositiveInteger(1);
    }

    private PlaneInfo getPlane(int imageIndex, int planeIndex)
    {
        Image i = _getImage(imageIndex);
        if (i == null)
        {
            return null;
        }
        Pixels p = i.getPrimaryPixels();
        if (p == null)
        {
            return null;
        }
        try
        {
            return p.copyPlaneInfo().get(planeIndex);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            return null;
        }
    }

    @Override
    public int getPlaneCount(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o == null? 0 : o.getPrimaryPixels().sizeOfPlaneInfo();
    }

    @Override
    public Time getPlaneDeltaT(int imageIndex, int planeIndex)
    {
        PlaneInfo o = getPlane(imageIndex, planeIndex);
        return o != null? fromRType(o.getDeltaT()) : null;
    }

    @Override
    public Time getPlaneExposureTime(int imageIndex, int planeIndex)
    {
        PlaneInfo o = getPlane(imageIndex, planeIndex);
        return o != null? fromRType(o.getExposureTime()) : null;
    }

    @Override
    public ome.units.quantity.Length getPlanePositionX(int imageIndex, int planeIndex)
    {
        PlaneInfo o = getPlane(imageIndex, planeIndex);
        return o != null? convertLength(o.getPositionX()) : null;
    }

    @Override
    public ome.units.quantity.Length getPlanePositionY(int imageIndex, int planeIndex)
    {
        PlaneInfo o = getPlane(imageIndex, planeIndex);
        return o != null? convertLength(o.getPositionY()) : null;
    }

    @Override
    public ome.units.quantity.Length getPlanePositionZ(int imageIndex, int planeIndex)
    {
        PlaneInfo o = getPlane(imageIndex, planeIndex);
        return o != null? convertLength(o.getPositionZ()) : null;
    }

    @Override
    public NonNegativeInteger getPlaneTheC(int imageIndex, int planeIndex)
    {
        PlaneInfo o = getPlane(imageIndex, planeIndex);
        return toNonNegativeInteger(o.getTheC());
    }

    @Override
    public NonNegativeInteger getPlaneTheT(int imageIndex, int planeIndex)
    {
        PlaneInfo o = getPlane(imageIndex, planeIndex);
        return toNonNegativeInteger(o.getTheT());
    }

    @Override
    public NonNegativeInteger getPlaneTheZ(int imageIndex, int planeIndex)
    {
        PlaneInfo o = getPlane(imageIndex, planeIndex);
        return toNonNegativeInteger(o.getTheZ());
    }

    /* ANNOTATION */

    private <T extends Annotation> T getAnnotation(Class<T> klass, int index)
    {
        try
        {
            if (klass.equals(XmlAnnotation.class))
            {
                return (T) xmlAnnotationList.get(index);
            }
            else if (klass.equals(LongAnnotation.class))
            {
                return (T) longAnnotationList.get(index);
            }
            else if (klass.equals(BooleanAnnotation.class))
            {
                return (T) booleanAnnotationList.get(index);
            }
            else if (klass.equals(DoubleAnnotation.class))
            {
                return (T) doubleAnnotationList.get(index);
            }
            else if (klass.equals(CommentAnnotation.class))
            {
                return (T) commentAnnotationList.get(index);
            }
            else if (klass.equals(TimestampAnnotation.class))
            {
                return (T) timestampAnnotationList.get(index);
            }
            else if (klass.equals(TagAnnotation.class))
            {
                return (T) tagAnnotationList.get(index);
            }
            else if (klass.equals(TermAnnotation.class))
            {
                return (T) termAnnotationList.get(index);
            }
            else
            {
                log.warn(String.format(
                        "Unhandled annotation of type '%s' index:%d",
                        klass, index));
                return null;
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            return null;
        }
    }

    private String getAnnotationDescription(
            Class<? extends Annotation> klass, int index)
    {
        Annotation o = getAnnotation(klass, index);
        return o != null? fromRType(o.getDescription()) : null;
    }

    private String getAnnotationID(
            Class<? extends Annotation> klass, int index)
    {
        Annotation o = getAnnotation(klass, index);
        return o != null? handleLsid(o) : null;
    }

    private String getAnnotationNamespace(
            Class<? extends Annotation> klass, int index)
    {
        Annotation o = getAnnotation(klass, index);
        return o != null? fromRType(o.getNs()) : null;
    }

    @Override
    public int getXMLAnnotationCount()
    {
        return xmlAnnotationList.size();
    }

    @Override
    public String getXMLAnnotationDescription(int XMLAnnotationIndex)
    {
        return getAnnotationDescription(
                XmlAnnotation.class, XMLAnnotationIndex);
    }

    @Override
    public String getXMLAnnotationID(int XMLAnnotationIndex)
    {
        return getAnnotationID(XmlAnnotation.class, XMLAnnotationIndex);
    }

    @Override
    public String getXMLAnnotationNamespace(int XMLAnnotationIndex)
    {
        return getAnnotationNamespace(XmlAnnotation.class, XMLAnnotationIndex);
    }

    @Override
    public String getXMLAnnotationValue(int XMLAnnotationIndex)
    {
        XmlAnnotation o = getAnnotation(
                XmlAnnotation.class, XMLAnnotationIndex);
        return o != null? fromRType(o.getTextValue()) : null;
    }

    @Override
    public int getLongAnnotationCount()
    {
        return longAnnotationList.size();
    }

    @Override
    public String getLongAnnotationDescription(int longAnnotationIndex)
    {
        return getAnnotationDescription(
                LongAnnotation.class, longAnnotationIndex);
    }

    @Override
    public String getLongAnnotationID(int longAnnotationIndex)
    {
        return getAnnotationID(LongAnnotation.class, longAnnotationIndex);
    }

    @Override
    public String getLongAnnotationNamespace(int longAnnotationIndex)
    {
        return getAnnotationNamespace(
                LongAnnotation.class, longAnnotationIndex);
    }

    @Override
    public Long getLongAnnotationValue(int longAnnotationIndex)
    {
        LongAnnotation o = getAnnotation(
                LongAnnotation.class, longAnnotationIndex);
        return o != null? fromRType(o.getLongValue()) : null;
    }

    @Override
    public int getBooleanAnnotationCount()
    {
        return booleanAnnotationList.size();
    }

    @Override
    public String getBooleanAnnotationDescription(int booleanAnnotationIndex)
    {
        return getAnnotationDescription(
                BooleanAnnotation.class, booleanAnnotationIndex);
    }

    @Override
    public String getBooleanAnnotationID(int booleanAnnotationIndex)
    {
        return getAnnotationID(BooleanAnnotation.class, booleanAnnotationIndex);
    }

    @Override
    public String getBooleanAnnotationNamespace(int booleanAnnotationIndex)
    {
        return getAnnotationNamespace(
                BooleanAnnotation.class, booleanAnnotationIndex);
    }

    @Override
    public Boolean getBooleanAnnotationValue(int booleanAnnotationIndex)
    {
        BooleanAnnotation o = getAnnotation(
                BooleanAnnotation.class, booleanAnnotationIndex);
        return o != null? fromRType(o.getBoolValue()) : null;
    }

    @Override
    public int getDoubleAnnotationCount()
    {
        return doubleAnnotationList.size();
    }

    @Override
    public String getDoubleAnnotationDescription(int doubleAnnotationIndex)
    {
        return getAnnotationDescription(
                DoubleAnnotation.class, doubleAnnotationIndex);
    }

    @Override
    public String getDoubleAnnotationID(int doubleAnnotationIndex)
    {
        return getAnnotationID(DoubleAnnotation.class, doubleAnnotationIndex);
    }

    @Override
    public String getDoubleAnnotationNamespace(int doubleAnnotationIndex)
    {
        return getAnnotationNamespace(
                DoubleAnnotation.class, doubleAnnotationIndex);
    }

    @Override
    public Double getDoubleAnnotationValue(int doubleAnnotationIndex)
    {
        DoubleAnnotation o = getAnnotation(
                DoubleAnnotation.class, doubleAnnotationIndex);
        return o != null? fromRType(o.getDoubleValue()) : null;
    }

    @Override
    public int getCommentAnnotationCount()
    {
        return commentAnnotationList.size();
    }

    @Override
    public String getCommentAnnotationDescription(int commentAnnotationIndex)
    {
        return getAnnotationDescription(
                CommentAnnotation.class, commentAnnotationIndex);
    }

    @Override
    public String getCommentAnnotationID(int commentAnnotationIndex)
    {
        return getAnnotationID(CommentAnnotation.class, commentAnnotationIndex);
    }

    @Override
    public String getCommentAnnotationNamespace(int commentAnnotationIndex)
    {
        return getAnnotationNamespace(
                CommentAnnotation.class, commentAnnotationIndex);
    }

    @Override
    public String getCommentAnnotationValue(int commentAnnotationIndex)
    {
        CommentAnnotation o = getAnnotation(
                CommentAnnotation.class, commentAnnotationIndex);
        return o != null? fromRType(o.getTextValue()) : null;
    }

    @Override
    public int getTimestampAnnotationCount()
    {
        return timestampAnnotationList.size();
    }

    @Override
    public String getTimestampAnnotationDescription(int timestampAnnotationIndex)
    {
        return getAnnotationDescription(
                TimestampAnnotation.class, timestampAnnotationIndex);
    }

    @Override
    public String getTimestampAnnotationID(int timestampAnnotationIndex)
    {
        return getAnnotationID(
                TimestampAnnotation.class, timestampAnnotationIndex);
    }

    @Override
    public String getTimestampAnnotationNamespace(int timestampAnnotationIndex)
    {
        return getAnnotationNamespace(
                TimestampAnnotation.class, timestampAnnotationIndex);
    }

    @Override
    public Timestamp getTimestampAnnotationValue(int timestampAnnotationIndex)
    {
        TimestampAnnotation o = getAnnotation(
                TimestampAnnotation.class, timestampAnnotationIndex);
        return o != null? new Timestamp(new Instant(o.getTimeValue().getValue())) : null;
    }

    @Override
    public int getTagAnnotationCount()
    {
        return tagAnnotationList.size();
    }

    @Override
    public String getTagAnnotationDescription(int tagAnnotationIndex)
    {
        return getAnnotationDescription(
                TagAnnotation.class, tagAnnotationIndex);
    }

    @Override
    public String getTagAnnotationID(int tagAnnotationIndex)
    {
        return getAnnotationID(TagAnnotation.class, tagAnnotationIndex);
    }

    @Override
    public String getTagAnnotationNamespace(int tagAnnotationIndex)
    {
        return getAnnotationNamespace(TagAnnotation.class, tagAnnotationIndex);
    }

    @Override
    public String getTagAnnotationValue(int tagAnnotationIndex)
    {
        TagAnnotation o = getAnnotation(
                TagAnnotation.class, tagAnnotationIndex);
        return o != null? fromRType(o.getTextValue()) : null;    }

    @Override
    public int getTermAnnotationCount()
    {
        return termAnnotationList.size();
    }

    @Override
    public String getTermAnnotationDescription(int termAnnotationIndex)
    {
        return getAnnotationDescription(
                TermAnnotation.class, termAnnotationIndex);
    }

    @Override
    public String getTermAnnotationID(int termAnnotationIndex)
    {
        return getAnnotationID(TermAnnotation.class, termAnnotationIndex);
    }

    @Override
    public String getTermAnnotationNamespace(int termAnnotationIndex)
    {
        return getAnnotationNamespace(
                TermAnnotation.class, termAnnotationIndex);
    }

    @Override
    public String getTermAnnotationValue(int termAnnotationIndex)
    {
        TermAnnotation o = getAnnotation(
                TermAnnotation.class, termAnnotationIndex);
        return o != null? fromRType(o.getTermValue()) : null;
    }

    /* ROI */

    @Override
    public int getROICount() {
        return roiList.size();
    }

    @Override
    public String getROIAnnotationRef(int ROIIndex, int annotationRefIndex) {
        if (ROIIndex < 0 || annotationRefIndex < 0 || ROIIndex >= roiList.size()) {
            return null;
        }
        final Roi roi = roiList.get(ROIIndex);
        final List<Annotation> annotations = roi.linkedAnnotationList();
        if (annotationRefIndex >= annotations.size()) {
            return null;
        }
        final Annotation annotation = annotations.get(annotationRefIndex);
        return handleLsid(annotation);
    }

    @Override
    public int getROIAnnotationRefCount(int ROIIndex) {
        if (ROIIndex < 0 || ROIIndex >= roiList.size()) {
            return -1;
        }
        final Roi roi = roiList.get(ROIIndex);
        return roi.sizeOfAnnotationLinks();
    }

    @Override
    public String getROIDescription(int ROIIndex) {
        if (ROIIndex < 0 || ROIIndex >= roiList.size()) {
            return null;
        }
        final Roi roi = roiList.get(ROIIndex);
        return fromRType(roi.getDescription());
    }

    @Override
    public String getROIID(int ROIIndex) {
        if (ROIIndex < 0 || ROIIndex >= roiList.size()) {
            return null;
        }
        final Roi roi = roiList.get(ROIIndex);
        return handleLsid(roi);
    }

    @Override
    public String getROIName(int ROIIndex) {
        if (ROIIndex < 0 || ROIIndex >= roiList.size()) {
            return null;
        }
        final Roi roi = roiList.get(ROIIndex);
        return fromRType(roi.getName());
    }

    @Override
    public String getROINamespace(int ROIIndex) {
        if (ROIIndex < 0 || ROIIndex >= roiList.size()) {
            return null;
        }
        final Roi roi = roiList.get(ROIIndex);
        final String[] namespaces = roi.getNamespaces();
        if (ArrayUtils.isEmpty(namespaces)) {
            return null;
        }
        return namespaces[0];
    }

    @Override
    public int getShapeCount(int ROIIndex) {
        if (ROIIndex < 0 || ROIIndex >= roiList.size()) {
            return -1;
        }
        final Roi roi = roiList.get(ROIIndex);
        return roi.sizeOfShapes();
    }

    @Override
    public String getShapeType(int ROIIndex, int shapeIndex) {
        final Shape shape = getShape(ROIIndex, shapeIndex, Shape.class);
        if (shape == null) {
            return null;
        }
        Class<? extends Shape> shapeClass = null;
        Class<? extends Shape> currentClass = shape.getClass();
        while (currentClass != Shape.class) {
            shapeClass = currentClass;
            currentClass = currentClass.getSuperclass().asSubclass(Shape.class);
        }
        if (shapeClass == Rect.class) {
            return "Rectangle";
        } else {
            return shapeClass.getSimpleName();
        }
    }

    @Override
    public int getShapeAnnotationRefCount(int ROIIndex, int shapeIndex) {
        final Shape shape = getShape(ROIIndex, shapeIndex, Shape.class);
        if (shape == null) {
            return -1;
        }
        return shape.sizeOfAnnotationLinks();
    }

    private <X extends Shape> String getShapeAnnotationRef(int ROIIndex, int shapeIndex, int annotationRefIndex,
            Class<X> expectedSubclass) {
        if (annotationRefIndex < 0) {
            return null;
        }
        final X shape = getShape(ROIIndex, shapeIndex, expectedSubclass);
        if (shape == null) {
            return null;
        }
        final List<Annotation> annotations = shape.linkedAnnotationList();
        if (annotationRefIndex >= annotations.size()) {
            return null;
        }
        final Annotation annotation = annotations.get(annotationRefIndex);
        return handleLsid(annotation);
    }

    private <X extends Shape> Color getShapeFillColor(int ROIIndex, int shapeIndex, Class<X> expectedSubclass) {
        final X shape = getShape(ROIIndex, shapeIndex, expectedSubclass);
        if (shape == null) {
            return null;
        }
        final Integer color = fromRType(shape.getFillColor());
        if (color == null) {
            return null;
        }
        return new Color(color);
    }

    private <X extends Shape> FillRule getShapeFillRule(int ROIIndex, int shapeIndex, Class<X> expectedSubclass) {
        final X shape = getShape(ROIIndex, shapeIndex, expectedSubclass);
        if (shape == null) {
            return null;
        }
        final String fillRuleName = fromRType(shape.getFillRule());
        if (fillRuleName == null) {
            return null;
        }
        final FillRule fillRule;
        try {
            fillRule = FillRule.fromString(fillRuleName);
        } catch (EnumerationException e) {
            return null;
        }
        return fillRule;
    }

    private <X extends Shape> FontFamily getShapeFontFamily(int ROIIndex, int shapeIndex, Class<X> expectedSubclass) {
        final X shape = getShape(ROIIndex, shapeIndex, expectedSubclass);
        if (shape == null) {
            return null;
        }
        final String fontFamilyName = fromRType(shape.getFontFamily());
        if (fontFamilyName == null) {
            return null;
        }
        final FontFamily fontFamily;
        try {
            fontFamily = FontFamily.fromString(fontFamilyName);
        } catch (EnumerationException e) {
            return null;
        }
        return fontFamily;
    }

    private <X extends Shape> Length getShapeFontSize(int ROIIndex, int shapeIndex, Class<X> expectedSubclass) {
        final X shape = getShape(ROIIndex, shapeIndex, expectedSubclass);
        if (shape == null) {
            return null;
        }
        return convertLength(shape.getFontSize());
    }

    private <X extends Shape> FontStyle getShapeFontStyle(int ROIIndex, int shapeIndex, Class<X> expectedSubclass) {
        final X shape = getShape(ROIIndex, shapeIndex, expectedSubclass);
        if (shape == null) {
            return null;
        }
        final String fontStyleName = fromRType(shape.getFontStyle());
        if (fontStyleName == null) {
            return null;
        }
        final FontStyle fontStyle;
        try {
            fontStyle = FontStyle.fromString(fontStyleName);
        } catch (EnumerationException e) {
            return null;
        }
        return fontStyle;
    }

    private <X extends Shape> String getShapeID(int ROIIndex, int shapeIndex, Class<X> expectedSubclass) {
        final X shape = getShape(ROIIndex, shapeIndex, expectedSubclass);
        if (shape == null) {
            return null;
        }
        return handleLsid(shape);
    }

    private <X extends Shape> LineCap getShapeLineCap(int ROIIndex, int shapeIndex, Class<X> expectedSubclass) {
        final X shape = getShape(ROIIndex, shapeIndex, expectedSubclass);
        if (shape == null) {
            return null;
        }
        final String lineCapName = fromRType(shape.getStrokeLineCap());
        if (lineCapName == null) {
            return null;
        }
        final LineCap lineCap;
        try {
            lineCap = LineCap.fromString(lineCapName);
        } catch (EnumerationException e) {
            return null;
        }
        return lineCap;
    }

    private <X extends Shape> Boolean getShapeLocked(int ROIIndex, int shapeIndex, Class<X> expectedSubclass) {
        final X shape = getShape(ROIIndex, shapeIndex, expectedSubclass);
        if (shape == null) {
            return null;
        }
        return fromRType(shape.getLocked());
    }

    private <X extends Shape> Color getShapeStrokeColor(int ROIIndex, int shapeIndex, Class<X> expectedSubclass) {
        final X shape = getShape(ROIIndex, shapeIndex, expectedSubclass);
        if (shape == null) {
            return null;
        }
        final Integer color = fromRType(shape.getStrokeColor());
        if (color == null) {
            return null;
        }
        return new Color(color);
    }

    private <X extends Shape> String getShapeStrokeDashArray(int ROIIndex, int shapeIndex, Class<X> expectedSubclass) {
        final X shape = getShape(ROIIndex, shapeIndex, expectedSubclass);
        if (shape == null) {
            return null;
        }
        return fromRType(shape.getStrokeDashArray());
    }

    private <X extends Shape> Length getShapeStrokeWidth(int ROIIndex, int shapeIndex, Class<X> expectedSubclass) {
        final X shape = getShape(ROIIndex, shapeIndex, expectedSubclass);
        if (shape == null) {
            return null;
        }
        return convertLength(shape.getStrokeWidth());
    }

    private <X extends Shape> NonNegativeInteger getShapeTheC(int ROIIndex, int shapeIndex, Class<X> expectedSubclass) {
        final X shape = getShape(ROIIndex, shapeIndex, expectedSubclass);
        if (shape == null) {
            return null;
        }
        return toNonNegativeInteger(shape.getTheC());
    }

    private <X extends Shape> NonNegativeInteger getShapeTheT(int ROIIndex, int shapeIndex, Class<X> expectedSubclass) {
        final X shape = getShape(ROIIndex, shapeIndex, expectedSubclass);
        if (shape == null) {
            return null;
        }
        return toNonNegativeInteger(shape.getTheT());
    }

    private <X extends Shape> NonNegativeInteger getShapeTheZ(int ROIIndex, int shapeIndex, Class<X> expectedSubclass) {
        final X shape = getShape(ROIIndex, shapeIndex, expectedSubclass);
        if (shape == null) {
            return null;
        }
        return toNonNegativeInteger(shape.getTheZ());
    }

    private <X extends Shape> AffineTransform getShapeTransform(int ROIIndex, int shapeIndex, Class<X> expectedSubclass) {
        final X shape = getShape(ROIIndex, shapeIndex, expectedSubclass);
        if (shape == null) {
            return null;
        }
        return toTransform(shape.getTransform());
    }

    private <X extends Shape> Boolean getShapeVisible(int ROIIndex, int shapeIndex, Class<X> expectedSubclass) {
        final X shape = getShape(ROIIndex, shapeIndex, expectedSubclass);
        if (shape == null) {
            return null;
        }
        return fromRType(shape.getVisibility());
    }

    @Override
    public String getEllipseAnnotationRef(int ROIIndex, int shapeIndex, int annotationRefIndex) {
        return getShapeAnnotationRef(ROIIndex, shapeIndex, annotationRefIndex, Ellipse.class);
    }

    @Override
    public Color getEllipseFillColor(int ROIIndex, int shapeIndex) {
        return getShapeFillColor(ROIIndex, shapeIndex, Ellipse.class);
    }

    @Override
    public FillRule getEllipseFillRule(int ROIIndex, int shapeIndex) {
        return getShapeFillRule(ROIIndex, shapeIndex, Ellipse.class);
    }

    @Override
    public FontFamily getEllipseFontFamily(int ROIIndex, int shapeIndex) {
        return getShapeFontFamily(ROIIndex, shapeIndex, Ellipse.class);
    }

    @Override
    public Length getEllipseFontSize(int ROIIndex, int shapeIndex) {
        return getShapeFontSize(ROIIndex, shapeIndex, Ellipse.class);
    }

    @Override
    public FontStyle getEllipseFontStyle(int ROIIndex, int shapeIndex) {
        return getShapeFontStyle(ROIIndex, shapeIndex, Ellipse.class);
    }

    @Override
    public String getEllipseID(int ROIIndex, int shapeIndex) {
        return getShapeID(ROIIndex, shapeIndex, Ellipse.class);
    }

    @Override
    public LineCap getEllipseLineCap(int ROIIndex, int shapeIndex) {
        return getShapeLineCap(ROIIndex, shapeIndex, Ellipse.class);
    }

    @Override
    public Boolean getEllipseLocked(int ROIIndex, int shapeIndex) {
        return getShapeLocked(ROIIndex, shapeIndex, Ellipse.class);
    }

    @Override
    public Color getEllipseStrokeColor(int ROIIndex, int shapeIndex) {
        return getShapeStrokeColor(ROIIndex, shapeIndex, Ellipse.class);
    }

    @Override
    public String getEllipseStrokeDashArray(int ROIIndex, int shapeIndex) {
        return getShapeStrokeDashArray(ROIIndex, shapeIndex, Ellipse.class);
    }

    @Override
    public Length getEllipseStrokeWidth(int ROIIndex, int shapeIndex) {
        return getShapeStrokeWidth(ROIIndex, shapeIndex, Ellipse.class);
    }

    @Override
    public NonNegativeInteger getEllipseTheC(int ROIIndex, int shapeIndex) {
        return getShapeTheC(ROIIndex, shapeIndex, Ellipse.class);
    }

    @Override
    public NonNegativeInteger getEllipseTheT(int ROIIndex, int shapeIndex) {
        return getShapeTheT(ROIIndex, shapeIndex, Ellipse.class);
    }

    @Override
    public NonNegativeInteger getEllipseTheZ(int ROIIndex, int shapeIndex) {
        return getShapeTheZ(ROIIndex, shapeIndex, Ellipse.class);
    }

    @Override
    public AffineTransform getEllipseTransform(int ROIIndex, int shapeIndex) {
        return getShapeTransform(ROIIndex, shapeIndex, Ellipse.class);
    }

    @Override
    public Boolean getEllipseVisible(int ROIIndex, int shapeIndex) {
        return getShapeVisible(ROIIndex, shapeIndex, Ellipse.class);
    }

    @Override
    public Double getEllipseRadiusX(int ROIIndex, int shapeIndex) {
        final Ellipse ellipse = getShape(ROIIndex, shapeIndex, Ellipse.class);
        if (ellipse == null) {
            return null;
        }
        return fromRType(ellipse.getRx());
    }

    @Override
    public Double getEllipseRadiusY(int ROIIndex, int shapeIndex) {
        final Ellipse ellipse = getShape(ROIIndex, shapeIndex, Ellipse.class);
        if (ellipse == null) {
            return null;
        }
        return fromRType(ellipse.getRy());
    }

    @Override
    public String getEllipseText(int ROIIndex, int shapeIndex) {
        final Ellipse ellipse = getShape(ROIIndex, shapeIndex, Ellipse.class);
        if (ellipse == null) {
            return null;
        }
        return fromRType(ellipse.getTextValue());
    }

    @Override
    public Double getEllipseX(int ROIIndex, int shapeIndex) {
        final Ellipse ellipse = getShape(ROIIndex, shapeIndex, Ellipse.class);
        if (ellipse == null) {
            return null;
        }
        return fromRType(ellipse.getCx());
    }

    @Override
    public Double getEllipseY(int ROIIndex, int shapeIndex) {
        final Ellipse ellipse = getShape(ROIIndex, shapeIndex, Ellipse.class);
        if (ellipse == null) {
            return null;
        }
        return fromRType(ellipse.getCy());
    }

    @Override
    public String getLabelAnnotationRef(int ROIIndex, int shapeIndex, int annotationRefIndex) {
        return getShapeAnnotationRef(ROIIndex, shapeIndex, annotationRefIndex, Label.class);
    }

    @Override
    public Color getLabelFillColor(int ROIIndex, int shapeIndex) {
        return getShapeFillColor(ROIIndex, shapeIndex, Label.class);
    }

    @Override
    public FillRule getLabelFillRule(int ROIIndex, int shapeIndex) {
        return getShapeFillRule(ROIIndex, shapeIndex, Label.class);
    }

    @Override
    public FontFamily getLabelFontFamily(int ROIIndex, int shapeIndex) {
        return getShapeFontFamily(ROIIndex, shapeIndex, Label.class);
    }

    @Override
    public Length getLabelFontSize(int ROIIndex, int shapeIndex) {
        return getShapeFontSize(ROIIndex, shapeIndex, Label.class);
    }

    @Override
    public FontStyle getLabelFontStyle(int ROIIndex, int shapeIndex) {
        return getShapeFontStyle(ROIIndex, shapeIndex, Label.class);
    }

    @Override
    public String getLabelID(int ROIIndex, int shapeIndex) {
        return getShapeID(ROIIndex, shapeIndex, Label.class);
    }

    @Override
    public LineCap getLabelLineCap(int ROIIndex, int shapeIndex) {
        return getShapeLineCap(ROIIndex, shapeIndex, Label.class);
    }

    @Override
    public Boolean getLabelLocked(int ROIIndex, int shapeIndex) {
        return getShapeLocked(ROIIndex, shapeIndex, Label.class);
    }

    @Override
    public Color getLabelStrokeColor(int ROIIndex, int shapeIndex) {
        return getShapeStrokeColor(ROIIndex, shapeIndex, Label.class);
    }

    @Override
    public String getLabelStrokeDashArray(int ROIIndex, int shapeIndex) {
        return getShapeStrokeDashArray(ROIIndex, shapeIndex, Label.class);
    }

    @Override
    public Length getLabelStrokeWidth(int ROIIndex, int shapeIndex) {
        return getShapeStrokeWidth(ROIIndex, shapeIndex, Label.class);
    }

    @Override
    public NonNegativeInteger getLabelTheC(int ROIIndex, int shapeIndex) {
        return getShapeTheC(ROIIndex, shapeIndex, Label.class);
    }

    @Override
    public NonNegativeInteger getLabelTheT(int ROIIndex, int shapeIndex) {
        return getShapeTheT(ROIIndex, shapeIndex, Label.class);
    }

    @Override
    public NonNegativeInteger getLabelTheZ(int ROIIndex, int shapeIndex) {
        return getShapeTheZ(ROIIndex, shapeIndex, Label.class);
    }

    @Override
    public AffineTransform getLabelTransform(int ROIIndex, int shapeIndex) {
        return getShapeTransform(ROIIndex, shapeIndex, Label.class);
    }

    @Override
    public Boolean getLabelVisible(int ROIIndex, int shapeIndex) {
        return getShapeVisible(ROIIndex, shapeIndex, Label.class);
    }

    @Override
    public String getLabelText(int ROIIndex, int shapeIndex) {
        final Label label = getShape(ROIIndex, shapeIndex, Label.class);
        if (label == null) {
            return null;
        }
        return fromRType(label.getTextValue());
    }

    @Override
    public Double getLabelX(int ROIIndex, int shapeIndex) {
        final Label label = getShape(ROIIndex, shapeIndex, Label.class);
        if (label == null) {
            return null;
        }
        return fromRType(label.getX());
    }

    @Override
    public Double getLabelY(int ROIIndex, int shapeIndex) {
        final Label label = getShape(ROIIndex, shapeIndex, Label.class);
        if (label == null) {
            return null;
        }
        return fromRType(label.getY());
    }

    @Override
    public String getLineAnnotationRef(int ROIIndex, int shapeIndex, int annotationRefIndex) {
        return getShapeAnnotationRef(ROIIndex, shapeIndex, annotationRefIndex, Line.class);
    }

    @Override
    public Color getLineFillColor(int ROIIndex, int shapeIndex) {
        return getShapeFillColor(ROIIndex, shapeIndex, Line.class);
    }

    @Override
    public FillRule getLineFillRule(int ROIIndex, int shapeIndex) {
        return getShapeFillRule(ROIIndex, shapeIndex, Line.class);
    }

    @Override
    public FontFamily getLineFontFamily(int ROIIndex, int shapeIndex) {
        return getShapeFontFamily(ROIIndex, shapeIndex, Line.class);
    }

    @Override
    public Length getLineFontSize(int ROIIndex, int shapeIndex) {
        return getShapeFontSize(ROIIndex, shapeIndex, Line.class);
    }

    @Override
    public FontStyle getLineFontStyle(int ROIIndex, int shapeIndex) {
        return getShapeFontStyle(ROIIndex, shapeIndex, Line.class);
    }

    @Override
    public String getLineID(int ROIIndex, int shapeIndex) {
        return getShapeID(ROIIndex, shapeIndex, Line.class);
    }

    @Override
    public LineCap getLineLineCap(int ROIIndex, int shapeIndex) {
        return getShapeLineCap(ROIIndex, shapeIndex, Line.class);
    }

    @Override
    public Boolean getLineLocked(int ROIIndex, int shapeIndex) {
        return getShapeLocked(ROIIndex, shapeIndex, Line.class);
    }

    @Override
    public Color getLineStrokeColor(int ROIIndex, int shapeIndex) {
        return getShapeStrokeColor(ROIIndex, shapeIndex, Line.class);
    }

    @Override
    public String getLineStrokeDashArray(int ROIIndex, int shapeIndex) {
        return getShapeStrokeDashArray(ROIIndex, shapeIndex, Line.class);
    }

    @Override
    public Length getLineStrokeWidth(int ROIIndex, int shapeIndex) {
        return getShapeStrokeWidth(ROIIndex, shapeIndex, Line.class);
    }

    @Override
    public NonNegativeInteger getLineTheC(int ROIIndex, int shapeIndex) {
        return getShapeTheC(ROIIndex, shapeIndex, Line.class);
    }

    @Override
    public NonNegativeInteger getLineTheT(int ROIIndex, int shapeIndex) {
        return getShapeTheT(ROIIndex, shapeIndex, Line.class);
    }

    @Override
    public NonNegativeInteger getLineTheZ(int ROIIndex, int shapeIndex) {
        return getShapeTheZ(ROIIndex, shapeIndex, Line.class);
    }

    @Override
    public AffineTransform getLineTransform(int ROIIndex, int shapeIndex) {
        return getShapeTransform(ROIIndex, shapeIndex, Line.class);
    }

    @Override
    public Boolean getLineVisible(int ROIIndex, int shapeIndex) {
        return getShapeVisible(ROIIndex, shapeIndex, Line.class);
    }

    @Override
    public String getLineText(int ROIIndex, int shapeIndex) {
        final Line line = getShape(ROIIndex, shapeIndex, Line.class);
        if (line == null) {
            return null;
        }
        return fromRType(line.getTextValue());
    }

    @Override
    public Double getLineX1(int ROIIndex, int shapeIndex) {
        final Line line = getShape(ROIIndex, shapeIndex, Line.class);
        if (line == null) {
            return null;
        }
        return fromRType(line.getX1());
    }

    @Override
    public Double getLineX2(int ROIIndex, int shapeIndex) {
        final Line line = getShape(ROIIndex, shapeIndex, Line.class);
        if (line == null) {
            return null;
        }
        return fromRType(line.getX2());
    }

    @Override
    public Double getLineY1(int ROIIndex, int shapeIndex) {
        final Line line = getShape(ROIIndex, shapeIndex, Line.class);
        if (line == null) {
            return null;
        }
        return fromRType(line.getY1());
    }

    @Override
    public Double getLineY2(int ROIIndex, int shapeIndex) {
        final Line line = getShape(ROIIndex, shapeIndex, Line.class);
        if (line == null) {
            return null;
        }
        return fromRType(line.getY2());
    }

    @Override
    public String getPointAnnotationRef(int ROIIndex, int shapeIndex, int annotationRefIndex) {
        return getShapeAnnotationRef(ROIIndex, shapeIndex, annotationRefIndex, Point.class);
    }

    @Override
    public Color getPointFillColor(int ROIIndex, int shapeIndex) {
        return getShapeFillColor(ROIIndex, shapeIndex, Point.class);
    }

    @Override
    public FillRule getPointFillRule(int ROIIndex, int shapeIndex) {
        return getShapeFillRule(ROIIndex, shapeIndex, Point.class);
    }

    @Override
    public FontFamily getPointFontFamily(int ROIIndex, int shapeIndex) {
        return getShapeFontFamily(ROIIndex, shapeIndex, Point.class);
    }

    @Override
    public Length getPointFontSize(int ROIIndex, int shapeIndex) {
        return getShapeFontSize(ROIIndex, shapeIndex, Point.class);
    }

    @Override
    public FontStyle getPointFontStyle(int ROIIndex, int shapeIndex) {
        return getShapeFontStyle(ROIIndex, shapeIndex, Point.class);
    }

    @Override
    public String getPointID(int ROIIndex, int shapeIndex) {
        return getShapeID(ROIIndex, shapeIndex, Point.class);
    }

    @Override
    public LineCap getPointLineCap(int ROIIndex, int shapeIndex) {
        return getShapeLineCap(ROIIndex, shapeIndex, Point.class);
    }

    @Override
    public Boolean getPointLocked(int ROIIndex, int shapeIndex) {
        return getShapeLocked(ROIIndex, shapeIndex, Point.class);
    }

    @Override
    public Color getPointStrokeColor(int ROIIndex, int shapeIndex) {
        return getShapeStrokeColor(ROIIndex, shapeIndex, Point.class);
    }

    @Override
    public String getPointStrokeDashArray(int ROIIndex, int shapeIndex) {
        return getShapeStrokeDashArray(ROIIndex, shapeIndex, Point.class);
    }

    @Override
    public Length getPointStrokeWidth(int ROIIndex, int shapeIndex) {
        return getShapeStrokeWidth(ROIIndex, shapeIndex, Point.class);
    }

    @Override
    public NonNegativeInteger getPointTheC(int ROIIndex, int shapeIndex) {
        return getShapeTheC(ROIIndex, shapeIndex, Point.class);
    }

    @Override
    public NonNegativeInteger getPointTheT(int ROIIndex, int shapeIndex) {
        return getShapeTheT(ROIIndex, shapeIndex, Point.class);
    }

    @Override
    public NonNegativeInteger getPointTheZ(int ROIIndex, int shapeIndex) {
        return getShapeTheZ(ROIIndex, shapeIndex, Point.class);
    }

    @Override
    public AffineTransform getPointTransform(int ROIIndex, int shapeIndex) {
        return getShapeTransform(ROIIndex, shapeIndex, Point.class);
    }

    @Override
    public Boolean getPointVisible(int ROIIndex, int shapeIndex) {
        return getShapeVisible(ROIIndex, shapeIndex, Point.class);
    }

    @Override
    public String getPointText(int ROIIndex, int shapeIndex) {
        final Point point = getShape(ROIIndex, shapeIndex, Point.class);
        if (point == null) {
            return null;
        }
        return fromRType(point.getTextValue());
    }

    @Override
    public Double getPointX(int ROIIndex, int shapeIndex) {
        final Point point = getShape(ROIIndex, shapeIndex, Point.class);
        if (point == null) {
            return null;
        }
        return fromRType(point.getCx());
    }

    @Override
    public Double getPointY(int ROIIndex, int shapeIndex) {
        final Point point = getShape(ROIIndex, shapeIndex, Point.class);
        if (point == null) {
            return null;
        }
        return fromRType(point.getCy());
    }

    @Override
    public String getPolygonAnnotationRef(int ROIIndex, int shapeIndex, int annotationRefIndex) {
        return getShapeAnnotationRef(ROIIndex, shapeIndex, annotationRefIndex, Polygon.class);
    }

    @Override
    public Color getPolygonFillColor(int ROIIndex, int shapeIndex) {
        return getShapeFillColor(ROIIndex, shapeIndex, Polygon.class);
    }

    @Override
    public FillRule getPolygonFillRule(int ROIIndex, int shapeIndex) {
        return getShapeFillRule(ROIIndex, shapeIndex, Polygon.class);
    }

    @Override
    public FontFamily getPolygonFontFamily(int ROIIndex, int shapeIndex) {
        return getShapeFontFamily(ROIIndex, shapeIndex, Polygon.class);
    }

    @Override
    public Length getPolygonFontSize(int ROIIndex, int shapeIndex) {
        return getShapeFontSize(ROIIndex, shapeIndex, Polygon.class);
    }

    @Override
    public FontStyle getPolygonFontStyle(int ROIIndex, int shapeIndex) {
        return getShapeFontStyle(ROIIndex, shapeIndex, Polygon.class);
    }

    @Override
    public String getPolygonID(int ROIIndex, int shapeIndex) {
        return getShapeID(ROIIndex, shapeIndex, Polygon.class);
    }

    @Override
    public LineCap getPolygonLineCap(int ROIIndex, int shapeIndex) {
        return getShapeLineCap(ROIIndex, shapeIndex, Polygon.class);
    }

    @Override
    public Boolean getPolygonLocked(int ROIIndex, int shapeIndex) {
        return getShapeLocked(ROIIndex, shapeIndex, Polygon.class);
    }

    @Override
    public Color getPolygonStrokeColor(int ROIIndex, int shapeIndex) {
        return getShapeStrokeColor(ROIIndex, shapeIndex, Polygon.class);
    }

    @Override
    public String getPolygonStrokeDashArray(int ROIIndex, int shapeIndex) {
        return getShapeStrokeDashArray(ROIIndex, shapeIndex, Polygon.class);
    }

    @Override
    public Length getPolygonStrokeWidth(int ROIIndex, int shapeIndex) {
        return getShapeStrokeWidth(ROIIndex, shapeIndex, Polygon.class);
    }

    @Override
    public NonNegativeInteger getPolygonTheC(int ROIIndex, int shapeIndex) {
        return getShapeTheC(ROIIndex, shapeIndex, Polygon.class);
    }

    @Override
    public NonNegativeInteger getPolygonTheT(int ROIIndex, int shapeIndex) {
        return getShapeTheT(ROIIndex, shapeIndex, Polygon.class);
    }

    @Override
    public NonNegativeInteger getPolygonTheZ(int ROIIndex, int shapeIndex) {
        return getShapeTheZ(ROIIndex, shapeIndex, Polygon.class);
    }

    @Override
    public AffineTransform getPolygonTransform(int ROIIndex, int shapeIndex) {
        return getShapeTransform(ROIIndex, shapeIndex, Polygon.class);
    }

    @Override
    public Boolean getPolygonVisible(int ROIIndex, int shapeIndex) {
        return getShapeVisible(ROIIndex, shapeIndex, Polygon.class);
    }

    @Override
    public String getPolygonPoints(int ROIIndex, int shapeIndex) {
        final Polygon polygon = getShape(ROIIndex, shapeIndex, Polygon.class);
        if (polygon == null) {
            return null;
        }
        return fromRType(polygon.getPoints());
    }

    @Override
    public String getPolygonText(int ROIIndex, int shapeIndex) {
        final Polygon polygon = getShape(ROIIndex, shapeIndex, Polygon.class);
        if (polygon == null) {
            return null;
        }
        return fromRType(polygon.getTextValue());
    }

    @Override
    public String getPolylineAnnotationRef(int ROIIndex, int shapeIndex, int annotationRefIndex) {
        return getShapeAnnotationRef(ROIIndex, shapeIndex, annotationRefIndex, Polyline.class);
    }

    @Override
    public Color getPolylineFillColor(int ROIIndex, int shapeIndex) {
        return getShapeFillColor(ROIIndex, shapeIndex, Polyline.class);
    }

    @Override
    public FillRule getPolylineFillRule(int ROIIndex, int shapeIndex) {
        return getShapeFillRule(ROIIndex, shapeIndex, Polyline.class);
    }

    @Override
    public FontFamily getPolylineFontFamily(int ROIIndex, int shapeIndex) {
        return getShapeFontFamily(ROIIndex, shapeIndex, Polyline.class);
    }

    @Override
    public Length getPolylineFontSize(int ROIIndex, int shapeIndex) {
        return getShapeFontSize(ROIIndex, shapeIndex, Polyline.class);
    }

    @Override
    public FontStyle getPolylineFontStyle(int ROIIndex, int shapeIndex) {
        return getShapeFontStyle(ROIIndex, shapeIndex, Polyline.class);
    }

    @Override
    public String getPolylineID(int ROIIndex, int shapeIndex) {
        return getShapeID(ROIIndex, shapeIndex, Polyline.class);
    }

    @Override
    public LineCap getPolylineLineCap(int ROIIndex, int shapeIndex) {
        return getShapeLineCap(ROIIndex, shapeIndex, Polyline.class);
    }

    @Override
    public Boolean getPolylineLocked(int ROIIndex, int shapeIndex) {
        return getShapeLocked(ROIIndex, shapeIndex, Polyline.class);
    }

    @Override
    public Color getPolylineStrokeColor(int ROIIndex, int shapeIndex) {
        return getShapeStrokeColor(ROIIndex, shapeIndex, Polyline.class);
    }

    @Override
    public String getPolylineStrokeDashArray(int ROIIndex, int shapeIndex) {
        return getShapeStrokeDashArray(ROIIndex, shapeIndex, Polyline.class);
    }

    @Override
    public Length getPolylineStrokeWidth(int ROIIndex, int shapeIndex) {
        return getShapeStrokeWidth(ROIIndex, shapeIndex, Polyline.class);
    }

    @Override
    public NonNegativeInteger getPolylineTheC(int ROIIndex, int shapeIndex) {
        return getShapeTheC(ROIIndex, shapeIndex, Polyline.class);
    }

    @Override
    public NonNegativeInteger getPolylineTheT(int ROIIndex, int shapeIndex) {
        return getShapeTheT(ROIIndex, shapeIndex, Polyline.class);
    }

    @Override
    public NonNegativeInteger getPolylineTheZ(int ROIIndex, int shapeIndex) {
        return getShapeTheZ(ROIIndex, shapeIndex, Polyline.class);
    }

    @Override
    public AffineTransform getPolylineTransform(int ROIIndex, int shapeIndex) {
        return getShapeTransform(ROIIndex, shapeIndex, Polyline.class);
    }

    @Override
    public Boolean getPolylineVisible(int ROIIndex, int shapeIndex) {
        return getShapeVisible(ROIIndex, shapeIndex, Polyline.class);
    }

    @Override
    public String getPolylinePoints(int ROIIndex, int shapeIndex) {
        final Polyline polyline = getShape(ROIIndex, shapeIndex, Polyline.class);
        if (polyline == null) {
            return null;
        }
        return fromRType(polyline.getPoints());
    }

    @Override
    public String getPolylineText(int ROIIndex, int shapeIndex) {
        final Polyline polyline = getShape(ROIIndex, shapeIndex, Polyline.class);
        if (polyline == null) {
            return null;
        }
        return fromRType(polyline.getTextValue());
    }

    @Override
    public String getRectangleAnnotationRef(int ROIIndex, int shapeIndex, int annotationRefIndex) {
        return getShapeAnnotationRef(ROIIndex, shapeIndex, annotationRefIndex, Rect.class);
    }

    @Override
    public Color getRectangleFillColor(int ROIIndex, int shapeIndex) {
        return getShapeFillColor(ROIIndex, shapeIndex, Rect.class);
    }

    @Override
    public FillRule getRectangleFillRule(int ROIIndex, int shapeIndex) {
        return getShapeFillRule(ROIIndex, shapeIndex, Rect.class);
    }

    @Override
    public FontFamily getRectangleFontFamily(int ROIIndex, int shapeIndex) {
        return getShapeFontFamily(ROIIndex, shapeIndex, Rect.class);
    }

    @Override
    public Length getRectangleFontSize(int ROIIndex, int shapeIndex) {
        return getShapeFontSize(ROIIndex, shapeIndex, Rect.class);
    }

    @Override
    public FontStyle getRectangleFontStyle(int ROIIndex, int shapeIndex) {
        return getShapeFontStyle(ROIIndex, shapeIndex, Rect.class);
    }

    @Override
    public String getRectangleID(int ROIIndex, int shapeIndex) {
        return getShapeID(ROIIndex, shapeIndex, Rect.class);
    }

    @Override
    public LineCap getRectangleLineCap(int ROIIndex, int shapeIndex) {
        return getShapeLineCap(ROIIndex, shapeIndex, Rect.class);
    }

    @Override
    public Boolean getRectangleLocked(int ROIIndex, int shapeIndex) {
        return getShapeLocked(ROIIndex, shapeIndex, Rect.class);
    }

    @Override
    public Color getRectangleStrokeColor(int ROIIndex, int shapeIndex) {
        return getShapeStrokeColor(ROIIndex, shapeIndex, Rect.class);
    }

    @Override
    public String getRectangleStrokeDashArray(int ROIIndex, int shapeIndex) {
        return getShapeStrokeDashArray(ROIIndex, shapeIndex, Rect.class);
    }

    @Override
    public Length getRectangleStrokeWidth(int ROIIndex, int shapeIndex) {
        return getShapeStrokeWidth(ROIIndex, shapeIndex, Rect.class);
    }

    @Override
    public NonNegativeInteger getRectangleTheC(int ROIIndex, int shapeIndex) {
        return getShapeTheC(ROIIndex, shapeIndex, Rect.class);
    }

    @Override
    public NonNegativeInteger getRectangleTheT(int ROIIndex, int shapeIndex) {
        return getShapeTheT(ROIIndex, shapeIndex, Rect.class);
    }

    @Override
    public NonNegativeInteger getRectangleTheZ(int ROIIndex, int shapeIndex) {
        return getShapeTheZ(ROIIndex, shapeIndex, Rect.class);
    }

    @Override
    public AffineTransform getRectangleTransform(int ROIIndex, int shapeIndex) {
        return getShapeTransform(ROIIndex, shapeIndex, Rect.class);
    }

    @Override
    public Boolean getRectangleVisible(int ROIIndex, int shapeIndex) {
        return getShapeVisible(ROIIndex, shapeIndex, Rect.class);
    }

    @Override
    public String getRectangleText(int ROIIndex, int shapeIndex) {
        final Rect rectangle = getShape(ROIIndex, shapeIndex, Rect.class);
        if (rectangle == null) {
            return null;
        }
        return fromRType(rectangle.getTextValue());
    }

    @Override
    public Double getRectangleHeight(int ROIIndex, int shapeIndex) {
        final Rect rectangle = getShape(ROIIndex, shapeIndex, Rect.class);
        if (rectangle == null) {
            return null;
        }
        return fromRType(rectangle.getHeight());
    }

    @Override
    public Double getRectangleWidth(int ROIIndex, int shapeIndex) {
        final Rect rectangle = getShape(ROIIndex, shapeIndex, Rect.class);
        if (rectangle == null) {
            return null;
        }
        return fromRType(rectangle.getWidth());
    }

    @Override
    public Double getRectangleX(int ROIIndex, int shapeIndex) {
        final Rect rectangle = getShape(ROIIndex, shapeIndex, Rect.class);
        if (rectangle == null) {
            return null;
        }
        return fromRType(rectangle.getX());
    }

    @Override
    public Double getRectangleY(int ROIIndex, int shapeIndex) {
        final Rect rectangle = getShape(ROIIndex, shapeIndex, Rect.class);
        if (rectangle == null) {
            return null;
        }
        return fromRType(rectangle.getY());
    }

    class OmeroMetadataRoot implements MetadataRoot
    {
    }
}
