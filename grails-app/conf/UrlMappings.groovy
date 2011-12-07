class UrlMappings {

	static mappings = {
		"/login/$action?"(controller: "login")
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
