package io.github.teonistor.spotifier

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import io.github.teonistor.spotifier.GeneralUtil.{ns, withWebClientExceptionLogging}
import io.github.teonistor.spotifier.data.FrontendData.{TransmissibleConnector, TransmissiblePlaylist, TransmissibleTrack}
import io.github.teonistor.spotifier.data.{FrontendData, NicePlaylist}
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound
import org.springframework.web.util.UriComponentsBuilder

import java.net.URLEncoder.encode
import java.nio.charset.StandardCharsets.UTF_8
import scala.jdk.CollectionConverters.IterableHasAsScala

object SpotifyDataUtil {

  def pullPlaylistCoordinates(web:WebClient, objectMapper:ObjectMapper, uriBuilder:UriComponentsBuilder): JsonNode = withWebClientExceptionLogging {
    val variables = objectMapper.createObjectNode()
      .put("limit", 200)
      .put("offset", 0)
      .put("textFilter", "")
      .putNull("order")
      .putNull("folderUri")
      .put("flatten", false)
      .put("includeFoldersWhenFlattening", true)
      .put("withCuration", false)
      .set[ObjectNode]("expandedFolders", objectMapper.createArrayNode())
      .set[ObjectNode]("features", objectMapper.createArrayNode().add("LIKED_SONGS").add("YOUR_EPISODES"))
      .set[ObjectNode]("filters", objectMapper.createArrayNode().add("Playlists"))
    val extensions = objectMapper.createObjectNode()
      .set[JsonNode]("persistedQuery", objectMapper.createObjectNode()
        .put("version", 1)
        .put("sha256Hash", "0cc9ca58bd1dad0ce11712768bf4357ca8a9c6dab1dc0b43331fe526c47ff885"))
    val uri = uriBuilder
      .path("pathfinder/v1/query")
      .queryParam("operationName", "libraryV3")
      .queryParam("variables", encode(variables.toString, UTF_8))
      .queryParam("extensions", encode(extensions.toString, UTF_8))
      .build(true)
      .toUri

    web.get()
      .uri(uri).asInstanceOf[RequestHeadersSpec[_]]
      .retrieve()
      .bodyToMono(classOf[JsonNode])
      .block()
  }

  def playlistCoordinatesToNice(structure:JsonNode):Iterable[(String,String)] = {
    val playlists = structure
      .get("data")
      .get("me")
      .get("libraryV3")

    val result = playlists
      .get("items").asScala
      .map(_.get("item").get("data"))
      // Keep Nones for a moment until we validate sizes. They will be things like "Liked Songs" which appear in the playlist list but aren't real playlists
      .map(item => item.get("uri").textValue() match {
        case s"spotify:playlist:$id" => Some((item.get("name").textValue(), id))
        case _=> None
      })

    val actualSize = result.size
    val reportedSize = playlists.get("totalCount").intValue()
    if (actualSize != reportedSize)
      throw new IllegalStateException(s"Number of playlists [$actualSize] differs from reported library size [$reportedSize]. " +
        "If it is very large, some unspoken limit on the Spotify API may have been reached (or there's a bug)")

    result.flatten
  }

  def pullPlaylistAndMakeNice(web:WebClient, objectMapper:ObjectMapper, uriBuilder:UriComponentsBuilder, playlistId: String): NicePlaylist ={
    val limit = 200
    Iterator.iterate(0)(_+limit)
      .map(pullPlaylistContent(web, objectMapper, uriBuilder, playlistId, limit, _))
      .map(playlistStructureToNice(objectMapper, _))
      .takeWhile(playlist => playlist != null && playlist.tracks.nonEmpty)
      .reduce[NicePlaylist] { case (l, r) =>
        NicePlaylist(l.name, l.owner, l.tracks ++ r.tracks)
      }
  }

  private def pullPlaylistContent(web: WebClient, objectMapper: ObjectMapper, uriBuilder: UriComponentsBuilder, playlistId: String, limit: Int, offset: Int): JsonNode = withWebClientExceptionLogging {
    val variables = objectMapper.getNodeFactory.objectNode()
      .put("uri", "spotify:playlist:" + playlistId)
      .put("limit", limit)
      .put("offset", offset)
    val extensions = objectMapper.getNodeFactory.objectNode()
      .set[JsonNode]("persistedQuery", objectMapper.getNodeFactory.objectNode()
        .put("version", 1)
        .put("sha256Hash", "13119b22ace87552aa2c15d8171d9d060bc2933644a53d41094d677ece3d132c"))
    val uri = uriBuilder
      .path("pathfinder/v1/query")
      .queryParam("operationName", "fetchPlaylist")
      .queryParam("variables", encode(variables.toString, UTF_8))
      .queryParam("extensions", encode(extensions.toString, UTF_8))
      .build(true)
      .toUri

    try
      web.get()
        .uri(uri).asInstanceOf[RequestHeadersSpec[_]]
        .retrieve()
        .bodyToMono(classOf[JsonNode])
        .block()
    catch {
      // To get around the API returning 404 if asked for a slice past the end of a thing which does exist. The following
      // method is null-lenient, and we eventually filter them out... which is a bit naff because an unfortunately positioned
      // null could cause a slice of a playlist to silently go missing
      case _: NotFound => null
    }
  }

  private def playlistStructureToNice(objectMapper:ObjectMapper, playlistStructure:JsonNode) = ns {
    val playlist = playlistStructure
      .get("data")
      .get("playlistV2")

    playlist
      .get("content")
      .get("items").asScala
      .foldLeft(NicePlaylist.empty(
        ns(playlist.get("name").textValue()),
        ns(playlist.get("ownerV2").get("data").get("uri").textValue())
      )) { case (nicePlaylist, trackStructure) =>

        val affinity = ns(trackStructure
          .get("attributes").asScala
          .find(_.get("key").textValue() == "multiUserAttributionMetadata")
          .map(_.get("value").textValue())
          .map(objectMapper.readTree(_))
          .orNull
          .get("attributed_users").asScala
          .map(_.get("display_name").textValue())
          .toVector)
        val track = trackStructure
          .get("itemV2")
          .get("data")

        nicePlaylist.addTrack(
          ns(track.get("uri").textValue()),  // Something like spotify:track:1dtfTodJ3Uld533EXKQokC
          ns(track.get("name").textValue()),
          ns(track.get("albumOfTrack")
            .get("name").textValue()),
          Option(ns(track.get("artists")
            .get("items").asScala
            .to(Vector)))
            .getOrElse(Vector.empty)
            .flatMap(artist => Option(ns(artist
              .get("profile")
              .get("name").textValue()))),
          Option(affinity).getOrElse(Vector.empty))
      }
  }

  def comparisonise(title: String, playlists: Vector[NicePlaylist]): FrontendData = {

    val connectorsByIsSkipping = playlists.to(LazyList)
      .zipWithIndex
      .flatMap { case (playlist, col) =>
        playlist.tracks.iterator
          .zipWithIndex
          .map { case (track, row) =>
            // This predates the use of IDs. Conceivably the ID should suffice...
            (track.copy(affinity = Vector.empty), (col, row))
          }
      }
      .groupMap(_._1)(_._2).view
      .mapValues(_.sortBy(_._1).toList)
      .filter(_._2.size > 1)
      .flatMap { case (track, occurrences) =>
        occurrences.sliding(2).map {
          case (startCol, startRow) :: (endCol, endRow) :: Nil => TransmissibleConnector(
            startCol,
            endCol,
            startRow,
            endRow,
            track.id)
        }
      }.toVector
      .groupBy(conn => conn.endCol - conn.startCol > 1)

    val playlistsOut = playlists.map(nicePlaylist => TransmissiblePlaylist(
      nicePlaylist.name,
      nicePlaylist.tracks.map(track => TransmissibleTrack(
        track.id,
        track.name,
        track.album,
        ns(track.artists.mkString(", ")),
        ns(track.affinity.mkString(", "))))))

    FrontendData(
      title,
      playlistsOut,
      connectorsByIsSkipping.getOrElse(false, Vector.empty),
      connectorsByIsSkipping.getOrElse(true, Vector.empty))
  }
}
