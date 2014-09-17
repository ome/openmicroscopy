#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Test custom modifications to path.py.

Copyright 2014 CRS4. All rights reserved.
Use is subject to license terms supplied in LICENSE.txt.
"""

import path


class TestPath(object):

    def test_parpath(self):
        root = path.path('/')
        a1, a2 = [root / _ for _ in 'a1', 'a2']
        b = a1 / 'b'
        for x, y in (root, a1), (root, a2), (a1, b):
            assert len(y.parpath(x)) == 1
            assert len(x.parpath(y)) == 0
        assert len(a1.parpath(a2)) == 0
        assert len(a2.parpath(a1)) == 0
        assert len(b.parpath(root)) == 2
        assert len(root.parpath(b)) == 0
