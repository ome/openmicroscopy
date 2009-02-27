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

from omeroweb.feedback.models import ErrorForm
from omeroweb.extlib.notification.sendfeedback import SendFeedback

logger = logging.getLogger('views-feedback')

try:
    if settings.ERROR2EMAIL_NOTIFICATION and settings.EMAIL_NOTIFICATION:
        import omeroweb.extlib.notification.handlesender as sender
        logger.info("Email sender imported")
except:
    logger.error(traceback.format_exc())
    
###############################################################################
def thanks(request):
    return render_to_response("thanks.html",None)

def send_feedback(request):
    error = None
    form = ErrorForm(data=request.REQUEST.copy())
    if form.is_valid():
        error = request.REQUEST['error']
        comment = None
        if request.REQUEST['comment'] is not None or request.REQUEST['comment'] != "":
            comment = request.REQUEST['comment']
        email = None
        if request.REQUEST['email'] is not None or request.REQUEST['email'] != "":
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
                
        return HttpResponseRedirect("/feedback/thanks/")
        
    context = {'form':form, 'error':error}
    t = template_loader.get_template('500.html') 
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
    exc_info = sys.exc_info()
    logger.error(traceback.format_exc())
    
    error500 = debug.technical_500_response(request, *exc_info)
    
    if settings.ERROR2EMAIL_NOTIFICATION and settings.EMAIL_NOTIFICATION:
        try:
            sender.handler().create_error_message("OMERO.web", request.session['username'], error500)
            logger.info('handler500: Email to queue')
        except:
            logger.error('handler500: Email could not be sent')
            logger.error(traceback.format_exc())
    
    return custom_server_error(request, error500)

def handler404(request):
    logger.error('handler404: Page not found')
    exc_info = sys.exc_info()
    logger.error(traceback.format_exc())
    
    if settings.ERROR2EMAIL_NOTIFICATION and settings.EMAIL_NOTIFICATION:
        try:
            sender.handler().create_error_message("OMERO.web", request.session['username'], debug.technical_404_response(request, exc_info[1]))
            logger.info('handler404: Email to queue')
        except:
            logger.error('handler404: Email could not be sent')
            logger.error(traceback.format_exc())
    
    return page_not_found(request, "404.html")

def handlerInternalError(error):
    template = "error.html"
    context = {"error":error}
    return render_to_response(template,context)