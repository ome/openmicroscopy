#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

"""
Test download of data.
"""

# import omero
# import omero.clients
from omero.model import PlateI, WellI, WellSampleI
from omero.rtypes import rstring

import pytest
import test.integration.library as lib

from urllib import urlencode
from django.test import Client
from django.core.urlresolvers import reverse


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
    """Returns a new user client in a read-only group."""
    # Use group read-only permissions (not private) by default
    return itest.new_client(perms='rwr---')


@pytest.fixture(scope='function')
def django_client(request, client):
    """Returns a logged in Django test client."""
    django_client = Client(enforce_csrf_checks=True)
    login_url = reverse('weblogin')

    response = django_client.get(login_url)
    assert response.status_code == 200
    csrf_token = django_client.cookies['csrftoken'].value

    data = {
        'server': 1,
        'username': client.getProperty('omero.user'),
        'password': client.getProperty('omero.pass'),
        'csrfmiddlewaretoken': csrf_token
    }
    response = django_client.post(login_url, data)
    assert response.status_code == 302

    def finalizer():
        logout_url = reverse('weblogout')
        data = {'csrfmiddlewaretoken': csrf_token}
        response = django_client.post(logout_url, data=data)
        assert response.status_code == 302
    request.addfinalizer(finalizer)
    return django_client


@pytest.fixture(scope='function')
def update_service(request, client):
    """Returns a new OMERO update service."""
    return client.getSession().getUpdateService()


@pytest.fixture(scope='function')
def image_well_plate(request, itest, update_service):
    """
    Returns a new OMERO Project, linked Dataset and linked Image populated
    by an L{test.integration.library.ITest} instance with required fields
    set.
    """
    plate = PlateI()
    plate.name = rstring(itest.uuid())
    plate = update_service.saveAndReturnObject(plate)

    well = WellI()
    well.plate = plate
    well = update_service.saveAndReturnObject(well)

    image = itest.new_image(name=itest.uuid())

    ws = WellSampleI()
    ws.image = image
    ws.well = well
    well.addWellSample(ws)
    ws = update_service.saveAndReturnObject(ws)
    return ws.image


class TestDownload(object):
    """
    Tests to check download is disabled where specified.
    """

    def test_spw_download(self, itest, client, django_client,
                          image_well_plate):
        """
        Download of an Image that is part of a plate should be disabled,
        and return a 404 response.
        """

        image = image_well_plate
        # download archived files
        request_url = reverse('webgateway.views.archived_files')
        data = {
            "image": image.id.val
        }
        _get_reponse(django_client, request_url, data, status_code=404)

    def test_image_download(self, itest, client, django_client):
        """
        Download of archived files for a non-SPW Image.
        """

        image = itest.importSingleImage(client=client)

        # download archived files
        request_url = reverse('webgateway.views.archived_files')
        data = {
            "image": image.id.val
        }
        _get_reponse(django_client, request_url, data, status_code=200)


# Helpers
def _get_reponse(django_client, request_url, query_string, status_code=405):
    query_string = urlencode(query_string.items())
    response = django_client.get('%s?%s' % (request_url, query_string))
    assert response.status_code == status_code
    return response
