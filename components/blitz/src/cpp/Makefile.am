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
	@SLICE2CPP@ $(SLICE_INCLUDES) --include-dir=OMERO --output-dir=$(CPP_GEN)/OMERO $(SLICE_GEN)/OMERO/*.ice $(SLICE_OME)/OMERO/*.ice

lib_LTLIBRARIES     	= libOMERO_common.la libOMERO_client.la 
libOMERO_common_la_SOURCES = OMERO/errors.cpp DetailsI.cpp PermissionsI.cpp generated/ObjectFactoryRegistrar.cpp generated/OMERO/fwd.cpp generated/OMERO/ice.cpp generated/OMERO/API.cpp generated/OMERO/Constants.cpp generated/OMERO/Error.cpp generated/OMERO/IObject.cpp generated/OMERO/ROMIO.cpp generated/OMERO/RTypes.cpp generated/OMERO/System.cpp generated/FilterI.cpp generated/ExternalInfoI.cpp generated/XYZTtoXYTLinkI.cpp generated/PlaneSlicingContextI.cpp generated/CellMinorAxisLengthI.cpp generated/EventI.cpp generated/ImageNucleasCountI.cpp generated/FormatI.cpp generated/XYZCTI.cpp generated/DatasetImageLinkI.cpp generated/XYTtoXYLinkI.cpp generated/ShapeAreaI.cpp generated/JobStatusI.cpp generated/ArcTypeI.cpp generated/URoiI.cpp generated/NucleusPositionI.cpp generated/ExcitationFilterI.cpp generated/QuantumDefI.cpp generated/ThumbnailI.cpp generated/ImportJobI.cpp generated/ImageCellCountI.cpp generated/DummyStatisticsI.cpp generated/CellEccentricityI.cpp generated/ContrastMethodI.cpp generated/IlluminationI.cpp generated/PlaneInfoI.cpp generated/DichroicI.cpp generated/ExperimentI.cpp generated/ProjectDatasetLinkI.cpp generated/NucleusEccentricityI.cpp generated/ReverseIntensityContextI.cpp generated/XYZI.cpp generated/MicroscopeTypeI.cpp generated/XYZCI.cpp generated/LaserMediumI.cpp generated/JobI.cpp generated/PulseI.cpp generated/CategoryGroupCategoryLinkI.cpp generated/DatasetAnnotationI.cpp generated/RoiMapI.cpp generated/Roi5DI.cpp generated/TransmittanceRangeI.cpp generated/JobOriginalFileLinkI.cpp generated/ExperimenterI.cpp generated/FilamentTypeI.cpp generated/CellAreaI.cpp generated/XYZCTtoXYZTLinkI.cpp generated/ImageI.cpp generated/XYCI.cpp generated/StageLabelI.cpp generated/NucleusMinorAxisLengthI.cpp generated/NucleusSolidityI.cpp generated/USquareI.cpp generated/FilterSetI.cpp generated/DetectorSettingsI.cpp generated/ObjectiveSettingsI.cpp generated/ProjectI.cpp generated/EventLogI.cpp generated/XYCTI.cpp generated/GroupExperimenterMapI.cpp generated/FrequencyMultiplicationI.cpp generated/ProjectAnnotationI.cpp generated/CategoryGroupI.cpp generated/ChannelBindingI.cpp generated/PixelsTypeI.cpp generated/EmissionFilterI.cpp generated/SquareI.cpp generated/XYZCTtoXYCTLinkI.cpp generated/XYZTI.cpp generated/PixelsDimensionsI.cpp generated/StatsInfoI.cpp generated/RoiExtentI.cpp generated/RenderingModelI.cpp generated/CellMajorAxisLengthI.cpp generated/XYZtoXYLinkI.cpp generated/XYCTtoXYTLinkI.cpp generated/ContrastStretchingContextI.cpp generated/XYZCTtoXYZCLinkI.cpp generated/LogicalChannelI.cpp generated/OverlayI.cpp generated/PixelsOriginalFileMapI.cpp generated/AcquisitionModeI.cpp generated/CellPerimeterI.cpp generated/ImageAnnotationI.cpp generated/OverlayTypeI.cpp generated/CategoryImageLinkI.cpp generated/CustomizedFilterSetI.cpp generated/ChannelI.cpp generated/LaserTypeI.cpp generated/CellPositionI.cpp generated/NucleusExtentI.cpp generated/DetectorTypeI.cpp generated/BoundingBoxI.cpp generated/ColorI.cpp generated/MicroscopeI.cpp generated/XYTI.cpp generated/ArcI.cpp generated/LightSettingsI.cpp generated/LightSourceI.cpp generated/XYZCtoXYZLinkI.cpp generated/FamilyI.cpp generated/CategoryI.cpp generated/IrisDiaphragmI.cpp generated/ExperimentTypeI.cpp generated/PhotometricInterpretationI.cpp generated/NucleusAreaI.cpp generated/OriginalFileI.cpp generated/PixelsI.cpp generated/XYCtoXYLinkI.cpp generated/SpecificationI.cpp generated/DatasetI.cpp generated/ExperimenterGroupI.cpp generated/FilterTypeI.cpp generated/BinningI.cpp generated/CoatingI.cpp generated/MediumI.cpp generated/CellExtentI.cpp generated/XYCTtoXYCLinkI.cpp generated/LaserI.cpp generated/OTFI.cpp generated/UShapeI.cpp generated/EventTypeI.cpp generated/ImagingEnvironmentI.cpp generated/USliceI.cpp generated/NucleusMajorAxisLengthI.cpp generated/RenderingDefI.cpp generated/ImmersionI.cpp generated/AberrationCorrectionI.cpp generated/MetadataI.cpp generated/InstrumentI.cpp generated/FilamentI.cpp generated/ObjectiveI.cpp generated/NucleusPerimeterI.cpp generated/RegionTypeI.cpp generated/CodomainMapContextI.cpp generated/RegionI.cpp generated/CellSolidityI.cpp generated/DetectorI.cpp generated/XYZTtoXYZLinkI.cpp generated/XYI.cpp generated/LinkI.cpp generated/RoiSetI.cpp generated/DimensionOrderI.cpp generated/XYZCtoXYCLinkI.cpp

#libOMERO_common_a_LDFLAGS = -no-undefined -version-info 0:0:0

libOMERO_client_la_SOURCES= ./OMERO/client.cpp ./OMERO/types.cpp
libOMERO_client_la_LIBADD = libOMERO_common.la
#libOMERO_client_a_LDFLAGS= -no-undefined -version-info 0:0:0

