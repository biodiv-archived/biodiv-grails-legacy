var langComboBoxArray = new Array($('.languageComboBox').size());

function doCustomization(langCombo){
    var inputTextEle = langCombo.data('combobox').$element
    inputTextEle.unbind('blur');
    inputTextEle.attr('name', 'languageName');
    inputTextEle.attr('autocomplete', 'off');
    inputTextEle.on('blur', $.proxy(myBlur, langCombo.data('combobox')));
}

function myBlur(e){
    var oldVal = this.$element.val();
    this.blur(e);
    this.$element.val(oldVal);
}

function initializeLanguage(that){
    that = that || $('.languageComboBox');
    that.each( function( index ) {
        langComboBoxArray[index] = $(this);
        $(this).combobox();
    });
    $.each(langComboBoxArray, function(index,value){
        var langCombo = value;        
        var is_Exist= langCombo.parent().parent();
        var is_ExistLn = langCombo.parent().parent().find('input[type="text"][name="languageName"]');
        if(is_ExistLn.size() > 0){ 
            is_ExistLn.next().remove();
            is_ExistLn.remove();
        }
        doCustomization(langCombo);
        var defaultLang = $(value).data("defaultlanguage");
        langCombo.val(defaultLang).attr("selected",true);
        langCombo.data('combobox').refresh();
    });
}


function updateCommonNameLanguage(that){
    var langCombo = that || $(".languageComboBox");
    var langComboVal = langCombo.val();
    if(langComboVal != null){
        langComboVal = langComboVal.toLowerCase()
    }

    var inputVal = $.trim(langCombo.data('combobox').$element.val());
    if(inputVal.toLowerCase() !== langComboVal){
        langCombo.append($('<option></option>').val(inputVal).html(inputVal));
        langCombo.data('combobox').refresh();
    }
}
