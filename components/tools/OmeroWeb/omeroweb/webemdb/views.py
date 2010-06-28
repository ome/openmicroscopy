from django.http import HttpResponseRedirect, HttpResponse
from django.core.urlresolvers import reverse
from django.shortcuts import render_to_response
from omeroweb.webgateway.views import getBlitzConnection, _session_logout
from omeroweb.webgateway import views as webgateway_views
import settings
import logging
import traceback
import omero

logger = logging.getLogger('webemdb')

def entry (request, entryId):
    conn = getBlitzConnection (request)
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webemdb_login'))
        
    entryName = str(entryId)
    project = conn.findProject(entryName)
    
    if project == None:
        # project not found (None) handled by template
        return render_to_response('webemdb/entries/entry.html', {'project':project})
        
    # find the mrc map image. E.g emd_1003.map In a dataset named same as project E.g. 1003
    img = None
    imgName = "emd_%s.map" % entryName
    for d in project.listChildren():
        if d.getName() == entryName:
            for i in d.listChildren():
                if i.getName() == imgName:
                    img = i
                    break
        
    #print dir(project)
    xml = None
    gif = None
    xmlName = "emd-%s.xml" % entryName
    gifName = "%s.gif" % entryName
    for a in project.listAnnotations():
        #print dir(a)
        #print a.getFileName()
        if xmlName == a.getFileName():
            xml = a
        if gifName == a.getFileName():
            gif = a
    
    #if xml:
        #print dir(xml)
        #print xml.getFile()
    return render_to_response('webemdb/entries/entry.html', {'project':project, 'xml': xml, 'gif': gif, 'img': img})
        
    
def gif (request, entryId, fileId):
    conn = getBlitzConnection (request)
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webemdb_login'))
        
    entryName = str(entryId)
    project = conn.findProject(entryName)
    
    if project == None: return HttpResponse()
        
    gif = None
    for a in project.listAnnotations():
        if a.id == long(fileId):
            gif = a
    
    if gif:
        gif_data = gif.getFile()
        return HttpResponse(gif_data, mimetype='image/gif')
    return HttpResponse()


def xml (request, entryId, fileId):
    conn = getBlitzConnection (request)
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webemdb_login'))
        
    entryName = str(entryId)
    project = conn.findProject(entryName)
    
    if project == None: return HttpResponse()
        
    xml = None
    for a in project.listAnnotations():
        if a.id == long(fileId):
            xml = a
    
    if xml:
        xml_data = xml.getFile()
        return HttpResponse(xml_data, mimetype='text/xml')
    return HttpResponse()
    
    
def login (request):
    if request.method == 'POST' and request.REQUEST['server']:
        blitz = settings.SERVER_LIST.get(pk=request.REQUEST['server'])
        request.session['server'] = blitz.id
        request.session['host'] = blitz.host
        request.session['port'] = blitz.port
    
    conn = getBlitzConnection (request)
    logger.debug(conn)
    if conn is not None:
        return HttpResponseRedirect(reverse('webemdb_index'))
    return render_to_response('webemdb/login.html', {'gw':settings.SERVER_LIST})

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
    return HttpResponseRedirect(reverse('webemdb_login'))

def index (request):
    conn = getBlitzConnection (request)
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webemdb_login'))

    entryIds = [] # names of projects
    for p in conn.listProjects():
        try:
            entryIds.append(int(p.getName()))
        except: pass
    entryIds.sort()
    entryIds.reverse()
    # truncate. 
    lastIds = entryIds[:10]
    print lastIds
    
    projects = []
    for entryName in lastIds:
        projects.append(conn.findProject(str(entryName)))
    
    return render_to_response('webemdb/index.html', {'projects': projects})

def metadata (request, iid):
    
    from omeroweb.webclient.forms import MetadataFilterForm, MetadataDetectorForm, MetadataChannelForm, \
                        MetadataEnvironmentForm, MetadataObjectiveForm, MetadataStageLabelForm, \
                        MetadataLightSourceForm, MetadataDichroicForm, MetadataMicroscopeForm
                        
    conn = getBlitzConnection (request)
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webemdb_login'))
    
    form_environment = None
    form_objective = None
    form_microscope = None
    form_stageLabel = None
    form_filters = list()
    form_detectors = list()
    form_channels = list()
    form_lasers = list()
    
    image = conn.getImage(iid)
    original_metadata = image.loadOriginalMetadata()
    for ch in image.getChannels():
        if ch.getLogicalChannel() is not None:
            channel = dict()
            channel['form'] = MetadataChannelForm(initial={'logicalChannel': ch.getLogicalChannel(), 
                                    'illuminations': list(conn.getEnumerationEntries("IlluminationI")), 
                                    'contrastMethods': list(conn.getEnumerationEntries("ContrastMethodI")), 
                                    'modes': list(conn.getEnumerationEntries("AcquisitionModeI"))})
            if ch.getLogicalChannel().getSecondaryEmissionFilter()._obj is not None:
                channel['form_emission_filter'] = MetadataFilterForm(initial={'filter': ch.getLogicalChannel().getSecondaryEmissionFilter(),
                                    'types':list(conn.getEnumerationEntries("FilterTypeI"))})
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
    
    return render_to_response('webemdb/metadata.html', {'image': image, 'text_annotations': text_annotations, 'txannSize':txannSize, 'long_annotations': long_annotations, 'url_annotations': url_annotations, 'urlannSize':urlannSize, 'file_annotations': file_annotations, 'fileannSize':fileannSize, 'tag_annotations': tag_annotations, 'tgannSize':tgannSize, 'global_metadata':original_metadata[0], 'serial_metadata':original_metadata[1], 'form_channels':form_channels, 'form_environment':form_environment, 'form_objective':form_objective, 'form_microscope':form_microscope, 'form_filters':form_filters, 'form_detectors':form_detectors, 'form_lasers':form_lasers, 'form_stageLabel':form_stageLabel})
    

def image_viewer (request, iid, **kwargs):
    """ This view is responsible for showing pixel data as images """
    
    conn = getBlitzConnection (request)
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webemdb_login'))
    
    kwargs['viewport_server'] = '/webclient'
    
    return webgateway_views.full_viewer(request, iid, _conn=conn, **kwargs)