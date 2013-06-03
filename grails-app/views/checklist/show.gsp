<%@page import="species.utils.Utils"%>
<%@ page import="species.participation.DownloadLog.DownloadType"%>

<html>
<head>
<g:set var="canonicalUrl" value="${uGroup.createLink([controller:'checklist', action:'show', id:checklistInstance.id, base:Utils.getIBPServerDomain()])}"/>
<g:set var="title" value="${checklistInstance.title}"/>
<g:set var="description" value="${Utils.stripHTML(checklistInstance.description?:'')}" />
<g:render template="/common/titleTemplate" model="['title':title, 'description':description, 'canonicalUrl':canonicalUrl, 'imagePath':null]"/>
<r:require modules="checklist"/>
</head>
<body>
	
			<div class="span12">
                            <div class="page-header clearfix">
                                <div style="width:100%;">
                                    <div class="main_heading" style="margin-left:0px;">

                                        <s:showHeadingAndSubHeading
						model="['heading':checklistInstance.title, 'subHeading':checklistInstance.attribution, 'headingClass':headingClass, 'subHeadingClass':subHeadingClass]" />

                                    </div>
                                </div>

                        </div>	
                        <div class="span12" style="margin-left:0px; padding:4px; background-color:whitesmoke">
                                   <g:render template="/common/observation/showObservationStoryActionsTemplate"
                                   model="['instance':checklistInstance, 'href':canonicalUrl, 'title':title, 'description':description, 'hideFlag':true, 'hideDownload':false, 'hideFollow':true]" />
                        </div>


			<div style="clear:both;">	
				<table class="table table-hover" style="margin-left: 0px;">
					<thead>
						<tr>
							<th>Species Group</th>
							<th>No. of Species</th>
							<th>Place Name</th>
							<th>State(s)</th>
							<th>District(s)</th>
							<th>Taluk(s)</th>
						</tr>
					</thead>
					<tbody>
							<tr>
								<td><sUser:interestedSpeciesGroups model="['userInstance':checklistInstance]"/></td>
								<td>${checklistInstance.speciesCount}</td>
								<td>${checklistInstance.placeName}</td>
								<td>${checklistInstance.state.join(",")}</td>
								<td>${checklistInstance.district.join(",")}</td>
								<td>${checklistInstance.taluka.join(",")}</td>
							</tr>
					</tbody>
				</table>
			</div>
			
			<div>
				<div class="sidebar_section" >
					<a class="speciesFieldHeader" data-toggle="collapse" href="#license_information"><h5>License information</h5></a>
					<div id="license_information" class="speciesField collapse in">
							<table>
								<tr>
									<td class="prop"><span class="grid_3 name">Attribution</span></td> 
									<td class="linktext">${checklistInstance.attribution}</td>
								</tr>
								<tr>
									<td class="prop"><span class="grid_3 name">License</span></td> 
									<td><img src="${resource(dir:'images/license',file:checklistInstance?.license?.name?.getIconFilename()+'.png', absolute:true)}"
										title="${checklistInstance.license.name}"/></td>
								</tr>
							</table>
					</div>
				</div>	
				
				<g:if test="${checklistInstance.sourceText}" >
					<div class="sidebar_section">
						<a class="speciesFieldHeader" data-toggle="collapse" href="#source"><h5>Source</h5></a>
						<div id="source" class="speciesField collapse in">
							<dl class="dl linktext">
								<dd>${checklistInstance.sourceText}</dd>
							</dl>
						</div>
					</div>		
				</g:if>
				
				<div class="sidebar_section">
						<a class="speciesFieldHeader" data-toggle="collapse" href="#checklist_details"><h5>Checklist details</h5></a>
						<div id="checklist_details" class="speciesField collapse in">
							<dl class="dl linktext">
								<dd>${checklistInstance.description}</dd>
    						</dl>
    					</div>
    			</div>
				
				<div class="sidebar_section">
						<a class="speciesFieldHeader" data-toggle="collapse" href="#checklistdata"><h5>Checklist</h5></a>
					<div id="checklistdata" class="speciesField collapse in">
					<clist:showData
						model="['checklistInstance':checklistInstance]">
					</clist:showData>
				</div>
			</div>
			
			<g:if test="${checklistInstance.refText}" >
				<div class="sidebar_section">
						<a class="speciesFieldHeader" data-toggle="collapse" href="#references"><h5>References</h5></a>
					<div id="references" class="speciesField collapse in">
						<dl class="dl linktext">
							<dd>${checklistInstance.refText}</dd>
						</dl>
						</div>
					</div>		
			</g:if>
				
				
			<div class="union-comment">
				<feed:showAllActivityFeeds model="['rootHolder':checklistInstance, feedType:'Specific', refreshType:'manual', 'feedPermission':'editable']" />
				<comment:showAllComments model="['commentHolder':checklistInstance, commentType:'super','showCommentList':false]" />
			</div>
			
			</div>
	</div>	
	<r:script>
	$(document).ready(function(){
	});
	</r:script>
</body>
</html>
