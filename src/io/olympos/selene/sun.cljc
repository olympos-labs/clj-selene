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

(ns io.olympos.selene.sun
  (:require [io.olympos.selene.julian :as julian]
            [io.olympos.selene.vector :as vector]
            [io.olympos.selene.math-util :refer [tau frac sin cos]]))

(def ^:const sun-distance 149598000.0)
(def ^:const sun-mean-radius 695700.0)

(defn position-equatorial
  "Returns the equatorial position of the sun at the Julian date in
  polar coordinates. Convert to vector form if needed manually."
  [jd]
  (let [T (julian/century jd)
        M (* tau (frac (+ 0.993133 (* 99.997361 T))))
        L (* tau (frac (+ 0.7859453 (/ M tau)
                          (/ (+ (* 6893.0 (sin M))
                                (* 72.0 (sin (* 2.0 M)))
                                (* 6191.2 T))
                             1296000.0))))

        d (* sun-distance (- 1 (* 0.016718 (cos (julian/true-anomaly jd)))))]

    (vector/->Polar L 0.0 d)))
