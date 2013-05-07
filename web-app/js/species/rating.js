function rate(ele, successHandler) {
    if(ele == undefined) {
        ele = $("body");
    }
   var rateFn = ele.find(".star").rating({
       required: 'hide',
        callback: function(value, link){
            var form = $(link).parent().parent().parent();
            if(form.prop("tagName") == 'FORM') {
                form.ajaxSubmit({ 
                    type: 'GET',
                    dataType:'json',
                    success: function(data, statusText, xhr, form) {
                        successHandler(data.avg, data.noOfRatings, rateFn, form);
                    }, error:function (xhr, ajaxOptions, thrownError){
                            handleError(xhr, ajaxOptions, thrownError, this.success, function() {
                                var response = $.parseJSON(xhr.responseText);
                                if(response.error){
                                }
                            });
                    } 
                });  
            }
        }
    });
   return rateFn
}

function galleryImageLoadFinish() {
        var form = $(".galleria-info-description").find('.ratingForm')
        var action = form.attr('action');
        $(".slideUp").bind('click', function() {
            var galleriaInfo = $(".galleria-info");
            var top = galleriaInfo.position().top
            if($(this).hasClass('open')) {
                galleriaInfo.css({'top': '350px'});
                $(this).addClass('close icon-chevron-up').removeClass('icon-chevron-down open');
            } else {
                var height = galleriaInfo.outerHeight();
                galleriaInfo.css({'top': (top - height+50)+'px'});
                $(this).addClass('open icon-chevron-down').removeClass('icon-chevron-up close');
           }
        });


        $.ajax ({
            url:action,
            success: function(data, statusText, xhr) {
                $(".galleria-info-description").find("input[value='"+Math.round(data.avg)+"']").attr("checked", true);
                var imgRating = rate($(".galleria-info-description"), updateRating);            
            }, error:function (xhr, ajaxOptions, thrownError){
                    handleError(xhr, ajaxOptions, thrownError, this.success, function() {
                        var response = $.parseJSON(xhr.responseText);
                        if(response.error){
                        }
                    });
            } 
        })        
 	$(".ellipsis.multiline").trunk8({
		lines:2,		
	});
}

function updateRating (avgrate, noofratings, imgRating, ratingContainer){
    imgRating.select(avgrate);
    if(ratingContainer.find(".like").length == 0)
        ratingContainer.find(".noOfRatings").html('('+noofratings+' rating'+(noofratings!=1?'s':'')+')');
    else
        ratingContainer.find(".noOfRatings").html('('+noofratings+' like'+(noofratings!=1?'s':'')+')');
}

