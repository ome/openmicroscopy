#!/usr/bin/env python
# 
# 
# 
# Copyright (c) 2008 University of Dundee. 
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
from omero.rtypes import *

from webclient.controller import BaseController

class BaseSearch(BaseController):

    criteria = None

    images = None
    projects = None
    datasets = None
    imgSize = 0
    dsSize = 0
    prSize = 0
    
    resultsSize = 0

    def __init__(self, conn, **kw):
        BaseController.__init__(self, conn)
        self.eContext['breadcrumb'] = ['Search']
        self.criteria = dict()

    def search(self, query, onlyTypes, period):
        self.url = "&query=%s&dateperiodinput=%s" % (query, period, )
        created = None
        if period != "":
            self.criteria['period'] = period
            p = str(period).split('_')
            # only for python 2.5
            # d1 = datetime.strptime(p[0]+" 00:00:00", "%Y-%m-%d %H:%M:%S") 
            # d2 = datetime.strptime(p[1]+" 23:59:59", "%Y-%m-%d %H:%M:%S") 
            d1 = datetime.datetime(*(time.strptime((p[0]+" 00:00:00"), "%Y-%m-%d %H:%M:%S")[0:6]))
            d2 = datetime.datetime(*(time.strptime((p[1]+" 23:59:59"), "%Y-%m-%d %H:%M:%S")[0:6]))
            
            created = [rtime(long(time.mktime(d1.timetuple())+1e-6*d1.microsecond)*1000), rtime(long(time.mktime(d2.timetuple())+1e-6*d2.microsecond)*1000)]

        self.criteria['query'] = query
        for ot in onlyTypes:
            if ot == 'image':
                self.criteria['image'] = 'CHECKED'
                self.images = list(self.conn.searchImages(query, created))
                self.imgSize = len(self.images)
                self.url = self.url + "&image=on"
            elif ot == 'dataset':
                self.criteria['dataset'] = 'CHECKED'
                self.datasets = list(self.conn.searchDatasets(query, created))
                self.dsSize = len(self.datasets)
                self.url = self.url + "&dataset=on"
            elif ot == 'project':
                self.criteria['project'] = 'CHECKED'
                self.projects = list(self.conn.searchProjects(query, created))
                self.prSize = len(self.projects)
                self.url = self.url + "&project=on"
        self.resultsSize = self.imgSize + self.dsSize + self.prSize

    def getThumbnails(self):
        pixelsIds = list()
        for im in self.images:
            for px in im.pixels:
                pixelsIds.append(px.id.val)
        print pixelsIds
        return self.conn.getThumbnailsSet(pixelsIds)

