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

from django.conf import settings

import omero
from omero.rtypes import *
from omero_model_CommentAnnotationI import CommentAnnotationI
from omero_model_UriAnnotationI import UriAnnotationI
from omero_model_LongAnnotationI import LongAnnotationI
from omero_model_TagAnnotationI import TagAnnotationI
from omero_model_FileAnnotationI import FileAnnotationI
from omero_model_OriginalFileI import OriginalFileI
from omero_model_ImageAnnotationLinkI import ImageAnnotationLinkI
from omero_model_DatasetAnnotationLinkI import DatasetAnnotationLinkI
from omero_model_ProjectAnnotationLinkI import ProjectAnnotationLinkI
from omero_model_DatasetI import DatasetI
from omero_model_ProjectI import ProjectI
from omero_model_DatasetImageLinkI import DatasetImageLinkI
from omero_model_ProjectDatasetLinkI import ProjectDatasetLinkI
from omero_model_PermissionsI import PermissionsI

from webclient.controller import BaseController

class BaseContainer(BaseController):
    
    project = None
    dataset = None
    image = None
    tag = None
    comment = None
    
    tags = None
    
    containers = None
    containersMyGroups = None
    myGroup = None
    experimenter = None
    
    c_size = 0
    c_mg_size = 0
    
    text_annotations = None
    txannSize = 0
    long_annotations = None
    url_annotations = None
    file_annotations = None
    urlannSize = 0
    
    orphaned = False
    
    def __init__(self, conn, o1_type=None, o1_id=None, o2_type=None, o2_id=None, o3_type=None, o3_id=None, metadata=False, tags=None, rtags=None, **kw):
        BaseController.__init__(self, conn)
        if o1_type == "project":
            self.project = self.conn.getProject(o1_id)
            if self.project is None:
                raise AttributeError("Project does not exist.")
            if self.project._obj is None:
                raise AttributeError("Project does not exist.")
            if o2_type == "dataset":
                self.dataset = self.conn.getDataset(o2_id)
                if self.dataset is None:
                    raise AttributeError("Dataset does not exist.")
                if self.dataset._obj is None:
                    raise AttributeError("Dataset does not exist.")
                if o3_type == "image":
                    self.image = self.conn.getImageWithMetadata(o3_id)
                    if self.image is None:
                        raise AttributeError("Image does not exist.")
                    if self.image._obj is None:
                        raise AttributeError("Image does not exist.")
        elif o1_type == "dataset":
            self.dataset = self.conn.getDataset(o1_id)
            if self.dataset is None:
                raise AttributeError("Dataset does not exist.")
            if self.dataset._obj is None:
                raise AttributeError("Dataset does not exist.")
            if o2_type == "image":
                self.image = self.conn.getImageWithMetadata(o2_id)
                if self.image is None:
                    raise AttributeError("Image does not exist.")
                if self.image._obj is None:
                    raise AttributeError("Image does not exist.")
        elif o1_type == "image":
            if metadata:
                self.image = self.conn.getImageWithMetadata(o1_id)
                if self.image is None:
                    raise AttributeError("Image does not exist.")
                if self.image._obj is None:
                    raise AttributeError("Image does not exist.")
                else:
                    self.image._loadPixels()
            else:
                self.image = self.conn.getImage(o1_id)
                if self.image is None:
                    raise AttributeError("Image does not exist.")
                if self.image._obj is None:
                    raise AttributeError("Image does not exist.")
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
        
    
    def saveMetadata(self, matadataType, metadataValue):
        metadata_rtype = {
            # ObjectiveSettings
            'correctionCollar':('double', 'ObjectiveSettings'), 'medium':('int', 'ObjectiveSettings', 'MediumI'), 
            'refractiveIndex':('double', 'ObjectiveSettings'),
            
            # Objective
            'correction':('int', 'Objective', 'CorrectionI'), 'calibratedMagnification':('double', 'Objective'), 'immersion':('int', 'Objective', 'ImmersionI'), 
            'iris':['bool', 'Objective'], 'lensNA':('double', 'Objective'), 'manufacturer':('string', 'Objective'), 'model':('string', 'Objective'), 
            'nominalMagnification':('int', 'Objective'), 'serialNumber':('string', 'Objective'), 'workingDistance':('double', 'Objective'),
            
            # ImagingEnvironment
            'airPressure':('int', 'ImagingEnvironment'), 'co2percent':('double', 'ImagingEnvironment'), 'humidity':('double', 'ImagingEnvironment'), 
            'temperature':('double', 'ImagingEnvironment'),
            
            # StageLabel
            'positionx':('double', 'StageLabel'), 'positiony':('double', 'StageLabel'), 'positionz':('double', 'StageLabel')
        }
        
        metadataFamily = metadata_rtype.get(matadataType)[1]
        m_rtype = metadata_rtype.get(matadataType)[0]
        enum = None
        try:
            m_class = metadata_rtype.get(matadataType)[2]
            m_name = matadataType[0].upper()+matadataType[1:]
            if m_class is not None:
                enum = self.conn.getEnumeration(m_class, metadataValue)
        except:
            pass
        
        #self.image._obj.getObjectiveSettings().getObjective().__dict__.has_key("_"+matadataType):
        meta = getattr(self.image, "get"+metadataFamily)()
        if meta is not None and meta._obj.__dict__.has_key("_"+matadataType):
            if enum is not None:
                setattr(meta._obj, matadataType, enum)
                self.conn.saveObject(meta._obj)
            else:
                if metadataValue == "":
                    setattr(meta._obj, matadataType, None)
                    self.conn.saveObject(meta._obj)
                else:
                    try:
                        if m_rtype == 'int':
                            setattr(meta._obj, matadataType, rint(int(metadataValue)))
                        elif m_rtype == 'float':
                            setattr(meta._obj, matadataType, rfloat(float(metadataValue)))
                        elif m_rtype == 'double':
                            setattr(meta._obj, matadataType, rdouble(float(metadataValue)))
                        elif m_rtype == 'string':
                            setattr(meta._obj, matadataType, rstring(str(metadataValue)))
                        elif m_rtype == 'bool':
                            setattr(meta._obj, matadataType, rbool(bool(metadataValue.lower())))
                        else:
                            raise "Cannot save the metadata"
                        self.conn.saveObject(meta._obj)
                    except:
                        raise
        else:
            pass
    
    def buildBreadcrumb(self, menu):
        if menu == 'new' or menu == 'addnew':
            self.eContext['breadcrumb'] = ['New container']
        elif menu == 'edit':
            if self.project is not None:
                self.eContext['breadcrumb'] = ['Edit project: %s' % (self.project.breadcrumbName())]
            elif self.dataset is not None:
                self.eContext['breadcrumb'] = ['Edit dataset: %s' % (self.dataset.breadcrumbName())]
            elif self.image is not None:
                self.eContext['breadcrumb'] = ['Edit image: %s' % (self.image.breadcrumbName())]
            elif self.tag is not None:
                self.eContext['breadcrumb'] = ['Edit tag: %s' % (self.tag.breadcrumbName())]
        elif self.orphaned:
            self.eContext['breadcrumb'] = ['<a href="/%s/%s/">%s</a>' % (settings.WEBCLIENT_ROOT_BASE, menu, menu.title()), "Orphaned images"]
        else:
            if self.tags is not None:
                try:
                    self.eContext['breadcrumb'] = ['<a href="/%s/%s/">%s</a>' % (settings.WEBCLIENT_ROOT_BASE, menu, menu.title()), 'Tags: %s | %s | %s | %s | %s' % (self.tags[0].breadcrumbName(), self.tags[1].breadcrumbName(), self.tags[2].breadcrumbName(), self.tags[3].breadcrumbName(), self.tags[4].breadcrumbName())]
                except:
                    try:
                        self.eContext['breadcrumb'] = ['<a href="/%s/%s/">%s</a>' % (settings.WEBCLIENT_ROOT_BASE, menu, menu.title()), 'Tags: %s | %s | %s | %s' % (self.tags[0].breadcrumbName(), self.tags[1].breadcrumbName(), self.tags[2].breadcrumbName(), self.tags[3].breadcrumbName())]
                    except:
                        try:
                            self.eContext['breadcrumb'] = ['<a href="/%s/%s/">%s</a>' % (settings.WEBCLIENT_ROOT_BASE, menu, menu.title()), 'Tags: %s | %s | %s' % (self.tags[0].breadcrumbName(), self.tags[1].breadcrumbName(), self.tags[2].breadcrumbName())]
                        except:
                            try:
                                self.eContext['breadcrumb'] = ['<a href="/%s/%s/">%s</a>' % (settings.WEBCLIENT_ROOT_BASE, menu, menu.title()), 'Tags: %s | %s' % (self.tags[0].breadcrumbName(), self.tags[1].breadcrumbName())]
                            except:
                                try:
                                    self.eContext['breadcrumb'] = ['<a href="/%s/%s/">%s</a>' % (settings.WEBCLIENT_ROOT_BASE, menu, menu.title()), 'Tag: %s' % (self.tags[0].breadcrumbName())]
                                except:
                                    self.eContext['breadcrumb'] = ['<a href="/%s/%s/">%s</a>' % (settings.WEBCLIENT_ROOT_BASE, menu, menu.title()), 'Tags']
            elif self.project is not None:
                self.eContext['breadcrumb'] = ['<a href="/%s/%s/">%s</a>' % (settings.WEBCLIENT_ROOT_BASE, menu, menu.title()),  
                            '<a href="/%s/%s/project/%i/">%s</a>' % (settings.WEBCLIENT_ROOT_BASE, menu, self.project.id, self.project.breadcrumbName())]
                if self.dataset is not None:
                    self.eContext['breadcrumb'].append('<a href="/%s/%s/project/%i/dataset/%i/">%s</a>' % (settings.WEBCLIENT_ROOT_BASE, menu, self.project.id, self.dataset.id, self.dataset.breadcrumbName()))
                    if self.image is not None:
                        self.eContext['breadcrumb'].append('<a href="/%s/%s/project/%i/dataset/%i/image/%i/">%s</a>' % (settings.WEBCLIENT_ROOT_BASE, menu, self.project.id, self.dataset.id, self.image.id, self.image.breadcrumbName()))
            elif self.dataset is not None:
                self.eContext['breadcrumb'] = ['<a href="/%s/%s/">%s</a>' % (settings.WEBCLIENT_ROOT_BASE, menu, menu.title()),  
                            '<a href="/%s/%s/dataset/%i/">%s</a>' % (settings.WEBCLIENT_ROOT_BASE, menu, self.dataset.id, self.dataset.breadcrumbName())]
                if self.image is not None:
                    self.eContext['breadcrumb'].append('<a href="/%s/%s/dataset/%i/image/%i/">%s</a>' % (settings.WEBCLIENT_ROOT_BASE, menu, self.dataset.id, self.image.id, self.image.breadcrumbName()))
            elif self.image is not None:
                self.eContext['breadcrumb'] = ['<a href="/%s/%s/">%s</a>' % (settings.WEBCLIENT_ROOT_BASE, menu, menu.title()),  
                            "%s" % (self.image.breadcrumbName())]
            else:
                self.eContext['breadcrumb'] = [menu.title()] 
    
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
            pr_child_counter = self.conn.getCollectionCount("Project", "datasetLinks", pr_ids)
            pr_annotation_counter = self.conn.getCollectionCount("Project", "annotationLinks", pr_ids)
            
            for pr in pr_list:
                pr.child_counter = pr_child_counter.get(pr.id)
                pr.annotation_counter = pr_annotation_counter.get(pr.id)
                pr_list_with_counters.append(pr)
        
        ds_ids = [ds.id for ds in ds_list]
        if len(ds_ids) > 0:
            ds_child_counter = self.conn.getCollectionCount("Dataset", "imageLinks", ds_ids)
            ds_annotation_counter = self.conn.getCollectionCount("Dataset", "annotationLinks", ds_ids)
            
            for ds in ds_list:
                ds.child_counter = ds_child_counter.get(ds.id)
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
            self.hierarchy = self.conn.findContainerHierarchies(self.image.id)
        # TODO #1015
        #elif self.dataset is not None:
        #    self.hierarchy = self.conn.findContainerHierarchies(self.dataset.id)
        #elif self.project is not None:
        #    self.hierarchy = self.conn.findContainerHierarchies(self.project.id)
    
    def listMyRoots(self):
        pr_list = self.sortByAttr(list(self.conn.listProjectsMine()), 'name')
        ds_list = self.sortByAttr(list(self.conn.listDatasetsOutoffProjectMine()), 'name')
        
        pr_list_with_counters = list()
        ds_list_with_counters = list()
        
        pr_ids = [pr.id for pr in pr_list]
        if len(pr_ids) > 0:
            pr_child_counter = self.conn.getCollectionCount("Project", "datasetLinks", pr_ids)
            pr_annotation_counter = self.conn.getCollectionCount("Project", "annotationLinks", pr_ids)
            
            for pr in pr_list:
                pr.child_counter = pr_child_counter.get(pr.id)
                pr.annotation_counter = pr_annotation_counter.get(pr.id)
                pr_list_with_counters.append(pr)
        
        ds_ids = [ds.id for ds in ds_list]
        if len(ds_ids) > 0:
            ds_child_counter = self.conn.getCollectionCount("Dataset", "imageLinks", ds_ids)
            ds_annotation_counter = self.conn.getCollectionCount("Dataset", "annotationLinks", ds_ids)
            
            for ds in ds_list:
                ds.child_counter = ds_child_counter.get(ds.id)
                ds.annotation_counter = ds_annotation_counter.get(ds.id)
                ds_list_with_counters.append(ds)
        
        self.containers={'projects': pr_list_with_counters, 'datasets': ds_list_with_counters}
        self.c_size = len(pr_list_with_counters)+len(ds_list_with_counters)

    def listMyDatasetsInProject(self, project_id, page):
        ds_list = self.sortByAttr(list(self.conn.listDatasetsInProjectMine(project_id, page)), 'name')
        ds_list_with_counters = list()
        
        ds_ids = [ds.id for ds in ds_list]
        if len(ds_ids) > 0:
            ds_child_counter = self.conn.getCollectionCount("Dataset", "imageLinks", ds_ids)
            ds_annotation_counter = self.conn.getCollectionCount("Dataset", "annotationLinks", ds_ids)
        
            for ds in ds_list:
                ds.child_counter = ds_child_counter.get(ds.id)
                ds.annotation_counter = ds_annotation_counter.get(ds.id)
                ds_list_with_counters.append(ds)
        
        self.containers = {'datasets': ds_list_with_counters}
        self.c_size = self.conn.getCollectionCount("Project", "datasetLinks", [long(project_id)])[long(project_id)]
        
        self.paging = self.doPaging(page, len(ds_list_with_counters), self.c_size)
        

    def listMyImagesInDataset(self, dataset_id, page):
        im_list = self.sortByAttr(list(self.conn.listImagesInDatasetMine(dataset_id, page)), 'name')
        im_list_with_counters = list()
        
        im_ids = [im.id for im in im_list]
        if len(im_ids) > 0:
            im_annotation_counter = self.conn.getCollectionCount("Image", "annotationLinks", im_ids)
            
            for im in im_list:
                im.annotation_counter = im_annotation_counter.get(im.id)
                im_list_with_counters.append(im)
        
        self.containers = {'images': im_list_with_counters}
        self.c_size = self.conn.getCollectionCount("Dataset", "imageLinks", [long(dataset_id)])[long(dataset_id)]
        
        self.paging = self.doPaging(page, len(im_list_with_counters), self.c_size)

    def loadMyContainerHierarchy(self):
        obj_list = list(self.conn.loadMyContainerHierarchy())
        
        pr_list = list()
        ds_list = list()
        for o in obj_list:
            if isinstance(o._obj, ProjectI):
                pr_list.append(o)
            if isinstance(o._obj, DatasetI):
                ds_list.append(o)
        
        pr_list = self.sortByAttr(pr_list, 'name')
        ds_list = self.sortByAttr(ds_list, 'name')
        
        pr_list_with_counters = list()
        ds_list_with_counters = list()
        
        pr_ids = [pr.id for pr in pr_list]
        if len(pr_ids) > 0:
            pr_annotation_counter = self.conn.getCollectionCount("Project", "annotationLinks", pr_ids)
            
            for pr in pr_list:
                pr.child_counter = len(pr.copyDatasetLinks())
                pr.annotation_counter = pr_annotation_counter.get(pr.id)
                pr_list_with_counters.append(pr)
                
        ds_ids = [ds.id for ds in ds_list]
        if len(ds_ids) > 0:
            ds_child_counter = self.conn.getCollectionCount("Dataset", "imageLinks", ds_ids)
            ds_annotation_counter = self.conn.getCollectionCount("Dataset", "annotationLinks", ds_ids)
            
            for ds in ds_list:
                ds.child_counter = ds_child_counter.get(ds.id)
                ds.annotation_counter = ds_annotation_counter.get(ds.id)
                ds_list_with_counters.append(ds)
        
        self.containers={'projects': pr_list_with_counters, 'datasets': ds_list_with_counters}
        self.c_size = len(pr_list_with_counters)+len(ds_list_with_counters)

    def loadMyImages(self, dataset_id):
        im_list = self.sortByAttr(list(self.conn.listImagesInDatasetMine(long(dataset_id))), 'name')
        im_list_with_counters = list()
        
        im_ids = [im.id for im in im_list]
        if len(im_ids) > 0:
            im_annotation_counter = self.conn.getCollectionCount("Image", "annotationLinks", im_ids)
            
            for im in im_list:
                im.annotation_counter = im_annotation_counter.get(im.id)
                im_list_with_counters.append(im)
        
        self.subcontainers = im_list_with_counters

    def loadMyOrphanedImages(self):
        im_list = self.sortByAttr(list(self.conn.listImagesOutoffDatasetMine()), 'name')
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

    # COLLABORATION - User
    def listRootsAsUser(self, exp_id):
        self.experimenter = self.conn.getExperimenter(exp_id)
        self.containers = dict()
        pr_list = self.sortByAttr(list(self.conn.listProjectsAsUser(exp_id)), 'name')
        ds_list = self.sortByAttr(list(self.conn.listDatasetsOutoffProjectAsUser(exp_id)), 'name')
        
        pr_list_with_counters = list()
        ds_list_with_counters = list()
        
        pr_ids = [pr.id for pr in pr_list]
        if len(pr_ids) > 0:
            pr_child_counter = self.conn.getCollectionCount("Project", "datasetLinks", pr_ids)
            pr_annotation_counter = self.conn.getCollectionCount("Project", "annotationLinks", pr_ids)
            
            for pr in pr_list:
                pr.child_counter = pr_child_counter.get(pr.id)
                pr.annotation_counter = pr_annotation_counter.get(pr.id)
                pr_list_with_counters.append(pr)
        
        ds_ids = [ds.id for ds in ds_list]
        if len(ds_ids) > 0:
            ds_child_counter = self.conn.getCollectionCount("Dataset", "imageLinks", ds_ids)
            ds_annotation_counter = self.conn.getCollectionCount("Dataset", "annotationLinks", ds_ids)
            
            for ds in ds_list:
                ds.child_counter = ds_child_counter.get(ds.id)
                ds.annotation_counter = ds_annotation_counter.get(ds.id)
                ds_list_with_counters.append(ds)
        
        self.containers={'projects': pr_list_with_counters, 'datasets': ds_list_with_counters}
        self.c_size = len(pr_list_with_counters)+len(ds_list_with_counters)

    def listDatasetsInProjectAsUser(self, project_id, exp_id, page):
        self.experimenter = self.conn.getExperimenter(exp_id)
        ds_list = self.sortByAttr(list(self.conn.listDatasetsInProjectAsUser(project_id, exp_id, page)), 'name')
        ds_list_with_counters = list()
        
        ds_ids = [ds.id for ds in ds_list]
        if len(ds_ids) > 0:
            ds_child_counter = self.conn.getCollectionCount("Dataset", "imageLinks", ds_ids)
            ds_annotation_counter = self.conn.getCollectionCount("Dataset", "annotationLinks", ds_ids)
            
            for ds in ds_list:
                ds.child_counter = ds_child_counter.get(ds.id)
                ds.annotation_counter = ds_annotation_counter.get(ds.id)
                ds_list_with_counters.append(ds)
        
        self.containers = {'datasets': ds_list_with_counters}
        self.c_size = self.conn.getCollectionCount("Project", "datasetLinks", [long(project_id)])[long(project_id)]
        
        self.paging = self.doPaging(page, len(ds_list_with_counters), self.c_size)

    def listImagesInDatasetAsUser(self, dataset_id, exp_id, page):
        self.experimenter = self.conn.getExperimenter(exp_id)
        im_list = self.sortByAttr(list(self.conn.listImagesInDatasetAsUser(dataset_id, exp_id, page)), 'name')
        im_list_with_counters = list()
        
        im_ids = [im.id for im in im_list]
        
        if len(im_ids) > 0:
            im_annotation_counter = self.conn.getCollectionCount("Image", "annotationLinks", im_ids)
            
            for im in im_list:
                im.annotation_counter = im_annotation_counter.get(im.id)
                im_list_with_counters.append(im)
            
        self.containers = {'images': im_list_with_counters}
        self.c_size = self.conn.getCollectionCount("Dataset", "imageLinks", [long(dataset_id)])[long(dataset_id)]
        
        self.paging = self.doPaging(page, len(im_list_with_counters), self.c_size)

    def loadUserContainerHierarchy(self, exp_id):
        self.experimenter = self.conn.getExperimenter(exp_id)
        obj_list = list(self.conn.loadUserContainerHierarchy(exp_id))
        
        pr_list = list()
        ds_list = list()
        for o in obj_list:
            if isinstance(o._obj, ProjectI):
                pr_list.append(o)
            if isinstance(o._obj, DatasetI):
                ds_list.append(o)
        
        self.sortByAttr(pr_list, 'name')
        self.sortByAttr(ds_list, 'name')
        
        pr_list_with_counters = list()
        ds_list_with_counters = list()
        
        pr_ids = [pr.id for pr in pr_list]
        if len(pr_ids) > 0:
            pr_child_counter = self.conn.getCollectionCount("Project", "datasetLinks", pr_ids)
            pr_annotation_counter = self.conn.getCollectionCount("Project", "annotationLinks", pr_ids)
            
            for pr in pr_list:
                pr.child_counter = pr_child_counter.get(pr.id)
                pr.annotation_counter = pr_annotation_counter.get(pr.id)
                pr_list_with_counters.append(pr)
        
        ds_ids = [ds.id for ds in ds_list]
        if len(ds_ids) > 0:
            ds_child_counter = self.conn.getCollectionCount("Dataset", "imageLinks", ds_ids)
            ds_annotation_counter = self.conn.getCollectionCount("Dataset", "annotationLinks", ds_ids)
            
            for ds in ds_list:
                ds.child_counter = ds_child_counter.get(ds.id)
                ds.annotation_counter = ds_annotation_counter.get(ds.id)
                ds_list_with_counters.append(ds)
        
        self.containers={'projects': pr_list_with_counters, 'datasets': ds_list_with_counters}
        self.c_size = len(pr_list_with_counters)+len(ds_list_with_counters)
        
    def loadUserImages(self, dataset_id, exp_id):
        im_list = self.sortByAttr(list(self.conn.listImagesInDatasetAsUser(dataset_id, exp_id)), 'name')
        
        im_list_with_counters = list()
        
        im_ids = [im.id for im in im_list]
        if len(im_ids) > 0:
            im_annotation_counter = self.conn.getCollectionCount("Image", "annotationLinks", im_ids)
            
            for im in im_list:
                im.annotation_counter = im_annotation_counter.get(im.id)
                im_list_with_counters.append(im)
            
        self.subcontainers = im_list_with_counters
    
    def loadUserOrphanedImages(self, exp_id):
        im_list = self.sortByAttr(list(self.conn.listImagesOutoffDatasetAsUser(exp_id)), 'name')
        im_list_with_counters = list()
        
        im_ids = [im.id for im in im_list]
        if len(im_ids) > 0:
            im_annotation_counter = self.conn.getCollectionCount("Image", "annotationLinks", im_ids)
            
            for im in im_list:
                im.annotation_counter = im_annotation_counter.get(im.id)
                im_list_with_counters.append(im)
        
        self.containers = {'images': im_list_with_counters}
        self.c_size = len(im_list_with_counters)
    
    # COLLABORATION - group
    def listRootsInGroup(self, group_id):
        self.myGroup = self.conn.getGroup(group_id)
        self.containersMyGroups = dict()
        pr_list = self.sortByAttr(list(self.conn.listProjectsInGroup(group_id)), 'name')
        ds_list = self.sortByAttr(list(self.conn.listDatasetsOutoffProjectInGroup(group_id)), 'name')
        
        pr_list_with_counters = list()
        ds_list_with_counters = list()
        
        pr_ids = [pr.id for pr in pr_list]
        if len(pr_ids) > 0:
            pr_child_counter = self.conn.getCollectionCount("Project", "datasetLinks", pr_ids)
            pr_annotation_counter = self.conn.getCollectionCount("Project", "annotationLinks", pr_ids)
        
            for pr in pr_list:
                pr.child_counter = pr_child_counter.get(pr.id)
                pr.annotation_counter = pr_annotation_counter.get(pr.id)
                pr_list_with_counters.append(pr)
        
        ds_ids = [ds.id for ds in ds_list]
        if len(ds_ids) > 0:
            ds_child_counter = self.conn.getCollectionCount("Dataset", "imageLinks", ds_ids)
            ds_annotation_counter = self.conn.getCollectionCount("Dataset", "annotationLinks", ds_ids)
            
            for ds in ds_list:
                ds.child_counter = ds_child_counter.get(ds.id)
                ds.annotation_counter = ds_annotation_counter.get(ds.id)
                ds_list_with_counters.append(ds)
        
        user_set = set()
        for pr in pr_list_with_counters:
            user_set.add(pr.details.owner.id.val)
        for ds in ds_list_with_counters:
            user_set.add(ds.details.owner.id.val)
        
        if len(user_set) > 0:
            experimenters = self.conn.getExperimenters(user_set)
            for e in experimenters:
                self.containersMyGroups[e.id]={'name': e.getFullName(), 'projects': list(), 'datasets': list()}
        
            for pr in pr_list_with_counters:
                self.containersMyGroups[pr.details.owner.id.val]['projects'].append(pr)
            for ds in ds_list_with_counters:
                self.containersMyGroups[ds.details.owner.id.val]['datasets'].append(ds)
            
            self.c_mg_size = len(pr_list_with_counters)+len(ds_list_with_counters)

    def listDatasetsInProjectInGroup(self, project_id, group_id, page):
        self.myGroup = self.conn.getGroup(group_id)
        self.containersMyGroups = dict()                
        ds_list = self.sortByAttr(list(self.conn.listDatasetsInProjectInGroup(project_id, group_id, page)), 'name')
        
        ds_list_with_counters = list()
        
        ds_ids = [ds.id for ds in ds_list]
        if len(ds_ids) > 0:
            ds_child_counter = self.conn.getCollectionCount("Dataset", "imageLinks", ds_ids)
            ds_annotation_counter = self.conn.getCollectionCount("Dataset", "annotationLinks", ds_ids)
            
            for ds in ds_list:
                ds.child_counter = ds_child_counter.get(ds.id)
                ds.annotation_counter = ds_annotation_counter.get(ds.id)
                ds_list_with_counters.append(ds)
        
        user_set = set()
        for ds in ds_list_with_counters:
            user_set.add(ds.details.owner.id.val)
        
        if len(user_set) > 0:
            experimenters = self.conn.getExperimenters(user_set)
            for e in experimenters:
                self.containersMyGroups[e.id]={'name': e.getFullName(), 'datasets': list()}

            for ds in ds_list_with_counters:
                self.containersMyGroups[ds.details.owner.id.val]['datasets'].append(ds)


        self.c_mg_size = self.conn.getCollectionCount("Project", "datasetLinks", [long(project_id)])[long(project_id)]
        
        self.paging = self.doPaging(page, len(ds_list_with_counters), self.c_mg_size)

    def listImagesInDatasetInGroup(self, dataset_id, group_id, page):
        self.myGroup = self.conn.getGroup(group_id)
        self.containersMyGroups = dict()
        
        im_list = self.sortByAttr(list(self.conn.listImagesInDatasetInGroup(dataset_id, group_id, page)), 'name')
        im_list_with_counters = list()
        im_ids = [im.id for im in im_list]
        if len(im_ids) > 0:
            im_annotation_counter = self.conn.getCollectionCount("Image", "annotationLinks", im_ids)
            
            for im in im_list:
                im.annotation_counter = im_annotation_counter.get(im.id)
                im_list_with_counters.append(im)
        
        user_set = set()
        for im in im_list_with_counters:
            user_set.add(im.details.owner.id.val)
        
        if len(user_set) > 0:
            experimenters = self.conn.getExperimenters(user_set)
            for e in experimenters:
                self.containersMyGroups[e.id]={'name': e.getFullName(), 'images': list()}

            for im in im_list_with_counters:
                self.containersMyGroups[im.details.owner.id.val]['images'].append(im)

            self.c_mg_size = len(im_list_with_counters)
        
        self.c_mg_size = self.conn.getCollectionCount("Dataset", "imageLinks", [long(dataset_id)])[long(dataset_id)]
        self.paging = self.doPaging(page, len(im_list_with_counters), self.c_mg_size)
        
    def loadGroupContainerHierarchy(self, group_id):
        self.myGroup = self.conn.getGroup(group_id)
        obj_list = self.sortByAttr(list(self.conn.loadGroupContainerHierarchy(group_id)), 'name')
        
        pr_list = list()
        ds_list = list()
        for o in obj_list:
            if isinstance(o._obj, ProjectI):
                pr_list.append(o)
            if isinstance(o._obj, DatasetI):
                ds_list.append(o)
        
        pr_list_with_counters = list()
        ds_list_with_counters = list()
        
        pr_ids = [pr.id for pr in pr_list]
        if len(pr_ids) > 0:
            pr_child_counter = self.conn.getCollectionCount("Project", "datasetLinks", pr_ids)
            pr_annotation_counter = self.conn.getCollectionCount("Project", "annotationLinks", pr_ids)
            
            for pr in pr_list:
                pr.child_counter = pr_child_counter.get(pr.id)
                pr.annotation_counter = pr_annotation_counter.get(pr.id)
                pr_list_with_counters.append(pr)
            
        ds_ids = [ds.id for ds in ds_list]
        if len(ds_ids) > 0:
            ds_child_counter = self.conn.getCollectionCount("Dataset", "imageLinks", ds_ids)
            ds_annotation_counter = self.conn.getCollectionCount("Dataset", "annotationLinks", ds_ids)
            
            for ds in ds_list:
                ds.child_counter = ds_child_counter.get(ds.id)
                ds.annotation_counter = ds_annotation_counter.get(ds.id)
                ds_list_with_counters.append(ds)
        
        self.containersMyGroups={'projects': pr_list_with_counters, 'datasets': ds_list_with_counters}
        self.c_mg_size = len(pr_list_with_counters)+len(ds_list_with_counters)

    def loadGroupImages(self, dataset_id, group_id):
        im_list = self.sortByAttr(list(self.conn.listImagesInDatasetInGroup(dataset_id, group_id)), 'name')
        
        im_list_with_counters = list()
        im_ids = [im.id for im in im_list]
        if len(im_ids) > 0:
            im_annotation_counter = self.conn.getCollectionCount("Image", "annotationLinks", im_ids)
            
            for im in im_list:
                im.annotation_counter = im_annotation_counter.get(im.id)
                im_list_with_counters.append(im)
        
        self.subcontainers = im_list_with_counters
    
    def loadGroupOrphanedImages(self, group_id):
        im_list = self.sortByAttr(list(self.conn.listImagesOutoffDatasetInGroup(group_id)), 'name')
        im_list_with_counters = list()
        im_ids = [im.id for im in im_list]
        if len(im_ids) > 0:
            im_annotation_counter = self.conn.getCollectionCount("Image", "annotationLinks", im_ids)
            for im in im_list:
                im.annotation_counter = im_annotation_counter.get(im.id)
                im_list_with_counters.append(im)
        
        self.containers = {'images': im_list_with_counters}
        self.c_size = len(im_list_with_counters)
    
    # Annotation list
    def annotationList(self):
        self.text_annotations = list()
        self.long_annotations = {'rate': 0.00 , 'votes': 0}
        self.url_annotations = list()
        self.file_annotations = list()
        self.tag_annotations = list()
        
        aList = None
        if self.image is not None:
            aList = self.image.listAnnotations()
        elif self.dataset is not None:
            aList = self.dataset.listAnnotations()
        elif self.project is not None:
            aList = self.project.listAnnotations()
        
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

        self.text_annotations = self.sortByAttr(self.text_annotations, "details.creationEvent.time")
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
    
    def listComments(self):
        if self.image is not None:
            return list(self.conn.listComments("image", self.image.id))
        elif self.dataset is not None:
            return list(self.conn.listComments("dataset", self.dataset.id))
        elif self.project is not None:
            return list(self.conn.listComments("project", self.project.id))
    
    def listUrls(self):
        if self.image is not None:
            return list(self.conn.listUrls("image", self.image.id))
        elif self.dataset is not None:
            return list(self.conn.listUrls("dataset", self.dataset.id))
        elif self.project is not None:
            return list(self.conn.listUrls("project", self.project.id))
    
    def listFiles(self):
        if self.image is not None:
            return list(self.conn.listFiles("image", self.image.id))
        elif self.dataset is not None:
            return list(self.conn.listFiles("dataset", self.dataset.id))
        elif self.project is not None:
            return list(self.conn.listFiles("project", self.project.id))
    
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
    
    # Comment annotation
    def createProjectCommentAnnotation(self, content):
        ann = CommentAnnotationI()
        ann.textValue = rstring(str(content))
        l_ann = ProjectAnnotationLinkI()
        l_ann.setParent(self.project._obj)
        l_ann.setChild(ann)
        self.conn.saveObject(l_ann)
    
    def createDatasetCommentAnnotation(self, content):
        ann = CommentAnnotationI()
        ann.textValue = rstring(str(content))
        l_ann = DatasetAnnotationLinkI()
        l_ann.setParent(self.dataset._obj)
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
    
    def createDatasetUriAnnotation(self, content):
        ann = UriAnnotationI()
        ann.textValue = rstring(str(content))
        l_ann = DatasetAnnotationLinkI()
        l_ann.setParent(self.dataset._obj)
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
    
    def createProjectTagAnnotation(self, tag, desc):
        ann = TagAnnotationI()
        ann.textValue = rstring(str(tag))
        ann.setDescription(rstring(str(desc)))
        t_ann = ProjectAnnotationLinkI()
        t_ann.setParent(self.project._obj)
        t_ann.setChild(ann)
        self.conn.saveObject(t_ann)
    
    # File annotation
    def createProjectFileAnnotation(self, newFile):
        if newFile.content_type.startswith("image"):
            f = newFile.content_type.split("/") 
            try:
                format = self.conn.getFileFormt(f[1].upper())
            except:
                format = self.conn.getFileFormt("application/octet-stream")
        else:
            format = self.conn.getFileFormt(newFile.content_type)
        
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
    
    def createDatasetFileAnnotation(self, newFile):
        if newFile.content_type.startswith("image"):
            f = newFile.content_type.split("/") 
            try:
                format = self.conn.getFileFormt(f[1].upper())
            except:
                format = self.conn.getFileFormt("application/octet-stream")
        else:
            format = self.conn.getFileFormt(newFile.content_type)
        
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
    
    def createImageFileAnnotation(self, newFile):
        if newFile.content_type.startswith("image"):
            f = newFile.content_type.split("/") 
            format = None
            try:
                format = self.conn.getFileFormt(f[1].upper())
            except:
                format = self.conn.getFileFormt("application/octet-stream")
        else:
            format = self.conn.getFileFormt(newFile.content_type)
        
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
    
    ################################################################
    # Update
    
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
    
    def updateProject(self, name, description):
        container = self.project._obj
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
        else:
            return 'No data was choosen.'
        return 
    
    def remove(self, parent, source):
        if source[0] == 'ds':
            if parent[0] == 'pr':
                pdl = self.conn.getProjectDatasetLink(parent[1], source[1])
                if pdl is not None:
                    self.conn.deleteObject(pdl._obj)
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
            elif parent[0] == 'img':
                ial = self.conn.getImageAnnotationLink(parent[1], source[1])
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

    ###########################################################
    # Paging
    
    def doPaging(self, page, page_size, total_size):
        total = list()
        t = total_size/24
        if total_size > 240:
            if page > 10 :
                total.append(-1)
            for i in range((1, page-9)[ page-9 >= 1 ], (t+1, page+10)[ page+9 < t ]):
                total.append(i)
            if page < t-9:
                total.append(-1)

        elif total_size > 24 and total_size <= 240:
            for i in range(1, t+2):
                total.append(i)
        else:
            total.append(1)
        next = None
        if page_size == 24 and page*24 < total_size:
            next = page + 1
        prev = None
        if page > 1:
            prev = page - 1
        
        return {'page': page, 'total':total, 'next':next, "prev":prev}
