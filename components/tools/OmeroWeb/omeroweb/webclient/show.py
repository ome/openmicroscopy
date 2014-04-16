#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Generic functionality for handling particular links and "showing" objects
# in the OMERO.web tree view.
#
#
# Copyright (c) 2014 University of Dundee.
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

"""
Generic functionality for handling particular links and "showing" objects
in the OMERO.web tree view.
"""

from django.http import HttpResponseRedirect
from django.core.urlresolvers import reverse


class Show(object):
    """
    This object is used by most of the top-level pages.  The "show" and
    "path" query strings are used by this object to set direct OMERO.web to
    the correct locations in the hierarchy and selected the correct objects
    in that hierarchy.
    """

    # List of prefiexes that are at the top level of the tree
    TOP_LEVEL_PREFIXES = ('project', 'screen')

    # List of supported prefixes for the "path" query string variable
    SUPPORTED_PATH_PREFIXES = (
        'project', 'dataset', 'image', 'screen', 'plate', 'tag'
    )

    # List of supported prefixes for the "show" query string variable
    SUPPORTED_SHOW_PREFIXES = (
        'project', 'dataset', 'image', 'screen', 'plate', 'tag',
        'acquisition', 'run', 'well'
    )

    def __init__(self, conn, request, menu):
        # Whether or not the tree was initially open
        self.initially_open = None
        self.initially_open_owner = None
        # The first "path" to select
        self.initially_select = list()
        self.first_sel = None

        self.conn = conn
        self.request = request
        self.menu = menu
        # TODO
        if len(self.initially_select) > 1:
            print self.initially_select
            self.init_first_sel()

        # E.g. backwards compatible support for
        # path=project=51|dataset=502|image=607 (select the image)
        path = self.request.REQUEST.get('path', '')
        i = path.split("|")[-1]
        if i.split("=")[0] in self.SUPPORTED_PATH_PREFIXES:
            # Backwards compatible with image=607 etc
            self.initially_select.append(str(i).replace("=", '-'))

        # Now we support show=image-607|image-123  (multi-objects selected)
        show = self.request.REQUEST.get('show', '')
        for i in show.split("|"):
            if i.split("-")[0] in self.SUPPORTED_SHOW_PREFIXES:
                # 'run' is an alternative for 'acquisition'
                i = i.replace('run', 'acquisition')
                self.initially_select.append(str(i))

    def load_first_sel(self, first_obj, first_id):
        first_sel = None
        if first_obj == "tag":
            # Tags have an "Annotation" suffix to the object name
            first_sel = self.conn.getObject(
                "TagAnnotation", self.first_id
            )
        else:
            first_sel = self.conn.getObject(
                first_obj, first_id
            )

        if first_obj == "well":
            # Wells aren't in the tree, so we need parent
            parent_node = first_sel.getWellSample().getPlateAcquisition()
            parent_type = "acquisition"
            if parent_node is None:
                # No Acquisition for this well, use Plate instead
                parent_node = first_sel.getParent()
                parent_type = "plate"
            first_sel = parent_node
            self.initially_open = [
                "%s-%s" % (parent_type, parent_node.getId())
            ]
            self.initially_select = self.initially_open[:]
        self.initially_open_owner = first_sel.details.owner.id.val
        return first_sel

    def get_first_selected(self):
        if len(self.initially_select) == 0:
            return list()

        # tree hierarchy open to first selected object
        self.initially_open = [self.initially_select[0]]
        first_obj, first_id = self.initially_open[0].split("-", 1)
        # if we're showing a tag, make sure we're on the tags page...
        if first_obj == "tag" and self.menu != "usertags":
            return HttpResponseRedirect(
                reverse(viewname="load_template", args=['usertags']) +
                "?show=" + self.initially_select[0]
            )
        first_sel = None
        try:
            first_id = long(first_id)
            # Set context to 'cross-group'
            self.conn.SERVICE_OPTS.setOmeroGroup('-1')
            first_sel = self.load_first_sel(first_obj, first_id)
        except:
            pass
        if first_obj not in self.TOP_LEVEL_PREFIXES:
            # Need to see if first item has parents
            if first_sel is not None:
                for p in first_sel.getAncestry():
                    if self.first_obj == "tag":
                        # Parents of tags must be tags (no OMERO_CLASS)
                        self.initially_open.insert(0, "tag-%s" % p.getId())
                    else:
                        self.initially_open.insert(
                            0, "%s-%s" % (p.OMERO_CLASS.lower(), p.getId())
                        )
                        self.initially_open_owner = p.details.owner.id.val
                if self.initially_open.split("-")[0] == 'image':
                    self.initially_open.insert(0, "orphaned-0")
        return first_sel
