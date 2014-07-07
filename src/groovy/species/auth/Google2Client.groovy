package species.auth;


import org.pac4j.oauth.client.Google2Client;
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
