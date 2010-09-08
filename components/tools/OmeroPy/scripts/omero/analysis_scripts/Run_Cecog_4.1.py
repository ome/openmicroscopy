#!/usr/bin/env python
"""
   Launcher script for CeCog/OMERO-integration.

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""


#
# Temporary settings for launching the demo. A more generalized
# directory lookup needs to be found.
#
# PYTHONPATH=.:lib/python2.6/ ../MacOS/python lib/python2.6/cecog/batch/batch.py -s conf
CECOG_DIR = "/opt/CecogAnalyzer.app/Contents/Resources/"
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
            optional = False,
            description = "ID of a CeCog configuration file"),
        version = "4.2.1",
        contact = "ome-users@lists.openmicroscopy.org.uk",
        description = """Executes CeCog via the batch interface.""")
    return client


def download(client, img_id, cf_id):
    """
    Downloads the binary data as CeCog is
    expecting it, as well as the settings file.
    """
    import os
    import omero
    import fileinput

    # from omero.util.script_utils import split_image
    # NYI: split_image(client, img_id, dir="")

    cwd = os.getcwd()
    conf = os.path.sep.join([cwd, "conf"])
    data = os.path.sep.join([cwd, "Data"])
    din  = os.path.sep.join([data, "In"])
    dout = os.path.sep.join([data, "Out"])
    pos  = os.path.sep.join([din, "Positions"])
    os.makedirs(din)
    os.makedirs(dout)
    os.makedirs(pos)

    client.download(omero.model.OriginalFileI(cf_id, False), filename=conf)
    for line in fileinput.input([conf], inplace=True):
        if line.startswith("pathin"):
            #print "pathin = %s" % din
            print "pathin = /Users/moore/Downloads/CecogPackage/Data/Demo_data/"
        elif line.startswith("pathout"):
            print "pathin = %s" % dout
        elif line.startswith("positions"):
            #print "positions = %s" % pos
            print "positions = 0037"
        else:
            print line,

    return conf


def execute(conf):
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
    rc = popen.poll()
    if rc:
        raise exceptions.Exception("cecog exited with rc=%s" % rc)


if __name__ == "__main__":
    client = setup()
    inputs = client.getInputs(True)
    ds_id = inputs["Image_ID"]
    cf_id = inputs["Settings_ID"]
    conf = download(client, ds_id, cf_id)
    execute(conf)
