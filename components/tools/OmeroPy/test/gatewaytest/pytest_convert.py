#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   pytest_convert.py - Simple script to help in refactoring tests for pytest

   Copyright 2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import re
import sys
import os


filtered_lines = (
    'import unittest',
    'import gatewaytest.library as lib',
    "if __name__ == '__main__':",
    'unittest.main()',
    )

simple_replace = (
    ('lib.GTest', 'object'),
    ('self.login', 'gatewaywrapper.login'),
    ('self.getTest', 'gatewaywrapper.getTest'),
    ('self.gateway', 'gatewaywrapper.gateway'),
    ('self.USER', 'gatewaywrapper.USER'),
    ('self.AUTHOR', 'gatewaywrapper.AUTHOR'),
    ('self.assertRaises', 'pytest.raises'),
    ('self.doLogin', 'gatewaywrapper.doLogin')
    )

regex_replace = (
    (re.compile(r'self.assertEqual\(([^,]*),(.*)\)$'), r'assert \1 == \2'),
    (re.compile(r'self.assertNotEqual\(([^,]*),(.*)\)$'), r'assert \1 != \2'),
    (re.compile(r'self.assertTrue\((.*)\)$'), r'assert \1'),
    (re.compile(r'self.assertFalse\((.*)\)$'), r'assert not \1'),
    (re.compile(r'self.assert_\((.*)\)$'), r'assert \1'),
    (re.compile(r'class (.*)Test \('), r'class Test\1 ('),
    )

if __name__ == '__main__':
    for fn in sys.argv[1:]:
        ofn = 'test_' + fn
        if os.path.exists(ofn):
            raise ValueError('Already exists: %s' % ofn)
        outf = file(ofn, 'w')
        with file(fn, 'r') as f:
            for l in f.readlines():
                if l.strip() in filtered_lines:
                    continue
                for srfrom, srto in simple_replace:
                    pos = l.find(srfrom)
                    while pos > -1:
                        l = l[:pos] + srto + l[pos+len(srfrom):]
                        pos = l.find(srfrom)
                for refrom, reto in regex_replace:
                    l = refrom.sub(reto, l)
                outf.write(l)
        outf.close()
