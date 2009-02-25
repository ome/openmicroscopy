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
import Image,ImageDraw
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

from models import ShareForm, ShareCommentForm, ContainerForm, CommentAnnotationForm, TagAnnotationForm, \
                    UriAnnotationForm, UploadFileForm, MyGroupsForm, MyUserForm, ActiveGroupForm, \
                    HistoryTypeForm, MetadataEnvironmentForm, MetadataObjectiveForm, MetadataStageLabelForm, \
                    TagListForm, UrlListForm, CommentListForm, FileListForm, TagFilterForm
from omeroweb.webadmin.models import MyAccountForm, MyAccountLdapForm, UploadPhotoForm

from omeroweb.webadmin.models import Gateway, LoginForm
from extlib.gateway import BlitzGateway

logger = logging.getLogger('views-web')

connectors = {}
share_connectors = {}

logger.info("INIT '%s'" % os.getpid())

################################################################################
# Blitz Gateway Connection

def timeit (func):
    def wrapped (*args, **kwargs):
        logger.debug("timing %s" % (func.func_name))
        now = time()
        rv = func(*args, **kwargs)
        logger.debug("timed %s : %f" % (func.func_name, time()-now))
        return rv
    return wrapped

@timeit
def getConnection (request):

    session_key = None
    server = None

    # gets Http session or create new one
    session_key = request.session.session_key

    # gets host base
    try:
        server = request.session['server']
    except KeyError:
        return None

    # clean up connections
    for k,v in connectors.items():
        if v is None:
            try:
                v.seppuku()
            except:
                logger.debug("Connection was already killed.")
                logger.debug(traceback.format_exc())
            del connectors[k]

    if len(connectors) > 75:
        for k,v in connectors.items()[50:]:
            v.seppuku()
            del connectors[k]

    # builds connection key for current session
    conn_key = None
    if (server and session_key) is not None:
        conn_key = 'S:' + str(request.session.session_key) + '#' + str(server)
    else:
        return None

    request.session.modified = True

    # gets connection for key if available
    conn = connectors.get(conn_key)

    if conn is None:
        # could not get connection for key
        # try retrives the connection from existing session
        try:
            if request.session['sessionUuid']: pass
            if request.session['groupId']: pass
        except KeyError:
            # create the connection from login parameters
            try:
                if request.session['host']: pass
                if request.session['port']: pass
                if request.session['username']: pass
                if request.session['password']: pass
            except KeyError:
                pass
            else:
                # login parameters found, create new connection
                try:
                    conn = BlitzGateway(request.session['host'], request.session['port'], request.session['username'], request.session['password'])
                    conn.connect()
                    request.session['sessionUuid'] = conn.getEventContext().sessionUuid
                    request.session['groupId'] = conn.getEventContext().groupId
                except:
                    logger.error(traceback.format_exc())
                    raise sys.exc_info()[1]
                else:
                    # stores connection on connectors
                    connectors[conn_key] = conn
                    logger.debug("Have connection, stored in connectors:'%s', uuid: '%s'" % (str(conn_key), str(request.session['sessionUuid'])))
                    logger.info("Total connectors: %i." % (len(connectors)))
        else:
            # retrieves connection from sessionUuid, join to existing session
            try:
                conn = BlitzGateway(request.session['host'], request.session['port'], request.session['username'], request.session['password'], request.session['sessionUuid'])
                conn.connect()
                #request.session['sessionUuid'] = conn.getEventContext().sessionUuid
                #request.session['groupId'] = conn.getEventContext().groupId
            except:
                logger.error(traceback.format_exc())
                raise sys.exc_info()[1]
            else:
                # stores connection on connectors
                connectors[conn_key] = conn
                logger.debug("Retreived connection, will travel: '%s', uuid: '%s'" % (str(conn_key), str(request.session['sessionUuid'])))
                logger.info("Total connectors: %i." % (len(connectors)))
    else:
        # gets connection
        try: 
            request.session['sessionUuid'] = conn.getEventContext().sessionUuid
        except:
            # connection is no longer available, try again
            connectors[conn_key] = None
            logger.debug("Connection '%s' is no longer available" % (conn_key))
            return getConnection(request)
        else:
            logger.debug("Connection exists: '%s', uuid: '%s'" % (str(conn_key), str(request.session['sessionUuid'])))
    return conn

@timeit
def getShareConnection (request):
    
    session_key = None
    server = None

    # gets Http session or create new one
    session_key = request.session.session_key

    # gets host base
    try:
        server = request.session['server']
    except KeyError:
        return None

    # clean up connections
    for k,v in share_connectors.items():
        if v is None:
            try:
                v.seppuku()
            except:
                logger.debug("Connection was already killed.")
                logger.debug(traceback.format_exc())
            del connectors[k]

    if len(share_connectors) > 75:
        for k,v in share_connectors.items()[50:]:
            v.seppuku()
            del share_connectors[k]

    # builds connection key for current session
    conn_key = None
    if (server and session_key) is not None:
        conn_key = 'S:' + str(request.session.session_key) + '#' + str(server)
    else:
        return None

    request.session.modified = True

    # gets connection for key if available
    conn = share_connectors.get(conn_key)
    
    if conn is None:
        try:
            if request.session['host']: pass
            if request.session['port']: pass
            if request.session['username']: pass
            if request.session['password']: pass
        except:
            logger.error(traceback.format_exc())
            raise sys.exc_info()[1]
        else:
            # login parameters found, create the connection
            try:
                conn = BlitzGateway(request.session['host'], request.session['port'], request.session['username'], request.session['password'])
                conn.connectAsShare()
            except:
                logger.error(traceback.format_exc())
                raise sys.exc_info()[1]
            else:
                # stores connection on connectors
                share_connectors[conn_key] = conn
                logger.debug("Have connection uuid: '%s'" % (str(conn._sessionUuid)))
    else:
        try:
            conn.getEventContext().sessionUuid
        except:
            # connection is no longer available, retrieves connection login parameters
            connectors[conn_key] = None
            logger.debug("Connection '%s' is no longer available" % (conn_key))
            return getShareConnection(request)
        else:
            logger.debug("Connection exists: '%s', uuid: '%s'" % (str(conn_key), str(conn._sessionUuid)))
    
    return conn

################################################################################
# decorators

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
                url = '%s://%s:%s%s?%s' % (request.META['wsgi.url_scheme'], request.META['SERVER_NAME'], request.META['SERVER_PORT'], request.META['PATH_INFO'], request.META['QUERY_STRING'])
            else:
                url = '%s://%s:%s%s' % (request.META['wsgi.url_scheme'], request.META['SERVER_NAME'], request.META['SERVER_PORT'], request.META['PATH_INFO'])
        
        conn = None
        try:
            conn = getConnection(request)
        except KeyError:
            return HttpResponseRedirect("/%s/login/?url=%s" % (settings.WEBCLIENT_ROOT_BASE, url))
        except Exception, x:
            logger.error(traceback.format_exc())
            return HttpResponseRedirect("/%s/login/?error=%s&url=%s" % (settings.WEBCLIENT_ROOT_BASE, x.__class__.__name__, url))
        if conn is None:
            return HttpResponseRedirect("/%s/login/?url=%s" % (settings.WEBCLIENT_ROOT_BASE, url))
        
        sessionHelper(request)        
        kwargs["conn"] = conn
        kwargs["url"] = url
        return f(request, *args, **kwargs)
    
    return wrapped

def sessionHelper(request):
    try:
        if request.session['clipboard']:
            pass
    except:
        request.session['clipboard'] = []
    try:
        if request.session['imageInBasket']:
            pass
    except:
        request.session['imageInBasket'] = list()
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
        request.session['imageInBasket'] = list()
        blitz_host = "%s:%s" % (blitz.host, blitz.port)
        request.session['nav']={"blitz": blitz_host, "menu": "start", "whos": "mydata", "view": "table", "basket": 0}
        
    try:
        error = request.REQUEST['error']
    except:
        error = None
    
    conn = None
    try:
        conn = getConnection(request)
    except Exception, x:
        #logger.error(traceback.format_exc())
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
            return HttpResponseRedirect("/%s/" % (settings.WEBCLIENT_ROOT_BASE))
    else:
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
                except:
                    initial = {'server': unicode(blitz[0].id)}
                    form = LoginForm(initial=initial)
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
    except:
        request.session['nav']['menu'] = 'start'
    
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
    controller.loadLastImports()
    
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
    return HttpResponseRedirect("/%s/" % (settings.WEBCLIENT_ROOT_BASE))

@isUserConnected
def logout(request, **kwargs):
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    try:
        session_key = "S:%s#%s" % (request.session.session_key,request.session['server'])
        if connectors.has_key(session_key):
            conn.seppuku()
            del connectors[session_key]
    except:
        logger.error(traceback.format_exc())
    
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
    try:
        del request.session['sessionUuid']
    except KeyError:
        logger.error(traceback.format_exc())
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

    return HttpResponseRedirect("/%s/" % (settings.WEBCLIENT_ROOT_BASE))


###########################################################################
# DATA MANAGEMENT request.session['nav']={"menu": "mydata", "whos": "mydata", "view": "table"}

@isUserConnected
def manage_my_data(request, o1_type=None, o1_id=None, o2_type=None, o2_id=None, o3_type=None, o3_id=None, action=None, **kwargs):
    request.session['nav']['menu'] = 'mydata'
    request.session['nav']['whos'] = 'mydata'
    
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
    
    form_environment = None
    form_objective = None
    form_stageLabel = None
    if o1_type =='image' or o2_type == 'image' or o3_type == 'image':
        form_environment = MetadataEnvironmentForm(initial={'image': manager.image})
        form_objective = MetadataObjectiveForm(initial={'image': manager.image, 'mediums': conn.getEnumerationEntries("MediumI"), 'immersions': conn.getEnumerationEntries("ImmersionI"), 'corrections': conn.getEnumerationEntries("CorrectionI") })
        form_stageLabel = MetadataStageLabelForm(initial={'image': manager.image })

    template = None
    if o3_type and o3_id:
        if o3_type == 'image':
            template = "omeroweb/image_details.html"
    elif o2_type and o2_id:
        if o2_type == 'dataset':
            manager.listMyImagesInDataset(o2_id, page)
        elif o2_type == 'image':
            template = "omeroweb/image_details.html"
    elif o1_type and o1_id:
        if o1_type == 'ajaxdataset':
            template = "omeroweb/container_subtree.html"
            manager.loadMyImages(o1_id)
        elif o1_type == 'project':
            manager.listMyDatasetsInProject(o1_id, page)
        elif o1_type == 'dataset':
            manager.listMyImagesInDataset(o1_id, page)
        elif o1_type == 'image':
            template = "omeroweb/image_details.html"
    elif o1_type == 'orphaned':
        manager.loadMyOrphanedImages()
    elif o1_type == 'ajaxorphaned':
        template = "omeroweb/container_subtree.html"
        manager.loadMyOrphanedImages()
    else:
        if view == 'tree':
            manager.loadMyContainerHierarchy()
        else:
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
                elif o1_type and o1_id:
                    if o1_type == 'project':
                        manager.createProjectCommentAnnotation(content)
                    elif o1_type == 'dataset':
                        manager.createDatasetCommentAnnotation(content)
                    elif o1_type == 'image':
                        manager.createImageCommentAnnotation(content)
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
                elif o1_type and o1_id:
                    if o1_type == 'project':
                        manager.createProjectUriAnnotation(content)
                    elif o1_type == 'dataset':
                        manager.createDatasetUriAnnotation(content)
                    elif o1_type == 'image':
                        manager.createImageUriAnnotation(content)
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
                elif o1_type and o1_id:
                    if o1_type == 'project':
                        manager.createProjectTagAnnotation(tag, desc)
                    elif o1_type == 'dataset':
                        manager.createDatasetTagAnnotation(tag, desc)
                    elif o1_type == 'image':
                        manager.createImageTagAnnotation(tag, desc)
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
                    elif o1_type and o1_id:
                        if o1_type == 'project':
                            manager.createProjectFileAnnotation(f)
                        elif o1_type == 'dataset':
                            manager.createDatasetFileAnnotation(f)
                        elif o1_type == 'image':
                            manager.createImageFileAnnotation(f)
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
                    elif o1_type and o1_id:
                        if o1_type == 'project':
                            manager.createProjectAnnotationLinks('tag',tags)
                        elif o1_type == 'dataset':
                            manager.createDatasetAnnotationLinks('tag',tags)
                        elif o1_type == 'image':
                            manager.createImageAnnotationLinks('tag',tags)
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
                    elif o1_type and o1_id:
                        if o1_type == 'project':
                            manager.createProjectAnnotationLinks('comment',comments)
                        elif o1_type == 'dataset':
                            manager.createDatasetAnnotationLinks('comment',comments)
                        elif o1_type == 'image':
                            manager.createImageAnnotationLinks('comment',comments)
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
                    elif o1_type and o1_id:
                        if o1_type == 'project':
                            manager.createProjectAnnotationLinks('url',urls)
                        elif o1_type == 'dataset':
                            manager.createDatasetAnnotationLinks('url',urls)
                        elif o1_type == 'image':
                            manager.createImageAnnotationLinks('url',urls)
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
                    elif o1_type and o1_id:
                        if o1_type == 'project':
                            manager.createProjectAnnotationLinks('file',files)
                        elif o1_type == 'dataset':
                            manager.createDatasetAnnotationLinks('file',files)
                        elif o1_type == 'image':
                            manager.createImageAnnotationLinks('file',files)
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
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_comment':form_comment, 'form_uri':form_uri, 'form_tag':form_tag, 'form_file':form_file, 'form_active_group':form_active_group, 'form_tags':form_tags, 'form_comments':form_comments, 'form_urls':form_urls, 'form_files':form_files}
    elif template is None and view =='table':
        template = "omeroweb/containers_table.html"
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_comment':form_comment, 'form_uri':form_uri, 'form_tag':form_tag, 'form_file':form_file, 'form_active_group':form_active_group, 'form_tags':form_tags, 'form_comments':form_comments, 'form_urls':form_urls, 'form_files':form_files}
    elif template is None and view =='tree' and o1_type is not None and o1_id > 0:
        template = "omeroweb/containers_table.html"
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_comment':form_comment, 'form_uri':form_uri, 'form_tag':form_tag, 'form_file':form_file, 'form_active_group':form_active_group, 'form_tags':form_tags, 'form_comments':form_comments, 'form_urls':form_urls, 'form_files':form_files}
    elif template is None and view =='tree' and o1_type is None and o1_id is None:
        template = "omeroweb/containers_tree.html"
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_comment':form_comment, 'form_uri':form_uri, 'form_tag':form_tag, 'form_file':form_file, 'form_active_group':form_active_group, 'form_tags':form_tags, 'form_comments':form_comments, 'form_urls':form_urls, 'form_files':form_files}
    elif template is not None and view == 'tree' and o1_type=='ajaxdataset' and o1_id > 0:
        context = {'manager':manager, 'eContext':manager.eContext}
    elif template is not None and view == 'tree' and o1_type=='ajaxorphaned':
        context = {'manager':manager, 'eContext':manager.eContext}
    else:
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_comment':form_comment, 'form_uri':form_uri, 'form_tag':form_tag, 'form_file':form_file, 'form_active_group':form_active_group, 'form_environment':form_environment, 'form_objective':form_objective, 'form_stageLabel':form_stageLabel, 'form_tags':form_tags, 'form_comments':form_comments, 'form_urls':form_urls, 'form_files':form_files}
    
    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def manage_user_containers(request, o1_type=None, o1_id=None, o2_type=None, o2_id=None, o3_type=None, o3_id=None, **kwargs):
    request.session['nav']['menu'] = 'collaboration'
    request.session['nav']['whos'] = 'userdata'
    
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
        
    grs = list()
    grs.extend(list(conn.getEventContext().memberOfGroups))
    grs.extend(list(conn.getEventContext().leaderOfGroups))
    my_groups = set(list(conn.getExperimenterGroups(set(grs))))
    request.session['groupId'] = None
    form_mygroups = MyGroupsForm(initial={'mygroups': my_groups})
    
    filter_user_id = None
    form_users = None
    users = set(conn.getColleaguesAndStaffs())
    
    try:
        if request.REQUEST['experimenter'] != "": 
            form_users = MyUserForm(initial={'users': users}, data=request.REQUEST.copy())
            if form_users.is_valid():
                filter_user_id = request.REQUEST['experimenter']
                request.session['experimenter'] = filter_user_id
                form_users = MyUserForm(initial={'user':filter_user_id, 'users': users})
            else:
                try:
                    filter_user_id = request.session['experimenter']
                except:
                    pass
        else:
            request.session['experimenter'] = None
            form_users = MyUserForm(initial={'users': users})
    except:
        try:
            filter_user_id = request.session['experimenter']
            form_users = MyUserForm(initial={'user':filter_user_id, 'users': users})
        except:
            form_users = MyUserForm(initial={'users': users})
    
    form_active_group = ActiveGroupForm(initial={'activeGroup':manager.eContext['context'].groupId, 'mygroups': manager.eContext['allGroups']})
    
    form_environment = None
    form_objective = None
    form_stageLabel = None
    if o1_type =='image' or o2_type == 'image' or o3_type == 'image':
        form_environment = MetadataEnvironmentForm(initial={'image': manager.image})
        form_objective = MetadataObjectiveForm(initial={'image': manager.image, 'mediums': conn.getEnumerationEntries("MediumI"), 'immersions': conn.getEnumerationEntries("ImmersionI"), 'corrections': conn.getEnumerationEntries("CorrectionI") })
        form_stageLabel = MetadataStageLabelForm(initial={'image': manager.image })
    
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
                elif o1_type and o1_id:
                    if o1_type == 'project':
                        manager.createProjectCommentAnnotation(content)
                    elif o1_type == 'dataset':
                        manager.createDatasetCommentAnnotation(content)
                    elif o1_type == 'image':
                        manager.createImageCommentAnnotation(content)
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
                elif o1_type and o1_id:
                    if o1_type == 'project':
                        manager.createProjectUriAnnotation(content)
                    elif o1_type == 'dataset':
                        manager.createDatasetUriAnnotation(content)
                    elif o1_type == 'image':
                        manager.createImageUriAnnotation(content)
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
                elif o1_type and o1_id:
                    if o1_type == 'project':
                        manager.createProjectTagAnnotation(tag, desc)
                    elif o1_type == 'dataset':
                        manager.createDatasetTagAnnotation(tag, desc)
                    elif o1_type == 'image':
                        manager.createImageTagAnnotation(tag, desc)
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
                            manager.saveImageFileAnnotation(f)
                    elif o2_type and o2_id:
                        if o2_type == 'dataset':
                            manager.saveDatasetFileAnnotation(f)
                        elif o2_type == 'image':
                            manager.saveImageFileAnnotation(f)
                    elif o1_type and o1_id:
                        if o1_type == 'project':
                            manager.saveProjectFileAnnotation(f)
                        elif o1_type == 'dataset':
                            manager.saveDatasetFileAnnotation(f)
                        elif o1_type == 'image':
                            manager.saveImageFileAnnotation(f)
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
                    elif o1_type and o1_id:
                        if o1_type == 'project':
                            manager.createProjectAnnotationLinks('tag',tags)
                        elif o1_type == 'dataset':
                            manager.createDatasetAnnotationLinks('tag',tags)
                        elif o1_type == 'image':
                            manager.createImageAnnotationLinks('tag',tags)
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
                    elif o1_type and o1_id:
                        if o1_type == 'project':
                            manager.createProjectAnnotationLinks('comment',comments)
                        elif o1_type == 'dataset':
                            manager.createDatasetAnnotationLinks('comment',comments)
                        elif o1_type == 'image':
                            manager.createImageAnnotationLinks('comment',comments)
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
                    elif o1_type and o1_id:
                        if o1_type == 'project':
                            manager.createProjectAnnotationLinks('url',urls)
                        elif o1_type == 'dataset':
                            manager.createDatasetAnnotationLinks('url',urls)
                        elif o1_type == 'image':
                            manager.createImageAnnotationLinks('url',urls)
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
                    elif o1_type and o1_id:
                        if o1_type == 'project':
                            manager.createProjectAnnotationLinks('file',files)
                        elif o1_type == 'dataset':
                            manager.createDatasetAnnotationLinks('file',files)
                        elif o1_type == 'image':
                            manager.createImageAnnotationLinks('file',files)
                    form_files = FileListForm(initial={'files':manager.listFiles()})
            
            form_comments = CommentListForm(initial={'comments':comment_list})
            form_tags = TagListForm(initial={'tags':tag_list})
            form_urls = UrlListForm(initial={'urls':url_list})
            
            form_tag = TagAnnotationForm()
            form_comment = CommentAnnotationForm()
            form_uri = UriAnnotationForm(initial={'link':'http://'})
            form_file = UploadFileForm()
    
    template = None
    if o3_type and o3_id:
        if o3_type == 'image':
            template = "omeroweb/image_details.html"
    elif o2_type and o2_id:
        if o2_type == 'dataset':
            if filter_user_id is not None:
                manager.listImagesInDatasetAsUser(o2_id, filter_user_id, page)
        elif o2_type == 'image':
            template = "omeroweb/image_details.html"
    elif o1_type and o1_id:
        if o1_type == 'ajaxdataset':
            template = "omeroweb/container_subtree.html"
            if filter_user_id is not None:
                manager.loadUserImages(o1_id, filter_user_id)
        elif o1_type == 'project':
            if filter_user_id is not None:
                manager.listDatasetsInProjectAsUser(o1_id, filter_user_id, page)
        elif o1_type == 'dataset':
            if filter_user_id is not None:
                manager.listImagesInDatasetAsUser(o1_id, filter_user_id, page)
        elif o1_type == 'image':
            template = "omeroweb/image_details.html"
    elif o1_type == 'orphaned':
        manager.loadUserOrphanedImages(filter_user_id)
    elif o1_type == 'ajaxorphaned':
        template = "omeroweb/container_subtree.html"
        manager.loadUserOrphanedImages(filter_user_id)
    else:
        if view == 'tree':
            if filter_user_id is not None:
                manager.loadUserContainerHierarchy(filter_user_id)
        else:
            if filter_user_id is not None:
                manager.listRootsAsUser(filter_user_id)

    
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
        template = "omeroweb/container_subtree.html"
        context = {'manager':manager, 'eContext':manager.eContext}
    elif template is not None and view == 'tree' and o1_type=='ajaxorphaned':
        template = "omeroweb/container_subtree.html"
        context = {'manager':manager, 'eContext':manager.eContext}
    else:
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager,  'form_comment':form_comment, 'form_uri':form_uri, 'form_tag':form_tag, 'form_file':form_file, 'form_environment':form_environment, 'form_objective':form_objective, 'form_stageLabel':form_stageLabel, 'form_tags':form_tags, 'form_comments':form_comments, 'form_urls':form_urls, 'form_files':form_files}
    
    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def manage_group_containers(request, o1_type=None, o1_id=None, o2_type=None, o2_id=None, o3_type=None, o3_id=None, **kwargs):
    request.session['nav']['menu'] = 'collaboration'
    request.session['nav']['whos'] = 'groupdata'
    
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
    
    form_users = None
    users = set(conn.getColleaguesAndStaffs())
    request.session['experimenter'] = None
    form_users = MyUserForm(initial={'users': users})
    
    filter_group_id = None
    form_mygroups = None
    grs = list()
    grs.extend(list(conn.getEventContext().memberOfGroups))
    grs.extend(list(conn.getEventContext().leaderOfGroups))
    my_groups = set(list(conn.getExperimenterGroups(set(grs))))
    try:
        if request.REQUEST['group'] != "": 
            form_mygroups = MyGroupsForm(initial={'mygroups': my_groups}, data=request.REQUEST.copy())
            if form_mygroups.is_valid():
                filter_group_id = request.REQUEST['group']
                request.session['groupId'] = filter_group_id
                form_mygroups = MyGroupsForm(initial={'mygroup':filter_group_id, 'mygroups': my_groups})
            else:
                try:
                    filter_group_id = request.session['groupId']
                except:
                    pass
        else:
            request.session['groupId'] = None
            form_mygroups = MyGroupsForm(initial={'mygroups': my_groups})
    except:
        try:
            filter_group_id = request.session['groupId']
            form_mygroups = MyGroupsForm(initial={'mygroup':filter_group_id, 'mygroups': my_groups})
        except:
            form_mygroups = MyGroupsForm(initial={'mygroups': my_groups})
    
    form_active_group = ActiveGroupForm(initial={'activeGroup':manager.eContext['context'].groupId, 'mygroups': manager.eContext['allGroups']})
    
    form_environment = None
    form_objective = None
    form_stageLabel = None
    if o1_type =='image' or o2_type == 'image' or o3_type == 'image':
        form_environment = MetadataEnvironmentForm(initial={'image': manager.image})
        form_objective = MetadataObjectiveForm(initial={'image': manager.image, 'mediums': conn.getEnumerationEntries("MediumI"), 'immersions': conn.getEnumerationEntries("ImmersionI"), 'corrections': conn.getEnumerationEntries("CorrectionI") })
        form_stageLabel = MetadataStageLabelForm(initial={'image': manager.image })
    
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
                elif o1_type and o1_id:
                    if o1_type == 'project':
                        manager.createProjectCommentAnnotation(content)
                    elif o1_type == 'dataset':
                        manager.createDatasetCommentAnnotation(content)
                    elif o1_type == 'image':
                        manager.createImageCommentAnnotation(content)
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
                elif o1_type and o1_id:
                    if o1_type == 'project':
                        manager.createProjectUriAnnotation(content)
                    elif o1_type == 'dataset':
                        manager.createDatasetUriAnnotation(content)
                    elif o1_type == 'image':
                        manager.createImageUriAnnotation(content)
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
                elif o1_type and o1_id:
                    if o1_type == 'project':
                        manager.createProjectTagAnnotation(tag, desc)
                    elif o1_type == 'dataset':
                        manager.createDatasetTagAnnotation(tag, desc)
                    elif o1_type == 'image':
                        manager.createImageTagAnnotation(tag, desc)
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
                    elif o1_type and o1_id:
                        if o1_type == 'project':
                            manager.createProjectFileAnnotation(f)
                        elif o1_type == 'dataset':
                            manager.createDatasetFileAnnotation(f)
                        elif o1_type == 'image':
                            manager.createImageFileAnnotation(f)
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
                    elif o1_type and o1_id:
                        if o1_type == 'project':
                            manager.createProjectAnnotationLinks('tag',tags)
                        elif o1_type == 'dataset':
                            manager.createDatasetAnnotationLinks('tag',tags)
                        elif o1_type == 'image':
                            manager.createImageAnnotationLinks('tag',tags)
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
                    elif o1_type and o1_id:
                        if o1_type == 'project':
                            manager.createProjectAnnotationLinks('comment',comments)
                        elif o1_type == 'dataset':
                            manager.createDatasetAnnotationLinks('comment',comments)
                        elif o1_type == 'image':
                            manager.createImageAnnotationLinks('comment',comments)
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
                    elif o1_type and o1_id:
                        if o1_type == 'project':
                            manager.createProjectAnnotationLinks('url',urls)
                        elif o1_type == 'dataset':
                            manager.createDatasetAnnotationLinks('url',urls)
                        elif o1_type == 'image':
                            manager.createImageAnnotationLinks('url',urls)
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
                    elif o1_type and o1_id:
                        if o1_type == 'project':
                            manager.createProjectAnnotationLinks('file',files)
                        elif o1_type == 'dataset':
                            manager.createDatasetAnnotationLinks('file',files)
                        elif o1_type == 'image':
                            manager.createImageAnnotationLinks('file',files)
                    form_files = FileListForm(initial={'files':manager.listFiles()})
            
            form_comments = CommentListForm(initial={'comments':comment_list})
            form_tags = TagListForm(initial={'tags':tag_list})
            form_urls = UrlListForm(initial={'urls':url_list})
            
            form_tag = TagAnnotationForm()
            form_comment = CommentAnnotationForm()
            form_uri = UriAnnotationForm(initial={'link':'http://'})
            form_file = UploadFileForm()
        
    template = None
    if o3_type and o3_id:
        if o3_type == 'image':
            template = "omeroweb/image_details.html"
    elif o2_type and o2_id:
        if o2_type == 'dataset':
            if filter_group_id is not None:
                manager.listImagesInDatasetInGroup(o2_id, filter_group_id, page)
        if o2_type == 'image':
            template = "omeroweb/image_details.html"
    elif o1_type and o1_id:
        if o1_type == 'ajaxdataset':
            template = "omeroweb/container_subtree.html"
            if filter_group_id is not None:
                manager.loadGroupImages(o1_id, filter_group_id)
        elif o1_type == 'project':
            if filter_group_id is not None:
                manager.listDatasetsInProjectInGroup(o1_id, filter_group_id, page)
        elif o1_type == 'dataset':
            if filter_group_id is not None:
                manager.listImagesInDatasetInGroup(o1_id, filter_group_id, page)
        if o1_type == 'image':
            template = "omeroweb/image_details.html"
    elif o1_type == 'orphaned':
        manager.loadGroupOrphanedImages(filter_group_id)
    elif o1_type == 'ajaxorphaned':
        template = "omeroweb/container_subtree.html"
        manager.loadGroupOrphanedImages(filter_group_id)
    else:
        if view == 'tree':
            if filter_group_id is not None:
                manager.loadGroupContainerHierarchy(filter_group_id)
        else:
            if filter_group_id is not None:
                manager.listRootsInGroup(filter_group_id)
                    
    if template is None and view =='icon':
        template = "omeroweb/containers_icon.html"
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_comment':form_comment, 'form_uri':form_uri, 'form_tag':form_tag, 'form_file':form_file, 'form_mygroups':form_mygroups, 'form_users':form_users, 'form_active_group':form_active_group, 'form_tags':form_tags, 'form_comments':form_comments, 'form_urls':form_urls, 'form_files':form_files}
    elif template is None and view =='table':
        template = "omeroweb/containers_table.html"
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_comment':form_comment, 'form_uri':form_uri, 'form_tag':form_tag, 'form_file':form_file, 'form_mygroups':form_mygroups, 'form_users':form_users, 'form_active_group':form_active_group, 'form_tags':form_tags, 'form_comments':form_comments, 'form_urls':form_urls, 'form_files':form_files}
    elif template is None and view =='tree' and o1_type is not None and o1_id > 0:
        template = "omeroweb/containers_table.html"
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_comment':form_comment, 'form_uri':form_uri, 'form_tag':form_tag, 'form_file':form_file, 'form_active_group':form_active_group, 'form_tags':form_tags, 'form_comments':form_comments, 'form_urls':form_urls, 'form_files':form_files}
    elif template is None and view =='tree' and o1_type is None and o1_id is None:
        template = "omeroweb/containers_tree.html"
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_comment':form_comment, 'form_uri':form_uri, 'form_tag':form_tag, 'form_file':form_file, 'form_mygroups':form_mygroups, 'form_users':form_users, 'form_active_group':form_active_group, 'form_tags':form_tags, 'form_comments':form_comments, 'form_urls':form_urls, 'form_files':form_files}
    elif template is not None and view == 'tree' and o1_type=='ajaxdataset' and o1_id > 0:
        template = "omeroweb/container_subtree.html"
        context = {'manager':manager, 'eContext':manager.eContext}
    elif template is not None and view == 'tree' and o1_type=='ajaxorphaned':
        template = "omeroweb/container_subtree.html"
        context = {'manager':manager, 'eContext':manager.eContext}
    else:
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager,  'form_comment':form_comment, 'form_uri':form_uri, 'form_tag':form_tag, 'form_file':form_file, 'form_environment':form_environment, 'form_objective':form_objective, 'form_stageLabel':form_stageLabel, 'form_tags':form_tags, 'form_comments':form_comments, 'form_urls':form_urls, 'form_files':form_files}
    
    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def manage_data_by_tag(request, tid=None, tid2=None, tid3=None, tid4=None, tid5=None, **kwargs):
    request.session['nav']['menu'] = 'mydata'
    request.session['nav']['whos'] = 'mydata'
    
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
        ext = ""
        for t in manager.tags:
            if t is not None:
                ext = ext + "%i/" % (t.id)
        return HttpResponseRedirect("/%s/tag/%s" % (settings.WEBCLIENT_ROOT_BASE, ext))
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
    
    json_data = simplejson.dumps(list(conn.getAllTags()))
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
    if o_type == "dataset" or o_type == "project" or o_type == "image":
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
        form = ContainerForm(initial={'access_controll': '0'})
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
            return HttpResponseRedirect("/%s/action/new/" % (settings.WEBCLIENT_ROOT_BASE))
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
    if request.session['nav']['menu'] != 'collaboration' and request.session['nav']['menu'] != 'mydata':
        if image.image.details.owner.id.val == image.eContext['context'].userId:
            request.session['nav']['menu'] = 'mydata'
        elif image.image.details.group.id.val == image.eContext['context'].groupId:
            request.session['nav']['menu'] = 'collaboration'
        else:
            request.session['nav']['menu'] = 'collaboration'
    
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
def manage_share(request, action, oid=None, **kwargs):
    request.session['nav']['menu'] = 'share'
    request.session['nav']['whos'] = 'share'
    
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
        share = BaseShare(request.session['nav']['menu'], conn, None, oid, action)
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
            host = '%s://%s:%s/%s' % (request.META['wsgi.url_scheme'], request.META['SERVER_NAME'], request.META['SERVER_PORT'], settings.WEBCLIENT_ROOT_BASE)
            share.createShare(host, request.session['server'], request.session['imageInBasket'], message, members, enable, expiration)
            return HttpResponseRedirect("/%s/share/" % (settings.WEBCLIENT_ROOT_BASE))
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
            host = '%s://%s:%s/%s' % (request.META['wsgi.url_scheme'], request.META['SERVER_NAME'], request.META['SERVER_PORT'], settings.WEBCLIENT_ROOT_BASE)
            share.createDiscussion(host, request.session['server'], message, members, enable, expiration)
            return HttpResponseRedirect("/%s/share/" % (settings.WEBCLIENT_ROOT_BASE))
        else:
            basket = BaseBasket(conn)
            basket.load_basket(request)
            form_active_group = ActiveGroupForm(initial={'activeGroup':basket.eContext['context'].groupId, 'mygroups': basket.eContext['allGroups']})
            context = {'nav':request.session['nav'], 'eContext': share.eContext, 'basket':basket, 'form':form, 'form_active_group':form_active_group}
    elif action == 'edit':
        template = "omeroweb/share_form.html"
        share.getMembers(oid)
        share.getComments(oid)
        
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
            share.updateShare(message, members, enable, expiration)
            return HttpResponseRedirect("/%s/share/" % (settings.WEBCLIENT_ROOT_BASE))
        else:
            template = "omeroweb/share_form.html"
            share.getShare(oid)
            share.getComments(oid)
            context = {'nav':request.session['nav'], 'eContext': share.eContext, 'share':share, 'form':form, 'form_active_group':form_active_group}
    elif action == 'delete':
        return HttpResponseRedirect("/%s/share/" % (settings.WEBCLIENT_ROOT_BASE))
    elif action == 'view':
        template = "omeroweb/share_details.html"
        share.getAllUsers(oid)
        share.getComments(oid)
        form = ShareCommentForm()
        context = {'nav':request.session['nav'], 'eContext': share.eContext, 'share':share, 'form':form, 'form_active_group':form_active_group}
    elif action == 'comment':
        f = ShareCommentForm(data=request.REQUEST.copy())
        if f.is_valid():
            comment = request.REQUEST['comment'].encode('utf-8')
            host = '%s://%s:%s/%s' % (request.META['wsgi.url_scheme'], request.META['SERVER_NAME'], request.META['SERVER_PORT'], settings.WEBCLIENT_ROOT_BASE)
            share.addComment(host, request.session['server'], comment)
            return HttpResponseRedirect("/%s/share/view/%s/" % (settings.WEBCLIENT_ROOT_BASE, oid))
        else:
            template = "omeroweb/share_details.html"
            share.getComments(oid)
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
        conn_share = getShareConnection(request)
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


##################################################################
# Basket

@isUserConnected
def basket_action (request, action=None, oid=None, **kwargs):
    request.session['nav']['menu'] = ''
    request.session['nav']['whos'] = ''
    
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
        experimenters = conn.getExperimenters()
        
        form_active_group = ActiveGroupForm(initial={'activeGroup':basket.eContext['context'].groupId, 'mygroups': basket.eContext['allGroups']})
        
        form = ShareForm(initial={'experimenters': experimenters})
        context = {'nav':request.session['nav'], 'eContext':basket.eContext, 'basket':basket, 'form':form, 'form_active_group':form_active_group}
    elif action == "todiscuss":
        template = "omeroweb/basket_discuss_action.html"
        
        basket = BaseBasket(conn)
        basket.buildBreadcrumb(action)
        experimenters = conn.getExperimenters()
        
        form_active_group = ActiveGroupForm(initial={'activeGroup':basket.eContext['context'].groupId, 'mygroups': basket.eContext['allGroups']})
        
        form = ShareForm(initial={'experimenters': experimenters})
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
        
    request.session['imageInBasket'] = list()
    request.session['nav']['basket'] = 0
    return HttpResponseRedirect("/%s/basket/" % (settings.WEBCLIENT_ROOT_BASE))

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
                elif request.REQUEST['productType'] == 'share':
                    rv = "Error: This action is not available"
                    return HttpResponse(rv)
            elif action == 'del':
                if ptype == 'image':
                    try:
                        request.session['imageInBasket'].remove(prod)
                    except:
                        rv = "Error: could not remove image from the basket."
                        return HttpResponse(rv)
                elif request.REQUEST['productType'] == 'share':
                    rv = "Error: This action is not available"
                    return HttpResponse(rv)
        total = len(request.session['imageInBasket'])
        request.session['nav']['basket'] = total
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
                if len(request.session['clipboard']) > 0 and request.session['clipboard'][0] != ptype and request.session['clipboard'][1] != prod:
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
def impexp(request, menu, **kwargs):
    request.session['nav']['menu'] = 'import'
    template = "omeroweb/impexp.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    controller = BaseImpexp(conn)

    form_active_group = ActiveGroupForm(initial={'activeGroup':controller.eContext['context'].groupId, 'mygroups': controller.eContext['allGroups']})
    
    context = {'nav':request.session['nav'], 'eContext': controller.eContext, 'controller':controller, 'form_active_group':form_active_group}
    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def myaccount(request, action, **kwargs):
    request.session['nav']['menu'] = 'person'
    template = "omeroweb/myaccount.html"
    
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
    eContext['user'] = conn.getUserWrapped()
    eContext['breadcrumb'] = ["My Account",  controller.experimenter.id]
    
    grs = list(conn.getGroupsMemberOf())
    grs.extend(list(conn.getGroupsLeaderOf()))
    eContext['memberOfGroups']  = controller.sortByAttr(grs, "name")
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
    
    form_file = UploadPhotoForm()
    
    if action == "save":
        form = MyAccountForm(data=request.REQUEST.copy(), initial={'groups':controller.otherGroups})
        if form.is_valid():
            firstName = request.REQUEST['first_name'].encode('utf-8')
            middleName = request.REQUEST['middle_name'].encode('utf-8')
            lastName = request.REQUEST['last_name'].encode('utf-8')
            email = request.REQUEST['email'].encode('utf-8')
            institution = request.REQUEST['institution'].encode('utf-8')
            defaultGroup = request.REQUEST['default_group']
            password = str(request.REQUEST['password'].encode('utf-8'))
            controller.updateMyAccount(firstName, lastName, email, defaultGroup, middleName, institution, password)
            return HttpResponseRedirect("/%s/myaccount/details/" % (settings.WEBCLIENT_ROOT_BASE))
    elif action == "upload":
        if request.method == 'POST':
            form_file = UploadPhotoForm(request.POST, request.FILES)
            if form_file.is_valid():
                controller = BaseUploadFile(conn)
                controller.attach_photo(request.FILES['photo'])
                return HttpResponseRedirect("/%s/myaccount/details/" % (settings.WEBCLIENT_ROOT_BASE))
    
    form_active_group = ActiveGroupForm(initial={'activeGroup':eContext['context'].groupId, 'mygroups': eContext['memberOfGroups']})
    context = {'nav':request.session['nav'], 'eContext': eContext, 'form':form, 'ldapAuth': controller.ldapAuth, 'form_active_group':form_active_group, 'form_file':form_file}
    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def help(request, menu, **kwargs):
    request.session['nav']['menu'] = 'help'
    template = "omeroweb/help.html"

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
def history(request, menu, year, month, **kwargs):
    request.session['nav']['menu'] = 'history'
    template = "omeroweb/history.html"

    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    controller = BaseCalendar(conn=conn, year=year, month=month)
    controller.create_calendar()
    
    form_active_group = ActiveGroupForm(initial={'activeGroup':controller.eContext['context'].groupId, 'mygroups': controller.eContext['allGroups']})
    
    context = {'nav':request.session['nav'], 'eContext': controller.eContext, 'controller':controller, 'form_active_group':form_active_group}
    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def history_details(request, menu, year, month, day, **kwargs):
    request.session['nav']['menu'] = 'history'
    request.session['nav']['whos'] = 'mydata'
    
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
        
    cal_type = None
    try:
        cal_type = request.REQUEST['history_type']
        if cal_type == "all":
            cal_type = None
    except:
        cal_type = None
    
    template = "omeroweb/history_details.html"
    
    controller = BaseCalendar(conn=conn, year=year, month=month, day=day)
    controller.get_items(cal_type)
    
    form_active_group = ActiveGroupForm(initial={'activeGroup':controller.eContext['context'].groupId, 'mygroups': controller.eContext['allGroups']})
    
    if cal_type is None:
        form_history_type = HistoryTypeForm()
    else:
        form_history_type = HistoryTypeForm(initial={'data_type':cal_type})
    
    context = {'nav':request.session['nav'], 'url':url, 'eContext': controller.eContext, 'controller':controller, 'form_active_group':form_active_group, 'form_history_type':form_history_type}
    t = template_loader.get_template(template)
    c = Context(request,context)
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
def render_thumbnail (request, iid, **kwargs):
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")

    img = conn.getImage(iid)
    if img is None:
        try:
            conn = getShareConnection(request)
        except Exception, x:
            logger.debug(traceback.format_exc())
            raise x
        if conn is None:
            raise Http500("Share connection not available")
        img = conn.getImage(iid)
    
    if img is None:
        logger.debug("Image %s not found..." % (str(iid)))
        return handlerInternalError("Image %s not found..." % (str(iid)))
    
    jpeg_data = img.getThumbnail()
    return HttpResponse(jpeg_data, mimetype='image/jpeg')

@isUserConnected
def render_thumbnail_details (request, iid, **kwargs):
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")

    img = conn.getImage(iid)
    if img is None:
        try:
            conn = getShareConnection(request)
        except Exception, x:
            logger.debug(traceback.format_exc())
            raise x
        if conn is None:
            raise Http500("Share connection not available")
        img = conn.getImage(iid)
    
    if img is None:
        logger.debug("Image %s not found..." % (str(iid)))
        return handlerInternalError("Image %s not found..." % (str(iid)))
    
    side = 0
    if img.getWidth() > img.getHeight():
        side = img.getWidth() 
    else:
        side = img.getHeight()
    size = 0
    if side < 400:
        size = side
    else:
        size = 400
    
    jpeg_data = img.getThumbnailByLongestSide(size=size)
    return HttpResponse(jpeg_data, mimetype='image/jpeg')

@isUserConnected
def render_thumbnail_resize (request, size, iid, **kwargs):
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")

    img = conn.getImage(iid)
    if img is None:
        try:
            conn = getShareConnection(request)
        except Exception, x:
            logger.debug(traceback.format_exc())
            raise x
        if conn is None:
            raise Http500("Share connection not available")
        img = conn.getImage(iid)
    
    if img is None:
        logger.debug("Image %s not found..." % (str(iid)))
        return handlerInternalError("Image %s not found..." % (str(iid)))
        
    jpeg_data = img.getThumbnail((int(size),int(size)))
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
        try:
            conn = getShareConnection(request)
        except Exception, x:
            logger.debug(traceback.format_exc())
            raise x
        if conn is None:
            raise Http500("Share connection not available")
        img = conn.getImage(iid)
    
    if img is None:
        logger.debug("Image %s not found..." % (str(iid)))
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
    
    jpeg_data = img.getThumbnailByLongestSide(size=size)
    return HttpResponse(jpeg_data, mimetype='image/jpeg')
    
class UserAgent (object):
    def __init__ (self, request):
        self.ua = request.META['HTTP_USER_AGENT']

    def isIE (self):
        return 'MSIE' in self.ua

    def isFF (self):
        return 'Firefox' in self.ua

    def isSafari (self):
        return 'Safari' in self.ua

@isUserConnected
def _get_prepared_image (request, iid, **kwargs):
    r = request.REQUEST
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")

    img = conn.getImage(iid)
    if img is None:
        try:
            conn = getShareConnection(request)
        except Exception, x:
            logger.debug(traceback.format_exc())
            raise x
        if conn is None:
            raise Http500("Share connection not available")
        img = conn.getImage(iid)
    
    if img is None:
        logger.debug("Image %s not found..." % (str(iid)))
        return handlerInternalError("Image %s not found..." % (str(iid)))
    
    if r.has_key('c'):
        logger.debug("c="+r['c'])
        channels, windows, colors =  _split_channel_info(r['c'])
        if not img.setActiveChannels(channels, windows, colors):
            logger.debug("Could not set the active channels")
    if r.get('m', None) == 'g':
        img.setGreyscaleRenderingModel()
    elif r.get('m', None) == 'c':
        img.setColorRenderingModel()
    compress_quality = r.get('q', None)
    return (img, compress_quality)

@isUserConnected
def render_image (request, iid, z, t, **kwargs):
    """ Renders the image with id {{iid}} at {{z}} and {{t}} as jpeg.
        Many options are available from the request dict.
    I am assuming a single Pixels object on image with id='iid'. May be wrong """

    r = request.REQUEST
    
    pi = _get_prepared_image(request, iid)
    img, compress_quality = pi
    jpeg_data = img.renderJpeg(z,t, compression=compress_quality)
    return HttpResponse(jpeg_data, mimetype='image/jpeg')

@isUserConnected
def image_viewer (request, iid, dsid=None, **kwargs):
    """ This view is responsible for showing pixel data as images """
    user_agent = UserAgent(request)
    rid = _get_img_details_from_req(request)
    rk = "&".join(["%s=%s" % (x[0], x[1]) for x in rid.items()])
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")

    img = conn.getImage(iid)
    if img is None:
        try:
            conn = getShareConnection(request)
        except Exception, x:
            logger.debug(traceback.format_exc())
            raise x
        if conn is None:
            raise Http500("Share connection not available")
        img = conn.getImage(iid)
    
    if img is None:
        logger.debug("Image %s not found..." % (str(iid)))
        return handlerInternalError("Image %s not found..." % (str(iid)))
    
    if dsid is not None:
        ds = conn.getDataset(dsid)
    else:
        ds = None
    context = {'conn': conn, 'image': img, 'dataset': ds, 'opts': rid, 'user_agent': user_agent, 'object': 'image:%i' % long(iid)}
    template = "omeroweb/omero_image.html"
    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

def _split_channel_info (rchannels):
    channels = []
    windows = []
    colors = []
    for chan in rchannels.split(','):
        chan = chan.split('|')
        t = chan[0].strip()
        color = None
        if t.find('$')>=0:
            t,color = t.split('$')
        try:
            channels.append(long(t))
            ch_window = (None, None)
            if len(chan) > 1:
                t = chan[1].strip()
                if t.find('$')>=0:
                    t, color = t.split('$')
                t = t.split(':')
                if len(t) == 2:
                    try:
                        ch_window = [float(x) for x in t]
                    except ValueError:
                        pass
            windows.append(ch_window)
            colors.append(color)
        except ValueError:
            pass
    logger.debug(str(channels)+","+str(windows)+","+str(colors))
    return channels, windows, colors

def _get_img_details_from_req (request, as_string=False):
    """ Break the GET information from the request object into details on how to render the image.
    The following keys are recognized:
    z - Z axis position
    t - T axis position
    q - Quality set (0,0..1,0)
    m - Model (g for greyscale, c for color)
    x - X position (for now based on top/left offset on the browser window)
    y - Y position (same as above)
    c - a comma separated list of channels to be rendered (start index 1)
      - format for each entry [-]ID[|wndst:wndend][#HEXCOLOR][,...]
    zm - the zoom setting (as a percentual value)
    """
    r = request.REQUEST
    rv = {}
    for k in ('z', 't', 'q', 'm', 'zm', 'x', 'y'):
        if r.has_key(k):
           rv[k] = r[k]
    if r.has_key('c'):
        rv['c'] = []
        ci = _split_channel_info(r['c'])
        logger.debug(ci)
        for i in range(len(ci[0])):
            # a = abs channel, i = channel, s = window start, e = window end, c = color
          rv['c'].append({'a':abs(ci[0][i]), 'i':ci[0][i], 's':ci[1][i][0], 'e':ci[1][i][1], 'c':ci[2][i]})
    if as_string:
        return "&".join(["%s=%s" % (x[0], x[1]) for x in rv.items()])
    return rv

@isUserConnected
def imageData_json (request, iid, **kwargs):
    """ Get a dict with image information """
    r = request.REQUEST
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")

    img = conn.getImage(iid)
    if img is None:
        try:
            conn = getShareConnection(request)
        except Exception, x:
            logger.debug(traceback.format_exc())
            raise x
        if conn is None:
            raise Http500("Share connection not available")
        img = conn.getImage(iid)
    
    if img is None:
        logger.debug("Image %s not found..." % (str(iid)))
        return handlerInternalError("Image %s not found..." % (str(iid)))
    
    rv = {
        'id': iid,
        'size': {'width': img.getWidth(),
                 'height': img.getHeight(),
                 'z': img.z_count(),
                 't': img.t_count(),
                 'c': img.c_count(),},
        'pixel_size': {'x': img.getPixelSizeX(),
                       'y': img.getPixelSizeY(),
                       'z': img.getPixelSizeZ(),},
        'rdefs': {'model': img.isGreyscaleRenderingModel() and 'greyscale' or 'color',
                  },
        'channels': map(lambda x: {'emissionWave': x.getEmissionWave(),
                                   'color': x.getColor().getHtml(),
                                   'window': {'min': x.getWindowMin(),
                                              'max': x.getWindowMax(),
                                              'start': x.getWindowStart(),
                                              'end': x.getWindowEnd(),},
                                   'active': x.isActive()}, img.getChannels()),
        'meta': {'name': img.name or '',
                 'description': img.description or '',
                 'author':img.getOwner(),
                 'timestamp': img.getDateAsTimestamp(),},
        }
    json_data = simplejson.dumps(rv)
    return HttpResponse(json_data, mimetype='application/javascript')

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


def defaultThumbnail():
    img = Image.new("RGB", (200,200), "#FFF")
    draw = ImageDraw.Draw(img)
    draw.text((73,95), "no image", font=None, fill="#000")
    draw.line((0,0) + img.size, fill=128)
    draw.line((0, img.size[1], img.size[0], 0), fill=128)
    f = cStringIO.StringIO()
    img.save(f, "PNG")
    f.seek(0)
    return f.read()
