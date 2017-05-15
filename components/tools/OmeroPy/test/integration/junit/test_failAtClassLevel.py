#!/usr/bin/env python
# -*- coding: utf-8 -*-

import pytest


@pytest.mark.junit
class TestFailClassClass(object):

    UL = []
    UL._non_atrrib

    def testFailClassMethod(self):
        assert True
