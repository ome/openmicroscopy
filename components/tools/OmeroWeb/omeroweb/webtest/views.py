#!/usr/bin/env python
# -*- coding: utf-8 -*-
from django.http import HttpResponseRedirect, HttpResponse
from django.core.urlresolvers import reverse
from django.shortcuts import render_to_response
from omeroweb.webgateway import views as webgateway_views
from omeroweb.connector import Server

from omeroweb.webclient.decorators import login_required, render_response
from omeroweb.connector import Connector

from cStringIO import StringIO

import settings
import logging
import traceback
import omero
from omero.rtypes import rint, rstring
import omero.gateway
import random


logger = logging.getLogger(__name__)    


try:
    from PIL import Image
except: #pragma: nocover
    try:
        import Image
    except:
        logger.error('No PIL installed, line plots and split channel will fail!')


@login_required()    # wrapper handles login (or redirects to webclient login). Connection passed in **kwargs
def dataset(request, datasetId, conn=None, **kwargs):
    """ 'Hello World' example from tutorial on http://trac.openmicroscopy.org.uk/ome/wiki/OmeroWeb """
    ds = conn.getObject("Dataset", datasetId)     # before OMERO 4.3 this was conn.getDataset(datasetId)
    return render_to_response('webtest/dataset.html', {'dataset': ds})    # generate html from template


@login_required()    # wrapper handles login (or redirects to webclient login). Connection passed in **kwargs
def index(request, conn=None, **kwargs):
    # use Image IDs from request...
    if request.REQUEST.get("Image", None):
        imageIds = request.REQUEST.get("Image", None)
        ids = [int(iid) for iid in imageIds.split(",")]
        images = list(conn.getObjects("Image", ids))
    else:
        # OR find a random image and dataset to display & can be used in links to other pages
        all_images = list(conn.getObjects("Image"))
        img = random.choice(all_images)
        images = [img]
    
    imgIds = ",".join([str(img.getId()) for img in images])
    
    # get a random dataset (making sure we get one that has some images in it)
    all_datasets = list(conn.getObjects("Dataset"))
    dataset = random.choice(all_datasets)
    attempts = 0
    while (dataset.countChildren() == 0 and attempts < 10):
        dataset = random.choice(all_datasets)
        attempts += 1

    return render_to_response('webtest/index.html', {'images': images, 'imgIds': imgIds, 'dataset': dataset})


@login_required()
def channel_overlay_viewer(request, imageId, conn=None, **kwargs):
    """
    Viewer for overlaying separate channels from the same image or different images
    and adjusting horizontal and vertical alignment of each
    """
    image = conn.getObject("Image", imageId)
    default_z = image.getSizeZ()/2
    
    # try to work out which channels should be 'red', 'green', 'blue' based on rendering settings
    red = None
    green = None
    blue = None
    notAssigned = []
    channels = []
    for i, c in enumerate(image.getChannels()):
        channels.append( {'name':c.getName()} )
        if c.getColor().getRGB() == (255, 0, 0) and red == None:
            red = i
        elif c.getColor().getRGB() == (0, 255, 0) and green == None:
            green = i
        elif c.getColor().getRGB() == (0, 0, 255) and blue == None:
            blue = i
        else: 
            notAssigned.append(i)
    # any not assigned - try assigning
    for i in notAssigned:
        if red == None: red = i
        elif green == None: green = i
        elif blue == None: blue = i
        
    # see if we have z, x, y offsets already annotated on this image
    # added by javascript in viewer. E.g. 0|z:1_x:0_y:0,1|z:0_x:10_y:0,2|z:0_x:0_y:0
    ns = "omero.web.channel_overlay.offsets"
    comment = image.getAnnotation(ns)
    if comment == None:     # maybe offset comment has been added manually (no ns)
        for ann in image.listAnnotations():
            if isinstance(ann, omero.gateway.CommentAnnotationWrapper):
                if ann.getValue().startswith("0|z:"):
                    comment = ann
                    break
    if comment != None:
        offsets = comment.getValue()
        for o in offsets.split(","):
            index,zxy = o.split("|",1)
            if int(index) < len(channels):
                keyVals = zxy.split("_")
                for kv in keyVals:
                    key, val = kv.split(":")
                    if key == "z": val = int(val) + default_z
                    channels[int(index)][key] = int(val)

    return render_to_response('webtest/demo_viewers/channel_overlay_viewer.html', {
        'image': image, 'channels':channels, 'default_z':default_z, 'red': red, 'green': green, 'blue': blue})


@login_required()
def render_channel_overlay (request, conn=None, **kwargs):
    """
    Overlays separate channels (red, green, blue) from the same image or different images
    manipulating each indepdently (translate, scale, rotate etc? )
    """
    # request holds info on all the planes we are working on and offset (may not all be visible)
    # planes=0|imageId:z:c:t$x:shift_y:shift_rot:etc,1|imageId...
    # E.g. planes=0|2305:7:0:0$x:-50_y:10,1|2305:7:1:0,2|2305:7:2:0&red=2&blue=0&green=1
    planes = {}
    p = request.REQUEST.get('planes', None)
    if p is None:
        return HttpResponse("Request needs plane info to render jpeg. E.g. ?planes=0|2305:7:0:0$x:-50_y:10,1|2305:7:1:0,2|2305:7:2:0&red=2&blue=0&green=1")
    for plane in p.split(','):
        infoMap = {}
        plane_info = plane.split('|')
        key = plane_info[0].strip()
        info = plane_info[1].strip()
        shift = None
        if info.find('$')>=0:
            info,shift = info.split('$')
        imageId,z,c,t = [int(i) for i in info.split(':')]
        infoMap['imageId'] = imageId
        infoMap['z'] = z
        infoMap['c'] = c
        infoMap['t'] = t
        if shift != None:
            for kv in shift.split("_"):
                k, v = kv.split(":")
                infoMap[k] = v
        planes[key] = infoMap

    # from the request we need to know which plane is blue, green, red (if any) by index
    # E.g. red=0&green=2
    red = request.REQUEST.get('red', None)
    green = request.REQUEST.get('green', None)
    blue = request.REQUEST.get('blue', None)

    # kinda like split-view: we want to get single-channel images...
    # red...
    redImg = None

    def translate(image, deltaX, deltaY):

        xsize, ysize = image.size
        mode = image.mode
        bg = Image.new(mode, image.size)
        x = abs(min(deltaX, 0))
        pasteX = max(0, deltaX)
        y = abs(min(deltaY, 0))
        pasteY = max(0, deltaY)

        part = image.crop((x, y, xsize-deltaX, ysize-deltaY))
        bg.paste(part, (pasteX, pasteY))
        return bg

    def getPlane(planeInfo):
        """ Returns the rendered plane split into a single channel (ready for merging) """
        img = conn.getObject("Image", planeInfo['imageId'])
        img.setActiveChannels((planeInfo['c']+1,))
        img.setGreyscaleRenderingModel()
        rgb = img.renderImage(planeInfo['z'], planeInfo['t'])

        # somehow this line is required to prevent an error at 'rgb.split()'
        rgb.save(StringIO(), 'jpeg', quality=90)

        r,g,b = rgb.split()  # go from RGB to L

        x,y = 0,0
        if 'x' in planeInfo:
            x = int(planeInfo['x'])
        if 'y' in planeInfo:
            y = int(planeInfo['y'])

        if x or y:
            r = translate(r, x, y)
        return r

    redChannel = None
    greenChannel = None
    blueChannel = None
    if red != None and red in planes:
        redChannel = getPlane(planes[red])
    if green != None and green in planes:
        greenChannel = getPlane(planes[green])
    if blue != None and blue in planes:
        blueChannel = getPlane(planes[blue])

    if redChannel != None:
        size = redChannel.size
    elif greenChannel != None:
        size = greenChannel.size
    elif blueChannel != None:
        size = blueChannel.size

    black = Image.new('L', size)
    redChannel = redChannel and redChannel or black
    greenChannel = greenChannel and greenChannel or black
    blueChannel = blueChannel and blueChannel or black

    merge = Image.merge("RGB", (redChannel, greenChannel, blueChannel))
    # convert from PIL back to string image data
    rv = StringIO()
    compression = 0.9
    merge.save(rv, 'jpeg', quality=int(compression*100))
    jpeg_data = rv.getvalue()

    rsp = HttpResponse(jpeg_data, mimetype='image/jpeg')
    return rsp


@login_required()
def add_annotations (request, conn=None, **kwargs):
    """
    Creates a L{omero.gateway.CommentAnnotationWrapper} and adds it to the images according 
    to variables in the http request. 
    
    @param request:     The django L{django.core.handlers.wsgi.WSGIRequest}
                            - imageIds:     A comma-delimited list of image IDs
                            - comment:      The text to add as a comment to the images
                            - ns:           Namespace for the annotation
                            - replace:      If "true", try to replace existing annotation with same ns
                            
    @return:            A simple html page with a success message 
    """
    idList = request.REQUEST.get('imageIds', None)    # comma - delimited list
    if idList:
        imageIds = [long(i) for i in idList.split(",")]
    else: imageIds = []
    
    comment = request.REQUEST.get('comment', None)
    ns = request.REQUEST.get('ns', None)
    replace = request.REQUEST.get('replace', False) in ('true', 'True')
    
    updateService = conn.getUpdateService()
    ann = omero.model.CommentAnnotationI()
    ann.setTextValue(rstring( str(comment) ))
    if ns != None:
        ann.setNs(rstring( str(ns) ))
    ann = updateService.saveAndReturnObject(ann)
    annId = ann.getId().getValue()
    
    images = []
    for iId in imageIds:
        image = conn.getObject("Image", iId)
        if image == None: continue
        if replace and ns != None:
            oldComment = image.getAnnotation(ns)
            if oldComment != None:
                oldComment.setTextValue(rstring( str(comment) ))
                updateService.saveObject(oldComment)
                continue
        l = omero.model.ImageAnnotationLinkI()
        parent = omero.model.ImageI(iId, False)     # use unloaded object to avoid update conflicts
        l.setParent(parent)
        l.setChild(ann)
        updateService.saveObject(l)
        images.append(image)
        
    return render_to_response('webtest/util/add_annotations.html', {'images':images, 'comment':comment})
    

@login_required()
def split_view_figure (request, conn=None, **kwargs):
    """
    Generates an html page displaying a number of images in a grid with channels split into different columns. 
    The page also includes a form for modifying various display parameters and re-submitting
    to regenerate this page. 
    If no 'imageIds' parameter (comma-delimited list) is found in the 'request', the page generated is simply 
    a form requesting image IDs. 
    If there are imageIds, the first ID (image) is used to generate the form based on channels of that image.
    
    @param request:     The django L{http request <django.core.handlers.wsgi.WSGIRequest>}
    
    @return:            The http response - html page displaying split view figure.  
    """
    query_string = request.META["QUERY_STRING"]
    
    
    idList = request.REQUEST.get('imageIds', None)    # comma - delimited list
    idList = request.REQUEST.get('Image', idList)    # we also support 'Image'
    if idList:
        imageIds = [long(i) for i in idList.split(",")]
    else:
        imageIds = []
    
    split_grey = request.REQUEST.get('split_grey', None)
    merged_names = request.REQUEST.get('merged_names', None)
    proj = request.REQUEST.get('proj', "normal")    # intmean, intmax, normal
    try:
        w = request.REQUEST.get('width', 0)
        width = int(w)
    except:
        width = 0
    try:
        h = request.REQUEST.get('height', 0)
        height = int(h)
    except:
        height = 0
        
    # returns a list of channel info from the image, overridden if values in request
    def getChannelData(image):
        channels = []
        i = 0;
        channel_data = image.getChannels()
        if channel_data is None:    # E.g. failed import etc
            return None
        for i, c in enumerate(channel_data):
            name = request.REQUEST.get('cName%s' % i, c.getLogicalChannel().getName())
            # if we have channel info from a form, we know that checkbox:None is unchecked (not absent)
            if request.REQUEST.get('cName%s' % i, None):
                active = (None != request.REQUEST.get('cActive%s' % i, None) )
                merged = (None != request.REQUEST.get('cMerged%s' % i, None) )
            else:
                active = True
                merged = True
            colour = c.getColor()
            if colour is None:
                return None     # rendering engine problems
            colour = colour.getHtml()
            start = request.REQUEST.get('cStart%s' % i, c.getWindowStart())
            end = request.REQUEST.get('cEnd%s' % i, c.getWindowEnd())
            render_all = (None != request.REQUEST.get('cRenderAll%s' % i, None) )
            channels.append({"name": name, "index": i, "active": active, "merged": merged, "colour": colour, 
                "start": start, "end": end, "render_all": render_all})
        return channels
    
    channels = None
    images = []
    for iId in imageIds:
        image = conn.getObject("Image", iId)
        if image == None: continue
        default_z = image.getSizeZ()/2   # image.getZ() returns 0 - should return default Z? 
        # need z for render_image even if we're projecting
        images.append({"id":iId, "z":default_z, "name": image.getName() })
        if channels is None:
            channels = getChannelData(image)
        if height == 0:
            height = image.getSizeY()
        if width == 0:
            width = image.getSizeX()
    
    if channels is None:
        return HttpResponse("Couldn't load channels for this image")
    size = {"height": height, "width": width}
    c_strs = []
    if channels:    # channels will be none when page first loads (no images)
        indexes = range(1, len(channels)+1)
        c_string = ",".join(["-%s" % str(c) for c in indexes])     # E.g. -1,-2,-3,-4
        mergedFlags = []
        for i, c, in enumerate(channels):
            if c["render_all"]:
                levels = "%s:%s" % (c["start"], c["end"])
            else: levels = ""
            if c["active"]:
                onFlag = str(i+1) + "|"
                onFlag += levels
                if split_grey: onFlag += "$FFFFFF"  # E.g.   1|100:505$0000FF
                c_strs.append( c_string.replace("-%s" % str(i+1), onFlag) )  # E.g. 1,-2,-3  or  1|$FFFFFF,-2,-3
            if c["merged"]:
                mergedFlags.append("%s|%s" % (i+1, levels))     # E.g. '1|200:4000'
            else: mergedFlags.append("-%s" % (i+1))  # E.g. '-1'
        # turn merged channels on in the last image
        c_strs.append( ",".join(mergedFlags) )
    
    template = kwargs.get('template', 'webtest/demo_viewers/split_view_figure.html')
    return render_to_response(template, {'images':images, 'c_strs': c_strs,'imageIds':idList,
        'channels': channels, 'split_grey':split_grey, 'merged_names': merged_names, 'proj': proj, 'size': size, 'query_string':query_string})


@login_required()
def dataset_split_view (request, datasetId, conn=None, **kwargs):
    """
    Generates a web page that displays a dataset in two panels, with the option to choose different
    rendering settings (channels on/off) for each panel. It uses the render_image url for each
    image, generating the full sized image which is scaled down to view. 
    
    The page also includes a form for editing the channel settings and display size of images.
    This form resubmits to this page and displays the page again with updated parameters. 
    
    @param request:     The django L{http request <django.core.handlers.wsgi.WSGIRequest>}
    @param datasetId:   The ID of the dataset. 
    @type datasetId:    Number. 
    
    @return:            The http response - html page displaying split view figure.
    """
    dataset = conn.getObject("Dataset", datasetId)
    
    try:
        size = request.REQUEST.get('size', 100)
        size = int(size)
    except:
        size = 100
        
    # returns a list of channel info from the image, overridden if values in request
    def getChannelData(image):
        channels = []
        i = 0;
        chs = image.getChannels()
        if chs is None:
            return []
        for i, c in enumerate(chs):
            if c is None:
                continue
            name = c.getLogicalChannel().getName()
            # if we have channel info from a form, we know that checkbox:None is unchecked (not absent)
            if request.REQUEST.get('cStart%s' % i, None):
                active_left = (None != request.REQUEST.get('cActiveLeft%s' % i, None) )
                active_right = (None != request.REQUEST.get('cActiveRight%s' % i, None) )
            else:
                active_left = True
                active_right = True
            colour = c.getColor()
            if colour is None:
                continue    # serious rendering engine problems
            colour = colour.getHtml();
            start = request.REQUEST.get('cStart%s' % i, c.getWindowStart())
            end = request.REQUEST.get('cEnd%s' % i, c.getWindowEnd())
            render_all = (None != request.REQUEST.get('cRenderAll%s' % i, None) )
            channels.append({"name": name, "index": i, "active_left": active_left, "active_right": active_right, 
                "colour": colour, "start": start, "end": end, "render_all": render_all})
        return channels
        
    images = []
    channels = None
    
    for image in dataset.listChildren():
        if channels == None or len(channels) == 0:
            channels = getChannelData(image)
        default_z = image.getSizeZ()/2   # image.getZ() returns 0 - should return default Z? 
        # need z for render_image even if we're projecting
        images.append({"id":image.getId(), "z":default_z, "name": image.getName() })

    if channels is None:
        return HttpResponse("<p class='center_message'>No Images in Dataset<p>")

    indexes = range(1, len(channels)+1)
    c_string = ",".join(["-%s" % str(c) for c in indexes])     # E.g. -1,-2,-3,-4

    leftFlags = []
    rightFlags = []
    for i, c, in enumerate(channels):
        if c["render_all"]:
            levels = "%s:%s" % (c["start"], c["end"])
        else: levels = ""
        if c["active_left"]:
            leftFlags.append("%s|%s" % (i+1, levels))     # E.g. '1|200:4000'
        else: leftFlags.append("-%s" % (i+1))  # E.g. '-1'
        if c["active_right"]:
            rightFlags.append("%s|%s" % (i+1, levels))     # E.g. '1|200:4000'
        else: rightFlags.append("-%s" % (i+1))  # E.g. '-1'
    
    c_left = ",".join(leftFlags)
    c_right = ",".join(rightFlags)
    
    template = kwargs.get('template', 'webtest/webclient_plugins/dataset_split_view.html')

    return render_to_response(template, {'dataset': dataset, 'images': images, 
        'channels':channels, 'size': size, 'c_left': c_left, 'c_right': c_right})


@login_required()
def image_dimensions (request, imageId, conn=None, **kwargs):
    """
    Prepare data to display various dimensions of a multi-dim image as axes of a grid of image planes. 
    E.g. x-axis = Time, y-axis = Channel.
    """
    image = conn.getObject("Image", imageId)
    if image is None:
        return render_to_response('webtest/demo_viewers/image_dimensions.html', {}) 
    
    mode = request.REQUEST.get('mode', None) and 'g' or 'c'
    dims = {'Z':image.getSizeZ(), 'C': image.getSizeC(), 'T': image.getSizeT()}
    
    default_yDim = 'Z'
    
    xDim = request.REQUEST.get('xDim', 'C')
    if xDim not in dims.keys():
        xDim = 'C'
        
    yDim = request.REQUEST.get('yDim', default_yDim)
    if yDim not in dims.keys():
        yDim = 'Z'
    
    xFrames = int(request.REQUEST.get('xFrames', 5))
    xSize = dims[xDim]
    yFrames = int(request.REQUEST.get('yFrames', 10))
    ySize = dims[yDim]
    
    xFrames = min(xFrames, xSize)
    yFrames = min(yFrames, ySize)
    
    xRange = range(xFrames)
    yRange = range(yFrames)
    
    # 2D array of (theZ, theC, theT)
    grid = []
    for y in yRange:
        grid.append([])
        for x in xRange:
            iid, theZ, theC, theT = image.id, 0,None,0
            if xDim == 'Z':
                theZ = x
            if xDim == 'C':
                theC = x
            if xDim == 'T':
                theT = x
            if yDim == 'Z':
                theZ = y
            if yDim == 'C':
                theC = y
            if yDim == 'T':
                theT = y
                
            grid[y].append( (iid, theZ, theC is not None and theC+1 or None, theT) )
    
        
    size = {"height": 125, "width": 125}
    
    return render_to_response('webtest/demo_viewers/image_dimensions.html', {'image':image, 'grid': grid, 
        "size": size, "mode":mode, 'xDim':xDim, 'xRange':xRange, 'yRange':yRange, 'yDim':yDim, 
        'xFrames':xFrames, 'yFrames':yFrames})


@login_required()
def image_rois (request, imageId, conn=None, **kwargs):
    """ Simply shows a page of ROI thumbnails for the specified image """
    roiService = conn.getRoiService()
    result = roiService.findByImage(long(imageId), None, conn.SERVICE_OPTS)
    roiIds = [r.getId().getValue() for r in result.rois]
    return render_to_response('webtest/demo_viewers/image_rois.html', {'roiIds':roiIds})


def webgateway_templates (request, base_template):
    """ Simply return the named template. Similar functionality to django.views.generic.simple.direct_to_template """
    template_name = 'webtest/webgateway/%s.html' % base_template
    return render_to_response(template_name, {})

@login_required()
@render_response()
def webclient_templates (request, base_template, **kwargs):
    """ Simply return the named template. Similar functionality to django.views.generic.simple.direct_to_template """
    template_name = 'webtest/webgateway/%s.html' % base_template
    return {'template': template_name}


@login_required()
def image_viewer (request, iid=None, conn=None, **kwargs):
    """ This view is responsible for showing pixel data as images. Delegates to webgateway, using share connection if appropriate """
    
    if iid is None:
        iid = request.REQUEST.get('image')

    template = 'webtest/webclient_plugins/center_plugin.fullviewer.html'
    
    return webgateway_views.full_viewer(request, iid, _conn=conn, template=template, **kwargs)

@login_required()
def stack_preview (request, imageId, conn=None, **kwargs):
    """ Shows a subset of Z-planes for an image """
    image = conn.getObject("Image", imageId)
    image_name = image.getName()
    sizeZ = image.getSizeZ()
    z_indexes = [0, int(sizeZ*0.25), int(sizeZ*0.5), int(sizeZ*0.75), sizeZ-1]
    return render_to_response('webtest/stack_preview.html', {'imageId':imageId, 'image_name':image_name, 'z_indexes':z_indexes})

@login_required()
def render_performance (request, obj_type, id, conn=None, **kwargs):
    """ Test rendering performance for all planes in an image """
    context = {}
    if obj_type == 'image':
        image = conn.getObject("Image", id)
        image._prepareRenderingEngine()

        # If a 'BIG Image'
        if image._re.requiresPixelsPyramid():
            MAX_TILES = 50
            tileList = []
            tile_w, tile_h = image._re.getTileSize()
            cols = image.getSizeX() / tile_w
            rows = image.getSizeY() / tile_h
            tileList = [ {'col':c, 'row':r} for r in range(rows) for c in range(cols)]
            if (len(tileList) > 2*MAX_TILES):
                tileList = tileList[ (len(tileList)/2):]    # start in middle of list (looks nicer!)
            tileList = tileList[:MAX_TILES]
            context = {'tileList': tileList, 'imageId':id}
        # A regular Image
        else:
            zctList = []
            for z in range(image.getSizeZ()):
                for c in range(image.getSizeC()):
                    for t in range(image.getSizeT()):
                        zctList.append({'z':z, 'c':c+1, 't':t})
            context = {'zctList':zctList, 'imageId':id}
    # A Plate
    elif obj_type == 'plate':
        imageIds = []
        plate = conn.getObject("Plate", id)
        for well in plate._listChildren():
            for ws in well.copyWellSamples():
                imageIds.append(ws.image.id.val)
        context = {'plate':plate, 'imageIds':imageIds}

    elif obj_type == "dataset":
        dataset = conn.getObject("Dataset", id)
        imageIds = [i.getId() for i in dataset.listChildren()]
        context = {'imageIds':imageIds}

    return render_to_response('webtest/demo_viewers/render_performance.html', context)

@login_required(setGroupContext=True)
def createTestImage (request, conn=None, **kwargs):
    """
    Creates a Test Image using numpy.
    Various parameters can be set using request.
    name, sizeX, sizeY, sizeZ, sizeC, sizeT, pixelType, dataset
    Returns the Image ID.
    """

    from numpy import fromfunction, int8, int16, int32, int64, uint8, uint16

    def getNumber(rstring, default, maxValue=None):
        try:
            n = int(request.REQUEST.get(rstring, default))
            if maxValue is not None:
                return min(n, maxValue)
            return n
        except:
            return default

    name = request.REQUEST.get('name', "webtest-TestImage")
    sizeX = getNumber('sizeX', 125, 2000)
    sizeY = getNumber('sizeY', 125, 2000)
    sizeZ = getNumber('sizeZ', 1, 500)
    sizeC = getNumber('sizeC', 1, 100)
    sizeT = getNumber('sizeT', 1, 500)
    ptype = request.REQUEST.get('pixelType', 'int8')
    dataset = getNumber('dataset', None)

    ptypes = {'int8':int8, 'int16':int16, 'int32':int32, 'int64':int64, 'uint8':uint8, 'uint16':uint16}
    if ptype not in ptypes:
        ptype = 'int8'
    dtype = ptypes[ptype]

    if dataset is not None:
        dataset = conn.getObject("Dataset", dataset)

    def f(y, x):
        return x

    def planeGen():
        for count in range(sizeZ * sizeC * sizeT):
            yield fromfunction(f, (sizeY, sizeX), dtype=dtype)

    desc = "Created via /webtest/createTestImage"
    zctPlanes = planeGen()

    image = conn.createImageFromNumpySeq(zctPlanes, name, sizeZ, sizeC, sizeT, description=desc, dataset=dataset)

    return HttpResponse(image.getId())
