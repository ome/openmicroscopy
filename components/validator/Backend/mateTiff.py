#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
mateTiff.py

Created by Andrew Patterson on 2007-09-14.
Copyright (C) 2007-2012 University of Dundee & Open Microscopy Environment.
All Rights Reserved.
"""

import sys
import os
import tempfile
import getopt
import Image


help_message = '''
This extracts the Description block [270] from the TIFF file and opens it
in the mate editor then writes it back
'''


class Usage(Exception):
    def __init__(self, msg):
        self.msg = msg


def main(argv=None):
    print "At present this destroys multi plane TIFF files - kill process" \
        " to stop changes being applied"
    if argv is None:
        argv = sys.argv
    try:
        try:
            opts, args = getopt.getopt(argv[1:], "ho:v", ["help", "output="])
        except getopt.error, msg:
            raise Usage(msg)

        # option processing
        for option, value in opts:
            # if option == "-v":
            #     verbose = True
            if option in ("-h", "--help"):
                raise Usage(help_message)
            # if option in ("-o", "--output"):
            #     output = value

        if len(args) is 0:
            raise Usage(help_message)

        for aFilename in args:
            # open the tiff
            try:
                image = Image.open(aFilename)
            except IOError:
                raise Usage("InvalidFile(%s) Not recognised as Image file -"
                            " file could not be read" % aFilename)
            else:
                # check for the XML containing tag within the tiff
                if 270 not in image.tag.keys():
                    raise Usage("InvalidTiff: Tiff file did not contain an"
                                " ImageDescription Tag - no XML found")
                else:
                    # read the xml from the tiff
                    theXml = image.tag[270]
                    theNewXml = edited_text(theXml)
                    write_tag_into_tiff(aFilename, theNewXml)

    except Usage, err:
        print >> sys.stderr, sys.argv[0].split("/")[-1] + ": " + str(err.msg)
        print >> sys.stderr, "\t for help use --help"
        return 2

    ''' From Python Cookbook [#10.6]'''
    ''' Edited as original version would not work for an editor that had an \
argument e.g. "mate -w" '''


def what_editor():
    editor = os.getenv('VISUAL') or os.getenv('EDITOR')
    if not editor:
        if sys.playform == 'windows':
            editor = 'Notepad.Exe'
        else:
            editor = 'vi'
    return editor


def edited_text(starting_text=''):
    temp_fd, temp_filename = tempfile.mkstemp(text=True)
    os.write(temp_fd, starting_text)
    os.close(temp_fd)
    # editor = what_editor()
    # x = os.spawnlp(os.P_WAIT, editor, editor, temp_filename)
    x = os.spawnlp(os.P_WAIT, "mate", "mate", "-w", temp_filename)
    if x:
        raise RuntimeError("Can't run %s %s (%s)"
                           % ("mate", temp_filename, x))
    result = open(temp_filename).read()
    os.unlink(temp_filename)
    return result


def write_tag_into_tiff(inTiffFilename, inXmlString):
    temp_fd, temp_filename = tempfile.mkstemp(text=True)
    os.write(temp_fd, inXmlString)
    os.close(temp_fd)
    x = os.spawnlp(os.P_WAIT, "java", "java", "EditTiff", inTiffFilename,
                   temp_filename)
    if x:
        raise RuntimeError("Can't run java EditTiff %s %s (%s)"
                           % (inTiffFilename, temp_filename, x))
    result = open(temp_filename).read()
    os.unlink(temp_filename)
    return result

if __name__ == "__main__":
    sys.exit(main())
