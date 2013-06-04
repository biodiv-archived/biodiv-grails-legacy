<%@page import="species.utils.Utils"%>
<html>
<head>

<g:set var="canonicalUrl" value="${uGroup.createLink([controller:'species', action:'contribute', base:Utils.getIBPServerDomain()])}" />
<g:set var="title" value="Contribute"/>
<g:render template="/common/titleTemplate" model="['title':title, 'description':'', 'canonicalUrl':canonicalUrl, 'imagePath':'']"/>

<r:require modules="species"/>
<g:javascript src="species/util.js"/>

</head>
<body>
<div>
    <s:showSubmenuTemplate model="['entityName':'Contribute']" />
    <g:render template="contributeTemplate"/>
</body>
</html>
