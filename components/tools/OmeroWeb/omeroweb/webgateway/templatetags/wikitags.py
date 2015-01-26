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

from django.template import Library
from django.templatetags.static import PrefixNode

register = Library()


@register.filter
def wikify(value):
    if value is not None:

        urlier = (r'(http[s]?://|localhost|ftp://|ftps://)'
                  '(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\(\)\|,]|'
                  '(?:%[0-9a-fA-F][0-9a-fA-F]))+')
        wikifier = re.compile(r'\b(%s)\b' % urlier, re.IGNORECASE)
        value = wikifier.sub(r'<a href="\1" target="_blank">\1</a>', value)

        return value

    return value

# happy                :) :-)
# wink                 ;) ;-)
# big smile            :-D :D
# tongue sticking out  :-P :P :-p :p
# surprised / o, no    :-o :O
# sad                  :( :-(
# very sad             :-(( :((
# embarrassed          :">
# wearing sunglasses   B-)
# kiss                 =* :-* :*
# confused             :-/ :-\
# angry                X-( x-( X( x(


@register.filter
def sharewikify(value):
    if value is not None:

        WIKI_WORD = (r'http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\(\)\|,]|'
                     '(?:%[0-9a-fA-F][0-9a-fA-F]))+')
        wikifier = re.compile(r'\b(%s)\b' % WIKI_WORD)
        value = wikifier.sub(r'<a href="\1" target="_blank">\1</a>', value)

        # happy :) :-)
        emot1 = re.compile(r'\:[\-]?\)', re.VERBOSE)
        value = emot1.sub(
            r'<img src="%simage/emots/tinymce_smiley-smile18.gif" />'
            % PrefixNode.handle_simple("STATIC_URL"), value)

        # wink ;) ;-)
        emot11 = re.compile(r'\;[\-]?\)', re.VERBOSE)
        value = emot11.sub(
            r'<img src="%simage/emots/tinymce_smiley-wink18.gif" />'
            % PrefixNode.handle_simple("STATIC_URL"), value)

        # very sad :-(( :((
        emot22 = re.compile(r'\:[\-]?\(\(', re.VERBOSE)
        value = emot22.sub(
            r'<img src="%simage/emots/tinymce_smiley-cry18.gif" />'
            % PrefixNode.handle_simple("STATIC_URL"), value)

        # sad :( :-(
        emot2 = re.compile(r'\:[\-]?\(', re.VERBOSE)
        value = emot2.sub(
            r'<img src="%simage/emots/tinymce_smiley-frown18.gif" />'
            % PrefixNode.handle_simple("STATIC_URL"), value)

        # kiss =* :-* :*
        emot3 = re.compile(r'[=\:][\-]?\*', re.VERBOSE)
        value = emot3.sub(
            r'<img src="%simage/emots/tinymce_smiley-kiss18.gif" />'
            % PrefixNode.handle_simple("STATIC_URL"), value)

        # big smile :-D :D
        emot4 = re.compile(r'\:[\-]?[dD]', re.VERBOSE)
        value = emot4.sub(
            r'<img src="%simage/emots/tinymce_smiley-laughing18.gif" />'
            % PrefixNode.handle_simple("STATIC_URL"), value)

        # tongue sticking out :-P :P :-p :p
        emot5 = re.compile(r'\:[\-]?[pP]', re.VERBOSE)
        value = emot5.sub(
            r'<img src="%simage/emots/tinymce_smiley-tongue-out18.gif" />'
            % PrefixNode.handle_simple("STATIC_URL"), value)

        # surprised / o, no :-o :O
        emot6 = re.compile(r'\:[\-]?[oO]', re.VERBOSE)
        value = emot6.sub(
            r'<img src="%simage/emots/tinymce_smiley-surprised18.gif" />'
            % PrefixNode.handle_simple("STATIC_URL"), value)

        # embarrassed :">
        emot7 = re.compile(r'\:\"\>', re.VERBOSE)
        value = emot7.sub(
            r'<img src="%simage/emots/tinymce_smiley-embarassed18.gif" />'
            % PrefixNode.handle_simple("STATIC_URL"), value)

        # wearing sunglasses B-)
        emot8 = re.compile(r'B\-\)', re.VERBOSE)
        value = emot8.sub(
            r'<img src="%simage/emots/tinymce_smiley-cool18.gif" />'
            % PrefixNode.handle_simple("STATIC_URL"), value)

        # confused :-/ :-\
        emot9 = re.compile(r'\:\-[\\/]', re.VERBOSE)
        value = emot9.sub(
            r'<img src="%simage/emots/tinymce_smiley-undecided18.gif" />'
            % PrefixNode.handle_simple("STATIC_URL"), value)

        # angry X-( x-( X( x(
        emot9 = re.compile(r'[xX][\-]?\(', re.VERBOSE)
        value = emot9.sub(
            r'<img src="%simage/emots/tinymce_smiley-yell18.gif" />'
            % PrefixNode.handle_simple("STATIC_URL"), value)

        # TODO: Beta 4.1
        # quote = re.compile(r'\[quote\](.*)\[/quote\]', re.DOTALL)
        # value = quote.sub(r'<p class="quote">\1</p>', value)

    return value

# happy                :) :-)
# wink                 ;) ;-)
# big smile            :-D :D
# tongue sticking out  :-P :P :-p :p
# surprised / o, no    :-o :O
# sad                  :( :-(
# very sad             :-(( :((
# embarrassed          :">
# wearing sunglasses   B-)
# kiss                 =* :-* :*
# confused             :-/ :-\
# angry                X-( x-( X( x(
