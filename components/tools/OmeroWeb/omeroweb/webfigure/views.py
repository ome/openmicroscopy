from django.http import HttpResponseRedirect
from django.core.urlresolvers import reverse
from django.shortcuts import render_to_response
from omeroweb.webgateway.views import getBlitzConnection, _session_logout
from omeroweb.webgateway import views as webgateway_views
import settings
import logging
import traceback
import omero
import omero.model
from omero.rtypes import rstring

logger = logging.getLogger('webfigure')


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
    #annotation = conn.getCommentAnnotation(annId)
    
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
        return HttpResponseRedirect(reverse('webfigure_login'))
        
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
            print "Channel", i
            print c.getLogicalChannel().getName()
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
    conn = getBlitzConnection (request, useragent="OMERO.webfigure")
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webfigure_login'))

    return render_to_response('webfigure/index.html', {'client': conn})
