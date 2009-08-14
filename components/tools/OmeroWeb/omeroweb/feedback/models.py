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

from django import forms
from django.db import models
from django.forms.widgets import Textarea

from omeroweb.webadmin.models import Gateway

class ErrorForm(forms.Form):
    
    email = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':50}), label="Your email", required=False)
    comment = forms.CharField(widget=forms.Textarea(attrs={'rows': 5, 'cols': 70}), required=False)
    error = forms.CharField(widget=forms.Textarea(attrs={'rows': 10, 'cols': 70}))

class EmailTemplate(models.Model):
    template = models.CharField(max_length=100)
    content_html = models.TextField()
    content_txt = models.TextField()
    
    def __unicode__(self):
        t = "%s" % (self.template)
        return t

class EmailToSend(models.Model):
    host = models.CharField(max_length=100)
    blitz = models.ForeignKey(Gateway)
    share = models.PositiveIntegerField()
    sender = models.CharField(max_length=100, blank=True, null=True)
    sender_email = models.CharField(max_length=100, blank=True, null=True)
    recipients = models.TextField()
    template = models.ForeignKey(EmailTemplate)
    	   
    def __init__(self, *args, **kwargs):
        super(EmailToSend, self).__init__(*args, **kwargs)
           
    def __unicode__(self):
        e = "%s %s on %s" % (self.sender, self.template, self.blitz.host)
        return e
