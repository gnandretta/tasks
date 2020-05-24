(ns tasks.core
  (:require [tasks.fs :as fs]
            [tasks.parser :as p]
            [clojure.core.async :as a :refer [<! chan close! go put!]]
            [clojure.string :as s]))

(defn find-tasks [paths]
  (let [c (chan)]
    (doseq [path paths]
      (put! c (p/parse (fs/read-file path)
                       path)))
    (close! c)
    (a/into [] c)))

(defn filter-tasks [pred nodes]
  (->> nodes
       (map (fn [[props sections]]
              (let [sections (filter-tasks pred sections)
                    tasks (filter pred (:tasks props))]
                (if (and (empty? tasks) (empty? sections))
                  nil
                  [(assoc props :tasks tasks) sections]))))
       (filter some?)))

(defn first-task [nodes]
  (some (fn [[{:keys [tasks]} sections]]
          (or (first tasks) (first-task sections)))
        nodes))

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

(defn parse-args [args]
  (let [parsed-args (loop [args args parsed-args {:paths     []
                                                  :filter-fn pending
                                                  :search-fn (constantly true)
                                                  :edit? false}]
                      (if-let [arg (first args)]
                        (case arg       ; think about names
                          ("-a" "--all") (recur (rest args)
                                                (assoc parsed-args
                                                  :filter-fn (constantly true)))
                          ("-c" "--completed") (recur (rest args)
                                                      (assoc parsed-args
                                                        :filter-fn completed))
                          ("-p" "--pending") (recur (rest args)
                                                    (assoc parsed-args
                                                      :filter-fn pending))
                          ("-s" "--search") (recur (rest (rest args)) ; what happens if there's no search string?
                                                   (assoc parsed-args
                                                     :search-fn #(task-includes % (first (rest args)))))
                          ("-e" "--edit") (recur (rest args)
                                                 (assoc parsed-args :edit? true))
                          (recur (rest args)
                                 (update parsed-args :paths conj arg)))
                        parsed-args))
        parsed-args (update parsed-args
                            :paths
                            (fn [paths]
                              (mapcat (fn [path]
                                        (cond
                                          (fs/dir? path) (fs/find-files path "md")
                                          (and (fs/file? path) (s/ends-with? path ".md")) path
                                          :else [])) ; handle unknown paths
                                      (if (empty? paths) [(process.cwd)] paths))))]
    parsed-args))

(def spawn (.-spawn (js/require "child_process")))

(defn edit-file
  ([path] (edit-file path nil))
  ([path line]
   (spawn (or process.env.EDITOR "vi")  ; ${EDITOR:-vi} doesn't work
          (clj->js (if (nil? line)
                     [path]
                     [(str "+" line) path]))
          #js {:stdio "inherit"})))

(defn -main [& args]
  (go (let [{:keys [paths filter-fn search-fn edit?]} (parse-args args)
            nodes (->> (<! (find-tasks paths))
                       (filter-tasks (every-pred filter-fn search-fn)))]
        (if edit?
          (when-let [{:keys [file line]} (first-task nodes)]
            (edit-file file line))
          (doall (map print-node nodes))))))

(set! *main-cli-fn* -main)
