#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
# All rights reserved.
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

"""Views.py for the OMERO JSON api app."""

from django.views.generic import View
from django.middleware import csrf
from django.utils.decorators import method_decorator
from django.core.urlresolvers import reverse
from django.conf import settings
from django.http import JsonResponse

import traceback
import json

from api_query import query_projects
from omeroweb.webadmin.forms import LoginForm
from omeroweb.decorators import get_client_ip
from omeroweb.connector import Connector
from omeroweb.webadmin.webadmin_utils import upgradeCheck
from omero_marshal import get_encoder, get_decoder, OME_SCHEMA_URL
from omero import ValidationException
from omeroweb.connector import Server
from omeroweb.api.api_exceptions import BadRequestError, NotFoundError, \
    CreatedObject
from omeroweb.api.decorators import LoginRequired, JsonResponseHandler
from omeroweb.webgateway.util import getIntOrDefault


def build_url(request, name, api_version, **kwargs):
    """
    Helper for generating urls within /api json responses.

    By default we use request.build_absolute_uri() but this
    can be configured by setting "omero.web.api.absolute_url"
    to a string or empty string, used to prefix relative urls.
    Extra **kwargs are passed to reverse() function.

    @param name:            Name of the url
    @param api_version      Version string
    """
    kwargs['api_version'] = api_version
    url = reverse(name, kwargs=kwargs)
    if settings.API_ABSOLUTE_URL is None:
        return request.build_absolute_uri(url)
    else:
        # remove trailing slash
        prefix = settings.API_ABSOLUTE_URL.rstrip('/')
        return "%s%s" % (prefix, url)


@JsonResponseHandler()
def api_versions(request, **kwargs):
    """Base url of the webgateway json api."""
    versions = []
    for v in settings.API_VERSIONS:
        versions.append({
            'version': v,
            'base_url': build_url(request, 'api_base', v)
        })
    return {'data': versions}


@JsonResponseHandler()
def api_base(request, api_version=None, **kwargs):
    """Base url of the webgateway json api for a specified version."""
    v = api_version
    rv = {'projects_url': build_url(request, 'api_projects', v),
          'token_url': build_url(request, 'api_token', v),
          'servers_url': build_url(request, 'api_servers', v),
          'login_url': build_url(request, 'api_login', v),
          'save_url': build_url(request, 'api_save', v),
          'schema_url': OME_SCHEMA_URL}
    return rv


@JsonResponseHandler()
def api_token(request, api_version, **kwargs):
    """Provide CSRF token for current session."""
    token = csrf.get_token(request)
    return {'data': token}


@JsonResponseHandler()
def api_servers(request, api_version, **kwargs):
    """List the available servers to connect to."""
    servers = []
    for i, obj in enumerate(Server):
        s = {'id': i + 1,
             'host': obj.host,
             'port': obj.port
             }
        if obj.server is not None:
            s['server'] = obj.server
        servers.append(s)
    return {'data': servers}


class LoginView(View):
    """Webgateway Login - Subclassed by WebclientLoginView."""

    form_class = LoginForm
    useragent = 'OMERO.webapi'

    def get(self, request, api_version=None):
        """Simply return a message to say GET not supported."""
        return JsonResponse({"message":
                            ("POST only with username, password, "
                             "server and csrftoken")},
                            status=405)

    def handle_logged_in(self, request, conn, connector):
        """Return a response for successful login."""
        c = conn.getEventContext()
        ctx = {}
        for a in ['sessionId', 'sessionUuid', 'userId', 'userName', 'groupId',
                  'groupName', 'isAdmin', 'eventId', 'eventType',
                  'memberOfGroups', 'leaderOfGroups']:
            if (hasattr(c, a)):
                ctx[a] = getattr(c, a)
        return JsonResponse({"success": True, "eventContext": ctx})

    def handle_not_logged_in(self, request, error=None, form=None):
        """
        Return a response for failed login.

        Reason for failure may be due to server 'error' or because
        of form validation errors.

        @param request:     http request
        @param error:       Error message
        @param form:        Instance of Login Form, populated with data
        """
        if error is None and form is not None:
            # If no error from server, maybe form wasn't valid
            formErrors = []
            for field in form:
                for e in field.errors:
                    formErrors.append("%s: %s" % (field.label, e))
            error = " ".join(formErrors)
        elif error is None:
            # Just in case no error or invalid form is given
            error = "Login failed. Reason unknown."
        return JsonResponse({"message": error}, status=403)

    def post(self, request, api_version=None):
        """
        Here we handle the main login logic, creating a connection to OMERO.

        and store that on the request.session OR handling login failures
        """
        error = None
        form = self.form_class(request.POST.copy())
        if form.is_valid():
            username = form.cleaned_data['username']
            password = form.cleaned_data['password']
            server_id = form.cleaned_data['server']
            is_secure = form.cleaned_data['ssl']

            connector = Connector(server_id, is_secure)

            # TODO: version check should be done on the low level, see #5983
            compatible = True
            if settings.CHECK_VERSION:
                compatible = connector.check_version(self.useragent)
            if (server_id is not None and username is not None and
                    password is not None and compatible):
                conn = connector.create_connection(
                    self.useragent, username, password,
                    userip=get_client_ip(request))
                if conn is not None:
                    request.session['connector'] = connector
                    # UpgradeCheck URL should be loaded from the server or
                    # loaded omero.web.upgrades.url allows to customize web
                    # only
                    try:
                        upgrades_url = settings.UPGRADES_URL
                    except:
                        upgrades_url = conn.getUpgradesUrl()
                    upgradeCheck(url=upgrades_url)
                    return self.handle_logged_in(request, conn, connector)
            # Once here, we are not logged in...
            # Need correct error message
            if not connector.is_server_up(self.useragent):
                error = ("Server is not responding,"
                         " please contact administrator.")
            elif not settings.CHECK_VERSION:
                error = ("Connection not available, please check your"
                         " credentials and version compatibility.")
            else:
                if not compatible:
                    error = ("Client version does not match server,"
                             " please contact administrator.")
                else:
                    error = ("Connection not available, please check your"
                             " user name and password.")
        return self.handle_not_logged_in(request, error, form)


class ProjectView(View):
    """Handle access to an individual Project to GET or DELETE it."""

    @method_decorator(LoginRequired(useragent='OMERO.webapi'))
    @method_decorator(JsonResponseHandler())
    def dispatch(self, *args, **kwargs):
        """Wrap other methods to add decorators."""
        return super(ProjectView, self).dispatch(*args, **kwargs)

    def get(self, request, pid, conn=None, **kwargs):
        """Simply GET a single Project and marshal it or 404 if not found."""
        project = conn.getObject("Project", pid)
        if project is None:
            raise NotFoundError('Project %s not found' % pid)
        encoder = get_encoder(project._obj.__class__)
        return encoder.encode(project._obj)

    def delete(self, request, pid, conn=None, **kwargs):
        """
        Delete the Project and return marshal of deleted Project.

        Return 404 if not found.
        """
        try:
            project = conn.getQueryService().get('Project', long(pid))
        except ValidationException:
            raise NotFoundError('Project %s not found' % pid)
        encoder = get_encoder(project.__class__)
        json = encoder.encode(project)
        conn.deleteObject(project)
        return json


class ProjectsView(View):
    """Handles GET for /projects/ to list available Projects."""

    @method_decorator(LoginRequired(useragent='OMERO.webapi'))
    @method_decorator(JsonResponseHandler())
    def dispatch(self, *args, **kwargs):
        """Use dispatch to add decorators to class methods."""
        return super(ProjectsView, self).dispatch(*args, **kwargs)

    def get(self, request, conn=None, **kwargs):
        """GET a list of Projects, filtering by various request parameters."""
        try:
            page = getIntOrDefault(request, 'page', 1)
            limit = getIntOrDefault(request, 'limit', settings.PAGE)
            group = getIntOrDefault(request, 'group', -1)
            owner = getIntOrDefault(request, 'owner', -1)
            childCount = request.GET.get('childCount', False) == 'true'
            normalize = request.GET.get('normalize', False) == 'true'
        except ValueError as ex:
            raise BadRequestError(str(ex))

        # Get the projects
        projects = query_projects(conn,
                                  group=group,
                                  owner=owner,
                                  childCount=childCount,
                                  page=page,
                                  limit=limit,
                                  normalize=normalize)

        return projects


class SaveView(View):
    """
    This view provides 'Save' functionality for all types of objects.

    POST to create a new Object and PUT to replace existing one.
    """

    @method_decorator(LoginRequired(useragent='OMERO.webapi'))
    @method_decorator(JsonResponseHandler())
    def dispatch(self, *args, **kwargs):
        """Apply decorators for class methods below."""
        return super(SaveView, self).dispatch(*args, **kwargs)

    def put(self, request, conn=None, **kwargs):
        """
        PUT handles saving of existing objects.

        Therefore '@id' should be set.
        """
        object_json = json.loads(request.body)
        if '@id' not in object_json:
            raise BadRequestError(
                "No '@id' attribute. Use POST to create new objects")
        return self._save_object(request, conn, object_json, **kwargs)

    def post(self, request, conn=None, **kwargs):
        """
        POST handles saving of NEW objects.

        Therefore '@id' should not be set.
        """
        object_json = json.loads(request.body)
        if '@id' in object_json:
            raise BadRequestError(
                "Object has '@id' attribute. Use PUT to update objects")
        rsp = self._save_object(request, conn, object_json, **kwargs)
        # will return 201 ('Created')
        raise CreatedObject(rsp)

    def _save_object(self, request, conn, object_json, **kwargs):
        """Here we handle the saving for PUT and POST."""
        # Try to get group from request, OR from details below...
        group = getIntOrDefault(request, 'group', None)
        decoder = None
        if '@type' not in object_json:
            raise BadRequestError('Need to specify @type attribute')
        objType = object_json['@type']
        decoder = get_decoder(objType)
        # If we are passed incomplete object, or decoder couldn't be found...
        if decoder is None:
            raise BadRequestError('No decoder found for type: %s' % objType)

        # Any marshal errors most likely due to invalid input. status=400
        try:
            obj = decoder.decode(object_json)
        except Exception:
            msg = 'Error in decode of json data by omero_marshal'
            raise BadRequestError(msg, traceback.format_exc())

        if group is None:
            try:
                # group might be None or unloaded
                group = obj.getDetails().group.id.val
            except AttributeError:
                # Instead of default stack trace, give nicer message:
                msg = ("Specify Group in omero:details or "
                       "query parameters ?group=:id")
                raise BadRequestError(msg)

        # If owner was unloaded (E.g. from get() above) or if missing
        # ome.model.meta.Experimenter.ldap (not supported by omero_marshal)
        # then saveObject() will give ValidationException.
        # Therefore we ignore any details for now:
        obj.unloadDetails()

        conn.SERVICE_OPTS.setOmeroGroup(group)
        obj = conn.getUpdateService().saveAndReturnObject(obj,
                                                          conn.SERVICE_OPTS)
        encoder = get_encoder(obj.__class__)
        return encoder.encode(obj)
