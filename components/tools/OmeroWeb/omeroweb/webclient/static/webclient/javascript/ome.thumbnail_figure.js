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


    // Make a data structure of image-ids : [tag ids]
    imageTags = {}
    imgIds = []
    $(".img_panel").each(function(){
        var $this = $(this),
            iId = $this.attr('data-iId'),
            tagString = $this.attr('data-tags');
        imageTags[iId] = tagString.split(",");
        imgIds.push(iId);
    });

    $("select[name=Tag_IDs] option").removeAttr('selected');

    var selectedTags = [];
    $("select[name=Tag_IDs]")
        .chosen({placeholder_text:'Choose one or more groups'})
        .change(function(evt, data) {
            if (data.deselected) {
                var toRemove = data.deselected;
                selectedTags.splice(selectedTags.indexOf(toRemove), 1);
            } else if (data.selected) {
                selectedTags.push(data.selected);
            }
            var selectedTagIds = [],
                tagValues = {};
            for (var t=0; t<selectedTags.length; t++) {
                var idTxt = selectedTags[t].split(/-(.+)/);     // split id-text on first '-'
                selectedTagIds.push(idTxt[0]);
                tagValues[idTxt[0]] = idTxt[1];     // map of id:text
            }

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
            var $thumbnail_container = $("#thumbnail_container");
            $('h2', $thumbnail_container).remove();

            // For each Tag combination... (E.g. 'Metaphase'+'Dead')
            for (var r=0; r<results.length; r++) {

                var tagData = results[r],
                    tagsText = tagData.tags.join(" ");     // 'Metaphase Dead'
                if (tagsText.length === 0) {
                    tagsText = "Not Tagged";
                }
                $thumbnail_container.append('<h2>'+ tagsText + '</h2>');

                // Add the images (move from previous position)...
                for (var i=0; i<tagData.imgIds.length; i++) {
                    $('#thumbnail-'+tagData.imgIds[i]).appendTo($thumbnail_container);
                }

            }

    });


    $(".chzn-container").width('350px');

});