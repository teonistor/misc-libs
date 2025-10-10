package io.github.teonistor.spotifier

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import io.github.teonistor.spotifier.GeneralUtil.{ns, withWebClientExceptionLogging}
import io.github.teonistor.spotifier.data.FrontendData.{TransmissibleConnector, TransmissiblePlaylist, TransmissibleTrack}
import io.github.teonistor.spotifier.data.NicePlaylist.NiceTrack
import io.github.teonistor.spotifier.data.{FrontendData, NicePlaylist}
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.{RequestBodyUriSpec, RequestHeadersUriSpec}
import org.springframework.web.reactive.function.client.WebClientResponseException.{Forbidden, NotFound, Unauthorized}
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux

import java.lang.{Iterable => JIter}
import java.net.URI
import scala.jdk.CollectionConverters.{IterableHasAsScala, IteratorHasAsScala}

class SpotifyDataUtil(webHelper: WebHelper, objectMapper: ObjectMapper) {

  private val getItemsArr: java.util.function.Function[JsonNode, JIter[JsonNode]] =
    json => () => json.get("items").elements

  private val trackJsonToNiceTrack: java.util.function.Function[JsonNode, NiceTrack] =
    json => NiceTrack(
      json.get("id").textValue(),
      json.get("name").textValue(),
      json.get("album").get("name").textValue(),
      json.get("artists").elements().asScala.map(_.get("name").textValue()).to(Vector),
      Vector.empty)

  def newListPlaylists(): Flux[NicePlaylist] =
    webHelper.getRepeatedly("https://api.spotify.com/v1/me/playlists")
      .flatMapIterable(getItemsArr)
      .map(json => NicePlaylist.empty(json.get("name").textValue, json.get("owner").get("display_name").textValue))

  def newGetPlaylist(): Flux[NiceTrack] =
    webHelper.getRepeatedly("https://api.spotify.com/v1/playlists/3cEYpjA9oz9GiPac4AsH4n/tracks?fields=items%28added_by.id%2Ctrack.album.name%2Ctrack.artists%28name%29%2Ctrack.id%2Ctrack.name%29%2Cnext&additional_types=track%2Cepisode")
      .flatMapIterable(getItemsArr)
      .map(json => json.get("track"))
      .map(trackJsonToNiceTrack)

  def newGetTopTracks(): Flux[NiceTrack] =
    webHelper.getRepeatedly("https://api.spotify.com/v1/me/top/tracks?time_range=short_term&limit=50")
      .flatMapIterable(getItemsArr)
      .map(trackJsonToNiceTrack)
}

object SpotifyDataUtil {

  def main(args: Array[String]): Unit = {
//    new SpotifyDataUtil(new WebHelper(
//      WebClient.builder()
//        .defaultHeader("Authorization", "Bearer " + AuthHelper.obtainToken()).build()),
//      JsonMapper.builder().findAndAddModules().build())
//
//      .newGetPlaylist()
//      .collectList()
//      .block()
//      .forEach(println)

// Can't get On Repeat and Repeat Rewind as playlists, but could perhaps get top tracks
// https://developer.spotify.com/documentation/web-api/reference/get-users-top-artists-and-tracks

// Can't even get Tops of the years and Blends!!! Ridiculous!

//    println(WebClient.builder()
//      .defaultHeader("Authorization", "Bearer " + AuthHelper.obtainToken()).build()
//      .get()
//      .uri("https://api.spotify.com/v1/playlists/7I0GeD6DEOLuVC9ljL6w3h").asInstanceOf[RequestHeadersSpec[_]]
//      .retrieve()
//      .bodyToMono(classOf[JsonNode])
//      .block())
  }

  def pullPlaylistCoordinates(web:WebClient, objectMapper:ObjectMapper, uriBuilder:UriComponentsBuilder): JsonNode = withWebClientExceptionLogging {
    val variables = objectMapper.createObjectNode()
      .set[ObjectNode]("expandedFolders", objectMapper.createArrayNode())
      .set[ObjectNode]("features", objectMapper.createArrayNode().add("LIKED_SONGS").add("YOUR_EPISODES_V2"))
      .put("flatten", false)
      .putNull("folderUri")
      .put("includeFoldersWhenFlattening", true)
      .put("limit", 400)
      .put("offset", 0)
      .putNull("order")
      .put("textFilter", "")
    val extensions = objectMapper.createObjectNode()
      .set[JsonNode]("persistedQuery", objectMapper.createObjectNode()
        .put("version", 1)
        .put("sha256Hash", "2de10199b2441d6e4ae875f27d2db361020c399fb10b03951120223fbed10b08"))
    val body = objectMapper.createObjectNode()
      .put("operationName", "libraryV3")
      .set[ObjectNode]("variables", variables)
      .set[ObjectNode]("extensions", extensions)

    val uri = uriBuilder
      .path("pathfinder/v2/query")
      .build(true)
      .toUri

    callAPI(web, uri, body)
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
      .map(item => if (item == null || !item.hasNonNull("uri"))
          None
        else
          item.get("uri").textValue() match {
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

  def pullPlaylistAndMakeNice(web:WebClient, objectMapper:ObjectMapper, uriBuilder: => UriComponentsBuilder, playlistId: String): NicePlaylist ={
    val limit = 200
    Iterator.iterate(0)(_+limit)
      .map(pullPlaylistContent(web, objectMapper, uriBuilder, playlistId, limit, _))
      .map(playlistStructureToNice(objectMapper, _))
      .takeWhile(playlist => playlist != null && playlist.tracks.nonEmpty)
      .reduceOption[NicePlaylist] { case (l, r) =>
        NicePlaylist(l.name, l.owner, l.tracks ++ r.tracks)
      }
      // TODO What did I just do here
      .getOrElse(NicePlaylist("NAFF", "NAFF", Vector.empty))
  }

  private def pullPlaylistContent(web: WebClient, objectMapper: ObjectMapper, uriBuilder: => UriComponentsBuilder, playlistId: String, limit: Int, offset: Int): JsonNode = withWebClientExceptionLogging {
    val variables = objectMapper.getNodeFactory.objectNode()
      .put("uri", "spotify:playlist:" + playlistId)
      .put("limit", limit)
      .put("offset", offset)
    val extensions = objectMapper.getNodeFactory.objectNode()
      .set[JsonNode]("persistedQuery", objectMapper.getNodeFactory.objectNode()
        .put("version", 1)
        .put("sha256Hash", "837211ef46f604a73cd3d051f12ee63c81aca4ec6eb18e227b0629a7b36adad3"))
    val body = objectMapper.createObjectNode()
      .put("operationName", "fetchPlaylistContents")
      .set[ObjectNode]("variables", variables)
      .set[ObjectNode]("extensions", extensions)

    val uri = uriBuilder
      .path("pathfinder/v2/query")
      .build(true)
      .toUri

    try
      callAPI(web, uri, body)

    catch {
      case e: Forbidden => System.err.println(e.getResponseBodyAsString); e.printStackTrace(); throw e
      case e: Unauthorized => System.err.println(e.getResponseBodyAsString); e.printStackTrace(); throw e
      // To get around the API returning 404 if asked for a slice past the end of a thing which does exist. The following
      // method is null-lenient, and we eventually filter them out... which is a bit naff because an unfortunately positioned
      // null could cause a slice of a playlist to silently go missing
      case _: NotFound => null
    }
  }

  private def callAPI(web: WebClient, uri: URI, body: ObjectNode) =
    web.post()
      .uri(uri).asInstanceOf[RequestBodyUriSpec]
      .contentType(MediaType.APPLICATION_JSON)

      // One of these bastards is used to sus out hacks like this very one
      .header("app-platform", "WebPlayer")
      .header("Host", "api-partner.spotify.com")
      .header("Origin", "https://open.spotify.com")
      .header("Referer", "https://open.spotify.com/")
      .header("Sec-Fetch-Dest", "empty")
      .header("Sec-Fetch-Mode", "cors")
      .header("Sec-Fetch-Site", "same-site")
      .header("spotify-app-version", "1.2.75.269.gee97f28c")
      .header("TE", "trailers")
      .header("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:136.0) Gecko/20100101 Firefox/136.0")

      .bodyValue(body.toString).asInstanceOf[RequestHeadersUriSpec[_]]
      .retrieve()
      .bodyToMono(classOf[JsonNode])
      .block()

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
          ns(track.get("uri").textValue()  // Something like spotify:track:1dtfTodJ3Uld533EXKQokC
            .replace("spotify:track:", "")),
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

  def backfillIds(playlists: Vector[NicePlaylist]):Vector[NicePlaylist] = {

    val finder = playlists
      .flatMap(_.tracks)
      .map(track => (track.copy(id = null, affinity = null), track.id))
      //   Meh... maybe we should validate that it didn't go wrong
      //      .groupMapReduce(_._1)(_._2){
      //        case (l,r) => println(???)
      //      }
      .toMap

    val result = playlists.map(playlist => playlist.copy(
      tracks = playlist.tracks.map(track => finder
        .get(track.copy(affinity = null))
        .map(id => track.copy(id = id))
        .getOrElse(track))))

    val unresolved = result.flatMap(_.tracks)
      .filter(_.id == null)
    if (unresolved.nonEmpty)
      println("backfillIds: Warning! The following tracks were left without an ID: " + unresolved.mkString("\n  ", "\n  ", ""))

    result
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
