import omero
from omero.rtypes import *
from omero_sys_ParametersI import ParametersI # Temporary

c = omero.client();
s = c.createSession();
q = s.getQueryService();

LOAD_WELLS = 'select w from Well w join fetch w.wellSamples ws join fetch ws.image i where w.plate.id = :id'

filter = omero.sys.Filter();
filter.limit = rint(10)
filter.offset = rint(0)


plates = q.findAll("Plate", filter)
if len(plates) == 0:
    import sys
    sys.exit(0)
else:
    import random
    example_plate = random.choice(plates)
    print "Loading wells for Plate %s (%s)" % (example_plate.getId().getValue(), example_plate.getName().getValue())

# An example of true paging
filter.limit = rint(12)
params = ParametersI()
params.addId(example_plate.getId().getValue())
params.theFilter = filter

offset = 0
while True:

    wells = q.findAllByQuery(LOAD_WELLS, params)
    if len(wells) == 0:
        break
    else:
        offset += len(wells)
        params.theFilter.offset = rint( offset )

    for well in wells:
        id = well.getId().getValue()
        row = well.getRow().getValue()
        col = well.getColumn().getValue()
        images = []
        for ws in well.copyWellSamples():
            images.append( ws.getImage().getId().getValue() )
        print "Well %s (%2sx%2s) contains the images: %s" % (id, row, col, images)
