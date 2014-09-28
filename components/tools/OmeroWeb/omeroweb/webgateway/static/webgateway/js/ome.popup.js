/*
 * Copyright (c) 2008-2013 University of Dundee. & Open Microscopy Environment.
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

/*global OME:true */
if (typeof OME === "undefined") {
    OME = {};
}

// Use userAgent to detect mobile devices
// from http://stackoverflow.com/questions/3514784/what-is-the-best-way-to-detect-a-handheld-device-in-jquery
OME.isMobileDevice = function() {
    return (/Android|webOS|iPhone|iPad|iPod|BlackBerry|PlayBook|IEMobile|Opera Mini|Mobile Safari|Silk/i).test(navigator.userAgent);
};

OME.rgbToHex = function rgbToHex(rgb) {
    if (rgb.substring(0,1) == '#') {
        return rgb.substring(1);
    }
    var rgbvals = /rgb\((.+),(.+),(.+)\)/i.exec(rgb);
    if (!rgbvals) return rgb;
    var rval = parseInt(rgbvals[1], 10).toString(16);
    var gval = parseInt(rgbvals[2], 10).toString(16);
    var bval = parseInt(rgbvals[3], 10).toString(16);
    if (rval.length == 1) rval = '0' + rval;
    if (gval.length == 1) gval = '0' + gval;
    if (bval.length == 1) bval = '0' + bval;
    return (
        rval +
        gval +
        bval
    ).toUpperCase();
};

OME.hexToRgb = function hexToRgb(hex) {
    hex = OME.rgbToHex(hex);    // in case 'hex' is actually rgb!

    var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
    return result ? {
        r: parseInt(result[1], 16),
        g: parseInt(result[2], 16),
        b: parseInt(result[3], 16)
    } : null;
};

// Calculate value, saturation and hue as in org.openmicroscopy.shoola.util.ui.colour.HSV
OME.isDark = function(color) {

    var c = OME.hexToRgb(color);

    var min, max, delta;
    var v, s, h;

    min = Math.min(c.r, c.g, c.b);
    max = Math.max(c.r, c.g, c.b);

    v = max;
    delta = max-min;

    if (max !== 0) {
        s = delta/max;
    }
    else {
        v = 0;
        s = 0;
        h = 0;
    }

    if (c.r==max) {
        h = (c.g-c.b)/delta;
    } else if (c.g == max) {
        h = 2 + (c.b-c.r)/delta;
    } else {
        h = 4 +(c.r-c.g)/delta;
    }

    h = h * 60;
    if (h < 0) {
        h += 360;
    }
    h = h/360;
    v = v/255;

    return (v < 0.6 || (h > 0.6 && s > 0.7));
};

function isInt(n){
    return typeof n== "number" && isFinite(n) && n%1===0;
}

Number.prototype.filesizeformat = function (round) {
    /*
    Formats the value like a 'human-readable' file size (i.e. 13 KB, 4.1 MB,
    102 bytes, etc).*/
    
    if (round === undefined || !isInt(round)) round = 2;
    
    var bytes = this;
    if (bytes < 1024) {
        return bytes + ' B';
    } else if (bytes < (1024*1024)) {
        return (bytes / 1024).toFixed(round) + ' KB';
    } else if (bytes < (1024*1024*1024)) {
        return (bytes / (1024*1024)).toFixed(round) + ' MB';
    } else if (bytes < (1024*1024*1024*1024)) {
        return (bytes / (1024*1024*1024)).toFixed(round) + ' GB';
    } else if (bytes < (1024*1024*1024*1024*1024)) {
        return (bytes / (1024*1024*1024*1024)).toFixed(round) + ' TB';
    } else {
        return (bytes / (1024*1024*1024*1024*1024)).toFixed(round) + ' PB';
    }
    
};

Number.prototype.lengthformat = function (round) {
    if (round === undefined || !isInt(round)) round = 2;
    
    var length = this;
    if (length < 0.001) {
        return (length * 1000 * 1000).toFixed(round) + ' pm';
    } else if (length < 0.1) {
        return (length * 1000 * 10).toFixed(round) + ' &#8491;';
    } else if (length < 1) {
        return (length * 1000).toFixed(round) + ' nm';
    } else if (length < 1000) {
        return length.toFixed(round) + ' &#181m';
    } else if (length < 1000 * 100) {
        return (length / 1000).toFixed(round) + ' mm';
    } else if (length < 1000 * 100 * 10) {
        return (length / 1000 / 100).toFixed(round) + ' cm';
    } else if (length < 1000 * 100 * 10 * 100) {
        return (length / 1000 / 100 / 10).toFixed(round) + ' m';
    } else {
        return (length / 1000 / 100 / 10 / 1000).toFixed(round) + ' km';
    }
};

String.prototype.escapeHTML = function(){
    /*
    HTML Escape Before Inserting Untrusted Data into HTML Element Content
    https://www.owasp.org/index.php/XSS_%28Cross_Site_Scripting%29_Prevention
    _Cheat_Sheet#RULE_.231_-_HTML_Escape_Before_Inserting_Untrusted_Data_into
    _HTML_Element_Content
    */
    var s = this;
    if (!s) {
        return "";
    }
    s = s + "";
    return s.replace(/[\&"<>\\]/g, function(s) {
        switch(s) {
            case "&": return "&amp;";
            case "\\": return "&#92;";
            case '"': return '\"';
            case "<": return "&lt;";
            case ">": return "&gt;";
            default: return s;
        }
    });
};

String.prototype.capitalize = function() {
    return this.charAt(0).toUpperCase() + this.slice(1);
};

jQuery.fn.alternateRowColors = function() {
    var $rows = $(this).children().children('tr');
    $rows.not('.hidden').filter(':odd').removeClass('even').addClass('odd');
    $rows.not('.hidden').filter(':even').removeClass('odd').addClass('even');
  return this;
};


// Call this on an <input> to only allow numbers.
// I rejects all non-numeric characters but allows paste (then checks value)
// By default it only allows positive ints.
// To allow negative or float values use $(".number").numbersOnly({negative:true, float:true});
jQuery.fn.numbersOnly = function(options) {

    // First, save the current value (assumed to be valid)
    this.each(function() {
        $(this).data('numbersOnly', $(this).val());
    })
    .keypress(function(event){

        // we allow copy, paste, left or right
        var allowedChars = [37, 39, 99, 118];
        if (options && options.negative) {
            allowedChars.push(45);
        }
        if (options && options.float) {
            allowedChars.push(46);
        }
        // Reject keypress if not a number and NOT one of our allowed Chars
        var charCode = (event.which) ? event.which : event.keyCode;
        if (charCode > 31 && (charCode < 48 || charCode > 57) && allowedChars.indexOf(charCode) == -1) {
            return false;
        }

        // We've allowed keypress (including paste)...
        //finally check field value after waiting for keypress to update...
        var $this = $(this);
        setTimeout(function(){
            var n = $this.val();
            var isNumber = function(n) {
                if (n.length === 0) {
                    return true;        // empty strings are allowed
                }
                return !isNaN(parseFloat(n)) && isFinite(n);
            };
            // If so, save to 'data', otherwise revert to 'data'
            if (isNumber(n)) {
                $this.data('numbersOnly', n);     // update
            } else {
                $this.val( $this.data('numbersOnly') );
            }
        }, 10);

        return true;
    });

    return this;
};

OME.openPopup = function(url) {
    // IE8 doesn't support arbitrary text for 'name' 2nd arg.  #6118
    var owindow = window.open(url, '', 'height=600,width=850,left=50,top=50,toolbar=no,menubar=no,scrollbars=yes,resizable=yes,location=no,directories=no,status=no');
    if(!owindow.closed) {
        owindow.focus();
    }
    return false;
};


OME.openCenteredWindow = function(url, w, h) {
    var width = w ? +w : 550;
    var height = h ? +h : 600;
    var left = parseInt((screen.availWidth/2) - (width/2), 10);
    var top = 0;
    var windowFeatures = "width=" + width + ",height=" + height + ",status=no,resizable=yes,scrollbars=yes,menubar=no,toolbar=no,left=" + left + ",top=" + top + "screenX=" + left + ",screenY=" + top;
    var myWindow = window.open(url, "", windowFeatures);
    if(!myWindow.closed) {
        myWindow.focus();
    }
    return false;
};


OME.openScriptWindow = function(event, width, height) {
    // open script url, providing Data_Type and IDs params in request
    var script_url = $(event.target).attr('href');
    if (script_url == "#") return false;

    // selected is list of {'id':'image-123'} etc.
    var selected = $("body").data("selected_objects.ome"),
        sel_types = {};
    if (typeof selected !== "undefined") {
        for (var i=0; i<selected.length; i++) {
            var type = selected[i].id.split("-")[0],
                oid = selected[i].id.split("-")[1];
            if (typeof sel_types[type] === "undefined") {
                sel_types[type] = [];
            }
            sel_types[type].push(oid);
        }
        var args = [];
        for (var key in sel_types) {
            // If in SPW with wells selected, handy to know what 'field'
            if (key === "well") {
                // grab the index select value:
                if ($("#id_index").length > 0) {
                    args.push("Index=" + $("#id_index").val());
                }
            }
            if (sel_types.hasOwnProperty(key)){
                args.push(key.capitalize() + "=" + sel_types[key].join(","));
            }
        }
        script_url += "?" + args.join("&");
    }
    OME.openCenteredWindow(script_url, width, height);
    return false;
};


/*
 *  Returns a string representing the currently selected items in the $.jstree.
 * E.g.     "Image=23,34,98&Dataset=678"
**/
OME.get_tree_selection = function() {
    if (typeof $.jstree === "undefined") {
        return "";
    }
    var datatree = $.jstree.reference('#dataTree');

    var selected = datatree.get_selected(true);
    if (selected.length == 1) {
        var klass = selected[0].type;
        if (klass == 'plate' || klass == 'acquisition') {
            var plateSelected = $('#spw .ui-selected img');
            if (plateSelected.size() > 0) {
                selected = plateSelected;
            }
        }
    }

    var selected_ids = {};

    $.each(selected, function(index, node) {
        var dtype = node.type;
        var data_type = dtype.charAt(0).toUpperCase() + dtype.slice(1); // capitalise
        var data_id = node.data.obj.id;
        if (data_type in selected_ids) {
            selected_ids[data_type] += ","+data_id;
        } else {
            selected_ids[data_type] = data_id;
        }
    });

    var ids_list = [];
    for (var key in selected_ids){
        ids_list.push(key+"="+selected_ids[key]);
    }
    return ids_list.join("&");
};


// we need to know parent for Channels or Rdefs 'apply to all'
OME.getParentId = function() {
    if (typeof $.jstree === "undefined") {
        return;
    }
    var datatree = $.jstree.reference('#dataTree');

    var selected = datatree.get_selected(true);
    if (selected.length == 1) {
        var node = selected[0];

        if (node.type === 'acquisition') {
            var parentNode = datatree.get_node(datatree.get_parent(node));
            return parentNode.type + '-' + parentNode.data.obj.id;
        } else if (node.type === 'plate') {
            return node.type + '-' + node.data.obj.id;
        } else if  (node.type === 'image') {
            var parentNode = datatree.get_node(datatree.get_parent(node));
            if (parentNode.type === 'dataset') {
                return parentNode.type + '-' + parentNode.data.obj.id;
            }
        }
    }
};


/*
 * Confirm dialog using jquery-ui dialog. http://jqueryui.com/demos/dialog/
 * This code provides a short-cut that doens't need html elements on the page
 * Basic usage (text only - Default buttons are 'OK' and 'Cancel'):
 *    var OK_dialog = OME.confirm_dialog("Can you confirm that you want to proceed?", function() {
 *        var clicked_button_text = OK_dialog.data("clicked_button");
 *        alert(clicked_button_text);
 *    });
 *
 * Also possible to specify title, buttons, width, height:
 *    var btn_labels = ["Yes", "No", "Maybe", "Later"];
 *    var title_dialog = OME.confirm_dialog("Can you confirm that you want to proceed?",
 *          function() { alert( title_dialog.data("clicked_button") },
 *          "Dialog Title", btn_labels, 300, 200);
 */
OME.confirm_dialog = function(dialog_text, callback, title, button_labels, width, height) {

    if ((typeof title == "undefined") || (title === null)) {
        title = "Confirm";
    }
    if ((typeof width == "undefined") || (width === null)) {
        width = 350;
    }
    if ((typeof height == "undefined") || (height === null)) {
        height = 140;
    }

    var $dialog = $("#confirm_dialog");
    if ($dialog.length > 0) {       // get rid of any old dialogs
        $dialog.remove();
    }
    $dialog = $("<div id='confirm_dialog'></div>");
    $('body').append($dialog);

    $dialog.attr("title", title).hide();
    $dialog.html("<p>"+ dialog_text +"</p>");

    if (typeof button_labels == "undefined") {
        button_labels = ['OK', 'Cancel'];
    }
    var btns = {};
    for (var i=0; i<button_labels.length; i++) {
        var b = button_labels[i];
        btns[b] = function(event) {
            var btxt = $(event.target).text();
            $dialog.data("clicked_button", btxt);
            $( this ).dialog( "close" );
        };
    }

    $dialog.dialog({
        resizable: true,
        height: height,
        width: width,
        modal: true,
        buttons: btns
    });
    if (callback) {
        $dialog.bind("dialogclose", callback);
    }

    return $dialog;
};

// short-cut to simply display a message
OME.alert_dialog = function(message) {
    OME.confirm_dialog(message, undefined, "", ["OK"]);
};


/*
 * A dialog for sending feedback.
 * Loads and submits the feedback form at "/feedback/feedback"
 */
OME.feedback_dialog = function(error, feedbackUrl) {

    var $feedback_dialog = $("#feedback_dialog");
    if ($feedback_dialog.length > 0) {       // get rid of any old dialogs
        $feedback_dialog.remove();
    }
    $feedback_dialog = $("<div id='feedback_dialog'></div>");
    $('body').append($feedback_dialog);

    $feedback_dialog.attr("title", "Send Feedback").hide();
    $feedback_dialog.load(feedbackUrl + " #form-500", function() {
        $("textarea[name=error]", $feedback_dialog).val(error);
        $("input[type=submit]", $feedback_dialog).hide();
        $("form", $feedback_dialog).ajaxForm({
            success: function(data) {
                $feedback_dialog.html(data);
                $feedback_dialog.dialog("option", "buttons", {
                    "Close": function() {
                        $( this ).dialog( "close" );
                    }
                });
            }
        });
    });

    $feedback_dialog.dialog({
        resizable: true,
        height: 500,
        width: 700,
        modal: true,
        buttons: {
            "Cancel": function() {
                $( this ).dialog( "close" );
            },
            "Send": function() {
                $("form", $feedback_dialog).submit();
            }
        }
    });
    return $feedback_dialog;
};

/**
 * Handle jQuery load() errors (E.g. timeout)
 * In this case we simply refresh (will redirect to login page)
**/
OME.setupAjaxError = function(feedbackUrl){

    $(document).ajaxError(function(e, req, settings, exception) {

        if (req.status == 404) {
            var msg = "Url: " + settings.url + "<br/>" + req.responseText;
            OME.confirm_dialog(msg, null, "404 Error", ["OK"], 360, 200);
        } else if (req.status == 403) {
            // Denied (E.g. session timeout) Refresh - will redirect to login page
            window.location.reload();
        } else if (req.status == 500) {
            // Our 500 handler returns only the stack-trace if request.is_json()
            var error = req.responseText;
            OME.feedback_dialog(error, feedbackUrl);
        } else if (req.status == 400) {
            // 400 Bad Request. Usually indicates some invalid parameter, e.g. an invalid group id
            // Usually indicates a problem with the webclient rather than the server as the webclient
            // requested something invalid
            var error = req.responseText;
            OME.feedback_dialog(error, feedbackUrl);
        }
    });
};


/*
 * NB: This code is NOT USED currently. Experimental.
 * A dialog for logging-in on the fly (without redirect to login page).
 * On clicking 'Connect' we post username & password to login url and on callback, the callback function is called
 */
OME.login_dialog = function(login_url, callback) {

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
                $.post(login_url, {'password':password, 'username':username, 'noredirect':'true'},  function(data) {
                    //console.log("logged-in...");
                    callback();
                });
                $( this ).dialog( "close" );
            }
        }
    });
    $dialog.bind("dialogclose", callback);

    return $dialog;
};


(function ($) {

    // This jQuery plugin is used to init a right-panel webclient-plugin (too many plugins!)
    // It adds listeners to selection and tab-change events, updating the panel by loading
    // a url based on the currently selected objects.
    // Example usage:
    //
    //  $("#rotation_3d_tab").omeroweb_right_plugin({           // The tab content element
    //      plugin_index: 3,                                    // The tab index
    //      load_plugin_content: function(selected, obj_dtype, obj_id) {    // Url based on selected object(s)
    //          $(this).load('{% url weblabs_index %}rotation_3d_viewer/'+obj_id);
    //      },
    //      supported_obj_types: ['image','dataset'],   // E.g. only support single image/dataset selected
    //  });
    $.fn.omeroweb_right_plugin = function (settings) {

        var returnValue = this;

        // Process each jQuery object in array
        this.each(function(i) {
            // 'this' is the element we're working with
            var $this = $(this);
            var plugin_tab_index = $this.index()-1;

            // store settings
            // 'load_plugin_content' was called 'load_tab_content' (4.4.9 and earlier). Support both...
            var load_plugin_content = settings['load_plugin_content'] || settings['load_tab_content'],
                supported_obj_types = settings['supported_obj_types'],
                // only used if 'supported_obj_types' undefined. (was called 'tab_enabled' in 4.4.9)
                plugin_enabled = settings['plugin_enabled'] || settings['tab_enabled'];

            var update_tab_content = function() {
                // get the selected id etc
                var selected = $("body").data("selected_objects.ome");
                if (selected.length === 0) {
                    return;
                }
                var obj_id = selected[0]['id'];     // E.g. image-123
                var dtype = obj_id.split("-")[0];    // E.g. 'image'
                var oid = obj_id.split("-")[1];

                // if the tab is visible and not loaded yet...
                if ($this.is(":visible") && $this.is(":empty")) {
                    // we want the context of load_plugin_content to be $this
                    $.proxy(load_plugin_content, $this)(selected, dtype, oid);
                }
            };

            // update tabs when tree selection changes or tabs switch
            $("#annotation_tabs").on( "tabsactivate", function(event, ui){
                // we get a "tabsactivate" event, but need to check if 'this' tab was chosen...
                if (ui.newTab.index() == plugin_tab_index) {
                    $this.show();   // sometimes this doesn't get shown until too late
                    update_tab_content();
                }
            });

            // on change of selection in tree, update which tabs are enabled
            $("body").bind("selection_change.ome", function(event) {

                // clear contents of panel
                $this.empty();

                // get selected objects
                var selected = $("body").data("selected_objects.ome");
                if (selected.length === 0) {
                    $("#annotation_tabs").tabs("disable", plugin_tab_index);
                    return;
                }
                var obj_id = selected[0]['id'];     // E.g. image-123
                var orel = obj_id.split("-")[0];    // E.g. 'image'

                // we only care about changing selection if this tab is selected...
                var select_tab = $("#annotation_tabs").tabs( "option", "selected" );
                var supported;
                if (typeof supported_obj_types != 'undefined') {
                    supported = ($.inArray(orel, supported_obj_types) >-1) && (selected.length == 1);
                } else {
                    supported = plugin_enabled ? plugin_enabled(selected) : true;
                }

                // update enabled & selected state
                if(!supported) {
                    if (plugin_tab_index == select_tab) {
                        // if we're currently selected - switch to first tab
                        $("#annotation_tabs").tabs("select", 0);
                    }
                    $("#annotation_tabs").tabs("disable", plugin_tab_index);
                } else {
                    $("#annotation_tabs").tabs("enable", plugin_tab_index);
                    // update tab content
                    update_tab_content();
                }
            });

        });
        // return the jquery selection (or if it was a method call that returned a value - the returned value)
        return returnValue;
    };


    // This plugin is similar to the one above, handling center-panel webclient-plugin init.
    $.fn.omeroweb_center_plugin = function (settings) {

        var returnValue = this;

        // Process each jQuery object in array
        this.each(function(i) {
            // 'this' is the element we're working with
            var $this = $(this),
                plugin_index = $this.index() - 1;

            // store settings
            var load_plugin_content = settings['load_plugin_content'],
                supported_obj_types = settings['supported_obj_types'],
                plugin_enabled = settings['plugin_enabled'],      // only used if 'supported_obj_types' undefined
                empty_on_sel_change = settings['empty_on_sel_change'];
            if (typeof empty_on_sel_change == 'undefined') {
                empty_on_sel_change = true;  // TODO use default settings
            }

            var update_plugin_content = function() {
                // get the selected id etc
                var selected = $("body").data("selected_objects.ome");
                if (selected.length === 0) {
                    return;
                }
                var obj_id = selected[0]['id'];     // E.g. image-123
                var dtype = obj_id.split("-")[0];    // E.g. 'image'
                var oid = obj_id.split("-")[1];

                // if the tab is visible...
                if ($this.is(":visible")) {
                    // we want the context of load_plugin_content to be $this
                    $.proxy(load_plugin_content,$this)(selected, dtype, oid);
                }
            };


            $('#center_panel_chooser').bind('center_plugin_changed.ome', update_plugin_content);

            // on change of selection in tree, update which tabs are enabled
            $("body").bind("selection_change.ome", function(event) {

                // clear contents of panel
                if (empty_on_sel_change) {
                    $this.empty();
                }

                // get selected objects
                var selected = $("body").data("selected_objects.ome");
                if (selected.length === 0) {
                    OME.set_center_plugin_enabled(plugin_index, false);
                    return;
                }
                var obj_id = selected[0]['id'];     // E.g. image-123
                var orel = obj_id.split("-")[0];    // E.g. 'image'

                // do we support the data currently selected?
                var supported;
                if (typeof supported_obj_types != 'undefined') {
                    // simply test E.g. if "image" is in the supported types
                    supported = ($.inArray(orel, supported_obj_types) >-1) && (selected.length == 1);
                } else {
                    // OR use the user-specified function to check support
                    supported = plugin_enabled ? plugin_enabled(selected) : true;
                }

                // update enabled state
                OME.set_center_plugin_enabled(plugin_index, supported);
                if(supported) {
                    update_plugin_content();
                } else {
                    $this.empty();
                }
            });
        });

        // return the jquery selection (or if it was a method call that returned a value - the returned value)
        return returnValue;
    };

}(jQuery));


// jQuery plugin: simple emulation of table-sorter for other elements...
// Based on code from 'Learning jQuery 1.3 http://book.learningjquery.com/'
// Called on an element that resembles a table.
// Example usage:
//$(".element_sorter").elementsorter({
//    head: '.thead div',             // Selector for the equivalent of 'table head'
//    body: 'li.row',                 // Selector for the equivalent of 'table rows'
//    sort_key: '.hidden_sort_text'   // optional - how to find the text within each child of a 'row'.
//});
(function ($) {

    "use strict";

    var methods = {

    // initialise the plugin
    init : function (options) {

        if (!options.head || !options.body) {
            return;
        }

        return this.each(function(){
            var $this = $(this),
                data = $this.data('elementsorter');

            // If the plugin hasn't been initialized yet
            if ( ! data ) {

                data = options;     // save for later ref (E.g. destroy())
                
                var $headers = $(options.head, $this);
                // for each 'column'...
                $headers.each(function(column) {
                    var $header = $(this),
                        findSortKey;
                    var findSortText = function($cell) {
                        if (options.sort_key) {
                            if ($(options.sort_key, $cell).length > 0) {
                                return $(options.sort_key, $cell).text();
                            }
                        }
                        return $cell.text();
                    };
                    if ($header.is('.sort-alpha')) {
                        findSortKey = function($cell) {
                            return findSortText($cell).toUpperCase();
                        };
                    } else if ($header.is('.sort-numeric')) {
                        findSortKey = function($cell) {
                            var key = findSortText($cell).replace(/^[^\d.]*/, '');
                            key = parseFloat(key);
                            return isNaN(key) ? 0 : key;
                        };
                    } else if ($header.is('.sort-date')) {
                        findSortKey = function($cell) {
                            var date = Date.parse(findSortText($cell));
                            return isNaN(date) ? 0 : date;
                        };
                    }
                    if (findSortKey) {
                        $header
                            .addClass('clickable')
                            .click(function() {
                                var sortDirection = 1;
                                if ($header.is('.sorted-asc')) {
                                    sortDirection = -1;
                                }
                                var rows = $(options.body, $this).get();
                                // populate each row with current sort key
                                $.each(rows, function(index, row) {
                                    var $cell = $(row).children().eq(column);
                                    row.sortKey = findSortKey($cell);
                                });
                                // Do the sorting...
                                rows.sort(function(a, b){
                                    if (a.sortKey < b.sortKey) {
                                        return -sortDirection;
                                    }
                                    if (a.sortKey > b.sortKey) {
                                        return sortDirection;
                                    }
                                    return 0;
                                });
                                // add rows to DOM in order
                                $.each(rows, function(index, row) {
                                    $this.append(row);
                                    row.sortKey = null;
                                });
                                // clear classes from other headers
                                $headers.removeClass('sorted-asc')
                                    .removeClass('sorted-desc');
                                if (sortDirection == 1) {
                                    $header.addClass('sorted-asc');
                                } else {
                                    $header.addClass('sorted-desc');
                                }
                            });
                    }
                });

                $this.data('elementsorter', data);
                
            }
        });
    },

    destroy: function() {
        
        return this.each(function(){
            //var $this = $(this),
            //    data = $this.data('elementsorter');

            // all we need to do is remove the click handlers from headers
            // var $headers = $(data.head, $this);
        });
    }

    };


    // the plugin definition: either we init or we're calling a named method.
    $.fn.elementsorter = function( method ) {

        if ( methods[method] ) {
          return methods[method].apply( this, Array.prototype.slice.call( arguments, 1 ));
        }
        if ( typeof method === 'object' || ! method ) {
          return methods.init.apply( this, arguments );
        }
        $.error( 'Method ' +  method + ' does not exist on jQuery.src_loader' );
    };

}(jQuery));

// ** TESTING ONLY **
// http://remysharp.com/2007/11/01/detect-global-variables/
if (false) {                    // set to 'true' to run. NB: Need to uncomment 'console.log..' below.
    setTimeout(function(){      // use timeout to allow all scripts to load etc
        console.log("SHOWING GLOBAL VARIABLES...");
        var differences = {},
            exceptions,
            globals = {},
            //ignoreList = (prompt('Ignore filter (comma sep)?', '') || '').split(','),
            ignoreList = [],    // E.g. ["function"]
            i = ignoreList.length,
            iframe = document.createElement('iframe');
        while (i--) {
          globals[ignoreList[i]] = 1;
        }
        for (i in window) {
          differences[i] = {
            'type': typeof window[i],
            'val': window[i]
          };
        }
        iframe.style.display = 'none';
        document.body.appendChild(iframe);
        iframe.src = 'about:blank';
        iframe = iframe.contentWindow || iframe.contentDocument;
        for (i in differences) {
          if (typeof iframe[i] != 'undefined') {delete differences[i];}
          else if (globals[differences[i].type]) {delete differences[i];}
        }
        exceptions = 'addEventListener,document,location,navigator,window'.split(',');
        exceptions.push("jQuery", "$");  // Ignore jQuery etc...
        exceptions.push("isClientPhone", "callback", "isClientTouch", "isIE");      // from panojs/utils.js
        exceptions.push("sanitizeHexColor", "toRGB", "rgbToHex", "parseQuery", "downloadLandingDialog"); // from ome.gs_utils.js
        // All these from PanoJS
        exceptions.push("PanoJS", "PanoControls", "BisqueISLevel", "BisqueISPyramid", "formatInt");
        exceptions.push("ImgcnvPyramid", "ImgcnvLevel", "InfoControl", "Metadata", "OsdControl", "ROIControl", "ScaleBarControl");
        exceptions.push("Tile", "ZoomifyLevel", "ZoomifyPyramid", "SvgControl", "ThumbnailControl", "trim");
        i = exceptions.length;
        while (--i) {
          delete differences[exceptions[i]];
        }
        console.dir(differences);     // comment out to keep jsHint happy!
    }, 1000);
}
