(function ($) {
    "use strict";

    var AbstractEditableType = function (div, options) { 
        this.init($(div), options);
    };

    $.fn.editableutils.inherit(AbstractEditableType, $.fn.components.abstracttype);

    $.extend(AbstractEditableType.prototype, {
        constructor: AbstractEditableType,
        
        initEditables : function(editSelector, addSelector) {
            this.editSelector = editSelector;
            this.addSelector = addSelector;
            this.options.editable = true;
            this.initButtons();
            this.$element.find('.addFieldButton').click($.proxy(this.onAdd, this));
            this.$element.find('.editFieldButton').click($.proxy(this.onEdit, this));
            this.$element.find('.deleteFieldButton').click($.proxy(this.onDelete, this));
        },

        initButtons : function() {
            this.$element.find(this.addSelector).before("<a class='addFieldButton btn btn-success pull-left' title="+window.i8ln.species.specie.adon+"><i class='icon-plus'></i>"+window.i8ln.species.specie.adon+"</a>");
            this.$element.find(this.editSelector).before("<a class='pull-right deleteFieldButton btn btn-danger' title="+window.i8ln.species.specie.bdel+"><i class='icon-trash'></i>"+window.i8ln.species.specie.bdel+"</a><a class='pull-right editFieldButton btn btn-primary' title="+window.i8ln.species.specie.bedi+"><i class='icon-edit'></i>"+window.i8ln.species.specie.bedi+"</a>");
        },

        onEdit : function(e){    
            e.stopPropagation();
            e.preventDefault();
            var $conEntry = $(e.currentTarget).parent();
            var $container = $conEntry.parent();
            this.initEditableForm($container, $conEntry, $container.data());

        },

        onAdd : function(e) {    
            e.stopPropagation();
            e.preventDefault();
            $(e.currentTarget).hide();
            //TODO:little messy structure
            var $container = $(e.currentTarget).next('.dummy.speciesField').show();
            var $conEntry = $container.children('.contributor_entry');
            this.initEditableForm($container, $conEntry, $container.data());
        },

        onDelete : function(e) {
            e.stopPropagation();
            e.preventDefault();
            var c = confirm(window.i8ln.species.abstracteditabletype.del);
            if(c == true) {

                var $conEntry = $(e.currentTarget).parent();
                var $container = $conEntry.parent();

                var params = $container.data();
                params['act'] = 'delete';
                $.ajax({
                    url : params.url ? params.url : window.params.species.updateUrl,
                    type : 'POST',
                    data : params,
                    context : $container,
                    success : this.onUpdateSuccess,
                    error : this.onUpdateError
                });
            }
        },


        initEditableForm : function($container, $conEntry, options) {
            var addMediaHtml = '<a title="Add Media" style="position: relative;top: 54px;margin-right: 5px;right: 233px;" class="pull-right speciesFieldMedia"><i class="icon-picture"></i></a>';

            $container.prepend(addMediaHtml);

            $.fn.editableform.buttons = '<button type="submit" class="btn btn-primary editable-submit"><i class="icon-ok icon-white"></i>'+window.i8ln.species.specie.bsav+'</button><button type="button" class="btn editable-cancel"><i class="icon-remove"></i>'+window.i8ln.species.specie.bcanc+'</button>'

            $.fn.neweditableform_template = '\
                                            <form class="form-horizontal editableform">\
                                            <div class="control-group">\
                                            <div><div class="editable-input"></div><div class="editable-buttons editable-buttons-bottom pull-right">'+$.fn.editableform.buttons+'</div></div>\
                                            <div class="editable-error-block"></div>\
                                            </div> \
                                            </form>';


            $(".speciesFieldMedia").unbind("click").click(function(){
                var me = this;
                var $container = $(me).closest(".speciesField");
                getSpeciesFieldMedia($container.data("speciesid"), $container.data("pk"), "fromSingleSpeciesField", window.params.getSpeciesFieldMedia)
            });

            var $sf = this;
            if(!options) options = {};

            $conEntry.find('.attributionContent').show();

            $container.find('.editableform').hide();

            var $form = $container.find('.editableform');
            var contriEditor;

            if($form.length == 0) {
                var $form = $($.fn.neweditableform_template);
                var $editableInput = $form.find('.editable-input').css({'display':'block'});

                var $editable = $conEntry.find('.editField, .selector, .addField');
                $.each($editable, function (index, value) {
                    var $editField = $(value);

                    var data = $editField.data();
                    var v = data.value?data.value:$editField.text().trim();

                    var $existingEle = $editableInput.find('[name='+data.name+']');

                    if($existingEle.length == 0) {
                        var $html = $sf.renderEditField($editField, contriEditor);
                        $editableInput.append($html);
                    } else {
                        v = $existingEle.val() + '\n' + v;
                        $existingEle.val(v);
                    }

                    if(data.type == 'autofillUsers') {
                        if(!contriEditor) {
                            //autofill users
                            contriEditor = $form.find(".autofillUsers").autofillUsers({
                                usersUrl : window.params.userTermsUrl
                            });
                        }
                        contriEditor[0].addUserId({'item':{'userId':data.pk, 'value':data.value}});
                    }
                });

                $container.append($form);
            }
            $conEntry.hide();
            $form.show();

            var editor = $sf.renderCKEditor($conEntry.find('.ck_desc'), $editableInput);

            //Submit
            var params = {};
            $.extend(params, options, {'editor':editor, 'contriEditor':contriEditor[0], '$container':$container, $form:$form});
            $form.on('submit', params, $.proxy($sf.onFormSubmit, $sf));

            //Cancel
            $form.find('.editable-cancel').click(function(){
                //TODO:can even remove form
                $form.hide();
                if(!$conEntry.parent().hasClass('dummy')) {
                    $conEntry.show();
                }

                $('body').animate({
                    scrollTop: $conEntry.offset().top
                }, 2000);
            });
            
        },

        renderEditField : function($editField, contriEditor) {
            var data = $editField.data();
            var value = data.value?data.value:$editField.text().trim();
            var $html = new Array();
            $html.push('<div class="control-group">');
            var name = data.name.toLowerCase().replace(/\b[a-z]/g, function(letter) {
                return letter.toUpperCase();
            });
            $html.push('<label class="control-label '+data.name+'">'+name+'</label><div class="controls">');
            switch(data.type) {
                case 'text' : 
                    $html.push('<input class="input-block-level" type="'+data.type+'" name="'+data.name+'" value="'+value+'" placeholder="'+(data.placeholder?data.placeholder:data.originalTitle)+'" title="'+data.originalTitle+'"/>');
                    break;
                case 'textarea' :
                    $html.push('<textarea class="input-block-level" rows="'+data.rows+'" name="'+data.name+'" placeholder="'+(data.placeholder?data.placeholder:data.originalTitle)+'" title="'+data.originalTitle+'">'+value+'</textarea>');
                    break;
                case 'select' :
                    $html.push('<select name="'+data.name+'">');
                    var $selectorOptions;
                    if(data.name === 'license') {
                        $selectorOptions = licenseSelectorOptions
                    } else if (data.name === 'audienceType') {
                        $selectorOptions = audienceTypeSelectorOptions
                    } else if (data.name === 'status') {
                        $selectorOptions = statusSelectorOptions
                    }
                    $.each($selectorOptions, function(index, v) {
                        var selected = (value == v.text)
                        if(selected)
                        $html.push('<option value="'+v.value+'" selected>'+v.text+'</option>');
                        else 
                        $html.push('<option value="'+v.value+'" >'+v.text+'</option>');
                    });
                    $html.push('</select>');
                    break;
                case 'ckeditor' :
                    break;
                case 'autofillUsers' : 
                    if(contriEditor == undefined) {
                        $html.push('<ul class="userOrEmail-list"><input id="userAndEmailList_'+data.fieldid+'" class="autofillUsers" placeholder="Type user name or email id" style="float: left" type="text" /><input name="contributor" type="hidden" /></ul>');
                    }
                    break;
            }
            $html.push('</div></div>');

            return $html.join(' ');
        },

        renderCKEditor : function($textarea, $editableInput) {
            $textarea = $textarea.clone();
            //changing id while adding it to the form
            var id = $textarea.attr('id');
            id = id+"_e"
            $textarea.attr('id', id);
            $textarea = $textarea.prependTo($editableInput);
            var editor = CKEDITOR.instances[id];
            if(editor) {
                //TODO: instead of destroying look for ways of using the same form and editor
                //editor.destroy()
                //if(editor.container.isVisible()) {
                //    editor.container.hide();
                //} else {
                editor.container.show();
                // }
            } else {
                editor = CKEDITOR.replace(id, config);
            }
            return editor
        },

        onFormSubmit :  function(e) {
            $("body").css("cursor", "progress");
            e.stopPropagation();
            e.preventDefault();
            var $sf = this;
            var params = e.data?e.data:{};
            if(params.$container) {
                $(e.target).data('$container', params.$container);
                $(e.target).data('editor', params.editor);
                $(e.target).data('contriEditor', params.contriEditor);
                $(e.target).data('$form', params.$form);
            }
            var $container = $(e.target).data('$container');;
            var $form = $(e.target).data('$form');;
            $form.find('textarea[name="description"]').val(params.editor.getData());
            
		    $form.find('input[name="contributor"]').val(params.contriEditor.getEmailAndIdsList().join(","));
            delete params['$container'];
            delete params['editor'];
            delete params['contriEditor'];
            delete params['$form'];
            if($("#addSpFieldResourcesModal").data("spfieldid") == $(e.target).closest(".speciesField").data("pk")){
                params['runForImages'] = true;
                var paramsForObvSpField = {} //new Object();
                var paramsForUploadSpField = {} //new Object();

                var allInputs = $("#pullObvImagesSpFieldForm :input");
                allInputs.each(function() {
                    if($(this).hasClass("pullImage") && $(this).is(':checked')){
                        paramsForObvSpField[this.name] = $(this).val();
                    } else if(!$(this).hasClass("pullImage")){
                        paramsForObvSpField[this.name] = $(this).val();
                    }

                });
                var allInputs1 = $("#uploadSpeciesFieldImagesForm :input");
                allInputs1.each(function() {
                    paramsForUploadSpField[this.name] = $(this).val();
                });
                params['paramsForObvSpField'] = JSON.stringify(paramsForObvSpField);
                params['paramsForUploadSpField'] = JSON.stringify(paramsForUploadSpField);
            } else {
                params['runForImages'] = false;
            }
            $form.ajaxSubmit({
                url : window.params.species.updateUrl,
                type : 'POST',
                data : params,
                context : $container,
                success : $sf.onUpdateSuccess,
                error: $sf.onUpdateError
            });
            return false;
        },

        onUpdateSuccess : function(data) {
            //$container is always the speciesField element
            var $container = $(this);
            var $form = $container.children('form.editableform');

            var $errorBlock = $form? $form.find('.editable-error-block') : $('<div class="errors"></div>').appendTo($container);

            if(data.errors && data.errors.length > 0) {
                data.msg += "<div class='alert-error'>"+ window.i8ln.species.abstracteditabletype.er +"</div><ul class='alert-error'>";
                $.each(data.errors, function(i, v) {
                    data.msg += "<li>"+v+"</li>"
                });
                data.msg += "</ul>"
            }

            if(data.success == true) {
                var $newEle;

                //destroying all ckeditor instances
                $.each($form.find('.ck_desc'), function(index, textarea) {
                    CKEDITOR.instances[$(textarea).attr('id')].destroy();
                });

                if(data.act == 'add') {
                    //replaces dummy field
                    $newEle = $(data.content).replaceAll($container.parent()).show();
                    //$newEle = $container.html(data.content).show();
                } else if(data.act == 'delete') {
                    if($container.nextAll('speciesField.contributor_entry').length == 0) {
                        //put add button on empty container when user deletes a last item
                        $newEle = $container.html(data.content).show();
                    } else {
                        $container.remove();
                    }
                } else {
                    $newEle = $(data.content).replaceAll($container).show();
                }

                $form.hide();
                $errorBlock.removeClass('alert-error').addClass('alert-info').html(data.msg);
                $container.removeClass('errors');
                //TODO: remove species field specific lines
                var speciesfields = $newEle.speciesfield();
                for (var i=0; i < speciesfields.length; i++) {
                    speciesfields[i].initEditables('.ck_desc', '.dummy.speciesField');
                }
                $newEle.effect("highlight", {color: '#4BADF5'}, 5000);
                $('body').animate({
                    scrollTop: $newEle.offset().top
                }, 2000);


            } else {
                $errorBlock.removeClass('alert-info').addClass('alert-error').html(data.msg);
                $container.addClass('errors');
            }
            $("body").css("cursor", "default");
        },

        onUpdateError : function(response, status, error) {
            var successHandler = this.success, errorHandler;
            handleError(response, undefined, undefined, function(data){
                return window.i8ln.species.abstracteditabletype.re;
            }, function(data) {
                if(data && data.status == 401) {
                    return window.i8ln.species.abstracteditabletype.sub; 
                } else if(response.status === 500) {
                    return window.i8ln.species.abstracteditabletype.un;
                } else {
                    return response.responseText;
                }
            });
            $("body").css("cursor", "default");
        }
    });

    $.extend($.fn.components, {abstracteditabletype: AbstractEditableType});
}(window.jQuery));
