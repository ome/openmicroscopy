#!/usr/bin/env python
# -*- coding: utf-8 -*-

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

"""Exceptions used by the api/views methods."""

class BadRequestError(Exception):
    """
    An exception that will result in a response status of 400.

    Due to invalid client input
    """
    status = 400

    def __init__(self, message, stacktrace=None):
    	"""Override init to handle message and stacktrace."""
        super(BadRequestError, self).__init__(message)
        self.stacktrace = stacktrace


class NotFoundError(Exception):
    """
    An exception that will result in a response status of 404
    due to objects not being found
    """
    status = 404

    def __init__(self, message, stacktrace=None):
        super(NotFoundError, self).__init__(message)
        self.stacktrace = stacktrace


class CreatedObject(Exception):
    """
    An exception that is thrown when new object created
    that returns a status of 201
    """
    status = 201

    def __init__(self, response):
        super(CreatedObject, self).__init__(response)
        self.response = response
