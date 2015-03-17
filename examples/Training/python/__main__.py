#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
#                    All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import os
training_dir = os.path.dirname(os.path.abspath(__file__))

if __name__ == "__main__":
        print "Running training suite"
        execfile(os.path.join(training_dir, 'Connect_To_OMERO.py'))
        execfile(os.path.join(training_dir, 'Create_Image.py'))
        execfile(os.path.join(training_dir, 'Delete.py'))
        execfile(os.path.join(training_dir, 'Filesets.py'))
        execfile(os.path.join(training_dir, 'Groups_Permissions.py'))
        execfile(os.path.join(training_dir, 'Metadata.py'))
        execfile(os.path.join(training_dir, 'Raw_Data_Access.py'))
        execfile(os.path.join(training_dir, 'Read_Data.py'))
        execfile(os.path.join(training_dir, 'Render_Images.py'))
        execfile(os.path.join(training_dir, 'ROIS.py'))
        execfile(os.path.join(training_dir, 'Tables.py'))
        execfile(os.path.join(training_dir, 'Write_Data.py'))
