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
	href="${resource(dir:'js/galleria/themes/classic/',file:'galleria.classic.css', absolute:true)}" />

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

<g:javascript src="galleria/galleria-1.2.4.min.js"
	base="${grailsApplication.config.grails.serverURL+'/js/'}" />

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
$(document).ready(function() {
	$("#resourceTabs").tabs();
	$(".readmore").readmore({
		substr_len : 400,
		more_link : '<a class="more readmore">&nbsp;More</a>'
	});

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
		},
		extend : function(options) {
			// listen to when an image is shown
			this.bind('image', function(e) {
				// lets make galleria open a lightbox when clicking the main
				// image:
				$(e.imageTarget).click(this.proxy(function() {
					this.openLightbox();
				}));
			});
		}

	});
});	

google.load("search", "1");
Galleria.loadTheme('${resource(dir:'js/galleria/themes/classic/',file:'galleria.classic.min.js', absolute:true)}');

$(document).ready(function(){

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

	$(".ui-icon-control").click(function() {
		var div = $(this).siblings("div.toolbarIconContent");
		if (div.is(":visible")) {
			div.hide(400);
		} else {
			div.slideDown("slow");	
			// div.css("float","right");
			if(div.offset().left < 0) {
				div.offset({left:div.parent().offset().left});					
			}
		}
	});

	$(".ui-icon-edit").click(function() {
		var ele =$(this).siblings("div.toolbarIconContent").find("textArea.fieldEditor");
		if(ele) { 
			ele.ckeditor(function(){}, {customConfig:"${resource(dir:'js',file:'ckEditorConfig.js', absolute:true)}"});
			CKEDITOR.replace( ele.attr('id') );
		}
	});

	$("a.ui-icon-close").click(function() {
		$(this).parent().hide("slow");
	})

    $(".speciesField").each(function() {
	    if(jQuery.trim($(this).text()).length == 0) {
		    $(this).prev("div.speciesFieldHeader").children("span").removeClass("ui-icon ui-icon-circle-triangle-s")
		}
		// $(this).children(".toolbar").appendTo($(this).parent().children(".ui-dialog-titlebar"))
	})		

	$(".defaultSpeciesConcept").prev("a").trigger('click');	

	
	$("#googleImages").click(function() {
		$( "#resourceTabs-4 input:submit").button();
		// Create a search control
		var searchControl = new google.search.SearchControl();

		// Add in a full set of searchers
		var imageSearch = new google.search.ImageSearch();
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
		            description: $(img).next('.notes').html() // tell Galleria
																// to grab the
																// content from
																// the .desc div
																// as caption
		        };
		    }, extend: function(options) {
		        // listen to when an image is shown
		        this.bind('image', function(e) {
		            // lets make galleria open a lightbox when clicking the main
					// image:
		            $(e.imageTarget).click(this.proxy(function() {
		               this.openLightbox();
		            }));
		        });
		        
		        this.bind("loadstart", function(e) {
		            if ( (e.index + 1) % 8 === 0 && e.index < 64 	) {
		            	getGoogleImages(imageSearch, (e.index + 1) / 8);
		            }					            
		        });
		    }
		});    
		imageSearch.execute('${speciesName}');
		getGoogleImages(imageSearch, 0);	    
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
  	
  	//loadIFrame();
  	//initializeCKEditor();	
  	// bind click event on delete buttons using jquery live
  	$('.del-reference').live('click', deleteReferenceHandler);
});

$("#taxaHierarchy").change(function() {
	console.log($(this))
	console.log($('#taxaHierarchy option:selected').val());
});

function showTaxonHierarchy(taxonHierarchyId, classificationName) {
	var taxonHierarchy = $("#"+taxonHierarchyId); 
	var grid = $("#"+taxonHierarchyId).jqGrid({
		url:'${createLink(controller:'data', action:'listHierarchy')}',
		datatype: "xml",
   		colNames:['Id', classificationName,'#Species', 'SpeciesId'],
   		colModel:[
   			{name:'id',index:'id',hidden:true},
   			{name:'name',index:'name',formatter:heirarchyLevelFormatter, width:275},
   			{name:'count', index:'count', width:50, hidden:true},
   			{name:'speciesId',index:'speciesId', hidden:true}
   		],
   		atoWidth:true,
   		height:350,        	
    	scrollOffset: 0,
   		treeGrid: true,
   		ExpandColumn : 'name',
   		ExpandColClick  : true,
   		treeGridModel: 'adjacency',
        postData:{n_level:-1, expand_species:true, speciesid:${speciesInstance.id}, classSystem:classificationName},
        sortable:false,
        loadComplete:function(data) {
        	var postData = $("#"+taxonHierarchyId).getGridParam('postData');
			postData["expand_species"] = false;
        	postData["expand_all"] = false;
	    }
	});
}

var heirarchyLevelFormatter = function(el, cellVal, opts) {
	var cells = $(opts).find('cell')
	var taxonId = $(cells[0]).text().trim()
	var speciesId = $(cells[3]).text().trim()
	var level = $(cells[4]).text()
	var levelTxt;
	if(level == ${TaxonomyRank.KINGDOM.ordinal()} ) {
		levelTxt = "<span class='rank'>${TaxonomyRank.KINGDOM}</span>"
	} else if(level == ${TaxonomyRank.PHYLUM.ordinal()} ) {
		levelTxt = "<span class='rank'>${TaxonomyRank.PHYLUM}</span>"
	} else if(level == ${TaxonomyRank.CLASS.ordinal() }) {
		levelTxt = "<span class='rank'>${TaxonomyRank.CLASS}</span>"
	} else if(level == ${TaxonomyRank.ORDER.ordinal() }) {
		levelTxt = "<span class='rank'>${TaxonomyRank.ORDER}</span>"
	} else if(level == ${TaxonomyRank.FAMILY.ordinal() }) {
		levelTxt = "<span class='rank'>${TaxonomyRank.FAMILY}</span>"
	} else if(level == ${TaxonomyRank.SUB_FAMILY.ordinal()} ) {
		levelTxt = "<span class='rank'>${TaxonomyRank.SUB_FAMILY}</span>"
	} else if(level == ${TaxonomyRank.GENUS.ordinal() }) {
		levelTxt = "<span class='rank'>${TaxonomyRank.GENUS}</span>"
	} else if(level == ${TaxonomyRank.SUB_GENUS.ordinal()} ) {
		levelTxt = "<span class='rank'>${TaxonomyRank.SUB_GENUS}</span>"
	} else if(level == ${TaxonomyRank.SPECIES.ordinal()} ) {
		levelTxt = ""
	}

	if(level == ${TaxonomyRank.SPECIES.ordinal() }) {
		el = "<a href='${createLink(action:"show")}/"+speciesId+"'>"+el+"</a>";
	} else {
		// el = "<a href='${createLink(action:"taxon")}/"+taxonId+"'
		// class='rank"+level+"'>"+levelTxt+": "+el+"</a>";
		el = levelTxt+": "+"<span class='rank"+level+"'>"+el+"&nbsp;<a class='taxonExpandAll' onClick='expandAll(\""+cellVal.rowId+"\")'>+</a> </span>"
	}
	return el;	   
}			

	


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

				<h2 style="color: black;">
					${speciesInstance.taxonConcept.italicisedForm }
				</h2>

				<h4><%=CommonNames.findByTaxonConceptAndLanguage(speciesInstance.taxonConcept, Language.findByThreeLetterCode('eng'))?.name%></h4>
			</div>

			<!-- media gallery -->
			<div class="grid_10">
				<div id="resourceTabs">
					<ul>
						<li><a href="#resourceTabs-1">Images</a></li>
						<li><a href="#resourceTabs-2">Audio</a></li>
						<li><a href="#resourceTabs-3">Video</a></li>
						<li><a id="googleImages" href="#resourceTabs-4">Images
								from Google</a></li>
					</ul>
					<div id="resourceTabs-1">
						<div id="gallery1">
						<g:if test="${speciesInstance.getImages()}">
							<g:each in="${speciesInstance.getImages()}" var="r">
							
								<%def gallImagePath = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.gallery.suffix)%>
								<%def gallThumbImagePath = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.galleryThumbnail.suffix)%>
								<a target="_blank"
									rel="${createLinkTo(file: r.fileName.trim(), base:grailsApplication.config.speciesPortal.resources.serverURL)}"
									href="${createLinkTo(file: gallImagePath, base:grailsApplication.config.speciesPortal.resources.serverURL)}">
									<img class="galleryImage"
									src="${createLinkTo(file: gallThumbImagePath, base:grailsApplication.config.speciesPortal.resources.serverURL)}"
									title="${r?.description}" /> </a>

									<g:imageAttribution model="['resource':r]"/>
							</g:each>
							</g:if>
							<g:else>
									<img class="galleryImage"
									src="${createLinkTo(file:"no-image.jpg", base:grailsApplication.config.speciesPortal.resources.serverURL)}"
									title="You can contribute!!!" />
							
							</g:else>
							
						</div>

					</div>
					<div id="resourceTabs-2">There is no audio.</div>
					<div id="resourceTabs-3">There is no video.</div>
					<div id="resourceTabs-4">
						<div class="message ui-corner-all">This portal is not
							responsible for the accuracy or completeness of data presented at
							other web sites.</div>
						<div id="gallery2"></div>
						<div id="googleBranding"></div>
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
				<div class="grid_14 icons">

							<g:each in="${speciesInstance.getIcons()}" var="r">
									<img class="icon" href="${href}"
										src="${createLinkTo(dir: 'images/icons', file: r.fileName.trim(), absolute:true)}"
										title="${r?.description}" />
							</g:each>
				</div>
				<div id="tagcloud"></div>

			</div>

			<!--  static species content -->
			<div class="grid_6 classifications">
				<select name="taxaHierarchy" id="taxaHierarchy" class="value ui-corner-all">
					<g:each in="${Classification.list()}" var="classification">
						<option value="${classification.id}">
							${classification.name}
						</option>
					</g:each>
				</select>
				
				<div class="taxonomyBrowser">						
					<table id="taxonHierarchy"></table>
				</div>
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
			<div id="fieldstoc" class="<%=sparse?'grid_12':'grid_4'%>">
				<ul style="list-style: none;">
					<g:each in="${fields}" var="concept">
						<g:if
							test="${concept.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.TAXONRECORDID) || concept.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.GLOBALUNIQUEIDENTIFIER) || concept.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.NOMENCLATURE_AND_CLASSIFICATION)}">
						</g:if>
						<g:else>
							<hr />
							<g:if test="${sparse}">
								<li style="clear: both; margin-left: 0px">
							</g:if>
							<g:else>
								<li class="nav ui-state-default ui-corner-all"
									onClick="showSpeciesConcept('${conceptCounter}'); showSpeciesField('${conceptCounter}.${fieldCounter}')">
							</g:else>
							<g:showSpeciesConcept
								model="['speciesInstance':speciesInstance, 'concept':concept, 'conceptCounter':conceptCounter, 'sparse':sparse]" />
							</li>
							<%conceptCounter++%>
						</g:else>
					</g:each>
				</ul>
			</div>
			<div class="grid_4" style="float:right;margin-left: .3em;">
				<div class="ui-widget">
					<div class="speciesFieldHeader ui-dialog-titlebar ui-corner-all ui-helper-clearfix ui-widget-header">
						<span class="ui-icon ui-icon-circle-triangle-s" style="float: left; margin-right: .3em;"></span>
							<a href="#taxonRecordName"> Taxon Record Name</a> 
					</div>
					<div class="ui-widget-content">
						<g:collect in="${fields.get(grailsApplication.config.speciesPortal.fields.NOMENCLATURE_AND_CLASSIFICATION).get(grailsApplication.config.speciesPortal.fields.TAXON_RECORD_NAME)}"
							expr="${it.value.get('speciesFieldInstance')}">

							<div class="prop"> 
								<g:if test="${it?.field?.subCategory?.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.REFERENCES)}">
									<span class="name">${it?.field?.subCategory}</span> : <a href="${it?.description}" target="_blank"> ${it?.description}</a>
								</g:if> 
								<g:elseif test="${it?.field?.subCategory?.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.GENERIC_SPECIFIC_NAME)}">
									<span class="name">${it?.field?.subCategory}</span> : <a href="#" class="speciesName"> ${it?.description} </a>

								</g:elseif> 
								<g:else>
									<span class="name">${it?.field?.subCategory}</span> : ${it?.description}
								</g:else> 
							</div>
						</g:collect>
					</div>
				</div>
				<br/>
				<div class="ui-widget">
					<div class="speciesFieldHeader ui-dialog-titlebar ui-corner-all ui-helper-clearfix ui-widget-header">
						<span class="ui-icon ui-icon-circle-triangle-s" style="float: left; margin-right: .3em;"></span>
						<a href="#synonyms"> Synonyms</a> 
					</div>
					<div class="ui-widget-content">
						<g:each in="${Synonyms.findAllByTaxonConcept(speciesInstance.taxonConcept)}" var="synonym">
							<div class="prop"><span class="name">${synonym?.relationship?.value()}</span> : <a class="speciesName" href="#"> ${synonym?.name} </a> </div>
						</g:each>
					</div>
				</div>
				<br/>
				<div class="ui-widget">
				<%
					Map names = new LinkedHashMap();
					CommonNames.findAllByTaxonConcept(speciesInstance.taxonConcept).each(){
						String languageName = it?.language?.name ?: "Others";
						if(!names.containsKey(languageName)) {
							names.put(languageName, new ArrayList());
						}
						names.get(languageName).add(it)
					};
				%>
				<div class="speciesFieldHeader ui-dialog-titlebar ui-corner-all ui-helper-clearfix ui-widget-header">
					<span class="ui-icon ui-icon-circle-triangle-s" style="float: left; margin-right: .3em;"></span>
					<a href="#commonNames"> Common Names</a> 
				</div>
				<div class="ui-widget-content">
					<g:if test="${names}">
						<table>
							<g:each in="${names}">
							<tr><td class="prop">
								<span class="name">${it.key}</span> : <g:each in="${it.value}">
											<a href="#" class="speciesName"> ${it.name}, </a>
										</g:each></td>
								</tr>
							</g:each>
						</table>
					</g:if>
				</div>
				<br/>
				<div class="ui-widget">
					<div class="speciesFieldHeader ui-dialog-titlebar ui-corner-all ui-helper-clearfix ui-widget-header">
						<span class="ui-icon ui-icon-circle-triangle-s" style="float: left; margin-right: .3em;"></span>
						<a href="#externalLinks">External Links</a> 
					</div>
					<div class="ui-widget-content">
						<ul>
						<li><a target="_blank"
								href="http://www.ubio.org/browser/search.php?search_all=${speciesInstance.taxonConcept.binomialForm?:speciesInstance.taxonConcept.canonicalForm}">
							Search on uBio
						</a></li>
						</ul>
					</div>
				</div>
			</div>
			</div>
			<g:if test="${!sparse}">
				<div id="speciesFieldContainer" class="ui-corner-all grid_12"></div>
			</g:if>
		</div>
	

</body>

</html>
