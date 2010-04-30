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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import loci.formats.meta.MetadataRetrieve;
import loci.formats.meta.MetadataStore;
import ome.conditions.ApiUsageException;
import ome.services.db.DatabaseIdentity;
import ome.services.util.Executor;
import ome.services.util.Executor.SimpleWork;
import ome.services.util.Executor.Work;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.tools.hibernate.ProxyCleanupFilter;
import ome.tools.hibernate.QueryBuilder;
import ome.util.Filterable;
import omero.model.Arc;
import omero.model.Dataset;
import omero.model.Details;
import omero.model.Ellipse;
import omero.model.Event;
import omero.model.Experiment;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExternalInfo;
import omero.model.ExternalInfoI;
import omero.model.Filament;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Instrument;
import omero.model.Laser;
import omero.model.Line;
import omero.model.Plate;
import omero.model.Point;
import omero.model.Polygon;
import omero.model.Polyline;
import omero.model.Project;
import omero.model.Rect;
import omero.model.Screen;
import omero.util.IceMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * An implementation for {@link MetadataStore} and {@link MetadataRetrieve} that
 * knows how to read and write the OMERO object model.
 * 
 * @author Josh Moore josh at glencoesoftware.com
 * @author Chris Allan callan at blackcat.ca
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public class OmeroMetadata implements MetadataRetrieve {

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
                qb.join("i.details.owner",  "i_o",  false, true);
                qb.join("i.details.group",  "i_g",  false, true);
                qb.join("i.pixels",         "p",    false, true);
                qb.join("p.details.owner",  "p_o",  false, true);
                qb.join("p.details.group",  "p_g",  false, true);
                qb.join("p.pixelsType",     "pt",   false, true);
                qb.join("p.dimensionOrder", "do",   false, true);
                qb.join("p.channels",       "c",    false, true);
                qb.join("c.logicalChannel", "l",    false, true);
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

    private Float dbl2float(double value) {
        return Double.valueOf(value).floatValue();
    }

    private Integer dbl2int(double value) {
        return Double.valueOf(value).intValue();
    }

    private String millis2time(long millis) {
        return new Timestamp(millis).toString();
    }

    private String dbl2str(double value) {
        return Double.toString(value);
    }

    private String int2str(int value) {
        return Integer.toString(value);
    }

    private String flt2str(float value) {
        return Float.toString(value);
    }

    public String getUUID() {
        return null;
    }

    // -- MetadataRetrieve API methods --

    // - Entity counting -

    /* @see MetadataRetrieve#getDatasetCount() */
    public int getDatasetCount() {
        int count = Integer.MIN_VALUE;
        try {
            count = datasetList.size();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getDatasetCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getScreenRefCount(int) */
    public int getScreenRefCount(int idxPlate) {
        int count = Integer.MIN_VALUE;
        try {
            count = plateList.get(idxPlate).sizeOfScreenLinks();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getScreenRefCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getProjectRefCount(int) */
    public int getProjectRefCount(int idxDataset) {
        int count = Integer.MIN_VALUE;
        try {
            count = datasetList.get(idxDataset).sizeOfProjectLinks();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getProjectRefCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getProjectCount() */
    public int getProjectCount() {
        int count = Integer.MIN_VALUE;
        try {
            count = projectList.size();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getProjectCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getGroupCount() */
    public int getGroupCount() {
        int count = Integer.MIN_VALUE;
        try {
            count = experimenterGroupList.size();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getGroupCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getPixelsCount(int) */
    public int getPixelsCount(int idxImage) {
        int count = Integer.MIN_VALUE;
        try {
            count = imageList.get(idxImage).sizeOfPixels();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getPixelsCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getGroupRefCount(int) */
    public int getGroupRefCount(int idxExperimenter) {
        int count = Integer.MIN_VALUE;
        try {
            count = experimenterList.get(idxExperimenter)
                    .sizeOfGroupExperimenterMap();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getGroupRefCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getPlateRefCount(int) */
    public int getPlateRefCount(int idxScreen) {
        int count = Integer.MIN_VALUE;
        try {
            count = screenList.get(idxScreen).sizeOfPlateLinks();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getPlateRefCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getWellSampleCount(int,int) */
    public int getWellSampleCount(int idxPlate, int idxWell) {
        int count = Integer.MIN_VALUE;
        try {
            count = plateList.get(idxPlate).copyWells().get(idxWell)
                    .sizeOfWellSamples();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getWellSampleCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getRegionCount(int) */
    public int getRegionCount(int idxImage) {
        int count = Integer.MIN_VALUE;
        try {
            count = -2;
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getRegionCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getWellSampleRefCount(int,int) */
    public int getWellSampleRefCount(int idxScreen, int idxScreenAcquisition) {
        int count = Integer.MIN_VALUE;
        try {
            /* ticket:1750 - for Chris
            count = screenList.get(idxScreen).copyScreenAcquisition().get(
                    idxScreenAcquisition).sizeOfWellSampleLinks();
            */
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getWellSampleRefCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getPlateCount() */
    public int getPlateCount() {
        int count = Integer.MIN_VALUE;
        try {
            count = plateList.size();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getPlateCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getLogicalChannelCount(int) */
    public int getLogicalChannelCount(int idxImage) {
        int count = Integer.MIN_VALUE;
        try {
            count = imageList.get(idxImage).getPrimaryPixels().sizeOfChannels();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getLogicalChannelCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getDichroicCount(int) */
    public int getDichroicCount(int idxInstrument) {
        int count = Integer.MIN_VALUE;
        try {
            count = instrumentList.get(idxInstrument).sizeOfDichroic();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getDichroicCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getReagentCount(int) */
    public int getReagentCount(int idxScreen) {
        int count = Integer.MIN_VALUE;
        try {
            count = screenList.get(idxScreen).sizeOfReagent();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getReagentCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getROIRefCount(int,int) */
    public int getROIRefCount(int idxImage, int idxMicrobeamManipulation) {
        int count = Integer.MIN_VALUE;
        try {
            count = -2;
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getROIRefCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getInstrumentCount() */
    public int getInstrumentCount() {
        int count = Integer.MIN_VALUE;
        try {
            count = instrumentList.size();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getInstrumentCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getExperimenterCount() */
    public int getExperimenterCount() {
        int count = Integer.MIN_VALUE;
        try {
            count = experimenterList.size();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getExperimenterCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getDetectorCount(int) */
    public int getDetectorCount(int idxInstrument) {
        int count = Integer.MIN_VALUE;
        try {
            count = instrumentList.get(idxInstrument).sizeOfDetector();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getDetectorCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getLightSourceCount(int) */
    public int getLightSourceCount(int idxInstrument) {
        int count = Integer.MIN_VALUE;
        try {
            count = instrumentList.get(idxInstrument).sizeOfLightSource();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getLightSourceCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getExperimentCount() */
    public int getExperimentCount() {
        int count = Integer.MIN_VALUE;
        try {
            count = experimentList.size();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getExperimentCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getScreenAcquisitionCount(int) */
    public int getScreenAcquisitionCount(int idxScreen) {
        int count = Integer.MIN_VALUE;
        try {
            /* ticket:1750 - for Chris
            count = screenList.get(idxScreen).sizeOfScreenAcquisition();
            */
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getScreenAcquisitionCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getChannelComponentCount(int,int) */
    public int getChannelComponentCount(int idxImage, int idxLogicalChannel) {
        int count = Integer.MIN_VALUE;
        try {
            count = -2;
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getChannelComponentCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getScreenCount() */
    public int getScreenCount() {
        int count = Integer.MIN_VALUE;
        try {
            count = screenList.size();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getScreenCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getObjectiveCount(int) */
    public int getObjectiveCount(int idxInstrument) {
        int count = Integer.MIN_VALUE;
        try {
            count = instrumentList.get(idxInstrument).sizeOfObjective();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getObjectiveCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getDatasetRefCount(int) */
    public int getDatasetRefCount(int idxImage) {
        int count = Integer.MIN_VALUE;
        try {
            count = imageList.get(idxImage).sizeOfDatasetLinks();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getDatasetRefCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getMicrobeamManipulationRefCount(int) */
    public int getMicrobeamManipulationRefCount(int idxExperiment) {
        int count = Integer.MIN_VALUE;
        try {
            count = experimentList.get(idxExperiment)
                    .sizeOfMicrobeamManipulation();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getMicrobeamManipulationRefCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getLightSourceRefCount(int,int) */
    public int getLightSourceRefCount(int idxImage, int idxMicrobeamManipulation) {
        int count = Integer.MIN_VALUE;
        try {
            count = imageList.get(idxImage).getExperiment()
                    .copyMicrobeamManipulation().get(idxMicrobeamManipulation)
                    .sizeOfLightSourceSettings();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getLightSourceRefCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getFilterCount(int) */
    public int getFilterCount(int idxInstrument) {
        int count = Integer.MIN_VALUE;
        try {
            count = instrumentList.get(idxInstrument).sizeOfFilter();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getFilterCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getMicrobeamManipulationCount(int) */
    public int getMicrobeamManipulationCount(int idxImage) {
        int count = Integer.MIN_VALUE;
        try {
            count = imageList.get(idxImage).getExperiment()
                    .sizeOfMicrobeamManipulation();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getMicrobeamManipulationCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getImageCount() */
    public int getImageCount() {
        int count = Integer.MIN_VALUE;
        try {
            count = imageList.size();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getImageCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getROICount(int) */
    public int getROICount(int idxImage) {
        int count = Integer.MIN_VALUE;
        try {
            count = imageList.get(idxImage).sizeOfRois();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getROICount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getShapeCount(int,int) */
    public int getShapeCount(int idxImage, int idxROI) {
        int count = Integer.MIN_VALUE;
        try {
            count = imageList.get(idxImage).copyRois().get(idxROI)
                    .sizeOfShapes();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getShapeCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getTiffDataCount(int,int) */
    public int getTiffDataCount(int idxImage, int idxPixels) {
        int count = Integer.MIN_VALUE;
        try {
            count = -2;
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getTiffDataCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getFilterSetCount(int) */
    public int getFilterSetCount(int idxInstrument) {
        int count = Integer.MIN_VALUE;
        try {
            count = instrumentList.get(idxInstrument).sizeOfFilterSet();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getFilterSetCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getWellCount(int) */
    public int getWellCount(int idxPlate) {
        int count = Integer.MIN_VALUE;
        try {
            count = plateList.get(idxPlate).sizeOfWells();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getWellCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getPlaneCount(int,int) */
    public int getPlaneCount(int idxImage, int idxPixels) {
        int count = Integer.MIN_VALUE;
        try {
            count = imageList.get(idxImage).copyPixels().get(idxPixels)
                    .sizeOfPlaneInfo();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getPlaneCount()==" + count);
            }
        }
        return count;
    }

    /* @see MetadataRetrieve#getOTFCount(int) */
    public int getOTFCount(int idxInstrument) {
        int count = Integer.MIN_VALUE;
        try {
            count = instrumentList.get(idxInstrument).sizeOfOtf();
        } catch (NullPointerException npe) {
            count = -1;
        } catch (omero.ClientError ce) {
            count = -2;
        } catch (IndexOutOfBoundsException iob) {
            count = -3;
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("getOTFCount()==" + count);
            }
        }
        return count;
    }

    // - FilterSetRef property retrieval -

    // - ObjectiveRef property retrieval -

    // - OTFRef property retrieval -

    // - MaskPixels property retrieval -

    // Image+/DisplayOptions/ROI+/Union/Shape+/Mask/MaskPixels
    public String getMaskPixelsExtendedPixelType(int idxImage, int idxROI,
            int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getMaskPixelsExtendedPixelType()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Mask/MaskPixels
    public Integer getMaskPixelsSizeX(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getMaskPixelsSizeX()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Mask/MaskPixels
    public Integer getMaskPixelsSizeY(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getMaskPixelsSizeY()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Mask/MaskPixels
    public Boolean getMaskPixelsBigEndian(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        Boolean rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getMaskPixelsBigEndian()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Mask/MaskPixels
    public byte[] getMaskPixelsBinData(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        byte[] rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getMaskPixelsBinData()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Mask/MaskPixels
    public String getMaskPixelsID(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getMaskPixelsID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Dataset property retrieval -

    // Dataset+
    public Boolean getDatasetLocked(int idxDataset) {
        Exception e = null;
        Boolean rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getDatasetLocked()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Dataset+
    public String getDatasetDescription(int idxDataset) {
        Exception e = null;
        String rv = null;
        try {
            rv = datasetList.get(idxDataset).getDescription().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getDatasetDescription()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Dataset+
    public String getDatasetExperimenterRef(int idxDataset) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(datasetList.get(idxDataset).getDetails().getOwner());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getDatasetExperimenterRef()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Dataset+
    public String getDatasetGroupRef(int idxDataset) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(datasetList.get(idxDataset).getDetails().getGroup());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getDatasetGroupRef()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Dataset+
    public String getDatasetID(int idxDataset) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(datasetList.get(idxDataset));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getDatasetID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Dataset+
    public String getDatasetName(int idxDataset) {
        Exception e = null;
        String rv = null;
        try {
            rv = datasetList.get(idxDataset).getName().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getDatasetName()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Pump property retrieval -

    // Instrument+/LightSource+/Laser/Pump
    public String getPumpLightSource(int idxInstrument, int idxLightSource) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPumpLightSource()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - ScreenRef property retrieval -

    // Plate+/ScreenRef+
    public String getScreenRefID(int idxPlate, int idxScreenRef) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(plateList.get(idxPlate).copyScreenLinks().get(
                    idxScreenRef));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getScreenRefID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - ManufactSpec property retrieval -

    // - ProjectRef property retrieval -

    // Dataset+/ProjectRef+
    public String getProjectRefID(int idxDataset, int idxProjectRef) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(datasetList.get(idxDataset).copyProjectLinks().get(
                    idxProjectRef));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getProjectRefID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Project property retrieval -

    // Project+
    public String getProjectExperimenterRef(int idxProject) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(projectList.get(idxProject).getDetails().getOwner());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getProjectExperimenterRef()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Project+
    public String getProjectID(int idxProject) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(projectList.get(idxProject));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getProjectID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Project+
    public String getProjectGroupRef(int idxProject) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(projectList.get(idxProject).getDetails().getGroup());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getProjectGroupRef()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Project+
    public String getProjectName(int idxProject) {
        Exception e = null;
        String rv = null;
        try {
            rv = projectList.get(idxProject).getName().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getProjectName()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Project+
    public String getProjectDescription(int idxProject) {
        Exception e = null;
        String rv = null;
        try {
            rv = projectList.get(idxProject).getDescription().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getProjectDescription()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - LogicalChannelRef property retrieval -

    // - Time property retrieval -

    // - Group property retrieval -

    // Group+
    public String getGroupName(int idxGroup) {
        Exception e = null;
        String rv = null;
        try {
            rv = experimenterGroupList.get(idxGroup).getName().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getGroupName()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Pixels property retrieval -

    // Image+/Pixels+
    public Integer getPixelsSizeT(int idxImage, int idxPixels) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = imageList.get(idxImage).copyPixels().get(idxPixels).getSizeT()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPixelsSizeT()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Pixels+
    public String getPixelsDimensionOrder(int idxImage, int idxPixels) {
        Exception e = null;
        String rv = null;
        try {
            rv = imageList.get(idxImage).copyPixels().get(idxPixels)
                    .getDimensionOrder().getValue().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPixelsDimensionOrder()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Pixels+
    public String getPixelsPixelType(int idxImage, int idxPixels) {
        Exception e = null;
        String rv = null;
        try {
            rv = imageList.get(idxImage).copyPixels().get(idxPixels)
                    .getPixelsType().getValue().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPixelsPixelType()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Pixels+
    public Integer getPixelsSizeX(int idxImage, int idxPixels) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = imageList.get(idxImage).copyPixels().get(idxPixels).getSizeX()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPixelsSizeX()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Pixels+
    public Integer getPixelsSizeY(int idxImage, int idxPixels) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = imageList.get(idxImage).copyPixels().get(idxPixels).getSizeY()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPixelsSizeY()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Pixels+
    public Integer getPixelsSizeZ(int idxImage, int idxPixels) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = imageList.get(idxImage).copyPixels().get(idxPixels).getSizeZ()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPixelsSizeZ()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Pixels+
    public Boolean getPixelsBigEndian(int idxImage, int idxPixels) {
        Exception e = null;
        Boolean rv = null;
        try {
            rv = true; // Added for ExporterI
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPixelsBigEndian()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Pixels+
    public Integer getPixelsSizeC(int idxImage, int idxPixels) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = imageList.get(idxImage).copyPixels().get(idxPixels).getSizeC()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPixelsSizeC()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Pixels+
    public String getPixelsID(int idxImage, int idxPixels) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(imageList.get(idxImage).copyPixels().get(idxPixels));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPixelsID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - ExperimentRef property retrieval -

    // - GroupRef property retrieval -

    // - ChannelSpecType property retrieval -

    // - PlateRef property retrieval -

    // Screen+/PlateRef+
    public Integer getPlateRefSample(int idxScreen, int idxPlateRef) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPlateRefSample()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Screen+/PlateRef+
    public String getPlateRefWell(int idxScreen, int idxPlateRef) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPlateRefWell()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Screen+/PlateRef+
    public String getPlateRefID(int idxScreen, int idxPlateRef) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(screenList.get(idxScreen).copyPlateLinks().get(
                    idxPlateRef));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPlateRefID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - WellSample property retrieval -

    // Plate+/Well+/WellSample+
    public Integer getWellSampleIndex(int idxPlate, int idxWell,
            int idxWellSample) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getWellSampleIndex()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Plate+/Well+/WellSample+
    public String getWellSampleImageRef(int idxPlate, int idxWell,
            int idxWellSample) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(plateList.get(idxPlate).copyWells().get(idxWell)
                    .copyWellSamples().get(idxWellSample).getImage());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getWellSampleImageRef()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Plate+/Well+/WellSample+
    public Integer getWellSampleTimepoint(int idxPlate, int idxWell,
            int idxWellSample) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = plateList.get(idxPlate).copyWells().get(idxWell)
                    .copyWellSamples().get(idxWellSample).getTimepoint()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getWellSampleTimepoint()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Plate+/Well+/WellSample+
    public Double getWellSamplePosX(int idxPlate, int idxWell, int idxWellSample) {
        Exception e = null;
        Double rv = null;
        try {
            rv = plateList.get(idxPlate).copyWells().get(idxWell)
                    .copyWellSamples().get(idxWellSample).getPosX().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getWellSamplePosX()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Plate+/Well+/WellSample+
    public Double getWellSamplePosY(int idxPlate, int idxWell, int idxWellSample) {
        Exception e = null;
        Double rv = null;
        try {
            rv = new Double(plateList.get(idxPlate).copyWells().get(idxWell)
                    .copyWellSamples().get(idxWellSample).getPosY().getValue());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getWellSamplePosY()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Plate+/Well+/WellSample+
    public String getWellSampleID(int idxPlate, int idxWell, int idxWellSample) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(plateList.get(idxPlate).copyWells().get(idxWell)
                    .copyWellSamples().get(idxWellSample));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getWellSampleID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Polyline property retrieval -

    // Image+/DisplayOptions/ROI+/Union/Shape+/Polyline
    public String getPolylinePoints(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = ((Polyline) imageList.get(idxImage).copyRois().get(idxROI)
                    .copyShapes().get(idxShape)).getPoints().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPolylinePoints()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Polyline
    public String getPolylineID(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid((Polyline) imageList.get(idxImage).copyRois().get(
                    idxROI).copyShapes().get(idxShape));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPolylineID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Polyline
    public String getPolylineTransform(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = ((Polyline) imageList.get(idxImage).copyRois().get(idxROI)
                    .copyShapes().get(idxShape)).getTransform().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPolylineTransform()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Region property retrieval -

    // Image+/Region+
    public String getRegionTag(int idxImage, int idxRegion) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getRegionTag()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Region+
    public String getRegionName(int idxImage, int idxRegion) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getRegionName()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Region+
    public String getRegionID(int idxImage, int idxRegion) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getRegionID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - StagePosition property retrieval -

    // Image+/Pixels+/Plane+/StagePosition
    public Double getStagePositionPositionZ(int idxImage, int idxPixels,
            int idxPlane) {
        Exception e = null;
        Double rv = null;
        try {
            rv = imageList.get(idxImage).copyPixels().get(idxPixels)
                    .copyPlaneInfo().get(idxPlane).getPositionZ().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getStagePositionPositionZ()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Pixels+/Plane+/StagePosition
    public Double getStagePositionPositionX(int idxImage, int idxPixels,
            int idxPlane) {
        Exception e = null;
        Double rv = null;
        try {
            rv = imageList.get(idxImage).copyPixels().get(idxPixels)
                    .copyPlaneInfo().get(idxPlane).getPositionX().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getStagePositionPositionX()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Pixels+/Plane+/StagePosition
    public Double getStagePositionPositionY(int idxImage, int idxPixels,
            int idxPlane) {
        Exception e = null;
        Double rv = null;
        try {
            rv = imageList.get(idxImage).copyPixels().get(idxPixels)
                    .copyPlaneInfo().get(idxPlane).getPositionY().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getStagePositionPositionY()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - WellSampleRef property retrieval -

    // Screen+/ScreenAcquisition+/WellSampleRef+
    public String getWellSampleRefID(int idxScreen, int idxScreenAcquisition,
            int idxWellSampleRef) {
        Exception e = null;
        String rv = null;
        try {
            /* ticket:1750 - for Chris
            rv = handleLsid(screenList.get(idxScreen).copyScreenAcquisition()
                    .get(idxScreenAcquisition).copyWellSampleLinks().get(
                            idxWellSampleRef));
            */
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getWellSampleRefID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Union property retrieval -

    // - Plate property retrieval -

    // Plate+
    public String getPlateStatus(int idxPlate) {
        Exception e = null;
        String rv = null;
        try {
            rv = plateList.get(idxPlate).getStatus().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPlateStatus()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Plate+
    public String getPlateRowNamingConvention(int idxPlate) {
        Exception e = null;
        String rv = null;
        try {
            rv = plateList.get(idxPlate).getRowNamingConvention().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPlateRowNamingConvention()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Plate+
    public String getPlateDescription(int idxPlate) {
        Exception e = null;
        String rv = null;
        try {
            rv = plateList.get(idxPlate).getDescription().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPlateDescription()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Plate+
    public String getPlateExternalIdentifier(int idxPlate) {
        Exception e = null;
        String rv = null;
        try {
            rv = plateList.get(idxPlate).getExternalIdentifier().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPlateExternalIdentifier()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Plate+
    public String getPlateColumnNamingConvention(int idxPlate) {
        Exception e = null;
        String rv = null;
        try {
            rv = plateList.get(idxPlate).getColumnNamingConvention().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPlateColumnNamingConvention()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Plate+
    public Double getPlateWellOriginY(int idxPlate) {
        Exception e = null;
        Double rv = null;
        try {
            rv = plateList.get(idxPlate).getWellOriginY().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPlateWellOriginY()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Plate+
    public Double getPlateWellOriginX(int idxPlate) {
        Exception e = null;
        Double rv = null;
        try {
            rv = plateList.get(idxPlate).getWellOriginX().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPlateWellOriginX()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Plate+
    public String getPlateID(int idxPlate) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(plateList.get(idxPlate));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPlateID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Plate+
    public String getPlateName(int idxPlate) {
        Exception e = null;
        String rv = null;
        try {
            rv = plateList.get(idxPlate).getName().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPlateName()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - LogicalChannel property retrieval -

    // Image+/LogicalChannel+
    public String getLogicalChannelLightSource(int idxImage,
            int idxLogicalChannel) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(imageList.get(idxImage).getPrimaryPixels()
                    .copyChannels().get(idxLogicalChannel).getLogicalChannel()
                    .getLightSourceSettings().getLightSource());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLogicalChannelLightSource()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/LogicalChannel+
    public String getLogicalChannelSecondaryEmissionFilter(int idxImage,
            int idxLogicalChannel) {
        Exception e = null;
        String rv = null;
        try {
            /* ticket:1750 - to be fixed by Chris
            rv = handleLsid(imageList.get(idxImage).getPrimaryPixels()
                    .copyChannels().get(idxLogicalChannel).getLogicalChannel()
                    .getSecondaryEmissionFilter());
            */
            throw new RuntimeException("TBD");
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLogicalChannelSecondaryEmissionFilter()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/LogicalChannel+
    public Integer getLogicalChannelPockelCellSetting(int idxImage,
            int idxLogicalChannel) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = imageList.get(idxImage).getPrimaryPixels().copyChannels().get(
                    idxLogicalChannel).getLogicalChannel()
                    .getPockelCellSetting().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLogicalChannelPockelCellSetting()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/LogicalChannel+
    public Double getLogicalChannelPinholeSize(int idxImage,
            int idxLogicalChannel) {
        Exception e = null;
        Double rv = null;
        try {
            rv = imageList.get(idxImage).getPrimaryPixels()
                    .copyChannels().get(idxLogicalChannel).getLogicalChannel()
                    .getPinHoleSize().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLogicalChannelPinholeSize()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/LogicalChannel+
    public String getLogicalChannelSecondaryExcitationFilter(int idxImage,
            int idxLogicalChannel) {
        Exception e = null;
        String rv = null;
        try {
            /* ticket:1750 - to be fixed by chris
            rv = handleLsid(imageList.get(idxImage).getPrimaryPixels()
                    .copyChannels().get(idxLogicalChannel).getLogicalChannel()
                    .getSecondaryExcitationFilter());
            */
            throw new RuntimeException("TBD");
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLogicalChannelSecondaryExcitationFilter()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/LogicalChannel+
    public String getLogicalChannelPhotometricInterpretation(int idxImage,
            int idxLogicalChannel) {
        Exception e = null;
        String rv = null;
        try {
            rv = imageList.get(idxImage).getPrimaryPixels().copyChannels().get(
                    idxLogicalChannel).getLogicalChannel()
                    .getPhotometricInterpretation().getValue().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLogicalChannelPhotometricInterpretation()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/LogicalChannel+
    public String getLogicalChannelMode(int idxImage, int idxLogicalChannel) {
        Exception e = null;
        String rv = null;
        try {
            rv = imageList.get(idxImage).getPrimaryPixels().copyChannels().get(
                    idxLogicalChannel).getLogicalChannel().getMode().getValue()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLogicalChannelMode()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/LogicalChannel+
    public String getLogicalChannelContrastMethod(int idxImage,
            int idxLogicalChannel) {
        Exception e = null;
        String rv = null;
        try {
            rv = imageList.get(idxImage).getPrimaryPixels().copyChannels().get(
                    idxLogicalChannel).getLogicalChannel().getContrastMethod()
                    .getValue().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLogicalChannelContrastMethod()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/LogicalChannel+
    public String getLogicalChannelIlluminationType(int idxImage,
            int idxLogicalChannel) {
        Exception e = null;
        String rv = null;
        try {
            rv = imageList.get(idxImage).getPrimaryPixels().copyChannels().get(
                    idxLogicalChannel).getLogicalChannel().getIllumination()
                    .getValue().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLogicalChannelIlluminationType()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/LogicalChannel+
    public String getLogicalChannelOTF(int idxImage, int idxLogicalChannel) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(imageList.get(idxImage).getPrimaryPixels()
                    .copyChannels().get(idxLogicalChannel).getLogicalChannel()
                    .getOtf());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLogicalChannelOTF()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/LogicalChannel+
    public String getLogicalChannelID(int idxImage, int idxLogicalChannel) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(imageList.get(idxImage).getPrimaryPixels()
                    .copyChannels().get(idxLogicalChannel).getLogicalChannel());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLogicalChannelID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/LogicalChannel+
    public String getLogicalChannelFluor(int idxImage, int idxLogicalChannel) {
        Exception e = null;
        String rv = null;
        try {
            rv = imageList.get(idxImage).getPrimaryPixels().copyChannels().get(
                    idxLogicalChannel).getLogicalChannel().getFluor()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLogicalChannelFluor()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/LogicalChannel+
    public Integer getLogicalChannelEmWave(int idxImage, int idxLogicalChannel) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = imageList.get(idxImage).getPrimaryPixels().copyChannels().get(
                    idxLogicalChannel).getLogicalChannel().getEmissionWave()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLogicalChannelEmWave()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/LogicalChannel+
    public Double getLogicalChannelNdFilter(int idxImage, int idxLogicalChannel) {
        Exception e = null;
        Double rv = null;
        try {
            rv = imageList.get(idxImage).getPrimaryPixels()
                    .copyChannels().get(idxLogicalChannel).getLogicalChannel()
                    .getNdFilter().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLogicalChannelNdFilter()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/LogicalChannel+
    public String getLogicalChannelName(int idxImage, int idxLogicalChannel) {
        Exception e = null;
        String rv = null;
        try {
            rv = imageList.get(idxImage).getPrimaryPixels().copyChannels().get(
                    idxLogicalChannel).getLogicalChannel().getName().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLogicalChannelName()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/LogicalChannel+
    public String getLogicalChannelDetector(int idxImage, int idxLogicalChannel) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(imageList.get(idxImage).getPrimaryPixels()
                    .copyChannels().get(idxLogicalChannel).getLogicalChannel()
                    .getDetectorSettings().getDetector());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLogicalChannelDetector()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/LogicalChannel+
    public Integer getLogicalChannelSamplesPerPixel(int idxImage,
            int idxLogicalChannel) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = imageList.get(idxImage).getPrimaryPixels().copyChannels().get(
                    idxLogicalChannel).getLogicalChannel().getSamplesPerPixel()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLogicalChannelSamplesPerPixel()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/LogicalChannel+
    public Integer getLogicalChannelExWave(int idxImage, int idxLogicalChannel) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = imageList.get(idxImage).getPrimaryPixels().copyChannels().get(
                    idxLogicalChannel).getLogicalChannel().getExcitationWave()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLogicalChannelExWave()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/LogicalChannel+
    public String getLogicalChannelFilterSet(int idxImage, int idxLogicalChannel) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(imageList.get(idxImage).getPrimaryPixels()
                    .copyChannels().get(idxLogicalChannel).getLogicalChannel()
                    .getFilterSet());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLogicalChannelFilterSet()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Dichroic property retrieval -

    // Instrument+/Dichroic+
    public String getDichroicModel(int idxInstrument, int idxDichroic) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyDichroic().get(
                    idxDichroic).getModel().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getDichroicModel()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Dichroic+
    public String getDichroicLotNumber(int idxInstrument, int idxDichroic) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyDichroic().get(
                    idxDichroic).getLotNumber().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getDichroicLotNumber()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Dichroic+
    public String getDichroicManufacturer(int idxInstrument, int idxDichroic) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyDichroic().get(
                    idxDichroic).getManufacturer().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getDichroicManufacturer()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Channels property retrieval -

    // - ImageRef property retrieval -

    // - Reagent property retrieval -

    // Screen+/Reagent+
    public String getReagentReagentIdentifier(int idxScreen, int idxReagent) {
        Exception e = null;
        String rv = null;
        try {
            rv = screenList.get(idxScreen).copyReagent().get(idxReagent)
                    .getReagentIdentifier().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getReagentReagentIdentifier()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Screen+/Reagent+
    public String getReagentDescription(int idxScreen, int idxReagent) {
        Exception e = null;
        String rv = null;
        try {
            rv = screenList.get(idxScreen).copyReagent().get(idxReagent)
                    .getDescription().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getReagentDescription()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Screen+/Reagent+
    public String getReagentID(int idxScreen, int idxReagent) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(screenList.get(idxScreen).copyReagent().get(
                    idxReagent));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getReagentID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Screen+/Reagent+
    public String getReagentName(int idxScreen, int idxReagent) {
        Exception e = null;
        String rv = null;
        try {
            rv = screenList.get(idxScreen).copyReagent().get(idxReagent)
                    .getName().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getReagentName()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - ROIRef property retrieval -

    // Image+/MicrobeamManipulation+/ROIRef+
    public String getROIRefID(int idxImage, int idxMicrobeamManipulation,
            int idxROIRef) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getROIRefID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - OME property retrieval -

    // - Instrument property retrieval -

    // Instrument+
    public String getInstrumentID(int idxInstrument) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(instrumentList.get(idxInstrument));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getInstrumentID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Experimenter property retrieval -

    // Experimenter+
    public String getExperimenterEmail(int idxExperimenter) {
        Exception e = null;
        String rv = null;
        try {
            rv = experimenterList.get(idxExperimenter).getEmail().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getExperimenterEmail()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Experimenter+
    public String getExperimenterOMEName(int idxExperimenter) {
        Exception e = null;
        String rv = null;
        try {
            rv = experimenterList.get(idxExperimenter).getOmeName().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getExperimenterOMEName()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Experimenter+
    public String getExperimenterFirstName(int idxExperimenter) {
        Exception e = null;
        String rv = null;
        try {
            rv = experimenterList.get(idxExperimenter).getFirstName()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getExperimenterFirstName()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Experimenter+
    public String getExperimenterLastName(int idxExperimenter) {
        Exception e = null;
        String rv = null;
        try {
            rv = experimenterList.get(idxExperimenter).getLastName().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getExperimenterLastName()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Experimenter+
    public String getExperimenterInstitution(int idxExperimenter) {
        Exception e = null;
        String rv = null;
        try {
            rv = experimenterList.get(idxExperimenter).getInstitution()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getExperimenterInstitution()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Experimenter+
    public String getExperimenterID(int idxExperimenter) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(experimenterList.get(idxExperimenter));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getExperimenterID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Detector property retrieval -

    // Instrument+/Detector+
    public Double getDetectorGain(int idxInstrument, int idxDetector) {
        Exception e = null;
        Double rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyDetector().get(
                    idxDetector).getGain().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getDetectorGain()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Detector+
    public String getDetectorSerialNumber(int idxInstrument, int idxDetector) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyDetector().get(
                    idxDetector).getSerialNumber().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getDetectorSerialNumber()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Detector+
    public Double getDetectorZoom(int idxInstrument, int idxDetector) {
        Exception e = null;
        Double rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyDetector().get(
                    idxDetector).getZoom().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getDetectorZoom()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Detector+
    public Double getDetectorAmplificationGain(int idxInstrument, int idxDetector) {
        Exception e = null;
        Double rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyDetector().get(
                    idxDetector).getAmplificationGain().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getDetectorAmplificationGain()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Detector+
    public Double getDetectorVoltage(int idxInstrument, int idxDetector) {
        Exception e = null;
        Double rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyDetector().get(
                    idxDetector).getVoltage().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getDetectorVoltage()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Detector+
    public Double getDetectorOffset(int idxInstrument, int idxDetector) {
        Exception e = null;
        Double rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyDetector().get(
                    idxDetector).getOffsetValue().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getDetectorOffset()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Detector+
    public String getDetectorModel(int idxInstrument, int idxDetector) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyDetector().get(
                    idxDetector).getModel().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getDetectorModel()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Detector+
    public String getDetectorType(int idxInstrument, int idxDetector) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyDetector().get(
                    idxDetector).getType().getValue().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getDetectorType()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Detector+
    public String getDetectorID(int idxInstrument, int idxDetector) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(instrumentList.get(idxInstrument).copyDetector()
                    .get(idxDetector));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getDetectorID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Detector+
    public String getDetectorManufacturer(int idxInstrument, int idxDetector) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyDetector().get(
                    idxDetector).getManufacturer().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getDetectorManufacturer()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - LightSource property retrieval -

    // Instrument+/LightSource+
    public String getLightSourceModel(int idxInstrument, int idxLightSource) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyLightSource().get(
                    idxLightSource).getModel().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLightSourceModel()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/LightSource+
    public String getLightSourceSerialNumber(int idxInstrument,
            int idxLightSource) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyLightSource().get(
                    idxLightSource).getSerialNumber().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLightSourceSerialNumber()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/LightSource+
    public String getLightSourceID(int idxInstrument, int idxLightSource) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(instrumentList.get(idxInstrument).copyLightSource()
                    .get(idxLightSource));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLightSourceID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/LightSource+
    public Double getLightSourcePower(int idxInstrument, int idxLightSource) {
        Exception e = null;
        Double rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyLightSource()
                    .get(idxLightSource).getPower().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLightSourcePower()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/LightSource+
    public String getLightSourceManufacturer(int idxInstrument,
            int idxLightSource) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyLightSource().get(
                    idxLightSource).getManufacturer().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLightSourceManufacturer()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Arc property retrieval -

    // Instrument+/LightSource+/Arc
    public String getArcType(int idxInstrument, int idxLightSource) {
        Exception e = null;
        String rv = null;
        try {
            rv = ((Arc) instrumentList.get(idxInstrument).copyLightSource()
                    .get(idxLightSource)).getType().getValue().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getArcType()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - BasicSvgShape property retrieval -

    // - Rect property retrieval -

    // Image+/DisplayOptions/ROI+/Union/Shape+/Rect
    public String getRectTransform(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = ((Rect) imageList.get(idxImage).copyRois().get(idxROI)
                    .copyShapes().get(idxShape)).getTransform().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getRectTransform()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Rect
    public String getRectHeight(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = dbl2str(((Rect) imageList.get(idxImage).copyRois().get(idxROI)
                    .copyShapes().get(idxShape)).getHeight().getValue());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getRectHeight()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Rect
    public String getRectWidth(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = dbl2str(((Rect) imageList.get(idxImage).copyRois().get(idxROI)
                    .copyShapes().get(idxShape)).getWidth().getValue());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getRectWidth()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Rect
    public String getRectY(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = dbl2str(((Rect) imageList.get(idxImage).copyRois().get(idxROI)
                    .copyShapes().get(idxShape)).getY().getValue());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getRectY()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Rect
    public String getRectX(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = dbl2str(((Rect) imageList.get(idxImage).copyRois().get(idxROI)
                    .copyShapes().get(idxShape)).getX().getValue());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getRectX()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Rect
    public String getRectID(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid((Rect) imageList.get(idxImage).copyRois().get(
                    idxROI).copyShapes().get(idxShape));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getRectID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Experiment property retrieval -

    // Experiment+
    public String getExperimentExperimenterRef(int idxExperiment) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(experimentList.get(idxExperiment).getDetails()
                    .getOwner());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getExperimentExperimenterRef()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Experiment+
    public String getExperimentType(int idxExperiment) {
        Exception e = null;
        String rv = null;
        try {
            rv = experimentList.get(idxExperiment).getType().getValue()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getExperimentType()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Experiment+
    public String getExperimentDescription(int idxExperiment) {
        Exception e = null;
        String rv = null;
        try {
            rv = experimentList.get(idxExperiment).getDescription().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getExperimentDescription()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Experiment+
    public String getExperimentID(int idxExperiment) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(experimentList.get(idxExperiment));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getExperimentID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - ScreenAcquisition property retrieval -

    // Screen+/ScreenAcquisition+
    public String getScreenAcquisitionEndTime(int idxScreen,
            int idxScreenAcquisition) {
        Exception e = null;
        String rv = null;
        try {
            /* ticket:1750 - to be handled by chris
            rv = millis2time(screenList.get(idxScreen).copyScreenAcquisition()
                    .get(idxScreenAcquisition).getEndTime().getValue());
             */
            throw new RuntimeException("TBD");
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getScreenAcquisitionEndTime()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Screen+/ScreenAcquisition+
    public String getScreenAcquisitionID(int idxScreen, int idxScreenAcquisition) {
        Exception e = null;
        String rv = null;
        try {
            /* ticket:1750 - to be fixed by chris
            rv = handleLsid(screenList.get(idxScreen).copyScreenAcquisition()
                    .get(idxScreenAcquisition));
            */
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getScreenAcquisitionID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Screen+/ScreenAcquisition+
    public String getScreenAcquisitionStartTime(int idxScreen,
            int idxScreenAcquisition) {
        Exception e = null;
        String rv = null;
        try {
            /* ticket:1750 - to be fixed by chris
            rv = millis2time(screenList.get(idxScreen).copyScreenAcquisition()
                    .get(idxScreenAcquisition).getStartTime().getValue());
            */
            throw new RuntimeException("TBD");
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getScreenAcquisitionStartTime()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - PlaneTiming property retrieval -

    // Image+/Pixels+/Plane+/PlaneTiming
    public Double getPlaneTimingExposureTime(int idxImage, int idxPixels,
            int idxPlane) {
        Exception e = null;
        Double rv = null;
        try {
            rv = imageList.get(idxImage).copyPixels().get(idxPixels)
                    .copyPlaneInfo().get(idxPlane).getExposureTime().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPlaneTimingExposureTime()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Pixels+/Plane+/PlaneTiming
    public Double getPlaneTimingDeltaT(int idxImage, int idxPixels, int idxPlane) {
        Exception e = null;
        Double rv = null;
        try {
            rv = imageList.get(idxImage).copyPixels().get(idxPixels)
                    .copyPlaneInfo().get(idxPlane).getDeltaT().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPlaneTimingDeltaT()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - ImagingEnvironment property retrieval -

    // Image+/ImagingEnvironment
    public Double getImagingEnvironmentCO2Percent(int idxImage) {
        Exception e = null;
        Double rv = null;
        try {
            rv = imageList.get(idxImage).getImagingEnvironment()
                    .getCo2percent().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getImagingEnvironmentCO2Percent()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/ImagingEnvironment
    public Double getImagingEnvironmentTemperature(int idxImage) {
        Exception e = null;
        Double rv = null;
        try {
            rv = imageList.get(idxImage).getImagingEnvironment()
                    .getTemperature().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getImagingEnvironmentTemperature()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/ImagingEnvironment
    public Double getImagingEnvironmentAirPressure(int idxImage) {
        Exception e = null;
        Double rv = null;
        try {
            rv = imageList.get(idxImage).getImagingEnvironment()
                    .getAirPressure().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getImagingEnvironmentAirPressure()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/ImagingEnvironment
    public Double getImagingEnvironmentHumidity(int idxImage) {
        Exception e = null;
        Double rv = null;
        try {
            rv = imageList.get(idxImage).getImagingEnvironment()
                    .getHumidity().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getImagingEnvironmentHumidity()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Description property retrieval -

    // - Filament property retrieval -

    // Instrument+/LightSource+/Filament
    public String getFilamentType(int idxInstrument, int idxLightSource) {
        Exception e = null;
        String rv = null;
        try {
            rv = ((Filament) instrumentList.get(idxInstrument)
                    .copyLightSource().get(idxLightSource)).getType()
                    .getValue().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getFilamentType()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Microscope property retrieval -

    // Instrument+/Microscope
    public String getMicroscopeModel(int idxInstrument) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).getMicroscope().getModel()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getMicroscopeModel()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Microscope
    public String getMicroscopeSerialNumber(int idxInstrument) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).getMicroscope()
                    .getSerialNumber().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getMicroscopeSerialNumber()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Microscope
    public String getMicroscopeType(int idxInstrument) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).getMicroscope().getType()
                    .getValue().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getMicroscopeType()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Microscope
    public String getMicroscopeID(int idxInstrument) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(instrumentList.get(idxInstrument).getMicroscope());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getMicroscopeID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Microscope
    public String getMicroscopeManufacturer(int idxInstrument) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).getMicroscope()
                    .getManufacturer().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getMicroscopeManufacturer()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - ChannelComponent property retrieval -

    // Image+/LogicalChannel+/ChannelComponent+
    public Integer getChannelComponentIndex(int idxImage,
            int idxLogicalChannel, int idxChannelComponent) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getChannelComponentIndex()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/LogicalChannel+/ChannelComponent+
    public String getChannelComponentColorDomain(int idxImage,
            int idxLogicalChannel, int idxChannelComponent) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getChannelComponentColorDomain()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/LogicalChannel+/ChannelComponent+
    public String getChannelComponentPixels(int idxImage,
            int idxLogicalChannel, int idxChannelComponent) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getChannelComponentPixels()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - DetectorRef property retrieval -

    // - Laser property retrieval -

    // Instrument+/LightSource+/Laser
    public Boolean getLaserPockelCell(int idxInstrument, int idxLightSource) {
        Exception e = null;
        Boolean rv = null;
        try {
            rv = ((Laser) instrumentList.get(idxInstrument).copyLightSource()
                    .get(idxLightSource)).getPockelCell().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLaserPockelCell()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/LightSource+/Laser
    public Boolean getLaserTuneable(int idxInstrument, int idxLightSource) {
        Exception e = null;
        Boolean rv = null;
        try {
            rv = ((Laser) instrumentList.get(idxInstrument).copyLightSource()
                    .get(idxLightSource)).getTuneable().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLaserTuneable()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/LightSource+/Laser
    public String getLaserLaserMedium(int idxInstrument, int idxLightSource) {
        Exception e = null;
        String rv = null;
        try {
            rv = ((Laser) instrumentList.get(idxInstrument).copyLightSource()
                    .get(idxLightSource)).getLaserMedium().getValue()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLaserLaserMedium()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/LightSource+/Laser
    public String getLaserPulse(int idxInstrument, int idxLightSource) {
        Exception e = null;
        String rv = null;
        try {
            rv = ((Laser) instrumentList.get(idxInstrument).copyLightSource()
                    .get(idxLightSource)).getPulse().getValue().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLaserPulse()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/LightSource+/Laser
    public Integer getLaserWavelength(int idxInstrument, int idxLightSource) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = ((Laser) instrumentList.get(idxInstrument).copyLightSource()
                    .get(idxLightSource)).getWavelength().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLaserWavelength()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/LightSource+/Laser
    public Integer getLaserFrequencyMultiplication(int idxInstrument,
            int idxLightSource) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = ((Laser) instrumentList.get(idxInstrument).copyLightSource()
                    .get(idxLightSource)).getFrequencyMultiplication()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLaserFrequencyMultiplication()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/LightSource+/Laser
    public String getLaserType(int idxInstrument, int idxLightSource) {
        Exception e = null;
        String rv = null;
        try {
            rv = ((Laser) instrumentList.get(idxInstrument).copyLightSource()
                    .get(idxLightSource)).getType().getValue().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLaserType()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/LightSource+/Laser
    public Double getLaserRepetitionRate(int idxInstrument, int idxLightSource) {
        Exception e = null;
        Double rv = null;
        try {
            rv = ((Laser) instrumentList.get(idxInstrument)
                    .copyLightSource().get(idxLightSource)).getRepetitionRate()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLaserRepetitionRate()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Screen property retrieval -

    // Screen+
    public String getScreenDescription(int idxScreen) {
        Exception e = null;
        String rv = null;
        try {
            rv = screenList.get(idxScreen).getDescription().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getScreenDescription()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Screen+
    public String getScreenReagentSetDescription(int idxScreen) {
        Exception e = null;
        String rv = null;
        try {
            rv = screenList.get(idxScreen).getReagentSetDescription()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getScreenReagentSetDescription()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Screen+
    public String getScreenProtocolIdentifier(int idxScreen) {
        Exception e = null;
        String rv = null;
        try {
            rv = screenList.get(idxScreen).getProtocolIdentifier().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getScreenProtocolIdentifier()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Screen+
    public String getScreenProtocolDescription(int idxScreen) {
        Exception e = null;
        String rv = null;
        try {
            rv = screenList.get(idxScreen).getProtocolDescription().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getScreenProtocolDescription()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Screen+
    public String getScreenExtern(int idxScreen) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getScreenExtern()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Screen+
    public String getScreenName(int idxScreen) {
        Exception e = null;
        String rv = null;
        try {
            rv = screenList.get(idxScreen).getName().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getScreenName()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Screen+
    public String getScreenType(int idxScreen) {
        Exception e = null;
        String rv = null;
        try {
            rv = screenList.get(idxScreen).getType().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getScreenType()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Screen+
    public String getScreenID(int idxScreen) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(screenList.get(idxScreen));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getScreenID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Screen+
    public String getScreenReagentSetIdentifier(int idxScreen) {
        Exception e = null;
        String rv = null;
        try {
            rv = screenList.get(idxScreen).getReagentSetIdentifier().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getScreenReagentSetIdentifier()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Thumbnail property retrieval -

    // Image+/Thumbnail
    public String getThumbnailMIMEtype(int idxImage) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getThumbnailMIMEtype()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Thumbnail
    public String getThumbnailHref(int idxImage) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getThumbnailHref()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Thumbnail
    public String getThumbnailID(int idxImage) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getThumbnailID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Objective property retrieval -

    // Instrument+/Objective+
    public Boolean getObjectiveIris(int idxInstrument, int idxObjective) {
        Exception e = null;
        Boolean rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyObjective().get(
                    idxObjective).getIris().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getObjectiveIris()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Objective+
    public Double getObjectiveWorkingDistance(int idxInstrument, int idxObjective) {
        Exception e = null;
        Double rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyObjective().get(
                    idxObjective).getWorkingDistance().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getObjectiveWorkingDistance()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Objective+
    public String getObjectiveImmersion(int idxInstrument, int idxObjective) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyObjective().get(
                    idxObjective).getImmersion().getValue().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getObjectiveImmersion()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Objective+
    public String getObjectiveCorrection(int idxInstrument, int idxObjective) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyObjective().get(
                    idxObjective).getCorrection().getValue().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getObjectiveCorrection()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Objective+
    public String getObjectiveSerialNumber(int idxInstrument, int idxObjective) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyObjective().get(
                    idxObjective).getSerialNumber().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getObjectiveSerialNumber()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Objective+
    public Double getObjectiveLensNA(int idxInstrument, int idxObjective) {
        Exception e = null;
        Double rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyObjective().get(
                    idxObjective).getLensNA().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getObjectiveLensNA()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Objective+
    public Integer getObjectiveNominalMagnification(int idxInstrument,
            int idxObjective) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyObjective().get(
                    idxObjective).getNominalMagnification().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getObjectiveNominalMagnification()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Objective+
    public String getObjectiveModel(int idxInstrument, int idxObjective) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyObjective().get(
                    idxObjective).getModel().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getObjectiveModel()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Objective+
    public Double getObjectiveCalibratedMagnification(int idxInstrument,
            int idxObjective) {
        Exception e = null;
        Double rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyObjective().get(
                    idxObjective).getCalibratedMagnification().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getObjectiveCalibratedMagnification()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Objective+
    public String getObjectiveID(int idxInstrument, int idxObjective) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(instrumentList.get(idxInstrument).copyObjective()
                    .get(idxObjective));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getObjectiveID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Objective+
    public String getObjectiveManufacturer(int idxInstrument, int idxObjective) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyObjective().get(
                    idxObjective).getManufacturer().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getObjectiveManufacturer()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Circle property retrieval -

    // Image+/DisplayOptions/ROI+/Union/Shape+/Circle
    public String getCircleR(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getCircleR()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Circle
    public String getCircleCy(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getCircleCy()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Circle
    public String getCircleCx(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getCircleCx()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Circle
    public String getCircleID(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getCircleID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Circle
    public String getCircleTransform(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getCircleTransform()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - DatasetRef property retrieval -

    // Image+/DatasetRef+
    public String getDatasetRefID(int idxImage, int idxDatasetRef) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(imageList.get(idxImage).copyDatasetLinks().get(
                    idxDatasetRef));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getDatasetRefID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - MicrobeamManipulationRef property retrieval -

    // Experiment+/MicrobeamManipulationRef+
    public String getMicrobeamManipulationRefID(int idxExperiment,
            int idxMicrobeamManipulationRef) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(experimentList.get(idxExperiment)
                    .copyMicrobeamManipulation().get(
                            idxMicrobeamManipulationRef));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getMicrobeamManipulationRefID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - LightSourceRef property retrieval -

    // Image+/MicrobeamManipulation+/LightSourceRef+
    public String getLightSourceRefLightSource(int idxImage,
            int idxMicrobeamManipulation, int idxLightSourceRef) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(imageList.get(idxImage).getExperiment()
                    .copyMicrobeamManipulation().get(idxMicrobeamManipulation)
                    .copyLightSourceSettings().get(idxLightSourceRef)
                    .getLightSource());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLightSourceRefLightSource()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/MicrobeamManipulation+/LightSourceRef+
    public Integer getLightSourceRefWavelength(int idxImage,
            int idxMicrobeamManipulation, int idxLightSourceRef) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = imageList.get(idxImage).getExperiment()
                    .copyMicrobeamManipulation().get(idxMicrobeamManipulation)
                    .copyLightSourceSettings().get(idxLightSourceRef)
                    .getWavelength().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLightSourceRefWavelength()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/MicrobeamManipulation+/LightSourceRef+
    public Double getLightSourceRefAttenuation(int idxImage,
            int idxMicrobeamManipulation, int idxLightSourceRef) {
        Exception e = null;
        Double rv = null;
        try {
            rv = imageList.get(idxImage).getExperiment()
                    .copyMicrobeamManipulation().get(idxMicrobeamManipulation)
                    .copyLightSourceSettings().get(idxLightSourceRef)
                    .getAttenuation().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLightSourceRefAttenuation()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Point property retrieval -

    // Image+/DisplayOptions/ROI+/Union/Shape+/Point
    public String getPointR(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPointR()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Point
    public String getPointCy(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = dbl2str(((Point) imageList.get(idxImage).copyRois()
                    .get(idxROI).copyShapes().get(idxShape)).getCy().getValue());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPointCy()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Point
    public String getPointCx(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = dbl2str(((Point) imageList.get(idxImage).copyRois()
                    .get(idxROI).copyShapes().get(idxShape)).getCx().getValue());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPointCx()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Point
    public String getPointID(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid((Point) imageList.get(idxImage).copyRois().get(
                    idxROI).copyShapes().get(idxShape));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPointID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Point
    public String getPointTransform(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = ((Point) imageList.get(idxImage).copyRois().get(idxROI)
                    .copyShapes().get(idxShape)).getTransform().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPointTransform()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Leader property retrieval -

    // - Reference property retrieval -

    // - Filter property retrieval -

    // Instrument+/Filter+
    public String getFilterFilterWheel(int idxInstrument, int idxFilter) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyFilter().get(idxFilter)
                    .getFilterWheel().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getFilterFilterWheel()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Filter+
    public String getFilterModel(int idxInstrument, int idxFilter) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyFilter().get(idxFilter)
                    .getModel().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getFilterModel()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Filter+
    public String getFilterLotNumber(int idxInstrument, int idxFilter) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyFilter().get(idxFilter)
                    .getLotNumber().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getFilterLotNumber()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Filter+
    public String getFilterType(int idxInstrument, int idxFilter) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyFilter().get(idxFilter)
                    .getType().getValue().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getFilterType()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Filter+
    public String getFilterManufacturer(int idxInstrument, int idxFilter) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyFilter().get(idxFilter)
                    .getManufacturer().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getFilterManufacturer()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - MicrobeamManipulation property retrieval -

    // Image+/MicrobeamManipulation+
    public String getMicrobeamManipulationExperimenterRef(int idxImage,
            int idxMicrobeamManipulation) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(imageList.get(idxImage).getExperiment()
                    .copyMicrobeamManipulation().get(idxMicrobeamManipulation)
                    .getDetails().getOwner());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getMicrobeamManipulationExperimenterRef()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/MicrobeamManipulation+
    public String getMicrobeamManipulationType(int idxImage,
            int idxMicrobeamManipulation) {
        Exception e = null;
        String rv = null;
        try {
            rv = imageList.get(idxImage).getExperiment()
                    .copyMicrobeamManipulation().get(idxMicrobeamManipulation)
                    .getType().getValue().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getMicrobeamManipulationType()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/MicrobeamManipulation+
    public String getMicrobeamManipulationID(int idxImage,
            int idxMicrobeamManipulation) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(imageList.get(idxImage).getExperiment()
                    .copyMicrobeamManipulation().get(idxMicrobeamManipulation));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getMicrobeamManipulationID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Ellipse property retrieval -

    // Image+/DisplayOptions/ROI+/Union/Shape+/Ellipse
    public String getEllipseRx(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = dbl2str(((Ellipse) imageList.get(idxImage).copyRois().get(
                    idxROI).copyShapes().get(idxShape)).getRx().getValue());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getEllipseRx()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Ellipse
    public String getEllipseRy(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = dbl2str(((Ellipse) imageList.get(idxImage).copyRois().get(
                    idxROI).copyShapes().get(idxShape)).getRy().getValue());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getEllipseRy()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Ellipse
    public String getEllipseTransform(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = ((Ellipse) imageList.get(idxImage).copyRois().get(idxROI)
                    .copyShapes().get(idxShape)).getTransform().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getEllipseTransform()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Ellipse
    public String getEllipseCy(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = dbl2str(((Ellipse) imageList.get(idxImage).copyRois().get(
                    idxROI).copyShapes().get(idxShape)).getCy().getValue());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getEllipseCy()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Ellipse
    public String getEllipseCx(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = dbl2str(((Ellipse) imageList.get(idxImage).copyRois().get(
                    idxROI).copyShapes().get(idxShape)).getCx().getValue());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getEllipseCx()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Ellipse
    public String getEllipseID(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid((Ellipse) imageList.get(idxImage).copyRois().get(
                    idxROI).copyShapes().get(idxShape));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getEllipseID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - GreyChannel property retrieval -

    // Image+/DisplayOptions/GreyChannel
    public Integer getGreyChannelChannelNumber(int idxImage) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getGreyChannelChannelNumber()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/GreyChannel
    public Double getGreyChannelBlackLevel(int idxImage) {
        Exception e = null;
        Double rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getGreyChannelBlackLevel()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/GreyChannel
    public Double getGreyChannelWhiteLevel(int idxImage) {
        Exception e = null;
        Double rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getGreyChannelWhiteLevel()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/GreyChannel
    public Double getGreyChannelGamma(int idxImage) {
        Exception e = null;
        Double rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getGreyChannelGamma()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/GreyChannel
    public Boolean getGreyChannelisOn(int idxImage) {
        Exception e = null;
        Boolean rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getGreyChannelisOn()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - InstrumentRef property retrieval -

    // - TransmittanceRange property retrieval -

    // Instrument+/Filter+/TransmittanceRange
    public Integer getTransmittanceRangeCutIn(int idxInstrument, int idxFilter) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyFilter().get(idxFilter)
                    .getTransmittanceRange().getCutIn().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getTransmittanceRangeCutIn()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Filter+/TransmittanceRange
    public Integer getTransmittanceRangeTransmittance(int idxInstrument,
            int idxFilter) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = dbl2int(instrumentList.get(idxInstrument).copyFilter().get(
                    idxFilter).getTransmittanceRange().getTransmittance()
                    .getValue());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getTransmittanceRangeTransmittance()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Filter+/TransmittanceRange
    public Integer getTransmittanceRangeCutOut(int idxInstrument, int idxFilter) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyFilter().get(idxFilter)
                    .getTransmittanceRange().getCutOut().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getTransmittanceRangeCutOut()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Filter+/TransmittanceRange
    public Integer getTransmittanceRangeCutInTolerance(int idxInstrument,
            int idxFilter) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyFilter().get(idxFilter)
                    .getTransmittanceRange().getCutInTolerance().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getTransmittanceRangeCutInTolerance()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/Filter+/TransmittanceRange
    public Integer getTransmittanceRangeCutOutTolerance(int idxInstrument,
            int idxFilter) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyFilter().get(idxFilter)
                    .getTransmittanceRange().getCutOutTolerance().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getTransmittanceRangeCutOutTolerance()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Projection property retrieval -

    // - Image property retrieval -

    // Image+
    public String getImageExperimentRef(int idxImage) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(imageList.get(idxImage).getExperiment());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getImageExperimentRef()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+
    public String getImageDescription(int idxImage) {
        Exception e = null;
        String rv = null;
        try {
            rv = imageList.get(idxImage).getDescription().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getImageDescription()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+
    public String getImageExperimenterRef(int idxImage) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(imageList.get(idxImage).getDetails().getOwner());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getImageExperimenterRef()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+
    public String getImageGroupRef(int idxImage) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(imageList.get(idxImage).getDetails().getGroup());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getImageGroupRef()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+
    public String getImageInstrumentRef(int idxImage) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(imageList.get(idxImage).getInstrument());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getImageInstrumentRef()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+
    public String getImageDefaultPixels(int idxImage) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(imageList.get(idxImage).getPrimaryPixels());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getImageDefaultPixels()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+
    public String getImageAcquiredPixels(int idxImage) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getImageAcquiredPixels()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+
    public String getImageObjective(int idxImage) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(imageList.get(idxImage).getObjectiveSettings()
                    .getObjective());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getImageObjective()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+
    public String getImageCreationDate(int idxImage) {
        Exception e = null;
        String rv = null;
        try {
            rv = millis2time(imageList.get(idxImage).getAcquisitionDate()
                    .getValue());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getImageCreationDate()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+
    public String getImageID(int idxImage) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(imageList.get(idxImage));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getImageID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+
    public String getImageName(int idxImage) {
        Exception e = null;
        String rv = null;
        try {
            rv = imageList.get(idxImage).getName().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getImageName()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - ROI property retrieval -

    // Image+/DisplayOptions/ROI+
    public Integer getROIT0(int idxImage, int idxROI) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getROIT0()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+
    public Integer getROIT1(int idxImage, int idxROI) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getROIT1()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+
    public Integer getROIY1(int idxImage, int idxROI) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getROIY1()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+
    public Integer getROIY0(int idxImage, int idxROI) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getROIY0()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+
    public Integer getROIX0(int idxImage, int idxROI) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getROIX0()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+
    public Integer getROIX1(int idxImage, int idxROI) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getROIX1()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+
    public Integer getROIZ0(int idxImage, int idxROI) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getROIZ0()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+
    public Integer getROIZ1(int idxImage, int idxROI) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getROIZ1()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+
    public String getROIID(int idxImage, int idxROI) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(imageList.get(idxImage).copyRois().get(idxROI));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getROIID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Shape property retrieval -

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public String getShapeTextAnchor(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeTextAnchor()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public String getShapeVectorEffect(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = imageList.get(idxImage).copyRois().get(idxROI).copyShapes()
                    .get(idxShape).getVectorEffect().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeVectorEffect()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public String getShapeFontStyle(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeFontStyle()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public String getShapeStrokeLineJoin(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = imageList.get(idxImage).copyRois().get(idxROI).copyShapes()
                    .get(idxShape).getStrokeLineJoin().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeStrokeLineJoin()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public String getShapeBaselineShift(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeBaselineShift()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public Boolean getShapeVisibility(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        Boolean rv = null;
        try {
            rv = imageList.get(idxImage).copyRois().get(idxROI).copyShapes()
                    .get(idxShape).getVisibility().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeVisibility()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public Integer getShapeTheT(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = imageList.get(idxImage).copyRois().get(idxROI).copyShapes()
                    .get(idxShape).getTheT().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeTheT()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public String getShapeFontStretch(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeFontStretch()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public String getShapeStrokeLineCap(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = imageList.get(idxImage).copyRois().get(idxROI).copyShapes()
                    .get(idxShape).getStrokeLineCap().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeStrokeLineCap()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public String getShapeStrokeDashArray(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = imageList.get(idxImage).copyRois().get(idxROI).copyShapes()
                    .get(idxShape).getStrokeDashArray().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeStrokeDashArray()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public String getShapeDirection(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeDirection()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public Integer getShapeGlyphOrientationVertical(int idxImage, int idxROI,
            int idxShape) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeGlyphOrientationVertical()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public String getShapeFontVariant(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeFontVariant()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public Double getShapeStrokeOpacity(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        Double rv = null;
        try {
            rv = new Double(imageList.get(idxImage).copyRois().get(idxROI).copyShapes()
                    .get(idxShape).getStrokeOpacity().getValue());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeStrokeOpacity()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public String getShapeFontFamily(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeFontFamily()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public String getShapeFillRule(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = imageList.get(idxImage).copyRois().get(idxROI).copyShapes()
                    .get(idxShape).getFillRule().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeFillRule()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public String getShapeStrokeAttribute(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeStrokeAttribute()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public Boolean getShapeLocked(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        Boolean rv = null;
        try {
            rv = imageList.get(idxImage).copyRois().get(idxROI).copyShapes()
                    .get(idxShape).getLocked().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeLocked()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public String getShapeTextDecoration(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeTextDecoration()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public String getShapeG(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = imageList.get(idxImage).copyRois().get(idxROI).copyShapes()
                    .get(idxShape).getG().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeG()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public String getShapeWritingMode(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeWritingMode()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public Integer getShapeFontSize(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeFontSize()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public String getShapeFillColor(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = imageList.get(idxImage).copyRois().get(idxROI).copyShapes()
                    .get(idxShape).getFillColor().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeFillColor()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public String getShapeID(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(imageList.get(idxImage).copyRois().get(idxROI)
                    .copyShapes().get(idxShape));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public Integer getShapeStrokeWidth(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = imageList.get(idxImage).copyRois().get(idxROI).copyShapes()
                    .get(idxShape).getStrokeWidth().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeStrokeWidth()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public String getShapeTextStroke(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeTextStroke()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public Integer getShapeStrokeMiterLimit(int idxImage, int idxROI,
            int idxShape) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = imageList.get(idxImage).copyRois().get(idxROI).copyShapes()
                    .get(idxShape).getStrokeMiterLimit().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeStrokeMiterLimit()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public String getShapeText(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeText()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public String getShapeTextFill(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeTextFill()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public String getShapeFontWeight(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeFontWeight()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public String getShapeFillOpacity(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = flt2str(imageList.get(idxImage).copyRois().get(idxROI)
                    .copyShapes().get(idxShape).getFillOpacity().getValue());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeFillOpacity()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public String getShapeStrokeColor(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = imageList.get(idxImage).copyRois().get(idxROI).copyShapes()
                    .get(idxShape).getStrokeColor().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeStrokeColor()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+
    public Integer getShapeTheZ(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = imageList.get(idxImage).copyRois().get(idxROI).copyShapes()
                    .get(idxShape).getTheZ().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getShapeTheZ()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - FilterSpec property retrieval -

    // - Contact property retrieval -

    // Group+/Contact
    public String getContactExperimenter(int idxGroup) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getContactExperimenter()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Polygon property retrieval -

    // Image+/DisplayOptions/ROI+/Union/Shape+/Polygon
    public String getPolygonPoints(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = ((Polygon) imageList.get(idxImage).copyRois().get(idxROI)
                    .copyShapes().get(idxShape)).getPoints().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPolygonPoints()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Polygon
    public String getPolygonID(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid((Polygon) imageList.get(idxImage).copyRois().get(
                    idxROI).copyShapes().get(idxShape));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPolygonID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Polygon
    public String getPolygonTransform(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = ((Polygon) imageList.get(idxImage).copyRois().get(idxROI)
                    .copyShapes().get(idxShape)).getTransform().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPolygonTransform()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Mask property retrieval -

    // Image+/DisplayOptions/ROI+/Union/Shape+/Mask
    public String getMaskTransform(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getMaskTransform()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Mask
    public String getMaskHeight(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getMaskHeight()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Mask
    public String getMaskWidth(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getMaskWidth()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Mask
    public String getMaskY(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getMaskY()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Mask
    public String getMaskX(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getMaskX()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Mask
    public String getMaskID(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getMaskID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - StageLabel property retrieval -

    // Image+/StageLabel
    public Double getStageLabelY(int idxImage) {
        Exception e = null;
        Double rv = null;
        try {
            rv = imageList.get(idxImage).getStageLabel().getPositionY()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getStageLabelY()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/StageLabel
    public Double getStageLabelX(int idxImage) {
        Exception e = null;
        Double rv = null;
        try {
            rv = imageList.get(idxImage).getStageLabel().getPositionX()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getStageLabelX()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/StageLabel
    public Double getStageLabelZ(int idxImage) {
        Exception e = null;
        Double rv = null;
        try {
            rv = imageList.get(idxImage).getStageLabel().getPositionZ()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getStageLabelZ()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/StageLabel
    public String getStageLabelName(int idxImage) {
        Exception e = null;
        String rv = null;
        try {
            rv = imageList.get(idxImage).getStageLabel().getName().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getStageLabelName()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - TiffData property retrieval -

    // Image+/Pixels+/TiffData+
    public String getTiffDataUUID(int idxImage, int idxPixels, int idxTiffData) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getTiffDataUUID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Pixels+/TiffData+
    public Integer getTiffDataNumPlanes(int idxImage, int idxPixels,
            int idxTiffData) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getTiffDataNumPlanes()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Pixels+/TiffData+
    public Integer getTiffDataFirstC(int idxImage, int idxPixels,
            int idxTiffData) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getTiffDataFirstC()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Pixels+/TiffData+
    public String getTiffDataFileName(int idxImage, int idxPixels,
            int idxTiffData) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getTiffDataFileName()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Pixels+/TiffData+
    public Integer getTiffDataIFD(int idxImage, int idxPixels, int idxTiffData) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getTiffDataIFD()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Pixels+/TiffData+
    public Integer getTiffDataFirstZ(int idxImage, int idxPixels,
            int idxTiffData) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getTiffDataFirstZ()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Pixels+/TiffData+
    public Integer getTiffDataFirstT(int idxImage, int idxPixels,
            int idxTiffData) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getTiffDataFirstT()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - DisplayOptions property retrieval -

    // Image+/DisplayOptions
    public String getDisplayOptionsID(int idxImage) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getDisplayOptionsID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions
    public String getDisplayOptionsDisplay(int idxImage) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getDisplayOptionsDisplay()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions
    public Double getDisplayOptionsZoom(int idxImage) {
        Exception e = null;
        Double rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getDisplayOptionsZoom()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - FilterSet property retrieval -

    // Instrument+/FilterSet+
    public String getFilterSetDichroic(int idxInstrument, int idxFilterSet) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(instrumentList.get(idxInstrument).copyFilterSet()
                    .get(idxFilterSet).getDichroic());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getFilterSetDichroic()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/FilterSet+
    public String getFilterSetExFilter(int idxInstrument, int idxFilterSet) {
        Exception e = null;
        String rv = null;
        try {
            /* ticket:1750 - to be fixed by chris
            rv = handleLsid(instrumentList.get(idxInstrument).copyFilterSet()
                    .get(idxFilterSet).getExFilter());
            */
            throw new RuntimeException("TBD");
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getFilterSetExFilter()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/FilterSet+
    public String getFilterSetLotNumber(int idxInstrument, int idxFilterSet) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyFilterSet().get(
                    idxFilterSet).getLotNumber().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getFilterSetLotNumber()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/FilterSet+
    public String getFilterSetEmFilter(int idxInstrument, int idxFilterSet) {
        Exception e = null;
        String rv = null;
        try {
            /* ticket:1750 - to be handled by chris
            rv = handleLsid(instrumentList.get(idxInstrument).copyFilterSet()
                    .get(idxFilterSet).getEmFilter());
            */
            throw new RuntimeException("TBD");
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getFilterSetEmFilter()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/FilterSet+
    public String getFilterSetModel(int idxInstrument, int idxFilterSet) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyFilterSet().get(
                    idxFilterSet).getModel().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getFilterSetModel()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/FilterSet+
    public String getFilterSetManufacturer(int idxInstrument, int idxFilterSet) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyFilterSet().get(
                    idxFilterSet).getManufacturer().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getFilterSetManufacturer()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - LightEmittingDiode property retrieval -

    // - Well property retrieval -

    // Plate+/Well+
    public String getWellExternalIdentifier(int idxPlate, int idxWell) {
        Exception e = null;
        String rv = null;
        try {
            rv = plateList.get(idxPlate).copyWells().get(idxWell)
                    .getExternalIdentifier().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getWellExternalIdentifier()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Plate+/Well+
    public Integer getWellColumn(int idxPlate, int idxWell) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = plateList.get(idxPlate).copyWells().get(idxWell).getColumn()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getWellColumn()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Plate+/Well+
    public String getWellExternalDescription(int idxPlate, int idxWell) {
        Exception e = null;
        String rv = null;
        try {
            rv = plateList.get(idxPlate).copyWells().get(idxWell)
                    .getExternalDescription().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getWellExternalDescription()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Plate+/Well+
    public String getWellReagent(int idxPlate, int idxWell) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getWellReagent()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Plate+/Well+
    public String getWellType(int idxPlate, int idxWell) {
        Exception e = null;
        String rv = null;
        try {
            rv = plateList.get(idxPlate).copyWells().get(idxWell).getType()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getWellType()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Plate+/Well+
    public String getWellID(int idxPlate, int idxWell) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(plateList.get(idxPlate).copyWells().get(idxWell));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getWellID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Plate+/Well+
    public Integer getWellRow(int idxPlate, int idxWell) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = plateList.get(idxPlate).copyWells().get(idxWell).getRow()
                    .getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getWellRow()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - Plane property retrieval -

    // Image+/Pixels+/Plane+
    public String getPlaneID(int idxImage, int idxPixels, int idxPlane) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(imageList.get(idxImage).copyPixels().get(idxPixels)
                    .copyPlaneInfo().get(idxPlane));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPlaneID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Pixels+/Plane+
    public Integer getPlaneTheT(int idxImage, int idxPixels, int idxPlane) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = imageList.get(idxImage).copyPixels().get(idxPixels)
                    .copyPlaneInfo().get(idxPlane).getTheT().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPlaneTheT()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Pixels+/Plane+
    public Integer getPlaneTheC(int idxImage, int idxPixels, int idxPlane) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = imageList.get(idxImage).copyPixels().get(idxPixels)
                    .copyPlaneInfo().get(idxPlane).getTheC().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPlaneTheC()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Pixels+/Plane+
    public Integer getPlaneTheZ(int idxImage, int idxPixels, int idxPlane) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = imageList.get(idxImage).copyPixels().get(idxPixels)
                    .copyPlaneInfo().get(idxPlane).getTheZ().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPlaneTheZ()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/Pixels+/Plane+
    public String getPlaneHashSHA1(int idxImage, int idxPixels, int idxPlane) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getPlaneHashSHA1()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - UUID property retrieval -

    // - ReagentRef property retrieval -

    // - Line property retrieval -

    // Image+/DisplayOptions/ROI+/Union/Shape+/Line
    public String getLineX1(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = dbl2str(((Line) imageList.get(idxImage).copyRois().get(idxROI)
                    .copyShapes().get(idxShape)).getX1().getValue());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLineX1()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Line
    public String getLineTransform(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = ((Line) imageList.get(idxImage).copyRois().get(idxROI)
                    .copyShapes().get(idxShape)).getTransform().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLineTransform()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Line
    public String getLineY1(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = dbl2str(((Line) imageList.get(idxImage).copyRois().get(idxROI)
                    .copyShapes().get(idxShape)).getY1().getValue());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLineY1()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Line
    public String getLineX2(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = dbl2str(((Line) imageList.get(idxImage).copyRois().get(idxROI)
                    .copyShapes().get(idxShape)).getX2().getValue());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLineX2()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Line
    public String getLineY2(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = dbl2str(((Line) imageList.get(idxImage).copyRois().get(idxROI)
                    .copyShapes().get(idxShape)).getY2().getValue());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLineY2()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Image+/DisplayOptions/ROI+/Union/Shape+/Line
    public String getLineID(int idxImage, int idxROI, int idxShape) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid((Line) imageList.get(idxImage).copyRois().get(
                    idxROI).copyShapes().get(idxShape));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getLineID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - OTF property retrieval -

    // Instrument+/OTF+
    public String getOTFPixelType(int idxInstrument, int idxOTF) {
        Exception e = null;
        String rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyOtf().get(idxOTF)
                    .getPixelsType().getValue().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getOTFPixelType()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/OTF+
    public Integer getOTFSizeX(int idxInstrument, int idxOTF) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyOtf().get(idxOTF)
                    .getSizeX().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getOTFSizeX()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/OTF+
    public Integer getOTFSizeY(int idxInstrument, int idxOTF) {
        Exception e = null;
        Integer rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyOtf().get(idxOTF)
                    .getSizeY().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getOTFSizeY()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/OTF+
    public Boolean getOTFOpticalAxisAveraged(int idxInstrument, int idxOTF) {
        Exception e = null;
        Boolean rv = null;
        try {
            rv = instrumentList.get(idxInstrument).copyOtf().get(idxOTF)
                    .getOpticalAxisAveraged().getValue();
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getOTFOpticalAxisAveraged()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/OTF+
    public String getOTFObjective(int idxInstrument, int idxOTF) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(instrumentList.get(idxInstrument).copyOtf().get(
                    idxOTF).getObjective());
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getOTFObjective()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/OTF+
    public String getOTFBinaryFile(int idxInstrument, int idxOTF) {
        Exception e = null;
        String rv = null;
        try {
            rv = null;
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getOTFBinaryFile()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // Instrument+/OTF+
    public String getOTFID(int idxInstrument, int idxOTF) {
        Exception e = null;
        String rv = null;
        try {
            rv = handleLsid(instrumentList.get(idxInstrument).copyOtf().get(
                    idxOTF));
        } catch (NullPointerException npe) {
            e = npe;
            rv = null;
        } catch (omero.ClientError ce) {
            e = ce;
            rv = null;
        } catch (IndexOutOfBoundsException iob) {
            e = iob;
            rv = null;
        } finally {
            if (log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("getOTFID()==");
                sb.append(rv);
                if (e != null) {
                    sb.append("(");
                    sb.append(e.getClass().getSimpleName());
                    sb.append(")");
                }
                log.debug(sb.toString());
            }
        }
        return rv;
    }

    // - ExperimenterRef property retrieval -

    public String getDetectorSettingsBinning(int imageIndex,
            int logicalChannelIndex) {
        // Currently non-generated method
        return null;
    }

    public String getDetectorSettingsDetector(int imageIndex,
            int logicalChannelIndex) {
        // Currently non-generated method
        return null;
    }

    public Double getDetectorSettingsGain(int imageIndex, int logicalChannelIndex) {
        // Currently non-generated method
        return null;
    }

    public Double getDetectorSettingsOffset(int imageIndex,
            int logicalChannelIndex) {
        // Currently non-generated method
        return null;
    }

    public Double getDetectorSettingsReadOutRate(int imageIndex,
            int logicalChannelIndex) {
        // Currently non-generated method
        return null;
    }

    public Double getDetectorSettingsVoltage(int imageIndex,
            int logicalChannelIndex) {
        // Currently non-generated method
        return null;
    }

    public Double getDimensionsPhysicalSizeX(int imageIndex, int pixelsIndex) {
        // Currently non-generated method
        return null;
    }

    public Double getDimensionsPhysicalSizeY(int imageIndex, int pixelsIndex) {
        // Currently non-generated method
        return null;
    }

    public Double getDimensionsPhysicalSizeZ(int imageIndex, int pixelsIndex) {
        // Currently non-generated method
        return null;
    }

    public Double getDimensionsTimeIncrement(int imageIndex, int pixelsIndex) {
        // Currently non-generated method
        return null;
    }

    public Integer getDimensionsWaveIncrement(int imageIndex, int pixelsIndex) {
        // Currently non-generated method
        return null;
    }

    public Integer getDimensionsWaveStart(int imageIndex, int pixelsIndex) {
        // Currently non-generated method
        return null;
    }

    public Integer getDisplayOptionsProjectionZStart(int imageIndex) {
        // Currently non-generated method
        return null;
    }

    public Integer getDisplayOptionsProjectionZStop(int imageIndex) {
        // Currently non-generated method
        return null;
    }

    public Integer getDisplayOptionsTimeTStart(int imageIndex) {
        // Currently non-generated method
        return null;
    }

    public Integer getDisplayOptionsTimeTStop(int imageIndex) {
        // Currently non-generated method
        return null;
    }

    public String getEmFilterLotNumber(int instrumentIndex, int filterIndex) {
        // Currently non-generated method
        return null;
    }

    public String getEmFilterManufacturer(int instrumentIndex, int filterIndex) {
        // Currently non-generated method
        return null;
    }

    public String getEmFilterModel(int instrumentIndex, int filterIndex) {
        // Currently non-generated method
        return null;
    }

    public String getEmFilterType(int instrumentIndex, int filterIndex) {
        // Currently non-generated method
        return null;
    }

    public String getExFilterLotNumber(int instrumentIndex, int filterIndex) {
        // Currently non-generated method
        return null;
    }

    public String getExFilterManufacturer(int instrumentIndex, int filterIndex) {
        // Currently non-generated method
        return null;
    }

    public String getExFilterModel(int instrumentIndex, int filterIndex) {
        // Currently non-generated method
        return null;
    }

    public String getExFilterType(int instrumentIndex, int filterIndex) {
        // Currently non-generated method
        return null;
    }

    public int getExperimenterMembershipCount(int experimenterIndex) {
        // Currently non-generated method
        return 0;
    }

    public String getExperimenterMembershipGroup(int experimenterIndex,
            int groupRefIndex) {
        // Currently non-generated method
        return null;
    }

    public String getGreyChannelMapColorMap(int imageIndex) {
        // Currently non-generated method
        return null;
    }

    public Double getLightSourceSettingsAttenuation(int imageIndex,
            int logicalChannelIndex) {
        // Currently non-generated method
        return null;
    }

    public String getLightSourceSettingsLightSource(int imageIndex,
            int logicalChannelIndex) {
        // Currently non-generated method
        return null;
    }

    public Integer getLightSourceSettingsWavelength(int imageIndex,
            int logicalChannelIndex) {
        // Currently non-generated method
        return null;
    }

    public Double getObjectiveSettingsCorrectionCollar(int imageIndex) {
        // Currently non-generated method
        return null;
    }

    public String getObjectiveSettingsMedium(int imageIndex) {
        // Currently non-generated method
        return null;
    }

    public String getObjectiveSettingsObjective(int imageIndex) {
        // Currently non-generated method
        return null;
    }

    public Double getObjectiveSettingsRefractiveIndex(int imageIndex) {
        // Currently non-generated method
        return null;
    }

    public String getPathD(int imageIndex, int roiIndex, int shapeIndex) {
        // Currently non-generated method
        return null;
    }

    public String getPathID(int imageIndex, int roiIndex, int shapeIndex) {
        // Currently non-generated method
        return null;
    }

    public String getDichroicID(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getFilterID(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getFilterSetID(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    // Methods from bf upgrade(s);
    
    public int getRoiLinkCount(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getRoiLinkDirection(int arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getRoiLinkName(int arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getRoiLinkRef(int arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getGroupID(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

}
