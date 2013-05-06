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
                        successHandler(data.avg, data.noOfRatings, form);
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
        $.ajax ({
            url:action,
            success: function(data, statusText, xhr) {
                $(".galleria-info-description").find("input[value='"+Math.round(data.avg)+"']").attr("checked", true);
                var r = rate($(".galleria-info-description"), function(avgrate, noofratings, ratingcontainer){
                    r.select(avgrate);
                    ratingcontainer.find(".noOfRatings").html('('+noofratings+' ratings)');
                });
            }, error:function (xhr, ajaxOptions, thrownError){
                    handleError(xhr, ajaxOptions, thrownError, this.success, function() {
                        var response = $.parseJSON(xhr.responseText);
                        if(response.error){
                        }
                    });
            } 
        })

}
