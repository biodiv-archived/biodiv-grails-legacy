(function ($) {
    jQuery.noConflict();

    // Known image formats
    var image_formats = new Array("bmp", "gif", "jpg", "jpeg", "jpe", "png", "tif", "tiff");

    var is_image_format = function() {
        var format = $("#outputFormat").val();
        for (i in image_formats) {
            if (image_formats[i] === format) return true;
        }
        return false;
    }
    
    var auto_update = function(e) {
        if ($("#autoUpdate:checked").length >0) {
            update_preferences(e);
        }
    }

    var update_preferences = function(e) {
        $("#preferencesForm").submit();
    }
    
    var submit_async = function(e) {
        if (e) {
            $("#spinner").css({position:'fixed', left:e.pageX, top:e.pageY, 'z-index':10000});
        } else {
            $("#spinner").css({position:'fixed', left:20, top:20, 'z-index':10000});
        }
        $("#spinner").show();
        // 
        $.ajax({
            type:"POST",
               url: "image",
               data: $("#preferencesForm").serialize(),
               success: function(msg){
                var old_img = $(".model-img");
                var new_img = $(msg).appendTo("#image").find(".model-img");
                new_img.hide();
                new_img.load(function() {
                    old_img.remove();
                    new_img.fadeIn(500);
                    $("#hideMenu").attr("href",new_img.attr("src")); // update links too
                });
               },
            error: function(msg) {
                $("#spinner").hide();
                // alert("Something went wrong during class diagram generation. Please try again.") // We get it on 'Enter' on image formats
            },
               complete: function(msg){
                $("#spinner").hide();
            }
         });
    }
    // cant find this one in jquery doc?
    var toggle_enable = function(selector, checked) {
        if (checked) {
            $(selector+" input").removeAttr("disabled");
            $(selector).fadeTo(500, 1);
        } else {
            $(selector+" input").attr("disabled","disabled");
            $(selector).fadeTo(500, 0.6);
        }
    }
    
    var init_menu = function() {
        $("#menuItem_showPreferences").click(function() {
            $("#preferences").dialog('open');
            return false;
        });
        $("#menuItem_showLegend").click(function() {
            show_legend();
            return false;
        });
        $("#menuItem_hideMenu").click(function() {
            window.location=$(".model-img").attr("src");
            return false;
        });
    }

    var init_legend = function() {
        $("#legend").dialog({
            show: 'blind',
            hide: 'blind',
            position: ['right','top'],
            width: 700,
            autoOpen: false,
            buttons: {
                "Close" : function() {
                    $(this).dialog('close');
                }
            }
        });
    }
    
    var show_legend = function() {
        // Lazy initialize legend content. Copy img href to src 
        var legendImg = $("#legend .legend-img");
        if (legendImg.attr('src') == "") {
            legendImg.attr('src',legendImg.attr('href'));
        }
        $("#legend").dialog('open');
    }
        
    var init_preferences_dialog = function() {
        
        $("#preferences").dialog({
            show: 'blind',
            hide: 'blind',
            position: 'top',
            width: 350,
            autoOpen: false,
            buttons: {
                "Update": function(e) {
                    update_preferences(e);
                },
                "Close" : function() {
                    $(this).dialog('close');
                }
            }
        });
        
        $("#preferencesForm").submit(function(e) {
            if (is_image_format()) {
                // Show image format inside img tag as ajax request
                submit_async();
                return false;
            } else {
                // Show non-image format as root content (ie. not part of html)
                return true;
            }
        });

        $("#preferencesForm :input").change(function(e) {
            auto_update(e);
        });

        $("#showProperties").change(function(){
            toggle_enable("#showPropertiesSelected", this.checked);
        });

        $("#showMethods").change(function(){
            toggle_enable("#showMethodsSelected", this.checked);
        });

        $("#fontsize_slider").slider({
            max:30, 
            min:4,
            value: $("#fontsize").val(),
            animate: true,
            change: function(event, ui) {
                $("#fontsize").val(ui.value);
                auto_update(event);  // Dont know why needed - #fontsize is an :input field (but hidden, though)
            }
        });
        
        $("#randomize-class-order").click(function(e) {
            $("#randomizeOrder").val(true);
            update_preferences(e);
            $("#randomizeOrder").val(false);
        });
    }
    
    return function() {
        $(document).ready(function($) {
            init_menu();
            init_preferences_dialog();
            init_legend();
            if (is_image_format()) {
                update_preferences();
            }
        });

    };
    
}(jQuery))();



