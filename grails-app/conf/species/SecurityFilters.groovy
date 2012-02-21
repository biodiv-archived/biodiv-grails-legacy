package species

class SecurityFilters {

    def filters = {
        all(controller:'*', action:'*') {
            before = {
				println '--------------------------------------'
				print "params : "
				println params;
				print "Cookies : "
				request.cookies.each{println it.name+" : "+it.value}
				print "SessionId : "
				println request.getRequestedSessionId();
				def enames = request.getHeaderNames();
				   while (enames.hasMoreElements()) {
					  String name = (String) enames.nextElement();
					  String value = request.getHeader(name);
					  println name+":"+value;
				   }
				
            }
            after = {
//				println '=============================='
//				print "params : "
//				println params;
//				print "Cookies : "
//				request.cookies.each{println it.name+" : "+it.value}
//				print "SessionId : "
//				println request.getRequestedSessionId();
//				def enames = request.getHeaderNames();
//				   while (enames.hasMoreElements()) {
//					  String name = (String) enames.nextElement();
//					  String value = request.getHeader(name);
//					  println name+":"+value;
//				   }
				
            }
            afterView = {
                
            }
        }
    }
    
}
