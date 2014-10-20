<%@page import="species.ScientificName.TaxonomyRank"%>
<%@ page import="species.Species"%>
<%@ page import="species.Classification"%>
<html>
<head>
    
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<meta name="layout" content="main" />

<r:require modules="species_show,curation"/>

<title>NameList - Curation Interface</title>
<style type="text/css">
    input[type=text], select
    {
        min-height:22px !important;
    }
    .nav-tabs>li>a {
        padding:5px 10px !important;
        line-height:10px !important;
    }
    .nav-tabs > li.active > a,
    .nav-tabs > li.active > a:hover,
    .nav-tabs > li.active > a:focus {
      color: #555555 !important;
      cursor: default !important;
      background-color: #ffffff !important;
      border: 1px solid #dddddd !important;
      border-bottom-color: transparent !important;
      }
     .tab-content{
        overflow-y: scroll;
        height: 221px;
     }   
	.listarea{
		border:1px solid #ccc;
		height:250px;
		margin:0px !important;
	}
	.taxon_selector_wrapper{
		width:285px!important;
	}

	.taxon_selector_wrapper_span{
		color:white;
		padding: 3px 65px;
	}
	.taxon_selector_span{
		background-color:#00FFFF;			
	}
	.dirty_list_span{
		background-color:#66FF33;
	}
	.working_list_span{
		background-color:#FF99CC;
	}



	.clean_list_span{
		background-color:#9933FF;
	}    
	.listarea_content{
        position: relative;
        overflow-y: scroll;        
        height: 196px;
        width: 222px;
        margin: 0px;
	}
    .listarea_content ul {
        list-style-type:none;
        cursor:pointer;
    }
    .taxonomyBrowser{    
        width: 283px;    
    }
	.taxon_selector_list{

	}
	.taxon_selector_final{

	}
</style>

</head>
<body>

  <div class="row-fluid namelist_wrapper">
  	<div class="span3 listarea taxon_selector_wrapper">
  		<div class="taxon_selector_span taxon_selector_wrapper_span">
            Taxon Selector
        </div>
        <div class="taxonomyBrowser listarea_content" style="position: relative;" data-name="classification" data-speciesid="${speciesInstance?.id}">
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
        <div class="taxon_selector_final">
            <div class="row-fluid">
                <div class="span12">	
                    <input type="text" placeholder="Search" class="span12" style="min-height:22px;"/>
                </div> 
            </div>
        </div>
    </div>
    <div class="span3 listarea">
        <span class="dirty_list_span taxon_selector_wrapper_span">
            Dirty List
        </span>
        <div class="dl_content taxon_selector_list listarea_content">
        </div>

        <div class="row-fluid">
            <g:render template="/namelist/actionFieldsTemplate" model="['showArrow':false]"/>
        </div>


    </div>
    <div class="span3 listarea">
        <span class="working_list_span taxon_selector_wrapper_span">
            Working List
        </span>
        <div class="wl_content taxon_selector_list listarea_content">
                   
        </div>

        <div class="row-fluid">

            <g:render template="/namelist/actionFieldsTemplate" model="['showArrow':true]"/>
        </div>
    </div>
    <div class="span3 listarea">
        <span class="clean_list_span taxon_selector_wrapper_span">
            Clean List
        </span>
        <div class=" cl_content taxon_selector_list listarea_content">

        </div>

        <div class="row-fluid">
            <g:render template="/namelist/actionFieldsTemplate" model="['showArrow':true]"/>
        </div>


    </div>


</div>




<style type="text/css">
	.detailsarea{
		//height: 250px;
    	border: 1px solid#ccc;
	}
	.detailsareaLeft{
	    border: 1px solid #ccc;
	}
	.detailsareaRight{
	    margin: 0px !important;
	    border: 1px solid #ccc;
	    width: 161px !important;
	    text-align:center;
        height: 350px;
	}
	.connection_wrapper{
		background-color:#00FFCC;
	}
	.connection_wrapper_row1{
		background-color: rgb(218, 150, 70);
	}
	.connection_wrapper_row2{		
        background-color: burlywood;
        height: 18px;
        text-align: center;
        padding: 5.7px;
    }
    .taxonomyRanks {
	    border: 1px solid #ccc;
    }
    .nameInputs {
        width: 164px;
    }
    .column {
        padding: 1px 0px;
        margin:0px !important;
        border: 1px solid #c6c6c6;
        border-collapse: separate;
        -webkit-border-radius: 4px;
        -moz-border-radius: 4px;
        border-radius: 4px;
    }
    .tab-content td, .tab-content th {
        text-align:center;
	    border: 1px solid #ccc;
    }
    .tab_heading{
        font-size:13px;
    }
</style>

 <div class="row-fluid">

	<div class="span10 metadataDetails">
	
	
				<div class="row-fluid">
					
					<div class="span3 canBeDiasbled column">
							<div class ="row-fluid">
								<div class="span3"><label>Name</label></div>
								<div class="span9"> <input type="text" placeholder="Name" class="name span12"/></div>
                            </div>
                            <input type="hidden" class="taxonReg" value="">
                            <input type="hidden" class="fromCOL" value=false>
                            <input type="hidden" class="id_details" value="">
							<div class="row-fluid">	
                                <div class="span3"><label>Rank</label></div>
                                <div class="span9"> 
                                    <select id="rankDropDown" class="rankDropDown span12" >
                                        <option value="chooseRank">Choose Rank</option>
                                        <g:each in="${TaxonomyRank.list()}" var="t">
                                            <option value="${t.toString().toLowerCase()}">${t}</option>
                                        </g:each>
                                    </select>
                                </div>
							</div>	
							
					</div>
				
					<div class="span9 canBeDiasbled column" style="width: 610px;">
							<div class="row-fluid">
									<div class="span6">
											Author String :-
                                            <input type="text" placeholder="Name" class="authorString span8"/>
									</div>
									<div class="span6">
                                        Status :- <select id="statusDropDown" class="statusDropDown span9" >
                                            <option value="chooseNameStatus">Choose Name Status</option><option value="accepted">Accepted Name</option><option value="synonym">Synonym</option>
                                        </select>
									</div>
							</div>
							
							<div class="row-fluid">
									<div class="span4">
										Source :- <input type="text" placeholder="" class="source span8"/>
									</div>															
									<div class="span4">
										via :- <input type="text" placeholder="" class="via span9"/>
									</div>
									<div class="span4">
										ID :- <input type="text" placeholder="" class="id span9"/>
									</div>			
							</div>
							
							
					
					</div>
				</div>
				
                <div class="row-fluid">

                    <div class="span3 canBeDiasbled column">
                        <table style="width:100%">
                            <tr>
                                <td>Kingdom</td>
                                <td><input type="text" class="kingdom span12"></td> 
                            </tr>
                            <tr>
                                <td>Phylum</td>
                                <td><input type="text" class="phylum span12"></td> 
                            </tr>
                            <tr>
                                <td>Class</td>
                                <td><input type="text" class="class span12"></td> 
                            </tr>
                            <tr>
                                <td>Order</td>
                                <td><input type="text" class="order span12"></td> 
                            </tr>
                            <tr>
                                <td>Super-Family</td>
                                <td><input type="text" class="superfamily span12"></td> 
                            </tr>
                            <tr>
                                <td>Family</td>
                                <td><input type="text" class="family span12"></td> 
                            </tr>
                            <tr>
                                <td>Genus</td>
                                <td><input type="text" class="genus span12"></td> 
                            </tr>
                            <tr>
                                <td>Species</td>
                                <td><input type="text" class="species span12"></td> 
                            </tr>

                        </table>

                    </div>
						
					<div class="span9 column" style="width: 610px;">
						

                        <ul class="nav nav-tabs" id="" style="margin:0px;">
                            <li id="names-li0" class="active"><a href="#names-tab0" class="btn" data-toggle="tab">Accepted Name</a></li>
                            <li id="names-li1"><a href="#names-tab1" class="btn" data-toggle="tab">Synonyms</a></li>
                            <li id="names-li2"><a href="#names-tab2" class="btn" data-toggle="tab">Common Names</a></li>   
                            <li id="names-li3"><a href="#names-tab3" class="btn" data-toggle="tab">Reference(s)</a></li>   
                        </ul>

                        <div class="tab-content" id="names-tab-content">
                            <div class="tab-pane active" id="names-tab0" style="">
                                <g:render template="/namelist/dataTableTemplate" model="[]"/>

                            </div>
                            <div class="tab-pane" id="names-tab1" style="">
                                <g:render template="/namelist/dataTableTemplate" model="[]"/>
                            </div>
                            <div class="tab-pane" id="names-tab2" style="">
                                <g:render template="/namelist/dataTableTemplate" model="[]"/>
                            </div>
                            <div class="tab-pane" id="names-tab3" style="">
                                <g:render template="/namelist/dataTableTemplate" model="[]"/>
                            </div>
                        </div>
                        <button type="button" class="btn btn-success" onClick='saveHierarchy()'>Save & retain</button> 
                        <button type="button" class="btn btn-primary">Save & Move to WKG</button> 
                        <button type="button" class="btn btn-danger">Save & Move to Clean List</button> 
                    </div>


                </div>

	
	</div>
  	<div class="span2 column detailsareaRight">
        
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
            <button class="btn queryString span12" style="margin:0px; margin-bottom: 6px;" onClick='searchDatabase()'>Query <i class="icon-search" style="margin-left: 10px;"></i></button>
            </div>
        </div>

        <div class="row-fluid">
		  <div class="connection_wrapper" style="padding: 5px 0px;font-weight: bold;">Connections</div>
		  
		  <div class="connection_wrapper_row1">Species Page</div>
		  
		  <div class="connection_wrapper_row2">123456</div>

		  <div class="connection_wrapper_row1">Observations</div>
		  
		  <div class="connection_wrapper_row2">123456</div>
		  
		  <div class="connection_wrapper_row1">Lists</div>
		  
		  <div class="connection_wrapper_row2">123456</div>
		  
		  <div class="connection_wrapper_row1">Maps</div>
		  
		  <div class="connection_wrapper_row2">123456</div>
		  
		  <div class="connection_wrapper_row1">Documents</div>
		  
		  <div class="connection_wrapper_row2">123456</div>
      </div>

	</div>
</div>

    <g:render template="/namelist/externalDbResultsTemplate" model="[]"/>
    
<script type="text/javascript">
    var taxonRanks = [];
    <g:each in="${TaxonomyRank.list()}" var="t">
        taxonRanks.push({value:"${t.ordinal()}", text:"${g.message(error:t)}"});
        </g:each>
    <r:script>
        $(document).ready(function() {
            var taxonBrowser = $('.taxonomyBrowser').taxonhierarchy({
                expandAll:false,
                showCheckBox:false
            });	
            
        });
        </r:script>
</script>

</body>
</html>
