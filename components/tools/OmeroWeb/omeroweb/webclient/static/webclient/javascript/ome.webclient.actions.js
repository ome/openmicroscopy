//
// Copyright (C) 2013-2014 University of Dundee & Open Microscopy Environment.
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

/*global OME:true */
if (typeof OME === "undefined") {
    OME = {};
}

OME.multi_key = function() {
    if (navigator.appVersion.indexOf("Mac")!=-1) {
        return "meta";
    } else {
        return "ctrl";
    }
};

jQuery.fn.hide_if_empty = function() {
    if ($(this).children().length === 0) {
        $(this).hide();
    } else {
        $(this).show();
    }
  return this;
};

// called from OME.tree_selection_changed() below
OME.handle_tree_selection = function(data) {
    var selected_objs = [];

    if (typeof data != 'undefined' && typeof data.inst != 'undefined') {

        var selected = data.inst.get_selected();
        var share_id = null;
        if (selected.length == 1) {
            var pr = selected.parent().parent();
            if (pr.length>0 && pr.attr('rel') && pr.attr('rel')==="share") {
                share_id = pr.attr("id").split("-")[1];
            }
        }
        selected.each(function(){
            var $this = $(this),
                oid = $this.attr('id');
            if (typeof oid !== "undefined") {
                // after copy & paste, node will have id E.g. copy_dataset-123
                if (oid.substring(0,5) == "copy_") {
                    oid = oid.substring(5, oid.length);
                }
                var selected_obj = {"id":oid, "rel":$this.attr('rel')};
                selected_obj["class"] = $this.attr('class');
                if ($this.attr('data-fileset')) {
                    selected_obj["fileset"] = $this.attr('data-fileset');
                }
                if (share_id) {
                    selected_obj["share"] = share_id;
                }
                selected_objs.push(selected_obj);
            }
        });
    }
    $("body")
        .data("selected_objects.ome", selected_objs)
        .trigger("selection_change.ome");
};

// called on selection and deselection changes in jstree
OME.tree_selection_changed = function(data, evt) {
    // handle case of deselection immediately followed by selection - Only fire on selection
    if (typeof OME.select_timeout != 'undefined') {
        clearTimeout(OME.select_timeout);
    }
    OME.select_timeout = setTimeout(function() {
        OME.handle_tree_selection(data);
    }, 10);
};

// Short-cut to setting selection to [], with option to force refresh.
// (by default, center panel doesn't clear when nothing is selected)
OME.clear_selected = function(force_refresh) {
    var refresh = (force_refresh === true);
    $("body")
        .data("selected_objects.ome", [])
        .trigger("selection_change.ome", [refresh]);
};

// called when we change the index of a plate or acquisition
OME.field_selection_changed = function(field) {

    var datatree = $.jstree._focused();
    $("body")
        .data("selected_objects.ome", [{"id":datatree.data.ui.last_selected.attr("id"), "index":field}])
        .trigger("selection_change.ome", $(this).attr('id'));
};

// select all images from the specified fileset (if currently visible)
OME.select_fileset_images = function(filesetId) {
    var datatree = $.jstree._focused();
    $("#dataTree li[data-fileset="+filesetId+"]").each(function(){
        datatree.select_node(this);
    });
};

// actually called when share is edited, to refresh right-hand panel
OME.share_selection_changed = function(share_id) {
    $("body").trigger("selection_change.ome");
};


// Standard ids are in the form TYPE-ID, web extensions may add an
// additional -SUFFIX
OME.table_selection_changed = function($selected) {
    var selected_objs = [];
    if (typeof $selected != 'undefined') {
        $selected.each(function(i){
            var id_split = this.id.split('-');
            var id_obj = id_split.slice(0, 2).join('-');
            var id_suffix = id_split.slice(2).join('-');
            selected_objs.push( {"id":id_obj, "id_suffix":id_suffix} );
        });
    }
    $("body")
        .data("selected_objects.ome", selected_objs)
        .trigger("selection_change.ome");
};

// handles selection for 'clicks' on table (search, history & basket)
// including multi-select for shift and meta keys
OME.handleTableClickSelection = function(event) {

    var $clickedRow = $(event.target).parents('tr:first');
    var rows = $("table#dataTable tbody tr");
    var selIndex = rows.index($clickedRow.get(0));

    if ( event.shiftKey ) {
        // get existing selected items
        var $s = $("table#dataTable tbody tr.ui-selected");
        if ($s.length === 0) {
            $clickedRow.addClass("ui-selected");
            OME.table_selection_changed($clickedRow);
            return;
        }
        var sel_start = rows.index($s.first());
        var sel_end = rows.index($s.last());

        // select all rows between new and existing selections
        var new_start, new_end;
        if (selIndex < sel_start) {
            new_start = selIndex;
            new_end = sel_start;
        } else if (selIndex > sel_end) {
            new_start = sel_end+1;
            new_end = selIndex+1;
        // or just from the first existing selection to new one
        } else {
            new_start = sel_start;
            new_end = selIndex;
        }
        for (var i=new_start; i<new_end; i++) {
            rows.eq(i).addClass("ui-selected");
        }
    }
    else if (event.metaKey) {
        if ($clickedRow.hasClass("ui-selected")) {
            $clickedRow.removeClass("ui-selected");
        }
        else {
            $clickedRow.addClass("ui-selected");
        }
    }
    else {
        rows.removeClass("ui-selected");
        $clickedRow.addClass("ui-selected");
    }
    // update right hand panel etc
    OME.table_selection_changed($("table#dataTable tbody tr.ui-selected"));
};

// called from click events on plate. Selected wells
OME.well_selection_changed = function($selected, well_index, plate_class) {
    var selected_objs = [];
    $selected.each(function(i){
        selected_objs.push( {"id":$(this).attr('id').replace("=","-"),
                "rel":$(this).attr('rel'),
                "index":well_index,
                "class":plate_class} );     // assume every well has same permissions as plate
    });

    $("body")
        .data("selected_objects.ome", selected_objs)
        .trigger("selection_change.ome");
};


// This is called by the Pagination controls at the bottom of icon or table pages.
// We simply update the 'page' data on the parent (E.g. dataset node in tree) and refresh
OME.doPagination = function(view, page) {
    var $container;
    if (view == "icon") {
        $container = $("#content_details");
    }
    else if (view == "table") {
        $container = $("#image_table");
    }
    var rel = $container.attr('rel').split("-");
    var $parent = $("#dataTree #"+ rel[0]+'-'+rel[1]);
    $parent.data("page", page);     // let the parent node keep track of current page
    $("#dataTree").jstree("refresh", $('#'+rel[0]+'-'+rel[1]));
    $parent.children("a:eq(0)").click();    // this will cause center and right panels to update
    return false;
};



// handle deleting of Tag, File, Comment
// on successful delete via AJAX, the parent .domClass is hidden
OME.removeItem = function(event, domClass, url, parentId, index) {
    var removeId = $(event.target).attr('id');
    var dType = removeId.split("-")[1]; // E.g. 461-comment
    // /webclient/action/remove/comment/461/?parent=image-257
    var $parent = $(event.target).parents(domClass);
    var $annContainer = $parent.parent();
    var r = 'Remove ';
    if (dType === 'comment') r = 'Delete ';
    var confirm_remove = OME.confirm_dialog(r + dType + '?',
        function() {
            if(confirm_remove.data("clicked_button") == "OK") {
                $.ajax({
                    type: "POST",
                    url: url,
                    data: {'parent':parentId, 'index':index},
                    dataType: 'json',
                    success: function(r){
                        if(eval(r.bad)) {
                            OME.alert_dialog(r.errs);
                        } else {
                            // simply remove the item (parent class div)
                            //console.log("Success function");
                            $parent.remove();
                            $annContainer.hide_if_empty();
                        }
                    }
                });
            }
        }
    );
    return false;
};

OME.deleteItem = function(event, domClass, url) {
    var deleteId = $(event.target).attr('id');
    var dType = deleteId.split("-")[1]; // E.g. 461-comment
    // /webclient/action/delete/file/461/?parent=image-257
    var $parent = $(event.target).parents("."+domClass);
    var $annContainer = $parent.parent();
    var confirm_remove = OME.confirm_dialog('Delete '+ dType + '?',
        function() {
            if(confirm_remove.data("clicked_button") == "OK") {
                $.ajax({
                    type: "POST",
                    url: url,
                    dataType:'json',
                    success: function(r){
                        if(eval(r.bad)) {
                            OME.alert_dialog(r.errs);
                        } else {
                            // simply remove the item (parent class div)
                            $parent.remove();
                            $annContainer.hide_if_empty();
                            window.parent.OME.refreshActivities();
                        }
                    }
                });
            }
        }
    );
    event.preventDefault();
    return false;
};

// Used to filter annotations in the metadata_general and batch_anntotate panels.
// Assumes a single #annotationFilter select on the page.
OME.filterAnnotationsAddedBy = function() {
    var $this = $("#annotationFilter"),
        val = $this.val(),
        userId = $this.attr('data-userId');

    // select made smaller if only 'Show all' text
    if (val === "all") {
        $this.css('width', '80px');
    } else {
        $this.css('width', '180px');
    }

    $('.tag_annotation_wrapper, .keyValueTable, .file_ann_wrapper, .ann_comment_wrapper, #custom_annotations tr')
            .each(function() {
        var $ann = $(this),
            addby = $ann.attr('data-added-by').split(",");
        var show = false;
        switch (val) {
            case "me":
                show = ($.inArray(userId, addby) > -1);
                break;
            case "others":
                for (var i=0; i<addby.length; i++) {
                    if (addby[i] !== userId) {
                        show = true;
                    }
                }
                break;
            default:    // 'all'
                show = true;
        }
        if (show) {
            $ann.show();
        } else {
            $ann.hide();
        }
    });
};

// More code that is shared between metadata_general and batch_annotate panels
// Called when panel loaded. Does exactly what it says on the tin.
OME.initToolbarDropdowns = function() {
    // -- Toolbar buttons - show/hide dropdown options --
    $(".toolbar_dropdown ul").css('visibility', 'hidden');
    // show on click
    var $toolbar_dropdownlists = $(".toolbar_dropdown ul");
    $(".toolbar_dropdown button").click(function(e) {
        // hide any other lists that might be showing...
        $toolbar_dropdownlists.css('visibility', 'hidden');
        // then show this one...
        $("ul", $(this).parent()).css('visibility', 'visible');
        e.preventDefault();
        return false;
    });
    // on hover-out, hide drop-down menus
    $toolbar_dropdownlists.hover(function(){}, function(){
        $(this).css('visibility', 'hidden');
    });

    // For Figure scripts, we need a popup:
    $("#figScriptList li a").click(function(event){
        if (!$(this).parent().hasClass("disabled")) {
            OME.openScriptWindow(event, 800, 600);
        }
        event.preventDefault();
        return false;
    });
};

// Simply add query to thumbnail src to force refresh.
// By default we do ALL thumbnails, but can also specify ID
OME.refreshThumbnails = function(options) {
    options = options || {};
    var rdm = Math.random(),
        thumbs_selector = "#dataIcons img",
        search_selector = ".search_thumb",
        spw_selector = "#spw img";
    // handle Dataset thumbs, search rusults and SPW thumbs
    if (options.imageId) {
        thumbs_selector = "#image_icon-" + options.imageId + " img";
        search_selector = "#image-" + options.imageId + " img.search_thumb";
        spw_selector += "#image-" + options.imageId;
    }
    $(thumbs_selector + ", " + spw_selector + ", " + search_selector).each(function(){
        var $this = $(this),
            base_src = $this.attr('src').split('?')[0];
        $this.attr('src', base_src + "?_="+rdm);
    });

    // Update viewport via global variable
    if (!options.ignorePreview && OME.preview_viewport && OME.preview_viewport.loadedImg.id) {
        OME.preview_viewport.load(OME.preview_viewport.loadedImg.id);
    }
};

OME.truncateNames = (function(){
    var insHtml;
    // Resizing of left panel dynamically truncates image names
    // NB: no images loaded when page first laods. Do everything on resize...
    var truncateNames = function() {
        if (!insHtml) {
            // use the first image to get the html
            var $ins = $('.jstree li[rel^="image"] a ins').first();
            if ($ins.length > 0) {
                insHtml = $ins.get(0).outerHTML;
            }
        }
        // get the panel width, and number of chars that will fit
        var lp_width = $("#left_panel").width() - 20;  // margin
        // Go through all images, truncating names...
        // When we find matching size for a name length, save it...
        var maxChars;
        $('.jstree li[rel^="image"] a').each(function(){
            var $this = $(this),
                ofs = $this.offset(),
                name = $this.attr('data-name'),
                truncatedName,
                chars = name.length;
            name = name.escapeHTML();
            // if we know maxChars and we're longer than that...
            if (maxChars && name.length > maxChars) {
                chars = maxChars;
                truncatedName = "..." + name.slice(-chars);
                $this.html(insHtml + truncatedName);
            } else {
                // if needed, trim the full name until it fits and save maxChars
                $this.html(insHtml + name);
                var w = $this.width() + ofs.left;
                while (w > lp_width && chars > 2) {
                    chars = chars-2;
                    truncatedName = "..." + name.slice(-chars);
                    $this.html(insHtml + truncatedName);
                    w = $this.width() + ofs.left;
                    maxChars = chars;
                }
            }
        });
    };
    return truncateNames;
}());

// Handle deletion of selected objects in jsTree in container_tags.html and containers.html
OME.handleDelete = function() {
    var datatree = $.jstree._focused();
    var selected = datatree.get_selected();

    var del_form = $( "#delete-dialog-form" );
    del_form.dialog( "open" )
        .removeData("clicked_button");
    // clear previous stuff from form
    $.removeData(del_form, "clicked_button");
    $("#delete_contents_form").show();
    del_form.unbind("dialogclose");
    del_form.find("input[type='checkbox']").prop('checked', false);

    // set up form - process all the objects for data-types and children
    var ajax_data = [];
    var q = false;
    var dtypes = {};
    var first_parent;   // select this when we're done deleting
    var notOwned = false;
    selected.each(function (i) {
        if (!first_parent) first_parent = datatree._get_parent(this);
        var $this = $(this);
        ajax_data[i] = $this.attr('id').replace("-","=");
        var dtype = $this.attr('rel');
        if (dtype in dtypes) dtypes[dtype] += 1;
        else dtypes[dtype] = 1;
        if (!q && $this.attr('rel').indexOf('image')<0) q = true;
        if (!$this.hasClass('isOwned')) notOwned = true;
    });
    if (notOwned) {
        $("#deleteOthersWarning").show();
    } else {
        $("#deleteOthersWarning").hide();
    }
    var type_strings = [];
    for (var key in dtypes) {
        if (key === "acquisition") key = "Plate Run";
        type_strings.push(key.capitalize() + (dtypes[key]>1 && "s" || ""));
    }
    var type_str = type_strings.join(" & ");    // For delete dialog: E.g. 'Project & Datasets'
    $("#delete_type").text(type_str);
    if (!q) $("#delete_contents_form").hide();  // don't ask about deleting contents

    // callback when delete dialog is closed
    del_form.bind("dialogclose", function(event, ui) {
        if (del_form.data("clicked_button") == "Yes") {
            var delete_anns = $("#delete_anns").prop('checked');
            var delete_content = true;      // $("#delete_content").prop('checked');
            if (delete_content) ajax_data[ajax_data.length] = 'child=true';
            if (delete_anns) ajax_data[ajax_data.length] = 'anns=true';
            var url = del_form.attr('data-url');
            datatree.deselect_all();
            $.ajax({
                async : false,
                url: url,
                data : ajax_data.join("&"),
                dataType: "json",
                type: "POST",
                success: function(r){
                    if(eval(r.bad)) {
                          $.jstree.rollback(data.rlbk);
                          alert(r.errs);
                      } else {
                          // If deleting 'Plate Run', clear selection
                          if (type_str.indexOf('Plate Run') > -1) {
                            OME.clear_selected(true);
                          } else {
                            // otherwise, select parent
                            OME.tree_selection_changed();   // clear center and right panels etc
                            first_parent.children("a").click();
                          }
                          // remove node from tree
                          datatree.delete_node(selected);
                          OME.refreshActivities();
                      }
                },
                error: function(response) {
                    $.jstree.rollback(data.rlbk);
                    alert("Internal server error. Cannot remove object.");
                }
            });
        }
    });

    // Check if delete will attempt to partially delete a Fileset.
    var $deleteYesBtn = $('.delete_confirm_dialog .ui-dialog-buttonset button:nth-child(1)'),
        $deleteNoBtn = $('.delete_confirm_dialog .ui-dialog-buttonset button:nth-child(2) span'),
        filesetCheckUrl = del_form.attr('data-fileset-check-url');
    $.get(filesetCheckUrl + "?" + OME.get_tree_selection(), function(html){
        if($('div.split_fileset', html).length > 0) {
            var $del_form_content = del_form.children().hide();
            del_form.append(html);
            $deleteYesBtn.hide();
            $deleteNoBtn.text("Cancel");
            // On dialog close, clean-up what we changed above
            del_form.bind("dialogclose", function(event, ui) {
                $deleteYesBtn.show();
                $deleteNoBtn.text("No");
                $("#chgrp_split_filesets", del_form).remove();
                $del_form_content.show();
            });
        }
    });
};

OME.formatScriptName = function(name) {
    // format script name by replacing '_' with ' '
    name = name.replace(/_/g, " ");
    if (name.indexOf(".") > 0) {
        name = name.slice(0, name.indexOf(".")) + "...";
    }
    return name
};

OME.showScriptList = function(event) {
    // show menu - load if empty
    // $('#scriptList').css('visibility', 'visible');
    $('#scriptList').show();
    if ($("#scriptList li").length == 0){  // if none loaded yet...
        var $scriptLink = $(this);
        var $scriptSpinner = $("#scriptSpinner").show();
        var script_list_url = $(this).attr('href');
        $.get(script_list_url, function(data) {

            var build_ul = function(script_data) {
                var html = "";
                for (var i=0; i<script_data.length; i++) {
                    var li = script_data[i],   // dict of 'name' and 'ul' for menu items OR 'id' for scripts
                        name = li.name;
                    if (li.id) {
                        name = OME.formatScriptName(name);
                        html += "<li><a href='" + event.data.webindex + "script_ui/"+ li.id + "/'>" + name + "</a></li>";
                    } else {
                        html += "<li class='menuItem'><a href='#'>" + name + "</a>";
                        // sub-menus have a 'BACK' button at the top
                        html += "<ul><li class='menu_back'><a href='#'>back</a></li>" + build_ul(li.ul) + "</ul>";
                        html += "</li>";
                    }
                }
                return html;
            }

            var html = "<ul class='menulist'>" + build_ul(data) + "</ul>";

            $('#scriptList').append($(html));

            $('#scriptList ul ul').hide();
            $scriptSpinner.hide();
      }, "json");
    }
};

OME.hideScriptList = function() {
    $("#scriptList").hide();
}

jQuery.fn.tooltip_init = function() {
    $(this).tooltip({
        items: '.tooltip',
        content: function() {
            return $(this).parent().find("span.tooltip_html").html();
        },
        track: true,
        show: false,
        hide: false
    });
  return this;
};
