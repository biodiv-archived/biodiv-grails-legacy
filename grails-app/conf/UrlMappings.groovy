class UrlMappings {

	static mappings = {
		"/login/auth" {
			controller = 'openId'
			action = 'auth'
		}

		"/login/openIdCreateAccount" {
			controller = 'openId'
			action = 'createAccount'
		}

		"/login/facebookCreateAccount" {
			controller = 'openId'
			action = 'createFacebookAccount'
		}


		"/user/$action?/$id?" { controller = 'SUser' }


		//"/login/$action?"(controller: "login")
		"/logout/$action?"(controller: "logout")

		"/$controller/$action?/$id?"{ constraints { // apply constraints here
			} }


		"/"(controller:"observation", action:"list")
		"500"(view:'/error')
		"403"(view:'/error')
/*		"403"(controller: "errors", action: "error403")
		"500"(controller: "errors", action: "error500")
		"500"(controller: "errors", action: "error403", exception: AccessDeniedException)
		"500"(controller: "errors", action: "error403", exception: NotFoundException)
*/		
		"/static/$path"(controller:"species", action:"staticContent")
		
		"/userGroup/$id/pages"(controller:"userGroup", action:"pages")
		
		name userGroupPageShow: "/userGroup/$id/page/$newsletterId" {
			controller = 'userGroup'
			action = 'page'
		}

	}
}
