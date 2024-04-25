package io.github.teonistor.spotifier

import io.github.teonistor.spotifier.data.FrontendData.{TransmissibleConnector, TransmissiblePlaylist, TransmissibleTrack}
import io.github.teonistor.spotifier.data.NicePlaylist
import io.github.teonistor.spotifier.data.NicePlaylist.NiceTrack
import org.scalatest.funsuite.AnyFunSuite

class SpotifyDataUtilTest extends AnyFunSuite {

  test("comparisonise") {
    val result = SpotifyDataUtil.comparisonise(Vector(
      NicePlaylist("First", "irrelevant", Vector(
        NiceTrack("id1", "name 1", "album", Vector.empty, Vector.empty),
        NiceTrack("id2", "name 2", "album", Vector.empty, Vector.empty),
        NiceTrack("id3", "name 3", "album", Vector.empty, Vector.empty))),
      NicePlaylist("Second", "irrelevant", Vector(
        NiceTrack("id2", "name 2", "album", Vector.empty, Vector.empty),
        NiceTrack("id1", "name 1", "album", Vector.empty, Vector.empty),
        NiceTrack("id5", "name 5", "other", Vector.empty, Vector.empty))),
      NicePlaylist("Third", "irrelevant", Vector(
        NiceTrack("id2", "name 2", "album", Vector.empty, Vector.empty),
        NiceTrack("id4", "name 4", "album", Vector.empty, Vector.empty),
        NiceTrack("id3", "name 3", "album", Vector.empty, Vector.empty)))
    ), "Bob")

    assert(result.title == "Bob")
    assert(result.playlists == Vector(
      TransmissiblePlaylist("First", Vector(
        TransmissibleTrack("id1", "name 1", "album", "", ""),
        TransmissibleTrack("id2", "name 2", "album", "", ""),
        TransmissibleTrack("id3", "name 3", "album", "", ""))),
      TransmissiblePlaylist("Second", Vector(
        TransmissibleTrack("id2", "name 2", "album", "", ""),
        TransmissibleTrack("id1", "name 1", "album", "", ""),
        TransmissibleTrack("id5", "name 5", "other", "", ""))),
      TransmissiblePlaylist("Third", Vector(
        TransmissibleTrack("id2", "name 2", "album", "", ""),
        TransmissibleTrack("id4", "name 4", "album", "", ""),
        TransmissibleTrack("id3", "name 3", "album", "", "")))))
    assert(result.immediateConnectors.toSet == Set(
      TransmissibleConnector(0, 1, 0, 1, "id1"),
      TransmissibleConnector(0, 1, 1, 0, "id2"),
      TransmissibleConnector(1, 2, 0, 0, "id2")))
    assert(result.skippingConnectors.toSet == Set(
      TransmissibleConnector(0, 2, 2, 2, "id3")))
  }

}
