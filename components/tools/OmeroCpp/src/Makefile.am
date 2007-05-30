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
CPP_MODEL=$(CPP_GEN)/OMERO/Model.h

INCLUDES=-I$(CPP_GEN) -I$(CPP_SRC) -I$(COPY)
LIBS=-lstdc++ -lIce -lIceUtil -lSlice -lGlacier2

clean-local:
	find $(CPP_GEN) -name "*.cpp" -exec rm {} \;
	find $(CPP_GEN) -name "*.h" -exec rm {} \;

slice: $(CPP_GEN)

model: slice

$(CPP_GEN): $(COPY)/OMERO/Model/*.h
	mkdir -p $(CPP_GEN)/OMERO/Model && \
	@SLICE2CPP@ $(SLICE_INCLUDES) --include-dir=OMERO       --output-dir=$(CPP_GEN)/OMERO/      $(COPY)/OMERO/*.ice && \
	@SLICE2CPP@ $(SLICE_INCLUDES) --include-dir=OMERO/Model --output-dir=$(CPP_GEN)/OMERO/Model $(COPY)/OMERO/Model/*.ice && \
	(test -e $(CPP_MODEL) && rm $(CPP_MODEL)); \
	find $(CPP_GEN)/OMERO/Model/ -name "*.h" -exec grep class {} /dev/null \; | grep IceProxy | grep    omero::model::IObject | cut -d: -f1 | sort | uniq >> $(CPP_MODEL) && \
	find $(CPP_GEN)/OMERO/Model/ -name "*.h" -exec grep class {} /dev/null \; | grep IceProxy | grep -v omero::model::IObject | cut -d: -f1 | sort | uniq >> $(CPP_MODEL) && \
	perl -i -pe 's/^slice_generated\//#include </' $(CPP_MODEL) &&\
	perl -i -pe 's/.h$$/.h>/' $(CPP_MODEL)

lib_LTLIBRARIES            = libOMERO_common.la libOMERO_client.la 
libOMERO_common_la_SOURCES = ./OMERO/ClientErrors.cpp 

#libOMERO_common_a_LDFLAGS = -no-undefined -version-info 0:0:0

#libOMERO_client_a_LDFLAGS = -no-undefined -version-info 0:0:0
libOMERO_client_la_LIBADD  = libOMERO_common.la 
libOMERO_client_la_SOURCES = ./OMERO/client.cpp ./OMERO/CTypes.cpp \
	../target/temp/OMERO/Model/AberrationCorrectionI.cpp\
	../target/temp/OMERO/Model/AcquisitionModeI.cpp\
	../target/temp/OMERO/Model/ArcI.cpp\
	../target/temp/OMERO/Model/ArcTypeI.cpp\
	../target/temp/OMERO/Model/BinningI.cpp\
	../target/temp/OMERO/Model/BoundingBoxI.cpp\
	../target/temp/OMERO/Model/CategoryGroupCategoryLinkI.cpp\
	../target/temp/OMERO/Model/CategoryGroupI.cpp\
	../target/temp/OMERO/Model/CategoryI.cpp\
	../target/temp/OMERO/Model/CategoryImageLinkI.cpp\
	../target/temp/OMERO/Model/CellAreaI.cpp\
	../target/temp/OMERO/Model/CellEccentricityI.cpp\
	../target/temp/OMERO/Model/CellExtentI.cpp\
	../target/temp/OMERO/Model/CellMajorAxisLengthI.cpp\
	../target/temp/OMERO/Model/CellMinorAxisLengthI.cpp\
	../target/temp/OMERO/Model/CellPerimeterI.cpp\
	../target/temp/OMERO/Model/CellPositionI.cpp\
	../target/temp/OMERO/Model/CellSolidityI.cpp\
	../target/temp/OMERO/Model/ChannelBindingI.cpp\
	../target/temp/OMERO/Model/ChannelI.cpp\
	../target/temp/OMERO/Model/CoatingI.cpp\
	../target/temp/OMERO/Model/CodomainMapContextI.cpp\
	../target/temp/OMERO/Model/ColorI.cpp\
	../target/temp/OMERO/Model/ContrastMethodI.cpp\
	../target/temp/OMERO/Model/ContrastStretchingContextI.cpp\
	../target/temp/OMERO/Model/CustomizedFilterSetI.cpp\
	../target/temp/OMERO/Model/DBPatchI.cpp\
	../target/temp/OMERO/Model/DatasetAnnotationI.cpp\
	../target/temp/OMERO/Model/DatasetI.cpp\
	../target/temp/OMERO/Model/DatasetImageLinkI.cpp\
	../target/temp/OMERO/Model/DetectorI.cpp\
	../target/temp/OMERO/Model/DetectorSettingsI.cpp\
	../target/temp/OMERO/Model/DetectorTypeI.cpp\
	../target/temp/OMERO/Model/DichroicI.cpp\
	../target/temp/OMERO/Model/DimensionOrderI.cpp\
	../target/temp/OMERO/Model/DummyStatisticsI.cpp\
	../target/temp/OMERO/Model/EmissionFilterI.cpp\
	../target/temp/OMERO/Model/EventI.cpp\
	../target/temp/OMERO/Model/EventLogI.cpp\
	../target/temp/OMERO/Model/EventTypeI.cpp\
	../target/temp/OMERO/Model/ExcitationFilterI.cpp\
	../target/temp/OMERO/Model/ExperimentI.cpp\
	../target/temp/OMERO/Model/ExperimentTypeI.cpp\
	../target/temp/OMERO/Model/ExperimenterGroupI.cpp\
	../target/temp/OMERO/Model/ExperimenterI.cpp\
	../target/temp/OMERO/Model/ExternalInfoI.cpp\
	../target/temp/OMERO/Model/FamilyI.cpp\
	../target/temp/OMERO/Model/FilamentI.cpp\
	../target/temp/OMERO/Model/FilamentTypeI.cpp\
	../target/temp/OMERO/Model/FilterI.cpp\
	../target/temp/OMERO/Model/FilterSetI.cpp\
	../target/temp/OMERO/Model/FilterTypeI.cpp\
	../target/temp/OMERO/Model/FormatI.cpp\
	../target/temp/OMERO/Model/FrequencyMultiplicationI.cpp\
	../target/temp/OMERO/Model/GroupExperimenterMapI.cpp\
	../target/temp/OMERO/Model/IlluminationI.cpp\
	../target/temp/OMERO/Model/ImageAnnotationI.cpp\
	../target/temp/OMERO/Model/ImageCellCountI.cpp\
	../target/temp/OMERO/Model/ImageI.cpp\
	../target/temp/OMERO/Model/ImageNucleasCountI.cpp\
	../target/temp/OMERO/Model/ImagingEnvironmentI.cpp\
	../target/temp/OMERO/Model/ImmersionI.cpp\
	../target/temp/OMERO/Model/ImportJobI.cpp\
	../target/temp/OMERO/Model/InstrumentI.cpp\
	../target/temp/OMERO/Model/IrisDiaphragmI.cpp\
	../target/temp/OMERO/Model/JobI.cpp\
	../target/temp/OMERO/Model/JobOriginalFileLinkI.cpp\
	../target/temp/OMERO/Model/JobStatusI.cpp\
	../target/temp/OMERO/Model/LaserI.cpp\
	../target/temp/OMERO/Model/LaserMediumI.cpp\
	../target/temp/OMERO/Model/LaserTypeI.cpp\
	../target/temp/OMERO/Model/LightSettingsI.cpp\
	../target/temp/OMERO/Model/LightSourceI.cpp\
	../target/temp/OMERO/Model/LinkI.cpp\
	../target/temp/OMERO/Model/LogicalChannelI.cpp\
	../target/temp/OMERO/Model/MediumI.cpp\
	../target/temp/OMERO/Model/MetadataI.cpp\
	../target/temp/OMERO/Model/MicroscopeI.cpp\
	../target/temp/OMERO/Model/MicroscopeTypeI.cpp\
	../target/temp/OMERO/Model/NucleusAreaI.cpp\
	../target/temp/OMERO/Model/NucleusEccentricityI.cpp\
	../target/temp/OMERO/Model/NucleusExtentI.cpp\
	../target/temp/OMERO/Model/NucleusMajorAxisLengthI.cpp\
	../target/temp/OMERO/Model/NucleusMinorAxisLengthI.cpp\
	../target/temp/OMERO/Model/NucleusPerimeterI.cpp\
	../target/temp/OMERO/Model/NucleusPositionI.cpp\
	../target/temp/OMERO/Model/NucleusSolidityI.cpp\
	../target/temp/OMERO/Model/OTFI.cpp\
	../target/temp/OMERO/Model/ObjectiveI.cpp\
	../target/temp/OMERO/Model/ObjectiveSettingsI.cpp\
	../target/temp/OMERO/Model/OriginalFileI.cpp\
	../target/temp/OMERO/Model/OverlayI.cpp\
	../target/temp/OMERO/Model/OverlayTypeI.cpp\
	../target/temp/OMERO/Model/PhotometricInterpretationI.cpp\
	../target/temp/OMERO/Model/PixelsDimensionsI.cpp\
	../target/temp/OMERO/Model/PixelsI.cpp\
	../target/temp/OMERO/Model/PixelsOriginalFileMapI.cpp\
	../target/temp/OMERO/Model/PixelsTypeI.cpp\
	../target/temp/OMERO/Model/PlaneInfoI.cpp\
	../target/temp/OMERO/Model/PlaneSlicingContextI.cpp\
	../target/temp/OMERO/Model/ProjectAnnotationI.cpp\
	../target/temp/OMERO/Model/ProjectDatasetLinkI.cpp\
	../target/temp/OMERO/Model/ProjectI.cpp\
	../target/temp/OMERO/Model/PulseI.cpp\
	../target/temp/OMERO/Model/QuantumDefI.cpp\
	../target/temp/OMERO/Model/RegionI.cpp\
	../target/temp/OMERO/Model/RegionTypeI.cpp\
	../target/temp/OMERO/Model/RenderingDefI.cpp\
	../target/temp/OMERO/Model/RenderingModelI.cpp\
	../target/temp/OMERO/Model/ReverseIntensityContextI.cpp\
	../target/temp/OMERO/Model/Roi5DI.cpp\
	../target/temp/OMERO/Model/RoiExtentI.cpp\
	../target/temp/OMERO/Model/RoiMapI.cpp\
	../target/temp/OMERO/Model/RoiSetI.cpp\
	../target/temp/OMERO/Model/ShapeAreaI.cpp\
	../target/temp/OMERO/Model/SpecificationI.cpp\
	../target/temp/OMERO/Model/SquareI.cpp\
	../target/temp/OMERO/Model/StageLabelI.cpp\
	../target/temp/OMERO/Model/StatsInfoI.cpp\
	../target/temp/OMERO/Model/ThumbnailI.cpp\
	../target/temp/OMERO/Model/TransmittanceRangeI.cpp\
	../target/temp/OMERO/Model/URoiI.cpp\
	../target/temp/OMERO/Model/UShapeI.cpp\
	../target/temp/OMERO/Model/USliceI.cpp\
	../target/temp/OMERO/Model/USquareI.cpp\
	../target/temp/OMERO/Model/XYCI.cpp\
	../target/temp/OMERO/Model/XYCTI.cpp\
	../target/temp/OMERO/Model/XYCTtoXYCLinkI.cpp\
	../target/temp/OMERO/Model/XYCTtoXYTLinkI.cpp\
	../target/temp/OMERO/Model/XYCtoXYLinkI.cpp\
	../target/temp/OMERO/Model/XYI.cpp\
	../target/temp/OMERO/Model/XYTI.cpp\
	../target/temp/OMERO/Model/XYTtoXYLinkI.cpp\
	../target/temp/OMERO/Model/XYZCI.cpp\
	../target/temp/OMERO/Model/XYZCTI.cpp\
	../target/temp/OMERO/Model/XYZCTtoXYCTLinkI.cpp\
	../target/temp/OMERO/Model/XYZCTtoXYZCLinkI.cpp\
	../target/temp/OMERO/Model/XYZCTtoXYZTLinkI.cpp\
	../target/temp/OMERO/Model/XYZCtoXYCLinkI.cpp\
	../target/temp/OMERO/Model/XYZCtoXYZLinkI.cpp\
	../target/temp/OMERO/Model/XYZI.cpp\
	../target/temp/OMERO/Model/XYZTI.cpp\
	../target/temp/OMERO/Model/XYZTtoXYTLinkI.cpp\
	../target/temp/OMERO/Model/XYZTtoXYZLinkI.cpp\
	../target/temp/OMERO/Model/XYZtoXYLinkI.cpp\
	../target/temp/OMERO/ObjectFactoryRegistrar.cpp\
	./slice_generated/OMERO/API.cpp\
	./slice_generated/OMERO/Constants.cpp\
	./slice_generated/OMERO/IObject.cpp\
	./slice_generated/OMERO/Model/AberrationCorrection.cpp\
	./slice_generated/OMERO/Model/AcquisitionMode.cpp\
	./slice_generated/OMERO/Model/Arc.cpp\
	./slice_generated/OMERO/Model/ArcType.cpp\
	./slice_generated/OMERO/Model/Binning.cpp\
	./slice_generated/OMERO/Model/BoundingBox.cpp\
	./slice_generated/OMERO/Model/Category.cpp\
	./slice_generated/OMERO/Model/CategoryGroup.cpp\
	./slice_generated/OMERO/Model/CategoryGroupCategoryLink.cpp\
	./slice_generated/OMERO/Model/CategoryImageLink.cpp\
	./slice_generated/OMERO/Model/CellArea.cpp\
	./slice_generated/OMERO/Model/CellEccentricity.cpp\
	./slice_generated/OMERO/Model/CellExtent.cpp\
	./slice_generated/OMERO/Model/CellMajorAxisLength.cpp\
	./slice_generated/OMERO/Model/CellMinorAxisLength.cpp\
	./slice_generated/OMERO/Model/CellPerimeter.cpp\
	./slice_generated/OMERO/Model/CellPosition.cpp\
	./slice_generated/OMERO/Model/CellSolidity.cpp\
	./slice_generated/OMERO/Model/Channel.cpp\
	./slice_generated/OMERO/Model/ChannelBinding.cpp\
	./slice_generated/OMERO/Model/Coating.cpp\
	./slice_generated/OMERO/Model/CodomainMapContext.cpp\
	./slice_generated/OMERO/Model/Color.cpp\
	./slice_generated/OMERO/Model/ContrastMethod.cpp\
	./slice_generated/OMERO/Model/ContrastStretchingContext.cpp\
	./slice_generated/OMERO/Model/CustomizedFilterSet.cpp\
	./slice_generated/OMERO/Model/DBPatch.cpp\
	./slice_generated/OMERO/Model/Dataset.cpp\
	./slice_generated/OMERO/Model/DatasetAnnotation.cpp\
	./slice_generated/OMERO/Model/DatasetImageLink.cpp\
	./slice_generated/OMERO/Model/Details.cpp\
	./slice_generated/OMERO/Model/Detector.cpp\
	./slice_generated/OMERO/Model/DetectorSettings.cpp\
	./slice_generated/OMERO/Model/DetectorType.cpp\
	./slice_generated/OMERO/Model/Dichroic.cpp\
	./slice_generated/OMERO/Model/DimensionOrder.cpp\
	./slice_generated/OMERO/Model/DummyStatistics.cpp\
	./slice_generated/OMERO/Model/EmissionFilter.cpp\
	./slice_generated/OMERO/Model/Event.cpp\
	./slice_generated/OMERO/Model/EventLog.cpp\
	./slice_generated/OMERO/Model/EventType.cpp\
	./slice_generated/OMERO/Model/ExcitationFilter.cpp\
	./slice_generated/OMERO/Model/Experiment.cpp\
	./slice_generated/OMERO/Model/ExperimentType.cpp\
	./slice_generated/OMERO/Model/Experimenter.cpp\
	./slice_generated/OMERO/Model/ExperimenterGroup.cpp\
	./slice_generated/OMERO/Model/ExternalInfo.cpp\
	./slice_generated/OMERO/Model/Family.cpp\
	./slice_generated/OMERO/Model/Filament.cpp\
	./slice_generated/OMERO/Model/FilamentType.cpp\
	./slice_generated/OMERO/Model/Filter.cpp\
	./slice_generated/OMERO/Model/FilterSet.cpp\
	./slice_generated/OMERO/Model/FilterType.cpp\
	./slice_generated/OMERO/Model/Format.cpp\
	./slice_generated/OMERO/Model/FrequencyMultiplication.cpp\
	./slice_generated/OMERO/Model/GroupExperimenterMap.cpp\
	./slice_generated/OMERO/Model/Illumination.cpp\
	./slice_generated/OMERO/Model/Image.cpp\
	./slice_generated/OMERO/Model/ImageAnnotation.cpp\
	./slice_generated/OMERO/Model/ImageCellCount.cpp\
	./slice_generated/OMERO/Model/ImageNucleasCount.cpp\
	./slice_generated/OMERO/Model/ImagingEnvironment.cpp\
	./slice_generated/OMERO/Model/Immersion.cpp\
	./slice_generated/OMERO/Model/ImportJob.cpp\
	./slice_generated/OMERO/Model/Instrument.cpp\
	./slice_generated/OMERO/Model/IrisDiaphragm.cpp\
	./slice_generated/OMERO/Model/Job.cpp\
	./slice_generated/OMERO/Model/JobOriginalFileLink.cpp\
	./slice_generated/OMERO/Model/JobStatus.cpp\
	./slice_generated/OMERO/Model/Laser.cpp\
	./slice_generated/OMERO/Model/LaserMedium.cpp\
	./slice_generated/OMERO/Model/LaserType.cpp\
	./slice_generated/OMERO/Model/LightSettings.cpp\
	./slice_generated/OMERO/Model/LightSource.cpp\
	./slice_generated/OMERO/Model/Link.cpp\
	./slice_generated/OMERO/Model/LogicalChannel.cpp\
	./slice_generated/OMERO/Model/Medium.cpp\
	./slice_generated/OMERO/Model/Metadata.cpp\
	./slice_generated/OMERO/Model/Microscope.cpp\
	./slice_generated/OMERO/Model/MicroscopeType.cpp\
	./slice_generated/OMERO/Model/NucleusArea.cpp\
	./slice_generated/OMERO/Model/NucleusEccentricity.cpp\
	./slice_generated/OMERO/Model/NucleusExtent.cpp\
	./slice_generated/OMERO/Model/NucleusMajorAxisLength.cpp\
	./slice_generated/OMERO/Model/NucleusMinorAxisLength.cpp\
	./slice_generated/OMERO/Model/NucleusPerimeter.cpp\
	./slice_generated/OMERO/Model/NucleusPosition.cpp\
	./slice_generated/OMERO/Model/NucleusSolidity.cpp\
	./slice_generated/OMERO/Model/OTF.cpp\
	./slice_generated/OMERO/Model/Objective.cpp\
	./slice_generated/OMERO/Model/ObjectiveSettings.cpp\
	./slice_generated/OMERO/Model/OriginalFile.cpp\
	./slice_generated/OMERO/Model/Overlay.cpp\
	./slice_generated/OMERO/Model/OverlayType.cpp\
	./slice_generated/OMERO/Model/Permissions.cpp\
	./slice_generated/OMERO/Model/PhotometricInterpretation.cpp\
	./slice_generated/OMERO/Model/Pixels.cpp\
	./slice_generated/OMERO/Model/PixelsDimensions.cpp\
	./slice_generated/OMERO/Model/PixelsOriginalFileMap.cpp\
	./slice_generated/OMERO/Model/PixelsType.cpp\
	./slice_generated/OMERO/Model/PlaneInfo.cpp\
	./slice_generated/OMERO/Model/PlaneSlicingContext.cpp\
	./slice_generated/OMERO/Model/Project.cpp\
	./slice_generated/OMERO/Model/ProjectAnnotation.cpp\
	./slice_generated/OMERO/Model/ProjectDatasetLink.cpp\
	./slice_generated/OMERO/Model/Pulse.cpp\
	./slice_generated/OMERO/Model/QuantumDef.cpp\
	./slice_generated/OMERO/Model/Region.cpp\
	./slice_generated/OMERO/Model/RegionType.cpp\
	./slice_generated/OMERO/Model/RenderingDef.cpp\
	./slice_generated/OMERO/Model/RenderingModel.cpp\
	./slice_generated/OMERO/Model/ReverseIntensityContext.cpp\
	./slice_generated/OMERO/Model/Roi5D.cpp\
	./slice_generated/OMERO/Model/RoiExtent.cpp\
	./slice_generated/OMERO/Model/RoiMap.cpp\
	./slice_generated/OMERO/Model/RoiSet.cpp\
	./slice_generated/OMERO/Model/ShapeArea.cpp\
	./slice_generated/OMERO/Model/Specification.cpp\
	./slice_generated/OMERO/Model/Square.cpp\
	./slice_generated/OMERO/Model/StageLabel.cpp\
	./slice_generated/OMERO/Model/StatsInfo.cpp\
	./slice_generated/OMERO/Model/Thumbnail.cpp\
	./slice_generated/OMERO/Model/TransmittanceRange.cpp\
	./slice_generated/OMERO/Model/URoi.cpp\
	./slice_generated/OMERO/Model/UShape.cpp\
	./slice_generated/OMERO/Model/USlice.cpp\
	./slice_generated/OMERO/Model/USquare.cpp\
	./slice_generated/OMERO/Model/XY.cpp\
	./slice_generated/OMERO/Model/XYC.cpp\
	./slice_generated/OMERO/Model/XYCT.cpp\
	./slice_generated/OMERO/Model/XYCTtoXYCLink.cpp\
	./slice_generated/OMERO/Model/XYCTtoXYTLink.cpp\
	./slice_generated/OMERO/Model/XYCtoXYLink.cpp\
	./slice_generated/OMERO/Model/XYT.cpp\
	./slice_generated/OMERO/Model/XYTtoXYLink.cpp\
	./slice_generated/OMERO/Model/XYZ.cpp\
	./slice_generated/OMERO/Model/XYZC.cpp\
	./slice_generated/OMERO/Model/XYZCT.cpp\
	./slice_generated/OMERO/Model/XYZCTtoXYCTLink.cpp\
	./slice_generated/OMERO/Model/XYZCTtoXYZCLink.cpp\
	./slice_generated/OMERO/Model/XYZCTtoXYZTLink.cpp\
	./slice_generated/OMERO/Model/XYZCtoXYCLink.cpp\
	./slice_generated/OMERO/Model/XYZCtoXYZLink.cpp\
	./slice_generated/OMERO/Model/XYZT.cpp\
	./slice_generated/OMERO/Model/XYZTtoXYTLink.cpp\
	./slice_generated/OMERO/Model/XYZTtoXYZLink.cpp\
	./slice_generated/OMERO/Model/XYZtoXYLink.cpp\
	./slice_generated/OMERO/ROMIO.cpp\
	./slice_generated/OMERO/RTypes.cpp\
	./slice_generated/OMERO/ServerErrors.cpp\
	./slice_generated/OMERO/System.cpp\
	./slice_generated/OMERO/fwd.cpp\
	./OMERO/Model/DetailsI.cpp\
	./OMERO/Model/PermissionsI.cpp
