<%@page import="species.utils.Utils"%>
<html>
<head>
<link rel="canonical" href="${Utils.getIBPServerDomain() + createLink(controller:'activityFeed', action:'list')}" />
<g:set var="title" value="Activity"/>
<g:render template="/common/titleTemplate" model="['title':title, 'description':'', 'canonicalUrl':'', 'imagePath':'']"/>
<title>${title} | ${Utils.getDomainName(request)}</title>
<g:set var="entityName"
	value="${message(code: 'feeds.label', default: 'Activity')}" />
<r:require modules="activityfeed,comment"/>
</head>
<body>
	
			<div class="span12">
				<div class="page-header clearfix">
						<h1>
							<g:message code="default.observation.heading" args="[entityName]" />
						</h1>
				</div>

				<g:if test="${flash.message}">
					<div class="message alert alert-info">
						${flash.message}
					</div>
				</g:if>
				<uGroup:rightSidebar model="['userGroupInstance':userGroupInstance]"/>
				<div class="userGroup-section">
					<feed:showFeedWithFilter model="[feedType:feedType, feedCategory:'All','feedOrder':'latestFirst']" />
				</div>
			</div>
		
	<r:script>
	</r:script>
</body>
</html>
