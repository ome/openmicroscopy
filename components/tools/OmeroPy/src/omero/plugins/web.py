#!/usr/bin/env python
"""
   Plugin for our configuring the OMERO.web installation

   Copyright 2009 University of Dundee. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from exceptions import Exception
from omero.cli import BaseControl, CLI
import omero.java
import time
import sys
import os

HELP="""omero web settings

OMERO.web tools:

     settings           - Configuration for web

For advance use:
     custom_settings    - Creates only custom_settings.py
     server             - Set to 'default' for django internal webserver
                          'fastcgi' (UNIX socket) or 'fastcgi-tcp'
     config             - output a config template for server (only 'nginx'
                          for the moment)
     syncmedia          - creates needed symlinks for static media files
     enableapp          - TODO: document
     gateway
     test
     seleniumtest
     call

"""
class WebControl(BaseControl):

    def _configure(self, parser):
        sub = parser.sub()
        parser.add(sub, self.help, "Print extended help")
        parser.add(sub, self.settings, "Primary configuration for web")
        parser.add(sub, self.custom_settings, "Advanced use: Creates only a a custom_settings.py")

        start = parser.add(sub, self.start, "Primary start for webserver")
        start.add_argument("host", nargs="?")
        start.add_argument("port", nargs="?")
        
        emdb_settings = parser.add(sub, self.emdb_settings, "Configure settings for the web EMDB client")

        server = parser.add(sub, self.server, "Advanced use: Set to 'default' for django internal webserver or 'fastcfgi'")
        server.add_argument("server")

        config = parser.add(sub, self.config, "Advanced use: Output a config template for server (only 'nginx' for the moment")
        config.add_argument("type", choices=("nginx",))

        parser.add(sub, self.syncmedia, "Advanced use: Creates needed symlinks for static media files")

        enableapp = parser.add(sub, self.enableapp, "Advanced use:")
        enableapp.add_argument("appname", nargs="*")

        parser.add(sub, self.gateway, "Advanced use:")

        test = parser.add(sub, self.test, "Advanced use:")
        test.add_argument("arg", nargs="*")

        selenium = parser.add(sub, self.seleniumtest, "Advanced use: runs selenium tests on a django app")
        selenium.add_argument("djangoapp", help = "Django-app to be tested")

        call = parser.add(sub, self.call, """Advanced use: call appname "[executable] scriptname" args """)
        call.add_argument("appname")
        call.add_argument("scriptname")
        call.add_argument("arg", nargs="*")

    def help(self, args = None):
        self.ctx.out(HELP)


    def _setup_emdb(self):
        settings = dict()
        
        set_cache = self.ctx.input("Allow Django to use local memory caching? (yes/no) (default yes):")
        if not set_cache == "no":
            settings["CACHE_BACKEND"] = 'locmem://'
        
        use_eman = self.ctx.input("Allow Django to use EMAN2 if installed? (yes/no):")
        if not use_eman == "no":
            settings["EMAN2"] = True
        
        return settings
            
        
    def _setup_server(self, email_server=None, app_host=None, sender_address=None, smtp_server=None):
        settings = dict()

        while not app_host or len(app_host) < 1:
            app_host = self.ctx.input("Please enter the domain you want to run OMERO.web on (http://www.domain.com:8000/):")
            if app_host == None or app_host == "":
                self.ctx.err("Domain cannot be empty")
                continue
            settings["APPLICATION_HOST"] = app_host
            break

        while not sender_address or len(sender_address) < 1 :
            sender_address = self.ctx.input("Please enter the Email address you want to send from (omero_admin@example.com): ")
            if sender_address == None or sender_address == "":
                self.ctx.err("Email cannot be empty")
                continue
            break

        while not smtp_server or len(smtp_server) < 1 :
            smtp_server = self.ctx.input("Please enter the SMTP server host you want to send from (smtp.example.com): ")
            if smtp_server == None or smtp_server == "":
                self.ctx.err("SMTP server host cannot be empty")
                continue

            smtp_port = self.ctx.input("Optional: please enter the SMTP server port (default 25): ")
            smtp_user = self.ctx.input("Optional: Please enter the SMTP server username: ")
            smtp_password = self.ctx.input("Optional: Password: ", hidden=True)
            smtp_tls = self.ctx.input("Optional: TSL? (yes/no): ")
            if smtp_tls == "yes":
                smtp_tls = True
            else:
                smtp_tls = False
            break

        settings["SERVER_EMAIL"] = sender_address
        settings["EMAIL_HOST"] = smtp_server

        if smtp_port:
            settings["EMAIL_PORT"] = smtp_port
        if smtp_user:
            settings["EMAIL_HOST_USER"] = smtp_user
        if smtp_password:
            settings["EMAIL_HOST_PASSWORD"] = smtp_password
        if smtp_tls:
            settings["EMAIL_USE_TLS"] = smtp_tls

        return settings


    def _update_emdb_settings(self, location, settings=None):
        output = open(location, 'w')

        try:
            output.write("""#!/usr/bin/env python
# 
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # 
# #         Django custom settings for OMERO.web EMDB project.          # # 
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
# 
# 
# Copyright (c) 2009 University of Dundee. 
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
# 
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
# 
# Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>, 2010.
# Author: Will Moore <will(at)lifesci(dot)dundee(dot)ac(dot)uk>, 2010.
# 
# Version: 1.0

""")
            if settings.has_key('CACHE_BACKEND'):
                output.write("""CACHE_BACKEND = '%s'
""" % settings["CACHE_BACKEND"])

            if settings.has_key('EMAN2') and settings["EMAN2"]:
                output.write("""
# EMAN2 functionality is used in some features of the webemdb application. E.g. see webemdb.views.py eman()
# Do the import here since EMAN2 import fails if it happens for the first time in views.py "signal only works in main thread"
try:
    from EMAN2 import *
except:
    pass
""")
        finally:
            output.flush()
            output.close()

        self.ctx.out("Saved to " + location)
        sys.path_importer_cache.pop(self.ctx.dir / "var" / "lib", None)
        self.ctx.out("PYTHONPATH updated.")


    def _update_settings(self, location, settings=None):
        output = open(location, 'w')

        try:
            output.write("""#!/usr/bin/env python
# 
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # 
# #              Django custom settings for OMERO.web project.          # # 
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
# 
# 
# Copyright (c) 2009 University of Dundee. 
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
# 
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
# 
# Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>, 2008.
# 
# Version: 1.0

# Notification
# Application allows to notify user about new shares

#DEBUG = False
SERVER_LIST = (
    ('localhost', 4064, 'omero'),
)

ADMINS = (
    # ('Name', 'email'),
)

""")
            if settings.has_key('SERVER_EMAIL'):
                output.write("""SERVER_EMAIL = '%s'
""" % settings["SERVER_EMAIL"])
            if settings.has_key('EMAIL_HOST'):
                output.write("""EMAIL_HOST = '%s'
""" % settings["EMAIL_HOST"])
            if settings.has_key('EMAIL_PORT'):
                output.write("""EMAIL_PORT = %s
""" % settings["EMAIL_PORT"])
            if settings.has_key('EMAIL_HOST_USER'):
                output.write("""EMAIL_HOST_USER = '%s'
""" % settings["EMAIL_HOST_USER"])
            if settings.has_key('EMAIL_HOST_PASSWORD'):
                output.write("""EMAIL_HOST_PASSWORD = '%s'
""" % settings["EMAIL_HOST_PASSWORD"])
            if settings.has_key('EMAIL_USE_TLS'):
                if settings["EMAIL_USE_TLS"]:
                    output.write("""EMAIL_USE_TLS = 'True'
""")
                else:
                    output.write("""EMAIL_USE_TLS = 'False'
""")

            output.write("""
APPLICATION_HOST='%s' 
""" % settings["APPLICATION_HOST"])
        finally:
            output.flush()
            output.close()

        self.ctx.out("Saved to " + location)
        sys.path_importer_cache.pop(self.ctx.dir / "var" / "lib", None)
        self.ctx.out("PYTHONPATH updated.")

    def _get_yes_or_no(self, file_name, answer=None):
        while answer != "yes" and answer != "no":
            answer = self.ctx.input("%s already exist. Do you want to ovewrite it? (yes/no)" % file_name)
            if answer != "yes" and answer != "no":
                self.ctx.err("Answer yes or no")
                continue
            break
        return answer

    def custom_settings(self, args):
        location = self.ctx.dir / "var" / "lib" / "custom_settings.py"

        if location.exists():
            if self._get_yes_or_no("%s" % location) == 'no':
                if hasattr(args, "no_exit") and args.no_exit:
                    return
                else:
                    sys.exit()
        else:
            self.ctx.out("You just installed OMERO, which means you didn't have settings configured in OMERO.web.")
        
        if not os.path.exists(self.ctx.dir / "var" / "lib"):
            os.mkdir(self.ctx.dir / "var" / "lib")
            
        settings = self._setup_server()
        self._update_settings(location, settings)
        

    def emdb_settings(self, args):
        location = self.ctx.dir / "var" / "lib" / "emdb_settings.py"

        if location.exists():
            if self._get_yes_or_no("%s" % location) == 'no':
                if hasattr(args, "no_exit") and args.no_exit:
                    return
                else:
                    sys.exit()
        else:
            self.ctx.out("You don't have emdb_settings configured in OMERO.web. ")
        
        if not os.path.exists(self.ctx.dir / "var" / "lib"):
            os.mkdir(self.ctx.dir / "var" / "lib")
            
        settings = self._setup_emdb()
        self._update_emdb_settings(location, settings)
        

    def settings(self, args):
        args.no_exit = True
        self.custom_settings(args)
        try:
            sys.getwindowsversion()
        except:
            self.syncmedia(args)
            

    def server(self, args):
        if not args.server:
            self.ctx.out("OMERO.web application is served by 'default'")
        else:
            location = self.ctx.dir / "var" / "lib" / "custom_settings.py"
            settings = file(location, 'rb').read().split('\n')
            if settings[-1] == '':
                settings = settings[:-1]
            cserver = 'default'
            for l in settings:
                if l.startswith('APPLICATION_SERVER'):
                    cserver = l.split('=')[-1].strip().replace("'",'').replace('"','')
            server = args.server
            if server == cserver:
                self.ctx.out("OMERO.web was already configured to be served by '%s'" % server)
            elif server in ('default', 'fastcgi', 'fastcgi-tcp'):
                if server == 'fastcgi':
                    import flup
                out = file(location, 'wb')
                wrote = False
                for l in settings:
                    if l.startswith('APPLICATION_SERVER'):
                        wrote = True
                        out.write("APPLICATION_SERVER = '%s'\n" % server)
                    else:
                        out.write(l + '\n')
                if not wrote:
                    out.write("APPLICATION_SERVER = '%s'\n" % server)
                self.ctx.out("OMERO.web has been configured to be served by '%s'" % server)
            else:
                self.ctx.err("Unknown server '%s'" % server)

    def config(self, args):
        if not args.type:
            self.ctx.out("Available configuration helpers:\n - nginx\n")
        else:
            from omeroweb.settings import APPLICATION_HOST
            host = APPLICATION_HOST.split(':')
            try:
                port = int(host[-1])
            except ValueError:
                port = 8000
            server = args.type
            if server == "nginx":
                c = file(self.ctx.dir / "etc" / "nginx.conf.template").read()
                d = {
                    "ROOT":self.ctx.dir,
                    "OMEROWEBROOT":self.ctx.dir / "lib" / "python" / "omeroweb",
                    "HTTPPORT":port,
                    }
                self.ctx.out(c % d)

    def syncmedia(self, args):
        import shutil
        from glob import glob
        from omeroweb.settings import INSTALLED_APPS
        location = self.ctx.dir / "lib" / "python" / "omeroweb"
        # Targets
        apps = map(lambda x: x.startswith('omeroweb.') and x[9:] or x, INSTALLED_APPS)
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
                os.symlink(os.path.abspath(media_dir), location / 'media' / app)

    def enableapp(self, args):
        from omeroweb.settings import INSTALLED_APPS
        location = self.ctx.dir / "lib" / "python" / "omeroweb"
        if not args.appname:
            apps = [x.name for x in filter(lambda x: x.isdir() and (x / 'scripts' / 'enable.py').exists(), location.listdir())]
            iapps = map(lambda x: x.startswith('omeroweb.') and x[9:] or x, INSTALLED_APPS)
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
        os.environ['ICE_CONFIG'] = self.ctx.dir / "etc" / "ice.config"
        os.environ['PATH'] = os.environ.get('PATH', '.') + ':' + self.ctx.dir / 'bin'
        os.environ['DJANGO_SETTINGS_MODULE'] = os.environ.get('DJANGO_SETTINGS_MODULE', 'omeroweb.settings')
        rv = self.ctx.call(args, cwd = location)

    def test(self, args):
        location = self.ctx.dir / "lib" / "python" / "omeroweb"
        cargs = ["coverage","-x", "manage.py", "test"]
        if args.arg:
            cargs.append(args.arg)
        os.environ['ICE_CONFIG'] = self.ctx.dir / "etc" / "ice.config"
        os.environ['PATH'] = os.environ.get('PATH', '.') + ':' + self.ctx.dir / 'bin'
        rv = self.ctx.call(cargs, cwd = location)

    def seleniumtest (self, args):
        location = self.ctx.dir / "lib" / "python" / "omeroweb"
        cargs = ["python", "seleniumtests.py"]
        location = location / args.djangoapp / "tests"
        print location
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
            os.environ['ICE_CONFIG'] = self.ctx.dir / "etc" / "ice.config"
            os.environ['PATH'] = os.environ.get('PATH', '.') + ':' + self.ctx.dir / 'bin'
            rv = self.ctx.call(cargs, cwd = location)
        except:
            import traceback
            print traceback.print_exc()
    
    def start(self, args):
        host = args.host is not None and args.host or "0.0.0.0"
        port = args.port is not None and args.port or "8000"
        link = ("%s:%s" % (host, port))
        location = self.ctx.dir / "lib" / "python" / "omeroweb"
        self.ctx.out("Starting django development webserver... \n")
        import omeroweb.settings as settings
        deploy = getattr(settings, 'APPLICATION_SERVER', 'default')
        if deploy == 'fastcgi':
            cmd = "python manage.py runfcgi workdir=./"
            cmd += " method=prefork socket=%(base)s/var/django_fcgi.sock"
            cmd += " pidfile=%(base)s/var/django.pid daemonize=false"
            cmd += " maxchildren=5 minspare=1 maxspare=5 maxrequests=400"
            django = (cmd % {'base': self.ctx.dir}).split()
        elif deploy == 'fastcgi-tcp':
            cmd = "python manage.py runfcgi workdir=./"
            cmd += " method=prefork host=%(host)s port=%(port)s"
            cmd += " pidfile=%(base)s/var/django.pid daemonize=false"
            cmd += " maxchildren=5 minspare=1 maxspare=5 maxrequests=400"
            django = (cmd % {'base': self.ctx.dir, 'host': host,
                             'port':port}).split()
        else:
            django = ["python","manage.py","runserver", link, "--noreload"]
        rv = self.ctx.call(django, cwd = location)

try:
    register("web", WebControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("web", WebControl, HELP)
        cli.invoke(sys.argv[1:])
