#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2020 University of Dundee & Open Microscopy Environment.
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

import pytest
from django.http import HttpRequest, HttpResponse

from omero import ApiUsageException
from omeroweb.testlib import IWebTest
from omeroweb.decorators import login_required, ConnCleaningHttpResponse


def mock_view(request, streaming_response=False, conn=None, **kwargs):

    if streaming_response:
        return ConnCleaningHttpResponse()

    return HttpResponse('Test')


class TestShow(IWebTest):

    def test_conn_cleanup(self):
        """
        Tests developer usage of @login_required(doConnectionCleanup=False)

        You should get an Exception if your view method returns
        ConnCleaningHttpResponse and you didn't wrap the method with
        @login_required(doConnectionCleanup=False)
        """

        request = HttpRequest()
        request.session = self.django_client.session

        streaming_rsp_expected = login_required(
            doConnectionCleanup=False)(mock_view)
        streaming_not_expected = login_required()(mock_view)

        # No Exception if streaming expected (with/without streaming response)
        streaming_rsp_expected(request)
        streaming_rsp_expected(request, streaming_response=True)

        # No Exception when streaming not expected
        streaming_not_expected(request)

        # Exception when unexpected return of ConnCleaningHttpResponse
        with pytest.raises(ApiUsageException):
            streaming_not_expected(request, streaming_response=True)
