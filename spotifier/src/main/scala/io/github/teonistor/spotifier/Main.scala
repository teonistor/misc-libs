package io.github.teonistor.spotifier

object Main {
  def main(arg: Array[String]): Unit = {
//    Coordinator.createDirectoiesAndFiles()
    Coordinator.snapshotAccount()

    Coordinator.historiciseByLatestName("Board_Games")
    Coordinator.historiciseByLatestName("Carabin_Nicolae_teonoo7")
    Coordinator.historiciseByLatestName("Rom_ne_ti")
    Coordinator.historiciseByLatestName("On_Repeat")
    Coordinator.historiciseByLatestName("Foc_mocnit_de_tab_r_")
    Coordinator.historiciseByLatestName("Mezzo_Piano")
    Coordinator.historiciseByLatestName("Mezzo_Forte")
    Coordinator.historiciseByLatestName("Repeat_Rewind")
  }
}
