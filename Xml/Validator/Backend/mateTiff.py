#!/usr/bin/env python
# encoding: utf-8
"""
mateTiff.py

Created by Andrew Patterson on 2007-09-14.
Copyright (c) 2007 OME Group. All rights reserved.
"""

import sys
import getopt
import Image


help_message = '''
This extracts the Description block [270] from the TIFF file and opens it 
in the mate editor
'''


class Usage(Exception):
	def __init__(self, msg):
		self.msg = msg


def main(argv=None):
	if argv is None:
		argv = sys.argv
	try:
		try:
			opts, args = getopt.getopt(argv[1:], "ho:v", ["help", "output="])
		except getopt.error, msg:
			raise Usage(msg)
	
		# option processing
		for option, value in opts:
			if option == "-v":
				verbose = True
			if option in ("-h", "--help"):
				raise Usage(help_message)
			if option in ("-o", "--output"):
				output = value
		
		if len(args) is 0:
			raise Usage(help_message)
		
		for aFilename in args:
			# open the tiff
			try:
				image = Image.open(aFilename)
			except IOError:
				raise Usage("InvalidFile(%s) Not recognised as Image file - file could not be read"%aFilename)
			else:
	            # check for the XML containing tag within the tiff
				if 270 not in image.tag.keys():
					raise Usage("InvalidTiff: Tiff file did not contain an ImageDescription Tag - no XML found")
				else:
				    # read the xml from the tiff
					theXml = image.tag[270]
					print "Processing file : %s\n" % aFilename
					print theXml
					print ""
		
		
	except Usage, err:
		print >> sys.stderr, sys.argv[0].split("/")[-1] + ": " + str(err.msg)
		print >> sys.stderr, "\t for help use --help"
		return 2


if __name__ == "__main__":
	sys.exit(main())
