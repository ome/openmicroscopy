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
import re

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
from django.utils.encoding import smart_str

from webclient.webclient_gateway import OmeroWebGateway

from forms import LoginForm, ForgottonPasswordForm, ExperimenterForm, \
                   GroupForm, GroupOwnerForm, MyAccountForm, ChangePassword, \
                   ContainedExperimentersForm, UploadPhotoForm, \
                   EnumerationEntry, EnumerationEntries

from controller import BaseController
from controller.experimenter import BaseExperimenters, BaseExperimenter
from controller.group import BaseGroups, BaseGroup
from controller.drivespace import BaseDriveSpace, usersData
from controller.uploadfile import BaseUploadFile
from controller.enums import BaseEnums

from omeroweb.webclient.views import _session_logout
from omeroweb.webadmin.webadmin_utils import _checkVersion, _isServerOn, toBoolean, upgradeCheck, getGuestConnection
from omeroweb.webgateway.views import getBlitzConnection

from omeroweb.webadmin.custom_models import Server

logger = logging.getLogger('views-admin')

connectors = {}

logger.info("INIT '%s'" % os.getpid())

################################################################################
# decorators

def isAdminConnected (f):
    def wrapped (request, *args, **kwargs):
        #this check the connection exist, if not it will redirect to login page
        url = request.REQUEST.get('url')
        if url is None or len(url) == 0:
            url = request.get_full_path()
        
        conn = None
        try:
            conn = getBlitzConnection(request, useragent="OMERO.webadmin")
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
        if url is None or len(url) == 0:
            url = request.get_full_path()
        
        conn = None
        try:
            conn = getBlitzConnection(request, useragent="OMERO.webadmin")
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
        if url is None or len(url) == 0:
            url = request.get_full_path()
        
        conn = None
        try:
            conn = getBlitzConnection(request, useragent="OMERO.webadmin")
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
    blitz = None
    
    if request.method == 'POST':
        form = ForgottonPasswordForm(data=request.REQUEST.copy())
        if form.is_valid():
            blitz = Server.get(pk=request.REQUEST.get('server'))
            try:
                conn = getGuestConnection(blitz.host, blitz.port)
                if not conn.isForgottenPasswordSet():
                    error = "This server cannot reset password. Please contact your administrator."
                    conn = None
            except Exception, x:
                logger.error(traceback.format_exc())
                error = "Internal server error, please contact administrator."
        
            if conn is not None:
                try:
                    conn.reportForgottenPassword(smart_str(request.REQUEST.get('username')), smart_str(request.REQUEST.get('email')))
                    error = "Password was reseted. Check you mailbox."
                    form = None
                except Exception, x:
                    logger.error(traceback.format_exc())
                    error = "Internal server error, please contact administrator."
    else:
        form = ForgottonPasswordForm()
    
    context = {'error':error, 'form':form}    
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

def login(request):
    request.session.modified = True
    
    if request.method == 'POST' and request.REQUEST.get('server'):        
        blitz = Server.get(pk=request.REQUEST.get('server')) 
        request.session['server'] = blitz.id
        request.session['host'] = blitz.host
        request.session['port'] = blitz.port
        request.session['username'] = smart_str(request.REQUEST.get('username'))
        request.session['password'] = smart_str(request.REQUEST.get('password'))
        request.session['ssl'] = (True, False)[request.REQUEST.get('ssl') is None]
        
    error = request.REQUEST.get('error')
    
    conn = None
    # TODO: version check should be done on the low level, see #5983
    if _checkVersion(request.session.get('host'), request.session.get('port')):
        try:
            conn = getBlitzConnection(request, useragent="OMERO.webadmin")
        except Exception, x:
            logger.error(traceback.format_exc())
            error = str(x)
    
    if conn is not None:
        upgradeCheck()
        request.session['version'] = conn.getServerVersion()
        return HttpResponseRedirect(reverse("waindex"))
    else:
        if request.method == 'POST' and request.REQUEST.get('server'):
            if not _isServerOn(request.session.get('host'), request.session.get('port')):
                error = "Server is not responding, please contact administrator."
            elif not _checkVersion(request.session.get('host'), request.session.get('port')):
                error = "Client version does not match server, please contact administrator."
            else:
                error = "Connection not available, please check your user name and password."

        request.session['server'] = request.REQUEST.get('server')
        
        template = "webadmin/login.html"
        if request.method == 'POST':
            form = LoginForm(data=request.REQUEST.copy())
        else:
            blitz = Server.get(pk=request.session.get('server')) 
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


@isUserConnected
def logout(request, **kwargs):
    _session_logout(request, request.session.get('server'))
    #request.session.set_expiry(1)
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
        form = ExperimenterForm(initial={'with_password':True, 'active':True, 'available':controller.otherGroupsInitialList()})        
        context = {'info':info, 'eventContext':eventContext, 'form':form}
    elif action == 'create':
        if request.method != 'POST':
            return HttpResponseRedirect(reverse(viewname="wamanageexperimenterid", args=["new"]))
        else:
            name_check = conn.checkOmeName(request.REQUEST.get('omename'))
            email_check = conn.checkEmail(request.REQUEST.get('email'))
            
            initial={'with_password':True}
            
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
                logger.debug("Create experimenter form:" + str(form.cleaned_data))
                omename = form.cleaned_data['omename']
                firstName = form.cleaned_data['first_name']
                middleName = form.cleaned_data['middle_name']
                lastName = form.cleaned_data['last_name']
                email = form.cleaned_data['email']
                institution = form.cleaned_data['institution']
                admin = toBoolean(form.cleaned_data['administrator'])
                active = toBoolean(form.cleaned_data['active'])
                defaultGroup = form.cleaned_data['default_group']
                otherGroups = form.cleaned_data['other_groups']
                password = form.cleaned_data['password']
                controller.createExperimenter(omename, firstName, lastName, email, admin, active, defaultGroup, otherGroups, password, middleName, institution)
                return HttpResponseRedirect(reverse("waexperimenters"))
            context = {'info':info, 'eventContext':eventContext, 'form':form}
    elif action == 'edit' :
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
        
        context = {'info':info, 'eventContext':eventContext, 'form':form, 'eid': eid, 'ldapAuth': controller.ldapAuth}
    elif action == 'save':
        if request.method != 'POST':
            return HttpResponseRedirect(reverse(viewname="wamanageexperimenterid", args=["edit", controller.experimenter.id]))
        else:            
            name_check = conn.checkOmeName(request.REQUEST.get('omename'), controller.experimenter.omeName)
            email_check = conn.checkEmail(request.REQUEST.get('email'), controller.experimenter.email)
            initial={'active':True}
            exclude = list()
            
            if len(request.REQUEST.getlist('other_groups')) > 0:
                others = controller.getSelectedGroups(request.REQUEST.getlist('other_groups'))   
                initial['others'] = others
                initial['default'] = [(g.id, g.name) for g in others]
                exclude.extend([g.id for g in others])
            
            available = controller.otherGroupsInitialList(exclude)
            initial['available'] = available
            
            form = ExperimenterForm(initial=initial, data=request.POST.copy(), name_check=name_check, email_check=email_check)
               
            if form.is_valid():
                logger.debug("Update experimenter form:" + str(form.cleaned_data))
                omename = form.cleaned_data['omename']
                firstName = form.cleaned_data['first_name']
                middleName = form.cleaned_data['middle_name']
                lastName = form.cleaned_data['last_name']
                email = form.cleaned_data['email']
                institution = form.cleaned_data['institution']
                admin = toBoolean(form.cleaned_data['administrator'])
                active = toBoolean(form.cleaned_data['active'])
                defaultGroup = form.cleaned_data['default_group']
                otherGroups = form.cleaned_data['other_groups']
                controller.updateExperimenter(omename, firstName, lastName, email, admin, active, defaultGroup, otherGroups, middleName, institution)
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

@isUserConnected
def manage_password(request, eid, **kwargs):
    experimenters = True
    template = "webadmin/password.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'experimenters':experimenters}

    eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin, 'version': request.session.get('version')}
    
    error = None
    if request.method != 'POST':
        password_form = ChangePassword()
    else:
        password_form = ChangePassword(data=request.POST.copy())            
        if password_form.is_valid():
            old_password = password_form.cleaned_data['old_password']
            password = password_form.cleaned_data['password']
            if conn.isAdmin():
                exp = conn.getObject("Experimenter", eid)
                try:
                    conn.changeUserPassword(exp.omeName, password, old_password)
                except Exception, x:
                    error = x.message
                else:
                    request.session['password'] = password
                    return HttpResponseRedirect(reverse(viewname="wamanageexperimenterid", args=["edit", eid]))
            else:
                try:
                    conn.changeMyPassword(password, old_password) 
                except Exception, x:
                    error = x.message
                else:
                    request.session['password'] = password
                    return HttpResponseRedirect(reverse("wamyaccount"))
                
    context = {'info':info, 'error':error, 'eventContext':eventContext, 'password_form':password_form, 'eid': eid}
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
        form = GroupForm(initial={'experimenters':controller.experimenters, 'permissions': 0})
        context = {'info':info, 'eventContext':eventContext, 'form':form}
    elif action == 'create':
        if request.method != 'POST':
            return HttpResponseRedirect(reverse(viewname="wamanagegroupid", args=["new"]))
        else:
            name_check = conn.checkGroupName(request.REQUEST.get('name'))
            form = GroupForm(initial={'experimenters':controller.experimenters}, data=request.POST.copy(), name_check=name_check)
            if form.is_valid():
                logger.debug("Create group form:" + str(form.cleaned_data))
                name = form.cleaned_data['name']
                description = form.cleaned_data['description']
                owners = form.cleaned_data['owners']
                permissions = form.cleaned_data['permissions']
                readonly = toBoolean(form.cleaned_data['readonly'])
                controller.createGroup(name, owners, permissions, readonly, description)
                return HttpResponseRedirect(reverse("wagroups"))
            context = {'info':info, 'eventContext':eventContext, 'form':form}
    elif action == 'edit':
        permissions = controller.getActualPermissions()
        form = GroupForm(initial={'name': controller.group.name, 'description':controller.group.description,
                                     'permissions': permissions, 'readonly': controller.isReadOnly(), 
                                     'owners': controller.owners, 'experimenters':controller.experimenters})
        context = {'info':info, 'eventContext':eventContext, 'form':form, 'gid': gid, 'permissions': permissions}
    elif action == 'save':
        if request.method != 'POST':
            return HttpResponseRedirect(reverse(viewname="wamanagegroupid", args=["edit", controller.group.id]))
        else:
            name_check = conn.checkGroupName(request.REQUEST.get('name'), controller.group.name)
            form = GroupForm(initial={'experimenters':controller.experimenters}, data=request.POST.copy(), name_check=name_check)
            if form.is_valid():
                logger.debug("Update group form:" + str(form.cleaned_data))
                name = form.cleaned_data['name']
                description = form.cleaned_data['description']
                owners = form.cleaned_data['owners']
                permissions = form.cleaned_data['permissions']
                readonly = toBoolean(form.cleaned_data['readonly'])
                controller.updateGroup(name, owners, permissions, readonly, description)
                return HttpResponseRedirect(reverse("wagroups"))
            context = {'info':info, 'eventContext':eventContext, 'form':form, 'gid': gid}
    elif action == "update":
        template = "webadmin/group_edit.html"
        controller.containedExperimenters()
        form = ContainedExperimentersForm(initial={'members':controller.members, 'available':controller.available})
        if not form.is_valid():
            #available = form.cleaned_data['available']
            available = request.POST.getlist('available')
            #members = form.cleaned_data['members']
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
        permissions = controller.getActualPermissions()
        form = GroupOwnerForm(initial={'permissions': permissions, 'readonly': controller.isReadOnly()})
        context = {'info':info, 'eventContext':eventContext, 'form':form, 'gid': gid, 'permissions': permissions, 'group':controller.group, 'owners':controller.getOwnersNames()}
    elif action == "save":
        if request.method != 'POST':
            return HttpResponseRedirect(reverse(viewname="wamyaccount", args=["edit", controller.group.id]))
        else:
            form = GroupOwnerForm(data=request.POST.copy())
            if form.is_valid():
                permissions = form.cleaned_data['permissions']
                readonly = toBoolean(form.cleaned_data['readonly'])
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

#@isAdminConnected
#def enums(request, **kwargs):
#    enums = True
#    template = "webadmin/enums.html"
#    error = request.REQUEST.get('error') and request.REQUEST.get('error').replace("_", " ") or None
#    
#    conn = None
#    try:
#        conn = kwargs["conn"]
#    except:
#        logger.error(traceback.format_exc())
#    
#    info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'enums':enums, 'error':error}
#    eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin, 'version': request.session.get('version')}
#    
#    controller = BaseEnums(conn)
#    
#    context = {'info':info, 'eventContext':eventContext, 'controller':controller}
#    t = template_loader.get_template(template)
#    c = Context(request, context)
#    rsp = t.render(c)
#    return HttpResponse(rsp)

#@isAdminConnected
#def manage_enum(request, action, klass, eid=None, **kwargs):
#    enums = True
#    template = "webadmin/enum_form.html"
#        
#    conn = None
#    try:
#        conn = kwargs["conn"]
#    except:
#        logger.error(traceback.format_exc())
#    
#    info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'enums':enums}
#    eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin, 'version': request.session.get('version')}
#    
#    controller = BaseEnums(conn, klass)
#    if action == "save":
#        form = EnumerationEntries(entries=controller.entries, data=request.POST.copy())
#        if form.is_valid():
#            controller.saveEntries(form.data)
#            return HttpResponseRedirect(reverse(viewname="wamanageenum", args=["edit", klass]))
#    elif action == "delete" and eid is not None:
#        controller.deleteEntry(eid)
#        return HttpResponseRedirect(reverse(viewname="wamanageenum", args=["edit", klass]))
#    elif action == "new":
#        if request.method == "POST":
#            form = EnumerationEntry(data=request.POST.copy())
#            if form.is_valid():
#                new_entry = form.cleaned_data['new_entry]
#                controller.saveEntry(new_entry)
#                return HttpResponseRedirect(reverse(viewname="wamanageenum", args=["edit", klass]))
#        else:
#            form = EnumerationEntry()
#    elif action == "reset":
#        try:
#            controller.resetEnumerations()
#        except:
#            logger.error(traceback.format_exc())
#            return HttpResponseRedirect(reverse(viewname="waenums")+("?error=Enumeration_%s_cannot_be_reset" % (klass)))
#        else:
#            return HttpResponseRedirect(reverse("waenums"))
#    else:
#        form = EnumerationEntries(entries=controller.entries, initial={'entries':True})
#    
#    context = {'info':info, 'eventContext':eventContext, 'controller':controller, 'action':action, 'form':form}
#    t = template_loader.get_template(template)
#    c = Context(request, context)
#    rsp = t.render(c)
#    return HttpResponse(rsp)

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
    eventContext = {'userId':conn.getEventContext().userId,'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin, 'version': request.session.get('version')}
    
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
            email_check = conn.checkEmail(request.REQUEST.get('email'), myaccount.experimenter.email)
            form = MyAccountForm(data=request.POST.copy(), initial={'groups':myaccount.otherGroups}, email_check=email_check)
            if form.is_valid():
                firstName = form.cleaned_data['first_name']
                middleName = form.cleaned_data['middle_name']
                lastName = form.cleaned_data['last_name']
                email = form.cleaned_data['email']
                institution = form.cleaned_data['institution']
                defaultGroup = form.cleaned_data['default_group']
                myaccount.updateMyAccount(firstName, lastName, email, defaultGroup, middleName, institution)
                return HttpResponseRedirect(reverse("wamyaccount"))
    
    elif action == "upload":
        if request.method == 'POST':
            form_file = UploadPhotoForm(request.POST, request.FILES)
            if form_file.is_valid():
                controller = BaseUploadFile(conn)
                controller.attach_photo(request.FILES['photo'])
                return HttpResponseRedirect(reverse("wamyaccount"))
    elif action == "crop": 
        x1 = long(request.REQUEST.get('x1'))
        x2 = long(request.REQUEST.get('x2'))
        y1 = long(request.REQUEST.get('y1'))
        y2 = long(request.REQUEST.get('y2'))
        box = (x1,y1,x2,y2)
        conn.cropExperimenterPhoto(box)
        return HttpResponseRedirect(reverse("wamyaccount"))
    elif action == "editphoto":
        form = MyAccountForm(initial={'omename': myaccount.experimenter.omeName, 'first_name':myaccount.experimenter.firstName,
                                    'middle_name':myaccount.experimenter.middleName, 'last_name':myaccount.experimenter.lastName,
                                    'email':myaccount.experimenter.email, 'institution':myaccount.experimenter.institution,
                                    'default_group':myaccount.defaultGroup, 'groups':myaccount.otherGroups})
        
        photo_size = conn.getExperimenterPhotoSize()
        if photo_size is not None:
            edit_mode = True
    
    photo_size = conn.getExperimenterPhotoSize()
    form = MyAccountForm(initial={'omename': myaccount.experimenter.omeName, 'first_name':myaccount.experimenter.firstName,
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
        
    context = {'info':info, 'eventContext':eventContext, 'driveSpace': {'free':controller.freeSpace, 'used':controller.usedSpace }}
    
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)


@isUserConnected
def load_drivespace(request, **kwargs):
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        return handlerInternalError("Connection is not available. Please contact your administrator.")
    
    offset = request.REQUEST.get('offset', 0)
    rv = usersData(conn, offset)
    return HttpResponse(simplejson.dumps(rv),mimetype='application/json')
