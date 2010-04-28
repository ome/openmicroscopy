from selenium import selenium
import unittest, time, urllib2, cookielib

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
            self.selenium = selenium(self.host, self.port, self.browser, self.url)
            self.selenium.start()
            self.selenium.open("/%s/" % self.base)
            script = urllib2.urlopen("%s/appmedia/webgateway/js/3rdparty/jquery-1.3.2.js" % (self.url))
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

class SeleniumTestBase (unittest.TestCase):
    SERVER = SeleniumTestServer()

    def setUp (self):
        self.verificationErrors = []
        self.selenium = self.SERVER.getSelenium()

#    def waitOnFullViewerLoad (self):
#        time.sleep(1)
#        for i in range(20):
#            try:
#                if (not self.selenium.is_element_present("//div[@id='wblitz-workarea']/div[@class='box']/div[contains(@style,'none')]")) and self.selenium.get_text('wblitz-z-count') != '?': break
#            except: pass
#            time.sleep(1)
#        else: self.fail("time out")
#        time.sleep(1)

    def waitForElementPresence (self, element, present=True):
        """ waits for 60 seconds for a particular element to be present (or not).
        Also exits if an alert box comes up """
        for i in range(60):
            try:
                if self.selenium.is_element_present(element) == present: break
            except: pass
            time.sleep(1)
        else: self.fail("time out")

    def waitForElementVisibility (self, element, visible):
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
