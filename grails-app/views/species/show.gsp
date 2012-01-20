<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@page import="species.Resource.ResourceType"%>
<%@ page import="species.Species"%>
<%@ page import="species.Classification"%>
<%@ page import="species.Synonyms"%>
<%@ page import="species.CommonNames"%>
<%@ page import="species.Language"%>
<html>
<head>
<meta name="layout" content="main" />


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

<link rel="stylesheet" type="text/css" media="all"
	href="${resource(dir:'js/galleria/1.2.6/themes/classic/',file:'galleria.classic.css', absolute:true)}" />

<g:set var="sparse" value="${Boolean.TRUE}" />
<g:set var="entityName"
	value="${message(code: 'species.label', default: 'Species')}" />
<g:set var="speciesName"
	value="${speciesInstance.taxonConcept.binomialForm}" />

<g:set var="conceptCounter" value="${1}" />

<g:javascript src="jquery/jquery.jqGrid-4.1.2/js/i18n/grid.locale-en.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />
<g:javascript src="jquery/jquery.jqGrid-4.1.2/js/jquery.jqGrid.min.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />

<script
	src="https://www.google.com/jsapi?key=ABQIAAAAk7I0Cw42MpifyYznFgPLhhRmb189gvdF0PvFEJbEHF8DoiJl8hRsYqpBTt5r5L9DCsFHIsqlwnMKHA"
	type="text/javascript"></script>

<script type="text/javascript"
	src="http://maps.google.com/maps/api/js?sensor=false"></script>
<script type="text/javascript"
	src="/sites/all/themes/wg/scripts/OpenLayers-2.10/OpenLayers.js"></script>
<script type="text/javascript" src="/sites/all/themes/wg/scripts/am.js"></script>

<g:javascript src="galleria/1.2.6/galleria-1.2.6.min.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />
<g:javascript src="galleria/1.2.6/plugins/flickr/galleria.flickr.min.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}"></g:javascript>

<g:javascript src="jquery.collapser/jquery.collapser.min.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />
<g:javascript src="floating-1.7.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />
<!-- 
<ckeditor:resources />
<script type="text/javascript" src="${resource(dir:'plugins',file:'ckeditor-3.6.0.0/js/ckeditor/_source/adapters/jquery.js', absolute:true)}"></script>
<g:javascript src="ckEditorConfig.js" />
-->
<g:javascript>

occurrenceCount = undefined
function getOccurrenceCount(data) {
	occurrenceCount = data.count;
}

</g:javascript>

<script type="text/javascript"
	src="/geoserver/ows?request=getOccurrenceCount&service=amdb&version=1.0.0&species_name=${speciesName}"></script>
<g:javascript src="species/main.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />


<g:javascript>
google.load("search", "1");
Galleria.loadTheme('${resource(dir:'js/galleria/1.2.6/themes/classic/',file:'galleria.classic.min.js', absolute:true)}');

$(document).ready(function(){
	var tabs = $("#resourceTabs").tabs();
	
	$(".readmore").readmore({
		substr_len : 400,
		more_link : '<a class="more readmore">&nbsp;More</a>'
	});

	if($("#resourceTabs-1 img").length > 0) {
	
		
	
		//TODO:load gallery  images by ajax call getting response in json  
		$('#gallery1').galleria({
			height : 400,
			preload : 1,
			carousel : true,
			transition : 'pulse',
			image_pan_smoothness : 5,
			showInfo : true,
			dataSelector : "img.galleryImage",
			debug : false,
			thumbQuality : false,
			maxScaleRatio : 1,
			minScaleRatio : 1,
	
			dataConfig : function(img) {
				return {
					// tell Galleria to grab the content from the .desc div as caption
					description : $(img).parent().next('.notes').html()
				};
			}
		});	
			
	} else {
		$("#resourceTabs").tabs("remove", 0);
	}

	$('div.speciesFieldHeader').collapser({
		target: 'next',
		effect: 'slide',
		changeText: false
		},function(){
			var ele = $(this);
			var x = ele.find(".ui-icon")
			if(ele.next('.speciesField').is(":visible")) {				 
				x.removeClass('ui-icon-circle-triangle-s').addClass('ui-icon-circle-triangle-e');
			} else {
				x.removeClass('ui-icon-circle-triangle-e').addClass('ui-icon-circle-triangle-s')
			}
		} , function(){
					
		}
	);

    $(".speciesField").each(function() {
	    if(jQuery.trim($(this).text()).length == 0) {
		    $(this).prev("div.speciesFieldHeader").children("span").removeClass("ui-icon ui-icon-circle-triangle-s")
		}
		// $(this).children(".toolbar").appendTo($(this).parent().children(".ui-dialog-titlebar"))
	})		

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
				  	$("#flickrImages").hide();
			    	$("#resourceTabs-3").hide();
				  	$("#googleImages").click();				  	
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
		$('#gallery2').galleria({
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
    		 $("#map .message").html("Showing "+occurrenceCount+" occurrence records for <i>${speciesName}</i>.");
    	} else {
    		$("#map .message").html("Currently no occurrence records for <i>${speciesName}</i> is available on the portal.");
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
            $(e.imageTarget).click(this.proxy(function() {
               this.openLightbox();
            }));
        });
	});

  	//loadIFrame();
  	//initializeCKEditor();	
  	// bind click event on delete buttons using jquery live
  	$('.del-reference').live('click', deleteReferenceHandler);
  	if(${speciesInstance.getImages()?.size()?:0} == 0) {
  		$("#flickrImages").click();
  	}
  	
  	$('.thumbwrap .figure').hover(
  	function(){
  		$(this).children('.attributionBlock').css('visibility', 'visible');
  	
  	},function(){
  		$(this).children('.attributionBlock').css('visibility', 'hidden');
  	});
  	
  	$('.attribution').mouseleave(function(){
  		$(this).hide();
  	});
  	
  	$('.helpContent').mouseleave(function(){
  		$(this).hide();
  	});
  	
});

</g:javascript>

<title>
	${speciesName}
</title>

</head>

<body>

	

		<div class="container_16">
			<g:if test="${flash.message}">
				<div
					class="ui-state-highlight ui-corner-all grid_10 prefix_3 suffix_3">
					<span class="ui-icon-info" style="float: left; margin-right: .3em;"></span>
					${flash.message}
				</div>
			</g:if>

			<div class="grid_16">

				<h2 style="padding: 5px; color: black; text-shadow: 1px 1px 2px #c6c6c6;">
					${speciesInstance.taxonConcept.italicisedForm }
				</h2>

				<h4><%=CommonNames.findByTaxonConceptAndLanguage(speciesInstance.taxonConcept, Language.findByThreeLetterCode('eng'))?.name%></h4>
			</div>

			<!-- media gallery -->
			<div class="grid_10">
				<div id="resourceTabs">
					<ul>
						<li><a href="#resourceTabs-1">Images</a></li>
						<li><a id="flickrImages" href="#resourceTabs-3">Flickr Images</a></li>
						<li><a id="googleImages" href="#resourceTabs-4">Google Images</a></li>
					</ul>
					<div id="resourceTabs-1">
						<div id="gallery1">
							<s:showSpeciesImages model="['speciesInstance':speciesInstance]"></s:showSpeciesImages>							
						</div>

					</div>
					<div id="resourceTabs-3">
						
						<div id="gallery3"></div>
						<div id="flickrBranding"></div><br/>
						<div class="message ui-corner-all">These images are fetched from other sites and may contain some irrevelant images. Please use them at your own discretion.</div>
					</div>
					<div id="resourceTabs-4">
						
						<div id="gallery2"></div>
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
					</div>

				</div>
				<br />
				<!-- species page icons -->
				<div class="grid_10">

							<div>
							
							<g:each in="${speciesInstance.getIcons()}" var="r">
									<img class="group_icon" href="${href}"
										src="${createLinkTo(dir: 'images/icons', file: r.fileName.trim(), absolute:true)}"
										title="${r?.description}" />
							</g:each>
							<g:each in="${speciesInstance.taxonConcept.externalLinks}" var="r">
								<g:each in="${['eolId', 'iucnId', 'gbif']}" var="extLinkKey">
									<g:if test="${r[extLinkKey]}">
										<s:showExternalLink model="['key':extLinkKey, 'externalLinks':r, 'taxonConcept':speciesInstance.taxonConcept]"/>										
									</g:if>	
								</g:each>									
							</g:each>
							<s:showExternalLink model="['key':'wikipedia', 'taxonConcept':speciesInstance.taxonConcept]"/>
							
							
							 <img class="group_icon species_group_icon" src="${createLinkTo(dir: 'images', file: speciesInstance.fetchSpeciesGroupIcon()?.fileName?.trim(), absolute:true)}" 
							  title="${speciesInstance.fetchSpeciesGroup()?.name}"/>
							  
							  <g:if test="${speciesInstance.taxonConcept.threatenedStatus}">
							  		<s:showThreatenedStatus model="['threatenedStatus':speciesInstance.taxonConcept.threatenedStatus]"/>
							  </g:if>
							</div>
					</div>
				<div id="tagcloud"></div>

			</div>

			<!--  static species content -->
			<div class="grid_6 classifications">
				<t:showTaxonBrowser model="['expandSpecies':true, 'expandAll':false, 'speciesId':speciesInstance.taxonConcept?.id]"/>
				<br />					

				<div class="readmore" style="float:left;">
					${speciesInstance.findSummary() }
				</div>
			</div>

			<br />
		</div>
		<br />

		<!-- species toc and content -->
		<div class="container_16" id="content">
			<div class="grid_16" style="float:left;margin-right: .3em;">
				<%def nameRecords = fields.get(grailsApplication.config.speciesPortal.fields.NOMENCLATURE_AND_CLASSIFICATION)?.get(grailsApplication.config.speciesPortal.fields.TAXON_RECORD_NAME).collect{it.value.get('speciesFieldInstance')} %>
				<g:if test="${nameRecords}">
				<div class="ui-widget">
					<div class="speciesFieldHeader ui-dialog-titlebar ui-helper-clearfix ui-widget-header">
						<span class="ui-icon ui-icon-circle-triangle-s" style="float: left; margin-right: .3em;"></span>
							<a href="#taxonRecordName"> Taxon Record Name</a> 
					</div>
					<div class="ui-widget-content">
						<table>
						<tr class="prop">
								<td><span class="grid_3 name">${grailsApplication.config.speciesPortal.fields.SCIENTIFIC_NAME }</span></td><td> ${speciesInstance.taxonConcept.italicisedForm}</td>
						</tr>
						<g:each in="${nameRecords}">
							<tr class="prop">
							 
								<g:if test="${it?.field?.subCategory?.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.REFERENCES)}">
									<td><span class="grid_3 name">${it?.field?.subCategory} </span></td> <td><a href="${it?.description}" target="_blank"> ${it?.description}</a></td>
								</g:if> 
								<g:elseif test="${it?.field?.subCategory?.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.GENERIC_SPECIFIC_NAME)}">
									
								</g:elseif> 
								<g:elseif test="${it?.field?.subCategory?.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.SCIENTIFIC_NAME)}">
									
								</g:elseif> 
								<g:else>
									<td><span class="grid_3 name">${it?.field?.subCategory} </span></td> <td> ${it?.description}</td>
								</g:else> 
							</tr>
						</g:each>
						</table>
					</div>
				</div>
				<br/>
				</g:if>
				
				<!-- Synonyms -->
				<%def synonyms = Synonyms.findAllByTaxonConcept(speciesInstance.taxonConcept) %>
				<g:if test="${synonyms }">
				<div class="ui-widget">
					<div class="speciesFieldHeader ui-dialog-titlebar ui-helper-clearfix ui-widget-header">
						<span class="ui-icon ui-icon-circle-triangle-s" style="float: left; margin-right: .3em;"></span>
						<a href="#synonyms"> Synonyms</a> 
					</div>
					<div class="ui-widget-content">
						<table>
						<g:each in="${synonyms}" var="synonym">
						<tr><td class="prop">
							<span class="grid_3 name">${synonym?.relationship?.value()} </span></td><td> ${synonym?.name}  </td></tr>
						</g:each>
						</table>
					</div>
				</div>
				<br/>
				</g:if>
				
				<!-- Common Names -->
				<%
					Map names = new LinkedHashMap();
					CommonNames.findAllByTaxonConcept(speciesInstance.taxonConcept).each(){
						String languageName = it?.language?.name ?: "Others";
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
				<div class="ui-widget">
				
					<div class="speciesFieldHeader ui-dialog-titlebar ui-helper-clearfix ui-widget-header">
						<span class="ui-icon ui-icon-circle-triangle-s" style="float: left; margin-right: .3em;"></span>
						<a href="#commonNames"> Common Names</a> 
					</div>
					<div class="ui-widget-content">
						
							<table>
								<g:each in="${names}">
								<tr><td class="prop">
									<span class="grid_3 name">${it.key} </span></td> 
									<td><g:each in="${it.value}"  status="i" var ="n">
												 ${n.name}<g:if test="${i < it.value.size()-1}">,</g:if>
											</g:each></td>
									</tr>
								</g:each>
							</table>
						
					</div>
				</div>
				<br/>
				</g:if>
				<!-- Common Names End-->
				
			
			
			</div>
			
			
			<div id="fieldstoc" class="<%=sparse?'grid_16':'grid_4'%>">
				<ul style="list-style: none;">
					<g:each in="${fields}" var="concept">
						<g:if
							test="${concept.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.TAXONRECORDID) || concept.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.GLOBALUNIQUEIDENTIFIER) || concept.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.NOMENCLATURE_AND_CLASSIFICATION)}">
						</g:if>
						<g:else>
							
							<g:if test="${sparse}">
								<li style="clear: both; margin-left: 0px">
							</g:if>
							<g:else>
								<li class="nav ui-state-default"
									onClick="showSpeciesConcept('${conceptCounter}'); showSpeciesField('${conceptCounter}.${fieldCounter}')">
							</g:else>
							<g:showSpeciesConcept
								model="['speciesInstance':speciesInstance, 'concept':concept, 'conceptCounter':conceptCounter, 'sparse':sparse]" />
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

	</div>
		
		
			
	

</body>

</html>
