<!--<div id="right-sidebar" class="span3" style="position:absolute; right:-220px;top:0px;margin-top:20px">
    			<a class="btn btn-success"
				href="${uGroup.createLink(
						controller:'observation', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" style="width:125px;margin-bottom:10px;" 
				> <i class="icon-plus"></i><g:message code="link.add.observation" /></a>

    				<a class="btn btn-success"
					href="${uGroup.createLink(
						controller:'document', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
					class="btn btn-info"
					style="width:125px;margin-bottom:10px">
					<i class="icon-plus"></i><g:message code="link.add.document" />                                    </a>
	
                                </div>
-->
<%--<div id="right-sidebar" class="sidebar right-sidebar span12">--%>
<%--	<g:if test="${params.action=='search'}">--%>
<%--		<div class="sidebar_section" style="left: 0px">--%>
<%--			<a data-toggle="collapse" href="#advSearchBox"><h5>--%>
<%--					<i class=" icon-search"></i>Advanced Search--%>
<%--				</h5>--%>
<%--			</a>--%>
<%--			<div id="advSearchBox" class="collapse">--%>
<%--				<search:advSearch />--%>
<%--			</div>--%>
<%--		</div>--%>
<%--	</g:if>--%>
<%----%>
<%--	<div class="sidebar_section" style="left: 0px">--%>
<%--		<a data-toggle="collapse" href="#bookmarks"><h5><i class=" icon-bookmark"></i>Bookmarks</h5></a>--%>
<%--		--%>
<%--		<ul class="nav block-tagadelic" id="bookmarks">--%>
<%--			<g:each in="${pages}" var="newsletterInstance">--%>
<%----%>
<%--				<li><g:if test="${userGroupInstance}">--%>
<%--						<a--%>
<%--							href="${uGroup.createLink('mapping':'userGroup', 'action':'page', 'id':newsletterInstance.id, 'userGroup':userGroupInstance) }">--%>
<%--							${fieldValue(bean: newsletterInstance, field: "title")} </a>--%>
<%--					</g:if>--%>
<%--					<g:else>--%>
<%--						<a--%>
<%--							href="${uGroup.createLink(controller:'userGroup', action:'page', id:newsletterInstance.id) }">--%>
<%--							${fieldValue(bean: newsletterInstance, field: "title")} </a>--%>
<%--					</g:else></li>--%>
<%--			</g:each>--%>
<%--			<li><g:if test="${userGroupInstance}">--%>
<%--						<a--%>
<%--							href="${uGroup.createLink('mapping':'userGroup', action:"pages", 'userGroup':userGroupInstance)}">Pages </a>--%>
<%--				</g:if> <g:else>--%>
<%--					<g:link url="${uGroup.createLink(controller:'userGroup', action:'pages')}">Pages</g:link>--%>
<%--				</g:else>--%>
<%--			</li>--%>
<%--		</ul>--%>
<%--	</div>--%>
<%--	<div class="tags_section sidebar_section">--%>
<%--		<g:if test="${params.action == 'search' }">--%>
<%--			<obv:showAllTags--%>
<%--				model="['tags':tags , 'count':tags?tags.size():0, 'isAjaxLoad':true, 'context':userGroupInstance]" />--%>
<%--		</g:if>--%>
<%--		<g:else>--%>
<%--			<obv:showAllTags--%>
<%--				model="['tagFilterByProperty':'All' , 'params':params, 'isAjaxLoad':true, 'context':userGroupInstance]" />--%>
<%--		</g:else>--%>
<%--	</div>--%>
<%--</div>--%>
