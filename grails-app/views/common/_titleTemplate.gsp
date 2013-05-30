<%@page import="species.utils.Utils"%>
<g:set var="domain" value="${Utils.getDomain(request)}" />
<g:set var="fbAppId"/>
<%

if(domain.equals(grailsApplication.config.wgp.domain)) {
    fbAppId = grailsApplication.config.speciesPortal.wgp.facebook.appId;
} else { //if(domain.equals(grailsApplication.config.ibp.domain)) {
    fbAppId =  grailsApplication.config.speciesPortal.ibp.facebook.appId;
}

imagePath = imagePath?:(Utils.getIBPServerDomain()+'/sites/all/themes/ibp/images/map-logo.gif')
description = description?:"Welcome to the India Biodiversity Portal (IBP) - A repository of information designed to harness and disseminate collective intelligence on the biodiversity of the Indian subcontinent."
%>


<meta name="layout" content="main" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<g:if test="${canonicalUrl}">
	<link rel="canonical" href="${canonicalUrl}" />
</g:if>	
<link rel="image_src" href="${imagePath}" />

<meta name="description" content="${description}">

<meta property="og:type" content="article" />
<meta property="og:title" content="${title}"/>
<meta property="og:url" content="${canonicalUrl}" />
<meta property="og:image" content="${imagePath}" />
<g:if test="${videoPath}">
<meta property="og:video" content="${videoPath}" />
</g:if>
<meta property="og:site_name" content="${siteName?:Utils.getDomainName(request)}" />
<meta property="og:description" content="${description}"/>
<meta property="fb:app_id" content="${fbAppId}" />
<meta property="fb:admins" content="581308415,100000607869577" />

