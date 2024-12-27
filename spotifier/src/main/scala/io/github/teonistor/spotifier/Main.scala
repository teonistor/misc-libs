package io.github.teonistor.spotifier

object Main {
  def main(arg: Array[String]): Unit = {
//    Coordinator.createDirectoiesAndFiles()
    Coordinator.snapshotAccount()
    Coordinator.historiciseByLatestName("Board_Games")
//    Coordinator.historiciseByLatestName("Carabin_teonoo7")
    Coordinator.historiciseByLatestName("Carabin_Nicolae_teonoo7")
    Coordinator.historiciseByLatestName("Repeat_Rewind")
    Coordinator.historiciseByLatestName("Your_Top_Songs_2024")
  }
}
