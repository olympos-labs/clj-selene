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

(ns io.olympos.selene.math-util
  (:require [clojure.math :as math]))

(def pi math/PI)
(def tau (* 2 pi))

;; arcseconds/rad
(def arcs (* 10 tau))

;; todo: definline I guess
(defn frac [x] (rem x 1.0))

(defn cos [x] (Math/cos x))
(defn sin [x] (Math/sin x))

