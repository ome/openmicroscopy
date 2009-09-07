/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import static omero.rtypes.rstring;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import loci.formats.MetadataTools;
import loci.formats.meta.IMetadata;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.ome.OMEXMLMetadata;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.blitz.util.BlitzOnly;
import ome.services.blitz.util.ServiceFactoryAware;
import ome.services.util.Executor;
import ome.services.util.Executor.SimpleWork;
import ome.services.util.Executor.Work;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.util.Filterable;
import ome.xml.DOMUtil;
import ome.xml.OMEXMLFactory;
import ome.xml.OMEXMLNode;
import omero.ServerError;
import omero.api.AMD_Exporter_addImage;
import omero.api.AMD_Exporter_asXml;
import omero.api.AMD_Exporter_getBytes;
import omero.api.AMD_Exporter_start;
import omero.api.AMD_StatefulServiceInterface_activate;
import omero.api.AMD_StatefulServiceInterface_close;
import omero.api.AMD_StatefulServiceInterface_getCurrentEventContext;
import omero.api.AMD_StatefulServiceInterface_passivate;
import omero.api._ExporterOperations;
import omero.model.Details;
import omero.model.ExternalInfo;
import omero.model.ExternalInfoI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageI;
import omero.util.IceMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import Ice.Current;

/**
 * Implementation of the Exporter service.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.1
 */
public class ExporterI extends AbstractAmdServant implements
        _ExporterOperations, ServiceFactoryAware, BlitzOnly {

    private final static Log log = LogFactory.getLog(ExporterI.class);

    private final static int MAX_SIZE = 1024 * 1024;

    /**
     * Current user choice as to what output method should be used when
     * {@link ExporterI#start_async(AMD_Exporter_start, Current)} is invoked.
     */
    private enum Output {
        xml, tiff, hdf;
    }

    /**
     * Utility enum for asserting the state of Exporter instances.
     */
    private enum State {
        config, output, waiting;
        static State check(ExporterI self) {

            if (self.file != null && self.retrieve != null) {
                throw new InternalException("Doing 2 things at once");
            }

            if (self.retrieve != null) {
                return config;
            }

            if (self.file != null) {
                return self.offset < 0 ? waiting : output;
            }

            return waiting;
        }
    }

    private/* final */ServiceFactoryI factory;

    private volatile Output out = Output.xml;

    private volatile Retrieve retrieve;

    /**
     * Reference to the temporary file which is currently being
     */
    private volatile File file;

    /**
     * Offset into the file which we are currently reading. This field. is only
     * valid when the file field is non-null. If it is less than zero, then the
     * current file has been completely read.
     */
    private volatile long offset = 0;

    public ExporterI(BlitzExecutor be) {
        super(null, be);
    }

    public void setServiceFactory(ServiceFactoryI sf) throws ServerError {
        this.factory = sf;
    }

    // Interface methods
    // =========================================================================

    public void addImage_async(AMD_Exporter_addImage __cb, final long id,
            Current __current) throws ServerError {

        State state = State.check(this);
        ServerError se = assertConfig(state);
        if (se != null) {
            __cb.ice_exception(se);
        }

        retrieve.addImage(new ImageI(id, false));
        __cb.ice_response();
        return;

    }

    /**
     * 
     */
    public void asXml_async(AMD_Exporter_asXml __cb, Current __current)
            throws ServerError {

        State state = State.check(this);
        ServerError se = assertConfig(state);
        if (se != null) {
            __cb.ice_exception(se);
        }
        this.out = Output.xml;
        __cb.ice_response();
        return;

    }

    public void getBytes_async(AMD_Exporter_getBytes __cb, int size,
            Current __current) throws ServerError {

        State state = State.check(this);
        switch (state) {
        case waiting:
            __cb.ice_response(new byte[] {});
            return;
        case config:
            ServerError se = startOutput(); // Transitions to output so fall
            // through
            if (se != null) {
                __cb.ice_exception(se);
                return;
            }
        case output:
            __cb.ice_response(read(size));
            return;
        default:
            throw new InternalException("Unknown state: " + state);
        }
    }

    // State methods
    // =========================================================================

    /**
     * Transition from waiting to config
     */
    private ServerError assertConfig(State state) {
        switch (state) {
        case waiting:
            startConfig(); // Transitions to config, so fall through
        case config:
            return null;
        case output:
            return new omero.ApiUsageException(null, null,
                    "Cannot configure during output");
        default:
            return new omero.InternalException(null, null, "Unknown state: "
                    + state);
        }
    }

    /**
     * Transition from waiting to config
     */
    private void startConfig() {
        if (file != null) {
            file.delete();
            file = null;
        }
        retrieve = new Retrieve();
        offset = 0;
    }

    /**
     * Transitions from config to output.
     */
    private ServerError startOutput() {

        retrieve.initialize(factory);
        OMEXMLMetadata xmlMetadata = convertXml(retrieve);
        if (xmlMetadata != null) {
            Object root = xmlMetadata.getRoot();
            if (root instanceof OMEXMLNode) {
                OMEXMLNode node = (OMEXMLNode) root;

                try {

                    file = File.createTempFile("ome", "xml");
                    file.deleteOnExit();
                    FileOutputStream fos = new FileOutputStream(file);
                    DOMUtil.writeXML(fos, node.getDOMElement()
                            .getOwnerDocument());
                    fos.close();
                    retrieve = null;
                    offset = 0;

                    return null; // ONLY VALID EXIT

                } catch (IOException ioe) {
                    log.error(ioe);
                } catch (TransformerException e) {
                    log.error(e);
                }

            }
        }

        return new omero.InternalException(null, null,
                "Failed to create export");

    }

    /**
     * Read size bytes, and transition to "waiting" If any exception is thrown,
     * the offset for the current file will not be updated.
     */
    private byte[] read(int size) {
        if (size > MAX_SIZE) {
            throw new ApiUsageException("Max read size is: " + MAX_SIZE);
        }

        byte[] buf = new byte[size];

        RandomAccessFile ra = null;
        try {
            ra = new RandomAccessFile(file, "r");
            ra.seek(offset);
            int read = ra.read(buf);

            // Handle end of file
            if (read < 0) {
                offset = read; // Transition to waiting
            } else if (read < size) {
                offset = -1; // Transition to waiting
                byte[] newBuf = new byte[read];
                System.arraycopy(buf, 0, newBuf, 0, read);
                return newBuf;
            } else {
                // This should be fairly unlikely, but if the last read
                // brought us to the end of the file, then we should go
                // ahead and reset so that the next call doesn't block.
                if ((offset + read) == ra.length()) {
                    offset = -2; // Transition to waiting
                } else {
                    offset += read;
                }
            }

        } catch (IOException io) {

            throw new RuntimeException(io);
        } finally {

            if (ra != null) {
                try {
                    ra.close();
                } catch (IOException e) {
                    log.warn("IOException on file close");
                }
            }

        }

        return buf;
    }

    // XML Generation (public for testing)
    // =========================================================================

    public static OMEXMLMetadata convertXml(MetadataRetrieve retrieve) {
        try {
            OMEXMLMetadata xmlMeta = (OMEXMLMetadata) MetadataTools
                    .createOMEXMLMetadata();
            xmlMeta.setRoot(OMEXMLFactory.newOMENode());
            MetadataTools.convertMetadata(retrieve, xmlMeta);
            return xmlMeta;
        } catch (ClassCastException cce) {
            return null;
        } catch (IOException io) {
            return null;
        } catch (ParserConfigurationException e) {
            return null;
        } catch (SAXException e) {
            return null;
        }
    }

    public static String generateXml(MetadataRetrieve retrieve) {
        IMetadata xmlMeta = convertXml(retrieve);
        return MetadataTools.getOMEXML(xmlMeta);
    }

    // Stateful interface methods
    // =========================================================================

    public void activate_async(AMD_StatefulServiceInterface_activate __cb,
            Current __current) {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void passivate_async(AMD_StatefulServiceInterface_passivate __cb,
            Current __current) {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void close_async(AMD_StatefulServiceInterface_close __cb,
            Current __current) {
        callInvokerOnRawArgs(__cb, __current);

    }

    public void getCurrentEventContext_async(
            AMD_StatefulServiceInterface_getCurrentEventContext __cb,
            Current __current) throws ServerError {
        callInvokerOnRawArgs(__cb, __current);

    }

    // Inner classes
    // =========================================================================

    public static class Retrieve implements MetadataRetrieve {

        private final List<Image> images = new ArrayList<Image>();

        private static String nsString(omero.RString rs) {
            return rs == null ? null : rs.getValue();
        }

        private static String lsid(Class k, String id) {
            return String.format("urn:lsid:%s:%s", k.getName(), id);
        }

        private static String lsid(IObject obj) {

            if (obj == null) {
                return null;
            }

            Details d = obj.getDetails();
            ExternalInfo ei = d.getExternalInfo();

            // If an LSID has previously been set, always use that.
            if (ei != null && ei.getLsid() != null) {
                return ei.getLsid().getValue();
            }

            // Otherwise if we have an ID use that as the value.
            if (obj.getId() != null) {
                return lsid(obj.getClass(), "" + obj.getId().getValue());
            }

            // Finally, we need to create an LSID since this object
            // doesn't have one. This should not be done in the general
            // case, since all exported objects should be coming from
            // the database. On re-import they will be given their
            // LSIDs.

            if (ei == null) {
                ei = new ExternalInfoI();
                d.setExternalInfo(ei);
            }

            String uuid = UUID.randomUUID().toString();
            String lsid = lsid(obj.getClass(), uuid);
            ei.setLsid(rstring(lsid));
            return ei.getLsid().getValue();

        }

        public void initialize(ServiceFactoryI factory) {

            Executor ex = factory.getExecutor();
            Principal p = factory.principal;

            Map<Image, Image> replacements = new HashMap<Image, Image>();
            for (Image image : images) {
                if (!image.isLoaded()) {

                    final Image i = image;
                    Work work = new SimpleWork(this, "initialize") {
                        @Transactional(readOnly = true)
                        public Object doWork(Session session, ServiceFactory sf) {
                            return session.get("ome.model.core.Image", i
                                    .getId().getValue());
                        }
                    };

                    Filterable replacement = (Filterable) ex.execute(p, work);
                    IceMapper mapper = new IceMapper();
                    replacements.put(image, (Image) mapper.map(replacement));

                }
            }

            List<Image> newImages = new ArrayList<Image>();
            for (int i = 0; i < images.size(); i++) {
                Image image = images.get(i);
                Image replacement = replacements.get(image);
                if (replacement != null) {
                    newImages.add(replacement);
                } else {
                    newImages.add(image);
                }
            }
            this.images.clear();
            this.images.addAll(newImages);

        }

        public void addImage(Image image) {
            images.add(image);
        }

        public String getArcType(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getChannelComponentColorDomain(int arg0, int arg1,
                int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getChannelComponentCount(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return 0;
        }

        public Integer getChannelComponentIndex(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getChannelComponentPixels(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getCircleCx(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getCircleCy(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getCircleID(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getCircleR(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getCircleTransform(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getContactExperimenter(int arg0) {
            // TODO Auto-generated method stub
            return null;
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

        public Boolean getDatasetLocked(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getDatasetName(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getDatasetRefCount(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getDatasetRefID(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getDetectorAmplificationGain(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getDetectorCount(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        public Float getDetectorGain(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getDetectorID(int arg0, int arg1) {
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

        public Float getDetectorOffset(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getDetectorSerialNumber(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getDetectorSettingsBinning(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getDetectorSettingsDetector(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getDetectorSettingsGain(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getDetectorSettingsOffset(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getDetectorSettingsReadOutRate(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getDetectorSettingsVoltage(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getDetectorType(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getDetectorVoltage(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getDetectorZoom(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getDichroicCount(int arg0) {
            // TODO Auto-generated method stub
            return 0;
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

        public Float getDimensionsPhysicalSizeX(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getDimensionsPhysicalSizeY(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getDimensionsPhysicalSizeZ(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getDimensionsTimeIncrement(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getDimensionsWaveIncrement(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getDimensionsWaveStart(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getDisplayOptionsDisplay(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getDisplayOptionsID(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getDisplayOptionsProjectionZStart(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getDisplayOptionsProjectionZStop(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getDisplayOptionsTimeTStart(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getDisplayOptionsTimeTStop(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getDisplayOptionsZoom(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getEllipseCx(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getEllipseCy(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getEllipseID(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getEllipseRx(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getEllipseRy(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getEllipseTransform(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getEmFilterLotNumber(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getEmFilterManufacturer(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getEmFilterModel(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getEmFilterType(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getExFilterLotNumber(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getExFilterManufacturer(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getExFilterModel(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getExFilterType(int arg0, int arg1) {
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

        public String getExperimentType(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getExperimenterCount() {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getExperimenterEmail(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getExperimenterFirstName(int arg0) {
            // TODO Auto-generated method stub
            return null;
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

        public int getExperimenterMembershipCount(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getExperimenterMembershipGroup(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getExperimenterOMEName(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getFilamentType(int arg0, int arg1) {
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

        public int getFilterSetCount(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getFilterSetDichroic(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getFilterSetEmFilter(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getFilterSetExFilter(int arg0, int arg1) {
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

        public String getFilterType(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getGreyChannelBlackLevel(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getGreyChannelChannelNumber(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getGreyChannelGamma(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getGreyChannelMapColorMap(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getGreyChannelWhiteLevel(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public Boolean getGreyChannelisOn(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getGroupCount() {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getGroupName(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getGroupRefCount(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getImageAcquiredPixels(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getImageCount() {
            return images.size();
        }

        public String getImageCreationDate(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getImageDefaultPixels(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getImageDescription(int arg0) {
            return nsString(images.get(arg0).getDescription());
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
            return lsid(images.get(arg0));
        }

        public String getImageInstrumentRef(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getImageName(int arg0) {
            return nsString(images.get(arg0).getName());
        }

        public String getImageObjective(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getImagingEnvironmentAirPressure(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getImagingEnvironmentCO2Percent(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getImagingEnvironmentHumidity(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getImagingEnvironmentTemperature(int arg0) {
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

        public Integer getLaserFrequencyMultiplication(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLaserLaserMedium(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Boolean getLaserPockelCell(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLaserPulse(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Boolean getLaserRepetitionRate(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Boolean getLaserTuneable(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLaserType(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getLaserWavelength(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getLightSourceCount(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getLightSourceID(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLightSourceManufacturer(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLightSourceModel(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getLightSourcePower(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getLightSourceRefAttenuation(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getLightSourceRefCount(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getLightSourceRefLightSource(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getLightSourceRefWavelength(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLightSourceSerialNumber(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getLightSourceSettingsAttenuation(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLightSourceSettingsLightSource(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getLightSourceSettingsWavelength(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLineID(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLineTransform(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLineX1(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLineX2(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLineY1(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLineY2(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLogicalChannelContrastMethod(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getLogicalChannelCount(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getLogicalChannelDetector(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getLogicalChannelEmWave(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getLogicalChannelExWave(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLogicalChannelFilterSet(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLogicalChannelFluor(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLogicalChannelID(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLogicalChannelIlluminationType(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLogicalChannelLightSource(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLogicalChannelMode(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLogicalChannelName(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getLogicalChannelNdFilter(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLogicalChannelOTF(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLogicalChannelPhotometricInterpretation(int arg0,
                int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getLogicalChannelPinholeSize(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getLogicalChannelPockelCellSetting(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getLogicalChannelSamplesPerPixel(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLogicalChannelSecondaryEmissionFilter(int arg0,
                int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLogicalChannelSecondaryExcitationFilter(int arg0,
                int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getMaskHeight(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getMaskID(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Boolean getMaskPixelsBigEndian(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getMaskPixelsBinData(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getMaskPixelsExtendedPixelType(int arg0, int arg1,
                int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getMaskPixelsID(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getMaskPixelsSizeX(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getMaskPixelsSizeY(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getMaskTransform(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getMaskWidth(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getMaskX(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getMaskY(int arg0, int arg1, int arg2) {
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

        public int getMicrobeamManipulationRefCount(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getMicrobeamManipulationRefID(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getMicrobeamManipulationType(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getMicroscopeID(int arg0) {
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

        public String getMicroscopeType(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getOTFBinaryFile(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getOTFCount(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getOTFID(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getOTFObjective(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Boolean getOTFOpticalAxisAveraged(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getOTFPixelType(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getOTFSizeX(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getOTFSizeY(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getObjectiveCalibratedMagnification(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getObjectiveCorrection(int arg0, int arg1) {
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

        public String getObjectiveImmersion(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Boolean getObjectiveIris(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getObjectiveLensNA(int arg0, int arg1) {
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

        public Integer getObjectiveNominalMagnification(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getObjectiveSerialNumber(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getObjectiveSettingsCorrectionCollar(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getObjectiveSettingsMedium(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getObjectiveSettingsObjective(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getObjectiveSettingsRefractiveIndex(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getObjectiveWorkingDistance(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getPathD(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getPathID(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Boolean getPixelsBigEndian(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getPixelsCount(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getPixelsDimensionOrder(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getPixelsID(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getPixelsPixelType(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getPixelsSizeC(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getPixelsSizeT(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getPixelsSizeX(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getPixelsSizeY(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getPixelsSizeZ(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getPlaneCount(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getPlaneHashSHA1(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getPlaneID(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getPlaneTheC(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getPlaneTheT(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getPlaneTheZ(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getPlaneTimingDeltaT(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getPlaneTimingExposureTime(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getPlateColumnNamingConvention(int arg0) {
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

        public String getPlateRefID(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getPlateRefSample(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getPlateRefWell(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getPlateRowNamingConvention(int arg0) {
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

        public String getPointCx(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getPointCy(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getPointID(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getPointR(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getPointTransform(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getPolygonID(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getPolygonPoints(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getPolygonTransform(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getPolylineID(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getPolylinePoints(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getPolylineTransform(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
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

        public String getProjectRefID(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getPumpLightSource(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getROICount(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getROIID(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getROIRefCount(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getROIRefID(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getROIT0(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getROIT1(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getROIX0(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getROIX1(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getROIY0(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getROIY1(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getROIZ0(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getROIZ1(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
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

        public String getRectHeight(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getRectID(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getRectTransform(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getRectWidth(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getRectX(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getRectY(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getRegionCount(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getRegionID(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getRegionName(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getRegionTag(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getScreenAcquisitionCount(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getScreenAcquisitionEndTime(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getScreenAcquisitionID(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getScreenAcquisitionStartTime(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getScreenCount() {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getScreenDescription(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getScreenExtern(int arg0) {
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

        public String getScreenRefID(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getScreenType(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getShapeBaselineShift(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getShapeCount(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getShapeDirection(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getShapeFillColor(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getShapeFillOpacity(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getShapeFillRule(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getShapeFontFamily(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getShapeFontSize(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getShapeFontStretch(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getShapeFontStyle(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getShapeFontVariant(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getShapeFontWeight(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getShapeG(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getShapeGlyphOrientationVertical(int arg0, int arg1,
                int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getShapeID(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Boolean getShapeLocked(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getShapeStrokeAttribute(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getShapeStrokeColor(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getShapeStrokeDashArray(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getShapeStrokeLineCap(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getShapeStrokeLineJoin(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getShapeStrokeMiterLimit(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getShapeStrokeOpacity(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getShapeStrokeWidth(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getShapeText(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getShapeTextAnchor(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getShapeTextDecoration(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getShapeTextFill(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getShapeTextStroke(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getShapeTheT(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getShapeTheZ(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getShapeVectorEffect(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Boolean getShapeVisibility(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getShapeWritingMode(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getStageLabelName(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getStageLabelX(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getStageLabelY(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getStageLabelZ(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getStagePositionPositionX(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getStagePositionPositionY(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getStagePositionPositionZ(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getThumbnailHref(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getThumbnailID(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getThumbnailMIMEtype(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getTiffDataCount(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getTiffDataFileName(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getTiffDataFirstC(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getTiffDataFirstT(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getTiffDataFirstZ(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getTiffDataIFD(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getTiffDataNumPlanes(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getTiffDataUUID(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getTransmittanceRangeCutIn(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getTransmittanceRangeCutInTolerance(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getTransmittanceRangeCutOut(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getTransmittanceRangeCutOutTolerance(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getTransmittanceRangeTransmittance(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getUUID() {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getWellColumn(int arg0, int arg1) {
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

        public String getWellReagent(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getWellRow(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
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

        public Integer getWellSampleIndex(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getWellSamplePosX(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Float getWellSamplePosY(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public int getWellSampleRefCount(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getWellSampleRefID(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public Integer getWellSampleTimepoint(int arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            return null;
        }

        public String getWellType(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

    }
}
