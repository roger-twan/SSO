package com.roger.sso.e2e;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.Cookie;
import com.roger.sso.entity.User;
import com.roger.sso.repository.UserRepository;
import com.roger.sso.service.RedisService;
import com.roger.sso.util.PasswordUtil;

import jakarta.annotation.PostConstruct;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

@SpringBootTest
public class PlaywrightE2ETest {
  @Value("${spring.application.host}")
  private String host;

  @Value("${playwright.headless}")
  private boolean headless;

  private static boolean staticHeadless;
  private static Playwright playwright;
  private static Browser browser;
  private BrowserContext context;
  private Page page;

  @Autowired
  private PasswordUtil passwordUtil;

  @Autowired
  private RedisService redisService;

  @Autowired
  private UserRepository userRepository;

  @PostConstruct
  void init() {
    staticHeadless = headless;
  }

  @BeforeAll
  static void launchBrowser() {
    playwright = Playwright.create();
    browser = playwright.chromium().launch(
        new BrowserType.LaunchOptions()
            .setHeadless(staticHeadless)
            .setSlowMo(500));
  }

  @AfterAll
  static void closeBrowser() {
    browser.close();
    playwright.close();
  }

  @BeforeEach
  void createContextAndPage() {
    context = browser.newContext();
    page = context.newPage();
  }

  @AfterEach
  void closeContext() {
    context.close();
  }

  @Test
  public void testSignUpSuccess() {
    String testEmail = "test@example.com";

    page.navigate(host + "/signup");

    assertThat(page).hasTitle("Sign Up | SSO");

    Locator email = page.locator("#email");
    Locator password = page.locator("#password");
    Locator confirmPassword = page.locator("#confirmPassword");
    Locator submit = page.locator("button[type=submit]");

    email.fill(testEmail);
    password.fill("Password123");
    confirmPassword.fill("Password123");
    submit.click();

    page.waitForSelector("h2");
    assertThat(page).hasTitle("Email Sent | SSO");
    assertThat(page.locator("h2")).hasText("Thanks for signing up");

    userRepository.deleteByEmail(testEmail);
  }

  @Test
  public void testSignInToHomeSuccess() {
    String testEmail = "test@example.com";
    String testPassword = "Password123";

    User user = new User();
    user.setId("testId");
    user.setEmail(testEmail);
    user.setPassword(passwordUtil.encode(testPassword));
    user.setStatus(1);
    userRepository.save(user);

    page.navigate(host + "/signin");

    assertThat(page).hasTitle("Sign In | SSO");

    Locator email = page.locator("#email");
    Locator password = page.locator("#password");
    Locator submit = page.locator("button[type=submit]");

    email.fill(testEmail);
    password.fill(testPassword);
    submit.click();

    page.waitForURL("**/");
    assertThat(page).hasTitle("Home | SSO");

    List<Cookie> cookies = context.cookies();
    Cookie authTokenCookie = cookies.stream().filter(cookie -> cookie.name.equals("authToken")).findFirst().orElse(null);
    assertNotNull(authTokenCookie);

    context.clearCookies();
    redisService.deleteRedis("auth:" + authTokenCookie.value);
    userRepository.delete(user);
  }

  @Test
  public void testSignInToRedirectSuccess() {
    String testEmail = "test@example.com";
    String testPassword = "Password123";
    String testRedirectUrl = "https://google.com";

    User user = new User();
    user.setId("testId");
    user.setEmail(testEmail);
    user.setPassword(passwordUtil.encode(testPassword));
    user.setStatus(1);
    userRepository.save(user);

    page.navigate(host + "/signin?redirect=" + testRedirectUrl);

    assertThat(page).hasTitle("Sign In | SSO");

    Locator email = page.locator("#email");
    Locator password = page.locator("#password");
    Locator submit = page.locator("button[type=submit]");

    email.fill(testEmail);
    password.fill(testPassword);
    submit.click();

    page.waitForURL("**/authorization?redirect=" + testRedirectUrl);
    assertThat(page).hasTitle("Authorization | SSO");

    List<Cookie> cookies = context.cookies();
    Cookie authTokenCookie = cookies.stream().filter(cookie -> cookie.name.equals("authToken")).findFirst().orElse(null);
    assertNotNull(authTokenCookie);

    // TODO: verify redirect URL

    context.clearCookies();
    redisService.deleteRedis("auth:" + authTokenCookie.value);
    userRepository.delete(user);
  }
}
