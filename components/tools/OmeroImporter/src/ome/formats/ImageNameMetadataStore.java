/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats;

import java.util.HashMap;
import java.util.Map;

import loci.formats.meta.MetadataStore;

/**
 * Stores all Image names consumed by the interface.
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public class ImageNameMetadataStore implements MetadataStore
{
    /**
     * The Map of Image index vs. Image name. This is map because of the
     * potential out of order population via metadata store usage.
     */ 
    private Map<Integer, String> imageNames = new HashMap<Integer, String>();

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageName(java.lang.String, int)
     */
    public void setImageName(String imageName, int imageIndex)
    {
        imageNames.put(imageIndex, imageName);
    }

    /**
     * Retrieves the current map of Image names held.
     * @return Map of Image index vs. Image name.
     */
    public Map<Integer, String> getImageNames()
    {
        return imageNames;
    }

    //
    // MetadataStore stubs follow
    //

    public void createRoot()
    {
        // TODO Auto-generated method stub
        
    }

    public Object getRoot()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setArcType(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setChannelComponentColorDomain(String arg0, int arg1, int arg2,
            int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setChannelComponentIndex(Integer arg0, int arg1, int arg2,
            int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setChannelComponentPixels(String arg0, int arg1, int arg2,
            int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setCircleCx(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setCircleCy(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setCircleID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setCircleR(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setCircleTransform(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setContactExperimenter(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDatasetDescription(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDatasetExperimenterRef(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDatasetGroupRef(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDatasetID(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDatasetLocked(Boolean arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDatasetName(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDatasetRefID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDetectorAmplificationGain(Double arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDetectorGain(Double arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDetectorID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDetectorManufacturer(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDetectorModel(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDetectorOffset(Double arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDetectorSerialNumber(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDetectorSettingsBinning(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDetectorSettingsDetector(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDetectorSettingsGain(Double arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDetectorSettingsOffset(Double arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDetectorSettingsReadOutRate(Double arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDetectorSettingsVoltage(Double arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDetectorType(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDetectorVoltage(Double arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDetectorZoom(Double arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDichroicID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDichroicLotNumber(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDichroicManufacturer(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDichroicModel(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDimensionsPhysicalSizeX(Double arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDimensionsPhysicalSizeY(Double arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDimensionsPhysicalSizeZ(Double arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDimensionsTimeIncrement(Double arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDimensionsWaveIncrement(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDimensionsWaveStart(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDisplayOptionsDisplay(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDisplayOptionsID(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDisplayOptionsZoom(Double arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setEllipseCx(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setEllipseCy(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setEllipseID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setEllipseRx(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setEllipseRy(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setEllipseTransform(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setEmFilterLotNumber(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setEmFilterManufacturer(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setEmFilterModel(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setEmFilterType(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setExFilterLotNumber(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setExFilterManufacturer(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setExFilterModel(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setExFilterType(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setExperimentDescription(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setExperimentExperimenterRef(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setExperimentID(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setExperimentType(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setExperimenterEmail(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setExperimenterFirstName(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setExperimenterID(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setExperimenterInstitution(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setExperimenterLastName(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setExperimenterMembershipGroup(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setExperimenterOMEName(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setFilamentType(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setFilterFilterWheel(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setFilterID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setFilterLotNumber(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setFilterManufacturer(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setFilterModel(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setFilterSetDichroic(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setFilterSetEmFilter(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setFilterSetExFilter(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setFilterSetID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setFilterSetLotNumber(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setFilterSetManufacturer(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setFilterSetModel(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setFilterType(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setGroupID(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setGroupName(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setImageAcquiredPixels(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setImageCreationDate(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setImageDefaultPixels(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setImageDescription(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setImageExperimentRef(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setImageExperimenterRef(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setImageGroupRef(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setImageID(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setImageInstrumentRef(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setImagingEnvironmentAirPressure(Double arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setImagingEnvironmentCO2Percent(Double arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setImagingEnvironmentHumidity(Double arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setImagingEnvironmentTemperature(Double arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setInstrumentID(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLaserFrequencyMultiplication(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLaserLaserMedium(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLaserPockelCell(Boolean arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLaserPulse(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLaserRepetitionRate(Double arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLaserTuneable(Boolean arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLaserType(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLaserWavelength(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLightSourceID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLightSourceManufacturer(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLightSourceModel(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLightSourcePower(Double arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLightSourceRefAttenuation(Double arg0, int arg1, int arg2,
            int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLightSourceRefLightSource(String arg0, int arg1, int arg2,
            int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLightSourceRefWavelength(Integer arg0, int arg1, int arg2,
            int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLightSourceSerialNumber(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLightSourceSettingsAttenuation(Double arg0, int arg1,
            int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLightSourceSettingsLightSource(String arg0, int arg1,
            int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLightSourceSettingsWavelength(Integer arg0, int arg1,
            int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLineID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLineTransform(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLineX1(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLineX2(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLineY1(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLineY2(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLogicalChannelContrastMethod(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLogicalChannelDetector(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLogicalChannelEmWave(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLogicalChannelExWave(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLogicalChannelFilterSet(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLogicalChannelFluor(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLogicalChannelID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLogicalChannelIlluminationType(String arg0, int arg1,
            int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLogicalChannelLightSource(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLogicalChannelMode(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLogicalChannelName(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLogicalChannelNdFilter(Double arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLogicalChannelOTF(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLogicalChannelPhotometricInterpretation(String arg0,
            int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLogicalChannelPinholeSize(Double arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLogicalChannelPockelCellSetting(Integer arg0, int arg1,
            int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLogicalChannelSamplesPerPixel(Integer arg0, int arg1,
            int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLogicalChannelSecondaryEmissionFilter(String arg0, int arg1,
            int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setLogicalChannelSecondaryExcitationFilter(String arg0,
            int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setMaskHeight(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setMaskID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setMaskPixelsBigEndian(Boolean arg0, int arg1, int arg2,
            int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setMaskPixelsBinData(byte[] arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setMaskPixelsExtendedPixelType(String arg0, int arg1, int arg2,
            int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setMaskPixelsID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setMaskPixelsSizeX(Integer arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setMaskPixelsSizeY(Integer arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setMaskTransform(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setMaskWidth(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setMaskX(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setMaskY(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setMicrobeamManipulationExperimenterRef(String arg0, int arg1,
            int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setMicrobeamManipulationID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setMicrobeamManipulationRefID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setMicrobeamManipulationType(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setMicroscopeID(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setMicroscopeManufacturer(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setMicroscopeModel(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setMicroscopeSerialNumber(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setMicroscopeType(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setOTFBinaryFile(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setOTFID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setOTFObjective(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setOTFOpticalAxisAveraged(Boolean arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setOTFPixelType(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setOTFSizeX(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setOTFSizeY(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setObjectiveCalibratedMagnification(Double arg0, int arg1,
            int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setObjectiveCorrection(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setObjectiveID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setObjectiveImmersion(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setObjectiveIris(Boolean arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setObjectiveLensNA(Double arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setObjectiveManufacturer(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setObjectiveModel(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setObjectiveNominalMagnification(Integer arg0, int arg1,
            int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setObjectiveSerialNumber(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setObjectiveSettingsCorrectionCollar(Double arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setObjectiveSettingsMedium(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setObjectiveSettingsObjective(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setObjectiveSettingsRefractiveIndex(Double arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setObjectiveWorkingDistance(Double arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPathD(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPathID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPixelsBigEndian(Boolean arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPixelsDimensionOrder(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPixelsID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPixelsPixelType(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPixelsSizeC(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPixelsSizeT(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPixelsSizeX(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPixelsSizeY(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPixelsSizeZ(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPlaneHashSHA1(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPlaneID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPlaneTheC(Integer arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPlaneTheT(Integer arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPlaneTheZ(Integer arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPlaneTimingDeltaT(Double arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPlaneTimingExposureTime(Double arg0, int arg1, int arg2,
            int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPlateColumnNamingConvention(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPlateDescription(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPlateExternalIdentifier(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPlateID(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPlateName(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPlateRefID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPlateRefSample(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPlateRefWell(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPlateRowNamingConvention(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPlateStatus(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPlateWellOriginX(Double arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPlateWellOriginY(Double arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPointCx(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPointCy(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPointID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPointR(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPointTransform(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPolygonID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPolygonPoints(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPolygonTransform(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPolylineID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPolylinePoints(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPolylineTransform(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setProjectDescription(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setProjectExperimenterRef(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setProjectGroupRef(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setProjectID(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setProjectName(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setProjectRefID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setPumpLightSource(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setROIID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setROIRefID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setROIT0(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setROIT1(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setROIX0(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setROIX1(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setROIY0(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setROIY1(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setROIZ0(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setROIZ1(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setReagentDescription(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setReagentID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setReagentName(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setReagentReagentIdentifier(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setRectHeight(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setRectID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setRectTransform(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setRectWidth(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setRectX(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setRectY(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setRegionID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setRegionName(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setRegionTag(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setRoiLinkDirection(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setRoiLinkName(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setRoiLinkRef(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setRoot(Object arg0)
    {
        // TODO Auto-generated method stub
        
    }

    public void setScreenAcquisitionEndTime(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setScreenAcquisitionID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setScreenAcquisitionStartTime(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setScreenDescription(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setScreenExtern(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setScreenID(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setScreenName(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setScreenProtocolDescription(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setScreenProtocolIdentifier(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setScreenReagentSetDescription(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setScreenReagentSetIdentifier(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setScreenRefID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setScreenType(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeBaselineShift(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeDirection(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeFillColor(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeFillOpacity(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeFillRule(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeFontFamily(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeFontSize(Integer arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeFontStretch(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeFontStyle(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeFontVariant(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeFontWeight(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeG(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeGlyphOrientationVertical(Integer arg0, int arg1,
            int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeLocked(Boolean arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeStrokeAttribute(String arg0, int arg1, int arg2,
            int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeStrokeColor(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeStrokeDashArray(String arg0, int arg1, int arg2,
            int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeStrokeLineCap(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeStrokeLineJoin(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeStrokeMiterLimit(Integer arg0, int arg1, int arg2,
            int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeStrokeOpacity(Double arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeStrokeWidth(Integer arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeText(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeTextAnchor(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeTextDecoration(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeTextFill(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeTextStroke(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeTheT(Integer arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeTheZ(Integer arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeVectorEffect(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeVisibility(Boolean arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setShapeWritingMode(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setStageLabelName(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setStageLabelX(Double arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setStageLabelY(Double arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setStageLabelZ(Double arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setStagePositionPositionX(Double arg0, int arg1, int arg2,
            int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setStagePositionPositionY(Double arg0, int arg1, int arg2,
            int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setStagePositionPositionZ(Double arg0, int arg1, int arg2,
            int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setThumbnailHref(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setThumbnailID(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setThumbnailMIMEtype(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setTiffDataFileName(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setTiffDataFirstC(Integer arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setTiffDataFirstT(Integer arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setTiffDataFirstZ(Integer arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setTiffDataIFD(Integer arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setTiffDataNumPlanes(Integer arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setTiffDataUUID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setTransmittanceRangeCutIn(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setTransmittanceRangeCutInTolerance(Integer arg0, int arg1,
            int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setTransmittanceRangeCutOut(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setTransmittanceRangeCutOutTolerance(Integer arg0, int arg1,
            int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setTransmittanceRangeTransmittance(Integer arg0, int arg1,
            int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setUUID(String arg0)
    {
        // TODO Auto-generated method stub
        
    }

    public void setWellColumn(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setWellExternalDescription(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setWellExternalIdentifier(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setWellID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setWellReagent(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setWellRow(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

    public void setWellSampleID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setWellSampleImageRef(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setWellSampleIndex(Integer arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setWellSamplePosX(Double arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setWellSamplePosY(Double arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setWellSampleRefID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setWellSampleTimepoint(Integer arg0, int arg1, int arg2,
            int arg3)
    {
        // TODO Auto-generated method stub
        
    }

    public void setWellType(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        
    }

}
