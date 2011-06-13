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
logger = logging.getLogger('utils')

def _formatReport(delete_handle):
    """
    Added as workaround to the changes made in #3006.
    """
    delete_reports = delete_handle.report()
    rv = []
    for report in delete_reports:
        if report.error:
            rv.append(report.error)
        elif report.warning:
            rv.append(report.warning)
    return "; ".join(rv)
    # Might want to take advantage of other feedback here

def _purgeCallback(request):
    
    callbacks = request.session.get('callback').keys()
    if len(callbacks) > 200:
        for (cbString, count) in zip(request.session.get('callback').keys(), range(0,len(callbacks)-200)):
            del request.session['callback'][cbString]

def upgradeCheck():
    # upgrade check:
    # -------------
    # On each startup OMERO.web checks for possible server upgrades
    # and logs the upgrade url at the WARNING level. If you would
    # like to disable the checks, change the following to
    #
    #   if False:
    #
    # For more information, see
    # http://trac.openmicroscopy.org.uk/omero/wiki/UpgradeCheck
    #
    try:
        from omero.util.upgrade_check import UpgradeCheck
        check = UpgradeCheck("web")
        check.run()
        if check.isUpgradeNeeded():
            logger.error("Upgrade is available. Please visit http://trac.openmicroscopy.org.uk/omero/wiki/MilestoneDownloads.\n")
        else:
            logger.error("Up to date.\n")
    except Exception, x:
        logger.error("Upgrade check error: %s" % x)
    
def toBoolean(val):
    """ 
    Get the boolean value of the provided input.

        If the value is a boolean return the value.
        Otherwise check to see if the value is in 
        ["false", "f", "no", "n", "none", "0", "[]", "{}", "" ]
        and returns True if value is not in the list
    """

    if val is True or val is False:
        return val

    falseItems = ["false", "f", "no", "n", "none", "0", "[]", "{}", "" ]

    return not str( val ).strip().lower() in falseItems

def string_to_dict(string):
    kwargs = {}
    if string:
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

        