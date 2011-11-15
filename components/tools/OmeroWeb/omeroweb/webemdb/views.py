from django.http import HttpResponseRedirect, HttpResponse, Http404
from django.core.urlresolvers import reverse
from django.shortcuts import render_to_response
from django.utils import simplejson
from django.core.servers.basehttp import FileWrapper
from django.conf import settings
from django.core.cache import cache 

from omeroweb.webgateway.views import getBlitzConnection, _session_logout
from omeroweb.webgateway import views as webgateway_views
from omeroweb.webadmin.custom_models import Server

import settings
import logging
import traceback
import omero
import omero.constants
import omero.scripts
from omero.rtypes import rstring, rint, rlong, robject, unwrap, rdouble
from omero.sys import Parameters, Filter
import omero.util.script_utils as scriptUtil

import os
from django.core.servers.basehttp import FileWrapper
import random
import math
from numpy import zeros     # numpy for doing local projections

logger = logging.getLogger('webemdb')

PUBLICATION_NAMESPACE = "openmicroscopy.org/omero/emdb/publication"
RESOLUTION_NAMESPACE = "openmicroscopy.org/omero/emdb/resolutionByAuthor"  
FITTED_PDB_NAMESPACE = "openmicroscopy.org/omero/emdb/fittedPDBEntry"       # distinguish fittedPDBEntries from other pdbEntries (docked)


EMAN2_IMPORTED = False
try:
    from EMAN2 import *
    EMAN2_IMPORTED = True
except:
    logger.warning("Failed to import EMAN2. Some features of webemdb will not be supported.")


def eman(request, imageId, **kwargs):
    
    if not EMAN2_IMPORTED:
        return HttpResponse("EMAN2 not found")
        
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
            if len(value) == 0: continue
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
                
    
    proc = scriptService.runScript(sId, inputMap, None)
    
    # E.g. ProcessCallback/4ab13b23-22c9-4b5f-9318-40f9a1acc4e9 -t:tcp -h 10.37.129.2 -p 53154:tcp -h 10.211.55.2 -p 53154:tcp -h 10.12.1.230 -p 53154
    request.session.modified = True     # allows us to modify session...
    i = 0
    while str(i) in request.session['processors']:
        i += 1
    key = str(i)
    request.session['processors'][key] = str(proc)
    
    # TODO - return the input map, to display what the user entered. 
    return render_to_response('webemdb/scripts/script_running.html', {'scriptName': scriptName, 'jobId': key})
    
    
def script_results(request, jobId):
    
    if jobId not in request.session['processors']:
        return HttpResponse("Results not found (may have already been returned)")
        
    request.session.modified = True     # allows us to modify session...
    procString = request.session['processors'][jobId]
    del request.session['processors'][jobId]   # delete this, since we cannot use it again to get the results
    
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
    paramData["description"] = params.description
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
        #print key, pt.__class__
        if pt.__class__ == type(True):
            i["boolean"] = True
        elif pt.__class__ == type(0) or pt.__class__ == type(long(0)):
            i["number"] = "number"  # will stop the user entering anything other than numbers. 
        elif pt.__class__ == type(float(0.0)):
            #print "Float!"
            i["number"] = "float"
        i["prototype"] = unwrap(param.prototype)    # E.g  ""  (string) or [0] (int list) or 0.0 (float)
        i["grouping"] = param.grouping
        inputs.append(i)
    inputs.sort(key=lambda i: i["grouping"])
    paramData["inputs"] = inputs
        
    return render_to_response('webemdb/scripts/script_form.html', {'paramData': paramData})


def dataset_stack(request, datasetId):
    """
    Downloads a dataset of single-plane images as a .mrc file
    """
    conn = getConnection(request)
    
    queryService = conn.getQueryService()
    rawPixelStore = conn.createRawPixelsStore()
    
    def getImagePlane(imageId):
        query_string = "select p from Pixels p join fetch p.image as i join fetch p.pixelsType where i.id='%s'" % imageId
        pixels = queryService.findByQuery(query_string, None)
        theZ, theC, theT = (0,0,0)
        bypassOriginalFile = True
        rawPixelStore.setPixelsId(pixels.getId().getValue(), bypassOriginalFile)
        plane2D = scriptUtil.downloadPlane(rawPixelStore, pixels, theZ, theC, theT)
        return plane2D
        
    dataset = conn.getObject("Dataset", datasetId)
    
    em = None
    for z, i in enumerate(dataset.listChildren()):
        plane =  getImagePlane(i.getId())
        e = EMNumPy.numpy2em(plane)
        
        if em == None:
            sizeY, sizeX = plane.shape
            sizeZ = dataset.countChildren()
            em = EMData(sizeY, sizeX, sizeZ)    # x,y,z or y,x,z ?
            
        em.insert_clip(e,(0,0,z))
     
    tempdir = settings.FILE_UPLOAD_TEMP_DIR
    tempMrc = os.path.join(tempdir, ('%sdataset_stack.mrc' % (conn._sessionUuid))).replace('\\','/')
    em.write_image(tempMrc)
    
    originalFile_data = FileWrapper(file(tempMrc))
    rsp = HttpResponse(originalFile_data)
    rsp['Content-Type'] = 'application/octet-stream'
    # this tells the browser to give the user a 'download' dialog
    rsp['Content-Disposition'] = 'attachment; filename=%s.mrc' % (dataset.getName().replace(" ","_"))
    return rsp
    
    
def projection(request, imageId, projkey):
    """ Simply add the projkey (intmean, intsum or intmax) to the request and delegate to webgateway render_image() """
    
    """ NB: Not sure if we should be modifying request.REQUEST since http://docs.djangoproject.com/en/1.1/ref/request-response/ 
    says 'All attributes except session should be considered read-only.'   """
    
    conn = getConnection(request)
    request.REQUEST.dicts += ({'p': projkey},)
    
    #from django.http import QueryDict
    #q = QueryDict('', mutable=True)
    #q.update({'p': projkey})
    #request.REQUEST.dicts += (q,)
    
    return webgateway_views.render_image(request, imageId, 0, 0)
    

def projection_axis(request, imageId, axis, get_slice=False):
    
    conn = getConnection(request)
    import time

    startTime = time.time()
    
    x_proj_key = "x_proj_%s" % imageId
    y_proj_key = "y_proj_%s" % imageId
    z_proj_key = "z_proj_%s" % imageId
    x_slice_key = "x_slice_%s" % imageId
    y_slice_key = "y_slice_%s" % imageId
    z_slice_key = "z_slice_%s" % imageId
    
    # see if we have cached the projection we need
    key = "%s_proj_%s" % (axis, imageId)
    if get_slice:
        key = "%s_slice_%s" % (axis, imageId)
    #print "\n\nchecking cache for array: %s" % key
    #print "    %s secs" % (time.time() - startTime)
    proj = cache.get(key)
    
    if proj == None:
        
        #print "creating cube of data..."
        #print "    %s secs" % (time.time() - startTime)
        rawPixelStore = conn.createRawPixelsStore()
        queryService = conn.getQueryService()

        query_string = "select p from Pixels p join fetch p.image as i join fetch p.pixelsType where i.id='%s'" % imageId
        pixels = queryService.findByQuery(query_string, None)

        theZ, theC, theT = (0,0,0)
        bypassOriginalFile = True
        rawPixelStore.setPixelsId(pixels.getId().getValue(), bypassOriginalFile)

        sizeX = pixels.getSizeX().getValue()
        sizeY = pixels.getSizeY().getValue()
        sizeZ = pixels.getSizeZ().getValue()
        
        # create a 3D numpy array, and populate it with data
        cube = zeros( (sizeZ,sizeY,sizeX) )
        theC, theT = (0,0)
        for theZ in range(sizeZ):
            plane2D = scriptUtil.downloadPlane(rawPixelStore, pixels, theZ, theC, theT)
            cube[sizeZ-theZ-1] = plane2D
            
        # do the 3 projections and 3 central slices while we have the cube - save projections, not cube
        #print "doing projection"
        #print "    %s secs" % (time.time() - startTime)
        
        proj_z = zeros( (sizeY,sizeX) )
        for z in range(sizeZ):
            zPlane = cube[z, :, :]
            proj_z += zPlane
        slice_z = cube[sizeZ/2, :, :]
        
        proj_x = zeros( (sizeZ,sizeY) )
        for x in range(sizeX):
            xPlane = cube[:, :, x]
            proj_x += xPlane
        slice_x = cube[:, :, sizeX/2]
        
        proj_y = zeros( (sizeZ,sizeX) )
        for y in range(sizeY):
            yPlane = cube[:, y, :]
            proj_y += yPlane
        slice_y = cube[:, sizeY/2, :]
            
        #print "setting cache"
        #print "    %s secs" % (time.time() - startTime)
        # save arrays to cache
        cache.set(x_proj_key, proj_x)
        cache.set(x_slice_key, slice_x)
        cache.set(y_proj_key, proj_y)
        cache.set(y_slice_key, slice_y)
        cache.set(z_proj_key, proj_z)
        cache.set(z_slice_key, slice_z)
    
        if axis == "z":
            if get_slice:
                proj = slice_z
            else:
                proj = proj_z

        elif axis == "x":
            if get_slice:
                proj = slice_x
            else:
                proj = proj_x

        elif axis == "y":
            if get_slice:
                proj = slice_y
            else:
                proj = proj_y
    
    #print "got 2D plane...", proj.shape
    #print "    %s secs" % (time.time() - startTime)
        
    tempdir = settings.FILE_UPLOAD_TEMP_DIR
    tempJpg = os.path.join(tempdir, ('%s.projection.jpg' % (conn._sessionUuid))).replace('\\','/')
    
    #import matplotlib.pyplot as plt
    #plt.savefig(tempJpg)
    
    #import scipy
    #scipy.misc.imsave(tempJpg, proj)
    
    em = EMNumPy.numpy2em(proj)
    em.write_image(tempJpg)
    
    originalFile_data = FileWrapper(file(tempJpg))
    rsp = HttpResponse(originalFile_data)
    rsp['Content-Type'] = "image/jpg"
    
    return rsp
    
    
def mapmodelemdb(request, entryId):
    """ 
    We need to work out the OMERO imageId for the emd_map for this entryId, then call mapmodel().
    Do this via project, named entryId etc...
    """
    conn = getConnection(request)
    
    entryName = str(entryId)
    project = conn.getObject("Project", attributes={'name':entryName})
    
    if project == None:
        raise Http404
        
    # find the mrc map image. E.g emd_1003.map In a dataset named same as project E.g. 1003
    imageMap = get_entry_map(project)
    return mapmodel(request, imageMap.getId(), entryId)
    
    
def get_entry_map(project):
    """
    For a project, named by entryId (E.g. "1003") this returns the image named "emd_1003.map" in a dataset named "1003"
    """
    entryName = project.getName()
    imgName = "emd_%s.map" % entryName
    for d in project.listChildren():
        if d.getName() == entryName:
            for i in d.listChildren():
                if i.getName() == imgName:
                    return i
               
                    
def mapmodel(request, imageId, entryId=None):
    """
       Shows an image projections, slices etc. 
    """
    
    conn = getConnection(request)
    
    image = conn.getObject("Image", imageId)
    
    z = image.getSizeZ()/2
    
    return render_to_response('webemdb/data/mapmodel.html', {'image': image, 'z':z, 'entryId': entryId})
    

def image(request, imageId):
    """
    Shows an image preview (single plane), Name, Description etc. links to datasets.  
    """
    conn = getConnection(request)
    
    image = conn.getObject("Image", imageId)
    
    scriptService = conn.getScriptService()
    scripts = []
    scriptNames = {"/EMAN2/Nonlinear_Anisotropic_Diffusion.py": "IMOD: Nonlinear Anisotropic Diffusion",
            "/EMAN2/Segger_Segmentation.py": "Segger: Segmentation",
            "/EMAN2/Eman_Filters.py": "EMAN2: Filtering",
            "/EMAN2/Ctf_Correction.py": "EMAN2: CTF Correction",
            "/EMAN2/Run_Spider_Procedure.py": "Spider: Run Procedure"}
    for path, display in scriptNames.items():
         scriptId = scriptService.getScriptID(path)
         if scriptId and scriptId > 0:
             s = {}
             s["name"] = display
             s["id"] = scriptId
             scripts.append(s)

    if not image:
        return render_to_response('webemdb/data/image.html', {'image': image, "scripts": scripts})
    default_z = image.getSizeZ()/2
    
    return render_to_response('webemdb/data/image.html', {'image': image, "scripts": scripts, "default_z": default_z})
    

def dataset(request, datasetId):
    """
    Shows the thumbnails in a dataset, provides a link back to EMDB entry (project)
    """
    conn = getConnection(request)

    dataset = conn.getObject("Dataset", datasetId)
    
    entryId = None
    
    # look for parent project that has EMDB entry name (EMDB ID)
    for p in dataset.listParents():
        try:
            emdbId = long(p.getName())
            entryId = str(emdbId)
            break
        except:
            pass
    
    # add some scripts that we can run on a dataset
    scriptService = conn.getScriptService()
    scripts = []
    scriptNames = {"/EMAN2/Nonlinear_Anisotropic_Diffusion.py": "IMOD: Nonlinear Anisotropic Diffusion",
            "/EMAN2/Segger_Segmentation.py": "Segger: Segmentation",
            "/EMAN2/Eman_Filters.py": "EMAN2: Filtering",
            "/EMAN2/Ctf_Correction.py": "EMAN2: CTF Correction",
            "/EMAN2/Run_Spider_Procedure.py": "Spider: Run Procedure"}
    for path, display in scriptNames.items():
         scriptId = scriptService.getScriptID(path)
         if scriptId and scriptId > 0:
             s = {}
             s["name"] = display
             s["id"] = scriptId
             scripts.append(s)

    # gets list of {"id":annotationId, "name":fileName, "text":fileText} for .spf files
    spfFiles = getSpfFiles(conn.getQueryService(), conn.createRawFileStore())
    
    return render_to_response('webemdb/data/dataset.html', {'dataset': dataset, 'entryId': entryId, 'scripts': scripts, 'spfFiles':spfFiles})


def data(request, entryId):
    conn = getConnection(request)

    entryName = str(entryId)
    project = conn.getObject("Project", attributes={'name':entryName})
    
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
    project = conn.getObject("Project", attributes={'name':entryName})
    
    if project == None:
        # project not found (None) handled by template
        return render_to_response('webemdb/entries/entry.html', {'project':project})
        
    # find the mrc map image. E.g emd_1003.map In a dataset named same as project E.g. 1003
    img = get_entry_map(project)
    mrcMap = None
    smallMap = None
    segFiles = []  # Segger file.seg
    namespace = omero.constants.namespaces.NSCOMPANIONFILE 
    smallMapName = "small_%s.map" % entryName
    if img:
        imgName = img.getName()
        for a in img.listAnnotations():
            if a.getFileName().endswith(".seg"):
                segFiles.append(a)
            elif imgName == a.getFileName() and a.getNs() == namespace:
                mrcMap = a
            elif smallMapName == a.getFileName():
                smallMap = a
    
    xml = None
    gif = None
    bit = None
    pdbs = []
    fittedPdbs = []
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
                if a.getNs() == FITTED_PDB_NAMESPACE:
                    fittedPdbs.append(a)
                else:
                    pdbs.append(a)
        except:
            pass
    
    data = project.countChildren() > 1
    
    return render_to_response('webemdb/entries/entry.html', 
        {'project':project, 'xml': xml, 'gif': gif, 'img': img, 'map': mrcMap, 'smallMap': smallMap, 'bit': bit, 'pdbs': pdbs, 
            'fittedPdbs': fittedPdbs, 'sizeWarning':sizeWarning, 'data': data, 'segFiles': segFiles})
        
        
def oa_viewer(request, fileId): 
    """ Returns simply the <applet> element of Open Astex Viewer, for loading into another page. 
        The <applet> contains a script that will load a bit mask, identified by fileId """
        
    conn = getConnection(request)
    
    ann = conn.getObject("Annotation", long(fileId))
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
    project = conn.getObject("Project", attributes={'name':entryName})
    
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
    ann = conn.getObject("Annotation", long(fileId))

    # determine mime type to assign
    if ann:
        
        fileName = ann.getFileName()
        mimetype = "text/plain"
        
        if fileName.endswith(".bit") or fileName.endswith(".pdb.gz") or fileName.endswith(".map") or fileName.endswith(".seg"): 
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
        rsp['Content-Disposition'] = 'attachment; filename=%s' % (ann.getFileName().replace(" ","_"))
            
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

def about (request):
    return render_to_response('webemdb/about.html', {})

def index (request):
    """ Show a selection of the latest EMDB entries """
    conn = getConnection(request)

    entryIds = [] # names of projects
    for p in conn.listProjects():
        try:
            # make sure we only list projects that are emdb entries "1001" etc
            int(p.getName())
            entryIds.append(p.getName())
        except: pass
    entryIds.sort()
    entryIds.reverse()
    # truncate. 
    lastIds = entryIds[:5]
    
    if len(entryIds) > 0:
        randomIds = [random.choice(entryIds) for i in range(10)]
    else: randomIds = []
    
    projects = []
    for entryName in lastIds:
        p = conn.getObject("Project", attributes={'name':entryName})
        rows = p.getDescription().split("\n")
        title = rows[0]
        if len(rows) > 1: sample = rows[1]
        else:  sample = ""
        e = {"id": entryName, "title": title, "sample": sample }
        projects.append(e)
    
    return render_to_response('webemdb/index.html', {'projects': projects, 'entryCount': len(entryIds), 'randomIds': randomIds})


def autocompleteQuery(request):
    """
    Returns json data for autocomplete. Search terms must be provided in the request "GET". 
    E.g. returns a list of ("1003": "Title") results for entries that start with numbers specified. 
    """
    conn = getConnection(request)
    qs = conn.getQueryService()
    
    # limit the number of results we return
    p = omero.sys.Parameters()
    p.map = {}
    f = omero.sys.Filter()
    f.limit = rint(15)
    f.offset = rint(0)
    p.theFilter = f
    
    projects = []
    
    entryText = request.REQUEST.get('entry')
    if entryText:
        query = "select p from Project as p where p.name like '%s%s' order by p.name" % (entryText, "%")    # like '123%'
        projects = qs.findAllByQuery(query, p)
    
    results = []
    
    for p in projects:
        entryId = p.getName().getValue()
        desc = p.getDescription().getValue()
        title, sample = desc.split("\n")
        results.append((entryId, title))
    
    return HttpResponse(simplejson.dumps(results), mimetype='application/javascript')


def search(request):
    
    conn = getConnection(request)
    
    qs = conn.getQueryService()
    import omero.model
    searchTerm = request.REQUEST.get('search')
    
    # set up pagination
    page = int(request.REQUEST.get('page', 1))  # 1-based
    resultsPerPage = 20
    #f.limit = rint(resultsPerPage * page)
    #f.offset = rint(resultsPerPage * (page-1))
    
    p = omero.sys.Parameters()
    p.map = {}
    f = omero.sys.Filter()
    f.limit = rint(5)
    f.offset = rint(0)
    p.theFilter = f
    
    results = []
    if searchTerm:
        projects = qs.findAllByFullText('Project', "file.contents:%s" % searchTerm, p)
        for p in projects:
            entryId = p.getName().getValue()
            desc = p.getDescription().getValue()
            title, sample = desc.split("\n")
            results.append({"entryId":entryId, "title": title, "sample": sample})
    
    
    return render_to_response('webemdb/browse/search.html', {'searchString': searchTerm, 'results': results, 'nextPage': page+1 })


def getSpfFiles(queryService, rawFileService):
    """ list original files ending with .spf and get text for each, but provide the annotation ID (same as Insight) 
        returns a list of (id, name, text)"""
    
    query = "select a from Annotation a join fetch a.file as f where f.name like '%.spf'"
    
    annotations = queryService.findAllByQuery(query, None)
    
    spfFiles = []
    
    for a in annotations:
        aId = a.getId().getValue()
        fileName = a.file.getName().getValue()
        fileId = a.file.getId().getValue()
        text = scriptUtil.readFromOriginalFile(rawFileService, queryService, fileId)
        spfFiles.append({"id": aId, "name": fileName, "text": text})
        
    return spfFiles

def entries (request):
    
    conn = getConnection(request)
    qs = conn.getQueryService()
    
    sortString = ""     # make a string of the current sort, to use in pagination links. E.g. &sort=entry&order=reverse
    sortBy = request.REQUEST.get('sort', 'entry')
    if sortBy == 'resolution':
        order = "a.doubleValue"
        sortString += "&sort=resolution"
    elif sortBy == 'title':
        order = "p.description"
        sortString += "&sort=title"
    elif sortBy == 'entry':
        order = "p.name"
        sortString += "&sort=entry"
        
    desc = ""
    if "reverse" == request.REQUEST.get('order'):
        desc = "desc"
        sortString += "&order=reverse"
    
    namespace = RESOLUTION_NAMESPACE
    # If I don't need to get Projects when there is no child annotation,
    # then you don't need "outer join" which is intended to return rows (here projects) where a joined table (annotations) are null.
    #"left outer join fetch p.annotationLinks as a_link " \
    query = "Project as p " \
                    "join fetch p.annotationLinks as a_link " \
                    "join fetch a_link.child as a " \
                    "where a.ns='%s'" % namespace
    
    # do a query for total count before we add restrictions. 
    totalEntries = qs.projection("select count(p.id) from " + query.replace(" fetch", ""), None)[0][0].getValue()
    
    searchString = ""      # build up a query and search string (for sort-links in the results page)
    minRes = request.REQUEST.get('min')
    maxRes = request.REQUEST.get('max')
    entryText = request.REQUEST.get('entry')
    titleText = request.REQUEST.get('title')
    if minRes and len(minRes) > 0:
        query += " and a.doubleValue >= %s" % minRes
        searchString += '&min=%s' % minRes
    if maxRes and len(maxRes) > 0:
        query += " and a.doubleValue <= %s" % maxRes
        searchString += '&max=%s' % maxRes
    if entryText and len(entryText) > 0:
        query += " and p.name like '%s%s'" % (entryText, "%")
        searchString += '&entry=%s' % entryText
    if titleText and len(titleText) > 0:
        query += " and p.description like '%s%s%s'" % ("%", titleText, "%")
        searchString += '&title=%s' % titleText
        
    page = int(request.REQUEST.get('page', 1))  # 1-based
    resultsPerPage = 20
        
    p = omero.sys.Parameters()
    p.map = {}
    f = omero.sys.Filter()
    f.limit = rint(resultsPerPage * page)
    f.offset = rint(resultsPerPage * (page-1))
    logger.debug("Entries page: %s" % page)
    logger.debug("Offset: %s  Limit: %s" % (f.offset.val, f.limit.val))
    p.theFilter = f
    
    # get the total number of results with no pagination. Returns a list of lists 
    # the inner list is of len==1 because you only asked for one column ("count(p.id)") and 
    # the outer list is of size one because you're just asking for the size of one thing.
    countQuery = "select count(p.id) from "+ query.replace("fetch ", "")    # don't fetch, since we don't want to load data
    tr = qs.projection(countQuery, None)[0][0]
    totalResults = tr.getValue()
    
    if order:
        query += " order by %s %s" % (order, desc)
    # do the search
    projectsQuery = "select p from " + query
    #totalResults = len(qs.findAllByQuery(projectsQuery, None))
    #print projectsQuery
    projects = qs.findAllByQuery(projectsQuery, p)
    logger.debug("Entries query returned %s projects" % len(projects) )
    
    resolutions = []
    
    resData = []
    
    for p in projects:
        entryId = p.getName().getValue()
        desc = p.getDescription().getValue()
        title, sample = desc.split("\n")
        for a in p.copyAnnotationLinks():
            r = a.child.getDoubleValue().getValue()
            break
        resData.append({"entryId":entryId, "title": title, "resolution": r, "sample": sample})
    
    pcount = int(math.ceil(float(totalResults)/resultsPerPage))
    pageLinks = range(1, pcount+1)
    
    return render_to_response('webemdb/browse/entries.html', {'totalResults': totalResults, 'pageLinks': pageLinks, 
        'totalEntries': totalEntries, 'sortString':sortString, 'resolutions': resData, 'sorted': sortBy,
        'searchString': searchString, "minRes": minRes, "maxRes": maxRes, "title": titleText})
    

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
        title, sample = desc.split("\n")
        pData[entryId] = sample
    
    return HttpResponse(simplejson.dumps(pData), mimetype='application/javascript')
    

def getConnection(request):
    
    conn = None
    if request.session.get('username', None):
        logger.debug('attempting to retrieve emdb connection with username:  %s' % request.session.get('username', None))
        conn = getBlitzConnection(request, useragent="OMERO.webemdb")  
        if not request.session.has_key('processors'):
            request.session['processors'] = {}
        if conn != None:
            logger.debug('emdb connection:  %s' % conn._sessionUuid)
    if conn == None:
        # session has timed out. Need to logout and log in again. 
        try:
            _session_logout(request, request.session['server'])
        except:
            import traceback
            logger.debug("Failed to log out %s" % traceback.format_exc())
        server_id = request.REQUEST.get('server',None) 
        if server_id is not None:
            blitz = Server.get(pk=int(server_id))
        else:
            blitz = Server.get(pk=1)
        logger.debug('attempting to connect emdb with blitz:  %s' % blitz)
        request.session['server'] = blitz.id
        request.session['host'] = blitz.host
        request.session['port'] = blitz.port
        request.session['password'] = "ome"
        request.session['username'] = "emdb"
        request.session['processors'] = {}
        request.session.modified = True
        conn = getBlitzConnection (request, useragent="OMERO.webemdb")
        
        logger.debug('emdb connection: %s server %s' % (conn._sessionUuid, blitz.host))
    return conn


def image_viewer (request, iid, **kwargs):
    """ This view is responsible for showing pixel data as images """
    
    conn = getBlitzConnection (request, useragent="OMERO.webemdb")
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webemdb_login'))
    
    kwargs['viewport_server'] = '/webclient'
    
    return webgateway_views.full_viewer(request, iid, _conn=conn, **kwargs)
