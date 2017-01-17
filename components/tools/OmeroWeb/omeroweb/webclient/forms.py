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
from custom_forms import MetadataModelChoiceField
from custom_forms import AnnotationModelMultipleChoiceField
from custom_forms import ObjectModelMultipleChoiceField
from omeroweb.webadmin.custom_forms import ExperimenterModelMultipleChoiceField
from omeroweb.webadmin.custom_forms import GroupModelMultipleChoiceField
from omeroweb.webadmin.custom_forms import GroupModelChoiceField
from omeroweb.webclient.webclient_utils import formatPercentFraction

logger = logging.getLogger(__name__)


##################################################################
# Static values

# TODO: change to reverse
help_button = "%swebgateway/img/help16.png" % settings.STATIC_URL

help_enable = (
    '<span class="tooltip" title="Enable/Disable: This option'
    ' allows the owner to keep the access control of the share.">'
    '<img src="%s" /></span>') % help_button

help_expire = (
    '<span class="tooltip" title="Expiry date: This date defines'
    ' when the share will stop being available. Date format:'
    ' YYYY-MM-DD."><img src="%s" /></span>') % help_button


#################################################################
# Non-model Form


class GlobalSearchForm(NonASCIIForm):

    search_query = forms.CharField(widget=forms.TextInput(attrs={'size': 25}))


class ShareForm(NonASCIIForm):

    def __init__(self, *args, **kwargs):
        super(ShareForm, self).__init__(*args, **kwargs)

        try:
            if kwargs['initial']['shareMembers']:
                pass
            self.fields['members'] = ExperimenterModelMultipleChoiceField(
                queryset=kwargs['initial']['experimenters'],
                initial=kwargs['initial']['shareMembers'],
                widget=forms.SelectMultiple(attrs={'size': 28}))
        except:
            self.fields['members'] = ExperimenterModelMultipleChoiceField(
                queryset=kwargs['initial']['experimenters'],
                widget=forms.SelectMultiple(attrs={'size': 28}))
        self.fields.keyOrder = [
            'message', 'expiration', 'enable', 'members']  # , 'guests']

    message = forms.CharField(
        widget=forms.Textarea(attrs={'rows': 5, 'cols': 50}))
    expiration = forms.CharField(
        max_length=100,
        widget=forms.TextInput(attrs={'size': 10}),
        label="Expiry date",
        help_text=help_expire,
        required=False)
    enable = forms.BooleanField(required=False, help_text=help_enable)
    # guests = MultiEmailField(required=False,
    # widget=forms.TextInput(attrs={'size':75}))

    def clean_expiration(self):
        if (self.cleaned_data['expiration'] is not None and
                len(self.cleaned_data['expiration']) < 1):
            return None
        if self.cleaned_data['expiration'] is not None:
            d = str(self.cleaned_data['expiration']).rsplit("-")
            try:
                date = datetime.datetime.strptime(
                    ("%s-%s-%s" % (d[0], d[1], d[2])), "%Y-%m-%d")
            except:
                raise forms.ValidationError(
                    'Date is in the wrong format. YY-MM-DD')
            if time.mktime(date.timetuple()) <= time.time():
                raise forms.ValidationError(
                    'Expiry date must be in the future.')
        return self.cleaned_data['expiration']


class BasketShareForm(ShareForm):

    def __init__(self, *args, **kwargs):
        super(BasketShareForm, self).__init__(*args, **kwargs)

        try:
            self.fields['image'] = GroupModelMultipleChoiceField(
                queryset=kwargs['initial']['images'],
                initial=kwargs['initial']['selected'],
                widget=forms.SelectMultiple(attrs={'size': 10}))
        except:
            self.fields['image'] = GroupModelMultipleChoiceField(
                queryset=kwargs['initial']['images'],
                widget=forms.SelectMultiple(attrs={'size': 10}))


class ContainerForm(NonASCIIForm):

    name = forms.CharField(
        max_length=250,
        widget=forms.TextInput(attrs={'size': 45}))
    description = forms.CharField(
        widget=forms.Textarea(attrs={'rows': 2, 'cols': 49}),
        required=False)


class ContainerNameForm(NonASCIIForm):

    name = forms.CharField(
        max_length=250,
        widget=forms.TextInput(attrs={'size': 45}))


class ContainerDescriptionForm(NonASCIIForm):

    description = forms.CharField(
        widget=forms.Textarea(attrs={'rows': 3, 'cols': 39}),
        required=False)


class BaseAnnotationForm(NonASCIIForm):
    """
    This is the superclass of the various forms used for annotating single or
    multiple objects.
    All these forms use hidden fields to specify the object(s) currently being
    annotated.
    """
    def __init__(self, *args, **kwargs):
        super(BaseAnnotationForm, self).__init__(*args, **kwargs)

        images = ('images' in kwargs['initial'] and
                  kwargs['initial']['images'] or list())
        if len(images) > 0:
            try:
                self.fields['image'] = ObjectModelMultipleChoiceField(
                    queryset=images,
                    initial=kwargs['initial']['selected']['images'],
                    widget=forms.SelectMultiple(attrs={'size': 10}),
                    required=False)
            except:
                self.fields['image'] = ObjectModelMultipleChoiceField(
                    queryset=images,
                    widget=forms.SelectMultiple(attrs={'size': 10}),
                    required=False)

        datasets = ('datasets' in kwargs['initial'] and
                    kwargs['initial']['datasets'] or list())
        if len(datasets) > 0:
            try:
                self.fields['dataset'] = ObjectModelMultipleChoiceField(
                    queryset=datasets,
                    initial=kwargs['initial']['selected']['datasets'],
                    widget=forms.SelectMultiple(attrs={'size': 10}),
                    required=False)
            except:
                self.fields['dataset'] = ObjectModelMultipleChoiceField(
                    queryset=datasets,
                    widget=forms.SelectMultiple(attrs={'size': 10}),
                    required=False)

        projects = ('projects' in kwargs['initial'] and
                    kwargs['initial']['projects'] or list())
        if len(projects) > 0:
            try:
                self.fields['project'] = ObjectModelMultipleChoiceField(
                    queryset=projects,
                    initial=kwargs['initial']['selected']['projects'],
                    widget=forms.SelectMultiple(attrs={'size': 10}),
                    required=False)
            except:
                self.fields['project'] = ObjectModelMultipleChoiceField(
                    queryset=projects,
                    widget=forms.SelectMultiple(attrs={'size': 10}),
                    required=False)

        screens = ('screens' in kwargs['initial'] and
                   kwargs['initial']['screens'] or list())
        if len(screens) > 0:
            try:
                self.fields['screen'] = ObjectModelMultipleChoiceField(
                    queryset=screens,
                    initial=kwargs['initial']['selected']['screens'],
                    widget=forms.SelectMultiple(attrs={'size': 10}),
                    required=False)
            except:
                self.fields['screen'] = ObjectModelMultipleChoiceField(
                    queryset=screens,
                    widget=forms.SelectMultiple(attrs={'size': 10}),
                    required=False)

        plates = ('plates' in kwargs['initial'] and
                  kwargs['initial']['plates'] or list())
        if len(plates) > 0:
            try:
                self.fields['plate'] = ObjectModelMultipleChoiceField(
                    queryset=plates,
                    initial=kwargs['initial']['selected']['plates'],
                    widget=forms.SelectMultiple(attrs={'size': 10}),
                    required=False)
            except:
                self.fields['plate'] = ObjectModelMultipleChoiceField(
                    queryset=plates,
                    widget=forms.SelectMultiple(attrs={'size': 10}),
                    required=False)

        acquisitions = ('acquisitions' in kwargs['initial'] and
                        kwargs['initial']['acquisitions'] or list())
        if len(acquisitions) > 0:
            try:
                self.fields['acquisition'] = ObjectModelMultipleChoiceField(
                    queryset=acquisitions,
                    initial=kwargs['initial']['selected']['acquisitions'],
                    widget=forms.SelectMultiple(attrs={'size': 10}),
                    required=False)
            except:
                self.fields['acquisition'] = ObjectModelMultipleChoiceField(
                    queryset=acquisitions,
                    widget=forms.SelectMultiple(attrs={'size': 10}),
                    required=False)

        wells = ('wells' in kwargs['initial'] and
                 kwargs['initial']['wells'] or list())
        if len(wells) > 0:
            try:
                self.fields['well'] = ObjectModelMultipleChoiceField(
                    queryset=wells,
                    initial=kwargs['initial']['selected']['wells'],
                    widget=forms.SelectMultiple(attrs={'size': 10}),
                    required=False)
            except:
                self.fields['well'] = ObjectModelMultipleChoiceField(
                    queryset=wells,
                    widget=forms.SelectMultiple(attrs={'size': 10}),
                    required=False)

        shares = ('shares' in kwargs['initial'] and
                  kwargs['initial']['shares'] or list())
        if len(shares) > 0:
            try:
                self.fields['share'] = ObjectModelMultipleChoiceField(
                    queryset=shares,
                    initial=kwargs['initial']['selected']['shares'],
                    widget=forms.SelectMultiple(attrs={'size': 10}),
                    required=False)
            except:
                self.fields['share'] = ObjectModelMultipleChoiceField(
                    queryset=shares,
                    widget=forms.SelectMultiple(attrs={'size': 10}),
                    required=False)


class TagsAnnotationForm(BaseAnnotationForm):
    """
    Form for annotating one or more objects with existing Tags or New tags
    """

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
    tagset = forms.IntegerField(
        min_value=1, required=False, widget=forms.HiddenInput)

NewTagsAnnotationFormSet = formset_factory(NewTagsAnnotationForm, extra=0)


class FilesAnnotationForm(BaseAnnotationForm):

    def __init__(self, *args, **kwargs):
        super(FilesAnnotationForm, self).__init__(*args, **kwargs)
        self.fields['files'] = AnnotationModelMultipleChoiceField(
            queryset=kwargs['initial']['files'],
            widget=forms.SelectMultiple(attrs={
                'size': 8, 'class': 'existing'}),
            required=False)

    annotation_file = forms.FileField(required=False)


class CommentAnnotationForm(BaseAnnotationForm):
    comment = forms.CharField(
        widget=forms.Textarea(attrs={'rows': 2, 'cols': 39}))


class ActiveGroupForm(forms.Form):

    def __init__(self, *args, **kwargs):
        super(ActiveGroupForm, self).__init__(*args, **kwargs)
        try:
            self.fields['active_group'] = GroupModelChoiceField(
                queryset=kwargs['initial']['mygroups'],
                initial=kwargs['initial']['activeGroup'],
                empty_label=None,
                widget=forms.Select(attrs={
                    'onchange': (
                        'window.location.href=\'' +
                        reverse(viewname="change_active_group") +
                        '?url=' + kwargs['initial']['url'] +
                        '&active_group=\''
                        '+this.options[this.selectedIndex].value')}))
        except:
            self.fields['active_group'] = GroupModelChoiceField(
                queryset=kwargs['initial']['mygroups'],
                initial=kwargs['initial']['activeGroup'],
                empty_label=None, widget=forms.Select(attrs={
                    'onchange': (
                        'window.location.href=\'' +
                        reverse(viewname="change_active_group") +
                        '?active_group=\''
                        '+this.options[this.selectedIndex].value')}))
        self.fields.keyOrder = ['active_group']


class WellIndexForm(forms.Form):

    def __init__(self, *args, **kwargs):
        super(WellIndexForm, self).__init__(*args, **kwargs)
        rmin, rmax = kwargs['initial']['range']
        choices = [(str(i), "Field#%i" % (i-rmin+1))
                   for i in range(rmin, rmax+1)]
        self.fields['index'] = forms.ChoiceField(
            choices=tuple(choices),
            widget=forms.Select(attrs={
                'onchange': (
                    'changeField(this.options[this.selectedIndex].value);')}))
        self.fields.keyOrder = ['index']

###############################
# METADATA FORMS


def save_metadata(obj, name, options=False):
    s = 'javascript:save_metadata(' + str(obj) + ', \'' + name + '\', '
    if options:
        s += 'this.options[this.selectedIndex].value);'
    else:
        s += 'this.value);'

    return s


def set_widget_attrs(field, set_class=True):
    field.widget.attrs['disabled'] = True
    if set_class:
        field.widget.attrs['class'] = 'disabled-metadata'


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
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(logicalCh.id)}),
                    initial=logicalCh.name,
                    required=False)
            else:
                self.fields['name'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(logicalCh.id, 'name')}),
                    required=False)
            set_widget_attrs(self.fields['name'])
        except:
            self.fields['name'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['name'])

        # excitationWave
        try:
            if logicalCh is not None:
                self.fields['excitationWave'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(logicalCh.id, 'name')}),
                    initial=kwargs['initial']['exWave'].getValue(),
                    label=("Excitation (%s)"
                           % kwargs['initial']['exWave'].getSymbol()),
                    required=False)
            else:
                self.fields['excitationWave'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(logicalCh.id, 'name')}),
                    label="Excitation",
                    required=False)
            set_widget_attrs(self.fields['excitationWave'])
        except:
            self.fields['excitationWave'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Excitation",
                required=False)
            set_widget_attrs(self.fields['excitationWave'])

        # emissionWave
        try:
            if logicalCh is not None:
                self.fields['emissionWave'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(logicalCh.id, 'name')}),
                    initial=kwargs['initial']['emWave'].getValue(),
                    label=("Emission (%s)"
                           % kwargs['initial']['emWave'].getSymbol()),
                    required=False)
            else:
                self.fields['emissionWave'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(logicalCh.id, 'name')}),
                    label="Emission",
                    required=False)
            set_widget_attrs(self.fields['emissionWave'])
        except:
            self.fields['emissionWave'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Emission",
                required=False)
            set_widget_attrs(self.fields['emissionWave'])

        # ndFilter
        try:
            if logicalCh is not None and logicalCh.ndFilter is not None:
                self.fields['ndFilter'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(logicalCh.id, 'name')}),
                    initial=formatPercentFraction(logicalCh.ndFilter),
                    label="ND filter (%)",
                    required=False)
            else:
                self.fields['ndFilter'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(logicalCh.id, 'name')}),
                    label="ND filter (%)",
                    required=False)
            set_widget_attrs(self.fields['ndFilter'], set_class=False)
        except:
            self.fields['ndFilter'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="ND filter (%)",
                required=False)
            set_widget_attrs(self.fields['ndFilter'], set_class=False)

        # pinHoleSize
        try:
            if logicalCh is not None and logicalCh.pinHoleSize is not None:
                self.fields['pinHoleSize'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(logicalCh.id, 'name')}),
                    initial=logicalCh.pinHoleSize.getValue(),
                    label=("Pin hole size (%s)"
                           % logicalCh.pinHoleSize.getSymbol()),
                    required=False)
            else:
                self.fields['pinHoleSize'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(logicalCh.id, 'name')}),
                    label="Pin hole size",
                    required=False)
            set_widget_attrs(self.fields['pinHoleSize'], set_class=False)
        except:
            self.fields['pinHoleSize'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Pin hole size",
                required=False)
            set_widget_attrs(self.fields['pinHoleSize'], set_class=False)

        # fluor
        try:
            if logicalCh is not None:
                self.fields['fluor'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(logicalCh.id, 'name')}),
                    initial=logicalCh.fluor,
                    required=False)
            else:
                self.fields['fluor'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(logicalCh.id, 'name')}),
                    required=False)
            set_widget_attrs(self.fields['fluor'], set_class=False)
        except:
            self.fields['fluor'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['fluor'], set_class=False)

        # Illumination
        try:
            if logicalCh.getIllumination() is not None:
                self.fields['illumination'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['illuminations'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            logicalCh.id, 'illumination', options=True)}),
                    initial=logicalCh.getIllumination(),
                    required=False)
            else:
                self.fields['illumination'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['illuminations'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            logicalCh.id, 'illumination', options=True)}),
                    required=False)
            set_widget_attrs(self.fields['illumination'], set_class=False)
        except:
            self.fields['illumination'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['illumination'], set_class=False)

        # contrastMethods
        try:
            if logicalCh.contrastMethod is not None:
                self.fields['contrastMethod'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['contrastMethods'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            logicalCh.id, 'contrastMethod', options=True)}),
                    initial=logicalCh.getContrastMethod(),
                    label="Contrast method",
                    required=False)
            else:
                self.fields['contrastMethod'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['contrastMethods'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            logicalCh.id, 'contrastMethod', options=True)}),
                    label="Contrast method",
                    required=False)
            set_widget_attrs(self.fields['contrastMethod'])
        except:
            self.fields['contrastMethod'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Contrast method",
                required=False)
            set_widget_attrs(self.fields['contrastMethod'])

        # Mode
        try:
            if logicalCh.getMode() is not None:
                self.fields['mode'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['modes'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            logicalCh.id, 'mode', options=True)}),
                    initial=logicalCh.getMode().value,
                    required=False)
            else:
                self.fields['mode'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['modes'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            logicalCh.id, 'mode', options=True)}),
                    required=False)
            set_widget_attrs(self.fields['mode'])
        except:
            self.fields['mode'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['mode'])

        # pockelCellSetting
        try:
            if logicalCh.pockelCellSetting is not None:
                self.fields['pockelCellSetting'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(logicalCh.id, 'name')}),
                    initial=logicalCh.pockelCellSetting,
                    label="Pockel cell",
                    required=False)
            else:
                self.fields['pockelCellSetting'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(logicalCh.id, 'name')}),
                    label="Pockel cell",
                    required=False)
            set_widget_attrs(self.fields['pockelCellSetting'])
        except:
            self.fields['pockelCellSetting'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Pockel cell",
                required=False)
            set_widget_attrs(self.fields['pockelCellSetting'])

        self.fields.keyOrder = [
            'name', 'excitationWave', 'emissionWave', 'ndFilter',
            'pinHoleSize', 'fluor', 'illumination', 'contrastMethod', 'mode',
            'pockelCellSetting']


class MetadataDichroicForm(forms.Form):

    def __init__(self, *args, **kwargs):
        super(MetadataDichroicForm, self).__init__(*args, **kwargs)

        # Manufacturer
        try:
            if kwargs['initial']['dichroic'].manufacturer is not None:
                self.fields['manufacturer'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['dichroic'].id,
                            'manufacturer')}),
                    initial=kwargs['initial']['dichroic'].manufacturer,
                    required=False)
            else:
                self.fields['manufacturer'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['dichroic'].id,
                            'manufacturer')}),
                    required=False)
            set_widget_attrs(self.fields['manufacturer'])
        except:
            self.fields['manufacturer'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['manufacturer'])

        # Model
        try:
            if kwargs['initial']['dichroic'].model is not None:
                self.fields['model'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['dichroic'].id, 'model')}),
                    initial=kwargs['initial']['dichroic'].model,
                    required=False)
            else:
                self.fields['model'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['dichroic'].id, 'model')}),
                    required=False)
            set_widget_attrs(self.fields['model'])
        except:
            self.fields['model'] = forms.CharField(
                max_length=10, widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A", required=False)
            set_widget_attrs(self.fields['model'])

        # Serial number
        try:
            if kwargs['initial']['dichroic'].serialNumber is not None:
                self.fields['serialNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['dichroic'].serialNumber,
                            'serialNumber')}),
                    initial=kwargs['initial']['dichroic'].serialNumber,
                    label="Serial number",
                    required=False)
            else:
                self.fields['serialNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['dichroic'].serialNumber,
                            'serialNumber')}),
                    label="Serial number",
                    required=False)
            set_widget_attrs(self.fields['serialNumber'])
        except:
            self.fields['serialNumber'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Serial number",
                required=False)
            set_widget_attrs(self.fields['serialNumber'])

        # Lot number
        try:
            if kwargs['initial']['dichroic'].lotNumber is not None:
                self.fields['lotNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['dichroic'].lotNumber,
                            'lotNumber')}),
                    initial=kwargs['initial']['dichroic'].lotNumber,
                    label="Lot number",
                    required=False)
            else:
                self.fields['lotNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['dichroic'].lotNumber,
                            'lotNumber')}),
                    label="Lot number",
                    required=False)
            set_widget_attrs(self.fields['lotNumber'])
        except:
            self.fields['lotNumber'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Lot number",
                required=False)
            set_widget_attrs(self.fields['lotNumber'])

        self.fields.keyOrder = [
            'model', 'manufacturer', 'serialNumber', 'lotNumber']


class MetadataMicroscopeForm(forms.Form):

    def __init__(self, *args, **kwargs):
        super(MetadataMicroscopeForm, self).__init__(*args, **kwargs)

        # Model
        try:
            if kwargs['initial']['microscope'].model is not None:
                self.fields['model'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['microscope'].id, 'model')}),
                    initial=kwargs['initial']['microscope'].model,
                    required=False)
            else:
                self.fields['model'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['microscope'].id, 'model')}),
                    required=False)
            set_widget_attrs(self.fields['model'])
        except:
            self.fields['model'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['model'])

        # Manufacturer
        try:
            if kwargs['initial']['microscope'].manufacturer is not None:
                self.fields['manufacturer'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['microscope'].id,
                            'manufacturer')}),
                    initial=kwargs['initial']['microscope'].manufacturer,
                    required=False)
            else:
                self.fields['manufacturer'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['microscope'].id,
                            'manufacturer')}),
                    required=False)
            set_widget_attrs(self.fields['manufacturer'])
        except:
            self.fields['manufacturer'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['manufacturer'])

        # Serial number
        try:
            if kwargs['initial']['microscope'].serialNumber is not None:
                self.fields['serialNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['microscope'].id,
                            'lotNumber')}),
                    initial=kwargs['initial']['microscope'].serialNumber,
                    label="Serial number",
                    required=False)
            else:
                self.fields['serialNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['microscope'].id,
                            'lotNumber')}),
                    label="Serial number",
                    required=False)
            set_widget_attrs(self.fields['serialNumber'])
        except:
            self.fields['serialNumber'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Serial number",
                required=False)
            set_widget_attrs(self.fields['serialNumber'])

        # Lot number
        try:
            if kwargs['initial']['microscope'].lotNumber is not None:
                self.fields['lotNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['microscope'].id,
                            'lotNumber')}),
                    initial=kwargs['initial']['microscope'].lotNumber,
                    label="Lot number",
                    required=False)
            else:
                self.fields['lotNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['microscope'].id,
                            'lotNumber')}),
                    label="Serial number",
                    required=False)
            set_widget_attrs(self.fields['lotNumber'])
        except:
            self.fields['lotNumber'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Serial number",
                required=False)
            set_widget_attrs(self.fields['lotNumber'])

        # Type
        try:
            if (kwargs['initial']['microscope'].getMicroscopeType() is not
                    None):
                self.fields['type'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['microscopeTypes'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            kwargs['initial']['microscope'].id, 'type',
                            options=True)}),
                    initial=kwargs['initial'][
                        'microscope'].getMicroscopeType().value,
                    required=False)
            else:
                self.fields['type'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['microscopeTypes'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            kwargs['initial']['microscope'].id, 'type',
                            options=True)}),
                    required=False)
            set_widget_attrs(self.fields['type'])
        except:
            self.fields['type'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['type'])

        self.fields.keyOrder = [
            'model', 'manufacturer', 'serialNumber', 'lotNumber', 'type']


class MetadataObjectiveForm(forms.Form):

    def __init__(self, *args, **kwargs):
        super(MetadataObjectiveForm, self).__init__(*args, **kwargs)

        # Model
        try:
            if kwargs['initial']['objective'].model is not None:
                self.fields['model'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['objective'].id, 'model')}),
                    initial=kwargs['initial']['objective'].model,
                    required=False)
            else:
                self.fields['model'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['objective'].id, 'model')}),
                    required=False)
            set_widget_attrs(self.fields['model'])
        except:
            self.fields['model'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['model'])

        # Manufacturer
        try:
            if kwargs['initial']['objective'].manufacturer is not None:
                self.fields['manufacturer'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['objective'].id,
                            'manufacturer')}),
                    initial=kwargs['initial']['objective'].manufacturer,
                    required=False)
            else:
                self.fields['manufacturer'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['objective'].id,
                            'manufacturer')}),
                    required=False)
            set_widget_attrs(self.fields['manufacturer'])
        except:
            self.fields['manufacturer'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['manufacturer'])

        # Serial Number
        try:
            if kwargs['initial']['objective'].serialNumber is not None:
                self.fields['serialNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['objective'].id,
                            'serialNumber')}),
                    initial=kwargs['initial']['objective'].serialNumber,
                    label="Serial number",
                    required=False)
            else:
                self.fields['serialNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['objective'].id,
                            'serialNumber')}),
                    label="Serial number",
                    required=False)
            set_widget_attrs(self.fields['serialNumber'])
        except:
            self.fields['serialNumber'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Serial number",
                required=False)
            set_widget_attrs(self.fields['serialNumber'])

        # Lot number
        try:
            if kwargs['initial']['objective'].lotNumber is not None:
                self.fields['lotNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['objective'].lotNumber,
                            'lotNumber')}),
                    initial=kwargs['initial']['objective'].lotNumber,
                    label="Lot number",
                    required=False)
            else:
                self.fields['lotNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial'][
                                'logicalchannel'].getObjective().lotNumber,
                            'lotNumber')}),
                    label="Lot number",
                    required=False)
            set_widget_attrs(self.fields['lotNumber'])
        except:
            self.fields['lotNumber'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Lot number",
                required=False)
            set_widget_attrs(self.fields['lotNumber'])

        # Nominal Magnification
        try:
            if (kwargs['initial']['objective'].nominalMagnification is not
                    None):
                self.fields['nominalMagnification'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['objective'].id,
                            'nominalMagnification')}),
                    initial=kwargs['initial'][
                        'objective'].nominalMagnification,
                    label="Nominal magnification",
                    required=False)
            else:
                self.fields['nominalMagnification'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['objective'].id,
                            'nominalMagnification')}),
                    label="Nominal magnification",
                    required=False)
            set_widget_attrs(self.fields['nominalMagnification'])
        except:
            self.fields['nominalMagnification'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Nominal magnification",
                required=False)
            set_widget_attrs(self.fields['nominalMagnification'])

        # Calibrated Magnification
        try:
            if (kwargs['initial']['objective'].calibratedMagnification is not
                    None):
                self.fields['calibratedMagnification'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['objective'].id,
                            'calibratedMagnification')}),
                    initial=kwargs['initial'][
                        'objective'].calibratedMagnification,
                    label="Calibrated magnification",
                    required=False)
            else:
                self.fields['calibratedMagnification'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['objective'].id,
                            'calibratedMagnification')}),
                    label="Calibrated magnification",
                    required=False)
            set_widget_attrs(self.fields['calibratedMagnification'])
        except:
            self.fields['calibratedMagnification'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Calibrated magnification",
                required=False)
            set_widget_attrs(self.fields['calibratedMagnification'])

        # Lens NA
        try:
            if kwargs['initial']['objective'].lensNA is not None:
                self.fields['lensNA'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['objective'].id, 'lensNA')}),
                    initial=kwargs['initial']['objective'].lensNA,
                    label="Lens NA",
                    required=False)
            else:
                self.fields['lensNA'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['objective'].id, 'lensNA')}),
                    required=False)
            set_widget_attrs(self.fields['lensNA'])
        except:
            self.fields['lensNA'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Lens NA",
                required=False)
            set_widget_attrs(self.fields['lensNA'])

        # Immersion
        try:
            if kwargs['initial']['objective'].getImmersion() is not None:
                self.fields['immersion'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['immersions'],
                    empty_label=u"Not set", widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            kwargs['initial']['objective'].id,
                            'immersion', options=True)}),
                    initial=kwargs['initial'][
                        'objective'].getImmersion().value,
                    required=False)
            else:
                self.fields['immersion'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['immersions'],
                    empty_label=u"Not set", widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            kwargs['initial']['objective'].id,
                            'immersion', options=True)}),
                    required=False)
            set_widget_attrs(self.fields['immersion'])
        except:
            self.fields['immersion'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['immersion'])

        # Correction
        try:
            if kwargs['initial']['objective'].getCorrection() is not None:
                self.fields['correction'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['corrections'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            kwargs['initial']['objective'].id,
                            'correction', options=True)}),
                    initial=kwargs['initial'][
                        'objective'].getCorrection().value,
                    required=False)
            else:
                self.fields['correction'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['corrections'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            kwargs['initial']['objective'].id,
                            'correction', options=True)}),
                    required=False)
            set_widget_attrs(self.fields['correction'])

        except:
            self.fields['correction'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['correction'])

        # Working Distance
        try:
            if kwargs['initial']['objective'].workingDistance is not None:
                self.fields['workingDistance'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['objective'].id,
                            'workingDistance')}),
                    initial=kwargs['initial'][
                        'objective'].workingDistance.getValue(),
                    label=("Working distance (%s)" % kwargs['initial'][
                           'objective'].workingDistance.getSymbol()),
                    required=False)
            else:
                self.fields['workingDistance'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['objective'].id,
                            'workingDistance')}),
                    label="Working distance",
                    required=False)
            set_widget_attrs(self.fields['workingDistance'])
        except:
            self.fields['workingDistance'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Working distance",
                required=False)
            set_widget_attrs(self.fields['workingDistance'])

        # Iris
        try:
            if kwargs['initial']['objective'].getIris() is not None:
                self.fields['iris'] = forms.ChoiceField(
                    choices=self.BOOLEAN_CHOICES,  widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            kwargs['initial']['objective'].id, 'iris',
                            options=True)}),
                    initial=kwargs['initial']['objective'].getIris().value,
                    required=False)
            else:
                self.fields['iris'] = forms.ChoiceField(
                    choices=self.BOOLEAN_CHOICES,
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            kwargs['initial']['objective'].id, 'iris',
                            options=True)}),
                    required=False)
            set_widget_attrs(self.fields['iris'])
        except:
            self.fields['iris'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['iris'])

        self.fields.keyOrder = [
            'model', 'manufacturer', 'serialNumber', 'lotNumber',
            'nominalMagnification', 'calibratedMagnification', 'lensNA',
            'immersion', 'correction', 'workingDistance', 'iris']


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
            if (kwargs['initial']['objectiveSettings'].correctionCollar is not
                    None):
                self.fields['correctionCollar'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['objectiveSettings'].id,
                            'correctionCollar')}),
                    initial=kwargs['initial'][
                        'objectiveSettings'].correctionCollar,
                    label="Correction collar",
                    required=False)
            else:
                self.fields['correctionCollar'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['objectiveSettings'].id,
                            'correctionCollar')}),
                        label="Correction collar",
                        required=False)
            set_widget_attrs(self.fields['correctionCollar'])
        except:
            self.fields['correctionCollar'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Correction collar",
                required=False)
            set_widget_attrs(self.fields['correctionCollar'])

        # Medium
        try:
            if kwargs['initial']['objectiveSettings'].getMedium() is not None:
                self.fields['medium'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['mediums'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            kwargs['initial']['objectiveSettings'].id,
                            'medium', options=True)}),
                    initial=kwargs['initial'][
                        'objectiveSettings'].getMedium().value,
                    required=False)
            else:
                self.fields['medium'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['mediums'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            kwargs['initial']['objectiveSettings'].id,
                            'medium', options=True)}),
                    required=False)
            set_widget_attrs(self.fields['medium'])
        except:
            self.fields['medium'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['medium'])

        # Refractive Index
        try:
            if (kwargs['initial']['objectiveSettings'].refractiveIndex is not
                    None):
                self.fields['refractiveIndex'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['objectiveSettings'].id,
                            'refractiveIndex')}),
                    initial=kwargs['initial'][
                        'objectiveSettings'].refractiveIndex,
                    label="Refractive index",
                    required=False)
            else:
                self.fields['refractiveIndex'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['objectiveSettings'].id,
                            'refractiveIndex')}),
                    label="Refractive index",
                    required=False)
            set_widget_attrs(self.fields['refractiveIndex'])
        except:
            self.fields['refractiveIndex'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Refractive index",
                required=False)
            set_widget_attrs(self.fields['refractiveIndex'])

        self.fields.keyOrder = [
            'model', 'manufacturer', 'serialNumber', 'lotNumber',
            'nominalMagnification', 'calibratedMagnification', 'lensNA',
            'immersion', 'correction', 'workingDistance', 'iris',
            'correctionCollar',  'medium', 'refractiveIndex']


class MetadataFilterForm(forms.Form):

    def __init__(self, *args, **kwargs):
        super(MetadataFilterForm, self).__init__(*args, **kwargs)

        # Filter

        # Manufacturer
        try:
            if kwargs['initial']['filter'].manufacturer is not None:
                self.fields['manufacturer'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['filter'].id, 'manufacturer')}),
                    initial=kwargs['initial']['filter'].manufacturer,
                    required=False)
            else:
                self.fields['manufacturer'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['filter'].id, 'manufacturer')}),
                    required=False)
            set_widget_attrs(self.fields['manufacturer'])
        except:
            self.fields['manufacturer'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['manufacturer'])

        # Model
        try:
            if kwargs['initial']['filter'].model is not None:
                self.fields['model'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['filter'].id, 'model')}),
                    initial=kwargs['initial']['filter'].model,
                    required=False)
            else:
                self.fields['model'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['filter'].id, 'model')}),
                    required=False)
            set_widget_attrs(self.fields['model'])
        except:
            self.fields['model'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['model'])

        # Serial Number
        try:
            if kwargs['initial']['filter'].serialNumber is not None:
                self.fields['serialNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['filter'].id, 'serialNumber')}),
                    initial=kwargs['initial']['filter'].serialNumber,
                    label="Serial number",
                    required=False)
            else:
                self.fields['serialNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['filter'].id, 'serialNumber')}),
                    label="Serial number", required=False)
            set_widget_attrs(self.fields['serialNumber'])
        except:
            self.fields['serialNumber'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A", label="Serial number",
                required=False)
            set_widget_attrs(self.fields['serialNumber'])

        # Lot number
        try:
            if kwargs['initial']['filter'].lotNumber is not None:
                self.fields['lotNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['filter'].id, 'lotNumber')}),
                    initial=kwargs['initial']['filter'].lotNumber,
                    label="Lot number",
                    required=False)
            else:
                self.fields['lotNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['filter'].id, 'lotNumber')}),
                    label="Lot number",
                    required=False)
            set_widget_attrs(self.fields['lotNumber'])
        except:
            self.fields['lotNumber'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Lot number",
                required=False)
            set_widget_attrs(self.fields['lotNumber'])

        # Filter wheel
        try:
            if kwargs['initial']['filter'].filterWheel is not None:
                self.fields['filterWheel'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['filter'].id, 'filterWheel')}),
                    initial=kwargs['initial']['filter'].filterWheel,
                    label="Filter wheel",
                    required=False)
            else:
                self.fields['filterWheel'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['filter'].id, 'filterWheel')}),
                    label="Filter wheel",
                    required=False)
            set_widget_attrs(self.fields['filterWheel'])
        except:
            self.fields['filterWheel'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Filter wheel",
                required=False)
            set_widget_attrs(self.fields['filterWheel'])

        # Type
        try:
            if kwargs['initial']['filter'].getFilterType() is not None:
                self.fields['type'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['types'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            kwargs['initial']['filter'].id, 'type',
                            options=True)}),
                    initial=kwargs['initial']['filter'].getFilterType().value,
                    required=False)
            else:
                self.fields['type'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['types'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            kwargs['initial']['filter'].id, 'type',
                            options=True)}),
                    required=False)
            set_widget_attrs(self.fields['type'])
        except:
            self.fields['type'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['type'])

        # Cut in
        tr = kwargs['initial']['filter'].getTransmittanceRange()
        try:
            if tr is not None and tr.cutIn is not None:
                self.fields['cutIn'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['filter'].id, 'cutIn')}),
                    initial=kwargs['initial'][
                        'filter'].getTransmittanceRange().cutIn.getValue(),
                    label="Cut in (%s)" % tr.cutIn.getSymbol(),
                    required=False)
            else:
                self.fields['cutIn'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['filter'].id, 'cutIn')}),
                    label="Cut in",
                    required=False)
            set_widget_attrs(self.fields['cutIn'])
        except:
            self.fields['cutIn'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Cut in",
                required=False)
            set_widget_attrs(self.fields['cutIn'])

        # Cut out
        try:
            if tr is not None and tr.cutOut is not None:
                self.fields['cutOut'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['filter'].id, 'cutOut')}),
                    initial=tr.cutOut.getValue(),
                    label="Cut out (%s)" % tr.cutOut.getSymbol(),
                    required=False)
            else:
                self.fields['cutOut'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['filter'].id, 'cutOut')}),
                    label="Cut out",
                    required=False)
            set_widget_attrs(self.fields['cutOut'])
        except:
            self.fields['cutOut'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Cut out",
                required=False)
            set_widget_attrs(self.fields['cutOut'])

        # Cut in tolerance
        try:
            if tr is not None and tr.cutInTolerance is not None:
                self.fields['cutInTolerance'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['filter'].id,
                            'cutInTolerance')}),
                    initial=tr.cutInTolerance.getValue(),
                    label=("Cut in tolerance (%s)"
                           % tr.cutInTolerance.getSymbol()),
                    required=False)
            else:
                self.fields['cutInTolerance'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['filter'].id,
                            'cutInTolerance')}),
                    label="Cut in tolerance",
                    required=False)
            set_widget_attrs(self.fields['cutInTolerance'])
        except:
            self.fields['cutInTolerance'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Cut in tolerance",
                required=False)
            set_widget_attrs(self.fields['cutInTolerance'])

        # Cut on tolerance
        try:
            if tr is not None and tr.cutOutTolerance is not None:
                self.fields['cutOutTolerance'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['filter'].id, 'cutOut')}),
                    initial=tr.cutOutTolerance.getValue(),
                    label=("Cut out tolerance (%s)"
                           % tr.cutOutTolerance.getSymbol()),
                    required=False)
            else:
                self.fields['cutOutTolerance'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['filter'].id, 'cutOut')}),
                    label="Cut out tolerance",
                    required=False)
            set_widget_attrs(self.fields['cutOutTolerance'])
        except:
            self.fields['cutOutTolerance'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Cut out tolerance",
                required=False)
            set_widget_attrs(self.fields['cutOutTolerance'])

        # Transmittance
        try:
            if kwargs['initial']['filter'].transmittanceRange is not None:
                self.fields['transmittance'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['filter'].id,
                            'transmittance')}),
                    initial=formatPercentFraction(
                        kwargs['initial'][
                            'filter'].getTransmittanceRange().transmittance),
                    label="Transmittance (%)",
                    required=False)
            else:
                self.fields['transmittance'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['filter'].id,
                            'transmittance')}),
                    required=False)
            set_widget_attrs(self.fields['transmittance'])
        except:
            self.fields['transmittance'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['transmittance'])

        self.fields.keyOrder = [
            'model', 'manufacturer', 'serialNumber', 'lotNumber', 'type',
            'filterWheel', 'cutIn', 'cutOut', 'cutInTolerance',
            'cutOutTolerance', 'transmittance']


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
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            detector.id, 'manufacturer')}),
                    initial=detector.manufacturer,
                    required=False)
            else:
                self.fields['manufacturer'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            detector.id, 'manufacturer')}),
                    required=False)
            set_widget_attrs(self.fields['manufacturer'])
        except:
            self.fields['manufacturer'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['manufacturer'])

        # Model
        try:
            if detector is not None:
                self.fields['model'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(detector.id, 'model')}),
                    initial=detector.model,
                    required=False)
            else:
                self.fields['model'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(detector.id, 'model')}),
                    required=False)
            set_widget_attrs(self.fields['model'])
        except:
            self.fields['model'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['model'])

        # SN
        try:
            if detector is not None:
                self.fields['serialNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            detector.id, 'serialNumber')}),
                    initial=detector.serialNumber,
                    required=False)
            else:
                self.fields['serialNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            detector.id, 'serialNumber')}),
                    required=False)
            set_widget_attrs(self.fields['serialNumber'])
        except:
            self.fields['serialNumber'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['serialNumber'])

        # Lot number (NB. Untill OMERO model is updated in 4.3, this will
        # throw since lotNumber is not yet supported)
        try:
            if detector is not None:
                self.fields['lotNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(detector.id, 'lotNumber')}),
                    initial=detector.lotNumber,
                    required=False)
            else:
                self.fields['lotNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(detector.id, 'lotNumber')}),
                    required=False)
            set_widget_attrs(self.fields['lotNumber'])
        except:
            self.fields['lotNumber'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['lotNumber'])

        # Type
        try:
            if detector.getDetectorType() is not None:
                self.fields['type'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['types'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            detector.id, 'type', options=True)}),
                    initial=detector.getDetectorType().value,
                    required=False)
            else:
                self.fields['type'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['types'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            detector.id, 'type', options=True)}),
                    required=False)
            set_widget_attrs(self.fields['type'])
        except:
            self.fields['type'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['type'])

        # Gain
        try:
            if detSet is not None:
                self.fields['gain'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(detSet.id, 'gain')}),
                    initial=detSet.gain,
                    required=False)
            elif detector is not None:
                self.fields['gain'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(detector.id, 'gain')}),
                    initial=detector.gain,
                    required=False)
            else:
                self.fields['gain'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(detSet.id, 'gain')}),
                    required=False)
            set_widget_attrs(self.fields['gain'])
        except:
            self.fields['gain'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['gain'])

        # Voltage
        try:
            if detSet is not None and detSet.voltage is not None:
                self.fields['voltage'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(detSet.id, 'voltage')}),
                    initial=detSet.voltage.getValue(),
                    label="Voltage (%s)" % detSet.voltage.getSymbol(),
                    required=False)
            elif detector is not None:
                self.fields['voltage'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(detector.id, 'voltage')}),
                    initial=detector.voltage.getValue(),
                    label="Voltage (%s)" % detector.voltage.getSymbol(),
                    required=False)
            else:
                self.fields['voltage'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(detSet.id, 'voltage')}),
                    required=False)
            set_widget_attrs(self.fields['voltage'])
        except:
            self.fields['voltage'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['voltage'])

        # Offset
        try:
            if detSet is not None:
                self.fields['offsetValue'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(detSet.id, 'offsetValue')}),
                    initial=detSet.offsetValue,
                    label="Offset",
                    required=False)
            elif detector is not None:
                self.fields['offsetValue'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            detector.id, 'offsetValue')}),
                    initial=detector.offsetValue,
                    label="Offset",
                    required=False)
            else:
                self.fields['offsetValue'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            detector.id, 'offsetValue')}),
                    label="Offset",
                    required=False)
            set_widget_attrs(self.fields['offsetValue'])
        except:
            self.fields['offsetValue'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Offset",
                required=False)
            set_widget_attrs(self.fields['offsetValue'])

        # Zoom
        try:
            if detector is not None:
                self.fields['zoom'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            detector.id, 'voltage')}),
                    initial=detector.zoom,
                    required=False)
            else:
                self.fields['zoom'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            detector.id, 'voltage')}),
                    required=False)
            set_widget_attrs(self.fields['zoom'])
        except:
            self.fields['zoom'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['zoom'])

        # Amplification gain
        try:
            if detector is not None:
                self.fields['amplificationGain'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            detector.id, 'amplificationGain')}),
                    initial=detector.amplificationGain,
                    label="Amplification gain",
                    required=False)
            else:
                self.fields['amplificationGain'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            detector.id, 'amplificationGain')}),
                    label="Amplification gain",
                    required=False)
            set_widget_attrs(self.fields['amplificationGain'])
        except:
            self.fields['amplificationGain'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Amplification gain",
                required=False)
            set_widget_attrs(self.fields['amplificationGain'])

        # Read out rate
        try:
            if detSet is not None and detSet.readOutRate is not None:
                self.fields['readOutRate'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(detSet.id, 'readOutRate')}),
                    initial=detSet.readOutRate.getValue(),
                    label=("Read out rate (%s)"
                           % detSet.readOutRate.getSymbol()),
                    required=False)
            else:
                self.fields['readOutRate'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(detSet.id, 'readOutRate')}),
                    label="Read out rate",
                    required=False)
            set_widget_attrs(self.fields['readOutRate'])
        except:
            self.fields['readOutRate'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Read out rate",
                required=False)
            set_widget_attrs(self.fields['readOutRate'])

        # Binning
        try:
            if detSet is not None:
                self.fields['binning'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['binnings'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            detSet.id, 'type', options=True)}),
                    initial=detSet.getBinning().value,
                    required=False)
            else:
                self.fields['binning'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['binnings'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            detSet.id, 'type', options=True)}),
                    required=False)
            set_widget_attrs(self.fields['binning'])
        except:
            self.fields['binning'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['binning'])

        self.fields.keyOrder = [
            'model', 'manufacturer', 'serialNumber', 'lotNumber', 'type',
            'gain', 'voltage', 'offsetValue', 'zoom', 'amplificationGain',
            'readOutRate', 'binning']


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
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(lightSource.id, 'model')}),
                    initial=lightSource.manufacturer,
                    required=False)
            else:
                self.fields['manufacturer'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(lightSource.id, 'model')}),
                    required=False)
            set_widget_attrs(self.fields['manufacturer'])
        except:
            self.fields['manufacturer'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['manufacturer'])

        # Model
        try:
            if lightSource.model is not None:
                self.fields['model'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(lightSource.id, 'model')}),
                    initial=lightSource.model,
                    required=False)
            else:
                self.fields['model'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(lightSource.id, 'model')}),
                    required=False)
            set_widget_attrs(self.fields['model'])
        except:
            self.fields['model'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['model'])

        # Serial Number
        try:
            if lightSource.serialNumber is not None:
                self.fields['serialNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            lightSource.id, 'serialNumber')}),
                    initial=lightSource.serialNumber,
                    label="Serial number",
                    required=False)
            else:
                self.fields['serialNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            lightSource.id, 'serialNumber')}),
                    label="Serial number",
                    required=False)
            set_widget_attrs(self.fields['serialNumber'])
        except:
            self.fields['serialNumber'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Serial number",
                required=False)
            set_widget_attrs(self.fields['serialNumber'])

        # Lot Number
        try:
            if lightSource.lotNumber is not None:
                self.fields['lotNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            lightSource.id, 'lotNumber')}),
                    initial=lightSource.lotNumber,
                    label="Lot number",
                    required=False)
            else:
                self.fields['lotNumber'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            lightSource.id, 'lotNumber')}),
                    label="Lot number",
                    required=False)
            set_widget_attrs(self.fields['lotNumber'])
        except:
            self.fields['lotNumber'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Lot number",
                required=False)
            set_widget_attrs(self.fields['lotNumber'])

        # Power
        try:
            if lightSource.power is not None:
                self.fields['power'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(lightSource.id, 'power')}),
                    initial=lightSource.power.getValue(),
                    label="Power (%s)" % lightSource.power.getSymbol(),
                    required=False)
            else:
                self.fields['power'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(lightSource.id, 'power')}),
                    required=False)
            set_widget_attrs(self.fields['power'])
        except:
            self.fields['power'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['power'])

        # Type
        try:
            if lightSource.getLightSourceType() is not None:
                self.fields['lstype'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['lstypes'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            lightSource.id, 'type', options=True)}),
                    label="Type",
                    initial=lightSource.getLightSourceType().value,
                    required=False)
            else:
                self.fields['lstype'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['lstypes'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            lightSource.id, 'type', options=True)}),
                    label="Type",
                    required=False)
            set_widget_attrs(self.fields['lstype'])
        except:
            self.fields['lstype'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Type",
                required=False)
            set_widget_attrs(self.fields['lstype'])

        # Pump (laser only)
        try:
            # Will throw exception for non-Laser lightsources.
            pump = lightSource.getPump()
            pumpType = pump.OMERO_CLASS     # E.g. 'Arc'
            pumpModel = pump.getModel()
            pumpValue = "%s: %s" % (pumpType, pumpModel)
            self.fields['pump'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial=pumpValue,
                required=False)
        except:
            # Not a Laser - don't show Pump
            self.fields['pump'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
        set_widget_attrs(self.fields['pump'])

        # Medium
        try:
            if lightSource.getLaserMedium() is not None:
                self.fields['lmedium'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['mediums'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            lightSource.id, 'medium', options=True)}),
                    initial=lightSource.getLaserMedium().value,
                    label="Medium",
                    required=False)
            else:
                self.fields['lmedium'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['mediums'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            lightSource.id, 'medium', options=True)}),
                    label="Medium",
                    required=False)
            set_widget_attrs(self.fields['lmedium'])
        except:
            self.fields['lmedium'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Medium",
                required=False)
            set_widget_attrs(self.fields['lmedium'])

        # Wavelength
        try:
            if (lightSourceSettings is not None and
                    lightSourceSettings.wavelength is not None):
                self.fields['wavelength'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            lightSource.id, 'wavelength')}),
                    initial=lightSourceSettings.wavelength.getValue(),
                    label=("Wavelength (%s)"
                           % lightSourceSettings.wavelength.getSymbol()),
                    required=False)
            elif lightSource.wavelength is not None:
                self.fields['wavelength'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            lightSource.id, 'wavelength')}),
                    initial=lightSource.wavelength.getValue(),
                    label=("Wavelength (%s)"
                           % lightSource.wavelength.getSymbol()),
                    required=False)
            else:
                self.fields['wavelength'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            lightSource.id, 'wavelength')}),
                    required=False)
            set_widget_attrs(self.fields['wavelength'])
        except:
            self.fields['wavelength'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['wavelength'])

        # FrequencyMultiplication
        try:
            if lightSource.frequencyMultiplication is not None:
                self.fields['frequencyMultiplication'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            lightSource.id, 'frequencyMultiplication')}),
                    initial=lightSource.frequencyMultiplication,
                    label="Frequency Multiplication",
                    required=False)
            else:
                self.fields['frequencyMultiplication'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            lightSource.id, 'frequencyMultiplication')}),
                    label="Frequency Multiplication",
                    required=False)
            set_widget_attrs(self.fields['frequencyMultiplication'])
        except:
            self.fields['frequencyMultiplication'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Frequency Multiplication",
                required=False)
            set_widget_attrs(self.fields['frequencyMultiplication'])

        # Tuneable
        try:
            if lightSource.tuneable is not None:
                self.fields['tuneable'] = forms.ChoiceField(
                    choices=self.BOOLEAN_CHOICES,
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            lightSource.id, 'tuneable', options=True)}),
                    initial=lightSource.tuneable,
                    required=False)
            else:
                self.fields['tuneable'] = forms.ChoiceField(
                    choices=self.BOOLEAN_CHOICES,
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            lightSource.id, 'tuneable', options=True)}),
                    required=False)
            set_widget_attrs(self.fields['tuneable'])
        except:
            self.fields['tuneable'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['tuneable'])

        # Pulse
        try:
            if lightSource.pulse is not None:
                self.fields['pulse'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['pulses'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            lightSource.id, 'pulse', options=True)}),
                    initial=lightSource.pulse,
                    required=False)
            else:
                self.fields['pulse'] = MetadataModelChoiceField(
                    queryset=kwargs['initial']['pulses'],
                    empty_label=u"Not set",
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            lightSource.id, 'pulse', options=True)}),
                    required=False)
            set_widget_attrs(self.fields['pulse'])
        except:
            self.fields['pulse'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['pulse'])

        # Repetition Rate
        try:
            if lightSource.repetitionRate is not None:
                self.fields['repetitionRate'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            lightSource.id, 'repetitionRate')}),
                    initial=lightSource.repetitionRate.getValue(),
                    label=("Repetition rate (%s)"
                           % lightSource.repetitionRate.getSymbol()),
                    required=False)
            else:
                self.fields['repetitionRate'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            lightSource.id, 'repetitionRate')}),
                    label="Repetition rate",
                    required=False)
            set_widget_attrs(self.fields['repetitionRate'])
        except:
            self.fields['repetitionRate'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Repetition rate",
                required=False)
            set_widget_attrs(self.fields['repetitionRate'])

        # Pockel Cell
        try:
            if lightSource.pockelCell is not None:
                self.fields['pockelCell'] = forms.ChoiceField(
                    choices=self.BOOLEAN_CHOICES,
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            lightSource.id, 'pockelCell', options=True)}),
                    initial=lightSource.pockelCell,
                    label="Pockel Cell",
                    required=False)
            else:
                self.fields['pockelCell'] = forms.ChoiceField(
                    choices=self.BOOLEAN_CHOICES,
                    widget=forms.Select(attrs={
                        'onchange': save_metadata(
                            lightSource.id, 'pockelCell', options=True)}),
                    label="Pockel Cell",
                    required=False)
            set_widget_attrs(self.fields['pockelCell'])
        except:
            self.fields['pockelCell'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Pockel Cell",
                required=False)
            set_widget_attrs(self.fields['pockelCell'])

        # Attenuation
        if (lightSourceSettings is not None and
                lightSourceSettings.attenuation is not None):
            self.fields['attenuation'] = forms.CharField(
                max_length=100,
                widget=forms.TextInput(attrs={
                    'size': 25,
                    'onchange': save_metadata(
                        lightSourceSettings.id, 'attenuation')}),
                initial=formatPercentFraction(
                    lightSourceSettings.attenuation),
                label="Attenuation (%)",
                required=False)
        else:
            self.fields['attenuation'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
        set_widget_attrs(self.fields['attenuation'])

        self.fields.keyOrder = [
            'model', 'manufacturer', 'serialNumber', 'lotNumber', 'power',
            'lstype', 'pump', 'lmedium', 'wavelength',
            'frequencyMultiplication', 'tuneable', 'pulse', 'repetitionRate',
            'pockelCell', 'attenuation']


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
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['image'].id, 'temperature')}),
                    initial=imagingEnv.temperature.getValue(),
                    label=("Temperature (%s)"
                           % imagingEnv.temperature.getSymbol()),
                    required=False)
            else:
                self.fields['temperature'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['image'].id, 'temperature')}),
                    required=False)
            set_widget_attrs(self.fields['temperature'])
        except:
            self.fields['temperature'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['temperature'])

        # Air Pressure
        try:
            if imagingEnv.airPressure is not None:
                self.fields['airPressure'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['image'].id, 'airPressure')}),
                    initial=imagingEnv.airPressure.getValue(),
                    label=("Air Pressure (%s)"
                           % imagingEnv.airPressure.getSymbol()),
                    required=False)
            else:
                self.fields['airPressure'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['image'].id, 'airPressure')}),
                    label="Air Pressure",
                    required=False)
            set_widget_attrs(self.fields['airPressure'])
        except:
            self.fields['airPressure'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                label="Air Pressure",
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['airPressure'])

        # Humidity
        try:
            if imagingEnv.humidity is not None:
                self.fields['humidity'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['image'].id, 'humidity')}),
                    initial=imagingEnv.humidity,
                    required=False)
            else:
                self.fields['humidity'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['image'].id, 'humidity')}),
                    required=False)
            set_widget_attrs(self.fields['humidity'])
        except:
            self.fields['humidity'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                required=False)
            set_widget_attrs(self.fields['humidity'])

        # CO2 percent
        try:
            if imagingEnv.co2percent is not None:
                self.fields['co2percent'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['image'].id, 'co2percent')}),
                    initial=imagingEnv.co2percent,
                    label="CO2 (%)",
                    required=False)
            else:
                self.fields['co2percent'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['image'].id, 'co2percent')}),
                    label="CO2 (%)",
                    required=False)
            set_widget_attrs(self.fields['co2percent'])
        except:
            self.fields['co2percent'] = forms.CharField(
                max_length=10,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="CO2 (%)",
                required=False)
            set_widget_attrs(self.fields['co2percent'])

        self.fields.keyOrder = [
            'airPressure', 'co2percent', 'humidity', 'temperature']


class MetadataStageLabelForm(forms.Form):

    def __init__(self, *args, **kwargs):
        super(MetadataStageLabelForm, self).__init__(*args, **kwargs)

        # Stage label

        # Position x
        try:
            if kwargs['initial']['image'].getStageLabel() is not None:
                self.fields['positionx'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['image'].id, 'positionx')}),
                    initial=kwargs['initial'][
                        'image'].getStageLabel().positionx,
                    label="Position X",
                    required=False)
            else:
                self.fields['positionx'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['image'].id, 'positionx')}),
                    label="Position X",
                    required=False)
            set_widget_attrs(self.fields['positionx'])
        except:
            self.fields['positionx'] = forms.CharField(
                max_length=100,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Position X",
                required=False)
            set_widget_attrs(self.fields['positionx'])

        # Position y
        try:
            if kwargs['initial']['image'].getStageLabel() is not None:
                self.fields['positiony'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['image'].id, 'positiony')}),
                    initial=kwargs['initial'][
                        'image'].getStageLabel().positiony,
                    label="Position Y",
                    required=False)
            else:
                self.fields['positiony'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['image'].id, 'positiony')}),
                    label="Position Y",
                    required=False)
            set_widget_attrs(self.fields['positiony'])
        except:
            self.fields['positiony'] = forms.CharField(
                max_length=100,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A",
                label="Position Y",
                required=False)
            set_widget_attrs(self.fields['positionx'])

        # Position z
        try:
            if kwargs['initial']['image'].getStageLabel() is not None:
                self.fields['positionz'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['image'].id, 'positionz')}),
                    initial=kwargs['initial'][
                        'image'].getStageLabel().positionz,
                    label="Position Z",
                    required=False)
            else:
                self.fields['positionz'] = forms.CharField(
                    max_length=100,
                    widget=forms.TextInput(attrs={
                        'size': 25,
                        'onchange': save_metadata(
                            kwargs['initial']['image'].id, 'positionz')}),
                    label="Position Z",
                    required=False)
            set_widget_attrs(self.fields['positionz'])
        except:
            self.fields['positionz'] = forms.CharField(
                max_length=100,
                widget=forms.TextInput(attrs={'size': 25}),
                initial="N/A", label="Position Z", required=False)
            set_widget_attrs(self.fields['positionz'])

        self.fields.keyOrder = ['positionx', 'positiony', 'positionz']
