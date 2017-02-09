/**
  @class TaxonHierarchy
  @extends abstracttype
 **/
(function($) {
    "use strict";
    var TaxonHierarchy = function(div, options) {
        this.init($(div), options);
        this.show();
    };

    $.fn.editableutils.inherit(TaxonHierarchy, $.fn.components.abstracteditabletype);

    $.extend(TaxonHierarchy.prototype, {
        constructor: TaxonHierarchy,
        show: function() {
            var me = this;
            me.options.postData = {
                n_level: -1,
                expand_species: me.options.expandSpecies,
                expand_taxon: me.options.expandTaxon,
                expand_all: me.options.expandAll,
                speciesid: me.options.speciesId,
                taxonid: me.options.taxonId,
                classSystem: $.trim($('#taxaHierarchy option:selected').val()),
                user:me.options.user,
                variant:me.options.variant
            }
                

            $.jstree.plugins.questionmark = function(options, parent) {
                this.bind = function() {
                    parent.bind.call(this);
                    this.element.on("click.jstree", ".jstree-questionmark", $.proxy(function(e) {
                        e.stopImmediatePropagation();
                        this.settings.questionmark.call(this, this.get_node(e.target));
                    }, this));
                };

               this.teardown = function() {
                    if (this.settings.questionmark) {
                        this.element.find(".jstree-questionmark").remove();
                    }
                    parent.teardown.call(this);
                };
                
                this.redraw_node = function(obj, deep, callback, force_draw) {

                    var i, j, tmp = null,
                        elm = null;
                        
                        
                    obj = parent.redraw_node.call(this, obj, deep, callback, force_draw);
                    if (obj) {
                        var nodeData = this.get_node(obj);
                        for (i = 0, j = obj.childNodes.length; i < j; i++) {
                            if (obj.childNodes[i] && obj.childNodes[i].className && obj.childNodes[i].className.indexOf("jstree-anchor") !== -1) {
                                tmp = obj.childNodes[i];
                                break;
                            }
                        }
                        if (tmp) {
                            var btnAction = "Show",
                                level = nodeData.original.rank,
                                levelTxt, title, el = document.createElement('div');
                               
                               if(nodeData.original.haspermission==true) {   
                                 $(obj).children('.jstree-anchor').append("<span class='forContributor'>Contributor</span>");
                                
                                }


                            if(nodeData.original.speciesid && nodeData.original.speciesid != -1) {
                                $(obj).children('.jstree-anchor').attr('href', '/species/show/'+nodeData.original.speciesid).css({'color':'#08c', 'cursor':'pointer'}).attr('target', '_blank');
                                //$(obj).children('.jstree-anchor').attr("span")
                            }


                            $.each(taxonRanks, function(i, v) {
                                if (level == v.value) {
                                    levelTxt = "<span class='rank'>" + v.text + ": </span>";
                                    title = v.text;
                                    return;
                                }
                            });

                            if (me.options.action == 'taxonBrowser') {
                                el.innerHTML = levelTxt;
                                tmp.insertBefore(el.childNodes[0], tmp.childNodes[tmp.childNodes.length - 1]);
                            }
                            tmp.setAttribute('title', title);

                            var selectedTaxonId = $("input#taxon").val();
                            if (nodeData.original.isSpecies && nodeData.original.speciesId != -1) {
                                //elm = "<span class='rank rank" + level + "'><a href='/species/show/" + speciesId + "'>" + el + "</a>";
                            } 
                            
                            if (selectedTaxonId && nodeData.original.taxonid == selectedTaxonId) {
                                btnAction = "Hide";
                                elm = "<span class='rank rank" + level + " btn-info-nocolor'>";
                                $(obj).children('.jstree-anchor').addClass('taxon-highlight');

                            } else {
                                elm = "<span class='rank rank" + level + "'>";
                            }


                            if (me.options.controller == 'namelist') {
                                btnAction = 'Show';
                            }

                            if(me.options.controller == 'species' && (me.options.action == 'list' || me.options.action == 'taxonBrowser')) {                                
                                /*if(!nodeData.original.speciesid || nodeData.original.speciesid == -1) {
                                    var btn = "<button style='line-height:14px;' class='createPage" + ((btnAction == 'Show') ? '' : 'active') + "' data-controller='species' data-action='create' data-taxonid='"+nodeData.original.taxonid+"' title='Create page for this taxon'>Create page</button>";
                                    el.innerHTML = btn;
                                    $(el.childNodes[0]).insertAfter(tmp);
                                } else {
                                    //TODO: show page
                                }*/
                            }

                            if (me.options.action != 'show' && me.options.action != 'taxonBrowser') {
                                $(tmp).data('taxonid', nodeData.original.taxonid);
                                $(tmp).data('rank', nodeData.original.rank);
                                $(tmp).attr('title', 'Show all '+me.options.controller+'s for this taxon');
                               
                                
                            }

                            if (nodeData.original.isContributor == 'true') {
                                $("#taxonHierarchy").addClass('editField');
                            } else {
                                $("#taxonHierarchy").removeClass('editField');
                            }

                            if(me.options.action == 'taxonBrowser') {
                                if(nodeData.original.usersList.length > 0){
                                    for(var i=0;i<nodeData.original.usersList.length;i++){
                                        var userData = nodeData.original.usersList[i];
                                        $(obj).append('<span class="userList"><a title="'+userData['name']+'" href="/user/show/'+userData['id']+'" target="_blank"><img class="'+userData['perm']+'" src="'+userData['profile_pic']+'" /></a></span>');
                                    }
                                }
                            }
                        }
                        return obj;
                    };
                };
            };
            
            var filterResults = function(e) {
                var $me;
                if(e) {
                if(!$(e.target).hasClass('jstree-anchor')) {
                    $me = $(e.target).parent();
                } else {
                    $me = $(e.target);
                }
                }
                var selectedTaxonId = $me.data('taxonid');
                
                

                //                    $("#"+selectedTaxonId).removeClass('btn-info-nocolor').parent().closest('tr').removeClass('taxon-highlight');
                //                    $(e.target).parent('span').prev().addClass('btn-info-nocolor').closest('tr').addClass('taxon-highlight');
                $('#taxonHierarchy').find(".taxon-highlight").removeClass('taxon-highlight');
                $me.addClass('taxon-highlight');
                var selectedNode = $('.jstree-anchor.taxon-highlight');
                            var jstree = $('#taxonHierarchy').jstree(true);
                            var node = jstree.get_node(selectedNode);
                            var nodeData = node.original;
                            //var children=jstree._get_children(selectedNode);
                            //console.log(children);
                            console.log(node);
                            console.log(jstree);
                /*var s = $(e.target).hasClass('active');
                $('#taxonHierarchy .taxDefIdSelect').removeClass('active');
                s ? $(e.target).removeClass('active') : $(e.target).addClass('active');
                */
                switch (me.options.controller) {
                    case 'namelist':
                        $("input#taxon").val(selectedTaxonId);
                        getNamesFromTaxon($me, $me.attr('id').replace('_anchor',''), getSelectedStatus(), getSelectedPosition(), getSelectedRanks());
                        break;
                    /*case 'trait':
                        $("input#taxon").val(selectedTaxonId);
                        updateGallery(window.location.pathname + window.location.search, 40, 0, undefined, true);                    
                    break; 
                    */default:
                        if ($me.hasClass('taxon-highlight')) {
                            var classificationId = $('#taxaHierarchy option:selected').val();
                            $("input#taxon").val(selectedTaxonId);
                        } else {
                            $("input#taxon").val('');
                            //$("span.rank").removeClass('btn-info-nocolor').parent().closest('tr').removeClass('taxon-highlight');
                            $(".jstree-anchor").removeClass('taxon-highlight');
                        }
                        
                        $('.createPage').hide();

                        if (me.options.controller == 'species' && me.options.action == 'list') {
                            var selectedNode = $('.jstree-anchor.taxon-highlight');
                            var jstree = $('#taxonHierarchy').jstree(true);
                            var node = jstree.get_node(selectedNode);
                            var nodeData = node.original;
                            console.log(node);
                            console.log(nodeData);
                            if(nodeData.speciesid && nodeData.speciesid != -1) {
                                window.location.href = '/species/show/'+nodeData.speciesid;
                                return;
                            }
                            hasPermissionToCreateSpeciesPage(selectedTaxonId);
                        }

                        if (me.options.action != 'show' && me.options.action != 'taxonBrowser') {
                            updateGallery(window.location.pathname + window.location.search, 40, 0, undefined, true);
                        }

                        break;
                }

                
            };

            function scrollIntoView(ele) {
                console.log('scrollIntoView');
                var scrollTo = $(ele);
                if(scrollTo && scrollTo.offset()) {
                    var myContainer = $('#taxonHierarchy');
                    myContainer.animate({
                        scrollTop: scrollTo.offset().top - myContainer.offset().top + myContainer.scrollTop()
                    });
                }
            }


            var x = me.$element.find('#taxonHierarchy').jstree({
                'core': {
                    "themes": {
                        'dots': true,
                        'stripes': true,
                        "variant" : me.options.postData.variant
                    },
                    'data': {
                        'url': window.params.taxon.classification.listUrl,
                        'type': 'get',
                        'dataType': 'json',
                        'data': function(node) {
                            var postData = me.options.postData
                            if (node.id != '#')
                                postData['id'] = node.id
                            return postData;
                        }
                    }
                },
                "checkbox": {
                    'keep_selected_style': false,
                    'three_state': false,
                    'whole_node': false,
                    'cascade': 'down',
                    'visible': false
                },
                "search": {
                    'ajax': {
                        'url': window.params.taxon.searchUrl
                    }
                },
                'massload': function(ids, callback) {
                    $.ajax({
                        'url': window.params.taxon.nodesUrl,
                        'data': {
                            'id': ids.join(',')
                        },
                        dataType: 'json',
                        method: 'post'
                    }).done(function(data) {
                        callback(data);
                        console.log(data);
                        //$('#searchTaxonButton').html('Search').removeClass('disabled')
                        //$('.searchTaxonPaginate').removeClass('disabled');
                        //$('body').addClass('busy');
                    });
                },
                "plugins": [
                    "massload", "search", "sort", "themes", "questionmark", "checkbox"
                ]
            })

            me.$element.find('#taxonHierarchy').on('ready.jstree', function() {
                var postData = me.options.postData;
                postData["expand_species"] = false;
                postData["expand_taxon"] = false;
                postData["expand_all"] = false;
                if (me.options.editable) {
                    me.updateEditableForm(postData);
                    //removing existing buttons and also their event handlers bfr init agn
                    me.$element.find(me.addSelector).prevAll('.addFieldButton, .editFieldButton, .deleteFieldButton').remove();
                    me.initEditables(me.editSelector, me.addSelector);
                }
                //$("span.rank.btn-info-nocolor").parent().closest('tr').addClass('taxon-highlight');
                if(postData.taxonid) {
                    $("a.jstree-anchor[data-taxonid='"+postData.taxonid+"']").addClass('taxon-highlight');
                } else {

                    if (me.options.controller == 'namelist') {
                        $("#taxonHierarchy a.jstree-anchor").first().addClass('taxon-highlight');
                        $("input#taxon").val($("#taxonHierarchy a.jstree-anchor").first().data('taxonid'));
                    }
                }

                if (me.options.action == 'taxonBrowser') {
                    $(this).jstree(true).show_checkboxes();
                }
                
                if (me.options.controller == 'namelist') {
                    var anchor = $('a.jstree-anchor.taxon-highlight');
                    
                    if(anchor.length > 0) {
                        var parentId = anchor.parent().attr('id');
                        $(this).jstree(true).open_node('#'+parentId);
                        getNamesFromTaxon(anchor, anchor.attr('id').replace('_anchor',''), getSelectedStatus(), getSelectedPosition(), getSelectedRanks());
                        scrollIntoView(anchor);
                    } else if(me.options.taxonId) {
                        console.log("No node in the tree with this id");
                        getNameDetails(me.options.taxonId, me.options.postData.classSystem, 'synonym', undefined, false)
                    }
                }
               $("a.jstree-anchor[data-taxonid='"+postData.taxonid+"']").addClass('taxon-highlight');
                $('#taxonHierarchy').on('click', ".taxDefIdSelect", filterResults);

                var searchResultAnchors;
                $('#searchTaxonButton').click(function() {
                    $(this).html('Searching...').addClass('disabled');
                     $('.searchTaxonPaginate').addClass('disabled')
                    var v = $('#searchTaxon').val();
                    me.$element.find('#taxonHierarchy').jstree(true).search(v);
                    $('#searchTaxon').parent().parent().find('.ui-autocomplete').hide();
                });

                $('#searchTaxon').keypress(function (e) {
                    var key = e.which;
                    if(key == 13) { // the enter key code
                        $('#searchTaxonButton').click();
                        return false;  
                    }
                });   
                $('#searchTaxonNext').click(function() {
                    searchResultAnchors = $('.jstree-search');
                    if(searchResultAnchors.length == 1) {
                        $('.searchTaxonPaginate').addClass('disabled')
                    } else {
                        $('.searchTaxonPaginate').removeClass('disabled')
                    }
                    var selId = $('.jstree-search.search-highlight').attr('id');
                    for(var i=0; i< searchResultAnchors.length; i++) {
                        if($(searchResultAnchors[i]).attr('id') == selId) break;
                    }
                    if(i+1 < searchResultAnchors.length) {
                        $(searchResultAnchors[i]).removeClass('search-highlight');
                      scrollIntoView($(searchResultAnchors[i+1]).addClass('search-highlight')[0]);
                    } else {
                        $(this).addClass('disabled')
                    }
                });
                $('#searchTaxonPrev').click(function() {
                    searchResultAnchors = $('.jstree-search');
                    if(searchResultAnchors.length == 1) {
                        $('.searchTaxonPaginate').addClass('disabled')
                    } else {
                        $('.searchTaxonPaginate').removeClass('disabled')
                    }
                    var selId = $('.jstree-search.search-highlight').attr('id');
                    for(var i=0; i< searchResultAnchors.length; i++) {
                        if($(searchResultAnchors[i]).attr('id') == selId) break;
                    }

                    if(i > 0) {
                        $(searchResultAnchors[i]).removeClass('search-highlight');
                        scrollIntoView($(searchResultAnchors[i-1]).addClass('search-highlight')[0]);
                    } else {
                        $(this).addClass('disabled')
                    }
                });
                
                $("#searchTaxon").autofillNames({
                    'appendTo' : $("#searchTaxon").parent().parent().find('.nameSuggestions'),
                    'nameFilter':'scientificNames',
                    focus: function( event, ui ) {
                        $("#searchTaxon").val( ui.item.label.replace(/<.*?>/g,"") );
                        $("#nameSuggestions_searchTaxon li a").css('border', 0);
                        return false;
                    },
                    select: function( event, ui ) {
                        $("#searchTaxon").val( ui.item.label.replace(/<.*?>/g,"") );
                        return false;
                    },open: function(event, ui) {
                    }
                });

            }).on('load_node.jstree', function(event, obj) {
                var l = obj.node.children.length;
                for (var i = 0; i < l; i++) {}
            }).on('model.jstree', function(nodes, parent) {
            }).bind("select_node.jstree", function(e, data) {
                    filterResults(data.event);
            }).on('search.jstree', function(e, data) {
                $(this).find('.jstree-search:eq(0)').addClass('search-highlight');
                scrollIntoView($(this).find('.jstree-search:eq(0)')[0]);
            }).on('search.jstree', function(nodes, search_string, result_objects) {
                $('#searchTaxonButton').html('Search').removeClass('disabled')
                $('.searchTaxonPaginate').removeClass('disabled');
            });

            $("#taxaHierarchy>select[name='taxaHierarchy']").change($.proxy(this.onChange, this));
        },

        onChange: function(e) {
            var me = this;
            var postData = me.options.postData; //$("#taxonHierarchy").getGridParam('postData');
            postData["expand_species"] = me.options.expandSpecies;
            postData["expand_taxon"] = me.options.expandTaxon;
            postData["expand_all"] = me.options.expandAll;
            var selectedClassification = $('#taxaHierarchy option:selected').val();
            postData["classSystem"] = $.trim(selectedClassification);
            $('#cInfo').html($("#c-" + selectedClassification).html());
            $("input#taxon").val('');
            //$('#taxonHierarchy').trigger("reloadGrid");
            $('#taxonHierarchy').jstree(true).refresh();

        },

        onAdd: function(e) {
            e.stopPropagation();
            e.preventDefault();
            this.$element.find('#taxaHierarchy #taxonHierarchy').hide();
            this.clearEditableForm();
            this.initEditableForm(window.params.taxon.classification.createUrl, {});
        },

        onEdit: function(e) {
            e.stopPropagation();
            e.preventDefault();
            this.$element.find('#taxaHierarchy #taxonHierarchy').hide();
            this.updateEditableForm();
            this.initEditableForm(window.params.taxon.classification.updateUrl, {});
        },

        onDelete: function(e) {
            e.stopPropagation();
            e.preventDefault();
            var c = confirm(window.i8ln.species.abstracteditabletype.del);
            if (c == true) {
                var $sf = this;
                var $form = $(e.currentTarget);
                var params = e.data ? e.data : {};

                params['reg'] = $('#taxaHierarchy option:selected').val();
                delete params['context'];
                delete params['action'];

                $form.ajaxSubmit({
                    url: window.params.taxon.classification.deleteUrl,
                    type: 'POST',
                    data: params,
                    context: $sf,
                    success: $sf.onUpdateSuccess,
                    error: $sf.onUpdateError
                });
            }
            return false;
        },

        initEditableForm: function(action, options) {
            var $form = this.$element.find('#taxonHierarchyForm').show();

            $form.find(".taxonRank").autofillNames();

            var $errorBlock = $form.find('.editable-error-block');
            $errorBlock.removeClass('alert-error alert-info').html('');

            //Submit
            var params = {};
            $.extend(params, options, {
                action: action
            });
            $form.off('submit').on('submit', params, $.proxy(this.onFormSubmit, this));

            //Cancel
            $form.find('.editable-cancel').on('click', {
                $form: $form
            }, $.proxy(this.cancel, this));
            return false;
        },

        updateEditableForm: function(postData) {
            var hierarchy = this.$element.find('#taxonHierarchy').jstree().getJSON();
            $.each(hierarchy, function(i, v) {
                var temp = $(v.name.split(':')[1]);
                $('.taxonRank[name="taxonRegistry.' + v.level + '"]').val(temp.text());
            });

            /*if(postData) {
              $('.classification').val(postData.classSystem);
              }*/
        },

        clearEditableForm: function() {
            $('.taxonRank').val('');
        },

        cancel: function(e) {
            e.data.$form.hide();
            this.$element.find('#taxaHierarchy #taxonHierarchy').show();
        },

        onFormSubmit: function(e) {
            var $sf = this;
            var $form = $(e.currentTarget);
            var params = e.data ? e.data : {};
            if (params.action) {
                $(this).data('action', params.action);
            }

            var action = $(this).data('action');
            params['reg'] = $('#taxaHierarchy option:selected').val();
            delete params['context'];
            delete params['action'];

            $form.ajaxSubmit({
                url: action,
                type: 'POST',
                data: params,
                context: $sf,
                success: $sf.onUpdateSuccess,
                error: $sf.onUpdateError
            });
            return false;
        },

        onUpdateSuccess: function(data) {
            var $sf = this;
            var $form = $sf.$element.find('#taxonHierarchyForm');

            var $errorBlock = $sf.$element.find('.editable-error-block');

            if (data.errors && data.errors.length > 0) {
                data.msg += "<div class='alert-error'>" + window.i8ln.species.abstracteditabletype.er + "</div><ul class='alert-error'>";
                $.each(data.errors, function(i, v) {
                    data.msg += "<li>" + v + "</li>"
                });
                data.msg += "</ul>"
            }

            if (data.success == true) {
                var $newEle;
                $errorBlock.removeClass('alert-error').addClass('alert-info').html(data.msg);
                $sf.$element.removeClass('errors');
                if (data.action == 'delete')
                    $('#taxaHierarchy option:selected').remove();
                else if (data.action == 'update') {
                    $('#taxaHierarchy option:selected').val(data.reg.id);
                } else if (data.action == 'create') {
                    $('<option value="' + data.reg.id + '">' + data.reg.classification.name + '</option>').appendTo('#taxaHierarchy select').attr('selected', 'selected');
                }

                if (data.errors.length == 0) {
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
    $.fn.taxonhierarchy = function(options) {
        if (options.taxonId) {
            $("input#taxon").val(options.taxonId);
        }

        if (options.classSystem) {
            var t = '"'+options.classSystem+'"'
            $("#taxaHierarchy select").find("option[value="+t+"]").attr('selected',true);
        }

        var taxonHierarchies = new Array();
        this.each(function() {
            taxonHierarchies.push(new TaxonHierarchy(this, options));
        });

        return {
            'taxonHierarchies': taxonHierarchies
        }

    };
}(window.jQuery));
