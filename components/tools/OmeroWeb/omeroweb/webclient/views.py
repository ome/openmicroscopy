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
import re
import os
import calendar
import cStringIO
import datetime
import httplib
import Ice
import locale
import logging
import traceback

from time import time
from thread import start_new_thread

from omero_version import omero_version
import omero, omero.scripts 

from django.conf import settings
from django.contrib.sessions.backends.cache import SessionStore
from django.core import template_loader
from django.core.cache import cache
from django.http import HttpResponse, HttpResponseRedirect, HttpResponseServerError
from django.shortcuts import render_to_response
from django.template import RequestContext as Context
from django.utils import simplejson
from django.views.defaults import page_not_found, server_error
from django.views import debug
from django.core.urlresolvers import reverse
from django.utils.translation import ugettext_lazy as _
from django.utils.encoding import smart_str

from webclient_http import HttpJavascriptRedirect, HttpJavascriptResponse, HttpLoginRedirect

from webclient_utils import _formatReport, _purgeCallback
from forms import ShareForm, BasketShareForm, ShareCommentForm, \
                    ContainerForm, ContainerNameForm, ContainerDescriptionForm, \
                    CommentAnnotationForm, TagAnnotationForm, \
                    UploadFileForm, UsersForm, ActiveGroupForm, HistoryTypeForm, \
                    MetadataFilterForm, MetadataDetectorForm, MetadataChannelForm, \
                    MetadataEnvironmentForm, MetadataObjectiveForm, MetadataObjectiveSettingsForm, MetadataStageLabelForm, \
                    MetadataLightSourceForm, MetadataDichroicForm, MetadataMicroscopeForm, \
                    TagListForm, FileListForm, TagFilterForm, \
                    MultiAnnotationForm, \
                    WellIndexForm

from controller import sortByAttr, BaseController
from controller.index import BaseIndex
from controller.basket import BaseBasket
from controller.container import BaseContainer
from controller.help import BaseHelp
from controller.history import BaseCalendar
from controller.impexp import BaseImpexp
from controller.search import BaseSearch
from controller.share import BaseShare

from omeroweb.webadmin.forms import MyAccountForm, UploadPhotoForm, LoginForm, ChangePassword
from omeroweb.webadmin.controller.experimenter import BaseExperimenter 
from omeroweb.webadmin.controller.uploadfile import BaseUploadFile
from omeroweb.webadmin.views import _checkVersion, _isServerOn
from omeroweb.webclient.webclient_utils import toBoolean, string_to_dict, upgradeCheck

from omeroweb.webgateway.views import getBlitzConnection
from omeroweb.webgateway import views as webgateway_views

from omeroweb.feedback.views import handlerInternalError

logger = logging.getLogger('views-web')

connectors = {}
share_connectors = {}

logger.info("INIT '%s'" % os.getpid())


################################################################################
# Blitz Gateway Connection

def getShareConnection (request, share_id):
    browsersession_key = request.session.session_key
    share_conn_key = "S:%s#%s#%s" % (browsersession_key, request.session.get('server'), share_id)
    share = getBlitzConnection(request, force_key=share_conn_key, useragent="OMERO.web")
    share.attachToShare(share_id)
    request.session['shares'][share_id] = share._sessionUuid
    request.session.modified = True    
    logger.debug('shared connection: %s : %s' % (share_id, share._sessionUuid))
    return share

################################################################################
# decorators

def isUserConnected (f):
    def wrapped (request, *args, **kwargs):
        #this check the connection exist, if not it will redirect to login page
        server = string_to_dict(request.REQUEST.get('path')).get('server',request.REQUEST.get('server', None))
        url = request.REQUEST.get('url')
        if url is None or len(url) == 0:
            if request.META.get('QUERY_STRING'):
                url = '%s?%s' % (request.META.get('PATH_INFO'), request.META.get('QUERY_STRING'))
            else:
                url = '%s' % (request.META.get('PATH_INFO'))
        
        conn = None
        try:
            conn = getBlitzConnection(request, useragent="OMERO.web")
        except Exception, x:
            logger.error(traceback.format_exc())
        
        if conn is None:
            # TODO: Should be changed to use HttpRequest.is_ajax()
            # http://docs.djangoproject.com/en/dev/ref/request-response/
            # Thu  6 Jan 2011 09:57:27 GMT -- callan at blackcat dot ca
            if request.is_ajax():
                return HttpResponseServerError(reverse("weblogin"))
            if server is not None:
                return HttpLoginRedirect(reverse("weblogin")+(("?url=%s&server=%s") % (url,server)))
            return HttpLoginRedirect(reverse("weblogin")+(("?url=%s") % url))
            
        conn_share = None     
        share_id = kwargs.get('share_id', None)
        if share_id is not None:
            sh = conn.getShare(share_id)
            if sh is not None:
                try:
                    conn_share = getShareConnection(request, share_id)
                except Exception, x:
                    logger.error(traceback.format_exc())
        
        sessionHelper(request)
        kwargs["error"] = request.REQUEST.get('error')
        kwargs["conn"] = conn
        kwargs["conn_share"] = conn_share
        kwargs["url"] = url
        return f(request, *args, **kwargs)
    return wrapped

def sessionHelper(request):
    changes = False
    if request.session.get('callback') is None:
        request.session['callback'] = dict()
        changes = True
    if request.session.get('clipboard') is None:
        request.session['clipboard'] = list()
        changes = True
    if request.session.get('shares') is None:
        request.session['shares'] = dict()
        changes = True
    if request.session.get('imageInBasket') is None:
        request.session['imageInBasket'] = set()
        changes = True
    #if request.session.get('datasetInBasket') is None:
    #    request.session['datasetInBasket'] = set()
    if request.session.get('nav') is None:
        if request.session.get('server') is not None:
            blitz = settings.SERVER_LIST.get(pk=request.session.get('server'))
        elif request.session.get('host') is not None:
            blitz = settings.SERVER_LIST.get(host=request.session.get('host'))
        blitz = "%s:%s" % (blitz.host, blitz.port)
        request.session['nav']={"blitz": blitz, "menu": "mydata", "view": "tree", "basket": 0, "experimenter":None}
        changes = True
    if changes:
        request.session.modified = True        
        
################################################################################
# views controll

def login(request):
    request.session.modified = True    
    
    if request.REQUEST.get('server'):      
        
        blitz = settings.SERVER_LIST.get(pk=request.REQUEST.get('server')) 
        request.session['server'] = blitz.id
        request.session['host'] = blitz.host
        request.session['port'] = blitz.port
        request.session['username'] = smart_str(request.REQUEST.get('username',None))
        request.session['password'] = smart_str(request.REQUEST.get('password',None))
        request.session['ssl'] = (True, False)[request.REQUEST.get('ssl') is None]
        request.session['clipboard'] = {'images': None, 'datasets': None, 'plates': None}
        request.session['shares'] = dict()
        request.session['imageInBasket'] = set()
        blitz_host = "%s:%s" % (blitz.host, blitz.port)
        request.session['nav']={"error": None, "blitz": blitz_host, "menu": "start", "view": "icon", "basket": 0, "experimenter":None, 'callback':dict()}
        
    error = request.REQUEST.get('error')
    
    conn = None
    try:
        conn = getBlitzConnection(request, useragent="OMERO.web")
    except Exception, x:
        error = x.__class__.__name__
    
    if conn is not None:
        upgradeCheck()
        request.session['version'] = conn.getServerVersion()
        if request.REQUEST.get('noredirect'):
            return HttpResponse('OK')
        url = request.REQUEST.get("url")
        if url is not None and len(url) != 0:
            return HttpResponseRedirect(url)
        else:
            return HttpResponseRedirect(reverse("webindex"))
    else:
        if request.method == 'POST' and request.REQUEST.get('server'):
            if not _isServerOn(request.session.get('host'), request.session.get('port')):
                error = "Server is not responding, please contact administrator."
            elif not _checkVersion(request.session.get('host'), request.session.get('port')):
                error = "Client version does not match server, please contact administrator."
            else:
                error = "Connection not available, please check your user name and password."
        url = request.REQUEST.get("url")
        request.session['server'] = request.REQUEST.get('server')
        
        template = "webclient/login.html"
        if request.method == 'POST':
            form = LoginForm(data=request.REQUEST.copy())
        else:
            blitz = settings.SERVER_LIST.get(pk=request.session.get('server')) 
            if blitz is not None:
                initial = {'server': unicode(blitz.id)}
                form = LoginForm(initial=initial)
            else:
                form = LoginForm()
        
        context = {"version": omero_version, 'error':error, 'form':form, 'url': url}
        if url is not None and len(url) != 0:
            context['url'] = url
        
        t = template_loader.get_template(template)
        c = Context(request, context)
        rsp = t.render(c)
        return HttpResponse(rsp)

@isUserConnected
def index(request, **kwargs):
    template = "webclient/index/index.html"
    
    request.session['nav']['error'] = request.REQUEST.get('error')
    
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
        if request.session['nav']['menu'] != 'start':
            request.session['nav']['menu'] = 'home'
    except:
        request.session['nav']['menu'] = 'start'
    
    controller = BaseIndex(conn)
    #controller.loadData()
    form_active_group = ActiveGroupForm(initial={'activeGroup':controller.eContext['context'].groupId, 'mygroups': controller.eContext['allGroups'], 'url':url})
    
    context = {'nav':request.session['nav'], 'controller':controller, 'eContext': controller.eContext, 'form_active_group':form_active_group}

    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isUserConnected
def index_context(request, **kwargs):
    template = "webclient/index/index_context.html"
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    controller = BaseIndex(conn)
    #controller.loadData()
    
    context = {'nav':request.session['nav'], 'controller':controller}
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isUserConnected
def index_last_imports(request, **kwargs):
    template = "webclient/index/index_last_imports.html"
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
    template = "webclient/index/index_most_recent.html"
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
    template = "webclient/index/index_tag_cloud.html"
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
    
    url = None
    try:
        url = kwargs["url"]
    except:
        logger.error(traceback.format_exc())
    
    server = request.session.get('server')
    username = request.session.get('username')
    password = request.session.get('password')
    ssl = request.session.get('ssl')
    version = request.session.get('version')
       
    webgateway_views._session_logout(request, request.session.get('server'))
    
    blitz = settings.SERVER_LIST.get(pk=server) 
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
    
    conn = getBlitzConnection(request, useragent="OMERO.web")

    active_group = request.REQUEST.get('active_group')
    if conn.changeActiveGroup(active_group):
        request.session.modified = True                
    else:
        error = 'You cannot change your group becuase the data is currently processing. You can force it by logging out and logging in again.'
        url = reverse("webindex")+ ("?error=%s" % error)
        if request.session.get('nav')['experimenter'] is not None:
            url += "&experimenter=%s" % request.session.get('nav')['experimenter']
    
    request.session['version'] = conn.getServerVersion()
    
    return HttpResponseRedirect(url)
    
@isUserConnected
def logout(request, **kwargs):
    webgateway_views._session_logout(request, request.session.get('server'))
     
    try:
        if request.session.get('shares') is not None:
            for key in request.session.get('shares').iterkeys():
                session_key = "S:%s#%s#%s" % (request.session.session_key,request.session.get('server'), key)
                webgateway_views._session_logout(request,request.session.get('server'), force_key=session_key)
        for k in request.session.keys():
            if request.session.has_key(k):
                del request.session[k]      
    except:
        logger.error(traceback.format_exc())
    
    #request.session.set_expiry(1)
    return HttpResponseRedirect(reverse("webindex"))


###########################################################################
@isUserConnected
def load_template(request, menu, **kwargs):
    request.session.modified = True
        
    if menu == 'userdata':
        template = "webclient/data/containers.html"
    elif menu == 'usertags':
        template = "webclient/data/container_tags.html"
    else:
        template = "webclient/%s/%s.html" % (menu,menu)
    request.session['nav']['menu'] = menu
    
    request.session['nav']['error'] = request.REQUEST.get('error')
    
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
    if url is None:
        url = reverse(viewname="load_template", args=[menu])
    
    #tree support
    init = {'initially_open':[], 'initially_select': None}
    for k,v in string_to_dict(request.REQUEST.get('path')).items():
        if k.lower() in ('project', 'dataset', 'image', 'screen', 'plate'):
            for i in v.split(","):
                if ":selected" in str(i) and init['initially_select'] is None:
                    init['initially_select'] = k+"-"+i.replace(":selected", "")
                else:
                    init['initially_open'].append(k+"-"+i)
                
    try:
        manager = BaseContainer(conn)
    except AttributeError, x:
        logger.error(traceback.format_exc())
        return handlerInternalError(x)
    
    form_users = None
    filter_user_id = None
    
    users = list(conn.listColleagues())
    users.sort(key=lambda x: x.getOmeName().lower())
    empty_label = "*%s (%s)" % (conn.getUser().getFullName(), conn.getUser().omeName)
    if len(users) > 0:
        if request.REQUEST.get('experimenter') is not None and len(request.REQUEST.get('experimenter'))>0: 
            form_users = UsersForm(initial={'users': users, 'empty_label':empty_label, 'menu':menu}, data=request.REQUEST.copy())
            if form_users.is_valid():
                filter_user_id = request.REQUEST.get('experimenter', None)
                request.session.get('nav')['experimenter'] = filter_user_id
                form_users = UsersForm(initial={'user':filter_user_id, 'users': users, 'empty_label':empty_label, 'menu':menu})
        else:
            if request.REQUEST.get('experimenter') == "":
                request.session.get('nav')['experimenter'] = None
            filter_user_id = request.session.get('nav')['experimenter'] is not None and request.session.get('nav')['experimenter'] or None
            if filter_user_id is not None:
                form_users = UsersForm(initial={'user':filter_user_id, 'users': users, 'empty_label':empty_label, 'menu':menu})
            else:
                form_users = UsersForm(initial={'users': users, 'empty_label':empty_label, 'menu':menu})
            
    else:
        form_users = UsersForm(initial={'users': users, 'empty_label':empty_label, 'menu':menu})
            
    form_active_group = ActiveGroupForm(initial={'activeGroup':manager.eContext['context'].groupId, 'mygroups': manager.eContext['allGroups'], 'url':url})
    
    context = {'nav':request.session['nav'], 'url':url, 'init':init, 'eContext':manager.eContext, 'form_active_group':form_active_group, 'form_users':form_users}
    
    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))

@isUserConnected
def load_data(request, o1_type=None, o1_id=None, o2_type=None, o2_id=None, o3_type=None, o3_id=None, **kwargs):
    request.session.modified = True
    
    # check menu
    menu = request.REQUEST.get("menu")
    if menu is not None:
        request.session['nav']['menu'] = menu
    else:
        menu = request.session['nav']['menu']
    
    # check view
    view = request.REQUEST.get("view")
    if view is not None:
        request.session['nav']['view'] = view
    else:
        view = request.session['nav']['view']
    
    # get connection
    conn = None
    try:
        conn = kwargs["conn"]        
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    # get url to redirect
    url = None
    if o1_type is None and o1_id is None:
        args = [line for line in [o1_type, o1_id, o2_type, o2_id, o3_type, o3_id] if line is not None]
        url = reverse(viewname="load_data", args=args)
    else:
        try:
            url = kwargs["url"]
        except:
            logger.error(traceback.format_exc())
        if url is None:
            url = reverse(viewname="load_template", args=[menu])
    
    # get page 
    try:
        page = int(request.REQUEST['page'])
    except:
        page = (1, None)[view=="tree"]
        
    # get index of the plate
    try:
        index = int(request.REQUEST['index'])
    except:
        index = 0
        
    # prepare data
    kw = dict()
    if o1_type is not None:
        if o1_id is not None and o1_id > 0:
            kw[str(o1_type)] = long(o1_id)
        else:
            kw[str(o1_type)] = bool(o1_id)
    
    if o2_type is not None and o2_id > 0:
        kw[str(o2_type)] = long(o2_id)
    if o3_type is not None and o3_id > 0:
        kw[str(o3_type)] = long(o3_id)   
    try:
        manager= BaseContainer(conn, **kw)
    except AttributeError, x:
        logger.error(traceback.format_exc())
        return HttpJavascriptResponse("Object does not exist. Refresh the page.")
        #return handlerInternalError(x)

    # prepare forms
    filter_user_id = request.session.get('nav')['experimenter']
    form_well_index = None
        
    # load data & template
    template = None
    if kw.has_key('orphaned'):
        manager.listOrphanedImages(filter_user_id)
        if view =='icon':
            template = "webclient/data/containers_icon.html"
        elif view =='table':
            template = "webclient/data/containers_table.html"
        else:
            template = "webclient/data/container_subtree.html"
    elif len(kw.keys()) > 0 :
        if kw.has_key('dataset'):
            manager.listImagesInDataset(kw.get('dataset'), filter_user_id, page)
            if view =='icon':
                template = "webclient/data/containers_icon.html"
            elif view =='table':
                template = "webclient/data/containers_table.html"
            else:
                template = "webclient/data/container_subtree.html"
        elif kw.has_key('plate'):
            manager.listPlate(kw.get('plate'), index)
            template = "webclient/data/plate_details.html"
            form_well_index = WellIndexForm(initial={'index':index, 'range':manager.fields})
        elif kw.has_key('screen') and kw.has_key('plate'):
            manager.listPlate(o2_id, index)
            form_well_index = WellIndexForm(initial={'index':index, 'range':manager.fields})
    else:
        manager.listContainerHierarchy(filter_user_id)
        if view =='tree':
            template = "webclient/data/containers_tree.html"
        elif view =='icon':
            template = "webclient/data/containers_icon.html"
        elif view =='table':
            template = "webclient/data/containers_table.html"
        else:
            template = "webclient/data/containers.html"

    context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_well_index':form_well_index, 'index':index}
    
    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))

@isUserConnected
def load_searching(request, form=None, **kwargs):
    request.session.modified = True
    
    # check menu
    menu = request.REQUEST.get("menu")
    if menu is not None:
        request.session['nav']['menu'] = menu
    else:
        menu = request.session['nav']['menu']
    # check view
    view = request.REQUEST.get("view")
    if view is not None:
        request.session['nav']['view'] = view
    else:
        view = request.session['nav']['view']
    
    # get connection
    conn = None
    try:
        conn = kwargs["conn"]        
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    # get url to redirect
    url = None
    try:
        url = kwargs["url"]
    except:
        logger.error(traceback.format_exc())
    if url is None:
        url = reverse(viewname="load_template", args=[menu])

    # get page    
    try:
        page = int(request.REQUEST['page'])
    except:
        page = 1
    
    manager = BaseSearch(conn)
    if form is not None: 
        query_search = request.REQUEST.get('query')
        template = "webclient/search/search_details.html"

        onlyTypes = list()
        if request.REQUEST.get('projects') is not None and request.REQUEST.get('projects') == 'on':
            onlyTypes.append('projects')
        if request.REQUEST.get('datasets') is not None and request.REQUEST.get('datasets') == 'on':
            onlyTypes.append('datasets')
        if request.REQUEST.get('images') is not None and request.REQUEST.get('images') == 'on':
            onlyTypes.append('images')
        if request.REQUEST.get('plates') is not None and request.REQUEST.get('plates') == 'on':
            onlyTypes.append('plates')
        if request.REQUEST.get('screens') is not None and request.REQUEST.get('screens') == 'on':
            onlyTypes.append('screens')
        
        date = request.REQUEST.get('dateperiodinput', None)
        if date is not None:
            date = smart_str(date)
        
        manager.search(query_search, onlyTypes, date)
    else:
        template = "webclient/search/search.html"
    
    batch_query = request.REQUEST.get('batch_query')
    if batch_query is not None:
        delimiter = request.REQUEST.get('delimiter')
    	delimiter = delimiter.decode("string_escape")
        batch_query = batch_query.split("\n")
        batch_query = [query.split(delimiter) for query in batch_query]
        template = "webclient/search/search_details.html"
        manager.batch_search(batch_query)
    
    context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager}
        
    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))

@isUserConnected
def load_data_by_tag(request, o_type=None, o_id=None, **kwargs):
    request.session.modified = True
    
    if request.REQUEST.get("o_type") is not None and len(request.REQUEST.get("o_type")) > 0:
        o_type = request.REQUEST.get("o_type")
        try:
            o_id = long(request.REQUEST.get("o_id"))
        except:
            pass
            
    # check menu
    menu = request.REQUEST.get("menu")
    if menu is not None:
        request.session['nav']['menu'] = menu
    else:
        menu = request.session['nav']['menu']
    
    # check view
    view = request.REQUEST.get("view")
    if view is not None:
        request.session['nav']['view'] = view
    else:
        view = request.session['nav']['view']
    
    # get connection
    conn = None
    try:
        conn = kwargs["conn"]        
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    # get url to redirect
    url = None
    try:
        url = kwargs["url"]
    except:
        logger.error(traceback.format_exc())
    if url is None:
        url = reverse(viewname="load_data_by_tag")
    
    # get page    
    try:
        page = int(request.REQUEST['page'])
    except:
        page = None
    
    # get index of the plate
    try:
        index = int(request.REQUEST['index'])
    except:
        index = 0
    
    # prepare forms
    filter_user_id = request.session.get('nav')['experimenter']
    
    # prepare data
    kw = dict()
    if o_type is not None and o_id > 0:
        kw[str(o_type)] = long(o_id)
    try:
        manager= BaseContainer(conn, **kw)
    except AttributeError, x:
        logger.error(traceback.format_exc())
        return handlerInternalError(x)

    if o_id is not None:
        if o_type == "tag":
            manager.loadDataByTag()
            if view == "tree":
                template = "webclient/data/container_tags_containers.html"
            elif view == "icon":
                template = "webclient/data/containers_icon.html"
            elif view == "table":
                template = "webclient/data/containers_table.html"
            
        elif o_type == "dataset":
            manager.listImagesInDataset(o_id, filter_user_id)
            template = "webclient/data/container_tags_subtree.html"
    else:
        manager.loadTags(filter_user_id)
        template = "webclient/data/container_tags_tree.html"    
    # load data  
    form_well_index = None    
    
    
    context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_well_index':form_well_index}
    
    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))

@isUserConnected
def autocomplete_tags(request, **kwargs):
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    eid = conn.getGroupFromContext().isReadOnly() and conn.getEventContext().userId or None
        
    tags = [{'tag': t.textValue,'id':t.id, 'desc':t.description} for t in conn.listTags(eid)]
    json_data = simplejson.dumps(tags)
    return HttpResponse(json_data, mimetype='application/javascript')

@isUserConnected
def open_astex_viewer(request, fileAnnId, **kwargs):
    conn = None
    try:
        conn = kwargs["conn"]        
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    ann = conn.getObject("Annotation", long(fileAnnId))
    # determine mapType by name
    mapType = "map"
    if ann:
        fileName = ann.getFileName()
        if fileName.endswith(".bit"):
            mapType = "bit"
    
    return render_to_response('webclient/annotations/open_astex_viewer.html', {'fileAnnId': fileAnnId, 'mapType': mapType})
    
    
@isUserConnected
def load_metadata_details(request, c_type, c_id, share_id=None, **kwargs):
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

    url = None
    try:
        url = kwargs["url"]
    except:
        logger.error(traceback.format_exc())

    try:
        index = int(request.REQUEST['index'])
    except:
        index = 0

    form_comment = None
    try:
        if c_type in ("share", "discussion"):
            template = "webclient/annotations/annotations_share.html"
            manager = BaseShare(conn, conn_share, c_id)
            manager.getAllUsers(c_id)
            manager.getComments(c_id)
            form_comment = ShareCommentForm()
        else:
            if conn_share is not None:
                template = "webclient/annotations/annotations_share.html"
                manager = BaseContainer(conn_share, index=index, **{str(c_type): long(c_id)})
            else:
                #template = "webclient/annotations/annotations.html"
                template = "webclient/annotations/metadata_general.html"
                manager = BaseContainer(conn, index=index, **{str(c_type): long(c_id)})
                manager.annotationList()
                form_comment = CommentAnnotationForm()
    except AttributeError, x:
        logger.error(traceback.format_exc())
        return handlerInternalError(x)    

    if c_type in ("tag"):
        context = {'nav':request.session['nav'], 'url':url, 'eContext': manager.eContext, 'manager':manager}
    else:
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_comment':form_comment}

    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))

@isUserConnected
def load_metadata_preview(request, imageId, **kwargs):

    return render_to_response("webclient/annotations/metadata_preview.html", {"imageId": imageId})

@isUserConnected
def load_metadata_hierarchy(request, c_type, c_id, **kwargs):
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

    url = None
    try:
        url = kwargs["url"]
    except:
        logger.error(traceback.format_exc())

    try:
        index = int(request.REQUEST['index'])
    except:
        index = 0

    try:
        if conn_share is not None:
            template = "webclient/annotations/annotations_share.html"                
            manager = BaseContainer(conn_share, index=index, **{str(c_type): long(c_id)})
        else:
            template = "webclient/annotations/metadata_hierarchy.html"
            manager = BaseContainer(conn, index=index, **{str(c_type): long(c_id)})
    except AttributeError, x:
        logger.error(traceback.format_exc())
        return handlerInternalError(x)

    context = {'manager':manager}

    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))

@isUserConnected
def load_metadata_acquisition(request, c_type, c_id, share_id=None, **kwargs):  
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

    url = None
    try:
        url = kwargs["url"]
    except:
        logger.error(traceback.format_exc())

    try:
        index = int(request.REQUEST['index'])
    except:
        index = 0

    try:
        if c_type in ("share", "discussion"):
            template = "webclient/annotations/annotations_share.html"
            manager = BaseShare(conn, conn_share, c_id)
            manager.getAllUsers(c_id)
            manager.getComments(c_id)
        else:
            if conn_share is not None:
                template = "webclient/annotations/annotations_share.html"                
                manager = BaseContainer(conn_share, index=index, **{str(c_type): long(c_id)})
            else:
                template = "webclient/annotations/metadata_acquisition.html"
                manager = BaseContainer(conn, index=index, **{str(c_type): long(c_id)})
    except AttributeError, x:
        logger.error(traceback.format_exc())
        return handlerInternalError(x)

    form_environment = None
    form_objective = None
    form_microscope = None
    form_instrument_objectives = list()
    form_stageLabel = None
    form_filters = list()
    form_dichroics = list()
    form_detectors = list()
    form_channels = list()
    form_lasers = list()

    # various enums we need for the forms (don't load unless needed)
    mediums =  None
    immersions = None
    corrections = None

    if c_type == 'well' or c_type == 'image':
        if conn_share is None:
            manager.originalMetadata()
        manager.channelMetadata()
        for theC, ch in enumerate(manager.channel_metadata):
            logicalChannel = ch.getLogicalChannel()
            if logicalChannel is not None:
                channel = dict()
                channel['form'] = MetadataChannelForm(initial={'logicalChannel': logicalChannel,
                                        'illuminations': list(conn.getEnumerationEntries("IlluminationI")), 
                                        'contrastMethods': list(conn.getEnumerationEntries("ContrastMethodI")), 
                                        'modes': list(conn.getEnumerationEntries("AcquisitionModeI"))})
                lightPath = logicalChannel.getLightPath()
                if lightPath is not None:
                    channel['form_dichroic'] = None
                    channel['form_excitation_filters'] = list()
                    channel['form_emission_filters'] = list()
                    lightPathDichroic = lightPath.getDichroic()
                    if lightPathDichroic is not None:
                        channel['form_dichroic'] = MetadataDichroicForm(initial={'dichroic': lightPathDichroic})
                    filterTypes = list(conn.getEnumerationEntries("FilterTypeI"))
                    for f in lightPath.copyEmissionFilters():
                        channel['form_emission_filters'].append(MetadataFilterForm(initial={'filter': f,'types':filterTypes}))
                    for f in lightPath.copyExcitationFilters():
                        channel['form_excitation_filters'].append(MetadataFilterForm(initial={'filter': f,'types':filterTypes}))
                if logicalChannel.getDetectorSettings()._obj is not None and logicalChannel.getDetectorSettings().getDetector():
                    channel['form_detector_settings'] = MetadataDetectorForm(initial={'detectorSettings':logicalChannel.getDetectorSettings(),
                        'detector': logicalChannel.getDetectorSettings().getDetector(),
                        'types':list(conn.getEnumerationEntries("DetectorTypeI")),
                        'binnings':list(conn.getEnumerationEntries("Binning"))})

                lightSourceSettings = logicalChannel.getLightSourceSettings()
                if lightSourceSettings is not None and lightSourceSettings._obj is not None:
                    if lightSourceSettings.getLightSource() is not None:
                        channel['form_light_source'] = MetadataLightSourceForm(initial={'lightSource': lightSourceSettings.getLightSource(),
                                        'lstypes': list(conn.getEnumerationEntries("LaserType")), 
                                        'mediums': list(conn.getEnumerationEntries("LaserMediumI")),
                                        'pulses': list(conn.getEnumerationEntries("PulseI"))})
                # TODO: We don't display filter sets here yet since they are not populated on Import by BioFormats.
                channel['label'] = ch.getLabel()
                color = ch.getColor()
                channel['color'] = color is not None and color.getHtml() or None
                planeInfo = manager.image and manager.image.getPrimaryPixels().copyPlaneInfo(theC=theC, theZ=0)
                channel['plane_info'] = list(planeInfo)
                form_channels.append(channel)

        try:
            image = manager.well.selectedWellSample().image()
        except:
            image = manager.image

        if image.getObjectiveSettings() is not None:
            # load the enums if needed and create our Objective Form
            if mediums is None: mediums = list(conn.getEnumerationEntries("MediumI"))
            if immersions is None: immersions = list(conn.getEnumerationEntries("ImmersionI"))
            if corrections is None: corrections = list(conn.getEnumerationEntries("CorrectionI"))
            form_objective = MetadataObjectiveSettingsForm(initial={'objectiveSettings': image.getObjectiveSettings(),
                                    'objective': image.getObjectiveSettings().getObjective(),
                                    'mediums': mediums, 'immersions': immersions, 'corrections': corrections })
        if image.getImagingEnvironment() is not None:
            form_environment = MetadataEnvironmentForm(initial={'image': image})
        if image.getStageLabel() is not None:
            form_stageLabel = MetadataStageLabelForm(initial={'image': image })

        instrument = image.getInstrument()
        if instrument is not None:
            if instrument.getMicroscope() is not None:
                form_microscope = MetadataMicroscopeForm(initial={'microscopeTypes':list(conn.getEnumerationEntries("MicroscopeTypeI")), 'microscope': instrument.getMicroscope()})

            objectives = instrument.getObjectives()
            for o in objectives:
                # load the enums if needed and create our Objective Form
                if mediums is None: mediums = list(conn.getEnumerationEntries("MediumI"))
                if immersions is None: immersions = list(conn.getEnumerationEntries("ImmersionI"))
                if corrections is None: corrections = list(conn.getEnumerationEntries("CorrectionI"))
                obj_form = MetadataObjectiveForm(initial={'objective': o,
                                        'mediums': mediums, 'immersions': immersions, 'corrections': corrections })
                form_instrument_objectives.append(obj_form)
            filters = list(instrument.getFilters())
            if len(filters) > 0:
                for f in filters:
                    form_filter = MetadataFilterForm(initial={'filter': f, 'types':list(conn.getEnumerationEntries("FilterTypeI"))})
                    form_filters.append(form_filter)

            dichroics = list(instrument.getDichroics())
            for d in dichroics:
                form_dichroic = MetadataDichroicForm(initial={'dichroic': d})
                form_dichroics.append(form_dichroic)

            detectors = list(instrument.getDetectors())
            if len(detectors) > 0:
                for d in detectors:
                    form_detector = MetadataDetectorForm(initial={'detectorSettings':None, 'detector': d, 'types':list(conn.getEnumerationEntries("DetectorTypeI"))})
                    form_detectors.append(form_detector)

            lasers = list(instrument.getLightSources())
            if len(lasers) > 0:
                for l in lasers:
                    form_laser = MetadataLightSourceForm(initial={'lightSource': l, 
                                    'lstypes':list(conn.getEnumerationEntries("LaserType")),
                                    'mediums': list(conn.getEnumerationEntries("LaserMediumI")),
                                    'pulses': list(conn.getEnumerationEntries("PulseI"))})
                    form_lasers.append(form_laser)

    if c_type in ("share", "discussion", "tag"):
        context = {'nav':request.session['nav'], 'url':url, 'eContext': manager.eContext, 'manager':manager}
    else:
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 
        'form_channels':form_channels, 'form_environment':form_environment, 'form_objective':form_objective, 
        'form_microscope':form_microscope, 'form_instrument_objectives': form_instrument_objectives, 'form_filters':form_filters,
        'form_dichroics':form_dichroics, 'form_detectors':form_detectors, 'form_lasers':form_lasers, 'form_stageLabel':form_stageLabel}

    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))

@isUserConnected
def load_hierarchies(request, o_type=None, o_id=None, **kwargs):
    template = "webclient/hierarchy.html"
    conn = None
    try:
        conn = kwargs["conn"]
        
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    whos = request.session['nav']['whos']
    
    kw = dict()
    if o_type is not None and o_id > 0:
        kw[str(o_type)] = long(o_id)
    
    try:
        manager = BaseContainer(conn, **kw)
    except AttributeError, x:
        logger.error(traceback.format_exc())
        return handlerInternalError(x)
    manager.loadHierarchies()
    
    context = {'nav':request.session['nav'], 'eContext':manager.eContext, 'manager':manager}

    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))
    

###########################################################################
# ACTIONS

@isUserConnected
def manage_annotation_multi(request, action=None, **kwargs):   
    template = "webclient/annotations/annotation_new_form_multi.html"
     
    conn = None
    try:
        conn = kwargs["conn"]
        
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    # check menu
    menu = request.REQUEST.get("menu")
    if menu is not None:
        request.session['nav']['menu'] = menu
    else:
        menu = request.session['nav']['menu']
    
    url = None
    try:
        url = kwargs["url"]
    except:
        logger.error(traceback.format_exc())
    
    try:
        manager = BaseContainer(conn)
    except AttributeError, x:
        logger.error(traceback.format_exc())
        return handlerInternalError(x)

    oids = {'image':request.REQUEST.getlist('image'), 'dataset':request.REQUEST.getlist('dataset'), 'project':request.REQUEST.getlist('project'), 'screen':request.REQUEST.getlist('screen'), 'plate':request.REQUEST.getlist('plate'), 'well':request.REQUEST.getlist('well')}

    images = len(request.REQUEST.getlist('image')) > 0 and list(conn.getObjects("Image", request.REQUEST.getlist('image'))) or list()
    datasets = len(request.REQUEST.getlist('dataset')) > 0 and list(conn.getObjects("Dataset", request.REQUEST.getlist('dataset'))) or list()
    projects = len(request.REQUEST.getlist('project')) > 0 and list(conn.getObjects("Project", request.REQUEST.getlist('project'))) or list()
    screens = len(request.REQUEST.getlist('screen')) > 0 and list(conn.getObjects("Screen", request.REQUEST.getlist('screen'))) or list()
    plates = len(request.REQUEST.getlist('plate')) > 0 and list(conn.getObjects("Plates", request.REQUEST.getlist('plate'))) or list()
    wells = len(request.REQUEST.getlist('well')) > 0 and list(conn.getObjects("Well", request.REQUEST.getlist('well'))) or list()

    count = {'images':len(images), 'datasets':len(datasets), 'projects':len(projects), 'screens':len(screens), 'plates':len(plates), 'wells':len(wells)}
    
    form_multi = None
    if action == "annotatemany":
        selected = {'images':request.REQUEST.getlist('image'), 'datasets':request.REQUEST.getlist('dataset'), 'projects':request.REQUEST.getlist('project'), 'screens':request.REQUEST.getlist('screen'), 'plates':request.REQUEST.getlist('plate'), 'wells':request.REQUEST.getlist('well')}
        form_multi = MultiAnnotationForm(initial={'tags':manager.getTagsByObject(), 'files':manager.getFilesByObject(), 'selected':selected, 'images':images,  'datasets':datasets, 'projects':projects, 'screens':screens, 'plates':plates, 'wells':wells})
    else:
        if request.method == 'POST':
            form_multi = MultiAnnotationForm(initial={'tags':manager.getTagsByObject(), 'files':manager.getFilesByObject(), 'images':images, 'datasets':datasets, 'projects':projects, 'screens':screens, 'plates':plates, 'wells':wells}, data=request.REQUEST.copy(), files=request.FILES)
            if form_multi.is_valid():
                
                content = form_multi.cleaned_data['content']
                if content is not None and content != "":
                    manager.createCommentAnnotations(content, oids)
                
                tag = form_multi.cleaned_data['tag']
                description = form_multi.cleaned_data['description']
                if tag is not None and tag != "":
                    manager.createTagAnnotations(tag, description, oids)
                
                tags = request.REQUEST.getlist('tags')
                if tags is not None and len(tags) > 0:
                    manager.createAnnotationsLinks('tag', tags, oids)
                
                files = request.REQUEST.getlist('files')
                if files is not None and len(files) > 0:
                    manager.createAnnotationsLinks('file', files, oids)
                
                f = request.FILES.get('annotation_file')
                if f is not None:
                    manager.createFileAnnotations(f, oids)
                
                return HttpJavascriptRedirect(reverse(viewname="load_template", args=[menu]))
            
    context = {'url':url, 'nav':request.session['nav'], 'eContext':manager.eContext, 'manager':manager, 'form_multi':form_multi, 'count':count, 'oids':oids}
            
    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))

@isUserConnected
def manage_action_containers(request, action, o_type=None, o_id=None, **kwargs):
    template = None
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    # check menu
    menu = request.REQUEST.get("menu")
    if menu is not None:
        request.session['nav']['menu'] = menu
    else:
        menu = request.session['nav']['menu']
    
    url = None
    try:
        url = kwargs["url"]
    except:
        logger.error(traceback.format_exc())
    
    #manager = None        
    if o_type in ("dataset", "project", "image", "screen", "plate", "well","comment", "file", "tag", "tagset"):
        kw = dict()
        if o_type is not None and o_id > 0:
            kw[str(o_type)] = long(o_id)
        try:
            manager = BaseContainer(conn, **kw)
        except AttributeError, x:
            logger.error(traceback.format_exc())
            return handlerInternalError(x)
    elif o_type in ("share", "sharecomment"):
        manager = BaseShare(conn, None, o_id)
    else:
        manager = BaseContainer(conn)
        
    form = None
    if action == 'new':
        template = "webclient/data/container_new.html"
        form = ContainerForm()
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form':form}
    elif action == 'newcomment':
        template = "webclient/annotations/annotation_new_form.html"
        form_comment = CommentAnnotationForm()
        context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_comment':form_comment}     
    elif action == 'newtagonly':
        template = "webclient/annotations/annotation_new_form.html"
        form_tag = TagAnnotationForm()
        context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_tag':form_tag}
    elif action == 'newtag':
        template = "webclient/annotations/annotation_new_form.html"
        form_tag = TagAnnotationForm()
        form_tags = TagListForm(initial={'tags':manager.getTagsByObject()})
        context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_tag':form_tag, 'form_tags':form_tags}
    elif action == 'newfile':
        template = "webclient/annotations/annotation_new_form.html"
        form_file = UploadFileForm()
        form_files = FileListForm(initial={'files':manager.getFilesByObject()})
        context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_file':form_file, 'form_files':form_files}
    elif action == 'newsharecomment':
        template = "webclient/annotations/annotation_new_form.html"
        if manager.share.isExpired():
            form_sharecomments = None
        else:
            form_sharecomments = ShareCommentForm()
        context = {'nav':request.session['nav'], 'url':url, 'eContext': manager.eContext, 'manager':manager, 'form_sharecomments':form_sharecomments}  
    elif action == 'addnewcontainer':
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers", args=["edit", o_type, o_id]))        
        if o_type is not None and hasattr(manager, o_type) and o_id > 0:        
            form = ContainerForm(data=request.REQUEST.copy())
            if form.is_valid():
                logger.debug("Create new in %s: %s" % (o_type, str(form.cleaned_data)))
                name = form.cleaned_data['name']
                description = form.cleaned_data['description']                
                oid = manager.createDataset(name, description)
                rdict = {'bad':'false', 'id': oid}
                json = simplejson.dumps(rdict, ensure_ascii=False)
                return HttpResponse( json, mimetype='application/javascript')
            else:
                d = dict()
                for e in form.errors.iteritems():
                    d.update({e[0]:unicode(e[1])}) 
                rdict = {'bad':'true','errs': d }
                json = simplejson.dumps(rdict, ensure_ascii=False)
                return HttpResponse( json, mimetype='application/javascript')
        elif request.REQUEST.get('folder_type') in ("project", "screen", "dataset"):
            form = ContainerForm(data=request.REQUEST.copy())
            if form.is_valid():
                logger.debug("Create new: %s" % (str(form.cleaned_data)))
                name = form.cleaned_data['name']                
                description = form.cleaned_data['description']
                oid = getattr(manager, "create"+request.REQUEST.get('folder_type').capitalize())(name, description)
                rdict = {'bad':'false', 'id': oid}
                json = simplejson.dumps(rdict, ensure_ascii=False)
                return HttpResponse( json, mimetype='application/javascript')
            else:
                d = dict()
                for e in form.errors.iteritems():
                    d.update({e[0]:unicode(e[1])}) 
                rdict = {'bad':'true','errs': d }
                json = simplejson.dumps(rdict, ensure_ascii=False)
                return HttpResponse( json, mimetype='application/javascript')
        else:
            return HttpResponseServerError("Object does not exist")
    elif action == 'addnew':
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers", args=["action", "new"]))
        if o_type == "project" and o_id > 0:
            form = ContainerForm(data=request.REQUEST.copy())
            if form.is_valid():
                name = form.cleaned_data['name']
                description = form.cleaned_data['description']
                manager.createDataset(name, description)
                return HttpJavascriptRedirect(reverse(viewname="load_template", args=[menu])) 
            else:
                template = "webclient/data/container_new.html"
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'form':form}
        else:
            if request.REQUEST.get('folder_type') in ("project", "screen", "dataset"):
                form = ContainerForm(data=request.REQUEST.copy())
                if form.is_valid():
                    name = form.cleaned_data['name']
                    description = form.cleaned_data['description']
                    getattr(manager, "create"+request.REQUEST.get('folder_type').capitalize())(name, description)
                    return HttpJavascriptRedirect(reverse(viewname="load_template", args=[menu])) 
                else:
                    template = "webclient/data/container_new.html"
                    context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'form':form}
    elif action == 'edit':
        if o_type == "share" and o_id > 0:
            template = "webclient/public/share_form.html"
            manager.getMembers(o_id)
            manager.getComments(o_id)
            experimenters = list(conn.getExperimenters())
            if manager.share.getExpirationDate() is not None:
                form = ShareForm(initial={'message': manager.share.message, 'expiration': manager.share.getExpirationDate().strftime("%Y-%m-%d"), \
                                        'shareMembers': manager.membersInShare, 'enable': manager.share.active, \
                                        'experimenters': experimenters}) #'guests': share.guestsInShare,
            else:
                form = ShareForm(initial={'message': manager.share.message, 'expiration': "", \
                                        'shareMembers': manager.membersInShare, 'enable': manager.share.active, \
                                        'experimenters': experimenters}) #'guests': share.guestsInShare,
            context = {'url':url, 'nav':request.session['nav'], 'eContext': manager.eContext, 'share':manager, 'form':form}
        elif hasattr(manager, o_type) and o_id > 0:
            obj = getattr(manager, o_type)
            template = "webclient/data/container_form.html"
            form = ContainerForm(initial={'name': obj.name, 'description':obj.description})
            context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form':form}
    elif action == 'save':
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers", args=["edit", o_type, o_id]))        
        if o_type in ("project", "dataset", "image", "screen", "plate", "well"):
            if hasattr(manager, o_type) and o_id > 0:
                form = ContainerForm(data=request.REQUEST.copy())
                if form.is_valid():
                    name = form.cleaned_data['name']
                    description = form.cleaned_data['description']               
                    getattr(manager, "update"+o_type.capitalize())(name, description)
                    return HttpResponseRedirect(url)
                else:
                    template = "webclient/data/container_form.html"
                    context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form}
        elif o_type == 'comment':
            form = CommentAnnotationForm(data=request.REQUEST.copy())
            if form.is_valid():
                content = form.cleaned_data['content']
                manager.saveCommentAnnotation(content)
                return HttpResponseRedirect(url)
            else:
                template = "webclient/data/container_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form}
        elif o_type == 'tag':
            form = TagAnnotationForm(data=request.REQUEST.copy())
            if form.is_valid():
                tag = form.cleaned_data['tag']
                description = form.cleaned_data['description']
                manager.saveTagAnnotation(tag, description)
                return HttpResponseRedirect(url)
            else:
                template = "webclient/data/container_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form}
        elif o_type == "share":
            experimenters = list(conn.getExperimenters())            
            form = ShareForm(initial={'experimenters':experimenters}, data=request.REQUEST.copy())
            if form.is_valid():
                message = form.cleaned_data['message']
                expiration = form.cleaned_data['expiration']
                members = form.cleaned_data['members']
                #guests = request.REQUEST['guests']
                enable = toBoolean(form.cleaned_data['enable'])
                try:
                    host = '%s%s' % (settings.APPLICATION_HOST, reverse("webindex"))
                except:
                    host = '%s://%s:%s%s' % (request.META['wsgi.url_scheme'], request.META['SERVER_NAME'], request.META['SERVER_PORT'], reverse("webindex"))
                manager.updateShareOrDiscussion(host, request.session['server'], message, members, enable, expiration)
                return HttpResponseRedirect(url)
            else:
                template = "webclient/public/share_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'eContext': manager.eContext, 'share':manager, 'form':form}
        elif o_type == "sharecomment":
            form_sharecomments = ShareCommentForm(data=request.REQUEST.copy())
            if form_sharecomments.is_valid():
                comment = form_sharecomments.cleaned_data['comment']
                try:
                    host = '%s%s' % (settings.APPLICATION_HOST, reverse("webindex"))
                except:
                    host = '%s://%s:%s%s' % (request.META['wsgi.url_scheme'], request.META['SERVER_NAME'], request.META['SERVER_PORT'], reverse("webindex"))
                manager.addComment(host, request.session['server'], comment)
                return HttpResponseRedirect(url)
            else:
                template = "webclient/annotations/annotation_new_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'eContext': manager.eContext, 'manager':manager, 'form_sharecomments':form_sharecomments}
    elif action == 'editname':
        if hasattr(manager, o_type) and o_id > 0:
            obj = getattr(manager, o_type)
            template = "webclient/ajax_form/container_form_ajax.html"
            form = ContainerNameForm(initial={'name': (o_type != "tag" and obj.name or obj.textValue)})
            context = {'nav':request.session['nav'], 'manager':manager, 'eContext':manager.eContext, 'form':form}
        else:
            return HttpResponseServerError("Object does not exist")
    elif action == 'savename':
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers", args=["edit", o_type, o_id]))
        if hasattr(manager, o_type) and o_id > 0:
            form = ContainerNameForm(data=request.REQUEST.copy())
            if form.is_valid():
                logger.debug("Update name form:" + str(form.cleaned_data))
                name = form.cleaned_data['name']
                manager.updateName(o_type, o_id, name)                
                rdict = {'bad':'false' }
                json = simplejson.dumps(rdict, ensure_ascii=False)
                return HttpResponse( json, mimetype='application/javascript')
            else:
                d = dict()
                for e in form.errors.iteritems():
                    d.update({e[0]:unicode(e[1])}) 
                rdict = {'bad':'true','errs': d }
                json = simplejson.dumps(rdict, ensure_ascii=False)
                return HttpResponse( json, mimetype='application/javascript')
        else:
            return HttpResponseServerError("Object does not exist")
    elif action == 'editdescription':
        if hasattr(manager, o_type) and o_id > 0:
            obj = getattr(manager, o_type)
            template = "webclient/ajax_form/container_form_ajax.html"
            form = ContainerDescriptionForm(initial={'description': obj.description})
            context = {'nav':request.session['nav'], 'manager':manager, 'eContext':manager.eContext, 'form':form}
        else:
            return HttpResponseServerError("Object does not exist")
    elif action == 'savedescription':
        if not request.method == 'POST':
            return HttpResponseServerError("Action '%s' on the '%s' id:%s cannot be complited" % (action, o_type, o_id))
        if hasattr(manager, o_type) and o_id > 0:
            form = ContainerDescriptionForm(data=request.REQUEST.copy())
            if form.is_valid():
                logger.debug("Update name form:" + str(form.cleaned_data))
                description = form.cleaned_data['description']
                manager.updateDescription(o_type, o_id, description)                
                rdict = {'bad':'false' }
                json = simplejson.dumps(rdict, ensure_ascii=False)
                return HttpResponse( json, mimetype='application/javascript')
            else:
                d = dict()
                for e in form.errors.iteritems():
                    d.update({e[0]:unicode(e[1])}) 
                rdict = {'bad':'true','errs': d }
                json = simplejson.dumps(rdict, ensure_ascii=False)
                return HttpResponse( json, mimetype='application/javascript')
        else:
            return HttpResponseServerError("Object does not exist")
    elif action == 'paste':
        destination = request.REQUEST['destination'].split('-')
        rv = manager.paste(destination)
        if rv:
            rdict = {'bad':'true','errs': rv }
            json = simplejson.dumps(rdict, ensure_ascii=False)
            return HttpResponse( json, mimetype='application/javascript')
        else:
            rdict = {'bad':'false' }
            json = simplejson.dumps(rdict, ensure_ascii=False)
            return HttpResponse( json, mimetype='application/javascript')
    elif action == 'move':
        parent = request.REQUEST['parent'].split('-')
        #source = request.REQUEST['source'].split('-')
        destination = request.REQUEST['destination'].split('-')
        rv = None
        try:
            if parent[1] == destination[1]:
                rv = "Error: Cannot move to the same place."
        except Exception, x:
            rdict = {'bad':'true','errs': str(x) }
        else:
            if rv is None:
                rv = manager.move(parent,destination)
            if rv:
                rdict = {'bad':'true','errs': rv }
            else:
                rdict = {'bad':'false' }
        json = simplejson.dumps(rdict, ensure_ascii=False)
        return HttpResponse( json, mimetype='application/javascript')
    elif action == 'remove':
        parent = request.REQUEST['parent'].split('-')
        #source = request.REQUEST['source'].split('-')
        try:
            manager.remove(parent)            
        except Exception, x:
            logger.error(traceback.format_exc())
            rdict = {'bad':'true','errs': str(x) }
            json = simplejson.dumps(rdict, ensure_ascii=False)
            return HttpResponse( json, mimetype='application/javascript')
        
        if o_type == "dataset" or o_type == "image" or o_type == "plate":
            images = o_type=='image' and [o_id] or None
            datasets = o_type == 'dataset' and [o_id] or None
            plates = o_type == 'plate' and [o_id] or None        
            request.session['clipboard'] = {'images': images, 'datasets': datasets, 'plates': plates}
        rdict = {'bad':'false' }
        json = simplejson.dumps(rdict, ensure_ascii=False)
        return HttpResponse( json, mimetype='application/javascript')
    elif action == 'removefromshare':
        image_id = request.REQUEST['source'].split('-')[1]
        try:
            manager.removeImage(image_id)
        except Exception, x:
            logger.error(traceback.format_exc())
            rv = "Error: %s" % x
            return HttpResponse(rv)
        return HttpResponseRedirect(url)
    elif action == 'addcomment':
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers", args=["newcomment", o_type, oid]))
        form_comment = CommentAnnotationForm(data=request.REQUEST.copy())
        if form_comment.is_valid() and o_type is not None and o_id > 0:
            content = form_comment.cleaned_data['content']
            manager.createCommentAnnotation(o_type, content)    
            return HttpResponseRedirect(url)
        else:
            template = "webclient/annotations/annotation_new_form.html"
            context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_comment':form_comment}
    elif action == 'addtag':
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers", args=["newtag", o_type, oid]))
        form_tag = TagAnnotationForm(data=request.REQUEST.copy())
        if form_tag.is_valid() and o_type is not None and o_id > 0:
            tag = form_tag.cleaned_data['tag']
            desc = form_tag.cleaned_data['description']
            manager.createTagAnnotation(o_type, tag, desc)
            return HttpResponseRedirect(url)
        else:
            template = "webclient/annotations/annotation_new_form.html"
            context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_tag':form_tag}
    elif action == 'addtagonly':
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers", args=["newtagonly"]))
        form_tag = TagAnnotationForm(data=request.REQUEST.copy())
        if form_tag.is_valid():
            tag = form_tag.cleaned_data['tag']
            desc = form_tag.cleaned_data['description']            
            manager.createTagAnnotationOnly(tag, desc)
            return HttpJavascriptRedirect(reverse("load_template", args=["usertags"])) 
        else:
            template = "webclient/annotations/annotation_new_form.html"
            context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_tag':form_tag}
    elif action == 'usetag':
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers", args=["usetag", o_type, oid]))
        tag_list = manager.getTagsByObject()
        form_tags = TagListForm(data=request.REQUEST.copy(), initial={'tags':tag_list})
        if form_tags.is_valid() and o_type is not None and o_id > 0:
            tags = form_tags.cleaned_data['tags']
            manager.createAnnotationLinks(o_type, 'tag', tags)    
            return HttpResponseRedirect(url)
        else:
            template = "webclient/annotations/annotation_new_form.html"
            context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_tags':form_tags}
    elif action == 'addfile':
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers", args=["newfile", o_type, oid]))
        form_file = UploadFileForm(request.REQUEST.copy(), request.FILES)
        if form_file.is_valid() and o_type is not None and o_id > 0:
            f = request.FILES['annotation_file']
            manager.createFileAnnotation(o_type, f)
            return HttpResponseRedirect(url)
        else:
            template = "webclient/annotations/annotation_new_form.html"
            context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_file':form_file}
    elif action == 'usefile':
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers", args=["usefile", o_type, oid]))
        file_list = manager.getFilesByObject()
        form_files = FileListForm(data=request.REQUEST.copy(), initial={'files':file_list})
        if form_files.is_valid() and o_type is not None and o_id > 0:
            files = request.POST.getlist('files')
            manager.createAnnotationLinks(o_type, 'file', files)    
            return HttpResponseRedirect(url)
        else:
            template = "webclient/annotations/annotation_new_form.html"
            context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_files':form_files}
    elif action == 'delete':
        child = toBoolean(request.REQUEST.get('child'))
        anns = toBoolean(request.REQUEST.get('anns'))
        try:
            handle = manager.deleteItem(child, anns)
            request.session['callback'][str(handle)] = {'delmany':False,'did':o_id, 'dtype':o_type, 'dstatus':'in progress', 'derror':handle.errors(), 'dreport':_formatReport(handle)}
            request.session.modified = True            
        except Exception, x:
            logger.error('Failed to delete: %r' % {'did':o_id, 'dtype':o_type}, exc_info=True)
            rdict = {'bad':'true','errs': str(x) }
        else:
            rdict = {'bad':'false' }
        json = simplejson.dumps(rdict, ensure_ascii=False)
        return HttpResponse( json, mimetype='application/javascript')
    elif action == 'deletemany':
        object_ids = {'image':request.REQUEST.getlist('image'), 'dataset':request.REQUEST.getlist('dataset'), 'project':request.REQUEST.getlist('project'), 'screen':request.REQUEST.getlist('screen'), 'plate':request.REQUEST.getlist('plate'), 'well':request.REQUEST.getlist('well')}
        child = toBoolean(request.REQUEST.get('child'))
        anns = toBoolean(request.REQUEST.get('anns'))
        try:
            for key,ids in object_ids.iteritems():
                if ids is not None and len(ids) > 0:
                    handle = manager.deleteObjects(key, ids, child, anns)
                    if len(ids) > 1:
                        request.session['callback'][str(handle)] = {'delmany':len(ids), 'did':ids, 'dtype':key, 'dstatus':'in progress', 'derror':handle.errors(), 'dreport':_formatReport(handle)}
                    else:
                        request.session['callback'][str(handle)] = {'delmany':False, 'did':ids[0], 'dtype':key, 'dstatus':'in progress', 'derror':handle.errors(), 'dreport':_formatReport(handle)}
            request.session.modified = True
        except Exception, x:
            logger.error('Failed to delete: %r' % {'did':ids, 'dtype':key}, exc_info=True)
            rdict = {'bad':'true','errs': str(x) }
        else:
            rdict = {'bad':'false' }
        json = simplejson.dumps(rdict, ensure_ascii=False)
        return HttpResponse( json, mimetype='application/javascript')
    
    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))

@isUserConnected
def download_annotation(request, action, iid, **kwargs):
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    try:
        ann = conn.getObject("Annotation", long(iid))
        
        from django.conf import settings 
        tempdir = settings.FILE_UPLOAD_TEMP_DIR
        temp = os.path.join(tempdir, ('%i-%s.download' % (ann.file.id.val, conn._sessionUuid))).replace('\\','/')
        logger.info("temp path: %s" % str(temp))
        f = open(str(temp),"wb")
        for piece in ann.getFileInChunks():
            f.write(piece)
        f.seek(0)
                
        from django.core.servers.basehttp import FileWrapper
        originalFile_data = FileWrapper(file(temp))
    except Exception, x:
        logger.error(traceback.format_exc())
        return handlerInternalError("Cannot download annotation (id:%s)." % (iid))
    rsp = HttpResponse(originalFile_data)
    if originalFile_data is None:
        return handlerInternalError("Cannot download annotation (id:%s)." % (iid))
    if action == 'download':
        rsp['Content-Type'] = 'application/force-download'
        rsp['Content-Length'] = ann.getFileSize()
        rsp['Content-Disposition'] = 'attachment; filename=%s' % (ann.getFileName().replace(" ","_"))
    return rsp

@isUserConnected
def load_public(request, share_id=None, **kwargs):
    request.session.modified = True
    
    # SUBTREE TODO:
    if share_id is None:
        share_id = request.REQUEST.get("o_id") is not None and long(request.REQUEST.get("o_id")) or None
    
    # check menu
    menu = request.REQUEST.get("menu")
    if menu is not None:
        request.session['nav']['menu'] = menu
    else:
        menu = request.session['nav']['menu']
    # check view
    view = request.REQUEST.get("view")
    if view is not None:
        request.session['nav']['view'] = view
    else:
        view = request.session['nav']['view']

    # get connection
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
        return handlerInternalError("Share connection is not available. Please contact your administrator.")
    
    # get url to redirect
    url = None
    try:
        url = kwargs["url"]
    except:
        logger.error(traceback.format_exc())
    if url is None:
        url = reverse(viewname="load_template", args=[menu])

    # get page    
    try:
        page = int(request.REQUEST['page'])
    except:
        page = 1    
    
    if share_id is not None:
        if view == 'tree':
            template = "webclient/public/share_subtree.html"
        elif view == 'icon':
            template = "webclient/public/share_content_icon.html"
        elif view == 'table':
            template = "webclient/public/share_content_table.html"
        controller = BaseShare(conn, conn_share, share_id)
        if conn_share is None:
            controller.loadShareOwnerContent()
        else:
            controller.loadShareContent()
    else:
        template = "webclient/public/share_tree.html"
        controller = BaseShare(conn)
        controller.getShares()

    context = {'nav':request.session['nav'], 'eContext':controller.eContext, 'share':controller}
    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))

##################################################################
# Basket

@isUserConnected
def basket_action (request, action=None, **kwargs):
    request.session.modified = True
    
    request.session['nav']['menu'] = 'basket'
    
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
    
    if action == "toshare":
        template = "webclient/basket/basket_share_action.html"
        basket = BaseBasket(conn)
        basket.load_basket(request)
        experimenters = sortByAttr(list(conn.getExperimenters()), 'lastName')
        selected = [long(i) for i in request.REQUEST.getlist('image')]        
        form = BasketShareForm(initial={'experimenters':experimenters, 'images':basket.imageInBasket, 'enable':True, 'selected':selected})            
        context = {'nav':request.session['nav'], 'eContext':basket.eContext, 'form':form}
    elif action == "createshare":
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers", args=["edit", o_type, oid]))
        
        basket = BaseBasket(conn)
        basket.load_basket(request)
        experimenters = sortByAttr(list(conn.getExperimenters()), 'lastName')
        form = BasketShareForm(initial={'experimenters':experimenters, 'images':basket.imageInBasket}, data=request.REQUEST.copy())
        if form.is_valid():
            images = form.cleaned_data['image']
            message = form.cleaned_data['message']
            expiration = form.cleaned_data['expiration']
            members = form.cleaned_data['members']
            #guests = request.REQUEST['guests']
            enable = toBoolean(form.cleaned_data['enable'])
            try:
                host = '%s%s' % (settings.APPLICATION_HOST, reverse("webindex"))
            except:
                host = '%s://%s:%s%s' % (request.META['wsgi.url_scheme'], request.META['SERVER_NAME'], request.META['SERVER_PORT'], reverse("webindex"))
            share = BaseShare(conn)
            share.createShare(host, request.session.get('server'), images, message, members, enable, expiration)
            return HttpJavascriptRedirect(reverse("load_template", args=["public"])) 
        else:
            template = "webclient/basket/basket_share_action.html"
            context = {'nav':request.session['nav'], 'eContext':basket.eContext, 'form':form}
    elif action == "todiscuss":
        template = "webclient/basket/basket_discussion_action.html"
        basket = BaseBasket(conn)
        experimenters = sortByAttr(list(conn.getExperimenters()), 'lastName')
        form = ShareForm(initial={'experimenters':experimenters, 'enable':True})            
        context = {'nav':request.session['nav'], 'eContext':basket.eContext, 'form':form}
    elif action == "createdisc":
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers", args=["edit", o_type, oid]))
        
        basket = BaseBasket(conn)
        experimenters = sortByAttr(list(conn.getExperimenters()), 'lastName')
        form = ShareForm(initial={'experimenters':experimenters}, data=request.REQUEST.copy())
        if form.is_valid():
            message = form.cleaned_data['message']
            expiration = form.cleaned_data['expiration']
            members = form.cleaned_data['members']
            #guests = request.REQUEST['guests']
            enable = toBoolean(form.cleaned_data['enable'])
            try:
                host = '%s%s' % (settings.APPLICATION_HOST, reverse("webindex"))
            except:
                host = '%s://%s:%s%s' % (request.META['wsgi.url_scheme'], request.META['SERVER_NAME'], request.META['SERVER_PORT'], reverse("webindex"))
            share = BaseShare(conn)
            share.createDiscussion(host, request.session.get('server'), message, members, enable, expiration)
            return HttpJavascriptRedirect(reverse("load_template", args=["public"])) 
        else:
            template = "webclient/basket/basket_discussion_action.html"
            context = {'nav':request.session['nav'], 'eContext':basket.eContext, 'form':form}
    else:
        template = "webclient/basket/basket.html"
        
        basket = BaseBasket(conn)
        basket.load_basket(request)
        
        form_active_group = ActiveGroupForm(initial={'activeGroup':basket.eContext['context'].groupId, 'mygroups': basket.eContext['allGroups'], 'url':url})
        
        context = {'nav':request.session['nav'], 'eContext':basket.eContext, 'basket':basket, 'form_active_group':form_active_group }

    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
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
    request.session['imageInBasket'] = set()
    #request.session['datasetInBasket'] = list()
    return HttpResponseRedirect(reverse("basket_action"))

@isUserConnected
def update_basket(request, **kwargs):
    action = None
    if request.method == 'POST':
        request.session.modified = True        
        try:
            action = request.REQUEST['action']
        except Exception, x:
            logger.error(traceback.format_exc())
            return handlerInternalError("Attribute error: 'action' is missed.")
        else:
            prod = request.REQUEST.get('productId')
            ptype = request.REQUEST.get('productType')
            if action == 'add':
                images = request.REQUEST.getlist('image')
                #datasets = request.REQUEST.getlist('datasets')
                for i in images:
                    flag = False
                    for item in request.session['imageInBasket']:
                        if item == long(i):
                            flag = True
                            break
                    if not flag:
                        request.session['imageInBasket'].add(long(i))
                #for i in datasets:
                #    flag = False
                #    for item in request.session['datasetInBasket']:
                #        if item == long(i):
                #            flag = True
                #            break
                #    if not flag:
                 #       request.session['datasetInBasket'].append(long(i))
            elif action == 'del':
                if ptype == 'image':
                    try:
                        request.session['imageInBasket'].remove(long(prod))
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
            elif action == 'delmany':
                images = [long(i) for i in request.REQUEST.getlist('image')]
                for i in images:
                    if i in request.session['imageInBasket']:
                        request.session['imageInBasket'].remove(long(i))
                    else:
                        rv = "Error: could not remove image from the basket."
                        return HttpResponse(rv)                

        total = len(request.session['imageInBasket'])#+len(request.session['datasetInBasket'])
        request.session['nav']['basket'] = total
        return HttpResponse(total)
    else:
        return handlerInternalError("Request method error in Basket.")

@isUserConnected
def manage_myaccount(request, action=None, **kwargs):
    template = "webclient/person/myaccount.html"
    request.session.modified = True
    
    request.session['nav']['menu'] = 'person'
    
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
        
    controller = BaseExperimenter(conn)
    controller.getMyDetails()
    controller.getOwnedGroups()
    
    eContext = dict()
    eContext['context'] = conn.getEventContext()
    eContext['user'] = conn.getUser()
    eContext['allGroups']  = controller.sortByAttr(list(conn.getGroupsMemberOf()), "name")
    
    form = MyAccountForm(initial={'omename': controller.experimenter.omeName, 'first_name':controller.experimenter.firstName,
                                'middle_name':controller.experimenter.middleName, 'last_name':controller.experimenter.lastName,
                                'email':controller.experimenter.email, 'institution':controller.experimenter.institution,
                                'default_group':controller.defaultGroup, 'groups':controller.otherGroups})
    
    if action == "save":
        form = MyAccountForm(data=request.POST.copy(), initial={'groups':controller.otherGroups})
        if form.is_valid():
            firstName = form.cleaned_data['first_name']
            middleName = form.cleaned_data['middle_name']
            lastName = form.cleaned_data['last_name']
            email = form.cleaned_data['email']
            institution = form.cleaned_data['institution']
            defaultGroup = form.cleaned_data['default_group']
            controller.updateMyAccount(firstName, lastName, email, defaultGroup, middleName, institution)
            return HttpResponseRedirect(reverse("myaccount"))

    form_active_group = ActiveGroupForm(initial={'activeGroup':eContext['context'].groupId, 'mygroups': eContext['allGroups'], 'url':url})
    
    context = {'nav':request.session['nav'], 'eContext': eContext, 'controller':controller, 'form':form, 'ldapAuth': controller.ldapAuth, 'form_active_group':form_active_group}
    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))

@isUserConnected
def change_password(request, **kwargs):
    template = "webclient/person/password.html"
    request.session.modified = True
    
    request.session['nav']['menu'] = 'person'
    
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
    
    error = None
    if request.method != 'POST':
        password_form = ChangePassword()
    else:
        password_form = ChangePassword(data=request.POST.copy())
                    
        if password_form.is_valid():
            old_password = password_form.cleaned_data['old_password']
            password = password_form.cleaned_data['password']
            try:
                conn.changeMyPassword(password, old_password) 
            except Exception, x:
                error = x.message
            else:
                request.session['password'] = password
                return HttpJavascriptResponse("Password was changed successfully")                
                
    context = {'nav':request.session['nav'], 'password_form':password_form, 'error':error}
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isUserConnected
def upload_myphoto(request, action=None, **kwargs):
    template = "webclient/person/upload_myphoto.html"
    request.session.modified = True
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    photo_size = conn.getExperimenterPhotoSize()
    form_file = UploadPhotoForm()

    request.session['nav']['edit_mode'] = False    
    if action == "upload":
        if request.method == 'POST':
            form_file = UploadPhotoForm(request.POST, request.FILES)
            if form_file.is_valid():
                controller = BaseUploadFile(conn)
                controller.attach_photo(request.FILES['photo'])
                return HttpResponseRedirect(reverse("upload_myphoto"))
    elif action == "crop": 
        x1 = long(request.REQUEST.get('x1'))
        x2 = long(request.REQUEST.get('x2'))
        y1 = long(request.REQUEST.get('y1'))
        y2 = long(request.REQUEST.get('y2'))
        box = (x1,y1,x2,y2)
        conn.cropExperimenterPhoto(box)
        return HttpResponseRedirect(reverse("upload_myphoto"))
    elif action == "editphoto":
        if photo_size is not None:
            request.session['nav']['edit_mode'] = True

    context = {'nav':request.session['nav'], 'form_file':form_file, 'photo_size':photo_size}
    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))

@isUserConnected
def help(request, **kwargs):
    template = "webclient/help.html"
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
    
    controller = BaseHelp(conn)
    
    form_active_group = ActiveGroupForm(initial={'activeGroup':controller.eContext['context'].groupId, 'mygroups': controller.eContext['allGroups'], 'url':url})
    
    context = {'nav':request.session['nav'], 'eContext': controller.eContext, 'controller':controller, 'form_active_group':form_active_group}
    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))

@isUserConnected
def load_calendar(request, year=None, month=None, **kwargs):
    template = "webclient/history/calendar.html"
    
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
    
    context = {'nav':request.session['nav'], 'eContext': controller.eContext, 'controller':controller}
    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))

@isUserConnected
def load_history(request, year, month, day, **kwargs):
    
    template = "webclient/history/history_details.html"
    
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
    
    filter_user_id = request.session.get('nav')['experimenter']
    controller = BaseCalendar(conn=conn, year=year, month=month, day=day, eid=filter_user_id)
    controller.get_items(cal_type, page)
    
    #if cal_type is None:
    #    form_history_type = HistoryTypeForm()
    #else:
    #    form_history_type = HistoryTypeForm(initial={'data_type':cal_type})
    
    context = {'nav':request.session['nav'], 'url':url, 'eContext': controller.eContext, 'controller':controller}#, 'form_history_type':form_history_type}
    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))

########################################
# Progressbar

@isUserConnected
def progress(request, **kwargs):
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    in_progress = 0
    failure = 0
    _purgeCallback(request)
    
    for cbString in request.session.get('callback').keys():
        dstatus = request.session['callback'][cbString]['dstatus']
        if dstatus == "failed":
            failure+=1
        elif dstatus != "failed" or dstatus != "finished":
            try:
                handle = omero.api.delete.DeleteHandlePrx.checkedCast(conn.c.ic.stringToProxy(cbString))
                cb = omero.callbacks.DeleteCallbackI(conn.c, handle)
                if cb.block(500) is None: # ms.
                    err = handle.errors()
                    request.session['callback'][cbString]['derror'] = err
                    if err > 0:
                        logger.error("Status job '%s'error:" % cbString)
                        logger.error(err)
                        request.session['callback'][cbString]['dstatus'] = "failed"
                        request.session['callback'][cbString]['dreport'] = _formatReport(handle)
                        failure+=1
                    else:
                        request.session['callback'][cbString]['dstatus'] = "in progress"
                        request.session['callback'][cbString]['dreport'] = _formatReport(handle)
                        in_progress+=1
                else:
                    err = handle.errors()
                    request.session['callback'][cbString]['derror'] = err
                    if err > 0:
                        request.session['callback'][cbString]['dstatus'] = "failed"
                        request.session['callback'][cbString]['dreport'] = _formatReport(handle)
                        failure+=1
                    else:
                        request.session['callback'][cbString]['dstatus'] = "finished"
                        request.session['callback'][cbString]['dreport'] = _formatReport(handle)
                        cb.close()
            except Ice.ObjectNotExistException:
                request.session['callback'][cbString]['derror'] = 0
                request.session['callback'][cbString]['dstatus'] = "finished"
                request.session['callback'][cbString]['dreport'] = None
            except Exception, x:
                logger.error(traceback.format_exc())
                logger.error("Status job '%s'error:" % cbString)
                request.session['callback'][cbString]['derror'] = 1
                request.session['callback'][cbString]['dstatus'] = "failed"
                request.session['callback'][cbString]['dreport'] = str(x)
                failure+=1
            request.session.modified = True        
        
    rv = {'inprogress':in_progress, 'failure':failure, 'jobs':len(request.session['callback'])}
    return HttpResponse(simplejson.dumps(rv),mimetype='application/json')

@isUserConnected
def status_action (request, action=None, **kwargs):
    request.session.modified = True
    
    request.session['nav']['menu'] = 'status'
    
    if action == "clean":
        request.session['callback'] = dict()
        return HttpResponseRedirect(reverse("status"))
        
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    template = "webclient/status/status.html"

    _purgeCallback(request)
            
    controller = BaseController(conn)    
    form_active_group = ActiveGroupForm(initial={'activeGroup':controller.eContext['context'].groupId, 'mygroups': controller.eContext['allGroups']})
        
    context = {'nav':request.session['nav'], 'eContext':controller.eContext, 'sizeOfJobs':len(request.session['callback']), 'jobs':request.session['callback'], 'form_active_group':form_active_group }

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
    img = conn.getObject("Image", iid)
    
    if img is None:
        jpeg_data = conn.defaultThumbnail(80)
        logger.error("Image %s not found..." % (str(iid)))
        #return handlerInternalError("Image %s not found..." % (str(iid)))
    else:
        jpeg_data = img.getThumbnailOrDefault(size=80)
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
    img = conn.getObject("Image", iid)
    
    if img is None:
        jpeg_data = conn.defaultThumbnail(size=int(size))
        logger.error("Image %s not found..." % (str(iid)))
        #return handlerInternalError("Image %s not found..." % (str(iid)))
    else:
        jpeg_data = img.getThumbnailOrDefault(size=int(size))
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
def render_image_region (request, iid, z, t, server_id=None, share_id=None, _conn=None, **kwargs):
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

    return webgateway_views.render_image_region(request, iid, z, t, server_id=None, _conn=conn, **kwargs)

@isUserConnected
def image_viewer (request, iid, share_id=None, **kwargs):
    """ This view is responsible for showing pixel data as images """
    
    conn = None
    if share_id is not None:
        kwargs['viewport_server'] = '/webclient/%s' % share_id
        try:
            conn = kwargs["conn_share"]
        except:
            logger.error(traceback.format_exc())
            return handlerInternalError("Connection is not available. Please contact your administrator.")
    else:
        kwargs['viewport_server'] = '/webclient'
        try:
            conn = kwargs["conn"]
        except:
            logger.error(traceback.format_exc())
            return handlerInternalError("Connection is not available. Please contact your administrator.")
         
    if conn is None:
        raise Exception("Share connection not available")

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
    img = conn.getObject("Image", iid)

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
    img = conn.getObject("Image", iid)

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
    img = conn.getObject("Image", iid)

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
        return HttpJavascriptResponse(r_text)

