(ns define-acronym.core
  (:require [clojure.string :as s]
            [clojure.core.reducers :as r]))

(def dictionary (atom {}))

(def word-list-urls (atom ["adjectives.txt" "nouns.txt"]))
(def word-lists-loaded (atom 0))

(def acronym (atom nil))

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
  "Check if all word lists have been loaded and if so, call display-meaning and
  then hook up the re-roll button to call-meaning."
  []
  (when (= @word-lists-loaded (count @word-list-urls))
    (do (.log js/console "The dictionary is loaded.")
        (display-meaning)
        (.addEventListener roll-button-el "click" display-meaning))))

(defn process-word-list
  "Extract words, add them to the dictionary, mark the word list as loaded,
  and call process-dictionary."
  [text]
  (let [words (map s/capitalize (s/split-lines text))]
    (swap! dictionary #(r/reduce add-to-dictionary % words))
    (swap! word-lists-loaded inc)
    (process-dictionary)))

(defn with-text
  "Get the body of an http response as text and pass it to a callback."
  [response callback]
  (.then (.text response) callback))

(defn fetch-word-list [url]
  (fetch url #(with-text % process-word-list)))

(defn query-param
  "Get a query parameter from the current page url by name."
  [param]
  (.get (js/URLSearchParams. (aget js/window "location" "search")) param))

(defn query-params
  "Get query parameters from the current page url by name as a vector."
  [param]
  (.getAll (js/URLSearchParams. (aget js/window "location" "search")) param))

(defn init
  "Get the acronym to define, display it, and load the word lists"
  []
  (let [preset-param (query-param "preset")
        word-list-param (query-params "w")
        acronym-param (query-param "a")]
    (when (= "false" preset-param)
      (swap! word-list-urls #{identity []}))
    (when (not (empty? word-list-param))
      (swap! word-list-urls
             #(into [] (concat % word-list-param))))
    (when acronym-param
      (swap! acronym #(identity acronym-param))
      (aset acronym-el "textContent" (s/upper-case acronym-param))
      (doall (map fetch-word-list @word-list-urls)))))
