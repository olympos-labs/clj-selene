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

(ns io.olympos.selene.julian-test
  (:require [cljc.java-time.zoned-date-time :as zdt]
            [cljc.java-time.zone-id :as zid]
            [io.olympos.selene.julian :as julian]
            [io.olympos.selene.math-util :refer [pi]]
            [clojure.test :refer [deftest is]]
            ;; ensure timezones are loaded
            #?@(:cljs
                [["@js-joda/core"]
                 ["@js-joda/timezone"]])))

(defn new-date [year month day hour minute second zone]
  (zdt/of year month day hour minute second 0 (zid/of zone)))

(defn jd [year month day hour minute second zone]
  (julian/from-datetime (new-date year month day hour minute second zone)))

(defn approx? [a b]
  (< (abs (- a b)) 0.001))

(defn sloppy-approx? [a b]
  (< (abs (- a b)) 0.1))

(deftest modified-julian-date
  ;; MJD epoch is midnight of November 17th, 1858.
  (is (approx? 0.0 (.-mjd (jd 1858 11 17 0 0 0 "UTC"))))

  (is (approx? 57984.629 (.-mjd (jd 2017 8 19 15 6 16 "UTC"))))

  ;; verify that time zones are taken into account
  (is (approx? 57984.546 (.-mjd (jd 2017 8 19 15 6 16 "GMT+2")))))

(deftest julian-century
  (let [jc (fn [year month day hour minute second zone]
             (julian/century (jd year month day hour minute second zone)))]
    (is (approx? 0.000 (jc 2000 1 1 0 0 0 "UTC")))
    (is (approx? 0.170 (jc 2017 1 1 0 0 0 "UTC")))
    (is (approx? 0.505 (jc 2050 7 1 0 0 0 "UTC")))))

(deftest true-anomaly
  (let [ta (fn [year month day hour minute second zone]
             (julian/true-anomaly (jd year month day hour minute second zone)))]
    (is (sloppy-approx? 0.0 (ta 2017 1 4 0 0 0 "UTC")))
    (is (sloppy-approx? pi (ta 2017 7 4 0 0 0 "UTC")))))
