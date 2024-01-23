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

  case class TransmissibleTrack(name: String,
                                additionalLines: Vector[String])

  case class TransmissibleConnector(startCol: Int,
                                    endCol: Int,
                                    startRow: Int,
                                    endRow: Int)
}