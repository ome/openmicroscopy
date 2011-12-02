/*
 * Copyright (c) 2008-2011 University of Dundee. & Open Microscopy Environment.
 * All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Author: Aleksandra Tarkowska <A(dot)Tarkowska(at)dundee(dot)ac(dot)uk>, 2008.
 */


function openHelp() {
        owindow = window.open('/webadmin/help/index.htm', '', config='height=650,width=600,left=50,top=50,toolbar=no,menubar=no,scrollbars=yes,resizable=yes,location=no,directories=no,status=no');
        if(!owindow.closed) owindow.focus();
}

function openPopup(url) {
    // IE8 doesn't support arbitrary text for 'name' 2nd arg.  #6118
    var owindow = window.open(url, '', config='height=600,width=850,left=50,top=50,toolbar=no,menubar=no,scrollbars=yes,resizable=yes,location=no,directories=no,status=no');
    if(!owindow.closed) owindow.focus();
    return false;
}


function popupActivites(url) {
    // IE8 doesn't support arbitrary text for 'name' 2nd arg.  #6118
    var w = 650;
    var h = 400;
    var left = parseInt((screen.availWidth/2) - (w/2));
    var top = parseInt((screen.availHeight - h)/3);
    var activitiesWindow = window.open(url, 'Activities', config='height='+h+',width='+w+',left='+left+',top='+top+',toolbar=no,menubar=no,scrollbars=yes,resizable=yes,location=no,directories=no,status=no');
    if(!activitiesWindow.closed) activitiesWindow.focus();
    return activitiesWindow;
}


function openCenteredWindow(url) {
    var width = 550;
    var height = 600;
    var left = parseInt((screen.availWidth/2) - (width/2));
    var top = 0 // parseInt((screen.availHeight/2) - (height/2));
    var windowFeatures = "width=" + width + ",height=" + height + ",status=no,resizable=yes,scrollbars=yes,menubar=no,toolbar=no,left=" + left + ",top=" + top + "screenX=" + left + ",screenY=" + top;
    var myWindow = window.open(url, "", windowFeatures);
    if(!myWindow.closed) myWindow.focus();
    return false;
}


/*
 *  Returns a string representing the currently selected items in the $.jstree.
 * E.g.     "Image=23,34,98&Dataset=678"
**/
function get_tree_selection() {
    var datatree = $.jstree._focused();

    var ids = new Array();
    var Data_Type = null;
    var selected = datatree.data.ui.selected;
    if (selected.size() == 1) {
        var klass = selected.attr('rel');
        if (klass == 'plate' || klass == 'acquisition') {
            var plateSelected = $('#spw .ui-selected img');
            if (plateSelected.size() > 0) {
                selected = plateSelected;
            }
        }
    }
    var selected_ids = {}
    selected.each(function() {
        var dtype = this.id.split("-")[0];
        var data_type = dtype.charAt(0).toUpperCase() + dtype.slice(1); // capitalise
        var data_id = this.id.split("-")[1];
        if (data_type in selected_ids) {
            selected_ids[data_type] += ","+data_id;
        } else {
            selected_ids[data_type] = data_id
        }
    });
    var ids_list = []
    for (key in selected_ids){
        ids_list.push(key+"="+selected_ids[key]);
    }
    return ids_list.join("&");
}


/*
 * Confirm dialog using jquery-ui dialog. http://jqueryui.com/demos/dialog/
 * This code provides a short-cut that doens't need html elements on the page
 * Basic usage (text only - Default buttons are 'OK' and 'Cancel'):
 *    var OK_dialog = confirm_dialog("Can you confirm that you want to proceed?", function() {
 *        var clicked_button_text = OK_dialog.data("clicked_button");
 *        alert(clicked_button_text);
 *    });
 *
 * Also possible to specify title, buttons, width, height:
 *    var btn_labels = ["Yes", "No", "Maybe", "Later"];
 *    var title_dialog = confirm_dialog("Can you confirm that you want to proceed?", "Dialog Title", btn_labels, 300, 200);
 */
var confirm_dialog = function(dialog_text, callback, title, button_labels, width, height) {

    if (typeof title == "undefined") var title = "Confirm";
    if (typeof width == "undefined") var width = 350;
    if (typeof height == "undefined") var height = 140;

    var $dialog = $("#confirm_dialog");
    if ($dialog.length > 0) {       // get rid of any old dialogs
        $dialog.remove();
    }
    $dialog = $("<div id='confirm_dialog'></div>");
    $('body').append($dialog);

    $dialog.attr("title", title).hide();
    $dialog.html("<p>"+ dialog_text +"</p>");

    if (typeof button_labels == "undefined") {
        button_labels = ['OK', 'Cancel']
    }
    var btns = {}
    for (var i=0; i<button_labels.length; i++) {
        var b = button_labels[i];
        btns[b] = function(event) {
            var btxt = $(event.target).text();
            $dialog.data("clicked_button", btxt);
            $( this ).dialog( "close" );
        }
    }

    $dialog.dialog({
        resizable: true,
        height: height,
        width: width,
        modal: true,
        buttons: btns
    });
    $dialog.bind("dialogclose", callback);

    return $dialog;
};


/*
 * A dialog for logging-in on the fly (without redirect to login page)
 * On 'Connect' we post username & password to login url and on callback, the specified function is called
 */
var login_dialog = function(login_url, callback) {

    var $dialog = $("#login_dialog");
    if ($dialog.length > 0) {       // get rid of any old dialogs
        $dialog.remove();
    }
    $dialog = $("<div id='login_dialog'></div>");
    $('body').append($dialog);

    $dialog.attr("title", "Login").hide();
    $dialog.html("<form>Username:<input type='text' name='username' id='login_username' /><br />Password:<input type='text' name='password' id='login_password'/>");

    $dialog.dialog({
        resizable: true,
        height: 200,
        width: 300,
        modal: true,
        buttons: {
            "Cancel": function() {
                $( this ).dialog( "close" );
            },
            "Connect": function() {
                var username = $("#login_username").val();
                var password = $("#login_password").val();
                $.post(login_url, {'password':password, 'username':username},  function(data) {
                    console.log("logged-in...");
                    callback();
                });
                $( this ).dialog( "close" );
            }
        }
    });
    $dialog.bind("dialogclose", callback);

    return $dialog;
};