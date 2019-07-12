# -*- coding: utf-8 -*-

# Copyright (C) 2019 University of Dundee & Open Microscopy Environment.
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

# https://docs.djangoproject.com/en/1.8/ref/middleware/

from django.conf import settings


class CustomHeadersMiddleware(object):
    """
    Django middleware to add custom headers to a response.

    Headers will only be set if not already present in the response.
    """
    def process_response(self, request, response):
        for header in settings.HTTP_RESPONSE_HEADERS:
            k, v = header
            if not response.has_header(k):
                response[k] = v
        return response
