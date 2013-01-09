//
// Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
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
    setupAjaxError("{% url fsend %}");      // just in case!

    $('#script_form').ajaxForm({
        success: function(data) {
            window.opener.showActivities();
            self.close();
        }
    });


    // Basically what we're doing here is supplementing the form fields
    // generated from the script itself to make a nier UI.
    // We use these controls to update the paramter fields themselves,
    // as well as updating the Figure preview.

    var $channelNamesMap = $("#channelNamesMap"),
        $figure_table = $("#figure_table"),
        $columnNames = $(".split_column .channel_name", $figure_table),
        $mergedNames = $("div.merged_name", $figure_table),
        $splitIndexes = $("input[name='Split_Indexes']");

    var updateChannelNames = function() {
        // Update Channel-Names map index:Name
        var activeCs = [],
            cNames = [];
        $("#split_channels input[type=checkbox]").each(function(i) {
            var cName = $('input[type=text]', $(this).parent()).val(),
                idx = parseInt( $(this).attr('name'), 10 );
            cNames.push(cName);
            if ($(this).is(":checked")) {
                activeCs.push(idx);
            }
        });
        $('input[name*=Channel_Names_value]', $channelNamesMap).each(function(c){
            $(this).val( cNames[c] );
        });
        // Update Split Indexes
        $splitIndexes.val(activeCs.join(","));
        // Update Figure - channels on/off
        $figure_table.find('tr').each(function(){
            $(this).find('td.split_column').each(function(c) {
                if (activeCs.indexOf(c) > -1) {
                    $(this).show();
                } else {
                    $(this).hide();
                }
            });
        });
        // Update Figure - channel Names
        $columnNames.each(function(i){  // Column names
            $(this).text(cNames[i]);
        });
        $mergedNames.each(function(i){  // Merged names
            $(this).text(cNames[i]);
        });

    };
    // Add / Remove Channels
    $("#split_channels input[type=checkbox]").click(updateChannelNames);
    // Renaming Channels
    $("#split_channels input[type=text]").keyup(updateChannelNames);

    var $mergecColoursMap = $("#mergedColoursMap");
    var updateMergedChannels = function() {
        // Update Merged-Colours map index:colourInt
        $mergecColoursMap.empty();
        var mergedCs = [],
            mergedColurs = [];
        $("#merged_channels input[type=checkbox]:checked").each(function(i){
            var colourInt = $(this).attr('value'),
                idx = parseInt( $(this).attr('name'), 10 ),
                html = "<input type='text' name='Merged_Colours_key"+i+"' value='"+idx+"' />" +
                    "<input type='text' name='Merged_Colours_value"+i+"' value='"+colourInt+"'/>";
            mergedCs.push(idx+1);   // Use 1-based Channel idx for rendering
            $(html).appendTo($mergecColoursMap);
        });
        // Update url of merged panels in Figure
        $(".merged_column img").each(function(){
            // remember original src
            var $this = $(this);
            if (typeof $this.data('src') === 'undefined') {
                $this.data('src', $this.attr('src'));
            }
            var newSrc = $this.data('src') + "?c=" + mergedCs.join(",");
            $this.attr('src', newSrc);
        });
        // Merged Channel names
        $mergedNames.each(function(i) {
            if (mergedCs.indexOf(i+1) > -1) {
                $(this).show();
            } else {
                $(this).hide();
            }
        });

    };
    // When merged channels are toggled
    $("#merged_channels input[type=checkbox]").click(function() {
        updateMergedChannels();
        updateGrey();
    });

    var $splitPanelsGrey = $("input[name=Split_Panels_Grey]");
    var updateGrey = function() {
        // Split Channels grey if 'splitPanelsGrey' OR channel is not merged
        var grey = $splitPanelsGrey.is(":checked"),
            mergedCs = [];
        $("#merged_channels input[type=checkbox]:checked").each(function(i){
            mergedCs.push( parseInt( $(this).attr('name'), 10 ));
        });
        $columnNames.each(function(i){
            var $this = $(this);
            // Note the original color if we haven't done so already
            if (typeof $this.data('color') === 'undefined') {
                $this.data('color', $this.css('color'));
            }
            // Apply color or black to column Names
            if (grey && mergedCs.indexOf(i) > -1) {
                $this.css('color', $this.data('color'));
            } else {
                $this.css('color', 'rgb(0,0,0)');
            }
        });
        // Update url of split panels in Figure
        $(".split_column img").each(function(){
            // remember original src
            var $this = $(this),
                colIdx = parseInt($this.attr("colIdx"), 10),
                chGrey = (grey || (mergedCs.indexOf(colIdx) === -1)),
                flag =  chGrey ? "g" : "c";
            if (typeof $this.data('src') === 'undefined') {
                $this.data('src', $this.attr('src'));
            }
            var newSrc = $this.data('src') + "&m=" + flag;
            $this.attr('src', newSrc);
        });
    };
    // Update grey on checkbox click
    $splitPanelsGrey.click(updateGrey);

    // Merged names
    $("input[name=Merged_Names]").click(function(){
        if ( $(this).is(":checked") ) {
            $("#merged_label").hide();
            $("#merged_names").show();
        } else {
            $("#merged_label").show();
            $("#merged_names").hide();
        }
    });

    $("#zRangeSlider").slider();

    // Lets sync everything to start with:
    updateChannelNames();
    updateMergedChannels();
    updateGrey();

});