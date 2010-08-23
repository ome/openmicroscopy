from django.http import HttpResponseRedirect, HttpResponse
from django.core.urlresolvers import reverse
from django.shortcuts import render_to_response
from django.utils import simplejson
from django.core.servers.basehttp import FileWrapper
from django.conf import settings 

from omeroweb.webgateway.views import getBlitzConnection, _session_logout
from omeroweb.webgateway import views as webgateway_views
from webclient.controller.annotation import BaseAnnotation
import settings
import logging
import traceback
import omero
import omero.constants
import omero.scripts
from omero.rtypes import rstring, rint, rlong, robject, unwrap

logger = logging.getLogger('webemdb')

PUBLICATION_NAMESPACE = "openmicroscopy.org/omero/emdb/publication"

# for wrapping the bit mask 
import os, tempfile, zipfile
from django.core.servers.basehttp import FileWrapper

from omero.sys import Parameters, Filter
import omero.util.script_utils as scriptUtil

# temp solution for job-ID : str(processor)
jobMap = {}

def eman(request, imageId, **kwargs):
    
    try:
        from EMAN2 import * 
    except:
        logger.info("EMAN2 failed to import. This can be fixed by adding try/catch to EMAN2.py, line 56: 'import EMAN2db.py' ")
    
    
    conn = getConnection(request)
    
    rawPixelStore = conn.createRawPixelsStore()
    queryService = conn.getQueryService()
    
    query_string = "select p from Pixels p join fetch p.image as i join fetch p.pixelsType where i.id='%s'" % imageId
    pixels = queryService.findByQuery(query_string, None)
    
    theZ, theC, theT = (0,0,0)
    bypassOriginalFile = True
    rawPixelStore.setPixelsId(pixels.getId().getValue(), bypassOriginalFile)
    plane2D = scriptUtil.downloadPlane(rawPixelStore, pixels, theZ, theC, theT)
    
    sizeX = pixels.getSizeX().getValue()
    sizeY = pixels.getSizeY().getValue()
    sizeZ = 1
    #em = EMData()
    
    em = EMNumPy.numpy2em(plane2D)
    
    f = kwargs['filter']
    if f == "fft":
        filterName = "basis.fft"
        filterParamMap = {"dir": 1, }
        em.process_inplace(filterName, filterParamMap)
    elif f == "median":
        if 'radius' in kwargs:
            filterParamMap = {"radius": int(kwargs['radius'])}
            em.process_inplace("eman1.filter.median", filterParamMap) 
        else:     em.process_inplace("eman1.filter.median") 
    elif f == "log":
        em.process_inplace("math.log")
    
    tempdir = settings.FILE_UPLOAD_TEMP_DIR
    tempJpg = os.path.join(tempdir, ('%s.emanFilter.jpg' % (conn._sessionUuid))).replace('\\','/')
    
    em.write_image(tempJpg)
    
    originalFile_data = FileWrapper(file(tempJpg))
        
    rsp = HttpResponse(originalFile_data)
           
    rsp['Content-Type'] = "image/jpg"
    
    return rsp


def script_run(request, scriptId):
    """
    Runs a script using values in a POST
    """
    conn = getConnection(request)
    #print dir(conn)
    scriptService = conn.getScriptService()
    
    inputMap = {}
    
    sId = long(scriptId)
    
    params = scriptService.getParams(sId)
    scriptName = params.name.replace("_", " ")
    
    for key, param in params.inputs.items():
        if key in request.POST:
            value = request.POST[key]
            prototype = param.prototype
            pclass = prototype.__class__
            if pclass == omero.rtypes.RListI:
                valueList = []
                listClass = omero.rtypes.rstring
                l = prototype.val     # list
                if len(l) > 0:       # check if a value type has been set (first item of prototype list)
                    listClass = l[0].getValue().__class__
                    if listClass == int(1).__class__:
                        listClass = omero.rtypes.rint
                    if listClass == long(1).__class__:
                        listClass = omero.rtypes.rlong
                
                for v in value.split(","):
                    try:
                        obj = listClass(str(v.strip())) # seem to need the str() for some reason
                    except:
                        # print "Invalid entry for '%s' : %s" % (key, v)
                        continue
                    if isinstance(obj, omero.model.IObject):
                        valueList.append(omero.rtypes.robject(obj))
                    else:
                        valueList.append(obj)
                inputMap[key] = omero.rtypes.rlist(valueList)
            
            elif pclass == omero.rtypes.RMapI:
                # TODO: Handle maps same way as lists. 
                valueMap = {}
                m = prototype.val   # check if a value type has been set for the map

            else:
                try:
                    inputMap[key] = pclass(value)
                except:
                    # print "Invalid entry for '%s' : %s" % (key, value)
                    continue
                
    #print inputMap
    
    proc = scriptService.runScript(sId, inputMap, None)
    
    i = 0
    while str(i) in jobMap:
        i += 1
    key = str(i)
    jobMap[key] = str(proc)
    
    # TODO - return the input map, to display what the user entered. 
    return render_to_response('webemdb/scripts/script_running.html', {'scriptName': scriptName, 'jobId': key})
    
    
def script_results(request, jobId):
    
    if jobId not in jobMap:
        return HttpResponse("Results not found (may have already been returned)")
        
    procString = jobMap[jobId]
    del jobMap[jobId]   # delete this, since we cannot use it again to get the results
    
    conn = getConnection(request)
    client = conn.c
    
    proc = omero.grid.ScriptProcessPrx.checkedCast(client.ic.stringToProxy(procString))
    
    try:
        cb = omero.scripts.ProcessCallbackI(client, proc)
        while not cb.block(1000): # ms.
            pass
        cb.close()
        results = proc.getResults(0)    # ms
    finally:
        proc.close(False) 
    
    message = None
    # Handle the expected 'Message' in results. 
    if 'Message' in results:
        message = results['Message'].getValue()
    
    # if we have stdout or stderr, download the file and return it.
    rawFileService = conn.createRawFileStore()
    queryService = conn.getQueryService()
    stdout = None
    stderr = None
    if 'stdout' in results:
        origFile = results['stdout'].getValue()
        fileId = origFile.getId().getValue()
        stdout = scriptUtil.readFromOriginalFile(rawFileService, queryService, fileId)
    if 'stderr' in results:
        origFile = results['stderr'].getValue()
        fileId = origFile.getId().getValue()
        stderr = scriptUtil.readFromOriginalFile(rawFileService, queryService, fileId)
        
    # look for any other string values and images in results...  
    resultMap = {}
    strings = []  
    images = []
    for key, value in results.items():
        if key not in ["Message", "stdout", "stderr"]:
            obj = value.getValue()
            # if rstring, value is "string"
            if type(obj) == type(""):
                strings.append({"key":key, "value": obj })
            elif type(obj) == omero.model.ImageI:
                images.append({"key":key, "name": obj.getName().getValue(), "id": obj.getId().getValue() })
            elif type(obj) == omero.model.DatasetI:
                resultMap['dataset'] = {"name": obj.getName().getValue(), "id": obj.getId().getValue()}
    resultMap['strings'] = strings
    images.sort(key=lambda i: i["id"])
    resultMap['images'] = images
    
    # html will give users links to any Image, stdout, stderr and any strings returned in results
    return render_to_response('webemdb/scripts/script_results.html', 
            {'message': message, 'resultMap': resultMap, 'stdout': stdout, 'stderr': stderr})
    
    
def script_form(request, scriptId):
    """
    Generates an html form for the parameters of a defined script. 
    """
    conn = getConnection(request)
    scriptService = conn.getScriptService()
    
    params = scriptService.getParams(long(scriptId))
    if params == None:
        return HttpResponse()
    
    paramData = {}
    
    paramData["id"] = long(scriptId)
    paramData["name"] = params.name.replace("_", " ")
    paramData["authors"] = ", ".join([a for a in params.authors])
    paramData["contact"] = params.contact
    paramData["version"] = params.version
    paramData["institutions"] = ", ".join([i for i in params.institutions])
    
    inputs = []     # use a list so we can sort by 'grouping'
    for key, param in params.inputs.items():
        i = {}
        i["name"] = key.replace("_", " ")
        i["key"] = key
        if not param.optional:
            i["required"] = True
        i["description"] = param.description
        if param.min:
            i["min"] = param.min.getValue()
        if param.max:
            i["max"] = param.max.getValue()
        if param.values:
            i["options"] = [v.getValue() for v in param.values.getValue()]
        pt = unwrap(param.prototype)
        if pt.__class__ == type(True):
            i["boolean"] = True
        i["prototype"] = unwrap(param.prototype)    # E.g  ""  (string) or [0] (int list) or 0.0 (float)
        i["grouping"] = param.grouping
        inputs.append(i)
    inputs.sort(key=lambda i: i["grouping"])
    paramData["inputs"] = inputs
        
    return render_to_response('webemdb/scripts/script_form.html', {'paramData': paramData})


def image(request, imageId):
    """
    Shows an image preview (single plane), Name, Description etc. links to datasets.  
    """
    conn = getConnection(request)

    image = conn.getImage(imageId)
    
    # enable the django template to access all parents of the image
    image.showAllParents = image.listParents(single=False)
    
    entryId = None
    
    scriptService = conn.getScriptService()
    scripts = []
    scriptNames = {"/EMAN2/Nonlinear_Anisotropic_Diffusion.py": "Nonlinear Anisotropic Diffusion",
            "/omero/figure_scripts/Movie_ROI_Figure.py": "Movie_ROI_Figure"}
    for path, display in scriptNames.items():
         scriptId = scriptService.getScriptID(path)
         if scriptId and scriptId > 0:
             s = {}
             s["name"] = display
             s["id"] = scriptId
             scripts.append(s)

    return render_to_response('webemdb/data/image.html', {'image': image, "scripts": scripts})
    

def dataset(request, datasetId):
    """
    Shows the thumbnails in a dataset, provides a link back to EMDB entry (project)
    """
    conn = getConnection(request)

    dataset = conn.getDataset(datasetId)
    
    entryId = None
    
    # look for parent project that has EMDB entry name (EMDB ID)
    for p in dataset.listParents(single = False):
        try:
            emdbId = long(p.getName())
            entryId = str(emdbId)
            break
        except:
            pass

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
        
    entryName = str(entryId)
    project = conn.findProject(entryName)
    
    if project == None:
        # project not found (None) handled by template
        return render_to_response('webemdb/entries/entry.html', {'project':project})
        
    # find the mrc map image. E.g emd_1003.map In a dataset named same as project E.g. 1003
    img = None
    mrcMap = None
    smallMap = None
    namespace = omero.constants.namespaces.NSCOMPANIONFILE 
    imgName = "emd_%s.map" % entryName
    smallMapName = "small_%s.map" % entryName
    for d in project.listChildren():
        if d.getName() == entryName:
            for i in d.listChildren():
                if i.getName() == imgName:
                    img = i
                    for a in img.listAnnotations():
                        if imgName == a.getFileName() and a.getNs() == namespace:
                            mrcMap = a
                        elif smallMapName == a.getFileName():
                            smallMap = a
                    break
    
    xml = None
    gif = None
    bit = None
    pdbs = []
    xmlName = "emd-%s.xml" % entryName
    gifName = "400_%s.gif" % entryName
    bitName = "%s.bit" % entryName
    sizeWarning = False
    
    for a in project.listAnnotations():
        try:    # not all annotations have getFileName() E.g. comment annotations
            if xmlName == a.getFileName():
                xml = a
            elif gifName == a.getFileName():
                gif = a
            elif bitName == a.getFileName():
                bit = a
                if bit.getFileSize() > 2000000:
                    sizeWarning = True
            elif a.getFileName().endswith(".pdb.gz"):
                pdbs.append(a)
        except:
            pass
    
    data = project.countChildren() > 1
    
    return render_to_response('webemdb/entries/entry.html', 
        {'project':project, 'xml': xml, 'gif': gif, 'img': img, 'map': mrcMap, 'smallMap': smallMap, 'bit': bit, 'pdbs': pdbs, 
            'sizeWarning':sizeWarning, 'data': data})
        
        
def oa_viewer(request, fileId): 
    """ Returns simply the <applet> element of Open Astex Viewer, for loading into another page. 
        The <applet> contains a script that will load a bit mask, identified by fileId """
        
    conn = getConnection(request)
    
    ann = conn.getFileAnnotation(long(fileId))
    # determine mapType by name
    mapType = "map"
    if ann:
        fileName = ann.getFileName()
        if fileName.endswith(".bit"):
            mapType = "bit"
            
    return render_to_response('webemdb/entries/oa_viewer.html', {'fileId': fileId, 'mapType': mapType})
    
    
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
        return getFile(request, gif.getId())
    else:
        return HttpResponse()


def getFile (request, fileId):
    """
    Gets the file by Id and returns it according to mime type for display.
    """
    conn = getConnection(request)
        
    # get the file by ID
    ann = None
    ann = conn.getFileAnnotation(long(fileId))

    # determine mime type to assign
    if ann:
        
        fileName = ann.getFileName()
        mimetype = "text/plain"
        
        if fileName.endswith(".bit") or fileName.endswith(".pdb.gz") or fileName.endswith(".map"): 
            mimetype='application/octet-stream'
        if fileName.endswith(".xml"): mimetype='text/xml'
        if fileName.endswith(".gif"): mimetype='image/gif'
        
        # file_data = ann.getFile()
        # return HttpResponse(file_data, mimetype=mimetype)
        
        # if the file data is large, we will have a temp file
        tempdir = settings.FILE_UPLOAD_TEMP_DIR
        temp = os.path.join(tempdir, ('%i-%s.download' % (ann.file.id.val, conn._sessionUuid))).replace('\\','/')
        logger.info("temp path: %s" % str(temp))
        f = open(str(temp),"wb")
        for piece in ann.getFileInChunks():
            f.write(piece)
        f.seek(0)
        f.close()
        
        originalFile_data = FileWrapper(file(temp))
            
        rsp = HttpResponse(originalFile_data)
               
        rsp['Content-Type'] = mimetype
        rsp['Content-Length'] = ann.getFileSize()
        # this tells the browser to give the user a 'download' dialog
        #rsp['Content-Disposition'] = 'attachment; filename=%s' % (ann.getFileName().replace(" ","_"))
            
        return rsp
        
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
    return HttpResponseRedirect(reverse('webemdb_loggedout'))

def loggedout (request):
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
    
    return HttpResponse(simplejson.dumps(pData), mimetype='application/javascript')
    
def getConnection(request):
    #print request.session.session_key
    #emdb_conn_key = "S:emdb#%s" % (request.session.get('server'))
    conn = getBlitzConnection(request, useragent="OMERO.webemdb")    
    if conn is None or not conn.isConnected() and request.REQUEST['server']:
        blitz = settings.SERVER_LIST.get(pk=1)
        request.session['server'] = blitz.id
        request.session['host'] = blitz.host
        request.session['port'] = blitz.port
        request.session['password'] = "ome"
        request.session['username'] = "emdb"
        #request.session['server'] = 'localhost'
        request.session.modified = True
        conn = getBlitzConnection (request, useragent="OMERO.webemdb")
    logger.debug('emdb connection: %s' % (conn._sessionUuid))
    #print type(conn), conn._sessionUuid #, emdb_conn_key
    return conn

def image_viewer (request, iid, **kwargs):
    """ This view is responsible for showing pixel data as images """
    
    conn = getBlitzConnection (request, useragent="OMERO.webemdb")
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webemdb_login'))
    
    kwargs['viewport_server'] = '/webclient'
    
    return webgateway_views.full_viewer(request, iid, _conn=conn, **kwargs)