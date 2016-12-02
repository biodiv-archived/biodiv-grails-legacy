<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="species.utils.ImageType"%>
<%@ page import="species.ScientificName.TaxonomyRank"%>
<%@page import="species.Resource.ResourceType"%>
<%@ page import="species.Species"%>
<%@ page import="species.Classification"%>
<%@ page import="species.ScientificName"%>
<%@ page import="species.CommonNames"%>
<%@ page import="species.Language"%>
<%@page import="species.utils.Utils"%>
<%@page import="species.participation.Featured"%>
<%@page import="species.participation.Observation"%>
<%@page import="species.participation.ActivityFeedService"%>
<%@page import="grails.plugin.springsecurity.SpringSecurityUtils"%>
<%@page import="species.Synonyms"%>
<%@page import="species.Language"%>
<%@page import="species.License"%>
<%@page import="species.SpeciesField"%>
<%@page import="species.sourcehandler.XMLConverter"%>
<%@page import="species.participation.ActivityFeedService"%>

<html>
    <head>
        <g:set var="canonicalUrl" value="${uGroup.createLink([controller:'species', action:'show', id:speciesInstance.id, base:Utils.getIBPServerDomain()])}"/>
        <g:set var="title" value="${speciesInstance.taxonConcept.name}"/>
        <g:set var="description" value="${Utils.stripHTML(speciesInstance.notes(userLanguage)?:'')}" />
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


        <style>
            .container_16 {
            width:940px;
            }

            .speciesField .jcarousel-skin-ie7 .jcarousel-item-horizontal .snippet.tablet .figure {
            width:210px;
            height:150px;
            }

            .speciesField .jcarousel-skin-ie7 .jcarousel-item-horizontal  .thumbnail .figure a {
            max-width:210px;
            max-height:150px;
            }

            .speciesField .jcarousel-skin-ie7 .jcarousel-item-horizontal  .thumbnail .img-polaroid {
            max-width:210px;
            max-height:140px;
            }

            .speciesField .jcarousel-skin-ie7 .jcarousel-item-horizontal {
            width:210px;
            height:250px;
            margin-right:3px;
            }

            .speciesField .jcarousel-skin-ie7 .jcarousel-item-horizontal .snippet.tablet {
            width:210px;
            height:250px;
            }

            .speciesField .jcarousel-skin-ie7 .jcarousel-clip-horizontal {
            height:250px;
            padding:0px 1px;
            }

            .speciesField .jcarousel-skin-ie7 .jcarousel-item-horizontal .snippet.tablet .caption {
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

            #commonNames select{
                width:245px;
                margin-left:-15px;
            }           
            .citation{background-color:white;padding:15px;} 
            .reco-comment-table{ right: inherit; }
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

        <asset:script>

        $(document).ready(function(){
            if(${sparse}) {
                
                showOccurence('${speciesName}');
                
            } else {
                showSpeciesConcept($(".defaultSpeciesConcept").attr("id"))
                showSpeciesField($(".defaultSpeciesField").attr("id"))
            }
            });

        </asset:script>

        <%String space = speciesInstance.taxonConcept.canonicalForm%>
            <asset:script type='text/javascript'> 
            CKEDITOR.plugins.addExternal( 'confighelper', "${assetPath(src:'ckeditor/confighelper/plugin.js')}" );

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
                    filebrowserImageBrowseUrl: "/${grailsApplication.metadata['app.name']}/ck/${grailsApplication.metadata['app.name']}ofm?fileConnector=/${grailsApplication.metadata['app.name']}/ck/biodivofm/filemanager&viewMode=grid&space=img/${speciesInstance.taxonConcept.canonicalForm}",
                    //filebrowserImageUploadUrl: "/biodiv/ck/standard/uploader?Type=Image&userSpace=${speciesInstance.taxonConcept.canonicalForm}",

                    height: '300px',
                    ignoreEmptyParagraph: true,
                    enterMode:CKEDITOR.ENTER_BR,
                    autoParagraph:false,
                    fillEmptyBlocks:false,
                    contentsCss:'/assets/all/ckeditorCss.css'
                    //uiColor:'#AADC6F'
                };
                var speciesId = ${speciesInstance?.id}
            </asset:script>
    </head>

    <body>

   

        <g:if test="${speciesInstance}">
        <g:set var="featureCount" value="${speciesInstance.featureCount}"/>
        </g:if>
        <s:isSpeciesContributor model="['speciesInstance':speciesInstance]">
        <g:set var="isSpeciesContributor" value="${Boolean.TRUE}"/>
        </s:isSpeciesContributor>
        
        <div class="span12">
            <s:showSubmenuTemplate model="['entityName':speciesInstance.taxonConcept.italicisedForm , 'subHeading':CommonNames.findWhere(taxonConcept:speciesInstance.taxonConcept, language:Language.findByThreeLetterCode('eng'), isDeleted:false)?.name, 'headingClass':'sci_name', 'isSpeciesContributor':isSpeciesContributor, speciesInstance:speciesInstance]"/>

                <g:if test="${!speciesInstance.percentOfInfo}">
                <div class="poor_species_content alert">
                    <i class="icon-info"></i>
                   <g:message code="showspeciesstorytablet.no.information" />

                </div>
                </g:if>

                <div class="span12" style="margin-left:0px">

                    <g:render template="/common/observation/showObservationStoryActionsTemplate"
                    model="['instance':speciesInstance, 'href':canonicalUrl, 'title':title, 'description':description, 'hideFlag':true, 'hideDownload':true, ibpClassification:speciesInstance.taxonConcept.fetchDefaultHierarchy()]" />
                </div>

                <g:render template="/species/showSpeciesIntro" model="['speciesInstance':speciesInstance, 'isSpeciesContributor':isSpeciesContributor, fieldFromName:fieldFromName, userLanguage:userLanguage]"/>
                
                <div class="span12" style="margin-left:0px">
                    <g:if  test="${traitInstanceList}">
                    <div class="sidebar_section" style="margin:10px 0px;">
                    <a class="speciesFieldHeader" data-toggle="collapse" href="#traits"><h5>Traits</h5></a>
                    <div class="sidebar_section pre-scrollable" style="max-height:419px;overflow:visible;">
                    <div id="traits" class="trait">
                   <g:render template="/trait/showTraitListTemplate" model="['instanceList':traitInstanceList, 'factInstance':factInstanceList, 'speciesInstance': speciesInstance, 'fromSpeciesShow':true]"/>
                    </div>
                    </div>
                    </div>
                    </g:if>
                    <g:render template="/species/speciesImageUpload" model="['speciesInstance': speciesInstance, 'isSpeciesContributor':isSpeciesContributor]"/>                    
                    
                    <g:render template="/species/addSpeciesFieldMedia" model="['observationInstance':speciesInstance, 'isSpeciesContributor':isSpeciesContributor]"/>

                    <g:render template="/species/showSpeciesNames" model="['speciesInstance':speciesInstance, 'fields':fields, 'isSpeciesContributor':isSpeciesContributor, fieldFromName:fieldFromName, userLanguage:userLanguage]"/>

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
                    <!--div id="tocContainer" class="sidebar_section">
                    <div id="toc" class="tile"></div>
                    </div-->
                    <!--div id="map" class="sidebar_section">
                    <h5>${g.message(code:'heading.occurence.map')}</h5>
                    <div id="mapSpinner" class="spinner">
                        <center>
                            <img src="${assetPath(src:'/all/spinner.gif', absolute:true)}" alt="${message(code:'spinner.alt',default:'Loading...')}" />
                        </center>
                    </div>


                    <div id="map1311326056727" class="occurenceMap"
                        style="height: 350px; width: 100%"></div>
                    <div class="alert alert-info">
                        <img src="${assetPath(src:'/all/maplegend.png')}" alt="map legend"/>
                        ${g.message(code:'info.about.map.species')}
                    </div>

                    <comment:showCommentPopup model="['commentHolder':[objectType:ActivityFeedService.SPECIES_MAPS, id:speciesInstance.id], 'rootHolder':speciesInstance, 'userLanguage':userLanguage]" />	

                    </div-->
                    <!-- Citation  -->
                    <div class="sidebar_section">
                     <%     def current_date = new Date()
                            def domainName=Utils.getIBPServerDomain();  %>
                            <h5>${g.message(code:'default.citation.title')}</h5>
                            <div class="citation">
                    {Author1, Author2...}, (n.d.). <i>${title} </i>. [online] ${grailsApplication.config.speciesPortal.app.siteName},Species Page : {name of species field} Available at: <a href="${domainName+request.forwardURI }">${domainName+request.forwardURI}</a> [Accessed date <g:formatDate format="dd-MMM-yyyy" date="${current_date}" type="date" style="MEDIUM" />].
                    </div>
                    </div>
                    <!-- Citation End -->
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
            <input type="hidden" name="policy" value="${policy}"/>
            <input type="hidden" name="signature" value="${signature}"/>

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
            <g:each in="${ScientificName.RelationShip.toList()}" var="rel">
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
            <asset:script>
            $(document).ready(function() {
                var uploadResource; 
                window.params.carousel = {maxHeight:150, maxWidth:210}
                window.params.species.name = "${speciesName}"
                $('input#taxon').val("${speciesInstance.taxonConcept.id}");
                $('.observations_list').removeClass('observations_list');
                window.params.queryParamsMax = 8;
                intializesSpeciesHabitatInterest(false);
                updateGallery('/species/list', 8, 0, undefined, true,undefined,undefined,undefined,undefined,false);
                var getResourceUrl = "${uGroup.createLink(controller:'species', action:'getObjResources', userGroupWebaddress:params.webaddress)}";
                galleryAjax(getResourceUrl+'/'+${speciesInstance.id},'species');
            });


            </asset:script>
        </body>

    </html>
