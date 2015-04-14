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
def client(request, itest):
    """Returns a new user client."""
    return itest.new_client()


@pytest.fixture(scope='function')
def conn(request, client):
    """Returns a new OMERO gateway."""
    return BlitzGateway(client_obj=client)


@pytest.fixture(scope='function')
def update_service(request, client):
    """Returns a new OMERO update service."""
    return client.getSession().getUpdateService()


@pytest.fixture(scope='function')
def plate_wells(request, itest, update_service):
    """
    Returns a new OMERO Plate, linked Wells, linked WellSamples, and linked
    Images populated by an L{test.integration.library.ITest} instance.
    """
    plate = PlateI()
    plate.name = rstring(itest.uuid())
    # Well A10 (will have two WellSamples)
    well_a = WellI()
    well_a.row = rint(0)
    well_a.column = rint(9)
    # Well A11 (will not have a WellSample)
    well_b = WellI()
    well_b.row = rint(0)
    well_b.column = rint(10)
    # Well D3 (will have one WellSample)
    well_c = WellI()
    well_c.row = rint(3)
    well_c.column = rint(2)
    ws_a = WellSampleI()
    image_a = itest.new_image(name=itest.uuid())
    ws_a.image = image_a
    ws_b = WellSampleI()
    image_b = itest.new_image(name=itest.uuid())
    ws_b.image = image_b
    ws_c = WellSampleI()
    image_c = itest.new_image(name=itest.uuid())
    ws_c.image = image_c
    well_a.addWellSample(ws_a)
    well_a.addWellSample(ws_b)
    well_c.addWellSample(ws_c)
    plate.addWell(well_a)
    plate.addWell(well_b)
    plate.addWell(well_c)
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
        xtra = {'key': 'value'}
        plateGrid = PlateGrid(conn, plate_wells.id.val, 0, xtra)
        assert plateGrid
        assert plateGrid.plate.id == plate_wells.id.val
        assert plateGrid.field == 0
        assert plateGrid.xtra == xtra

    def test_metadata_grid_size(self, plate_wells, conn):
        """
        Check that the grid represented in the metadata is the correct size
        """
        plateGrid = PlateGrid(conn, plate_wells.id.val, 0, {})
        assert len(plateGrid.metadata['grid']) == 8
        assert len(plateGrid.metadata['grid'][0]) == 12

    def test_metadata_thumbnail_url(self, plate_wells, conn):
        """
        Check that extra elements of the thumbnail URL passed in the `xtra`
        dictionary are properly prepended
        """
        xtra = {'thumbUrlPrefix': 'foo/bar/'}
        plateGrid = PlateGrid(conn, plate_wells.id.val, 0, xtra)
        metadata = plateGrid.metadata
        for well in plate_wells.copyWells():
            well_metadata = metadata['grid'][well.row.val][well.column.val]
            if well_metadata:
                assert well_metadata['thumb_url'].startswith('foo/bar/')
