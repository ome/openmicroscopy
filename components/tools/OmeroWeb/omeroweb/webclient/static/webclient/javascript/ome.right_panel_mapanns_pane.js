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


var MapAnnsPane = function MapAnnsPane($element, opts) {

    var $header = $element.children('h1'),
        $body = $element.children('div'),
        $mapAnnContainer = $("#mapAnnContainer"),
        objects = opts.selected,
        canAnnotate = opts.canAnnotate;

    var tmplText = $('#mapanns_template').html();
    var mapAnnsTempl = _.template(tmplText);

    var initEvents = (function initEvents() {

        $header.click(function(){
            $header.toggleClass('closed');
            $body.slideToggle();

            var expanded = !$header.hasClass('closed');
            OME.setPaneExpanded('maps', expanded);

            if (expanded && $mapAnnContainer.is(":empty")) {
                this.render();
            }
        }.bind(this));
    }).bind(this);


    var isClientMapAnn = function(ann) {
        return ann.ns === OMERO.constants.metadata.NSCLIENTMAPANNOTATION;
    };
    var isMyClientMapAnn = function(ann) {
        return isClientMapAnn(ann) && ann.owner.id == WEBCLIENT.USER.id;
    };

    this.apiAnnotationUrl = function apiAnnotationUrl(url) {
        if (!url) { return WEBCLIENT.URLS.webindex + "api/annotations/";}
        return url;
    }

    this.render = function render() {

        if ($mapAnnContainer.is(":visible")) {

            if ($mapAnnContainer.is(":empty")) {
                $mapAnnContainer.html("Loading key value annotations...");
            }

            // convert objects to json data
            ajaxdata = {"type": "map"};
            for (var i=0; i < objects.length; i++) {
                var o = objects[i].split(/-(.+)/);
                if (typeof ajaxdata[o[0]] !== 'undefined') {
                    ajaxdata[o[0]].push(o[1]);
                } else {
                    ajaxdata[o[0]] = [o[1]];
                }
            }

            $.ajax({
                dataType: "json",
                url: this.apiAnnotationUrl(opts.url),
                data: ajaxdata,
                traditional: true,
                success: function(data){

                    var experimenters = [];
                    var batchAnn = objects.length > 1;
                    if (data.experimenters.length > 0) {
                        // manipulate data...
                        // make an object of eid: experimenter
                        experimenters = data.experimenters.reduce(function(prev, exp){
                            prev[exp.id + ""] = exp;
                            return prev;
                        }, {});
                    }

                    // Populate experimenters within anns
                    var anns = data.annotations.map(function(ann){
                        if (data.experimenters.length > 0) {
                            ann.owner = experimenters[ann.owner.id];
                        }
                        if (ann.link && ann.link.owner) {
                            ann.link.owner = experimenters[ann.link.owner.id];
                            // AddedBy IDs for filtering
                            ann.addedBy = [ann.link.owner.id]; 
                        }
                        return ann;
                    });

                    // Sort map anns into 3 lists...
                    var client_map_annotations = [];
                    var my_client_map_annotations = [];
                    var map_annotations = [];

                    anns.forEach(function(ann){
                        if (batchAnn) {
                            // Don't allow editing in batch annotate panel
                            // Get error if all rows are deleted then try to add new map
                            ann.permissions.canEdit = false;
                        }
                        if (isMyClientMapAnn(ann)) {
                            my_client_map_annotations.push(ann);
                        } else if (isClientMapAnn(ann)) {
                            client_map_annotations.push(ann);
                        } else {
                            map_annotations.push(ann);
                        }
                    });

                    // Update html...
                    var html = "";
                    var showHead = true;
                    // If not batch_annotate, add placeholder to create map ann
                    if (canAnnotate && !batchAnn) {
                        if (my_client_map_annotations.length === 0) {
                            showHead = false;
                            my_client_map_annotations = [{}];   // placeholder
                        }
                    }
                    // In batch_annotate view, we show which object each map is linked to
                    var showParent = batchAnn;
                    html = mapAnnsTempl({'anns': my_client_map_annotations,
                        'showTableHead': showHead, 'showNs': false, 'clientMapAnn': true, 'showParent': showParent});
                    html = html + mapAnnsTempl({'anns': client_map_annotations,
                        'showTableHead': false, 'showNs': false, 'clientMapAnn': true, 'showParent': showParent});
                    html = html + mapAnnsTempl({'anns': map_annotations,
                        'showTableHead': false, 'showNs': true, 'clientMapAnn': false, 'showParent': showParent});
                    $mapAnnContainer.html(html);

                    // Finish up...
                    OME.linkify_element($( "table.keyValueTable" ));
                    OME.filterAnnotationsAddedBy();
                    $(".tooltip", $mapAnnContainer).tooltip_init();
                }
            });
        }
    };


    initEvents();

    if (OME.getPaneExpanded('maps')) {
        $header.toggleClass('closed');
        $body.show();
    }

    this.render();
};