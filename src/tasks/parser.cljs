(ns tasks.parser
  (:require [clojure.string :as s]))

(defn read-line [{:keys [lines] :as state}]
  (when (seq lines)
    (let [line (first lines)
          state (-> state
                    (update :lines rest)
                    (update :offset inc))]
      [line state])))

(defn parse-line [state re]
  (when-let [[line state] (read-line state)]
    (when-let [match (re-matches re line)]
      [match state])))

(defn parse-heading [{:keys [offset depth] :as state}]
  (let [[[raw hashes text :as m] state*] (parse-line state #"(#+)\s+(.+)")]
    (when (and m (> (count hashes) depth))
       (let [heading {:type :heading
                      :raw raw
                      :rank (count hashes)
                      :text (s/trim text)
                      :line (inc offset)}
             state* (assoc state* :depth (count hashes))]
         [heading state*]))))

(defn parse-task-multi-line [task state]
  (let [[line state*] (read-line state)]
    (if (or (not line)
            (re-matches #"\s*" line)
            (re-matches #"\s*([#*+-]|[0-9]+\.|```).*" line))
      [task state]
      (parse-task-multi-line (-> task
                                 (update :raw str "\n" line)
                                 (update :text str " " (s/trim line)))
                             state*))))

(defn parse-task-meta [task state]
  (let [indent (apply str (repeat (:indent task) " "))
        meta-re (re-pattern (str indent "-.*"))
        [line state*] (parse-line state meta-re)]
    (if line
      (parse-task-meta (update task :meta (fnil conj []) line)
                       state*)
      [task state])))

(defn parse-task [{:keys [offset] :as state}]
  (let [[[raw check text :as m] state*] (parse-line state #"- \[(.)?\] (.+)")]
    (when m
      (let [task {:raw raw
                  :indent 2 ; for nested tasks needs to be derived
                  :completed? (contains? #{"x" "X"} check)
                  :text (s/trim text)
                  :line (inc offset)}
            [task state*] (parse-task-multi-line task state*)
            [task state*] (parse-task-meta task state*)]
        [task state*]))))

(defn parse-tasks [state]
  (loop [tasks [] state state]
    (let [[line state*] (read-line state)]
      (if (and line (not (re-matches #"(#+)\s+(.+)"
                                     line)))
        (if-let [[task state] (parse-task state)]
          (recur (conj tasks task) state)
          (recur tasks state*))
        [tasks state]))))

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
