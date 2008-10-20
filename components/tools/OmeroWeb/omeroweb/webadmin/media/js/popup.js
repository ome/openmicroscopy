/*
 * Help
 *
 * Copyright (c) 2008 University of Dundee. All rights reserved.
 * Author: Aleksandra Tarkowska
 * Use is subject to license terms supplied in LICENSE.txt
 *
 * Version: 1.0
 */


function openHelp() {
        owindow = window.open('/webadmin/help/index.htm', 'window', config='height=650,width=600,left=50,top=50,toolbar=no,menubar=no,scrollbars=yes,resizable=yes,location=no,directories=no,status=no');
        if(!owindow.closed) owindow.focus();
}