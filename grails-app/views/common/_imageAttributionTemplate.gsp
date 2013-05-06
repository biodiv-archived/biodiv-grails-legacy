<%@page import="species.utils.Utils"%>
<g:if test="${resource}">
	<div class="notes" style="text-align: left;">
            <div>
                <obv:rating model="['resource':resource, 'class':'galleryRating']"/>

		<g:if test="${resource.contributors?.size() > 0}">
			<b>Contributors:</b>
			<ol>
				<g:each in="${resource.contributors}" var="a">
					<li>
						${a?.name}
					</li>
				</g:each>
			</ol>
		</g:if>
		<g:if test="${resource.attributors?.size() > 0}">
			<b>Attributions:</b>
			<ol>
				<g:each in="${resource.attributors}" var="a">
					<li>
						${a?.name}
					</li>
				</g:each>
			</ol>
		</g:if>

		<div class="license license_div">
			<g:if test="${resource.url}">
				<a href="${resource.url}" target="_blank"><b>View image
                                        source</b> </a>
			</g:if>
			<g:each in="${resource?.licenses}" var="l">
				<a href="${l?.url}" target="_blank"> <img class="icon" style="height:auto;"
					src="${createLinkTo(dir:'images/license', file: l?.name.value().toLowerCase().replaceAll('\\s+','')+'.png', absolute:true)}"
					alt="${l?.name.value()}" /> </a>
			</g:each>
		</div>
            </div>
        </div>
</g:if>
