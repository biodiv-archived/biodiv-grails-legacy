<head>
<meta name="layout" content="main" />
<r:require modules="auth"/>
<title>Login</title>
</head>

<body>
	<div class="container outer-wrapper">
		<div class="row">

			<div class="openid-loginbox super-section">

				<g:if test="${flash.error}">
					<div class="alert alert-error">
						${flash.error}
					</div>
				</g:if>
				
				<g:if test="${flash.message}">
					<div class="alert alert-error">
						${flash.message}
					</div>
				</g:if>
			</div>
		</div>
	</div>
	<script type="text/javascript">
<%--		alert("parent location" +window.opener.location );--%>
<%--		alert("closing window " + window.location);--%>
<%--		window.opener.mynewlocation = window.location.search;--%>
		
<%--		window.opener.mynewlocation = "abcd";--%>
<%--		--%>
<%--		alert("url params " + JSON.stringify(ee));--%>
<%--		alert("=== " +  JSON.stringify($.param(ee)));--%>
<%--		alert("=== " +  JSON.stringify(decodeURIComponent($.param(ee))));--%>
		
		window.opener.mynewparams = getUrlParams();
		
	    window.close();
	    function getUrlParams(){
	    	var prmstr = window.location.search.substr(1);
			var prmarr = prmstr.split ("&");
			var params = {};

			for ( var i = 0; i < prmarr.length; i++) {
    			var tmparr = prmarr[i].split("=");
    			params[tmparr[0]] = decodeURIComponent(tmparr[1]);
			}
			
			
			
			return params;
	    }
	    
	</script>	
</body>
