# Selene

A Clojure/ClojureScript library to compute the lunar calendar and Moon phases,
accurate up to a couple of minutes (on average ~2 minutes off).

See [Moon Phase Approximation
Benchmark](https://github.com/hypirion/moon-phase-approximation-benchmark) for
exact details on how this fare compared to other Moon phase algorithms.

## Quickstart

First, add the dependency on Selene like so:

```clj
;; lein
[io.olympos/selene "1.0.0"]
;; deps.edn
io.olympos/selene {:mvn/version "1.0.0"}

;; if you're on CLJS, ensure you have dependencies on js-joda and
;; js-joda-timezone, e.g. like so:

henryw374/js-joda {:mvn/version "3.2.0-0"}
io.github.cljsjs/js-joda-timezone {:mvn/version "2.17.2-1"}
```

The namespace `io.olympos.selene.moon` contains all the functions. If you're
e.g. making a calendar app and want to know whether a day contains a Moon phase,
this is one way to get all dates with a Moon phase change:

```clj
(require '[io.olympos.selene.moon :as moon]
         '[cljc.java-time.zoned-date-time :as zdt]
         '[cljc.java-time.local-date :as ld]
         '[cljc.java-time.temporal.chrono-unit :as chrono-unit])

(let [now (zdt/now)
      start-of-year (zdt/of (zdt/get-year now) 1 1 0 0 0 0 (zdt/get-zone now))]
  (->> (moon/phase-seq {:time start-of-year})
       ;; remove gibbous/crescent phases:
       (filter #(contains? #{::moon/new ::moon/first-quarter
                             ::moon/full ::moon/third-quarter} (:phase %)))
       ;; only pick the ones in this year
       (take-while #(= (zdt/get-year start-of-year)
                       (zdt/get-year (:time %))))
       ;; prettyprint it
       (map (fn [{:keys [time phase]}]
              [(-> time zdt/to-local-date ld/to-string) phase]))))
```

However, probing one and one day isn't inefficient either:

```clj
(moon/phase-change-on-date (zdt/now))
```

Note that the functions always take in a ZonedDateTime (or the js-joda
equivalent), even for date like functions like the one above. Selene needs to
know the start and end of the day to correctly know if the phase change happens
inside that interval, which requires time zones.

For a complete example, have a look at [moon phase calendar example
app](https://github.com/hypirion/selene-calendar-example). Its source is over at
https://github.com/hypirion/selene-calendar-example.

See the [API reference](http://olympos-labs.github.io/clj-selene/) for the full
list of functions, as well as tips on how to use it in your application.

## Rationale

Finding a library to compute the Moon phases in JavaScript (or ClojureScript)
should be simple, right? Some Googling led me to
[lunarphase-js](https://www.npmjs.com/package/lunarphase-js), but this isn't
exact at all. It is on average 8 hours off, but it can be as inaccurate as 19.5
hours. That causes it to report the wrong date for about 1/3 of all Moon phases.

This happens for two reasons: First, most approximations use the synadic month
and just compute the distance into a lunar month by dividing by it. However, the
lunar month changes in length: While the average is about 29.53 days, it varies
from 29.26 to 29.80 days -- about 13 hours difference. That matters, and can
put the Moon phase on the wrong date.

The second is purely technical: JavaScript's native date type doesn't contain
time zones, and the libraries I've seen usually use that one. This can be iffy
if you're dealing with actual dates (not datetimes): Not too long ago, we had a
full Moon happening at 23:46 UTC, which in my time zone was 00:46 the next day.
If I didn't take the time zone into consideration, I'd attach the full Moon to
the wrong date.

Now, you could argue that time zones isn't technically part of "the compute the
lunar calendar or Moon phases", as long as you handle them in some layer above.
However, I guarantee you incorrect use of this library would happen if it didn't
enforce time zones on you.

That's why this library was made: To compute Moon phases accurate up to a couple
of minutes off, as well as taking time zones into account.

The implementation is effectively a port of a subset of
[commons-suncalc](https://github.com/shred/commons-suncalc) and some additions
for common operations related to Moon phases.

## Current Issues

Using this in ClojureScript incurs a heavy penalty if you want to use it with
time zones, as that [time zone lookup
table](https://github.com/js-joda/js-joda/blob/d8431367a355e3e86adebedaab05c3072178ff5b/packages/timezone/data/packed/latest.json)
is large... Like, surprisingly large. Optimized it is 782.5 KB unzipped.
However, it compresses surprisingly well: Down to only 37 KB using gzip with
standard compression settings. Therefore, if you want to use this with time
zones (as you should), be very sure that you send compressed JavaScript files
from your server.

I still think 37 KB is quite a lot, so whenever the Temporal proposal is to
ECMAScript is accepted and used by the vast majority of browsers, I will move
over to that instead. Since that will be a breaking change, it will likely be a
new library and this one will be deprecated.

## Change Policy

Changes to this repository are always backwards compatible, but I may improve
the underlying algorithm and make it more accurate. For this reason, you can not
assume that the algorithm will give back identical timestamps for different
versions, but you can assume that its accuracy won't worsen in future
implementations.

## Contributing

Please read <CONTRIBUTING.md> for the full gist, but TL;DR: This library is
stable and I don't want feature requests or PRs related to that. Feel free to
fork the library though!

If you encounter a bug, add a test case and verify that it works/fails by
running

```shell
$ clj -Mtest && clj -Mcljs-test
```

on the command line. This first tests Clojure and then ClojureScript version.

To build a version, do

```shell
$ clj -T:build clean && clj -T:build jar && env CLOJARS_USERNAME=xxx CLOJARS_PASSWORD=yyy clj -X:deploy
```

## License

Copyright © 2018 Richard "Shred" Körber, 2024 Jean Niklas L'orange

Distributed under the [Apache License
2.0](http://www.apache.org/licenses/LICENSE-2.0), see the file LICENSE.
