#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Simple integration tests for the "show" module.

   Copyright 2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import omero
import omero.clients
import pytest
import test.integration.library as lib

from omero.gateway import BlitzGateway, ProjectWrapper, DatasetWrapper, \
    ImageWrapper, TagAnnotationWrapper, ScreenWrapper, PlateWrapper, \
    PlateAcquisitionWrapper
from omero.model import ProjectI, DatasetI, TagAnnotationI, ScreenI, PlateI, \
    WellI, WellSampleI, PlateAcquisitionI
from omero.rtypes import rstring, rint
from omeroweb.webclient.show import Show, IncorrectMenuError
from django.test.client import RequestFactory


def cmp_well_column(x, y):
    """Well column comparator."""
    return cmp(x.column.val, y.column.val)


@pytest.fixture(scope='module')
def path():
    """Returns the root OMERO.web webclient path."""
    return '/webclient'


@pytest.fixture(scope='module')
def request_factory():
    """Returns a fresh Django request factory."""
    return RequestFactory()


@pytest.fixture(scope='function')
def itest(request):
    """
    Returns a new L{test.integration.library.ITest} instance.  With
    attached finalizer so that pytest will clean it up.
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
def project(request, itest, update_service):
    """Returns a new OMERO Project with required fields set."""
    project = ProjectI()
    project.name = rstring(itest.uuid())
    return update_service.saveAndReturnObject(project)


@pytest.fixture(scope='function')
def projects(request, itest, update_service):
    """Returns 2 new OMERO Projects with required fields set."""
    project = ProjectI()
    project.name = rstring(itest.uuid())
    project = update_service.saveAndReturnObject(project)
    proj = ProjectI()
    proj.name = rstring(itest.uuid())
    proj = update_service.saveAndReturnObject(proj)
    return [project, proj]


@pytest.fixture(scope='function')
def project_dataset(request, itest, update_service):
    """
    Returns a new OMERO Project and linked Dataset with required fields set.
    """
    project = ProjectI()
    project.name = rstring(itest.uuid())
    dataset = DatasetI()
    dataset.name = rstring(itest.uuid())
    project.linkDataset(dataset)
    return update_service.saveAndReturnObject(project)


@pytest.fixture(scope='function')
def project_dataset_image(request, itest, update_service):
    """
    Returns a new OMERO Project, linked Dataset and linked Image populated
    by an L{test.integration.library.ITest} instance with required fields
    set.
    """
    project = ProjectI()
    project.name = rstring(itest.uuid())
    dataset = DatasetI()
    dataset.name = rstring(itest.uuid())
    image = itest.new_image(name=itest.uuid())
    dataset.linkImage(image)
    project.linkDataset(dataset)
    return update_service.saveAndReturnObject(project)


@pytest.fixture(scope='function', params=[1, 2])
def tag(request, itest, update_service):
    """Returns a new OMERO TagAnnotation with required fields set."""
    name = rstring(itest.uuid())
    for index in range(request.param):
        tag = TagAnnotationI()
        tag.textValue = name
        tag = update_service.saveAndReturnObject(tag)
    return tag


@pytest.fixture(scope='function')
def tagset_tag(request, itest, update_service):
    """
    Returns a new OMERO TagAnnotation, with the OMERO.insight tagset
    namespace set, and linked TagAnnotation with required fields set.
    """
    tagset = TagAnnotationI()
    tagset.ns = rstring(omero.constants.metadata.NSINSIGHTTAGSET)
    tagset.textValue = rstring(itest.uuid())
    tag = TagAnnotationI()
    tag.textValue = rstring(itest.uuid())
    tagset.linkAnnotation(tag)
    return update_service.saveAndReturnObject(tagset)


@pytest.fixture(scope='function')
def image(request, itest, update_service):
    """
    Returns a new OMERO Image populated by an
    L{test.integration.library.ITest} instance.
    """
    image = itest.new_image(name=itest.uuid())
    return update_service.saveAndReturnObject(image)


@pytest.fixture(scope='function')
def screen(request, itest, update_service):
    """Returns a new OMERO Screen with required fields set."""
    screen = ScreenI()
    screen.name = rstring(itest.uuid())
    return update_service.saveAndReturnObject(screen)


@pytest.fixture(scope='function')
def screen_plate(request, itest, update_service):
    """
    Returns a new OMERO Screen and linked Plate with required fields set.
    """
    screen = ScreenI()
    screen.name = rstring(itest.uuid())
    plate = PlateI()
    plate.name = rstring(itest.uuid())
    screen.linkPlate(plate)
    return update_service.saveAndReturnObject(screen)


@pytest.fixture(scope='function')
def screen_plate_well(request, itest, update_service):
    """
    Returns a new OMERO Screen, linked Plate and linked Well with required
    fields set.
    """
    screen = ScreenI()
    screen.name = rstring(itest.uuid())
    plate = PlateI()
    plate.name = rstring(itest.uuid())
    # Well A10
    well = WellI()
    well.row = rint(0)
    well.column = rint(9)
    plate.addWell(well)
    screen.linkPlate(plate)
    return update_service.saveAndReturnObject(screen)


@pytest.fixture(scope='function')
def screen_plate_run_well(request, itest, update_service):
    """
    Returns a new OMERO Screen, linked Plate, linked Well, linked WellSample,
    linked Image populate by an L{test.integration.library.ITest} instance and
    linked PlateAcquisition with all required fields set.
    """
    screen = ScreenI()
    screen.name = rstring(itest.uuid())
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
    ws_a = WellSampleI()
    image_a = itest.new_image(name=itest.uuid())
    ws_a.image = image_a
    ws_b = WellSampleI()
    image_b = itest.new_image(name=itest.uuid())
    ws_b.image = image_b
    plate_acquisition = PlateAcquisitionI()
    plate_acquisition.plate = plate
    ws_a.plateAcquisition = plate_acquisition
    ws_b.plateAcquisition = plate_acquisition
    well_a.addWellSample(ws_a)
    well_a.addWellSample(ws_b)
    plate.addWell(well_a)
    plate.addWell(well_b)
    screen.linkPlate(plate)
    return update_service.saveAndReturnObject(screen)


@pytest.fixture(scope='function')
def empty_request(request, request_factory, path):
    """
    Returns a simple GET request object with no 'path' query string.
    """
    return {
        'request': request_factory.get(path),
        'initially_select': list(),
        'initially_open': None
    }


@pytest.fixture(scope='function')
def project_path_request(request, project, request_factory, path):
    """
    Returns a simple GET request object with the 'path' query string
    variable set in the legacy ("project=id") form.
    """
    as_string = 'project=%d' % project.id.val
    initially_select = ['project-%d' % project.id.val]
    return {
        'request': request_factory.get(path, data={'path': as_string}),
        'initially_select': initially_select,
        'initially_open': initially_select
    }


@pytest.fixture(scope='function')
def projects_show_request(request, projects, request_factory, path):
    """
    Returns a simple GET request object with the 'show' query string
    variable set in the legacy ("project=id") form.
    """
    as_string = 'project-%d|project-%d' % (projects[0].id.val,
            projects[1].id.val)
    initially_select = ['project-%d' % projects[0].id.val,
            'project-%d' % projects[1].id.val]
    return {
        'request': request_factory.get(path, data={'show': as_string}),
        'initially_select': initially_select,
        'initially_open': initially_select
    }


@pytest.fixture(scope='function')
def project_dataset_path_request(
        request, project_dataset, request_factory, path):
    """
    Returns a simple GET request object with the 'path' query string
    variable set in the legacy ("project=id|dataset=id") form.
    """
    dataset, = project_dataset.linkedDatasetList()
    as_string = 'project=%d|dataset=%d' % \
        (project_dataset.id.val, dataset.id.val)
    initially_select = ['dataset-%d' % dataset.id.val]
    initially_open = [
        'project-%d' % project_dataset.id.val,
        'dataset-%d' % dataset.id.val
    ]
    return {
        'request': request_factory.get(path, data={'path': as_string}),
        'initially_select': initially_select,
        'initially_open': initially_open
    }


@pytest.fixture(scope='function')
def project_dataset_image_path_request(
        request, project_dataset_image, request_factory, path):
    """
    Returns a simple GET request object with the 'path' query string
    variable set in the legacy ("project=id|dataset=id|image=id") form.
    """
    dataset, = project_dataset_image.linkedDatasetList()
    image, = dataset.linkedImageList()
    as_string = 'project=%d|dataset=%d|image=%d' % \
        (project_dataset_image.id.val, dataset.id.val, image.id.val)
    initially_select = ['image-%d' % image.id.val]
    initially_open = [
        'project-%d' % project_dataset_image.id.val,
        'dataset-%d' % dataset.id.val,
        'image-%d' % image.id.val
    ]
    return {
        'request': request_factory.get(path, data={'path': as_string}),
        'initially_select': initially_select,
        'initially_open': initially_open
    }


@pytest.fixture(scope='function')
def tag_path_request(request, tag, request_factory, path):
    """
    Returns a simple GET request object with the 'path' query string
    variable set in the legacy ("tag=id") form.
    """
    as_string = 'tag=%d' % tag.id.val
    initially_select = ['tag-%d' % tag.id.val]
    initially_open = ['tag-%d' % tag.id.val]
    return {
        'request': request_factory.get(path, data={'path': as_string}),
        'initially_select': initially_select,
        'initially_open': initially_open
    }


@pytest.fixture(scope='function')
def tagset_tag_path_request(request, tagset_tag, request_factory, path):
    """
    Returns a simple GET request object with the 'path' query string
    variable set in the legacy ("tag=id|tag=id") form.
    """
    tag, = tagset_tag.linkedAnnotationList()
    as_string = 'tag=%d|tag=%d' % (tagset_tag.id.val, tag.id.val)
    initially_select = ['tag-%d' % tag.id.val]
    initially_open = [
        'tag-%d' % tagset_tag.id.val,
        'tag-%d' % tag.id.val
    ]
    return {
        'request': request_factory.get(path, data={'path': as_string}),
        'initially_select': initially_select,
        'initially_open': initially_open
    }


@pytest.fixture(scope='function')
def image_path_request(request, image, request_factory, path):
    """
    Returns a simple GET request object with the 'path' query string
    variable set in the legacy ("image=id") form.  Also handles the
    'orphaned-0' container.
    """
    as_string = 'image=%d' % image.id.val
    initially_select = ['image-%d' % image.id.val]
    initially_open = ['orphaned-0', 'image-%d' % image.id.val]
    return {
        'request': request_factory.get(path, data={'path': as_string}),
        'initially_select': initially_select,
        'initially_open': initially_open
    }


@pytest.fixture(scope='function')
def screen_path_request(request, screen, request_factory, path):
    """
    Returns a simple GET request object with the 'path' query string
    variable set in the legacy ("screen=id") form.
    """
    as_string = 'screen=%d' % screen.id.val
    initially_select = ['screen-%d' % screen.id.val]
    return {
        'request': request_factory.get(path, data={'path': as_string}),
        'initially_select': initially_select,
        'initially_open': initially_select
    }


@pytest.fixture(scope='function')
def screen_plate_path_request(request, screen_plate, request_factory, path):
    """
    Returns a simple GET request object with the 'path' query string
    variable set in the legacy ("screen=id|plate=id") form.
    """
    plate, = screen_plate.linkedPlateList()
    as_string = 'screen=%d|plate=%d' % (screen_plate.id.val, plate.id.val)
    initially_select = ['plate-%d' % plate.id.val]
    initially_open = [
        'screen-%d' % screen_plate.id.val,
        'plate-%d' % plate.id.val
    ]
    return {
        'request': request_factory.get(path, data={'path': as_string}),
        'initially_select': initially_select,
        'initially_open': initially_open
    }


@pytest.fixture(scope='function')
def screen_plate_well_show_request(
        request, screen_plate_well, request_factory, path):
    """
    Returns a simple GET request object with the 'show' query string
    variable set in the new ("well-id") form without a PlateAcquisition 'run'.
    """
    plate, = screen_plate_well.linkedPlateList()
    well, = plate.copyWells()
    as_string = 'well-%d' % well.id.val
    initially_select = [
        'plate-%d' % plate.id.val,
        'well-%d' % well.id.val
    ]
    initially_open = [
        'screen-%d' % screen_plate_well.id.val,
        'plate-%d' % plate.id.val,
        'well-%d' % well.id.val
    ]
    return {
        'request': request_factory.get(path, data={'show': as_string}),
        'initially_select': initially_select,
        'initially_open': initially_open
    }


@pytest.fixture(scope='function', params=[0, 1])
def screen_plate_run_well_show_request(
        request, screen_plate_run_well, request_factory, path):
    """
    Returns a simple GET request object with the 'show' query string
    variable set in the new ("well-id") form with a PlateAcquisition 'run'.
    """
    well_index = request.param
    plate, = screen_plate_run_well.linkedPlateList()
    wells = sorted(plate.copyWells(), cmp_well_column)
    # Only the first Well has a WellSample and is linked to the
    # PlateAcquisition.
    ws_a, ws_b = wells[0].copyWellSamples()
    plate_acquisition = ws_a.plateAcquisition
    as_string = 'well-%d' % wells[well_index].id.val
    initially_select = [
        'acquisition-%d' % plate_acquisition.id.val,
        'well-%d' % wells[well_index].id.val
    ]
    initially_open = [
        'screen-%d' % screen_plate_run_well.id.val,
        'plate-%d' % plate.id.val,
        'acquisition-%d' % plate_acquisition.id.val,
        'well-%d' % wells[well_index].id.val
    ]
    return {
        'request': request_factory.get(path, data={'show': as_string}),
        'initially_select': initially_select,
        'initially_open': initially_open
    }


@pytest.fixture(scope='function')
def project_dataset_image_show_request(
        request, project_dataset_image, request_factory, path):
    """
    Returns a simple GET request object with the 'show' query string
    variable set in the new ("image-id") form.
    """
    dataset, = project_dataset_image.linkedDatasetList()
    image, = dataset.linkedImageList()
    as_string = 'image-%d' % image.id.val
    initially_select = ['image-%d' % image.id.val]
    initially_open = [
        'project-%d' % project_dataset_image.id.val,
        'dataset-%d' % dataset.id.val,
        'image-%d' % image.id.val
    ]
    return {
        'request': request_factory.get(path, data={'show': as_string}),
        'initially_select': initially_select,
        'initially_open': initially_open
    }


@pytest.fixture(scope='function')
def project_by_id_path_request(request, project, request_factory, path):
    """
    Returns a simple GET request object with the 'path' query string
    variable set in the key / value ("project.key=value") form.
    """
    as_string = 'project.id=%d' % project.id.val
    initially_select = ['project-%d' % project.id.val]
    return {
        'request': request_factory.get(path, data={'path': as_string}),
        'initially_select': initially_select,
        'initially_open': initially_select
    }


@pytest.fixture(scope='function')
def project_by_name_path_request(request, project, request_factory, path):
    """
    Returns a simple GET request object with the 'path' query string
    variable set in the key / value ("project.key=value") form.
    """
    as_string = 'project.name=%s' % project.name.val
    initially_select = ['project-%d' % project.id.val]
    return {
        'request': request_factory.get(path, data={'path': as_string}),
        'initially_select': initially_select,
        'initially_open': initially_select
    }


@pytest.fixture(scope='function')
def tag_by_textvalue_path_request(request, tag, request_factory, path):
    """
    Returns a simple GET request object with the 'path' query string
    variable set in the key / value ("tag.key=value") form.
    """
    as_string = 'tag.textValue=%s' % tag.textValue.val
    initially_select = ['tag-%d' % tag.id.val]
    return {
        'request': request_factory.get(path, data={'path': as_string}),
        'initially_select': initially_select,
        'initially_open': initially_select
    }


@pytest.fixture(scope='module', params=['A10', '1J'])
def well_name(request):
    return request.param


@pytest.fixture(scope='function')
def well_by_name_path_request(
        request, screen_plate_well, request_factory, path, well_name):
    """
    Returns a simple GET request object with the 'path' query string
    variable set in the key / value ("well.key=value") form.
    """
    plate, = screen_plate_well.linkedPlateList()
    well, = plate.copyWells()
    as_string = 'plate.name=%s|well.name=%s' % (plate.name.val, well_name)
    initially_select = [
        'plate-%d' % plate.id.val,
        'well-%d' % well.id.val
    ]
    initially_open = [
        'screen-%d' % screen_plate_well.id.val,
        'plate-%d' % plate.id.val,
        'well-%d' % well.id.val
    ]
    return {
        'request': request_factory.get(path, data={'path': as_string}),
        'initially_select': initially_select,
        'initially_open': initially_open
    }


@pytest.fixture(scope='module', params=[
    'plate.name=%(plate_name)s|well.name=%(well_name)s',
    'run=%(plate_acquisition_id)s|well.name=%(well_name)s'
])
def as_string_well_by_name(request):
    return request.param


@pytest.fixture(scope='function')
def screen_plate_run_well_by_name_path_request(
        request, screen_plate_run_well, request_factory, path, well_name,
        as_string_well_by_name):
    """
    Returns a simple GET request object with the 'path' query string
    variable set in the new ("well.key=value") form with a PlateAcquisition
    'run'.
    """
    plate, = screen_plate_run_well.linkedPlateList()
    well_a, well_b = sorted(plate.copyWells(), cmp_well_column)
    ws_a, ws_b = well_a.copyWellSamples()
    plate_acquisition = ws_a.plateAcquisition
    as_string = as_string_well_by_name % {
        'plate_acquisition_id': plate_acquisition.id.val,
        'plate_name': plate.name.val,
        'well_name': well_name
    }
    initially_select = [
        'acquisition-%d' % plate_acquisition.id.val,
        'well-%d' % well_a.id.val
    ]
    initially_open = [
        'screen-%d' % screen_plate_run_well.id.val,
        'plate-%d' % plate.id.val,
        'acquisition-%d' % plate_acquisition.id.val,
        'well-%d' % well_a.id.val
    ]
    return {
        'request': request_factory.get(path, data={'path': as_string}),
        'initially_select': initially_select,
        'initially_open': initially_open
    }


@pytest.fixture(scope='function')
def screen_plate_run_illegal_run_request(
        request, screen_plate_run_well, request_factory, path):
    """
    Returns a simple GET request object with the 'path' query string
    variable set to an illegal ("run.name=value") form with a
    PlateAcquisition 'run'.
    """
    plate, = screen_plate_run_well.linkedPlateList()
    well_a, well_b = sorted(plate.copyWells(), cmp_well_column)
    ws_a, ws_b = well_a.copyWellSamples()
    plate_acquisition = ws_a.plateAcquisition
    as_string = 'plate.name-%s|run.name-Run%d' % (
        plate.name.val, plate_acquisition.id.val
    )
    initially_select = [
        'acquisition.name-Run%d' % plate_acquisition.id.val
    ]
    return {
        'request': request_factory.get(path, data={'path': as_string}),
        'initially_select': initially_select
    }


class TestShow(object):
    """
    Tests to ensure that OMERO.web "show" infrastructure is working
    correctly.

    These tests make __extensive__ use of pytest fixtures.  In particular
    the scoping semantics allowing re-use of instances populated by the
    *request fixtures.  It is recommended that the pytest fixture
    documentation be studied in detail before modifications or attempts to
    fix failing tests are made:

     * https://pytest.org/latest/fixture.html
    """

    def assert_instantiation(self, show, request, conn):
        assert show.conn == conn
        assert show.initially_open is None
        assert show.initially_open_owner is None
        assert show._first_selected is None

    def test_empty_path(self, empty_request):
        show = Show(conn, empty_request['request'], None)
        self.assert_instantiation(show, empty_request, conn)
        first_selected = show.first_selected
        assert first_selected is None

    def test_project_legacy_path(self, conn, project_path_request, project):
        show = Show(conn, project_path_request['request'], None)
        self.assert_instantiation(show, project_path_request, conn)

        first_selected = show.first_selected
        assert first_selected is not None
        assert isinstance(first_selected, ProjectWrapper)
        assert first_selected.getId() == project.id.val
        assert show.initially_open == project_path_request['initially_open']
        assert show.initially_open_owner == project.details.owner.id.val
        assert show._first_selected == first_selected
        assert show.initially_select == \
            project_path_request['initially_select']

    def test_projects_legacy_show(self, conn, projects_show_request, projects):
        show = Show(conn, projects_show_request['request'], None)
        self.assert_instantiation(show, projects_show_request, conn)

        first_selected = show.first_selected
        assert first_selected is not None
        assert isinstance(first_selected, ProjectWrapper)
        assert first_selected.getId() == projects[0].id.val
        assert show.initially_open == projects_show_request['initially_open'][:1]
        assert show.initially_open_owner == projects[0].details.owner.id.val
        assert show._first_selected == first_selected
        assert len(show.initially_select) == 2
        assert show.initially_select == \
            projects_show_request['initially_select']

    def test_project_dataset_legacy_path(
            self, conn, project_dataset_path_request, project_dataset):
        show = Show(conn, project_dataset_path_request['request'], None)
        self.assert_instantiation(show, project_dataset_path_request, conn)

        dataset, = project_dataset.linkedDatasetList()
        first_selected = show.first_selected
        assert first_selected is not None
        assert isinstance(first_selected, DatasetWrapper)
        assert first_selected.getId() == dataset.id.val
        assert show.initially_open == \
            project_dataset_path_request['initially_open']
        assert show.initially_open_owner == \
            project_dataset.details.owner.id.val
        assert show._first_selected == first_selected
        assert show.initially_select == \
            project_dataset_path_request['initially_select']

    def test_project_dataset_image_legacy_path(
            self, conn, project_dataset_image_path_request,
            project_dataset_image):
        show = Show(conn, project_dataset_image_path_request['request'], None)
        self.assert_instantiation(
            show, project_dataset_image_path_request, conn
        )

        dataset, = project_dataset_image.linkedDatasetList()
        image, = dataset.linkedImageList()
        first_selected = show.first_selected
        assert first_selected is not None
        assert isinstance(first_selected, ImageWrapper)
        assert first_selected.getId() == image.id.val
        assert show.initially_open == \
            project_dataset_image_path_request['initially_open']
        assert show.initially_open_owner == \
            project_dataset_image.details.owner.id.val
        assert show._first_selected == first_selected
        assert show.initially_select == \
            project_dataset_image_path_request['initially_select']

    def test_tag_redirect(self, tag_path_request):
        show = Show(conn, tag_path_request['request'], None)
        self.assert_instantiation(show, tag_path_request, conn)

        with pytest.raises(IncorrectMenuError) as excinfo:
            show.first_selected
        assert excinfo.value.uri is not None

    def test_tag_legacy_path(self, conn, tag_path_request, tag):
        show = Show(conn, tag_path_request['request'], 'usertags')
        self.assert_instantiation(show, tag_path_request, conn)

        first_selected = show.first_selected
        assert first_selected is not None
        assert isinstance(first_selected, TagAnnotationWrapper)
        assert first_selected.getId() == tag.id.val
        assert show.initially_open == \
            tag_path_request['initially_open']
        assert show.initially_open_owner == \
            tag.details.owner.id.val
        assert show._first_selected == first_selected
        assert show.initially_select == tag_path_request['initially_select']

    def test_tagset_tag_legacy_path(
            self, conn, tagset_tag_path_request, tagset_tag):
        show = Show(conn, tagset_tag_path_request['request'], 'usertags')
        self.assert_instantiation(show, tagset_tag_path_request, conn)

        tag, = tagset_tag.linkedAnnotationList()
        first_selected = show.first_selected
        assert first_selected is not None
        assert isinstance(first_selected, TagAnnotationWrapper)
        assert first_selected.getId() == tag.id.val
        assert show.initially_open == \
            tagset_tag_path_request['initially_open']
        assert show.initially_open_owner == \
            tag.details.owner.id.val
        assert show._first_selected == first_selected
        assert show.initially_select == \
            tagset_tag_path_request['initially_select']

    def test_image_legacy_path(self, conn, image_path_request, image):
        show = Show(conn, image_path_request['request'], None)
        self.assert_instantiation(show, image_path_request, conn)

        first_selected = show.first_selected
        assert first_selected is not None
        assert isinstance(first_selected, ImageWrapper)
        assert first_selected.getId() == image.id.val
        assert show.initially_open == \
            image_path_request['initially_open']
        assert show.initially_open_owner == \
            image.details.owner.id.val
        assert show._first_selected == first_selected
        assert show.initially_select == image_path_request['initially_select']

    def test_screen_legacy_path(self, conn, screen_path_request, screen):
        show = Show(conn, screen_path_request['request'], None)
        self.assert_instantiation(show, screen_path_request, conn)

        first_selected = show.first_selected
        assert first_selected is not None
        assert isinstance(first_selected, ScreenWrapper)
        assert first_selected.getId() == screen.id.val
        assert show.initially_open == \
            screen_path_request['initially_open']
        assert show.initially_open_owner == \
            screen.details.owner.id.val
        assert show._first_selected == first_selected
        assert show.initially_select == \
            screen_path_request['initially_select']

    def test_screen_plate_legacy_path(
            self, conn, screen_plate_path_request, screen_plate):
        show = Show(conn, screen_plate_path_request['request'], None)
        self.assert_instantiation(show, screen_plate_path_request, conn)

        plate, = screen_plate.linkedPlateList()
        first_selected = show.first_selected
        assert first_selected is not None
        assert isinstance(first_selected, PlateWrapper)
        assert first_selected.getId() == plate.id.val
        assert show.initially_open == \
            screen_plate_path_request['initially_open']
        assert show.initially_open_owner == \
            screen_plate.details.owner.id.val
        assert show._first_selected == first_selected
        assert show.initially_select == \
            screen_plate_path_request['initially_select']

    def test_screen_plate_well_show(
            self, conn, screen_plate_well_show_request, screen_plate_well):
        show = Show(conn, screen_plate_well_show_request['request'], None)
        self.assert_instantiation(show, screen_plate_well_show_request, conn)

        plate, = screen_plate_well.linkedPlateList()
        well, = plate.copyWells()
        first_selected = show.first_selected
        assert first_selected is not None
        assert isinstance(first_selected, PlateWrapper)
        assert first_selected.getId() == plate.id.val
        assert show.initially_open == \
            screen_plate_well_show_request['initially_open']
        assert show.initially_open_owner == \
            plate.details.owner.id.val
        assert show._first_selected == first_selected
        assert show.initially_select == \
            screen_plate_well_show_request['initially_select']

    def test_screen_plate_run_well_show(
            self, conn, screen_plate_run_well_show_request,
            screen_plate_run_well):
        show = Show(conn, screen_plate_run_well_show_request['request'], None)
        self.assert_instantiation(
            show, screen_plate_run_well_show_request, conn
        )

        plate, = screen_plate_run_well.linkedPlateList()
        well_a, well_b = \
            sorted(plate.copyWells(), cmp_well_column)
        ws_a, ws_b, = well_a.copyWellSamples()
        plate_acquisition = ws_a.plateAcquisition
        first_selected = show.first_selected
        assert first_selected is not None
        assert isinstance(first_selected, PlateAcquisitionWrapper)
        assert first_selected.getId() == plate_acquisition.id.val
        assert show.initially_open == \
            screen_plate_run_well_show_request['initially_open']
        assert show.initially_open_owner == \
            plate_acquisition.details.owner.id.val
        assert show._first_selected == first_selected
        assert show.initially_select == \
            screen_plate_run_well_show_request['initially_select']

    def test_project_dataset_image_show(
            self, conn, project_dataset_image_show_request,
            project_dataset_image):
        show = Show(conn, project_dataset_image_show_request['request'], None)
        self.assert_instantiation(
            show, project_dataset_image_show_request, conn
        )

        dataset, = project_dataset_image.linkedDatasetList()
        image, = dataset.linkedImageList()
        first_selected = show.first_selected
        assert first_selected is not None
        assert isinstance(first_selected, ImageWrapper)
        assert first_selected.getId() == image.id.val
        assert show.initially_open == \
            project_dataset_image_show_request['initially_open']
        assert show.initially_open_owner == \
            project_dataset_image.details.owner.id.val
        assert show._first_selected == first_selected
        assert show.initially_select == \
            project_dataset_image_show_request['initially_select']

    def test_project_by_id(
            self, conn, project_by_id_path_request, project):
        show = Show(conn, project_by_id_path_request['request'], None)
        self.assert_instantiation(show, project_by_id_path_request, conn)

        first_selected = show.first_selected
        assert first_selected is not None
        assert isinstance(first_selected, ProjectWrapper)
        assert first_selected.getId() == project.id.val
        assert show.initially_open == \
            project_by_id_path_request['initially_open']
        assert show.initially_open_owner == project.details.owner.id.val
        assert show._first_selected == first_selected
        assert show.initially_select == \
            project_by_id_path_request['initially_select']

    def test_project_by_name(
            self, conn, project_by_name_path_request, project):
        show = Show(conn, project_by_name_path_request['request'], None)
        self.assert_instantiation(show, project_by_name_path_request, conn)

        first_selected = show.first_selected
        assert first_selected is not None
        assert isinstance(first_selected, ProjectWrapper)
        assert first_selected.getId() == project.id.val
        assert show.initially_open == \
            project_by_name_path_request['initially_open']
        assert show.initially_open_owner == project.details.owner.id.val
        assert show._first_selected == first_selected
        assert show.initially_select == \
            project_by_name_path_request['initially_select']

    def test_tag_by_value(self, conn, tag_by_textvalue_path_request, tag):
        show = Show(conn, tag_by_textvalue_path_request['request'], 'usertags')
        self.assert_instantiation(show, tag_by_textvalue_path_request, conn)

        first_selected = show.first_selected
        assert first_selected is not None
        assert isinstance(first_selected, TagAnnotationWrapper)
        assert first_selected.getValue() == tag.textValue.val
        assert len(show.initially_open) == \
            len(tag_by_textvalue_path_request['initially_open'])
        assert show.initially_open_owner == \
            tag.details.owner.id.val
        assert show._first_selected == first_selected
        assert len(show.initially_select) == \
            len(tag_by_textvalue_path_request['initially_select'])

    def test_well_by_name(
            self, conn, well_by_name_path_request, screen_plate_well):
        show = Show(conn, well_by_name_path_request['request'], 'usertags')
        self.assert_instantiation(show, well_by_name_path_request, conn)

        plate, = screen_plate_well.linkedPlateList()
        first_selected = show.first_selected
        assert first_selected is not None
        assert isinstance(first_selected, PlateWrapper)
        assert first_selected.getId() == plate.id.val
        assert show.initially_open == \
            well_by_name_path_request['initially_open']
        assert show.initially_open_owner == \
            plate.details.owner.id.val
        assert show._first_selected == first_selected
        assert show.initially_select == \
            well_by_name_path_request['initially_select']

    def test_screen_plate_run_well_by_name(
            self, conn, screen_plate_run_well_by_name_path_request,
            screen_plate_run_well):
        show = Show(
            conn, screen_plate_run_well_by_name_path_request['request'], None
        )
        self.assert_instantiation(
            show, screen_plate_run_well_by_name_path_request, conn
        )

        plate, = screen_plate_run_well.linkedPlateList()
        well_a, well_b = \
            sorted(plate.copyWells(), cmp_well_column)
        ws_a, ws_b = well_a.copyWellSamples()
        plate_acquisition = ws_a.plateAcquisition
        first_selected = show.first_selected
        assert first_selected is not None
        assert isinstance(first_selected, PlateAcquisitionWrapper)
        assert first_selected.getId() == plate_acquisition.id.val
        assert show.initially_open == \
            screen_plate_run_well_by_name_path_request['initially_open']
        assert show.initially_open_owner == \
            plate_acquisition.details.owner.id.val
        assert show._first_selected == first_selected
        assert show.initially_select == \
            screen_plate_run_well_by_name_path_request['initially_select']

    def test_screen_plate_run_illegal_run(
            self, conn, screen_plate_run_illegal_run_request,
            screen_plate_run_well):
        show = Show(
            conn, screen_plate_run_illegal_run_request['request'], None
        )
        self.assert_instantiation(
            show, screen_plate_run_illegal_run_request, conn
        )

        first_selected = show.first_selected
        assert first_selected is None
        assert show.initially_open is None
        assert show.initially_select == \
            screen_plate_run_illegal_run_request['initially_select']
