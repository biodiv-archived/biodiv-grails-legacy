<div class="observations_list observation" style="clear: both;">
    <div class="btn-group button-bar pull-right" style="z-index: 10; margin-right:3px;">
        <div class="controls">
            <g:select name="limit" class="input-mini"
            placeholder="${g.message(code:'showobservationlisttemp.select.show')}"
            from="${[12,24,36,48,60]}"
            value="${queryParams?.max}" />

        </div>

    </div>


	<div class="mainContentList">
		<div class="mainContent" name="p${params?.offset}">
		
			<%
				def observationPos = (queryParams.offset != null) ? queryParams.offset : params?.offset
				def styleviewcheck = (params?.view=="list")? true:false;
			%>
			<ul class="grid_view thumbnails obvListwrapper">
				<g:each in="${instanceList}" status="i"
					var="datasetInstance">

					<g:if test="${i%4 == 0}">
						<li class="thumbnail ${styleviewcheck ? 'addmargin':''}" style="width:100%;clear: both;margin-left:0px;${!inGroupMap || inGroupMap[datasetInstance.id]?'':'background-color:transparent;'} ${styleviewcheck ? 'width:100%;':''}">
					</g:if>
					<g:else>
						<li class="thumbnail ${styleviewcheck ? 'addmargin':''}" style="width:100%;${!inGroupMap || inGroupMap[datasetInstance.id]?'':'background-color:transparent;'} ${styleviewcheck ? 'width:100%;':''}">
					</g:else>

                    <g:render template="/dataset/showDatasetStoryTemplate" model="['datasetInstance':datasetInstance, showDetails:true, showTitleDetail:true, 'userLanguage':userLanguage]"/>
					</li>
				</g:each>
			</ul>			
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
