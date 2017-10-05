package species.auth;

import org.pac4j.core.context.WebContext;
import org.scribe.model.Token;

class Google2Client extends org.pac4j.oauth.client.Google2Client {


    public Google2Client() {
        super();
    }

    public Google2Client(final String key, final String secret) {
        super(key,secret);
    }

    @Override
    protected void clientInit(final WebContext context) {
        super.internalInit(context);
        configuration.setWithState(false);
    }

    protected String sendRequestForData(final Token accessToken, final String dataUrl) {
        init();
        return super.sendRequestForData(accessToken, dataUrl);
     }
}
