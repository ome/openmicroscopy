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

from django.conf import settings
from django.contrib.sessions.backends.db import SessionStore
from django.contrib.sessions.models import Session
from django.core import template_loader
from django.core.cache import cache
from django.http import HttpResponse, HttpRequest, HttpResponseRedirect, Http404
from django.shortcuts import render_to_response
from django.template import RequestContext as Context
from django.utils import simplejson
from django.utils.translation import ugettext as _
from django.views.defaults import page_not_found, server_error
from django.views import debug

from controller import BaseController
from controller.experimenter import BaseExperimenters, BaseExperimenter
from controller.group import BaseGroups, BaseGroup
from controller.script import BaseScripts, BaseScript
from controller.drivespace import BaseDriveSpace
from controller.uploadfile import BaseUploadFile

from models import Gateway, LoginForm, ForgottonPasswordForm, ExperimenterForm, \
                   ExperimenterLdapForm, GroupForm, ScriptForm, MyAccountForm, \
                   MyAccountLdapForm, ContainedExperimentersForm, UploadPhotoForm

#from extlib.gateway import BlitzGateway

from extlib.gateway import _session_logout, timeit, getBlitzConnection, _createConnection

logger = logging.getLogger('views-admin')

connectors = {}

logger.info("INIT '%s'" % os.getpid())

try:
    if settings.EMAIL_NOTIFICATION:
        import omeroweb.extlib.notification.handlesender as sender
        sender.handler()
except:
    logger.error(traceback.format_exc())

################################################################################
# Blitz Gateway Connection

@timeit
def getConnection (request, force_key=None):
    try:
        server = request.session['server']
    except KeyError:
        return None

    return getBlitzConnection(request, server, with_session=True, skip_stored=True, force_key=force_key)

def getGuestConnection(host, port):
    conn = None
    guest = ["guest", "guest"]
    try:
        conn = _createConnection('', host=host, port=port, username=guest[0], passwd=guest[1], skip_stored=True)
        #conn.connectAsGuest()
    except:
        logger.error(traceback.format_exc())
        raise sys.exc_info()[1]
    else:
        # do not store connection on connectors
        logger.info("Have connection as Guest")
    return conn

################################################################################
# decorators

def isAdminConnected (f):
    def wrapped (request, *args, **kwargs):
        #this check the connection exist, if not it will redirect to login page
        conn = None
        try:
            conn = getConnection(request)
        except Exception, x:
            logger.error(traceback.format_exc())
            return HttpResponseRedirect("/%s/login/?error=%s" % (settings.WEBADMIN_ROOT_BASE, x.__class__.__name__))
        if conn is None:
            return HttpResponseRedirect("/%s/login/" % (settings.WEBADMIN_ROOT_BASE))
        if not conn.getEventContext().isAdmin:
            return page_not_found(request, "404.html")
        kwargs["conn"] = conn
        return f(request, *args, **kwargs)

    return wrapped

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
                url = '%s?%s' % (request.META['PATH_INFO'], request.META['QUERY_STRING'])
            else:
                url = '%s' % (request.META['PATH_INFO'])
        
        conn = None
        try:
            conn = getConnection(request)
        except KeyError:
            return HttpResponseRedirect("/%s/login/?url=%s" % (settings.WEBADMIN_ROOT_BASE, url))
        except Exception, x:
            logger.error(traceback.format_exc())
            return HttpResponseRedirect("/%s/login/?error=%s&url=%s" % (settings.WEBADMIN_ROOT_BASE, x.__class__.__name__, url))
        if conn is None:
            return HttpResponseRedirect("/%s/login/?url=%s" % (settings.WEBADMIN_ROOT_BASE, url))   
        
        kwargs["conn"] = conn
        kwargs["url"] = url
        return f(request, *args, **kwargs)
    
    return wrapped

################################################################################
# views controll

def forgotten_password(request, **kwargs):
    template = "omeroadmin/forgotten_password.html"
    
    conn = None
    error = None
    
    if request.method == 'POST' and request.REQUEST['server'] and request.REQUEST['username'] and request.REQUEST['email']:
        blitz = Gateway.objects.get(pk=request.REQUEST['server'])
        try:
            conn = getGuestConnection(blitz.host, blitz.port)
            if not conn.isForgottenPasswordSet():
                error = "This server cannot reset password. Please contact your administrator."
                conn = None
        except Exception, x:
            logger.error(traceback.format_exc())
            error = x.__class__.__name__

    if conn is not None:
        controller = None
        try:
            controller = conn.reportForgottenPassword(request.REQUEST['username'].encode('utf-8'), request.REQUEST['email'].encode('utf-8'))
        except Exception, x:
            logger.error(traceback.format_exc())
            error = x.__class__.__name__
        form = ForgottonPasswordForm(data=request.REQUEST.copy())
        context = {'error':error, 'controller':controller, 'form':form}
    else:
        if request.method == 'POST':
            form = ForgottonPasswordForm(data=request.REQUEST.copy())
        else:
            try:
                blitz = Gateway.objects.filter(id=request.REQUEST['server'])
                data = {'server': unicode(blitz[0].id), 'username':unicode(request.REQUEST['username']), 'password':unicode(request.REQUEST['password']) }
                form = ForgottonPasswordForm(data=data)
            except:
                form = ForgottonPasswordForm()
        context = {'error':error, 'form':form}
    
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

def login(request):
    if request.method == 'POST' and request.REQUEST['server']:
        blitz = Gateway.objects.get(pk=request.REQUEST['server'])
        _session_logout(request, blitz.id)
        request.session['server'] = blitz.id
        request.session['host'] = blitz.host
        request.session['port'] = blitz.port
        request.session['username'] = request.REQUEST['username'].encode('utf-8')
        request.session['password'] = request.REQUEST['password'].encode('utf-8')
    
    try:
        error = request.REQUEST['error']
    except:
        error = None
        
    conn = None
    try:
        conn = getConnection(request)
    except Exception, x:
        logger.debug(traceback.format_exc())
        error = x.__class__.__name__
    
    if conn is not None:
        return HttpResponseRedirect("/%s/" % (settings.WEBADMIN_ROOT_BASE))
    else:
        if request.method == 'POST' and request.REQUEST['server']:
            error = "Connection not available, please chceck your user name and password."
        try:
            request.session['server'] = request.REQUEST['server']
        except:
            pass
        
        template = "omeroadmin/login.html"
        if request.method == 'POST':
            form = LoginForm(data=request.REQUEST.copy())
        else:
            try:
                blitz = Gateway.objects.filter(id=request.session['server'])
                data = {'server': unicode(blitz[0].id), 'username':unicode(request.session['username']), 'password':unicode(request.session['password']) }
                form = LoginForm(data=data)
            except:
                form = LoginForm()
        context = {'error':error, 'form':form}
        t = template_loader.get_template(template)
        c = Context(request, context)
        rsp = t.render(c)
        return HttpResponse(rsp)

@isUserConnected
def index(request, **kwargs):
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    if conn.getEventContext().isAdmin:
        return HttpResponseRedirect("/%s/experimenters/" % (settings.WEBADMIN_ROOT_BASE))
    else:
        return HttpResponseRedirect("/%s/myaccount/" % (settings.WEBADMIN_ROOT_BASE))

def logout(request):
    _session_logout(request, request.session['server'])
#    try:
#        conn = getConnection(request)
#    except:
#        logger.error(traceback.format_exc())
#    else:
#        try:
#            session_key = "S:%s#%s" % (request.session.session_key,request.session['server'])
#            if connectors.has_key(session_key):
#                conn.seppuku()
#                del connectors[session_key]
#        except:
#            logger.error(traceback.format_exc())
    
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
#    try:
#        del request.session['sessionUuid']
#    except KeyError:
#        logger.error(traceback.format_exc())
    
    request.session.set_expiry(1)
    return HttpResponseRedirect("/%s/" % (settings.WEBADMIN_ROOT_BASE))

@isAdminConnected
def experimenters(request, **kwargs):
    experimenters = True
    template = "omeroadmin/experimenters.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'experimenters':experimenters}
    eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin }
    controller = BaseExperimenters(conn)
    
    context = {'info':info, 'eventContext':eventContext, 'controller':controller}
    
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isAdminConnected
def manage_experimenter(request, action, eid=None, **kwargs):
    experimenters = True
    template = "omeroadmin/experimenter_form.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'experimenters':experimenters}
    eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin }
    
    controller = BaseExperimenter(conn, eid)
    
    if action == 'new':
        form = ExperimenterForm(initial={'dgroups':controller.defaultGroupsInitialList(), 'groups':controller.otherGroupsInitialList()})
        context = {'info':info, 'eventContext':eventContext, 'form':form}
    elif action == 'create':
        name_check = conn.checkOmeName(request.REQUEST['omename'].encode('utf-8'))
        email_check = conn.checkEmail(request.REQUEST['email'].encode('utf-8'))
        form = ExperimenterForm(initial={'dgroups':controller.defaultGroupsInitialList(), 'groups':controller.otherGroupsInitialList()}, data=request.REQUEST.copy(), name_check=name_check, email_check=email_check, passwd_check=True)
        if form.is_valid():
            omeName = request.REQUEST['omename'].encode('utf-8')
            firstName = request.REQUEST['first_name'].encode('utf-8')
            middleName = request.REQUEST['middle_name'].encode('utf-8')
            lastName = request.REQUEST['last_name'].encode('utf-8')
            email = request.REQUEST['email'].encode('utf-8')
            institution = request.REQUEST['institution'].encode('utf-8')
            admin = False
            try:
                if request.REQUEST['administrator']:
                    admin = True
            except:
                pass
            active = False
            try:
                if request.REQUEST['active']:
                    active = True
            except:
                pass
            defaultGroup = request.REQUEST['default_group']
            otherGroups = request.POST.getlist('other_groups')
            password = request.REQUEST['password'].encode('utf-8')
            controller.createExperimenter(omeName, firstName, lastName, email, admin, active, defaultGroup, otherGroups, password, middleName, institution)
            return HttpResponseRedirect("/%s/experimenters/" % settings.WEBADMIN_ROOT_BASE)
        context = {'info':info, 'eventContext':eventContext, 'form':form}
    elif action == 'edit' :
        if controller.ldapAuth == "" or controller.ldapAuth is None:
            form = ExperimenterForm(initial={'omename': controller.experimenter.omeName, 'first_name':controller.experimenter.firstName,
                                    'middle_name':controller.experimenter.middleName, 'last_name':controller.experimenter.lastName,
                                    'email':controller.experimenter.email, 'institution':controller.experimenter.institution,
                                    'administrator': controller.isAdmin, 'active': controller.isActive,
                                    'default_group':controller.defaultGroup, 'other_groups': controller.otherGroups,
                                    'dgroups':controller.defaultGroupsInitialList(), 'groups':controller.otherGroupsInitialList()})
        else:
            form = ExperimenterLdapForm(initial={'omename': controller.experimenter.omeName, 'first_name':controller.experimenter.firstName,
                                    'middle_name':controller.experimenter.middleName, 'last_name':controller.experimenter.lastName,
                                    'email':controller.experimenter.email, 'institution':controller.experimenter.institution,
                                    'administrator': controller.isAdmin, 'active': controller.isActive,
                                    'default_group':controller.defaultGroup, 'other_groups': controller.otherGroups,
                                    'dgroups':controller.defaultGroupsInitialList(), 'groups':controller.otherGroupsInitialList()})
        context = {'info':info, 'eventContext':eventContext, 'form':form, 'eid': eid, 'ldapAuth': controller.ldapAuth}
    elif action == 'save':
        name_check = conn.checkOmeName(request.REQUEST['omename'].encode('utf-8'), controller.experimenter.omeName)
        email_check = conn.checkEmail(request.REQUEST['email'].encode('utf-8'), controller.experimenter.email)
        form = ExperimenterForm(initial={'dgroups':controller.defaultGroupsInitialList(), 'groups':controller.otherGroupsInitialList()}, data=request.POST.copy(), name_check=name_check, email_check=email_check)
        if form.is_valid():
            omeName = request.REQUEST['omename'].encode('utf-8')
            firstName = request.REQUEST['first_name'].encode('utf-8')
            middleName = request.REQUEST['middle_name'].encode('utf-8')
            lastName = request.REQUEST['last_name'].encode('utf-8')
            email = request.REQUEST['email'].encode('utf-8')
            institution = request.REQUEST['institution'].encode('utf-8')
            admin = False
            try:
                if request.REQUEST['administrator']:
                    admin = True
            except:
                pass
            active = False
            try:
                if request.REQUEST['active']:
                    active = True
            except:
                pass
            defaultGroup = request.REQUEST['default_group']
            otherGroups = request.POST.getlist('other_groups')
            try:
                password = request.REQUEST['password'].encode('utf-8')
                if len(password) == 0:
                    password = None
            except:
                password = None
            controller.updateExperimenter(omeName, firstName, lastName, email, admin, active, defaultGroup, otherGroups, middleName, institution, password)
            return HttpResponseRedirect("/%s/experimenters/" % settings.WEBADMIN_ROOT_BASE)
        context = {'info':info, 'eventContext':eventContext, 'form':form, 'eid': eid}
    elif action == "delete":
        controller.deleteExperimenter()
        return HttpResponseRedirect("/%s/experimenters/" % settings.WEBADMIN_ROOT_BASE)
    else:
        return HttpResponseRedirect("/%s/experimenters/" % settings.WEBADMIN_ROOT_BASE)
    
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isAdminConnected
def groups(request, **kwargs):
    groups = True
    template = "omeroadmin/groups.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'groups':groups}
    eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin }
    controller = BaseGroups(conn)
    
    context = {'info':info, 'eventContext':eventContext, 'controller':controller}
    
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isAdminConnected
def manage_group(request, action, gid=None, **kwargs):
    groups = True
    template = "omeroadmin/group_form.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'groups':groups}
    eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin }
    
    controller = BaseGroup(conn, gid)
    
    if action == 'new':
        form = GroupForm(initial={'experimenters':controller.experimenters})
        context = {'info':info, 'eventContext':eventContext, 'form':form}
    elif action == 'create':
        name_check = conn.checkGroupName(request.REQUEST['name'].encode('utf-8'))
        form = GroupForm(initial={'experimenters':controller.experimenters}, data=request.POST.copy(), name_check=name_check)
        if form.is_valid():
            name = request.REQUEST['name'].encode('utf-8')
            description = request.REQUEST['description'].encode('utf-8')
            owner = request.REQUEST['owner']
            controller.createGroup(name, owner, description)
            return HttpResponseRedirect("/%s/groups/" % settings.WEBADMIN_ROOT_BASE)
        context = {'info':info, 'eventContext':eventContext, 'form':form}
    elif action == 'edit':
        form = GroupForm(initial={'name': controller.group.name, 'description':controller.group.description,
                                     'owner': controller.group.details.owner.id.val, 'experimenters':controller.experimenters})
        context = {'info':info, 'eventContext':eventContext, 'form':form, 'gid': gid}
    elif action == 'save':
        name_check = conn.checkGroupName(request.REQUEST['name'].encode('utf-8'), controller.group.name)
        form = GroupForm(initial={'experimenters':controller.experimenters}, data=request.POST.copy(), name_check=name_check)
        if form.is_valid():
            name = request.REQUEST['name'].encode('utf-8')
            description = request.REQUEST['description'].encode('utf-8')
            owner = request.REQUEST['owner']
            controller.updateGroup(name, owner, description)
            return HttpResponseRedirect("/%s/groups/" % settings.WEBADMIN_ROOT_BASE)
        context = {'info':info, 'eventContext':eventContext, 'form':form, 'gid': gid}
    elif action == "update":
        template = "omeroadmin/group_edit.html"
        controller.containedExperimenters()
        form = ContainedExperimentersForm(initial={'members':controller.members, 'available':controller.available})
        if not form.is_valid():
            available = request.POST.getlist('available')
            members = request.POST.getlist('members')
            controller.setMembersOfGroup(available, members)
            return HttpResponseRedirect("/%s/groups/" % settings.WEBADMIN_ROOT_BASE)
        context = {'info':info, 'eventContext':eventContext, 'form':form, 'controller': controller}
    elif action == "members":
        template = "omeroadmin/group_edit.html"
        controller.containedExperimenters()
        form = ContainedExperimentersForm(initial={'members':controller.members, 'available':controller.available})
        context = {'info':info, 'eventContext':eventContext, 'form':form, 'controller': controller}
    else:
        return HttpResponseRedirect("/%s/groups/" % settings.WEBADMIN_ROOT_BASE)
    
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isAdminConnected
def ldap(request, **kwargs):
    return HttpResponseRedirect("/%s/" % (settings.WEBADMIN_ROOT_BASE))

@isAdminConnected
def scripts(request, **kwargs):
    scripts = True
    template = "omeroadmin/scripts.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'scripts':scripts}
    eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin }
    controller = BaseScripts(conn)
    
    context = {'info':info, 'eventContext':eventContext, 'controller':controller}
    
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isAdminConnected
def manage_script(request, action, sc_id=None, **kwargs):
    scripts = True
    template = "omeroadmin/script_form.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'scripts':scripts}
    eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin }
    controller = BaseScript(conn)
    
    if action == 'new':
        form = ScriptForm(initial={'script':controller.script})
        context = {'info':info, 'eventContext':eventContext, 'form':form}
    elif action == 'save':
        form = GroupForm(initial={'script':controller.script}, data=request.POST.copy())
        if form.is_valid():
            
            return HttpResponseRedirect("/%s/scripts/" % settings.WEBADMIN_ROOT_BASE)
        context = {'info':info, 'eventContext':eventContext, 'form':form}
    elif action == "edit":
        controller.getScript(sc_id)
        form = ScriptForm(initial={'name':controller.details.val.path.val, 'content':controller.script, 'size':controller.details.val.size.val})
        context = {'info':info, 'eventContext':eventContext, 'form':form, 'sc_id': sc_id}
    else:
        return HttpResponseRedirect("/%s/scripts/" % settings.WEBADMIN_ROOT_BASE)
    
    t = template_loader.get_template(template)
    c = Context(request, context)
    rsp = t.render(c)
    return HttpResponse(rsp)

@isAdminConnected
def imports(request, **kwargs):
    return HttpResponseRedirect("/%s/" % (settings.WEBADMIN_ROOT_BASE))

@isUserConnected
def my_account(request, action=None, **kwargs):
    myaccount = True
    template = "omeroadmin/myaccount.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'myaccount':myaccount}
    eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin }
    
    myaccount = BaseExperimenter(conn)
    myaccount.getMyDetails()
    
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
    
    edit_mode = False
    photo_size = conn.getExperimenterPhotoSize()
    form_file = UploadPhotoForm()
    
    if action == "save":
        form = MyAccountForm(data=request.POST.copy(), initial={'groups':myaccount.otherGroups})
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
            myaccount.updateMyAccount(firstName, lastName, email, defaultGroup, middleName, institution, password)
            logout(request)
            return HttpResponseRedirect("/%s/myaccount/" % (settings.WEBADMIN_ROOT_BASE))
    elif action == "upload":
        if request.method == 'POST':
            form_file = UploadPhotoForm(request.POST, request.FILES)
            if form_file.is_valid():
                controller = BaseUploadFile(conn)
                controller.attach_photo(request.FILES['photo'])
                return HttpResponseRedirect("/%s/myaccount/" % (settings.WEBADMIN_ROOT_BASE))
    elif action == "crop": 
        x1 = long(request.REQUEST['x1'].encode('utf-8'))
        x2 = long(request.REQUEST['x2'].encode('utf-8'))
        y1 = long(request.REQUEST['y1'].encode('utf-8'))
        y2 = long(request.REQUEST['y2'].encode('utf-8'))
        box = (x1,y1,x2,y2)
        conn.cropExperimenterPhoto(box)
        return HttpResponseRedirect("/%s/myaccount/" % (settings.WEBADMIN_ROOT_BASE))
    elif action == "editphoto":
        if photo_size is not None:
            edit_mode = True
    
    context = {'info':info, 'eventContext':eventContext, 'form':form, 'form_file':form_file, 'ldapAuth': myaccount.ldapAuth, 'edit_mode':edit_mode, 'photo_size':photo_size}
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
    template = "omeroadmin/drivespace.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'drivespace':drivespace}
    eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin }
    controller = BaseDriveSpace(conn)
    controller.pieChartData()
    
    for item in controller.topTen:
        if item[0] == "free space":
            controller.topTen.remove(item)
            continue
        if item[0] == "rest":
            controller.topTen.remove(item)
            continue
    
    context = {'info':info, 'eventContext':eventContext, 'driveSpace': {'free':controller.freeSpace, 'used':controller.usedSpace}, 'topTen':controller.topTen}
    
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
    
    from StringIO import StringIO
    from PIL import Image as PILImage
    
    from matplotlib.backends.backend_agg import FigureCanvasAgg as FigureCanvas
    from matplotlib.figure import Figure
    
    controller = BaseDriveSpace(conn)
    controller.pieChartData()
    
    values = list()
    keys = list()
    for item in controller.topTen:
        keys.append(str(item[0]))
        values.append(long(item[1]))
    
    fig = Figure()
    canvas = FigureCanvas(fig)
    ax = fig.add_subplot(111)

    explode = list()
    explode.append(0.1)
    for e in controller.topTen:
        explode.append(0)
    explode.remove(0)

    ax.pie(values, labels=tuple(keys), explode=tuple(explode), autopct='%1.1f%%', shadow=False)
    ax.set_title(_("Repository information status"))
    ax.grid(True)
    canvas.draw()
    size = canvas.get_renderer().get_canvas_width_height()
    buf=canvas.tostring_rgb()
    im=PILImage.fromstring('RGB', size, buf, 'raw', 'RGB', 0, 1)
    imdata=StringIO()
    im.save(imdata, format='PNG')
    return HttpResponse(imdata.getvalue(), mimetype='image/png')

