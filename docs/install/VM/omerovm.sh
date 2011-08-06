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

if test -e $HOME/Library/VirtualBox; then
    export HARDDISKS=${HARDDISKS:-"$HOME/Library/VirtualBox/HardDisks/"}
elif test -e $HOME/.VirtualBox; then
    export HARDDISKS=${HARDDISKS:-"$HOME/.VirtualBox/HardDisks/"}
else
    echo "Cannot find harddisks! Trying setting HARDDISKS"
    exit 3
fi

$VBOX list vms | grep "$VMNAME" && {
	if VBoxManage showvminfo "$VMNAME" | grep -q "running"
	then
	VBoxManage controlvm "$VMNAME" poweroff
	sleep 10
	fi
	VBoxManage storageattach "$VMNAME" --storagectl "SATA CONTROLLER" --port 0 --device 0 --type hdd --medium none
	VBoxManage unregistervm "$VMNAME" --delete
	VBoxManage closemedium disk $HARDDISKS"$VMNAME".vdi --delete
}


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



$VBOX list vms | grep "$VMNAME" || {
	VBoxManage clonehd "$HARDDISKS"omero-base-img_2011-08-04.vdi"" "$HARDDISKS$VMNAME.vdi"
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

$VBOX list runningvms | grep "$VMNAME" || {
    echo "Starting VM... first boot... give the VM time to boot..."
    $VBOX startvm "$VMNAME" --type headless && sleep 45
}

$VBOX guestproperty enumerate $VMNAME | grep "10.0.2.15" && {
	
	ssh-keygen -R [localhost]:2222 -f ~/.ssh/known_hosts
	rm -f omerokey omerokey.pub
	ssh-keygen -t dsa -f omerokey -N ''

	echo "Setting omerokey permissions"
	chmod 600 ./omerokey

	SCP="scp -2 -o NoHostAuthenticationForLocalhost=yes -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -o CheckHostIP=no -o PasswordAuthentication=no -o ChallengeResponseAuthentication=no -o PreferredAuthentications=publickey -i omerokey -P $SSH_PF"

	SSH="ssh -2 -o StrictHostKeyChecking=no -i omerokey -p $SSH_PF -t"

	SCP_K="scp -o StrictHostKeyChecking=no -o NoHostAuthenticationForLocalhost=yes -P $SSH_PF"
	SSH_K="ssh -o StrictHostKeyChecking=no -p $SSH_PF -t"


[ -f omerokey.pub ] && {
	echo "Copying my DSA key"
	
#	The following two lines work on Bash under Debian, Ubuntu & Mac OS X under normal Shell usage
#	expect -c "spawn $SCP_K omerokey.pub omero@localhost:~/; expect \"*?assword:*\"; send \"omero\n\r\"; interact"
#	expect -c "spawn $SCP_K setup_keys.sh omero@localhost:~/; expect \"*?assword:*\"; send \"omero\n\r\"; interact"

#	The	following two lines work on Bash under Debian, Ubuntu & Mac OS X when built using Hudons/Jenkins
	expect -c "
		spawn $SCP_K omerokey.pub omero@localhost:~/; 
		expect { \"*?assword:*\"; { send \"omero\r\n\"; interact }
		eof { exit }
		} exit"
				
	expect -c "
		spawn $SCP_K setup_keys.sh omero@localhost:~/; 
		expect { \"*?assword:*\"; { send \"omero\r\n\"; interact }
		eof { exit }
		} exit"

	echo "Setup key"
	expect -c "
		spawn $SSH_K omero@localhost sh /home/omero/setup_keys.sh; 
		expect { \"*?assword:*\"; { send \"omero\r\n\"; interact }
		eof { exit }
		} exit"

	} || echo "Local DSAAuthentication key was not found. Use: $ ssh-keygen -t dsa"


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

	sleep 60
	
	echo "ALL DONE!"
	echo "Connect to your OMERO VM using either OMERO.insight or another OMERO client or SSH using the connect.sh script"
	echo "Your VM has the following IP addresses:"
	VBoxManage guestproperty enumerate $VMNAME | grep IP

} || "VM has no network connection (VirtualBox error http://www.virtualbox.org/ticket/4038). Rerun omerovm.sh"