#!/bin/bash

export VMNAME=${VMNAME:-"$1"}
export VMNAME=${VMNAME:-"SEPT1"}

export SSH_PF=${SSH_PF:-"$2"}
export SSH_PF=${SSH_PF:-"2222"}

set -e
set -u
set -x

VBOX="VBoxManage --nologo"
SCP="scp -o StrictHostKeyChecking=no -i omerokey -P $SSH_PF"
SSH="ssh -o StrictHostKeyChecking=no -i omerokey -p $SSH_PF -t"

$VBOX list runningvms | grep "$VMNAME" || {
    # Under what conditions should we restart
    # echo "Stopping VM "
    # $VBOX controlvm "$VMNAME" poweroff && sleep 10
    echo "Starting VM "
    $VBOX startvm "$VMNAME" --type headless
    sleep 30
}

echo "Copying ubuntu-install"
$SCP ubuntu-install.sh omero@localhost:~/
echo "Copying omero.sh"
$SCP omero.sh omero@localhost:~/omero.sh
echo "Copying installDaemon.sh"
$SCP installDaemon.sh omero@localhost:~/
echo "ssh : exec ubuntu-install.sh"
$SSH omero@localhost 'yes ome | sudo -S sh /home/omero/ubuntu-install.sh'
echo "ssh : exec installDaemon.sh"
$SSH omero@localhost 'yes ome | sudo -S sh /home/omero/installDaemon.sh'
