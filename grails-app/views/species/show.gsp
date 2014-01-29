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
            .container_16 {
            width:940px;
            }

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
            margin-right:3px;
            }

            .jcarousel-skin-ie7 .jcarousel-item .snippet.tablet {
            width:210px;
            height:250px;
            }

            .jcarousel-skin-ie7 .jcarousel-clip-horizontal {
            height:250px;
            padding:0px 1px;
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

        <script type="text/javascript"
            src="/sites/all/themes/wg/scripts/OpenLayers-2.10/OpenLayers.js"></script>
        <script type="text/javascript" src="/sites/all/themes/wg/scripts/am.js"></script>
        -->
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
        });

        </r:script>
    </head>

    <body>
        <g:if test="${speciesInstance}">
        <g:set var="featureCount" value="${speciesInstance.featureCount}"/>
        </g:if>

        <div class="span12">
            <s:showSubmenuTemplate model="['entityName':speciesInstance.taxonConcept.italicisedForm , 'subHeading':CommonNames.findByTaxonConceptAndLanguage(speciesInstance.taxonConcept, Language.findByThreeLetterCode('eng'))?.name, 'headingClass':'sci_name']"/>

                <g:if test="${!speciesInstance.percentOfInfo}">
                <div class="poor_species_content alert">
                    <i class="icon-info"></i>
                    No information yet.

                </div>
                </g:if>

                <div class="span12" style="margin-left:0px">
                    <g:render template="/common/observation/showObservationStoryActionsTemplate"
                    model="['instance':speciesInstance, 'href':canonicalUrl, 'title':title, 'description':description, 'hideFlag':true, 'hideDownload':true]" />
                </div>

                <g:render template="/species/showSpeciesIntro" model="['speciesInstance':speciesInstance]"/>
                <div class="span12" style="margin-left:0px">
                    <g:render template="/species/showSpeciesNames" model="['speciesInstance':speciesInstance, 'fields':fields]"/>



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
                    <uGroup:objectPostToGroupsWrapper 
                    model="['objectType':speciesInstance.class.canonicalName, 'observationInstance':speciesInstance]" />
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
            window.params.species.name = "${speciesName}"
            });
            </g:javascript>	

        </body>

    </html>
