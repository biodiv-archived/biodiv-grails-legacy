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
<%@page import="grails.plugin.springsecurity.SpringSecurityUtils"%>
<%@page import="species.Synonyms"%>
<%@page import="species.Language"%>
<%@page import="species.License"%>
<%@page import="species.SpeciesField"%>
<%@page import="species.sourcehandler.XMLConverter"%>

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
            .resources .addedResource {
            height: 315px !important;
            max-height:315px !important;
            }
            .add_file_container {
                padding: 55px 10px !important;
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
        <script type="text/javascript" src="ckEditorConfig.js" />

        <script type="text/javascript"
            src="/sites/all/themes/wg/scripts/OpenLayers-2.10/OpenLayers.js"></script>
        <script type="text/javascript" src="/sites/all/themes/wg/scripts/am.js"></script>
        -->
        <script type="text/javascript">

        occurrenceCount = undefined
        function getOccurrenceCount(data) {
            occurrenceCount = ${speciesInstance.fetchOccurrence()} + data.count;
            console.log(occurrenceCount);
        }

        window.is_species_admin = ${SpringSecurityUtils.ifAllGranted('ROLE_SPECIES_ADMIN')} 
        </script>

        <script type="text/javascript"
            src="/geoserver/ows?request=getOccurrenceCount&service=amdb&version=1.0.0&species_name=${speciesName}"></script>

        <r:script>
        //google.load("search", "1");
        // Galleria.loadTheme('${resource(dir:'js/galleria/1.2.7/themes/classic/',file:'galleria.classic.min.js')}');

        $(document).ready(function(){
            if(${sparse}) {
                if(occurrenceCount > 0) {
                showOccurence('${speciesName}');
                //$("#map .alert").html("Showing "+occurrenceCount+" occurrence records for <i>${speciesName}</i>.");
                } else {
                $("#map").next('.alert').html("Currently no occurrence records are available right now. Please check back with us after some time or provide us if you have any.").css('height','auto');
                $('#map').hide();
                }
            } else {
                showSpeciesConcept($(".defaultSpeciesConcept").attr("id"))
                showSpeciesField($(".defaultSpeciesField").attr("id"))
            }
            });

        </r:script>

        <%String space = speciesInstance.taxonConcept.canonicalForm%>
            <r:script type='text/javascript'> 
                CKEDITOR.plugins.addExternal( 'confighelper', '${request.contextPath}/js/ckeditor/plugins/confighelper/' );

                var config = { extraPlugins: 'confighelper', toolbar:'EditorToolbar', toolbar_EditorToolbar:[
                    { name: 'document', groups: [ 'mode', 'document', 'doctools' ], items: [ 'Source', '-', 'Save', 'Preview'  ] },
                    { name: 'clipboard', groups: [ 'clipboard', 'undo' ], items: [ 'Cut', 'Copy', 'Paste', 'PasteText', 'PasteFromWord', '-', 'Undo', 'Redo' ] },
                    { name: 'editing', groups: [ 'find', 'selection', 'spellchecker' ], items: [ 'Find', 'Replace', '-', 'SelectAll', '-', 'Scayt' ] },
                    '/',
                    { name: 'basicstyles', groups: [ 'basicstyles', 'cleanup' ], items: [ 'Bold', 'Italic', 'Underline', 'Strike', 'Subscript', 'Superscript', '-', 'RemoveFormat' ] },
                    { name: 'paragraph', groups: [ 'list', 'indent', 'blocks', 'align', 'bidi' ], items: [ 'NumberedList', 'BulletedList', '-', 'Outdent', 'Indent', '-', 'Blockquote', 'CreateDiv', '-', 'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock', '-', 'BidiLtr', 'BidiRtl', 'Language' ] },
                    { name: 'links', items: [ 'Link', 'Unlink', 'Anchor' ] },
                    { name: 'insert', items: ['Table'] }
                    ],
                    filebrowserImageBrowseUrl: "/${grailsApplication.metadata['app.name']}/ck/biodivofm?fileConnector=/${grailsApplication.metadata['app.name']}/ck/biodivofm/filemanager&viewMode=grid&space=img/${speciesInstance.taxonConcept.canonicalForm}",
                    //filebrowserImageUploadUrl: "/biodiv/ck/standard/uploader?Type=Image&userSpace=${speciesInstance.taxonConcept.canonicalForm}",

                    height: '300px',
                    ignoreEmptyParagraph: true,
                    enterMode:CKEDITOR.ENTER_BR,
                    autoParagraph:false,
                    fillEmptyBlocks:false
                    //uiColor:'#AADC6F'
                };
                var speciesId = ${speciesInstance?.id}
            </r:script>
    </head>

    <body>
    <link rel="stylesheet" href="/biodiv/js/galleria/1.3.5/themes/classic/galleria.classic.css">
    <script src="/biodiv/js/galleria/1.3.5/galleria-1.3.5.js"></script>
    <script src="/biodiv/js/galleria/1.3.5/themes/classic/galleria.classic.min.js"></script>
    <script src="/biodiv/js/galleria/1.3.5/plugins/flickr/galleria.flickr.min.js"></script>
        <g:if test="${speciesInstance}">
        <g:set var="featureCount" value="${speciesInstance.featureCount}"/>
        </g:if>
        <s:isSpeciesContributor model="['speciesInstance':speciesInstance]">
        <g:set var="isSpeciesContributor" value="${Boolean.TRUE}"/>
        </s:isSpeciesContributor>
 
        <%def converter = new XMLConverter()%>
        <% 
        Map fieldFromName = [
            summary : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.SUMMARY,2,userLanguage),
            occurrenceRecords : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.OCCURRENCE_RECORDS,2,userLanguage),
            references : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.REFERENCES,2,userLanguage),
            brief : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.BRIEF,2,userLanguage),
            gdge : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.GLOBAL_DISTRIBUTION_GEOGRAPHIC_ENTITY,3,userLanguage),
            gege : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.GLOBAL_ENDEMICITY_GEOGRAPHIC_ENTITY,3,userLanguage) ,
            idge : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.INDIAN_DISTRIBUTION_GEOGRAPHIC_ENTITY,3,userLanguage), 
            iege : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.INDIAN_ENDEMICITY_GEOGRAPHIC_ENTITY,3,userLanguage),
            tri  : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.TAXONRECORDID,1,userLanguage),
            gui  : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.GLOBALUNIQUEIDENTIFIER,1,userLanguage),
            nc  : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.NOMENCLATURE_AND_CLASSIFICATION,1,userLanguage),
            md  : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.META_DATA,1,userLanguage),
            acth  : converter.getFieldFromName(grailsApplication.config.speciesPortal.fields.AUTHOR_CONTRIBUTED_TAXONOMIC_HIERARCHY,2,userLanguage)
            ]
        %>

        <div class="span12">
            <s:showSubmenuTemplate model="['entityName':speciesInstance.taxonConcept.italicisedForm , 'subHeading':CommonNames.findByTaxonConceptAndLanguage(speciesInstance.taxonConcept, Language.findByThreeLetterCode('eng'))?.name, 'headingClass':'sci_name', 'isSpeciesContributor':isSpeciesContributor]"/>

                <g:if test="${!speciesInstance.percentOfInfo}">
                <div class="poor_species_content alert">
                    <i class="icon-info"></i>
                   <g:message code="showspeciesstorytablet.no.information" />

                </div>
                </g:if>

                <div class="span12" style="margin-left:0px">
                    <g:render template="/common/observation/showObservationStoryActionsTemplate"
                    model="['instance':speciesInstance, 'href':canonicalUrl, 'title':title, 'description':description, 'hideFlag':true, 'hideDownload':true]" />
                </div>

                <g:render template="/species/showSpeciesIntro" model="['speciesInstance':speciesInstance, 'isSpeciesContributor':isSpeciesContributor, fieldFromName:fieldFromName]"/>
                <div class="span12" style="margin-left:0px">

                    <g:render template="/species/speciesImageUpload" model="['speciesInstance': speciesInstance, 'isSpeciesContributor':isSpeciesContributor]"/>                    
                    
                    <g:render template="/species/addSpeciesFieldMedia" model="['observationInstance':speciesInstance, 'isSpeciesContributor':isSpeciesContributor]"/>

                    <g:render template="/species/showSpeciesNames" model="['speciesInstance':speciesInstance, 'fields':fields, 'isSpeciesContributor':isSpeciesContributor, converter:converter, userLanguage:userLanguage]"/>

                    <ul style="list-style: none;margin:0px;">
                        <g:each in="${fields}" var="concept">
                        <s:hasContent model="['map':concept.value]">
                        <g:if
                        test="${concept.key.equalsIgnoreCase(fieldFromName.tri) || concept.key.equalsIgnoreCase(fieldFromName.gui) || concept.key.equalsIgnoreCase(fieldFromName.nc) || concept.key.equalsIgnoreCase(fieldFromName.md)}">
                        </g:if>
                        <g:else>

                        <g:if test="${sparse}">
                        <li style="clear: both; margin-left: 0px">
                        </g:if>
                        <g:else>
                        <li class="nav ui-state-default">
                        </g:else>
                        <g:showSpeciesConcept model="['speciesInstance':speciesInstance, 'concept':concept, 'conceptCounter':conceptCounter, 'sparse':sparse, 'observationInstanceList':observationInstanceList, 'instanceTotal':instanceTotal, 'queryParams':queryParams, 'activeFilters':activeFilters, 'userGroupWebaddress':userGroupWebaddress, newSpeciesFieldInstance:newSpeciesFieldInstance, 'isSpeciesContributor':isSpeciesContributor, 'userLanguage':userLanguage, fieldFromName:fieldFromName]" />
                        </li>
                        <%conceptCounter++%>
                        </g:else>
                        </s:hasContent>
                        </g:each>
                    </ul>
                </div>			
                <g:if test="${!sparse}">
                <div id="speciesFieldContainer" class="grid_12"></div>
                </g:if>

                <!-- right side bar -->
                <div class="span12 classifications" style="margin-left:0px;">
                    <uGroup:objectPostToGroupsWrapper 
                    model="['objectType':speciesInstance.class.canonicalName, 'observationInstance':speciesInstance]" />
                    <div class="sidebar_section">
                        <h5> <g:message code="button.activity" /> </h5>
                        <div class="union-comment">
                            <feed:showAllActivityFeeds model="['rootHolder':speciesInstance, feedType:'Specific', refreshType:'manual', 'feedPermission':'editable']" />
                            <comment:showAllComments model="['commentHolder':speciesInstance, commentType:'super','showCommentList':false, 'userLanguage':userLanguage]" />
                        </div>
                    </div>

                </div>
                
            </div>	 
            <script type="text/javascript">
           var licenseSelectorOptions = [];
            <g:each in="${License.LicenseType.toList()}" var="l">
            licenseSelectorOptions.push({value:"${l.value()}", text:"${l.value()}"});
            </g:each>

            var audienceTypeSelectorOptions = [];
            <g:each in="${SpeciesField.AudienceType.toList()}" var="l">
            audienceTypeSelectorOptions.push({value:"${l.value()}", text:"${l.value()}"});
            </g:each>

            var statusSelectorOptions = [];
            <g:each in="${SpeciesField.Status.toList()}" var="l">
            statusSelectorOptions.push({value:"${l.value()}", text:"${l.value()}"});
            </g:each>

            var synRelSelectorOptions = [], langSelectorOptions = [];
            <g:each in="${Synonyms.RelationShip.toList()}" var="rel">
            synRelSelectorOptions.push({value:"${rel.value()}", text:"${rel.value()}"});
            </g:each>
            var langSelectorOptions = [];
            <g:each in="${Language.findAllByIsDirty(Boolean.FALSE)}" var="lang">
            langSelectorOptions.push({value:"${lang.name}", text:"${lang.name}"});
            </g:each>

            var taxonRanks = [];
            <g:each in="${TaxonomyRank.list()}" var="t">
            taxonRanks.push({value:"${t.ordinal()}", text:"${g.message(error:t)}"});
            </g:each>

            </script>	
            <r:script>
            $(document).ready(function() {
                var uploadResource 
                window.params.carousel = {maxHeight:150, maxWidth:210}
                window.params.species.name = "${speciesName}"
            });
 
            </r:script>

        </body>

    </html>
