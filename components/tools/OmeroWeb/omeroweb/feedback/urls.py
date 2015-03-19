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

from django.conf.urls import url, patterns
from django.views.generic import TemplateView

from omeroweb.feedback import views

# url patterns
urlpatterns = patterns(
    '',
    url(r'^feedback/', views.send_feedback, name="fsend"),
    url(r'^comment/', views.send_comment, name="csend"),
    url(r'^thanks/', TemplateView.as_view(template_name='thanks.html'),
        name="fthanks"),
)
