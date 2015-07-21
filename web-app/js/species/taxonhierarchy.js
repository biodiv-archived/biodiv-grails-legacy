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
                var position = $(cells[12]).text();
                var selectedTaxonId = $("input#taxon").val();
                var levelTxt;
                $.each(taxonRanks, function(i,v) {
                    if(level == v.value) {
                        levelTxt = "<span class='rank'>"+v.text+"</span>";
                        return;
                    }
                });
                //el+= taxonId;
                var btnAction = "Show"
                if(speciesId && speciesId != -1) {
                    el = levelTxt+": "+"<span class='rank rank"+level+" "+position+"'><a href='/species/show/"+speciesId+"'>"+el+"</a>";
                } else if(selectedTaxonId && taxonId.endsWith(selectedTaxonId)) {
                    btnAction = "Hide"
                    el = levelTxt+": "+"<span class='rank rank"+level+" "+position+" btn-info-nocolor'>"+el+"";
                } else {
                    el = levelTxt+": "+"<span class='rank rank"+level+" "+position+"'>"+el;
                }
                
                if(this.expandAllIcon) {
                    el += "&nbsp;<a class='taxonExpandAll' onClick='expandAll(\"taxonHierarchy\", \""+cellVal.rowId+"\", true)'>+</a>";
                }
           
                if(me.options.controller == 'namelist') {
                    btnAction = 'Show';
                }
                var postData = $(this).getGridParam('postData');
                var expandSpecies = postData['expand_species'];
                var expandTaxon = postData['expand_taxon'];

                if(me.options.action == 'show' || me.options.action == 'taxonBrowser'){
                    el+= "</span><span class='taxDefId'><input class='taxDefIdVal' type='text' style='display:none;'></input><input class='taxDefIdCheck checkbox "+(expandSpecies?'hide':'')+"' type='hidden'></input></span>"
                } else {
                    el+= "</span><span class='taxDefId'><input class='taxDefIdVal' type='text' style='display:none;'></input><input class='taxDefIdCheck checkbox "+(expandSpecies?'hide':'')+"' type='hidden'></input>";
                    if(me.options.controller) {
                        el += "<button class='btn taxDefIdSelect pull-right "+ ((btnAction=='Show')?'':'active')+"' data-controller='"+me.options.controller+"' data-action='"+me.options.action+"' title='Show all names for this taxon' style='margin-left:5px;height:20px;line-height:11px;'>Show "+me.options.controller+"</button></span>"
                    }
                } 
                var isContributor= $(cells[11]).text();
                if(isContributor == 'true') {
                    $("#taxonHierarchy").addClass('editField');
                } else {
                    $("#taxonHierarchy").removeClass('editField');
                }

                return el;	   
            }
            
            var jqGrid = this.$element.find('#taxonHierarchy').jqGrid({
                url:window.params.taxon.classification.listUrl,
                datatype: "xml",
                colNames:['Id', '_Id_', '', '#Species', 'SpeciesId', 'Class System'],
                colModel:[
                {name:'id',index:'id',hidden:true},
                {name:'_id_',index:'id',hidden:true},
                {name:'name',index:'name',formatter:heirarchyLevelFormatter},
                {name:'count', index:'count',hidden:true},
                {name:'speciesId',index:'speciesId', hidden:true},
                {name:'classSystem', index:'classSystem', hidden:true}
                ],   		
                width: "100%",
                height: "100%", 
                autowidth:true,   
                scrollOffset: 0,
                loadui:'block',
                treeGrid: true,
                ExpandColumn : 'name',
                ExpandColClick  : false,
                treeGridModel: 'adjacency',
                postData:{n_level:-1, expand_species:me.options.expandSpecies, expand_taxon:me.options.expandTaxon, expand_all:me.options.expandAll, speciesid:me.options.speciesId,  taxonid:me.options.taxonId, classSystem:$.trim($('#taxaHierarchy option:selected').val())},
                sortable:false,
                loadComplete:function(data) {
                    var postData = $("#taxonHierarchy").getGridParam('postData');
                    postData["expand_species"] = false;
                    postData["expand_taxon"] = false;
                    postData["expand_all"] = false;
                    if(me.options.editable) {
                        me.updateEditableForm(postData);
                        //removing existing buttons and also their event handlers bfr init agn
                        me.$element.find(me.addSelector).prevAll('.addFieldButton, .editFieldButton, .deleteFieldButton').remove();
                        me.initEditables(me.editSelector, me.addSelector);
                    }
                    $("span.rank.btn-info-nocolor").parent().closest('tr').addClass('taxon-highlight');
                },
                loadError : function(xhr, status, error) {
                    if(xhr.status == 401) {
                        show_login_dialog();
                    } else {	 
                       // alert(error);
                    }
                },
                beforeSelectRow: function (rowid, e) {
                    var $this = $(this),

                    isLeafName = $this.jqGrid("getGridParam", "treeReader").leaf_field,

                    localIdName = $this.jqGrid("getGridParam", "localReader").id,

                    localData,

                    state;

/*                    setChechedStateOfChildrenItems = function (children) {

                        $.each(children, function () {

                            $("#" + this[localIdName] + " input.taxDefIdCheck").prop("checked", state);

                            if (!this[isLeafName]) {

                                setChechedStateOfChildrenItems($this.jqGrid("getNodeChildren", this));

                            }

                        });

                    }
*/
                    $("span.rank").removeClass('btn-info-nocolor').parent().closest('tr').removeClass('taxon-highlight');
                    $(e.target).parent('span').prev().addClass('btn-info-nocolor').closest('tr').addClass('taxon-highlight');

                    var s = $(e.target).hasClass('active');
                    $('#taxonHierarchy .taxDefIdSelect').removeClass('active');
                    s ? $(e.target).removeClass('active'): $(e.target).addClass('active');

                    if ((e.target.nodeName === "INPUT" && $(e.target).hasClass("taxDefIdCheck") )|| ($(e.target).hasClass("taxDefIdSelect"))) {
    
                        state = $(e.target).prop("checked");
                        var last = rowid.substring(rowid.lastIndexOf("_") + 1, rowid.length);
                        $(e.target).parent("span").find(".taxDefIdVal").val(last);
                        switch($(e.target).data('controller')) {
                            case 'namelist' :
                                getNamesFromTaxon($(e.target), rowid);
                                break;
                            default :
                                if($(e.target).hasClass('active')) {
                                    var taxonId = $(e.target).parent("span").find(".taxDefIdVal").val();
                                    var classificationId = $('#taxaHierarchy option:selected').val();

                                    $("input#taxon").val(taxonId);
                                } else {
                                    $("input#taxon").val('');
                                    $("span.rank").removeClass('btn-info-nocolor').parent().closest('tr').removeClass('taxon-highlight');
                                }
                                updateGallery(window.location.pathname + window.location.search, 40, 0, undefined, true);
                                break;
                        }
                        //localData = $this.jqGrid("getLocalRow", rowid);

                       //setChechedStateOfChildrenItems($this.jqGrid("getNodeChildren", localData), state);

                    }

                }

            });
        //    jqGrid.setSelection(selectedTaxonId, function() {console.log('setting selection');} );
            $("#taxaHierarchy>select[name='taxaHierarchy']").change($.proxy(this.onChange, this));
            
            $('#cInfo').html($("#c-"+$('#taxaHierarchy option:selected').val()).html());
            $('.ui-jqgrid-hdiv').hide();
            $('#taxonHierarchy').parents('div.ui-jqgrid-bdiv').css("max-height","425px");

        },

        onChange : function(e) {
            var me = this;
            var postData = $("#taxonHierarchy").getGridParam('postData');
            postData["expand_species"] = me.options.expandSpecies;
            postData["expand_taxon"] = me.options.expandTaxon;
            postData["expand_all"] = me.options.expandAll;
            var selectedClassification = $('#taxaHierarchy option:selected').val();
            postData["classSystem"] = $.trim(selectedClassification);
            $('#cInfo').html($("#c-"+selectedClassification).html());
            $("input#taxon").val('');
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
            var c = confirm(window.i8ln.species.abstracteditabletype.del);
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
            if(params.action) {
                $(this).data('action', params.action);
            }

            var action = $(this).data('action'); 
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
                data.msg += "<div class='alert-error'>"+ window.i8ln.species.abstracteditabletype.er +"</div><ul class='alert-error'>";
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

        if(options.taxonId) {
            $("input#taxon").val(options.taxonId);
        }
        return {
            'taxonHierarchies' : taxonHierarchies
        }

    };
}(window.jQuery)); 



