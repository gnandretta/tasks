(ns tasks.core
  (:require [tasks.fs :as fs]
            [tasks.parser :as p]))

(defn find-tasks [path]
  (-> path
      fs/read-file
      p/parse))

(comment
  (find-tasks "resources/tasks.md"))
