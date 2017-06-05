#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Simple unit tests for the "show" module.

   Copyright 2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import pytest

from omeroweb.webclient.show import Show
from django.test.client import RequestFactory


@pytest.fixture(scope='module')
def path():
    """Returns the root OMERO.web webclient path."""
    return '/webclient'


@pytest.fixture(scope='module')
def request_factory():
    """Returns a fresh Django request factory."""
    return RequestFactory()


@pytest.fixture(scope='function')
def empty_request(request_factory, path):
    """Returns a simple GET request with no query string."""
    return request_factory.get(path)


@pytest.fixture(scope='function', params=[
    ('project=1', ('project.id-1',)),
    ('project=1|dataset=1', ('dataset.id-1',)),
    ('project=1|dataset=1|image=1', ('image.id-1',)),
    ('illegal=1', tuple())
])
def path_request(request, request_factory, path):
    """
    Returns a simple GET request object with the 'path' query string
    variable set in the legacy ("type=id") form.
    """
    as_string, initially_select = request.param
    return {
        'request': request_factory.get(path, data={'path': as_string}),
        'initially_select': initially_select
    }


@pytest.fixture(scope='function', params=[
    ('project-1', ('project.id-1',)),
    ('project-1|project-2', ('project.id-1', 'project.id-2',)),
    ('acquisition-1', ('acquisition.id-1',)),
    ('run-1', ('acquisition.id-1',)),
    ('illegal-1', tuple())
])
def show_request(request, request_factory, path):
    """
    Returns a simple GET request object with the 'show' query string
    variable set.
    """
    as_string, initially_select = request.param
    return {
        'request': request_factory.get(path, data={'show': as_string}),
        'initially_select': initially_select
    }


@pytest.fixture(scope='function', params=('project-1', 'project=1'))
def project_path(request):
    """Returns a Project based path in both supported forms."""
    return request.param


@pytest.fixture(scope='function', params=(
    'project.name-the_name', 'project.name=the_name'
))
def project_path_key(request):
    """Returns a Project, key based path in both supported forms."""
    return request.param


class TestShow(object):
    """
    Tests to ensure that OMERO.web "show" infrastructure is working
    correctly.
    """

    def test_basic_instantiation(self, empty_request):
        show = Show(None, empty_request, None)
        assert len(show.initially_open) == 0
        assert show.initially_open_owner is None
        assert show.initially_select == list()
        assert show._first_selected is None

    def test_legacy_path_instantiation(self, path_request):
        show = Show(None, path_request['request'], None)
        assert len(show.initially_open) == 0
        assert show.initially_open_owner is None
        assert show.initially_select == list(path_request['initially_select'])
        assert show._first_selected is None

    def test_show_instantiation(self, show_request):
        show = Show(None, show_request['request'], None)
        assert len(show.initially_open) == 0
        assert show.initially_open_owner is None
        assert show.initially_select == list(show_request['initially_select'])
        assert show._first_selected is None

    def test_path_regex_single_no_key(self, project_path):
        m = Show.PATH_REGEX.match(project_path)
        assert m.group('object_type') == 'project'
        assert m.group('key') is None
        assert m.group('value') == '1'

    def test_path_regex_single_key(self, project_path_key):
        m = Show.PATH_REGEX.match(project_path_key)
        assert m.group('object_type') == 'project'
        assert m.group('key') == 'name'
        assert m.group('value') == 'the_name'

    def test_path_regex_single_name_includes_separator(self):
        m = Show.PATH_REGEX.match('project.name-blah-blah-blah')
        assert m.group('object_type') == 'project'
        assert m.group('key') == 'name'
        assert m.group('value') == 'blah-blah-blah'

    def test_path_regex_multiple(self, project_path_key):
        matches = Show.PATH_REGEX.finditer(
            '%s|%s' % (project_path_key, project_path_key)
        )
        count = 0
        for m in matches:
            assert m.group('object_type') == 'project'
            assert m.group('key') == 'name'
            assert m.group('value') == 'the_name'
            count += 1
        assert count == 2

    def test_well_regex_alpha_digit(self):
        m = Show.WELL_REGEX.match('A1')
        assert m is not None
        assert m.group('alpha_row') == 'A'
        assert m.group('digit_row') is None
        assert m.group('alpha_column') is None
        assert m.group('digit_column') == '1'

    def test_well_regex_digit_alpha(self):
        m = Show.WELL_REGEX.match('1A')
        assert m is not None
        assert m.group('alpha_row') is None
        assert m.group('digit_row') == '1'
        assert m.group('alpha_column') == 'A'
        assert m.group('digit_column') is None
