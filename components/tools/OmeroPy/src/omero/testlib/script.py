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

    def get_script(self, path):
        script_service = self.root.sf.getScriptService()
        script = _get_script(script_service, path)
        if script is None:
            return -1
        return script.getId().getValue()


def run_script(client, script_id, args, key=None):
    script_service = client.sf.getScriptService()
    proc = script_service.runScript(script_id, args, None)
    try:
        cb = omero.scripts.ProcessCallbackI(client, proc)
        while not cb.block(1000):  # ms.
            pass
        cb.close()
        results = proc.getResults(0)    # ms
    finally:
        proc.close(False)

    if 'stdout' in results:
        orig_file = results['stdout'].getValue()
        v = "Script generated StdOut in file:", orig_file.getId().getValue()
        logging.debug(v)
        assert orig_file.id.val > 0
    if 'stderr' in results:
        orig_file = results['stderr'].getValue()
        v = "Script generated StdErr in file:", orig_file.getId().getValue()
        logging.debug(v)
        assert orig_file.getId().getValue() > 0
    if key and key in results:
        return results[key]


def _get_script(script_service, script_path):
    """ Utility method, return the script or None """
    scripts = script_service.getScripts()     # returns list of OriginalFiles

    # make sure path starts with a slash.
    # ** If you are a Windows client - will need to convert all path separators
    #    to "/" since server stores /path/to/script.py **
    if not script_path.startswith("/"):
        script_path = "/" + script_path

    named_scripts = [
        s for s in scripts if
        s.getPath().getValue() + s.getName().getValue() == script_path]

    if len(named_scripts) == 0:
        return None

    return named_scripts[0]


def points_to_string(points):
    """ Returns legacy format supported by Insight """
    points = ["%s,%s" % (p[0], p[1]) for p in points]
    csv = ", ".join(points)
    return "points[%s] points1[%s] points2[%s]" % (csv, csv, csv)


def check_file_annotation(client, file_annotation,
                          parent_type="Image", is_linked=True,
                          file_name=None):
    """
    Check validity of file annotation. If hasFileAnnotation, check the size,
    name and number of objects linked to the original file.
    """
    assert file_annotation is not None
    orig_file = file_annotation.getValue().getFile()
    assert orig_file.getSize().getValue() > 0
    assert orig_file.getName().getValue() is not None
    id = file_annotation.getValue().getId().getValue()
    assert id > 0

    conn = BlitzGateway(client_obj=client)
    wrapper = conn.getObject("FileAnnotation", id)
    name = None
    if file_name is not None:
        name = wrapper.getFile().getName()

    links = sum(1 for i in wrapper.getParentLinks(parent_type))
    conn.close()

    if is_linked:
        assert links == 1
    else:
        assert links == 0

    if file_name is not None:
        assert name == file_name
