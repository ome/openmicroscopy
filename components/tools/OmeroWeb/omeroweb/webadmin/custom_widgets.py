#!/usr/bin/env python
# -*- coding: utf-8 -*-
from django.forms.widgets import RadioSelect, RadioInput, RadioFieldRenderer
from django.utils.encoding import force_unicode


class DefaultGroupRadioInput(RadioInput):
    """
    An object used by RadioFieldRenderer that represents a single
    <input type='radio'>.
    """

    def __init__(self, name, value, attrs, choice, index):
        self.name, self.value = name, value
        self.attrs = attrs
        self.choice_value = force_unicode(choice[0])
        self.choice_label = force_unicode(choice[1])
        # self.index = index
        self.index = choice[0]


class DefaultGroupRadioFieldRenderer(RadioFieldRenderer):
    """
    An object used by RadioSelect to enable customization of radio widgets.
    """

    def __iter__(self):
        for i, choice in enumerate(self.choices):
            yield DefaultGroupRadioInput(
                self.name, self.value, self.attrs.copy(), choice, i)

    def __getitem__(self, idx):
        choice = self.choices[idx]  # Let the IndexError propogate
        return DefaultGroupRadioInput(
            self.name, self.value, self.attrs.copy(), choice, idx)


class DefaultGroupRadioSelect(RadioSelect):
        renderer = DefaultGroupRadioFieldRenderer
