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

from django.conf import settings
from django import forms
from django.forms import ModelForm
from django.forms.widgets import Textarea
from django.forms.widgets import HiddenInput

from omeroweb.webadmin.custom_models import Server

from omeroweb.custom_forms import NonASCIIForm

from custom_forms import ServerModelChoiceField, \
        GroupModelChoiceField, GroupModelMultipleChoiceField, \
        ExperimenterModelChoiceField, ExperimenterModelMultipleChoiceField, \
        DefaultGroupField, OmeNameField
from custom_widgets import DefaultGroupRadioSelect


#################################################################
# Non-model Form

class LoginForm(NonASCIIForm):
    
    def __init__(self, *args, **kwargs):
        super(LoginForm, self).__init__(*args, **kwargs)
        try:
            if len(Server) > 1:
                self.fields['server'] = ServerModelChoiceField(Server, empty_label=u"---------")
            else:
                self.fields['server'] = ServerModelChoiceField(Server, empty_label=None)
        except:
            self.fields['server'] = ServerModelChoiceField(Server, empty_label=u"---------")
        
        self.fields.keyOrder = ['server', 'username', 'password', 'ssl']
            
    username = forms.CharField(max_length=50, widget=forms.TextInput(attrs={'size':22}))
    password = forms.CharField(max_length=50, widget=forms.PasswordInput(attrs={'size':22, 'autocomplete': 'off'}))
    ssl = forms.BooleanField(required=False, help_text='<img src="%scommon/image/nuvola_encrypted_grey16.png" title="Real-time encrypted data transfer can be turned on by checking the box, but it will slow down the data access. Turning it off does not affect the connection to the server which is always secure." alt="SSL"' % settings.STATIC_URL)

class ForgottonPasswordForm(NonASCIIForm):
    
    server = ServerModelChoiceField(Server, empty_label=u"---------")
    username = forms.CharField(max_length=50, widget=forms.TextInput(attrs={'size':28, 'autocomplete': 'off'}))
    email = forms.EmailField(widget=forms.TextInput(attrs={'size':28, 'autocomplete': 'off'}))

class ExperimenterForm(NonASCIIForm):

    def __init__(self, name_check=False, email_check=False, *args, **kwargs):
        super(ExperimenterForm, self).__init__(*args, **kwargs)
        self.name_check=name_check
        self.email_check=email_check 
        
        try:
            self.fields['default_group'] = DefaultGroupField(choices=kwargs['initial']['default'], widget=DefaultGroupRadioSelect(), required=True, label="Groups")
            self.fields['other_groups'] = GroupModelMultipleChoiceField(queryset=kwargs['initial']['others'], initial=kwargs['initial']['others'], required=False, widget=forms.SelectMultiple(attrs={'size':10}))
        except:
            self.fields['default_group'] = forms.ChoiceField(choices=list(), widget=forms.RadioSelect(), required=True, label="Groups")
            self.fields['other_groups'] = GroupModelMultipleChoiceField(queryset=list(), required=False, widget=forms.SelectMultiple(attrs={'size':10}))
        
        self.fields['available_groups'] = GroupModelMultipleChoiceField(queryset=kwargs['initial']['available'], required=False, widget=forms.SelectMultiple(attrs={'size':10}))
        
        if kwargs['initial'].has_key('with_password') and kwargs['initial']['with_password']:
            self.fields['password'] = forms.CharField(max_length=50, widget=forms.PasswordInput(attrs={'size':30, 'autocomplete': 'off'}))
            self.fields['confirmation'] = forms.CharField(max_length=50, widget=forms.PasswordInput(attrs={'size':30, 'autocomplete': 'off'}))
            
            self.fields.keyOrder = ['omename', 'first_name', 'middle_name', 'last_name', 'email', 'institution', 'administrator', 'active', 'password', 'confirmation', 'default_group', 'other_groups', 'available_groups']
        else:
            self.fields.keyOrder = ['omename', 'first_name', 'middle_name', 'last_name', 'email', 'institution', 'administrator', 'active', 'default_group', 'other_groups', 'available_groups']

    omename = OmeNameField(max_length=250, widget=forms.TextInput(attrs={'size':30, 'autocomplete': 'off'}), label="Username")
    first_name = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':30, 'autocomplete': 'off'}))
    middle_name = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':30, 'autocomplete': 'off'}), required=False)
    last_name = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':30, 'autocomplete': 'off'}))
    email = forms.EmailField(widget=forms.TextInput(attrs={'size':30, 'autocomplete': 'off'}), required=False)
    institution = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':30, 'autocomplete': 'off'}), required=False)
    administrator = forms.CharField(widget=forms.CheckboxInput(), required=False)
    active = forms.CharField(widget=forms.CheckboxInput(), required=False)
    
    def clean_confirmation(self):
        if self.cleaned_data.get('password') or self.cleaned_data.get('confirmation'):
            if len(self.cleaned_data.get('password')) < 3:
                raise forms.ValidationError('Password must be at least 3 letters long')
            if self.cleaned_data.get('password') != self.cleaned_data.get('confirmation'):
                raise forms.ValidationError('Passwords do not match')
            else:
                return self.cleaned_data.get('password')
                
    def clean_omename(self):
        if self.name_check:
            raise forms.ValidationError('This username already exist.')
        return self.cleaned_data.get('omename')

    def clean_email(self):
        if self.email_check:
            raise forms.ValidationError('This email already exist.')
        return self.cleaned_data.get('email')

class GroupForm(NonASCIIForm):
    
    PERMISSION_CHOICES = (
        ('0', 'Private'),
        ('1', 'Collaborative '),
        #('2', 'Public ')
    )
    
    def __init__(self, name_check=False, *args, **kwargs):
        super(GroupForm, self).__init__(*args, **kwargs)
        self.name_check=name_check
        help_text = None
        try:
            if kwargs['initial']['owners']: pass
            self.fields['owners'] = ExperimenterModelMultipleChoiceField(queryset=kwargs['initial']['experimenters'], initial=kwargs['initial']['owners'], required=False)
            help_text="<div class=\"error\">WARNING: Changing Permissions will change permissions of all objects in a group. This will take some time for large groups and could break the server.</div>"
        except:
            self.fields['owners'] = ExperimenterModelMultipleChoiceField(queryset=kwargs['initial']['experimenters'], required=False)
        
        self.fields['permissions'] = forms.ChoiceField(choices=self.PERMISSION_CHOICES, widget=forms.RadioSelect(), required=True, label="Permissions", help_text=help_text)
        
        self.fields.keyOrder = ['name', 'description', 'owners', 'permissions', 'readonly']

    name = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'autocomplete': 'off'}))
    description = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':25, 'autocomplete': 'off'}), required=False)    
    readonly = forms.BooleanField(required=False, label="(read-only)")  
    
    def clean_name(self):
        if self.name_check:
            raise forms.ValidationError('This name already exist.')
        return self.cleaned_data.get('name')

class GroupOwnerForm(forms.Form):
    
    PERMISSION_CHOICES = (
        ('0', 'Private'),
        ('1', 'Collaborative '),
        #('2', 'Public ')
    )

    permissions = forms.ChoiceField(choices=PERMISSION_CHOICES, widget=forms.RadioSelect(), required=True, label="Permissions", help_text="<div class=\"error\">WARNING: Changing Permissions will change permissions of all objects in a group. This will take some time for large groups and could break the server.</div>")
    readonly = forms.BooleanField(required=False, label="(read-only)")  

class MyAccountForm(NonASCIIForm):
        
    def __init__(self, email_check=False, *args, **kwargs):
        super(MyAccountForm, self).__init__(*args, **kwargs)
        self.email_check=email_check
        try:
            if kwargs['initial']['default_group']: pass
            self.fields['default_group'] = GroupModelChoiceField(queryset=kwargs['initial']['groups'], initial=kwargs['initial']['default_group'], empty_label=None)
        except:
            self.fields['default_group'] = GroupModelChoiceField(queryset=kwargs['initial']['groups'], empty_label=None)
        self.fields.keyOrder = ['omename', 'first_name', 'middle_name', 'last_name', 'email', 'institution', 'default_group']

    omename = forms.CharField(max_length=50, widget=forms.TextInput(attrs={'onfocus':'this.blur()', 'size':30, 'autocomplete': 'off'}), label="Username")
    first_name = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':30, 'autocomplete': 'off'}))
    middle_name = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':30, 'autocomplete': 'off'}), required=False)
    last_name = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':30, 'autocomplete': 'off'}))
    email = forms.EmailField(widget=forms.TextInput(attrs={'size':30, 'autocomplete': 'off'}), required=False)
    institution = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':30, 'autocomplete': 'off'}), required=False)

    def clean_email(self):
        if self.email_check:
            raise forms.ValidationError('This email already exist.')
        return self.cleaned_data.get('email')


class ContainedExperimentersForm(NonASCIIForm):

    def __init__(self, *args, **kwargs):
        super(ContainedExperimentersForm, self).__init__(*args, **kwargs)
        self.fields['members'] = ExperimenterModelMultipleChoiceField(queryset=kwargs['initial']['members'], required=False, widget=forms.SelectMultiple(attrs={'size':25}))
        self.fields['available'] = ExperimenterModelMultipleChoiceField(queryset=kwargs['initial']['available'], required=False, widget=forms.SelectMultiple(attrs={'size':25}))
        self.fields.keyOrder = ['members', 'available']


class UploadPhotoForm(forms.Form):
    
    photo = forms.FileField(required=False)

    def clean_photo(self):
        if self.cleaned_data.get('photo') is None:
            raise forms.ValidationError('No image selected. Supported image formats (file extensions allowed): jpeg, jpg, gif, png. The maximum image size allowed is 200KB.')
        if not self.cleaned_data.get('photo').content_type.startswith("image"):
            raise forms.ValidationError('Supported image formats (file extensions allowed): jpeg, jpg, gif, png.')
        if self.cleaned_data.get('photo').size > 204800:
            raise forms.ValidationError('The maximum image size allowed is 200KB.')
        return self.cleaned_data.get('photo') 

class ChangePassword(NonASCIIForm):

    password = forms.CharField(max_length=50, widget=forms.PasswordInput(attrs={'size':30, 'autocomplete': 'off'}), label="New password")
    confirmation = forms.CharField(max_length=50, widget=forms.PasswordInput(attrs={'size':30, 'autocomplete': 'off'}), label="Confirm new password")
    old_password = forms.CharField(max_length=50, widget=forms.PasswordInput(attrs={'size':30, 'autocomplete': 'off'}), label="Your current password")
    
    def clean_confirmation(self):
        if self.cleaned_data.get('password') or self.cleaned_data.get('confirmation'):
            if len(self.cleaned_data.get('password')) < 3:
                raise forms.ValidationError('Password must be at least 3 letters long')
            if self.cleaned_data.get('password') != self.cleaned_data.get('confirmation'):
                raise forms.ValidationError('Passwords do not match')
            else:
                return self.cleaned_data.get('password')    
    
class EnumerationEntry(NonASCIIForm):
    
    new_entry = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':30}))


class EnumerationEntries(NonASCIIForm):
    
    def __init__(self, entries, *args, **kwargs):
        super(EnumerationEntries, self).__init__(*args, **kwargs)        
        for i,e in enumerate(entries):
            try:
                if kwargs['initial']['entries']:
                    self.fields[str(e.id)] = forms.CharField(max_length=250, initial=e.value, widget=forms.TextInput(attrs={'size':30}), label=i+1)
                else:
                    self.fields[str(e.id)] = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':30}), label=i+1)
            except:
                self.fields[str(e.id)] = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':30}), label=i+1)
                    
        self.fields.keyOrder = [str(k) for k in self.fields.keys()]
    