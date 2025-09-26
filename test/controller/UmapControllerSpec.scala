package controller

import org.apache.pekko.stream.Materializer
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.*
import org.scalatestplus.play.guice.*
import play.api.libs.json.Json
import play.api.test.*
import play.api.test.Helpers.*
import umap.controller.UmapController
import umap.model.{AddMarkerForm, AddMarkerFromGoogleForm}
import umap.service.{GoogleService, UmapService}

import scala.concurrent.ExecutionContext.Implicits.global


class UmapControllerSpec extends PlaySpec with GuiceOneAppPerTest with MockitoSugar {
  implicit lazy val mat: Materializer = app.materializer
  "HomeController GET" should {

//    "tescior" in {
//      val umapService = app.injector.instanceOf[UmapService]
//      val googleService = app.injector.instanceOf[GoogleService]
//
//      val controller = new UmapController(stubControllerComponents(), umapService, googleService)
//
//      val form = new AddMarkerForm("54.51593000890236", "18.5436427292558", "some desc", "some name", "2", "https://maps.app.goo.gl/krvFf96qJYbujk5M7", "Test Playwright")
//
//      val request = FakeRequest(POST, "/marker").withJsonBody(Json.toJson(form))
//      val result = controller.addMarker().apply(request)
//
//      status(result) mustBe OK
//    }
//
//    "tescior2" in {
//      val umapService = app.injector.instanceOf[UmapService]
//      val googleService = app.injector.instanceOf[GoogleService]
//
//      val controller = new UmapController(stubControllerComponents(), umapService, googleService)
//
//      val url = "https://www.google.com/maps/place/Emigration+Museum+Gdynia/@54.5043197,18.5077655,20005m/data=!3m1!1e3!4m6!3m5!1s0x46fda71a2f9df88f:0x6c1398cfa70dc108!8m2!3d54.533039!4d18.5479922!16s%2Fg%2F1pz2tvbdn?entry=ttu&g_ep=EgoyMDI1MDkyMS4wIKXMDSoASAFQAw%3D%3D"
//      val form = new AddMarkerFromGoogleForm("some slay description", "3", url, "Test Playwright")
//      val request = FakeRequest(POST, "/marker/google").withJsonBody(Json.toJson(form))
//      val result = controller.addMarkerFromGoogle().apply(request)
//
//      status(result) mustBe OK
//    }
//
//    "tescior3" in {
//      val umapService = app.injector.instanceOf[UmapService]
//      val googleService = app.injector.instanceOf[GoogleService]
//
//      val controller = new UmapController(stubControllerComponents(), umapService, googleService)
//
//      val mapName = "Test Playwright"
//      val request = FakeRequest(GET, s"/layers/$mapName")
//      val result = controller.getLayers(mapName).apply(request)
//
//      status(result) mustBe OK
//    }

  }
}
