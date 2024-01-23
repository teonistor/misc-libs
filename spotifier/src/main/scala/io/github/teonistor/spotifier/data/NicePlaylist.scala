package io.github.teonistor.spotifier.data

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY
import io.github.teonistor.spotifier.data.NicePlaylist.NiceTrack

@JsonAutoDetect(fieldVisibility=ANY)
case class NicePlaylist(name: String,
                        owner: String,
                        tracks: Vector[NiceTrack]) {

  def addTrack(name: String, album: String, artists: String*) =
    new NicePlaylist(this.name, owner, this.tracks :+ NiceTrack(name, album, artists.toVector))
}

object NicePlaylist {

  def empty(name: String, owner: String) = new NicePlaylist(name, owner, Vector.empty)

  case class NiceTrack(name: String,
                       album: String,
                       artists: Vector[String])
}
