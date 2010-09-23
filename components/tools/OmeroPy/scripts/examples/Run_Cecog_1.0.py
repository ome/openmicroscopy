#!/usr/bin/env python
"""
   Launcher script for CeCog/OMERO-integration.

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
import re
import sys

SCRIPT_NAME = "Run_Cecog_1.0.py"

DEMO_CONF="""
[General]
pathin = Data/Demo_data
pathout = Data/Demo_output
namingscheme = MetaMorph_PlateScanPackage
# Modified
constrain_positions = False
positions = 0037
redofailedonly = False
framerange = False
framerange_begin = 100
framerange_end = 200
frameincrement = 1
createimagecontainer = True
preferimagecontainer = False
binningfactor = 1
timelapsedata = True
qualitycontrol = False
debugmode = False
createimages = True
imageoutcompression = 98
rendering = {'primary_contours': {'rfp': {'raw': ('#FFFFFF', 1.0), 'contours': {'primary': ('#FF0000', 1, True)}}}}
rendering_class = {'primary_classification': {'rfp': {'raw': ('#FFFFFF', 1.0), 'contours': [('primary', 'class_label', 1, False)]}}}
primary_featureextraction_exportfeaturenames = ['n2_avg', 'n2_stddev']
secondary_featureextraction_exportfeaturenames = ['n2_avg', 'n2_stddev']

[ObjectDetection]
primary_channelid = rfp
primary_normalizemin = 0
primary_normalizemax = 0
primary_zslice_selection = True
primary_zslice_selection_slice = 1
primary_zslice_projection = False
primary_zslice_projection_method = maximum
primary_zslice_projection_begin = 1
primary_zslice_projection_end = 1
primary_zslice_projection_step = 1
primary_medianradius = 2
primary_latwindowsize = 42
primary_latlimit = 3
primary_lat2 = True
primary_latwindowsize2 = 150
primary_latlimit2 = 10
# Faster demo
primary_shapewatershed = False
primary_shapewatershed_gausssize = 10
primary_shapewatershed_maximasize = 24
primary_shapewatershed_minmergesize = 300
primary_intensitywatershed = False
primary_intensitywatershed_gausssize = 5
primary_intensitywatershed_maximasize = 11
primary_intensitywatershed_minmergesize = 95
primary_postprocessing = True
primary_postprocessing_roisize_min = 100
primary_postprocessing_roisize_max = -1
primary_postprocessing_intensity_min = 7
primary_postprocessing_intensity_max = -1
primary_removeborderobjects = True
primary_regions = ['primary']
primary_emptyimagemax = 90
secondary_channelid = gfp
secondary_normalizemin = 0
secondary_normalizemax = 0
secondary_channelregistration_x = 0
secondary_channelregistration_y = 0
secondary_zslice_selection = True
secondary_zslice_selection_slice = 1
secondary_zslice_projection = False
secondary_zslice_projection_method = maximum
secondary_zslice_projection_begin = 1
secondary_zslice_projection_end = 1
secondary_zslice_projection_step = 1
secondary_regions_expanded = True
secondary_regions_expanded_expansionsize = 10
secondary_regions_inside = False
secondary_regions_inside_shrinkingsize = 0
secondary_regions_outside = False
secondary_regions_outside_expansionsize = 0
secondary_regions_outside_separationsize = 0
secondary_regions_rim = False
secondary_regions_rim_expansionsize = 0
secondary_regions_rim_shrinkingsize = 0

[Classification]
primary_classification_envpath = %(CECOG_PKG)sData/Classifier/H2B
primary_simplefeatures_texture = True
primary_simplefeatures_shape = True
primary_classification_regionname = primary
secondary_classification_envpath = %(CECOG_PKG)sData/Classifier/aTubulin
secondary_simplefeatures_texture = True
secondary_simplefeatures_shape = False
secondary_classification_regionname = expanded
collectsamples = False
collectsamples_prefix =
primary_classification_annotationfileext = .xml
secondary_classification_annotationfileext = .xml

[Tracking]
tracking_maxobjectdistance = 36
tracking_maxtrackinggap = 5
tracking_maxsplitobjects = 2
tracking_labeltransitions = (2,3)
tracking_backwardrange = 2
tracking_forwardrange = 2
tracking_backwardlabels = 1,2,9
tracking_forwardlabels = 3,4,5,6,7
tracking_backwardcheck = 2
tracking_forwardcheck = 2
tracking_backwardrange_min = False
tracking_forwardrange_min = False
# Modified
tracking_visualization = False
tracking_visualize_track_length = -1
tracking_centroid_radius = 3
tracking_maxindegree = 1
tracking_maxoutdegree = 2
tracking_exporttrackfeatures = True
tracking_compressiontrackfeatures = raw

[ErrorCorrection]
filename_to_r =
constrain_graph = False
primary_graph = %(CECOG_PKG)sData/Cecog_settings/graph_primary.txt
secondary_graph = %(CECOG_PKG)sData/Cecog_settings/graph_secondary.txt
position_labels = False
mappingfile = %(CECOG_PKG)sData/Cecog_settings/position_labels.txt
groupby_position = True
groupby_oligoid = False
groupby_genesymbol = False
timelapse = 4.5
max_time = 100.0
primary_sort = NULL
secondary_sort = NULL

[Output]
rendering_labels_discwrite = False
rendering_contours_discwrite = True
rendering_contours_showids = True
rendering_class_discwrite = True
rendering_class_showids = True
export_object_counts = True
export_object_details = True
export_track_data = True

[Processing]
# Values for demo
tracking = False
secondary_errorcorrection = False
secondary_processchannel = True
primary_errorcorrection = False
primary_classification = False
tracking_synchronize_trajectories = False
secondary_classification = False

[Cluster]
primary_classification = False
tracking = False
tracking_synchronize_trajectories = False
primary_errorcorrection = False
secondary_processchannel = False
secondary_classification = False
secondary_errorcorrection = False
"""

#
# Temporary settings for launching the demo. A more generalized
# directory lookup needs to be found.
#
# PYTHONPATH=.:lib/python2.6/ ../MacOS/python lib/python2.6/cecog/batch/batch.py -s conf
CECOG_PKG = "/opt/CecogPackage/"
CECOG_DIR = CECOG_PKG + "CecogAnalyzer.app/Contents/Resources/"
CECOG_PARAMS = ["time", "../MacOS/python", "lib/python2.6/cecog/batch/batch.py", "-s"]
CECOG_PYTHONPATH = ".:lib/python2.6"

POS_REGEX = re.compile(".*?_P(\d+)")
PSC_REGEX = re.compile("^\s*(primary|secondary)_classification_envpath\s*=\s*(.*?)\s*$")

class Dirs(object):
    """
    Utility for tracking the package layout for cecog integration.
    """

    def __init__(self):
        self.cwd = os.getcwd()
        self.conf = os.path.sep.join([self.cwd, "conf"])
        self.data = os.path.sep.join([self.cwd, "Data"])
        self.din  = os.path.sep.join([self.data, "In"])
        self.dout = os.path.sep.join([self.data, "Out"])
        self.pos = os.path.sep.join([self.data, "Pos"])
        os.makedirs(self.din)
        os.makedirs(self.dout)
        os.makedirs(self.pos)

    def setPosition(self, pos_name):
        self.pos_name = pos_name
        newpos  = os.path.sep.join([self.din, self.pos_name])
        os.rename(str(self.pos), str(newpos))
        self.pos = newpos
        for x in ("primary_contours", "primary_classification"):
            setattr(self, x, os.path.sep.join([self.dout, "analyzed", "%0s" % pos_name, "images", x]))
        self.statistics = os.path.sep.join([self.dout, "analyzed", "%0s" % pos_name, "statistics"])


def setup():
    """
    Defines the OMERO.scripts parameters and
    returns the created client object.
    """
    import omero.scripts as scripts
    client = scripts.client(SCRIPT_NAME,
        scripts.Long(
            "Image_ID",
            optional = False,
            description = "ID of a valid image",
            grouping = "1"),
        scripts.Long(
            "Dataset_ID",
            optional = True,
            description = "ID of a dataset to which output images should be added. If not provided,"+\
"the latest dataset which the image is contained in will be used. If the image is not in a dataset, one will be created.",
            grouping = "2"),
        scripts.Long(
            "Settings_ID",
            optional = True,
            description = "ID of a CeCog configuration file. If not provided, a default configuration will be used.",
            grouping = "3"),
        scripts.Bool(
            "Debug",
            optional = False,
            default = False,
            description = "Whether or not to print debugging text",
            grouping = "4"),
        version = "4.2.1",
        contact = "ome-users@lists.openmicroscopy.org.uk",
        description = """Executes CeCog via the batch interface.""")
    return client


def download(client, img_id, cfg_id, debug=False):
    """
    Downloads the binary data as CeCog is
    expecting it, as well as the settings file.
    """
    import omero
    import fileinput

    dirs = Dirs()
    conf = dirs.conf
    dout = dirs.dout
    pos  = dirs.pos

    from omero.util.script_utils import split_image
    split_image(client, img_id, pos)

    # Find, and set the position name
    params = omero.sys.ParametersI()
    params.addId(img_id)
    image_name = client.sf.getQueryService().projection("select i.name from Image i where i.id = :id", params)[0][0].val
    match = POS_REGEX.match(image_name)
    pos_name = match.group(1)
    dirs.setPosition(pos_name)


    if cfg_id is not None:
        client.download(omero.model.OriginalFileI(cfg_id, False), filename=conf)
    else:
        print "Using default settings file"
        f = open(conf, "w")
        f.write(DEMO_CONF % {"CECOG_PKG": CECOG_PKG})
        f.close()

    if debug:
        f = open(conf, "r")
        print f.read()
        f.close()

    for line in fileinput.input([conf], inplace=True):

        # Appending CecogPackage's location
        match = PSC_REGEX.match(line)
        if match:
            print "%s_classification_envpath = %s" % (match.group(1), os.path.sep.join([CECOG_PKG, match.group(2)])),
        # Full absolute path changes
        elif line.startswith("pathin"):
            print "pathin = %s" % dirs.din
        elif line.startswith("pathout"):
            print "pathout = %s" % dirs.dout
        elif line.startswith("positions"):
            print "positions = %s" % dirs.pos
        else:
            print line,

    return dirs


def execute(dirs, debug=False):
    """
    Launches CeCog using the given configuration file.
    """
    import sys
    import exceptions
    from subprocess import Popen
    popen = Popen(
        CECOG_PARAMS + [dirs.conf],
        cwd=CECOG_DIR,
        env={"PYTHONPATH": CECOG_PYTHONPATH},
        stdout=sys.stdout,
        stderr=sys.stderr)
    popen.wait()

    #
    # DEBUGGING
    #
    if debug:
        os.system("ls -lR")

    rc = popen.poll()
    if rc:
        raise exceptions.Exception("cecog exited with rc=%s" % rc)


def upload(client, img_id, ds_id, dirs):
    """
    Uploads the images in the dout folder to OMERO, creating a new Project / Dataset.
    """
    from path import path

    import omero
    from omero.rtypes import rstring, unwrap
    from omero.util.script_utils import uploadDirAsImages, uploadCecogObjectDetails

    sf = client.getSession()
    queryService = sf.getQueryService()
    updateService = sf.getUpdateService()
    pixelsService = sf.getPixelsService()

    if ds_id is None:
        params = omero.sys.ParametersI()
        params.page(0, 1)
        params.addId(img_id)
        ds = queryService.findByQuery("select l.parent from DatasetImageLink l where l.child.id = :id order by l.details.updateEvent.id desc", params)
        if ds is None:
            ds = omero.model.DatasetI()
            ds.linkImage(omero.model.ImageI(img_id, False))
            ds = updateService.saveAndReturnObject(ds)
        ds_id = ds.id.val
    dataset = omero.model.DatasetI(ds_id, False)
    print "Linking images to dataset %s" % ds_id

    # upload segmentation and classification jpegs as new movies
    newImageIds = []
    print "Uploading images from %s" % dirs.primary_contours
    newImageId = uploadDirAsImages(sf, queryService, updateService, pixelsService, dirs.primary_contours, dataset)
    newImageIds.append(unwrap(newImageId))

    print "Uploading images from %s" % dirs.primary_classification
    newImageId = uploadDirAsImages(sf, queryService, updateService, pixelsService, dirs.primary_classification, dataset)
    newImageIds.append(unwrap(newImageId))

    for newImageId in newImageIds:
        newImage = queryService.get("Image", newImageId)
        val = newImage.description and ("%s\n\n" % newImage.description.val) or ""
        newImage.description = rstring("%sCreated from Image ID: %s by %s" % (val, img_id, SCRIPT_NAME))
        updateService.saveObject(newImage)

    import time
    start = time.time()

    stats = path(dirs.statistics)
    details = stats.glob("*_object_details.txt")

    for file in details:
        print "Saving rois from %s..." % file
        uploadCecogObjectDetails(updateService, img_id, file)
        of = client.upload(filename=file)
        fa = omero.model.FileAnnotationI()
        fa.setFile(of)
        link = omero.model.ImageAnnotationLinkI()
        link.link(omero.model.ImageI(img_id, False), fa)
        updateService.saveObject(link)

    stop = time.time()
    print "Saving rois took %s secs." % (stop-start)


if __name__ == "__main__":
    client = setup()
    inputs = client.getInputs(True)
    debug = inputs["Debug"]
    img_id = inputs["Image_ID"]
    ds_id = inputs.get("Dataset_ID", None)
    cfg_id = inputs.get("Settings_ID", None)
    dirs = download(client, img_id, cfg_id, debug)
    execute(dirs, debug)
    upload(client, img_id, ds_id, dirs)
