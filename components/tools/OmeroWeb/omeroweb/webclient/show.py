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
    "path" query strings are used by this object to both direct OMERO.web to
    the correct locations in the hierarchy and select the correct objects
    in that hierarchy.
    """

    # List of prefixes that are at the top level of the tree
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
        """
        Constructs a Show instance.  The instance will not be fully
        initialised until the first retrieval of the L{Show.first_selected}
        property.

        @param conn OMERO gateway.
        @type conn L{omero.gateway.BlitzGateway}
        @param request Django HTTP request.
        @type request L{django.http.HttpRequest}
        @param menu Literal representing the current menu we are on.
        @type menu String
        """
        # The list of "paths" ("type-id") we have been requested to
        # show/select in the user interface.
        self.initially_select = list()
        # The nodes of the tree that will be initially open based on the
        # nodes that are initially selected.
        self.initially_open = None
        # The owner of the node closest to the root of the tree from the
        # list of initially open nodes.
        self.initially_open_owner = None
        # First selected node from the requested initially open "paths"
        # that is first loaded on first retrieval of the "first_selected"
        # property.
        self._first_selected = None

        self.conn = conn
        self.request = request
        self.menu = menu

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

    def _load_first_selected(self, first_obj, first_id):
        """
        Loads the first selected object from the server.

        @param first_obj Type of the first selected object.
        @type first_obj String
        @param first_id ID of the first selected object.
        @type first_id Long
        """
        first_selected = None
        if first_obj == "tag":
            # Tags have an "Annotation" suffix added to the object name so
            # need to be loaded differently.
            first_selected = self.conn.getObject("TagAnnotation", first_id)
        else:
            # All other objects can be loaded by prefix and id.
            first_selected = self.conn.getObject(first_obj, first_id)

        if first_obj == "well":
            # Wells aren't in the tree, so we need to look up the parent
            well_sample = first_selected.getWellSample()
            parent_node = None
            parent_type = None
            # It's possible that the Well that we've been requested to show
            # has no fields (WellSample instances).  In that case the Plate
            # will be used but we don't have much choice.
            if well_sample is not None:
                parent_node = well_sample.getPlateAcquisition()
                parent_type = "acquisition"
            if parent_node is None:
                # No PlateAcquisition for this well, use Plate instead
                parent_node = first_selected.getParent()
                parent_type = "plate"
            first_selected = parent_node
            self.initially_open = [
                "%s-%s" % (parent_type, parent_node.getId())
            ]
            self.initially_select = self.initially_open[:]
        self.initially_open_owner = first_selected.details.owner.id.val
        return first_selected

    def _find_first_selected(self):
        """Finds the first selected object."""
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
        first_selected = None
        try:
            first_id = long(first_id)
            # Set context to 'cross-group'
            self.conn.SERVICE_OPTS.setOmeroGroup('-1')
            first_selected = self._load_first_selected(first_obj, first_id)
        except:
            pass
        if first_obj not in self.TOP_LEVEL_PREFIXES:
            # Need to see if first item has parents
            if first_selected is not None:
                for p in first_selected.getAncestry():
                    if first_obj == "tag":
                        # Parents of tags must be tags (no OMERO_CLASS)
                        self.initially_open.insert(0, "tag-%s" % p.getId())
                    else:
                        self.initially_open.insert(
                            0, "%s-%s" % (p.OMERO_CLASS.lower(), p.getId())
                        )
                        self.initially_open_owner = p.details.owner.id.val
                if self.initially_open[0].split("-")[0] == 'image':
                    self.initially_open.insert(0, "orphaned-0")
        return first_selected

    @property
    def first_selected(self):
        """
        Retrieves the first selected object.  The first time this method is
        invoked on the instance the actual retrieval is performed.  All other
        invocations retrieve a the same instance without server interaction.
        """
        if self._first_selected is None:
            self._first_selected = self._find_first_selected()
        return self._first_selected
