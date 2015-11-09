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
# * http://code.google.com/docreader/#p=isapi-wsgi&s=isapi-wsgi&t=IntegrationWithDjango # noqa
# * http://code.google.com/docreader/#p=isapi-wsgi&s=isapi-wsgi&t=ServingFromRoot       # noqa
#


import os
import sys
CWD = os.path.dirname(__file__)
OMERO_HOME = os.path.join(CWD, os.path.pardir, os.path.pardir)
CONFIG = os.path.join(OMERO_HOME, "etc", "grid", "config.xml")
LOGS = os.path.join(OMERO_HOME, "var", "log")
STATICS = os.path.join(OMERO_HOME, "lib", "python", "omeroweb", "static")
STATICS = os.path.realpath(STATICS)

sys.path.append(str(CWD))
sys.path.append(str(os.path.join(CWD, 'omeroweb')))

from omeroweb import settings

os.environ['DJANGO_SETTINGS_MODULE'] = 'omeroweb.settings'
import django.core.handlers.wsgi
import threading
lock = threading.Lock()


class SerialWSGIHandler (django.core.handlers.wsgi.WSGIHandler):

    def __call__(self, *args, **kwargs):
        try:
            lock.acquire()
            return super(SerialWSGIHandler, self).__call__(*args, **kwargs)
        finally:
            lock.release()


def get_wsgi_application():
    """
    The public interface to Django's WSGI support. Should return a WSGI
    callable.
    Allows us to avoid making django.core.handlers.WSGIHandler public API, in
    case the internal WSGI implementation changes or moves in the future.
    see https://github.com/django/django/blob/1.6.11/django/core/wsgi.py
    """
    # django.setup()
    return SerialWSGIHandler()


application = get_wsgi_application()

import isapi_wsgi
# The entry points for the ISAPI extension.


def __ExtensionFactory__():
    return isapi_wsgi.ISAPISimpleHandler(application)


def permit_iis(filename):
    """
    Allow IIS to access required OMERO.web files
    """

    from win32security import LookupAccountName, OBJECT_INHERIT_ACE
    from win32security import CONTAINER_INHERIT_ACE, GetNamedSecurityInfo
    from win32security import SE_FILE_OBJECT, DACL_SECURITY_INFORMATION
    from win32security import ACL_REVISION_DS, SetNamedSecurityInfo
    from win32file import FILE_ALL_ACCESS

    flags = OBJECT_INHERIT_ACE | CONTAINER_INHERIT_ACE
    iusr, domain, type = LookupAccountName("", "IUSR")

    fileSecDesc = GetNamedSecurityInfo(
        filename, SE_FILE_OBJECT, DACL_SECURITY_INFORMATION)
    fileDacl = fileSecDesc.GetSecurityDescriptorDacl()
    fileDacl.AddAccessAllowedAceEx(
        ACL_REVISION_DS, flags, FILE_ALL_ACCESS, iusr)
    SetNamedSecurityInfo(
        filename, SE_FILE_OBJECT, DACL_SECURITY_INFORMATION, None, None,
        fileDacl, None)

if __name__ == '__main__':

    static_prefix = settings.STATIC_URL.rstrip("/")
    try:
        web_prefix = settings.FORCE_SCRIPT_NAME.rstrip("/")
    except:
        web_prefix = "/omero"

    permit_iis(CONFIG)
    permit_iis(LOGS)

    # If run from the command-line, install ourselves.
    from isapi.install import ISAPIParameters, ScriptMapParams
    from isapi.install import VirtualDirParameters, HandleCommandLine
    params = ISAPIParameters()
    # Setup the virtual directories - this is a list of directories our
    # extension uses - in this case only 2.
    # The OMERO.web application extension has a "script map" - this is the
    # mapping of ISAPI extensions.
    sm = [
        ScriptMapParams(Extension="*", Flags=0)
    ]
    vd1 = VirtualDirParameters(Name=web_prefix,
                               Description="ISAPI-WSGI OMERO.web",
                               ScriptMaps=sm,
                               ScriptMapUpdate="replace"
                               )
    vd2 = VirtualDirParameters(Name=static_prefix,
                               Description="OMERO.web static files",
                               Path=STATICS,
                               AccessRead=True,
                               )
    params.VirtualDirs = [vd1, vd2]
    HandleCommandLine(params)
