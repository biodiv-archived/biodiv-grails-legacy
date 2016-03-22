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

$(document).ready(function(){

    $('.newsl_parent').click(function(){ 
        if($(this).is(':checked')){ 
            $('.newsl_subp_selection').hide(); 
            $('.newsl_subparent').attr('disabled',true); 
            $('.inp_parentId').val(0);
        }else{            
            $('.newsl_subparent').attr('disabled',false);
        } 
    });


    $('.newsl_subparent').click(function(){
        if($(this).is(':checked')){ 
            $('.newsl_subp_selection').show(); 
            $('.newsl_parent').attr('disabled',true);
            $('.inp_parentId').val($('.newsl_subp_selection').val()); 
        }else{
            $('.inp_parentId').val(0);      
            $('.newsl_subp_selection').hide(); 
            $('.newsl_parent').attr('disabled',false);
        } 
    });

    $('.newsl_subp_selection').change(function(){
        $('.inp_parentId').val($(this).val()); 
    });
});