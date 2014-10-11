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
		height:200px;
		//overflow-y:scroll;
	}
    .listarea_content ul {
        list-style-type:none;
        cursor:pointer;
    }
	.taxon_selector_list{

	}
	.taxon_selector_final{

	}
</style>

</head>
<body>

  <div class="row-fluid">
  	<div class="span3 listarea taxon_selector_wrapper">
  		<div class="taxon_selector_span taxon_selector_wrapper_span">
            Taxon Selector
        </div>
        <div class="taxonomyBrowser sidebar_section" style="position: relative;" data-name="classification" data-speciesid="${speciesInstance?.id}">
            <div id="taxaHierarchy">
                <%
                def classifications = [];
                Classification.list().each {
                classifications.add([it.id, it, null]);
                }
                classifications = classifications.sort {return it[1].name};
                %>

                <g:render template="/common/taxonBrowserTemplate" model="['classifications':classifications, 'expandAll':false]"/>


            </div>
        </div>
        <div class="taxon_selector_final">
            <div class="row-fluid">
                <div class="span12">	
                    <input type="text" placeholder="Search" class="span12"/>
                </div> 
            </div>
        </div>
    </div>
    <div class="span3 listarea">
        <span class="dirty_list_span taxon_selector_wrapper_span">
            Dirty List
        </span>
        <div class=" dl_content taxon_selector_list listarea_content">
        </div>

        <div class="row-fluid">
            <div class="span2">
                <button><i class="icon-trash"></i></button>
            </div>
            <div class="span2">
                <button><i class="icon-refresh"></i></button>
            </div>
            <div class="span8">	
                <input type="text" placeholder="Search" class="span12"/>
            </div>
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
	}
	.connection_wrapper{
		background-color:#00FFCC;
	}
	.connection_wrapper_row1{
		background-color: rgb(218, 150, 70);
	}
	.connection_wrapper_row2{
		background-color: burlywood;
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
</style>

 <div class="row-fluid">

	<div class="span10">
	
	
				<div class="row-fluid">
					
					<div class="span3 column">
							<div class ="row-fluid">
								<div class="span4"><label>Name</label></div>
								<div class="span8"> <input type="text" placeholder="Name" class="name span12"/></div>
							</div>
							<div class="row-fluid">	
								<div class="span4"><label>Rank</label></div>
								<div class="span8"> <select class="rank span12" ><option>Name Status</option></select></div>
							</div>	
							
					</div>
				
					<div class="span9 column">
							<div class="row-fluid">
									<div class="span6">
											Author String :- <input type="text" placeholder="Name" class="authorString span6"/>
									</div>
									<div class="span6">
											Status :- <select class="status span9" ><option>Name Status</option></select>
									</div>
							</div>
							
							<div class="row-fluid">
									<div class="span4">
										Source :- <input type="text" placeholder="" class="source span6"/>
									</div>															
									<div class="span4">
										via :- <input type="text" placeholder="" class="via span6"/>
									</div>
									<div class="span4">
										ID :- <input type="text" placeholder="" class="id span6"/>
									</div>			
							</div>
							
							
					
					</div>
				</div>
				
                <div class="row-fluid">

                    <div class="span3 column">
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
                                <td>Superfamily</td>
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
						
					<div class="span9 column">
						

                        <ul class="nav nav-tabs" id="" style="margin:0px;background-color:transparent;">
                            <li id="names-li0" class="active"><a href="#names-tab0" class="btn" data-toggle="tab">Accepted Name</a></li>
                            <li id="names-li1"><a href="#names-tab1" class="btn" data-toggle="tab">Synonyms</a></li>
                            <li id="names-li2"><a href="#names-tab2" class="btn" data-toggle="tab">Cmmon Names</a></li>   
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
                        <button type="button" class="btn">Save & retain</button> 
                        <button type="button" class="btn">Save & Move to WKG</button> 
                        <button type="button" class="btn">Save & Move to Clean List</button> 
                    </div>


                </div>

	
	</div>
  	<div class="span2 column detailsareaRight">
  
		  <div class="connection_wrapper">Connections</div>
		  
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
 <script type="text/javascript">
    var taxonRanks = [];
    <g:each in="${TaxonomyRank.list()}" var="t">
        taxonRanks.push({value:"${t.ordinal()}", text:"${g.message(error:t)}"});
        </g:each>
        console.log("=========================================");
    console.log(taxonRanks);
    <r:script>
        $(document).ready(function() {
            var taxonBrowser = $('.taxonomyBrowser').taxonhierarchy({
                expandAll:false
            });	
            /*$("#searchPermission").autofillNames({
                'appendTo' : '#nameSuggestions',
                'nameFilter':'scientificNames',
                focus: function( event, ui ) {
                    $("#canName").val("");
                    $("#page").val( ui.item.label.replace(/<.*?>/g,"") );
                    $("#nameSuggestions li a").css('border', 0);
                    return false;
                },
                select: function( event, ui ) {
                    $("#page").val( ui.item.label.replace(/<.*?>/g,"") );
                    $("#canName").val( ui.item.value );
                    $("#mappedRecoNameForcanName").val(ui.item.label.replace(/<.*?>/g,""));
                    return false;
                },open: function(event, ui) {
                    //$("#nameSuggestions ul").removeAttr('style').css({'display': 'block','width':'300px'}); 
                }
            });

            var users_autofillUsersComp = $("#userAndEmailList_user").autofillUsers({
                usersUrl : window.params.userTermsUrl
            });

            $('#searchPermission').click(function() {
                var params = {};
                $("#addSpecies input").each(function(index, ele) {
                    if($(ele).val().trim()) params[$(ele).attr('name')] = $(ele).val().trim();
                });
                params['rank'] = $('#rank').find(":selected").val(); 
                $.ajax({
                    url:'/species/searchPermission',
                    data:params,
                    method:'POST',
                    dataType:'json',
                    success:function(data) {
                    console.log(data);
                        if(data.success == true) {

                        } else {
                            $('#errorMsg').removeClass('alert-info hide').addClass('alert-error').text(data.msg);
                        }
                    }, error: function(xhr, status, error) {
                        handleError(xhr, status, error, this.success, function() {
                            var msg = $.parseJSON(xhr.responseText);
                            $(".alertMsg").html(msg.msg).removeClass('alert-success').addClass('alert-error');
                        });
                    }
                });
            });*/
        });
        </r:script>
</script>

</body>
</html>
