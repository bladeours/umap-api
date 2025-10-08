package umap.model

import play.api.libs.json.{Format, Json}

case class AddMarkerFromGoogleForm(description: String, layer: String, url: String, mapName: String, coordinates: Option[String])

object AddMarkerFromGoogleForm {
  implicit val format: Format[AddMarkerFromGoogleForm] = Json.format[AddMarkerFromGoogleForm]
}