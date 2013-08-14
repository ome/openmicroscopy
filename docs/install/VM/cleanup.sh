#!/bin/bash

set -e -u -x

sudo userdel -f -r vagrant
sudo rm -f /etc/sudoers.d/vagrant
