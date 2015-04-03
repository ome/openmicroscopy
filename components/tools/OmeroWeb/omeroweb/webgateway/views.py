#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# webgateway/views.py - django application view handling functions
#
# Copyright (c) 2007-2013 Glencoe Software, Inc. All rights reserved.
#
# This software is distributed under the terms described by the LICENCE file
# you can find at the root of the distribution bundle, which states you are
# free to use it only for non commercial purposes.
# If the file is missing please request a copy by contacting
# jason@glencoesoftware.com.
#
# Author: Carlos Neves <carlos(at)glencoesoftware.com>

import re
import json
import omero
import omero.clients

from django.http import HttpResponse, HttpResponseServerError
from django.http import HttpResponseRedirect, HttpResponseNotAllowed, Http404
from django.template import loader as template_loader
from django.views.decorators.http import require_POST
from django.core.urlresolvers import reverse
from django.conf import settings
from django.template import RequestContext as Context
from django.core.servers.basehttp import FileWrapper
from omero.rtypes import rlong, unwrap
from omero.constants.namespaces import NSBULKANNOTATIONS
from omero_version import build_year
from marshal import imageMarshal, shapeMarshal

try:
    from hashlib import md5
except:
    from md5 import md5

from cStringIO import StringIO
import tempfile

from omero import ApiUsageException
from omero.util.decorators import timeit, TimeIt
from omeroweb.http import HttpJavascriptResponse, HttpJsonResponse, \
    HttpJavascriptResponseServerError

import glob


# from models import StoredConnection

from webgateway_cache import webgateway_cache, CacheBase, webgateway_tempfile

cache = CacheBase()

import logging
import os
import traceback
import time
import zipfile
import shutil

from omeroweb.decorators import login_required, ConnCleaningHttpResponse
from omeroweb.connector import Connector
from omeroweb.webgateway.util import zip_archived_files, getIntOrDefault

logger = logging.getLogger(__name__)

try:
    from PIL import Image
    from PIL import ImageDraw
except:  # pragma: nocover
    try:
        import Image
        import ImageDraw
    except:
        logger.error('No Pillow installed')


def index(request):
    """ /webgateway/ index placeholder """
    return HttpResponse("Welcome to webgateway")


def _safestr(s):
    return unicode(s).encode('utf-8')


class UserProxy (object):
    """
    Represents the current user of the connection, with methods delegating to
    the connection itself.
    """

    def __init__(self, blitzcon):
        """
        Initialises the User proxy with the L{omero.gateway.BlitzGateway}
        connection

        @param blitzcon:    connection
        @type blitzcon:     L{omero.gateway.BlitzGateway}
        """

        self._blitzcon = blitzcon
        self.loggedIn = False

    def logIn(self):
        """ Sets the loggedIn Flag to True """

        self.loggedIn = True

    def isAdmin(self):
        """
        True if the current user is an admin

        @return:    True if the current user is an admin
        @rtype:     Boolean
        """

        return self._blitzcon.isAdmin()

    def canBeAdmin(self):
        """
        True if the current user can be admin

        @return:    True if the current user can be admin
        @rtype:     Boolean
        """

        return self._blitzcon.canBeAdmin()

    def getId(self):
        """
        Returns the ID of the current user

        @return:    User ID
        @rtype:     Long
        """

        return self._blitzcon.getUserId()

    def getName(self):
        """
        Returns the Name of the current user

        @return:    User Name
        @rtype:     String
        """

        return self._blitzcon.getUser().omeName

    def getFirstName(self):
        """
        Returns the first name of the current user

        @return:    First Name
        @rtype:     String
        """

        return self._blitzcon.getUser().firstName or self.getName()

#    def getPreferences (self):
#        return self._blitzcon._user.getPreferences()
#
#    def getUserObj (self):
#        return self._blitzcon._user
#
# class SessionCB (object):
#    def _log (self, what, c):
#        logger.debug('CONN:%s %s:%d:%s' % (what, c._user, os.getpid(),
#                                           c._sessionUuid))
#
#    def create (self, c):
#        self._log('create',c)
#
#    def join (self, c):
#        self._log('join',c)
#
#    def close (self, c):
#        self._log('close',c)
# _session_cb = SessionCB()


def _split_channel_info(rchannels):
    """
    Splits the request query channel information for images into a sequence of
    channels, window ranges and channel colors.

    @param rchannels:   The request string with channel info. E.g
                        1|100:505$0000FF,-2,3|620:3879$FF0000
    @type rchannels:    String
    @return:            E.g. [1, -2, 3] [[100.0, 505.0], (None, None), [620.0,
                        3879.0]] [u'0000FF', None, u'FF0000']
    @rtype:             tuple of 3 lists
    """

    channels = []
    windows = []
    colors = []
    for chan in rchannels.split(','):
        chan = chan.split('|')
        t = chan[0].strip()
        color = None
        if t.find('$') >= 0:
            t, color = t.split('$')
        try:
            channels.append(int(t))
            ch_window = (None, None)
            if len(chan) > 1:
                t = chan[1].strip()
                if t.find('$') >= 0:
                    t, color = t.split('$')
                t = t.split(':')
                if len(t) == 2:
                    try:
                        ch_window = [float(x) for x in t]
                    except ValueError:
                        pass
            windows.append(ch_window)
            colors.append(color)
        except ValueError:
            pass
    logger.debug(str(channels)+","+str(windows)+","+str(colors))
    return channels, windows, colors


def getImgDetailsFromReq(request, as_string=False):
    """
    Break the GET information from the request object into details on how
    to render the image.
    The following keys are recognized:
    z - Z axis position
    t - T axis position
    q - Quality set (0,0..1,0)
    m - Model (g for greyscale, c for color)
    p - Projection (see blitz_gateway.ImageWrapper.PROJECTIONS for keys)
    x - X position (for now based on top/left offset on the browser window)
    y - Y position (same as above)
    c - a comma separated list of channels to be rendered (start index 1)
      - format for each entry [-]ID[|wndst:wndend][#HEXCOLOR][,...]
    zm - the zoom setting (as a percentual value)

    @param request:     http request with keys above
    @param as_string:   If True, return a string representation of the
                        rendering details
    @return:            A dict or String representation of rendering details
                        above.
    @rtype:             Dict or String
    """

    r = request.REQUEST
    rv = {}
    for k in ('z', 't', 'q', 'm', 'zm', 'x', 'y', 'p'):
        if k in r:
            rv[k] = r[k]
    if 'c' in r:
        rv['c'] = []
        ci = _split_channel_info(r['c'])
        logger.debug(ci)
        for i in range(len(ci[0])):
            # a = abs channel, i = channel, s = window start, e = window end,
            # c = color
            rv['c'].append({'a': abs(ci[0][i]), 'i': ci[0][i],
                            's': ci[1][i][0], 'e': ci[1][i][1],
                            'c': ci[2][i]})
    if as_string:
        return "&".join(["%s=%s" % (x[0], x[1]) for x in rv.items()])
    return rv


@login_required()
def render_birds_eye_view(request, iid, size=None,
                          conn=None, **kwargs):
    """
    Returns an HttpResponse wrapped jpeg with the rendered bird's eye view
    for image 'iid'. We now use a thumbnail for performance. #10626

    @param request:     http request
    @param iid:         Image ID
    @param conn:        L{omero.gateway.BlitzGateway} connection
    @param size:        Maximum size of the longest side of the resulting
                        bird's eye view.
    @return:            http response containing jpeg
    """
    if size is None:
        size = 96       # Use cached thumbnail
    return render_thumbnail(request, iid, w=size, **kwargs)


@login_required()
def render_thumbnail(request, iid, w=None, h=None, conn=None, _defcb=None,
                     **kwargs):
    """
    Returns an HttpResponse wrapped jpeg with the rendered thumbnail for image
    'iid'

    @param request:     http request
    @param iid:         Image ID
    @param w:           Thumbnail max width. 64 by default
    @param h:           Thumbnail max height
    @return:            http response containing jpeg
    """
    server_id = request.session['connector'].server_id
    direct = True
    if w is None:
        size = (64,)
    else:
        if h is None:
            size = (int(w),)
        else:
            size = (int(w), int(h))
    if size == (96,):
        direct = False
    user_id = conn.getUserId()
    z = getIntOrDefault(request, 'z', None)
    t = getIntOrDefault(request, 't', None)
    rdefId = getIntOrDefault(request, 'rdefId', None)
    # TODO - cache handles rdefId
    jpeg_data = webgateway_cache.getThumb(request, server_id, user_id, iid,
                                          size)
    if jpeg_data is None:
        prevent_cache = False
        img = conn.getObject("Image", iid)
        if img is None:
            logger.debug("(b)Image %s not found..." % (str(iid)))
            if _defcb:
                jpeg_data = _defcb(size=size)
                prevent_cache = True
            else:
                raise Http404
        else:
            jpeg_data = img.getThumbnail(
                size=size, direct=direct, rdefId=rdefId, z=z, t=t)
            if jpeg_data is None:
                logger.debug("(c)Image %s not found..." % (str(iid)))
                if _defcb:
                    jpeg_data = _defcb(size=size)
                    prevent_cache = True
                else:
                    return HttpResponseServerError(
                        'Failed to render thumbnail')
            else:
                prevent_cache = img._thumbInProgress
        if not prevent_cache:
            webgateway_cache.setThumb(request, server_id, user_id, iid,
                                      jpeg_data, size)
    else:
        pass
    rsp = HttpResponse(jpeg_data, content_type='image/jpeg')
    return rsp


@login_required()
def render_roi_thumbnail(request, roiId, w=None, h=None, conn=None, **kwargs):
    """
    For the given ROI, choose the shape to render (first time-point, mid
    z-section) then render a region around that shape, scale to width and
    height (or default size) and draw the shape on to the region
    """
    server_id = request.session['connector'].server_id

    # need to find the z indices of the first shape in T
    roiResult = conn.getRoiService().findByRoi(
        long(roiId), None, conn.SERVICE_OPTS)
    if roiResult is None or roiResult.rois is None:
        raise Http404
    zz = set()
    minT = None
    shapes = {}
    for roi in roiResult.rois:
        imageId = roi.image.id.val
        for s in roi.copyShapes():
            if s is None:   # seems possible in some situations
                continue
            t = s.getTheT().getValue()
            z = s.getTheZ().getValue()
            shapes[(z, t)] = s
            if minT is None:
                minT = t
            if t < minT:
                zz = set([z])
                minT = t
            elif minT == t:
                zz.add(z)
    zList = list(zz)
    zList.sort()
    midZ = zList[len(zList)/2]
    s = shapes[(midZ, minT)]

    pi = _get_prepared_image(request, imageId, server_id=server_id, conn=conn)

    if pi is None:
        raise Http404
    image, compress_quality = pi

    return get_shape_thumbnail(request, conn, image, s, compress_quality)


@login_required()
def render_shape_thumbnail(request, shapeId, w=None, h=None, conn=None,
                           **kwargs):
    """
    For the given Shape, redner a region around that shape, scale to width and
    height (or default size) and draw the shape on to the region.
    """
    server_id = request.session['connector'].server_id

    # need to find the z indices of the first shape in T
    params = omero.sys.Parameters()
    params.map = {'id': rlong(shapeId)}
    shape = conn.getQueryService().findByQuery(
        "select s from Shape s join fetch s.roi where s.id = :id", params,
        conn.SERVICE_OPTS)

    if shape is None:
        raise Http404

    imageId = shape.roi.image.id.val

    pi = _get_prepared_image(request, imageId, server_id=server_id, conn=conn)
    if pi is None:
        raise Http404
    image, compress_quality = pi

    return get_shape_thumbnail(request, conn, image, shape, compress_quality)


def get_shape_thumbnail(request, conn, image, s, compress_quality):
    """
    Render a region around the specified Shape, scale to width and height (or
    default size) and draw the shape on to the region. Returns jpeg data.

    @param image:   ImageWrapper
    @param s:       omero.model.Shape
    """

    MAX_WIDTH = 250
    color = request.REQUEST.get("color", "fff")
    colours = {"f00": (255, 0, 0), "0f0": (0, 255, 0), "00f": (0, 0, 255),
               "ff0": (255, 255, 0), "fff": (255, 255, 255), "000": (0, 0, 0)}
    lineColour = colours["f00"]
    if color in colours:
        lineColour = colours[color]
    # used for padding if we go outside the image area
    bg_color = (221, 221, 221)

    def pointsStringToXYlist(string):
        """
        Method for converting the string returned from
        omero.model.ShapeI.getPoints() into list of (x,y) points.
        E.g: "points[309,427, 366,503, 190,491] points1[309,427, 366,503,
        190,491] points2[309,427, 366,503, 190,491]"
        """
        pointLists = string.strip().split("points")
        if len(pointLists) < 2:
            logger.error(
                "Unrecognised ROI shape 'points' string: %s" % string)
            return ""
        firstList = pointLists[1]
        xyList = []
        for xy in firstList.strip(" []").split(", "):
            x, y = xy.split(",")
            xyList.append((int(x.strip()), int(y.strip())))
        return xyList

    def xyListToBbox(xyList):
        """
        Returns a bounding box (x,y,w,h) that will contain the shape
        represented by the XY points list
        """
        xList, yList = [], []
        for xy in xyList:
            x, y = xy
            xList.append(x)
            yList.append(y)
        return (min(xList), min(yList), max(xList)-min(xList),
                max(yList)-min(yList))

    bBox = None   # bounding box: (x, y, w, h)
    shape = {}
    theT = s.getTheT() is not None and s.getTheT().getValue() or 0
    theZ = s.getTheZ() is not None and s.getTheZ().getValue() or 0
    if type(s) == omero.model.RectI:
        shape['type'] = 'Rectangle'
        shape['x'] = s.getX().getValue()
        shape['y'] = s.getY().getValue()
        shape['width'] = s.getWidth().getValue()
        shape['height'] = s.getHeight().getValue()
        bBox = (shape['x'], shape['y'], shape['width'], shape['height'])
    elif type(s) == omero.model.MaskI:
        shape['type'] = 'Mask'
        shape['x'] = s.getX().getValue()
        shape['y'] = s.getY().getValue()
        shape['width'] = s.getWidth().getValue()
        shape['height'] = s.getHeight().getValue()
        bBox = (shape['x'], shape['y'], shape['width'], shape['height'])
        # TODO: support for mask
    elif type(s) == omero.model.EllipseI:
        shape['type'] = 'Ellipse'
        shape['cx'] = int(s.getCx().getValue())
        shape['cy'] = int(s.getCy().getValue())
        shape['rx'] = int(s.getRx().getValue())
        shape['ry'] = int(s.getRy().getValue())
        bBox = (shape['cx']-shape['rx'], shape['cy']-shape['ry'],
                2*shape['rx'], 2*shape['ry'])
    elif type(s) == omero.model.PolylineI:
        shape['type'] = 'PolyLine'
        shape['xyList'] = pointsStringToXYlist(s.getPoints().getValue())
        bBox = xyListToBbox(shape['xyList'])
    elif type(s) == omero.model.LineI:
        shape['type'] = 'Line'
        shape['x1'] = int(s.getX1().getValue())
        shape['x2'] = int(s.getX2().getValue())
        shape['y1'] = int(s.getY1().getValue())
        shape['y2'] = int(s.getY2().getValue())
        x = min(shape['x1'], shape['x2'])
        y = min(shape['y1'], shape['y2'])
        bBox = (x, y, max(shape['x1'], shape['x2'])-x,
                max(shape['y1'], shape['y2'])-y)
    elif type(s) == omero.model.PointI:
        shape['type'] = 'Point'
        shape['cx'] = s.getCx().getValue()
        shape['cy'] = s.getCy().getValue()
        bBox = (shape['cx']-50, shape['cy']-50, 100, 100)
    elif type(s) == omero.model.PolygonI:
        shape['type'] = 'Polygon'
        shape['xyList'] = pointsStringToXYlist(s.getPoints().getValue())
        bBox = xyListToBbox(shape['xyList'])
    elif type(s) == omero.model.LabelI:
        shape['type'] = 'Label'
        shape['x'] = s.getX().getValue()
        shape['y'] = s.getY().getValue()
        bBox = (shape['x']-50, shape['y']-50, 100, 100)
    else:
        logger.debug("Shape type not supported: %s" % str(type(s)))

    # we want to render a region larger than the bounding box
    x, y, w, h = bBox
    # make the aspect ratio (w/h) = 3/2
    requiredWidth = max(w, h*3/2)
    requiredHeight = requiredWidth*2/3
    # make the rendered region 1.5 times larger than the bounding box
    newW = int(requiredWidth * 1.5)
    newH = int(requiredHeight * 1.5)
    # Don't want the region to be smaller than the thumbnail dimensions
    if newW < MAX_WIDTH:
        newW = MAX_WIDTH
        newH = newW*2/3
    # Don't want the region to be bigger than a 'Big Image'!

    def getConfigValue(key):
        try:
            return conn.getConfigService().getConfigValue(key)
        except:
            logger.warn("webgateway: get_shape_thumbnail() could not get"
                        " Config-Value for %s" % key)
            pass
    max_plane_width = getConfigValue("omero.pixeldata.max_plane_width")
    max_plane_height = getConfigValue("omero.pixeldata.max_plane_height")
    if (max_plane_width is None or max_plane_height is None or
            (newW > int(max_plane_width)) or (newH > int(max_plane_height))):
        # generate dummy image to return
        dummy = Image.new('RGB', (MAX_WIDTH, MAX_WIDTH*2/3), bg_color)
        draw = ImageDraw.Draw(dummy)
        draw.text((10, 30), "Shape too large to \ngenerate thumbnail",
                  fill=(255, 0, 0))
        rv = StringIO()
        dummy.save(rv, 'jpeg', quality=90)
        return HttpResponse(rv.getvalue(), content_type='image/jpeg')

    xOffset = (newW - w)/2
    yOffset = (newH - h)/2
    newX = int(x - xOffset)
    newY = int(y - yOffset)

    # Need to check if any part of our region is outside the image. (assume
    # that SOME of the region is within the image!)
    sizeX = image.getSizeX()
    sizeY = image.getSizeY()
    left_xs, right_xs, top_xs, bottom_xs = 0, 0, 0, 0
    if newX < 0:
        newW = newW + newX
        left_xs = abs(newX)
        newX = 0
    if newY < 0:
        newH = newH + newY
        top_xs = abs(newY)
        newY = 0
    if newW+newX > sizeX:
        right_xs = (newW+newX) - sizeX
        newW = newW - right_xs
    if newH+newY > sizeY:
        bottom_xs = (newH+newY) - sizeY
        newH = newH - bottom_xs

    # now we should be getting the correct region
    jpeg_data = image.renderJpegRegion(theZ, theT, newX, newY, newW, newH,
                                       level=None,
                                       compression=compress_quality)
    img = Image.open(StringIO(jpeg_data))

    # add back on the xs we were forced to trim
    if left_xs != 0 or right_xs != 0 or top_xs != 0 or bottom_xs != 0:
        jpg_w, jpg_h = img.size
        xs_w = jpg_w + right_xs + left_xs
        xs_h = jpg_h + bottom_xs + top_xs
        xs_image = Image.new('RGBA', (xs_w, xs_h), bg_color)
        xs_image.paste(img, (left_xs, top_xs))
        img = xs_image

    # we have our full-sized region. Need to resize to thumbnail.
    current_w, current_h = img.size
    factor = float(MAX_WIDTH) / current_w
    resizeH = int(current_h * factor)
    img = img.resize((MAX_WIDTH, resizeH))

    draw = ImageDraw.Draw(img)
    if shape['type'] == 'Rectangle':
        rectX = int(xOffset * factor)
        rectY = int(yOffset * factor)
        rectW = int((w+xOffset) * factor)
        rectH = int((h+yOffset) * factor)
        draw.rectangle((rectX, rectY, rectW, rectH), outline=lineColour)
        # hack to get line width of 2
        draw.rectangle((rectX-1, rectY-1, rectW+1, rectH+1),
                       outline=lineColour)
    elif shape['type'] == 'Line':
        lineX1 = (shape['x1'] - newX + left_xs) * factor
        lineX2 = (shape['x2'] - newX + left_xs) * factor
        lineY1 = (shape['y1'] - newY + top_xs) * factor
        lineY2 = (shape['y2'] - newY + top_xs) * factor
        draw.line((lineX1, lineY1, lineX2, lineY2), fill=lineColour, width=2)
    elif shape['type'] == 'Ellipse':
        rectX = int(xOffset * factor)
        rectY = int(yOffset * factor)
        rectW = int((w+xOffset) * factor)
        rectH = int((h+yOffset) * factor)
        draw.ellipse((rectX, rectY, rectW, rectH), outline=lineColour)
        # hack to get line width of 2
        draw.ellipse((rectX-1, rectY-1, rectW+1, rectH+1), outline=lineColour)
    elif shape['type'] == 'Point':
        point_radius = 2
        rectX = (MAX_WIDTH/2) - point_radius
        rectY = int(resizeH/2) - point_radius
        rectW = rectX + (point_radius * 2)
        rectH = rectY + (point_radius * 2)
        draw.ellipse((rectX, rectY, rectW, rectH), outline=lineColour)
        # hack to get line width of 2
        draw.ellipse((rectX-1, rectY-1, rectW+1, rectH+1), outline=lineColour)
    elif 'xyList' in shape:
        # resizedXY = [(int(x*factor), int(y*factor))
        #              for (x,y) in shape['xyList']]
        def resizeXY(xy):
            x, y = xy
            return (int((x-newX + left_xs)*factor),
                    int((y-newY + top_xs)*factor))
        resizedXY = [resizeXY(xy) for xy in shape['xyList']]
        # doesn't support 'width' of line
        # draw.polygon(resizedXY, outline=lineColour)
        x2 = y2 = None
        for l in range(1, len(resizedXY)):
            x1, y1 = resizedXY[l-1]
            x2, y2 = resizedXY[l]
            draw.line((x1, y1, x2, y2), fill=lineColour, width=2)
        start_x, start_y = resizedXY[0]
        if shape['type'] != 'PolyLine':
            # Seems possible to have Polygon with only 1 point!
            if x2 is None:
                x2 = start_x + 1  # This will create a visible dot
            if y2 is None:
                y2 = start_y + 1
            draw.line((x2, y2, start_x, start_y), fill=lineColour, width=2)

    rv = StringIO()
    compression = 0.9
    img.save(rv, 'jpeg', quality=int(compression*100))
    jpeg = rv.getvalue()
    return HttpResponse(jpeg, content_type='image/jpeg')


def _get_signature_from_request(request):
    """
    returns a string that identifies this image, along with the settings
    passed on the request.
    Useful for using as img identifier key, for prepared image.

    @param request: http request
    @return:        String
    """

    r = request.REQUEST
    rv = r.get('m', '_') + r.get('p', '_') + r.get('c', '_') + r.get('q', '_')
    return rv


def _get_prepared_image(request, iid, server_id=None, conn=None,
                        saveDefs=False, retry=True):
    """
    Fetches the Image object for image 'iid' and prepares it according to the
    request query, setting the channels, rendering model and projection
    arguments. The compression level is parsed and returned too.
    For parameters in request, see L{getImgDetailsFromReq}

    @param request:     http request
    @param iid:         Image ID
    @param conn:        L{omero.gateway.BlitzGateway} connection
    @param saveDefs:    Try to save the rendering settings, default z and t.
    @param retry:       Try an extra attempt at this method
    @return:            Tuple (L{omero.gateway.ImageWrapper} image, quality)
    """
    r = request.REQUEST
    logger.debug('Preparing Image:%r saveDefs=%r '
                 'retry=%r request=%r conn=%s' % (iid, saveDefs, retry,
                                                  r, str(conn)))
    img = conn.getObject("Image", iid)
    if img is None:
        return
    if 'c' in r:
        logger.debug("c="+r['c'])
        channels, windows, colors = _split_channel_info(r['c'])
        if not img.setActiveChannels(channels, windows, colors):
            logger.debug(
                "Something bad happened while setting the active channels...")
    if r.get('m', None) == 'g':
        img.setGreyscaleRenderingModel()
    elif r.get('m', None) == 'c':
        img.setColorRenderingModel()
    # projection  'intmax' OR 'intmax|5:25'
    p = r.get('p', None)
    pStart, pEnd = None, None
    if p is not None and len(p.split('|')) > 1:
        p, startEnd = p.split('|', 1)
        try:
            pStart, pEnd = [int(s) for s in startEnd.split(':')]
        except ValueError:
            pass
    img.setProjection(p)
    img.setProjectionRange(pStart, pEnd)

    img.setInvertedAxis(bool(r.get('ia', "0") == "1"))
    compress_quality = r.get('q', None)
    if saveDefs:
        'z' in r and img.setDefaultZ(long(r['z'])-1)
        't' in r and img.setDefaultT(long(r['t'])-1)
        img.saveDefaults()
    return (img, compress_quality)


@login_required()
def render_image_region(request, iid, z, t, conn=None, **kwargs):
    """
    Returns a jpeg of the OMERO image, rendering only a region specified in
    query string as region=x,y,width,height. E.g. region=0,512,256,256
    Rendering settings can be specified in the request parameters.

    @param request:     http request
    @param iid:         image ID
    @param z:           Z index
    @param t:           T index
    @param conn:        L{omero.gateway.BlitzGateway} connection
    @return:            http response wrapping jpeg
    """
    server_id = request.session['connector'].server_id
    # if the region=x,y,w,h is not parsed correctly to give 4 ints then we
    # simply provide whole image plane.
    # alternatively, could return a 404?
    # if h == None:
    #    return render_image(request, iid, z, t, server_id=None, _conn=None,
    #                        **kwargs)
    pi = _get_prepared_image(request, iid, server_id=server_id, conn=conn)

    if pi is None:
        raise Http404
    img, compress_quality = pi

    tile = request.REQUEST.get('tile', None)
    region = request.REQUEST.get('region', None)
    level = None

    if tile:
        try:
            img._prepareRenderingEngine()
            w, h = img._re.getTileSize()
            levels = img._re.getResolutionLevels()-1

            zxyt = tile.split(",")

            # w = int(zxyt[3])
            # h = int(zxyt[4])
            level = levels-int(zxyt[0])

            x = int(zxyt[1])*w
            y = int(zxyt[2])*h
        except:
            logger.debug("render_image_region: tile=%s" % tile)
            logger.debug(traceback.format_exc())

    elif region:
        try:
            xywh = region.split(",")

            x = int(xywh[0])
            y = int(xywh[1])
            w = int(xywh[2])
            h = int(xywh[3])
        except:
            logger.debug("render_image_region: region=%s" % region)
            logger.debug(traceback.format_exc())

    # region details in request are used as key for caching.
    jpeg_data = webgateway_cache.getImage(request, server_id, img, z, t)
    if jpeg_data is None:
        jpeg_data = img.renderJpegRegion(z, t, x, y, w, h, level=level,
                                         compression=compress_quality)
        if jpeg_data is None:
            raise Http404
        webgateway_cache.setImage(request, server_id, img, z, t, jpeg_data)

    rsp = HttpResponse(jpeg_data, content_type='image/jpeg')
    return rsp


@login_required()
def render_image(request, iid, z=None, t=None, conn=None, **kwargs):
    """
    Renders the image with id {{iid}} at {{z}} and {{t}} as jpeg.
    Many options are available from the request dict. See
    L{getImgDetailsFromReq} for list.
    I am assuming a single Pixels object on image with image-Id='iid'. May be
    wrong

    @param request:     http request
    @param iid:         image ID
    @param z:           Z index
    @param t:           T index
    @param conn:        L{omero.gateway.BlitzGateway} connection
    @return:            http response wrapping jpeg
    """
    server_id = request.session['connector'].server_id
    pi = _get_prepared_image(request, iid, server_id=server_id, conn=conn)
    if pi is None:
        raise Http404
    img, compress_quality = pi
    jpeg_data = webgateway_cache.getImage(request, server_id, img, z, t)
    if jpeg_data is None:
        jpeg_data = img.renderJpeg(z, t, compression=compress_quality)
        if jpeg_data is None:
            raise Http404
        webgateway_cache.setImage(request, server_id, img, z, t, jpeg_data)

    format = request.REQUEST.get('format', 'jpeg')
    rsp = HttpResponse(jpeg_data, content_type='image/jpeg')
    if 'download' in kwargs and kwargs['download']:
        if format == 'png':
            # convert jpeg data to png...
            i = Image.open(StringIO(jpeg_data))
            output = StringIO()
            i.save(output, 'png')
            jpeg_data = output.getvalue()
            output.close()
            rsp = HttpResponse(jpeg_data, content_type='image/png')
        # don't seem to need to do this for tiff
        elif format == 'tif':
            rsp = HttpResponse(jpeg_data, content_type='image/tif')
        rsp['Content-Type'] = 'application/force-download'
        rsp['Content-Length'] = len(jpeg_data)
        rsp['Content-Disposition'] = (
            'attachment; filename=%s.%s'
            % (img.getName().replace(" ", "_"), format))
    return rsp


@login_required()
def render_ome_tiff(request, ctx, cid, conn=None, **kwargs):
    """
    Renders the OME-TIFF representation of the image(s) with id cid in ctx
    (i)mage, (d)ataset, or (p)roject.
    For multiple images export, images that require pixels pyramid (big
    images) will be silently skipped.
    If exporting a single big image or if all images in a multple image export
    are big, a 404 will be triggered.
    A request parameter dryrun can be passed to return the count of images
    that would actually be exported.

    @param request:     http request
    @param ctx:         'p' or 'd' or 'i'
    @param cid:         Project, Dataset or Image ID
    @param conn:        L{omero.gateway.BlitzGateway} connection
    @return:            http response wrapping the tiff (or zip for multiple
                        files), or redirect to temp file/zip
                        if dryrun is True, returns count of images that would
                        be exported
    """
    server_id = request.session['connector'].server_id
    imgs = []
    if ctx == 'p':
        obj = conn.getObject("Project", cid)
        if obj is None:
            raise Http404
        for d in obj.listChildren():
            imgs.extend(list(d.listChildren()))
        name = obj.getName()
    elif ctx == 'd':
        obj = conn.getObject("Dataset", cid)
        if obj is None:
            raise Http404
        imgs.extend(list(obj.listChildren()))
        selection = filter(None,
                           request.REQUEST.get('selection', '').split(','))
        if len(selection):
            logger.debug(selection)
            logger.debug(imgs)
            imgs = filter(lambda x: str(x.getId()) in selection, imgs)
            logger.debug(imgs)
            if len(imgs) == 0:
                raise Http404
        name = '%s-%s' % (obj.getParent().getName(), obj.getName())
    elif ctx == 'w':
        obj = conn.getObject("Well", cid)
        if obj is None:
            raise Http404
        imgs.extend([x.getImage() for x in obj.listChildren()])
        plate = obj.getParent()
        coord = "%s%s" % (plate.getRowLabels()[obj.row],
                          plate.getColumnLabels()[obj.column])
        name = '%s-%s-%s' % (plate.getParent().getName(), plate.getName(),
                             coord)
    else:
        obj = conn.getObject("Image", cid)
        if obj is None:
            raise Http404
        imgs.append(obj)

    imgs = filter(lambda x: not x.requiresPixelsPyramid(), imgs)

    if request.REQUEST.get('dryrun', False):
        rv = json.dumps(len(imgs))
        c = request.REQUEST.get('callback', None)
        if c is not None and not kwargs.get('_internal', False):
            rv = '%s(%s)' % (c, rv)
        return HttpJavascriptResponse(rv)
    if len(imgs) == 0:
        raise Http404
    if len(imgs) == 1:
        obj = imgs[0]
        key = ('_'.join((str(x.getId()) for x in obj.getAncestry())) +
               '_' + str(obj.getId()) + '_ome_tiff')
        # total name len <= 255, 9 is for .ome.tiff
        fnamemax = 255 - len(str(obj.getId())) - 10
        objname = obj.getName()[:fnamemax]
        fpath, rpath, fobj = webgateway_tempfile.new(
            str(obj.getId()) + '-' + objname + '.ome.tiff', key=key)
        if fobj is True:
            # already exists
            return HttpResponseRedirect(settings.STATIC_URL +
                                        'webgateway/tfiles/' + rpath)
        tiff_data = webgateway_cache.getOmeTiffImage(request, server_id,
                                                     imgs[0])
        if tiff_data is None:
            try:
                tiff_data = imgs[0].exportOmeTiff()
            except:
                logger.debug('Failed to export image (2)', exc_info=True)
                tiff_data = None
            if tiff_data is None:
                webgateway_tempfile.abort(fpath)
                raise Http404
            webgateway_cache.setOmeTiffImage(request, server_id, imgs[0],
                                             tiff_data)
        if fobj is None:
            rsp = HttpResponse(tiff_data, content_type='image/tiff')
            rsp['Content-Disposition'] = ('attachment; filename="%s.ome.tiff"'
                                          % (str(obj.getId()) + '-'+objname))
            rsp['Content-Length'] = len(tiff_data)
            return rsp
        else:
            fobj.write(tiff_data)
            fobj.close()
            return HttpResponseRedirect(settings.STATIC_URL +
                                        'webgateway/tfiles/' + rpath)
    else:
        try:
            img_ids = '+'.join((str(x.getId()) for x in imgs))
            key = ('_'.join((str(x.getId()) for x in imgs[0].getAncestry())) +
                   '_' + md5(img_ids).hexdigest() + '_ome_tiff_zip')
            fpath, rpath, fobj = webgateway_tempfile.new(name + '.zip',
                                                         key=key)
            if fobj is True:
                return HttpResponseRedirect(settings.STATIC_URL +
                                            'webgateway/tfiles/' + rpath)
            logger.debug(fpath)
            if fobj is None:
                fobj = StringIO()
            zobj = zipfile.ZipFile(fobj, 'w', zipfile.ZIP_STORED)
            for obj in imgs:
                tiff_data = webgateway_cache.getOmeTiffImage(request,
                                                             server_id, obj)
                if tiff_data is None:
                    tiff_data = obj.exportOmeTiff()
                    if tiff_data is None:
                        continue
                    webgateway_cache.setOmeTiffImage(request, server_id, obj,
                                                     tiff_data)
                # While ZIP itself doesn't have the 255 char limit for
                # filenames, the FS where these get unarchived might, so trim
                # names
                # total name len <= 255, 9 is for .ome.tiff
                fnamemax = 255 - len(str(obj.getId())) - 10
                objname = obj.getName()[:fnamemax]
                zobj.writestr(str(obj.getId()) + '-'+objname + '.ome.tiff',
                              tiff_data)
            zobj.close()
            if fpath is None:
                zip_data = fobj.getvalue()
                rsp = HttpResponse(zip_data, content_type='application/zip')
                rsp['Content-Disposition'] = (
                    'attachment; filename="%s.zip"' % name)
                rsp['Content-Length'] = len(zip_data)
                return rsp
        except:
            logger.debug(traceback.format_exc())
            raise
        return HttpResponseRedirect(settings.STATIC_URL + 'webgateway/tfiles/'
                                    + rpath)


@login_required()
def render_movie(request, iid, axis, pos, conn=None, **kwargs):
    """
    Renders a movie from the image with id iid

    @param request:     http request
    @param iid:         Image ID
    @param axis:        Movie frames are along 'z' or 't' dimension. String
    @param pos:         The T index (for z axis) or Z index (for t axis)
    @param conn:        L{omero.gateway.BlitzGateway} connection
    @return:            http response wrapping the file, or redirect to temp
                        file
    """
    server_id = request.session['connector'].server_id
    try:
        # Prepare a filename we'll use for temp cache, and check if file is
        # already there
        opts = {}
        opts['format'] = 'video/' + request.REQUEST.get('format', 'quicktime')
        opts['fps'] = int(request.REQUEST.get('fps', 4))
        opts['minsize'] = (512, 512, 'Black')
        ext = '.avi'
        key = "%s-%s-%s-%d-%s-%s" % (iid, axis, pos, opts['fps'],
                                     _get_signature_from_request(request),
                                     request.REQUEST.get('format',
                                                         'quicktime'))

        pos = int(pos)
        pi = _get_prepared_image(request, iid, server_id=server_id, conn=conn)
        if pi is None:
            raise Http404
        img, compress_quality = pi

        fpath, rpath, fobj = webgateway_tempfile.new(img.getName() + ext,
                                                     key=key)
        logger.debug(fpath, rpath, fobj)
        if fobj is True:
            return HttpResponseRedirect(settings.STATIC_URL +
                                        'webgateway/tfiles/' + rpath)
            # os.path.join(rpath, img.getName() + ext))

        if 'optsCB' in kwargs:
            opts.update(kwargs['optsCB'](img))
        opts.update(kwargs.get('opts', {}))
        logger.debug(
            'rendering movie for img %s with axis %s, pos %i and opts %s'
            % (iid, axis, pos, opts))
        # fpath, rpath = webgateway_tempfile.newdir()
        if fpath is None:
            fo, fn = tempfile.mkstemp()
        else:
            fn = fpath  # os.path.join(fpath, img.getName())
        if axis.lower() == 'z':
            dext, mimetype = img.createMovie(fn, 0, img.getSizeZ()-1, pos-1,
                                             pos-1, opts)
        else:
            dext, mimetype = img.createMovie(fn, pos-1, pos-1, 0,
                                             img.getSizeT()-1, opts)
        if dext is None and mimetype is None:
            # createMovie is currently only available on 4.1_custom
            # http://trac.openmicroscopy.org.uk/ome/ticket/3857
            raise Http404
        if fpath is None:
            movie = open(fn).read()
            os.close(fo)
            rsp = HttpResponse(movie, content_type=mimetype)
            rsp['Content-Disposition'] = 'attachment; filename="%s"' \
                % (img.getName()+ext)
            rsp['Content-Length'] = len(movie)
            return rsp
        else:
            fobj.close()
            # shutil.move(fn, fn + ext)
            return HttpResponseRedirect(settings.STATIC_URL +
                                        'webgateway/tfiles/' + rpath)
            # os.path.join(rpath, img.getName() + ext))
    except:
        logger.debug(traceback.format_exc())
        raise


@login_required()
def render_split_channel(request, iid, z, t, conn=None, **kwargs):
    """
    Renders a split channel view of the image with id {{iid}} at {{z}} and
    {{t}} as jpeg.
    Many options are available from the request dict.
    Requires Pillow to be installed on the server.

    @param request:     http request
    @param iid:         Image ID
    @param z:           Z index
    @param t:           T index
    @param conn:        L{omero.gateway.BlitzGateway} connection
    @return:            http response wrapping a jpeg
    """
    server_id = request.session['connector'].server_id
    pi = _get_prepared_image(request, iid, server_id=server_id, conn=conn)
    if pi is None:
        raise Http404
    img, compress_quality = pi
    compress_quality = compress_quality and float(compress_quality) or 0.9
    jpeg_data = webgateway_cache.getSplitChannelImage(request, server_id, img,
                                                      z, t)
    if jpeg_data is None:
        jpeg_data = img.renderSplitChannel(z, t, compression=compress_quality)
        if jpeg_data is None:
            raise Http404
        webgateway_cache.setSplitChannelImage(request, server_id, img, z, t,
                                              jpeg_data)
    rsp = HttpResponse(jpeg_data, content_type='image/jpeg')
    return rsp


def debug(f):
    """
    Decorator for adding debugging functionality to methods.

    @param f:       The function to wrap
    @return:        The wrapped function
    """

    def wrap(request, *args, **kwargs):
        debug = request.REQUEST.getlist('debug')
        if 'slow' in debug:
            time.sleep(5)
        if 'fail' in debug:
            raise Http404
        if 'error' in debug:
            raise AttributeError('Debug requested error')
        return f(request, *args, **kwargs)
    wrap.func_name = f.func_name
    return wrap


def jsonp(f):
    """
    Decorator for adding connection debugging and returning function result as
    json, depending on values in kwargs

    @param f:       The function to wrap
    @return:        The wrapped function, which will return json
    """

    def wrap(request, *args, **kwargs):
        logger.debug('jsonp')
        try:
            server_id = kwargs.get('server_id', None)
            if server_id is None:
                server_id = request.session['connector'].server_id
            kwargs['server_id'] = server_id
            rv = f(request, *args, **kwargs)
            if kwargs.get('_raw', False):
                return rv
            if isinstance(rv, HttpResponse):
                return rv
            rv = json.dumps(rv)
            c = request.REQUEST.get('callback', None)
            if c is not None and not kwargs.get('_internal', False):
                rv = '%s(%s)' % (c, rv)
            if kwargs.get('_internal', False):
                return rv
            return HttpJavascriptResponse(rv)
        except omero.ServerError:
            if kwargs.get('_raw', False) or kwargs.get('_internal', False):
                raise
            return HttpJavascriptResponseServerError(
                '("error in call","%s")' % traceback.format_exc())
        except:
            logger.debug(traceback.format_exc())
            if kwargs.get('_raw', False) or kwargs.get('_internal', False):
                raise
            return HttpJavascriptResponseServerError(
                '("error in call","%s")' % traceback.format_exc())
    wrap.func_name = f.func_name
    return wrap


@debug
@login_required()
def render_row_plot(request, iid, z, t, y, conn=None, w=1, **kwargs):
    """
    Renders the line plot for the image with id {{iid}} at {{z}} and {{t}} as
    gif with transparent background.
    Many options are available from the request dict.
    I am assuming a single Pixels object on image with Image ID='iid'. May be
    wrong
    TODO: cache

    @param request:     http request
    @param iid:         Image ID
    @param z:           Z index
    @param t:           T index
    @param y:           Y position of row to measure
    @param conn:        L{omero.gateway.BlitzGateway} connection
    @param w:           Line width
    @return:            http response wrapping a gif
    """

    if not w:
        w = 1
    pi = _get_prepared_image(request, iid, conn=conn)
    if pi is None:
        raise Http404
    img, compress_quality = pi
    try:
        gif_data = img.renderRowLinePlotGif(int(z), int(t), int(y), int(w))
    except:
        logger.debug('a', exc_info=True)
        raise
    if gif_data is None:
        raise Http404
    rsp = HttpResponse(gif_data, content_type='image/gif')
    return rsp


@debug
@login_required()
def render_col_plot(request, iid, z, t, x, w=1, conn=None, **kwargs):
    """
    Renders the line plot for the image with id {{iid}} at {{z}} and {{t}} as
    gif with transparent background.
    Many options are available from the request dict.
    I am assuming a single Pixels object on image with id='iid'. May be wrong
    TODO: cache

    @param request:     http request
    @param iid:         Image ID
    @param z:           Z index
    @param t:           T index
    @param x:           X position of column to measure
    @param conn:        L{omero.gateway.BlitzGateway} connection
    @param w:           Line width
    @return:            http response wrapping a gif
    """

    if not w:
        w = 1
    pi = _get_prepared_image(request, iid, conn=conn)
    if pi is None:
        raise Http404
    img, compress_quality = pi
    gif_data = img.renderColLinePlotGif(int(z), int(t), int(x), int(w))
    if gif_data is None:
        raise Http404
    rsp = HttpResponse(gif_data, content_type='image/gif')
    return rsp


@login_required()
@jsonp
def imageData_json(request, conn=None, _internal=False, **kwargs):
    """
    Get a dict with image information
    TODO: cache

    @param request:     http request
    @param conn:        L{omero.gateway.BlitzGateway}
    @param _internal:   TODO: ?
    @return:            Dict
    """

    iid = kwargs['iid']
    key = kwargs.get('key', None)
    image = conn.getObject("Image", iid)
    if image is None:
        return HttpJavascriptResponseServerError('""')
    if request.REQUEST.get('getDefaults') == 'true':
        image.resetDefaults(save=False)
    rv = imageMarshal(image, key)
    return rv


@login_required()
@jsonp
def wellData_json(request, conn=None, _internal=False, **kwargs):
    """
    Get a dict with image information
    TODO: cache

    @param request:     http request
    @param conn:        L{omero.gateway.BlitzGateway}
    @param _internal:   TODO: ?
    @return:            Dict
    """

    wid = kwargs['wid']
    well = conn.getObject("Well", wid)
    if well is None:
        return HttpJavascriptResponseServerError('""')
    prefix = kwargs.get('thumbprefix', 'webgateway.views.render_thumbnail')

    def urlprefix(iid):
        return reverse(prefix, args=(iid,))
    xtra = {'thumbUrlPrefix': kwargs.get('urlprefix', urlprefix)}
    rv = well.simpleMarshal(xtra=xtra)
    return rv


@login_required()
@jsonp
def plateGrid_json(request, pid, field=0, conn=None, **kwargs):
    """
    """
    plate = conn.getObject('plate', long(pid))
    try:
        field = long(field or 0)
    except ValueError:
        field = 0
    if plate is None:
        return HttpJavascriptResponseServerError('""')
    grid = []
    prefix = kwargs.get('thumbprefix', 'webgateway.views.render_thumbnail')
    thumbsize = int(request.REQUEST.get('size', 64))
    logger.debug(thumbsize)

    def urlprefix(iid):
        return reverse(prefix, args=(iid, thumbsize))
    xtra = {'thumbUrlPrefix': kwargs.get('urlprefix', urlprefix)}
    server_id = kwargs['server_id']

    rv = webgateway_cache.getJson(request, server_id, plate,
                                  'plategrid-%d-%d' % (field, thumbsize))
    if rv is None:
        plate.setGridSizeConstraints(8, 12)
        for row in plate.getWellGrid(field):
            tr = []
            for e in row:
                if e:
                    i = e.getImage()
                    if i:
                        t = i.simpleMarshal(xtra=xtra)
                        t['wellId'] = e.getId()
                        t['field'] = field
                        tr.append(t)
                        continue
                tr.append(None)
            grid.append(tr)
        rv = {'grid': grid,
              'collabels': plate.getColumnLabels(),
              'rowlabels': plate.getRowLabels()}
        webgateway_cache.setJson(request, server_id, plate, json.dumps(rv),
                                 'plategrid-%d-%d' % (field, thumbsize))
    else:
        rv = json.loads(rv)
    return rv


@login_required()
@jsonp
def listImages_json(request, did, conn=None, **kwargs):
    """
    lists all Images in a Dataset, as json
    TODO: cache

    @param request:     http request
    @param did:         Dataset ID
    @param conn:        L{omero.gateway.BlitzGateway}
    @return:            list of image json.
    """

    dataset = conn.getObject("Dataset", did)
    if dataset is None:
        return HttpJavascriptResponseServerError('""')
    prefix = kwargs.get('thumbprefix', 'webgateway.views.render_thumbnail')

    def urlprefix(iid):
        return reverse(prefix, args=(iid,))
    xtra = {'thumbUrlPrefix': kwargs.get('urlprefix', urlprefix),
            'tiled': request.REQUEST.get('tiled', False),
            }
    return map(lambda x: x.simpleMarshal(xtra=xtra), dataset.listChildren())


@login_required()
@jsonp
def listWellImages_json(request, did, conn=None, **kwargs):
    """
    lists all Images in a Well, as json
    TODO: cache

    @param request:     http request
    @param did:         Well ID
    @param conn:        L{omero.gateway.BlitzGateway}
    @return:            list of image json.
    """

    well = conn.getObject("Well", did)
    if well is None:
        return HttpJavascriptResponseServerError('""')
    prefix = kwargs.get('thumbprefix', 'webgateway.views.render_thumbnail')

    def urlprefix(iid):
        return reverse(prefix, args=(iid,))
    xtra = {'thumbUrlPrefix': kwargs.get('urlprefix', urlprefix)}
    return map(lambda x: x.getImage() and
               x.getImage().simpleMarshal(xtra=xtra),
               well.listChildren())


@login_required()
@jsonp
def listDatasets_json(request, pid, conn=None, **kwargs):
    """
    lists all Datasets in a Project, as json
    TODO: cache

    @param request:     http request
    @param pid:         Project ID
    @param conn:        L{omero.gateway.BlitzGateway}
    @return:            list of dataset json.
    """

    project = conn.getObject("Project", pid)
    if project is None:
        return HttpJavascriptResponse('[]')
    return [x.simpleMarshal(xtra={'childCount': 0})
            for x in project.listChildren()]


@login_required()
@jsonp
def datasetDetail_json(request, did, conn=None, **kwargs):
    """
    return json encoded details for a dataset
    TODO: cache
    """
    ds = conn.getObject("Dataset", did)
    return ds.simpleMarshal()


@login_required()
@jsonp
def listProjects_json(request, conn=None, **kwargs):
    """
    lists all Projects, as json
    TODO: cache

    @param request:     http request
    @param conn:        L{omero.gateway.BlitzGateway}
    @return:            list of project json.
    """

    rv = []
    for pr in conn.listProjects():
        rv.append({'id': pr.id,
                   'name': pr.name,
                   'description': pr.description or ''})
    return rv


@login_required()
@jsonp
def projectDetail_json(request, pid, conn=None, **kwargs):
    """
    grab details from one specific project
    TODO: cache

    @param request:     http request
    @param pid:         Project ID
    @param conn:        L{omero.gateway.BlitzGateway}
    @return:            project details as dict.
    """

    pr = conn.getObject("Project", pid)
    rv = pr.simpleMarshal()
    return rv


def searchOptFromRequest(request):
    """
    Returns a dict of options for searching, based on
    parameters in the http request
    Request keys include:
        - ctx: (http request) 'imgs' to search only images
        - text: (http request) the actual text phrase
        - start: starting index (0 based) for result
        - limit: nr of results to retuen (0 == unlimited)
        - author:
        - grabData:
        - parents:

    @param request:     http request
    @return:            Dict of options
    """

    try:
        r = request.REQUEST
        opts = {
            'search': unicode(r.get('text', '')).encode('utf8'),
            'ctx': r.get('ctx', ''),
            'grabData': not not r.get('grabData', False),
            'parents': not not bool(r.get('parents', False)),
            'start': int(r.get('start', 0)),
            'limit': int(r.get('limit', 0)),
            'key': r.get('key', None)
            }
        author = r.get('author', '')
        if author:
            opts['search'] += ' author:'+author
        return opts
    except:
        logger.error(traceback.format_exc())
        return {}


@TimeIt(logging.INFO)
@login_required()
@jsonp
def search_json(request, conn=None, **kwargs):
    """
    Search for objects in blitz.
    Returns json encoded list of marshalled objects found by the search query
    Request keys include:
        - text: The text to search for
        - ctx: (http request) 'imgs' to search only images
        - text: (http request) the actual text phrase
        - start: starting index (0 based) for result
        - limit: nr of results to retuen (0 == unlimited)
        - author:
        - grabData:
        - parents:

    @param request:     http request
    @param conn:        L{omero.gateway.BlitzGateway}
    @return:            json search results
    TODO: cache
    """
    server_id = request.session['connector'].server_id
    opts = searchOptFromRequest(request)
    rv = []
    logger.debug("searchObjects(%s)" % (opts['search']))
    # search returns blitz_connector wrapper objects

    def urlprefix(iid):
        return reverse('webgateway.views.render_thumbnail', args=(iid,))
    xtra = {'thumbUrlPrefix': kwargs.get('urlprefix', urlprefix)}
    try:
        if opts['ctx'] == 'imgs':
            sr = conn.searchObjects(["image"], opts['search'],
                                    conn.SERVICE_OPTS)
        else:
            # searches P/D/I
            sr = conn.searchObjects(None, opts['search'], conn.SERVICE_OPTS)
    except ApiUsageException:
        return HttpJavascriptResponseServerError('"parse exception"')

    def marshal():
        rv = []
        if (opts['grabData'] and opts['ctx'] == 'imgs'):
            bottom = min(opts['start'], len(sr)-1)
            if opts['limit'] == 0:
                top = len(sr)
            else:
                top = min(len(sr), bottom + opts['limit'])
            for i in range(bottom, top):
                e = sr[i]
            # for e in sr:
                try:
                    rv.append(imageData_json(
                        request, server_id, iid=e.id,
                        key=opts['key'], conn=conn, _internal=True))
                except AttributeError, x:
                    logger.debug('(iid %i) ignoring Attribute Error: %s'
                                 % (e.id, str(x)))
                    pass
                except omero.ServerError, x:
                    logger.debug('(iid %i) ignoring Server Error: %s'
                                 % (e.id, str(x)))
            return rv
        else:
            return map(lambda x: x.simpleMarshal(
                xtra=xtra, parents=opts['parents']), sr)
    rv = timeit(marshal)()
    logger.debug(rv)
    return rv


@require_POST
@login_required()
def save_image_rdef_json(request, iid, conn=None, **kwargs):
    """
    Requests that the rendering defs passed in the request be set as the
    default for this image.
    Rendering defs in request listed at L{getImgDetailsFromReq}
    TODO: jsonp

    @param request:     http request
    @param iid:         Image ID
    @param conn:        L{omero.gateway.BlitzGateway}
    @return:            http response 'true' or 'false'
    """
    server_id = request.session['connector'].server_id
    r = request.REQUEST
    pi = _get_prepared_image(request, iid, server_id=server_id, conn=conn,
                             saveDefs=True)
    if pi is None:
        json_data = 'false'
    else:
        user_id = pi[0]._conn.getEventContext().userId
        webgateway_cache.invalidateObject(server_id, user_id, pi[0])
        pi[0].getThumbnail()
        json_data = 'true'
    if r.get('callback', None):
        json_data = '%s(%s)' % (r['callback'], json_data)
    return HttpJavascriptResponse(json_data)


@login_required()
def list_compatible_imgs_json(request, iid, conn=None, **kwargs):
    """
    Lists the images on the same project that would be viable targets for
    copying rendering settings.
    TODO: change method to:
    list_compatible_imgs_json (request, iid, server_id=None, conn=None,
    **kwargs):

    @param request:     http request
    @param iid:         Image ID
    @param conn:        L{omero.gateway.BlitzGateway}
    @return:            json list of image IDs
    """

    json_data = 'false'
    r = request.REQUEST
    if conn is None:
        img = None
    else:
        img = conn.getObject("Image", iid)

    if img is not None:
        # List all images in project
        imgs = []
        for ds in img.getProject().listChildren():
            imgs.extend(ds.listChildren())
        # Filter the ones that would pass the applySettingsToImages call
        img_ptype = img.getPrimaryPixels().getPixelsType().getValue()
        img_ccount = img.getSizeC()
        img_ew = [x.getLabel() for x in img.getChannels()]
        img_ew.sort()

        def compat(i):
            if long(i.getId()) == long(iid):
                return False
            pp = i.getPrimaryPixels()
            if (pp is None or
                i.getPrimaryPixels().getPixelsType().getValue() != img_ptype
                    or i.getSizeC() != img_ccount):
                return False
            ew = [x.getLabel() for x in i.getChannels()]
            ew.sort()
            if ew != img_ew:
                return False
            return True
        imgs = filter(compat, imgs)
        json_data = json.dumps([x.getId() for x in imgs])

    if r.get('callback', None):
        json_data = '%s(%s)' % (r['callback'], json_data)
    return HttpJavascriptResponse(json_data)


@require_POST
@login_required()
@jsonp
def reset_rdef_json(request, toOwners=False, conn=None, **kwargs):
    """
    Simply takes request 'to_type' and 'toids' and
    delegates to Rendering Settings service to reset
    settings accordings.

    @param toOwners:    if True, default to the owner's settings.
    """

    r = request.POST
    toids = r.getlist('toids')
    to_type = str(r.get('to_type', 'image'))
    to_type = to_type.title()
    if to_type == 'Acquisition':
        to_type = 'PlateAcquisition'

    if len(toids) == 0:
        raise Http404("Need to specify objects in request, E.g."
                      " ?totype=dataset&toids=1&toids=2")

    toids = map(lambda x: long(x), toids)

    rss = conn.getRenderingSettingsService()

    # get the first object and set the group to match
    conn.SERVICE_OPTS.setOmeroGroup('-1')
    o = conn.getObject(to_type, toids[0])
    if o is not None:
        gid = o.getDetails().group.id.val
        conn.SERVICE_OPTS.setOmeroGroup(gid)

    if toOwners:
        rv = rss.resetDefaultsByOwnerInSet(to_type, toids, conn.SERVICE_OPTS)
    else:
        rv = rss.resetDefaultsInSet(to_type, toids, conn.SERVICE_OPTS)

    return rv


@login_required()
@jsonp
def copy_image_rdef_json(request, conn=None, **kwargs):
    """
    If 'fromid' is in request, copy the image ID to session,
    for applying later using this same method.
    If list of 'toids' is in request, paste the image ID from the session
    to the specified images.
    If 'fromid' AND 'toids' are in the reqest, we simply
    apply settings and don't save anything to request.
    If 'to_type' is in request, this can be 'dataset', 'plate', 'acquisition'
    Returns json dict of Boolean:[Image-IDs] for images that have successfully
    had the rendering settings applied, or not.

    @param request:     http request
    @param server_id:
    @param conn:        L{omero.gateway.BlitzGateway}
    @return:            json dict of Boolean:[Image-IDs]
    """

    server_id = request.session['connector'].server_id
    json_data = False

    fromid = request.GET.get('fromid', None)
    toids = request.POST.getlist('toids')
    to_type = str(request.POST.get('to_type', 'image'))
    rdef = None

    if to_type not in ('dataset', 'plate', 'acquisition'):
        to_type = "Image"  # default is image

    # Only 'fromid' is given, simply save to session
    if fromid is not None and len(toids) == 0:
        request.session.modified = True
        request.session['fromid'] = fromid
        if request.session.get('rdef') is not None:
            del request.session['rdef']
        return True

    # If we've got an rdef encoded in request instead of ImageId...
    if request.REQUEST.get('c') is not None:
        # make a map of settings we need
        rdef = {
            'c': str(request.REQUEST.get('c'))    # channels
        }
        if request.REQUEST.get('pixel_range'):
            rdef['pixel_range'] = str(request.REQUEST.get('pixel_range'))
        if request.REQUEST.get('m'):
            rdef['m'] = str(request.REQUEST.get('m'))   # model (grey)
        if request.REQUEST.get('z'):
            rdef['z'] = str(request.REQUEST.get('z'))    # z & t pos
        if request.REQUEST.get('t'):
            rdef['t'] = str(request.REQUEST.get('t'))
        if request.REQUEST.get('imageId'):
            rdef['imageId'] = int(request.REQUEST.get('imageId'))

        if request.method == "GET":
            request.session.modified = True
            request.session['rdef'] = rdef
            # remove any previous rdef we may have via 'fromId'
            if request.session.get('fromid') is not None:
                del request.session['fromid']
            return True

    # Check session for 'fromid'
    if fromid is None:
        fromid = request.session.get('fromid', None)

    # maybe these pair of methods should be on ImageWrapper??
    def getRenderingSettings(image):
        rv = {}
        chs = []
        for i, ch in enumerate(image.getChannels()):
            act = "" if ch.isActive() else "-"
            start = ch.getWindowStart()
            end = ch.getWindowEnd()
            color = ch.getColor().getHtml()
            chs.append("%s%s|%s:%s$%s" % (act, i+1, start, end, color))
        rv['c'] = ",".join(chs)
        rv['m'] = "g" if image.isGreyscaleRenderingModel() else "c"
        rv['z'] = image.getDefaultZ() + 1
        rv['t'] = image.getDefaultT() + 1
        return rv

    def applyRenderingSettings(image, rdef):
        channels, windows, colors = _split_channel_info(rdef['c'])
        # also prepares _re
        image.setActiveChannels(channels, windows, colors)
        if rdef['m'] == 'g':
            image.setGreyscaleRenderingModel()
        else:
            image.setColorRenderingModel()
        if 'z' in rdef:
            image._re.setDefaultZ(long(rdef['z'])-1)
        if 't' in rdef:
            image._re.setDefaultT(long(rdef['t'])-1)
        image.saveDefaults()

    # Use rdef from above or previously saved one...
    if rdef is None:
        rdef = request.session.get('rdef')
    if request.method == "POST":
        originalSettings = None
        fromImage = None
        if fromid is None:
            # if we have rdef, save to source image, then use that image as
            # 'fromId', then revert.
            if rdef is not None and len(toids) > 0:
                fromImage = conn.getObject("Image", rdef['imageId'])
                if fromImage is not None:
                    # copy orig settings
                    originalSettings = getRenderingSettings(fromImage)
                    applyRenderingSettings(fromImage, rdef)
                    fromid = fromImage.getId()

        # If we have both, apply settings...
        try:
            fromid = long(fromid)
            toids = map(lambda x: long(x), toids)
        except TypeError:
            fromid = None
        except ValueError:
            fromid = None
        if fromid is not None and len(toids) > 0:
            fromimg = conn.getObject("Image", fromid)
            userid = fromimg.getOwner().getId()
            json_data = conn.applySettingsToSet(fromid, to_type, toids)
            if json_data and True in json_data:
                for iid in json_data[True]:
                    img = conn.getObject("Image", iid)
                    img is not None and webgateway_cache.invalidateObject(
                        server_id, userid, img)

        # finally - if we temporarily saved rdef to original image, revert
        # if we're sure that from-image is not in the target set (Dataset etc)
        if to_type == "Image" and fromid not in toids:
            if originalSettings is not None and fromImage is not None:
                applyRenderingSettings(fromImage, originalSettings)
        return json_data

    else:
        return HttpResponseNotAllowed(["POST"])


@login_required()
@jsonp
def get_image_rdef_json(request, conn=None, **kwargs):
    """
    Gets any 'rdef' dict from the request.session and
    returns it as json
    """
    rdef = request.session.get('rdef')
    image = None
    if (rdef is None):
        fromid = request.session.get('fromid', None)
        if fromid is not None:
            # We only have an Image to copy rdefs from
            image = conn.getObject("Image", fromid)
        if image is not None:
            rv = imageMarshal(image, None)
            # return rv
            chs = []
            for i, ch in enumerate(rv['channels']):
                act = ch['active'] and str(i+1) or "-%s" % (i+1)
                chs.append("%s|%s:%s$%s" % (act, ch['window']['start'],
                                            ch['window']['end'], ch['color']))
            rdef = {'c': (",".join(chs)),
                    'm': rv['rdefs']['model'],
                    'pixel_range': "%s:%s" % (rv['pixel_range'][0],
                                              rv['pixel_range'][1])}

    return {'rdef': rdef}


@login_required()
def full_viewer(request, iid, conn=None, **kwargs):
    """
    This view is responsible for showing the omero_image template
    Image rendering options in request are used in the display page. See
    L{getImgDetailsFromReq}.

    @param request:     http request.
    @param iid:         Image ID
    @param conn:        L{omero.gateway.BlitzGateway}
    @param **kwargs:    Can be used to specify the html 'template' for
                        rendering
    @return:            html page of image and metadata
    """

    rid = getImgDetailsFromReq(request)
    try:
        image = conn.getObject("Image", iid)
        if image is None:
            logger.debug("(a)Image %s not found..." % (str(iid)))
            raise Http404
        d = {'blitzcon': conn,
             'image': image,
             'opts': rid,
             'build_year': build_year,
             'roiCount': image.getROICount(),
             'viewport_server': kwargs.get(
                 # remove any trailing slash
                 'viewport_server', reverse('webgateway')).rstrip('/'),

             'object': 'image:%i' % int(iid)}

        template = kwargs.get('template',
                              "webgateway/viewport/omero_image.html")
        t = template_loader.get_template(template)
        c = Context(request, d)
        rsp = t.render(c)
    except omero.SecurityViolation:
        raise Http404
    return HttpResponse(rsp)


@login_required()
def download_as(request, iid=None, conn=None, **kwargs):
    """
    Downloads the image as a single jpeg/png/tiff or as a zip (if more than
    one image)
    """
    format = request.REQUEST.get('format', 'png')
    if format not in ('jpeg', 'png', 'tif'):
        format = 'png'

    imgIds = []
    wellIds = []
    if iid is None:
        imgIds = request.REQUEST.getlist('image')
        if len(imgIds) == 0:
            wellIds = request.REQUEST.getlist('well')
            if len(wellIds) == 0:
                return HttpResponseServerError(
                    "No images or wells specified in request."
                    " Use ?image=123 or ?well=123")
    else:
        imgIds = [iid]

    images = []
    if imgIds:
        images = list(conn.getObjects("Image", imgIds))
    elif wellIds:
        try:
            index = int(request.REQUEST.get("index", 0))
        except ValueError:
            index = 0
        for w in conn.getObjects("Well", wellIds):
            images.append(w.getWellSample(index).image())

    if len(images) == 0:
        msg = "Cannot download as %s. Images (ids: %s) not found." \
            % (format, imgIds)
        logger.debug(msg)
        return HttpResponseServerError(msg)

    if len(images) == 1:
        jpeg_data = images[0].renderJpeg()
        if jpeg_data is None:
            raise Http404
        rsp = HttpResponse(jpeg_data, mimetype='image/jpeg')
        rsp['Content-Length'] = len(jpeg_data)
        rsp['Content-Disposition'] = 'attachment; filename=%s.jpg' \
            % (images[0].getName().replace(" ", "_"))
    else:
        temp = tempfile.NamedTemporaryFile(suffix='.download_as')

        def makeImageName(originalName, extension, folder_name):
            name = os.path.basename(originalName)
            imgName = "%s.%s" % (name, extension)
            imgName = os.path.join(folder_name, imgName)
            # check we don't overwrite existing file
            i = 1
            name = imgName[:-(len(extension)+1)]
            while os.path.exists(imgName):
                imgName = "%s_(%d).%s" % (name, i, extension)
                i += 1
            return imgName

        try:
            temp_zip_dir = tempfile.mkdtemp()
            logger.debug("download_as dir: %s" % temp_zip_dir)
            try:
                for img in images:
                    z = t = None
                    try:
                        pilImg = img.renderImage(z, t)
                        imgPathName = makeImageName(
                            img.getName(), format, temp_zip_dir)
                        pilImg.save(imgPathName)
                    finally:
                        # Close RenderingEngine
                        img._re.close()
                # create zip
                zip_file = zipfile.ZipFile(temp, 'w', zipfile.ZIP_DEFLATED)
                try:
                    a_files = os.path.join(temp_zip_dir, "*")
                    for name in glob.glob(a_files):
                        zip_file.write(name, os.path.basename(name))
                finally:
                    zip_file.close()
            finally:
                shutil.rmtree(temp_zip_dir, ignore_errors=True)

            zipName = request.REQUEST.get(
                'zipname', 'Download_as_%s' % format)
            zipName = zipName.replace(" ", "_")
            if not zipName.endswith('.zip'):
                zipName = "%s.zip" % zipName

            # return the zip or single file
            imageFile_data = FileWrapper(temp)
            rsp = HttpResponse(imageFile_data)
            rsp['Content-Length'] = temp.tell()
            rsp['Content-Disposition'] = 'attachment; filename=%s' % zipName
            temp.seek(0)
        except Exception:
            temp.close()
            stack = traceback.format_exc()
            logger.error(stack)
            return HttpResponseServerError(
                "Cannot download file (id:%s).\n%s" % (iid, stack))

    rsp['Content-Type'] = 'application/force-download'
    return rsp


@login_required(doConnectionCleanup=False)
def archived_files(request, iid=None, conn=None, **kwargs):
    """
    Downloads the archived file(s) as a single file or as a zip (if more than
    one file)
    """

    imgIds = []
    wellIds = []
    imgIds = request.REQUEST.getlist('image')
    wellIds = request.REQUEST.getlist('well')
    if iid is None:
        if len(imgIds) == 0 and len(wellIds) == 0:
            return HttpResponseServerError(
                "No images or wells specified in request."
                " Use ?image=123 or ?well=123")
    else:
        imgIds = [iid]

    images = list()
    wells = list()
    if imgIds:
        images = list(conn.getObjects("Image", imgIds))
    elif wellIds:
        try:
            index = int(request.REQUEST.get("index", 0))
        except ValueError:
            index = 0
        wells = conn.getObjects("Well", wellIds)
        for w in wells:
            images.append(w.getWellSample(index).image())
    if len(images) == 0:
        logger.debug(
            "Cannot download archived file becuase Images not found.")
        return HttpResponseServerError(
            "Cannot download archived file because Images not found (ids:"
            " %s)." % (imgIds))

    # Test permissions on images and weels
    for ob in (wells):
        if hasattr(ob, 'canDownload'):
            if not ob.canDownload():
                raise Http404

    for ob in (images):
        well = None
        try:
            well = ob.getParent().getParent()
        except:
            if hasattr(ob, 'canDownload'):
                if not ob.canDownload():
                    raise Http404
        else:
            if well and isinstance(well, omero.gateway.WellWrapper):
                if hasattr(well, 'canDownload'):
                    if not well.canDownload():
                        raise Http404

    # make list of all files, removing duplicates
    fileMap = {}
    for image in images:
        for f in image.getImportedImageFiles():
            fileMap[f.getId()] = f
    files = fileMap.values()

    if len(files) == 0:
        logger.debug("Tried downloading archived files from image with no"
                     " files archived.")
        return HttpResponseServerError("This image has no Archived Files.")

    if len(files) == 1:
        orig_file = files[0]
        rsp = ConnCleaningHttpResponse(orig_file.getFileInChunks())
        rsp.conn = conn
        rsp['Content-Length'] = orig_file.getSize()
        # ',' in name causes duplicate headers
        fname = orig_file.getName().replace(" ", "_").replace(",", ".")
        rsp['Content-Disposition'] = 'attachment; filename=%s' % (fname)
    else:

        temp = tempfile.NamedTemporaryFile(suffix='.archive')
        zipName = request.REQUEST.get('zipname', image.getName())

        try:
            zipName = zip_archived_files(images, temp, zipName)

            # return the zip or single file
            archivedFile_data = FileWrapper(temp)
            rsp = ConnCleaningHttpResponse(archivedFile_data)
            rsp.conn = conn
            rsp['Content-Length'] = temp.tell()
            rsp['Content-Disposition'] = 'attachment; filename=%s' % zipName
            temp.seek(0)
        except Exception:
            temp.close()
            stack = traceback.format_exc()
            logger.error(stack)
            return HttpResponseServerError(
                "Cannot download file (id:%s).\n%s" % (iid, stack))

    rsp['Content-Type'] = 'application/force-download'
    return rsp


@login_required()
@jsonp
def original_file_paths(request, iid, conn=None, **kwargs):
    """
    Get a list of path/name strings for original files associated with the
    image
    """

    image = conn.getObject("Image", iid)
    if image is None:
        raise Http404
    paths = image.getImportedImageFilePaths()
    return {'repo': paths['server_paths'], 'client': paths['client_paths']}


@login_required()
@jsonp
def get_shape_json(request, roiId, shapeId, conn=None, **kwargs):
    roiId = int(roiId)
    shapeId = int(shapeId)
    shape = conn.getQueryService().findByQuery(
        'select shape from Roi as roi '
        'join roi.shapes as shape '
        'where roi.id = %d and shape.id = %d' % (roiId, shapeId),
        None)
    logger.debug('Shape: %r' % shape)
    if shape is None:
        logger.debug('No such shape: %r' % shapeId)
        raise Http404
    return HttpJsonResponse(shapeMarshal(shape))


@login_required()
@jsonp
def get_rois_json(request, imageId, conn=None, **kwargs):
    """
    Returns json data of the ROIs in the specified image.
    """
    rois = []
    roiService = conn.getRoiService()
    # rois = webfigure_utils.getRoiShapes(roiService, long(imageId))  # gets a
    # whole json list of ROIs
    result = roiService.findByImage(long(imageId), None, conn.SERVICE_OPTS)

    for r in result.rois:
        roi = {}
        roi['id'] = r.getId().getValue()
        # go through all the shapes of the ROI
        shapes = []
        for s in r.copyShapes():
            if s is None:   # seems possible in some situations
                continue
            shapes.append(shapeMarshal(s))
        # sort shapes by Z, then T.
        shapes.sort(
            key=lambda x: "%03d%03d"
            % (x.get('theZ', -1), x.get('theT', -1)))
        roi['shapes'] = shapes
        rois.append(roi)

    # sort by ID - same as in measurement tool.
    rois.sort(key=lambda x: x['id'])

    return rois


@login_required(isAdmin=True)
@jsonp
def su(request, user, conn=None, **kwargs):
    """
    If current user is admin, switch the session to a new connection owned by
    'user' (puts the new session ID in the request.session)
    Return False if not possible

    @param request:     http request.
    @param user:        Username of new connection owner
    @param conn:        L{omero.gateway.BlitzGateway}
    @param **kwargs:    Can be used to specify the html 'template' for
                        rendering
    @return:            Boolean
    """
    if request.method == "POST":
        conn.setGroupNameForSession('system')
        connector = request.session['connector']
        connector = Connector(connector.server_id, connector.is_secure)
        session = conn.getSessionService().getSession(conn._sessionUuid)
        ttl = session.getTimeToIdle().val
        connector.omero_session_key = conn.suConn(user, ttl=ttl)._sessionUuid
        request.session['connector'] = connector
        conn.revertGroupForSession()
        conn.seppuku()
        return True
    else:
        context = {
            'url': reverse('webgateway_su', args=[user]),
            'submit': "Do you want to su to %s" % user}
        t = template_loader.get_template(
            'webgateway/base/includes/post_form.html')
        c = Context(request, context)
        return HttpResponse(t.render(c))


def _annotations(request, objtype, objid, conn=None, **kwargs):
    """
    Retrieve annotations for object specified by object type and identifier,
    optionally traversing object model graph.
    Returns dictionary containing annotations in NSBULKANNOTATIONS namespace
    if successful, error information otherwise

    Example:  /annotations/Plate/1/
              retrieves annotations for plate with identifier 1
    Example:  /annotations/Plate.wells/1/
              retrieves annotations for plate that contains well with
              identifier 1
    Example:  /annotations/Screen.plateLinks.child.wells/22/
              retrieves annotations for screen that contains plate with
              well with identifier 22

    @param request:     http request.
    @param objtype:     Type of target object, or type of target object
                        followed by a slash-separated list of properties to
                        resolve
    @param objid:       Identifier of target object, or identifier of object
                        reached by resolving given properties
    @param conn:        L{omero.gateway.BlitzGateway}
    @param **kwargs:    unused
    @return:            A dictionary with key 'error' with an error message or
                        with key 'data' containing an array of dictionaries
                        with keys 'id' and 'file' of the retrieved annotations
    """
    q = conn.getQueryService()
    # If more than one objtype is specified, use all in query to
    # traverse object model graph
    # Example: /annotations/Plate/wells/1/
    #          retrieves annotations from Plate that contains Well 1
    objtype = objtype.split('.')

    query = "select obj0 from %s obj0\n" % objtype[0]
    for i, t in enumerate(objtype[1:]):
        query += "join fetch obj%d.%s obj%d\n" % (i, t, i+1)
    query += """
        left outer join fetch obj0.annotationLinks links
        left outer join fetch links.child
        where obj%d.id=:id""" % (len(objtype) - 1)

    try:
        obj = q.findByQuery(query, omero.sys.ParametersI().addId(objid),
                            conn.createServiceOptsDict())
    except omero.QueryException:
        return dict(error='%s cannot be queried' % objtype,
                    query=query)

    if not obj:
        return dict(error='%s with id %s not found' % (objtype, objid))

    return dict(data=[
        dict(id=annotation.id.val,
             file=annotation.file.id.val)
        for annotation in obj.linkedAnnotationList()
        if unwrap(annotation.getNs()) == NSBULKANNOTATIONS
        ])

annotations = login_required()(jsonp(_annotations))


def _table_query(request, fileid, conn=None, **kwargs):
    """
    Query a table specified by fileid
    Returns a dictionary with query result if successful, error information
    otherwise

    @param request:     http request; querystring must contain key 'query'
                        with query to be executed, or '*' to retrieve all rows.
                        If query is in the format word-number, e.g. "Well-7",
                        if will be run as (word==number), e.g. "(Well==7)".
                        This is supported to allow more readable query strings.
    @param fileid:      Numeric identifier of file containing the table
    @param conn:        L{omero.gateway.BlitzGateway}
    @param **kwargs:    unused
    @return:            A dictionary with key 'error' with an error message
                        or with key 'data' containing a dictionary with keys
                        'columns' (an array of column names) and 'rows'
                        (an array of rows, each an array of values)
    """
    query = request.GET.get('query')
    if not query:
        return dict(
            error='Must specify query parameter, use * to retrieve all')

    r = conn.getSharedResources()
    t = r.openTable(omero.model.OriginalFileI(fileid),
                    conn.createServiceOptsDict())
    if not t:
        return dict(error="Table %s not found" % fileid)

    cols = t.getHeaders()
    rows = t.getNumberOfRows()

    if query == '*':
        hits = range(rows)
    else:
        match = re.match(r'^(\w+)-(\d+)', query)
        if match:
            query = '(%s==%s)' % (match.group(1), match.group(2))
        try:
            hits = t.getWhereList(query, None, 0, rows, 1)
        except Exception:
            return dict(error='Error executing query: %s' % query)

    return dict(data=dict(
        columns=[col.name for col in cols],
        rows=[[col.values[0] for col in t.read(range(len(cols)), hit,
                                               hit+1).columns]
              for hit in hits],
        )
    )

table_query = login_required()(jsonp(_table_query))


@login_required()
@jsonp
def object_table_query(request, objtype, objid, conn=None, **kwargs):
    """
    Query bulk annotations table attached to an object specified by
    object type and identifier, optionally traversing object model graph.
    Returns a dictionary with query result if successful, error information
    otherwise

    Example:  /table/Plate/1/query/?query=*
              queries bulk annotations table for plate with identifier 1
    Example:  /table/Plate.wells/1/query/?query=*
              queries bulk annotations table for plate that contains well with
              identifier 1
    Example:  /table/Screen.plateLinks.child.wells/22/query/?query=Well-22
              queries bulk annotations table for screen that contains plate
              with well with identifier 22

    @param request:     http request.
    @param objtype:     Type of target object, or type of target object
                        followed by a slash-separated list of properties to
                        resolve
    @param objid:       Identifier of target object, or identifier of object
                        reached by resolving given properties
    @param conn:        L{omero.gateway.BlitzGateway}
    @param **kwargs:    unused
    @return:            A dictionary with key 'error' with an error message
                        or with key 'data' containing a dictionary with keys
                        'columns' (an array of column names) and 'rows'
                        (an array of rows, each an array of values)
    """
    a = _annotations(request, objtype, objid, conn, **kwargs)
    if 'error' in a:
        return a

    if len(a['data']) < 1:
        return dict(error='Could not retrieve bulk annotations table')

    # multiple bulk annotations files could be attached, use the most recent
    # one (= the one with the highest identifier)
    fileId = max(annotation['file'] for annotation in a['data'])
    return _table_query(request, fileId, conn, **kwargs)
