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
from datetime import datetime

logger = logging.getLogger(__name__)


class PlateGrid(object):
    """
    A PlateGrid object encapsulates a PlateI reference and provides a number of
    methods useful for displaying the contents of the plate as a grid.
    """

    def __init__(self, conn, pid, fid, xtra):
        t0 = datetime.now()
        self.plate = conn.getObject('plate', long(pid))
        t1 = datetime.now()
        logger.debug('time to get plate: %s' % (t1 - t0))
        self.field = fid
        self.xtra = xtra
        self._metadata = None

    @property
    def metadata(self):
        if self._metadata is None:
            t0 = datetime.now()
            self.plate.setGridSizeConstraints(8, 12)
            t1 = datetime.now()
            logger.debug('time to set grid constraints: %s' % (t1 - t0))
            t0 = t1

            grid = []
            wellGrid = self.plate.getWellGrid(self.field)
            t1 = datetime.now()
            logger.debug('time to get well grid: %s' % (t1 - t0))
            t0 = t1
            for row in wellGrid:
                tr = []
                for e in row:
                    if e:
                        i = e.getImage()
                        t2 = datetime.now()
                        logger.warn('time to get image: %s' % (t2 - t1))
                        t1 = t2
                        if i:
                            t = i.simpleMarshal(xtra=self.xtra)
                            t['wellId'] = e.getId()
                            t['field'] = self.field
                            tr.append(t)
                            t2 = datetime.now()
                            logger.debug(
                                'time to marshal image: %s' % (t2 - t1))
                            t1 = t2
                            continue
                    tr.append(None)
                grid.append(tr)
            t1 = datetime.now()
            logger.debug('time to get wells in grid: %s' % (t1 - t0))
            self._metadata = {'grid': grid,
                              'collabels': self.plate.getColumnLabels(),
                              'rowlabels': self.plate.getRowLabels()}
        return self._metadata
