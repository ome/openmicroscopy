#!/bin/sh
export VMNAME="OMERO"
export PASSWORD="ome"
export SSH_PF="2222"
VBoxManage startvm "$VMNAME" --type headless
scp -P $SSH_PF ubuntu-install.sh omero@localhost:~/
export DISPLAY=:0
export SSH_ASKPASS=`pwd`/fakepass.sh
ssh -l omero -p $SSH_PF localhost 'echo "$PASSWORD" | sudo -S sh /home/omero/ubuntuinstall.sh'