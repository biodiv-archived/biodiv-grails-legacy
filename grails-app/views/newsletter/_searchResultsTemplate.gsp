<div class="observations_list observation" style="top: 0px;">

	<div class="mainContentList">
		<div class="mainContent">

			<ul class="list_view thumbnails">

				<g:each in="${instanceList}" status="i" var="newsletterInstance">
					<li class="thumbnail clearfix" style="width: 100%;">
						<h6 class="media-heading">

							<g:if test="${newsletterInstance.userGroup}">
								<g:link class="ellipsis"
									url="${uGroup.createLink(mapping:'userGroup', action:'page', id:newsletterInstance.id, userGroup:newsletterInstance.userGroup)}">
									${fieldValue(bean: newsletterInstance, field: "title")}
								</g:link>
							</g:if>

							<g:else>
								<g:link class="ellipsis"
									url="${uGroup.createLink(mapping:'userGroupGeneric', action:'page', id:newsletterInstance.id)}">
									${fieldValue(bean: newsletterInstance, field: "title")}
								</g:link>
							</g:else>
						</h6>
						<div class="ellipsis multiline">
							<g:set var="summary" value="${newsletterInstance.newsitem}"></g:set>
							<g:if test="${summary != null && summary.length() > 300}">
								${summary[0..300] + ' ...'}
							</g:if>
							<g:else>
								${summary?:''}
							</g:else>
						</div>

						<div class="pull-right">
							<g:if test="${newsletterInstance.userGroup}">
								<uGroup:showUserGroupSignature
									model="[ 'userGroup':newsletterInstance.userGroup]" />
							</g:if>
						</div>
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
		<center>
			<p:paginate total="${instanceTotal?:0}" action="${params.action}"
				controller="${params.controller?:'newsletter'}"
				userGroup="${userGroup}"
				userGroupWebaddress="${userGroupWebaddress}"
				max="${queryParams.max}" params="${activeFilters}" />
		</center>
	</div>
</div>
