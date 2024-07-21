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

(ns  ^{:doc "A library for computing moon phases"
       :author "Jean Niklas L'orange"}
    io.olympos.selene.moon
    (:require [cljc.java-time.zoned-date-time :as zdt]
              [cljc.java-time.temporal.chrono-unit :as chrono-unit]
              [io.olympos.selene.julian :as julian]
              [io.olympos.selene.sun :as sun]
              [io.olympos.selene.pegasus :as pegasus]
              [io.olympos.selene.math-util :refer [pi tau frac sin]]))


(defn position-equatorial-phi [jd]
  (let [T (julian/century jd)
        L0        (frac (+ 0.606433 (* 1336.855225 T)))
        l  (* tau (frac (+ 0.374897 (* 1325.552410 T))))
        ls (* tau (frac (+ 0.993133 (* 99.99736100 T))))
        D  (* tau (frac (+ 0.827361 (* 1236.853086 T))))
        F  (* tau (frac (+ 0.259086 (* 1342.227825 T))))

        D2 (* 2.0 D)
        l2 (* 2.0 l)
        F2 (* 2.0 F)

        dL (+ (+ (* 22640.0 (sin l)))
              (- (* 4586.0 (sin (- l D2))))
              (+ (* 2370.0 (sin D2)))
              (+ (* 769.0 (sin l2)))
              (- (* 668.0 (sin ls)))
              (- (* 412.0 (sin F2)))
              (- (* 212.0 (sin (- l2 D2))))
              (- (* 206.0 (sin (+ l ls (- D2)))))
              (+ (* 192.0 (sin (+ l D2))))
              (- (* 165.0 (sin (- ls D2))))
              (- (* 125.0 (sin D)))
              (- (* 110.0 (sin (+ l ls))))
              (+ (* 148.0 (sin (- l ls))))
              (- (* 55.0  (sin (- F2 D2)))))

        l-moon (* tau (frac (+ L0 (/ dL 1296000.0))))]
    l-moon))

(def phases
  "A list of all the phases used by this library, in their order in the
  lunar month. ::new is considered the start of the month."
  [::new ::waxing-crescent ::first-quarter ::waxing-gibbous
   ::full ::waning-gibbous ::last-quarter ::waning-crescent])

(def phase->angle
  "Returns the angle of a given phase"
  ;; 0, pi/4, 2*pi/4, 3*pi/4, ...
  {::new 0.0
   ::waxing-crescent 0.7853981633974483
   ::first-quarter 1.5707963267948966
   ::waxing-gibbous 2.356194490192345
   ::full 3.141592653589793
   ::waning-gibbous 3.9269908169872414
   ::last-quarter 4.71238898038469
   ::waning-crescent 5.497787143782138})

(defn angle->last-phase-change
  "Converts an angle, in radians, to the last moon phase change."
  [rad]
  (let [normalized (mod rad tau)]
    (condp > normalized
      0.7853981633974483 ::new
      1.5707963267948966 ::waxing-crescent
      2.356194490192345  ::first-quarter
      3.141592653589793  ::waxing-gibbous
      3.9269908169872414 ::full
      4.71238898038469   ::waning-gibbous
      5.497787143782138  ::last-quarter
      ::waning-crescent)))

(def phase-after-this
  "A mapping from the current phase to the next one."
  {::new ::waxing-crescent
   ::waxing-crescent ::first-quarter
   ::first-quarter ::waxing-gibbous
   ::waxing-gibbous ::full
   ::full ::waning-gibbous
   ::waning-gibbous ::last-quarter
   ::last-quarter ::waning-crescent
   ::waning-crescent ::new})

(defn angle->closest-phase
  "Converts an angle, in radians, to the closest matching moon phase.
  The closest matching moon phase may not have happened yet: This
  means this is not a good fit for calendars to tell when the phase changes."
  [rad]
  (let [normalized (mod rad tau)]
    ;; These are the midpoints between 0, pi/4, 2*pi/4, 3*pi/4, ...
    (condp > normalized
      0.39269908169872414 ::new
      1.1780972450961724 ::waxing-crescent
      1.9634954084936207 ::first-quarter
      2.748893571891069 ::waxing-gibbous
      3.5342917352885173 ::full
      4.319689898685966 ::waning-gibbous
      5.105088062083414 ::last-quarter
      5.8904862254808625 ::waning-crescent
      ::new)))

(defn angle->upcoming-phase-change
  "Converts an angle, in radians, to the upcoming moon phase change."
  [rad]
  (phase-after-this (angle->last-phase-change rad)))

(def ^:private sun-light-time-tau (/ 8.32 (* 1440.0 36525.0)))

(defn- phase-offset-fn
  "Returns a function taking in the julian century, converting it over
  to a julian date in jd's time zone and returns the offset of the
  target phase."
  [jd target-phase]
  ;; Computing the Julian date is necessary to get the true anomaly, which may
  ;; shift from time zone to time zone... I think. Which is a bit weird tbh, I
  ;; may have to dig a little deeper here.
  (fn [t]
    (let [sun-phi (sun/position-equatorial-phi (julian/at-century jd (- t sun-light-time-tau)))
          moon-phi (position-equatorial-phi (julian/at-century jd t))]
      (loop [diff (- moon-phi sun-phi target-phase)]
        (if (<= 0.0 diff)
          (- (rem (+ diff pi) tau) pi)
          (recur (+ diff tau)))))))

(defn- phase-angle* [jd]
  (let [sun-phi (sun/position-equatorial-phi (julian/at-century jd (- (julian/century jd)
                                                                      sun-light-time-tau)))
        moon-phi (position-equatorial-phi jd)]
    (mod (- moon-phi sun-phi) tau)))

(defn phase-angle
  "Returns the phase angle of the given datetime, in radians."
  [dt]
  (-> dt julian/from-datetime phase-angle*))

(defn closest-phase
  "Returns the closest matching moon phase of the given datetime. The
  closest matching moon phase may not have happened yet: This means
  this is not a good fit for calendars -- use phase-change-on-date
  instead if this is important to you."
  [dt]
  (angle->closest-phase (phase-angle dt)))

(defn last-phase-change
  "Returns the last moon phase change that happened before the given
  datetime."
  [dt]
  (angle->last-phase-change (phase-angle dt)))

(defn upcoming-phase-change
  "Returns the first moon phase change that will happen after the given
  datetime."
  [dt]
  (angle->upcoming-phase-change (phase-angle dt)))

(defn- start-of-today [dt]
  (zdt/truncated-to dt chrono-unit/days))

(defn- start-of-tomorrow [dt]
  (start-of-today (zdt/plus-days dt 1)))

(defn phase-change-on-date
  "Returns the phase we changed into on this date, if any. More
  precicely, this returns the transition between the start of this day
  and the end of this day. Returns nil if there's no phase change
  today.

  dt must be a datetime, as this needs a time zone to know exactly
  when the day starts and ends. Use (.atStartOfDay date zone-id) if
  you have a java.time.LocalDate instead."
  [dt]
  (let [p1 (last-phase-change (start-of-today dt))
        p2 (last-phase-change (start-of-tomorrow dt))]
    (if (not= p1 p2)
      p2
      nil)))

(def ^:private dT (/ 7.0 36525.0)) ;; step rate: 1 week
(def ^:private accuracy (/ 0.5 1440.0 36525.0)) ;; accuracy: 30 seconds. May want to jank this up

(defn next-phase-time
  "Returns the time of the first phase of type target-phase happening
  after dt, off by at most 30 seconds from the approximate value.

  Due to calculation inaccuracies,

    (let [x (next-phase-time dt phase)
          y (next-phase-time x phase)]
      (approx-equal? x y))

  may or may not return true. If you want to make a sequence of the
  same phase, check for the next event 1 week after the current event."
  [dt target-phase]
  (let [jd (julian/from-datetime dt)
        f (phase-offset-fn jd (phase->angle target-phase))]
    (loop [t0 (julian/century jd)
           d0 (f t0)

           t1 (+ t0 dT)
           d1 (f t1)]
      ;; step until we find the right interval
      (if (or (> (* d0 d1) 0.0) (< d1 d0))
        (let [t2 (+ t1 dT)]
          (recur t1 d1 t2 (f t2)))

        ;; use the pegasus method to find the exact time:
        (let [tphase (pegasus/calculate t0 t1 accuracy f)
              tjd (julian/at-century jd tphase)]
          (.-dt tjd))))))

(defn- phases* [{:keys [time phase] :as prev-phase}]
  (lazy-seq
   (let [p (phase-after-this phase)
         dt (next-phase-time time p)
         ret {:phase p, :time dt}]
     (cons ret (phases* ret)))))

(defn phase-seq
  "Returns a lazy seq of all the phases happening after dt, along with
  their timestamp.

  Values will be on the form {:time dt, :phase phase}"

  ;; TODO: Support reverse and the option to skip unwanted phases.
  [{:keys [time]}]
  (phases* {:time time
            :phase (last-phase-change time)}))
