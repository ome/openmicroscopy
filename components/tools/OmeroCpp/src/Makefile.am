#
#   $Id$
#
#   Copyright 2007 Glencoe Software, Inc. All rights reserved.
#   Use is subject to license terms supplied in LICENSE.txt
#
#

COPY ?= ../target/temp
SLICE_DIR=@SLICE_DIR@

SLICE_INCLUDES=-I$(SLICE_DIR) -I$(COPY)

CPP_SRC=.
CPP_GEN=slice_generated
CPP_MODEL=$(CPP_GEN)/omero/Model.h

INCLUDES=-I$(CPP_GEN) -I$(CPP_SRC) -I$(COPY)
LIBS=-lstdc++ -lIce -lIceUtil -lSlice -lGlacier2

clean-local:
	find $(CPP_GEN) -name "*.cpp" -exec rm {} \;
	find $(CPP_GEN) -name "*.h" -exec rm {} \;

slice: $(CPP_GEN)

model: slice

$(CPP_GEN): $(COPY)/omero/model/*.h
	mkdir -p $(CPP_GEN)/omero/model && \
	@SLICE2CPP@ $(SLICE_INCLUDES) --include-dir=omero       --output-dir=$(CPP_GEN)/omero/      $(COPY)/omero/*.ice && \
	@SLICE2CPP@ $(SLICE_INCLUDES) --include-dir=omero/model --output-dir=$(CPP_GEN)/omero/model $(COPY)/omero/model/*.ice && \
	(test -e $(CPP_MODEL) && rm $(CPP_MODEL)); \
	find $(CPP_GEN)/omero/model/ -name "*.h" -exec grep class {} /dev/null \; | grep IceProxy | grep    omero::model::IObject | cut -d: -f1 | sort | uniq >> $(CPP_MODEL) && \
	find $(CPP_GEN)/omero/model/ -name "*.h" -exec grep class {} /dev/null \; | grep IceProxy | grep -v omero::model::IObject | cut -d: -f1 | sort | uniq >> $(CPP_MODEL) && \
	perl -i -pe 's/^slice_generated\//#include </' $(CPP_MODEL) &&\
	perl -i -pe 's/.h$$/.h>/' $(CPP_MODEL)

lib_LTLIBRARIES            = libOMERO_common.la libOMERO_client.la 
libOMERO_common_la_SOURCES = ./omero/ClientErrors.cpp 

#libOMERO_common_a_LDFLAGS = -no-undefined -version-info 0:0:0

#libOMERO_client_a_LDFLAGS = -no-undefined -version-info 0:0:0
libOMERO_client_la_LIBADD  = libOMERO_common.la 
libOMERO_client_la_SOURCES = ./omero/client.cpp ./omero/CTypes.cpp \
	../target/temp/omero/Model/AberrationCorrectionI.cpp\
	../target/temp/omero/Model/AcquisitionModeI.cpp\
	../target/temp/omero/Model/ArcI.cpp\
	../target/temp/omero/Model/ArcTypeI.cpp\
	../target/temp/omero/Model/BinningI.cpp\
	../target/temp/omero/Model/BoundingBoxI.cpp\
	../target/temp/omero/Model/CategoryGroupCategoryLinkI.cpp\
	../target/temp/omero/Model/CategoryGroupI.cpp\
	../target/temp/omero/Model/CategoryI.cpp\
	../target/temp/omero/Model/CategoryImageLinkI.cpp\
	../target/temp/omero/Model/CellAreaI.cpp\
	../target/temp/omero/Model/CellEccentricityI.cpp\
	../target/temp/omero/Model/CellExtentI.cpp\
	../target/temp/omero/Model/CellMajorAxisLengthI.cpp\
	../target/temp/omero/Model/CellMinorAxisLengthI.cpp\
	../target/temp/omero/Model/CellPerimeterI.cpp\
	../target/temp/omero/Model/CellPositionI.cpp\
	../target/temp/omero/Model/CellSolidityI.cpp\
	../target/temp/omero/Model/ChannelBindingI.cpp\
	../target/temp/omero/Model/ChannelI.cpp\
	../target/temp/omero/Model/CoatingI.cpp\
	../target/temp/omero/Model/CodomainMapContextI.cpp\
	../target/temp/omero/Model/ColorI.cpp\
	../target/temp/omero/Model/ContrastMethodI.cpp\
	../target/temp/omero/Model/ContrastStretchingContextI.cpp\
	../target/temp/omero/Model/CustomizedFilterSetI.cpp\
	../target/temp/omero/Model/DBPatchI.cpp\
	../target/temp/omero/Model/DatasetAnnotationI.cpp\
	../target/temp/omero/Model/DatasetI.cpp\
	../target/temp/omero/Model/DatasetImageLinkI.cpp\
	../target/temp/omero/Model/DetectorI.cpp\
	../target/temp/omero/Model/DetectorSettingsI.cpp\
	../target/temp/omero/Model/DetectorTypeI.cpp\
	../target/temp/omero/Model/DichroicI.cpp\
	../target/temp/omero/Model/DimensionOrderI.cpp\
	../target/temp/omero/Model/DummyStatisticsI.cpp\
	../target/temp/omero/Model/EmissionFilterI.cpp\
	../target/temp/omero/Model/EventI.cpp\
	../target/temp/omero/Model/EventLogI.cpp\
	../target/temp/omero/Model/EventTypeI.cpp\
	../target/temp/omero/Model/ExcitationFilterI.cpp\
	../target/temp/omero/Model/ExperimentI.cpp\
	../target/temp/omero/Model/ExperimentTypeI.cpp\
	../target/temp/omero/Model/ExperimenterGroupI.cpp\
	../target/temp/omero/Model/ExperimenterI.cpp\
	../target/temp/omero/Model/ExternalInfoI.cpp\
	../target/temp/omero/Model/FamilyI.cpp\
	../target/temp/omero/Model/FilamentI.cpp\
	../target/temp/omero/Model/FilamentTypeI.cpp\
	../target/temp/omero/Model/FilterI.cpp\
	../target/temp/omero/Model/FilterSetI.cpp\
	../target/temp/omero/Model/FilterTypeI.cpp\
	../target/temp/omero/Model/FormatI.cpp\
	../target/temp/omero/Model/FrequencyMultiplicationI.cpp\
	../target/temp/omero/Model/GroupExperimenterMapI.cpp\
	../target/temp/omero/Model/IlluminationI.cpp\
	../target/temp/omero/Model/ImageAnnotationI.cpp\
	../target/temp/omero/Model/ImageCellCountI.cpp\
	../target/temp/omero/Model/ImageI.cpp\
	../target/temp/omero/Model/ImageNucleasCountI.cpp\
	../target/temp/omero/Model/ImagingEnvironmentI.cpp\
	../target/temp/omero/Model/ImmersionI.cpp\
	../target/temp/omero/Model/ImportJobI.cpp\
	../target/temp/omero/Model/InstrumentI.cpp\
	../target/temp/omero/Model/IrisDiaphragmI.cpp\
	../target/temp/omero/Model/JobI.cpp\
	../target/temp/omero/Model/JobOriginalFileLinkI.cpp\
	../target/temp/omero/Model/JobStatusI.cpp\
	../target/temp/omero/Model/LaserI.cpp\
	../target/temp/omero/Model/LaserMediumI.cpp\
	../target/temp/omero/Model/LaserTypeI.cpp\
	../target/temp/omero/Model/LightSettingsI.cpp\
	../target/temp/omero/Model/LightSourceI.cpp\
	../target/temp/omero/Model/LinkI.cpp\
	../target/temp/omero/Model/LogicalChannelI.cpp\
	../target/temp/omero/Model/MediumI.cpp\
	../target/temp/omero/Model/MetadataI.cpp\
	../target/temp/omero/Model/MicroscopeI.cpp\
	../target/temp/omero/Model/MicroscopeTypeI.cpp\
	../target/temp/omero/Model/NucleusAreaI.cpp\
	../target/temp/omero/Model/NucleusEccentricityI.cpp\
	../target/temp/omero/Model/NucleusExtentI.cpp\
	../target/temp/omero/Model/NucleusMajorAxisLengthI.cpp\
	../target/temp/omero/Model/NucleusMinorAxisLengthI.cpp\
	../target/temp/omero/Model/NucleusPerimeterI.cpp\
	../target/temp/omero/Model/NucleusPositionI.cpp\
	../target/temp/omero/Model/NucleusSolidityI.cpp\
	../target/temp/omero/Model/OTFI.cpp\
	../target/temp/omero/Model/ObjectiveI.cpp\
	../target/temp/omero/Model/ObjectiveSettingsI.cpp\
	../target/temp/omero/Model/OriginalFileI.cpp\
	../target/temp/omero/Model/OverlayI.cpp\
	../target/temp/omero/Model/OverlayTypeI.cpp\
	../target/temp/omero/Model/PhotometricInterpretationI.cpp\
	../target/temp/omero/Model/PixelsDimensionsI.cpp\
	../target/temp/omero/Model/PixelsI.cpp\
	../target/temp/omero/Model/PixelsOriginalFileMapI.cpp\
	../target/temp/omero/Model/PixelsTypeI.cpp\
	../target/temp/omero/Model/PlaneInfoI.cpp\
	../target/temp/omero/Model/PlaneSlicingContextI.cpp\
	../target/temp/omero/Model/ProjectAnnotationI.cpp\
	../target/temp/omero/Model/ProjectDatasetLinkI.cpp\
	../target/temp/omero/Model/ProjectI.cpp\
	../target/temp/omero/Model/PulseI.cpp\
	../target/temp/omero/Model/QuantumDefI.cpp\
	../target/temp/omero/Model/RegionI.cpp\
	../target/temp/omero/Model/RegionTypeI.cpp\
	../target/temp/omero/Model/RenderingDefI.cpp\
	../target/temp/omero/Model/RenderingModelI.cpp\
	../target/temp/omero/Model/ReverseIntensityContextI.cpp\
	../target/temp/omero/Model/Roi5DI.cpp\
	../target/temp/omero/Model/RoiExtentI.cpp\
	../target/temp/omero/Model/RoiMapI.cpp\
	../target/temp/omero/Model/RoiSetI.cpp\
	../target/temp/omero/Model/ShapeAreaI.cpp\
	../target/temp/omero/Model/SpecificationI.cpp\
	../target/temp/omero/Model/SquareI.cpp\
	../target/temp/omero/Model/StageLabelI.cpp\
	../target/temp/omero/Model/StatsInfoI.cpp\
	../target/temp/omero/Model/ThumbnailI.cpp\
	../target/temp/omero/Model/TransmittanceRangeI.cpp\
	../target/temp/omero/Model/URoiI.cpp\
	../target/temp/omero/Model/UShapeI.cpp\
	../target/temp/omero/Model/USliceI.cpp\
	../target/temp/omero/Model/USquareI.cpp\
	../target/temp/omero/Model/XYCI.cpp\
	../target/temp/omero/Model/XYCTI.cpp\
	../target/temp/omero/Model/XYCTtoXYCLinkI.cpp\
	../target/temp/omero/Model/XYCTtoXYTLinkI.cpp\
	../target/temp/omero/Model/XYCtoXYLinkI.cpp\
	../target/temp/omero/Model/XYI.cpp\
	../target/temp/omero/Model/XYTI.cpp\
	../target/temp/omero/Model/XYTtoXYLinkI.cpp\
	../target/temp/omero/Model/XYZCI.cpp\
	../target/temp/omero/Model/XYZCTI.cpp\
	../target/temp/omero/Model/XYZCTtoXYCTLinkI.cpp\
	../target/temp/omero/Model/XYZCTtoXYZCLinkI.cpp\
	../target/temp/omero/Model/XYZCTtoXYZTLinkI.cpp\
	../target/temp/omero/Model/XYZCtoXYCLinkI.cpp\
	../target/temp/omero/Model/XYZCtoXYZLinkI.cpp\
	../target/temp/omero/Model/XYZI.cpp\
	../target/temp/omero/Model/XYZTI.cpp\
	../target/temp/omero/Model/XYZTtoXYTLinkI.cpp\
	../target/temp/omero/Model/XYZTtoXYZLinkI.cpp\
	../target/temp/omero/Model/XYZtoXYLinkI.cpp\
	../target/temp/omero/ObjectFactoryRegistrar.cpp\
	./slice_generated/omero/API.cpp\
	./slice_generated/omero/Constants.cpp\
	./slice_generated/omero/IObject.cpp\
	./slice_generated/omero/Model/AberrationCorrection.cpp\
	./slice_generated/omero/Model/AcquisitionMode.cpp\
	./slice_generated/omero/Model/Arc.cpp\
	./slice_generated/omero/Model/ArcType.cpp\
	./slice_generated/omero/Model/Binning.cpp\
	./slice_generated/omero/Model/BoundingBox.cpp\
	./slice_generated/omero/Model/Category.cpp\
	./slice_generated/omero/Model/CategoryGroup.cpp\
	./slice_generated/omero/Model/CategoryGroupCategoryLink.cpp\
	./slice_generated/omero/Model/CategoryImageLink.cpp\
	./slice_generated/omero/Model/CellArea.cpp\
	./slice_generated/omero/Model/CellEccentricity.cpp\
	./slice_generated/omero/Model/CellExtent.cpp\
	./slice_generated/omero/Model/CellMajorAxisLength.cpp\
	./slice_generated/omero/Model/CellMinorAxisLength.cpp\
	./slice_generated/omero/Model/CellPerimeter.cpp\
	./slice_generated/omero/Model/CellPosition.cpp\
	./slice_generated/omero/Model/CellSolidity.cpp\
	./slice_generated/omero/Model/Channel.cpp\
	./slice_generated/omero/Model/ChannelBinding.cpp\
	./slice_generated/omero/Model/Coating.cpp\
	./slice_generated/omero/Model/CodomainMapContext.cpp\
	./slice_generated/omero/Model/Color.cpp\
	./slice_generated/omero/Model/ContrastMethod.cpp\
	./slice_generated/omero/Model/ContrastStretchingContext.cpp\
	./slice_generated/omero/Model/CustomizedFilterSet.cpp\
	./slice_generated/omero/Model/DBPatch.cpp\
	./slice_generated/omero/Model/Dataset.cpp\
	./slice_generated/omero/Model/DatasetAnnotation.cpp\
	./slice_generated/omero/Model/DatasetImageLink.cpp\
	./slice_generated/omero/Model/Details.cpp\
	./slice_generated/omero/Model/Detector.cpp\
	./slice_generated/omero/Model/DetectorSettings.cpp\
	./slice_generated/omero/Model/DetectorType.cpp\
	./slice_generated/omero/Model/Dichroic.cpp\
	./slice_generated/omero/Model/DimensionOrder.cpp\
	./slice_generated/omero/Model/DummyStatistics.cpp\
	./slice_generated/omero/Model/EmissionFilter.cpp\
	./slice_generated/omero/Model/Event.cpp\
	./slice_generated/omero/Model/EventLog.cpp\
	./slice_generated/omero/Model/EventType.cpp\
	./slice_generated/omero/Model/ExcitationFilter.cpp\
	./slice_generated/omero/Model/Experiment.cpp\
	./slice_generated/omero/Model/ExperimentType.cpp\
	./slice_generated/omero/Model/Experimenter.cpp\
	./slice_generated/omero/Model/ExperimenterGroup.cpp\
	./slice_generated/omero/Model/ExternalInfo.cpp\
	./slice_generated/omero/Model/Family.cpp\
	./slice_generated/omero/Model/Filament.cpp\
	./slice_generated/omero/Model/FilamentType.cpp\
	./slice_generated/omero/Model/Filter.cpp\
	./slice_generated/omero/Model/FilterSet.cpp\
	./slice_generated/omero/Model/FilterType.cpp\
	./slice_generated/omero/Model/Format.cpp\
	./slice_generated/omero/Model/FrequencyMultiplication.cpp\
	./slice_generated/omero/Model/GroupExperimenterMap.cpp\
	./slice_generated/omero/Model/Illumination.cpp\
	./slice_generated/omero/Model/Image.cpp\
	./slice_generated/omero/Model/ImageAnnotation.cpp\
	./slice_generated/omero/Model/ImageCellCount.cpp\
	./slice_generated/omero/Model/ImageNucleasCount.cpp\
	./slice_generated/omero/Model/ImagingEnvironment.cpp\
	./slice_generated/omero/Model/Immersion.cpp\
	./slice_generated/omero/Model/ImportJob.cpp\
	./slice_generated/omero/Model/Instrument.cpp\
	./slice_generated/omero/Model/IrisDiaphragm.cpp\
	./slice_generated/omero/Model/Job.cpp\
	./slice_generated/omero/Model/JobOriginalFileLink.cpp\
	./slice_generated/omero/Model/JobStatus.cpp\
	./slice_generated/omero/Model/Laser.cpp\
	./slice_generated/omero/Model/LaserMedium.cpp\
	./slice_generated/omero/Model/LaserType.cpp\
	./slice_generated/omero/Model/LightSettings.cpp\
	./slice_generated/omero/Model/LightSource.cpp\
	./slice_generated/omero/Model/Link.cpp\
	./slice_generated/omero/Model/LogicalChannel.cpp\
	./slice_generated/omero/Model/Medium.cpp\
	./slice_generated/omero/Model/Metadata.cpp\
	./slice_generated/omero/Model/Microscope.cpp\
	./slice_generated/omero/Model/MicroscopeType.cpp\
	./slice_generated/omero/Model/NucleusArea.cpp\
	./slice_generated/omero/Model/NucleusEccentricity.cpp\
	./slice_generated/omero/Model/NucleusExtent.cpp\
	./slice_generated/omero/Model/NucleusMajorAxisLength.cpp\
	./slice_generated/omero/Model/NucleusMinorAxisLength.cpp\
	./slice_generated/omero/Model/NucleusPerimeter.cpp\
	./slice_generated/omero/Model/NucleusPosition.cpp\
	./slice_generated/omero/Model/NucleusSolidity.cpp\
	./slice_generated/omero/Model/OTF.cpp\
	./slice_generated/omero/Model/Objective.cpp\
	./slice_generated/omero/Model/ObjectiveSettings.cpp\
	./slice_generated/omero/Model/OriginalFile.cpp\
	./slice_generated/omero/Model/Overlay.cpp\
	./slice_generated/omero/Model/OverlayType.cpp\
	./slice_generated/omero/Model/Permissions.cpp\
	./slice_generated/omero/Model/PhotometricInterpretation.cpp\
	./slice_generated/omero/Model/Pixels.cpp\
	./slice_generated/omero/Model/PixelsDimensions.cpp\
	./slice_generated/omero/Model/PixelsOriginalFileMap.cpp\
	./slice_generated/omero/Model/PixelsType.cpp\
	./slice_generated/omero/Model/PlaneInfo.cpp\
	./slice_generated/omero/Model/PlaneSlicingContext.cpp\
	./slice_generated/omero/Model/Project.cpp\
	./slice_generated/omero/Model/ProjectAnnotation.cpp\
	./slice_generated/omero/Model/ProjectDatasetLink.cpp\
	./slice_generated/omero/Model/Pulse.cpp\
	./slice_generated/omero/Model/QuantumDef.cpp\
	./slice_generated/omero/Model/Region.cpp\
	./slice_generated/omero/Model/RegionType.cpp\
	./slice_generated/omero/Model/RenderingDef.cpp\
	./slice_generated/omero/Model/RenderingModel.cpp\
	./slice_generated/omero/Model/ReverseIntensityContext.cpp\
	./slice_generated/omero/Model/Roi5D.cpp\
	./slice_generated/omero/Model/RoiExtent.cpp\
	./slice_generated/omero/Model/RoiMap.cpp\
	./slice_generated/omero/Model/RoiSet.cpp\
	./slice_generated/omero/Model/ShapeArea.cpp\
	./slice_generated/omero/Model/Specification.cpp\
	./slice_generated/omero/Model/Square.cpp\
	./slice_generated/omero/Model/StageLabel.cpp\
	./slice_generated/omero/Model/StatsInfo.cpp\
	./slice_generated/omero/Model/Thumbnail.cpp\
	./slice_generated/omero/Model/TransmittanceRange.cpp\
	./slice_generated/omero/Model/URoi.cpp\
	./slice_generated/omero/Model/UShape.cpp\
	./slice_generated/omero/Model/USlice.cpp\
	./slice_generated/omero/Model/USquare.cpp\
	./slice_generated/omero/Model/XY.cpp\
	./slice_generated/omero/Model/XYC.cpp\
	./slice_generated/omero/Model/XYCT.cpp\
	./slice_generated/omero/Model/XYCTtoXYCLink.cpp\
	./slice_generated/omero/Model/XYCTtoXYTLink.cpp\
	./slice_generated/omero/Model/XYCtoXYLink.cpp\
	./slice_generated/omero/Model/XYT.cpp\
	./slice_generated/omero/Model/XYTtoXYLink.cpp\
	./slice_generated/omero/Model/XYZ.cpp\
	./slice_generated/omero/Model/XYZC.cpp\
	./slice_generated/omero/Model/XYZCT.cpp\
	./slice_generated/omero/Model/XYZCTtoXYCTLink.cpp\
	./slice_generated/omero/Model/XYZCTtoXYZCLink.cpp\
	./slice_generated/omero/Model/XYZCTtoXYZTLink.cpp\
	./slice_generated/omero/Model/XYZCtoXYCLink.cpp\
	./slice_generated/omero/Model/XYZCtoXYZLink.cpp\
	./slice_generated/omero/Model/XYZT.cpp\
	./slice_generated/omero/Model/XYZTtoXYTLink.cpp\
	./slice_generated/omero/Model/XYZTtoXYZLink.cpp\
	./slice_generated/omero/Model/XYZtoXYLink.cpp\
	./slice_generated/omero/ROMIO.cpp\
	./slice_generated/omero/RTypes.cpp\
	./slice_generated/omero/ServerErrors.cpp\
	./slice_generated/omero/System.cpp\
	./slice_generated/omero/fwd.cpp\
	./omero/Model/DetailsI.cpp\
	./omero/Model/PermissionsI.cpp
