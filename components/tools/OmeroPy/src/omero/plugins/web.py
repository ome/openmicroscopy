#!/usr/bin/env python
"""
   Plugin for our configuring the OMERO.web installation

   Copyright 2009 University of Dundee. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from exceptions import Exception
from omero.cli import Arguments, BaseControl, VERSION
import omero.java
import time

HELP=""" omero web settings

OMERO.web tools:

     settings           - Configuration for web
     
For advance use:
     custom_settings    - Creates only custom_settings.py
     initial            - Creates initial_data.json
     syncdb             - Synchronise local database
     server             - Set to 'default' for django internal webserver
                          or 'fastcgi'
     config             - output a config template for server (only 'nginx'
                          for the moment)
     syncmedia          - creates needed symlinks for static media files
     enableapp          - TODO: document

"""
class WebControl(BaseControl):

    def help(self, args = None):
        self.ctx.out(HELP)

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
    
    def _get_yes_or_no(self, file_name, answer=None):
        while answer != "yes" and answer != "no":
            answer = self.ctx.input("%s already exist. Do you want to ovewrite it? (yes/no)" % file_name)
            if answer != "yes" and answer != "no":
                self.ctx.err("Answer yes or no")
                continue
            break
        return answer
    
    def custom_settings(self, do_exit=True, *args):
        location = self.ctx.dir / "lib" / "python" / "omeroweb" / "custom_settings.py"
        
        if location.exists():
            if self._get_yes_or_no("%s" % location) == 'no':
                if do_exit:
                    sys.exit()
                else:
                    return
        else:
            self.ctx.out("You just installed OMERO, which means you didn't have settings configured in OMERO.web.")
            
        settings = self._setup_server()
        self._update_settings(location, settings)
    
    def settings(self, *args):
        self.custom_settings(do_exit=False)
        self.syncmedia()

    def server(self, *args):
        if not len(args[0]):
            self.ctx.out("OMERO.web application is served by 'default'")
        else:
            location = self.ctx.dir / "lib" / "python" / "omeroweb" / "custom_settings.py"
            settings = file(location, 'rb').read().split('\n')
            if settings[-1] == '':
                settings = settings[:-1]
            cserver = 'default'
            for l in settings:
                if l.startswith('APPLICATION_SERVER'):
                    cserver = l.split('=')[-1].strip().replace("'",'').replace('"','')
            server = args[0][0]
            if server == cserver:
                self.ctx.out("OMERO.web was already configured to be served by '%s'" % server)
            elif server in ('default', 'fastcgi'):
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

    def config(self, *args):
        if not len(args[0]):
            self.ctx.out("Available configuration helpers:\n - nginx\n")
        else:
            import omeroweb.custom_settings as settings
            host = settings.APPLICATION_HOST.split(':')
            try:
                port = int(host[-1])
            except ValueError:
                port = 8000
            server = args[0][0]
            if server == "nginx":
                c = file(self.ctx.dir / "etc" / "nginx.conf.template").read()
                d = {
                    "ROOT":self.ctx.dir,
                    "OMEROWEBROOT":self.ctx.dir / "lib" / "python" / "omeroweb",
                    "HTTPPORT":port,
                    }
                self.ctx.out(c % d)

    def syncmedia(self, *args):
        import os, shutil
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
        
    def enableapp(self, *args):
        from omeroweb.settings import INSTALLED_APPS
        location = self.ctx.dir / "lib" / "python" / "omeroweb"
        if len(args[0]) < 1:
            apps = [x.name for x in filter(lambda x: x.isdir() and (x / 'scripts' / 'enable.py').exists(), location.listdir())]
            iapps = map(lambda x: x.startswith('omeroweb.') and x[9:] or x, INSTALLED_APPS)
            apps = filter(lambda x: x not in iapps, apps)
            self.ctx.out('[enableapp] available apps:\n - ' + '\n - '.join(apps) + '\n')
        else:
            for app in args[0]:
                args = ["python", location / app / "scripts" / "enable.py"]
                rv = self.ctx.call(args, cwd = location)
                if rv != 0:
                    self.ctx.die(121, "Failed to enable '%s'.\n" % app)
                else:
                    self.ctx.out("App '%s' was enabled\n" % app)
            args = ["python", "manage.py", "syncdb", "--noinput"]
            rv = self.ctx.call(args, cwd = location)
            self.syncmedia()

    def gateway(self, *args):
        location = self.ctx.dir / "lib" / "python" / "omeroweb"
        args = ["python", "-i", location / "../omero/gateway/scripts/dbhelpers.py"]
        os.environ['ICE_CONFIG'] = self.ctx.dir / "etc" / "ice.config"
        os.environ['PATH'] = os.environ.get('PATH', '.') + ':' + self.ctx.dir / 'bin'
        os.environ['DJANGO_SETTINGS_MODULE'] = os.environ.get('DJANGO_SETTINGS_MODULE', 'omeroweb.settings')
        rv = self.ctx.call(args, cwd = location)

    def test(self, *args):
        location = self.ctx.dir / "lib" / "python" / "omeroweb"
        args = ["coverage","-x", "manage.py", "test"]
        os.environ['ICE_CONFIG'] = self.ctx.dir / "etc" / "ice.config"
        os.environ['PATH'] = os.environ.get('PATH', '.') + ':' + self.ctx.dir / 'bin'
        rv = self.ctx.call(args, cwd = location)


try:
    register("web", WebControl)
except NameError:
    WebControl()._main()
