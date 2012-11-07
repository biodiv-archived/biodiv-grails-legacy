
<div class="observations_list observation" style="clear: both;">
	<div class="btn-group button-bar" data-toggle="buttons-radio"
		style="float: right;">
		<button class="list_view_bttn btn list_style_button active">
			<i class="icon-align-justify"></i>
		</button>
		<button class="grid_view_bttn btn grid_style_button">
			<i class="icon-th-large"></i>
		</button>
	</div>
	
	<div class="mainContentList">
		<div class="mainContent" name="p${params?.offset}">
		
			<%
				def observationPos = (queryParams?.offset != null) ? queryParams.offset : params?.offset
			%>
			<ul class="grid_view thumbnails">
			
				<g:each in="${observationInstanceList}" status="i"
					var="observationInstance">

					<g:if test="${i%3 == 0}">
						<li class="thumbnail" style="clear: both;">
					</g:if>
					<g:else>
						<li class="thumbnail" style="margin: 0;">
					</g:else>
					<obv:showSnippetTablet
						model="['observationInstance':observationInstance, 'obvTitle':obvTitleList?.get(i), 'pos': observationPos+i, 'userGroup':userGroup]"></obv:showSnippetTablet>
					</li>

				</g:each>
			</ul>
			
			<ul class="list_view thumbnails" style="display: none;">
				<g:each in="${observationInstanceList}" status="i"
					var="observationInstance">
					<li class="thumbnail" style="clear: both;"><obv:showSnippet
							model="['observationInstance':observationInstance, 'obvTitle':obvTitleList?.get(i), 'pos':observationPos+i, 'userGroup':userGroup]"></obv:showSnippet>
					</li>
				</g:each>
			</ul>
			
		</div>
	</div>
	
	<g:if test="${instanceTotal > (queryParams.max?:0)}">
		<div class="centered">
			<div class="btn loadMore">
				<span class="progress" style="display: none;">Loading ... </span> <span
					class="buttonTitle">Load more</span>
			</div>
		</div>
	</g:if>
	
	<%
		activeFilters?.loadMore = true
		activeFilters?.webaddress = userGroup?.webaddress
	%>
	<div class="paginateButtons" style="visibility: hidden; clear: both">
		<p:paginate total="${instanceTotal?:0}" action="${params.action}"
			userGroup="${userGroup}" userGroupWebaddress="${userGroupWebaddress}"
			 max="${queryParams.max}" params="${activeFilters}" />
	</div>
	

</div>

