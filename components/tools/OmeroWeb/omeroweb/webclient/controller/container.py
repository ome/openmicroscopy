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

import omero
from omero.rtypes import *
from django.core.urlresolvers import reverse

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
    file_annotations = None
    
    orphaned = False
    
    def __init__(self, conn, o1_type=None, o1_id=None, o2_type=None, o2_id=None, o3_type=None, o3_id=None, tag=None, index=None, **kwargs):
        BaseController.__init__(self, conn)
        if o1_type == "project":
            self.project = self.conn.getObject("Project", o1_id)
            if self.project is None:
                raise AttributeError("We are sorry, but that project (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(o1_id))
            if self.project._obj is None:
                raise AttributeError("We are sorry, but that project (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(o1_id))
            if o2_type == "dataset":
                self.dataset = self.conn.getDataset(o2_id)
                if self.dataset is None:
                    raise AttributeError("We are sorry, but that dataset (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(o2_id))
                if self.dataset._obj is None:
                    raise AttributeError("We are sorry, but that dataset (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(o2_id))
        elif o1_type == "screen":
            self.screen = self.conn.getScreen(o1_id)
            if self.screen is None:
                raise AttributeError("We are sorry, but that screen (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(o1_id))
            if self.screen._obj is None:
                raise AttributeError("We are sorry, but that screen (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(o1_id))
            if o2_type == "plate":
                self.plate = self.conn.getPlate(o2_id)
                if self.plate is None:
                    raise AttributeError("We are sorry, but that plate (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(o2_id))
                if self.plate._obj is None:
                    raise AttributeError("We are sorry, but that plate (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(o2_id)) 
        elif o1_type == "plate":
            self.plate = self.conn.getPlate(o1_id)
            if self.plate is None:
                raise AttributeError("We are sorry, but that plate (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(o1_id))
            if self.plate._obj is None:
                raise AttributeError("We are sorry, but that plate (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(o1_id))  
        elif o1_type == "dataset":
            self.dataset = self.conn.getDataset(o1_id)
            if self.dataset is None:
                raise AttributeError("We are sorry, but that dataset (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(o1_id))
            if self.dataset._obj is None:
                raise AttributeError("We are sorry, but that dataset (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(o1_id))
        elif o1_type == "image":
            self.image = self.conn.getImage(o1_id)
            if self.image is None:
                raise AttributeError("We are sorry, but that image (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(o1_id))
            if self.image._obj is None:
                raise AttributeError("We are sorry, but that image (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(o1_id))
        elif o1_type == "well":
            self.well = self.conn.getWell(o1_id, index)
            if self.well is None:
                raise AttributeError("We are sorry, but that well (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(o1_id))
            if self.well._obj is None:
                raise AttributeError("We are sorry, but that well (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(o1_id))
        elif o1_type == "tag" and o1_id is not None:
            self.tag = self.conn.getAnnotation(o1_id)
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
        if self.image is not None:
            om = self.image.loadOriginalMetadata()
        elif self.well.selectedWellSample().image is not None:
            om = self.well.selectedWellSample().image().loadOriginalMetadata()
        if om is not None:
            self.original_metadata = om[0]
            self.global_metadata = om[1]
            self.series_metadata = om[2]

    def channelMetadata(self):
        self.channel_metadata = None
        try:
            if self.image is not None:
                self.channel_metadata = self.image.getChannels()
            elif self.well is not None:
                self.channel_metadata = self.well.selectedWellSample().image().getChannels()
        except:
            pass
        
        if self.channel_metadata is None:
            self.channel_metadata = list()
        
    def loadTags(self, eid=None):
        if eid is not None:
            self.experimenter = self.conn.getExperimenter(eid)
        else:            
            eid = self.conn.getEventContext().userId
        self.tags = list(self.conn.listAnnotations(eid, 'tag'))
        self.t_size = len(self.tags)
    
    def loadDataByTag(self):
        pr_list = list(self.conn.getObjectsByAnnotations('Project',[self.tag.id]))
        ds_list = list(self.conn.getObjectsByAnnotations('Dataset',[self.tag.id]))
        im_list = list(self.conn.getObjectsByAnnotations('Image',[self.tag.id]))
        sc_list = list(self.conn.getObjectsByAnnotations('Screen',[self.tag.id]))
        pl_list = list(self.conn.getObjectsByAnnotations('Plate',[self.tag.id]))
        
        pr_list_with_counters = list()
        ds_list_with_counters = list()
        im_list_with_counters = list()
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
        
        im_ids = [im.id for im in im_list]
        if len(im_ids) > 0:
            im_annotation_counter = self.conn.getCollectionCount("Image", "annotationLinks", im_ids)
            
            for im in im_list:
                im.annotation_counter = im_annotation_counter.get(im.id)
                im_list_with_counters.append(im)
        
        sc_ids = [sc.id for sc in sc_list]
        if len(sc_ids) > 0:
            sc_annotation_counter = self.conn.getCollectionCount("Screen", "annotationLinks", sc_ids)
            
            for sc in sc_list:
                sc.annotation_counter = sc_annotation_counter.get(sc.id)
                sc_list_with_counters.append(sc)
        
        pl_ids = [pl.id for pl in pl_list]
        if len(pl_ids) > 0:
            pl_annotation_counter = self.conn.getCollectionCount("Plate", "annotationLinks", pl_ids)
            
            for pl in pl_list:
                pl.annotation_counter = pl_annotation_counter.get(pl.id)
                pl_list_with_counters.append(pl)
        
        self.containers={'projects': pr_list_with_counters, 'datasets': ds_list_with_counters, 'images': im_list_with_counters, 'screens':sc_list_with_counters, 'plates':pl_list_with_counters}
        self.c_size = len(pr_list_with_counters)+len(ds_list_with_counters)+len(im_list_with_counters)+len(sc_list_with_counters)+len(pl_list_with_counters)
        
    def listImagesInDataset(self, did, eid=None, page=None):
        if eid is not None:
            self.experimenter = self.conn.getExperimenter(eid)  
        
        im_list = list(self.conn.listImagesInDataset(oid=did, eid=eid, page=page))
        im_list_with_counters = list()
        
        im_ids = [im.id for im in im_list]
        if len(im_ids) > 0:
            im_annotation_counter = self.conn.getCollectionCount("Image", "annotationLinks", im_ids)
            
            for im in im_list:
                im.annotation_counter = im_annotation_counter.get(im.id)
                im_list_with_counters.append(im)
        
        im_list_with_counters = self.sortByAttr(im_list_with_counters, 'id')
        self.containers = {'images': im_list_with_counters}
        self.c_size = self.conn.getCollectionCount("Dataset", "imageLinks", [long(did)])[long(did)]
        
        if page is not None:
            self.paging = self.doPaging(page, len(im_list_with_counters), self.c_size)
    
    def listPlate(self, plid, index):
        
        def letterNamingConventions(i):
            i-=1
            if i < 26:
                return chr(i+ord("A"))
            elif i >= 26 and i < 702:
                return chr(((i/26)-1)+ord("A")), chr(((i % 26))+ord("A"))        
        
        wl_list = list(self.conn.listWellsInPlate(oid=plid, index=index))
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
            
        if row_count >= 0 and col_count >= 0:   
            for r in range(1, row_count+2):
                row_names.add(r)
                wl_list_with_counters[r] = dict()
                for c in range(1, col_count+2):
                    column_names.add(c)
                    wl_list_with_counters[r][c] = None

            if len(wl_ids) > 0:
                wl_annotation_counter = self.conn.getCollectionCount("Well", "annotationLinks", wl_ids)
                for wl in wl_list:
                    if self.fields is None or self.fields == 0:
                        self.fields = wl.countWellSample()
                    wl.annotation_counter = wl_annotation_counter.get(wl.id)
                    wl_list_with_counters[wl.row+1][wl.column+1]= wl
        
        wl_list_with_counters_final = list()
        for key,val in wl_list_with_counters.iteritems():
            row_final = list()
            for k,v in val.items():
                k = self.plate.columnNamingConvention=='number' and k or letterNamingConventions(k)
                row_final.append((k,v))
            key = self.plate.rowNamingConvention=='number' and key or letterNamingConventions(key)
            wl_list_with_counters_final.append((key,row_final))

        self.index = index is None and 0 or index
        self.containers = {'wells': wl_list_with_counters_final}
        self.names = {'row_names':row_names, 'column_names':column_names}
        self.c_size = len(wl_list) #self.conn.getCollectionCount("Plate", "wellLinks", [long(plid)])[long(plid)]
    
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
        
    def listContainerHierarchy(self, eid=None):
        if eid is not None:
            self.experimenter = self.conn.getExperimenter(eid)
        else:
            eid = self.conn.getEventContext().userId
        #obj_list = list(self.conn.listContainerHierarchy('Project', eid=eid))
        #obj_list.extend(list(self.conn.listContainerHierarchy('Screen', eid=eid)))
            
        pr_list = list(self.conn.listProjects(eid))
        ds_list = list(self.conn.listOrphans("Dataset", eid))
        sc_list = list(self.conn.listScreens(eid))
        pl_list = list(self.conn.listOrphans("Plate", eid))

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
    
    def listOrphanedImages(self, eid=None):
        if eid is not None:
            self.experimenter = self.conn.getExperimenter(eid)
        else:
            self.conn.getEventContext().userId
        
        im_list = list(self.conn.listOrphans("Image", eid=eid))
        im_list_with_counters = list()
        
        im_ids = [im.id for im in im_list]
        if len(im_ids) > 0:
            im_annotation_counter = self.conn.getCollectionCount("Image", "annotationLinks", im_ids)
            
            for im in im_list:
                im.annotation_counter = im_annotation_counter.get(im.id)
                im_list_with_counters.append(im)
        
        im_list_with_counters = self.sortByAttr(im_list_with_counters, 'id')
        self.containers = {'orphaned': True, 'images': im_list_with_counters}
        self.c_size = len(im_list_with_counters)
        
        #if page is not None:
        #    self.paging = self.doPaging(page, len(im_list_with_counters), self.c_size)

    # Annotation list
    def annotationList(self):
        self.text_annotations = list()
        self.rating_annotations = list()
        self.file_annotations = list()
        self.tag_annotations = list()
        self.xml_annotations = list()
        self.boolean_annotations = list()
        self.double_annotations = list()
        self.long_annotations = list()
        self.term_annotations = list()
        self.time_annotations = list()
        self.companion_files =  list()
        
        annTypes = {omero.model.CommentAnnotationI: self.text_annotations,
                    omero.model.LongAnnotationI: self.long_annotations,
                    omero.model.FileAnnotationI: self.file_annotations,
                    omero.model.TagAnnotationI: self.tag_annotations,
                    omero.model.XmlAnnotationI: self.xml_annotations,
                    omero.model.BooleanAnnotationI: self.boolean_annotations,
                    omero.model.DoubleAnnotationI: self.double_annotations,
                    omero.model.TermAnnotationI: self.term_annotations,
                    omero.model.TimestampAnnotationI: self.time_annotations}
            
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
        elif self.well is not None:
            aList = list(self.well.selectedWellSample().image().listAnnotations())
        
        for ann in aList:
            annClass = ann._obj.__class__
            if annClass in annTypes:
                if ann.ns == omero.constants.metadata.NSINSIGHTRATING:
                    self.rating_annotations.append(ann)
                elif ann.ns == omero.constants.namespaces.NSCOMPANIONFILE:
                    if ann.getFileName != omero.constants.annotation.file.ORIGINALMETADATA:
                        self.companion_files.append(ann)
                else:
                    annTypes[annClass].append(ann)

        self.text_annotations = self.sortByAttr(self.text_annotations, "details.creationEvent.time", True)
        self.file_annotations = self.sortByAttr(self.file_annotations, "details.creationEvent.time")
        self.rating_annotations = self.sortByAttr(self.rating_annotations, "details.creationEvent.time")
        self.tag_annotations = self.sortByAttr(self.tag_annotations, "textValue")
        
        self.txannSize = len(self.text_annotations)
        self.fileannSize = len(self.file_annotations)
        self.tgannSize = len(self.tag_annotations)

    
    def getTagsByObject(self):
        eid = self.conn.getGroupFromContext().isReadOnly() and self.conn.getEventContext().userId or None
        
        if self.image is not None:
            return list(self.image.listOrphanedAnnotations(eid=eid, anntype='Tag'))
        elif self.dataset is not None:
            return list(self.dataset.listOrphanedAnnotations(eid=eid, anntype='Tag'))
        elif self.project is not None:
            return list(self.project.listOrphanedAnnotations(eid=eid, anntype='Tag'))
        elif self.well is not None:
            return list(self.well.listOrphanedAnnotations(eid=eid, anntype='Tag'))
        elif self.plate is not None:
            return list(self.plate.listOrphanedAnnotations(eid=eid, anntype='Tag'))
        elif self.screen is not None:
            return list(self.screen.listOrphanedAnnotations(eid=eid, anntype='Tag'))
        else:
            eid = self.conn.getGroupFromContext().isReadOnly() and self.conn.getEventContext().userId or None
            return list(self.conn.listAnnotations(eid, 'tag'))
    
    def getFilesByObject(self):
        eid = self.conn.getGroupFromContext().isReadOnly() and self.conn.getEventContext().userId or None
        ns = [omero.constants.namespaces.NSCOMPANIONFILE, omero.constants.namespaces.NSEXPERIMENTERPHOTO]
        
        if self.image is not None:
            return list(self.image.listOrphanedAnnotations(eid=eid, ns=ns, anntype='File'))
        elif self.dataset is not None:
            return list(self.dataset.listOrphanedAnnotations(eid=eid, ns=ns, anntype='File'))
        elif self.project is not None:
            return list(self.project.listOrphanedAnnotations(eid=eid, ns=ns, anntype='File'))
        elif self.well is not None:
            return list(self.well.listOrphanedAnnotations(eid=eid, ns=ns, anntype='File'))
        elif self.plate is not None:
            return list(self.plate.listOrphanedAnnotations(eid=eid, ns=ns, anntype='File'))
        elif self.screen is not None:
            return list(self.screen.listOrphanedAnnotations(eid=eid, ns=ns, anntype='File'))
        else:
            eid = self.conn.getGroupFromContext().isReadOnly() and self.conn.getEventContext().userId or None
            return list(self.conn.listAnnotations(eid, 'file'))
    ####################################################################
    # Creation
    
    def createDataset(self, name, description):
        ds = omero.model.DatasetI()
        ds.name = rstring(str(name))
        if description != "" :
            ds.description = rstring(str(description))
        if self.project is not None:
            l_ds = omero.model.ProjectDatasetLinkI()
            l_ds.setParent(self.project._obj)
            l_ds.setChild(ds)
            ds.addProjectDatasetLink(l_ds)
        self.conn.saveObject(ds)
        
    def createProject(self, name, description):
        pr = omero.model.ProjectI()
        pr.name = rstring(str(name))
        if description != "" :
            pr.description = rstring(str(description))
        self.conn.saveObject(pr)
    
    def createScreen(self, name, description):
        sc = omero.model.ScreenI()
        sc.name = rstring(str(name))
        if description != "" :
            sc.description = rstring(str(description))
        self.conn.saveObject(sc)
    
    # Comment annotation
    def createCommentAnnotation(self, otype, content):
        otype = str(otype).lower()
        if not otype in ("project", "dataset", "image", "screen", "plate", "well"):
            raise AttributeError("Object type must be: project, dataset, image, screen, plate, well. ")
        if otype == 'well':
            otype = 'image'
            selfobject = self.well.selectedWellSample().image()
        else:
            selfobject = getattr(self, otype)

        ann = omero.model.CommentAnnotationI()
        ann.textValue = rstring(str(content))
        l_ann = getattr(omero.model, otype.title()+"AnnotationLinkI")()
        l_ann.setParent(selfobject._obj)
        l_ann.setChild(ann)
        self.conn.saveObject(l_ann)
    
    # Tag annotation 
    def createTagAnnotationOnly(self, tag, desc):
        ann = None
        try:
            ann = self.conn.findTag(tag, desc)._obj
        except:
            pass
        if ann is None:
            ann = omero.model.TagAnnotationI()
            ann.textValue = rstring(str(tag))
            ann.setDescription(rstring(str(desc)))
            self.conn.saveObject(ann)
     
    def createTagAnnotation(self, otype, tag, desc):
        otype = str(otype).lower()
        if not otype in ("project", "dataset", "image", "screen", "plate", "well"):
            raise AttributeError("Object type must be: project, dataset, image, screen, plate. ")
        if otype == 'well':
            otype = 'image'
            selfobject = self.well.selectedWellSample().image()
        else:
            selfobject = getattr(self, otype)
        
        ann = None
        try:
            ann = self.conn.findTag(tag, desc)._obj
        except:
            pass
        if ann is None:
            ann = omero.model.TagAnnotationI()
            ann.textValue = rstring(str(tag))
            ann.setDescription(rstring(str(desc)))            
            t_ann = getattr(omero.model, otype.title()+"AnnotationLinkI")()
            t_ann.setParent(selfobject._obj)
            t_ann.setChild(ann)
            self.conn.saveObject(t_ann)
        else:
            t_ann = getattr(self.conn, "get"+otype.title()+"AnnotationLink")(selfobject.id, ann.id.val)    
            if t_ann is None:
                t_ann.setParent(selfobject._obj)
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
            format = "application/octet-stream"
        return format
    
    def createFileAnnotation(self, otype, newFile):
        otype = str(otype).lower()
        if not otype in ("project", "dataset", "image", "screen", "plate", "well"):
            raise AttributeError("Object type must be: project, dataset, image, screen, plate. ")
        if otype == 'well':
            otype = 'image'
            selfobject = self.well.selectedWellSample().image()
        else:
            selfobject = getattr(self, otype)
        
        if newFile.content_type.startswith("image"):
            f = newFile.content_type.split("/") 
            format = f[1].upper()
        else:
            format = newFile.content_type
        oFile = omero.model.OriginalFileI()
        oFile.setName(rstring(str(newFile.name)));
        oFile.setPath(rstring(str(newFile.name)));
        oFile.setSize(rlong(long(newFile.size)));
        oFile.setSha1(rstring("pending"));
        oFile.setMimetype(rstring(str(format)));
        
        ofid = self.conn.saveAndReturnId(oFile);
        of = self.conn.saveAndReturnFile(newFile, ofid)
        
        fa = omero.model.FileAnnotationI()
        fa.setFile(of)
        l_ia = getattr(omero.model, otype.title()+"AnnotationLinkI")()
        l_ia.setParent(selfobject._obj)
        l_ia.setChild(fa)
        self.conn.saveObject(l_ia)
    
    def createCommentAnnotations(self, content, oids):  
        ann = omero.model.CommentAnnotationI()
        ann.textValue = rstring(str(content))
        ann = self.conn.saveAndReturnObject(ann)
   
        new_links = list() 
        for k in oids.keys():                
            if len(oids[k]) > 0:
                #listing = getattr(self.conn, "get"+k.lower().title()+"sById")
                for ob in self.conn.getObjects(k.lower().title(), oids[k]):
                    if isinstance(ob._obj, omero.model.WellI):
                        t = 'Image'
                        obj = ob.selectedWellSample().image()
                    else:
                        t = k.lower().title()
                        obj = ob
                    l_ann = getattr(omero.model, t+"AnnotationLinkI")()
                    l_ann.setParent(obj._obj)
                    l_ann.setChild(ann._obj)
                    new_links.append(l_ann)
        
        if len(new_links) > 0 :
            self.conn.saveArray(new_links)
    
    def createTagAnnotations(self, tag, desc, oids):
        ann = None
        try:
            ann = self.conn.findTag(tag, desc)
        except:
            pass
        if ann is None:
            ann = omero.model.TagAnnotationI()
            ann.textValue = rstring(str(tag))
            ann.setDescription(rstring(str(desc)))
            ann = self.conn.saveAndReturnObject(ann)
        
        new_links = list()
        for k in oids:
            if len(oids[k]) > 0:
                for ob in self.conn.getAnnotations(oids[k]):
                    if isinstance(ob._obj, omero.model.WellI):
                        t = 'Image'
                        obj = ob.selectedWellSample().image()
                    else:
                        t = k.lower().title()
                        obj = ob
                    l_ann = getattr(omero.model, t+"AnnotationLinkI")()
                    l_ann.setParent(obj._obj)
                    l_ann.setChild(ann._obj)
                    new_links.append(l_ann)
        if len(new_links) > 0 :
            self.conn.saveArray(new_links)
    
    def createFileAnnotations(self, newFile, oids):
        if newFile.content_type.startswith("image"):
            f = newFile.content_type.split("/") 
            format = f[1].upper()
        else:
            format = newFile.content_type
        oFile = omero.model.OriginalFileI()
        oFile.setName(rstring(str(newFile.name)));
        oFile.setPath(rstring(str(newFile.name)));
        oFile.setSize(rlong(long(newFile.size)));
        oFile.setSha1(rstring("pending"));
        oFile.setMimetype(rstring(str(format)));
        
        ofid = self.conn.saveAndReturnId(oFile);
        of = self.conn.saveAndReturnFile(newFile, ofid)
        
        fa = omero.model.FileAnnotationI()
        fa.setFile(of)
        fa = self.conn.saveAndReturnObject(fa)
        
        new_links = list()
        for k in oids:
            if len(oids[k]) > 0:
                for ob in self.conn.getAnnotations(oids[k]):                    
                    if isinstance(ob._obj, omero.model.WellI):
                        t = 'Image'
                        obj = ob.selectedWellSample().image()
                    else:
                        t = k.lower().title()
                        obj = ob
                    l_ann = getattr(omero.model, t+"AnnotationLinkI")()
                    l_ann.setParent(obj._obj)
                    l_ann.setChild(fa._obj)
                    new_links.append(l_ann)
        if len(new_links) > 0 :
            self.conn.saveArray(new_links)
    
    # Create links
    def createAnnotationLinks(self, otype, atype, ids):
        otype = str(otype).lower()
        if not otype in ("project", "dataset", "image", "screen", "plate", "well"):
            raise AttributeError("Object type must be: project, dataset, image, screen, plate.")
        atype = str(atype).lower()
        if not atype in ("tag", "comment", "file"):
            raise AttributeError("Object type must be: tag, comment, file.")
        if otype == 'well':
            otype = 'image'
            selfobject = self.well.selectedWellSample().image()
        else:
            selfobject = getattr(self, otype)
        
        new_links = list()
        for a in self.conn.getAnnotations(ids):
            ann = getattr(omero.model, otype.title()+"AnnotationLinkI")()
            ann.setParent(selfobject._obj)
            ann.setChild(a._obj)
            new_links.append(ann)
        
        failed = 0
        try:
            self.conn.saveArray(new_links)
        except omero.ValidationException, x:
            for l in new_links:
                try:
                    self.conn.saveObject(l)
                except:
                    failed+=1
        return failed
    
    def createAnnotationsLinks(self, atype, tids, oids):
        #TODO: check if link already exist !!!
        atype = str(atype).lower()
        if not atype.lower() in ("tag", "comment", "file"):
            raise AttributeError("Object type must be: tag, comment, file.")
        
        new_links = list()
        for k in oids:
            if len(oids[k]) > 0:
                for ob in self.conn.getObjects(k.lower().title(), oids[k]):
                    for a in self.conn.getAnnotations(tids):
                        if isinstance(ob._obj, omero.model.WellI):
                            t = 'Image'
                            obj = ob.selectedWellSample().image()
                        else:
                            t = k.lower().title()
                            obj = ob
                        l_ann = getattr(omero.model, t+"AnnotationLinkI")()
                        l_ann.setParent(obj._obj)
                        l_ann.setChild(a._obj)
                        new_links.append(l_ann)
        failed = 0
        try:
            self.conn.saveArray(new_links)            
        except omero.ValidationException, x:
            for l in new_links:
                try:
                    self.conn.saveObject(l)
                except:
                    failed+=1
        return failed
            
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
    
    def move(self, parent, destination):
        if self.project is not None:
            return 'Cannot move project.'
        elif self.dataset is not None:
            if destination[0] == 'ds':
                return 'Cannot move dataset to dataset'
            elif destination[0] == 'pr':
                up_pdl = None
                pdls = self.dataset.getParentLinks()
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
                    new_pr = self.conn.getObject("Project", destination[1])
                    if len(parent) > 1:
                        up_pdl.setParent(new_pr._obj)
                        self.conn.saveObject(up_pdl._obj)
                    else:
                        up_pdl = omero.model.ProjectDatasetLinkI()
                        up_pdl.setChild(self.dataset._obj)
                        up_pdl.setParent(new_pr._obj)
                        self.conn.saveObject(up_pdl)
            elif destination[0] == '0':
                up_pdl = None
                pdls = list(self.dataset.getParentLinks())
                
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
        elif self.image is not None:
            if destination[0] == 'ds':
                up_dsl = None
                dsls = self.image.getParentLinks() #gets every links for child
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
                        up_dsl = omero.model.DatasetImageLinkI()
                        up_dsl.setChild(self.image._obj)
                        up_dsl.setParent(new_ds._obj)
                        self.conn.saveObject(up_dsl)
            elif destination[0] == 'pr':
                return 'Cannot move image to project.'
            elif destination[0] == '0' or destination[0] == 'orphaned':
                if parent[0] != destination[0]:
                    up_dsl = None
                    dsls = list(self.image.getParentLinks()) #gets every links for child
                    if len(dsls) == 1:
                        # gets old parent to delete
                        if dsls[0].parent.id.val == long(parent[1]):
                            up_dsl = dsls[0]
                            self.conn.deleteObject(up_dsl._obj)
                    else:
                        return 'This image is linked in multiple places. Please unlink the image first.'
            else:
                return 'Destination not supported.'
        elif self.screen is not None:
            return 'Cannot move screen.'
        elif self.plate is not None:
            if destination[0] == 'pl':
                return 'Cannot move plate to plate'
            elif destination[0] == 'sc':
                up_spl = None
                spls = self.plate.getParentLinks()
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
                        up_spl = omero.model.ScreenPlateLinkI()
                        up_spl.setChild(self.plate._obj)
                        up_spl.setParent(new_sc._obj)
                        self.conn.saveObject(up_spl)
            elif destination[0] == '0' or destination[0] == 'orphan':
                if parent[0] != destination[0]:
                    up_spl = None
                    spls = list(self.plate.getParentLinks()) #gets every links for child
                    if len(spls) == 1:
                        # gets old parent to delete
                        if spls[0].parent.id.val == long(parent[1]):
                            up_spl = spls[0]
                            self.conn.deleteObject(up_spl._obj)
                    else:
                        return 'This plate is linked in multiple places. Please unlink the plate first.'
            else:
                return 'Destination not supported.'
        else:
            return 'No data was choosen.'
        return 
    
    def remove(self, parent):
        if self.dataset is not None:
            if parent[0] == 'pr':
                for pdl in self.dataset.getParentLinks([parent[1]]):
                    if pdl is not None:
                        self.conn.deleteObject(pdl._obj)
        elif self.plate is not None:
            if parent[0] == 'sc':
                for spl in self.plate.getParentLinks([parent[1]]):
                    if spl is not None:
                        self.conn.deleteObject(spl._obj)
        elif self.image is not None:
            if parent[0] == 'ds':
                for dil in self.image.getParentLinks([parent[1]]):
                    if dil is not None:
                        self.conn.deleteObject(dil._obj)
        else:
            raise AttributeError("Attribute not specified. Cannot be removed.")
    
    def removemany(self, images):
        if self.dataset is not None:
            dil = self.dataset.getParentLinks('image', images)
            if dil is not None:
                self.conn.deleteObject(dil._obj)            
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
            new_dsl = omero.model.DatasetImageLinkI()
            new_dsl.setChild(im._obj)
            new_dsl.setParent(ds._obj)
            self.conn.saveObject(new_dsl)
            return True
    
    def copyImagesToDataset(self, images, dataset):
           if dataset is None:
               pass
           else:
               ims = self.conn.getObjects("Image", images)
               ds = self.conn.getDataset(dataset[1])
               link_array = list()
               for im in ims:
                   new_dsl = omero.model.DatasetImageLinkI()
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
            pr = self.conn.getObject("Project", destination[1])
            new_pdl = omero.model.ProjectDatasetLinkI()
            new_pdl.setChild(ds._obj)
            new_pdl.setParent(pr._obj)
            self.conn.saveObject(new_pdl)
            return True
   
    def copyDatasetsToProject(self, datasets, project):
        if project is None:
            pass
        else:
            dss = self.conn.getObjects("Dataset", datasets)
            pr = self.conn.getObject("Project", project[1])
            link_array = list()
            for ds in dss:
                new_pdl = omero.model.ProjectDatasetLinkI()
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
            new_spl = omero.model.ScreenPlateLinkI()
            new_spl.setChild(pl._obj)
            new_spl.setParent(sc._obj)
            self.conn.saveObject(new_spl)
            return True
    
    def copyPlatesToScreen(self, plates, screen):
        if screen is None:
            pass
        else:
            pls = self.conn.getObjects("Plate", plates)
            sc = self.conn.getScreen(screen[1])
            link_array = list()
            for pl in pls:
                new_spl = omero.model.ScreenPlateLinkI()
                new_spl.setChild(pl._obj)
                new_spl.setParent(sc._obj)
                link_array.append(new_spl)
            self.conn.saveArray(link_array)
            return True


    ##########################################################
    # Delete
    
    def deleteItem(self, child=False, anns=False):
        handle = None
        if self.image:
            handle = self.conn.deleteImage(self.image.id, anns)
        elif self.dataset:
            handle = self.conn.deleteDataset(self.dataset.id, child, anns)
        elif self.project:
            handle = self.conn.deleteProject(self.project.id, child, anns)
        elif self.screen:
            handle = self.conn.deleteScreen(self.screen.id, child, anns)
        elif self.plate:
            handle = self.conn.deletePlate(self.plate.id, anns)
        return handle
    
    def deleteImages(self, ids, anns=False):
        return self.conn.deleteImages(ids, anns)
        