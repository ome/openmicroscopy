# -*- coding: utf-8 -*-

# Copyright (C) 2015 Glencoe Software, Inc.
# All rights reserved.
#
# Use is subject to license terms supplied in LICENSE.txt

"""
   Module to encapsulate operations concerned with displaying the contents of a
   plate as a grid.
"""

import logging
from datetime import datetime as dt
import time

import omero.sys
from omero.rtypes import rint

logger = logging.getLogger(__name__)


class PlateGrid(object):
    """
    A PlateGrid object encapsulates a PlateI reference and provides a number of
    methods useful for displaying the contents of the plate as a grid.
    """

    def __init__(self, conn, pid, fid, thumbprefix=''):
        self.plate = conn.getObject('plate', long(pid))
        self._conn = conn
        self.field = fid
        self._thumbprefix = thumbprefix
        self._metadata = None

    @property
    def metadata(self):
        if self._metadata is None:
            self.plate.setGridSizeConstraints(8, 12)
            size = self.plate.getGridSize()
            grid = [[None] * size['columns'] for _ in range(size['rows'])]

            q = self._conn.getQueryService()
            params = omero.sys.ParametersI()
            params.addId(self.plate.id)
            params.add('wsidx', rint(self.field))
            query = "select well.row, well.column, img.id, img.name, "\
                    "author.firstName||' '||author.lastName, "\
                    "well.id, img.acquisitionDate "\
                    "from Well well "\
                    "join well.wellSamples ws "\
                    "join ws.image img "\
                    "join img.details.owner author "\
                    "where well.plate.id = :id "\
                    "and index(ws) = :wsidx"

            for res in q.projection(query, params, self._conn.SERVICE_OPTS):
                row, col, img_id, img_name, author, well_id, time = res
                wellmeta = {'type': 'Image',
                            'id': img_id.val,
                            'name': img_name.val,
                            'author': author.val,
                            'date': time.val / 1000,
                            'wellId': well_id.val,
                            'field': self.field}

                if callable(self._thumbprefix):
                    wellmeta['thumb_url'] = self._thumbprefix(str(img_id.val))
                else:
                    wellmeta['thumb_url'] = self._thumbprefix + str(img_id.val)

                grid[row.val][col.val] = wellmeta

            self._metadata = {'grid': grid,
                              'collabels': self.plate.getColumnLabels(),
                              'rowlabels': self.plate.getRowLabels()}
        return self._metadata
