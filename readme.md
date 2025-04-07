# What Does That Stand For, Anyway?

Find the[^1] meaning of that acronym you've always been curious about!

[^1]: Almost certainly incorrect unless you get *very* lucky.

![screenshot of the project defining "RB" to mean "Regular Bake"](assets/screenshot.png)

## Build

This project uses [shadow-cljs](https://github.com/thheller/shadow-cljs) to
compile [ClojureScript](https://clojurescript.org/) to javascript.
You'll need a java sdk and node.js installed.

To compile, run `npx shadow-cljs release frontend`.

The compiled output will be in `public/js`.

## Run

Use some http server to serve the `public` directory.

As an example, to serve with python's built in http server on port 8080, run `python -m http.server 8080`.
