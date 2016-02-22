# strelka-wat-bot

This is simple bot for Telegram that will tell you all about event in Strelka Institute

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running

To run locally you will need to supply configuration file `dev-config.edn`.
You can find template in [`dev-config-sample.edn`](/dev-config-sample.edn).

To start a web server for the application, run:

    lein ring server

## License

Copyright © 2016 Anton Chebotaev
