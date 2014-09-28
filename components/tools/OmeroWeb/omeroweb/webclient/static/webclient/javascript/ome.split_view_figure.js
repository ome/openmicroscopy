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


    // Basically what we're doing here is supplementing the form fields
    // generated from the script itself to make a nier UI.
    // We use these controls to update the paramter fields themselves,
    // as well as updating the Figure preview.

    $("#enableScalebar").click(function(){
        var enabled = $(this).is(":checked");
        if (enabled) {
            $("input[name=Scalebar]").prop("disabled", false).focus();
        } else {
            $("input[name=Scalebar]").prop("disabled", true);
        }
    });

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

    // Used to make sure we don't have white labels (on white background!)
    var checkNotWhite = function(cssColour) {
        if (cssColour === "rgb(255, 255, 255)") {
            cssColour = "rgb(0,0,0)";
        }
        return cssColour;
    };
    // checkNotWhite for each of the mergedNames
    $mergedNames.each(function(){
        var $mn = $(this);
        $mn.css('color', checkNotWhite($mn.css('color')));
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
                $this.data('color', checkNotWhite($this.css('color')));
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


    // Z selection.
    // NB: We disable Z-projection by clearing the zStart and zEnd fields.
    var $zRangeSlider = $("#zRangeSlider"),
        $zProjectionControls = $("#zProjectionControls"),
        $zStart = $("input[name=Z_Start]"),
        $zEnd = $("input[name=Z_End]"),
        zMin = parseInt($zStart.val(), 10),
        zMax = parseInt($zEnd.val(), 10),
        $algorithm = $("select[name=Algorithm]"),
        $stepping = $("input[name=Stepping]");
    $zRangeSlider.slider({
        range: true,
        min: zMin,
        max: zMax,
        values: [ zMin, zMax ],
        slide: function( event, ui ) {
            $zStart.val(ui.values[ 0 ]);
            $zEnd.val(ui.values[ 1 ]);
        }
    }).slider( "disable" );     // enable if user chooses zProjection
    $("input[name=zProjection]").click(function(){
        if($(this).attr('value') === "z_projection"){
            $zRangeSlider.slider( "enable" );
            $zProjectionControls.show();
            $zStart.prop("disabled", false);
            $zEnd.prop("disabled", false);
            $algorithm.prop("disabled", false);
            $stepping.prop("disabled", false);
        } else {
            $zProjectionControls.hide();
            $zRangeSlider.slider( "disable" );
            $zStart.prop("disabled", true);
            $zEnd.prop("disabled", true);
            $algorithm.prop("disabled", true);
            $stepping.prop("disabled", true);
        }
    });
    // Don't allow users to type values here
    $zStart.keydown(function(){ return false; });
    $zEnd.keydown(function(){ return false; });


    // Image Labels
    var $Image_Labels = $("select[name=Image_Labels]"),
        $rowLabels = $(".rowLabel>div"),
        $imgName = $("div.imgName"),
        $imgTags = $("div.imgTags"),
        $imgDatasets = $("div.imgDatasets");
    $Image_Labels.change(function(){
        var labels = $(this).val();
        $rowLabels.hide(); // hide all then show one...
        if (labels == "Image Name") {
            $imgName.show();
        } else if (labels == "Datasets") {
            $imgDatasets.show();
        } else if (labels == "Tags") {
            $imgTags.show();
        }
        updateColWidths();
    });


    // Drag and Drop to re-order rows
    $('table tbody').sortable({
        disabled: false,
        items: 'tr.figImageData',
        handle: '.rowDragHandle',
        opacity: 0.7,
        axis: 'y',
        forceHelperSize: true,
        update: function( e, ui ) {
            var sortedIds = []
            $(".figImageData").each(function(){
                sortedIds.push($(this).attr('data-imageId') );
            });
            $("input[name=IDs]").val(sortedIds.join(","));
        }
    });
    // set <td> widths, so that while rows are drag-n-dropped, <td> widths stay the same
    var updateColWidths = function() {
        $(".figImageData>td")
            .removeAttr('width')  // clear any previous setting
            .each(function(){
                var $td = $(this);
                $td.attr('width', $td.width());
            });
    }

    // Lets sync everything to start with:
    updateChannelNames();
    updateMergedChannels();
    updateGrey();
    updateColWidths();


    // Bonus feature - Zoom the preview thumbs with slider
    // Make a list of styles (for quick access on zoom)
    var img_panel_styles = [];
    $(".img_panel").each(function(){
        img_panel_styles.push(this.style);
    });
    var setImgSize = function(size) {
        var i, l = img_panel_styles.length;
        for (i=0; i<l; i++) {
            img_panel_styles[i].maxWidth = size + "px";
            img_panel_styles[i].maxHeight = size + "px";
        }
    };
    $("#img_size_slider").slider({
        max: 200,
        min: 30,
        value: 100,
        slide: function(event, ui) {
            setImgSize(ui.value);
        }
    });
});