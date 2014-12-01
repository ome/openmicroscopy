#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
RunAndCheck.py

Created by Andrew Patterson on 2007-07-30.
Copyright (C) 2007-2012 University of Dundee & Open Microscopy Environment.
All Rights Reserved.
"""

import OmeValidator


def main():
    for aFilename in [
            "samples/completesamplenopre.xml", "samples/completesample.xml",
            "samples/sdub.ome", "samples/sdub-fix.ome",
            "samples/sdub-fix-pre.ome", "samples/2010-04.ome",
            "samples/2010-06.ome", "samples/tiny.ome", "samples/broke.ome"]:
        print "============ XML file %s ============ " % aFilename
        print OmeValidator.XmlReport.validateFile(aFilename)

    for aFilename in [
            "samples/4d2wOME.tif", "samples/4d2wOME-fixed.tif",
            "samples/4d2wOME-fixed-updated.tif", "samples/blank.tif",
            "samples/2010-06.ome.tiff", "samples/ome.xsd"]:
        print "============ XML file %s ============ " % aFilename
        print OmeValidator.XmlReport.validateTiff(aFilename)
    print "============"

if __name__ == '__main__':
    main()
