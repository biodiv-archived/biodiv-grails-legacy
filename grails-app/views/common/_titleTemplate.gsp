<%@page import="species.utils.Utils"%>
<%@page import="species.utils.ImageType"%>
<g:set var="domain" value="${Utils.getDomain(request)}" />
<g:set var="fbAppId"/>
<%
if(domain.equals(grailsApplication.config.wgp.domain)) {
    fbAppId = grailsApplication.config.speciesPortal.wgp.facebook.appId;
} else { //if(domain.equals(grailsApplication.config.ibp.domain)) {
    fbAppId =  grailsApplication.config.speciesPortal.ibp.facebook.appId;
}

canonicalUrl = canonicalUrl ?: uGroup.createLink('controller':params.controller, 'action':params.action, userGroup:userGroupInstance,absolute:true)

if(params.webaddress && userGroupInstance && userGroupInstance.id) {
    imagePath = imagePath?:userGroupInstance.mainImage()?.fileName
    favIconPath = favIconPath?:userGroupInstance.icon(ImageType.SMALL)?.fileName;
    description = description?: userGroupInstance.description.replaceAll(/<.*?>/, '').trim()
    siteName = userGroupInstance.name +' - '+ grailsApplication.config.speciesPortal.app.siteName;
} else {
    imagePath = imagePath?:(Utils.getIBPServerDomain()+'/'+grailsApplication.config.speciesPortal.app.logo)
    favIconPath = favIconPath?:(Utils.getIBPServerDomain()+'/'+grailsApplication.config.speciesPortal.app.favicon)
    description = description?:grailsApplication.config.speciesPortal.app.siteDescription
    siteName = siteName ?: grailsApplication.config.speciesPortal.app.siteName;
}

if(description != null && description.length() > 300) {
    description = description[0..300] + ' ...'
}
%>

<meta name="layout" content="main" />
<title><g:if test="${title}">${title} | </g:if><g:if test="${params.action.equals('show')}"> ${params.controller.capitalize()} </g:if> <g:if test="${params.webaddress && userGroupInstance && !title.equals(userGroupInstance?.name)}"> | ${userGroupInstance.name} </g:if> | ${grailsApplication.config.speciesPortal.app.siteName}</title>

<g:if test="${favIconPath}">
<link rel="shortcut icon" href="${favIconPath}" type="image/x-icon" />
</g:if>
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
<meta property="og:title" content="${title?:params.controller.capitalize()}"/>
<g:if test="${videoPath}">
<meta property="og:video" content="${videoPath}" />
<meta property="og:video:width" content="640">
<meta property="og:video:height" content="480">
<meta property="og:video:type" content="application/x-shockwave-flash">
</g:if>
<meta property="og:site_name" content="${siteName}" />
<meta property="fb:app_id" content="${fbAppId}" />
<meta property="fb:admins" content="581308415,100000607869577" />
