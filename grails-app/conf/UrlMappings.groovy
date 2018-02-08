import org.gualdi.grails.plugins.ckeditor.CkeditorConfig

class UrlMappings {

	static mappings = {


        "/jcaptcha/$action/$id"(controller:'Jcaptcha')
		//"500"(controller:'BiodivException', action:'error')
		'500'(view:'/error')
		'403'(view:'/error')
		'404'(view:'/notfound')

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


		"/user/create"( controller : 'SUser', action:'create', method:'GET')
		"/user"( controller : 'SUser', action:'index', method:'GET')
		"/user/list"( controller : 'SUser', action:'list', method:'GET')
		"/user/edit/$id?"( controller : 'SUser', action:'edit', method:'GET')
        "/user/$action/$id?"(controller:"SUser") {
            action = [GET:"show", POST:"update", POST:"delete", POST:"save"]
        }
        "/user/$action/$id?"(controller:"SUser")

        group('/api') {
//          "/user"(resources:'SUser')
            "/observation/$id"(controller : 'observation', action : 'flagDeleted', method:'DELETE')

            "/user"( controller : 'SUser', action:'index', method:'GET')
            "/user"( controller:'SUser', action:'save', method:'POST')
            "/user/$id"(controller:"SUser") {
                action = [GET:"show", PUT:"update", DELETE:"delete"]
                constraints { id matches: /\d+/ }
            }
            "/user/$id/$action"( controller : 'SUser') 
            "/user/$action" {
                controller = 'SUser'
                constraints {
                    action(matches:/(?!\d+$)\w+/)
                }
            }

            "/group"( controller : 'UserGroup', action:'index', method:'GET')
            "/group/$id"(controller:"UserGroup") {
                action = [GET:"show", PUT:"update", DELETE:"delete", POST:"save"]
                constraints { id matches: /\d+/ }
            }
            "/group/$id/$action"( controller : 'UserGroup') {
                constraints { id matches: /\d+/ }
            }
            "/group/$action" {
                controller = 'userGroup'
                constraints {
                    action(matches:/(?!\d+$)\w+/)
                }
            }
            "/group/$webaddress/$controller/$action/$id?"( ) {
            }

            "/related/$controller/$filterProperty?/$filterPropertyValue?" (action:'related', method:'GET')
            

            "/$controller"( action:'index', method:'GET')
            "/$controller"( action:'save', method:'POST')
            "/$controller/$id" {
                action = [GET:"show", PUT:"update", DELETE:"delete"]
                constraints { id matches: /\d+/ }
            }
            "/$controller/$id/$action" {
                constraints {
                    controller(matches:/\w+/)
                    id(matches:/\d+/)
                    action(matches:/\w+/)
                }
            }
            "/$controller/$action" {
                constraints {
                    controller(matches:/\w+/)
                    action(matches:/(?!\d+$)\w+/)
                }
            }


            name oauth: "/oauth/${action}/${provider}"(controller: 'restOauth')

            "/register/forgotPassword" {
                controller = 'register'
                action = 'forgotPasswordMobile'
                format = 'json'
            }


        }

		"/" {
            controller='home'
        }
		//"/login/$action?"(controller: "login")
		"/logout/$action?"(controller: "logout")

        //DONOT REMOVE
		"/$controller/$action?/$id?(.${format})?"{ 
            constraints { id matches: /\d+/ }
        }
		"/static/$path"(controller:"species", action:"staticContent")
		
	
		name pages:"/pages" {
			controller = 'userGroup'
			action = 'pages'
		}
		
		name page:"/page/$newsletterId" {
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

        name userGroupModule:"/group/$webaddress/$controller/$action?/$id?" {
	        method='*'	
		}
		
		name userGroupPageShow: "/group/$webaddress/page/$newsletterId" {
			controller = 'userGroup'
			action = 'pages'
		}
		
		name userGroup: "/group/$webaddress/$action" {
			controller = 'userGroup'
		}
	
        group("/group") {
            "/$webaddress/user/list" {
                controller='userGroup'
                action='user'
            }
            "/$webaddress/user" {
                controller='userGroup'
                action='user'
            }

            "/$webaddress/login/auth/$id?" {
                controller = "openId"
                action = "auth"
            }

            "/$webaddress/login/openIdCreateAccount" {
                controller = "openId"
                action = "createAccount"
            }

            "/$webaddress/login/facebookCreateAccount" {
                controller = "openId"
                action = "createFacebookAccount"
            }

            "/$webaddress/logout" {
                controller = "logout"
                action = "index"
            }

            "/$webaddress/oauth/${provider}/${action}"(controller: 'oauth')


            "/$webaddress/group/$action/$id?" {
                controller = "userGroup"
            }


            "/$webaddress/user/$action/$id?" {
                controller = "SUser"
            }
        }

		"/confirm/$id?" {
			controller = 'emailConfirmation'
			action = "index"
		}
   
    "/rating/rate/$id" {
        controller = "rating"
        action = "rate"
    }

    "/rating/fetchRate/$id" {
        controller = "rating"
        action = "fetchRate"
    }

    "/rating/$action/$id" {
        controller = "rateable"
    }

    "/document/list" {
        controller = 'document'
        action = 'browser'
    }

    "/group/$webaddress/document/list" {
        controller='document'
        action='browser'
    }

    def prefix = "/${CkeditorConfig.getConnectorsPrefix()}";
    def uploadPrefix = CkeditorConfig.getUploadPrefix();

    // Open File Manager
    //using ofm index page
    delegate.(prefix + "/biodivofm") (controller: "biodivOpenFileManagerConnector", action: "index")
    delegate.(prefix + "/biodivofm/filemanager") (controller: "biodivOpenFileManagerConnector", action: "fileManager")

    // Images outside the web-app dir
    if (uploadPrefix) { 
        delegate.(uploadPrefix + "/$filepath**") (controller: "biodivOpenFileManagerConnector", action: "show")
    }
    

    "/admin/manage/$action?"(controller: "adminManage")
    "/adminManage/$action?"(controller: "errors", action: "urlMapping")
    
} 
}
