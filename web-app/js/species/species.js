/**
 * This file needs to be cleaned up
 */

visibleSpeciesFieldId = undefined;
visibleSpeciesConceptId = undefined;
function showSpeciesField(fieldId) {
    if (!fieldId)
        return;
    fieldId = fieldId.indexOf("speciesField") === 0 ? fieldId : "speciesField"
        + fieldId
        if (visibleSpeciesFieldId && visibleSpeciesFieldId != fieldId) {
            // $("#"+visibleSpeciesFieldId).hide();
        }
    $("#" + fieldId).show();
    visibleSpeciesFieldId = fieldId
}

function showSpeciesConcept(fieldId) {
    if (!fieldId)
        return;
    fieldId = fieldId.indexOf("speciesConcept") === 0 ? fieldId
        : "speciesConcept" + fieldId
        if (visibleSpeciesConceptId) {
            $("#" + visibleSpeciesConceptId).hide();
        }
    $("#" + fieldId).appendTo("#speciesFieldContainer").show();
    visibleSpeciesConceptId = fieldId
        if ($("#" + fieldId + " div.occurenceMap").length === 1
                && $("#" + fieldId + " div.occurenceMap").is(":empty")) {
                    showOccurence();
                }
}

var addReference = function(childCount, id) {
    sid = id + "_" + (childCount - 1)
        var clone = $("#reference" + sid).clone()
        var htmlId = 'referencesList[' + id + "_" + childCount + '].';
    var referenceInput = clone.find("input[id$=sid]");

    clone.find("input[id$=sid]").attr('id', htmlId + 'id').attr('name',
            htmlId + 'id');
    clone.find("input[id$=deleted]").attr('id', htmlId + 'deleted').attr(
            'name', htmlId + 'deleted');
    clone.find("input[id$=new]").attr('id', htmlId + 'new').attr('name',
            htmlId + 'new').attr('value', 'true');
    referenceInput.attr('id', htmlId + 'number')
        .attr('name', htmlId + 'number');
    clone.find("select[id$=type]").attr('id', htmlId + 'type').attr('name',
            htmlId + 'type');

    clone.attr('id', 'reference' + childCount);
    $("#childList" + id).append(clone);
    clone.show();
    referenceInput.focus();
}

var deleteReferenceHandler = function() {
    // find the parent div
    var prnt = $(this).parents(".reference-div");
    // find the deleted hidden input
    var delInput = prnt.find("input[id$=deleted]");
    // check if this is still not persisted
    var newValue = prnt.find("input[id$=new]").attr('value');
    // if it is new then i can safely remove from dom
    if (newValue == 'true') {
        prnt.remove();
    } else {
        // set the deletedFlag to true
        delInput.attr('value', 'true');
        // hide the div
        prnt.hide();
    }
}

var positionTOC = function() {
    var pos = $("#fieldstoc").position().top + $("#fieldstoc").height();
    $(window).scroll(function() {
        if ($(window).scrollTop() >= pos) {
            $("#fieldstoc").css({
                position : 'fixed',
                top : '0'
            });
        } else {
            $("#fieldstoc").css({
                position : 'fixed',
                top : $("#speciesFieldContainer").offset().top
            });
        }
    });
}

var showOccurence = function(speciesName) {
    loadGoogleMapsAPI(function() {
        var mapOptions = {
            popup_enabled : true,
    toolbar_enabled : true
        //bbox : "5801108.428222222,674216.547942332, 12138100.077777777, 4439106.786632658"
        };
        var layersOptions = [
    {
        title :window.i8ln.species.specie.oc ,
    layers : 'ibp:occurrence',
    styles : '',
    cql_filter : "species_name='" + speciesName + "'",
    opacity : 0.7
    },
    {
        title :window.i8ln.species.specie.obs ,
    layers : 'ibp:observation_locations',
    styles : '',
    cql_filter : "species_name='" + speciesName + "'",
    opacity : 0.7
    },
    {
        title :window.i8ln.species.specie.ckl,
        layers : 'ibp:checklist_species_locations',
        styles : '',
        cql_filter : "species_name='" + speciesName + "'",
        opacity : 0.7
    }
    ]
        showMap("map1311326056727", mapOptions, layersOptions)
        $("#mapSpinner").hide();
    });
}

var updateEditorContent = function() {
    var editor = $('.fieldEditor').ckeditorGet();
    if (editor.checkDirty()) {
        $(textarea[name = 'description']).val(editor.getData());
    }
}

var setTaxonId = function(el, rowId){
    var last = rowId.substring(rowId.lastIndexOf("_") + 1, rowId.length);
    $(el).parent("span").find(".taxDefIdVal").val(last);
}

var expandAll = function(gridId, rowId, force) {
    var grid = $("#"+gridId);
    var rowData = grid.getRowData(rowId);
    if(force) {
        /*var children = grid.getNodeChildren(rowData);
          for ( var i = 0; i < children.length; i++) {
          grid.delTreeNode(children[i].id);	
          }
          rowData['expanded'] = 'false';
          rowData['loaded'] = 'false';
          */
    }
    if (!grid.isNodeLoaded(rowData) || grid.isNodeLoaded(rowData) == 'false') {
        var postData = grid.getGridParam('postData');
        if(grid.getNodeChildren(rowData).length == 0)
            postData["expand_all"] = true;
        grid.expandRow(rowData);
        grid.expandNode(rowData);
        //$("#" + rowId + " div.treeclick").trigger('click');
    } 
}

var initializeCKEditor = function() {
    /*
     * CKEDITOR.on('instanceReady', function(ev) { var editor = ev.editor; var
     * dataProcessor = editor.dataProcessor; var htmlFilter = dataProcessor &&
     * dataProcessor.htmlFilter; htmlFilter.addRules( { elements : { input:
     * function(element) { return false; }, } }); });
     */
}

var getGoogleImages = function(imageSearch, page) {
    if (page >= 8)
        return;
    imageSearch.gotoPage(page);
    var galleries = Galleria.get();
    var gallery = galleries[galleries.length -1]; // gallery is now the first galleria

    imageSearch
        .setSearchCompleteCallback(
                this,
                function() {
                    for ( var i = 0; i < imageSearch.results.length; i++) {
                        //var url = imageSearch.results[i].url.replace(/%2520/g, " ")
                        var url = decodeURIComponent(imageSearch.results[i].url);
                        gallery
            .push({
                image : url,
                title : imageSearch.results[i].titleNoFormatting,
                description : "<div class='notes'><a href="
                + url
                + " target='_blank'><b>View image source</b> </a></div>"
            })
                    }
                    gallery.show(page * 8);
                })
}

function loadIFrame() {
    var uBioLink = $('#uBioIframeLink'); 
    var url = uBioLink.attr('href')
        uBioLink.remove();
    $('iframe#uBioIframe').attr('src', url).height("500px").width("100%");
}


function initGalleryTabs() {
    var tabs = $("#resourceTabs").tabs();
    if(tabs.length == 0) return;
    if($("#resourceTabs-1 img").length > 0) {

        //TODO:load gallery  images by ajax call getting response in json  
        $('.gallery').galleria({
            height : 400,
            preload : 1,
            carousel : true,
            lightbox:false,
            transition : 'pulse',
            image_pan_smoothness : 5,
            showInfo : true,
            dataSelector : ".galleryImage",
            debug : false,
            thumbnails:true,
            thumbQuality : false,
            maxScaleRatio : 1,
            minScaleRatio : 1,
            _toggleInfo: false,
            wait:true,
            idleMode:false,
            youtube:{
                modestbranding: 1,
        autohide: 1,
        color: 'white',
        hd: 1,
        rel: 0,
        showinfo: 1
            },
            dataConfig : function(img) {
                return {
                    // tell Galleria to grab the content from the .desc div as caption
                    description : $(img).parent().next('.notes').html(),
                    _biodiv_url:$(img).data('original')
                };
            },
            extend : function(options) {
                this.bind('image', function(e) {
                    $(e.imageTarget).click(this.proxy(function() {
                        window.open(Galleria.get(0).getData()._biodiv_url);
                        //this.openLightbox();
                    }));
                });

                this.bind('loadfinish', function(e){
                    var galleriaInfo = $(".galleria-info");
                    galleriaInfo.css('cssText', 'top : 350px !important');
                    //galleriaInfo.css({'top': '350px'});
                    galleryImageLoadFinish();
                    var galleriaSlideUp = $(".galleria-info .slideUp");
                    galleriaSlideUp.addClass('icon-chevron-down').removeClass('icon-chevron-up');
                    galleriaSlideUp.click();
                    var top = galleriaInfo.position().top
                    // minus 8 out of top bcoz trunk8 of 2 lines takes 
                    // height of 8 in galleryImageLoadFinish()
                    galleriaInfo.css({'top': (top - 8)+'px'});
                })

            }

        });	
        Galleria.ready(function() {
            $("#gallerySpinner").hide();
            $("#resourceTabs").css('visibility', 'visible');
           // $(".galleria-thumbnails-container").hide();
        });

    } else {
        $("#resourceTabs").tabs("remove", 0);
    }

    $(".defaultSpeciesConcept").prev("a").trigger('click');	

    var flickrGallery;
    var createFlickrGallery = function(data) {
        $('#gallery3').galleria({
            height:400,
            carousel:true,
            transition:'pulse',
            image_pan_smoothness:5,
            showInfo:true,
            dataSource : data,
            debug: false,
            clicknext:true
        });
        //TODO:some dirty piece of code..find a way to get galleries by name
        var galleries = Galleria.get();	
        if(galleries.length === 1) {
            flickrGallery = Galleria.get(0);
        } else {
            flickrGallery = Galleria.get(1);
        }
        return flickrGallery;
    }

    var flickr = new Galleria.Flickr();
    $("#flickrImages").click(function() {
        flickr.setOptions({
            max: 20,
            description:true
        })._find({tags:window.params.species.name}, function(data) {
            if(data.length != 0) {
                if(!flickrGallery) {
                    flickrGallery = createFlickrGallery(data);
                } else {
                    flickrGallery.load(data);
                }	    			
            } else {
                //$("#flickrImages").hide();
                //$("#resourceTabs-3").hide();
                //$("#googleImages").click();				  	
            }
        });
    });

    var imageSearch;
    var googleGallery;
    var createGoogleGallery = function() {
        // Create a search control
        var searchControl = new google.search.SearchControl();
        // Add in a full set of searchers
        imageSearch = new google.search.ImageSearch();
        imageSearch.setResultSetSize(8);
        imageSearch.setNoHtmlGeneration();
        google.search.Search.getBranding(document.getElementById("googleBranding"));
        $('#gallery4').galleria({
            height:400,
            carousel:true,
            transition:'pulse',
            image_pan_smoothness:5,
            showInfo:true,
            dataSource:[],
            debug: false,
            clicknext:true,
            dataConfig: function(img) {
                return {
                    description: $(img).next('.notes').html() 
                };
            }, extend: function(options) {
                this.bind("loadstart", function(e) {
                    if ( (e.index + 1) % 8 === 0 && e.index < 64 	) {
                        getGoogleImages(imageSearch, (e.index + 1) / 8);
                    }					            
                });
            }
        });

        //TODO:some dirty piece of code..find a way to get galleries by name
        var galleries = Galleria.get();
        return galleries[galleries.length - 1];
    }
    $("#googleImages").click(function() {
        if(!googleGallery) {
            googleGallery = createGoogleGallery();
        }
        if(!googleGallery.getData()) {
            $( "#resourceTabs-4 input:submit").button();
            imageSearch.execute(window.params.species.name);
            getGoogleImages(imageSearch, 0);
        }	
    });
}


$.fn.editable.defaults.mode = 'inline';

function onEditableSuccess(response, newValue) {
    if(!response) {
        return "Unknown error!";
    }          
    if(!response.success) {
        var $a = $(this).next(".alert-error")
            if( $a.length == 0) {
                $(this).after('<div class="alert-error">'+response.msg+'</div>');
            } else {
                $a.html(response.msg).show();
            }            
        return '';//response.msg
    } else {
        $(this).next(".alert-error").hide();
        $(this).effect("highlight", {color: '#4BADF5'}, 2000);
    }
}

function onEditableError(response, newValue) {
    var successHandler = this.success, errorHandler;
    handleError(response, undefined, undefined, function(data){
        $ele.find('.editField').editable('submit');
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
}

function initEditor($ele) {
    if($ele.length > 0 && $ele.attr('data-type') == 'ckeditor') {
        var textareaId = $ele.attr('id');
        var editor = CKEDITOR.instances[textareaId];
        if(editor) {
            if(editor.container.isVisible()) {
                editor.container.hide();
            } else {
                editor.container.show();
            }
        } else {
            CKEDITOR.replace(textareaId, config);
        }
    } else {
        $ele.editable('toggle');
    }
}

function initEditables($ele) {
    if($ele == undefined) $ele = $(document);
    $ele.find('.editField').editable({
        params: function(params) {
            if(params.name == 'synonym') {
                //collecting additional params like relationship for synonym
                var o = $(this).parent().parent().find('.synRel.selector').editable('getValue');
                if(o) params.relationship = o.relationship;
                params.sid = $(this).data('sid');
            } else if(params.name == 'commonname') {
                //collecting additional params like language for commonname
                var o = $(this).closest('li').find('.lang.selector').editable('getValue');
                if(o) params.language = o.language;
                params.cid = $(this).data('cid');
            }
            return params;
        },
        inputClass:'input-block-level',
        display: onAddableDisplay,
        success: onEditableSuccess,
        error:onEditableError,
        savenochange:true,
        onblur: 'ignore'
    }).on('shown', function(e, editable) {
        $(this).parent().parent().find('.synRel.selector, .lang.selector').editable('show'); 
    }).on('hidden', function(e, reason) {
        var editable = $(this).data('editable');
        $(this).prev().show().prev().show();
        $(this).parent().parent().find('.synRel.selector, .lang.selector').editable('hide'); 
    });
    
    $ele.find(".editField.editable, .ck_desc").before("<a class='pull-right deleteFieldButton btn btn-danger' title='"+window.i8ln.button.delete+"'><i class='icon-trash'></i>"+window.i8ln.button.delete+"</a><a class='pull-right editFieldButton btn btn-primary' title='"+window.i8ln.button.edit+"'><i class='icon-edit'></i>"+window.i8ln.button.edit+"</a>");
    $ele.find('.editFieldButton').click(function(e){    
        e.stopPropagation();

        var $textarea = $(this).nextAll('textarea');
        var $editable = $(this).nextAll('.editField.editable')
        if($textarea.length != 0)
        initEditor($textarea);
        else {
            initEditor($editable);
            $(this).parent().parent().find('.synRel.selector').editable('show');
            $(this).parent().parent().parent().find('.lang.selector').editable('show'); 
            $(this).hide().prev().hide();
        }
    });

    $ele.find('.deleteFieldButton').click(function(e) {
        var c = confirm(window.i8ln.species.abstracteditabletype.del)
        if(c == true) {
            var $f =  $(this).nextAll('.editField.editable');
            var $textarea = $(this).nextAll('textarea');

            if($textarea.length != 0) $f = $textarea;

            var d = $f.data();
            var params = {};
            if(d.params) $.extend(params, $.fn.editableutils.tryParseJson(d.params, true));
            if(d.cid) params.cid = d.cid;
            if(d.sid) params.sid = d.sid;

            $.extend(params, {'name':d.name, 'pk':d.pk, 'act':'delete'});

            $.ajax({
                url:d.url?d.url:window.params.species.updateUrl,
                type:'POST',
                data:params,
                context:$f,
                success : function( data, textStatus, jqXHR) {
                    onEditableSuccess.call($f, data, jqXHR);
                    if(data.success == true) {
                        if(data.type == 'description' || data.type == 'newdescription') {
                            var textareaId = $f.attr('id');
                            var editor = CKEDITOR.instances[textareaId];
                            if(editor) {
                                editor.destroy(false);
                            }

                            onAddableDisplay(undefined, data, jqXHR, $f.parent());
                        } else {
                            onAddableDisplay(undefined, data, jqXHR, $f);
                        }
                    }
                }
            });

        }
    });
}

function onAddableDisplay(value, sourceData, response, context) {
    var me = $(this);
    if(context) me = context
        var html = [];
    if(sourceData && sourceData.content) {
        var speciesId = sourceData.speciesId?sourceData.speciesId:'';
        var content = sourceData.content;
        var data_type = 'textarea';
        if(sourceData.type == 'description' || sourceData.type == 'newdescription') {
            data_type = 'ckeditor';
            if(content.length == 0 )
                html.push ('<li><textarea id="description_'+sourceData.id+'"  name="description_'+sourceData.id+'" class="ck_desc_add" data-type="'+data_type+'" data-pk="'+sourceData.id+'" data-speciesId="'+speciesId+'" data-url="'+window.params.species.updateUrl+'" data-name="newdescription" data-original-title="Add '+sourceData.type+' name" style="display:none;"/></li>'); 
        }

        $.each(content, function(i, v) { 
            switch(sourceData.type) {
                case 'contributor' :
                case 'attribution' :
                    html.push (createContributor(v, sourceData, v));
                    break;
                case 'reference' :
                    html.push (createReference(v, sourceData));
                    break;
                case 'description' : 
                case 'newdescription' : 
                    data_type = 'ckeditor';
                    html.push (v);//createSpeciesFieldHtml(v, sourceData));
                    break;
                case 'synonym' : 
                    data_type = 'text';
                    html.push (createSynonym(v, sourceData));
                    break;
                case 'commonname' : 
                    data_type = 'text';
                    html.push (createCommonname(v, sourceData));
                    break;

            }
        });

        var $ul = me.parent().parent();

        if(sourceData.type == 'synonym') {
            data_type = 'text';
            html.push('<li><div class="span3"><a href="#" class="synRel add_selector selector" data-type="select" data-name="relationship" data-original-title="Edit Synonym Relationship"></a></div><div class="span8"><a href="#" class="addField" data-type="'+data_type+'" data-pk="'+sourceData.id+'" data-rows="2" data-url="'+window.params.species.updateUrl+'" data-name="'+sourceData.type+'" data-original-title="Add '+sourceData.type+' name"></a></div></li>');
            $ul = me.parent().parent().parent();

        } else if(sourceData.type == 'commonname') {
            data_type = 'text';
            html.push('<li><div class="span3"><a href="#" class="lang add_selector selector" data-type="select" data-name="language" data-original-title="Edit Commonname Language"></a></div><div class="span8"><div><a href="#" class="addField" data-type="'+data_type+'" data-pk="'+sourceData.id+'" data-rows="2" data-url="'+window.params.species.updateUrl+'" data-name="'+sourceData.type+'" data-original-title="Add '+sourceData.type+' name"></a></div></div></li>');
            $ul = me.parent().parent().parent().parent();

        } else if(sourceData.type != 'description' && sourceData.type != 'newdescription'){
            html.push ('<li><a href="#" class="addField" data-type="'+data_type+'" data-pk="'+sourceData.id+'" data-rows="2" data-url="'+window.params.species.updateUrl+'" data-name="'+sourceData.type+'" data-original-title="Add '+sourceData.type+' name"></a></li>'); 
        }

        /*$('html, body').animate({
            scrollTop: $ul.offset().top
        }, 2000);*/
        $ul.empty().html(html.join(' ')).effect("highlight", {color: '#4BADF5'}, 5000);
        $ul.find('.attributionContent').show();
        //$('body').scrollTo($ul);
        initNameEditables($ul);
    } else {
        //me.html('Add'); 
    }
    return window.i8ln.species.specie.sad;
}

function onAddableError(response, newValue) {
    var successHandler = this.success, errorHandler;
    handleError(response, undefined, undefined, function(data){
        $ele.find('.addField').editable('submit');
        return window.i8ln.species.abstracteditabletype.re;
    }, function(data) {
        if(data && data.status == 401) {
            return window.i8ln.species.abstracteditabletype.sub; 
        } else if(response.status === 500) {
            return window.i8ln.species.abstracteditabletype.un;
        } else {
            return response.responseText;
        }
    })
}

function initAddables($ele) {
    if($ele == undefined) $ele = $(document);
    $ele.find('.addField').editable({
        params: function(params) {
            if(params.name == 'synonym') {
                //collecting additional params like relationship for synonym
                var o = $(this).parent().parent().find('.synRel.selector').editable('getValue');
                if(o) params.relationship = o.relationship;
            }
            else if(params.name == 'commonname') {
                //collecting additional params like language for commonname
                var o = $(this).closest('li').find('.lang.selector').editable('getValue');
                if(o) params.language = o.language;
            }
    return params;
        },
        inputClass:'input-block-level',
        success: onEditableSuccess,
        display: onAddableDisplay,
        error:onAddableError,
        onblur:'ignore'
    })

    if($ele.find('.addFieldButton').length == 0)
        $ele.find('.ck_desc_add').before("<a class='addFieldButton' title='"+window.i8ln.species.specie.adont+"'><i class='icon-plus'></i>"+window.i8ln.species.specie.adont+"</a>");


    $ele.find('.addFieldButton').click(function(e){    
        e.stopPropagation();
        var $textarea = $(this).nextAll('textarea');
        if($textarea)
        initEditor($textarea);
        else
        console.log('no textarea found');
    })

    $ele.find('.addField').each(function(){
        if($(this).attr('data-type') == 'ckeditor') {
            //initEditor($(this));
        } else {
            $(this).editable('show');
        }
    });
}

function createContributor(content, sourceData) {
    return '<li><a href="#" class="editField" data-type="textarea" data-rows="2" data-pk="'+sourceData.id+'" data-params="{cid:'+content.id+'}" data-url="'+window.params.species.updateUrl+'" data-name="'+sourceData.type+'" data-original-title="Edit '+sourceData.type+' name">'+ $.fn.editableutils.escape(content.name)+'</a></li>' ;
}

function createReference(content, sourceData) {
    return '<li><a href="#" class="editField" data-type="textarea" data-rows="2" data-pk="'+sourceData.id+'" data-params="{cid:'+content.id+'}" data-url="'+window.params.species.updateUrl+'" data-name="'+sourceData.type+'" data-original-title="Edit '+sourceData.type+' name">'+ $.fn.editableutils.escape(content.title)+'</a></li>' ;
}

function createSynonym(content, sourceData) {
    return '<li><div class="span3"><a href="#" class="synRel span3 '+(content.isContributor?'selector':'')+'" data-type="select" data-name="relationship" data-original-title="Edit Synonym Relationship">'+content.relationship.name+'</a></div><div class="span8"><a href="#" class="'+(content.isContributor?'editField':'')+'" data-type="text" data-pk="'+sourceData.id+'" data-sid="'+content.id+'" data-url="'+window.params.species.updateUrl+'" data-name="'+sourceData.type+'" data-original-title="Edit '+sourceData.type+' name">'+ content.italicisedForm+'</a></div></li>' ;
}

function createCommonname(content, sourceData) {
    if(content.language == undefined) {
        content.language = {name:'Others'};
    }
    return '<li><div class="span3"><a href="#" class="lang span3 '+(content.isContributor?'selector':'')+'" data-type="select" data-name="language" data-original-title="Edit Common name Language">'+content.language.name+'</a></div><div class="span8" style="display:table"><div style="display:table-row;"><a href="#" class="common_name '+(content.isContributor?'editField':'')+' " data-type="text" data-pk="'+sourceData.id+'" data-cid="'+content.id+'" data-url="'+window.params.species.updateUrl+'" data-name="'+sourceData.type+'" data-original-title="Edit '+sourceData.type+' name">'+ content.name+'</a>,</div></div></li>' ;
}

function createSpeciesFieldHtml(content, sourceData) {
    var toolbar = createMetadataToolbar(content);
    return '<div class="contributor_entry"><div href="#" class="ck_desc" data-type="ckeditor" data-pk="'+content.id+'"  data-url="'+window.params.species.updateUrl+'" data-name="'+sourceData.type+'" data-original-title="Edit '+sourceData.type+' name">'+content.description+'</div>'+toolbar+'</div>'; 
}

function createMetadataToolbar(content, sourceData) {
    var html = [];
    html.push("<div class='toolbar'>");
    $.each(content.contributors, function(i, v) {
        html.push (createContributor(v, sourceData));
    });
    $.each(content.attributors, function(i, v) {
        html.push (createContributor(v, sourceData));
    });
    html.push('</div>');
    return html.join(' ');
}

/*var onSelectorSuccess = function(response, newValue) {
  if(!response) {
  return "Unknown error!";
  }          

  if(!response.success) {
  $(this).next(".alert-error").html(response.msg).show();
  return response.msg
  } else {
  $(this).next(".alert-error").hide();
  $(this).effect("highlight", {color: '#4BADF5'}, 2000);
  }
  }*/

var onSelectorError =  function(response, newValue) {
    var successHandler = this.success, errorHandler;
    handleError(response, undefined, undefined, function(data){
        $ele.find('.selector').editable('submit');
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
}
function initLicenseSelector($ele, $selectorOptions, defaultValue) {
    if($ele == undefined)
        $ele = $(document);
    $ele.find('.license.selector').editable({
        value: defaultValue,    
        showbuttons:false,
        source: $selectorOptions,
        inputClass:'input-block-level',
        success: onEditableSuccess,
        error:onSelectorError,
        onblur:'ignore'
    });


    $ele.find(".license.selector.editable").editable('toggle');
    /*.before("<a class='pull-right editFieldButton' title='Edit'><i class='icon-edit'></i></a>");
      $ele.find('.editFieldButton').click(function(e){    
      e.stopPropagation();
      $(this).next('.license.selector.editable').editable('toggle');
      });*/
}

function initAudienceTypeSelector($ele, $selectorOptions, defaultValue) {
    if($ele == undefined)
        $ele = $(document);
    $ele.find('.audienceType.selector').editable({
        value: defaultValue,    
        showbuttons:false,
        source: $selectorOptions,
        inputClass:'input-block-level',
        success: onEditableSuccess,
        error:onSelectorError,
        onblur:'ignore'
    });


    $ele.find(".audienceType.selector.editable").editable('toggle');
    /*.before("<a class='pull-right editFieldButton' title='Edit'><i class='icon-edit'></i></a>");
      $ele.find('.editFieldButton').click(function(e){    
      e.stopPropagation();
      $(this).next('.audienceType.selector.editable').editable('toggle');
      });*/
}

function initStatusSelector($ele, $selectorOptions, defaultValue) {
    if($ele == undefined)
        $ele = $(document);
    $ele.find('.status.selector').editable({
        value: defaultValue,    
        showbuttons:false,
        source: $selectorOptions,
        success: onEditableSuccess,
        error:onSelectorError,
        inputClass:'input-block-level',
        onblur:'ignore'
    });


    $ele.find(".status.selector.editable").editable('toggle');
    /*.before("<a class='pull-right editFieldButton' title='Edit'><i class='icon-edit'></i></a>");
      $ele.find('.editFieldButton').click(function(e){    
      e.stopPropagation();
      $(this).next('.status.selector.editable').editable('toggle');
      });*/
}

function initSynRelSelector($ele, $selectorOptions, defaultValue) {
    if($ele == undefined)
        $ele = $(document);
    var $selector = $ele.find('.synRel.selector');
    if($selector.text()) 
        defaultValue = $selector.text();
    $selector.editable({
        value: defaultValue,    
        showbuttons:false,
        source: $selectorOptions,
        error:onSelectorError,
        inputClass:'input-block-level',
        onblur: 'ignore',
        send:'never'
    }).on('shown', function(e, editable) {
        editable.input.$input.val($.trim(editable.$element.text()));
    }).on('hidden', function(e, reason) {
         if(reason === 'save') {
            $(this).data('editable').show();
         }
    });

    $ele.find('.synRel.add_selector').editable('toggle');

}

function initLangSelector($ele, $selectorOptions, defaultValue) {
    if($ele == undefined)
        $ele = $(document);
    $ele.find('.lang.selector').editable({
        value: defaultValue,    
        showbuttons:false,
        source: $selectorOptions,
        error:onSelectorError,
        inputClass:'input-block-level',
        onblur: 'ignore',
        send:'never'
    }).on('shown', function(e, editable) {
        editable.input.$input.val($.trim(editable.$element.text()));
    }).on('hidden', function(e, reason) {
         if(reason === 'save') {
            $(this).data('editable').show();
         }
    });

    $ele.find('.lang.add_selector').editable('toggle');

}


function selectLicense($this, i) {
    var licenseDiv = $this.closest(".license_div");
    $(licenseDiv).find("input").val($.trim($this.text()));
    $(licenseDiv).find('img:first').replaceWith($this.html());
    //$('#license_'+i).val($.trim($this.text()));
    //$('#selected_license_'+i).find('img:first').replaceWith($this.html());
    return false;}

var initNameEditables = function ($e) {
    initEditables($e);
    initAddables($e);
    initSynRelSelector($e, synRelSelectorOptions, "Synonym");
    initLangSelector($e, langSelectorOptions, "English");
    $('#commonNames .entry').removeClass('pull-left');
}


$(document).ready(function() {

    $(".readmore").readmore({
        substr_len : 400,
        more_link : '<a class="more readmore">&nbsp;More</a>'
    });

    /*$("#toc").tocify({
      selectors:'h5,h6',
      }).data("toc-tocify");*/

    //$("#tocContainer").affix();

    $('li.poor_species_content').hover(function(){
        $(this).children('.poor_species_content').slideDown(200);
    }, function(){
        $(this).children('.poor_species_content').slideUp(200);
    });
    $(".grid_view").toggle();


    initGalleryTabs();

    try {
        $(".contributor_ellipsis").trunk8();
    } catch(e) {
        console.log(e)
    } 
    
    $('.thumbwrap .figure').hover(function() {
        $(this).children('.attributionBlock').css('visibility', 'visible');
    }, function(){
        $(this).children('.attributionBlock').css('visibility', 'hidden');
    });
    
    if(typeof speciesId != 'undefined') {
        var taxonBrowser = $('.taxonomyBrowser').taxonhierarchy({
            speciesId:speciesId,
            //speciesLevel:${TaxonomyRank.SPECIES.ordinal()},
            expandAll:true,
            expandSpecies:true
        });	

        var speciesfields = $('body').speciesfield();

        var initEditableFields = function(e) {
            if($(document).find('.editFieldButton').length == 0) {
                refreshEditables($('body'));
                //$("#requestPermission").show();
                //$("#inviteCurators").show();
                //$("#inviteContributors").show();
            } else {
                /*    $('.editable').editable('disable');
                      $('.addField').hide();
                      $('.editFieldButton').hide();
                      */ 
                window.location = window.location.href.split("?")[0];
                //window.open(window.location.href);
            }
            if(e) e.stopPropagation();
        }

        var refreshEditables = function($e) {
            //initEdit($e);
            //
            for (var i=0; i < speciesfields.length; i++) {
                speciesfields[i].initEditables('.ck_desc', '.dummy.speciesField');
            }

            for (var i=0; i < taxonBrowser.taxonHierarchies.length; i++) {
                taxonBrowser.taxonHierarchies[i].initEditables('#taxaHierarchy #taxonHierarchy.editField', '#taxaHierarchy #taxonHierarchy.emptyField');
            }
            //initLicenseSelector($e, licenseSelectorOptions, "CC BY");
            //initAudienceTypeSelector($e, audienceTypeSelectorOptions, "General Audience");
            //initStatusSelector($e, statusSelectorOptions, "Under Validation");

            initNameEditables($('#synonyms,#commonNames'));

            $('.emptyField').show();
            //$('.hidePoint').show();
            $('#editSpecies').addClass('editing').html('<i class="icon-edit"></i>'+window.i8ln.species.specie.eem);
            if($e) rate($e.find('.star_rating'));
        }

        $('#editSpecies').click(function() {
            initEditableFields();
            return false;
        });

        if($('#deleteSpecies').length > 0) {
            $('#deleteSpecies').click (function() {
                if(confirm(window.i8ln.species.specie.sdel)) {
                    $.ajax({
                        url:window.params.species.deleteUrl,
                        type:'POST',
                        success : function( data, textStatus, jqXHR) {
                           //alert(data); 
                        },error: function(xhr, status, error) {
                            //alert(msg);
                            return false;
                        }
                    });
                }
                return false;
            });
        }

        var editMode = getParameterByName('editMode');

        if(editMode == "true" || editMode == "1") {
            $('#editSpecies').click();
        }

    }
});

