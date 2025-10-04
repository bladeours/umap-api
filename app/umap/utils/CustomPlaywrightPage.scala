package umap.utils

import com.microsoft.playwright.{Browser, BrowserType, Page, Playwright, TimeoutError}
import play.api.{Configuration, Logging}

import java.io.File
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

case class CustomPlaywrightPage(page: Page, playwright: Playwright, browser: Browser) {
  def close(): Unit = {
    this.page.close()
    this.browser.close()
    this.playwright.close()
  }
}

class CustomPlaywrightPageFactory @javax.inject.Inject() (config: Configuration) extends Logging {

  private val debugDir = new File("debug")
  if (!debugDir.exists()) debugDir.mkdirs()

  private def saveScreenshot(page: Page, prefix: String = "error"): Unit = {
    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"))
    val file = new File(debugDir, s"${prefix}_$timestamp.png")
    page.screenshot(new Page.ScreenshotOptions().setPath(Path.of(file.getAbsolutePath)))
    logger.warn(s"Saved screenshot to ${file.getAbsolutePath}")
  }

  def preparePage(): CustomPlaywrightPage = {
    val playwright = Playwright.create()

    val browser = playwright.chromium().launch(
      new BrowserType.LaunchOptions()
        .setHeadless(true)
        //      .setHeadless(false)
        .setSlowMo(50)
    )

    val context = browser.newContext(
      new Browser.NewContextOptions()
        .setLocale("en-GB")
    )

    val page = context.newPage()
    page.setViewportSize(1920, 1080)
    page.setDefaultTimeout(config.get[Int]("playwright.timeoutS") * 1000)
    CustomPlaywrightPage(page, playwright, browser)
  }

  def withPageRetry[T](block: Page => T): T = {
    val retryMax = config.get[Int]("umap.retryMax")
    if (retryMax < 1 || retryMax > 10)
      throw new IllegalArgumentException("umap.retryMax must be between <1 to 10>")

    var retryCounter = 1
    var lastError: Option[Throwable] = None

    while (retryCounter <= retryMax) {
      val customPlaywright = preparePage()
      try {
        return block(customPlaywright.page)
      } catch {
        case ex: TimeoutError =>
          lastError = Some(ex)
          saveScreenshot(customPlaywright.page, s"timeout_retry$retryCounter")
          if (retryCounter < retryMax) {
            logger.debug(s"TimeoutError, retrying... (max $retryMax, current $retryCounter)")
            retryCounter += 1
          } else {
            throw ex
          }
      } finally {
        customPlaywright.close()
      }
    }

    throw lastError.getOrElse(new RuntimeException("Unknown Playwright failure"))
  }
}