#
# webgateway/views.py - django application view handling functions
# 
# Copyright (c) 2007, 2008, 2009 Glencoe Software, Inc. All rights reserved.
# 
# This software is distributed under the terms described by the LICENCE file
# you can find at the root of the distribution bundle, which states you are
# free to use it only for non commercial purposes.
# If the file is missing please request a copy by contacting
# jason@glencoesoftware.com.
#
# Author: Carlos Neves <carlos(at)glencoesoftware.com>

import re
import tempfile

import omero
import omero.clients
from django.http import HttpResponse, HttpResponseServerError, HttpResponseRedirect, Http404, HttpResponseForbidden
from django.utils import simplejson
from django.utils.encoding import smart_str
from django.utils.http import urlquote
from django.core import template_loader
from django.core.urlresolvers import reverse
from django.conf import settings
from django.template import RequestContext as Context
import django.views.generic
from django.views.decorators.http import require_POST
from omero.rtypes import rlong, unwrap
from marshal import imageMarshal, shapeMarshal
import omero_ext.uuid as uuid

try:
    from hashlib import md5
except:
    from md5 import md5
    
try:
    from hashlib import sha1 as sha
except:
    from sha import sha

from cStringIO import StringIO

from omero import client_wrapper, ApiUsageException, InternalException
from omero.gateway import timeit, TimeIt, OriginalFileWrapper
from omeroweb.decorators import ConnCleaningHttpResponse

import Ice


import settings

#from models import StoredConnection

from webgateway_cache import webgateway_cache, CacheBase, webgateway_tempfile

cache = CacheBase()

import logging, os, traceback, time, zipfile, shutil

from omeroweb.decorators import login_required
from omeroweb.connector import Connector

logger = logging.getLogger(__name__)

try:
    from PIL import Image
    from PIL import ImageDraw
except: #pragma: nocover
    try:
        import Image
        import ImageDraw
    except:
        logger.error('No PIL installed')


def _safestr (s):
    return unicode(s).encode('utf-8')

class UserProxy (object):
    """
    Represents the current user of the connection, with methods delegating to the connection itself. 
    """
    
    def __init__ (self, blitzcon):
        """
        Initialises the User proxy with the L{omero.gateway.BlitzGateway} connection
        
        @param blitzcon:    connection
        @type blitzcon:     L{omero.gateway.BlitzGateway}
        """
        
        self._blitzcon = blitzcon
        self.loggedIn = False

    def logIn (self):
        """ Sets the loggedIn Flag to True """
        
        self.loggedIn = True

    def isAdmin (self):
        """ 
        True if the current user is an admin
        
        @return:    True if the current user is an admin
        @rtype:     Boolean
        """
        
        return self._blitzcon.isAdmin()

    def canBeAdmin (self):
        """ 
        True if the current user can be admin
        
        @return:    True if the current user can be admin
        @rtype:     Boolean
        """
        
        return self._blitzcon.canBeAdmin()

    def getId (self):
        """ 
        Returns the ID of the current user
        
        @return:    User ID
        @rtype:     Long
        """
        
        return self._blitzcon.getUserId()

    def getName (self):
        """ 
        Returns the Name of the current user
        
        @return:    User Name
        @rtype:     String
        """
        
        return self._blitzcon.getUser().omeName

    def getFirstName (self):
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

#class SessionCB (object):
#    def _log (self, what, c):
#        logger.debug('CONN:%s %s:%d:%s' % (what, c._user, os.getpid(), c._sessionUuid))
#
#    def create (self, c):
#        self._log('create',c)
#
#    def join (self, c):
#        self._log('join',c)
#
#    def close (self, c):
#        self._log('close',c)
#_session_cb = SessionCB()

def _split_channel_info (rchannels):
    """
    Splits the request query channel information for images into a sequence of channels, window ranges
    and channel colors.
    
    @param rchannels:   The request string with channel info. E.g 1|100:505$0000FF,-2,3|620:3879$FF0000
    @type rchannels:    String
    @return:            E.g. [1, -2, 3] [[100.0, 505.0], (None, None), [620.0, 3879.0]] [u'0000FF', None, u'FF0000']
    @rtype:             tuple of 3 lists
    """
    
    channels = []
    windows = []
    colors = []
    for chan in rchannels.split(','):
        chan = chan.split('|')
        t = chan[0].strip()
        color = None
        if t.find('$')>=0:
            t,color = t.split('$')
        try:
            channels.append(int(t))
            ch_window = (None, None)
            if len(chan) > 1:
                t = chan[1].strip()
                if t.find('$')>=0:
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

def getImgDetailsFromReq (request, as_string=False):
    """ Break the GET information from the request object into details on how to render the image.
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
    @param as_string:   If True, return a string representation of the rendering details
    @return:            A dict or String representation of rendering details above. 
    @rtype:             Dict or String
    """
    
    r = request.REQUEST
    rv = {}
    for k in ('z', 't', 'q', 'm', 'zm', 'x', 'y', 'p'):
        if r.has_key(k):
           rv[k] = r[k]
    if r.has_key('c'):
        rv['c'] = []
        ci = _split_channel_info(r['c'])
        logger.debug(ci)
        for i in range(len(ci[0])):
            # a = abs channel, i = channel, s = window start, e = window end, c = color
          rv['c'].append({'a':abs(ci[0][i]), 'i':ci[0][i], 's':ci[1][i][0], 'e':ci[1][i][1], 'c':ci[2][i]})
    if as_string:
        return "&".join(["%s=%s" % (x[0], x[1]) for x in rv.items()])
    return rv

@login_required()
def render_birds_eye_view (request, iid, size=None,
                           conn=None, **kwargs):
    """
    Returns an HttpResponse wrapped jpeg with the rendered bird's eye view
    for image 'iid'. Rendering settings can be specified in the request
    parameters as in L{render_image} and L{render_image_region}; see
    L{getImgDetailsFromReq} for a complete list.

    @param request:     http request
    @param iid:         Image ID
    @param conn:        L{omero.gateway.BlitzGateway} connection
    @param size:        Maximum size of the longest side of the resulting bird's eye view.
    @return:            http response containing jpeg
    """
    server_id = request.session['connector'].server_id
    img = _get_prepared_image(request, iid, conn=conn, server_id=server_id)
    if img is None:
        logger.debug("(b)Image %s not found..." % (str(iid)))
        raise Http404
    img, compress_quality = img
    return HttpResponse(img.renderBirdsEyeView(size), mimetype='image/jpeg')

@login_required()
def render_thumbnail (request, iid, w=None, h=None, conn=None, _defcb=None, **kwargs):
    """ 
    Returns an HttpResponse wrapped jpeg with the rendered thumbnail for image 'iid' 
    
    @param request:     http request
    @param iid:         Image ID
    @param w:           Thumbnail max width. 64 by default
    @param h:           Thumbnail max height
    @return:            http response containing jpeg
    """
    server_id = request.session['connector'].server_id
    if w is None:
        size = (64,)
    else:
        if h is None:
            size = (int(w),)
        else:
            size = (int(w), int(h))
    user_id = conn.getUserId()
    jpeg_data = webgateway_cache.getThumb(request, server_id, user_id, iid, size)
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
            jpeg_data = img.getThumbnail(size=size)
            if jpeg_data is None:
                logger.debug("(c)Image %s not found..." % (str(iid)))
                if _defcb:
                    jpeg_data = _defcb(size=size)
                    prevent_cache = True
                else:
                    return HttpResponseServerError('Failed to render thumbnail')
            else:
                prevent_cache = img._thumbInProgress
        if not prevent_cache:
            webgateway_cache.setThumb(request, server_id, user_id, iid, jpeg_data, size)
    else:
        pass
    rsp = HttpResponse(jpeg_data, mimetype='image/jpeg')
    return rsp

@login_required()
def render_roi_thumbnail (request, roiId, w=None, h=None, conn=None, **kwargs):
    """
    For the given ROI, choose the shape to render (first time-point, mid z-section) then render 
    a region around that shape, scale to width and height (or default size) and draw the
    shape on to the region
    """
    server_id = request.session['connector'].server_id
    
    # need to find the z indices of the first shape in T
    roiResult = conn.getRoiService().findByRoi(long(roiId), None, conn.SERVICE_OPTS)
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
            shapes[(z,t)] = s
            if minT is None: minT = t
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

    return get_shape_thumbnail (request, conn, image, s, compress_quality)

@login_required()
def render_shape_thumbnail (request, shapeId, w=None, h=None, conn=None, **kwargs):
    """
    For the given Shape, redner a region around that shape, scale to width and height (or default size) and draw the
    shape on to the region. 
    """
    server_id = request.session['connector'].server_id
    
    # need to find the z indices of the first shape in T
    params = omero.sys.Parameters()
    params.map = {'id':rlong(shapeId)}
    shape = conn.getQueryService().findByQuery("select s from Shape s join fetch s.roi where s.id = :id", params, conn.SERVICE_OPTS)

    if shape is None:
        raise Http404

    imageId = shape.roi.image.id.val

    pi = _get_prepared_image(request, imageId, server_id=server_id, conn=conn)
    if pi is None:
        raise Http404
    image, compress_quality = pi

    return get_shape_thumbnail (request, conn, image, shape, compress_quality)


def get_shape_thumbnail (request, conn, image, s, compress_quality):
    """
    Render a region around the specified Shape, scale to width and height (or default size) and draw the
    shape on to the region. Returns jpeg data. 
    
    @param image:   ImageWrapper
    @param s:       omero.model.Shape
    """

    MAX_WIDTH = 250
    color = request.REQUEST.get("color", "fff")
    colours = {"f00":(255,0,0), "0f0":(0,255,0), "00f":(0,0,255), "ff0":(255,255,0), "fff":(255,255,255), "000":(0,0,0)}
    lineColour = colours["f00"]
    if color in colours:
        lineColour = colours[color]
    bg_color = (221,221,221)        # used for padding if we go outside the image area
    
    def pointsStringToXYlist(string):
        """
        Method for converting the string returned from omero.model.ShapeI.getPoints()
        into list of (x,y) points.
        E.g: "points[309,427, 366,503, 190,491] points1[309,427, 366,503, 190,491] points2[309,427, 366,503, 190,491]"
        """
        pointLists = string.strip().split("points")
        if len(pointLists) < 2:
            logger.error("Unrecognised ROI shape 'points' string: %s" % string)
            return ""
        firstList = pointLists[1]
        xyList = []
        for xy in firstList.strip(" []").split(", "):
            x, y = xy.split(",")
            xyList.append( ( int( x.strip() ), int(y.strip() ) ) )
        return xyList

    def xyListToBbox(xyList):
        """ Returns a bounding box (x,y,w,h) that will contain the shape represented by the XY points list """
        xList, yList = [], []
        for xy in xyList:
            x, y = xy
            xList.append(x)
            yList.append(y)
        return (min(xList), min(yList), max(xList)-min(xList), max(yList)-min(yList))

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
        bBox = (shape['cx']-shape['rx'], shape['cy']-shape['ry'], 2*shape['rx'], 2*shape['ry'])
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
        x = min(shape['x1'],shape['x2'])
        y = min(shape['y1'],shape['y2'])
        bBox = (x, y, max(shape['x1'],shape['x2'])-x, max(shape['y1'],shape['y2'])-y)
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
    #print shape
    
    # we want to render a region larger than the bounding box
    x,y,w,h = bBox
    requiredWidth = max(w,h*3/2)            # make the aspect ratio (w/h) = 3/2
    requiredHeight = requiredWidth*2/3
    newW = int(requiredWidth * 1.5)         # make the rendered region 1.5 times larger than the bounding box
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
            logger.warn("webgateway: get_shape_thumbnail() could not get Config-Value for %s" % key)
            pass
    max_plane_width = getConfigValue("omero.pixeldata.max_plane_width")
    max_plane_height = getConfigValue("omero.pixeldata.max_plane_height")
    if max_plane_width is None or max_plane_height is None or (newW > int(max_plane_width)) or (newH > int(max_plane_height)):
        # generate dummy image to return
        dummy = Image.new('RGB', (MAX_WIDTH, MAX_WIDTH*2/3), bg_color)
        draw = ImageDraw.Draw(dummy)
        draw.text((10,30), "Shape too large to \ngenerate thumbnail", fill=(255,0,0))
        rv = StringIO()
        dummy.save(rv, 'jpeg', quality=90)
        return HttpResponse(rv.getvalue(), mimetype='image/jpeg')

    xOffset = (newW - w)/2
    yOffset = (newH - h)/2
    newX = int(x - xOffset)
    newY = int(y - yOffset)
    
    # Need to check if any part of our region is outside the image. (assume that SOME of the region is within the image!)
    sizeX = image.getSizeX()
    sizeY = image.getSizeY()
    left_xs, right_xs, top_xs, bottom_xs = 0,0,0,0
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
    jpeg_data = image.renderJpegRegion(theZ,theT,newX, newY, newW, newH,level=None, compression=compress_quality)
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
    resizeH = current_h * factor
    img = img.resize((MAX_WIDTH, resizeH))
    
    draw = ImageDraw.Draw(img)
    if shape['type'] == 'Rectangle':
        rectX = int(xOffset * factor)
        rectY = int(yOffset * factor)
        rectW = int((w+xOffset) * factor)
        rectH = int((h+yOffset) * factor)
        draw.rectangle((rectX, rectY, rectW, rectH), outline=lineColour)
        draw.rectangle((rectX-1, rectY-1, rectW+1, rectH+1), outline=lineColour)    # hack to get line width of 2
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
        draw.ellipse((rectX-1, rectY-1, rectW+1, rectH+1), outline=lineColour) # hack to get line width of 2
    elif shape['type'] == 'Point':
        point_radius = 2
        rectX = (MAX_WIDTH/2) - point_radius
        rectY = int(resizeH/2) - point_radius
        rectW = rectX + (point_radius * 2)
        rectH = rectY + (point_radius * 2)
        draw.ellipse((rectX, rectY, rectW, rectH), outline=lineColour)
        draw.ellipse((rectX-1, rectY-1, rectW+1, rectH+1), outline=lineColour) # hack to get line width of 2
    elif 'xyList' in shape:
        #resizedXY = [ (int(x*factor), int(y*factor)) for (x,y) in shape['xyList'] ]
        def resizeXY(xy):
            x,y = xy
            return (int((x-newX + left_xs)*factor), int((y-newY + top_xs)*factor))
        resizedXY = [ resizeXY(xy) for xy in shape['xyList'] ]
        #draw.polygon(resizedXY, outline=lineColour)    # doesn't support 'width' of line
        x2 = y2 = None
        for l in range(1, len(resizedXY)):
            x1, y1 = resizedXY[l-1]
            x2, y2 = resizedXY[l]
            draw.line((x1, y1, x2, y2), fill=lineColour, width=2)
        start_x, start_y = resizedXY[0]
        if shape['type'] != 'PolyLine':
            if x2 is None:          # Seems possible to have Polygon with only 1 point!
                x2 = start_x + 1    # This will create a visible dot
            if y2 is None:
                y2 = start_y + 1
            draw.line((x2, y2, start_x, start_y), fill=lineColour, width=2)
        
    rv = StringIO()
    compression = 0.9
    img.save(rv, 'jpeg', quality=int(compression*100))
    jpeg = rv.getvalue()
    
    return HttpResponse(jpeg, mimetype='image/jpeg')


def _get_signature_from_request (request):
    """
    returns a string that identifies this image, along with the settings passed on the request.
    Useful for using as img identifier key, for prepared image.
    
    @param request: http request
    @return:        String
    """
    
    r = request.REQUEST
    rv = r.get('m','_') + r.get('p','_')+r.get('c','_')+r.get('q', '_')
    return rv

def _get_prepared_image (request, iid, server_id=None, conn=None, saveDefs=False, retry=True):
    """
    Fetches the Image object for image 'iid' and prepares it according to the request query, setting the channels,
    rendering model and projection arguments. The compression level is parsed and returned too.
    For parameters in request, see L{getImgDetailsFromReq}
    
    @param request:     http request
    @param iid:         Image ID
    @param conn:        L{omero.gateway.BlitzGateway} connection
    @param saveDefs:    Try to save the rendering settings, default z and t. 
    @param retry:       Try an extra attempt at this method
    @return:            Tuple (L{omero.gateway.ImageWrapper} image, quality)
    """
    r = request.REQUEST
    logger.debug('Preparing Image:%r saveDefs=%r ' \
                 'retry=%r request=%r conn=%s' % (iid, saveDefs, retry,
                 r, str(conn)))
    img = conn.getObject("Image", iid)
    if img is None:
        return
    if r.has_key('c'):
        logger.debug("c="+r['c'])
        channels, windows, colors =  _split_channel_info(r['c'])
        if not img.setActiveChannels(channels, windows, colors):
            logger.debug("Something bad happened while setting the active channels...")
    if r.get('m', None) == 'g':
        img.setGreyscaleRenderingModel()
    elif r.get('m', None) == 'c':
        img.setColorRenderingModel()
    img.setProjection(r.get('p', None))
    img.setInvertedAxis(bool(r.get('ia', "0") == "1"))
    compress_quality = r.get('q', None)
    if saveDefs:
        r.has_key('z') and img._re.setDefaultZ(long(r['z'])-1)
        r.has_key('t') and img._re.setDefaultT(long(r['t'])-1)
        img.saveDefaults()
    return (img, compress_quality)

@login_required()
def render_image_region(request, iid, z, t, conn=None, **kwargs):
    """
    Returns a jpeg of the OMERO image, rendering only a region specified in query string as
    region=x,y,width,height. E.g. region=0,512,256,256 
    Rendering settings can be specified in the request parameters.
    
    @param request:     http request
    @param iid:         image ID
    @param z:           Z index
    @param t:           T index
    @param conn:        L{omero.gateway.BlitzGateway} connection
    @return:            http response wrapping jpeg
    """
    server_id = request.session['connector'].server_id
    # if the region=x,y,w,h is not parsed correctly to give 4 ints then we simply provide whole image plane. 
    # alternatively, could return a 404?    
    #if h == None:
    #    return render_image (request, iid, z, t, server_id=None, _conn=None, **kwargs)
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
            tiles = img._re.requiresPixelsPyramid()
            w, h = img._re.getTileSize()
            levels = img._re.getResolutionLevels()-1
            
            zxyt = tile.split(",")
            
            #w = int(zxyt[3])
            #h = int(zxyt[4])
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
        jpeg_data = img.renderJpegRegion(z,t,x,y,w,h,level=level, compression=compress_quality)
        if jpeg_data is None:
            raise Http404
        webgateway_cache.setImage(request, server_id, img, z, t, jpeg_data)
    rsp = HttpResponse(jpeg_data, mimetype='image/jpeg')
    return rsp    
    
@login_required()
def render_image (request, iid, z=None, t=None, conn=None, **kwargs):
    """ 
    Renders the image with id {{iid}} at {{z}} and {{t}} as jpeg.
    Many options are available from the request dict. See L{getImgDetailsFromReq} for list.
    I am assuming a single Pixels object on image with image-Id='iid'. May be wrong
    
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
        jpeg_data = img.renderJpeg(z,t, compression=compress_quality)
        if jpeg_data is None:
            raise Http404
        webgateway_cache.setImage(request, server_id, img, z, t, jpeg_data)

    rsp = HttpResponse(jpeg_data, mimetype='image/jpeg')
    if 'download' in kwargs and kwargs['download']:
        rsp['Content-Type'] = 'application/force-download'
        rsp['Content-Length'] = len(jpeg_data)
        rsp['Content-Disposition'] = 'attachment; filename=%s.jpg' % (img.getName().replace(" ","_"))
    return rsp

@login_required()
def render_ome_tiff (request, ctx, cid, conn=None, **kwargs):
    """
    Renders the OME-TIFF representation of the image(s) with id cid in ctx (i)mage,
    (d)ataset, or (p)roject.
    For multiple images export, images that require pixels pyramid (big images) will be silently skipped.
    If exporting a single big image or if all images in a multple image export are big,
    a 404 will be triggered.
    A request parameter dryrun can be passed to return the count of images that would actually be exported.
    
    @param request:     http request
    @param ctx:         'p' or 'd' or 'i'
    @param cid:         Project, Dataset or Image ID
    @param conn:        L{omero.gateway.BlitzGateway} connection
    @return:            http response wrapping the tiff (or zip for multiple files), or redirect to temp file/zip
                        if dryrun is True, returns count of images that would be exported
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
        selection = filter(None, request.REQUEST.get('selection', '').split(','))
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
        coord = "%s%s" % (plate.getRowLabels()[obj.row],plate.getColumnLabels()[obj.column])
        name = '%s-%s-%s' % (plate.getParent().getName(), plate.getName(), coord)
    else:
        obj = conn.getObject("Image", cid)
        if obj is None:
            raise Http404
        imgs.append(obj)

    imgs = filter(lambda x: not x.requiresPixelsPyramid(), imgs)

    if request.REQUEST.get('dryrun', False):
        rv = simplejson.dumps(len(imgs))
        c = request.REQUEST.get('callback', None)
        if c is not None and not kwargs.get('_internal', False):
            rv = '%s(%s)' % (c, rv)
        return HttpResponse(rv, mimetype='application/javascript')
    
    if len(imgs) == 0:
        raise Http404
    if len(imgs) == 1:
        obj = imgs[0]
        key = '_'.join((str(x.getId()) for x in obj.getAncestry())) + '_' + str(obj.getId()) + '_ome_tiff'
        fnamemax = 255 - len(str(obj.getId())) - 10 # total name len <= 255, 9 is for .ome.tiff
        objname = obj.getName()[:fnamemax]
        fpath, rpath, fobj = webgateway_tempfile.new(str(obj.getId()) + '-'+ objname + '.ome.tiff', key=key)
        if fobj is True:
            # already exists
            return HttpResponseRedirect(settings.STATIC_URL + 'webgateway/tfiles/' + rpath)
        tiff_data = webgateway_cache.getOmeTiffImage(request, server_id, imgs[0])
        if tiff_data is None:
            try:
                tiff_data = imgs[0].exportOmeTiff()
            except:
                logger.debug('Failed to export image (2)', exc_info=True)
                tiff_data = None
            if tiff_data is None:
                webgateway_tempfile.abort(fpath)
                raise Http404
            webgateway_cache.setOmeTiffImage(request, server_id, imgs[0], tiff_data)
        if fobj is None:
            rsp = HttpResponse(tiff_data, mimetype='image/tiff')
            rsp['Content-Disposition'] = 'attachment; filename="%s.ome.tiff"' % (str(obj.getId()) + '-'+objname)
            rsp['Content-Length'] = len(tiff_data)
            return rsp
        else:
            fobj.write(tiff_data)
            fobj.close()
            return HttpResponseRedirect(settings.STATIC_URL + 'webgateway/tfiles/' + rpath)
    else:
        try:
            img_ids = '+'.join((str(x.getId()) for x in imgs))
            key = '_'.join((str(x.getId()) for x in imgs[0].getAncestry())) + '_' + md5(img_ids).hexdigest() + '_ome_tiff_zip'
            fpath, rpath, fobj = webgateway_tempfile.new(name + '.zip', key=key)
            if fobj is True:
                return HttpResponseRedirect(settings.STATIC_URL + 'webgateway/tfiles/' + rpath)
            logger.debug(fpath)
            if fobj is None:
                fobj = StringIO()
            zobj = zipfile.ZipFile(fobj, 'w', zipfile.ZIP_STORED)
            for obj in imgs:
                tiff_data = webgateway_cache.getOmeTiffImage(request, server_id, obj)
                if tiff_data is None:
                    tiff_data = obj.exportOmeTiff()
                    if tiff_data is None:
                        continue
                    webgateway_cache.setOmeTiffImage(request, server_id, obj, tiff_data)
                # While ZIP itself doesn't have the 255 char limit for filenames, the FS where these
                # get unarchived might, so trim names
                fnamemax = 255 - len(str(obj.getId())) - 10 # total name len <= 255, 9 is for .ome.tiff
                objname = obj.getName()[:fnamemax]
                zobj.writestr(str(obj.getId()) + '-'+objname + '.ome.tiff', tiff_data)
            zobj.close()
            if fpath is None:
                zip_data = fobj.getvalue()
                rsp = HttpResponse(zip_data, mimetype='application/zip')
                rsp['Content-Disposition'] = 'attachment; filename="%s.zip"' % name
                rsp['Content-Length'] = len(zip_data)
                return rsp
        except:
            logger.debug(traceback.format_exc())
            raise
        return HttpResponseRedirect(settings.STATIC_URL + 'webgateway/tfiles/' + rpath)

@login_required()
def render_movie (request, iid, axis, pos, conn=None, **kwargs):
    """ 
    Renders a movie from the image with id iid
    
    @param request:     http request
    @param iid:         Image ID
    @param axis:        Movie frames are along 'z' or 't' dimension. String
    @param pos:         The T index (for z axis) or Z index (for t axis)
    @param conn:        L{omero.gateway.BlitzGateway} connection
    @return:            http response wrapping the file, or redirect to temp file
    """
    server_id = request.session['connector'].server_id
    try:
        # Prepare a filename we'll use for temp cache, and check if file is already there
        opts = {}
        opts['format'] = 'video/' + request.REQUEST.get('format', 'quicktime')
        opts['fps'] = int(request.REQUEST.get('fps', 4))
        opts['minsize'] = (512,512, 'Black')
        ext = '.avi'
        key = "%s-%s-%s-%d-%s-%s" % (iid, axis, pos, opts['fps'], _get_signature_from_request(request),
                                  request.REQUEST.get('format', 'quicktime'))
        
        pos = int(pos)
        pi = _get_prepared_image(request, iid, server_id=server_id, conn=conn)
        if pi is None:
            raise Http404
        img, compress_quality = pi

        fpath, rpath, fobj = webgateway_tempfile.new(img.getName() + ext, key=key)
        logger.debug(fpath, rpath, fobj)
        if fobj is True:
            return HttpResponseRedirect(settings.STATIC_URL + 'webgateway/tfiles/' + rpath)#os.path.join(rpath, img.getName() + ext))

        if kwargs.has_key('optsCB'):
            opts.update(kwargs['optsCB'](img))
        opts.update(kwargs.get('opts', {}))
        logger.debug('rendering movie for img %s with axis %s, pos %i and opts %s' % (iid, axis, pos, opts))
        #fpath, rpath = webgateway_tempfile.newdir()
        if fpath is None:
            import tempfile
            fo, fn = tempfile.mkstemp()
        else:
            fn = fpath #os.path.join(fpath, img.getName())
        if axis.lower() == 'z':
            dext, mimetype = img.createMovie(fn, 0, img.getSizeZ()-1, pos-1, pos-1, opts)
        else:
            dext, mimetype = img.createMovie(fn, pos-1, pos-1, 0, img.getSizeT()-1, opts)
        if dext is None and mimetype is None:
            # createMovie is currently only available on 4.1_custom
            # http://trac.openmicroscopy.org.uk/ome/ticket/3857
            raise Http404
        if fpath is None:
            movie = open(fn).read()
            os.close(fo)
            rsp = HttpResponse(movie, mimetype=mimetype)
            rsp['Content-Disposition'] = 'attachment; filename="%s"' % (img.getName()+ext)
            rsp['Content-Length'] = len(movie)
            return rsp
        else:
            fobj.close()
            #shutil.move(fn, fn + ext)
            return HttpResponseRedirect(settings.STATIC_URL + 'webgateway/tfiles/' + rpath)#os.path.join(rpath, img.getName() + ext))
    except:
        logger.debug(traceback.format_exc())
        raise
        
@login_required()
def render_split_channel (request, iid, z, t, conn=None, **kwargs):
    """
    Renders a split channel view of the image with id {{iid}} at {{z}} and {{t}} as jpeg.
    Many options are available from the request dict.
    Requires PIL to be installed on the server. 
    
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
    jpeg_data = webgateway_cache.getSplitChannelImage(request, server_id, img, z, t)
    if jpeg_data is None:
        jpeg_data = img.renderSplitChannel(z,t, compression=compress_quality)
        if jpeg_data is None:
            raise Http404
        webgateway_cache.setSplitChannelImage(request, server_id, img, z, t, jpeg_data)
    rsp = HttpResponse(jpeg_data, mimetype='image/jpeg')
    return rsp

def debug (f):
    """
    Decorator for adding debugging functionality to methods.
    
    @param f:       The function to wrap
    @return:        The wrapped function
    """
    
    def wrap (request, *args, **kwargs):
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

def jsonp (f):
    """
    Decorator for adding connection debugging and returning function result as json, depending on 
    values in kwargs
    
    @param f:       The function to wrap
    @return:        The wrapped function, which will return json 
    """
    
    def wrap (request, *args, **kwargs):
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
            rv = simplejson.dumps(rv)
            c = request.REQUEST.get('callback', None)
            if c is not None and not kwargs.get('_internal', False):
                rv = '%s(%s)' % (c, rv)
            if kwargs.get('_internal', False):
                return rv
            return HttpResponse(rv, mimetype='application/javascript')
        except omero.ServerError:
            if kwargs.get('_raw', False) or kwargs.get('_internal', False):
                raise
            return HttpResponseServerError('("error in call","%s")' % traceback.format_exc(), mimetype='application/javascript')
        except:
            logger.debug(traceback.format_exc())
            if kwargs.get('_raw', False) or kwargs.get('_internal', False):
                raise
            return HttpResponseServerError('("error in call","%s")' % traceback.format_exc(), mimetype='application/javascript')
    wrap.func_name = f.func_name
    return wrap

@debug
@login_required()
def render_row_plot (request, iid, z, t, y, conn=None, w=1, **kwargs):
    """
    Renders the line plot for the image with id {{iid}} at {{z}} and {{t}} as gif with transparent background.
    Many options are available from the request dict.
    I am assuming a single Pixels object on image with Image ID='iid'. May be wrong 
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
        gif_data = img.renderRowLinePlotGif(int(z),int(t),int(y), int(w))
    except:
        logger.debug('a', exc_info=True)
        raise
    if gif_data is None:
        raise Http404
    rsp = HttpResponse(gif_data, mimetype='image/gif')
    return rsp

@debug
@login_required()
def render_col_plot (request, iid, z, t, x, w=1, conn=None, **kwargs):
    """ 
    Renders the line plot for the image with id {{iid}} at {{z}} and {{t}} as gif with transparent background.
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
    gif_data = img.renderColLinePlotGif(int(z),int(t),int(x), int(w))
    if gif_data is None:
        raise Http404
    rsp = HttpResponse(gif_data, mimetype='image/gif')
    return rsp
 
@login_required()
@jsonp
def imageData_json (request, conn=None, _internal=False, **kwargs):
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
        return HttpResponseServerError('""', mimetype='application/javascript')
    rv = imageMarshal(image, key)
    return rv

@login_required()
@jsonp
def wellData_json (request, conn=None, _internal=False, **kwargs):
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
        return HttpResponseServerError('""', mimetype='application/javascript')
    prefix = kwargs.get('thumbprefix', 'webgateway.views.render_thumbnail')
    def urlprefix(iid):
        return reverse(prefix, args=(iid,))
    xtra = {'thumbUrlPrefix': kwargs.get('urlprefix', urlprefix)}
    rv = well.simpleMarshal(xtra=xtra)
    return rv

@login_required()
@jsonp
def plateGrid_json (request, pid, field=0, conn=None, **kwargs):
    """
    """
    plate = conn.getObject('plate', long(pid))
    try:
        field = long(field or 0)
    except ValueError:
        field = 0
    if plate is None:
        return HttpResponseServerError('""', mimetype='application/javascript')
    grid = []
    prefix = kwargs.get('thumbprefix', 'webgateway.views.render_thumbnail')
    thumbsize = int(request.REQUEST.get('size', 64))
    logger.debug(thumbsize)

    def urlprefix(iid):
        return reverse(prefix, args=(iid,thumbsize))
    xtra = {'thumbUrlPrefix': kwargs.get('urlprefix', urlprefix)}
    server_id = kwargs['server_id']

    rv = webgateway_cache.getJson(request, server_id, plate, 'plategrid-%d-%d' % (field, thumbsize))
    if rv is None:
        plate.setGridSizeConstraints(8,12)
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
        webgateway_cache.setJson(request, server_id, plate, simplejson.dumps(rv), 'plategrid-%d-%d' % (field, thumbsize))
    else:
        rv = simplejson.loads(rv)
    return rv

@login_required()
@jsonp
def listImages_json (request, did, conn=None, **kwargs):
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
        return HttpResponseServerError('""', mimetype='application/javascript')
    prefix = kwargs.get('thumbprefix', 'webgateway.views.render_thumbnail')
    def urlprefix(iid):
        return reverse(prefix, args=(iid,))
    xtra = {'thumbUrlPrefix': kwargs.get('urlprefix', urlprefix),
            'tiled': request.REQUEST.get('tiled', False),}
    return map(lambda x: x.simpleMarshal(xtra=xtra), dataset.listChildren())

@login_required()
@jsonp
def listWellImages_json (request, did, conn=None, **kwargs):
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
        return HttpResponseServerError('""', mimetype='application/javascript')
    prefix = kwargs.get('thumbprefix', 'webgateway.views.render_thumbnail')
    def urlprefix(iid):
        return reverse(prefix, args=(iid,))
    xtra = {'thumbUrlPrefix': kwargs.get('urlprefix', urlprefix)}
    return map(lambda x: x.getImage() and x.getImage().simpleMarshal(xtra=xtra), well.listChildren())

@login_required()
@jsonp
def listDatasets_json (request, pid, conn=None, **kwargs):
    """
    lists all Datasets in a Project, as json
    TODO: cache
    
    @param request:     http request
    @param pid:         Project ID
    @param conn:        L{omero.gateway.BlitzGateway}
    @return:            list of dataset json.
    """
    
    project = conn.getObject("Project", pid)
    rv = []
    if project is None:
        return HttpResponse('[]', mimetype='application/javascript')
    return [x.simpleMarshal(xtra={'childCount':0}) for x in project.listChildren()]

@login_required()
@jsonp
def datasetDetail_json (request, did, conn=None, **kwargs):
    """
    return json encoded details for a dataset
    TODO: cache
    """
    ds = conn.getObject("Dataset", did)
    return ds.simpleMarshal()

@login_required()
@jsonp
def listProjects_json (request, conn=None, **kwargs):
    """
    lists all Projects, as json
    TODO: cache
    
    @param request:     http request
    @param conn:        L{omero.gateway.BlitzGateway}
    @return:            list of project json.
    """
    
    rv = []
    for pr in conn.listProjects():
        rv.append( {'id': pr.id, 'name': pr.name, 'description': pr.description or ''} )
    return rv

@login_required()
@jsonp
def projectDetail_json (request, pid, conn=None, **kwargs):
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

def searchOptFromRequest (request):
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
def search_json (request, conn=None, **kwargs):
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
    opts = searchOptFromRequest(request)
    rv = []
    logger.debug("searchObjects(%s)" % (opts['search']))
    # search returns blitz_connector wrapper objects
    def urlprefix(iid):
        return reverse('webgateway.views.render_thumbnail', args=(iid,))
    xtra = {'thumbUrlPrefix': kwargs.get('urlprefix', urlprefix)}
    pks = None
    try:
        if opts['ctx'] == 'imgs':
            sr = conn.searchObjects(["image"], opts['search'], conn.SERVICE_OPTS)
        else:
            sr = conn.searchObjects(None, opts['search'], conn.SERVICE_OPTS)  # searches P/D/I
    except ApiUsageException:
        return HttpResponseServerError('"parse exception"', mimetype='application/javascript')
    def marshal ():
        rv = []
        if (opts['grabData'] and opts['ctx'] == 'imgs'):
            bottom = min(opts['start'], len(sr)-1)
            if opts['limit'] == 0:
                top = len(sr)
            else:
                top = min(len(sr), bottom + opts['limit'])
            for i in range(bottom, top):
                e = sr[i]
            #for e in sr:
                try:
                    rv.append(imageData_json(request, server_id, iid=e.id, key=opts['key'], conn=conn, _internal=True))
                except AttributeError, x:
                    logger.debug('(iid %i) ignoring Attribute Error: %s' % (e.id, str(x)))
                    pass
                except omero.ServerError, x:
                    logger.debug('(iid %i) ignoring Server Error: %s' % (e.id, str(x)))
            return rv
        else:
            return map(lambda x: x.simpleMarshal(xtra=xtra, parents=opts['parents']), sr)
    rv = timeit(marshal)()
    logger.debug(rv)
    return rv

@login_required()
def save_image_rdef_json (request, iid, conn=None, **kwargs):
    """
    Requests that the rendering defs passed in the request be set as the default for this image.
    Rendering defs in request listed at L{getImgDetailsFromReq}
    TODO: jsonp
    
    @param request:     http request
    @param iid:         Image ID
    @param conn:        L{omero.gateway.BlitzGateway}
    @return:            http response 'true' or 'false'
    """
    server_id = request.session['connector'].server_id
    r = request.REQUEST
    pi = _get_prepared_image(request, iid, server_id=server_id, conn=conn, saveDefs=True)
    if pi is None:
        json_data = 'false'
    else:
        user_id = pi[0]._conn.getEventContext().userId
        webgateway_cache.invalidateObject(server_id, user_id, pi[0])
        pi[0].getThumbnail()
        json_data = 'true'
    if r.get('callback', None):
        json_data = '%s(%s)' % (r['callback'], json_data)
    return HttpResponse(json_data, mimetype='application/javascript')

@login_required()
def list_compatible_imgs_json (request, iid, conn=None, **kwargs):
    """
    Lists the images on the same project that would be viable targets for copying rendering settings.
    TODO: change method to:
    list_compatible_imgs_json (request, iid, server_id=None, conn=None, **kwargs):
    
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
        def compat (i):
            if long(i.getId()) == long(iid):
                return False
            pp = i.getPrimaryPixels()
            if pp is None or \
               i.getPrimaryPixels().getPixelsType().getValue() != img_ptype or \
               i.getSizeC() != img_ccount:
                return False
            ew = [x.getLabel() for x in i.getChannels()]
            ew.sort()
            if ew != img_ew:
                return False
            return True
        imgs = filter(compat, imgs)
        json_data = simplejson.dumps([x.getId() for x in imgs])

    if r.get('callback', None):
        json_data = '%s(%s)' % (r['callback'], json_data)
    return HttpResponse(json_data, mimetype='application/javascript')

@login_required()
@jsonp
def copy_image_rdef_json (request, conn=None, **kwargs):
    """
    Copy the rendering settings from one image to a list of images.
    Images are specified in request by 'fromid' and list of 'toids'
    Returns json dict of Boolean:[Image-IDs] for images that have successfully 
    had the rendering settings applied, or not. 
    
    @param request:     http request
    @param server_id:   
    @param conn:        L{omero.gateway.BlitzGateway}
    @return:            json dict of Boolean:[Image-IDs]
    """
    
    server_id = request.session['connector'].server_id
    json_data = False
    r = request.REQUEST
    try:
        fromid = long(r.get('fromid', None))
        toids = map(lambda x: long(x), r.getlist('toids'))
    except TypeError:
        fromid = None
    except ValueError:
        fromid = None
    if fromid is not None and len(toids) > 0:
        
        fromimg = conn.getObject("Image", fromid)
        frompid = fromimg.getPixelsId()
        userid = fromimg.getOwner().getId()
        if fromimg.canWrite():
            ctx = conn.SERVICE_OPTS.copy()
            ctx.setOmeroGroup(fromimg.getDetails().getGroup().getId())
            ctx.setOmeroUser(userid)
            rsettings = conn.getRenderingSettingsService()
            json_data = rsettings.applySettingsToImages(frompid, list(toids), ctx)
            if fromid in json_data[True]:
                del json_data[True][json_data[True].index(fromid)]
            for iid in json_data[True]:
                img = conn.getObject("Image", iid)
                img is not None and webgateway_cache.invalidateObject(server_id, userid, img)
    return json_data
#
#            json_data = simplejson.dumps(json_data)
#
#    if r.get('callback', None):
#        json_data = '%s(%s)' % (r['callback'], json_data)
#    return HttpResponse(json_data, mimetype='application/javascript')

@login_required()
@jsonp
def reset_image_rdef_json (request, iid, conn=None, **kwargs):
    """
    Try to remove all rendering defs the logged in user has for this image.
    
    @param request:     http request
    @param iid:         Image ID
    @param conn:        L{omero.gateway.BlitzGateway}
    @return:            json 'true', or 'false' if failed
    """

    img = conn.getObject("Image", iid)

    if img is not None and img.resetRDefs():
        user_id = conn.getEventContext().userId
        server_id = request.session['connector'].server_id
        webgateway_cache.invalidateObject(server_id, user_id, img)
        return True
        json_data = 'true'
    else:
        json_data = 'false'
        return False
#    if _conn is not None:
#        return json_data == 'true'      # TODO: really return a boolean? (not json)
#    if r.get('callback', None):
#        json_data = '%s(%s)' % (r['callback'], json_data)
#    return HttpResponse(json_data, mimetype='application/javascript')

@login_required()
def full_viewer (request, iid, conn=None, **kwargs):
    """
    This view is responsible for showing the omero_image template
    Image rendering options in request are used in the display page. See L{getImgDetailsFromReq}.
    
    @param request:     http request.
    @param iid:         Image ID
    @param conn:        L{omero.gateway.BlitzGateway}
    @param **kwargs:    Can be used to specify the html 'template' for rendering
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
             'roiCount': image.getROICount(),
             'viewport_server': kwargs.get('viewport_server', '/webgateway'),
             'object': 'image:%i' % int(iid)}

        template = kwargs.get('template', "webgateway/viewport/omero_image.html")
        t = template_loader.get_template(template)
        c = Context(request,d)
        rsp = t.render(c)
    except omero.SecurityViolation:
        raise Http404
    return HttpResponse(rsp)


@login_required()
def get_shape_json(request, roiId, shapeId, conn=None, **kwargs):
    roiId = int(roiId)
    shapeId = int(shapeId)
    shape = conn.getQueryService().findByQuery(
            'select shape from Roi as roi ' \
            'join roi.shapes as shape ' \
            'where roi.id = %d and shape.id = %d' % (roiId, shapeId),
            None)
    logger.debug('Shape: %r' % shape)
    if shape is None:
        logger.debug('No such shape: %r' % shapeId)
        raise Http404
    return HttpResponse(simplejson.dumps(shapeMarshal(shape)),
            mimetype='application/javascript')

@login_required()
def get_rois_json(request, imageId, conn=None, **kwargs):
    """
    Returns json data of the ROIs in the specified image. 
    """
    rois = []
    roiService = conn.getRoiService()
    #rois = webfigure_utils.getRoiShapes(roiService, long(imageId))  # gets a whole json list of ROIs
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
        shapes.sort(key=lambda x:
                "%03d%03d"% (x.get('theZ', -1), x.get('theT', -1)));
        roi['shapes'] = shapes
        rois.append(roi)
        
    rois.sort(key=lambda x: x['id']) # sort by ID - same as in measurement tool.
    
    return HttpResponse(simplejson.dumps(rois), mimetype='application/javascript')
    

def test (request):
    """
    Tests the L{full_viewer} with no args passed to the template. 
    
    @param request:     http request.
    @return:            blank page template
    """
    
    context = {}

    t = template_loader.get_template('webgateway/viewport/omero_image.html')
    c = Context(request,context)
    return HttpResponse(t.render(c))

@login_required(isAdmin=True)
@jsonp
def su (request, user, conn=None, **kwargs):
    """
    If current user is admin, switch the session to a new connection owned by 'user'
    (puts the new session ID in the request.session)
    Return False if not possible
    
    @param request:     http request.
    @param user:        Username of new connection owner
    @param conn:        L{omero.gateway.BlitzGateway}
    @param **kwargs:    Can be used to specify the html 'template' for rendering
    @return:            Boolean
    """
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


@login_required()
@jsonp
def repositories(request, conn=None, **kwargs):
    """
    Returns a list of repositories and their indices
    """
    sr = conn.getSharedResources()
    repositories = sr.repositories()
    result = []
    for index, description in enumerate(repositories.descriptions):
        result.append(dict(index=index,
                           repository=OriginalFileWrapper(conn=conn, obj=description).simpleMarshal()))
    return result


@login_required()
@jsonp
def repository(request, index, conn=None, **kwargs):
    """
    Returns a repository and its root property
    """
    sr = conn.getSharedResources()
    repositories = sr.repositories()
    repository = repositories.proxies[int(index)]
    description = repositories.descriptions[int(index)]
    return dict(repository=OriginalFileWrapper(conn=conn, obj=description).simpleMarshal(),
                root=unwrap(repository.root().path))


@login_required()
@jsonp
def repository_list(request, index, filepath=None, conn=None, **kwargs):
    """
    Returns a list of files in a repository.  If filepath is not specified,
    returns files at the top level of the repository, otherwise files within
    the specified filepath
    """
    sr = conn.getSharedResources()
    repositories = sr.repositories()
    repository = repositories.proxies[int(index)]
    description = repositories.descriptions[int(index)]
    name = OriginalFileWrapper(conn=conn, obj=description).getName()
    root = os.path.join(unwrap(repository.root().path), name)
    if filepath:
        root = os.path.join(root, filepath)
    if repository.fileExists(root):
        result = [f for f in repository.list(root) if not f.startswith('.')]
    else:
        result = []
    return dict(result=result)


@login_required()
@jsonp
def repository_listfiles(request, index, filepath=None, conn=None, **kwargs):
    """
    Returns a list of files and some of their metadata in a repository.
    If filepath is not specified, returns files at the top level of the
    repository, otherwise files within the specified filepath
    """
    sr = conn.getSharedResources()
    repositories = sr.repositories()
    repository = repositories.proxies[int(index)]
    description = repositories.descriptions[int(index)]
    name = OriginalFileWrapper(conn=conn, obj=description).getName()
    root = os.path.join(unwrap(repository.root().path), name)
    if filepath:
        root = os.path.join(root, filepath)

    def _getFile(f):
        w = OriginalFileWrapper(conn=conn, obj=f)
        return w.simpleMarshal()

    if repository.fileExists(root):
        result = [_getFile(f) for f in repository.listFiles(root)]
        result = [f for f in result if not f.get('name', '').startswith('.')]
    else:
        result = []
    return dict(result=result)


@login_required()
@jsonp
def repository_sha(request, index, filepath, conn=None, **kwargs):
    """
    json method: Returns the sha1 checksum of the specified file
    """
    sr = conn.getSharedResources()
    repositories = sr.repositories()
    repository = repositories.proxies[int(index)]
    description = repositories.descriptions[int(index)]
    name = OriginalFileWrapper(conn=conn, obj=description).getName()
    fullpath = os.path.join(unwrap(repository.root().path), name, filepath)

    try:
        sourcefile = repository.file(fullpath, 'r')
    except InternalException:
        raise Http404()

    digest = sha()
    for block in iterate_content(sourcefile, 0, sourcefile.size() - 1):
        digest.update(block)

    return dict(sha=digest.hexdigest())



@login_required()
@jsonp
def repository_root(request, index, conn=None, **kwargs):
    """
    Returns the root and name property of a repository
    """
    sr = conn.getSharedResources()
    repositories = sr.repositories()
    repository = repositories.proxies[int(index)]
    description = repositories.descriptions[int(index)]
    return dict(root=unwrap(repository.root().path),
                name=OriginalFileWrapper(conn=conn, obj=description).getName())


@require_POST
@login_required()
@jsonp
def repository_makedir(request, index, dirpath, conn=None, **kwargs):
    """
    Returns the root and name property of a repository
    """
    sr = conn.getSharedResources()
    repositories = sr.repositories()
    repository = repositories.proxies[int(index)]
    description = repositories.descriptions[int(index)]
    root = unwrap(repository.root().path)
    name = OriginalFileWrapper(conn=conn, obj=description).getName()

    try:
        rdict = {'bad': 'false'}
        repository.makeDir(os.path.join(root, name, dirpath))
    except Exception, ex:
        logger.error(traceback.format_exc())
        rdict = {'bad': 'true', 'errs': str(ex)}
    return rdict


def iterate_content(source, start, end):
    chunk_size = getattr(settings, 'MPU_CHUNK_SIZE', 64 * 1024)
    position = start
    to_read = end - start + 1
    while True:
        chunk = source.read(position, min(chunk_size, to_read))
        if not chunk:
            break
        to_read -= len(chunk)
        position += len(chunk)
        yield chunk


@login_required(doConnectionCleanup=False)
def repository_download(request, index, filepath, conn=None, **kwargs):
    """
    Downloads a file from a repository.  Supports the HTTP_RANGE header to
    perform partial downloads or download continuation
    """
    sr = conn.getSharedResources()
    repositories = sr.repositories()
    repository = repositories.proxies[int(index)]
    description = repositories.descriptions[int(index)]
    name = OriginalFileWrapper(conn=conn, obj=description).getName()
    fullpath = os.path.join(unwrap(repository.root().path), name, filepath)

    try:
        sourcefile = repository.file(fullpath, 'r')
    except InternalException:
        raise Http404()

    def parse_range(request, size):
        # See http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.35
        m = re.match(r"^bytes=(\d*)-(\d*)$", request.META.get('HTTP_RANGE', ''))
        if not m:
            return 200, 0, size - 1
        try:
            start = int(m.group(1) or 0)
            end = int(m.group(2) or (size - 1))
            if start == 0 and end == size - 1:
                return 200, start, end
            if start <= end:
                if end >= size:
                    return 416, start, end
                else:
                    return 206, start, end
        except ValueError:
            pass
        return 416, 0, size - 1

    filesize = sourcefile.size()

    code, start, end = parse_range(request, filesize)

    if code < 400:
        response = ConnCleaningHttpResponse(iterate_content(sourcefile, start, end))
        response.conn = conn
        response['Content-Type'] = 'application/octet-stream'
        response['Content-Length'] = end - start + 1

    else:
        response = HttpResponse(status=code)

    return response



def chunk_copy(source, target, start=None, end=None):
    chunk_size = getattr(settings, 'MPU_CHUNK_SIZE', 64 * 1024)
    if start > 0:
        source.seek(start)
    to_read = (end - (start or 0) + 1) if end else None
    while True:
        chunk = source.read(min(chunk_size, to_read) if to_read > -1 else chunk_size)
        if not chunk:
            break
        if to_read:
            to_read -= len(chunk)
        target.write(chunk)


def touch(fname, times=None):
    with file(fname, 'a'):
        os.utime(fname, times)

def get_temp_path(uploadId=None):
    temp_dir = getattr(settings, 'MPU_TEMP_DIR', tempfile.gettempdir())
    if not uploadId:
        return os.path.join(temp_dir, 'mpu_uploads')
    else:
        return os.path.join(temp_dir, 'mpu_uploads', uploadId)

def _delete_upload(objectname):
    shutil.rmtree(os.path.dirname(objectname), ignore_errors=True)


class login_required_no_redirect(login_required):

    def on_not_logged_in(self, request, url, error=None):
        """Called whenever the user is not logged in."""

        logger.debug("webapi: Could not log in - always 403")

        return HttpResponseForbidden()


# Use these utility decorators to use any decorator designed for classic views
# on class-based view methods.
# Example:
#
# class ViewClass(django.views.generic.View):
#     @cloak_self
#     @login_required()
#     @uncloak_self
#     def protected_view(self, request, *args, **kwargs):
#         ...

def cloak_self(function):
    def decorated(self, *args, **kwargs):
        kwargs['__self__'] = self
        return function(*args, **kwargs)
    return decorated

def uncloak_self(function):
    def decorated(*args, **kwargs):
        self = kwargs.pop('__self__', None)
        return function(self, *args, **kwargs)
    return decorated


def process_request(require_uploadId):
    def decorate_method(method):
        @cloak_self
        @login_required_no_redirect()
        @jsonp
        @uncloak_self
        def decorated(self, request, index, filepath, conn, **kwargs):

            sr = conn.getSharedResources()
            repositories = sr.repositories()
            repository = repositories.proxies[int(index)]
            description = repositories.descriptions[int(index)]
            name = OriginalFileWrapper(conn=conn, obj=description).getName()
            fullpath = os.path.join(unwrap(repository.root().path), name, filepath)

            objectname = os.path.basename(fullpath)
            uploadId = request.GET.get('uploadId')
            if uploadId:
                path = get_temp_path(uploadId)
                objectname = os.path.join(path, objectname)
                if not os.path.exists(objectname):
                    return HttpResponseForbidden()
                touch(os.path.join(path, objectname))
            elif require_uploadId:
                return HttpResponseForbidden()
            try:
                rdict = {'bad': 'false'}
                rdict.update(method(self, request, objectname, conn,
                                    fullpath=fullpath, repository=repository,
                                    **kwargs))
            except Exception, ex:
                logger.error(traceback.format_exc())
                rdict = {'bad': 'true', 'errs': str(ex)}
            return rdict
        return decorated
    return decorate_method


class repository_upload(django.views.generic.View):
    """
    Upload a file into a repository using multi-part upload.  Modeled on
    the Amazon S3 MPU calls.

    POST /filepath?uploads - Initiates the upload and returns an uploadId
    PUT /filepath?uploadId=X&partNumber=Y - uploads a file part
    GET /filepath?uploadId=X - returns a list of already uploaded parts
    POST /filepath?uploadId=X - assembles file parts and submits to repository
    DELETE /filepath?uploadId=X - abort upload process
    """

    @process_request(require_uploadId=True)
    def get(self, request, objectname, conn, **kwargs):
        rdict = {}
        if objectname:
            rdict['parts'] = self._get_parts(objectname)
        return rdict

    @process_request(require_uploadId=False)
    def post(self, request, objectname, conn, fullpath, repository, **kwargs):
        rdict = {}
        if request.GET.has_key('uploads'):
            rdict['uploadId'] = self._initiate_upload(objectname)
        else:
            self._complete_upload(objectname, fullpath, repository)
        return rdict

    @process_request(require_uploadId=True)
    def put(self, request, objectname, conn, **kwargs):
        rdict = {}
        try:
            partNumber = int(request.GET.get('partNumber'))
        except ValueError:
            partNumber = 0
        if partNumber < 1 or partNumber > 10000:
            raise ValueError('Part number must be between 1 and 10000')
        with file('%s.%05d' % (objectname, partNumber), 'wb') as part:
            chunk_copy(request, part)
        return rdict

    @process_request(require_uploadId=True)
    def delete(self, request, objectname, conn, **kwargs):
        rdict = {}
        self._delete_upload(objectname)
        return rdict

    def _initiate_upload(self, objectname):
        uploadId = str(uuid.uuid4())
        path = get_temp_path(uploadId)
        os.makedirs(path)
        touch(os.path.join(path, objectname))
        return uploadId

    def _get_parts(self, objectname):
        filename = os.path.basename(objectname) + '.'
        return sorted(part for part in os.listdir(os.path.dirname(objectname))
                      if part.startswith(filename))

    def _complete_upload(self, objectname, fullpath, repository):
        parts = self._get_parts(objectname)
        if len(parts) < 1 or len(parts) != int(parts[-1][-5:]):
            raise Exception("Missing parts in multi-part upload")

        def chunk_copy_to_repo(source, target, position):
            chunk_size = getattr(settings, 'MPU_CHUNK_SIZE', 64 * 1024)
            while True:
                chunk = source.read(chunk_size)
                if not chunk:
                    break
                target.write(chunk, position, len(chunk))
                position += len(chunk)
            return position

        targetfile = repository.file(fullpath, 'rw')
        targetfile.truncate(0)
        position = 0
        for part in parts:
            with file(os.path.join(os.path.dirname(objectname), part)) as source:
                position = chunk_copy_to_repo(source, targetfile, position)

        _delete_upload(objectname)


@require_POST
@login_required()
@jsonp
def clean_incomplete_mpus(request, conn, **kwargs):
    """
    Remove incomplete multi-part uploads that have not been active in a
    certain timeout period.  Returns a list of all uploads before any
    are removed.
    """
    now = time.time()
    rdict = dict()
    path = get_temp_path()
    for upload in os.listdir(path):
        p = os.path.join(path, upload)
        last_change = None
        master = None
        if os.path.isdir(p):
            files = sorted(os.listdir(p), key=len)
            if files:
                master = os.path.join(p, files[0])
                last_change = now - os.path.getatime(master)
        rdict[upload] = last_change
        if last_change > getattr(settings, 'MPU_TIMEOUT', 60 * 60):
            _delete_upload(master)
    return rdict
