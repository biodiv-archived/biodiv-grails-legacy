<%@page import="species.utils.Utils"%>
<%@page import="species.utils.ImageType"%>
<%@ page import="species.ScientificName.TaxonomyRank"%>
<html>
    <head>
        <g:set var="title" value="${g.message(code:'facts.label')}"/>
        <g:render template="/common/titleTemplate" model="['title':title]"/>
        <style>
	        .super-section{background-color:white;}
	        td{width:50px;}
	        .userGroups{margin:0px;}
	        [class*="span"] {
	    	margin-left: 0px;
			}
			.table th, .table td {
					border-top:0px;
				    border-bottom: 1px solid #dddddd;
				}
        </style>
    </head>
   <body>
	    <div class="container">
	    	<h1 class="sci_name">${traitInstance.name}</h1>
	    	<div id="content" class="super-section">
			    	<table class="table">
			    		<g:if test="${traitInstance.description}">
					    	<tr><td><h6>Description</h6></td>
					    	<td>${traitInstance.description}</td></tr>
				    	</g:if>

				    	<tr>
				    		<td><h6>Values</h6></td>
					    	<td>
						    	<table class="table">
						    		<tr><th>Icon</th><th>Value</th><th>Description</th><th>Source</th></tr>
						    			<g:each in="${traitValue}" var="value">
						    				<tr><td><img
				                    				 class="user-icon small_profile_pic"
				                    				 src="${value?.mainImage()?.fileName}" title="${value.value}"
				                    					alt="${value.value}" /> </td><td>${value.value}</td><td>${value.description}</td><td>${value.source}</td></tr>
						    			</g:each>
						    	</table>
					    	</td>
				    	</tr>

				    	<g:if test="${traitInstance.traitTypes}">
					    	<tr><td><h6>Trait Type</h6></td>
					    	<td>${traitInstance?.traitTypes.toString().replaceAll('_',' ')}</td></tr>
				    	</g:if>

				    	<g:if test="${traitInstance.dataTypes}">
					    	<tr><td><h6>Data Type</h6></td>
					    	<td>${traitInstance.dataTypes}</td></tr>
				    	</g:if>

				    	<g:if test="${traitInstance.field}">
					    	<tr><td><h6>Species Field</h6></td>
					    	<td>${field}</td></tr>
				    	</g:if>

				    	<g:if test="${traitInstance.taxon}">
					    	<tr><td><h6>Coverage</h6></td>
					    	<td>${coverage}</td></tr>
				    	</g:if>
				    </table>
			    </div>
			    			    	<h5>Filter by value</h5>
			    	<div class="trait thumbnail" data-toggle="buttons-radio">
			    		<g:render template="/trait/showTraitValuesListTemplate" model="['traitValues':traitValue]" />
			    	</div>
			      	 <g:render template="/trait/matchingSpeciesTableTemplate" model="[matchingSpeciesList:matchingSpeciesList, totalCount:totalCount]"/>	
	    			</div>

	 	</div>
	<script>
	    $(document).ready (function() {
	         $(document).on('click', '.trait button, .trait .all, .trait .any, .trait .none', function(){
            if($(this).hasClass('active')){
            return false;
            }
            $(this).parent().parent().find('button, .all, .any, .none').removeClass('active btn-success');
            $(this).addClass('active btn-success');

            updateMatchingSpeciesTable();
            return false;
        });
            updateMatchingSpeciesTable();
	    });
	</script>
		<asset:script type="text/javascript">
		$(document).ready(function() {
			$(".trait button").button();
			$(".trait button").tooltip({placement:'bottom'});
		    <g:each in="${params.trait}" var="t">
		        $('.trait button[data-tvid="${t.value}"][data-tid="${t.key}"]').addClass('active btn-success');
		    </g:each>
		   
		});
		</asset:script>
	</body>
</html>