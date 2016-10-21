<% 
def ref=[];
def instanceFieldList=[:]
instanceList.each{ iL ->
    if(ref.contains(iL.field)){           
        instanceFieldList[iL.field] << iL
    }else{
        ref << iL.field
        instanceFieldList[iL.field] = [iL]
    }
}
%>                   
 <div class="observations_list observation" style="clear: both;">
	<div class="mainContentList">
		<div class="mainContent" name="p${params?.offset}">
		    <div class="filters">
            <g:each in="${instanceFieldList}" status="j" var="inst">                
                <div class="sidebar_section">
                    <a class="speciesFieldHeader" data-toggle="collapse" href="#trait${j}">
                    	<h5>${inst.key}</h5>
                    </a>
			<ul id="trait${j}" class="grid_view thumbnails obvListwrapper">
				<g:each in="${inst.value}" status="i" var="instance">
					<li class="thumbnail" style="clear: both;margin-left:0px;width:100%;">
                    <g:render template="/trait/showTraitTemplate" model="['trait':instance]"/>
					</li>
				</g:each>
			</ul>
			</div>
			</g:each>
			</div>			
		</div>
	</div>
        
    <g:if test="${instanceTotal > (queryParams.max?:0)}">
		<div class="centered">
			<div class="btn loadMore">
				<span class="progress" style="display: none;"><g:message code="msg.loading" /></span> <span
					class="buttonTitle"><g:message code="msg.load.more" /></span>
			</div>
		</div>
	</g:if>
	
	<%
		activeFilters?.loadMore = true
		activeFilters?.webaddress = userGroupInstance?.webaddress
	%>
	<div class="paginateButtons" style="visibility: hidden; clear: both">
		<p:paginate total="${instanceTotal?:0}" action="${params.action}" controller="${params.controller?:'observation'}"
			userGroup="${userGroupInstance}" userGroupWebaddress="${userGroupWebaddress?:params.webaddress}"
			 max="${queryParams.max}" params="${activeFilters}" />
	</div>	
</div>
