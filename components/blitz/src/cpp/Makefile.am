#
#   $Id$
#
#   Copyright 2007 Glencoe Software, Inc. All rights reserved.
#   Use is subject to license terms supplied in LICENSE.txt
#
#

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
	@SLICE2CPP@ $(SLICE_INCLUDES) --output-dir=$(CPP_GEN) $(SLICE_GEN)/*.ice $(SLICE_GEN)/OMERO/*.ice $(SLICE_OME)/*.ice

lib_LTLIBRARIES     	= libOMERO_common.la libOMERO_client.la
libOMERO_common_la_SOURCES = generated/ice.cpp generated/omero.cpp generated/API.cpp generated/AberrationCorrectionI.cpp generated/AcquisitionModeI.cpp generated/ArcI.cpp generated/ArcTypeI.cpp generated/BinningI.cpp generated/BoundingBoxI.cpp generated/CategoryGroupCategoryLinkI.cpp generated/CategoryGroupI.cpp generated/CategoryI.cpp generated/CategoryImageLinkI.cpp generated/CellAreaI.cpp generated/CellEccentricityI.cpp generated/CellExtentI.cpp generated/CellMajorAxisLengthI.cpp generated/CellMinorAxisLengthI.cpp generated/CellPerimeterI.cpp generated/CellPositionI.cpp generated/CellSolidityI.cpp generated/ChannelBindingI.cpp generated/ChannelI.cpp generated/CoatingI.cpp generated/CodomainMapContextI.cpp generated/ColorI.cpp generated/Constants.cpp generated/ContrastMethodI.cpp generated/ContrastStretchingContextI.cpp generated/CustomizedFilterSetI.cpp generated/DatasetAnnotationI.cpp generated/DatasetI.cpp generated/DatasetImageLinkI.cpp generated/DetectorI.cpp generated/DetectorSettingsI.cpp generated/DetectorTypeI.cpp generated/DichroicI.cpp generated/DimensionOrderI.cpp generated/DummyStatisticsI.cpp generated/EmissionFilterI.cpp generated/Error.cpp generated/EventI.cpp generated/EventLogI.cpp generated/EventTypeI.cpp generated/ExcitationFilterI.cpp generated/ExperimentI.cpp generated/ExperimentTypeI.cpp generated/ExperimenterGroupI.cpp generated/ExperimenterI.cpp generated/ExternalInfoI.cpp generated/FamilyI.cpp generated/FilamentI.cpp generated/FilamentTypeI.cpp generated/FilterI.cpp generated/FilterSetI.cpp generated/FilterTypeI.cpp generated/FormatI.cpp generated/FrequencyMultiplicationI.cpp generated/GroupExperimenterMapI.cpp generated/IObject.cpp generated/IlluminationI.cpp generated/ImageAnnotationI.cpp generated/ImageCellCountI.cpp generated/ImageI.cpp generated/ImageNucleasCountI.cpp generated/ImagingEnvironmentI.cpp generated/ImmersionI.cpp generated/ImportJobI.cpp generated/InstrumentI.cpp generated/IrisDiaphragmI.cpp generated/JobI.cpp generated/JobOriginalFileLinkI.cpp generated/JobStatusI.cpp generated/LaserI.cpp generated/LaserMediumI.cpp generated/LaserTypeI.cpp generated/LightSettingsI.cpp generated/LightSourceI.cpp generated/LinkI.cpp generated/LogicalChannelI.cpp generated/MediumI.cpp generated/MetadataI.cpp generated/MicroscopeI.cpp generated/MicroscopeTypeI.cpp generated/NucleusAreaI.cpp generated/NucleusEccentricityI.cpp generated/NucleusExtentI.cpp generated/NucleusMajorAxisLengthI.cpp generated/NucleusMinorAxisLengthI.cpp generated/NucleusPerimeterI.cpp generated/NucleusPositionI.cpp generated/NucleusSolidityI.cpp generated/OTFI.cpp generated/ObjectiveI.cpp generated/ObjectiveSettingsI.cpp generated/OriginalFileI.cpp generated/OverlayI.cpp generated/OverlayTypeI.cpp generated/PhotometricInterpretationI.cpp generated/PixelsDimensionsI.cpp generated/PixelsI.cpp generated/PixelsOriginalFileMapI.cpp generated/PixelsTypeI.cpp generated/PlaneInfoI.cpp generated/PlaneSlicingContextI.cpp generated/ProjectAnnotationI.cpp generated/ProjectDatasetLinkI.cpp generated/ProjectI.cpp generated/PulseI.cpp generated/QuantumDefI.cpp generated/ROMIO.cpp generated/RTypes.cpp generated/RegionI.cpp generated/RegionTypeI.cpp generated/RenderingDefI.cpp generated/RenderingModelI.cpp generated/ReverseIntensityContextI.cpp generated/Roi5DI.cpp generated/RoiExtentI.cpp generated/RoiMapI.cpp generated/RoiSetI.cpp generated/ShapeAreaI.cpp generated/SpecificationI.cpp generated/SquareI.cpp generated/StageLabelI.cpp generated/StatsInfoI.cpp generated/System.cpp generated/ThumbnailI.cpp generated/TransmittanceRangeI.cpp generated/URoiI.cpp generated/UShapeI.cpp generated/USliceI.cpp generated/USquareI.cpp generated/XYCI.cpp generated/XYCTI.cpp generated/XYCTtoXYCLinkI.cpp generated/XYCTtoXYTLinkI.cpp generated/XYCtoXYLinkI.cpp generated/XYI.cpp generated/XYTI.cpp generated/XYTtoXYLinkI.cpp generated/XYZCI.cpp generated/XYZCTI.cpp generated/XYZCTtoXYCTLinkI.cpp generated/XYZCTtoXYZCLinkI.cpp generated/XYZCTtoXYZTLinkI.cpp generated/XYZCtoXYCLinkI.cpp generated/XYZCtoXYZLinkI.cpp generated/XYZI.cpp generated/XYZTI.cpp generated/XYZTtoXYTLinkI.cpp generated/XYZTtoXYZLinkI.cpp generated/XYZtoXYLinkI.cpp

#libOMERO_common_a_LDFLAGS = -no-undefined -version-info 0:0:0

libOMERO_client_la_SOURCES= ./OMERO/client.cpp
libOMERO_client_la_LIBADD = libOMERO_common.la
#libOMERO_client_a_LDFLAGS= -no-undefined -version-info 0:0:0

