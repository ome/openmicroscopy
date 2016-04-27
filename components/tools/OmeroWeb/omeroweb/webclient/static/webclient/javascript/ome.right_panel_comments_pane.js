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


var CommentsPane = function CommentsPane($element, opts) {

    var $header = $element.children('h1'),
        $body = $element.children('div'),
        $comments_container = $("#comments_container"),
        objects = opts.selected,
        canAnnotate = opts.canAnnotate;
    var self = this;

    var tmplText = $('#comments_template').html();
    var commentsTempl = _.template(tmplText);


    var initEvents = (function initEvents() {

        $header.click(function(){
            $header.toggleClass('closed');
            $body.slideToggle();

            var expanded = !$header.hasClass('closed');
            OME.setPaneExpanded('comments', expanded);

            if (expanded && $comments_container.is(":empty")) {
                this.render();
            }
        }.bind(this));
    }).bind(this);

    // Comment field - show/hide placeholder and submit button.
    $("#add_comment_wrapper label").inFieldLabels();
    $("#id_comment")
        .blur(function(event){
            setTimeout(function(){
                $("#add_comment_form input[type='submit']").hide();
            }, 200);    // Delay allows clicking on the submit button!
        })
        .focus(function(){
            $("#add_comment_form input[type='submit']").show();
        });

    // bind removeItem to various [-] buttons
    $("#comments_container").on("click", ".removeComment", function(event){
        var url = $(this).attr('url');
        var objId = objects.join("|");
        OME.removeItem(event, ".ann_comment_wrapper", url, objId);
        return false;
    });

    // handle submit of Add Comment form
    $("#add_comment_form").ajaxForm({
        beforeSubmit: function(data) {
            var textArea = $('#add_comment_form textarea');
            if ($.trim(textArea.val()).length === 0) return false;
        },
        success: function(html) {
            $("#id_comment").val("");
            self.render();
        },
    });


    this.render = function render() {

        if ($comments_container.is(":visible")) {

            if ($comments_container.is(":empty")) {
                $comments_container.html("Loading comments...");
            }

            var request = objects.map(function(o){
                return o.replace("-", "=");
            });
            request = request.join("&");

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
                    ann.addedBy = [ann.link.owner.id];
                    ann.textValue = _.escape(ann.textValue);
                    return ann;
                });

                // Show most recent comments at the top
                anns.sort(function(a, b) {
                    return a.date < b.date ? 1 : -1;
                });

                // Remove duplicates (same comment on multiple objects)
                anns = anns.filter(function(ann, idx){
                    // already sorted, so just compare with last item
                    return (idx === 0 || anns[idx - 1].id !== ann.id);
                });

                // Update html...
                var html = "";
                if (anns.length > 0) {
                    html = commentsTempl({'anns': anns,
                                          'static': WEBCLIENT.URLS.static_webclient,
                                          'webindex': WEBCLIENT.URLS.webindex});
                }
                $comments_container.html(html);

                // Finish up...
                OME.linkify_element($( ".commentText" ));
                OME.filterAnnotationsAddedBy();
                $(".tooltip", $comments_container).tooltip_init();
            });
        }
    };


    initEvents();

    if (OME.getPaneExpanded('comments')) {
        $header.toggleClass('closed');
        $body.show();
    }

    this.render();
};