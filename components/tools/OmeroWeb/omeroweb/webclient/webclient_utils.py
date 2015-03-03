#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#
# Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
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

import logging

logger = logging.getLogger(__name__)


def formatPercentFraction(value):
    """ Formats a fraction as a percentage for display """
    value = value * 100
    if value < 1:
        value = "%.1f" % round(value, 1)
    else:
        value = "%s" % int(round(value))
    return value


def _formatReport(callback):
    """
    Added as workaround to the changes made in #3006.
    """
    rsp = callback.getResponse()
    if not rsp:
        return  # Unfinished

    import omero
    if isinstance(rsp, omero.cmd.ERR):
        err = rsp.parameters.get("Error", "")
        warn = rsp.parameters.get("Warning", "")
        logger.error('Format report: %r' % {'error': err, 'warning': warn})
        return "Operation could not be completed successfully"
    else:
        for rsp in rsp.responses:
            if rsp.warning:
                logger.warning("Delete warning: %s" % rsp.warning)
    # Might want to take advantage of other feedback here


def _purgeCallback(request):

    callbacks = request.session.get('callback').keys()
    if len(callbacks) > 200:
        for (cbString, count) in zip(request.session.get('callback').keys(),
                                     range(0, len(callbacks)-200)):
            del request.session['callback'][cbString]
