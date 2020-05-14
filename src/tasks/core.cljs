(ns tasks.core
  (:require [tasks.fs :as fs]
            [tasks.parser :as p]
            [tasks.prompt :refer [prompt]]
            [clojure.core.async :refer [<! chan close! go put!]]
            [clojure.string :as str]))

(defn find-tasks-in-files [paths]
  (let [c (chan)]
    (doseq [path paths]
      (put! c {:path path
               :tree (-> path fs/read-file p/parse)}))
    (close! c)
    c))

(defn find-tasks [pattern]
  (go (let [c (find-tasks-in-files (<! (fs/ls pattern)))]
        (loop [xs []]
          (when-some [x (<! c)]
            (recur (conj xs x)))
          xs))))

(defn flatten-tree ; TODO rename
  ([tree] (flatten-tree tree 1))
  ([tree lvl]
   (reduce (fn [xs [k v]]
             (cond
               (string? k)
               (apply conj
                      xs
                      (str (apply str (repeat lvl "#")) " " k)
                      (flatten-tree v (inc lvl)))
               (= k :tasks)
               (apply conj xs (map :raw v))
               :else xs))
           []
           tree)))

(comment
  (go (->> (<! (find-tasks "resources/**/*.md"))
           (mapcat (comp flatten-tree :tree))
           (map (fn [s]
                  (if (str/starts-with? s "#")
                    {:disabled s}
                    s)))
           prompt)))
