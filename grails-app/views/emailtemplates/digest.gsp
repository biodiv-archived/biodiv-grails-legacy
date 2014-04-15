<%@ page contentType="text/html"%>
<%@page import="species.Resource.ResourceType"%>
<%@page import="species.utils.ImageType"%>

<html><head><title>Digest</title>

        <meta content="text/html; charset=utf-8" http-equiv="Content-Type">
        <style type="text/css">
            <table>
                <tbody>
                    <tr>
                        <td id="header" class="w640" align="center" bgcolor="#ffffff" width="640"><a href="${grailsApplication.config.grails.serverURL}/"><img src="${resource(dir:'images', file:'whatsnewbanner_3.gif',absolute:'true' )}" alt="${grailsApplication.config.speciesPortal.app.siteName}" style="border: 0px solid ; width: 639px; height: 53px;"></a></td>
                    </tr>
                    <g:if test = "${digestContent.observations}">
                    <g:set var="obvIns" value="${digestContent.observations}"></g:set>
                    <tr align="center">
                        <g:each in="${obvIns}" var="observationInstance">
                        <g:set var="mainImage" value="${observationInstance.mainImage()}" />
                        <%
                        def imagePath = mainImage?mainImage.thumbnailUrl(null, !observationInstance.resource ? '.png' :null): null;
                        def controller = observationInstance.isChecklist ? 'checklist' :'observation'
                        def obvId = observationInstance.id
                        %>
                        <td class="w640" bgcolor="#ffffff" height="30" width="160"><a href="${uGroup.createLink(controller:controller, action:'show','id': obvId, absolute:true)}"><img src="${imagePath}" alt="" style="border: 0px solid ; width: 160px; height: 101px;"></a></td>
                        </g:each>
                    </tr>
                    <tr align="right">
                        <td>
                            <a href="">See More..</a>
                        </td>
                    </tr>
                    </g:if>

                    <g:if test = "${digestContent.species}">
                    <g:set var="spIns" value="${digestContent.species}"></g:set>
                    <tr align="center">
                        <g:each in="${spIns}" var="speciesInstance">
                        <g:set var="mainImage" value="${speciesInstance.mainImage()}" />
                        <%
                        def imagePath = '';
                        def speciesGroupIcon =  speciesInstance.fetchSpeciesGroup().icon(ImageType.ORIGINAL)
                        if(mainImage?.fileName == speciesGroupIcon.fileName) 
                            imagePath = mainImage.thumbnailUrl(grailsApplication.config.speciesPortal.resources.serverURL, '.png');
                        else
                            imagePath = mainImage?mainImage.thumbnailUrl(grailsApplication.config.speciesPortal.resources.serverURL):null;
                            def spId = speciesInstance.id
                        %>
                        <td class="w640" bgcolor="#ffffff" height="30" width="160"><a href="${uGroup.createLink(controller:'species', action:'show','id': spId, absolute:true)}"><img src="${imagePath}" alt="" style="border: 0px solid ; width: 160px; height: 101px;"></a></td>
                        </g:each>
                    </tr>
                    <tr align="right">
                        <td>
                            <a href="">See More..</a>
                        </td>
                    </tr>
                    </g:if>

                    <g:if test = "${digestContent.documents}">
                    <g:set var="docIns" value="${digestContent.documents}"></g:set>
                    <tr align="center">
                        <g:each in="${docIns}" var="documentInstance">
                        <g:set var="mainImage" value="${documentInstance.mainImage()}" />
                        <%
                            def imagePath = mainImage?mainImage.thumbnailUrl(null, null): null;
                            def docId = documentInstance.id
                        %>
                        <td class="w640" bgcolor="#ffffff" height="30" width="160"><a href="${uGroup.createLink(controller:'species', action:'show','id': docId, absolute:true)}"><img src="${imagePath}" alt="" style="border: 0px solid ; width: 160px; height: 101px;"></a></td>
                        </g:each>
                    </tr>
                    <tr align="right">
                        <td>
                            <a href="">See More..</a>
                        </td>
                    </tr>
                    </g:if>
                    
                    <g:if test = "${digestContent.users}">
                    <g:set var="userIns" value="${digestContent.users}"></g:set>
                    <tr align="center">
                        <g:each in="${userIns}" var="userInstance">
                        <td class="w640" bgcolor="#ffffff" height="30" width="160"><a href="${uGroup.createLink([action:"show", controller:"SUser", id:userInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress])}">
			<img src="${userInstance.profilePicture()}" title="${userInstance.name}" />
		</a></td>
                        </g:each>
                    </tr>
                    <tr align="right">
                        <td>
                            <a href="">See More..</a>
                        </td>
                    </tr>
                    </g:if>
                    
                </tbody>
            </table>
