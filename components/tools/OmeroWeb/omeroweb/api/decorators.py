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

"""Decorators for use with the webgateway application."""

import omero
import omeroweb.decorators
import logging
import traceback
from django.http import JsonResponse
from functools import update_wrapper
from api_exceptions import NotFoundError, BadRequestError, CreatedObject


logger = logging.getLogger(__name__)


class LoginRequired(omeroweb.decorators.login_required):
    """webgateway specific extension of the login_required() decorator."""

    def on_not_logged_in(self, request, url, error=None):
        """Used for json api methods."""
        return JsonResponse({'message': 'Not logged in'},
                            status=403)


class json_response(object):
    """
    Class-based decorator for wrapping Django views methods.
    Returns JsonResponse based on dict returned by views methods.
    Also handles exceptions from views methods, returning
    JsonResponse with appropriate status values.
    """

    def __init__(self):
        """Initialises the decorator."""
        pass

    def handle_success(self, rv):
        """
        Handle successful response from wrapped function.

        By default, we simply return a JsonResponse() but this can be
        overwritten by subclasses if needed.
        """
        return JsonResponse(rv)

    def handle_error(self, ex, trace):
        """
        Handle errors from wrapped function.

        By default, we format exception or message and return this
        as a JsonResponse with an appropriate status code.
        """

        # Default status is 500 'server error'
        # But we try to handle all 'expected' errors appropriately
        # TODO: handle omero.ConcurrencyException
        status = 500
        if isinstance(ex, NotFoundError):
            status = ex.status
        if isinstance(ex, BadRequestError):
            status = ex.status
            trace = ex.stacktrace   # Might be None
        elif isinstance(ex, omero.SecurityViolation):
            status = 403
        elif isinstance(ex, omero.ApiUsageException):
            status = 400
        logger.debug(trace)
        rsp_json = {"message": str(ex)}
        if trace is not None:
            rsp_json["stacktrace"] = trace
        # In this case, there's no Error and the response
        # is valid (status code is 201)
        if isinstance(ex, CreatedObject):
            status = ex.status
            rsp_json = ex.response
        return JsonResponse(rsp_json, status=status)

    def __call__(ctx, f):
        """
        Returns the decorator.

        The decorator calls the wrapped function and
        handles success or exception, returning a
        JsonResponse
        """
        def wrapped(request, *args, **kwargs):
            logger.debug('json_response')
            try:
                rv = f(request, *args, **kwargs)
                return ctx.handle_success(rv)
            except Exception, ex:
                trace = traceback.format_exc()
                return ctx.handle_error(ex, trace)
        return update_wrapper(wrapped, f)
