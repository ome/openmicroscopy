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
# Static values

help_wiki = "<small><p>If you'd like to include an OMERO image or container in the above field, simply get the id of the item from the URL and type:<br/><b>[image:157], </b><b>[dataset:64], </b><b>[project:76].</b></p><p>In addition, for URL please type <b>http://www.openmicroscopy.org.uk/</b></p></small>"

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
from custom_forms import PermissionCheckboxSelectMultiple, MultiEmailField, UrlField, MetadataModelChoiceField, \
                         AnnotationModelMultipleChoiceField
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
    
    message = forms.CharField(widget=forms.Textarea(attrs={'rows': 10, 'cols': 70}), help_text=help_wiki) 
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

    comment = forms.CharField(widget=forms.Textarea(attrs={'rows': 10, 'cols': 60}), help_text=help_wiki)
    
class ContainerForm(forms.Form):
    
    PERMISSION_CHOICES = (
        ('r', 'read'),
        ('w', 'write'),
    )

    name = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':61}))
    description = forms.CharField(widget=forms.Textarea(attrs={'rows': 10, 'cols': 60}), required=False, help_text=help_wiki)
    owner = forms.MultipleChoiceField(PERMISSION_CHOICES, widget=PermissionCheckboxSelectMultiple, required=False)
    group = forms.MultipleChoiceField(PERMISSION_CHOICES, widget=PermissionCheckboxSelectMultiple, required=False)
    world = forms.MultipleChoiceField(PERMISSION_CHOICES, widget=PermissionCheckboxSelectMultiple, required=False)

class CommentAnnotationForm(forms.Form):
    content = forms.CharField(widget=forms.Textarea(attrs={'rows': 9, 'cols': 65}))

class UriAnnotationForm(forms.Form):
    link = UrlField(widget=forms.TextInput(attrs={'size':55}))

class TagFilterForm(forms.Form):
    
    def __init__(self, *args, **kwargs):
        super(TagFilterForm, self).__init__(*args, **kwargs)
        try:
            if kwargs['initial']['tag']: pass
            self.fields['tag'] = forms.CharField(widget=forms.TextInput(attrs={'size':25}), initial=kwargs['initial']['tag'], required=False)
        except:
            self.fields['tag'] = forms.CharField(widget=forms.TextInput(attrs={'size':25}), required=False)
        
        try:
            if kwargs['initial']['tag2']: pass
            self.fields['tag2'] = forms.CharField(widget=forms.TextInput(attrs={'size':25}), initial=kwargs['initial']['tag2'], required=False)
        except:
            self.fields['tag2'] = forms.CharField(widget=forms.TextInput(attrs={'size':25}), required=False)
        
        try:
            if kwargs['initial']['tag3']: pass
            self.fields['tag3'] = forms.CharField(widget=forms.TextInput(attrs={'size':25}), initial=kwargs['initial']['tag3'], required=False)
        except:
            self.fields['tag3'] = forms.CharField(widget=forms.TextInput(attrs={'size':25}), required=False)
        
        try:
            if kwargs['initial']['tag4']: pass
            self.fields['tag4'] = forms.CharField(widget=forms.TextInput(attrs={'size':25}), initial=kwargs['initial']['tag4'], required=False)
        except:
            self.fields['tag4'] = forms.CharField(widget=forms.TextInput(attrs={'size':25}), required=False)
        
        try:
            if kwargs['initial']['tag5']: pass
            self.fields['tag5'] = forms.CharField(widget=forms.TextInput(attrs={'size':25}), initial=kwargs['initial']['tag5'], required=False)
        except:
            self.fields['tag5'] = forms.CharField(widget=forms.TextInput(attrs={'size':25}), required=False)
        
        self.fields.keyOrder = ['tag', 'tag2', 'tag3', 'tag4', 'tag5']

class TagAnnotationForm(forms.Form):
    tag = forms.CharField(widget=forms.TextInput(attrs={'size':45}))
    description = forms.CharField(widget=forms.Textarea(attrs={'rows': 7, 'cols': 42}), required=False)

class TagListForm(forms.Form):
    
    def __init__(self, *args, **kwargs):
        super(TagListForm, self).__init__(*args, **kwargs)
        self.fields['tags'] = AnnotationModelMultipleChoiceField(queryset=kwargs['initial']['tags'], widget=forms.SelectMultiple(attrs={'size':10, 'class':'existing'}), required=False)
        self.fields.keyOrder = ['tags']

class CommentListForm(forms.Form):
    
    def __init__(self, *args, **kwargs):
        super(CommentListForm, self).__init__(*args, **kwargs)
        self.fields['comments'] = AnnotationModelMultipleChoiceField(queryset=kwargs['initial']['comments'], widget=forms.SelectMultiple(attrs={'size':10, 'class':'existing'}), required=False)
        self.fields.keyOrder = ['comments']

class UrlListForm(forms.Form):
    
    def __init__(self, *args, **kwargs):
        super(UrlListForm, self).__init__(*args, **kwargs)
        self.fields['urls'] = AnnotationModelMultipleChoiceField(queryset=kwargs['initial']['urls'], widget=forms.SelectMultiple(attrs={'size':10, 'class':'existing'}), required=False)
        self.fields.keyOrder = ['urls']

class FileListForm(forms.Form):
    
    def __init__(self, *args, **kwargs):
        super(FileListForm, self).__init__(*args, **kwargs)
        self.fields['files'] = AnnotationModelMultipleChoiceField(queryset=kwargs['initial']['files'], widget=forms.SelectMultiple(attrs={'size':10, 'class':'existing'}), required=False)
        self.fields.keyOrder = ['files']

class UploadFileForm(forms.Form):
    annotation_file  = forms.FileField()

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
        #('renderdef', 'Views'),
    )
    
    data_type = forms.ChoiceField(choices=HISTORY_CHOICES,  widget=forms.Select(attrs={'onchange':'window.location.href=\'?history_type=\'+this.options[this.selectedIndex].value'}))


###############################
# METADATA FORMS

class MetadataObjectiveForm(forms.Form):
    
    BOOLEAN_CHOICES = (
        ('', '---------'),
        ('True', 'True'),
        ('False', 'False'),
    )
    
    def __init__(self, *args, **kwargs):
        super(MetadataObjectiveForm, self).__init__(*args, **kwargs)
        
        # Objective Settings
        
        # Correction Collar
        try:
            if kwargs['initial']['image'].getObjectiveSettings():
                self.fields['correctionCollar'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'correctionCollar\', this.value);'}), initial=kwargs['initial']['image'].getObjectiveSettings().correctionCollar, label="Calibrated collar", required=False)
            else:
                self.fields['correctionCollar'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", label="Calibrated collar", required=False)
                self.fields['correctionCollar'].widget.attrs['disabled'] = True 
                self.fields['correctionCollar'].widget.attrs['class'] = 'disable'
        except:
            self.fields['correctionCollar'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", label="Calibrated collar", required=False)
            self.fields['correctionCollar'].widget.attrs['disabled'] = True 
            self.fields['correctionCollar'].widget.attrs['class'] = 'disable'
        
        # Medium
        try:
            if kwargs['initial']['image'].getObjectiveSettings():
                self.fields['medium'] = MetadataModelChoiceField(queryset=kwargs['initial']['mediums'], empty_label=u"---------", widget=forms.Select(attrs={'onchange':'saveMetadata('+str(kwargs['initial']['image'].id)+', \'medium\', this.options[this.selectedIndex].value);'}), initial=kwargs['initial']['image'].getObjectiveSettings().medium, required=False) 
            else:
                self.fields['medium'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", required=False)
                self.fields['medium'].widget.attrs['disabled'] = True 
                self.fields['medium'].widget.attrs['class'] = 'disable'
        except:
            self.fields['medium'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", required=False)
            self.fields['medium'].widget.attrs['disabled'] = True 
            self.fields['medium'].widget.attrs['class'] = 'disable'
        
        # Refractive Index
        try:
            if kwargs['initial']['image'].getObjectiveSettings():
                self.fields['refractiveIndex'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'refractiveIndex\', this.value);'}), initial=kwargs['initial']['image'].getObjectiveSettings().refractiveIndex, label="Refractive index", required=False)
            else:
                self.fields['refractiveIndex'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", label="Refractive index", required=False)
                self.fields['refractiveIndex'].widget.attrs['disabled'] = True 
                self.fields['refractiveIndex'].widget.attrs['class'] = 'disable'
        except:
            self.fields['refractiveIndex'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", label="Refractive index", required=False)
            self.fields['refractiveIndex'].widget.attrs['disabled'] = True 
            self.fields['refractiveIndex'].widget.attrs['class'] = 'disable'
        
        # Objective
        
        # Manufacturer
        try:
            if kwargs['initial']['image'].getObjective():
                self.fields['manufacturer'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'manufacturer\', this.value);'}), initial=kwargs['initial']['image'].getObjective().manufacturer, required=False)
            else:
                self.fields['manufacturer'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", required=False)
                self.fields['manufacturer'].widget.attrs['disabled'] = True 
                self.fields['manufacturer'].widget.attrs['class'] = 'disable'
        except:
            self.fields['manufacturer'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", required=False)
            self.fields['manufacturer'].widget.attrs['disabled'] = True 
            self.fields['manufacturer'].widget.attrs['class'] = 'disable'
        
        # Model
        try:
            if kwargs['initial']['image'].getObjective():
                self.fields['model'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'model\', this.value);'}), initial=kwargs['initial']['image'].getObjective().model, required=False)
            else:
                self.fields['model'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", required=False)
                self.fields['model'].widget.attrs['disabled'] = True 
                self.fields['model'].widget.attrs['class'] = 'disable'
        except:
            self.fields['model'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", required=False)
            self.fields['model'].widget.attrs['disabled'] = True 
            self.fields['model'].widget.attrs['class'] = 'disable'
        
        # Serial Number
        try:
            if kwargs['initial']['image'].getObjective():
                self.fields['serialNumber'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'serialNumber\', this.value);'}), initial=kwargs['initial']['image'].getObjective().serialNumber, label="Serial number", required=False)
            else:
                self.fields['serialNumber'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", label="Serial number", required=False)
                self.fields['serialNumber'].widget.attrs['disabled'] = True 
                self.fields['serialNumber'].widget.attrs['class'] = 'disable'
        except:
            self.fields['serialNumber'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", label="Serial number", required=False)
            self.fields['serialNumber'].widget.attrs['disabled'] = True 
            self.fields['serialNumber'].widget.attrs['class'] = 'disable'
        
        # Nominal Magnification
        try:
            if kwargs['initial']['image'].getObjective():
                self.fields['nominalMagnification'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'nominalMagnification\', this.value);'}), initial=kwargs['initial']['image'].getObjective().nominalMagnification, label="Nominal magnification", required=False)
            else:
                self.fields['nominalMagnification'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), label="Nominal magnification", initial="N/A", required=False)
                self.fields['nominalMagnification'].widget.attrs['disabled'] = True 
                self.fields['nominalMagnification'].widget.attrs['class'] = 'disable'
        except:
            self.fields['nominalMagnification'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", label="Nominal magnification", required=False)
            self.fields['nominalMagnification'].widget.attrs['disabled'] = True 
            self.fields['nominalMagnification'].widget.attrs['class'] = 'disable'
        
        # Calibrated Magnification
        try:
            if kwargs['initial']['image'].getObjective():
                self.fields['calibratedMagnification'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'calibratedMagnification\', this.value);'}), initial=kwargs['initial']['image'].getObjective().calibratedMagnification, label="Calibrated magnification", required=False)
            else:
                self.fields['calibratedMagnification'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", label="Calibrated magnification", required=False)
                self.fields['calibratedMagnification'].widget.attrs['disabled'] = True 
                self.fields['calibratedMagnification'].widget.attrs['class'] = 'disable'
        except:
            self.fields['calibratedMagnification'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", label="Calibrated magnification", required=False)
            self.fields['calibratedMagnification'].widget.attrs['disabled'] = True 
            self.fields['calibratedMagnification'].widget.attrs['class'] = 'disable'
        
        # Lens NA
        try:
            if kwargs['initial']['image'].getObjective():
                self.fields['lensNA'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'lensNA\', this.value);'}), initial=kwargs['initial']['image'].getObjective().lensNA, label="Lens NA", required=False)
            else:
                self.fields['lensNA'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", label="Lens NA", required=False)
                self.fields['lensNA'].widget.attrs['disabled'] = True 
                self.fields['lensNA'].widget.attrs['class'] = 'disable'
        except:
            self.fields['lensNA'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", label="Lens NA", required=False)
            self.fields['lensNA'].widget.attrs['disabled'] = True 
            self.fields['lensNA'].widget.attrs['class'] = 'disable'
        
        # Immersion
        try:
            if kwargs['initial']['image'].getObjective():
                self.fields['immersion'] = MetadataModelChoiceField(queryset=kwargs['initial']['immersions'], empty_label=u"---------", widget=forms.Select(attrs={'onchange':'saveMetadata('+str(kwargs['initial']['image'].id)+', \'immersion\', this.options[this.selectedIndex].value);'}), initial=kwargs['initial']['image'].getObjective().immersion, required=False) 
            else:
                self.fields['immersion'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", required=False)
                self.fields['immersion'].widget.attrs['disabled'] = True 
                self.fields['immersion'].widget.attrs['class'] = 'disable'
        except:
            self.fields['immersion'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", required=False)
            self.fields['immersion'].widget.attrs['disabled'] = True 
            self.fields['immersion'].widget.attrs['class'] = 'disable'
        
        # Correction
        try:
            if kwargs['initial']['image'].getObjective():
                self.fields['correction'] = MetadataModelChoiceField(queryset=kwargs['initial']['corrections'], empty_label=u"---------", widget=forms.Select(attrs={'onchange':'saveMetadata('+str(kwargs['initial']['image'].id)+', \'correction\', this.options[this.selectedIndex].value);'}), initial=kwargs['initial']['image'].getObjective().correction, required=False) 
            else:
                self.fields['correction'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", required=False)
                self.fields['correction'].widget.attrs['disabled'] = True 
                self.fields['correction'].widget.attrs['class'] = 'disable'
        except:
            self.fields['correction'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", required=False)
            self.fields['correction'].widget.attrs['disabled'] = True 
            self.fields['correction'].widget.attrs['class'] = 'disable'
        
        # Working Distance
        try:
            if kwargs['initial']['image'].getObjective():
                self.fields['workingDistance'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'workingDistance\', this.value);'}), initial=kwargs['initial']['image'].getObjective().workingDistance, label="Working distance", required=False)
            else:
                self.fields['workingDistance'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", label="Working distance", required=False)
                self.fields['workingDistance'].widget.attrs['disabled'] = True 
                self.fields['workingDistance'].widget.attrs['class'] = 'disable'
        except:
            self.fields['workingDistance'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", label="Working distance", required=False)
            self.fields['workingDistance'].widget.attrs['disabled'] = True 
            self.fields['workingDistance'].widget.attrs['class'] = 'disable'
        
        # Iris
        try:
            if kwargs['initial']['image'].getObjective() is not None:
                self.fields['iris'] = forms.ChoiceField(choices=self.BOOLEAN_CHOICES,  widget=forms.Select(attrs={'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'iris\', this.options[this.selectedIndex].value);'}), required=False)
            else:
                self.fields['iris'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", required=False)
                self.fields['iris'].widget.attrs['disabled'] = True 
                self.fields['iris'].widget.attrs['class'] = 'disable'
        except:
            self.fields['iris'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", required=False)
            self.fields['iris'].widget.attrs['disabled'] = True 
            self.fields['iris'].widget.attrs['class'] = 'disable'
        
        self.fields.keyOrder = ['correction', 'correctionCollar', 'calibratedMagnification', 'immersion', 'iris', 'lensNA', 'manufacturer', 'medium', 'model', 'nominalMagnification', 'refractiveIndex', 'serialNumber', 'workingDistance'] 
    
    
class MetadataEnvironmentForm(forms.Form):
    
    def __init__(self, *args, **kwargs):
        super(MetadataEnvironmentForm, self).__init__(*args, **kwargs)
        
        # Imaging environment 
        
        # Temperature
        try:
            if kwargs['initial']['image'].getImagingEnvironment() is not None:
                self.fields['temperature'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'temperature\', this.value);'}), initial=kwargs['initial']['image'].getImagingEnvironment().temperature, required=False)
            else:
                self.fields['temperature'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", required=False)
                self.fields['temperature'].widget.attrs['disabled'] = True 
                self.fields['temperature'].widget.attrs['class'] = 'disable'
        except:
            self.fields['temperature'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", required=False)
            self.fields['temperature'].widget.attrs['disabled'] = True 
            self.fields['temperature'].widget.attrs['class'] = 'disable'
        
        # Air Pressure
        try:
            if kwargs['initial']['image'].getImagingEnvironment() is not None:
                self.fields['airPressure'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'airPressure\', this.value);'}), initial=kwargs['initial']['image'].getImagingEnvironment().airPressure, label="Air Pressure", required=False)
            else:
                self.fields['airPressure'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), label="Air Pressure", initial="N/A", required=False)
                self.fields['airPressure'].widget.attrs['disabled'] = True 
                self.fields['airPressure'].widget.attrs['class'] = 'disable'
        except:
            self.fields['airPressure'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), label="Air Pressure", initial="N/A", required=False)
            self.fields['airPressure'].widget.attrs['disabled'] = True 
            self.fields['airPressure'].widget.attrs['class'] = 'disable'
        
        # Humidity
        try:
            if kwargs['initial']['image'].getImagingEnvironment() is not None:
                self.fields['humidity'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'humidity\', this.value);'}), initial=kwargs['initial']['image'].getImagingEnvironment().humidity, required=False)
            else:
                self.fields['humidity'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", required=False)
                self.fields['humidity'].widget.attrs['disabled'] = True 
                self.fields['humidity'].widget.attrs['class'] = 'disable'
        except:
            self.fields['humidity'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", required=False)
            self.fields['humidity'].widget.attrs['disabled'] = True 
            self.fields['humidity'].widget.attrs['class'] = 'disable'
        
        # CO2 percent
        try:
            if kwargs['initial']['image'].getImagingEnvironment() is not None:
                self.fields['co2percent'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'co2percent\', this.value);'}), initial=kwargs['initial']['image'].getImagingEnvironment().co2percent, label="CO2 [%]", required=False)
            else:
                self.fields['co2percent'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", label="CO2 [%]", required=False)
                self.fields['co2percent'].widget.attrs['disabled'] = True 
                self.fields['co2percent'].widget.attrs['class'] = 'disable'
        except:
            self.fields['co2percent'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", label="CO2 [%]", required=False)
            self.fields['co2percent'].widget.attrs['disabled'] = True 
            self.fields['co2percent'].widget.attrs['class'] = 'disable'
        
        self.fields.keyOrder = ['airPressure', 'co2percent', 'humidity', 'temperature']

class MetadataStageLabelForm(forms.Form):
    
    def __init__(self, *args, **kwargs):
        super(MetadataStageLabelForm, self).__init__(*args, **kwargs)
        
        # Stage label
        
        # Position x
        try:
            if kwargs['initial']['image'].getStageLabel() is not None:
                self.fields['positionx'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'positionx\', this.value);'}), initial=kwargs['initial']['image'].getStageLabel().positionx, label="Position X", required=False)
            else:
                self.fields['positionx'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", label="Position X", required=False)
                self.fields['positionx'].widget.attrs['disabled'] = True 
                self.fields['positionx'].widget.attrs['class'] = 'disable'
        except:
            self.fields['positionx'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", label="Position X", required=False)
            self.fields['positionx'].widget.attrs['disabled'] = True 
            self.fields['positionx'].widget.attrs['class'] = 'disable'
        
        # Position y
        try:
            if kwargs['initial']['image'].getStageLabel() is not None:
                self.fields['positiony'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'positiony\', this.value);'}), initial=kwargs['initial']['image'].getStageLabel().positiony, label="Position Y", required=False)
            else:
                self.fields['positiony'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", label="Position Y", required=False)
                self.fields['positiony'].widget.attrs['disabled'] = True 
                self.fields['positiony'].widget.attrs['class'] = 'disable'
        except:
            self.fields['positiony'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", label="Position Y", required=False)
            self.fields['positiony'].widget.attrs['disabled'] = True 
            self.fields['positiony'].widget.attrs['class'] = 'disable'
        
        # Position z
        try:
            if kwargs['initial']['image'].getStageLabel() is not None:
                self.fields['positionz'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'positionz\', this.value);'}), initial=kwargs['initial']['image'].getStageLabel().positionz, label="Position Z", required=False)
            else:
                self.fields['positionz'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", label="Position Z", required=False)
                self.fields['positionz'].widget.attrs['disabled'] = True 
                self.fields['positionz'].widget.attrs['class'] = 'disable'
        except:
            self.fields['positionz'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':15}), initial="N/A", label="Position Z", required=False)
            self.fields['positionz'].widget.attrs['disabled'] = True 
            self.fields['positionz'].widget.attrs['class'] = 'disable'
        
        self.fields.keyOrder = ['positionx', 'positiony', 'positionz']