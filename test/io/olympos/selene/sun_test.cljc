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

(ns io.olympos.selene.sun-test
  (:require [io.olympos.selene.julian-test :refer [new-date]]
            [io.olympos.selene.julian :as julian]
            [io.olympos.selene.sun :as sun]
            [clojure.test :refer [deftest is are]]
            #?@(:cljs
                [["@js-joda/core"]
                 ["@js-joda/timezone"]]))
  #?(:clj (:import org.shredzone.commons.suncalc.util.Sun
                   org.shredzone.commons.suncalc.util.JulianDate)))

(defn approx? [a b]
  (< (abs (- a b)) 1e-8))

#?(:clj
   (defn compare-phis [dt]
     (let [expected (Sun/positionEquatorial (JulianDate. dt))
           actual-phi (sun/position-equatorial-phi (julian/from-datetime dt))]
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
