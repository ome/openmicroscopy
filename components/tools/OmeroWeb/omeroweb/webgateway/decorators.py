#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
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
#

"""
Decorators for use with the webgateway application.
"""

import omeroweb.decorators
from omeroweb.http import JsonResponseForbidden


class login_required(omeroweb.decorators.login_required):
    """
    webgateway specific extension of the OMERO.web login_required() decorator.
    """

    def on_not_logged_in(self, request, url, error=None):
        """
        Used for json api methods
        """
        return JsonResponseForbidden({'message': 'Not logged in'})
