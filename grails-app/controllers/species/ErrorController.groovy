package species;

class ErrorController {

    def index() {
println "++++++++++++++++++++++++++++++++++++++++++++++++++++++"
        log.info params
        def exception = request.exception.cause
        def message = ExceptionMapper.mapException(exception)
        def status = message.status

        response.status = status
        render(view: "/error", model: [status: status, exception: exception])
    }
}
