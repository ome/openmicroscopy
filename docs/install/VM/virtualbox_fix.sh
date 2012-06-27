echo $PASSWORD | sudo -S cp /home/omero/virtualbox-network-fix-init.d /etc/init.d/virtualbox-network-fix
echo $PASSWORD | sudo -S chmod a+x /etc/init.d/virtualbox-network-fix
echo $PASSWORD | sudo -S update-rc.d -f virtualbox-network-fix remove
echo $PASSWORD | sudo -S update-rc.d -f virtualbox-network-fix defaults 98 02
