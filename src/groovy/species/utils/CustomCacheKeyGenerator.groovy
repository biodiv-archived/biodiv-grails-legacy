package species.utils

//import grails.plugin.cache.web.filter.DefaultWebKeyGenerator;
import javax.servlet.http.HttpServletRequest;
import org.codehaus.groovy.grails.web.util.WebUtils;
import org.springframework.util.StringUtils;
class CustomCacheKeyGenerator {//extends DefaultWebKeyGenerator {
/*
    @Override
    public String generate(HttpServletRequest request) {

        String uri = WebUtils.getForwardURI(request);

        StringBuilder key = new StringBuilder();
        key.append(request.getServerName().toLowerCase()).append(':');
        key.append(request.getMethod().toUpperCase());

        String format = WebUtils.getFormatFromURI(uri);
        if (StringUtils.hasLength(format) && !"all".equals(format)) {
            key.append(":format:").append(format);
        }

        if (supportAjax) {
            String requestedWith = request.getHeader(X_REQUESTED_WITH);
            if (StringUtils.hasLength(requestedWith)) {
                key.append(':').append(X_REQUESTED_WITH).append(':').append(requestedWith);
            }
        }

        key.append(':').append(uri);
        if (StringUtils.hasLength(request.getQueryString())) {
            String queryString = request.getQueryString();
            queryString = queryString.replaceFirst("&_=\\d+", '')
            key.append('?').append(queryString);
        }

        return key.toString();
    }    
*/}
