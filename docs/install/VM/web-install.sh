#!/bin/bash


##
# Setup FastCGI
cd /etc/apache2/sites-available
cat > default << EOF
FastCGIExternalServer "/var/www/omero.fcgi" -host 127.0.0.1:8000

<VirtualHost *:80>
        ServerAdmin webmaster@localhost

        DocumentRoot /var/www
        <Directory />
                Options FollowSymLinks
                AllowOverride None
        </Directory>
        <Directory /var/www/>
                Options Indexes FollowSymLinks MultiViews
                AllowOverride None
                Order allow,deny
                allow from all
        </Directory>

        ScriptAlias /cgi-bin/ /usr/lib/cgi-bin/
        <Directory "/usr/lib/cgi-bin">
                AllowOverride None
                Options +ExecCGI -MultiViews +SymLinksIfOwnerMatch
                Order allow,deny
                Allow from all
        </Directory>

        ErrorLog /var/log/apache2/error.log

        # Possible values include: debug, info, notice, warn, error, crit,
        # alert, emerg.
        LogLevel warn

        CustomLog /var/log/apache2/access.log combined

    Alias /doc/ "/usr/share/doc/"
    <Directory "/usr/share/doc/">
        Options Indexes MultiViews FollowSymLinks
        AllowOverride None
        Order deny,allow
        Deny from all
        Allow from 127.0.0.0/255.0.0.0 ::1/128
    </Directory>


    Alias / "/var/www/omero.fcgi/"

</VirtualHost>
EOF

##
# Setup Matplotlib
#
mkdir -p /Server/logs/matplotlib
sudo echo "backend: Agg" > /Server/logs/matplotlib/matplotlibrc
#sudo chown -R www-data:www-data /Server/logs/matplotlib/

##
# Setup Webclient
#
sudo -u omero mkdir -p /Server/omero/dist/var
sudo -u omero mkdir -p /Server/omero/dist/var/lib
sudo -u omero chmod +rx /Server/omero/dist/var/
cd /Server/omero/dist/var/lib

FILE=custom_settings.py
sudo -u omero cat > ${FILE} << EOF
#!/usr/bin/env python
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
#LOGDIR = '/home/jboss/web-log/'
SERVER_LIST = (
    ('localhost', 4064, 'omero'),
)

#ADMINS = (
#    ('username', 'emailaddress'),
#)

SERVER_EMAIL = 'omero@localhost'
EMAIL_HOST = 'localhost'

APPLICATION_HOST='http://localhost/'
APPLICATION_SERVER='fastcgi-tcp'

EOF

cd /Server/omero/dist
sudo -u omero bin/omero web syncmedia
sudo -u omero bin/omero web start &