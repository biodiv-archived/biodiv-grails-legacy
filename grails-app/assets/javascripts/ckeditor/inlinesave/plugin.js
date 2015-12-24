CKEDITOR.plugins.add( 'inlinesave',{
    init: function( editor ) {
        editor.addCommand( 'inlinesave', {
            exec : function( editor ) {
                addData();
                function addData() {
                    var data = editor.getData();
                    var params = $($(editor.element)[0].$).data();
                    var url = $($(editor.element)[0].$).attr('data-url');
                    delete params['url'];
                    console.log(url);
                    console.log($($(editor.element)[0].$).data());
                    params['value'] = data;
                    jQuery.ajax({
                        type: "POST",
                        url: url,
                        data:  params
                    })
                    .done(function (data, textStatus, jqXHR) {
                        if(data.success == true) {
                            editor.container.removeClass('alert alert-error');
                            $($(editor.element)[0].$).next('.alert.alert-error').remove();
                            if(params.name == 'description') {
                                $($(editor.element)[0].$).nextAll('.description').first().html(editor.getData());
                                editor.container.hide();
                            } else if (params.name == 'newdescription'){
                                console.log(data);
                                onAddableDisplay(data,data, jqXHR,$($(editor.element)[0].$));
                            }
                        } else {
                            editor.container.addClass('alert alert-error');
                            $($(editor.element)[0].$).after('<div class="alert alert-error">'+data.msg+'</div>');
                        }
                    })
                    .fail(function (jqXHR, textStatus, errorThrown) {
                        alert("Error saving content. [" + jqXHR.responseText + "]");
                    });
                }//addData
            }
        });
        /*editor.ui.addButton( 'Inlinesave', {
            label: 'Save',
            command: 'inlinesave',
            icon: this.path + 'images/inlinesave.png'
        } );*/
    }
});
