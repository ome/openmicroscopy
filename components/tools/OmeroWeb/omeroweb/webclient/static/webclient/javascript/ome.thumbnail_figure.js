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


    $("select[name=Tag_IDs] option").removeAttr('selected');

    var updateNotTagged = function() {
        var show_untagged_images = $("input[name=Show_Untagged_Images]").is(":checked");
        if (show_untagged_images) {
            $(".notTagged").show();
        } else {
            $(".notTagged").hide();
        }
    };
    $("input[name=Show_Untagged_Images]").click(updateNotTagged);


    var updateRowCount = function(){
        var colCount = parseInt($("input[name=Max_Columns]").val(), 10);
        console.log(colCount);
        $(".thumbnail_set td:has(.img_panel)").each(function(){
            console.log(this);
            var $td = $(this);
            $("br", $td).remove();  // remove old <br>
            console.log( $(":nth-child(" + colCount + "n)", $td) );
            $(":nth-child(" + colCount + "n)", $td).after("<br/>");
        });
    };
    $("input[name=Max_Columns]").keyup(updateRowCount);

    updateRowCount();   // initialise layout


    var selectedTagIds = [];
    $("select[name=Tag_IDs]")
        .chosen({placeholder_text:'Choose Tags'})
        .change(function(evt, data) {
            if (data.deselected) {
                var toRemove = data.deselected;
                selectedTagIds.splice(selectedTagIds.indexOf(toRemove), 1);
            } else if (data.selected) {
                selectedTagIds.push(data.selected);
            }
            var tagValues = {};
            // Have to look-up the Tag names from the UI
            $("select[name=Tag_IDs] option:selected").each(function(){
                var $this = $(this);
                tagValues[$this.attr('value')] = $this.text();
            });

            // Now we need to sort images by ids....
            // Let's assign letters to tags in order, so we can use to sort,
            // E.g. 'Metaphase' == 'a', 'Anaphase' == 'b', 'Dead' == 'c'
            // Then we can combine: 'Metaphase'+'Dead' ==> 'ac', 'Anaphase'+'Dead' ==> 'bc'
            // And sort: 'a', 'ab', 'abc', 'b', 'bc', 'c', '' (untagged)
            var letters = "abcdefghijklmnopqrstuvwxyz",
                tagLetters = {},
                letterTags = {};    // backwards map to decode
            for (var t=0; t<selectedTagIds.length; t++){
                tagLetters[selectedTagIds[t]] = letters[t];
                letterTags[letters[t]] = selectedTagIds[t];
            }

            // E.g. 'ac' ==> ['Metaphase', 'Dead']
            var getTagsFromKey = function(key) {
                var rv = [];
                for (var k=0; k<key.length; k++) {
                    var tagId = letterTags[key[k]];
                    rv.push(tagValues[tagId]);
                }
                return rv;
            }

            // For each 'Dataset' (or set of Images)...
            $(".thumbnail_set").each(function() {
                // Make a data structure of image-ids : [tag ids]
                var $this = $(this),
                    imageTags = {},
                    imgIds = [];
                $(".img_panel", $this).each(function(){
                    var $img = $(this),
                        iId = $img.attr('data-iId'),
                        tagString = $img.attr('data-tags');
                    imageTags[iId] = tagString.split(",");
                    imgIds.push(iId);
                });

                // Prepare for sorting...
                sortedImgs = []
                for (var i=0; i<imgIds.length; i++) {
                    // Build up a string based on the image's Tags, that can be used to sort
                    var imgId = imgIds[i],
                        tagIds = imageTags[imgId],
                        letterKeys = [];
                    for (var c=0; c<selectedTagIds.length; c++) {
                        var tid = selectedTagIds[c];
                        if (tagIds.indexOf(tid) > -1) {
                            letterKeys.push(tagLetters[tid]);
                        }
                    }
                    var tagKey = letterKeys.join("");    // E.g. 'a' or 'bc'
                    sortedImgs.push({'id':imgId, 'tagKey':tagKey});
                }
                // Do the Sorting!
                sortedImgs.sort(function(a, b) {
                    var x = a['tagKey'], y = b['tagKey'];
                    if (x.length === 0) return 1;
                    if (y.length === 0) return -1;
                    return ((x < y) ? -1 : ((x > y) ? 1 : 0));
                });

                // Group Images with same tags. E.g. all of the with key 'ac'
                var results = [],        // List of {'tagNames':[], 'imgIds':[]}
                    rkey = "", rIds = [];
                for (var s=0; s<sortedImgs.length; s++) {
                    var sImg = sortedImgs[s];
                    if (sImg.tagKey == rkey) {
                        rIds.push(sImg.id);
                    } else {
                        if (rIds.length > 0) {
                            results.push({'rkey':rkey, 'tags': getTagsFromKey(rkey), 'imgIds':rIds});
                        }
                        rIds = [sImg.id];
                        rkey = sImg.tagKey;
                    }
                }
                results.push({'rkey':rkey, 'tags': getTagsFromKey(rkey), 'imgIds':rIds});


                // Update the UI
                var $toRemove = $('tr:has(.img_panel)', $this);

                // For each Tag combination... (E.g. 'Metaphase'+'Dead')
                var topLevelTag = "";
                var $td, $tr;
                for (var r=0; r<results.length; r++) {

                    var tagData = results[r],
                        tagsText = tagData.tags.join(" ");     // 'Metaphase Dead'
                    if (tagsText.length === 0) {
                        tagsText = "Not Tagged";
                    }
                    if (tagsText.length === 0 || tagData.tags[0] !== topLevelTag) {
                        // start a new container...
                        topLevelTag = tagData.tags[0];

                        $tr = $('<tr><th><h2>' + tagsText + '</h2></th></tr>');
                        $tr.appendTo($this);
                        $td = $('<td></td>').appendTo($tr);
                    } else {
                        $td.append('<div>'+ tagsText.replace(topLevelTag, "") + '</div>');
                    }
                    if (tagsText === "Not Tagged") {
                        $tr.addClass('notTagged');
                    }

                    // Add the images (move from previous position)...
                    for (var i=0; i<tagData.imgIds.length; i++) {
                        $('#thumbnail-'+tagData.imgIds[i]).show().appendTo($td);
                    }

                }
                // now that we've moved the images, we can clean up!
                $toRemove.remove();

                updateNotTagged();
                updateRowCount();
            });

    });

    $(".chzn-container").width('350px');

    // Bonus feature - Zoom the preview thumbs with slider
    // Make a list of styles (for quick access on zoom)
    var img_panel_styles = [];
    $(".img_panel").each(function(){
        img_panel_styles.push(this.style);
    });
    var setImgSize = function(size) {
        console.log(size);
        var i, l = img_panel_styles.length;
        for (i=0; i<l; i++) {
            img_panel_styles[i].maxWidth = size + "px";
            img_panel_styles[i].maxHeight = size + "px";
        }
    };
    $("#img_size_slider").slider({
        max: 96,
        min: 20,
        value: 50,
        slide: function(event, ui) {
            setImgSize(ui.value);
        }
    });

});