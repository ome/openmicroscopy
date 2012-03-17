var multi_key = function() {
    if (navigator.appVersion.indexOf("Mac")!=-1) return "meta";
    else return "ctrl";
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

var multipleAnnotation = function(selected, index, prefix){
    if (selected != null && selected.length > 0) {
        var productListQuery = new Array(); 
        selected.each( function(i){
            productListQuery[i] = $(this).attr('id').replace("-","=");
        });
        var query = prefix+"?"+productListQuery.join("&")
        if (index != null && index > -1) {
            query += "&index="+index;
        }
        loadMetadataPanel(query);
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

var refreshCenterPanel = function() {
    var rel = $("div#content_details").attr("rel");
    var page = parseInt($("div#content_details").find("#page").attr("rel"));
    
    if (typeof rel!=="undefined") {
        if (rel.indexOf("orphaned")>=0) {
            $("div#content_details").html('<p>Loading data... please wait <img src ="../../static/webgateway/img/spinner.gif"/></p>');
            url = '/webclient/load_data/'+rel.split('-')[0]+'/';
        } else if (rel.indexOf("share")>=0) {
            $("div#content_details").html('<p>Loading data... please wait <img src ="../../static/webgateway/img/spinner.gif"/></p>');
            url = '/webclient/load_public/'+rel.split('-')[1]+'/';
        } else if(rel.indexOf('tag')>=0) {
            $("div#content_details").html('<p>Loading data... please wait <img src="../../static/webgateway/img/spinner.gif"/></p>');
            url = '/webclient/load_tags/tag/'+rel.split('-')[1]+'/';
        } else {
            $("div#content_details").html('<p>Loading data... please wait <img src ="../../static/webgateway/img/spinner.gif"/></p>');
            url = '/webclient/load_data/'+rel.replace('-', '/')+'/';
        }
        
        var view = $("div#content_details").find("#toolbar").attr('rel') ? $("div#content_details").find("#toolbar").attr('rel') : "icon";
        
        $("div#content_details").html('<p>Loading data... please wait <img src ="../../static/webgateway/img/spinner.gif"/></p>');
        url = url+'?view='+view
        if (page!=null && page > 0) {
            url = url+"&page="+page;
        }        
        $("div#content_details").load(url);
        if(rel.indexOf('tag')<0) $("div#content_details").attr('rel', rel);
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

function makeShare(prefix) {
    if (!isCheckedById("image")) {//&& !isCheckedById("dataset") && !isCheckedById("plate")) {
        alert ("Please select at least one image. Currently you cannot add other objects to basket."); 
    } else { 
        var productArray = $("input[type='checkbox']:checked");
        var productListQuery = "";
        if (productArray.length > 0 ) {
            productArray.each(function() {
                if(this.checked) {
                    productListQuery += "&"+this.name+"="+this.id;
                }
            });
        } else {
            productListQuery += "&"+productArray.name+"="+productArray.id;
        }
    }
    
    src = prefix+'?'+productListQuery+'';
    loadMetadataPanel(src);
    return false;
}

function makeDiscussion() {
    src = '/webclient/basket/todiscuss/';
    loadMetadataPanel(src);
    return false;
}