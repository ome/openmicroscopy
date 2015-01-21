#!/usr/bin/env python
# -*- coding: utf-8 -*-
import logging

from django.core.validators import validate_email

logger = logging.getLogger(__name__)


def upgradeCheck(url):
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
        if url:
            check = UpgradeCheck("web", url=url)
            check.run()
            if check.isUpgradeNeeded():
                logger.error(
                    "Upgrade is available. Please visit"
                    " http://downloads.openmicroscopy.org/latest/omero/.\n")
            else:
                logger.debug("Up to date.\n")
    except Exception, x:
        logger.error("Upgrade check error: %s" % x)


def removeUnaddressable(experimenters):
    """
    Remove experimenters that do not have email addresses or do not pass
    validation
    """
    remaining = list([])
    for experimenter in experimenters:
        if experimenter.email:
            email = experimenter.email.strip()
            if len(email) > 0:
                try:
                    validate_email(email)
                    remaining.append(experimenter)
                except Exception:
                    # Silently ignore experimenters with broken email
                    # addresses
                    # It would make sense to send an error message back to the
                    # client here if it were not for the fact that in the
                    # experimenter addressee field, these will already have
                    # been removed
                    pass

    return remaining


def resolveExperimenters(conn, everyone=False, group_ids=None,
                         experimenter_ids=None):
    """
    Turn group IDs and experimenter IDs into a list of experimenters broken
    down by if they are inactive or not

    If the everyone paramter is provided, ignore the other parameters and
    simply get all the available email addresses in the server
    """

    # Ignore all other inputs if everyone is set
    if everyone:
        # It would be possible to use the query service to only get
        # experimenters with email addresses, but it's not really worth it
        experimenters = conn.getObjects('Experimenter')

        experimenters_active = []
        experimenters_inactive = []

        for experimenter in experimenters:
            if experimenter.isActive():
                experimenters_active.append(experimenter)
            else:
                experimenters_inactive.append(experimenter)

        return experimenters_active, experimenters_inactive

    # TODO Should use BlitzSet to give a set with key from id
    experimenters = {}
    experimenters_inactive = {}

    # Add experimenters in given groups
    if group_ids:
        for group_id in group_ids:
            for experimenter in conn.containedExperimenters(group_id):
                if experimenter.isActive():
                    experimenters[experimenter.id] = experimenter
                else:
                    experimenters_inactive[experimenter.id] = experimenter

    # Add explicitly specified experimenters
    if experimenter_ids:
        for experimenter in conn.getObjects('Experimenter', experimenter_ids):
            if experimenter.isActive():
                experimenters[experimenter.id] = experimenter
            else:
                experimenters_inactive[experimenter.id] = experimenter

    return experimenters.values(), experimenters_inactive.values()
