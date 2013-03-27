#!/usr/bin/env python
# -*- coding: utf-8 -*-

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
OMERO_HOME = os.path.join(CWD, os.path.pardir, os.path.pardir)
CONFIG = os.path.join(OMERO_HOME, "etc", "grid", "config.xml")
LOGS = os.path.join(OMERO_HOME, "var", "log")
STATICS = os.path.join(OMERO_HOME, "lib", "python", "omeroweb", "static")
STATICS = os.path.realpath(STATICS)

sys.path.append(str(CWD))
sys.path.append(str(os.path.join(CWD, 'omeroweb')))

os.environ['DJANGO_SETTINGS_MODULE'] = 'omeroweb.settings'
import django.core.handlers.wsgi
import threading
lock = threading.Lock()

class SerialWSGIHandler (django.core.handlers.wsgi.WSGIHandler):
    def __call__ (self, *args, **kwargs):
        try:
            lock.acquire()
            return super(SerialWSGIHandler, self).__call__(*args, **kwargs)
        finally:
            lock.release()
        
application = SerialWSGIHandler()

import isapi_wsgi
# The entry points for the ISAPI extension.
def __ExtensionFactory__():
    return isapi_wsgi.ISAPISimpleHandler(application)

def permit_iis(filename):
    """
    Allow IIS to access required OMERO.web files
    """

    from win32security import LookupAccountName, GetFileSecurity, OBJECT_INHERIT_ACE
    from win32security import CONTAINER_INHERIT_ACE, GetNamedSecurityInfo, SE_FILE_OBJECT
    from win32security import DACL_SECURITY_INFORMATION, ACL_REVISION_DS, SetNamedSecurityInfo
    from win32file import FILE_ALL_ACCESS

    flags = OBJECT_INHERIT_ACE | CONTAINER_INHERIT_ACE
    iusr, domain, type = LookupAccountName("", "IUSR")

    fileSecDesc = GetNamedSecurityInfo(filename, SE_FILE_OBJECT, DACL_SECURITY_INFORMATION)
    fileDacl = fileSecDesc.GetSecurityDescriptorDacl()
    fileDacl.AddAccessAllowedAceEx(ACL_REVISION_DS, flags, FILE_ALL_ACCESS, iusr)
    SetNamedSecurityInfo(filename, SE_FILE_OBJECT, DACL_SECURITY_INFORMATION, None, None, fileDacl, None )

if __name__ == '__main__':

    permit_iis(CONFIG)
    permit_iis(LOGS)

    # If run from the command-line, install ourselves.
    from isapi.install import *
    params = ISAPIParameters()
    # Setup the virtual directories - this is a list of directories our
    # extension uses - in this case only 2.
    # The OMERO.web application extension has a "script map" - this is the
    # mapping of ISAPI extensions.
    sm = [
        ScriptMapParams(Extension="*", Flags=0)
    ]
    vd1 = VirtualDirParameters(Name="/omero",
                              Description = "ISAPI-WSGI OMERO.web",
                              ScriptMaps = sm,
                              ScriptMapUpdate = "replace"
                              )
    vd2 = VirtualDirParameters(Name="/static",
                              Description = "OMERO.web static files",
                              Path=STATICS,
                              AccessRead=True,
                              )
    params.VirtualDirs = [vd1, vd2]
    HandleCommandLine(params)
