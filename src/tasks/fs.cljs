(ns tasks.fs
  (:require fs))

(defn read-file [path]
  (.readFileSync fs path "utf8"))
