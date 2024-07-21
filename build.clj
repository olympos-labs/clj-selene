(ns build
  (:require [clojure.tools.build.api :as b]))

(def lib 'io.olympos/selene)
(def version (format "1.0.0"))
(def class-dir "target/classes")
(def jar-file "target/clj-selene.jar")

;; delay to defer side effects (artifact downloads)
(def basis (delay (b/create-basis {:project "deps.edn"})))

(defn clean [_]
  (b/delete {:path "target"}))

(defn jar [_]
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :basis @basis
                :src-dirs ["src"]})
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file jar-file}))
