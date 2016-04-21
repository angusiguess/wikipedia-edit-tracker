(ns wikipedia-edit-tracker.handler.irc-data
  (:require [clojure.string :as str]))

;; Here's an example pulled from the irc listener in the format that comes from our library.
{:command "PRIVMSG"
 :params ["#en.wikipedia" "14[[07List of intelligence gathering disciplines14]]4 10 02https://en.wikipedia.org/w/index.php?diff=715127585&oldid=705758737 5* 03Dainomite 5* (+24) 10/* See also */"]
 :raw ":rc-pmtpa!~rc-pmtpa@special.user PRIVMSG #en.wikipedia :14[[07List of intelligence gathering disciplines14]]4 10 02https://en.wikipedia.org/w/index.php?diff=715127585&oldid=705758737 5* 03Dainomite 5* (+24) 10/* See also */",
 :nick "rc-pmtpa",
 :user "~rc-pmtpa",
 :host "special.user",
 :target "#en.wikipedia",
 :text "14[[07List of intelligence gathering disciplines14]]4 10 02https://en.wikipedia.org/w/index.php?diff=715127585&oldid=705758737 5* 03Dainomite 5* (+24) 10/* See also */"}

;; We want to do the following:
;; Pull command, nick, target, and text from this map.
;; Filter on the nick that publishes these changes, in this case rm-pmtpa.
;; Pull the text into separate values

(defn strip-colour-codes [s]
  (str/replace s #"[0-9]{0,2}" ""))

(defn parse-text
  "The text emitted by the bot has this format:
  [[Wikipedia Article Name]] <operation?> <Edit Diff URL> <Edit Comment>
  We want to transform this into a map with the format:
  {:title \"[[Wikipedia Article Name]]\"
   :operation <nil or operation>
   :diff-url <Edit Diff URL>
   :comment <Edit Comment>}"
  [s]
  (let [pattern #"(?x)(\[\[[^\]]*\]\])   #Match and capture the article name.
                  .* #Chomp
                  (https://[^\ ]+ ) #Match edit url
                  \s+(.*) # Match comment"]
    (-> (re-seq pattern s)
        first
        rest
        vec)))

(defn tx-process-irc-data []
  (let [get-keys (map #(select-keys % [:command :nick :target :text]))
        filter-nick (filter #(= (:nick %) "rc-pmtpa"))
        ;; Strip out mIRC colour codes. See http://www.mirc.com/colors.html
        strip-colours (map #(update %1 :text strip-colour-codes))
        parse-text (map #(update %1 :text parse-text))
        add-parsed-values (map (fn [event]
                                 (let [{:keys [text]} event
                                       [topic edit-url comment] text]
                                   (assoc event :topic topic
                                          :edit-url edit-url
                                          :comment comment))))]
    (comp get-keys filter-nick strip-colours parse-text add-parsed-values)))


(comment (into [] (tx-process-irc-data) [{:command "PRIVMSG"
                                          :params ["#en.wikipedia" "14[[07List of intelligence gathering disciplines14]]4 10 02https://en.wikipedia.org/w/index.php?diff=715127585&oldid=705758737 5* 03Dainomite 5* (+24) 10/* See also */"]
                                          :raw ":rc-pmtpa!~rc-pmtpa@special.user PRIVMSG #en.wikipedia :14[[07List of intelligence gathering disciplines14]]4 10 02https://en.wikipedia.org/w/index.php?diff=715127585&oldid=705758737 5* 03Dainomite 5* (+24) 10/* See also */",
                                          :nick "rc-pmtpa",
                                          :user "~rc-pmtpa",
                                          :host "special.user",
                                          :target "#en.wikipedia",
                                          :text "14[[07List of intelligence gathering disciplines14]]4 10 02https://en.wikipedia.org/w/index.php?diff=715127585&oldid=705758737 5* 03Dainomite 5* (+24) 10/* See also */"}]))

