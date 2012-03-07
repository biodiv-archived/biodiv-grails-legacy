class UrlMappings {

	static mappings = {
		"/login/$action?"(controller: "login")
		
//		"/login/auth"(
//			controller: "openId",
//			action:"auth"
//		)
//		"/login/openIdCreateAccount" (
//			controller : 'openId',
//			action : 'createAccount'
//		 )

		"/logout/$action?"(controller: "logout")
		
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(view:"/index")
		"500"(view:'/error')
		
		"/static/$path"(controller:"species", action:"staticContent")
		
	}
}
