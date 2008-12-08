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

from django.forms.fields import Field, ChoiceField, EMPTY_VALUES
from django.forms.widgets import Select, SelectMultiple, MultipleHiddenInput
from django.forms import ModelChoiceField, ModelMultipleChoiceField, ValidationError
from django.utils.translation import ugettext_lazy as _
from django.utils.encoding import smart_unicode
        
# Group queryset iterator for group form
class GroupQuerySetIterator(object):
    def __init__(self, queryset, empty_label, cache_choices):
        self.queryset = queryset
        self.empty_label = empty_label
        self.cache_choices = cache_choices

    def __iter__(self):
        if self.empty_label is not None:
            yield (u"", self.empty_label)
        for obj in self.queryset:
            if hasattr(obj.id, 'val'):
                yield (obj.id.val, smart_unicode(obj.name.val))
            else:
                yield (obj.id, smart_unicode(obj.name))
        # Clear the QuerySet cache if required.
        #if not self.cache_choices:
            #self.queryset._result_cache = None

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
        return GroupQuerySetIterator(self.queryset, self.empty_label,
                                self.cache_choices)

    def _set_choices(self, value):
        # This method is copied from ChoiceField._set_choices(). It's necessary
        # because property() doesn't allow a subclass to overwrite only
        # _get_choices without implementing _set_choices.
        self._choices = self.widget.choices = list(value)

    choices = property(_get_choices, _set_choices)

    def clean(self, value):
        Field.clean(self, value)
        if value in EMPTY_VALUES:
            return None
        #try:
            #value = self.queryset.get(pk=value)
        #except self.queryset.model.DoesNotExist:
            #raise ValidationError(self.error_messages['invalid_choice'])
        res = False
        for q in self.queryset:
            if hasattr(q.id, 'val'):
                if long(value) == q.id.val:
                    res = True
            else:
                if long(value) == q.id:
                    res = True
        if not res:
            raise ValidationError(self.error_messages['invalid_choice'])
        return value

class GroupModelMultipleChoiceField(GroupModelChoiceField):
    """A MultipleChoiceField whose choices are a model QuerySet."""
    hidden_widget = MultipleHiddenInput
    default_error_messages = {
        'list': _(u'Enter a list of values.'),
        'invalid_choice': _(u'Select a valid choice. That choice is not one of the'
                            u' available choices.'),
    }

    def __init__(self, queryset, cache_choices=False, required=True,
                 widget=SelectMultiple, label=None, initial=None,
                 help_text=None, *args, **kwargs):
        super(GroupModelMultipleChoiceField, self).__init__(queryset, None,
            cache_choices, required, widget, label, initial, help_text,
            *args, **kwargs)

    def clean(self, value):
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
            #try:
                #obj = self.queryset.get(pk=val)
            #except self.queryset.model.DoesNotExist:
                #raise ValidationError(self.error_messages['invalid_choice'] % val)
            #else:
                #final_values.append(val)
                res = False
                for q in self.queryset:
                    if hasattr(q.id, 'val'):
                        if long(val) == q.id.val:
                            res = True
                    else:
                        if long(val) == q.id:
                            res = True
                if not res:
                    raise ValidationError(self.error_messages['invalid_choice'])
                else:
                    final_values.append(val)
        return final_values

# Experimenter queryset iterator for experimenter form
class ExperimenterQuerySetIterator(object):
    def __init__(self, queryset, empty_label, cache_choices):
        self.queryset = queryset
        self.empty_label = empty_label
        self.cache_choices = cache_choices

    def __iter__(self):
        if self.empty_label is not None:
            yield (u"", self.empty_label)
        for obj in self.queryset:
            # only for python 2.5
            # lastName = self._obj.details.owner.lastName.val if hasattr(self._obj.details.owner.lastName, 'val') else ""
            # firstName = self._obj.details.owner.firstName.val if hasattr(self._obj.details.owner.firstName, 'val') else ""
            # middleName = self._obj.details.owner.middleName.val if hasattr(self._obj.details.owner.middleName, 'val') else ""
            lastName = ""
            if hasattr(obj.lastName, 'val'):
                lastName = obj.lastName.val
            else:
                if obj.lastName is not None:
                    lastName = obj.lastName
            firstName = ""
            if hasattr(obj.firstName, 'val'):
                firstName = obj.firstName.val
            else:
                if obj.firstName is not None:
                    firstName = obj.firstName
            middleName = ""
            if hasattr(obj.middleName, 'val'):
                middleName = obj.middleName.val
            else:
                if obj.middleName is not None:
                    middleName = obj.middleName
            name = "%s %s, %s" % (lastName, firstName, middleName)
            if hasattr(obj.id, 'val'):
                oid = obj.id.val
            else:
                oid = obj.id

            yield (oid, smart_unicode(name))
        # Clear the QuerySet cache if required.
        #if not self.cache_choices:
            #self.queryset._result_cache = None

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
        return ExperimenterQuerySetIterator(self.queryset, self.empty_label,
                                self.cache_choices)

    def _set_choices(self, value):
        # This method is copied from ChoiceField._set_choices(). It's necessary
        # because property() doesn't allow a subclass to overwrite only
        # _get_choices without implementing _set_choices.
        self._choices = self.widget.choices = list(value)

    choices = property(_get_choices, _set_choices)

    def clean(self, value):
        Field.clean(self, value)
        if value in EMPTY_VALUES:
            return None
        #try:
            #value = self.queryset.get(pk=value)
        #except self.queryset.model.DoesNotExist:
            #raise ValidationError(self.error_messages['invalid_choice'])
        res = False
        for q in self.queryset:
            if hasattr(q.id, 'val'):
                if long(value) == q.id.val:
                    res = True
            else:
                if long(value) == q.id:
                    res = True
        if not res:
            raise ValidationError(self.error_messages['invalid_choice'])
        return value

class ExperimenterModelMultipleChoiceField(ExperimenterModelChoiceField):
    """A MultipleChoiceField whose choices are a model QuerySet."""
    hidden_widget = MultipleHiddenInput
    default_error_messages = {
        'list': _(u'Enter a list of values.'),
        'invalid_choice': _(u'Select a valid choice. That choice is not one of the'
                            u' available choices.'),
    }

    def __init__(self, queryset, cache_choices=False, required=True,
                 widget=SelectMultiple, label=None, initial=None,
                 help_text=None, *args, **kwargs):
        super(ExperimenterModelMultipleChoiceField, self).__init__(queryset, None,
            cache_choices, required, widget, label, initial, help_text,
            *args, **kwargs)

    def clean(self, value):
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
            #try:
                #obj = self.queryset.get(pk=val)
            #except self.queryset.model.DoesNotExist:
                #raise ValidationError(self.error_messages['invalid_choice'] % val)
            #else:
                #final_values.append(val)
                res = False
                for q in self.queryset:
                    if hasattr(q.id, 'val'):
                        if long(val) == q.id.val:
                            res = True
                    else:
                        if long(val) == q.id:
                            res = True
                if not res:
                    raise ValidationError(self.error_messages['invalid_choice'])
                else:
                    final_values.append(val)
        return final_values