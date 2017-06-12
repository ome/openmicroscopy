#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014-2017 University of Dundee & Open Microscopy Environment.
#                    All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
FOR TRAINING PURPOSES ONLY!
"""

import omero
from omero.gateway import BlitzGateway
from omero.rtypes import rdouble
from Parse_OMERO_Properties import USERNAME, PASSWORD, HOST, PORT

"""
start-code
"""

# Create a connection
# ===================
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()
queryService = conn.getQueryService()
updateService = conn.getUpdateService()


# Decide how many Shapes
# ======================
total_shape_count = 20


# Create ROI with Shapes
# ======================

roi = omero.model.RoiI()

for shape in range(total_shape_count):
    point = omero.model.PointI()
    point.setX(rdouble(2))
    point.setY(rdouble(3))
    roi.addShape(point)

roi = updateService.saveAndReturnObject(roi)
print "created {0} shapes".format(total_shape_count)


# Query Shapes in batches
# =======================
shapes_per_batch = 5
shapes_queried = 0
shape_batch_count = 0

hql = 'FROM Shape WHERE roi.id = :id ORDER BY id'

while True:
    params = omero.sys.ParametersI()
    params.addId(roi.id)
    params.page(shapes_queried, shapes_per_batch)
    shapes = queryService.findAllByQuery(hql, params)

    if not shapes:
        break
    else:
        shapes_queried += len(shapes)
        shape_batch_count += 1

print "retrieved {0} shapes in {1} batches".format(shapes_queried,
                                                   shape_batch_count)


# Delete ROI with Shapes
# ======================
conn.deleteObject(roi)


# Close connection
# ================
# When you are done, close the session to free up server resources.
conn.close()
