#!/usr/bin/env python
# 
# 
# 
# Copyright (c) 2008 University of Dundee. 
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
# 
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
# 
# Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>, 2008.
# 
# Version: 1.0
#

''' A view functions is simply a Python function that takes a Web request and 
returns a Web response. This response can be the HTML contents of a Web page, 
or a redirect, or the 404 and 500 error, or an XML document, or an image... 
or anything.'''

import sys
import os
import calendar
import cStringIO
import datetime
import httplib
import Ice
import locale
import logging
import traceback

import omero
from time import time
from thread import start_new_thread

from django.conf import settings
from django.contrib.sessions.backends.db import SessionStore
from django.contrib.sessions.models import Session
from django.core import template_loader
from django.core.cache import cache
from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import render_to_response
from django.template import RequestContext as Context
from django.utils import simplejson
from django.views.defaults import page_not_found, server_error
from django.views import debug
from django.core.urlresolvers import reverse

from controller import sortByAttr
from controller.index import BaseIndex
from controller.annotation import BaseAnnotation
from controller.basket import BaseBasket
from controller.container import BaseContainer
from controller.help import BaseHelp
from controller.history import BaseCalendar
from controller.impexp import BaseImpexp
from controller.search import BaseSearch
from controller.share import BaseShare

from omeroweb.feedback.views import handlerInternalError
from omeroweb.webadmin.controller.experimenter import BaseExperimenter 
from omeroweb.webadmin.controller.uploadfile import BaseUploadFile

from webadmin.models import Gateway
from forms import ShareForm, ShareCommentForm, ContainerForm, CommentAnnotationForm, TagAnnotationForm, \
                    UriAnnotationForm, UploadFileForm, MyGroupsForm, MyUserForm, ActiveGroupForm, HistoryTypeForm, \
                    MetadataFilterForm, MetadataDetectorForm, \
                    MetadataEnvironmentForm, MetadataObjectiveForm, MetadataStageLabelForm, \
                    TagListForm, UrlListForm, CommentListForm, FileListForm, TagFilterForm
from omeroweb.webadmin.forms import MyAccountForm, MyAccountLdapForm, UploadPhotoForm, LoginForm

from omeroweb.webadmin.views import _session_logout
from omeroweb.webgateway.views import getBlitzConnection
from omeroweb.webgateway import views as webgateway_views
#from extlib.gateway import _session_logout, timeit, getBlitzConnection

#from extlib.gateway import BlitzGateway

logger = logging.getLogger('views-web')

connectors = {}
share_connectors = {}

logger.info("INIT '%s'" % os.getpid())

try:
    if settings.EMAIL_NOTIFICATION:
        import omeroweb.feedback.notification.handlesender as sender
        sender.handler()
except:
    logger.error(traceback.format_exc())


################################################################################
# Blitz Gateway Connection

def getShareConnection (request, share_id):
    browsersession_key = request.session.session_key
    share_conn_key = "S:%s#%s#%s" % (browsersession_key, request.session.get('server'), share_id)
    share = getBlitzConnection(request, force_key=share_conn_key)
    share.attachToShare(share_id)
    request.session['shares'][share_id] = share._sessionUuid
    request.session.modified = True
    logger.debug('shared connection: %s : %s' % (share_id, share._sessionUuid))
    return share

################################################################################
# decorators
def load_session_from_request(handler):
    """Read the session key from the GET/POST vars instead of the cookie.

    Centipedes, in my request headers?
    Yes! We sometimes receive the session key in the POST, because the
    multiple-file-uploader uses Flash to send the request, and the best Flash
    can do is grab our cookies from javascript and send them in the POST.
    """
    def func(request, *args, **kwargs):
        session_key = request.REQUEST.get(settings.SESSION_COOKIE_NAME, None)
        if not session_key:
            # TODO(rnk): Do something more sane like ask the user if their
            #            session is expired or some other weirdness.
            logger.error("Session key does not exist.")
            raise Http404()
        # This is how SessionMiddleware does it.
        session_engine = __import__(settings.SESSION_ENGINE, {}, {}, [''])
        try:
            request.session = session_engine.SessionStore(session_key)
        except Exception, e:
            logger.error(e)
            logger.error(traceback.format_exc())
            return html_error(e)
        logger.debug("Session from request loaded successfully.")
        return handler(request, *args, **kwargs)
    return func


def isUserConnected (f):
    def wrapped (request, *args, **kwargs):
        try:
            request.session['server'] = request.REQUEST['server']
        except:
            pass
        #this check connection exist, if not it will redirect to login page
        try:
            url = request.REQUEST['url']
        except:
            if request.META['QUERY_STRING']:
                url = '%s?%s' % (request.META['PATH_INFO'], request.META['QUERY_STRING'])
            else:
                url = '%s' % (request.META['PATH_INFO'])
        
        conn_share = None
        if kwargs.get('share_id', None) is not None:
            try:
                conn_share = getShareConnection(request, kwargs.get('share_id', None))
            except Exception, x:
                logger.error(traceback.format_exc())
                
        conn = None
        try:
            conn = getBlitzConnection(request)
        except KeyError:
            return HttpResponseRedirect(reverse("weblogin")+(("?url=%s") % (url)))
        except Exception, x:
            logger.error(traceback.format_exc())
            return HttpResponseRedirect(reverse("weblogin")+(("?error=%s&url=%s") % (x.__class__.__name__,url)))
        if conn is None:
            return HttpResponseRedirect(reverse("weblogin")+(("?url=%s") % (url)))
        
        sessionHelper(request)
        notification()
        kwargs["conn"] = conn
        kwargs["conn_share"] = conn_share
        kwargs["url"] = url
        return f(request, *args, **kwargs)
    
    return wrapped

def notification():
    try:
        if settings.EMAIL_NOTIFICATION:
            import omeroweb.feedback.notification.handlesender as sender
            sender.handler()
    except:
        logger.error(traceback.format_exc())

def sessionHelper(request):
    try:
        if request.session['clipboard']:
            pass
    except:
        request.session['clipboard'] = []
    try:
        if request.session['shares']:
            pass
    except:
        request.session['shares'] = dict()
    try:
        if request.session['imageInBasket']:
            pass
    except:
        request.session['imageInBasket'] = list()
    #try:
    #    if request.session['datasetInBasket']:
    #        pass
    #except:
    #    request.session['datasetInBasket'] = list()
    try:
        if request.session['nav']:
            pass
    except:
        blitz = Gateway.objects.get(pk=request.session['server'])
        blitz = "%s:%s" % (blitz.host, blitz.port)
        request.session['nav']={"blitz": blitz, "menu": "mydata", "whos": "mydata", "view": "table", "basket": 0}

################################################################################
# views controll

def login(request):
    if request.method == 'POST' and request.REQUEST['server']:
        blitz = Gateway.objects.get(pk=request.REQUEST['server'])
        request.session['server'] = blitz.id
        request.session['host'] = blitz.host
        request.session['port'] = blitz.port
        request.session['username'] = request.REQUEST['username'].encode('utf-8').strip()
        request.session['password'] = request.REQUEST['password'].encode('utf-8').strip()
        request.session['experimenter'] = None
        request.session['groupId'] = None
        request.session['clipboard'] = []
        request.session['shares'] = dict()
        request.session['imageInBasket'] = list()
        blitz_host = "%s:%s" % (blitz.host, blitz.port)
        request.session['nav']={"blitz": blitz_host, "menu": "start", "whos": "mydata", "view": "table", "basket": 0}
        
    try:
        error = request.REQUEST['error']
    except:
        error = None
    
    conn = None
    try:
        conn = getBlitzConnection(request)
    except Exception, x:
        error = x.__class__.__name__
    
    if conn is not None:
        url = None
        try:
            url = request.REQUEST["url"]
        except:
            pass
        if url is not None:
            return HttpResponseRedirect(url)
        else:
            return HttpResponseRedirect(reverse("webindex"))
    else:
        if request.method == 'POST' and request.REQUEST['server']:
            error = "Connection not available, please chceck your user name and password."
        url = None
        try:
            url = request.REQUEST["url"]
        except:
            pass
        
        try:
            request.session['server'] = request.REQUEST['server']
        except:
            pass
        
        template = "omeroweb/login.html"
        if request.method == 'POST':
            form = LoginForm(data=request.REQUEST.copy())
        else:
            try:
                blitz = Gateway.objects.filter(id=request.session['server'])
                try:
                    if request.session['username']:
                        data = {'server': unicode(blitz[0].id), 'username':unicode(request.session['username']) }
                        form = LoginForm(data=data)
                    else:
                        initial = {'server': unicode(blitz[0].id)}
                        form = LoginForm(initial=initial)
                except:
                    initial = {'server': unicode(blitz[0].id)}
                    form = LoginForm(initial=initial)
                if blitz:
                    initial = {'server': unicode(blitz[0].id)}
                    form = LoginForm(initial=initial)
                else:
                    form = LoginForm()
            except:
                form = LoginForm()
        if url is not None:
            context = {'url':url, 'error':error, 'form':form}
        else:
            context = {'error':error, 'form':form}
        
        t = template_loader.get_template(template)
        c = Context(request, context)
        rsp = t.render(c)
        return HttpResponse(rsp)

@isUserConnected
def index(request, **kwargs):
    template = "omeroweb/index.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    sessionHelper(request)
    
    try:
        if request.session['nav']['menu'] != 'start':
            request.session['nav']['menu'] = 'home'
            request.session.modified = True
    except:
        request.session['nav']['menu'] = 'start'
        request.session.modified = True
    
    controller = BaseIndex(conn)
    controller.loadData()
    form_active_group = ActiveGroupForm(initial={'activeGroup':controller.eContext['context'].groupId, 'mygroups': controller.eContext['allGroups']})
    
    context = {'nav':request.session['nav'], 'controller':controller, 'eContext': controller.eContext, 'form_active_group':form_active_group}

    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isUserConnected
def index_context(request, **kwargs):
    template = "omeroweb/index_context.html"
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    controller = BaseIndex(conn)
    controller.loadData()
    
    context = {'nav':request.session['nav'], 'controller':controller}
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isUserConnected
def index_last_imports(request, **kwargs):
    template = "omeroweb/index_last_imports.html"
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    controller = BaseIndex(conn)
    controller.loadLastAcquisitions()
    
    context = {'controller':controller, 'eContext': controller.eContext }
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isUserConnected
def index_most_recent(request, **kwargs):
    template = "omeroweb/index_most_recent.html"
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    controller = BaseIndex(conn)
    controller.loadMostRecent()
    
    context = {'controller':controller, 'eContext': controller.eContext }
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isUserConnected
def index_tag_cloud(request, **kwargs):
    template = "omeroweb/index_tag_cloud.html"
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    controller = BaseIndex(conn)
    controller.loadTagCloud()
    
    context = {'controller':controller, 'eContext': controller.eContext }
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isUserConnected
def change_active_group(request, **kwargs):
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    active_group = request.REQUEST['active_group']
    conn.changeActiveGroup(active_group)
    request.session['groupId'] = active_group
    return HttpResponseRedirect(reverse("webindex"))

@isUserConnected
def logout(request, **kwargs):
    _session_logout(request, request.session['server'])
#    conn = None
#    try:
#        conn = kwargs["conn"]
#    except:
#        logger.error(traceback.format_exc())
#        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    try:
        for key in request.session['shares'].iterkeys():
            try:
                session_key = "S:%s#%s#%s" % (request.session.session_key,request.session['server'], key)
                _session_logout(request, request.session['server'], force_key=session_key)
                #if share_connectors.has_key(session_key):
                #    share_connectors.get(session_key).seppuku()
                #    del share_connectors[session_key]
            except:
                logger.error(traceback.format_exc())
    except KeyError:
        pass
    
    try:
        del request.session['shares']
    except KeyError:
        logger.error(traceback.format_exc())
    
#    try:
#        session_key = "S:%s#%s" % (request.session.session_key,request.session['server'])
#        if connectors.has_key(session_key):
#            conn.seppuku()
#            del connectors[session_key]
#    except:
#        logger.error(traceback.format_exc())
    
    try:
        del request.session['server']
    except KeyError:
        logger.error(traceback.format_exc())
    try:
        del request.session['host']
    except KeyError:
        logger.error(traceback.format_exc())
    try:
        del request.session['port']
    except KeyError:
        logger.error(traceback.format_exc())
    try:
        del request.session['username']
    except KeyError:
        logger.error(traceback.format_exc())
    try:
        del request.session['password']
    except KeyError:
        logger.error(traceback.format_exc())
#    try:
#        del request.session['sessionUuid']
#    except KeyError:
#        logger.error(traceback.format_exc())
    try:
        del request.session['groupId']
    except KeyError:
        logger.error(traceback.format_exc())
    try:
        del request.session['experimenter']
    except KeyError:
        logger.error(traceback.format_exc())
        pass
    try:
        del request.session['imageInBasket']
    except KeyError:
        logger.error(traceback.format_exc())
    try:
        del request.session['nav']
    except KeyError:
        logger.error(traceback.format_exc())
    request.session.set_expiry(1)

    return HttpResponseRedirect(reverse("webindex"))


###########################################################################
# DATA MANAGEMENT request.session['nav']={"menu": "mydata", "whos": "mydata", "view": "table"}
@isUserConnected
def manage_data(request, whos, o1_type=None, o1_id=None, o2_type=None, o2_id=None, o3_type=None, o3_id=None, **kwargs):
    request.session['nav']['menu'] = whos
    request.session['nav']['whos'] = whos
    
    conn = None
    try:
        conn = kwargs["conn"]
        
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    url = None
    try:
        url = kwargs["url"]
    except:
        logger.error(traceback.format_exc())
    
    request.session.modified = True
    
    try:
        request.session['nav']['view'] = request.REQUEST['view'] # table, icon, tree 
    except:
        pass
    
    try:
        page = int(request.REQUEST['page'])
    except:
        page = 1
    
    view = request.session['nav']['view']
    menu = request.session['nav']['menu']
    whos = request.session['nav']['whos']
    
    try:
        manager = BaseContainer(conn, o1_type, o1_id, o2_type, o2_id, o3_type, o3_id, metadata=True)
    except AttributeError, x:
        return handlerInternalError(x)
    manager.buildBreadcrumb(whos)
        
    form_active_group = ActiveGroupForm(initial={'activeGroup':manager.eContext['context'].groupId, 'mygroups': manager.eContext['allGroups']})
    
    form_users = None
    form_mygroups = None
    filter_user_id = None    
    filter_group_id = None
    
    if whos == 'userdata':
        '''grs = list()
        grs.extend(list(conn.getEventContext().memberOfGroups))
        #grs.extend(list(conn.getEventContext().leaderOfGroups))
        my_groups = sortByAttr(list(conn.getExperimenterGroups(set(grs))), "name")
        request.session['groupId'] = None
        form_mygroups = MyGroupsForm(initial={'mygroups': my_groups})'''
                
        users = sortByAttr(list(conn.getColleagues()), "lastName")

        try:
            if request.REQUEST['experimenter'] != "": 
                form_users = MyUserForm(initial={'users': users}, data=request.REQUEST.copy())
                if form_users.is_valid():
                    filter_user_id = request.REQUEST.get('experimenter', None)
                    request.session['experimenter'] = filter_user_id
                    form_users = MyUserForm(initial={'user':filter_user_id, 'users': users})
                else:
                    try:
                        filter_user_id = request.session.get('experimenter', None)
                    except:
                        pass
            else:
                request.session['experimenter'] = None
                form_users = MyUserForm(initial={'users': users})
        except:
            try:
                filter_user_id = request.session.get('experimenter', None)
                form_users = MyUserForm(initial={'user':filter_user_id, 'users': users})
            except:
                form_users = MyUserForm(initial={'users': users})
    elif whos == "groupdata":
        users = sortByAttr(list(conn.getColleagues()), "lastName")
        request.session['experimenter'] = None
        form_users = MyUserForm(initial={'users': users})

        grs = list()
        grs.extend(list(conn.getEventContext().memberOfGroups))
        #grs.extend(list(conn.getEventContext().leaderOfGroups))
        my_groups = sortByAttr(list(conn.getExperimenterGroups(set(grs))), "name")
        try:
            if request.REQUEST['group'] != "": 
                form_mygroups = MyGroupsForm(initial={'mygroups': my_groups}, data=request.REQUEST.copy())
                if form_mygroups.is_valid():
                    filter_group_id = request.REQUEST.get('groupId', None)
                    request.session['groupId'] = filter_group_id
                    form_mygroups = MyGroupsForm(initial={'mygroup':filter_group_id, 'mygroups': my_groups})
                else:
                    try:
                        filter_group_id = request.session.get('groupId', None)
                    except:
                        pass
            else:
                request.session['groupId'] = None
                form_mygroups = MyGroupsForm(initial={'mygroups': my_groups})
        except:
            try:
                filter_group_id = request.session.get('groupId', None)
                form_mygroups = MyGroupsForm(initial={'mygroup':filter_group_id, 'mygroups': my_groups})
            except:
                form_mygroups = MyGroupsForm(initial={'mygroups': my_groups})
    
    
    form_environment = None
    form_objective = None
    form_stageLabel = None
    form_filters = list()
    form_detectors = list()
    if o1_type =='image' or o2_type == 'image' or o3_type == 'image':
        manager.originalMetadata()
        manager.channelMetadata()
        
        form_objective = MetadataObjectiveForm(initial={'image': manager.image, 'mediums': list(conn.getEnumerationEntries("MediumI")), 'immersions': list(conn.getEnumerationEntries("ImmersionI")), 'corrections': list(conn.getEnumerationEntries("CorrectionI")) })
        if manager.image.getImagingEnvironment() is not None:
            form_environment = MetadataEnvironmentForm(initial={'image': manager.image})
        if manager.image.getStageLabel() is not None:
            form_stageLabel = MetadataStageLabelForm(initial={'image': manager.image })
        
        try:
            if manager.image.getMicroscopFilters().next() is not None:
                filters = list(manager.image.getMicroscopFilters())
            else:
                filters = list()
        except StopIteration:
            pass
        else:
            for f in filters:
                form_filter = MetadataFilterForm(initial={'filter': f, 'types':list(conn.getEnumerationEntries("FilterTypeI"))})
                form_filters.append(form_filter)
        try:
            if manager.image.getMicroscopDetectors().next() is not None:
                detectors = list(manager.image.getMicroscopDetectors())
            else:
                detectors = list()
        except StopIteration:
            pass
        else:
            for d in detectors:
                form_detector = MetadataDetectorForm(initial={'detector': d, 'types':list(conn.getEnumerationEntries("DetectorTypeI"))})
                form_detectors.append(form_detector)

    template = None
    if o3_type and o3_id:
        if o3_type == 'image':
            template = "omeroweb/image_details.html"
    elif o2_type and o2_id:
        if o2_type == 'dataset':
            if filter_user_id is not None:
                manager.listImagesInDatasetAsUser(o2_id, filter_user_id, page)
            elif filter_group_id is not None:
                manager.listImagesInDatasetInGroup(o2_id, filter_group_id, page)
            elif whos == "mydata":
                manager.listMyImagesInDataset(o2_id, page)
        elif o2_type == 'image':
            template = "omeroweb/image_details.html"
    elif o1_type and o1_id:
        if o1_type == 'ajaxdataset':
            template = "omeroweb/container_subtree.html"
            if filter_user_id is not None:
                manager.loadUserImages(o1_id, filter_user_id)
            elif filter_group_id is not None:
                manager.loadGroupImages(o1_id, filter_group_id)
            elif whos == "mydata":
                manager.loadMyImages(o1_id)
        elif o1_type == 'project':
            if filter_user_id is not None:
                manager.listDatasetsInProjectAsUser(o1_id, filter_user_id, page)
            elif filter_group_id is not None:
                manager.listDatasetsInProjectInGroup(o1_id, filter_group_id, page)
            elif whos == "mydata":
                manager.listMyDatasetsInProject(o1_id, page)
        #elif o1_type == 'screen':
        #    if filter_user_id is not None:
        #        manager.listPlatesInScreenAsUser(o1_id, filter_user_id, page)
        #    elif filter_group_id is not None:
        #        manager.listPlatesInScreenInGroup(o1_id, filter_group_id, page)
        #    elif whos == "mydata":
        #        manager.listMyPlatesInScreen(o1_id, page)
        elif o1_type == 'dataset':
            if filter_user_id is not None:
                manager.listImagesInDatasetAsUser(o1_id, filter_user_id, page)
            elif filter_group_id is not None:
                manager.listImagesInDatasetInGroup(o1_id, filter_group_id, page)
            elif whos == "mydata":
                manager.listMyImagesInDataset(o1_id, page)
        elif o1_type == 'image':
            template = "omeroweb/image_details.html"
    elif o1_type == 'orphaned':
        if filter_user_id is not None:
            manager.loadUserOrphanedImages(filter_user_id)
        elif filter_group_id is not None:
            manager.loadGroupOrphanedImages(filter_group_id)
        elif whos == "mydata":
            manager.loadMyOrphanedImages()
    elif o1_type == 'ajaxorphaned':
        template = "omeroweb/container_subtree.html"
        if filter_user_id is not None:
            manager.loadUserOrphanedImages(filter_user_id)
        elif filter_group_id is not None:
            manager.loadGroupOrphanedImages(filter_group_id)
        elif whos == "mydata":
            manager.loadMyOrphanedImages()
    else:
        if view == 'tree':
            if filter_user_id is not None:
                manager.loadUserContainerHierarchy(filter_user_id)
            elif filter_group_id is not None:
                manager.loadGroupContainerHierarchy(filter_group_id)
            elif whos == "mydata":
                manager.loadMyContainerHierarchy()
        else:
            if filter_user_id is not None:
                manager.listRootsAsUser(filter_user_id)
            elif filter_group_id is not None:
                manager.listRootsInGroup(filter_group_id)
            elif whos == "mydata":
                manager.listMyRoots()
    
    form_comment = None
    form_tag = None
    form_uri = None
    form_file = None
    
    form_tags = None
    form_urls = None
    form_comments = None
    form_files = None
    
    tag_list = manager.listTags()
    comment_list = manager.listComments()
    url_list = manager.listUrls()
    file_list = manager.listFiles()
    
    try:
        action = request.REQUEST["action"]
    except:
        if o1_type and o1_id:
            form_comment = CommentAnnotationForm()
            form_tag = TagAnnotationForm()
            form_uri = UriAnnotationForm(initial={'link':'http://'})
            form_file = UploadFileForm()
            
            form_tags = TagListForm(initial={'tags':tag_list})
            form_urls = UrlListForm(initial={'urls':url_list})
            form_comments = CommentListForm(initial={'comments':comment_list})
            form_files = FileListForm(initial={'files':file_list})
    else:
        if action == "comment":
            form_comment = CommentAnnotationForm(data=request.REQUEST.copy())
            if form_comment.is_valid():
                content = request.REQUEST['content'].encode('utf-8')
                if o3_type and o3_id:
                    if o3_type == 'image':
                        manager.createImageCommentAnnotation(content)
                elif o2_type and o2_id:
                    if o2_type == 'dataset':
                        manager.createDatasetCommentAnnotation(content)
                    elif o2_type == 'image':
                        manager.createImageCommentAnnotation(content)
                    #elif o1_type == 'plate':
                    #    manager.createPlateCommentAnnotation(content)
                elif o1_type and o1_id:
                    if o1_type == 'project':
                        manager.createProjectCommentAnnotation(content)
                    elif o1_type == 'dataset':
                        manager.createDatasetCommentAnnotation(content)
                    elif o1_type == 'image':
                        manager.createImageCommentAnnotation(content)
                    #elif o1_type == 'screen':
                    #    manager.createScreenCommentAnnotation(content)
                form_comment = CommentAnnotationForm()
            form_tag = TagAnnotationForm()
            form_uri = UriAnnotationForm(initial={'link':'http://'})
            form_file = UploadFileForm()
            
            form_tags = TagListForm(initial={'tags':tag_list})
            form_urls = UrlListForm(initial={'urls':url_list})
            form_comments = CommentListForm(initial={'comments':comment_list})
            form_files = FileListForm(initial={'files':file_list})
        elif action == "url":
            form_uri = UriAnnotationForm(data=request.REQUEST.copy())
            if form_uri.is_valid():
                content = request.REQUEST['link'].encode('utf-8')
                if o3_type and o3_id:
                    if o3_type == 'image':
                        manager.createImageUriAnnotation(content)
                elif o2_type and o2_id:
                    if o2_type == 'dataset':
                        manager.createDatasetUriAnnotation(content)
                    elif o2_type == 'image':
                        manager.createImageUriAnnotation(content)
                    #elif o2_type == 'plate':
                    #    manager.createPlateUriAnnotation(content)
                elif o1_type and o1_id:
                    if o1_type == 'project':
                        manager.createProjectUriAnnotation(content)
                    elif o1_type == 'dataset':
                        manager.createDatasetUriAnnotation(content)
                    elif o1_type == 'image':
                        manager.createImageUriAnnotation(content)
                    #elif o1_type == 'screen':
                    #    manager.createScreenUriAnnotation(content)
                form_uri = UriAnnotationForm(initial={'link':'http://'})
            form_tag = TagAnnotationForm()
            form_comment = CommentAnnotationForm()
            form_file = UploadFileForm()
            
            form_tags = TagListForm(initial={'tags':tag_list})
            form_urls = UrlListForm(initial={'urls':url_list})
            form_comments = CommentListForm(initial={'comments':comment_list})
            form_files = FileListForm(initial={'files':file_list})
        elif action == "tag":
            form_tag = TagAnnotationForm(data=request.REQUEST.copy())
            if form_tag.is_valid():
                tag = request.REQUEST['tag'].encode('utf-8')
                desc = request.REQUEST['description'].encode('utf-8')
                if o3_type and o3_id:
                    if o3_type == 'image':
                        manager.createImageTagAnnotation(tag, desc)
                elif o2_type and o2_id:
                    if o2_type == 'dataset':
                        manager.createDatasetTagAnnotation(tag, desc)
                    elif o2_type == 'image':
                        manager.createImageTagAnnotation(tag, desc)
                    #elif o2_type == 'plate':
                    #    manager.createPlateTagAnnotation(tag, desc)
                elif o1_type and o1_id:
                    if o1_type == 'project':
                        manager.createProjectTagAnnotation(tag, desc)
                    elif o1_type == 'dataset':
                        manager.createDatasetTagAnnotation(tag, desc)
                    elif o1_type == 'image':
                        manager.createImageTagAnnotation(tag, desc)
                    #elif o1_type == 'screen':
                    #    manager.createScreenTagAnnotation(tag, desc)
                form_tag = TagAnnotationForm()
            form_uri = UriAnnotationForm(initial={'link':'http://'})
            form_comment = CommentAnnotationForm()
            form_file = UploadFileForm()
            
            form_tags = TagListForm(initial={'tags':tag_list})
            form_urls = UrlListForm(initial={'urls':url_list})
            form_comments = CommentListForm(initial={'comments':comment_list})
            form_files = FileListForm(initial={'files':file_list})
        elif action == "file":
            if request.method == 'POST':
                form_file = UploadFileForm(request.POST, request.FILES)
                if form_file.is_valid():
                    f = request.FILES['annotation_file']
                    if o3_type and o3_id:
                        if o3_type == 'image':
                            manager.createImageFileAnnotation(f)
                    elif o2_type and o2_id:
                        if o2_type == 'dataset':
                            manager.createDatasetFileAnnotation(f)
                        elif o2_type == 'image':
                            manager.createImageFileAnnotation(f)
                        #elif o2_type == 'plate':
                        #    manager.createPlateFileAnnotation(f)
                    elif o1_type and o1_id:
                        if o1_type == 'project':
                            manager.createProjectFileAnnotation(f)
                        elif o1_type == 'dataset':
                            manager.createDatasetFileAnnotation(f)
                        elif o1_type == 'image':
                            manager.createImageFileAnnotation(f)
                        #elif o1_type == 'screen':
                        #    manager.createScreenFileAnnotation(f)
                    form_file = UploadFileForm()
            form_tag = TagAnnotationForm()
            form_comment = CommentAnnotationForm()
            form_uri = UriAnnotationForm(initial={'link':'http://'})
            
            form_tags = TagListForm(initial={'tags':tag_list})
            form_urls = UrlListForm(initial={'urls':url_list})
            form_comments = CommentListForm(initial={'comments':comment_list})
            form_files = FileListForm(initial={'files':file_list})
        elif action == "usetag":
            if request.method == 'POST':
                form_tags = TagListForm(data=request.REQUEST.copy(), initial={'tags':tag_list})
                if form_tags.is_valid():
                    tags = request.POST.getlist('tags')
                    if o3_type and o3_id:
                        if o3_type == 'image':
                            manager.createImageAnnotationLinks('tag',tags)
                    elif o2_type and o2_id:
                        if o2_type == 'dataset':
                            manager.createDatasetAnnotationLinks('tag',tags)
                        elif o2_type == 'image':
                            manager.createImageAnnotationLinks('tag',tags)
                        #elif o2_type == 'plate':
                        #    manager.createPlateAnnotationLinks('tag',tags)
                    elif o1_type and o1_id:
                        if o1_type == 'project':
                            manager.createProjectAnnotationLinks('tag',tags)
                        elif o1_type == 'dataset':
                            manager.createDatasetAnnotationLinks('tag',tags)
                        elif o1_type == 'image':
                            manager.createImageAnnotationLinks('tag',tags)
                        #elif o1_type == 'screen':
                        #    manager.createScreenAnnotationLinks('tag',tags)
                    form_tags = TagListForm(initial={'tags':manager.listTags()})
            
            form_urls = UrlListForm(initial={'urls':url_list})
            form_comments = CommentListForm(initial={'comments':comment_list})
            form_files = FileListForm(initial={'files':file_list})
                    
            form_tag = TagAnnotationForm()
            form_comment = CommentAnnotationForm()
            form_uri = UriAnnotationForm(initial={'link':'http://'})
            form_file = UploadFileForm()
        
        elif action == "usecomment":
            if request.method == 'POST':
                form_comments = CommentListForm(data=request.REQUEST.copy(), initial={'comments':comment_list})
                if form_comments.is_valid():
                    comments = request.POST.getlist('comments')
                    if o3_type and o3_id:
                        if o3_type == 'image':
                            manager.createImageAnnotationLinks('comment',comments)
                    elif o2_type and o2_id:
                        if o2_type == 'dataset':
                            manager.createDatasetAnnotationLinks('comment',comments)
                        elif o2_type == 'image':
                            manager.createImageAnnotationLinks('comment',comments)
                        #elif o2_type == 'plate':
                        #    manager.createPlateAnnotationLinks('comment',comments)
                    elif o1_type and o1_id:
                        if o1_type == 'project':
                            manager.createProjectAnnotationLinks('comment',comments)
                        elif o1_type == 'dataset':
                            manager.createDatasetAnnotationLinks('comment',comments)
                        elif o1_type == 'image':
                            manager.createImageAnnotationLinks('comment',comments)
                        #elif o1_type == 'screen':
                        #    manager.createScreenAnnotationLinks('comment',comments)
                    form_comments = CommentListForm(initial={'comments':manager.listComments()})
            
            form_tags = TagListForm(initial={'tags':tag_list})
            form_urls = UrlListForm(initial={'urls':url_list})
            form_files = FileListForm(initial={'files':file_list})
                    
            form_tag = TagAnnotationForm()
            form_comment = CommentAnnotationForm()
            form_uri = UriAnnotationForm(initial={'link':'http://'})
            form_file = UploadFileForm()
            
        elif action == "useurl":
            if request.method == 'POST':
                form_urls = UrlListForm(data=request.REQUEST.copy(), initial={'urls':url_list})
                if form_urls.is_valid():
                    urls = request.POST.getlist('urls')
                    if o3_type and o3_id:
                        if o3_type == 'image':
                            manager.createImageAnnotationLinks('url',urls)
                    elif o2_type and o2_id:
                        if o2_type == 'dataset':
                            manager.createDatasetAnnotationLinks('url',urls)
                        elif o2_type == 'image':
                            manager.createImageAnnotationLinks('url',urls)
                        #elif o2_type == 'plate':
                        #    manager.createPlateAnnotationLinks('url',urls)
                    elif o1_type and o1_id:
                        if o1_type == 'project':
                            manager.createProjectAnnotationLinks('url',urls)
                        elif o1_type == 'dataset':
                            manager.createDatasetAnnotationLinks('url',urls)
                        elif o1_type == 'image':
                            manager.createImageAnnotationLinks('url',urls)
                        #elif o1_type == 'screen':
                        #    manager.createScreenAnnotationLinks('url',urls)
                    form_urls = UrlListForm(initial={'urls':manager.listUrls()})
                    
            form_comments = CommentListForm(initial={'comments':comment_list})
            form_tags = TagListForm(initial={'tags':tag_list})
            form_files = FileListForm(initial={'files':file_list})
                    
            form_tag = TagAnnotationForm()
            form_comment = CommentAnnotationForm()
            form_uri = UriAnnotationForm(initial={'link':'http://'})
            form_file = UploadFileForm()
        
        elif action == "usefile":
            if request.method == 'POST':
                form_files = FileListForm(data=request.REQUEST.copy(), initial={'files':file_list})
                if form_files.is_valid():
                    files = request.POST.getlist('files')
                    if o3_type and o3_id:
                        if o3_type == 'image':
                            manager.createImageAnnotationLinks('file',files)
                    elif o2_type and o2_id:
                        if o2_type == 'dataset':
                            manager.createDatasetAnnotationLinks('file',files)
                        elif o2_type == 'image':
                            manager.createImageAnnotationLinks('file',files)
                        #elif o2_type == 'plate':
                        #    manager.createPlateAnnotationLinks('file',files)
                    elif o1_type and o1_id:
                        if o1_type == 'project':
                            manager.createProjectAnnotationLinks('file',files)
                        elif o1_type == 'dataset':
                            manager.createDatasetAnnotationLinks('file',files)
                        elif o1_type == 'image':
                            manager.createImageAnnotationLinks('file',files)
                        #elif o1_type == 'screen':
                        #    manager.createScreenAnnotationLinks('file',files)
                    form_files = FileListForm(initial={'files':manager.listFiles()})
            
            form_comments = CommentListForm(initial={'comments':comment_list})
            form_tags = TagListForm(initial={'tags':tag_list})
            form_urls = UrlListForm(initial={'urls':url_list})
            
            form_tag = TagAnnotationForm()
            form_comment = CommentAnnotationForm()
            form_uri = UriAnnotationForm(initial={'link':'http://'})
            form_file = UploadFileForm()
    
    if template is None and view =='icon':
        template = "omeroweb/containers_icon.html"
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_comment':form_comment, 'form_uri':form_uri, 'form_tag':form_tag, 'form_file':form_file, 'form_users':form_users, 'form_mygroups':form_mygroups, 'form_active_group':form_active_group, 'form_tags':form_tags, 'form_comments':form_comments, 'form_urls':form_urls, 'form_files':form_files}
    elif template is None and view =='table':
        template = "omeroweb/containers_table.html"
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_comment':form_comment, 'form_uri':form_uri, 'form_tag':form_tag, 'form_file':form_file, 'form_users':form_users, 'form_mygroups':form_mygroups, 'form_active_group':form_active_group, 'form_tags':form_tags, 'form_comments':form_comments, 'form_urls':form_urls, 'form_files':form_files}
    elif template is None and view =='tree' and o1_type is not None and o1_id > 0:
        template = "omeroweb/containers_table.html"
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_comment':form_comment, 'form_uri':form_uri, 'form_tag':form_tag, 'form_file':form_file, 'form_active_group':form_active_group, 'form_tags':form_tags, 'form_comments':form_comments, 'form_urls':form_urls, 'form_files':form_files}
    elif template is None and view =='tree' and o1_type is None and o1_id is None:
        template = "omeroweb/containers_tree.html"
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_comment':form_comment, 'form_uri':form_uri, 'form_tag':form_tag, 'form_file':form_file, 'form_users':form_users, 'form_mygroups':form_mygroups, 'form_active_group':form_active_group, 'form_tags':form_tags, 'form_comments':form_comments, 'form_urls':form_urls, 'form_files':form_files}
    elif template is not None and view == 'tree' and o1_type=='ajaxdataset' and o1_id > 0:
        context = {'manager':manager, 'eContext':manager.eContext}
    elif template is not None and view == 'tree' and o1_type=='ajaxorphaned':
        context = {'manager':manager, 'eContext':manager.eContext}
    else:
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_comment':form_comment, 'form_uri':form_uri, 'form_tag':form_tag, 'form_file':form_file, 'form_active_group':form_active_group, 'form_environment':form_environment, 'form_objective':form_objective, 'form_filters':form_filters, 'form_detectors':form_detectors, 'form_stageLabel':form_stageLabel, 'form_tags':form_tags, 'form_comments':form_comments, 'form_urls':form_urls, 'form_files':form_files}
    
    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))

@isUserConnected
def manage_data_by_tag(request, tid=None, tid2=None, tid3=None, tid4=None, tid5=None, **kwargs):
    request.session['nav']['menu'] = 'mydata'
    request.session['nav']['whos'] = 'mydata'
    
    request.session.modified = True

    template = "omeroweb/tag.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
        
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    url = None
    try:
        url = kwargs["url"]
    except:
        logger.error(traceback.format_exc())
    
    view = request.session['nav']['view']
    menu = request.session['nav']['menu']
    whos = request.session['nav']['whos']
    
    form_filter = None
    tags = list()
    tag_list = list()
    
    if request.method == 'POST':
        form_filter = TagFilterForm(data=request.REQUEST.copy())
        if form_filter.is_valid():
            if request.REQUEST['tag'] != "":
                tags.append(str(request.REQUEST['tag']))
            if request.REQUEST['tag2'] != "":
                tags.append(str(request.REQUEST['tag2']))
            if request.REQUEST['tag3'] != "":
                tags.append(str(request.REQUEST['tag3']))
            if request.REQUEST['tag4'] != "":
                tags.append(str(request.REQUEST['tag4']))
            if request.REQUEST['tag5'] != "":
                tags.append(str(request.REQUEST['tag5']))
    else:
        tag_ids = list()
        
        if tid is not None:
            tag_ids.append(long(tid))
        if tid2 is not None:
            tag_ids.append(long(tid2))
        if tid3 is not None:
            tag_ids.append(long(tid3))
        if tid4 is not None:
            tag_ids.append(long(tid4))
        if tid5 is not None:
            tag_ids.append(long(tid5))
        
        tag_list = list()
        if len(tag_ids) > 0:
            tag_list = list(conn.listSpecifiedTags(tag_ids))
        initail = {}
        for i in range(1,len(tag_list)+1):
            val = tag_list[i-1].textValue
            if i == 1:
                initail['tag'] = val
            else:
                initail['tag%i' % i] = val
            tags.append(val)
        form_filter = TagFilterForm(initial=initail)
    
    try:
        manager = BaseContainer(conn, tags=tag_list, rtags=tags)
    except AttributeError, x:
        return handlerInternalError(x)
    if len(tags) > 0:
        manager.loadDataByTag()
    else:
        pass
    
    if request.method == 'POST':
        viewargs = []
        for t in manager.tags:
            if t is not None:
                viewargs.append(t.id)

        return HttpResponseRedirect(reverse(viewname="manage_data_by_tag", args=viewargs))
    else:
        manager.buildBreadcrumb(whos)
    
    form_active_group = ActiveGroupForm(initial={'activeGroup':manager.eContext['context'].groupId, 'mygroups': manager.eContext['allGroups']})
    
    if form_filter is None:
        form_filter = TagFilterForm()
    
    context = {'url':url, 'nav':request.session['nav'], 'eContext':manager.eContext, 'manager':manager, 'form_active_group':form_active_group, 'form_filter':form_filter}
    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def autocomplete_tags(request, **kwargs):
    conn = None
    try:
        conn = kwargs["conn"]
        
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    json_data = simplejson.dumps(list(conn.lookupTags()))
    return HttpResponse(json_data, mimetype='application/javascript')

@isUserConnected
def manage_annotations(request, o_type, o_id, **kwargs):
    conn = None
    try:
        conn = kwargs["conn"]
        
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    url = None
    try:
        url = kwargs["url"]
    except:
        logger.error(traceback.format_exc())
    
    t = None
    try:
        t = request.REQUEST['t']
    except:
        pass
    
    manager = BaseContainer(conn, o_type, o_id)
    if t == "zoom":
        template = "omeroweb/annotations_zoom.html"
    elif o_type == 'project':
        template = "omeroweb/annotations.html"
    elif o_type == "dataset":
        template = "omeroweb/annotations.html"
    elif o_type == "image":
        template = "omeroweb/annotations.html"
    else:
        return handlerInternalError("Annotations cannot be displayed for - %s (id:%s)." % (o_type, o_id))
    
    manager.annotationList()
    
    context = {'url':url, 'manager':manager}

    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))
    
@isUserConnected
def manage_tree_details(request, c_type, c_id, **kwargs):
    template = "omeroweb/container_tree_details.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
        
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    url = None
    try:
        url = kwargs["url"]
    except:
        logger.error(traceback.format_exc())
    
    whos = request.session['nav']['whos']
    
    try:
        manager = BaseContainer(conn, c_type, c_id)
    except AttributeError, x:
        return handlerInternalError(x)
    
    context = {'url':url, 'nav':request.session['nav'], 'eContext':manager.eContext, 'manager':manager}

    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def manage_container_hierarchies(request, o_type=None, o_id=None, **kwargs):
    template = "omeroweb/hierarchy.html"
    conn = None
    try:
        conn = kwargs["conn"]
        
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    whos = request.session['nav']['whos']
    
    try:
        manager = BaseContainer(conn, o_type, o_id)
    except AttributeError, x:
        return handlerInternalError(x)
    manager.loadHierarchies()
    
    context = {'nav':request.session['nav'], 'eContext':manager.eContext, 'manager':manager}

    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))
    

###########################################################################
# ACTIONS

@isUserConnected
def manage_metadata(request, o_type, o_id, **kwargs):
    conn = None
    try:
        conn = kwargs["conn"]
        
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    matadataType = request.REQUEST['matadataType']
    metadataValue = request.REQUEST['metadataValue']
    
    try:
        manager = BaseContainer(conn, o_type, o_id, metadata=True)
    except AttributeError, x:
        return handlerInternalError(x)
    manager.saveMetadata(matadataType, metadataValue)
    
    return HttpResponse()

@isUserConnected
def manage_action_containers(request, action, o_type=None, o_id=None, **kwargs):
    template = None
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    url = None
    try:
        url = kwargs["url"]
    except:
        logger.error(traceback.format_exc())
    
    manager = None
    if o_type == "dataset" or o_type == "project" or o_type == "image": # or o_type == "screen" or o_type == "plate":
        try:
            manager = BaseContainer(conn, o_type, o_id)
        except AttributeError, x:
            return handlerInternalError(x)
        manager.buildBreadcrumb(action)
    elif o_type == "comment" or o_type == "url" or o_type == "tag":
        manager = BaseAnnotation(conn, o_type, o_id)
        manager.buildBreadcrumb(action)
    else:
        manager = BaseContainer(conn)
        manager.buildBreadcrumb(action)
    
    form_active_group = ActiveGroupForm(initial={'activeGroup':manager.eContext['context'].groupId, 'mygroups': manager.eContext['allGroups']})
    
    form = None
    if action == 'new':
        template = "omeroweb/container_new.html"
        #form = ContainerForm(initial={'access_controll': '0'})
        form = ContainerForm()
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form':form, 'form_active_group':form_active_group}
    elif action == 'edit':
        if o_type == "dataset":
            template = "omeroweb/container_form.html"
            form = ContainerForm(initial={'name': manager.dataset.name, 'description':manager.dataset.description})
            context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form':form, 'form_active_group':form_active_group}
        elif o_type == "project":
            template = "omeroweb/container_form.html"
            form = ContainerForm(initial={'name': manager.project.name, 'description':manager.project.description})
            context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form':form, 'form_active_group':form_active_group}
        #elif o_type == "screen":
        #    template = "omeroweb/container_form.html"
        #    form = ContainerForm(initial={'name': manager.screen.name, 'description':manager.screen.description})
        #    context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form':form, 'form_active_group':form_active_group}
        #elif o_type == "plate":
        #    template = "omeroweb/container_form.html"
        #    form = ContainerForm(initial={'name': manager.plate.name, 'description':manager.plate.description})
        #    context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form':form, 'form_active_group':form_active_group}
        elif o_type =="image" and o_id > 0:
            template = "omeroweb/container_form.html"
            form = ContainerForm(initial={'name': manager.image.name, 'description':manager.image.description})
            context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form, 'form_active_group':form_active_group}
        elif o_type =="comment" and o_id > 0:
            template = "omeroweb/annotation_form.html"
            form = CommentAnnotationForm(initial={'content':manager.comment.textValue})
            context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form, 'form_active_group':form_active_group}
        elif o_type =="url" and o_id > 0:
            template = "omeroweb/annotation_form.html"
            form = UriAnnotationForm(initial={'link':manager.url.textValue})
            context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form, 'form_active_group':form_active_group}
        elif o_type =="tag" and o_id > 0:
            template = "omeroweb/annotation_form.html"
            form = TagAnnotationForm(initial={'tag':manager.tag.textValue, 'description':manager.tag.description})
            context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form, 'form_active_group':form_active_group}
    elif action == 'move':
        parent = request.REQUEST['parent'].split('-')
        source = request.REQUEST['source'].split('-')
        destination = request.REQUEST['destination'].split('-')
        try:
            if parent[1] == destination[1]:
                return HttpResponse("Error: Cannot move to the same place.")
        except :
            pass
        rv = manager.move(parent,source, destination)
        if rv:
            rv = "Error: %s" % rv
        return HttpResponse(rv)
    elif action == 'remove':
        parent = request.REQUEST['parent'].split('-')
        source = request.REQUEST['source'].split('-')
        try:
            manager.remove(parent,source)
        except Exception, x:
            logger.error(traceback.format_exc())
            raise x
        return HttpResponseRedirect(url)
    elif action == 'save':
        if o_type == "dataset":
            form = ContainerForm(data=request.REQUEST.copy())
            if form.is_valid():
                name = request.REQUEST['name'].encode('utf-8')
                description = request.REQUEST['description'].encode('utf-8')
                #permissions = request.REQUEST.getlist('access_controll')
                manager.updateDataset(name, description)
                return HttpResponseRedirect(url)
            else:
                template = "omeroweb/container_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form, 'form_active_group':form_active_group}
        elif o_type == "project":
            form = ContainerForm(data=request.REQUEST.copy())
            if form.is_valid():
                name = request.REQUEST['name'].encode('utf-8')
                description = request.REQUEST['description'].encode('utf-8')
                #permissions = request.REQUEST.getlist('access_controll')
                manager.updateProject(name, description)
                return HttpResponseRedirect(url)
            else:
                template = "omeroweb/container_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form, 'form_active_group':form_active_group}
        #elif o_type == "screen":
        #    form = ContainerForm(data=request.REQUEST.copy())
        #    if form.is_valid():
        #        name = request.REQUEST['name'].encode('utf-8')
        #        description = request.REQUEST['description'].encode('utf-8')
        #        #permissions = request.REQUEST.getlist('access_controll')
        #        manager.updateScreen(name, description)
        #        return HttpResponseRedirect(url)
        #    else:
        #        template = "omeroweb/container_form.html"
        #        context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form, 'form_active_group':form_active_group}
        #elif o_type == "plate":
        #    form = ContainerForm(data=request.REQUEST.copy())
        #    if form.is_valid():
        #        name = request.REQUEST['name'].encode('utf-8')
        #        description = request.REQUEST['description'].encode('utf-8')
        #        #permissions = request.REQUEST.getlist('access_controll')
        #        manager.updatePlate(name, description)
        #        return HttpResponseRedirect(url)
        #    else:
        #        template = "omeroweb/container_form.html"
        #        context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form, 'form_active_group':form_active_group}
        elif o_type == 'image':
            form = ContainerForm(data=request.REQUEST.copy())
            if form.is_valid():
                name = request.REQUEST['name'].encode('utf-8')
                description = request.REQUEST['description'].encode('utf-8')
                #permissions = request.REQUEST.getlist('access_controll')
                manager.updateImage(name, description)
                return HttpResponseRedirect(url)
            else:
                template = "omeroweb/container_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form, 'form_active_group':form_active_group}
        elif o_type == 'comment':
            form = CommentAnnotationForm(data=request.REQUEST.copy())
            if form.is_valid():
                content = request.REQUEST['content'].encode('utf-8')
                manager.saveCommentAnnotation(content)
                return HttpResponseRedirect(url)
            else:
                template = "omeroweb/container_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form, 'form_active_group':form_active_group}
        elif o_type == 'url':
            form = UriAnnotationForm(data=request.REQUEST.copy())
            if form.is_valid():
                content = request.REQUEST['link'].encode('utf-8')
                manager.saveUrlAnnotation(content)
                return HttpResponseRedirect(url)
            else:
                template = "omeroweb/container_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form, 'form_active_group':form_active_group}
        elif o_type == 'tag':
            form = TagAnnotationForm(data=request.REQUEST.copy())
            if form.is_valid():
                content = request.REQUEST['tag'].encode('utf-8')
                description = request.REQUEST['description'].encode('utf-8')
                manager.saveTagAnnotation(content, description)
                return HttpResponseRedirect(url)
            else:
                template = "omeroweb/container_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form, 'form_active_group':form_active_group}
    elif action == 'addnew':
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers"))
        if o_type == "project" and o_id > 0:
            form = ContainerForm(data=request.REQUEST.copy())
            if form.is_valid():
                name = request.REQUEST['name'].encode('utf-8')
                description = request.REQUEST['description'].encode('utf-8')
                #permissions = request.REQUEST.getlist('access_controll')
                manager.createDataset(name, description)
                return HttpResponseRedirect(url)
            else:
                template = "omeroweb/container_new.html"
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'form':form, 'form_active_group':form_active_group}
        else:
            if request.REQUEST['folder_type'] == "dataset":
                form = ContainerForm(data=request.REQUEST.copy())
                if form.is_valid():
                    name = request.REQUEST['name'].encode('utf-8')
                    description = request.REQUEST['description'].encode('utf-8')
                    #permissions = request.REQUEST.getlist('access_controll')
                    manager.createDataset(name, description)
                    return HttpResponseRedirect(url)
                else:
                    template = "omeroweb/container_new.html"
                    context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'form':form, 'form_active_group':form_active_group}
            elif request.REQUEST['folder_type'] == "project":
                form = ContainerForm(data=request.REQUEST.copy())
                if form.is_valid():
                    name = request.REQUEST['name'].encode('utf-8')
                    description = request.REQUEST['description'].encode('utf-8')
                    #permissions = request.REQUEST.getlist('access_controll')
                    manager.createProject(name, description)
                    return HttpResponseRedirect(url)
                else:
                    template = "omeroweb/container_new.html"
                    context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'form':form, 'form_active_group':form_active_group}
            #elif request.REQUEST['folder_type'] == "screen":
            #    form = ContainerForm(data=request.REQUEST.copy())
            #    if form.is_valid():
            #        name = request.REQUEST['name'].encode('utf-8')
            #        description = request.REQUEST['description'].encode('utf-8')
            #        #permissions = request.REQUEST.getlist('access_controll')
            #        manager.createScreen(name, description)
            #        return HttpResponseRedirect(url)
            #    else:
            #        template = "omeroweb/container_new.html"
            #        context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'form':form, 'form_active_group':form_active_group}
    elif action == 'delete':
        pass
    
    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def manage_image_zoom (request, iid, **kwargs):
    template = "omeroweb/image_zoom.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    url = None
    try:
        url = kwargs["url"]
    except:
        logger.error(traceback.format_exc())
    
    try:
        image = BaseContainer(conn, 'image', iid)
    except AttributeError, x:
        return handlerInternalError(x)
    
    if request.session['nav']['whos'] != 'mydata' and request.session['nav']['whos'] != 'userdata' and request.session['nav']['whos'] != 'groupdata':
        if image.image.details.owner.id.val == image.eContext['context'].userId:
            request.session['nav']['whos'] = 'mydata'
        elif image.image.details.group.id.val == image.eContext['context'].groupId:
            request.session['nav']['whos'] = 'groupdata'
        else:
            request.session['nav']['whos'] = 'userdata'
    if request.session['nav']['menu'] != 'userdata' and request.session['nav']['menu'] != 'mydata':
        if image.image.details.owner.id.val == image.eContext['context'].userId:
            request.session['nav']['menu'] = 'mydata'
        elif image.image.details.group.id.val == image.eContext['context'].groupId:
            request.session['nav']['menu'] = 'userdata'
        else:
            request.session['nav']['menu'] = 'userdata'
    
    context = {'url':url, 'nav':request.session['nav'], 'image':image}
    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def manage_annotation(request, action, iid, **kwargs):
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    annotation = BaseAnnotation(conn)
    
    if action == 'download':
        try:
            annotation.getFileAnnotation(iid)
        except Exception, x:
            logger.error(traceback.format_exc())
            return handlerInternalError("Cannot download annotation (id:%s)." % (iid))
        rsp = HttpResponse(annotation.originalFile_data)
        if annotation.originalFile_data is None:
            return handlerInternalError("Cannot download annotation (id:%s)." % (iid))
        if action == 'download':
            rsp['ContentType'] = 'application/octet-stream'
            rsp['Content-Disposition'] = 'attachment; filename=%s' % (annotation.annotation.file.name.val)
    else:
        return handlerInternalError("%s is not available." % action.title())
    return rsp

@isUserConnected
def manage_shares(request, **kwargs):
    request.session['nav']['menu'] = 'share'
    request.session['nav']['whos'] = 'share'
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    try:
        request.session['nav']['view'] = request.REQUEST['view'] # table, icon, tree 
    except:
        pass
    
    request.session.modified = True

    view = request.session['nav']['view']

    if view == 'icon':
        template = "omeroweb/shares_icon.html"
    else: # view == 'table':
        template = "omeroweb/shares_table.html"
    
    controller = BaseShare(conn=conn, menu=request.session['nav']['menu'])
    controller.getShares()
    form_active_group = ActiveGroupForm(initial={'activeGroup':controller.eContext['context'].groupId, 'mygroups': controller.eContext['allGroups']})
    
    context = {'nav':request.session['nav'], 'eContext':controller.eContext, 'share':controller, 'form_active_group':form_active_group}
    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def manage_share(request, action, sid=None, **kwargs):
    request.session['nav']['menu'] = 'share'
    request.session['nav']['whos'] = 'share'
    
    request.session.modified = True
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    url = None
    try:
        url = kwargs["url"]
    except:
        logger.error(traceback.format_exc())
    
    try:
        share = BaseShare(request.session['nav']['menu'], conn, None, sid, action)
    except AttributeError, x:
        return handlerInternalError(x)
    form_active_group = ActiveGroupForm(initial={'activeGroup':share.eContext['context'].groupId, 'mygroups': share.eContext['allGroups']})
    
    experimenters = list(conn.getExperimenters())
    context = None
    form = None
    if action == "create":
        template = "omeroweb/basket_share_action.html"
        form = ShareForm(initial={'experimenters':experimenters}, data=request.REQUEST.copy())
        if form.is_valid():
            message = request.REQUEST['message'].encode('utf-8')
            expiration = None
            try:
                if request.REQUEST['expiration'].encode('utf-8') is not None and request.REQUEST['expiration'].encode('utf-8') != "":
                    expiration = str(request.REQUEST['expiration'].encode('utf-8'))
            except:
                pass
            members = request.REQUEST.getlist('members')
            #guests = request.REQUEST['guests']
            enable = False
            try:
                if request.REQUEST['enable']: enable = True
            except:
                pass
            
            try:
                host = '%s%s' % (settings.APPLICATION_HOST, reverse("webindex"))
            except:
                host = '%s://%s:%s%s' % (request.META['wsgi.url_scheme'], request.META['SERVER_NAME'], request.META['SERVER_PORT'], reverse("webindex"))
            share.createShare(host, request.session['server'], request.session['imageInBasket'], message, members, enable, expiration)
            return HttpResponseRedirect(reverse("manage_shares"))
        else:
            basket = BaseBasket(conn)
            basket.load_basket(request)
            form_active_group = ActiveGroupForm(initial={'activeGroup':basket.eContext['context'].groupId, 'mygroups': basket.eContext['allGroups']})
            context = {'nav':request.session['nav'], 'eContext': share.eContext, 'basket':basket, 'form':form, 'form_active_group':form_active_group}
    elif action == "createdisc":
        template = "omeroweb/basket_discuss_action.html"
        form = ShareForm(initial={'experimenters':experimenters}, data=request.REQUEST.copy())
        if form.is_valid():
            message = request.REQUEST['message'].encode('utf-8')
            expiration = None
            try:
                if request.REQUEST['expiration'].encode('utf-8') is not None and request.REQUEST['expiration'].encode('utf-8') != "":
                    expiration = str(request.REQUEST['expiration'].encode('utf-8'))
            except:
                pass
            members = request.REQUEST.getlist('members')
            #guests = request.REQUEST['guests']
            enable = False
            try:
                if request.REQUEST['enable']: enable = True
            except:
                pass
            try:
                host = '%s%s' % (settings.APPLICATION_HOST, reverse("webindex"))
            except:
                host = '%s://%s:%s%s' % (request.META['wsgi.url_scheme'], request.META['SERVER_NAME'], request.META['SERVER_PORT'], reverse("webindex"))
            share.createDiscussion(host, request.session['server'], message, members, enable, expiration)
            return HttpResponseRedirect(reverse("manage_shares"))
        else:
            basket = BaseBasket(conn)
            basket.load_basket(request)
            form_active_group = ActiveGroupForm(initial={'activeGroup':basket.eContext['context'].groupId, 'mygroups': basket.eContext['allGroups']})
            context = {'nav':request.session['nav'], 'eContext': share.eContext, 'basket':basket, 'form':form, 'form_active_group':form_active_group}
    elif action == 'edit':
        template = "omeroweb/share_form.html"
        share.getMembers(sid)
        share.getComments(sid)
        
        if share.share.getExpirationDate() is not None:
            form = ShareForm(initial={'message': share.share.message, 'expiration': share.share.getExpirationDate().strftime("%Y-%m-%d"), \
                                    'shareMembers': share.membersInShare, 'enable': share.share.active, \
                                    'experimenters': experimenters}) #'guests': share.guestsInShare,
        else:
            form = ShareForm(initial={'message': share.share.message, 'expiration': "", \
                                    'shareMembers': share.membersInShare, 'enable': share.share.active, \
                                    'experimenters': experimenters}) #'guests': share.guestsInShare,
        context = {'url':url, 'nav':request.session['nav'], 'eContext': share.eContext, 'share':share, 'form':form, 'form_active_group':form_active_group}
    elif action == 'save':
        form = ShareForm(initial={'experimenters':experimenters}, data=request.REQUEST.copy())
        if form.is_valid():
            message = request.REQUEST['message'].encode('utf-8')
            expiration = None
            try:
                if request.REQUEST['expiration'].encode('utf-8') is not None and request.REQUEST['expiration'].encode('utf-8') != "":
                    expiration = str(request.REQUEST['expiration'].encode('utf-8'))
            except:
                pass
            members = request.REQUEST.getlist('members')
            #guests = request.REQUEST['guests']
            enable = False
            try:
                if request.REQUEST['enable']: enable = True
            except:
                pass
            
            try:
                host = '%s%s' % (settings.APPLICATION_HOST, reverse("webindex"))
            except:
                host = '%s://%s:%s%s' % (request.META['wsgi.url_scheme'], request.META['SERVER_NAME'], request.META['SERVER_PORT'], reverse("webindex"))
            share.updateShareOrDiscussion(host, request.session['server'], message, members, enable, expiration)
            return HttpResponseRedirect(reverse("manage_shares"))
        else:
            template = "omeroweb/share_form.html"
            share.getComments(sid)
            context = {'url':url, 'nav':request.session['nav'], 'eContext': share.eContext, 'share':share, 'form':form, 'form_active_group':form_active_group}
    elif action == 'delete':
        return HttpResponseRedirect(reverse("manage_shares"))
    elif action == 'view':
        template = "omeroweb/share_details.html"
        share.getAllUsers(sid)
        share.getComments(sid)
        if share.share.isExpired():
            form = None
        else:
            form = ShareCommentForm()
        context = {'nav':request.session['nav'], 'eContext': share.eContext, 'share':share, 'form':form, 'form_active_group':form_active_group}
    elif action == 'comment':
        f = ShareCommentForm(data=request.REQUEST.copy())
        if f.is_valid():
            comment = request.REQUEST['comment'].encode('utf-8')
            try:
                host = '%s%s' % (settings.APPLICATION_HOST, reverse("webindex"))
            except:
                host = '%s://%s:%s%s' % (request.META['wsgi.url_scheme'], request.META['SERVER_NAME'], request.META['SERVER_PORT'], reverse("webindex"))
            share.addComment(host, request.session['server'], comment)
            return HttpResponseRedirect(reverse(viewname="manage_share", args=["view", sid]))
        else:
            template = "omeroweb/share_details.html"
            share.getComments(sid)
            form = ShareCommentForm(data=request.REQUEST.copy())
            context = {'nav':request.session['nav'], 'eContext': share.eContext, 'share':share, 'form':form, 'form_active_group':form_active_group}

    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def load_share_content(request, share_id, **kwargs):
    template = "omeroweb/share_content.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    conn_share = None
    try:
        conn_share = kwargs["conn_share"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    try:
        share = BaseShare(request.session['nav']['menu'], conn, conn_share, share_id)
    except AttributeError, x:
        return handlerInternalError(x)
    share.loadShareContent()
    
    context = {'share':share, 'eContext':share.eContext}
    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def load_share_owner_content(request, share_id, **kwargs):
    template = "omeroweb/share_content.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    try:
        share = BaseShare(request.session['nav']['menu'], conn, None, share_id)
    except AttributeError, x:
        return handlerInternalError(x)
    share.loadShareOwnerContent(share_id)
    
    context = {'share':share, 'eContext':share.eContext}
    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

##################################################################
# Basket

@isUserConnected
def basket_action (request, action=None, **kwargs):
    request.session['nav']['menu'] = ''
    request.session['nav']['whos'] = ''

    request.session.modified = True
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    if action == "toshare":
        template = "omeroweb/basket_share_action.html"
        
        basket = BaseBasket(conn)
        basket.buildBreadcrumb(action)
        basket.load_basket(request)
        experimenters = sortByAttr(list(conn.getExperimenters()), 'lastName')
        
        form_active_group = ActiveGroupForm(initial={'activeGroup':basket.eContext['context'].groupId, 'mygroups': basket.eContext['allGroups']})
        
        form = ShareForm(initial={'experimenters': experimenters, 'enable':True})
        context = {'nav':request.session['nav'], 'eContext':basket.eContext, 'basket':basket, 'form':form, 'form_active_group':form_active_group}
    elif action == "todiscuss":
        template = "omeroweb/basket_discuss_action.html"
        
        basket = BaseBasket(conn)
        basket.buildBreadcrumb(action)
        experimenters = sortByAttr(list(conn.getExperimenters()), 'lastName')
        
        form_active_group = ActiveGroupForm(initial={'activeGroup':basket.eContext['context'].groupId, 'mygroups': basket.eContext['allGroups']})
        
        form = ShareForm(initial={'experimenters': experimenters, 'enable':True})
        context = {'nav':request.session['nav'], 'eContext':basket.eContext, 'basket':basket, 'form':form, 'form_active_group':form_active_group}
    elif action == "toannotate":
        # TODO
        template = "omeroweb/basket_share_action.html"
        
        basket = BaseBasket(conn)
        basket.buildBreadcrumb(action)
        basket.load_basket(request)
        experimenters = conn.getExperimenters()
        
        form_active_group = ActiveGroupForm(initial={'activeGroup':basket.eContext['context'].groupId, 'mygroups': basket.eContext['allGroups']})
        
        form = ShareForm(initial={'experimenters': experimenters})
        context = {'nav':request.session['nav'], 'eContext':basket.eContext, 'basket':basket, 'form':form, 'form_active_group':form_active_group}
    elif action == "totag":
        # TODO
        template = "omeroweb/basket_share_action.html"
        
        basket = BaseBasket(conn)
        basket.buildBreadcrumb(action)
        basket.load_basket(request)
        experimenters = conn.getExperimenters()
        
        form_active_group = ActiveGroupForm(initial={'activeGroup':basket.eContext['context'].groupId, 'mygroups': basket.eContext['allGroups']})
        
        form = ShareForm(initial={'experimenters': experimenters})
        context = {'nav':request.session['nav'], 'eContext':basket.eContext, 'basket':basket, 'form':form, 'form_active_group':form_active_group}
    else:
        template = "omeroweb/basket.html"
        
        basket = BaseBasket(conn)
        basket.buildBreadcrumb()
        basket.load_basket(request)
        
        form_active_group = ActiveGroupForm(initial={'activeGroup':basket.eContext['context'].groupId, 'mygroups': basket.eContext['allGroups']})
        
        context = {'nav':request.session['nav'], 'eContext':basket.eContext, 'basket':basket, 'form_active_group':form_active_group }

    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def empty_basket(request, **kwargs):
    try:
        del request.session['imageInBasket']
    except KeyError:
        logger.error(traceback.format_exc())
    
    #try:
    #    del request.session['datasetInBasket']
    #except KeyError:
    #    logger.error(traceback.format_exc())
        
    request.session['nav']['basket'] = 0
    request.session['imageInBasket'] = list()
    #request.session['datasetInBasket'] = list()
    return HttpResponseRedirect(reverse("basket_action"))

@isUserConnected
def update_basket(request, **kwargs):
    action = None
    if request.method == 'POST':
        try:
            action = request.REQUEST['action']
        except Exception, x:
            logger.error(traceback.format_exc())
            return handlerInternalError("Attribute error: 'action' is missed.")
        else:
            prod = long(request.REQUEST['productId'])
            ptype = str(request.REQUEST['productType'])
            if action == 'add':
                if ptype == 'image':
                    for item in request.session['imageInBasket']:
                        if item == prod:
                            rv = "Error: This object is already in the basket"
                            return HttpResponse(rv)
                    request.session['imageInBasket'].append(prod)
                #elif ptype == 'dataset':
                #    for item in request.session['datasetInBasket']:
                #        if item == prod:
                #            rv = "Error: This object is already in the basket"
                #            return HttpResponse(rv)
                #    request.session['datasetInBasket'].append(prod)
                else:
                    rv = "Error: This action is not available"
                    return HttpResponse(rv)
            elif action == 'del':
                if ptype == 'image':
                    try:
                        request.session['imageInBasket'].remove(prod)
                    except:
                        rv = "Error: could not remove image from the basket."
                        return HttpResponse(rv)
                #elif ptype == 'dataset':
                #    try:
                #        request.session['datasetInBasket'].remove(prod)
                #    except:
                #        rv = "Error: could not remove image from the basket."
                #        return HttpResponse(rv)
                else:
                    rv = "Error: This action is not available"
                    return HttpResponse(rv)
        total = len(request.session['imageInBasket'])#+len(request.session['datasetInBasket'])
        request.session['nav']['basket'] = total
        request.session.modified = True
        return HttpResponse(total)
    else:
        return handlerInternalError("Request method error in Basket.")

##################################################################
# Clipboard

@isUserConnected
def update_clipboard(request, **kwargs):
    action = None
    rv = "Error: Action not available"
    if request.method == 'POST':
        try:
            action = request.REQUEST['action']
        except Exception, x:
            logger.error(traceback.format_exc())
            return handlerInternalError("Attribute error: 'action' is missed.")
        else:
            if action == 'copy':
                prod = long(request.REQUEST['productId'])
                ptype = str(request.REQUEST['productType'])
                if len(request.session['clipboard']) > 0 and request.session['clipboard'][0] == ptype and request.session['clipboard'][1] == prod:
                    rv = "Error: This object is already in the clipboard."
                    return HttpResponse(rv)
                else:
                    request.session['clipboard'] = [ptype, prod]
                    rv = "%s (id:%ld) was copied to clipboard." % (ptype.title(), prod)
            elif action == 'paste':
                destination = [str(request.REQUEST['destinationType']), long(request.REQUEST['destinationId'])]
                if len(request.session['clipboard']) == 2 :
                    conn = None
                    try:
                        conn = kwargs["conn"]
                    except:
                        logger.error(traceback.format_exc())
                    
                    manager = BaseContainer(conn)
                    if request.session['clipboard'][0] == 'dataset' and destination[0] == 'project':
                        try:
                            manager.copyDatasetToProject(request.session['clipboard'], destination)
                        except Exception, x:
                            return HttpResponse("Error: %s" % (x.__class__.__name__))
                    #elif request.session['clipboard'][0] == 'plate' and destination[0] == 'screen':
                    #    try:
                    #        manager.copyPlateToScreen(request.session['clipboard'], destination)
                    #    except Exception, x:
                    #        return HttpResponse("Error: %s" % (x.__class__.__name__))
                    elif request.session['clipboard'][0] == 'image' and destination[0] == 'dataset':
                        try:
                            manager.copyImageToDataset(request.session['clipboard'], destination)
                        except Exception, x:
                            return HttpResponse("Error: %s" % (x.__class__.__name__))
                    else:
                        return HttpResponse(rv)
                    
                    request.session['clipboard'] = []
                    rv = "Copied successful"
                else:
                    rv = "Error: Clipboard is empty. You need to copy before paste."
                    return HttpResponse(rv)
            elif action == 'clean':
                try:
                    del request.session['clipboard']
                except KeyError:
                    logger.error(traceback.format_exc())

                request.session['clipboard'] = []
                rv = "Cleapboard is empty"
        return HttpResponse(rv)
    else:
        return handlerInternalError("Request method error in Clipboard.")

@isUserConnected
def search(request, **kwargs):
    request.session['nav']['menu'] = 'search'
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    url = None
    try:
        url = kwargs["url"]
    except:
        logger.error(traceback.format_exc())
        
    try:
        request.session['nav']['view'] = request.REQUEST['view'] # table, icon, tree 
    except:
        pass

    request.session.modified = True
        
    if request.session['nav']['view'] == 'table':
        template = "omeroweb/search_table.html"
    elif request.session['nav']['view'] == 'icon':
        template = "omeroweb/search_icon.html"
    else:
        request.session['nav']['view'] = "table"
        template = "omeroweb/search_icon.html"
    
    controller = BaseSearch(conn)
    
    try:
        if request.method == 'GET' and request.REQUEST['query']: 
            pass
    except:
        controller.criteria['image'] = 'CHECKED'
    else:
        onlyTypes = list()
        try:
            if request.REQUEST['query']:
                query_search = request.REQUEST['query']
            else:
                query_search = None
        except:
            query_search = None
        
        try:
            if request.REQUEST['project'] == unicode('on'):
                onlyTypes.append('project')
        except:
            pass
        
        try:
            if request.REQUEST['dataset'] == unicode('on'):
                onlyTypes.append('dataset')
        except:
            pass
        
        try:
            if request.REQUEST['image'] == unicode('on'):
                onlyTypes.append('image')
        except:
            pass
        
        try:
            if request.REQUEST['dateperiodinput'] != unicode(''):
                period = request.REQUEST['dateperiodinput']
            else:
                period = ""
        except:
            period = ""
        
        controller.search(query_search, onlyTypes, period)
    
    form_active_group = ActiveGroupForm(initial={'activeGroup':controller.eContext['context'].groupId, 'mygroups': controller.eContext['allGroups']})
    
    context = {'nav':request.session['nav'], 'url':url, 'eContext':controller.eContext, 'controller':controller, 'form_active_group':form_active_group}
    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def importer(request, **kwargs):
    request.session['nav']['menu'] = 'import'
    template = "omeroweb/importer.html"
    
    request.session.modified = True

    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    controller = BaseImpexp(conn)

    form_active_group = ActiveGroupForm(initial={'activeGroup':controller.eContext['context'].groupId, 'mygroups': controller.eContext['allGroups']})
    
    context = {'sid':request.session['server'], 'uuid':conn._sessionUuid, 'nav':request.session['nav'], 'eContext': controller.eContext, 'controller':controller, 'form_active_group':form_active_group}
    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))


@load_session_from_request
@isUserConnected
def flash_uploader(request, **kwargs):
    logger.debug("Upload from web processing...")
    try:
        if request.method == 'POST':
            logger.debug("Web POST data sent:")
            logger.debug(request.POST)
            logger.debug(request.FILES)
        else:
            raise AttributeError("Only POST accepted")
        try:
            conn = kwargs["conn"]
        except:
            logger.error(traceback.format_exc())
            return handlerInternalError("Connection is not available. Please contact your administrator.")
        return HttpResponse()
    except Exception, x:
        logger.error(traceback.format_exc())
        return HttpResponse(x)


@isUserConnected
def myaccount(request, action=None, **kwargs):
    request.session['nav']['menu'] = 'person'
    template = "omeroweb/myaccount.html"
    request.session.modified = True
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    controller = BaseExperimenter(conn)
    controller.getMyDetails()
    
    eContext = dict()
    eContext['context'] = conn.getEventContext()
    eContext['user'] = conn.getUser()
    eContext['breadcrumb'] = ["My Account",  controller.experimenter.id]
    
    grs = list(conn.getGroupsMemberOf())
    #grs.extend(list(conn.getGroupsLeaderOf()))
    eContext['allGroups']  = controller.sortByAttr(grs, "name")
    #eContext['memberOfGroups'] = controller.sortByAttr(list(conn.getGroupsMemberOf()), "name")
    
    if controller.ldapAuth == "" or controller.ldapAuth is None:
        form = MyAccountForm(initial={'omename': controller.experimenter.omeName, 'first_name':controller.experimenter.firstName,
                                'middle_name':controller.experimenter.middleName, 'last_name':controller.experimenter.lastName,
                                'email':controller.experimenter.email, 'institution':controller.experimenter.institution,
                                'default_group':controller.defaultGroup, 'groups':controller.otherGroups})
    else:
        form = MyAccountLdapForm(initial={'omename': controller.experimenter.omeName, 'first_name':controller.experimenter.firstName,
                                'middle_name':controller.experimenter.middleName, 'last_name':controller.experimenter.lastName,
                                'email':controller.experimenter.email, 'institution':controller.experimenter.institution,
                                'default_group':controller.defaultGroup, 'groups':controller.otherGroups})
    photo_size = conn.getExperimenterPhotoSize()
    form_file = UploadPhotoForm()

    request.session['nav']['edit_mode'] = False    
    if action == "save":
        if controller.ldapAuth == "" or controller.ldapAuth is None:
            form = MyAccountForm(data=request.POST.copy(), initial={'groups':controller.otherGroups})
        else:
            form = MyAccountLdapForm(data=request.POST.copy(), initial={'groups':controller.otherGroups})
        if form.is_valid():
            firstName = request.REQUEST['first_name'].encode('utf-8')
            middleName = request.REQUEST['middle_name'].encode('utf-8')
            lastName = request.REQUEST['last_name'].encode('utf-8')
            email = request.REQUEST['email'].encode('utf-8')
            institution = request.REQUEST['institution'].encode('utf-8')
            defaultGroup = request.REQUEST['default_group']
            try:
                password = request.REQUEST['password'].encode('utf-8')
                if len(password) == 0:
                    password = None
            except:
                password = None
            controller.updateMyAccount(firstName, lastName, email, defaultGroup, middleName, institution, password)
            return HttpResponseRedirect(reverse("myaccount"))
    elif action == "upload":
        if request.method == 'POST':
            form_file = UploadPhotoForm(request.POST, request.FILES)
            if form_file.is_valid():
                controller = BaseUploadFile(conn)
                controller.attach_photo(request.FILES['photo'])
                return HttpResponseRedirect(reverse("myaccount"))
    elif action == "crop": 
        x1 = long(request.REQUEST['x1'].encode('utf-8'))
        x2 = long(request.REQUEST['x2'].encode('utf-8'))
        y1 = long(request.REQUEST['y1'].encode('utf-8'))
        y2 = long(request.REQUEST['y2'].encode('utf-8'))
        box = (x1,y1,x2,y2)
        conn.cropExperimenterPhoto(box)
        return HttpResponseRedirect(reverse("myaccount"))
    elif action == "editphoto":
        if photo_size is not None:
            request.session['nav']['edit_mode'] = True

    form_active_group = ActiveGroupForm(initial={'activeGroup':eContext['context'].groupId, 'mygroups': eContext['allGroups']})
    context = {'nav':request.session['nav'], 'eContext': eContext, 'form':form, 'ldapAuth': controller.ldapAuth, 'form_active_group':form_active_group, 'form_file':form_file, 'photo_size':photo_size}
    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def help(request, **kwargs):
    request.session['nav']['menu'] = 'help'
    template = "omeroweb/help.html"
    request.session.modified = True
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    controller = BaseHelp(conn)
    
    form_active_group = ActiveGroupForm(initial={'activeGroup':controller.eContext['context'].groupId, 'mygroups': controller.eContext['allGroups']})
    
    context = {'nav':request.session['nav'], 'eContext': controller.eContext, 'controller':controller, 'form_active_group':form_active_group}
    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def history(request, year=None, month=None, **kwargs):
    request.session['nav']['menu'] = 'history'
    template = "omeroweb/history.html"
    request.session.modified = True
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    if year is not None and month is not None:
        controller = BaseCalendar(conn=conn, year=year, month=month)
    else:
        today = datetime.datetime.today()
        controller = BaseCalendar(conn=conn, year=today.year, month=today.month)
    controller.create_calendar()
    
    form_active_group = ActiveGroupForm(initial={'activeGroup':controller.eContext['context'].groupId, 'mygroups': controller.eContext['allGroups']})
    
    context = {'nav':request.session['nav'], 'eContext': controller.eContext, 'controller':controller, 'form_active_group':form_active_group}
    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))

@isUserConnected
def history_details(request, year, month, day, **kwargs):
    request.session['nav']['menu'] = 'history'
    request.session['nav']['whos'] = 'mydata'
    request.session.modified = True
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    url = None
    try:
        url = kwargs["url"]
    except:
        logger.error(traceback.format_exc())
    
    try:
        page = int(request.REQUEST['page'])
    except:
        page = 1
    
    cal_type = None
    try:
        cal_type = request.REQUEST['history_type']
        if cal_type == "all":
            cal_type = None
    except:
        cal_type = None
    
    template = "omeroweb/history_details.html"
    
    controller = BaseCalendar(conn=conn, year=year, month=month, day=day)
    controller.get_items(cal_type, page)
    
    form_active_group = ActiveGroupForm(initial={'activeGroup':controller.eContext['context'].groupId, 'mygroups': controller.eContext['allGroups']})
    
    if cal_type is None:
        form_history_type = HistoryTypeForm()
    else:
        form_history_type = HistoryTypeForm(initial={'data_type':cal_type})
    
    context = {'nav':request.session['nav'], 'url':url, 'eContext': controller.eContext, 'controller':controller, 'form_active_group':form_active_group, 'form_history_type':form_history_type}
    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))

####################################################################################
# User Photo

@isUserConnected
def load_photo(request, oid=None, **kwargs):
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    photo = conn.getExperimenterPhoto(oid)
    return HttpResponse(photo, mimetype='image/jpeg')

@isUserConnected
def myphoto(request, **kwargs):
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    photo = conn.getExperimenterPhoto()
    return HttpResponse(photo, mimetype='image/jpeg')

####################################################################################
# Rendering

@isUserConnected
def render_thumbnail (request, iid, share_id=None, **kwargs):
    conn = None
    if share_id is not None:
        try:
            conn = kwargs["conn_share"]
        except:
            logger.error(traceback.format_exc())
            return handlerInternalError("Connection is not available. Please contact your administrator.")
    else:
        try:
            conn = kwargs["conn"]
        except:
            logger.error(traceback.format_exc())
            return handlerInternalError("Connection is not available. Please contact your administrator.")
         
    if conn is None:
        raise Exception("Share connection not available")
    img = conn.getImage(iid)
    
    if img is None:
        logger.error("Image %s not found..." % (str(iid)))
        return handlerInternalError("Image %s not found..." % (str(iid)))
    
    jpeg_data = img.getThumbnail(size=(120,120))
    return HttpResponse(jpeg_data, mimetype='image/jpeg')

@isUserConnected
def render_thumbnail_resize (request, size, iid, share_id=None, **kwargs):
    conn = None
    if share_id is not None:
        try:
            conn = kwargs["conn_share"]
        except:
            logger.error(traceback.format_exc())
            return handlerInternalError("Connection is not available. Please contact your administrator.")
    else:
        try:
            conn = kwargs["conn"]
        except:
            logger.error(traceback.format_exc())
            return handlerInternalError("Connection is not available. Please contact your administrator.")
         
    if conn is None:
        raise Exception("Share connection not available")
    img = conn.getImage(iid)
    
    if img is None:
        logger.error("Image %s not found..." % (str(iid)))
        return handlerInternalError("Image %s not found..." % (str(iid)))
    
    jpeg_data = img.getThumbnail(size=int(size))
    return HttpResponse(jpeg_data, mimetype='image/jpeg')

@isUserConnected
def render_big_thumbnail (request, iid, **kwargs):
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")

    img = conn.getImage(iid)
    
    if img is None:
        logger.error("Image %s not found..." % (str(iid)))
        return handlerInternalError("Image %s not found..." % (str(iid)))
    
    size = 0
    if img.getWidth() >= 200:
        size = img.getWidth()
    else:
        size = 200
    
    if size <=750:
        size = size
    else:
        size = 750 
    
    jpeg_data = img.getThumbnail(size=size)
    return HttpResponse(jpeg_data, mimetype='image/jpeg')


@isUserConnected
def render_image (request, iid, z, t, share_id=None, **kwargs):
    """ Renders the image with id {{iid}} at {{z}} and {{t}} as jpeg.
        Many options are available from the request dict.
    I am assuming a single Pixels object on image with id='iid'. May be wrong """

    conn = None
    if share_id is not None:
        try:
            conn = kwargs["conn_share"]
        except:
            logger.error(traceback.format_exc())
            return handlerInternalError("Connection is not available. Please contact your administrator.")
    else:
        try:
            conn = kwargs["conn"]
        except:
            logger.error(traceback.format_exc())
            return handlerInternalError("Connection is not available. Please contact your administrator.")
         
    if conn is None:
        raise Exception("Share connection not available")

    return webgateway_views.render_image(request, iid, z, t, _conn=conn, **kwargs)


@isUserConnected
def image_viewer (request, iid, share_id=None, **kwargs):
    """ This view is responsible for showing pixel data as images """
    
    conn = None
    if share_id is not None:
        try:
            conn = kwargs["conn_share"]
        except:
            logger.error(traceback.format_exc())
            return handlerInternalError("Connection is not available. Please contact your administrator.")
    else:
        try:
            conn = kwargs["conn"]
        except:
            logger.error(traceback.format_exc())
            return handlerInternalError("Connection is not available. Please contact your administrator.")
         
    if conn is None:
        raise Exception("Share connection not available")
    
    kwargs['viewport_server'] = '/webclient'
    if share_id:
        kwargs['viewport_server'] += '/%s' % share_id

    return webgateway_views.full_viewer(request, iid, _conn=conn, **kwargs)


@isUserConnected
def imageData_json (request, iid, share_id=None, **kwargs):
    """ Get a dict with image information """
    conn = None
    if share_id is not None:
        try:
            conn = kwargs["conn_share"]
        except:
            logger.error(traceback.format_exc())
            return handlerInternalError("Connection is not available. Please contact your administrator.")
    else:
        try:
            conn = kwargs["conn"]
        except:
            logger.error(traceback.format_exc())
            return handlerInternalError("Connection is not available. Please contact your administrator.")
         
    if conn is None:
        raise Exception("Share connection not available")

    return HttpResponse(webgateway_views.imageData_json(request, iid=iid, _conn=conn, **kwargs), mimetype='application/javascript')

@isUserConnected
def render_row_plot (request, iid, z, t, y, share_id=None, w=1, **kwargs):
    """ Get a dict with image information """
    conn = None
    if share_id is not None:
        try:
            conn = kwargs["conn_share"]
        except:
            logger.error(traceback.format_exc())
            return handlerInternalError("Connection is not available. Please contact your administrator.")
    else:
        try:
            conn = kwargs["conn"]
        except:
            logger.error(traceback.format_exc())
            return handlerInternalError("Connection is not available. Please contact your administrator.")
         
    if conn is None:
        raise Exception("Share connection not available")
    img = conn.getImage(iid)

    return webgateway_views.render_row_plot(request, iid=iid, z=z, t=t, y=y, w=w, _conn=conn, **kwargs)

@isUserConnected
def render_col_plot (request, iid, z, t, x, share_id=None, w=1, **kwargs):
    """ Get a dict with image information """
    conn = None
    if share_id is not None:
        try:
            conn = kwargs["conn_share"]
        except:
            logger.error(traceback.format_exc())
            return handlerInternalError("Connection is not available. Please contact your administrator.")
    else:
        try:
            conn = kwargs["conn"]
        except:
            logger.error(traceback.format_exc())
            return handlerInternalError("Connection is not available. Please contact your administrator.")
         
    if conn is None:
        raise Exception("Share connection not available")
    img = conn.getImage(iid)

    return webgateway_views.render_col_plot(request, iid=iid, z=z, t=t, x=x, w=w, _conn=conn, **kwargs)

@isUserConnected
def render_split_channel (request, iid, z, t, share_id=None, **kwargs):
    """ Get a dict with image information """
    conn = None
    if share_id is not None:
        try:
            conn = kwargs["conn_share"]
        except:
            logger.error(traceback.format_exc())
            return handlerInternalError("Connection is not available. Please contact your administrator.")
    else:
        try:
            conn = kwargs["conn"]
        except:
            logger.error(traceback.format_exc())
            return handlerInternalError("Connection is not available. Please contact your administrator.")
         
    if conn is None:
        raise Exception("Share connection not available")
    img = conn.getImage(iid)

    return webgateway_views.render_split_channel(request, iid, z, t, _conn=conn, **kwargs)

####################################################################################
# utils

GOOGLE_URL = "www.google.com"
def spellchecker(request):
    if request.method == 'POST':
        lang = request.GET.get("lang", "en")
        data = request.raw_post_data
        con = httplib.HTTPSConnection(GOOGLE_URL)
        con.request("POST", "/tbproxy/spell?lang=%s" % lang, data)
        response = con.getresponse()
        r_text = response.read()
        con.close()
        return HttpResponse(r_text, mimetype='text/javascript')


@isUserConnected
def test(request, **kwargs):
    template = "omeroweb/testROIs/test.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    r = request.REQUEST
    rv = {}
    for k in ('x1', 'x2', 'y1', 'y2', 'w', 'h', 'shape'):
        if r.has_key(k):
           rv[k] = r[k]
    
    photo_size = conn.getExperimenterPhotoSize()
    
    context = {'photo_size':photo_size, 'params':rv}
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)


@isUserConnected
def histogram(request, oid, **kwargs):
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
        
    import matplotlib
    matplotlib.use('Agg')
    from matplotlib.backends.backend_agg import FigureCanvasAgg as FigureCanvas
    from matplotlib.figure import Figure
    from cStringIO import StringIO

    import Image
    
    from PIL import Image, ImageOps, ImageDraw
    
    valsR = conn.downloadPlane(oid, 5,0,1)
    valsG = conn.downloadPlane(oid, 5,1,1)
    valsB = conn.downloadPlane(oid, 5,2,1)
    import numpy as np
    import matplotlib.pyplot as plt
    import matplotlib.mlab as mlab
    
    import pylab

    # plt.figure creates a matplotlib.figure.Figure instance
    #fig = plt.figure()
    #canvas = FigureCanvas(fig)
    #ax = fig.add_subplot(111)
    #ax.set_title("title")
    #ax.set_xlabel("Score")
    #ax.set_ylabel("Frequency")
    fig = pylab.figure()
    binsR = valsR[0]
    binsG = valsG[0]
    binsB = valsB[0]

    # the histogram of the data with histtype='step'
    #nR, binsR, patchesR = ax.hist(binsR, bins=max(binsR), facecolor='red', edgecolor='red')
    #nG, binsG, patchesG = ax.hist(binsG, bins=max(binsG), facecolor='green', edgecolor='green')
    #nB, binsB, patchesB = ax.hist(binsB, bins=max(binsB), facecolor='blue', edgecolor='blue')

    nR, binsR, patchesR = pylab.hist(binsR, max(binsR), normed=1, histtype='bar', edgecolor='red')
    pylab.setp(patchesR, 'facecolor', 'r', 'alpha', 0.75)
    nG, binsG, patchesG = pylab.hist(binsG, bins=max(binsG), normed=1, histtype='bar', edgecolor='green')
    pylab.setp(patchesG, 'facecolor', 'g', 'alpha', 0.75)
    nB, binsB, patchesB = pylab.hist(binsB, bins=max(binsB), normed=1, histtype='bar', edgecolor='blue')
    pylab.setp(patchesB, 'facecolor', 'b', 'alpha', 0.75)
    
    #for label in ax.xaxis.get_ticklabels():
    #    label.set_color('red')
    #    label.set_rotation(90)
    
    canvas = FigureCanvas(fig)
    
    imdata = StringIO()
    canvas.print_figure(imdata)
    return HttpResponse(imdata.getvalue(), mimetype='image/png')
    
