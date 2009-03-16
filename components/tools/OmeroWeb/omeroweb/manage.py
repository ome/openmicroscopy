#!/usr/bin/env python
# 
# 
# 
# Copyright (c) 2008 University of Dundee. 
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

from django.core.management import execute_manager
import sys

try:
    import settings # Assumed to be in the same directory.
except ImportError:
    sys.stderr.write("Error: Can't find the file 'settings.py' in the directory containing %r. It appears you've customized things.\nYou'll have to run django-admin.py, passing it your settings module.\n(If the file settings.py does indeed exist, it's causing an ImportError somehow.)\n" % __file__)
    sys.exit(1)

# upgrade check:
# -------------
# On each startup OMERO.web checks for possible server upgrades
# and logs the upgrade url at the WARNING level. If you would
# like to disable the checks, change the following to
#
#   if False:
#
# For more information, see
# http://trac.openmicroscopy.org.uk/omero/wiki/UpgradeCheck
#
try:
    from omero.util.upgrade_check import UpgradeCheck
    check = UpgradeCheck("web")
    check.run()
    if check.isUpgradeNeeded():
        sys.stderr.write("Upgrade is available. Please visit http://trac.openmicroscopy.org.uk/omero/wiki/MilestoneDownloads.\n")
except Exception, x:
    sys.stderr.write("Upgrade check error: %s" % x)

if not settings.EMAIL_NOTIFICATION:
    sys.stderr.write("Settings.py has not been configured. EmailServerError: The application will not send any emails. Sharing notification is not available.\n" )

if __name__ == "__main__":
    execute_manager(settings)
