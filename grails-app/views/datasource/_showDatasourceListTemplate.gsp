<%@page import="species.dataset.Dataset"%>

<div class="observations_list observation" style="clear: both;">
	<div class="btn-group button-bar" data-toggle="buttons-radio"
		style="float: right;visibility:hidden;">
		<button class="list_view_bttn btn list_style_button active">
			<i class="icon-align-justify"></i>
		</button>
	</div>
	<div>
		<div>
			<%
				def pos = (queryParams?.offset != null) ? queryParams.offset : (params?.offset != null) ? params.offset : 0
			%>
			
			<table class="table table-hover span8" style="margin-left: 0px;">
				<thead>
					<tr>
						<th style="width:20%"><g:message code="datasource.name.label" /></th>
						<th style="width:20%"><g:message code="default.name.label" /></th>
						<th><g:message code="button.datasets" /></th>
					</tr>
				</thead>
				<tbody class="mainContentList">
					<g:each in="${instanceList}" status="i"
						var="instance">
						<tr class="mainContent">
                            <g:set var="mainImage" value="${instance.mainImage()}" />
                            <%def imagePath = mainImage?mainImage.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix): null%>
                            <td>
                                <div class="figure pull-left observation_story_image" style="height:150px;">
                                    <a
                                        href="${uGroup.createLink(controller:'datasource', action:'show', id:instance.id)}">
                                        <img
                                        class="normal_profile_pic"
                                        src="${instance.mainImage()?.fileName}" title="${instance.title}"
                                        alt="${instance.title}" /> </a>
                                </div>

                            </td>
                            <td>
                                ${instance.title}
                            </td>
                            <td>
                                ${Dataset.countByDatasource(instance)}
                            </td>



						</tr>
					</g:each>
				</tbody>
			</table>
		</div>
	</div>

	<g:if test="${instanceTotal > (queryParams?.max?:0)}">
		<div class="centered" style="clear: both;">
			<div class="btn loadMore">
				<span class="progress" style="display: none;"><g:message code="msg.loading" /> </span> <span
					class="buttonTitle"><g:message code="msg.load.more" /></span>
			</div>
		</div>
	</g:if>

	<%activeFilters?.loadMore = true %>
	<div class="paginateButtons" style="visibility: hidden; clear: both">
		<g:paginate total="${instanceTotal}" max="${queryParams?.max}"
			action="${params.action}" params="${activeFilters}" />
	</div>
	
</div>

<style>
thead th {
	background: rgba(106, 201, 162, 0.2);
}

.table tbody tr:hover td,.table tbody tr:hover th {
	background-color: #FEFAD5;
}

.table tbody tr td,.table tbody tr th {
	background-color: white;
}
</style>

