/*jshint browser: true, jquery: true, curly: true, maxlen: 80,
  eqeqeq: true, immed: true, indent: 4, latedef: true,
  newcap: true, noarg: true, noempty: true,
  nonew: true, undef: true, unused: true, trailing: true */
/*global $, setTimeout, clearTimeout, OME, WEBCLIENT */
/*exported tagging_form */
//
// Copyright (C) 2013-2016 University of Dundee & Open Microscopy Environment.
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

var tagging_form = function(
    selected_tags, formset_prefix, tags_field_id, me, my_name
    ) {

    var div_all_tags = $("#id_all_tags");
    var div_selected_tags = $("#id_selected_tags");
    var tag_input = $("#id_tag");
    var tag_input_filter = $("#id_tag_filter");
    var description_input = $("#tag_description");

    var child_tags;
    var all_tags = {};
    var owners = {};

    var canceled = false;
    var loaded = false;

    if ($("#add_tags_progress").length === 0) {
        $("#add_tags_form").next().append(
            "<div id='add_tags_progress' style='display:none;'>" +
            "    <div class='progress-label'></div>" +
            "    <div class='progress-striped animate-stripes'></div>" +
            "    <div class='progress-value'>0%</div>" +
            "</div>");
    }

    // progress bar
    var progressbar = $("#add_tags_progress .progress-striped").progressbar({
            change: function() {
                $("#add_tags_progress .progress-value").text(
                    $("#add_tags_progress .progress-striped")
                    .progressbar("value") + "%");
            },
            complete: function() {
                setTimeout(
                    function() { $("#add_tags_progress").remove(); }, 2000);
            },
            value: -1
        });
    var progressbar_label = $("#add_tags_progress .progress-label");
    $("#add_tags_form").on("dialogclose", function() {
        $("#add_tags_progress").remove();
        progressbar_label = progressbar = $();
        canceled = true;
    });

    var get_selected_tagset = function() {
        var selected = $(".ui-selected", div_all_tags).not(".filtered");
        if (selected.length === 1 && (
            selected.hasClass('alltags-tagset') ||
            selected.hasClass('alltags-childtag'))) {
            return (selected.hasClass('alltags-tagset') ?
                selected : selected.prevAll('.alltags-tagset').eq(0));
        } else {
            return null;
        }
    };

    var tag_click = function(event) {
        $(this).toggleClass(
            'ui-selected').siblings('.ui-selected').removeClass('ui-selected');
        update_selected_labels();
        event.stopPropagation();
    };

    $(".tag_selection_wrapper").on('click', function() {
        // when clicking in the blank space of the list, deselect all
        $(".ui-selected", $(this)).removeClass('ui-selected');
        update_selected_labels();
    });

    var create_html = function() {
        var html = "";
        for (var id in all_tags) {
            var tag = all_tags[id];
            if (tag && !(id in child_tags)) {
                html += create_tag_html(tag.t, tag.d, owners[tag.o], tag.i,
                                        null, tag.s !== 0);
                tag.sort_key = tag.t.toLowerCase();
                if (tag.s) {
                    for (var sid in tag.s) {
                        var child = all_tags[tag.s[sid]];
                        if (child) {
                            child.sort_key = (tag.t.toLowerCase() +
                                child.t.toLowerCase());
                            html += create_tag_html(
                                child.t, child.d, owners[child.o], child.i,
                                tag.i);
                        }
                    }
                }
            }
        }
        div_all_tags.append(html);
        // TODO This tooltip application is used until the extra data has loaded
        // at which point the tooltips are updated and this handler is replaced?
        $(".tag_selection div").on('click', tag_click).tooltip({
            track: false,
            show: false,
            hide: false,
            items: '[data-content]',
            content: function() {
                return $(this).data('content');
            }
        });
    };

    var update_selected_labels = function() {
        var count = 0;
        $(".ui-selected", div_all_tags).not(".filtered").each(function() {
            var $this = $(this);
            if ($this.hasClass('alltags-tagset')) {
                count += $this.nextUntil(":not(.alltags-childtag)").not(
                    ".filtered, .ui-selected").length;
            } else {
                count++;
            }
        });
        $("#id_tags_selected").text(count ? count + " selected" : "");
        var tagset = get_selected_tagset();
        if (tagset) {
            $("#id_selected_tag_set").html(
                "Add a new tag in <span class='tagset-title'>" +
                tagset.text() + "</span> tag set and select it immediately:");
        } else {
            $("#id_selected_tag_set").text(
                "Add a new tag and select it immediately:");
        }
    };

    var update_tooltip = function() {
        var $this = $(this);
        var tag = all_tags[this.getAttribute("data-id")];
        var parent_id = all_tags[this.getAttribute("data-set")];
        var link_owner = null;
        if ($this.parent().attr("id") === "id_selected_tags") {
            link_owner = this.getAttribute("data-linkownername");
            if (link_owner && $this.hasClass('owner-tagged')) {
                link_owner = 'you and ' + link_owner;
            }
        }
        var title = create_tag_title(tag.d, owners[tag.o],
            parent_id ? parent_id.t : null, link_owner);
        $this.tooltip({
            track: false,
            show: false,
            hide: false,
            content: title
        });
    };

    var update_html_list = function(list) {
        $("div", list).each(update_tooltip);
    };

    var update_html = function() {
        update_html_list($("#id_all_tags"));
        update_html_list($("#id_selected_tags"));
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
            var url = WEBCLIENT.URLS.webindex + "marshal_tagging_form_data/";
            url = url + "?jsonmode=" + mode +
                        "&group=" + WEBCLIENT.active_group_id;
            if (offset !== undefined && limit !== undefined) {
                url += "&offset=" + offset + "&limit=" + limit;
            }
            $.ajax({
                url: url,
                dataType: 'json',
                success: callback
            });
        };

        $(":button:contains('Reset'),:button:contains('Save')",
            $("#add_tags_form").parent()).prop("disabled", true).addClass(
            'ui-state-disabled');

        progressbar_label.text("Initializing");
        progressbar.progressbar("value", 0);
        $("#add_tags_progress").show();

        var tag_count_callback = function(data) {
            if (canceled) {
                return;
            }
            tag_count = data.tag_count;
            if (tag_count > 0) {
                batch_steps = Math.ceil(tag_count / batch_size);
                step_weight = 100 / (2 * batch_steps + 1);
                progressbar_label.text("Loading tags");
                for (var offset = 0; offset < tag_count; offset += batch_size) {
                    load('tags', tags_callback, offset, batch_size);
                }
            } else {
                progressbar_label.text("Complete");
                progressbar.progressbar("value", 100);
                finalize_load();
            }
        };

        var tags_callback = function(data) {
            if (canceled) {
                return;
            }
            raw_tags = raw_tags.concat(data);
            /*jsl:ignore*/
            if (++num_tag_callbacks === batch_steps) {
            /*jsl:end*/
                process_tags();
                progressbar_label.text("Loading owners");
                load('owners', owners_callback);
            }
            progressbar.progressbar("value", Math.ceil(
                num_tag_callbacks * step_weight));
        };

        var owners_callback = function(data) {
            if (canceled) {
                return;
            }
            process_owners(data);
            progressbar.progressbar("value", Math.ceil((batch_steps + 1) *
                                                       step_weight));
            progressbar_label.text("Loading descriptions");
            for (var offset = 0; offset < tag_count; offset += batch_size) {
                load('desc', desc_callback, offset, batch_size);
            }
        };

        var desc_callback = function(data) {
            if (canceled) {
                return;
            }
            $.extend(raw_desc, data);
            /*jsl:ignore*/
            if (++num_desc_callbacks === batch_steps) {
            /*jsl:end*/
                process_desc();
                progressbar_label.text("Complete");
                progressbar.progressbar("value", 100);
            } else {
                progressbar.progressbar("value", Math.ceil(
                    (batch_steps + 1 + num_desc_callbacks) * step_weight));
            }
        };

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
            for (idx in raw_tags) {
                var tag = tagstruct(raw_tags[idx]);
                all_tags[tag.i] = tag;
            }
            create_html();

            var others_tags = []; // track ids of tags linked by other users
            for (idx in selected_tags) {
                if (!selected_tags[idx][5]) {
                    others_tags.push(selected_tags[idx][0]);
                    // link(s) not owned by current user, don't add to list
                    continue;
                }
                var selected_tag = $(".tag_selection div[data-id=" +
                                     selected_tags[idx][0] + "]");
                if (selected_tag.length) { // not yet selected
                    selected_tag.appendTo(div_selected_tags);
                    selected_tag.addClass('owner-tagged');
                }
            }

            // add others_count to selected tags
            for (idx in others_tags) {
                var tag_to_update = $(".tag_selection div[data-id=" +
                                      others_tags[idx] + "]");
                if (tag_to_update.length) {
                    var others_count = parseInt(
                        tag_to_update.attr("data-others_count") || "0", 10
                        ) + 1;
                    tag_to_update.attr("data-others_count", others_count);
                    tag_to_update.attr("data-linkownername", others_count ?
                        others_count + " other user" +
                        (others_count > 1 ? "s" : "") : selected_tags[idx][2]);
                }
            }

            update_html_list($("#id_selected_tags"));
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
                all_tags[id].d = raw_desc[id] || "";
            }
            update_html();
            finalize_load();
        };

        var finalize_load = function() {
            loaded = true;
            update_add_new_button_state();
        };

        load('tagcount', tag_count_callback);
    };

    var enable_buttons = function() {
        if (loaded) {
            $(":button:contains('Reset'),:button:contains('Save')",
              $("#add_tags_form").parent()
              ).prop("disabled", false).removeClass('ui-state-disabled');
        }
    };

    var encode_html = function(text) {
        return $('<div/>').text(text).html();
    };

    var create_tag_title = function(description, owner, tagset, link_owner) {
        var title = "";
        if (owner) {
            title += "<b>Owner:</b> " + owner + "<br />";
        }
        if (link_owner) {
            title += "<b>Linked by:</b> " + link_owner + "<br />";
        }
        if (description) {
            title += "<b>Description:</b> " + description + "<br />";
        }
        if (tagset) {
            title += "<b>Tag set:</b> " + tagset + "<br />";
        }
        return title;
    };

    var create_tag_html = function(text, description, owner, id, parent_id,
                                   is_tagset) {
        var cls = is_tagset ? "alltags-tagset" :
            (parent_id ? "alltags-childtag" : "alltags-tag");
        var html = "<div class='" + cls + "' data-id='" + id + "'";
        var tagset;
        if (parent_id) {
            html += " data-set='" + parent_id + "'";
            tagset = all_tags[parent_id].t;
        }
        if (id < 0) { // new tag, save description
            html += " data-description='" + encode_html(description) + "'";
        }
        var title = create_tag_title(description, owner, tagset);
        // Add content even if it is empty
        // and handle case for no tooltip in the tooltip code
        html += " data-content='" + title.replace(/'/g, "&#39;") + "'";
        html += ">" + encode_html(text) + "</div>";
        return html;
    };

    var select_tags = function(event) {
        // clear selections in right box
        $("div.ui-selected", div_selected_tags).removeClass('ui-selected');
        // move individual tags first
        $("div.ui-selected.alltags-tag:not(.filtered), " +
          "div.ui-selected.alltags-childtag:not(.filtered)", div_all_tags).each(
            function() {
                $(this).appendTo(div_selected_tags);
            }).each(update_tooltip);
        // move whole tag sets
        $("div.ui-selected.alltags-tagset:not(.filtered)", div_all_tags).each(
            function() {
                var tag = $(this).next("div.alltags-childtag");
                while (tag.length) {
                    var current = tag;
                    tag = current.next("div.alltags-childtag");
                    if (!current.hasClass("filtered")) {
                        current.addClass(
                            'ui-selected').appendTo(div_selected_tags).each(
                                update_tooltip);
                    }
                }
            });
        sort_tag_list(div_selected_tags);
        update_filter();
        // scroll to first selected tag
        var first_selected = $("div.ui-selected", div_selected_tags);
        if (first_selected.length > 0) {
            div_selected_tags.parent().scrollTop(
                first_selected.offset().top - div_selected_tags.offset().top -
                40);
        }
        enable_buttons();
        event.preventDefault();
    };

    tag_input_filter.keypress(function(event) {
        if (event.which === 13) {
            select_tags(event);
            tag_input_filter.val('');
        }
    });

    var deselect_tags = function(event) {
        // clear selections in left box
        $("div.ui-selected", div_all_tags).removeClass('ui-selected');
        // move tags back to left box
        $("div.ui-selected", div_selected_tags).each(function() {
            var tagset = $(this).attr("data-set");
            if (tagset) {
                $(this).insertAfter($("div[data-id=" + tagset + "]",
                                      div_all_tags));
            } else {
                $(this).appendTo(div_all_tags);
            }
        }).each(update_tooltip);
        sort_tag_list(div_all_tags);
        update_filter(undefined, true);
        // scroll to first selected tag
        var first_selected = $("div.ui-selected", div_all_tags);
        if (first_selected.length > 0) {
            div_all_tags.parent().scrollTop(first_selected.offset().top -
                                            div_all_tags.offset().top - 40);
        }
        enable_buttons();
        event.preventDefault();
    };

    var update_timeout = null;
    var update_filter = function(event, keep_selection) {
        clearTimeout(update_timeout);
        var filters;
        var cleanup = function() {
            // make sure tagsets with unfiltered tags are also not filtered
            if (!no_filter) {
                var unfiltered_tagsets = {};
                $("div.alltags-childtag:not(.filtered)", div_all_tags).each(
                    function() {
                        unfiltered_tagsets[
                            this.getAttribute("data-set")] = true;
                    });
                $("div.alltags-tagset", div_all_tags).each(function() {
                    $(this).toggleClass('filtered', !unfiltered_tagsets[
                        this.getAttribute("data-id")]);
                });
            }
            if (!keep_selection) {
                $("div.ui-selected", div_all_tags).removeClass("ui-selected");
                if (filters !== '') {
                    $("div.alltags-tag,div.alltags-childtag", div_all_tags).not(
                        ".filtered").first().addClass("ui-selected");
                }
            }
            update_selected_labels();
        };
        var input = tag_input_filter.val();
        if (input === tag_input_filter.attr('placeholder')) {
            input = '';
        }
        var owner_mode = $(
            "select[name=filter_owner_mode] option:selected").val();
        filters = $.trim(input).toLowerCase();
        var filters_split = filters.split(/ +/);
        var no_filter = filters === "" && owner_mode === "any";
        if (no_filter) {
            $("div.filtered", div_all_tags).removeClass('filtered');
            cleanup();
        } else {
            var mode = $("select[name=filter_mode] option:selected").val();
            var tags = $("div.alltags-childtag,div.alltags-tag", div_all_tags);
            var dofilter = function(pos) {
                var endpos = Math.min(pos + 1000, tags.length);
                for (var idx = pos; idx < endpos; idx++) {
                    var tag = tags.eq(idx);
                    var tagobj = all_tags[tag.attr("data-id")];
                    var match = true;
                    var text = $.trim(tagobj.t.toLowerCase());
                    if (mode === "any") {
                        for (var filter in filters_split) {
                            match = match && text.indexOf(
                                filters_split[filter]) >= 0;
                        }
                    } else {
                        match = (text.substr(0, filters.length) === filters);
                    }
                    if (match && owner_mode !== "all") {
                        match = ((owner_mode === "me" && tagobj.o === me) ||
                                 (owner_mode === "others" && tagobj.o !== me));
                    }
                    tag.toggleClass("filtered", !match);
                }
                if (endpos < tags.length) {
                    update_timeout = setTimeout(function() {
                        dofilter(endpos);
                    }, 1);
                } else {
                    cleanup();
                }
            };
            dofilter(0);
        }
    };

    var new_tag_counter = 0;

    var add_new_tag = function(event, force) {
        event.preventDefault();

        var text = tag_input.val();
        if (text === tag_input.attr('placeholder')) {
            text = '';
        }
        text = $.trim(text);
        var description = description_input.val();
        if (description === description_input.attr('placeholder')) {
            description = '';
        }
        description = $.trim(description);

        var tagset = get_selected_tagset();
        if (text.length > 0) {

            var select_dialog;
            var confirm_tag_selection = function() {
                if (select_dialog.data("clicked_button") === "Yes") {
                    $("div.ui-selected", div_all_tags).removeClass(
                        "ui-selected");
                    $("[data-id=" + id + "]", div_all_tags).addClass(
                        "ui-selected").removeClass("filtered");
                    select_tags(event);
                    tag_input.val('');
                    description_input.val('');
                    enable_buttons();
                }
            };
            var confirm_tag_creation = function() {
                if (select_dialog.data("clicked_button") === "Yes") {
                    add_new_tag(event, true);
                    enable_buttons();
                }
            };

            // check for tag with same name
            if (!force) {
                var lowertext = text.toLowerCase();
                var lowerdesc = description.toLowerCase();
                for (var id in all_tags) {
                    if (all_tags[id].t.toLowerCase() === lowertext) {
                        if (all_tags[id].d.toLowerCase() === lowerdesc) {
                            if ($("[data-id=" + id + "]",
                                  div_selected_tags).length > 0) {
                                OME.alert_dialog(
                                    "A tag with the same name and description" +
                                    " already exists and is selected.");
                            } else if (all_tags[id].s !== 0) {
                                OME.alert_dialog(
                                    "A tag set with the same name and " +
                                    "description already exists.");
                            } else {
                                select_dialog = OME.confirm_dialog(
                                    "A tag with the same name and description" +
                                    " already exists. Would you like to " +
                                    "select the existing tag?",
                                    confirm_tag_selection, "Add new tag",
                                    ["Yes", "No"]);
                            }
                        } else {
                            select_dialog = OME.confirm_dialog(
                                "A tag with the same name and a different " +
                                "description already exists. " +
                                "Would you still like to add a new tag?",
                                confirm_tag_creation, "Add new tag",
                                ["Yes", "No"]);
                        }
                        return;
                    }
                }
            }

            new_tag_counter -= 1;
            var tagset_id = (tagset ? parseInt(tagset.attr('data-id'), 10) :
                             false);
            owners[me] = owners[me] || my_name;
            all_tags[new_tag_counter] = {
                i: new_tag_counter,
                d: description,
                t: text,
                o: me,
                s: tagset_id,
                sort_key: (tagset_id ? all_tags[tagset_id].t.toLowerCase() :
                           '') + text.toLowerCase()
            };
            var div = $(create_tag_html(
                text, description, my_name, new_tag_counter,
                tagset ? tagset.attr('data-id') : null));
            div.addClass('ui-selected').on('click', tag_click).tooltip({
                track: true,
                show: false,
                hide: false,
                items: '[data-content]',
                content: function() {
                    return $(this).data('content');
                }
            });
            $("div.ui-selected", div_selected_tags).removeClass('ui-selected');
            div_selected_tags.append(div);
            tag_input.val('').focus();
            description_input.val('');
            enable_buttons();
        }
        sort_tag_list(div_selected_tags);
        update_filter();
        // scroll to first selected tag
        div_selected_tags.parent().scrollTop(
            $("div.ui-selected", div_selected_tags).offset().top -
            div_selected_tags.offset().top - 40);
        update_add_new_button_state();
    };

    var add_new_tag_on_enter_key = function(event) {
        if (event.which === 13 && !$("#id_add_new_tag").prop('disabled')) {
            add_new_tag(event);
        }
    };
    tag_input.keypress(add_new_tag_on_enter_key);
    description_input.keypress(add_new_tag_on_enter_key);

    var save_tags = function() {
        var existing_tags = [];
        var new_tags = $("#id_" + formset_prefix + "-TOTAL_FORMS");
        var count = 0;
        $('div', div_selected_tags).each(function() {
            var tag_id = this.getAttribute('data-id');
            if (tag_id[0] === "-") { // newly created tag
                new_tags.after($("<input type='hidden' />").attr(
                    'name', "newtags-" + count + "-tag").val($(this).text()));
                new_tags.after($("<input type='hidden' />").attr(
                    'name', "newtags-" + count + "-description").val(
                        this.getAttribute('data-description')));
                new_tags.after($("<input type='hidden' />").attr(
                    'name', "newtags-" + count + "-tagset").val(
                        this.getAttribute('data-set')));
                count += 1;
            } else {
                // previously existing tag link owned by current user
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
        if (loaded && tag_input.val() !== '' &&
            tag_input.val() !== tag_input.attr('placeholder')) {
            $("#id_add_new_tag").prop('disabled', false);
        } else {
            $("#id_add_new_tag").prop('disabled', true);
        }
    };

    $("#id_tag_select_button").click(select_tags);
    $("#id_tag_deselect_button").click(deselect_tags);
    $("#id_add_new_tag").click(add_new_tag);
    $("#add_tags_form").off('prepare-submit').on('prepare-submit', save_tags);
    tag_input.keyup(update_add_new_button_state).change(
        update_add_new_button_state);
    update_add_new_button_state();
    tag_input_filter.keyup(update_filter).change(update_filter);
    $("select[name=filter_mode],select[name=filter_owner_mode]").change(
        update_filter);

    loader();

    // placeholder fixes - should probably be in a more generic place
    $('[placeholder]').focus(function() {
        var input = $(this);
        if (input.val() === input.attr('placeholder')) {
            input.val('');
            input.removeClass('placeholder');
        }
    }).blur(function() {
        var input = $(this);
        if (input.val() === '' || input.val() === input.attr('placeholder')) {
            input.addClass('placeholder');
            input.val(input.attr('placeholder'));
        }
    }).blur().parents('form').submit(function() {
        $(this).find('[placeholder]').each(function() {
            var input = $(this);
            if (input.val() === input.attr('placeholder')) {
                input.val('');
            }
        });
    });
};
