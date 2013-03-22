#!/usr/bin/env python
# -*- coding: utf-8 -*-
import logging
import traceback
import re
from omero_version import omero_version

from webclient.webclient_gateway import OmeroWebGateway

logger = logging.getLogger(__name__)

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
    # http://trac.openmicroscopy.org.uk/ome/wiki/UpgradeCheck
    #
    try:
        from omero.util.upgrade_check import UpgradeCheck
        check = UpgradeCheck("web")
        check.run()
        if check.isUpgradeNeeded():
            logger.error("Upgrade is available. Please visit http://trac.openmicroscopy.org.uk/ome/wiki/MilestoneDownloads.\n")
        else:
            logger.debug("Up to date.\n")
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
