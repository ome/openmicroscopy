#!/bin/bash

export VMNAME=${VMNAME:-"$1"}
export VMNAME=${VMNAME:-"omerovm"}

export SSH_PF=${SSH_PF:-"$2"}
export SSH_PF=${SSH_PF:-"2222"}

set -e -u -x

cd /home/omero 
if [ ! -d ".ssh" ]; then
	mkdir .ssh
fi

chmod 0700 .ssh

if [ -f ".ssh/authorized_keys" ]; then
	cp .ssh/authorized_keys .ssh/authorized_keys.backup
	rm -f .ssh/authorized_keys
fi

cat omerokey.pub >> .ssh/authorized_keys
chmod 0600 .ssh/authorized_keys







