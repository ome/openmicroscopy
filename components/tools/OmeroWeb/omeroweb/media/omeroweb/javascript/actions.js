function isCheckedById(name) { 
    var checked = $("input[name='"+name+"']:checked").length; 
    if (checked == 0) { return false; } else { return true; } 
}

var calculateCartTotal = function(total)
{
    $('#cartTotal').html(total); 
};

function loadMetadata(src) {
    var h = $(window).height()-200;
    $("#right_panel").show();
    $("#swapMeta").html('<img tabindex="0" src="/appmedia/omeroweb/images/tree/spacer.gif"" class="collapsed-right" id="lhid_trayhandle_icon_right">'); 
    $("div#metadata_details").html('<iframe width="370" height="'+(h+31)+'" src="'+src+'" id="metadata_details" name="metadata_details"></iframe>');
    $('iframe#metadata_details').load();
}

function manyToAnnotation(){
    if (!isCheckedById("image") && !isCheckedById("dataset") && !isCheckedById("project") && !isCheckedById("well") && !isCheckedById("plate") && !isCheckedById("screen")) {
        alert ("Please select at least one image. Currently you cannot add other objects to basket."); 
    } else { 
        var productListQuery = "/webclient/metadata_details/multiaction/annotatemany/?";
        $("input[type='checkbox']:checked").each(function() {
            if(this.checked) {
                productListQuery += "&"+this.name+"="+this.id;
            }
        });
        
        loadMetadata(productListQuery);
    }    
}

function manyAddToBasket() {     
    if (!isCheckedById("image")) {//&& !isCheckedById("dataset") && !isCheckedById("plate")) {
        alert ("Please select at least one image. Currently you cannot add other objects to basket."); 
    } else { 
        manyToBasket($("input[type='checkbox']:checked"));
    }
};

function toBasket (productType, productId) {
    if (productId == null) {
        alert("No object selected.")
    } else {
        $.ajax({
            type: "POST",
            url: "/webclient/basket/update/", //this.href,
            data: "action=add&productId="+productId+"&productType="+productType,
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
    }
};

function manyToBasket (productArray) { 
    if(productArray.length==1) {
        toBasket(productArray[0].name, productArray[0].id);
    } else {
        var productListQuery = "action=addmany";
        productArray.each(function() {
            if(this.checked) {
                productListQuery += "&"+this.name+"="+this.id;
            }
        });

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
    }
};

function manyRemoveFromBasket() {     
    if (!isCheckedById("image")) {//&& !isCheckedById("dataset") && !isCheckedById("plate")) {
        alert ("Please select at least one image. Currently you cannot add other objects to basket."); 
    } else { 
        manyFromBasket($("input[type='checkbox']:checked"));
    }
};

function manyFromBasket(productArray) {
    var productListQuery = "action=delmany";
    productArray.each(function() {
        if(this.checked) {
            productListQuery += "&"+this.name+"="+this.id;
        }
    });
    
    $.ajax({
        type: "POST",
        url: "/webclient/basket/update/", //this.href,
        data: productListQuery,
        contentType:'html',
        cache:false,
        success: function(responce){
            if(responce.match(/(Error: ([A-z]+))/gi)) {
                alert(responce)
            } else {
                window.location = "/webclient/basket/";
            }
        },
        error: function(responce) {
            alert("Internal server error. Cannot remove from basket.")
        }
    });
}

function manyUnlink(parent) { 
    if (!isCheckedById("dataset") && !isCheckedById("image") && !isCheckedById("plate")) {
        alert ("Please select at least one object"); 
    } else { 
        unlink($("input[type='checkbox']:checked"), parent);
    }
};

function selectAll() {
    $("INPUT[type='checkbox']").attr('checked', $('#checkAllAuto').is(':checked'));   
}

function unlink (productArray, parent) { 
    var productListQuery = "parent="+parent;
    productArray.each(function() {
        if(this.checked) {
            productListQuery += "&"+this.name+"="+this.id;
        }
    });
    $.ajax({
        type: "POST",
        url: "/webclient/action/removemany/", //this.href,
        data: productListQuery,
        contentType:'html',
        success: function(responce){
            if(responce.match(/(Error: ([A-z]+))/gi)) {
                alert(responce)
            } else {
                window.location.replace("");
            }
        },
        error: function(responce) {
            alert("Internal server error. Cannot add to basket.")
        }
    });
};

function manyDelete() { 
    if (confirm('Delete selected objects?')) {
        if (!isCheckedById("image") && !isCheckedById("plate")) {
            alert ("Please select at least one object"); 
        } else { 
            deleteItems($("input[type='checkbox']:checked"), parent);
        }
    }
};

function deleteItems (productArray, parent) { 
    var productListQuery = "parent="+parent;
    productArray.each(function() {
        if(this.checked) {
            productListQuery += "&"+this.name+"="+this.id;
        }
    });
    if (confirm('Also delete annotations?')) {
        productListQuery += '&anns=on';
    } 
    $.ajax({
        type: "POST",
        url: "/webclient/action/deletemany/", //this.href,
        data: productListQuery,
        contentType:'html',
        success: function(responce){
            if(responce.match(/(Error: ([A-z]+))/gi)) {
                alert(responce)
            } else {
                //window.location.replace("");
                var i = setInterval(function (){
                    $.getJSON("/webclient/progress/", function(data) {
                        if (data.inprogress== 0) {
                            clearInterval(i);
                            $("#progress").hide();
                            if(data.failure>0) {
                                $("#jobstatus").html('<a href="/webclient/status/" class="align_left">STATUS:</a>' + data.failure + ' job(s) failed');
                            } else {
                                $("#jobstatus").html('');
                            }
                            return;
                        }

                        $("#progress").show();
                        $("#jobstatus").html('<a href="/webclient/status/" class="align_left">STATUS:</a>' + data.jobs + ' job(s)');
                    });
                }, 1000);
                productArray.each(function() {
                    if(this.checked) {
                        $('li#'+this.id).remove();
                        a = simpleTreeCollection.find('li#img-'+this.id);
                        a.prev('li.line').remove();
                        a.remove();
                    }
                });
                
            }
        },
        error: function(responce) {
            alert("Internal server error. Cannot delete objects.");
        }
    });
};

function deleteItem(productType, productId) {
    if ((productType == 'project' || productType == 'dataset' || productType == 'image' || productType == 'screen' || productType == 'plate' || productType == 'share') && productId > 0){
        if (confirm('Delete '+productType+'?')) {
            var productListQuery = null;
            if ((productType == 'project' || productType == 'dataset' || productType == 'screen') && confirm('Also delete content?')) {
                productListQuery += 'child=on';
            }
            if (confirm('Also delete annotations?')) {
                if(productListQuery!=null){
                    productListQuery = 'anns=on';
                } else {
                    productListQuery += '&anns=on';
                } 
            }
            $.ajax({
                type: "POST",
                url: "/webclient/action/delete/"+productType+"/"+productId+"/", //this.href,
                data: productListQuery,
                contentType:'html',
                success: function(responce){
                    if(responce.match(/(Error: ([A-z]+))/gi)) {
                        alert(responce)
                    } else {  
                        //window.location.replace("");
                        if ((productType == 'image') && productId > 0) {
                            $("div#metadata_details").empty();
                        } else if ((productType == 'dataset' || productType == 'plate') && productId > 0) {
                            $("div#metadata_details").empty();
                            $("div#content_details").removeAttr('rel').children().remove();
                        } else if ((productType == 'project' || productType == 'screen') && productId > 0) {
                            $("div#metadata_details").empty();
                        }

                        a = simpleTreeCollection.find('span.active').parents('li:first');
                        if (a.attr('class').indexOf('last')>=0) {  
                            a.prev().prev().attr('class', a.prev().prev().attr('class')+'-last');
                        }
                        if ((productType == 'image') && productId > 0) {
                            if ($('#dataIcons').length != 0) {
                                $('#dataIcons').find('li#'+a.attr('id').split("-")[1]).remove();
                            } else if ($('#dataTable').length != 0) {
                                $('#dataTable').find('tr#'+a.attr('id').split("-")[1]).remove();
                            }
                        }
                        a.prev('li.line').remove();
                        a.remove();
                                          
                        var i = setInterval(function (){
                            $.getJSON("/webclient/progress/", function(data) {
                                if (data.inprogress== 0) {
                                    clearInterval(i);
                                    $("#progress").hide();
                                    if(data.failure>0) {
                                        $("#jobstatus").html('<a href="/webclient/status/" class="align_left">STATUS:</a>' + data.failure + ' job(s) failed');
                                    } else {
                                        $("#jobstatus").html('');
                                    }
                                    return;
                                }

                                $("#progress").show();
                                $("#jobstatus").html('<a href="/webclient/status/" class="align_left">STATUS:</a>' + data.jobs + ' job(s)');
                            });
                        }, 1000);
                    }
                },
                error: function(responce) {
                    alert("Internal server error. Cannot delete object.");
                }
            });
            
        }
    } 
}

function manyCopyToClipboard() { 
    if (isCheckedById("project") || isCheckedById("screen")) {
        alert ("You can only copy datasets, images or plates. Please uncheck projects and screens."); 
    } else if (!isCheckedById("dataset") && !isCheckedById("plate") && !isCheckedById("image")) {
        alert ("Please select at least one dataset, image or plate."); 
    } else if (isCheckedById("dataset") && isCheckedById("plate")) {
        alert ("Please select only datasets, images or plates."); 
    } else { 
        copyToClipboard($("input[type='checkbox']:checked"));
    }
};

function copyToClipboard (productArray) {
    var productListQuery = "action=copy";
    if (productArray.length > 0 ) {
        productArray.each(function() {
            if(this.checked) {
                productListQuery += "&"+this.name+"="+this.id;
            }
        });
    } else {
        productListQuery += "&"+productArray.name+"="+productArray.id;
    }
    $.ajax({
        type: "POST",
        url: "/webclient/clipboard/", //this.href,
        data: productListQuery,
        contentType:'html',
        success: function(responce) {
            if(responce.match(/(Error: ([A-z]+))/gi)) {
                alert(responce)
            } else {
                alert(responce)
            }
        },
        error: function(responce) {
            alert("Internal server error. Cannot copy to clipboard.")
        }
    });
};

function treeCopyToClipboard(productType, productId) {
    if (productId == null) {
        alert("No object selected.");
    } else {
        input = $('<input type="checkbox" checked/>').attr('name', productType).attr('id', productId).attr('class', 'hide');
        copyToClipboard(input);
    }
};

function pasteFromClipboard (destinationType, destinationId, url) {
    if (destinationId == null) {
        alert("No object selected.")
    } else {
        $.ajax({
            type: "POST",
            url: "/webclient/clipboard/", //this.href,
            data: "action=paste&destinationId="+destinationId+"&destinationType="+destinationType,
            contentType:'html',
            success: function(responce){
                if(responce.match(/(Error: ([A-z]+))/gi)) {
                    alert(responce)
                } else {
                    window.location = url
                }
            },
            error: function(responce) {
                alert("Internal server error. Cannot paste from clipboard.")
            }
        });
    }
};


function cleanClipboard (productType, productId) {
    if (productId == null) {
        alert("No object selected.")
    } else {
        $.ajax({
            type: "POST",
            url: "/webclient/clipboard/", //this.href,
            data: "action=clean",
            contentType:'html',
            success: function(responce){
                alert(responce);
            },
            error: function(responce) {
                alert("Internal server error. Cannot clean clipboard.")
            }
        });
    }
};

function changeView(view) { 
    var rel = $("div#content_details").attr('rel').split("-");
    if(rel.indexOf('orphaned')>=0) {
        $("div#content_details").html('<p>Loading data... please wait <img src="/appmedia/omeroweb/images/tree/spinner.gif"/></p>');
        $("div#content_details").load('/webclient/load_data/orphaned/?view='+view);
    } else if(rel.indexOf('tag')>=0) {
        $("div#content_details").html('<p>Loading data... please wait <img src="/appmedia/omeroweb/images/tree/spinner.gif"/></p>');
        $("div#content_details").load('/webclient/load_tags/tag/'+rel[1]+'/?view='+view);
    } else {
        $("div#content_details").html('<p>Loading data... please wait <img src="/appmedia/omeroweb/images/tree/spinner.gif"/></p>');
        $("div#content_details").load('/webclient/load_data/dataset/'+rel[1]+'/?view='+view);
    }
    return false;
};

function openPopup(url) {
    owindow = window.open(url, 'anew', config='height=600,width=850,left=50,top=50,toolbar=no,menubar=no,scrollbars=yes,resizable=yes,location=no,directories=no,status=no');
    if(!owindow.closed) owindow.focus();
    return false;
}


function saveMetadata (image_id, metadata_type, metadata_value) {
    if (image_id == null) {
        alert("No image selected.")
    } else {
        $($('#id_'+metadata_type).parent()).append('<img src="/appmedia/omeroweb/images/tree/spinner.gif"/>');
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

function editItem(type, item_id) {
    src = '/webclient/action/edit/'+type+'/'+item_id+'/';
    loadMetadata(src);
    return false;
}

function doPagination(view, page) {
    var rel = $("div#content_details").attr('rel').split("-");
    $("div#content_details").html('<p>Loading data... please wait <img src="/appmedia/omeroweb/images/tree/spinner.gif"/></p>');
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
