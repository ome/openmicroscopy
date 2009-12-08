var calculateCartTotal = function(total)
{
    $('#cartTotal').html(total);
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
                if(responce.match(/(Error: ([a-z][A-Z]+))/gi)) {
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
            if(responce.match(/(Error: ([a-z][A-Z]+))/gi)) {
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

function fromBasket (productType, productId) {
    if (productId == null) {
        alert("No object selected.")
    } else {
        $.ajax({
            type: "POST",
            url: "/webclient/basket/update/", //this.href,
            data: "action=del&productId="+productId+"&productType="+productType,
            contentType:'html',
            cache:false,
            success: function(responce){
                if(responce.match(/(Error: ([a-z][A-Z]+))/gi)) {
                    alert(responce)
                } else {
                    calculateCartTotal(responce);
                    var rm = "#"+productType+"-"+productId
                    $(rm).remove()
                }
            },
            error: function(responce) {
                alert("Internal server error. Cannot remove from basket.")
            }
        });
        
        var calculateCartTotal = function(total)
        {
            if (total == 0) {
                window.location = "/webclient/basket/";
            } else {
                $('#cartTotal').html(total);
            }
        };
    }
}


