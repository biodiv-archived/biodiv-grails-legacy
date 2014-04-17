<%@ page import="utils.Newsletter"%>
<%@page import="species.utils.Utils"%>

<html>
<head>
<g:set var="canonicalUrl" value="${uGroup.createLink([controller:'newsletter', action:'show', id:newsletterInstance.id, base:Utils.getIBPServerDomain()])}"/>
<g:set var="title" value="${newsletterInstance.title}"/>
<g:set var="description" value="${Utils.stripHTML(newsletterInstance.newsitem?:'')}" />
<g:render template="/common/titleTemplate" model="['title':title, 'description':description, 'canonicalUrl':canonicalUrl]"/>

<style>
.newsletter_wrapper {
	padding: 30px;
	margin-left: auto;
	margin-right: auto;
	background-color: #e8f6f0;
	font-family: 'Helvetica Neue', Arial, 'Liberation Sans', FreeSans,
		sans-serif;
}

.newsletter_wrapper .body {
	background-color: #ffffff;
	padding: 10px;
}

.newsletter_wrapper .body h1 {
	padding: 10px;
	border-bottom: 2px solid #60c59e;
}

.newsletter_wrapper .body .date {
	font-size: 10px;
	font-style: italic;
}
</style>
<r:require modules="core, distinct_reco" />
</head>
<body>
	<div id="pageContent" class="observation  span8"  style="margin-left:0px;">
		<div class="page-header clearfix">
			<h1>
				${fieldValue(bean: newsletterInstance, field: "title")}
			</h1>
		</div>
		
		<div class="description bodymarker" >
			
			${newsletterInstance?.newsitem}
			<g:if test="${newsletterInstance?.userGroup}">
				<sec:permitted className='species.groups.UserGroup'
							id='${newsletterInstance.userGroup.id}'
							permission='${org.springframework.security.acls.domain.BasePermission.ADMINISTRATION}'>
					<div class="buttons" style="clear:both;">
						<form
							action="${uGroup.createLink(controller:'newsletter', action:'edit', userGroupWebaddress:params.webaddress)}"
							method="GET">
							<g:hiddenField name="id" value="${newsletterInstance?.id}" />
							<g:hiddenField name="userGroup"
								value="${newsletterInstance?.userGroup?.webaddress}" />
							<span class="button">
								<input class="btn btn-primary" style="float: right; margin-right: 5px;" type="submit" value="Edit"/>
							</span> <span class="button"> <a class="btn btn-danger"
								style="float: right; margin-right: 5px;"
								href="${uGroup.createLink(controller:'newsletter', action:'delete', id:newsletterInstance.id)}"
								onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">Delete
							</a> </span>
						</form>
					</div>
				</sec:permitted>
			</g:if>
			<g:else>
				<sUser:isAdmin>
					<div class="buttons" style="clear:both;">
						<form
							action="${uGroup.createLink(controller:'newsletter', action:'edit', userGroupWebaddress:params.webaddress)}"
							method="GET">
							<g:hiddenField name="id" value="${newsletterInstance?.id}" />
							<g:hiddenField name="userGroup"
								value="${newsletterInstance?.userGroup?.webaddress}" />
							<span class="button">
								<input class="btn btn-primary" style="float: right; margin-right: 5px;" type="submit" value="Edit"/>
							</span> <span class="button"> <a class="btn btn-danger"
								style="float: right; margin-right: 5px;"
								href="${uGroup.createLink(controller:'newsletter', action:'delete', id:newsletterInstance.id)}"
								onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">Delete
							</a> </span>
						</form>
					</div>
				</sUser:isAdmin>
			</g:else>
			<div class="union-comment">
				<feed:showAllActivityFeeds model="['rootHolder':newsletterInstance, feedType:'Specific', refreshType:'manual', 'feedPermission':'editable']" />
					<comment:showAllComments model="['commentHolder':newsletterInstance, commentType:'super','showCommentList':false]" />
			</div>
		</div>
		
	</div>
</body>
</html>
