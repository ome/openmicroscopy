#
# models.py - django application model description
# 
# Copyright (c) 2007, 2008 Glencoe Software, Inc. All rights reserved.
# 
# This software is distributed under the terms described by the LICENCE file
# you can find at the root of the distribution bundle, which states you are
# free to use it only for non commercial purposes.
# If the file is missing please request a copy by contacting
# jason@glencoesoftware.com.

from django import forms
from django.db import models
from django.conf import settings
from django.forms import ModelForm
from django.forms.widgets import Textarea
from django.forms.widgets import HiddenInput

from omeroweb.webadmin.models import Gateway

import re

##################################################################
# Model

class CategoryAdvice(models.Model):
    category = models.CharField(max_length=100)
    description = models.CharField(max_length=2000)

    def __unicode__(self):
        c = "%s" % (self.category)
        return c

class Advice(models.Model):
    title = models.CharField(max_length=100)
    description = models.TextField(max_length=2000)
    rating = models.PositiveIntegerField(default=0)
    category = models.ForeignKey(CategoryAdvice)
    

    def __unicode__(self):
        t = "%s" % (self.title)
        return t
    
    def shortDescription(self):
        try:
            desc = "%s" % (self.description)
            l = len(desc)
            if l < 50:
                return desc
            return "..." + desc[l - 50:]
        except:
            return self.description

#################################################################
# Non-model Form
from custom_forms import PermissionCheckboxSelectMultiple, MultiEmailField, UrlField
from omeroweb.webadmin.custom_forms import ExperimenterModelChoiceField, \
                        GroupModelChoiceField, ExperimenterModelMultipleChoiceField

import datetime
import time
class ShareForm(forms.Form):
    
    def __init__(self, *args, **kwargs):
        super(ShareForm, self).__init__(*args, **kwargs)
        try:
            if kwargs['initial']['shareMembers']: pass
            self.fields['members'] = ExperimenterModelMultipleChoiceField(queryset=kwargs['initial']['experimenters'], initial=kwargs['initial']['shareMembers'], required=False, widget=forms.SelectMultiple(attrs={'size':7}))
        except:
            self.fields['members'] = ExperimenterModelMultipleChoiceField(queryset=kwargs['initial']['experimenters'], required=False, widget=forms.SelectMultiple(attrs={'size':7}))
        self.fields.keyOrder = ['message', 'expiretion', 'enable', 'members']#, 'guests']
    
    message = forms.CharField(widget=forms.Textarea(attrs={'rows': 10, 'cols': 70})) 
    expiretion = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':20}))
    enable = forms.CharField(widget=forms.CheckboxInput(attrs={'size':1}), required=False)
    #guests = MultiEmailField(required=False, widget=forms.TextInput(attrs={'size':75}))

    def clean_expiretion(self):
        d = self.cleaned_data['expiretion'].rsplit("-")
        # only for python 2.5
        # date = datetime.datetime.strptime(("%s-%s-%s" % (d[0],d[1],d[2])), "%Y-%m-%d")
        date = datetime.datetime(*(time.strptime(("%s-%s-%s" % (d[0],d[1],d[2])), "%Y-%m-%d")[0:6]))
        if time.time() >= time.mktime(date.timetuple()):
            raise forms.ValidationError('Expire date must be in the future.')

class ShareCommentForm(forms.Form):

    comment = forms.CharField(widget=forms.Textarea(attrs={'rows': 10, 'cols': 60}))
    
class ContainerForm(forms.Form):
    
    PERMISSION_CHOICES = (
        ('r', 'read'),
        ('w', 'write'),
    )

    name = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':61}))
    description = forms.CharField(widget=forms.Textarea(attrs={'rows': 10, 'cols': 60}), required=False)
    owner = forms.MultipleChoiceField(PERMISSION_CHOICES, widget=PermissionCheckboxSelectMultiple, required=False)
    group = forms.MultipleChoiceField(PERMISSION_CHOICES, widget=PermissionCheckboxSelectMultiple, required=False)
    world = forms.MultipleChoiceField(PERMISSION_CHOICES, widget=PermissionCheckboxSelectMultiple, required=False)

class TextAnnotationForm(forms.Form):
    content = forms.CharField(widget=forms.Textarea(attrs={'rows': 10, 'cols': 60}))

class UrlAnnotationForm(forms.Form):
    link = UrlField(widget=forms.TextInput(attrs={'size':61}))

class UploadFileForm(forms.Form):
    custom_file  = forms.FileField()

class MyGroupsForm(forms.Form):

    def __init__(self, *args, **kwargs):
        super(MyGroupsForm, self).__init__(*args, **kwargs)
        try:
            if kwargs['initial']['mygroup']: pass
            self.fields['group'] = GroupModelChoiceField(queryset=kwargs['initial']['mygroups'], initial=kwargs['initial']['mygroup'], widget=forms.Select(attrs={'onchange':'window.location.href=\'/'+settings.WEBCLIENT_ROOT_BASE+'/groupdata/?group=\'+this.options[this.selectedIndex].value'}), required=False)
        except:
            self.fields['group'] = GroupModelChoiceField(queryset=kwargs['initial']['mygroups'], widget=forms.Select(attrs={'onchange':'window.location.href=\'/'+settings.WEBCLIENT_ROOT_BASE+'/groupdata/?group=\'+this.options[this.selectedIndex].value'}), required=False)
        self.fields.keyOrder = ['group']

class MyUserForm(forms.Form):

    def __init__(self, *args, **kwargs):
        super(MyUserForm, self).__init__(*args, **kwargs)
        try:
            if kwargs['initial']['user']: pass
            self.fields['experimenter'] = ExperimenterModelChoiceField(queryset=kwargs['initial']['users'], initial=kwargs['initial']['user'], widget=forms.Select(attrs={'onchange':'window.location.href=\'/'+settings.WEBCLIENT_ROOT_BASE+'/userdata/?experimenter=\'+this.options[this.selectedIndex].value'}), required=False)
        except:
            self.fields['experimenter'] = ExperimenterModelChoiceField(queryset=kwargs['initial']['users'], widget=forms.Select(attrs={'onchange':'window.location.href=\'/'+settings.WEBCLIENT_ROOT_BASE+'/userdata/?experimenter=\'+this.options[this.selectedIndex].value'}), required=False)
        self.fields.keyOrder = ['experimenter']

class ActiveGroupForm(forms.Form):

    def __init__(self, *args, **kwargs):
        super(ActiveGroupForm, self).__init__(*args, **kwargs)
        self.fields['active_group'] = GroupModelChoiceField(queryset=kwargs['initial']['mygroups'], initial=kwargs['initial']['activeGroup'], empty_label=None, widget=forms.Select(attrs={'onchange':'window.location.href=\'/'+settings.WEBCLIENT_ROOT_BASE+'/active_group/?active_group=\'+this.options[this.selectedIndex].value'})) 
        self.fields.keyOrder = ['active_group']

class HistoryTypeForm(forms.Form):
    HISTORY_CHOICES = (
        ('all', '---------'),
        ('project', 'Projects'),
        ('dataset', 'Datasets'),
        ('image', 'Images'),
        ('renderdef', 'Views'),
    )
    
    data_type = forms.ChoiceField(choices=HISTORY_CHOICES,  widget=forms.Select(attrs={'onchange':'window.location.href=\'?history_type=\'+this.options[this.selectedIndex].value'}))