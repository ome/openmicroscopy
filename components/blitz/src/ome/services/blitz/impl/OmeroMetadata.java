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

/*-----------------------------------------------------------------------------
 *
 * THIS IS AUTOMATICALLY GENERATED CODE.  DO NOT MODIFY.
 * Created by josh via xsd-fu on 2009-06-09 15:26:21.685835
 *
 *-----------------------------------------------------------------------------
 */

package ome.services.blitz.impl;

import static omero.rtypes.rstring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.text.SimpleDateFormat;

import loci.formats.meta.DummyMetadata;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.meta.MetadataStore;
import ome.conditions.ApiUsageException;
import ome.services.db.DatabaseIdentity;
import ome.tools.hibernate.ProxyCleanupFilter;
import ome.tools.hibernate.QueryBuilder;
import ome.xml.model.enums.AcquisitionMode;
import ome.xml.model.enums.ContrastMethod;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.EnumerationException;
import ome.xml.model.enums.IlluminationType;
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.Color;
import ome.xml.model.primitives.NonNegativeInteger;
import ome.xml.model.primitives.PositiveInteger;
import ome.xml.model.primitives.PositiveFloat;
import ome.xml.model.primitives.Timestamp;
import omero.RBool;
import omero.RDouble;
import omero.RFloat;
import omero.RInt;
import omero.RLong;
import omero.RString;
import omero.RTime;
import omero.model.Annotation;
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
import omero.model.Plane;
import omero.util.IceMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An implementation for {@link MetadataStore} and {@link MetadataRetrieve} that
 * knows how to read and write the OMERO object model.
 * 
 * @author Josh Moore josh at glencoesoftware.com
 * @author Chris Allan callan at blackcat.ca
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public class OmeroMetadata extends DummyMetadata {

    private final static Log log = LogFactory.getLog(OmeroMetadata.class);

    /** The base format string for xsd:dateTime. */
    private SimpleDateFormat xsdDateTimeFormat =
            new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

    // -- State --

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

    private final DatabaseIdentity db;

    public OmeroMetadata(DatabaseIdentity db) {
        this.db = db;
    }

    @Override
    public Object getRoot()
    {
        return "";
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

            Class k = obj.getClass();
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
                qb.join("p.type",             "pt",       false, true);
                qb.join("p.dimensionOrder",   "do",       false, true);
                qb.join("p.channels",         "c",        false, true);
                qb.join("p.planes",        "pinfo",    true, true);
                qb.join("c.acquisitionMode",             "a_mode",   true, true);
                qb.join("c.illuminationType",     "i_type",   true, true);
                qb.join("c.contrastMethod",   "c_method", true, true);
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
            if (replacement != null) {
                newImages.add(replacement);
                initializeAnnotations(replacement);
            } else {
                newImages.add(image);
                initializeAnnotations(image);
            }
        }
        this.imageList.clear();
        this.imageList.addAll(newImages);

    }

    private void initializeAnnotations(Image image)
    {
        for (Annotation annotation : image.linkedAnnotationList())
        {
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

    private Timestamp millisToXsdDateTime(Long millis)
    {
        if (millis == null)
        {
            return null;
        }
        return new Timestamp(new java.sql.Timestamp(millis));
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

    private Boolean fromRType(RBool v)
    {
        return v == null? null : v.getValue();
    }

    private Long fromRType(RTime v)
    {
        return v == null? null : v.getValue();
    }

    private String fromRType(RString v)
    {
        return v == null? null : v.getValue();
    }

    private Double fromRType(RDouble v)
    {
        return v == null? null : v.getValue();
    }

    private Integer fromRType(RInt v)
    {
        return v == null? null : v.getValue();
    }

    private Long fromRType(RLong v)
    {
        return v == null? null : v.getValue();
    }

    private Float fromRType(RFloat v)
    {
        return v == null? null : v.getValue();
    }
    
    private PositiveInteger toPositiveInteger(RInt v)
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

    private NonNegativeInteger toNonNegativeInteger(RInt v)
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

    private PositiveFloat toPositiveFloat(RDouble v)
    {
        try
        {
            Double asDouble = fromRType(v);
            return asDouble != null? new PositiveFloat(asDouble) : null;
        }
        catch (IllegalArgumentException e)
        {
            log.warn("Using new PositiveFloat(1.0)!", e);
            return new PositiveFloat(1.0);
        }
    }

    private PositiveFloat toPositiveFloat(RFloat v)
    {
        try
        {
            Float f = fromRType(v);
            return f != null? new PositiveFloat(f.doubleValue()) : null;
        }
        catch (IllegalArgumentException e)
        {
            log.warn("Using new PositiveFloat(1.0)!", e);
            return new PositiveFloat(1.0);
        }
    }
    
    @Override
    public Timestamp getImageAcquisitionDate(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? millisToXsdDateTime(
                fromRType(o.getAcquisitionDate())) : null;
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
        return o != null? handleLsid(o.getPixels()) : null;
    }

    @Override
    public PositiveFloat getPixelsPhysicalSizeX(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? toPositiveFloat(
                o.getPixels().getPhysicalSizeX()) : null;
    }

    @Override
    public PositiveFloat getPixelsPhysicalSizeY(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? toPositiveFloat(
                o.getPixels().getPhysicalSizeY()) : null;
    }

    @Override
    public PositiveFloat getPixelsPhysicalSizeZ(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? toPositiveFloat(
                o.getPixels().getPhysicalSizeZ()) : null;
    }

    @Override
    public PositiveInteger getPixelsSizeC(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? toPositiveInteger(
                o.getPixels().getSizeC()) : null;
    }

    @Override
    public PositiveInteger getPixelsSizeT(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? toPositiveInteger(
                o.getPixels().getSizeT()) : null;
    }

    @Override
    public PositiveInteger getPixelsSizeX(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? toPositiveInteger(
                o.getPixels().getSizeX()) : null;
    }

    @Override
    public PositiveInteger getPixelsSizeY(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? toPositiveInteger(
                o.getPixels().getSizeY()) : null;
    }

    @Override
    public PositiveInteger getPixelsSizeZ(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? toPositiveInteger(
                o.getPixels().getSizeZ()) : null;
    }

    @Override
    public Double getPixelsTimeIncrement(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? fromRType(
                o.getPixels().getTimeIncrement()) : null;
    }

    @Override
    public PixelType getPixelsType(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        if (o == null)
        {
            return null;
        }
        omero.model.PixelType e = o.getPixels().getType();
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
        Pixels p = i.getPixels();
        if (p == null)
        {
            return null;
        }
        try
        {
        	return p.copyChannels().get(channelIndex);
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
        omero.model.AcquisitionMode e = o.getAcquisitionMode();
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
            return new Color(o.getColor().getValue());
            //int argb = color.getRGB();
            // ARGB --> RGBA
            //return (argb << 8) | (argb >>> (32-8));
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
        omero.model.ContrastMethod e = o.getContrastMethod();
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
        return o.getPixels().sizeOfChannels();
    }

    @Override
    public PositiveInteger getChannelEmissionWavelength(int imageIndex,
            int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        return toPositiveInteger(o.getEmissionWavelength());
    }

    @Override
    public PositiveInteger getChannelExcitationWavelength(int imageIndex,
            int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        return toPositiveInteger(o.getExcitationWavelength()); 
    }

    @Override
    public String getChannelFluor(int imageIndex, int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        return o != null? fromRType(o.getFluor()) : null;
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
        omero.model.IlluminationType e = o.getIlluminationType();
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
        return o != null? fromRType(o.getName()) : null;
    }

    @Override
    public Double getChannelNDFilter(int imageIndex, int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        return o != null? fromRType(o.getNdFilter()) : null;
    }

    @Override
    public Double getChannelPinholeSize(int imageIndex, int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        return o != null? fromRType(o.getPinholeSize()) : null;
    }

    @Override
    public Integer getChannelPockelCellSetting(int imageIndex, int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        return o != null? fromRType(o.getPockelCellSetting()) : null;
    }

    @Override
    public PositiveInteger getChannelSamplesPerPixel(int imageIndex,
            int channelIndex)
    {
        return new PositiveInteger(1);
    }

    private Plane getPlane(int imageIndex, int planeIndex)
    {
        Image i = _getImage(imageIndex);
        if (i == null)
        {
            return null;
        }
        Pixels p = i.getPixels();
        if (p == null)
        {
            return null;
        }
        try
        {
            return p.copyPlanes().get(planeIndex);
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
        return o == null? 0 : o.getPixels().sizeOfPlanes();
    }

    @Override
    public Double getPlaneDeltaT(int imageIndex, int planeIndex)
    {
        Plane o = getPlane(imageIndex, planeIndex);
        return o != null? fromRType(o.getDeltaT()) : null;
    }

    @Override
    public Double getPlaneExposureTime(int imageIndex, int planeIndex)
    {
        Plane o = getPlane(imageIndex, planeIndex);
        return o != null? fromRType(o.getExposureTime()) : null;
    }

    @Override
    public Double getPlanePositionX(int imageIndex, int planeIndex)
    {
        Plane o = getPlane(imageIndex, planeIndex);
        return o != null? fromRType(o.getPositionX()) : null;
    }

    @Override
    public Double getPlanePositionY(int imageIndex, int planeIndex)
    {
        Plane o = getPlane(imageIndex, planeIndex);
        return o != null? fromRType(o.getPositionY()) : null;
    }

    @Override
    public Double getPlanePositionZ(int imageIndex, int planeIndex)
    {
        Plane o = getPlane(imageIndex, planeIndex);
        return o != null? fromRType(o.getPositionZ()) : null;
    }

    @Override
    public NonNegativeInteger getPlaneTheC(int imageIndex, int planeIndex)
    {
        Plane o = getPlane(imageIndex, planeIndex);
        return toNonNegativeInteger(o.getTheC());
    }

    @Override
    public NonNegativeInteger getPlaneTheT(int imageIndex, int planeIndex)
    {
        Plane o = getPlane(imageIndex, planeIndex);
        return toNonNegativeInteger(o.getTheT());
    }

    @Override
    public NonNegativeInteger getPlaneTheZ(int imageIndex, int planeIndex)
    {
        Plane o = getPlane(imageIndex, planeIndex);
        return toNonNegativeInteger(o.getTheZ());
    }

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
        return o != null? 
                millisToXsdDateTime(fromRType(o.getTimeValue())) : null;
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
}
