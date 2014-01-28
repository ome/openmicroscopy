#!/usr/bin/env python
# -*- coding: utf-8 -*-
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

from time import time
from thread import start_new_thread

from omero_version import omero_version
import omero, omero.scripts 
from omero.rtypes import *

from django.conf import settings
from django.contrib.sessions.backends.cache import SessionStore
from django.core import template_loader
from django.core.cache import cache
from django.http import Http404, HttpResponse, HttpResponseRedirect, HttpResponseServerError, HttpResponseForbidden
from django.shortcuts import render_to_response
from django.template import RequestContext as Context
from django.utils import simplejson
from django.utils.http import urlencode
from django.views.defaults import page_not_found, server_error
from django.views import debug
from django.core.urlresolvers import reverse
from django.utils.translation import ugettext_lazy as _
from django.utils.encoding import smart_str
from django.core.servers.basehttp import FileWrapper

from webclient.webclient_gateway import OmeroWebGateway

from webclient_http import HttpJavascriptRedirect, HttpJavascriptResponse, HttpLoginRedirect

from webclient_utils import _formatReport, _purgeCallback
from forms import ShareForm, BasketShareForm, \
                    ContainerForm, ContainerNameForm, ContainerDescriptionForm, \
                    CommentAnnotationForm, TagsAnnotationForm, \
                    UsersForm, ActiveGroupForm, \
                    MetadataFilterForm, MetadataDetectorForm, MetadataChannelForm, \
                    MetadataEnvironmentForm, MetadataObjectiveForm, MetadataObjectiveSettingsForm, MetadataStageLabelForm, \
                    MetadataLightSourceForm, MetadataDichroicForm, MetadataMicroscopeForm, \
                    FilesAnnotationForm, WellIndexForm

from controller import BaseController
from controller.index import BaseIndex
from controller.basket import BaseBasket
from controller.container import BaseContainer
from controller.help import BaseHelp
from controller.history import BaseCalendar
from controller.impexp import BaseImpexp
from controller.search import BaseSearch
from controller.share import BaseShare

from omeroweb.connector import Server

from omeroweb.webadmin.forms import LoginForm
from omeroweb.webadmin.webadmin_utils import toBoolean, upgradeCheck

from omeroweb.webgateway import views as webgateway_views

from omeroweb.feedback.views import handlerInternalError

from omeroweb.webclient.decorators import login_required
from omeroweb.webclient.decorators import render_response
from omeroweb.connector import Connector
from omeroweb.decorators import ConnCleaningHttpResponse

logger = logging.getLogger(__name__)

logger.info("INIT '%s'" % os.getpid())

# helper method
def getIntOrDefault(request, name, default):
    try:
        index = int(request.REQUEST.get(name, default))
    except ValueError:
        index = 0
    return index

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
    
    conn = None
    error = None
    
    server_id = request.REQUEST.get('server')
    form = LoginForm(data=request.REQUEST.copy())
    useragent = 'OMERO.web'
    if form.is_valid():
        username = form.cleaned_data['username']
        password = form.cleaned_data['password']
        server_id = form.cleaned_data['server']
        is_secure = toBoolean(form.cleaned_data['ssl'])
        
        connector = Connector(server_id, is_secure)
        
        # TODO: version check should be done on the low level, see #5983
        if server_id is not None and username is not None and password is not None \
                and connector.check_version(useragent):
            conn = connector.create_connection(useragent, username, password)
            if conn is not None:
                # Check if user is in "user" group
                userGroupId = conn.getAdminService().getSecurityRoles().userGroupId
                if userGroupId in conn.getEventContext().memberOfGroups:
                    request.session['connector'] = connector
                    upgradeCheck()

                    # if 'active_group' remains in session from previous login, check it's valid for this user
                    if request.session.get('active_group'):
                        if request.session.get('active_group') not in conn.getEventContext().memberOfGroups:
                            del request.session['active_group']
                    if request.session.get('user_id'):  # always want to revert to logged-in user
                        del request.session['user_id']
                    # do we ned to display server version ?
                    # server_version = conn.getServerVersion()
                    if request.REQUEST.get('noredirect'):
                        return HttpResponse('OK')
                    url = request.REQUEST.get("url")
                    if url is not None and len(url) != 0:
                        return HttpResponseRedirect(url)
                    else:
                        return HttpResponseRedirect(reverse("webindex"))
                elif username == "guest":
                    error = "Guest account is for internal OMERO use only. Not for login."
                else:
                    error = "This user is not active."

    
    if request.method == 'POST' and server_id is not None:
        connector = Connector(server_id, True)
        if not connector.is_server_up(useragent):
            error = "Server is not responding, please contact administrator."
        elif not connector.check_version(useragent):
            error = "Client version does not match server, please contact administrator."
        else:
            error = "Connection not available, please check your user name and password."
    url = request.REQUEST.get("url")
    
    template = "webclient/login.html"
    if request.method != 'POST':
        if server_id is not None:
            initial = {'server': unicode(server_id)}
            form = LoginForm(initial=initial)
        else:
            form = LoginForm()
        
    context = {"version": omero_version, 'error':error, 'form':form}
    if url is not None and len(url) != 0:
        context['url'] = urlencode({'url':url})
    
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@login_required(ignore_login_fail=True)
def keepalive_ping(request, conn=None, **kwargs):
    """ Keeps the OMERO session alive by pinging the server """

    # login_required handles ping, timeout etc, so we don't need to do anything else
    return HttpResponse("OK")

@login_required()
@render_response()
def feed(request, conn=None, **kwargs):
    """
    Viewing this page doesn't perform any action. All we do here is assemble various data for display.
    Last imports, tag cloud etc are retrived via separate AJAX calls.
    """
    template = "webclient/index/index.html"
    
    controller = BaseIndex(conn)
    
    context = {'controller':controller}
    context['template'] = template
    return context


@login_required()
@render_response()
def index_last_imports(request, conn=None, **kwargs):
    """
    Gets the most recent imports - Used in an AJAX call by home page.
    """
    
    controller = BaseIndex(conn)
    controller.loadLastAcquisitions()
    
    context = {'controller':controller}
    context['template'] = "webclient/index/index_last_imports.html"
    return context

@login_required()
@render_response()
def index_most_recent(request, conn=None, **kwargs):
    """ Gets the most recent 'shares' and 'share' comments. Used by the homepage via AJAX call """

    controller = BaseIndex(conn)
    controller.loadMostRecent()
    
    context = {'controller':controller}
    context['template'] = "webclient/index/index_most_recent.html"
    return context

@login_required()
@render_response()
def index_tag_cloud(request, conn=None, **kwargs):
    """ Gets the most used Tags. Used by the homepage via AJAX call """
    
    controller = BaseIndex(conn)
    controller.loadTagCloud()
    
    context = {'controller':controller }
    context['template'] = "webclient/index/index_tag_cloud.html"
    return context

@login_required()
def change_active_group(request, conn=None, url=None, **kwargs):
    """
    Simply changes the request.session['active_group'] which is then used by the 
    @login_required decorator to configure conn for any group-based queries.
    Finally this redirects to the 'url'.
    """
    switch_active_group(request)
    url = url or reverse("webindex")
    return HttpResponseRedirect(url)

def switch_active_group(request, active_group=None):
    """
    Simply changes the request.session['active_group'] which is then used by the 
    @login_required decorator to configure conn for any group-based queries.
    """
    if active_group is None:
        active_group = request.REQUEST.get('active_group')
    active_group = int(active_group)
    if 'active_group' not in request.session or active_group != request.session['active_group']:
        request.session.modified = True
        request.session['active_group'] = active_group
        request.session['imageInBasket'] = set()        # empty basket
        request.session['basket_counter'] = 0

@login_required(login_redirect='webindex')
def logout(request, conn=None, **kwargs):
    """ Logout of the session and redirects to the homepage (will redirect to login first) """

    if request.session.get('active_group') is not None:
        try:
            conn.setDefaultGroup(request.session.get('active_group'))
        except:
            logger.error('Exception during logout.', exc_info=True)
    try:
        try:
            conn.seppuku()
        except:
            logger.error('Exception during logout.', exc_info=True)
    finally:
        request.session.flush()
    return HttpResponseRedirect(reverse("webindex"))


###########################################################################
@login_required()
@render_response()
def load_template(request, menu, conn=None, url=None, **kwargs):
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

    #tree support
    init = {'initially_open':None, 'initially_select': []}
    first_sel = None
    initially_open_owner = None
    # E.g. backwards compatible support for path=project=51|dataset=502|image=607 (select the image)
    path = request.REQUEST.get('path', '')
    i = path.split("|")[-1]
    if i.split("=")[0] in ('project', 'dataset', 'image', 'screen', 'plate', 'tag'):
        init['initially_select'].append(str(i).replace("=",'-'))  # Backwards compatible with image=607 etc
    # Now we support show=image-607|image-123  (multi-objects selected)
    show = request.REQUEST.get('show', '')
    for i in show.split("|"):
        if i.split("-")[0] in ('project', 'dataset', 'image', 'screen', 'plate', 'tag', 'acquisition', 'run', 'well'):
            i = i.replace('run', 'acquisition')   # alternatives for 'acquisition'
            init['initially_select'].append(str(i))
    if len(init['initially_select']) > 0:
        # tree hierarchy open to first selected object
        init['initially_open'] = [ init['initially_select'][0] ]
        first_obj, first_id = init['initially_open'][0].split("-",1)
        # if we're showing a tag, make sure we're on the tags page...
        if first_obj == "tag" and menu != "usertags":
            return HttpResponseRedirect(reverse(viewname="load_template", args=['usertags']) + "?show=" + init['initially_select'][0])
        try:
            conn.SERVICE_OPTS.setOmeroGroup('-1')   # set context to 'cross-group'
            if first_obj == "tag":
                first_sel = conn.getObject("TagAnnotation", long(first_id))
            else:
                first_sel = conn.getObject(first_obj, long(first_id))
                initially_open_owner = first_sel.details.owner.id.val
                # Wells aren't in the tree, so we need parent...
                if first_obj == "well":
                    parentNode = first_sel.getWellSample().getPlateAcquisition()
                    ptype = "acquisition"
                    if parentNode is None:      # No Acquisition for this well...
                        parentNode = first_sel.getParent()  #...use Plate instead
                        ptype = "plate"
                    first_sel = parentNode
                    init['initially_open'] = ["%s-%s" % (ptype, parentNode.getId())]
                    init['initially_select'] = init['initially_open'][:]
        except:
            pass    # invalid id
        if first_obj not in ("project", "screen"):
            # need to see if first item has parents
            if first_sel is not None:
                for p in first_sel.getAncestry():
                    if first_obj == "tag":  # parents of tags must be tags (no OMERO_CLASS)
                        init['initially_open'].insert(0, "tag-%s" % p.getId())
                    else:
                        init['initially_open'].insert(0, "%s-%s" % (p.OMERO_CLASS.lower(), p.getId()))
                        initially_open_owner = p.details.owner.id.val
                if init['initially_open'][0].split("-")[0] == 'image':
                    init['initially_open'].insert(0, "orphaned-0")
    # need to be sure that tree will be correct omero.group
    if first_sel is not None:
        switch_active_group(request, first_sel.details.group.id.val)

    # search support
    if menu == "search" and request.REQUEST.get('search_query'):
        init['query'] = str(request.REQUEST.get('search_query').encode('utf-8', 'replace'))

    # get url without request string - used to refresh page after switch user/group etc
    url = reverse(viewname="load_template", args=[menu])

    manager = BaseContainer(conn)

    # validate experimenter is in the active group
    active_group = request.session.get('active_group') or conn.getEventContext().groupId
    # prepare members of group...
    s = conn.groupSummary(active_group)
    leaders = s["leaders"]
    members = s["colleagues"]
    userIds = [u.id for u in leaders]
    userIds.extend( [u.id for u in members] )
    users = []
    if len(leaders) > 0:
        users.append( ("Owners", leaders) )
    if len(members) > 0:
        users.append( ("Members", members) )
    users = tuple(users)

    # check any change in experimenter...
    user_id = request.REQUEST.get('experimenter')
    if initially_open_owner is not None:
        if (request.session.get('user_id', None) != -1): # if we're not already showing 'All Members'...
            user_id = initially_open_owner
    try:
        user_id = long(user_id)
    except:
        user_id = None
    if user_id is not None:
        form_users = UsersForm(initial={'users': users, 'empty_label':None, 'menu':menu}, data=request.REQUEST.copy())
        if not form_users.is_valid():
            if user_id != -1:           # All users in group is allowed
                user_id = None
    if user_id is None:
        # ... or check that current user is valid in active group
        user_id = request.session.get('user_id', None)
        if user_id is None or int(user_id) not in userIds:
            if user_id != -1:           # All users in group is allowed
                user_id = conn.getEventContext().userId

    request.session['user_id'] = user_id

    if conn.isAdmin():  # Admin can see all groups
        myGroups = [g for g in conn.getObjects("ExperimenterGroup") if g.getName() not in ("user", "guest")]
    else:
        myGroups = list(conn.getGroupsMemberOf())
    myGroups.sort(key=lambda x: x.getName().lower())
    new_container_form = ContainerForm()

    context = {'init':init, 'myGroups':myGroups, 'new_container_form':new_container_form}
    context['groups'] = myGroups
    context['active_group'] = conn.getObject("ExperimenterGroup", long(active_group))
    for g in context['groups']:
        g.groupSummary()    # load leaders / members
    context['active_user'] = conn.getObject("Experimenter", long(user_id))
    
    context['isLeader'] = conn.isLeader()
    context['current_url'] = url
    context['template'] = template
    return context


@login_required(setGroupContext=True)
@render_response()
def load_data(request, o1_type=None, o1_id=None, o2_type=None, o2_id=None, o3_type=None, o3_id=None, conn=None, **kwargs):
    """
    This loads data for the tree, via AJAX calls. 
    The template is specified by query string. E.g. icon, table, tree.
    By default this loads Projects and Datasets.
    E.g. /load_data?view=tree provides data for the tree as <li>.
    """
    
    # get page 
    page = getIntOrDefault(request, 'page', 1)
    
    # get view 
    view = str(request.REQUEST.get('view', None))

    # get index of the plate
    index = getIntOrDefault(request, 'index', 0)

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
        manager= BaseContainer(conn, **kw)
    except AttributeError, x:
        return handlerInternalError(request, x)
    
    # prepare forms
    filter_user_id = request.session.get('user_id')
    form_well_index = None
        
    context = {'manager':manager, 'form_well_index':form_well_index, 'index':index}

    # load data & template
    template = None
    if kw.has_key('orphaned'):
        manager.listOrphanedImages(filter_user_id, page)
        if view =='icon':
            template = "webclient/data/containers_icon.html"
        else:
            template = "webclient/data/container_subtree.html"
    elif len(kw.keys()) > 0 :
        if kw.has_key('dataset'):
            load_pixels = (view == 'icon')  # we need the sizeX and sizeY for these
            filter_user_id = None           # Show images belonging to all users
            manager.listImagesInDataset(kw.get('dataset'), filter_user_id, page, load_pixels=load_pixels)
            if view =='icon':
                template = "webclient/data/containers_icon.html"
            else:
                template = "webclient/data/container_subtree.html"
        elif kw.has_key('plate') or kw.has_key('acquisition'):
            if view == 'tree':  # Only used when pasting Plate into Screen - load Acquisition in tree
                template = "webclient/data/container_subtree.html"
            else:
                fields = manager.getNumberOfFields()
                if fields is not None:
                    form_well_index = WellIndexForm(initial={'index':index, 'range':fields})
                    if index == 0:
                        index = fields[0]
                show = request.REQUEST.get('show', None)
                if show is not None:
                    select_wells = [w.split("-")[1] for w in show.split("|") if w.startswith("well-")]
                    context['select_wells'] = ",".join(select_wells)
                context['baseurl'] = reverse('webgateway').rstrip('/')
                context['form_well_index'] = form_well_index
                template = "webclient/data/plate.html"
    else:
        manager.listContainerHierarchy(filter_user_id)
        if view =='tree':
            template = "webclient/data/containers_tree.html"
        elif view =='icon':
            template = "webclient/data/containers_icon.html"
        else:
            template = "webclient/data/containers.html"

    context['template_view'] = view
    context['isLeader'] = conn.isLeader()
    context['template'] = template
    return context


@login_required(setGroupContext=True)
@render_response()
def load_chgrp_target(request, group_id, target_type, conn=None, **kwargs):
    """ Loads a tree for user to pick target Project, Dataset or Screen """

    # filter by group (not switching group)
    conn.SERVICE_OPTS.setOmeroGroup(int(group_id))

    manager= BaseContainer(conn)
    manager.listContainerHierarchy()
    template = 'webclient/data/chgrp_target_tree.html'
    
    show_projects = target_type in ('project', 'dataset')
    context = {'manager': manager, 'target_type': target_type, 'show_projects':show_projects, 'template': template}
    return context

@login_required(setGroupContext=True)
@render_response()
def load_searching(request, form=None, conn=None, **kwargs):
    """
    Handles AJAX calls to search 
    """
    
    manager = BaseSearch(conn)
    # form = 'form' if we are searching. Get query from request...
    if form is not None: 
        query_search = request.REQUEST.get('query').replace("+", " ")
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

        startdate = request.REQUEST.get('startdateinput', None)
        startdate = startdate is not None and smart_str(startdate) or None
        enddate = request.REQUEST.get('enddateinput', None)
        enddate = enddate is not None and smart_str(enddate) or None
        date = None
        if startdate is not None:
            if enddate is None:
                enddate = startdate
            date = "%s_%s" % (startdate, enddate)

        # by default, if user has not specified any types:
        if len(onlyTypes) == 0:
            onlyTypes = ['images']

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
    
    context = {'manager':manager}
    context['template'] = template
    return context


@login_required(setGroupContext=True)
@render_response()
def load_data_by_tag(request, o_type=None, o_id=None, conn=None, **kwargs):
    """ 
    Loads data for the tag tree and center panel.
    Either get the P/D/I etc under tags, or the images etc under a tagged Dataset or Project.
    @param o_type       'tag' or 'project', 'dataset'.
    """
    
    if request.REQUEST.get("o_type") is not None and len(request.REQUEST.get("o_type")) > 0:
        o_type = request.REQUEST.get("o_type")
        try:
            o_id = long(request.REQUEST.get("o_id"))
        except:
            pass
            
    # check view
    view = request.REQUEST.get("view")
    
    # the index of a field within a well
    index = getIntOrDefault(request, 'index', 0)

    
    # prepare forms
    filter_user_id = request.session.get('user_id')
    
    # prepare data
    kw = dict()
    if o_type is not None and o_id > 0:
        kw[str(o_type)] = long(o_id)
    
    try:
        manager= BaseContainer(conn, **kw)
    except AttributeError, x:
        return handlerInternalError(request, x)
    
    if o_id is not None:
        if o_type == "tag":
            manager.loadDataByTag()
            if view == "tree":
                template = "webclient/data/container_tags_containers.html"
            elif view == "icon":
                template = "webclient/data/containers_icon.html"
            
        elif o_type == "dataset":
            manager.listImagesInDataset(o_id, filter_user_id)
            template = "webclient/data/container_tags_subtree.html"
    else:
        manager.loadTags(filter_user_id)
        template = "webclient/data/container_tags_tree.html"    
    # load data  
    form_well_index = None    
    
    
    context = {'manager':manager}
    context['template_view'] = view
    context['isLeader'] = conn.isLeader()
    context['template'] = template
    return context


@login_required()
@render_response()
def open_astex_viewer(request, obj_type, obj_id, conn=None, **kwargs):
    """
    Opens the Open Astex Viewer applet, to display volume masks in a couple of formats:
    - mrc.map files that are attached to images. obj_type = 'file'
    - Convert OMERO image to mrc on the fly. obj_type = 'image_8bit' or 'image'
        In this case, we may use 'scipy' to scale the image volume. 
    """
    
    # can only populate these for 'image'
    image = None
    data_storage_mode = ""
    pixelRange = None       # (min, max) values of the raw data
    contourSliderInit, contourSliderIncr = None, None
    sizeOptions = None  # only give user choice if we need to scale down (and we CAN scale with scipy)
    # If we convert to 8bit map, subtract dataOffset, multiply by mapPixelFactor add mapOffset. (used for js contour controls)
    if obj_type == 'file':
        ann = conn.getObject("Annotation", obj_id)
        if ann is None:
            return handlerInternalError(request, "Can't find file Annotation ID %s as data source for Open Astex Viewer." % obj_id)
        # determine mapType by name
        imageName = ann.getFileName()
        if imageName.endswith(".bit"):
            data_url = reverse("open_astex_bit", args=[obj_id])
        else:
            data_url = reverse("open_astex_map", args=[obj_id])

    elif obj_type in ('image', 'image_8bit'):
        image = conn.getObject("Image", obj_id)     # just check the image exists
        if image is None:
            return handlerInternalError(request, "Can't find image ID %s as data source for Open Astex Viewer." % obj_id)
        imageName = image.getName()
        c = image.getChannels()[0]
        # By default, scale to 120 ^3. Also give option to load 'bigger' map or full sized
        DEFAULTMAPSIZE = 120
        BIGGERMAPSIZE = 160
        targetSize = DEFAULTMAPSIZE * DEFAULTMAPSIZE * DEFAULTMAPSIZE
        biggerSize = BIGGERMAPSIZE * BIGGERMAPSIZE * BIGGERMAPSIZE
        imgSize = image.getSizeX() * image.getSizeY() * image.getSizeZ()
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

    context = {'data_url': data_url, "image": image,
        "sizeOptions":sizeOptions, "contourSliderInit":contourSliderInit, "contourSliderIncr":contourSliderIncr,
        "data_storage_mode": data_storage_mode,'pixelRange':pixelRange}
    context['template'] = 'webclient/annotations/open_astex_viewer.html'
    return context


@login_required()
@render_response()
def load_metadata_details(request, c_type, c_id, conn=None, share_id=None, **kwargs):
    """
    This page is the right-hand panel 'general metadata', first tab only.
    Shown for Projects, Datasets, Images, Screens, Plates, Wells, Tags etc.
    The data and annotations are loaded by the manager. Display of appropriate data is handled by the template.
    """

    # the index of a field within a well
    index = getIntOrDefault(request, 'index', 0)

    # we only expect a single object, but forms can take multiple objects
    images = c_type == "image" and list(conn.getObjects("Image", [c_id])) or list()
    datasets = c_type == "dataset" and list(conn.getObjects("Dataset", [c_id])) or list()
    projects = c_type == "project" and list(conn.getObjects("Project", [c_id])) or list()
    screens = c_type == "screen" and list(conn.getObjects("Screen", [c_id])) or list()
    plates = c_type == "plate" and list(conn.getObjects("Plate", [c_id])) or list()
    acquisitions = c_type == "acquisition" and list(conn.getObjects("PlateAcquisition", [c_id])) or list()
    shares = (c_type == "share" or c_type == "discussion") and [conn.getShare(c_id)] or list()
    wells = list()
    if c_type == "well":
        for w in conn.getObjects("Well", [c_id]):
            w.index=index
            wells.append(w)

    # we simply set up the annotation form, passing the objects to be annotated.
    selected = {'images':c_type == "image" and [c_id] or [],
        'datasets':c_type == "dataset" and [c_id] or [],
        'projects':c_type == "project" and [c_id] or [],
        'screens':c_type == "screen" and [c_id] or [],
        'plates':c_type == "plate" and [c_id] or [],
        'acquisitions':c_type == "acquisition" and [c_id] or [],
        'wells':c_type == "well" and [c_id] or [],
        'shares':(c_type == "share" or c_type == "discussion") and [c_id] or []}

    initial={'selected':selected, 'images':images,  'datasets':datasets, 'projects':projects, 'screens':screens, 'plates':plates, 'acquisitions':acquisitions, 'wells':wells, 'shares': shares}
    
    form_comment = None
    figScripts = None
    if c_type in ("share", "discussion"):
        template = "webclient/annotations/annotations_share.html"
        manager = BaseShare(conn, c_id)
        manager.getAllUsers(c_id)
        manager.getComments(c_id)
        form_comment = CommentAnnotationForm(initial=initial)
    else:
        try:
            manager = BaseContainer(conn, index=index, **{str(c_type): long(c_id)})
        except AttributeError, x:
            return handlerInternalError(request, x)
        if share_id is None:
            template = "webclient/annotations/metadata_general.html"
            manager.annotationList()
            figScripts = manager.listFigureScripts()
            form_comment = CommentAnnotationForm(initial=initial)
        else:
            template = "webclient/annotations/annotations_share.html"
    
    if c_type in ("tag"):
        context = {'manager':manager}
    else:
        context = {'manager':manager, 'form_comment':form_comment, 'index':index, 
            'share_id':share_id}
    context['figScripts'] = figScripts
    context['template'] = template
    context['webclient_path'] = request.build_absolute_uri(reverse('webindex'))
    return context


@login_required()
@render_response()
def load_metadata_preview(request, c_type, c_id, conn=None, share_id=None, **kwargs):
    """
    This is the image 'Preview' tab for the right-hand panel. 
    Currently this doesn't do much except launch the view-port plugin using the image Id (and share Id if necessary)
    """

    # the index of a field within a well
    index = getIntOrDefault(request, 'index', 0)

    manager = BaseContainer(conn, index=index, **{str(c_type): long(c_id)})
    
    if c_type == "well":
        manager.image = manager.well.getImage(index)

    context = {'manager':manager, 'share_id':share_id}
    context['template'] = "webclient/annotations/metadata_preview.html"
    return context


@login_required()
@render_response()
def load_metadata_hierarchy(request, c_type, c_id, conn=None, **kwargs):
    """
    This loads the ancestors of the specified object and displays them in a static tree.
    Used by an AJAX call from the metadata_general panel.
    """

    # the index of a field within a well
    index = getIntOrDefault(request, 'index', 0)
    
    manager = BaseContainer(conn, index=index, **{str(c_type): long(c_id)})
    
    context = {'manager':manager}
    context['template'] = "webclient/annotations/metadata_hierarchy.html"
    return context


@login_required()
@render_response()
def load_metadata_acquisition(request, c_type, c_id, conn=None, share_id=None, **kwargs):  
    """
    The acquisition tab of the right-hand panel. Only loaded for images.
    TODO: urls regex should make sure that c_type is only 'image' OR 'well'
    """

    # the index of a field within a well
    index = getIntOrDefault(request, 'index', 0)

    try:
        if c_type in ("share", "discussion"):
            template = "webclient/annotations/annotations_share.html"
            manager = BaseShare(conn, c_id)
            manager.getAllUsers(c_id)
            manager.getComments(c_id)
        else:
            template = "webclient/annotations/metadata_acquisition.html"
            manager = BaseContainer(conn, index=index, **{str(c_type): long(c_id)})
    except AttributeError, x:
        return handlerInternalError(request, x)

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
        if c_type == "well":
            manager.image = manager.well.getImage(index)
        if share_id is None:
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
                if share_id is None:        #9853 Much metadata is not available to 'shares'
                    lightPath = logicalChannel.getLightPath()
                    if lightPath is not None:
                        channel['form_dichroic'] = None
                        channel['form_excitation_filters'] = list()
                        channel['form_emission_filters'] = list()
                        lightPathDichroic = lightPath.getDichroic()
                        if lightPathDichroic is not None:
                            channel['form_dichroic'] = MetadataDichroicForm(initial={'dichroic': lightPathDichroic})
                        filterTypes = list(conn.getEnumerationEntries("FilterTypeI"))
                        for f in lightPath.getEmissionFilters():
                            channel['form_emission_filters'].append(MetadataFilterForm(initial={'filter': f,'types':filterTypes}))
                        for f in lightPath.getExcitationFilters():
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

        if share_id is None:    #9853
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
        context = {'manager':manager}
    else:
        context = {'manager':manager, 
        'form_channels':form_channels, 'form_environment':form_environment, 'form_objective':form_objective, 
        'form_microscope':form_microscope, 'form_instrument_objectives': form_instrument_objectives, 'form_filters':form_filters,
        'form_dichroics':form_dichroics, 'form_detectors':form_detectors, 'form_lasers':form_lasers, 'form_stageLabel':form_stageLabel}
    context['template'] = template
    return context


###########################################################################
# ACTIONS

# Annotation in the right-hand panel is handled the same way for single objects (metadata_general.html)
# AND for batch annotation (batch_annotate.html) by 4 forms:
# Comment (this is loaded in the initial page)
# Tags (the empty form is in the initial page but fields are loaded via AJAX)
# Local File (this is loaded in the initial page)
# Existing File (the empty form is in the initial page but field is loaded via AJAX)
#
# In each case, the form itself contains hidden fields to specify the object(s) being annotated
# All forms inherit from a single form that has these fields.

def getObjects(request, conn=None):
    """ 
    Prepare objects for use in the annotation forms. 
    These objects are required by the form superclass to populate hidden fields, so we know what we're annotating on submission
    """
    images = len(request.REQUEST.getlist('image')) > 0 and list(conn.getObjects("Image", request.REQUEST.getlist('image'))) or list()
    datasets = len(request.REQUEST.getlist('dataset')) > 0 and list(conn.getObjects("Dataset", request.REQUEST.getlist('dataset'))) or list()
    projects = len(request.REQUEST.getlist('project')) > 0 and list(conn.getObjects("Project", request.REQUEST.getlist('project'))) or list()
    screens = len(request.REQUEST.getlist('screen')) > 0 and list(conn.getObjects("Screen", request.REQUEST.getlist('screen'))) or list()
    plates = len(request.REQUEST.getlist('plate')) > 0 and list(conn.getObjects("Plate", request.REQUEST.getlist('plate'))) or list()
    acquisitions = len(request.REQUEST.getlist('acquisition')) > 0 and \
            list(conn.getObjects("PlateAcquisition", request.REQUEST.getlist('acquisition'))) or list()
    shares = len(request.REQUEST.getlist('share')) > 0 and [conn.getShare(request.REQUEST.getlist('share')[0])] or list()
    wells = list()
    if len(request.REQUEST.getlist('well')) > 0:
        index = getIntOrDefault(request, 'index', 0)
        for w in conn.getObjects("Well", request.REQUEST.getlist('well')):
            w.index=index
            wells.append(w)
    return {'image':images, 'dataset':datasets, 'project':projects, 'screen':screens, 
            'plate':plates, 'acquisition':acquisitions, 'well':wells, 'share':shares}

def getIds(request):
    """ Used by forms to indicate the currently selected objects prepared above """
    selected = {'images':request.REQUEST.getlist('image'), 'datasets':request.REQUEST.getlist('dataset'), \
            'projects':request.REQUEST.getlist('project'), 'screens':request.REQUEST.getlist('screen'), \
            'plates':request.REQUEST.getlist('plate'), 'acquisitions':request.REQUEST.getlist('acquisition'), \
            'wells':request.REQUEST.getlist('well'), 'shares':request.REQUEST.getlist('share')}
    return selected


@login_required(setGroupContext=True)
@render_response()
def batch_annotate(request, conn=None, **kwargs):
    """
    This page gives a form for batch annotation. 
    Local File form and Comment form are loaded. Other forms are loaded via AJAX
    """

    objs = getObjects(request, conn)
    selected = getIds(request)
    initial = {'selected':selected, 'images':objs['image'], 'datasets': objs['dataset'], 'projects':objs['project'], 
            'screens':objs['screen'], 'plates':objs['plate'], 'acquisitions':objs['acquisition'], 'wells':objs['well']}
    form_comment = CommentAnnotationForm(initial=initial)
    index = getIntOrDefault(request, 'index', 0)

    manager = BaseContainer(conn)
    batchAnns = manager.loadBatchAnnotations(objs)
    figScripts = manager.listFigureScripts(objs)

    obj_ids = []
    obj_labels = []
    for key in objs:
        obj_ids += ["%s=%s"%(key,o.id) for o in objs[key]]
        for o in objs[key]:
            obj_labels.append( {'type':key.title(), 'id':o.id, 'name':o.getName()} )
    obj_string = "&".join(obj_ids)
    link_string = "|".join(obj_ids).replace("=", "-")
    
    context = {'form_comment':form_comment, 'obj_string':obj_string, 'link_string': link_string,
            'obj_labels': obj_labels, 'batchAnns': batchAnns, 'batch_ann':True, 'index': index,
            'figScripts':figScripts}
    context['template'] = "webclient/annotations/batch_annotate.html"
    context['webclient_path'] = request.build_absolute_uri(reverse('webindex'))
    return context


@login_required(setGroupContext=True)
@render_response()
def annotate_file(request, conn=None, **kwargs):
    """ 
    On 'POST', This handles attaching an existing file-annotation(s) and/or upload of a new file to one or more objects 
    Otherwise it generates the form for choosing file-annotations & local files.
    """
    index = getIntOrDefault(request, 'index', 0)
    oids = getObjects(request, conn)
    selected = getIds(request)
    initial = {'selected':selected, 'images':oids['image'], 'datasets': oids['dataset'], 'projects':oids['project'], 
            'screens':oids['screen'], 'plates':oids['plate'], 'acquisitions':oids['acquisition'], 'wells':oids['well']}
    
    obj_count = sum( [len(selected[types]) for types in selected] )
    
    # Get appropriate manager, either to list available Tags to add to single object, or list ALL Tags (multiple objects)
    manager = None
    if obj_count == 1:
        for t in selected:
            if len(selected[t]) > 0:
                o_type = t[:-1]         # "images" -> "image"
                o_id = selected[t][0]
                break
        if o_type in ("dataset", "project", "image", "screen", "plate", "acquisition", "well","comment", "file", "tag", "tagset"):
            if o_type == 'tagset': o_type = 'tag' # TODO: this should be handled by the BaseContainer
            kw = {'index':index}
            if o_type is not None and o_id > 0:
                kw[str(o_type)] = long(o_id)
            try:
                manager = BaseContainer(conn, **kw)
            except AttributeError, x:
                return handlerInternalError(request, x)

    if manager is not None:
        files = manager.getFilesByObject()
    else:
        manager = BaseContainer(conn)
        for dtype, objs in oids.items():
            if len(objs) > 0:
                # NB: we only support a single data-type now. E.g. 'image' OR 'dataset' etc.
                files = manager.getFilesByObject(parent_type=dtype, parent_ids=[o.getId() for o in objs])
                break
    
    initial['files'] = files

    if request.method == 'POST':
        # handle form submission
        form_file = FilesAnnotationForm(initial=initial, data=request.REQUEST.copy())
        if form_file.is_valid():
            # Link existing files...
            files = form_file.cleaned_data['files']
            added_files = []
            if files is not None and len(files)>0:
                added_files = manager.createAnnotationsLinks('file', files, oids, well_index=index)
            # upload new file
            fileupload = 'annotation_file' in request.FILES and request.FILES['annotation_file'] or None
            if fileupload is not None and fileupload != "":
                newFileId = manager.createFileAnnotations(fileupload, oids, well_index=index)
                added_files.append(newFileId)
            if len(added_files) == 0:
                return HttpResponse("<div>No Files chosen</div>")
            template = "webclient/annotations/fileanns.html"
            context = {}
            # Now we lookup the object-annotations (same as for def batch_annotate above)
            batchAnns = manager.loadBatchAnnotations(oids, ann_ids=added_files, addedByMe=(obj_count==1))
            if obj_count > 1:
                context["batchAnns"] = batchAnns
                context['batch_ann'] = True
            else:
                # We only need a subset of the info in batchAnns
                fileanns = []
                for a in batchAnns['File']:
                    for l in a['links']:
                        fileanns.append(l.getAnnotation())
                context['fileanns'] = fileanns
                context['can_remove'] = True
        else:
            return HttpResponse(form_file.errors)

    else:
        form_file = FilesAnnotationForm(initial=initial)
        context = {'form_file': form_file, 'index': index}
        template = "webclient/annotations/files_form.html"
    context['template'] = template
    return context

@login_required(setGroupContext=True)
@render_response()
def annotate_comment(request, conn=None, **kwargs):
    """ Handle adding Comments to one or more objects 
    Unbound instance of Comment form not available. 
    If the form has been submitted, a bound instance of the form 
    is created using request.POST"""

    if request.method != 'POST':
        raise Http404("Unbound instance of form not available.")
    
    index = getIntOrDefault(request, 'index', 0)
    oids = getObjects(request, conn)
    selected = getIds(request)
    initial = {'selected':selected, 'images':oids['image'], 'datasets': oids['dataset'], 'projects':oids['project'], 
            'screens':oids['screen'], 'plates':oids['plate'], 'acquisitions':oids['acquisition'], 'wells':oids['well'],
            'shares':oids['share']}
    
    # Handle form submission...
    form_multi = CommentAnnotationForm(initial=initial, data=request.REQUEST.copy())
    if form_multi.is_valid():
        # In each case below, we pass the {'object_type': [ids]} map
        content = form_multi.cleaned_data['comment']
        if content is not None and content != "":
            if oids['share'] is not None and len(oids['share']) > 0:
                sid = oids['share'][0].id
                manager = BaseShare(conn, sid)
                host = request.build_absolute_uri(reverse("load_template", args=["public"]))
                textAnn = manager.addComment(host, conn.server_id, content)
            else:
                manager = BaseContainer(conn)
                textAnn = manager.createCommentAnnotations(content, oids, well_index=index)
            context = {'tann': textAnn, 'template':"webclient/annotations/comment.html"}
            return context
    else:
        return HttpResponse(str(form_multi.errors))      # TODO: handle invalid form error

@login_required(setGroupContext=True)
@render_response()
def annotate_tags(request, conn=None, **kwargs):
    """ This handles creation AND submission of Tags form, adding new AND/OR existing tags to one or more objects """

    index = getIntOrDefault(request, 'index', 0)
    oids = getObjects(request, conn)
    selected = getIds(request)
    obj_count = sum( [len(selected[types]) for types in selected] )

    # Get appropriate manager, either to list available Tags to add to single object, or list ALL Tags (multiple objects)
    manager = None
    if obj_count == 1:
        for t in selected:
            if len(selected[t]) > 0:
                o_type = t[:-1]         # "images" -> "image"
                o_id = selected[t][0]
                break
        if o_type in ("dataset", "project", "image", "screen", "plate", "acquisition", "well","comment", "file", "tag", "tagset"):
            if o_type == 'tagset': o_type = 'tag' # TODO: this should be handled by the BaseContainer
            kw = {'index':index}
            if o_type is not None and o_id > 0:
                kw[str(o_type)] = long(o_id)
            try:
                manager = BaseContainer(conn, **kw)
            except AttributeError, x:
                return handlerInternalError(request, x)
        elif o_type in ("share", "sharecomment"):
            manager = BaseShare(conn, o_id)

    if manager is not None:
        tags = manager.getTagsByObject()
    else:
        manager = BaseContainer(conn)
        for dtype, objs in oids.items():
            if len(objs) > 0:
                # NB: we only support a single data-type now. E.g. 'image' OR 'dataset' etc.
                tags = manager.getTagsByObject(parent_type=dtype, parent_ids=[o.getId() for o in objs])
                break

    initial = {'selected':selected, 'images':oids['image'], 'datasets': oids['dataset'], 'projects':oids['project'], 
            'screens':oids['screen'], 'plates':oids['plate'], 'acquisitions':oids['acquisition'], 'wells':oids['well']}
    initial['tags'] = tags

    if request.method == 'POST':
        # handle form submission
        form_tags = TagsAnnotationForm(initial=initial, data=request.REQUEST.copy())
        # Create new tags or Link existing tags...
        if form_tags.is_valid():
            tag = form_tags.cleaned_data['tag']
            description = form_tags.cleaned_data['description']
            tags = form_tags.cleaned_data['tags']
            added_tags = [];
            if tags is not None and len(tags)>0:
                added_tags = manager.createAnnotationsLinks('tag', tags, oids, well_index=index)
            if tag is not None and tag != "":
                new_tag_id = manager.createTagAnnotations(tag, description, oids, well_index=index)
                added_tags.append(new_tag_id)
            if len(added_tags) == 0:
                return HttpResponse("<div>No Tags Added</div>")
            template = "webclient/annotations/tags.html"
            context = {}
            # Now we lookup the object-annotations (same as for def batch_annotate above)
            batchAnns = manager.loadBatchAnnotations(oids, ann_ids=added_tags, addedByMe=(obj_count==1))
            if obj_count > 1:
                context["batchAnns"] = batchAnns
                context['batch_ann'] = True
            else:
                # We only need a subset of the info in batchAnns
                taganns = []
                for a in batchAnns['Tag']:
                    for l in a['links']:
                        taganns.append(l.getAnnotation())
                context['tags'] = taganns
                context['can_remove'] = True
        else:
            return HttpResponse(str(form_tags.errors))      # TODO: handle invalid form error

    else:
        form_tags = TagsAnnotationForm(initial=initial)
        context = {'form_tags': form_tags, 'index': index}
        template = "webclient/annotations/tags_form.html"
    context['template'] = template
    return context


@login_required()
@render_response()
def edit_channel_names(request, imageId, conn=None, **kwargs):
    """
    Edit and save channel names
    """
    image = conn.getObject("Image", imageId)
    sizeC = image.getSizeC()
    channelNames = {}
    nameDict = {}
    for i in range(sizeC):
        cname = request.REQUEST.get("channel%d" % i, None)
        if cname is not None:
            channelNames["channel%d" % i] = smart_str(cname)
            nameDict[i+1] = smart_str(cname)
    # If the 'Apply to Dataset' button was used to submit...
    if request.REQUEST.get('confirm_apply', None) is not None:
        parentId = request.REQUEST.get('parentId', None)    # plate-123 OR dataset-234
        if parentId is not None:
            ptype = parentId.split("-")[0].title()
            pid = long(parentId.split("-")[1])
            counts = conn.setChannelNames(ptype, [pid], nameDict, channelCount=sizeC)
    else:
        counts = conn.setChannelNames("Image", [image.getId()], nameDict)
    rv = {"channelNames": channelNames}
    if counts:
        rv['imageCount'] = counts['imageCount']
        rv['updateCount'] = counts['updateCount']
        return rv
    else:
        return {"error": "No parent found to apply Channel Names"}


@login_required(setGroupContext=True)
@render_response()
def manage_action_containers(request, action, o_type=None, o_id=None, conn=None, **kwargs):
    """
    Handles many different actions on various objects.
    
    @param action:      "addnewcontainer", (creates a new Project, Dataset, Screen)
                        "editname", "savename", "editdescription", "savedescription",  (used as GET and POST for in-line editing)
                        "paste", "move", "remove", "removefromshare", (tree P/D/I moving etc)
                        "delete", "deletemany"      (delete objects)
    @param o_type:      "dataset", "project", "image", "screen", "plate", "acquisition", "well","comment", "file", "tag", "tagset","share", "sharecomment"
    """
    template = None
    
    # the index of a field within a well
    index = getIntOrDefault(request, 'index', 0)
    
    manager = None
    if o_type in ("dataset", "project", "image", "screen", "plate", "acquisition", "well","comment", "file", "tag", "tagset"):
        if o_type == 'tagset': o_type = 'tag' # TODO: this should be handled by the BaseContainer
        kw = {'index':index}
        if o_type is not None and o_id > 0:
            kw[str(o_type)] = long(o_id)
        try:
            manager = BaseContainer(conn, **kw)
        except AttributeError, x:
            return handlerInternalError(request, x)
    elif o_type in ("share", "sharecomment"):
        manager = BaseShare(conn, o_id)
    else:
        manager = BaseContainer(conn)
        
    form = None
    if action == 'addnewcontainer':
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
                folder_type = request.REQUEST.get('folder_type')
                if folder_type == "dataset":
                    oid = manager.createDataset(name,description, img_ids=request.REQUEST.get('img_ids', None))
                else:
                    oid = getattr(manager, "create"+folder_type.capitalize())(name, description)
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
    elif action == 'edit':
        # form for editing an Object. E.g. Project etc. TODO: not used now? 
        if o_type == "share" and o_id > 0:
            template = "webclient/public/share_form.html"
            manager.getMembers(o_id)
            manager.getComments(o_id)
            experimenters = list(conn.getExperimenters())
            experimenters.sort(key=lambda x: x.getOmeName().lower())
            initial={'message': manager.share.message, 'expiration': "", \
                                    'shareMembers': manager.membersInShare, 'enable': manager.share.active, \
                                    'experimenters': experimenters}
            if manager.share.getExpireDate() is not None:
                initial['expiration'] = manager.share.getExpireDate().strftime("%Y-%m-%d")
            form = ShareForm(initial=initial) #'guests': share.guestsInShare,
            context = {'share':manager, 'form':form}
        elif hasattr(manager, o_type) and o_id > 0:
            obj = getattr(manager, o_type)
            template = "webclient/data/container_form.html"
            form = ContainerForm(initial={'name': obj.name, 'description':obj.description})
            context = {'manager':manager, 'form':form}
    elif action == 'save':
        # Handles submission of the 'edit' form above. TODO: not used now?
        if not request.method == 'POST':
            return HttpResponseRedirect(reverse("manage_action_containers", args=["edit", o_type, o_id]))
        if o_type == "share":
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
                manager.updateShareOrDiscussion(host, conn.server_id, message, members, enable, expiration)
                return HttpResponse("DONE")
            else:
                template = "webclient/public/share_form.html"
                context = {'share':manager, 'form':form}
        else:
            return HttpResponseServerError("Object does not exist")
    elif action == 'editname':
        # start editing 'name' in-line
        if hasattr(manager, o_type) and o_id > 0:
            obj = getattr(manager, o_type)
            if (o_type == "well"):
                obj = obj.getWellSample(index).image()
            template = "webclient/ajax_form/container_form_ajax.html"
            if o_type == "tag":
                txtValue = obj.textValue
            else:
                txtValue = obj.getName()
            form = ContainerNameForm(initial={'name': txtValue})
            context = {'manager':manager, 'form':form}
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
                rdict = {'bad':'false', 'o_type': o_type}
                if (o_type == "well"):
                    manager.image = manager.well.getWellSample(index).image()
                    o_type = "image"
                manager.updateName(o_type, name)
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
            if (o_type == "well"):
                obj = obj.getWellSample(index).image()
            template = "webclient/ajax_form/container_form_ajax.html"
            form = ContainerDescriptionForm(initial={'description': obj.description})
            context = {'manager':manager, 'form':form}
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
                if (o_type == "well"):
                    manager.image = manager.well.getWellSample(index).image()
                    o_type = "image"
                manager.updateDescription(o_type, description)
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
        parents = request.REQUEST['parent']     # E.g. image-123  or image-1|image-2
        try:
            manager.remove(parents.split('|'), index)
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
    elif action == 'delete':
        # Handles delete of a file attached to object.
        child = toBoolean(request.REQUEST.get('child'))
        anns = toBoolean(request.REQUEST.get('anns'))
        try:
            handle = manager.deleteItem(child, anns)
            request.session['callback'][str(handle)] = {'job_type': 'delete', 'delmany':False,'did':o_id, 'dtype':o_type, 'status':'in progress',
                'derror':0, 'dreport':_formatReport(handle), 'start_time': datetime.datetime.now()}
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
        object_ids = {'Image':request.REQUEST.getlist('image'), 'Dataset':request.REQUEST.getlist('dataset'), 'Project':request.REQUEST.getlist('project'), 'Screen':request.REQUEST.getlist('screen'), 'Plate':request.REQUEST.getlist('plate'), 'Well':request.REQUEST.getlist('well'), 'PlateAcquisition':request.REQUEST.getlist('acquisition')}
        child = toBoolean(request.REQUEST.get('child'))
        anns = toBoolean(request.REQUEST.get('anns'))
        logger.debug("Delete many: child? %s anns? %s object_ids %s" % (child, anns, object_ids))
        try:
            for key,ids in object_ids.iteritems():
                if ids is not None and len(ids) > 0:
                    handle = manager.deleteObjects(key, ids, child, anns)
                    dMap = {'job_type': 'delete', 'start_time': datetime.datetime.now(),'status':'in progress', 'derrors':0,
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
    context['template'] = template
    return context

@login_required(doConnectionCleanup=False)
def get_original_file(request, fileId, conn=None, **kwargs):
    """ Returns the specified original file as an http response. Used for displaying text or png/jpeg etc files in browser """

    # May be viewing results of a script run in a different group.
    conn.SERVICE_OPTS.setOmeroGroup(-1)

    orig_file = conn.getObject("OriginalFile", fileId)
    if orig_file is None:
        return handlerInternalError(request, "Original File does not exists (id:%s)." % (fileId))
    
    rsp = ConnCleaningHttpResponse(orig_file.getFileInChunks())
    rsp.conn = conn
    mimetype = orig_file.mimetype
    if mimetype == "text/x-python": 
        mimetype = "text/plain" # allows display in browser
    rsp['Content-Type'] =  mimetype
    rsp['Content-Length'] = orig_file.getSize()
    #rsp['Content-Disposition'] = 'attachment; filename=%s' % (orig_file.name.replace(" ","_"))
    return rsp


@login_required()
def image_as_map(request, imageId, conn=None, **kwargs):
    """ Converts OMERO image into mrc.map file (using tiltpicker utils) and returns the file """

    from omero_ext.tiltpicker.pyami import mrc
    from numpy import dstack, zeros, int8

    image = conn.getObject("Image", imageId)
    if image is None:
        message = "Image ID %s not found in image_as_map" % imageId
        logger.error(message)
        return handlerInternalError(request, message)

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
    # Sometimes causes scaling issues in OAV.
    #header["xlen"] = pixels.physicalSizeX * image.getSizeX()
    #header["ylen"] = pixels.physicalSizeY * image.getSizeY()
    #header["zlen"] = pixels.physicalSizeZ * image.getSizeZ()
    #if header["xlen"] == 0 or header["ylen"] == 0 or header["zlen"] == 0:
        #header = {}

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
        return handlerInternalError(request, "Cannot generate map (id:%s)." % (imageId))
    return rsp


@login_required(doConnectionCleanup=False)
def download_annotation(request, annId, conn=None, **kwargs):
    """ Returns the file annotation as an http response for download """
    ann = conn.getObject("Annotation", annId)
    if ann is None:
        return handlerInternalError(request, "Annotation does not exist (id:%s)." % (annId))
    
    rsp = ConnCleaningHttpResponse(ann.getFileInChunks())
    rsp.conn = conn
    rsp['Content-Type'] = 'application/force-download'
    rsp['Content-Length'] = ann.getFileSize()
    rsp['Content-Disposition'] = 'attachment; filename=%s' % (ann.getFileName().replace(" ","_"))
    return rsp


@login_required()
def download_orig_metadata(request, imageId, conn=None, **kwargs):
    """ Downloads the 'Original Metadata' as a text file """

    image = conn.getObject("Image", imageId)
    if image is None:
        raise Http404("No Image found with ID %s" % imageId)

    om = image.loadOriginalMetadata()

    txtLines = ["[Global Metadata]"]
    txtLines.extend( ["%s=%s" % (kv[0], kv[1]) for kv in om[1]] )

    txtLines.append("[Series Metadata]")
    txtLines.extend( ["%s=%s" % (kv[0], kv[1]) for kv in om[2]] )
    rspText = "\n".join(txtLines)

    rsp = HttpResponse(rspText)
    rsp['Content-Type'] = 'application/force-download'
    rsp['Content-Length'] = len(rspText)
    rsp['Content-Disposition'] = 'attachment; filename=Original_Metadata.txt'
    return rsp


@login_required()
@render_response()
def load_public(request, share_id=None, conn=None, **kwargs):
    """ Loads data for the tree in the 'public' main page. """
    
    # SUBTREE TODO:
    if share_id is None:
        share_id = request.REQUEST.get("o_id") is not None and long(request.REQUEST.get("o_id")) or None
    
    # check view
    view = request.REQUEST.get("view")
    
    if share_id is not None:
        if view == 'tree':
            template = "webclient/public/share_subtree.html"
        elif view == 'icon':
            template = "webclient/public/share_content_icon.html"
        controller = BaseShare(conn, share_id)
        controller.loadShareContent()
        
    else:
        template = "webclient/public/share_tree.html"
        controller = BaseShare(conn)
        controller.getShares()

    context = {'share':controller}
    context['isLeader'] = conn.isLeader()
    context['template'] = template
    return context

##################################################################
# Basket

@login_required(setGroupContext=True)
@render_response()
def basket_action (request, action=None, conn=None, **kwargs):
    """
    Various actions for creating a 'share' or 'discussion' (no images).
    
    @param action:      'toshare', 'createshare'    (form to create share and handling the action itself)
                        'todiscuss', 'createdisc'    (form to create discussion and handling the action itself)
    """
    
    if action == "toshare":
        template = "webclient/basket/basket_share_action.html"
        basket = BaseBasket(conn)
        basket.load_basket(request)
        experimenters = list(conn.getExperimenters())
        experimenters.sort(key=lambda x: x.getOmeName().lower())
        selected = [long(i) for i in request.REQUEST.getlist('image')]        
        form = BasketShareForm(initial={'experimenters':experimenters, 'images':basket.imageInBasket, 'enable':True, 'selected':selected})            
        context = {'form':form}
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
            share.createShare(host, conn.server_id, images, message, members, enable, expiration)
            return HttpResponse("success")
        else:
            template = "webclient/basket/basket_share_action.html"
            context = {'form':form}
    elif action == "todiscuss":
        template = "webclient/basket/basket_discussion_action.html"
        basket = BaseBasket(conn)
        experimenters = list(conn.getExperimenters())
        experimenters.sort(key=lambda x: x.getOmeName().lower())
        form = ShareForm(initial={'experimenters':experimenters, 'enable':True})            
        context = {'form':form}
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
            share.createDiscussion(host, conn.server_id, message, members, enable, expiration)
            return HttpResponse("success")
        else:
            template = "webclient/basket/basket_discussion_action.html"
            context = {'form':form}
    else:
        template = kwargs.get("template", "webclient/basket/basket.html")
        
        basket = BaseBasket(conn)
        basket.load_basket(request)
        
        context = {'basket':basket }
    context['template'] = template
    return context

@login_required()
def empty_basket(request, **kwargs):
    """ Empty the basket of images """

    try:
        del request.session['imageInBasket']
        del request.session['basket_counter']
    except KeyError:
        logger.error(traceback.format_exc())

    return HttpResponseRedirect(reverse("basket_action"))

@login_required()
def update_basket(request, **kwargs):
    """ Add or remove images to the set in the basket """

    action = None
    if request.method == 'POST':
        request.session.modified = True        
        try:
            action = request.REQUEST['action']
        except Exception, x:
            logger.error(traceback.format_exc())
            return handlerInternalError(request, "Attribute error: 'action' is missed.")
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
        request.session['basket_counter'] = total
        return HttpResponse(total)
    else:
        return handlerInternalError(request, "Request method error in Basket.")

@login_required(setGroupContext=True)
@render_response()
def load_calendar(request, year=None, month=None, conn=None, **kwargs):
    """ 
    Loads the calendar which is displayed in the left panel of the history page. 
    Shows current month by default. Filter by experimenter 
    """
    
    template = "webclient/history/calendar.html"
    filter_user_id = request.session.get('user_id')
    
    if year is not None and month is not None:
        controller = BaseCalendar(conn=conn, year=year, month=month, eid=filter_user_id)
    else:
        today = datetime.datetime.today()
        controller = BaseCalendar(conn=conn, year=today.year, month=today.month, eid=filter_user_id)
    controller.create_calendar()

    context = {'controller':controller}

    context['template'] = template
    return context


@login_required(setGroupContext=True)
@render_response()
def load_history(request, year, month, day, conn=None, **kwargs):
    """ The data for a particular date that is loaded into the center panel """

    template = "webclient/history/history_details.html"
    
    # get page 
    page = int(request.REQUEST.get('page', 1))
    
    filter_user_id = request.session.get('user_id')
    controller = BaseCalendar(conn=conn, year=year, month=month, day=day, eid=filter_user_id)
    controller.get_items(page)
    
    context = {'controller':controller}
    context['template'] = template
    return context


def getObjectUrl(conn, obj):
    """
    This provides a url to browse to the specified omero.model.ObjectI P/D/I, S/P, FileAnnotation etc.
    used to display results from the scripting service
    E.g webclient/userdata/?path=image-12601 
    If the object is a file annotation, try to browse to the parent P/D/I
    """
    base_url = reverse(viewname="load_template", args=['userdata'])

    blitz_obj = None
    url = None
    # if we have a File Annotation, then we want our URL to be for the parent object...
    if isinstance(obj, omero.model.FileAnnotationI):
        fa = conn.getObject("Annotation", obj.id.val)
        for ptype in ['project', 'dataset', 'image']:
            links = list(fa.getParentLinks(ptype))
            if len(links) > 0:
                obj = links[0].parent
                break

    if obj.__class__.__name__ in ("ImageI", "DatasetI", "ProjectI", "ScreenI", "PlateI"):
        otype = obj.__class__.__name__[:-1].lower()
        base_url += "?show=%s-%s" % (otype, obj.id.val)
        return base_url


######################
# Activities window & Progressbar
@login_required()
@render_response()
def activities(request, conn=None, **kwargs):
    """
    This refreshes callback handles (delete, scripts, chgrp etc) and provides html to update Activities window & Progressbar.
    The returned html contains details for ALL callbacks in web session, regardless of their status.
    We also add counts of jobs, failures and 'in progress' to update status bar.
    """

    in_progress = 0
    failure = 0
    new_results = []
    _purgeCallback(request)


    # test each callback for failure, errors, completion, results etc
    for cbString in request.session.get('callback').keys():
        job_type = request.session['callback'][cbString]['job_type']

        status = request.session['callback'][cbString]['status']
        if status == "failed":
            failure+=1

        request.session.modified = True

        # update chgrp
        if job_type == 'chgrp':
            if status not in ("failed", "finished"):
                rsp = None
                try:
                    prx = omero.cmd.HandlePrx.checkedCast(conn.c.ic.stringToProxy(cbString))
                    rsp = prx.getResponse()
                    close_handle = False
                    try:
                        # if response is None, then we're still in progress, otherwise...
                        if rsp is not None:
                            close_handle = True
                            new_results.append(cbString)
                            if isinstance(rsp, omero.cmd.ERR):
                                request.session['callback'][cbString]['status'] = "failed"
                                rsp_params = ", ".join(["%s: %s" % (k,v) for k,v in rsp.parameters.items()])
                                logger.error("chgrp failed with: %s" % rsp_params)
                                request.session['callback'][cbString]['error'] = "%s %s" % (rsp.name, rsp_params)
                            elif isinstance(rsp, omero.cmd.OK):
                                request.session['callback'][cbString]['status'] = "finished"
                        else:
                            in_progress+=1
                    finally:
                        prx.close(close_handle)
                except:
                    logger.info("Activities chgrp handle not found: %s" % cbString)
                    continue

        # update delete
        elif job_type == 'delete':
            if status not in ("failed", "finished"):
                try:
                    handle = omero.cmd.HandlePrx.checkedCast(conn.c.ic.stringToProxy(cbString))
                    cb = omero.callbacks.CmdCallbackI(conn.c, handle)
                    close_handle = False
                    try:
                        if not cb.block(0): # Response not available
                            request.session['callback'][cbString]['derror'] = 0
                            request.session['callback'][cbString]['status'] = "in progress"
                            request.session['callback'][cbString]['dreport'] = _formatReport(handle)
                            in_progress+=1
                        else: # Response available
                            close_handle = True
                            err = isinstance(cb.getResponse(), omero.cmd.ERR)
                            new_results.append(cbString)
                            if err:
                                request.session['callback'][cbString]['derror'] = 1
                                request.session['callback'][cbString]['status'] = "failed"
                                request.session['callback'][cbString]['dreport'] = _formatReport(handle)
                                failure+=1
                            else:
                                request.session['callback'][cbString]['derror'] = 0
                                request.session['callback'][cbString]['status'] = "finished"
                                request.session['callback'][cbString]['dreport'] = _formatReport(handle)
                    finally:
                        cb.close(close_handle)
                except Ice.ObjectNotExistException, e:
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
                        results = proc.getResults(0, conn.SERVICE_OPTS)     # we can only retrieve this ONCE - must save results
                        request.session['callback'][cbString]['status'] = "finished"
                        new_results.append(cbString)
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
                                    mimetypes = {'image/png':'png', 'image/jpeg':'jpeg', 'text/plain': 'text'}
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
                else:
                    in_progress+=1

    # having updated the request.session, we can now prepare the data for http response
    rv = {}
    for cbString in request.session.get('callback').keys():
        # make a copy of the map in session, so that we can replace non json-compatible objects, without modifying session
        rv[cbString] = copy.copy(request.session['callback'][cbString])
    
    # return json (not used now, but still an option)
    if 'template' in kwargs and kwargs['template'] == 'json':
        for cbString in request.session.get('callback').keys():
            rv[cbString]['start_time'] = str(request.session['callback'][cbString]['start_time'])
        rv['inprogress'] = in_progress
        rv['failure'] = failure
        rv['jobs'] = len(request.session['callback'])
        return HttpResponse(simplejson.dumps(rv),mimetype='application/javascript') # json
        
    jobs = []
    for key, data in rv.items():
        # E.g. key: ProcessCallback/39f77932-c447-40d8-8f99-910b5a531a25 -t:tcp -h 10.211.55.2 -p 54727:tcp -h 10.37.129.2 -p 54727:tcp -h 10.12.2.21 -p 54727
        # create id we can use as html id, E.g. 39f77932-c447-40d8-8f99-910b5a531a25
        if len(key.split(" ")) > 0:
            htmlId = key.split(" ")[0]
            if len(htmlId.split("/")) > 1:
                htmlId = htmlId.split("/")[1]
        rv[key]['id'] = htmlId
        rv[key]['key'] = key
        if key in new_results:
            rv[key]['new'] = True
        jobs.append(rv[key])

    jobs.sort(key=lambda x:x['start_time'], reverse=True)
    context = {'sizeOfJobs':len(request.session['callback']),
            'jobs':jobs,
            'inprogress':in_progress,
            'new_results':len(new_results),
            'failure':failure}

    context['template'] = "webclient/activities/activitiesContent.html"
    return context


@login_required()
def activities_update (request, action, **kwargs):
    """
    If the above 'action' == 'clean' then we clear jobs from request.session['callback']
    either a single job (if 'jobKey' is specified in POST) or all jobs (apart from those in progress)
    """

    request.session.modified = True

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
    return HttpResponse("OK")

####################################################################################
# User Photo

@login_required()
def avatar(request, oid=None, conn=None, **kwargs):
    """ Returns the experimenter's photo """
    photo = conn.getExperimenterPhoto(oid)
    return HttpResponse(photo, mimetype='image/jpeg')

####################################################################################
# webgateway extention

@login_required()
def image_viewer (request, iid, share_id=None, **kwargs):
    """ Delegates to webgateway, using share connection if appropriate """
    kwargs['viewport_server'] = share_id is not None and reverse("webindex")+share_id or reverse("webindex")
    kwargs['viewport_server'] = kwargs['viewport_server'].rstrip('/')   # remove any trailing slash
    return webgateway_views.full_viewer(request, iid, **kwargs)


####################################################################################
# scripting service....
@login_required()
@render_response()
def list_scripts (request, conn=None, **kwargs):
    """ List the available scripts - Just officical scripts for now """
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

        # We want to build a hierarchical <ul> <li> structure
        # Each <ul> is a {}, each <li> is either a script 'name': <id> or directory 'name': {ul}

        ul = scriptMenu
        dirs = fullpath.split("/");
        for l, d in enumerate(dirs):
            if len(d) == 0:
                continue
            if d not in ul:
                # if last component in path:
                if l+1 == len(dirs):
                    ul[d] = scriptId
                else:
                    ul[d] = {}
            ul = ul[d]

    # convert <ul> maps into lists and sort

    def ul_to_list(ul):
        dir_list = []
        for name, value in ul.items():
            if isinstance(value, dict):
                # value is a directory
                dir_list.append({'name': name, 'ul': ul_to_list(value)})
            else:
                dir_list.append({'name': name, 'id':value})
        dir_list.sort(key=lambda x:x['name'].lower())
        return dir_list

    scriptList = ul_to_list(scriptMenu)

    # If we have a single top-level directory, we can skip it
    if len(scriptList) == 1:
        scriptList = scriptList[0]['ul']

    return scriptList


@login_required()
@render_response()
def script_ui(request, scriptId, conn=None, **kwargs):
    """
    Generates an html form for the parameters of a defined script.
    """
    scriptService = conn.getScriptService()

    try:
        params = scriptService.getParams(long(scriptId))
    except Exception, ex:
        if ex.message.lower().startswith("no processor available"):
            return {'template':'webclient/scripts/no_processor.html', 'scriptId': scriptId}
        raise ex
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
            if isinstance(i["default"], omero.model.IObject):
                i["default"] = None
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

    # if we have Data_Type param - use the request parameters to populate IDs
    if Data_TypeParam is not None and IDsParam is not None and "options" in Data_TypeParam:
        IDsParam["default"] = ""
        for dtype in Data_TypeParam["options"]:
            if request.REQUEST.get(dtype, None) is not None:
                Data_TypeParam["default"] = dtype
                IDsParam["default"] = request.REQUEST.get(dtype, "")
                break       # only use the first match

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

    return {'template':'webclient/scripts/script_ui.html', 'paramData': paramData, 'scriptId': scriptId}


@login_required(setGroupContext=True)       # group ctx used for getting Tags etc.
@render_response()
def figure_script(request, scriptName, conn=None, **kwargs):
    """
    Show a UI for running figure scripts
    """

    imageIds = request.REQUEST.get('Image', None)    # comma - delimited list
    datasetIds = request.REQUEST.get('Dataset', None)
    if imageIds is None and datasetIds is None:
        return HttpResponse("Need to specify /?Image=1,2 or /?Dataset=1,2")

    def validateIds(dtype, ids):
        ints = [int(oid) for oid in ids.split(",")]
        validObjs = {}
        for obj in conn.getObjects(dtype, ints):
            validObjs[obj.id] = obj
        filteredIds = [iid for iid in ints if iid in validObjs.keys()]
        if len(filteredIds) == 0:
            raise Http404("No %ss found with IDs %s" % (dtype, ids))
        return filteredIds, validObjs

    context = {}

    if imageIds is not None:
        imageIds, validImages = validateIds("Image", imageIds)
        context['idString'] = ",".join( [str(i) for i in imageIds] )
        context['dtype'] = "Image"
    if datasetIds is not None:
        datasetIds, validDatasets = validateIds("Dataset", datasetIds)
        context['idString'] = ",".join( [str(i) for i in datasetIds] )
        context['dtype'] = "Dataset"

    if scriptName == "SplitView":
        scriptPath = "/omero/figure_scripts/Split_View_Figure.py"
        template = "webclient/scripts/split_view_figure.html"
        # Lookup Tags & Datasets (for row labels)
        imgDict = []    # A list of data about each image.
        for iId in imageIds:
            data = {'id':iId}
            img = validImages[iId]
            data['name'] = img.getName()
            tags = [ann.getTextValue() for ann in img.listAnnotations() if ann._obj.__class__ == omero.model.TagAnnotationI]
            data['tags'] = tags
            data['datasets'] = [d.getName() for d in img.listParents()]
            imgDict.append(data)

        # Use the first image as a reference
        image = validImages[imageIds[0]]
        context['imgDict'] = imgDict
        context['image'] = image
        context['channels'] = image.getChannels()

    elif scriptName == "Thumbnail":
        scriptPath = "/omero/figure_scripts/Thumbnail_Figure.py"
        template = "webclient/scripts/thumbnail_figure.html"
        #context['tags'] = BaseContainer(conn).getTagsByObject()    # ALL tags

        def loadImageTags(imageIds):
            tagLinks = conn.getAnnotationLinks("Image", parent_ids=imageIds)
            linkMap = {}    # group tags. {imageId: [tags]}
            tagMap = {}
            for iId in imageIds:
                linkMap[iId] = []
            for l in tagLinks:
                c = l.getChild()
                if c._obj.__class__ == omero.model.TagAnnotationI:
                    tagMap[c.id] = c
                    linkMap[l.getParent().id].append(c)
            imageTags = []
            for iId in imageIds:
                imageTags.append({'id':iId, 'tags':linkMap[iId]})
            tags = []
            for tId, t in tagMap.items():
                tags.append(t)
            return imageTags, tags

        thumbSets = []  # multiple collections of images
        tags = []
        figureName = "Thumbnail_Figure"
        if datasetIds is not None:
            for d in conn.getObjects("Dataset", datasetIds):
                figureName = d.getName()
                imgIds = [i.id for i in d.listChildren()]
                imageTags, ts = loadImageTags(imgIds)
                thumbSets.append({'name':d.getName(), 'imageTags': imageTags})
                tags.extend(ts)
        else:
            imageTags, ts = loadImageTags(imageIds)
            thumbSets.append({'name':'images', 'imageTags': imageTags})
            tags.extend(ts)
            figureName = conn.getObject("Image", imageIds[0]).getParent().getName()
        uniqueTagIds = set()      # remove duplicates
        uniqueTags = []
        for t in tags:
            if t.id not in uniqueTagIds:
                uniqueTags.append(t)
                uniqueTagIds.add(t.id)
        uniqueTags.sort(key=lambda x: x.getTextValue().lower())
        context['thumbSets'] = thumbSets
        context['tags'] = uniqueTags
        context['figureName'] = figureName.replace(" ", "_")

    scriptService = conn.getScriptService()
    scriptId = scriptService.getScriptID(scriptPath);
    if (scriptId < 0):
        raise AttributeError("No script found for path '%s'" % scriptPath)

    context['template'] = template
    context['scriptId'] = scriptId
    return context


@login_required()
def chgrp(request, conn=None, **kwargs):
    """
    Moves data to a new group, using the chgrp queue.
    Handles submission of chgrp form: all data in POST.
    Adds the callback handle to the request.session['callback']['jobId']
    """
    
    group_id = request.REQUEST.get('group_id', None)
    if group_id is None:
        raise AttributeError("chgrp: No group_id specified")
    group_id = long(group_id)

    group = conn.getObject("ExperimenterGroup", group_id)
    target_id = request.REQUEST.get('target_id', None)      # E.g. "dataset-234"
    container_id = target_id is not None and target_id.split("-")[1] or None
    dtypes = ["Project", "Dataset", "Image", "Screen", "Plate"]
    for dtype in dtypes:
        oids = request.REQUEST.get(dtype, None)
        if oids is not None:
            obj_ids = oids.split(",")
            logger.debug("chgrp to group:%s %s-%s" % (group_id, dtype, obj_ids))
            handle = conn.chgrpObjects(dtype, obj_ids, group_id, container_id)
            jobId = str(handle)
            request.session['callback'][jobId] = {
                'job_type': "chgrp",
                'group': group.getName(),
                'dtype': dtype,
                'obj_ids': obj_ids,
                'job_name': "Change group",
                'start_time': datetime.datetime.now(),
                'status':'in progress'}
            request.session.modified = True

    return HttpResponse("OK")


@login_required(setGroupContext=True)
def script_run(request, scriptId, conn=None, **kwargs):
    """
    Runs a script using values in a POST
    """
    scriptService = conn.getScriptService()

    inputMap = {}

    sId = long(scriptId)

    try:
        params = scriptService.getParams(sId)
    except Exception, x:
        if x.message and x.message.startswith("No processor available"):
            # Delegate to run_script() for handling 'No processor available'
            rsp = run_script(request, conn, sId, inputMap, scriptName='Script')
            return HttpResponse(simplejson.dumps(rsp), mimetype='json')
        else:
            raise
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
            keyName = "%s_key0" % key
            valueName = "%s_value0" % key
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
    rsp = run_script(request, conn, sId, inputMap, scriptName)
    return HttpResponse(simplejson.dumps(rsp), mimetype='json')


@login_required(setGroupContext=True)
def ome_tiff_script(request, imageId, conn=None, **kwargs):
    """
    Uses the scripting service (Batch Image Export script) to generate OME-TIFF for an
    image and attach this as a file annotation to the image.
    Script will show up in the 'Activities' for users to monitor and download result etc.
    """
    #if not request.method == 'POST':
    #    return HttpResponse("Need to use POST")
    
    scriptService = conn.getScriptService()
    sId = scriptService.getScriptID("/omero/export_scripts/Batch_Image_Export.py")
    
    imageIds = [long(imageId)]
    inputMap = {'Data_Type': wrap('Image'), 'IDs': wrap(imageIds)}
    inputMap['Format'] = wrap('OME-TIFF')
    rsp = run_script(request, conn, sId, inputMap, scriptName='Create OME-TIFF')
    return HttpResponse(simplejson.dumps(rsp), mimetype='json')


def run_script(request, conn, sId, inputMap, scriptName='Script'):
    """
    Starts running a script, adding details to the request.session so that it shows up
    in the webclient Activities panel and results are available there etc.
    """
    request.session.modified = True
    scriptService = conn.getScriptService()
    try:
        handle = scriptService.runScript(sId, inputMap, None, conn.SERVICE_OPTS)
        # E.g. ProcessCallback/4ab13b23-22c9-4b5f-9318-40f9a1acc4e9 -t:tcp -h 10.37.129.2 -p 53154:tcp -h 10.211.55.2 -p 53154:tcp -h 10.12.1.230 -p 53154
        jobId = str(handle)
        status = 'in progress'
        request.session['callback'][jobId] = {
            'job_type': "script",
            'job_name': scriptName,
            'start_time': datetime.datetime.now(),
            'status':status}
        request.session.modified = True
    except Exception, x:
        jobId = str(time())      # E.g. 1312803670.6076391
        if x.message and x.message.startswith("No processor available"): # omero.ResourceError
            logger.info(traceback.format_exc())
            error = "No Processor Available"
            status = 'no processor available'
            message = "" # template displays message and link
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
        return {'status': status, 'error': error}

    return {'jobId': jobId, 'status': status}

@login_required()
@render_response()
def ome_tiff_info(request, imageId, conn=None, **kwargs):
    """
    Query to see if we have an OME-TIFF attached to the image (assume only 1, since Batch Image Export will delete old ones)
    """
    # Any existing OME-TIFF will appear in list
    links = list( conn.getAnnotationLinks("Image", [imageId], ns=omero.constants.namespaces.NSOMETIFF) )
    rv = {}
    if len(links) > 0:
        links.sort(key=lambda x: x.getId(), reverse=True)   # use highest ID === most recent
        annlink = links[0]
        created = annlink.creationEventDate()
        annId = annlink.getChild().getId()
        from omeroweb.webgateway.templatetags.common_filters import ago
        download = reverse("download_annotation", args=[annId])
        rv = {"created": str(created), "ago": ago(created), "id":annId, "download": download}
    return rv       # will get returned as json by default
