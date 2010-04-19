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

from omero.rtypes import *
from django.core.urlresolvers import reverse

from omero.model import CommentAnnotationI, UriAnnotationI, LongAnnotationI, TagAnnotationI, \
                        FileAnnotationI, OriginalFileI, ImageAnnotationLinkI, DatasetAnnotationLinkI, \
                        ProjectAnnotationLinkI, PlateAnnotationLinkI, ScreenAnnotationLinkI, \
                        DatasetI, ProjectI, ScreenI, PlateI, DatasetImageLinkI, ProjectDatasetLinkI, \
                        ScreenPlateLinkI, PermissionsI

from webclient.controller import BaseController

class BaseContainer(BaseController):
    
    project = None
    screen = None
    dataset = None
    plate = None
    well = None
    image = None
    tag = None
    comment = None
    
    tags = None
    
    containers = None
    experimenter = None
    
    c_size = 0
    
    text_annotations = None
    txannSize = 0
    long_annotations = None
    url_annotations = None
    file_annotations = None
    urlannSize = 0
    
    orphaned = False
    
    def __init__(self, conn, o1_type=None, o1_id=None, o2_type=None, o2_id=None, o3_type=None, o3_id=None, metadata=False, tags=None, rtags=None, index=None, **kw):
        BaseController.__init__(self, conn)
        if o1_type == "project":
            self.project = self.conn.getProject(o1_id)
            if self.project is None:
                raise AttributeError("We are sorry, but that project does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you.")
            if self.project._obj is None:
                raise AttributeError("We are sorry, but that project does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you.")
            if o2_type == "dataset":
                self.dataset = self.conn.getDataset(o2_id)
                if self.dataset is None:
                    raise AttributeError("We are sorry, but that dataset does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you.")
                if self.dataset._obj is None:
                    raise AttributeError("We are sorry, but that dataset does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you.")
        elif o1_type == "screen":
            self.screen = self.conn.getScreen(o1_id)
            if self.screen is None:
                raise AttributeError("We are sorry, but that screen does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you.")
            if self.screen._obj is None:
                raise AttributeError("We are sorry, but that screen does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you.")
            if o2_type == "plate":
                self.plate = self.conn.getPlate(o2_id)
                if self.plate is None:
                    raise AttributeError("We are sorry, but that plate does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you.")
                if self.plate._obj is None:
                    raise AttributeError("We are sorry, but that plate does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you.") 
        elif o1_type == "plate":
            self.plate = self.conn.getPlate(o1_id)
            if self.plate is None:
                raise AttributeError("We are sorry, but that plate does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you.")
            if self.plate._obj is None:
                raise AttributeError("We are sorry, but that plate does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you.")  
        elif o1_type == "dataset":
            self.dataset = self.conn.getDataset(o1_id)
            if self.dataset is None:
                raise AttributeError("We are sorry, but that dataset does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you.")
            if self.dataset._obj is None:
                raise AttributeError("We are sorry, but that dataset does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you.")
        elif o1_type == "image":
            self.image = self.conn.getImage(o1_id)
            if self.image is None:
                raise AttributeError("We are sorry, but that image does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you.")
            if self.image._obj is None:
                raise AttributeError("We are sorry, but that image does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you.")                
        elif o1_type == "tag":
            self.tag = self.conn.getTagAnnotation(o1_id)
        elif o1_type == "url":
            self.uri = self.conn.getUriAnnotation(o1_id)
        elif tags is not None:
            if len(tags) > 0:
                self.tags = tags
            elif len(rtags) > 0:
                self.tags = list(self.conn.lookupTagsAnnotation(rtags))
        elif o1_type == "orphaned":
            self.orphaned = True
    
    def formatMetadataLine(self, l):
        if len(l) < 1:
            return None
        return l.split("=")
        
    def originalMetadata(self):
        # TODO: hardcoded values.
        self.global_metadata = list()
        self.series_metadata = list()
        if self.image is not None or self.well.selectedWellSample().image is not None:
            om = self.image.loadOriginalMetadata()
            self.original_metadata = om[0]
            self.global_metadata = sorted(om[1])
            self.series_metadata = sorted(om[2])

    def channelMetadata(self):
        try:
            if self.image is not None:
                self.channel_metadata = self.image.getChannels()
            elif self.well is not None:
                self.channel_metadata = self.well.selectedWellSample().image().getChannels()
        except:
            self.channel_metadata = list()
    
    def loadDataByTag(self):
        tagids = list()
        for t in self.tags:
            if t is not None:
                tagids.append(t.id)
        
        pr_list = list(self.conn.listProjectsByTag(tagids))
        ds_list = list(self.conn.listDatasetsByTag(tagids))
        im_list = list(self.conn.listImagesByTag(tagids))
        
        pr_list_with_counters = list()
        ds_list_with_counters = list()
        im_list_with_counters = list()
        
        pr_ids = [pr.id for pr in pr_list]
        if len(pr_ids) > 0:
            pr_annotation_counter = self.conn.getCollectionCount("Project", "annotationLinks", pr_ids)
            
            for pr in pr_list:
                pr.annotation_counter = pr_annotation_counter.get(pr.id)
                pr_list_with_counters.append(pr)
        
        ds_ids = [ds.id for ds in ds_list]
        if len(ds_ids) > 0:
            ds_annotation_counter = self.conn.getCollectionCount("Dataset", "annotationLinks", ds_ids)
            
            for ds in ds_list:
                ds.annotation_counter = ds_annotation_counter.get(ds.id)
                ds_list_with_counters.append(ds)
        
        im_ids = [im.id for im in im_list]
        if len(im_ids) > 0:
            im_annotation_counter = self.conn.getCollectionCount("Image", "annotationLinks", im_ids)
            
            for im in im_list:
                im.annotation_counter = im_annotation_counter.get(im.id)
                im_list_with_counters.append(im)
        
        self.containers={'projects': pr_list_with_counters, 'datasets': ds_list_with_counters, 'images': im_list_with_counters}
        self.c_size = len(pr_list_with_counters)+len(ds_list_with_counters)+len(im_list_with_counters)
        
    def loadHierarchies(self):
        if self.image is not None:
            obj_list = self.conn.findContainerHierarchies(self.image.id)
            pr_list = list()
            ds_list = list()
            for o in obj_list:
                if isinstance(o._obj, ProjectI):
                    pr_list.append(o)
                if isinstance(o._obj, DatasetI):
                    ds_list.append(o)
                    
            self.hierarchy={'projects': self.sortByAttr(pr_list, 'name'), 'datasets': self.sortByAttr(ds_list, 'name')}
        #1015    
        #elif self.dataset is not None:
        #    obj_list = self.conn.findContainerHierarchies(self.dataset.id)
        #    pr_list = list()
        #    for o in obj_list:
        #        if isinstance(o._obj, ProjectI):
        #            pr_list.append(o)
        #    self.hierarchy={'projects': self.sortByAttr(pr_list, 'name')}
        else:
            self.hierarchy = None

    def listDatasetsInProject(self, pid, page, eid=None):
        if eid is not None:
            self.experimenter = self.conn.getExperimenter(eid)
        
        ds_list = self.sortByAttr(list(self.conn.lookupDatasetsInProject(oid=project_id, eid=eid, page=page)), 'name')        
        ds_list_with_counters = list()
        
        ds_ids = [ds.id for ds in ds_list]
        if len(ds_ids) > 0:
            ds_annotation_counter = self.conn.getCollectionCount("Dataset", "annotationLinks", ds_ids)
        
            for ds in ds_list:
                ds.annotation_counter = ds_annotation_counter.get(ds.id)
                ds_list_with_counters.append(ds)
        
        ds_list_with_counters = self.sortByAttr(ds_list_with_counters, "name")
        self.containers = {'datasets': ds_list_with_counters}
        self.c_size = self.conn.getCollectionCount("Project", "datasetLinks", [long(pid)])[long(pid)]
        
        self.paging = self.doPaging(page, len(ds_list_with_counters), self.c_size)
    
    def listPlatesInScreen(self, sid, page, eid=None):
        if eid is not None:
            self.experimenter = self.conn.getExperimenter(eid)
        
        pl_list = self.sortByAttr(list(self.conn.lookupPlatesInScreens(oid=sid, eid=eid, page=page)), 'name')        
        pl_list_with_counters = list()
        
        pl_ids = [pl.id for pl in pl_list]
        if len(pl_ids) > 0:
            pl_annotation_counter = self.conn.getCollectionCount("Plate", "annotationLinks", pl_ids)
        
            for pl in pl_list:
                pl.annotation_counter = pl_annotation_counter.get(pl.id)
                pl_list_with_counters.append(pl)
        
        pl_list_with_counters = self.sortByAttr(pl_list_with_counters, "name")
        self.containers = {'plates': pl_list_with_counters}
        self.c_size = self.conn.getCollectionCount("Screen", "plateLinks", [long(sid)])[long(sid)]
        
        self.paging = self.doPaging(page, len(pl_list_with_counters), self.c_size)
    
    def listPlate(self, plid, index, page):
        wl_list = list(self.conn.lookupWellsInPlate(oid=plid, index=index))
        wl_list_with_counters = dict()
        wl_ids = list()
        self.fields = None
        
        row_names = set()
        column_names = set()
        row_count = 0
        col_count = 0
        for wl in wl_list:
            wl_ids.append(wl.id)
            row_count = wl.row > row_count and wl.row or row_count
            col_count = wl.column > col_count and wl.column or col_count
        
        if row_count >= 0 or col_count >= 0:             
            for r in range(0, row_count+1):
                if self.plate.rowNamingConvention.isdigit():
                    row_names.add(r+int(self.plate.rowNamingConvention))
                elif self.plate.rowNamingConvention.isalpha():
                    row_names.add(chr(r+ord(self.plate.rowNamingConvention)))
                wl_list_with_counters[r] = dict()
                for c in range(0, col_count+1):
                    if self.plate.columnNamingConvention.isdigit():
                        column_names.add(c+int(self.plate.columnNamingConvention))
                    elif self.plate.columnNamingConvention.isalpha():
                        column_names.add(chr(c+ord(self.plate.columnNamingConvention)))
                    wl_list_with_counters[r][c] = None

            if len(wl_ids) > 0:
                wl_annotation_counter = self.conn.getCollectionCount("Well", "annotationLinks", wl_ids)

                for wl in wl_list:
                    if self.fields is None or self.fields == 0:
                        self.fields = wl.countWellSample()
                    wl.annotation_counter = wl_annotation_counter.get(wl.id)
                    wl_list_with_counters[wl.row][wl.column]= wl
        
        if self.plate.rowNamingConvention.isalpha():
            row_names = list(row_names).sort()
        if self.plate.columnNamingConvention.isalpha():
            column_names = list(column_names).sort()
        
        wl_list_with_counters_final = list()
        for key,val in wl_list_with_counters.items():
            row_final = list()
            for k,v in val.items():
                if self.plate.columnNamingConvention.isalpha():
                    k = chr(k+ord(self.plate.columnNamingConvention))
                if self.plate.columnNamingConvention.isdigit():
                    k = k+int(self.plate.columnNamingConvention)
                row_final.append((k,v))
            if self.plate.rowNamingConvention.isalpha():
                key = chr(key+ord(self.plate.rowNamingConvention))
            if self.plate.rowNamingConvention.isdigit():
                key = key+int(self.plate.rowNamingConvention)
            wl_list_with_counters_final.append((key,row_final))
        
        self.index = index is None and 0 or index
        self.containers = {'wells': wl_list_with_counters_final}
        self.names = {'row_names':row_names, 'column_names':column_names}
        self.c_size = len(wl_list) #self.conn.getCollectionCount("Plate", "wellLinks", [long(plid)])[long(plid)]
        # self.paging = self.doPaging(page, len(pl_list_with_counters), self.c_size)
        
    def listImagesInDataset(self, did, page, eid=None):
        if eid is not None:
            self.experimenter = self.conn.getExperimenter(eid)
        
        im_list = self.sortByAttr(list(self.conn.lookupImagesInDataset(oid=did, eid=eid, page=page)), 'name')
        im_list_with_counters = list()
        
        im_ids = [im.id for im in im_list]
        if len(im_ids) > 0:
            im_annotation_counter = self.conn.getCollectionCount("Image", "annotationLinks", im_ids)
            
            for im in im_list:
                im.annotation_counter = im_annotation_counter.get(im.id)
                im_list_with_counters.append(im)
        
        im_list_with_counters = self.sortByAttr(im_list_with_counters, 'name')
        self.containers = {'images': im_list_with_counters}
        self.c_size = self.conn.getCollectionCount("Dataset", "imageLinks", [long(did)])[long(did)]
        
        self.paging = self.doPaging(page, len(im_list_with_counters), self.c_size)

    def loadContainerHierarchy(self, eid=None):
        if eid is not None:
            self.experimenter = self.conn.getExperimenter(eid)
        
        obj_list = list(self.conn.loadContainerHierarchy('Project', eid=eid))
        obj_list.extend(list(self.conn.loadContainerHierarchy('Screen', eid=eid)))
        
        pr_list = list()
        ds_list = list()
        sc_list = list()
        pl_list = list()
        
        for o in obj_list:
            if isinstance(o._obj, ProjectI):
                pr_list.append(o)
            elif isinstance(o._obj, DatasetI):
                ds_list.append(o)
            elif isinstance(o._obj, ScreenI):
                sc_list.append(o)
            elif isinstance(o._obj, PlateI):
                pl_list.append(o)

        pr_list_with_counters = list()
        ds_list_with_counters = list()
        sc_list_with_counters = list()
        pl_list_with_counters = list()
        
        pr_ids = [pr.id for pr in pr_list]
        if len(pr_ids) > 0:
            pr_annotation_counter = self.conn.getCollectionCount("Project", "annotationLinks", pr_ids)
            
            for pr in pr_list:
                pr.annotation_counter = pr_annotation_counter.get(pr.id)
                pr_list_with_counters.append(pr)
                
        ds_ids = [ds.id for ds in ds_list]
        if len(ds_ids) > 0:
            ds_annotation_counter = self.conn.getCollectionCount("Dataset", "annotationLinks", ds_ids)
            
            for ds in ds_list:
                ds.annotation_counter = ds_annotation_counter.get(ds.id)
                ds_list_with_counters.append(ds)
        
        sc_ids = [sc.id for sc in sc_list]
        if len(sc_ids) > 0:
            sc_annotation_counter = self.conn.getCollectionCount("Screen", "annotationLinks", sc_ids)

            for sc in sc_list:
                sc.annotation_counter = sc_annotation_counter.get(sc.id)
                sc_list_with_counters.append(sc)

        pl_ids = [pl.id for pl in pl_list]
        if len(pl_ids) > 0:
            pl_annotation_counter = self.conn.getCollectionCount("Plate", "annotationLinks", ds_ids)

            for pl in pl_list:
                pl.annotation_counter = pl_annotation_counter.get(pl.id)
                pl_list_with_counters.append(pl)
                
        pr_list_with_counters = self.sortByAttr(pr_list_with_counters, 'name')
        ds_list_with_counters = self.sortByAttr(ds_list_with_counters, 'name')
        sc_list_with_counters = self.sortByAttr(sc_list_with_counters, "name")
        pl_list_with_counters = self.sortByAttr(pl_list_with_counters, "name")
        
        self.containers={'projects': pr_list_with_counters, 'datasets': ds_list_with_counters, 'screens': sc_list_with_counters, 'plates': pl_list_with_counters}
        self.c_size = len(pr_list_with_counters)+len(ds_list_with_counters)+len(sc_list_with_counters)+len(pl_list_with_counters)

    def loadImages(self, did, eid=None):
        if eid is not None:
            self.experimenter = self.conn.getExperimenter(eid)
        
        im_list = self.sortByAttr(list(self.conn.lookupImagesInDataset(oid=did, eid=eid)), 'name')
        im_list_with_counters = list()
        
        im_ids = [im.id for im in im_list]
        if len(im_ids) > 0:
            im_annotation_counter = self.conn.getCollectionCount("Image", "annotationLinks", im_ids)
            
            for im in im_list:
                im.annotation_counter = im_annotation_counter.get(im.id)
                im_list_with_counters.append(im)
        
        self.subcontainers = im_list_with_counters

    def loadOrphanedImages(self, eid=None):
        if eid is not None:
            self.experimenter = self.conn.getExperimenter(eid)
        
        im_list = self.sortByAttr(list(self.conn.lookupOrphanedImages(eid=eid)), 'name')
        im_list_with_counters = list()
        
        im_ids = [im.id for im in im_list]
        if len(im_ids) > 0:
            im_annotation_counter = self.conn.getCollectionCount("Image", "annotationLinks", im_ids)
            
            for im in im_list:
                im.annotation_counter = im_annotation_counter.get(im.id)
                im_list_with_counters.append(im)
        
        self.containers = {'images': im_list_with_counters}
        self.subcontainers = im_list_with_counters
        self.c_size = len(im_list_with_counters)

    # Annotation list
    def annotationList(self):
        self.text_annotations = list()
        self.long_annotations = {'rate': 0.00 , 'votes': 0}
        self.url_annotations = list()
        self.file_annotations = list()
        self.tag_annotations = list()
        
        aList = list()
        if self.image is not None:
            aList = list(self.image.listAnnotations())
        elif self.dataset is not None:
            aList = list(self.dataset.listAnnotations())
        elif self.project is not None:
            aList = list(self.project.listAnnotations())
        elif self.screen is not None:
            aList = list(self.screen.listAnnotations())
        elif self.plate is not None:
            aList = list(self.plate.listAnnotations())
        
        for ann in aList:
            if isinstance(ann._obj, CommentAnnotationI):
                self.text_annotations.append(ann)
            elif isinstance(ann._obj, UriAnnotationI):
                self.url_annotations.append(ann)
            elif isinstance(ann._obj, LongAnnotationI):
                self.long_annotations['votes'] += 1
                self.long_annotations['rate'] += int(ann.longValue)
            elif isinstance(ann._obj, FileAnnotationI):
                self.file_annotations.append(ann)
            elif isinstance(ann._obj, TagAnnotationI):
                self.tag_annotations.append(ann)

        self.text_annotations = self.sortByAttr(self.text_annotations, "details.creationEvent.time", True)
        self.url_annotations = self.sortByAttr(self.url_annotations, "textValue")
        self.file_annotations = self.sortByAttr(self.file_annotations, "details.creationEvent.time")
        self.tag_annotations = self.sortByAttr(self.tag_annotations, "textValue")
        
        self.txannSize = len(self.text_annotations)
        self.urlannSize = len(self.url_annotations)
        self.fileannSize = len(self.file_annotations)
        self.tgannSize = len(self.tag_annotations)

        if self.long_annotations['votes'] > 0:
            self.long_annotations['rate'] /= self.long_annotations['votes']
    
    def listTags(self):
        if self.image is not None:
            return list(self.conn.listTags("image", self.image.id))
        elif self.dataset is not None:
            return list(self.conn.listTags("dataset", self.dataset.id))
        elif self.project is not None:
            return list(self.conn.listTags("project", self.project.id))
        elif self.well is not None:
            return list(self.conn.listTags("well", self.well.id))
        elif self.plate is not None:
            return list(self.conn.listTags("plate", self.plate.id))
        elif self.screen is not None:
            return list(self.conn.listTags("screen", self.screen.id))
    
    def listComments(self):
        if self.image is not None:
            return list(self.conn.listComments("image", self.image.id))
        elif self.dataset is not None:
            return list(self.conn.listComments("dataset", self.dataset.id))
        elif self.project is not None:
            return list(self.conn.listComments("project", self.project.id))
        elif self.well is not None:
            return list(self.conn.listComments("well", self.well.id))
        elif self.plate is not None:
            return list(self.conn.listComments("plate", self.plate.id))
        elif self.screen is not None:
            return list(self.conn.listComments("screen", self.screen.id))
    
    def listUrls(self):
        if self.image is not None:
            return list(self.conn.listUrls("image", self.image.id))
        elif self.dataset is not None:
            return list(self.conn.listUrls("dataset", self.dataset.id))
        elif self.project is not None:
            return list(self.conn.listUrls("project", self.project.id))
        elif self.well is not None:
            return list(self.conn.listUrls("well", self.well.id))
        elif self.plate is not None:
            return list(self.conn.listUrls("plate", self.plate.id))
        elif self.screen is not None:
            return list(self.conn.listUrls("screen", self.screen.id))
    
    def listFiles(self):
        if self.image is not None:
            return list(self.conn.listFiles("image", self.image.id))
        elif self.dataset is not None:
            return list(self.conn.listFiles("dataset", self.dataset.id))
        elif self.project is not None:
            return list(self.conn.listFiles("project", self.project.id))
        elif self.well is not None:
            return list(self.conn.listFiles("well", self.well.id))
        elif self.plate is not None:
            return list(self.conn.listFiles("plate", self.plate.id))
        elif self.screen is not None:
            return list(self.conn.listFiles("screen", self.screen.id))
    
    ####################################################################
    # Creation
    
    def createDataset(self, name, description):
        ds = DatasetI()
        ds.name = rstring(str(name))
        if description != "" :
            ds.description = rstring(str(description))
        if self.project is not None:
            l_ds = ProjectDatasetLinkI()
            l_ds.setParent(self.project._obj)
            l_ds.setChild(ds)
            ds.addProjectDatasetLink(l_ds)
        self.conn.saveObject(ds)
        
    def createProject(self, name, description):
        pr = ProjectI()
        pr.name = rstring(str(name))
        if description != "" :
            pr.description = rstring(str(description))
        self.conn.saveObject(pr)
    
    def createScreen(self, name, description):
        sc = ScreenI()
        sc.name = rstring(str(name))
        if description != "" :
            sc.description = rstring(str(description))
        self.conn.saveObject(sc)
    
    # Comment annotation
    def createProjectCommentAnnotation(self, content):
        ann = CommentAnnotationI()
        ann.textValue = rstring(str(content))
        l_ann = ProjectAnnotationLinkI()
        l_ann.setParent(self.project._obj)
        l_ann.setChild(ann)
        self.conn.saveObject(l_ann)
    
    def createScreenCommentAnnotation(self, content):
        ann = CommentAnnotationI()
        ann.textValue = rstring(str(content))
        l_ann = ScreenAnnotationLinkI()
        l_ann.setParent(self.screen._obj)
        l_ann.setChild(ann)
        self.conn.saveObject(l_ann)
    
    def createDatasetCommentAnnotation(self, content):
        ann = CommentAnnotationI()
        ann.textValue = rstring(str(content))
        l_ann = DatasetAnnotationLinkI()
        l_ann.setParent(self.dataset._obj)
        l_ann.setChild(ann)
        self.conn.saveObject(l_ann)
    
    def createPlateCommentAnnotation(self, content):
        ann = CommentAnnotationI()
        ann.textValue = rstring(str(content))
        l_ann = PlateAnnotationLinkI()
        l_ann.setParent(self.plate._obj)
        l_ann.setChild(ann)
        self.conn.saveObject(l_ann)
    
    def createImageCommentAnnotation(self, content):
        ann = CommentAnnotationI()
        ann.textValue = rstring(str(content))
        l_ann = ImageAnnotationLinkI()
        l_ann.setParent(self.image._obj)
        l_ann.setChild(ann)
        self.conn.saveObject(l_ann)
    
    # URI annotation
    def createProjectUriAnnotation(self, content):
        ann = UriAnnotationI()
        ann.textValue = rstring(str(content))
        l_ann = ProjectAnnotationLinkI()
        l_ann.setParent(self.project._obj)
        l_ann.setChild(ann)
        self.conn.saveObject(l_ann)
    
    def createScreenUriAnnotation(self, content):
        ann = UriAnnotationI()
        ann.textValue = rstring(str(content))
        l_ann = ScreenAnnotationLinkI()
        l_ann.setParent(self.screen._obj)
        l_ann.setChild(ann)
        self.conn.saveObject(l_ann)
    
    def createDatasetUriAnnotation(self, content):
        ann = UriAnnotationI()
        ann.textValue = rstring(str(content))
        l_ann = DatasetAnnotationLinkI()
        l_ann.setParent(self.dataset._obj)
        l_ann.setChild(ann)
        self.conn.saveObject(l_ann)
    
    def createPlateUriAnnotation(self, content):
        ann = UriAnnotationI()
        ann.textValue = rstring(str(content))
        l_ann = PlateAnnotationLinkI()
        l_ann.setParent(self.plate._obj)
        l_ann.setChild(ann)
        self.conn.saveObject(l_ann)
    
    def createImageUriAnnotation(self, content):
        ann = UriAnnotationI()
        ann.textValue = rstring(str(content))
        l_ann = ImageAnnotationLinkI()
        l_ann.setParent(self.image._obj)
        l_ann.setChild(ann)
        self.conn.saveObject(l_ann)
    
    # Tag annotation
    def createImageTagAnnotation(self, tag, desc):
        ann = None
        try:
            ann = self.conn.findTag(tag, desc)._obj
        except:
            pass
        if ann is None:
            ann = TagAnnotationI()
            ann.textValue = rstring(str(tag))
            ann.setDescription(rstring(str(desc)))
        
        t_ann = ImageAnnotationLinkI()
        t_ann.setParent(self.image._obj)
        t_ann.setChild(ann)
        self.conn.saveObject(t_ann)
    
    def createDatasetTagAnnotation(self, tag, desc):
        ann = TagAnnotationI()
        ann.textValue = rstring(str(tag))
        ann.setDescription(rstring(str(desc)))
        t_ann = DatasetAnnotationLinkI()
        t_ann.setParent(self.dataset._obj)
        t_ann.setChild(ann)
        self.conn.saveObject(t_ann)
    
    def createPlateTagAnnotation(self, tag, desc):
        ann = TagAnnotationI()
        ann.textValue = rstring(str(tag))
        ann.setDescription(rstring(str(desc)))
        t_ann = PlateAnnotationLinkI()
        t_ann.setParent(self.plate._obj)
        t_ann.setChild(ann)
        self.conn.saveObject(t_ann)
    
    def createProjectTagAnnotation(self, tag, desc):
        ann = TagAnnotationI()
        ann.textValue = rstring(str(tag))
        ann.setDescription(rstring(str(desc)))
        t_ann = ProjectAnnotationLinkI()
        t_ann.setParent(self.project._obj)
        t_ann.setChild(ann)
        self.conn.saveObject(t_ann)
    
    def createScreenTagAnnotation(self, tag, desc):
        ann = TagAnnotationI()
        ann.textValue = rstring(str(tag))
        ann.setDescription(rstring(str(desc)))
        t_ann = ScreenAnnotationLinkI()
        t_ann.setParent(self.screen._obj)
        t_ann.setChild(ann)
        self.conn.saveObject(t_ann)
    
    # File annotation
    def getFileFormat(self, newFile):
        format = None
        try:
            format = self.conn.getFileFormat(newFile.content_type)
        except:
            pass
        
        if format is None:
            format = self.conn.getFileFormat("application/octet-stream")
        return format
    
    def createProjectFileAnnotation(self, newFile):
        format = self.getFileFormat(newFile)
        oFile = OriginalFileI()
        oFile.setName(rstring(str(newFile.name)));
        oFile.setPath(rstring(str(newFile.name)));
        oFile.setSize(rlong(long(newFile.size)));
        oFile.setSha1(rstring("pending"));
        oFile.setFormat(format);
        
        of = self.conn.saveAndReturnObject(oFile);
        self.conn.saveFile(newFile, of.id)
        
        fa = FileAnnotationI()
        fa.setFile(of._obj)
        l_ia = ProjectAnnotationLinkI()
        l_ia.setParent(self.project._obj)
        l_ia.setChild(fa)
        self.conn.saveObject(l_ia)
    
    def createScreenFileAnnotation(self, newFile):
        format = self.getFileFormat(newFile)
        oFile = OriginalFileI()
        oFile.setName(rstring(str(newFile.name)));
        oFile.setPath(rstring(str(newFile.name)));
        oFile.setSize(rlong(long(newFile.size)));
        oFile.setSha1(rstring("pending"));
        oFile.setFormat(format);
        
        of = self.conn.saveAndReturnObject(oFile);
        self.conn.saveFile(newFile, of.id)
        
        fa = FileAnnotationI()
        fa.setFile(of._obj)
        l_ia = ScreenAnnotationLinkI()
        l_ia.setParent(self.screen._obj)
        l_ia.setChild(fa)
        self.conn.saveObject(l_ia)
    
    def createDatasetFileAnnotation(self, newFile):
        format = self.getFileFormat(newFile)
        oFile = OriginalFileI()
        oFile.setName(rstring(str(newFile.name)));
        oFile.setPath(rstring(str(newFile.name)));
        oFile.setSize(rlong(long(newFile.size)));
        oFile.setSha1(rstring("pending"));
        oFile.setFormat(format);
        
        of = self.conn.saveAndReturnObject(oFile);
        self.conn.saveFile(newFile, of.id)
        
        fa = FileAnnotationI()
        fa.setFile(of._obj)
        l_ia = DatasetAnnotationLinkI()
        l_ia.setParent(self.dataset._obj)
        l_ia.setChild(fa)
        self.conn.saveObject(l_ia)
    
    def createPlateFileAnnotation(self, newFile):
        format = self.getFileFormat(newFile)
        oFile = OriginalFileI()
        oFile.setName(rstring(str(newFile.name)));
        oFile.setPath(rstring(str(newFile.name)));
        oFile.setSize(rlong(long(newFile.size)));
        oFile.setSha1(rstring("pending"));
        oFile.setFormat(format);
        
        of = self.conn.saveAndReturnObject(oFile);
        self.conn.saveFile(newFile, of.id)
        
        fa = FileAnnotationI()
        fa.setFile(of._obj)
        l_ia = PlateAnnotationLinkI()
        l_ia.setParent(self.plate._obj)
        l_ia.setChild(fa)
        self.conn.saveObject(l_ia)
    
    def createImageFileAnnotation(self, newFile):
        format = self.getFileFormat(newFile)
        oFile = OriginalFileI()
        oFile.setName(rstring(str(newFile.name)));
        oFile.setPath(rstring(str(newFile.name)));
        oFile.setSize(rlong(long(newFile.size)));
        oFile.setSha1(rstring("pending"));
        oFile.setFormat(format);
        
        of = self.conn.saveAndReturnObject(oFile);
        self.conn.saveFile(newFile, of.id)
        
        fa = FileAnnotationI()
        fa.setFile(of._obj)
        l_ia = ImageAnnotationLinkI()
        l_ia.setParent(self.image._obj)
        l_ia.setChild(fa)
        self.conn.saveObject(l_ia)
    
    # Create links
    def createImageAnnotationLinks(self, o_type, ids):
        anns = None
        if o_type == 'tag':
            anns = self.conn.listSpecifiedTags(ids)
        elif o_type == 'comment':
            anns = self.conn.listSpecifiedComments(ids)
        elif o_type == 'url':
            anns = self.conn.listSpecifiedUrls(ids)
        elif o_type == 'file':
            anns = self.conn.listSpecifiedFiles(ids)
        
        new_links = list()
        for a in anns:
            ann = ImageAnnotationLinkI()
            ann.setParent(self.image._obj)
            ann.setChild(a._obj)
            new_links.append(ann)
        self.conn.saveArray(new_links)
    
    def createDatasetAnnotationLinks(self, o_type, ids):
        anns = None
        if o_type == 'tag':
            anns = self.conn.listSpecifiedTags(ids)
        elif o_type == 'comment':
            anns = self.conn.listSpecifiedComments(ids)
        elif o_type == 'url':
            anns = self.conn.listSpecifiedUrls(ids)
        elif o_type == 'file':
            anns = self.conn.listSpecifiedFiles(ids)
        
        new_links = list()
        for a in anns:
            ann = DatasetAnnotationLinkI()
            ann.setParent(self.dataset._obj)
            ann.setChild(a._obj)
            new_links.append(ann)
        self.conn.saveArray(new_links)
    
    def createPlateAnnotationLinks(self, o_type, ids):
        anns = None
        if o_type == 'tag':
            anns = self.conn.listSpecifiedTags(ids)
        elif o_type == 'comment':
            anns = self.conn.listSpecifiedComments(ids)
        elif o_type == 'url':
            anns = self.conn.listSpecifiedUrls(ids)
        elif o_type == 'file':
            anns = self.conn.listSpecifiedFiles(ids)
        
        new_links = list()
        for a in anns:
            ann = PlateAnnotationLinkI()
            ann.setParent(self.plate._obj)
            ann.setChild(a._obj)
            new_links.append(ann)
        self.conn.saveArray(new_links)
    
    def createProjectAnnotationLinks(self, o_type, ids):
        anns = None
        if o_type == 'tag':
            anns = self.conn.listSpecifiedTags(ids)
        elif o_type == 'comment':
            anns = self.conn.listSpecifiedComments(ids)
        elif o_type == 'url':
            anns = self.conn.listSpecifiedUrls(ids)
        elif o_type == 'file':
            anns = self.conn.listSpecifiedFiles(ids)
        
        new_links = list()
        for a in anns:
            ann = ProjectAnnotationLinkI()
            ann.setParent(self.project._obj)
            ann.setChild(a._obj)
            new_links.append(ann)
        self.conn.saveArray(new_links)
    
    def createScreenAnnotationLinks(self, o_type, ids):
        anns = None
        if o_type == 'tag':
            anns = self.conn.listSpecifiedTags(ids)
        elif o_type == 'comment':
            anns = self.conn.listSpecifiedComments(ids)
        elif o_type == 'url':
            anns = self.conn.listSpecifiedUrls(ids)
        elif o_type == 'file':
            anns = self.conn.listSpecifiedFiles(ids)
        
        new_links = list()
        for a in anns:
            ann = ScreenAnnotationLinkI()
            ann.setParent(self.screen._obj)
            ann.setChild(a._obj)
            new_links.append(ann)
        self.conn.saveArray(new_links)
    
    ################################################################
    # Update
    
    def updateDescription(self, description):
        img = self.image._obj
        if description != "" :
            img.description = rstring(str(description))
        else:
            img.description = None
        self.conn.saveObject(img)
    
    def updateImage(self, name, description):
        img = self.image._obj
        img.name = rstring(str(name))
        if description != "" :
            img.description = rstring(str(description))
        else:
            img.description = None
        self.conn.saveObject(img)
    
    def updateDataset(self, name, description):
        container = self.dataset._obj
        container.name = rstring(str(name))
        if description != "" :
            container.description = rstring(str(description))
        else:
            container.description = None
        self.conn.saveObject(container)
    
    def updatePlate(self, name, description):
        container = self.plate._obj
        container.name = rstring(str(name))
        if description != "" :
            container.description = rstring(str(description))
        else:
            container.description = None
        self.conn.saveObject(container)
    
    def updateProject(self, name, description):
        container = self.project._obj
        container.name = rstring(str(name))
        if description != "" :
            container.description = rstring(str(description))
        else:
            container.description = None
        self.conn.saveObject(container)
    
    def updateScreen(self, name, description):
        container = self.screen._obj
        container.name = rstring(str(name))
        if description != "" :
            container.description = rstring(str(description))
        else:
            container.description = None
        self.conn.saveObject(container)
    
    def move(self, parent, source, destination):
        if source[0] == "pr":
            return 'Cannot move project.'
        elif source[0] == "ds":
            if destination[0] == 'ds':
                return 'Cannot move dataset to dataset'
            elif destination[0] == 'pr':
                up_pdl = None
                pdls = self.conn.getProjectDatasetLinks(source[1])
                already_there = None
                
                for pdl in pdls:
                    if pdl.parent.id.val == long(destination[1]):
                        already_there = True
                    if pdl.parent.id.val == long(parent[1]):
                        up_pdl = pdl
                if already_there:
                    if long(parent[1]) != long(destination[1]):
                        self.conn.deleteObject(up_pdl._obj)
                else:
                    new_pr = self.conn.getProject(destination[1])
                    if len(parent) > 1:
                        up_pdl.setParent(new_pr._obj)
                        self.conn.saveObject(up_pdl._obj)
                    else:
                        ds = self.conn.getDataset(source[1])
                        up_pdl = ProjectDatasetLinkI()
                        up_pdl.setChild(ds._obj)
                        up_pdl.setParent(new_pr._obj)
                        self.conn.saveObject(up_pdl)
            elif destination[0] == '0':
                up_pdl = None
                pdls = list(self.conn.getProjectDatasetLinks(source[1]))
                
                if len(pdls) == 1:
                    # gets old parent to delete
                    if pdls[0].parent.id.val == long(parent[1]):
                        up_pdl = pdls[0]
                        self.conn.deleteObject(up_pdl._obj)
                else:
                    return 'This dataset is linked in multiple places. Please unlink the dataset first.'
            elif destination[0] == 'orphan':
                return 'Cannot move dataset to orphaned images.'
            else:
                return 'Destination not supported.'
        elif source[0] == "img":
            if destination[0] == 'ds':
                up_dsl = None
                dsls = self.conn.getDatasetImageLinks(source[1]) #gets every links for child
                already_there = None
                
                #checks links
                for dsl in dsls:
                    #if is already linked to destination
                    if dsl.parent.id.val == long(destination[1]):
                        already_there = True
                    # gets old parent to update or delete
                    if dsl.parent.id.val == long(parent[1]):
                        up_dsl = dsl
                if already_there:
                    # delete link to not duplicate
                    if long(parent[1]) != long(destination[1]):
                        self.conn.deleteObject(up_dsl._obj)
                else:
                    # update link to new destination
                    new_ds = self.conn.getDataset(destination[1])
                    if len(parent) > 1:
                        up_dsl.setParent(new_ds._obj)
                        self.conn.saveObject(up_dsl._obj)
                    else:
                        im = self.conn.getImage(source[1])
                        up_dsl = DatasetImageLinkI()
                        up_dsl.setChild(im._obj)
                        up_dsl.setParent(new_ds._obj)
                        self.conn.saveObject(up_dsl)
            elif destination[0] == 'pr':
                return 'Cannot move image to project.'
            elif destination[0] == '0' or destination[0] == 'orphan':
                if parent[0] != destination[0]:
                    up_dsl = None
                    dsls = list(self.conn.getDatasetImageLinks(source[1])) #gets every links for child
                    if len(dsls) == 1:
                        # gets old parent to delete
                        if dsls[0].parent.id.val == long(parent[1]):
                            up_dsl = dsls[0]
                            self.conn.deleteObject(up_dsl._obj)
                    else:
                        return 'This image is linked in multiple places. Please unlink the image first.'
            else:
                return 'Destination not supported.'
        elif source[0] == "sc":
            return 'Cannot move screen.'
        elif source[0] == "pl":
            if destination[0] == 'pl':
                return 'Cannot move plate to plate'
            elif destination[0] == 'sc':
                up_spl = None
                spls = self.conn.getScreenPlateLinks(source[1])
                already_there = None
                
                for spl in spls:
                    if spl.parent.id.val == long(destination[1]):
                        already_there = True
                    if spl.parent.id.val == long(parent[1]):
                        up_spl = spl
                if already_there:
                    if long(parent[1]) != long(destination[1]):
                        self.conn.deleteObject(up_spl._obj)
                else:
                    new_sc = self.conn.getScreen(destination[1])
                    if len(parent) > 1:
                        up_spl.setParent(new_sc._obj)
                        self.conn.saveObject(up_spl._obj)
                    else:
                        pl = self.conn.getDataset(source[1])
                        up_spl = ScreenPlateLinkI()
                        up_spl.setChild(pl._obj)
                        up_spl.setParent(new_sc._obj)
                        self.conn.saveObject(up_spl)
            elif destination[0] == '0':
                return 'Cannot move plate to unknown place.'
            elif destination[0] == 'orphan':
                return 'Cannot move plate to orphaned images.'
            else:
                return 'Destination not supported.'
        else:
            return 'No data was choosen.'
        return 
    
    def remove(self, parent, source):
        if source[0] == 'ds':
            if parent[0] == 'pr':
                pdl = self.conn.getProjectDatasetLink(parent[1], source[1])
                if pdl is not None:
                    self.conn.deleteObject(pdl._obj)
        elif source[0] == 'pl':
            if parent[0] == 'sc':
                spl = self.conn.getScreenPlateLink(parent[1], source[1])
                if sdl is not None:
                    self.conn.deleteObject(spl._obj)
        elif source[0] == 'img':
            if parent[0] == 'ds':
                dil = self.conn.getDatasetImageLink(parent[1], source[1])
                if dil is not None:
                    self.conn.deleteObject(dil._obj)
        elif source[0] == 'tann' or source[0] == 'cann' or source[0] == 'fann' or source[0] == 'uann':
            if parent[0] == 'pr':
                pal = self.conn.getProjectAnnotationLink(parent[1], source[1])
                if pal is not None:
                    self.conn.deleteObject(pal._obj)
            elif parent[0] == 'ds':
                dal = self.conn.getDatasetAnnotationLink(parent[1], source[1])
                if dal is not None:
                    self.conn.deleteObject(dal._obj)
            elif parent[0] == 'sc':
                sal = self.conn.getScreenAnnotationLink(parent[1], source[1])
                if sal is not None:
                    self.conn.deleteObject(sal._obj)
            elif parent[0] == 'pl':
                pal = self.conn.getPlateAnnotationLink(parent[1], source[1])
                if pal is not None:
                    self.conn.deleteObject(pal._obj)
            elif parent[0] == 'img':
                ial = self.conn.getImageAnnotationLink(parent[1], source[1])
                if ial is not None:
                    self.conn.deleteObject(ial._obj)
        else:
            raise AttributeError("Attribute not specified. Cannot be removed.")
    
    def removemany(self, parent, source):
        if parent[0] == 'pr':
            try:
                datasets = source['datasets']
            except:
                raise AttributeError("Object cannot be removed.")
            for ds in datasets:
                pdl = self.conn.getProjectDatasetLink(parent[1], ds)
                if pdl is not None:
                    self.conn.deleteObject(pdl._obj)
        elif parent[0] == 'ds':
            try:
                images = source['images']
            except:
                raise AttributeError("Object cannot be removed.")
            for im in images:
                dil = self.conn.getDatasetImageLink(parent[1], im)
                if dil is not None:
                    self.conn.deleteObject(dil._obj)
        elif parent[0] == 'sc':
            try:
                plates = source['plates']
            except:
                raise AttributeError("Object cannot be removed.")
            for pl in plates:
                spl = self.conn.getScreenPlateLink(parent[1], pl)
                if spl is not None:
                    self.conn.deleteObject(spl._obj)
        elif parent[0] == 'tann' or parent[0] == 'cann' or parent[0] == 'fann' or parent[0] == 'uann':
            if source['projects'] is not None or len(source['projects']) > 0:
                for s in source['projects']:
                    pal = self.conn.getProjectAnnotationLink(s, parent[1])
                    if pal is not None:
                        self.conn.deleteObject(pal._obj)
            if source['datasets'] is not None or len(source['datasets']) > 0:
                for s in source['projects']:
                    dal = self.conn.getDatasetAnnotationLink(s, parent[1])
                    if dal is not None:
                        self.conn.deleteObject(dal._obj)
            if source['screens'] is not None or len(source['screens']) > 0:
                for s in source['screens']:
                    sal = self.conn.getScreenAnnotationLink(s, parent[1])
                    if sal is not None:
                        self.conn.deleteObject(sal._obj)
            if source['plates'] is not None or len(source['plates']) > 0:
                for s in source['plates']:
                    pal = self.conn.getPlateAnnotationLink(s, parent[1])
                    if pal is not None:
                        self.conn.deleteObject(pal._obj)
            if source['images'] is not None or len(source['images']) > 0:
                for s in source['images']:
                    ial = self.conn.getImageAnnotationLink(s, parent[1])
                    if ial is not None:
                        self.conn.deleteObject(ial._obj)
        
        else:
            raise AttributeError("Attribute not specified. Cannot be removed.")
    
    ##########################################################
    # Copy
    
    def copyImageToDataset(self, source, destination=None):
        if destination is None:
            dsls = self.conn.getDatasetImageLinks(source[1]) #gets every links for child
            for dsl in dsls:
                self.conn.deleteObject(dsl._obj)
        else:
            im = self.conn.getImage(source[1])
            ds = self.conn.getDataset(destination[1])
            new_dsl = DatasetImageLinkI()
            new_dsl.setChild(im._obj)
            new_dsl.setParent(ds._obj)
            self.conn.saveObject(new_dsl)
            return True
    
    def copyImagesToDataset(self, images, dataset):
           if dataset is None:
               pass
           else:
               ims = self.conn.getSpecifiedImages(images)
               ds = self.conn.getDataset(dataset[1])
               link_array = list()
               for im in ims:
                   new_dsl = DatasetImageLinkI()
                   new_dsl.setChild(im._obj)
                   new_dsl.setParent(ds._obj)
                   link_array.append(new_dsl)
               self.conn.saveArray(link_array)
               return True
    
    def copyDatasetToProject(self, source, destination=None):
        if destination is None:
            pass
        else:
            ds = self.conn.getDataset(source[1])
            pr = self.conn.getProject(destination[1])
            new_pdl = ProjectDatasetLinkI()
            new_pdl.setChild(ds._obj)
            new_pdl.setParent(pr._obj)
            self.conn.saveObject(new_pdl)
            return True
   
    def copyDatasetsToProject(self, datasets, project):
        if project is None:
            pass
        else:
            dss = self.conn.getSpecifiedDatasets(datasets)
            pr = self.conn.getProject(project[1])
            link_array = list()
            for ds in dss:
                new_pdl = ProjectDatasetLinkI()
                new_pdl.setChild(ds._obj)
                new_pdl.setParent(pr._obj)
                link_array.append(new_pdl)
            self.conn.saveArray(link_array)
            return True
    
    def copyPlateToScreen(self, source, destination=None):
        if destination is None:
            pass
        else:
            pl = self.conn.getPlate(source[1])
            sc = self.conn.getScreen(destination[1])
            new_spl = ScreenPlateLinkI()
            new_spl.setChild(pl._obj)
            new_spl.setParent(sc._obj)
            self.conn.saveObject(new_spl)
            return True
    
    def copyPlatesToScreen(self, plates, screen):
        if screen is None:
            pass
        else:
            pls = self.conn.getSpecifiedPlates(plates)
            sc = self.conn.getScreen(screen[1])
            link_array = list()
            for pl in pls:
                new_spl = ScreenPlateLinkI()
                new_spl.setChild(pl._obj)
                new_spl.setParent(sc._obj)
                link_array.append(new_spl)
            self.conn.saveArray(link_array)
            return True

