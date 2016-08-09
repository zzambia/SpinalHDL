import random
from Queue import Queue

import cocotb
from cocotb.result import TestFailure, TestSuccess
from cocotb.triggers import Timer, Edge, RisingEdge, Join, FallingEdge

from spinal.common.AhbLite3 import AhbLite3MasterDriver, AhbLite3SlaveMemory, AhbLite3MasterIdle, AhbLite3TraficGenerator, AhbLite3MasterReadChecker, AhbLite3Terminaison
from spinal.common.misc import setBit, randSignal, assertEquals, truncUInt, sint, ClockDomainAsyncReset, randBoolSignal, \
    BoolRandomizer, StreamRandomizer,StreamReader, FlowRandomizer, Bundle, simulationSpeedPrinter


class AhbLite3TraficGeneratorWithMemory(AhbLite3TraficGenerator):
    def getTransactions(self):
        transactions = AhbLite3TraficGenerator.getTransactions(self)
        for trans in transactions:
            if trans.HTRANS >= 2:
                write = trans.HWRITE
                size = 1 << trans.HSIZE
                address = trans.HADDR
                addressOffset = address % (self.dataWidth / 8)

                if write == 1:
                    for idx in range(size):
                        self.ram[address  + idx] = (trans.HWDATA >> (8*(addressOffset + idx))) & 0xFF
                        # cocotb.log.info("WRITE %x %x" % (address  + idx, (trans.HWDATA >> (8*(addressOffset + idx))) & 0xFF))
                else:
                    data = 0
                    for idx in xrange(size):
                        data |= self.ram[address + idx] << (8*(addressOffset + idx))
                    self.readBuffer.put(data)
                    # cocotb.log.info("READ %x %x" % (trans.HADDR,data))


        return transactions

    def __init__(self, addressWidth, dataWidth,readBuffer):
        AhbLite3TraficGenerator.__init__(self, addressWidth, dataWidth)
        self.ram = bytearray(b'\x00' * (1 << addressWidth))
        self.readBuffer = readBuffer



@cocotb.test()
def test1(dut):
    dut.log.info("Cocotb test boot")
    random.seed(0)

    cocotb.fork(ClockDomainAsyncReset(dut.clk, dut.reset))
    cocotb.fork(simulationSpeedPrinter(dut.clk))

    readQueue = Queue()
    ahb = Bundle(dut, "ahb")
    driver  = AhbLite3MasterDriver(ahb, AhbLite3TraficGeneratorWithMemory(10, 32,readQueue), dut.clk, dut.reset)
    checker = AhbLite3MasterReadChecker(ahb, readQueue, dut.clk, dut.reset)
    terminaison = AhbLite3Terminaison(ahb,dut.clk,dut.reset)

    while True:
        yield RisingEdge(dut.clk)
        if checker.counter > 4000:
            break

    dut.log.info("Cocotb test done")