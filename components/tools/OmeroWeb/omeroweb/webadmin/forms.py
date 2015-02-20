#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Copyright (c) 2008-2014 University of Dundee.
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

import logging

from django.conf import settings
from django import forms
from django.forms.widgets import Textarea

from omeroweb.connector import Server

from omeroweb.custom_forms import NonASCIIForm

from custom_forms import ServerModelChoiceField, GroupModelChoiceField
from custom_forms import GroupModelMultipleChoiceField, OmeNameField
from custom_forms import ExperimenterModelMultipleChoiceField, MultiEmailField

logger = logging.getLogger(__name__)

#################################################################
# Non-model Form


class LoginForm(NonASCIIForm):

    def __init__(self, *args, **kwargs):
        super(LoginForm, self).__init__(*args, **kwargs)
        self.fields['server'] = ServerModelChoiceField(
            Server, empty_label=None)

        self.fields.keyOrder = ['server', 'username', 'password', 'ssl']

    username = forms.CharField(
        max_length=50, widget=forms.TextInput(attrs={'size': 22}))
    password = forms.CharField(
        max_length=50,
        widget=forms.PasswordInput(attrs={'size': 22, 'autocomplete': 'off'}))
    ssl = forms.BooleanField(
        required=False,
        help_text='<img src="%swebgateway/img/nuvola_encrypted_grey16.png"'
        ' title="Real-time encrypted data transfer can be turned on by'
        ' checking the box, but it will slow down the data access. Turning'
        ' it off does not affect the connection to the server which is always'
        ' secure." alt="SSL"/>' % settings.STATIC_URL)


class ForgottonPasswordForm(NonASCIIForm):

    server = ServerModelChoiceField(Server, empty_label=None)
    username = forms.CharField(
        max_length=50,
        widget=forms.TextInput(attrs={'size': 28, 'autocomplete': 'off'}))
    email = forms.EmailField(
        widget=forms.TextInput(attrs={'size': 28, 'autocomplete': 'off'}))


class ExperimenterForm(NonASCIIForm):

    def __init__(self, name_check=False, email_check=False,
                 experimenter_is_me_or_system=False, *args, **kwargs):
        super(ExperimenterForm, self).__init__(*args, **kwargs)
        self.name_check = name_check
        self.email_check = email_check

        try:
            self.fields['other_groups'] = GroupModelMultipleChoiceField(
                queryset=kwargs['initial']['groups'],
                initial=kwargs['initial']['other_groups'], required=False,
                label="Groups")
        except:
            self.fields['other_groups'] = GroupModelMultipleChoiceField(
                queryset=kwargs['initial']['groups'], required=False,
                label="Groups")

        try:
            if kwargs['initial']['default_group']:
                pass
            self.fields['default_group'] = GroupModelChoiceField(
                queryset=kwargs['initial']['groups'],
                initial=kwargs['initial']['default_group'],
                empty_label=u"---------", required=False)
        except:
            self.fields['default_group'] = GroupModelChoiceField(
                queryset=kwargs['initial']['groups'],
                empty_label=u"---------", required=False)
        self.fields['default_group'].widget.attrs['class'] = 'hidden'

        if ('with_password' in kwargs['initial'] and
                kwargs['initial']['with_password']):
            self.fields['password'] = forms.CharField(
                max_length=50,
                widget=forms.PasswordInput(attrs={'size': 30,
                                                  'autocomplete': 'off'}))
            self.fields['confirmation'] = forms.CharField(
                max_length=50,
                widget=forms.PasswordInput(attrs={'size': 30,
                                                  'autocomplete': 'off'}))

            self.fields.keyOrder = [
                'omename', 'password', 'confirmation', 'first_name',
                'middle_name', 'last_name', 'email', 'institution',
                'administrator', 'active', 'default_group', 'other_groups']
        else:
            self.fields.keyOrder = [
                'omename', 'first_name', 'middle_name', 'last_name', 'email',
                'institution', 'administrator', 'active', 'default_group',
                'other_groups']
        if experimenter_is_me_or_system:
            self.fields['omename'].widget.attrs['readonly'] = True
            self.fields['omename'].widget.attrs['title'] = \
                "Changing of system username would be un-doable"
            self.fields['administrator'].widget.attrs['disabled'] = True
            self.fields['administrator'].widget.attrs['title'] = \
                "Removal of your own admin rights would be un-doable"
            self.fields['active'].widget.attrs['disabled'] = True
            self.fields['active'].widget.attrs['title'] = \
                "You cannot disable yourself"

    omename = OmeNameField(
        max_length=250,
        widget=forms.TextInput(attrs={'size': 30, 'autocomplete': 'off'}),
        label="Username")
    first_name = forms.CharField(
        max_length=250,
        widget=forms.TextInput(attrs={'size': 30, 'autocomplete': 'off'}))
    middle_name = forms.CharField(max_length=250, widget=forms.TextInput(
        attrs={'size': 30, 'autocomplete': 'off'}), required=False)
    last_name = forms.CharField(
        max_length=250,
        widget=forms.TextInput(attrs={'size': 30, 'autocomplete': 'off'}))
    email = forms.EmailField(
        widget=forms.TextInput(attrs={'size': 30, 'autocomplete': 'off'}),
        required=False)
    institution = forms.CharField(
        max_length=250,
        widget=forms.TextInput(attrs={'size': 30, 'autocomplete': 'off'}),
        required=False)
    administrator = forms.BooleanField(required=False)
    active = forms.BooleanField(required=False)

    def clean_confirmation(self):
        if (self.cleaned_data.get('password') or
                self.cleaned_data.get('confirmation')):
            if len(self.cleaned_data.get('password')) < 3:
                raise forms.ValidationError(
                    'Password must be at least 3 characters long.')
            if (self.cleaned_data.get('password') !=
                    self.cleaned_data.get('confirmation')):
                raise forms.ValidationError('Passwords do not match')
            else:
                return self.cleaned_data.get('password')

    def clean_omename(self):
        if self.name_check:
            raise forms.ValidationError('This username already exists.')
        return self.cleaned_data.get('omename')

    def clean_email(self):
        if self.email_check:
            raise forms.ValidationError('This email already exist.')
        return self.cleaned_data.get('email')

    def clean_other_groups(self):
        if (self.cleaned_data.get('other_groups') is None or
                len(self.cleaned_data.get('other_groups')) <= 0):
            raise forms.ValidationError(
                'User must be a member of at least one group.')
        else:
            return self.cleaned_data.get('other_groups')


PERMISSION_CHOICES = (
    ('0', 'Private'),
    ('1', 'Read-Only'),
    ('2', 'Read-Annotate'),
)


class GroupForm(NonASCIIForm):

    def __init__(self, name_check=False, group_is_current_or_system=False,
                 *args, **kwargs):
        super(GroupForm, self).__init__(*args, **kwargs)
        self.name_check = name_check
        try:
            if kwargs['initial']['owners']:
                pass
            self.fields['owners'] = ExperimenterModelMultipleChoiceField(
                queryset=kwargs['initial']['experimenters'],
                initial=kwargs['initial']['owners'], required=False)
        except:
            self.fields['owners'] = ExperimenterModelMultipleChoiceField(
                queryset=kwargs['initial']['experimenters'], required=False)

        try:
            if kwargs['initial']['members']:
                pass
            self.fields['members'] = ExperimenterModelMultipleChoiceField(
                queryset=kwargs['initial']['experimenters'],
                initial=kwargs['initial']['members'], required=False)
        except:
            self.fields['members'] = ExperimenterModelMultipleChoiceField(
                queryset=kwargs['initial']['experimenters'], required=False)

        self.fields['permissions'] = forms.ChoiceField(
            choices=PERMISSION_CHOICES, widget=forms.RadioSelect(),
            required=True, label="Permissions")

        if group_is_current_or_system:
            self.fields['name'].widget.attrs['readonly'] = True
            self.fields['name'].widget.attrs['title'] = \
                "Changing of system groupname would be un-doable"

        self.fields.keyOrder = [
            'name', 'description', 'owners', 'members', 'permissions']

    name = forms.CharField(
        max_length=100,
        widget=forms.TextInput(attrs={'size': 25, 'autocomplete': 'off'}))
    description = forms.CharField(
        max_length=250,
        widget=forms.TextInput(attrs={'size': 25, 'autocomplete': 'off'}),
        required=False)

    def clean_name(self):
        if self.name_check:
            raise forms.ValidationError('This name already exist.')
        return self.cleaned_data.get('name')


class GroupOwnerForm(forms.Form):

    def __init__(self, *args, **kwargs):
        super(GroupOwnerForm, self).__init__(*args, **kwargs)

        try:
            if kwargs['initial']['owners']:
                pass
            self.fields['owners'] = ExperimenterModelMultipleChoiceField(
                queryset=kwargs['initial']['experimenters'],
                initial=kwargs['initial']['owners'], required=False)
        except:
            self.fields['owners'] = ExperimenterModelMultipleChoiceField(
                queryset=kwargs['initial']['experimenters'], required=False)

        try:
            if kwargs['initial']['members']:
                pass
            self.fields['members'] = ExperimenterModelMultipleChoiceField(
                queryset=kwargs['initial']['experimenters'],
                initial=kwargs['initial']['members'], required=False)
        except:
            self.fields['members'] = ExperimenterModelMultipleChoiceField(
                queryset=kwargs['initial']['experimenters'], required=False)

        self.fields.keyOrder = ['owners', 'members', 'permissions']

    permissions = forms.ChoiceField(
        choices=PERMISSION_CHOICES, widget=forms.RadioSelect(), required=True,
        label="Permissions")


class MyAccountForm(NonASCIIForm):

    def __init__(self, email_check=False, *args, **kwargs):
        super(MyAccountForm, self).__init__(*args, **kwargs)
        self.email_check = email_check
        try:
            if kwargs['initial']['default_group']:
                pass
            self.fields['default_group'] = GroupModelChoiceField(
                queryset=kwargs['initial']['groups'],
                initial=kwargs['initial']['default_group'],
                empty_label=None)
        except:
            self.fields['default_group'] = GroupModelChoiceField(
                queryset=kwargs['initial']['groups'],
                empty_label=None)
        self.fields.keyOrder = [
            'omename', 'first_name', 'middle_name', 'last_name', 'email',
            'institution', 'default_group']

    omename = forms.CharField(
        max_length=50,
        widget=forms.TextInput(attrs={'onfocus': 'this.blur()', 'size': 30,
                                      'autocomplete': 'off'}),
        label="Username")
    first_name = forms.CharField(
        max_length=250,
        widget=forms.TextInput(attrs={'size': 30, 'autocomplete': 'off'}))
    middle_name = forms.CharField(
        max_length=250,
        widget=forms.TextInput(attrs={'size': 30, 'autocomplete': 'off'}),
        required=False)
    last_name = forms.CharField(
        max_length=250,
        widget=forms.TextInput(attrs={'size': 30, 'autocomplete': 'off'}))
    email = forms.EmailField(
        widget=forms.TextInput(attrs={'size': 30, 'autocomplete': 'off'}),
        required=False)
    institution = forms.CharField(
        max_length=250,
        widget=forms.TextInput(attrs={'size': 30, 'autocomplete': 'off'}),
        required=False)

    def clean_email(self):
        if self.email_check:
            raise forms.ValidationError('This email already exist.')
        return self.cleaned_data.get('email')


class ContainedExperimentersForm(NonASCIIForm):

    def __init__(self, *args, **kwargs):
        super(ContainedExperimentersForm, self).__init__(*args, **kwargs)

        try:
            if kwargs['initial']['members']:
                pass
            self.fields['members'] = ExperimenterModelMultipleChoiceField(
                queryset=kwargs['initial']['experimenters'],
                initial=kwargs['initial']['members'],
                required=False)
        except:
            self.fields['members'] = ExperimenterModelMultipleChoiceField(
                queryset=kwargs['initial']['experimenters'],
                required=False)

        self.fields.keyOrder = ['members']


class UploadPhotoForm(forms.Form):

    photo = forms.FileField(required=False)

    def clean_photo(self):
        if self.cleaned_data.get('photo') is None:
            raise forms.ValidationError(
                'No image selected. Supported image formats (file extensions'
                ' allowed): jpeg, jpg, gif, png. The maximum image size'
                ' allowed is 200KB.')
        if not self.cleaned_data.get(
                'photo').content_type.startswith("image"):
            raise forms.ValidationError(
                'Supported image formats (file extensions allowed):'
                ' jpeg, jpg, gif, png.')
        if self.cleaned_data.get('photo').size > 204800:
            raise forms.ValidationError(
                'The maximum image size allowed is 200KB.')
        return self.cleaned_data.get('photo')


class ChangePassword(NonASCIIForm):

    old_password = forms.CharField(
        max_length=50,
        widget=forms.PasswordInput(attrs={'size': 30, 'autocomplete': 'off'}),
        label="Current password")
    password = forms.CharField(
        max_length=50,
        widget=forms.PasswordInput(attrs={'size': 30, 'autocomplete': 'off'}),
        label="New password")
    confirmation = forms.CharField(
        max_length=50,
        widget=forms.PasswordInput(attrs={'size': 30, 'autocomplete': 'off'}),
        label="Confirm password")

    def clean_confirmation(self):
        if (self.cleaned_data.get('password') or
                self.cleaned_data.get('confirmation')):
            if len(self.cleaned_data.get('password')) < 3:
                raise forms.ValidationError('Password must be at least 3'
                                            ' characters long.')
            if (self.cleaned_data.get('password') !=
                    self.cleaned_data.get('confirmation')):
                raise forms.ValidationError('Passwords do not match')
            else:
                return self.cleaned_data.get('password')


class EnumerationEntry(NonASCIIForm):

    new_entry = forms.CharField(
        max_length=250,
        widget=forms.TextInput(attrs={'size': 30}))


class EnumerationEntries(NonASCIIForm):

    def __init__(self, entries, *args, **kwargs):
        super(EnumerationEntries, self).__init__(*args, **kwargs)
        for i, e in enumerate(entries):
            try:
                if kwargs['initial']['entries']:
                    self.fields[str(e.id)] = forms.CharField(
                        max_length=250,
                        initial=e.value,
                        widget=forms.TextInput(attrs={'size': 30}),
                        label=i+1)
                else:
                    self.fields[str(e.id)] = forms.CharField(
                        max_length=250,
                        widget=forms.TextInput(attrs={'size': 30}),
                        label=i+1)
            except:
                self.fields[str(e.id)] = forms.CharField(
                    max_length=250,
                    widget=forms.TextInput(attrs={'size': 30}),
                    label=i+1)

        self.fields.keyOrder = [str(k) for k in self.fields.keys()]


class EmailForm(forms.Form):
    """
    Form to gather recipients, subject and message for sending email
    announcements
    """

    error_css_class = 'field-error'
    required_css_class = 'field-required'

    # Define these as None just so I can order them
    everyone = forms.BooleanField(required=False, label='All Users')

    experimenters = forms.TypedMultipleChoiceField(
        required=False,
        coerce=int,
        label='Users'
    )
    groups = forms.TypedMultipleChoiceField(
        required=False,
        coerce=int
    )

    # TODO CC isn't really CC. Maybe change label or change functionality
    cc = MultiEmailField(required=False)
    subject = forms.CharField(max_length=100, required=True)
    message = forms.CharField(widget=Textarea, required=True)

    # Include/Exclude inactive users
    inactive = forms.BooleanField(label='Include inactive users',
                                  required=False)

    def __init__(self, experimenters, groups, conn, request, *args, **kwargs):
        super(EmailForm, self).__init__(*args, **kwargs)
        # Process Experimenters/Groups into choices (lists of tuples)
        self.fields['experimenters'].choices = [
            (experimenter.id, experimenter.firstName +
             ' ' + experimenter.lastName + ' (' + experimenter.omeName + ')' +
             (' - Inactive' if not experimenter.isActive() else ''))
            for experimenter in experimenters]

        self.fields['groups'].choices = [
            (group.id, group.name) for group in groups]

        self.conn = conn
        self.request = request

    def clean(self):
        cleaned_data = super(EmailForm, self).clean()
        everyone = cleaned_data.get("everyone")
        experimenters = cleaned_data.get("experimenters")
        groups = cleaned_data.get("groups")
        cc = cleaned_data.get("cc")

        # If nobody addressed, throw an error
        if not cc and not everyone and not experimenters and not groups:
            raise forms.ValidationError("At least one addressee must be "
                                        "specified in one or more of 'all',"
                                        " 'user', 'group' or 'cc'")
        return cleaned_data
