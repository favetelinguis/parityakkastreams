# parityakkastreams
A client for Parity using Akka-streams

[PMD][1] source for parity with an optional flow to convert PMD
events to MarketListner events.

###Usage example
```
PMDSource(settings = s, bufferSize = 10)
  .via(ParityOrderBookReconstructionFlow())
  .runWith(Sink.foreach(println(_)))
```

###TODO:
1. Make PMD Source more robust to failures and configurable
2. Make a source AND a sink to support [POE][2]

[1]:https://github.com/paritytrading/parity/blob/master/parity-net/doc/PMD.md
[2]:https://github.com/paritytrading/parity/blob/master/parity-net/doc/POE.md