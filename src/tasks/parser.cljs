(ns tasks.parser
  (:require [clojure.string :as s]))

(defn parse-header [state]
  (when-let [[_ hashes heading] (re-matches #"(#+)\s+(.+)"
                                            (first (:lines state)))]
    (-> state
        (update :headings (fn [headings]
                            (-> (take (dec (count hashes))
                                      (concat headings (repeat nil)))
                                vec
                                (conj heading))))
        (update :lines rest))))

(defn parse-task [state]
  (when-let [raw (re-matches #"- \[.?\] .+" (first (:lines state)))]
    (let [task {:raw raw}]
      (-> state
          (update-in (concat [:tree] (:headings state) [:tasks])
                     (fnil conj [])
                     task)
          (update :lines rest)))))

(defn parse-irrelevant-line [state]
  (update state :lines rest))

(defn parse-line [state]
  (or (parse-header state)
      (parse-task state)
      (parse-irrelevant-line state)))

(defn parse [md]
  (loop [state {:tree (sorted-map)
                :lines (s/split-lines md)
                :headings []}]
    (if-let [new-state (and (seq (:lines state))
                            (parse-line state))]
      (recur new-state)
      state)))
