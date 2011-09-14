The purpose of these scripts is to automate the process of building an OMERO virtual machine

1. Retrieve the OMERO VM scripts:
	(A) Using Git:
			$ git archive --remote=git://git.openmicroscopy.org/ome.git --format=tar HEAD:docs/install/VM > OMERO_VM.tar

	(C) If you have ssh access to the OMERO git repo then you can also use git as follows, replacing $USERNAME with your user name as required:
		$ git archive --remote=ssh://$USERNAME@git.openmicroscopy.org/home/git/ome.git --format=tar HEAD:docs/install/VM > OMERO_VM.tar

	(C)	Via HTTP by download the scripts manually from:
		http://git.openmicroscopy.org/?p=ome.git;a=tree;f=docs/install/VM;hb=HEAD
	& saving ALL of the contents of that directory to a suitable location on your machine. NB. This is the least desireable option and requires more work on your part.

2. Prerequisites:
	A Linux or Mac OS X operating system
	A recent & working Virtual Box install
	
3. Environment Setup:
	Retrieve the OMERO VM base VDI from either:
		team/virtual-machines/OMERO.VM/vdi/omero-base-img_2011-08-08.vdi [2.5GB]
	on squig, or via the public web repo:	
		http://
	
	Place the VDI in your VirtualBox harddisk directory. On Mac OS X this should be $HOME/Library/VirtualBox/HardDisks/ and on Linux $HOME/.VirtualBox/HardDisks/
	The OMERO.VM script will look in these locations for the VDI to kick start the VM building process.

4. [OPTIONAL] In setup_omero.sh you can define whether you wish to build OMERO from source, use the most recent QA build or use the release build by setting the TARGET var. Valid values for TARGET are QA or SRC or RELEASE. Anything else will cause the latest QA build to be used by default. The is for latest trunk QA builds from the Hudson build process. To specify a different build you can adjust the following vars, DL_LOC & DL_ARCHIVE:
	
	DL_LOC stores the URL from which to retrieve our build:
		DL_LOC="http://url-of-your-download-folder"
	e.g.
		DL_LOC="http://hudson.openmicroscopy.org.uk/job/OMERO-trunk-qa-builds/lastSuccessfulBuild/artifact/"
	
	DL_ARCHIVE stores the name of the zip archive to retrieve from DL_LOC because the build process could deploy many archives to that location and we must specify the particular one that we want to retrieve.
	DL_ARCHIVE="omero.server.archive.zip"
	e.g.
		DL_ARCHIVE="OMERO.server-4.3.0-DEV-bfe035dd.zip"

	If using a release build then you can also alter the RELEASE_ARCHIVE var to reflect the build of OMERO.server that you want to install. 
	
7. Run the VM build script:
	$ bash omerovm.sh $VMNAME
	e.g.	$ bash omerovm.sh omero-vm
	to build a VM named omero-vm

8. This should take roughly 8-15 minutes to complete depending upon your machine so go and grab a coffee

9. When you see the message "All Done!" you should be able to either:
(a) Start an OMERO client such as OMERO.insight and connect to your VM
(b) SSH into your VM using the IP address printed at the end of the build script
(c) Use the utility script connect.sh to open a shell into your vm
	$ bash connect.sh $VMNAME
	e.g.	$ bash connect.sh omero-vm
	to connect directly and automatically to the OMERO.VM named omero-vm.
	
Utility Scripts
===============

A number of utility scripts are included to enable you to easily start, stop, and connect to your VM:

	connect.sh - To get a shell on a named OMERO VM, supply the name of the VM that you wish to connect to, e.g. bash connect.sh omero-vm
	start.sh - To start a stopped OMERO VM, supply the name of VM you wish to run, e.g. bash start.sh omero-vm
	stop.sh - To stop a started OMERO VM, supply the name of VM you wish to halt, e.g. bash stop.sh omero-vm
	setup_port_forwarding.sh - To automatically set up port forwarding settings for the nominated VM in VirtualBox, e.g. bash setup_port_forwarding.sh omero-vm