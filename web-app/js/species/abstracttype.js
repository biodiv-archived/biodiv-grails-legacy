(function ($) {
    "use strict";

    //types
    $.fn.speciesComponents = {};

    var AbstractType = function () { 
    };

    AbstractType.prototype = {
        /**
          Initializes input

          @method init() 
         **/
        init : function(element, options) {
            this.options = $.extend({}, options);
            this.$element = $(element);
            if(!this.options.scope) {
                this.options.scope = this;
            }
        },

        initEditables : function(selector) {
            this.$element.find(".dummy.speciesField").before("<a class='addFieldButton btn btn-success' title='Add'><i class='icon-plus'></i>Add</a>");
            this.$element.find(selector).before("<a class='pull-right deleteFieldButton btn btn-danger' title='Delete'><i class='icon-trash'></i>Delete</a><a class='pull-right editFieldButton btn btn-primary' title='Edit'><i class='icon-edit'></i>Edit</a>");

            this.$element.find('.addFieldButton').click($.proxy(this.onAdd, this));
            this.$element.find('.editFieldButton').click($.proxy(this.onEdit, this));
            this.$element.find('.deleteFieldButton').click($.proxy(this.onDelete, this));
        },

        onEdit : function(e){    
            e.stopPropagation();
            e.preventDefault();
            console.log(this);
            var $conEntry = $(e.currentTarget).parent();
            var $container = $conEntry.parent();
            this.initEditableForm($container, $conEntry, $container.data());
        },

        onAdd : function(e){    
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
        },


        initEditableForm : function($container, $conEntry, options) {
            var $sf = this;
            if(!options) options = {};
            console.log(options);

            $conEntry.find('.attributionContent').show();

            $('.editableform').hide();

            var $form = $container.find('.editableform');

            if($form.length == 0) {
                var $form = $($.fn.neweditableform_template);
                var $editableInput = $form.find('.editable-input').css({'display':'block'});

                var $editable = $conEntry.find('.editField, .selector, .addField');
                $.each($editable, function (index, value) {
                    var $editField = $(value);

                    var data = $editField.data();
                    var v = data.value?data.value:$editField.text().trim();

                    var $html = $sf.renderEditField($editField);
                    var $existingEle = $editableInput.find(data.type+'[name='+data.name+']');
                    if($existingEle.length == 0) {
                        $editableInput.append($html);
                    } else {
                        v = $existingEle.val() + '\n' + v;
                        $existingEle.val(v);
                    }
                });

                $container.append($form);
            }
            $conEntry.hide();
            $form.show();

            var editor = $sf.renderCKEditor($conEntry.find('.ck_desc'), $editableInput);

            //Submit
            var params = {};
            $.extend(params, options, {'editor':editor, '$container':$container, context:this});
            $form.on('submit', params, $sf.onFormSubmit);

            //Cancel
            $form.find('.editable-cancel').click(function(){
                //TODO:can even remove form
                $form.hide();
                //TODO:dont show empty form on add cancel
                $conEntry.show();
            });

        },

        renderEditField : function($editField) {
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
            var $form = $(e.currentTarget);
            var params = e.data?e.data:{};
            console.log(params);
            var $sf = params.context; 
            var $container = params.$container;
            $form.find('textarea[name="description"]').val(e.data.editor.getData());
            
            delete params['$container'];
            delete params['editor'];
            delete params['context'];

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

            var $errorBlock = $form? $form.find('.editable-error-block') : $container.append('<div></div>');

            if(data.errors && data.errors.length > 0) {
                data.msg += "<div class='alert-error'>Please fix following errors</div><ul class='alert-error'>";
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
                    $newEle = $(data.content).replaceAll($container).show();
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
                $newEle.speciesfield();
                $newEle.effect("highlight", {color: '#4BADF5'}, 5000);
            } else {
                $errorBlock.removeClass('alert-info').addClass('alert-error').html(data.msg);
                $container.addClass('errors');
            }
        },

        onUpdateError : function(response, status, error) {
            var successHandler = this.success, errorHandler;
            handleError(response, undefined, undefined, function(data){
                return "Please resubmit the form again";
            }, function(data) {
                if(data && data.status == 401) {
                    return "Please login and resubmit the changes"; 
                } else if(response.status === 500) {
                    return 'Service unavailable. Please try later.';
                } else {
                    return response.responseText;
                }
            });
        }

    }

    $.extend($.fn.speciesComponents, {abstracttype: AbstractType});

}(window.jQuery));


