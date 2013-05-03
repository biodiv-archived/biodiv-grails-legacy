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
                    success: function(data, statusText, xhr, form) {
                        var arr = data.split(',');
                        successHandler(arr[0], arr[1]);
                    }, error:function (xhr, ajaxOptions, thrownError){
                            //successHandler is used when ajax login succedes
                            var successHandler = this.success, errorHandler;
                            handleError(xhr, ajaxOptions, thrownError, successHandler, function() {
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
