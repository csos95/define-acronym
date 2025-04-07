(ns define-acronym.core
  (:require [clojure.string :as s]
            [clojure.core.reducers :as r]))

(def adjectives (atom {}))
(def nouns (atom {}))
(def dictionary (atom {}))

(def loaded-adjectives (atom false))
(def loaded-nouns (atom false))

(def acronym (atom ""))

(def acronym-el (.querySelector js/document "#acronym"))
(def meaning-el (.querySelector js/document "#meaning"))
(def roll-button-el (.querySelector js/document "#roll-button"))

(defn fetch [url callback]
  (.then (js/fetch url) callback))

;; TODO: exclude already used words?
(defn get-word
  "Get a random word starting with the given letter."
  [letter]
  (let [words (get @dictionary (keyword letter))
        word (get words (rand (count words)))]
    (print (str "There are " (count words) " words starting with '" letter "'."))
    word))

(defn define-acronym
  "Define an acronym meaning by getting a word for each character."
  [acronym]
  (print (str "Defining '" (s/upper-case acronym) "'."))
  (map get-word (seq (s/upper-case acronym))))

(defn add-to-dictionary
  "A dictionary is a map of single character keywords to a vector of words
  starting with that letter."
  [dictionary word]
  (let [key (keyword (first word))]
    (if (get dictionary key)
      (update-in dictionary [key] #(conj % word))
      (assoc dictionary key [word]))))

(defn display-meaning
  "Generate an acronym meaning and display it in the html page."
  []
  (let [acronym @acronym
        meaning (define-acronym acronym)
        meaning-str (s/triml (r/reduce #(str %1 " " %2) meaning))]
    (print (str "The acronym '" acronym "' stands for '" meaning-str "'."))
    (aset meaning-el "textContent" meaning-str)))

(defn process-dictionary
  "Check if both word lists have been loaded and if so, call display-meaning and
  then hook up the re-roll button to call-meaning."
  []
  (when (and @loaded-adjectives @loaded-nouns)
    (do (.log js/console "The dictionary is loaded.")
        (display-meaning)
        (.addEventListener roll-button-el "click" display-meaning))))

(defn process-word-list
  "Extract words, add them to the dictionaries, mark the word list as loaded,
  and call process-dictionary."
  [list-dict list-loaded text]
  (let [words (map s/capitalize (s/split-lines text))]
    ;; I added separate dictionaries because I might want to extend this in the
    ;; future with options to use different combinations of dictionaries.
    ;; This would especially be useful if I add custom dictionaries.
    ;; (swap! list-dict #(r/reduce add-to-dictionary % words))
    (swap! dictionary #(r/reduce add-to-dictionary % words))
    (swap! list-loaded #(identity true))
    (process-dictionary)))

(defn process-adjectives
  "Process the adjectives word list."
  [text]
  (process-word-list adjectives loaded-adjectives text))

(defn process-nouns
  "Process the nouns word list."
  [text]
  (process-word-list nouns loaded-nouns text))

(defn with-text
  "Get the body of an http response as text and pass it to a callback."
  [response callback]
  (.then (.text response) callback))

(defn init
  "Get the acronym to define, display it, and load the word lists"
  []
  (let [acronym-param (.get (js/URLSearchParams. (aget js/window "location" "search")) "q")]
    (print (str "acronym: " acronym-param))
    (swap! acronym #(identity acronym-param))
    (aset acronym-el "textContent" (s/upper-case acronym-param)))
  (fetch "adjectives.txt" #(with-text % process-adjectives))
  (fetch "nouns.txt" #(with-text % process-nouns)))
