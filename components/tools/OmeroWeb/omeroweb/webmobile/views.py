from django.http import HttpResponse, HttpResponseRedirect
from django.core.urlresolvers import reverse
from django.shortcuts import render_to_response
from omeroweb.webgateway.views import getBlitzConnection, _session_logout
from omeroweb.webgateway import views as webgateway_views
from omeroweb.webadmin.custom_models import Server

import settings
import logging
import traceback
import omero
# use the webclient's gateway connection wrapper
from webclient.webclient_gateway import OmeroWebGateway
import webmobile_util

logger = logging.getLogger('webmobilewebmobile')
    
    
def isUserConnected (f):
    """ 
    connection decorator (wraps methods that require connection) - adapted from webclient.views
    retrieves connection and passes it to the wrapped method in kwargs
    TODO: would be nice to refactor isUserConnected from webclient to be usable from here. 
    """
    def wrapped (request, *args, **kwargs):
        #this check the connection exist, if not it will redirect to login page
        url = request.REQUEST.get('url')
        if url is None or len(url) == 0:
            if request.META.get('QUERY_STRING'):
                url = '%s?%s' % (request.META.get('PATH_INFO'), request.META.get('QUERY_STRING'))
            else:
                url = '%s' % (request.META.get('PATH_INFO'))
        
        conn = None
        loginUrl = reverse("webmobile_login")
        try:
            conn = getBlitzConnection(request, useragent="OMERO.webmobile")
        except Exception, x:
            logger.error(traceback.format_exc())
            return HttpResponseRedirect("%s?error=%s&url=%s" % (loginUrl, str(x), url))
            
        # if we failed to connect - redirect to login page, passing the destination url
        if conn is None:
            return HttpResponseRedirect("%s?url=%s" % (loginUrl, url))
        
        # if we got a connection, pass it to the wrapped method in kwargs
        kwargs["error"] = request.REQUEST.get('error')
        kwargs["conn"] = conn
        kwargs["url"] = url
        return f(request, *args, **kwargs)
    return wrapped


def groups_members(request):
    """
    List the users of the current group - if permitted
    """
    conn = getBlitzConnection (request, useragent="OMERO.webmobile")
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webmobile_login'))
    
    groupId = conn.getEventContext().groupId
    showMembers = True
    if str(conn.getEventContext().groupPermissions) == "rw----":
        showMembers = False
    members = conn.containedExperimenters(groupId)
    
    groups = []
    perms = {"rw----":'private', "rwr---":'read-only', "rwrw--":'collaborative'}
    for g in conn.getGroupsMemberOf():
        try:
            p = perms[str(g.getDetails().permissions)]
        except KeyError:
            p = ""
        groups.append({
            "id": g.id,
            "name": g.getName(),
            "permissions": p
        })
        
    return render_to_response('webmobile/groups_members.html', {'client': conn, 'showMembers': showMembers, 
        'members': members, 'groups': groups})
    
    
def switch_group(request, groupId):
    """
    Switch to the specified group, then redirect to index. 
    """
    conn = getBlitzConnection (request, useragent="OMERO.webmobile")
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webmobile_login'))
    
    from webclient.views import change_active_group
    try:
        #change_active_group(request, kwargs={'conn': conn})
        conn.changeActiveGroup(long(groupId))   # works except after viewing thumbnails in private group!
    except:
        logger.error(traceback.format_exc())
        return HttpResponse(traceback.format_exc())
        
    return HttpResponseRedirect(reverse('webmobile_index'))
 
 
@isUserConnected
def change_active_group(request, groupId, **kwargs):
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    url = reverse('webmobile_index')
    
    server = request.session.get('server')
    username = request.session.get('username')
    password = request.session.get('password')
    ssl = request.session.get('ssl')
    version = request.session.get('version')
       
    webgateway_views._session_logout(request, request.session.get('server'))
    
    blitz = Server.get(pk=server) 
    request.session['server'] = blitz.id
    request.session['host'] = blitz.host
    request.session['port'] = blitz.port
    request.session['username'] = username
    request.session['password'] = password
    request.session['ssl'] = (True, False)[request.REQUEST.get('ssl') is None]
    request.session['clipboard'] = {'images': None, 'datasets': None, 'plates': None}
    request.session['shares'] = dict()
    request.session['imageInBasket'] = set()
    blitz_host = "%s:%s" % (blitz.host, blitz.port)
    request.session['nav']={"error": None, "blitz": blitz_host, "menu": "start", "view": "icon", "basket": 0, "experimenter":None, 'callback':dict()}
    
    #conn = getBlitzConnection(request, useragent="OMERO.webmobile")

    if conn.changeActiveGroup(groupId):
        request.session.modified = True                
    else:
        error = 'You cannot change your group becuase the data is currently processing. You can force it by logging out and logging in again.'
        url = reverse("webindex")+ ("?error=%s" % error)
        if request.session.get('nav')['experimenter'] is not None:
            url += "&experimenter=%s" % request.session.get('nav')['experimenter']
    
    request.session['version'] = conn.getServerVersion()
    
    return HttpResponseRedirect(url)   
    
    
def viewer(request, imageId):
    conn = getBlitzConnection (request, useragent="OMERO.webmobile")
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webmobile_login'))
        
    image = conn.getObject("Image", imageId)
    w = image.getSizeX()
    h = image.getSizeY()
    
    return render_to_response('webmobile/viewers/viewer_iphone.html', {'image':image})
    

@isUserConnected
def viewer_big(request, imageId, **kwargs):
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return HttpResponse(traceback.format_exc())
        
    image = conn.getImage(imageId)
    w = image.getWidth() 
    h = image.getHeight() 
    z = image.z_count() /2
    
    return render_to_response('webmobile/viewers/big_iphone.html', {'image':image, 'w':w, 'h': h, 'z':z})
    
    
@isUserConnected
def projects (request, eid=None, **kwargs):
    """ List the projects owned by the current user, or another user specified by eId """
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return HttpResponse(traceback.format_exc())
        
    #projects = filter(lambda x: x.isOwned(), conn.listProjects())
    #eId = request.REQUEST.get('experimenter', None)
    experimenter = None
    if eid is not None:
        experimenter = conn.getObject("Experimenter", eid)
    else:
        # show current user's projects by default
        eid = conn.getEventContext().userId
        
    projs = conn.listProjects(eid=eid)
    projs = list(projs)
    
    if request.REQUEST.get('sort', None) == 'recent':
        projs.sort(key=lambda x: x.creationEventDate())
        projs.reverse()
    else:
        projs.sort(key=lambda x: x.getName().lower())
    ods = conn.listOrphans("Dataset", eid=eid)
    orphanedDatasets = list(ods)
    
    return render_to_response('webmobile/browse/projects.html',
        { 'client':conn, 'projects':projs, 'datasets':orphanedDatasets, 'experimenter':experimenter })


@isUserConnected
def project(request, id, **kwargs):
    """ Show datasets belonging to the specified project """
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return HttpResponse(traceback.format_exc())
        
    prj = conn.getObject("Project", id)
    return render_to_response('webmobile/browse/project.html', {'client':conn, 'project':prj})


@isUserConnected
def object_details(request, obj_type, id, **kwargs):
    """ Show project/dataset details: Name, description, owner, annotations etc """
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return HttpResponse(traceback.format_exc())
        
    if obj_type == 'project':
        obj = conn.getObject("Project", id)
        title = 'Project'
    elif obj_type == 'dataset':
        obj = conn.getObject("Dataset", id)
        title = 'Dataset'
    anns = getAnnotations(obj)
    
    parent = obj.getParent()
    
    return render_to_response('webmobile/browse/object_details.html', {'client': conn, 'object': obj, 'title': title, 
        'annotations':anns, 'obj_type': obj_type})


@isUserConnected
def dataset(request, id, **kwargs):
    """ Show images in the specified dataset """
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return HttpResponse(traceback.format_exc())
        
    ds = conn.getObject("Dataset", id)
    return render_to_response('webmobile/browse/dataset.html', {'client': conn, 'dataset': ds})
    
        
@isUserConnected
def image(request, imageId, **kwargs):
    """ Show image summary: Name, dimensions, large thumbnail, description, annotations """
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return HttpResponse(traceback.format_exc())
        
    img = conn.getObject("Image", imageId)
    anns = getAnnotations(img)
    
    return render_to_response('webmobile/browse/image.html', {'client': conn, 'object':img, 'obj_type':'image',
        'annotations': anns})
    
@isUserConnected
def orphaned_images(request, eid, **kwargs):
    """ Show image summary: Name, dimensions, large thumbnail, description, annotations """
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return HttpResponse(traceback.format_exc())
    
    orphans = conn.listOrphans("Image", eid=eid)
    return render_to_response('webmobile/browse/orphaned_images.html', {'client': conn, 'orphans':orphans})


@isUserConnected
def screens(request, eid=None, **kwargs):
    """  """
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return HttpResponse(traceback.format_exc())

    experimenter = None
    if eid is not None:
        experimenter = conn.getObject("Experimenter", eid)
    else:
        # show current user's screens by default
        eid = conn.getEventContext().userId
        
    scrs = conn.listScreens(eid=eid)
    
    if request.REQUEST.get('sort', None) == 'recent':
        scrs = list(scrs)
        scrs.sort(key=lambda x: x.creationEventDate())
        scrs.reverse()
        
    ops = conn.listOrphans("Plate", eid=eid)
    orphanedPlates = list(ops)
    
    return render_to_response('webmobile/browse/screens.html', 
        {'client':conn, 'screens':scrs, 'orphans':orphanedPlates, 'experimenter':experimenter })
 

@isUserConnected
def screen(request, id, **kwargs):
    """ Show plates in the specified scren """
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return HttpResponse(traceback.format_exc())
        
    scrn = conn.getObject("Screen", id)
    return render_to_response('webmobile/browse/screen.html', {'client': conn, 'screen': scrn})   


@isUserConnected
def plate(request, id, **kwargs):
    """ Show plate - grid of thumbs? """
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return HttpResponse(traceback.format_exc())
        
    scrn = conn.getObject("Screen", id)
    return render_to_response('webmobile/browse/screen.html', {'client': conn, 'screen': scrn})


def getAnnotations(obj):
    """ List the annotations and sort into comments, tags, ratings, files etc """
    
    comments = list()
    ratings = list()
    files = list()
    tags = list()
    
    from omero.model import CommentAnnotationI, LongAnnotationI, TagAnnotationI, FileAnnotationI
                            
    for ann in obj.listAnnotations():
        if isinstance(ann._obj, CommentAnnotationI):
            comments.append(ann)
        elif isinstance(ann._obj, LongAnnotationI):
            ratings.append(ann)
        elif isinstance(ann._obj, FileAnnotationI):
            files.append(ann)
        elif isinstance(ann._obj, TagAnnotationI):
            tags.append(ann)
            
    comments.sort(key=lambda x: x.creationEventDate())
    comments.reverse()
    
    return {"comments":comments, "ratings":ratings, "files":files, "tags":tags}


@isUserConnected
def edit_object(request, obj_type, obj_id, **kwargs):
    """
    Display a page for editing Name and Description of Project/Dataset/Image etc
    Page 'submit' redirects here with 'name' and 'description' in POST, which 
    will do the edit and return to the object_details page. 
    """
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return HttpResponse(traceback.format_exc())
    
    if obj_type == 'image': 
        obj = conn.getObject("Image", obj_id)
        title = 'Image'
        redirect = reverse('webmobile_image', kwargs={'imageId':obj_id})
    elif obj_type == 'dataset':
        obj = conn.getObject("Dataset", obj_id)
        title = 'Dataset'
        redirect = reverse('webmobile_dataset_details', kwargs={'id':obj_id})
    elif obj_type == 'project':
        obj = conn.getObject("Project", obj_id)
        title = 'Project'
        redirect = reverse('webmobile_project_details', kwargs={'id':obj_id})
        
    # if name, description in request, edit and redirect to object_details
    name = request.REQUEST.get('name', None)
    if name:
        obj.setName(name)
        description = request.REQUEST.get('description', '').strip()
        if len(description) == 0:
            description = None
        obj.setDescription(description)
        obj.save()
        return HttpResponseRedirect(redirect)
    
    return render_to_response('webmobile/browse/edit_object.html', {'client': conn, 'title':title, 'object':obj})
    

@isUserConnected
def add_comment(request, obj_type, obj_id, **kwargs):
    """
    Adds a comment (from request 'comment') to object 'project', 'dataset', 'image' then 
    redirects to the 'details' page for that object: E.g. project_details page etc. 
    """
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return HttpResponse(traceback.format_exc())
    
    from omero.rtypes import rstring
    
    redirect = reverse('webmobile_index')   # default
    if obj_type == 'image': 
        l = omero.model.ImageAnnotationLinkI()
        parent = omero.model.ImageI(obj_id, False)     # use unloaded object to avoid update conflicts
        redirect = reverse('webmobile_image', kwargs={'imageId':obj_id})
    elif obj_type == 'dataset':
        l = omero.model.DatasetAnnotationLinkI()
        parent = omero.model.DatasetI(obj_id, False)
        redirect = reverse('webmobile_dataset_details', kwargs={'id':obj_id})
    elif obj_type == 'project':
        l = omero.model.ProjectAnnotationLinkI()
        parent = omero.model.ProjectI(obj_id, False)
        redirect = reverse('webmobile_project_details', kwargs={'id':obj_id})
    
    comment = request.REQUEST.get('comment', None)
    if comment is None or (len(comment.strip()) == 0):
        return HttpResponseRedirect(redirect)
        
    updateService = conn.getUpdateService()
    ann = omero.model.CommentAnnotationI()
    comment = unicode(comment).encode("utf-8").strip()
    ann.setTextValue(rstring(comment))
    ann = updateService.saveAndReturnObject(ann)
    l.setParent(parent)
    l.setChild(ann)
    updateService.saveObject(l)
    
    return HttpResponseRedirect(redirect)


def login (request):
    if request.method == 'POST' and request.REQUEST['server']:
        blitz = Server.get(pk=request.REQUEST['server'])
        request.session['server'] = blitz.id
        request.session['host'] = blitz.host
        request.session['port'] = blitz.port
    
    conn = getBlitzConnection (request, useragent="OMERO.webmobile")
    logger.debug(conn)
    
    url = request.REQUEST.get("url")
    
    if conn is None:
        return render_to_response('webmobile/login.html', {'gw':Server, 'url': url})
        
    if url is not None and len(url) != 0:
        return HttpResponseRedirect(url)
    else:
        return HttpResponseRedirect(reverse('webmobile_index'))
    

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
    return HttpResponseRedirect(reverse('webmobile_login'))

@isUserConnected
def index (request, eid=None, **kwargs):
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return HttpResponse(traceback.format_exc())
    
    experimenter = None
    if eid is not None:
        experimenter = conn.getObject("Experimenter", eid)
      
    return render_to_response('webmobile/index.html', {'client': conn, 'experimenter': experimenter})


@isUserConnected
def recent (request, obj_type, eid=None, **kwargs):
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return HttpResponse(traceback.format_exc())
    
    experimenter = None
    if eid:
        experimenter = conn.getObject("Experimenter", eid)
        
    # By default, get 3 each of Projects, Datasets, Images, Ratings, Comments, Tags
    obj_count = 3   
    obj_types = None
    if obj_type == 'images':    # Get the last 12 images
        obj_types = ['Image']
        obj_count = 12
    elif obj_type == 'anns':    # 4 each of Tags, Comments, Rating
        obj_types = ['Annotation']
        obj_count = 4
    
    if obj_type == 'rois':
        recentResults = webmobile_util.listRois(conn, eid)
    else:
        recentItems = webmobile_util.listMostRecentObjects(conn, obj_count, obj_types, eid)
        recentResults = [ webmobile_util.RecentEvent(r) for r in recentItems ]
    
    # list members for links to other's recent activity
    groupId = conn.getEventContext().groupId
    members = conn.containedExperimenters(groupId)
        
    return render_to_response('webmobile/timeline/recent.html', {'client':conn, 'recent':recentResults, 
        'exp':experimenter, 'members':members, 'obj_type':str(obj_type) })

@isUserConnected
def recent_full_page (request, **kwargs):
    """
    Mock-up full page for Usability testing of recent views. 
    """
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return HttpResponse(traceback.format_exc())
    
    exp = conn.getObject("Experimenter", conn.getEventContext().userId)
        
    return render_to_response('webmobile/timeline/recent_full_page.html', {'client':conn, 'exp':exp })
    
    

@isUserConnected
def collab_annotations (request, myData=True, **kwargs):
    """
    Page displays recent annotations of OTHER users on MY data (myData=True) or
    MY annotations on data belonging to OTHER users. 
    """
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return HttpResponse(traceback.format_exc())
        
    collabAnns = webmobile_util.listCollabAnnotations(conn, myData)
    
    return render_to_response('webmobile/timeline/recent_collab.html', {'client':conn, 'recent':collabAnns, 'myData':myData })
    

def image_viewer (request, iid, **kwargs):
    """ This view is responsible for showing pixel data as images """
    
    conn = getBlitzConnection (request, useragent="OMERO.webmobile")
    if conn is None or not conn.isConnected():
        return HttpResponseRedirect(reverse('webmobile_login'))
    
    kwargs['viewport_server'] = '/webclient'
    
    return webgateway_views.full_viewer(request, iid, _conn=conn, **kwargs)
