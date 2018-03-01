<div id="pageContent" class="observation  span8"  style="margin-left:0px;">
		<div class="page-header clearfix">
			<h1>
				${fieldValue(bean: newsletterInstance, field: "title")}
			</h1>
		</div>
		
		<div class="description bodymarker" >
			
			${raw(newsletterInstance?.newsitem)}
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
								<input class="btn btn-primary" style="float: right; margin-right: 5px;" type="submit" value="${g.message(code:'button.edit')}"/>
							</span> <span class="button"> <a class="btn btn-danger"
								style="float: right; margin-right: 5px;"
								href="${uGroup.createLink(controller:'newsletter', action:'delete', id:newsletterInstance.id)}"
								onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"><g:message code="button.delete" />
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
								<input class="btn btn-primary" style="float: right; margin-right: 5px;" type="submit" value="${g.message(code:'button.edit')}"/>
							</span> <span class="button"> <a class="btn btn-danger"
								style="float: right; margin-right: 5px;"
								href="${uGroup.createLink(controller:'newsletter', action:'delete', id:newsletterInstance.id)}"
								onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"><g:message code="button.delete" />
							</a> </span>
						</form>
					</div>
				</sUser:isAdmin>
			</g:else>
			<g:if test="${!newsletterInstance.fetchIsHomePage()}">
				<div class="union-comment">
					<feed:showAllActivityFeeds model="['rootHolder':newsletterInstance, feedType:'Specific', refreshType:'manual', 'feedPermission':'editable']" />
					<comment:showAllComments model="['commentHolder':newsletterInstance, commentType:'super','showCommentList':false]" />
				</div>
			</g:if>
		</div>
	</div>
