#!/usr/bin/env python
# 
# WebAdmin views definitions.
# 
# Copyright (c) 2008 University of Dundee. All rights reserved.
# This file is distributed under the same license as the OMERO package.
# Use is subject to license terms supplied in LICENSE.txt
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

from controller.experimenter import BaseExperimenters, BaseExperimenter
from controller.group import BaseGroups, BaseGroup
from controller.script import BaseScripts
from controller.drivespace import BaseDriveSpace

from models import Gateway, LoginForm, ExperimenterForm, ExperimenterLdapForm, \
                   GroupForm, ScriptForm, MyAccountForm, MyAccountLdapForm, \
                   ContainedExperimentersForm

from extlib.gateway import BlitzGateway
from extlib.sendemail.sendemail import SendEmial

logger = logging.getLogger('views')

connectors = {}

logger.info("INIT '%s'" % os.getpid())

try:
    if settings.EMAIL_ERROR_NOTIFICATION:
        emailsender = SendEmial()
except:
    logger.error(traceback.format_exc())

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
    client_base = None
    
    # gets Http session or create new one
    session_key = request.session.session_key
    
    # gets server base
    try:
        client_base = request.session['base']
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
    if (client_base and session_key) is not None:
        conn_key = 'S:' + str(request.session.session_key) + '#' + str(client_base)
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
                if request.session['server']: pass
                if request.session['port']: pass
                if request.session['login']: pass
                if request.session['password']: pass
            except KeyError:
                logger.error(traceback.format_exc())
                raise sys.exc_info()[1]
            else:
                # login parameters found, create the connection
                try:
                    conn = BlitzGateway(request.session['server'], request.session['port'], request.session['login'], request.session['password'])
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
                conn = BlitzGateway(request.session['server'], request.session['port'], request.session['login'], request.session['password'], request.session['sessionUuid'])
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
                if request.session['server']: pass
                if request.session['port']: pass
                if request.session['login']: pass
                if request.session['password']: pass
            except KeyError:
                logger.error(traceback.format_exc())
                raise sys.exc_info()[1]
            else:
                try:
                    conn = BlitzGateway(request.session['server'], request.session['port'], request.session['login'], request.session['password'], request.session['sessionUuid'])
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
            return HttpResponseRedirect("/%s/?error=%s" % (settings.WEBADMIN_ROOT_BASE, x.__class__.__name__))
        if conn is None:
            return HttpResponseRedirect("/%s/?error=%s" % (settings.WEBADMIN_ROOT_BASE, "Error: Connection does not exist"))
        if not conn.getEventContext().isAdmin:
            return page_not_found(request, "404.html")
        kwargs["conn"] = conn
        return f(request, *args, **kwargs)

    return wrapped

def isUserConnected (f):
    def wrapped (request, *args, **kwargs):
        #this check connection exist, if not it will redirect to login page
        conn = None
        try:
            conn = getConnection(request)
        except Exception, x:
            logger.error(traceback.format_exc())
            return HttpResponseRedirect("/%s/?error=%s" % (settings.WEBADMIN_ROOT_BASE, x.__class__.__name__))
        if conn is None:
            return HttpResponseRedirect("/%s/?error=%s" % (settings.WEBADMIN_ROOT_BASE, "Error: Connection does not exist"))
        kwargs["conn"] = conn
        return f(request, *args, **kwargs)

    return wrapped

################################################################################
# views controll

def index(request):
    error = None
    try:
        error = request.GET['error']
    except:
        pass
    
    conn = None
    try:
        conn = getConnection(request)
    except Exception, x:
        logger.error(traceback.format_exc())
        error = x.__class__.__name__
    
    if conn is None:
        template = "login.html"
        if request.method == 'POST':
            form = LoginForm(data=request.POST.copy())
        else:
            try:
                blitz = Gateway.objects.filter(id=request.session['base'])
                data = {'base': unicode(blitz[0].id), 'login':unicode(request.session['login']), 'password':unicode(request.session['password']) }
                form = LoginForm(data=data)
            except:
                form = LoginForm()
        context = {'error':error, 'form':form}
        t = template_loader.get_template(template)
        c = Context(request, context)
        rsp = t.render(c)
        return HttpResponse(rsp)
    else:
        if conn.getEventContext().isAdmin:
            return HttpResponseRedirect("/%s/experimenters/" % (settings.WEBADMIN_ROOT_BASE))
        else:
            return HttpResponseRedirect("/%s/myaccount/" % (settings.WEBADMIN_ROOT_BASE))

def login(request):
    if request.method == 'POST' and request.POST.get('base'):
        blitz = Gateway.objects.get(pk=request.POST.get('base'))
        request.session['base'] = blitz.id
        request.session['server'] = blitz.server
        request.session['port'] = blitz.port
        request.session['login'] = request.POST.get('login')
        request.session['password'] = request.POST.get('password')
    
    conn = None
    try:
        conn = getConnection(request)
    except Exception, x:
        logger.error(traceback.format_exc())
        error = x.__class__.__name__
    
    if conn is None:
        return HttpResponseRedirect("/%s/?error=%s" % (settings.WEBADMIN_ROOT_BASE, error))
    else:
        if conn.getEventContext().isAdmin:
            return HttpResponseRedirect("/%s/experimenters/" % (settings.WEBADMIN_ROOT_BASE))
        else:
            return HttpResponseRedirect("/%s/myaccount/" % (settings.WEBADMIN_ROOT_BASE))

def logout(request):
    try:
        conn = getConnection(request)
    except:
        logger.error(traceback.format_exc())
    else:
        try:
            session_key = "S:%s#%s" % (request.session.session_key,request.session['base'])
            if connectors.has_key(session_key):
                conn.seppuku()
                del connectors[session_key]
        except:
            logger.error(traceback.format_exc())
    
    try:
        del request.session['base']
    except KeyError:
        logger.error(traceback.format_exc())
    try:
        del request.session['server']
    except KeyError:
        logger.error(traceback.format_exc())
    try:
        del request.session['port']
    except KeyError:
        logger.error(traceback.format_exc())
    try:
        del request.session['login']
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
    
    request.session.set_expiry(1)
    return HttpResponseRedirect("/%s/" % (settings.WEBADMIN_ROOT_BASE))

@isAdminConnected
def experimenters(request, **kwargs):
    experimenters = True
    template = "experimenters.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    if conn is None:
        return HttpResponseRedirect("/%s/" % (settings.WEBADMIN_ROOT_BASE))
    else:
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
    template = "experimenter_form.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    if conn is None:
        return HttpResponseRedirect("/%s/" % settings.WEBADMIN_ROOT_BASE)
    else:
        info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'experimenters':experimenters}
        eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin }
        
        controller = BaseExperimenter(conn, eid)
        
        if action == 'new':
            form = ExperimenterForm(initial={'dgroups':controller.defaultGroupsInitialList(), 'groups':controller.otherGroupsInitialList()})
            context = {'info':info, 'eventContext':eventContext, 'form':form}
        elif action == 'create':
            name_check = conn.checkOmeName(request.POST.get('omename').encode('utf8'))
            email_check = conn.checkEmail(request.POST.get('email').encode('utf8'))
            form = ExperimenterForm(initial={'dgroups':controller.defaultGroupsInitialList(), 'groups':controller.otherGroupsInitialList()}, data=request.POST.copy(), name_check=name_check, email_check=email_check, passwd_check=True)
            if form.is_valid():
                omeName = request.POST.get('omename').encode('utf8')
                firstName = request.POST.get('first_name').encode('utf8')
                middleName = request.POST.get('middle_name').encode('utf8')
                lastName = request.POST.get('last_name').encode('utf8')
                email = request.POST.get('email').encode('utf8')
                institution = request.POST.get('institution').encode('utf8')
                admin = True if request.POST.get('administrator') else False
                active = True if request.POST.get('active') else False
                defaultGroup = request.POST.get('default_group').encode('utf8')
                otherGroups = request.POST.getlist('other_groups')
                if request.POST.get('password').encode('utf8') is None or request.POST.get('password').encode('utf8') == "":
                    password = "ome"
                else:
                    password = request.POST.get('password').encode('utf8')
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
            name_check = conn.checkOmeName(request.POST.get('omename').encode('utf8'), controller.experimenter.omeName)
            email_check = conn.checkEmail(request.POST.get('email').encode('utf8'), controller.experimenter.email)
            form = ExperimenterForm(initial={'dgroups':controller.defaultGroupsInitialList(), 'groups':controller.otherGroupsInitialList()}, data=request.POST.copy(), name_check=name_check, email_check=email_check)
            if form.is_valid():
                omeName = request.POST.get('omename').encode('utf8')
                firstName = request.POST.get('first_name').encode('utf8')
                middleName = request.POST.get('middle_name').encode('utf8')
                lastName = request.POST.get('last_name').encode('utf8')
                email = request.POST.get('email').encode('utf8')
                institution = request.POST.get('institution').encode('utf8')
                admin = True if request.POST.get('administrator') else False
                active = True if request.POST.get('active') else False
                defaultGroup = request.POST.get('default_group').encode('utf8')
                otherGroups = request.POST.getlist('other_groups')
                try:
                    if request.POST.get('password').encode('utf8') is None or request.POST.get('password').encode('utf8') == "":
                        password = None
                    else:
                        password = request.POST.get('password').encode('utf8')
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
    template = "groups.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    if conn is None:
        return HttpResponseRedirect("/%s/" % settings.WEBADMIN_ROOT_BASE)
    else:
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
    template = "group_form.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    if conn is None:
        return HttpResponseRedirect("/%s/" % settings.WEBADMIN_ROOT_BASE)
    else:
        info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'groups':groups}
        eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin }
        
        controller = BaseGroup(conn, gid)
        
        if action == 'new':
            form = GroupForm(initial={'experimenters':controller.experimenters})
            context = {'info':info, 'eventContext':eventContext, 'form':form}
        elif action == 'create':
            name_check = conn.checkGroupName(request.POST.get('name').encode('utf8'))
            form = GroupForm(initial={'experimenters':controller.experimenters}, data=request.POST.copy(), name_check=name_check)
            if form.is_valid():
                name = request.POST.get('name').encode('utf8')
                description = request.POST.get('description').encode('utf8')
                owner = request.POST.get('owner').encode('utf8')
                controller.createGroup(name, owner, description)
                return HttpResponseRedirect("/%s/groups/" % settings.WEBADMIN_ROOT_BASE)
            context = {'info':info, 'eventContext':eventContext, 'form':form}
        elif action == 'edit':
            form = GroupForm(initial={'name': controller.group.name, 'description':controller.group.description,
                                         'owner': controller.group.details.owner.id.val, 'experimenters':controller.experimenters})
            context = {'info':info, 'eventContext':eventContext, 'form':form, 'gid': gid}
        elif action == 'save':
            name_check = conn.checkGroupName(request.POST.get('name').encode('utf8'), controller.group.name)
            form = GroupForm(initial={'experimenters':controller.experimenters}, data=request.POST.copy(), name_check=name_check)
            if form.is_valid():
                name = request.POST.get('name').encode('utf8')
                description = request.POST.get('description').encode('utf8')
                owner = request.POST.get('owner').encode('utf8')
                controller.updateGroup(name, owner, description)
                return HttpResponseRedirect("/%s/groups/" % settings.WEBADMIN_ROOT_BASE)
            context = {'info':info, 'eventContext':eventContext, 'form':form, 'gid': gid}
        elif action == "update":
            template = "group_edit.html"
            controller.containedExperimenters()
            form = ContainedExperimentersForm(initial={'members':controller.members, 'available':controller.available})
            if not form.is_valid():
                available = request.POST.getlist('available')
                members = request.POST.getlist('members')
                controller.setMembersOfGroup(available, members)
                return HttpResponseRedirect("/%s/groups/" % settings.WEBADMIN_ROOT_BASE)
            context = {'info':info, 'eventContext':eventContext, 'form':form, 'controller': controller}
        elif action == "members":
            template = "group_edit.html"
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
    template = "scripts.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    if conn is None:
        return HttpResponseRedirect("/%s/" % settings.WEBADMIN_ROOT_BASE)
    else:
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
    template = "script_form.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    if conn is None:
        return HttpResponseRedirect("/%s/" % settings.WEBADMIN_ROOT_BASE)
    else:
        info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'scripts':scripts}
        eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin }
        controller = BaseScripts(conn, sc_id)
        
        if action == 'new':
            form = ScriptForm(initial={'script':controller.script})
            context = {'info':info, 'eventContext':eventContext, 'form':form}
        elif action == 'save':
            form = GroupForm(initial={'script':controller.script}, data=request.POST.copy())
            if form.is_valid():
                
                return HttpResponseRedirect("/%s/scripts/" % settings.WEBADMIN_ROOT_BASE)
            context = {'info':info, 'eventContext':eventContext, 'form':form}
        elif action == "edit":
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
    template = "myaccount.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    if conn is None:
        return HttpResponseRedirect("/%s/" % settings.WEBADMIN_ROOT_BASE)
    else:
        info = {'today': _("Today is %(tday)s") % {'tday': datetime.date.today()}, 'myaccount':myaccount}
        eventContext = {'userName':conn.getEventContext().userName, 'isAdmin':conn.getEventContext().isAdmin }
        
        myaccount = BaseExperimenter(conn)
        myaccount.getMyDetails()
        
        if action == "save":
            form = MyAccountForm(data=request.POST.copy(), initial={'groups':myaccount.otherGroups})
            if form.is_valid():
                firstName = request.POST.get('first_name').encode('utf8')
                middleName = request.POST.get('middle_name').encode('utf8')
                lastName = request.POST.get('last_name').encode('utf8')
                email = request.POST.get('email').encode('utf8')
                institution = request.POST.get('institution').encode('utf8')
                defaultGroup = request.POST.get('default_group').encode('utf8')
                try:
                    if request.POST.get('password').encode('utf8') is None or request.POST.get('password').encode('utf8') == "":
                        password = None
                    else:
                        password = request.POST.get('password').encode('utf8')
                except:
                    password = None
                myaccount.updateMyAccount(firstName, lastName, email, defaultGroup, middleName, institution, password)
                logout(request)
                return HttpResponseRedirect("/%s/" % (settings.WEBADMIN_ROOT_BASE))
        else:
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
        
        context = {'info':info, 'eventContext':eventContext, 'form':form, 'ldapAuth': myaccount.ldapAuth}
        t = template_loader.get_template(template)
        c = Context(request,context)
        return HttpResponse(t.render(c))

@isUserConnected
def drivespace(request, **kwargs):
    drivespace = True
    template = "drivespace.html"
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    if conn is None:
        return HttpResponseRedirect("/%s/" % settings.WEBADMIN_ROOT_BASE)
    else:
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
    from StringIO import StringIO
    from PIL import Image as PILImage

    from matplotlib.backends.backend_agg import FigureCanvasAgg as FigureCanvas
    from matplotlib.figure import Figure
    
    conn = None
    try:
        conn = kwargs["conn"]
    except:
        logger.error(traceback.format_exc())
    
    if conn is None:
        return HttpResponseRedirect("/%s/" % settings.WEBADMIN_ROOT_BASE)
    else:
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
        #ax.set_xlabel('time')
        #ax.set_ylabel('volts')
        canvas.draw()
        size = canvas.get_renderer().get_canvas_width_height()
        buf=canvas.tostring_rgb()
        im=PILImage.fromstring('RGB', size, buf, 'raw', 'RGB', 0, 1)
        imdata=StringIO()
        im.save(imdata, format='PNG')
        return HttpResponse(imdata.getvalue(), mimetype='image/png')

################################################################################
# handlers

def handler404(request):
    logger.error('handler404: Page not found')
    exc_info = sys.exc_info()
    logger.error(traceback.format_exc())
    if settings.EMAIL_ERROR_NOTIFICATION:
        try:
            emailsender.create_error_message("webadmin", debug.technical_404_response(request, exc_info[1]))
            logger.debug('handler404: Email to queue')
        except:
            logger.error('handler404: Email could not be sent')
            logger.error(traceback.format_exc())
    return page_not_found(request, "404.html")

def handler500(request):
    logger.error('handler500: Server error')
    exc_info = sys.exc_info()
    logger.error(traceback.format_exc())
    if settings.EMAIL_ERROR_NOTIFICATION:
        try:
            emailsender.create_error_message("webadmin", debug.technical_500_response(request, *exc_info))
            logger.debug('handler500: Email to queue')
        except:
            logger.error('handler500: Email could not be sent')
            logger.error(traceback.format_exc())
    return server_error(request, "500.html")
