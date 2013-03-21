#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# $Id$
#
# Copyright 2009 Glencoe Software, Inc. All rights reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# Version comparison functionality

import re
import logging

# Regex copied from http://hudson.openmicroscopy.org.uk/job/OMERO/javadoc/constant-values.html#ome.api.IConfig.VERSION_REGEX
REGEX = re.compile("^.*?[-]?(\\d+[.]\\d+([.]\\d+)?)[-]?.*?$")
LOG = logging.getLogger("omero.version")

def needs_upgrade(client_version, server_version, verbose = False):
    """
    Tests whether the client version is behind the server version.
    For example:

    import omero
    from omero_version import omero_version as client_version
    
    client = omero.client()
    session = client.createSession()
    config = session.getConfigService()
    server_version = config.getVersion()

    upgrade = needs_upgrade(client_version, server_version)
    if upgrade:
       # Inform client

    Alternatively, from the command-line:
    ./versions.py --quiet 4.1.0 4.2.0-DEV || echo upgrade

    """
    try:
        client_cleaned = REGEX.match(client_version).group(1)
        client_split = client_cleaned.split(".")

        server_cleaned = REGEX.match(server_version).group(1)
        server_split = server_cleaned.split(".")

        rv = (client_split < server_split)
        if verbose:
            LOG.info("Client=%20s (%-5s)  v.  Server=%20s (%-5s) Upgrade? %s", \
                    client_version, ".".join(client_split), \
                    server_version, ".".join(server_split), rv)
        return rv

    except:
        LOG.warn("Bad versions: client=%s server=%s", client_version, server_version, exc_info = 1)
        return True

if __name__ == "__main__":

    import sys
    args = list(sys.argv[1:])

    if "--quiet" in args:
        args.remove("--quiet")
        logging.basicConfig(level=logging.WARN)
    else:
        logging.basicConfig(level=logging.DEBUG)

    if "--test" in args:
        print "="*10, "Test", "="*72
        needs_upgrade("4.0", "4.1.1", True)
        needs_upgrade("4.1", "4.1.1", True)
        needs_upgrade("4.1.0", "4.1.1", True)
        needs_upgrade("4.1.0", "4.1.1-Dev", True)
        needs_upgrade("4.1.0-Dev", "4.1.1", True)
        needs_upgrade("4.1.1", "4.1.1", True)
        needs_upgrade("Beta-4.1", "4.1.1", True)
        needs_upgrade("Beta-4.1.0", "4.1.1", True)
        needs_upgrade("Beta-4.1.1", "4.1.1", True)
        needs_upgrade("4.1.1", "Beta-4.1.1", True)
        needs_upgrade("Beta-4.1.0", "Beta-4.1.1", True)
        needs_upgrade("4.1.1-Foo", "4.1.1", True)
        needs_upgrade("4.1.1-Foo", "4.1.1-Dev", True)
        needs_upgrade("4.1.1-Foo", "4.1.2-Dev", True)
        needs_upgrade("4.1.1-Foo", "4.2.0-Dev", True)
        needs_upgrade("4.1.1-Foo", "4.2", True)
        needs_upgrade("4.1.1-Foo", "5.0", True)
        needs_upgrade("v.4.1.1-Foo", "5.0", True)
        # Additions post git-describe
        needs_upgrade("v.4.1.1-Foo", "5.0", True)
        needs_upgrade("v4.1.1-Foo", "5.0", True)
        needs_upgrade("Beta-v4.1.1-Foo", "5.0", True)
        needs_upgrade("A1-4.1.1-Foo", "5.0", True)
        needs_upgrade("A1-v4.1.1-Foo", "5.0", True)
    else:
        try:
	    rv = int(needs_upgrade(args[0], args[1], True))
        except:
            rv = 2
            print """    %s [--quiet] client_version server_version
or: %s [--quiet] --test
	    """ % (sys.argv[0], sys.argv[0])
        sys.exit(rv)
