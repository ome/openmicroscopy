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
                calculateCartTotal(responce);
            },
            error: function(responce) {
                alert("This object is already in the basket")
            }
        });
        
        var calculateCartTotal = function(total)
        {
            $('#cartTotal').html(total);
        };
    }
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
                calculateCartTotal(responce);
                var rm = "#"+productType+"-"+productId
                $(rm).remove()
            },
            error: function(responce) {
                alert("Cannot remove this object")
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
