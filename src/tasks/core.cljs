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

(defn filter-tasks [pred nodes]
  (->> nodes
       (map (fn [[props sections]]
              (let [sections (filter-tasks pred sections)
                    tasks (filter pred (:tasks props))]
                (if (and (empty? tasks) (empty? sections))
                  nil
                  [(assoc props :tasks tasks) sections]))))
       (filter some?)))

(defn print-tasks [tasks]
  (doseq [task tasks]
    (println (:raw task))))

(defn print-node [[{:keys [path raw tasks]} nodes]]
  (println (or path raw)) ; probably better to handle file and heading node types separately
  (print-tasks tasks)
  (doseq [node nodes]
    (print-node node)))

(comment
  (require '[clojure.pprint :refer [pprint]])
  (go (->> (<! (find-tasks "resources/**/*.md"))
           (filter-tasks (complement :completed?))
           (map print-node)
           doall)))
