#!/usr/bin/env python
# -*- coding: utf-8 -*-
from django import forms
from django.utils.encoding import smart_str

from django.forms.util import ErrorDict, ValidationError
from django.forms.fields import FileField, CharField


class NonASCIIForm(forms.Form):

    def __init__(self, *args, **kwargs):
        super(NonASCIIForm, self).__init__(*args, **kwargs)

    def full_clean(self):
        """
        Cleans all of self.data and populates self._errors and
        self.cleaned_data.
        """
        self._errors = ErrorDict()
        if not self.is_bound:  # Stop further processing.
            return
        self.cleaned_data = {}
        # If the form is permitted to be empty, and none of the form data has
        # changed from the initial data, short circuit any validation.
        if self.empty_permitted and not self.has_changed():
            return
        for name, field in self.fields.items():
            # value_from_datadict() gets the data from the data dictionaries.
            # Each widget type knows how to retrieve its own data, because
            # some widgets split data over several HTML fields.
            value = field.widget.value_from_datadict(
                self.data, self.files, self.add_prefix(name))
            try:
                if isinstance(field, FileField):
                    initial = self.initial.get(name, field.initial)
                    value = field.clean(value, initial)
                elif isinstance(field, CharField):
                    if (value is not None and
                            isinstance(value, basestring) and len(value) > 0):
                        value = str(smart_str(value))
                    else:
                        value = field.clean(value)
                else:
                    value = field.clean(value)
                self.cleaned_data[name] = value
                if hasattr(self, 'clean_%s' % name):
                    value = getattr(self, 'clean_%s' % name)()
                    self.cleaned_data[name] = value
            except ValidationError, e:
                self._errors[name] = self.error_class(e.messages)
                if name in self.cleaned_data:
                    del self.cleaned_data[name]
        try:
            self.cleaned_data = self.clean()
        except ValidationError, e:
            self._errors[forms.Form.NON_FIELD_ERRORS] = \
                self.error_class(e.messages)
        if self._errors:
            delattr(self, 'cleaned_data')
