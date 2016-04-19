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
        canAnnotate = opts.canAnnotate;

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

        // Handle events on objects we will load later...
        // $element.on( "click", "tr", function() {
        //     console.log( $( this ).text() );
        // });
    }).bind(this);


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
                var tags = data.annotations.map(function(tag){
                    tag.owner = experimenters[tag.owner.id];
                    if (tag.link && tag.link.owner) {
                        tag.link.owner = experimenters[tag.link.owner.id];
                    }
                    tag.textValue = _.escape(tag.textValue);
                    tag.description = _.escape(tag.description);
                    tag.canRemove = tag.link.permissions.canDelete;
                    return tag;
                });
                console.log(tags);

                // If we are batch annotating multiple objects, we show a summary of each tag
                if (objects.length > 1) {

                    var canUnlinkCount = 0;
                    // Map tag.id to summary for that tag
                    // For each tag (link), need parentClass, parentId, parentName, link owner name
                    var summary = {};
                    tags.forEach(function(tag){
                        var tagId = tag.id;
                        if (summary[tagId] === undefined) {
                            summary[tagId] = {'textValue': tag.textValue,
                                              'id': tag.id,
                                              'canRemove': false,
                                              'canRemoveCount': 0,
                                              'links': []
                                             };
                        }
                        var l = tag.link;
                        if (l.permissions.canDelete) {
                            summary[tagId].canRemoveCount += 1;
                        }
                        summary[tagId].canRemove = summary[tagId].canRemove || l.permissions.canDelete;
                        // slice parent class 'ProjectI' > 'Project'
                        l.parent.class = l.parent.class.slice(0, -1);

                        summary[tagId].links.push(l);
                    });
                    console.log(summary);
                    tags = [];

                    for (var tagId in summary) {
                        if (summary.hasOwnProperty(tagId)) {
                            summary[tagId].links.sort(function(a, b){
                                return a.parent.name > b.parent.name;
                            });
                            tags.push(summary[tagId]);
                        }
                    }
                    console.log(tags);

                }

                // Update html...
                var html = tagTmpl({'tags': tags, 'webindex': WEBCLIENT.URLS.webindex});
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