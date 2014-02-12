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
        title : 'Occurrence',
    layers : 'ibp:occurrence',
    styles : '',
    cql_filter : "species_name='" + speciesName + "'",
    opacity : 0.7
    },
    {
        title : 'Observation',
    layers : 'ibp:observation_locations',
    styles : '',
    cql_filter : "species_name='" + speciesName + "'",
    opacity : 0.7
    },
    {
        title : 'Checklist',
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

    if($("#resourceTabs-1 img").length > 0) {

        //TODO:load gallery  images by ajax call getting response in json  
        $('.gallery').galleria({
            height : 400,
            preload : 1,
            carousel : false,
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
                    galleryImageLoadFinish();
                })

            }

        });	
        Galleria.ready(function() {
            $("#gallerySpinner").hide();
            $("#resourceTabs").css('visibility', 'visible');
            $(".galleria-thumbnails-container").hide();
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
            $(this).next(".alert-error").html(response.msg).show();
            return response.msg
        } else {
            $(this).next(".alert-error").hide();
            $(this).effect("highlight", {color: '#4BADF5'}, 2000);
        }
    }

    function onEditableError(response, newValue) {
        var successHandler = this.success, errorHandler;
        handleError(response, undefined, undefined, function(data){
            $ele.find('.editField').editable('submit');
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

    function initEditables($ele) {
        if($ele == undefined)
            $ele = $(document);
        $ele.find('.editField').editable({
/*            wysihtml5 : {
                "font-styles": true, //Font styling, e.g. h1, h2, etc. Default true
            "emphasis": true, //Italics, bold, etc. Default true
            "lists": true, //(Un)ordered lists, e.g. Bullets, Numbers. Default true
            "html": true, //Button which allows you to edit the generated HTML. Default false
            "link": true, //Button to insert a link. Default true
            "image": true, //Button to insert an image. Default true,
            "color": false //Button to change color of font
            },*/
            success: onEditableSuccess,
            error:onEditableError,
            onblur: 'ignore'
        });

        $ele.find(".editField.editable").before("<a class='pull-right editFieldButton'><i class='icon-edit'></i>Edit</a>");
        $ele.find('.editFieldButton').click(function(e){    
            e.stopPropagation();
            $(this).next('.editField.editable').editable('toggle');
        });
    }

    function onAddableDisplay(value, sourceData, response) {
        var html = [];
        if(sourceData && sourceData.content) {
            var speciesId = sourceData.speciesId?sourceData.speciesId:'';
            var content = sourceData.content;
            var data_type = 'text';
            if(sourceData.type == 'description') {
                data_type = 'wysihtml5';
                html.push ('<li></i><a href="#" class="addField" data-type="'+data_type+'" data-pk="'+sourceData.id+'" data-params="[speciesId:'+speciesId+'"] data-url="'+window.params.species.updateUrl+'" data-name="'+sourceData.type+'" data-original-title="Add '+sourceData.type+' name">Add</a></li>'); 
            }

            $.each(content, function(i, v) { 
                switch(sourceData.type) {
                    case 'contributor' :
                    case 'attributor' :
                        html.push (createContributor(v, sourceData, v));
                        break;
                    case 'reference' :
                        html.push (createReference(v, sourceData));
                        break;
                    case 'description' : 
                        data_type = 'wysihtml5';
                        html.push (v);//createSpeciesFieldHtml(v, sourceData));
                        break;
                }
            });

            if(sourceData.type != 'description'){
                html.push ('<li><a href="#" class="addField" data-type="'+data_type+'" data-pk="'+sourceData.id+'" ] data-url="'+window.params.species.updateUrl+'" data-name="'+sourceData.type+'" data-original-title="Add '+sourceData.type+' name">Add</a></li>'); 
            }


            var $ul = $(this).parent().parent();

            $ul.empty().html(html.join(' ')).effect("highlight", {color: '#4BADF5'}, 2000);
            $ul.find('.attributionContent').show();
            initEditables($ul);
            initAddables($ul);
            initLicenseSelector($ul, licenseSelectorOptions, "CC BY");
            initAudienceTypeSelector($ul, audienceTypeSelectorOptions, "General Audience");
            initStatusSelector($ul, statusSelectorOptions, "Under Validation");
            rate($ul.find('.star_rating'));
        } else {
            $(this).html('Add'); 
        }
        return 'Successfully added data';
    }

    function onAddableError(response, newValue) {
        var successHandler = this.success, errorHandler;
        handleError(response, undefined, undefined, function(data){
            $ele.find('.addField').editable('submit');
            return "Please resubmit the form again";
        }, function(data) {
            if(data && data.status == 401) {
                return "Please login and resubmit the changes"; 
            } else if(response.status === 500) {
                return 'Service unavailable. Please try later.';
            } else {
                return response.responseText;
            }
        })
    }

    function initAddables($ele) {
        if($ele == undefined)
            $ele = $(document);
        var editor = $ele.find('.addField').editable({
/*            wysihtml5 : {
                "font-styles": true, //Font styling, e.g. h1, h2, etc. Default true
                "emphasis": true, //Italics, bold, etc. Default true
                "lists": true, //(Un)ordered lists, e.g. Bullets, Numbers. Default true
                "html": true, //Button which allows you to edit the generated HTML. Default false
                "link": true, //Button to insert a link. Default true
                "image": true, //Button to insert an image. Default true,
                "color": false //Button to change color of font
            },*/
            success: function(response, newValue) {
                if(!response) {
                    return "Unknown error!";
                }          

                if(!response.success) {
                    $(this).next(".alert-error").html(response.msg).show();
                    return response.msg
                } else {
                    $(this).next(".alert-error").hide();
                }
            },
            display: onAddableDisplay,
            error:onAddableError,
            onblur:'ignore'
       })

        $ele.find('.addField').each(function(){
            if($(this).attr('data-type') != 'wysihtml5') {
                $(this).editable('show');
                /*.on('shown', function(e, editable) {

                        console.log('onshown')
                        $('.wysihtml5-sandbox').css({'width':'100%', 'height':'100px', 'margin-bottom':'5px'});
                });;*/
            }
        });
    }

    function createContributor(content, sourceData) {
        return '<li><a href="#" class="editField" data-type="text" data-pk="'+sourceData.id+'" data-params="{cid:'+content.id+'}" data-url="'+window.params.species.updateUrl+'" data-name="'+sourceData.type+'" data-original-title="Edit '+sourceData.type+' name">'+ $.fn.editableutils.escape(content.name)+'</a></li>' ;
    }

    function createReference(content, sourceData) {
        return '<li><a href="#" class="editField" data-type="text" data-pk="'+sourceData.id+'" data-params="{cid:'+content.id+'}" data-url="'+window.params.species.updateUrl+'" data-name="'+sourceData.type+'" data-original-title="Edit '+sourceData.type+' name">'+ $.fn.editableutils.escape(content.title)+'</a></li>' ;
    }


    function createSpeciesFieldHtml(content, sourceData) {
        var toolbar = createMetadataToolbar(content);
        return '<div class="contributor_entry"><div href="#" class="editField description" data-type="wysihtml5" data-pk="'+content.id+'"  data-url="'+window.params.species.updateUrl+'" data-name="'+sourceData.type+'" data-original-title="Edit '+sourceData.type+' name">'+content.description+'</div>'+toolbar+'</div>'; 
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

    var onSelectorSuccess = function(response, newValue) {
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
    }

    var onSelectorError =  function(response, newValue) {
        var successHandler = this.success, errorHandler;
        handleError(response, undefined, undefined, function(data){
            $ele.find('.selector').editable('submit');
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
    var licenseSelectorOptions = [
                {value:"CC BY" , text: 'CC BY'},
                {value: "CC BY-NC", text: 'CC BY-NC'},
                {value: "CC BY-ND", text: 'CC BY-ND'},
                {value: "CC BY-NC-ND", text: 'CC BY-NC-ND'},
                {value: "CC BY-NC-SA", text: 'CC BY-NC-SA'},
                {value: "CC BY-SA", text: 'CC BY-SA'},
                {value: "CC PUBLIC DOMAIN", text: 'CC Public Domain'},
            ]
    var audienceTypeSelectorOptions = [
        {value: "Children", text:"Children"},
        {value: "General Audience", text:"General Audience"},
        {value: "Expert", text:"Expert"}
    ]
    var statusSelectorOptions = [
        {value: "Under Creation", text:"Under Creation"},
        {value: "Under Validation", text:"Under Validation"},
        {value: "Validated", text:"Validated"},
        {value: "Published", text:"Published"}
    ]

    function initLicenseSelector($ele, $selectorOptions, defaultValue) {
        if($ele == undefined)
            $ele = $(document);
        $ele.find('.license.selector').editable({
            value: defaultValue,    
            showbuttons:false,
            source: $selectorOptions,
            success: onSelectorSuccess,
            error:onSelectorError
        });


        $ele.find(".license.selector.editable").before("<a class='pull-right editFieldButton'><i class='icon-edit'></i>Edit</a>");
        $ele.find('.editFieldButton').click(function(e){    
            e.stopPropagation();
            $(this).next('.license.selector.editable').editable('toggle');
        });
    }

    function initAudienceTypeSelector($ele, $selectorOptions, defaultValue) {
        if($ele == undefined)
            $ele = $(document);
        $ele.find('.audienceType.selector').editable({
            value: defaultValue,    
            showbuttons:false,
            source: $selectorOptions,
            success: onSelectorSuccess,
            error:onSelectorError
        });


        $ele.find(".audienceType.selector.editable").before("<a class='pull-right editFieldButton'><i class='icon-edit'></i>Edit</a>");
        $ele.find('.editFieldButton').click(function(e){    
            e.stopPropagation();
            $(this).next('.audienceType.selector.editable').editable('toggle');
        });
    }

    function initStatusSelector($ele, $selectorOptions, defaultValue) {
        if($ele == undefined)
            $ele = $(document);
        $ele.find('.status.selector').editable({
            value: defaultValue,    
            showbuttons:false,
            source: $selectorOptions,
            success: onSelectorSuccess,
            error:onSelectorError
        });


        $ele.find(".status.selector.editable").before("<a class='pull-right editFieldButton'><i class='icon-edit'></i>Edit</a>");
        $ele.find('.editFieldButton').click(function(e){    
            e.stopPropagation();
            $(this).next('.status.selector.editable').editable('toggle');
        });
    }


function initEditableFields(e) {
    if($(document).find('.editFieldButton').length == 0) {
        initEditables();
        initAddables();
        initLicenseSelector(undefined, licenseSelectorOptions, "CC BY");
        initAudienceTypeSelector(undefined, audienceTypeSelectorOptions, "General Audience");
        initStatusSelector(undefined, statusSelectorOptions, "Under Validation");
        $('.emptyField').show();

        $('#editSpecies').text('Exit Edit Mode');
    } else {
    /*    $('.editable').editable('disable');
        $('.addField').hide();
        $('.editFieldButton').hide();
    */  
        window.location.reload(true);
    }
    e.stopPropagation();
}

function selectLicense($this, i) {
    $('#license_'+i).val($.trim($this.text()));
    $('#selected_license_'+i).find('img:first').replaceWith($this.html());
    return false;
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
    //loadIFrame();
    //initializeCKEditor();	
    // bind click event on delete buttons using jquery live
    $('.del-reference').live('click', deleteReferenceHandler);
    //  	if($("#resourceTabs-1 img").length === 0) {
    //  		$("#flickrImages").click();
    //  	}

    $('.thumbwrap .figure').hover(function() {
        $(this).children('.attributionBlock').css('visibility', 'visible');
    }, function(){
        $(this).children('.attributionBlock').css('visibility', 'hidden');
    });

});

