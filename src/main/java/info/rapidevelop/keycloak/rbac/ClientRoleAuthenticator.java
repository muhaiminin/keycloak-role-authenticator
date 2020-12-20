package info.rapidevelop.keycloak.rbac;

import org.keycloak.Config;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.idm.OAuth2ErrorRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Stream;

import static org.keycloak.models.AuthenticationExecutionModel.Requirement.DISABLED;
import static org.keycloak.models.AuthenticationExecutionModel.Requirement.REQUIRED;

public class ClientRoleAuthenticator implements Authenticator, AuthenticatorFactory, ConfigurableAuthenticatorFactory {
    private static final Logger log = LoggerFactory.getLogger(ClientRoleAuthenticator.class);
    private static final String DISPLAY_NAME = "Client Role Authentication";
    private static final String ID = "role-authentication";
    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENTS = {REQUIRED, DISABLED};

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        UserModel user = context.getUser();
        ClientModel client = context.getAuthenticationSession().getClient();
        log.info("Authenticate user '{}/{}' for client '{}'", user.getUsername(), user.getEmail(), client.getClientId());

        Stream<RoleModel> clientRoles = user.getClientRoleMappingsStream(client);
        if (clientRoles.count() > 0) {
            context.success();
            return;
        }

        Response response = Response.status(Response.Status.UNAUTHORIZED)
                .entity(new OAuth2ErrorRepresentation("invalid", "Invalid user credentials"))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
        context.failure(AuthenticationFlowError.INVALID_USER, response);
    }

    @Override
    public void action(AuthenticationFlowContext context) {

    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public String getDisplayType() {
        return DISPLAY_NAME;
    }

    @Override
    public String getReferenceCategory() {
        return DISPLAY_NAME;
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENTS;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Role base authentication, only user with selected client role will pass.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return ID;
    }
}
