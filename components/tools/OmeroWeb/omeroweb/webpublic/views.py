# Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
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
#

from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import get_object_or_404, render_to_response
from django.core.urlresolvers import reverse
from django.utils.datastructures import MergeDict
from omeroweb.webclient.views import isUserConnected, login as webclient_login
from omeroweb.webgateway.views import getBlitzConnection, _session_logout
from omeroweb.webpublic.models import Link
from omeroweb.webpublic.baseconv import base62
import settings
import logging
import traceback
import omero

logger = logging.getLogger('webpublic')
useragent = "OMERO.webpublic"

class UrlValidationError(Exception):
    def __init__(self, error):
        self.error = error

class DummyHttpRequest(object):
    def __init__(self, REQUEST, session, method):
        self.REQUEST = REQUEST
        self.session = session
        self.method = method

def _validate_url(url, request, conn):
    group = conn.getGroupFromContext()
    members = conn.containedExperimenters(group.id)
    webindex = request.build_absolute_uri(reverse('webindex'))
    if not hasattr(settings, 'PUBLIC_USER') \
       or not hasattr(settings, 'PUBLIC_PASSWORD'):
        raise UrlValidationError('Missing "omero.web.public.user" or ' \
                                 '"omero.web.public.password" configuration!')
    if not url.startswith(webindex):
        raise UrlValidationError('Not from this OMERO.web host!')
    if not group.details.permissions.isGroupRead():
        raise UrlValidationError('Group "%s" is not collaborative!' % \
                                 group.name)
    for member in members:
        if settings.PUBLIC_USER == member.omeName:
            return
    raise UrlValidationError('Public user "%s" not a member of "%s"!' % \
            (settings.PUBLIC_USER, group.name))

@isUserConnected
def index(request, **kwargs):
    conn = kwargs['conn']
    return render_to_response('webpublic/index.html', {'client': conn})

@isUserConnected
def publicise(request, **kwargs):
    template = 'webpublic/publicise.html'
    conn = kwargs['conn']
    url = request.POST.get('public_url')
    if url is None or len(url) == 0:
        return render_to_response(
                template, {'url': '', 'error': 'No URL specified!'})
    webindex = request.build_absolute_uri(reverse('webindex'))
    data = {'url': url}
    try:
        _validate_url(url, request, conn)
    except UrlValidationError, e:
        data['error'] = e.error
        return render_to_response(template, data)
    owner = conn.user.getId()
    group = conn.getGroupFromContext().id
    try:
        link = Link.objects.get(url=url, owner=owner)
    except Link.DoesNotExist:
        link = Link(url=url, owner=owner, group=group)
        link.save()
    data['tinyurl'] = reverse('webpublic_tinyurl', args=[link.to_base62()])
    data['tinyurl'] = request.build_absolute_uri(data['tinyurl'])
    return render_to_response(template, data)

@isUserConnected
def de_publicise(request, id, **kwargs):
    conn = kwargs['conn']
    owner = conn.user.getId()
    # Sanity check for "permissions" by using owner during lookup
    link = link = get_object_or_404(Link, id=id, owner=owner)
    link.delete()
    return HttpResponseRedirect(reverse('webpublic_user_listing'))

@isUserConnected
def user_listing(request, **kwargs):
    conn = kwargs['conn']
    owner = conn.user.getId()
    links = Link.objects.filter(owner=owner)
    data = {'client': conn, 'links': links}
    return render_to_response('webpublic/user_listing.html', data)

def tinyurl(request, base_62):
    base_62 = base62.to_decimal(base_62)
    link = get_object_or_404(Link, pk=base_62)
    REQUEST = {'url': link.url,
               'server': 1,
               'username': settings.PUBLIC_USER,
               'password': settings.PUBLIC_PASSWORD}
    REQUEST = MergeDict(REQUEST, request.REQUEST)
    dummy_request = DummyHttpRequest(REQUEST, request.session, request.method)
    to_return = webclient_login(dummy_request)
    conn = getBlitzConnection(dummy_request, useragent="OMERO.web")
    conn.changeActiveGroup(link.group)
    return to_return
