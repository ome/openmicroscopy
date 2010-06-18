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
import ome.xml.model.enums.AcquisitionMode;
import ome.xml.model.enums.ArcType;
import ome.xml.model.enums.Binning;
import ome.xml.model.enums.ContrastMethod;
import ome.xml.model.enums.Correction;
import ome.xml.model.enums.DetectorType;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.ExperimentType;
import ome.xml.model.enums.FilamentType;
import ome.xml.model.enums.FilterType;
import ome.xml.model.enums.IlluminationType;
import ome.xml.model.enums.Immersion;
import ome.xml.model.enums.LaserMedium;
import ome.xml.model.enums.LaserType;
import ome.xml.model.enums.Medium;
import ome.xml.model.enums.MicrobeamManipulationType;
import ome.xml.model.enums.MicroscopeType;
import ome.xml.model.enums.NamingConvention;
import ome.xml.model.enums.PixelType;
import ome.xml.model.enums.Pulse;
import ome.xml.model.primitives.NonNegativeInteger;
import ome.xml.model.primitives.NonNegativeLong;
import ome.xml.model.primitives.PercentFraction;
import ome.xml.model.primitives.PositiveInteger;
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

        public int getPixelsBinDataCount(int imageIndex)
        {
		// TODO Auto-generated method stub
		return 0;
        }

	public String getArcID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getArcLotNumber(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getArcManufacturer(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getArcModel(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getArcPower(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getArcSerialNumber(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public ArcType getArcType(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getBooleanAnnotationCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getBooleanAnnotationID(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getBooleanAnnotationNamespace(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public Boolean getBooleanAnnotationValue(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public AcquisitionMode getChannelAcquisitionMode(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getChannelAnnotationRef(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getChannelAnnotationRefCount(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}


	public Integer getChannelColor(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public ContrastMethod getChannelContrastMethod(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getChannelCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public PositiveInteger getChannelEmissionWavelength(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public PositiveInteger getChannelExcitationWavelength(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getChannelFilterSetRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getChannelFluor(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getChannelID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public IlluminationType getChannelIlluminationType(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public PercentFraction getChannelLightSourceSettingsAttenuation(int arg0,
			int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getChannelLightSourceSettingsID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public PositiveInteger getChannelLightSourceSettingsWavelength(int arg0,
			int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getChannelNDFilter(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getChannelName(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getChannelOTFRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getChannelPinholeSize(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Integer getChannelPockelCellSetting(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public PositiveInteger getChannelSamplesPerPixel(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getDatasetAnnotationRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getDatasetAnnotationRefCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public int getDatasetCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getDatasetDescription(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getDatasetExperimenterRef(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getDatasetGroupRef(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getDatasetID(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getDatasetName(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getDatasetProjectRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getDatasetRefCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public Double getDetectorAmplificationGain(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getDetectorCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public Double getDetectorGain(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getDetectorID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getDetectorLotNumber(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getDetectorManufacturer(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getDetectorModel(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getDetectorOffset(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getDetectorSerialNumber(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Binning getDetectorSettingsBinning(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getDetectorSettingsGain(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getDetectorSettingsID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getDetectorSettingsOffset(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getDetectorSettingsReadOutRate(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getDetectorSettingsVoltage(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public DetectorType getDetectorType(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getDetectorVoltage(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getDetectorZoom(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getDichroicCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getDichroicID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getDichroicLotNumber(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getDichroicManufacturer(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getDichroicModel(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getDichroicSerialNumber(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getDoubleAnnotationCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getDoubleAnnotationID(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getDoubleAnnotationNamespace(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getDoubleAnnotationValue(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getEllipseDescription(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Integer getEllipseFill(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getEllipseFontSize(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getEllipseID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getEllipseLabel(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getEllipseName(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getEllipseRadiusX(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getEllipseRadiusY(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Integer getEllipseStroke(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getEllipseStrokeDashArray(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getEllipseStrokeWidth(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getEllipseTheC(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getEllipseTheT(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getEllipseTheZ(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getEllipseTransform(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getEllipseX(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getEllipseY(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getExperimentCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getExperimentDescription(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getExperimentExperimenterRef(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getExperimentID(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public ExperimentType getExperimentType(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getExperimenterAnnotationRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getExperimenterAnnotationRefCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public int getExperimenterCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getExperimenterDisplayName(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getExperimenterEmail(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getExperimenterFirstName(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getExperimenterGroupRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getExperimenterGroupRefCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getExperimenterID(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getExperimenterInstitution(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getExperimenterLastName(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getExperimenterMiddleName(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getExperimenterUserName(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getFilamentID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getFilamentLotNumber(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getFilamentManufacturer(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getFilamentModel(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getFilamentPower(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getFilamentSerialNumber(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public FilamentType getFilamentType(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getFileAnnotationBinaryFileFileName(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getFileAnnotationBinaryFileMIMEType(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeLong getFileAnnotationBinaryFileSize(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getFileAnnotationCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getFileAnnotationID(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getFileAnnotationNamespace(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getFilterCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getFilterFilterWheel(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getFilterID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getFilterLotNumber(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getFilterManufacturer(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getFilterModel(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getFilterSerialNumber(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getFilterSetCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getFilterSetDichroicRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getFilterSetEmissionFilterRef(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getFilterSetEmissionFilterRefCount(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getFilterSetExcitationFilterRef(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getFilterSetExcitationFilterRefCount(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getFilterSetID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getFilterSetLotNumber(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getFilterSetManufacturer(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getFilterSetModel(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getFilterSetSerialNumber(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public FilterType getFilterType(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getGroupContact(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getGroupCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getGroupDescription(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getGroupID(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getGroupLeader(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getGroupName(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getImageAcquiredDate(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getImageAnnotationRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getImageAnnotationRefCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public int getImageCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getImageDatasetRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getImageDescription(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getImageExperimentRef(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getImageExperimenterRef(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getImageGroupRef(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getImageID(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getImageInstrumentRef(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getImageMicrobeamManipulationRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getImageName(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getImageObjectiveSettingsCorrectionCollar(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getImageObjectiveSettingsID(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public Medium getImageObjectiveSettingsMedium(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getImageObjectiveSettingsRefractiveIndex(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getImageROIRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getImageROIRefCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public Double getImagingEnvironmentAirPressure(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public PercentFraction getImagingEnvironmentCO2Percent(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public PercentFraction getImagingEnvironmentHumidity(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getImagingEnvironmentTemperature(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getInstrumentCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getInstrumentID(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public PositiveInteger getLaserFrequencyMultiplication(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getLaserID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public LaserMedium getLaserLaserMedium(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getLaserLotNumber(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getLaserManufacturer(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getLaserModel(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Boolean getLaserPockelCell(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getLaserPower(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Pulse getLaserPulse(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getLaserPump(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getLaserRepetitionRate(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getLaserSerialNumber(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Boolean getLaserTuneable(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public LaserType getLaserType(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public PositiveInteger getLaserWavelength(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getLightEmittingDiodeID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getLightEmittingDiodeLotNumber(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getLightEmittingDiodeManufacturer(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getLightEmittingDiodeModel(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getLightEmittingDiodePower(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getLightEmittingDiodeSerialNumber(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getLightPathDichroicRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getLightPathEmissionFilterRef(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getLightPathEmissionFilterRefCount(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getLightPathExcitationFilterRef(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getLightPathExcitationFilterRefCount(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getLineDescription(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Integer getLineFill(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getLineFontSize(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getLineID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getLineLabel(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getLineName(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Integer getLineStroke(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getLineStrokeDashArray(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getLineStrokeWidth(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getLineTheC(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getLineTheT(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getLineTheZ(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getLineTransform(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getLineX1(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getLineX2(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getLineY1(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getLineY2(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getListAnnotationAnnotationRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getListAnnotationAnnotationRefCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public int getListAnnotationCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getListAnnotationID(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getListAnnotationNamespace(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getLongAnnotationCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getLongAnnotationID(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getLongAnnotationNamespace(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public Long getLongAnnotationValue(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getMaskDescription(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Integer getMaskFill(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getMaskFontSize(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getMaskID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getMaskLabel(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getMaskName(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Integer getMaskStroke(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getMaskStrokeDashArray(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getMaskStrokeWidth(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getMaskTheC(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getMaskTheT(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getMaskTheZ(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getMaskTransform(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getMaskX(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getMaskY(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getMicrobeamManipulationCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getMicrobeamManipulationExperimenterRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getMicrobeamManipulationID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public PercentFraction getMicrobeamManipulationLightSourceSettingsAttenuation(
			int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getMicrobeamManipulationLightSourceSettingsCount(int arg0,
			int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getMicrobeamManipulationLightSourceSettingsID(int arg0,
			int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}


	public PositiveInteger getMicrobeamManipulationLightSourceSettingsWavelength(
			int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getMicrobeamManipulationROIRef(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getMicrobeamManipulationROIRefCount(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}


	public int getMicrobeamManipulationRefCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public MicrobeamManipulationType getMicrobeamManipulationType(int arg0,
			int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getMicroscopeLotNumber(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getMicroscopeManufacturer(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getMicroscopeModel(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getMicroscopeSerialNumber(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public MicroscopeType getMicroscopeType(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getOTFBinaryFileFileName(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getOTFBinaryFileMIMEType(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeLong getOTFBinaryFileSize(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getOTFCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getOTFFilterSetRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getOTFID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getOTFObjectiveSettingsCorrectionCollar(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getOTFObjectiveSettingsID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Medium getOTFObjectiveSettingsMedium(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getOTFObjectiveSettingsRefractiveIndex(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Boolean getOTFOpticalAxisAveraged(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public PositiveInteger getOTFSizeX(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public PositiveInteger getOTFSizeY(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public PixelType getOTFType(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getObjectiveCalibratedMagnification(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Correction getObjectiveCorrection(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getObjectiveCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getObjectiveID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Immersion getObjectiveImmersion(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Boolean getObjectiveIris(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getObjectiveLensNA(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getObjectiveLotNumber(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getObjectiveManufacturer(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getObjectiveModel(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public PositiveInteger getObjectiveNominalMagnification(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getObjectiveSerialNumber(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getObjectiveWorkingDistance(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPathDefinition(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPathDescription(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Integer getPathFill(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getPathFontSize(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPathID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPathLabel(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPathName(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Integer getPathStroke(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPathStrokeDashArray(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getPathStrokeWidth(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getPathTheC(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getPathTheT(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getPathTheZ(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPathTransform(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPixelsAnnotationRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getPixelsAnnotationRefCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public Boolean getPixelsBinDataBigEndian(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public DimensionOrder getPixelsDimensionOrder(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPixelsID(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getPixelsPhysicalSizeX(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getPixelsPhysicalSizeY(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getPixelsPhysicalSizeZ(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public PositiveInteger getPixelsSizeC(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public PositiveInteger getPixelsSizeT(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public PositiveInteger getPixelsSizeX(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public PositiveInteger getPixelsSizeY(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public PositiveInteger getPixelsSizeZ(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getPixelsTimeIncrement(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public PixelType getPixelsType(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPlaneAnnotationRef(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getPlaneAnnotationRefCount(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}


	public int getPlaneCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public Double getPlaneDeltaT(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getPlaneExposureTime(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPlaneHashSHA1(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getPlanePositionX(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getPlanePositionY(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getPlanePositionZ(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getPlaneTheC(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getPlaneTheT(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getPlaneTheZ(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPlateAcquisitionAnnotationRef(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getPlateAcquisitionAnnotationRefCount(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}


	public int getPlateAcquisitionCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getPlateAcquisitionDescription(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPlateAcquisitionEndTime(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPlateAcquisitionID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public PositiveInteger getPlateAcquisitionMaximumFieldCount(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPlateAcquisitionName(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPlateAcquisitionStartTime(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPlateAcquisitionWellSampleRef(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPlateAnnotationRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getPlateAnnotationRefCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public NamingConvention getPlateColumnNamingConvention(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public PositiveInteger getPlateColumns(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getPlateCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getPlateDescription(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPlateExternalIdentifier(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPlateID(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPlateName(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getPlateRefCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public NamingConvention getPlateRowNamingConvention(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public PositiveInteger getPlateRows(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPlateScreenRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPlateStatus(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getPlateWellOriginX(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getPlateWellOriginY(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPointDescription(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Integer getPointFill(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getPointFontSize(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPointID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPointLabel(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPointName(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Integer getPointStroke(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPointStrokeDashArray(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getPointStrokeWidth(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getPointTheC(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getPointTheT(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getPointTheZ(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPointTransform(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getPointX(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getPointY(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Boolean getPolylineClosed(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPolylineDescription(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Integer getPolylineFill(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getPolylineFontSize(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPolylineID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPolylineLabel(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPolylineName(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPolylinePoints(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Integer getPolylineStroke(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPolylineStrokeDashArray(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getPolylineStrokeWidth(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getPolylineTheC(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getPolylineTheT(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getPolylineTheZ(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getPolylineTransform(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getProjectAnnotationRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getProjectAnnotationRefCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public int getProjectCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getProjectDescription(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getProjectExperimenterRef(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getProjectGroupRef(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getProjectID(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getProjectName(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getProjectRefCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getROIAnnotationRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getROIAnnotationRefCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public int getROICount() {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getROIDescription(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getROIID(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getROIName(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getROINamespace(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getReagentAnnotationRef(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getReagentAnnotationRefCount(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}


	public int getReagentCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getReagentDescription(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getReagentID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getReagentName(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getReagentReagentIdentifier(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getRectangleDescription(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Integer getRectangleFill(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getRectangleFontSize(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getRectangleHeight(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getRectangleID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getRectangleLabel(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getRectangleName(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Integer getRectangleStroke(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getRectangleStrokeDashArray(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getRectangleStrokeWidth(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getRectangleTheC(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getRectangleTheT(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getRectangleTheZ(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getRectangleTransform(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getRectangleWidth(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getRectangleX(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getRectangleY(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getScreenAnnotationRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getScreenAnnotationRefCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public int getScreenCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getScreenDescription(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getScreenID(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getScreenName(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getScreenPlateRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getScreenProtocolDescription(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getScreenProtocolIdentifier(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getScreenReagentSetDescription(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getScreenReagentSetIdentifier(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getScreenRefCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getScreenType(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getShapeAnnotationRefCount(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getStageLabelName(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getStageLabelX(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getStageLabelY(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getStageLabelZ(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getCommentAnnotationCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getCommentAnnotationID(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getCommentAnnotationNamespace(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getCommentAnnotationValue(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getTextDescription(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Integer getTextFill(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getTextFontSize(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getTextID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getTextLabel(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getTextName(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Integer getTextStroke(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getTextStrokeDashArray(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getTextStrokeWidth(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getTextTheC(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getTextTheT(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getTextTheZ(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getTextTransform(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getTextValue(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getTextX(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getTextY(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getTiffDataCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public NonNegativeInteger getTiffDataFirstC(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getTiffDataFirstT(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getTiffDataFirstZ(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getTiffDataIFD(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getTiffDataPlaneCount(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getTimestampAnnotationCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getTimestampAnnotationID(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getTimestampAnnotationNamespace(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getTimestampAnnotationValue(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public PositiveInteger getTransmittanceRangeCutIn(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getTransmittanceRangeCutInTolerance(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public PositiveInteger getTransmittanceRangeCutOut(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getTransmittanceRangeCutOutTolerance(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public PercentFraction getTransmittanceRangeTransmittance(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getUUIDValue(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUUIDFileName(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getWellAnnotationRef(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getWellAnnotationRefCount(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}


	public Integer getWellColor(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getWellColumn(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getWellCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getWellExternalDescription(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getWellExternalIdentifier(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getWellID(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getWellReagentRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getWellRow(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getWellSampleAnnotationRef(int arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getWellSampleAnnotationRefCount(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return 0;
	}


	public int getWellSampleCount(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getWellSampleID(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getWellSampleImageRef(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}


	public NonNegativeInteger getWellSampleIndex(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getWellSamplePositionX(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getWellSamplePositionY(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getWellSampleRefCount(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getWellSampleTimepoint(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getWellStatus(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getXMLAnnotationCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getXMLAnnotationID(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getXMLAnnotationNamespace(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getXMLAnnotationValue(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getBooleanAnnotationAnnotationRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getBooleanAnnotationDescription(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getCommentAnnotationAnnotationRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getCommentAnnotationDescription(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getDoubleAnnotationAnnotationRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getDoubleAnnotationDescription(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getFileAnnotationAnnotationRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getFileAnnotationDescription(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getLightSourceCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getListAnnotationDescription(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getLongAnnotationAnnotationRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getLongAnnotationDescription(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getMaskHeight(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public Double getMaskWidth(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getShapeCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getTagAnnotationAnnotationRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getTagAnnotationCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getTagAnnotationDescription(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getTagAnnotationID(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getTagAnnotationNamespace(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getTagAnnotationValue(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getTermAnnotationAnnotationRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public int getTermAnnotationCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getTermAnnotationDescription(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getTermAnnotationID(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getTermAnnotationNamespace(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getTermAnnotationValue(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getTimestampAnnotationAnnotationRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getTimestampAnnotationDescription(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getXMLAnnotationAnnotationRef(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}


	public String getXMLAnnotationDescription(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

    // -- MetadataRetrieve API methods --

    // - Entity counting -

}
