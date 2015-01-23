package species.auth;

import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.core.util.CommonHelper;
import org.scribe.model.OAuthConfig;
import org.scribe.builder.api.FacebookApi;
import org.scribe.oauth.OAuth20ServiceImpl;
import org.scribe.model.SignatureType;
import org.scribe.model.Token;
import org.apache.commons.lang3.StringUtils;

class FacebookClient extends org.pac4j.oauth.client.FacebookClient {

    protected FacebookApi api20;

    public FacebookClient() {
        super()
        connectTimeout = 2000;
    }

    public FacebookClient(final String key, final String secret) {
        super(key,secret);
        connectTimeout = 2000;
    }

    @Override
    protected void internalInit() {
        super.internalInit();
        CommonHelper.assertNotBlank("fields", this.fields);
        this.api20 = new FacebookApi();
        if (StringUtils.isNotBlank(this.scope)) {
            this.service = new OAuth20ServiceImpl(this.api20,  new OAuthConfig(this.key, this.secret,
            this.callbackUrl,
            SignatureType.Header, this.scope,
            null));

            /*this.service = new ExtendedOAuth20ServiceImpl(this.api20, new OAuthConfig(this.key, this.secret,
            this.callbackUrl,
            SignatureType.Header, this.scope,
            null), this.connectTimeout,
            this.readTimeout, this.proxyHost, this.proxyPort);*/
        } else {
            this.service = new OAuth20ServiceImpl(this.api20, new OAuthConfig(this.key, this.secret,
            this.callbackUrl,
            SignatureType.Header, null, null));
            /*,
            this.connectTimeout, this.readTimeout, this.proxyHost,
            this.proxyPort);*/
        }
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
