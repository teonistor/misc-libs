package io.github.teonistor.spotifier

import com.fasterxml.jackson.databind.json.JsonMapper
import io.github.teonistor.spotifier.data.FrontendData.{TransmissibleConnector, TransmissiblePlaylist, TransmissibleTrack}
import io.github.teonistor.spotifier.data.NicePlaylist
import io.github.teonistor.spotifier.data.NicePlaylist.NiceTrack
import org.mockito.IdiomaticMockito
import org.mockito.stubbing.{DefaultAnswer, ReturnsDefaults}
import org.scalatest.funsuite.AnyFunSuite
import org.springframework.core.io.ClassPathResource
import reactor.core.publisher.Flux

import scala.jdk.CollectionConverters.IterableHasAsScala

class SpotifyDataUtilTest extends AnyFunSuite with IdiomaticMockito {
  private implicit val defaultAnswer: DefaultAnswer = ReturnsDefaults

  test("new list playlists") {
    val webHelper = mock[WebHelper]
    val objectMapper = JsonMapper.builder().findAndAddModules().build()
    val spotifyDataUtil = new SpotifyDataUtil(webHelper, objectMapper)

    webHelper.getRepeatedly("https://api.spotify.com/v1/me/playlists") returns Flux.just(
      objectMapper.readTree(new ClassPathResource("api-response/list-playlists.json").getInputStream))

    assert(spotifyDataUtil.newListPlaylists()
      .collectList().block().asScala.toList == List(
        NicePlaylist.empty("Mezzo Piano", "teonoo7"),
        NicePlaylist.empty("Cântece din poezii", "foreigner")))
  }

  test("new get playlist") {
    val webHelper = mock[WebHelper]
    val objectMapper = JsonMapper.builder().findAndAddModules().build()
    val spotifyDataUtil = new SpotifyDataUtil(webHelper, objectMapper)

    webHelper.getRepeatedly("https://api.spotify.com/v1/playlists/3cEYpjA9oz9GiPac4AsH4n/tracks?fields=items%28added_by.id%2Ctrack.album.name%2Ctrack.artists%28name%29%2Ctrack.id%2Ctrack.name%29%2Cnext&additional_types=track%2Cepisode") returns Flux.just(
      objectMapper.readTree(new ClassPathResource("api-response/get-playlist.json").getInputStream))

    assert(spotifyDataUtil.newGetPlaylist()
      .collectList().block().asScala.toList == List(
        NiceTrack("4rzfv0JLZfVhOhbSQ8o5jZ", "Api", "Progressive Psy Trance Picks Vol.8", Vector("Odiseo"), Vector.empty),
        NiceTrack("5o3jMYOSbaVz3tkgwhELSV", "Is", "Wellness & Dreaming Source", Vector("Vlasta Marek","Vlasta Marek 2"), Vector.empty),
        NiceTrack("4Cy0NHJ8Gh0xMdwyM9RkQm", "All I Want", "This Is Happening", Vector("LCD Soundsystem"), Vector.empty)))
  }

  test("new get top tracks") {
    val webHelper = mock[WebHelper]
    val objectMapper = JsonMapper.builder().findAndAddModules().build()
    val spotifyDataUtil = new SpotifyDataUtil(webHelper, objectMapper)

    webHelper.getRepeatedly("https://api.spotify.com/v1/me/top/tracks?time_range=short_term&limit=50") returns Flux.just(
      objectMapper.readTree(new ClassPathResource("api-response/get-top-tracks.json").getInputStream))

    assert(spotifyDataUtil.newGetTopTracks()
      .collectList().block().asScala.toList == List(
        NiceTrack("7g4R3AxihibF6CffMpPjqp", "Our Decades in the Sun - Instrumental", "Endless Forms Most Beautiful", Vector("Nightwish"), Vector.empty),
        NiceTrack("1nVMT61EdYLfFkQvfAaxAQ", "Cu Flori In Par Si Nimic In Picioare", "Romantica", Vector("Taxi"), Vector.empty)))
  }

  test("backfill IDs") {
    val result = SpotifyDataUtil.backfillIds(Vector(
      NicePlaylist("First", "irrelevant", Vector(
        NiceTrack(null, "name 1", "album", Vector("artist 1"), Vector.empty),
        NiceTrack(null, "name 2", "album", Vector("artist 2"), Vector.empty),
        NiceTrack(null, "name 3", "album", Vector("artist 3"), Vector.empty))),
      NicePlaylist("Second", "irrelevant", Vector(
        NiceTrack(null, "name 2", "album", Vector("artist 2"), Vector.empty),
        NiceTrack(null, "name 1", "album", Vector("artist 1"), Vector.empty),
        NiceTrack(null, "name 5", "other", Vector("artist 5"), Vector.empty))),
      NicePlaylist("Third", "irrelevant", Vector(
        NiceTrack("id1", "name 1", "album", Vector("artist 1"), Vector.empty),
        NiceTrack("id2", "name 2", "album", Vector("artist 2"), Vector.empty),
        NiceTrack("id4", "name 4", "album", Vector("artist 4"), Vector.empty),
        NiceTrack("id3", "name 3", "album", Vector("artist 3"), Vector.empty)))))

    assert(result == Vector(
      NicePlaylist("First", "irrelevant", Vector(
        NiceTrack("id1", "name 1", "album", Vector("artist 1"), Vector.empty),
        NiceTrack("id2", "name 2", "album", Vector("artist 2"), Vector.empty),
        NiceTrack("id3", "name 3", "album", Vector("artist 3"), Vector.empty))),
      NicePlaylist("Second", "irrelevant", Vector(
        NiceTrack("id2", "name 2", "album", Vector("artist 2"), Vector.empty),
        NiceTrack("id1", "name 1", "album", Vector("artist 1"), Vector.empty),
        NiceTrack(null, "name 5", "other", Vector("artist 5"), Vector.empty))),
      NicePlaylist("Third", "irrelevant", Vector(
        NiceTrack("id1", "name 1", "album", Vector("artist 1"), Vector.empty),
        NiceTrack("id2", "name 2", "album", Vector("artist 2"), Vector.empty),
        NiceTrack("id4", "name 4", "album", Vector("artist 4"), Vector.empty),
        NiceTrack("id3", "name 3", "album", Vector("artist 3"), Vector.empty)))))
  }

  test("comparisonise") {
    val result = SpotifyDataUtil.comparisonise("Bob", Vector(
      NicePlaylist("First", "irrelevant", Vector(
        NiceTrack("id1", "name 1", "album", Vector("artist 1"), Vector.empty),
        NiceTrack("id2", "name 2", "album", Vector("artist 2"), Vector.empty),
        NiceTrack("id3", "name 3", "album", Vector("artist 3"), Vector.empty))),
      NicePlaylist("Second", "irrelevant", Vector(
        NiceTrack("id2", "name 2", "album", Vector("artist 2"), Vector.empty),
        NiceTrack("id1", "name 1", "album", Vector("artist 1"), Vector.empty),
        NiceTrack("id5", "name 5", "other", Vector("artist 5"), Vector.empty))),
      NicePlaylist("Third", "irrelevant", Vector(
        NiceTrack("id2", "name 2", "album", Vector("artist 2"), Vector.empty),
        NiceTrack("id4", "name 4", "album", Vector("artist 4"), Vector.empty),
        NiceTrack("id3", "name 3", "album", Vector("artist 3"), Vector.empty)))))

    assert(result.title == "Bob")
    assert(result.playlists == Vector(
      TransmissiblePlaylist("First", Vector(
        TransmissibleTrack("id1", "name 1", "album", "artist 1", ""),
        TransmissibleTrack("id2", "name 2", "album", "artist 2", ""),
        TransmissibleTrack("id3", "name 3", "album", "artist 3", ""))),
      TransmissiblePlaylist("Second", Vector(
        TransmissibleTrack("id2", "name 2", "album", "artist 2", ""),
        TransmissibleTrack("id1", "name 1", "album", "artist 1", ""),
        TransmissibleTrack("id5", "name 5", "other", "artist 5", ""))),
      TransmissiblePlaylist("Third", Vector(
        TransmissibleTrack("id2", "name 2", "album", "artist 2", ""),
        TransmissibleTrack("id4", "name 4", "album", "artist 4", ""),
        TransmissibleTrack("id3", "name 3", "album", "artist 3", "")))))
    assert(result.immediateConnectors.toSet == Set(
      TransmissibleConnector(0, 1, 0, 1, "id1"),
      TransmissibleConnector(0, 1, 1, 0, "id2"),
      TransmissibleConnector(1, 2, 0, 0, "id2")))
    assert(result.skippingConnectors.toSet == Set(
      TransmissibleConnector(0, 2, 2, 2, "id3")))
  }
}
