# -*- coding: utf-8 -*-

# Copyright (C) 2015 Glencoe Software, Inc.
# All rights reserved.
#
# Use is subject to license terms supplied in LICENSE.txt

"""
   gateway tests - Testing the gateway plate and plate acquisition wrappers

   pytest fixtures used as defined in conftest.py:
   - gatewaywrapper

"""

from datetime import datetime
import pytest

from omero.model import PlateAcquisitionI, PlateI, WellI, WellSampleI, ImageI
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

    plateacq = PlateAcquisitionI()
    plateacq.name = rstring(uuid())
    plateacq.plate = plate
    plateacq.startTime = rtime(1436227200L * 1000)

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
            well_sample.plateAcquisition = plateacq
        plate.addWell(well)
    plateacq = update_service.saveAndReturnObject(plateacq)
    plate_id = plateacq.plate.id.val
    return gw.getObject('Plate', plate_id)


class TestPlateWrapper(object):

    def testGetGridSize(self, gatewaywrapper, plate):
        assert plate.getGridSize() == {'rows': 5L, 'columns': 9L}


class TestPlateAcquistionWrapper(object):

    def testListPlateAcquisitions(self, gatewaywrapper, plate):
        acqs = list(plate.listPlateAcquisitions())
        assert len(acqs) == 1

    def testGetDate(self, gatewaywrapper, plate):
        acq = list(plate.listPlateAcquisitions())[0]
        assert acq.getStartTime() == 1436227200L * 1000
        # Note OMERO currently uses localtime not utctime
        assert acq.getDate() == datetime.fromtimestamp(1436227200L)

    def testGetDateDefault(self, gatewaywrapper):
        gatewaywrapper.loginAsAuthor()
        gw = gatewaywrapper.gateway
        update_service = gw.getUpdateService()
        plate = PlateI()
        plate.name = rstring(uuid())

        plateacq = PlateAcquisitionI()
        plateacq.plate = plate

        plateacq = update_service.saveAndReturnObject(plateacq)
        plate_id = plateacq.plate.id.val
        return gw.getObject('Plate', plate_id)

        acq = list(plate.listPlateAcquisitions())[0]
        assert acq.getStartTime() > 0
