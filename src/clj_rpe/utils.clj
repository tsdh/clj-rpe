(ns clj-rpe.utils
  (:require [flatland.ordered.set :as os])
  (:import (java.util Collection Iterator Enumeration))
  (:import (java.lang.reflect Method Field)))

(defn into-oset
  "Return an ordered set representation of `x' or the union of `s' and `x'.
  For a single-valued object or a map, an ordered set containing it is
  returned.  For multi-valued objects (Collections, Iterators, Enumerations,
  arrays), the returned ordered set contains all their values."
  ([x]
     (cond
      (nil? x)                  (os/ordered-set)
      (set? x)                  x
      (or
       (instance? Collection x)
       (.isArray (class x)))    (apply os/ordered-set (seq x))
       (instance? Iterator x)    (apply os/ordered-set (iterator-seq x))
       (instance? Enumeration x) (loop [^Enumeration enum x,
                                        vals (transient (os/ordered-set))]
                                   (if (.hasMoreElements enum)
                                     (let [n (.nextElement enum)]
                                       (recur enum (conj! vals n)))
                                     (persistent! vals)))
       :else                     (os/ordered-set x)))
  ([s x]
     (into (into-oset s) (into-oset x))))

(defn invoke-method
  "Returns an ordered set of `o's `mname' instance method's return value.
  If there's no such method, return an empty ordered set."
  [o mname]
  (try
    (into-oset
     (clojure.lang.Reflector/invokeInstanceMethod
      o (name mname) (to-array [])))
    (catch Exception _ (os/ordered-set))))

(defn access-field
  "Return an ordered set of `o's `fname' field value.
  If there's no such field, return an empty ordered set."
  [o fname]
  (try
    (into-oset
     (clojure.lang.Reflector/getInstanceField
      o (name fname)))
    (catch Exception _ (os/ordered-set))))
