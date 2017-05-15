#!/usr/bin/env python
# -*- coding: utf-8 -*-

import pytest


@pytest.mark.junit
class TestMethodFailClass(object):

    def testMethodFailMethod(self):
        UL = []
        UL._non_atrrib

        assert True
