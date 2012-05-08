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

// called on selection changes in jstree
var tree_selection_changed = function(data) {
    
    var selected = data.inst.get_selected();
    var share_id = null;
    if (selected.length == 1) {
        var pr = selected.parent().parent();
        if (pr.length>0 && pr.attr('rel') && pr.attr('rel').replace("-locked", "")==="share") {
            share_id = pr.attr("id").split("-")[1];
        }
    }
    var selected_objs = [];
    selected.each(function(){
        var selected_obj = {"id":$(this).attr('id'), "rel":$(this).attr('rel')}
        if (share_id) selected_obj["share"] = share_id;
        selected_objs.push(selected_obj);
    });
    
    $("body")
        .data("selected_objects.ome", selected_objs)
        .trigger("selection_change.ome");
}

// called when we change the index of a plate or acquisition
var field_selection_changed = function(field) {
    
    var datatree = $.jstree._focused();
    datatree.data.ui.last_selected;
    $("body")
        .data("selected_objects.ome", [{"id":datatree.data.ui.last_selected.attr("id"), "index":field}])
        .trigger("selection_change.ome", $(this).attr('id'));
}

// change in seletion of search results - only single object selection
var search_selection_changed = function($row) {
    var $body = $("body");
    if (typeof $row != 'undefined') {
        $body.data("selected_objects.ome", [{"id": $row.attr("id")}])
    } else {
        $body.data("selected_objects.ome", [])
    }
    $body.trigger("selection_change.ome");
}

// change in seletion of history results - only single object selection
var history_selection_changed = function($row) {
    var $body = $("body");
    if (typeof $row != 'undefined') {
        $body.data("selected_objects.ome", [{"id": $row.attr("id")}])
    } else {
        $body.data("selected_objects.ome", [])
    }
    $body.trigger("selection_change.ome");
}

// actually called when share is edited, to refresh right-hand panel
var share_selection_changed = function(share_id) {
    $("body")
        .data("selected_objects.ome", [{"id": share_id}])
        .trigger("selection_change.ome");
}

var basket_selection_changed = function($selected) {

    var selected_objs = [];
    $selected.each(function(i){
        // we only support images in basket:
        selected_objs.push( {"id":"image-"+$(this).attr('id'), "rel":"image"} );
    });
    
    $("body")
        .data("selected_objects.ome", selected_objs)
        .trigger("selection_change.ome");
}

// called from click events on plate. Selected wells 
var well_selection_changed = function($selected, well_index) {
    var selected_objs = [];
    $selected.each(function(i){
        selected_objs.push( {"id":$(this).attr('id').replace("=","-"), "rel":$(this).attr('rel'), "index":well_index} );
    });
    
    $("body")
        .data("selected_objects.ome", selected_objs)
        .trigger("selection_change.ome");
}

var multipleAnnotation = function(selected, index, prefix){
    if (selected != null && selected.length > 0) {
        var productListQuery = new Array(); 
        selected.each( function(i){
            productListQuery[i] = {"id":$(this).attr('id').replace("-","=")};
        });
        var query = prefix+"?"+productListQuery.join("&")
        if (index != null && index > -1) {
            query += "&index="+index;
        }
        $("body")
            .data("selected_objects.ome", productListQuery)
            .trigger("selection_change.ome");
        
    } else {
        alert ("Please select at least one element."); 
    }
};


function saveMetadata (image_id, metadata_type, metadata_value) {
    if (image_id == null) {
        alert("No image selected.")
    } else {
        $($('#id_'+metadata_type).parent()).append('<img src="../../static/webgateway/img/spinner.gif"/>');
        $.ajax({
            type: "POST",
            url: "/webclient/metadata/image/"+image_id+"/", //this.href,
            data: "matadataType="+metadata_type+"&metadataValue="+metadata_value,
            contentType:'html',
            cache:false,
            success: function(responce){
                $($('#id_'+metadata_type).parent().find('img')).remove()
            }
        });
    }
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