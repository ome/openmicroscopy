var multi_key = function() {
    if (navigator.appVersion.indexOf("Mac")!=-1) return "meta";
    else return "ctrl";
};

var calculateCartTotal = function(total){
    $('#cartTotal').html(total); 
};

var addToBasket = function(selected) {
    var productListQuery = "action=add";                    
    if (selected != null && selected.length > 0) {
        selected.each(function() {
            productListQuery += "&"+$(this).attr('id').replace("-","=");
        });                        
    } else {
        alert ("Please select at least one element."); 
        return
    }                    
    $.ajax({
        type: "POST",
        url: "/webclient/basket/update/", //this.href,
        data: productListQuery,
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

var multipleAnnotation = function(){
    var datatree = $.jstree._focused();
    if (datatree.data.ui.selected.length < 1) {
        alert ("Please select at least one element."); 
    }
    var productListQuery = "/webclient/metadata_details/multiaction/annotatemany/?"; 
    datatree.data.ui.selected.each( function(){
        productListQuery += "&"+$(this).attr('id').replace("-","=");
    });                    
    loadMetadataPanel(productListQuery);
};

var loadMetadataPanel = function(src, html) {
    var iframe = $("div#metadata_details").find('iframe');
    var description = $("div#metadata_description");

    $("#right_panel").show();
    $("#swapMeta").html('<img tabindex="0" src="/appmedia/omeroweb/images/spacer.gif"" class="collapsed-right" id="lhid_trayhandle_icon_right">');

    if (src!=null) {
        description.hide();
        iframe.show();
    } else {
        iframe.hide();
        description.show();
    }

    if (iframe.length > 0) {
        if (html!=null) {
            iframe.attr('src', "");
            iframe.hide();
            description.html(html);
        } else  {
            description.empty();
            iframe.attr('src', src);
            iframe.show();
        }
    } else {
        var h = $(window).height()-200;
        $("div#metadata_details").html('<iframe width="370" height="'+(h+31)+'" src="'+src+'" id="metadata_details" name="metadata_details"></iframe>');
        $('iframe#metadata_details').load();
    }
};

var refreshCenterPanel = function() {
    var rel = $("div#content_details").attr("rel");
    if (typeof rel!=="undefined") {
        if (rel.indexOf("orphaned")>=0) {
            $("div#content_details").html('<p>Loading data... please wait <img src ="{% url webstatic "images/spinner.gif" %}"/></p>');
            $("div#content_details").attr('rel', rel);
            $("div#content_details").load('/webclient/load_data/'+rel.split('-')[0]+'/?view=icon');
        } else {
            $("div#content_details").html('<p>Loading data... please wait <img src ="{% url webstatic "images/spinner.gif" %}"/></p>');
            $("div#content_details").attr('rel', rel);
            $("div#content_details").load('/webclient/load_data/'+rel.replace('-', '/')+'/?view=icon');
        }        
    }
};



function changeView(view) { 
    var rel = $("div#content_details").attr('rel').split("-");
    if(rel.indexOf('orphaned')>=0) {
        $("div#content_details").html('<p>Loading data... please wait <img src="/appmedia/omeroweb/images/spinner.gif"/></p>');
        $("div#content_details").load('/webclient/load_data/orphaned/?view='+view);
    } else if(rel.indexOf('tag')>=0) {
        $("div#content_details").html('<p>Loading data... please wait <img src="/appmedia/omeroweb/images/spinner.gif"/></p>');
        $("div#content_details").load('/webclient/load_tags/tag/'+rel[1]+'/?view='+view);
    } else {
        $("div#content_details").html('<p>Loading data... please wait <img src="/appmedia/omeroweb/images/spinner.gif"/></p>');
        $("div#content_details").load('/webclient/load_data/dataset/'+rel[1]+'/?view='+view);
    }
    return false;
};

function saveMetadata (image_id, metadata_type, metadata_value) {
    if (image_id == null) {
        alert("No image selected.")
    } else {
        $($('#id_'+metadata_type).parent()).append('<img src="/appmedia/omeroweb/images/spinner.gif"/>');
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

function doPagination(view, page) {
    var rel = $("div#content_details").attr('rel').split("-");
    $("div#content_details").html('<p>Loading data... please wait <img src="/appmedia/omeroweb/images/spinner.gif"/></p>');
    $("div#content_details").load('/webclient/load_data/dataset/'+rel[1]+'/?view='+view+'&page='+page);
    return false;
}

function makeShare() {
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
    
    src = '/webclient/basket/toshare/?'+productListQuery+'';
    loadMetadata(src);
    return false;
}

function makeDiscussion() {
    src = '/webclient/basket/todiscuss/';
    loadMetadata(src);
    return false;
}

function loadMetadata(src) {
    var h = $(window).height()-200;
    $("#right_panel").show();
    $("#swapMeta").html('<img tabindex="0" src="/appmedia/omeroweb/images/spacer.gif"" class="collapsed-right" id="lhid_trayhandle_icon_right">'); 
    $("div#metadata_details").html('<iframe width="370" height="'+(h+31)+'" src="'+src+'" id="metadata_details" name="metadata_details"></iframe>');
    $('iframe#metadata_details').load();
}