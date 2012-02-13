#!/usr/bin/env python
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

import settings
import logging

from django.utils.datastructures import SortedDict

logger = logging.getLogger(__name__)

def _formatReport(delete_handle):
    """
    Added as workaround to the changes made in #3006.
    """
    delete_reports = delete_handle.report()
    for report in delete_reports:
        if report.error or report.warning:
            logger.error('Format report: %r' % {'error':report.error, 'warning':report.warning})
            if report.error:
                return "Operation could not be completed successfully"
    # Might want to take advantage of other feedback here

def _purgeCallback(request):
    
    callbacks = request.session.get('callback').keys()
    if len(callbacks) > 200:
        for (cbString, count) in zip(request.session.get('callback').keys(), range(0,len(callbacks)-200)):
            del request.session['callback'][cbString]

def string_to_dict(string):
    """
    Converts string e.g. path=project=51|dataset=502|image=607:selected to
    dictionary that keeps its keys in the order in which they're inserted.
    """
    kwargs = SortedDict()
    if string is not None and len(string) > 0:
        string = str(string)
        if '|' not in string:
            # ensure at least one ','
            string += '|'
        for arg in string.split('|'):
            arg = arg.strip()
            if arg == '': continue
            kw, val = arg.split('=', 1)
            kwargs[kw] = val
    return kwargs

        