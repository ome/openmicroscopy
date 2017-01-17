# -*- coding: utf-8 -*-

# Copyright (C) 2015 Glencoe Software, Inc.
# All rights reserved.
#
# Use is subject to license terms supplied in LICENSE.txt

"""
   Integration tests for the "plategrid" module.
"""

import pytest
from omeroweb.testlib import IWebTest

from omero.model import PlateI, WellI, WellSampleI
from omero.model import FileAnnotationI, OriginalFileI, PlateAnnotationLinkI
from omero.model import LengthI
from omero.model.enums import UnitsLength
from omero.rtypes import rint, rstring, rtime
from omero.gateway import BlitzGateway
from omero.grid import WellColumn, StringColumn
from omeroweb.webgateway.plategrid import PlateGrid

from django.test import Client
from django.core.urlresolvers import reverse
from random import random
import json
import time


@pytest.fixture(scope='module')
def itest(request):
    """
    Returns a new L{omeroweb.testlib.IWebTest} instance. With attached
    finalizer so that pytest will clean it up.
    """
    class PlateGridIWebTest(IWebTest):
        """
        This class emulates py.test scoping semantics when the xunit style
        is in use.
        """
        pass
    PlateGridIWebTest.setup_class()

    def finalizer():
        PlateGridIWebTest.teardown_class()
    request.addfinalizer(finalizer)
    return PlateGridIWebTest()


@pytest.fixture()
def client(itest):
    """Returns a new user client."""
    return itest.new_client()


@pytest.fixture()
def conn(client):
    """Returns a new OMERO gateway."""
    return BlitzGateway(client_obj=client)


@pytest.fixture()
def update_service(client):
    """Returns a new OMERO update service."""
    return client.getSession().getUpdateService()


@pytest.fixture()
def well_sample_factory(itest):

    def make_well_sample(x_pos=None, y_pos=None):
        ws = WellSampleI()
        image = itest.new_image(name=itest.uuid())
        ws.image = image
        if x_pos is not None:
            ws.posX = LengthI(x_pos, UnitsLength.REFERENCEFRAME)
        if y_pos is not None:
            ws.posY = LengthI(y_pos, UnitsLength.REFERENCEFRAME)
        return ws
    return make_well_sample


@pytest.fixture()
def well_factory(well_sample_factory):

    def make_well(ws_count=0):
        well = WellI()
        for i in range(ws_count):
            well.addWellSample(well_sample_factory(i, i + 1))
        return well
    return make_well


@pytest.fixture()
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


@pytest.fixture()
def plate_wells(itest, well_grid_factory, update_service):
    """
    Returns a new OMERO Plate, linked Wells, linked WellSamples, and linked
    Images populated by an L{omeroweb.testlib.IWebTest} instance.
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


@pytest.fixture()
def full_plate_wells(itest, update_service):
    """
    Returns a full OMERO Plate, linked Wells, linked WellSamples, and linked
    Images populated by an L{omeroweb.testlib.IWebTest} instance.
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


@pytest.fixture()
def plate_wells_with_acq_date(itest, well_grid_factory, update_service):
    """
    Creates a plate with a single well containing an image with both an
    acquisition date set as well as a creation event with a date. Returns the
    plate and the acquisition date in a map.
    """
    acq_date = (time.time() - 60 * 60 * 24) * 1000
    plate = PlateI()
    plate.name = rstring(itest.uuid())
    # Simple grid: one well with one image
    [well] = well_grid_factory({(0, 0): 1})
    well.copyWellSamples()[0].image.acquisitionDate = rtime(int(acq_date))
    plate.addWell(well)
    plate = update_service.saveAndReturnObject(plate)
    return {'plate': plate,
            'acq_date': int(acq_date / 1000)}


@pytest.fixture()
def plate_wells_with_no_acq_date(itest, well_grid_factory, update_service,
                                 conn):
    """
    Creates a plate with a single well containing an image with no acquisition
    date set. Returns the plate and the time from the creation event in a map.
    """
    plate = PlateI()
    plate.name = rstring(itest.uuid())
    # Simple grid: one well with one image
    [well] = well_grid_factory({(0, 0): 1})
    plate.addWell(well)
    well.copyWellSamples()[0].image.acquisitionDate = None
    plate = update_service.saveAndReturnObject(plate)
    image = plate.copyWells()[0].copyWellSamples()[0].image
    creation_date = image.details.creationEvent.time
    return {'plate': plate,
            'creation_date': creation_date.val / 1000}


@pytest.fixture()
def plate_wells_with_description(itest, well_grid_factory, update_service):
    """
    Creates a plate with a single well containing an image with a description.
    Returns the plate and the description in a map.
    """
    description = "test description"
    plate = PlateI()
    plate.name = rstring(itest.uuid())
    # Simple grid: one well with one image
    [well] = well_grid_factory({(0, 0): 1})
    well.copyWellSamples()[0].image.description = rstring(description)
    plate.addWell(well)
    plate = update_service.saveAndReturnObject(plate)
    return {'plate': plate,
            'description': description}


@pytest.fixture()
def plate_well_table(itest, well_grid_factory, update_service, conn):
    """
    Returns a new OMERO Plate, linked Wells, linked WellSamples, and linked
    Images populated by an L{omeroweb.testlib.IWebTest} instance.
    """
    plate = PlateI()
    plate.name = rstring(itest.uuid())
    # Well A1 has one WellSample
    plate.addWell(well_grid_factory({(0, 0): 1})[0])
    plate = update_service.saveAndReturnObject(plate)

    col1 = WellColumn('Well', '', [])
    col2 = StringColumn('TestColumn', '', 64, [])

    columns = [col1, col2]
    tablename = "plate_well_table_test:%s" % str(random())
    table = conn.c.sf.sharedResources().newTable(1, tablename)
    table.initialize(columns)

    wellIds = [w.id.val for w in plate.copyWells()]
    print "WellIds", wellIds

    data1 = WellColumn('Well', '', wellIds)
    data2 = StringColumn('TestColumn', '', 64, ["foobar"])
    data = [data1, data2]
    table.addData(data)
    table.close()

    orig_file = table.getOriginalFile()
    fileAnn = FileAnnotationI()
    fileAnn.ns = rstring('openmicroscopy.org/omero/bulk_annotations')
    fileAnn.setFile(OriginalFileI(orig_file.id.val, False))
    fileAnn = conn.getUpdateService().saveAndReturnObject(fileAnn)
    link = PlateAnnotationLinkI()
    link.setParent(PlateI(plate.id.val, False))
    link.setChild(FileAnnotationI(fileAnn.id.val, False))
    update_service.saveAndReturnObject(link)
    return plate, wellIds


@pytest.fixture()
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
                    img = well_samples[field].getImage()
                    assert well_metadata['name'] == img.name.val
                    # expect default thumbnail (no size specified)
                    assert well_metadata['thumb_url'] ==\
                        reverse('webgateway.views.render_thumbnail',
                                args=[img.id.val])

    def test_well_images(self, django_client, plate_wells, conn):
        """
        Test listing of wellSamples/images in a Well
        """
        for well in plate_wells.copyWells():
            request_url = reverse('webgateway_listwellimages_json',
                                  args=[well.id.val])
            response = django_client.get(request_url)
            assert response.status_code == 200
            well_json = json.loads(response.content)
            rf = str(UnitsLength.REFERENCEFRAME)
            for i, ws in enumerate(well.copyWellSamples()):
                ws_json = well_json[i]
                img = ws.getImage()
                assert ws_json['name'] == img.name.val
                assert ws_json['id'] == img.id.val
                assert ws_json['thumb_url'] ==\
                    reverse('webgateway.views.render_thumbnail',
                            args=[img.id.val])
                assert ws_json['position'] == {'x': {'value': i,
                                               'unit': rf},
                                               'y': {'value': i + 1,
                                                     'unit': rf}
                                               }

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
                assert metadata['grid'][row][column]['description'] == ''

    def test_acquisition_date(self, plate_wells_with_acq_date, conn):
        """
        Check that acquisition date is used for an image in the plate if it is
        specified
        """
        plate = plate_wells_with_acq_date['plate']
        acq_date = plate_wells_with_acq_date['acq_date']
        plate_grid = PlateGrid(conn, plate.id.val, 0)
        metadata = plate_grid.metadata
        assert metadata['grid'][0][0]['date'] == acq_date

    def test_creation_date(self, plate_wells_with_no_acq_date, conn):
        """
        Check that plate grid metadata falls back to using creation event time
        if an image has no (or an invalid) acquistion date
        """
        plate = plate_wells_with_no_acq_date['plate']
        creation_date = plate_wells_with_no_acq_date['creation_date']
        plate_grid = PlateGrid(conn, plate.id.val, 0)
        metadata = plate_grid.metadata
        assert metadata['grid'][0][0]['date'] == creation_date

    def test_description(self, plate_wells_with_description, conn):
        """
        Check that an images description is included with the grid metadata
        """
        plate = plate_wells_with_description['plate']
        description = plate_wells_with_description['description']
        plate_grid = PlateGrid(conn, plate.id.val, 0)
        metadata = plate_grid.metadata
        assert metadata['grid'][0][0]['description'] == description


class TestScreenPlateTables(object):
    """
    Tests the retrieval of tabular data attached to Plate
    """

    def test_get_plate_table(self, django_client, plate_well_table, conn):
        """
        Do a simple GET request to query the metadata for a single well
        attached to the plate in JSON form
        """
        plate, wellIds = plate_well_table
        wellId = wellIds[0]
        # E.g. webgateway/table/Plate.wells/2061/query/?query=Well-2061
        request_url = reverse('webgateway_object_table_query',
                              args=("Plate.wells", wellId))
        response = django_client.get(request_url,
                                     data={'query': 'Well-%s' % wellId})
        rspJson = json.loads(response.content)
        print rspJson
        assert rspJson['data'] == {
            'rows': [[wellId, 'foobar']],
            'columns': ['Well', 'TestColumn']}
        assert rspJson['parentId'] == plate.id.val
        user = conn.getUser()
        userName = "%s %s" % (user.getFirstName(), user.getLastName())
        assert rspJson['addedBy'] == userName
        assert rspJson['owner'] == userName
        assert rspJson['parentType'] == "Plate"
