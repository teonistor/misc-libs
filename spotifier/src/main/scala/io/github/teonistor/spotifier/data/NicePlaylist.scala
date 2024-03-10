package io.github.teonistor.spotifier.data

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.annotation.{JsonAutoDetect, JsonInclude}
import io.github.teonistor.spotifier.data.NicePlaylist.NiceTrack

@JsonAutoDetect(fieldVisibility=ANY)
case class NicePlaylist(name: String,
                        owner: String,
                        tracks: Vector[NiceTrack]) {

  def addTrack(name: String, album: String, artists: Vector[String], affinity: Vector[String]) =
    new NicePlaylist(this.name, owner, this.tracks :+ NiceTrack(name, album, artists, affinity))
}

object NicePlaylist {

  def empty(name: String, owner: String) = new NicePlaylist(name, owner, Vector.empty)

  @JsonInclude(NON_EMPTY)
  case class NiceTrack(name: String,
                       album: String,
                       artists: Vector[String],
                       affinity: Vector[String])
}
