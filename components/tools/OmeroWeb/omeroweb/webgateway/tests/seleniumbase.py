#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# webgateway/tests/seleniumbase.py - Helpers to implement selenium tests
# 
# Copyright (c) 2010 Glencoe Software, Inc. All rights reserved.
# 
# This software is distributed under the terms described by the LICENCE file
# you can find at the root of the distribution bundle, which states you are
# free to use it only for non commercial purposes.
# If the file is missing please request a copy by contacting
# jason@glencoesoftware.com.
#
# Author: Carlos Neves <carlos(at)glencoesoftware.com>
""" 
Base class and utils to ease implementation of selenium tests.

To use this, create a file called C{seleniumtests.py} inside your app C{tests} folder,
and include the following template::

  from omeroweb.webgateway.tests.seleniumbase import SeleniumTestBase, Utils
  
  class MyTests (SeleniumTestBase):
      def runTest (self):
          " Implement your tests here "
  
  if __name__ == "__main__":
      Utils.runAsScript('MyDjangoAppURLPrefix')

Of course you'll need to replace C{MyDjangoAppURLPrefix} with a real value, and you
can implement tests in any way you want, so as long as unittest can run them.

By extending L{SeleniumTestBase} you get an instance variable names C{selenium} which
has the selenium RC client connected.

After all this is done, and assuming you have a standard C{Omero} installation, you'll
be able to issue:

  C{omero web seleniumtest MyDjangoApp seleniumserver.host http://omeroweb.host:port firefox}

The values above are just examples.
""" 
__docformat__='epytext' 

from selenium import selenium
import unittest, time, urllib2

class SeleniumTestServer (object):
    def __init__ (self, base='webgateway', url='http://localhost:8000', host='localhost', port=4444, browser='*firefox'):
        self.base = base
        self.host = host
        self.port = port
        self.browser = browser
        self.url = url
        self.selenium = None

    def __del__ (self):
        if self.selenium is not None:
            self.selenium.stop()

    def getSelenium (self):
        if self.selenium is None:
            if self.host.find(':') >= 0:
                host = self.host.split(':')
                try:
                    self.port = int(host[1])
                    if host[0] != '':
                        self.host = host[0]
                except:
                    pass
            self.selenium = selenium(self.host, self.port, self.browser, self.url)
        self.selenium.start()
        self.selenium.open("/%s" % self.base)
        script = urllib2.urlopen("%s/static/3rdparty/jquery-1.7.2.js" % (self.url))
        r = script.read()
        self.selenium.run_script(r)
        script.close()
        self.selenium.add_location_strategy("jquery",
'''
  if (inWindow.jQuery.expr[':'].containsExactly === undefined) {
    inWindow.jQuery.expr[':'].containsExactly = function (a,i,m) { return inWindow.jQuery(a).text() == m[3];};
  }
  var found = inWindow.jQuery(inDocument).find(locator);
  if(found.length >= 1 ){
    return found.get(0);
  }else{
    return null;
  }
''')
        return self.selenium
        
class Utils:
    @staticmethod
    def flattenHTMLStyleColor (color):
        """ Takes a color in rgb() or #ABC or #AABBCC format and returns #AABBCC. 
        There may be a prefix from the style attribute.
        TODO: support multiple style attrs, find the correct one."""
        if color.endswith(';'):
            color = color[:-1]
        if color.find(':') >= 0:
            color = color.split(':')[-1].strip()
        if color.startswith('rgb('):
            r,g,b = [int(x) for x in color[4:-1].split(',')]
        else:
            if color.startswith('#'):
                color = color[1:]
            if len(color) == 3:
                r,g,b = color[0]*2,color[1]*2,color[2]*2
            elif len(color) == 6:
                r,g,b = color[0:2],color[2:4],color[4:6]
            else:
                return '#' + color
            r,g,b = [int(x, 16) for x in (r,g,b)]
        return '#%0.2X%0.2X%0.2X' % (r,g,b)

    @staticmethod
    def runAsScript (base='webgateway'):
        import sys
        SeleniumTestBase.SERVER = SeleniumTestServer(base)
        #print sys.argv
        if len(sys.argv) > 1:
            SeleniumTestBase.SERVER.host = sys.argv.pop(1)
        if len(sys.argv) > 1:
            SeleniumTestBase.SERVER.url = sys.argv.pop(1)
        if len(sys.argv) > 1:
            SeleniumTestBase.SERVER.browser = sys.argv.pop(1)
        unittest.main()

class SeleniumTestBase (unittest.TestCase):
    """
    The base class for selenium tests.

    All tests will have the C{self.selenium} attr with a selenium client ready for usage.
    """
    SERVER = None

    def setUp (self):
        self.verificationErrors = []
        if self.SERVER is None:
            self.SERVER = SeleniumTestServer()
        self.selenium = self.SERVER.getSelenium()

    def waitForElementPresence (self, element, present=True):
        """
        Waits for 60 seconds for a particular element to be present (or not).
        Also exits if an alert box comes up.
        """
        for i in range(60):
            try:
                if self.selenium.is_element_present(element) == present: break
            except: pass
            time.sleep(1)
        else: self.fail("time out")

    def waitForElementVisibility (self, element, visible):
        """
        Waits for 60 seconds for a particular element to be visible (or not).
        """
        for i in range(60):
            try:
                if self.selenium.is_visible(element) == visible: break
            except:
                if not visible:
                    break
            time.sleep(1)
        else: self.fail("time out")

    def tearDown(self):
        self.selenium.stop()
        self.assertEqual([], self.verificationErrors)
