#!/usr/bin/env python

#
# omero_web_iis.py - OMERO.web installer and ISAPI/WSGI handler for IIS
# 
# Copyright (c) 2010 University of Dundee. All rights reserved.
# 
# This software is distributed under the terms described by the LICENCE file
# you can find at the root of the distribution bundle, which states you are
# free to use it only for non commercial purposes.
# If the file is missing please request a copy by contacting
# ome-users@lists.openmicroscopy.org.uk.
#
# Author: Chris Allan <callan(at)blackcat.ca>
#
# Modified version of the example Django script and serving from the IIS root:
#  * http://code.google.com/docreader/#p=isapi-wsgi&s=isapi-wsgi&t=IntegrationWithDjango
#  * http://code.google.com/docreader/#p=isapi-wsgi&s=isapi-wsgi&t=ServingFromRoot
#

import os, sys
CWD = os.path.dirname(__file__)
sys.path.append(str(CWD))
sys.path.append(str(os.path.join(CWD, 'omeroweb')))
os.environ['DJANGO_SETTINGS_MODULE'] = 'omeroweb.settings'
import django.core.handlers.wsgi
application = django.core.handlers.wsgi.WSGIHandler()

import isapi_wsgi
# The entry points for the ISAPI extension.
def __ExtensionFactory__():
    return isapi_wsgi.ISAPISimpleHandler(application)

if __name__ == '__main__':
    # If run from the command-line, install ourselves.
    from isapi.install import *
    params = ISAPIParameters()
    # Setup the virtual directories - this is a list of directories our
    # extension uses - in this case only 1.
    # Each extension has a "script map" - this is the mapping of ISAPI
    # extensions.
    sm = [
        ScriptMapParams(Extension="*", Flags=0)
    ]
    vd = VirtualDirParameters(Name="/",
                              Description = "ISAPI-WSGI OMERO.web",
                              ScriptMaps = sm,
                              ScriptMapUpdate = "replace"
                              )
    params.VirtualDirs = [vd]
    HandleCommandLine(params)
