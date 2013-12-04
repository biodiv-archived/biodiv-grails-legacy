$(function() {
    
    var map_height = $(window).height()-80;
    $("#map").css('height', map_height + 'px');
    $("#parent_panel").css('height', map_height + 'px');

    $(window).resize(function() {
    	var map_height = $(window).height()-80;
    	$("#map").css('height', map_height + 'px');
    	$("#parent_panel").css('height', map_height + 'px');
    });

    $( "#panel_show_bttn" ).click(function() {
            $("#panel_show_bttn").css('display', 'none');
            $("#panel").show( "slide", {}, 500, function(){
	       var map = window.map;
               map.updateSize();
	    });
            $("#panel_hide_bttn").css('display', 'block');

            return false;
    });

    $( "#panel_hide_bttn" ).click(function() {
            $("#panel_hide_bttn").css('display', 'none');
            $("#panel").hide( "slide", {}, 500 , function() {
	       var map = window.map;
               map.updateSize();
	    });
            $("#panel_show_bttn").css('display', 'block');
            return false;
    });
    
    $("#explore_bttn").click(function() {
	$(".side_panel").hide();
        $("#layers_list_panel").fadeIn(500);
        return false;
    });

    $("#search_bttn").click(function() {
	$(".side_panel").hide();
        $("#search_panel").fadeIn(500);    
        var last_query = eatCookie("last_search_query");

        if (last_query !== null && last_query !== ''){
            search(last_query, "search_results_panel");
        }

        return false;
    });

    $("#share_bttn").click(function() {
        updateSharePanel('share_panel');
	$(".side_panel").hide();
        $("#share_panel").fadeIn(500);    
        return false;
    });

    $("#selected_layers_bttn").click(function() {
        updateSelectedLayersPanel('selected_layers_panel');
	$(".side_panel").hide();
        $("#selected_layers_panel").fadeIn(500);    
        return false;
    });

    $("#selected_features_bttn").click(function() {
	$(".side_panel").hide();
        $("#feature_info_panel").fadeIn(500);    
        return false;
    });


});

