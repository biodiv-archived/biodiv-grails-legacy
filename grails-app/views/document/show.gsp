
<%@ page import="species.utils.Utils"%>
<%@ page import="content.eml.Document"%>
<html>
<head>
<g:set var="canonicalUrl" value="${uGroup.createLink([controller:'document', action:'show', id:documentInstance.id, base:Utils.getIBPServerDomain()])}"/>
<g:set var="title" value="${raw(documentInstance.title)}"/>
<g:set var="description" value="${Utils.stripHTML(documentInstance.notes?:'') }" />
<g:render template="/common/titleTemplate" model="['title':title, 'description':description, 'canonicalUrl':canonicalUrl, 'imagePath':null]"/>
<style>
.sidebar_section{
margin-bottom: 0;
}
</style>
</head>

<body>
    <div class="span12">
        <g:if test="${documentInstance}">
        <g:set var="featureCount" value="${documentInstance.featureCount}"/>
        </g:if>
        <div class="page-header clearfix" style="position:relative">
            <span class="badge ${(featureCount>0) ? 'featured':''}" style="left:-50px;"  title="${(featureCount>0) ? g.message(code:'text.featured'):''}">
            </span>

			<div style="width: 100%;">
				<div class="main_heading" style="margin-left: 0px;">
					<sUser:ifOwns model="['user':documentInstance.author]">
                                            <div class="pull-right">
                                                <a class="btn btn-success pull-right"
                                                        href="${uGroup.createLink(
                                                                controller:'document', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
                                                        class="btn btn-info" title="${g.message(code:'title.document.add')}">
                                                        <i class="icon-plus"></i><g:message code="link.add.document" />  
                                                </a>

						<a class="btn btn-primary pull-right" title="${g.message(code:'title.document.edit')}" style="margin-right: 5px;"
							href="${uGroup.createLink(controller:'document', action:'edit', id:documentInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}">
							<i class="icon-edit"></i><g:message code="button.edit" /> 
						</a>
						
						<a class="btn btn-danger pull-right"  href="#" title="${g.message(code:'title.document.delete')}" style="margin-right: 5px;"

							onclick="deleteDocument(); return false;">
							<i class="icon-trash"></i> <g:message code="button.delete" />
						</a>
							
						<form action="${uGroup.createLink(controller:'document', action:'delete')}" method='POST' name='deleteForm'>
							<input type="hidden" name="id" value="${documentInstance.id}" />
						</form>
                        <%
                        def y="${g.message(code:'info.document.delete')}"
                        %>
						<asset:script>
						function deleteDocument(){
                            var test="${y}";
                            
							if(confirm(test)){
								document.forms.deleteForm.submit();
							}
						}
						</asset:script>
	                                        </div>					
						
				</sUser:ifOwns>
<s:showHeadingAndSubHeading
						model="['heading':documentInstance.title, 'subHeading':documentInstance.attribution, 'headingClass':headingClass, 'subHeadingClass':subHeadingClass]" />


                            </div>

			</div>
		</div>

                <div class="span12" style="margin-left:0px">
                    <g:render template="/common/observation/showObservationStoryActionsTemplate"
                    model="['instance':documentInstance, 'href':canonicalUrl, 'title':title, 'description':description, 'hideFlag':false, 'hideDownload':true, 'hideFollow':false]" />
                </div>

                <div class="span8 right-shadow-box observation" style="margin:0;">
                    <g:render template="/document/showDocumentStoryTemplate" model="['documentInstance':documentInstance, showDetails:true,'userLanguage':userLanguage]"/>

			<g:if
				test="${documentInstance?.speciesGroups || documentInstance?.habitats || documentInstance?.placeName }">
			</g:if>

			<uGroup:objectPostToGroupsWrapper 
                           	model="['objectType':documentInstance.class.canonicalName, 'observationInstance':documentInstance]" />

			<div class="union-comment">
				<feed:showAllActivityFeeds model="['rootHolder':documentInstance, feedType:'Specific', refreshType:'manual', 'feedPermission':'editable']" />
				<comment:showAllComments model="['commentHolder':documentInstance, commentType:'super','showCommentList':false]" />
			</div>
		</div>
		
     	<g:render template="/document/showDocumentSidebar" model="['documentInstance':documentInstance, 'webaddress':params.webaddress]" />
	</div>
        </body>
</html>
