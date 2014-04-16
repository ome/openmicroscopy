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
    ('project=1', ['project-1']),
    ('project=1|dataset=1', ['dataset-1']),
    ('project=1|dataset=1|image=1', ['image-1'])
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
    ('project-1', ['project-1']),
    ('project-1|project-2', ['project-1', 'project-2'])
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


class TestShow(object):
    """
    Tests to ensure that OMERO.web "show" infrastructure is working
    correctly.
    """

    def test_basic_instantiation(self, empty_request):
        show = Show(None, empty_request, None)
        assert show.initially_open is None
        assert show.initially_open_owner is None
        assert show.initially_select == list()
        assert show.first_sel is None

    def test_legacy_path(self, path_request):
        show = Show(None, path_request['request'], None)
        assert show.initially_open is None
        assert show.initially_open_owner is None
        assert show.initially_select == path_request['initially_select']
        assert show.first_sel is None

    def test_legacy_path_instantiation(self, show_request):
        show = Show(None, show_request['request'], None)
        assert show.initially_open is None
        assert show.initially_open_owner is None
        assert show.initially_select == show_request['initially_select']
        assert show.first_sel is None
