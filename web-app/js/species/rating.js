$(document).ready(function(){
    if(window.params)
        $.fn.raty.defaults.path = window.params.imagesPath 
    $.fn.raty.defaults.cancel = false
    $.fn.raty.defaults.space = false
});

function rateCallback(ele, successHandler) {
    var form = ele.parent();
    var action = (ele.data('action') == 'unlike')?'unrate':'rate'
    var url = "/rating/"+action+'/'+ele.data('id');
    if(form.prop("tagName") == 'FORM') {
        form.ajaxSubmit({
            url: url,
            type: 'GET',
            dataType:'json',
            data:{type:ele.data('type')},
            success: function(data, statusText, xhr, form) {
                updateRating(data.avg, data.noOfRatings, ele)
                if(successHandler) {
                    successHandler(data.avg, data.noOfRatings, rateFn, form);
                }
                if(ele.data('action') === 'unlike') {
                    ele.data('action', 'like');
                    ele.attr('title', 'Like');
                    ele.raty('cancel', true);
                } else if(ele.data('action') === 'like') {
                    ele.data('action', 'unlike');
                    ele.attr('title', 'Unlike');
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
var hintLike = $(ele).attr('title');
    return ele.raty({
        number: 1,
        halfShow:false, 
        scoreName:'rating',
        score: function() {
            return $(this).attr('data-score');
        },
        click: function(score, evt) {
            if(score != null) {
                rateCallback($(evt.target).parent(), successHandler);
            } else {
                //cancel rating
            }
        },
        hints: [hintLike],

        starOff  : 'like-icon.png',
        starOn   : 'liked-icon.png'
    });
}

function rate(ele, successHandler, inputName) {
    if(!inputName) {
        inputName = ele.attr('data-input-name');
        if (typeof inputName == 'undefined' || inputName == false) {
            inputName = 'rating';
        }
    }
    return rateFn = ele.raty({
        number: 5,
        halfShow:true, 
        scoreName:inputName,
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
                galleriaInfo.css('cssText', 'top : 350px !important');
                galleriaInfo.css({'height' : '50px'});
                $(this).addClass('close icon-chevron-up').removeClass('icon-chevron-down open');
            } else {
                if($(this).attr('rel') === undefined){
                    rel_height = galleriaInfo.outerHeight()+8;
                    $(this).attr('rel' , rel_height);
                }
                var height = ($(this).attr('rel') != '') ? $(this).attr('rel') : galleriaInfo.outerHeight();
                galleriaInfo.css('cssText', 'top : '+(top - height+50)+'px !important');
                galleriaInfo.css({'height' : height});
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
        if(form.length == 0) return;
        var url = "/rating/fetchRate/"+$container.data('id');

        $.ajax ({
            url:url,
            data:{type:$container.data('type')},
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
    imgRating.raty('score', avgrate);
    if(!ratingContainer) 
        ratingContainer = imgRating.parent();
    if(ratingContainer.find('.like_rating').length != 0)
        ratingContainer.find(".noOfRatings").html(noofratings);
    else
        ratingContainer.find(".noOfRatings").html('('+noofratings+' rating'+(noofratings!=1?'s':'')+')');
}

