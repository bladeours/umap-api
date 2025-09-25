package umap.model

import play.api.libs.json.{Format, Json}

case class AddMarkerForm(lat: String, long: String, description: String, name: String, layer: String, googleLink: String, mapName: String)

object AddMarkerForm {
  implicit val format: Format[AddMarkerForm] = Json.format[AddMarkerForm]

  def of(google: GoogleDetails, form:AddMarkerFromGoogleForm): AddMarkerForm =
    AddMarkerForm(google.lat, google.long, form.description, google.name, form.layer, google.googleLink, form.mapName)
}