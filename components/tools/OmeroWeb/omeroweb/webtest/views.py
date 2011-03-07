from django.http import HttpResponseRedirect
from django.core.urlresolvers import reverse
from django.shortcuts import render_to_response
from omeroweb.webgateway.views import getBlitzConnection, _session_logout
from omeroweb.webgateway import views as webgateway_views

import settings
import logging
import traceback
import omero
from omero.rtypes import rint
import omero.gateway

logger = logging.getLogger('webtest')    
    
def login (request):
    """
    Attempts to get a connection to the server by calling L{omeroweb.webgateway.views.getBlitzConnection} with the 'request'
    object. If a connection is created, the user is directed to the 'webfigure_index' page. 
    If a connection is not created, this method returns a login page.
    
    @param request:     The django http request
    @return:            The http response - webtest_index or login page
    """
    if request.method == 'POST' and request.REQUEST['server']:
        blitz = settings.SERVER_LIST.get(pk=request.REQUEST['server'])
        request.session['server'] = blitz.id
        request.session['host'] = blitz.host
        request.session['port'] = blitz.port
    
    conn = getBlitzConnection (request, useragent="OMERO.webtest")
    logger.debug(conn)
    if conn is not None:
        return HttpResponseRedirect(reverse('webtest_index'))
    return render_to_response('webtest/login.html', {'gw':settings.SERVER_LIST})


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
    
    #request.session.set_expiry(1)
    return HttpResponseRedirect(reverse('webtest_login'))

def index (request):
    conn = getBlitzConnection (request, useragent="OMERO.webtest")
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webtest_login'))

    return render_to_response('webtest/index.html', {'client': conn})

def metadata (request, iid):    
    from omeroweb.webclient.forms import MetadataFilterForm, MetadataDetectorForm, MetadataChannelForm, \
                        MetadataEnvironmentForm, MetadataObjectiveForm, MetadataStageLabelForm, \
                        MetadataLightSourceForm, MetadataDichroicForm, MetadataMicroscopeForm
                        
    conn = getBlitzConnection(request, useragent="OMERO.webtest")
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webtest_login'))
    
    form_environment = None
    form_objective = None
    form_microscope = None
    form_stageLabel = None
    form_filters = list()
    form_detectors = list()
    form_channels = list()
    form_lasers = list()
    
    image = conn.getImage(iid)
    om = image.loadOriginalMetadata()
    global_metadata = sorted(om[1])
    series_metadata = sorted(om[2])
        
        
    for ch in image.getChannels():
        if ch.getLogicalChannel() is not None:
            channel = dict()
            channel['form'] = MetadataChannelForm(initial={'logicalChannel': ch.getLogicalChannel(), 
                                    'illuminations': list(conn.getEnumerationEntries("IlluminationI")), 
                                    'contrastMethods': list(conn.getEnumerationEntries("ContrastMethodI")), 
                                    'modes': list(conn.getEnumerationEntries("AcquisitionModeI"))})
            channel['form_emission_filters'] = list()
            if ch.getLogicalChannel().getLightPath().copyEmissionFilters():
                for f in ch.getLogicalChannel().getLightPath().copyEmissionFilters():
                    channel['form_filters'].append(MetadataFilterForm(initial={'filter': f,
                                    'types':list(conn.getEnumerationEntries("FilterTypeI"))}))
            channel['form_excitation_filters'] = list()
            if ch.getLogicalChannel().getLightPath().copyExcitationFilters():
                for f in ch.getLogicalChannel().getLightPath().copyExcitationFilters():
                    channel['form_excitation_filters'].append(MetadataFilterForm(initial={'filter': f,
                                    'types':list(conn.getEnumerationEntries("FilterTypeI"))}))
                                    
            if ch.getLogicalChannel().getDetectorSettings()._obj is not None:
                channel['form_detector_settings'] = MetadataDetectorForm(initial={'detectorSettings':ch.getLogicalChannel().getDetectorSettings(), 'detector': ch.getLogicalChannel().getDetectorSettings().getDetector(),
                                    'types':list(conn.getEnumerationEntries("DetectorTypeI"))})
            if ch.getLogicalChannel().getLightSourceSettings()._obj is not None:      
                channel['form_light_source'] = MetadataLightSourceForm(initial={'lightSource': ch.getLogicalChannel().getLightSourceSettings(),
                                    'types':list(conn.getEnumerationEntries("FilterTypeI")), 
                                    'mediums': list(conn.getEnumerationEntries("LaserMediumI")),
                                    'pulses': list(conn.getEnumerationEntries("PulseI"))})
            if ch.getLogicalChannel().getFilterSet()._obj is not None and ch.getLogicalChannel().getFilterSet().getDichroic()._obj:
                channel['form_dichroic'] = MetadataDichroicForm(initial={'logicalchannel': ch.getLogicalChannel().getFilterSet().getDichroic()})
            channel['name'] = ch.getEmissionWave()
            channel['color'] = ch.getColor().getHtml()
            form_channels.append(channel)
            
    if image.getObjectiveSettings() is not None:
        form_objective = MetadataObjectiveForm(initial={'objectiveSettings': image.getObjectiveSettings(), 
                                'mediums': list(conn.getEnumerationEntries("MediumI")), 
                                'immersions': list(conn.getEnumerationEntries("ImmersionI")), 
                                'corrections': list(conn.getEnumerationEntries("CorrectionI")) })
    if image.getImagingEnvironment() is not None:
        form_environment = MetadataEnvironmentForm(initial={'image': image})
    if image.getStageLabel() is not None:
        form_stageLabel = MetadataStageLabelForm(initial={'image': image })

    if image.getInstrument() is not None:
        if image.getInstrument().getMicroscope() is not None:
            form_microscope = MetadataMicroscopeForm(initial={'microscopeTypes':list(conn.getEnumerationEntries("MicroscopeTypeI")), 'microscope': image.getInstrument().getMicroscope()})

        if image.getInstrument().getFilters() is not None:
            filters = list(image.getInstrument().getFilters())    
            for f in filters:
                form_filter = MetadataFilterForm(initial={'filter': f, 'types':list(conn.getEnumerationEntries("FilterTypeI"))})
                form_filters.append(form_filter)
    
        if image.getInstrument().getDetectors() is not None:
            detectors = list(image.getInstrument().getDetectors())    
            for d in detectors:
                form_detector = MetadataDetectorForm(initial={'detectorSettings':None, 'detector': d, 'types':list(conn.getEnumerationEntries("DetectorTypeI"))})
                form_detectors.append(form_detector)
    
        if image.getInstrument().getLightSources() is not None:
            lasers = list(image.getInstrument().getLightSources())
            for l in lasers:
                form_laser = MetadataLightSourceForm(initial={'lightSource': l, 
                                'types':list(conn.getEnumerationEntries("FilterTypeI")), 
                                'mediums': list(conn.getEnumerationEntries("LaserMediumI")),
                                'pulses': list(conn.getEnumerationEntries("PulseI"))})
                form_lasers.append(form_laser)

    # Annotations #
    text_annotations = list()
    long_annotations = {'rate': 0.00 , 'votes': 0}
    url_annotations = list()
    file_annotations = list()
    tag_annotations = list()
    
    from omero.model import CommentAnnotationI, LongAnnotationI, TagAnnotationI, FileAnnotationI
                            
    for ann in image.listAnnotations():
        if isinstance(ann._obj, CommentAnnotationI):
            text_annotations.append(ann)
        elif isinstance(ann._obj, LongAnnotationI):
            long_annotations['votes'] += 1
            long_annotations['rate'] += int(ann.longValue)
        elif isinstance(ann._obj, FileAnnotationI):
            file_annotations.append(ann)
        elif isinstance(ann._obj, TagAnnotationI):
            tag_annotations.append(ann)

    txannSize = len(text_annotations)
    urlannSize = len(url_annotations)
    fileannSize = len(file_annotations)
    tgannSize = len(tag_annotations)
    if long_annotations['votes'] > 0:
        long_annotations['rate'] /= long_annotations['votes']
    
    return render_to_response('webtest/metadata.html', {'image': image, 'text_annotations': text_annotations, 'txannSize':txannSize, 'long_annotations': long_annotations, 'url_annotations': url_annotations, 'urlannSize':urlannSize, 'file_annotations': file_annotations, 'fileannSize':fileannSize, 'tag_annotations': tag_annotations, 'tgannSize':tgannSize, 'global_metadata':global_metadata, 'serial_metadata':series_metadata, 'form_channels':form_channels, 'form_environment':form_environment, 'form_objective':form_objective, 'form_microscope':form_microscope, 'form_filters':form_filters, 'form_detectors':form_detectors, 'form_lasers':form_lasers, 'form_stageLabel':form_stageLabel})
    

def roi_viewer(request, roi_library, imageId):
    """
    Displays an image, using 'jquery.drawinglibrary.js' to draw ROIs on the image. 
    """
    conn = getBlitzConnection (request, useragent="OMERO.webroi")
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webfigure_login'))
    
    image = conn.getImage(imageId)
    default_z = image.z_count()/2
    
    templates = {"processing":'webtest/roi_viewers/processing_viewer.html',
            "jquery": "webtest/roi_viewers/jquery_drawing.html",
            "raphael":"webtest/roi_viewers/raphael_viewer.html"}
    
    template = templates[roi_library]
    
    return render_to_response(template, {'image':image, 'default_z':default_z})


def image_viewer (request, iid, **kwargs):
    """ This view is responsible for showing pixel data as images """
    
    conn = getBlitzConnection (request, useragent="OMERO.webtest")
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webtest_login'))
    
    kwargs['viewport_server'] = '/webclient'
    
    return webgateway_views.full_viewer(request, iid, _conn=conn, **kwargs)