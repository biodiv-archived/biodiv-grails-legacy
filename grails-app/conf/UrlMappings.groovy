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
					"/user/$action?" {
						controller = 'SUser'
					}

					
		//"/login/$action?"(controller: "login")
		"/logout/$action?"(controller: "logout")
		
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}
		

		"/"(controller:"observation", action:"list")
		"500"(view:'/error')
		
		"/static/$path"(controller:"species", action:"staticContent")
		
	}
}
