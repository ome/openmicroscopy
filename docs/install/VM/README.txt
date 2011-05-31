The purpose of these scripts is to automate the process of building an OMERO virtual machine

1. Retrieve the OMERO VM scripts:
(a) If you have ssh access to the OMERO git repo then:
	$ git archive --remote=ssh://szwells@git.openmicroscopy.org/home/git/ome.git --format=tar HEAD:docs/install/VM > omerovmscripts.tar
(b) Download the scripts from:
	http://git.openmicroscopy.org/?p=ome.git;a=tree;f=docs/install/VM;hb=HEAD
& save them all to suitable directory on your machine.

2. Prerequisites:
	Linux or Mac OS X operating system
	A recent & working Virtual Box install
	
3. Environment Setup:
	Retrieve the OMERO VM base VDI from either:
		team/virtual-machines/OMERO.VM/vdi/omero-base-image-debian-6.vdi [1.2GB]
	or
		team/virtual-machines/OMERO.VM/vdi/omero-base-image-debian-6.vdi.gz [388MB]
	on squig, or via the public web repo:	
		http://
	NB. If you download the zipped version then you will have to unzip to before use.
	
	Place the VDI in your VirtualBox harddisk directory. On Mac OS X this should be $HOME/Library/VirtualBox/HardDisks/ and on Linux $HOME/.VirtualBox/HardDisks/
	The OMERO.VM script will look in these locations for the VDI to kick start the VM building process.

4. If you have an entry in your known_hosts file [ .ssh/known_hosts ] for localhost port 2222 then you should remove that line from known_hosts so as to avoid conflict with the host key of your new VM. If the omerovm.sh script stalls several lines after booting the new VM then this is generally the cause.

5. [OPTIONAL] If you do not wish to use the supplied keypair then you can create a new SSH key pair using:
	$ ssh-keygen -t rsa
	and save it as omerokey. NB. If you have done this correctly then you should have a keypair called omerokey and omerokey.pub in your .ssh folder. Copy these to the folder containing your OMERO.VM scripts
	NB. You might get an unprotected private key file warning from SCP. If this happens then you should check the permissions of the omerokey files and ensure that they are chmod 600. If not then:
			$ chmod 600 omerokey*
		in the OMERO.VM directory.

6. In the setup_omero.sh script alter the DL_ARCHIVE var to reflect the build of OMERO.server that you want to install. The current default is for QA builds from the hudson build process. To specify a different build you can adjust the following vars:
		DL_LOC="http://hudson.openmicroscopy.org.uk/job/OMERO-trunk-qa-builds/lastSuccessfulBuild/artifact/"
		DL_ARCHIVE="OMERO.server-4.3.0-DEV-bfe035dd.zip"
	
7. Run the VM build script:
	$ sh omerovm.sh $VMNAME
	e.g.	$ sh omerovm.sh omero-vm
	to build a VM named omero-vm

8. This should take roughly 8-15 minutes to complete depending upon your machine so go and grab a coffee

9. When you see the message "All Done!" you should be able to either:
(a) Start an OMERO client such as OMERO.insight and connect to your VM
(b) SSH into your VM using the IP address printed at the end of the build script
(c) Use the utility script connect.sh to open a shell into your vm
	$ sh connect.sh $VMNAME
	e.g.	$ sh connect.sh omero-vm
	to connect directly and automatically to the OMERO.VM named omero-vm.
	
Utility Scripts
===============

A number of utility scripts are included to enable you to easily start, stop, and connect to your VM:

	connect.sh - To get a shell on a named OMERO VM, supply the name of the VM that you wish to connect to, e.g. sh connect.sh omero-vm
	start.sh - To start a stopped OMERO VM, supply the name of VM you wish to run, e.g. sh start.sh omero-vm
	stop.sh - To stop a started OMERO VM, supply the name of VM you wish to halt, e.g. sh stop.sh omero-vm
	
KNOWN ISSUES
============

1. A very infrequent issue is that the IP address held by the VirtualBox DHCP server & output by the network script & at the end of omerovm.sh can sometimes get out of sync with the actual IP address that the VM itself has. The VM will still function and the solution is to log in directly to the VM and run ifconfig. If you try to connect to the server using Insight and it doesn't work then this is something to check. This only happens very occasionally, usually when creating, starting, stopping, and deleting a lot of VMs, such as when I was testing this script.

2. If you cannot connect to the OMERO server using an OMERO client, the connect script or SSH then there is a problem with networking in the VM. Restart your VM using the stop.sh and start.sh scripts then log into your VM directly via the VirtualBox console and investigate the network settings. You might be able to do:
		$ sudo dhclient $INTERFACE
		$ sudo /etc/init.d/networking restart
	NB. If this doesn't work you might have to delete /etc/udev/rules.d/70-persistent-net.rules then restart the udev daemon:
		$ /etc/init.d/udev restart