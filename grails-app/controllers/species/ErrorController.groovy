package species;

class ErrorController {

    def index() {
        log.info params
        def exception = request.exception.cause
        def message = ExceptionMapper.mapException(exception)
        def status = message.status

        response.status = status
        render(view: "/error", model: [status: status, exception: exception])
    }
}
