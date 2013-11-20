from django.http import Http404, HttpResponse
from django.shortcuts import render_to_response
from django.utils import simplejson
from django.conf import settings
from datetime import datetime
import os
import shutil

from omeroweb.webgateway import views as webgateway_views
from omeroweb.webclient.views import run_script
from omero.rtypes import wrap, rlong
import omero

try:
    from PIL import Image
except ImportError:
    import Image
from cStringIO import StringIO

try:
    import hashlib
    hash_sha1 = hashlib.sha1
except:
    import sha
    hash_sha1 = sha.new

from omeroweb.webclient.decorators import login_required

JSON_FILEANN_NS = "omero.web.figure.json"


@login_required()
def index(request, conn=None, **kwargs):
    """
    Single page 'app' for creating a Figure, allowing you to choose images
    and lay them out in canvas by dragging & resizing etc
    """

    return render_to_response("webfigure/index.html", {})


@login_required(setGroupContext=True)
def save_web_figure(request, conn=None, **kwargs):
    """
    Saves 'figureJSON' in POST as an original file. If 'fileId' is specified
    in POST, then we update that file. Otherwise create a new one with
    name 'figureName' from POST.
    """

    if not request.method == 'POST':
        return HttpResponse("Need to use POST")

    figureJSON = request.POST.get('figureJSON')
    if figureJSON is None:
        return HttpResponse("No 'figureJSON' in POST")
    figureJSON = str(figureJSON)

    fileId = request.POST.get('fileId')

    if fileId is None:
        # Create new file
        figureName = request.POST.get('figureName')
        if figureName is None:
            n = datetime.now()
            # time-stamp name by default: WebFigure_2013-10-29_22-43-53.json
            figureName = "WebFigure_%s-%s-%s_%s-%s-%s.json" % \
                (n.year, n.month, n.day, n.hour, n.minute, n.second)
        else:
            figureName = str(figureName)
        fileSize = len(figureJSON)
        f = StringIO()
        f.write(figureJSON)
        origF = conn.createOriginalFileFromFileObj(f, '', figureName, fileSize)
        fa = omero.model.FileAnnotationI()
        fa.setFile(origF._obj)
        fa.setNs(wrap(JSON_FILEANN_NS))
        fa = conn.getUpdateService().saveAndReturnObject(fa)
        fileId = fa.id.val

    else:
        # Update existing Original File
        fa = conn.getObject("FileAnnotation", fileId)
        origFile = fa._obj.file
        size = len(figureJSON)
        print figureJSON
        print size
        origFile.setSize(rlong(size))
        # set sha1
        h = hash_sha1()
        h.update(figureJSON)
        shaHast = h.hexdigest()
        origFile.setHash(wrap(shaHast))
        origFile = conn.getUpdateService().saveAndReturnObject(origFile)
        # upload file
        rawFileStore = conn.createRawFileStore()
        rawFileStore.setFileId(origFile.getId().getValue())
        rawFileStore.write(figureJSON, 0, size)
        rawFileStore.close()

    return HttpResponse(str(fileId))


@login_required()
def load_web_figure(request, fileId, conn=None, **kwargs):
    """
    Loads the json stored in the file, identified by file annotation ID 
    """

    fileAnn = conn.getObject("FileAnnotation", fileId)
    jsonData = "".join(list(fileAnn.getFileInChunks()))

    return HttpResponse(jsonData, mimetype='json')



@login_required(setGroupContext=True)
def make_web_figure(request, conn=None, **kwargs):
    """
    Uses the scripting service to generate pdf via json etc in POST data.
    Script will show up in the 'Activities' for users to monitor and
    download result etc.
    """
    if not request.method == 'POST':
        return HttpResponse("Need to use POST")

    scriptService = conn.getScriptService()
    sId = scriptService.getScriptID("/webfigure_scripts/Figure_To_Pdf.py")

    pageWidth = int(request.POST.get('pageWidth'))
    pageHeight = int(request.POST.get('pageHeight'))
    panelsJSON = str(request.POST.get('panelsJSON'))

    inputMap = {
        'Page_Width': wrap(pageWidth),
        'Page_Height': wrap(pageHeight),
        'Panels_JSON': wrap(panelsJSON)}

    rsp = run_script(request, conn, sId, inputMap, scriptName='Web Figure.pdf')
    return HttpResponse(simplejson.dumps(rsp), mimetype='json')


@login_required()
def list_web_figures(request, conn=None, **kwargs):

    fileAnns = list( conn.getObjects("FileAnnotation", attributes={'ns': JSON_FILEANN_NS}) )
    fileAnns.sort(key=lambda x: x.creationEventDate(), reverse=True)

    rsp = []
    for fa in fileAnns:
        print dir(fa.creationEventDate())
        rsp.append({'id': fa.id,
            'name': fa.getFile().getName(),
            'creationDate': str(fa.creationEventDate())
        })

    return HttpResponse(simplejson.dumps(rsp), mimetype='json')
