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

import traceback
import json

from api_query import query_projects, query_datasets
from omero_marshal import get_encoder, get_decoder, OME_SCHEMA_URL
from omero import ValidationException
from omeroweb.connector import Server
from omeroweb.api.api_exceptions import BadRequestError, NotFoundError, \
    CreatedObject
from omeroweb.api.decorators import login_required, json_response
from omeroweb.webgateway.util import getIntOrDefault

from django.http import JsonResponse

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


@json_response()
def api_versions(request, **kwargs):
    """Base url of the webgateway json api."""
    versions = []
    for v in settings.API_VERSIONS:
        versions.append({
            'version': v,
            'base_url': build_url(request, 'api_base', v)
        })
    return {'data': versions}


@json_response()
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


@json_response()
def api_token(request, api_version, **kwargs):
    """Provide CSRF token for current session."""
    token = csrf.get_token(request)
    return {'data': token}


@json_response()
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


class ObjectView(View):
    """Handle access to an individual Project to GET or DELETE it."""

    @method_decorator(login_required(useragent='OMERO.webapi'))
    @method_decorator(json_response())
    def dispatch(self, *args, **kwargs):
        """Wrap other methods to add decorators."""
        return super(ObjectView, self).dispatch(*args, **kwargs)

    def get(self, request, pid, conn=None, **kwargs):
        """Simply GET a single Object and marshal it or 404 if not found."""
        obj = conn.getObject(self.OMERO_TYPE, pid)
        if obj is None:
            return JsonResponse(
                {'message': '%s %s not found' % (self.OMERO_TYPE, pid)},
                status=404)
        encoder = get_encoder(obj._obj.__class__)
        return encoder.encode(obj._obj)

    def delete(self, request, pid, conn=None, **kwargs):
        """
        Delete the Project and return marshal of deleted Project.

        Return 404 if not found.
        """
        try:
            obj = conn.getQueryService().get(self.OMERO_TYPE, long(pid))
        except ValidationException:
            return JsonResponse(
                {'message': '%s %s not found' % (self.OMERO_TYPE, pid)},
                status=404)
        encoder = get_encoder(obj.__class__)
        json = encoder.encode(obj)
        conn.deleteObject(obj)
        return json


class ProjectView(ObjectView):
    OMERO_TYPE = 'Project'

    def get(self, request, pid, conn=None, **kwargs):
        o = super(ProjectView, self).get(request, pid, conn, **kwargs)
        if isinstance(o, JsonResponse):
            return o
        version = kwargs['api_version']
        datasets_url = build_url(request, 'api_project_datasets',
                                 version, pid=o['@id'])
        o['omero:datasets_url'] = datasets_url
        return o


class DatasetView(ObjectView):
    OMERO_TYPE = 'Dataset'


class ProjectsView(View):
    """Handles GET for /projects/ to list available Projects."""

    @method_decorator(login_required(useragent='OMERO.webapi'))
    @method_decorator(json_response())
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

        version = kwargs['api_version']
        for p in projects['data']:
            url = build_url(request, 'api_project_datasets',
                            version, pid=p['@id'])
            p['omero:datasets_url'] = url
        return projects


class DatasetsView(View):
    """Handles GET for /datasets/ to list available Datasets."""

    @method_decorator(login_required(useragent='OMERO.webapi'))
    @method_decorator(json_response())
    def dispatch(self, *args, **kwargs):
        """Use dispatch to add decorators to class methods."""
        return super(DatasetsView, self).dispatch(*args, **kwargs)

    def get(self, request, pid=None, conn=None, **kwargs):
        """GET a list of Datasets, filtering by various request parameters."""
        try:
            page = getIntOrDefault(request, 'page', 1)
            limit = getIntOrDefault(request, 'limit', settings.PAGE)
            group = getIntOrDefault(request, 'group', -1)
            owner = getIntOrDefault(request, 'owner', -1)
            childCount = request.GET.get('childCount', False) == 'true'
            normalize = request.GET.get('normalize', False) == 'true'
            # filter by ?project=pid instead of /projects/:pdi/datasets/
            if pid is None:
                pid = getIntOrDefault(request, 'project', None)
        except ValueError as ex:
            raise BadRequestError(str(ex))

        # Get the datasets
        datasets = query_datasets(conn,
                                  project=pid,
                                  group=group,
                                  owner=owner,
                                  childCount=childCount,
                                  page=page,
                                  limit=limit,
                                  normalize=normalize)

        return datasets


class SaveView(View):
    """
    This view provides 'Save' functionality for all types of objects.

    POST to create a new Object and PUT to replace existing one.
    """

    @method_decorator(login_required(useragent='OMERO.webapi'))
    @method_decorator(json_response())
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
