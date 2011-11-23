$(document).ready(function() 
    {
        $('#swapTree').click(function() { 
            var flag_l = true;
            if($('img#lhid_trayhandle_icon_left').attr('class')=='collapsed-left') {
                flag_l = false
            }
            if (flag_l) {
                $("#left_panel").hide();
                $("#swapTree").children('img').removeClass("expanded-left").addClass("collapsed-left");
                flag_l = false;
            } else {
                $("#left_panel").show();
                $("#swapTree").children('img').removeClass("collapsed-left").addClass("expanded-left"); 
                flag_l = true;
            }
        });

        $('#swapMeta').click(function() { 
            var flag_r = true;
            if($('img#lhid_trayhandle_icon_right').attr('class')=='expanded-right') {
                flag_r = false
            }
            if (flag_r) {                        
                $("#right_panel").hide();
                $("#swapMeta").children('img').removeClass("collapsed-right").addClass("expanded-right"); 
                flag_r = false;                        
            } else {
                $("#right_panel").show();
                $("#swapMeta").children('img').removeClass("expanded-right").addClass("collapsed-right"); 
                flag_r = true; 
            }
        });
        
})