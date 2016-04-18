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
        objects = opts.selected,
        canAnnotate = opts.canAnnotate;

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


    var isNotCompanionFile = function isNotCompanionFile(ann) {
        return ann.ns !== OMERO.constants.namespaces.NSCOMPANIONFILE;
    };


    this.render = function render() {

        if ($fileanns_container.is(":visible")) {

            if ($fileanns_container.is(":empty")) {
                $fileanns_container.html("Loading attachments...");
            }

            var request = objects.map(function(o){
                return o.replace("-", "=");
            });

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
                    ann.description = _.escape(ann.description);
                    ann.file.size = ann.file.size.filesizeformat();
                    return ann;
                });
                // Don't show companion files
                anns = anns.filter(isNotCompanionFile);

                console.log(anns);

                // Update html...
                var html = "";
                if (anns.length > 0) {
                    html = filesTempl({'anns': anns, 'webindex': WEBCLIENT.URLS.webindex});
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