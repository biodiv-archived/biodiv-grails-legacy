<%@page import="species.utils.Utils"%>
<g:set var="domain" value="${Utils.getDomain(request)}" />
<g:set var="fbAppId"/>
<%
if(domain.equals(grailsApplication.config.wgp.domain)) {
    fbAppId = grailsApplication.config.speciesPortal.wgp.facebook.appId;
} else { //if(domain.equals(grailsApplication.config.ibp.domain)) {
    fbAppId =  grailsApplication.config.speciesPortal.ibp.facebook.appId;
}

canonicalUrl = canonicalUrl ?: uGroup.createLink(action:params.action, controller:params.controller, userGroup:userGroupInstance,absolute:true)

if(params.webaddress) {
    imagePath = imagePath?:userGroupInstance.mainImage()?.fileName
    description = description?: userGroupInstance.description.replaceAll(/<.*?>/, '').trim()
    siteName = userGroupInstance.name +' - India Biodiversity Portal';
} else {
    imagePath = imagePath?:(Utils.getIBPServerDomain()+'/sites/all/themes/ibp/images/map-logo.gif')
    description = description?:"Welcome to the India Biodiversity Portal (IBP) - A repository of information designed to harness and disseminate collective intelligence on the biodiversity of the Indian subcontinent."
    siteName = siteName ?: "India Biodiversity Portal";
}

if(description != null && description.length() > 300) {
    description = description[0..300] + ' ...'
}
%>

<meta name="layout" content="main" />
<title>${title}<g:if test="${params.action.equals('show')}"><g:if test="${params.webaddress && !title.equals(userGroupInstance.name)}"> | ${userGroupInstance.name} </g:if> | ${params.controller.capitalize()} </g:if> | India Biodiversity Portal</title>
<g:if test="${canonicalUrl}">
<link rel="canonical" href="${canonicalUrl}" />
<meta property="og:url" content="${canonicalUrl}" />
</g:if>	
<g:if test="${imagePath}">
<link rel="image_src" href="${imagePath}" />
<meta property="og:image" content="${imagePath}" />
</g:if>
<g:if test="${description}">
<meta name="description" content="${description}">
<meta property="og:description" content="${description}"/>
</g:if>
<meta property="og:type" content="article" />
<meta property="og:title" content="${title}"/>
<g:if test="${videoPath}">
<meta property="og:video" content="${videoPath}" />
<meta property="og:video:width" content="640">
<meta property="og:video:height" content="480">
<meta property="og:video:type" content="application/x-shockwave-flash">
</g:if>
<meta property="og:site_name" content="${siteName}" />
<meta property="fb:app_id" content="${fbAppId}" />
<meta property="fb:admins" content="581308415,100000607869577" />
