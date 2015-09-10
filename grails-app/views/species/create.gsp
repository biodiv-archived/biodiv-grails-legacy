<%@ page import="species.Species"%>
<%@ page import="species.TaxonomyDefinition"%>
<%@ page import="species.ScientificName.TaxonomyRank"%>
<html>
    <head>
        <g:set var="title" value="${g.message(code:'default.species.label')}"/>
        <g:render template="/common/titleTemplate" model="['title':title]"/>
        <r:require modules="observations_create, curation"/>
        <style>
            #addSpeciesPage .add-on {
            height:20px;
            width:98px;
            }
            #addSpeciesPage select.add-on {
            height:30px;
            width:110px;
            }


            #addSpeciesPage .taxonRank {
            min-height:20px;
            width:400px;  
            padding-bottom: 0px;
            }
        </style>
    </head>
    <body>

        <div class="observation_create">
            <h1> <g:message code="species.create.add.species" /> </h1>
            <g:hasErrors bean="${speciesInstance}">
            <i class="icon-warning-sign"></i>
            <span class="label label-important"> <g:message
                code="fix.errors.before.proceeding" default="Fix errors" /> </span>
            <%--<g:renderErrors bean="${speciesInstance}" as="list" />--%>
            </g:hasErrors>

            <form id="addSpeciesPage" action="${uGroup.createLink(action:'save', controller:'species', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" method="POST" class="form-horizontal">

                <div class="span12 super-section" style="clear:both;">

            <div class="section help-block"> 
                <ul>
                    <li>
                    <g:message code="species.create.contributors.create.rights" /> <a href="${uGroup.createLink('controller':'species', 'action':'taxonBrowser')}"><g:message code="msg.here" /></a>.
                    </li>
                    <li>
                   <g:message code="species.create.validate.species.name" /> 
                    </li>
                </ul>
           </div>

                    <div class="section">
                    <div class="control-group">
                        <label class="control-label span3" for="name"><g:message code="default.add.page.label" /> </label> 
                        <div class="pull-left" style="width:700px;margin-left:20px;">
 
                            <div class="input-prepend">
                                <select id="rank" name="rank" class="add-on">
                                <g:each in="${TaxonomyRank.list().reverse()[0..1]}" var="rank">
                                    <option value="${rank.ordinal()}" ${(requestParams?requestParams.rank:-1) == rank?'selected':''}><g:message error="${rank}"/></option>
                                </g:each>
                            </select>

                            <input id="page" 
                            data-provide="typeahead" type="text" class="taxonRank" style=""
                            name="page" value="${requestParams?requestParams.speciesName:''}" data-rank="${requestParams?requestParams.rank:TaxonomyRank.SPECIES.ordinal()}"
                            placeholder="${g.message(code:'placeholder.create.add')}" />
                            <input type="hidden" name="canName" id="canName" value=""/>
                            <div id="nameSuggestions" style="display: block;position:relative;"></div>
                            <input type="hidden" name="lang" value="${lang?:params.lang}"/>
                            <input type="hidden" name="colId" value="${params.colId}"/>

                        </div>
                        <div id="parserInfo" style="margin-top:10px; display:none;">
                        	<label style="float:left;">Canonical Name :</label> <div  class="canonicalName"> </div>
                        	<label style="clear:both; float:left;">Author Year:</label> <div class="authorYear"> </div>
                        </div>
                        <div id="errorMsg" class="alert hide" style="clear:both;"></div>
                        </div>
                    </div>  
                    <g:render template="/common/createTaxonRegistryTemplate" model='[requestParams:requestParams, errors:errors]'/>
                    </div>
					<g:render template="/namelist/externalDbResultsTemplate" model="[]"/>
					 <g:render template="/namelist/dialogMsgTemplate" model="[]"/>
                </div>   
                <div class="span12 submitButtons">

                    <g:if test="${speciesInstance?.id}">
                    <a href="${uGroup.createLink(controller:params.controller, action:'show', id:speciesInstance.id)}" class="btn"
                        style="float: right; margin-right: 30px;"> <g:message code="button.cancel" /> </a>
                    </g:if>
                    <g:else>
                    <a href="${uGroup.createLink(controller:params.controller, action:'list')}" class="btn"
                        style="float: right; margin-right: 30px;"> <g:message code="button.cancel" /> </a>
                    </g:else>
                    <a id="validateSpeciesSubmit" class="btn btn-primary"
                        style="float: right; margin-right: 5px;"> <g:message code="button.validate" /></a>


                    <a id="addSpeciesPageSubmit" class="btn btn-primary"
                        style="float: right; margin-right: 5px;display:none;"> <g:message code="default.add.page.label" /></a>

                </div>

            </form>
        </div>

    </body>
    <r:script>
    $(document).ready(function() {
        $("#page").autofillNames({
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
        
 
        var text1 = $('#page').data('rank');
        $('#rank option').filter(function() {
            return $(this).val() == text1; 
        }).prop('selected', true);

        $('#rank').change(function() {
            $('#page').attr('data-rank', $('#rank').find(':selected').val());
        });

        <g:if test="${requestParams}">
            var $hier = $('#taxonHierachyInput');
            $hier.empty();
            var rank = <%=requestParams?requestParams.rank:0%> ;
            for (var i=0; i<rank; i++) {
                $('<div class="input-prepend"><span class="add-on">'+taxonRanks[i].text+(taxonRanks[i].mandatory?'*':'')+'</span><input data-provide="typeahead" data-rank ="'+taxonRanks[i].value+'" type="text" class="taxonRank" name="taxonRegistry.'+taxonRanks[i].value+'" value="'+taxonRanks[i].taxonValue+'" placeholder="Add '+taxonRanks[i].text+'" /></div>').appendTo($hier);
            }
            if(rank > 0) $('#taxonHierarchyInputForm').show();
        </g:if>

        if($(".taxonRank:not(#page)").length > 0)
            $(".taxonRank:not(#page)").autofillNames();

        $('#validateSpeciesSubmit').click(function() {	
        	var params = {};
            $("#addSpeciesPage input").each(function(index, ele) {
                if($(ele).val().trim()) params[$(ele).attr('name')] = $(ele).val().trim();
            });
            params['rank'] = $('#rank').find(":selected").val(); 
            //Did u mean species 
            $.ajax({
                url:'/species/validate',
                data:params,
                method:'POST',
                dataType:'json',
                success:function(data) {
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

        $('#addSpeciesPageSubmit').click(function() {
            var allValidated = true;
        	$("#taxonHierachyInput .input-prepend").each(function(index, ele) {
        		if(!$(ele).children('div').hasClass('disabled'));
        			allValidated = false;
    		});
    		
    		if(!allValidated){
    			alert("Some names are not validated in the Taxon Hierarchy. Please validated them before submit.")
    			return; 
    		}
    		
        	$('#addSpeciesPage').submit();
        });
    });
    </r:script>
</html>
