<div class="observations thumbwrap">

	<obv:showObservationFilterMessage
		model="['instanceTotal':total, 'queryParams':queryParams, resultType:'page']" />
	<g:if test="${total > 0}">
		<ul class="list_view thumbnails">
			<g:each in="${instanceList}" status="i" var="newsletterInstance">
				<li class="thumbnail clearfix" >
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
		<div class="paginateButtons" style="clear: both;">
			<center>
				<p:paginateOnSearchResult total="${total}" action="search"
					params="[query:queryParams.q, fl:queryParams.fl]" />
			</center>
		</div>
	</g:if>
</div>
<r:script>
$(document).ready(function() {
	$(".list_view").show();
});
</r:script>