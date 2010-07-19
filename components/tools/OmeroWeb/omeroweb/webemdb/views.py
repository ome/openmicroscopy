from django.http import HttpResponseRedirect, HttpResponse
from django.core.urlresolvers import reverse
from django.shortcuts import render_to_response
from omeroweb.webgateway.views import getBlitzConnection, _session_logout
from omeroweb.webgateway import views as webgateway_views
from webclient.controller.annotation import BaseAnnotation
import settings
import logging
import traceback
import omero
import omero.constants

logger = logging.getLogger('webemdb')
    

def entry (request, entryId):
    conn = getConnection(request)
        
    entryName = str(entryId)
    project = conn.findProject(entryName)
    
    if project == None:
        # project not found (None) handled by template
        return render_to_response('webemdb/entries/entry.html', {'project':project})
        
    # find the mrc map image. E.g emd_1003.map In a dataset named same as project E.g. 1003
    img = None
    mrcMap = None
    namespace = omero.constants.namespaces.NSCOMPANIONFILE 
    imgName = "emd_%s.map" % entryName
    for d in project.listChildren():
        if d.getName() == entryName:
            for i in d.listChildren():
                if i.getName() == imgName:
                    img = i
                    for a in img.listAnnotations():
                        if imgName == a.getFileName() and a.getNs() == namespace:
                            mrcMap = a
                    break
    
    xml = None
    gif = None
    bit = None
    xmlName = "emd-%s.xml" % entryName
    gifName = "%s.gif" % entryName
    bitName = "%s.bit" % entryName
    
    for a in project.listAnnotations():
        if a.getFileName == None: continue
        if xmlName == a.getFileName():
            xml = a
        elif gifName == a.getFileName():
            gif = a
        elif bitName == a.getFileName():
            bit = a
    
    return render_to_response('webemdb/entries/entry.html', 
        {'project':project, 'xml': xml, 'gif': gif, 'img': img, 'map': mrcMap, 'bit': bit})
        
        
def oa_viewer(request, entryId, fileId): 
    conn = getConnection(request)
    
    return render_to_response('webemdb/entries/oa_viewer.html', {'entryId': entryId, 'fileId': fileId})
    

def map (request, imageId, fileId):
    """
    Gets the file by Id and returns it as a mrc map.
    N.B. We get the file from the Image it is attached to, because the 
    project.listAnnotations() method returns the original file in a wrapper which provides
    the file data with getFile(). 
    see http://djangosnippets.org/snippets/365/  for zip, temp file, etc
    """
    conn = getConnection(request)
        
    """
    annotation = BaseAnnotation(conn)
    annotation.getFileAnnotation(fileId)    
    rsp = HttpResponse(annotation.originalFile_data)
    rsp['ContentType'] = 'application/octet-stream'
    rsp['Content-Disposition'] = 'attachment; filename=%s' % (a.getFileName())
    return rsp"""
    
    image = conn.getImage(long(imageId))
    imgName = image.getName()  # map named same as image
    namespace = omero.constants.namespaces.NSCOMPANIONFILE
    mrcMap = None
    for a in image.listAnnotations():
        if imgName == a.getFileName() and a.getNs() == namespace:
            data = a.getFile()
            rsp = HttpResponse(annotation.originalFile_data)
            rsp['ContentType'] = 'application/octet-stream'
            rsp['Content-Disposition'] = 'attachment; filename=%s' % (a.getFileName())
            return rsp
            
    return HttpResponse()


def file (request, entryId, fileId):
    """
    Gets the file by Id and returns it according to mime type for display.
    N.B. We get the file from the Project it is attached to, because the 
    project.listAnnotations() method returns the original file in a wrapper which provides
    the file data with getFile(). 
    """
    
    conn = getConnection(request)
        
    entryName = str(entryId)
    project = conn.findProject(entryName)
    
    if project == None: return HttpResponse()
        
    oFile = None
    for a in project.listAnnotations():
        if a.id == long(fileId):
            oFile = a
    
    if oFile:
        file_data = oFile.getFile()
        fileName = oFile.getFileName()
        mimetype = "text/plain"
        if fileName.endswith(".bit"): mimetype='application/oav'
        if fileName.endswith(".xml"): mimetype='text/xml'
        if fileName.endswith(".gif"): mimetype='image/gif'
        return HttpResponse(file_data, mimetype=mimetype)
    return HttpResponse()
    

def logout (request):
    """ Shouldn't ever be used (public db)"""
    
    _session_logout(request, request.session['server'])
    try:
        del request.session['username']
    except KeyError:
        logger.error(traceback.format_exc())
    try:
        del request.session['password']
    except KeyError:
        logger.error(traceback.format_exc())
    return HttpResponseRedirect(reverse('webemdb_index'))


def index (request):
    conn = getConnection(request)

    entryIds = [] # names of projects
    for p in conn.listProjects():
        try:
            entryIds.append(int(p.getName()))
        except: pass
    entryIds.sort()
    entryIds.reverse()
    # truncate. 
    lastIds = entryIds[:10]
    
    projects = []
    for entryName in lastIds:
        projects.append(conn.findProject(str(entryName)))
    
    return render_to_response('webemdb/index.html', {'projects': projects})


def getConnection(request):
    conn = getBlitzConnection (request)
    if conn is None or not conn.isConnected() and request.REQUEST['server']:
        blitz = settings.SERVER_LIST.get(pk=1)
        request.session['server'] = blitz.id
        request.session['host'] = blitz.host
        request.session['port'] = blitz.port
        request.session['password'] = "ola"
        request.session['username'] = "ome"
        request.session['server'] = 'localhost'
        conn = getBlitzConnection (request)
        logger.debug(conn)
    return conn

def image_viewer (request, iid, **kwargs):
    """ This view is responsible for showing pixel data as images """
    
    conn = getBlitzConnection (request)
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webemdb_login'))
    
    kwargs['viewport_server'] = '/webclient'
    
    return webgateway_views.full_viewer(request, iid, _conn=conn, **kwargs)