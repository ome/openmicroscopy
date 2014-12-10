#!/usr/bin/env python
# -*- coding: utf-8 -*-
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


import re

from django import forms
from django.forms.fields import ChoiceField, EMPTY_VALUES
from django.forms.widgets import SelectMultiple, MultipleHiddenInput
from django.forms import ModelChoiceField, ValidationError
from django.utils.translation import ugettext_lazy as _
from django.utils.encoding import smart_unicode
from django.core.validators import validate_email

##################################################################
# Fields


class OmeNameField(forms.CharField):
    def to_python(self, value):
        omeName = value
        if not value:
            raise forms.ValidationError('This field is required.')
        if not self.is_valid_omeName(omeName):
            raise forms.ValidationError('%s is not a valid Omename.' % omeName)
        return omeName

    def is_valid_omeName(self, omeName):
        # TODO: PATTERN !!!!!!!
        omeName_pattern = re.compile(r"(?:^|\s)[a-zA-Z0-9_.]")
        return omeName_pattern.match(omeName) is not None

# Group queryset iterator for group form


class ServerQuerySetIterator(object):
    def __init__(self, queryset, empty_label):
        self.queryset = queryset
        self.empty_label = empty_label

    def __iter__(self):
        if self.empty_label is not None:
            yield (u"", self.empty_label)
        for obj in self.queryset:
            if obj.server is None:
                name = "%s:%s" % (obj.host, obj.port)
            else:
                name = "%s:%s" % (obj.server, obj.port)
            yield (smart_unicode(obj.id), smart_unicode(name))


class ServerModelChoiceField(ModelChoiceField):

    def _get_choices(self):
        # If self._choices is set, then somebody must have manually set
        # the property self.choices. In this case, just return self._choices.
        if hasattr(self, '_choices'):
            return self._choices
        # Otherwise, execute the QuerySet in self.queryset to determine the
        # choices dynamically. Return a fresh QuerySetIterator that has not
        # been consumed. Note that we're instantiating a new QuerySetIterator
        # *each* time _get_choices() is called (and, thus, each time
        # self.choices is accessed) so that we can ensure the QuerySet has not
        # been consumed.
        return ServerQuerySetIterator(self.queryset, self.empty_label)

    def _set_choices(self, value):
        # This method is copied from ChoiceField._set_choices(). It's necessary
        # because property() doesn't allow a subclass to overwrite only
        # _get_choices without implementing _set_choices.
        self._choices = self.widget.choices = list(value)

    choices = property(_get_choices, _set_choices)

    def to_python(self, value):
        if value in EMPTY_VALUES:
            return None
        res = False
        for q in self.queryset:
            if long(value) == q.id:
                res = True
        if not res:
            raise ValidationError(self.error_messages['invalid_choice'])
        return value


# Group queryset iterator for group form
class GroupQuerySetIterator(object):
    def __init__(self, queryset, empty_label):
        self.queryset = queryset
        self.empty_label = empty_label

    def __iter__(self):
        if self.empty_label is not None:
            yield (u"", self.empty_label)
        for obj in self.queryset:
            if hasattr(obj.name, 'val'):
                name = obj.name.val
            else:
                name = obj.name
            l = len(name)
            if l > 35:
                name = name[:35] + "..."
            name += " (%s)" % str(obj.getDetails().permissions)
            if hasattr(obj.id, 'val'):
                oid = obj.id.val
            else:
                oid = obj.id
            yield (smart_unicode(oid), smart_unicode(name))


class GroupModelChoiceField(ModelChoiceField):

    def _get_choices(self):
        # If self._choices is set, then somebody must have manually set
        # the property self.choices. In this case, just return self._choices.
        if hasattr(self, '_choices'):
            return self._choices
        # Otherwise, execute the QuerySet in self.queryset to determine the
        # choices dynamically. Return a fresh QuerySetIterator that has not
        # been consumed. Note that we're instantiating a new QuerySetIterator
        # *each* time _get_choices() is called (and, thus, each time
        # self.choices is accessed) so that we can ensure the QuerySet has not
        # been consumed.
        return GroupQuerySetIterator(self.queryset, self.empty_label)

    def _set_choices(self, value):
        # This method is copied from ChoiceField._set_choices(). It's necessary
        # because property() doesn't allow a subclass to overwrite only
        # _get_choices without implementing _set_choices.
        self._choices = self.widget.choices = list(value)

    choices = property(_get_choices, _set_choices)

    def to_python(self, value):
        if value in EMPTY_VALUES:
            return None
        res = False
        exps = []
        try:
            for experimenter_type, experimenters in self.queryset:
                for experimenter in experimenters:
                    exps.append(experimenter)
        except:
            exps = self.queryset
        for experimenter in exps:
            if hasattr(experimenter.id, 'val'):
                if long(value) == experimenter.id.val:
                    res = True
            else:
                if long(value) == experimenter.id:
                    res = True
        if not res:
            raise ValidationError(self.error_messages['invalid_choice'])
        return value


class GroupModelMultipleChoiceField(GroupModelChoiceField):
    """A MultipleChoiceField whose choices are a model QuerySet."""
    hidden_widget = MultipleHiddenInput
    default_error_messages = {
        'list': _(u'Enter a list of values.'),
        'invalid_choice': _(u'Select a valid choice. That choice is not one'
                            ' of the available choices.'),
    }

    def __init__(self, queryset, cache_choices=False, required=True,
                 widget=SelectMultiple, label=None, initial=None,
                 help_text=None, *args, **kwargs):
        super(GroupModelMultipleChoiceField, self).__init__(
            queryset, None, cache_choices, required, widget, label, initial,
            help_text, *args, **kwargs)

    def to_python(self, value):
        if self.required and not value:
            raise ValidationError(self.error_messages['required'])
        elif not self.required and not value:
            return []
        if not isinstance(value, (list, tuple)):
            raise ValidationError(self.error_messages['list'])
        final_values = []
        for val in value:
            try:
                long(val)
            except:
                raise ValidationError(self.error_messages['invalid_choice'])
            else:
                res = False
                for q in self.queryset:
                    if hasattr(q.id, 'val'):
                        if long(val) == q.id.val:
                            res = True
                    else:
                        if long(val) == q.id:
                            res = True
                if not res:
                    raise ValidationError(
                        self.error_messages['invalid_choice'])
                else:
                    final_values.append(val)
        return final_values


# Experimenter queryset iterator for experimenter form
class ExperimenterQuerySetIterator(object):
    def __init__(self, queryset, empty_label):
        self.queryset = queryset

        self.empty_label = empty_label

        self.rendered_set = []
        if self.empty_label is not None:
            self.rendered_set.append((u"", self.empty_label))

        # queryset may be a list of Experimenters 'exp_list' OR may be
        # (("Leaders", exp_list), ("Members", exp_list))
        for obj in queryset:
            if hasattr(obj, 'id'):
                self.rendered_set.append(self.render(obj))
            else:
                subset = [self.render(m) for m in obj[1]]
                self.rendered_set.append((obj[0], subset))

    def __iter__(self):
        for obj in self.rendered_set:
            yield obj

    def render(self, obj):
        try:
            # lastName = obj.details.owner.lastName.val if
            # hasattr(obj.details.owner.lastName, 'val') else ""
            # firstName = obj.details.owner.firstName.val if
            # hasattr(obj.details.owner.firstName, 'val') else ""
            # middleName = obj.details.owner.middleName.val if
            # hasattr(obj.details.owner.middleName, 'val') else ""
            if hasattr(obj, 'getFullName'):
                name = "%s (%s)" % (obj.getFullName(), obj.omeName)
            else:
                omeName = None
                if hasattr(obj.omeName, 'val'):
                    omeName = obj.omeName.val
                lastName = None
                if hasattr(obj.lastName, 'val'):
                    lastName = obj.lastName.val
                firstName = None
                if hasattr(obj.firstName, 'val'):
                    firstName = obj.firstName.val
                middleName = None
                if hasattr(obj.middleName, 'val'):
                    middleName = obj.middleName.val

                # 'myself' was introduced in the commit below, but it's not
                # clear what it should be.  Setting to blank string to prevent
                # exception.
                # https://github.com/openmicroscopy/openmicroscopy/commit/f6b5dcd89ce9e03c7f0c7cdb2abc5e4da5d717ee
                myself = ''

                if middleName != '' and middleName is not None:
                    name = "%s%s %s. %s (%s)" % (
                        myself, firstName, middleName[:1], lastName, omeName)
                else:
                    name = "%s%s %s (%s)" % (
                        myself, firstName, lastName, omeName)

            l = len(name)
            if l > 50:
                name = name[:50] + "..."
        except:
            name = _("Unknown")

        if hasattr(obj.id, 'val'):
            oid = obj.id.val
        else:
            oid = obj.id
        return (smart_unicode(oid), smart_unicode(name))


class ExperimenterModelChoiceField(ModelChoiceField):

    def _get_choices(self):
        # If self._choices is set, then somebody must have manually set
        # the property self.choices. In this case, just return self._choices.
        if hasattr(self, '_choices'):
            return self._choices
        # Otherwise, execute the QuerySet in self.queryset to determine the
        # choices dynamically. Return a fresh QuerySetIterator that has not
        # been consumed. Note that we're instantiating a new QuerySetIterator
        # *each* time _get_choices() is called (and, thus, each time
        # self.choices is accessed) so that we can ensure the QuerySet has not
        # been consumed.
        return ExperimenterQuerySetIterator(self.queryset, self.empty_label)

    def _set_choices(self, value):
        # This method is copied from ChoiceField._set_choices(). It's necessary
        # because property() doesn't allow a subclass to overwrite only
        # _get_choices without implementing _set_choices.
        self._choices = self.widget.choices = list(value)

    choices = property(_get_choices, _set_choices)

    def to_python(self, value):
        """
        Go through all values in queryset, looking to find 'value'. If not
        found raise ValidationError.

        @return value:      The input value
        """

        if value in EMPTY_VALUES:
            return None
        res = False

        def checkValue(q, value):
            if not hasattr(q, 'id'):
                return False
            if hasattr(q.id, 'val'):
                if long(value) == q.id.val:
                    return True
            if long(value) == q.id:
                return True

        for q in self.queryset:
            if isinstance(q, tuple) or isinstance(q, list):
                for qu in q:
                    if isinstance(qu, tuple) or isinstance(qu, list):
                        for qq in qu:
                            if checkValue(qq, value):
                                res = True
                    else:
                        if checkValue(qu, value):
                            res = True
            else:
                if checkValue(q, value):
                    res = True
        if not res:
            raise ValidationError(self.error_messages['invalid_choice'])
        return value


class ExperimenterModelMultipleChoiceField(ExperimenterModelChoiceField):
    """A MultipleChoiceField whose choices are a model QuerySet."""
    hidden_widget = MultipleHiddenInput
    default_error_messages = {
        'list': _(u'Enter a list of values.'),
        'invalid_choice': _(u'Select a valid choice. That choice is not one'
                            ' of the available choices.'),
    }

    def __init__(self, queryset, cache_choices=False, required=True,
                 widget=SelectMultiple, label=None, initial=None,
                 help_text=None, *args, **kwargs):
        super(ExperimenterModelMultipleChoiceField, self).__init__(
            queryset, None, cache_choices, required, widget, label, initial,
            help_text, *args, **kwargs)

    def to_python(self, value):
        if self.required and not value:
            raise ValidationError(self.error_messages['required'])
        elif not self.required and not value:
            return []
        if not isinstance(value, (list, tuple)):
            raise ValidationError(self.error_messages['list'])
        final_values = []
        for val in value:
            try:
                long(val)
            except:
                raise ValidationError(self.error_messages['invalid_choice'])
            else:
                res = False
                for q in self.queryset:
                    if hasattr(q.id, 'val'):
                        if long(val) == q.id.val:
                            res = True
                    else:
                        if long(val) == q.id:
                            res = True
                if not res:
                    raise ValidationError(
                        self.error_messages['invalid_choice'])
                else:
                    final_values.append(val)
        return final_values


class DefaultGroupField(ChoiceField):

    def to_python(self, value):
        """
        Check that the field was selected.
        """
        if not value:
            raise forms.ValidationError("Choose one of the 'Selected groups'"
                                        " to specify 'Default Group'.")

        # Always return the cleaned data.
        return value


class MultiEmailField(forms.Field):
    """
    A field to process comma seperated email into an array of stripped strings
    """

    def to_python(self, value):
        """Normalize data to a list of strings."""
        # Return an empty list if no input was given.
        if not value:
            return []

        return [v.strip() for v in value.split(',')]

    def validate(self, value):
        """Check if value consists only of valid emails."""

        # Use the parent's handling of required fields, etc.
        super(MultiEmailField, self).validate(value)

        for email in value:
            validate_email(email)
