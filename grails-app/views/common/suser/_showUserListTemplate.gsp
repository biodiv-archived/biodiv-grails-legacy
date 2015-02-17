<div class="observations_list observation" style="clear: both;">
	<!--div class="btn-group button-bar" data-toggle="buttons-radio"
		style="float: right;">
		<button class="list_view_bttn btn list_style_button active">
			<i class="icon-align-justify"></i>
		</button>
		<button class="grid_view_bttn btn grid_style_button">
			<i class="icon-th-large"></i>
		</button>
	</div-->
	
	<div class="mainContentList" style="clear:both;">
		<div class="mainContent" name="p${params?.offset}">

			<ul class="grid_view thumbnails">
	
				<g:each in="${userInstanceList}" status="i" var="userInstance">
					<g:if test="${i% ((params.controller != 'userGroup')?6:6) == 0}">
						<li class="thumbnail" style="clear: both;margin-left:0px;">
					</g:if>
					<g:else>
						<li class="thumbnail">
					</g:else>
					<sUser:showUserSnippetTablet model="['userInstance':userInstance, 'userGroupInstance':userGroupInstance]"></sUser:showUserSnippetTablet>
					</li>
				</g:each>
			</ul>
		</div>
	</div>
		
		<g:if test="${instanceTotal > searchQuery?.max}">
		    <div class="centered">
		          <div class="btn loadMore">
		             <span class="progress" style="display: none;"><g:message code="msg.loading" /> </span> <span
          		        class="buttonTitle"><g:message code="msg.load.more" /></span>
        		  </div>
		    </div>
		</g:if>

	<%
		params.loadMore = true
		params.webaddress = userGroupInstance?.webaddress
	%>
	<div class="paginateButtons" style="visibility: hidden; clear: both">
		<p:paginate total="${instanceTotal}" max="${params.max}" action="${params.action}" controller="${params.controller?:'user'}"
			userGroup="${userGroupInstance}" userGroupWebaddress="${userGroupWebaddress}"
			params="${params}" />
	</div>
</div>

