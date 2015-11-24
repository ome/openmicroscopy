#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#
#
# Copyright (c) 2008-2014 University of Dundee.
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
# Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>,
# 2008-2013.
#
# Version: 1.0
#

''' A view functions is simply a Python function that takes a Web request and
returns a Web response. This response can be the HTML contents of a Web page,
or a redirect, or the 404 and 500 error, or an XML document, or an image...
or anything.'''

import traceback
import logging
import datetime

import omeroweb.webclient.views

from omero_version import build_year
from omero_version import omero_version

from django.template import loader as template_loader
from django.core.urlresolvers import reverse
from django.http import HttpResponse, HttpResponseRedirect
from django.template import RequestContext as Context
from django.utils.translation import ugettext as _
from django.utils.encoding import smart_str

from forms import ForgottonPasswordForm, ExperimenterForm, GroupForm
from forms import GroupOwnerForm, MyAccountForm, ChangePassword
from forms import UploadPhotoForm, EmailForm

from omeroweb.http import HttpJPEGResponse
from omeroweb.webclient.decorators import login_required, render_response
from omeroweb.connector import Connector

logger = logging.getLogger(__name__)

##############################################################################
# decorators


class render_response_admin(omeroweb.webclient.decorators.render_response):
    """
    Subclass for adding additional data to the 'context' dict passed to
    templates
    """

    def prepare_context(self, request, context, *args, **kwargs):
        """
        We extend the webclient render_response to check if any groups are
        created.
        If not, add an appropriate message to the template context
        """
        super(render_response_admin, self).prepare_context(request, context,
                                                           *args, **kwargs)

        if 'conn' not in kwargs:
            return
        conn = kwargs['conn']

        noGroupsCreated = conn.isAnythingCreated()
        if noGroupsCreated:
            msg = _('User must be in a group - You have not created any'
                    ' groups yet. Click <a href="%s">here</a> to create a'
                    ' group') % (reverse(viewname="wamanagegroupid",
                                         args=["new"]))
            context['ome']['message'] = msg
        context['ome']['email'] = request.session \
                                         .get('server_settings', False) \
                                         .get('email', False)

##############################################################################
# utils

import omero
from omero.model import PermissionsI


def prepare_experimenter(conn, eid=None):
    if eid is None:
        eid = conn.getEventContext().userId
    experimenter = conn.getObject("Experimenter", eid)
    defaultGroup = experimenter.getDefaultGroup()
    otherGroups = list(experimenter.getOtherGroups())
    hasAvatar = conn.hasExperimenterPhoto()
    isLdapUser = experimenter.isLdapUser()
    return experimenter, defaultGroup, otherGroups, isLdapUser, hasAvatar


def otherGroupsInitialList(groups, excluded_names=("user", "guest"),
                           excluded_ids=list()):
    formGroups = list()
    for gr in groups:
        flag = False
        if gr.name in excluded_names:
            flag = True
        if gr.id in excluded_ids:
            flag = True
        if not flag:
            formGroups.append(gr)
    formGroups.sort(key=lambda x: x.getName().lower())
    return formGroups


def ownedGroupsInitial(conn, excluded_names=("user", "guest", "system"),
                       excluded_ids=list()):
    groupsList = list(conn.listOwnedGroups())
    ownedGroups = list()
    for gr in groupsList:
        flag = False
        if gr.name in excluded_names:
            flag = True
        if gr.id in excluded_ids:
            flag = True
        if not flag:
            ownedGroups.append(gr)
    ownedGroups.sort(key=lambda x: x.getName().lower())
    return ownedGroups


# myphoto helpers
def attach_photo(conn, newFile):
    if newFile.content_type.startswith("image"):
        f = newFile.content_type.split("/")
        format = f[1].upper()
    else:
        format = newFile.content_type

    conn.uploadMyUserPhoto(smart_str(newFile.name), format, newFile.read())


# permission helpers
def setActualPermissions(permissions):
    permissions = int(permissions)
    if permissions == 0:
        p = PermissionsI("rw----")
    elif permissions == 1:
        p = PermissionsI("rwr---")
    elif permissions == 2:
        p = PermissionsI("rwra--")
    elif permissions == 3:
        p = PermissionsI("rwrw--")
    else:
        p = PermissionsI()
    return p


def getActualPermissions(group):
    p = None
    if group.details.getPermissions() is None:
        raise AttributeError('Object has no permissions')
    else:
        p = group.details.getPermissions()

    flag = None
    if p.isGroupWrite():
        flag = 3
    elif p.isGroupAnnotate():
        flag = 2
    elif p.isGroupRead():
        flag = 1
    elif p.isUserRead():
        flag = 0

    return flag


# getters
def getSelectedGroups(conn, ids):
    if ids is not None and len(ids) > 0:
        return list(conn.getObjects("ExperimenterGroup", ids))
    return list()


def getSelectedExperimenters(conn, ids):
    if ids is not None and len(ids) > 0:
        return list(conn.getObjects("Experimenter", ids))
    return list()


def mergeLists(list1, list2):
    if not list1 and not list2:
        return list()
    if not list1:
        return list(list2)
    if not list2:
        return list(list1)
    result = list()
    result.extend(list1)
    result.extend(list2)
    return set(result)


@login_required()
@render_response()
def drivespace_json(request, query=None, groupId=None, userId=None, conn=None,
                    **kwargs):
    """
    Returns a json list of {"label":<Name>, "data": <Value>, "groupId /
    userId": <id>} for plotting disk usage by users or groups.
    If 'query' is "groups" or "users", this is for an Admin to show all data
    on server divided into groups or users.
    Else, if groupId is not None, we return data for that group, split by user.
    Else, if userId is not None, we return data for that user, split by group.
    """

    diskUsage = []

    # diskUsage.append({"label": "Free space", "data":conn.getFreeSpace()})

    queryService = conn.getQueryService()
    ctx = conn.SERVICE_OPTS.copy()
    params = omero.sys.ParametersI()
    params.theFilter = omero.sys.Filter()

    def getBytes(ctx, eid=None):
        bytesInGroup = 0

        pixelsQuery = "select sum(cast( p.sizeX as double ) * p.sizeY * p.sizeZ * p.sizeT * p.sizeC * pt.bitSize / 8) " \
            "from Pixels p join p.pixelsType as pt join p.image i left outer join i.fileset f " \
            "join p.details.owner as owner " \
            "where f is null"

        filesQuery = "select sum(origFile.size) from OriginalFile as origFile " \
            "join origFile.details.owner as owner"

        if eid is not None:
            params.add('eid', omero.rtypes.rlong(eid))
            pixelsQuery = pixelsQuery + " and owner.id = (:eid)"
            filesQuery = filesQuery + " where owner.id = (:eid)"
        # Calculate disk usage via Pixels
        result = queryService.projection(pixelsQuery, params, ctx)
        if len(result) > 0 and len(result[0]) > 0:
            bytesInGroup += result[0][0].val
        # Now get Original File usage
        result = queryService.projection(filesQuery, params, ctx)
        if len(result) > 0 and len(result[0]) > 0:
            bytesInGroup += result[0][0]._val
        return bytesInGroup

    sr = conn.getAdminService().getSecurityRoles()

    if query == 'groups':
        for g in conn.listGroups():
            # ignore 'user' and 'guest' groups
            if g.getId() in (sr.guestGroupId, sr.userGroupId):
                continue
            ctx.setOmeroGroup(g.getId())
            b = getBytes(ctx)
            if b > 0:
                diskUsage.append({"label": g.getName(), "data": b,
                                  "groupId": g.getId()})

    elif query == 'users':
        ctx.setOmeroGroup('-1')
        for e in conn.getObjects("Experimenter"):
            b = getBytes(ctx, e.getId())
            if b > 0:
                diskUsage.append({"label": e.getNameWithInitial(), "data": b,
                                  "userId": e.getId()})

    elif userId is not None:
        eid = long(userId)
        for g in conn.getOtherGroups(eid):
            # ignore 'user' and 'guest' groups
            if g.getId() in (sr.guestGroupId, sr.userGroupId):
                continue
            ctx.setOmeroGroup(g.getId())
            b = getBytes(ctx, eid)
            if b > 0:
                diskUsage.append({"label": g.getName(), "data": b,
                                  "groupId": g.getId()})

    # users within a single group
    elif groupId is not None:
        ctx.setOmeroGroup(groupId)
        for e in conn.getObjects("Experimenter"):
            b = getBytes(ctx, e.getId())
            if b > 0:
                diskUsage.append({"label": e.getNameWithInitial(),
                                  "data": b, "userId": e.getId()})

    diskUsage.sort(key=lambda x: x['data'], reverse=True)

    return diskUsage


##############################################################################
# views control

def forgotten_password(request, **kwargs):
    request.session.modified = True

    template = "webadmin/forgotten_password.html"

    conn = None
    error = None

    def getGuestConnection(server_id):
        return Connector(server_id, True).create_guest_connection('OMERO.web')

    if request.method == 'POST':
        form = ForgottonPasswordForm(data=request.POST.copy())
        if form.is_valid():
            server_id = form.cleaned_data['server']
            try:
                conn = getGuestConnection(server_id)
            except Exception:
                logger.error(traceback.format_exc())
                error = "Internal server error, please contact administrator."

            if conn is not None:
                try:
                    req = omero.cmd.ResetPasswordRequest(
                        smart_str(form.cleaned_data['username']),
                        smart_str(form.cleaned_data['email']))
                    handle = conn.c.sf.submit(req)
                    try:
                        conn._waitOnCmd(handle)
                    finally:
                        handle.close()
                    error = "Password was reset. Check your mailbox."
                    form = None
                except omero.CmdError, exp:
                    logger.error(exp.err)
                    try:
                        error = exp.err.parameters[
                            exp.err.parameters.keys()[0]]
                    except:
                        error = exp
    else:
        form = ForgottonPasswordForm()

    context = {'error': error, 'form': form, 'build_year': build_year,
               'omero_version': omero_version}
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
    template = "webadmin/experimenters.html"

    experimenterList = list(conn.getObjects("Experimenter"))

    context = {'experimenterList': experimenterList}
    context['template'] = template
    return context


@login_required(isAdmin=True)
@render_response_admin()
def manage_experimenter(request, action, eid=None, conn=None, **kwargs):
    template = "webadmin/experimenter_form.html"

    groups = list(conn.getObjects("ExperimenterGroup"))
    groups.sort(key=lambda x: x.getName().lower())

    if action == 'new':
        form = ExperimenterForm(initial={
            'with_password': True, 'active': True,
            'groups': otherGroupsInitialList(groups)})
        context = {'form': form}
    elif action == 'create':
        if request.method != 'POST':
            return HttpResponseRedirect(
                reverse(viewname="wamanageexperimenterid", args=["new"]))
        else:
            name_check = conn.checkOmeName(request.POST.get('omename'))
            email_check = conn.checkEmail(request.POST.get('email'))
            my_groups = getSelectedGroups(
                conn,
                request.POST.getlist('other_groups'))
            initial = {'with_password': True,
                       'my_groups': my_groups,
                       'groups': otherGroupsInitialList(groups)}
            form = ExperimenterForm(
                initial=initial, data=request.POST.copy(),
                name_check=name_check, email_check=email_check)
            if form.is_valid():
                logger.debug("Create experimenter form:" +
                             str(form.cleaned_data))
                omename = form.cleaned_data['omename']
                firstName = form.cleaned_data['first_name']
                middleName = form.cleaned_data['middle_name']
                lastName = form.cleaned_data['last_name']
                email = form.cleaned_data['email']
                institution = form.cleaned_data['institution']
                admin = form.cleaned_data['administrator']
                active = form.cleaned_data['active']
                defaultGroup = form.cleaned_data['default_group']
                otherGroups = form.cleaned_data['other_groups']
                password = form.cleaned_data['password']

                # default group
                # if default group was not selected take first from the list.
                if defaultGroup is None:
                    defaultGroup = otherGroups[0]
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

                conn.createExperimenter(
                    omename, firstName, lastName, email, admin, active,
                    dGroup, listOfOtherGroups, password, middleName,
                    institution)
                return HttpResponseRedirect(reverse("waexperimenters"))
            context = {'form': form}
    elif action == 'edit':
        experimenter, defaultGroup, otherGroups, isLdapUser, hasAvatar = \
            prepare_experimenter(conn, eid)
        try:
            defaultGroupId = defaultGroup.id
        except:
            defaultGroupId = None

        initial = {
            'omename': experimenter.omeName,
            'first_name': experimenter.firstName,
            'middle_name': experimenter.middleName,
            'last_name': experimenter.lastName,
            'email': experimenter.email,
            'institution': experimenter.institution,
            'administrator': experimenter.isAdmin(),
            'active': experimenter.isActive(),
            'default_group': defaultGroupId,
            'my_groups': otherGroups,
            'other_groups': [g.id for g in otherGroups],
            'groups': otherGroupsInitialList(groups)}
        system_users = [conn.getAdminService().getSecurityRoles().rootId,
                        conn.getAdminService().getSecurityRoles().guestId]
        experimenter_is_me_or_system = (
            (conn.getEventContext().userId == long(eid)) or
            (long(eid) in system_users))
        form = ExperimenterForm(
            experimenter_is_me_or_system=experimenter_is_me_or_system,
            initial=initial)
        password_form = ChangePassword()

        admin_groups = (
            experimenter_is_me_or_system and
            [conn.getAdminService().getSecurityRoles().systemGroupId] or
            list())
        context = {'form': form, 'eid': eid, 'ldapAuth': isLdapUser,
                   'password_form': password_form,
                   'admin_groups': admin_groups}
    elif action == 'save':
        experimenter, defaultGroup, otherGroups, isLdapUser, hasAvatar = \
            prepare_experimenter(conn, eid)
        if request.method != 'POST':
            return HttpResponseRedirect(
                reverse(viewname="wamanageexperimenterid",
                        args=["edit", experimenter.id]))
        else:
            name_check = conn.checkOmeName(request.POST.get('omename'),
                                           experimenter.omeName)
            email_check = conn.checkEmail(request.POST.get('email'),
                                          experimenter.email)
            my_groups = getSelectedGroups(
                conn,
                request.POST.getlist('other_groups'))
            initial = {'my_groups': my_groups,
                       'groups': otherGroupsInitialList(groups)}
            form = ExperimenterForm(initial=initial, data=request.POST.copy(),
                                    name_check=name_check,
                                    email_check=email_check)

            if form.is_valid():
                logger.debug("Update experimenter form:" +
                             str(form.cleaned_data))
                omename = form.cleaned_data['omename']
                firstName = form.cleaned_data['first_name']
                middleName = form.cleaned_data['middle_name']
                lastName = form.cleaned_data['last_name']
                email = form.cleaned_data['email']
                institution = form.cleaned_data['institution']
                admin = form.cleaned_data['administrator']
                active = form.cleaned_data['active']
                rootId = conn.getAdminService().getSecurityRoles().rootId
                # User can't disable themselves or 'root'
                if experimenter.getId() in [conn.getUserId(), rootId]:
                    # disabled checkbox not in POST: do it manually
                    active = True
                defaultGroup = form.cleaned_data['default_group']
                otherGroups = form.cleaned_data['other_groups']

                # default group
                # if default group was not selected take first from the list.
                if defaultGroup is None:
                    defaultGroup = otherGroups[0]
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

                conn.updateExperimenter(
                    experimenter, omename, firstName, lastName, email, admin,
                    active, dGroup, listOfOtherGroups, middleName,
                    institution)
                return HttpResponseRedirect(reverse("waexperimenters"))
            context = {'form': form, 'eid': eid, 'ldapAuth': isLdapUser}
    # elif action == "delete":
    #    conn.deleteExperimenter()
    #    return HttpResponseRedirect(reverse("waexperimenters"))
    else:
        return HttpResponseRedirect(reverse("waexperimenters"))

    context['template'] = template
    return context


@login_required()
@render_response_admin()
def manage_password(request, eid, conn=None, **kwargs):
    template = "webadmin/password.html"

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
                    conn.changeUserPassword(exp.omeName, password,
                                            old_password)
                except Exception, x:
                    error = x.message
            else:
                raise AttributeError("Can't change another user's password"
                                     " unless you are an Admin")

    context = {'error': error, 'password_form': password_form, 'eid': eid}
    context['template'] = template
    return context


@login_required(isAdmin=True)
@render_response_admin()
def groups(request, conn=None, **kwargs):
    template = "webadmin/groups.html"

    groups = conn.getObjects("ExperimenterGroup")

    context = {'groups': groups}
    context['template'] = template
    return context


@login_required(isAdmin=True)
@render_response_admin()
def manage_group(request, action, gid=None, conn=None, **kwargs):
    template = "webadmin/group_form.html"
    msgs = []

    experimenters = list(conn.getObjects("Experimenter"))
    experimenters.sort(key=lambda x: x.getLastName().lower())

    def getEditFormContext():
        group = conn.getObject("ExperimenterGroup", gid)
        ownerIds = [e.id for e in group.getOwners()]
        memberIds = [m.id for m in group.getMembers()]
        permissions = getActualPermissions(group)
        system_groups = [
            conn.getAdminService().getSecurityRoles().systemGroupId,
            conn.getAdminService().getSecurityRoles().userGroupId,
            conn.getAdminService().getSecurityRoles().guestGroupId]
        group_is_current_or_system = (
            (conn.getEventContext().groupId == long(gid)) or
            (long(gid) in system_groups))
        form = GroupForm(initial={
            'name': group.name,
            'description': group.description,
            'permissions': permissions,
            'owners': ownerIds,
            'members': memberIds,
            'experimenters': experimenters},
            group_is_current_or_system=group_is_current_or_system)
        admins = [conn.getAdminService().getSecurityRoles().rootId]
        if long(gid) in system_groups:
            # prevent removing 'root' or yourself from group if it's a system
            # group
            admins.append(conn.getUserId())
        return {'form': form, 'gid': gid, 'permissions': permissions,
                "admins": admins}

    if action == 'new':
        form = GroupForm(initial={'experimenters': experimenters,
                                  'permissions': 0})
        context = {'form': form}
    elif action == 'create':
        if request.method != 'POST':
            return HttpResponseRedirect(reverse(viewname="wamanagegroupid",
                                                args=["new"]))
        else:
            name_check = conn.checkGroupName(request.POST.get('name'))
            form = GroupForm(initial={'experimenters': experimenters},
                             data=request.POST.copy(), name_check=name_check)
            if form.is_valid():
                logger.debug("Create group form:" + str(form.cleaned_data))
                name = form.cleaned_data['name']
                description = form.cleaned_data['description']
                owners = form.cleaned_data['owners']
                members = form.cleaned_data['members']
                permissions = form.cleaned_data['permissions']

                perm = setActualPermissions(permissions)
                listOfOwners = getSelectedExperimenters(conn, owners)
                gid = conn.createGroup(name, perm, listOfOwners, description)
                new_members = getSelectedExperimenters(
                    conn, mergeLists(members, owners))
                group = conn.getObject("ExperimenterGroup", gid)
                conn.setMembersOfGroup(group, new_members)

                return HttpResponseRedirect(reverse("wagroups"))
            context = {'form': form}
    elif action == 'edit':
        context = getEditFormContext()
    elif action == 'save':
        group = conn.getObject("ExperimenterGroup", gid)

        if request.method != 'POST':
            return HttpResponseRedirect(reverse(viewname="wamanagegroupid",
                                                args=["edit", group.id]))
        else:
            permissions = getActualPermissions(group)

            name_check = conn.checkGroupName(request.POST.get('name'),
                                             group.name)
            form = GroupForm(initial={'experimenters': experimenters},
                             data=request.POST.copy(), name_check=name_check)
            context = {'form': form, 'gid': gid, 'permissions': permissions}
            if form.is_valid():
                logger.debug("Update group form:" + str(form.cleaned_data))
                name = form.cleaned_data['name']
                description = form.cleaned_data['description']
                owners = form.cleaned_data['owners']
                permissions = form.cleaned_data['permissions']
                members = form.cleaned_data['members']

                listOfOwners = getSelectedExperimenters(conn, owners)
                if permissions != int(permissions):
                    perm = setActualPermissions(permissions)
                else:
                    perm = None

                context = getEditFormContext()
                context['ome'] = {}

                try:
                    msgs = conn.updateGroup(group, name, perm, listOfOwners,
                                            description)
                except omero.SecurityViolation, ex:
                    if ex.message.startswith('Cannot change permissions'):
                        msgs.append("Downgrade to private group not currently"
                                    " possible")
                    else:
                        msgs.append(ex.message)

                new_members = getSelectedExperimenters(
                    conn, mergeLists(members, owners))
                removalFails = conn.setMembersOfGroup(group, new_members)
                if len(removalFails) == 0 and len(msgs) == 0:
                    return HttpResponseRedirect(reverse("wagroups"))
                # If we've failed to remove user...

                # prepare error messages
                for e in removalFails:
                    url = reverse("wamanageexperimenterid",
                                  args=["edit", e.id])
                    msgs.append("Can't remove user <a href='%s'>%s</a> from"
                                " their only group" % (url, e.getFullName()))
                # refresh the form and add messages
                context = getEditFormContext()

    else:
        return HttpResponseRedirect(reverse("wagroups"))

    context['userId'] = conn.getEventContext().userId
    context['template'] = template

    if len(msgs) > 0:
        context['ome'] = {}
        context['ome']['message'] = "<br>".join(msgs)
    return context


@login_required(isGroupOwner=True)
@render_response_admin()
def manage_group_owner(request, action, gid, conn=None, **kwargs):
    template = "webadmin/group_form_owner.html"

    group = conn.getObject("ExperimenterGroup", gid)
    experimenters = list(conn.getObjects("Experimenter"))
    userId = conn.getEventContext().userId

    def getEditFormContext():
        group = conn.getObject("ExperimenterGroup", gid)
        memberIds = [m.id for m in group.getMembers()]
        ownerIds = [e.id for e in group.getOwners()]
        permissions = getActualPermissions(group)
        form = GroupOwnerForm(initial={'permissions': permissions,
                                       'members': memberIds,
                                       'owners': ownerIds,
                                       'experimenters': experimenters})
        context = {'form': form, 'gid': gid, 'permissions': permissions,
                   "group": group}

        experimenterDefaultIds = list()
        for e in experimenters:
            if (e != userId and e.getDefaultGroup() is not None and
                    e.getDefaultGroup().id == group.id):
                experimenterDefaultIds.append(str(e.id))
        context['experimenterDefaultGroups'] = ",".join(experimenterDefaultIds)
        context['ownerIds'] = (",".join(str(x) for x in ownerIds
                               if x != userId))

        return context

    msgs = []
    if action == 'edit':
        context = getEditFormContext()
    elif action == "save":

        if request.method != 'POST':
            return HttpResponseRedirect(
                reverse(viewname="wamanagegroupownerid",
                        args=["edit", group.id]))
        else:
            form = GroupOwnerForm(data=request.POST.copy(),
                                  initial={'experimenters': experimenters})
            if form.is_valid():
                members = form.cleaned_data['members']
                owners = form.cleaned_data['owners']
                permissions = form.cleaned_data['permissions']

                listOfOwners = getSelectedExperimenters(conn, owners)
                conn.setOwnersOfGroup(group, listOfOwners)

                new_members = getSelectedExperimenters(conn, members)
                removalFails = conn.setMembersOfGroup(group, new_members)

                permissions = int(permissions)
                if getActualPermissions(group) != permissions:
                    perm = setActualPermissions(permissions)
                    try:
                        msg = conn.updatePermissions(group, perm)
                        if msg is not None:
                            msgs.append(msg)
                    except omero.SecurityViolation, ex:
                        if ex.message.startswith('Cannot change permissions'):
                            msgs.append("Downgrade to private group not"
                                        " currently possible")
                        else:
                            msgs.append(ex.message)

                if len(removalFails) == 0 and len(msgs) == 0:
                    return HttpResponseRedirect(reverse("wamyaccount"))
                # If we've failed to remove user...
                # prepare error messages
                for e in removalFails:
                    url = reverse("wamanageexperimenterid",
                                  args=["edit", e.id])
                    msgs.append("Can't remove user <a href='%s'>%s</a> from"
                                " their only group" % (url, e.getFullName()))
                # refresh the form and add messages
                context = getEditFormContext()
            else:
                context = {'gid': gid, 'form': form}
    else:
        return HttpResponseRedirect(reverse("wamyaccount"))

    context['userId'] = userId
    context['template'] = template

    if len(msgs) > 0:
        context['ome'] = {}
        context['ome']['message'] = "<br>".join(msgs)
    return context


@login_required()
@render_response_admin()
def my_account(request, action=None, conn=None, **kwargs):
    template = "webadmin/myaccount.html"

    experimenter, defaultGroup, otherGroups, isLdapUser, hasAvatar = \
        prepare_experimenter(conn)
    try:
        defaultGroupId = defaultGroup.id
    except:
        defaultGroupId = None

    ownedGroups = ownedGroupsInitial(conn)

    password_form = ChangePassword()

    form = None
    if action == "save":
        if request.method != 'POST':
            return HttpResponseRedirect(reverse(viewname="wamyaccount",
                                                args=["edit"]))
        else:
            email_check = conn.checkEmail(request.POST.get('email'),
                                          experimenter.email)
            form = MyAccountForm(data=request.POST.copy(),
                                 initial={'groups': otherGroups},
                                 email_check=email_check)
            if form.is_valid():
                firstName = form.cleaned_data['first_name']
                middleName = form.cleaned_data['middle_name']
                lastName = form.cleaned_data['last_name']
                email = form.cleaned_data['email']
                institution = form.cleaned_data['institution']
                defaultGroupId = form.cleaned_data['default_group']
                conn.updateMyAccount(
                    experimenter, firstName, lastName, email, defaultGroupId,
                    middleName, institution)
                return HttpResponseRedirect(reverse("wamyaccount"))

    else:
        form = MyAccountForm(initial={
            'omename': experimenter.omeName,
            'first_name': experimenter.firstName,
            'middle_name': experimenter.middleName,
            'last_name': experimenter.lastName,
            'email': experimenter.email,
            'institution': experimenter.institution,
            'default_group': defaultGroupId,
            'groups': otherGroups})

    context = {'form': form, 'ldapAuth': isLdapUser,
               'experimenter': experimenter, 'ownedGroups': ownedGroups,
               'password_form': password_form}
    context['freeSpace'] = conn.getFreeSpace()
    context['template'] = template
    return context


@login_required()
def myphoto(request, conn=None, **kwargs):
    photo = conn.getExperimenterPhoto()
    return HttpJPEGResponse(photo)


@login_required()
@render_response_admin()
def manage_avatar(request, action=None, conn=None, **kwargs):
    template = "webadmin/avatar.html"

    edit_mode = False
    photo_size = None
    form_file = UploadPhotoForm()

    if action == "upload":
        if request.method == 'POST':
            form_file = UploadPhotoForm(request.POST, request.FILES)
            if form_file.is_valid():
                attach_photo(conn, request.FILES['photo'])
                return HttpResponseRedirect(
                    reverse(viewname="wamanageavatar",
                            args=[conn.getEventContext().userId]))
    elif action == "crop":
        x1 = long(request.POST.get('x1'))
        x2 = long(request.POST.get('x2'))
        y1 = long(request.POST.get('y1'))
        y2 = long(request.POST.get('y2'))
        box = (x1, y1, x2, y2)
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
    context = {'form_file': form_file, 'edit_mode': edit_mode,
               'photo_size': photo_size}
    context['template'] = template
    return context


@login_required()
@render_response_admin()
def stats(request, conn=None, **kwargs):
    template = "webadmin/statistics.html"
    freeSpace = conn.getFreeSpace()
    context = {'template': template, 'freeSpace': freeSpace}
    return context


# @login_required()
# def load_drivespace(request, conn=None, **kwargs):
#     offset = request.POST.get('offset', 0)
#     rv = usersData(conn, offset)
#     return HttpJsonResponse(rv)


@login_required(isAdmin=True)
@render_response_admin()
def email(request, conn=None, **kwargs):
    """
    View to gather recipients, subject and message for sending email
    announcements
    """

    # Check that the appropriate web settings are available
    if (not request.session.get('server_settings', False)
                           .get('email', False)):
        return {'template': 'webadmin/noemail.html'}
    context = {'template': 'webadmin/email.html'}

    # Get experimenters and groups.
    experimenter_list = list(conn.getObjects("Experimenter"))
    group_list = list(conn.getObjects("ExperimenterGroup"))

    # Sort experimenters and groups
    experimenter_list.sort(key=lambda x: x.getFirstName().lower())
    group_list.sort(key=lambda x: x.getName().lower())

    if request.method == 'POST':  # If the form has been submitted...
        # ContactForm was defined in the the previous section
        form = EmailForm(experimenter_list, group_list, conn, request,
                         data=request.POST.copy())
        if form.is_valid():  # All validation rules pass
            subject = form.cleaned_data['subject']
            message = form.cleaned_data['message']
            experimenters = form.cleaned_data['experimenters']
            groups = form.cleaned_data['groups']
            everyone = form.cleaned_data['everyone']
            inactive = form.cleaned_data['inactive']

            req = omero.cmd.SendEmailRequest(subject=subject, body=message,
                                             groupIds=groups,
                                             userIds=experimenters,
                                             everyone=everyone,
                                             inactive=inactive)
            handle = conn.c.sf.submit(req)
            if handle is not None:
                request.session.modified = True
                request.session['callback'][str(handle)] = {
                    'job_type': 'send_email',
                    'status': 'in progress', 'error': 0,
                    'start_time': datetime.datetime.now()}
            form = EmailForm(experimenter_list, group_list, conn, request)
            context['non_field_errors'] = ("Email sent."
                                           "Check status in activities.")
        else:
            context['non_field_errors'] = "Email wasn't sent."

    else:
        form = EmailForm(experimenter_list, group_list, conn, request)

    context['form'] = form

    return context

# Problem where render_response_admin was not populating required
# admin details:
# Explanation is that the CBV FormView returns an http response so the
# decorator render_response_admin simply bails out and returns this
# I think maybe the render_response decorator should not be adding context
# because it fails in situations like this, better to insert that context
# using a template tag when required
