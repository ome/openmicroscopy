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
        },
        error: function(responce) {
            alert("Internal server error. Cannot add to basket.")
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
    $("body")
        .data("selected_objects.ome", [{"id": $row.attr("id")}])
        .trigger("selection_change.ome");
}

// change in seletion of history results - only single object selection
var history_selection_changed = function($row) {
    $("body")
        .data("selected_objects.ome", [{"id": $row.attr("id")}])
        .trigger("selection_change.ome");
}

// actually called when share is edited, to refresh right-hand panel
var share_selection_changed = function(share_id) {
    //loadMetadataPanel("{% url load_metadata_details c_type='share' c_id=manager.share.id %}");
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
        
    //loadMetadataPanel('{% url load_metadata_details %}image/'+$("tr.ui-selected td input", this).first().attr("id")+'/');
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

var loadMetadataPanel = function(src, html) {
    
    console.log("DEPRECATED! loadMetadataPanel()", src, html);
    
    var $metadataPanel = $("#right_panel");

    if (src!=null) {
        $metadataPanel.load(src);
    } else {
        $metadataPanel.html(html);
    }
};


function changeView(view, page) { 
    var rel = $("div#content_details").attr('rel').split("-");
    if(rel.indexOf('orphaned')>=0) {
        url = '/webclient/load_data/orphaned/?view='+view;
    } else if(rel.indexOf('tag')>=0) {
        $("div#content_details").html('<p>Loading data... please wait <img src="../../static/webgateway/img/spinner.gif"/></p>');
        url = '/webclient/load_tags/tag/'+rel[1]+'/?view='+view;
    } else {
        $("div#content_details").html('<p>Loading data... please wait <img src="../../static/webgateway/img/spinner.gif"/></p>');
        url = '/webclient/load_data/dataset/'+rel[1]+'/?view='+view;
    }
    if (page!=null && page > 0) {
        url = url+"&page="+page;
    }
    $("div#content_details").html('<p>Loading data... please wait <img src="../../static/webgateway/img/spinner.gif"/></p>');
    $("div#content_details").load(url);
    return false;
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
            },
            error: function(responce) {
                $($('#id_'+metadata_type).parent().find('img')).remove()
                alert("Cannot save new value for '"+metadata_type+"'.")
            }
        });
    }
}

// This is called by the Pagination controls at the bottom of icon or table pages.
// We simply update the 'page' data on the parent (E.g. dataset node in tree) and refresh
function doPagination(view, page) {
    var $container = $("#content_details");
    if (view == "tree") $container = $("#image_table");
    var rel = $container.attr('rel').split("-");
    var $parent = $("#dataTree #"+ rel[0]+'-'+rel[1]);
    $parent.data("page", page);     // let the parent node keep track of current page
    $("#dataTree").jstree("refresh", $('#'+rel[0]+'-'+rel[1]));
    $parent.children("a:eq(0)").click();    // this will cause center and right panels to update
    return false;
}


function makeDiscussion() {
    src = '/webclient/basket/todiscuss/';
    loadMetadataPanel(src);
    return false;
}