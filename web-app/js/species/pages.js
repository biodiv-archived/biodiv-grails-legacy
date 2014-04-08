function changeDisplayOrder(url, pageId, typeOfChange){
    console.log("clicked");
    $.ajax({
        url : url,
        datatype : 'json',
        data: {'pageId' : pageId, 'typeOfChange': typeOfChange},
        success: function(data) {
            if(data.success){
                var a = $('#newsletter_'+pageId);
                //var toBeMovedLi = $("#newsletter_"+pageId);
                if(typeOfChange == "up"){
                    var b = $(a).prev()
                    $(a).insertBefore($(b))
                    //$(toBeMovedLi).parent().insertBefore( $(toBeMovedLi).parent().prev() );
                }
                else{
                    var b = $(a).next()
                    $(a).insertAfter($(b));
                    //$(toBeMovedLi).parent().insertAfter( $(toBeMovedLi).parent().next() );
                }
                
            }
            else{

            }
        },
        error:function (xhr, ajaxOptions, thrownError){
            return false;
        } 
    });
}

