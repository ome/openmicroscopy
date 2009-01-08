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
from custom_forms import PermissionCheckboxSelectMultiple, MultiEmailField, UrlField, MetadataModelChoiceField
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
    content = forms.CharField(widget=forms.Textarea(attrs={'rows': 9, 'cols': 65}))

class UrlAnnotationForm(forms.Form):
    link = UrlField(widget=forms.TextInput(attrs={'size':55}))

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
                self.fields['correctionCollar'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'correctionCollar\', this.value);'}), initial=kwargs['initial']['image'].getObjectiveSettings().correctionCollar, label="Calibrated collar", required=False)
                if kwargs['initial']['image'].getObjectiveSettings().correctionCollar is not None:
                    self.fields['correctionCollar'].widget.attrs['disabled'] = True
                    self.fields['correctionCollar'].widget.attrs['class'] = 'disable'
            else:
                self.fields['correctionCollar'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'correctionCollar\', this.value);'}), label="Calibrated Collar", required=False)
        except:
            self.fields['correctionCollar'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'correctionCollar\', this.value);'}), label="Calibrated Collar", required=False)
        
        # Medium
        try:
            if kwargs['initial']['image'].getObjectiveSettings():
                if kwargs['initial']['image'].getObjectiveSettings().medium is None:
                    self.fields['medium'] = MetadataModelChoiceField(queryset=kwargs['initial']['mediums'], empty_label=u"---------", widget=forms.Select(attrs={'onchange':'saveMetadata('+str(kwargs['initial']['image'].id)+', \'medium\', this.options[this.selectedIndex].value);'}), required=False) 
                else:
                    self.fields['medium'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), initial=kwargs['initial']['image'].getObjectiveSettings().correctionCollar, required=False)
                    self.fields['medium'].widget.attrs['disabled'] = True
                    self.fields['medium'].widget.attrs['class'] = 'disable'
            else:
                self.fields['medium'] = MetadataModelChoiceField(queryset=kwargs['initial']['mediums'], empty_label=u"---------", widget=forms.Select(attrs={'onchange':'saveMetadata('+str(kwargs['initial']['image'].id)+', \'medium\', this.options[this.selectedIndex].value);'}), required=False) 
        except:
            self.fields['medium'] = MetadataModelChoiceField(queryset=kwargs['initial']['mediums'], empty_label=u"---------", widget=forms.Select(attrs={'onchange':'saveMetadata('+str(kwargs['initial']['image'].id)+', \'medium\', this.options[this.selectedIndex].value);'}), required=False) 
        
        # Refractive Index
        try:
            if kwargs['initial']['image'].getObjectiveSettings():
                self.fields['refractiveIndex'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'refractiveIndex\', this.value);'}), initial=kwargs['initial']['image'].getObjectiveSettings().refractiveIndex, label="Refractive index", required=False)
                if kwargs['initial']['image'].getObjectiveSettings().refractiveIndex is not None:
                    self.fields['refractiveIndex'].widget.attrs['disabled'] = True
                    self.fields['refractiveIndex'].widget.attrs['class'] = 'disable'
            else:
                self.fields['refractiveIndex'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'refractiveIndex\', this.value);'}), label="Refractive index", required=False)
        except:
            self.fields['refractiveIndex'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'refractiveIndex\', this.value);'}), label="Refractive index", required=False)
        
        # Objective
        
        # Manufacturer
        try:
            if kwargs['initial']['image'].getObjective():
                self.fields['manufacturer'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'manufacturer\', this.value);'}), initial=kwargs['initial']['image'].getObjective().manufacturer, required=False)
                if kwargs['initial']['image'].getObjective().manufacturer is not None:
                    self.fields['manufacturer'].widget.attrs['disabled'] = True
                    self.fields['manufacturer'].widget.attrs['class'] = 'disable'
            else:
                self.fields['manufacturer'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'manufacturer\', this.value);'}), required=False)
        except:
            self.fields['manufacturer'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'manufacturer\', this.value);'}), required=False)
        
        # Model
        try:
            if kwargs['initial']['image'].getObjective():
                self.fields['model'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'model\', this.value);'}), initial=kwargs['initial']['image'].getObjective().model, required=False)
                if kwargs['initial']['image'].getObjective().model is not None:
                    self.fields['model'].widget.attrs['disabled'] = True
                    self.fields['model'].widget.attrs['class'] = 'disable'
            else:
                self.fields['model'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'model\', this.value);'}), required=False)
        except:
            self.fields['model'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'model\', this.value);'}), required=False)
        
        # Serial Number
        try:
            if kwargs['initial']['image'].getObjective():
                self.fields['serialNumber'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'serialNumber\', this.value);'}), initial=kwargs['initial']['image'].getObjective().serialNumber, label="Serial number", required=False)
                if kwargs['initial']['image'].getObjective().serialNumber is not None:
                    self.fields['serialNumber'].widget.attrs['disabled'] = True
                    self.fields['serialNumber'].widget.attrs['class'] = 'disable'
            else:
                self.fields['serialNumber'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'serialNumber\', this.value);'}), required=False)
        except:
            self.fields['serialNumber'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'serialNumber\', this.value);'}), required=False)
        
        # Nominal Magnification
        try:
            if kwargs['initial']['image'].getObjective():
                self.fields['nominalMagnification'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'nominalMagnification\', this.value);'}), initial=kwargs['initial']['image'].getObjective().nominalMagnification, label="Nominal magnification", required=False)
                if kwargs['initial']['image'].getObjective().nominalMagnification is not None:
                    self.fields['nominalMagnification'].widget.attrs['disabled'] = True
                    self.fields['nominalMagnification'].widget.attrs['class'] = 'disable'
            else:
                self.fields['nominalMagnification'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'nominalMagnification\', this.value);'}), label="Nominal magnification", required=False)
        except:
            self.fields['nominalMagnification'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'nominalMagnification\', this.value);'}), label="Nominal magnification", required=False)
        
        # Calibrated Magnification
        try:
            if kwargs['initial']['image'].getObjective():
                self.fields['calibratedMagnification'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'calibratedMagnification\', this.value);'}), initial=kwargs['initial']['image'].getObjective().calibratedMagnification, label="Calibrated magnification", required=False)
                if kwargs['initial']['image'].getObjective().calibratedMagnification is not None:
                    self.fields['calibratedMagnification'].widget.attrs['disabled'] = True
                    self.fields['calibratedMagnification'].widget.attrs['class'] = 'disable'
            else:
                self.fields['calibratedMagnification'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'calibratedMagnification\', this.value);'}), label="Calibrated magnification", required=False)
        except:
            self.fields['calibratedMagnification'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'calibratedMagnification\', this.value);'}), label="Calibrated magnification", required=False)
        
        # Lens NA
        try:
            if kwargs['initial']['image'].getObjective():
                self.fields['lensNA'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'lensNA\', this.value);'}), initial=kwargs['initial']['image'].getObjective().lensNA, label="Lens NA", required=False)
                if kwargs['initial']['image'].getObjective().lensNA is not None:
                    self.fields['lensNA'].widget.attrs['disabled'] = True
                    self.fields['lensNA'].widget.attrs['class'] = 'disable'
            else:
                self.fields['lensNA'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'lensNA\', this.value);'}), label="Lens NA", required=False)
        except:
            self.fields['lensNA'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'lensNA\', this.value);'}), label="Lens NA", required=False)
        
        # Immersion
        try:
            if kwargs['initial']['image'].getObjective():
                if kwargs['initial']['image'].getObjective().immersion is None:
                    self.fields['immersion'] = MetadataModelChoiceField(queryset=kwargs['initial']['immersions'], empty_label=u"---------", widget=forms.Select(attrs={'onchange':'saveMetadata('+str(kwargs['initial']['image'].id)+', \'immersion\', this.options[this.selectedIndex].value);'}), required=False) 
                else:
                    self.fields['immersion'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), initial=kwargs['initial']['image'].getObjective().immersion, required=False)
                    self.fields['immersion'].widget.attrs['disabled'] = True
                    self.fields['immersion'].widget.attrs['class'] = 'disable'
            else:
                self.fields['immersion'] = MetadataModelChoiceField(queryset=kwargs['initial']['immersions'], empty_label=u"---------", widget=forms.Select(attrs={'onchange':'saveMetadata('+str(kwargs['initial']['image'].id)+', \'immersion\', this.options[this.selectedIndex].value);'}), required=False) 
        except:
            self.fields['immersion'] = MetadataModelChoiceField(queryset=kwargs['initial']['immersions'], empty_label=u"---------", widget=forms.Select(attrs={'onchange':'saveMetadata('+str(kwargs['initial']['image'].id)+', \'immersion\', this.options[this.selectedIndex].value);'}), required=False) 
        
        # Correction
        try:
            if kwargs['initial']['image'].getObjective():
                if kwargs['initial']['image'].getObjective().correction is None:
                    self.fields['correction'] = MetadataModelChoiceField(queryset=kwargs['initial']['corrections'], empty_label=u"---------", widget=forms.Select(attrs={'onchange':'saveMetadata('+str(kwargs['initial']['image'].id)+', \'correction\', this.options[this.selectedIndex].value);'}), required=False) 
                else:
                    self.fields['correction'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), initial=kwargs['initial']['image'].getObjective().correction, required=False)
                    self.fields['correction'].widget.attrs['disabled'] = True
                    self.fields['correction'].widget.attrs['class'] = 'disable'
            else:
                self.fields['correction'] = MetadataModelChoiceField(queryset=kwargs['initial']['corrections'], empty_label=u"---------", widget=forms.Select(attrs={'onchange':'saveMetadata('+str(kwargs['initial']['image'].id)+', \'correction\', this.options[this.selectedIndex].value);'}), required=False) 
        except:
            self.fields['correction'] = MetadataModelChoiceField(queryset=kwargs['initial']['corrections'], empty_label=u"---------", widget=forms.Select(attrs={'onchange':'saveMetadata('+str(kwargs['initial']['image'].id)+', \'correction\', this.options[this.selectedIndex].value);'}), required=False) 
        
        # Working Distance
        try:
            if kwargs['initial']['image'].getObjective():
                self.fields['workingDistance'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'workingDistance\', this.value);'}), initial=kwargs['initial']['image'].getObjective().workingDistance, label="Working distance", required=False)
                if kwargs['initial']['image'].getObjective().workingDistance is not None:
                    self.fields['workingDistance'].widget.attrs['disabled'] = True
                    self.fields['workingDistance'].widget.attrs['class'] = 'disable'
            else:
                self.fields['workingDistance'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'workingDistance\', this.value);'}), label="Working distance", required=False)
        except:
            self.fields['workingDistance'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'workingDistance\', this.value);'}), label="Working distance", required=False)
        
        # Iris
        try:
            if kwargs['initial']['image'].getObjective():
                if kwargs['initial']['image'].getObjective().iris is None:
                    self.fields['iris'] = forms.ChoiceField(choices=self.BOOLEAN_CHOICES,  widget=forms.Select(attrs={'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'iris\', this.options[this.selectedIndex].value);'}), required=False)
                else:
                    self.fields['iris'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), initial=kwargs['initial']['image'].getObjective().iris, required=False)
                    self.fields['iris'].widget.attrs['disabled'] = True
                    self.fields['iris'].widget.attrs['class'] = 'disable'
            else:
                self.fields['iris'] = forms.ChoiceField(choices=self.BOOLEAN_CHOICES,  widget=forms.Select(attrs={'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'iris\', this.options[this.selectedIndex].value);'}), required=False)
        except:
            self.fields['iris'] = forms.ChoiceField(choices=self.BOOLEAN_CHOICES,  widget=forms.Select(attrs={'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'iris\', this.options[this.selectedIndex].value);'}), required=False)
        
        
        self.fields.keyOrder = ['correction', 'correctionCollar', 'calibratedMagnification', 'immersion', 'iris', 'lensNA', 'manufacturer', 'medium', 'model', 'nominalMagnification', 'refractiveIndex', 'serialNumber', 'workingDistance'] 

class MetadataInstrumentForm(forms.Form):
    
    def __init__(self, *args, **kwargs):
        super(MetadataInstrumentForm, self).__init__(*args, **kwargs)
        
        # Instrument Settings
        
        # Microscope
        try:
            if kwargs['initial']['image'].getInstrument():
                self.fields['microscope'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), initial=kwargs['initial']['image'].getInstrument().microscope, required=False)
                if kwargs['initial']['image'].getInstrument().microscope is not None:
                    self.fields['microscope'].widget.attrs['disabled'] = True
                    self.fields['microscope'].widget.attrs['class'] = 'disable'
            else:
                self.fields['microscope'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), required=False)
        except:
            self.fields['microscope'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), required=False)
        
        # Detector Seq
        try:
            if kwargs['initial']['image'].getInstrument().detectorLoaded:
                self.fields['detectorSeq'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), initial=kwargs['initial']['image'].getInstrument().detectorSeq, required=False)
                if kwargs['initial']['image'].getInstrument().detectorSeq is not None:
                    self.fields['detectorSeq'].widget.attrs['disabled'] = True
                    self.fields['detectorSeq'].widget.attrs['class'] = 'disable'
            else:
                self.fields['detectorSeq'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), required=False)
        except:
            self.fields['detectorSeq'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), required=False)
        
        # Objective Seq
        try:
            if kwargs['initial']['image'].getInstrument().objectiveLoaded:
                self.fields['objectiveSeq'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), initial=kwargs['initial']['image'].getInstrument().objectiveSeq, required=False)
                if kwargs['initial']['image'].getInstrument().objectiveSeq is not None:
                    self.fields['objectiveSeq'].widget.attrs['disabled'] = True
                    self.fields['objectiveSeq'].widget.attrs['class'] = 'disable'
            else:
                self.fields['objectiveSeq'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), required=False)
        except:
            self.fields['objectiveSeq'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), required=False)
        
        # Light Source Seq
        try:
            if kwargs['initial']['image'].getInstrument().lightSourceLoaded:
                self.fields['lightSourceSeq'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), initial=kwargs['initial']['image'].getInstrument().lightSourceSeq, required=False)
                if kwargs['initial']['image'].getInstrument().lightSourceSeq is not None:
                    self.fields['lightSourceSeq'].widget.attrs['disabled'] = True
                    self.fields['lightSourceSeq'].widget.attrs['class'] = 'disable'
            else:
                self.fields['lightSourceSeq'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), required=False)
        except:
            self.fields['lightSourceSeq'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), required=False)
        
        # Filter Seq
        try:
            if kwargs['initial']['image'].getInstrument().filterLoaded:
                self.fields['filterSeq'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), initial=kwargs['initial']['image'].getInstrument().filterSeq, required=False)
                if kwargs['initial']['image'].getInstrument().filterSeq is not None:
                    self.fields['filterSeq'].widget.attrs['disabled'] = True
                    self.fields['filterSeq'].widget.attrs['class'] = 'disable'
            else:
                self.fields['filterSeq'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), required=False)
        except:
            self.fields['filterSeq'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), required=False)
        
        # Dichroic Seq
        try:
            if kwargs['initial']['image'].getInstrument().dichroicLoaded:
                self.fields['dichroicSeq'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), initial=kwargs['initial']['image'].getInstrument().dichroicSeq, required=False)
                if kwargs['initial']['image'].getInstrument().dichroicSeq is not None:
                    self.fields['dichroicSeq'].widget.attrs['disabled'] = True
                    self.fields['dichroicSeq'].widget.attrs['class'] = 'disable'
            else:
                self.fields['dichroicSeq'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), required=False)
        except:
            self.fields['dichroicSeq'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), required=False)
        
        # Filter Set Seq
        try:
            if kwargs['initial']['image'].getInstrument().filterSetLoaded:
                self.fields['filterSetSeq'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), initial=kwargs['initial']['image'].getInstrument().filterSetSeq, required=False)
                if kwargs['initial']['image'].getInstrument().filterSetSeq is not None:
                    self.fields['filterSetSeq'].widget.attrs['disabled'] = True
                    self.fields['filterSetSeq'].widget.attrs['class'] = 'disable'
            else:
                self.fields['filterSetSeq'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), required=False)
        except:
            self.fields['filterSetSeq'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), required=False)
        
        # Otf Seq
        try:
            if kwargs['initial']['image'].getInstrument().otfLoaded:
                self.fields['otfSeq'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), initial=kwargs['initial']['image'].getInstrument().otfSeq, required=False)
                if kwargs['initial']['image'].getInstrument().otfSeq is not None:
                    self.fields['otfSeq'].widget.attrs['disabled'] = True
                    self.fields['otfSeq'].widget.attrs['class'] = 'disable'
            else:
                self.fields['otfSeq'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), required=False)
        except:
            self.fields['otfSeq'] = forms.CharField(max_length=5, widget=forms.TextInput(attrs={'size':15}), required=False)
        
        
        self.fields.keyOrder = ['microscope', 'detectorSeq', 'objectiveSeq', 'lightSourceSeq', 'filterSeq', 'dichroicSeq', 'filterSetSeq', 'otfSeq']
    
class MetadataEnvironmentForm(forms.Form):
    
    def __init__(self, *args, **kwargs):
        super(MetadataEnvironmentForm, self).__init__(*args, **kwargs)
        
        # Condition
        
        # Temperature
        try:
            if kwargs['initial']['image'].getCondition():
                self.fields['temperature'] = forms.CharField(max_length=50, widget=forms.TextInput(attrs={'size':15}), initial=kwargs['initial']['image'].getCondition().temperature, required=False)
                if kwargs['initial']['image'].getCondition().temperature is not None:
                    self.fields['temperature'].widget.attrs['disabled'] = True
                    self.fields['temperature'].widget.attrs['class'] = 'disable'
            else:
                self.fields['temperature'] = forms.CharField(max_length=50, widget=forms.TextInput(attrs={'size':15}), required=False)
        except:
            self.fields['temperature'] = forms.CharField(max_length=50, widget=forms.TextInput(attrs={'size':15}), required=False)
        
        # Air Pressure
        try:
            if kwargs['initial']['image'].getCondition():
                self.fields['air_pressure'] = forms.CharField(max_length=50, widget=forms.TextInput(attrs={'size':15}), initial=kwargs['initial']['image'].getCondition().airPressure, required=False)
                if kwargs['initial']['image'].getCondition().air_pressure is not None:
                    self.fields['air_pressure'].widget.attrs['disabled'] = True
                    self.fields['air_pressure'].widget.attrs['class'] = 'disable'
            else:
                self.fields['air_pressure'] = forms.CharField(max_length=50, widget=forms.TextInput(attrs={'size':15}), required=False)
        except:
            self.fields['air_pressure'] = forms.CharField(max_length=50, widget=forms.TextInput(attrs={'size':15}), required=False)
        
        # Humidity
        try:
            if kwargs['initial']['image'].getCondition():
                self.fields['humidity'] = forms.CharField(max_length=50, widget=forms.TextInput(attrs={'size':15}), initial=kwargs['initial']['image'].getCondition().humidity, required=False)
                if kwargs['initial']['image'].getCondition().humidity is not None:
                    self.fields['humidity'].widget.attrs['disabled'] = True
                    self.fields['humidity'].widget.attrs['class'] = 'disable'
            else:
                self.fields['humidity'] = forms.CharField(max_length=50, widget=forms.TextInput(attrs={'size':15}), required=False)
        except:
            self.fields['humidity'] = forms.CharField(max_length=50, widget=forms.TextInput(attrs={'size':15}), required=False)
        
        # CO2 percent
        try:
            if kwargs['initial']['image'].getCondition():
                self.fields['co2percent'] = forms.CharField(max_length=50, widget=forms.TextInput(attrs={'size':15}), initial=kwargs['initial']['image'].getCondition().co2percent, label="CO2 [%]", required=False)
                if kwargs['initial']['image'].getCondition().co2percent is not None:
                    self.fields['co2percent'].widget.attrs['disabled'] = True
                    self.fields['co2percent'].widget.attrs['class'] = 'disable'
            else:
                self.fields['co2percent'] = forms.CharField(max_length=50, widget=forms.TextInput(attrs={'size':15}), label="CO2 [%]", required=False)
        except:
            self.fields['co2percent'] = forms.CharField(max_length=50, widget=forms.TextInput(attrs={'size':15}), label="CO2 [%]", required=False)
        
        
        self.fields.keyOrder = ['air_pressure', 'co2percent', 'humidity', 'temperature']