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

$(document).ready(function() 
    {
        $('#swapTree').click(function() { 
            var flag_l = true;
            if($('img#lhid_trayhandle_icon_left').hasClass('collapsed-left')) {
                flag_l = false
            }
            if (flag_l) {
                $("#left_panel").hide();
                $("#swapTree").children('img').removeClass("expanded-left").addClass("collapsed-left");
                flag_l = false;
            } else {
                $("#left_panel").show();
                $("#swapTree").children('img').removeClass("collapsed-left").addClass("expanded-left"); 
                flag_l = true;
            }
        });

        $('#swapMeta').click(function() { 
            var flag_r = true;
            if($('img#lhid_trayhandle_icon_right').attr('class')=='expanded-right') {
                flag_r = false
            }
            if (flag_r) {                        
                $("#right_panel").hide();
                $("#swapMeta").children('img').removeClass("collapsed-right").addClass("expanded-right"); 
                flag_r = false;                        
            } else {
                $("#right_panel").show();
                $("#swapMeta").children('img').removeClass("expanded-right").addClass("collapsed-right"); 
                flag_r = true; 
            }
        });
        
})