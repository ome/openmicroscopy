#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
# #                Django settings for OMERO.web project.               # #
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
#
#
# Copyright (c) 2014 University of Dundee.
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


import json
from omeroweb.settings import process_custom_setting


class TestProcessSettings(object):

    def testDefault(self):
        CUSTOM_SETTINGS = {
        }
        value, action = process_custom_setting(
            CUSTOM_SETTINGS, 'test', 'TEST', 'test-value', str)
        assert 'test-value' == value
        assert 'default' == action

    def testCustom(self):
        CUSTOM_SETTINGS = {
            'test': 'custom-value',
        }
        value, action = process_custom_setting(
            CUSTOM_SETTINGS, 'test', 'TEST', 'test-value', str)
        assert 'custom-value' == value
        assert 'custom' == action

    def testExtendList(self):
        CUSTOM_SETTINGS = {
            'test.extend': '["b"]',
        }
        value, action = process_custom_setting(
            CUSTOM_SETTINGS, 'test', 'TEST', '["a"]', json.loads)
        assert ["a", "b"] == value
        assert 'default,extend' == action

    def testExtendDict(self):
        CUSTOM_SETTINGS = {
            'test.extend': '{"b":2}',
        }
        value, action = process_custom_setting(
            CUSTOM_SETTINGS, 'test', 'TEST', '{"a":1}', json.loads)
        assert {"a": 1, "b": 2} == value
        assert 'default,extend' == action

    def testExtendExistingList(self):
        CUSTOM_SETTINGS = {
            'test.extend': '["b", "c"]',
        }
        value, action = process_custom_setting(
            CUSTOM_SETTINGS, 'test', 'TEST', '["a", "c"]', json.loads)
        assert ["a", "c", "b"] == value
        assert 'default,extend' == action

    def testExtendExistingDict(self):
        CUSTOM_SETTINGS = {
            'test.extend': '{"b":2,"c":4}',
        }
        value, action = process_custom_setting(
            CUSTOM_SETTINGS, 'test', 'TEST', '{"a":1,"c":3}', json.loads)
        assert {"a": 1, "b": 2, "c": 4} == value
        assert 'default,extend' == action

    def testCustomExtendList(self):
        CUSTOM_SETTINGS = {
            'test': '["c"]',
            'test.extend': '["b"]',
        }
        value, action = process_custom_setting(
            CUSTOM_SETTINGS, 'test', 'TEST', '["a"]', json.loads)
        assert ["c", "b"] == value
        assert 'custom,extend' == action

    def testCustomExtendDict(self):
        CUSTOM_SETTINGS = {
            'test': '{"c": 3}',
            'test.extend': '{"b":2}',
        }
        value, action = process_custom_setting(
            CUSTOM_SETTINGS, 'test', 'TEST', '{"a":1}', json.loads)
        assert {"c": 3, "b": 2} == value
        assert 'custom,extend' == action

    def testRemoveExistingList(self):
        CUSTOM_SETTINGS = {
            'test.remove': '["b"]',
        }
        value, action = process_custom_setting(
            CUSTOM_SETTINGS, 'test', 'TEST', '["a","b"]', json.loads)
        assert ["a"] == value
        assert 'default,remove' == action

    def testRemoveNonexistingList(self):
        CUSTOM_SETTINGS = {
            'test.remove': '["b"]',
        }
        value, action = process_custom_setting(
            CUSTOM_SETTINGS, 'test', 'TEST', '["a","c"]', json.loads)
        assert ["a", "c"] == value
        assert 'default,remove' == action

    def testRemoveExistingDict(self):
        CUSTOM_SETTINGS = {
            'test.remove': '["b"]',
        }
        value, action = process_custom_setting(
            CUSTOM_SETTINGS, 'test', 'TEST', '{"a":1,"b":2}', json.loads)
        assert {"a": 1} == value
        assert 'default,remove' == action

    def testRemoveNonexistingDict(self):
        CUSTOM_SETTINGS = {
            'test.remove': '["b"]',
        }
        value, action = process_custom_setting(
            CUSTOM_SETTINGS, 'test', 'TEST', '{"a":1,"c":3}', json.loads)
        assert {"a": 1, "c": 3} == value
        assert 'default,remove' == action

    def testExtendRemoveList(self):
        CUSTOM_SETTINGS = {
            'test.remove': '["b"]',
            'test.extend': '["c"]',
        }
        value, action = process_custom_setting(
            CUSTOM_SETTINGS, 'test', 'TEST', '["a","b"]', json.loads)
        assert ["a","c"] == value
        assert 'default,extend,remove' == action

    def testExtendRemoveDict(self):
        CUSTOM_SETTINGS = {
            'test.remove': '["b"]',
            'test.extend': '{"c":3}',
        }
        value, action = process_custom_setting(
            CUSTOM_SETTINGS, 'test', 'TEST', '{"a":1,"b":2}', json.loads)
        assert {"a": 1, "c": 3} == value
        assert 'default,extend,remove' == action

    def testCustomExtendRemoveList(self):
        CUSTOM_SETTINGS = {
            'test': '["d", "e"]',
            'test.remove': '["e"]',
            'test.extend': '["c"]',
        }
        value, action = process_custom_setting(
            CUSTOM_SETTINGS, 'test', 'TEST', '["a","b"]', json.loads)
        assert ["d","c"] == value
        assert 'custom,extend,remove' == action

    def testCustomExtendRemoveDict(self):
        CUSTOM_SETTINGS = {
            'test': '{"d":4,"e":5}',
            'test.remove': '["e"]',
            'test.extend': '{"c":3}',
        }
        value, action = process_custom_setting(
            CUSTOM_SETTINGS, 'test', 'TEST', '{"a":1,"b":2}', json.loads)
        assert {"d": 4, "c": 3} == value
        assert 'custom,extend,remove' == action
