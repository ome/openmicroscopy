#!/usr/bin/env python
"""
   Launcher script for CeCog/OMERO-integration.

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

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
primary_shapewatershed = True
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
primary_classification_envpath = Data/Classifier/H2B
primary_simplefeatures_texture = True
primary_simplefeatures_shape = True
primary_classification_regionname = primary
secondary_classification_envpath = Data/Classifier/aTubulin
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
primary_graph = Data/Cecog_settings/graph_primary.txt
secondary_graph = Data/Cecog_settings/graph_secondary.txt
position_labels = False
mappingfile = Data/Cecog_settings/position_labels.txt
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
primary_classification = True
tracking = True
tracking_synchronize_trajectories = True
primary_errorcorrection = False
secondary_processchannel = True
secondary_classification = True
secondary_errorcorrection = False

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
CECOG_PARAMS = ["../MacOS/python", "lib/python2.6/cecog/batch/batch.py", "-s"]
CECOG_PYTHONPATH = ".:lib/python2.6"


def setup():
    """
    Defines the OMERO.scripts parameters and
    returns the created client object.
    """
    import omero.scripts as scripts
    client = scripts.client('Run_Cecog_4.1.py',
        scripts.Long(
            "Image_ID",
            optional = False,
            description = "ID of a valid dataset"),
        scripts.Long(
            "Settings_ID",
            optional = True,
            description = "ID of a CeCog configuration file. If not provided, a default value will be used."),
        scripts.Bool(
            "Debug",
            optional = False,
            default = False,
            description = "Whether or not to print debugging text"),
        version = "4.2.1",
        contact = "ome-users@lists.openmicroscopy.org.uk",
        description = """Executes CeCog via the batch interface.""")
    return client


def download(client, img_id, cf_id):
    """
    Downloads the binary data as CeCog is
    expecting it, as well as the settings file.
    """
    import re
    import os
    import omero
    import fileinput

    cwd = os.getcwd()
    conf = os.path.sep.join([cwd, "conf"])
    data = os.path.sep.join([cwd, "Data"])
    din  = os.path.sep.join([data, "In"])
    dout = os.path.sep.join([data, "Out"])
    pos  = os.path.sep.join([din, "0037"])
    os.makedirs(din)
    os.makedirs(dout)
    os.makedirs(pos)

    from omero.util.script_utils import split_image
    split_image(client, img_id, pos)
    params = omero.sys.ParametersI()
    params.addId(img_id)
    image_name = client.sf.getQueryService().projection("select i.name from Image i where i.id = :id", params)[0][0]

    if cf_id is not None:
        client.download(omero.model.OriginalFileI(cf_id, False), filename=conf)
    else:
        print "Using default settings file"
        f = open(conf, "w")
        f.write(DEMO_CONF)
        f.close()
    for line in fileinput.input([conf], inplace=True):
        if line.startswith("pathin"):
            print "pathin = %s" % din
            #print "pathin = /opt/CecogPackage/Data/Demo_data/"
        elif line.startswith("pathout"):
            print "pathout = %s" % dout
        elif line.startswith("positions"):
            print "positions = %s" % pos
        elif line.startswith("primary_classification_envpath"):
            # FIXME
            print "primary_classification_envpath = %s/Data/Classifier/H2B" % CECOG_PKG
        elif line.startswith("secondary_classification_envpath"):
            # FIXME
            print "secondary_classification_envpath = %s/Data/Classifier/aTubulin" % CECOG_PKG
        else:
            print line,

    return (conf, dout)


def execute(conf, debug=False):
    """
    Launches CeCog using the given configuration file.
    """
    import sys
    import exceptions
    from subprocess import Popen
    popen = Popen(
        CECOG_PARAMS + [conf],
        cwd=CECOG_DIR,
        env={"PYTHONPATH": CECOG_PYTHONPATH},
        stdout=sys.stdout,
        stderr=sys.stderr)
    popen.wait()

    #
    # DEBUGGING
    #
    if debug:
        import os
        cwd = os.getcwd()
        conf = os.path.sep.join([cwd, "conf"])
        data = os.path.sep.join([cwd, "Data"])
        din  = os.path.sep.join([data, "In"])
        dout = os.path.sep.join([data, "Out"])
        from path import path
        p = path(dout)
        os.system("ls -lR")

    rc = popen.poll()
    if rc:
        raise exceptions.Exception("cecog exited with rc=%s" % rc)


def upload(client, dout):
    """
    Uploads the images in the dout folder to OMERO, creating a new Project / Dataset. 
    """
    from omero.util.script_utils import uploadDirAsImages
    sf = client.getSession()
    queryService = sf.getQueryService()
    updateService = sf.getUpdateService()
    pixelsService = sf.getPixelsService()
    
    # create dataset
    import omero.model
    from omero.rtypes import rstring
    dataset = omero.model.DatasetI()
    dataset.name = rstring("Cecog-Analysis-Results")
    dataset = updateService.saveAndReturnObject(dataset)
    
    # upload segmentation and classification jpegs as new movies
    import os
    contoursDir = os.path.join(dout, "analyzed", "0037", "images", "primary_contours")
    print "Uploading images from %s" % contoursDir
    uploadDirAsImages(sf, queryService, updateService, pixelsService, contoursDir, dataset)
    
    classDir = os.path.join(dout, "analyzed", "0037", "images", "primary_classification")
    print "Uploading images from %s" % classDir
    uploadDirAsImages(sf, queryService, updateService, pixelsService, classDir, dataset)


if __name__ == "__main__":
    client = setup()
    inputs = client.getInputs(True)
    debug = inputs["Debug"]
    ds_id = inputs["Image_ID"]
    cf_id = inputs.get("Settings_ID", None)
    conf, dout = download(client, ds_id, cf_id)
    execute(conf, debug)
    upload(client, dout)
