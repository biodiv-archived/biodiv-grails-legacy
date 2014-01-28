
<style>
#ajaxLogin .modal {
	z-index : 10000;
}
</style>
<div id="ajaxLogin" class="modal hide fade in" style="display: none;z-index:3000;" tabindex='-1' aria-hidden="true">
	<div class="modal-body">
		<div class="openid-loginbox super-section">
			<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
			
			<div id="loginMessage" class="alert alert-error" style="display:none"></div>
			<auth:loginForm model="['openIdPostUrl': openIdPostUrl,
					'daoPostUrl':daoPostUrl,
					'persistentRememberMe': persistentRememberMe,
					'rememberMeParameter': rememberMeParameter,
					'openidIdentifier': openidIdentifier, 'ajax':true]"/>
		</div>
	</div>
</div>
