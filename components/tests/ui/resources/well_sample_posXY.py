
"""
Script used by robot_setup.py to set posX and posY on Well Samples.

Usage:
$ python well_sample_posXY.py host port sessionKey plateId

Used after import of SPW data to provide spatial settings on well
samples. Tested by spw_test.
"""

import argparse
import omero
from omero.gateway import BlitzGateway
from omero.model.enums import UnitsLength
parser = argparse.ArgumentParser()
parser.add_argument('host', help='OMERO host')
parser.add_argument('port', help='OMERO port')
parser.add_argument('key', help='OMERO session key')
parser.add_argument('plateId', help='Plate ID to process', type=int)
args = parser.parse_args()
conn = BlitzGateway(host=args.host, port=args.port)
conn.connect(args.key)
update = conn.getUpdateService()
plate = conn.getObject('Plate', args.plateId)
r = UnitsLength.REFERENCEFRAME
cols = 3
for well in plate.listChildren():
    for i, ws in enumerate(well.listChildren()):
        x = i % cols
        y = i / cols
        ws = conn.getQueryService().get('WellSample', ws.id)
        ws.posY = omero.model.LengthI(y, r)
        ws.posX = omero.model.LengthI(x, r)
        update.saveObject(ws)
