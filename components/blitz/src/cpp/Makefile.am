AUTOMAKE_OPTIONS = foreign

SLICE_DIR=@SLICE_DIR@
SLICE_OME=../../resources/slice
SLICE_GEN=../../target/generated/resources
SLICE_INCLUDES=-I$(SLICE_DIR) -I$(SLICE_OME)  -I$(SLICE_GEN)

CPP_SRC=.
CPP_GEN=generated

AM_CXXFLAGS=-O0 -Wall
INCLUDES=-I$(CPP_GEN) -I$(CPP_SRC)
LIBS=-lstdc++ -lIce -lIceUtil -lSlice -lGlacier2

default:
	echo "Nothing"

slice: 
	mkdir -p $(CPP_GEN) && \
	@SLICE2CPP@ $(SLICE_INCLUDES) --output-dir=$(CPP_GEN) $(SLICE_GEN)/*.ice && \
	@SLICE2CPP@ $(SLICE_INCLUDES) --output-dir=$(CPP_GEN) $(SLICE_OME)/*.ice	

lib_LTLIBRARIES     	= libOMERO_model.la libOMERO_client.la
libOMERO_model_la_SOURCES = ./DetailsI.cpp ./PermissionsI.cpp \
	./generated/AberrationCorrection.cpp \
	./generated/AberrationCorrectionI.cpp \
	./generated/AcquisitionMode.cpp \
	./generated/AcquisitionModeI.cpp \
	./generated/API.cpp \
	./generated/Arc.cpp \
	./generated/ArcI.cpp \
	./generated/ArcType.cpp \
	./generated/ArcTypeI.cpp \
	./generated/Binning.cpp \
	./generated/BinningI.cpp \
	./generated/BoundingBox.cpp \
	./generated/BoundingBoxI.cpp \
	./generated/Category.cpp \
	./generated/CategoryGroupCategoryLink.cpp \
	./generated/CategoryGroupCategoryLinkI.cpp \
	./generated/CategoryGroup.cpp \
	./generated/CategoryGroupI.cpp \
	./generated/CategoryI.cpp \
	./generated/CategoryImageLink.cpp \
	./generated/CategoryImageLinkI.cpp \
	./generated/CellArea.cpp \
	./generated/CellAreaI.cpp \
	./generated/CellEccentricity.cpp \
	./generated/CellEccentricityI.cpp \
	./generated/CellExtent.cpp \
	./generated/CellExtentI.cpp \
	./generated/CellMajorAxisLength.cpp \
	./generated/CellMajorAxisLengthI.cpp \
	./generated/CellMinorAxisLength.cpp \
	./generated/CellMinorAxisLengthI.cpp \
	./generated/CellPerimeter.cpp \
	./generated/CellPerimeterI.cpp \
	./generated/CellPosition.cpp \
	./generated/CellPositionI.cpp \
	./generated/CellSolidity.cpp \
	./generated/CellSolidityI.cpp \
	./generated/ChannelBinding.cpp \
	./generated/ChannelBindingI.cpp \
	./generated/Channel.cpp \
	./generated/ChannelI.cpp \
	./generated/Coating.cpp \
	./generated/CoatingI.cpp \
	./generated/CodomainMapContext.cpp \
	./generated/CodomainMapContextI.cpp \
	./generated/Color.cpp \
	./generated/ColorI.cpp \
	./generated/Constants.cpp \
	./generated/ContrastMethod.cpp \
	./generated/ContrastMethodI.cpp \
	./generated/ContrastStretchingContext.cpp \
	./generated/ContrastStretchingContextI.cpp \
	./generated/CustomizedFilterSet.cpp \
	./generated/CustomizedFilterSetI.cpp \
	./generated/DatasetAnnotation.cpp \
	./generated/DatasetAnnotationI.cpp \
	./generated/Dataset.cpp \
	./generated/DatasetI.cpp \
	./generated/DatasetImageLink.cpp \
	./generated/DatasetImageLinkI.cpp \
	./generated/Details.cpp \
	./generated/Detector.cpp \
	./generated/DetectorI.cpp \
	./generated/DetectorSettings.cpp \
	./generated/DetectorSettingsI.cpp \
	./generated/DetectorType.cpp \
	./generated/DetectorTypeI.cpp \
	./generated/Dichroic.cpp \
	./generated/DichroicI.cpp \
	./generated/DimensionOrder.cpp \
	./generated/DimensionOrderI.cpp \
	./generated/DummyStatistics.cpp \
	./generated/DummyStatisticsI.cpp \
	./generated/EmissionFilter.cpp \
	./generated/EmissionFilterI.cpp \
	./generated/Error.cpp \
	./generated/Event.cpp \
	./generated/EventI.cpp \
	./generated/EventLog.cpp \
	./generated/EventLogI.cpp \
	./generated/EventType.cpp \
	./generated/EventTypeI.cpp \
	./generated/ExcitationFilter.cpp \
	./generated/ExcitationFilterI.cpp \
	./generated/Experiment.cpp \
	./generated/Experimenter.cpp \
	./generated/ExperimenterGroup.cpp \
	./generated/ExperimenterGroupI.cpp \
	./generated/ExperimenterI.cpp \
	./generated/ExperimentI.cpp \
	./generated/ExperimentType.cpp \
	./generated/ExperimentTypeI.cpp \
	./generated/ExternalInfo.cpp \
	./generated/ExternalInfoI.cpp \
	./generated/Family.cpp \
	./generated/FamilyI.cpp \
	./generated/Filament.cpp \
	./generated/FilamentI.cpp \
	./generated/FilamentType.cpp \
	./generated/FilamentTypeI.cpp \
	./generated/Filter.cpp \
	./generated/FilterI.cpp \
	./generated/FilterSet.cpp \
	./generated/FilterSetI.cpp \
	./generated/FilterType.cpp \
	./generated/FilterTypeI.cpp \
	./generated/Format.cpp \
	./generated/FormatI.cpp \
	./generated/FrequencyMultiplication.cpp \
	./generated/FrequencyMultiplicationI.cpp \
	./generated/GroupExperimenterMap.cpp \
	./generated/GroupExperimenterMapI.cpp \
	./generated/Illumination.cpp \
	./generated/IlluminationI.cpp \
	./generated/ImageAnnotation.cpp \
	./generated/ImageAnnotationI.cpp \
	./generated/ImageCellCount.cpp \
	./generated/ImageCellCountI.cpp \
	./generated/Image.cpp \
	./generated/ImageI.cpp \
	./generated/ImageNucleasCount.cpp \
	./generated/ImageNucleasCountI.cpp \
	./generated/ImagingEnvironment.cpp \
	./generated/ImagingEnvironmentI.cpp \
	./generated/Immersion.cpp \
	./generated/ImmersionI.cpp \
	./generated/Instrument.cpp \
	./generated/InstrumentI.cpp \
	./generated/IObject.cpp \
	./generated/IrisDiaphragm.cpp \
	./generated/IrisDiaphragmI.cpp \
	./generated/Laser.cpp \
	./generated/LaserI.cpp \
	./generated/LaserMedium.cpp \
	./generated/LaserMediumI.cpp \
	./generated/LaserType.cpp \
	./generated/LaserTypeI.cpp \
	./generated/LightSettings.cpp \
	./generated/LightSettingsI.cpp \
	./generated/LightSource.cpp \
	./generated/LightSourceI.cpp \
	./generated/Link.cpp \
	./generated/LinkI.cpp \
	./generated/LogicalChannel.cpp \
	./generated/LogicalChannelI.cpp \
	./generated/Medium.cpp \
	./generated/MediumI.cpp \
	./generated/Metadata.cpp \
	./generated/MetadataI.cpp \
	./generated/Microscope.cpp \
	./generated/MicroscopeI.cpp \
	./generated/MicroscopeType.cpp \
	./generated/MicroscopeTypeI.cpp \
	./generated/NucleusArea.cpp \
	./generated/NucleusAreaI.cpp \
	./generated/NucleusEccentricity.cpp \
	./generated/NucleusEccentricityI.cpp \
	./generated/NucleusExtent.cpp \
	./generated/NucleusExtentI.cpp \
	./generated/NucleusMajorAxisLength.cpp \
	./generated/NucleusMajorAxisLengthI.cpp \
	./generated/NucleusMinorAxisLength.cpp \
	./generated/NucleusMinorAxisLengthI.cpp \
	./generated/NucleusPerimeter.cpp \
	./generated/NucleusPerimeterI.cpp \
	./generated/NucleusPosition.cpp \
	./generated/NucleusPositionI.cpp \
	./generated/NucleusSolidity.cpp \
	./generated/NucleusSolidityI.cpp \
	./generated/ObjectFactoryRegistrar.cpp \
	./generated/Objective.cpp \
	./generated/ObjectiveI.cpp \
	./generated/ObjectiveSettings.cpp \
	./generated/ObjectiveSettingsI.cpp \
	./generated/omero.cpp \
	./generated/OriginalFile.cpp \
	./generated/OriginalFileI.cpp \
	./generated/OTF.cpp \
	./generated/OTFI.cpp \
	./generated/Overlay.cpp \
	./generated/OverlayI.cpp \
	./generated/OverlayType.cpp \
	./generated/OverlayTypeI.cpp \
	./generated/Permissions.cpp \
	./generated/PhotometricInterpretation.cpp \
	./generated/PhotometricInterpretationI.cpp \
	./generated/Pixels.cpp \
	./generated/PixelsDimensions.cpp \
	./generated/PixelsDimensionsI.cpp \
	./generated/PixelsI.cpp \
	./generated/PixelsOriginalFileMap.cpp \
	./generated/PixelsOriginalFileMapI.cpp \
	./generated/PixelsType.cpp \
	./generated/PixelsTypeI.cpp \
	./generated/PlaneInfo.cpp \
	./generated/PlaneInfoI.cpp \
	./generated/PlaneSlicingContext.cpp \
	./generated/PlaneSlicingContextI.cpp \
	./generated/ProjectAnnotation.cpp \
	./generated/ProjectAnnotationI.cpp \
	./generated/Project.cpp \
	./generated/ProjectDatasetLink.cpp \
	./generated/ProjectDatasetLinkI.cpp \
	./generated/ProjectI.cpp \
	./generated/Pulse.cpp \
	./generated/PulseI.cpp \
	./generated/QuantumDef.cpp \
	./generated/QuantumDefI.cpp \
	./generated/Region.cpp \
	./generated/RegionI.cpp \
	./generated/RegionType.cpp \
	./generated/RegionTypeI.cpp \
	./generated/RenderingDef.cpp \
	./generated/RenderingDefI.cpp \
	./generated/RenderingModel.cpp \
	./generated/RenderingModelI.cpp \
	./generated/ReverseIntensityContext.cpp \
	./generated/ReverseIntensityContextI.cpp \
	./generated/Roi5D.cpp \
	./generated/Roi5DI.cpp \
	./generated/RoiExtent.cpp \
	./generated/RoiExtentI.cpp \
	./generated/RoiMap.cpp \
	./generated/RoiMapI.cpp \
	./generated/RoiSet.cpp \
	./generated/RoiSetI.cpp \
	./generated/ROMIO.cpp \
	./generated/RTypes.cpp \
	./generated/ShapeArea.cpp \
	./generated/ShapeAreaI.cpp \
	./generated/Specification.cpp \
	./generated/SpecificationI.cpp \
	./generated/Square.cpp \
	./generated/SquareI.cpp \
	./generated/StageLabel.cpp \
	./generated/StageLabelI.cpp \
	./generated/StatsInfo.cpp \
	./generated/StatsInfoI.cpp \
	./generated/System.cpp \
	./generated/Thumbnail.cpp \
	./generated/ThumbnailI.cpp \
	./generated/TransmittanceRange.cpp \
	./generated/TransmittanceRangeI.cpp \
	./generated/URoi.cpp \
	./generated/URoiI.cpp \
	./generated/UShape.cpp \
	./generated/UShapeI.cpp \
	./generated/USlice.cpp \
	./generated/USliceI.cpp \
	./generated/USquare.cpp \
	./generated/USquareI.cpp \
	./generated/XYC.cpp \
	./generated/XYCI.cpp \
	./generated/XY.cpp \
	./generated/XYCT.cpp \
	./generated/XYCTI.cpp \
	./generated/XYCtoXYLink.cpp \
	./generated/XYCtoXYLinkI.cpp \
	./generated/XYCTtoXYCLink.cpp \
	./generated/XYCTtoXYCLinkI.cpp \
	./generated/XYCTtoXYTLink.cpp \
	./generated/XYCTtoXYTLinkI.cpp \
	./generated/XYI.cpp \
	./generated/XYT.cpp \
	./generated/XYTI.cpp \
	./generated/XYTtoXYLink.cpp \
	./generated/XYTtoXYLinkI.cpp \
	./generated/XYZC.cpp \
	./generated/XYZCI.cpp \
	./generated/XYZ.cpp \
	./generated/XYZCT.cpp \
	./generated/XYZCTI.cpp \
	./generated/XYZCtoXYCLink.cpp \
	./generated/XYZCtoXYCLinkI.cpp \
	./generated/XYZCtoXYZLink.cpp \
	./generated/XYZCtoXYZLinkI.cpp \
	./generated/XYZCTtoXYCTLink.cpp \
	./generated/XYZCTtoXYCTLinkI.cpp \
	./generated/XYZCTtoXYZCLink.cpp \
	./generated/XYZCTtoXYZCLinkI.cpp \
	./generated/XYZCTtoXYZTLink.cpp \
	./generated/XYZCTtoXYZTLinkI.cpp \
	./generated/XYZI.cpp \
	./generated/XYZT.cpp \
	./generated/XYZTI.cpp \
	./generated/XYZtoXYLink.cpp \
	./generated/XYZtoXYLinkI.cpp \
	./generated/XYZTtoXYTLink.cpp \
	./generated/XYZTtoXYTLinkI.cpp \
	./generated/XYZTtoXYZLink.cpp \
	./generated/XYZTtoXYZLinkI.cpp

#libOMERO_model_a_LDFLAGS = -no-undefined -version-info 0:0:0

libOMERO_client_la_SOURCES= ./OMERO/client.cpp ./generated/ObjectFactoryRegistrar.cpp
libOMERO_client_la_LIBADD = libOMERO_model.la
#libOMERO_client_a_LDFLAGS= -no-undefined -version-info 0:0:0

bin_PROGRAMS            = OMERO.demo
OMERO_demo_SOURCES     	= main.cpp
OMERO_demo_LDADD   	= libOMERO_client.la


