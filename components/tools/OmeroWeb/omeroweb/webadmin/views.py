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

import omeroweb.webclient.views

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

from controller.group import BaseGroups, BaseGroup
from controller.drivespace import BaseDriveSpace, usersData

from omeroweb.webadmin.webadmin_utils import _checkVersion, _isServerOn, toBoolean, upgradeCheck, getGuestConnection

from omeroweb.webadmin.custom_models import Server

from omeroweb.webclient.decorators import login_required
from omeroweb.connector import Connector

logger = logging.getLogger(__name__)

logger.info("INIT '%s'" % os.getpid())

################################################################################
# decorators

class render_response_admin(omeroweb.webclient.decorators.render_response):
    """ Subclass for adding additional data to the 'context' dict passed to templates """

    def prepare_context(self, request, context, *args, **kwargs):
        """
        We extend the webclient render_response to check if any groups are created.
        If not, add an appropriate message to the template context
        """
        super(render_response_admin, self).prepare_context(request, context, *args, **kwargs)

        if 'info' not in context:
            context['info'] = {}
        context['info']['today'] = _("Today is %(tday)s") % {'tday': datetime.date.today()}
        context['omero_version'] = request.session.get('version')
        noGroupsCreated = kwargs["conn"].isAnythingCreated()
        if noGroupsCreated:
            msg = _('User must be in a group - You have not created any groups yet. Click <a href="%s">here</a> to create a group') % (reverse(viewname="wamanagegroupid", args=["new"]))
            context['info']['message'] = msg

################################################################################
# utils

from omero.rtypes import *
from omero.model import PermissionsI

def prepare_experimenterList(conn):
    
    def isLdapUser(eid):
        try:
            if len(auth.val) > 0:
                for a in auth.val:
                    for k,v in a.val.iteritems():
                        if long(eid) == long(v.val):
                            return True
        except:
            return False
        return False
        
    experimentersList = list(conn.getObjects("Experimenter"))
    #experimentersList.sort(key=lambda x: x.getOmeName().lower())
    auth = conn.listLdapAuthExperimenters()
    experimenters = list()
    for exp in experimentersList:
        exp.ldapUser = isLdapUser(exp.id)
        experimenters.append(exp)
    return experimenters

def prepare_experimenter(conn, eid=None):
    if eid is None:
        eid = conn.getEventContext().userId
    experimenter = conn.getObject("Experimenter", eid)
    defaultGroup = experimenter.getDefaultGroup()
    otherGroups = list(experimenter.getOtherGroups())
    hasAvatar = conn.hasExperimenterPhoto()
    isLdapUser = experimenter.isLdapUser()
    return experimenter, defaultGroup, otherGroups, isLdapUser, hasAvatar

def otherGroupsInitialList(groups, excluded_names=("user","guest"), excluded_ids=list()):
    formGroups = list()
    for gr in groups:
        flag = False
        if gr.name in excluded_names:
            flag = True
        if gr.id in excluded_ids:
            flag = True
        if not flag:
            formGroups.append(gr)
    return formGroups

def ownedGroupsInitial(conn, excluded_names=("user","guest", "system"), excluded_ids=list()):
    groupsList = list(conn.listOwnedGroups())
    ownedGroups = list()
    for gr in groupsList:
        flag = False
        if gr.name in excluded_names:
            flag = True
        if gr.id in excluded_ids:
            flag = True
        if not flag:
            ownedGroups.append({'group': gr, 'permissions': gr.getPermissions()})
    return ownedGroups

def attach_photo(conn, newFile):
    if newFile.content_type.startswith("image"):
        f = newFile.content_type.split("/") 
        format = f[1].upper()
    else:
        format = newFile.content_type
    
    conn.uploadMyUserPhoto(smart_str(newFile.name), format, newFile.read())

def setActualPermissions(permissions, readonly=None):
    p = PermissionsI()
    permissions = int(permissions)
    if permissions == 0:
        # 0 private
        p.setUserRead(True)
        p.setUserWrite(True)
        p.setGroupRead(False)
        p.setGroupWrite(False)
        p.setWorldRead(False)
        p.setWorldWrite(False)
    elif permissions == 1:
        # 1 collaborative
        p.setUserRead(True)
        p.setUserWrite(True)
        p.setGroupRead(True)
        p.setGroupWrite(not readonly)
        p.setWorldRead(False)
        p.setWorldWrite(False)
    elif permissions == 2:
        # 2 public
        p.setUserRead(True)
        p.setUserWrite(True)
        p.setGroupRead(True)
        p.setGroupWrite(not readonly)
        p.setWorldRead(True)
        p.setWorldWrite(not readonly)
    return p

def getActualPermissions(group):
    p = None
    if group.details.getPermissions() is None:
        raise AttributeError('Object has no permissions')
    else:
        p = group.details.getPermissions()
    
    flag = None
    if p.isUserRead():
        flag = 0
    if p.isGroupRead():
        flag = 1
    if p.isWorldRead():
        flag = 2
    
    return flag

def getSelectedGroups(conn, ids):
    if len(ids)>0:
        return list(conn.getObjects("ExperimenterGroup", ids))
    return list()

def getSelectedExperimenters(conn, ids):
    if len(ids)>0:
        return list(conn.getObjects("Experimenter", ids))
    return list()
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

@login_required()
def index(request, **kwargs):
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    if conn.isAdmin():
        return HttpResponseRedirect(reverse("waexperimenters"))
    else:
        return HttpResponseRedirect(reverse("wamyaccount"))

@login_required()
def logout(request, **kwargs):
    omeroweb.webclient.views.logout(request, **kwargs)
    return HttpResponseRedirect(reverse("waindex"))

@login_required(isAdmin=True)
@render_response_admin()
def experimenters(request, conn=None, **kwargs):
    experimenters = True
    template = "webadmin/experimenters.html"
    
    info = {'experimenters':experimenters}
    experimenterList = prepare_experimenterList(conn)
    
    context = {'nav':request.session['nav'], 'info':info, 'experimenterList':experimenterList}
    context['template'] = template
    return context

@login_required(isAdmin=True)
@render_response_admin()
def manage_experimenter(request, action, eid=None, conn=None, **kwargs):
    experimenters = True
    template = "webadmin/experimenter_form.html"
    
    info = {'experimenters':experimenters}
    groups = list(conn.getObjects("ExperimenterGroup"))
    groups.sort(key=lambda x: x.getName().lower())
    
    if action == 'new':
        form = ExperimenterForm(initial={'with_password':True, 'active':True, 'groups':otherGroupsInitialList(groups)})
        context = {'info':info, 'form':form}
    elif action == 'create':
        if request.method != 'POST':
            return HttpResponseRedirect(reverse(viewname="wamanageexperimenterid", args=["new"]))
        else:
            name_check = conn.checkOmeName(request.REQUEST.get('omename'))
            email_check = conn.checkEmail(request.REQUEST.get('email'))
            
            initial={'with_password':True, 'groups':otherGroupsInitialList(groups)}
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
                
                # default group
                for g in groups:
                    if long(defaultGroup) == g.id:
                        dGroup = g
                        break

                listOfOtherGroups = set()
                # rest of groups
                for g in groups:
                    for og in otherGroups:
                        # remove defaultGroup from otherGroups if contains
                        if long(og) == long(dGroup.id):
                            pass
                        elif long(og) == g.id:
                            listOfOtherGroups.add(g)

                conn.createExperimenter(omename, firstName, lastName, email, admin, active, dGroup, listOfOtherGroups, password, middleName, institution)
                return HttpResponseRedirect(reverse("waexperimenters"))
            context = {'info':info, 'form':form}
    elif action == 'edit' :
        experimenter, defaultGroup, otherGroups, isLdapUser, hasAvatar = prepare_experimenter(conn, eid)
        initial={'omename': experimenter.omeName, 'first_name':experimenter.firstName,
                                'middle_name':experimenter.middleName, 'last_name':experimenter.lastName,
                                'email':experimenter.email, 'institution':experimenter.institution,
                                'administrator': experimenter.isAdmin(), 'active': experimenter.isActive(), 
                                'default_group': defaultGroup.id, 'other_groups':[g.id for g in otherGroups],
                                'groups':otherGroupsInitialList(groups)}
        
        form = ExperimenterForm(initial=initial)
        
        context = {'info':info, 'form':form, 'eid': eid, 'ldapAuth': isLdapUser}
    elif action == 'save':
        experimenter, defaultGroup, otherGroups, isLdapUser, hasAvatar = prepare_experimenter(conn, eid)
        if request.method != 'POST':
            return HttpResponseRedirect(reverse(viewname="wamanageexperimenterid", args=["edit", experimenter.id]))
        else:            
            name_check = conn.checkOmeName(request.REQUEST.get('omename'), experimenter.omeName)
            email_check = conn.checkEmail(request.REQUEST.get('email'), experimenter.email)
            initial={'active':True, 'groups':otherGroupsInitialList(groups)}
            
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
                
                # default group
                for g in groups:
                    if long(defaultGroup) == g.id:
                        dGroup = g
                        break

                listOfOtherGroups = set()
                # rest of groups
                for g in groups:
                    for og in otherGroups:
                        # remove defaultGroup from otherGroups if contains
                        if long(og) == long(dGroup.id):
                            pass
                        elif long(og) == g.id:
                            listOfOtherGroups.add(g)

                conn.updateExperimenter(experimenter, omename, firstName, lastName, email, admin, active, dGroup, listOfOtherGroups, middleName, institution)
                return HttpResponseRedirect(reverse("waexperimenters"))
            context = {'info':info, 'form':form, 'eid': eid, 'ldapAuth': isLdapUser}
    elif action == "delete":
        controller.deleteExperimenter()
        return HttpResponseRedirect(reverse("waexperimenters"))
    else:
        return HttpResponseRedirect(reverse("waexperimenters"))
    
    context['template'] = template
    return context


@login_required()
@render_response_admin()
def manage_password(request, eid, conn=None, **kwargs):
    experimenters = True
    template = "webadmin/password.html"

    info = {'experimenters':experimenters}
    
    error = None
    if request.method == 'POST':
        password_form = ChangePassword(data=request.POST.copy())
        if not password_form.is_valid():
            error = password_form.errors
        else:
            old_password = password_form.cleaned_data['old_password']
            password = password_form.cleaned_data['password']
            # if we're trying to change our own password...
            if conn.getEventContext().userId == int(eid):
                try:
                    conn.changeMyPassword(password, old_password)
                except Exception, x:
                    error = x.message   # E.g. old_password not valid
            elif conn.isAdmin():
                exp = conn.getObject("Experimenter", eid)
                try:
                    conn.changeUserPassword(exp.omeName, password, old_password)
                except Exception, x:
                    error = x.message
                else:
                    return HttpResponseRedirect(reverse(viewname="wamanageexperimenterid", args=["edit", eid]))
            else:
                raise AttributeError("Can't change another user's password unless you are an Admin")
                
    context = {'info':info, 'error':error, 'password_form':password_form, 'eid': eid}
    context['template'] = template
    return context


@login_required(isAdmin=True)
@render_response_admin()
def groups(request, conn=None, **kwargs):
    groups = True
    template = "webadmin/groups.html"
    
    info = {'groups':groups}
    
    groups = conn.getObjects("ExperimenterGroup")
    
    context = {'info':info, 'groups':groups}
    context['template'] = template
    return context


@login_required(isAdmin=True)
@render_response_admin()
def manage_group(request, action, gid=None, conn=None, **kwargs):
    groups = True
    template = "webadmin/group_form.html"
    
    info = {'groups':groups}
    
    experimenters = list(conn.getObjects("Experimenter"))
    
    if action == 'new':
        form = GroupForm(initial={'experimenters':experimenters, 'permissions': 0})
        context = {'info':info, 'form':form}
    elif action == 'create':
        if request.method != 'POST':
            return HttpResponseRedirect(reverse(viewname="wamanagegroupid", args=["new"]))
        else:
            name_check = conn.checkGroupName(request.REQUEST.get('name'))
            form = GroupForm(initial={'experimenters':experimenters}, data=request.POST.copy(), name_check=name_check)
            if form.is_valid():
                logger.debug("Create group form:" + str(form.cleaned_data))
                name = form.cleaned_data['name']
                description = form.cleaned_data['description']
                owners = form.cleaned_data['owners']
                permissions = form.cleaned_data['permissions']
                readonly = toBoolean(form.cleaned_data['readonly'])
                
                perm = setActualPermissions(permissions, readonly)
                listOfOwners = getSelectedExperimenters(conn, owners)
                conn.createGroup(name, perm, listOfOwners, description)
                return HttpResponseRedirect(reverse("wagroups"))
            context = {'info':info, 'form':form}
    elif action == 'edit':
        group = conn.getObject("ExperimenterGroup", gid)
        ownerIds = group.getOwners()
        
        permissions = getActualPermissions(group)
        form = GroupForm(initial={'name': group.name, 'description':group.description,
                                     'permissions': permissions, 'readonly': group.isReadOnly(), 
                                     'owners': ownerIds, 'experimenters':experimenters})
        context = {'info':info, 'form':form, 'gid': gid, 'permissions': permissions}
    elif action == 'save':
        if request.method != 'POST':
            return HttpResponseRedirect(reverse(viewname="wamanagegroupid", args=["edit", group.id]))
        else:
            group = conn.getObject("ExperimenterGroup", gid)
            
            name_check = conn.checkGroupName(request.REQUEST.get('name'), group.name)
            form = GroupForm(initial={'experimenters':experimenters}, data=request.POST.copy(), name_check=name_check)
            if form.is_valid():
                logger.debug("Update group form:" + str(form.cleaned_data))
                name = form.cleaned_data['name']
                description = form.cleaned_data['description']
                owners = form.cleaned_data['owners']
                permissions = form.cleaned_data['permissions']
                readonly = toBoolean(form.cleaned_data['readonly'])
                
                listOfOwners = getSelectedExperimenters(conn, owners)
                if getActualPermissions(group) != int(permissions) or group.isReadOnly() != readonly:
                    perm = setActualPermissions(permissions, readonly)
                else:
                    perm = None
                conn.updateGroup(group, name, perm, listOfOwners, description)
                return HttpResponseRedirect(reverse("wagroups"))
            context = {'info':info, 'form':form, 'gid': gid}
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
        context = {'info':info, 'form':form, 'controller': controller}
    elif action == "members":
        template = "webadmin/group_edit.html"
        controller.containedExperimenters()
        form = ContainedExperimentersForm(initial={'members':controller.members, 'available':controller.available})
        context = {'info':info, 'form':form, 'controller': controller}
    else:
        return HttpResponseRedirect(reverse("wagroups"))
    
    context['template'] = template
    return context


@login_required(isGroupOwner=True)
@render_response_admin()
def manage_group_owner(request, action, gid, conn=None, **kwargs):
    myaccount = True
    template = "webadmin/group_form_owner.html"
    
    info = {'myaccount':myaccount}
    
    controller = BaseGroup(conn, gid)
    
    if action == 'edit':
        permissions = controller.getActualPermissions()
        form = GroupOwnerForm(initial={'permissions': permissions, 'readonly': controller.isReadOnly()})
        context = {'info':info, 'form':form, 'gid': gid, 'permissions': permissions, 'group':controller.group, 'owners':controller.getOwnersNames()}
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
            context = {'info':info, 'form':form, 'gid': gid}
    else:
        return HttpResponseRedirect(reverse("wamyaccount"))
    
    context['template'] = template
    return context


@login_required(isAdmin=True)
@render_response_admin()
def ldap(request, conn=None, **kwargs):
    scripts = True
    template = "webadmin/ldap_search.html"
    
    info = {'scripts':scripts}

    controller = None
    
    context = {'info':info, 'controller':controller}
    context['template'] = template
    return context

@login_required(isAdmin=True)
def imports(request, **kwargs):
    return HttpResponseRedirect(reverse("waindex"))


@login_required()
@render_response_admin()
def my_account(request, action=None, conn=None, **kwargs):
    myaccount = True
    template = "webadmin/myaccount.html"
    
    info = {'myaccount':myaccount}
    
    experimenter, defaultGroup, otherGroups, isLdapUser, hasAvatar = prepare_experimenter(conn)
    ownedGroups = ownedGroupsInitial(conn)
    
    password_form = ChangePassword()
    
    form = None
    if action == "save":
        if request.method != 'POST':
            return HttpResponseRedirect(reverse(viewname="wamyaccount", args=["edit"]))
        else:
            email_check = conn.checkEmail(request.REQUEST.get('email'), experimenter.email)
            form = MyAccountForm(data=request.POST.copy(), initial={'groups':otherGroups}, email_check=email_check)
            if form.is_valid():
                firstName = form.cleaned_data['first_name']
                middleName = form.cleaned_data['middle_name']
                lastName = form.cleaned_data['last_name']
                email = form.cleaned_data['email']
                institution = form.cleaned_data['institution']
                defaultGroupId = form.cleaned_data['default_group']
                conn.updateMyAccount(experimenter, firstName, lastName, email, defaultGroupId, middleName, institution)
                return HttpResponseRedirect(reverse("wamyaccount"))
    
    else:
        form = MyAccountForm(initial={'omename': experimenter.omeName, 'first_name':experimenter.firstName,
                                    'middle_name':experimenter.middleName, 'last_name':experimenter.lastName,
                                    'email':experimenter.email, 'institution':experimenter.institution,
                                    'default_group':defaultGroup, 'groups':otherGroups})
    
    photo_size = conn.getExperimenterPhotoSize()
    form = MyAccountForm(initial={'omename': experimenter.omeName, 'first_name':experimenter.firstName,
                                    'middle_name':experimenter.middleName, 'last_name':experimenter.lastName,
                                    'email':experimenter.email, 'institution':experimenter.institution,
                                    'default_group':defaultGroup, 'groups':otherGroups})
        
    context = {'info':info, 'form':form, 'ldapAuth': isLdapUser, 'experimenter':experimenter, 'ownedGroups':ownedGroups, 'password_form':password_form}
    context['template'] = template
    return context


@login_required()
def myphoto(request, conn=None, **kwargs):
    photo = conn.getExperimenterPhoto()
    return HttpResponse(photo, mimetype='image/jpeg')


@login_required()
@render_response_admin()
def manage_avatar(request, action=None, conn=None, **kwargs):
    myaccount = True
    template = "webadmin/avatar.html"
    
    info = {'myaccount':myaccount}
    
    edit_mode = False
    photo_size = None
    form_file = UploadPhotoForm()
    
    if action == "upload":
        if request.method == 'POST':
            form_file = UploadPhotoForm(request.POST, request.FILES)
            if form_file.is_valid():
                attach_photo(conn, request.FILES['photo'])
                return HttpResponseRedirect(reverse(viewname="wamanageavatar", args=[conn.getEventContext().userId]))
    elif action == "crop": 
        x1 = long(request.REQUEST.get('x1'))
        x2 = long(request.REQUEST.get('x2'))
        y1 = long(request.REQUEST.get('y1'))
        y2 = long(request.REQUEST.get('y2'))
        box = (x1,y1,x2,y2)
        conn.cropExperimenterPhoto(box)
        return HttpResponseRedirect(reverse("wamyaccount"))
    elif action == "editphoto":
        photo_size = conn.getExperimenterPhotoSize()
        if photo_size is not None:
            edit_mode = True
    elif action == "deletephoto":
        conn.deleteExperimenterPhoto()
        return HttpResponseRedirect(reverse("wamyaccount"))
    
    photo_size = conn.getExperimenterPhotoSize()
    context = {'info':info, 'form_file':form_file, 'edit_mode':edit_mode, 'photo_size':photo_size}
    context['template'] = template
    return context


@login_required()
@render_response_admin()
def drivespace(request, conn=None, **kwargs):

    controller = BaseDriveSpace(conn)
    return {'free':controller.freeSpace}


@login_required()
def load_drivespace(request, conn=None, **kwargs):
    offset = request.REQUEST.get('offset', 0)
    rv = usersData(conn, offset)
    return HttpResponse(simplejson.dumps(rv),mimetype='application/json')
