#!/usr/bin/env python
# encoding: utf-8
"""
RunAndCheck.py

Created by Andrew Patterson on 2007-07-30.
Copyright (c) 2007 __MyCompanyName__. All rights reserved.
"""

import sys
import os
import pychecker.checker

import OmeValidator

def main():
	print OmeValidator.XmlReport.validateTiff("samples/claire-multi-channel-4D-series.ome.tif")
	"""
	for aFilename in ["samples/completesamplenopre.xml","samples/completesample.xml",
		"samples/sdub.ome", "samples/sdub-fix.ome", "samples/sdub-fix-pre.ome", 
		"samples/tiny.ome", "samples/broke.ome"]:
		print "============ XML file %s ============ " % aFilename
		print OmeValidator.XmlReport.validateFile(aFilename)
		
	for aFilename in ["samples/4d2wOME.tif", "samples/4d2wOME-fixed.tif",
	 	"samples/4d2wOME-fixed-updated.tif", "samples/blank.tif", 
		"samples/ome.xsd"]:
		print "============ XML file %s ============ " % aFilename
		print OmeValidator.XmlReport.validateTiff(aFilename)
	"""
	print "============"

if __name__ == '__main__':
	main()

