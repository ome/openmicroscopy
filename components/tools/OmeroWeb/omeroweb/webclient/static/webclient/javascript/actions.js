var multi_key = function() {
    if (navigator.appVersion.indexOf("Mac")!=-1) return "meta";
    else return "ctrl";
};

var loadOtherPanels = function(inst, prefix) {
    var selected = inst.get_selected();
    if(selected.length > 0) {
        var cm_var = new Object();
        cm_var['content_details'] = {'url': null, 'rel': null, 'empty':false };
        cm_var['metadata_details']= {'iframe': null, 'html': null};

        var oid = selected.attr('id');
        var orel = selected.attr('rel').replace("-locked", "");
        var crel = $("div#content_details").attr('rel');
        if (typeof oid!=="undefined" && oid!==false) {
            if (oid.indexOf("orphaned")>=0) {
                if(oid!==crel) {
                    cm_var['metadata_details']['html'] = '<p>This is virtual container with orphaned images. These images are not linked anywhere. Just drag them to the selected container.</p>';
                    cm_var['content_details']['rel'] = oid;
                    cm_var['content_details']['url'] = prefix+'load_data/'+orel+'/?view=icon';
                } else {
                    cm_var['metadata_details']['html'] = '<p>This is virtual container with orphaned images. These images are not linked anywhere. Just drag them to the selected container.</p>';
                    cm_var['content_details']['empty'] = true;
                }
            } else if(oid.indexOf("experimenter")<0) {
                //METADATA panel
                if(orel=="image") {
                    var pr = selected.parent().parent();
                    if (pr.length>0 && pr.attr('rel').replace("-locked", "")==="share") {
                        cm_var['metadata_details']['iframe'] = prefix+'metadata_details/'+orel+'/'+oid.split("-")[1]+'/'+pr.attr("id").split("-")[1]+'/';
                    } else {
                        cm_var['metadata_details']['iframe'] = prefix+'metadata_details/'+orel+'/'+oid.split("-")[1]+'/';
                    }
                } else {
                    cm_var['metadata_details']['iframe'] = prefix+'metadata_details/'+orel+'/'+oid.split("-")[1]+'/';
                }
                
                // CONTENT panel
                if ($.inArray(orel, ["project", "screen"]) > -1) {
                    cm_var['content_details']['empty'] = true;
                } else if($.inArray(orel, ["plate"]) > -1) {
                    if (inst.is_leaf(selected)) {
                        cm_var['content_details']['rel'] = oid;
                        cm_var['content_details']['url'] = prefix+'load_data/'+orel+'/'+oid.split("-")[1]+'/';
                    } else {
                        cm_var['content_details']['empty'] = true;
                    }
                } else if($.inArray(orel, ["acquisition"]) > -1 && oid!==crel) {
                    var plate = inst._get_parent(selected).attr('id').replace("-", "/");
                    cm_var['content_details']['rel'] = oid;
                    cm_var['content_details']['url'] = prefix+'load_data/'+plate+'/'+orel+'/'+oid.split("-")[1]+'/';
                } else if($.inArray(orel, ["dataset", "tag"]) > -1 && oid!==crel) {
                    cm_var['content_details']['rel'] = oid;
                    cm_var['content_details']['url'] = prefix+'load_data/'+orel+'/'+oid.split("-")[1]+'/?view=icon';
                    
                } else if($.inArray(orel, ["share"]) > -1 && oid!==crel) {
                    cm_var['content_details']['rel'] = oid;
                    cm_var['content_details']['url'] = prefix+'load_data/'+oid.split("-")[1]+'/?view=icon';
                    
                } else if($.inArray(orel, ["tag"]) > -1 && oid!==crel) {
                    cm_var['content_details']['rel'] = oid;
                    cm_var['content_details']['url'] = prefix+'load_tags/?view=icon&o_type=tag&o_id='+oid.split("-")[1];
                } else if(orel=="image") {
                    var pr = selected.parent().parent();
                    if (pr.length>0 && pr.attr('id')!==crel) {
                        if(pr.attr('rel').replace("-locked", "")==="share" && pr.attr('id')!==crel) {
                            cm_var['content_details']['rel'] = pr.attr('id');
                            cm_var['content_details']['url'] = prefix+'load_data/'+pr.attr('id').split("-")[1]+'/?view=icon';
                        } else if (pr.attr('rel').replace("-locked", "")!=="tag") {
                            cm_var['content_details']['rel'] = pr.attr('id');
                            cm_var['content_details']['url'] = prefix+'load_data/'+pr.attr('rel').replace("-locked", "")+'/'+pr.attr("id").split("-")[1]+'/?view=icon';
                        } else if (pr.attr('rel').replace("-locked", "")!=="orphaned") {
                            cm_var['content_details']['rel'] = pr.attr('id');
                            cm_var['content_details']['url'] = prefix+'load_data/'+pr.attr('rel').replace("-locked", "")+'/'+pr.attr("id").split("-")[1]+'/?view=icon';
                        } else {
                            cm_var['content_details']['rel'] = pr.attr("id");
                            cm_var['content_details']['url'] = prefix+'load_data/'+pr.attr('rel').replace("-locked", "")+'/?view=icon';
                        }
                    }
                } 
            } else {
                cm_var['metadata_details']['html'] = '<p>'+selected.children().eq(1).text()+'</p>';
                cm_var['content_details']['empty'] = true;
            }
        }

        if (cm_var.metadata_details.iframe!==null || cm_var.metadata_details.html!==null) {
            if (cm_var.metadata_details.iframe!==null) {
                loadMetadataPanel(cm_var.metadata_details.iframe)
            } 
            if (cm_var.metadata_details.html!==null) {
                loadMetadataPanel(null, cm_var.metadata_details.html);
            }
        }
        
        if (cm_var.content_details.rel!==null && cm_var.content_details.url!==null){
            $("div#content_details").html('<p>Loading data... please wait <img src ="../../static/common/image/spinner.gif"/></p>');
            $("div#content_details").attr('rel', cm_var.content_details.rel);
            $("div#content_details").load(cm_var.content_details.url, function() {
                syncPanels(selected);
            });
        } else if (cm_var.content_details.empty){
            $("div#content_details").empty();
            $("div#content_details").removeAttr('rel');
        }
    } else {
        $("div#content_details").empty();
        $("div#content_details").removeAttr('rel');
        loadMetadataPanel(null,'<p></p>');
    }
};

var syncPanels = function(get_selected) {
    var toSelect = new Array();
    get_selected.each(function(i) {
        var _this = $(this)
        if ($.inArray(_this.attr("rel").replace("-locked", ""), ["image"]) >= 0) toSelect[i]=_this.attr("id").split("-")[1];
    });
    $(".ui-selectee", $("ul.ui-selectable")).each(function(){
        var selectee = $(this);
        if ($.inArray(selectee.attr('id'), toSelect) != -1) {
            if(!selectee.hasClass('ui-selected')) {
                selectee.addClass('ui-selected');
            }
        } else {
            selectee.removeClass('ui-selected');
        }
    });
}

var calculateCartTotal = function(total){
    $('#cartTotal').html(total); 
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
        var query = prefix+"metadata_details/multiaction/annotatemany/?"+productListQuery.join("&")
        if (index != null && index > -1) {
            query += "&index="+index;
        }
        loadMetadataPanel(query);
    } else {
        alert ("Please select at least one element."); 
    }
};

var loadMetadataPanel = function(src, html) {
    var iframe = $("div#metadata_details").find('iframe');
    var description = $("div#metadata_description");

    $("#right_panel").show();
    $("#swapMeta").html('<img tabindex="0" src="../../static/common/image/spacer.gif" class="collapsed-right" id="lhid_trayhandle_icon_right">');

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
        $("div#metadata_details").html('<iframe width="370" height="'+(h+31)+'" src="'+src+'" id="metadata_details" name="metadata_details" frameborder="0"></iframe>');
        $('iframe#metadata_details').load();
    }
};

var refreshCenterPanel = function() {
    var rel = $("div#content_details").attr("rel");
    var page = parseInt($("div#content_details").find("#page").attr("rel"));
    
    if (typeof rel!=="undefined") {
        if (rel.indexOf("orphaned")>=0) {
            $("div#content_details").html('<p>Loading data... please wait <img src ="../../static/common/image/spinner.gif"/></p>');
            url = '/webclient/load_data/'+rel.split('-')[0]+'/';
        } else if (rel.indexOf("share")>=0) {
            $("div#content_details").html('<p>Loading data... please wait <img src ="../../static/common/image/spinner.gif"/></p>');
            url = '/webclient/load_public/'+rel.split('-')[1]+'/';
        } else if(rel.indexOf('tag')>=0) {
            $("div#content_details").html('<p>Loading data... please wait <img src="../../static/common/image/spinner.gif"/></p>');
            url = '/webclient/load_tags/tag/'+rel.split('-')[1]+'/';
        } else {
            $("div#content_details").html('<p>Loading data... please wait <img src ="../../static/common/image/spinner.gif"/></p>');
            url = '/webclient/load_data/'+rel.replace('-', '/')+'/';
        }
        
        var view = $("div#content_details").find("#toolbar").attr('rel') ? $("div#content_details").find("#toolbar").attr('rel') : "icon";
        
        $("div#content_details").html('<p>Loading data... please wait <img src ="../../static/common/image/spinner.gif"/></p>');
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
        $("div#content_details").html('<p>Loading data... please wait <img src="../../static/common/image/spinner.gif"/></p>');
        url = '/webclient/load_tags/tag/'+rel[1]+'/?view='+view;
    } else {
        $("div#content_details").html('<p>Loading data... please wait <img src="../../static/common/image/spinner.gif"/></p>');
        url = '/webclient/load_data/dataset/'+rel[1]+'/?view='+view;
    }
    if (page!=null && page > 0) {
        url = url+"&page="+page;
    }
    $("div#content_details").html('<p>Loading data... please wait <img src="../../static/common/image/spinner.gif"/></p>');
    $("div#content_details").load(url);
    return false;
};

function saveMetadata (image_id, metadata_type, metadata_value) {
    if (image_id == null) {
        alert("No image selected.")
    } else {
        $($('#id_'+metadata_type).parent()).append('<img src="../../static/common/image/spinner.gif"/>');
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
    $("div#content_details").html('<p>Loading data... please wait <img src="../../static/common/image/spinner.gif"/></p>');
    $("div#content_details").load('/webclient/load_data/'+rel[0]+'/'+rel[1]+'/?view='+view+'&page='+page, function() {
        $("#dataTree").jstree("refresh", $('#'+rel[0]+'-'+rel[1]));
        if(rel[0].indexOf("orphaned")<0) {
            src = '/webclient/metadata_details/'+rel[0]+'/'+rel[1]+'/';
            loadMetadata(src);
        }
    });
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
    $("#swapMeta").html('<img tabindex="0" src="../../static/common/image/spacer.gif" class="collapsed-right" id="lhid_trayhandle_icon_right">'); 
    $("div#metadata_details").html('<iframe width="370" height="'+(h+31)+'" src="'+src+'" id="metadata_details" name="metadata_details" frameborder="0"></iframe>');
    $('iframe#metadata_details').load();
}