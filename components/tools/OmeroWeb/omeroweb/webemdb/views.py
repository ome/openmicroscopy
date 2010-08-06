from django.http import HttpResponseRedirect, HttpResponse
from django.core.urlresolvers import reverse
from django.shortcuts import render_to_response
from django.utils import simplejson
from omeroweb.webgateway.views import getBlitzConnection, _session_logout
from omeroweb.webgateway import views as webgateway_views
from webclient.controller.annotation import BaseAnnotation
import settings
import logging
import traceback
import omero
import omero.constants
from omero.rtypes import rstring, rint

logger = logging.getLogger('webemdb')

PUBLICATION_NAMESPACE = "openmicroscopy.org/omero/emdb/publication"

# for wrapping the bit mask 
import os, tempfile, zipfile
from django.core.servers.basehttp import FileWrapper

from omero.sys import Parameters, Filter


def dataset(request, entryId, datasetId):
    conn = getConnection(request)

    dataset = conn.getDataset(datasetId)

    return render_to_response('webemdb/data/dataset.html', {'dataset': dataset, 'entryId': entryId})


def data(request, entryId):
    conn = getConnection(request)

    entryName = str(entryId)
    project = conn.findProject(entryName)
    
    # only want the first few images from each dataset
    p = omero.sys.Parameters()
    p.map = {}
    f = omero.sys.Filter()
    f.limit = rint(5)
    f.offset = rint(0)
    p.theFilter = f
    
    datasets = []
    entryId = project.getName()
    for d in project.listChildren():
        ds = {}
        ds["getId"] = str(d.getId())
        print "ID", d.getId()
        name = d.getName()
        if name == entryId: continue    # this dataset contains the map, not associated data. 
        ds["getName"] = name
        ds["getDescription"] = d.getDescription()
        ds["countChildren"] = d.countChildren()
        ds["listChildren"] = d.listChildren(params=p)
        datasets.append(ds)
    
    if project == None:
        # project not found (None) handled by template
        return render_to_response('webemdb/data/data.html', {'project':project})

    return render_to_response('webemdb/data/data.html', {'project':project, 'datasets': datasets})
    
    
def entry (request, entryId):
    conn = getConnection(request)
    
    #print dir(conn)
        
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
    pdbs = []
    xmlName = "emd-%s.xml" % entryName
    gifName = "%s.gif" % entryName
    bitName = "%s.bit" % entryName
    
    for a in project.listAnnotations():
        try:    # not all annotations have getFileName() E.g. comment annotations
            if xmlName == a.getFileName():
                xml = a
            elif gifName == a.getFileName():
                gif = a
            elif bitName == a.getFileName():
                bit = a
            elif a.getFileName().endswith(".pdb.gz"):
                pdbs.append(a)
        except:
            pass
    
    return render_to_response('webemdb/entries/entry.html', 
        {'project':project, 'xml': xml, 'gif': gif, 'img': img, 'map': mrcMap, 'bit': bit, 'pdbs': pdbs})
        
        
def oa_viewer(request, entryId, fileId): 
    conn = getConnection(request)
    return render_to_response('webemdb/entries/oa_viewer.html', {'entryId': entryId, 'fileId': fileId})
    
def viewport(request, imageId):
    conn = getConnection(request)
    return render_to_response('webemdb/entries/viewport.html', {'imageId': imageId})


def gif (request, entryId):
    """
    Looks up the preview gif E.g. "80_1001.gif"  for the specified entry, based on name of originalfile.
    
    TODO: This method gets the file via it's project, because it's wrapped nicely. Same as  def file() below...
    """
    conn = getConnection(request)
        
    entryName = str(entryId)
    project = conn.findProject(entryName)
    
    if project == None: return HttpResponse()
        
    # get the file by name
    gif = None
    gifName = "80_%s.gif" % entryId
    for a in project.listAnnotations():
        try:
            if a.getFileName() == gifName:
                gif = a
        except:
            pass
    
    if gif:
        file_data = gif.getFile()
        return HttpResponse(file_data, mimetype='image/gif')
    else:
        return HttpResponse()


def getFile (request, entryId, fileId):
    """
    Gets the file by Id and returns it according to mime type for display.
    """
    
    conn = getConnection(request)
        
    entryName = str(entryId)
    project = conn.findProject(entryName)
    
    if project == None: return HttpResponse()
    print "Project", project
        
    # get the file by ID
    ann = None
    ann = conn.getFileAnnotation(long(fileId))
    print "getFileAnnotation", ann
    #for a in project.listAnnotations():
    #    if a.id == long(fileId):
    #        ann = a
    #print "Annotation", ann

    # determine mime type to assign
    if ann:
        
        fileName = ann.getFileName()
        mimetype = "text/plain"
        
        if fileName.endswith(".bit") or fileName.endswith(".pdb.gz"): mimetype='application/octet-stream'
        if fileName.endswith(".xml"): mimetype='text/xml'
        if fileName.endswith(".gif"): mimetype='image/gif'
        
        # file_data = ann.getFile()
        # return HttpResponse(file_data, mimetype=mimetype)
        
        # if the file data is large, we will have a temp file
        from django.conf import settings 
        tempdir = settings.FILE_UPLOAD_TEMP_DIR
        temp = os.path.join(tempdir, ('%i-%s.download' % (ann.file.id.val, conn._sessionUuid))).replace('\\','/')
        logger.info("temp path: %s" % str(temp))
        f = open(str(temp),"wb")
        for piece in ann.getFileInChunks():
            f.write(piece)
        f.seek(0)
        f.close()
        print "temp file created at", temp
        
        from django.core.servers.basehttp import FileWrapper
        originalFile_data = FileWrapper(file(temp))
            
        rsp = HttpResponse(originalFile_data)
               
        rsp['Content-Type'] = mimetype
        rsp['Content-Length'] = ann.getFileSize()
        # this tells the browser to give the user a 'download' dialog
        #rsp['Content-Disposition'] = 'attachment; filename=%s' % (ann.getFileName().replace(" ","_"))
            
        return rsp
        """
        if file_data.startswith(settings.FILE_UPLOAD_TEMP_DIR):
            from django.core.servers.basehttp import FileWrapper
            temp = FileWrapper(file(file_data))
            rsp = HttpResponse(temp, content_type=mimetype)
            rsp['Content-Type'] = 'application/octet-stream'
            rsp['Content-Disposition'] = 'attachment; filename=%s' % (a.getFileName())
            rsp['Content-Length'] = os.path.getsize(file_data)
            print "File Size", os.path.getsize(file_data)
            
            return rsp
          """  
        #return HttpResponse(file_data, mimetype=mimetype)
    return HttpResponse()
    

def logout (request):
    """ Shouldn't ever be used (public db)"""
    print "logging out...."
    _session_logout(request, request.session['server'])
    try:
        del request.session['username']
    except KeyError:
        logger.error(traceback.format_exc())
    try:
        del request.session['password']
    except KeyError:
        logger.error(traceback.format_exc())
    return HttpResponseRedirect(reverse('webemdb_loggedout'))

def loggedout (request):
    print "logged out"
    return render_to_response('webemdb/loggedout.html', {})


def index (request):
    """ Show a selection of the latest EMDB entries """
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
    
    return render_to_response('webemdb/index.html', {'projects': projects, 'entryCount': len(entryIds)})


def publications (request):
    """ List all the publications, which are stored as CommentAnnotations with namespace """
                    
    conn = getConnection(request)
    qs = conn.getQueryService()
    
    namespace = PUBLICATION_NAMESPACE
    comments = qs.findAllByQuery("select a from CommentAnnotation a where a.ns='%s'" % namespace, None)
    
    pubs = []
    # entryId is the first EMDB entry for this publication
    pubAttributes = ["entryId", "authors", "title", "journal", "volume", "pages", "year", "externalReference"] 
    
    for c in comments:
        txt = c.getTextValue().getValue()
        values = txt.split('\n')
        pub = {}
        for a in zip(pubAttributes, values):
            pub[a[0]] = a[1]
        pub["commentId"] = c.getId().getValue()
        pubs.append(pub)
        
    pubs.sort(key=lambda p: p['entryId'])
    pubs.reverse()
    
    return render_to_response('webemdb/browse/publications.html', {'publications': pubs})
    

def getEntriesByPub (request, publicationId):
    
    conn = getConnection(request)
    qs = conn.getQueryService()
    
    projects = qs.findAllByQuery("select p from Project as p " \
                    "left outer join fetch p.annotationLinks as a_link " \
                    "left outer join fetch a_link.child " \
                    "where a_link.child.id = %s" % publicationId, None)
                    
    pData = {}
    
    for p in projects:
        entryId = p.getName().getValue()
        desc = p.getDescription().getValue()
        pData[entryId] = desc
    print pData
    
    return HttpResponse(simplejson.dumps(pData), mimetype='application/javascript')
    

def getConnection(request):
    conn = getBlitzConnection (request)
    if conn is None or not conn.isConnected() and request.REQUEST['server']:
        blitz = settings.SERVER_LIST.get(pk=1)
        request.session['server'] = blitz.id
        request.session['host'] = blitz.host
        request.session['port'] = blitz.port
        request.session['password'] = "ome"
        request.session['username'] = "emdb"
        #request.session['server'] = 'localhost'
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