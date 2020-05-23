(ns tasks.core
  (:require [tasks.fs :as fs]
            [tasks.parser :as p]
            [clojure.core.async :as a :refer [<! chan close! go put!]]
            [clojure.string :as s]))

(defn find-tasks-in-files [paths]
  (let [c (chan)]
    (doseq [path paths]
      (put! c (p/parse (fs/read-file path)
                       path)))
    (close! c)
    c))

(defn find-tasks [pattern]
  (a/into [] (find-tasks-in-files (<! (fs/ls pattern)))))

(defn filter-tasks [pred nodes]
  (->> nodes
       (map (fn [[props sections]]
              (let [sections (filter-tasks pred sections)
                    tasks (filter pred (:tasks props))]
                (if (and (empty? tasks) (empty? sections))
                  nil
                  [(assoc props :tasks tasks) sections]))))
       (filter some?)))

(defn ansi-escape [set reset]
  (fn [s] (if process.stdout.isTTY
            (str set s reset)
            s)))

(def bold (ansi-escape "\u001b[1m" "\u001b[22m"))
(def magenta (ansi-escape "\u001b[35m" "\u001b[39m"))
(def bright-magenta (ansi-escape "\u001b[95m" "\u001b[39m"))

(defn print-tasks [tasks]
  (doseq [task tasks]
    (println (:raw task))))

(defn print-node [[{:keys [path raw tasks]} nodes]]
  ;; probably better to handle file and heading node types separately—but figure out what to do
  ;; with empty lines
  (when path                                                ; file
    (println (-> path bright-magenta)))
  (when raw                                                 ; heading
    (println (-> raw magenta bold))
    (println))
  (print-tasks tasks)
  (println)
  (doseq [node nodes]
    (print-node node))
  (when path                                                ; file—shouldn't happen on last file
    (println)
    (println)))

(defn completed [task]
  (:completed? task))

(def pending (complement completed))

(defn task-includes [task s]
  (s/includes? (:text task) s))

(defn -main []
  (go (->> (<! (find-tasks "resources/**/*.md"))
           (filter-tasks (every-pred #(task-includes % "content") pending))
           (map print-node)
           doall)))

(set! *main-cli-fn* -main)
