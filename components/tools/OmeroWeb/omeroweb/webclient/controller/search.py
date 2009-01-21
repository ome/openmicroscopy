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
                im_list = list(self.conn.searchImages(query, created))
                
                im_ids = [im.id for im in im_list]
                im_annotation_counter = self.conn.getCollectionCount("Image", "annotationLinks", im_ids)

                im_list_with_counters = list()
                for im in im_list:
                    im.annotation_counter = im_annotation_counter.get(im.id)
                    im_list_with_counters.append(im)
                
                self.images = im_list_with_counters
                self.imgSize = len(self.images)
                self.url = self.url + "&image=on"
            elif ot == 'dataset':
                self.criteria['dataset'] = 'CHECKED'
                ds_list = list(self.conn.searchDatasets(query, created))
                
                ds_ids = [ds.id for ds in ds_list]
                ds_child_counter = self.conn.getCollectionCount("Dataset", "imageLinks", ds_ids)
                ds_annotation_counter = self.conn.getCollectionCount("Dataset", "annotationLinks", ds_ids)

                ds_list_with_counters = list()
                for ds in ds_list:
                    ds.child_counter = ds_child_counter.get(ds.id)
                    ds.annotation_counter = ds_annotation_counter.get(ds.id)
                    ds_list_with_counters.append(ds)
                
                self.datasets = ds_list_with_counters
                self.dsSize = len(self.datasets)
                self.url = self.url + "&dataset=on"
            elif ot == 'project':
                self.criteria['project'] = 'CHECKED'
                pr_list = list(self.conn.searchProjects(query, created))
                
                pr_ids = [pr.id for pr in pr_list]
                pr_child_counter = self.conn.getCollectionCount("Project", "datasetLinks", pr_ids)
                pr_annotation_counter = self.conn.getCollectionCount("Project", "annotationLinks", pr_ids)

                pr_list_with_counters = list()
                for pr in pr_list:
                    pr.child_counter = pr_child_counter.get(pr.id)
                    pr.annotation_counter = pr_annotation_counter.get(pr.id)
                    pr_list_with_counters.append(pr)
                
                self.projects = pr_list_with_counters
                self.prSize = len(self.projects)
                self.url = self.url + "&project=on"
        self.resultsSize = self.imgSize + self.dsSize + self.prSize

