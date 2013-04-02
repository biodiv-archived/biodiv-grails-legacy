<%@page import="species.utils.ImageType"%>
<%@page import="species.utils.ImageUtils"%>
<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@ page import="species.Species"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@page import="species.utils.Utils"%>
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<meta name="layout" content="main" />


<r:require modules="species_list" />

<g:set var="entityName"
	value="${message(code: 'species.label', default: 'Species Upload')}" />
<title>Species List</title>
</head>
<body>
	<div class="span12">
		<s:showSubmenuTemplate model="['entityName':'Species Upload']" />
		<uGroup:rightSidebar/>
		<div style="margin-left:0px;clear:both;">
			<form id="upload_species_spreadsheet" action="upload" 
				title="Upload spreadsheet" 
                                method="post">
                <input name="data_file" type="file"/>
                <input name="mapping_file" type="file"/>
                <input type="submit" value="upload"/>
			</form>			
		</div>
		<div id="uploadConsole">
			
		</div>
	</div>
</body>
</html>
