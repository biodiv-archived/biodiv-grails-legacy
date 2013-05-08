
<g:if test="${entityName}">
	<div class="page-header clearfix">
		<div style="width: 100%;">
			<div class="span8 main_heading" style="margin-left: 0px;">

				<s:showHeadingAndSubHeading
					model="['heading':entityName, 'subHeading':subHeading, 'headingClass':headingClass, 'subHeadingClass':subHeadingClass]" />
			</div>
				<sUser:isCEPFAdmin>
			
			<a class="btn btn-success pull-right"
				href="${uGroup.createLink(
						controller:'project', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
				> <i class="icon-plus"></i>Add CEPF Project</a>
				</sUser:isCEPFAdmin>
		</div>
	</div>
</g:if>
<g:hasErrors bean="${projectInstance}">
	<i class="icon-warning-sign"></i>
	<span class="label label-important"> <g:message
			code="fix.errors.before.proceeding" default="Fix errors" /> </span>

	<g:renderErrors bean="${projectInstance}" as="list" />
</g:hasErrors>
