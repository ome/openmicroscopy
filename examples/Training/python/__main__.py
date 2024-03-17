#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2015-2017 University of Dundee & Open Microscopy Environment.
#                    All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import os
training_dir = os.path.dirname(os.path.abspath(__file__))

if __name__ == "__main__":
    print("Running training suite")
    exec(open(os.path.join(training_dir, 'Bulk_Shapes.py')).read())
    exec(open(os.path.join(training_dir, 'Connect_To_OMERO.py')).read())
    exec(open(os.path.join(training_dir, 'Create_Image.py')).read())
    exec(open(os.path.join(training_dir, 'Delete.py')).read())
    exec(open(os.path.join(training_dir, 'Filesets.py')).read())
    exec(open(os.path.join(training_dir, 'Groups_Permissions.py')).read())
    exec(open(os.path.join(training_dir, 'Metadata.py')).read())
    exec(open(os.path.join(training_dir, 'Raw_Data_Access.py')).read())
    exec(open(os.path.join(training_dir, 'Read_Data.py')).read())
    exec(open(os.path.join(training_dir, 'Render_Images.py')).read())
    exec(open(os.path.join(training_dir, 'ROIs.py')).read())
    exec(open(os.path.join(training_dir, 'Tables.py')).read())
    exec(open(os.path.join(training_dir, 'Write_Data.py')).read())
    exec(open(os.path.join(training_dir,
         'Advanced/Create_Image_advanced.py')).read())
    exec(open(os.path.join(training_dir,
         'Advanced/Raw_Data_advanced.py')).read())
    exec(open(os.path.join(training_dir,
         'Advanced/Read_Data_advanced.py')).read())
    exec(open(os.path.join(training_dir,
         'Advanced/Write_data_advanced.py')).read())
    exec(open(os.path.join(training_dir,
         'Task_Scripts/Raw_Data2.py')).read())
    exec(open(os.path.join(training_dir,
         'Task_Scripts/Raw_Data_Task.py')).read())
    exec(open(os.path.join(training_dir,
         'Task_Scripts/Write_Data_4.py')).read())
    exec(open(os.path.join(training_dir,
         'Task_Scripts/Write_Data_3.py')).read())
    exec(open(os.path.join(training_dir, 'Json_Api/Login.py')).read())
