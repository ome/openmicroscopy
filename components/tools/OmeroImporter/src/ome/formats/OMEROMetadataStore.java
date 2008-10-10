package ome.formats;

import java.io.File;
import java.security.MessageDigest;
import java.util.List;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

import omero.RBool;
import omero.RDouble;
import omero.RFloat;
import omero.RInt;
import omero.RLong;
import omero.RObject;
import omero.RString;
import omero.RType;
import omero.ServerError;
import omero.client;
import omero.api.MetadataStorePrx;
import omero.api.MetadataStorePrxHelper;
import omero.api.ServiceFactoryPrx;
import omero.constants.METADATASTORE;
import omero.model.BooleanAnnotation;
import omero.model.BooleanAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.Pixels;
import omero.model.PixelsI;
import omero.model.Project;
import omero.model.ProjectI;

import loci.formats.meta.IMinMaxStore;
import loci.formats.meta.MetadataStore;


public class OMEROMetadataStore implements MetadataStore, IMinMaxStore
{
    MetadataStorePrx delegate;
    
    ServiceFactoryPrx serviceFactory;

    public OMEROMetadataStore(String username, String password, String server,
            String port) throws CannotCreateSessionException, PermissionDeniedException, ServerError
    {
        client c = new client(server);
        serviceFactory = c.createSession(username, password);
        delegate = MetadataStorePrxHelper.checkedCast(serviceFactory.getByName(METADATASTORE.value));
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
            delegate.setArcType(new RString(type), instrumentIndex, lightSourceIndex);
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
            delegate.setChannelComponentColorDomain(new RString(colorDomain), 
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
            delegate.setChannelComponentIndex(new RInt(index), 
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
            delegate.setDetectorGain(new RFloat(gain), instrumentIndex, detectorIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDetectorID(String id, int instrumentIndex, int detectorIndex)
    {
        try
        {
            delegate.setDetectorID(new RString(id), instrumentIndex, detectorIndex);
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
            delegate.setDetectorManufacturer(new RString(manufacturer), instrumentIndex, detectorIndex);
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
            delegate.setDetectorModel(new RString(model), instrumentIndex, detectorIndex);
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
            delegate.setDetectorOffset(new RFloat(offset), instrumentIndex, detectorIndex);
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
            delegate.setDetectorSerialNumber(new RString(serialNumber), instrumentIndex, detectorIndex);
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
            delegate.setDetectorSettingsDetector(new RString(detector), imageIndex, logicalChannelIndex);
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
            delegate.setDetectorSettingsGain(new RFloat(gain), imageIndex, logicalChannelIndex);
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
            delegate.setDetectorSettingsOffset(new RFloat(offset), imageIndex, logicalChannelIndex);
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
            delegate.setDetectorType(new RString(type), instrumentIndex, detectorIndex);
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
            delegate.setDetectorVoltage(new RFloat(voltage), instrumentIndex, detectorIndex);
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
            delegate.setDimensionsPhysicalSizeX(new RFloat(physicalSizeX), imageIndex, pixelsIndex);
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
            delegate.setDimensionsPhysicalSizeY(new RFloat(physicalSizeY), imageIndex, pixelsIndex);
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
            delegate.setDimensionsPhysicalSizeZ(new RFloat(physicalSizeZ), imageIndex, pixelsIndex);
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
            delegate.setDimensionsTimeIncrement(new RFloat(timeIncrement), imageIndex, pixelsIndex);
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
            delegate.setDimensionsWaveIncrement(new RInt(waveIncrement), imageIndex, pixelsIndex);
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
            delegate.setDimensionsWaveStart(new RInt(waveStart), imageIndex, pixelsIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDisplayOptionsID(String id, int imageIndex)
    {
        try
        {
            delegate.setDisplayOptionsID(new RString(id), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDisplayOptionsProjectionZStart(Integer start, int imageIndex)
    {
        try
        {
            delegate.setDisplayOptionsProjectionZStart(new RInt(start), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDisplayOptionsProjectionZStop(Integer stop, int imageIndex)
    {
        try
        {
            delegate.setDisplayOptionsProjectionZStop(new RInt(stop), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDisplayOptionsTimeTStart(Integer start, int imageIndex)
    {
        try
        {
            delegate.setDisplayOptionsTimeTStart(new RInt(start), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDisplayOptionsTimeTStop(Integer stop, int imageIndex)
    {
        try
        {
            delegate.setDisplayOptionsTimeTStop(new RInt(stop), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setDisplayOptionsZoom(Float zoom, int imageIndex)
    {
        try
        {
            delegate.setDisplayOptionsZoom(new RFloat(zoom), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setExperimenterEmail(String email, int experimenterIndex)
    {
        try
        {
            delegate.setExperimenterEmail(new RString(email), experimenterIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setExperimenterFirstName(String firstName, int experimenterIndex)
    {
        try
        {
            delegate.setExperimenterFirstName(new RString(firstName), experimenterIndex);
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
            delegate.setExperimenterInstitution(new RString(institution), experimenterIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setExperimenterLastName(String lastName, int experimenterIndex)
    {
        try
        {
            delegate.setExperimenterLastName(new RString(lastName), experimenterIndex);
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
            delegate.setFilamentType(new RString(type), instrumentIndex, lightSourceIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setImageCreationDate(String creationDate, int imageIndex)
    {
        try
        {
            delegate.setImageCreationDate(new RString(creationDate), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setImageDescription(String description, int imageIndex)
    {
        try
        {
            delegate.setImageDescription(new RString(description), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setImageID(String id, int imageIndex)
    {
        try
        {
            delegate.setImageID(new RString(id), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setImageInstrumentRef(String instrumentRef, int imageIndex)
    {
        try
        {
            delegate.setImageInstrumentRef2(new RString(instrumentRef), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setImageName(String name, int imageIndex)
    {
        try
        {
            delegate.setImageName(new RString(name), imageIndex);
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
            delegate.setImagingEnvironmentAirPressure(new RFloat(airPressure), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setImagingEnvironmentCO2Percent(Float percent, int imageIndex)
    {
        try
        {
            delegate.setImagingEnvironmentCO2Percent(new RFloat(percent), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setImagingEnvironmentHumidity(Float humidity, int imageIndex)
    {
        try
        {
            delegate.setImagingEnvironmentHumidity(new RFloat(humidity), imageIndex);
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
            delegate.setImagingEnvironmentTemperature(new RFloat(temperature), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setInstrumentID(String id, int instrumentIndex)
    {
        try
        {
            delegate.setInstrumentID(new RString(id), instrumentIndex);
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
            delegate.setLaserFrequencyMultiplication(new RInt(frequencyMultiplication), instrumentIndex, lightSourceIndex);
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
            delegate.setLaserLaserMedium(new RString(laserMedium), instrumentIndex, lightSourceIndex);
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
            delegate.setLaserPulse(new RString(pulse), instrumentIndex, lightSourceIndex);
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
            delegate.setLaserTuneable(new RBool(tuneable), instrumentIndex, lightSourceIndex);
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
            delegate.setLaserType(new RString(type), instrumentIndex, lightSourceIndex);
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
            delegate.setLaserWavelength(new RInt(wavelength), instrumentIndex, lightSourceIndex);
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
            delegate.setLightSourceID(new RString(id), instrumentIndex, lightSourceIndex);
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
            delegate.setLightSourceManufacturer(new RString(manufacturer), instrumentIndex, lightSourceIndex);
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
            delegate.setLightSourceModel(new RString(model), instrumentIndex, lightSourceIndex);
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
            delegate.setLightSourcePower(new RFloat(power), instrumentIndex, lightSourceIndex);
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
            delegate.setLightSourceSerialNumber(new RString(serialNumber), instrumentIndex, lightSourceIndex);
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
            delegate.setLightSourceSettingsAttenuation(new RFloat(attenuation), imageIndex, logicalChannelIndex);
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
            delegate.setLightSourceSettingsLightSource(new RString(lightSource), imageIndex, logicalChannelIndex);
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
            delegate.setLightSourceSettingsWavelength(new RInt(wavelength), imageIndex, logicalChannelIndex);
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
            delegate.setLogicalChannelContrastMethod(new RString(contrastMethod), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLogicalChannelEmWave(Integer emWave, int imageIndex,
            int logicalChannelIndex)
    {
         try
        {
            delegate.setLogicalChannelEmWave(new RInt(emWave), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLogicalChannelExWave(Integer exWave, int imageIndex,
            int logicalChannelIndex)
    {
         try
        {
            delegate.setLogicalChannelExWave(new RInt(exWave), imageIndex, logicalChannelIndex);
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
            delegate.setLogicalChannelFluor(new RString(fluor), imageIndex, logicalChannelIndex);
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
            delegate.setLogicalChannelID(new RString(id), imageIndex, logicalChannelIndex);
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
            delegate.setLogicalChannelID(new RString(illuminationType), imageIndex, logicalChannelIndex);
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
            delegate.setLogicalChannelMode(new RString(mode), imageIndex, logicalChannelIndex);
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
            delegate.setLogicalChannelName(new RString(name), imageIndex, logicalChannelIndex);
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
            delegate.setLogicalChannelNdFilter(new RFloat(ndFilter), imageIndex, logicalChannelIndex);
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
            delegate.setLogicalChannelPhotometricInterpretation(new RString(photometricInterpretation), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setLogicalChannelPinholeSize(Integer pinholeSize,
            int imageIndex, int logicalChannelIndex)
    {
        try
        {
            delegate.setLogicalChannelPinholeSize(new RInt(pinholeSize), imageIndex, logicalChannelIndex);
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
            delegate.setLogicalChannelPockelCellSetting(new RInt(pockelCellSetting), imageIndex, logicalChannelIndex);
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
            delegate.setLogicalChannelSamplesPerPixel(new RInt(samplesPerPixel), imageIndex, logicalChannelIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setOTFID(String id, int instrumentIndex, int otfIndex)
    {
        try
        {
            delegate.setOTFID(new RString(id), instrumentIndex, otfIndex);
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
            delegate.setOTFOpticalAxisAveraged(new RBool(opticalAxisAveraged), instrumentIndex, otfIndex);
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
            delegate.setOTFPixelType(new RString(pixelType), instrumentIndex, otfIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setOTFSizeX(Integer sizeX, int instrumentIndex, int otfIndex)
    {
        try
        {
            delegate.setOTFSizeX(new RInt(sizeX), instrumentIndex, otfIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setOTFSizeY(Integer sizeY, int instrumentIndex, int otfIndex)
    {
        try
        {
            delegate.setOTFSizeY(new RInt(sizeY), instrumentIndex, otfIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setObjectiveCalibratedMagnification(
            Float calibratedMagnification, int instrumentIndex,
            int objectiveIndex)
    {
        try
        {
            delegate.setObjectiveCalibratedMagnification(new RFloat(calibratedMagnification), instrumentIndex, objectiveIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setObjectiveCorrection(String correction, int instrumentIndex,
            int objectiveIndex)
    {
        try
        {
            delegate.setObjectiveCorrection(new RString(correction), instrumentIndex, objectiveIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setObjectiveID(String id, int instrumentIndex,
            int objectiveIndex)
    {
        try
        {
            delegate.setObjectiveID(new RString(id), instrumentIndex, objectiveIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setObjectiveImmersion(String immersion, int instrumentIndex,
            int objectiveIndex)
    {
        try
        {
            delegate.setObjectiveImmersion(new RString(immersion), instrumentIndex, objectiveIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setObjectiveLensNA(Float lensNA, int instrumentIndex,
            int objectiveIndex)
    {
        try
        {
            delegate.setObjectiveLensNA(new RFloat(lensNA), instrumentIndex, objectiveIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setObjectiveManufacturer(String manufacturer,
            int instrumentIndex, int objectiveIndex)
    {
        try
        {
            delegate.setObjectiveManufacturer(new RString(manufacturer), instrumentIndex, objectiveIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setObjectiveModel(String model, int instrumentIndex,
            int objectiveIndex)
    {
        try
        {
            delegate.setObjectiveModel(new RString(model), instrumentIndex, objectiveIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setObjectiveNominalMagnification(Integer nominalMagnification,
            int instrumentIndex, int objectiveIndex)
    {
        try
        {
            delegate.setObjectiveNominalMagnification(new RInt(nominalMagnification), instrumentIndex, objectiveIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setObjectiveSerialNumber(String serialNumber,
            int instrumentIndex, int objectiveIndex)
    {
        try
        {
            delegate.setObjectiveSerialNumber(new RString(serialNumber), instrumentIndex, objectiveIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setObjectiveWorkingDistance(Float workingDistance,
            int instrumentIndex, int objectiveIndex)
    {
        try
        {
            delegate.setObjectiveWorkingDistance(new RFloat(workingDistance), instrumentIndex, objectiveIndex);
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
            delegate.setPixelsBigEndian(new RBool(bigEndian), imageIndex, pixelsIndex);
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
            delegate.setPixelsDimensionOrder(new RString(dimensionOrder), imageIndex, pixelsIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPixelsID(String id, int imageIndex, int pixelsIndex)
    {
        try
        {
            delegate.setPixelsID(new RString(id), imageIndex, pixelsIndex);
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
            delegate.setPixelsPixelType(new RString(pixelType), imageIndex, pixelsIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPixelsSizeC(Integer sizeC, int imageIndex, int pixelsIndex)
    {
        try
        {
            delegate.setPixelsSizeC(new RInt(sizeC), imageIndex, pixelsIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPixelsSizeT(Integer sizeT, int imageIndex, int pixelsIndex)
    {
        try
        {
            delegate.setPixelsSizeT(new RInt(sizeT), imageIndex, pixelsIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPixelsSizeX(Integer sizeX, int imageIndex, int pixelsIndex)
    {
        try
        {
            delegate.setPixelsSizeX(new RInt(sizeX), imageIndex, pixelsIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPixelsSizeY(Integer sizeY, int imageIndex, int pixelsIndex)
    {
        try
        {
            delegate.setPixelsSizeY(new RInt(sizeY), imageIndex, pixelsIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPixelsSizeZ(Integer sizeZ, int imageIndex, int pixelsIndex)
    {
        try
        {
            delegate.setPixelsSizeZ(new RInt(sizeZ), imageIndex, pixelsIndex);
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
            delegate.setPlaneTheC(new RInt(theC), imageIndex, pixelsIndex, planeIndex);
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
            delegate.setPlaneTheT(new RInt(theT), imageIndex, pixelsIndex, planeIndex);
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
            delegate.setPlaneTheZ(new RInt(theZ), imageIndex, pixelsIndex, planeIndex);
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
            delegate.setPlaneTimingDeltaT(new RFloat(deltaT), imageIndex, pixelsIndex, planeIndex);
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
            delegate.setPlaneTimingExposureTime(new RFloat(exposureTime), imageIndex, pixelsIndex, planeIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPlateDescription(String description, int plateIndex)
    {
        try
        {
            delegate.setPlateDescription(new RString(description), plateIndex);
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
            delegate.setPlateExternalIdentifier(new RString(externalIdentifier), plateIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPlateID(String id, int plateIndex)
    {
        try
        {
            delegate.setPlateID(new RString(id), plateIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPlateName(String name, int plateIndex)
    {
        try
        {
            delegate.setPlateName(new RString(name), plateIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPlateRefID(String id, int screenIndex, int plateRefIndex)
    {
        try
        {
            delegate.setPlateRefID(new RString(id), screenIndex, plateRefIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPlateStatus(String status, int plateIndex)
    {
        try
        {
            delegate.setPlateStatus(new RString(status), plateIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setROIID(String id, int imageIndex, int roiIndex)
    {
        try
        {
            delegate.setROIID(new RString(id), imageIndex, roiIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setROIT0(Integer t0, int imageIndex, int roiIndex)
    {
        try
        {
            delegate.setROIT0(new RInt(t0), imageIndex, roiIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setROIT1(Integer t1, int imageIndex, int roiIndex)
    {
        try
        {
            delegate.setROIT1(new RInt(t1), imageIndex, roiIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setROIX0(Integer x0, int imageIndex, int roiIndex)
    {
        try
        {
            delegate.setROIX0(new RInt(x0), imageIndex, roiIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setROIX1(Integer x1, int imageIndex, int roiIndex)
    {
        try
        {
            delegate.setROIX1(new RInt(x1), imageIndex, roiIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setROIY0(Integer y0, int imageIndex, int roiIndex)
    {
        try
        {
            delegate.setROIY0(new RInt(y0), imageIndex, roiIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setROIY1(Integer y1, int imageIndex, int roiIndex)
    {
        try
        {
            delegate.setROIY1(new RInt(y1), imageIndex, roiIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setROIZ0(Integer z0, int imageIndex, int roiIndex)
    {
        try
        {
            delegate.setROIZ0(new RInt(z0), imageIndex, roiIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        };
    }

    public void setROIZ1(Integer z1, int imageIndex, int roiIndex)
    {
        try
        {
            delegate.setROIZ1(new RInt(z1), imageIndex, roiIndex);
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
            delegate.setReagentDescription(new RString(description), screenIndex, reagentIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setReagentID(String id, int screenIndex, int reagentIndex)
    {
        try
        {
            delegate.setReagentID(new RString(id), screenIndex, reagentIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setReagentName(String name, int screenIndex, int reagentIndex)
    {
        try
        {
            delegate.setReagentName(new RString(name), screenIndex, reagentIndex);
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
            delegate.setReagentReagentIdentifier(new RString(reagentIdentifier), screenIndex, reagentIndex);
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
                new RString(endTime), screenIndex, screenAcquisitionIndex);
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
                new RString(id), screenIndex, screenAcquisitionIndex);
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
                new RString(startTime), screenIndex, screenAcquisitionIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setScreenID(String id, int screenIndex)
    {
        try
        {
             delegate.setScreenID(new RString(id), screenIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setScreenName(String name, int screenIndex)
    {
        try
        {
             delegate.setScreenName(new RString(name), screenIndex);
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
                new RString(protocolDescription), screenIndex);
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
                new RString(protocolIdentifier), screenIndex);
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
                new RString(reagentSetDescription), screenIndex);
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
             delegate.setScreenType(new RString(type), screenIndex);
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
             delegate.setStageLabelName(new RString(name), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setStageLabelX(Float x, int imageIndex)
    {
        try
        {
             delegate.setStageLabelX(new RFloat(x), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setStageLabelY(Float y, int imageIndex)
    {
        try
        {
             delegate.setStageLabelY(new RFloat(y), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setStageLabelZ(Float z, int imageIndex)
    {
        try
        {
             delegate.setStageLabelZ(new RFloat(z), imageIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setStagePositionPositionX(Float positionX, int imageIndex,
            int pixelsIndex, int planeIndex)
    {
        try
        {
             delegate.setStagePositionPositionX(
                new RFloat(positionX), imageIndex, pixelsIndex, planeIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setStagePositionPositionY(Float positionY, int imageIndex,
            int pixelsIndex, int planeIndex)
    {
        try
        {
             delegate.setStagePositionPositionY(
                new RFloat(positionY), imageIndex, pixelsIndex, planeIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setStagePositionPositionZ(Float positionZ, int imageIndex,
            int pixelsIndex, int planeIndex)
    {
        try
        {
             delegate.setStagePositionPositionZ(
                new RFloat(positionZ), imageIndex, pixelsIndex, planeIndex);
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
                new RString(fileName), imageIndex, pixelsIndex, tiffDataIndex);
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
                new RInt(firstC), imageIndex, pixelsIndex, tiffDataIndex);
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
                new RInt(firstT), imageIndex, pixelsIndex, tiffDataIndex);
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
                new RInt(firstZ), imageIndex, pixelsIndex, tiffDataIndex);
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
                new RInt(ifd), imageIndex, pixelsIndex, tiffDataIndex);
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
                new RInt(numPlanes), imageIndex, pixelsIndex, tiffDataIndex);
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
                new RString(uuid), imageIndex, pixelsIndex, tiffDataIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setUUID(String uuid)
    {
        try
        {
             delegate.setUUID(new RString(uuid));
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
                new RInt(column), plateIndex, wellIndex);
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
                new RString(externalDescription), plateIndex, wellIndex);
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
                new RString(externalIdentifier), plateIndex, wellIndex);
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
                new RString(id), plateIndex, wellIndex);
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
                new RInt(row), plateIndex, wellIndex);
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
                new RString(id), plateIndex, wellIndex, wellSampleIndex);
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
                new RInt(index), plateIndex, wellIndex, wellSampleIndex);
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
                new RFloat(posX), plateIndex, wellIndex, wellSampleIndex);
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
                new RFloat(posY), plateIndex, wellIndex, wellSampleIndex);
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
                new RInt(timepoint), plateIndex, wellIndex, wellSampleIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setWellType(String type, int plateIndex, int wellIndex)
    {
        try
        {
            delegate.setWellType(new RString(type), plateIndex, wellIndex);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public long getExperimenterID()
    {
        try
        {
            return delegate.getExperimenterID();
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
            return delegate.getRepositorySpace();
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
            delegate.addImageToDataset(image, dataset);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public Project getProject(long projectId)
    {
        try
        {
            return delegate.getProject(projectId);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public Dataset getDataset(long datasetID)
    {
        try
        {
            return delegate.getDataset(datasetID);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void addBooleanAnnotationToPixels(BooleanAnnotation annotation,
            Pixels pixels)
    {
        try
        {
            delegate.addBooleanAnnotationToPixels(annotation, pixels);
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
        try
        {
            return delegate.addDataset(new RString(datasetName), new RString(datasetDescription), project);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

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
            return null;
            
        /*
        try {
            //return delegate.addProject(new RString(projectName), new RString(projectDescription));
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
        */
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

    public void setPlane(Long pixId, byte[] arrayBuf, int z, int c, int t)
    {
        try
        {
            delegate.setPlane(new RLong(pixId), arrayBuf, z, c, t);
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setChannelGlobalMinMax(int channel, double minimum,
            double maximum, int series)
    {
        try
        {
            delegate.setChannelGlobalMinMax(channel, new RDouble(minimum), new RDouble(maximum), new RInt(series));
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void populateSHA1(MessageDigest md, Long id)
    {
        return;
        /*
        try
        {
            // FIXME: missing populateSha1
            delegate.populateSHA1(md, new RLong(id));
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
        */
    }

    public void populateMinMax(Long id, Integer i)
    {
        try
        {
            delegate.populateMinMax(new RLong(id), new RInt(i));
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }
}
