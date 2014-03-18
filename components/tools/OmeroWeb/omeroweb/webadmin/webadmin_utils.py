import logging
import traceback
import re
from omero_version import omero_version

from webclient.webclient_gateway import OmeroWebGateway
from omeroweb.webgateway.views import _createConnection

logger = logging.getLogger(__name__)

def getGuestConnection(host, port):
    conn = None
    guest = "guest"
    try:
        # do not store connection on connectors
        conn = _createConnection('', host=host, port=port, username=guest, passwd=guest, secure=True, useragent="OMERO.web")
        if conn is not None:
            logger.info("Have connection as Guest")
        else:
            logger.info("Open connection is not available")
    except Exception, x:
        logger.error(traceback.format_exc())
    return conn

def _checkVersion(host, port):
    rv = False
    conn = getGuestConnection(host, port)
    if conn is not None:
        try:
            agent = conn.getServerVersion()
            regex = re.compile("^.*?[-]?(\\d+[.]\\d+([.]\\d+)?)[-]?.*?$")

            agent_cleaned = regex.match(agent).group(1)
            agent_split = agent_cleaned.split(".")

            local_cleaned = regex.match(omero_version).group(1)
            local_split = local_cleaned.split(".")

            rv = (agent_split == local_split)
            logger.info("Client version: '%s'; Server version: '%s'"% (omero_version, agent))
        except Exception, x:
            logger.error(traceback.format_exc())
    return rv

def _isServerOn(host, port):
    conn = getGuestConnection(host, port)
    if conn is not None:
        try:
            conn.getServerVersion()
            return True
        except Exception, x:
            logger.error(traceback.format_exc())
    return False

def upgradeCheck():
    # upgrade check:
    # -------------
    # On each startup OMERO.web checks for possible server upgrades
    # and logs the upgrade url at the WARNING level. If you would
    # like to disable the checks, please set 'omero.web.upgrades_url`
    # to an empty string.
    #
    # For more information, see
    # http://trac.openmicroscopy.org.uk/ome/wiki/UpgradeCheck
    #
    try:
        from omero.util.upgrade_check import UpgradeCheck
        from django.conf import settings
        check = UpgradeCheck("web", url=settings.UPGRADES_URL)
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
