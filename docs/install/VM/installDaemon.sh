#!/bin/bash
sudo cp /home/omero/omero /etc/init.d
sudo update-rc.d omero start 99 3 4 5 .