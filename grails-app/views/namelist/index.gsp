<%@page import="species.ScientificName.TaxonomyRank"%>
<%@page import="species.Language"%>
<%@page import="species.NamesMetadata.NameStatus"%>
<%@page import="species.NamesMetadata.NamePosition"%>
<%@ page import="species.Species"%>
<%@ page import="species.Classification"%>
<%@ page import="species.participation.DownloadLog.DownloadType"%>
<%@ page import="species.TaxonomyDefinition"%>
<html>
    <head>

        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
        <meta name="layout" content="main" />
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <title>NameList - Curation Interface</title>

        <asset:javascript src="slickgrid.js"/>
        <asset:javascript src="biodiv/curation.js"/>
    </head>
    <body>

        <div class="row-fluid namelist_wrapper">
            <div class="span12">
            <div class="span3 listarea taxon_selector_wrapper">
                <div class="taxon_selector_span taxon_selector_wrapper_span">
                    Taxon Selector
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.taxonselector.info')}"></i>
                </div>
                <div class="taxonomyBrowser" data-name="classification" data-speciesid="${speciesInstance?.id}">
                    <div id="taxaHierarchy">
                        <%
                        def classifications = [];
                        Classification.list().each {
                        classifications.add([it.id, it, null]);
                        }
                        classifications = classifications.sort {return it[1].name};
                        %>

                        <g:render template="/common/taxonBrowserTemplate" model="['classifications':classifications, 'expandAll':false, 'showCheckBox':false, height:'450px']"/>


                    </div>
                </div>
            </div>
            
            
            <div class="span9 listarea" style="height:460px;">
                <div id="inlineFilterPanel" class="filters">
                    <%--Filter names with text <input type="text" id="txtSearch" style="margin:3px 0px 3px 0px">
                    and with --%>
                    <div class="span6 filter">
                        <div class="taxon_selector_span taxon_selector_wrapper_span">
                            Rank
                            <button class="selectNone btn-link pull-right">Reset</button>
                            <button class="selectAll btn-link pull-right">Select All</button>
                        </div>
                        <div class="filterValues well">
                            <g:each in="${TaxonomyRank.list()}" var="t">
                            <label class="checkbox">
                                <input type="checkbox" name="taxonRank" data-ordinal="${t.ordinal()}" value="${t.toString().toLowerCase()}" ${params.ranksToFetch?.split(',')?.contains(t.ordinal().toString())? "checked='checked'":''}>${t}</label>
                            </g:each>
                        </div>
                    </div>

                    <div class="span3 filter">
                        <div class="taxon_selector_span taxon_selector_wrapper_span">
                            Status
                            <button class="selectNone btn-link pull-right">Reset</button>
                            <button class="selectAll btn-link pull-right">Select All</button>
                        </div>
                        <div class="filterValues well">
                            <g:each in ="${NameStatus.list()}" var="status">
                            <g:if test="${status != NameStatus.COMMON}">
                            <label class="checkbox">
                                <input type="checkbox" name="taxonStatus" value="${status}" ${params.statusToFetch?.contains(status.value.toUpperCase())? "checked='checked'":''}><span>${status.label()}</span>
                            </label>
                            </g:if>
                            </g:each>

                        </div>
                    </div>
                    <div class="span3 filter">
                        <div class="taxon_selector_span taxon_selector_wrapper_span">
                            Position
                            <button class="selectNone btn-link pull-right">Reset</button>
                            <button class="selectAll btn-link pull-right">Select All</button>
                        </div>
                        <div class="filterValues well">
                            <g:each in ="${NamePosition.list()}" var="position">
                            <label class="checkbox ${position}">
                                <input type="checkbox" name="taxonPosition" value="${position}" ${params.positionsToFetch?.contains(position.value.toUpperCase())? "checked='checked'":''}><span>${position.label()}
                                    <%def code = g.message(code:"namelist.${position.label().toLowerCase()}list.info")%>
                                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${code}"></i>
                                </span>
                            </label>
                            </g:each>
                        </div>
                    </div>
               </div>

               <div id="taxonGrid" class="dl_content" style="height:318px"></div>
               <div id="listCounts" class="pull-left info-message" style="padding:0px;margin-left:0px;">
                   <span id="instanceCount"></span>
                   <!--span id="acceptedCount"></span>
                   <span id="synonymCount"></span>
                   <span id="dirtyListCount" class="RAW"></span>
                   <span id="workingListCount" class="WORKING"></span>
                   <span id="cleanListCount" class="CLEAN"></span-->
              </div>

 
                <div>
                    <div id="taxonPager" class="pull-right paginateButtons" style="padding:0px;"></div>
                    <obv:download  model="['source':'TaxonomyDefinition', 'requestObject':request, 'downloadTypes':[DownloadType.CSV], downloadObjectId:params.taxon, 'exportFields':TaxonomyDefinition.fetchExportableFields()]" />
                </div>
            </div>   
            </div>
            
<%--            <div class="span3 listarea dirty_listarea">
                <div class="dirty_list taxon_selector_wrapper_span">
                    Raw List

                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.rawlist.info')}"></i>
                </div>
                <select class="span12 listSelector">
                    <option value='speciesDLContent'>Species and Subspecies</option>
                    <option value='accDLContent'>Child Taxa</option>
                    <!--option value='synDLContent'>Synonyms</option-->
                    <!--option value='comDLContent'>Common Names</option-->
                </select>
                <div class="dl_content taxon_selector_list listarea_content">
                </div>

                <!--div class="row-fluid">
                <g:render template="/namelist/actionFieldsTemplate" model="['showArrow':false]"/>
                </div-->


            </div>
            <div class="span3 listarea working_listarea">
                <div class="working_list taxon_selector_wrapper_span">
                    Working List
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.workinglist.info')}"></i>
                </div>
                <select class="span12 listSelector">
                    <option value='speciesWLContent'>Species and Subspecies</option>
                    <option value='accWLContent'>Child Taxa</option>
                    <!--option value='synWLContent'>Synonyms</option-->
                    <!--option value='comWLContent'>Common Names</option-->
                </select>

                <div class="wl_content taxon_selector_list listarea_content">
                </div>

                <!--div class="row-fluid">
                <g:render template="/namelist/actionFieldsTemplate" model="['showArrow':true]"/>
                </div-->
            </div>
            <div class="span3 listarea clean_listarea">
                <div class="clean_list taxon_selector_wrapper_span">
                    Clean List
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.cleanlist.info')}"></i>
                </div>
                <select class="span12 listSelector">
                    <option value='speciesCLContent'>Species and Subspecies</option>
                    <option value='accCLContent'>Child Taxa</option>
                    <!--option value='synCLContent'>Synonyms</option-->
                    <!--option value='comCLContent'>Common Names</option-->
                </select>

                <div class="cl_content taxon_selector_list listarea_content">
                </div>

                <!--div class="row-fluid">
                <g:render template="/namelist/actionFieldsTemplate" model="['showArrow':true]"/>
                </div-->


            </div>
            --%>

        </div>

        <div class="row-fluid metadataDetails namelist_wrapper">
            <div class="span12">

            
                <div class="span3 taxon_selector_wrapper" >
            <div class="listarea">
                <div class="taxon_selector_span taxon_selector_wrapper_span">
                    Query 
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.query.info')}"></i>
                </div>


                <div class="row-fluid">
                    <div>
                        <select id="queryDatabase" class="queryDatabase span12" style="margin-bottom: 6px;">
                            <option value="databaseName">Database name</option>
                            <option value="col">Catalogue of Life</option>
                            <option value="gbif">GBIF</option>
                            <option value="ubio">Ubio</option>
                            <option value="tnrs">TNRS</option>
                            <option value="gni">Global Names Index</option>
                            <option value="eol">EoL</option>
                            <option value="worms">WoRMS</option>
                        </select>
                        <button class="btn btn-primary queryString span12" style="margin:0px; margin-bottom: 6px;" onClick='searchDatabase(false)'>Search <i class="icon-search icon-white" style="margin-left: 10px;"></i></button>
                    </div>
                </div>
            </div>
                <div class="listarea">
                    <div class="connection_wrapper taxon_selector_span taxon_selector_wrapper_span" style="padding: 5px 0px;font-weight: bold;">Connections
                    
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.connections.info')}"></i>
                    </div>

                    <g:render template="/namelist/connectionsTemplate" model="[controller:'species', instanceTotal:speciesInstanceTotal]"/>
                    <g:render template="/namelist/connectionsTemplate" model="[controller:'observation', instanceTotal:observationInstanceTotal]"/>
                    <g:render template="/namelist/connectionsTemplate" model="[controller:'checklist', instanceTotal:checklistInstanceTotal]"/>
                    <g:render template="/namelist/connectionsTemplate" model="[controller:'map', instanceTotal:mapInstanceTotal]"/>
                    <g:render template="/namelist/connectionsTemplate" model="[controller:'document', instanceTotal:documentInstanceTotal]"/>
                </div>

            </div>
            <!--div class="span3 canBeDisabled listarea taxon_selector_wrapper">
                <div class="taxon_selector_span taxon_selector_wrapper_span">
                    Hierarchy
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.hierarchy.info')}"></i>
                </div>

           </div-->
            <div class="span9 canBeDisabled listarea taxon_selector_wrapper">
                <div class="taxon_selector_span taxon_selector_wrapper_span">
                    Attributes
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.attributes.info')}"></i>
                </div>


                <input type="hidden" class="taxonRegId" value="">
                <input type="hidden" class="taxonId" value="">
                <input type="hidden" class="recoId" value="">
                <input type="hidden" class="isOrphanName" value="">
                <input type="hidden" class="fromCOL" value=false>
                <input type="hidden" class="id_details" value="">

                 <div>
                   <div class="span8 attributesBlock form-horizontal" style="margin-left:0px;">
                    <div class="control-group" style="width:100%">
                        <label class="control-label" style="width:100px; text-align:right;display:inline-block;">Name
                            <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.name.info')}"></i>
                        </label>
                        <input type="text" placeholder="Name" class="name" style="width:81.6%;display: inline-block;margin-bottom: 0;vertical-align: middle;"/>
                    </div>
 
                    <div class="control-group">
                        <label class="control-label">Canonical
                        
                            <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.canonicalname.info')}"></i>
                        </label> 
                        <input type="text" placeholder="Canonical Name" class="canonicalForm" disabled/>
                    </div>
                    <div class="control-group">
                        <label class="control-label"> Author
                        
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.authorstring.info')}"></i>
                        </label>
                        <input type="text" placeholder="Author" class="authorString" disabled/>
                    </div>
                    <div class="control-group">
                        <label class="control-label">Status
                        
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.status.info')}"></i>
                        </label>
                        <select id="statusDropDown" class="status">
                            <option value="chooseNameStatus">Choose Name Status</option>
                            <g:each in="${NameStatus.list()}" var="ns">
                            <g:if test="${ns != NameStatus.PROV_ACCEPTED && ns != NameStatus.COMMON}">
                            <option value="${ns.toString().toLowerCase()}">${ns.value()}</option>
                            </g:if>
                            </g:each>
                        </select>
                    </div>
                    
                    <div class="control-group">
                        <label class="control-label">Rank
                        
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.rank.info')}"></i>
                        </label>
                        <select id="rankDropDown" class="rank">
                            <option value="chooseRank">Choose Rank</option>
                            <% def rankCount = 0 %>
                            <g:each in="${TaxonomyRank.list()}" var="t">
                            <option data-ordinal="${t.ordinal()}" value="${t.toString().toLowerCase()}" ${params.ranksToFetch?.split(',')?.contains(t.ordinal().toString())?"selected='selected'":''}>${t}</option>
                            </g:each>
                        </select>
                    </div>
                    <div class="control-group">
                        <label class="control-label">Source 
                        
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.source.info')}"></i>
                        </label> 
                        <input type="text" placeholder="Source" class="source"/>
                    </div>															
                    <div class="control-group">
                        <label class="control-label">via
                        
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.via.info')}"></i>
                        </label>  
                        <input type="text" placeholder="Via" class="sourceDatabase via"/>
                    </div>

                    <div class="control-group">
                        <label class="control-label">ID
                        
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.id.info')}"></i>
                        </label>  
                        <input type="text" placeholder="Id" class="id"/>
                    </div>			

                    <div class="control-group">
                        <label class="control-label">Position
                        
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.position.info')}"></i>
                        </label>
                        <select id="positionDropDown" class="position">
                            <option value="choosePosition">Choose Position</option>
                            <g:each in="${NamePosition.list()}" var="t">
                            <option value="${t.toString().toLowerCase()}">${t}</option>
                            </g:each>
                        </select>
                    </div>
                    <div class="rt_family" style="background:slategrey;width:100%;clear:both;">


                        <ul class="nav nav-tabs" id="" style="margin:0px;">
                            <li id="names-li0" class="active" style="width:175px;"><a href="#names-tab0" class="btn" data-toggle="tab">Accepted Name

                                <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.acceptednames.info')}"></i>
                            </a></li>
                            <li id="names-li1" style="width:175px;"><a href="#names-tab1" class="btn" data-toggle="tab">Synonyms

                                <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.synonyms.info')}"></i>
                            </a></li>
                            <li id="names-li2" style="width:175px;"><a href="#names-tab2" class="btn" data-toggle="tab">Common Names

                                <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.commonnames.info')}"></i>
                            </a></li>   
                            <!--li id="names-li3"><a href="#names-tab3" class="btn" data-toggle="tab">Reference(s)</a></li-->   
                        </ul>

                        <div class="tab-content" id="names-tab-content">
                            <div class="tab-pane active" id="names-tab0" style="">
                                <g:render template="/namelist/dataTableTemplate" model="['type':'a']"/>

                            </div>
                            <div class="tab-pane" id="names-tab1" style="">
                                <g:render template="/namelist/dataTableTemplate" model="['type':'s']"/>
                            </div>
                            <div class="tab-pane" id="names-tab2" style="">
                                <g:render template="/namelist/dataTableCommonTemplate" model="['type':'c']"/>
                            </div>
                            <!--div class="tab-pane" id="names-tab3" style="">
                            <g:render template="/namelist/dataTableReferenceTemplate" model="['type':'r']"/>
                            </div--!>
                        </div>




                        <script id="newRowTmpl" type="text/x-jquery-tmpl">
                            <div class="row-fluid tab_div singleRow new">
                                <span class="tab_form">
                                    {{if typeClass == "rid"}}
                                    <div class="span10">
                                        <input type="hidden" class ="{{>typeClass}}" name="{{>typeClass}}" value=""/>
                                        <input type="text" class="nameInputs span12" name="value">
                                    </div>   
                                    {{else}}
                                    <div class="span4"style ="padding-left:4px;padding-right:4px;">
                                        <input type="hidden" class ="{{>typeClass}}" name="{{>typeClass}}" value=""/>
                                        <input type="text" class="nameInputs span12" name="value">
                                    </div>                                    
                                    {{if typeClass == "cid"}}
                                    <div class="span2">
                                        <select class="languageDropDown span12" >
                                            <g:each in="${Language.list(sort: 'name', order: 'asc')}" var="lang">
                                            <g:if test="${lang.name == 'English'}">
                                            <option value="${lang.name}" selected>${lang.name}</option>
                                            </g:if>
                                            <g:else>
                                            <option value="${lang.name}">${lang.name}</option>
                                            </g:else> 
                                            </g:each>
                                        </select>
                                    </div>
                                    <div class="span2"><input type="text" class="nameInputs span12" name="source"></div>
                                    <div class="span2"><input type="text" class="nameInputs span12" name="contributor"></div> 
                                    {{else}}
                                    <div class="span3"style ="padding-right:4px;"><input type="text" class="nameInputs span12" name="source"></div>
                                    <div class="span3"style ="padding-right:4px;"><input type="text" class="nameInputs span12" name="contributor"></div> 
                                    {{/if}}
                                    {{/if}}    
                                    <div class="span2" style="text-align:center;">
                                        {{if typeClass == "aid"}}
                                        <button class="btn btn-mini btn-primary addEdit disabled" onClick='validateName(this, false);'>Validate Name</button>
                                        {{else}}
                                        <button class="btn btn-mini btn-primary addEdit disabled" onClick='modifyContent(this,"{{>typeClass}}");' rel="add"><i class="icon-ok icon-white"></i></button>
                                        <button class="btn btn-mini delete disabled" onClick='modifyContent(this,"{{>typeClass}}");' rel='delete'><i class="icon-remove"></i></button>
                                        {{/if}}
                                    </div>

                                </span>
                            </div>
                        </script>

                    </div>

                </div> 
                <div class="span4 tableBlock" style="overflow:hidden">
                    <table class='table-striped table-bordered'>
                        <g:each in="${TaxonomyRank.list()}" var="taxon">
                        <tr>
                            <td class='rankStrings'>${taxon.value()}</td>
                            <td class='rankValues'><input type="text" class="taxon taxon${taxon.ordinal()}"></td> 
                        </tr>
                        </g:each>
                    </table>
                    <div>
                        <button id="saveNameDetails" type="button" class="canBeDisabled btn btn-primary input-block-level pull-right" onClick='saveNameDetails(false, false, false)' style="margin-right:2px;">Save </button> 
                    </div>


                </div>
                
                </div>
                
                <div class="row-fluid">
                    <!--input type="hidden" placeholder="" class="span8 position"/-->
                    <!--button type="button" class="save_button btn" onClick='saveHierarchy(false, false)'>Save & Retain</button--!> 
                    <!--div class="btn-group">
                        <a class="btn dropdown-toggle" data-toggle="dropdown" href="#">
                            Move Name to List
                            <span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu">
                            <li><a id="moveToWKG" class="save_button btn btn-link disabled" onClick='saveNameDetails(false, true, false)'>Move to Working List</a> </li>
                            <li><a id="removeFromWKG" class="remove_button btn btn-link disabled" onClick='saveNameDetails(true, false, false)'>Remove from Working List</a> </li>
                            <li><a id="moveToClean" class="save_button btn btn-link disabled" onClick='saveNameDetails(false, false, true)'>Move to Clean List</a> </li>
                            <li><a id="removeFromClean" class="remove_button btn btn-link disabled" onClick='saveNameDetails(false, true, false)'>Remove from Clean List</a></li>
                        </ul>
                    </div-->
                </div>
                <div class="row-fluid">	
                   
                </div>
                      


            </div>

        </div>
        </div>

        <g:render template="/namelist/externalDbResultsTemplate" model="[]"/>
        <g:render template="/namelist/newNamePopupTemplate" model="[]"/>
        <g:render template="/namelist/dialogMsgTemplate" model="[]"/>
        <div id="searching" style="font-weight:bold">Processing...</div>
        <div id="dialog" title="Basic dialog" style="display:none;">
            <p id="dialogMsg"></p>
        </div>

        <div class='row-fluid' style="margin-top:10px">
        	<div class="union-comment">
             	<feed:showAllActivityFeeds model="['rootHolder':observationInstance, feedType:'Specific', refreshType:'manual', 'feedPermission':'editable']" />
                <comment:showAllComments model="['commentHolder':observationInstance, commentType:'super','showCommentList':false]" />
            </div>
        </div>
        <script type="text/javascript">
            var taxonRanks = [];
            <g:each in="${TaxonomyRank.list()}" var="t">
            taxonRanks.push({value:"${t.ordinal()}", text:"${g.message(error:t)}"});
            </g:each>
            </script>
            <asset:script>
            $(document).ready(function() {
                //$(".outer-wrapper").removeClass("container").addClass("container-fluid");
                var taxonBrowserOptions = {
                    expandAll:false,
                    controller:"${params.controller?:'namelist'}",
                    action:"${params.action?:'index'}",
                    expandTaxon:"${params.taxon?true:false}"
                }
                if(${params.taxon?:false}){
                    taxonBrowserOptions['taxonId'] = "${params.taxon}";
                }
                if(${params.classSystem?:false}){
                    taxonBrowserOptions['classSystem'] = "${params.classSystem}";
                }

                var taxonBrowser = $('.taxonomyBrowser').taxonhierarchy(taxonBrowserOptions);	
                $('.icon-question-sign').tooltip();
            });
</asset:script>
</body>
</html>
