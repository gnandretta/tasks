(ns tasks.core
  (:require [tasks.fs :as fs]
            [tasks.parser :as p]))

(defn find-tasks [path]
  (-> path
      fs/read-file
      p/parse))

(comment
  (require '[clojure.pprint :refer [pprint]])
  (pprint (find-tasks "resources/tasks.md"))
  (require '[clojure.core.async :refer [<! go]])
  (go (println (<! (fs/ls "resources/**/*.md")))))
