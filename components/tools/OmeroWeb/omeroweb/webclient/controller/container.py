#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# container
#
# Copyright (c) 2008-2015 University of Dundee.
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
from omero.rtypes import rstring, rlong, unwrap
from django.utils.encoding import smart_str
import logging
from omero.cmd import Delete2

from webclient.controller import BaseController

logger = logging.getLogger(__name__)


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

    obj_type = None

    text_annotations = None
    txannSize = 0
    long_annotations = None
    file_annotations = None

    orphaned = False

    def __init__(self, conn, project=None, dataset=None, image=None,
                 screen=None, plate=None, acquisition=None, well=None,
                 tag=None, tagset=None, file=None, comment=None,
                 annotation=None, index=None, orphaned=None, **kw):
        BaseController.__init__(self, conn)
        if project is not None:
            self.obj_type = "project"
            self.project = self.conn.getObject("Project", project)
            self.assertNotNone(self.project, project, "Project")
            self.assertNotNone(self.project._obj, project, "Project")
        if dataset is not None:
            self.obj_type = "dataset"
            self.dataset = self.conn.getObject("Dataset", dataset)
            self.assertNotNone(self.dataset, dataset, "Dataset")
            self.assertNotNone(self.dataset._obj, dataset, "Dataset")
        if screen is not None:
            self.obj_type = "screen"
            self.screen = self.conn.getObject("Screen", screen)
            self.assertNotNone(self.screen, screen, "Screen")
            self.assertNotNone(self.screen._obj, screen, "Screen")
        if plate is not None:
            self.obj_type = "plate"
            self.plate = self.conn.getObject("Plate", plate)
            self.assertNotNone(self.plate, plate, "Plate")
            self.assertNotNone(self.plate._obj, plate, "Plate")
        if acquisition is not None:
            self.obj_type = "acquisition"
            self.acquisition = self.conn.getObject(
                "PlateAcquisition", acquisition)
            self.assertNotNone(
                self.acquisition, acquisition, "Plate Acquisition")
            self.assertNotNone(
                self.acquisition._obj, acquisition, "Plate Acquisition")
        if image is not None:
            self.obj_type = "image"
            self.image = self.conn.getObject("Image", image)
            self.assertNotNone(self.image, image, "Image")
            self.assertNotNone(self.image._obj, image, "Image")
        if well is not None:
            self.obj_type = "well"
            self.well = self.conn.getObject("Well", well)
            self.assertNotNone(self.well, well, "Well")
            self.assertNotNone(self.well._obj, well, "Well")
            if index is not None:
                self.well.index = index
        if tag is not None:
            self.obj_type = "tag"
            self.tag = self.conn.getObject("Annotation", tag)
            self.assertNotNone(self.tag, tag, "Tag")
            self.assertNotNone(self.tag._obj, tag, "Tag")
        if tagset is not None:
            self.obj_type = "tagset"
            self.tag = self.conn.getObject("Annotation", tagset)
            # We need to check if tagset via hasattr(manager, o_type)
            self.tagset = self.tag
            self.assertNotNone(self.tag, tagset, "Tag")
            self.assertNotNone(self.tag._obj, tagset, "Tag")
        if comment is not None:
            self.obj_type = "comment"
            self.comment = self.conn.getObject("Annotation", comment)
            self.assertNotNone(self.comment, comment, "Comment")
            self.assertNotNone(self.comment._obj, comment, "Comment")
        if file is not None:
            self.obj_type = "file"
            self.file = self.conn.getObject("Annotation", file)
            self.assertNotNone(self.file, file, "File")
            self.assertNotNone(self.file._obj, file, "File")
        if annotation is not None:
            self.obj_type = "annotation"
            self.annotation = self.conn.getObject("Annotation", annotation)
            self.assertNotNone(self.annotation, annotation, "Annotation")
            self.assertNotNone(self.annotation._obj, annotation, "Annotation")
        if orphaned:
            self.orphaned = True

    def assertNotNone(self, obj, obj_id, obj_name):
        if obj is None:
            raise AttributeError(
                "We are sorry, but that %s (id:%s) does not exist, or if it"
                " does, you have no permission to see it."
                % (obj_name, obj_id))

    def _get_object(self):
        """
        Since the container is often used to wrap a single Project, Dataset
        etc, several methods need access to the underlying object. E.g.
        obj_type(), obj_id(), canAnnotate(), canEdit().
        This removes many if statements from the metadata_general.html
        template for places that are displaying data for a single Object. E.g.
        Edit Name etc.
        """
        if self.project is not None:
            return self.project
        if self.dataset is not None:
            return self.dataset
        if self.image is not None:
            return self.image
        if self.screen is not None:
            return self.screen
        if self.acquisition is not None:
            return self.acquisition
        if self.plate is not None:
            return self.plate
        if self.well is not None:
            return self.well
        if self.tag is not None:
            return self.tag
        if self.file is not None:
            return self.file

    def obj_id(self):
        obj = self._get_object()
        return obj is not None and obj.id or None

    def canAnnotate(self):
        obj = self._get_object()
        return obj is not None and obj.canAnnotate() or False

    def canEdit(self):
        obj = self._get_object()
        return obj is not None and obj.canEdit() or None

    def getPermsCss(self):
        """ Shortcut to get permissions flags, E.g. for css """
        return self._get_object().getPermsCss()

    def getNumberOfFields(self):
        """ Applies to Plates (all fields) or PlateAcquisitions"""
        if self.plate is not None:
            return self.plate.getNumberOfFields()
        elif self.acquisition:
            p = self.conn.getObject(
                "Plate", self.acquisition._obj.plate.id.val)
            return p.getNumberOfFields(self.acquisition.getId())

    def getPlateId(self):
        """ Used by templates that display Plates or PlateAcquisitions """
        if self.plate is not None:
            return self.plate.getId()
        elif self.acquisition:
            return self.acquisition._obj.plate.id.val

    def canExportAsJpg(self, request, objDict=None):
        """
        Can't export as Jpg, Png, Tiff if bigger than approx 12k * 12k.
        Limit set by OOM error in omeis.providers.re.RGBIntBuffer
        """
        can = True
        try:
            limit = request.session['server_settings'][
                'download_as']['max_size']
        except:
            limit = 144000000
        if self.image:
            if (self.image.getSizeX() * self.image.getSizeY()) > limit:
                can = False
        elif objDict is not None:
            if 'image' in objDict:
                for i in objDict['image']:
                    if (i.getSizeX() * i.getSizeY()) > limit:
                        can = False
        return can

    def canDownload(self, objDict=None):
        """
        Returns False if any of selected object cannot be downloaded
        """
        # As used in batch_annotate panel
        if objDict is not None:
            for key in objDict:
                for o in objDict[key]:
                    if hasattr(o, 'canDownload'):
                        if not o.canDownload():
                            return False
            return True
        # As used in metadata_general panel
        else:
            return self.image.canDownload() or \
                self.well.canDownload() or self.plate.canDownload()

    def listFigureScripts(self, objDict=None):
        """
        This configures all the Figure Scripts, setting their enabled status
        given the currently selected object (self.image etc) or batch objects
        (uses objDict).
        """
        figureScripts = []
        # id is used in url and is mapped to full script path by
        # views.figure_script()
        splitView = {
            'id': 'SplitView',
            'name': 'Split View Figure',
            'enabled': False,
            'tooltip': ("Create a figure of images, splitting their channels"
                        " into separate views")}
        # Split View Figure is enabled if we have at least one image with
        # SizeC > 1
        if self.image:
            splitView['enabled'] = (self.image.getSizeC() > 1)
        elif objDict is not None:
            if 'image' in objDict:
                for i in objDict['image']:
                    if i.getSizeC() > 1:
                        splitView['enabled'] = True
                        break
        thumbnailFig = {
            'id': 'Thumbnail',
            'name': 'Thumbnail Figure',
            'enabled': False,
            'tooltip': ("Export a figure of thumbnails, optionally sorted by"
                        " tag")}
        # Thumbnail figure is enabled if we have Datasets or Images selected
        if self.image or self.dataset:
            thumbnailFig['enabled'] = True
        elif objDict is not None:
            if 'image' in objDict or 'dataset' in objDict:
                thumbnailFig['enabled'] = True

        makeMovie = {
            'id': 'MakeMovie',
            'name': 'Make Movie',
            'enabled': False,
            'tooltip': "Create a movie of the image"}
        if (self.image and (self.image.getSizeT() > 0 or
                            self.image.getSizeZ() > 0)):
            makeMovie['enabled'] = True

        figureScripts.append(splitView)
        figureScripts.append(thumbnailFig)
        figureScripts.append(makeMovie)
        return figureScripts

    def formatMetadataLine(self, l):
        if len(l) < 1:
            return None
        return l.split("=")

    def companionFiles(self):
        # Look for companion files on the Image
        self.companion_files = list()
        if self.image is not None:
            comp_obj = self.image
            p = self.image.getPlate()
            # in SPW model, companion files can be found on Plate
            if p is not None:
                comp_obj = p
            for ann in comp_obj.listAnnotations():
                if (hasattr(ann._obj, "file") and
                        ann.ns == omero.constants.namespaces.NSCOMPANIONFILE):
                    if (ann.getFileName() !=
                            omero.constants.annotation.file.ORIGINALMETADATA):
                        self.companion_files.append(ann)

    def channelMetadata(self, noRE=False):
        self.channel_metadata = None

        if self.image is None and self.well is None:
            return

        img = self.image
        if img is None:
            img = self.well.getWellSample().image()

        # Exceptions handled by webclient_gateway ImageWrapper.getChannels()
        self.channel_metadata = img.getChannels(noRE=noRE)

        if self.channel_metadata is None:
            self.channel_metadata = list()

    def loadTags(self, eid=None):
        if eid is not None:
            if eid == -1:       # Load data for all users
                eid = None
            else:
                self.experimenter = self.conn.getObject("Experimenter", eid)
        else:
            eid = self.conn.getEventContext().userId
        self.tags = list(self.conn.listTags(eid))
        self.tags.sort(
            key=lambda x: x.getTextValue() and x.getTextValue().lower())
        self.t_size = len(self.tags)

    def loadTagsRecursive(self, eid=None, offset=None, limit=1000):
        if eid is not None:
            if eid == -1:       # Load data for all users
                if self.canUseOthersAnns():
                    eid = None
                else:
                    eid = self.conn.getEventContext().userId
            else:
                self.experimenter = self.conn.getObject("Experimenter", eid)
        else:
            eid = self.conn.getEventContext().userId
        self.tags_recursive, self.tags_recursive_owners = \
            self.conn.listTagsRecursive(eid, offset, limit)

    def getTagCount(self, eid=None):
        return self.conn.getTagCount(eid)

    def listContainerHierarchy(self, eid=None):
        if eid is not None:
            if eid == -1:
                eid = None
            else:
                self.experimenter = self.conn.getObject("Experimenter", eid)
        else:
            eid = self.conn.getEventContext().userId
        pr_list = list(self.conn.listProjects(eid))
        ds_list = list(self.conn.listOrphans("Dataset", eid))
        sc_list = list(self.conn.listScreens(eid))
        pl_list = list(self.conn.listOrphans("Plate", eid))

        pr_list.sort(key=lambda x: x.getName() and x.getName().lower())
        ds_list.sort(key=lambda x: x.getName() and x.getName().lower())
        sc_list.sort(key=lambda x: x.getName() and x.getName().lower())
        pl_list.sort(key=lambda x: x.getName() and x.getName().lower())

        self.orphans = self.conn.countOrphans("Image", eid)

        self.containers = {
            'projects': pr_list,
            'datasets': ds_list,
            'screens': sc_list,
            'plates': pl_list}
        self.c_size = len(pr_list)+len(ds_list)+len(sc_list)+len(pl_list)

    def canUseOthersAnns(self):
        """
        Test to see whether other user's Tags, Files etc should be provided
        for annotating.
        Used to ensure that E.g. Group Admins / Owners don't try to link other
        user's Annotations when in a private group (even though they could
        retrieve those annotations)
        """
        gid = self.conn.SERVICE_OPTS.getOmeroGroup()
        if gid is None:
            return False
        try:
            group = self.conn.getObject("ExperimenterGroup", long(gid))
        except:
            return False
        if group is None:
            return False
        perms = str(group.getDetails().getPermissions())
        if perms in ("rwrw--", "rwra--"):
            return True
        if (perms == "rwr---" and (self.conn.isAdmin() or
                                   self.conn.isLeader(group.id))):
            return True
        return False

    def getFilesByObject(self, parent_type=None, parent_ids=None):
        eid = ((not self.canUseOthersAnns()) and
               self.conn.getEventContext().userId or None)
        ns = [omero.constants.namespaces.NSCOMPANIONFILE,
              omero.constants.namespaces.NSEXPERIMENTERPHOTO]

        def sort_file_anns(file_ann_gen):
            file_anns = list(file_ann_gen)
            try:
                file_anns.sort(key=lambda x: x.getFile().getName().lower())
            except:
                pass
            return file_anns

        if self.image is not None:
            return sort_file_anns(self.image.listOrphanedAnnotations(
                eid=eid, ns=ns, anntype='File'))
        elif self.dataset is not None:
            return sort_file_anns(self.dataset.listOrphanedAnnotations(
                eid=eid, ns=ns, anntype='File'))
        elif self.project is not None:
            return sort_file_anns(self.project.listOrphanedAnnotations(
                eid=eid, ns=ns, anntype='File'))
        elif self.well is not None:
            return sort_file_anns(
                self.well.getWellSample().image().listOrphanedAnnotations(
                    eid=eid, ns=ns, anntype='File'))
        elif self.plate is not None:
            return sort_file_anns(self.plate.listOrphanedAnnotations(
                eid=eid, ns=ns, anntype='File'))
        elif self.screen is not None:
            return sort_file_anns(self.screen.listOrphanedAnnotations(
                eid=eid, ns=ns, anntype='File'))
        elif self.acquisition is not None:
            return sort_file_anns(self.acquisition.listOrphanedAnnotations(
                eid=eid, ns=ns, anntype='File'))
        elif parent_type and parent_ids:
            parent_type = parent_type.title()
            if parent_type == "Acquisition":
                parent_type = "PlateAcquisition"
            return sort_file_anns(self.conn.listOrphanedAnnotations(
                parent_type, parent_ids, eid=eid, ns=ns, anntype='File'))
        else:
            return sort_file_anns(self.conn.listFileAnnotations(eid=eid))
    ####################################################################
    # Creation

    def createDataset(self, name, description=None, img_ids=None):
        dsId = self.conn.createDataset(name, description, img_ids)
        if self.project is not None:
            l_ds = omero.model.ProjectDatasetLinkI()
            l_ds.setParent(self.project._obj)
            l_ds.setChild(omero.model.DatasetI(dsId, False))
            # ds.addProjectDatasetLink(l_ds)
            self.conn.saveAndReturnId(l_ds)
        return dsId

    def createProject(self, name, description=None):
        return self.conn.createProject(name, description)

    def createScreen(self, name, description=None):
        return self.conn.createScreen(name, description)

    def createTag(self, name, description=None):
        tId = self.conn.createTag(name, description)
        if (self.tag and
                self.tag.getNs() == omero.constants.metadata.NSINSIGHTTAGSET):
            link = omero.model.AnnotationAnnotationLinkI()
            link.setParent(omero.model.TagAnnotationI(self.tag.getId(), False))
            link.setChild(omero.model.TagAnnotationI(tId, False))
            self.conn.saveObject(link)
        return tId

    def createTagset(self, name, description=None):
        return self.conn.createTagset(name, description)

    def checkMimetype(self, file_type):
        if file_type is None or len(file_type) == 0:
            file_type = "application/octet-stream"
        return file_type

    def createCommentAnnotations(self, content, oids, well_index=0):
        ann = omero.model.CommentAnnotationI()
        ann.textValue = rstring(str(content))
        ann = self.conn.saveAndReturnObject(ann)

        new_links = list()
        for k in oids.keys():
            if len(oids[k]) > 0:
                for ob in oids[k]:
                    if isinstance(ob._obj, omero.model.WellI):
                        t = 'Image'
                        obj = ob.getWellSample(well_index).image()
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

        if len(new_links) > 0:
            self.conn.saveArray(new_links)
        return ann.getId()

    def createTagAnnotations(self, tag, desc, oids, well_index=0,
                             tag_group_id=None):
        """
        Creates a new tag (with description) OR uses existing tag with the
        specified name if found.
        Links the tag to the specified objects.
        @param tag:         Tag text/name
        @param desc:        Tag description
        @param oids:        Dict of Objects and IDs. E.g. {"Image": [1,2,3],
                            "Dataset", [6]}
        """
        ann = None
        try:
            ann = self.conn.findTag(tag, desc)
        except:
            pass
        if ann is None:
            ann = omero.model.TagAnnotationI()
            ann.textValue = rstring(tag.encode('utf8'))
            ann.setDescription(rstring(desc.encode('utf8')))
            ann = self.conn.saveAndReturnObject(ann)
            if tag_group_id:  # Put new tag in given tag set
                tag_group = None
                try:
                    tag_group = self.conn.getObject(
                        'TagAnnotation', tag_group_id)
                except:
                    pass
                if tag_group is not None:
                    link = omero.model.AnnotationAnnotationLinkI()
                    link.parent = tag_group._obj
                    link.child = ann._obj
                    self.conn.saveObject(link)

        new_links = list()
        parent_objs = []
        for k in oids:
            if len(oids[k]) > 0:
                for ob in oids[k]:
                    if isinstance(ob._obj, omero.model.WellI):
                        t = 'Image'
                        obj = ob.getWellSample(well_index).image()
                    elif isinstance(ob._obj, omero.model.PlateAcquisitionI):
                        t = 'PlateAcquisition'
                        obj = ob
                    else:
                        t = k.lower().title()
                        obj = ob
                    parent_objs.append(obj)
                    l_ann = getattr(omero.model, t+"AnnotationLinkI")()
                    l_ann.setParent(obj._obj)
                    l_ann.setChild(ann._obj)
                    new_links.append(l_ann)

        if len(new_links) > 0:
            # If we retrieved an existing Tag above, link may already exist...
            try:
                self.conn.saveArray(new_links)
            except omero.ValidationException:
                for l in new_links:
                    try:
                        self.conn.saveObject(l)
                    except:
                        pass
        return ann.getId()

    def createFileAnnotations(self, newFile, oids, well_index=0):
        format = self.checkMimetype(newFile.content_type)

        oFile = omero.model.OriginalFileI()
        oFile.setName(rstring(smart_str(newFile.name)))
        oFile.setPath(rstring(smart_str(newFile.name)))
        oFile.hasher = omero.model.ChecksumAlgorithmI()
        oFile.hasher.value = omero.rtypes.rstring("SHA1-160")
        oFile.setMimetype(rstring(str(format)))

        ofid = self.conn.saveAndReturnId(oFile)
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
                        obj = ob.getWellSample(well_index).image()
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
        if len(new_links) > 0:
            new_links = self.conn.getUpdateService().saveAndReturnArray(
                new_links, self.conn.SERVICE_OPTS)
        return fa.getId()

    def createAnnotationsLinks(self, atype, tids, oids, well_index=0):
        """
        Links existing annotations to 1 or more objects

        @param atype:       Annotation type E.g. "tag", "file"
        @param tids:        Annotation IDs
        @param oids:        Dict of Objects and IDs. E.g. {"Image": [1,2,3],
                            "Dataset", [6]}
        """
        atype = str(atype).lower()
        if not atype.lower() in ("tag", "comment", "file"):
            raise AttributeError("Object type must be: tag, comment, file.")

        new_links = list()
        annotations = list(self.conn.getObjects("Annotation", tids))
        parent_objs = []
        for k in oids:
            if len(oids[k]) > 0:
                if k.lower() == 'acquisition':
                    parent_type = 'PlateAcquisition'
                else:
                    parent_type = k.lower().title()
                parent_ids = [o.id for o in oids[k]]
                # check for existing links belonging to Current user
                params = omero.sys.Parameters()
                params.theFilter = omero.sys.Filter()
                params.theFilter.ownerId = rlong(self.conn.getUserId())
                links = self.conn.getAnnotationLinks(
                    parent_type, parent_ids=parent_ids, ann_ids=tids,
                    params=params)
                pcLinks = [(l.parent.id.val, l.child.id.val) for l in links]
                # Create link between each object and annotation
                for ob in self.conn.getObjects(parent_type, parent_ids):
                    parent_objs.append(ob)
                    for a in annotations:
                        if (ob.id, a.id) in pcLinks:
                            continue    # link already exists
                        if isinstance(ob._obj, omero.model.WellI):
                            parent_type = 'Image'
                            obj = ob.getWellSample(well_index).image()
                        else:
                            obj = ob
                        l_ann = getattr(
                            omero.model, parent_type+"AnnotationLinkI")()
                        l_ann.setParent(obj._obj)
                        l_ann.setChild(a._obj)
                        new_links.append(l_ann)
        failed = 0
        saved_links = []
        try:
            # will fail if any of the links already exist
            saved_links = self.conn.getUpdateService().saveAndReturnArray(
                new_links, self.conn.SERVICE_OPTS)
        except omero.ValidationException:
            for l in new_links:
                try:
                    saved_links.append(
                        self.conn.getUpdateService().saveAndReturnObject(
                            l, self.conn.SERVICE_OPTS))
                except:
                    failed += 1

        return tids

    ################################################################
    # Update

    def updateDescription(self, o_type, description=None):
        obj = getattr(self, o_type)._obj
        if description is not None and description != "":
            obj.description = rstring(str(description))
        else:
            obj.description = None
        self.conn.saveObject(obj)

    def updateName(self, o_type, name):
        obj = getattr(self, o_type)._obj
        if o_type not in ('tag', 'tagset'):
            obj.name = rstring(str(name))
        else:
            obj.textValue = rstring(str(name))
        self.conn.saveObject(obj)

    def updateImage(self, name, description=None):
        img = self.image._obj
        img.name = rstring(str(name))
        if description is not None and description != "":
            img.description = rstring(str(description))
        else:
            img.description = None
        self.conn.saveObject(img)

    def updateDataset(self, name, description=None):
        container = self.dataset._obj
        container.name = rstring(str(name))
        if description is not None and description != "":
            container.description = rstring(str(description))
        else:
            container.description = None
        self.conn.saveObject(container)

    def updatePlate(self, name, description=None):
        container = self.plate._obj
        container.name = rstring(str(name))
        if description is not None and description != "":
            container.description = rstring(str(description))
        else:
            container.description = None
        self.conn.saveObject(container)

    def updateProject(self, name, description=None):
        container = self.project._obj
        container.name = rstring(str(name))
        if description is not None and description != "":
            container.description = rstring(str(description))
        else:
            container.description = None
        self.conn.saveObject(container)

    def updateScreen(self, name, description=None):
        container = self.screen._obj
        container.name = rstring(str(name))
        if description is not None and description != "":
            container.description = rstring(str(description))
        else:
            container.description = None
        self.conn.saveObject(container)

    def remove(self, parents, index, tag_owner_id=None):
        """
        Removes the current object (file, tag, comment, dataset, plate, image)
        from its parents by manually deleting the link.
        For Comments, we check whether it becomes an orphan & delete if true
        If self.tag and owner_id is specified, only remove the tag if it is
        owned by that owner

        @param parents:     List of parent IDs, E.g. ['image-123']
        """
        for p in parents:
            parent = p.split('-')
            dtype = str(parent[0])
            parentId = long(parent[1])
            if dtype == "acquisition":
                dtype = "PlateAcquisition"
            if dtype == "well":
                dtype = "Image"
                w = self.conn.getObject("Well", parentId)
                parentId = w.getWellSample(index=index).image().getId()
            if self.tag:
                for al in self.tag.getParentLinks(dtype, [parentId]):
                    if (al is not None and al.canDelete() and (
                            tag_owner_id is None or
                            unwrap(al.details.owner.id) == tag_owner_id)):
                        self.conn.deleteObject(al._obj)
            elif self.file:
                for al in self.file.getParentLinks(dtype, [parentId]):
                    if al is not None and al.canDelete():
                        self.conn.deleteObject(al._obj)
            elif self.comment:
                # remove the comment from specified parent
                for al in self.comment.getParentLinks(dtype, [parentId]):
                    if al is not None and al.canDelete():
                        self.conn.deleteObject(al._obj)
                # we delete the comment if orphaned below

            elif self.dataset is not None:
                if dtype == 'project':
                    for pdl in self.dataset.getParentLinks([parentId]):
                        if pdl is not None:
                            self.conn.deleteObject(pdl._obj)
            elif self.plate is not None:
                if dtype == 'screen':
                    for spl in self.plate.getParentLinks([parentId]):
                        if spl is not None:
                            self.conn.deleteObject(spl._obj)
            elif self.image is not None:
                if dtype == 'dataset':
                    for dil in self.image.getParentLinks([parentId]):
                        if dil is not None:
                            self.conn.deleteObject(dil._obj)
            else:
                raise AttributeError(
                    "Attribute not specified. Cannot be removed.")

        # Having removed comment from all parents, we can delete if orphan
        if self.comment:
            orphan = True

            # Use delete Dry Run...
            cid = self.comment.getId()
            command = Delete2(targetObjects={"CommentAnnotation": [cid]},
                              dryRun=True)
            cb = self.conn.c.submit(command)
            # ...to check for any remaining links
            rsp = cb.getResponse()
            cb.close(True)
            for parentType in ["Project", "Dataset", "Image", "Screen",
                               "Plate", "PlateAcquisition", "Well"]:
                key = 'ome.model.annotations.%sAnnotationLink' % parentType
                if key in rsp.deletedObjects:
                    orphan = False
                    break
            if orphan:
                self.conn.deleteObject(self.comment._obj)

    ##########################################################
    # Copy

    def copyImageToDataset(self, source, destination=None):
        if destination is None:
            # gets every links for child
            dsls = self.conn.getDatasetImageLinks(source[1])
            dslIds = [dsl._obj.id.val for dsl in dsls]
            self.conn.deleteObjects("DatasetImageLink", dslIds, wait=True)
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
            handle = self.conn.deleteObjects(
                "Image", [self.image.id], deleteAnns=anns)
        elif self.dataset:
            handle = self.conn.deleteObjects(
                "Dataset", [self.dataset.id], deleteChildren=child,
                deleteAnns=anns)
        elif self.project:
            handle = self.conn.deleteObjects(
                "Project", [self.project.id], deleteChildren=child,
                deleteAnns=anns)
        elif self.screen:
            handle = self.conn.deleteObjects(
                "Screen", [self.screen.id], deleteChildren=child,
                deleteAnns=anns)
        elif self.plate:
            handle = self.conn.deleteObjects(
                "Plate", [self.plate.id], deleteChildren=True,
                deleteAnns=anns)
        elif self.comment:
            handle = self.conn.deleteObjects(
                "Annotation", [self.comment.id], deleteAnns=anns)
        elif self.tag:
            handle = self.conn.deleteObjects(
                "Annotation", [self.tag.id], deleteAnns=anns)
        elif self.file:
            handle = self.conn.deleteObjects(
                "Annotation", [self.file.id], deleteAnns=anns)
        return handle

    def deleteObjects(self, otype, ids, child=False, anns=False):
        return self.conn.deleteObjects(
            otype, ids, deleteChildren=child, deleteAnns=anns)
