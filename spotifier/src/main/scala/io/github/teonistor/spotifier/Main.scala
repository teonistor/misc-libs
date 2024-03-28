package io.github.teonistor.spotifier

object Main {
  def main(arg: Array[String]): Unit = {
//    Coordinator.createDirectoiesAndFiles()
//    Coordinator.execute()
    Coordinator.historiciseByLatestName("Board_Games")
//    Coordinator.historiciseByLatestName("_Drifting_Home_OST_-_2022")
  }
}
