#!/usr/bin/env python
"""
   Plugin for our managing the OMERO database.

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from exceptions import Exception
from omero.cli import Arguments, BaseControl, VERSION
import omero.java
import time

HELP=""" omero web [superuser|settings]

OMERO.web tools:

     superuser  - Creates a superuser for managing OMERO.web local database
     settings   - Configuration for web

"""
class DatabaseControl(BaseControl):

    def help(self, args = None):
        self.ctx.out(HELP)
    
    def _get_password_hash(self, root_pass=None):
        while not root_pass or len(root_pass) < 1:
            root_pass = self.ctx.input("Please enter password for OMERO.web administrator: ", hidden = True)
            if root_pass == None or root_pass == "":
                self.ctx.err("Password cannot be empty")
                continue
            confirm = self.ctx.input("Please re-enter password for new OMERO root user: ", hidden = True)
            if root_pass != confirm:
                self.ctx.err("Passwords don't match")
                root_pass = None
                continue
            break
        
        import sha, random
        algo = 'sha1'
        salt = sha.new(str(random.random())).hexdigest()[:5]
        hsh = sha.new(salt+root_pass).hexdigest()
        value = '%s$%s$%s' % (algo, salt, hsh)
        return value.strip()
    
    def _get_username_and_email(self, username=None, email=None):
        while not username or len(username) < 1:
            username = self.ctx.input("Please enter Username for OMERO.web superuser: ")
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
    
    def _create_superuser(self, username, email, passwd):
        location = self.ctx.dir / "lib" / "python" / "omeroweb" / "initial_data.json"
        output = open(location, 'w')
        print "Saving to " + location

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
  }
]""" % (username, time.strftime("%Y-%m-%d %H:%M:%S", time.localtime()), passwd, email, time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())))
        finally:
            output.flush()
            output.close()
    
    def superuser(self, *args):
        details = dict()
        details["user"] = self._get_username_and_email()
        details["passwd"] = self._get_password_hash()
        self._create_superuser(details["user"]["username"], details["user"]["email"], details["passwd"])
    
    
    def _setup_email_server(self, email_server=None, app_host=None):
        settings = dict()
        
        while not app_host or len(app_host) < 1:
            app_host = self.ctx.input("You just installed OMERO, which means you didn't have settings configured in OMERO.web.\nPlease enter the domain you want to run the OMERO.web (http://www.domain.com:8000/): ")
            if app_host == None or app_host == "":
                self.ctx.err("Domain cannot be empty")
                continue
            settings["APPLICATION_HOST"] = app_host
            break
        
        while not email_server or len(email_server) < 1 or email_server != "yes" or email_server != "no":
            email_server = self.ctx.input("Would you like to set up email server? (yes/no): ")
            if email_server != "yes" and email_server != "no":
                self.ctx.err("Please enter 'yes' or 'no'.")
                continue
            if email_server == "yes":
                notification = True
                sender_address = None
                smtp_server = None
                
                smtp_port = None
                smtp_user = None
                smtp_password = None
                smtp_tls = None
                while not sender_address or len(sender_address) < 1 or not smtp_server or len(smtp_server) < 1 :
                    sender_address = self.ctx.input("Please enter the Email address you want to send from (omero_admin@example.com): ")
                    if sender_address == None or sender_address == "":
                        self.ctx.err("Email cannot be empty")
                        continue
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
                
                settings["NOTIFICATION"] = notification
                settings["SENDER_ADDRESS"] = sender_address
                settings["SMTP_SERVER"] = smtp_server
                
                if smtp_port:
                    settings["SMTP_PORT"] = smtp_port
                if smtp_user:
                    settings["SMTP_USER"] = smtp_user
                if smtp_password:
                    settings["SMTP_PASSWORD"] = smtp_password
                if smtp_tls:
                    settings["SMTP_TLS"] = smtp_tls
            
            break
        return settings
    
    def _update_settings(self, settings=None):
        location = self.ctx.dir / "lib" / "python" / "omeroweb" / "custom_settings.py"
        output = open(location, 'w')
        print "Saving to " + location

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
#
# ADMIN notification
# If you wish to help us catching errors, please set the Error notifier to True (please
# be sure you turned on EMAIL_NOTIFICATION and set ADMIN details).
# That mechanism sent to the administrator every errors.
# We are very appreciative if you can deliver them to:
#   Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>
ERROR2EMAIL_NOTIFICATION = %s

# Notification
# Application allows to notify user about new shares
EMAIL_NOTIFICATION = %s
EMAIL_SENDER_ADDRESS = '%s'
EMAIL_SMTP_SERVER = '%s'
""" % ('False', str(settings["NOTIFICATION"]), str(settings["SENDER_ADDRESS"]), str(settings["SMTP_SERVER"]) ))
            if settings.has_key('SMTP_PORT'):
                output.write("""EMAIL_SMTP_PORT = %s""" % settings["SMTP_PORT"])
            if settings.has_key('SMTP_USER'):
                output.write("""EMAIL_SMTP_USER = '%s'""" % settings["SMTP_USER"])
            if settings.has_key('SMTP_PASSWORD'):
                output.write("""EMAIL_SMTP_PASSWORD = '%s'""" % settings["SMTP_PASSWORD"])
            if settings.has_key('SMTP_TLS'):
                if settings["SMTP_TLS"]:
                    output.write("""EMAIL_SMTP_TLS = 'True'""")
                else:
                    output.write("""EMAIL_SMTP_TLS = 'False'""")

            output.write("""
APPLICATION_HOST='%s' 
""" % settings["APPLICATION_HOST"])
        finally:
            output.flush()
            output.close()
    
    def settings(self, *args):
        details = dict()
        settings = self._setup_email_server()
        self._update_settings(settings)

try:
    register("web", DatabaseControl)
except NameError:
    DatabaseControl()._main()
