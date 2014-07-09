
<g:if test="${heading}">
    <h1 class="${headingClass?:''}" title="${heading.replaceAll('<.*>','')}">
		${raw(heading)}
    </h1>
</g:if>
<g:if test="${subHeading}">
    <h4 class="${subHeadingClass?:''}" title="${subHeading.replaceAll('<.*>','')}">
		${subHeading }
    </h4>
</g:if>
