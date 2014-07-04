/*
 * Copyright (c) 2008-2012 University of Dundee. & Open Microscopy Environment.
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

/*
 * This code controls the collapse and expansion of the side columns of the 2 or 3 columns
 * layouts in the container base templates.
 */

/*global OME:true */
if (typeof OME === "undefined") {
    OME = {};
}
OME.nav = {};
OME.nav.hide_left_panel = function() {
    var lp = $("#left_panel");
    var lp_width = parseInt(lp.css('width'), 10);
    lp.css('width', '0px').data('expand_width', lp_width); // remember width to expand to
    $("#center_container").css('left', '0px');
    $("#swapTree").children('img').removeClass("expanded-left").addClass("collapsed-left");
};

OME.nav.show_left_panel = function() {
    var lp = $("#left_panel");
    var lp_width = lp.data('expand_width');
    lp.css('width', lp_width+'px');
    $("#center_container").css('left', + lp_width + 'px');    // 1px for border
    $("#swapTree").children('img').removeClass("collapsed-left").addClass("expanded-left");
};

OME.nav.hide_right_panel = function() {
    var rp = $("#right_panel");
    var rp_width = parseInt(rp.css('width'));
    rp.css('width', '0px').data('expand_width', rp_width); // remember width to expand to
    $("#center_container").css('right', '0px');
    $("#swapMeta").children('img').removeClass("collapsed-right").addClass("expanded-right");
}

OME.nav.show_right_panel = function() {
    var rp = $("#right_panel");
    var rp_width = rp.data('expand_width');
    rp.css('width', rp_width+'px');
    $("#center_container").css('right', + rp_width + 'px');
    $("#swapMeta").children('img').removeClass("expanded-right").addClass("collapsed-right");
}

OME.nav.set_right_panel_width = function(pixels) {
    $("#right_panel").css('width', pixels+'px');
    OME.nav.show_right_panel();
}

$(document).ready(function() 
    {
        $('#swapTree').click(function() { 
            var flag_l = true;
            if($('img#lhid_trayhandle_icon_left').hasClass('collapsed-left')) {
                flag_l = false
            }
            if (flag_l) {
                OME.nav.hide_left_panel();
                flag_l = false;
            } else {
                OME.nav.show_left_panel();
                flag_l = true;
            }
        });

        $('#swapMeta').click(function() { 
            var flag_r = true;
            if($('img#lhid_trayhandle_icon_right').attr('class')=='expanded-right') {
                flag_r = false
            }
            if (flag_r) {                        
                OME.nav.hide_right_panel();
                flag_r = false;                        
            } else {
                OME.nav.show_right_panel();
                flag_r = true; 
            }
        });
        
        /*
         * Drag functionality, to allow resizing of the left and right panels. 
         * The draggable element is the blank gif image. It's position is absolute wrt it's container.
         * The container also moves when the panels are resized, so the draggable element moves twice as 
         * fast as the mouse pointer (blank gif moves outsize container). And has to be put back on dragstop.
         */
        // Left
        $("#dragLeft").draggable({ axis: "x" })
        .bind("dragstart", function(event, ui) {
            // note the starting position and original width
            $(this).data("drag_start_x", event.pageX);
            var lp_width = $("#left_panel").width();
            $(this).data("lp_width", lp_width);
            $(this).data("cp_width", $("#center_panel").width());
            $("#swapTree").children('img').removeClass("collapsed-left").addClass("expanded-left");     // show 'expanded'
        })
        .bind("drag", function(event, ui) {
            var moved = event.pageX - $(this).data("drag_start_x");
            var new_width = $(this).data("lp_width") + moved;
            var new_center_w = $(this).data("cp_width") - moved;
            if ((moved < $(this).data("cp_width")) && (new_width > 50) && (new_center_w > 50)) {
                $("#left_panel").css('width', new_width+"px");
                $("#center_container").css('left', new_width+"px");
            }
        })
        .bind("dragstop", function(event, ui) {
            $(this).css("left", "0px");
            $("#left_panel").trigger('resize');
        });
        
        // Right
        $("#dragRight").draggable({ axis: "x" })
        .bind("dragstart", function(event, ui) {
            // note the starting position and original width
            $(this).data("drag_start_x", event.pageX);
            var rp_width = $("#right_panel").width();
            $(this).data("rp_width", rp_width);
            $(this).data("cp_width", $("#center_panel").width());
            $("#swapMeta").children('img').removeClass("expanded-right").addClass("collapsed-right");   // show 'expanded'
        })
        .bind("drag", function(event, ui) {
            var moved = event.pageX - $(this).data("drag_start_x");
            var new_width = $(this).data("rp_width") - moved;
            var new_center_w = $(this).data("cp_width") + moved;
            if (((moved) < $(this).data("cp_width")) && (new_width > 50) && (new_center_w > 50)) {
                $("#right_panel").css('width', new_width+"px");
                $("#center_container").css('right', new_width +"px");
            }
        })
        .bind("dragstop", function(event, ui) {
            $(this).css("left", "0px");
            $("#right_panel").trigger('resize');
        });
        
})