/*
 *    OmeroMetadata.java
 *
 *-----------------------------------------------------------------------------
 *
 *    Copyright (C) 2009 Glencoe Software, Inc.
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

import java.awt.Color;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import ome.xml.model.primitives.NonNegativeInteger;
import ome.xml.model.primitives.PositiveInteger;
import omero.RDouble;
import omero.RInt;
import omero.RLong;
import omero.RString;
import omero.RTime;
import omero.model.Annotation;
import omero.model.Channel;
import omero.model.Dataset;
import omero.model.Details;
import omero.model.Event;
import omero.model.Experiment;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExternalInfo;
import omero.model.ExternalInfoI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Instrument;
import omero.model.Pixels;
import omero.model.PlaneInfo;
import omero.model.Plate;
import omero.model.Project;
import omero.model.Screen;
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

    // -- State --

    private final List<Image> imageList = new ArrayList<Image>();
    private final List<Dataset> datasetList = new ArrayList<Dataset>();
    private final List<Project> projectList = new ArrayList<Project>();
    private final List<Instrument> instrumentList = new ArrayList<Instrument>();
    private final List<Experiment> experimentList = new ArrayList<Experiment>();
    private final List<Experimenter> experimenterList = new ArrayList<Experimenter>();
    private final List<ExperimenterGroup> experimenterGroupList = new ArrayList<ExperimenterGroup>();
    // SPW
    private final List<Screen> screenList = new ArrayList<Screen>();
    private final List<Plate> plateList = new ArrayList<Plate>();

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
        String lsid = obj.getClass().getSimpleName() + ":" + uuid;
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
                qb.join("i.details.owner",   "i_o",      false, true);
                qb.join("i.details.group",   "i_g",      false, true);
                qb.join("i.pixels",          "p",        false, true);
                qb.join("i.annotationLinks", "i_a_link", false, true);
                qb.join("i_a_link.child",    "i_a",      false, true);
                qb.join("p.details.owner",   "p_o",      false, true);
                qb.join("p.details.group",   "p_g",      false, true);
                qb.join("p.pixelsType",      "pt",       false, true);
                qb.join("p.dimensionOrder",  "do",       false, true);
                qb.join("p.channels",        "c",        false, true);
                qb.join("p.planeInfo",       "pinfo",    true, true);
                qb.join("c.logicalChannel",  "l",        false, true);
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
            } else {
                newImages.add(image);
            }
        }
        this.imageList.clear();
        this.imageList.addAll(newImages);

    }

    private String millis2time(Long millis)
    {
        if (millis == null)
        {
            return null;
        }
        return new Timestamp(millis).toString();
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

    @Override
    public String getImageAcquiredDate(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? millis2time(fromRType(o.getAcquisitionDate())) : null;
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
        return o != null? handleLsid(o.getPrimaryPixels()) : null;
    }

    @Override
    public Double getPixelsPhysicalSizeX(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? fromRType(
                o.getPrimaryPixels().getPhysicalSizeX()) : null;
    }

    @Override
    public Double getPixelsPhysicalSizeY(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? fromRType(
                o.getPrimaryPixels().getPhysicalSizeY()) : null;
    }

    @Override
    public Double getPixelsPhysicalSizeZ(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? fromRType(
                o.getPrimaryPixels().getPhysicalSizeZ()) : null;
    }

    @Override
    public PositiveInteger getPixelsSizeC(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? new PositiveInteger(fromRType(
                o.getPrimaryPixels().getSizeC())) : null;
    }

    @Override
    public PositiveInteger getPixelsSizeT(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? new PositiveInteger(fromRType(
                o.getPrimaryPixels().getSizeT())) : null;
    }

    @Override
    public PositiveInteger getPixelsSizeX(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? new PositiveInteger(fromRType(
                o.getPrimaryPixels().getSizeX())) : null;
    }

    @Override
    public PositiveInteger getPixelsSizeY(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? new PositiveInteger(fromRType(
                o.getPrimaryPixels().getSizeY())) : null;
    }

    @Override
    public PositiveInteger getPixelsSizeZ(int imageIndex)
    {
        Image o = _getImage(imageIndex);
        return o != null? new PositiveInteger(fromRType(
                o.getPrimaryPixels().getSizeZ())) : null;
    }

    @Override
    public Double getPixelsTimeIncrement(int imageIndex)
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
    public Integer getChannelColor(int imageIndex, int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        if (o == null)
        {
            return null;
        }
        try
        {
            Color color = new Color(
                    fromRType(o.getRed()), fromRType(o.getGreen()),
                    fromRType(o.getBlue()), fromRType(o.getAlpha()));
            return color.getRGB();
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
    public PositiveInteger getChannelEmissionWavelength(int imageIndex,
            int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        Integer v = fromRType(o.getLogicalChannel().getEmissionWave());
        return v != null? new PositiveInteger(v) : null; 
    }

    @Override
    public PositiveInteger getChannelExcitationWavelength(int imageIndex,
            int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        Integer v = fromRType(o.getLogicalChannel().getExcitationWave());
        return v != null? new PositiveInteger(v) : null; 
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
    public Double getChannelPinholeSize(int imageIndex, int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        return o != null? fromRType(
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
    public Double getPlaneDeltaT(int imageIndex, int planeIndex)
    {
        PlaneInfo o = getPlane(imageIndex, planeIndex);
        return o != null? fromRType(o.getDeltaT()) : null;
    }

    @Override
    public Double getPlaneExposureTime(int imageIndex, int planeIndex)
    {
        PlaneInfo o = getPlane(imageIndex, planeIndex);
        return o != null? fromRType(o.getExposureTime()) : null;
    }

    @Override
    public Double getPlanePositionX(int imageIndex, int planeIndex)
    {
        PlaneInfo o = getPlane(imageIndex, planeIndex);
        return o != null? fromRType(o.getPositionX()) : null;
    }

    @Override
    public Double getPlanePositionY(int imageIndex, int planeIndex)
    {
        PlaneInfo o = getPlane(imageIndex, planeIndex);
        return o != null? fromRType(o.getPositionY()) : null;
    }

    @Override
    public Double getPlanePositionZ(int imageIndex, int planeIndex)
    {
        PlaneInfo o = getPlane(imageIndex, planeIndex);
        return o != null? fromRType(o.getPositionZ()) : null;
    }

    @Override
    public NonNegativeInteger getPlaneTheC(int imageIndex, int planeIndex)
    {
        PlaneInfo o = getPlane(imageIndex, planeIndex);
        Integer v = fromRType(o.getTheC());
        return v != null? new NonNegativeInteger(v) : null;
    }

    @Override
    public NonNegativeInteger getPlaneTheT(int imageIndex, int planeIndex)
    {
        PlaneInfo o = getPlane(imageIndex, planeIndex);
        Integer v = fromRType(o.getTheT());
        return v != null? new NonNegativeInteger(v) : null;
    }

    @Override
    public NonNegativeInteger getPlaneTheZ(int imageIndex, int planeIndex)
    {
        PlaneInfo o = getPlane(imageIndex, planeIndex);
        Integer v = fromRType(o.getTheZ());
        return v != null? new NonNegativeInteger(v) : null;
    }
}
