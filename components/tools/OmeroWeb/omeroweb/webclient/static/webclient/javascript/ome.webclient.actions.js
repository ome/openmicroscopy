var multi_key = function() {
    if (navigator.appVersion.indexOf("Mac")!=-1) return "meta";
    else return "ctrl";
};

jQuery.fn.hide_if_empty = function() {
    if ($(this).children().length == 0) {
        $(this).hide();
    } else {
        $(this).show();
    }
  return this;
};

var addToBasket = function(selected, prefix) {
    var productListQuery = new Array("action=add");
    if (selected != null && selected.length > 0) {
        selected.each(function(i) {
            productListQuery[i+1]= $(this).attr('id').replace("-","=");
        });
    } else {
        alert ("Please select at least one element."); 
        return
    }
    $.ajax({
        type: "POST",
        url: prefix, //this.href,
        data: productListQuery.join("&"),
        contentType:'html',
        success: function(responce){
            if(responce.match(/(Error: ([A-z]+))/gi)) {
                alert(responce)
            } else {
                calculateCartTotal(responce);
            }
        }
    });
};

// called from tree_selection_changed() below
var handle_tree_selection = function(data) {
    var selected_objs = [];

    if (typeof data != 'undefined' && typeof data.inst != 'undefined') {
        
        var selected = data.inst.get_selected();
        var share_id = null;
        if (selected.length == 1) {
            var pr = selected.parent().parent();
            if (pr.length>0 && pr.attr('rel') && pr.attr('rel').replace("-locked", "")==="share") {
                share_id = pr.attr("id").split("-")[1];
            }
        }
        selected.each(function(){
            var oid = $(this).attr('id');
            // after copy & paste, node will have id E.g. copy_dataset-123
            if (oid.substring(0,5) == "copy_") oid = oid.substring(5, oid.length);
            var selected_obj = {"id":oid, "rel":$(this).attr('rel')}
            selected_obj["class"] = $(this).attr('class');
            if (share_id) selected_obj["share"] = share_id;
            selected_objs.push(selected_obj);
        });
    }
    $("body")
        .data("selected_objects.ome", selected_objs)
        .trigger("selection_change.ome");
}

var select_timeout;
// called on selection and deselection changes in jstree
var tree_selection_changed = function(data, evt) {
    // handle case of deselection immediately followed by selection - Only fire on selection
    if (typeof select_timeout != 'undefined') {
        clearTimeout(select_timeout);
    }
    select_timeout = setTimeout(function() {
        handle_tree_selection(data);
    }, 10);
}

// called when we change the index of a plate or acquisition
var field_selection_changed = function(field) {
    
    var datatree = $.jstree._focused();
    datatree.data.ui.last_selected;
    $("body")
        .data("selected_objects.ome", [{"id":datatree.data.ui.last_selected.attr("id"), "index":field}])
        .trigger("selection_change.ome", $(this).attr('id'));
}

// actually called when share is edited, to refresh right-hand panel
var share_selection_changed = function(share_id) {
    $("body")
        .data("selected_objects.ome", [{"id": share_id}])
        .trigger("selection_change.ome");
}

var table_selection_changed = function($selected) {
    var selected_objs = [];
    if (typeof $selected != 'undefined') {
        $selected.each(function(i){
            selected_objs.push( {"id":$(this).attr('id')} );
        });
    }
    $("body")
        .data("selected_objects.ome", selected_objs)
        .trigger("selection_change.ome");
}

// handles selection for 'clicks' on table (search, history & basket) 
// including multi-select for shift and meta keys
var handleTableClickSelection = function(event) {

    var $clickedRow = $(event.target).parents('tr:first');
    var rows = $("table#dataTable tbody tr");
    var selIndex = rows.index($clickedRow.get(0));
    
    if ( event.shiftKey ) {
        // get existing selected items
        var $s = $("table#dataTable tbody tr.ui-selected");
        if ($s.length == 0) {
            $clickedRow.addClass("ui-selected");
            table_selection_changed($clickedRow);
            return;
        }
        var sel_start = rows.index($s.first());
        var sel_end = rows.index($s.last());
        
        // select all rows between new and existing selections
        var new_start, new_end
        if (selIndex < sel_start) {
            new_start = selIndex
            new_end = sel_start
        } else if (selIndex > sel_end) {
            new_start = sel_end+1
            new_end = selIndex+1
        // or just from the first existing selection to new one
        } else {
            new_start = sel_start
            new_end = selIndex
        }
        for (var i=new_start; i<new_end; i++) {
            rows.eq(i).addClass("ui-selected");
        }
    }
    else if (event.metaKey) {
        if ($clickedRow.hasClass("ui-selected")) $clickedRow.removeClass("ui-selected");
        else $clickedRow.addClass("ui-selected");
    }
    else {
        rows.removeClass("ui-selected");
        $clickedRow.addClass("ui-selected");
    }
    // update right hand panel etc
    table_selection_changed($("table#dataTable tbody tr.ui-selected"));
}

// called from click events on plate. Selected wells 
var well_selection_changed = function($selected, well_index, plate_class) {
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
}


// This is called by the Pagination controls at the bottom of icon or table pages.
// We simply update the 'page' data on the parent (E.g. dataset node in tree) and refresh
function doPagination(view, page) {
    if (view == "icon") var $container = $("#content_details");
    else if (view == "table") $container = $("#image_table");
    var rel = $container.attr('rel').split("-");
    var $parent = $("#dataTree #"+ rel[0]+'-'+rel[1]);
    $parent.data("page", page);     // let the parent node keep track of current page
    $("#dataTree").jstree("refresh", $('#'+rel[0]+'-'+rel[1]));
    $parent.children("a:eq(0)").click();    // this will cause center and right panels to update
    return false;
}


var OME = {}


// handle deleting of Tag, File, Comment
// on successful delete via AJAX, the parent .domClass is hidden
OME.removeItem = function(event, domClass, url, parentId) {
    var removeId = $(event.target).attr('id');
    var dType = removeId.split("-")[1]; // E.g. 461-comment
    // /webclient/action/remove/comment/461/?parent=image-257
    var $parent = $(event.target).parents(domClass);
    var $annContainer = $parent.parent();
    var confirm_remove = confirm_dialog('Remove '+ dType + '?',
        function() {
            if(confirm_remove.data("clicked_button") == "OK") {
                $.ajax({
                    type: "POST",
                    url: url,
                    data: {'parent':parentId},
					contentType: 'application/javascript',
                    dataType: 'json',
                    success: function(r){
                        if(eval(r.bad)) {
                            alert(r.errs);
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
}

OME.deleteItem = function(event, domClass, url) {
    var deleteId = $(event.target).attr('id');
    var dType = deleteId.split("-")[1]; // E.g. 461-comment
    // /webclient/action/delete/file/461/?parent=image-257
    var $parent = $(event.target).parents("."+domClass);
    var $annContainer = $parent.parent();
    var confirm_remove = confirm_dialog('Delete '+ dType + '?',
        function() {
            if(confirm_remove.data("clicked_button") == "OK") {
                $.ajax({
                    type: "POST",
                    url: url,
                    contentType: 'application/javascript',
                    dataType:'json',
                    success: function(r){
                        if(eval(r.bad)) {
                            alert(r.errs);
                        } else {
                            // simply remove the item (parent class div)
                            $parent.remove();
                            $annContainer.hide_if_empty();
                            window.parent.refreshActivities();
                        }
                    }
                });
            }
        }
    );
    event.preventDefault();
    return false;
}

jQuery.fn.tooltip_init = function() {
    $(this).tooltip({
        bodyHandler: function() {
                return $(this).parent().children("span.tooltip_html").html();
            },
        track: true,
        delay: 0,
        showURL: false,
        fixPNG: true,
        showBody: " - ",
        top: 10,
        left: -100
    });
  return this;
};