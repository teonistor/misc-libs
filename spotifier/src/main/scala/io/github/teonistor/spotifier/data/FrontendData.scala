package io.github.teonistor.spotifier.data

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY
import io.github.teonistor.spotifier.data.FrontendData.{TransmissibleConnector, TransmissiblePlaylist}

@JsonAutoDetect(fieldVisibility=ANY)
case class FrontendData(title: String,
                        playlists: Vector[TransmissiblePlaylist],
                        immediateConnectors: Vector[TransmissibleConnector],
                        skippingConnectors: Vector[TransmissibleConnector])

object FrontendData {

  case class TransmissiblePlaylist(name: String,
                                   tracks: Vector[TransmissibleTrack])

  case class TransmissibleTrack(id:String,  // Double whammy! Let's use this to send the Spotify ID *and* to support click-selection!
                                name: String,
                                additionalLines: Vector[String])

  def TransmissibleTrack(id:String, name: String, additionalLines: String*): TransmissibleTrack =
    TransmissibleTrack(id, name, additionalLines.toVector)

  case class TransmissibleConnector(startCol: Int,
                                    endCol: Int,
                                    startRow: Int,
                                    endRow: Int,
                                    trackId: String) // I'm tempted to say Any, but really it has to be the same as TransmissibleTrack.id and I'm not about ot type-parameterise the whole thing
  // TODO Preconditions? Which are really postconditions
}