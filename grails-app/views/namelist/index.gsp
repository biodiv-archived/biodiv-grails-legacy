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
<style>
.selectedNamesWrapper {
  min-height: 360px;
  overflow-y: scroll;
  overflow-x:hidden;
  height: 360px;
  }
.selectedNamesWrapper  .header{
        font-weight:bold;
}
.mergeWrapper{
    border: 1px solid #ccc;
  padding: 10px 5px;
  margin-bottom: 5px;
  background-color: aliceblue;
  height:50px;
}
.mergeWrapper h6{
    margin:0px;
}
.mergeWrapper select{
    width: 100%;
}

.popupModal{
  width: 1050px;
  left: 30%;
}
.rowSel{
    border-bottom:1px solid #ccc;
    padding-top: 5px;
}
.rowSelActive{
      background-color: burlywood;
}
.disableSciName{
    opacity: 0.2;
}
#taxonHierachyInput{
    width:400px;
}
#taxonHierarchyModal{
  width: 700px;
  left: 45%;
}
#taxonHierarchyModal, #taxonHierarchyModal .modal-body{    
    max-height:100%;
}

#dialogMsg,#externalDbResults,#newNamePopup{
    z-index: 99999999;
}
.ui-menu .ui-menu-item {
  margin: 0;
  padding: 0;
  width: 100%;
  list-style-image: url(data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7);
}
.editNameAttr{
    margin-left: 60px;
}
#taxonHierarchyModal .control-label{
    text-align:right;
}
#taxonHierarchyModal #positionDropDown{
    margin-left:15px;
}
#taxonHierarchyModal #statusDropDown{
    margin-left:15px;
}
#taxonHierarchyModal #rank{
    height:30px;
}
#taxonHierarchyModal #page{
    width: 400px;
}
#taxonHierarchyModal .synToAccWrap{
    margin-left: 254px;
}
#taxonHierarchyModal label{
    font-weight:bold;
}
#parserInfo{
    margin-left: 36px;
}
.ui-autocomplete{
    font-size:14px;
}
.singleNameUpdate{
    float: right;
    margin-right: 55px;
}
</style>
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

               <div id="taxonGrid" class="dl_content" style="height:309px"></div>
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
        <sUser:isAdmin>
        <div class="span4 offset8 clickSelectedRowWrap" style="margin-bottom: 10px;display:none;">
            <div class="btn btn-primary btn-large clickSelectedRow">Action on selected names</div>
        </div>
        </sUser:isAdmin>
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
                        <input type="text" placeholder="Name" class="name scientificNameInput" style="width:81.6%;display: inline-block;margin-bottom: 0;vertical-align: middle;" disabled/>
                        <!-- div class="btn btn-success btn-small nl_edit_name" style="position: relative;float: right;top: -28px;right: 4px;">Edit</div>
                        <div class="btn btn-primary btn-small nl_edit_name_validate" style="position: relative;float: right;top: -28px;right: 4px;display:none;">Validate Name</div -->
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
                    <g:render template="/namelist/createStatusTemplate" model="[requestParams:requestParams,NameStatus:NameStatus]" />
                    
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

                    <g:render template="/namelist/createPositionTemplate" model="[NamePosition:NamePosition]" />

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
                <div class="span4 tableBlock taxonomyRankTable" style="overflow:hidden">
                    <table class='table-striped table-bordered'>
                        <g:each in="${TaxonomyRank.list()}" var="taxon">
                        <tr>
                            <td class='rankStrings'>${taxon.value()}</td>
                            <td class='rankValues'><input type="text" class="taxon taxon${taxon.ordinal()}"></td> 
                        </tr>
                        </g:each>
                    </table>
                    <div>
                        <div class="btn btn-primary btn-large editNameAttr">Edit Name Attributes</div>
                        <!--button id="saveNameDetails" type="button" class="canBeDisabled btn btn-primary input-block-level pull-right" onClick='saveNameDetails(false, false, false)' style="margin-right:2px;">Save </button --> 
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




 
<!-- Modal -->
<div id="myModal" class="modal popupModal hide fade" tabindex="-1" role="dialog" data-keyboard="false" data-backdrop="static" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-header">
    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
    <h3 id="myModalLabel">Action on selected names</h3>
  </div>
  <div class="modal-body">
  <div class="row-fluid">
    <div class="span9">
        <div class="selectedNamesWrapper">
        </div>
        <a class="btn btn-small btn-danger removeSelName disabled" href="javascript:void(0);" style="margin-top: 10px;">Remove Selected</a>
    </div>
    
    <div class="span3">                     
        <div class="mergeWrapper">
            <h6>Move to</h6>
            <select class="movePosition" name="movePosition">
                <option value="">Choose Position</option>
                <option value="Raw">Raw</option>
                <option value="Working">Working</option>
                <option value="Clean">Clean</option>
            </select>
            <a href="javascript:void(0);" class="btn btn-small btn-success selSub pull-right" onclick="updatePosition($(this));" style="display:none;">submit</a>
        </div>
        <div class="mergeWrapper">
            <h6>Merge With</h6>
            <select class="mergeTarget" name="mergeTarget"> 
                
            </select>
            <a href="javascript:void(0);" class="btn btn-small btn-success selSub pull-right" onclick="mergeWithSource($(this));" style="display:none;">submit</a>
        </div>
        <div class="mergeWrapper">
            <h6>Make synonyms of</h6>
            <select class="changeSynTarget" name="changeSynTarget">
                <option value="">Choose Name</option>
            </select>
            <a href="javascript:void(0);" class="btn btn-small btn-success selSub pull-right" onclick="changeAccToSyn($(this));" style="display:none;">submit</a>
        </div>           
        <!-- div class="mergeWrapper">
            <h6>Make as Accepted</h6>
            <div class="btn btn-small span12 btn-primary" style="margin: 0px;">Make As Accepted</div>
        </div -->
         <div class="mergeWrapper">
            <h6>Delete</h6>
            <div class="btn btn-small span12 btn-danger" onclick="deleteSourceName($(this));" style="margin: 0px;">Delete</div>
        </div> 
    </div>
</div>    
   
  </div>  
</div>



<!-- Modal -->
<div id="taxonHierarchyModal" class="modal popupModal hide fade" tabindex="-1" role="dialog" data-keyboard="false" data-backdrop="static" aria-labelledby="myModalLabel" aria-hidden="true">  
  <div class="modal-body">
  <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
  <form id="addSpeciesPage" class="form-horizontal">
    <g:render template="/species/createSpeciesTaxonTemplate" model="[TaxonomyRank:TaxonomyRank,requestParams:requestParams,isPopup:true, validate:true, errors:errors]" />
    <input type="hidden" name="newPath" class="newTaxonPath" value="" /> 
    <div style="margin-left: -48px;"><g:render template="/namelist/createPositionTemplate" model="[NamePosition:NamePosition,isPopup:true]" /></div>
    <div style="margin-left: -48px;"><g:render template="/namelist/createStatusTemplate" model="[NameStatus:NameStatus,isPopup:true]" /></div>
    <input type="hidden" name="FormName" class="checkFormName" value=""/>
    <div class="btn btn-success singleNameUpdate">Save</div>
    </form>
  </div>  
</div>





        <script type="text/javascript">
            var taxonRanks = [];
            <g:each in="${TaxonomyRank.list()}" var="t">
            taxonRanks.push({value:"${t.ordinal()}", text:"${g.message(error:t)}"});
            </g:each>
            </script>
            <asset:javascript src="slickgrid.js"/>
            <asset:javascript src="biodiv/curation.js"/>

            <asset:script>

            function attachAutofill(){
                $("#taxonHierarchyModal #page").autofillNames({
                    'appendTo' : '#nameSuggestions',
                    'nameFilter':'scientificNames',
                    focus: function( event, ui ) {
                        $("#canName").val("");                        
                        $(this).val( ui.item.label.replace(/<.*?>/g,"") );
                        $("#nameSuggestions li a").css('border', 0);
                        return false;
                    },
                    select: function( event, ui ) {
                        $(this).val( ui.item.label.replace(/<.*?>/g,"") );
                        $(this).parent().find('.recoId').val(ui.item.recoId);
                        console.log(ui);
                        return false;
                    },open: function(event, ui) {
                        //$("#nameSuggestions ul").removeAttr('style').css({'display': 'block','width':'300px'}); 
                    }
                });
               // alert($("#taxonHierachyInput input[type=text]").length);
               // alert("passed");
                $("#taxonHierachyInput input[type=text]").each(function(){                 
                    $(this).autofillNames({
                        'appendTo' : '#nameSuggestions',
                        'nameFilter':'scientificNames',
                        focus: function( event, ui ) {
                            //alert("sdsds");
                            return false;
                        },
                        select: function( event, ui ) {
                            $(this).val( ui.item.label.replace(/<.*?>/g,"") );
                            $(this).parent().find('.recoId').val(ui.item.recoId);
                            console.log(ui); 
                            console.log(event);
                            enableValidButton($(this).parent());
                            //alert(ui); 
                            return false;
                        },open: function(event, ui) {
                            //alert("Test");
                            //$("#nameSuggestions ul").removeAttr('style').css({'display': 'block','width':'300px'}); 
                        }
                    });
                });
            }

            function updateHirRank(selValue){
                $.each(taxonRanks, function(index, item) {                        
                    if(index < selValue){
                        $('.hie_'+index).show();
                    }else{
                        $('.hie_'+index).hide();
                    }
                });
            }

            function checkHirInput(rank){
                var result = true;
                for(var i=0; i< taxonRanks.length; i++) {                    
                    var tRank = parseInt(taxonRanks[i].value);                    
                    if(tRank < parseInt(rank)){ 
                        console.log(result);                       
                        if(($('.hie_'+tRank+' .taxonRank').val() == "") && ($.inArray(tRank,[4,6,8]) == -1)){
                            result = false;                            
                        }
                    }
                    if(!result)
                        break;
                }
                return result;
            }
            
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

                $('.nl_edit_name').click(function(){
                    //enableValidButton($(this).parent());
                    $(this).hide();
                    $(this).parent().find('.scientificNameInput').attr('disabled',false);
                    var vButton = $(this).next();
                    vButton.removeClass('btn-primary').addClass('btn-success disabled');
                    vButton.html('Validated').show();
                    //$(this).next().show();
                });

                $('.removeSelName').click(function(){
                        // TODO Remove selected 
                });

                $('#validateSpeciesSubmit').click(function() {
                    isSearchCol=true;
                    var params = {};
                    var that = $(this);
                    that.parent().find('input').each(function(index, ele) {
                        console.log($(ele).attr('name'));
                        if($(ele).val().trim()) params[$(ele).attr('name')] = $(ele).val().trim();
                    });
                    params['rank'] = that.parent().find('#rank').find(":selected").val();                  
                    params['namelist'] = true;
                    //Did u mean species 
                    $.ajax({
                        url:'/species/validate',
                        data:params,
                        method:'POST',
                        dataType:'json',
                        success:function(data) {
                            if (data.id){
                                data['namelist'] = true;
                                //alert(namelistDefault);
                                namelistDefault=true;
                                }
                            console.log("Passed");
                            console.log(data);
                            setRank = data.rank;
                            validateSpeciesSuccessHandler(data, true);
                        }, error: function(xhr, status, error) {
                            handleError(xhr, status, error, this.success, function() {
                                var msg = $.parseJSON(xhr.responseText);
                                $(".alertMsg").html(msg.msg).removeClass('alert-success').addClass('alert-error');
                            });
                        }
                    });
                    //get COL hierarchy 
                    // get and autofill author contrib hierarchy
        
                });

                $('.editNameAttr').click(function(){
                        var data = {}
                        data['taxonRanks']=taxonRanks;
                        var taxonRegistry = [];
                        for(var i=0;i<11;i++){
                            var tR = $('.taxonomyRankTable .taxon'+i).val();
                            //alert(tR);
                            taxonRegistry[i]= (tR)?tR:"";
                        }
                        var statusDropDown = $(".attributesBlock #statusDropDown").val();
                        var rankValue = getSelectedRank($('.attributesBlock #rankDropDown').val(),'name');
                        var canName   = $('.attributesBlock .canonicalForm').val();
                        var authorName =  $('.attributesBlock .authorString').val();
                        var requestParams = {"taxonRegistry":taxonRegistry};
                        var position = $(".attributesBlock #positionDropDown").val();
                        data['requestParams']= requestParams;
                        console.log(data);
                        console.log("rankValue = "+rankValue);
                        nameRank = 11;
                        
                        $('#taxonHierarchyModal #page').val($(".attributesBlock .scientificNameInput").val());
                        $('#taxonHierarchyModal #positionDropDown').val(position).data('prev',position);
                        $('#taxonHierarchyModal #statusDropDown').val(statusDropDown);
                        $('#taxonHierarchyModal #rank').val(rankValue);
                        $('#taxonHierarchyModal #canName').val(canName);
                        
                        $('#taxonHierarchyModal #parserInfo .canonicalName').html(canName);
                        $('#taxonHierarchyModal #parserInfo .authorYear').html(authorName);
                        updateHirInput(data);
                        updateHirRank(rankValue);
                        attachAutofill();
                        $('#taxonHierarchyModal .checkFormName').val(statusDropDown);
                        if(statusDropDown == 'synonym'){
                            $('#taxonHierarchyModal #taxonHierarchyInputForm').hide();                                                      
                            $('#taxonHierarchyModal #rank').attr('disabled',false);
                        }else{
                            $('#taxonHierarchyModal #rank').attr('disabled',true);
                            $('#taxonHierarchyModal #taxonHierarchyInputForm').show();
                        }
                        $('#taxonHierarchyModal').modal('show');                        
                                                
                });

                $('#taxonHierarchyModal #positionDropDown').change(function(){
                    var selVal = $(this).val();
                    console.log(selVal);
                    if(selVal =='choosePosition')
                        return;
                    if((selVal == 'working' || selVal == 'clean') && $('#taxonHierarchyModal .checkFormName').val() != 'synonym'){
                        var rank = $('#taxonHierarchyModal #rank').val();
                        if(!checkHirInput(rank)){
                            alert("Please fill & validate the Mandatory Hierarchy");
                            $(this).val($(this).data('prev'));
                            return;
                        }
                    }
                });

                $('#taxonHierarchyModal #statusDropDown').change(function(){
                        var that = $(this);                        
                        var status = that.val();
                        that.parent().find(".synToAccWrap").hide();
                        if(status == 'synonym' && $('#taxonHierarchyModal .checkFormName').val() != 'synonym'){
                            that.parent().find(".synToAccWrap").show();
                        }else if(status == 'accepted'){
                            $('#taxonHierarchyModal #taxonHierarchyInputForm').show();
                        }
                });

                $('#taxonHierarchyModal #rank').change(function(){
                    var selValue = $(this).val();
                    updateHirRank(selValue);
                });

                $('.taxonRank').click(function(){
                        var that = $(this);
                        console.log(that.parent());
                });

                $('.singleNameUpdate').click(function(){
                    var params = {}
                    params['taxonId'] = $('.taxonId').val();
                    params['status']   =$('#taxonHierarchyModal #statusDropDown').val();
                    if(params['status'] == 'synonym'){
                        params['newRecoId'] = $('.synToAccWrap').find('.recoId').val();
                    }
                    params['position'] =$('#taxonHierarchyModal #positionDropDown').val();
                    addSpeciesPage('/namelist/singleNameUpdate',params);
                    return false;                   
                });
            });
</asset:script>
</body>
</html>
