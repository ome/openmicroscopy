//   Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
//   All rights reserved.

//   This program is free software: you can redistribute it and/or modify
//   it under the terms of the GNU Affero General Public License as
//   published by the Free Software Foundation, either version 3 of the
//   License, or (at your option) any later version.

//   This program is distributed in the hope that it will be useful,
//   but WITHOUT ANY WARRANTY; without even the implied warranty of
//   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//   GNU Affero General Public License for more details.

//   You should have received a copy of the GNU Affero General Public License
//   along with this program.  If not, see <http://www.gnu.org/licenses/>.


var TagPane = function TagPane($element, opts) {

    var $header = $element.children('h1'),
        $body = $element.children('div'),
        $tags_container = $("#tags_container"),
        objects = opts.selected,
        self = this;

    var tmplText = $('#tags_template').html();
    var tagTmpl = _.template(tmplText);


    var initEvents = (function initEvents() {

        $header.click(function(){
            $header.toggleClass('closed');
            $body.slideToggle();

            var expanded = !$header.hasClass('closed');
            OME.setPaneExpanded("tags", expanded);

            if (expanded && $tags_container.is(":empty")) {
                this.render();
            }
        }.bind(this));
    }).bind(this);


    if ($("#add_tags_form").length === 0) {
        $("<form id='add_tags_form' title='Tags Selection' action='" + WEBCLIENT.URLS.webindex + "annotate_tags/' method='post'>")
            .hide().appendTo('body');
    }

    $("#launch_tags_form").click(function(event) {
        $("#add_tags_form").dialog("open");
        // load form via AJAX...
        var load_url = $(this).attr('href');
        $("#add_tags_form").load(load_url);
        return false;
    });
    // set-up the tags form to use dialog

    $("#add_tags_form").dialog({
        autoOpen: false,
        resizable: false,
        height: 520,
        width: 780,
        modal: true,
        buttons: {
            "Save": function() {
                // simply submit the form (AJAX handling set-up above)
                $("#add_tags_form").trigger('prepare-submit').submit();
                $( this ).dialog( "close" );
            },
            "Cancel": function() {
                $( this ).dialog( "close" );
            },
            "Reset": function() {
                // discard all changes and reload the form
                $("#add_tags_form").html('').load($("#launch_tags_form").attr('href'));
            }
        }
    });
    $('#add_tags_form').ajaxForm({
        beforeSubmit: function(data) {
            $("#tagann_spinner").show();
            // $("#add_tags_form").dialog( "close" );
        },
        success: function(data) {
            // hide in case it was submitted via 'Enter'
            $("#add_tags_form").dialog( "close" );
            // update the list of tags: Re-render tag pane...
            self.render();
            // show_batch_msg("Tags added to Objects");
        },
        error: function(data) {
            $("#tagann_spinner").hide();
        }
    });


    // bind removeItem to various [-] buttons
    $("#tags_container").on("click", ".removeTag", function(event){
        var url = $(this).attr('url'),
            parents = objects.join("|");  // E.g image-123|image-456
        OME.removeItem(event, ".tag_annotation_wrapper", url, parents);
        return false;
    });

    var compareParentName = function(a, b){
        return a.parent.name.toLowerCase() > b.parent.name.toLowerCase() ? 1 : -1;
    };

    this.render = function render() {

        if ($tags_container.is(":visible")) {

            if ($tags_container.is(":empty")) {
                $tags_container.html("Loading tags...");
            }

            var request = objects.map(function(o){
                return o.replace("-", "=");
            });
            request = request.join("&");

            $.getJSON(WEBCLIENT.URLS.webindex + "api/annotations/?type=tag&" + request, function(data){

                // manipulate data...
                // make an object of eid: experimenter
                var experimenters = data.experimenters.reduce(function(prev, exp){
                    prev[exp.id + ""] = exp;
                    return prev;
                }, {});

                // Populate experimenters within tags
                // And do other tag marshalling
                var tags = data.annotations.map(function(tag){
                    tag.owner = experimenters[tag.owner.id];
                    if (tag.link && tag.link.owner) {
                        tag.link.owner = experimenters[tag.link.owner.id];
                    }
                    // AddedBy IDs for filtering
                    tag.addedBy = [tag.link.owner.id];
                    tag.textValue = _.escape(tag.textValue);
                    tag.description = _.escape(tag.description);
                    tag.canRemove = tag.link.permissions.canDelete;
                    return tag;
                });

                // If we are batch annotating multiple objects, we show a summary of each tag
                if (objects.length > 1) {

                    // Map tag.id to summary for that tag
                    var summary = {};
                    tags.forEach(function(tag){
                        var tagId = tag.id,
                            linkOwner = tag.link.owner.id;
                        if (summary[tagId] === undefined) {
                            summary[tagId] = {'textValue': tag.textValue,
                                              'id': tag.id,
                                              'canRemove': false,
                                              'canRemoveCount': 0,
                                              'links': [],
                                              'addedBy': []
                                             };
                        }
                        // Add link to list...
                        var l = tag.link;
                        // slice parent class 'ProjectI' > 'Project'
                        l.parent.class = l.parent.class.slice(0, -1);
                        summary[tagId].links.push(l);

                        // ...and summarise other properties on the tag
                        if (l.permissions.canDelete) {
                            summary[tagId].canRemoveCount += 1;
                        }
                        summary[tagId].canRemove = summary[tagId].canRemove || l.permissions.canDelete;
                        if (summary[tagId].addedBy.indexOf(linkOwner) === -1) {
                            summary[tagId].addedBy.push(linkOwner);
                        }
                    });

                    // convert summary back to list of 'tags'
                    tags = [];
                    for (var tagId in summary) {
                        if (summary.hasOwnProperty(tagId)) {
                            summary[tagId].links.sort(compareParentName);
                            tags.push(summary[tagId]);
                        }
                    }
                }

                // Update html...
                var html = tagTmpl({'tags': tags,
                                    'webindex': WEBCLIENT.URLS.webindex,
                                    'userId': WEBCLIENT.USER.id});
                $tags_container.html(html);

                // Finish up...
                OME.filterAnnotationsAddedBy();
                $(".tooltip", $tags_container).tooltip_init();
            });
            
        }
    };

    initEvents();

    if (OME.getPaneExpanded('tags')) {
        $header.toggleClass('closed');
        $body.show();
    }

    this.render();
};