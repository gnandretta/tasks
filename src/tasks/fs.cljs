(ns tasks.fs
  (:require [clojure.string :as s]
            [path :as p]
            fs))

(defn dir? [path]
  (and (fs/existsSync path)
       (.isDirectory (fs/lstatSync path))))

(defn file? [path]
  (and (fs/existsSync path)
       (.isFile (fs/lstatSync path))))

(defn ls [dir]
  (map (partial p/join dir)
       (fs/readdirSync dir)))

(defn find-files [dir ext]
  (reduce (fn [paths path]
            (cond
              (and (file? path) (s/ends-with? path (str "." ext)))
              (conj paths path)

              (dir? path)
              (into paths (find-files path ext))

              :else paths))
          []
          (ls dir)))

(defn read-file [path]
  (fs/readFileSync path "utf8"))
