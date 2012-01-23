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

/* 
 * This code controls the collapse and expansion of the side columns of the 2 or 3 columns
 * layouts in the container base templates.
 */

var hide_left_panel = function() {
    $("#left_panel").hide();
    $("#center_container").css('left', '0px');
    $("#swapTree").children('img').removeClass("expanded-left").addClass("collapsed-left");
}

var show_left_panel = function() {
    $("#left_panel").show();
    var lp_width = parseInt($("#left_panel").css('width'));
    $("#center_container").css('left', + lp_width + 'px');
    $("#swapTree").children('img').removeClass("collapsed-left").addClass("expanded-left");
}

var hide_right_panel = function() {
    $("#right_panel").hide();
    $("#center_container").css('right', '0px');
    $("#swapMeta").children('img').removeClass("collapsed-right").addClass("expanded-right");
}

var show_right_panel = function() {
    $("#right_panel").show();
    var rp_width = parseInt($("#right_panel").css('width'));
    $("#center_container").css('right', rp_width + 'px');
    $("#swapMeta").children('img').removeClass("expanded-right").addClass("collapsed-right");
}

var set_right_panel_width = function(pixels) {
    $("#right_panel").css('width', pixels+'px');
    show_right_panel();
}

$(document).ready(function() 
    {
        $('#swapTree').click(function() { 
            var flag_l = true;
            if($('img#lhid_trayhandle_icon_left').hasClass('collapsed-left')) {
                flag_l = false
            }
            if (flag_l) {
                hide_left_panel();
                flag_l = false;
            } else {
                show_left_panel();
                flag_l = true;
            }
        });

        $('#swapMeta').click(function() { 
            var flag_r = true;
            if($('img#lhid_trayhandle_icon_right').attr('class')=='expanded-right') {
                flag_r = false
            }
            if (flag_r) {                        
                hide_right_panel();
                flag_r = false;                        
            } else {
                show_right_panel();
                flag_r = true; 
            }
        });
        
})