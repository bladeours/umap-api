package umap.service

import com.google.inject.ImplementedBy
import com.google.openlocationcode.OpenLocationCode
import com.microsoft.playwright.*
import com.microsoft.playwright.options.LoadState
import play.api.{Configuration, Logging}
import umap.model.GoogleDetails
import umap.utils.CustomPlaywrightPageFactory

import java.net.URLDecoder
import java.nio.charset.StandardCharsets.UTF_8
import java.util.regex.Pattern
import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[GoogleServiceImpl])
trait GoogleService {
  def getDetailsFromGoogle(url: String, coordinates: Option[String])(implicit ec: ExecutionContext): Future[GoogleDetails]
}

@Singleton
class GoogleServiceImpl @Inject(val config: Configuration, val playwrightFactory: CustomPlaywrightPageFactory) extends GoogleService with Logging {
  override def getDetailsFromGoogle(url: String, coordinates: Option[String])(implicit ec: ExecutionContext): Future[GoogleDetails] =
    Future {
      playwrightFactory.withPageRetry { implicit page =>
        getDetails(url, coordinates)
      }
    }

  private def getDetails(url: String, coordinates: Option[String])(implicit ec: ExecutionContext, page: Page): GoogleDetails = {
    logger.debug("getting info from google page")
    page.navigate(url)
    logger.debug("rejecting cookies")
    page.locator("button[aria-label='Reject all']").all().get(0).click()
    page.waitForLoadState(LoadState.NETWORKIDLE)
    val (lat, long) = getCoordinates(page, url, coordinates)
    val shareUrl = getShareUrl(page)
    logger.debug("finished getting info from google page")
    GoogleDetails(lat, long, getPlaceName(url), shareUrl)
  }

  private def getCoordinatesFromUrl(url: String): (String, String) = {
    val pattern = "!3d([0-9.-]+)!4d([0-9.-]+)".r
    pattern.findAllMatchIn(url).map(m => (m.group(1), m.group(2))).toList.head
  }

  private def getCoordinates(page: Page, url: String, coordinates: Option[String]): (String, String) = {
    if (coordinates.exists(c => c.contains(","))) {
      return coordinates.map(c => {
        val split = c.replace(" ", "").split(",")
        (split(0), split(1))
      }).get
    }
    val (lat, long) = getCoordinatesFromUrl(url)
    logger.debug("getting coordinates from open location code")
    val pattern = "([23456789CFGHJMPQRVWX]{2,8}\\+[23456789CFGHJMPQRVWX]{2,8})"
    val code = page.getByText(Pattern.compile(pattern)).textContent()
    logger.debug(s"found code: $code")
    val decodedCode = new OpenLocationCode(code.split(" ")(0).replace(",", "")).recover(lat.toDouble, long.toDouble).decode()
    (decodedCode.getCenterLatitude.toString, decodedCode.getCenterLongitude.toString)
  }

  private def getShareUrl(page: Page): String = {
    logger.debug("get share url")
    page.getByLabel("Share", new Page.GetByLabelOptions().setExact(true)).click()
    page.locator("input[value*='maps.app.goo.gl']").inputValue()
  }

  private def getPlaceName(url: String): String = {
    val placePattern = "/place/([^/@]+)".r
    val placeNameOpt = placePattern.findFirstMatchIn(url).map { m =>
      URLDecoder.decode(m.group(1).replace("+", " "), UTF_8)
    }
    placeNameOpt.getOrElse("Unknown place")
  }
}