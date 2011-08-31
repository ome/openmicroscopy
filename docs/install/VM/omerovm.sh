#!/bin/bash

export VMNAME=${VMNAME:-"$1"}
export VMNAME=${VMNAME:-"omerovm"}

export MEMORY=${MEMORY:-"1024"}
export SSH_PF=${SSH_PF:-"2222"}
export OMERO_PORT=${OMERO_PORT:-"4063"}
export OMERO_PF=${OMERO_PF:-"4063"}
export OMEROS_PORT=${OMEROS_PORT:-"4064"}
export OMEROS_PF=${OMEROS_PF:-"4064"}

set -e
set -u
set -x

VBOX="VBoxManage --nologo"
OS=`uname -s`
ATTEMPTS=0
MAXATTEMPTS=5
DELAY=2
NATADDR="10.0.2.15"

##################
##################
# SCRIPT FUNCTIONS
##################
##################

function checknet ()
{
	UP=$($VBOX guestproperty enumerate $VMNAME | grep "10.0.2.15") || true
	ATTEMPTS=$(($ATTEMPTS + 1))
}

function installvm ()
{
	ssh-keygen -R [localhost]:2222 -f ~/.ssh/known_hosts
	chmod 600 ./omerovmkey
	SCP="scp -2 -o NoHostAuthenticationForLocalhost=yes -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -o CheckHostIP=no -o PasswordAuthentication=no -o ChallengeResponseAuthentication=no -o PreferredAuthentications=publickey -i omerovmkey -P $SSH_PF"
	SSH="ssh -2 -o StrictHostKeyChecking=no -i omerovmkey -p $SSH_PF -t"
	echo "Copying scripts to VM"
	$SCP driver.sh omero@localhost:~/
	$SCP setup_userspace.sh omero@localhost:~/
	$SCP setup_postgres.sh omero@localhost:~/
	$SCP setup_environment.sh omero@localhost:~/
	$SCP setup_omero.sh omero@localhost:~/
	$SCP setup_omero_daemon.sh omero@localhost:~/
	$SCP omero-init.d omero@localhost:~/
	echo "ssh : exec driver.sh"
	$SSH omero@localhost 'bash /home/omero/driver.sh'
	sleep 10
	
	echo "ALL DONE!"
}

function failfast ()
{
	exit 1
}

function poweroffvm ()
{
	$VBOX list vms | grep "$VMNAME" && {
		if VBoxManage showvminfo "$VMNAME" | grep -q "running"
		then
			VBoxManage controlvm "$VMNAME" poweroff
			sleep 10
		fi
	}
}

function poweronvm ()
{
	$VBOX list runningvms | grep "$VMNAME" || {
    	#echo "Starting VM... first boot... give the VM time to boot..."
    	$VBOX startvm "$VMNAME" --type headless && sleep 45
	}

}

function rebootvm ()
{
	poweroffvm
	poweronvm
}

function killallvbox ()
{
	set +e

	ps aux | grep [V]Box && {
	
		if [ "$OS" == "Darwin" ]; then
			killall -m [V]Box
		else [ "$OS" == "Linux" ];
			killall -r [V]Box
		fi
	
	}
	
	ps aux | grep [V]irtualBox && {
	
		if [ "$OS" == "Darwin" ]; then
			killall -m [V]irtualBox
		else [ "$OS" == "Linux" ];
			killall -r [V]irtualBox
		fi
	
	}
	
	set -e
}

function checkhddfolder ()
{
	if test -e $HOME/Library/VirtualBox; then
	    export HARDDISKS=${HARDDISKS:-"$HOME/Library/VirtualBox/HardDisks/"}
	elif test -e $HOME/.VirtualBox; then
	    export HARDDISKS=${HARDDISKS:-"$HOME/.VirtualBox/HardDisks/"}
	else
	    echo "Cannot find harddisks! Trying setting HARDDISKS"
	    failfast
	fi
}

function deletevm ()
{
	poweroffvm
	
	VBoxManage storageattach "$VMNAME" --storagectl "SATA CONTROLLER" --port 0 --device 0 --type hdd --medium none
	VBoxManage unregistervm "$VMNAME" --delete
	VBoxManage closemedium disk $HARDDISKS"$VMNAME".vdi --delete

}

function createvm ()
{
		$VBOX list vms | grep "$VMNAME" || {
		VBoxManage clonehd "$HARDDISKS"omero-base-img_2011-08-08.vdi"" "$HARDDISKS$VMNAME.vdi"
		VBoxManage createvm --name "$VMNAME" --register --ostype "Debian"
		VBoxManage storagectl "$VMNAME" --name "SATA CONTROLLER" --add sata
		VBoxManage storageattach "$VMNAME" --storagectl "SATA CONTROLLER" --port 0 --device 0 --type hdd --medium $HARDDISKS$VMNAME.vdi
			
		VBoxManage modifyvm "$VMNAME" --nic1 nat --nictype1 "82545EM"
		VBoxManage modifyvm "$VMNAME" --memory $MEMORY --acpi on
	
		VBoxManage modifyvm "$VMNAME" --natpf1 "ssh,tcp,127.0.0.1,2222,10.0.2.15,22"
		VBoxManage modifyvm "$VMNAME" --natpf1 "omero-unsec,tcp,127.0.0.1,4063,10.0.2.15,4063"
		VBoxManage modifyvm "$VMNAME" --natpf1 "omero-ssl,tcp,127.0.0.1,4064,10.0.2.15,4064"
		VBoxManage modifyvm "$VMNAME" --natpf1 "omero-web,tcp,127.0.0.1,4080,10.0.2.15,4080"
	}
}

####################
####################
# SCRIPT ENTRY POINT
####################
####################

checkhddfolder

deletevm

killallvbox

createvm

poweronvm

checknet

if [[ -z "$UP" ]]
then
	while [[ -z "$UP" && $ATTEMPTS -lt $MAXATTEMPTS ]]
	do
		rebootvm
	    checknet
	    sleep $DELAY
	done
	if [[ -z "$UP" ]]
	then
    	echo "No connection to x. Failure after $ATTEMPTS tries"
    	failfast
    fi
fi

echo "Network up after $ATTEMPTS tries"
installvm