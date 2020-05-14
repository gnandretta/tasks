(ns tasks.prompt
  (:require [clojure.core.async :refer [chan put!]]
            [enquirer :as e]))

(defn prompt [choices]
  (let [c (chan)]
    (->
     (e/prompt (clj->js {:type "select"
                         :message "pick"
                         :choices choices}))
     (.then (fn [resp] (put! c resp))))
    c))
