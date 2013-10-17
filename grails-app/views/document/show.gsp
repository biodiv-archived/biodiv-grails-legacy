
<%@page import="species.utils.Utils"%>
<%@ page import="content.eml.Document"%>
<html>
<head>
<g:set var="canonicalUrl" value="${uGroup.createLink([controller:'document', action:'show', id:documentInstance.id, base:Utils.getIBPServerDomain()])}"/>
<g:set var="title" value="${documentInstance.title}"/>
<g:set var="description" value="${Utils.stripHTML(documentInstance.notes?:'') }" />
<g:render template="/common/titleTemplate" model="['title':title, 'description':description, 'canonicalUrl':canonicalUrl, 'imagePath':null]"/>
<r:require modules="content_view, activityfeed, comment" />
</head>
<body>
	<div class="span12"> 
		<div class="page-header clearfix">
			<div style="width: 100%;">
				<div class="main_heading" style="margin-left: 0px;">
					<sUser:ifOwns model="['user':documentInstance.author]">
                                            <div class="pull-right">
                                                <a class="btn btn-success pull-right"
                                                        href="${uGroup.createLink(
                                                                controller:'document', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
                                                        class="btn btn-info" title="Add Document">
                                                        <i class="icon-plus"></i> Add Document
                                                </a>

						<a class="btn btn-primary pull-right" title="Edit Document" style="margin-right: 5px;"
							href="${uGroup.createLink(controller:'document', action:'edit', id:documentInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
							<i class="icon-edit"></i> Edit
						</a>
						
						<a class="btn btn-danger pull-right"  href="#" title="Delete Document" style="margin-right: 5px;"

							onclick="deleteDocument(); return false;">
							<i class="icon-trash"></i> Delete
						</a>
							
						<form action="${uGroup.createLink(controller:'document', action:'delete')}" method='POST' name='deleteForm'>
							<input type="hidden" name="id" value="${documentInstance.id}" />
						</form>
						<r:script>
						function deleteDocument(){
							if(confirm('This document will be deleted. Are you sure ?')){
								document.forms.deleteForm.submit();
							}
						}
						</r:script>
	                                        </div>					
						<s:showHeadingAndSubHeading
						model="['heading':documentInstance.title, 'subHeading':documentInstance.attribution, 'headingClass':headingClass, 'subHeadingClass':subHeadingClass]" />

				</sUser:ifOwns>


                            </div>

			</div>
		</div>

                <div class="span12" style="margin-left:0px">
                    <g:render template="/common/observation/showObservationStoryActionsTemplate"
                    model="['instance':documentInstance, 'href':canonicalUrl, 'title':title, 'description':description, 'hideFlag':false, 'hideDownload':true, 'hideFollow':false]" />
                </div>



                <div class="span8 right-shadow-box observation" style="margin:0;">
                    <g:render template="/document/showDocument" model="['documentInstance':documentInstance, showDetails:true]"/>
                    
                                        
                    
			<g:if
				test="${documentInstance?.speciesGroups || documentInstance?.habitats || documentInstance?.placeName }">

				<div class="sidebar_section">
					<a class="speciesFieldHeader" href="#coverageInfo"
						data-toggle="collapse"><h5>Coverage Information</h5></a>
					<div id="coverageInfo" class="speciesField collapse in">
						<table>

							<g:if test="${documentInstance?.speciesGroups}">


								<tr>
									<td class="prop"><span class="grid_3 name">Species Groups</span></td>
									<td class="linktext"><g:each
											in="${documentInstance.speciesGroups}"
											var="speciesGroup">
											<button
												class="btn species_groups_sprites ${speciesGroup.iconClass()} active"
												id="${"group_" + speciesGroup.id}"
												value="${speciesGroup.id}" title="${speciesGroup.name}"></button>
										</g:each></td>
								</tr>
							</g:if>



							<g:if test="${documentInstance?.habitats}">
								<tr>
									<td class="prop"><span class="grid_3 name">Habitats</span></td>

									<td class="linktext"><g:each
											in="${documentInstance.habitats}" var="habitat">
											<button
												class="btn habitats_sprites ${habitat.iconClass()} active"
												id="${"habitat_" + habitat.id}" value="${habitat.id}"
												title="${habitat.name}"
												data-content="${message(code: 'habitat.definition.' + habitat.name)}"
												rel="tooltip" data-original-title="A Title"></button>
										</g:each></td>
								</tr>
							</g:if>

							<g:if test="${documentInstance?.placeName || documentInstance?.reverseGeocodedName}">
								<tr>

                                                                    <td class="prop"><span class="grid_3 name">
                                                                            Place</span></td>
                                                                    <td>
                                                                        
                                                                        <g:if test="${documentInstance?.placeName}">
                                                                        <g:set var="location" value="${documentInstance.placeName}"/>
                                                                        </g:if>
                                                                        <g:else>
                                                                        <g:set var="location" value="${documentInstance.reverseGeocodedName}"/>
                                                                        </g:else>

                                                                        <div class="value ellipsis multiline" title="${location}">
                                                                            ${location}
                                                                        </div>
										</td>
								</tr>
							</g:if>
						</table>

					</div>
				</div>

			</g:if>
			<uGroup:objectPostToGroupsWrapper 
                           	model="[canPullResource:canPullResource, 'objectType':documentInstance.class.canonicalName, 'observationInstance':documentInstance]" />
                        
                        <g:render template="/common/featureWrapperTemplate" model="['observationInstance':documentInstance]"/>

						<div class="union-comment">
				<feed:showAllActivityFeeds model="['rootHolder':documentInstance, feedType:'Specific', refreshType:'manual', 'feedPermission':'editable']" />
				<comment:showAllComments model="['commentHolder':documentInstance, commentType:'super','showCommentList':false]" />
			</div>
		</div>
                <div class="span4">
                <g:render template="/document/documentSidebar" model="['documentInstance':documentInstance]"/>
                                </div>

	</div>

</body>
</html>
