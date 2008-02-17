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
	test -e $(CPP_GEN) && find $(CPP_GEN) -name "*.cpp" -exec rm {} \; || true
	test -e $(CPP_GEN) && $find $(CPP_GEN) -name "*.h" -exec rm {} \; || true

distclean-local:
	test -e .deps && rm -rf .deps || true
	test -e .libs && rm -rf .libs || true

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
	./omero/model/DetailsI.cpp\
	./omero/model/PermissionsI.cpp\
	slice_generated//omero/API.cpp\
	slice_generated//omero/Constants.cpp\
	slice_generated//omero/fwd.cpp\
	slice_generated//omero/IObject.cpp\
	slice_generated//omero/model/AberrationCorrection.cpp\
	slice_generated//omero/model/AcquisitionMode.cpp\
	slice_generated//omero/model/Annotation.cpp\
	slice_generated//omero/model/AnnotationAnnotationLink.cpp\
	slice_generated//omero/model/Arc.cpp\
	slice_generated//omero/model/ArcType.cpp\
	slice_generated//omero/model/BasicAnnotation.cpp\
	slice_generated//omero/model/Binning.cpp\
	slice_generated//omero/model/BooleanAnnotation.cpp\
	slice_generated//omero/model/Category.cpp\
	slice_generated//omero/model/CategoryGroup.cpp\
	slice_generated//omero/model/CategoryGroupCategoryLink.cpp\
	slice_generated//omero/model/CategoryImageLink.cpp\
	slice_generated//omero/model/CellArea.cpp\
	slice_generated//omero/model/CellEccentricity.cpp\
	slice_generated//omero/model/CellExtent.cpp\
	slice_generated//omero/model/CellMajorAxisLength.cpp\
	slice_generated//omero/model/CellMinorAxisLength.cpp\
	slice_generated//omero/model/CellPerimeter.cpp\
	slice_generated//omero/model/CellPosition.cpp\
	slice_generated//omero/model/CellSolidity.cpp\
	slice_generated//omero/model/Channel.cpp\
	slice_generated//omero/model/ChannelAnnotationLink.cpp\
	slice_generated//omero/model/ChannelBinding.cpp\
	slice_generated//omero/model/Coating.cpp\
	slice_generated//omero/model/CodomainMapContext.cpp\
	slice_generated//omero/model/Color.cpp\
	slice_generated//omero/model/ContrastMethod.cpp\
	slice_generated//omero/model/ContrastStretchingContext.cpp\
	slice_generated//omero/model/CustomizedFilterSet.cpp\
	slice_generated//omero/model/Dataset.cpp\
	slice_generated//omero/model/DatasetAnnotationLink.cpp\
	slice_generated//omero/model/DatasetImageLink.cpp\
	slice_generated//omero/model/DBPatch.cpp\
	slice_generated//omero/model/Details.cpp\
	slice_generated//omero/model/Detector.cpp\
	slice_generated//omero/model/DetectorSettings.cpp\
	slice_generated//omero/model/DetectorType.cpp\
	slice_generated//omero/model/Dichroic.cpp\
	slice_generated//omero/model/DimensionOrder.cpp\
	slice_generated//omero/model/DoubleAnnotation.cpp\
	slice_generated//omero/model/EmissionFilter.cpp\
	slice_generated//omero/model/Event.cpp\
	slice_generated//omero/model/EventLog.cpp\
	slice_generated//omero/model/EventType.cpp\
	slice_generated//omero/model/ExcitationFilter.cpp\
	slice_generated//omero/model/Experiment.cpp\
	slice_generated//omero/model/Experimenter.cpp\
	slice_generated//omero/model/ExperimenterAnnotationLink.cpp\
	slice_generated//omero/model/ExperimenterGroup.cpp\
	slice_generated//omero/model/ExperimentType.cpp\
	slice_generated//omero/model/ExternalInfo.cpp\
	slice_generated//omero/model/Family.cpp\
	slice_generated//omero/model/Filament.cpp\
	slice_generated//omero/model/FilamentType.cpp\
	slice_generated//omero/model/FileAnnotation.cpp\
	slice_generated//omero/model/Filter.cpp\
	slice_generated//omero/model/FilterSet.cpp\
	slice_generated//omero/model/FilterType.cpp\
	slice_generated//omero/model/Format.cpp\
	slice_generated//omero/model/FrequencyMultiplication.cpp\
	slice_generated//omero/model/GroupExperimenterMap.cpp\
	slice_generated//omero/model/Illumination.cpp\
	slice_generated//omero/model/Image.cpp\
	slice_generated//omero/model/ImageAnnotationLink.cpp\
	slice_generated//omero/model/ImageCellCount.cpp\
	slice_generated//omero/model/ImageNucleasCount.cpp\
	slice_generated//omero/model/ImagingEnvironment.cpp\
	slice_generated//omero/model/Immersion.cpp\
	slice_generated//omero/model/ImportJob.cpp\
	slice_generated//omero/model/Instrument.cpp\
	slice_generated//omero/model/IrisDiaphragm.cpp\
	slice_generated//omero/model/Job.cpp\
	slice_generated//omero/model/JobOriginalFileLink.cpp\
	slice_generated//omero/model/JobStatus.cpp\
	slice_generated//omero/model/Laser.cpp\
	slice_generated//omero/model/LaserMedium.cpp\
	slice_generated//omero/model/LaserType.cpp\
	slice_generated//omero/model/LightSettings.cpp\
	slice_generated//omero/model/LightSource.cpp\
	slice_generated//omero/model/Link.cpp\
	slice_generated//omero/model/ListAnnotation.cpp\
	slice_generated//omero/model/LogicalChannel.cpp\
	slice_generated//omero/model/LongAnnotation.cpp\
	slice_generated//omero/model/Medium.cpp\
	slice_generated//omero/model/Microscope.cpp\
	slice_generated//omero/model/MicroscopeType.cpp\
	slice_generated//omero/model/NucleusArea.cpp\
	slice_generated//omero/model/NucleusEccentricity.cpp\
	slice_generated//omero/model/NucleusExtent.cpp\
	slice_generated//omero/model/NucleusMajorAxisLength.cpp\
	slice_generated//omero/model/NucleusMinorAxisLength.cpp\
	slice_generated//omero/model/NucleusPerimeter.cpp\
	slice_generated//omero/model/NucleusPosition.cpp\
	slice_generated//omero/model/NucleusSolidity.cpp\
	slice_generated//omero/model/NumericAnnotation.cpp\
	slice_generated//omero/model/Objective.cpp\
	slice_generated//omero/model/ObjectiveSettings.cpp\
	slice_generated//omero/model/OriginalFile.cpp\
	slice_generated//omero/model/OriginalFileAnnotationLink.cpp\
	slice_generated//omero/model/OTF.cpp\
	slice_generated//omero/model/Permissions.cpp\
	slice_generated//omero/model/PhotometricInterpretation.cpp\
	slice_generated//omero/model/Pixels.cpp\
	slice_generated//omero/model/PixelsAnnotationLink.cpp\
	slice_generated//omero/model/PixelsDimensions.cpp\
	slice_generated//omero/model/PixelsOriginalFileMap.cpp\
	slice_generated//omero/model/PixelsType.cpp\
	slice_generated//omero/model/PlaneInfo.cpp\
	slice_generated//omero/model/PlaneInfoAnnotationLink.cpp\
	slice_generated//omero/model/PlaneSlicingContext.cpp\
	slice_generated//omero/model/Plate.cpp\
	slice_generated//omero/model/PlateAnnotationLink.cpp\
	slice_generated//omero/model/Project.cpp\
	slice_generated//omero/model/ProjectAnnotationLink.cpp\
	slice_generated//omero/model/ProjectDatasetLink.cpp\
	slice_generated//omero/model/Pulse.cpp\
	slice_generated//omero/model/QuantumDef.cpp\
	slice_generated//omero/model/QueryAnnotation.cpp\
	slice_generated//omero/model/Reagent.cpp\
	slice_generated//omero/model/ReagentAnnotationLink.cpp\
	slice_generated//omero/model/RenderingDef.cpp\
	slice_generated//omero/model/RenderingModel.cpp\
	slice_generated//omero/model/ReverseIntensityContext.cpp\
	slice_generated//omero/model/Roi.cpp\
	slice_generated//omero/model/RoiLink.cpp\
	slice_generated//omero/model/RoiLinkAnnotationLink.cpp\
	slice_generated//omero/model/Screen.cpp\
	slice_generated//omero/model/ScreenAcquisition.cpp\
	slice_generated//omero/model/ScreenAcquisitionAnnotationLink.cpp\
	slice_generated//omero/model/ScreenAnnotationLink.cpp\
	slice_generated//omero/model/ScreenPlateLink.cpp\
	slice_generated//omero/model/ScreenReagentLink.cpp\
	slice_generated//omero/model/ScriptJob.cpp\
	slice_generated//omero/model/Session.cpp\
	slice_generated//omero/model/StageLabel.cpp\
	slice_generated//omero/model/StatsInfo.cpp\
	slice_generated//omero/model/TagAnnotation.cpp\
	slice_generated//omero/model/TextAnnotation.cpp\
	slice_generated//omero/model/Thumbnail.cpp\
	slice_generated//omero/model/ThumbnailAnnotation.cpp\
	slice_generated//omero/model/TimestampAnnotation.cpp\
	slice_generated//omero/model/TransmittanceRange.cpp\
	slice_generated//omero/model/TypeAnnotation.cpp\
	slice_generated//omero/model/UrlAnnotation.cpp\
	slice_generated//omero/model/Well.cpp\
	slice_generated//omero/model/WellAnnotationLink.cpp\
	slice_generated//omero/model/WellSample.cpp\
	slice_generated//omero/model/WellSampleAnnotationLink.cpp\
	slice_generated//omero/model/WellSampleImageLink.cpp\
	slice_generated//omero/model/XmlAnnotation.cpp\
	slice_generated//omero/ROMIO.cpp\
	slice_generated//omero/RTypes.cpp\
	slice_generated//omero/Scripts.cpp\
	slice_generated//omero/ServerErrors.cpp\
	slice_generated//omero/System.cpp\
	../target/temp//omero/model/AberrationCorrectionI.cpp\
	../target/temp//omero/model/AcquisitionModeI.cpp\
	../target/temp//omero/model/AnnotationAnnotationLinkI.cpp\
	../target/temp//omero/model/AnnotationI.cpp\
	../target/temp//omero/model/ArcI.cpp\
	../target/temp//omero/model/ArcTypeI.cpp\
	../target/temp//omero/model/BasicAnnotationI.cpp\
	../target/temp//omero/model/BinningI.cpp\
	../target/temp//omero/model/BooleanAnnotationI.cpp\
	../target/temp//omero/model/CategoryGroupCategoryLinkI.cpp\
	../target/temp//omero/model/CategoryGroupI.cpp\
	../target/temp//omero/model/CategoryI.cpp\
	../target/temp//omero/model/CategoryImageLinkI.cpp\
	../target/temp//omero/model/CellAreaI.cpp\
	../target/temp//omero/model/CellEccentricityI.cpp\
	../target/temp//omero/model/CellExtentI.cpp\
	../target/temp//omero/model/CellMajorAxisLengthI.cpp\
	../target/temp//omero/model/CellMinorAxisLengthI.cpp\
	../target/temp//omero/model/CellPerimeterI.cpp\
	../target/temp//omero/model/CellPositionI.cpp\
	../target/temp//omero/model/CellSolidityI.cpp\
	../target/temp//omero/model/ChannelAnnotationLinkI.cpp\
	../target/temp//omero/model/ChannelBindingI.cpp\
	../target/temp//omero/model/ChannelI.cpp\
	../target/temp//omero/model/CoatingI.cpp\
	../target/temp//omero/model/CodomainMapContextI.cpp\
	../target/temp//omero/model/ColorI.cpp\
	../target/temp//omero/model/ContrastMethodI.cpp\
	../target/temp//omero/model/ContrastStretchingContextI.cpp\
	../target/temp//omero/model/CustomizedFilterSetI.cpp\
	../target/temp//omero/model/DatasetAnnotationLinkI.cpp\
	../target/temp//omero/model/DatasetI.cpp\
	../target/temp//omero/model/DatasetImageLinkI.cpp\
	../target/temp//omero/model/DBPatchI.cpp\
	../target/temp//omero/model/DetectorI.cpp\
	../target/temp//omero/model/DetectorSettingsI.cpp\
	../target/temp//omero/model/DetectorTypeI.cpp\
	../target/temp//omero/model/DichroicI.cpp\
	../target/temp//omero/model/DimensionOrderI.cpp\
	../target/temp//omero/model/DoubleAnnotationI.cpp\
	../target/temp//omero/model/EmissionFilterI.cpp\
	../target/temp//omero/model/EventI.cpp\
	../target/temp//omero/model/EventLogI.cpp\
	../target/temp//omero/model/EventTypeI.cpp\
	../target/temp//omero/model/ExcitationFilterI.cpp\
	../target/temp//omero/model/ExperimenterAnnotationLinkI.cpp\
	../target/temp//omero/model/ExperimenterGroupI.cpp\
	../target/temp//omero/model/ExperimenterI.cpp\
	../target/temp//omero/model/ExperimentI.cpp\
	../target/temp//omero/model/ExperimentTypeI.cpp\
	../target/temp//omero/model/ExternalInfoI.cpp\
	../target/temp//omero/model/FamilyI.cpp\
	../target/temp//omero/model/FilamentI.cpp\
	../target/temp//omero/model/FilamentTypeI.cpp\
	../target/temp//omero/model/FileAnnotationI.cpp\
	../target/temp//omero/model/FilterI.cpp\
	../target/temp//omero/model/FilterSetI.cpp\
	../target/temp//omero/model/FilterTypeI.cpp\
	../target/temp//omero/model/FormatI.cpp\
	../target/temp//omero/model/FrequencyMultiplicationI.cpp\
	../target/temp//omero/model/GroupExperimenterMapI.cpp\
	../target/temp//omero/model/IlluminationI.cpp\
	../target/temp//omero/model/ImageAnnotationLinkI.cpp\
	../target/temp//omero/model/ImageCellCountI.cpp\
	../target/temp//omero/model/ImageI.cpp\
	../target/temp//omero/model/ImageNucleasCountI.cpp\
	../target/temp//omero/model/ImagingEnvironmentI.cpp\
	../target/temp//omero/model/ImmersionI.cpp\
	../target/temp//omero/model/ImportJobI.cpp\
	../target/temp//omero/model/InstrumentI.cpp\
	../target/temp//omero/model/IrisDiaphragmI.cpp\
	../target/temp//omero/model/JobI.cpp\
	../target/temp//omero/model/JobOriginalFileLinkI.cpp\
	../target/temp//omero/model/JobStatusI.cpp\
	../target/temp//omero/model/LaserI.cpp\
	../target/temp//omero/model/LaserMediumI.cpp\
	../target/temp//omero/model/LaserTypeI.cpp\
	../target/temp//omero/model/LightSettingsI.cpp\
	../target/temp//omero/model/LightSourceI.cpp\
	../target/temp//omero/model/LinkI.cpp\
	../target/temp//omero/model/ListAnnotationI.cpp\
	../target/temp//omero/model/LogicalChannelI.cpp\
	../target/temp//omero/model/LongAnnotationI.cpp\
	../target/temp//omero/model/MediumI.cpp\
	../target/temp//omero/model/MicroscopeI.cpp\
	../target/temp//omero/model/MicroscopeTypeI.cpp\
	../target/temp//omero/model/NucleusAreaI.cpp\
	../target/temp//omero/model/NucleusEccentricityI.cpp\
	../target/temp//omero/model/NucleusExtentI.cpp\
	../target/temp//omero/model/NucleusMajorAxisLengthI.cpp\
	../target/temp//omero/model/NucleusMinorAxisLengthI.cpp\
	../target/temp//omero/model/NucleusPerimeterI.cpp\
	../target/temp//omero/model/NucleusPositionI.cpp\
	../target/temp//omero/model/NucleusSolidityI.cpp\
	../target/temp//omero/model/NumericAnnotationI.cpp\
	../target/temp//omero/model/ObjectiveI.cpp\
	../target/temp//omero/model/ObjectiveSettingsI.cpp\
	../target/temp//omero/model/OriginalFileAnnotationLinkI.cpp\
	../target/temp//omero/model/OriginalFileI.cpp\
	../target/temp//omero/model/OTFI.cpp\
	../target/temp//omero/model/PhotometricInterpretationI.cpp\
	../target/temp//omero/model/PixelsAnnotationLinkI.cpp\
	../target/temp//omero/model/PixelsDimensionsI.cpp\
	../target/temp//omero/model/PixelsI.cpp\
	../target/temp//omero/model/PixelsOriginalFileMapI.cpp\
	../target/temp//omero/model/PixelsTypeI.cpp\
	../target/temp//omero/model/PlaneInfoAnnotationLinkI.cpp\
	../target/temp//omero/model/PlaneInfoI.cpp\
	../target/temp//omero/model/PlaneSlicingContextI.cpp\
	../target/temp//omero/model/PlateAnnotationLinkI.cpp\
	../target/temp//omero/model/PlateI.cpp\
	../target/temp//omero/model/ProjectAnnotationLinkI.cpp\
	../target/temp//omero/model/ProjectDatasetLinkI.cpp\
	../target/temp//omero/model/ProjectI.cpp\
	../target/temp//omero/model/PulseI.cpp\
	../target/temp//omero/model/QuantumDefI.cpp\
	../target/temp//omero/model/QueryAnnotationI.cpp\
	../target/temp//omero/model/ReagentAnnotationLinkI.cpp\
	../target/temp//omero/model/ReagentI.cpp\
	../target/temp//omero/model/RenderingDefI.cpp\
	../target/temp//omero/model/RenderingModelI.cpp\
	../target/temp//omero/model/ReverseIntensityContextI.cpp\
	../target/temp//omero/model/RoiI.cpp\
	../target/temp//omero/model/RoiLinkAnnotationLinkI.cpp\
	../target/temp//omero/model/RoiLinkI.cpp\
	../target/temp//omero/model/ScreenAcquisitionAnnotationLinkI.cpp\
	../target/temp//omero/model/ScreenAcquisitionI.cpp\
	../target/temp//omero/model/ScreenAnnotationLinkI.cpp\
	../target/temp//omero/model/ScreenI.cpp\
	../target/temp//omero/model/ScreenPlateLinkI.cpp\
	../target/temp//omero/model/ScreenReagentLinkI.cpp\
	../target/temp//omero/model/ScriptJobI.cpp\
	../target/temp//omero/model/SessionI.cpp\
	../target/temp//omero/model/StageLabelI.cpp\
	../target/temp//omero/model/StatsInfoI.cpp\
	../target/temp//omero/model/TagAnnotationI.cpp\
	../target/temp//omero/model/TextAnnotationI.cpp\
	../target/temp//omero/model/ThumbnailAnnotationI.cpp\
	../target/temp//omero/model/ThumbnailI.cpp\
	../target/temp//omero/model/TimestampAnnotationI.cpp\
	../target/temp//omero/model/TransmittanceRangeI.cpp\
	../target/temp//omero/model/TypeAnnotationI.cpp\
	../target/temp//omero/model/UrlAnnotationI.cpp\
	../target/temp//omero/model/WellAnnotationLinkI.cpp\
	../target/temp//omero/model/WellI.cpp\
	../target/temp//omero/model/WellSampleAnnotationLinkI.cpp\
	../target/temp//omero/model/WellSampleI.cpp\
	../target/temp//omero/model/WellSampleImageLinkI.cpp\
	../target/temp//omero/model/XmlAnnotationI.cpp\
	../target/temp//omero/ObjectFactoryRegistrar.cpp
