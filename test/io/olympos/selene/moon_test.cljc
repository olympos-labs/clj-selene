;; clj-selene, based on "Shredzone Commons - suncalc"
;;
;; Copyright (C) 2018 Richard "Shred" Körber, 2024 Jean Niklas L'orange
;;   http://commons.shredzone.org, https://olympos.io
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;;
;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

(ns io.olympos.selene.moon-test
  (:require [io.olympos.selene.julian-test :refer [new-date]]
            [io.olympos.selene.math-util :refer [tau]]
            [io.olympos.selene.julian :as julian]
            [io.olympos.selene.moon :as moon]
            [clojure.test :refer [deftest is are]]
            ;; ensure timezones are loaded
            #?@(:cljs
                [["@js-joda/core"]
                 ["@js-joda/timezone"]]))
  #?(:clj (:import org.shredzone.commons.suncalc.util.Moon
                   org.shredzone.commons.suncalc.util.Sun
                   org.shredzone.commons.suncalc.util.JulianDate)))

(defn approx? [a b]
  (< (abs (- a b)) 1e-8))

#?(:clj
   (defn compare-phis [dt]
     (let [expected (Moon/positionEquatorial (JulianDate. dt))
           actual-phi (moon/position-equatorial-phi (julian/from-datetime dt))]
       (is (approx? (.getPhi expected) actual-phi))))
   :default
   (defn compare-phis [dt]
     ;; no reference library in other languages, so noop here
     true))

(deftest equatorial-phis-are-the-same
  (are [year month day hour minute second zone]
      (compare-phis (new-date year month day hour minute second zone))
      1858 11 17 0 0 0 "UTC"
      2017 8 19 15 6 16 "UTC"
      2017 8 19 15 6 16 "GMT+2"

      2000 1 1 0 0 0 "UTC"
      2017 1 1 0 0 0 "UTC"
      2050 7 1 0 0 0 "UTC"

      2017 1 4 0 0 0 "Europe/Oslo"
      2017 1 4 18 19 20 "Europe/Oslo"))


(def ^:private sun-light-time-tau (/ 8.32 (* 1440.0 36525.0)))

#?(:clj
   (defn suncalc-moonphase [dt]
     (let [jd (JulianDate. dt)
           t (.getJulianCentury jd)
           sun (Sun/positionEquatorial (.atJulianCentury jd (- t sun-light-time-tau)))
           moon (Moon/positionEquatorial (.atJulianCentury jd t))]
       (mod (- (.getPhi moon) (.getPhi sun)) tau))))

#?(:clj
   (defn compare-moon-phases [dt]
     (let [expected (suncalc-moonphase dt)
           actual (moon/phase-angle dt)]
       (is (approx? expected actual))))
   :default
   (defn compare-moon-phases [dt]
     ;; no reference library in other languages, so noop here
     true))

(deftest compare-with-suncalc
  (are [year month day hour minute second zone]
      (compare-moon-phases (new-date year month day hour minute second zone))
      1858 11 17 0 0 0 "UTC"
      2017 8 19 15 6 16 "UTC"
      2017 8 19 15 6 16 "GMT+2"

      2000 1 1 0 0 0 "UTC"
      2017 1 1 0 0 0 "UTC"
      2050 7 1 0 0 0 "UTC"

      2017 1 4 0 0 0 "Europe/Oslo"
      2017 1 4 18 19 20 "Europe/Oslo"))

;; TODO: Compare 2024 and 2071 for UTC, Oslo and Japan.
