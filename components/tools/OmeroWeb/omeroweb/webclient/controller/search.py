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

import datetime
import time
import omero
import logging

from omero.rtypes import rtime
from webclient.controller import BaseController

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
               useAcquisitionDate, date=None):

        # If fields contains 'annotation', we really want to search files too
        fields = set(fields)
        if "annotation" in fields:
            fields = fields.union(("file.name", "file.path", "file.format",
                                   "file.contents"))
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
                d1 = datetime.datetime.strptime(
                    p[0]+" 00:00:00", "%Y-%m-%d %H:%M:%S")
                d2 = datetime.datetime.strptime(
                    p[1]+" 23:59:59", "%Y-%m-%d %H:%M:%S")

                created = [rtime(long(time.mktime(d1.timetuple()) + 1e-6 *
                                      d1.microsecond) * 1000),
                           rtime(long(time.mktime(d2.timetuple()) + 1e-6 *
                                      d2.microsecond) * 1000)]
            else:
                d1 = datetime.datetime.strptime(
                    p[0]+" 00:00:00", "%Y-%m-%d %H:%M:%S")

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
                useAcquisitionDate=useAcquisitionDate))
            return obj_list

        self.containers = {}
        resultCount = 0
        self.searchError = None

        try:
            for dt in onlyTypes:
                dt = str(dt)
                if dt in ['projects', 'datasets', 'images', 'screens',
                          'plateacquisitions', 'plates']:
                    self.containers[dt] = doSearch(dt)
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
