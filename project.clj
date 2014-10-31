(defproject clj-rpe "1.2.0"
  :description "Regular path expressions for Java object networks and Clojure
  structures."
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.flatland/ordered "1.5.2"]]
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :jar-exclusions [#"(?:^|/).git/"
                   #"leiningen/"]
  :global-vars {*warn-on-reflection* true})
