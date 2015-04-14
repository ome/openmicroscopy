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
from omero.rtypes import rint, rlong

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
            grid = [[None] * size['columns']] * size['rows']

            q = self._conn.getQueryService()
            params = omero.sys.Parameters()
            params.map = {'pid': rlong(self.plate.id),
                          'wsidx': rint(self.field)}
            query = ' '.join([
                    "select well.row, well.column,",  # Grid index
                    "img.id,",                        # 'id'
                    "img.name,",                      # 'name'
                    "img.details.owner.firstName||' '",
                    "||img.details.owner.lastName,",  # 'author'
                    "well.id,",                       # 'wellId'
                    "img.acquisitionDate",            # 'date'
                    "from Well well",
                    "join well.wellSamples ws",
                    "join ws.image img",
                    "where well.plate.id = :pid",     # plate ID
                    "and index(ws) = :wsidx"          # field
                ])

            wellGrid = q.projection(query, params, self._conn.SERVICE_OPTS)
            for w in wellGrid:
                gridRow = w[0].val
                gridCol = w[1].val
                wellmeta = {'type': 'Image',
                            'id': w[2].val,
                            'name': w[3].val,
                            'author': w[4].val,
                            'wellId': w[5].val,
                            'field': self.field}

                date = dt.fromtimestamp(w[6].val / 1000)
                wellmeta['date'] = time.mktime(date.timetuple())
                if callable(self._thumbprefix):
                    wellmeta['thumb_url'] = self._thumbprefix(str(w[2].val))
                else:
                    wellmeta['thumb_url'] = self._thumbprefix + str(w[2].val)

                grid[gridRow][gridCol] = wellmeta

            self._metadata = {'grid': grid,
                              'collabels': self.plate.getColumnLabels(),
                              'rowlabels': self.plate.getRowLabels()}
        return self._metadata
