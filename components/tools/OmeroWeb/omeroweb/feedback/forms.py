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
# Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>
#
# Version: 1.0
#

from django import forms


class ErrorForm(forms.Form):

    email = forms.CharField(
        max_length=250, widget=forms.TextInput(attrs={'size': 40}),
        label="Your email", required=False)
    comment = forms.CharField(
        widget=forms.Textarea(attrs={'rows': 5, 'cols': 60}), required=False)
    error = forms.CharField(
        widget=forms.Textarea(attrs={'rows': 10, 'cols': 60}))


class CommentForm(forms.Form):

    email = forms.CharField(
        max_length=250, widget=forms.TextInput(attrs={'size': 40}),
        label="Your email", required=False)
    comment = forms.CharField(
        widget=forms.Textarea(attrs={'rows': 5, 'cols': 120}))
