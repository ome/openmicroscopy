#!/usr/bin/env python

import os
import platform
import subprocess
WINDOWS = platform.system() == "Windows"
###########################################################################
# CONFIGURATION
###########################################################################

def DEFINE(key, value):
    m = globals()
    m[key] = os.environ.get(key, value)

# Most likely to be changed
if WINDOWS:
    DEFINE("NAME", "win-2k8")
    DEFINE("ADDRESS", "bp.openmicroscopy.org.uk")
    DEFINE("MEM", "Xmx1024M")
    DEFINE("UNZIP", "C:\\Program Files (x86)\\7-Zip\\7z.exe")
    DEFINE("UNZIPARGS", "x")
else:
    DEFINE("NAME", "gretzky")
    DEFINE("ADDRESS", "gretzky.openmicroscopy.org.uk")
    DEFINE("MEM", "Xmx1024M")
    DEFINE("UNZIP", "unzip")
    DEFINE("UNZIPARGS", "")

# Also run detection here, since these values are
# re-used during DEFINITION
p = subprocess.Popen(["hostname"], stdout=subprocess.PIPE)
hostname = p.communicate()[0].strip()

# If this is the default linux situation, then we want
# to also handle howe. Other servers will need to be
# handled better later.
if NAME == "gretzky" and NAME != hostname:
    if hostname == "howe":
        print "Detected hostname == 'howe'"
        DEFINE("NAME", "howe")
        DEFINE("ADDRESS", "howe.openmicroscopy.org.uk")
    else:
        print "Setting hostname to '%s'" % hostname
        DEFINE("NAME", hostname)
        DEFINE("ADDRESS", "localhost")

# new_server.py
DEFINE("SYM", "OMERO-CURRENT")
DEFINE("CFG", os.path.join(os.path.expanduser("~"), "config.xml"))
DEFINE("WEB", """'[["localhost", 4064, "%s"], ["gretzky.openmicroscopy.org.uk", 4064, "gretzky"], ["howe.openmicroscopy.org.uk", 4064, "howe"]]'""" % NAME)

# send_email.py
DEFINE("SUBJECT", "OMERO - %s was upgraded" % NAME)
DEFINE("BRANCH", "OMERO-trunk")
DEFINE("BUILD", "http://hudson.openmicroscopy.org.uk/job/%s/lastSuccessfulBuild/" % BRANCH)
DEFINE("SENDER", "Chris Allan <callan@lifesci.dundee.ac.uk>")
DEFINE("RECIPIENTS", ["ome-nitpick@lists.openmicroscopy.org.uk"])
DEFINE("SERVER", "%s (%s)" % (NAME, ADDRESS))
DEFINE("SMTP_SERVER", "smtp.dundee.ac.uk")
DEFINE("WEBURL", "http://%s/omero/webclient/" % ADDRESS)

DEFINE("SKIPWEB", "false")
DEFINE("SKIPEMAIL", "false")
DEFINE("SKIPUNZIP", "false")
###########################################################################

import fileinput
import smtplib
import sys
import urllib
import re

from email.MIMEMultipart import MIMEMultipart
from email.MIMEText import MIMEText
from email.MIMEImage import MIMEImage

from zipfile import ZipFile

try:
    from xml.etree.ElementTree import XML, ElementTree, tostring
except ImportError:
    from elementtree.ElementTree import XML, ElementTree, tostring


class Artifacts(object):

    def __init__(self, build = BUILD):
        self.server = None
        self.win = list()
        self.mac = list()
        self.linux = list()

        url = urllib.urlopen(build+"api/xml")
        hudson_xml = url.read()
        url.close()

        root = XML(hudson_xml)

        artifacts = root.findall("./artifact")
        base_url = build+"artifact/"
        if len(artifacts) <= 0:
            raise AttributeError("No artifacts, please check build on Hudson.")
        for artifact in artifacts:
            filename = artifact.find("fileName").text

            if filename.startswith("OMERO.server"):
                self.server = base_url + artifact.find("relativePath").text
            elif filename.startswith('OMERO.source'):
                self.source = base_url + artifact.find("relativePath").text
            elif filename.startswith('OMERO.imagej') or\
                 filename.startswith('OMERO.java') or\
                 filename.startswith('OMERO.matlab') or\
                 filename.startswith('OMERO.py') or\
                 filename.startswith('OMERO.server'):
                pass
            elif filename.startswith("OMERO.importer"):
                regex = re.compile(r'.*win.zip')
                regex2 = re.compile(r'.*mac.zip')
                if not regex.match(filename) and not regex2.match(filename):
                    self.linux.append(base_url + artifact.find("relativePath").text)
            else:
                regex = re.compile(r'.*win.zip')
                if regex.match(filename):
                    self.win.append(base_url + artifact.find("relativePath").text)

                regex = re.compile(r'.*OSX.zip')
                if regex.match(filename):
                    self.mac.append(base_url + artifact.find("relativePath").text)

                regex = re.compile(r'.*mac.zip')
                if regex.match(filename):
                    self.mac.append(base_url + artifact.find("relativePath").text)

                regex = re.compile(r'.*b\d+.zip')
                if regex.match(filename):
                    self.linux.append(base_url + artifact.find("relativePath").text)

    def download_server(self):

        if self.server == None:
            raise Exception("No server found")

        filename = os.path.basename(self.server)
        unzipped = filename.replace(".zip", "")

        if os.path.exists(unzipped):
            return unzipped

        if not os.path.exists(filename):
            print "Downloading %s..." % self.server
            urllib.urlretrieve(self.server, filename)

        if "false" == SKIPUNZIP.lower():
            if UNZIPARGS:
                command = [UNZIP, UNZIPARGS, filename]
            else:
                command = [UNZIP, filename]
            p = subprocess.Popen(command)
            rc = p.wait()
            if rc != 0:
                print "Couldn't unzip!"
            else:
                return unzipped

        print "Unzip and run again"
        sys.exit(0)


class Email(object):

    def __init__(self, artifacts, server = SERVER,\
            sender = SENDER, recipients = RECIPIENTS,\
            weburl = WEBURL, subject = SUBJECT,\
            smtp_server = SMTP_SERVER):

        TO = ",".join(recipients)
        FROM = sender
        text = "The OMERO.server on %s has been upgraded. \n" \
                    "=========================\n" \
                    "THIS SERVER REQUIRES VPN!\n" \
                    "=========================\n" \
                    "Please download suitable clients from: \n " \
                    "\n - Windows: \n %s\n " \
                    "\n - MAC: \n %s\n " \
                    "\n - Linux: \n %s\n " \
                    "\n - Webclient available on %s. \n \n " %\
                    (server, "\n".join(artifacts.win), "\n".join(artifacts.mac), "\
                    \n".join(artifacts.linux), weburl)
        BODY = "\r\n".join((
                "From: %s" % FROM,
                "To: %s" % TO,
                "Subject: %s" % subject,
                "",
                text))
        server = smtplib.SMTP(smtp_server)
        server.sendmail(FROM, recipients, BODY)
        server.quit()

        print "Mail was sent to: %s" % ",".join(recipients)


class Upgrade(object):

    def __init__(self, dir, cfg = CFG, mem = MEM, sym = SYM, skipweb = SKIPWEB):

        print "%s: Upgrading %s (%s)..." % (self.__class__.__name__, dir, sym)

        self.mem = mem
        self.sym = sym
        self.skipweb = skipweb

        _ = self.set_cli(self.sym)

        # Need lib/python set above
        import path
        self.cfg = path.path(cfg)
        self.dir = path.path(dir)

        self.stop(_)
        self.configure(_)
        self.directories(_)
        self.start(_)

    def stop(self, _):
        import omero
        try:
            print "Stopping server..."
            _("admin status --nodeonly")
            _("admin stop")
        except omero.cli.NonZeroReturnCode:
            pass

        if self.web():
            print "Stopping web..."
            self.stopweb(_)

    def configure(self, _):

        target = self.dir / "etc" / "grid" / "config.xml"
        if target.exists():
            print "Target %s already exists. Skipping..." % target
            return # Early exit!

        if not self.cfg.exists():
            print "%s not found. Copying old files" % self.cfg
            from path import path
            old_grid = path("OMERO-CURRENT") / "etc" / "grid"
            old_cfg = old_grid / "config.xml"
            old_cfg.copy(target)
        else:
            self.cfg.copy(target)
            _(["config", "set", "omero.web.server_list", WEB]) # TODO: Unneeded if copy old?

        for line in fileinput.input([self.dir / "etc" / "grid" / "templates.xml"], inplace=True):
            print line.replace("Xmx512M", self.mem).replace("Xmx256M", self.mem),

    def start(self, _):
        _("admin start")
        if self.web():
            print "Starting web ..."
            self.startweb(_)

    def set_cli(self, dir):

        dir = os.path.abspath(dir)
        lib = os.path.join(dir, "lib", "python")
        if not os.path.exists(lib):
            raise Exception("%s does not exist!" % lib)
        sys.path.insert(0, lib)

        import omero
        import omero.cli

        print "Using %s..." % omero.cli.__file__

        self.cli = omero.cli.CLI()
        self.cli.loadplugins()

        return self._

    def _(self, command):
        """
        Runs a command as if from the command-line
        without the need for using popen or subprocess
        """
        if isinstance(command, str):
            command = command.split()
        else:
            for idx, val in enumerate(command):
                command[idx] = val
        self.cli.invoke(command, strict=True)

    def web(self):
        return "false" == self.skipweb.lower()


class UnixUpgrade(Upgrade):
    """
    def rmtree(self, d):
        def on_rmtree(self, func, name, exc):
            print "rmtree error: %s('%s') => %s" % (func.__name__, name, exc[1])
        d = path.path(d)
        d.rmtree(onerror = on_rmtree)
    """

    def stopweb(self, _):
        _("web stop")

    def startweb(self, _):
        _("web start")

    def confgure(self, _):
        super(UnixUpgrade, self).configure(_)
        var = self.dir / "var"
        var.mkdir()
        var.chmod(755) # For Apache/Nginx

    def directories(self, _):
        try:
            os.unlink(self.sym)
        except:
            print "Failed to delete %s" % self.sym

        try:
            os.symlink(self.dir, self.sym)
        except:
            print "Failed to symlink %s to %s" % (self.dir, self.sym)


class WindowsUpgrade(Upgrade):

    def stopweb(self, _):
        print "Removing web from IIS ..."
        _("web iis --remove")
        self.iisreset()

    def startweb(self, _):
        print "Configuring web in IIS ..."
        _("web iis")
        self.iisreset()

    def directories(self, _):
        self.rmdir()
        print "Should probably move directory to OLD_OMERO and test handles"
        self.mklink(self.dir)

    def call(self, command):
        rc = subprocess.call(command, shell=True)
        if rc != 0:
            print "*"*80
            print "Warning: '%s' returned with non-zero value: %s" % (command, rc)
            print "*"*80

    def rmdir(self):
        """
        """
        self.call("rmdir OMERO-CURRENT".split())

    def mklink(self, dir):
        """
        """
        self.call("mklink /d OMERO-CURRENT".split() + ["%s" % dir])

    def iisreset(self):
        """
        Calls iisreset
        """
        self.call(["iisreset"])


if __name__ == "__main__":

    artifacts = Artifacts()

    if len(sys.argv) != 2:
        dir = artifacts.download_server()
        # Exits if directory does not exist!
    else:
        dir = sys.argv[1]

    if platform.system() != "Windows":
        u = UnixUpgrade(dir)
    else:
        u = WindowsUpgrade(dir)

    if "false" == SKIPEMAIL.lower():
        e = Email(artifacts)
    else:
        print "Skipping email..."
