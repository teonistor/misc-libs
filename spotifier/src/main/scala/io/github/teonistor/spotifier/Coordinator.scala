package io.github.teonistor.spotifier

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.json.JsonMapper
import io.github.teonistor.spotifier.GeneralUtil.{VectorParallelMap, cachingJson, cachingString}
import io.github.teonistor.spotifier.SpotifyDataUtil._
import io.github.teonistor.spotifier.data.NicePlaylist
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder

import java.nio.file.Files.{readString, walk, writeString}
import java.nio.file.Path.{of => path}
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import scala.jdk.CollectionConverters.IteratorHasAsScala

object Coordinator {

  private val credLocation = path("spotify-creds3")
  private val cacheLocation = path("spotify-cache")
  private val outputLocation = path("spotify-out")

  private val archiveOutputLocation = outputLocation resolve "archive"
  private val frontendOutputLocation = outputLocation resolve "frontend"

  private val authorizationCredLocation = credLocation resolve "authorization"
  private val clientTokenCredLocation = credLocation resolve "client-token"

//  private val executor = newFixedThreadPool(8, (r: Runnable) => {
//    val t = new Thread(r)
//    t.setDaemon(true)
//    t
//  })
  private lazy val web = WebClient.builder()
    .codecs(_.defaultCodecs().maxInMemorySize(1024 * 1024 * 1024))
    .defaultHeader("authorization", readString(authorizationCredLocation))
    .defaultHeader("client-token", readString(clientTokenCredLocation))
    .build()

  private val objectMapper = JsonMapper.builder()
    .findAndAddModules()
    .configure(FAIL_ON_UNKNOWN_PROPERTIES, false).build()

  private val uriBuilder = () => UriComponentsBuilder.newInstance()
    .scheme("https")
    .host("api-partner.spotify.com")
    .port(443)

//  private def cachingJson(cacheFile: String)(func: => JsonNode) =
//     cachingObj[JsonNode](_.toPrettyString, objectMapper.readTree)(cacheFile)(func)

  def createDirectoiesAndFiles(): Unit = {
    List(credLocation, cacheLocation, archiveOutputLocation, frontendOutputLocation)
      .foreach(_.toFile.mkdirs())
    authorizationCredLocation.toFile.createNewFile()
    clientTokenCredLocation.toFile.createNewFile()
  }

 /* Top-level actions include:
    * Taking a snapshot of the account
    * Creating frontend data from (the latest/only version of) a list of playlists
    * Creating frontend data from the version history of one playlist
    */

  def execute(playlistName: Option[String] = None): Unit = {
    val today = LocalDate.now().format(ISO_LOCAL_DATE)

    cachingJson(objectMapper, cacheLocation.resolve("coordinates").resolve(today + ".json").toString, new TypeReference[Vector[(String,String)]]{}) {
      playlistCoordinatesToNice(pullPlaylistCoordinates(web, objectMapper, uriBuilder())).toVector
    }

//    cachingObj(objectMapper.writeValueAsString(_), objectMapper.readValue(_, new TypeReference[Vector[(String,String)]]{}))(
//      cacheLocation.resolve("coordinates").resolve(today + ".json").toString) {
//      playlistCoordinatesToNice(pullPlaylistCoordinates(web, objectMapper, uriBuilder())).toVector
//    }

//    val v = Iterator(cachingJson(cacheLocation.resolve("coordinates").resolve(today + ".json").toString)(
//        pullPlaylistCoordinates(web, objectMapper, uriBuilder())))
//      .flatMap(playlistCoordinatesToNice)
      .filter(playlistName
        .map(name => (nameAndId:(String,String)) => name == nameAndId._1)
        .getOrElse(_=> true))
//      .toVector
      .parallelMap { case (name, id) =>
//        cachingObj()
//
        objectMapper.readValue(
          cachingString(archiveOutputLocation
            .resolve(cleanName(name))
            .resolve(id)
            .resolve(today + ".json")
            .toString) {
            objectMapper
              .valueToTree[JsonNode](pullPlaylistAndMakeNice(web, objectMapper, uriBuilder(), id))
              .toPrettyString
          },
          classOf[NicePlaylist])
      }
  }

  def historiciseByLatestName(sanisisedName: String): Unit = {
    val today = LocalDate.now().format(ISO_LOCAL_DATE)
    val finder = ".+/([^/]+)/([^/]+)/([^/]+).json".r

    val files = walk(archiveOutputLocation)
      .iterator().asScala
      .map(_.toString)
      .flatMap(path => path match {
        case finder(name, id, date) => Some((name, date, id, path))
        case _ => None
      })
      .to(LazyList)

    // Probably overengineering to track a playlist by its unique ID back in time even across renames
    val targetId = files
      .filter(_._1 == sanisisedName)
      .maxBy(_._2)
      ._3

    val datesAndPlaylists = files
      .filter(_._3 == targetId)
      .sortBy(_._2)
      .map {
        case (_, date, _, pathStr) => (date, objectMapper
          .readValue(readString(path(pathStr)), classOf[NicePlaylist])) }
      .toVector

//    val plsts = tuples
//      .map {
//        case (_, date, _, pathStr) => objectMapper
//          .readValue(readString(path(pathStr)), classOf[NicePlaylist])
//          .copy(name = date) }
////      .map(_._4)
////      .map(path(_))
////      .map(readString)
////      .map(objectMapper.readValue(_, classOf[NicePlaylist]))
//      .toVector
    val result = comparisonise(
      datesAndPlaylists.map {
        case (date, playlist) => playlist.copy(name = date)},
      datesAndPlaylists.last._2.name)

    val directory = frontendOutputLocation.resolve(today)
    directory.toFile.mkdirs()

    writeString(
      directory.resolve(s"$sanisisedName.json"),
      objectMapper.valueToTree[JsonNode](result).toPrettyString)
  }

  private def cleanName(playlistName:String) =
    playlistName.replaceAll("[^a-zA-Z0-9,_-]+", "_")

  /*def fixYesterday()={
    val finderToday = ".+/([^/]+)/([^/]+)/([^/]+).json".r
    val finderYesterday = ".+/2024-03-09/([^/]+)".r

    val todays = walk(cacheLocation)
      .iterator().asScala
      .map(_.toString)
      .filter(!_.contains("2024-03-09"))
      .flatMap(path => path match {
        case finderToday(name, id, date) => Some((name, date, id, path))
        case _ => None
      })

    val yesterdays = walk(cacheLocation)
      .iterator().asScala
      .map(_.toString)
      .flatMap(path => path match {
        case finderYesterday(name) => Some((name, path))
        case _ => None
      })

      // Meh it's not worth it
  }*/
}
