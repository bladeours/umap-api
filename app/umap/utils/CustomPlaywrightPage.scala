package umap.utils

import com.microsoft.playwright.{Browser, BrowserType, Page, Playwright}

case class CustomPlaywrightPage(page: Page, playwright: Playwright, browser: Browser) {
  def close(): Unit = {
    this.page.close()
    this.browser.close()
    this.playwright.close()
  }
}

object CustomPlaywrightPage {
  def preparePage(): CustomPlaywrightPage = {
    val playwright = Playwright.create()

    val browser = playwright.chromium().launch(
      new BrowserType.LaunchOptions()
        .setHeadless(true)
        //        .setHeadless(false)
        .setSlowMo(50)
    )

    val context = browser.newContext(
      new Browser.NewContextOptions()
        .setLocale("en-GB")
    )

    val page = context.newPage()
    page.setViewportSize(1920, 1080)

    CustomPlaywrightPage(page, playwright, browser)
  }

}
