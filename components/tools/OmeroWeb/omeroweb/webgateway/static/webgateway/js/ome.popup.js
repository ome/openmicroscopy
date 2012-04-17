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

String.prototype.capitalize = function() {
    return this.charAt(0).toUpperCase() + this.slice(1);
}

jQuery.fn.alternateRowColors = function() {
    var $rows = $(this).children().children('tr');
    $rows.not('.hidden').filter(':odd').removeClass('even').addClass('odd');
    $rows.not('.hidden').filter(':even').removeClass('odd').addClass('even');
  return this;
}

function openPopup(url) {
    // IE8 doesn't support arbitrary text for 'name' 2nd arg.  #6118
    var owindow = window.open(url, '', config='height=600,width=850,left=50,top=50,toolbar=no,menubar=no,scrollbars=yes,resizable=yes,location=no,directories=no,status=no');
    if(!owindow.closed) owindow.focus();
    return false;
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
    if (typeof $.jstree == "undefined") return "";
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
 *    var title_dialog = confirm_dialog("Can you confirm that you want to proceed?", 
 *          function() { alert( title_dialog.data("clicked_button") },
 *          "Dialog Title", btn_labels, 300, 200);
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