from django.http import HttpResponseRedirect, HttpResponse
from django.core.urlresolvers import reverse
from django.shortcuts import render_to_response
from omeroweb.webgateway.views import getBlitzConnection, _session_logout
import settings
import logging
import traceback
import omero
import omero.model
from omero.rtypes import rstring
from omero.gateway import XmlAnnotationWrapper
import xml.sax
from django.utils import simplejson

# use the webclient's gateway connection wrapper
from webclient.webclient_gateway import OmeroWebGateway

logger = logging.getLogger('webfigure')

import webfigure_utils

def roi_viewer_jquery(request, imageId):
    """
    Displays an image, using 'jquery.drawinglibrary.js' to draw ROIs on the image. 
    """
    conn = getBlitzConnection (request, useragent="OMERO.webroi")
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webfigure_login'))
    
    image = conn.getImage(imageId)
    default_z = image.z_count()/2
    
    return render_to_response('webfigure/roi_viewers/raphael_viewer.html', {'image':image, 'default_z':default_z})
    
    
def roi_viewer_processing(request, imageId):
    """
    Displays an image, using 'processing.js' to draw ROIs on the image. 
    """
    conn = getBlitzConnection (request, useragent="OMERO.webroi")
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webfigure_login'))
    
    image = conn.getImage(imageId)
    default_z = image.z_count()/2
    
    return render_to_response('webfigure/roi_viewers/processing_viewer.html', {'image':image, 'default_z':default_z})


def get_rois(request, imageId):
    """
    Returns json data of the ROIs in the imageId in request. 
    """
    conn = getBlitzConnection (request, useragent="OMERO.webroi")
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webfigure_login'))
        
    rois = []
    
    if imageId != None:
        roiService = conn.getRoiService()
        rois = webfigure_utils.getRoiShapes(roiService, long(imageId))  # gets a whole json list of ROIs
        rois.sort(key=lambda x: x['id']) # sort by ID - same as in measurement tool.
    
    return HttpResponse(simplejson.dumps(rois), mimetype='application/javascript')
    

def getSpimData (conn, image):
    """
    Returns a map of SPIM data according to the specification at http://www.ome-xml.org/wiki/SPIM/InitialSupport
    where extra Objectives are stored in the Instrument, multiple Images are linked to the same 'spim-set' annotation, one
    Image for each SPIM angle. 
    Extra Objective attributes, SPIM angles and Stage Positions stored in XML annotations with 3 different namespaces.  
    """
    
    instrument = image.getInstrument()
    
    obs = []
    for o in instrument.getObjectives():
        ob = []
        ob.append( ('Model', o.model ) )
        ob.append( ('Manufacturer', o.manufacturer ) )
        ob.append( ('Serial Number', o.serialNumber ) )
        ob.append( ('Nominal Magnification', o.nominalMagnification ) )
        obs.append(ob)
       
    images = [] 
    objExtras = []
    spimAngles = []
    stagePositions = {}     # iid: []
    
    def getLinkedImages(annId):
        query = "select i from Image as i join i.annotationLinks as a_link join a_link.child as a where a.id='%s'" % annId
        imgs = conn.getQueryService().findAllByQuery(query, None)
        return [omero.gateway.ImageWrapper(conn, i) for i in imgs]
    
    # get the Objective attributes and Spim-set annotations (spim-set also linked to other images)
    for ann in image.listAnnotations():
        if isinstance(ann, XmlAnnotationWrapper):
            print "ID:", ann.id
            xmlText = ann.textValue
            if ann.ns == "ome-xml.org:additions:post2010-06:objective":
                elementNames = ['ObjectiveAdditions']
            elif ann.ns == "ome-xml.org:additions:post2010-06:spim:set":
                elementNames = ['SpimImage']
                # also get the other images annotated with this 
                images = getLinkedImages(ann.id)
            else:
                elementNames = []
                
            handler = AnnXmlHandler(elementNames)
            xml.sax.parseString(xmlText, handler)
            
            if ann.ns == "ome-xml.org:additions:post2010-06:objective":
                objExtras.extend(handler.attributes)
            elif ann.ns == "ome-xml.org:additions:post2010-06:spim:set":
                spimAngles.extend(handler.attributes)
    
    # for All images, get the spim-position data. 
    for i in images:
        spos = []
        for ann in i.listAnnotations():
            if isinstance(ann, XmlAnnotationWrapper) and ann.ns == "ome-xml.org:additions:post2010-06:spim:positions":
                handler = AnnXmlHandler(['StagePosition'])
                xml.sax.parseString(xmlText, handler)
                spos.extend(handler.attributes)
        
        stagePositions[i.id] = spos
        
    #print "Object Extras"
    #print objExtras
    #print "Stage Positions"
    #print stagePositions
    #print "Spim Angles"
    #print spimAngles
    
    if len(objExtras) == 0 and len(stagePositions) == 0 and len(spimAngles) == 0:
        return None
        
    return {'images':images, 'obs': obs, 'objExtras':objExtras, 'stagePositions':stagePositions, 'spimAngles': spimAngles}
   

class AnnXmlHandler(xml.sax.handler.ContentHandler):
    """ Parse XML to get Objective attributes """
    def __init__(self, elementNames):
        self.inElement = False
        self.elementNames = elementNames
        self.attributes = []
 
    def startElement(self, name, attributes):
        if name in self.elementNames:
            kv = {}
            for k, v in attributes.items():
                kv[str(k)] = str(v) 
            self.attributes.append(kv)
            self.inElement = True
            self.buffer = ""
 
    def characters(self, data):
        if self.inElement:
            self.buffer += data
 
    # if we're ending an element that we're interested in, save the text in map
    def endElement(self, name):
        self.inElement = False

    
def image_dimensions (request, imageId):
    """
    Prepare data to display various dimensions of a multi-dim image as axes of a grid of image planes. 
    E.g. x-axis = Time, y-axis = Channel. 
    If the image has spim data, then combine images with different SPIM angles to provide an additional
    dimension. Also get the SPIM data from various XML annotations and display on page. 
    """
    
    conn = getBlitzConnection (request, useragent="OMERO.webfigure")
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webfigure_login'))
    
    image = conn.getImage(imageId)
    if image is None:
        return render_to_response('webfigure/image_dimensions.html', {}) 
    
    mode = request.REQUEST.get('mode', None) and 'g' or 'c'
    dims = {'Z':image.z_count(), 'C': image.c_count(), 'T': image.t_count()}
    
    default_yDim = 'Z'
    
    spim_data = getSpimData(conn, image)
    if spim_data is not None:
        dims['Angle'] = len(spim_data['images'])
        default_yDim = 'Angle'
    
    xDim = request.REQUEST.get('xDim', 'T')
    if xDim not in dims.keys():
        xDim = 'T'
        
    yDim = request.REQUEST.get('yDim', default_yDim)
    if yDim not in dims.keys():
        yDim = 'Z'
    
    xFrames = int(request.REQUEST.get('xFrames', 5))
    xSize = dims[xDim]
    yFrames = int(request.REQUEST.get('yFrames', 5))
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
            if xDim == 'Angle':
                iid = spim_data['images'][x].id
            if yDim == 'Z':
                theZ = y
            if yDim == 'C':
                theC = y
            if yDim == 'T':
                theT = y
            if yDim == 'Angle':
                iid = spim_data['images'][y].id
                
            grid[y].append( (iid, theZ, theC is not None and theC+1 or None, theT) )
    
    for y in yRange:
        print ":".join( [str(d) for d in grid[y] ] )
        
    size = {"height": 125, "width": 125}
    
    return render_to_response('webfigure/image_dimensions.html', {'image':image, 'spim_data':spim_data, 'grid': grid, 
        "size": size, "mode":mode, 'xDim':xDim, 'xRange':xRange, 'yRange':yRange, 'yDim':yDim, 
        'xFrames':xFrames, 'yFrames':yFrames})
    

def add_annotations (request):
    """
    Creates a L{omero.gateway.CommentAnnotationWrapper} and adds it to the images according 
    to variables in the http request. 
    
    @param request:     The django L{django.core.handlers.wsgi.WSGIRequest}
                            - imageIds:     A comma-delimited list of image IDs
                            - comment:      The text to add as a comment to the images
                            
    @return:            A simple html page with a success message 
    """
    
    conn = getBlitzConnection (request, useragent="OMERO.webfigure")
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webfigure_login'))
    
    idList = request.REQUEST.get('imageIds', None)    # comma - delimited list
    if idList:
        imageIds = [long(i) for i in idList.split(",")]
    else: imageIds = []
    
    comment = request.REQUEST.get('comment', None)
    print imageIds
    print comment
    
    updateService = conn.getUpdateService()
    ann = omero.model.CommentAnnotationI()
    ann.setTextValue(rstring( str(comment) ))
    ann = updateService.saveAndReturnObject(ann)
    annId = ann.getId().getValue()
    """
    from omero.gateway import CommentAnnotationWrapper
    ann = CommentAnnotationWrapper()
    #ann.setNs()
    ann.setValue(comment)
    """
    # get the wrapped annotation
    #annotation = conn.getAnnotation(annId)
    
    images = []
    for iId in imageIds:
        image = conn.getImage(iId)
        if image == None: continue
        #image.linkAnnotation(ann)   # get: PermissionMismatchGroupSecurityViolation: Manually setting permissions currently disallowed
        
        l = omero.model.ImageAnnotationLinkI()
        parent = omero.model.ImageI(iId, False)     # use unloaded object to avoid update conflicts
        l.setParent(parent)
        l.setChild(ann)
        updateService.saveObject(l)
        images.append(image)
        
    return render_to_response('webfigure/add_annotations.html', {'images':images, 'comment':comment})
    

def dataset_split_view (request, datasetId):
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
    
    conn = getBlitzConnection (request, useragent="OMERO.webfigure")
    if conn is None or not conn.isConnected():
        # get the url that directed us here:
        # reverse(viewname, urlconf=None, args=None, kwargs=None, prefix=None, current_app=None)
        # url = reverse(dataset_split_view, kwargs={'datasetId':datasetId})
        url = request.META.get("PATH_INFO")
        # log in with the webclient login, redirecting back here:
        loginUrl = "%s?url=%s" % (reverse('weblogin'), url)
        return HttpResponseRedirect(loginUrl)
        
    dataset = conn.getDataset(datasetId)
    
    try:
        w = request.REQUEST.get('width', 100)
        width = int(w)
    except:
        width = 100
    try:
        h = request.REQUEST.get('height', 100)
        height = int(h)
    except:
        height = 100
        
    # returns a list of channel info from the image, overridden if values in request
    def getChannelData(image):
        channels = []
        i = 0;
        for i, c in enumerate(image.getChannels()):
            name = c.getLogicalChannel().getName()
            # if we have channel info from a form, we know that checkbox:None is unchecked (not absent)
            if request.REQUEST.get('cStart%s' % i, None):
                active_left = (None != request.REQUEST.get('cActiveLeft%s' % i, None) )
                active_right = (None != request.REQUEST.get('cActiveRight%s' % i, None) )
            else:
                active_left = True
                active_right = True
            colour = c.getColor().getHtml()
            start = request.REQUEST.get('cStart%s' % i, c.getWindowStart())
            end = request.REQUEST.get('cEnd%s' % i, c.getWindowEnd())
            render_all = (None != request.REQUEST.get('cRenderAll%s' % i, None) )
            channels.append({"name": name, "index": i, "active_left": active_left, "active_right": active_right, 
                "colour": colour, "start": start, "end": end, "render_all": render_all})
        print channels
        return channels
        
    images = []
    channels = None
    
    for image in dataset.listChildren():
        if channels == None:
            channels = getChannelData(image)
        default_z = image.z_count()/2   # image.getZ() returns 0 - should return default Z? 
        # need z for render_image even if we're projecting
        images.append({"id":image.getId(), "z":default_z, "name": image.getName() })
    
    size = {'width':width, 'height':height}
    
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
    
    return render_to_response('webfigure/dataset_split_view.html', {'dataset': dataset, 'images': images, 
        'channels':channels, 'size': size, 'c_left': c_left, 'c_right': c_right})
    
    
def split_view_figure (request):
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
    
    print type(request)
    conn = getBlitzConnection (request, useragent="OMERO.webfigure")
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webfigure_login'))
    
    query_string = request.META["QUERY_STRING"]
    
    
    idList = request.REQUEST.get('imageIds', None)    # comma - delimited list
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
        for i, c in enumerate(image.getChannels()):
            name = request.REQUEST.get('cName%s' % i, c.getLogicalChannel().getName())
            # if we have channel info from a form, we know that checkbox:None is unchecked (not absent)
            if request.REQUEST.get('cName%s' % i, None):
                active = (None != request.REQUEST.get('cActive%s' % i, None) )
                merged = (None != request.REQUEST.get('cMerged%s' % i, None) )
            else:
                active = True
                merged = True
            colour = c.getColor().getHtml()
            start = request.REQUEST.get('cStart%s' % i, c.getWindowStart())
            end = request.REQUEST.get('cEnd%s' % i, c.getWindowEnd())
            render_all = (None != request.REQUEST.get('cRenderAll%s' % i, None) )
            channels.append({"name": name, "index": i, "active": active, "merged": merged, "colour": colour, 
                "start": start, "end": end, "render_all": render_all})
        return channels
    
    channels = None
    images = []
    for iId in imageIds:
        image = conn.getImage(iId)
        if image == None: continue
        default_z = image.z_count()/2   # image.getZ() returns 0 - should return default Z? 
        # need z for render_image even if we're projecting
        images.append({"id":iId, "z":default_z, "name": image.getName() })
        if channels == None:
            channels = getChannelData(image)
        if height == 0:
            height = image.getHeight()
        if width == 0:
            width = image.getWidth()
    
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
    
    return render_to_response('webfigure/split_view_figure.html', {'images':images, 'c_strs': c_strs,'imageIds':idList,
        'channels': channels, 'split_grey':split_grey, 'merged_names': merged_names, 'proj': proj, 'size': size, 'query_string':query_string})
    

def login (request):
    """
    Attempts to get a connection to the server by calling L{omeroweb.webgateway.views.getBlitzConnection} with the 'request'
    object. If a connection is created, the user is directed to the 'webfigure_index' page. 
    If a connection is not created, this method returns a login page.
    
    @param request:     The django http request
    @return:            The http response - webfigure_index or login page
    """
    if request.method == 'POST' and request.REQUEST['server']:
        blitz = settings.SERVER_LIST.get(pk=request.REQUEST['server'])
        request.session['server'] = blitz.id
        request.session['host'] = blitz.host
        request.session['port'] = blitz.port
    
    conn = getBlitzConnection (request, useragent="OMERO.webfigure")
    logger.debug(conn)
    if conn is not None:
        return HttpResponseRedirect(reverse('webfigure_index'))
    return render_to_response('webfigure/login.html', {'gw':settings.SERVER_LIST})


def logout (request):
    """
    Attempts to delete the username and password from the request.session, then returns 
    the login page. 
    @param request:     The django http request
    @return:            The http response - webfigure_login
    """
    _session_logout(request, request.session['server'])
    try:
        del request.session['username']
    except KeyError:
        logger.error(traceback.format_exc())
    try:
        del request.session['password']
    except KeyError:
        logger.error(traceback.format_exc())
    return HttpResponseRedirect(reverse('webfigure_login'))


def index (request):
    """
    Displays the 'home page' of the webfigure app, with links to various pages in the app.
    
    @param request:     The django http request
    @return:            The http response - webfigure index
    """
    print "webfigure login..."
    conn = getBlitzConnection (request, useragent="OMERO.webfigure")
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webfigure_login'))

    return render_to_response('webfigure/index.html', {'client': conn})
