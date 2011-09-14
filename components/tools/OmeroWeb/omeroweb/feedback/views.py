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

import sys
import locale
import datetime
import traceback
import logging

from django.conf import settings
from django.core import template_loader
from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import render_to_response
from django.template import RequestContext as Context
from django.views.defaults import page_not_found, server_error
from django.views import debug
from django.core.urlresolvers import reverse

from omeroweb.feedback.sendfeedback import SendFeedback
from omeroweb.feedback.forms import ErrorForm, CommentForm

logger = logging.getLogger('views-feedback')

###############################################################################
def thanks(request):
    return render_to_response("thanks.html",None)

def send_feedback(request):
    error = None
    form = ErrorForm(data=request.REQUEST.copy())
    if form.is_valid():
        error = request.REQUEST['error']
        comment = None
        if request.REQUEST.get('comment',None) is not None and request.REQUEST['comment'] != "":
            comment = request.REQUEST['comment']
        email = None
        if request.REQUEST.get('email', None) is not None and request.REQUEST['email'] != "":
            email = request.REQUEST['email']
        try:
            sf = SendFeedback()
            sf.give_feedback(error, comment, email)
        except:
            logger.error('handler500: Feedback could not be sent')
            logger.error(traceback.format_exc())
            error = "Feedback could not been sent. Please contact administrator."
            try:
                fileObj = open(("%s/error500-%s.html" % (settings.LOGDIR, datetime.datetime.now())),"w")
                fileObj.write(request.REQUEST['error'])
                fileObj.close() 
            except:
                logger.error('handler500: Error could not be saved.')
                logger.error(traceback.format_exc())
                
        return HttpResponseRedirect(reverse("fthanks"))
        
    context = {'form':form, 'error':error}
    t = template_loader.get_template('500.html') 
    c = Context(request, context)
    return HttpResponse(t.render(c))

def send_comment(request):
    error = None
    form = CommentForm()    
    
    if request.method == "POST":
        form = CommentForm(data=request.REQUEST.copy())
        if form.is_valid():
            comment = request.REQUEST['comment']
            email = None
            if request.REQUEST['email'] is not None or request.REQUEST['email'] != "":
                email = request.REQUEST['email']
            try:
                sf = SendFeedback()
                sf.give_comment(comment, email)
            except:
                logger.error('handler500: Feedback could not be sent')
                logger.error(traceback.format_exc())
                error = "Feedback could not been sent. Please contact administrator."
            else:
                return HttpResponseRedirect(reverse("fthanks"))
        
    context = {'form':form, 'error':error}
    t = template_loader.get_template('comment.html') 
    c = Context(request, context)
    return HttpResponse(t.render(c))

def custom_server_error(request, error500):
    """
    Custom 500 error handler.

    Templates: `500.html`
    Context: ErrorForm
    """
    form = ErrorForm(initial={'error':error500})
    context = {'form':form}
    t = template_loader.get_template('500.html') 
    c = Context(request, context)
    return HttpResponse(t.render(c))

################################################################################
# handlers

def handler500(request):
    logger.error('handler500: Server error')
    as_string = '\n'.join(traceback.format_exception(*sys.exc_info()))
    logger.error(as_string)
        
    try:
        request_repr = repr(request)
    except:
        request_repr = "Request repr() unavailable"
        
    error500 = "%s\n\n%s" % (as_string, request_repr)
        
    return custom_server_error(request, error500)

def handler404(request):
    logger.error('handler404: Page not found')
    as_string = '\n'.join(traceback.format_exception(*sys.exc_info()))
    logger.error(as_string)
    
    try:
        request_repr = repr(request)
    except:
        request_repr = "Request repr() unavailable"
        
    error404 = "%s\n\n%s" % (as_string, request_repr)
    
    return page_not_found(request, "404.html")

def handlerInternalError(error):
    template = "error.html"
    context = {"error":error}
    return render_to_response(template,context)