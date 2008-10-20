/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_METADATASTORE_ICE
#define OMERO_METADATASTORE_ICE

#include <Ice/BuiltinSequences.ice>
#include <omero/API.ice>
#include <omero/ServerErrors.ice>

module omero {

    module constants {

	const string METADATASTORE = "omero.api.MetadataStore";

    };

    module api {

	["ami","amd"] interface MetadataStore extends StatefulServiceInterface
	    {

		void createRoot() throws ServerError;
		void setRoot(RType root) throws ServerError;
		ImageList getRoot() throws ServerError;
		void setChannelGlobalMinMax(int channelIdx, RDouble globalMin, RDouble globalMax, RInt pixelsIndex) throws ServerError;
		void setThePixelsId(RLong id) throws ServerError;
		DatasetList getDatasets(omero::model::Project project) throws ServerError;
		ProjectList getProjects() throws ServerError;
		PixelsList saveToDB() throws ServerError;
		void populateMinMax(RLong id, RInt i) throws ServerError;
		omero::model::Image getImage(int imageIndex) throws ServerError;
		omero::model::Pixels getPixels(int series) throws ServerError;
		void setImageName(RString name, int imageIndex) throws ServerError;
		void setImageCreationDate(RString creationDate, int imageIndex) throws ServerError;
		void setImageDescription(RString description, int imageIndex) throws ServerError;
		void setImageInstrumentRef(RString instrumentRef, int imageIndex) throws ServerError;
		void setPixelsSizeX(RInt sizeX, int imageIndex, int pixelsIndex) throws ServerError;
		void setPixelsSizeY(RInt sizeY, int imageIndex, int pixelsIndex) throws ServerError;
		void setPixelsSizeZ(RInt sizeZ, int imageIndex, int pixelsIndex) throws ServerError;
		void setPixelsSizeC(RInt sizeC, int imageIndex, int pixelsIndex) throws ServerError;
		void setPixelsSizeT(RInt sizeT, int imageIndex, int pixelsIndex) throws ServerError;
		void setPixelsPixelType(RString pixelType, int imageIndex, int pixelsIndex) throws ServerError;
		void setPixelsDimensionOrder(RString dimensionOrder, int imageIndex, int pixelsIndex) throws ServerError;
		void setDimensionsPhysicalSizeX(RFloat physicalSizeX, int imageIndex, int pixelsIndex) throws ServerError;
		void setDimensionsPhysicalSizeY(RFloat physicalSizeY, int imageIndex, int pixelsIndex) throws ServerError;
		void setDimensionsPhysicalSizeZ(RFloat physicalSizeZ, int imageIndex, int pixelsIndex) throws ServerError;
		void setImagingEnvironmentTemperature(RFloat temperature, int imageIndex) throws ServerError;
		void setImagingEnvironmentAirPressure(RFloat airPressure, int imageIndex) throws ServerError;
		void setImagingEnvironmentHumidity(RFloat humidity, int imageIndex) throws ServerError;
		void setImagingEnvironmentCO2Percent(RFloat percent, int imageIndex) throws ServerError;
		omero::model::PlaneInfo getPlaneInfo(int imageIndex, int pixelsIndex, int planeIndex) throws ServerError;
		void setPlaneTheZ(RInt theZ, int imageIndex, int pixelsIndex, int planeIndex) throws ServerError;
		void setPlaneTheC(RInt theC, int imageIndex, int pixelsIndex, int planeIndex) throws ServerError;
		void setPlaneTheT(RInt theT, int imageIndex, int pixelsIndex, int planeIndex) throws ServerError;
		void setPlaneTimingDeltaT(RFloat deltaT, int imageIndex, int pixelsIndex, int planeIndex) throws ServerError;
		void setPlaneTimingExposureTime(RFloat exposureTime, int imageIndex, int pixelsIndex, int planeIndex)  throws ServerError;
		void setLogicalChannelName(RString name, int imageIndex, int logicalChannelIndex) throws ServerError;
		void setLogicalChannelIlluminationType(RString illuminationType, int imageIndex, int logicalChannelIndex) throws ServerError;
		void setLogicalChannelPinholeSize(RInt pinholeSize, int imageIndex, int logicalChannelIndex) throws ServerError;
		void setLogicalChannelPhotometricInterpretation(RString photometricInterpretation, int imageIndex, int logicalChannelIndex) throws ServerError;
		void setLogicalChannelMode(RString mode, int imageIndex, int logicalChannelIndex) throws ServerError;
		void setLogicalChannelContrastMethod(RString contrastMethod, int imageIndex, int logicalChannelIndex) throws ServerError;
		void setLogicalChannelExWave(RInt exWave, int imageIndex, int logicalChannelIndex) throws ServerError;
		void setLogicalChannelEmWave(RInt emWave, int imageIndex, int logicalChannelIndex) throws ServerError;
		void setLogicalChannelFluor(RString fluor, int imageIndex, int logicalChannelIndex) throws ServerError;
		void setLogicalChannelNdFilter(RFloat ndFilter, int imageIndex, int logicalChannelIndex) throws ServerError;
		void setLogicalChannelPockelCellSetting(RInt pockelCellSetting, int imageIndex, int logicalChannelIndex) throws ServerError;
		void setStagePositionPositionX(RFloat positionX, int imageIndex, int pixelsIndex, int planeIndex) throws ServerError;
		void setStagePositionPositionY(RFloat positionY, int imageIndex, int pixelsIndex, int planeIndex) throws ServerError;
		void setStagePositionPositionZ(RFloat positionZ, int imageIndex, int pixelsIndex, int planeIndex) throws ServerError;
		void setStageLabelName(RString name, int imageIndex) throws ServerError;
		void setStageLabelX(RFloat x, int imageIndex) throws ServerError;
		void setStageLabelY(RFloat y, int imageIndex) throws ServerError;
		void setStageLabelZ(RFloat z, int imageIndex) throws ServerError;
		void setDetectorSettingsDetector(RString detector, int imageIndex, int logicalChannelIndex) throws ServerError;
		void setDetectorSettingsGain(RFloat gain, int imageIndex, int logicalChannelIndex)  throws ServerError;
		void setDetectorSettingsOffset(RFloat offset, int imageIndex, int logicalChannelIndex)  throws ServerError;
		void setLightSourceID(RString id, int instrumentIndex, int lightSourceIndex) throws ServerError;
		void setLightSourceSettingsLightSource(RString lightSource, int imageIndex, int logicalChannelIndex) throws ServerError;
		void setLightSourceSettingsAttenuation(RFloat attenuation, int imageIndex, int logicalChannelIndex) throws ServerError;
		void setLightSourceSettingsWavelength(RInt wavelength, int imageIndex, int logicalChannelIndex) throws ServerError;
		void setLightSourceManufacturer(RString manufacturer, int instrumentIndex, int lightSourceIndex)  throws ServerError;
		void setLightSourceModel(RString model, int instrumentIndex, int lightSourceIndex)  throws ServerError;
		void setLightSourceSerialNumber(RString serialNumber, int instrumentIndex, int lightSourceIndex)  throws ServerError;
		void setLightSourcePower(RFloat power, int instrumentIndex, int lightSourceIndex) throws ServerError;
		void setLaserFrequencyMultiplication(RInt frequencyMultiplication, int instrumentIndex, int lightSourceIndex)  throws ServerError;
		void setLaserLaserMedium(RString laserMedium, int instrumentIndex, int lightSourceIndex)  throws ServerError;
		void setLaserPower(RFloat power, int instrumentIndex, int lightSourceIndex)  throws ServerError;
		void setLaserPulse(RString pulse, int instrumentIndex, int lightSourceIndex)  throws ServerError;
		void setLaserTuneable(RBool tuneable, int instrumentIndex, int lightSourceIndex)  throws ServerError;
		void setLaserType(RString type, int instrumentIndex, int lightSourceIndex)  throws ServerError;
		void setLaserWavelength(RInt wavelength, int instrumentIndex, int lightSourceIndex)  throws ServerError;
		void setArcPower(RFloat power, int instrumentIndex, int lightSourceIndex)  throws ServerError;
		void setArcType(RString type, int instrumentIndex, int lightSourceIndex)  throws ServerError;
		void setFilamentPower(RFloat power, int instrumentIndex, int lightSourceIndex) throws ServerError;
		void setFilamentType(RString type, int instrumentIndex, int lightSourceIndex) throws ServerError;
		void setDetectorID(RString id, int instrumentIndex, int detectorIndex) throws ServerError;
		void setDetectorGain(RFloat gain, int instrumentIndex, int detectorIndex)  throws ServerError;
		void setDetectorManufacturer(RString manufacturer, int instrumentIndex, int detectorIndex) throws ServerError;
		void setDetectorModel(RString model, int instrumentIndex, int detectorIndex) throws ServerError;
		void setDetectorOffset(RFloat offset, int instrumentIndex, int detectorIndex) throws ServerError;
		void setDetectorSerialNumber(RString serialNumber, int instrumentIndex, int detectorIndex) throws ServerError;
		void setDetectorType(RString type, int instrumentIndex, int detectorIndex) throws ServerError;
		void setDetectorVoltage(RFloat voltage, int instrumentIndex, int detectorIndex) throws ServerError;
		void setObjectiveID(RString id, int instrumentIndex, int objectiveIndex)  throws ServerError;
		void setObjectiveCalibratedMagnification(RFloat calibratedMagnification, int instrumentIndex, int objectiveIndex)  throws ServerError;
		void setObjectiveImmersion(RString immersion, int instrumentIndex, int objectiveIndex)  throws ServerError;
		void setObjectiveLensNA(RFloat lensNA, int instrumentIndex, int objectiveIndex) throws ServerError;
		void setObjectiveManufacturer(RString manufacturer, int instrumentIndex, int objectiveIndex) throws ServerError;
		void setObjectiveModel(RString model, int instrumentIndex, int objectiveIndex) throws ServerError;
		void setObjectiveNominalMagnification(RInt nominalMagnification, int instrumentIndex, int objectiveIndex)  throws ServerError;
		void setObjectiveSerialNumber(RString serialNumber, int instrumentIndex, int objectiveIndex)  throws ServerError;
		void setObjectiveWorkingDistance(RFloat workingDistance, int instrumentIndex, int objectiveIndex) throws ServerError;
		void setOTFID(RString id, int instrumentIndex, int otfIndex) throws ServerError;
		void setOTFOpticalAxisAveraged(RBool opticalAxisAveraged, int instrumentIndex, int otfIndex) throws ServerError;
		void setOTFPath(RString path, int instrumentIndex, int otfIndex) throws ServerError;
		void setOTFPixelType(RString pixelType, int instrumentIndex, int otfIndex) throws ServerError;
		void setOTFSizeX(RInt sizeX, int instrumentIndex, int otfIndex) throws ServerError;
		void setOTFSizeY(RInt sizeY, int instrumentIndex, int otfIndex) throws ServerError;
		void setScreenID(RString id, int screenIndex) throws ServerError;
		void setScreenName(RString name, int screenIndex) throws ServerError;
		void setScreenProtocolDescription(RString protocolDescription, int screenIndex)  throws ServerError;
		void setScreenProtocolIdentifier(RString protocolIdentifier, int screenIndex) throws ServerError;
		void setScreenReagentSetDescription(RString reagentSetDescription, int screenIndex) throws ServerError;
		void setScreenAcquisitionID(RString id, int screenIndex, int screenAcquisitionIndex) throws ServerError;
		void setScreenAcquisitionEndTime(RString endTime, int screenIndex, int screenAcquisitionIndex) throws ServerError;
		void setScreenAcquisitionStartTime(RString startTime, int screenIndex, int screenAcquisitionIndex) throws ServerError;
		void setPlateID(RString id, int plateIndex) throws ServerError;
		void setPlateDescription(RString description, int plateIndex) throws ServerError;
		void setPlateExternalIdentifier(RString externalIdentifier, int plateIndex) throws ServerError;
		void setPlateName(RString name, int plateIndex) throws ServerError;
		void setPlateStatus(RString status, int plateIndex) throws ServerError;
		void setWellID(RString id, int plateIndex, int wellIndex) throws ServerError;
		void setWellColumn(RInt column, int plateIndex, int wellIndex) throws ServerError;
		void setWellExternalDescription(RString externalDescription, int plateIndex, int wellIndex) throws ServerError;
		void setWellExternalIdentifier(RString externalIdentifier, int plateIndex, int wellIndex) throws ServerError;
		void setWellRow(RInt row, int plateIndex, int wellIndex) throws ServerError;
		void setWellType(RString type, int plateIndex, int wellIndex) throws ServerError;
		void setWellSampleID(RString id, int plateIndex, int wellIndex, int wellSampleIndex) throws ServerError;
		void setWellSampleIndex(RInt index, int plateIndex, int wellIndex, int wellSampleIndex) throws ServerError;
		void setWellSamplePosX(RFloat posX, int plateIndex, int wellIndex, int wellSampleIndex) throws ServerError;
		void setWellSamplePosY(RFloat posY, int plateIndex, int wellIndex, int wellSampleIndex) throws ServerError;
		void setWellSampleTimepoint(RInt timepoint, int plateIndex, int wellIndex, int wellSampleIndex) throws ServerError;
		void setExperimenterDataDirectory(RString dataDirectory, int experimenterIndex) throws ServerError;
		void setExperimenterEmail(RString email, int experimenterIndex) throws ServerError;
		void setExperimenterFirstName(RString firstName, int experimenterIndex) throws ServerError;
		void setExperimenterInstitution(RString institution, int experimenterIndex) throws ServerError;
		void setExperimenterLastName(RString lastName, int experimenterIndex) throws ServerError;
		void setDetectorNodeID(RString nodeID, int instrumentIndex, int detectorIndex) throws ServerError;
		void setDisplayOptionsNodeID(RString nodeID, int imageIndex) throws ServerError;
		void setExperimenterNodeID(RString nodeID, int experimenterIndex) throws ServerError;
		void setImageNodeID(RString nodeID, int imageIndex) throws ServerError;
		void setInstrumentNodeID(RString nodeID, int instrumentIndex) throws ServerError;
		void setLightSourceNodeID(RString nodeID, int instrumentIndex, int lightSourceIndex) throws ServerError;
		void setLogicalChannelNodeID(RString nodeID, int imageIndex, int logicalChannelIndex) throws ServerError;
		void setOTFNodeID(RString nodeID, int instrumentIndex, int otfIndex) throws ServerError;
		void setObjectiveNodeID(RString nodeID, int instrumentIndex, int objectiveIndex) throws ServerError;
		void setPixelsNodeID(RString nodeID, int imageIndex, int pixelsIndex) throws ServerError;
		void setROINodeID(RString nodeID, int imageIndex, int roiIndex) throws ServerError;
		void setDisplayOptionsID(RString id, int imageIndex) throws ServerError;
		void setExperimenterID(RString id, int experimenterIndex) throws ServerError;
		void setImageID(RString id, int imageIndex) throws ServerError;
		void setInstrumentID(RString id, int instrumentIndex) throws ServerError;
		void setLogicalChannelID(RString id, int imageIndex, int logicalChannelIndex) throws ServerError;
		void setPixelsID(RString id, int imageIndex, int pixelsIndex) throws ServerError;
		void setROIID(RString id, int imageIndex, int roiIndex) throws ServerError;
		void setROIT0(RInt t0, int imageIndex, int roiIndex) throws ServerError;
		void setROIT1(RInt t1, int imageIndex, int roiIndex) throws ServerError;
		void setROIX0(RInt x0, int imageIndex, int roiIndex) throws ServerError;
		void setROIX1(RInt x1, int imageIndex, int roiIndex) throws ServerError;
		void setROIY0(RInt y0, int imageIndex, int roiIndex) throws ServerError;
		void setROIY1(RInt y1, int imageIndex, int roiIndex) throws ServerError;
		void setROIZ0(RInt z0, int imageIndex, int roiIndex) throws ServerError;
		void setROIZ1(RInt z1, int imageIndex, int roiIndex) throws ServerError;
		void setDisplayOptionsProjectionZStart(RInt start, int imageIndex) throws ServerError;
		void setDisplayOptionsProjectionZStop(RInt stop, int imageIndex) throws ServerError;
		void setDisplayOptionsTimeTStart(RInt start, int imageIndex) throws ServerError;
		void setDisplayOptionsTimeTStop(RInt stop, int imageIndex) throws ServerError;
		void setDisplayOptionsZoom(RFloat zoom, int imageIndex) throws ServerError;
		void setChannelComponentIndex(RInt index, int imageIndex, int logicalChannelIndex, int channelComponentIndex) throws ServerError;
		void setPixelsBigEndian(RBool bigEndian, int imageIndex, int pixelsIndex) throws ServerError;
		void setLogicalChannelSamplesPerPixel(RInt samplesPerPixel, int imageIndex, int logicalChannelIndex) throws ServerError;
		void setTiffDataFileName(RString fileName, int imageIndex, int pixelsIndex, int tiffDataIndex) throws ServerError;
		void setTiffDataFirstC(RInt firstC, int imageIndex, int pixelsIndex, int tiffDataIndex) throws ServerError;
		void setTiffDataFirstT(RInt firstT, int imageIndex, int pixelsIndex, int tiffDataIndex) throws ServerError;
		void setTiffDataFirstZ(RInt firstZ, int imageIndex, int pixelsIndex, int tiffDataIndex) throws ServerError;
		void setTiffDataIFD(RInt ifd, int imageIndex, int pixelsIndex, int tiffDataIndex) throws ServerError;
		void setTiffDataNumPlanes(RInt numPlanes, int imageIndex, int pixelsIndex, int tiffDataIndex) throws ServerError;
		void setTiffDataUUID(RString uuid, int imageIndex, int pixelsIndex, int tiffDataIndex) throws ServerError;
		void setPlateRefID(RString id, int screenIndex, int plateRefIndex) throws ServerError;
		void setDimensionsTimeIncrement(RFloat timeIncrement, int imageIndex, int pixelsIndex) throws ServerError;
		void setDimensionsWaveIncrement(RInt waveIncrement, int imageIndex, int pixelsIndex) throws ServerError;
		void setDimensionsWaveStart(RInt waveStart, int imageIndex, int pixelsIndex) throws ServerError;
		void setChannelComponentColorDomain(RString colorDomain, int imageIndex, int logicalChannelIndex, int channelComponentIndex) throws ServerError;
		void setObjectiveCorrection(RString correction, int instrumentIndex, int objectiveIndex)  throws ServerError;
		void setReagentDescription(RString description, int screenIndex, int reagentIndex) throws ServerError;
		void setReagentID(RString id, int screenIndex, int reagentIndex) throws ServerError;
		void setReagentName(RString name, int screenIndex, int reagentIndex) throws ServerError;
		void setReagentReagentIdentifier(RString reagentIdentifier, int screenIndex, int reagentIndex) throws ServerError;
		void setUUID(RString uuid) throws ServerError;

	    };
    };

};
#endif
