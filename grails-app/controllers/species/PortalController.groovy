package species

class PortalController {

static allowedMethods = [test:'GET', index:'GET', list:'GET', save: "POST", update: ["POST","PUT"], delete: ["POST", "DELETE"]]
    static defaultAction = "test"

def citation(){}
def contact(){}
def datasharing(){}
def donors(){}
def feedback(){}
def licenses(){}
def partners(){}
def team(){}
def terms(){}
}