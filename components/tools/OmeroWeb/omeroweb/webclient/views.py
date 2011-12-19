#!/usr/bin/env python
# 
# 
# 
# Copyright (c) 2008-2011 University of Dundee.
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
import copy
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

import shutil
import zipfile
import glob

from time import time
from thread import start_new_thread

from omero_version import omero_version
import omero, omero.scripts 
from omero.rtypes import *

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
from django.core.servers.basehttp import FileWrapper

from webclient.webclient_gateway import OmeroWebGateway
from omeroweb.webclient.webclient_utils import string_to_dict

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

from controller import BaseController
from controller.index import BaseIndex
from controller.basket import BaseBasket
from controller.container import BaseContainer
from controller.help import BaseHelp
from controller.history import BaseCalendar
from controller.impexp import BaseImpexp
from controller.search import BaseSearch
from controller.share import BaseShare

from omeroweb.webadmin.custom_models import Server

from omeroweb.webadmin.forms import LoginForm
from omeroweb.webadmin.webadmin_utils import _checkVersion, _isServerOn, toBoolean, upgradeCheck

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
            url = request.get_full_path()
        
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
            _session_logout(request, request.REQUEST.get('server', None))
            if server is not None:
                return HttpLoginRedirect(reverse("weblogin")+(("?url=%s&server=%s") % (url,server)))
            return HttpLoginRedirect(reverse("weblogin")+(("?url=%s") % url))
        
        conn_share = None
        share_id = kwargs.get('share_id', None)
        if share_id is not None:
            sh = conn.getShare(share_id)
            if sh is not None:
                try:
                    if sh.getOwner().id != conn.getEventContext().userId:
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
    """
    Makes sure that various dictionaries exist under request.session, so that we don't have to check each time.
    they are used. These are:
    - 'callback'    Used to hold job handles and extra data for delete and scripts. Added in manage_action_containers and script_run,
                    Accessed in progress and status views.
    - 'shares'      Reset in login and change_active_group, added to in get_share_connection. Not accessed Anyware??
    - 'imageInBasket'   Reset in login, change_active_group and empty_basket. Changed in update_basket. Used in controller/basket.py load_basket().
    - 'nav'         This is a collection of parameters required for rendering various templates. Passed to the template context
                    using context = {'nav':request.session['nav']... etc.
    
    """
    changes = False
    if request.session.get('callback') is None:
        request.session['callback'] = dict()
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
            blitz = Server.get(pk=request.session.get('server'))
        elif request.session.get('host') is not None:
            blitz = Server.get(host=request.session.get('host'))
        blitz = "%s:%s" % (blitz.host, blitz.port)
        request.session['nav']={"blitz": blitz, "menu": "mydata", "view": "tree", "basket": 0, "experimenter":None}
        changes = True
    if changes:
        request.session.modified = True
        
################################################################################
# views controll

def login(request):
    """
    Webclient Login - Also can be used by other Apps to log in to OMERO. 
    Uses the 'server' id from request to lookup the server-id (index), host and port from settings. E.g. "localhost", 4064.
    Stores these details, along with username, password etc in the request.session.
    Resets other data parameters in the request.session.
    Tries to get connection to OMERO and if this works, then we are redirected to the 'index' page or url specified in REQUEST.
    If we can't connect, the login page is returned with appropriate error messages.
    """
    request.session.modified = True
    if request.REQUEST.get('server'):
        blitz = Server.get(pk=request.REQUEST.get('server'))
        request.session['server'] = blitz.id
        request.session['host'] = blitz.host
        request.session['port'] = blitz.port
        request.session['username'] = smart_str(request.REQUEST.get('username',None))
        request.session['password'] = smart_str(request.REQUEST.get('password',None))
        request.session['ssl'] = (True, False)[request.REQUEST.get('ssl') is None]
        request.session['shares'] = dict()
        request.session['imageInBasket'] = set()
        blitz_host = "%s:%s" % (blitz.host, blitz.port)
        request.session['nav']={"error": None, "blitz": blitz_host, "menu": "start", "view": "icon", "basket": 0, "experimenter":None, 'callback':dict()}
        
    error = request.REQUEST.get('error')
    
    conn = None
    # TODO: version check should be done on the low level, see #5983
    if _checkVersion(request.session.get('host'), request.session.get('port')):
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
            blitz = Server.get(request.session.get('server'))
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
    """
    The webclient home page. 
    Viewing this page doesn't perform any action. All we do here is assemble various data for display, including form for changing current group.
    Last imports, tag cloud etc are retrived via separate AJAX calls.
    """
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
    """ NOT USED? TODO: remove this, url and template """

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
    """
    Gets the most recent imports - Used in an AJAX call by home page.
    """
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
    """ Gets the most recent 'shares' and 'share' comments. Used by the homepage via AJAX call """

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
    """ Gets the most used Tags. Used by the homepage via AJAX call """

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
    """
    Changes the active group of the OMERO connection, using conn.changeActiveGroup() with 'active_group' from request.REQUEST.
    First we log out and log in again, to force closing of any processes?
    TODO: This requires usage of request.session.get('password'), which should be avoided.
    Finally this redirects to the 'url'.
    """
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
    
    blitz = Server.get(pk=server) 
    request.session['server'] = blitz.id
    request.session['host'] = blitz.host
    request.session['port'] = blitz.port
    request.session['username'] = username
    request.session['password'] = password
    request.session['ssl'] = (True, False)[request.REQUEST.get('ssl') is None]
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

def _session_logout (request, server_id):
    """ 
    Delegates to the webgateway _session_logout, while also using this to log out any share sessions.
    Used internally by logout and 
    """

    webgateway_views._session_logout(request, server_id)
     
    try:
        if request.session.get('shares') is not None:
            for key in request.session.get('shares').iterkeys():
                session_key = "S:%s#%s#%s" % (request.session.session_key,server_id, key)
                webgateway_views._session_logout(request,server_id, force_key=session_key)
        for k in request.session.keys():
            if request.session.has_key(k):
                del request.session[k]      
    except:
        logger.error(traceback.format_exc())
    
@isUserConnected
def logout(request, **kwargs):
    """ Logout of the session and redirects to the homepage (will redirect to login first) """
    _session_logout(request, request.session.get('server'))
    #request.session.set_expiry(1)
    return HttpResponseRedirect(reverse("webindex"))


###########################################################################
@isUserConnected
def load_template(request, menu, **kwargs):
    """
    This view handles most of the top-level pages, as specified by 'menu' E.g. userdata, usertags, history, search etc.
    Query string 'path' that specifies an object to display in the data tree is parsed.
    We also prepare the list of users in the current group, for the switch-user form. Change-group form is also prepared.
    """
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
    # E.g. path=project=51|dataset=502|image=607:selected
    for k,v in string_to_dict(request.REQUEST.get('path')).items():
        if k.lower() in ('project', 'dataset', 'image', 'screen', 'plate'):
            for i in v.split(","):
                if ":selected" in str(i) and init['initially_select'] is None:
                    init['initially_select'] = k+"-"+i.replace(":selected", "")     # E.g. image-607
                else:
                    init['initially_open'].append(k+"-"+i)          # E.g. ['project-51', 'dataset-502']
    
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
    """
    This loads data for the tree, via AJAX calls. 
    The template is specified by query string. E.g. icon, table, tree.
    By default this loads Projects and Datasets.
    E.g. /load_data?view=tree provides data for the tree as <li>.
    """
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
    
    # get url to redirect. Not sure what this is used for?
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
        #page = (1, None)[view=="tree"]
        page = 1
        
    # get index of the plate
    try:
        index = int(request.REQUEST['index'])
    except:
        index = 0
        
    # prepare data. E.g. kw = {}  or  {'dataset': 301L}  or  {'project': 151L, 'dataset': 301L}
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
        # we set up the manager with the datatypes & ids we require. Manager loads data below
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
        manager.listOrphanedImages(filter_user_id, page)
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
            fields = manager.plate.getNumberOfFields(kw.get('acquisition', None))
            if fields is not None:
                form_well_index = WellIndexForm(initial={'index':index, 'range':fields})
                if index == 0:
                    index = fields[0]
            template = "webclient/data/plate.html"
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
    """
    Handles AJAX calls to search 
    """
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
    # form = 'form' if we are searching. Get query from request...
    if form is not None: 
        query_search = request.REQUEST.get('query').replace("+", " ")
        print "".replace("+", " ")
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
        
        # search is carried out and results are stored in manager.containers.images etc.
        manager.search(query_search, onlyTypes, date)
    else:
        # simply display the search home page.
        template = "webclient/search/search.html"
    
    # batch query for searching wells in plates
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
    """ 
    Loads data for the tag tree and center panel.
    Either get the P/D/I etc under tags, or the images etc under a tagged Dataset or Project.
    @param o_type       'tag' or 'project', 'dataset'.
    """

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
    """ Autocomplete for tag. Not used now? TODO: remove this? """

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
def open_astex_viewer(request, obj_type, obj_id, **kwargs):
    """
    Opens the Open Astex Viewer applet, to display volume masks in a couple of formats:
    - mrc.map files that are attached to images. obj_type = 'file'
    - Convert OMERO image to mrc on the fly. obj_type = 'image_8bit' or 'image'
        In this case, we may use 'scipy' to scale the image volume. 
    """
    conn = None
    try:
        conn = kwargs["conn"]        
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    # can only populate these for 'image'
    image = None
    data_storage_mode = None
    pixelRange = None       # (min, max) values of the raw data
    # If we convert to 8bit map, subtract dataOffset, multiply by mapPixelFactor add mapOffset. (used for js contour controls)
    if obj_type == 'file':
        ann = conn.getObject("Annotation", obj_id)
        if ann is None:
            return handlerInternalError("Can't find file Annotation ID %s as data source for Open Astex Viewer." % obj_id)
        # determine mapType by name
        imageName = ann.getFileName()
        if imageName.endswith(".bit"):
            data_url = reverse("open_astex_bit", args=[obj_id])
        else:
            data_url = reverse("open_astex_map", args=[obj_id])

    elif obj_type in ('image', 'image_8bit'):
        image = conn.getObject("Image", obj_id)     # just check the image exists
        if image is None:
            return handlerInternalError("Can't find image ID %s as data source for Open Astex Viewer." % obj_id)
        imageName = image.getName()
        c = image.getChannels()[0]
        # By default, scale to 120 ^3. Also give option to load 'bigger' map or full sized
        DEFAULTMAPSIZE = 120
        BIGGERMAPSIZE = 160
        targetSize = DEFAULTMAPSIZE * DEFAULTMAPSIZE * DEFAULTMAPSIZE
        biggerSize = BIGGERMAPSIZE * BIGGERMAPSIZE * BIGGERMAPSIZE
        imgSize = image.getSizeX() * image.getSizeY() * image.getSizeZ()
        sizeOptions = None  # only give user choice if we need to scale down (and we CAN scale with scipy)
        if imgSize > targetSize:
            try:
                import scipy.ndimage
                sizeOptions = {}
                factor = float(targetSize)/ imgSize
                f = pow(factor,1.0/3)
                sizeOptions["small"] = {'x':image.getSizeX() * f, 'y':image.getSizeY() * f, 'z':image.getSizeZ() * f, 'size':DEFAULTMAPSIZE}
                if imgSize > biggerSize:
                    factor2 = float(biggerSize)/ imgSize
                    f2 = pow(factor2,1.0/3)
                    sizeOptions["medium"] = {'x':image.getSizeX() * f2, 'y':image.getSizeY() * f2, 'z':image.getSizeZ() * f2, 'size':BIGGERMAPSIZE}
                else:
                    sizeOptions["full"] = {'x':image.getSizeX(), 'y':image.getSizeY(), 'z':image.getSizeZ()}
            except ImportError:
                DEFAULTMAPSIZE = 0  # don't try to resize the map (see image_as_map)
                pass
        pixelRange = (c.getWindowMin(), c.getWindowMax())
        contourSliderInit = (pixelRange[0] + pixelRange[1])/2   # best guess as starting position for contour slider

        def calcPrecision(range):
            dec=0
            if (range == 0):    dec = 0
            elif (range < 0.0000001): dec = 10
            elif (range < 0.000001): dec = 9
            elif (range < 0.00001): dec = 8
            elif (range < 0.0001): dec = 7
            elif (range < 0.001): dec = 6
            elif (range < 0.01): dec = 5
            elif (range < 0.1): dec = 4
            elif (range < 1.0): dec = 3
            elif (range < 10.0): dec = 2
            elif (range < 100.0): dec = 1
            return dec
        dec = calcPrecision(pixelRange[1]-pixelRange[0])
        contourSliderIncr = "%.*f" % (dec,abs((pixelRange[1]-pixelRange[0])/128.0))

        if obj_type == 'image_8bit':
            data_storage_mode = 1
            data_url = reverse("webclient_image_as_map_8bit", args=[obj_id, DEFAULTMAPSIZE])
        else:
            if image.getPrimaryPixels().getPixelsType.value == 'float':
                data_storage_mode = 2
            else:
                data_storage_mode = 1   # E.g. uint16 image will get served as 8bit map
            data_url = reverse("webclient_image_as_map", args=[obj_id, DEFAULTMAPSIZE])

    return render_to_response('webclient/annotations/open_astex_viewer.html', {'data_url': data_url, "image": image,
        "sizeOptions":sizeOptions, "contourSliderInit":contourSliderInit, "contourSliderIncr":contourSliderIncr,
        "data_storage_mode": data_storage_mode,'pixelRange':pixelRange}, context_instance=Context(request))


@isUserConnected
def load_metadata_details(request, c_type, c_id, share_id=None, **kwargs):
    """
    This page is the right-hand panel 'general metadata', first tab only.
    Shown for Projects, Datasets, Images, Screens, Plates, Wells, Tags etc.
    The data and annotations are loaded by the manager. Display of appropriate data is handled by the template.
    """
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
        # the index of a field within a well
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
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_comment':form_comment, 'index':index}

    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))

@isUserConnected
def load_metadata_preview(request, imageId, share_id=None, **kwargs):
    """
    This is the image 'Preview' tab for the right-hand panel. 
    Currently this doesn't do much except launch the view-port plugin using the image Id (and share Id if necessary)
    """
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
        # the index of a field within a well
        index = int(request.REQUEST['index'])
    except:
        index = 0

    try:
        template = "webclient/annotations/metadata_preview.html"
        if conn_share is not None:
            manager = BaseContainer(conn_share, index=index, image=long(imageId))
        else:
            manager = BaseContainer(conn, index=index, image=long(imageId))
    except AttributeError, x:
        logger.error(traceback.format_exc())
        return handlerInternalError(x)
    
    context = {'imageId':imageId, 'manager':manager}

    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))

@isUserConnected
def load_metadata_hierarchy(request, c_type, c_id, **kwargs):
    """
    This loads the ancestors of the specified object and displays them in a static tree.
    Used by an AJAX call from the metadata_general panel.
    """

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
        template = "webclient/annotations/metadata_hierarchy.html"
        if conn_share is not None:
            manager = BaseContainer(conn_share, index=index, **{str(c_type): long(c_id)})
        else:
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
    """
    The acquisition tab of the right-hand panel. Only loaded for images.
    TODO: urls regex should make sure that c_type is only 'image' OR 'well'
    """
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
                template = "webclient/annotations/metadata_acquisition.html"                
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
            image = manager.well.getWellSample().image()
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

    # TODO: remove this 'if' since we should only have c_type = 'image'?
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
    

###########################################################################
# ACTIONS

@isUserConnected
def manage_annotation_multi(request, action=None, **kwargs):   
    """
    Handles setup OR sumbission of batch annotation form.
    """
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
        index = int(request.REQUEST['index'])
    except:
        index = None
    
    try:
        manager = BaseContainer(conn, index=index)
    except AttributeError, x:
        logger.error(traceback.format_exc())
        return handlerInternalError(x)

    oids = {'image':request.REQUEST.getlist('image'), 'dataset':request.REQUEST.getlist('dataset'), 'project':request.REQUEST.getlist('project'), 'screen':request.REQUEST.getlist('screen'), 'plate':request.REQUEST.getlist('plate'), 'well':request.REQUEST.getlist('well')}

    images = len(request.REQUEST.getlist('image')) > 0 and list(conn.getObjects("Image", request.REQUEST.getlist('image'))) or list()
    datasets = len(request.REQUEST.getlist('dataset')) > 0 and list(conn.getObjects("Dataset", request.REQUEST.getlist('dataset'))) or list()
    projects = len(request.REQUEST.getlist('project')) > 0 and list(conn.getObjects("Project", request.REQUEST.getlist('project'))) or list()
    screens = len(request.REQUEST.getlist('screen')) > 0 and list(conn.getObjects("Screen", request.REQUEST.getlist('screen'))) or list()
    plates = len(request.REQUEST.getlist('plate')) > 0 and list(conn.getObjects("Plate", request.REQUEST.getlist('plate'))) or list()
    acquisitions = len(request.REQUEST.getlist('acquisition')) > 0 and list(conn.getObjects("PlateAcquisition", request.REQUEST.getlist('acquisition'))) or list()
    wells = list()
    if len(request.REQUEST.getlist('well')) > 0:
        for w in conn.getObjects("Well", request.REQUEST.getlist('well')):
            w.index=index
            wells.append(w)
    
    oids = {'image':images, 'dataset':datasets, 'project':projects, 'screen':screens, 'plate':plates, 'acquisitions':acquisitions, 'well':wells}
    
    count = {'images':len(images), 'datasets':len(datasets), 'projects':len(projects), 'screens':len(screens), 'plates':len(plates), 'wells':len(wells)}
    
    form_multi = None
    if action == "annotatemany":
        # we simply set up the annotation form, passing the objects to be annotated.
        selected = {'images':request.REQUEST.getlist('image'), 'datasets':request.REQUEST.getlist('dataset'), 'projects':request.REQUEST.getlist('project'), 'screens':request.REQUEST.getlist('screen'), 'plates':request.REQUEST.getlist('plate'), 'acquisitions':request.REQUEST.getlist('acquisition'), 'wells':request.REQUEST.getlist('well')}
        form_multi = MultiAnnotationForm(initial={'tags':manager.getTagsByObject(), 'files':manager.getFilesByObject(), 'selected':selected, 'images':images,  'datasets':datasets, 'projects':projects, 'screens':screens, 'plates':plates, 'acquisitions':acquisitions, 'wells':wells})
    else:
        # Handle form submission...
        if request.method == 'POST':
            form_multi = MultiAnnotationForm(initial={'tags':manager.getTagsByObject(), 'files':manager.getFilesByObject(), 'images':images, 'datasets':datasets, 'projects':projects, 'screens':screens, 'plates':plates, 'acquisitions':acquisitions, 'wells':wells}, data=request.REQUEST.copy(), files=request.FILES)
            if form_multi.is_valid():
                
                # In each case below, we pass the {'object_type': [ids]} map
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
            
    context = {'url':url, 'nav':request.session['nav'], 'eContext':manager.eContext, 'manager':manager, 'form_multi':form_multi, 'count':count, 'oids':oids, 'index':index}
            
    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))

@isUserConnected
def manage_action_containers(request, action, o_type=None, o_id=None, **kwargs):
    """
    Handles many different actions on various objects.
    
    @param action:      "new", "newcomment", "newtagonly", "newtag", "newfile", "newsharecomment",  (these prepare forms for annotation etc.)
                            "addnewcontainer", "addnew", (create new P/D, Screen etc.)
                            "edit", "save",
                            "editname", "savename", "editdescription", "savedescription",  (used as GET and POST for in-line editing)
                            "paste", "move", "remove", "removefromshare", 
                            "addcomment", "addtag", "addtagonly", "addfile", "usefile", "delete", "deletemany"
    @param o_type:      "dataset", "project", "image", "screen", "plate", "acquisition", "well","comment", "file", "tag", "tagset","share", "sharecomment"
    """
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
    
    # Url is often used to redirect after performing action.
    url = None
    try:
        url = kwargs["url"]
    except:
        logger.error(traceback.format_exc())
    
    try:
        index = int(request.REQUEST['index'])
    except:
        index = None
    
    manager = None
    if o_type in ("dataset", "project", "image", "screen", "plate", "acquisition", "well","comment", "file", "tag", "tagset"):
        if o_type == 'tagset': o_type = 'tag' # TODO: this should be handled by the BaseContainer
        kw = {'index':index}
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
        # form to create new Project, Dataset or Screen. - TODO: not used now?
        template = "webclient/data/container_new.html"
        form = ContainerForm()
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form':form}
    elif action == 'newcomment':
        # form for Comment - TODO: not used now?
        template = "webclient/annotations/annotation_new_form.html"
        form_comment = CommentAnnotationForm()
        context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_comment':form_comment, 'index':index}
    elif action == 'newtagonly':
        # form only allows creating new tag. TODO: not used now?
        template = "webclient/annotations/annotation_new_form.html"
        form_tag = TagAnnotationForm()
        context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_tag':form_tag}
    elif action == 'newtag':
        # form allows creating new tag AND adding existing tags
        template = "webclient/annotations/annotation_new_form.html"
        form_tag = TagAnnotationForm()
        form_tags = TagListForm(initial={'tags':manager.getTagsByObject()})
        context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_tag':form_tag, 'form_tags':form_tags, 'index':index}
    elif action == 'newfile':
        # form for attaching new file (upload) or existing one.
        template = "webclient/annotations/annotation_new_form.html"
        form_file = UploadFileForm()
        form_files = FileListForm(initial={'files':manager.getFilesByObject()})
        context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_file':form_file, 'form_files':form_files, 'index':index}
    elif action == 'newsharecomment':
        # TODO: not used now?
        template = "webclient/annotations/annotation_new_form.html"
        if manager.share.isExpired():
            form_sharecomments = None
        else:
            form_sharecomments = ShareCommentForm()
        context = {'nav':request.session['nav'], 'url':url, 'eContext': manager.eContext, 'manager':manager, 'form_sharecomments':form_sharecomments}  
    elif action == 'addnewcontainer':
        # Used within the jsTree to add a new Project, Dataset etc under a specified parent OR top-level
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers", args=["edit", o_type, o_id]))
        if o_type is not None and hasattr(manager, o_type) and o_id > 0: 
            # E.g. Parent o_type is 'project'...
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
            # No parent specified. We can create orphaned 'project', 'dataset' etc.
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
        # Handles submission from 'container_new.html'. TODO: not used now?
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers", args=["action", "new"]))
        if o_type == "project" and o_id > 0:
            form = ContainerForm(data=request.REQUEST.copy())
            if form.is_valid():
                logger.debug("Create new dataset: %s" % (str(form.cleaned_data)))
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
                    logger.debug("Create new folder: %s" % (str(form.cleaned_data)))
                    name = form.cleaned_data['name']
                    description = form.cleaned_data['description']
                    getattr(manager, "create"+request.REQUEST.get('folder_type').capitalize())(name, description)
                    return HttpJavascriptRedirect(reverse(viewname="load_template", args=[menu])) 
                else:
                    template = "webclient/data/container_new.html"
                    context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'form':form}
    elif action == 'edit':
        # form for editing an Object. E.g. Project etc. TODO: not used now? 
        if o_type == "share" and o_id > 0:
            template = "webclient/public/share_form.html"
            manager.getMembers(o_id)
            manager.getComments(o_id)
            experimenters = list(conn.getExperimenters())
            experimenters.sort(key=lambda x: x.getOmeName().lower())
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
        # Handles submission of the 'edit' form above. TODO: not used now?
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers", args=["edit", o_type, o_id]))        
        if o_type in ("project", "dataset", "image", "screen", "plate", "well"):
            if hasattr(manager, o_type) and o_id > 0:
                form = ContainerForm(data=request.REQUEST.copy())
                if form.is_valid():
                    logger.debug("Update: %s" % (str(form.cleaned_data)))
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
                logger.debug("Save Comment: %s" % (str(form.cleaned_data)))
                content = form.cleaned_data['content']
                manager.saveCommentAnnotation(content)
                return HttpResponseRedirect(url)
            else:
                template = "webclient/data/container_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form, 'index':index}
        elif o_type == 'tag':
            form = TagAnnotationForm(data=request.REQUEST.copy())
            if form.is_valid():
                logger.debug("Save Tag: %s" % (str(form.cleaned_data)))
                tag = form.cleaned_data['tag']
                description = form.cleaned_data['description']
                manager.saveTagAnnotation(tag, description)
                return HttpResponseRedirect(url)
            else:
                template = "webclient/data/container_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form, 'index':index}
        elif o_type == "share":
            experimenters = list(conn.getExperimenters())
            experimenters.sort(key=lambda x: x.getOmeName().lower())
            form = ShareForm(initial={'experimenters':experimenters}, data=request.REQUEST.copy())
            if form.is_valid():
                logger.debug("Update share: %s" % (str(form.cleaned_data)))
                message = form.cleaned_data['message']
                expiration = form.cleaned_data['expiration']
                members = form.cleaned_data['members']
                #guests = request.REQUEST['guests']
                enable = toBoolean(form.cleaned_data['enable'])
                host = request.build_absolute_uri(reverse("load_template", args=["public"]))
                manager.updateShareOrDiscussion(host, request.session.get('server'), message, members, enable, expiration)
                return HttpResponseRedirect(url)
            else:
                template = "webclient/public/share_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'eContext': manager.eContext, 'share':manager, 'form':form}
        elif o_type == "sharecomment":
            form_sharecomments = ShareCommentForm(data=request.REQUEST.copy())
            if form_sharecomments.is_valid():
                logger.debug("Create share comment: %s" % (str(form_sharecomments.cleaned_data)))
                comment = form_sharecomments.cleaned_data['comment']
                host = request.build_absolute_uri(reverse("load_template", args=["public"]))
                manager.addComment(host, request.session['server'], comment)
                return HttpResponseRedirect(url)
            else:
                template = "webclient/annotations/annotation_new_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'eContext': manager.eContext, 'manager':manager, 'form_sharecomments':form_sharecomments}
    elif action == 'editname':
        # start editing 'name' in-line
        if hasattr(manager, o_type) and o_id > 0:
            obj = getattr(manager, o_type)
            template = "webclient/ajax_form/container_form_ajax.html"
            form = ContainerNameForm(initial={'name': ((o_type != ("tag")) and obj.getName() or obj.textValue)})
            context = {'nav':request.session['nav'], 'manager':manager, 'eContext':manager.eContext, 'form':form}
        else:
            return HttpResponseServerError("Object does not exist")
    elif action == 'savename':
        # Save name edit in-line
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
        # start editing description in-line
        if hasattr(manager, o_type) and o_id > 0:
            obj = getattr(manager, o_type)
            template = "webclient/ajax_form/container_form_ajax.html"
            form = ContainerDescriptionForm(initial={'description': obj.description})
            context = {'nav':request.session['nav'], 'manager':manager, 'eContext':manager.eContext, 'form':form}
        else:
            return HttpResponseServerError("Object does not exist")
    elif action == 'savedescription':
        # Save editing of description in-line
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
        # Handles 'paste' action from the jsTree. Destination in POST
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
        # Handles drag-and-drop moving of objects in jsTree. 
        # Also handles 'remove' of Datasets (moves to 'Experimenter' parent)
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
        # Handles 'remove' of Images from jsTree, removal of comment, tag from Object etc.
        parent = request.REQUEST['parent'].split('-')
        try:
            manager.remove(parent)            
        except Exception, x:
            logger.error(traceback.format_exc())
            rdict = {'bad':'true','errs': str(x) }
            json = simplejson.dumps(rdict, ensure_ascii=False)
            return HttpResponse( json, mimetype='application/javascript')
        
        rdict = {'bad':'false' }
        json = simplejson.dumps(rdict, ensure_ascii=False)
        return HttpResponse( json, mimetype='application/javascript')
    elif action == 'removefromshare':
        image_id = request.REQUEST.get('source')
        try:
            manager.removeImage(image_id)
        except Exception, x:
            logger.error(traceback.format_exc())
            rdict = {'bad':'true','errs': str(x) }
            json = simplejson.dumps(rdict, ensure_ascii=False)
            return HttpResponse( json, mimetype='application/javascript')
        rdict = {'bad':'false' }
        json = simplejson.dumps(rdict, ensure_ascii=False)
        return HttpResponse( json, mimetype='application/javascript')
    elif action == 'addcomment':
        # Handles Adding of comment to Project etc. in the 'metadata_general' page
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("load_metadata_details", args=[o_type, o_id]))
        form_comment = CommentAnnotationForm(data=request.REQUEST.copy())
        if form_comment.is_valid() and o_type is not None and o_id > 0:
            content = form_comment.cleaned_data['content']
            manager.createCommentAnnotation(o_type, content)    
            return HttpResponseRedirect(url)
        else:
            template = "webclient/annotations/annotation_new_form.html"
            context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_comment':form_comment, 'index':index}
    elif action == 'addtag':
        # Handles creation of a new tag from the 'newtag' form above
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers", args=["newtag", o_type, o_id]))
        form_tag = TagAnnotationForm(data=request.REQUEST.copy())
        if form_tag.is_valid() and o_type is not None and o_id > 0:
            tag = form_tag.cleaned_data['tag']
            desc = form_tag.cleaned_data['description']
            manager.createTagAnnotation(o_type, tag, desc)
            return HttpResponseRedirect(url)
        else:
            template = "webclient/annotations/annotation_new_form.html"
            context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_tag':form_tag, 'index':index}
    elif action == 'addtagonly':
        # Creates a new tag. Submitted from the 'newtagonly' form above. TODO: not used now?
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
        # Adds an existing tag to object. Submitted from the 'newtag' form above.
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers", args=["usetag", o_type, o_id]))
        tag_list = manager.getTagsByObject()
        form_tags = TagListForm(data=request.REQUEST.copy(), initial={'tags':tag_list})
        if form_tags.is_valid() and o_type is not None and o_id > 0:
            tags = form_tags.cleaned_data['tags']
            manager.createAnnotationLinks(o_type, 'tag', tags)    
            return HttpResponseRedirect(url)
        else:
            template = "webclient/annotations/annotation_new_form.html"
            context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_tags':form_tags, 'index':index}
    elif action == 'addfile':
        # Uploads a new file, to attach as file annotation
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers", args=["newfile", o_type, o_id]))
        form_file = UploadFileForm(request.REQUEST.copy(), request.FILES)
        if form_file.is_valid() and o_type is not None and o_id > 0:
            f = request.FILES['annotation_file']
            manager.createFileAnnotation(o_type, f)
            return HttpResponseRedirect(url)
        else:
            template = "webclient/annotations/annotation_new_form.html"
            context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_file':form_file, 'index':index}
    elif action == 'usefile':
        # Attatches an existing file.
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers", args=["usefile", o_type, o_id]))
        file_list = manager.getFilesByObject()
        form_files = FileListForm(data=request.REQUEST.copy(), initial={'files':file_list})
        if form_files.is_valid() and o_type is not None and o_id > 0:
            files = request.POST.getlist('files')
            manager.createAnnotationLinks(o_type, 'file', files)    
            return HttpResponseRedirect(url)
        else:
            template = "webclient/annotations/annotation_new_form.html"
            context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_files':form_files, 'index':index}
    elif action == 'delete':
        # Handles delete of a file attached to object.
        child = toBoolean(request.REQUEST.get('child'))
        anns = toBoolean(request.REQUEST.get('anns'))
        try:
            handle = manager.deleteItem(child, anns)
            request.session['callback'][str(handle)] = {'job_type': 'delete', 'delmany':False,'did':o_id, 'dtype':o_type, 'status':'in progress',
                'derror':handle.errors(), 'dreport':_formatReport(handle), 'start_time': datetime.datetime.now()}
            request.session.modified = True
        except Exception, x:
            logger.error('Failed to delete: %r' % {'did':o_id, 'dtype':o_type}, exc_info=True)
            rdict = {'bad':'true','errs': str(x) }
        else:
            rdict = {'bad':'false' }
        json = simplejson.dumps(rdict, ensure_ascii=False)
        return HttpResponse( json, mimetype='application/javascript')
    elif action == 'deletemany':
        # Handles multi-delete from jsTree.
        object_ids = {'image':request.REQUEST.getlist('image'), 'dataset':request.REQUEST.getlist('dataset'), 'project':request.REQUEST.getlist('project'), 'screen':request.REQUEST.getlist('screen'), 'plate':request.REQUEST.getlist('plate'), 'well':request.REQUEST.getlist('well')}
        child = toBoolean(request.REQUEST.get('child'))
        anns = toBoolean(request.REQUEST.get('anns'))
        logger.debug("Delete many: child? %s anns? %s object_ids %s" % (child, anns, object_ids))
        try:
            for key,ids in object_ids.iteritems():
                if ids is not None and len(ids) > 0:
                    handle = manager.deleteObjects(key, ids, child, anns)
                    dMap = {'job_type': 'delete', 'start_time': datetime.datetime.now(),'status':'in progress', 'derrors':handle.errors(),
                        'dreport':_formatReport(handle), 'dtype':key}
                    if len(ids) > 1:
                        dMap['delmany'] = len(ids)
                        dMap['did'] = ids
                    else:
                        dMap['delmany'] = False
                        dMap['did'] = ids[0]
                    request.session['callback'][str(handle)] = dMap
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
def get_original_file(request, fileId, **kwargs):
    """ Returns the specified original file as an http response. Used for displaying text or png/jpeg etc files in browser """

    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")

    orig_file = conn.getObject("OriginalFile", fileId)
    if orig_file is None:
        return handlerInternalError("Original File does not exists (id:%s)." % (iid))
    
    rsp = HttpResponse(orig_file.getFileInChunks())
    mimetype = orig_file.mimetype
    if mimetype == "text/x-python": 
        mimetype = "text/plain" # allows display in browser
    rsp['Content-Type'] =  mimetype
    rsp['Content-Length'] = orig_file.getSize()
    #rsp['Content-Disposition'] = 'attachment; filename=%s' % (orig_file.name.replace(" ","_"))
    return rsp


@isUserConnected
def image_as_map(request, imageId, **kwargs):
    """ Converts OMERO image into mrc.map file (using tiltpicker utils) and returns the file """

    from omero_ext.tiltpicker.pyami import mrc
    from numpy import dstack, zeros, int8

    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")

    image = conn.getObject("Image", imageId)
    if image is None:
        message = "Image ID %s not found in image_as_map" % imageId
        logger.error(message)
        return handlerInternalError(message)

    imageName = image.getName()
    downloadName = imageName.endswith(".map") and imageName or "%s.map" % imageName
    pixels = image.getPrimaryPixels()

    # get a list of numpy planes and make stack
    zctList = [(z,0,0) for z in range(image.getSizeZ())]
    npList = list(pixels.getPlanes(zctList))
    npStack = dstack(npList)
    logger.info("Numpy stack for image_as_map: dtype: %s, range %s-%s" % (npStack.dtype.name, npStack.min(), npStack.max()) )

    # OAV only supports 'float' and 'int8'. Convert anything else to int8
    if pixels.getPixelsType().value != 'float' or ('8bit' in kwargs and kwargs['8bit']):
        #scale from -127 -> 128 and conver to 8 bit integer
        npStack = npStack - npStack.min()  # start at 0
        npStack = (npStack * 255.0 / npStack.max()) - 127 # range - 127 -> 128
        a = zeros(npStack.shape, dtype=int8)
        npStack = npStack.round(out=a)

    if "maxSize" in kwargs and int(kwargs["maxSize"]) > 0:
        sz = int(kwargs["maxSize"])
        targetSize = sz * sz * sz
        # if available, use scipy.ndimage to resize
        if npStack.size > targetSize:
            try:
                import scipy.ndimage
                from numpy import round
                factor = float(targetSize)/ npStack.size
                factor = pow(factor,1.0/3)
                logger.info("Resizing numpy stack %s by factor of %s" % (npStack.shape, factor))
                npStack = round(scipy.ndimage.interpolation.zoom(npStack, factor), 1)
            except ImportError:
                logger.info("Failed to import scipy.ndimage for interpolation of 'image_as_map'. Full size: %s" % str(npStack.shape))
                pass

    header = {}
    header["xlen"] = pixels.physicalSizeX * image.getSizeX()
    header["ylen"] = pixels.physicalSizeY * image.getSizeY()
    header["zlen"] = pixels.physicalSizeZ * image.getSizeZ()
    if header["xlen"] == 0 or header["ylen"] == 0 or header["zlen"] == 0:
        header = {}

    # write mrc.map to temp file
    import tempfile
    temp = tempfile.NamedTemporaryFile(suffix='.map')
    try:
        mrc.write(npStack, temp.name, header)
        logger.debug("download file: %r" % {'name':temp.name, 'size':temp.tell()})
        originalFile_data = FileWrapper(temp)
        rsp = HttpResponse(originalFile_data)
        rsp['Content-Type'] = 'application/force-download'
        #rsp['Content-Length'] = temp.tell()
        rsp['Content-Length'] =os.path.getsize(temp.name)
        rsp['Content-Disposition'] = 'attachment; filename=%s' % downloadName
        temp.seek(0)
    except Exception, x:
        temp.close()
        logger.error(traceback.format_exc())
        return handlerInternalError("Cannot generate map (id:%s)." % (imageId))
    return rsp


@isUserConnected
def archived_files(request, iid, **kwargs):
    """
    Downloads the archived file(s) as a single file or as a zip (if more than one file)
    """
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")

    image = conn.getObject("Image", iid)
    if image is None:
        logger.debug("Cannot download archived file becuase Image does not exist.")
        return handlerInternalError("Cannot download archived file becuase Image does not exist (id:%s)." % (iid))
    
    files = list(image.getArchivedFiles())

    if len(files) == 0:
        logger.debug("Tried downloading archived files from image with no files archived.")
        return handlerInternalError("This image has no Archived Files.")

    if len(files) == 1:
        orig_file = files[0]
        rsp = HttpResponse(orig_file.getFileInChunks())
        rsp['Content-Length'] = orig_file.getSize()
        rsp['Content-Disposition'] = 'attachment; filename=%s' % (orig_file.getName().replace(" ","_"))
    else:
        import tempfile
        temp = tempfile.NamedTemporaryFile(suffix='.archive')
        try:
            temp_zip_dir = tempfile.mkdtemp()
            logger.debug("download dir: %s" % temp_zip_dir)
            try:
                for a in files:
                    temp_f = os.path.join(temp_zip_dir, a.name)
                    f = open(str(temp_f),"wb")
                    try:
                        for chunk in a.getFileInChunks():
                            f.write(chunk)
                    finally:
                        f.close()

                # create zip
                zip_file = zipfile.ZipFile(temp, 'w', zipfile.ZIP_DEFLATED)
                try:
                    a_files = os.path.join(temp_zip_dir, "*")
                    for name in glob.glob(a_files):
                        zip_file.write(name, os.path.basename(name))
                finally:
                    zip_file.close()
                    # delete temp dir
            finally:
                shutil.rmtree(temp_zip_dir, ignore_errors=True)
            
            file_name = "%s.zip" % image.getName().replace(" ","_")

            # return the zip or single file
            archivedFile_data = FileWrapper(temp)
            rsp = HttpResponse(archivedFile_data)
            rsp['Content-Length'] = temp.tell()
            rsp['Content-Disposition'] = 'attachment; filename=%s' % file_name
            temp.seek(0)
        except Exception, x:
            temp.close()
            logger.error(traceback.format_exc())
            return handlerInternalError("Cannot download file (id:%s)." % (iid))

    rsp['Content-Type'] = 'application/force-download'
    return rsp

@isUserConnected
def download_annotation(request, action, iid, **kwargs):
    """ Returns the file annotation as an http response for download """

    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    ann = conn.getObject("Annotation", iid)
    if ann is None:
        return handlerInternalError("Annotation does not exist (id:%s)." % (iid))
    
    rsp = HttpResponse(ann.getFileInChunks())
    rsp['Content-Type'] = 'application/force-download'
    rsp['Content-Length'] = ann.getFileSize()
    rsp['Content-Disposition'] = 'attachment; filename=%s' % (ann.getFileName().replace(" ","_"))
    return rsp

@isUserConnected
def load_public(request, share_id=None, **kwargs):
    """ Loads data for the tree in the 'public' main page. """

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
    """
    Various actions for creating a 'share' or 'discussion' (no images).
    
    @param action:      'toshare', 'createshare'    (form to create share and handling the action itself)
                        'todiscuss', 'createdisc'    (form to create discussion and handling the action itself)
    """
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
        experimenters = list(conn.getExperimenters())
        experimenters.sort(key=lambda x: x.getOmeName().lower())
        selected = [long(i) for i in request.REQUEST.getlist('image')]        
        form = BasketShareForm(initial={'experimenters':experimenters, 'images':basket.imageInBasket, 'enable':True, 'selected':selected})            
        context = {'nav':request.session['nav'], 'eContext':basket.eContext, 'form':form}
    elif action == "createshare":
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("basket_action"))
        basket = BaseBasket(conn)
        basket.load_basket(request)
        experimenters = list(conn.getExperimenters())
        experimenters.sort(key=lambda x: x.getOmeName().lower())
        form = BasketShareForm(initial={'experimenters':experimenters, 'images':basket.imageInBasket}, data=request.REQUEST.copy())
        if form.is_valid():
            images = form.cleaned_data['image']
            message = form.cleaned_data['message']
            expiration = form.cleaned_data['expiration']
            members = form.cleaned_data['members']
            #guests = request.REQUEST['guests']
            enable = toBoolean(form.cleaned_data['enable'])
            host = request.build_absolute_uri(reverse("load_template", args=["public"]))
            share = BaseShare(conn)
            share.createShare(host, request.session.get('server'), images, message, members, enable, expiration)
            return HttpJavascriptRedirect(reverse("load_template", args=["public"])) 
        else:
            template = "webclient/basket/basket_share_action.html"
            context = {'nav':request.session['nav'], 'eContext':basket.eContext, 'form':form}
    elif action == "todiscuss":
        template = "webclient/basket/basket_discussion_action.html"
        basket = BaseBasket(conn)
        experimenters = list(conn.getExperimenters())
        experimenters.sort(key=lambda x: x.getOmeName().lower())
        form = ShareForm(initial={'experimenters':experimenters, 'enable':True})            
        context = {'nav':request.session['nav'], 'eContext':basket.eContext, 'form':form}
    elif action == "createdisc":
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("basket_action"))
        basket = BaseBasket(conn)
        experimenters = list(conn.getExperimenters())
        experimenters.sort(key=lambda x: x.getOmeName().lower())
        form = ShareForm(initial={'experimenters':experimenters}, data=request.REQUEST.copy())
        if form.is_valid():
            message = form.cleaned_data['message']
            expiration = form.cleaned_data['expiration']
            members = form.cleaned_data['members']
            #guests = request.REQUEST['guests']
            enable = toBoolean(form.cleaned_data['enable'])
            host = request.build_absolute_uri(reverse("load_template", args=["public"]))
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
    """ Empty the basket of images """

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
    """ Add or remove images to the set in the basket """

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
def help(request, **kwargs):
    """ Displays help page. Includes the choosers for changing current group and current user. """

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
    """ 
    Loads the calendar which is displayed in the left panel of the history page. 
    Shows current month by default. Filter by experimenter 
    """

    template = "webclient/history/calendar.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
   
    filter_user_id = request.session.get('nav')['experimenter']
   
    if year is not None and month is not None:
        controller = BaseCalendar(conn=conn, year=year, month=month, eid=filter_user_id)
    else:
        today = datetime.datetime.today()
        controller = BaseCalendar(conn=conn, year=today.year, month=today.month, eid=filter_user_id)
    controller.create_calendar()
    
    context = {'nav':request.session['nav'], 'eContext': controller.eContext, 'controller':controller}
    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))

@isUserConnected
def load_history(request, year, month, day, **kwargs):
    """ The data for a particular date that is loaded into the center panel """

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


def getObjectUrl(conn, obj):
    """
    This provides a url to browse to the specified omero.model.ObjectI P/D/I, S/P, FileAnnotation etc.
    used to display results from the scripting service
    E.g webclient/userdata/?path=project=1|dataset=5|image=12601:selected
    If the object is a file annotation, try to browse to the parent P/D/I
    """
    base_url = reverse(viewname="load_template", args=['userdata'])

    # if we have a File Annotation, then we want our URL to be for the parent object...
    if isinstance(obj, omero.model.FileAnnotationI):
        fa = conn.getObject("Annotation", obj.id.val)
        for ptype in ['project', 'dataset', 'image']:
            links = fa.getParentLinks(ptype)
            for l in links:
                obj = l.parent

    if isinstance(obj, omero.model.ImageI):
        # return path from first Project we find, or None if no Projects
        image = conn.getObject("Image", obj.id.val)
        for d in image.listParents():
            for p in d.listParents():
                return "%s?path=project=%d|dataset=%d|image=%d:selected" % (base_url, p.id, d.id, image.id)
        return None

    if isinstance(obj, omero.model.DatasetI):
        dataset = conn.getObject("Dataset", obj.id.val)
        for p in dataset.listParents():
            return "%s?path=project=%d|dataset=%d:selected" % (base_url, p.id, dataset.id)
        return None

    if isinstance(obj, omero.model.ProjectI):
        return "%s?path=project=%d:selected" % (base_url, obj.id.val)

    if isinstance(obj, omero.model.PlateI):
        plate = conn.getObject("Plate", obj.id.val)
        screen = plate.getParent()
        if screen is not None:
            return "%s?path=screen=%d|plate=%d:selected" % (base_url, screen.id, plate.id)
        return "%s?path=plate=%d:selected" % (base_url, obj.id.val)

    if isinstance(obj, omero.model.ScreenI):
        return "%s?path=screen=%d:selected" % (base_url, obj.id.val)


######################
# Activities window & Progressbar
@isUserConnected
def progress(request, **kwargs):
    """
    Refresh callbacks (delete, scripts etc) and provide json to update Activities window & Progressbar.
    The returned json contains details for ALL callbacks in web session, regardless of their status.
    We also add counts of jobs, failures and 'in progress' to update status bar.
    """
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")

    in_progress = 0
    failure = 0
    _purgeCallback(request)

    rv = {}
    # test each callback for failure, errors, completion, results etc
    for cbString in request.session.get('callback').keys():
        job_type = request.session['callback'][cbString]['job_type']

        status = request.session['callback'][cbString]['status']
        if status == "failed":
            failure+=1

        # update delete
        if job_type == 'delete':
            if status not in ("failed", "finished"):
                try:
                    handle = omero.api.delete.DeleteHandlePrx.checkedCast(conn.c.ic.stringToProxy(cbString))
                    cb = omero.callbacks.DeleteCallbackI(conn.c, handle)
                    if cb.block(0) is None: # ms #500
                        err = handle.errors()
                        request.session['callback'][cbString]['derror'] = err
                        if err > 0:
                            logger.error("Status job '%s'error:" % cbString)
                            logger.error(err)
                            request.session['callback'][cbString]['status'] = "failed"
                            request.session['callback'][cbString]['dreport'] = _formatReport(handle)
                            failure+=1
                        else:
                            request.session['callback'][cbString]['status'] = "in progress"
                            request.session['callback'][cbString]['dreport'] = _formatReport(handle)
                            in_progress+=1
                    else:
                        err = handle.errors()
                        request.session['callback'][cbString]['derror'] = err
                        if err > 0:
                            request.session['callback'][cbString]['status'] = "failed"
                            request.session['callback'][cbString]['dreport'] = _formatReport(handle)
                            failure+=1
                        else:
                            request.session['callback'][cbString]['status'] = "finished"
                            request.session['callback'][cbString]['dreport'] = _formatReport(handle)
                            cb.close()
                except Ice.ObjectNotExistException:
                    request.session['callback'][cbString]['derror'] = 0
                    request.session['callback'][cbString]['status'] = "finished"
                    request.session['callback'][cbString]['dreport'] = None
                except Exception, x:
                    logger.error(traceback.format_exc())
                    logger.error("Status job '%s'error:" % cbString)
                    request.session['callback'][cbString]['derror'] = 1
                    request.session['callback'][cbString]['status'] = "failed"
                    request.session['callback'][cbString]['dreport'] = str(x)
                    failure+=1
                request.session.modified = True
            # make a copy of the map in session, so that we can replace non json-compatible objects, without modifying session
            rv[cbString] = copy.copy(request.session['callback'][cbString])
            rv[cbString]['start_time'] = str(request.session['callback'][cbString]['start_time'])

        # update scripts
        elif job_type == 'script':
            # if error on runScript, the cbString is not a ProcessCallback...
            if not cbString.startswith('ProcessCallback'): continue  # ignore
            if status not in ("failed", "finished"):
                logger.info("Check callback on script: %s" % cbString)
                proc = omero.grid.ScriptProcessPrx.checkedCast(conn.c.ic.stringToProxy(cbString))
                cb = omero.scripts.ProcessCallbackI(conn.c, proc)
                # check if we get something back from the handle...
                if cb.block(0): # ms.
                    cb.close()
                    try:
                        results = proc.getResults(0)        # we can only retrieve this ONCE - must save results
                        request.session['callback'][cbString]['status'] = "finished"
                    except Exception, x:
                        logger.error(traceback.format_exc())
                        continue
                    # value could be rstring, rlong, robject
                    rMap = {}
                    for key, value in results.items():
                        v = value.getValue()
                        if key in ("stdout", "stderr", "Message"):
                            if key in ('stderr', 'stdout'):
                                v = v.id.val    # just save the id of original file
                            request.session['callback'][cbString][key] = v
                        else:
                            if hasattr(v, "id"):    # do we have an object (ImageI, FileAnnotationI etc)
                                obj_data = {'id': v.id.val, 'type': v.__class__.__name__[:-1]}
                                obj_data['browse_url'] = getObjectUrl(conn, v)
                                if v.isLoaded() and hasattr(v, "file"):
                                    #try:
                                    mimetypes = {'image/png':'png', 'image/jpeg':'jpeg', 'image/tiff': 'tiff'}
                                    if v.file.mimetype.val in mimetypes:
                                        obj_data['fileType'] = mimetypes[v.file.mimetype.val]
                                        obj_data['fileId'] = v.file.id.val
                                    obj_data['name'] = v.file.name.val
                                    #except:
                                    #    pass
                                if v.isLoaded() and hasattr(v, "name"):  # E.g Image, OriginalFile etc
                                    obj_data['name'] = v.name.val
                                rMap[key] = obj_data
                            else:
                                rMap[key] = v
                    request.session['callback'][cbString]['results'] = rMap
                    request.session.modified = True
                else:
                    in_progress+=1

            # make a copy of the map in session, so that we can replace non json-compatible objects, without modifying session
            rv[cbString] = copy.copy(request.session['callback'][cbString])
            rv[cbString]['start_time'] = str(request.session['callback'][cbString]['start_time'])

    rv['inprogress'] = in_progress
    rv['failure'] = failure
    rv['jobs'] = len(request.session['callback'])

    return HttpResponse(simplejson.dumps(rv),mimetype='application/javascript') # json


@isUserConnected
def status_action (request, action=None, **kwargs):
    """
    Opens the Activites window, using data from jobs in the request.session['callback'].
    Once this window is open, AJAX calls are used to poll the request.session['callback'] and update the page

    If the above 'action' == 'clean' then we clear jobs from request.session['callback']
    either a single job (if 'jobKey' is specified in POST) or all jobs (apart from those in progress)
    """

    request.session.modified = True

    request.session['nav']['menu'] = 'status'

    if action == "clean":
        if 'jobKey' in request.POST:
            jobId = request.POST.get('jobKey')
            rv = {}
            if jobId in request.session['callback']:
                del request.session['callback'][jobId]
                request.session.modified = True
                rv['removed'] = True
            else:
                rv['removed'] = False
            return HttpResponse(simplejson.dumps(rv),mimetype='application/javascript')
        else:
            for key, data in request.session['callback'].items():
                if data['status'] != "in progress":
                    del request.session['callback'][key]
        return HttpResponseRedirect(reverse("status"))

    elif action == "update":
        # try to update the 'attribute' of the job with 'new_value' (or None)
        if 'jobKey' in request.POST:
            jobId = request.POST.get('jobKey')
            rv = {'updated':False}
            if jobId in request.session['callback']:
                if 'attribute' in request.POST:
                    attribute = request.POST.get('attribute')
                    new_value = request.POST.get('new_value', None)
                    request.session['callback'][jobId][attribute] = new_value
                    request.session.modified = True
                    rv['updated'] = True
            return HttpResponse(simplejson.dumps(rv),mimetype='application/javascript')

    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")

    template = "webclient/status/statusWindow.html"
    if request.REQUEST.get('content_only'):
        template = "webclient/status/activitiesContent.html"

    _purgeCallback(request)

    jobs = []
    for key, data in request.session['callback'].items():
        # E.g. key: ProcessCallback/39f77932-c447-40d8-8f99-910b5a531a25 -t:tcp -h 10.211.55.2 -p 54727:tcp -h 10.37.129.2 -p 54727:tcp -h 10.12.2.21 -p 54727
        # create id we can use as html id, E.g. 39f77932-c447-40d8-8f99-910b5a531a25
        data['id'] = key
        if len(key.split(" ")) > 0:
            data['id'] = key.split(" ")[0]
            if len(data['id'].split("/")) > 1:
                data['id'] = data['id'].split("/")[1]
        data['key'] = key
        jobs.append(data)

    jobs.sort(key=lambda x:x['start_time'], reverse=True)
    context = {'sizeOfJobs':len(request.session['callback']), 'jobs':jobs }

    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))
            
####################################################################################
# User Photo

@isUserConnected
def avatar(request, oid=None, **kwargs):
    """ Returns the experimenter's photo """

    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    photo = conn.getExperimenterPhoto(oid)
    return HttpResponse(photo, mimetype='image/jpeg')

####################################################################################
# Bird's eye view

@isUserConnected
def render_birds_eye_view (request, iid, size=200, share_id=None, **kwargs):
    """ Delegates to webgateway, using share connection if appropriate """

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
        raise Exception("Connection not available")

    return webgateway_views.render_birds_eye_view(request, iid, size=size, _conn=conn, _defcb=conn.defaultThumbnail, **kwargs)

####################################################################################
# Rendering

@isUserConnected
def render_thumbnail (request, iid, share_id=None, **kwargs):
    """ Delegates to webgateway, using share connection if appropriate """

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
        raise Exception("Connection not available")

    return webgateway_views.render_thumbnail(request, iid, w=80, _conn=conn, _defcb=conn.defaultThumbnail, **kwargs)

@isUserConnected
def render_thumbnail_resize (request, size, iid, share_id=None, **kwargs):
    """ Delegates to webgateway, using share connection if appropriate """

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
        raise Exception("Connection not available")
    
    return webgateway_views.render_thumbnail(request, iid, w=size, _conn=conn, _defcb=conn.defaultThumbnail, **kwargs)

@isUserConnected
def render_image (request, iid, z, t, share_id=None, **kwargs):
    """ Renders the image with id {{iid}} at {{z}} and {{t}} as jpeg.
        Many options are available from the request dict.
    I am assuming a single Pixels object on image with imageId='iid'. May be wrong """

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
        raise Exception("Connection not available")

    return webgateway_views.render_image(request, iid, z, t, _conn=conn, **kwargs)

@isUserConnected
def render_image_region (request, iid, z, t, server_id=None, share_id=None, _conn=None, **kwargs):
    """ Renders the image with id {{iid}} at {{z}} and {{t}} as jpeg.
        Many options are available from the request dict.
    I am assuming a single Pixels object on image with imageId='iid'. May be wrong """

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
        raise Exception("Connection not available")

    return webgateway_views.render_image_region(request, iid, z, t, server_id=None, _conn=conn, **kwargs)

@isUserConnected
def plateGrid_json (request, pid, field=0, server_id=None, _conn=None, **kwargs):
    """ This view is responsible for showing well data within plate """
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    if conn is None:
        raise Exception("Connection not available")
    
    def urlprefix(iid):
        return reverse('render_thumbnail', args=(iid,))
    kwargs['urlprefix'] = urlprefix
    
    return webgateway_views.plateGrid_json(request, pid, field=field, server_id=None, _conn=None, **kwargs)

@isUserConnected
def image_viewer (request, iid, share_id=None, **kwargs):
    """ This view is responsible for showing pixel data as images. Delegates to webgateway, using share connection if appropriate """
    
    conn = None
    if share_id is not None:
        kwargs['viewport_server'] = reverse('webindex') + ("%s/" % share_id)
        try:
            conn = kwargs["conn_share"]
        except:
            logger.error(traceback.format_exc())
            return handlerInternalError("Connection is not available. Please contact your administrator.")
    else:
        kwargs['viewport_server'] = reverse('webindex')
        try:
            conn = kwargs["conn"]
        except:
            logger.error(traceback.format_exc())
            return handlerInternalError("Connection is not available. Please contact your administrator.")
         
    if conn is None:
        raise Exception("Connection not available")

    return webgateway_views.full_viewer(request, iid, _conn=conn, **kwargs)


@isUserConnected
def imageData_json (request, iid, share_id=None, **kwargs):
    """ Get a dict with image information. Delegates to webgateway, using share connection if appropriate """
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
        raise Exception("Connection not available")

    return HttpResponse(webgateway_views.imageData_json(request, iid=iid, _conn=conn, **kwargs), mimetype='application/javascript')

@isUserConnected
def render_row_plot (request, iid, z, t, y, share_id=None, w=1, **kwargs):
    """ Plot of intenisty for a row of pixels. Delegates to webgateway, using share connection if appropriate """
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
        raise Exception("Connection not available")
    img = conn.getObject("Image", iid)

    return webgateway_views.render_row_plot(request, iid=iid, z=z, t=t, y=y, w=w, _conn=conn, **kwargs)

@isUserConnected
def render_col_plot (request, iid, z, t, x, share_id=None, w=1, **kwargs):
    """ Plot of intenisty for a row of pixels. Delegates to webgateway, using share connection if appropriate """
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
        raise Exception("Connection not available")
    img = conn.getObject("Image", iid)

    return webgateway_views.render_col_plot(request, iid=iid, z=z, t=t, x=x, w=w, _conn=conn, **kwargs)

@isUserConnected
def render_split_channel (request, iid, z, t, share_id=None, **kwargs):
    """ Jpeg of each channel as a separate panel. Delegates to webgateway, using share connection if appropriate """

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
        raise Exception("Connection not available")
    img = conn.getObject("Image", iid)

    return webgateway_views.render_split_channel(request, iid, z, t, _conn=conn, **kwargs)


# scripting service....
@isUserConnected
def list_scripts (request, **kwargs):
    """ List the available scripts - Just officical scripts for now """

    conn = kwargs['conn']

    scriptService = conn.getScriptService()
    scripts = scriptService.getScripts()

    # group scripts into 'folders' (path), named by parent folder name
    scriptMenu = {}
    for s in scripts:
        scriptId = s.id.val
        path = s.path.val
        name = s.name.val
        fullpath = os.path.join(path, name)
        if fullpath in settings.SCRIPTS_TO_IGNORE:
            logger.info('Ignoring script %r' % fullpath)
            continue
        displayName = name.replace("_", " ")

        if path not in scriptMenu:
            folder, name = os.path.split(path)
            if len(name) == 0:      # path was /path/to/folderName/  - we want 'folderName'
                folderName = os.path.basename(folder)
            else:                   # path was /path/to/folderName  - we want 'folderName'
                folderName = name
            folderName = folderName.title().replace("_", " ")
            scriptMenu[path] = {'name': folderName, 'scripts': []}

        scriptMenu[path]['scripts'].append((scriptId, displayName))

    # convert map into list
    scriptList = []
    for path, sData in scriptMenu.items():
        sData['path'] = path    # sData map has 'name', 'path', 'scripts'
        scriptList.append(sData)
    scriptList.sort(key=lambda x:x['name'])

    return render_to_response("webclient/scripts/list_scripts.html", {'scriptMenu': scriptList})

@isUserConnected
def script_ui(request, scriptId, **kwargs):
    """
    Generates an html form for the parameters of a defined script.
    """

    conn = kwargs['conn']
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
    Data_TypeParam = None
    IDsParam = None
    for key, param in params.inputs.items():
        i = {}
        i["name"] = key.replace("_", " ")
        i["key"] = key
        if not param.optional:
            i["required"] = True
        i["description"] = param.description
        if param.min:
            i["min"] = str(param.min.getValue())
        if param.max:
            i["max"] = str(param.max.getValue())
        if param.values:
            i["options"] = [v.getValue() for v in param.values.getValue()]
        if param.useDefault:
            i["default"] = unwrap(param.prototype)
        pt = unwrap(param.prototype)
        if pt.__class__.__name__ == 'dict':
            i["map"] = True
        elif pt.__class__.__name__ == 'list':
            i["list"] = True
            if "default" in i: i["default"] = i["default"][0]
        elif pt.__class__ == type(True):
            i["boolean"] = True
        elif pt.__class__ == type(0) or pt.__class__ == type(long(0)):
            i["number"] = "number"  # will stop the user entering anything other than numbers.
        elif pt.__class__ == type(float(0.0)):
            i["number"] = "float"

        # if we got a value for this key in the page request, use this as default
        if request.REQUEST.get(key, None) is not None:
            i["default"] = request.REQUEST.get(key, None)

        i["prototype"] = unwrap(param.prototype)    # E.g  ""  (string) or [0] (int list) or 0.0 (float)
        i["grouping"] = param.grouping
        inputs.append(i)

        if key == "IDs": IDsParam = i           # remember these...
        if key == "Data_Type": Data_TypeParam = i
    inputs.sort(key=lambda i: i["grouping"])

    # if we have Data_Type param that has a "default" and "options" - check they match...
    if Data_TypeParam is not None and "default" in Data_TypeParam and "options" in Data_TypeParam:
        if Data_TypeParam["default"] not in Data_TypeParam["options"]:
            IDsParam["default"] = ""        # ... if not, don't set IDs

    # try to determine hierarchies in the groupings - ONLY handle 1 hierarchy level now (not recursive!)
    for i in range(len(inputs)):
        if len(inputs) <= i:    # we may remove items from inputs as we go - need to check
            break
        param = inputs[i]
        grouping = param["grouping"]    # E.g  03
        param['children'] = list()
        c = 1
        while len(inputs) > i+1:
            nextParam = inputs[i+1]
            nextGrp = inputs[i+1]["grouping"]  # E.g. 03.1
            if nextGrp.split(".")[0] == grouping:
                param['children'].append(inputs[i+1])
                inputs.pop(i+1)
            else:
                break

    paramData["inputs"] = inputs

    return render_to_response('webclient/scripts/script_ui.html', {'paramData': paramData, 'scriptId': scriptId}, 
        context_instance=Context(request))

@isUserConnected
def script_run(request, scriptId, **kwargs):
    """
    Runs a script using values in a POST
    """
    conn = kwargs['conn']
    scriptService = conn.getScriptService()

    inputMap = {}

    sId = long(scriptId)

    params = scriptService.getParams(sId)
    scriptName = params.name.replace("_", " ").replace(".py", "")

    logger.debug("Script: run with request.POST: %s" % request.POST)

    for key, param in params.inputs.items():
        prototype = param.prototype
        pclass = prototype.__class__
        
        # handle bool separately, since unchecked checkbox will not be in request.POST
        if pclass == omero.rtypes.RBoolI:
            value = key in request.POST
            inputMap[key] = pclass(value)
            continue
        
        if pclass.__name__ == 'RMapI':
            keyName = "%s_key" % key
            valueName = "%s_value" % key
            row = 0
            paramMap = {}
            while keyName in request.POST:
                # the key and value don't have any data-type defined by scripts - just use string
                k = str(request.POST[keyName])
                v = str(request.POST[valueName])
                if len(k) > 0 and len(v) > 0:
                    paramMap[str(k)] = str(v)
                row +=1
                keyName = "%s_key%d" % (key, row)
                valueName = "%s_value%d" % (key, row)
            if len(paramMap) > 0:
                inputMap[key] = wrap(paramMap)
            continue

        if key in request.POST:
            if pclass == omero.rtypes.RListI:
                values = request.POST.getlist(key)
                if len(values) == 0: continue
                if len(values) == 1:     # process comma-separated list
                    if len(values[0]) == 0: continue
                    values = values[0].split(",")

                # try to determine 'type' of values in our list
                listClass = omero.rtypes.rstring
                l = prototype.val     # list
                if len(l) > 0:       # check if a value type has been set (first item of prototype list)
                    listClass = l[0].__class__
                    if listClass == int(1).__class__:
                        listClass = omero.rtypes.rint
                    if listClass == long(1).__class__:
                        listClass = omero.rtypes.rlong

                # construct our list, using appropriate 'type'
                valueList = []
                for v in values:
                    try:
                        obj = listClass(str(v.strip())) # convert unicode -> string
                    except:
                        logger.debug("Invalid entry for '%s' : %s" % (key, v))
                        continue
                    if isinstance(obj, omero.model.IObject):
                        valueList.append(omero.rtypes.robject(obj))
                    else:
                        valueList.append(obj)
                inputMap[key] = omero.rtypes.rlist(valueList)

            # Handle other rtypes: String, Long, Int etc.
            else:
                value = request.POST[key]
                if len(value) == 0: continue
                try:
                    inputMap[key] = pclass(value)
                except:
                    logger.debug("Invalid entry for '%s' : %s" % (key, value))
                    continue

    logger.debug("Running script %s with params %s" % (scriptName, inputMap))
    try:
        handle = scriptService.runScript(sId, inputMap, None)
        # E.g. ProcessCallback/4ab13b23-22c9-4b5f-9318-40f9a1acc4e9 -t:tcp -h 10.37.129.2 -p 53154:tcp -h 10.211.55.2 -p 53154:tcp -h 10.12.1.230 -p 53154
        jobId = str(handle)
        request.session['callback'][jobId] = {
            'job_type': "script",
            'job_name': scriptName,
            'start_time': datetime.datetime.now(),
            'status':'in progress'}
        request.session.modified = True
    except Exception, x:
        jobId = str(time())      # E.g. 1312803670.6076391
        if x.message == "No processor available.": # omero.ResourceError
            logger.info(traceback.format_exc())
            error = None
            status = 'no processor available'
            message = 'No Processor Available: Please try again later'
        else:
            logger.error(traceback.format_exc())
            error = traceback.format_exc()
            status = 'failed'
            message = x.message
        # save the error to http session, for display in 'Activities' window
        request.session['callback'][jobId] = {
            'job_type': "script",
            'job_name': scriptName,
            'start_time': datetime.datetime.now(),
            'status':status,
            'Message': message,
            'error':error}
        request.session.modified = True
        # we return this, although it is now ignored (script window closes)
        return HttpResponse(simplejson.dumps({'status': status, 'error': error}), mimetype='json')

    return HttpResponse(simplejson.dumps({'jobId': jobId, 'status':'in progress'}), mimetype='json')


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

