"""
components/tools/OmeroPy/scripts/omero/import_scripts/Populate_Plate_Roi.py

Uses the omero.util.populate_roi functionality to parse all the
measurement files attached to a plate, and generate server-side
rois.

params:
	Plate_ID: id of the plate which should be parsed.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import omero.scripts as scripts
from omero.util.populate_roi import *

client = scripts.client('Populate_ROI.py',
    scripts.Long("Plate_ID", optional = False,
        description = "ID of a valid plate with attached results files"),
    version = "4.2.0",
    contact = "ome-users@lists.openmicroscopy.org.uk",
    description = """Generates regions of interest from the measurement files associated with a plate

This script is executed by the server on initial import, and should typically not need
to be run by users.""")

factory = PlateAnalysisCtxFactory(client.getSession())
analysis_ctx = factory.get_analysis_ctx(client.getInput("Plate_ID").val)
n_measurements = analysis_ctx.get_measurement_count()
for i in range(n_measurements):
    measurement_ctx = analysis_ctx.get_measurement_ctx(i)
    measurement_ctx.parse_and_populate()
