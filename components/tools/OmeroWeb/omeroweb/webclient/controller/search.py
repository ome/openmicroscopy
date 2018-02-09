#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#
#
# Copyright (c) 2008-2011 University of Dundee.
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
# Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>, 2008.
#
# Version: 1.0
#

import time
import omero
import logging

from omero.rtypes import rtime
from webclient.controller import BaseController
from webclient.webclient_utils import getDateTime

logger = logging.getLogger(__name__)


class BaseSearch(BaseController):

    images = None
    projects = None
    datasets = None
    imgSize = 0
    dsSize = 0
    prSize = 0

    c_size = 0
    # Indicates that search returned a full page of batchSize
    moreResults = False

    def __init__(self, conn, **kw):
        BaseController.__init__(self, conn)

    def search(self, query, onlyTypes, fields, searchGroup, ownedBy,
               useAcquisitionDate, date=None, rawQuery=False):
        # If rawQuery, the raw lucene query is used in search.byFullText()
        # and fields is ignored.
        # If fields contains 'annotation', we really want to search files too
        # docs.openmicroscopy.org/latest/omero/developers/Modules/Search.html
        fields = set(fields)
        if "annotation" in fields or "file" in fields:
            fields = fields.union(("file.name", "file.path", "file.format",
                                   "file.contents"))
            fields.discard("file")      # Not supported
        fields = list(fields)

        if 'plates' in onlyTypes:
            onlyTypes.append('plateacquisitions')
        created = None
        self.moreResults = False
        batchSize = 500
        if len(onlyTypes) == 1:
            batchSize = 1000
        if date is not None:
            p = str(date).split('_')
            if len(p) > 1:
                try:
                    d1 = getDateTime(p[0]+" 00:00:00")
                    d2 = getDateTime(p[1]+" 23:59:59")

                    created = [rtime(long(time.mktime(d1.timetuple()) + 1e-6 *
                                          d1.microsecond) * 1000),
                               rtime(long(time.mktime(d2.timetuple()) + 1e-6 *
                                          d2.microsecond) * 1000)]
                except ValueError:
                    # User entered an invalid date format - Ignore
                    pass
            else:
                msg = "date should be start_end, e.g. 2015-12-01_2016-01-01"
                raise ValueError(msg)

        def doSearch(searchType):
            """ E.g. searchType is 'images' """
            objType = searchType[0:-1]  # remove 's'

            obj_list = list(self.conn.searchObjects(
                [objType],
                query,
                created,
                fields=fields,
                batchSize=batchSize,
                searchGroup=searchGroup,
                ownedBy=ownedBy,
                useAcquisitionDate=useAcquisitionDate,
                rawQuery=rawQuery),
                )
            return obj_list

        self.containers = {}
        resultCount = 0
        self.searchError = None
        self.iids = []

        try:
            for dt in onlyTypes:
                dt = str(dt)
                if dt in ['projects', 'datasets', 'images', 'screens',
                          'plateacquisitions', 'plates', 'wells']:
                    self.containers[dt] = doSearch(dt)
                    if dt == "wells":
                        for well in self.containers[dt]:
                            well.name = "%s - %s" %\
                                        (well.listParents()[0].name,
                                         well.getWellPos())
                    if dt == 'images':
                        self.iids = [i.id for i in self.containers[dt]]
                    # If we get a full page of results, we know there are more
                    if len(self.containers[dt]) == batchSize:
                        self.moreResults = True
                    resultCount += len(self.containers[dt])
        except Exception, x:
            logger.info("Search Exception: %s" % x.message)
            if isinstance(x, omero.ServerError):
                # Only show message to user if we can be helpful
                if "TooManyClauses" in x.message:
                    self.searchError = (
                        "Please try to narrow down your query. The wildcard"
                        " matched too many terms.")
                elif ":" in query:
                    self.searchError = (
                        "There was an error parsing your query."
                        " Colons ':' are reserved for searches of"
                        " key-value annotations in the form: 'key:value'.")

        self.c_size = resultCount
