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
from django.utils.translation import ugettext_lazy as _
from django.utils.html import escape

from omeroweb.connector import Server

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
            if reduce( (lambda x, y : x + 1), Server, 0) > 1:
                self.fields['server'] = ServerModelChoiceField(Server, empty_label=u"---------")
            else:
                self.fields['server'] = ServerModelChoiceField(Server, empty_label=None)
        except:
            self.fields['server'] = ServerModelChoiceField(Server, empty_label=u"---------")
        
        self.fields.keyOrder = ['server', 'username', 'password', 'ssl']
            
    username = forms.CharField(max_length=50, widget=forms.TextInput(attrs={'size':22}), label=_('Username'))
    password = forms.CharField(max_length=50, widget=forms.PasswordInput(attrs={'size':22, 'autocomplete': 'off'}), label=_('Password'))
    ssl = forms.BooleanField(required=False, help_text='<img src="%swebgateway/img/nuvola_encrypted_grey16.png"' % settings.STATIC_URL +
        ' title="' + escape(_('Real-time encrypted data transfer can be turned on by checking the box, but it will slow down the data access. ' +
          'Turning it off does not affect the connection to the server which is always secure.')) +'" alt="SSL"')

class ForgottonPasswordForm(NonASCIIForm):
    
    server = ServerModelChoiceField(Server, empty_label=u"---------")
    username = forms.CharField(max_length=50, widget=forms.TextInput(attrs={'size':28, 'autocomplete': 'off'}), label=_('Username'))
    email = forms.EmailField(widget=forms.TextInput(attrs={'size':28, 'autocomplete': 'off'}), label=_('Email'))

class ExperimenterForm(NonASCIIForm):

    def __init__(self, name_check=False, email_check=False, experimenter_is_me=False, *args, **kwargs):
        super(ExperimenterForm, self).__init__(*args, **kwargs)
        self.name_check=name_check
        self.email_check=email_check 
        
        try:
            self.fields['other_groups'] = GroupModelMultipleChoiceField(queryset=kwargs['initial']['groups'], initial=kwargs['initial']['other_groups'], required=False, label=_("Other groups"))
        except:
            self.fields['other_groups'] = GroupModelMultipleChoiceField(queryset=kwargs['initial']['groups'], required=False, label=_("Other groups"))
        
        try:
            if kwargs['initial']['default_group']: pass
            self.fields['default_group'] = GroupModelChoiceField(queryset=kwargs['initial']['groups'], initial=kwargs['initial']['default_group'], empty_label=u"---------", required=False, label=_("Default group"))
        except:
            self.fields['default_group'] = GroupModelChoiceField(queryset=kwargs['initial']['groups'], empty_label=u"---------", required=False, label=_("Default group"))
        self.fields['default_group'].widget.attrs['class'] = 'hidden'
        
        if kwargs['initial'].has_key('with_password') and kwargs['initial']['with_password']:
            self.fields['password'] = forms.CharField(max_length=50, widget=forms.PasswordInput(attrs={'size':30, 'autocomplete': 'off'}), label=_("Password"))
            self.fields['confirmation'] = forms.CharField(max_length=50, widget=forms.PasswordInput(attrs={'size':30, 'autocomplete': 'off'}), label=_("Confirmation"))
            
            self.fields.keyOrder = ['omename', 'password', 'confirmation', 'first_name', 'middle_name', 'last_name', 'email', 'institution', 'administrator', 'active', 'default_group', 'other_groups']
        else:
            self.fields.keyOrder = ['omename', 'first_name', 'middle_name', 'last_name', 'email', 'institution', 'administrator', 'active', 'default_group', 'other_groups']
        if experimenter_is_me:
            self.fields['administrator'].widget.attrs['disabled'] = True
            self.fields['administrator'].widget.attrs['title'] = _("Removal of your own admin rights would be un-doable")
            self.fields['active'].widget.attrs['disabled'] = True
            self.fields['active'].widget.attrs['title'] = _("You cannot disable yourself")

    omename = OmeNameField(max_length=250, widget=forms.TextInput(attrs={'size':30, 'autocomplete': 'off'}), label=_("Username"))
    first_name = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':30, 'autocomplete': 'off'}), label=_("First name"))
    middle_name = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':30, 'autocomplete': 'off'}), required=False, label=_("Middle name"))
    last_name = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':30, 'autocomplete': 'off'}), label=_("Last name"))
    email = forms.EmailField(widget=forms.TextInput(attrs={'size':30, 'autocomplete': 'off'}), required=False, label=_("Email"))
    institution = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':30, 'autocomplete': 'off'}), required=False, label=_("Institution"))
    administrator = forms.CharField(widget=forms.CheckboxInput(), required=False, label=_("Administrator"))
    active = forms.CharField(widget=forms.CheckboxInput(), required=False, label=_("Active"))
    
    def clean_confirmation(self):
        if self.cleaned_data.get('password') or self.cleaned_data.get('confirmation'):
            if len(self.cleaned_data.get('password')) < 3:
                raise forms.ValidationError(_('Password must be at least 3 characters long.'))
            if self.cleaned_data.get('password') != self.cleaned_data.get('confirmation'):
                raise forms.ValidationError(_('Passwords do not match'))
            else:
                return self.cleaned_data.get('password')
    
    def clean_omename(self):
        if self.name_check:
            raise forms.ValidationError(_('This username already exists.'))
        return self.cleaned_data.get('omename')

    def clean_email(self):
        if self.email_check:
            raise forms.ValidationError(_('This email already exist.'))
        return self.cleaned_data.get('email')
    
    def clean_other_groups(self):
        if self.cleaned_data.get('other_groups') is None or len(self.cleaned_data.get('other_groups')) <= 0:
            raise forms.ValidationError(_('User must be a member of at least one group.'))
        else:
            return self.cleaned_data.get('other_groups')


PERMISSION_CHOICES = (
    ('0', _('Private')),
    ('1', _('Read-Only')),
    ('2', _('Read-Annotate')),
)

class GroupForm(NonASCIIForm):
    
    def __init__(self, name_check=False, *args, **kwargs):
        super(GroupForm, self).__init__(*args, **kwargs)
        self.name_check=name_check
        try:
            if kwargs['initial']['owners']: pass
            self.fields['owners'] = ExperimenterModelMultipleChoiceField(queryset=kwargs['initial']['experimenters'], initial=kwargs['initial']['owners'], required=False, label=_("Owners"))
        except:
            self.fields['owners'] = ExperimenterModelMultipleChoiceField(queryset=kwargs['initial']['experimenters'], required=False, label=_("Owners"))
        
        try:
            if kwargs['initial']['members']: pass
            self.fields['members'] = ExperimenterModelMultipleChoiceField(queryset=kwargs['initial']['experimenters'], initial=kwargs['initial']['members'], required=False, label=_("Members"))
        except:
            self.fields['members'] = ExperimenterModelMultipleChoiceField(queryset=kwargs['initial']['experimenters'], required=False, label=_("Members"))
        
        
        self.fields['permissions'] = forms.ChoiceField(choices=PERMISSION_CHOICES, widget=forms.RadioSelect(), required=True, label=_("Permissions"))
        
        self.fields.keyOrder = ['name', 'description', 'owners', 'members', 'permissions']

    name = forms.CharField(max_length=100, widget=forms.TextInput(attrs={'size':25, 'autocomplete': 'off'}), label=_("Name"))
    description = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':25, 'autocomplete': 'off'}), required=False, label=_("Description")) 
    
    def clean_name(self):
        if self.name_check:
            raise forms.ValidationError(_('This name already exist.'))
        return self.cleaned_data.get('name')

class GroupOwnerForm(forms.Form):

    def __init__(self, *args, **kwargs):
        super(GroupOwnerForm, self).__init__(*args, **kwargs)
        
        try:
            if kwargs['initial']['owners']: pass
            self.fields['owners'] = ExperimenterModelMultipleChoiceField(queryset=kwargs['initial']['experimenters'], initial=kwargs['initial']['owners'], required=False, label=_("Owners"))
        except:
            self.fields['owners'] = ExperimenterModelMultipleChoiceField(queryset=kwargs['initial']['experimenters'], required=False, label=_("Owners"))
        
        try:
            if kwargs['initial']['members']: pass
            self.fields['members'] = ExperimenterModelMultipleChoiceField(queryset=kwargs['initial']['experimenters'], initial=kwargs['initial']['members'], required=False, label=_("Members"))
        except:
            self.fields['members'] = ExperimenterModelMultipleChoiceField(queryset=kwargs['initial']['experimenters'], required=False, label=_("Members"))
            
        self.fields.keyOrder = ['owners', 'members', 'permissions']
            
    permissions = forms.ChoiceField(choices=PERMISSION_CHOICES, widget=forms.RadioSelect(), required=True, label=_("Permissions"))
    
class MyAccountForm(NonASCIIForm):
        
    def __init__(self, email_check=False, *args, **kwargs):
        super(MyAccountForm, self).__init__(*args, **kwargs)
        self.email_check=email_check
        try:
            if kwargs['initial']['default_group']: pass
            self.fields['default_group'] = GroupModelChoiceField(queryset=kwargs['initial']['groups'], initial=kwargs['initial']['default_group'], empty_label=None, label=_("Default group"))
        except:
            self.fields['default_group'] = GroupModelChoiceField(queryset=kwargs['initial']['groups'], empty_label=None, label=_("Default group"))
        self.fields.keyOrder = ['omename', 'first_name', 'middle_name', 'last_name', 'email', 'institution', 'default_group']

    omename = forms.CharField(max_length=50, widget=forms.TextInput(attrs={'onfocus':'this.blur()', 'size':30, 'autocomplete': 'off'}), label=_("Username"))
    first_name = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':30, 'autocomplete': 'off'}), label=_("First name"))
    middle_name = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':30, 'autocomplete': 'off'}), required=False, label=_("Middle name"))
    last_name = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':30, 'autocomplete': 'off'}), label=_("Last name"))
    email = forms.EmailField(widget=forms.TextInput(attrs={'size':30, 'autocomplete': 'off'}), required=False, label=_("Email"))
    institution = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':30, 'autocomplete': 'off'}), required=False, label=_("Institution"))

    def clean_email(self):
        if self.email_check:
            raise forms.ValidationError(_('This email already exist.'))
        return self.cleaned_data.get('email')


class ContainedExperimentersForm(NonASCIIForm):

    def __init__(self, *args, **kwargs):
        super(ContainedExperimentersForm, self).__init__(*args, **kwargs)
        
        try:
            if kwargs['initial']['members']: pass
            self.fields['members'] = ExperimenterModelMultipleChoiceField(queryset=kwargs['initial']['experimenters'], initial=kwargs['initial']['members'], required=False, label=_("Members"))
        except:
            self.fields['members'] = ExperimenterModelMultipleChoiceField(queryset=kwargs['initial']['experimenters'], required=False, label=_("Members"))

        self.fields.keyOrder = ['members']


class UploadPhotoForm(forms.Form):
    
    photo = forms.FileField(required=False, label=_("Photo"))

    def clean_photo(self):
        if self.cleaned_data.get('photo') is None:
            raise forms.ValidationError(_('No image selected. Supported image formats (file extensions allowed): jpeg, jpg, gif, png. The maximum image size allowed is 200KB.'))
        if not self.cleaned_data.get('photo').content_type.startswith("image"):
            raise forms.ValidationError(_('Supported image formats (file extensions allowed): jpeg, jpg, gif, png.'))
        if self.cleaned_data.get('photo').size > 204800:
            raise forms.ValidationError(_('The maximum image size allowed is 200KB.'))
        return self.cleaned_data.get('photo') 

class ChangePassword(NonASCIIForm):

    old_password = forms.CharField(max_length=50, widget=forms.PasswordInput(attrs={'size':30, 'autocomplete': 'off'}), label=_("Current password"))
    password = forms.CharField(max_length=50, widget=forms.PasswordInput(attrs={'size':30, 'autocomplete': 'off'}), label=_("New password"))
    confirmation = forms.CharField(max_length=50, widget=forms.PasswordInput(attrs={'size':30, 'autocomplete': 'off'}), label=_("Confirm password"))
    
    def clean_confirmation(self):
        if self.cleaned_data.get('password') or self.cleaned_data.get('confirmation'):
            if len(self.cleaned_data.get('password')) < 3:
                raise forms.ValidationError(_('Password must be at least 3 characters long.'))
            if self.cleaned_data.get('password') != self.cleaned_data.get('confirmation'):
                raise forms.ValidationError(_('Passwords do not match'))
            else:
                return self.cleaned_data.get('password')    
    
class EnumerationEntry(NonASCIIForm):
    
    new_entry = forms.CharField(max_length=250, widget=forms.TextInput(attrs={'size':30}), label=_("New entry"))


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
    
