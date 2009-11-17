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

"""
class WebControl(BaseControl):

    def help(self, args = None):
        self.ctx.out(HELP)
    
    def _get_password_hash(self, root_pass=None):

        root_pass = self._ask_for_password(" for OMERO.web administrator")

        import sha, random
        algo = 'sha1'
        salt = sha.new(str(random.random())).hexdigest()[:5]
        hsh = sha.new(salt+root_pass).hexdigest()
        value = '%s$%s$%s' % (algo, salt, hsh)
        return value.strip()

    def _get_username_and_email(self, username=None, email=None):
        while not username or len(username) < 1:
            username = self.ctx.input("Please enter Username for OMERO.web administrator: ")
            if username == None or username == "":
                self.ctx.err("Username cannot be empty")
                continue
            break
        while not email or len(email) < 1:
            email = self.ctx.input("Please enter Email address: ")
            if email == None or email == "":
                self.ctx.err("Email cannot be empty")
                continue
            break
        return {"username":username, "email":email}

    def _create_superuser(self, location, username, email, passwd):
        output = open(location, 'w')
        
        try:
            output.write("""[
  {
    "pk": 1,
    "model": "auth.user",
    "fields": {
      "username": "%s",
      "first_name": "",
      "last_name": "",
      "is_active": true,
      "is_superuser": true,
      "is_staff": true,
      "last_login": "%s",
      "groups": [],
      "user_permissions": [],
      "password": "%s",
      "email": "%s",
      "date_joined": "%s"
    }
  },
  {
      "pk": 1,
      "model": "webadmin.gateway",
      "fields": {
          "server": "omero",
          "host": "localhost",
          "port": 4063
      }
  }
]""" % (username, time.strftime("%Y-%m-%d %H:%M:%S", time.localtime()), passwd, email, time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())))
        finally:
            output.flush()
            output.close()
        
        self.ctx.out("Saved to " + location)

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
    
    def initial(self, do_exit=True, *args):
        details = dict()
        location = self.ctx.dir / "lib" / "python" / "omeroweb" / "initial_data.json"
        
        if location.exists():
            if self._get_yes_or_no("%s" % location) == 'no':
                if do_exit:
                    sys.exit()
                else:
                    return
        
        details["user"] = self._get_username_and_email()
        details["passwd"] = self._get_password_hash()
        self._create_superuser(location, details["user"]["username"], details["user"]["email"], details["passwd"])
    
    def syncdb(self, do_exit=True, *args):
        self.ctx.out("Database synchronization...")
        omero_web = self.ctx.dir / "lib" / "python" / "omeroweb"
        
        if os.path.isfile(os.path.join(omero_web, 'db.sqlite3')):
            if self._get_yes_or_no("Local database") == 'no':
                if do_exit:
                    sys.exit()
                else:
                    return
            else:
                try:
                    os.remove(os.path.join(omero_web, 'db.sqlite3'))
                except Exception, e:
                    self.ctx.err("'db.sqlite3' was not deleted becuase: %s" % str(e))
                    sys.exit()
                else:
                    self.ctx.out("Old database file 'db.sqlite3' was deleted successfully.")
        
        if not os.path.isfile(os.path.join(omero_web, 'custom_settings.py')):
            self.ctx.err("custom_settings.py does not exist. Please run bin/omero web custom_settings")
            sys.exit()
        if not os.path.isfile(os.path.join(omero_web, 'initial_data.json')):
            self.ctx.out("initial_data.json does not exist. Please run bin/omero web initial")
            sys.exit()
        
        args = ["python", "manage.py", "syncdb", "--noinput"]
        rv = self.ctx.call(args, cwd = omero_web)
        if rv != 0:
            self.ctx.die(121, "OMERO.web was not configured.\n")
        else:
            self.ctx.out("OMERO.web was configured successful. Please start the application.\n")

    def settings(self, *args):
        self.custom_settings(do_exit=False)
        self.ctx.out("\n")
        self.initial(do_exit=False)
        self.ctx.out("\n")
        self.syncdb(do_exit=False)

try:
    register("web", WebControl)
except NameError:
    WebControl()._main()
