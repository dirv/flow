(ns pinaclj.quote-transform-spec
  (:require [speclj.core :refer :all]
            [pinaclj.quote-transform :refer :all]))

(def noQuotes
  "abc")

(def singleQuotes
  "'singlestart' 'singleend' let's")

(def doubleQuotes
  "\"doublestart\" \"doubleend\"")

(def inside-html
  "<p>''</p>")

(def inside-code-block
  "<p><code>''\"\"</code></p>")

(def attributes
  "<img src=\"t'est\" />")

(def quote-sentence
  "\"This code is not clean, he said.\"")

(def code-with-class
  "<p><code class=\"clojure\">''</code></p>")

(describe "replace quotes"
  (it "returns string with no quotes"
    (should= noQuotes (convert noQuotes)))

  (it "converts starting single quote to &lsquo;"
    (should-contain "&lsquo;singlestart" (convert singleQuotes)))

  (it "converts inner single quote to &rsquo;"
    (should-contain "singlestart&rsquo;" (convert singleQuotes)))

  (it "converts inner single quote to &lsquo;"
    (should-contain "&lsquo;singleend" (convert singleQuotes)))

  (it "converts ending single quote to &rsquo;"
    (should-contain "singleend&rsquo;" (convert singleQuotes)))

  (it "converts apostrophe to &rsquo;"
    (should-contain "let&rsquo;s" (convert singleQuotes)))

  (it "converts starting double quote to &lsquo;"
    (should-contain "&ldquo;doublestart" (convert doubleQuotes)))

  (it "converts inner double quote to &rsquo;"
    (should-contain "doublestart&rdquo;" (convert doubleQuotes)))

  (it "converts inner double quote to &lsquo;"
    (should-contain "&ldquo;doubleend" (convert doubleQuotes)))

  (it "converts ending double quote to &rsquo;" (should-contain "doubleend&rdquo;" (convert doubleQuotes)))

  (it "converts inside html"
    (should= "<p>&lsquo;&rsquo;</p>" (convert inside-html)))

  (it "does not convert inside code block"
    (should= inside-code-block (convert inside-code-block)))

  (it "does not convert attribute quotes"
    (should= attributes (convert attributes)))

  (it "quotes after punctuation"
    (should-contain ".&rdquo;" (convert quote-sentence)))

  (it "does not convert inside code element with class block"
    (should= code-with-class (convert code-with-class))))
