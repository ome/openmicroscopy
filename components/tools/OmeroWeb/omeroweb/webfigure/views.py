from django.http import Http404, HttpResponse
from django.shortcuts import render_to_response
from django.utils import simplejson
from django.conf import settings
from datetime import datetime
import os
import shutil
import json

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
        description = {}
        if figureName is None:
            n = datetime.now()
            # time-stamp name by default: WebFigure_2013-10-29_22-43-53.json
            figureName = "WebFigure_%s-%s-%s_%s-%s-%s.json" % \
                (n.year, n.month, n.day, n.hour, n.minute, n.second)
        else:
            figureName = str(figureName)
        try:
            json_data = json.loads(figureJSON)
            firstImgId = json_data['panels'][0]['imageId']
            description['imageId'] = long(firstImgId);
        except:
            # Maybe give user warning that figure json is invalid?
            pass
        fileSize = len(figureJSON)
        f = StringIO()
        f.write(figureJSON)
        origF = conn.createOriginalFileFromFileObj(f, '', figureName, fileSize)
        fa = omero.model.FileAnnotationI()
        fa.setFile(origF._obj)
        fa.setNs(wrap(JSON_FILEANN_NS))
        desc = simplejson.dumps(description)
        fa.setDescription(wrap(desc))
        fa = conn.getUpdateService().saveAndReturnObject(fa)
        fileId = fa.id.val

    else:
        # Update existing Original File
        conn.SERVICE_OPTS.setOmeroGroup('-1')
        fa = conn.getObject("FileAnnotation", fileId)
        if fa is None:
            return Http404("Couldn't find FileAnnotation of ID: %s" % fileId)
        origFile = fa._obj.file
        size = len(figureJSON)
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
    if fileAnn is None:
        raise Http404("Figure File-Annotation %s not found" % fileId)
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
        owner = fa.getDetails().getOwner()

        figFile = {'id': fa.id,
            'name': fa.getFile().getName(),
            'creationDate': str(fa.creationEventDate()),
            'ownerFullName': owner.getFullName()
        }

        try:
            desc = fa.getDescription()
            description = json.loads(desc)
            figFile['description'] = description
        except:
            pass

        rsp.append(figFile)

    return HttpResponse(simplejson.dumps(rsp), mimetype='json')

@login_required()
def delete_web_figure(request, conn=None, **kwargs):
    """ POST 'fileId' to delete the FileAnnotation """

    if request.method != 'POST':
        return HttpResponse("Need to POST 'fileId' to delete")

    fileId = request.POST.get('fileId')
    # fileAnn = conn.getObject("FileAnnotation", fileId)
    conn.deleteObjects("Annotation", [fileId])
    return HttpResponse("Deleted OK")

