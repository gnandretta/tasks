(ns tasks.parser
  (:require [clojure.string :as s]))

(defn parse-heading [{:keys [lines offset depth] :as state}]
  (when-let [[raw hashes text] (and (first lines)
                                  (re-matches #"(#+)\s+(.+)" (first lines)))]
    (when (> (count hashes) depth)
      (let [heading {:type :heading
                     :raw raw
                     :rank (count hashes)
                     :text (s/trim text)
                     :line offset}
            state (-> state
                      (update :lines rest)
                      (update :offset inc)
                      (assoc :depth (count hashes)))]
        [heading state]))))

(defn parse-task-multi-line [task {:keys [lines] :as state}]
  (let [line (first lines)]
    (if (or (not line)
            (re-matches #"\s*" line)
            (re-matches #"\s*([#*+-]|[0-9]+\.|```).*" line))
      [task state]
      (parse-task-multi-line (-> task
                                 (update :raw str "\n" line)
                                 (update :text str " " (s/trim line)))
                             (-> state
                                 (update :lines rest)
                                 (update :offset inc))))))

(defn parse-task-meta [task {:keys [lines] :as state}]
  (let [line (first lines)
        indent (apply str (repeat (:indent task) " "))
        meta-re (re-pattern (str indent "-.*"))]
    (if (and line (re-matches meta-re line))
      (parse-task-meta (update task :meta (fnil conj []) line)
                       (-> state
                                 (update :lines rest)
                                 (update :offset inc)))
      [task state])))

(defn parse-task [{:keys [lines offset] :as state}]
  (when-let [[raw check text] (re-matches #"- \[(.)?\] (.+)" (first lines))]
    (let [task {:raw raw
                :indent 2 ; for nested tasks needs to be derived
                :completed? (contains? #{"x" "X"} check)
                :text (s/trim text)
                :line offset}
          state (-> state
                    (update :lines rest)
                    (update :offset inc))
          [task state] (parse-task-multi-line task state)
          [task state] (parse-task-meta task state)]
      [task state])))

(defn parse-tasks [state]
  (loop [tasks [] {:keys [lines] :as state} state]
    (if (and (seq lines)
             (not (re-matches #"(#+)\s+(.+)" (first lines))))
      (if-let [[task state] (parse-task state)]
        (recur (conj tasks task) state)
        (recur tasks (-> state
                         (update :lines rest)
                         (update :offset inc))))
      [tasks state])))

(declare parse-node)

(defn parse-section [state]
  (when-let [[heading state] (parse-heading state)]
    (parse-node heading state)))

(defn parse-sections [{:keys [depth] :as state}]
  (loop [sections [] state state]
    (if-let [[section state] (parse-section state)]
      (recur (conj sections section) state)
      [sections (assoc state :depth depth)])))

;; TODO Check the empty case.
(defn parse-node [props state]
  (let [[tasks state] (parse-tasks state) ; consumes lines until next heading/section
        [sections state] (parse-sections state) ; if there's not a heading in the next line won't parse a thing
        node [(assoc props :tasks tasks) sections]]
    [node state]))

(defn parse [md path]
  (first (parse-node {:type :file :path path}
                     {:lines (s/split-lines md)
                      :offset 0
                      :depth 0})))
