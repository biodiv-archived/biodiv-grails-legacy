
function getCustomFields(){
    var result = [];
    var cPrefixClass = '.CustomField_';
    var fieldList = ['name', 'description','dataType', 'isMandatory', 'allowedMultiple' , 'options', 'defaultValue', 'allowedParticipation', 'supportingModule'];
    $("ul.customFieldList li").each( function (index){
        var thisli = $(this);
        var cfMap = {};
        $.each(fieldList, function(index, value){
            var key = cPrefixClass + value;
            var val = $(thisli).find(key).val();
            if(key == '.CustomField_isMandatory' || key == '.CustomField_allowedMultiple' ||  key == '.CustomField_allowedParticipation' ){
                var val = $(thisli).find(key).prop('checked');
            }
            cfMap[value] = val;
        });
        result.push(cfMap);
    });
    return  JSON.stringify(result);
}


$(document).ready(function(){
        function registerCustomFieldEvent(){
            $('input.cfRaidioButtonSelector').unbind('change').change( function(){
                    var lo = $(this).closest(".customField").siblings(".listOptions");
                    if($(this).val() == 'textbox') {
                        lo.hide();
                    }else{
                        lo.show();
                    }
            });

            $(".CustomField_dataType").unbind('change').change(function() {
                    var dType = $(this).val();
                    var fT = $(this).closest(".customField").siblings(".formType");
                    var lo = $(this).closest(".customField").siblings(".listOptions");
                    var multiList = fT.find('.multipleFromList');
                    var oneList = fT.find('.oneFromList');

                    if((dType == 'PARAGRAPH_TEXT') || (dType == 'DATE') ){
                        oneList.hide();
                        multiList.hide();
                        lo.hide();
                        fT.find('.defTextbox').find('input').prop("checked", true); 
                    }else if((dType == 'INTEGER') || (dType == 'DECIMAL')){
                        oneList.show();
                        multiList.hide();
                        fT.find('.defTextbox').find('input').prop('checked' , true);
                        lo.hide();
                    }else{
                        oneList.show();
                        multiList.show();
                    }
            });
        }

        $(document).on('click', ".addNewCustomField", function() {
            var p = new Object();
            p['radioGroupName']= 'cfradioGroup' + Math.floor((Math.random() * 1000) + 1);
            var html = $(this).prevAll(".newCustomField").render(p);
            $(this).before(html); 
            registerCustomFieldEvent();
            return false;
        });
});

