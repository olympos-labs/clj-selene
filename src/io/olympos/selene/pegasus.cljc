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

(ns io.olympos.selene.pegasus)

(defn calculate
  "Uses the Pegasus method to find x where f(x) = 0 within accuracy/2 bounds."
  [lower upper accuracy f]
  (let [x1 lower
        x2 upper

        f1 (f x1)
        f2 (f x2)]
    (when (>= (* f1 f2) 0.0)
      (throw (ex-info "No root within the given boundaries"
                      {:x1-and-f1 [x1 f1]
                       :x2-and-f2 [x2 f2]})))

    (loop [iters 0
           x1 x1
           f1 f1
           x2 x2
           f2 f2]
      (cond
        ;; something went wrong
        (>= iters 30) (throw (ex-info "Maximum number of iterations exceeded"
                                      {:iters iters
                                       :bounds [lower upper]
                                       :current-bounds [x1 x2]}))
        ;; if we're within the tolerance, pick the closest of x1 and x2:
        (<= (abs (- x2 x1)) accuracy) (if (< (abs f1) (abs f2))
                                        x1
                                        x2)
        ;; else linear interpolation between x1 and x2a
        :else (let [x3 (- x2 (/ f2 (/ (- f2 f1)
                                      (- x2 x1))))
                    f3 (f x3)]
                (if (<= (* f3 f2) 0.0)
                  (recur (inc iters) x2 f2 x3 f3)
                  (let [f1' (* f1 (/ f2 (+ f2 f3)))]
                    (recur (inc iters) x1 f1' x3 f3))))))))


