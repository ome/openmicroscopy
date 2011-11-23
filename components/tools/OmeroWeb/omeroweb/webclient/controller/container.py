#!/usr/bin/env python
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

import omero
from omero.rtypes import *
from django.core.urlresolvers import reverse
from django.utils.encoding import smart_str
import logging

logger = logging.getLogger('web-container')

from webclient.controller import BaseController

class BaseContainer(BaseController):
    
    project = None
    screen = None
    dataset = None
    plate = None
    acquisition = None
    well = None
    image = None
    tag = None
    file = None
    comment = None
    tags = None
    
    index = None
    containers = None
    experimenter = None
    
    c_size = 0
    
    text_annotations = None
    txannSize = 0
    long_annotations = None
    file_annotations = None
    
    orphaned = False
    
    def __init__(self, conn, project=None, dataset=None, image=None, screen=None, plate=None, acquisition=None, well=None, tag=None, tagset=None, file=None, comment=None, annotation=None, index=None, orphaned=None, **kw):
        BaseController.__init__(self, conn)
        if project is not None:
            self.project = self.conn.getObject("Project", project)
            if self.project is None:
                raise AttributeError("We are sorry, but that project (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(project))
            if self.project._obj is None:
                raise AttributeError("We are sorry, but that project (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(project))
        if dataset is not None:
            self.dataset = self.conn.getObject("Dataset", dataset)
            if self.dataset is None:
                raise AttributeError("We are sorry, but that dataset (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(dataset))
            if self.dataset._obj is None:
                raise AttributeError("We are sorry, but that dataset (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(dataset))
        if screen is not None:
            self.screen = self.conn.getObject("Screen", screen)
            if self.screen is None:
                raise AttributeError("We are sorry, but that screen (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(screen))
            if self.screen._obj is None:
                raise AttributeError("We are sorry, but that screen (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(screen))
        if plate is not None:
            self.plate = self.conn.getObject("Plate", plate)
            if self.plate is None:
                raise AttributeError("We are sorry, but that plate (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(plate))
            if self.plate._obj is None:
                raise AttributeError("We are sorry, but that plate (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(plate)) 
        if acquisition is not None:
            self.acquisition = self.conn.getObject("PlateAcquisition", acquisition)
            if self.acquisition is None:
                raise AttributeError("We are sorry, but that plate acquisition (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(acquisition))
            if self.acquisition._obj is None:
                raise AttributeError("We are sorry, but that plate acquisition (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(acquisition))
        if image is not None:
            self.image = self.conn.getObject("Image", image)
            if self.image is None:
                raise AttributeError("We are sorry, but that image (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(image))
            if self.image._obj is None:
                raise AttributeError("We are sorry, but that image (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(image))
        if well is not None:
            self.well = self.conn.getObject("Well", well)
            if self.well is None:
                raise AttributeError("We are sorry, but that well (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(well))
            if self.well._obj is None:
                raise AttributeError("We are sorry, but that well (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(well))
            if index is not None:
                self.well.index = index
        if tag is not None:
            self.tag = self.conn.getObject("Annotation", tag)
            if self.tag is None:
                raise AttributeError("We are sorry, but that tag (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(tag))
            if self.tag._obj is None:
                raise AttributeError("We are sorry, but that tag (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(tag))
        if tagset is not None:
            self.tag = self.conn.getObject("Annotation", tagset)
            if self.tag is None:
                raise AttributeError("We are sorry, but that tag (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(tag))
            if self.tag._obj is None:
                raise AttributeError("We are sorry, but that tag (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(tag))
        if comment is not None:
            self.comment = self.conn.getObject("Annotation", comment)
            if self.comment is None:
                raise AttributeError("We are sorry, but that comment (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(comment))
            if self.comment._obj is None:
                raise AttributeError("We are sorry, but that comment (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(comment))
        if file is not None:
            self.file = self.conn.getObject("Annotation", file)
            if self.file is None:
                raise AttributeError("We are sorry, but that file (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(file))
            if self.file._obj is None:
                raise AttributeError("We are sorry, but that file (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(file))
        if annotation is not None:
            self.annotation = self.conn.getObject("Annotation", annotation)
            if self.annotation is None:
                raise AttributeError("We are sorry, but that annotation (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(annotation))
            if self.annotation._obj is None:
                raise AttributeError("We are sorry, but that annotation (id:%s) does not exist, or if it does, you have no permission to see it.  Contact the user you think might share that data with you." % str(annotation))
        if orphaned:
            self.orphaned = True

    def openAstexViewerCompatible(self):
        """
        Is the image suitable to be viewed with the Volume viewer 'Open Astex Viewer' applet?
        Image must be a 'volume' of suitable dimensions and not too big.
        """
        from django.conf import settings 
        MAX_SIDE = settings.OPEN_ASTEX_MAX_SIDE     # default is 400
        MIN_SIDE = settings.OPEN_ASTEX_MIN_SIDE     # default is 20
        MAX_VOXELS = settings.OPEN_ASTEX_MAX_VOXELS # default is 15625000 (250 * 250 * 250)

        if self.image is None:
            return False
        sizeZ = self.image.getSizeZ()
        if self.image.getSizeC() > 1: return False
        sizeX = self.image.getSizeX()
        sizeY = self.image.getSizeY()
        if sizeZ < MIN_SIDE or sizeX < MIN_SIDE or sizeY < MIN_SIDE: return False
        if sizeX > MAX_SIDE or sizeY > MAX_SIDE or sizeZ > MAX_SIDE: return False
        voxelCount = (sizeX * sizeY * sizeZ)
        if voxelCount > MAX_VOXELS: return False

        try:    # if scipy ndimage is not available for interpolation, can only handle smaller images
            import scipy.ndimage
        except ImportError:
            logger.debug("Failed to import scipy.ndimage - Open Astex Viewer limited to display of smaller images.")
            MAX_VOXELS = (160 * 160 * 160)
            if voxelCount > MAX_VOXELS: return False

        return True

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
        elif self.well.getWellSample().image is not None:
            om = self.well.getWellSample().image().loadOriginalMetadata()
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
                self.channel_metadata = self.well.getWellSample().image().getChannels()
        except:
            pass
        
        if self.channel_metadata is None:
            self.channel_metadata = list()
        
    def loadTags(self, eid=None):
        if eid is not None:
            self.experimenter = self.conn.getObject("Experimenter", eid)
        else:            
            eid = self.conn.getEventContext().userId
        
        self.tags = list(self.conn.listTags(eid))
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
            self.experimenter = self.conn.getObject("Experimenter", eid)  
        
        im_list = list(self.conn.listImagesInDataset(oid=did, eid=eid, page=page))
        # Not displaying annotation icons (same as Insight). #5514.
        #im_list_with_counters = list()
        
        #im_ids = [im.id for im in im_list]
        #if len(im_ids) > 0:
        #    im_annotation_counter = self.conn.getCollectionCount("Image", "annotationLinks", im_ids)
            
        #    for im in im_list:
        #        im.annotation_counter = im_annotation_counter.get(im.id)
        #        im_list_with_counters.append(im)
        
        im_list_with_counters = im_list
        im_list_with_counters.sort(key=lambda x: x.getName().lower())
        self.containers = {'images': im_list_with_counters}
        self.c_size = self.conn.getCollectionCount("Dataset", "imageLinks", [long(did)])[long(did)]
        
        if page is not None:
            self.paging = self.doPaging(page, len(im_list_with_counters), self.c_size)
    
    def listContainerHierarchy(self, eid=None):
        if eid is not None:
            self.experimenter = self.conn.getObject("Experimenter", eid)
        else:
            eid = self.conn.getEventContext().userId
        
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

        pr_list_with_counters.sort(key=lambda x: x.getName() and x.getName().lower())
        ds_list_with_counters.sort(key=lambda x: x.getName() and x.getName().lower())
        sc_list_with_counters.sort(key=lambda x: x.getName() and x.getName().lower())
        pl_list_with_counters.sort(key=lambda x: x.getName() and x.getName().lower())

        self.orphans = self.conn.countOrphans("Image", eid)
        
        self.containers={'projects': pr_list_with_counters, 'datasets': ds_list_with_counters, 'screens': sc_list_with_counters, 'plates': pl_list_with_counters}
        self.c_size = len(pr_list_with_counters)+len(ds_list_with_counters)+len(sc_list_with_counters)+len(pl_list_with_counters)
    
    def listOrphanedImages(self, eid=None, page=None):
        if eid is not None:
            self.experimenter = self.conn.getObject("Experimenter", eid)
        else:
            eid = self.conn.getEventContext().userId
        
        im_list = list(self.conn.listOrphans("Image", eid=eid, page=page))
        # Not displaying annotation icons (same as Insight). #5514.
        #im_list_with_counters = list()
        
        #im_ids = [im.id for im in im_list]
        #if len(im_ids) > 0:
            #im_annotation_counter = self.conn.getCollectionCount("Image", "annotationLinks", im_ids)
            
            #for im in im_list:
                #im.annotation_counter = im_annotation_counter.get(im.id)
                #im_list_with_counters.append(im)
        
        im_list_with_counters = im_list
        im_list_with_counters.sort(key=lambda x: x.getName().lower())
        self.containers = {'orphaned': True, 'images': im_list_with_counters}
        self.c_size = self.conn.countOrphans("Image", eid=eid)
        
        if page is not None:
            self.paging = self.doPaging(page, len(im_list_with_counters), self.c_size)

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
        elif self.acquisition is not None:
            aList = list(self.acquisition.listAnnotations())
        elif self.well is not None:
            aList = list(self.well.getWellSample().image().listAnnotations())

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

        self.text_annotations.sort(key=lambda x: x.creationEventDate(), reverse=True)
        self.file_annotations.sort(key=lambda x: x.creationEventDate())
        self.rating_annotations.sort(key=lambda x: x.creationEventDate())
        self.tag_annotations.sort(key=lambda x: x.textValue)
        
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
            return list(self.well.getWellSample().image().listOrphanedAnnotations(eid=eid, anntype='Tag'))
        elif self.plate is not None:
            return list(self.plate.listOrphanedAnnotations(eid=eid, anntype='Tag'))
        elif self.screen is not None:
            return list(self.screen.listOrphanedAnnotations(eid=eid, anntype='Tag'))
        else:
            eid = self.conn.getGroupFromContext().isReadOnly() and self.conn.getEventContext().userId or None
            if eid is not None:
                params = omero.sys.Parameters()
                params.theFilter = omero.sys.Filter()
                params.theFilter.ownerId = omero.rtypes.rlong(eid)
                return list(self.conn.getObjects("TagAnnotation", params=params))
            return list(self.conn.getObjects("TagAnnotation"))
    
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
            return list(self.well.getWellSample().image().listOrphanedAnnotations(eid=eid, ns=ns, anntype='File'))
        elif self.plate is not None:
            return list(self.plate.listOrphanedAnnotations(eid=eid, ns=ns, anntype='File'))
        elif self.screen is not None:
            return list(self.screen.listOrphanedAnnotations(eid=eid, ns=ns, anntype='File'))
        else:
            eid = self.conn.getGroupFromContext().isReadOnly() and self.conn.getEventContext().userId or None
            if eid is not None:
                params = omero.sys.Parameters()
                params.theFilter = omero.sys.Filter()
                params.theFilter.ownerId = omero.rtypes.rlong(eid)
                return list(self.conn.listFileAnnotations(params=params))
            return list(self.conn.listFileAnnotations())
    ####################################################################
    # Creation
    
    def createDataset(self, name, description=None):
        ds = omero.model.DatasetI()
        ds.name = rstring(str(name))
        if description is not None and description != "" :
            ds.description = rstring(str(description))
        if self.project is not None:
            l_ds = omero.model.ProjectDatasetLinkI()
            l_ds.setParent(self.project._obj)
            l_ds.setChild(ds)
            ds.addProjectDatasetLink(l_ds)
        return self.conn.saveAndReturnId(ds)
        
    def createProject(self, name, description=None):
        pr = omero.model.ProjectI()
        pr.name = rstring(str(name))
        if description is not None and description != "" :
            pr.description = rstring(str(description))
        return self.conn.saveAndReturnId(pr)
    
    def createScreen(self, name, description=None):
        sc = omero.model.ScreenI()
        sc.name = rstring(str(name))
        if description is not None and description != "" :
            sc.description = rstring(str(description))
        return self.conn.saveAndReturnId(sc)
    
    # Comment annotation
    def createCommentAnnotation(self, otype, content):
        otype = str(otype).lower()
        if not otype in ("project", "dataset", "image", "screen", "plate", "acquisition", "well"):
            raise AttributeError("Object type must be: project, dataset, image, screen, plate, acquisition, well. ")
        if otype == 'well':
            otype = 'Image'
            selfobject = self.well.getWellSample().image()
        elif otype == 'acquisition':
            otype = 'PlateAcquisition'
            selfobject = self.acquisition
        else:
            selfobject = getattr(self, otype)
            otype = otype.title()
            
        ann = omero.model.CommentAnnotationI()
        ann.textValue = rstring(str(content))
        l_ann = getattr(omero.model, otype+"AnnotationLinkI")()
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
        if not otype in ("project", "dataset", "image", "screen", "plate", "acquisition", "well"):
            raise AttributeError("Object type must be: project, dataset, image, screen, plate, acquisition, well. ")
        if otype == 'well':
            otype = 'Image'
            selfobject = self.well.getWellSample().image()
        elif otype == 'acquisition':
            otype = 'PlateAcquisition'
            selfobject = self.acquisition
        else:
            selfobject = getattr(self, otype)
            otype = otype.title()
        
        ann = None
        try:
            ann = self.conn.findTag(tag, desc)._obj
        except:
            pass
        if ann is None:
            ann = omero.model.TagAnnotationI()
            ann.textValue = rstring(str(tag))
            ann.setDescription(rstring(str(desc)))            
            t_ann = getattr(omero.model, otype+"AnnotationLinkI")()
            t_ann.setParent(selfobject._obj)
            t_ann.setChild(ann)
            self.conn.saveObject(t_ann)
        else:
            # Tag exists - check it isn't already linked to parent by this user
            params = omero.sys.Parameters()
            params.theFilter = omero.sys.Filter()
            params.theFilter.ownerId = rlong(self.conn.getUser().id) # linked by current user
            links = self.conn.getAnnotationLinks(otype, parent_ids=[selfobject.id], ann_ids=[ann.id.val], params=params)
            links = list(links)
            if len(links) == 0:     # current user has not already tagged this object
                t_ann = getattr(omero.model, otype+"AnnotationLinkI")()
                t_ann.setParent(selfobject._obj)
                t_ann.setChild(ann)
                self.conn.saveObject(t_ann)
    
    def checkMimetype(self, file_type):
        if file_type is None or len(file_type) == 0:
            file_type = "application/octet-stream"
        return file_type
            
    def createFileAnnotation(self, otype, newFile):
        otype = str(otype).lower()
        if not otype in ("project", "dataset", "image", "screen", "plate", "acquisition", "well"):
            raise AttributeError("Object type must be: project, dataset, image, screen, plate, acquisition, well. ")
        if otype == 'well':
            otype = 'Image'
            selfobject = self.well.getWellSample().image()
        elif otype == 'acquisition':
            otype = 'PlateAcquisition'
            selfobject = self.acquisition
        else:
            selfobject = getattr(self, otype)
            otype = otype.title()
            
        format = self.checkMimetype(newFile.content_type)
        
        oFile = omero.model.OriginalFileI()
        oFile.setName(rstring(smart_str(newFile.name)));
        oFile.setPath(rstring(smart_str(newFile.name)));
        oFile.setSize(rlong(long(newFile.size)));
        oFile.setSha1(rstring("pending"));
        oFile.setMimetype(rstring(str(format)));
        
        ofid = self.conn.saveAndReturnId(oFile);
        of = self.conn.saveAndReturnFile(newFile, ofid)
        
        fa = omero.model.FileAnnotationI()
        fa.setFile(of)
        l_ia = getattr(omero.model, otype+"AnnotationLinkI")()
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
                for ob in oids[k]:
                    if isinstance(ob._obj, omero.model.WellI):
                        t = 'Image'
                        obj = ob.getWellSample().image()
                    elif isinstance(ob._obj, omero.model.PlateAcquisitionI):
                        t = 'PlateAcquisition'
                        obj = ob
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
                for ob in oids[k]:
                    if isinstance(ob._obj, omero.model.WellI):
                        t = 'Image'
                        obj = ob.getWellSample().image()
                    elif isinstance(ob._obj, omero.model.PlateAcquisitionI):
                        t = 'PlateAcquisition'
                        obj = ob
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
        format = self.checkMimetype(newFile.content_type)
        
        oFile = omero.model.OriginalFileI()
        oFile.setName(rstring(smart_str(newFile.name)));
        oFile.setPath(rstring(smart_str(newFile.name)));
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
                for ob in oids[k]:
                    if isinstance(ob._obj, omero.model.WellI):
                        t = 'Image'
                        obj = ob.getWellSample().image()
                    elif isinstance(ob._obj, omero.model.PlateAcquisitionI):
                        t = 'PlateAcquisition'
                        obj = ob
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
        if not otype in ("project", "dataset", "image", "screen", "plate", "acquisition", "well"):
            raise AttributeError("Object type must be: project, dataset, image, screen, plate, acquisition, well.")
        atype = str(atype).lower()
        if not atype in ("tag", "comment", "file"):
            raise AttributeError("Object type must be: tag, comment, file.")
        if otype == 'well':
            otype = 'Image'
            selfobject = self.well.getWellSample().image()
        elif otype == 'acquisition':
            otype = 'PlateAcquisition'
            selfobject = self.acquisition
        else:
            selfobject = getattr(self, otype)
            otype = otype.title()
            
        new_links = list()
        for a in self.conn.getObjects("Annotation", ids):
            ann = getattr(omero.model, otype+"AnnotationLinkI")()
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
                if k.lower() == 'acquisitions':
                    t = 'PlateAcquisition'
                else:
                    t = k.lower().title()
                for ob in self.conn.getObjects(t, [o.id for o in oids[k]]):
                    for a in self.conn.getObjects("Annotation", tids):
                        if isinstance(ob._obj, omero.model.WellI):
                            t = 'Image'
                            obj = ob.getWellSample().image()
                        else:
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
    
    def updateDescription(self, o_type, o_id, description=None):
        obj = getattr(self, o_type)._obj
        if description is not None and description != "" :
            obj.description = rstring(str(description))
        else:
            obj.description = None
        self.conn.saveObject(obj)
    
    def updateName(self, o_type, o_id, name):
        obj = getattr(self, o_type)._obj
        if o_type not in ('tag', 'tagset'):
            obj.name = rstring(str(name))
        else:
            obj.textValue = rstring(str(name))
        self.conn.saveObject(obj)
    
    def updateImage(self, name, description=None):
        img = self.image._obj
        img.name = rstring(str(name))
        if description is not None and description != "" :
            img.description = rstring(str(description))
        else:
            img.description = None
        self.conn.saveObject(img)
    
    def updateDataset(self, name, description=None):
        container = self.dataset._obj
        container.name = rstring(str(name))
        if description is not None and description != "" :
            container.description = rstring(str(description))
        else:
            container.description = None
        self.conn.saveObject(container)
    
    def updatePlate(self, name, description=None):
        container = self.plate._obj
        container.name = rstring(str(name))
        if description is not None and description != "" :
            container.description = rstring(str(description))
        else:
            container.description = None
        self.conn.saveObject(container)
    
    def updateProject(self, name, description=None):
        container = self.project._obj
        container.name = rstring(str(name))
        if description is not None and description != "" :
            container.description = rstring(str(description))
        else:
            container.description = None
        self.conn.saveObject(container)
    
    def updateScreen(self, name, description=None):
        container = self.screen._obj
        container.name = rstring(str(name))
        if description is not None and description != "" :
            container.description = rstring(str(description))
        else:
            container.description = None
        self.conn.saveObject(container)
    
    def saveCommentAnnotation(self, content):
        ann = self.comment._obj
        ann.textValue = rstring(str(content))
        self.conn.saveObject(ann)
    
    def saveTagAnnotation(self, tag, description=None):
        ann = self.tag._obj
        ann.textValue = rstring(str(tag))
        if description is not None and description != "" :
            ann.description = rstring(str(description))
        else:
            ann.description = None
        self.conn.saveObject(ann)
    
    def move(self, parent, destination):
        if self.project is not None:
            return 'Cannot move project.'
        elif self.dataset is not None:
            if destination[0] == 'dataset':
                return 'Cannot move dataset to dataset'
            elif destination[0] == 'project':
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
                        self.conn.deleteObjectDirect(up_pdl._obj)
                else:
                    new_pr = self.conn.getObject("Project", destination[1])
                    if parent[0] not in ('experimenter', 'orphaned'):
                        up_pdl.setParent(new_pr._obj)
                        self.conn.saveObject(up_pdl._obj)
                    else:
                        up_pdl = omero.model.ProjectDatasetLinkI()
                        up_pdl.setChild(self.dataset._obj)
                        up_pdl.setParent(new_pr._obj)
                        self.conn.saveObject(up_pdl)
            elif destination[0] == 'experimenter':
                up_pdl = None
                for p in self.dataset.getParentLinks():
                    if p.parent.id.val == long(parent[1]):
                        up_pdl = p
                        self.conn.deleteObjectDirect(up_pdl._obj)
            elif destination[0] == 'orphaned':
                return 'Cannot move dataset to orphaned images.'
            else:
                return 'Destination not supported.'
        elif self.image is not None:
            if destination[0] == 'dataset':
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
                        self.conn.deleteObjectDirect(up_dsl._obj)
                else:
                    # update link to new destination
                    new_ds = self.conn.getObject("Dataset", destination[1])
                    if parent[0] not in ('experimenter', 'orphaned'):
                        up_dsl.setParent(new_ds._obj)
                        self.conn.saveObject(up_dsl._obj)
                    else:
                        up_dsl = omero.model.DatasetImageLinkI()
                        up_dsl.setChild(self.image._obj)
                        up_dsl.setParent(new_ds._obj)
                        self.conn.saveObject(up_dsl)
            elif destination[0] == 'project':
                return 'Cannot move image to project.'
            elif destination[0] == 'experimenter' or destination[0] == 'orphaned':
                if parent[0] != destination[0]:
                    up_dsl = None
                    dsls = list(self.image.getParentLinks()) #gets every links for child
                    if len(dsls) == 1:
                        # gets old parent to delete
                        if dsls[0].parent.id.val == long(parent[1]):
                            up_dsl = dsls[0]
                            self.conn.deleteObjectDirect(up_dsl._obj)
                    else:
                        return 'This image is linked in multiple places. Please unlink the image first.'
            else:
                return 'Destination not supported.'
        elif self.screen is not None:
            return 'Cannot move screen.'
        elif self.plate is not None:
            if destination[0] == 'plate':
                return 'Cannot move plate to plate'
            elif destination[0] == 'screen':
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
                        self.conn.deleteObjectDirect(up_spl._obj)
                else:
                    new_sc = self.conn.getObject("Screen", destination[1])
                    if parent[0] not in ('experimenter', 'orphaned'):
                        up_spl.setParent(new_sc._obj)
                        self.conn.saveObject(up_spl._obj)
                    else:
                        up_spl = omero.model.ScreenPlateLinkI()
                        up_spl.setChild(self.plate._obj)
                        up_spl.setParent(new_sc._obj)
                        self.conn.saveObject(up_spl)
            elif destination[0] == 'experimenter' or destination[0] == 'orphaned':
                if parent[0] != destination[0]:
                    up_spl = None
                    spls = list(self.plate.getParentLinks()) #gets every links for child
                    if len(spls) == 1:
                        # gets old parent to delete
                        if spls[0].parent.id.val == long(parent[1]):
                            up_spl = spls[0]
                            self.conn.deleteObjectDirect(up_spl._obj)
                    else:
                        return 'This plate is linked in multiple places. Please unlink the plate first.'
            else:
                return 'Destination not supported.'
        else:
            return 'No data was choosen.'
        return 
    
    def remove(self, parent):
        if self.tag:
            for al in self.tag.getParentLinks(str(parent[0]), [long(parent[1])]):
                if al is not None and al.details.owner.id.val == self.conn.getUser().id:
                    self.conn.deleteObjectDirect(al._obj)
        elif self.file:
            for al in self.file.getParentLinks(str(parent[0]), [long(parent[1])]):
                if al is not None and al.details.owner.id.val == self.conn.getUser().id:
                    self.conn.deleteObjectDirect(al._obj)
        elif self.comment:
            # remove the comment from specified parent
            for al in self.comment.getParentLinks(str(parent[0]), [long(parent[1])]):
                if al is not None and al.details.owner.id.val == self.conn.getUser().id:
                    self.conn.deleteObjectDirect(al._obj)
            # if comment is orphan, delete it directly
            orphan = True
            for parentType in ["Project", "Dataset", "Image", "Screen", "Plate"]:
                annLinks = list(self.conn.getAnnotationLinks(parentType, ann_ids=[self.comment.id]))
                if len(annLinks) > 0:
                    orphan = False
                    break
            if orphan:
                self.conn.deleteObjectDirect(self.comment._obj)
        
        elif self.dataset is not None:
            if parent[0] == 'project':
                for pdl in self.dataset.getParentLinks([parent[1]]):
                    if pdl is not None:
                        self.conn.deleteObjectDirect(pdl._obj)
        elif self.plate is not None:
            if parent[0] == 'screen':
                for spl in self.plate.getParentLinks([parent[1]]):
                    if spl is not None:
                        self.conn.deleteObjectDirect(spl._obj)
        elif self.image is not None:
            if parent[0] == 'dataset':
                for dil in self.image.getParentLinks([parent[1]]):
                    if dil is not None:
                        self.conn.deleteObjectDirect(dil._obj)
        else:
            raise AttributeError("Attribute not specified. Cannot be removed.")
    
    def removemany(self, images):
        if self.dataset is not None:
            dil = self.dataset.getParentLinks('image', images)
            if dil is not None:
                self.conn.deleteObjectDirect(dil._obj)
        else:
            raise AttributeError("Attribute not specified. Cannot be removed.")
    
    ##########################################################
    # Copy
    
    def paste(self, destination):
        if self.project is not None:
            return 'Cannot paste project.'
        elif self.dataset is not None:
            if destination[0] == 'dataset':
                return 'Cannot paste dataset to dataset'
            elif destination[0] == 'project':
                pdls = self.dataset.getParentLinks()
                already_there = None
                
                for pdl in pdls:
                    if pdl.parent.id.val == long(destination[1]):
                        already_there = True
                if already_there:
                    return 'Dataset is already there.'
                else:
                    new_pr = self.conn.getObject("Project", destination[1])
                    up_pdl = omero.model.ProjectDatasetLinkI()
                    up_pdl.setChild(self.dataset._obj)
                    up_pdl.setParent(new_pr._obj)
                    self.conn.saveObject(up_pdl)
            else:
                return 'Destination not supported.'
        elif self.image is not None:
            if destination[0] == 'dataset':
                dsls = self.image.getParentLinks() #gets every links for child
                already_there = None
                
                #checks links
                for dsl in dsls:
                    #if is already linked to destination
                    if dsl.parent.id.val == long(destination[1]):
                        already_there = True
                if already_there:
                    return 'Image is already there.'
                else:
                    # update link to new destination
                    new_ds = self.conn.getObject("Dataset", destination[1])                    
                    up_dsl = omero.model.DatasetImageLinkI()
                    up_dsl.setChild(self.image._obj)
                    up_dsl.setParent(new_ds._obj)
                    self.conn.saveObject(up_dsl)
            elif destination[0] == 'project':
                return 'Cannot copy image to project.'
            else:
                return 'Destination not supported.'
        elif self.screen is not None:
            return 'Cannot paste screen.'
        elif self.plate is not None:
            if destination[0] == 'plate':
                return 'Cannot move plate to plate'
            elif destination[0] == 'screen':
                spls = self.plate.getParentLinks()
                already_there = None
                
                for spl in spls:
                    if spl.parent.id.val == long(destination[1]):
                        already_there = True
                if already_there:
                    return 'Plate is already there.'
                else:
                    new_sc = self.conn.getObject("Screen", destination[1])
                    up_spl = omero.model.ScreenPlateLinkI()
                    up_spl.setChild(self.plate._obj)
                    up_spl.setParent(new_sc._obj)
                    self.conn.saveObject(up_spl)
            else:
                return 'Destination not supported.'
        else:
            return 'No data was choosen.'
    
    def copyImageToDataset(self, source, destination=None):
        if destination is None:
            dsls = self.conn.getDatasetImageLinks(source[1]) #gets every links for child
            for dsl in dsls:
                self.conn.deleteObjectDirect(dsl._obj)
        else:
            im = self.conn.getObject("Image", source[1])
            ds = self.conn.getObject("Dataset", destination[1])
            new_dsl = omero.model.DatasetImageLinkI()
            new_dsl.setChild(im._obj)
            new_dsl.setParent(ds._obj)
            self.conn.saveObject(new_dsl)
    
    def copyImagesToDataset(self, images, dataset):
        if dataset is not None and dataset[0] is not "dataset":
            ims = self.conn.getObjects("Image", images)
            ds = self.conn.getObject("Dataset", dataset[1])
            link_array = list()
            for im in ims:
                new_dsl = omero.model.DatasetImageLinkI()
                new_dsl.setChild(im._obj)
                new_dsl.setParent(ds._obj)
                link_array.append(new_dsl)
            self.conn.saveArray(link_array)
        raise AttributeError("Destination not supported")
    
    def copyDatasetToProject(self, source, destination=None):
        if destination is not None and destination[0] is not "project":
            ds = self.conn.getObject("Dataset", source[1])
            pr = self.conn.getObject("Project", destination[1])
            new_pdl = omero.model.ProjectDatasetLinkI()
            new_pdl.setChild(ds._obj)
            new_pdl.setParent(pr._obj)
            self.conn.saveObject(new_pdl)
        raise AttributeError("Destination not supported")
   
    def copyDatasetsToProject(self, datasets, project):
        if project is not None and project[0] is not "project":
            dss = self.conn.getObjects("Dataset", datasets)
            pr = self.conn.getObject("Project", project[1])
            link_array = list()
            for ds in dss:
                new_pdl = omero.model.ProjectDatasetLinkI()
                new_pdl.setChild(ds._obj)
                new_pdl.setParent(pr._obj)
                link_array.append(new_pdl)
            self.conn.saveArray(link_array)
        raise AttributeError("Destination not supported")
    
    def copyPlateToScreen(self, source, destination=None):
        if destination is not None and destination[0] is not "screen":
            pl = self.conn.getObject("Plate", source[1])
            sc = self.conn.getObject("Screen", destination[1])
            new_spl = omero.model.ScreenPlateLinkI()
            new_spl.setChild(pl._obj)
            new_spl.setParent(sc._obj)
            self.conn.saveObject(new_spl)
        raise AttributeError("Destination not supported")
    
    def copyPlatesToScreen(self, plates, screen):
        if screen is not None and screen[0] is not "screen":
            pls = self.conn.getObjects("Plate", plates)
            sc = self.conn.getObject("Screen", screen[1])
            link_array = list()
            for pl in pls:
                new_spl = omero.model.ScreenPlateLinkI()
                new_spl.setChild(pl._obj)
                new_spl.setParent(sc._obj)
                link_array.append(new_spl)
            self.conn.saveArray(link_array)
        raise AttributeError("Destination not supported")


    ##########################################################
    # Delete
    
    def deleteItem(self, child=False, anns=False):
        handle = None
        if self.image:
            handle = self.conn.deleteObjects("Image", [self.image.id], deleteAnns=anns)
        elif self.dataset:
            handle = self.conn.deleteObjects("Dataset", [self.dataset.id], deleteChildren=child, deleteAnns=anns)
        elif self.project:
            handle = self.conn.deleteObjects("Project", [self.project.id], deleteChildren=child, deleteAnns=anns)
        elif self.screen:
            handle = self.conn.deleteObjects("Screen", [self.screen.id], deleteChildren=child, deleteAnns=anns)
        elif self.plate:
            handle = self.conn.deleteObjects("Plate", [self.plate.id], deleteAnns=anns)
        elif self.comment:
            handle = self.conn.deleteObjects("Annotation", [self.comment.id], deleteAnns=anns)
        elif self.tag:
            handle = self.conn.deleteObjects("Annotation", [self.tag.id], deleteAnns=anns)
        elif self.file:
            handle = self.conn.deleteObjects("Annotation", [self.file.id], deleteAnns=anns)
        return handle
    
    def deleteObjects(self, otype, ids, child=False, anns=False):
        return self.conn.deleteObjects(otype, ids, deleteChildren=child, deleteAnns=anns)
        