#!/bin/bash

export VMNAME=${VMNAME:-"$1"}
export VMNAME=${VMNAME:-"OMERO42"}

export SSH_PF=${SSH_PF:-"$2"}
export SSH_PF=${SSH_PF:-"2222"}

set -e
set -u
set -x

VBOX="VBoxManage --nologo"
SCP="spawn scp -P $SSH_PF"
SSH="spawn ssh -p $SSH_PF -t"

SSH_DIR="$HOME/.ssh"

# In order to prompt password to connect to vm user must generate local RSA key by '$ ssh-keygen -t rsa'
# then copy them to the VM
#expect -c 'spawn ssh -p 2222 -t omero@localhost ls -al; expect assword ; send "ome\n" ; interact'
#expect -c 'spawn scp -P 2222 file omero@localhost:~/; expect assword; send "ome\n"; interact'

[ -f omerokey.pub ] && {
    echo "Copying my RSA key"
    expect -c "$SCP omerokey.pub omero@localhost:~/; expect assword; send \"ome\n\"; interact"

    echo "Setup key"
    expect -c "$SSH omero@localhost cd /home/omero && cat omerokey.pub >> .ssh/authorized_keys ; expect assword ; send \"ome\n\"; interact "
    
} || echo "Local RSAAuthentication key was not found. Use: $ ssh-keygen -t rsa"






