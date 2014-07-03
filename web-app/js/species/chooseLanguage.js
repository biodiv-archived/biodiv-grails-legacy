var langComboBoxArray = new Array($('.languageComboBox').size());

function doCustomization(langCombo){
    console.log("AGAIN");
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

function initializeLanguage(){
    $('.languageComboBox').each( function( index ) {
        langComboBoxArray[index] = $(this);
        $(this).combobox();
    });
    $.each(langComboBoxArray, function(index,value){
        console.log( index + ": " + value );
        var langCombo = value;
        doCustomization(langCombo);
        var defaultLang = $(value).data("defaultlanguage");
        langCombo.val(defaultLang).attr("selected",true);
        langCombo.data('combobox').refresh();
    });
}


function updateCommonNameLanguage(){
    var langCombo = $(".languageComboBox");
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
