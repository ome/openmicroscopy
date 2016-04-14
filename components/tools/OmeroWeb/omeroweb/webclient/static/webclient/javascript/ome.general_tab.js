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


(function() {
// We use the "#metadata_general" element to store this data since
// it is not reloaded on selection change.
var setExpanded = function setExpanded(name, expanded) {
    var open_panes = $("#metadata_general").data('open_panes') || [];
    if (expanded && open_panes.indexOf(name) === -1) {
        open_panes.push(name);
    }
    if (!expanded && open_panes.indexOf(name) > -1) {
        open_panes = open_panes.reduce(function(l, item){
            if (item !== name) l.push(item);
            return l;
        }, []);
    }
    $("#metadata_general").data('open_panes', open_panes);
};

var getExpanded = function getExpanded(name) {
    var open_panes = $("#metadata_general").data('open_panes') || [];
    return open_panes.indexOf(name) > -1;
};


window.TagPane = function TagPane($element, opts) {

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
            setExpanded("tags", expanded);

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
                    return tag;
                });
                console.log(tags);

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

    if (getExpanded('tags')) {
        $header.toggleClass('closed');
        $body.show();
    }

    this.render();
};



window.FileAnnsPane = function FileAnnsPane($element, opts) {

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
            setExpanded('files', expanded);

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

    if (getExpanded('files')) {
        $header.toggleClass('closed');
        $body.show();
    }

    this.render();
};



window.MapAnnsPane = function MapAnnsPane($element, opts) {

    var $header = $element.children('h1'),
        $body = $element.children('div'),
        $mapAnnContainer = $("#mapAnnContainer"),
        objects = opts.selected,
        canAnnotate = opts.canAnnotate;

    var tmplText = $('#mapanns_template').html();
    var mapAnnsTempl = _.template(tmplText);


    var initEvents = (function initEvents() {

        $header.click(function(){
            console.log('cic');
            $header.toggleClass('closed');
            $body.slideToggle();

            var expanded = !$header.hasClass('closed');
            setExpanded('maps', expanded);

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


    this.render = function render() {

        if ($mapAnnContainer.is(":visible")) {

            if ($mapAnnContainer.is(":empty")) {
                $mapAnnContainer.html("Loading key value annotations...");
            }

            var request = objects.map(function(o){
                return o.replace("-", "=");
            });

            $.getJSON(WEBCLIENT.URLS.webindex + "api/annotations/?type=map&" + request, function(data){

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
                    return ann;
                });

                // Sort map anns into 3 lists...
                var client_map_annotations = [];
                var my_client_map_annotations = [];
                var map_annotations = [];

                anns.forEach(function(ann){
                    if (isMyClientMapAnn(ann)) {
                        my_client_map_annotations.push(ann);
                    } else if (isClientMapAnn(ann)) {
                        client_map_annotations.push(ann);
                    } else {
                        map_annotations.push(ann);
                    }
                });

                console.log(my_client_map_annotations);
                console.log(client_map_annotations);
                console.log(map_annotations);

                // Update html...
                var html = "";
                if (canAnnotate) {
                    if (my_client_map_annotations.length === 0) {
                        my_client_map_annotations = [{}];   // placeholder
                    }
                    html = mapAnnsTempl({'anns': my_client_map_annotations,
                    'showTableHead': true, 'showNs': false, 'clientMapAnn': true});
                }
                html = html + mapAnnsTempl({'anns': client_map_annotations,
                    'showTableHead': false, 'showNs': false, 'clientMapAnn': true});
                html = html + mapAnnsTempl({'anns': map_annotations,
                    'showTableHead': false, 'showNs': true, 'clientMapAnn': false});
                $mapAnnContainer.html(html);

                // Finish up...
                OME.filterAnnotationsAddedBy();
                $(".tooltip", $mapAnnContainer).tooltip_init();
            });
        }
    };


    initEvents();

    if (getExpanded('maps')) {
        $header.toggleClass('closed');
        $body.show();
    }

    this.render();
};




window.CommentsPane = function CommentsPane($element, opts) {

    var $header = $element.children('h1'),
        $body = $element.children('div'),
        $comments_container = $("#comments_container"),
        objects = opts.selected,
        canAnnotate = opts.canAnnotate;

    var tmplText = $('#comments_template').html();
    var commentsTempl = _.template(tmplText);


    var initEvents = (function initEvents() {

        $header.click(function(){
            $header.toggleClass('closed');
            $body.slideToggle();

            var expanded = !$header.hasClass('closed');
            setExpanded('comments', expanded);

            if (expanded && $comments_container.is(":empty")) {
                this.render();
            }
        }.bind(this));
    }).bind(this);


    this.render = function render() {

        console.log("comments", $comments_container.is(":visible"));

        if ($comments_container.is(":visible")) {

            if ($comments_container.is(":empty")) {
                $comments_container.html("Loading comments...");
            }

            var request = objects.map(function(o){
                return o.replace("-", "=");
            });

            $.getJSON(WEBCLIENT.URLS.webindex + "api/annotations/?type=comment&" + request, function(data){


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
                    ann.textValue = _.escape(ann.textValue);
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
                    html = commentsTempl({'anns': anns,
                                          'static': WEBCLIENT.URLS.static_webclient,
                                          'webindex': WEBCLIENT.URLS.webindex});
                }
                $comments_container.html(html);

                // Finish up...
                OME.filterAnnotationsAddedBy();
                $(".tooltip", $comments_container).tooltip_init();
            });
        }
    };


    initEvents();

    if (getExpanded('comments')) {
        $header.toggleClass('closed');
        $body.show();
    }

    this.render();
};

})();

