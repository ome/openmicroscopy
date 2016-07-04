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


var FileAnnsPane = function FileAnnsPane($element, opts) {

    var $header = $element.children('h1'),
        $body = $element.children('div'),
        $fileanns_container = $("#fileanns_container"),
        objects = opts.selected;
    var self = this;

    var tmplText = $('#fileanns_template').html();
    var filesTempl = _.template(tmplText);


    var initEvents = (function initEvents() {

        $header.click(function(){
            $header.toggleClass('closed');
            $body.slideToggle();

            var expanded = !$header.hasClass('closed');
            OME.setPaneExpanded('files', expanded);

            if (expanded && $fileanns_container.is(":empty")) {
                this.render();
            }
        }.bind(this));
    }).bind(this);


    // set-up the attachment selection form to use AJAX. (requires jquery.form.js plugin)
    if ($("#choose_attachments_form").length === 0) {
        $("<form id='choose_attachments_form' title='Choose attachments' " +
            "action='" + WEBCLIENT.URLS.webindex + "annotate_file/' method='post'></form>")
            .hide().appendTo('body');
    }
    $('#choose_attachments_form').ajaxForm({
        beforeSubmit: function(data) {
            $("#batch_attachments_form").dialog( "close" );
            $("#fileann_spinner").show();
        },
        success: function() {
            $("#fileann_spinner").hide();
            // update the list of file annotations and bind actions
            self.render();
        },
        error: function(data){
            $("#fileann_spinner").hide();
            alert("Upload failed [" + data.status + " " + data.statusText + "]");
        }
    });
    // prepare dialog for choosing file to attach...
    $("#choose_attachments_form").dialog({
        autoOpen: false,
        resizable: false,
        height: 420,
        width:360,
        modal: true,
        buttons: {
            "Accept": function() {
                // simply submit the form (AJAX handling set-up above)
                $("#choose_attachments_form").submit();
                $( this ).dialog( "close" );
            },
            "Cancel": function() {
                $( this ).dialog( "close" );
            }
        }
    });
    // show dialog for choosing file to attach...
    $("#choose_file_anns").click(function() {
        // show dialog first, then do the AJAX call to load files...
        var $attach_form = $( "#choose_attachments_form" );
        $attach_form.dialog( "open" );
        // load form via AJAX...
        var load_url = $(this).attr('href');
        $attach_form.html("&nbsp<br><img src='" + WEBCLIENT.URLS.static_webgateway + "img/spinner.gif' /> Loading attachments");
        $attach_form.load(load_url);
        return false;
    });


    // Show/hide checkboxes beside files to select files for scripts
    $(".toolbar input[type=button]", $body).click(
        OME.toggleFileAnnotationCheckboxes
    );
    $("#fileanns_container").on(
        "change", "li input[type=checkbox]",
        OME.fileAnnotationCheckboxChanged
    );

    $("#fileanns_container").on("click", ".removeFile", function(event) {
        var url = $(this).attr('href'),
            parents = objects.join("|");  // E.g image-123|image-456
        OME.removeItem(event, ".file_ann_wrapper", url, parents);
        return false;
    });

    // delete action (files)
    $("#fileanns_container").on("click", ".deleteFile", function(event) {
        var url = $(this).attr('href');
        OME.deleteItem(event, "file_ann_wrapper", url);
    });


    var isNotCompanionFile = function isNotCompanionFile(ann) {
        return ann.ns !== OMERO.constants.namespaces.NSCOMPANIONFILE;
    };

    var compareParentName = function(a, b){
        return a.parent.name.toLowerCase() > b.parent.name.toLowerCase() ? 1 : -1;
    };


    this.render = function render() {

        if ($fileanns_container.is(":visible")) {

            if ($fileanns_container.is(":empty")) {
                $fileanns_container.html("Loading attachments...");
            }

            var request = objects.map(function(o){
                return o.replace("-", "=");
            });
            request = request.join("&");

            $.getJSON(WEBCLIENT.URLS.webindex + "api/annotations/?type=file&" + request, function(data){

                var checkboxesAreVisible = $(
                    "#fileanns_container input[type=checkbox]:visible"
                ).length > 0;

                // manipulate data...
                // make an object of eid: experimenter
                var experimenters = data.experimenters.reduce(function(prev, exp){
                    prev[exp.id + ""] = exp;
                    return prev;
                }, {});

                // Populate experimenters within anns
                var anns = data.annotations.map(function(ann){
                    ann.owner = experimenters[ann.owner.id];
                    if (ann.link && ann.link.owner) {
                        ann.link.owner = experimenters[ann.link.owner.id];
                    }
                    // AddedBy IDs for filtering
                    ann.addedBy = [ann.link.owner.id];
                    ann.description = _.escape(ann.description);
                    ann.file.size = ann.file.size.filesizeformat();
                    return ann;
                });
                // Don't show companion files
                anns = anns.filter(isNotCompanionFile);

                
                // If we are batch annotating multiple objects, we show a summary of each tag
                if (objects.length > 1) {

                    // Map tag.id to summary for that tag
                    var summary = {};
                    anns.forEach(function(ann){
                        var annId = ann.id,
                            linkOwner = ann.link.owner.id;
                        if (summary[annId] === undefined) {
                            ann.canRemove = false;
                            ann.canRemoveCount = 0;
                            ann.links = [];
                            ann.addedBy = [];
                            summary[annId] = ann;
                        }
                        // Add link to list...
                        var l = ann.link;
                        // slice parent class 'ProjectI' > 'Project'
                        l.parent.class = l.parent.class.slice(0, -1);
                        summary[annId].links.push(l);

                        // ...and summarise other properties on the ann
                        if (l.permissions.canDelete) {
                            summary[annId].canRemoveCount += 1;
                        }
                        summary[annId].canRemove = summary[annId].canRemove || l.permissions.canDelete;
                        if (summary[annId].addedBy.indexOf(linkOwner) === -1) {
                            summary[annId].addedBy.push(linkOwner);
                        }
                    });

                    // convert summary back to list of 'anns'
                    anns = [];
                    for (var annId in summary) {
                        if (summary.hasOwnProperty(annId)) {
                            summary[annId].links.sort(compareParentName);
                            anns.push(summary[annId]);
                        }
                    }
                }

                // Update html...
                var html = "";
                if (anns.length > 0) {
                    html = filesTempl({'anns': anns,
                                       'webindex': WEBCLIENT.URLS.webindex,
                                       'userId': WEBCLIENT.USER.id});
                }
                $fileanns_container.html(html);

                // Finish up...
                OME.filterAnnotationsAddedBy();
                if (checkboxesAreVisible) {
                    $("#fileanns_container input[type=checkbox]:not(:visible)").toggle();
                }
                $(".tooltip", $fileanns_container).tooltip_init();
            });
            
        }
    };


    initEvents();

    if (OME.getPaneExpanded('files')) {
        $header.toggleClass('closed');
        $body.show();
    }

    this.render();
};