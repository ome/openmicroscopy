#!/usr/bin/env python

"""
   Tests the sudo/var functionality of bin/omero

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from omero.util.temp_files import create_path
import subprocess
import unittest
import signal
import path
import time
import os

orig = path.path(".") / "bin" / "omero"

def has_sudo():
    """
    This tests if we have password-less sudo
    """
    proc = subprocess.Popen(["sudo","-S","sh","-c","true"], stdin=subprocess.PIPE)
    proc.communicate("***\n"*10)

    rc = None
    start = time.time()
    while rc is None and (time.time() - start) < 1:
        rc = proc.poll()
        time.sleep(0.1)

    # Looks like we have sudo, EARLY EXIT
    if rc is not None and rc == 0:
        return True

    try:
        os.kill(rc.pid, signal.SIGKILL)
    except:
        print "Error on kill process"

    return False


if not has_sudo():
    print "NO SUDO"

else:

    class TestBin(unittest.TestCase):

        def preMethod(self):
            self.dir = create_path("bin_test", folder=True)
            self.bin = self.dir / "bin"
            self.bin.makedirs()
            self.omero = self.bin / "omero"
            self.omero.write_text( orig.text() )
            self.exe = """sh -c "python '%s' '' " """ % self.omero.abspath()
            self.mkdir = """sudo sh -c "mkdir '%s/var'" """ % self.dir.abspath()     # Always sudo
            self.chmod = """sudo sh -c "chmod %%s '%s/var'" """ % self.dir.abspath() # "

        def testSudoFails(self):
            self.preMethod()
            self.assertFalse(0 == os.system("sudo %s" % self.exe))

        def testVarIsCreated(self):
            self.preMethod()
            v = self.dir / "var"
            self.assertFalse(v.exists())
            os.system(self.exe)
            self.assertTrue(v.exists())

        def testVarIsNotReadable(self):
            self.preMethod()
            os.system(self.mkdir)
            os.system(self.chmod % "700")
            self.assertFalse(0 == os.system(self.exe))

        def testVarIsNotWriteable(self):
            self.preMethod()
            os.system(self.mkdir)
            os.system(self.chmod % "740")
            self.assertFalse(0 == os.system(self.exe))

if __name__ == '__main__':
    unittest.main()
