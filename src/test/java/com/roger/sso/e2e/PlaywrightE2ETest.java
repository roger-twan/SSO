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
import com.roger.sso.service.UserService;

import jakarta.annotation.PostConstruct;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

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
  private UserService userService;

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
        .setSlowMo(500)
    );
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

    userService.deleteUserByEmail(testEmail);
  }
}
