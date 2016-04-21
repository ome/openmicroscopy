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


var RatingsPane = function RatingsPane($element, opts) {

    var $header = $element.children('h1'),
        $body = $element.children('div'),
        $rating_annotations = $("#rating_annotations"),
        objects = opts.selected,
        canAnnotate = opts.canAnnotate;

    var tmplText = $('#ratings_template').html();
    var ratingsTempl = _.template(tmplText);


    var initEvents = (function initEvents() {

        $header.click(function(){
            $header.toggleClass('closed');
            $body.slideToggle();

            var expanded = !$header.hasClass('closed');
            OME.setPaneExpanded('ratings', expanded);

            if (expanded && $rating_annotations.is(":empty")) {
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


    this.render = function render() {

        if ($rating_annotations.is(":visible")) {

            if ($rating_annotations.is(":empty")) {
                $rating_annotations.html("Loading ratings...");
            }

            var request = objects.map(function(o){
                return o.replace("-", "=");
            });
            request = request.join("&");

            $.getJSON(WEBCLIENT.URLS.webindex + "api/annotations/?type=rating&" + request, function(data){

                var anns = data.annotations;
                var sum = anns.reduce(function(prev, ann){
                    return prev + ann.longValue;
                }, 0);
                var myRatings = anns.filter(function(ann){
                    return ann.owner.id == WEBCLIENT.USER.id;
                });
                var myRating = myRatings.length > 0 ? myRatings[0].longValue : 0;
                var average = Math.round(sum/anns.length);

                // Update html...
                var html = ratingsTempl({'anns': anns,
                                         'myRating': myRating,
                                         'average': average,
                                         'count': anns.length,
                                         'static': WEBCLIENT.URLS.static_webclient});
                $rating_annotations.html(html);

                // Finish up...
                OME.filterAnnotationsAddedBy();
                $(".tooltip", $rating_annotations).tooltip_init();
            });
        }
    };


    initEvents();

    if (OME.getPaneExpanded('ratings')) {
        $header.toggleClass('closed');
        $body.show();
    }

    this.render();
};