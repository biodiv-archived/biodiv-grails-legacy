<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="species.utils.ImageType"%>
<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@page import="species.Resource.ResourceType"%>
<%@ page import="species.Species"%>
<%@ page import="species.Classification"%>
<%@ page import="species.Synonyms"%>
<%@ page import="species.CommonNames"%>
<%@ page import="species.Language"%>
<%@page import="species.utils.Utils"%>
<%@page import="species.participation.Featured"%>
<%@page import="species.participation.Observation"%>
<%@page import="species.participation.ActivityFeedService"%>
<%@page import="org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils"%>

<html>
<head>
<g:set var="canonicalUrl" value="${uGroup.createLink([controller:'species', action:'show', id:speciesInstance.id, base:Utils.getIBPServerDomain()])}"/>
<g:set var="title" value="${speciesInstance.taxonConcept.name}"/>
<g:set var="description" value="${Utils.stripHTML(speciesInstance.notes()?:'')}" />
<%
def r = speciesInstance.mainImage();
def imagePath = '';
if(r) {
    def gThumbnail = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.gallery.suffix)?:null;
    if(r && gThumbnail) {
            if(r.type == ResourceType.IMAGE) {
                    imagePath = g.createLinkTo(base:grailsApplication.config.speciesPortal.resources.serverURL,	file: gThumbnail)
            } else if(r.type == ResourceType.VIDEO){
                    imagePath = r.thumbnailUrl()
            }
    }
}
%>

<g:render template="/common/titleTemplate" model="['title':title, 'description':description, 'canonicalUrl':canonicalUrl, 'imagePath':imagePath]"/>

<r:require modules="species_show"/>

<style>

.jcarousel-skin-ie7 .jcarousel-item .snippet.tablet .figure {
	width:210px;
	height:150px;
}

.jcarousel-skin-ie7 .jcarousel-item  .thumbnail .figure a {
	max-width:210px;
	max-height:150px;
}

.jcarousel-skin-ie7 .jcarousel-item  .thumbnail .img-polaroid {
	max-width:210px;
	max-height:140px;
}

.jcarousel-skin-ie7 .jcarousel-item {
	width:210px;
	height:250px;
}

.jcarousel-skin-ie7 .jcarousel-item .snippet.tablet {
	width:210px;
	height:250px;
}

.jcarousel-skin-ie7 .jcarousel-clip-horizontal {
	height:250px;
}

.jcarousel-skin-ie7 .jcarousel-item .snippet.tablet .caption {
	height:75px;
	padding:16px 0px;
	background-color : #fff;
}

</style>

<!--[if lt IE 8]><style>
.thumbwrap > li {
	width: 201px;
	w\idth: 200px;
	display: inline;
}
.thumbwrap {
	_height: 0;
	zoom: 1;
	display: inline;
}
.thumbwrap li .wrimg {
	display: block;
	width: auto;
	height: auto;
}
.thumbwrap .wrimg span {
	vertical-align: middle;
	height: 200px;
	zoom: 1;
}
</style><![endif]--> 


<g:set var="sparse" value="${Boolean.TRUE}" />
<g:set var="entityName"
	value="${message(code: 'species.label', default: 'Species')}" />
<g:set var="speciesName"
	value="${speciesInstance.taxonConcept.canonicalForm}" />

<g:set var="conceptCounter" value="${1}" />

<!-- 
<ckeditor:resources />
<script type="text/javascript" src="${resource(dir:'plugins',file:'ckeditor-3.6.0.0/js/ckeditor/_source/adapters/jquery.js')}"></script>
<g:javascript src="ckEditorConfig.js" />
-->

<script type="text/javascript"
		src="/sites/all/themes/wg/scripts/OpenLayers-2.10/OpenLayers.js"></script>
<script type="text/javascript" src="/sites/all/themes/wg/scripts/am.js"></script>
<g:javascript>

occurrenceCount = undefined
function getOccurrenceCount(data) {
	occurrenceCount = ${speciesInstance.fetchOccurrence()} + data.count;
}

window.is_species_admin = ${SpringSecurityUtils.ifAllGranted('ROLE_SPECIES_ADMIN')} 
</g:javascript>

<script type="text/javascript"
	src="/geoserver/ows?request=getOccurrenceCount&service=amdb&version=1.0.0&species_name=${speciesName}"></script>

<r:script>
google.load("search", "1");
Galleria.loadTheme('${resource(dir:'js/galleria/1.2.7/themes/classic/',file:'galleria.classic.min.js')}');

$(document).ready(function(){
	var tabs = $("#resourceTabs").tabs();
	
	$(".readmore").readmore({
		substr_len : 400,
		more_link : '<a class="more readmore">&nbsp;More</a>'
	});
        
        /*$("#toc").tocify({
            selectors:'h5,h6',
        }).data("toc-tocify");*/
        
        //$("#tocContainer").affix();
        
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
<%--                            this.bind('lightbox_image', function(e){--%>
<%--                                $(".galleria-lightbox-title").append('<a target="_blank" href="'+Galleria.get(0).getData()._biodiv_url+'">View Full Image</a>');--%>
<%--                            })--%>


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
			})._find({tags:'${speciesName}'}, function(data) {
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
			imageSearch.execute('${speciesName}');
			getGoogleImages(imageSearch, 0);
		}	    
	});
            
     if(${sparse}) {
    	 if(occurrenceCount > 0) {
    	 	showOccurence('${speciesName}');
            //$("#map .alert").html("Showing "+occurrenceCount+" occurrence records for <i>${speciesName}</i>.");
    	} else {
            $("#map .alert").html("Currently no occurrence records are available right now. Please check back with us after some time or provide us if you have any.");
            $('#map1311326056727').hide();
    	}
     } else {
    	 showSpeciesConcept($(".defaultSpeciesConcept").attr("id"))
    	 showSpeciesField($(".defaultSpeciesField").attr("id"))
     }
  	
  	// bind the method to Galleria.ready
	Galleria.ready(function(options) {
        // listen to when an image is shown
        this.bind('image', function(e) {
            // lets make galleria open a lightbox when clicking the main
			// image:
<%--            $(e.imageTarget).click(this.proxy(function() {--%>
<%--               this.openLightbox();--%>
<%--            }));--%>
        });
	});

  	//loadIFrame();
  	//initializeCKEditor();	
  	// bind click event on delete buttons using jquery live
  	$('.del-reference').live('click', deleteReferenceHandler);
//  	if($("#resourceTabs-1 img").length === 0) {
//  		$("#flickrImages").click();
//  	}
  	
  	$('.thumbwrap .figure').hover(
	  	function(){
	  		$(this).children('.attributionBlock').css('visibility', 'visible');
	  	
	  	},function(){
	  		$(this).children('.attributionBlock').css('visibility', 'hidden');
	  	}
	  );
  	
  	try {
		$(".contributor_ellipsis").trunk8();
	} catch(e) {
  		console.log(e)
	}  	

<%--  	//init editables --%>
<%--$('.myeditable').editable({--%>
<%--    url: '/post' //this url will not be used for creating new user, it is only for update--%>
<%--});--%>
<%-- --%>
<%--//make username required--%>
<%--$('#video').editable('option', 'validate', function(v) {--%>
<%--    if(!v) return 'Required field!';--%>
<%--});--%>
<%-- --%>
<%--//automatically show next editable--%>
<%--$('.myeditable').on('save.newuser', function(){--%>
<%--    var that = this;--%>
<%--    setTimeout(function() {--%>
<%--        $(that).closest('tr').next().find('.myeditable').editable('show');--%>
<%--    }, 200);--%>
<%--});--%>
<%--$('#save-btn').click(function() {--%>
<%--   $('.myeditable').editable('submit', { --%>
<%--       url: '${uGroup.createLink(controller:'species', action:'addResource', id:speciesInstance.id) }', --%>
<%--       ajaxOptions: {--%>
<%--           dataType: 'json'//assuming json response--%>
<%--           --%>
<%--       },          --%>
<%--       success: function(data, config) {--%>
<%--           if(data && data.id) {  //record created, response like {"id": 2}--%>
<%--               //set pk--%>
<%--               $(this).editable('option', 'pk', data.id);--%>
<%--               //remove unsaved class--%>
<%--               $(this).removeClass('editable-unsaved');--%>
<%--               //show messages--%>
<%--               var msg = 'New user created! Now editables submit individually.';--%>
<%--               $('#msg').addClass('alert-success').removeClass('alert-error').html(msg).show();--%>
<%--               $('#save-btn').hide(); --%>
<%--               $(this).off('save.newuser');                     --%>
<%--           } else if(data && data.errors){ --%>
<%--               //server-side validation error, response like {"errors": {"username": "username already exist"} }--%>
<%--               config.error.call(this, data.errors);--%>
<%--           }               --%>
<%--       },--%>
<%--       error: function(errors) {--%>
<%--           var msg = '';--%>
<%--           if(errors && errors.responseText) { //ajax error, errors = xhr object--%>
<%--               msg = errors.responseText;--%>
<%--           } else { //validation error (client-side or server-side)--%>
<%--               $.each(errors, function(k, v) { msg += k+": "+v+"<br>"; });--%>
<%--           } --%>
<%--           $('#msg').removeClass('alert-success').addClass('alert-error').html(msg).show();--%>
<%--       }--%>
<%--   });--%>
<%--});--%>
<%--$('#reset-btn').click(function() {--%>
<%--    $('.myeditable').editable('setValue', null)  //clear values--%>
<%--        .editable('option', 'pk', null)          //clear pk--%>
<%--        .removeClass('editable-unsaved');        //remove bold css--%>
<%--                   --%>
<%--    $('#save-btn').show();--%>
<%--    $('#msg').hide();                --%>
<%--});--%>
<%--$("#contributeVideo").click(function(){--%>
<%--	$(this).next("#contributeVideoForm").toggle();--%>
<%--});--%>

});

</r:script>
<style>
	 .container_16 {
	 	width:940px;
	 }
</style>
</head>

<body>

<div class="span12">
			<s:showSubmenuTemplate model="['entityName':speciesInstance.taxonConcept.italicisedForm , 'subHeading':CommonNames.findByTaxonConceptAndLanguage(speciesInstance.taxonConcept, Language.findByThreeLetterCode('eng'))?.name, 'headingClass':'sci_name']"/>
			
			<g:if test="${!speciesInstance.percentOfInfo}">
				<div
					class="poor_species_content alert">
					<i class="icon-info"></i>
					No information yet.
					
				</div>
			</g:if>
                        
                        <div class="span12" style="margin-left:0px">
                                   <g:render template="/common/observation/showObservationStoryActionsTemplate"
                                   model="['instance':speciesInstance, 'href':canonicalUrl, 'title':title, 'description':description, 'hideFlag':true, 'hideDownload':true]" />
                        </div>


                        <div class="span4 pull-right">
                            <t:showTaxonBrowser model="['speciesInstance':speciesInstance, 'expandSpecies':true, 'expandAll':false, 'speciesId':speciesInstance.taxonConcept?.id, expandAllIcon:false]"/>
                        </div>

			<!-- media gallery -->
			<div class="span8 right-shadow-box" style="margin:0px;">

				<div style="padding-bottom:10px;height:430px;">
                                    <center>
                                        <div id="gallerySpinner" class="spinner">
                                        <img src="${resource(dir:'images',file:'spinner.gif', absolute:true)}"
                                            alt="${message(code:'spinner.alt',default:'Loading...')}" />
                                        </div>
                                    </center>


					<div id="resourceTabs" style="visibility:hidden;">
						<ul>
							<li><a href="#resourceTabs-1">Images</a></li>
	<%--						<li><a href="#resourceTabs-2">Video</a></li>--%>
							<li><a id="flickrImages" href="#resourceTabs-3">Flickr Images</a></li>
	<%--						<li><a id="googleImages" href="#resourceTabs-4">Google Images</a></li>--%>
							
						</ul>
						<div id="resourceTabs-1">
	<%--						<a href="#">Contribute Images</a>--%>
                                                        <g:set var="images" value="${speciesInstance.getImages()}"/>
                                                        <div class="story-footer" style="right:0;bottom:55px;z-index:5;background-color:whitesmoke" >
                                                            <g:render template="/common/observation/noOfResources" model="['instance':speciesInstance, 'bottom':'bottom:55px;', noOfResources:[[ResourceType.IMAGE, images.size()]]]"/>
                                                        </div>
                                                        <div id="gallery1" class="gallery">
                                                                <g:if test="${images}">
									<s:showSpeciesImages model="['speciesInstance':speciesInstance]"></s:showSpeciesImages>
								</g:if>
								<g:else>
									<% def fileName = speciesInstance.fetchSpeciesGroupIcon(ImageType.LARGE)?.fileName; %>
									<img class="group_icon galleryImage" src="${createLinkTo(dir: 'images', file: fileName, absolute:true)}" 
								  		title="Contribute!!!"/>
								</g:else>
							
							</div>
						</div>
						
						
						<div id="resourceTabs-3">						
							<div id="gallery3"></div>
							<div id="flickrBranding"></div><br/>
							<div class="message ui-corner-all">These images are fetched from other sites and may contain some irrevelant images. Please use them at your own discretion.</div>
						</div>
						<!--div id="resourceTabs-4">
							
							<div id="gallery4"></div>
							<div id="googleBranding"></div><br/>
							<div class="message ui-corner-all">These images are fetched from other sites and may contain some irrevelant images. Please use them at your own discretion.</div>
							<div>
								<center>
								<form method="get" action="http://images.google.com/images"
									target="_blank">
									<input type="text" name="q" value='"${speciesName}"' />
									<input type="submit" value="Search Google Images" />
								</form>
								</center>
							</div>
						</div-->
						<div id="tagcloud"></div>
					</div>
				<!--  static species content -->
				</div>

                                <div style="background-color:white">
				
				<!-- species page icons -->
				<div style="padding:5px 0px;">
					<s:showSpeciesExternalLink model="['speciesInstance':speciesInstance]"/>
					<div class="observation-icons">		
						<img class="group_icon species_group_icon"  
							  	title="${speciesInstance.fetchSpeciesGroup()?.name}"
							 	 src='${createLinkTo(dir: 'images', file: speciesInstance.fetchSpeciesGroupIcon(ImageType.VERY_SMALL)?.fileName?.trim(), absolute:true)}'/>
							  
						<g:if test="${speciesInstance.taxonConcept.threatenedStatus}">
						  		<s:showThreatenedStatus model="['threatenedStatus':speciesInstance.taxonConcept.threatenedStatus]"/>
						</g:if>
					</div>
				</div>
				<div style="margin:5px 0px;">
					<g:each in="${speciesInstance.getIcons()}" var="r">
							<%def imagePath = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix)%>
							<img class="icon group_icon" href="${href}"
								src="${createLinkTo(file: imagePath, base:grailsApplication.config.speciesPortal.resources.serverURL)}"
								title="${r?.description}" />
					</g:each>
				</div>
                                <div class="readmore sidebar_section notes_view">
				    ${speciesInstance.notes() }
                                </div>
                            </div>
                        </div>
                        <uGroup:featureUserGroups model="['observationInstance':speciesInstance]"/>

                        <div class="span12" style="margin-left:0px">
				<%def nameRecords = fields.get(grailsApplication.config.speciesPortal.fields.NOMENCLATURE_AND_CLASSIFICATION)?.get(grailsApplication.config.speciesPortal.fields.TAXON_RECORD_NAME).collect{it.value.get('speciesFieldInstance')[0]} %>
				<g:if test="${nameRecords}">
                                <div class="sidebar_section" style="clear:both;">
                                    					<a class="speciesFieldHeader"  data-toggle="collapse" href="#taxonRecordName">
						<h5>Taxon Record Name</h5>
					</a>
					
					<div id="taxonRecordName" class="speciesField collapse in">
						<table>
						<tr class="prop">
								<td><span class="grid_3 name">${grailsApplication.config.speciesPortal.fields.SCIENTIFIC_NAME }</span></td><td> ${speciesInstance.taxonConcept.italicisedForm}</td>
						</tr>
						<g:each in="${nameRecords}">
							<tr class="prop">
							 
								<g:if test="${it?.field?.subCategory?.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.REFERENCES)}">
									<td><span class="grid_3 name">${it?.field?.subCategory} </span></td> <td class="linktext">${it?.description}</td>
								</g:if> 
								<g:elseif test="${it?.field?.subCategory?.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.GENERIC_SPECIFIC_NAME)}">
									
								</g:elseif> 
								<g:elseif test="${it?.field?.subCategory?.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.SCIENTIFIC_NAME)}">
									
								</g:elseif> 
								<g:elseif test="${it?.field?.subCategory?.equalsIgnoreCase('year')}">
									<td><span class="grid_3 name">${it?.field?.subCategory} </span></td> <td> ${it?.description}</td>
								</g:elseif> 
								<g:else>
									<td><span class="grid_3 name">${it?.field?.subCategory} </span></td> <td> ${it?.description}</td>
								</g:else> 
							</tr>
						</g:each>
						</table>
					</div>
					<comment:showCommentPopup model="['commentHolder':[objectType:ActivityFeedService.SPECIES_TAXON_RECORD_NAME, id:speciesInstance.id], 'rootHolder':speciesInstance]" />
				</div>
				<br/>
				</g:if>
				
				<!-- Synonyms -->
				<%def synonyms = Synonyms.findAllByTaxonConcept(speciesInstance.taxonConcept) %>
				<g:if test="${synonyms }">
				<div class="sidebar_section">
					<a class="speciesFieldHeader"  data-toggle="collapse" href="#synonyms"> 
						<h5>Synonyms</h5>
					</a> 
					<div id="synonyms" class="speciesField collapse in">
						<table>
						<g:each in="${synonyms}" var="synonym">
						<tr><td class="prop">
							<span class="grid_3 name">${synonym?.relationship?.value()} </span></td><td class='sci_name'> ${(synonym?.italicisedForm)?synonym.italicisedForm:'<i>'+(synonym?.name)+'</i>'}  </td></tr>
						</g:each>
						</table>
					</div>
					<comment:showCommentPopup model="['commentHolder':[objectType:ActivityFeedService.SPECIES_SYNONYMS, id:speciesInstance.id], 'rootHolder':speciesInstance]" />
				</div>
				<br/>
				</g:if>
				
				<!-- Common Names -->
				<%
					Map names = new LinkedHashMap();
					CommonNames.findAllByTaxonConcept(speciesInstance.taxonConcept).each() {
						String languageName = it?.language?.name ?: "Others";
						
						if(it?.language?.isDirty) {
							languageName = "Others";	
						}
						if(!names.containsKey(languageName)) {
							names.put(languageName, new ArrayList());
						}
						names.get(languageName).add(it)
					};
				
					names = names.sort();
					names.each { key, list ->
						list.sort();						
					}
					
				%>
				<g:if test="${names}">
				<div class="sidebar_section">
					<a class="speciesFieldHeader" data-toggle="collapse" href="#commonNames"><h5> Common Names</h5></a> 
					<div id="commonNames" class="speciesField collapse in">
						
							<table>
								<g:each in="${names}">
								<tr><td class="prop">
									<span class="grid_3 name">${it.key} </span></td> 
									<td><g:each in="${it.value}"  status="i" var ="n">
												 <g:if test="${n.language?.isDirty}">${n.language.name+ " : "} </g:if>${n.name}<g:if test="${i < it.value.size()-1}">,</g:if>
											</g:each></td>
									</tr>
								</g:each>
							</table>
					</div>
					<comment:showCommentPopup model="['commentHolder':[objectType:ActivityFeedService.SPECIES_COMMON_NAMES, id:speciesInstance.id], 'rootHolder':speciesInstance]" />
				</div>
				<br/>
				</g:if>
				<!-- Common Names End-->
				
			
			
				<ul style="list-style: none;margin:0px;">
					<g:each in="${fields}" var="concept">
						<g:if
							test="${concept.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.TAXONRECORDID) || concept.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.GLOBALUNIQUEIDENTIFIER) || concept.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.NOMENCLATURE_AND_CLASSIFICATION)}">
						</g:if>
						<g:else>
							
							<g:if test="${sparse}">
								<li style="clear: both; margin-left: 0px">
							</g:if>
							<g:else>
								<li class="nav ui-state-default">
<%--									onClick="showSpeciesConcept('${conceptCounter}'); showSpeciesField('${conceptCounter}.${fieldCounter}')">--%>
							</g:else>
							<g:showSpeciesConcept
								model="['speciesInstance':speciesInstance, 'concept':concept, 'conceptCounter':conceptCounter, 'sparse':sparse, 'observationInstanceList':observationInstanceList, 'instanceTotal':instanceTotal, 'queryParams':queryParams, 'activeFilters':activeFilters, 'userGroupWebaddress':userGroupWebaddress]" />
							</li>
							<br/>
							<%conceptCounter++%>
						</g:else>
					</g:each>
				</ul>
			</div>			
			
			<g:if test="${!sparse}">
				<div id="speciesFieldContainer" class="grid_12"></div>
			</g:if>

	                <!-- right side bar -->
			<div class="span12 classifications" style="margin-left:0px;">
                            <!--div id="tocContainer" class="sidebar_section">
                                <div id="toc" class="tile"></div>
                            </div-->


                            <!--div id="map" class="sidebar_section">
                                <h5>Occurrence Map</h5>
                                    <div id="mapSpinner" class="spinner">
                                        <center>
                                            <img src="${resource(dir:'images',file:'spinner.gif', absolute:true)}"
                                                alt="${message(code:'spinner.alt',default:'Loading...')}" />
                                        </center>
                                    </div>


                               <div id="map1311326056727" class="occurenceMap"
                                    style="height: 350px; width: 100%"></div>
                                <div class="alert alert-info">
                                The current map showing distribution of species is only indicative.
                                </div>
 
                                <comment:showCommentPopup model="['commentHolder':[objectType:ActivityFeedService.SPECIES_MAPS, id:speciesInstance.id], 'rootHolder':speciesInstance]" />	

                            </div-->

                           <div class="sidebar_section">
                                <h5> Activity </h5>
                                    <div class="union-comment">
                                        <feed:showAllActivityFeeds model="['rootHolder':speciesInstance, feedType:'Specific', refreshType:'manual', 'feedPermission':'editable']" />
                                            <comment:showAllComments model="['commentHolder':speciesInstance, commentType:'super','showCommentList':false]" />
                                    </div>
                            </div>
 
                        </div>

		

</div>		
			
<g:javascript>
$(document).ready(function() {
	window.params.carousel = {maxHeight:150, maxWidth:210}
});
</g:javascript>	

</body>

</html>
