package ${package}.ui;

import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vaadin.addon.oauthpopup.OAuthListener;
import org.vaadin.addon.oauthpopup.buttons.GitHubApi;
import org.vaadin.addon.oauthpopup.buttons.GitHubButton;

import ru.xpoft.vaadin.VaadinView;
import ${package}.beans.AloaService;
import ${package}.ui.main.MainView;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;

@SuppressWarnings("serial")
@VaadinView(LoginView.NAME)
@Component
@Lazy(true)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LoginView extends CustomComponent implements View
{
    @AutoGenerated
    private GridLayout mainLayout;

    @AutoGenerated
    private Label label_1;

    @AutoGenerated
    private Button buttonLogin;

    @AutoGenerated
    private PasswordField passwordField;

    @AutoGenerated
    private TextField textFieldUserId;

    private GitHubButton oauthLoginButton;

    private final static Logger log = LoggerFactory.getLogger(LoginView.class);

    private final static String GITHUB_KEY = "94718a8a2129efa3a309";

    private final static String GITHUB_SECRET = "9dc64cf9526fb559698beecd9d0012fa855bedf2";

    public final static String NAME = "login";

    @Autowired
    AloaService aloaService;

    /*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

    /**
     * The constructor should first build the main layout, set the composition
     * root and then do any custom initialization.
     * 
     * The constructor will not be automatically regenerated by the visual
     * editor.
     */
    public LoginView()
    {
        buildMainLayout();
        setCompositionRoot(mainLayout);

        // TODO add user code here

        // make password field respond to pressing Enter key
        passwordField.addFocusListener(new FocusListener() {
            // What will be done when your Text Field is active is Enter Key is
            // pressed
            public void focus(final FocusEvent event)
            {
                // Whatever you want to do on Enter Key pressed
                buttonLogin.setClickShortcut(KeyCode.ENTER);
            }
        });
        passwordField.addBlurListener(new BlurListener() {
            // To control waht happens when your Text Field looses focus
            @Override
            public void blur(final BlurEvent event)
            {
                buttonLogin.removeClickShortcut();
            }
        });

        textFieldUserId.setIcon(FontAwesome.USER);
        passwordField.setIcon(FontAwesome.LOCK);
        buttonLogin.setIcon(FontAwesome.SIGN_IN);
        buttonLogin.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event)
            {
                buttonLogin.removeClickShortcut();

                if (!textFieldUserId.isValid() || !passwordField.isValid())
                {
                    return;
                }

                String user = textFieldUserId.getValue();
                String password = passwordField.getValue();

                // Validate username and password with database here.
                // For examples sake I use a dummy username and password.
                boolean isValid = user.equals("test@test.com") && password.equals("passw0rd");

                if (isValid)
                {
                    loggedIn(user, null, null, null);
                }
                else
                {
                    Notification.show("Wrong username or password", Notification.Type.ERROR_MESSAGE);
                    // Wrong password clear the password field and refocuses it
                    passwordField.setValue(null);
                    passwordField.focus();
                }
            }
        });

        oauthLoginButton = new GitHubButton(GITHUB_KEY, GITHUB_SECRET);
        oauthLoginButton.setCaption("Login with your Github account");
        oauthLoginButton.setScope("user");

        oauthLoginButton.addOAuthListener(new OAuthListener() {
            @Override
            public void authSuccessful(String accessToken, String accessTokenSecret)
            {
                String userid;
                String username;
                String avatarUri;
                log.info("accessToken=" + accessToken);
                log.info("accessTokenSecret=" + accessTokenSecret);

                OAuthService service = new ServiceBuilder().provider(GitHubApi.class).apiKey(GITHUB_KEY).apiSecret(GITHUB_SECRET).scope("user").build();
                OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.github.com/user");
                Token token = new Token(accessToken, accessTokenSecret);
                service.signRequest(token, request);
                Response response = request.send();
                if (response.getCode() != 200)
                {
                    throw new Error(String.valueOf(response.getCode()));
                }
                try
                {
                    JSONObject json = new JSONObject(response.getBody());
                    userid = json.getString("login");
                    username = json.getString("name");
                    avatarUri = json.getString("avatar_url")+"size=16px&token="+accessToken;
                    loggedIn(userid, username, avatarUri, token);
                }
                catch (JSONException e)
                {
                    throw new Error(response.getBody(), e);
                }

            }

            @Override
            public void authDenied(String reason)
            {
                Notification.show("Authorization denied");
            }
        });
        oauthLoginButton.setWidth("100.0%");
        oauthLoginButton.setId("myOAuthButton");
        mainLayout.addComponent(oauthLoginButton, 1, 5);
    }

    protected void loggedIn(String userid, String username, String avatarUri, Token token)
    {
        String user = username != null ? username : userid;

        getSession().setAttribute("user", userid);
        getSession().setAttribute("username", user);
        
        if (token != null) {
            getSession().setAttribute("token", token);
        }
        if (avatarUri != null) {
            getSession().setAttribute("avatarUri", avatarUri);
        }

        Notification.show("Welcome " + user);

        // notify listeners that user logged in
        aloaService.sendAloa(user + " logged in");
        
        getUI().getNavigator().navigateTo(MainView.NAME);
    }

    @Override
    public void enter(ViewChangeEvent event)
    {
        textFieldUserId.focus();
    }

    @AutoGenerated
    private GridLayout buildMainLayout()
    {
        // common part: create layout
        mainLayout = new GridLayout();
        mainLayout.setImmediate(false);
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        mainLayout.setColumns(3);
        mainLayout.setRows(6);
        
        // top-level component properties
        setWidth("100.0%");
        setHeight("-1px");
        
        // textFieldUserName
        textFieldUserId = new TextField();
        textFieldUserId.setCaption("User ID");
        textFieldUserId.setImmediate(false);
        textFieldUserId.setDescription("Enter your user ID here (email address)");
        textFieldUserId.setWidth("100.0%");
        textFieldUserId.setHeight("-1px");
        textFieldUserId.setTabIndex(1);
        textFieldUserId.setRequired(true);
        textFieldUserId.setInputPrompt("User ID");
        textFieldUserId.setNullRepresentation("User ID");
        mainLayout.addComponent(textFieldUserId, 1, 1);
        
        // passwordField
        passwordField = new PasswordField();
        passwordField.setCaption("Password");
        passwordField.setImmediate(false);
        passwordField.setDescription("Enter your password here");
        passwordField.setWidth("100.0%");
        passwordField.setHeight("-1px");
        passwordField.setTabIndex(2);
        passwordField.setRequired(true);
        passwordField.setInputPrompt("Password");
        passwordField.setNullRepresentation("Password");
        mainLayout.addComponent(passwordField, 1, 2);
        
        // buttonLogin
        buttonLogin = new Button();
        buttonLogin.setCaption("Login");
        buttonLogin.setImmediate(true);
        buttonLogin.setDescription("Press to login");
        buttonLogin.setWidth("100.0%");
        buttonLogin.setHeight("-1px");
        mainLayout.addComponent(buttonLogin, 1, 3);
        
        // label_1
        label_1 = new Label();
        label_1.setImmediate(false);
        label_1.setWidth("-1px");
        label_1.setHeight("-1px");
        label_1.setValue("or");
        mainLayout.addComponent(label_1, 1, 4);
        mainLayout.setComponentAlignment(label_1, new Alignment(48));
        
        return mainLayout;
    }

}
