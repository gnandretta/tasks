(ns tasks.fs
  (:require [clojure.core.async :refer [chan put!]]
            glob
            fs))

(defn ls [pattern]
  (let [c (chan)]
    (glob pattern (fn [err paths]
                    (when err (throw err))
                    (put! c paths)))
    c))

(defn read-file [path]
  (.readFileSync fs path "utf8"))
