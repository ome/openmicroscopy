#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

import omero
import logging
from omero.testlib import ITest
from omero.gateway import BlitzGateway


class ScriptTest(ITest):

    def getScript(self, path):
        scriptService = self.root.sf.getScriptService()
        script = _getScript(scriptService, path)
        if script is None:
            return -1
        return script.id.val


def runScript(client, scriptId, argMap, returnKey=None):
    scriptService = client.sf.getScriptService()
    proc = scriptService.runScript(scriptId, argMap, None)
    try:
        cb = omero.scripts.ProcessCallbackI(client, proc)
        while not cb.block(1000):  # ms.
            pass
        cb.close()
        results = proc.getResults(0)    # ms
    finally:
        proc.close(False)

    if 'stdout' in results:
        origFile = results['stdout'].getValue()
        v = "Script generated StdOut in file:", origFile.getId().getValue()
        logging.debug(v)
        print v
    if 'stderr' in results:
        origFile = results['stderr'].getValue()
        v = "Script generated StdErr in file:", origFile.getId().getValue()
        logging.debug(v)
        print v
    if returnKey and returnKey in results:
        return results[returnKey]


def _getScript(scriptService, scriptPath):
    """ Utility method, return the script or None """
    scripts = scriptService.getScripts()     # returns list of OriginalFiles

    # make sure path starts with a slash.
    # ** If you are a Windows client - will need to convert all path separators
    #    to "/" since server stores /path/to/script.py **
    if not scriptPath.startswith("/"):
        scriptPath = "/" + scriptPath

    namedScripts = [
        s for s in scripts if s.path.val + s.name.val == scriptPath]

    if len(namedScripts) == 0:
        return None

    if len(namedScripts) > 1:
        v = "Found more than one script with specified path: %s" % scriptPath
        logging.debug(v)

    return namedScripts[0]


def pointsToString(points):
    """ Returns legacy format supported by Insight """
    points = ["%s,%s" % (p[0], p[1]) for p in points]
    csv = ", ".join(points)
    return "points[%s] points1[%s] points2[%s]" % (csv, csv, csv)


def checkFileAnnotation(client, fileAnnotation, hasFileAnnotation=True,
                        parentType="Image", isLinked=True):
    """
    Check validity of file annotation. If hasFileAnnotation, check the size,
    name and number of objects linked to the original file.
    """
    if hasFileAnnotation:
        assert fileAnnotation is not None
        assert fileAnnotation.val._file._size._val > 0
        assert fileAnnotation.val._file._name._val is not None
        assert fileAnnotation.val.id.val > 0
        # session is closed during teardown
        conn = BlitzGateway(client_obj=client)

        faWrapper = conn.getObject("FileAnnotation", fileAnnotation.val.id.val)
        nLinks = sum(1 for i in faWrapper.getParentLinks(parentType))
        if isLinked:
            assert nLinks == 1
        else:
            assert nLinks == 0
        conn.close()
    else:
        assert fileAnnotation is None
