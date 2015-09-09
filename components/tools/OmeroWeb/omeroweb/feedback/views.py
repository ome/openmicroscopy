#!/usr/bin/env python
# -*- coding: utf-8 -*-
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

import sys
import datetime
import traceback
import logging

from django.conf import settings
from django.template import loader as template_loader
from django.http import HttpResponse, HttpResponseRedirect
from django.http import HttpResponseServerError, HttpResponseNotFound
from django.http import HttpResponseForbidden
from django.template import RequestContext
from django.views.defaults import page_not_found
from django.core.urlresolvers import reverse

from omeroweb.feedback.sendfeedback import SendFeedback
from omeroweb.feedback.forms import ErrorForm, CommentForm

logger = logging.getLogger(__name__)


def get_user_agent(request):
    user_agent = ""
    try:
        user_agent = request.META['HTTP_USER_AGENT']
    except:
        pass
    return user_agent


###############################################################################
def send_feedback(request):
    error = None
    form = ErrorForm(data=request.REQUEST.copy())
    if form.is_valid():
        error = request.REQUEST['error']
        comment = None
        if (request.REQUEST.get('comment', None) is not None and
                request.REQUEST['comment'] != ""):
            comment = request.REQUEST['comment']
        email = None
        if (request.REQUEST.get('email', None) is not None and
                request.REQUEST['email'] != ""):
            email = request.REQUEST['email']
        try:
            sf = SendFeedback(settings.FEEDBACK_URL)
            sf.send_feedback(error=error, comment=comment, email=email,
                             user_agent=get_user_agent(request))
        except Exception, e:
            logger.error('handler500: Feedback could not be sent')
            logger.error(traceback.format_exc())
            error = ("Feedback could not been sent. Please contact"
                     " administrator. %s" % e)
            fileObj = open(
                ("%s/error500-%s.html"
                 % (settings.LOGDIR, datetime.datetime.now())), "w")
            try:
                try:
                    fileObj.write(request.REQUEST['error'])
                except:
                    logger.error('handler500: Error could not be saved.')
                    logger.error(traceback.format_exc())
            finally:
                fileObj.close()
        else:
            if request.is_ajax():
                return HttpResponse(
                    "<h1>Thanks for your feedback</h1><p>You may need to"
                    " refresh your browser to recover from the error</p>")
            return HttpResponseRedirect(reverse("fthanks"))

    context = {'form': form, 'error': error}
    t = template_loader.get_template('500.html')
    c = RequestContext(request, context)
    return HttpResponse(t.render(c))


def send_comment(request):
    error = None
    form = CommentForm()

    if request.method == "POST":
        form = CommentForm(data=request.REQUEST.copy())
        if form.is_valid():
            comment = request.REQUEST['comment']
            email = None
            if (request.REQUEST['email'] is not None or
                    request.REQUEST['email'] != ""):
                email = request.REQUEST['email']
            try:
                sf = SendFeedback(settings.FEEDBACK_URL)
                sf.send_feedback(comment=comment, email=email,
                                 user_agent=get_user_agent(request))
            except:
                logger.error('handler500: Feedback could not be sent')
                logger.error(traceback.format_exc())
                error = ("Feedback could not been sent."
                         " Please contact administrator.")
            else:
                return HttpResponseRedirect(reverse("fthanks"))

    context = {'form': form, 'error': error}
    t = template_loader.get_template('comment.html')
    c = RequestContext(request, context)
    return HttpResponse(t.render(c))


def custom_server_error(request, error500):
    """
    Custom 500 error handler.

    Templates: `500.html`
    Context: ErrorForm
    """
    form = ErrorForm(initial={'error': error500})
    context = {'form': form}
    t = template_loader.get_template('500.html')
    c = RequestContext(request, context)
    return HttpResponse(t.render(c))

##############################################################################
# handlers


def csrf_failure(request, reason=""):
    logger.warn('csrf_failure: Forbidden')
    t = template_loader.get_template("403_csrf.html")
    c = RequestContext(request, {})
    return HttpResponseForbidden(t.render(c))


def handler500(request):
    """
    Custom error handling.
    Catches errors that are not handled elsewhere.
    NB: This only gets used by Django if omero.web.debug False (production use)
    If debug is True, Django returns it's own debug error page
    """
    logger.error('handler500: Server error')
    as_string = '\n'.join(traceback.format_exception(*sys.exc_info()))
    logger.error(as_string)

    try:
        request_repr = repr(request)
    except:
        request_repr = "Request repr() unavailable"

    error500 = "%s\n\n%s" % (as_string, request_repr)

    # If AJAX, return JUST the error message (not within html page)
    if request.is_ajax():
        return HttpResponseServerError(error500)

    form = ErrorForm(initial={'error': error500})
    context = {'form': form}
    t = template_loader.get_template('500.html')
    c = RequestContext(request, context)
    return HttpResponse(t.render(c))


def handler404(request):
    logger.warning(
        'Not Found: %s' % request.path,
        extra={
            'status_code': 404,
            'request': request})
    if request.is_ajax():
        msg = traceback.format_exception(*sys.exc_info())[-1]
        return HttpResponseNotFound(msg)

    return page_not_found(request, "404.html")


def handlerInternalError(request, error):
    """
    This is mostly used in an "object not found" situation,
    So there is no feedback form - simply display "not found" message.
    If the call was AJAX, we return the message in a 404 response.
    Otherwise return an html page, with 404 response.
    """
    logger.warning(
        'Object Not Found: %s' % request.path,
        extra={
            'status_code': 404,
            'request': request})

    if request.is_ajax():
        return HttpResponseNotFound(error)

    context = {"error": error}
    t = template_loader.get_template("error.html")
    c = RequestContext(request, context)
    return HttpResponseNotFound(t.render(c))
