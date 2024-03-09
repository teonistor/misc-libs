package io.github.teonistor.spotifier

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.json.JsonMapper
import io.github.teonistor.spotifier.GeneralUtil.{VectorParallelMap, cachingObj, cachingString}
import io.github.teonistor.spotifier.SpotifyDataUtil._
import io.github.teonistor.spotifier.data.NicePlaylist
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder

import java.nio.file.Files.readString
import java.nio.file.Path.{of => path}
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

object Coordinator {

  private val credLocation = path("spotify-creds")
  private val cacheLocation = path("spotify-cache")
  private val outputLocation = path("spotify-out")

//  private val executor = newFixedThreadPool(8, (r: Runnable) => {
//    val t = new Thread(r)
//    t.setDaemon(true)
//    t
//  })

  private val web = WebClient.builder()
    .codecs(_.defaultCodecs().maxInMemorySize(1024 * 1024 * 1024))
    .defaultHeader("authorization", readString(credLocation resolve "authorization"))
    .defaultHeader("client-token", readString(credLocation resolve "client-token"))
    .build()

  private val objectMapper = JsonMapper.builder()
    .findAndAddModules()
    .configure(FAIL_ON_UNKNOWN_PROPERTIES, false).build()

  private val uriBuilder = () => UriComponentsBuilder.newInstance()
    .scheme("https")
    .host("api-partner.spotify.com")
    .port(443)

  private def cachingJson(cacheFile: String)(func: => JsonNode) =
     cachingObj[JsonNode](_.toPrettyString, objectMapper.readTree)(cacheFile)(func)

 /* Top-level actions include:
    * Taking a snapshot of the account
    * Creating frontend data from (the latest/only version of) a list of playlists
    * Creating frontend data from the version history of one playlist
    */

  def execute(playlistName: Option[String] = None): Unit = {
    val today = LocalDate.now().format(ISO_LOCAL_DATE)

    val v= Iterator(pullPlaylistCoordinates(web, objectMapper, uriBuilder()))
      .flatMap(playlistCoordinatesToNice)
      .filter(playlistName
        .map(name => (nameAndId:(String,String)) => name == nameAndId._1)
        .getOrElse(_=> true))
      .toVector
      .parallelMap { case (name, id) =>
        // TODO Ensure file names don't conflict from different playlists by sheer misfortune
        val fileName = name.replaceAll("[^a-zA-Z0-9,_-]+", "_")

        val nice = objectMapper.readValue(
          cachingString(cacheLocation.resolve(today).resolve(fileName).toString) {
            objectMapper
              .valueToTree[JsonNode](pullPlaylistAndMakeNice(web, objectMapper, uriBuilder(), id))
              .toPrettyString
          },
          classOf[NicePlaylist])

        nice
      }
  }
}
