package io.github.teonistor.ggg

import org.junit.jupiter.api.Test

class NameDecoderTest {

  private val nameDecoder = new NameDecoder

  @Test
  def unencodeSample(): Unit = {
    val keyIn = "test%2Fkey"
    val keyOut= "test/key"

    assert(nameDecoder(keyIn) == keyOut)
  }

  @Test
  def unencodeEncoded(): Unit = {
    val keyIn = "20220625-114728+-+Boscombe%2C+Talbot+circular.kml"
    val keyOut= "20220625-114728 - Boscombe, Talbot circular.kml"

    assert(nameDecoder(keyIn) == keyOut)
  }

  @Test
  def unencodeUnencoded(): Unit = {
    val keyIn = "20220701-162330 - Sandbanks Barbecue Loop.kml"
    val keyOut= "20220701-162330 - Sandbanks Barbecue Loop.kml"

    assert(nameDecoder(keyIn) == keyOut)
  }
}
