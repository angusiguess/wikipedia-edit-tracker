(ns wikipedia-edit-tracker.producer.irc
  (:require [irclj.core :as irc]
            [clojure.core.async :as a]))

(defn irc-supervisor [server port nick irc-channel]
  (let [publish (a/chan 1024)
        error (a/chan)
        shutdown (a/chan)
        connection (irc/connect server port nick :callbacks {:privmsg (fn [irc type & s]
                                                                        (a/put! publish type))})]
    (a/go-loop []
      (println "Starting up")
      (irc/join connection "#en.wikipedia")
      (a/<! shutdown)
      (a/close! publish)
      (irc/kill connection)
      (println "IRC connection is shutting down."))
    {:shutdown shutdown
     :publish publish}))
