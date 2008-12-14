package ome.formats;

import java.io.File;
import java.security.MessageDigest;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

import static omero.rtypes.*;
import omero.RBool;
import omero.RDouble;
import omero.RFloat;
import omero.RInt;
import omero.RLong;
import omero.RString;
import omero.RType;
import omero.ServerError;
import omero.client;
import omero.api.IAdminPrx;
import omero.api.IPojosPrx;
import omero.api.IQueryPrx;
import omero.api.IRepositoryInfoPrx;
import omero.api.IUpdatePrx;
import omero.api.MetadataStorePrx;
import omero.api.MetadataStorePrxHelper;
import omero.api.RawFileStorePrx;
import omero.api.RawPixelsStorePrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceInterfacePrx;
import omero.constants.METADATASTORE;
import omero.model.BooleanAnnotation;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.Pixels;
import omero.model.PixelsAnnotationLink;
import omero.model.PixelsAnnotationLinkI;
import omero.model.PixelsI;
import omero.model.PlaneInfo;
import omero.model.ProjectI;
import omero.model.Project;

import loci.formats.meta.IMinMaxStore;
import loci.formats.meta.MetadataStore;


public class OMEROMetadataStoreClient implements MetadataStore, IMinMaxStore
{
    /** Logger for this class. */
    private static Log     log    = LogFactory.getLog(OMEROMetadataStoreClient.class);
    
    MetadataStorePrx delegate;
    
    ServiceFactoryPrx serviceFactory;
    IUpdatePrx iUpdate;
    IQueryPrx iQuery;
    IAdminPrx iAdmin;
    RawFileStorePrx rawFileStore;
    RawPixelsStorePrx rawPixelStore;
    IRepositoryInfoPrx iRepoInfo;
    IPojosPrx iPojos;

    private Long currentPixId;
    
    private PlaneInfo pInfo;

    private void initialize()
    	throws ServerError
    {
    	 iUpdate = serviceFactory.getUpdateService();
         iQuery = serviceFactory.getQueryService();
         iAdmin = serviceFactory.getAdminService();
         rawFileStore = serviceFactory.createRawFileStore();
         rawPixelStore = serviceFactory.createRawPixelsStore();
         iRepoInfo = serviceFactory.getRepositoryInfoService();
         iPojos = serviceFactory.getPojosService();
         
         
         delegate = MetadataStorePrxHelper.checkedCast(serviceFactory.getByName(METADATASTORE.value));        
    }
    
    /**
     * Creates a new instance.
     * 
     * @param serviceFactory The factory. Mustn't be <code>null</code>.
     */
    public OMEROMetadataStoreClient(ServiceFactoryPrx serviceFactory)
    	throws ServerError
    {
    	if (serviceFactory == null)
    		throw new IllegalArgumentException("No factory.");
    	this.serviceFactory = serviceFactory;
    	initialize();
    }
    
    public OMEROMetadataStoreClient(String username, String password, String server,
            String port) throws CannotCreateSessionException, PermissionDeniedException, ServerError
    {
        client c = new client(server);
        serviceFactory = c.createSession(username, password);
        initialize();
    }

    public void ping()
    {
        serviceFactory.keepAllAlive(new ServiceInterfacePrx[] 
                {iQuery, iAdmin, rawFileStore, rawPixelStore, iRepoInfo, iPojos, iUpdate, delegate});
        
        log.debug("KeepAlive ping");

    }
    
    /**
     * Transforms a Java type into the corresponding OMERO RType.
     * @param value Java concrete type value.
     * @return RType or <code>null</code> if <code>value</code> is 
     * <code>null</code>.
     */
    public RInt toRType(Integer value)
    {
        return value == null? null : rint(value);
    }
    
    /**
     * Transforms a Java type into the corresponding OMERO RType.
     * @param value Java concrete type value.
     * @return RType or <code>null</code> if <code>value</code> is 
     * <code>null</code>.
     */
    public RLong toRType(Long value)
    {
        return value == null? null : rlong(value);
    }
    
    /**
     * Transforms a Java type into the corresponding OMERO RType.
     * @param value Java concrete type value.
     * @return RType or <code>null</code> if <code>value</code> is 
     * <code>null</code>.
     */
    public RString toRType(String value)
    {
        return value == null? null : rstring(value);
    }
    
    /**
     * Transforms a Java type into the corresponding OMERO RType.
     * @param value Java concrete type value.
     * @return RType or <code>null</code> if <code>value</code> is 
     * <code>null</code>.
     */
    public RBool toRType(Boolean value)
    {
        return value == null? null : rbool(value);
    }
    
    /**
     * Transforms a Java type into the corresponding OMERO RType.
     * @param value Java concrete type value.
     * @return RType or <code>null</code> if <code>value</code> is 
     * <code>null</code>.
     */
    public RDouble toRType(Double value)
    {
        return value == null? null : rdouble(value);
    }
    
    /**
     * Transforms a Java type into the corresponding OMERO RType.
     * @param value Java concrete type value.
     * @return RType or <code>null</code> if <code>value</code> is 
     * <code>null</code>.
     */
    public RFloat toRType(Float value)
    {
        return value == null? null : rfloat(value);
    }
    
    public void logout()
    {
        serviceFactory.destroy();
    }
    
    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#createRoot()
     */
    public void createRoot()
    {
        try
        {
            delegate.createRoot();
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#getRoot()
     */
    public Object getRoot()
    {
        try
        {
            return delegate.getRoot();
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setArcType(java.lang.String, int, int)
     */
    public void setArcType(String type, int instrumentIndex,
            int lightSourceIndex)
    {
        try
        {
            delegate.setArcType(toRType(type), instrumentIndex, lightSourceIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelComponentColorDomain(java.lang.String, int, int, int)
     */
    public void setChannelComponentColorDomain(String colorDomain,
            int imageIndex, int logicalChannelIndex, int channelComponentIndex)
    {
        try
        {
            delegate.setChannelComponentColorDomain(toRType(colorDomain), 
                    imageIndex, logicalChannelIndex, channelComponentIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelComponentIndex(java.lang.Integer, int, int, int)
     */
    public void setChannelComponentIndex(Integer index, int imageIndex,
            int logicalChannelIndex, int channelComponentIndex)
    {
        try
        {
            delegate.setChannelComponentIndex(toRType(index), 
                    imageIndex, logicalChannelIndex, channelComponentIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorGain(java.lang.Float, int, int)
     */
    public void setDetectorGain(Float gain, int instrumentIndex,
            int detectorIndex)
    {
        try
        {
            delegate.setDetectorGain(toRType(gain), instrumentIndex, detectorIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDetectorID(String id, int instrumentIndex, int detectorIndex)
    {
        try
        {
            delegate.setDetectorID(toRType(id), instrumentIndex, detectorIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDetectorManufacturer(String manufacturer,
            int instrumentIndex, int detectorIndex)
    {
        try
        {
            delegate.setDetectorManufacturer(toRType(manufacturer), instrumentIndex, detectorIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDetectorModel(String model, int instrumentIndex,
            int detectorIndex)
    {
        try
        {
            delegate.setDetectorModel(toRType(model), instrumentIndex, detectorIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDetectorOffset(Float offset, int instrumentIndex,
            int detectorIndex)
    {
        try
        {
            delegate.setDetectorOffset(toRType(offset), instrumentIndex, detectorIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDetectorSerialNumber(String serialNumber,
            int instrumentIndex, int detectorIndex)
    {
        try
        {
            delegate.setDetectorSerialNumber(toRType(serialNumber), instrumentIndex, detectorIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDetectorSettingsDetector(String detector, int imageIndex,
            int logicalChannelIndex)
    {
        try
        {
            delegate.setDetectorSettingsDetector(toRType(detector), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }


    public void setDetectorSettingsBinning(String binning, int imageIndex,
            int logicalChannelIndex)
    {
        try
        {
            delegate.setDetectorSettingsBinning(toRType(binning), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDetectorSettingsReadOutRate(Float readOutRate,
            int imageIndex, int logicalChannelIndex)
    {
        try
        {
            delegate.setDetectorSettingsReadOutRate(toRType(readOutRate), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDetectorSettingsVoltage(Float voltage, int imageIndex,
            int logicalChannelIndex)
    {
        try
        {
            delegate.setDetectorSettingsVoltage(toRType(voltage), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDetectorSettingsGain(Float gain, int imageIndex,
            int logicalChannelIndex)
    {
        try
        {
            delegate.setDetectorSettingsGain(toRType(gain), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDetectorSettingsOffset(Float offset, int imageIndex,
            int logicalChannelIndex)
    {
        try
        {
            delegate.setDetectorSettingsOffset(toRType(offset), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDetectorType(String type, int instrumentIndex,
            int detectorIndex)
    {
        try
        {
            delegate.setDetectorType(toRType(type), instrumentIndex, detectorIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDetectorVoltage(Float voltage, int instrumentIndex,
            int detectorIndex)
    {
        try
        {
            delegate.setDetectorVoltage(toRType(voltage), instrumentIndex, detectorIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDimensionsPhysicalSizeX(Float physicalSizeX, int imageIndex,
            int pixelsIndex)
    {
        try
        {
            delegate.setDimensionsPhysicalSizeX(toRType(physicalSizeX), imageIndex, pixelsIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDimensionsPhysicalSizeY(Float physicalSizeY, int imageIndex,
            int pixelsIndex)
    {
        try
        {
            delegate.setDimensionsPhysicalSizeY(toRType(physicalSizeY), imageIndex, pixelsIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDimensionsPhysicalSizeZ(Float physicalSizeZ, int imageIndex,
            int pixelsIndex)
    {
        try
        {
            delegate.setDimensionsPhysicalSizeZ(toRType(physicalSizeZ), imageIndex, pixelsIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDimensionsTimeIncrement(Float timeIncrement, int imageIndex,
            int pixelsIndex)
    {
        try
        {
            delegate.setDimensionsTimeIncrement(toRType(timeIncrement), imageIndex, pixelsIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDimensionsWaveIncrement(Integer waveIncrement,
            int imageIndex, int pixelsIndex)
    {
        try
        {
            delegate.setDimensionsWaveIncrement(toRType(waveIncrement), imageIndex, pixelsIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDimensionsWaveStart(Integer waveStart, int imageIndex,
            int pixelsIndex)
    {
        try
        {
            delegate.setDimensionsWaveStart(toRType(waveStart), imageIndex, pixelsIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDisplayOptionsID(String id, int imageIndex)
    {
        try
        {
            delegate.setDisplayOptionsID(toRType(id), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDisplayOptionsProjectionZStart(Integer start, int imageIndex)
    {
        try
        {
            delegate.setDisplayOptionsProjectionZStart(toRType(start), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDisplayOptionsProjectionZStop(Integer stop, int imageIndex)
    {
        try
        {
            delegate.setDisplayOptionsProjectionZStop(toRType(stop), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDisplayOptionsTimeTStart(Integer start, int imageIndex)
    {
        try
        {
            delegate.setDisplayOptionsTimeTStart(toRType(start), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDisplayOptionsTimeTStop(Integer stop, int imageIndex)
    {
        try
        {
            delegate.setDisplayOptionsTimeTStop(toRType(stop), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDisplayOptionsZoom(Float zoom, int imageIndex)
    {
        try
        {
            delegate.setDisplayOptionsZoom(toRType(zoom), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setExperimenterEmail(String email, int experimenterIndex)
    {
        try
        {
            delegate.setExperimenterEmail(toRType(email), experimenterIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setExperimenterFirstName(String firstName, int experimenterIndex)
    {
        try
        {
            delegate.setExperimenterFirstName(toRType(firstName), experimenterIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setExperimenterID(String id, int experimenterIndex)
    {
        return;
    }

    public void setExperimenterInstitution(String institution,
            int experimenterIndex)
    {
        try
        {
            delegate.setExperimenterInstitution(toRType(institution), experimenterIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setExperimenterLastName(String lastName, int experimenterIndex)
    {
        try
        {
            delegate.setExperimenterLastName(toRType(lastName), experimenterIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setFilamentType(String type, int instrumentIndex,
            int lightSourceIndex)
    {
        try
        {
            delegate.setFilamentType(toRType(type), instrumentIndex, lightSourceIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setImageCreationDate(String creationDate, int imageIndex)
    {
        try
        {
            delegate.setImageCreationDate(toRType(creationDate), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setImageDescription(String description, int imageIndex)
    {
        try
        {
            delegate.setImageDescription(toRType(description), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setImageID(String id, int imageIndex)
    {
        try
        {
            delegate.setImageID(toRType(id), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setImageInstrumentRef(String instrumentRef, int imageIndex)
    {
        try
        {
            delegate.setImageInstrumentRef(toRType(instrumentRef), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setImageName(String name, int imageIndex)
    {
        try
        {
            delegate.setImageName(toRType(name), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setImagingEnvironmentAirPressure(Float airPressure,
            int imageIndex)
    {
        try
        {
            delegate.setImagingEnvironmentAirPressure(toRType(airPressure), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setImagingEnvironmentCO2Percent(Float percent, int imageIndex)
    {
        try
        {
            delegate.setImagingEnvironmentCO2Percent(toRType(percent), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setImagingEnvironmentHumidity(Float humidity, int imageIndex)
    {
        try
        {
            delegate.setImagingEnvironmentHumidity(toRType(humidity), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setImagingEnvironmentTemperature(Float temperature,
            int imageIndex)
    {
        try
        {
            delegate.setImagingEnvironmentTemperature(toRType(temperature), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setInstrumentID(String id, int instrumentIndex)
    {
        try
        {
            delegate.setInstrumentID(toRType(id), instrumentIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLaserFrequencyMultiplication(
            Integer frequencyMultiplication, int instrumentIndex,
            int lightSourceIndex)
    {
        try
        {
            delegate.setLaserFrequencyMultiplication(toRType(frequencyMultiplication), instrumentIndex, lightSourceIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLaserLaserMedium(String laserMedium, int instrumentIndex,
            int lightSourceIndex)
    {
        try
        {
            delegate.setLaserLaserMedium(toRType(laserMedium), instrumentIndex, lightSourceIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLaserPulse(String pulse, int instrumentIndex,
            int lightSourceIndex)
    {
        try
        {
            delegate.setLaserPulse(toRType(pulse), instrumentIndex, lightSourceIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLaserTuneable(Boolean tuneable, int instrumentIndex,
            int lightSourceIndex)
    {
        try
        {
            delegate.setLaserTuneable(toRType(tuneable), instrumentIndex, lightSourceIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLaserType(String type, int instrumentIndex,
            int lightSourceIndex)
    {
        try
        {
            delegate.setLaserType(toRType(type), instrumentIndex, lightSourceIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLaserWavelength(Integer wavelength, int instrumentIndex,
            int lightSourceIndex)
    {
        try
        {
            delegate.setLaserWavelength(toRType(wavelength), instrumentIndex, lightSourceIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLightSourceID(String id, int instrumentIndex,
            int lightSourceIndex)
    {
        try
        {
            delegate.setLightSourceID(toRType(id), instrumentIndex, lightSourceIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLightSourceManufacturer(String manufacturer,
            int instrumentIndex, int lightSourceIndex)
    {
        try
        {
            delegate.setLightSourceManufacturer(toRType(manufacturer), instrumentIndex, lightSourceIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLightSourceModel(String model, int instrumentIndex,
            int lightSourceIndex)
    {
        try
        {
            delegate.setLightSourceModel(toRType(model), instrumentIndex, lightSourceIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLightSourcePower(Float power, int instrumentIndex,
            int lightSourceIndex)
    {
        try
        {
            delegate.setLightSourcePower(toRType(power), instrumentIndex, lightSourceIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLightSourceSerialNumber(String serialNumber,
            int instrumentIndex, int lightSourceIndex)
    {
        try
        {
            delegate.setLightSourceSerialNumber(toRType(serialNumber), instrumentIndex, lightSourceIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLightSourceSettingsAttenuation(Float attenuation,
            int imageIndex, int logicalChannelIndex)
    {
        try
        {
            delegate.setLightSourceSettingsAttenuation(toRType(attenuation), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLightSourceSettingsLightSource(String lightSource,
            int imageIndex, int logicalChannelIndex)
    {
        try
        {
            delegate.setLightSourceSettingsLightSource(toRType(lightSource), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLightSourceSettingsWavelength(Integer wavelength,
            int imageIndex, int logicalChannelIndex)
    {
        try
        {
            delegate.setLightSourceSettingsWavelength(toRType(wavelength), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLogicalChannelContrastMethod(String contrastMethod,
            int imageIndex, int logicalChannelIndex)
    {
        try
        {
            delegate.setLogicalChannelContrastMethod(toRType(contrastMethod), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLogicalChannelEmWave(Integer emWave, int imageIndex,
            int logicalChannelIndex)
    {
        //TODO: Fix this hack on the server
        if (emWave == null)
            return;
        
         try
        {
             log.debug(String.format(
                     "Setting Image[%d] LogicalChannel[%d] emission wavelength: '%d'",
                     imageIndex, logicalChannelIndex, emWave));
            delegate.setLogicalChannelEmWave(toRType(emWave), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLogicalChannelExWave(Integer exWave, int imageIndex,
            int logicalChannelIndex)
    {
        if (exWave == null)
            return;
         try
        {
            delegate.setLogicalChannelExWave(toRType(exWave), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLogicalChannelFluor(String fluor, int imageIndex,
            int logicalChannelIndex)
    {
        try
        {
            delegate.setLogicalChannelFluor(toRType(fluor), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLogicalChannelID(String id, int imageIndex,
            int logicalChannelIndex)
    {
        try
        {
            delegate.setLogicalChannelID(toRType(id), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLogicalChannelIlluminationType(String illuminationType,
            int imageIndex, int logicalChannelIndex)
    {
        try
        {
            delegate.setLogicalChannelID(toRType(illuminationType), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLogicalChannelMode(String mode, int imageIndex,
            int logicalChannelIndex)
    {
        try
        {
            delegate.setLogicalChannelMode(toRType(mode), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLogicalChannelName(String name, int imageIndex,
            int logicalChannelIndex)
    {
        try
        {
            delegate.setLogicalChannelName(toRType(name), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLogicalChannelNdFilter(Float ndFilter, int imageIndex,
            int logicalChannelIndex)
    {
        try
        {
            delegate.setLogicalChannelNdFilter(toRType(ndFilter), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLogicalChannelPhotometricInterpretation(
            String photometricInterpretation, int imageIndex,
            int logicalChannelIndex)
    {
        try
        {
            delegate.setLogicalChannelPhotometricInterpretation(toRType(photometricInterpretation), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLogicalChannelPinholeSize(Float pinholeSize,
            int imageIndex, int logicalChannelIndex)
    {
        try
        {
            delegate.setLogicalChannelPinholeSize(toRType(pinholeSize), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLogicalChannelPockelCellSetting(Integer pockelCellSetting,
            int imageIndex, int logicalChannelIndex)
    {
        try
        {
            delegate.setLogicalChannelPockelCellSetting(toRType(pockelCellSetting), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLogicalChannelSamplesPerPixel(Integer samplesPerPixel,
            int imageIndex, int logicalChannelIndex)
    {
        try
        {
            delegate.setLogicalChannelSamplesPerPixel(toRType(samplesPerPixel), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setOTFID(String id, int instrumentIndex, int otfIndex)
    {
        try
        {
            delegate.setOTFID(toRType(id), instrumentIndex, otfIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setOTFOpticalAxisAveraged(Boolean opticalAxisAveraged,
            int instrumentIndex, int otfIndex)
    {
        try
        {
            delegate.setOTFOpticalAxisAveraged(toRType(opticalAxisAveraged), instrumentIndex, otfIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setOTFPixelType(String pixelType, int instrumentIndex,
            int otfIndex)
    {
        try
        {
            delegate.setOTFPixelType(toRType(pixelType), instrumentIndex, otfIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setOTFSizeX(Integer sizeX, int instrumentIndex, int otfIndex)
    {
        try
        {
            delegate.setOTFSizeX(toRType(sizeX), instrumentIndex, otfIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setOTFSizeY(Integer sizeY, int instrumentIndex, int otfIndex)
    {
        try
        {
            delegate.setOTFSizeY(toRType(sizeY), instrumentIndex, otfIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setObjectiveIris(Boolean iris, int instrumentIndex,
            int objectiveIndex)
    {
        try
        {
            delegate.setObjectiveIris(toRType(iris), instrumentIndex, objectiveIndex);
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public void setObjectiveCalibratedMagnification(
            Float calibratedMagnification, int instrumentIndex,
            int objectiveIndex)
    {
        log.debug(String.format(
                "setObjectiveCalibratedMagnification[%f] instrumentIndex[%d] objectiveIndex[%d]",
                calibratedMagnification, instrumentIndex, objectiveIndex));
        try
        {
            delegate.setObjectiveCalibratedMagnification(toRType(calibratedMagnification), instrumentIndex, objectiveIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setObjectiveCorrection(String correction, int instrumentIndex,
            int objectiveIndex)
    {
        log.debug(String.format(
                "setObjectiveCorrection[%s] instrumentIndex[%d] objectiveIndex[%d]",
                correction, instrumentIndex, objectiveIndex));
        try
        {
            delegate.setObjectiveCorrection(toRType(correction), instrumentIndex, objectiveIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setObjectiveID(String id, int instrumentIndex,
            int objectiveIndex)
    {
        log.debug(String.format(
                "setObjectiveID[%s] instrumentIndex[%d] objectiveIndex[%d]",
                id, instrumentIndex, objectiveIndex));
        try
        {
            delegate.setObjectiveID(toRType(id), instrumentIndex, objectiveIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setObjectiveImmersion(String immersion, int instrumentIndex,
            int objectiveIndex)
    {
        log.debug(String.format(
                "setObjectiveImmersion[%s] instrumentIndex[%d] objectiveIndex[%d]",
                immersion, instrumentIndex, objectiveIndex));
        try
        {
            delegate.setObjectiveImmersion(toRType(immersion), instrumentIndex, objectiveIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setObjectiveLensNA(Float lensNA, int instrumentIndex,
            int objectiveIndex)
    {
        log.debug(String.format(
                "setObjectiveLensNA[%f] instrumentIndex[%d] objectiveIndex[%d]",
                lensNA, instrumentIndex, objectiveIndex));
        try
        {
            delegate.setObjectiveLensNA(toRType(lensNA), instrumentIndex, objectiveIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setObjectiveManufacturer(String manufacturer,
            int instrumentIndex, int objectiveIndex)
    {
        log.debug(String.format(
                "setObjectiveManufacturer[%s] instrumentIndex[%d] objectiveIndex[%d]",
                manufacturer, instrumentIndex, objectiveIndex));
        try
        {
            delegate.setObjectiveManufacturer(toRType(manufacturer), instrumentIndex, objectiveIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setObjectiveModel(String model, int instrumentIndex,
            int objectiveIndex)
    {
        log.debug(String.format(
                "setObjectiveModel[%s] instrumentIndex[%d] objectiveIndex[%d]",
                model, instrumentIndex, objectiveIndex));
        try
        {
            delegate.setObjectiveModel(toRType(model), instrumentIndex, objectiveIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setObjectiveNominalMagnification(Integer nominalMagnification,
            int instrumentIndex, int objectiveIndex)
    {
        log.debug(String.format(
                "setObjectiveNominalMagnification[%d] instrumentIndex[%d] objectiveIndex[%d]",
                nominalMagnification, instrumentIndex, objectiveIndex));
        try
        {
            delegate.setObjectiveNominalMagnification(toRType(nominalMagnification), instrumentIndex, objectiveIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setObjectiveSerialNumber(String serialNumber,
            int instrumentIndex, int objectiveIndex)
    {
        log.debug(String.format(
                "setObjectiveSerialNumber[%s] instrumentIndex[%d] objectiveIndex[%d]",
                serialNumber, instrumentIndex, objectiveIndex));
        try
        {
            delegate.setObjectiveSerialNumber(toRType(serialNumber), instrumentIndex, objectiveIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setObjectiveWorkingDistance(Float workingDistance,
            int instrumentIndex, int objectiveIndex)
    {
        log.debug(String.format(
                "setObjectiveWorkingDistance[%f] instrumentIndex[%d] objectiveIndex[%d]",
                workingDistance, instrumentIndex, objectiveIndex));
        try
        {
            delegate.setObjectiveWorkingDistance(toRType(workingDistance), instrumentIndex, objectiveIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPixelsBigEndian(Boolean bigEndian, int imageIndex,
            int pixelsIndex)
    {
        try
        {
            delegate.setPixelsBigEndian(toRType(bigEndian), imageIndex, pixelsIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPixelsDimensionOrder(String dimensionOrder, int imageIndex,
            int pixelsIndex)
    {
        try
        {
            delegate.setPixelsDimensionOrder(toRType(dimensionOrder), imageIndex, pixelsIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPixelsID(String id, int imageIndex, int pixelsIndex)
    {
        try
        {
            delegate.setPixelsID(toRType(id), imageIndex, pixelsIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPixelsPixelType(String pixelType, int imageIndex,
            int pixelsIndex)
    {
        try
        {
            delegate.setPixelsPixelType(toRType(pixelType), imageIndex, pixelsIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPixelsSizeC(Integer sizeC, int imageIndex, int pixelsIndex)
    {
        try
        {
            delegate.setPixelsSizeC(toRType(sizeC), imageIndex, pixelsIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
        
    }

    public void setPixelsSizeT(Integer sizeT, int imageIndex, int pixelsIndex)
    {
        try
        {
            delegate.setPixelsSizeT(toRType(sizeT), imageIndex, pixelsIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
        
    }

    public void setPixelsSizeZ(Integer sizeZ, int imageIndex, int pixelsIndex)
    {
        try
        {
            delegate.setPixelsSizeZ(toRType(sizeZ), imageIndex, pixelsIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPixelsSizeX(Integer sizeX, int imageIndex, int pixelsIndex)
    {       
        try
        {
            delegate.setPixelsSizeX(toRType(sizeX), imageIndex, pixelsIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPixelsSizeY(Integer sizeY, int imageIndex, int pixelsIndex)
    {
        try
        {
            delegate.setPixelsSizeY(toRType(sizeY), imageIndex, pixelsIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPlaneTheC(Integer theC, int imageIndex, int pixelsIndex,
            int planeIndex)
    {    
        try
        {
            
            delegate.setPlaneTheC(toRType(theC), imageIndex, pixelsIndex, planeIndex);
            
            /*
            if (pInfo == null)
            {
                pInfo = new PlaneInfoI();
            }

            pInfo.setTheC(toRType(theC));

            if (pInfo.getTheC() != null && pInfo.getTheT() != null && pInfo.getTheZ() != null)
            {
                // Submit the pInfo.
                delegate.setPlaneInfo(pInfo, imageIndex, pixelsIndex, planeIndex);
            }
            */
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPlaneTheT(Integer theT, int imageIndex, int pixelsIndex,
            int planeIndex)
    {
        try
        {
            
            
            delegate.setPlaneTheT(toRType(theT), imageIndex, pixelsIndex, planeIndex);
            
            /*
            if (pInfo == null)
            {
                pInfo = new PlaneInfoI();
            }

            pInfo.setTheT(toRType(theT));  

            if (pInfo.getTheC() != null && pInfo.getTheT() != null && pInfo.getTheZ() != null)
            {
                // Submit the pInfo.
                delegate.setPlaneInfo(pInfo, imageIndex, pixelsIndex, planeIndex);
            }
            */
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPlaneTheZ(Integer theZ, int imageIndex, int pixelsIndex,
            int planeIndex)
    {
        try
        {
            
            delegate.setPlaneTheZ(toRType(theZ), imageIndex, pixelsIndex, planeIndex);
            
            /*
            if (pInfo == null)
            {
                pInfo = new PlaneInfoI();
            }

            pInfo.setTheZ(toRType(theZ));  

            if (pInfo.getTheC() != null && pInfo.getTheT() != null && pInfo.getTheZ() != null)
            {
                // Submit the pInfo.
                delegate.setPlaneInfo(pInfo, imageIndex, pixelsIndex, planeIndex);
            }
            */
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPlaneTimingDeltaT(Float deltaT, int imageIndex,
            int pixelsIndex, int planeIndex)
    {
        try
        {
            delegate.setPlaneTimingDeltaT(toRType(deltaT), imageIndex, pixelsIndex, planeIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPlaneTimingExposureTime(Float exposureTime, int imageIndex,
            int pixelsIndex, int planeIndex)
    {
        try
        {
            delegate.setPlaneTimingExposureTime(toRType(exposureTime), imageIndex, pixelsIndex, planeIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPlateDescription(String description, int plateIndex)
    {
        try
        {
            delegate.setPlateDescription(toRType(description), plateIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPlateExternalIdentifier(String externalIdentifier,
            int plateIndex)
    {
        try
        {
            delegate.setPlateExternalIdentifier(toRType(externalIdentifier), plateIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPlateID(String id, int plateIndex)
    {
        try
        {
            delegate.setPlateID(toRType(id), plateIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPlateName(String name, int plateIndex)
    {
        try
        {
            delegate.setPlateName(toRType(name), plateIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPlateRefID(String id, int screenIndex, int plateRefIndex)
    {
        try
        {
            delegate.setPlateRefID(toRType(id), screenIndex, plateRefIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPlateStatus(String status, int plateIndex)
    {
        try
        {
            delegate.setPlateStatus(toRType(status), plateIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setROIID(String id, int imageIndex, int roiIndex)
    {
        try
        {
            delegate.setROIID(toRType(id), imageIndex, roiIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setROIT0(Integer t0, int imageIndex, int roiIndex)
    {
        try
        {
            delegate.setROIT0(toRType(t0), imageIndex, roiIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setROIT1(Integer t1, int imageIndex, int roiIndex)
    {
        try
        {
            delegate.setROIT1(toRType(t1), imageIndex, roiIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setROIX0(Integer x0, int imageIndex, int roiIndex)
    {
        try
        {
            delegate.setROIX0(toRType(x0), imageIndex, roiIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setROIX1(Integer x1, int imageIndex, int roiIndex)
    {
        try
        {
            delegate.setROIX1(toRType(x1), imageIndex, roiIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setROIY0(Integer y0, int imageIndex, int roiIndex)
    {
        try
        {
            delegate.setROIY0(toRType(y0), imageIndex, roiIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setROIY1(Integer y1, int imageIndex, int roiIndex)
    {
        try
        {
            delegate.setROIY1(toRType(y1), imageIndex, roiIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setROIZ0(Integer z0, int imageIndex, int roiIndex)
    {
        try
        {
            delegate.setROIZ0(toRType(z0), imageIndex, roiIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        };
    }

    public void setROIZ1(Integer z1, int imageIndex, int roiIndex)
    {
        try
        {
            delegate.setROIZ1(toRType(z1), imageIndex, roiIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setReagentDescription(String description, int screenIndex,
            int reagentIndex)
    {
        try
        {
            delegate.setReagentDescription(toRType(description), screenIndex, reagentIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setReagentID(String id, int screenIndex, int reagentIndex)
    {
        try
        {
            delegate.setReagentID(toRType(id), screenIndex, reagentIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setReagentName(String name, int screenIndex, int reagentIndex)
    {
        try
        {
            delegate.setReagentName(toRType(name), screenIndex, reagentIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setReagentReagentIdentifier(String reagentIdentifier,
            int screenIndex, int reagentIndex)
    {
        try
        {
            delegate.setReagentReagentIdentifier(toRType(reagentIdentifier), screenIndex, reagentIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setRoot(Object root)
    {
        try
        {
             delegate.setRoot((RType) root);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setScreenAcquisitionEndTime(String endTime, int screenIndex,
            int screenAcquisitionIndex)
    {
        try
        {
             delegate.setScreenAcquisitionEndTime(
                toRType(endTime), screenIndex, screenAcquisitionIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setScreenAcquisitionID(String id, int screenIndex,
            int screenAcquisitionIndex)
    {
        try
        {
             delegate.setScreenAcquisitionStartTime(
                toRType(id), screenIndex, screenAcquisitionIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setScreenAcquisitionStartTime(String startTime,
            int screenIndex, int screenAcquisitionIndex)
    {
        try
        {
             delegate.setScreenAcquisitionStartTime(
                toRType(startTime), screenIndex, screenAcquisitionIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setScreenID(String id, int screenIndex)
    {
        try
        {
             delegate.setScreenID(toRType(id), screenIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setScreenName(String name, int screenIndex)
    {
        try
        {
             delegate.setScreenName(toRType(name), screenIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setScreenProtocolDescription(String protocolDescription,
            int screenIndex)
    {
        try
        {
             delegate.setScreenProtocolDescription(
                toRType(protocolDescription), screenIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setScreenProtocolIdentifier(String protocolIdentifier,
            int screenIndex)
    {
        try
        {
             delegate.setScreenProtocolIdentifier(
                toRType(protocolIdentifier), screenIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setScreenReagentSetDescription(String reagentSetDescription,
            int screenIndex)
    {
        try
        {
             delegate.setScreenReagentSetDescription(
                toRType(reagentSetDescription), screenIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setScreenType(String type, int screenIndex)
    {
        //FIXME: missing in delegate?
                /*
        try
        {
             delegate.setScreenType(toRType(type), screenIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
        */
                return;
    }

    public void setStageLabelName(String name, int imageIndex)
    {
        try
        {
             delegate.setStageLabelName(toRType(name), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setStageLabelX(Float x, int imageIndex)
    {
        try
        {
             delegate.setStageLabelX(toRType(x), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setStageLabelY(Float y, int imageIndex)
    {
        try
        {
             delegate.setStageLabelY(toRType(y), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setStageLabelZ(Float z, int imageIndex)
    {
        try
        {
             delegate.setStageLabelZ(toRType(z), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setStagePositionPositionX(Float positionX, int imageIndex,
            int pixelsIndex, int planeIndex)
    {        
        log.debug(String.format(
            "Setting Image[%d] Pixels[%d] PlaneInfo[%d] position X: '%f'",
            imageIndex, pixelsIndex, planeIndex, positionX));
        try
        {
             delegate.setStagePositionPositionX(
                toRType(positionX), imageIndex, pixelsIndex, planeIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setStagePositionPositionY(Float positionY, int imageIndex,
            int pixelsIndex, int planeIndex)
    {
        log.debug(String.format(
                "Setting Image[%d] Pixels[%d] PlaneInfo[%d] position Y: '%f'",
                imageIndex, pixelsIndex, planeIndex, positionY));
        try
        {
             delegate.setStagePositionPositionY(
                toRType(positionY), imageIndex, pixelsIndex, planeIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setStagePositionPositionZ(Float positionZ, int imageIndex,
            int pixelsIndex, int planeIndex)
    {
        log.debug(String.format(
                "Setting Image[%d] Pixels[%d] PlaneInfo[%d] position Z: '%f'",
                imageIndex, pixelsIndex, planeIndex, positionZ));
        try
        {
             delegate.setStagePositionPositionZ(
                toRType(positionZ), imageIndex, pixelsIndex, planeIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setTiffDataFileName(String fileName, int imageIndex,
            int pixelsIndex, int tiffDataIndex)
    {
        try
        {
             delegate.setTiffDataFileName(
                toRType(fileName), imageIndex, pixelsIndex, tiffDataIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setTiffDataFirstC(Integer firstC, int imageIndex,
            int pixelsIndex, int tiffDataIndex)
    {
        try
        {
             delegate.setTiffDataFirstC(
                toRType(firstC), imageIndex, pixelsIndex, tiffDataIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setTiffDataFirstT(Integer firstT, int imageIndex,
            int pixelsIndex, int tiffDataIndex)
    {
        try
        {
             delegate.setTiffDataFirstT(
                toRType(firstT), imageIndex, pixelsIndex, tiffDataIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setTiffDataFirstZ(Integer firstZ, int imageIndex,
            int pixelsIndex, int tiffDataIndex)
    {
        try
        {
             delegate.setTiffDataFirstZ(
                toRType(firstZ), imageIndex, pixelsIndex, tiffDataIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setTiffDataIFD(Integer ifd, int imageIndex, int pixelsIndex,
            int tiffDataIndex)
    {
        try
        {
             delegate.setTiffDataIFD(
                toRType(ifd), imageIndex, pixelsIndex, tiffDataIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setTiffDataNumPlanes(Integer numPlanes, int imageIndex,
            int pixelsIndex, int tiffDataIndex)
    {
        try
        {
             delegate.setTiffDataNumPlanes(
                toRType(numPlanes), imageIndex, pixelsIndex, tiffDataIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setTiffDataUUID(String uuid, int imageIndex, int pixelsIndex,
            int tiffDataIndex)
    {
        try
        {
             delegate.setTiffDataUUID(
                toRType(uuid), imageIndex, pixelsIndex, tiffDataIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setUUID(String uuid)
    {
        try
        {
             delegate.setUUID(toRType(uuid));
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setWellColumn(Integer column, int plateIndex, int wellIndex)
    {
        try
        {
             delegate.setWellColumn(
                toRType(column), plateIndex, wellIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setWellExternalDescription(String externalDescription,
            int plateIndex, int wellIndex)
    {
        try
        {
             delegate.setWellExternalDescription(
                toRType(externalDescription), plateIndex, wellIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setWellExternalIdentifier(String externalIdentifier,
            int plateIndex, int wellIndex)
    {
        try
        {
             delegate.setWellExternalIdentifier(
                toRType(externalIdentifier), plateIndex, wellIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setWellID(String id, int plateIndex, int wellIndex)
    {
        try
        {
             delegate.setWellID(
                toRType(id), plateIndex, wellIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setWellRow(Integer row, int plateIndex, int wellIndex)
    {
        try
        {
             delegate.setWellRow(
                toRType(row), plateIndex, wellIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setWellSampleID(String id, int plateIndex, int wellIndex,
            int wellSampleIndex)
    {
        try
        {
             delegate.setWellSampleID(
                toRType(id), plateIndex, wellIndex, wellSampleIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setWellSampleIndex(Integer index, int plateIndex,
            int wellIndex, int wellSampleIndex)
    {
        try
        {
             delegate.setWellSampleIndex(
                toRType(index), plateIndex, wellIndex, wellSampleIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setWellSamplePosX(Float posX, int plateIndex, int wellIndex,
            int wellSampleIndex)
    {
        try
        {
             delegate.setWellSamplePosX(
                toRType(posX), plateIndex, wellIndex, wellSampleIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setWellSamplePosY(Float posY, int plateIndex, int wellIndex,
            int wellSampleIndex)
    {
        try
        {            
            delegate.setWellSamplePosY(
                toRType(posY), plateIndex, wellIndex, wellSampleIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setWellSampleTimepoint(Integer timepoint, int plateIndex,
            int wellIndex, int wellSampleIndex)
    {
        try
        {
            delegate.setWellSampleTimepoint(
                toRType(timepoint), plateIndex, wellIndex, wellSampleIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setWellType(String type, int plateIndex, int wellIndex)
    {
        try
        {
            delegate.setWellType(toRType(type), plateIndex, wellIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public long getExperimenterID()
    {
        try
        {
            return iAdmin.getEventContext().userId;
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void writeFilesToFileStore(File[] files, Long pixId)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }
    
    public long getRepositorySpace()
    {
        try
        {
            return iRepoInfo.getFreeSpaceInKilobytes();
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public List<Pixels> saveToDB()
    {
        try
        {
            return delegate.saveToDB();
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void addImageToDataset(Image image, Dataset dataset)
    {   
        try
        {
            Image unloadedImage = new ImageI(image.getId(), false);
            Dataset unloadedDataset = new DatasetI(dataset.getId(), false);
            DatasetImageLink l = new DatasetImageLinkI();
            l.setChild(unloadedImage);
            l.setParent(unloadedDataset);
            iUpdate.saveObject(l);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public Project getProject(long projectId)
    {
        try
        {
            return (Project) iQuery.get("Project", projectId);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public Dataset getDataset(long datasetId)
    {
        try
        {
            return (Dataset) iQuery.get("Dataset", datasetId);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

 // FIXME: change to iQuery
    public void addBooleanAnnotationToPixels(BooleanAnnotation annotation,
            Pixels pixels)
    {
        try
        {
            Pixels unloadedPixels = new PixelsI(pixels.getId(), false);
            PixelsAnnotationLink l = new PixelsAnnotationLinkI();
            l.setChild(annotation);
            l.setParent(unloadedPixels);
            iUpdate.saveObject(l);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public Pixels getPixels(int series)
    {
        try
        {
            return delegate.getPixels(series);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public Dataset addDataset(String datasetName, String datasetDescription,
            Project project)
    {
        Dataset dataset = new DatasetI();
        if (datasetName.length() != 0)
            dataset.setName(toRType(datasetName));
        if (datasetDescription.length() != 0)
            dataset.setDescription(toRType(datasetDescription));
        Project p = new ProjectI(project.getId().getValue(), false);
        dataset.linkProject(p);

        try
        {
            return (Dataset) iUpdate.saveAndReturnObject(dataset);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

 // FIXME: change to iQuery
    public List<Project> getProjects()
    {
        try
        {
            return delegate.getProjects();
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

 // FIXME: change to iQuery
    public List<Dataset> getDatasets(Project p)
    {
        try
        {
            return delegate.getDatasets(p);
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public Project addProject(String projectName, String projectDescription)
    {
        Project project = new ProjectI();
        if (projectName.length() != 0)
            project.setName(toRType(projectName));
        if (projectDescription.length() != 0)
            project.setDescription(toRType(projectDescription));

        try
        {
            return (Project) iUpdate.saveAndReturnObject(project);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public void setOriginalFiles(File[] files, String formatString)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void setPixelsId(long long1)
    {
        return;
    }

 // FIXME: change to iQuery
    public void setPlane(Long pixId, byte[] arrayBuf, int z, int c, int t) throws ServerError
    {
            if (currentPixId != pixId)
            {
                rawPixelStore.setPixelsId(pixId);
                currentPixId = pixId;
            }
            rawPixelStore.setPlane(arrayBuf, z, c, t);
    }

    public void setChannelGlobalMinMax(int channel, double minimum,
            double maximum, int series)
    {
        try
        {
            delegate.setChannelGlobalMinMax(channel, toRType(minimum), toRType(maximum), toRType(series));
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void populateSHA1(MessageDigest md, Long id)
    {
        Pixels p;
        try
        {
            p = (Pixels) iQuery.get("Pixels", id);
            p.setSha1(toRType(byteArrayToHexString(md.digest())));
            iUpdate.saveObject(p);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    static String byteArrayToHexString(byte in[]) {

        byte ch = 0x00;
        int i = 0;

        if (in == null || in.length <= 0) {
            return null;
        }

        String pseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                "a", "b", "c", "d", "e", "f" };

        StringBuffer out = new StringBuffer(in.length * 2);

        while (i < in.length) {

            ch = (byte) (in[i] & 0xF0);
            ch = (byte) (ch >>> 4);
            ch = (byte) (ch & 0x0F);
            out.append(pseudo[ch]);
            ch = (byte) (in[i] & 0x0F);
            out.append(pseudo[ch]);
            i++;

        }

        String rslt = new String(out);
        return rslt;
    }

    public void populateMinMax(Long id, Integer i)
    {
        try
        {
            delegate.populateMinMax(toRType(id), toRType(i));
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setExperimentDescription(String description, int experimentIndex)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void setExperimentID(String id, int experimentIndex)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void setExperimentType(String type, int experimentIndex)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void setExperimenterMembershipGroup(String group,
            int experimenterIndex, int groupRefIndex)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void setImageDefaultPixels(String defaultPixels, int imageIndex)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void setLogicalChannelOTF(String otf, int imageIndex,
            int logicalChannelIndex)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void setOTFObjective(String objective, int instrumentIndex,
            int otfIndex)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    /* ---- Objective Settings ---- */
    
    public void setObjectiveSettingsCorrectionCollar(Float correctionCollar,
            int imageIndex)
    {
        try
        {
            delegate.setObjectiveSettingsCorrectionCollar(toRType(correctionCollar), imageIndex);
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setObjectiveSettingsMedium(String medium, int imageIndex)
    {
        try
        {
            delegate.setObjectiveSettingsMedium(toRType(medium), imageIndex);
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setObjectiveSettingsObjective(String objective, int imageIndex)
    {
        try
        {
            delegate.setObjectiveSettingsObjective(toRType(objective), imageIndex);
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setObjectiveSettingsRefractiveIndex(Float refractiveIndex,
            int imageIndex)
    {
        try
        {
            delegate.setObjectiveSettingsRefractiveIndex(toRType(refractiveIndex), imageIndex);
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }
    
/*
    public void setLogicalChannelPinholeSize(Float a, int b, int c)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }
*/
}
