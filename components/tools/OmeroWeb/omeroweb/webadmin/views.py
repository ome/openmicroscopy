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

import os
import sys
import locale
import calendar
import datetime
import traceback
import logging

from time import time

from omero_version import omero_version

from django.conf import settings
from django.contrib.sessions.backends.cache import SessionStore
from django.core import template_loader
from django.core.cache import cache
from django.core.urlresolvers import reverse
from django.http import HttpResponse, HttpRequest, HttpResponseRedirect, Http404
from django.shortcuts import render_to_response
from django.template import RequestContext as Context
from django.utils import simplejson
from django.utils.translation import ugettext as _
from django.views.defaults import page_not_found, server_error
from django.views import debug
from django.core.cache import cache

from controller import BaseController
from controller.experimenter import BaseExperimenters, BaseExperimenter
from controller.group import BaseGroups, BaseGroup
from controller.script import BaseScripts, BaseScript
from controller.drivespace import BaseDriveSpace
from controller.uploadfile import BaseUploadFile
from controller.enums import BaseEnums

from forms import LoginForm, ForgottonPasswordForm, ExperimenterForm, \
                   ExperimenterLdapForm, GroupForm, GroupOwnerForm, MyAccountForm, \
                   MyAccountLdapForm, ContainedExperimentersForm, UploadPhotoForm, \
                   EnumerationEntry, EnumerationEntries, ScriptForm

from extlib import gateway

#from extlib.gateway import _session_logout, timeit, getBlitzConnection, _createConnection
from webgateway.views import getBlitzConnection, timeit, _session_logout, _createConnection
from webgateway import views as webgateway_views
logger = logging.getLogger('views-admin')

connectors = {}

logger.info("INIT '%s'" % os.getpid())

################################################################################

def getGuestConnection(host, port):
    conn = None
    guest = ["guest", "guest"]
    try:
        # do not store connection on connectors
        conn = _createConnection('', host=host, port=port, username=guest[0], passwd=guest[1], secure=True)
        if conn is not None:
            logger.info("Have connection as Guest")
        else:
            logger.info("Open connection is not available")
    except Exception, x:
        logger.error(traceback.format_exc())
    return conn

def _checkVersion(host, port):
    import re
    rv = False
    conn = getGuestConnection(host, port)
    if conn is not None:
        try:
            agent = conn.getServerVersion()
            regex = re.compile("^.*?[-]?(\\d+[.]\\d+([.]\\d+)?)[-]?.*?$")

            agent_cleaned = regex.match(agent).group(1)
            agent_split = agent_cleaned.split(".")

            local_cleaned = regex.match(omero_version).group(1)
            local_split = local_cleaned.split(".")

            rv = (agent_split == local_split)
            logger.debug("Client version: '%s'; Server version: '%s'"% (omero_version, agent))
        except Exception, x:
            logger.error(traceback.format_exc())
            error = str(x)
    return rv

################################################################################
# decorators

def isAdminConnected (f):
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
            return HttpResponseRedirect(reverse("walogin")+(("?url=%s") % (url)))
        except Exception, x:
            logger.error(traceback.format_exc())
            return HttpResponseRedirect(reverse("walogin")+(("?error=%s&url=%s") % (str(x),url)))
        if conn is None:
            return HttpResponseRedirect(reverse("walogin")+(("?url=%s") % (url)))
        
        if not conn.isAdmin():
            return page_not_found(request, "404.html")
        kwargs["conn"] = conn
        return f(request, *args, **kwargs)

    return wrapped

def isOwnerConnected (f):
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
            return HttpResponseRedirect(reverse("walogin")+(("?url=%s") % (url)))
        except Exception, x:
            logger.error(traceback.format_exc())
            return HttpResponseRedirect(reverse("walogin")+(("?error=%s&url=%s") % (str(x),url)))
        if conn is None:
            return HttpResponseRedirect(reverse("walogin")+(("?url=%s") % (url)))
        
        if kwargs.get('gid') is not None:
            if not conn.isOwner(kwargs.get('gid')):
                return page_not_found(request, "404.html")
        else:
            if not conn.isOwner():
                return page_not_found(request, "404.html")
        kwargs["conn"] = conn
        return f(request, *args, **kwargs)

    return wrapped

def isUserConnected (f):
    def wrapped (request, *args, **kwargs):
        #this check connection exist, if not it will redirect to login page
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
            return HttpResponseRedirect(reverse("walogin")+(("?url=%s") % (url)))
        except Exception, x:
            logger.error(traceback.format_exc())
            return HttpResponseRedirect(reverse("walogin")+(("?error=%s&url=%s") % (str(x),url)))
        if conn is None:
            return HttpResponseRedirect(reverse("walogin")+(("?url=%s") % (url)))   
        
        kwargs["conn"] = conn
        kwargs["url"] = url
        return f(request, *args, **kwargs)
    
    return wrapped

def isAnythingCreated(f):
    def wrapped (request, *args, **kwargs):
        kwargs["firsttime"] = kwargs["conn"].isAnythingCreated()
        if kwargs['firsttime']:
            kwargs['msg'] = _('User must be in a group - You have not created any groups yet. Click <a href="%s">here</a> to create a group') % (reverse(viewname="wamanagegroupid", args=["new"]))
        #return HttpResponseRedirect(reverse(viewname="wamanagegroupid", args=["new"]))   
        return f(request, *args, **kwargs)

    return wrapped

################################################################################
# views controll

def forgotten_password(request, **kwargs):
    request.session.modified = True
    template = "webadmin/forgotten_password.html"
    
    conn = None
    error = None
    
    if request.method == 'POST' and request.REQUEST.get('server') is not None and request.REQUEST.get('username') is not None and request.REQUEST.get('email') is not None:
        blitz = settings.SERVER_LIST.get(pk=request.REQUEST.get('server'))
        try:
            conn = getGuestConnection(blitz.host, blitz.port)
            if not conn.isForgottenPasswordSet():
                error = "This server cannot reset password. Please contact your administrator."
                conn = None
        except Exception, x:
            logger.error(traceback.format_exc())
            error = str(x)

    if conn is not None:
        controller = None
        try:
            controller = conn.reportForgottenPassword(request.REQUEST.get('username').encode('utf-8'), request.REQUEST.get('email').encode('utf-8'))
        except Exception, x:
            logger.error(traceback.format_exc())
            error = str(x)
        form = ForgottonPasswordForm(data=request.REQUEST.copy())
        context = {'error':error, 'controller':controller, 'form':form}
    else:
        if request.method == 'POST':
            form = ForgottonPasswordForm(data=request.REQUEST.copy())
        else:
            try:
                blitz = settings.SERVER_LIST.get(pk=request.session.get('server'))
                data = {'server': unicode(blitz.id), 'username':unicode(request.REQUEST.get('username')), 'password':unicode(request.REQUEST.get('password')) }
                form = ForgottonPasswordForm(data=data)
            except:
                form = ForgottonPasswordForm()
        context = {'error':error, 'form':form}
    
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

def login(request):
    request.session.modified = True
    
    if request.method == 'POST' and request.REQUEST.get('server'):        
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
    
    error = request.REQUEST.get('error')
    
    conn = None
    try:
        conn = getBlitzConnection(request)
    except Exception, x:
        logger.error(traceback.format_exc())
        error = str(x)
            
    if conn is not None:
        request.session['version'] = conn.getServerVersion()
        return HttpResponseRedirect(reverse("waindex"))
    else:
        if request.method == 'POST' and request.REQUEST.get('server'):
            if not _checkVersion(request.session.get('host'), request.session.get('port')):
                error = "Client version does not match server, please contact administrator."
            else:
                error = "Connection not available, please check your user name and password."

        request.session['server'] = request.REQUEST.get('server')
        
        template = "webadmin/login.html"
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
        context = {'version': omero_version, 'error':error, 'form':form}
        t = template_loader.get_template(template)
        c = Context(request, context)
        rsp = t.render(c)
        return HttpResponse(rsp)

@isUserConnected
@isAnythingCreated
def index(request, **kwargs):    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    if conn.isAdmin():
        if kwargs["firsttime"]:
            return HttpResponseRedirect(reverse("wagroups"))
        else:
            return HttpResponseRedirect(reverse("waexperimenters"))
    else:
        return HttpResponseRedirect(reverse("wamyaccount"))

def logout(request):
    _session_logout(request, request.session['server'])

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
    
    request.session.set_expiry(1)
    return HttpResponseRedirect(reverse("waindex"))

@isAdminConnected
@isAnythingCreated
def experimenters(request, **kwargs):
    experimenters = True
    template = "webadmin/experimenters.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'experimenters':experimenters}
    if kwargs['firsttime']:
        info['message'] = kwargs["msg"]
    
    eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin, 'version': request.session.get('version')}
    controller = BaseExperimenters(conn)
    
    context = {'info':info, 'eventContext':eventContext, 'controller':controller}
    
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isAdminConnected
@isAnythingCreated
def manage_experimenter(request, action, eid=None, **kwargs):
    experimenters = True
    template = "webadmin/experimenter_form.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'experimenters':experimenters}
    if kwargs['firsttime']:
        info['message'] = kwargs["msg"]
    
    eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin, 'version': request.session.get('version')}
    
    controller = BaseExperimenter(conn, eid)
    
    if action == 'new':
        form = ExperimenterForm(initial={'active':True, 'available':controller.otherGroupsInitialList()})
        
        context = {'info':info, 'eventContext':eventContext, 'form':form}
    elif action == 'create':
        if request.method != 'POST':
            return HttpResponseRedirect(reverse(viewname="wamanageexperimenterid", args=["new"]))
        else:
            name_check = conn.checkOmeName(request.REQUEST.get('omename').encode('utf-8'))
            email_check = conn.checkEmail(request.REQUEST.get('email').encode('utf-8'))
            initial={'active':True}
            exclude = list()
            
            if len(request.REQUEST.getlist('other_groups')) > 0:
                others = controller.getSelectedGroups(request.REQUEST.getlist('other_groups'))   
                initial['others'] = others
                initial['default'] = [(g.id, g.name) for g in others]
                exclude.extend([g.id for g in others])
            
            available = controller.otherGroupsInitialList(exclude)
            initial['available'] = available
            form = ExperimenterForm(initial=initial, data=request.REQUEST.copy(), name_check=name_check, email_check=email_check)
            if form.is_valid():
                omeName = request.REQUEST.get('omename').encode('utf-8')
                firstName = request.REQUEST.get('first_name').encode('utf-8')
                middleName = request.REQUEST.get('middle_name').encode('utf-8')
                lastName = request.REQUEST.get('last_name').encode('utf-8')
                email = request.REQUEST.get('email').encode('utf-8')
                institution = request.REQUEST.get('institution').encode('utf-8')
                admin = False
                if request.REQUEST.get('administrator'):
                    admin = True

                active = False
                if request.REQUEST.get('active'):
                    active = True

                defaultGroup = request.REQUEST.get('default_group')
                otherGroups = request.POST.getlist('other_groups')
                password = request.REQUEST.get('password').encode('utf-8')
                controller.createExperimenter(omeName, firstName, lastName, email, admin, active, defaultGroup, otherGroups, password, middleName, institution)
                return HttpResponseRedirect(reverse("waexperimenters"))
            context = {'info':info, 'eventContext':eventContext, 'form':form}
    elif action == 'edit' :
        if controller.ldapAuth == "" or controller.ldapAuth is None:
            
            initial={'omename': controller.experimenter.omeName, 'first_name':controller.experimenter.firstName,
                                    'middle_name':controller.experimenter.middleName, 'last_name':controller.experimenter.lastName,
                                    'email':controller.experimenter.email, 'institution':controller.experimenter.institution,
                                    'administrator': controller.experimenter.isAdmin(), 'active': controller.experimenter.isActive(), 
                                    'default_group': controller.defaultGroup, 'other_groups':controller.otherGroups}
            
            initial['default'] = controller.default
            others = controller.others
            initial['others'] = others
            if len(others) > 0:
                exclude = [g.id.val for g in others]
            else:
                exclude = [controller.defaultGroup]
            available = controller.otherGroupsInitialList(exclude)
            initial['available'] = available
            form = ExperimenterForm(initial=initial)
        else:
            initial={'omename': controller.experimenter.omeName, 'first_name':controller.experimenter.firstName,
                                    'middle_name':controller.experimenter.middleName, 'last_name':controller.experimenter.lastName,
                                    'email':controller.experimenter.email, 'institution':controller.experimenter.institution,
                                    'administrator': controller.experimenter.isAdmin(), 'active': controller.experimenter.isActive(), 
                                    'default_group': controller.defaultGroup, 'other_groups':controller.otherGroups}
            
            initial['default'] = controller.default
            others = controller.others
            initial['others'] = others
            if len(others) > 0:
                exclude = [g.id.val for g in others]
            else:
                exclude = [controller.defaultGroup]
            available = controller.otherGroupsInitialList(exclude)
            initial['available'] = available
            form = ExperimenterLdapForm(initial=initial)
        context = {'info':info, 'eventContext':eventContext, 'form':form, 'eid': eid, 'ldapAuth': controller.ldapAuth}
    elif action == 'save':
        if request.method != 'POST':
            return HttpResponseRedirect(reverse(viewname="wamanageexperimenterid", args=["edit", controller.experimenter.id]))
        else:
            name_check = conn.checkOmeName(request.REQUEST.get('omename').encode('utf-8'), controller.experimenter.omeName)
            email_check = conn.checkEmail(request.REQUEST.get('email').encode('utf-8'), controller.experimenter.email)
            initial={'active':True}
            exclude = list()
            
            if len(request.REQUEST.getlist('other_groups')) > 0:
                others = controller.getSelectedGroups(request.REQUEST.getlist('other_groups'))   
                initial['others'] = others
                initial['default'] = [(g.id, g.name) for g in others]
                exclude.extend([g.id for g in others])
            
            available = controller.otherGroupsInitialList(exclude)
            initial['available'] = available
            
            if controller.ldapAuth == "" or controller.ldapAuth is None:
                form = ExperimenterForm(initial=initial, data=request.POST.copy(), name_check=name_check, email_check=email_check)
            else:
                form = ExperimenterLdapForm(initial=initial, data=request.POST.copy(), name_check=name_check, email_check=email_check)
                
            if form.is_valid():
                omeName = request.REQUEST.get('omename').encode('utf-8')
                firstName = request.REQUEST.get('first_name').encode('utf-8')
                middleName = request.REQUEST.get('middle_name').encode('utf-8')
                lastName = request.REQUEST.get('last_name').encode('utf-8')
                email = request.REQUEST.get('email').encode('utf-8')
                institution = request.REQUEST.get('institution').encode('utf-8')
                admin = False
                if request.REQUEST.get('administrator'):
                    admin = True

                active = False
                if request.REQUEST.get('active'):
                    active = True

                defaultGroup = request.REQUEST.get('default_group')
                otherGroups = request.POST.getlist('other_groups')
                try:
                    password = request.REQUEST.get('password').encode('utf-8')
                    if len(password) == 0:
                        password = None
                except:
                    password = None
                controller.updateExperimenter(omeName, firstName, lastName, email, admin, active, defaultGroup, otherGroups, middleName, institution, password)
                return HttpResponseRedirect(reverse("waexperimenters"))
            context = {'info':info, 'eventContext':eventContext, 'form':form, 'eid': eid, 'ldapAuth': controller.ldapAuth}
    elif action == "delete":
        controller.deleteExperimenter()
        return HttpResponseRedirect(reverse("waexperimenters"))
    else:
        return HttpResponseRedirect(reverse("waexperimenters"))
    
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isAdminConnected
@isAnythingCreated
def groups(request, **kwargs):
    groups = True
    template = "webadmin/groups.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'groups':groups}
    if kwargs['firsttime']:
        info['message'] = kwargs["msg"]
    
    eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin, 'version': request.session.get('version')}
    controller = BaseGroups(conn)
    
    context = {'info':info, 'eventContext':eventContext, 'controller':controller}
    
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isAdminConnected
@isAnythingCreated
def manage_group(request, action, gid=None, **kwargs):
    groups = True
    template = "webadmin/group_form.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'groups':groups}
    if kwargs['firsttime']:
        info['message'] = kwargs["msg"]
    
    eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin, 'version': request.session.get('version')}
    
    controller = BaseGroup(conn, gid)
    
    if action == 'new':
        form = GroupForm(initial={'experimenters':controller.experimenters, 'access_controll': 0})
        context = {'info':info, 'eventContext':eventContext, 'form':form}
    elif action == 'create':
        if request.method != 'POST':
            return HttpResponseRedirect(reverse(viewname="wamanagegroupid", args=["new"]))
        else:
            name_check = conn.checkGroupName(request.REQUEST.get('name').encode('utf-8'))
            form = GroupForm(initial={'experimenters':controller.experimenters}, data=request.POST.copy(), name_check=name_check)
            if form.is_valid():
                name = request.REQUEST.get('name').encode('utf-8')
                description = request.REQUEST.get('description').encode('utf-8')
                owners = request.POST.getlist('owners')
                permissions = request.REQUEST.get('access_controll')                
                readonly = request.REQUEST.get('readonly') is None and True or False  
                controller.createGroup(name, owners, permissions, readonly, description)
                return HttpResponseRedirect(reverse("wagroups"))
            context = {'info':info, 'eventContext':eventContext, 'form':form}
    elif action == 'edit':
        access_controll = controller.getActualPermissions()
        form = GroupForm(initial={'name': controller.group.name, 'description':controller.group.description,
                                     'access_controll': access_controll, 'readonly': controller.isReadOnly(), 
                                     'owners': controller.owners, 'experimenters':controller.experimenters})
        context = {'info':info, 'eventContext':eventContext, 'form':form, 'gid': gid, 'access_controll': access_controll}
    elif action == 'save':
        if request.method != 'POST':
            return HttpResponseRedirect(reverse(viewname="wamanagegroupid", args=["edit", controller.group.id]))
        else:
            name_check = conn.checkGroupName(request.REQUEST.get('name').encode('utf-8'), controller.group.name)
            form = GroupForm(initial={'experimenters':controller.experimenters}, data=request.POST.copy(), name_check=name_check)
            if form.is_valid():
                name = request.REQUEST.get('name').encode('utf-8')
                description = request.REQUEST.get('description').encode('utf-8')
                owners = request.POST.getlist('owners')
                permissions = request.REQUEST.get('access_controll').encode('utf-8')
                readonly = request.REQUEST.get('readonly') is None and True or False
                controller.updateGroup(name, owners, permissions, readonly, description)
                return HttpResponseRedirect(reverse("wagroups"))
            context = {'info':info, 'eventContext':eventContext, 'form':form, 'gid': gid}
    elif action == "update":
        template = "webadmin/group_edit.html"
        controller.containedExperimenters()
        form = ContainedExperimentersForm(initial={'members':controller.members, 'available':controller.available})
        if not form.is_valid():
            available = request.POST.getlist('available')
            members = request.POST.getlist('members')
            controller.setMembersOfGroup(available, members)
            return HttpResponseRedirect(reverse("wagroups"))
        context = {'info':info, 'eventContext':eventContext, 'form':form, 'controller': controller}
    elif action == "members":
        template = "webadmin/group_edit.html"
        controller.containedExperimenters()
        form = ContainedExperimentersForm(initial={'members':controller.members, 'available':controller.available})
        context = {'info':info, 'eventContext':eventContext, 'form':form, 'controller': controller}
    else:
        return HttpResponseRedirect(reverse("wagroups"))
    
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isOwnerConnected
def manage_group_owner(request, action, gid, **kwargs):
    myaccount = True
    template = "webadmin/group_form_owner.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'myaccount':myaccount}
    eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin, 'version': request.session.get('version')}
    
    controller = BaseGroup(conn, gid)
    
    if action == 'edit':
        access_controll = controller.getActualPermissions()
        form = GroupOwnerForm(initial={'access_controll': access_controll, 'readonly': controller.isReadOnly()})
        context = {'info':info, 'eventContext':eventContext, 'form':form, 'gid': gid, 'access_controll': access_controll, 'group':controller.group, 'owners':controller.getOwnersNames()}
    elif action == "save":
        if request.method != 'POST':
            return HttpResponseRedirect(reverse(viewname="wamyaccount", args=["edit", controller.group.id]))
        else:
            form = GroupOwnerForm(data=request.POST.copy())
            if form.is_valid():
                permissions = request.REQUEST.get('access_controll')                
                readonly = request.REQUEST.get('readonly') is None and True or False
                controller.updatePermissions(permissions, readonly)
                return HttpResponseRedirect(reverse("wamyaccount"))
            context = {'info':info, 'eventContext':eventContext, 'form':form, 'gid': gid}
    else:
        return HttpResponseRedirect(reverse("wamyaccount"))
    
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isAdminConnected
def ldap(request, **kwargs):
    scripts = True
    template = "webadmin/ldap_search.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'scripts':scripts}
    eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin, 'version': request.session.get('version')}
    controller = None
    
    context = {'info':info, 'eventContext':eventContext, 'controller':controller}
    
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isAdminConnected
def scripts(request, **kwargs):
    scripts = True
    template = "webadmin/scripts.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'scripts':scripts}
    eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin, 'version': request.session.get('version')}
    controller = BaseScripts(conn)
    
    context = {'info':info, 'eventContext':eventContext, 'controller':controller}
    
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isAdminConnected
def manage_script(request, action, sc_id=None, **kwargs):
    scripts = True
    template = "webadmin/script_form.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'scripts':scripts}
    eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin, 'version': request.session.get('version')}
    controller = BaseScript(conn)
    
    if action == 'new':
        form = ScriptForm(initial={'script':controller.script})
        context = {'info':info, 'eventContext':eventContext, 'form':form}
    elif action == 'save':
        form = GroupForm(initial={'script':controller.script}, data=request.POST.copy())
        if form.is_valid():
            
            return HttpResponseRedirect(reverse("wascripts"))
        context = {'info':info, 'eventContext':eventContext, 'form':form}
    elif action == "edit":
        controller.getScript(sc_id)
        form = ScriptForm(initial={'name':controller.details.val.path.val, 'content':controller.script, 'size':controller.details.val.size.val})
        context = {'info':info, 'eventContext':eventContext, 'form':form, 'sc_id': sc_id}
    else:
        return HttpResponseRedirect(reverse("wascripts"))
    
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isAdminConnected
def enums(request, **kwargs):
    enums = True
    template = "webadmin/enums.html"
    error = request.REQUEST.get('error') and request.REQUEST.get('error').replace("_", " ") or None
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'enums':enums, 'error':error}
    eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin, 'version': request.session.get('version')}
    
    controller = BaseEnums(conn)
    
    context = {'info':info, 'eventContext':eventContext, 'controller':controller}
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isAdminConnected
def manage_enum(request, action, klass, eid=None, **kwargs):
    enums = True
    template = "webadmin/enum_form.html"
        
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'enums':enums}
    eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin, 'version': request.session.get('version')}
    
    controller = BaseEnums(conn, klass)
    if action == "save":
        form = EnumerationEntries(entries=controller.entries, data=request.POST.copy())
        if form.is_valid():
            controller.saveEntries(form.data)
            return HttpResponseRedirect(reverse(viewname="wamanageenum", args=["edit", klass]))
    elif action == "delete" and eid is not None:
        controller.deleteEntry(eid)
        return HttpResponseRedirect(reverse(viewname="wamanageenum", args=["edit", klass]))
    elif action == "new":
        if request.method == "POST":
            form = EnumerationEntry(data=request.POST.copy())
            if form.is_valid():
                new_entry = request.REQUEST.get('new_entry').encode('utf-8')
                controller.saveEntry(new_entry)
                return HttpResponseRedirect(reverse(viewname="wamanageenum", args=["edit", klass]))
        else:
            form = EnumerationEntry()
    elif action == "reset":
        try:
            controller.resetEnumerations()
        except:
            logger.error(traceback.format_exc())
            return HttpResponseRedirect(reverse(viewname="waenums")+("?error=Enumeration_%s_cannot_be_reset" % (klass)))
        else:
            return HttpResponseRedirect(reverse("waenums"))
    else:
        form = EnumerationEntries(entries=controller.entries, initial={'entries':True})
    
    context = {'info':info, 'eventContext':eventContext, 'controller':controller, 'action':action, 'form':form}
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isAdminConnected
def imports(request, **kwargs):
    return HttpResponseRedirect(reverse("waindex"))

@isUserConnected
def my_account(request, action=None, **kwargs):
    myaccount = True
    template = "webadmin/myaccount.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'myaccount':myaccount}
    eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin, 'version': request.session.get('version')}
    
    myaccount = BaseExperimenter(conn)
    myaccount.getMyDetails()
    myaccount.getOwnedGroups()
    
    edit_mode = False
    photo_size = None
    form = None
    form_file = UploadPhotoForm()    
    
    if action == "save":
        if request.method != 'POST':
            return HttpResponseRedirect(reverse(viewname="wamyaccount", args=["edit"]))
        else:
            email_check = conn.checkEmail(request.REQUEST.get('email').encode('utf-8'), myaccount.experimenter.email)
            if myaccount.ldapAuth == "" or myaccount.ldapAuth is None:
                form = MyAccountForm(data=request.POST.copy(), initial={'groups':myaccount.otherGroups}, email_check=email_check)
            else:
                form = MyAccountLdapForm(data=request.POST.copy(), initial={'groups':myaccount.otherGroups}, email_check=email_check)
            if form.is_valid():
                firstName = request.REQUEST.get('first_name').encode('utf-8')
                middleName = request.REQUEST.get('middle_name').encode('utf-8')
                lastName = request.REQUEST.get('last_name').encode('utf-8')
                email = request.REQUEST.get('email').encode('utf-8')
                institution = request.REQUEST.get('institution').encode('utf-8')
                defaultGroup = request.REQUEST.get('default_group')
                password = request.REQUEST.get('password').encode('utf-8')
                if len(password) == 0:
                    password = None
                myaccount.updateMyAccount(firstName, lastName, email, defaultGroup, middleName, institution, password)
                logout(request)
                return HttpResponseRedirect(reverse("wamyaccount"))
    elif action == "upload":
        if request.method == 'POST':
            form_file = UploadPhotoForm(request.POST, request.FILES)
            if form_file.is_valid():
                controller = BaseUploadFile(conn)
                controller.attach_photo(request.FILES['photo'])
                return HttpResponseRedirect(reverse("wamyaccount"))
    elif action == "crop": 
        x1 = long(request.REQUEST.get('x1').encode('utf-8'))
        x2 = long(request.REQUEST.get('x2').encode('utf-8'))
        y1 = long(request.REQUEST.get('y1').encode('utf-8'))
        y2 = long(request.REQUEST.get('y2').encode('utf-8'))
        box = (x1,y1,x2,y2)
        conn.cropExperimenterPhoto(box)
        return HttpResponseRedirect(reverse("wamyaccount"))
    elif action == "editphoto":
        if myaccount.ldapAuth == "" or myaccount.ldapAuth is None:
            form = MyAccountForm(initial={'omename': myaccount.experimenter.omeName, 'first_name':myaccount.experimenter.firstName,
                                    'middle_name':myaccount.experimenter.middleName, 'last_name':myaccount.experimenter.lastName,
                                    'email':myaccount.experimenter.email, 'institution':myaccount.experimenter.institution,
                                    'default_group':myaccount.defaultGroup, 'groups':myaccount.otherGroups})
        else:
            form = MyAccountLdapForm(initial={'omename': myaccount.experimenter.omeName, 'first_name':myaccount.experimenter.firstName,
                                    'middle_name':myaccount.experimenter.middleName, 'last_name':myaccount.experimenter.lastName,
                                    'email':myaccount.experimenter.email, 'institution':myaccount.experimenter.institution,
                                    'default_group':myaccount.defaultGroup, 'groups':myaccount.otherGroups})
        
        photo_size = conn.getExperimenterPhotoSize()        
        if photo_size is not None:
            edit_mode = True
    else:
        photo_size = conn.getExperimenterPhotoSize()        
        if myaccount.ldapAuth == "" or myaccount.ldapAuth is None:
            form = MyAccountForm(initial={'omename': myaccount.experimenter.omeName, 'first_name':myaccount.experimenter.firstName,
                                    'middle_name':myaccount.experimenter.middleName, 'last_name':myaccount.experimenter.lastName,
                                    'email':myaccount.experimenter.email, 'institution':myaccount.experimenter.institution,
                                    'default_group':myaccount.defaultGroup, 'groups':myaccount.otherGroups})
        else:
            form = MyAccountLdapForm(initial={'omename': myaccount.experimenter.omeName, 'first_name':myaccount.experimenter.firstName,
                                    'middle_name':myaccount.experimenter.middleName, 'last_name':myaccount.experimenter.lastName,
                                    'email':myaccount.experimenter.email, 'institution':myaccount.experimenter.institution,
                                    'default_group':myaccount.defaultGroup, 'groups':myaccount.otherGroups})
    
    context = {'info':info, 'eventContext':eventContext, 'form':form, 'form_file':form_file, 'ldapAuth': myaccount.ldapAuth, 'edit_mode':edit_mode, 'photo_size':photo_size, 'myaccount':myaccount}
    t = template_loader.get_template(template)
    c = Context(request,context)
    return HttpResponse(t.render(c))

@isUserConnected
def myphoto(request, **kwargs):
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    photo = conn.getExperimenterPhoto()
    return HttpResponse(photo, mimetype='image/jpeg')

@isUserConnected
def drivespace(request, **kwargs):
    drivespace = True
    template = "webadmin/drivespace.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'drivespace':drivespace}
    eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin, 'version': request.session.get('version')}
    controller = BaseDriveSpace(conn)
    controller.usersData()
    
    context = {'info':info, 'eventContext':eventContext, 'driveSpace': {'free':controller.freeSpace, 'used':controller.usedSpace }, 'usage':controller.usage}
    
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isUserConnected
def piechart(request, **kwargs):
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    from cStringIO import StringIO
    
    try:
        from matplotlib.backends.backend_agg import FigureCanvasAgg as FigureCanvas
        import numpy as np
        import matplotlib.pyplot as plt
        from pylab import * 
    except Exception, x:
        logger.error(traceback.format_exc())
        rv = "Error: %s" % x.message
        return HttpResponse(rv)
    
    controller = BaseDriveSpace(conn)
    controller.pieChartData() 
    
    keys, values = list(), list()
    for item in controller.usage:
        keys.append(str(item[0]))
        values.append(long(item[1]))
    
    explode = list()
    explode.append(0.1)
    for e in range(0, len(keys)):
        explode.append(0)
    explode.remove(0)
    
    # make a square figure and axes
    fig = plt.figure()
    ax = axes([0.1, 0.1, 0.8, 0.8])

    labels = labels=tuple(keys)
    explode = tuple(explode)

    plt.pie(values, labels=labels, explode=explode, autopct='%1.1f%%', shadow=False)
    plt.title(_("Repository information status"))
    plt.grid(True)
    canvas = FigureCanvas(fig)
    imdata = StringIO()
    canvas.print_figure(imdata)
    return HttpResponse(imdata.getvalue(), mimetype='image/png')

