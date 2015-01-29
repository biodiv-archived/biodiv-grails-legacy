function changeDisplayOrder(url, instanceId, typeOfChange, type, parentInsId){
    $.ajax({
        url : url,
        datatype : 'json',
        data: {'instanceId' : instanceId, 'typeOfChange': typeOfChange, 'parentInsId': parentInsId},
        success: function(data) {
            if(data.success){
                var a = $('#'+type+'_'+instanceId);
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

