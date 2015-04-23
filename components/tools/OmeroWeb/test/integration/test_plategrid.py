# -*- coding: utf-8 -*-

# Copyright (C) 2015 Glencoe Software, Inc.
# All rights reserved.
#
# Use is subject to license terms supplied in LICENSE.txt

"""
   Integration tests for the "plategrid" module.
"""

import pytest
import test.integration.library as lib

from omero.model import PlateI, WellI, WellSampleI
from omero.rtypes import rint, rstring
from omero.gateway import BlitzGateway
from omeroweb.webgateway.plategrid import PlateGrid

from django.test import Client
from django.core.urlresolvers import reverse
import json


@pytest.fixture(scope='function')
def itest(request):
    """
    Returns a new L{test.integration.library.ITest} instance. With attached
    finalizer so that pytest will clean it up.
    """
    o = lib.ITest()
    o.setup_method(None)

    def finalizer():
        o.teardown_method(None)
    request.addfinalizer(finalizer)
    return o


@pytest.fixture(scope='function')
def client(itest):
    """Returns a new user client."""
    return itest.new_client()


@pytest.fixture(scope='function')
def conn(client):
    """Returns a new OMERO gateway."""
    return BlitzGateway(client_obj=client)


@pytest.fixture(scope='function')
def update_service(client):
    """Returns a new OMERO update service."""
    return client.getSession().getUpdateService()


@pytest.fixture(scope='function')
def well_sample_factory(itest):

    def make_well_sample():
        ws = WellSampleI()
        image = itest.new_image(name=itest.uuid())
        ws.image = image
        return ws
    return make_well_sample


@pytest.fixture(scope='function')
def well_factory(well_sample_factory):

    def make_well(ws_count=0):
        well = WellI()
        for _ in range(ws_count):
            well.addWellSample(well_sample_factory())
        return well
    return make_well


@pytest.fixture(scope='function')
def well_grid_factory(well_factory):

    def make_well_grid(grid_layout={}):
        wells = []
        for row, column in grid_layout:
            ws_count = grid_layout[(row, column)]
            well = well_factory(ws_count)
            well.row = rint(row)
            well.column = rint(column)
            wells.append(well)
        return wells
    return make_well_grid


@pytest.fixture(scope='function')
def plate_wells(itest, well_grid_factory, update_service):
    """
    Returns a new OMERO Plate, linked Wells, linked WellSamples, and linked
    Images populated by an L{test.integration.library.ITest} instance.
    """
    plate = PlateI()
    plate.name = rstring(itest.uuid())
    # Well A10 has two WellSamples
    # Well A11 has no WellSamples
    # Well D3 has one WellSample
    wells = well_grid_factory({(0, 9): 2, (0, 10): 0, (3, 2): 1})
    for well in wells:
        plate.addWell(well)
    return update_service.saveAndReturnObject(plate)


@pytest.fixture(scope='function')
def full_plate_wells(itest, update_service):
    """
    Returns a full OMERO Plate, linked Wells, linked WellSamples, and linked
    Images populated by an L{test.integration.library.ITest} instance.
    """
    lett = map(chr, range(ord('A'), ord('Z')+1))
    plate = PlateI()
    plate.name = rstring(itest.uuid())
    for row in range(8):
        for column in range(12):
            well = WellI()
            well.row = rint(row)
            well.column = rint(column)
            ws = WellSampleI()
            image = itest.new_image(name=lett[row]+str(column))
            ws.image = image
            well.addWellSample(ws)
            plate.addWell(well)
    return update_service.saveAndReturnObject(plate)


@pytest.fixture(scope='function')
def django_client(request, client):
    """Returns a logged in Django test client."""
    django_client = Client()
    login_url = reverse('weblogin')

    response = django_client.get(login_url)
    assert response.status_code == 200

    data = {
        'server': 1,
        'username': client.getProperty('omero.user'),
        'password': client.getProperty('omero.pass'),
    }
    response = django_client.post(login_url, data)
    assert response.status_code == 302

    def finalizer():
        logout_url = reverse('weblogout')
        response = django_client.post(logout_url, data=data)
        assert response.status_code == 302
    request.addfinalizer(finalizer)
    return django_client


class TestPlateGrid(object):
    """
    Tests to ensure that the OMERO.web "plategrid" functionality works as
    expected.
    """

    def test_get_plate_grid_metadata(self, django_client, plate_wells, conn):
        """
        Do a simple GET request to retrieve the metadata for a plate in JSON
        form
        """
        for field in range(2):
            request_url = reverse('webgateway_plategrid_json',
                                  args=(plate_wells.id.val, field))

            response = django_client.get(request_url)
            assert response.status_code == 200

            plate_metadata = json.loads(response.content)
            assert len(plate_metadata['rowlabels']) == 8
            assert len(plate_metadata['collabels']) == 12

            grid = plate_metadata['grid']
            for well in plate_wells.copyWells():
                well_metadata = grid[well.row.val][well.column.val]
                well_samples = well.copyWellSamples()
                if len(well_samples) > field:
                    assert well_metadata['name'] ==\
                        well_samples[field].getImage().name.val

    def test_instantiation(self, plate_wells, conn):
        """
        Check that the helper object can be created
        """
        plate_grid = PlateGrid(conn, plate_wells.id.val, 0)
        assert plate_grid
        assert plate_grid.plate.id == plate_wells.id.val
        assert plate_grid.field == 0

    def test_metadata_grid_size(self, plate_wells, conn):
        """
        Check that the grid represented in the metadata is the correct size
        """
        plate_grid = PlateGrid(conn, plate_wells.id.val, 0)
        assert len(plate_grid.metadata['grid']) == 8
        assert len(plate_grid.metadata['grid'][0]) == 12

    def test_metadata_thumbnail_url(self, plate_wells, conn):
        """
        Check that extra elements of the thumbnail URL passed in the `xtra`
        dictionary are properly prepended
        """
        plate_grid = PlateGrid(conn, plate_wells.id.val, 0, 'foo/bar/')
        metadata = plate_grid.metadata
        for well in plate_wells.copyWells():
            well_metadata = metadata['grid'][well.row.val][well.column.val]
            if well_metadata:
                assert well_metadata['thumb_url'].startswith('foo/bar/')

    def test_full_grid(self, full_plate_wells, conn):
        """
        Check that all wells are assigned correctly even if the entire plate of
        wells is full
        """
        lett = map(chr, range(ord('A'), ord('Z')+1))
        plate_grid = PlateGrid(conn, full_plate_wells.id.val, 0)
        metadata = plate_grid.metadata
        for row in range(8):
            for column in range(12):
                assert metadata['grid'][row][column]['name'] ==\
                    lett[row]+str(column)
