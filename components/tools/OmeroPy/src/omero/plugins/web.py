#!/usr/bin/env python
"""
   Plugin for our configuring the OMERO.web installation

   Copyright 2009 University of Dundee. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from exceptions import Exception
from datetime import datetime
from omero.cli import BaseControl, CLI
import omero.java
import platform
import time
import sys
import os
import re

try:
    from omeroweb import settings

    CONFIG_TABLE_FMT = "    %-35.35s  %-8s  %r\n"
    CONFIG_TABLE = CONFIG_TABLE_FMT % ("Key", "Default?", "Current value")

    for key in sorted(settings.CUSTOM_SETTINGS_MAPPINGS):
        global_name, default_value, mapping, using_default = settings.CUSTOM_SETTINGS_MAPPINGS[key]
        global_value = getattr(settings, global_name, "(unset)")
        CONFIG_TABLE += CONFIG_TABLE_FMT  % (key, using_default, global_value)
except:
    CONFIG_TABLE="INVALID CONFIGURATION! Cannot display default values"

HELP="""OMERO.web configuration/deployment tools

Configuration:

    Configuration for OMERO.web takes place via the
    omero config commands. The configuration values
    which are checked are as below:

%s

Example Nginx usage:

    omero config set omero.web.debug true
    omero config set omero.web.application_server fastcgi
    omero web config nginx --http=8000 >> nginx.conf
    omero web start
    nginx -c `pwd`/nginx.conf
    omero web status
    omero web stop
    nginx -s stop

""" % CONFIG_TABLE


class WebControl(BaseControl):

    def _configure(self, parser):
        sub = parser.sub()

        parser.add(sub, self.start, "Primary start for the OMERO.web server")
        parser.add(sub, self.stop, "Stop the OMERO.web server")
        parser.add(sub, self.status, "Status for the OMERO.web server")

        #
        # Advanced
        #

        config = parser.add(sub, self.config, "Output a config template for server ('nginx' or 'apache' for the moment")
        config.add_argument("type", choices=("nginx","apache"))
        config.add_argument("--http", type=int, help="HTTP port for web server (not fastcgi)")

        parser.add(sub, self.syncmedia, "Advanced use: Creates needed symlinks for static media files")

        #
        # Developer
        #

        call = parser.add(sub, self.call, """Developer use: call appname "[executable] scriptname" args """)
        call.add_argument("appname")
        call.add_argument("scriptname")
        call.add_argument("arg", nargs="*")

        enableapp = parser.add(sub, self.enableapp, "Developer use: runs enable.py and then syncdb")
        enableapp.add_argument("appname", nargs="*")

        gateway = parser.add(sub, self.gateway, "Developer use: Loads the blitz gateway into a Python interpreter")

        selenium = parser.add(sub, self.seleniumtest, "Developer use: runs selenium tests on a django app")
        selenium.add_argument("djangoapp", help = "Django-app to be tested")
        selenium.add_argument("seleniumserver", help = "E.g. localhost")
        selenium.add_argument("hostname", help = "E.g. http://localhost:4080")
        selenium.add_argument("browser", help = "E.g. firefox")

        unittest = parser.add(sub, self.unittest, "Developer use: Runs 'coverage -x manage.py test'")
        unittest.add_argument("--config", action="store", help = "ice.config location")
        unittest.add_argument("--test", action="store", help = "Specific test case(-s).")
        unittest.add_argument("--path", action="store", help = "Path to Django-app. Must include '/'.")


    def host_and_port(self, APPLICATION_HOST):
        parts = APPLICATION_HOST.split(':')
        if len(parts) != 3:
            self.ctx.die(656, "Invalid application host: %s" % ":".join(parts))
        try:
            host = parts[1]
            while host.startswith(r"/"):
                host = host[1:]
            port = parts[2]
            port = re.search(r'^(\d+).*', port).group(1)
            port = int(port)
            return (host, port)
        except Exception, e:
            self.ctx.die(567, "Badly formed domain: %s -- %s" % (":".join(parts), e))

    def config(self, args):
        if not args.type:
            self.ctx.out("Available configuration helpers:\n - nginx, apache\n")
        else:
            server = args.type
            host, port = self.host_and_port(settings.APPLICATION_HOST)
            if args.http:
                port = args.http
            if settings.APPLICATION_SERVER == settings.FASTCGITCP:
                if settings.APPLICATION_SERVER_PORT == port:
                    self.ctx.die(678, "Port conflict: HTTP(%s) and fastcgi-tcp(%s)." % \
                            (port, settings.APPLICATION_SERVER_PORT))
            if server == "nginx":
                if settings.APPLICATION_SERVER == settings.FASTCGITCP:
                    fastcgi_pass = "%s:%s" % (settings.APPLICATION_SERVER_HOST,
                                              settings.APPLICATION_SERVER_PORT)
                else:
                    fastcgi_pass = "unix:%s/var/django_fcgi.sock" % self.ctx.dir
                c = file(self.ctx.dir / "etc" / "nginx.conf.template").read()
                d = {
                    "ROOT":self.ctx.dir,
                    "OMEROWEBROOT":self.ctx.dir / "lib" / "python" / "omeroweb",
                    "HTTPPORT":port,
                    "FASTCGI_PASS":fastcgi_pass,
                    }
                self.ctx.out(c % d)
            if server == "apache":
                if settings.APPLICATION_SERVER == settings.FASTCGITCP:
                    fastcgi_external = '-host %s:%s' % \
                            (settings.APPLICATION_SERVER_HOST,
                             settings.APPLICATION_SERVER_PORT)
                else:
                    fastcgi_external = '-socket "%s/var/django_fcgi.sock"' % \
                        self.ctx.dir
                stanza  = """###
### Stanza for OMERO.web created %(NOW)s
###
FastCGIExternalServer "%(ROOT)s/var/omero.fcgi" %(FASTCGI_EXTERNAL)s

<Directory "%(ROOT)s/var">
    Options -Indexes FollowSymLinks
    Order allow,deny
    Allow from all
</Directory>

<Directory "%(MEDIA)s">
    Options -Indexes FollowSymLinks
    Order allow,deny
    Allow from all
</Directory>

Alias /appmedia %(MEDIA)s
Alias / "%(ROOT)s/var/omero.fcgi/"
"""
                d = {
                    "ROOT":self.ctx.dir,
                    "MEDIA":self.ctx.dir / "lib" / "python" / "omeroweb" / "media",
                    "OMEROWEBROOT":self.ctx.dir / "lib" / "python" / "omeroweb",
                    "FASTCGI_EXTERNAL":fastcgi_external,
                    "NOW":str(datetime.now()),
                    }
                self.ctx.out(stanza % d)

    def syncmedia(self, args):
        import shutil
        from glob import glob
        location = self.ctx.dir / "lib" / "python" / "omeroweb"
        # Targets
        apps = map(lambda x: x.startswith('omeroweb.') and x[9:] or x, settings.INSTALLED_APPS)
        apps = filter(lambda x: os.path.exists(location / x), apps)
        # Destination dir
        if not os.path.exists(location / 'media'):
            os.mkdir(location / 'media')

        # Create app media links
        for app in apps:
            media_dir = location / app / 'media'
            if os.path.exists(media_dir):
                if os.path.exists(location / 'media' / app):
                    os.remove(os.path.abspath(location / 'media' / app))
                try:
                    # Windows does not support symlink
                    sys.getwindowsversion()
                    shutil.copytree(os.path.abspath(media_dir), location / 'media' / app) 
                except:
                    os.symlink(os.path.abspath(media_dir), location / 'media' / app)

    def enableapp(self, args):
        location = self.ctx.dir / "lib" / "python" / "omeroweb"
        if not args.appname:
            apps = [x.name for x in filter(lambda x: x.isdir() and (x / 'scripts' / 'enable.py').exists(), location.listdir())]
            iapps = map(lambda x: x.startswith('omeroweb.') and x[9:] or x, settings.INSTALLED_APPS)
            apps = filter(lambda x: x not in iapps, apps)
            self.ctx.out('[enableapp] available apps:\n - ' + '\n - '.join(apps) + '\n')
        else:
            for app in args.appname:
                args = ["python", location / app / "scripts" / "enable.py"]
                rv = self.ctx.call(args, cwd = location)
                if rv != 0:
                    self.ctx.die(121, "Failed to enable '%s'.\n" % app)
                else:
                    self.ctx.out("App '%s' was enabled\n" % app)
            args = ["python", "manage.py", "syncdb", "--noinput"]
            rv = self.ctx.call(args, cwd = location)
            self.syncmedia(None)

    def gateway(self, args):
        location = self.ctx.dir / "lib" / "python" / "omeroweb"
        args = ["python", "-i", location / "../omero/gateway/scripts/dbhelpers.py"]
        self.set_environ()
        os.environ['DJANGO_SETTINGS_MODULE'] = os.environ.get('DJANGO_SETTINGS_MODULE', 'omeroweb.settings')
        rv = self.ctx.call(args, cwd = location)

    def unittest(self, args):
        try:
            ice_config = args.config
            test = args.test
            testpath = args.path
        except:
            self.ctx.die(121, "usage: unittest --test=appname.TestCase --path=/external/path/")
            
        if testpath is not None and testpath.find('/') >= 0:
            path = testpath.split('/')
            test = path[len(path)-1]
            if testpath.startswith('/'):
                location = "/".join(path[:(len(path)-1)])
            else:
                appbase = test.split('.')[0]
                location = self.ctx.dir / "/".join(path[:(len(path)-1)])
        
        if testpath is None:
            location = self.ctx.dir / "lib" / "python" / "omeroweb"
                    
        if testpath is not None and len(testpath) > 1:
            cargs = [testpath]
        else:
            cargs = ['python']
        
        cargs.extend([ "manage.py", "test"])
        if test:
            cargs.append(test)
        self.set_environ()
        if ice_config is not None:
            os.environ['ICE_CONFIG'] = str(ice_config)
        rv = self.ctx.call(cargs, cwd = location)

    def seleniumtest (self, args):
        try:
            appname = args.djangoapp
            seleniumserver = args.seleniumserver
            hostname = args.hostname
            browser = args.browser
        except:
            self.ctx.die(121, "usage: seleniumtest [path.]{djangoapp} [seleniumserver] [hostname] [browser]")
        
        if appname.find('.') > 0:
            appname = appname.split('.')
            appbase = appname[0]
            location = self.ctx.dir / appbase
            appname = '.'.join(appname[1:])
        else:
            appbase = "omeroweb"
            location = self.ctx.dir / "lib" / "python" / "omeroweb"

        cargs = ["python", location / appname / "tests" / "seleniumtests.py", seleniumserver, hostname, browser]
        #cargs += args.arg[1:]
        rv = self.ctx.call(cargs, cwd = location )
        
    def call (self, args):
        try:
            location = self.ctx.dir / "lib" / "python" / "omeroweb"
            cargs = []
            appname = args.appname
            scriptname = args.scriptname.split(' ')
            if len(scriptname) > 1:
                cargs.append(scriptname[0])
                scriptname = ' '.join(scriptname[1:])
            else:
                scriptname = scriptname[0]
            cargs.extend([location / appname / "scripts" / scriptname] + args.arg)
            print cargs
            os.environ['DJANGO_SETTINGS_MODULE'] = 'omeroweb.settings'
            self.set_environ()
            rv = self.ctx.call(cargs, cwd = location)
        except:
            import traceback
            print traceback.print_exc()


    def start(self, args):
        import omeroweb.settings as settings
        link = ("%s:%s" % (settings.APPLICATION_SERVER_HOST,
                           settings.APPLICATION_SERVER_PORT))
        location = self.ctx.dir / "lib" / "python" / "omeroweb"
        self.ctx.out("Starting OMERO.web... ", newline=False)
        cache_backend = getattr(settings, 'CACHE_BACKEND', None)
        if cache_backend is not None and cache_backend.startswith("file:///"):
            cache_backend = cache_backend[7:]
            if "Windows" != platform.system() \
               and not os.access(cache_backend, os.R_OK|os.W_OK):
                self.ctx.out("[FAILED]")
                self.ctx.out("CACHE_BACKEND '%s' not writable or missing." % \
                             getattr(settings, 'CACHE_BACKEND'))
                return 1
        deploy = getattr(settings, 'APPLICATION_SERVER')

        # 3216
        if deploy in (settings.FASTCGI_TYPES):
            if "Windows" == platform.system():
                self.ctx.out("""
WARNING: Unless you **really** know what you are doing you should NOT be
using bin\omero web start on Windows with FastCGI.
""")
            pid_path = self.ctx.dir / "var" / "django.pid"
            pid_num = None

            if pid_path.exists():
                pid_txt = pid_path.text().strip()
                try:
                    pid_num = int(pid_txt)
                except:
                    pid_path.remove()
                    self.ctx.err("Removed invalid %s: '%s'" % (pid_path, pid_txt))

            if pid_num is not None:
                try:
                    os.kill(pid_num, 0)
                    self.ctx.die(606, "%s exists! Use 'web stop' first" % pid_path)
                except OSError:
                    pid_path.remove()
                    self.ctx.err("Removed stale %s" % pid_path)

        if deploy == settings.FASTCGI:
            cmd = "python manage.py runfcgi workdir=./"
            cmd += " method=prefork socket=%(base)s/var/django_fcgi.sock"
            cmd += " pidfile=%(base)s/var/django.pid daemonize=true"
            cmd += " maxchildren=5 minspare=1 maxspare=5 maxrequests=400"
            django = (cmd % {'base': self.ctx.dir}).split()
            rv = self.ctx.popen(args=django, cwd=location) # popen
        elif deploy == settings.FASTCGITCP:
            cmd = "python manage.py runfcgi workdir=./"
            cmd += " method=prefork host=%(host)s port=%(port)s"
            cmd += " pidfile=%(base)s/var/django.pid daemonize=true"
            cmd += " maxchildren=5 minspare=1 maxspare=5 maxrequests=400"
            django = (cmd % {'base': self.ctx.dir,
                             'host': settings.APPLICATION_SERVER_HOST,
                             'port': settings.APPLICATION_SERVER_PORT}).split()
            rv = self.ctx.popen(args=django, cwd=location) # popen
        else:
            django = ["python","manage.py","runserver", link, "--noreload"]
            rv = self.ctx.call(django, cwd = location)
        self.ctx.out("[OK]")
        return rv


    def status(self, args):
        location = self.ctx.dir / "lib" / "python" / "omeroweb"
        self.ctx.out("OMERO.web status... ", newline=False)
        import omeroweb.settings as settings
        deploy = getattr(settings, 'APPLICATION_SERVER')
        cache_backend = getattr(settings, 'CACHE_BACKEND', None)
        if cache_backend is not None:
            cache_backend = ' (CACHE_BACKEND %s)' % cache_backend
        else:
            cache_backend = ''
        rv = 0
        if deploy in settings.FASTCGI_TYPES:
            try:
                f=open(self.ctx.dir / "var" / "django.pid", 'r')
                pid = int(f.read())
            except IOError:
                self.ctx.out("[NOT STARTED]")
                return rv
            import signal
            try:
                os.kill(pid, 0)  # NULL signal
                self.ctx.out("[RUNNING] (PID %d)%s" % (pid, cache_backend))
            except:
                self.ctx.out("[NOT STARTED]")
                return rv
        else:
            self.ctx.err("DEVELOPMENT: You will have to check status by hand!")
        return rv

    def stop(self, args):
        self.ctx.out("Stopping OMERO.web... ", newline=False)
        import omeroweb.settings as settings
        deploy = getattr(settings, 'APPLICATION_SERVER')
        if deploy in settings.FASTCGI_TYPES:
            if "Windows" == platform.system():
                self.ctx.out("""
WARNING: Unless you **really** know what you are doing you should NOT be
using bin\omero web start on Windows with FastCGI.
""")
            pid = 'Unknown'
            pid_path = self.ctx.dir / "var" / "django.pid"
            pid_text = "Unknown"
            if pid_path.exists():
                pid_text = pid_path.text().strip()
            try:
                try:
                    pid = int(pid_text)
                    import signal
                    os.kill(pid, 0)  # NULL signal
                except:
                    self.ctx.out("[FAILED]")
                    self.ctx.out("Django FastCGI workers (PID %s) not started?" % pid_text)
                    return
                os.kill(pid, signal.SIGTERM) #kill whole group
                self.ctx.out("[OK]")
                self.ctx.out("Django FastCGI workers (PID %d) killed." % pid)
            finally:
                if pid_path.exists():
                    pid_path.remove()
        else:
            self.ctx.err("DEVELOPMENT: You will have to kill processes by hand!")

    def set_environ(self):
        os.environ['ICE_CONFIG'] = str(self.ctx.dir / "etc" / "ice.config")
        os.environ['PATH'] = str(os.environ.get('PATH', '.') + ':' + self.ctx.dir / 'bin')

try:
    register("web", WebControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("web", WebControl, HELP)
        cli.invoke(sys.argv[1:])
