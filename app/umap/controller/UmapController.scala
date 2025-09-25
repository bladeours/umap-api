package umap.controller

import play.api.*
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc.*
import umap.model.{AddMarkerForm, AddMarkerFromGoogleForm}
import umap.service.{GoogleService, UmapService}

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UmapController @Inject()(val controllerComponents: ControllerComponents,
                               val umapService: UmapService,
                               val googleService: GoogleService)(implicit ec: ExecutionContext) extends BaseController with Logging {

  def addMarker(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    logger.debug("got request to add marker using form")
    request.body.validate[AddMarkerForm].fold(
      errors => Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors)))),
      form =>
        umapService.addMarker(form).map(_ => Ok).recover {
          case ex: Exception =>
            ex.printStackTrace()
            InternalServerError(Json.obj(
              "status" -> "error",
              "message" -> "Failed to add marker"
            ))
        }
    )
  }

  def addMarkerFromGoogle(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    logger.debug("got request to add marker using google url")
    request.body.validate[AddMarkerFromGoogleForm].fold(
      errors => Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors)))),
      form =>
        (for {
          googleDetails <- googleService.getDetailsFromGoogle(form.url)
          _ <- umapService.addMarker(AddMarkerForm.of(googleDetails, form))
        } yield Ok).recover {
          case ex: Exception =>
            ex.printStackTrace()
            InternalServerError(Json.obj(
              "status" -> "error",
              "message" -> "Failed to add marker from Google"
            ))
        }
    )
  }

  def getLayers(mapName: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] => {
    logger.debug(s"got request to return all layers for map $mapName")
    for {
      layers <- umapService.getLayers(mapName: String)
    } yield Ok(Json.toJson(layers))
  }.recover {
    case ex: Exception =>
      ex.printStackTrace()
      InternalServerError(Json.obj(
        "status" -> "error",
        "message" -> "Failed to fetch events"
      ))
  }
  }

}