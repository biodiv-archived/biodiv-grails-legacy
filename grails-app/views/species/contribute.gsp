<%@page import="species.utils.Utils"%>
<html>
<head>

<meta name="layout" content="main" />
<r:require modules="species"/>
<title>Contribute</title>
<g:javascript src="species/util.js"/>

</head>
<body>
	<div class="span12 outer_wrapper">
		<s:showSubmenuTemplate model="['entityName':'Contribute to Species Pages']"/>
				
<%
String supportEmail = "";
String domain = Utils.getDomain(request);
if(domain.equals(grailsApplication.config.wgp.domain)) {
	supportEmail = grailsApplication.config.speciesPortal.wgp.supportEmail;
} else if(domain.equals(grailsApplication.config.ibp.domain)) {
	supportEmail =  grailsApplication.config.speciesPortal.ibp.supportEmail;
}
%>
		<div class="">
			<div class="super-section">
				<div class="section">
				<p>We request you to contribute to the species pages and build
					rich and reliable information on the biodiversity of India.</p>
	
				<p>All information published on the Portal will be on public
					access and under the Creative Commons License of your choice.</p>
	
				<p>You can contribute to species pages in three ways:</p>
				<ol>
					<li><b>Multiple species pages in one spreadsheet</b> : Download
						the xlsx spreadsheet <a
						href="${createLinkTo(dir: '/../static/templates/spreadsheet/', file:'speciesTemplateSimple_v2.xlsx' , base:grailsApplication.config.speciesPortal.resources.serverURL)}">here</a>,
						fill in multiple species descriptions in the spreadsheet, have a
						directory of images, zip the directory and send it to us <span
						class="mailme">${supportEmail}</span>
					</li>
	
					<li><b>One species page in one spreadsheet</b> : If you have
						elaborate information on species and are compiling information from
						many sources with different attributions and different licenses,
						you will have to use the expanded spreadsheet <a
						href="${createLinkTo(dir: '/../static/templates/spreadsheet/', file:'speciesTemplateExpanded_v2.xlsx' , base:grailsApplication.config.speciesPortal.resources.serverURL)}">here</a>.
						The expanded spreadsheet will allow you to fill in all the species
						pages fields available on the portal with appropriate attribution
						and license. Mail the completed species page to us <span
						class="mailme">${supportEmail}</span>
					</li>
	
					<li><b>On-line creating of species pages</b> : We are still
						working on the facility of creating species pages on-line on the
						portal. Please let us know your interest and we will inform you
						once the feature is available. Please email <span class="mailme">${supportEmail}</span>
					</li>
				</ol>
				
				<h3>Downloads</h3>
				<ol>
					<li><a
						href="${createLinkTo(dir: '/../static/templates/spreadsheet/', file:'speciesTemplateSimple_v2.xlsx' , base:grailsApplication.config.speciesPortal.resources.serverURL)}">Simple
							Species Template</a>
					</li>
					<li><a
						href="${createLinkTo(dir: '/../static/templates/spreadsheet/', file:'speciesTemplateExpanded_v2.xlsx' , base:grailsApplication.config.speciesPortal.resources.serverURL)}">Expanded
							Species Template</a>
					</li>
				</ol>
				<br /> If you have any question please provide feedback and email us
				at <span class="mailme">${supportEmail}</span>
				</div>
	
			</div>
		</div>
	</div>
</body>
</html>
