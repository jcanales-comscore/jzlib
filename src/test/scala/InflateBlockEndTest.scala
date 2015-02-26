package com.jcraft.jzlib

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers


class InflateBlockEndTest extends FlatSpec with BeforeAndAfter with ShouldMatchers {
  
  var inflater : Inflater = _
  
  before {
    inflater = new Inflater(JZlib.WrapperType.GZIP)
  }
  
  after {
    
  }
  
  behavior of "Inflater"
  
  it can "return on block end of a multi-block gz file" in {
    val twoblocks = "/helloworld.gz".fromResource
    
    twoblocks.length should equal (37)
    
    val block1 = "hello\n".getBytes
    val block2 = "world\n".getBytes
    
    block1.length should equal (6)
    block2.length should equal (6)
    
    val outBuf = new Array[Byte](12)
        
    inflater.setInput(twoblocks)
    inflater.setOutput(outBuf)
    val ret = inflater.inflate(JZlib.Z_FULL_FLUSH, true)
    ret should equal (JZlib.Z_OK)
    val writtenBytes = inflater.total_out.asInstanceOf[Int]
    writtenBytes should equal (6)
    
    val tmpBuf = new Array[Byte](writtenBytes)
    System.arraycopy(inflater.getNextOut, 0, tmpBuf, 0, writtenBytes)
    tmpBuf should equal (block1)
    
    var ret2 = JZlib.Z_UNKNOWN.asInstanceOf[Int]
    var blocksRead = 1
    do {
      ret2 = inflater.inflate(JZlib.Z_FULL_FLUSH, true)
      if ((inflater.getDataType & 128) != 0)
        blocksRead += 1
    } while (!inflater.finished)
    ret2 should equal (JZlib.Z_STREAM_END)
    blocksRead should equal (5)
    val writtenBytes2 = inflater.total_out.asInstanceOf[Int]
    writtenBytes2 should equal (12)
    inflater.getNextOut should equal (block1 ++ block2)
  }
}
