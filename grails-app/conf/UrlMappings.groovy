import org.gualdi.grails.plugins.ckeditor.CkeditorConfig
import org.codehaus.groovy.grails.commons.ApplicationHolder

class UrlMappings {

	static mappings = {

		
		//"500"(controller:'BiodivException', action:'error')
		"500"(view:'/error')
		"403"(view:'/error')
		"404"(view:'/notfound')
/*		"403"(controller: "errors", action: "error403")
		"500"(controller: "errors", action: "error500")
		"500"(controller: "errors", action: "error403", exception: AccessDeniedException)
		"500"(controller: "errors", action: "error403", exception: NotFoundException)
*/		

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
/*		"/api/user/$action?/$id?" { 
            controller = 'SUser' 
            format = 'json'
            constraints { id matches: /\d+/ }
        }
*/		

        group('/api') {
//          "/user"(resources:'SUser')
/*          "/api/user/create"(controller: 'user', action: 'create', method: 'GET')
            "/api/user/edit"(controller: 'user', action: 'edit', method: 'GET')
            "/api/user(.(*))?"(controller: 'user', action: 'delete', method: 'DELETE')
            "/api/user(.(*))?"(controller: 'user', action: 'update', method: 'PUT')
            "/api/user(.(*))?"(controller: 'user', action: 'save', method: 'POST')
*/
            "/user"( controller : 'SUser', action:'index', method:'GET')
            "/user/$id"(controller:"SUser") {
                action = [GET:"show", PUT:"update", DELETE:"delete", POST:"save"]
                constraints { id matches: /\d+/ }
            }
            "/user/$id?/$action?"( controller : 'SUser') 
            
            "/group"( controller : 'UserGroup', action:'index', method:'GET')
            "/group/$id"(controller:"UserGroup") {
                action = [GET:"show", PUT:"update", DELETE:"delete", POST:"save"]
                constraints { id matches: /\d+/ }
            }
            "/group/$id/$action"( controller : 'UserGroup') {
                constraints { id matches: /\d+/ }
            }
            "/group/$webaddress/$controller/$action/$id?"( ) {
            }

            "/related/$controller/$filterProperty?/$filterPropertyValue?" (action:'related', method:'GET')
            "/observation/occurrences" (controller:'Observation', action:'occurrences', method:'GET')
            "/observation/distinctReco" (controller:'Observation', action:'distinctReco', method:'GET')
            "/$controller/upload_resource" (action:'upload_resource', method:'GET')

            "/$controller"( action:'index', method:'GET')
            "/$controller/$id" {
                action = [GET:"show", PUT:"update", DELETE:"delete", POST:"save"]
                constraints { id matches: /\d+/ }
            }
            "/$controller/$id?/$action?" {}

/*            "/login" {
                      format = 'json'

            }

            "/validate" {
            format = 'json'
            }

            "/logout" {
            format = 'json'
            }
*/
            name oauth: "/oauth/${action}/${provider}"(controller: 'oauth')

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
