# -*- coding: utf-8 -*-

# Copyright (C) 2015 Glencoe Software, Inc.
# All rights reserved.
#
# Use is subject to license terms supplied in LICENSE.txt

"""
   gateway tests - Testing the gateway plate wrapper

   pytest fixtures used as defined in conftest.py:
   - gatewaywrapper

"""

import pytest

from omero.model import PlateI, WellI, WellSampleI, ImageI
from omero.rtypes import rstring, rint, rtime
from uuid import uuid4


def uuid():
    return str(uuid4())


@pytest.fixture()
def plate(request, gatewaywrapper):
    """Creates a Plate."""
    gatewaywrapper.loginAsAuthor()
    gw = gatewaywrapper.gateway
    update_service = gw.getUpdateService()
    plate = PlateI()
    plate.name = rstring(uuid())
    for well_index in range(3):
        well = WellI()
        well.row = rint(well_index**2)
        well.column = rint(well_index**3)
        for well_sample_index in range(2):
            well_sample = WellSampleI()
            image = ImageI()
            image.name = rstring('%s_%d' % (uuid(), well_sample_index))
            image.acquisitionDate = rtime(0)
            well_sample.image = image
            well.addWellSample(well_sample)
        plate.addWell(well)
    plate_id, = update_service.saveAndReturnIds([plate])
    return gw.getObject('Plate', plate_id)


class TestPlateWrapper(object):

    def testGetGridSize(self, gatewaywrapper, plate):
        assert plate.getGridSize() == {'rows': 5L, 'columns': 9L}
