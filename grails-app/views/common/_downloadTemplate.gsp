<div class="download-box btn-group"  style="z-index: 10; margin-left: 5px;${instanceTotal == 0 ? 'display:none;' :'' }">
    <a class="download-action ${(onlyIcon == 'true')?'':'btn'} ${(params.action=='show')?'btn-link':''} dropdown-toggle" data-toggle="dropdown"
        href="#" title="Download"> <i class=" icon-download-alt" style="${(downloadFrom=='uniqueSpecies')?'margin-top:-7px':''}"></i>
        <g:if test="${onlyIcon == 'false'}">
            <g:message code="button.download" />
        </g:if>
		</a>

		<div class="download-options popup-form" style="display: none; ${(downloadFrom=='uniqueSpecies')?'left:-399px;font-weight:normal':''} ">
			<form class="download-form form-horizontal">
				<div><div class="alert alert-info"><g:message code="msg.link.available" /></div></div>
				<g:each in="${downloadTypes}" var="downloadType" status="i">
                    <label>
					<g:if test="${i > 0}">
                    <input type="radio" style="margin-top: 0px;" name="downloadType" value="${downloadType}" >
						${g.message(code:'download.export')} ${downloadType.value()}</input>
					</g:if>
					<g:else>
                    <input type="radio" style="margin-top: 0px;" name="downloadType" value="${downloadType}" CHECKED >
						${g.message(code:'download.export')} ${downloadType.value()}</input>
                        </g:else>
                    </label>
				</g:each>
                <br />
				<input type="hidden" name="downloadFrom" value="${downloadFrom}"/>
				<input type="hidden" name="source" value="${source}"/>
				<input type="hidden" name="downloadObjectId" value="${downloadObjectId}"/>
				<input id="instanceTotal" type="hidden" name="instanceTotal" value="${instanceTotal}"/>
				<textarea class="comment-textbox noComment" placeholder="${g.message(code:'placeholder.how.intend')}" name="notes"></textarea>
<%--				<input style="width:385px" type="text" name="notes"></input><br />--%>
				<input class="btn pull-right" type="submit" value="${g.message(code:'button.ok')}" ></input>
				<div class = "download-close popup-form-close" value="${g.message(code:'button.close')}">
					<i class="icon-remove"></i>
				</div>
			</form>
			
			<div class="downloadMessage">
			</div>
		</div>
</div>

<r:script>
$('.download-close').click(function(){
    var me = this;
    var download_box = $(me).parents('.download-box');
    $(download_box).find('.download-options').hide();
});

$('.download-action').click(function(){
    var me = this;
    var download_box = $(me).parent('.download-box');
	$.ajax({ 
        	url:"${uGroup.createLink(controller:'SUser', action:'isLoggedIn')}",
		    success: function(data, statusText, xhr, form) {
			if(data === "true"){
                $(download_box).find('.download-options').show();
				return false;
			}else{
				window.location.href = "${uGroup.createLink(controller:'login')}?spring-security-redirect="+window.location.href;
			}
           },
           error:function (xhr, ajaxOptions, thrownError){
           	return false;
		} 
    	});
});
</r:script>
