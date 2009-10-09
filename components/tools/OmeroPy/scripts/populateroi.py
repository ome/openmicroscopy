"""
 components/tools/OmeroPy/scripts/populateroi.py

Uses the omero.util.populate_roi functionality to parse all the
measurement files attached to a plate, and generate server-side
rois.

params:
	plate_id: id of the plate which should be parsed.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import omero.scripts as scripts
from omero.util.populate_roi import *

client = scripts.client('populateroi',\
    'Generates regions of interest from the measurement files associated with a plate',\
    scripts.Long("plate_id"))

factory = PlateAnalysisCtxFactory(client.getSession())
analysis_ctx = factory.get_analysis_ctx(client.getInput("plate_id").val)
n_measurements = analysis_ctx.get_measurement_count()
for i in range(n_measurements):
    measurement_ctx = analysis_ctx.get_measurement_ctx(i)
    measurement_ctx.parse_and_populate()
