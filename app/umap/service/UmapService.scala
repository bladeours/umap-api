package umap.service

import com.google.inject.ImplementedBy
import com.microsoft.playwright.*
import com.microsoft.playwright.options.SelectOption
import play.api.{Configuration, Logging}
import umap.model.AddMarkerForm
import umap.utils.CustomPlaywrightPage

import javax.inject.*
import scala.jdk.CollectionConverters.*
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[UmapServiceImpl])
trait UmapService {
  def addMarker(form: AddMarkerForm)(implicit ec: ExecutionContext): Future[Unit]
  def getLayers(mapName: String)(implicit ec: ExecutionContext): Future[Seq[String]]
}

@Singleton
  class UmapServiceImpl @Inject(val config: Configuration) extends UmapService with Logging {
  override def addMarker(form: AddMarkerForm)(implicit ec: ExecutionContext): Future[Unit] = {
    val customPlaywright = CustomPlaywrightPage.preparePage()
    try {
      implicit val page: Page = customPlaywright.page
      val loginUrl = config.get[String]("umap.loginUrl")
      logger.debug(s"going to login page $loginUrl")
      page.navigate(loginUrl)
      login()
      logger.debug(s"going to map ${form.mapName}")
      page.getByText(form.mapName).click()
      page.locator("button").getByText("Edit").click()
      createEmptyMarker()
      putCoordinates(form.lat, form.long)
      selectLayer(form.layer)
      putDescription(form.description)
      putGoogleLink(form.googleLink)
      putName(form.name)
      Thread.sleep(500)
      save()
      Thread.sleep(500)
      Future(None)
    } finally {
      customPlaywright.close()
    }
  }

  private def putName(name: String)(implicit page: Page): Unit = {
    logger.debug(s"putting name $name")
    page.locator("input[name='name']").fill(name)
  }

  private def putGoogleLink(googleLink: String)(implicit page: Page): Unit = {
    if (!page.locator("input[name='google']").isVisible()) {
      logger.debug("adding new field 'google'")
      page.getByText("Add a new field").click()
      page.locator("input[name='prompt']").fill("google")
      page.getByText("OK").click()
    }
    logger.debug(s"putting google link in google field $googleLink")
    page.locator("input[name='google']").fill(googleLink)
  }

  private def putDescription(description: String)(implicit page: Page): Unit = {
    logger.debug(s"putting description: $description")
    page.locator("textarea[name='description']").fill(description)
  }

  private def selectLayer(layer: String)(implicit page: Page): Unit = {
    logger.debug(s"selecting layer $layer")
    page.locator("select[name='datalayer']").selectOption(new SelectOption().setLabel(layer))
  }

  private def createEmptyMarker()(implicit page: Page): Unit = {
    logger.debug("creating empty marker")
    page.locator("button[title*='Draw a marker']").click()
    val size = page.viewportSize()
    val centerX = size.width / 2
    val centerY = size.height / 2

    page.mouse().click(centerX, centerY)
  }

  private def putCoordinates(lat: String, long: String)(implicit page: Page): Unit = {
    logger.debug(s"putting coordinates $lat $long")
    page.getByText("Coordinates").click()
    page.locator("input[name='lat']").fill(lat)
    page.locator("input[name='lng']").fill(long)
  }

  private def login()(implicit page: Page): Unit = {
    page.locator("input[name='username']").fill(config.get[String]("umap.username"))
    page.locator("input[name='password']").fill(config.get[String]("umap.password"))
    page.locator("//html/body/section/div[1]/form/input[4]").click()
    logger.debug("logged in")
  }

  private def save()(implicit page: Page): Unit = {
    page.locator("button").getByText("Save draft").click()
    logger.debug("saved correctly")
  }

  def getLayers(mapName: String)(implicit ec: ExecutionContext): Future[Seq[String]] = Future {
    val customPlaywright = CustomPlaywrightPage.preparePage()
    try {
      implicit val page: Page = customPlaywright.page
      page.navigate(config.get[String]("umap.loginUrl"))
      login()
      page.getByText(mapName).click()
      page.locator("button[title='Open browser']").click()
      page.locator(".datalayer-name").allInnerTexts().asScala.toList
    } finally {
      customPlaywright.close()
    }
  }
}