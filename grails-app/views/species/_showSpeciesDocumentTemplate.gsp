<%@ page import="content.eml.Document"%>
<%@page import="species.Resource.ResourceType"%>
<%@page import= "species.utils.ImageType"%>
<%@page import= "species.auth.SUser"%>
<%
   def userInstance = documentInstance.author;
   
   %>
<g:set var="mainImage" value="${documentInstance.mainImage()}" />
<%
   def imagePath = mainImage?mainImage.thumbnailUrl(null, null): null;
   def obvId = documentInstance.id
   int desSize = 0
   int titlesize = 0
   

   if(desc){
    desc = desc.replaceAll("<(.|\n)*?>", '');
    desc = desc.replaceAll("&nbsp;", '');
    desc = desc.replaceAll("&quot;", '"');

    desSize = desc.length();
   
   } 
   if(docTitle){
    titlesize = docTitle.length();
   } 
   %>
<div class="row-fluid">
<div class="span1" style="padding:2px 0px 2px 12px">
   <g:link url="${uGroup.createLink(controller:'document', action:'show', id:docId,  'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }" >
      <img   src="${imagePath}" style="height:42px; margin-top:4px"/>
   </g:link>
</div>
<div class="span10" style="margin: 0px; padding-top: 5px;">
   <i style="margin: 2px;">Title  : </i>
   <g:if test="${titlesize>101}" >
      <g:link url="${uGroup.createLink(controller:'document', action:'show', id:docId,  'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }" style="color:blue; ">
         ${docTitle[0..101]}...
      </g:link>
   </g:if>
   <g:else>
      <g:link url="${uGroup.createLink(controller:'document', action:'show', id:docId,  'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }" style="color:blue; " >
         ${docTitle}
      </g:link>
   </g:else>
   <br/>
   <div class="row-fluid">
      <div class="span1" style="width: 42px">
         <i>Desc:</i>
      </div>
      <div class="span11" style="margin: 0px">
         <g:if test="${desSize>98}">
            <%
            def descPrint = desc[0..98]
            %>
            ${descPrint}...
            ${descPrint.length()}         
         </g:if>
         <g:else>
            ${desc}
         </g:else>
      </div>
   </div>
</div>
<div>
   <div class="span1 pull-right">
      <div style="margin-left: 5px;">
         <g:link url="${uGroup.createLink(controller:'user', action:'show', id:userInstance.id,  'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress) }">
            <img src= "${userInstance.profilePicture(ImageType.SMALL)}" style="margin-top:3px"/>
         </g:link>
      </div>
      <div class="value" style="font-size: 10px; width: 100px; margin-left: -9px;">
         <g:formatDate format="dd/MM/yyyy" date="${documentInstance.createdOn}"
            type="date" style="MEDIUM" />
      </div>
   </div>
</div>