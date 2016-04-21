(ns wikipedia-edit-tracker.core
  (:require [wikipedia-edit-tracker.handler.irc-data :as irc-data]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(defn -main [& args]
  (println (into [] (irc-data/tx-process-irc-data) [{:command "PRIVMSG"
                                             :params ["#en.wikipedia" "14[[07List of intelligence gathering disciplines14]]4 10 02https://en.wikipedia.org/w/index.php?diff=715127585&oldid=705758737 5* 03Dainomite 5* (+24) 10/* See also */"]
                                             :raw ":rc-pmtpa!~rc-pmtpa@special.user PRIVMSG #en.wikipedia :14[[07List of intelligence gathering disciplines14]]4 10 02https://en.wikipedia.org/w/index.php?diff=715127585&oldid=705758737 5* 03Dainomite 5* (+24) 10/* See also */",
                                             :nick "rc-pmtpa",
                                             :user "~rc-pmtpa",
                                             :host "special.user",
                                             :target "#en.wikipedia",
                                             :text "14[[07List of intelligence gathering disciplines14]]4 10 02https://en.wikipedia.org/w/index.php?diff=715127585&oldid=705758737 5* 03Dainomite 5* (+24) 10/* See also */"}])))
