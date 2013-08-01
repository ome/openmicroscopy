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

var tagging_form = function(selected_tags, formset_prefix, tags_field_id) {

    var div_all_tags = $("#id_all_tags");
    var div_selected_tags = $("#id_selected_tags");
    var tag_input = $("#id_tag");
    var tag_input_filter = $("#id_tag_filter");
    var description_input = $("#tag_description");

    var child_tags;
    var all_tags = {};
    var owners = {};

    if ($("#add_tags_progress").length == 0) {
        $("#add_tags_form").next().append(
            "<div id='add_tags_progress' style='display:none;'>" +
            "    <div class='progress-label'></div>" +
            "    <div class='progress-striped animate-stripes'></div>" +
            "    <div class='progress-value'>0%</div>" +
            "</div>"
        );
    }

    // progress bar
    var progressbar = $("#add_tags_progress .progress-striped").progressbar({
            change: function() {
                $("#add_tags_progress .progress-value").text($("#add_tags_progress .progress-striped").progressbar("value") + "%")
            },
            complete: function() {
                setTimeout(function() { $("#add_tags_progress").hide(); }, 2000);
            },
            value: -1
        });
    var progressbar_label = $("#add_tags_progress .progress-label");

    var get_selected_tagset = function() {
        var selected = $(".ui-selected", div_all_tags);
        if (selected.length == 1 && (selected.hasClass('alltags-tagset') || selected.hasClass('alltags-childtag'))) {
            return selected.hasClass('alltags-tagset') ? selected : selected.prevAll('.alltags-tagset').eq(0);
        } else {
            return null;
        }
    };

    var create_html = function() {
        var html = "";
        for (var id in all_tags) {
            var tag = all_tags[id];
            if (tag && !(id in child_tags)) {
                html += create_tag_html(tag.t, tag.d, owners[tag.o], tag.i, null, tag.s);
                tag.sort_key = tag.t.toLowerCase();
                if (tag.s) {
                    for (var sid in tag.s) {
                        var child = all_tags[tag.s[sid]];
                        if (child) {
                            child.sort_key = tag.t.toLowerCase() + child.t.toLowerCase();
                            html += create_tag_html(child.t, child.d, owners[child.o], child.i, tag.i);
                        } else {
                            console.log(sid, tag.s[sid], child);
                        }
                    }
                }
            }
        }
        div_all_tags.append(html);
        $(".tag_selection").selectable({
            filter: "div:not(.filtered)",
            autoRefresh: false,
            stop: function(event, ui) {
                var selected = $(".ui-selected", div_all_tags);
                var count = selected.length;
                $("#id_tags_selected").text(count ? count + " selected" : "");
                var tagset = get_selected_tagset();
                if (tagset) {
                    $("#id_selected_tag_set").text("Add a new tag in " + tagset.text() + " tag set:");
                } else {
                    $("#id_selected_tag_set").text("Add a new tag:");
                }
            },
            start: function(event, ui) {
                // remember which element(s) were selected at start, so we can deselect it if it's clicked again
                $(".was-selected", div_all_tags).removeClass('was-selected');
                $(".ui-selected", div_all_tags).addClass('was-selected');
            },
            selected: function(event, ui) {
                if ($(ui.selected).hasClass('was-selected') && !event.metaKey && !event.ctrlKey) {
                    $(ui.selected).removeClass('ui-selected');
                }
            }
        });
        $("div", div_all_tags).tooltip();
        $("div", div_selected_tags).tooltip();
    };

    var update_html = function() {
        $("#id_all_tags div, #id_selected_tags div").each(function() {
            var tag = all_tags[this.getAttribute("data-id")];
            this.setAttribute("title", create_tag_title(tag.d, owners[tag.o]));
        });
        $("div", div_all_tags).tooltip();
        $("div", div_selected_tags).tooltip();
    };

    var loader = function() {   // wrapper function for all data loading code

        var batch_size = 2000;
        var tag_count;
        var raw_tags = [];
        var raw_desc = {};
        var batch_steps;
        var step_weight;
        var num_tag_callbacks = 0;
        var num_desc_callbacks = 0;

        var load = function(mode, callback, offset, limit) {
            var url = $("#launch_tags_form").attr('href') + "&jsonmode=" + mode;
            if (offset !== undefined && limit !== undefined) {
                url += "&offset=" + offset + "&limit=" + limit;
            }
            $.ajax({ url: url, cache: false, dataType: 'json', success: callback });
        }

        $("#add_tags_progress").show();
        progressbar_label.text("Initializing");
        progressbar.progressbar("value", 0);

        var tag_count_callback = function(data) {
            tag_count = data.tag_count;
            batch_steps = Math.ceil(tag_count / batch_size);
            step_weight = 100 / (2 * batch_steps + 1);
            progressbar_label.text("Loading tags");
            for (var offset = 0; offset < tag_count; offset += batch_size) {
                load('tags', tags_callback, offset, batch_size);
            }
        }

        var tags_callback = function(data) {
            raw_tags = raw_tags.concat(data);
            if (++num_tag_callbacks == batch_steps) {
                process_tags();
                progressbar_label.text("Loading owners");
                load('owners', owners_callback);
            }
            progressbar.progressbar("value", Math.ceil(num_tag_callbacks * step_weight));
        }

        var owners_callback = function(data) {
            process_owners(data);
            progressbar.progressbar("value", Math.ceil((batch_steps + 1) * step_weight));
            progressbar_label.text("Loading descriptions");
            for (var offset = 0; offset < tag_count; offset += batch_size) {
                load('desc', desc_callback, offset, batch_size);
            }
        }

        var desc_callback = function(data) {
            $.extend(raw_desc, data);
            if (++num_desc_callbacks == batch_steps) {
                process_desc();
                progressbar_label.text("Complete");
                progressbar.progressbar("value", 100);
            } else {
                progressbar.progressbar("value", Math.ceil((batch_steps + 1 + num_desc_callbacks) * step_weight));
            }
        }

        var process_tags = function() {
            child_tags = {};
            for (var idx in raw_tags) {
                var c = raw_tags[idx][3];
                if (c) {
                    for (var cidx in c) {
                        child_tags[c[cidx]] = true;
                    }
                }
            }
            var tagstruct = function(tag) {
                return {
                    i: tag[0],
                    t: tag[1],
                    o: tag[2],
                    s: tag[3]
                };
            };
            for (var idx in raw_tags) {
                var tag = tagstruct(raw_tags[idx]);
                all_tags[tag.i] = tag;
            }
            create_html();
            for (var idx in selected_tags) {
                $("div[data-id=" + selected_tags[idx] + "]", div_all_tags).appendTo(div_selected_tags);
            }
            sort_tag_list(div_all_tags);
            sort_tag_list(div_selected_tags);
            update_filter();
        };

        var process_owners = function(data) {
            owners = data;
            update_html();
        };

        var process_desc = function() {
            for (var id in all_tags) {
                all_tags[id].d = raw_desc[id];
            }
            update_html();
        };

        load('tagcount', tag_count_callback);
    }


    var encode_html = function(text) {
        return $('<div/>').text(text).html();
    };

    var create_tag_title = function(description, owner) {
        var title = "";
        if (owner) {
            title += "<b>Owner:</b> " + owner + "<br />";
        }
        if (description) {
            title += "<b>Description:</b> " + description;
        }
        return title;
    };

    var create_tag_html = function(text, description, owner, id, parent_id, is_tagset) {
        var cls = is_tagset ? "alltags-tagset" : (parent_id ? "alltags-childtag" : "alltags-tag");
        var html = "<div class='" + cls + "' data-id='" + id + "'";
        var title = create_tag_title(description, owner);
        if (parent_id) {
            html += " data-set='" + parent_id + "'";
        }
        if (id < 0) { // new tag, save description
            html += " data-description='" + encode_html(description) + "'";
        }
        if (title) {
            html += " title='" + title + "'";
        }
        html += ">" + text + "</div>";
        return html;
    };

    var select_tags = function(event) {
        // clear selections in right box
        $("div.ui-selected", div_selected_tags).removeClass('ui-selected');
        // move individual tags first
        $("div.ui-selected.alltags-tag:not(.filtered), div.ui-selected.alltags-childtag:not(.filtered)", div_all_tags).each(function() {
            $(this).appendTo(div_selected_tags);
        });
        // move whole tag sets
        $("div.ui-selected.alltags-tagset:not(.filtered)", div_all_tags).each(function() {
            var tag = $(this).next("div.alltags-childtag");
            while (tag.length) {
                var current = tag;
                tag = current.next("div.alltags-childtag");
                if (!current.hasClass("filtered")) {
                    current.addClass('ui-selected').appendTo(div_selected_tags);
                }
            }
        });
        sort_tag_list(div_selected_tags);
        update_filter();
        // scroll to first selected tag
        var first_selected = $("div.ui-selected", div_selected_tags);
        if (first_selected.length > 0) {
            div_selected_tags.parent().scrollTop(first_selected.offset().top - div_selected_tags.offset().top - 40);
        }
        event.preventDefault();
    };

    var deselect_tags = function(event) {
        // clear selections in left box
        $("div.ui-selected", div_all_tags).removeClass('ui-selected');
        // move tags back to left box
        $("div.ui-selected", div_selected_tags).each(function() {
            var tagset = $(this).attr("data-set");
            if (tagset) {
                $(this).insertAfter($("div[data-id=" + tagset + "]", div_all_tags));
            } else {
                $(this).appendTo(div_all_tags);
            }
        });
        sort_tag_list(div_all_tags);
        update_filter();
        // scroll to first selected tag
        var first_selected = $("div.ui-selected", div_all_tags);
        if (first_selected.length > 0) {
            div_all_tags.parent().scrollTop(first_selected.offset().top - div_all_tags.offset().top - 40);
        }
        event.preventDefault();
    };

    var update_timeout = null;
    var update_filter = function() {
        clearTimeout(update_timeout);
        var cleanup = function() {
            // make sure tagsets with unfiltered tags are also not filtered
            var unfiltered_tagsets = {};
            $("div.alltags-childtag:not(.filtered)", div_all_tags).each(function() {
                unfiltered_tagsets[this.getAttribute("data-set")] = true;
            });
            $("div.alltags-tagset", div_all_tags).each(function() {
                $(this).toggleClass('filtered', !unfiltered_tagsets[this.getAttribute("data-id")]);
            });
            // hide tag sets that have no unselected tags anymore
            $("div.alltags-tagset + div.alltags-tagset, div.alltags-tagset:last-child", div_all_tags).prev().addClass('empty-tagset');
            $("div.alltags-tagset + div.alltags-childtag", div_all_tags).prev().removeClass('empty-tagset');
        };
        var input = tag_input_filter.val();
        var filters = $.trim(input).toLowerCase().split(/ +/);
        if (filters.length == 1 && filters[0] == "") {
            $("div.filtered", div_all_tags).removeClass('filtered');
            cleanup();
        } else {
            var tags = $("div.alltags-childtag,div.alltags-tag", div_all_tags);
            var dofilter = function(pos) {
                var endpos = Math.min(pos + 1000, tags.length);
                for (var idx = pos; idx < endpos; idx++) {
                    var tag = tags.eq(idx);
                    var match = true;
                    var text = all_tags[tag.attr("data-id")].t.toLowerCase();
                    for (filter in filters) {
                        match = match && text.indexOf(filters[filter]) >= 0;
                    }
                    tag.toggleClass("filtered", !match);
                }
                if (endpos < tags.length) {
                    update_timeout = setTimeout(function() { dofilter(endpos); }, 1);
                } else {
                    cleanup();
                }
            };
            dofilter(0);
        }
    };

    var new_tag_counter = 0;

    var add_new_tag = function() {
        var text = $.trim(tag_input.val());
        var description = $.trim(description_input.val());
        var tagset = get_selected_tagset();
        if (text.length > 0) {
            new_tag_counter -= 1;
            var tagset_id = tagset ? parseInt(tagset.attr('data-id'), 10) : false;
            all_tags[new_tag_counter] = {
                i: new_tag_counter,
                d: description,
                t: text,
                o: null,
                s: tagset_id,
                sort_key: (tagset_id ? all_tags[tagset_id].t.toLowerCase() : '') + text.toLowerCase()
            };
            var div = $(create_tag_html(text, description, null, new_tag_counter, tagset ? tagset.attr('data-id') : null));
            div.addClass('ui-selected').tooltip();
            div_selected_tags.append(div).selectable("refresh");
            tag_input.val('');
            description_input.val('');
        }
        sort_tag_list(div_selected_tags);
        update_filter();
        // scroll to first selected tag
        div_selected_tags.parent().scrollTop($("div.ui-selected", div_selected_tags).offset().top - div_selected_tags.offset().top - 40);
        event.preventDefault();
    };

    var save_tags = function() {
        var existing_tags = [];
        var new_tags = $("#id_" + formset_prefix + "-TOTAL_FORMS");
        var count = 0;
        $('div', div_selected_tags).each(function() {
            var tag_id = this.getAttribute('data-id');
            if (tag_id[0] == "-") { // newly created tag
                new_tags.after($("<input type='hidden' />").attr('name', "newtags-" + count + "-tag").val($(this).text()));
                new_tags.after($("<input type='hidden' />").attr('name', "newtags-" + count + "-description").val(this.getAttribute('data-description')));
                new_tags.after($("<input type='hidden' />").attr('name', "newtags-" + count + "-tagset").val(this.getAttribute('data-set')));
                count += 1;
            } else { // previously existing tag
                existing_tags.push(tag_id);
            }
        });
        new_tags.val(count);
        $("#" + tags_field_id).val(existing_tags.join(','));
    };

    var sort_tag_list = function(list) {
        $("div", list).sort(function(a, b) {
            return (all_tags[a.getAttribute('data-id')].sort_key >
                    all_tags[b.getAttribute('data-id')].sort_key ? 1 : -1);
        }).appendTo(list);
    };

    var update_add_new_button_state = function() {
        if (tag_input.val() != '') {
            $("#id_add_new_tag").removeAttr('disabled');
        } else {
            $("#id_add_new_tag").attr('disabled','disabled');
        }
    };

    $("#id_tag_select_button").click(select_tags);
    $("#id_tag_deselect_button").click(deselect_tags);
    $("#id_add_new_tag").click(add_new_tag);
    $("#add_tags_form").off('prepare-submit').on('prepare-submit', save_tags);
    tag_input.keyup(update_add_new_button_state).change(update_add_new_button_state);
    update_add_new_button_state();
    tag_input_filter.keyup(update_filter).change(update_filter);

    //load_tags();
    loader();
};
