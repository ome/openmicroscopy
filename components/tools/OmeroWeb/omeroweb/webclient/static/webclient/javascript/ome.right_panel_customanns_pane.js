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


var CustomAnnsPane = function CustomAnnsPane($element, opts) {

    var $header = $element.children('h1'),
        $body = $element.children('div'),
        $custom_annotations = $("#custom_annotations"),
        objects = opts.selected;

    var tmplText = $('#customanns_template').html();
    var customannsTempl = _.template(tmplText);


    var initEvents = (function initEvents() {

        $header.click(function(){
            $header.toggleClass('closed');
            $body.slideToggle();

            var expanded = !$header.hasClass('closed');
            OME.setPaneExpanded('others', expanded);

            if (expanded && $custom_annotations.is(":empty")) {
                this.render();
            }
        }.bind(this));
    }).bind(this);


    this.render = function render() {

        if ($custom_annotations.is(":visible")) {

            if ($custom_annotations.is(":empty")) {
                $custom_annotations.html("Loading other annotations...");
            }

            var request = objects.map(function(o){
                return o.replace("-", "=");
            });

            $.getJSON(WEBCLIENT.URLS.webindex + "api/annotations/?type=custom&" + request, function(data){


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

                    // convert 'class' to 'type' E.g. XmlAnnotationI to Xml
                    ann.type = ann.class.replace('AnnotationI', '');
                    var attrs = ['textValue', 'timeValue', 'termValue', 'longValue', 'doubleValue', 'boolValue'];
                    attrs.forEach(function(a){
                        if (ann[a] !== undefined){
                            ann.value = _.escape(ann[a]);
                        }
                    });
                    return ann;
                });

                // Show most recent comments at the top
                anns.sort(function(a, b) {
                    return a.date < b.date;
                });

                console.log(anns);

                // Update html...
                var html = "";
                if (anns.length > 0) {
                    html = customannsTempl({'anns': anns,
                                          'static': WEBCLIENT.URLS.static_webclient,
                                          'webindex': WEBCLIENT.URLS.webindex});
                }
                $custom_annotations.html(html);

                // Finish up...
                OME.filterAnnotationsAddedBy();
                $(".tooltip", $custom_annotations).tooltip_init();
            });
        }
    };


    initEvents();

    if (OME.getPaneExpanded('others')) {
        $header.toggleClass('closed');
        $body.show();
    }

    this.render();
};