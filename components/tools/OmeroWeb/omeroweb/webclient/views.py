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

from time import time
from thread import start_new_thread

from django.conf import settings
from django.contrib.sessions.backends.db import SessionStore
from django.contrib.sessions.models import Session
from django.core import template_loader
from django.core.cache import cache
from django.http import HttpResponse, HttpRequest, HttpResponseRedirect, Http404
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

from omeroweb.webadmin.controller.experimenter import BaseExperimenter

from models import ShareForm, ShareCommentForm, ContainerForm, TextAnnotationForm, UrlAnnotationForm, \
                    UploadFileForm, MyGroupsForm, MyUserForm, ActiveGroupForm, HistoryTypeForm
from omeroweb.webadmin.models import MyAccountForm, MyAccountLdapForm

from omeroweb.webadmin.models import Gateway, LoginForm
from extlib.gateway import BlitzGateway

logger = logging.getLogger('views-web')

connectors = {}

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
        # retrives the connection from existing session
        try:
            if request.session['sessionUuid']: pass
            if request.session['groupId']: pass
        except KeyError:
            # retrives the connection from login parameters
            try:
                if request.session['host']: pass
                if request.session['port']: pass
                if request.session['username']: pass
                if request.session['password']: pass
            except KeyError:
                pass
            else:
                # login parameters found, create the connection
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
                request.session['sessionUuid'] = conn.getEventContext().sessionUuid
                request.session['groupId'] = conn.getEventContext().groupId
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
            # connection is no longer available, retrieves connection login parameters
            connectors[conn_key] = None
            logger.debug("Connection '%s' is no longer available" % (conn_key))
            try:
                if request.session['host']: pass
                if request.session['port']: pass
                if request.session['username']: pass
                if request.session['password']: pass
            except KeyError:
                pass
            else:
                try:
                    conn = BlitzGateway(request.session['host'], request.session['port'], request.session['username'], request.session['password'], request.session['sessionUuid'])
                    conn.connect()
                    request.session['sessionUuid'] = conn.getEventContext().sessionUuid
                    request.session['groupId'] = conn.getEventContext().groupId
                except:
                    logger.error(traceback.format_exc())
                    raise sys.exc_info()[1]
                else:
                    # stores connection on connectors
                    connectors[conn_key] = conn
                    logger.debug("Retreived connection for:'%s', uuid: '%s'" % (str(conn_key), str(request.session['sessionUuid'])))
                    logger.info("Total connectors: %i." % (len(connectors)))
        else:
            logger.debug("Connection exists: '%s', uuid: '%s'" % (str(conn_key), str(request.session['sessionUuid'])))
    return conn

@timeit
def getShareConnection (request):
    session_key = None
    server = None

    # gets Http session or create new one
    session_key = request.session.session_key

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
        conn_key = 'S:' + str(request.session.session_key) + '#' + str(server) +" share"
    else:
        return None

    request.session.modified = True
    
    # gets connection for key if available
    conn = connectors.get(conn_key)

    if conn is None:
        # could not get connection for key
        # retrives the connection from existing session
        try:
            if request.session['shareSessionId']: pass
        except KeyError:
            # retrives the connection from login parameters
            try:
                if request.session['host']: pass
                if request.session['port']: pass
                if request.session['username']: pass
                if request.session['password']: pass
            except KeyError:
                pass
            else:
                # login parameters found, create the connection
                try:
                    conn = BlitzGateway(request.session['host'], request.session['port'], "root", "ome")
                    conn.connect()
                    request.session['shareSessionId'] = conn.getEventContext().sessionUuid
                except:
                    logger.error(traceback.format_exc())
                    raise sys.exc_info()[1]
                else:
                    # stores connection on connectors
                    connectors[conn_key] = conn
                    logger.debug("Have connection, stored in connectors:'%s', uuid: '%s'" % (str(conn_key), str(request.session['shareSessionId'])))
        else:
            # retrieves connection from sessionUuid, join to existing session
            try:
                conn = BlitzGateway(request.session['host'], request.session['port'], "root", "ome", request.session['shareSessionId'])
                conn.connect()
                request.session['shareSessionId'] = conn.getEventContext().sessionUuid
            except:
                logger.error(traceback.format_exc())
                raise sys.exc_info()[1]
            else:
                # stores connection on connectors
                connectors[conn_key] = conn
                logger.debug("Retreived connection, will travel: '%s', uuid: '%s'" % (str(conn_key), str(request.session['shareSessionId'])))
    else:
        # gets connection
        try:
            request.session['shareSessionId'] = conn.getEventContext().sessionUuid
        except:
            # connection is no longer available, retrieves connection login parameters
            connectors[conn_key] = None
            logger.debug("Connection '%s' is no longer available" % (conn_key))
            try:
                if request.session['host']: pass
                if request.session['port']: pass
                if request.session['username']: pass
                if request.session['password']: pass
            except KeyError:
                pass
            else:
                try:
                    conn = BlitzGateway(request.session['host'], request.session['port'], "root", "ome", request.session['shareSessionId'])
                    conn.connect()
                    request.session['shareSessionId'] = conn.getEventContext().sessionUuid
                    request.session['groupId'] = conn.getEventContext().groupId
                    connectors[conn_key] = conn
                    logger.debug("Retreived connection, will travel:%s, uuid: %s" % (str(conn_key), str(request.session['shareSessionId'])))
                except:
                    logger.error(traceback.format_exc())
                    raise sys.exc_info()[1]
                else:
                    # stores connection on connectors
                    connectors[conn_key] = conn
                    logger.debug("Retreived connection for:'%s', uuid: '%s'" % (str(conn_key), str(request.session['sessionUuid'])))
                    logger.info("Total connectors: %i." % (len(connectors)))
    
        else:
            logger.debug("Connection exists: '%s', uuid: '%s'" % (str(conn_key), str(request.session['shareSessionId'])))
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
        if request.session['imageInBasket']:
            pass
    except:
        request.session['imageInBasket'] = list()
    try:
        if request.session['datasetInBasket']:
            pass
    except:
        request.session['datasetInBasket'] = list()
    try:
        if request.session['projectInBasket']:
            pass
    except:
        request.session['projectInBasket'] = list()
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
        request.session['username'] = request.REQUEST['username']
        request.session['password'] = request.REQUEST['password']
        request.session['groupId'] = None
        request.session['imageInBasket'] = list()
        request.session['datasetInBasket'] = list()
        request.session['projectInBasket'] = list()
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
    
    sessionHelper(request)
    
    try:
        if request.session['nav']['menu'] != 'start':
            request.session['nav']['menu'] = 'home'
    except:
        request.session['nav']['menu'] = 'start'
    
    controller = BaseIndex(conn)
    controller.loadData()
    form_active_group = ActiveGroupForm(initial={'activeGroup':controller.eContext['context'].groupId, 'mygroups': controller.eContext['memberOfGroups']})
    
    context = {'nav':request.session['nav'], 'controller':controller, 'eContext': controller.eContext, 'form_active_group':form_active_group}

    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isUserConnected
def index_my_data(request, **kwargs):
    template = "omeroweb/index_my_data.html"
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    controller = BaseContainer(conn)
    #controller.loadMyContainerHierarchy()
    controller.listMyRoots()
    
    context = {'nav':request.session['nav'], 'eContext': controller.eContext, 'controller':controller}
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
    
    controller = BaseIndex(conn)
    controller.loadMostRecent()
    
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
        #logger.error(traceback.format_exc())
        pass
    try:
        del request.session['imageInBasket']
    except KeyError:
        logger.error(traceback.format_exc())
    try:
        del request.session['datasetInBasket']
    except KeyError:
        logger.error(traceback.format_exc())
    try:
        del request.session['projectInBasket']
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
def manage_my_data(request, o1_type=None, o1_id=None, o2_type=None, o2_id=None, o3_type=None, o3_id=None, **kwargs):
    request.session['nav']['menu'] = 'mydata'
    request.session['nav']['whos'] = 'mydata'
    
    try:
        url = kwargs["url"]
    except:
        logger.error(traceback.format_exc())
    
    try:
        request.session['nav']['view'] = request.REQUEST['view'] # table, icon, tree 
    except:
        pass
    
    view = request.session['nav']['view']
    menu = request.session['nav']['menu']
    whos = request.session['nav']['whos']

    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    manager = BaseContainer(conn, o1_type, o1_id, o2_type, o2_id, o3_type, o3_id)
    manager.buildBreadcrumb(whos)
        
    form_active_group = ActiveGroupForm(initial={'activeGroup':manager.eContext['context'].groupId, 'mygroups': manager.eContext['memberOfGroups']})
    
    if o1_type and o1_id:
        form_comment = TextAnnotationForm()
        form_url = UrlAnnotationForm(initial={'link':'http://'})
        form_file = UploadFileForm()
    
    template = None
    if o3_type and o3_id:
        if o3_type == 'image':
            template = "omeroweb/image_details.html"
    elif o2_type and o2_id:
        if o2_type == 'dataset':
            manager.listMyImagesInDataset(o2_id)
        elif o2_type == 'image':
            template = "omeroweb/image_details.html"
    elif o1_type and o1_id:
        if o1_type == 'project':
            manager.listMyDatasetsInProject(o1_id)
        elif o1_type == 'dataset':
            if view == 'tree':
                manager.loadMyImages(o1_id)
            else:
                manager.listMyImagesInDataset(o1_id)
        elif o1_type == 'image':
            template = "omeroweb/image_details.html"
    else:
        if view == 'tree':
            manager.loadMyContainerHierarchy()
        else:
            manager.listMyRoots()
    
    if template is None and view =='icon':
        template = "omeroweb/containers_icon.html"
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_active_group':form_active_group}
    elif template is None and view =='table':
        template = "omeroweb/containers_table.html"
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_active_group':form_active_group}
    elif template is None and view =='tree' and o1_type is None and o1_id is None:
        template = "omeroweb/containers_tree.html"
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_active_group':form_active_group}
    elif view == 'tree' and o1_type=='dataset' and o1_id > 0:
        template = "omeroweb/container_subtree.html"
        context = {'manager':manager, 'eContext':manager.eContext}
    else:
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_comment':form_comment, 'form_url':form_url, 'form_file':form_file, 'form_active_group':form_active_group}
    
    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def manage_user_containers(request, o1_type=None, o1_id=None, o2_type=None, o2_id=None, o3_type=None, o3_id=None, **kwargs):
    request.session['nav']['menu'] = 'collaboration'
    request.session['nav']['whos'] = 'userdata'
    
    try:
        url = kwargs["url"]
    except:
        logger.error(traceback.format_exc())
    
    try:
        request.session['nav']['view'] = request.REQUEST['view'] # table, icon, tree 
    except:
        pass
    
    view = request.session['nav']['view']
    menu = request.session['nav']['menu']
    whos = request.session['nav']['whos']
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    manager = BaseContainer(conn, o1_type, o1_id, o2_type, o2_id, o3_type, o3_id)
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
    
    form_active_group = ActiveGroupForm(initial={'activeGroup':manager.eContext['context'].groupId, 'mygroups': manager.eContext['memberOfGroups']})
    
    if o1_type and o1_id:
        form_comment = TextAnnotationForm()
        form_url = UrlAnnotationForm(initial={'link':'http://'})
        form_file = UploadFileForm()
    
    template = None
    if o3_type and o3_id:
        if o3_type == 'image':
            template = "omeroweb/image_details.html"
    elif o2_type and o2_id:
        if o2_type == 'dataset':
            if filter_user_id is not None:
                manager.listImagesInDatasetInUser(o2_id, filter_user_id)
        elif o2_type == 'image':
            template = "omeroweb/image_details.html"
    elif o1_type and o1_id:
        if o1_type == 'project':
            if filter_user_id is not None:
                manager.listDatasetsInProjectInUser(o1_id, filter_user_id)
        elif o1_type == 'dataset':
            if view == 'tree':
                if filter_user_id is not None:
                    manager.loadUserImages(o1_id, filter_user_id)
            else:
                if filter_user_id is not None:
                    manager.listImagesInDatasetInUser(o1_id, filter_user_id)
        elif o1_type == 'image':
            template = "omeroweb/image_details.html"
    else:
        if view == 'tree':
            if filter_user_id is not None:
                manager.loadUserContainerHierarchy(filter_user_id)
        else:
            if filter_user_id is not None:
                manager.listRootsInUser(filter_user_id)

    
    if template is None and view =='icon':
        template = "omeroweb/containers_icon.html"
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_users':form_users, 'form_mygroups':form_mygroups, 'form_active_group':form_active_group}
    elif template is None and view =='table':
        template = "omeroweb/containers_table.html"
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_users':form_users, 'form_mygroups':form_mygroups, 'form_active_group':form_active_group}
    elif template is None and view =='tree' and o1_type is None and o1_id is None:
        template = "omeroweb/containers_tree.html"
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_users':form_users, 'form_mygroups':form_mygroups, 'form_active_group':form_active_group}
    elif view == 'tree' and o1_type=='dataset' and o1_id > 0:
        template = "omeroweb/container_subtree.html"
        context = {'manager':manager, 'eContext':manager.eContext}
    else:
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager}
    
    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def manage_group_containers(request, o1_type=None, o1_id=None, o2_type=None, o2_id=None, o3_type=None, o3_id=None, **kwargs):
    request.session['nav']['menu'] = 'collaboration'
    request.session['nav']['whos'] = 'groupdata'
    
    try:
        url = kwargs["url"]
    except:
        logger.error(traceback.format_exc())
        
    try:
        request.session['nav']['view'] = request.REQUEST['view'] # table, icon, tree 
    except:
        pass
    
    view = request.session['nav']['view']
    menu = request.session['nav']['menu']
    whos = request.session['nav']['whos']
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    manager = BaseContainer(conn, o1_type, o1_id, o2_type, o2_id, o3_type, o3_id)
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
    
    form_active_group = ActiveGroupForm(initial={'activeGroup':manager.eContext['context'].groupId, 'mygroups': manager.eContext['memberOfGroups']})
    
    if o1_type and o1_id:
        form_comment = TextAnnotationForm()
        form_url = UrlAnnotationForm(initial={'link':'http://'})
        form_file = UploadFileForm()
    
    template = None
    if o3_type and o3_id:
        if o3_type == 'image':
            template = "omeroweb/image_details.html"
    elif o2_type and o2_id:
        if o2_type == 'dataset':
            if filter_group_id is not None:
                manager.listImagesInDatasetInGroup(o2_id, filter_group_id)
        if o2_type == 'image':
            template = "omeroweb/image_details.html"
    elif o1_type and o1_id:
        if o1_type == 'project':
            if filter_group_id is not None:
                manager.listDatasetsInProjectInGroup(o1_id, filter_group_id)
        elif o1_type == 'dataset':
            if view == 'tree':
                if filter_group_id is not None:
                    manager.loadGroupImages(o1_id, filter_group_id)
            else:
                if filter_group_id is not None:
                    manager.listImagesInDatasetInGroup(o1_id, filter_group_id)
        if o1_type == 'image':
            template = "omeroweb/image_details.html"
    else:
        if view == 'tree':
            if filter_group_id is not None:
                manager.loadGroupContainerHierarchy(filter_group_id)
        else:
            if filter_group_id is not None:
                manager.listRootsInGroup(filter_group_id)
                    
    if template is None and view =='icon':
        template = "omeroweb/containers_icon.html"
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_mygroups':form_mygroups, 'form_users':form_users, 'form_active_group':form_active_group}
    elif template is None and view =='table':
        template = "omeroweb/containers_table.html"
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_mygroups':form_mygroups, 'form_users':form_users, 'form_active_group':form_active_group}
    elif template is None and view =='tree' and o1_type is None and o1_id is None:
        template = "omeroweb/containers_tree.html"
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_mygroups':form_mygroups, 'form_users':form_users, 'form_active_group':form_active_group}
    elif view == 'tree' and o1_type=='dataset' and o1_id > 0:
        template = "omeroweb/container_subtree.html"
        context = {'manager':manager, 'eContext':manager.eContext}
    else:
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager}
    
    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def manage_annotations(request, o_type, o_id, **kwargs):
    url = None
    try:
        url = kwargs["url"]  
    except:
        pass
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    manager = BaseContainer(conn, o_type, o_id)
    if o_type == 'project':
        template = "omeroweb/container_annotations.html"
        manager.projectAnnotationList()
    elif o_type == "dataset":
        template = "omeroweb/container_annotations.html"
        manager.datasetAnnotationList()
    elif o_type == "image":
        template = "omeroweb/image_annotations.html"
        manager.imageAnnotationList()
    
    form_comment = TextAnnotationForm()
    form_url = UrlAnnotationForm(initial={'link':'http://'})
    form_file = UploadFileForm()
    
    context = {'url':url, 'manager':manager, 'form_comment':form_comment, 'form_url':form_url, 'form_file':form_file}

    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))
    
@isUserConnected
def manage_tree_details(request, c_type, c_id, **kwargs):
    template = "omeroweb/container_tree_details.html"
    
    url = None
    try:
        url = kwargs["url"]
    except:
        pass
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    whos = request.session['nav']['whos']
    
    manager = BaseContainer(conn, c_type, c_id)
    
    context = {'url':url, 'nav':request.session['nav'], 'eContext':manager.eContext, 'manager':manager}

    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def manage_container_hierarchy(request, o_type=None, o_id=None, **kwargs):
    template = "omeroweb/hierarchy.html"
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    whos = request.session['nav']['whos']
    
    manager = BaseContainer(conn, o_type, o_id)
    manager.loadHierarchy()
    
    context = {'nav':request.session['nav'], 'eContext':manager.eContext, 'manager':manager}

    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))
    

###########################################################################
# ACTIONS

@isUserConnected
def manage_action_containers(request, action, o_type=None, o_id=None, **kwargs):
    template = None
    
    try:
        url = request.REQUEST['url'] # table, icon, tree 
    except:
        url = kwargs["url"]
        logger.error(traceback.format_exc())

    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    manager = None
    if o_type == "dataset" or o_type == "project" or o_type == "image":
        manager = BaseContainer(conn, o_type, o_id)
        manager.buildBreadcrumb(action)
    else:
        manager = BaseContainer(conn)
        manager.buildBreadcrumb(action)
    
    form_active_group = ActiveGroupForm(initial={'activeGroup':manager.eContext['context'].groupId, 'mygroups': manager.eContext['memberOfGroups']})
    
    form = None
    if action == 'new':
        template = "omeroweb/container_new.html"
        form = ContainerForm(initial={'owner': ('r', 'w'), 'group': ('', ''), 'world': ('', '')})
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form':form, 'form_active_group':form_active_group}
    elif action == 'edit':
        if o_type == "dataset":
            template = "omeroweb/container_form.html"
            form = ContainerForm(initial={'name': manager.dataset.name, 'description':manager.dataset.description, \
                        'owner': ((manager.dataset.details.permissions.isUserRead() and 'r' or ''), (manager.dataset.details.permissions.isUserWrite() and 'w' or '')), \
                        'group': ((manager.dataset.details.permissions.isGroupRead() and 'r' or ''), (manager.dataset.details.permissions.isGroupWrite() and 'w' or '')), \
                        'world': ((manager.dataset.details.permissions.isWorldRead() and 'r' or ''), (manager.dataset.details.permissions.isWorldWrite() and 'w' or ''))})
            context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form':form, 'form_active_group':form_active_group}
        elif o_type == "project":
            template = "omeroweb/container_form.html"
            form = ContainerForm(initial={'name': manager.project.name, 'description':manager.project.description, \
                        'owner': ((manager.project.details.permissions.isUserRead() and 'r' or ''), (manager.project.details.permissions.isUserWrite() and 'w' or '')), \
                        'group': ((manager.project.details.permissions.isGroupRead() and 'r' or ''), (manager.project.details.permissions.isGroupWrite() and 'w' or '')), \
                        'world': ((manager.project.details.permissions.isWorldRead() and 'r' or ''), (manager.project.details.permissions.isWorldWrite() and 'w' or ''))})
            context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form':form, 'form_active_group':form_active_group}
        elif o_type =="image" and o_id > 0:
            template = "omeroweb/container_form.html"
            form = ContainerForm(initial={'name': manager.image.name, 'description':manager.image.description, 'permissions':manager.image.details.permissions, \
                        'owner': ((manager.image.details.permissions.isUserRead() and 'r' or ''), (manager.image.details.permissions.isUserWrite() and 'w' or '')), \
                        'group': ((manager.image.details.permissions.isGroupRead() and 'r' or ''), (manager.image.details.permissions.isGroupWrite() and 'w' or '')), \
                        'world': ((manager.image.details.permissions.isWorldRead() and 'r' or ''), (manager.image.details.permissions.isWorldWrite() and 'w' or ''))})
            context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form, 'form_active_group':form_active_group}
    elif action == 'move':
        message = "done"
        parent = request.REQUEST['parent'].split('-')
        source = request.REQUEST['source'].split('-')
        destination = request.REQUEST['destination'].split('-')
        if not manager.move(parent,source, destination):
            return False
        template = "omeroweb/message.html"
        try:
            if parent[1] == destination[1]:
                message = "nothing changed"
        except:
            pass
        try:
            if parent[0] == "0" and destination[0] == "0":
                message = "nothing changed"
        except:
            pass
        context = {'message': message}
    elif action == 'save':
        if o_type == "dataset":
            form = ContainerForm(data=request.REQUEST.copy())
            if form.is_valid():
                name = request.REQUEST['name']
                description = request.REQUEST['description']
                permissions = {'owner': "".join(request.REQUEST.getlist('owner')), 'group': "".join(request.REQUEST.getlist('group')), 'world': "".join(request.REQUEST.getlist('world'))}
                manager.updateDataset(name, description, permissions)
                return HttpResponseRedirect(url)
            else:
                template = "omeroweb/container_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form, 'form_active_group':form_active_group}
        elif o_type == "project":
            form = ContainerForm(data=request.REQUEST.copy())
            if form.is_valid():
                name = request.REQUEST['name']
                description = request.REQUEST['description']
                permissions = {'owner': "".join(request.REQUEST.getlist('owner')), 'group': "".join(request.REQUEST.getlist('group')), 'world': "".join(request.REQUEST.getlist('world'))}
                manager.updateProject(name, description, permissions)
                return HttpResponseRedirect(url)
            else:
                template = "omeroweb/container_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form, 'form_active_group':form_active_group}
        elif o_type == 'image':
            form = ContainerForm(data=request.REQUEST.copy())
            if form.is_valid():
                name = request.REQUEST['name']
                description = request.REQUEST['description']
                permissions = {'owner': "".join(request.REQUEST.getlist('owner')), 'group': "".join(request.REQUEST.getlist('group')), 'world': "".join(request.REQUEST.getlist('world'))}
                manager.updateImage(name, description, permissions)
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
                name = request.REQUEST['name']
                description = request.REQUEST['description']
                permissions = {'owner': "".join(request.REQUEST.getlist('owner')), 'group': "".join(request.REQUEST.getlist('group')), 'world': "".join(request.REQUEST.getlist('world'))}
                res = manager.createDataset(name, description, permissions)
                return HttpResponseRedirect(url)
            else:
                template = "omeroweb/container_new.html"
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'form':form, 'form_active_group':form_active_group}
        else:
            if request.REQUEST['folder_type'] == "dataset":
                form = ContainerForm(data=request.REQUEST.copy())
                if form.is_valid():
                    name = request.REQUEST['name']
                    description = request.REQUEST['description']
                    permissions = {'owner': "".join(request.REQUEST.getlist('owner')), 'group': "".join(request.REQUEST.getlist('group')), 'world': "".join(request.REQUEST.getlist('world'))}
                    res = manager.createDataset(name, description, permissions)
                    return HttpResponseRedirect(url)
                else:
                    template = "omeroweb/container_new.html"
                    context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'form':form, 'form_active_group':form_active_group}
            elif request.REQUEST['folder_type'] == "project":
                form = ContainerForm(data=request.REQUEST.copy())
                if form.is_valid():
                    name = request.REQUEST['name']
                    description = request.REQUEST['description']
                    permissions = {'owner': "".join(request.REQUEST.getlist('owner')), 'group': "".join(request.REQUEST.getlist('group')), 'world': "".join(request.REQUEST.getlist('world'))}
                    res = manager.createProject(name, description, permissions)
                    return HttpResponseRedirect(url)
                else:
                    template = "omeroweb/container_new.html"
                    context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'form':form, 'form_active_group':form_active_group}
    elif action == 'delete':
        pass
    elif action == "comment":
        form_comment = TextAnnotationForm(data=request.REQUEST.copy())
        if form_comment.is_valid():
            content = request.REQUEST['content']
            if o_type == "dataset":
                manager.saveDatasetTextAnnotation(content)
                return HttpResponseRedirect(url)
            elif o_type == "project":
                manager.saveProjectTextAnnotation(content)
                return HttpResponseRedirect(url)
            elif o_type == "image":
                content = request.REQUEST['content']
                manager.saveImageTextAnnotation(content)
                return HttpResponseRedirect(url)
        else:
            if o_type == "dataset" or o_type == "project":
                template = "omeroweb/containers_%s.html" % (request.session['nav']["view"])
                form_url = UrlAnnotationForm(initial={'link':'http://'})
                form_file = UploadFileForm()
                context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_comment':form_comment, 'form_url':form_url, 'form_file': form_file, 'form_active_group':form_active_group}
            elif o_type == "image":
                template = "omeroweb/image_details.html"
                form_url = UrlAnnotationForm(initial={'link':'http://'})
                form_file = UploadFileForm()
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_comment':form_comment, 'form_url':form_url, 'form_file':form_file, 'form_active_group':form_active_group}
    elif action == "url":
        form_url = UrlAnnotationForm(data=request.REQUEST.copy())
        if form_url.is_valid():
            content = request.REQUEST['link']
            if o_type == "dataset":
                manager.saveDatasetUrlAnnotation(content)
                return HttpResponseRedirect(url)
            elif o_type == "project":
                manager.saveProjectUrlAnnotation(content)
                return HttpResponseRedirect(url)
            elif o_type == "image":
                content = request.REQUEST['link']
                manager.saveImageUrlAnnotation(content)
                return HttpResponseRedirect(url)
        else:
            if o_type == "dataset" or o_type == "project":
                template = "omeroweb/containers_%s.html" % (request.session['nav']["view"])
                form_comment = TextAnnotationForm()
                form_file = UploadFileForm()
                context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_comment':form_comment, 'form_url':form_url, 'form_file':form_file, 'form_active_group':form_active_group}
            elif o_type == "image":
                template = "omeroweb/image_details.html"
                form_comment = TextAnnotationForm()
                form_file = UploadFileForm()
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_comment':form_comment, 'form_url':form_url, 'form_file':form_file, 'form_active_group':form_active_group}
    elif action == "file":
        form_file = UploadFileForm(request.REQUEST, request.FILES)
        if form_file.is_valid():
            f = request.FILES['custom_file']
            #for chunk in f.chunks():
                #print chunk
            if o_type == "dataset":
                return HttpResponseRedirect(url)
            elif o_type == "project":
                return HttpResponseRedirect(url)
            elif o_type == "image":
                return HttpResponseRedirect(url)
        else:
            if o_type == "dataset" or o_type == "project":
                template = "omeroweb/containers_%s.html" % (request.session['nav']["view"])
                form_comment = TextAnnotationForm()
                form_url = UrlAnnotationForm(initial={'link':'http://'})
                context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_comment':form_comment, 'form_url':form_url, 'form_file':form_file, 'form_active_group':form_active_group}
            elif o_type == "image":
                template = "omeroweb/image_details.html"
                form_comment = TextAnnotationForm()
                form_url = UrlAnnotationForm(initial={'link':'http://'})
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_comment':form_comment, 'form_url':form_url, 'form_file':form_file, 'form_active_group':form_active_group}
    elif action == "remove":
        return HttpResponseRedirect(url)

    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def manage_image_zoom (request, iid, **kwargs):
    template = "omeroweb/image_zoom.html"
    
    try:
        url = kwargs["url"]
    except:
        logger.error(traceback.format_exc())
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    image = BaseContainer(conn, 'image', iid)

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
    menu = "annotation"
    template = "omeroweb/annotation.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    annotation = BaseAnnotation(conn)
    annotation.getFileAnnotation(iid)

    rsp = HttpResponse(annotation.originalFile_data)
    if annotation.originalFile_data is None:
        raise Http404
    if action == 'download':
        rsp['ContentType'] = 'application/octet-stream'
        rsp['Content-Disposition'] = 'attachment; filename=%s' % (annotation.annotation.getOriginalFile().name)
    elif action == 'view':
        rsp['ContentType'] = str(annotation.annotation.getOriginalFile().format.value.val)
    return rsp

@isUserConnected
def manage_shares(request, **kwargs):
    request.session['nav']['menu'] = 'share'
    request.session['nav']['whos'] = 'share'
    try:
        request.session['nav']['view'] = request.REQUEST['view'] # table, icon, tree 
    except:
        pass
    
    view = request.session['nav']['view']

    if view == 'icon':
        template = "omeroweb/shares_icon.html"
    elif view == 'table':
        template = "omeroweb/shares_table.html"
    else:
        # TODO
        template = "omeroweb/shares_icon.html"
        
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    controller = BaseShare(conn=conn, menu=request.session['nav']['menu'])
    controller.getShares()
    form_active_group = ActiveGroupForm(initial={'activeGroup':controller.eContext['context'].groupId, 'mygroups': controller.eContext['memberOfGroups']})
    
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
    
    share = BaseShare(request.session['nav']['menu'], conn, None, oid, action)
    form_active_group = ActiveGroupForm(initial={'activeGroup':share.eContext['context'].groupId, 'mygroups': share.eContext['memberOfGroups']})
    
    experimenters = list(conn.getExperimenters())
    context = None
    form = None
    if action == "create":
        template = "omeroweb/basket_share_action.html"
        form = ShareForm(initial={'experimenters':experimenters}, data=request.REQUEST.copy())
        if form.is_valid():
            new_share = BaseShare(request.session['nav']['menu'], conn, None)
            message = request.REQUEST['message']
            expiretion = request.REQUEST['expiretion']
            members = request.REQUEST.getlist('members')
            #guests = request.REQUEST['guests']
            enable = False
            try:
                if request.REQUEST['enable']: enable = True
            except:
                pass
            host = '%s://%s:%s/%s' % (request.META['wsgi.url_scheme'], request.META['SERVER_NAME'], request.META['SERVER_PORT'], settings.WEBCLIENT_ROOT_BASE)
            new_share.createShare(host, request.session['server'], request.session['imageInBasket'], request.session['datasetInBasket'], request.session['projectInBasket'], message, expiretion, members, enable)
            return HttpResponseRedirect("/%s/share/" % (settings.WEBCLIENT_ROOT_BASE))
        else:
            
            basket = BaseBasket(conn)
            basket.load_basket(request)
            form_active_group = ActiveGroupForm(initial={'activeGroup':basket.eContext['context'].groupId, 'mygroups': basket.eContext['memberOfGroups']})
            context = {'nav':request.session['nav'], 'eContext': share.eContext, 'basket':basket, 'form':form, 'form_active_group':form_active_group}
    elif action == 'edit':
        template = "omeroweb/share_form.html"
        share.getShare(oid)
        share.loadShareContent(oid)
        share.getComments(oid)
        form = ShareForm(initial={'message': share.share.message, 'expiretion': share.share.getExpiretionDate, \
                                    'shareMembers': share.membersInShare, 'enable': share.share.active, \
                                    'experimenters': experimenters}) #'guests': share.guestsInShare,
        context = {'nav':request.session['nav'], 'eContext': share.eContext, 'share':share, 'form':form, 'form_active_group':form_active_group}
    elif action == 'save':
        template = "omeroweb/share_form.html"
        experimenters = list(conn.getExperimenters())
        
        form = ShareForm(initial={'experimenters':experimenters}, data=request.REQUEST.copy())
        if form.is_valid():
            message = request.REQUEST['message']
            expiretion = request.REQUEST['expiretion']
            members = request.REQUEST.getlist('members')
            #guests = request.REQUEST['guests']
            enable = request.REQUEST['enable']
            #share.updateShare(message, expiretion, members, enable)
            return HttpResponseRedirect("/%s/shares/" % (settings.WEBCLIENT_ROOT_BASE))
        else:
            share.getShare(oid)
            share.loadShareContent(oid)
            share.getComments(oid)
            context = {'nav':request.session['nav'], 'eContext': share.eContext, 'share':share, 'form':form, 'form_active_group':form_active_group}
    elif action == 'delete':
        return HttpResponseRedirect("/%s/shares/" % (settings.WEBCLIENT_ROOT_BASE))
    elif action == 'view':
        template = "omeroweb/share_details_active.html"
        share.getShare(oid)
        share.getComments(oid)
        form = ShareCommentForm()
        context = {'nav':request.session['nav'], 'eContext': share.eContext, 'share':share, 'form':form, 'form_active_group':form_active_group}
    elif action == 'comment':
        f = ShareCommentForm(data=request.REQUEST.copy())
        if f.is_valid():
            comment = request.REQUEST['comment']
            share.addComment(comment)
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
def manage_shared(request, action, share_id, **kwargs):
    request.session['nav']['menu'] = 'share'
    request.session['nav']['whos'] = 'share'
    
    template = "omeroweb/share_details_active.html"
    
    conn = None
    conn_share = None
    try:
        conn = kwargs["conn"]
        conn_share = getShareConnection(request)
    except:
        logger.error(traceback.format_exc())
    
    share = BaseShare(request.session['nav']['menu'], conn, conn_share, share_id, action)
    experimenters = conn.getExperimenters()
    share.getShareActive(share_id)
    share.getComments(share_id)
    
    context = None

    if action == 'view':
        form = ShareCommentForm()
        context = {'nav':request.session['nav'], 'eContext':share.eContext, 'share':share, 'form':form}
    elif action == 'comment':
        form = ShareCommentForm(data=request.REQUEST.copy())
        if form.is_valid():
            comment = request.REQUEST['comment']
            share.addComment(comment)
            return HttpResponseRedirect("/%s/shared/view/%s/" % (settings.WEBCLIENT_ROOT_BASE, share_id))
        else:
            form = ShareCommentForm(data=request.REQUEST.copy())
            context = {'nav':request.session['nav'], 'eContext':share.eContext, 'share':share, 'form':form}

    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def load_share_content(request, share_id, **kwargs):
    template = "omeroweb/share_content.html"
    
    conn = None
    #conn_share = None
    try:
        conn = kwargs["conn"]
        #conn_share = getShareConnection(request)
    except:
        logger.error(traceback.format_exc())
    
    share = BaseShare( request.session['nav']['menu'], conn, None, share_id)
    share.loadShareContent(share_id)
        
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
    
    if action == "todiscuss" or action == "toshare":
        template = "omeroweb/basket_share_action.html"
        
        basket = BaseBasket(conn)
        basket.load_basket(request)
        experimenters = conn.getExperimenters()
        
        form_active_group = ActiveGroupForm(initial={'activeGroup':basket.eContext['context'].groupId, 'mygroups': basket.eContext['memberOfGroups']})
        
        form = ShareForm(initial={'experimenters': experimenters})
        context = {'nav':request.session['nav'], 'basket':basket, 'form':form, 'form_active_group':form_active_group}
    elif action == "toannotate":
        # TODO
        template = "omeroweb/basket_share_action.html"
        
        basket = BaseBasket(conn)
        basket.load_basket(request)
        experimenters = conn.getExperimenters()
        
        form_active_group = ActiveGroupForm(initial={'activeGroup':basket.eContext['context'].groupId, 'mygroups': basket.eContext['memberOfGroups']})
        
        form = ShareForm(initial={'experimenters': experimenters})
        context = {'nav':request.session['nav'], 'basket':basket, 'form':form, 'form_active_group':form_active_group}
    elif action == "totag":
        # TODO
        template = "omeroweb/basket_share_action.html"
        
        basket = BaseBasket(conn)
        basket.load_basket(request)
        experimenters = conn.getExperimenters()
        
        form_active_group = ActiveGroupForm(initial={'activeGroup':basket.eContext['context'].groupId, 'mygroups': basket.eContext['memberOfGroups']})
        
        form = ShareForm(initial={'experimenters': experimenters})
        context = {'nav':request.session['nav'], 'basket':basket, 'form':form, 'form_active_group':form_active_group}
    elif action == 'dataset':
        template = "omeroweb/basket_subtree.html"
        basket = BaseBasket(conn)
        basket.loadBasketImages(oid)
        context = {'basket':basket}
    else:
        template = "omeroweb/basket.html"
        
        basket = BaseBasket(conn)
        basket.load_basket(request)
        
        form_active_group = ActiveGroupForm(initial={'activeGroup':basket.eContext['context'].groupId, 'mygroups': basket.eContext['memberOfGroups']})
        
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
    try:
        del request.session['datasetInBasket']
    except KeyError:
        logger.error(traceback.format_exc())
    try:
        del request.session['projectInBasket']
    except KeyError:
        logger.error(traceback.format_exc())
        
    request.session['imageInBasket'] = list()
    request.session['datasetInBasket'] = list()
    request.session['projectInBasket'] = list()
    request.session['nav']['basket'] = 0
    return HttpResponseRedirect("/%s/basket/" % (settings.WEBCLIENT_ROOT_BASE))

@isUserConnected
def update_basket(request, **kwargs):
    action = None
    if request.method == 'POST':
        try:
            action = request.REQUEST['action']
        except:
            raise AttributeError()
        else:
            prod = long(request.REQUEST['productId'])
            ptype = str(request.REQUEST['productType'])
            if action == 'add':
                if ptype == 'image':
                    for index, item in enumerate(request.session['imageInBasket']):
                        if item == prod:
                            raise AttributeError("This object is already in the basket")
                    request.session['imageInBasket'].append(prod)
                elif ptype == 'dataset':
                    for index, item in enumerate(request.session['datasetInBasket']):
                        if item == prod:
                            raise AttributeError("This object is already in the basket")
                    request.session['datasetInBasket'].append(prod)
                elif ptype == 'project':
                    for index, item in enumerate(request.session['projectInBasket']):
                        if item == prod:
                            raise AttributeError("This object is already in the basket")
                    request.session['projectInBasket'].append(prod)
                elif request.REQUEST['productType'] == 'share':
                    raise AttributeError()
            elif action == 'del':
                if ptype == 'image':
                    try:
                        request.session['imageInBasket'].remove(prod)
                    except:
                        raise AttributeError()
                elif ptype == 'dataset':
                    try:
                        request.session['datasetInBasket'].remove(prod)
                    except:
                        raise AttributeError()
                elif ptype == 'project':
                    try:
                        request.session['projectInBasket'].remove(prod)
                    except:
                        raise AttributeError()
                elif request.REQUEST['productType'] == 'share':
                    raise AttributeError()
    total = len(request.session['imageInBasket'])+len(request.session['datasetInBasket'])+len(request.session['projectInBasket'])
    request.session['nav']['basket'] = total
    return HttpResponse(total)

@isUserConnected
def search(request, **kwargs):
    request.session['nav']['menu'] = 'search'
    
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
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
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
    
    form_active_group = ActiveGroupForm(initial={'activeGroup':controller.eContext['context'].groupId, 'mygroups': controller.eContext['memberOfGroups']})
    
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
    
    controller = BaseImpexp(conn)

    form_active_group = ActiveGroupForm(initial={'activeGroup':controller.eContext['context'].groupId, 'mygroups': controller.eContext['memberOfGroups']})
    
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
    
    controller = BaseExperimenter(conn)
    controller.getMyDetails()
    
    eContext = dict()
    eContext['context'] = conn.getEventContext()
    eContext['user'] = conn.getUserWrapped()
    eContext['breadcrumb'] = ["My Account",  controller.experimenter.id]
    eContext['memberOfGroups'] = controller.sortAsc(list(conn.getGroupsMemberOf()), "name")
    
    if action == "save":
        form = MyAccountForm(data=request.REQUEST.copy(), initial={'groups':controller.otherGroups})
        if form.is_valid():
            firstName = str(request.REQUEST['first_name'])
            middleName = str(request.REQUEST['middle_name'])
            lastName = str(request.REQUEST['last_name'])
            email = str(request.REQUEST['email'])
            institution = str(request.REQUEST['institution'])
            defaultGroup = str(request.REQUEST['default_group'])
            try:
                if request.REQUEST['password'] is None or request.REQUEST['password'] == "":
                    password = None
                else:
                    password = str(request.REQUEST['password'])
            except:
                password = None
            controller.updateMyAccount(firstName, lastName, email, defaultGroup, middleName, institution, password)
            return HttpResponseRedirect("/%s/myaccount/details/" % (settings.WEBCLIENT_ROOT_BASE))
    else:
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
    
    form_active_group = ActiveGroupForm(initial={'activeGroup':eContext['context'].groupId, 'mygroups': eContext['memberOfGroups']})
    
    context = {'nav':request.session['nav'], 'eContext': eContext, 'form':form, 'ldapAuth': controller.ldapAuth, 'form_active_group':form_active_group}
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
    
    controller = BaseHelp(conn)
    
    form_active_group = ActiveGroupForm(initial={'activeGroup':controller.eContext['context'].groupId, 'mygroups': controller.eContext['memberOfGroups']})
    
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
    
    controller = BaseCalendar(conn=conn, year=year, month=month)
    controller.create_calendar()
    
    form_active_group = ActiveGroupForm(initial={'activeGroup':controller.eContext['context'].groupId, 'mygroups': controller.eContext['memberOfGroups']})
    
    context = {'nav':request.session['nav'], 'eContext': controller.eContext, 'controller':controller, 'form_active_group':form_active_group}
    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def history_details(request, menu, year, month, day, **kwargs):
    request.session['nav']['menu'] = 'history'
    request.session['nav']['whos'] = 'mydata'
    
    try:
        url = kwargs["url"]
    except:
        logger.error(traceback.format_exc())
        
    cal_type = None
    try:
        cal_type = request.REQUEST['history_type']
    except:
        cal_type = None
    
    template = "omeroweb/history_details.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    controller = BaseCalendar(conn=conn, year=year, month=month, day=day)
    controller.get_items(cal_type)
    
    form_active_group = ActiveGroupForm(initial={'activeGroup':controller.eContext['context'].groupId, 'mygroups': controller.eContext['memberOfGroups']})
    
    if cal_type is None:
        form_history_type = HistoryTypeForm()
    else:
        form_history_type = HistoryTypeForm(initial={'data_type':cal_type})
    
    context = {'nav':request.session['nav'], 'url':url, 'eContext': controller.eContext, 'controller':controller, 'form_active_group':form_active_group, 'form_history_type':form_history_type}
    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

####################################################################################
# Rendering

@isUserConnected
def render_thumbnail (request, iid, **kwargs):

    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    img = None
    try:
        img = conn.getImage(iid)
    except:
        try:
            conn = getShareConnection(request)
        except AttributeError:
            raise Http404("Connection not available")
        if conn is None:
            raise Http404("Connection not available")
        img = conn.getImage(iid)
    if img is None:
        raise Http404
    jpeg_data = img.getThumbnail()
    if jpeg_data is None:
        raise Http404
    rsp = HttpResponse(jpeg_data, mimetype='image/jpeg')
    return rsp

@isUserConnected
def render_thumbnail_details (request, iid, **kwargs):

    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    img = None
    try:
        img = conn.getImage(iid)
    except:
        try:
            conn = getShareConnection(request)
        except AttributeError:
            raise Http404("Connection not available")
        if conn is None:
            raise Http404("Connection not available")
        img = conn.getImage(iid)
    if img is None:
        raise Http404
    
    side = 0
    if img.getWidth() > img.getHeight():
        side = img.getWidth() 
    else:
        side = img.getHeight()
    size = 0
    if side < 300:
        size = side
    else:
        size = 300
    
    jpeg_data = img.getThumbnailByLongestSide(size=size)
    if jpeg_data is None:
        raise Http404
    rsp = HttpResponse(jpeg_data, mimetype='image/jpeg')
    return rsp

@isUserConnected
def render_thumbnail_resize (request, size, iid, **kwargs):

    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    img = None
    try:
        img = conn.getImage(iid)
    except:
        try:
            conn = getShareConnection(request)
        except AttributeError:
            raise Http404("Connection not available")
        if conn is None:
            raise Http404("Connection not available")
        img = conn.getImage(iid)
    if img is None:
        raise Http404
    jpeg_data = img.getThumbnail((int(size),int(size)))
    if jpeg_data is None:
        raise Http404
    rsp = HttpResponse(jpeg_data, mimetype='image/jpeg')
    return rsp

@isUserConnected
def render_big_thumbnail (request, iid, **kwargs):

    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    img = None
    try:
        img = conn.getImage(iid)
    except:
        try:
            conn = getShareConnection(request)
        except AttributeError:
            raise Http404("Connection not available")
        if conn is None:
            raise Http404("Connection not available")
        img = conn.getImage(iid)
    if img is None:
        raise Http404
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
    if jpeg_data is None:
        raise Http404
    rsp = HttpResponse(jpeg_data, mimetype='image/jpeg')
    return rsp

class UserAgent (object):
    def __init__ (self, request):
        self.ua = request.META['HTTP_USER_AGENT']

    def isIE (self):
        return 'MSIE' in self.ua

    def isFF (self):
        return 'Firefox' in self.ua

    def isSafari (self):
        return 'Safari' in self.ua

def _get_prepared_image (request, iid):
    r = request.REQUEST
    try:
        conn = getConnection(request)
    except AttributeError:
        raise Http404("Connection not available")
    if conn is None:
        raise Http404("Connection not available")

    img = None
    try:
        img = conn.getImage(iid)
    except:
        try:
            conn = getShareConnection(request)
        except AttributeError:
            raise Http404("Connection not available")
        if conn is None:
            raise Http404("Connection not available")
        img = conn.getImage(iid)

    if r.has_key('c'):
        logger.debug("c="+r['c'])
        channels, windows, colors =  _split_channel_info(r['c'])
        if not img.setActiveChannels(channels, windows, colors):
            logger.debug("Something bad happened while setting the active channels...")
    if r.get('m', None) == 'g':
        img.setGreyscaleRenderingModel()
    elif r.get('m', None) == 'c':
        img.setColorRenderingModel()
    compress_quality = r.get('q', None)
    return (img, compress_quality)


def render_image (request, iid, z, t):
    """ Renders the image with id {{iid}} at {{z}} and {{t}} as jpeg.
        Many options are available from the request dict.
    I am assuming a single Pixels object on image with id='iid'. May be wrong """

    r = request.REQUEST
    
    pi = _get_prepared_image(request, iid)
    if pi is None:
        raise Http404
    img, compress_quality = pi
    jpeg_data = img.renderJpeg(z,t, compression=compress_quality)
    if jpeg_data is None:
        raise Http404
    return HttpResponse(jpeg_data, mimetype='image/jpeg')


def image_viewer (request, iid, dsid=None):
    """ This view is responsible for showing pixel data as images """

    user_agent = UserAgent(request)
    rid = _get_img_details_from_req(request)
    rk = "&".join(["%s=%s" % (x[0], x[1]) for x in rid.items()])
    try:
        conn = getConnection(request)
    except AttributeError:
        raise Http404("Connection not available")
    if conn is None:
        raise Http404("Connection not available")


    img = None
    try:
        img = conn.getImage(iid)
    except:
        try:
            conn = getShareConnection(request)
        except AttributeError:
            raise Http404("Connection not available")
        if conn is None:
            raise Http404("Connection not available")
        img = conn.getImage(iid)
    
    if img is None:
        logger.debug("(a)Image %s not found..." % (str(iid)))
        raise Http404
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

def imageData_json (request, iid):
    """ Get a dict with image information """
    r = request.REQUEST
    
    try:
        conn = getConnection(request)
    except AttributeError:
        raise Http404("Connection not available")
    if conn is None:
        raise Http404("Connection not available")

    image = None
    try:
        image = conn.getImage(iid)
    except:
        try:
            conn = getShareConnection(request)
        except AttributeError:
            raise Http404("Connection not available")
        if conn is None:
            raise Http404("Connection not available")
        image = conn.getImage(iid)
    if image is None:
        raise Http404("Image not available")
    rv = {
        'id': iid,
        'size': {'width': image.getWidth(),
                 'height': image.getHeight(),
                 'z': image.z_count(),
                 't': image.t_count(),
                 'c': image.c_count(),},
        'pixel_size': {'x': image.getPixelSizeX(),
                       'y': image.getPixelSizeY(),
                       'z': image.getPixelSizeZ(),},
        'rdefs': {'model': image.isGreyscaleRenderingModel() and 'greyscale' or 'color',
                  },
        'channels': map(lambda x: {'emissionWave': x.getEmissionWave(),
                                   'color': x.getColor().getHtml(),
                                   'window': {'min': x.getWindowMin(),
                                              'max': x.getWindowMax(),
                                              'start': x.getWindowStart(),
                                              'end': x.getWindowEnd(),},
                                   'active': x.isActive()}, image.getChannels()),
        'meta': {'name': image.name or '',
                 'description': image.description or '',
                 'author': ("%s %s" % (image.details.owner.firstName, image.details.owner.lastName)),
                 #'publication': image.getPublication(),
                 #'publication_id': image.getPublicationId(),
                 'timestamp': image.getDate(),},
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
