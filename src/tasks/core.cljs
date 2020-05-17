(ns tasks.core
  (:require [tasks.fs :as fs]
            [tasks.parser :as p]
            [clojure.core.async :refer [<! chan close! go put!]]))

(defn find-tasks-in-files [paths]
  (let [c (chan)]
    (doseq [path paths]
      (put! c (p/parse (fs/read-file path)
                       path)))
    (close! c)
    c))

(defn find-tasks [pattern]
  (go (let [c (find-tasks-in-files (<! (fs/ls pattern)))]
        (loop [xs []]
          (when-some [x (<! c)]
            (recur (conj xs x)))
          xs))))

(comment
  (require '[clojure.pprint :refer [pprint]])
  (go (->> (<! (find-tasks "resources/**/*.md"))
           pprint)))
