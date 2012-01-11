(defproject clj-rpe "0.0.1-SNAPSHOT"
  :description "Regular path expressions for Java object networks and Clojure
  structures."
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [ordered "[0.3,)"]]
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :jar-exclusions [#"(?:^|/).git/"
                   #"leiningen/"]
  :warn-on-reflection true)

