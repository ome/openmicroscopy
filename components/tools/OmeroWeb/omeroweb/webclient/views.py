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

from time import time
from thread import start_new_thread

from omero_version import omero_version

from django.conf import settings
from django.contrib.sessions.backends.cache import SessionStore
from django.core import template_loader
from django.core.cache import cache
from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import render_to_response
from django.template import RequestContext as Context
from django.utils import simplejson
from django.views.defaults import page_not_found, server_error
from django.views import debug
from django.core.urlresolvers import reverse
from django.utils.translation import ugettext_lazy as _

from omeroweb.extlib.http import HttpJavascriptRedirect, HttpJavascriptResponse

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

from forms import ShareForm, BasketShareForm, ShareCommentForm, \
                    ContainerForm, CommentAnnotationForm, TagAnnotationForm, \
                    UploadFileForm, UsersForm, ActiveGroupForm, HistoryTypeForm, \
                    MetadataFilterForm, MetadataDetectorForm, MetadataChannelForm, \
                    MetadataEnvironmentForm, MetadataObjectiveForm, MetadataStageLabelForm, \
                    MetadataLightSourceForm, MetadataDichroicForm, MetadataMicroscopeForm, \
                    TagListForm, FileListForm, TagFilterForm, \
                    MultiAnnotationForm, \
                    WellIndexForm
from omeroweb.webadmin.forms import MyAccountForm, MyAccountLdapForm, UploadPhotoForm, LoginForm

from omeroweb.webadmin.views import _session_logout, _checkVersion
from omeroweb.webgateway.views import getBlitzConnection
from omeroweb.webgateway import views as webgateway_views

logger = logging.getLogger('views-web')

connectors = {}
share_connectors = {}

logger.info("INIT '%s'" % os.getpid())


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
        #this check the connection exist, if not it will redirect to login page
        url = request.REQUEST.get('url')
        if url is None:
            if request.META.get('QUERY_STRING'):
                url = '%s?%s' % (request.META.get('PATH_INFO'), request.META.get('QUERY_STRING'))
            else:
                url = '%s' % (request.META.get('PATH_INFO'))
        
        conn = None
        try:
            conn = getBlitzConnection(request)
        except KeyError:
            return HttpResponseRedirect(reverse("weblogin")+(("?url=%s") % (url)))
        except Exception, x:
            logger.error(traceback.format_exc())
            return HttpResponseRedirect(reverse("weblogin")+(("?error=%s&url=%s") % (str(x),url)))
        if conn is None:
            return HttpResponseRedirect(reverse("weblogin")+(("?url=%s") % (url)))
        
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
    request.session.modified = True
    if request.session.get('clipboard') is None:
        request.session['clipboard'] = []
    if request.session.get('shares') is None:
        request.session['shares'] = dict()
    if request.session.get('imageInBasket') is None:
        request.session['imageInBasket'] = set()
    #if request.session.get('datasetInBasket') is None:
    #    request.session['datasetInBasket'] = set()
    if request.session.get('nav') is None:
        if request.session.get('server') is not None:
            blitz = settings.SERVER_LIST.get(pk=request.session.get('server'))
        elif request.session.get('host') is not None:
            blitz = settings.SERVER_LIST.get(host=request.session.get('host'))
        blitz = "%s:%s" % (blitz.host, blitz.port)
        request.session['nav']={"blitz": blitz, "menu": "mydata", "view": "tree", "basket": 0, "experimenter":None}

################################################################################
# views controll

def login(request):
    request.session.modified = True
    
    if request.REQUEST.get('server'):      
        # upgrade check:
        # -------------
        # On each startup OMERO.web checks for possible server upgrades
        # and logs the upgrade url at the WARNING level. If you would
        # like to disable the checks, change the following to
        #
        #   if False:
        #
        # For more information, see
        # http://trac.openmicroscopy.org.uk/omero/wiki/UpgradeCheck
        #
        try:
            from omero.util.upgrade_check import UpgradeCheck
            check = UpgradeCheck("web")
            check.run()
            if check.isUpgradeNeeded():
                logger.error("Upgrade is available. Please visit http://trac.openmicroscopy.org.uk/omero/wiki/MilestoneDownloads.\n")
        except Exception, x:
            logger.error("Upgrade check error: %s" % x)
        
        blitz = settings.SERVER_LIST.get(pk=request.REQUEST.get('server')) 
        request.session['server'] = blitz.id
        request.session['host'] = blitz.host
        request.session['port'] = blitz.port
        request.session['username'] = request.REQUEST.get('username').encode('utf-8').strip()
        request.session['password'] = request.REQUEST.get('password').encode('utf-8').strip()
        request.session['ssl'] = request.REQUEST.get('ssl') is None and True or False
        request.session['clipboard'] = {'images': None, 'datasets': None, 'plates': None}
        request.session['shares'] = dict()
        request.session['imageInBasket'] = set()
        blitz_host = "%s:%s" % (blitz.host, blitz.port)
        request.session['nav']={"blitz": blitz_host, "menu": "start", "view": "icon", "basket": 0, "experimenter":None}
        
    error = request.REQUEST.get('error')
    
    conn = None
    try:
        conn = getBlitzConnection(request)
    except Exception, x:
        error = x.__class__.__name__
    
    if conn is not None:
        request.session['version'] = conn.getServerVersion()
        if request.REQUEST.get('noredirect'):
            return HttpResponse('OK')
        url = request.REQUEST.get("url")
        if url is not None:
            return HttpResponseRedirect(url)
        else:
            return HttpResponseRedirect(reverse("webindex"))
    else:
        if request.method == 'POST' and request.REQUEST.get('server'):
            if not _checkVersion(request.session.get('host'), request.session.get('port')):
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
                try:
                    if request.session.get('username'):
                        initial['username'] = unicode(request.session.get('username'))
                        form = LoginForm(data=initial)
                    else:                        
                        form = LoginForm(initial=initial)
                except:
                    form = LoginForm(initial=initial)
            else:
                form = LoginForm()
        if url is not None:
            context = {"version": omero_version, 'url':url, 'error':error, 'form':form}
        else:
            context = {"version": omero_version, 'error':error, 'form':form}
        
        t = template_loader.get_template(template)
        c = Context(request, context)
        rsp = t.render(c)
        return HttpResponse(rsp)

@isUserConnected
def index(request, **kwargs):
    template = "webclient/index/index.html"
    
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
    
    sessionHelper(request)
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
    
    active_group = request.REQUEST['active_group']
    if conn.changeActiveGroup(active_group):
        request.session.modified = True
        try:
            del request.session['imageInBasket']
            request.session['nav']["basket"] = 0
        except KeyError:
            logger.error(traceback.format_exc())
        return HttpResponseRedirect(url)
    else:
        error = 'You cannot change your group becuase other session is on.'
        url = reverse("webindex")+ ("?error=%s" % error)
        if request.session.get('nav')['experimenter'] is not None:
            url += "&experimenter=%s" % request.session.get('nav')['experimenter']
        return HttpResponseRedirect(url)
    
@isUserConnected
def logout(request, **kwargs):
    _session_logout(request, request.session['server'])

    try:
        for key in request.session['shares'].iterkeys():
            try:
                session_key = "S:%s#%s#%s" % (request.session.session_key,request.session['server'], key)
                _session_logout(request, request.session['server'], force_key=session_key)
            except:
                logger.error(traceback.format_exc())
    except KeyError:
        pass
    
    try:
        del request.session['shares']
    except KeyError:
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
    
    try:
        manager = BaseContainer(conn)
    except AttributeError, x:
        logger.error(traceback.format_exc())
        return handlerInternalError(x)
    
    form_users = None
    filter_user_id = None
    
    users = sortByAttr(list(conn.getColleagues()), "lastName")
    empty_label = "*%s (%s)" % (conn.getUser().getFullName(), conn.getUser().omeName)
    if len(users) > 0:
        if request.REQUEST.get('experimenter') == "":
            request.session.get('nav')['experimenter'] = None
            form_users = UsersForm(initial={'users': users, 'empty_label':empty_label, 'menu':menu})
        elif request.REQUEST.get('experimenter') is not None: 
            form_users = UsersForm(initial={'users': users, 'empty_label':empty_label, 'menu':menu}, data=request.REQUEST.copy())
            if form_users.is_valid():
                filter_user_id = request.REQUEST.get('experimenter', None)
                request.session.get('nav')['experimenter'] = filter_user_id
                form_users = UsersForm(initial={'user':filter_user_id, 'users': users, 'empty_label':empty_label, 'menu':menu})
        else:
            filter_user_id = request.session.get('nav')['experimenter']
            if filter_user_id is not None:
                form_users = UsersForm(initial={'user':filter_user_id, 'users': users, 'empty_label':empty_label, 'menu':menu})
            else:
                form_users = UsersForm(initial={'users': users, 'empty_label':empty_label, 'menu':menu})
            
    form_active_group = ActiveGroupForm(initial={'activeGroup':manager.eContext['context'].groupId, 'mygroups': manager.eContext['allGroups'], 'url':url})
    
    context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'form_active_group':form_active_group, 'form_users':form_users}
    
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
    try:
        manager= BaseContainer(conn, o1_type, o1_id, o2_type, o2_id, o3_type, o3_id)
    except AttributeError, x:
        logger.error(traceback.format_exc())
        return handlerInternalError(x)

    # prepare forms
    filter_user_id = request.session.get('nav')['experimenter']
        
    # load data  
    form_well_index = None    
    
    if o1_type is not None and o1_id > 0:
        if o1_type == 'dataset':
            manager.listImagesInDataset(o1_id, filter_user_id, page)
        elif o1_type == 'project' and o2_type == 'dataset':
            manager.listImagesInDataset(o2_id, filter_user_id, page)
        elif o1_type == 'plate':
            manager.listPlate(o1_id, index)
            form_well_index = WellIndexForm(initial={'index':index, 'range':manager.fields})
        elif o1_type == 'screen' and o2_type == 'plate':
            manager.listPlate(o2_id, index)
            form_well_index = WellIndexForm(initial={'index':index, 'range':manager.fields})
    elif o1_type == 'orphaned':
        manager.listOrphanedImages(filter_user_id)
    else:
        manager.listContainerHierarchy(filter_user_id)
    
    template = None
    if o1_type =='plate' or o2_type == 'plate':
        template = "webclient/data/plate_details.html"
    elif o1_type=='dataset' and o1_id > 0:
        if view =='icon':
            template = "webclient/data/containers_icon.html"
        elif view =='table':
            template = "webclient/data/containers_table.html"
        else:
            template = "webclient/data/container_subtree.html"      
    elif o1_type=='orphaned':
        if view =='icon':
            template = "webclient/data/containers_icon.html"
        elif view =='table':
            template = "webclient/data/containers_table.html"
        else:
            template = "webclient/data/container_subtree.html"
    elif view =='tree':
        template = "webclient/data/containers_tree.html"
    elif view =='icon':
        template = "webclient/data/containers_icon.html"
    elif view =='table':
        template = "webclient/data/containers_table.html"
    else:
        template = "webclient/data/containers.html"
    
    context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_well_index':form_well_index}
    
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
        if request.REQUEST.get('projects') is not None and request.REQUEST.get('projects').encode('utf-8') == 'on':
            onlyTypes.append('projects')
        if request.REQUEST.get('datasets') is not None and request.REQUEST.get('datasets').encode('utf-8') == 'on':
            onlyTypes.append('datasets')
        if request.REQUEST.get('images') is not None and request.REQUEST.get('images').encode('utf-8') == 'on':
            onlyTypes.append('images')
        if request.REQUEST.get('plates') is not None and request.REQUEST.get('plates').encode('utf-8') == 'on':
            onlyTypes.append('plates')
        if request.REQUEST.get('screens') is not None and request.REQUEST.get('screens').encode('utf-8') == 'on':
            onlyTypes.append('screens')
        
        date = request.REQUEST.get('dateperiodinput', None)
        if date is not None:
            date = date.encode('utf-8')
        
        manager.search(query_search, onlyTypes, date)
    else:
        template = "webclient/search/search.html"
    
    context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager}
        
    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))

@isUserConnected
def load_data_by_tag(request, o_type=None, o_id=None, **kwargs):
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
    try:
        manager= BaseContainer(conn, o_type, o_id)
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
    
    tags = [{'tag': t.textValue,'id':t.id, 'desc':t.description} for t in conn.lookupTags()]
    json_data = simplejson.dumps(tags)
    return HttpResponse(json_data, mimetype='application/javascript')

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
    
    try:
        if c_type in ("share", "discussion"):
            template = "webclient/annotations/annotations_share.html"
            manager = BaseShare(conn, conn_share, c_id)
            manager.getAllUsers(c_id)
            manager.getComments(c_id)
        else:
            if conn_share is not None:
                template = "webclient/annotations/annotations_share.html"                
                manager = BaseContainer(conn_share, c_type, c_id)
            else:
                template = "webclient/annotations/annotations.html"                
                manager = BaseContainer(conn, c_type, c_id)
                manager.annotationList()
    except AttributeError, x:
        logger.error(traceback.format_exc())
        return handlerInternalError(x)
    
    form_environment = None
    form_objective = None
    form_microscope = None
    form_stageLabel = None
    form_filters = list()
    form_detectors = list()
    form_channels = list()
    form_lasers = list()
    
    if c_type == 'well' or c_type == 'image':
        if conn_share is None:
            manager.originalMetadata()
        manager.channelMetadata()
        for ch in manager.channel_metadata:
            if ch.getLogicalChannel() is not None:
                channel = dict()
                channel['form'] = MetadataChannelForm(initial={'logicalChannel': ch.getLogicalChannel(), 
                                        'illuminations': list(conn.getEnumerationEntries("IlluminationI")), 
                                        'contrastMethods': list(conn.getEnumerationEntries("ContrastMethodI")), 
                                        'modes': list(conn.getEnumerationEntries("AcquisitionModeI"))})
                if ch.getLogicalChannel().getLightPath() is not None:
                    # one changed to many per one channel
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

                if ch.getLogicalChannel().getDetectorSettings()._obj is not None and ch.getLogicalChannel().getDetectorSettings().getDetector():
                    channel['form_detector_settings'] = MetadataDetectorForm(initial={'detectorSettings':ch.getLogicalChannel().getDetectorSettings(), 'detector': ch.getLogicalChannel().getDetectorSettings().getDetector(),
                                        'types':list(conn.getEnumerationEntries("DetectorTypeI"))})
                if ch.getLogicalChannel().getLightSourceSettings()._obj is not None and ch.getLogicalChannel().getLightSourceSettings().getLightSource() is not None:      
                    channel['form_light_source'] = MetadataLightSourceForm(initial={'lightSource': ch.getLogicalChannel().getLightSourceSettings().getLightSource(),
                                        'types':list(conn.getEnumerationEntries("FilterTypeI")), 
                                        'mediums': list(conn.getEnumerationEntries("LaserMediumI")),
                                        'pulses': list(conn.getEnumerationEntries("PulseI"))})
                if ch.getLogicalChannel().getFilterSet()._obj is not None and ch.getLogicalChannel().getFilterSet().getDichroic() is not None:
                        channel['form_dichroic'] = MetadataDichroicForm(initial={'logicalchannel': ch.getLogicalChannel().getFilterSet().getDichroic()})
                channel['name'] = ch.getEmissionWave()
                channel['color'] = ch.getColor().getHtml()
                form_channels.append(channel)
                
        try:
            image = manager.well.selectedWellSample().image()
        except:
            image = manager.image
                
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

            if len(image.getInstrument().getFilters()) > 0:
                filters = list(image.getInstrument().getFilters())    
                for f in filters:
                    form_filter = MetadataFilterForm(initial={'filter': f, 'types':list(conn.getEnumerationEntries("FilterTypeI"))})
                    form_filters.append(form_filter)
            
            if len(image.getInstrument().getDetectors()) > 0:
                detectors = list(image.getInstrument().getDetectors())    
                for d in detectors:
                    form_detector = MetadataDetectorForm(initial={'detectorSettings':None, 'detector': d, 'types':list(conn.getEnumerationEntries("DetectorTypeI"))})
                    form_detectors.append(form_detector)
        
            if len(image.getInstrument().getLightSources()) > 0:
                lasers = list(image.getInstrument().getLightSources())
                for l in lasers:
                    form_laser = MetadataLightSourceForm(initial={'lightSource': l, 
                                    'types':list(conn.getEnumerationEntries("FilterTypeI")), 
                                    'mediums': list(conn.getEnumerationEntries("LaserMediumI")),
                                    'pulses': list(conn.getEnumerationEntries("PulseI"))})
                    form_lasers.append(form_laser)
    
    if c_type in ("share", "discussion", "tag"):
        context = {'nav':request.session['nav'], 'url':url, 'eContext': manager.eContext, 'manager':manager}
    else:
        context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form_channels':form_channels, 'form_environment':form_environment, 'form_objective':form_objective, 'form_microscope':form_microscope, 'form_filters':form_filters, 'form_detectors':form_detectors, 'form_lasers':form_lasers, 'form_stageLabel':form_stageLabel}

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
    
    try:
        manager = BaseContainer(conn, o_type, o_id)
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
    
    images = len(request.REQUEST.getlist('image')) > 0 and list(conn.listSelectedImages(request.REQUEST.getlist('image'))) or list()
    datasets = len(request.REQUEST.getlist('dataset')) > 0 and list(conn.listSelectedDatasets(request.REQUEST.getlist('dataset'))) or list()
    projects = len(request.REQUEST.getlist('project')) > 0 and list(conn.listSelectedProjects(request.REQUEST.getlist('project'))) or list()
    screens = len(request.REQUEST.getlist('screen')) > 0 and list(conn.listSelectedScreens(request.REQUEST.getlist('screen'))) or list()
    plates = len(request.REQUEST.getlist('plate')) > 0 and list(conn.listSelectedPlates(request.REQUEST.getlist('plate'))) or list()
    wells = len(request.REQUEST.getlist('well')) > 0 and list(conn.listSelectedWells(request.REQUEST.getlist('well'))) or list()

    count = {'images':len(images), 'datasets':len(datasets), 'projects':len(projects), 'screens':len(screens), 'plates':len(plates), 'wells':len(wells)}
    
    form_multi = None
    if action == "annotatemany":
        selected = {'images':request.REQUEST.getlist('image'), 'datasets':request.REQUEST.getlist('dataset'), 'projects':request.REQUEST.getlist('project'), 'screens':request.REQUEST.getlist('screen'), 'plates':request.REQUEST.getlist('plate'), 'wells':request.REQUEST.getlist('well')}
        form_multi = MultiAnnotationForm(initial={'tags':manager.listTags(), 'files':manager.listFiles(), 'selected':selected, 'images':images,  'datasets':datasets, 'projects':projects, 'screens':screens, 'plates':plates, 'wells':wells})
    else:
        if request.method == 'POST':
            form_multi = MultiAnnotationForm(initial={'tags':manager.listTags(), 'files':manager.listFiles(), 'images':images, 'datasets':datasets, 'projects':projects, 'screens':screens, 'plates':plates, 'wells':wells}, data=request.REQUEST.copy(), files=request.FILES)
            if form_multi.is_valid():
                oids = {'image':request.REQUEST.getlist('image'), 'dataset':request.REQUEST.getlist('dataset'), 'project':request.REQUEST.getlist('project'), 'screen':request.REQUEST.getlist('screen'), 'plate':request.REQUEST.getlist('plate'), 'well':request.REQUEST.getlist('well')}
                
                content = request.REQUEST.get('content')
                if content is not None and content != "":
                    manager.createCommentAnnotations(content, oids)
                
                tag = request.REQUEST.get('tag')
                description = request.REQUEST.get('description')
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

                return HttpJavascriptRedirect("javascript:window.top.location.href=\'%s\'" % reverse(viewname="load_template", args=[menu]))
            
    context = {'url':url, 'nav':request.session['nav'], 'eContext':manager.eContext, 'manager':manager, 'form_multi':form_multi, 'count':count}
            
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
    
    manager = None
    if o_type == "dataset" or o_type == "project" or o_type == "image" or o_type == "screen" or o_type == "plate" or o_type == 'well':
        try:
            manager = BaseContainer(conn, o_type, o_id)
        except AttributeError, x:
            logger.error(traceback.format_exc())
            return handlerInternalError(x)
    elif o_type == "comment" or o_type == "url" or o_type == "tag":
        manager = BaseAnnotation(conn, o_type, o_id)
    elif o_type == "share" or o_type == "sharecomment":
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
    elif action == 'newtag':
        template = "webclient/annotations/annotation_new_form.html"
        form_tag = TagAnnotationForm()
        form_tags = TagListForm(initial={'tags':manager.listTags()})
        context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_tag':form_tag, 'form_tags':form_tags}
    elif action == 'newfile':
        template = "webclient/annotations/annotation_new_form.html"
        form_file = UploadFileForm()
        form_files = FileListForm(initial={'files':manager.listFiles()})
        context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_file':form_file, 'form_files':form_files}
    elif action == 'newsharecomment':
        template = "webclient/annotations/annotation_new_form.html"
        if manager.share.isExpired():
            form_sharecomments = None
        else:
            form_sharecomments = ShareCommentForm()
        context = {'nav':request.session['nav'], 'url':url, 'eContext': manager.eContext, 'manager':manager, 'form_sharecomments':form_sharecomments}  
    elif action == 'edit':
        if o_type == "dataset":
            template = "webclient/data/container_form.html"
            form = ContainerForm(initial={'name': manager.dataset.name, 'description':manager.dataset.description})
            context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form':form}
        elif o_type == "project":
            template = "webclient/data/container_form.html"
            form = ContainerForm(initial={'name': manager.project.name, 'description':manager.project.description})
            context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form':form}
        elif o_type == "screen":
            template = "webclient/data/container_form.html"
            form = ContainerForm(initial={'name': manager.screen.name, 'description':manager.screen.description})
            context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form':form}
        elif o_type == "plate":
            template = "webclient/data/container_form.html"
            form = ContainerForm(initial={'name': manager.plate.name, 'description':manager.plate.description})
            context = {'nav':request.session['nav'], 'url':url, 'eContext':manager.eContext, 'manager':manager, 'form':form}
        elif o_type =="image" and o_id > 0:
            template = "webclient/data/container_form.html"
            form = ContainerForm(initial={'name': manager.image.name, 'description':manager.image.description})
            context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form}
        elif o_type =="comment" and o_id > 0:
            template = "webclient/annotations/annotation_form.html"
            form = CommentAnnotationForm(initial={'content':manager.comment.textValue})
            context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form}
        elif o_type =="tag" and o_id > 0:
            template = "webclient/annotations/annotation_form.html"
            form = TagAnnotationForm(initial={'tag':manager.tag.textValue, 'description':manager.tag.description})
            context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form}
        elif o_type == "share" and o_id > 0:
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
            rv = "Error: %s" % x.message
            return HttpResponse(rv)
        return HttpResponseRedirect(url)
    elif action == 'removemany':
        parent = request.REQUEST.get('parent')
        if parent is not None:
            parent = parent.split('-')
            source = {'images': request.REQUEST.getlist('image'), 'datasets': request.REQUEST.getlist('dataset'), 'projects': request.REQUEST.getlist('projects'), 'plates': request.REQUEST.getlist('plate'), 'screens': request.REQUEST.getlist('screens')}
            try:
                manager.removemany(parent,source)
            except Exception, x:
                logger.error(traceback.format_exc())
                rv = "Error: %s" % x.message
                return HttpResponse(rv)
            return HttpResponse()
        else:
            raise AttributeError('Operation not supported.')
    elif action == 'removefromshare':
        image_id = request.REQUEST['source'].split('-')[1]
        try:
            manager.removeImage(image_id)
        except Exception, x:
            logger.error(traceback.format_exc())
            rv = "Error: %s" % x.message
            return HttpResponse(rv)
        return HttpResponseRedirect(url)    
    elif action == 'save':
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers", args=["edit", o_type, o_id]))
        if o_type == "dataset":
            form = ContainerForm(data=request.REQUEST.copy())
            if form.is_valid():
                name = request.REQUEST['name'].encode('utf-8')
                description = request.REQUEST['description'].encode('utf-8')
                manager.updateDataset(name, description)
                return HttpResponseRedirect(url)
            else:
                template = "webclient/data/container_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form}
        elif o_type == "project":
            form = ContainerForm(data=request.REQUEST.copy())
            if form.is_valid():
                name = request.REQUEST['name'].encode('utf-8')
                description = request.REQUEST['description'].encode('utf-8')
                manager.updateProject(name, description)
                return HttpResponseRedirect(url)
            else:
                template = "webclient/data/container_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form}
        elif o_type == "screen":
            form = ContainerForm(data=request.REQUEST.copy())
            if form.is_valid():
                name = request.REQUEST['name'].encode('utf-8')
                description = request.REQUEST['description'].encode('utf-8')
                manager.updateScreen(name, description)
                return HttpResponseRedirect(url)
            else:
                template = "webclient/data/container_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form}
        elif o_type == "plate":
            form = ContainerForm(data=request.REQUEST.copy())
            if form.is_valid():
                name = request.REQUEST['name'].encode('utf-8')
                description = request.REQUEST['description'].encode('utf-8')
                manager.updatePlate(name, description)
                return HttpResponseRedirect(url)
            else:
                template = "webclient/data/container_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form}
        elif o_type == 'image':
            form = ContainerForm(data=request.REQUEST.copy())
            if form.is_valid():
                name = request.REQUEST['name'].encode('utf-8')
                description = request.REQUEST['description'].encode('utf-8')
                manager.updateImage(name, description)
                return HttpResponseRedirect(url)
            else:
                template = "webclient/data/container_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form}
        elif o_type == 'comment':
            form = CommentAnnotationForm(data=request.REQUEST.copy())
            if form.is_valid():
                content = request.REQUEST['content'].encode('utf-8')
                manager.saveCommentAnnotation(content)
                return HttpResponseRedirect(url)
            else:
                template = "webclient/data/container_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form}
        elif o_type == 'tag':
            form = TagAnnotationForm(data=request.REQUEST.copy())
            if form.is_valid():
                content = request.REQUEST['tag'].encode('utf-8')
                description = request.REQUEST['description'].encode('utf-8')
                manager.saveTagAnnotation(content, description)
                return HttpResponseRedirect(url)
            else:
                template = "webclient/data/container_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form':form}
        elif o_type == "share":
            experimenters = list(conn.getExperimenters())            
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
                manager.updateShareOrDiscussion(host, request.session['server'], message, members, enable, expiration)
                return HttpResponseRedirect(url)
            else:
                template = "webclient/public/share_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'eContext': manager.eContext, 'share':manager, 'form':form}
        elif o_type == "sharecomment":
            form_sharecomments = ShareCommentForm(data=request.REQUEST.copy())
            if form_sharecomments.is_valid():
                comment = request.REQUEST['comment'].encode('utf-8')
                try:
                    host = '%s%s' % (settings.APPLICATION_HOST, reverse("webindex"))
                except:
                    host = '%s://%s:%s%s' % (request.META['wsgi.url_scheme'], request.META['SERVER_NAME'], request.META['SERVER_PORT'], reverse("webindex"))
                manager.addComment(host, request.session['server'], comment)
                return HttpResponseRedirect(url)
            else:
                template = "webclient/annotations/annotation_new_form.html"
                context = {'nav':request.session['nav'], 'url':url, 'eContext': manager.eContext, 'manager':manager, 'form_sharecomments':form_sharecomments}      
    elif action == 'addnew':
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers", args=["action", "new"]))
        if o_type == "project" and o_id > 0:
            form = ContainerForm(data=request.REQUEST.copy())
            if form.is_valid():
                name = request.REQUEST.get('name').encode('utf-8')
                description = request.REQUEST.get('description').encode('utf-8')
                manager.createDataset(name, description)
                return HttpJavascriptRedirect("javascript:window.top.location.href=\'%s\'" % reverse(viewname="load_template", args=[menu])) 
            else:
                template = "webclient/data/container_new.html"
                context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'form':form}
        else:
            if request.REQUEST.get('folder_type') == "dataset":
                form = ContainerForm(data=request.REQUEST.copy())
                if form.is_valid():
                    name = request.REQUEST['name'].encode('utf-8')
                    description = request.REQUEST['description'].encode('utf-8')
                    manager.createDataset(name, description)
                    return HttpJavascriptRedirect("javascript:window.top.location.href=\'%s\'" % reverse(viewname="load_template", args=[menu])) 
                else:
                    template = "webclient/data/container_new.html"
                    context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'form':form}
            elif request.REQUEST.get('folder_type') == "project":
                form = ContainerForm(data=request.REQUEST.copy())
                if form.is_valid():
                    name = request.REQUEST['name'].encode('utf-8')
                    description = request.REQUEST['description'].encode('utf-8')
                    manager.createProject(name, description)
                    return HttpJavascriptRedirect("javascript:window.top.location.href=\'%s\'" % reverse(viewname="load_template", args=[menu])) 
                else:
                    template = "webclient/data/container_new.html"
                    context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'form':form}
            elif request.REQUEST.get('folder_type') == "screen":
                form = ContainerForm(data=request.REQUEST.copy())
                if form.is_valid():
                    name = request.REQUEST['name'].encode('utf-8')
                    description = request.REQUEST['description'].encode('utf-8')
                    manager.createScreen(name, description)
                    return HttpJavascriptRedirect("javascript:window.top.location.href=\'%s\'" % reverse(viewname="load_template", args=[menu])) 
                else:
                    template = "webclient/data/container_new.html"
                    context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'form':form}
    elif action == 'addcomment':
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers", args=["newcomment", o_type, oid]))
        form_comment = CommentAnnotationForm(data=request.REQUEST.copy())
        if form_comment.is_valid() and o_type is not None and o_id > 0:
            content = request.REQUEST['content'].encode('utf-8')            
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
            tag = request.REQUEST['tag'].encode('utf-8')
            desc = request.REQUEST['description'].encode('utf-8')
            manager.createTagAnnotation(o_type, tag, desc)
            return HttpResponseRedirect(url)
        else:
            template = "webclient/annotations/annotation_new_form.html"
            context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_tag':form_tag}
    elif action == 'usetag':
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers", args=["usetag", o_type, oid]))
        tag_list = manager.listTags()
        form_tags = TagListForm(data=request.REQUEST.copy(), initial={'tags':tag_list})
        if form_tags.is_valid() and o_type is not None and o_id > 0:
            tags = request.POST.getlist('tags')
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
        file_list = manager.listFiles()
        form_files = FileListForm(data=request.REQUEST.copy(), initial={'files':file_list})
        if form_files.is_valid() and o_type is not None and o_id > 0:
            files = request.POST.getlist('files')
            manager.createAnnotationLinks(o_type, 'file', files)    
            return HttpResponseRedirect(url)
        else:
            template = "webclient/annotations/annotation_new_form.html"
            context = {'nav':request.session['nav'], 'url':url, 'manager':manager, 'eContext':manager.eContext, 'form_files':form_files}
    elif action == 'delete':
        allitems = request.REQUEST.get('all')
        try:
            manager.deleteItem(allitems)
        except Exception, x:
            logger.error(traceback.format_exc())
            rv = "Error: %s" % x.message
            return HttpResponse(rv)
        return HttpResponse()
    elif action == 'deletemany':
        ids = request.REQUEST.getlist('image')
        try:
            manager.deleteImages(ids)
        except Exception, x:
            logger.error(traceback.format_exc())
            rv = "Error: %s" % x.message
            return HttpResponse(rv)
        return HttpResponse()
    
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
def load_public(request, share_id=None, **kwargs):
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
            images = [long(i) for i in request.REQUEST.getlist('image')]
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
            share = BaseShare(conn)
            share.createShare(host, request.session['server'], images, message, members, enable, expiration)
            return HttpJavascriptRedirect("javascript:window.top.location.href=\'%s\'" % reverse("load_template", args=["public"])) 
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
            share = BaseShare(conn)
            share.createDiscussion(host, request.session['server'], message, members, enable, expiration)
            return HttpJavascriptRedirect("javascript:window.top.location.href=\'%s\'" % reverse("load_public")) 
        else:
            template = "webclient/basket/basket_discussion_action.html"
            context = {'nav':request.session['nav'], 'eContext':basket.eContext, 'form':form}
    elif action == "toannotate":
        # TODO
        template = "webclient/basket/basket_share_action.html"
        
        basket = BaseBasket(conn)
        basket.load_basket(request)
        experimenters = conn.getExperimenters()
        
        form = ShareForm(initial={'experimenters': experimenters})
        form_active_group = ActiveGroupForm(initial={'activeGroup':basket.eContext['context'].groupId, 'mygroups': basket.eContext['allGroups'], 'url':url})
        context = {'nav':request.session['nav'], 'eContext':basket.eContext, 'basket':basket, 'form':form, 'form_active_group':form_active_group}
    elif action == "totag":
        # TODO
        template = "webclient/basket/basket_share_action.html"
        
        basket = BaseBasket(conn)
        basket.load_basket(request)
        experimenters = conn.getExperimenters()
        
        form = ShareForm(initial={'experimenters': experimenters})
        form_active_group = ActiveGroupForm(initial={'activeGroup':basket.eContext['context'].groupId, 'mygroups': basket.eContext['allGroups'], 'url':url})
        context = {'nav':request.session['nav'], 'eContext':basket.eContext, 'basket':basket, 'form':form, 'form_active_group':form_active_group}
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
                if ptype == 'image':
                    for item in request.session['imageInBasket']:
                        if item == long(prod):
                            rv = "Error: This object is already in the basket"
                            return HttpResponse(rv)
                    request.session['imageInBasket'].add(long(prod))
                #elif ptype == 'dataset':
                #    for item in request.session['datasetInBasket']:
                #        if item == prod:
                #            rv = "Error: This object is already in the basket"
                #            return HttpResponse(rv)
                #    request.session['datasetInBasket'].append(prod)
                else:
                    rv = "Error: This action is not available"
                    return HttpResponse(rv)
            elif action == 'addmany':
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

##################################################################
# Clipboard

@isUserConnected
def update_clipboard(request, **kwargs):
    action = None
    rv = None
    if request.method == 'POST':
        action = request.REQUEST.get('action')
        if action is None:
            rv = "Error: Action not available."
        else:
            if action == 'copy':
                images = request.REQUEST.getlist('image')
                datasets = request.REQUEST.getlist('dataset')
                plates = request.REQUEST.getlist('plate')
                request.session['clipboard'] = {'images': images, 'datasets': datasets, 'plates': plates}
                rv = "Data were copied to clipboard."
            elif action == 'paste':
                destinationType = request.REQUEST.get('destinationType')
                destinationId = request.REQUEST.get('destinationId')
                if destinationType is not None and destinationId is not None:
                    destination = [str(request.REQUEST.get('destinationType')), long(request.REQUEST.get('destinationId'))]
                    conn = None
                    try:
                        conn = kwargs["conn"]
                    except:
                        logger.error(traceback.format_exc())
                    manager = BaseContainer(conn)
                    if request.session['clipboard']['images'] is not None and len(request.session['clipboard']['images']) > 0 :
                        try:
                            manager.copyImagesToDataset(request.session['clipboard']['images'], destination)
                        except Exception, x:
                            logger.error(traceback.format_exc())
                            return HttpResponse("Error: %s" % (x.__class__.__name__))
                    elif request.session['clipboard']['datasets'] is not None and len(request.session['clipboard']['datasets']) > 0:
                        try:
                            manager.copyDatasetsToProject(request.session['clipboard']['datasets'], destination)
                        except Exception, x:
                            logger.error(traceback.format_exc())
                            return HttpResponse("Error: %s" % (x.__class__.__name__))
                    elif request.session['clipboard']['plates'] is not None and len(request.session['clipboard']['plates']) > 0:
                        try:
                            manager.copyPlatesToScreen(request.session['clipboard']['plates'], destination)
                        except Exception, x:
                            logger.error(traceback.format_exc())
                            return HttpResponse("Error: %s" % (x.__class__.__name__)) 
                    else:
                        rv = "Error: Clipboard is empty. You need to copy before paste."

                    if rv is None:
                        request.session['clipboard'] = {'images': None, 'datasets': None, 'plates': None}
                        rv = "Copied successful"
                else:
                    rv = "Error: Destination was not specified."    
            elif action == 'clean':
                try:
                    del request.session['clipboard']
                except KeyError:
                    logger.error(traceback.format_exc())
                request.session['clipboard'] = {'images': None, 'datasets': None, 'plates': None}
                rv = "Cleapboard is empty"
            else:
                rv = "Error: Action not supported."
    else:
        rv = "Error: Action not available."
    return HttpResponse(rv)



@isUserConnected
def importer(request, **kwargs):
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
        
    controller = BaseImpexp(conn)
    
    form_active_group = ActiveGroupForm(initial={'activeGroup':controller.eContext['context'].groupId, 'mygroups': controller.eContext['allGroups'], 'url':url})
    
    context = {'sid':request.session['server'], 'uuid':conn._sessionUuid, 'nav':request.session['nav'], 'eContext': controller.eContext, 'controller':controller, 'form_active_group':form_active_group}
    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
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

    form_active_group = ActiveGroupForm(initial={'activeGroup':eContext['context'].groupId, 'mygroups': eContext['allGroups'], 'url':url})
    
    context = {'nav':request.session['nav'], 'eContext': eContext, 'controller':controller, 'form':form, 'ldapAuth': controller.ldapAuth, 'form_active_group':form_active_group}
    t = template_loader.get_template(template)
    c = Context(request,context)
    logger.debug('TEMPLATE: '+template)
    return HttpResponse(t.render(c))

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
        x1 = long(request.REQUEST['x1'].encode('utf-8'))
        x2 = long(request.REQUEST['x2'].encode('utf-8'))
        y1 = long(request.REQUEST['y1'].encode('utf-8'))
        y2 = long(request.REQUEST['y2'].encode('utf-8'))
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
    
    controller = BaseCalendar(conn=conn, year=year, month=month, day=day)
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
    img = conn.getImage(iid)
    
    if img is None:
        jpeg_data = conn.defaultThumbnail(size=int(size))
        logger.error("Image %s not found..." % (str(iid)))
        #return handlerInternalError("Image %s not found..." % (str(iid)))
    else:
        jpeg_data = img.getThumbnailOrDefault(size=int(size))
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
    
    size = 0
    if img.getWidth() >= 200:
        size = img.getWidth()
    else:
        size = 200
    
    if size <=750:
        size = size
    else:
        size = 750
        
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
        return HttpJavascriptResponse(r_text)

