#!/bin/bash
#
SCP="scp -o StrictHostKeyChecking=no -i omerokey"
SSH="ssh -o StrictHostKeyChecking=no -i omerokey"
if [ -n $VMNAME ]
then 
    if [ -n $1 ]
    then
        export VMNAME="SEPT1"
    else
        export VMNAME=$1
    fi
fi
if [ -n $SSH_PF ]
then
    if [ -n $3 ] 
    then
        export SSH_PF="2222"
    else
        export SSH_PF=$3
    fi
fi
echo "Stopping VM " 
VBoxManage controlvm "$VMNAME" poweroff
sleep 10
echo "Starting VM " 
VBoxManage startvm "$VMNAME" --type headless
sleep 30
echo "Copying ubuntu-install" 
$SCP -P $SSH_PF ubuntu-install.sh omero@localhost:~/
echo "Copying omero.sh" 
$SCP -P $SSH_PF omero.sh omero@localhost:~/omero
echo "Copying installDaemon.sh" 
$SCP -P $SSH_PF installDaemon.sh omero@localhost:~/
echo "Copying ubuntu-install" 
echo "ssh : exec ubuntu-install.sh"
$SSH -l omero -p $SSH_PF localhost 'yes ome | sudo -S sh /home/omero/ubuntu-install.sh'
echo "ssh : exec installDaemon.sh"
$SSH -l omero -p $SSH_PF localhost 'yes ome | sudo -S sh /home/omero/installDaemon.sh'
