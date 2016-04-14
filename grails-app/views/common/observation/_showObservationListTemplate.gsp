
<div class="observations_list observation" style="clear: both;">
    <%
    def observationPos = (queryParams.offset != null) ? queryParams.offset : params?.offset
    def styleviewcheck = ((!queryParams?.view || queryParams?.view !="grid") && !activeFilters.isChecklistOnly)?true:false;		
    %>

	<div class="mainContentList">
		<div class="mainContent" name="p${params?.offset}">
		
			
			<ul class="grid_view thumbnails obvListwrapper">
			
				<g:each in="${observationInstanceList}" status="i"
					var="observationInstance">

					<g:if test="${i%4 == 0}">
						<li class="thumbnail ${styleviewcheck ? 'addmargin':''}" style="clear: both;margin-left:0px;${!inGroupMap || inGroupMap[observationInstance.id]?'':'background-color:transparent;'} ${styleviewcheck ? 'width:100%;':''}">
					</g:if>
					<g:else>
						<li class="thumbnail ${styleviewcheck ? 'addmargin':''}" style="${!inGroupMap || inGroupMap[observationInstance.id]?'':'background-color:transparent;'} ${styleviewcheck ? 'width:100%;':''}">
					</g:else>
					<obv:showSnippetTablet model="['observationInstance':observationInstance, 'obvTitle':obvTitleList?.get(i), 'pos': ((observationPos != null)?observationPos+i:0), 'userGroup':userGroupInstance, canPullResource:canPullResource, 'styleviewcheck':styleviewcheck, 'recoVotes':recoVotes?.get(observationInstance.id)]"></obv:showSnippetTablet>
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
