//
// Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
// All rights reserved.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//

$(document).ready(function() {

    // Use js to show button labels in white if color isDark().
    $(".ch-btn").each(function() {
        var $this = $(this),
            color = $this.css('background-color');
        if (OME.isDark(color)) {
            $this.addClass('fontWhite');
        }
    });


    // Channel buttons update hidden 'Channels' field
    var $channels = $("input[name='Channels']");
    var syncChannels = function() {
        var active = [];
        $(".ch-btn").each(function(i) {
            if ($(this).hasClass('pressed')) {
                active.push(i);
            }
        });
        $channels.val(active.join(","));
    };
    $(".ch-btn").click(function(event){
        event.preventDefault();
        var $this = $(this);
        $this.toggleClass("pressed");
        syncChannels();
        return false;
    });
    syncChannels();


    // Time range sliders
    var $tStart = $("input[name='T_Start']"),
        $tEnd = $("input[name='T_End']"),
        $tStartPlus1 = $("input[name='T_StartPlus1']"),
        $tEndPlus1 = $("input[name='T_EndPlus1']"),
        tStart = parseInt($tStart.val(), 10),
        tEnd = parseInt($tEnd.val(), 10);
    $("#tSlider").slider({
        range: true,
        values: [tStart, tEnd],
        min: tStart,
        max: tEnd,
        slide: function( event, ui ) {
            $tStart.val(ui.values[ 0 ]);
            $tStartPlus1.val(ui.values[ 0 ] + 1);
            $tEnd.val(ui.values[ 1 ]);
            $tEndPlus1.val(ui.values[ 1 ] + 1);
        }
    });
    // Don't allow users to type values here
    $tStart.keydown(function(){ return false; });
    $tEnd.keydown(function(){ return false; });

    // Z-interval range sliders
    var $zStart = $("input[name='Z_Start']"),
        $zEnd = $("input[name='Z_End']"),
        $zStartPlus1 = $("input[name='Z_StartPlus1']"),
        $zEndPlus1 = $("input[name='Z_EndPlus1']"),
        zStart = parseInt($zStart.val(), 10),
        zEnd = parseInt($zEnd.val(), 10);
    $("#zSlider").slider({
        range: true,
        values: [zStart, zEnd],
        min: zStart,
        max: zEnd,
        slide: function( event, ui ) {
            $zStart.val(ui.values[ 0 ]);
            $zStartPlus1.val(ui.values[ 0 ] + 1);
            $zEnd.val(ui.values[ 1 ]);
            $zEndPlus1.val(ui.values[ 1 ] + 1);
        }
    });
    // Don't allow users to type values here
    $zStart.keydown(function(){ return false; });
    $zEnd.keydown(function(){ return false; });


    // If we have a movie, disable Z-range by default...
    if (tEnd > 1) {
        $("#zSlider").slider( "disable" );
        $zStartPlus1.prop('disabled', true);
        $zEndPlus1.prop('disabled', true);
        $("#zRangeControls .sliderToggle").prop('checked', false);
    } else {
        // or disable T-range if not a movie
        $("#tSlider").slider( "disable" );
        $tStartPlus1.prop('disabled', true);
        $tEndPlus1.prop('disabled', true);
        $("#tRangeControls .sliderToggle").prop('checked', false);
    }
    // Z/T range can be enabled / disabled if we have a choice
    if (tEnd > 1 && zEnd > 1) {
        $(".sliderToggle").click(function(){
            var $this = $(this),
                enabled = $this.prop('checked'),
                $controls = $this.parent();
            $controls.find('.number').prop('disabled', !enabled);
            $controls.find('.ui-slider').slider( "option", "disabled", !enabled );
        });
    } else {
        $(".sliderToggle").prop('disabled', true);
    }

    // Scalebar - can be disabled
    $("#scalebarToggle").click(function(){
        var enabled = $(this).prop('checked');
        $("input[name='Scalebar']").prop('disabled', !enabled);
    });


    // Keep 'Show Labels' checkboxes in sync
    $("input[name='Show_Plane_Info']").click(function(){
        var chkd = $(this).prop('checked');
        $("input[name='Show_Time']").prop('checked', chkd);
    });

});
