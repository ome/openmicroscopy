#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (c) 2014 University of Dundee & Open Microscopy Environment.
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

import omero
import re

from omero.rtypes import rint
from django.core.urlresolvers import reverse


class IncorrectMenuError(Exception):
    """Exception to signal that we are on the wrong menu."""

    def __init__(self, uri):
        """
        Constructs a new Exception instance.

        @param uri URI to redirect to.
        @type uri String
        """
        super(Exception, self).__init__()
        self.uri = uri


class Show(object):
    """
    This object is used by most of the top-level pages.  The "show" and
    "path" query strings are used by this object to both direct OMERO.web to
    the correct locations in the hierarchy and select the correct objects
    in that hierarchy.
    """

    # List of prefixes that are at the top level of the tree
    TOP_LEVEL_PREFIXES = ('project', 'screen')

    # List of supported object types
    SUPPORTED_OBJECT_TYPES = (
        'project', 'dataset', 'image', 'screen', 'plate', 'tag',
        'acquisition', 'run', 'well'
    )

    # Regular expression which declares the format for a "path" used either
    # in the "path" or "show" query string.  No modifications should be made
    # to this regex without corresponding unit tests in
    # "tests/unit/test_show.py".
    PATH_REGEX = re.compile(
        r'(?P<object_type>\w+)\.?(?P<key>\w+)?[-=](?P<value>[^\|]*)\|?'
    )

    # Regular expression for matching Well names
    WELL_REGEX = re.compile(
        '^(?:(?P<alpha_row>[a-zA-Z]+)(?P<digit_column>\d+))|'
        '(?:(?P<digit_row>\d+)(?P<alpha_column>[a-zA-Z]+))$'
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
        # show/select in the user interface.  May be modified if one or
        # more of the elements is not in the tree.  This is currently the
        # case for all Screen-Plate-Well hierarchy elements below Plate
        # (Well for example).
        self._initially_select = list()
        # The nodes of the tree that will be initially open based on the
        # nodes that are initially selected.
        self._initially_open = None
        # The owner of the node closest to the root of the tree from the
        # list of initially open nodes.
        self._initially_open_owner = None
        # First selected node from the requested initially open "paths"
        # that is first loaded on first retrieval of the "first_selected"
        # property.
        self._first_selected = None

        self.conn = conn
        self.request = request
        self.menu = menu

        path = self.request.REQUEST.get('path', '').split('|')[-1]
        self._add_if_supported(path)

        show = self.request.REQUEST.get('show', '')
        for path in show.split('|'):
            self._add_if_supported(path)

    def _add_if_supported(self, path):
        """Adds a path to the initially selected list if it is supported."""
        m = self.PATH_REGEX.match(path)
        if m is None:
            return
        object_type = m.group('object_type')
        key = m.group('key')
        value = m.group('value')
        if key is None:
            key = 'id'
        if object_type in self.SUPPORTED_OBJECT_TYPES:
            # 'run' is an alternative for 'acquisition'
            object_type = object_type.replace('run', 'acquisition')
            self._initially_select.append(
                '%s.%s-%s' % (object_type, key, value)
            )

    def _load_tag(self, attributes):
        """
        Loads a Tag based on a certain set of attributes from the server.

        @param attributes Set of attributes to filter on.
        @type attributes L{dict}
        """
        # Tags have an "Annotation" suffix added to the object name so
        # need to be loaded differently.
        return next(self.conn.getObjects(
            "TagAnnotation", attributes=attributes
        ))

    def get_well_row_column(self, well):
        """
        Retrieves a tuple of row and column as L{int} for a given Well name
        ("A1" or "1A") string.

        @param well Well name string to retrieve the row and column tuple for.
        @type well L{str}
        """
        m = self.WELL_REGEX.match(well)
        if m is None:
            return None
        # We are using an algorithm that expects alpha columns and digit
        # rows (like a spreadsheet).  is_reversed will be True if those
        # conditions are not met, signifying that the row and column
        # calculated needs to be reversed before returning.
        is_reversed = False
        if m.group('alpha_row') is not None:
            a = m.group('alpha_row').upper()
            b = m.group('digit_column')
            is_reversed = True
        else:
            a = m.group('alpha_column').upper()
            b = m.group('digit_row')

        # Convert base26 column string to number.  Adapted from XlsxWriter:
        #   * https://github.com/jmcnamara/XlsxWriter
        #     * xlsxwriter/utility.py
        n = 0
        column = 0
        for character in reversed(a):
            column += (ord(character) - ord('A') + 1) * (26 ** n)
            n += 1

        # Convert 1-index to zero-index
        row = int(b) - 1
        column -= 1

        if is_reversed:
            return column, row
        return row, column

    def _load_well(self, attributes):
        """
        Loads a Well based on a certain set of attributes from the server.

        @param attributes Set of attributes to filter on.
        @type attributes L{dict}
        """
        if 'id' in attributes:
            return self.conn.getObject('Well', attributes=attributes)
        if 'name' in attributes:
            row, column = self.get_well_row_column(attributes['name'])
            path = self.request.REQUEST.get('path', '')
            for m in self.PATH_REGEX.finditer(path):
                object_type = m.group('object_type')
                # May have 'run' here rather than 'acquisition' because
                # the path will not have been validated and replaced.
                if object_type not in ('plate', 'run', 'acquisition'):
                    continue
                # 'run' is an alternative for 'acquisition'
                object_type = object_type.replace('run', 'acquisition')

                # Try and load the potential parent first
                key = m.group('key')
                value = m.group('value')
                if key is None:
                    key = 'id'
                if key == 'id':
                    value = long(value)
                parent_attributes = {key: value}
                parent, = self.conn.getObjects(
                    object_type, attributes=parent_attributes
                )

                # Now use the parent to try and locate the Well
                query_service = self.conn.getQueryService()
                params = omero.sys.ParametersI()
                params.map['row'] = rint(row)
                params.map['column'] = rint(column)
                params.addId(parent.id)
                if object_type == 'plate':
                    db_row, = query_service.projection(
                        'select w.id from Well as w '
                        'where w.row = :row and w.column = :column '
                        'and w.plate.id = :id', params, self.conn.SERVICE_OPTS
                    )
                if object_type == 'acquisition':
                    db_row, = query_service.projection(
                        'select distinct w.id from Well as w '
                        'join w.wellSamples as ws '
                        'where w.row = :row and w.column = :column '
                        'and ws.plateAcquisition.id = :id',
                        params, self.conn.SERVICE_OPTS
                    )
                well_id, = db_row
                return self.conn.getObject(
                    'Well', well_id.val
                )

    def _load_first_selected(self, first_obj, attributes):
        """
        Loads the first selected object from the server.  Will raise
        L{IncorrectMenuError} if the initialized menu was incorrect for
        the loaded objects.

        @param first_obj Type of the first selected object.
        @type first_obj String
        @param attributes Set of attributes to filter on.
        @type attributes L{dict}
        """
        first_selected = None
        if first_obj == "tag":
            first_selected = self._load_tag(attributes)
        elif first_obj == "well":
            first_selected = self._load_well(attributes)
        else:
            # All other objects can be loaded by type and attributes.
            first_selected, = self.conn.getObjects(
                first_obj, attributes=attributes
            )

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
                # No WellSample for this well, try and retrieve the
                # PlateAcquisition from the parent Plate.
                plate = first_selected.getParent()
                try:
                    parent_node, = plate.listPlateAcquisitions()
                    parent_type = "acquisition"
                except ValueError:
                    # No PlateAcquisition for this well, use Plate instead
                    parent_node = plate
                    parent_type = "plate"
            # Tree hierarchy open to first selected "real" object available
            # in the tree.
            self._initially_open = [
                "%s-%s" % (parent_type, parent_node.getId()),
                "%s-%s" % (first_obj, first_selected.getId())
            ]
            first_selected = parent_node
            self._initially_select = self._initially_open[:]
        else:
            # Tree hierarchy open to first selected object.
            self._initially_open = [
                '%s-%s' % (first_obj, first_selected.getId())
            ]
            # support for multiple objects selected by ID,
            # E.g. show=image-1|image-2
            if 'id' in attributes.keys() and len(self._initially_select) > 1:
                # 'image.id-1' -> 'image-1'
                self._initially_select = [
                    i.replace(".id", "") for i in self._initially_select]
            else:
                # Only select a single object
                self._initially_select = self._initially_open[:]

        self._initially_open_owner = first_selected.details.owner.id.val
        return first_selected

    def _find_first_selected(self):
        """Finds the first selected object."""
        if len(self._initially_select) == 0:
            return None

        # tree hierarchy open to first selected object
        m = self.PATH_REGEX.match(self._initially_select[0])
        if m is None:
            return None
        first_obj = m.group('object_type')
        # if we're showing a tag, make sure we're on the tags page...
        if first_obj == "tag" and self.menu != "usertags":
            raise IncorrectMenuError(
                reverse(viewname="load_template", args=['usertags']) +
                "?show=" + self._initially_select[0]
            )
        first_selected = None
        try:
            key = m.group('key')
            value = m.group('value')
            if key == 'id':
                value = long(value)
            attributes = {key: value}
            # Set context to 'cross-group'
            self.conn.SERVICE_OPTS.setOmeroGroup('-1')
            first_selected = self._load_first_selected(first_obj, attributes)
        except:
            pass
        if first_obj not in self.TOP_LEVEL_PREFIXES:
            # Need to see if first item has parents
            if first_selected is not None:
                for p in first_selected.getAncestry():
                    # If 'Well' is a parent, we have stared with Image.
                    # We want to start again at 'Well' to _load_first_selected with
                    # well, so we get 'acquisition' in ancestors.
                    if p.OMERO_CLASS == "Well":
                        self._initially_select = ['well.id-%s' % p.getId()]
                        return self._find_first_selected()
                    if first_obj == "tag":
                        # Parents of tags must be tags (no OMERO_CLASS)
                        self._initially_open.insert(0, "tag-%s" % p.getId())
                    else:
                        self._initially_open.insert(
                            0, "%s-%s" % (p.OMERO_CLASS.lower(), p.getId())
                        )
                        self._initially_open_owner = p.details.owner.id.val
                m = self.PATH_REGEX.match(self._initially_open[0])
                if m.group('object_type') == 'image':
                    self._initially_open.insert(0, "orphaned-0")
        return first_selected

    @property
    def first_selected(self):
        """
        Retrieves the first selected object.  The first time this method is
        invoked on the instance the actual retrieval is performed.  All other
        invocations retrieve the same instance without server interaction.
        Will raise L{IncorrectMenuError} if the initialized menu was
        incorrect for the loaded objects.
        """
        if self._first_selected is None:
            self._first_selected = self._find_first_selected()
        return self._first_selected

    @property
    def initially_select(self):
        """
        Retrieves the list of "paths" ("type-id") we have been requested to
        show/select in the user interface.  May be different than we were
        first initialised with due to certain nodes of the Screen-Plate-Well
        hierachy not being present in the tree.  Should not be invoked until
        after first retrieval of the L{Show.first_selected} property.
        """
        return self._initially_select

    @property
    def initially_open(self):
        """
        Retrieves the nodes of the tree that will be initially open based on
        the nodes that are initially selected.  Should not be invoked until
        after first retrieval of the L{Show.first_selected} property.
        """
        return self._initially_open

    @property
    def initially_open_owner(self):
        """
        Retrieves the owner of the node closest to the root of the tree from
        the list of initially open nodes.  Should not be invoked until
        after first retrieval of the L{Show.first_selected} property.
        """
        return self._initially_open_owner
