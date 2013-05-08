$(document).ready(function(){
    $.fn.raty.defaults.path = window.params.imagesPath 
    $.fn.raty.defaults.scoreName = 'rating'
    $.fn.raty.defaults.cancel = false
});

function rateCallback(ele, successHandler) {
    var form = ele.parent();
            if(form.prop("tagName") == 'FORM') {
                form.ajaxSubmit({ 
                    type: 'GET',
                    dataType:'json',
                    success: function(data, statusText, xhr, form) {
                        console.log('success'+data.avg);
                        updateRating(data.avg, data.noOfRatings, ele)
                        if(successHandler) {
                            console.log(successHandler)
                            successHandler(data.avg, data.noOfRatings, rateFn, form);
                        }
                    }, error:function (xhr, ajaxOptions, thrownError){
                            handleError(xhr, ajaxOptions, thrownError, this.success, function() {
                                var response = $.parseJSON(xhr.responseText);
                                if(response.error){
                                }
                            }, function(){
                                $(ele).raty('reload');
                                //refreshRating (ele);
                            });
                    } 
                });  
            }
}

function like(ele, successHandler) {
    return ele.raty({
        number: 1,
        halfShow:false, 
        score: function() {
            return $(this).attr('data-score');
        },
        click: function(score, evt) {
            rateCallback($(evt.target).parent(), successHandler);
        },
        hints: ['Like'],
        starOff  : 'like-icon.png',
        starOn   : 'liked-icon.png'
    });
}

function rate(ele, successHandler) {
    return rateFn = ele.raty({
        number: 5,
        halfShow:true, 
        score: function() {
            return $(this).attr('data-score');
        },
        click: function(score, evt) {
            rateCallback($(evt.target).parent(), successHandler);
        },
        hints: ['Bad', 'Poor', 'Ok', 'Good', 'Best'],
        halfShow:true,
        starHalf : 'star-half.png',
        starOff  : 'star-off.png',
        starOn   : 'star-on.png'
    });
}

function galleryImageLoadFinish() {
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

        refreshRating($(".star_gallery_rating"));
 	$(".ellipsis.multiline").trunk8({
		lines:2,		
	});

}

function refreshRating($container) {
        var form = $container.parent()
        var action = form.attr('action');

        $.ajax ({
            url:action.replace('/rating/rate', '/rating/fetchRate'),
            success: function(data, statusText, xhr) {
                var imgRating = rate($container);            
                updateRating(data.avg, data.noOfRatings, $container)
            }, error:function (xhr, ajaxOptions, thrownError){
                    handleError(xhr, ajaxOptions, thrownError, this.success, function() {
                        var response = $.parseJSON(xhr.responseText);
                        if(response.error){
                        }
                    });
            } 
        });
}

function updateRating (avgrate, noofratings, imgRating, ratingContainer) {
    console.log(imgRating);
    console.log('updating rating'+avgrate);
    imgRating.raty('score', avgrate);
    console.log(imgRating.raty('score'))
    if(!ratingContainer) 
        ratingContainer = imgRating.parent();
    if(ratingContainer.find('.like_rating').length != 0)
        ratingContainer.find(".noOfRatings").html(noofratings);
    else
        ratingContainer.find(".noOfRatings").html('('+noofratings+' rating'+(noofratings!=1?'s':'')+')');
}

