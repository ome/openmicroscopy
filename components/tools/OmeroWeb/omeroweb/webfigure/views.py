from django.http import Http404, HttpResponse
from django.shortcuts import render_to_response
from django.utils import simplejson
from django.conf import settings
import os
import shutil

from omeroweb.webgateway import views as webgateway_views
from omeroweb.webclient.views import run_script
from omero.rtypes import wrap

try:
    from PIL import Image
except ImportError:
    import Image
from cStringIO import StringIO

from omeroweb.webclient.decorators import login_required


@login_required()
def index (request, conn=None, **kwargs):
    """
    Single page 'app' for creating a Figure, allowing you to choose images and lay them
    out in canvas by dragging & resizing etc
    """

    return render_to_response("webfigure/index.html", {})


@login_required(setGroupContext=True)
def make_web_figure(request, conn=None, **kwargs):
    """
    Uses the scripting service to generate pdf via json etc in POST data.
    Script will show up in the 'Activities' for users to monitor and download result etc.
    """
    if not request.method == 'POST':
        return HttpResponse("Need to use POST")

    scriptService = conn.getScriptService()
    sId = scriptService.getScriptID("/webfigure_scripts/Figure_To_Pdf.py")

    pageWidth = int(request.POST.get('pageWidth'))
    pageHeight = int(request.POST.get('pageHeight'))
    panelsJSON = str(request.POST.get('panelsJSON'))

    inputMap = {'Page_Width': wrap(pageWidth),
            'Page_Height': wrap(pageHeight),
            'Panels_JSON': wrap(panelsJSON)}

    rsp = run_script(request, conn, sId, inputMap, scriptName='Create Web Figure.pdf')
    return HttpResponse(simplejson.dumps(rsp), mimetype='json')
