#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
/*
 *   $Id$
 *
 *   Copyright 2010-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
"""

import omero_ext.uuid as uuid  # see ticket:3774

DUMMY = object()


class TestExt(object):

    def testUuid4(self):
        """
        Tests that on Mac platforms after 10.5 our use of omero_ext.uuid
        ensures that the broken functions _uuid_generate_{random,time}
        are nulled. In newer versions of Python, this will be the case
        as soon as the uuid moduly is loaded and so we fake their being
        non-null by setting the value to DUMMY and reloading omero_ext.uuid.

        On non Mac systems, this test does more or less nothing.

        See http://trac.openmicroscopy.org.uk/ome/ticket/3774
        """
        import sys
        if sys.platform == 'darwin':
            import os
            if int(os.uname()[2].split('.')[0]) >= 9:
                # uuid.__uuid__ is the original module used.
                U = uuid.__uuid__
                U._uuid_generate_random = U._uuid_generate_time = DUMMY
                reload(uuid)
                assert None == U._uuid_generate_random
                assert None == U._uuid_generate_time
        assert uuid.uuid4()
