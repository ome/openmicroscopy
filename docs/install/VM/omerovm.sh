#!/bin/bash

export VMNAME=${VMNAME:-"$1"}
export VMNAME=${VMNAME:-"omero-vm"}

export MEMORY=${MEMORY:-"1024"}
export SSH_PF=${SSH_PF:-"2222"}
export OMERO_PORT=${OMERO_PORT:-"4063"}
export OMERO_PF=${OMERO_PF:-"4063"}
export OMEROS_PORT=${OMEROS_PORT:-"4064"}
export OMEROS_PF=${OMEROS_PF:-"4064"}

export OMEROWEB_PORT=${OMEROWEB_PORT:-"80"}
export OMEROWEB_PF=${OMEROWEB_PF:-"8080"}

set -e
set -u
set -x

if test -e $HOME/Library/VirtualBox; then
    export HARDDISKS=${HARDDISKS:-"$HOME/Library/VirtualBox/HardDisks/"}
elif test -e $HOME/.VirtualBox; then
    export HARDDISKS=${HARDDISKS:-"$HOME/.VirtualBox/HardDisks/"}
else
    echo "Cannot find harddisks! Trying setting HARDDISKS"
    exit 3
fi

VBOX="VBoxManage --nologo"

$VBOX list vms | grep "$VMNAME" || {
	VBoxManage clonehd "$HARDDISKS"omero-base-image-debian-6.vdi"" "$HARDDISKS$VMNAME.vdi"
	VBoxManage createvm --name "$VMNAME" --register --ostype "Debian"
	VBoxManage storagectl "$VMNAME" --name "IDE CONTROLLER" --add ide
	VBoxManage storagectl "$VMNAME" --name "SATA CONTROLLER" --add sata
	VBoxManage storageattach "$VMNAME" --storagectl "SATA CONTROLLER" --port 0 --device 0 --type hdd --medium $HARDDISKS$VMNAME.vdi
	
	#VBoxManage modifyvm "$VMNAME" --nic1 nat --nictype1 "Am79C973"
	#VBoxManage modifyvm "$VMNAME" --nic2 hostonly --nictype2 "Am79C973" --hostonlyadapter2 "vboxnet0"
	
	VBoxManage modifyvm "$VMNAME" --nic1 nat --nictype1 "82540EM"
	VBoxManage modifyvm "$VMNAME" --nic2 hostonly --nictype2 "82540EM" --hostonlyadapter2 "vboxnet0"
	
	VBoxManage modifyvm "$VMNAME" --memory $MEMORY --acpi on
	
#	VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/pcnet/0/LUN#0/Config/ssh/HostPort" $SSH_PF
#	VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/pcnet/0/LUN#0/Config/ssh/GuestPort" 22
#	VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/pcnet/0/LUN#0/Config/ssh/Protocol" TCP
#	VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/pcnet/0/LUN#0/Config/omeroserver/HostPort" $OMERO_PF
#	VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/pcnet/0/LUN#0/Config/omeroserver/GuestPort" $OMERO_PORT
#	VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/pcnet/0/LUN#0/Config/omeroservers/Protocol" TCP
#	VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/pcnet/0/LUN#0/Config/omeroservers/HostPort" $OMEROS_PF
#	VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/pcnet/0/LUN#0/Config/omeroservers/GuestPort" $OMEROS_PORT
#	VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/pcnet/0/LUN#0/Config/omeroserver/Protocol" TCP
#	VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/pcnet/0/LUN#0/Config/omeroweb/HostPort" $OMEROWEB_PF
#	VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/pcnet/0/LUN#0/Config/omeroweb/GuestPort" $OMEROWEB_PORT
#	VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/pcnet/0/LUN#0/Config/omeroweb/Protocol" TCP

	VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/e1000/0/LUN#0/Config/ssh/HostPort" $SSH_PF
	VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/e1000/0/LUN#0/Config/ssh/GuestPort" 22
	VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/e1000/0/LUN#0/Config/ssh/Protocol" TCP
	VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/e1000/0/LUN#0/Config/omeroserver/HostPort" $OMERO_PF
	VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/e1000/0/LUN#0/Config/omeroserver/GuestPort" $OMERO_PORT
	VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/e1000/0/LUN#0/Config/omeroservers/Protocol" TCP
	VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/e1000/0/LUN#0/Config/omeroservers/HostPort" $OMEROS_PF
	VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/e1000/0/LUN#0/Config/omeroservers/GuestPort" $OMEROS_PORT
	VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/e1000/0/LUN#0/Config/omeroserver/Protocol" TCP
	VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/e1000/0/LUN#0/Config/omeroweb/HostPort" $OMEROWEB_PF
	VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/e1000/0/LUN#0/Config/omeroweb/GuestPort" $OMEROWEB_PORT
	VBoxManage setextradata "$VMNAME" "VBoxInternal/Devices/e1000/0/LUN#0/Config/omeroweb/Protocol" TCP
	
	sleep 5
}

$VBOX list runningvms | grep "$VMNAME" || {
    echo "Starting VM..."
    $VBOX startvm "$VMNAME" --type headless
    echo "Give the VM time to boot..."
    sleep 20
}


# TESTING key setup procedures :: START
# Remove entry from known_hosts for old key
ssh-keygen -R [localhost]:2222 -f ~/.ssh/known_hosts

# Delete any old keys that are hanging around
rm -f omerokey omerokey.pub

# Create clean new keys
ssh-keygen -t dsa -f omerokey -N ''
# TESTING key setup procedures :: END

cp omerokey ~/.ssh/omerokey
cp omerokey.pub ~/.ssh/omerokey.pub

echo "Setting omerokey permissions"
chmod 600 ./omerokey
chmod 600 ~/.ssh/omerokey*

#SCP="scp -2 -v -o NoHostAuthenticationForLocalhost=yes -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -o CheckHostIP=no PasswordAuthentication=no -o ChallengeResponseAuthentication=no -o PreferredAuthentications=publickey -i ~/VM/omerokey -P $SSH_PF"
#SSH="ssh -2 -v -o NoHostAuthenticationForLocalhost=yes -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -o CheckHostIP=no PasswordAuthentication=no -o ChallengeResponseAuthentication=no -o PreferredAuthentications=publickey -i ~/VM/omerokey -p $SSH_PF -t"

#SCP_K="spawn scp -2 -vvv -o UserKnownHostsFile=/dev/null -o NoHostAuthenticationForLocalhost=yes -o StrictHostKeyChecking=no -o CheckHostIP=no -P $SSH_PF"
#SSH_K="spawn ssh -2 -vvv -o UserKnownHostsFile=/dev/null -o NoHostAuthenticationForLocalhost=yes -o StrictHostKeyChecking=no -o CheckHostIP=no -p $SSH_PF -t"

#SCP_K="spawn scp -o UserKnownHostsFile=/dev/null -o NoHostAuthenticationForLocalhost=yes -o StrictHostKeyChecking=no -o CheckHostIP=no -P $SSH_PF"
#SSH_K="spawn ssh -o UserKnownHostsFile=/dev/null -o NoHostAuthenticationForLocalhost=yes -o StrictHostKeyChecking=no -o CheckHostIP=no -p $SSH_PF -t"

SCP_K="spawn scp -vvv -P $SSH_PF"
SSH_K="spawn ssh -vvv -p $SSH_PF -t"


[ -f omerokey.pub ] && {
    echo "Copying my DSA key"
    expect -c "$SCP_K omerokey.pub omero@localhost:~/; expect assword; send \"omero\n\"; interact"
    expect -c "$SCP_K setup_keys.sh omero@localhost:~/; expect assword; send \"omero\n\"; interact"

    echo "Setup key"
    expect -c "$SSH_K omero@localhost sh /home/omero/setup_keys.sh; expect assword; send \"omero\n\"; interact"
    
} || echo "Local DSAAuthentication key was not found. Use: $ ssh-keygen -t dsa"

#echo "Copying scripts to VM"
#$SCP driver.sh omero@localhost:~/
#$SCP setup_userspace.sh omero@localhost:~/
#$SCP setup_environment.sh omero@localhost:~/
#$SCP setup_omero.sh omero@localhost:~/
#$SCP omero-init.d omero@localhost:~/
#
#echo "ssh : exec driver.sh"
#$SSH omero@localhost 'sh /home/omero/driver.sh'
#
#sleep 40
#
echo "ALL DONE!"
echo "Connect to your OMERO VM using either OMERO.insight or another OMERO client or SSH using the connect.sh script"
echo "Your VM has the following IP addresses:"
VBoxManage guestproperty enumerate $VMNAME | grep IP
