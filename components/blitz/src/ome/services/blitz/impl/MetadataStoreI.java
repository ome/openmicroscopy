/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.List;

import ome.formats.OMEROMetadataStore;
import ome.model.acquisition.Filament;
import ome.model.screen.Plate;
import ome.model.screen.Screen;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.blitz.util.BlitzOnly;
import ome.system.OmeroContext;
import omero.RBool;
import omero.RDouble;
import omero.RFloat;
import omero.RInt;
import omero.RLong;
import omero.RString;
import omero.RType;
import omero.ServerError;
import omero.api.*;
import omero.model.Arc;
import omero.model.BooleanAnnotation;
import omero.model.Dataset;
import omero.model.Image;
import omero.model.Instrument;
import omero.model.Pixels;
import omero.model.Project;
import omero.sys.EventContext;
import omero.util.IceMapper;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import Ice.Current;

/**
 */
public class MetadataStoreI extends AbstractAmdServant implements
        _MetadataStoreOperations, BlitzOnly {
    
    protected OMEROMetadataStore store;

    public MetadataStoreI(final BlitzExecutor be) throws Exception {
        super(null, be);
    }

    public void setOmeroContext(OmeroContext ctx) throws Exception {
        ome.system.ServiceFactory sf = new ome.system.ServiceFactory(ctx);
        this.store = new OMEROMetadataStore(sf);
    }

    // ~ Service methods
    // =========================================================================

    public void addBooleanAnnotationToPixels_async(
            final AMD_MetadataStore_addBooleanAnnotationToPixels __cb,
            final BooleanAnnotation ba, final Pixels p,
            final Ice.Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    ome.model.annotations.BooleanAnnotation _ba = (ome.model.annotations.BooleanAnnotation) mapper
                            .reverse(ba);
                    ome.model.core.Pixels _p = (ome.model.core.Pixels) mapper
                            .reverse(p);
                    store.addBooleanAnnotationToPixels(_ba, _p);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });
    }

    public void addDataset_async(final AMD_MetadataStore_addDataset __cb,
            final RString name, final RString description,
            final Project project, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    ome.model.containers.Project _p = (ome.model.containers.Project) mapper
                            .reverse(project);
                    ome.model.containers.Dataset d = store.addDataset(name.val,
                            description.val, _p);
                    Dataset _d = (Dataset) mapper.map(d);
                    // Code here
                    __cb.ice_response(_d);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void addImageToDataset_async(
            final AMD_MetadataStore_addImageToDataset __cb, final Image image,
            final Dataset dataset, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    ome.model.core.Image _image = (ome.model.core.Image) mapper
                            .reverse(image);
                    ome.model.containers.Dataset _dataset = (ome.model.containers.Dataset) mapper
                            .reverse(dataset);
                    store.addImageToDataset(_image, _dataset);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void addPlate_async(final AMD_MetadataStore_addPlate __cb,
            final int plateIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    Plate plate = store.addPlate(plateIndex);
                    omero.model.Plate _plate = (omero.model.Plate) mapper
                            .map(plate);
                    // Code here
                    __cb.ice_response(_plate);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void addScreen_async(final AMD_MetadataStore_addScreen __cb,
            final int screenIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    Screen screen = store.addScreen(screenIndex);
                    omero.model.Screen _screen = (omero.model.Screen) mapper
                            .map(screen);
                    // Code here
                    __cb.ice_response(_screen);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void createRoot_async(final AMD_MetadataStore_createRoot __cb,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.createRoot();
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void getArc_async(final AMD_MetadataStore_getArc __cb,
            final Instrument instrument, final int lightSourceIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    ome.model.acquisition.Instrument _i = (ome.model.acquisition.Instrument) mapper
                            .reverse(instrument);
                    ome.model.acquisition.Arc arc = store.getArc(_i,
                            lightSourceIndex);
                    Arc _arc = (Arc) mapper.map(arc);
                    // Code here
                    __cb.ice_response(_arc);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void getDataset_async(final AMD_MetadataStore_getDataset __cb,
            final long datasetID, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    ome.model.containers.Dataset d = store
                            .getDataset(datasetID);
                    Dataset _d = (Dataset) mapper.map(d);
                    // Code here
                    __cb.ice_response(_d);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void getDatasets_async(final AMD_MetadataStore_getDatasets __cb,
            final Project project, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    ome.model.containers.Project _project = (ome.model.containers.Project) mapper
                            .reverse(project);
                    List<ome.model.containers.Dataset> datasets = store
                            .getDatasets(_project);
                    List<Dataset> _datasets = (List<Dataset>) mapper
                            .map(datasets);
                    // Code here
                    __cb.ice_response(_datasets);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void getExperimenterID_async(
            final AMD_MetadataStore_getExperimenterID __cb,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    long id = store.getExperimenterID();
                    // Code here
                    __cb.ice_response(id);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void getFilament_async(final AMD_MetadataStore_getFilament __cb,
            final Instrument instrument, final int lightSourceIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    ome.model.acquisition.Instrument _instrument = (ome.model.acquisition.Instrument) mapper
                            .reverse(instrument);
                    Filament f = store.getFilament(_instrument,
                            lightSourceIndex);
                    omero.model.Filament _f = (omero.model.Filament) mapper
                            .map(f);
                    // Code here
                    __cb.ice_response(_f);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void getImage_async(final AMD_MetadataStore_getImage __cb,
            final int imageIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.getImage(imageIndex);
                    // Code here
                    __cb.ice_response(null /* FIXME */);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void getInstrument_async(final AMD_MetadataStore_getInstrument __cb,
            final int instrumentIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.getInstrument(instrumentIndex);
                    // Code here
                    __cb.ice_response(null /* FIXME */);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void getLaser_async(final AMD_MetadataStore_getLaser __cb,
            final Instrument instrument, final int lightSourceIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    ome.model.acquisition.Instrument _instrument = (ome.model.acquisition.Instrument) mapper
                            .reverse(instrument);
                    store.getLaser(_instrument, lightSourceIndex);
                    // Code here
                    __cb.ice_response(null /* FIXME */);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void getPixels2_async(final AMD_MetadataStore_getPixels2 __cb,
            final int imageIndex, final int pixelsIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.getPixels(imageIndex, pixelsIndex);
                    // Code here
                    __cb.ice_response(null /* FIXME */);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void getPixels_async(final AMD_MetadataStore_getPixels __cb,
            final int series, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.getPixels(series);
                    // Code here
                    __cb.ice_response(null /* FIXME */);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void getPlaneInfo_async(final AMD_MetadataStore_getPlaneInfo __cb,
            final int imageIndex, final int pixelsIndex, final int planeIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.getPlaneInfo(imageIndex, pixelsIndex, planeIndex);
                    // Code here
                    __cb.ice_response(null /* FIXME */);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void getPlate_async(final AMD_MetadataStore_getPlate __cb,
            final int plateIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.getPlate(plateIndex);
                    // Code here
                    __cb.ice_response(null /* FIXME */);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void getProject_async(final AMD_MetadataStore_getProject __cb,
            final long projectID, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.getProject(projectID);
                    // Code here
                    __cb.ice_response(null /* FIXME */);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void getProjects_async(final AMD_MetadataStore_getProjects __cb,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.getProjects();
                    // Code here
                    __cb.ice_response(null /* FIXME */);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void getRepositorySpace_async(
            final AMD_MetadataStore_getRepositorySpace __cb,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    long space = store.getRepositorySpace();
                    // Code here
                    __cb.ice_response(space);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void getRoot_async(final AMD_MetadataStore_getRoot __cb,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.getRoot();
                    // Code here
                    __cb.ice_response(null /* FIXME */);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void getScreen_async(final AMD_MetadataStore_getScreen __cb,
            final int screenIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.getScreen(screenIndex);
                    // Code here
                    __cb.ice_response(null /* FIXME */);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void getWell_async(final AMD_MetadataStore_getWell __cb,
            final int plateIndex, final int wellIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.getWell(plateIndex, wellIndex);
                    // Code here
                    __cb.ice_response(null /* FIXME */);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void populateMinMax_async(
            final AMD_MetadataStore_populateMinMax __cb, final RLong id,
            final RInt i, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.populateMinMax(id.val, i.val);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void saveToDB_async(final AMD_MetadataStore_saveToDB __cb,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.saveToDB();
                    // Code here
                    __cb.ice_response(null /* FIXME */);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setArcPower_async(final AMD_MetadataStore_setArcPower __cb,
            final RFloat power, final int instrumentIndex,
            final int lightSourceIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setArcPower(power.val, instrumentIndex,
                            lightSourceIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setArcType_async(final AMD_MetadataStore_setArcType __cb,
            final RString type, final int instrumentIndex,
            final int lightSourceIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setArcType(type.val, instrumentIndex,
                            lightSourceIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setChannelComponentColorDomain_async(
            final AMD_MetadataStore_setChannelComponentColorDomain __cb,
            final RString colorDomain, final int imageIndex,
            final int logicalChannelIndex, final int channelComponentIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setChannelComponentColorDomain(colorDomain.val,
                            imageIndex, logicalChannelIndex,
                            channelComponentIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setChannelComponentIndex_async(
            final AMD_MetadataStore_setChannelComponentIndex __cb,
            final RInt index, final int imageIndex,
            final int logicalChannelIndex, final int channelComponentIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setChannelComponentIndex(index.val, imageIndex,
                            logicalChannelIndex, channelComponentIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setChannelGlobalMinMax_async(
            final AMD_MetadataStore_setChannelGlobalMinMax __cb,
            final int channelIdx, final RDouble globalMin,
            final RDouble globalMax, final RInt pixelsIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setChannelGlobalMinMax(channelIdx, globalMin.val,
                            globalMax.val, pixelsIndex.val);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDetectorGain_async(
            final AMD_MetadataStore_setDetectorGain __cb, final RFloat gain,
            final int instrumentIndex, final int detectorIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setDetectorGain(gain.val, instrumentIndex,
                            detectorIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDetectorID_async(final AMD_MetadataStore_setDetectorID __cb,
            final RString id, final int instrumentIndex,
            final int detectorIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setDetectorID(id.val, instrumentIndex, detectorIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDetectorManufacturer_async(
            final AMD_MetadataStore_setDetectorManufacturer __cb,
            final RString manufacturer, final int instrumentIndex,
            final int detectorIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setDetectorManufacturer(manufacturer.val,
                            instrumentIndex, detectorIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDetectorModel_async(
            final AMD_MetadataStore_setDetectorModel __cb, final RString model,
            final int instrumentIndex, final int detectorIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setDetectorModel(model.val, instrumentIndex,
                            detectorIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDetectorNodeID_async(
            final AMD_MetadataStore_setDetectorNodeID __cb,
            final RString nodeID, final int instrumentIndex,
            final int detectorIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setDetectorNodeID(nodeID.val, instrumentIndex,
                            detectorIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDetectorOffset_async(
            final AMD_MetadataStore_setDetectorOffset __cb,
            final RFloat offset, final int instrumentIndex,
            final int detectorIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setDetectorOffset(offset.val, instrumentIndex,
                            detectorIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDetectorSerialNumber_async(
            final AMD_MetadataStore_setDetectorSerialNumber __cb,
            final RString serialNumber, final int instrumentIndex,
            final int detectorIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setDetectorSerialNumber(serialNumber.val,
                            instrumentIndex, detectorIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDetectorSettingsDetector_async(
            final AMD_MetadataStore_setDetectorSettingsDetector __cb,
            final RString detector, final int imageIndex,
            final int logicalChannelIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setDetectorSettingsDetector(detector.val, imageIndex,
                            logicalChannelIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDetectorSettingsGain_async(
            final AMD_MetadataStore_setDetectorSettingsGain __cb,
            final RFloat gain, final int imageIndex,
            final int logicalChannelIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setDetectorSettingsGain(gain.val, imageIndex,
                            logicalChannelIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDetectorSettingsOffset_async(
            final AMD_MetadataStore_setDetectorSettingsOffset __cb,
            final RFloat offset, final int imageIndex,
            final int logicalChannelIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setDetectorSettingsOffset(offset.val, imageIndex,
                            logicalChannelIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDetectorType_async(
            final AMD_MetadataStore_setDetectorType __cb, final RString type,
            final int instrumentIndex, final int detectorIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setDetectorType(type.val, instrumentIndex,
                            detectorIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDetectorVoltage_async(
            final AMD_MetadataStore_setDetectorVoltage __cb,
            final RFloat voltage, final int instrumentIndex,
            final int detectorIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setDetectorVoltage(voltage.val, instrumentIndex,
                            detectorIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDimensionsPhysicalSizeX_async(
            final AMD_MetadataStore_setDimensionsPhysicalSizeX __cb,
            final RFloat physicalSizeX, final int imageIndex,
            final int pixelsIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setDimensionsPhysicalSizeX(physicalSizeX.val,
                            imageIndex, pixelsIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDimensionsPhysicalSizeY_async(
            final AMD_MetadataStore_setDimensionsPhysicalSizeY __cb,
            final RFloat physicalSizeY, final int imageIndex,
            final int pixelsIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setDimensionsPhysicalSizeY(physicalSizeY.val,
                            imageIndex, pixelsIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDimensionsPhysicalSizeZ_async(
            final AMD_MetadataStore_setDimensionsPhysicalSizeZ __cb,
            final RFloat physicalSizeZ, final int imageIndex,
            final int pixelsIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setDimensionsPhysicalSizeZ(physicalSizeZ.val,
                            imageIndex, pixelsIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDimensionsTimeIncrement_async(
            final AMD_MetadataStore_setDimensionsTimeIncrement __cb,
            final RFloat timeIncrement, final int imageIndex,
            final int pixelsIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setDimensionsTimeIncrement(timeIncrement.val,
                            imageIndex, pixelsIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDimensionsWaveIncrement_async(
            final AMD_MetadataStore_setDimensionsWaveIncrement __cb,
            final RInt waveIncrement, final int imageIndex,
            final int pixelsIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setDimensionsWaveIncrement(waveIncrement.val,
                            imageIndex, pixelsIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDimensionsWaveStart_async(
            final AMD_MetadataStore_setDimensionsWaveStart __cb,
            final RInt waveStart, final int imageIndex, final int pixelsIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setDimensionsWaveStart(waveStart.val, imageIndex,
                            pixelsIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDisplayOptionsID_async(
            final AMD_MetadataStore_setDisplayOptionsID __cb, final RString id,
            final int imageIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setDisplayOptionsID(id.val, imageIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDisplayOptionsNodeID_async(
            final AMD_MetadataStore_setDisplayOptionsNodeID __cb,
            final RString nodeID, final int imageIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setDisplayOptionsNodeID(nodeID.val, imageIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDisplayOptionsProjectionZStart_async(
            final AMD_MetadataStore_setDisplayOptionsProjectionZStart __cb,
            final RInt start, final int imageIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setDisplayOptionsProjectionZStart(start.val,
                            imageIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDisplayOptionsProjectionZStop_async(
            final AMD_MetadataStore_setDisplayOptionsProjectionZStop __cb,
            final RInt stop, final int imageIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store
                            .setDisplayOptionsProjectionZStop(stop.val,
                                    imageIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDisplayOptionsTimeTStart_async(
            final AMD_MetadataStore_setDisplayOptionsTimeTStart __cb,
            final RInt start, final int imageIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setDisplayOptionsTimeTStart(start.val, imageIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDisplayOptionsTimeTStop_async(
            final AMD_MetadataStore_setDisplayOptionsTimeTStop __cb,
            final RInt stop, final int imageIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setDisplayOptionsTimeTStop(stop.val, imageIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setDisplayOptionsZoom_async(
            final AMD_MetadataStore_setDisplayOptionsZoom __cb,
            final RFloat zoom, final int imageIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setDisplayOptionsZoom(zoom.val, imageIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setExperimenterDataDirectory_async(
            final AMD_MetadataStore_setExperimenterDataDirectory __cb,
            final RString dataDirectory, final int experimenterIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setExperimenterDataDirectory(dataDirectory.val,
                            experimenterIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setExperimenterEmail_async(
            final AMD_MetadataStore_setExperimenterEmail __cb,
            final RString email, final int experimenterIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setExperimenterEmail(email.val, experimenterIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setExperimenterFirstName_async(
            final AMD_MetadataStore_setExperimenterFirstName __cb,
            final RString firstName, final int experimenterIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setExperimenterFirstName(firstName.val,
                            experimenterIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setExperimenterID_async(
            final AMD_MetadataStore_setExperimenterID __cb, final RString id,
            final int experimenterIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setExperimenterID(id.val, experimenterIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setExperimenterInstitution_async(
            final AMD_MetadataStore_setExperimenterInstitution __cb,
            final RString institution, final int experimenterIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setExperimenterInstitution(institution.val,
                            experimenterIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setExperimenterLastName_async(
            final AMD_MetadataStore_setExperimenterLastName __cb,
            final RString lastName, final int experimenterIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setExperimenterLastName(lastName.val,
                            experimenterIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setExperimenterNodeID_async(
            final AMD_MetadataStore_setExperimenterNodeID __cb,
            final RString nodeID, final int experimenterIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setExperimenterNodeID(nodeID.val, experimenterIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setFilamentPower_async(
            final AMD_MetadataStore_setFilamentPower __cb, final RFloat power,
            final int instrumentIndex, final int lightSourceIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setFilamentPower(power.val, instrumentIndex,
                            lightSourceIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setFilamentType_async(
            final AMD_MetadataStore_setFilamentType __cb, final RString type,
            final int instrumentIndex, final int lightSourceIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setFilamentType(type.val, instrumentIndex,
                            lightSourceIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setImageCreationDate_async(
            final AMD_MetadataStore_setImageCreationDate __cb,
            final RString creationDate, final int imageIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setImageCreationDate(creationDate.val, imageIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setImageDescription_async(
            final AMD_MetadataStore_setImageDescription __cb,
            final RString description, final int imageIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setImageDescription(description.val, imageIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setImageID_async(final AMD_MetadataStore_setImageID __cb,
            final RString id, final int imageIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setImageID(id.val, imageIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setImageInstrumentRef2_async(
            final AMD_MetadataStore_setImageInstrumentRef2 __cb,
            final RString instrumentRef, final int imageIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setImageInstrumentRef(instrumentRef.val, imageIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setImageInstrumentRef_async(
            final AMD_MetadataStore_setImageInstrumentRef __cb,
            final RInt instrumentRef, final int imageIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setImageInstrumentRef(instrumentRef.val, imageIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setImageName_async(final AMD_MetadataStore_setImageName __cb,
            final RString name, final int imageIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setImageName(name.val, imageIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setImageNodeID_async(
            final AMD_MetadataStore_setImageNodeID __cb, final RString nodeID,
            final int imageIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setImageNodeID(nodeID.val, imageIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setImagingEnvironmentAirPressure_async(
            final AMD_MetadataStore_setImagingEnvironmentAirPressure __cb,
            final RFloat airPressure, final int imageIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setImagingEnvironmentAirPressure(airPressure.val,
                            imageIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setImagingEnvironmentCO2Percent_async(
            final AMD_MetadataStore_setImagingEnvironmentCO2Percent __cb,
            final RFloat percent, final int imageIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setImagingEnvironmentCO2Percent(percent.val,
                            imageIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setImagingEnvironmentHumidity_async(
            final AMD_MetadataStore_setImagingEnvironmentHumidity __cb,
            final RFloat humidity, final int imageIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setImagingEnvironmentHumidity(humidity.val,
                            imageIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setImagingEnvironmentTemperature_async(
            final AMD_MetadataStore_setImagingEnvironmentTemperature __cb,
            final RFloat temperature, final int imageIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setImagingEnvironmentTemperature(temperature.val,
                            imageIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setInstrumentID_async(
            final AMD_MetadataStore_setInstrumentID __cb, final RString id,
            final int instrumentIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setInstrumentID(id.val, instrumentIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setInstrumentNodeID_async(
            final AMD_MetadataStore_setInstrumentNodeID __cb,
            final RString nodeID, final int instrumentIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setInstrumentNodeID(nodeID.val, instrumentIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLaserFrequencyMultiplication_async(
            final AMD_MetadataStore_setLaserFrequencyMultiplication __cb,
            final RInt frequencyMultiplication, final int instrumentIndex,
            final int lightSourceIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLaserFrequencyMultiplication(
                            frequencyMultiplication.val, instrumentIndex,
                            lightSourceIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLaserLaserMedium_async(
            final AMD_MetadataStore_setLaserLaserMedium __cb,
            final RString laserMedium, final int instrumentIndex,
            final int lightSourceIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLaserLaserMedium(laserMedium.val, instrumentIndex,
                            lightSourceIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLaserPower_async(final AMD_MetadataStore_setLaserPower __cb,
            final RFloat power, final int instrumentIndex,
            final int lightSourceIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLaserPower(power.val, instrumentIndex,
                            lightSourceIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLaserPulse_async(final AMD_MetadataStore_setLaserPulse __cb,
            final RString pulse, final int instrumentIndex,
            final int lightSourceIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLaserPulse(pulse.val, instrumentIndex,
                            lightSourceIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLaserTuneable_async(
            final AMD_MetadataStore_setLaserTuneable __cb,
            final RBool tuneable, final int instrumentIndex,
            final int lightSourceIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLaserTuneable(tuneable.val, instrumentIndex,
                            lightSourceIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLaserType_async(final AMD_MetadataStore_setLaserType __cb,
            final RString type, final int instrumentIndex,
            final int lightSourceIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLaserType(type.val, instrumentIndex,
                            lightSourceIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLaserWavelength_async(
            final AMD_MetadataStore_setLaserWavelength __cb,
            final RInt wavelength, final int instrumentIndex,
            final int lightSourceIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLaserWavelength(wavelength.val, instrumentIndex,
                            lightSourceIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLightSourceID_async(
            final AMD_MetadataStore_setLightSourceID __cb, final RString id,
            final int instrumentIndex, final int lightSourceIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLightSourceID(id.val, instrumentIndex,
                            lightSourceIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLightSourceManufacturer_async(
            final AMD_MetadataStore_setLightSourceManufacturer __cb,
            final RString manufacturer, final int instrumentIndex,
            final int lightSourceIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLightSourceManufacturer(manufacturer.val,
                            instrumentIndex, lightSourceIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLightSourceModel_async(
            final AMD_MetadataStore_setLightSourceModel __cb,
            final RString model, final int instrumentIndex,
            final int lightSourceIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLightSourceModel(model.val, instrumentIndex,
                            lightSourceIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLightSourceNodeID_async(
            final AMD_MetadataStore_setLightSourceNodeID __cb,
            final RString nodeID, final int instrumentIndex,
            final int lightSourceIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLightSourceNodeID(nodeID.val, instrumentIndex,
                            lightSourceIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLightSourcePower_async(
            final AMD_MetadataStore_setLightSourcePower __cb,
            final RFloat power, final int instrumentIndex,
            final int lightSourceIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLightSourcePower(power.val, instrumentIndex,
                            lightSourceIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLightSourceSerialNumber_async(
            final AMD_MetadataStore_setLightSourceSerialNumber __cb,
            final RString serialNumber, final int instrumentIndex,
            final int lightSourceIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLightSourceSerialNumber(serialNumber.val,
                            instrumentIndex, lightSourceIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLightSourceSettingsAttenuation_async(
            final AMD_MetadataStore_setLightSourceSettingsAttenuation __cb,
            final RFloat attenuation, final int imageIndex,
            final int logicalChannelIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLightSourceSettingsAttenuation(attenuation.val,
                            imageIndex, logicalChannelIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLightSourceSettingsLightSource_async(
            final AMD_MetadataStore_setLightSourceSettingsLightSource __cb,
            final RString lightSource, final int imageIndex,
            final int logicalChannelIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLightSourceSettingsLightSource(lightSource.val,
                            imageIndex, logicalChannelIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLightSourceSettingsWavelength_async(
            final AMD_MetadataStore_setLightSourceSettingsWavelength __cb,
            final RInt wavelength, final int imageIndex,
            final int logicalChannelIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLightSourceSettingsWavelength(wavelength.val,
                            imageIndex, logicalChannelIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLogicalChannelContrastMethod_async(
            final AMD_MetadataStore_setLogicalChannelContrastMethod __cb,
            final RString contrastMethod, final int imageIndex,
            final int logicalChannelIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLogicalChannelContrastMethod(contrastMethod.val,
                            imageIndex, logicalChannelIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLogicalChannelEmWave_async(
            final AMD_MetadataStore_setLogicalChannelEmWave __cb,
            final RInt emWave, final int imageIndex,
            final int logicalChannelIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLogicalChannelEmWave(emWave.val, imageIndex,
                            logicalChannelIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLogicalChannelExWave_async(
            final AMD_MetadataStore_setLogicalChannelExWave __cb,
            final RInt exWave, final int imageIndex,
            final int logicalChannelIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLogicalChannelExWave(exWave.val, imageIndex,
                            logicalChannelIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLogicalChannelFluor_async(
            final AMD_MetadataStore_setLogicalChannelFluor __cb,
            final RString fluor, final int imageIndex,
            final int logicalChannelIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLogicalChannelFluor(fluor.val, imageIndex,
                            logicalChannelIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLogicalChannelID_async(
            final AMD_MetadataStore_setLogicalChannelID __cb, final RString id,
            final int imageIndex, final int logicalChannelIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLogicalChannelID(id.val, imageIndex,
                            logicalChannelIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLogicalChannelIlluminationType_async(
            final AMD_MetadataStore_setLogicalChannelIlluminationType __cb,
            final RString illuminationType, final int imageIndex,
            final int logicalChannelIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLogicalChannelIlluminationType(
                            illuminationType.val, imageIndex,
                            logicalChannelIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLogicalChannelMode_async(
            final AMD_MetadataStore_setLogicalChannelMode __cb,
            final RString mode, final int imageIndex,
            final int logicalChannelIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLogicalChannelMode(mode.val, imageIndex,
                            logicalChannelIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLogicalChannelName_async(
            final AMD_MetadataStore_setLogicalChannelName __cb,
            final RString name, final int imageIndex,
            final int logicalChannelIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLogicalChannelName(name.val, imageIndex,
                            logicalChannelIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLogicalChannelNdFilter_async(
            final AMD_MetadataStore_setLogicalChannelNdFilter __cb,
            final RFloat ndFilter, final int imageIndex,
            final int logicalChannelIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLogicalChannelNdFilter(ndFilter.val, imageIndex,
                            logicalChannelIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLogicalChannelNodeID_async(
            final AMD_MetadataStore_setLogicalChannelNodeID __cb,
            final RString nodeID, final int imageIndex,
            final int logicalChannelIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLogicalChannelNodeID(nodeID.val, imageIndex,
                            logicalChannelIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLogicalChannelPhotometricInterpretation_async(
            final AMD_MetadataStore_setLogicalChannelPhotometricInterpretation __cb,
            final RString photometricInterpretation, final int imageIndex,
            final int logicalChannelIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLogicalChannelPhotometricInterpretation(
                            photometricInterpretation.val, imageIndex,
                            logicalChannelIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLogicalChannelPinholeSize_async(
            final AMD_MetadataStore_setLogicalChannelPinholeSize __cb,
            final RInt pinholeSize, final int imageIndex,
            final int logicalChannelIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLogicalChannelPinholeSize(pinholeSize.val,
                            imageIndex, logicalChannelIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLogicalChannelPockelCellSetting_async(
            final AMD_MetadataStore_setLogicalChannelPockelCellSetting __cb,
            final RInt pockelCellSetting, final int imageIndex,
            final int logicalChannelIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLogicalChannelPockelCellSetting(
                            pockelCellSetting.val, imageIndex,
                            logicalChannelIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setLogicalChannelSamplesPerPixel_async(
            final AMD_MetadataStore_setLogicalChannelSamplesPerPixel __cb,
            final RInt samplesPerPixel, final int imageIndex,
            final int logicalChannelIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setLogicalChannelSamplesPerPixel(samplesPerPixel.val,
                            imageIndex, logicalChannelIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setOTFID_async(final AMD_MetadataStore_setOTFID __cb,
            final RString id, final int instrumentIndex, final int otfIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setOTFID(id.val, instrumentIndex, otfIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setOTFNodeID_async(final AMD_MetadataStore_setOTFNodeID __cb,
            final RString nodeID, final int instrumentIndex,
            final int otfIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setOTFNodeID(nodeID.val, instrumentIndex, otfIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setOTFOpticalAxisAveraged_async(
            final AMD_MetadataStore_setOTFOpticalAxisAveraged __cb,
            final RBool opticalAxisAveraged, final int instrumentIndex,
            final int otfIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setOTFOpticalAxisAveraged(opticalAxisAveraged.val,
                            instrumentIndex, otfIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setOTFPath_async(final AMD_MetadataStore_setOTFPath __cb,
            final RString path, final int instrumentIndex, final int otfIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setOTFPath(path.val, instrumentIndex, otfIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setOTFPixelType_async(
            final AMD_MetadataStore_setOTFPixelType __cb,
            final RString pixelType, final int instrumentIndex,
            final int otfIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setOTFPixelType(pixelType.val, instrumentIndex,
                            otfIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setOTFSizeX_async(final AMD_MetadataStore_setOTFSizeX __cb,
            final RInt sizeX, final int instrumentIndex, final int otfIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setOTFSizeX(sizeX.val, instrumentIndex, otfIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setOTFSizeY_async(final AMD_MetadataStore_setOTFSizeY __cb,
            final RInt sizeY, final int instrumentIndex, final int otfIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setOTFSizeY(sizeY.val, instrumentIndex, otfIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setObjectiveCalibratedMagnification_async(
            final AMD_MetadataStore_setObjectiveCalibratedMagnification __cb,
            final RFloat calibratedMagnification, final int instrumentIndex,
            final int objectiveIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setObjectiveCalibratedMagnification(
                            calibratedMagnification.val, instrumentIndex,
                            objectiveIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setObjectiveCorrection_async(
            final AMD_MetadataStore_setObjectiveCorrection __cb,
            final RString correction, final int instrumentIndex,
            final int objectiveIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setObjectiveCorrection(correction.val,
                            instrumentIndex, objectiveIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setObjectiveID_async(
            final AMD_MetadataStore_setObjectiveID __cb, final RString id,
            final int instrumentIndex, final int objectiveIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setObjectiveID(id.val, instrumentIndex,
                            objectiveIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setObjectiveImmersion_async(
            final AMD_MetadataStore_setObjectiveImmersion __cb,
            final RString immersion, final int instrumentIndex,
            final int objectiveIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setObjectiveImmersion(immersion.val, instrumentIndex,
                            objectiveIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setObjectiveLensNA_async(
            final AMD_MetadataStore_setObjectiveLensNA __cb,
            final RFloat lensNA, final int instrumentIndex,
            final int objectiveIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setObjectiveLensNA(lensNA.val, instrumentIndex,
                            objectiveIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setObjectiveManufacturer_async(
            final AMD_MetadataStore_setObjectiveManufacturer __cb,
            final RString manufacturer, final int instrumentIndex,
            final int objectiveIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setObjectiveManufacturer(manufacturer.val,
                            instrumentIndex, objectiveIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setObjectiveModel_async(
            final AMD_MetadataStore_setObjectiveModel __cb,
            final RString model, final int instrumentIndex,
            final int objectiveIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setObjectiveModel(model.val, instrumentIndex,
                            objectiveIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setObjectiveNodeID_async(
            final AMD_MetadataStore_setObjectiveNodeID __cb,
            final RString nodeID, final int instrumentIndex,
            final int objectiveIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setObjectiveNodeID(nodeID.val, instrumentIndex,
                            objectiveIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setObjectiveNominalMagnification_async(
            final AMD_MetadataStore_setObjectiveNominalMagnification __cb,
            final RInt nominalMagnification, final int instrumentIndex,
            final int objectiveIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setObjectiveNominalMagnification(
                            nominalMagnification.val, instrumentIndex,
                            objectiveIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setObjectiveSerialNumber_async(
            final AMD_MetadataStore_setObjectiveSerialNumber __cb,
            final RString serialNumber, final int instrumentIndex,
            final int objectiveIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setObjectiveSerialNumber(serialNumber.val,
                            instrumentIndex, objectiveIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setObjectiveWorkingDistance_async(
            final AMD_MetadataStore_setObjectiveWorkingDistance __cb,
            final RFloat workingDistance, final int instrumentIndex,
            final int objectiveIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setObjectiveWorkingDistance(workingDistance.val,
                            instrumentIndex, objectiveIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setPixelsBigEndian_async(
            final AMD_MetadataStore_setPixelsBigEndian __cb,
            final RBool bigEndian, final int imageIndex, final int pixelsIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setPixelsBigEndian(bigEndian.val, imageIndex,
                            pixelsIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setPixelsDimensionOrder_async(
            final AMD_MetadataStore_setPixelsDimensionOrder __cb,
            final RString dimensionOrder, final int imageIndex,
            final int pixelsIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setPixelsDimensionOrder(dimensionOrder.val,
                            imageIndex, pixelsIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setPixelsID_async(final AMD_MetadataStore_setPixelsID __cb,
            final RString id, final int imageIndex, final int pixelsIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setPixelsID(id.val, imageIndex, pixelsIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setPixelsNodeID_async(
            final AMD_MetadataStore_setPixelsNodeID __cb, final RString nodeID,
            final int imageIndex, final int pixelsIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setPixelsNodeID(nodeID.val, imageIndex, pixelsIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setPixelsPixelType_async(
            final AMD_MetadataStore_setPixelsPixelType __cb,
            final RString pixelType, final int imageIndex,
            final int pixelsIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setPixelsPixelType(pixelType.val, imageIndex,
                            pixelsIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setPixelsSizeC_async(
            final AMD_MetadataStore_setPixelsSizeC __cb, final RInt sizeC,
            final int imageIndex, final int pixelsIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setPixelsSizeC(sizeC.val, imageIndex, pixelsIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setPixelsSizeT_async(
            final AMD_MetadataStore_setPixelsSizeT __cb, final RInt sizeT,
            final int imageIndex, final int pixelsIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setPixelsSizeT(sizeT.val, imageIndex, pixelsIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setPixelsSizeX_async(
            final AMD_MetadataStore_setPixelsSizeX __cb, final RInt sizeX,
            final int imageIndex, final int pixelsIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setPixelsSizeX(sizeX.val, imageIndex, pixelsIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setPixelsSizeY_async(
            final AMD_MetadataStore_setPixelsSizeY __cb, final RInt sizeY,
            final int imageIndex, final int pixelsIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setPixelsSizeY(sizeY.val, imageIndex, pixelsIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setPixelsSizeZ_async(
            final AMD_MetadataStore_setPixelsSizeZ __cb, final RInt sizeZ,
            final int imageIndex, final int pixelsIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setPixelsSizeZ(sizeZ.val, imageIndex, pixelsIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setPlaneTheC_async(final AMD_MetadataStore_setPlaneTheC __cb,
            final RInt theC, final int imageIndex, final int pixelsIndex,
            final int planeIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setPlaneTheC(theC.val, imageIndex, pixelsIndex,
                            planeIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setPlaneTheT_async(final AMD_MetadataStore_setPlaneTheT __cb,
            final RInt theT, final int imageIndex, final int pixelsIndex,
            final int planeIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setPlaneTheT(theT.val, imageIndex, pixelsIndex,
                            planeIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setPlaneTheZ_async(final AMD_MetadataStore_setPlaneTheZ __cb,
            final RInt theZ, final int imageIndex, final int pixelsIndex,
            final int planeIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setPlaneTheZ(theZ.val, imageIndex, pixelsIndex,
                            planeIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setPlaneTimingDeltaT_async(
            final AMD_MetadataStore_setPlaneTimingDeltaT __cb,
            final RFloat deltaT, final int imageIndex, final int pixelsIndex,
            final int planeIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setPlaneTimingDeltaT(deltaT.val, imageIndex,
                            pixelsIndex, planeIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setPlaneTimingExposureTime_async(
            final AMD_MetadataStore_setPlaneTimingExposureTime __cb,
            final RFloat exposureTime, final int imageIndex,
            final int pixelsIndex, final int planeIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setPlaneTimingExposureTime(exposureTime.val,
                            imageIndex, pixelsIndex, planeIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setPlane_async(final AMD_MetadataStore_setPlane __cb,
            final RLong id, final byte[] pixels, final int theZ,
            final int theC, final int theT, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setPlane(id.val, pixels, theZ, theC, theT);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setPlateDescription_async(
            final AMD_MetadataStore_setPlateDescription __cb,
            final RString description, final int plateIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setPlateDescription(description.val, plateIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setPlateExternalIdentifier_async(
            final AMD_MetadataStore_setPlateExternalIdentifier __cb,
            final RString externalIdentifier, final int plateIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setPlateExternalIdentifier(externalIdentifier.val,
                            plateIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setPlateID_async(final AMD_MetadataStore_setPlateID __cb,
            final RString id, final int plateIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setPlateID(id.val, plateIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setPlateName_async(final AMD_MetadataStore_setPlateName __cb,
            final RString name, final int plateIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setPlateName(name.val, plateIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setPlateRefID_async(final AMD_MetadataStore_setPlateRefID __cb,
            final RString id, final int screenIndex, final int plateRefIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setPlateRefID(id.val, screenIndex, plateRefIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setPlateStatus_async(
            final AMD_MetadataStore_setPlateStatus __cb, final RString status,
            final int plateIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setPlateStatus(status.val, plateIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setROIID_async(final AMD_MetadataStore_setROIID __cb,
            final RString id, final int imageIndex, final int roiIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setROIID(id.val, imageIndex, roiIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setROINodeID_async(final AMD_MetadataStore_setROINodeID __cb,
            final RString nodeID, final int imageIndex, final int roiIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setROINodeID(nodeID.val, imageIndex, roiIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setROIT0_async(final AMD_MetadataStore_setROIT0 __cb,
            final RInt t0, final int imageIndex, final int roiIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setROIT0(t0.val, imageIndex, roiIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setROIT1_async(final AMD_MetadataStore_setROIT1 __cb,
            final RInt t1, final int imageIndex, final int roiIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setROIT1(t1.val, imageIndex, roiIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setROIX0_async(final AMD_MetadataStore_setROIX0 __cb,
            final RInt x0, final int imageIndex, final int roiIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setROIX0(x0.val, imageIndex, roiIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setROIX1_async(final AMD_MetadataStore_setROIX1 __cb,
            final RInt x1, final int imageIndex, final int roiIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setROIX1(x1.val, imageIndex, roiIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setROIY0_async(final AMD_MetadataStore_setROIY0 __cb,
            final RInt y0, final int imageIndex, final int roiIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setROIY0(y0.val, imageIndex, roiIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setROIY1_async(final AMD_MetadataStore_setROIY1 __cb,
            final RInt y1, final int imageIndex, final int roiIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setROIY1(y1.val, imageIndex, roiIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setROIZ0_async(final AMD_MetadataStore_setROIZ0 __cb,
            final RInt z0, final int imageIndex, final int roiIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setROIZ0(z0.val, imageIndex, roiIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setROIZ1_async(final AMD_MetadataStore_setROIZ1 __cb,
            final RInt z1, final int imageIndex, final int roiIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setROIZ1(z1.val, imageIndex, roiIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setReagentDescription_async(
            final AMD_MetadataStore_setReagentDescription __cb,
            final RString description, final int screenIndex,
            final int reagentIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setReagentDescription(description.val, screenIndex,
                            reagentIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setReagentID_async(final AMD_MetadataStore_setReagentID __cb,
            final RString id, final int screenIndex, final int reagentIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setReagentID(id.val, screenIndex, reagentIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setReagentName_async(
            final AMD_MetadataStore_setReagentName __cb, final RString name,
            final int screenIndex, final int reagentIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setReagentName(name.val, screenIndex, reagentIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setReagentReagentIdentifier_async(
            final AMD_MetadataStore_setReagentReagentIdentifier __cb,
            final RString reagentIdentifier, final int screenIndex,
            final int reagentIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setReagentReagentIdentifier(reagentIdentifier.val,
                            screenIndex, reagentIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setRoot_async(final AMD_MetadataStore_setRoot __cb,
            final RType root, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setRoot(null /* FIXME */);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setScreenAcquisitionEndTime_async(
            final AMD_MetadataStore_setScreenAcquisitionEndTime __cb,
            final RString endTime, final int screenIndex,
            final int screenAcquisitionIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setScreenAcquisitionEndTime(endTime.val, screenIndex,
                            screenAcquisitionIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setScreenAcquisitionID_async(
            final AMD_MetadataStore_setScreenAcquisitionID __cb,
            final RString id, final int screenIndex,
            final int screenAcquisitionIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setScreenAcquisitionID(id.val, screenIndex,
                            screenAcquisitionIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setScreenAcquisitionStartTime_async(
            final AMD_MetadataStore_setScreenAcquisitionStartTime __cb,
            final RString startTime, final int screenIndex,
            final int screenAcquisitionIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setScreenAcquisitionStartTime(startTime.val,
                            screenIndex, screenAcquisitionIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setScreenID_async(final AMD_MetadataStore_setScreenID __cb,
            final RString id, final int screenIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setScreenID(id.val, screenIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setScreenName_async(final AMD_MetadataStore_setScreenName __cb,
            final RString name, final int screenIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setScreenName(name.val, screenIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setScreenProtocolDescription_async(
            final AMD_MetadataStore_setScreenProtocolDescription __cb,
            final RString protocolDescription, final int screenIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setScreenProtocolDescription(protocolDescription.val,
                            screenIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setScreenProtocolIdentifier_async(
            final AMD_MetadataStore_setScreenProtocolIdentifier __cb,
            final RString protocolIdentifier, final int screenIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setScreenProtocolIdentifier(protocolIdentifier.val,
                            screenIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setScreenReagentSetDescription_async(
            final AMD_MetadataStore_setScreenReagentSetDescription __cb,
            final RString reagentSetDescription, final int screenIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setScreenReagentSetDescription(
                            reagentSetDescription.val, screenIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setStack_async(final AMD_MetadataStore_setStack __cb,
            final RLong id, final byte[] pixels, final int theC,
            final int theT, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setStack(id.val, pixels, theC, theT);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setStageLabelName_async(
            final AMD_MetadataStore_setStageLabelName __cb, final RString name,
            final int imageIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setStageLabelName(name.val, imageIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setStageLabelX_async(
            final AMD_MetadataStore_setStageLabelX __cb, final RFloat x,
            final int imageIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setStageLabelX(x.val, imageIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setStageLabelY_async(
            final AMD_MetadataStore_setStageLabelY __cb, final RFloat y,
            final int imageIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setStageLabelY(y.val, imageIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setStageLabelZ_async(
            final AMD_MetadataStore_setStageLabelZ __cb, final RFloat z,
            final int imageIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setStageLabelZ(z.val, imageIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setStagePositionPositionX_async(
            final AMD_MetadataStore_setStagePositionPositionX __cb,
            final RFloat positionX, final int imageIndex,
            final int pixelsIndex, final int planeIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setStagePositionPositionX(positionX.val, imageIndex,
                            pixelsIndex, planeIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setStagePositionPositionY_async(
            final AMD_MetadataStore_setStagePositionPositionY __cb,
            final RFloat positionY, final int imageIndex,
            final int pixelsIndex, final int planeIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setStagePositionPositionY(positionY.val, imageIndex,
                            pixelsIndex, planeIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setStagePositionPositionZ_async(
            final AMD_MetadataStore_setStagePositionPositionZ __cb,
            final RFloat positionZ, final int imageIndex,
            final int pixelsIndex, final int planeIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setStagePositionPositionZ(positionZ.val, imageIndex,
                            pixelsIndex, planeIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setThePixelsId_async(
            final AMD_MetadataStore_setThePixelsId __cb, final RLong id,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setPixelsId(id.val);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setTiffDataFileName_async(
            final AMD_MetadataStore_setTiffDataFileName __cb,
            final RString fileName, final int imageIndex,
            final int pixelsIndex, final int tiffDataIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setTiffDataFileName(fileName.val, imageIndex,
                            pixelsIndex, tiffDataIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setTiffDataFirstC_async(
            final AMD_MetadataStore_setTiffDataFirstC __cb, final RInt firstC,
            final int imageIndex, final int pixelsIndex,
            final int tiffDataIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setTiffDataFirstC(firstC.val, imageIndex,
                            pixelsIndex, tiffDataIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setTiffDataFirstT_async(
            final AMD_MetadataStore_setTiffDataFirstT __cb, final RInt firstT,
            final int imageIndex, final int pixelsIndex,
            final int tiffDataIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setTiffDataFirstT(firstT.val, imageIndex,
                            pixelsIndex, tiffDataIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setTiffDataFirstZ_async(
            final AMD_MetadataStore_setTiffDataFirstZ __cb, final RInt firstZ,
            final int imageIndex, final int pixelsIndex,
            final int tiffDataIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setTiffDataFirstZ(firstZ.val, imageIndex,
                            pixelsIndex, tiffDataIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setTiffDataIFD_async(
            final AMD_MetadataStore_setTiffDataIFD __cb, final RInt ifd,
            final int imageIndex, final int pixelsIndex,
            final int tiffDataIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setTiffDataIFD(ifd.val, imageIndex, pixelsIndex,
                            tiffDataIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setTiffDataNumPlanes_async(
            final AMD_MetadataStore_setTiffDataNumPlanes __cb,
            final RInt numPlanes, final int imageIndex, final int pixelsIndex,
            final int tiffDataIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setTiffDataNumPlanes(numPlanes.val, imageIndex,
                            pixelsIndex, tiffDataIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setTiffDataUUID_async(
            final AMD_MetadataStore_setTiffDataUUID __cb, final RString uuid,
            final int imageIndex, final int pixelsIndex,
            final int tiffDataIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setTiffDataUUID(uuid.val, imageIndex, pixelsIndex,
                            tiffDataIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setUUID_async(final AMD_MetadataStore_setUUID __cb,
            final RString uuid, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setUUID(uuid.val);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setWellColumn_async(final AMD_MetadataStore_setWellColumn __cb,
            final RInt column, final int plateIndex, final int wellIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setWellColumn(column.val, plateIndex, wellIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setWellExternalDescription_async(
            final AMD_MetadataStore_setWellExternalDescription __cb,
            final RString externalDescription, final int plateIndex,
            final int wellIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setWellExternalDescription(externalDescription.val,
                            plateIndex, wellIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setWellExternalIdentifier_async(
            final AMD_MetadataStore_setWellExternalIdentifier __cb,
            final RString externalIdentifier, final int plateIndex,
            final int wellIndex, final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setWellExternalIdentifier(externalIdentifier.val,
                            plateIndex, wellIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setWellID_async(final AMD_MetadataStore_setWellID __cb,
            final RString id, final int plateIndex, final int wellIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setWellID(id.val, plateIndex, wellIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setWellRow_async(final AMD_MetadataStore_setWellRow __cb,
            final RInt row, final int plateIndex, final int wellIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setWellRow(row.val, plateIndex, wellIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setWellSampleID_async(
            final AMD_MetadataStore_setWellSampleID __cb, final RString id,
            final int plateIndex, final int wellIndex,
            final int wellSampleIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setWellSampleID(id.val, plateIndex, wellIndex,
                            wellSampleIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setWellSampleIndex_async(
            final AMD_MetadataStore_setWellSampleIndex __cb, final RInt index,
            final int plateIndex, final int wellIndex,
            final int wellSampleIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setWellSampleIndex(index.val, plateIndex, wellIndex,
                            wellSampleIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setWellSamplePosX_async(
            final AMD_MetadataStore_setWellSamplePosX __cb, final RFloat posX,
            final int plateIndex, final int wellIndex,
            final int wellSampleIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setWellSamplePosX(posX.val, plateIndex, wellIndex,
                            wellSampleIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setWellSamplePosY_async(
            final AMD_MetadataStore_setWellSamplePosY __cb, final RFloat posY,
            final int plateIndex, final int wellIndex,
            final int wellSampleIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setWellSamplePosY(posY.val, plateIndex, wellIndex,
                            wellSampleIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setWellSampleTimepoint_async(
            final AMD_MetadataStore_setWellSampleTimepoint __cb,
            final RInt timepoint, final int plateIndex, final int wellIndex,
            final int wellSampleIndex, final Current __current)
            throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setWellSampleTimepoint(timepoint.val, plateIndex,
                            wellIndex, wellSampleIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void setWellType_async(final AMD_MetadataStore_setWellType __cb,
            final RString type, final int plateIndex, final int wellIndex,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    store.setWellType(type.val, plateIndex, wellIndex);
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void close_async(final AMD_StatefulServiceInterface_close __cb,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    // FIXME What to do here?
                    store = null;
                    // Code here
                    __cb.ice_response();
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

    public void getCurrentEventContext_async(
            final AMD_StatefulServiceInterface_getCurrentEventContext __cb,
            final Current __current) throws ServerError {
        runnableCall(__current, new BlitzExecutor.Task() {
            public void run() {
                try {
                    // Code here
                    IceMapper mapper = new IceMapper();
                    ome.system.EventContext ec = store.getSF().getAdminService().getEventContext();
                    EventContext _ec = mapper.convert(ec);
                    // Code here
                    __cb.ice_response(_ec);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }
        });

    }

}
