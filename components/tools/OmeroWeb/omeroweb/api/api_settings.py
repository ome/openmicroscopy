#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
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

"""Settings for the OMERO JSON api app."""

import sys
from omeroweb.settings import process_custom_settings, report_settings, \
    str_slash

# load settings
API_SETTINGS_MAPPING = {

    "omero.web.api.limit":
        ["API_LIMIT",
         200,
         int,
         "Default number of items returned from json api."],
    "omero.web.api.max_limit":
        ["API_MAX_LIMIT",
         500,
         int,
         "Maximum number of items returned from json api."],
    "omero.web.api.absolute_url":
        ["API_ABSOLUTE_URL",
         None,
         str_slash,
         ("URL to use for generating urls within API json responses. "
          "By default this is None, and we use Django's "
          "request.build_absolute_uri() to generate absolute urls "
          "based on each request. If set to a string or empty string, "
          "this will be used as prefix to relative urls.")],
}

process_custom_settings(sys.modules[__name__], 'API_SETTINGS_MAPPING')
report_settings(sys.modules[__name__])

# For any given release of api, we may support
# one or more versions of the api.
# E.g. /api/v0/
# TODO - need to decide how this is configured, strategy for extending etc.
API_VERSIONS = ('0',)

# Current major.minor version number
API_VERSION = '0.1'
