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

try:
    from collections import OrderedDict  # Python 2.7+ only
except:
    pass

from django.conf import settings
from django import forms
from django.forms.widgets import Textarea
from django.utils.encoding import force_unicode
from django.utils.safestring import mark_safe

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
        max_length=50, widget=forms.TextInput(attrs={
            'size': 22, 'autofocus': 'autofocus'}))
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

    def clean_username(self):
        if (self.cleaned_data['username'] == 'guest'):
            raise forms.ValidationError("Guest account is not supported.")
        return self.cleaned_data['username']


class ForgottonPasswordForm(NonASCIIForm):

    server = ServerModelChoiceField(Server, empty_label=None)
    username = forms.CharField(
        max_length=50,
        widget=forms.TextInput(attrs={'size': 28, 'autocomplete': 'off'}))
    email = forms.EmailField(
        widget=forms.TextInput(attrs={'size': 28, 'autocomplete': 'off'}))


ROLE_CHOICES = (
    ('user', 'User'),
    ('administrator', 'Administrator'),
    ('restricted_administrator', 'Administrator with restricted privileges')
)


class RoleRenderer(forms.RadioSelect.renderer):
    """Allows disabling of 'administrator' Radio button."""
    def render(self):
        midList = []
        for x, wid in enumerate(self):
            if ROLE_CHOICES[x][0] == 'administrator':
                disable_admin = hasattr(self, 'disable_admin')
                wid.attrs['disabled'] = getattr(self, 'disable_admin')
            midList.append(u'<li>%s</li>' % force_unicode(wid))
        finalList = mark_safe(u'<ul id="id_role">\n%s\n</ul>'
                              % u'\n'.join([u'<li>%s</li>'
            % w for w in midList]))
        return finalList


class ExperimenterForm(NonASCIIForm):

    def __init__(self, name_check=False, email_check=False,
                 experimenter_is_me_or_system=False,
                 experimenter_me=False,
                 can_modify_user=True,
                 user_privileges=[],
                 experimenter_root=False,
                 *args, **kwargs):
        super(ExperimenterForm, self).__init__(*args, **kwargs)
        self.name_check = name_check
        self.email_check = email_check
        self.user_privileges = user_privileges

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
            self.fields['default_group'] = GroupModelChoiceField(
                queryset=kwargs['initial']['my_groups'],
                initial=kwargs['initial']['default_group'],
                empty_label=u"", required=False)
        except:
            try:
                self.fields['default_group'] = GroupModelChoiceField(
                    queryset=kwargs['initial']['my_groups'],
                    empty_label=u"", required=False)
            except:
                self.fields['default_group'] = GroupModelChoiceField(
                    queryset=list(), empty_label=u"", required=False)

        # 'Role' is disabled if experimenter is 'admin' or self,
        # so required=False to avoid validation error.
        self.fields['role'] = forms.ChoiceField(
            choices=ROLE_CHOICES,
            widget=forms.RadioSelect(renderer=RoleRenderer),
            required=False,
            initial='user')
        # If current user is restricted Admin, can't create full Admin
        restricted_admin = "ReadSession" not in self.user_privileges
        self.fields['role'].widget.renderer.disable_admin = restricted_admin

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

            fields_key_order = [
                'omename', 'password', 'confirmation', 'first_name',
                'middle_name', 'last_name', 'email', 'institution',
                'role', 'active', 'other_groups', 'default_group']
        else:
            fields_key_order = [
                'omename', 'first_name', 'middle_name', 'last_name',
                'email', 'institution', 'role', 'active',
                'other_groups', 'default_group']

        ordered_fields = [(k, self.fields[k]) for k in fields_key_order]

        roles = [('Sudo', 'Sudo'),
                 # combine WriteFile/ManagedRepo/Owned roles into 'Write'
                 ('Write', 'Write Data'),
                 # combine DeleteFile/ManagedRepo/Owned roles into 'Delete'
                 ('Delete', 'Delete Data'),
                 ('Chgrp', 'Chgrp'),
                 ('Chown', 'Chown'),
                 ('ModifyGroup', 'Create and Edit Groups'),
                 ('ModifyUser', 'Create and Edit Users'),
                 ('ModifyGroupMembership', 'Add Users to Groups'),
                 ('Script', 'Upload Scripts')]
        for role in roles:
            # If current user is light-admin, ignore privileges they don't have
            # So they can't add/remove these from experimenter
            # We don't disable them - (not in form data and will be removed)
            ordered_fields.append(
                (role[0], forms.BooleanField(
                    required=False,
                    label=role[1],
                    widget=forms.CheckboxInput(
                        attrs={'class': 'privilege',
                               'disabled': role[0] not in user_privileges})
                ))
            )

        # Django 1.8: Form.fields uses OrderedDict from the collections module.
        self.fields = OrderedDict(ordered_fields)

        if experimenter_me or experimenter_root:
            self.fields['omename'].widget.attrs['readonly'] = True
            name = "yourself"
            if experimenter_root:
                name = "'root' user"
            self.fields['omename'].widget.attrs['title'] = \
                "You can't edit Username of %s" % name
            self.fields['active'].widget.attrs['disabled'] = True
            self.fields['active'].widget.attrs['title'] = \
                "You cannot disable %s" % name

        # If we can't modify user, ALL fields are disabled
        if not can_modify_user:
            for field in self.fields.values():
                field.widget.attrs['disabled'] = True

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
            raise forms.ValidationError('This email already exists.')
        return self.cleaned_data.get('email')

    def clean_default_group(self):
        if (self.cleaned_data.get('default_group') is None or
                len(self.cleaned_data.get('default_group')) <= 0):
            raise forms.ValidationError('No default group selected.')
        else:
            return self.cleaned_data.get('default_group')

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
    ('3', 'Read-Write'),
)


class GroupForm(NonASCIIForm):

    def __init__(self, name_check=False, group_is_current_or_system=False,
                 can_modify_group=True, can_add_member=True, *args, **kwargs):
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

        # If we can't modify group, ALL fields are disabled
        if not can_modify_group:
            for field in self.fields.values():
                field.widget.attrs['disabled'] = True

        # If we can't add members, disable owners and members fields
        if not can_add_member:
            self.fields['owners'].widget.attrs['disabled'] = True
            self.fields['members'].widget.attrs['disabled'] = True

    name = forms.CharField(
        max_length=100,
        widget=forms.TextInput(attrs={'size': 25, 'autocomplete': 'off'}))
    description = forms.CharField(
        max_length=250,
        widget=forms.TextInput(attrs={'size': 25, 'autocomplete': 'off'}),
        required=False)

    def clean_name(self):
        if self.name_check:
            raise forms.ValidationError('This name already exists.')
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
            raise forms.ValidationError('This email already exists.')
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
