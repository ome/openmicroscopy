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
from omero_model_TextAnnotationI import TextAnnotationI
from omero_model_UrlAnnotationI import UrlAnnotationI
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
    
    def __init__(self, conn, o1_type=None, o1_id=None, o2_type=None, o2_id=None, o3_type=None, o3_id=None, **kw):
        BaseController.__init__(self, conn)
        if o1_type == "project":
            self.project = self.conn.getProject(o1_id)
            if o2_type == "dataset":
                self.dataset = self.conn.getDataset(o2_id)
                if o3_type == "image":
                    self.image = self.conn.getImageWithMetadata(o3_id)
        elif o1_type == "dataset":
            self.dataset = self.conn.getDataset(o1_id)
            if o2_type == "image":
                self.image = self.conn.getImageWithMetadata(o2_id)
        elif o1_type == "image":
            self.image = self.conn.getImageWithMetadata(o1_id)
            self.image._loadPixels()
    
    def buildBreadcrumb(self, menu):
        if menu == 'new' or menu == 'addnew':
            self.eContext['breadcrumb'] = ['New container']
        elif menu == 'edit':
            if self.project is not None:
                self.eContext['breadcrumb'] = ['Edit project: %s (%i)' % (self.project.name, self.project.id)]
            elif self.dataset is not None:
                self.eContext['breadcrumb'] = ['Edit dataset: %s (%i)' % (self.dataset.name, self.dataset.id)]
            elif self.image is not None:
                self.eContext['breadcrumb'] = ['Edit image: %s (%i)' % (self.image.name, self.image.id)]
        else:
            if self.project is not None:
                self.eContext['breadcrumb'] = ['<a href="/%s/%s/">%s</a>' % (settings.WEBCLIENT_ROOT_BASE, menu, menu.title()),  
                            '<a href="/%s/%s/project/%i/">%s: %s</a>' % (settings.WEBCLIENT_ROOT_BASE, menu, self.project.id, 'Project', self.project.name)]
                if self.dataset is not None:
                    self.eContext['breadcrumb'].append('<a href="/%s/%s/project/%i/dataset/%i/">%s: %s</a>' % (settings.WEBCLIENT_ROOT_BASE, menu, self.project.id, self.dataset.id, 'Dataset', self.dataset.name))
                    if self.image is not None:
                        self.eContext['breadcrumb'].append('<a href="/%s/%s/project/%i/dataset/%i/image/%i/">%s: %s</a>' % (settings.WEBCLIENT_ROOT_BASE, menu, self.project.id, self.dataset.id, self.image.id, 'Image', self.image.name))
            elif self.dataset is not None:
                self.eContext['breadcrumb'] = ['<a href="/%s/%s/">%s</a>' % (settings.WEBCLIENT_ROOT_BASE, menu, menu.title()),  
                            "Dataset %s (%s)" % (self.dataset.name, self.dataset.id)]
                if self.image is not None:
                    self.eContext['breadcrumb'].append('<a href="/%s/%s/dataset/%i/image/%i/">%s: %s</a>' % (settings.WEBCLIENT_ROOT_BASE, menu, self.dataset.id, self.image.id, 'Image', self.image.name))
            elif self.image is not None:
                self.eContext['breadcrumb'] = ['<a href="/%s/%s/">%s</a>' % (settings.WEBCLIENT_ROOT_BASE, menu, menu.title()),  
                            "Image %s (%s)" % (self.image.name, self.image.id)]
            else:
                self.eContext['breadcrumb'] = [menu.title()] 
    
    def loadHierarchy(self):
        if self.image is not None:
            self.hierarchy = self.conn.findContainerHierarchies(self.image.id)
        elif self.dataset is not None:
            self.hierarchy = self.conn.findContainerHierarchies(self.dataset.id)
        elif self.project is not None:
            self.hierarchy = self.conn.findContainerHierarchies(self.project.id)
    
    def listMyRoots(self):
        pr_list = list(self.conn.listProjectsMine())
        ds_list = list(self.conn.listDatasetsOutoffProjectMine())
        im_list = list(self.conn.listImagesOutoffDatasetMine())
        self.containers={'projects': pr_list, 'datasets': ds_list, 'images': im_list}
        self.c_size = len(pr_list)+len(ds_list)+len(im_list)

    def listMyDatasetsInProject(self, project_id):
        ds_list = list(self.conn.listDatasetsInProjectMine(project_id))
        self.containers = {'datasets': ds_list}
        self.c_size = len(ds_list)

    def listMyImagesInDataset(self, dataset_id):
        im_list = list(self.conn.listImagesInDatasetMine(dataset_id))
        self.containers = {'images': im_list}
        self.c_size = len(im_list)

    def loadMyContainerHierarchy(self):
        pr_list = list(self.conn.loadMyContainerHierarchy())
        ds_list = list(self.conn.listDatasetsOutoffProjectMine())
        im_list = list(self.conn.listImagesOutoffDatasetMine())
        self.containers={'projects': pr_list, 'datasets': ds_list, 'images': im_list}
        self.c_size = len(pr_list)+len(ds_list)+len(im_list)

    def loadMyImages(self, dataset_id):
        self.subcontainers = list(self.conn.listImagesInDatasetMine(long(dataset_id)))


    # COLLABORATION - User
    def listRootsInUser(self, exp_id):
        self.experimenter = self.conn.getExperimenter(exp_id)
        self.containers = dict()
        pr_exp = list(self.conn.listProjectsInUser(exp_id))
        ds_exp = list(self.conn.listDatasetsOutoffProjectInUser(exp_id))
        im_exp = list(self.conn.listImagesOutoffDatasetInUser(exp_id))
        self.containers={'projects': pr_exp, 'datasets': ds_exp, 'images': im_exp}
        self.c_size = len(pr_exp)+len(ds_exp)+len(im_exp)

    def listDatasetsInProjectInUser(self, project_id, exp_id):
        self.experimenter = self.conn.getExperimenter(exp_id)
        ds_list = list(self.conn.listDatasetsInProjectInUser(project_id, exp_id))
        self.containers = {'datasets': ds_list}
        self.c_size = len(ds_list)

    def listImagesInDatasetInUser(self, dataset_id, exp_id):
        self.experimenter = self.conn.getExperimenter(exp_id)
        im_list = list(self.conn.listImagesInDatasetInUser(dataset_id, exp_id))
        self.containers = {'images': im_list}
        self.c_size = len(im_list)

    def loadUserContainerHierarchy(self, exp_id):
        self.experimenter = self.conn.getExperimenter(exp_id)
        pr_list = list(self.conn.loadUserContainerHierarchy(exp_id))
        ds_list = list(self.conn.listDatasetsOutoffProjectInUser(exp_id))
        im_list = list(self.conn.listImagesOutoffDatasetInUser(exp_id))
        self.containers={'projects': pr_list, 'datasets': ds_list, 'images': im_list}
        self.c_size = len(pr_list)+len(ds_list)+len(im_list)
        
    def loadUserImages(self, dataset_id, exp_id):
        self.subcontainers = list(self.conn.listImagesInDatasetInUser(dataset_id, exp_id))

    # COLLABORATION
    def listRootsInGroup(self, group_id):
        self.myGroup = self.conn.getGroup(group_id)
        self.containersMyGroups = dict()
        pr_mygroups = list(self.conn.listProjectsInGroup(group_id))
        ds_mygroups = list(self.conn.listDatasetsOutoffProjectInGroup(group_id))
        im_mygroups = list(self.conn.listImagesOutoffDatasetInGroup(group_id))
        user_set = set()
        for pr in pr_mygroups:
            user_set.add(pr.details.owner.id.val)
        for ds in ds_mygroups:
            user_set.add(ds.details.owner.id.val)
        for im in im_mygroups:
            user_set.add(im.details.owner.id.val)
        
        if len(user_set) > 0:
            experimenters = self.conn.getExperimenters(user_set)
            for e in experimenters:
                self.containersMyGroups[e.id]={'name': e.getFullName(), 'projects': list(), 'datasets': list(), 'images': list()}
        
            for pr in pr_mygroups:
                self.containersMyGroups[pr.details.owner.id.val]['projects'].append(pr)
            for ds in ds_mygroups:
                self.containersMyGroups[ds.details.owner.id.val]['datasets'].append(ds)
            for im in im_mygroups:
                self.containersMyGroups[im.details.owner.id.val]['images'].append(im)
            
            self.c_mg_size = len(pr_mygroups)+len(ds_mygroups)+len(im_mygroups)

    def listDatasetsInProjectInGroup(self, project_id, group_id):
        self.myGroup = self.conn.getGroup(group_id)
        self.containersMyGroups = dict()                
        ds_mygroups = list(self.conn.listDatasetsInProjectInGroup(project_id, group_id))
        user_set = set()
        for ds in ds_mygroups:
            user_set.add(ds.details.owner.id.val)
        
        if len(user_set) > 0:
            experimenters = self.conn.getExperimenters(user_set)
            for e in experimenters:
                self.containersMyGroups[e.id]={'name': e.getFullName(), 'datasets': list()}

            for ds in ds_mygroups:
                self.containersMyGroups[ds.details.owner.id.val]['datasets'].append(ds)

            self.c_mg_size = len(ds_mygroups)

    def listImagesInDatasetInGroup(self, dataset_id, group_id):
        self.myGroup = self.conn.getGroup(group_id)
        self.containersMyGroups = dict()                
        im_mygroups = list(self.conn.listImagesInDatasetInGroup(dataset_id, group_id))
        user_set = set()
        for im in im_mygroups:
            user_set.add(im.details.owner.id.val)
        
        if len(user_set) > 0:
            experimenters = self.conn.getExperimenters(user_set)
            for e in experimenters:
                self.containersMyGroups[e.id]={'name': e.getFullName(), 'images': list()}

            for im in im_mygroups:
                self.containersMyGroups[im.details.owner.id.val]['images'].append(im)

            self.c_mg_size = len(im_mygroups)

    def loadGroupContainerHierarchy(self, group_id):
        self.myGroup = self.conn.getGroup(group_id)
        pr_list = list(self.conn.loadGroupContainerHierarchy(group_id))
        ds_list = list(self.conn.listDatasetsOutoffProjectInGroup(group_id))
        im_list = list(self.conn.listImagesOutoffDatasetInGroup(group_id))
        self.containersMyGroups={'projects': pr_list, 'datasets': ds_list, 'images': im_list}
        self.c_mg_size = len(pr_list)+len(ds_list)+len(im_list)

    def loadGroupImages(self, dataset_id, group_id):
        self.subcontainers = list(self.conn.listImagesInDatasetInGroup(dataset_id, group_id))
    
    ############################################################
    # Update and save
    
    def updateImage(self, name, description, permissions):
        img = self.image._obj
        img.name = rstring(str(name))
        if description != "" :
            img.description = rstring(str(description))
        else:
            img.description = None
        self.objectPermissions(img, permissions)
        self.conn.updateObject(img)
    
    def imageAnnotationList(self):
        self.text_annotations = list()
        self.long_annotations = {'rate': 0.00 , 'votes': 0}
        self.url_annotations = list()
        for ann in self.image.listAnnotations():
            if ann._obj.__class__.__name__ == 'TextAnnotationI':
                self.text_annotations.append(ann)
            elif ann._obj.__class__.__name__ == 'LongAnnotationI':
                self.long_annotations['votes'] += 1
                self.long_annotations['rate'] += int(ann.longValue)
            elif ann._obj.__class__.__name__ == 'UrlAnnotationI':
                self.url_annotations.append(ann)

        self.txannSize = len(self.text_annotations)
        self.urlannSize = len(self.url_annotations)

        if self.long_annotations['votes'] > 0:
            self.long_annotations['rate'] /= self.long_annotations['votes']
    
    def datasetAnnotationList(self):
        self.text_annotations = list()
        self.long_annotations = {'rate': 0.00 , 'votes': 0}
        self.url_annotations = list()
        self.file_annotations = list()
        for ann in self.dataset.listAnnotations():
            if ann._obj.__class__.__name__ == 'TextAnnotationI':
                self.text_annotations.append(ann)
            elif ann._obj.__class__.__name__ == 'LongAnnotationI':
                self.long_annotations['votes'] += 1
                self.long_annotations['rate'] += int(ann.longValue)
            elif ann._obj.__class__.__name__ == 'UrlAnnotationI':
                self.url_annotations.append(ann)
            elif ann._obj.__class__.__name__ == 'FileAnnotationI':
                self.file_annotations.append(ann)

        self.txannSize = len(self.text_annotations)
        self.urlannSize = len(self.url_annotations)
        self.fileannSize = len(self.file_annotations)

        if self.long_annotations['votes'] > 0:
            self.long_annotations['rate'] /= self.long_annotations['votes']

    def projectAnnotationList(self):
        self.text_annotations = list()
        self.long_annotations = {'rate': 0.00 , 'votes': 0}
        self.url_annotations = list()
        for ann in self.project.listAnnotations():
            if ann._obj.__class__.__name__ == 'TextAnnotationI':
                self.text_annotations.append(ann)
            elif ann._obj.__class__.__name__ == 'LongAnnotationI':
                self.long_annotations['votes'] += 1
                self.long_annotations['rate'] += int(ann.longValue)
            elif ann._obj.__class__.__name__ == 'UrlAnnotationI':
                self.url_annotations.append(ann)

        self.txannSize = len(self.text_annotations)
        self.urlannSize = len(self.url_annotations)

        if self.long_annotations['votes'] > 0:
            self.long_annotations['rate'] /= self.long_annotations['votes']
    
    
    def updateDataset(self, name, description, permissions):
        container = self.dataset._obj
        container.name = rstring(str(name))
        if description != "" :
            container.description = rstring(str(description))
        else:
            container.description = None
        self.objectPermissions(container, permissions)
        for l_ds in container.copyProjectLinks():
            self.objectPermissions(l_ds,permissions)
        self.conn.updateObject(container)

    def updateProject(self, name, description, permissions):
        container = self.project._obj
        container.name = rstring(str(name))
        if description != "" :
            container.description = rstring(str(description))
        else:
            container.description = None
        self.objectPermissions(container, permissions)
        self.conn.updateObject(container)

    def createDataset(self, name, description, permissions):
        ds = DatasetI()
        self.objectPermissions(ds, permissions)
        ds.name = rstring(str(name))
        if description != "" :
            ds.description = rstring(str(description))
        if self.project is not None:
            l_ds = ProjectDatasetLinkI()
            l_ds.setParent(self.project._obj)
            l_ds.setChild(ds)
            self.objectPermissions(l_ds,permissions)
            ds.addProjectDatasetLink(l_ds)
        res = self.conn.createObject(ds)
        return res
        
    def createProject(self, name, description, permissions):
        pr = ProjectI()
        self.objectPermissions(pr, permissions)
        pr.name = rstring(str(name))
        if description != "" :
            pr.description = rstring(str(description))
        
        res = self.conn.createObject(pr)
        return res
    
    def saveImageTextAnnotation(self, content):
        ann = TextAnnotationI()
        ann.textValue = rstring(str(content))
        l_ann = ImageAnnotationLinkI()
        l_ann.setParent(self.image._obj)
        l_ann.setChild(ann)
        self.conn.updateObject(l_ann)
    
    def saveImageUrlAnnotation(self, content):
        ann = UrlAnnotationI()
        ann.textValue = rstring(str(content))
        l_ann = ImageAnnotationLinkI()
        l_ann.setParent(self.image._obj)
        l_ann.setChild(ann)
        self.conn.updateObject(l_ann)
    
    def saveProjectTextAnnotation(self, content):
        ann = TextAnnotationI()
        ann.textValue = rstring(str(content))
        l_ann = ProjectAnnotationLinkI()
        l_ann.setParent(self.project._obj)
        l_ann.setChild(ann)
        self.conn.updateObject(l_ann)

    def saveProjectUrlAnnotation(self, content):
        ann = UrlAnnotationI()
        ann.textValue = rstring(str(content))
        l_ann = ProjectAnnotationLinkI()
        l_ann.setParent(self.project._obj)
        l_ann.setChild(ann)
        self.conn.updateObject(l_ann)

    def saveDatasetTextAnnotation(self, content):
        ann = TextAnnotationI()
        ann.textValue = rstring(str(content))
        l_ann = DatasetAnnotationLinkI()
        l_ann.setParent(self.dataset._obj)
        l_ann.setChild(ann)
        self.conn.updateObject(l_ann)

    def saveDatasetUrlAnnotation(self, content):
        ann = UrlAnnotationI()
        ann.textValue = rstring(str(content))
        l_ann = DatasetAnnotationLinkI()
        l_ann.setParent(self.dataset._obj)
        l_ann.setChild(ann)
        self.conn.updateObject(l_ann)

    def move(self, parent, source, destination):
        #print parent, source, destination
        if source[0] == "pr":
            return False
        elif source[0] == "ds":
            if destination[0] == 'ds':
                return False
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
                        self.conn.updateObject(up_pdl._obj)
                    else:
                        ds = self.conn.getDataset(source[1])
                        up_pdl = ProjectDatasetLinkI()
                        up_pdl.setChild(ds._obj)
                        up_pdl.setParent(new_pr._obj)
                        self.conn.updateObject(up_pdl)
                        
                    
            elif destination[0] == '0':
                up_pdl = None
                pdls = self.conn.getProjectDatasetLinks(source[1])
                already_there = None
                
                for pdl in pdls:
                    if pdl.parent.id.val == long(parent[1]):
                        up_pdl = pdl
                self.conn.deleteObject(up_pdl._obj)

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
                        self.conn.updateObject(up_dsl._obj)
                    else:
                        im = self.conn.getImage(source[1])
                        up_dsl = DatasetImageLinkI()
                        up_dsl.setChild(im._obj)
                        up_dsl.setParent(new_ds._obj)
                        self.conn.updateObject(up_dsl)
            elif destination[0] == 'pr':
                return False
            elif destination[0] == '0':
                if parent[0] != destination[0]:
                    up_dsl = None
                    dsls = self.conn.getDatasetImageLinks(source[1]) #gets every links for child
                
                    for dsl in dsls:
                        # gets old parent to update of delete
                        if dsl.parent.id.val == long(parent[1]):
                            up_dsl = dsl
                    self.conn.deleteObject(up_dsl._obj)
        else:
            return False
        return True

    #####################################################################
    # Permissions
    
    def objectPermissions(self, obj, permissions):
        if permissions['owner'] == 'rw':
            obj.details.permissions.setUserRead(True)
            obj.details.permissions.setUserWrite(True)
        elif permissions['owner'] == 'w':
            obj.details.permissions.setUserRead(False)
            obj.details.permissions.setUserWrite(True)
        elif permissions['owner'] == 'r':
            obj.details.permissions.setUserRead(True)
            obj.details.permissions.setUserWrite(False)
        else:
            obj.details.permissions.setUserRead(False)
            obj.details.permissions.setUserWrite(False)
        
        if permissions['group'] == 'rw':
            obj.details.permissions.setGroupRead(True)
            obj.details.permissions.setGroupWrite(True)
        elif permissions['group'] == 'w':
            obj.details.permissions.setGroupRead(False)
            obj.details.permissions.setGroupWrite(True)
        elif permissions['group'] == 'r':
            obj.details.permissions.setGroupRead(True)
            obj.details.permissions.setGroupWrite(False)
        else:
            obj.details.permissions.setGroupRead(False)
            obj.details.permissions.setGroupWrite(False)
        
        if permissions['world'] == 'rw':
            obj.details.permissions.setWorldRead(True)
            obj.details.permissions.setWorldWrite(True)
        elif permissions['world'] == 'w':
            obj.details.permissions.setWorldRead(False)
            obj.details.permissions.setWorldWrite(True)
        elif permissions['world'] == 'r':
            obj.details.permissions.setWorldRead(True)
            obj.details.permissions.setWorldWrite(False)
        else:
            obj.details.permissions.setWorldRead(False)
            obj.details.permissions.setWorldWrite(False)

