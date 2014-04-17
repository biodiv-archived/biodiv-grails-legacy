<%@page contentType="text/html"%>
<%@page import="species.Resource.ResourceType"%>
<%@page import="species.utils.ImageType"%>

<html><head><title>Digest</title>

        <meta content="text/html; charset=utf-8" http-equiv="Content-Type">
        <style type="text/css">
            h3 {margin:0;padding-left:5px;background-color: white;}
            h2 {padding-left:5px;margin:0;margin-bottom:10px;background-color: rgb(236, 233, 183);}
        </style>
    </head>
    <body>
        <table style="width:621px; border:1px solid #a1a376;">
            <tr>
                <td id="header" class="w640" align="left" bgcolor="#ffffff"><a href="${grailsApplication.config.grails.serverURL}/"><img src="${resource(dir:'images', file:'whatsnewbanner_3.gif',absolute:'true' )}" alt="${grailsApplication.config.speciesPortal.app.siteName}" style="border: 0px solid ;width: 627px; height: 53px;"></a></td>
            </tr>
            <tr>
                <td class="w580" style="height: 10px; background-color: white;"></td>
            </tr>
            <big style="font-weight: bold;"> <small>Dear</small> <small>&nbsp;${username},</small></big>
            <p>Here is the activity digest for the ${userGroup} on the India Biodiversity Portal</p>
            <g:if test = "${digestContent.observations || digestContent.unidObvs}">
            <div class="resBlock" style="border:1px solid rgb(236, 233, 183);">
                <h2>Observations</h2>
                <div style="background-color: #d4ece3;">
                    <g:if test = "${digestContent.observations }">
                    <g:set var="obvIns" value="${digestContent.observations}"></g:set>
                    <h3>Recent</h3>
                    <table>
                        <tr align="left" style="background-color: #d4ece3">
                            <g:each in="${obvIns.size() < 5 ? obvIns : obvIns.subList(0, 5)}" var="observationInstance">
                            <g:set var="mainImage" value="${observationInstance.mainImage()}" />
                            <%
                            def imagePath = mainImage?mainImage.thumbnailUrl(null, !observationInstance.resource ? '.png' :null): null;
                            def controller = observationInstance.isChecklist ? 'checklist' :'observation'
                            def obvId = observationInstance.id
                            %>
                            <td class="w640" height="30" width="120"><a href="${uGroup.createLink(controller:controller, action:'show','id': obvId, absolute:true)}"><img src="${imagePath}" alt="" style="border: 0px solid ; width: 120px; height: 101px;"></a></td>
                            </g:each>
                        </tr>
                    </table>
                    <p style="text-align:right;padding-right:5px;font-weight:bold;color:#2ba6cb;background-color:white;margin:0;"><a href="www.google.com" style="color:#2ba6cb;">View More</a></p>
                    </g:if>
                    <g:if test = "${digestContent.unidObvs}">
                    <g:set var="uniObvIns" value="${digestContent.unidObvs}"></g:set>
                    <h3>Unidentified</h3>
                    <table>
                        <tr align="left" style="background-color: #d4ece3">
                            <g:each in="${uniObvIns.size() < 5 ? uniObvIns : uniObvIns.subList(0, 5)}" var="uniObvInstance">
                            <g:set var="mainImage" value="${uniObvInstance.mainImage()}" />
                            <%
                            def imagePath = mainImage?mainImage.thumbnailUrl(null, !uniObvInstance.resource ? '.png' :null): null;
                            def controller = uniObvInstance.isChecklist ? 'checklist' :'observation'
                            def obvId = uniObvInstance.id
                            %>
                            <td class="w640" height="30" width="120"><a href="${uGroup.createLink(controller:controller, action:'show','id': obvId, absolute:true)}"><img src="${imagePath}" alt="" style="border: 0px solid ; width: 120px; height: 101px;"></a></td>
                            </g:each>
                        </tr>
                    </table>
                
                <p style="text-align:right;padding-right:5px;font-weight:bold;color:#2ba6cb;background-color:white;margin:0;"><a href="www.google.com" style="color:#2ba6cb;">View More</a></p>
                </g:if>
                </div>
            </div>
            </g:if>

            <g:if test = "${digestContent.species}">
            <g:set var="spIns" value="${digestContent.species}"></g:set>
            <div class="resBlock" style="border:1px solid rgb(236, 233, 183);">
                <h2>Species</h2>
                <div style="background-color: #d4ece3;">
                    <h3>Latest Updated</h3>
                    <table>
                        <tr align="left">
                            <g:each in="${spIns.size() < 5 ? spIns : spIns.subList(0, 5)}" var="speciesInstance">
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
                            <td class="w640" height="30" width="120"><a href="${uGroup.createLink(controller:'species', action:'show','id': spId, absolute:true)}"><img src="${imagePath}" alt="" style="border: 0px solid ; width: 120px; height: 101px;"></a></td>
                            </g:each>
                        </tr>
                    </table>
                </div>
                <p style="text-align:right; padding-right:5px; font-weight:bold;background-color:white;margin:0;"><a href="www.google.com" style="color:#2ba6cb;">View More</a></p>
            </div>
            </g:if>
            
            <g:if test = "${digestContent.users}">
            <g:set var="userIns" value="${digestContent.users}"></g:set>
            <div class="resBlock" style="border:1px solid rgb(236, 233, 183);">
                <h2>Users</h2>
                <div style="background-color: #d4ece3;">
                    <h3>New</h3>
                    <table>
                        <tr align="left">
                            <g:each in="${userIns.size() < 5 ? userIns : userIns.subList(0, 5)}" var="userInstance">
                            <td class="w640" height="30" width="120"><a href="${uGroup.createLink([action:"show", controller:"SUser", id:userInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':userGroupWebaddress])}">
                                    <img src="${userInstance.profilePicture()}" title="${userInstance.name}" />
                            </a></td>
                            </g:each>
                        </tr>
                    </table>
                </div>
                <p style="text-align:right; padding-right:5px; font-weight:bold; color:#2ba6cb;background-color:white;margin:0;"><a href="www.google.com" style="color:#2ba6cb;">View More</a></p>
            </div>
            </g:if>

            <g:if test = "${digestContent.documents}">
            <g:set var="docIns" value="${digestContent.documents}"></g:set>
            <%
            def counter = 1
            %>
            <div class="resBlock" style="border:1px solid rgb(236, 233, 183);">
                <h2>Documents</h2>
                <div style="background-color: #d4ece3;">
                    <h3>Latest Updated</h3>
                    <table>
                        <tr align="left">
                            <g:each in="${docIns.size() < 5 ? docIns : docIns.subList(0, 5)}" var="documentInstance">
                            <%
                            def docId = documentInstance.id
                            %>
                            <tr>
                                <td class="w640" height="30" width="627" style="font-weight:bold;padding-left:5px;">${counter}. <a href="${uGroup.createLink(controller:'document', action:'show','id': docId, absolute:true)}" style="color:#2ba6cb;">${documentInstance.title}</a></td>
                            </tr>
                            <g:if test="${documentInstance.notes != null}">
                            <tr> 
                                <td class="w640" height="30" width="627">
                                    <g:if test="${documentInstance.notes != null && documentInstance.notes.length() > 140}">
                                    ${documentInstance.notes[0..138] + '...'} <br />
                                    </g:if>
                                    <g:else>
                                    ${documentInstance.notes?:''} <br />
                                    </g:else>
                                </td>
                            </tr>
                            </g:if>
                            <%
                                counter++
                            %>
                            </g:each>
                        </tr>
                    </table>
                </div>
                <p style="text-align:right; padding-right:5px; font-weight:bold;color:#2ba6cb;background-color:white;margin:0;"><a href="www.google.com" style="color:#2ba6cb;">View More</a></p>
            </div>
            </g:if>
            <p>If you don't want to recieve notifications from our portal, please unsubscribe by logging into <a href="${userProfileUrl}"> your profile</a></p>
        </table>
    </body>
</html>
