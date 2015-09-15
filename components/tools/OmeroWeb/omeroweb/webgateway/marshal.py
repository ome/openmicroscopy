#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2012-2014 University of Dundee & Open Microscopy Environment.
# All rights reserved.
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
import omero
import time
import re
import logging
import traceback

logger = logging.getLogger(__name__)

from omero.rtypes import unwrap

# OMERO.insight point list regular expression
INSIGHT_POINT_LIST_RE = re.compile(r'points\[([^\]]+)\]')

# OME model point list regular expression
OME_MODEL_POINT_LIST_RE = re.compile(r'([\d.]+),([\d.]+)')


def channelMarshal(channel):
    """
    return a dict with all there is to know about a channel

    @param channel:     L{omero.gateway.ChannelWrapper}
    @return:            Dict
    """

    return {'emissionWave': channel.getEmissionWave(),
            'label': channel.getLabel(),
            'color': channel.getColor().getHtml(),
            'window': {'min': channel.getWindowMin(),
                       'max': channel.getWindowMax(),
                       'start': channel.getWindowStart(),
                       'end': channel.getWindowEnd()},
            'active': channel.isActive()}


def imageMarshal(image, key=None, request=None):
    """
    return a dict with pretty much everything we know and care about an image,
    all wrapped in a pretty structure.

    @param image:   L{omero.gateway.ImageWrapper}
    @param key:     key of specific attributes to select
    @return:        Dict
    """

    image.loadRenderOptions()
    pr = image.getProject()
    ds = None
    wellsample = None
    well = None
    try:
        # Replicating the functionality of the deprecated
        # ImageWrapper.getDataset() with shares in mind.
        # -- Tue Sep  6 10:48:47 BST 2011 (See #6660)
        parents = image.listParents()
        if parents is not None and len(parents) == 1:
            if parents[0].OMERO_CLASS == 'Dataset':
                ds = parents[0]
            elif parents[0].OMERO_CLASS == 'WellSample':
                wellsample = parents[0]
                if wellsample.well is not None:
                    well = wellsample.well
    except omero.SecurityViolation, e:
        # We're in a share so the Image's parent Dataset cannot be loaded
        # or some other permissions related issue has tripped us up.
        logger.warn('Security violation while retrieving Dataset when '
                    'marshaling image metadata: %s' % e.message)

    rv = {
        'id': image.id,
        'meta': {
            'imageName': image.name or '',
            'imageDescription': image.description or '',
            'imageAuthor': image.getAuthor(),
            'projectName': pr and pr.name or 'Multiple',
            'projectId': pr and pr.id or None,
            'projectDescription': pr and pr.description or '',
            'datasetName': ds and ds.name or 'Multiple',
            'datasetId': ds and ds.id or None,
            'datasetDescription': ds and ds.description or '',
            'wellSampleId': wellsample and wellsample.id or '',
            'wellId': well and well.id.val or '',
            'imageTimestamp': time.mktime(
                image.getDate().timetuple()),
            'imageId': image.id,
            'pixelsType': image.getPixelsType(),
            },
        'perms': {
            'canAnnotate': image.canAnnotate(),
            'canEdit': image.canEdit(),
            'canDelete': image.canDelete(),
            'canLink': image.canLink()
            }
        }
    try:
        reOK = image._prepareRenderingEngine()
        if not reOK:
            logger.debug(
                "Failed to prepare Rendering Engine for imageMarshal")
            return rv
    except omero.ConcurrencyException, ce:
        backOff = ce.backOff
        rv = {
            'ConcurrencyException': {
                'backOff': backOff
            }
        }
        return rv
    except Exception, ex:   # Handle everything else.
        rv['Exception'] = ex.message
        logger.error(traceback.format_exc())
        return rv       # Return what we have already, in case it's useful

    # big images
    tiles = image._re.requiresPixelsPyramid()
    rv['tiles'] = tiles
    if (tiles):
        width, height = image._re.getTileSize()
        levels = image._re.getResolutionLevels()
        zoomLevelScaling = image.getZoomLevelScaling()

        rv.update({'tile_size': {'width': width,
                                 'height': height},
                   'levels': levels})
        if zoomLevelScaling is not None:
            rv['zoomLevelScaling'] = zoomLevelScaling

    nominalMagnification = image.getObjectiveSettings() is not None \
        and image.getObjectiveSettings().getObjective().getNominalMagnification() \
        or None

    try:
        init_zoom = request.session['server_settings']['initial_zoom_level']
        if init_zoom < 0:
            init_zoom = levels + init_zoom
    except:
        init_zoom = 0

    try:
        rv.update({
            'size': {'width': image.getSizeX(),
                     'height': image.getSizeY(),
                     'z': image.getSizeZ(),
                     't': image.getSizeT(),
                     'c': image.getSizeC()},
            'pixel_size': {'x': image.getPixelSizeX(),
                           'y': image.getPixelSizeY(),
                           'z': image.getPixelSizeZ()},
            })
        if init_zoom is not None:
            rv['init_zoom'] = init_zoom
        if nominalMagnification is not None:
            rv.update({'nominalMagnification': nominalMagnification})
        try:
            rv['pixel_range'] = image.getPixelRange()
            rv['channels'] = map(lambda x: channelMarshal(x),
                                 image.getChannels())
            rv['split_channel'] = image.splitChannelDims()
            rv['rdefs'] = {'model': (image.isGreyscaleRenderingModel() and
                                     'greyscale' or 'color'),
                           'projection': image.getProjection(),
                           'defaultZ': image._re.getDefaultZ(),
                           'defaultT': image._re.getDefaultT(),
                           'invertAxis': image.isInvertedAxis()}
        except TypeError:
            # Will happen if an image has bad or missing pixel data
            logger.error('imageMarshal', exc_info=True)
            rv['pixel_range'] = (0, 0)
            rv['channels'] = ()
            rv['split_channel'] = ()
            rv['rdefs'] = {'model': 'color',
                           'projection': image.getProjection(),
                           'defaultZ': 0,
                           'defaultT': 0,
                           'invertAxis': image.isInvertedAxis()}
    except AttributeError:
        rv = None
        raise
    if key is not None and rv is not None:
        for k in key.split('.'):
            rv = rv.get(k, {})
        if rv == {}:
            rv = None
    return rv


def shapeMarshal(shape):
    """
    return a dict with all there is to know about a shape

    @param channel:     L{omero.model.ShapeI}
    @return:            Dict
    """
    rv = {}

    def set_if(k, v, func=lambda a: a is not None):
        """
        Sets the key L{k} with the value of L{v} if the unwrapped value L{v}
        passed to L{func} evaluates to True.  In the default case this is
        True if the unwrapped value L{v} is not None.
        """
        v = unwrap(v)
        if func(v):
            rv[k] = v

    rv['id'] = shape.getId().getValue()
    set_if('theT', shape.getTheT())
    set_if('theZ', shape.getTheZ())
    shape_type = type(shape)
    if shape_type == omero.model.RectI:
        rv['type'] = 'Rectangle'
        rv['x'] = shape.getX().getValue()
        rv['y'] = shape.getY().getValue()
        rv['width'] = shape.getWidth().getValue()
        rv['height'] = shape.getHeight().getValue()
    elif shape_type == omero.model.MaskI:
        rv['type'] = 'Mask'
        rv['x'] = shape.getX().getValue()
        rv['y'] = shape.getY().getValue()
        rv['width'] = shape.getWidth().getValue()
        rv['height'] = shape.getHeight().getValue()
        # TODO: support for mask
    elif shape_type == omero.model.EllipseI:
        rv['type'] = 'Ellipse'
        rv['cx'] = shape.getCx().getValue()
        rv['cy'] = shape.getCy().getValue()
        rv['rx'] = shape.getRx().getValue()
        rv['ry'] = shape.getRy().getValue()
    elif shape_type == omero.model.PolylineI:
        rv['type'] = 'PolyLine'
        rv['points'] = stringToSvg(shape.getPoints().getValue())
    elif shape_type == omero.model.LineI:
        rv['type'] = 'Line'
        rv['x1'] = shape.getX1().getValue()
        rv['x2'] = shape.getX2().getValue()
        rv['y1'] = shape.getY1().getValue()
        rv['y2'] = shape.getY2().getValue()
    elif shape_type == omero.model.PointI:
        rv['type'] = 'Point'
        rv['cx'] = shape.getCx().getValue()
        rv['cy'] = shape.getCy().getValue()
    elif shape_type == omero.model.PolygonI:
        rv['type'] = 'Polygon'
        # z = closed line
        rv['points'] = stringToSvg(shape.getPoints().getValue()) + " z"
    elif shape_type == omero.model.LabelI:
        rv['type'] = 'Label'
        rv['x'] = shape.getX().getValue()
        rv['y'] = shape.getY().getValue()
    else:
        logger.debug("Shape type not supported: %s" % str(shape_type))

    text_value = unwrap(shape.getTextValue())
    if text_value is not None:
        # only populate json with font styles if we have some text
        rv['textValue'] = text_value
        # FIXME: units ignored for font size
        if shape.getFontSize() is not None:
            set_if('fontSize', shape.getFontSize().getValue())
        set_if('fontStyle', shape.getFontStyle())
        set_if('fontFamily', shape.getFontFamily())

    set_if('transform', shape.getTransform(),
           func=lambda a: a is not None and a != 'None')
    fill_color = unwrap(shape.getFillColor())
    if fill_color is not None:
        rv['fillColor'], rv['fillAlpha'] = rgb_int2css(fill_color)
    stroke_color = unwrap(shape.getStrokeColor())
    if stroke_color is not None:
        rv['strokeColor'], rv['strokeAlpha'] = rgb_int2css(stroke_color)
    if shape.getStrokeWidth() is not None:
        # FIXME: units ignored for stroke width
        set_if('strokeWidth', shape.getStrokeWidth().getValue())
    return rv


def stringToSvg(string):
    """
    Method for converting the string returned from
    omero.model.ShapeI.getPoints() into an SVG for display on web.
    E.g: "points[309,427, 366,503, 190,491] points1[309,427, 366,503, 190,491]
          points2[309,427, 366,503, 190,491]"
    To: M 309 427 L 366 503 L 190 491 z
    """
    point_list = string.strip()
    match = INSIGHT_POINT_LIST_RE.search(point_list)
    if match is not None:
        point_list = match.group(1)
    point_list = OME_MODEL_POINT_LIST_RE.findall(point_list)
    if len(point_list) == 0:
        logger.error("Unrecognised ROI shape 'points' string: %r" % string)
        return ""
    point_list = ' L '.join([' '.join(point) for point in point_list])
    return "M %s" % point_list


def rgb_int2css(rgbint):
    """
    converts a bin int number into css colour, E.g. -1006567680 to '#00ff00'
    """
    alpha = rgbint // 256 // 256 // 256 % 256
    alpha = float(alpha) / 256
    r, g, b = (rgbint // 256 // 256 % 256, rgbint // 256 % 256, rgbint % 256)
    return "#%02x%02x%02x" % (r, g, b), alpha


def chgrpMarshal(conn, rsp):
    """
    Helper for marshalling a Chgrp response.
    Uses conn to lookup unlinked objects.
    """
    rv = {}
    if isinstance(rsp, omero.cmd.ERR):
        rsp_params = ", ".join(["%s: %s" % (k, v) for k, v in
                               rsp.parameters.items()])
        rv['error'] = rsp.message
        rv['report'] = "%s %s" % (rsp.name, rsp_params)
    else:
        included = rsp.responses[0].includedObjects
        deleted = rsp.responses[0].deletedObjects
        rv['includedObjects'] = included
        rv['deletedObjects'] = deleted

        # Included: just simplify the key, e.g. -> Projects, Datasets etc
        includedDetails = {}
        objKeys = ['ome.model.containers.Project',
                   'ome.model.containers.Dataset',
                   'ome.model.core.Image',
                   'ome.model.screen.Screen',
                   'ome.model.screen.Plate',
                   'ome.model.screen.Well']
        for k in objKeys:
            if k in included:
                otype = k.split(".")[-1]
                includedDetails[otype + 's'] = included[k]
        rv['includedDetails'] = includedDetails

        # Annotation links - need to get info on linked objects
        tags = {}
        files = {}
        comments = 0
        others = 0
        annotationLinks = ['ome.model.annotations.ProjectAnnotationLink',
                           'ome.model.annotations.DatasetAnnotationLink',
                           'ome.model.annotations.ImageAnnotationLink',
                           'ome.model.annotations.ScreenAnnotationLink',
                           'ome.model.annotations.PlateAnnotationLink',
                           'ome.model.annotations.WellAnnotationLink']
        for l in annotationLinks:
            if l in deleted:
                linkType = l.split(".")[-1]
                params = omero.sys.ParametersI()
                params.addIds(deleted[l])
                query = ("select annLink from %s as annLink "
                         "join fetch annLink.child as ann "
                         "left outer join fetch ann.file "
                         "where annLink.id in (:ids)" % linkType)
                links = conn.getQueryService().findAllByQuery(
                    query, params, conn.SERVICE_OPTS)
                for lnk in links:
                    ann = lnk.child
                    if isinstance(ann, omero.model.FileAnnotationI):
                        name = unwrap(ann.getFile().getName())
                        files[ann.id.val] = {'id': ann.id.val,
                                             'name': name}
                    elif isinstance(ann, omero.model.TagAnnotationI):
                        name = unwrap(ann.getTextValue())
                        tags[ann.id.val] = {'id': ann.id.val,
                                            'name': name}
                    elif isinstance(ann, omero.model.CommentAnnotationI):
                        comments += 1
                    else:
                        others += 1
        # sort tags & comments
        tags = tags.values()
        tags.sort(key=lambda x: x['name'])
        files = files.values()
        files.sort(key=lambda x: x['name'])
        rv['unlinkedDetails'] = {'Tags': tags,
                                 'Files': files,
                                 'Comments': comments,
                                 'Others': others
                                 }

        # Container links - only report these if we are moving the *parent*,
        # E.g. DatasetImageLinks are only reported if we are moving the Dataset
        # (and the image is left behind). If we were moving the Image then we'd
        # expect the link to be broken (can ignore)
        objects = {}
        containerLinks = {
            'ome.model.containers.ProjectDatasetLink': 'Datasets',
            'ome.model.containers.DatasetImageLink': 'Images',
            'ome.model.screen.ScreenPlateLink': 'Screens'}
        for l, ch in containerLinks.items():
            if l in deleted:
                linkType = l.split(".")[-1]
                params = omero.sys.ParametersI()
                params.addIds(deleted[l])
                query = ("select conLink from %s as conLink "
                         "join fetch conLink.child as ann "
                         "where conLink.id in (:ids)" % linkType)
                links = conn.getQueryService().findAllByQuery(
                    query, params, conn.SERVICE_OPTS)
                for lnk in links:
                    child = lnk.child
                    if child.id.val not in includedDetails[ch]:
                        name = unwrap(child.getName())
                        # Put objects in a dictionary to avoid duplicates
                        if ch not in objects:
                            objects[ch] = {}
                        # E.g. objects['Dataset']['1'] = {}
                        objects[ch][child.id.val] = {'id': child.id.val,
                                                     'name': name}
        # sort objects
        for otype, objs in objects.items():
            objs = objs.values()
            objs.sort(key=lambda x: x['name'])
            # E.g. 'Dataset' objects in 'Datasets'
            rv['unlinkedDetails'][otype] = objs

    return rv
