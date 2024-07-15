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

(ns io.olympos.selene.julian
  (:require [clojure.math :as math]
            [io.olympos.selene.math-util :refer [tau frac]]
            [cljc.java-time.instant :as instant]
            [cljc.java-time.zoned-date-time :as zdt]))

(deftype JulianDate [dt mjd])

(defn from-datetime [dt]
  (->JulianDate
   dt
   (+ (/ (-> dt zdt/to-instant instant/to-epoch-milli)
         86400000.0)
    40587.0)))

(defn zone [^JulianDate jd]
  (zdt/get-zone (.-dt jd)))

(defn to-datetime [mjd zone]
  (let [mjdi (instant/of-epoch-milli (math/round  (* (- mjd 40587.0) 86400000.0)))]
    (zdt/of-instant mjdi zone)))

(defn at-mjd [jd mdj]
  (from-datetime (to-datetime mdj (zone jd))))


(defn- day-of-year [^JulianDate jd]
  (zdt/get-day-of-year (.-dt jd)))

(defn century [^JulianDate jd]
  (/ (- (.-mjd jd) 51544.5) 36525.0))

(defn at-century [jd jc]
  (at-mjd jd (+ (* jc 36525.0) 51544.5)))

(defn true-anomaly [jd]
  (* tau (frac (/ (- (day-of-year jd) 5.0)
                  365.256363))))
