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

HELP=""" omero web [settings|superuser|syncdb]

OMERO.web tools:

     settings   - Configuration for web
     superuser  - Creates a superuser for managing OMERO.web local database
     syncdb     - Local database synchronisation

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
  },
  {
      "pk": 1,
      "model": "webadmin.gateway",
      "fields": {
          "server": "omero",
          "host": "localhost",
          "port": 4063
      }
  },
  {
    "pk": 1,
    "model": "feedback.emailtemplate",
    "fields": {
      "content_html": "<p><img src='cid:image1'/></p><hr/><p>Hi,</p><p>I would like to share some of my data with you.<br/>Please find it on the <a href='%%sshare/view/%%i/?server=%%i'>%%sshare/view/%%i/?server=%%i</a>.</p><p>-- %%s</p>",
      "template": "create_share",
      "content_txt": "Hi, I would like to share some of my data with you. Please find it on the %%sshare/view/%%i/?server=%%i. /n -- %%s"
    }
  },
  {
    "pk": 2,
    "model": "feedback.emailtemplate",
    "fields": {
      "content_html": "<p><img src='cid:image1'/></p><hr/><p>Hi,</p><p>I would like to share some of my data with you.<br/>Please find it on the <a href='%%sshare/view/%%i/?server=%%i'>%%sshare/view/%%i/?server=%%i</a>.</p><p>-- %%s</p>",
      "template": "add_member_to_share",
      "content_txt": "Hi, I would like to share some of my data with you. Please find it on the %%sshare/view/%%i/?server=%%i. /n -- %%s"
    }
  },
  {
    "pk": 3,
    "model": "feedback.emailtemplate",
    "fields": {
      "content_html": "<p><img src='cid:image1'/></p><hr/><p>You were removed from the share <a href='%%sshare/view/%%i/?server=%%i'>%%sshare/view/%%i/?server=%%i</a>. This share is no longer available for you.</p>",
      "template": "remove_member_from_share",
      "content_txt": "You were removed from the share %%sshare/view/%%i/?server=%%i. This share is no longer available for you."
    }
  },
  {
    "pk": 4,
    "model": "feedback.emailtemplate",
    "fields": {
      "content_html": "<p><img src='cid:image1'/></p><hr/><p>New comment is available on share <a href='%%sshare/view/%%i/?server=%%i'>%%sshare/view/%%i/?server=%%i</a>.</p>",
      "template": "add_comment_to_share",
      "content_txt": "New comment is available on share %%sshare/view/%%i/?server=%%i."
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

    def _setup_server(self, location, email_server=None, app_host=None):
        settings = dict()

        if location.exists():
            self.ctx.out("Reconfiguring OMERO.web...")
        else:
            self.ctx.out("You just installed OMERO, which means you didn't have settings configured in OMERO.web.")
        while not app_host or len(app_host) < 1:
            app_host = self.ctx.input("Please enter the domain you want to run OMERO.web on (http://www.domain.com:8000/):")
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
            else:
                settings["NOTIFICATION"] = False
            break
        return settings

    def _update_settings(self, location, settings=None):
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
""" % ('False', str(settings["NOTIFICATION"])))
            if settings.has_key('SENDER_ADDRESS'):
                output.write("""EMAIL_SENDER_ADDRESS = '%s'
""" % settings["SENDER_ADDRESS"])
            if settings.has_key('SMTP_SERVER'):
                output.write("""EMAIL_SMTP_SERVER = '%s'
""" % settings["SMTP_SERVER"])
            if settings.has_key('SMTP_PORT'):
                output.write("""EMAIL_SMTP_PORT = %s
""" % settings["SMTP_PORT"])
            if settings.has_key('SMTP_USER'):
                output.write("""EMAIL_SMTP_USER = '%s'
""" % settings["SMTP_USER"])
            if settings.has_key('SMTP_PASSWORD'):
                output.write("""EMAIL_SMTP_PASSWORD = '%s'
""" % settings["SMTP_PASSWORD"])
            if settings.has_key('SMTP_TLS'):
                if settings["SMTP_TLS"]:
                    output.write("""EMAIL_SMTP_TLS = 'True'
""")
                else:
                    output.write("""EMAIL_SMTP_TLS = 'False'
""")

            output.write("""
APPLICATION_HOST='%s' 
""" % settings["APPLICATION_HOST"])
        finally:
            output.flush()
            output.close()

    def settings(self, *args):
        location = self.ctx.dir / "lib" / "python" / "omeroweb" / "custom_settings.py"
        settings = self._setup_server(location)
        self._update_settings(location, settings)

    def syncdb(self, *args):
        sys.stderr.write("Database synchronization... \n")
        omero_web = self.ctx.dir / "lib" / "python" / "omeroweb"
        subprocess.call(["python","manage.py","syncdb","--noinput"], cwd=str(omero_web), env = os.environ)
        sys.stderr.write("OMERO.web was prepared. Please start the application.\n")

try:
    register("web", WebControl)
except NameError:
    WebControl()._main()
