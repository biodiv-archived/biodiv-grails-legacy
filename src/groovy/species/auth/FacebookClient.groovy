package species.auth;

import org.pac4j.oauth.client.FacebookClient;
import org.scribe.oauth.OAuth20ServiceImpl;
import org.scribe.model.OAuthConfig;
import org.scribe.model.OAuthConstants;
import org.scribe.model.ProxyOAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.SignatureType;
import org.scribe.model.Token;
import org.scribe.builder.api.FacebookApi;
import org.pac4j.core.util.CommonHelper;
import org.apache.commons.lang3.StringUtils;


import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Protocol;
import org.pac4j.core.client.RedirectAction;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.HttpCommunicationException;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.oauth.client.exception.OAuthCredentialsException;
import org.pac4j.oauth.credentials.OAuthCredentials;
import org.pac4j.oauth.profile.OAuth10Profile;
import org.pac4j.oauth.profile.OAuth20Profile;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.ProxyOAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
class FacebookClient extends org.pac4j.oauth.client.FacebookClient {

    protected FacebookApi api20;

    public FacebookClient() {
        super()
    }

    public FacebookClient(final String key, final String secret) {
        super(key,secret);
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
