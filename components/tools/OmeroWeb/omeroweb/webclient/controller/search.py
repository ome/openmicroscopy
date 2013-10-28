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

from omero.rtypes import *
from webclient.controller import BaseController

class BaseSearch(BaseController):

    images = None
    projects = None
    datasets = None
    imgSize = 0
    dsSize = 0
    prSize = 0
    
    c_size = 0

    def __init__(self, conn, **kw):
        BaseController.__init__(self, conn)

    def batch_search(self, query_list):
        im_list = list()
        well_list = list()
        for (plate_name, row, column) in query_list:
            row = int(row)
            column = int(column)
            well = self.conn.findWellInPlate(plate_name, row, column)
            if well is not None:
                well_list.append(well)
        for well in well_list:
            img=well.getWellSample().image()
            im_list.append(img)
    
        im_ids = [im.id for im in im_list]
        im_annotation_counter = self.conn.getCollectionCount("Image", "annotationLinks", im_ids)
    
        im_list_with_counters = list()
        for im in im_list:
            im.annotation_counter = im_annotation_counter.get(im.id)
            im_list_with_counters.append(im)
    
        self.containers = {'projects': list(), 'datasets': list(), 'images': im_list_with_counters, 'screens': list(), 'plates': list()}
        self.c_size = len(im_list_with_counters)

    
    def search(self, query, onlyTypes, date=None):
        created = None
        if date is not None:
            p = str(date).split('_')
            if len(p)>1:
                d1 = datetime.datetime.strptime(p[0]+" 00:00:00", "%Y-%m-%d %H:%M:%S")
                d2 = datetime.datetime.strptime(p[1]+" 23:59:59", "%Y-%m-%d %H:%M:%S")
            
                created = [rtime(long(time.mktime(d1.timetuple())+1e-6*d1.microsecond)*1000), rtime(long(time.mktime(d2.timetuple())+1e-6*d2.microsecond)*1000)]
            else:
                d1 = datetime.datetime.strptime(p[0]+" 00:00:00", "%Y-%m-%d %H:%M:%S")
            
        pr_list_with_counters = list()
        ds_list_with_counters = list()
        im_list_with_counters = list()
        sc_list_with_counters = list()
        pl_list_with_counters = list()
        for ot in onlyTypes:
            if ot == 'images':
                im_list = list(self.conn.searchObjects(["image"], query, created))
                
                im_ids = [im.id for im in im_list]
                im_annotation_counter = self.conn.getCollectionCount("Image", "annotationLinks", im_ids)

                im_list_with_counters = list()
                for im in im_list:
                    im.annotation_counter = im_annotation_counter.get(im.id)
                    im_list_with_counters.append(im)
            elif ot == 'datasets':
                ds_list = list(self.conn.searchObjects(["Dataset"], query, created))
                
                ds_ids = [ds.id for ds in ds_list]
                ds_annotation_counter = self.conn.getCollectionCount("Dataset", "annotationLinks", ds_ids)

                ds_list_with_counters = list()
                for ds in ds_list:
                    ds.annotation_counter = ds_annotation_counter.get(ds.id)
                    ds_list_with_counters.append(ds)
            elif ot == 'projects':
                pr_list = list(self.conn.searchObjects(["Project"], query, created))
                
                pr_ids = [pr.id for pr in pr_list]
                pr_annotation_counter = self.conn.getCollectionCount("Project", "annotationLinks", pr_ids)

                pr_list_with_counters = list()
                for pr in pr_list:
                    pr.annotation_counter = pr_annotation_counter.get(pr.id)
                    pr_list_with_counters.append(pr)
            elif ot == 'plates':
                pl_list = list(self.conn.searchObjects(["Plate"], query, created))
                pl_ids = [pl.id for pl in pl_list]
                pl_annotation_counter = self.conn.getCollectionCount("Plate", "annotationLinks", pl_ids)

                pl_list_with_counters = list()
                for pl in pl_list:
                    pl.annotation_counter = pl_annotation_counter.get(pl.id)
                    pl_list_with_counters.append(pl)
            elif ot == 'screens':
                sc_list = list(self.conn.searchObjects(["Screen"], query, created))
                
                sc_ids = [sc.id for sc in sc_list]
                sc_annotation_counter = self.conn.getCollectionCount("Screen", "annotationLinks", sc_ids)

                sc_list_with_counters = list()
                for sc in sc_list:
                    sc.annotation_counter = sc_annotation_counter.get(sc.id)
                    sc_list_with_counters.append(sc)
            
        self.containers={'projects': pr_list_with_counters, 'datasets': ds_list_with_counters, 'images': im_list_with_counters, 'screens': sc_list_with_counters, 'plates': pl_list_with_counters}
        
        self.c_size = len(pr_list_with_counters)+len(ds_list_with_counters)+len(im_list_with_counters)+len(sc_list_with_counters)+len(pl_list_with_counters)
