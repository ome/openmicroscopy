#!/usr/bin/env python
# -*- coding: utf-8 -*-
# 
# 
# 
# Copyright (c) 2008-2015 University of Dundee.
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

import datetime
import time
import logging

from django.conf import settings
from django import forms
from django.forms.formsets import formset_factory
from django.core.urlresolvers import reverse

from omeroweb.custom_forms import NonASCIIForm
from custom_forms import MetadataModelChoiceField, \
                        AnnotationModelMultipleChoiceField, \
                        ObjectModelMultipleChoiceField
from omeroweb.webadmin.custom_forms import ExperimenterModelChoiceField, \
                        ExperimenterModelMultipleChoiceField, \
                        GroupModelMultipleChoiceField, GroupModelChoiceField
from omeroweb.webclient.webclient_utils import formatPercentFraction

logger = logging.getLogger(__name__)

             
##################################################################
# Static values

# TODO: change to reverse
help_button = "%swebgateway/img/help16.png" % settings.STATIC_URL

help_wiki = '<span id="markup" data-content="Markups - <small>If you\'d like to include URL please type:<br/><b>http://www.openmicroscopy.org.uk/</b></small>"><img src="%s" /></span>' % help_button

help_wiki_c = '<span id="markup_c" data-content="Markups - <small>If you\'d like to include URL please type:<br/><b>http://www.openmicroscopy.org.uk/</b></small>"><img src="%s" /></span>' % help_button

help_enable = '<span id="enable" data-content="Enable/Disable - <small>This option allows the owner to keep the access control of the share.</small>"><img src="%s" /></span>' % help_button

help_expire = '<span id="expire" data-content="Expire date - <small>This date defines when share will stop being available. Date format: YYYY-MM-DD.</small>"><img src="%s" /></span>' % help_button

#################################################################
# Non-model Form

class GlobalSearchForm(NonASCIIForm):
    
    search_query = forms.CharField(widget=forms.TextInput(attrs={'size':25}))


class ShareForm(NonASCIIForm):
    
    def __init__(self, *args, **kwargs):
        super(ShareForm, self).__init__(*args, **kwargs)
        
        try:
            if kwargs['initial']['shareMembers']: pass
            self.fields['members'] = ExperimenterModelMultipleChoiceField(queryset=kwargs['initial']['experimenters'], initial=kwargs['initial']['shareMembers'], widget=forms.SelectMultiple(attrs={'size':5}))
        except:
            self.fields['members'] = ExperimenterModelMultipleChoiceField(queryset=kwargs['initial']['experimenters'], widget=forms.SelectMultiple(attrs={'size':5}))
        self.fields.keyOrder = ['message', 'expiration', 'enable', 'members']#, 'guests']
    
    message = forms.CharField(widget=forms.Textarea(attrs={'rows': 7, 'cols': 39}), help_text=help_wiki_c) 
    expiration = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':20}), label="Expire date", help_text=help_expire, required=False)
    enable = ssl = forms.BooleanField(required=False, help_text=help_enable)
    #guests = MultiEmailField(required=False, widget=forms.TextInput(attrs={'size':75}))

    def clean_expiration(self):
        if self.cleaned_data['expiration'] is not None and len(self.cleaned_data['expiration']) < 1:
            return None
        if self.cleaned_data['expiration'] is not None:
            d = str(self.cleaned_data['expiration']).rsplit("-")
            try:
                date = datetime.datetime.strptime(("%s-%s-%s" % (d[0],d[1],d[2])), "%Y-%m-%d")
            except:
                raise forms.ValidationError('Date is in the wrong format. YY-MM-DD')
            if time.mktime(date.timetuple()) <= time.time():
                raise forms.ValidationError('Expire date must be in the future.')
        return self.cleaned_data['expiration']
    
class BasketShareForm(ShareForm):
    
    def __init__(self, *args, **kwargs):
        super(BasketShareForm, self).__init__(*args, **kwargs)
        
        try:
            self.fields['image'] = GroupModelMultipleChoiceField(queryset=kwargs['initial']['images'], initial=kwargs['initial']['selected'], widget=forms.SelectMultiple(attrs={'size':10}))
        except:
            self.fields['image'] = GroupModelMultipleChoiceField(queryset=kwargs['initial']['images'], widget=forms.SelectMultiple(attrs={'size':10}))

class ContainerForm(NonASCIIForm):
    
    name = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':45}))
    description = forms.CharField(widget=forms.Textarea(attrs={'rows': 2, 'cols': 49}), required=False, help_text=help_wiki)

class ContainerNameForm(NonASCIIForm):
    
    name = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':45}))
    
class ContainerDescriptionForm(NonASCIIForm):
    
    description = forms.CharField(widget=forms.Textarea(attrs={'rows': 3, 'cols': 39}), required=False)


class BaseAnnotationForm(NonASCIIForm):
    """
    This is the superclass of the various forms used for annotating single or multiple objects.
    All these forms use hidden fields to specify the object(s) currently being annotated.
    """
    def __init__(self, *args, **kwargs):
        super(BaseAnnotationForm, self).__init__(*args, **kwargs)
        
        images = 'images' in kwargs['initial'] and kwargs['initial']['images'] or list()
        if len(images) > 0:
            try:
                self.fields['image'] = ObjectModelMultipleChoiceField(queryset=images, initial=kwargs['initial']['selected']['images'], widget=forms.SelectMultiple(attrs={'size':10}), required=False)
            except:
                self.fields['image'] = ObjectModelMultipleChoiceField(queryset=images, widget=forms.SelectMultiple(attrs={'size':10}), required=False)
        
        datasets = 'datasets' in kwargs['initial'] and kwargs['initial']['datasets'] or list()
        if len(datasets) > 0:
            try:
                self.fields['dataset'] = ObjectModelMultipleChoiceField(queryset=datasets, initial=kwargs['initial']['selected']['datasets'], widget=forms.SelectMultiple(attrs={'size':10}), required=False)
            except:
                self.fields['dataset'] = ObjectModelMultipleChoiceField(queryset=datasets, widget=forms.SelectMultiple(attrs={'size':10}), required=False)
        
        projects = 'projects' in kwargs['initial'] and kwargs['initial']['projects'] or list()
        if len(projects) > 0:
            try:
                self.fields['project'] = ObjectModelMultipleChoiceField(queryset=projects, initial=kwargs['initial']['selected']['projects'], widget=forms.SelectMultiple(attrs={'size':10}), required=False)
            except:
                self.fields['project'] = ObjectModelMultipleChoiceField(queryset=projects, widget=forms.SelectMultiple(attrs={'size':10}), required=False)
        
        screens = 'screens' in kwargs['initial'] and kwargs['initial']['screens'] or list()
        if len(screens) > 0:
            try:
                self.fields['screen'] = ObjectModelMultipleChoiceField(queryset=screens, initial=kwargs['initial']['selected']['screens'], widget=forms.SelectMultiple(attrs={'size':10}), required=False)
            except:
                self.fields['screen'] = ObjectModelMultipleChoiceField(queryset=screens, widget=forms.SelectMultiple(attrs={'size':10}), required=False)
        
        plates = 'plates' in kwargs['initial'] and kwargs['initial']['plates'] or list()
        if len(plates) > 0:
            try:
                self.fields['plate'] = ObjectModelMultipleChoiceField(queryset=plates, initial=kwargs['initial']['selected']['plates'], widget=forms.SelectMultiple(attrs={'size':10}), required=False)
            except:
                self.fields['plate'] = ObjectModelMultipleChoiceField(queryset=plates, widget=forms.SelectMultiple(attrs={'size':10}), required=False)
        
        acquisitions = 'acquisitions' in kwargs['initial'] and kwargs['initial']['acquisitions'] or list()
        if len(acquisitions) > 0:
            try:
                self.fields['acquisition'] = ObjectModelMultipleChoiceField(queryset=acquisitions, initial=kwargs['initial']['selected']['acquisitions'], widget=forms.SelectMultiple(attrs={'size':10}), required=False)
            except:
                self.fields['acquisition'] = ObjectModelMultipleChoiceField(queryset=acquisitions, widget=forms.SelectMultiple(attrs={'size':10}), required=False)
        
        wells = 'wells' in kwargs['initial'] and kwargs['initial']['wells'] or list()
        if len(wells) > 0:
            try:
                self.fields['well'] = ObjectModelMultipleChoiceField(queryset=wells, initial=kwargs['initial']['selected']['wells'], widget=forms.SelectMultiple(attrs={'size':10}), required=False)
            except:
                self.fields['well'] = ObjectModelMultipleChoiceField(queryset=wells, widget=forms.SelectMultiple(attrs={'size':10}), required=False)
        
        shares = 'shares' in kwargs['initial'] and kwargs['initial']['shares'] or list()
        if len(shares) > 0:
            try:
                self.fields['share'] = ObjectModelMultipleChoiceField(queryset=shares, initial=kwargs['initial']['selected']['shares'], widget=forms.SelectMultiple(attrs={'size':10}), required=False)
            except:
                self.fields['share'] = ObjectModelMultipleChoiceField(queryset=shares, widget=forms.SelectMultiple(attrs={'size':10}), required=False)
        

class TagsAnnotationForm(BaseAnnotationForm):
    """ Form for annotating one or more objects with existing Tags or New tags """

    def __init__(self, *args, **kwargs):
        super(TagsAnnotationForm, self).__init__(*args, **kwargs)

    tags = forms.CharField(required=False, widget=forms.HiddenInput)

    def clean_tags(self):
        data = self.cleaned_data['tags']
        if not data:
            return []
        try:
            data = map(int, data.split(','))
        except Exception:
            raise forms.ValidationError()
        return data


class NewTagsAnnotationForm(forms.Form):
    """ Helper form for new tags """
    tag = forms.CharField(required=True, widget=forms.HiddenInput)
    description = forms.CharField(required=False, widget=forms.HiddenInput)
    tagset = forms.IntegerField(min_value=1, required=False, widget=forms.HiddenInput)

NewTagsAnnotationFormSet = formset_factory(NewTagsAnnotationForm, extra=0)


class FilesAnnotationForm(BaseAnnotationForm):
    
    def __init__(self, *args, **kwargs):
        super(FilesAnnotationForm, self).__init__(*args, **kwargs)
        self.fields['files'] = AnnotationModelMultipleChoiceField(queryset=kwargs['initial']['files'], widget=forms.SelectMultiple(attrs={'size':8, 'class':'existing'}), required=False)
    
    annotation_file = forms.FileField(required=False)


class CommentAnnotationForm(BaseAnnotationForm):
    comment = forms.CharField(widget=forms.Textarea(attrs={'rows': 2, 'cols': 39}))

class UsersForm(forms.Form):

    def __init__(self, *args, **kwargs):
        super(UsersForm, self).__init__(*args, **kwargs)
        try:
            empty_label = kwargs['initial']['empty_label']
        except:
            empty_label='---------'
        try:
            menu = kwargs['initial']['menu']
        except:
            menu = '----------'
        try:
            user = kwargs['initial']['user']
        except:
            user = None
        users = kwargs['initial']['users']
        
        self.fields['experimenter'] = ExperimenterModelChoiceField(queryset=users, initial=user, widget=forms.Select(attrs={'onchange':'window.location.href=\''+reverse(viewname="load_template", args=[menu])+'?experimenter=\'+this.options[this.selectedIndex].value'}), required=False, empty_label=empty_label)
        
        if users is None or len(users)<2:
            self.fields['experimenter'].widget.attrs['disabled'] = True
            self.fields['experimenter'].widget.attrs['class'] = 'disabled'
        
        self.fields.keyOrder = ['experimenter']

class ActiveGroupForm(forms.Form):

    def __init__(self, *args, **kwargs):
        super(ActiveGroupForm, self).__init__(*args, **kwargs)
        try:
            self.fields['active_group'] = GroupModelChoiceField(queryset=kwargs['initial']['mygroups'], initial=kwargs['initial']['activeGroup'], empty_label=None, widget=forms.Select(attrs={'onchange':'window.location.href=\''+reverse(viewname="change_active_group")+'?url='+kwargs['initial']['url']+'&active_group=\'+this.options[this.selectedIndex].value'})) 
        except:
            self.fields['active_group'] = GroupModelChoiceField(queryset=kwargs['initial']['mygroups'], initial=kwargs['initial']['activeGroup'], empty_label=None, widget=forms.Select(attrs={'onchange':'window.location.href=\''+reverse(viewname="change_active_group")+'?active_group=\'+this.options[this.selectedIndex].value'})) 
        self.fields.keyOrder = ['active_group']


class WellIndexForm(forms.Form):
    
    def __init__(self, *args, **kwargs):
        super(WellIndexForm, self).__init__(*args, **kwargs)
        rmin, rmax = kwargs['initial']['range']
        choices = [(str(i), "Field#%i" % (i-rmin+1)) for i in range(rmin, rmax+1)]
        self.fields['index'] = forms.ChoiceField(choices=tuple(choices),  widget=forms.Select(attrs={'onchange':'changeField(this.options[this.selectedIndex].value);'}))
        self.fields.keyOrder = ['index']

###############################
# METADATA FORMS
class MetadataChannelForm(forms.Form):
    
    def __init__(self, *args, **kwargs):
        super(MetadataChannelForm, self).__init__(*args, **kwargs)
        
        # Logical channel
        
        # Name
        logicalCh = kwargs['initial']['logicalChannel']
        try:
            if logicalCh is not None:
                self.fields['name'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(logicalCh.id)+', \'name\', this.value);'}),
                    initial=logicalCh.name,
                    required=False)
            else:
                self.fields['name'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(logicalCh.id)+', \'name\', this.value);'}),
                    required=False)
            self.fields['name'].widget.attrs['disabled'] = True 
            self.fields['name'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['name'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['name'].widget.attrs['disabled'] = True 
            self.fields['name'].widget.attrs['class'] = 'disabled-metadata'
        
        # excitationWave
        try:
            if logicalCh is not None:
                self.fields['excitationWave'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(logicalCh.id)+', \'name\', this.value);'}),
                    initial=kwargs['initial']['exWave'].getValue(),
                    label="Excitation (%s)" % kwargs['initial']['exWave'].getSymbol(),
                    required=False)
            else:
                self.fields['excitationWave'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(logicalCh.id)+', \'name\', this.value);'}),
                    label="Excitation",
                    required=False)
            self.fields['excitationWave'].widget.attrs['disabled'] = True 
            self.fields['excitationWave'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['excitationWave'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Excitation",
                required=False)
            self.fields['excitationWave'].widget.attrs['disabled'] = True 
            self.fields['excitationWave'].widget.attrs['class'] = 'disabled-metadata'
        
        # emissionWave
        try:
            if logicalCh is not None:
                self.fields['emissionWave'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(logicalCh.id)+', \'name\', this.value);'}),
                    initial=kwargs['initial']['emWave'].getValue(),
                    label="Emission (%s)" % kwargs['initial']['emWave'].getSymbol(),
                    required=False)
            else:
                self.fields['emissionWave'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(logicalCh.id)+', \'name\', this.value);'}),
                    label="Emission",
                    required=False)
            self.fields['emissionWave'].widget.attrs['disabled'] = True 
            self.fields['emissionWave'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['emissionWave'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Emission",
                required=False)
            self.fields['emissionWave'].widget.attrs['disabled'] = True 
            self.fields['emissionWave'].widget.attrs['class'] = 'disabled-metadata'
        
        # ndFilter
        try:
            if logicalCh is not None and logicalCh.ndFilter is not None:
                self.fields['ndFilter'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(logicalCh.id)+', \'name\', this.value);'}),
                    initial=formatPercentFraction(logicalCh.ndFilter),
                    label="ND filter (%)",
                    required=False)
            else:
                self.fields['ndFilter'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(logicalCh.id)+', \'name\', this.value);'}),
                    label="ND filter (%)",
                    required=False)
            self.fields['ndFilter'].widget.attrs['disabled'] = True 
        except:
            self.fields['ndFilter'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="ND filter (%)",
                required=False)
            self.fields['ndFilter'].widget.attrs['disabled'] = True
        
        # pinHoleSize
        try:
            if logicalCh is not None and logicalCh.pinHoleSize is not None:
                self.fields['pinHoleSize'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(logicalCh.id)+', \'name\', this.value);'}),
                    initial=logicalCh.pinHoleSize.getValue(),
                    label="Pin hole size (%s)" % logicalCh.pinHoleSize.getSymbol(),
                    required=False)
            else:
                self.fields['pinHoleSize'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(logicalCh.id)+', \'name\', this.value);'}),
                    label="Pin hole size",
                    required=False)
            self.fields['pinHoleSize'].widget.attrs['disabled'] = True 
        except:
            self.fields['pinHoleSize'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Pin hole size",
                required=False)
            self.fields['pinHoleSize'].widget.attrs['disabled'] = True
        
        # fluor
        try:
            if logicalCh is not None:
                self.fields['fluor'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(logicalCh.id)+', \'name\', this.value);'}),
                    initial=logicalCh.fluor,
                    required=False)
            else:
                self.fields['fluor'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(logicalCh.id)+', \'name\', this.value);'}),
                    required=False)
            self.fields['fluor'].widget.attrs['disabled'] = True
        except:
            self.fields['fluor'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['fluor'].widget.attrs['disabled'] = True
        
        # Illumination
        try:
            if logicalCh.getIllumination() is not None:
                self.fields['illumination'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['illuminations'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={'onchange':'saveMetadata('+str(logicalCh.id)+', \'illumination\', this.options[this.selectedIndex].value);'}),
                    initial=logicalCh.getIllumination(),
                    required=False) 
            else:
                self.fields['illumination'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['illuminations'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={'onchange':'saveMetadata('+str(logicalCh.id)+', \'illumination\', this.options[this.selectedIndex].value);'}),
                    required=False) 
            self.fields['illumination'].widget.attrs['disabled'] = True
        except:
            self.fields['illumination'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['illumination'].widget.attrs['disabled'] = True
        
        # contrastMethods
        try:
            if logicalCh.contrastMethod is not None:
                self.fields['contrastMethod'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['contrastMethods'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={'onchange':'saveMetadata('+str(logicalCh.id)+', \'contrastMethod\', this.options[this.selectedIndex].value);'}),
                    initial=logicalCh.getContrastMethod(),
                    label="Contrast method",
                    required=False) 
            else:
                self.fields['contrastMethod'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['contrastMethods'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={'onchange':'saveMetadata('+str(logicalCh.id)+', \'contrastMethod\', this.options[this.selectedIndex].value);'}),
                    label="Contrast method",
                    required=False) 
            self.fields['contrastMethod'].widget.attrs['disabled'] = True 
            self.fields['contrastMethod'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['contrastMethod'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Contrast method",
                required=False)
            self.fields['contrastMethod'].widget.attrs['disabled'] = True 
            self.fields['contrastMethod'].widget.attrs['class'] = 'disabled-metadata'
        
        # Mode
        try:
            if logicalCh.getMode() is not None:
                self.fields['mode'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['modes'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={'onchange':'saveMetadata('+str(logicalCh.id)+', \'mode\', this.options[this.selectedIndex].value);'}),
                    initial=logicalCh.getMode().value,
                    required=False) 
            else:
                self.fields['mode'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['modes'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={'onchange':'saveMetadata('+str(logicalCh.id)+', \'mode\', this.options[this.selectedIndex].value);'}),
                    required=False) 
            self.fields['mode'].widget.attrs['disabled'] = True 
            self.fields['mode'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['mode'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['mode'].widget.attrs['disabled'] = True 
            self.fields['mode'].widget.attrs['class'] = 'disabled-metadata'
        
        # pockelCellSetting
        try:
            if logicalCh.pockelCellSetting is not None:
                self.fields['pockelCellSetting'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(logicalCh.id)+', \'name\', this.value);'}),
                    initial=logicalCh.pockelCellSetting,
                    label="Pockel cell",
                    required=False)
            else:
                self.fields['pockelCellSetting'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(logicalCh.id)+', \'name\', this.value);'}),
                    label="Pockel cell",
                    required=False)
            self.fields['pockelCellSetting'].widget.attrs['disabled'] = True 
            self.fields['pockelCellSetting'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['pockelCellSetting'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Pockel cell",
                required=False)
            self.fields['pockelCellSetting'].widget.attrs['disabled'] = True 
            self.fields['pockelCellSetting'].widget.attrs['class'] = 'disabled-metadata'
        
        self.fields.keyOrder = ['name', 'excitationWave', 'emissionWave', 'ndFilter', 'pinHoleSize', 'fluor', 'illumination', 'contrastMethod', 'mode', 'pockelCellSetting'] 


class MetadataDichroicForm(forms.Form):
    
    def __init__(self, *args, **kwargs):
        super(MetadataDichroicForm, self).__init__(*args, **kwargs)
    
        # Manufacturer
        try:
            if kwargs['initial']['dichroic'].manufacturer is not None:
                self.fields['manufacturer'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['dichroic'].id)+', \'manufacturer\', this.value);'}), initial=kwargs['initial']['dichroic'].manufacturer, required=False)
            else:
                self.fields['manufacturer'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['dichroic'].id)+', \'manufacturer\', this.value);'}), required=False)
            self.fields['manufacturer'].widget.attrs['disabled'] = True 
            self.fields['manufacturer'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['manufacturer'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':25}), initial="N/A", required=False)
            self.fields['manufacturer'].widget.attrs['disabled'] = True 
            self.fields['manufacturer'].widget.attrs['class'] = 'disabled-metadata'

        # Model
        try:
            if kwargs['initial']['dichroic'].model is not None:
                self.fields['model'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['dichroic'].id)+', \'model\', this.value);'}), initial=kwargs['initial']['dichroic'].model, required=False)
            else:
                self.fields['model'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['dichroic'].id)+', \'model\', this.value);'}), required=False)
            self.fields['model'].widget.attrs['disabled'] = True 
            self.fields['model'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['model'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':25}), initial="N/A", required=False)
            self.fields['model'].widget.attrs['disabled'] = True 
            self.fields['model'].widget.attrs['class'] = 'disabled-metadata'
        
        # Serial number
        try:
            if kwargs['initial']['dichroic'].serialNumber is not None:
                self.fields['serialNumber'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['dichroic'].serialNumber)+', \'serialNumber\', this.value);'}), initial=kwargs['initial']['dichroic'].serialNumber, label="Serial number", required=False)
            else:
                self.fields['serialNumber'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['dichroic'].serialNumber)+', \'serialNumber\', this.value);'}), label="Serial number", required=False)
            self.fields['serialNumber'].widget.attrs['disabled'] = True 
            self.fields['serialNumber'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['serialNumber'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':25}), initial="N/A", label="Serial number", required=False)
            self.fields['serialNumber'].widget.attrs['disabled'] = True 
            self.fields['serialNumber'].widget.attrs['class'] = 'disabled-metadata'
            
        # Lot number
        try:
            if kwargs['initial']['dichroic'].lotNumber is not None:
                self.fields['lotNumber'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['dichroic'].lotNumber)+', \'lotNumber\', this.value);'}), initial=kwargs['initial']['dichroic'].lotNumber, label="Lot number", required=False)
            else:
                self.fields['lotNumber'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['dichroic'].lotNumber)+', \'lotNumber\', this.value);'}), label="Lot number", required=False)
            self.fields['lotNumber'].widget.attrs['disabled'] = True 
            self.fields['lotNumber'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['lotNumber'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':25}), initial="N/A", label="Lot number", required=False)
            self.fields['lotNumber'].widget.attrs['disabled'] = True 
            self.fields['lotNumber'].widget.attrs['class'] = 'disabled-metadata'
        
        self.fields.keyOrder = ['manufacturer', 'model', 'serialNumber', 'lotNumber'] 


class MetadataMicroscopeForm(forms.Form):
    
    def __init__(self, *args, **kwargs):
        super(MetadataMicroscopeForm, self).__init__(*args, **kwargs)
    
        # Model
        try:
            if kwargs['initial']['microscope'].model is not None:
                self.fields['model'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['microscope'].id)+', \'model\', this.value);'}), initial=kwargs['initial']['microscope'].model, required=False)
            else:
                self.fields['model'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['microscope'].id)+', \'model\', this.value);'}), required=False)
            self.fields['model'].widget.attrs['disabled'] = True 
            self.fields['model'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['model'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':25}), initial="N/A", required=False)
            self.fields['model'].widget.attrs['disabled'] = True 
            self.fields['model'].widget.attrs['class'] = 'disabled-metadata'
        
        # Manufacturer
        try:
            if kwargs['initial']['microscope'].manufacturer is not None:
                self.fields['manufacturer'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['microscope'].id)+', \'manufacturer\', this.value);'}), initial=kwargs['initial']['microscope'].manufacturer, required=False)
            else:
                self.fields['manufacturer'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['microscope'].id)+', \'manufacturer\', this.value);'}), required=False)
            self.fields['manufacturer'].widget.attrs['disabled'] = True 
            self.fields['manufacturer'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['manufacturer'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':25}), initial="N/A", required=False)
            self.fields['manufacturer'].widget.attrs['disabled'] = True 
            self.fields['manufacturer'].widget.attrs['class'] = 'disabled-metadata'
        
        # Serial number
        try:
            if kwargs['initial']['microscope'].serialNumber is not None:
                self.fields['serialNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['microscope'].id)+', \'lotNumber\', this.value);'}),
                    initial=kwargs['initial']['microscope'].serialNumber,
                    label="Serial number",
                    required=False)
            else:
                self.fields['serialNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['microscope'].id)+', \'lotNumber\', this.value);'}),
                    label="Serial number",
                    required=False)
            self.fields['serialNumber'].widget.attrs['disabled'] = True 
            self.fields['serialNumber'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['serialNumber'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Serial number",
                required=False)
            self.fields['serialNumber'].widget.attrs['disabled'] = True 
            self.fields['serialNumber'].widget.attrs['class'] = 'disabled-metadata'
            
        # Lot number
        try:
            if kwargs['initial']['microscope'].lotNumber is not None:
                self.fields['lotNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['microscope'].id)+', \'lotNumber\', this.value);'}),
                    initial=kwargs['initial']['microscope'].lotNumber,
                    label="Lot number",
                    required=False)
            else:
                self.fields['lotNumber'] = forms.CharField(
                    max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['microscope'].id)+', \'lotNumber\', this.value);'}),
                    label="Serial number",
                    required=False)
            self.fields['lotNumber'].widget.attrs['disabled'] = True 
            self.fields['lotNumber'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['lotNumber'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Serial number",
                required=False)
            self.fields['lotNumber'].widget.attrs['disabled'] = True 
            self.fields['lotNumber'].widget.attrs['class'] = 'disabled-metadata'
        
        # Type
        try:
            if kwargs['initial']['microscope'].getMicroscopeType() is not None:
                self.fields['type'] = MetadataModelChoiceField(queryset=kwargs['initial']['microscopeTypes'], empty_label=u"Not set", widget=forms.Select(attrs={'onchange':'saveMetadata('+str(kwargs['initial']['microscope'].id)+', \'type\', this.options[this.selectedIndex].value);'}), initial=kwargs['initial']['microscope'].getMicroscopeType().value, required=False) 
            else:
                self.fields['type'] = MetadataModelChoiceField(queryset=kwargs['initial']['microscopeTypes'], empty_label=u"Not set", widget=forms.Select(attrs={'onchange':'saveMetadata('+str(kwargs['initial']['microscope'].id)+', \'type\', this.options[this.selectedIndex].value);'}), required=False) 
            self.fields['type'].widget.attrs['disabled'] = True 
            self.fields['type'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['type'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':25}), initial="N/A", required=False)
            self.fields['type'].widget.attrs['disabled'] = True 
            self.fields['type'].widget.attrs['class'] = 'disabled-metadata'
        
        self.fields.keyOrder = ['model', 'manufacturer', 'serialNumber', 'lotNumber', 'type']


class MetadataObjectiveForm(forms.Form):
    
    def __init__(self, *args, **kwargs):
        super(MetadataObjectiveForm, self).__init__(*args, **kwargs)

        # Model
        try:
            if kwargs['initial']['objective'].model is not None:
                self.fields['model'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['objective'].id)+', \'model\', this.value);'}), initial=kwargs['initial']['objective'].model, required=False)
            else:
                self.fields['model'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['objective'].id)+', \'model\', this.value);'}),
                    required=False)
            self.fields['model'].widget.attrs['disabled'] = True 
            self.fields['model'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['model'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['model'].widget.attrs['disabled'] = True 
            self.fields['model'].widget.attrs['class'] = 'disabled-metadata'

        # Manufacturer
        try:
            if kwargs['initial']['objective'].manufacturer is not None:
                self.fields['manufacturer'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['objective'].id)+', \'manufacturer\', this.value);'}), initial=kwargs['initial']['objective'].manufacturer, required=False)
            else:
                self.fields['manufacturer'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['objective'].id)+', \'manufacturer\', this.value);'}), required=False)
            self.fields['manufacturer'].widget.attrs['disabled'] = True 
            self.fields['manufacturer'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['manufacturer'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':25}), initial="N/A", required=False)
            self.fields['manufacturer'].widget.attrs['disabled'] = True 
            self.fields['manufacturer'].widget.attrs['class'] = 'disabled-metadata'
        
        # Serial Number
        try:
            if kwargs['initial']['objective'].serialNumber is not None:
                self.fields['serialNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['objective'].id)+', \'serialNumber\', this.value);'}),
                    initial=kwargs['initial']['objective'].serialNumber,
                    label="Serial number",
                    required=False)
            else:
                self.fields['serialNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['objective'].id)+', \'serialNumber\', this.value);'}),
                    label="Serial number",
                    required=False)
            self.fields['serialNumber'].widget.attrs['disabled'] = True 
            self.fields['serialNumber'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['serialNumber'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Serial number",
                required=False)
            self.fields['serialNumber'].widget.attrs['disabled'] = True 
            self.fields['serialNumber'].widget.attrs['class'] = 'disabled-metadata'
        
        # Lot number
        try:
            if kwargs['initial']['objective'].lotNumber is not None:
                self.fields['lotNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['objective'].lotNumber)+', \'lotNumber\', this.value);'}),
                    initial=kwargs['initial']['objective'].lotNumber,
                    label="Lot number",
                    required=False)
            else:
                self.fields['lotNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['logicalchannel'].getObjective().lotNumber)+', \'lotNumber\', this.value);'}),
                    label="Lot number",
                    required=False)
            self.fields['lotNumber'].widget.attrs['disabled'] = True 
            self.fields['lotNumber'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['lotNumber'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Lot number",
                required=False)
            self.fields['lotNumber'].widget.attrs['disabled'] = True 
            self.fields['lotNumber'].widget.attrs['class'] = 'disabled-metadata'
            
        # Nominal Magnification
        try:
            if kwargs['initial']['objective'].nominalMagnification is not None:
                self.fields['nominalMagnification'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['objective'].id)+', \'nominalMagnification\', this.value);'}), initial=kwargs['initial']['objective'].nominalMagnification, label="Nominal magnification", required=False)
            else:
                self.fields['nominalMagnification'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['objective'].id)+', \'nominalMagnification\', this.value);'}), label="Nominal magnification", required=False)
            self.fields['nominalMagnification'].widget.attrs['disabled'] = True 
            self.fields['nominalMagnification'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['nominalMagnification'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':25}), initial="N/A", label="Nominal magnification", required=False)
            self.fields['nominalMagnification'].widget.attrs['disabled'] = True 
            self.fields['nominalMagnification'].widget.attrs['class'] = 'disabled-metadata'
        
        # Calibrated Magnification
        try:
            if kwargs['initial']['objective'].calibratedMagnification is not None:
                self.fields['calibratedMagnification'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['objective'].id)+', \'calibratedMagnification\', this.value);'}), initial=kwargs['initial']['objective'].calibratedMagnification, label="Calibrated magnification", required=False)
            else:
                self.fields['calibratedMagnification'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['objective'].id)+', \'calibratedMagnification\', this.value);'}), label="Calibrated magnification", required=False)
            self.fields['calibratedMagnification'].widget.attrs['disabled'] = True 
            self.fields['calibratedMagnification'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['calibratedMagnification'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':25}), initial="N/A", label="Calibrated magnification", required=False)
            self.fields['calibratedMagnification'].widget.attrs['disabled'] = True 
            self.fields['calibratedMagnification'].widget.attrs['class'] = 'disabled-metadata'
        
        # Lens NA
        try:
            if kwargs['initial']['objective'].lensNA is not None:
                self.fields['lensNA'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['objective'].id)+', \'lensNA\', this.value);'}), initial=kwargs['initial']['objective'].lensNA, label="Lens NA", required=False)
            else:
                self.fields['lensNA'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['objective'].id)+', \'lensNA\', this.value);'}), required=False)
            self.fields['lensNA'].widget.attrs['disabled'] = True 
            self.fields['lensNA'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['lensNA'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':25}), initial="N/A", label="Lens NA", required=False)
            self.fields['lensNA'].widget.attrs['disabled'] = True 
            self.fields['lensNA'].widget.attrs['class'] = 'disabled-metadata'
        
        # Immersion
        try:
            if kwargs['initial']['objective'].getImmersion() is not None:
                self.fields['immersion'] = MetadataModelChoiceField(queryset=kwargs['initial']['immersions'], empty_label=u"Not set", widget=forms.Select(attrs={'onchange':'saveMetadata('+str(kwargs['initial']['objective'].id)+', \'immersion\', this.options[this.selectedIndex].value);'}), initial=kwargs['initial']['objective'].getImmersion().value, required=False)
            else:
                self.fields['immersion'] = MetadataModelChoiceField(queryset=kwargs['initial']['immersions'], empty_label=u"Not set", widget=forms.Select(attrs={'onchange':'saveMetadata('+str(kwargs['initial']['objective'].id)+', \'immersion\', this.options[this.selectedIndex].value);'}), required=False)
            self.fields['immersion'].widget.attrs['disabled'] = True 
            self.fields['immersion'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['immersion'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':25}), initial="N/A", required=False)
            self.fields['immersion'].widget.attrs['disabled'] = True 
            self.fields['immersion'].widget.attrs['class'] = 'disabled-metadata'
        
        # Correction
        try:
            if kwargs['initial']['objective'].getCorrection() is not None:
                self.fields['correction'] = MetadataModelChoiceField(queryset=kwargs['initial']['corrections'], empty_label=u"Not set", widget=forms.Select(attrs={'onchange':'saveMetadata('+str(kwargs['initial']['objective'].id)+', \'correction\', this.options[this.selectedIndex].value);'}), initial=kwargs['initial']['objective'].getCorrection().value, required=False)
            else:
                self.fields['correction'] = MetadataModelChoiceField(queryset=kwargs['initial']['corrections'], empty_label=u"Not set", widget=forms.Select(attrs={'onchange':'saveMetadata('+str(kwargs['initial']['objective'].id)+', \'correction\', this.options[this.selectedIndex].value);'}), required=False)
            self.fields['correction'].widget.attrs['disabled'] = True 
            self.fields['correction'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['correction'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':25}), initial="N/A", required=False)
            self.fields['correction'].widget.attrs['disabled'] = True 
            self.fields['correction'].widget.attrs['class'] = 'disabled-metadata'
        
        # Working Distance
        try:
            if kwargs['initial']['objective'].workingDistance is not None:
                self.fields['workingDistance'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['objective'].id)+', \'workingDistance\', this.value);'}),
                    initial=kwargs['initial']['objective'].workingDistance.getValue(),
                    label="Working distance (%s)" % kwargs['initial']['objective'].workingDistance.getSymbol(),
                    required=False)
            else:
                self.fields['workingDistance'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['objective'].id)+', \'workingDistance\', this.value);'}), label="Working distance", required=False)
            self.fields['workingDistance'].widget.attrs['disabled'] = True 
            self.fields['workingDistance'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['workingDistance'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':25}), initial="N/A", label="Working distance", required=False)
            self.fields['workingDistance'].widget.attrs['disabled'] = True 
            self.fields['workingDistance'].widget.attrs['class'] = 'disabled-metadata'
        
        # Iris
        try:
            if kwargs['initial']['objective'].getIris() is not None:
                self.fields['iris'] = forms.ChoiceField(choices=self.BOOLEAN_CHOICES,  widget=forms.Select(attrs={'onchange':'javascript:saveMetadata('+str(kwargs['initial']['objective'].id)+', \'iris\', this.options[this.selectedIndex].value);'}), initial=kwargs['initial']['objective'].getIris().value, required=False)
            else:
                self.fields['iris'] = forms.ChoiceField(choices=self.BOOLEAN_CHOICES,  widget=forms.Select(attrs={'onchange':'javascript:saveMetadata('+str(kwargs['initial']['objective'].id)+', \'iris\', this.options[this.selectedIndex].value);'}), required=False)
            self.fields['iris'].widget.attrs['disabled'] = True 
            self.fields['iris'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['iris'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':25}), initial="N/A", required=False)
            self.fields['iris'].widget.attrs['disabled'] = True 
            self.fields['iris'].widget.attrs['class'] = 'disabled-metadata'

        self.fields.keyOrder = ['model', 'manufacturer', 'serialNumber', 'lotNumber', 'nominalMagnification', 'calibratedMagnification', 'lensNA', 'immersion', 'correction', 'workingDistance', 'iris']


class MetadataObjectiveSettingsForm(MetadataObjectiveForm):

    BOOLEAN_CHOICES = (
        ('', '---------'),
        ('True', 'True'),
        ('False', 'False'),
    )

    def __init__(self, *args, **kwargs):
        super(MetadataObjectiveSettingsForm, self).__init__(*args, **kwargs)

        # Objective Settings

        # Correction Collar
        try:
            if kwargs['initial']['objectiveSettings'].correctionCollar is not None:
                self.fields['correctionCollar'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['objectiveSettings'].id)+', \'correctionCollar\', this.value);'}), initial=kwargs['initial']['objectiveSettings'].correctionCollar, label="Correction collar", required=False)
            else:
                self.fields['correctionCollar'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['objectiveSettings'].id)+', \'correctionCollar\', this.value);'}), label="Correction collar", required=False)
            self.fields['correctionCollar'].widget.attrs['disabled'] = True
            self.fields['correctionCollar'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['correctionCollar'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':25}), initial="N/A", label="Correction collar", required=False)
            self.fields['correctionCollar'].widget.attrs['disabled'] = True
            self.fields['correctionCollar'].widget.attrs['class'] = 'disabled-metadata'

        # Medium
        try:
            if kwargs['initial']['objectiveSettings'].getMedium() is not None:
                self.fields['medium'] = MetadataModelChoiceField(queryset=kwargs['initial']['mediums'], empty_label=u"Not set", widget=forms.Select(attrs={'onchange':'saveMetadata('+str(kwargs['initial']['objectiveSettings'].id)+', \'medium\', this.options[this.selectedIndex].value);'}), initial=kwargs['initial']['objectiveSettings'].getMedium().value, required=False)
            else:
                self.fields['medium'] = MetadataModelChoiceField(queryset=kwargs['initial']['mediums'], empty_label=u"Not set", widget=forms.Select(attrs={'onchange':'saveMetadata('+str(kwargs['initial']['objectiveSettings'].id)+', \'medium\', this.options[this.selectedIndex].value);'}), required=False)
            self.fields['medium'].widget.attrs['disabled'] = True
            self.fields['medium'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['medium'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':25}), initial="N/A", required=False)
            self.fields['medium'].widget.attrs['disabled'] = True
            self.fields['medium'].widget.attrs['class'] = 'disabled-metadata'

        # Refractive Index
        try:
            if kwargs['initial']['objectiveSettings'].refractiveIndex is not None:
                self.fields['refractiveIndex'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['objectiveSettings'].id)+', \'refractiveIndex\', this.value);'}), initial=kwargs['initial']['objectiveSettings'].refractiveIndex, label="Refractive index", required=False)
            else:
                self.fields['refractiveIndex'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['objectiveSettings'].id)+', \'refractiveIndex\', this.value);'}), label="Refractive index", required=False)
            self.fields['refractiveIndex'].widget.attrs['disabled'] = True
            self.fields['refractiveIndex'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['refractiveIndex'] = forms.CharField(max_length=10, widget=forms.TextInput(attrs={'size':25}), initial="N/A", label="Refractive index", required=False)
            self.fields['refractiveIndex'].widget.attrs['disabled'] = True
            self.fields['refractiveIndex'].widget.attrs['class'] = 'disabled-metadata'

        
        self.fields.keyOrder = ['model', 'manufacturer', 'serialNumber', 'lotNumber', 'nominalMagnification', 'calibratedMagnification', 'lensNA', 'immersion', 'correction', 'workingDistance', 'iris', 'correctionCollar',  'medium', 'refractiveIndex'] 


class MetadataFilterForm(forms.Form):
    
    def __init__(self, *args, **kwargs):
        super(MetadataFilterForm, self).__init__(*args, **kwargs)
        
        # Filter 
        
        # Manufacturer
        try:
            if kwargs['initial']['filter'].manufacturer is not None:
                self.fields['manufacturer'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['filter'].id)+', \'manufacturer\', this.value);'}),
                    initial=kwargs['initial']['filter'].manufacturer,
                    required=False)
            else:
                self.fields['manufacturer'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['filter'].id)+', \'manufacturer\', this.value);'}),
                    required=False)
            self.fields['manufacturer'].widget.attrs['disabled'] = True 
            self.fields['manufacturer'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['manufacturer'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['manufacturer'].widget.attrs['disabled'] = True 
            self.fields['manufacturer'].widget.attrs['class'] = 'disabled-metadata'
        
        # Model
        try:
            if kwargs['initial']['filter'].model is not None:
                self.fields['model'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['filter'].id)+', \'model\', this.value);'}),
                    initial=kwargs['initial']['filter'].model,
                    required=False)
            else:
                self.fields['model'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['filter'].id)+', \'model\', this.value);'}),
                    required=False)
            self.fields['model'].widget.attrs['disabled'] = True 
            self.fields['model'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['model'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['model'].widget.attrs['disabled'] = True 
            self.fields['model'].widget.attrs['class'] = 'disabled-metadata'

        # Serial Number
        try:
            if kwargs['initial']['filter'].serialNumber is not None:
                self.fields['serialNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['filter'].id)+', \'serialNumber\', this.value);'}),
                    initial=kwargs['initial']['filter'].serialNumber, label="Serial number", required=False)
            else:
                self.fields['serialNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['filter'].id)+', \'serialNumber\', this.value);'}),
                    label="Serial number", required=False)
            self.fields['serialNumber'].widget.attrs['disabled'] = True 
            self.fields['serialNumber'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['serialNumber'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A", label="Serial number",
                required=False)
            self.fields['serialNumber'].widget.attrs['disabled'] = True 
            self.fields['serialNumber'].widget.attrs['class'] = 'disabled-metadata'
            
        # Lot number
        try:
            if kwargs['initial']['filter'].lotNumber is not None:
                self.fields['lotNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['filter'].id)+', \'lotNumber\', this.value);'}),
                    initial=kwargs['initial']['filter'].lotNumber,
                    label="Lot number",
                    required=False)
            else:
                self.fields['lotNumber'] = forms.CharField(
                    max_length=100, 
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['filter'].id)+', \'lotNumber\', this.value);'}),
                    label="Lot number",
                    required=False)
            self.fields['lotNumber'].widget.attrs['disabled'] = True 
            self.fields['lotNumber'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['lotNumber'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Lot number",
                required=False)
            self.fields['lotNumber'].widget.attrs['disabled'] = True 
            self.fields['lotNumber'].widget.attrs['class'] = 'disabled-metadata'
        
        # Filter wheel
        try:
            if kwargs['initial']['filter'].filterWheel is not None:
                self.fields['filterWheel'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['filter'].id)+', \'filterWheel\', this.value);'}),
                    initial=kwargs['initial']['filter'].filterWheel,
                    label="Filter wheel", 
                    required=False)
            else:
                self.fields['filterWheel'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['filter'].id)+', \'filterWheel\', this.value);'}),
                    label="Filter wheel",
                    required=False)
            self.fields['filterWheel'].widget.attrs['disabled'] = True 
            self.fields['filterWheel'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['filterWheel'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Filter wheel",
                required=False)
            self.fields['filterWheel'].widget.attrs['disabled'] = True 
            self.fields['filterWheel'].widget.attrs['class'] = 'disabled-metadata'
        
        # Type
        try:
            if kwargs['initial']['filter'].getFilterType() is not None:
                self.fields['type'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['types'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={'onchange':'saveMetadata('+str(kwargs['initial']['filter'].id)+', \'type\', this.options[this.selectedIndex].value);'}),
                    initial=kwargs['initial']['filter'].getFilterType().value,
                    required=False) 
            else:
                self.fields['type'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['types'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={'onchange':'saveMetadata('+str(kwargs['initial']['filter'].id)+', \'type\', this.options[this.selectedIndex].value);'}),
                    required=False) 
            self.fields['type'].widget.attrs['disabled'] = True 
            self.fields['type'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['type'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['type'].widget.attrs['disabled'] = True 
            self.fields['type'].widget.attrs['class'] = 'disabled-metadata'
        
        # Cut in
        tr = kwargs['initial']['filter'].getTransmittanceRange()
        try:
            if tr is not None and tr.cutIn is not None:
                self.fields['cutIn'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['filter'].id)+', \'cutIn\', this.value);'}),
                    initial=kwargs['initial']['filter'].getTransmittanceRange().cutIn.getValue(),
                    label="Cut in (%s)" % tr.cutIn.getSymbol(),
                    required=False)
            else:
                self.fields['cutIn'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['filter'].id)+', \'cutIn\', this.value);'}),
                    label="Cut in",
                    required=False)
            self.fields['cutIn'].widget.attrs['disabled'] = True 
            self.fields['cutIn'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['cutIn'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Cut in",
                required=False)
            self.fields['cutIn'].widget.attrs['disabled'] = True 
            self.fields['cutIn'].widget.attrs['class'] = 'disabled-metadata'
        
        # Cut out
        try:
            if tr is not None and tr.cutOut is not None:
                self.fields['cutOut'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['filter'].id)+', \'cutOut\', this.value);'}),
                    initial=tr.cutOut.getValue(),
                    label="Cut out (%s)" % tr.cutOut.getSymbol(),
                    required=False)
            else:
                self.fields['cutOut'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['filter'].id)+', \'cutOut\', this.value);'}),
                    label="Cut out",
                    required=False)
            self.fields['cutOut'].widget.attrs['disabled'] = True 
            self.fields['cutOut'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['cutOut'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Cut out",
                required=False)
            self.fields['cutOut'].widget.attrs['disabled'] = True 
            self.fields['cutOut'].widget.attrs['class'] = 'disabled-metadata'
        
        # Cut in tolerance
        try:
            if tr is not None and tr.cutInTolerance is not None:
                self.fields['cutInTolerance'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['filter'].id)+', \'cutInTolerance\', this.value);'}),
                    initial=tr.cutInTolerance.getValue(),
                    label="Cut in tolerance (%s)" % tr.cutInTolerance.getSymbol(),
                    required=False)
            else:
                self.fields['cutInTolerance'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['filter'].id)+', \'cutInTolerance\', this.value);'}),
                    label="Cut in tolerance",
                    required=False)
            self.fields['cutInTolerance'].widget.attrs['disabled'] = True 
            self.fields['cutInTolerance'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['cutInTolerance'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Cut in tolerance",
                required=False)
            self.fields['cutInTolerance'].widget.attrs['disabled'] = True 
            self.fields['cutInTolerance'].widget.attrs['class'] = 'disabled-metadata'
        
        # Cut on tolerance
        try:
            if tr is not None and tr.cutOutTolerance is not None:
                self.fields['cutOutTolerance'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['filter'].id)+', \'cutOut\', this.value);'}),
                    initial=tr.cutOutTolerance.getValue(),
                    label="Cut out tolerance (%s)" % tr.cutOutTolerance.getSymbol(),
                    required=False)
            else:
                self.fields['cutOutTolerance'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['filter'].id)+', \'cutOut\', this.value);'}),
                    label="Cut out tolerance",
                    required=False)
            self.fields['cutOutTolerance'].widget.attrs['disabled'] = True 
            self.fields['cutOutTolerance'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['cutOutTolerance'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Cut out tolerance",
                required=False)
            self.fields['cutOutTolerance'].widget.attrs['disabled'] = True 
            self.fields['cutOutTolerance'].widget.attrs['class'] = 'disabled-metadata'
        
        # Transmittance
        try:
            if kwargs['initial']['filter'].transmittanceRange is not None:
                self.fields['transmittance'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['filter'].id)+', \'transmittance\', this.value);'}),
                    initial=formatPercentFraction(kwargs['initial']['filter'].getTransmittanceRange().transmittance),
                    label="Transmittance (%)",
                    required=False)
            else:
                self.fields['transmittance'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['filter'].id)+', \'transmittance\', this.value);'}),
                    required=False)
            self.fields['transmittance'].widget.attrs['disabled'] = True 
            self.fields['transmittance'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['transmittance'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['transmittance'].widget.attrs['disabled'] = True 
            self.fields['transmittance'].widget.attrs['class'] = 'disabled-metadata'
        
        self.fields.keyOrder = ['manufacturer', 'model', 'serialNumber', 'lotNumber', 'type', 'filterWheel', 'cutIn', 'cutOut', 'cutInTolerance', 'cutOutTolerance', 'transmittance']


class MetadataDetectorForm(forms.Form):
    
    def __init__(self, *args, **kwargs):
        super(MetadataDetectorForm, self).__init__(*args, **kwargs)
        
        detSet = kwargs['initial']['detectorSettings']
        detector = kwargs['initial']['detector']
        
        # Manufacturer
        try:
            if detector is not None:
                self.fields['manufacturer'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(detector.id)+', \'manufacturer\', this.value);'}),
                    initial=detector.manufacturer,
                    required=False)
            else:
                self.fields['manufacturer'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(detector.id)+', \'manufacturer\', this.value);'}),
                    required=False)
            self.fields['manufacturer'].widget.attrs['disabled'] = True 
            self.fields['manufacturer'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['manufacturer'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['manufacturer'].widget.attrs['disabled'] = True 
            self.fields['manufacturer'].widget.attrs['class'] = 'disabled-metadata'
        
        # Model
        try:
            if detector is not None:
                self.fields['model'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(detector.id)+', \'model\', this.value);'}),
                    initial=detector.model,
                    required=False)
            else:
                self.fields['model'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(detector.id)+', \'model\', this.value);'}),
                    required=False)
            self.fields['model'].widget.attrs['disabled'] = True 
            self.fields['model'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['model'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['model'].widget.attrs['disabled'] = True 
            self.fields['model'].widget.attrs['class'] = 'disabled-metadata'
        
        # SN
        try:
            if detector is not None:
                self.fields['serialNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(detector.id)+', \'serialNumber\', this.value);'}),
                    initial=detector.serialNumber,
                    required=False)
            else:
                self.fields['serialNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(detector.id)+', \'serialNumber\', this.value);'}),
                    required=False)
            self.fields['serialNumber'].widget.attrs['disabled'] = True 
            self.fields['serialNumber'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['serialNumber'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['serialNumber'].widget.attrs['disabled'] = True 
            self.fields['serialNumber'].widget.attrs['class'] = 'disabled-metadata'
        
        # Lot number (NB. Untill OMERO model is updated in 4.3, this will throw since lotNumber is not yet supported)
        try:
            if detector is not None:
                self.fields['lotNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(detector.id)+', \'lotNumber\', this.value);'}),
                    initial=detector.lotNumber,
                    required=False)
            else:
                self.fields['lotNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(detector.id)+', \'lotNumber\', this.value);'}),
                    required=False)
            self.fields['lotNumber'].widget.attrs['disabled'] = True 
            self.fields['lotNumber'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['lotNumber'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['lotNumber'].widget.attrs['disabled'] = True 
            self.fields['lotNumber'].widget.attrs['class'] = 'disabled-metadata'
            
        # Type
        try:
            if detector.getDetectorType() is not None:
                self.fields['type'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['types'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={'onchange':'saveMetadata('+str(detector.id)+', \'type\', this.options[this.selectedIndex].value);'}),
                    initial=detector.getDetectorType().value,
                    required=False) 
            else:
                self.fields['type'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['types'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={'onchange':'saveMetadata('+str(detector.id)+', \'type\', this.options[this.selectedIndex].value);'}),
                    required=False) 
            self.fields['type'].widget.attrs['disabled'] = True 
            self.fields['type'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['type'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['type'].widget.attrs['disabled'] = True 
            self.fields['type'].widget.attrs['class'] = 'disabled-metadata'
        
        # Gain
        try:
            if detSet is not None:
                self.fields['gain'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(detSet.id)+', \'gain\', this.value);'}),
                    initial=detSet.gain,
                    required=False)
            elif detector is not None:
                self.fields['gain'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(detector.id)+', \'gain\', this.value);'}),
                    initial=detector.gain,
                    required=False)
            else:
                self.fields['gain'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(detSet.id)+', \'gain\', this.value);'}),
                    required=False)
            self.fields['gain'].widget.attrs['disabled'] = True 
            self.fields['gain'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['gain'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['gain'].widget.attrs['disabled'] = True 
            self.fields['gain'].widget.attrs['class'] = 'disabled-metadata'
        
        # Voltage
        try:
            if detSet is not None and detSet.voltage is not None:
                self.fields['voltage'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(detSet.id)+', \'voltage\', this.value);'}),
                    initial=detSet.voltage.getValue(),
                    label="Voltage (%s)" % detSet.voltage.getSymbol(),
                    required=False)
            elif detector is not None:
                self.fields['voltage'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(detector.id)+', \'voltage\', this.value);'}),
                    initial=detector.voltage.getValue(),
                    label="Voltage (%s)" % detector.voltage.getSymbol(),
                    required=False)
            else:
                self.fields['voltage'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(detSet.id)+', \'voltage\', this.value);'}),
                    required=False)
            self.fields['voltage'].widget.attrs['disabled'] = True 
            self.fields['voltage'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['voltage'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['voltage'].widget.attrs['disabled'] = True 
            self.fields['voltage'].widget.attrs['class'] = 'disabled-metadata'
        
        # Offset
        try:
            if detSet is not None:
                self.fields['offsetValue'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(detSet.id)+', \'offsetValue\', this.value);'}),
                    initial=detSet.offsetValue,
                    label="Offset",
                    required=False)
            elif detector is not None:
                self.fields['offsetValue'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(detector.id)+', \'offsetValue\', this.value);'}),
                    initial=detector.offsetValue,
                    label="Offset",
                    required=False)
            else:
                self.fields['offsetValue'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(detSet.id)+', \'offsetValue\', this.value);'}),
                    label="Offset",
                    required=False)
            self.fields['offsetValue'].widget.attrs['disabled'] = True 
            self.fields['offsetValue'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['offsetValue'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Offset",
                required=False)
            self.fields['offsetValue'].widget.attrs['disabled'] = True 
            self.fields['offsetValue'].widget.attrs['class'] = 'disabled-metadata'
        
        # Zoom
        try:
            if detector is not None:
                self.fields['zoom'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(detector.id)+', \'zoom\', this.value);'}),
                    initial=detector.zoom,
                    required=False)
            else:
                self.fields['zoom'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(detector.id)+', \'voltage\', this.value);'}),
                    required=False)
            self.fields['zoom'].widget.attrs['disabled'] = True 
            self.fields['zoom'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['zoom'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['zoom'].widget.attrs['disabled'] = True 
            self.fields['zoom'].widget.attrs['class'] = 'disabled-metadata'
        
        # Amplification gain
        try:
            if detector is not None:
                self.fields['amplificationGain'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(detector.id)+', \'amplificationGain\', this.value);'}),
                    initial=detector.amplificationGain,
                    label="Amplification gain",
                    required=False)
            else:
                self.fields['amplificationGain'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(detector.id)+', \'amplificationGain\', this.value);'}),
                    label="Amplification gain",
                    required=False)
            self.fields['amplificationGain'].widget.attrs['disabled'] = True 
            self.fields['amplificationGain'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['amplificationGain'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Amplification gain",
                required=False)
            self.fields['amplificationGain'].widget.attrs['disabled'] = True 
            self.fields['amplificationGain'].widget.attrs['class'] = 'disabled-metadata'
        
        # Read out rate
        try:
            if detSet is not None and detSet.readOutRate is not None:
                self.fields['readOutRate'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(detSet.id)+', \'readOutRate\', this.value);'}),
                    initial=detSet.readOutRate.getValue(),
                    label="Read out rate (%s)" % detSet.readOutRate.getSymbol(),
                    required=False)
            else:
                self.fields['readOutRate'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(detSet.id)+', \'readOutRate\', this.value);'}),
                    label="Read out rate",
                    required=False)
            self.fields['readOutRate'].widget.attrs['disabled'] = True 
            self.fields['readOutRate'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['readOutRate'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Read out rate",
                required=False)
            self.fields['readOutRate'].widget.attrs['disabled'] = True 
            self.fields['readOutRate'].widget.attrs['class'] = 'disabled-metadata'
            
        # Binning
        try:
            if detSet is not None:
                self.fields['binning'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['binnings'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={'onchange':'saveMetadata('+str(detSet.id)+', \'type\', this.options[this.selectedIndex].value);'}),
                    initial=detSet.getBinning().value,
                    required=False) 
            else:
                self.fields['binning'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['binnings'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={'onchange':'saveMetadata('+str(detSet.id)+', \'type\', this.options[this.selectedIndex].value);'}),
                    required=False) 
            self.fields['binning'].widget.attrs['disabled'] = True 
            self.fields['binning'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['binning'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['binning'].widget.attrs['disabled'] = True 
            self.fields['binning'].widget.attrs['class'] = 'disabled-metadata'
            
        
        self.fields.keyOrder = ['manufacturer', 'model', 'serialNumber', 'lotNumber', 'type', 'gain', 'voltage', 'offsetValue', 'zoom', 'amplificationGain', 'readOutRate', 'binning']


class MetadataLightSourceForm(forms.Form):
    
    BOOLEAN_CHOICES = (
        ('', '---------'),
        ('True', 'True'),
        ('False', 'False'),
    )
        
    def __init__(self, *args, **kwargs):
        super(MetadataLightSourceForm, self).__init__(*args, **kwargs)
        
        lightSource = kwargs['initial']['lightSource']
        lightSourceSettings = None
        if 'lightSourceSettings' in kwargs['initial']:
            lightSourceSettings = kwargs['initial']['lightSourceSettings']

        self.lightSourceType = lightSource.OMERO_CLASS
        
        # Manufacturer
        try:
            if lightSource.manufacturer is not None:
                self.fields['manufacturer'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(lightSource.id)+', \'model\', this.value);'}),
                    initial=lightSource.manufacturer,
                    required=False)
            else:
                self.fields['manufacturer'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(lightSource.id)+', \'model\', this.value);'}),
                    required=False)
            self.fields['manufacturer'].widget.attrs['disabled'] = True 
            self.fields['manufacturer'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['manufacturer'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['manufacturer'].widget.attrs['disabled'] = True 
            self.fields['manufacturer'].widget.attrs['class'] = 'disabled-metadata'
        
        # Model
        try:
            if lightSource.model is not None:
                self.fields['model'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(lightSource.id)+', \'model\', this.value);'}),
                    initial=lightSource.model,
                    required=False)
            else:
                self.fields['model'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(lightSource.id)+', \'model\', this.value);'}),
                    required=False)
            self.fields['model'].widget.attrs['disabled'] = True 
            self.fields['model'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['model'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['model'].widget.attrs['disabled'] = True 
            self.fields['model'].widget.attrs['class'] = 'disabled-metadata'
        
        # Serial Number
        try:
            if lightSource.serialNumber is not None:
                self.fields['serialNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(lightSource.id)+', \'serialNumber\', this.value);'}),
                    initial=lightSource.serialNumber,
                    label="Serial number",
                    required=False)
            else:
                self.fields['serialNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(lightSource.id)+', \'serialNumber\', this.value);'}),
                    label="Serial number",
                    required=False)
            self.fields['serialNumber'].widget.attrs['disabled'] = True 
            self.fields['serialNumber'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['serialNumber'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Serial number",
                required=False)
            self.fields['serialNumber'].widget.attrs['disabled'] = True 
            self.fields['serialNumber'].widget.attrs['class'] = 'disabled-metadata'
        
        # Lot Number
        try:
            if lightSource.lotNumber is not None:
                self.fields['lotNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(lightSource.id)+', \'lotNumber\', this.value);'}),
                    initial=lightSource.lotNumber,
                    label="Lot number",
                    required=False)
            else:
                self.fields['lotNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(lightSource.id)+', \'lotNumber\', this.value);'}),
                    label="Lot number",
                    required=False)
            self.fields['lotNumber'].widget.attrs['disabled'] = True 
            self.fields['lotNumber'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['lotNumber'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Lot number",
                required=False)
            self.fields['lotNumber'].widget.attrs['disabled'] = True 
            self.fields['lotNumber'].widget.attrs['class'] = 'disabled-metadata'
        
        # Power
        try:
            if lightSource.power is not None:
                self.fields['power'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(lightSource.id)+', \'power\', this.value);'}),
                    initial=lightSource.power.getValue(),
                    label="Power (%s)" % lightSource.power.getSymbol(),
                    required=False)
            else:
                self.fields['power'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(lightSource.id)+', \'power\', this.value);'}),
                    required=False)
            self.fields['power'].widget.attrs['disabled'] = True 
            self.fields['power'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['power'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['power'].widget.attrs['disabled'] = True 
            self.fields['power'].widget.attrs['class'] = 'disabled-metadata'
        
        # Type
        try:
            if lightSource.getLightSourceType() is not None:
                self.fields['lstype'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['lstypes'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={'onchange':'saveMetadata('+str(lightSource.id)+', \'type\', this.options[this.selectedIndex].value);'}),
                    label="Type",
                    initial=lightSource.getLightSourceType().value,
                    required=False) 
            else:
                self.fields['lstype'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['lstypes'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={'onchange':'saveMetadata('+str(lightSource.id)+', \'type\', this.options[this.selectedIndex].value);'}),
                    label="Type",
                    required=False) 
            self.fields['lstype'].widget.attrs['disabled'] = True 
            self.fields['lstype'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['lstype'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Type",
                required=False)
            self.fields['lstype'].widget.attrs['disabled'] = True 
            self.fields['lstype'].widget.attrs['class'] = 'disabled-metadata'

        # Pump (laser only)
        try:
            pump = lightSource.getPump()    # Will throw exception for non-Laser lightsources.
            pumpType = pump.OMERO_CLASS     # E.g. 'Arc'
            pumpModel = pump.getModel()
            pumpValue = "%s: %s" % (pumpType, pumpModel)
            self.fields['pump'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial=pumpValue,
                required=False)
        except:
            # Not a Laser - don't show Pump
            self.fields['pump'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
        self.fields['pump'].widget.attrs['disabled'] = True
        self.fields['pump'].widget.attrs['class'] = 'disabled-metadata'
        
        # Medium
        try:
            if lightSource.getLaserMedium() is not None:
                self.fields['lmedium'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['mediums'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={'onchange':'saveMetadata('+str(lightSource.id)+', \'medium\', this.options[this.selectedIndex].value);'}),
                    initial=lightSource.getLaserMedium().value,
                    label="Medium",
                    required=False) 
            else:
                self.fields['lmedium'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['mediums'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={'onchange':'saveMetadata('+str(lightSource.id)+', \'medium\', this.options[this.selectedIndex].value);'}),
                    label="Medium",
                    required=False) 
            self.fields['lmedium'].widget.attrs['disabled'] = True 
            self.fields['lmedium'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['lmedium'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Medium",
                required=False)
            self.fields['lmedium'].widget.attrs['disabled'] = True 
            self.fields['lmedium'].widget.attrs['class'] = 'disabled-metadata'
        
        # Wavelength
        try:
            if lightSourceSettings is not None and lightSourceSettings.wavelength is not None:
                self.fields['wavelength'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(lightSourceSettings.id)+', \'wavelength\', this.value);'}),
                    initial=lightSourceSettings.wavelength.getValue(),
                    label = "Wavelength (%s)" % lightSourceSettings.wavelength.getSymbol(),
                    required=False)
            elif lightSource.wavelength is not None:
                self.fields['wavelength'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(lightSource.id)+', \'wavelength\', this.value);'}),
                    initial=lightSource.wavelength.getValue(),
                    label = "Wavelength (%s)" % lightSource.wavelength.getSymbol(),
                    required=False)
            else:
                self.fields['wavelength'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(lightSource.id)+', \'wavelength\', this.value);'}),
                    required=False)
            self.fields['wavelength'].widget.attrs['disabled'] = True 
            self.fields['wavelength'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['wavelength'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['wavelength'].widget.attrs['disabled'] = True 
            self.fields['wavelength'].widget.attrs['class'] = 'disabled-metadata'
        
        # FrequencyMultiplication
        try:
            if lightSource.frequencyMultiplication is not None:
                self.fields['frequencyMultiplication'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(lightSource.id)+', \'frequencyMultiplication\', this.value);'}),
                    initial=lightSource.frequencyMultiplication,
                    label="Frequency Multiplication",
                    required=False)
            else:
                self.fields['frequencyMultiplication'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(lightSource.id)+', \'frequencyMultiplication\', this.value);'}),
                    label="Frequency Multiplication",
                    required=False)
            self.fields['frequencyMultiplication'].widget.attrs['disabled'] = True 
            self.fields['frequencyMultiplication'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['frequencyMultiplication'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Frequency Multiplication",
                required=False)
            self.fields['frequencyMultiplication'].widget.attrs['disabled'] = True 
            self.fields['frequencyMultiplication'].widget.attrs['class'] = 'disabled-metadata'
        
        # Tuneable
        try:
            if lightSource.tuneable is not None:
                self.fields['tuneable'] = forms.ChoiceField(
                    choices=self.BOOLEAN_CHOICES, 
                    widget=forms.Select(attrs={'onchange':'javascript:saveMetadata('+str(lightSource.id)+', \'tuneable\', this.options[this.selectedIndex].value);'}),
                    initial=lightSource.tuneable,
                    required=False)
            else:
                self.fields['tuneable'] = forms.ChoiceField(
                    choices=self.BOOLEAN_CHOICES, 
                    widget=forms.Select(attrs={'onchange':'javascript:saveMetadata('+str(lightSource.id)+', \'tuneable\', this.options[this.selectedIndex].value);'}),
                    required=False)
            self.fields['tuneable'].widget.attrs['disabled'] = True 
            self.fields['tuneable'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['tuneable'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['tuneable'].widget.attrs['disabled'] = True 
            self.fields['tuneable'].widget.attrs['class'] = 'disabled-metadata'
        
        # Pulse
        try:
            if lightSource.pulse is not None:
                self.fields['pulse'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['pulses'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={'onchange':'saveMetadata('+str(lightSource.id)+', \'pulse\', this.options[this.selectedIndex].value);'}),
                    initial=lightSource.pulse,
                    required=False) 
            else:
                self.fields['pulse'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['pulses'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={'onchange':'saveMetadata('+str(lightSource.id)+', \'pulse\', this.options[this.selectedIndex].value);'}),
                    required=False) 
            self.fields['pulse'].widget.attrs['disabled'] = True 
            self.fields['pulse'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['pulse'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['pulse'].widget.attrs['disabled'] = True 
            self.fields['pulse'].widget.attrs['class'] = 'disabled-metadata'
        
        # Repetition Rate
        try:
            if lightSource.repetitionRate is not None:
                self.fields['repetitionRate'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(lightSource.id)+', \'repetitionRate\', this.value);'}),
                    initial=lightSource.repetitionRate.getValue(),
                    label="Repetition rate (%s)" % lightSource.repetitionRate.getSymbol(),
                    required=False)
            else:
                self.fields['repetitionRate'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(lightSource.id)+', \'repetitionRate\', this.value);'}),
                    label="Repetition rate",
                    required=False)
            self.fields['repetitionRate'].widget.attrs['disabled'] = True 
            self.fields['repetitionRate'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['repetitionRate'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Repetition rate",
                required=False)
            self.fields['repetitionRate'].widget.attrs['disabled'] = True 
            self.fields['repetitionRate'].widget.attrs['class'] = 'disabled-metadata'
        
        # Pockel Cell
        try:
            if lightSource.pockelCell is not None:
                self.fields['pockelCell'] = forms.ChoiceField(
                    choices=self.BOOLEAN_CHOICES, 
                    widget=forms.Select(attrs={'onchange':'javascript:saveMetadata('+str(lightSource.id)+', \'pockelCell\', this.options[this.selectedIndex].value);'}),
                    initial=lightSource.pockelCell,
                    label="Pockel Cell",
                    required=False)
            else:
                self.fields['pockelCell'] = forms.ChoiceField(
                    choices=self.BOOLEAN_CHOICES, 
                    widget=forms.Select(attrs={'onchange':'javascript:saveMetadata('+str(lightSource.id)+', \'pockelCell\', this.options[this.selectedIndex].value);'}),
                    label="Pockel Cell",
                    required=False)
            self.fields['pockelCell'].widget.attrs['disabled'] = True 
            self.fields['pockelCell'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['pockelCell'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="Pockel Cell",
                required=False)
            self.fields['pockelCell'].widget.attrs['disabled'] = True 
            self.fields['pockelCell'].widget.attrs['class'] = 'disabled-metadata'
        
        # Attenuation
        if lightSourceSettings is not None and lightSourceSettings.attenuation is not None:
            self.fields['attenuation'] = forms.CharField(
                max_length=100,
                widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(lightSourceSettings.id)+', \'attenuation\', this.value);'}),
                initial=formatPercentFraction(lightSourceSettings.attenuation),
                label="Attenuation (%)",
                required=False)
        else:
            self.fields['attenuation'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
        self.fields['attenuation'].widget.attrs['disabled'] = True
        self.fields['attenuation'].widget.attrs['class'] = 'disabled-metadata'

        self.fields.keyOrder = ['manufacturer', 'model', 'serialNumber', 'lotNumber', 'power', 'lstype', 'pump', 'lmedium', 'wavelength', 'frequencyMultiplication', 'tuneable', 'pulse' , 'repetitionRate', 'pockelCell', 'attenuation']
    

class MetadataEnvironmentForm(forms.Form):
    
    def __init__(self, *args, **kwargs):
        super(MetadataEnvironmentForm, self).__init__(*args, **kwargs)
        
        # Imaging environment 

        imagingEnv = kwargs['initial']['image'].getImagingEnvironment()        
        # Temperature
        try:
            if imagingEnv.temperature is not None:
                self.fields['temperature'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'temperature\', this.value);'}),
                    initial=imagingEnv.temperature.getValue(),
                    label = "Temperature (%s)" % imagingEnv.temperature.getSymbol(),
                    required=False)
            else:
                self.fields['temperature'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'temperature\', this.value);'}),
                    required=False)
            self.fields['temperature'].widget.attrs['disabled'] = True 
            self.fields['temperature'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['temperature'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['temperature'].widget.attrs['disabled'] = True 
            self.fields['temperature'].widget.attrs['class'] = 'disabled-metadata'
        
        # Air Pressure
        try:
            if imagingEnv.airPressure is not None:
                self.fields['airPressure'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'airPressure\', this.value);'}),
                    initial=imagingEnv.airPressure.getValue(),
                    label="Air Pressure (%s)" % imagingEnv.airPressure.getSymbol(),
                    required=False)
            else:
                self.fields['airPressure'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'airPressure\', this.value);'}),
                    label="Air Pressure",
                    required=False)
            self.fields['airPressure'].widget.attrs['disabled'] = True 
            self.fields['airPressure'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['airPressure'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                label="Air Pressure",
                initial="N/A",
                required=False)
            self.fields['airPressure'].widget.attrs['disabled'] = True 
            self.fields['airPressure'].widget.attrs['class'] = 'disabled-metadata'
        
        # Humidity
        try:
            if imagingEnv.humidity is not None:
                self.fields['humidity'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'humidity\', this.value);'}),
                    initial=imagingEnv.humidity,
                    required=False)
            else:
                self.fields['humidity'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'humidity\', this.value);'}),
                    required=False)
            self.fields['humidity'].widget.attrs['disabled'] = True 
            self.fields['humidity'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['humidity'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                required=False)
            self.fields['humidity'].widget.attrs['disabled'] = True 
            self.fields['humidity'].widget.attrs['class'] = 'disabled-metadata'
        
        # CO2 percent
        try:
            if imagingEnv.co2percent is not None:
                self.fields['co2percent'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'co2percent\', this.value);'}),
                    initial=imagingEnv.co2percent,
                    label="CO2 (%)",
                    required=False)
            else:
                self.fields['co2percent'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'co2percent\', this.value);'}),
                    label="CO2 (%)",
                    required=False)
            self.fields['co2percent'].widget.attrs['disabled'] = True 
            self.fields['co2percent'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['co2percent'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size':25}),
                initial="N/A",
                label="CO2 (%)",
                required=False)
            self.fields['co2percent'].widget.attrs['disabled'] = True 
            self.fields['co2percent'].widget.attrs['class'] = 'disabled-metadata'
        
        self.fields.keyOrder = ['airPressure', 'co2percent', 'humidity', 'temperature']

class MetadataStageLabelForm(forms.Form):
    
    def __init__(self, *args, **kwargs):
        super(MetadataStageLabelForm, self).__init__(*args, **kwargs)
        
        # Stage label
        
        # Position x
        try:
            if kwargs['initial']['image'].getStageLabel() is not None:
                self.fields['positionx'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'positionx\', this.value);'}), initial=kwargs['initial']['image'].getStageLabel().positionx, label="Position X", required=False)
            else:
                self.fields['positionx'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'positionx\', this.value);'}), label="Position X", required=False)
            self.fields['positionx'].widget.attrs['disabled'] = True 
            self.fields['positionx'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['positionx'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25}), initial="N/A", label="Position X", required=False)
            self.fields['positionx'].widget.attrs['disabled'] = True 
            self.fields['positionx'].widget.attrs['class'] = 'disabled-metadata'
        
        # Position y
        try:
            if kwargs['initial']['image'].getStageLabel() is not None:
                self.fields['positiony'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'positiony\', this.value);'}), initial=kwargs['initial']['image'].getStageLabel().positiony, label="Position Y", required=False)
            else:
                self.fields['positiony'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'positiony\', this.value);'}), label="Position Y", required=False)
            self.fields['positiony'].widget.attrs['disabled'] = True 
            self.fields['positiony'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['positiony'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25}), initial="N/A", label="Position Y", required=False)
            self.fields['positiony'].widget.attrs['disabled'] = True 
            self.fields['positiony'].widget.attrs['class'] = 'disabled-metadata'
        
        # Position z
        try:
            if kwargs['initial']['image'].getStageLabel() is not None:
                self.fields['positionz'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'positionz\', this.value);'}), initial=kwargs['initial']['image'].getStageLabel().positionz, label="Position Z", required=False)
            else:
                self.fields['positionz'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'onchange':'javascript:saveMetadata('+str(kwargs['initial']['image'].id)+', \'positionz\', this.value);'}), label="Position Z", required=False)
            self.fields['positionz'].widget.attrs['disabled'] = True 
            self.fields['positionz'].widget.attrs['class'] = 'disabled-metadata'
        except:
            self.fields['positionz'] = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25}), initial="N/A", label="Position Z", required=False)
            self.fields['positionz'].widget.attrs['disabled'] = True 
            self.fields['positionz'].widget.attrs['class'] = 'disabled-metadata'
        
        self.fields.keyOrder = ['positionx', 'positiony', 'positionz']
