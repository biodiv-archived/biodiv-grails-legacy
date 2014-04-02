/**
  @class TaxonHierarchy
  @extends abstracttype
 **/
(function ($) {
    "use strict";

    var TaxonHierarchy = function (div, options) {
        this.init($(div), options);
        this.show();
    };

    $.fn.editableutils.inherit(TaxonHierarchy, $.fn.components.abstracteditabletype);

    $.extend(TaxonHierarchy.prototype, {
        constructor: TaxonHierarchy,
        show : function() {
            var me = this;

            var heirarchyLevelFormatter = function(el, cellVal, opts) {
                var cells = $(opts).find('cell');
                var taxonId = $.trim($(cells[0]).text());
                var speciesId = $.trim($(cells[4]).text());
                var level = $(cells[6]).text();
                var levelTxt;
                $.each(taxonRanks, function(i,v) {
                    if(level == v.value) {
                        levelTxt = "<span class='rank'>"+v.text+"</span>";
                        return;
                    }
                });

                //el+= taxonId;
                //if(level == me.options.speciesLevel) {
                //    el = "<a href='/species/show/"+me.options.speciesId+"'>"+el+"</a>";
                //} else {
                    // el = "<a href='${createLink(action:"taxon")}/"+taxonId+"'
                    // class='rank"+level+"'>"+levelTxt+": "+el+"</a>";
                    el = levelTxt+": "+"<span class='rank"+level+"'>"+el;

                    if(this.expandAllIcon) {
                        el += "&nbsp;<a class='taxonExpandAll' onClick='expandAll(\"taxonHierarchy\", \""+cellVal.rowId+"\", true)'>+</a>";
                    }

                    el+= "</span>";

                    /*if("${speciesInstance}".length == 0){
                      el+= "</span><span class='taxDefId'><input class='taxDefIdVal' type='text' style='display:none;'><input class='taxDefIdCheck' type='checkbox' value='werw' onClick='setTaxonId(this,\""+cellVal.rowId+"\")'></span>"
                      }*/
                //}


                var isContributor= $(cells[11]).text();
                if(isContributor == 'true') {
                    $("#taxonHierarchy").addClass('editField');
                } else {
                    $("#taxonHierarchy").removeClass('editField');
                }

                return el;	   
            }


            this.$element.find('#taxonHierarchy').jqGrid({
                url:window.params.taxon.classification.listUrl,
                datatype: "xml",
                colNames:['Id', '_Id_', '', '#Species', 'SpeciesId', 'Class System'],
                colModel:[
                {name:'id',index:'id',hidden:true},
                {name:'_id_',index:'id',hidden:true},
                {name:'name',index:'name',formatter:heirarchyLevelFormatter},
                {name:'count', index:'count',hidden:true, width:50},
                {name:'speciesId',index:'speciesId', hidden:true},
                {name:'classSystem', index:'classSystem', hidden:true}
                ],   		
                width: "${width?:'100%'}",
                height: "${height?:'100%'}", 
                autowidth:true,   
                scrollOffset: 0,
                loadui:'block',
                treeGrid: true,
                ExpandColumn : 'name',
                ExpandColClick  : false,
                treeGridModel: 'adjacency',
                postData:{n_level:-1, expand_species:me.options.expandSpecies, expand_all:me.options.expandAll, speciesid:me.options.speciesId, classSystem:$.trim($('#taxaHierarchy option:selected').val())},
                sortable:false,
                loadComplete:function(data) {
                    var postData = $("#taxonHierarchy").getGridParam('postData');
                    postData["expand_species"] = false;
                    postData["expand_all"] = false;
                    if(me.options.editable) {
                        me.updateEditableForm(postData);
                        //removing existing buttons and also their event handlers bfr init agn
                        me.$element.find(me.addSelector).prevAll('.addFieldButton, .editFieldButton, .deleteFieldButton').remove();
                        me.initEditables(me.editSelector, me.addSelector);
                    }
                },
                loadError : function(xhr, status, error) {
                    if(xhr.status == 401) {
                        show_login_dialog();
                    } else {	    
                        alert(error);
                    }
                } 
            });

            $("#taxaHierarchy").change($.proxy(this.onChange, this));
            
            $('#cInfo').html($("#c-"+$('#taxaHierarchy option:selected').val()).html());
            $('.ui-jqgrid-hdiv').hide();
            $('#taxonHierarchy').parents('div.ui-jqgrid-bdiv').css("max-height","425px");

        },

        onChange : function(e) {
            var me = this;
            var postData = $("#taxonHierarchy").getGridParam('postData');
            postData["expand_species"] = me.options.expandSpecies;
            postData["expand_all"] = me.options.expandAll;
            var selectedClassification = $('#taxaHierarchy option:selected').val();
            postData["classSystem"] = $.trim(selectedClassification);
            $('#cInfo').html($("#c-"+selectedClassification).html());
            $('#taxonHierarchy').trigger("reloadGrid");
        },

        onAdd : function(e){    
            e.stopPropagation();
            e.preventDefault();
            this.$element.find('#taxaHierarchy #taxonHierarchy').hide();
            this.clearEditableForm();
            this.initEditableForm(window.params.taxon.classification.createUrl, {});
        },

        onEdit : function(e){    
            e.stopPropagation();
            e.preventDefault();
            this.$element.find('#taxaHierarchy #taxonHierarchy').hide();
            this.updateEditableForm();
            this.initEditableForm(window.params.taxon.classification.updateUrl, {});
        },

        onDelete : function(e){    
            e.stopPropagation();
            e.preventDefault();
            var c = confirm('You are about to delete some content. Are you sure?');
            if(c == true) {
                var $sf = this; 
                var $form = $(e.currentTarget);
                var params = e.data?e.data:{};

                params['reg'] = $('#taxaHierarchy option:selected').val();
                delete params['context'];
                delete params['action'];

                $form.ajaxSubmit({
                    url : window.params.taxon.classification.deleteUrl,
                    type : 'POST',
                    data : params,
                    context : $sf,
                    success : $sf.onUpdateSuccess,
                    error: $sf.onUpdateError
                });
            }
            return false;
        },

        initEditableForm : function(action, options) {
            var $form = this.$element.find('#taxonHierarchyForm').show();

            $form.find(".taxonRank").autofillNames();
            
            var $errorBlock = $form.find('.editable-error-block');
            $errorBlock.removeClass('alert-error alert-info').html('');

            //Submit
            var params = {};
            $.extend(params, options, {action:action});
            $form.off('submit').on('submit', params, $.proxy(this.onFormSubmit, this));

            //Cancel
            $form.find('.editable-cancel').on('click', {$form:$form}, $.proxy(this.cancel, this));
            return false;
        },

        updateEditableForm: function(postData) {
            var hierarchy = this.$element.find('#taxonHierarchy').getRowData();
            $.each(hierarchy, function(i, v) {
                var temp = $(v.name.split(':')[1]);
                $('.taxonRank[name="taxonRegistry.'+v.level+'"]').val(temp.text());
            });

            /*if(postData) {
                $('.classification').val(postData.classSystem);
            }*/
        },

        clearEditableForm: function() {
            $('.taxonRank').val('');
        },

        cancel : function(e) {
            e.data.$form.hide();
            this.$element.find('#taxaHierarchy #taxonHierarchy').show();
        },

        onFormSubmit :  function(e) {
            var $sf = this; 
            var $form = $(e.currentTarget);
            var params = e.data?e.data:{};
            var action = params.action; 

            params['reg'] = $('#taxaHierarchy option:selected').val();
            delete params['context'];
            delete params['action'];

            $form.ajaxSubmit({
                url : action,
                type : 'POST',
                data : params,
                context : $sf,
                success : $sf.onUpdateSuccess,
                error: $sf.onUpdateError
            });
            return false;
        },

        onUpdateSuccess : function(data) {
            var $sf = this;
            var $form = $sf.$element.find('#taxonHierarchyForm');

            var $errorBlock = $sf.$element.find('.editable-error-block');

            if(data.errors && data.errors.length > 0) {
                data.msg += "<div class='alert-error'>Please fix following errors</div><ul class='alert-error'>";
                $.each(data.errors, function(i, v) {
                    data.msg += "<li>"+v+"</li>"
                });
                data.msg += "</ul>"
            }

            if(data.success == true) {
                var $newEle;
                $errorBlock.removeClass('alert-error').addClass('alert-info').html(data.msg);
                $sf.$element.removeClass('errors');
                if(data.action == 'delete') 
                    $('#taxaHierarchy option:selected').remove();
                else if(data.action == 'update') {
                    $('#taxaHierarchy option:selected').val(data.reg.id);
                } else if(data.action == 'create') {
                    $('<option value="'+data.reg.id+'">'+data.reg.classification.name+'</option>').appendTo('#taxaHierarchy select').attr('selected', 'selected');
                }

                if(data.errors.length == 0) {
                    $form.hide();
                }
                $sf.$element.find('#taxaHierarchy #taxonHierarchy').show();
                $sf.onChange();
            } else {
                $errorBlock.removeClass('alert-info').addClass('alert-error').html(data.msg);
                $sf.$element.addClass('errors');
            }
        }
    });

    /*
       Initialize taxonHierarchy. Applied to jQuery object.

       @method $().taxonHierarchy(options)
       @params {Object} options
       */
    $.fn.taxonhierarchy = function (options) {
        var taxonHierarchies = new Array();
        this.each(function () {
            taxonHierarchies.push(new TaxonHierarchy(this, options));
        });
        return {
            'taxonHierarchies' : taxonHierarchies
        }

    };
}(window.jQuery)); 



