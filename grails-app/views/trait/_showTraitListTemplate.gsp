<style>
.collapse{height:opx;}
</style>
<% 
def instanceFieldList=[:]
instanceList.each{ iL ->
    if(!instanceFieldList[iL.field]) {
        instanceFieldList[iL.field] = []
    }
    instanceFieldList[iL.field] << iL
}
%>                   

 <div class="observations_list observation" style="clear: both;">
	<div class="mainContentList">
		<div class="mainContent" name="p${params?.offset}">
		    <div class="filters">
            <g:each in="${instanceFieldList}" status="j" var="inst">                
                <div class="sidebar_section">
                <g:if test="${params.action=='show' || fromList}">
                   <a class="speciesFieldHeader"  href="#trait${j}">
                    	<h5>${inst.key}</h5>
                    </a>

                    <ul id="trait${j}" class="grid_view thumbnails obvListwrapper">
				<g:each in="${inst.value}" status="i" var="instance">
					<li class="thumbnail" style="clear: both;margin-left:0px;width:100%;">
                    <g:render template="/trait/showTraitTemplate" model="['trait':instance, 'factInstance':factInstance, 'fromSpeciesShow':fromSpeciesShow, 'observationInstance':observationInstance]"/>
		<ul id="trait${j}" class="grid_view thumbnails obvListwrapper collapse">
				<g:each in="${inst.value}" status="i" var="instance">
					<li class="thumbnail" style="clear: both;margin-left:0px;width:100%;">
                    <g:render template="/trait/showTraitTemplate" model="['trait':instance, traitList:inst.value, 'factInstance':factInstance, 'fromSpeciesShow':fromSpeciesShow, 'fromObservationCreate':fromObservationCreate, 'fromObservationShow':fromObservationShow,  'observationInstance':observationInstance,displayAny:false]"/>
					</li>
				</g:each>
			</ul>
                </g:if>
                <g:else>
	              <a class="speciesFieldHeader collapse"  data-toggle="collapse" href="#trait${j}">
                    	<h5>${inst.key}</h5>
                   </a> 
                 <ul id="trait${j}" class="grid_view thumbnails obvListwrapper collapse">
				<g:each in="${inst.value}" status="i" var="instance">
					<li class="thumbnail" style="clear: both;margin-left:0px;width:100%;">
                    <g:render template="/trait/showTraitTemplate" model="['trait':instance, 'factInstance':factInstance, 'fromSpeciesShow':fromSpeciesShow, 'observationInstance':observationInstance]"/>
					</li>
				</g:each>
			</ul>               
                </g:else>
			</div>
			</g:each>
			</div>			
		</div>
	</div>
        
    <g:if test="${instanceTotal > (queryParams?.max?:0)}">
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
			 max="${queryParams?.max}" params="${activeFilters}" />
	</div>	
</div>
