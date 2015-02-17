package species.auth;

import org.scribe.model.Token;

class Google2Client extends org.pac4j.oauth.client.Google2Client {


    public Google2Client() {
        super();
    }

    public Google2Client(final String key, final String secret) {
        super(key,secret);
    }

    @Override
    protected void internalInit() {
        super.internalInit();
    }

    @Override
    protected boolean requiresStateParameter() {
        return false;
    }

    @Override
    protected String sendRequestForData(final Token accessToken, final String dataUrl) {
        init();
        return super.sendRequestForData(accessToken, dataUrl);
     }
}
