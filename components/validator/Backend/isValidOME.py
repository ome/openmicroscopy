#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
isValidOME.py

Created by Andrew Patterson on 2007-09-20.
Copyright (C) 2007-2012 University of Dundee & Open Microscopy Environment.
All Rights Reserved.
"""

import sys
import getopt
import OmeValidator

help_message = '''
isValidOME.py validates each file on it's command line.
Files with a .tif or .tiff extension are treadet as TIFF images - all other
as ome-xml. Each file is listed as OK or Invalid.
If the -v option is given a full report is printed for each file and it the
file is a tiff the XML found is also printed.
'''


class Usage(Exception):
    def __init__(self, msg):
        self.msg = msg


def main(argv=None):
    if argv is None:
        argv = sys.argv
    try:
        try:
            opts, args = getopt.getopt(argv[1:], "ho:vew", ["help", "output="])
        except getopt.error, msg:
            raise Usage(msg)

        verbose = False
        warnings = False
        errors = False
        # option processing
        for option, value in opts:
            if option == "-v":
                verbose = True
            if option == "-w":
                warnings = True
            if option == "-e":
                errors = True
            if option in ("-h", "--help"):
                raise Usage(help_message)

        if len(args) is 0:
            raise Usage(help_message)

        for aFilename in args:
            # validate the file
            if verbose is True:
                print "============ XML file %s ============ " % aFilename

            if (aFilename[-4:].lower() == ".tif" or
                    aFilename[-5:].lower() == ".tiff"):
                report = OmeValidator.XmlReport.validateTiff(aFilename)
            else:
                report = OmeValidator.XmlReport.validateFile(aFilename)
            if report.isXsdValid is True:
                print "File OK: %s" % aFilename
            else:
                print "File Invalid: %s" % aFilename

            if verbose is True:
                print report
                if report.isOmeTiff:
                    print "============ XML block %s [formatted]===========" \
                        "= " % aFilename
                    print report.theDom.toprettyxml()
                    print "============ XML block %s [raw]============ " \
                        % aFilename
                    print report.theDom.toxml()
            else:
                out = str()
                if errors and len(report.errorList) > 0:
                    out = out + "Errors:\n"
                    for error in report.errorList:
                        out = out + str(error)
                if warnings and len(report.warningList) > 0:
                    out = out + "Warnings:\n"
                    for warning in report.warningList:
                        out = out + str(warning)

                if len(out) > 0:
                    print out

    except Usage, err:
        print >> sys.stderr, sys.argv[0].split("/")[-1] + ": " + str(err.msg)
        print >> sys.stderr, "\t for help use --help"
        return 2


if __name__ == "__main__":
    sys.exit(main())
