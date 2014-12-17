#!/usr/bin/env python
# -*- coding: utf-8 -*-
import omero
from omero.rtypes import rint
from omero_sys_ParametersI import ParametersI  # Temporary

c = omero.client()
s = c.createSession()
q = s.getQueryService()

GET_IMAGES_WITH_PLATES = (
    "select i from Image i join i.wellSamples ws join ws.well w"
    " join w.plate p")  # Inner joins
GET_PLATE_FROM_IMAGE_ID = (
    "select p from Plate p join p.wells w join w.wellSamples ws"
    " join ws.image i where i.id = :id")

filter = omero.sys.Filter()
filter.limit = rint(100)
filter.offset = rint(0)
params = ParametersI()
params.theFilter = filter

images = q.findAllByQuery(GET_IMAGES_WITH_PLATES, params)
print "Found %s images" % len(images)

for image in images:

    params = ParametersI()
    params.addId(image.getId().getValue())
    # Multiple plates per image will through an exception
    plate = q.findByQuery(GET_PLATE_FROM_IMAGE_ID, params)
    print 'Image %s belongs to Plate %s (%s)' % (
        image.getId().getValue(), plate.getId().getValue(),
        plate.getName().getValue())
