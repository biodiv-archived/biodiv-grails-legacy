
<g:if test="${entityName}">
	<div class="page-header clearfix">
		<div style="width: 100%;">
			<div class="main_heading" style="margin-left: 0px;">
                            <div class="pull-right">
            			<a class="btn btn-success"
				href="${uGroup.createLink(
						controller:'observation', action:'create', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}"
				> <i class="icon-plus"></i>Add an Observation</a>

                                </div>
				<s:showHeadingAndSubHeading
					model="['heading':entityName, 'subHeading':subHeading, 'headingClass':headingClass, 'subHeadingClass':subHeadingClass]" />
			</div>
		</div>
	</div>
</g:if>
<g:hasErrors bean="${observationInstance}">
	<i class="icon-warning-sign"></i>
	<span class="label label-important"> <g:message
			code="fix.errors.before.proceeding" default="Fix errors" /> </span>
	<%--<g:renderErrors bean="${observationInstance}" as="list" />--%>
</g:hasErrors>
