<%@page import="species.ScientificName.TaxonomyRank"%>
<%@page import="species.Language"%>
<%@page import="species.NamesMetadata.NameStatus"%>
<%@ page import="species.Species"%>
<%@ page import="species.Classification"%>
<html>
    <head>

        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
        <meta name="layout" content="main" />
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <r:require modules="species_show,curation"/>

        <title>NameList - Curation Interface</title>

    </head>
    <body>

        <div class="row-fluid namelist_wrapper">
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

                        <g:render template="/common/taxonBrowserTemplate" model="['classifications':classifications, 'expandAll':false, 'showCheckBox':false]"/>


                    </div>
                </div>
                <!--div class="taxon_selector_final">
                <div>
                    <button type="button" class="save_button btn" style="height:20px;line-height:11px;" onClick='getOrphanRecoNames()'>Show orphan names</button> 
                </div>
                <div class="row-fluid">
                    <div class="span12">	
                        <input type="text" placeholder="Search" class="span12" style="min-height:22px;"/>
                    </div> 
                </div>
                </div-->
            </div>
            <div class="span3 listarea">
                <div class="dirty_list taxon_selector_wrapper_span">
                    Raw List

                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.rawlist.info')}"></i>
                </div>
                <select class="span12 listSelector">
                    <option value='accDLContent'>Accepted Names</option>
                    <option value='synDLContent'>Synonyms</option>
                    <!--option value='comDLContent'>Common Names</option-->
                </select>
                <div class="dl_content taxon_selector_list listarea_content">
                </div>

                <!--div class="row-fluid">
                <g:render template="/namelist/actionFieldsTemplate" model="['showArrow':false]"/>
                </div-->


            </div>
            <div class="span3 listarea">
                <div class="working_list taxon_selector_wrapper_span">
                    Working List
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.workinglist.info')}"></i>
                </div>
                <select class="span12 listSelector">
                    <option value='accWLContent'>Accepted Names</option>
                    <option value='synWLContent'>Synonyms</option>
                    <!--option value='comWLContent'>Common Names</option-->
                </select>

                <div class="wl_content taxon_selector_list listarea_content">
                </div>

                <!--div class="row-fluid">
                <g:render template="/namelist/actionFieldsTemplate" model="['showArrow':true]"/>
                </div-->
            </div>
            <div class="span3 listarea">
                <div class="clean_list taxon_selector_wrapper_span">
                    Clean List
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.cleanlist.info')}"></i>
                </div>
                <select class="span12 listSelector">
                    <option value='accCLContent'>Accepted Names</option>
                    <option value='synCLContent'>Synonyms</option>
                    <!--option value='comCLContent'>Common Names</option-->
                </select>

                <div class="cl_content taxon_selector_list listarea_content">
                </div>

                <!--div class="row-fluid">
                <g:render template="/namelist/actionFieldsTemplate" model="['showArrow':true]"/>
                </div-->


            </div>


        </div>

        <div class="row-fluid metadataDetails namelist_wrapper">


            <div class="span3 canBeDisabled listarea" style="background:seagreen">
                <div class=" taxon_selector_wrapper_span">
                    Hierarchy
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.hierarchy.info')}"></i>
                </div>

                <table style="width:100%;">
                    <tr>
                        <td class='rankStrings'>Kingdom</td>
                        <td class='rankValues'><input type="text" class="kingdom span12"></td> 
                    </tr>
                    <tr>
                        <td class='rankStrings'>Phylum</td>
                        <td class='rankValues'><input type="text" class="phylum span12 "></td> 
                    </tr>
                    <tr>
                        <td class='rankStrings'>Class</td>
                        <td class='rankValues'><input type="text" class="class span12 "></td> 
                    </tr>
                    <tr>
                        <td class='rankStrings'>Order</td>
                        <td class='rankValues'><input type="text" class="order span12 "></td> 
                    </tr>
                    <tr>
                        <td class="super_family rankStrings">Super-Family</td>
                        <td class='rankValues'><input type="text" class="superfamily span12 "></td> 
                    </tr>
                    <tr>
                        <td class='rankStrings'>Family</td>
                        <td class='rankValues'><input type="text" class="family span12 "></td> 
                    </tr>
                    <tr>
                        <td class="sub_family rankStrings">Sub-Family</td>
                        <td class='rankValues'><input type="text" class="subfamily span12 "></td> 
                    </tr>
                    <tr>
                        <td class='rankStrings'>Genus</td>
                        <td class='rankValues'><input type="text" class="genus span12 "></td> 
                    </tr>

                    <tr>
                        <td class="sub_genus rankStrings">Sub-Genus</td>
                        <td class='rankValues'><input type="text" class="subgenus span12 "></td> 
                    </tr>
                    <tr>
                        <td class='rankStrings'>Species</td>
                        <td class='rankValues'><input type="text" class="species span12 "></td> 
                    </tr>
                    <tr>
                        <td class="infra_species rankStrings">Infra-Species</td>
                        <td class='rankValues'><input type="text" class="infraspecies span12"></td> 
                    </tr>

                </table>

            </div>
            <div class="span7 canBeDisabled listarea" style="background:seagreen">
                <div class=" taxon_selector_wrapper_span">
                    Attributes
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.attributes.info')}"></i>
                </div>


                <input type="hidden" class="taxonRegId" value="">
                <input type="hidden" class="taxonId" value="">
                <input type="hidden" class="recoId" value="">
                <input type="hidden" class="isOrphanName" value="">
                <input type="hidden" class="fromCOL" value=false>
                <input type="hidden" class="id_details" value="">

                <div class="row-fluid form-inline">
                    <div class="span4">
                        <label>Name
                        
                            <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.name.info')}"></i>
                        </label> 
                        <input type="text" placeholder="Name" class="span5" style="width:65%;"/>
                    </div>
                    <div class="span4">
                        <label> Author String
                        
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.authorstring.info')}"></i>
                        </label>
                        <input type="text" placeholder="Author string" class="span3" style="width:45%;"/>
                    </div>
                    <div class="span4">
                        <label>Status
                        
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.status.info')}"></i>
                        </label>
                        <select id="statusDropDown" class="span4" style="width:65%;" >
                            <option value="chooseNameStatus">Choose Name Status</option>
                            <g:each in="${NameStatus.list()}" var="ns">
                            <option value="${ns.toString().toLowerCase()}">${ns}</option>
                            </g:each>
                        </select>
                    </div>
                    
                    <div class="span3" style="margin-left:0px;">
                        <label>Rank
                        
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.rank.info')}"></i>
                        </label>
                        <select id="rankDropDown" class="span7">
                            <option value="chooseRank">Choose Rank</option>
                            <% def rankCount = 0 %>
                            <g:each in="${TaxonomyRank.list()}" var="t">
                            <option value="${t.toString().toLowerCase()}">${t}</option>
                            </g:each>
                        </select>
                    </div>
                    <div class="span3">
                        <label>Source 
                        
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.source.info')}"></i>
                        </label> 
                        <input type="text" placeholder="" class="span7" style=""/>
                    </div>															
                    <div class="span3">
                        <label>via
                        
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.via.info')}"></i>
                        </label>  
                        <input type="text" placeholder="" class="span8" style=""/>
                    </div>
                    <div class="span3">
                        <label>ID
                        
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.id.info')}"></i>
                        </label>  
                        <input type="text" placeholder="" class="span8" style=""/>
                    </div>			
                </div>

                <div class="row-fluid">	
                    <div class="span12 column rt_family" style="background:slategrey">


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



                        <!--button type="button" class="save_button" style="background-color:#C0504D;" onClick='saveHierarchy(false)'>Save & Retain</button--!> 
                        <button type="button" class="save_button" style="background-color:#FF9900;" onClick='saveHierarchy(true)'>Save & Move to Working List</button> 
                        <!--button type="button" class="save_button" style="background-color:#009933;">Save & Move to Clean List</button--!> 
                    </div>


                </div>




            </div>


            <div class="span2 detailsareaRight listarea" >
                <div class=" taxon_selector_wrapper_span">
                    Query 
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.query.info')}"></i>
                </div>


                <div class= row-fluid>
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

                <div class="row-fluid"style=" background: #009933; ">
                    <div class="connection_wrapper" style="background:grey; padding: 5px 0px;font-weight: bold;">Connections
                    
                    <i class="icon-question-sign" data-toggle="tooltip" data-trigger="hover" data-original-title="${g.message(code:'namelist.connections.info')}"></i>
                    </div>

                    <g:render template="/namelist/connectionsTemplate" model="[controller:'species', instanceTotal:speciesInstanceTotal]"/>
                    <g:render template="/namelist/connectionsTemplate" model="[controller:'observation', instanceTotal:observationInstanceTotal]"/>
                    <g:render template="/namelist/connectionsTemplate" model="[controller:'checklist', instanceTotal:checklistInstanceTotal]"/>
                    <g:render template="/namelist/connectionsTemplate" model="[controller:'map', instanceTotal:mapInstanceTotal]"/>
                    <g:render template="/namelist/connectionsTemplate" model="[controller:'document', instanceTotal:documentInstanceTotal]"/>
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

        <div class='row-fluid feedComment' style="margin-top:10px">
        </div>
        <script type="text/javascript">
            var taxonRanks = [];
            <g:each in="${TaxonomyRank.list()}" var="t">
            taxonRanks.push({value:"${t.ordinal()}", text:"${g.message(error:t)}"});
            </g:each>
            <r:script>
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

//modifySourceOnEdit();
//initializeLanguage();
$(".listSelector").change(function () {
    var selectedList = $(this).val();
    var list_content = $(this).parents(".listarea").find(".listarea_content");
    $(list_content).find("ul").remove();
    switch (selectedList) {
    case 'accDLContent':
    $(list_content).append(accDLContent);
    break;
    case 'synDLContent':
    $(list_content).append(synDLContent);
    break;
    case 'comDLContent':
    //$(list_content).append(comDLContent);
    break;
    case 'accWLContent':
    $(list_content).append(accWLContent);
    break;
    case 'synWLContent':
    $(list_content).append(synWLContent);
    break;
    case 'comWLContent':
    //$(list_content).append(comWLContent);
    break;
    case 'accCLContent':
    $(list_content).append(accCLContent);
    break;
    case 'synCLContent':
    $(list_content).append(synCLContent);
    break;
    case 'comCLContent':
    //$(list_content).append(comCLContent);
    break;
    default: alert('Wrong option selected!!')
    }
});
});
</r:script>
</script>

</body>
</html>
