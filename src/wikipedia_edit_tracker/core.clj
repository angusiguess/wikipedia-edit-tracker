(ns wikipedia-edit-tracker.core
  (:require [wikipedia-edit-tracker.handler.irc-data :as irc-data]
            [wikipedia-edit-tracker.producer.irc :as irc]
            [clojure.core.async :as a]))

(defn file-writer [channel filename]
  (a/go (with-open [writer (clojure.java.io/writer filename :append true)]
          (a/<! (a/go-loop []
                  (when-let [msg (a/<! channel)]
                    (.write writer (pr-str msg))
                    (recur)))))))

(defn -main [& args]
  (let [{:keys [shutdown publish]} (irc/irc-supervisor "irc.wikimedia.org" 6667 "goose" "#en.wikipedia")
        to-file> (a/chan 1024)
        _ (a/pipeline 8 to-file> (irc-data/tx-process-irc-data) publish)
        write-producer (file-writer to-file> "edits.txt")]
    (a/<!! write-producer)))
