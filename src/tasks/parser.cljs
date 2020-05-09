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

(defn parse-task-multi-line [task lines]
  (let [line (first lines)]
    (if (or (not line)
            (re-matches #"\s*" line)
            (re-matches #"\s*([#*+-]|[0-9]+\.|```).*" line))
      [task lines]
      (parse-task-multi-line (update task :raw str "\n" line)
                            (rest lines)))))
(defn parse-task-meta [task lines]
  (let [line (first lines)
        indent (apply str (repeat (:indent task) " "))
        meta-re (re-pattern (str indent "-.*"))]
    (if (and line (re-matches meta-re line))
      (parse-task-meta (update task :meta (fnil conj []) line)
                       (rest lines))
      [task lines])))

(defn parse-task [state]
  (when-let [[raw markup] (re-matches #"(- \[.?\] ).+" (first (:lines state)))]
    (let [task {:raw raw :indent (count markup)}
          lines (rest (:lines state))
          [task lines] (parse-task-multi-line task lines)
          [task lines] (parse-task-meta task lines)]
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
