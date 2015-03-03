#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# container
#
# Copyright (c) 2008-2014 University of Dundee.
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
from django.conf import settings
from django.utils.encoding import smart_str
import logging

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

    def isDownloadDisabled(self, objDict=None):
        """
        Returns True only if we have an SPW object(s) and
        settings.PLATE_DOWNLOAD_ENABLED is false
        """
        # As used in batch_annotate panel
        if objDict is not None:
            spwData = False
            for spw in ('screen', 'plate', 'well', 'acquisition'):
                if spw in objDict and len(objDict[spw]) > 0:
                    spwData = True
            if not spwData:
                return False
        # As used in metadata_general panel
        elif (self.screen is None and self.acquisition is None and
                self.plate is None and self.well is None):
            return False
        if hasattr(settings, 'PLATE_DOWNLOAD_ENABLED'):
            return (not settings.PLATE_DOWNLOAD_ENABLED)

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

    def openAstexViewerCompatible(self):
        """
        Is the image suitable to be viewed with the Volume viewer 'Open Astex
        Viewer' applet?
        Image must be a 'volume' of suitable dimensions and not too big.
        """
        MAX_SIDE = settings.OPEN_ASTEX_MAX_SIDE     # default is 400
        MIN_SIDE = settings.OPEN_ASTEX_MIN_SIDE     # default is 20
        # default is 15625000 (250 * 250 * 250)
        MAX_VOXELS = settings.OPEN_ASTEX_MAX_VOXELS

        if self.image is None:
            return False
        sizeZ = self.image.getSizeZ()
        if self.image.getSizeC() > 1:
            return False
        sizeX = self.image.getSizeX()
        sizeY = self.image.getSizeY()
        if sizeZ < MIN_SIDE or sizeX < MIN_SIDE or sizeY < MIN_SIDE:
            return False
        if sizeX > MAX_SIDE or sizeY > MAX_SIDE or sizeZ > MAX_SIDE:
            return False
        voxelCount = (sizeX * sizeY * sizeZ)
        if voxelCount > MAX_VOXELS:
            return False

        try:
            # if scipy ndimage is not available for interpolation, can only
            # handle smaller images
            import scipy.ndimage  # noqa
        except ImportError:
            logger.debug("Failed to import scipy.ndimage - Open Astex Viewer"
                         " limited to display of smaller images.")
            MAX_VOXELS = (160 * 160 * 160)
            if voxelCount > MAX_VOXELS:
                return False

        return True

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

    def channelMetadata(self):
        self.channel_metadata = None
        try:
            if self.image is not None:
                self.channel_metadata = self.image.getChannels()
            elif self.well is not None:
                self.channel_metadata = \
                    self.well.getWellSample().image().getChannels()
        except:
            pass

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

    def loadDataByTag(self):
        pr_list = list(self.conn.getObjectsByAnnotations(
            'Project', [self.tag.id]))
        ds_list = list(self.conn.getObjectsByAnnotations(
            'Dataset', [self.tag.id]))
        im_list = list(self.conn.getObjectsByAnnotations(
            'Image', [self.tag.id]))
        sc_list = list(self.conn.getObjectsByAnnotations(
            'Screen', [self.tag.id]))
        pl_list = list(self.conn.getObjectsByAnnotations(
            'Plate', [self.tag.id]))
        pa_list = list(self.conn.getObjectsByAnnotations(
            'PlateAcquisition', [self.tag.id]))

        pr_list.sort(key=lambda x: x.getName() and x.getName().lower())
        ds_list.sort(key=lambda x: x.getName() and x.getName().lower())
        im_list.sort(key=lambda x: x.getName() and x.getName().lower())
        sc_list.sort(key=lambda x: x.getName() and x.getName().lower())
        pl_list.sort(key=lambda x: x.getName() and x.getName().lower())
        pa_list.sort(key=lambda x: x.getName() and x.getName().lower())

        self.containers = {
            'projects': pr_list,
            'datasets': ds_list,
            'images': im_list,
            'screens': sc_list,
            'plates': pl_list,
            'aquisitions': pa_list}
        self.c_size = (len(pr_list) + len(ds_list) + len(im_list) +
                       len(sc_list) + len(pl_list) + len(pa_list))

    def listImagesInDataset(self, did, eid=None, page=None,
                            load_pixels=False):
        if eid is not None:
            if eid == -1:       # Load data for all users
                eid = None
            else:
                self.experimenter = self.conn.getObject("Experimenter", eid)
        im_list = list(self.conn.listImagesInDataset(
            oid=did, eid=eid, page=page, load_pixels=load_pixels))
        im_list.sort(key=lambda x: x.getName().lower())
        self.containers = {'images': im_list}
        self.c_size = self.conn.getCollectionCount(
            "Dataset", "imageLinks", [long(did)])[long(did)]

        if page is not None:
            self.paging = self.doPaging(page, len(im_list), self.c_size)

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

    def listOrphanedImages(self, eid=None, page=None):
        if eid is not None:
            if eid == -1:
                eid = None
            else:
                self.experimenter = self.conn.getObject("Experimenter", eid)
        else:
            eid = self.conn.getEventContext().userId

        params = omero.sys.ParametersI()
        if page is not None:
            params.page((int(page)-1)*settings.PAGE, settings.PAGE)
        im_list = list(self.conn.listOrphans(
            "Image", eid=eid, params=params, loadPixels=True))
        im_list.sort(key=lambda x: x.getName().lower())
        self.containers = {'orphaned': True, 'images': im_list}
        self.c_size = self.conn.countOrphans("Image", eid=eid)

        if page is not None:
            self.paging = self.doPaging(page, len(im_list), self.c_size)

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
        self.my_client_map_annotations = list()  # 'should' only be 1
        self.client_map_annotations = list()
        self.map_annotations = list()
        self.companion_files = list()

        annTypes = {omero.model.CommentAnnotationI: self.text_annotations,
                    omero.model.LongAnnotationI: self.long_annotations,
                    omero.model.FileAnnotationI: self.file_annotations,
                    omero.model.TagAnnotationI: self.tag_annotations,
                    omero.model.XmlAnnotationI: self.xml_annotations,
                    omero.model.BooleanAnnotationI: self.boolean_annotations,
                    omero.model.DoubleAnnotationI: self.double_annotations,
                    omero.model.TermAnnotationI: self.term_annotations,
                    omero.model.TimestampAnnotationI: self.time_annotations,
                    omero.model.MapAnnotationI: self.map_annotations}

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
                    if (ann.getFileName() !=
                            omero.constants.annotation.file.ORIGINALMETADATA):
                        self.companion_files.append(ann)
                elif ann.ns == omero.constants.metadata.NSCLIENTMAPANNOTATION:
                    if (ann.getDetails().getOwner().id ==
                            self.conn.getUserId()):
                        self.my_client_map_annotations.append(ann)
                    else:
                        self.client_map_annotations.append(ann)
                else:
                    annTypes[annClass].append(ann)

        self.text_annotations.sort(
            key=lambda x: x.creationEventDate(), reverse=True)
        self.file_annotations.sort(key=lambda x: x.creationEventDate())
        self.rating_annotations.sort(key=lambda x: x.creationEventDate())
        self.tag_annotations.sort(key=lambda x: x.textValue)
        self.map_annotations.sort(key=lambda x: x.creationEventDate())

        self.txannSize = len(self.text_annotations)
        self.fileannSize = len(self.file_annotations)
        self.tgannSize = len(self.tag_annotations)

    def getGroupedRatings(self, rating_annotations=None):
        """
        Groups ratings in preparation for display. Picks out the user's rating
        and groups the remaining ones by value.
        NB: This should be called after annotationList() has loaded
        annotations.
        """
        if rating_annotations is None:
            rating_annotations = self.rating_annotations
        userId = self.conn.getUserId()
        myRating = None
        ratingsByValue = {}
        for r in range(1, 6):
            ratingsByValue[r] = []
        for rating in rating_annotations:
            if rating.getDetails().getOwner().id == userId:
                myRating = rating
            else:
                rVal = rating.getValue()
                if rVal in ratingsByValue:
                    ratingsByValue[rVal].append(rating)

        avgRating = 0
        if (len(rating_annotations) > 0):
            sumRating = sum([r.getValue() for r in rating_annotations])
            avgRating = float(sumRating)/len(rating_annotations)
            avgRating = int(round(avgRating))

        # Experimental display of ratings as in PR #3322
        # groupedRatings = []
        # for r in range(5,0, -1):
        #     ratings = ratingsByValue[r]
        #     if len(ratings) > 0:
        #         groupedRatings.append({
        #             'value': r,
        #             'count': len(ratings),
        #             'owners': ", ".join([
        #                str(r.getDetails().getOwner().getNameWithInitial())
        #                for r in ratings])
        #             })

        myRating = myRating is not None and myRating.getValue() or 0
        # NB: this should be json serializable as used in
        # views.annotate_rating
        return {
            'myRating': myRating,
            'average': avgRating,
            'count': len(rating_annotations)}

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

    def loadBatchAnnotations(self, objDict, ann_ids=None, addedByMe=False):
        """
        Look up the Tags, Files, Comments, Ratings etc that are on one or more
        of the objects in objDect.
        """

        batchAnns = {
            omero.model.CommentAnnotationI: 'Comment',
            omero.model.LongAnnotationI: 'Long',
            omero.model.FileAnnotationI: 'File',
            omero.model.TagAnnotationI: 'Tag',
            omero.model.XmlAnnotationI: 'Xml',
            omero.model.BooleanAnnotationI: 'Boolean',
            omero.model.DoubleAnnotationI: 'Double',
            omero.model.TermAnnotationI: 'Term',
            omero.model.TimestampAnnotationI: 'TimeStamp'
        }

        # return, E.g {"Tag": {AnnId: {'ann': ObjWrapper, 'parents':
        #                              [ImageWrapper, etc] } }, etc...}
        rv = {}
        rv["UserRatings"] = {}
        rv["OtherRatings"] = {}
        # populate empty return map
        for key, value in batchAnns.items():
            rv[value] = {}

        params = omero.sys.Parameters()
        params.theFilter = omero.sys.Filter()
        if addedByMe:
            params.theFilter.ownerId = omero.rtypes.rlong(
                self.conn.getUserId())
        for objType, objList in objDict.items():
            if len(objList) == 0:
                continue
            parent_ids = [o.getId() for o in objList]
            # If we're working with a 'well', we're actually annotating the
            # image
            for i in range(len(objList)):
                o = objList[i]
                if isinstance(o._obj, omero.model.WellI):
                    objType = "Image"
                    # index has already been set
                    parent_ids[i] = o.getWellSample().image().getId()
            if isinstance(objList[0]._obj, omero.model.PlateAcquisitionI):
                objType = 'PlateAcquisition'
            for annLink in self.conn.getAnnotationLinks(
                    objType, parent_ids=parent_ids, ann_ids=ann_ids,
                    params=params):
                ann = annLink.getAnnotation()
                if ann.ns == omero.constants.namespaces.NSCOMPANIONFILE:
                    continue
                annClass = ann._obj.__class__
                if annClass in batchAnns:
                    if ann.ns == omero.constants.metadata.NSINSIGHTRATING:
                        if (ann.getDetails().owner.id.val ==
                                self.conn.getUserId()):
                            annotationsMap = rv["UserRatings"]
                        else:
                            annotationsMap = rv["OtherRatings"]
                    else:
                        # E.g. map for 'Tags'
                        annotationsMap = rv[batchAnns[annClass]]
                    if ann.getId() not in annotationsMap:
                        annotationsMap[ann.getId()] = {
                            'ann': ann,
                            'links': [annLink],
                            'unlink': 0}
                    else:
                        annotationsMap[ann.getId()]['links'].append(annLink)
                    if annLink.canDelete():
                        annotationsMap[ann.getId()]['unlink'] += 1

        # bit more preparation for display...
        batchAnns = {}
        for key, annMap in rv.items():
            # E.g. key = 'Tag', 'Comment', 'File' etc
            annList = []
            for annId, annDict in annMap.items():
                # ann is {'ann':AnnWrapper, 'links'[AnnotationLinkWrapper, ..]}
                # Each ann has links to several objects
                annDict['links'].sort(key=lambda x: x.parent.id.val)
                annDict['added_by'] = ",".join([
                    str(l.getDetails().getOwner().id)
                    for l in annDict['links']])
                annDict['can_remove'] = annDict['unlink'] > 0
                annList.append(annDict)
            batchAnns[key] = annList
        return batchAnns

    def getTagsByObject(self, parent_type=None, parent_ids=None):
        eid = ((not self.canUseOthersAnns()) and
               self.conn.getEventContext().userId or None)

        def sort_tags(tag_gen):
            tag_anns = list(tag_gen)
            try:
                tag_anns.sort(key=lambda x: x.getValue().lower())
            except:
                pass
            return tag_anns

        if self.image is not None:
            return sort_tags(self.image.listOrphanedAnnotations(
                eid=eid, anntype='Tag'))
        elif self.dataset is not None:
            return sort_tags(self.dataset.listOrphanedAnnotations(
                eid=eid, anntype='Tag', ns=['any']))
        elif self.project is not None:
            return sort_tags(self.project.listOrphanedAnnotations(
                eid=eid, anntype='Tag'))
        elif self.well is not None:
            return sort_tags(
                self.well.getWellSample().image().listOrphanedAnnotations(
                    eid=eid, anntype='Tag'))
        elif self.plate is not None:
            return sort_tags(self.plate.listOrphanedAnnotations(
                eid=eid, anntype='Tag'))
        elif self.screen is not None:
            return sort_tags(self.screen.listOrphanedAnnotations(
                eid=eid, anntype='Tag'))
        elif self.acquisition is not None:
            return sort_tags(self.acquisition.listOrphanedAnnotations(
                eid=eid, anntype='Tag'))
        elif parent_type and parent_ids:
            parent_type = parent_type.title()
            if parent_type == "Acquisition":
                parent_type = "PlateAcquisition"
            return sort_tags(self.conn.listOrphanedAnnotations(
                parent_type, parent_ids, eid=eid, anntype='Tag'))
        else:
            if eid is not None:
                params = omero.sys.Parameters()
                params.theFilter = omero.sys.Filter()
                params.theFilter.ownerId = omero.rtypes.rlong(eid)
                return sort_tags(
                    self.conn.getObjects("TagAnnotation", params=params))
            return sort_tags(self.conn.getObjects("TagAnnotation"))

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
        return self.conn.getObject("CommentAnnotation", ann.getId())

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
            ann.textValue = rstring(str(tag))
            ann.setDescription(rstring(str(desc)))
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
                return ('Cannot move dataset to %s.' %
                        self.conn.getOrphanedContainerSettings()[1])
            else:
                return 'Destination not supported.'
        elif self.image is not None:
            if destination[0] == 'dataset':
                up_dsl = None
                # gets every links for child
                dsls = self.image.getParentLinks()
                already_there = None

                # checks links
                for dsl in dsls:
                    # if is already linked to destination
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
            elif (destination[0] == 'experimenter' or
                    destination[0] == 'orphaned'):
                if parent[0] != destination[0]:
                    up_dsl = None
                    # gets every links for child
                    dsls = list(self.image.getParentLinks())
                    if len(dsls) == 1:
                        # gets old parent to delete
                        if dsls[0].parent.id.val == long(parent[1]):
                            up_dsl = dsls[0]
                            self.conn.deleteObjectDirect(up_dsl._obj)
                    else:
                        return ('This image is linked in multiple places.'
                                ' Please unlink the image first.')
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
            elif (destination[0] == 'experimenter' or
                    destination[0] == 'orphaned'):
                if parent[0] != destination[0]:
                    up_spl = None
                    # gets every links for child
                    spls = list(self.plate.getParentLinks())
                    for spl in spls:
                        if spl.parent.id.val == long(parent[1]):
                            self.conn.deleteObjectDirect(spl._obj)
                            break
            else:
                return 'Destination not supported.'
        else:
            return 'No data was choosen.'
        return

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
                        self.conn.deleteObjectDirect(al._obj)
            elif self.file:
                for al in self.file.getParentLinks(dtype, [parentId]):
                    if al is not None and al.canDelete():
                        self.conn.deleteObjectDirect(al._obj)
            elif self.comment:
                # remove the comment from specified parent
                for al in self.comment.getParentLinks(dtype, [parentId]):
                    if al is not None and al.canDelete():
                        self.conn.deleteObjectDirect(al._obj)
                # if comment is orphan, delete it directly
                orphan = True
                for parentType in ["Project", "Dataset", "Image", "Screen",
                                   "Plate", "PlateAcquisition", "Well"]:
                    annLinks = list(self.conn.getAnnotationLinks(
                        parentType, ann_ids=[self.comment.id]))
                    if len(annLinks) > 0:
                        orphan = False
                        break
                if orphan:
                    self.conn.deleteObjectDirect(self.comment._obj)

            elif self.dataset is not None:
                if dtype == 'project':
                    for pdl in self.dataset.getParentLinks([parentId]):
                        if pdl is not None:
                            self.conn.deleteObjectDirect(pdl._obj)
            elif self.plate is not None:
                if dtype == 'screen':
                    for spl in self.plate.getParentLinks([parentId]):
                        if spl is not None:
                            self.conn.deleteObjectDirect(spl._obj)
            elif self.image is not None:
                if dtype == 'dataset':
                    for dil in self.image.getParentLinks([parentId]):
                        if dil is not None:
                            self.conn.deleteObjectDirect(dil._obj)
            else:
                raise AttributeError(
                    "Attribute not specified. Cannot be removed.")

    def removemany(self, images):
        if self.dataset is not None:
            dil = self.dataset.getParentLinks('image', images)
            if dil is not None:
                self.conn.deleteObjectDirect(dil._obj)
        else:
            raise AttributeError(
                "Attribute not specified. Cannot be removed.")

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
                # gets every links for child
                dsls = self.image.getParentLinks()
                already_there = None

                # checks links
                for dsl in dsls:
                    # if is already linked to destination
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
            # gets every links for child
            dsls = self.conn.getDatasetImageLinks(source[1])
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
