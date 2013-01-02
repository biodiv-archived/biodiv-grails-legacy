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

		"/" {view="index"}
		//"/login/$action?"(controller: "login")
		"/logout/$action?"(controller: "logout")

		"/$controller/$action?/$id?"{ constraints { // apply constraints here
			} }


		
		"500"(view:'/error')
		"403"(view:'/error')
		"404"(view:'/pagenotfound')
/*		"403"(controller: "errors", action: "error403")
		"500"(controller: "errors", action: "error500")
		"500"(controller: "errors", action: "error403", exception: AccessDeniedException)
		"500"(controller: "errors", action: "error403", exception: NotFoundException)
*/		
		"/static/$path"(controller:"species", action:"staticContent")
		
		name userGroupModule:"/group/$webaddress/$controller/$action?/$id?" {
		
		}
		
		name pages:"/pages" {
			controller = 'userGroup'
			action = 'pages'
		}
		
		name page:"/page/$newsletterId" {
			controller = 'userGroup'
			action = 'pages'
		}
		
		name userGroupPageShow: "/group/$webaddress/page/$newsletterId" {
			controller = 'userGroup'
			action = 'pages'
		}
		
		//just for replacement sake in taglib..not to be used for mapping
		name onlyUserGroup:"/group/$webaddress" {
			controller='userGroup'
			action='index'
		}
		
		//to match /group/list
		name userGroupGeneric: "/group/$action" {
			controller = 'userGroup'
		}

		name userGroup: "/group/$webaddress/$action" {
			controller = 'userGroup'
		}
		
		"/group/$webaddress/observation/list" {
			controller='userGroup'
			action='observation'
		}
		"/group/$webaddress/user/list" {
			controller='userGroup'
			action='user'
		}
		
		"/group/$webaddress/user" {
			controller='userGroup'
			action='user'
		}
//		"/group/$webaddress/species/list" {
//			controller='userGroup'
//			action='species'
//		}

//		"/group/$webaddress/login/$action?" {
//			controller = "login"
//		}
		
		"/group/$webaddress/login/auth/$id?" {
			controller = "openId"
			action = "auth"
		}
		
		"/group/$webaddress/login/openIdCreateAccount" {
			controller = "openId"
			action = "createAccount"
		}
		
		"/group/$webaddress/login/facebookCreateAccount" {
			controller = "openId"
			action = "createFacebookAccount"
		}
		
		
		"/group/$webaddress/group/$action/$id?" {
			controller = "userGroup"
		}
		
		
		"/group/$webaddress/user/$action/$id?" {
			controller = "SUser"
		}
		
		"/confirm/$id?" {
			controller = 'emailConfirmation'
			action = "index"
		}
	}
}
